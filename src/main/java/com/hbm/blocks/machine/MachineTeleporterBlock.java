package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineTeleporterBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineTeleporter.
 *
 * Ein Block ohne Ausrichtung und ohne Oberflaeche. Eingestellt wird er mit dem
 * Verbindungsstueck; was er weiss, steht in der Anzeige beim Hinsehen.
 */
public class MachineTeleporterBlock extends BaseEntityBlock implements ILookOverlay {

    public static final MapCodec<MachineTeleporterBlock> CODEC = simpleCodec(MachineTeleporterBlock::new);

    public MachineTeleporterBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineTeleporterBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineTeleporterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof MachineTeleporterBlockEntity tele)) return;

        List<Component> text = new ArrayList<>();

        if(!tele.hasTarget()) {
            text.add(Component.translatable("teleporter.noDestination").withStyle(ChatFormatting.RED));
        } else {
            text.add(Component.literal(String.format(Locale.US, "%,d", tele.getPower()) + " / " + String.format(Locale.US, "%,d", tele.getMaxPower()))
                    .withStyle(tele.getPower() >= MachineTeleporterBlockEntity.CONSUMPTION ? ChatFormatting.GREEN : ChatFormatting.RED));
            text.add(Component.translatable("teleporter.destination",
                    tele.targetX + " / " + tele.targetY + " / " + tele.targetZ,
                    tele.targetDim == null ? "-" : tele.targetDim.toString()));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
