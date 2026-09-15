package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKAutoloaderBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKAutoloader.
 *
 * Ein neun Bloecke hoher Turm auf einem Brennkanal. Unten der duenne Stempelschacht, oben das
 * Gehaeuse mit den Faechern -- die beiden Trefferkoerper stehen wortgetreu wie im Original.
 */
public class RBMKAutoloaderBlock extends DummyableBlock {

    public RBMKAutoloaderBlock(Properties properties) {
        super(properties);

        this.bounding.add(new AABB(-0.125, 0, -0.125, 0.125, 4, 0.125));
        this.bounding.add(new AABB(-0.5, 4, -0.5, 0.5, 9, 0.5));
    }

    public static final MapCodec<RBMKAutoloaderBlock> CODEC = simpleCodec(RBMKAutoloaderBlock::new);

    @Override
    protected MapCodec<? extends RBMKAutoloaderBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) == DummyBlockType.CORE) return new RBMKAutoloaderBlockEntity(pos, state);
        return new ProxyComboBlockEntity(pos, state).inventory();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    /** Acht Lagen ueber dem Kernblock, sonst keine Ausdehnung. */
    @Override public int[] getDimensions() { return new int[] {8, 0, 0, 0, 0, 0}; }
    @Override public int getOffset() { return 0; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
