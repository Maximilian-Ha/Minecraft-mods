package com.hbm.blocks.machine.rbmk;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.rbmk.RBMKDisplayBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKDisplay.
 *
 * Die Rasteranzeige. Sie hat keine Oberflaeche -- eingestellt wird nur der Zielpunkt (mit dem
 * Verbindungsstab) und die Ausrichtung des Rasters (mit dem Schraubenzieher).
 */
public class RBMKDisplayBlock extends RBMKMiniPanelBlock implements EntityBlock, IToolable {

    public RBMKDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKDisplayBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;

        if(!level.isClientSide && level.getBlockEntity(pos) instanceof RBMKDisplayBlockEntity display) {
            display.rotate();
        }

        return true;
    }

    public static final MapCodec<RBMKDisplayBlock> CODEC = simpleCodec(RBMKDisplayBlock::new);
    @Override protected MapCodec<? extends RBMKDisplayBlock> codec() { return CODEC; }
}
