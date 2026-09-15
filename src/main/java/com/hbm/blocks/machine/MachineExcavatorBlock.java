package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineExcavatorBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineExcavator.
 *
 * Ein Klotz von sieben mal sieben mal vier, mit drei zusaetzlichen Auslegern -- das Original
 * fuehrt sie in getAllDimensions. Der Port schreibt sie als drei eigene Grundrisse aus und legt
 * sie ueber dieselbe Pruefung wie die Hauptflaeche; das Verfahren ist das des Kompressors aus
 * einer frueheren Runde.
 *
 * DER KERN LIEGT DREI BLOECKE HOCH, nicht am Boden: der Bohrer haengt darunter und braucht Platz.
 */
public class MachineExcavatorBlock extends DummyableBlock implements ITooltipProvider {

    /** Die drei Ausleger, die das Original ueber getAllDimensions mitbelegt. */
    private static final int[] DIM_ARM_FRONT = new int[] { -1, 3, 3, -2, 3, -2 };
    private static final int[] DIM_ARM_LEFT  = new int[] { -1, 3, 3, -2, -2, 3 };
    private static final int[] DIM_ARM_RIGHT = new int[] { -1, 3, -2, 3, 3, 3 };

    public static final MapCodec<MachineExcavatorBlock> CODEC = simpleCodec(MachineExcavatorBlock::new);

    public MachineExcavatorBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineExcavatorBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineExcavatorBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 3, 3, 3, 3 }; }
    @Override public int getOffset() { return 3; }
    @Override public int getHeightOffset() { return 3; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        BlockPos core = pos.relative(dir, offset);
        return super.checkRequirement(level, pos, dir, offset)
                && MultiblockHandlerXR.checkSpace(level, core, DIM_ARM_FRONT, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, core, DIM_ARM_LEFT, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, core, DIM_ARM_RIGHT, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        MultiblockHandlerXR.fillSpace(level, core, DIM_ARM_FRONT, this, dir);
        MultiblockHandlerXR.fillSpace(level, core, DIM_ARM_LEFT, this, dir);
        MultiblockHandlerXR.fillSpace(level, core, DIM_ARM_RIGHT, this, dir);

        Direction rot = dir.getClockWise();

        /* Vier Anschlussstellen eine Lage ueber dem Kern. */
        this.makeExtra(level, core.relative(dir, 3).relative(rot).above());
        this.makeExtra(level, core.relative(dir, 3).relative(rot.getOpposite()).above());
        this.makeExtra(level, core.relative(rot, 3).above());
        this.makeExtra(level, core.relative(rot.getOpposite(), 3).above());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
