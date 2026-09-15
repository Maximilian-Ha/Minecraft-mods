package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DiFurnaceExtensionBlockEntity;
import com.hbm.blockentity.machine.MachineDiFurnaceBlockEntity;
import com.hbm.blocks.IProxyController;
import com.hbm.util.Compat;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineDiFurnaceExtension.
 *
 * Eigenstaendig setzbarer Aufsatz. Sitzt er direkt ueber einem Doppelofen, verdreifacht
 * er dessen Arbeitstempo. Der Block selbst ist nur eine Huelle: Gegenstaende und Fluide
 * reicht er an den Ofen darunter weiter. Dargestellt wird er ueber das OBJ-Modell
 * difurnace_extension, deshalb liefert er kein Blockmodell.
 */
public class MachineDiFurnaceExtensionBlock extends BaseEntityBlock implements IProxyController {

    public static final MapCodec<MachineDiFurnaceExtensionBlock> CODEC = simpleCodec(MachineDiFurnaceExtensionBlock::new);

    public MachineDiFurnaceExtensionBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MachineDiFurnaceExtensionBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiFurnaceExtensionBlockEntity(pos, state);
    }

    @Override
    public BlockEntity getCore(Level level, BlockPos pos) {
        BlockEntity be = Compat.getBlockEntityStandard(level, pos.below());
        return be instanceof MachineDiFurnaceBlockEntity ? be : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        BlockEntity core = level.getBlockEntity(pos.below());
        if(core instanceof MachineDiFurnaceBlockEntity && core instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos.below());
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
