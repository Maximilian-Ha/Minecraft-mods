package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineMissileAssemblyBlockEntity;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineMissileAssembly.
 *
 * Ein einzelner Block, kein Bauwerk -- die Raketenmontage ist der Werktisch fuer Eigenbauten.
 *
 * NICHT UEBERNOMMEN: der Aufruf von BossSpawnHandler.markFBI beim Oeffnen. Im Original merkt sich
 * das Spiel, wer an Raketen baut, und schickt ihm irgendwann Besuch. Den Gegenspieler gibt es im
 * Port nicht.
 */
public class MachineMissileAssemblyBlock extends BaseEntityBlock implements ITooltipProvider {

    public static final MapCodec<MachineMissileAssemblyBlock> CODEC = simpleCodec(MachineMissileAssemblyBlock::new);

    public MachineMissileAssemblyBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineMissileAssemblyBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineMissileAssemblyBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
                Containers.dropContents(level, pos, container);
                super.onRemove(state, level, pos, newState, isMoving);
                level.updateNeighbourForOutputSignal(pos, this);
                return;
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;

        if(!player.isShiftKeyDown()) {
            if(level.getBlockEntity(pos) instanceof MenuProvider menu) {
                player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
