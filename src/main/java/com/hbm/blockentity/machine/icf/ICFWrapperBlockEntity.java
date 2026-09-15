package com.hbm.blockentity.machine.icf;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.icf.ICFLaserComponentBlock;
import com.hbm.util.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockICF.TileEntityBlockICF.
 *
 * Der Stellvertreter fuer ein eingelesenes Laserbauteil. Er merkt sich, welcher Untertyp hier
 * stand und wo die Steuerung sitzt; beim Abbauen gibt er das Bauteil zurueck und meldet die
 * Anlage ab.
 *
 * Strom nimmt er stellvertretend fuer die Steuerung an -- deshalb ist er selbst ein
 * Stromempfaenger und reicht alles weiter.
 */
public class ICFWrapperBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2 {

    /** Nur alle zwanzig Ticks nachsehen, ob die Steuerung noch da ist. */
    private static final int CHECK_INTERVAL = 20;

    /** Welcher Untertyp des Laserbauteils hier stand. Minus eins heisst: keiner. */
    public int part = -1;
    public BlockPos core = BlockPos.ZERO;

    @Nullable private ICFControllerBlockEntity cachedCore;

    public ICFWrapperBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.ICF_BLOCK.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(this.part < 0) return;
        if(this.level.getGameTime() % CHECK_INTERVAL != 0) return;

        ICFControllerBlockEntity controller = this.getCore();

        if(controller == null) {
            if(this.level.isLoaded(this.core)) this.restore();
        } else if(!controller.assembled) {
            this.restore();
        }
    }

    /** Macht aus dem Stellvertreter wieder das Laserbauteil, das er war. */
    public void restore() {

        if(this.level == null || this.part < 0) return;

        int subtype = this.part;
        this.part = -1;

        ICFControllerBlockEntity controller = this.getCore();
        if(controller != null) controller.assembled = false;

        this.level.setBlock(this.worldPosition,
                NtmBlocks.ICF_LASER_COMPONENT.get().defaultBlockState().setValue(ICFLaserComponentBlock.SUBTYPE, subtype), 3);
    }

    @Nullable
    public ICFControllerBlockEntity getCore() {

        if(this.cachedCore != null && !this.cachedCore.isRemoved()) return this.cachedCore;
        if(this.level == null || !this.level.isLoaded(this.core)) return null;

        if(Compat.getBlockEntityStandard(this.level, this.core) instanceof ICFControllerBlockEntity controller) {
            this.cachedCore = controller;
            return controller;
        }

        return null;
    }

    /* Der Strom geht an die Steuerung; der Stellvertreter ist nur der Anschluss. */

    @Override
    public long getPower() {
        ICFControllerBlockEntity controller = this.getCore();
        return controller == null ? 0 : controller.getPower();
    }

    @Override
    public void setPower(long power) {
        ICFControllerBlockEntity controller = this.getCore();
        if(controller != null) controller.setPower(power);
    }

    @Override
    public long getMaxPower() {
        ICFControllerBlockEntity controller = this.getCore();
        return controller == null ? 0 : controller.getMaxPower();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.part = tag.contains("part") ? tag.getInt("part") : -1;
        this.core = BlockPos.of(tag.getLong("core"));
        this.cachedCore = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        if(this.part >= 0) {
            tag.putInt("part", this.part);
            tag.putLong("core", this.core.asLong());
        }
    }
}
