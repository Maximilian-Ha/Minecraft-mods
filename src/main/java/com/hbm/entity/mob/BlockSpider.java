package com.hbm.entity.mob;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityBlockSpider.
 *
 * Ein Block auf acht Beinen. Er zieht umher und geht auf Spieler los, und was er traegt,
 * entscheidet, wie zaeh er ist: seine Lebensenergie ist der Sprengwiderstand des Blocks,
 * mindestens aber eins.
 *
 * DER BLOCK STEHT IM NETZ: das Original fuehrt Blocknummer und Metawert in zwei
 * DataWatcher-Feldern. Auf 1.21 gibt es weder das eine noch das andere -- ein Blockzustand
 * traegt beides zusammen, und Block.getId/stateById bilden ihn auf eine Zahl ab, die sich
 * uebertragen laesst. Der Zeichner liest genau diese Zahl.
 *
 * ABWEICHUNG: das Original ruft getExplosionResistance(null) -- mit einer Entitaet, die es
 * nicht gibt. Auf 1.21 steht der Wert am Blockzustand selbst und braucht kein Gegenueber.
 */
public class BlockSpider extends Monster {

    private static final EntityDataAccessor<Integer> BLOCKZUSTAND =
            SynchedEntityData.defineId(BlockSpider.class, EntityDataSerializers.INT);

    public BlockSpider(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.5D));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLOCKZUSTAND, Block.getId(Blocks.STONE.defaultBlockState()));
    }

    public BlockState getBlockZustand() {
        return Block.stateById(this.entityData.get(BLOCKZUSTAND));
    }

    /** Setzt den getragenen Block und leitet die Lebensenergie daraus ab. */
    public void makeBlock(BlockState state) {

        this.entityData.set(BLOCKZUSTAND, Block.getId(state));

        double leben = Math.max(1D, state.getBlock().getExplosionResistance());

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(leben);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("blockState", this.entityData.get(BLOCKZUSTAND));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(BLOCKZUSTAND, tag.getInt("blockState"));
    }
}
