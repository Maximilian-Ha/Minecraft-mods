package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachinePollutingBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineDiFurnaceBlock;
import com.hbm.blocks.machine.MachineDiFurnaceExtensionBlock;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineDiFurnaceMenu;
import com.hbm.inventory.recipes.BlastFurnaceRecipe;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.items.NtmItems;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityDiFurnace.
 *
 * Zwei-Kammer-Ofen ("Blast Furnace" des Originals): zwei Zutaten, ein Brennstoffslot,
 * ein Ausgabeslot. Steht der Aufsatz (machine_difurnace_extension) direkt darueber,
 * laeuft der Ofen dreifach so schnell und russt dreifach so stark.
 *
 * Kennzahlen unveraendert: maxFuel 12800, processingSpeed 400, Brennwerte wie im Original.
 *
 * Unterschiede zum Original:
 * - Original hatte zwei Bloecke (an/aus). Hier ein Block mit BlockStateProperty LIT.
 * - Die Rezepte kommen aus dem im Port vorhandenen BlastFurnaceRecipes (GenericRecipes).
 *   Deren Zutatenmengen werden respektiert; das Original zog stumpf je 1 Stueck ab.
 *   Ein etwaiges zweites Ausgabeprodukt (Schlacke) entfaellt -- der DiFurnace hat wie
 *   im Original nur einen Ausgabeslot.
 * - Weggelassen: IConfigurableMachine und IInfoProviderEC (im Port nicht vorhanden).
 *   // todo config
 */
public class MachineDiFurnaceBlockEntity extends MachinePollutingBlockEntity {

    public static final int MAX_FUEL = 12800;
    public static final int PROCESSING_SPEED = 400;

    public static final int SLOT_INPUT_UPPER = 0;
    public static final int SLOT_INPUT_LOWER = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;

    private static final int[] ACCESSIBLE_SLOTS = new int[] { SLOT_INPUT_UPPER, SLOT_INPUT_LOWER, SLOT_FUEL, SLOT_OUTPUT };

    public int progress;
    public int fuel;

    /** Seite, von der der jeweilige Slot Gegenstaende annimmt. Index wie Direction.get3DDataValue(). */
    public byte sideFuel = 1;
    public byte sideUpper = 1;
    public byte sideLower = 1;

