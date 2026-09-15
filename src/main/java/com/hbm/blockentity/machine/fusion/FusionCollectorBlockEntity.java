package com.hbm.blockentity.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionCollector.
 *
 * Der Kollektor nimmt nichts ab und gibt nichts her -- er haengt nur im Plasmanetz herum. Sein
 * Nutzen liegt allein darin, dass der Torus ihn zaehlt: jeder Kollektor beschleunigt dessen
 * Ausbeute um die Haelfte. Vier davon, und die Anlage wirft das Dreifache ab.
 */
public class FusionCollectorBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    protected GenNode<?> plasmaNode;
    private AABB renderBox;

    public FusionCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FUSION_COLLECTOR.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getOpposite();
        BlockPos nodePos = this.worldPosition.offset(dir.getStepX() * 2, 2, dir.getStepZ() * 2);

        this.plasmaNode = FusionNodes.ensure(this.plasmaNode, this.level, nodePos,
                new DirPos(this.worldPosition.offset(dir.getStepX() * 3, 2, dir.getStepZ() * 3), dir),
                PlasmaNetworkProvider.THE_PROVIDER);

        FusionNodes.subscribe(this.plasmaNode, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        FusionNodes.destroy(this.level, this.plasmaNode);
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 4, z + 3);
        }
        return this.renderBox;
    }
}
