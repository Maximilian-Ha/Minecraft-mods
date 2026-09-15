package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.CableSwitchBlockEntity;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CableDetector.
 *
 * Kein Multiblock. Der Detektor ist ein Kabelschalter, den nicht der Spieler, sondern
 * ein Redstonesignal umlegt: liegt Strom an, entsteht ein Netzknoten, sonst nicht.
 * Das Metadatenpaar 1/0 des Originals ist hier LIT; beide Zustaende haben wie im
 * Original eine eigene Textur. Das BlockEntity ist dasselbe wie beim Handschalter,
 * genau wie im Original (TileEntityCableSwitch).
 */
public class CableDetectorBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<CableDetectorBlock> CODEC = simpleCodec(CableDetectorBlock::new);

    public CableDetectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.FALSE));
    }

    @Override
    public MapCodec<CableDetectorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableSwitchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if(level.isClientSide) return;

        boolean on = level.hasNeighborSignal(pos);
        boolean lit = state.getValue(LIT);

        if(on == lit) return;

        level.setBlock(pos, state.setValue(LIT, on), Block.UPDATE_CLIENTS);
        // Original: "hbm:block.reactorStart" mit Tonhoehe 1.0 beim Einschalten, 0.85 beim Ausschalten.
        // Dieser Klang fehlt im Port, daher der Hebel-Klang der Mod -- wie beim Kabelschalter aus Runde 5.
        level.playSound(null, pos, NtmSoundEvents.LEVER.get(), SoundSource.BLOCKS, 1.0F, on ? 1.0F : 0.85F);

        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof CableSwitchBlockEntity sw) sw.updateState();
    }
}
