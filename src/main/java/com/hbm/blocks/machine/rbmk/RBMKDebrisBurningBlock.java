package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.NtmBlocks;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.SoundUtils;
import com.hbm.util.particle.ParticleUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKDebrisBurning.
 *
 * Brennender Schutt. Qualmt, setzt Schmelzgas frei und wird nach einiger Zeit zu normalem
 * Schutt. Schaum in der Nachbarschaft beschleunigt das Erloeschen deutlich.
 *
 * Abweichung vom Original: dort helfen neben dem Schaum auch Borsand und Schaumschichten,
 * die im Port noch fehlen. Der Schaumblock tut es genauso.
 */
public class RBMKDebrisBurningBlock extends RBMKDebrisBlock {

    public RBMKDebrisBurningBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKDebrisBurningBlock> CODEC = simpleCodec(RBMKDebrisBurningBlock::new);

    @Override
    protected MapCodec<? extends RBMKDebrisBurningBlock> codec() {
        return CODEC;
    }

    private static int tickRate(RandomSource random) {
        return 100 + random.nextInt(20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if(random.nextInt(5) == 0) {
            spawnFlame(level, pos, random);
            SoundUtils.playAtVec3(level, Vec3.atCenterOf(pos), SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F);
        }

        BlockPos neighborPos = pos.relative(Direction.values()[random.nextInt(6)]);
        BlockState neighbor = level.getBlockState(neighborPos);

        if(random.nextInt(10) == 0 && neighbor.isAir()) {
            level.setBlock(neighborPos, NtmBlocks.GAS_MELTDOWN.get().defaultBlockState(), 3);
        }

        // Schaum erstickt das Feuer: 1 von 10 statt 1 von 100 je Durchgang.
        int chance = neighbor.is(NtmBlocks.BLOCK_FOAM.get()) ? 10 : 100;

        if(random.nextInt(chance) == 0) {
            level.setBlock(pos, NtmBlocks.RBMK_DEBRIS.get().defaultBlockState(), 3);
        } else {
            level.scheduleTick(pos, this, tickRate(random));
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if(!level.isClientSide && level.random.nextInt(3) == 0) spawnFlame(level, pos, level.random);

        level.scheduleTick(pos, this, tickRate(level.random));
    }

    protected static void spawnFlame(Level level, BlockPos pos, RandomSource random) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("lifetime", 300);
        ParticleUtil.addParticle(level, new NbtParticleOptions(NtmParticleTypes.RBMK_FLAME.get(), tag),
                pos.getX() + 0.25 + random.nextDouble() * 0.5, pos.getY() + 1.75, pos.getZ() + 0.25 + random.nextDouble() * 0.5);
    }
}
