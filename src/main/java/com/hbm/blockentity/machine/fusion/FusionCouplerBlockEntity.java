package com.hbm.blockentity.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.networkproviders.KlystronNetworkProvider;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionCoupler.
 *
 * Der Koppler haengt mit der einen Seite im Plasmanetz und mit der anderen im Klystronnetz: was
 * ein Torus an Waerme abgibt, geht als Zuendenergie in den naechsten. So laesst sich eine Anlage
 * hintereinanderschalten, die allein nie genug Zuendleistung haette -- eine kleine Stufe zuendet
 * die grosse.
 */
public class FusionCouplerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFusionPowerReceiver {

    protected GenNode<?> klystronNode;
    protected GenNode<?> plasmaNode;

    private AABB renderBox;

    public FusionCouplerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FUSION_COUPLER.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getOpposite();
        Direction rot = dir.getClockWise();

        this.klystronNode = FusionNodes.ensure(this.klystronNode, this.level,
                this.worldPosition.offset(rot.getStepX(), 2, rot.getStepZ()),
                new DirPos(this.worldPosition.offset(rot.getStepX() * 2, 2, rot.getStepZ() * 2), rot),
                KlystronNetworkProvider.THE_PROVIDER);

        this.plasmaNode = FusionNodes.ensure(this.plasmaNode, this.level,
                this.worldPosition.offset(-rot.getStepX(), 2, -rot.getStepZ()),
                new DirPos(this.worldPosition.offset(-rot.getStepX() * 2, 2, -rot.getStepZ() * 2), rot.getOpposite()),
                PlasmaNetworkProvider.THE_PROVIDER);

        FusionNodes.provide(this.klystronNode, this);
        FusionNodes.subscribe(this.plasmaNode, this);
    }

    @Override public boolean receivesFusionPower() { return true; }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        FusionKlystronBlockEntity.provideKyU(this.klystronNode, fusionPower);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        FusionNodes.destroy(this.level, this.klystronNode);
        FusionNodes.destroy(this.level, this.plasmaNode);
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 4, z + 2);
        }
        return this.renderBox;
    }
}
