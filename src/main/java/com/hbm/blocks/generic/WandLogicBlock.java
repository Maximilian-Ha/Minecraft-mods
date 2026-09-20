package com.hbm.blocks.generic;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.WandLogicBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockWandLogic.
 *
 * DER ZWEITE ZAUBERSTAB, den der Port als Block braucht -- nach dem Beutestab. Er ist die
 * Falle in einem Bauwerk: in ihm stehen zwei Namen, eine Bedingung und eine Aktion, und er
 * fuehrt sie aus, solange er steht. Was er kann, steht in der Blockentitaet; hier steht nur,
 * dass er tickt und dass ein Rechtsklick seine Wechselwirkung ruft.
 *
 * KEINE AUSRICHTUNG IM BLOCKZUSTAND. Das Original traegt die Richtung als Metadatenwert UND
 * ein zweites Mal in der Blockentitaet (Feld "rotation"), und nur die zweite liest es je aus.
 * Der Port fuehrt deshalb nur die zweite -- ein Zustandswert, den niemand liest, waere ein
 * Zustandswert zu viel.
 *
 * ER TAUCHT IN KEINEM REITER AUF, wie der Beutestab auch: ein Bauwerksstueck, kein Gegenstand
 * fuer die Hand.
 */
public class WandLogicBlock extends BaseEntityBlock {

    public static final MapCodec<WandLogicBlock> CODEC = simpleCodec(WandLogicBlock::new);

    public WandLogicBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<WandLogicBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WandLogicBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player spieler, BlockHitResult treffer) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(level.getBlockEntity(pos) instanceof WandLogicBlockEntity be) be.benutze(spieler);
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return createTickerHelper(type, NtmBlockEntityTypes.WAND_LOGIC.get(), (lvl, pos, st, be) -> be.serverTick());
    }
}
