package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineCyclotronBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCyclotron.
 *
 * Fuenf mal fuenf Bloecke, drei hoch. Die zwoelf Anschlussbloecke liegen an den vier
 * Aussenkanten, je drei nebeneinander -- und welcher der drei es ist, entscheidet, welche
 * Bahn ein Trichter dort erreicht.
 *
 * NICHT UEBERNOMMEN: die vier Stecker, die das Original als Ostereier vorsieht. Sie kosten
 * einen Gegenstand und aendern nur das Modell, das der Port ohnehin nicht hat.
 */
public class MachineCyclotronBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineCyclotronBlock> CODEC = simpleCodec(MachineCyclotronBlock::new);

    public MachineCyclotronBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineCyclotronBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineCyclotronBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 2, 2, 2, 2 }; }
    @Override public int getOffset() { return 2; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        /* Je drei Anschluesse in der Mitte jeder Aussenkante. */
        for(Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos middle = core.relative(side, 2);
            Direction rot = side.getClockWise();

            this.makeExtra(level, middle.relative(rot));
            this.makeExtra(level, middle);
            this.makeExtra(level, middle.relative(rot.getOpposite()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
