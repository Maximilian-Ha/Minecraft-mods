package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.RadioTorchCounterBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.RadioTorchCounter.
 *
 * Anders als Sender und Empfaenger hat der Zaehler ein Inventar -- drei Musterfaecher --, und
 * damit eine richtige Oberflaeche mit Menue statt eines blossen Bildschirms. Der Rechtsklick
 * der Basis wird deshalb hier ueberschrieben.
 */
public class RadioTorchCounterBlock extends RadioTorchBaseBlock {

    public static final MapCodec<RadioTorchCounterBlock> CODEC = simpleCodec(RadioTorchCounterBlock::new);

    public RadioTorchCounterBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<RadioTorchCounterBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioTorchCounterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        if(level.getBlockEntity(pos) instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
        }

        return InteractionResult.CONSUME;
    }

    /** Ein Bildschirm ohne Menue gehoert nicht hierher -- der Zaehler geht ueber openMenu. */
    @Override
    public Object provideScreen(Player player, BlockPos pos) {
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RadioTorchCounterBlockEntity zaehler) {
            Containers.dropContents(level, pos, zaehler);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof RadioTorchCounterBlockEntity zaehler)) return;

        List<Component> text = new ArrayList<>();
        for(int i = 0; i < RadioTorchCounterBlockEntity.KANAELE; i++) {
            if(zaehler.channel[i] == null || zaehler.channel[i].isEmpty()) continue;
            text.add(Component.literal(zaehler.channel[i] + ": " + zaehler.lastCount[i]).withStyle(ChatFormatting.AQUA));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
