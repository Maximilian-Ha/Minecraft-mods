package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionPlasmaForgeBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionPlasmaForge.
 *
 * Die Plasmaschmiede ist elf Bloecke lang und stufenweise schmaler: in der Mitte fuenf Bloecke
 * breit, an den Enden nur noch zwei. Das Original setzt das aus neun Quadern zusammen; hier steht
 * derselbe Satz Masse, nur einmal statt achtmal ausgeschrieben.
 *
 * An beiden Stirnseiten sitzt je eine Reihe von fuenf Anschluessen fuer Strom und Fluessigkeit.
 */
public class MachineFusionPlasmaForgeBlock extends DummyableBlock implements ITooltipProvider {

    /* Die acht Quader neben dem Rumpf. Erst das Dach, dann die vier Stufen nach vorn und hinten. */
    private static final int[][] EXTRA_DIMS = new int[][] {
            {4, -3,  0,  0, 4, 4},
            {2,  0,  3, -2, 4, 4},
            {2,  0, -2,  3, 4, 4},
            {2,  0,  4, -3, 3, 3},
            {2,  0, -3,  4, 3, 3},
            {2,  0,  5, -4, 2, 2},
            {2,  0, -4,  5, 2, 2},
            {3, -2,  1,  1, 5, 5}
    };

    public MachineFusionPlasmaForgeBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionPlasmaForgeBlock> CODEC = simpleCodec(MachineFusionPlasmaForgeBlock::new);
    @Override public MapCodec<MachineFusionPlasmaForgeBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new FusionPlasmaForgeBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 2, 2, 5, 5 }; }
    @Override public int getOffset() { return 5; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!super.checkRequirement(level, pos, dir, offset)) return false;

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMS) {
            if(!MultiblockHandlerXR.checkSpace(level, core, dim, pos, dir)) return false;
        }

        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMS) MultiblockHandlerXR.fillSpace(level, core, dim, this, dir);

        Direction rot = dir.getClockWise();

        /* Die beiden Anschlussreihen an den Stirnseiten. */
        for(int i = -2; i <= 2; i++) {
            this.makeExtra(level, core.offset(
                    dir.getStepX() * 5 + rot.getStepX() * i, 0, dir.getStepZ() * 5 + rot.getStepZ() * i));
            this.makeExtra(level, core.offset(
                    -dir.getStepX() * 5 + rot.getStepX() * i, 0, -dir.getStepZ() * 5 + rot.getStepZ() * i));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
