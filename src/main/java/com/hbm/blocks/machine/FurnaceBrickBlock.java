package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FurnaceBrickBlockEntity;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineBrickFurnace.
 *
 * Im Original sind "aus" und "an" zwei getrennte Bloecke -- machine_furnace_brick_off und
 * machine_furnace_brick_on. Hier ist das die Eigenschaft LIT, wie beim Vanilla-Ofen und wie
 * schon beim Doppelofen des Ports.
 */
public class FurnaceBrickBlock extends BaseEntityBlock implements ITooltipProvider {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<FurnaceBrickBlock> CODEC = simpleCodec(FurnaceBrickBlock::new);

    public FurnaceBrickBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override public MapCodec<FurnaceBrickBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FurnaceBrickBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        if(level.getBlockEntity(pos) instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FurnaceBrickBlockEntity ofen) {
            Containers.dropContents(level, pos, ofen);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Rauch und Flamme an der Feuerklappe. Das Original schreibt dafuer vier Faelle aus, je
     * einen pro Metadatenwert; es ist aber viermal dieselbe Formel, nur mit gedrehten Achsen.
     * Mit FACING steht sie einmal da.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        if(!state.getValue(LIT)) return;

        Direction vorn = state.getValue(FACING);
        Direction quer = vorn.getClockWise();

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + random.nextFloat() * 0.375D;
        double z = pos.getZ() + 0.5D;
        double versatz = 0.52D;
        double streuung = random.nextFloat() * 0.6D - 0.3D;

        double px = x + vorn.getStepX() * versatz + quer.getStepX() * streuung;
        double pz = z + vorn.getStepZ() * versatz + quer.getStepZ() * streuung;

        level.addParticle(ParticleTypes.SMOKE, px, y, pz, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, px, y, pz, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
