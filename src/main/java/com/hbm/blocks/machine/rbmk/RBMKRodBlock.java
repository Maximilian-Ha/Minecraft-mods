package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKRod.
 *
 * Der Brennkanal. Die moderierte Bauform hat Graphit um den Kanal und bremst damit die
 * Neutronen, die durch sie hindurchlaufen.
 */
public class RBMKRodBlock extends RBMKBaseBlock {

    public static final MapCodec<RBMKRodBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("moderated").forGetter(block -> block.moderated),
            propertiesCodec()
    ).apply(instance, RBMKRodBlock::new));

    public final boolean moderated;

    public RBMKRodBlock(boolean moderated, Properties properties) {
        super(properties);
        this.moderated = moderated;
    }

    @Override
    protected MapCodec<? extends RBMKRodBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKRodBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
