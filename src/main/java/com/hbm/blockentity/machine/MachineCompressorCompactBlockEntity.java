package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: TileEntityMachineCompressorCompact.
 * Die gesamte Logik steckt in der Basis MachineCompressorBlockEntity; hier stehen nur
 * die Anschlusspunkte der kompakten Bauform, ihre Luefteranimation und der Renderkasten.
 */
public class MachineCompressorCompactBlockEntity extends MachineCompressorBlockEntity {

    public MachineCompressorCompactBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_COMPRESSOR_COMPACT.get(), pos, state);
    }

    /** Die kompakte Bauform hat nur einen Luefter, keinen Pumpenhub */
    @Override
    protected void updateAnimation() {

        this.prevFanSpin = this.fanSpin;

        if(this.isOn) {
            this.fanSpin += 45;

            if(this.fanSpin >= 360) {
                this.prevFanSpin -= 360;
                this.fanSpin -= 360;
            }
        }
    }

    @Override
    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getCounterClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + rot.getStepX() * 4, y + 1, z + rot.getStepZ() * 4, rot),
                new DirPos(x - rot.getStepX() * 4, y + 1, z - rot.getStepZ() * 4, rot.getOpposite()),
                new DirPos(x + dir.getStepX() * 2 - rot.getStepX(), y + 1, z + dir.getStepZ() * 2 - rot.getStepZ(), dir),
                new DirPos(x + dir.getStepX() * 2 + rot.getStepX(), y + 1, z + dir.getStepZ() * 2 + rot.getStepZ(), dir),
                new DirPos(x - dir.getStepX() * 2 - rot.getStepX(), y + 1, z - dir.getStepZ() * 2 - rot.getStepZ(), dir.getOpposite()),
                new DirPos(x - dir.getStepX() * 2 + rot.getStepX(), y + 1, z - dir.getStepZ() * 2 + rot.getStepZ(), dir.getOpposite())
        };
    }

    @Override
    protected Block getInfoBlock() {
        return NtmBlocks.MACHINE_COMPRESSOR_COMPACT.get();
    }

    @Override
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 3, z + 4);
        }
        return this.renderBox;
    }
}
