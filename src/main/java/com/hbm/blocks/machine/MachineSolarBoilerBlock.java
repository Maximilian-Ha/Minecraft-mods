package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.SolarBoilerBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

public class MachineSolarBoilerBlock extends DummyableBlock implements ILookOverlay {

    public MachineSolarBoilerBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineSolarBoilerBlock> CODEC = simpleCodec(MachineSolarBoilerBlock::new);
    @Override public MapCodec<MachineSolarBoilerBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new SolarBoilerBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        // Der Fluidanschluss sitzt zwei Bloecke ueber dem Kern, oben am Turm
        this.makeExtra(level, pos.relative(dir, offset).above(2));
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {
        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        BlockEntity be = level.getBlockEntity(corePos);
        if(!(be instanceof SolarBoilerBlockEntity boiler)) return;

        List<Component> text = new ArrayList<>();

        FluidTank[] tanks = boiler.getAllTanks();

        for(int i = 0; i < tanks.length; i++) {
            text.add(Component.literal(i < 1 ? "-> " : "<- ").withStyle(i < 1 ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(tanks[i].getTankType().getName())
                    .append(Component.literal(": " + tanks[i].getFill() + "/" + tanks[i].getMaxFill() + "mB").withStyle(ChatFormatting.GRAY)));
        }

        if(boiler.display < 1) {
            // Original blinkt zwischen Rot und Gelb
            text.add(Component.literal("Too cold!").withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.YELLOW));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
