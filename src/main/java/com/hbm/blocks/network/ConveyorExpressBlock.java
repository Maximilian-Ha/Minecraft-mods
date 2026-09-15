package com.hbm.blocks.network;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorExpress.
 *
 * Das Schnellband. Eine Spur wie das einfache Band, aber die dreifache Schrittweite.
 */
public class ConveyorExpressBlock extends ConveyorBendableBlock {

    public static final MapCodec<ConveyorExpressBlock> CODEC = simpleCodec(ConveyorExpressBlock::new);

    public ConveyorExpressBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return super.getTravelLocation(level, pos, itemPos, speed * 3D);
    }

    @Override protected MapCodec<? extends ConveyorBendableBlock> codec() { return CODEC; }
}