    public MachineDiFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DIFURNACE.get(), pos, state, 4, 50);
        // Die Rezeptliste teilt sich der Doppelofen mit dem Hochofen. Ohne diesen Aufruf
        // bliebe sie leer, wenn im Spiel nie ein Hochofen gesetzt wurde. registerDefaults()
        // bricht bei bereits gefuellter Liste sofort ab, ist also mehrfach aufrufbar.
        BlastFurnaceRecipes.INSTANCE.registerDefaults();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_difurnace");
    }

    /**
     * Brennwerte des Originals. Der Original-Eintrag fuer ModBlocks.block_coke entfaellt,
     * den Kohleblock gibt es im Port nicht. Briketts und Koks lagen im Original als je ein
     * Item vor, im Port sind sie nach Ausgangsstoff aufgeteilt -- alle Varianten erben den
     * Brennwert des Originalitems.
     */
    public static int getItemPower(ItemStack stack) {
        if(stack.isEmpty()) return 0;

        if(stack.is(Items.COAL)) return 200;
        if(stack.is(Blocks.COAL_BLOCK.asItem())) return 2000;
        if(stack.is(Items.LAVA_BUCKET)) return 12800;
        if(stack.is(Items.BLAZE_ROD)) return 1000;
        if(stack.is(Items.BLAZE_POWDER)) return 300;
        if(stack.is(NtmItems.LIGNITE.get())) return 150;
        if(stack.is(NtmItems.POWDER_LIGNITE.get())) return 150;
        if(stack.is(NtmItems.POWDER_COAL.get())) return 200;
        if(stack.is(NtmItems.BRIQUETTE_COAL.get())) return 200;
        if(stack.is(NtmItems.BRIQUETTE_LIGNITE.get())) return 200;
        if(stack.is(NtmItems.BRIQUETTE_WOOD.get())) return 200;
        if(stack.is(NtmItems.COKE_COAL.get())) return 400;
        if(stack.is(NtmItems.COKE_LIGNITE.get())) return 400;
        if(stack.is(NtmItems.COKE_PETROLEUM.get())) return 400;
        if(stack.is(NtmItems.SOLID_FUEL.get())) return 400;

        return 0;
    }

    public boolean hasItemPower(ItemStack stack) {
        return getItemPower(stack) > 0;
    }

    /** Der Aufsatz sitzt direkt ueber dem Kernblock. */
    public boolean hasExtension() {
        if(this.level == null) return false;
        return this.level.getBlockState(this.worldPosition.above()).getBlock() instanceof MachineDiFurnaceExtensionBlock;
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        boolean extension = this.hasExtension();

        this.sendSmoke(this.getConPos(extension));

        boolean markDirty = false;

        ItemStack fuelStack = this.slots.get(SLOT_FUEL);
        int itemPower = getItemPower(fuelStack);

        if(itemPower > 0 && this.fuel <= MAX_FUEL - itemPower) {
            this.fuel += itemPower;
            markDirty = true;

            ItemStack remainder = fuelStack.hasCraftingRemainingItem() ? fuelStack.getCraftingRemainingItem().copy() : ItemStack.EMPTY;
            fuelStack.shrink(1);
            if(fuelStack.isEmpty()) this.slots.set(SLOT_FUEL, remainder);
        }

        if(this.canProcess()) {

            // Kommentar des Originals: der Aufsatz kostet keinen zusaetzlichen Brennstoff.
            this.fuel -= 1;
            this.progress += extension ? 3 : 1;

            if(this.progress >= PROCESSING_SPEED) {
                this.progress -= PROCESSING_SPEED;
                this.processItem();
                markDirty = true;
            }

            if(this.fuel < 0) this.fuel = 0;

            if(this.level.getGameTime() % 20 == 0) {
                this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * (extension ? 3 : 1));
            }

        } else {
            this.progress = 0;
        }

        // Wie im Original: im Anlaufmoment (kann arbeiten, aber Fortschritt noch 0) bleibt
        // der Blockzustand unveraendert.
        boolean trigger = !(this.canProcess() && this.progress == 0);

        if(trigger) {
            markDirty = true;
            this.updateLitState(this.progress > 0);
        }

        this.networkPackNT(15);

        if(markDirty) this.setChanged();
    }

    private void updateLitState(boolean lit) {
        if(this.level == null) return;

        BlockState state = this.getBlockState();
        if(!(state.getBlock() instanceof MachineDiFurnaceBlock)) return;
        if(state.getValue(MachineDiFurnaceBlock.LIT) == lit) return;

        this.level.setBlock(this.worldPosition, state.setValue(MachineDiFurnaceBlock.LIT, lit), Block.UPDATE_ALL);
    }

    @Nullable
    public BlastFurnaceRecipe getRecipe() {
        return BlastFurnaceRecipes.INSTANCE.getRecipe(this.slots.get(SLOT_INPUT_UPPER), this.slots.get(SLOT_INPUT_LOWER));
    }

    public boolean canProcess() {
        if(this.slots.get(SLOT_INPUT_UPPER).isEmpty() || this.slots.get(SLOT_INPUT_LOWER).isEmpty()) return false;
        if(!this.hasPower()) return false;

        BlastFurnaceRecipe recipe = this.getRecipe();
        if(recipe == null) return false;
        if(!BlastFurnaceRecipes.matchesInputs(recipe, this.slots.get(SLOT_INPUT_UPPER), this.slots.get(SLOT_INPUT_LOWER), false)) return false;

        ItemStack output = getPrimaryOutput(recipe);
        if(output.isEmpty()) return false;

        ItemStack current = this.slots.get(SLOT_OUTPUT);
        if(current.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(current, output)) return false;

        return current.getCount() + output.getCount() <= current.getMaxStackSize();
    }

    private void processItem() {
        BlastFurnaceRecipe recipe = this.getRecipe();
        if(recipe == null) return;

        ItemStack output = getPrimaryOutput(recipe);
        if(output.isEmpty()) return;

        this.consumeInputs(recipe);

        ItemStack current = this.slots.get(SLOT_OUTPUT);
        if(current.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, output.copy());
        } else if(ItemStack.isSameItemSameComponents(current, output)) {
            current.grow(output.getCount());
        }
    }

    private void consumeInputs(BlastFurnaceRecipe recipe) {
        consumeInputs(recipe, this.slots, SLOT_INPUT_UPPER, SLOT_INPUT_LOWER);
    }

    /**
     * Zutatenabzug wie beim Hochofen des Ports: die Rezeptmengen gelten, die Reihenfolge ist frei.
     *
     * Statisch, weil der RTG-Doppelofen dieselbe Rechnung braucht, seine Faecher aber anders
     * liegen -- im Original steht der Abzug dort ein zweites Mal.
     */
    public static void consumeInputs(BlastFurnaceRecipe recipe, NonNullList<ItemStack> slots, int upper, int lower) {
        if(recipe.inputItem == null || recipe.inputItem.length == 0) return;

        if(recipe.inputItem.length == 1) {
            if(recipe.inputItem[0].matchesRecipe(slots.get(upper), false)) {
                slots.get(upper).shrink(recipe.inputItem[0].stacksize);
            } else {
                slots.get(lower).shrink(recipe.inputItem[0].stacksize);
            }
        } else if(recipe.inputItem[0].matchesRecipe(slots.get(upper), false)
                && recipe.inputItem[1].matchesRecipe(slots.get(lower), false)) {
            slots.get(upper).shrink(recipe.inputItem[0].stacksize);
            slots.get(lower).shrink(recipe.inputItem[1].stacksize);
        } else {
            slots.get(upper).shrink(recipe.inputItem[1].stacksize);
            slots.get(lower).shrink(recipe.inputItem[0].stacksize);
        }

        if(slots.get(upper).isEmpty()) slots.set(upper, ItemStack.EMPTY);
        if(slots.get(lower).isEmpty()) slots.set(lower, ItemStack.EMPTY);
    }

    /** Nur das erste Ausgabeprodukt; der DiFurnace hat wie im Original genau einen Ausgabeslot. */
    public static ItemStack getPrimaryOutput(BlastFurnaceRecipe recipe) {
        if(recipe.outputItem == null || recipe.outputItem.length == 0) return ItemStack.EMPTY;
        ItemStack stack = recipe.outputItem[0].collapse();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public boolean hasPower() {
        return this.fuel > 0;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    public int getDiFurnaceProgressScaled(int pixels) {
        return (this.progress * pixels) / PROCESSING_SPEED;
    }

    public int getPowerRemainingScaled(int pixels) {
        return (this.fuel * pixels) / MAX_FUEL;
    }

    /**
     * Rauchabgabe an alle sechs Nachbarn; mit Aufsatz zusaetzlich zwei Bloecke weiter oben,
     * also an den Block ueber dem Aufsatz. Genau wie im Original.
     */
    public DirPos[] getConPos(boolean extension) {
        BlockPos pos = this.getBlockPos();

        DirPos[] base = new DirPos[] {
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Direction.NORTH),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Direction.UP),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Direction.DOWN)
        };

        if(!extension) return base;

        DirPos[] all = new DirPos[base.length + 1];
        System.arraycopy(base, 0, all, 0, base.length);
        all[base.length] = new DirPos(pos.getX(), pos.getY() + 2, pos.getZ(), Direction.UP);
        return all;
    }

    /** Der Ofen selbst haelt keine abzapfbaren Tanks; der Rauch laeuft ueber getSendingTanks. */
    @Override
    public FluidTank[] getAllTanks() {
        return FluidTank.EMPTY_ARRAY;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != SLOT_OUTPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if(direction == null) return this.canPlaceItem(index, stack);

        int side = direction.get3DDataValue();

        if(index == SLOT_INPUT_UPPER && this.sideUpper != side) return false;
        if(index == SLOT_INPUT_LOWER && this.sideLower != side) return false;
        if(index == SLOT_FUEL && this.sideFuel != side) return false;
        if(index == SLOT_OUTPUT) return false;

        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.fuel = tag.getInt("powerTime");
        this.progress = tag.getShort("cookTime");

        byte[] modes = tag.getByteArray("modes");
        if(modes.length >= 3) {
            this.sideFuel = modes[0];
            this.sideUpper = modes[1];
            this.sideLower = modes[2];
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("powerTime", this.fuel);
        tag.putShort("cookTime", (short) this.progress);
        tag.putByteArray("modes", new byte[] { this.sideFuel, this.sideUpper, this.sideLower });
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.progress);
        buf.writeInt(this.fuel);
        buf.writeByte(this.sideFuel);
        buf.writeByte(this.sideUpper);
        buf.writeByte(this.sideLower);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readInt();
        this.fuel = buf.readInt();
        this.sideFuel = buf.readByte();
        this.sideUpper = buf.readByte();
        this.sideLower = buf.readByte();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineDiFurnaceMenu(id, inventory, this);
    }
}
