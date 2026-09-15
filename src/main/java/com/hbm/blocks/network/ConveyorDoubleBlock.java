package com.hbm.blocks.network;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorDouble.
 *
 * Das Doppelband: zwei Spuren nebeneinander, je einen Viertelblock neben der Mitte. Welche der
 * beiden ein Gegenstand bekommt, entscheidet sich danach, auf welcher Seite der Mitte er liegt.
 */
public class ConveyorDoubleBlock extends ConveyorBendableBlock {

    public static final MapCodec<ConveyorDoubleBlock> CODEC = simpleCodec(ConveyorDoubleBlock::new);

    public ConveyorDoubleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {

        Direction dir = this.getTravelDirection(level, pos, itemPos);

        double clampedX = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
        double clampedZ = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;

        if(dir.getStepX() != 0) {
            x = clampedX;
            z += clampedZ > z ? 0.25 : -0.25;
        }
        if(dir.getStepZ() != 0) {
            z = clampedZ;
            x += clampedX > x ? 0.25 : -0.25;
        }

        return new Vec3(x, pos.getY() + 0.25, z);
    }

    @Override protected MapCodec<? extends ConveyorBendableBlock> codec() { return CODEC; }
}
