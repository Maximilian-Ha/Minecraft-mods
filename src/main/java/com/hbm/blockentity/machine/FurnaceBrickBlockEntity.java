package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.heater.HeaterFireboxBlockEntity;
import com.hbm.blockentity.machine.heater.HeaterFireboxBlockEntity.AshType;
import com.hbm.blocks.machine.FurnaceBrickBlock;
import com.hbm.inventory.menus.FurnaceBrickMenu;
import com.hbm.items.NtmItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFurnaceBrick.
 *
 * Ein gemauerter Ofen. Er schmilzt nach den normalen Ofenrezepten, aber schneller, wenn das
 * Eingelegte zu ihm passt: Ton und Netherrack viermal so schnell, Bruchstein, Sand und
 * Stammholz doppelt so schnell, alles andere in Normalzeit.
 *
 * Nebenbei faellt Asche an -- dieselbe Einteilung wie beim Feuerraum: Holz, Kohle oder
 * Sonstiges, je Sorte ein eigener Zaehler. Bei 2000 gesammelten Brennticks einer Sorte
 * springt eine Portion Pulver in den Aschefach.
 *
 * ABWEICHUNG: der Ausgabeplatz des Originals ist ein SlotSmelting und gibt beim Herausnehmen
 * Erfahrung. Der Port hat dafuer keinen Platztyp; seine Oefen benutzen durchgehend
 * SlotTakeOnly, und dabei bleibt es auch hier, damit der Ziegelofen sich nicht anders verhaelt
 * als der Eisen- oder Stahlofen daneben.
 */
