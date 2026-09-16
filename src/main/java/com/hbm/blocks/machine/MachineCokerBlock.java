package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.oil.MachineCokerBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCoker.
 *
 * Ein schlanker Turm von dreiundzwanzig Bloecken auf einem Sockel von drei mal drei, dazu ein
 * Absatz von fuenf mal fuenf ueber dem Sockel und vier Stuetzen an dessen Ecken. Anders als
 * beim Krackturm haengt nichts davon an der Blickrichtung -- alle Teilkoerper stehen im
 * Original fest nach Norden ausgerichtet, und hier ebenso.
 */
public class MachineCokerBlock extends DummyableBlock implements ITooltipProvider {

    /** Der Absatz ueber dem Sockel: fuenf mal fuenf, sechs Bloecke hoch. */
    private static final int[] DIM_SHOULDER = { 5, 0, 2, 2, 2, 2 };
    /** Je eine Stuetze an den vier Ecken des Absatzes. */
    private static final int[] DIM_LEG = { 0, 1, 0, 0, 0, 0 };

    public MachineCokerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineCokerBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineCokerBlock> CODEC = simpleCodec(MachineCokerBlock::new);
    @Override public MapCodec<MachineCokerBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {22, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!super.checkRequirement(level, pos, dir, offset)) return false;

        BlockPos core = pos.relative(dir, offset);

        if(!MultiblockHandlerXR.checkSpace(level, core.above(), DIM_SHOULDER, core, Direction.NORTH)) return false;

        for(BlockPos leg : this.legs(core)) {
            if(!MultiblockHandlerXR.checkSpace(level, leg, DIM_LEG, core, Direction.NORTH)) return false;
        }

        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        MultiblockHandlerXR.fillSpace(level, core.above(), DIM_SHOULDER, this, Direction.NORTH);

        for(BlockPos leg : this.legs(core)) {
            MultiblockHandlerXR.fillSpace(level, leg, DIM_LEG, this, Direction.NORTH);
        }

        /* Die vier Ecken des Sockels tragen die Anschluesse. */
        this.makeExtra(level, core.offset(1, 0, 1));
        this.makeExtra(level, core.offset(1, 0, -1));
        this.makeExtra(level, core.offset(-1, 0, 1));
        this.makeExtra(level, core.offset(-1, 0, -1));
    }

    private BlockPos[] legs(BlockPos core) {
        return new BlockPos[] {
                core.offset(2, 1, 2),
                core.offset(2, 1, -2),
                core.offset(-2, 1, 2),
                core.offset(-2, 1, -2)
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
