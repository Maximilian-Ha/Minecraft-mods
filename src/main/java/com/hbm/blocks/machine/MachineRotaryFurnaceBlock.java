package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineRotaryFurnaceBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineRotaryFurnace.
 *
 * Ein langgestreckter Bau. Hinten sitzen die drei Zutatenklappen und der Dampfanschluss, vorn
 * der Brennstoff und die Fluidanschluesse, oben das Abgasrohr. Welcher Huellenblock wofuer da
 * ist, sagt die Einblendung beim Hinsehen.
 */
public class MachineRotaryFurnaceBlock extends DummyableBlock implements ILookOverlay {

    public MachineRotaryFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineRotaryFurnaceBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineRotaryFurnaceBlock> CODEC = simpleCodec(MachineRotaryFurnaceBlock::new);
    @Override public MapCodec<MachineRotaryFurnaceBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {4, 0, 1, 1, 2, 2}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getCounterClockWise();

        // hinten: die drei Zutatenklappen und die beiden Dampfanschluesse
        for(int i = -2; i <= 2; i++) {
            this.makeExtra(level, core.relative(dir, -1).relative(rot, i));
        }

        // seitlicher Fluidanschluss
        this.makeExtra(level, core.relative(dir, 1).relative(rot, 2));
        // Abgasrohr
        this.makeExtra(level, core.relative(rot, 1).above(4));
        // Brennstoff
        this.makeExtra(level, core.relative(dir, 1).relative(rot, 1));
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof MachineRotaryFurnaceBlockEntity furnace)) return;

        Direction dir = furnace.getPointing();
        List<Component> text = new ArrayList<>();

        if(hits(corePos, dir, -1, -1, 0, pos) || hits(corePos, dir, -1, -2, 0, pos)) {
            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(furnace.tanks[1].getTankType().getName().copy().withStyle(ChatFormatting.RESET)));
            text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(furnace.tanks[2].getTankType().getName().copy().withStyle(ChatFormatting.RESET)));
        }

        if(hits(corePos, dir, 1, 2, 0, pos) || hits(corePos, dir, -1, 2, 0, pos)) {
            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(furnace.tanks[0].getTankType().getName().copy().withStyle(ChatFormatting.RESET)));
        }

        if(hits(corePos, dir, 1, 1, 0, pos)) {
            text.add(Component.literal("-> ").withStyle(ChatFormatting.YELLOW).append(Component.literal("Fuel").withStyle(ChatFormatting.RESET)));
        }

        if(!text.isEmpty()) ILookOverlay.printGeneric(event, this.getName(), 0xFFFF00, 0x404000, text);
    }

    /** Liegt der angesehene Block an der Stelle, die in Richtung und Querrichtung angegeben ist? */
    private static boolean hits(BlockPos core, Direction dir, int alongDir, int alongRot, int up, BlockPos hit) {
        Direction rot = dir.getCounterClockWise();
        return core.relative(dir, alongDir).relative(rot, alongRot).above(up).equals(hit);
    }
}
