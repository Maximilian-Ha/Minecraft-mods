package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.rbmk.RBMKInletBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKOutletBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKInlet und RBMKOutlet.
 *
 * Ein- und Auslass stehen neben dem Reaktor, nicht in ihm. Beide sind gewoehnliche Bloecke mit
 * Block-Entitaet und unterscheiden sich nur in der Richtung, in die das Wasser laeuft -- daher
 * eine Klasse mit einem Schalter.
 */
public class RBMKPortBlock extends BaseEntityBlock {

    public static final MapCodec<RBMKPortBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("inlet").forGetter(block -> block.inlet),
            propertiesCodec()
    ).apply(instance, RBMKPortBlock::new));

    /** true ist der Wassereinlass, false der Dampfauslass. */
    public final boolean inlet;

    public RBMKPortBlock(boolean inlet, Properties properties) {
        super(properties);
        this.inlet = inlet;
    }

    @Override
    protected MapCodec<RBMKPortBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.inlet ? new RBMKInletBlockEntity(pos, state) : new RBMKOutletBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
