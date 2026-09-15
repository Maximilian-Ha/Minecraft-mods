package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.ReactorResearchBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.ReactorResearch.
 *
 * Eine drei Bloecke hohe Saeule. Steht Wasser daneben, steigen Blasen auf -- so sieht man von
 * aussen, ob das Becken geflutet ist.
 */
public class ReactorResearchBlock extends DummyableBlock implements ITooltipProvider {

    public ReactorResearchBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<ReactorResearchBlock> CODEC = simpleCodec(ReactorResearchBlock::new);
    @Override public MapCodec<ReactorResearchBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new ReactorResearchBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory();
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

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        for(Direction dir : Direction.Plane.HORIZONTAL) {

            BlockPos side = pos.relative(dir);
            if(level.getFluidState(side).getType() != Fluids.WATER
                    && level.getFluidState(side).getType() != Fluids.FLOWING_WATER) continue;

            double x = pos.getX() + 0.5D + dir.getStepX() * 0.5D + random.nextDouble() * 0.125D * dir.getStepX();
            double y = pos.getY() + 0.5D + random.nextDouble() - 0.5D;
            double z = pos.getZ() + 0.5D + dir.getStepZ() * 0.5D + random.nextDouble() * 0.125D * dir.getStepZ();

            level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0D, 0.2D, 0.0D);
        }
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 0, 0, 0, 0 }; }
    @Override public int getOffset() { return 0; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
