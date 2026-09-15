package com.hbm.blocks.network;

import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyor.
 *
 * Das einfache Band: eine Spur in der Mitte, Schrittweite ein Sechzehntel Block je Takt.
 */
public class ConveyorBlock extends ConveyorBendableBlock {

    public static final MapCodec<ConveyorBlock> CODEC = simpleCodec(ConveyorBlock::new);

    public ConveyorBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends ConveyorBendableBlock> codec() { return CODEC; }
}
