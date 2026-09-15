package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.NtmBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.SoundUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKDebrisRadiating.
 *
 * Der Schutt unmittelbar um eine ausgelaufene Brennstoffsaeule. Er brennt wie gewoehnlicher
 * brennender Schutt, verstrahlt aber zusaetzlich alles im Umkreis von hundert Bloecken -- durch
 * Waende hindurch, nur gedaempft von deren Sprengfestigkeit. Wer naeher als fuenf Bloecke steht,
 * verbrennt obendrein.
 *
 * Er kuehlt nicht einfach aus: erst nach sechzehn Stufen wird er zu gewoehnlichem brennendem
 * Schutt, und ohne Bor dauert das sehr lange.
 *
 * ABWEICHUNGEN:
 * - Das Original nimmt Borsand und Borsandschichten als Beschleuniger. Beide fehlen im Port; der
 *   Borblock tut denselben Dienst und ist derselbe Stoff.
 * - NICHT UEBERNOMMEN: das Marshmallow-Osterei (wer eines in der Hand haelt und nah genug steht,
 *   bekommt es geroestet). Der Gegenstand ist nicht portiert.
 */
public class RBMKDebrisRadiatingBlock extends RBMKDebrisBurningBlock {

    /** Wie weit das Abklingen fortgeschritten ist; bei 15 ist Schluss. */
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 15);

    /** Die Strahlung an der Quelle, vor Abstand und Abschirmung. */
    private static final float RADS = 1000000F;
    private static final double RANGE = 100D;

    public RBMKDebrisRadiatingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DECAY, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECAY);
    }

    public static final MapCodec<RBMKDebrisRadiatingBlock> CODEC = simpleCodec(RBMKDebrisRadiatingBlock::new);

    @Override
    protected MapCodec<? extends RBMKDebrisRadiatingBlock> codec() {
        return CODEC;
    }

    private static int tickRate(RandomSource random) {
        return 20 + random.nextInt(20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        this.radiate(level, pos);

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

        /* Bor faengt die Neutronen ab: einer von 25 statt einer von 1000 je Durchgang. */
        int chance = neighbor.is(NtmBlocks.BLOCK_BORON.get()) ? 25 : 1000;

        if(random.nextInt(chance) == 0) {

            int decay = state.getValue(DECAY);

            if(decay < 15) {
                level.setBlock(pos, state.setValue(DECAY, decay + 1), 2);
                level.scheduleTick(pos, this, tickRate(random));
            } else {
                level.setBlock(pos, NtmBlocks.RBMK_DEBRIS_BURNING.get().defaultBlockState(), 3);
            }

        } else {
            level.scheduleTick(pos, this, tickRate(random));
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {

        if(level.isClientSide) return;

        if(level.random.nextInt(3) == 0) spawnFlame(level, pos, level.random);

        level.scheduleTick(pos, this, tickRate(level.random));
    }

    /**
     * Verstrahlt alles in Reichweite. Die Dosis faellt mit dem Quadrat des Abstands und wird
     * zusaetzlich durch alles gedaempft, was dazwischen steht -- aufsummiert ueber die
     * Sprengfestigkeit jedes Blocks auf der Sichtlinie.
     */
    private void radiate(ServerLevel level, BlockPos pos) {

        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(center, center).inflate(RANGE);

        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {

            Vec3 delta = new Vec3(
                    entity.getX() - center.x,
                    entity.getY() + entity.getEyeHeight() - center.y,
                    entity.getZ() - center.z);

            double len = delta.length();
            if(len <= 0) continue;

            Vec3 dir = delta.normalize();

            float res = 0F;
            for(int i = 1; i < len; i++) {
                BlockPos step = BlockPos.containing(center.add(dir.scale(i)));
                res += level.getBlockState(step).getBlock().getExplosionResistance();
            }

            if(res < 1F) res = 1F;

            float rads = (float) (RADS / res / (len * len));
            ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, rads);

            /* Wer so nah steht, hat ganz andere Sorgen als die Dosis. */
            if(len < 5) entity.hurt(level.damageSources().inFire(), 100F);
        }
    }
}
