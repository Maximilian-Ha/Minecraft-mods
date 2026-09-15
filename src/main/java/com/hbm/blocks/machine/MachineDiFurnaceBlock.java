package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineDiFurnaceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
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

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineDiFurnace.
 *
 * Im Original waren "aus" und "an" zwei getrennte Bloecke; hier ist das die
 * Blockstate-Eigenschaft LIT, wie beim Vanilla-Ofen. Dass ein Aufsatz darueber sitzt,
 * wechselte im Original die Texturen ueber getIcon; hier haelt COVERED diesen Zustand,
 * damit die Datengeneratoren dafuer eigene Modelle hinterlegen koennen.
 */
public class MachineDiFurnaceBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** Aufsatz sitzt direkt darueber: schmalere Seitentextur, Oberseite wird zur Feuerfestziegeldecke. */
    public static final BooleanProperty COVERED = BooleanProperty.create("covered");

    public static final MapCodec<MachineDiFurnaceBlock> CODEC = simpleCodec(MachineDiFurnaceBlock::new);

    public MachineDiFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE)
                .setValue(COVERED, Boolean.FALSE));
    }

    @Override
    public MapCodec<MachineDiFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, COVERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean covered = context.getLevel().getBlockState(context.getClickedPos().above()).getBlock() instanceof MachineDiFurnaceExtensionBlock;

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, Boolean.FALSE)
                .setValue(COVERED, covered);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if(direction == Direction.UP) {
            return state.setValue(COVERED, neighborState.getBlock() instanceof MachineDiFurnaceExtensionBlock);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineDiFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if(be instanceof ITickable tickable) tickable.updateEntity();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof MachineDiFurnaceBlockEntity machine) {
                Containers.dropContents(level, pos, machine);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Flammen an der Feuerklappe und Rauch aus dem Schacht, Versatz wie im Original. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if(!state.getValue(LIT)) return;

        Direction dir = state.getValue(FACING);
        Direction lateral = dir.getClockWise();

        float x0 = pos.getX() + 0.5F;
        float y0 = pos.getY() + 0.25F + random.nextFloat() * 6.0F / 16.0F;
        float z0 = pos.getZ() + 0.5F;
        float sideOff = 0.52F;
        float sideRand = random.nextFloat() * 0.5F - 0.25F;
        float xOff = random.nextFloat() * 0.375F + 0.3125F;
        float zOff = random.nextFloat() * 0.375F + 0.3125F;

        // Mit Aufsatz steigt der Rauch eine Ebene hoeher auf.
        int smokeY = pos.getY() + (state.getValue(COVERED) ? 2 : 1);

        level.addParticle(ParticleTypes.FLAME,
                x0 + dir.getStepX() * sideOff + lateral.getStepX() * sideRand,
                y0,
                z0 + dir.getStepZ() * sideOff + lateral.getStepZ() * sideRand,
                0.0D, 0.0D, 0.0D);

        level.addParticle(ParticleTypes.SMOKE, pos.getX() + xOff, smokeY, pos.getZ() + zOff, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
