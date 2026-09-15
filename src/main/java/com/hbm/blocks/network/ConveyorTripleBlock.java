package com.hbm.blocks.network;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorTriple.
 *
 * Das Dreifachband: drei Spuren, die mittlere und je eine fuenf Pixel daneben. Wer nahe genug
 * an der Mitte liegt, bleibt in der Mitte -- der Totbereich von anderthalb Pixeln verhindert,
 * dass ein Gegenstand zwischen zwei Spuren hin und her springt.
 */
public class ConveyorTripleBlock extends ConveyorBendableBlock {

    public static final MapCodec<ConveyorTripleBlock> CODEC = simpleCodec(ConveyorTripleBlock::new);

    /** Abstand der aeusseren Spuren von der Mitte. */
    private static final double LANE = 0.3125D;
    /** Halber Totbereich um die Mittelspur. */
    private static final double DEAD_ZONE = 0.15D;

    public ConveyorTripleBlock(Properties properties) {
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
            z += lane(clampedZ, z);
        }
        if(dir.getStepZ() != 0) {
            z = clampedZ;
            x += lane(clampedX, x);
        }

        return new Vec3(x, pos.getY() + 0.25, z);
    }

    private static double lane(double itemCoord, double centre) {
        if(itemCoord > centre + DEAD_ZONE) return LANE;
        if(itemCoord < centre - DEAD_ZONE) return -LANE;
        return 0D;
    }

    @Override protected MapCodec<? extends ConveyorBendableBlock> codec() { return CODEC; }
}
