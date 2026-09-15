package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.CableSwitchBlockEntity;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CableSwitch.
 *
 * Im Original waren an/aus die Metadaten 1/0 mit zwei Texturen; hier ist es die
 * Blockstate-Eigenschaft LIT. Ausgeschaltet entsteht kein Netzknoten, das Netz ist
 * an dieser Stelle also aufgetrennt.
 */
public class CableSwitchBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<CableSwitchBlock> CODEC = simpleCodec(CableSwitchBlock::new);

    public CableSwitchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.FALSE));
    }

    @Override
    public MapCodec<CableSwitchBlock> codec() {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        boolean on = state.getValue(LIT);
        level.setBlock(pos, state.setValue(LIT, !on), Block.UPDATE_CLIENTS);
        // Original: "hbm:block.reactorStart" mit Tonhoehe 1.0 beim Einschalten, 0.85 beim Ausschalten.
        // Dieser Klang fehlt im Port, daher der Hebel-Klang der Mod.
        level.playSound(null, pos, NtmSoundEvents.LEVER.get(), SoundSource.BLOCKS, 1.0F, on ? 0.85F : 1.0F);

        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof CableSwitchBlockEntity sw) sw.updateState();

        return InteractionResult.CONSUME;
    }
}
