package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.MachineMicrowaveMenu;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMicrowave.
 *
 * Eine Mikrowelle fuer Nahrung: sie nimmt die normalen Ofenrezepte, aber nur, wenn entweder
 * das Eingelegte oder das Ergebnis essbar ist. Der Regler geht von null bis fuenf und
 * bestimmt, wie schnell die Zeit laeuft -- auf Stufe fuenf fliegt sie in die Luft.
 *
 * ABWEICHUNG: das Original setzt der Explosion zusaetzlich einen PlayerProcessorStandard
 * vor, der Spielern eigene Schadens- und Rueckstossregeln gibt. Den gibt es im Port nicht;
 * ohne ihn behandelt die Explosion den Spieler wie jedes andere Wesen -- so wie an den
 * anderen Stellen des Ports auch.
 */
public class MachineMicrowaveBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IControlReceiver {

    public static final long MAX_POWER = 50_000L;
    public static final int CONSUMPTION = 50;
    public static final int MAX_TIME = 300;
    public static final int MAX_SPEED = 5;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BATTERY = 2;

    public long power;
    public int time;
    public int speed;

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> rezeptsuche =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public MachineMicrowaveBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_MICROWAVE.get(), pos, state, 3);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.microwave");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        for(DirPos dirPos : this.getConPos()) this.trySubscribe(this.level, dirPos);

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);

        if(this.kannArbeiten()) {

            if(this.speed >= MAX_SPEED) {
                this.level.destroyBlock(this.worldPosition, false);
                new ExplosionVNT(this.level,
                        this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 5)
                        .setEntityProcessor(new EntityProcessorCrossSmooth(1, 50))
                        .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                        .explode();
                return;
            }

            if(this.time >= MAX_TIME) {
                this.verarbeite();
                this.time = 0;
            }

            // Zweite Abfrage wie im Original: das Verarbeiten kann die Lage geaendert haben.
            if(this.kannArbeiten()) {
                this.power -= CONSUMPTION;
                this.time += this.speed * 2;
            }
        }

        this.networkPackNT(50);
    }

    /** Das Schmelzergebnis nach den normalen Ofenrezepten, oder ein leerer Stapel. */
    private ItemStack ergebnis(ItemStack eingabe) {
        if(this.level == null || eingabe.isEmpty()) return ItemStack.EMPTY;
        return this.rezeptsuche.getRecipeFor(new SingleRecipeInput(eingabe.copy()), this.level)
                .map(holder -> holder.value().getResultItem(this.level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
    }

    private static boolean essbar(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    private boolean kannArbeiten() {

        if(this.speed == 0) return false;
        if(this.power < CONSUMPTION) return false;

        ItemStack eingabe = this.slots.get(SLOT_INPUT);
        ItemStack ergebnis = this.ergebnis(eingabe);
        if(ergebnis.isEmpty()) return false;

        // Nur Nahrung: entweder das Eingelegte oder das Ergebnis muss essbar sein.
        if(!essbar(eingabe) && !essbar(ergebnis)) return false;

        ItemStack ausgabe = this.slots.get(SLOT_OUTPUT);
        if(ausgabe.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(ausgabe, ergebnis)) return false;

        return ergebnis.getCount() + ausgabe.getCount() <= ergebnis.getMaxStackSize();
    }

    private void verarbeite() {

        ItemStack ergebnis = this.ergebnis(this.slots.get(SLOT_INPUT));
        if(ergebnis.isEmpty()) return;

        ItemStack ausgabe = this.slots.get(SLOT_OUTPUT);
        if(ausgabe.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, ergebnis.copy());
        } else {
            ausgabe.grow(ergebnis.getCount());
        }

        ItemStack eingabe = this.slots.get(SLOT_INPUT);
        eingabe.shrink(1);
        if(eingabe.isEmpty()) this.slots.set(SLOT_INPUT, ItemStack.EMPTY);

        this.setChanged();
    }

    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.relative(Direction.EAST), Direction.EAST),
                new DirPos(pos.relative(Direction.WEST), Direction.WEST),
                new DirPos(pos.relative(Direction.SOUTH), Direction.SOUTH),
                new DirPos(pos.relative(Direction.NORTH), Direction.NORTH),
                new DirPos(pos.relative(Direction.UP), Direction.UP),
                new DirPos(pos.relative(Direction.DOWN), Direction.DOWN)
        };
    }

    /** Die beiden Knoepfe der Oberflaeche: null hoeher, eins niedriger. */
    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.getBoolean("schneller")) this.speed++;
        if(tag.getBoolean("langsamer")) this.speed--;
        this.speed = Math.max(0, Math.min(MAX_SPEED, this.speed));
        this.setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    public long getPowerScaled(int pixel) { return this.power * pixel / MAX_POWER; }
    public int getProgressScaled(int pixel) { return this.time * pixel / MAX_TIME; }
    public int getSpeedScaled(int pixel) { return this.speed * pixel / MAX_SPEED; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_INPUT) return !this.ergebnis(stack).isEmpty();
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? new int[] { SLOT_OUTPUT } : new int[] { SLOT_INPUT };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.time = tag.getInt("time");
        this.speed = tag.getInt("speed");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("time", this.time);
        tag.putInt("speed", this.speed);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.time);
        buf.writeInt(this.speed);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.time = buf.readInt();
        this.speed = buf.readInt();
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMicrowaveMenu(id, inventory, this);
    }
}
