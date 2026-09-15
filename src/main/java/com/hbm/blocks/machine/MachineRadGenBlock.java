package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineRadGenBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineRadGen.
 *
 * Drei Bloecke breit, sechs lang, drei hoch. Der Stromabgriff sitzt hinten, fuenf Bloecke vom
 * Kern entfernt -- ein Radiothermalgenerator gibt ab, er nimmt nichts an.
 */
public class MachineRadGenBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineRadGenBlock> CODEC = simpleCodec(MachineRadGenBlock::new);

    public MachineRadGenBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineRadGenBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineRadGenBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 3, 2, 1, 1 }; }
    @Override public int getOffset() { return 2; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        /* Der Stromabgriff sitzt am hinteren Rand des Baus -- der Blockeintrag schiebt einen
         * Block dahinter, also in das, was dort angeschlossen ist. Dazu zwei Anschlussstellen
         * links und rechts des Kerns.
         *
         * ACHTUNG BEIM VERGLEICH MIT DEM ORIGINAL: dort ist x die SETZSTELLE, nicht der Kern --
         * der Versatz ist negativ. Die erste Stelle rechnet deshalb von pos, die beiden anderen
         * von core. */
        this.makeExtra(level, pos.relative(dir, -5));
        this.makeExtra(level, core.relative(rot));
        this.makeExtra(level, core.relative(rot.getOpposite()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
