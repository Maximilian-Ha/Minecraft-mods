package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.CondenserPoweredBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCondenserPowered.
 *
 * Multiblock mit denselben Abmessungen und Extra-Positionen wie im Original; der
 * Aufbau deckt sich mit dem des kompakten Verdichters, der dasselbe Modell benutzt.
 */
public class MachineCondenserPoweredBlock extends DummyableBlock implements ILookOverlay {

    public static final MapCodec<MachineCondenserPoweredBlock> CODEC = simpleCodec(MachineCondenserPoweredBlock::new);

    public MachineCondenserPoweredBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineCondenserPoweredBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new CondenserPoweredBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 3, 3}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos corePos = pos.relative(dir, offset);
        Direction rot = dir.getCounterClockWise();

        this.makeExtra(level, corePos.relative(rot, 3).above());
        this.makeExtra(level, corePos.relative(rot.getOpposite(), 3).above());
        this.makeExtra(level, corePos.relative(dir).relative(rot).above());
        this.makeExtra(level, corePos.relative(dir).relative(rot.getOpposite()).above());
        this.makeExtra(level, corePos.relative(dir.getOpposite()).relative(rot).above());
        this.makeExtra(level, corePos.relative(dir.getOpposite()).relative(rot.getOpposite()).above());
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        BlockEntity be = level.getBlockEntity(corePos);
        if(!(be instanceof CondenserPoweredBlockEntity tower)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal(BobMathUtil.getShortNumber(tower.power) + "HE / " + BobMathUtil.getShortNumber(CondenserPoweredBlockEntity.maxPower) + "HE"));

        for(int i = 0; i < tower.tanks.length; i++) {
            String fill = ": " + BobMathUtil.format(tower.tanks[i].getFill()) + "/" + BobMathUtil.format(tower.tanks[i].getMaxFill()) + "mB";

            if(i < 1) {
                text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(tower.tanks[i].getTankType().getName()).append(fill).withStyle(ChatFormatting.RESET));
            } else {
                text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(tower.tanks[i].getTankType().getName()).append(fill).withStyle(ChatFormatting.RESET));
            }
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
