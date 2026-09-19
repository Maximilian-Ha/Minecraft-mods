package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockMush.
 *
 * Der Pilz waechst auf allem, was Pflanzen traegt, und zusaetzlich auf den Muellboeden des
 * Mods. Er breitet sich langsam aus, verwandelt verseuchte Erde in Myzel und laesst sich mit
 * Knochenmehl zum Riesenpilz treiben.
 *
 * ABWEICHUNG -- und zwar eine bewusste: die Ausbreitung des Originals ist an zwei Stellen
 * vertauscht. Sie zaehlt die Nachbarn mit getBlock(ix, iz, iy) und setzt den neuen Pilz mit
 * setBlock(ix, iy, iz), wobei iy aus der z-Koordinate und iz aus der y-Koordinate stammt --
 * die Hoehe wird also aus einer waagerechten Koordinate gezogen und umgekehrt. Das ist die
 * Vanilla-Pilzroutine mit umbenannten Variablen, bei der die Reihenfolge der Argumente
 * stehen geblieben ist. Hier steht sie richtig herum; die Spannen des Originals
 * (plus/minus 2 waagerecht, plus/minus 1 senkrecht, hoechstens drei Nachbarn im Umkreis
 * von vier) bleiben unveraendert.
 */
public class MushBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<MushBlock> CODEC = simpleCodec(MushBlock::new);

    /** (0,3|0|0,3)-(0,7|0,4|0,7) aus setBlockBounds des Originals. */
    private static final VoxelShape SHAPE = Block.box(4.8D, 0.0D, 4.8D, 11.2D, 6.4D, 11.2D);

    public MushBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MushBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Das Original gibt keinen Kollisionskasten zurueck: man laeuft hindurch. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * Das Original fragt den Boden als Hoehlenpflanze (EnumPlantType.Cave). Vanilla
     * beantwortet das mit "hat der Block oben eine feste Flaeche?" -- in 1.21 steht dafuer
     * isFaceSturdy. Die Muellboeden des Mods sind volle Bloecke und damit eingeschlossen.
     */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    /** Die Muellboeden, auf denen der Pilz auch ohne Pflanzenwuchs steht. */
    public static boolean waechstHier(LevelReader level, BlockPos pos) {
        BlockState unten = level.getBlockState(pos.below());
        return unten.is(NtmBlocks.WASTE_EARTH.get())
                || unten.is(NtmBlocks.WASTE_MYCELIUM.get())
                || unten.is(NtmBlocks.WASTE_TRINITITE.get())
                || unten.is(NtmBlocks.WASTE_TRINITITE_RED.get())
                || unten.is(NtmBlocks.BLOCK_WASTE.get())
                || unten.is(NtmBlocks.BLOCK_WASTE_PAINTED.get())
                || unten.is(NtmBlocks.BLOCK_WASTE_VITRIFIED.get());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource zufall) {

        // Verseuchte Erde unter dem Pilz vermyzelt mit der Zeit.
        if(level.getBlockState(pos.below()).is(NtmBlocks.WASTE_EARTH.get()) && zufall.nextInt(5) == 0) {
            level.setBlockAndUpdate(pos.below(), NtmBlocks.WASTE_MYCELIUM.get().defaultBlockState());
        }

        if(zufall.nextInt(25) != 0) return;

        // Hoechstens drei Pilze im Umkreis von vier Bloecken und einer Lage darueber/darunter.
        int erlaubt = 3;
        for(int x = pos.getX() - 4; x <= pos.getX() + 4; x++) {
            for(int z = pos.getZ() - 4; z <= pos.getZ() + 4; z++) {
                for(int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                    if(level.getBlockState(new BlockPos(x, y, z)).is(this)) {
                        erlaubt--;
                        if(erlaubt <= 0) return;
                    }
                }
            }
        }

        BlockPos von = pos;
        BlockPos ziel = streue(von, zufall);

        for(int i = 0; i < 4; i++) {
            if(level.isEmptyBlock(ziel) && waechstHier(level, ziel)) von = ziel;
            ziel = streue(von, zufall);
        }

        if(level.isEmptyBlock(ziel) && waechstHier(level, ziel)) {
            level.setBlock(ziel, this.defaultBlockState(), 2);
        }
    }

    private static BlockPos streue(BlockPos pos, RandomSource zufall) {
        return new BlockPos(
                pos.getX() + zufall.nextInt(5) - 2,
                pos.getY() + zufall.nextInt(2) - zufall.nextInt(2),
                pos.getZ() + zufall.nextInt(5) - 2);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource zufall, BlockPos pos, BlockState state) {
        return zufall.nextFloat() < 0.4D;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource zufall, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
        HugeMush.wachse(level, pos);
    }
}
