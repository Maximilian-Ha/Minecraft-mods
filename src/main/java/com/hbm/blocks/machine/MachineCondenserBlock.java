package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.CondenserBlockEntity;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCondenser.
 *
 * Kein Multiblock: ein einzelner Wuerfel, der Altdampf zu Wasser macht.
 */
public class MachineCondenserBlock extends BaseEntityBlock implements ILookOverlay {

    public static final MapCodec<MachineCondenserBlock> CODEC = simpleCodec(MachineCondenserBlock::new);

    public MachineCondenserBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineCondenserBlock> codec() { return CODEC; }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CondenserBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof CondenserBlockEntity condenser)) return;

        List<Component> text = new ArrayList<>();

        for(int i = 0; i < condenser.tanks.length; i++) {
            String fill = ": " + condenser.tanks[i].getFill() + "/" + condenser.tanks[i].getMaxFill() + "mB";

            if(i < 1) {
                text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(condenser.tanks[i].getTankType().getName()).append(fill).withStyle(ChatFormatting.RESET));
            } else {
                text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(condenser.tanks[i].getTankType().getName()).append(fill).withStyle(ChatFormatting.RESET));
            }
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
