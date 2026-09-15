package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.IScreenProvider;
import com.hbm.blockentity.machine.rbmk.RBMKGaugeBlockEntity;
import com.hbm.inventory.screens.RBMKGaugeScreen;
import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKGauge.
 * Die Tafel mit den vier Rundinstrumenten.
 */
public class RBMKGaugeBlock extends RBMKMiniPanelBlock implements EntityBlock, IScreenProvider {

    public RBMKGaugeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKGaugeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /* Rechtsklick ohne Schleichen oeffnet die Einstelloberflaeche. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(!player.isShiftKeyDown()) {
            NuclearTechMod.proxy.openScreen(player, pos);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object provideScreen(Player player, BlockPos pos) {
        BlockEntity be = player.level().getBlockEntity(pos);
        if(be instanceof RBMKGaugeBlockEntity gauge) return new RBMKGaugeScreen(gauge);
        return null;
    }

    public static final MapCodec<RBMKGaugeBlock> CODEC = simpleCodec(RBMKGaugeBlock::new);
    @Override protected MapCodec<? extends RBMKGaugeBlock> codec() { return CODEC; }
}