public class FurnaceBrickBlockEntity extends MachineBaseBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_ASH = 3;

    /** Ticks je Schmelzvorgang, bevor die Brenngeschwindigkeit eingerechnet wird. */
    public static final int PROCESS_TIME = 200;

    /** Brennticks einer Aschesorte, bis eine Portion Pulver abfaellt. */
    private static final int ASH_THRESHOLD = 2000;

    private static final int[] SLOTS_TOP = new int[] { SLOT_INPUT };
    private static final int[] SLOTS_BOTTOM = new int[] { SLOT_OUTPUT, SLOT_FUEL, SLOT_ASH };
    private static final int[] SLOTS_SIDES = new int[] { SLOT_FUEL };

    /** Was der Ofen besonders gut kann, aus dem Original uebernommen. */
    private static final Map<Item, Integer> BURN_SPEED = new HashMap<>();

    static {
        BURN_SPEED.put(Items.CLAY_BALL, 4);
        // NICHT UEBERNOMMEN: ball_fireclay -- die Schamottekugel gibt es im Port nicht.
        BURN_SPEED.put(Blocks.NETHERRACK.asItem(), 4);
        BURN_SPEED.put(Blocks.COBBLESTONE.asItem(), 2);
        BURN_SPEED.put(Blocks.SAND.asItem(), 2);
        // Das Original nennt log und log2, also beide Stammholzbloecke von 1.7.10. In 1.21
        // ist daraus eine ganze Familie geworden; sie steht hier vollstaendig.
        for(var holz : new net.minecraft.world.level.block.Block[] {
                Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
                Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG }) {
            BURN_SPEED.put(holz.asItem(), 2);
        }
    }

    public int burnTime;
    public int maxBurnTime;
    public int progress;

    private int ashLevelWood;
    private int ashLevelCoal;
    private int ashLevelMisc;

    public FurnaceBrickBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FURNACE_BRICK.get(), pos, state, 4);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.furnaceBrick");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        boolean brannte = this.burnTime > 0;
        boolean geaendert = false;

        if(this.burnTime > 0) this.burnTime--;

        if(this.burnTime != 0 || (!this.slots.get(SLOT_FUEL).isEmpty() && !this.slots.get(SLOT_INPUT).isEmpty())) {

            if(this.burnTime == 0 && this.kannSchmelzen()) {

                ItemStack brennstoff = this.slots.get(SLOT_FUEL);
                this.maxBurnTime = this.burnTime = brennstoff.getBurnTime(RecipeType.SMELTING);

                if(this.burnTime > 0) {
                    geaendert = true;
                    this.verbrenne(brennstoff);
                }
            }

            if(this.burnTime > 0 && this.kannSchmelzen()) {

                this.progress += this.brenngeschwindigkeit();

                if(this.progress >= PROCESS_TIME) {
                    this.progress = 0;
                    this.schmelze();
                    geaendert = true;
                }
            } else {
                this.progress = 0;
            }
        }

        if(brannte != this.burnTime > 0) {
            geaendert = true;
            this.level.setBlock(this.worldPosition,
                    this.getBlockState().setValue(FurnaceBrickBlock.LIT, this.burnTime > 0), 3);
        }

        if(geaendert) this.setChanged();

        this.networkPackNT(15);
    }

    /** Ein Stueck Brennstoff wegnehmen und seine Asche gutschreiben. */
    private void verbrenne(ItemStack brennstoff) {

        AshType sorte = HeaterFireboxBlockEntity.getAshFromFuel(brennstoff);
        if(sorte == AshType.WOOD) this.ashLevelWood += this.burnTime;
        if(sorte == AshType.COAL) this.ashLevelCoal += this.burnTime;
        if(sorte == AshType.MISC) this.ashLevelMisc += this.burnTime;

        if(this.gibAscheAus(this.ashLevelWood, NtmItems.POWDER_ASH_WOOD.get())) this.ashLevelWood -= ASH_THRESHOLD;
        if(this.gibAscheAus(this.ashLevelCoal, NtmItems.POWDER_ASH_COAL.get())) this.ashLevelCoal -= ASH_THRESHOLD;
        if(this.gibAscheAus(this.ashLevelMisc, NtmItems.POWDER_ASH_MISC.get())) this.ashLevelMisc -= ASH_THRESHOLD;

        ItemStack rest = brennstoff.hasCraftingRemainingItem() ? brennstoff.getCraftingRemainingItem().copy() : ItemStack.EMPTY;
        brennstoff.shrink(1);
        if(brennstoff.isEmpty()) this.slots.set(SLOT_FUEL, rest);
    }

    /** Wahr, wenn eine Portion wirklich abgelegt wurde -- nur dann wird der Zaehler gesenkt. */
    private boolean gibAscheAus(int stand, Item asche) {

        if(stand < ASH_THRESHOLD) return false;

        ItemStack fach = this.slots.get(SLOT_ASH);

        if(fach.isEmpty()) {
            this.slots.set(SLOT_ASH, new ItemStack(asche));
            return true;
        }

        if(fach.is(asche) && fach.getCount() < fach.getMaxStackSize()) {
            fach.grow(1);
            return true;
        }

        return false;
    }

    public int brenngeschwindigkeit() {
        ItemStack eingabe = this.slots.get(SLOT_INPUT);
        if(eingabe.isEmpty()) return 1;
        return BURN_SPEED.getOrDefault(eingabe.getItem(), 1);
    }

    /** Schmelzergebnis nach den normalen Ofenrezepten, sonst ein leerer Stapel. */
    private ItemStack ergebnis() {

        ItemStack eingabe = this.slots.get(SLOT_INPUT);
        if(this.level == null || eingabe.isEmpty()) return ItemStack.EMPTY;

        SingleRecipeInput eingang = new SingleRecipeInput(eingabe.copy());

        for(var halter : this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            if(!halter.value().matches(eingang, this.level)) continue;
            return halter.value().getResultItem(this.level.registryAccess()).copy();
        }

        return ItemStack.EMPTY;
    }

    private boolean kannSchmelzen() {

        ItemStack ergebnis = this.ergebnis();
        if(ergebnis.isEmpty()) return false;

        ItemStack ausgabe = this.slots.get(SLOT_OUTPUT);
        if(ausgabe.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(ausgabe, ergebnis)) return false;

        int summe = ausgabe.getCount() + ergebnis.getCount();
        return summe <= this.getMaxStackSize() && summe <= ausgabe.getMaxStackSize();
    }

    private void schmelze() {

        ItemStack ergebnis = this.ergebnis();
        if(ergebnis.isEmpty()) return;

        ItemStack ausgabe = this.slots.get(SLOT_OUTPUT);
        if(ausgabe.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, ergebnis.copy());
        } else {
            ausgabe.grow(ergebnis.getCount());
        }

        this.removeItem(SLOT_INPUT, 1);
    }

    public int getBurnTimeScaled(int pixel) {
        return this.maxBurnTime <= 0 ? 0 : this.burnTime * pixel / this.maxBurnTime;
    }

    public int getProgressScaled(int pixel) {
        return this.progress * pixel / PROCESS_TIME;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot >= SLOT_OUTPUT) return false;
        if(slot == SLOT_FUEL) return stack.getBurnTime(RecipeType.SMELTING) > 0;
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch(direction) {
            case DOWN -> SLOTS_BOTTOM;
            case UP -> SLOTS_TOP;
            default -> SLOTS_SIDES;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= SLOT_OUTPUT;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.burnTime = tag.getInt("burnTime");
        this.maxBurnTime = tag.getInt("maxBurn");
        this.progress = tag.getInt("progress");
        this.ashLevelWood = tag.getInt("ashWood");
        this.ashLevelCoal = tag.getInt("ashCoal");
        this.ashLevelMisc = tag.getInt("ashMisc");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("burnTime", this.burnTime);
        tag.putInt("maxBurn", this.maxBurnTime);
        tag.putInt("progress", this.progress);
        tag.putInt("ashWood", this.ashLevelWood);
        tag.putInt("ashCoal", this.ashLevelCoal);
        tag.putInt("ashMisc", this.ashLevelMisc);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.burnTime);
        buf.writeInt(this.maxBurnTime);
        buf.writeInt(this.progress);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.burnTime = buf.readInt();
        this.maxBurnTime = buf.readInt();
        this.progress = buf.readInt();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FurnaceBrickMenu(id, inventory, this);
    }
}
