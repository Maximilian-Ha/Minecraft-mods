package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.bomb.DetonatableBlock;
import com.hbm.entity.item.TNTPrimedBase;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.YellowBarrel.
 *
 * Das gelbe Fass und seine verglaste Schwester. Beide strahlen staendig -- das gelbe stark,
 * das verglaste schwach --, und beide gehen hoch, wenn man sie beschiesst oder anzuendet.
 *
 * Drei Dinge unterscheiden die zwei, alle aus dem Original uebernommen:
 *  - die Strahlung im Takt (5 gegen 0,5 je Sekunde),
 *  - nur das gelbe zuendet mit, wenn nebenan etwas explodiert,
 *  - beim Bersten wird zu einem Drittel Giftbruehe gesetzt, sonst eine Explosion der
 *    Staerke 12 ausgeloest; dazu Fallout und Radongas im Umkreis von fuenf Bloecken.
 */
public class YellowBarrelBlock extends DetonatableBlock implements SimpleWaterloggedBlock {

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    /** Wieviel Strahlung das Fass je Sekunde abgibt. */
    private final float radiation;

    /** Nur das gelbe Fass geht mit hoch, wenn nebenan etwas explodiert. */
    private final boolean chainReacts;

    public YellowBarrelBlock(Properties properties, float radiation, boolean chainReacts) {
        super(properties, 0, 0, 100, true, false);
        this.radiation = radiation;
        this.chainReacts = chainReacts;
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if(state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    /** Das Original strahlt im Takt von 20 Ticks; hier uebernimmt das der Zufallstakt. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ChunkRadiationManager.proxy.incrementRad(level, pos, this.radiation);
    }

    /**
     * Das verglaste Fass zuendet nicht mit, wenn nebenan etwas hochgeht -- es wird von der
     * Explosion einfach zerstoert. Im Original steht dafuer die Abfrage
     * "if(this != ModBlocks.yellow_barrel) return;" in onBlockDestroyedByExplosion.
     */
    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if(!this.chainReacts) return;
        super.wasExploded(level, pos, explosion);
    }

    @Override
    public void explodeEntity(Level level, double x, double y, double z, TNTPrimedBase entity) {

        BlockPos pos = BlockPos.containing(x, y, z);
        RandomSource random = level.random;

        if(random.nextInt(3) == 0) {
            level.setBlock(pos, NtmBlocks.TOXIC_BLOCK.get().defaultBlockState(), 3);
        } else {
            level.explode(entity, x, y, z, 12.0F, Level.ExplosionInteraction.TNT);
        }

        ExplosionNukeGeneric.waste(level, pos, 35);

        int ix = Mth.floor(x), iy = Mth.floor(y), iz = Mth.floor(z);
        for(int i = -5; i <= 5; i++) {
            for(int j = -5; j <= 5; j++) {
                for(int k = -5; k <= 5; k++) {
                    BlockPos target = new BlockPos(ix + i, iy + j, iz + k);
                    if(random.nextInt(5) == 0 && level.getBlockState(target).is(Blocks.AIR)) {
                        level.setBlock(target, NtmBlocks.GAS_RADON_DENSE.get().defaultBlockState(), 3);
                    }
                }
            }
        }

        ChunkRadiationManager.proxy.incrementRad(level, pos, 35F);
    }
}
