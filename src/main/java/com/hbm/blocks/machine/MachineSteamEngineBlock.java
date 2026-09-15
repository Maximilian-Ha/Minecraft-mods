package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineSteamEngineBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Pre;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachineSteamEngineBlock extends DummyableBlock implements ILookOverlay, ITooltipProvider {

    public MachineSteamEngineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineSteamEngineBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineSteamEngineBlock> CODEC = simpleCodec(MachineSteamEngineBlock::new);
    @Override public MapCodec<MachineSteamEngineBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {1, 0, 5, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        this.makeExtra(level, core.offset(rot.getStepX(), 1, rot.getStepZ()));
        this.makeExtra(level, core.offset(rot.getStepX() + dir.getStepX(), 1, rot.getStepZ() + dir.getStepZ()));
        this.makeExtra(level, core.offset(rot.getStepX() - dir.getStepX(), 1, rot.getStepZ() - dir.getStepZ()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(Pre event, Level level, BlockPos pos) {
        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        BlockEntity blockEntity = level.getBlockEntity(corePos);
        if(blockEntity instanceof MachineSteamEngineBlockEntity engine) {
            List<Component> text = new ArrayList<>();

            FluidTank tankInput = engine.tanks[0];
            FluidTank tankOutput = engine.tanks[1];

            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(tankInput.getTankType().getName()).append(": " + String.format(Locale.US, "%,d", tankInput.getFill()) + " / " + String.format(Locale.US, "%,d", tankInput.getMaxFill()) + "mB").withStyle(ChatFormatting.RESET));
            text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(tankOutput.getTankType().getName()).append(": " + String.format(Locale.US, "%,d", tankOutput.getFill()) + " / " + String.format(Locale.US, "%,d", tankOutput.getMaxFill()) + "mB").withStyle(ChatFormatting.RESET));

            ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
        }
    }
}
