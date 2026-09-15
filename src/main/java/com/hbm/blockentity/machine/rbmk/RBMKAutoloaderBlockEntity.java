package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.RBMKAutoloaderMenu;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKAutoloader.
 *
 * Der selbsttaetige Lader steht auf einem Brennkanal und tauscht dessen Stab aus, sobald dieser
 * unter die eingestellte Restanreicherung faellt. Neun Faecher oben nehmen frische Staebe auf,
 * neun unten die abgebrannten; der Stempel faehrt langsam herunter und wieder hoch.
 *
 * ABWEICHUNGEN: das Original spielt beim Fahren eine Schleife aus der Tuersammlung und stoesst
 * am unteren Umkehrpunkt eine Dampfsaeule aus. Beides -- Schleifengeraeusche und die
 * Partikelsorte "tower" -- ist noch nicht portiert und bleibt darum weg.
 */
public class RBMKAutoloaderBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    /** Wie weit der Stempel je Tick faehrt. */
    public static final double SPEED = 0.005D;
    /** Pause am oberen und unteren Umkehrpunkt. */
    private static final int DELAY = 40;

    public double piston;
    public double renderPiston;
    public double lastPiston;
    private double syncPiston;

    private boolean isRetracting = true;
    private int delay = 0;

    /** Ab welcher Restanreicherung in Prozent ein Stab getauscht wird. */
    public int cycle = 50;

    public RBMKAutoloaderBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_AUTOLOADER.get(), pos, state, 18);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkAutoloader");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.lastPiston = this.renderPiston;
            this.renderPiston = this.syncPiston;
            return;
        }

        if(this.delay > 0) this.delay--;

        if(this.delay <= 0 && this.isRetracting && this.piston > 0D) {
            this.piston -= SPEED;
            if(this.piston <= 0) {
                this.piston = 0;
                this.delay = DELAY;
            }
        }

        // Einmal je Sekunde nachsehen, ob der Kanal darunter einen Tausch braucht.
        if(this.isRetracting && this.level.getGameTime() % 20 == 0 && this.hasFuel() && this.hasSpace()) {
            RBMKRodBlockEntity rod = this.getRodBelow();
            if(rod != null && rod.coldEnoughForAutoloader()) {
                ItemStack inChannel = rod.getItem(0);
                if(inChannel.isEmpty() || (inChannel.getItem() instanceof RBMKRodItem && RBMKRodItem.getEnrichment(inChannel) * 100 < this.cycle)) {
                    this.isRetracting = false;
                }
            }
        }

        if(this.delay <= 0 && !this.isRetracting && this.piston < 1D) {
            this.piston += SPEED;
            if(this.piston >= 1) {
                this.piston = 1;
                this.delay = DELAY;
            }
        }

        if(!this.isRetracting && this.piston >= 1D) {
            this.piston = 1D;
            this.swapRod();
        }

        this.networkPackNT(100);
    }

    /** Der Stempel steht unten: alten Stab herausnehmen, frischen einsetzen. */
    private void swapRod() {

        RBMKRodBlockEntity rod = this.getRodBelow();
        if(rod == null) return;

        if(!rod.getItem(0).isEmpty() && this.hasSpace()) {
            for(int i = 9; i < 18; i++) {
                if(this.slots.get(i).isEmpty()) {
                    this.slots.set(i, rod.getItem(0).copy());
                    rod.setItem(0, ItemStack.EMPTY);
                    break;
                }
            }
        }

        if(rod.getItem(0).isEmpty()) {
            for(int i = 0; i < 9; i++) {
                ItemStack stack = this.slots.get(i);
                if(stack.getItem() instanceof RBMKRodItem && RBMKRodItem.getEnrichment(stack) * 100 >= this.cycle) {
                    rod.setItem(0, stack.copy());
                    this.slots.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }

        this.isRetracting = true;
        this.delay = DELAY;
        this.setChanged();
    }

    /** Der Brennkanal genau unter dem Lader, oder null. */
    private @Nullable RBMKRodBlockEntity getRodBelow() {

        BlockPos below = this.worldPosition.below();
        if(!(this.level.getBlockState(below).getBlock() instanceof RBMKBaseBlock column)) return null;

        BlockPos corePos = column.findCore(this.level, below);
        if(corePos == null) return null;

        return this.level.getBlockEntity(corePos) instanceof RBMKRodBlockEntity rod ? rod : null;
    }

    /** Ob oben ein Stab liegt, der frisch genug zum Einsetzen ist. */
    public boolean hasFuel() {
        for(int i = 0; i < 9; i++) {
            ItemStack stack = this.slots.get(i);
            if(stack.getItem() instanceof RBMKRodItem && RBMKRodItem.getEnrichment(stack) * 100 >= this.cycle) return true;
        }
        return false;
    }

    /** Ob unten noch ein Fach fuer einen abgebrannten Stab frei ist. */
    public boolean hasSpace() {
        for(int i = 9; i < 18; i++) if(this.slots.get(i).isEmpty()) return true;
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < 9 && stack.getItem() instanceof RBMKRodItem && RBMKRodItem.getEnrichment(stack) * 100 >= this.cycle;
    }

    /** Waehrend der Stempel unten ist, kommt keine Rohrleitung an die Faecher. */
    @Override
    public int[] getSlotsForFace(Direction direction) {
        if(this.piston > 0) return new int[0];
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.piston <= 0 && this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return this.piston <= 0 && index >= 9;
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("minus")) this.cycle -= 5;
        if(data.contains("plus")) this.cycle += 5;
        this.cycle = Mth.clamp(this.cycle, 5, 95);
        this.setChanged();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKAutoloaderMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.piston);
        buf.writeInt(this.cycle);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.syncPiston = buf.readDouble();
        this.cycle = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.piston = tag.getDouble("piston");
        this.isRetracting = tag.getBoolean("ret");
        this.delay = tag.getInt("delay");
        this.cycle = tag.getInt("cycle");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("piston", this.piston);
        tag.putBoolean("ret", this.isRetracting);
        tag.putInt("delay", this.delay);
        tag.putInt("cycle", this.cycle);
    }

    /** Der Turm ragt neun Bloecke hoch, das Zeichenfenster muss ihn ganz umfassen. */
    public AABB getRenderBoundingBox() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new AABB(x, y, z, x + 1, y + 9, z + 1);
    }
}
