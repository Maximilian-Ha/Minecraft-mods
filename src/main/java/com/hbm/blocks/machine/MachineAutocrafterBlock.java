package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineAutocrafterBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineAutocrafter.
 *
 * Ein Block ohne Ausrichtung -- oben, unten und Seite haben je ihr eigenes Bild, mehr braucht er
 * nicht. Er nimmt von allen Seiten an und gibt nach allen Seiten ab.
 *
 * BEIM ABBAUEN FALLEN NUR DIE ZUTATEN UND DIE AUSGABE HERAUS, nicht die neun Muster. Die sind
 * Abbilder und kein Gut; wer sie mit ausschuettete, verwandelte eine Einstellung in Material aus
 * dem Nichts. Das Original zaehlt dafuer ebenfalls erst ab Fach zehn.
 */
public class MachineAutocrafterBlock extends BaseEntityBlock {

    public static final MapCodec<MachineAutocrafterBlock> CODEC = simpleCodec(MachineAutocrafterBlock::new);

    public MachineAutocrafterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MachineAutocrafterBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineAutocrafterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if(be instanceof ITickable tickable) tickable.updateEntity();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        if(level.getBlockEntity(pos) instanceof MenuProvider provider) player.openMenu(provider, pos);

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineAutocrafterBlockEntity machine) {

            for(int i = MachineAutocrafterBlockEntity.SLOT_INGREDIENTS; i < MachineAutocrafterBlockEntity.SLOTS; i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), machine.getItem(i));
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
