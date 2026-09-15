package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.PWRBlock;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockPWR.TileEntityBlockPWR.
 *
 * Was der eingelesene Reaktor aus seinen Bauteilen macht. Jeder Block der Anlage wird beim
 * Einlesen durch diesen hier ersetzt; er merkt sich, was er vorher war und wo die Steuerung
 * steht.
 *
 * Der Sinn: die Anlage laeuft als ein Stueck. Ein Stellvertreter ist zaeher als das Original,
 * gibt beim Abbauen das urspruengliche Bauteil zurueck und reisst die ganze Anlage aus dem
 * Betrieb -- ein halb abgerissener Reaktor darf nicht weiterlaufen.
 *
 * Nur die Anschlussstellen reichen Fluid durch; der Rest der Huelle ist dicht.
 */
public class PWRBlockEntity extends BlockEntity implements ITickable, IFluidStandardTransceiverMK2 {

    /** Nur alle zwanzig Ticks nachsehen, ob die Steuerung noch da ist. */
    private static final int CHECK_INTERVAL = 20;

    /** Was hier vor dem Einlesen stand. */
    @Nullable public Block block;
    public BlockPos core = BlockPos.ZERO;

    @Nullable private MachinePWRControllerBlockEntity cachedCore;

    /**
     * Ob dieser Stellvertreter noch in einem geladenen Stueck Welt sitzt. Das Fluidnetz haelt
     * Verweise auf seine Anschlussstellen; ohne diese Abmeldung bliebe ein entladener Reaktor
     * als Leiche im Netz haengen.
     */
    private boolean isLoaded = true;

    public PWRBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PWR_BLOCK.get(), pos, state);
    }

    @Override
    public boolean isLoaded() {
        return this.isLoaded;
    }

    @Override
    public void onChunkUnloaded() {
        this.isLoaded = false;
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(this.block == null) return;
        if(this.level.getGameTime() % CHECK_INTERVAL != 0) return;

        MachinePWRControllerBlockEntity controller = this.getCore();

        if(controller == null) {
            /* Die Steuerung ist weg -- nur zerfallen, wenn ihr Stueck Welt auch wirklich da ist. */
            if(this.level.isLoaded(this.core)) this.restore();

        } else if(!controller.assembled) {
            this.restore();
        }
    }

    /** Macht aus dem Stellvertreter wieder das Bauteil, das er einmal war. */
    public void restore() {

        if(this.level == null || this.block == null) return;

        Block old = this.block;
        this.block = null;

        MachinePWRControllerBlockEntity controller = this.getCore();
        if(controller != null) controller.assembled = false;

        this.level.setBlock(this.getBlockPos(), old.defaultBlockState(), 3);
    }

    @Nullable
    public MachinePWRControllerBlockEntity getCore() {

        if(this.cachedCore != null && !this.cachedCore.isRemoved()) return this.cachedCore;
        if(this.level == null || !this.level.isLoaded(this.core)) return null;

        if(this.level.getBlockEntity(this.core) instanceof MachinePWRControllerBlockEntity controller) {
            this.cachedCore = controller;
            return controller;
        }

        return null;
    }

    /** Ob dieser Stellvertreter eine Anschlussstelle war. */
    private boolean isPort() {
        return this.getBlockState().getValue(PWRBlock.PORT);
    }

    @Override
    public FluidTank[] getReceivingTanks() {
        MachinePWRControllerBlockEntity controller = this.isPort() ? this.getCore() : null;
        return controller == null ? new FluidTank[0] : controller.getReceivingTanks();
    }

    @Override
    public FluidTank[] getSendingTanks() {
        MachinePWRControllerBlockEntity controller = this.isPort() ? this.getCore() : null;
        return controller == null ? new FluidTank[0] : controller.getSendingTanks();
    }

    @Override
    public FluidTank[] getAllTanks() {
        MachinePWRControllerBlockEntity controller = this.isPort() ? this.getCore() : null;
        return controller == null ? new FluidTank[0] : controller.getAllTanks();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        String id = tag.getString("block");
        Block stored = id.isEmpty() ? null : BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        this.block = (stored == null || stored == Blocks.AIR) ? null : stored;

        this.core = BlockPos.of(tag.getLong("core"));
        this.cachedCore = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        /*
         * Als Kennung statt als Zahl: numerische Block-IDs sind seit 1.13 nicht mehr stabil, eine
         * gespeicherte Zahl waere nach dem naechsten Modwechsel ein anderer Block.
         */
        if(this.block != null) {
            tag.putString("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            tag.putLong("core", this.core.asLong());
        }
    }
}
