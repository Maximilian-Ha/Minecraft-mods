package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityDeuteriumTower.
 *
 * Derselbe Vorgang wie im Extraktor, nur im Grossen: fuenfzig Eimer Wasser fassen die Tanks,
 * fuenf Eimer schweres Wasser, und der Stromspeicher ist zehnmal so gross. Dafuer ist der Turm
 * ein Mehrblockbau, und seine acht Anschlussstellen sitzen rings um seinen Fuss.
 *
 * ABWEICHUNG: das Original rechnet die Drehung mit getRotation(ForgeDirection.DOWN). Gemessen
 * ueber alle portierten Mehrblockbauten bildet der Port getRotation(UP) einundvierzigmal auf
 * getClockWise ab -- die Gegenrichtung ist also getCounterClockWise.
 */
public class DeuteriumTowerBlockEntity extends DeuteriumExtractorBlockEntity {

    public DeuteriumTowerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DEUTERIUM_TOWER.get(), pos, state, 50_000, 5_000);
    }

    @Override
    protected DirPos[] getConPos() {

        BlockState state = this.getBlockState();
        if(!state.hasProperty(DummyableBlock.FACING)) return new DirPos[0];

        Direction dir = state.getValue(DummyableBlock.FACING);
        Direction rot = dir.getCounterClockWise();
        BlockPos pos = this.worldPosition;

        return new DirPos[] {
                new DirPos(pos.offset(-dir.getStepX() * 2, 0, -dir.getStepZ() * 2), dir.getOpposite()),
                new DirPos(pos.offset(-dir.getStepX() * 2 + rot.getStepX(), 0, -dir.getStepZ() * 2 + rot.getStepZ()), dir.getOpposite()),

                new DirPos(pos.offset(dir.getStepX(), 0, dir.getStepZ()), dir),
                new DirPos(pos.offset(dir.getStepX() + rot.getStepX(), 0, dir.getStepZ() + rot.getStepZ()), dir),

                new DirPos(pos.offset(-rot.getStepX(), 0, -rot.getStepZ()), rot.getOpposite()),
                new DirPos(pos.offset(-dir.getStepX() - rot.getStepX(), 0, -dir.getStepZ() - rot.getStepZ()), rot.getOpposite()),

                new DirPos(pos.offset(rot.getStepX() * 2, 0, rot.getStepZ() * 2), rot),
                new DirPos(pos.offset(-dir.getStepX() + rot.getStepX() * 2, 0, -dir.getStepZ() + rot.getStepZ() * 2), rot),
        };
    }

    @Override public long getMaxPower() { return 100_000; }

    private AABB renderBox;

    /**
     * Der Turm ist zehn Bloecke hoch und steht auf vier Grundflaechen -- ohne eigenen Sichtkasten
     * verschwaende er, sobald der Kernblock unten aus dem Bild faellt. Masse aus dem Original.
     */
    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 10, z + 2);
        }

        return this.renderBox;
    }
}
