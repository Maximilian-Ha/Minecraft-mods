package com.hbm.blocks.network;

import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.RadioTorchReaderBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.screens.RadioTorchReaderScreen;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.RadioTorchReader.
 *
 * Anders als Sender, Empfaenger und Zaehler haelt der Leser nicht an jeder festen Wand: er
 * braucht hinter sich eine Maschine, die Werte herausgibt. Das Original loest das ueber
 * canBlockStay, im Port heisst dieselbe Pruefung canSurvive -- sie greift beim Setzen und
 * wieder, sobald sich der Nachbar dahinter aendert.
 */
public class RadioTorchReaderBlock extends RadioTorchBaseBlock {

    public static final MapCodec<RadioTorchReaderBlock> CODEC = simpleCodec(RadioTorchReaderBlock::new);

    public RadioTorchReaderBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<RadioTorchReaderBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioTorchReaderBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction hinten = state.getValue(FACING).getOpposite();
        return level.getBlockEntity(pos.relative(hinten)) instanceof IRORValueProvider;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object provideScreen(Player player, BlockPos pos) {
        if(player.level().getBlockEntity(pos) instanceof RadioTorchReaderBlockEntity leser) return new RadioTorchReaderScreen(leser);
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof RadioTorchReaderBlockEntity leser)) return;

        List<Component> text = new ArrayList<>();
        for(int i = 0; i < RadioTorchReaderBlockEntity.KANAELE; i++) {
            if(leser.channels[i] == null || leser.channels[i].isEmpty()) continue;
            if(leser.names[i] == null || leser.names[i].isEmpty()) continue;
            text.add(Component.literal(leser.channels[i] + ": " + leser.names[i]).withStyle(ChatFormatting.AQUA));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
