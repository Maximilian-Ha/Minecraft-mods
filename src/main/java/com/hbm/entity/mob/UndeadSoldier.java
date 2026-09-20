package com.hbm.entity.mob;

import com.hbm.items.NtmItems;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityUndeadSoldier.
 *
 * Der Untote Soldat. Ein Wachposten der Halde: er traegt die Taurun-Ruestung und eine von
 * fuenf Handfeuerwaffen, und er ist zur Haelfte Zombie, zur Haelfte Skelett -- was er ist,
 * wuerfelt er beim Erscheinen aus, und es entscheidet ueber Aussehen und Stimme.
 *
 * ER IST DIE QUELLE DER TAURUN-RUESTUNG. Bis zu dieser Runde lag sie nur in der Kreativkiste;
 * im Original traegt sie dieser Soldat, und der Dungeon-Spawner stellt ihn auf.
 *
 * WAS ER NICHT FALLEN LAESST: nichts. Das Original ueberschreibt dropFewItems und
 * dropEquipment leer -- die Ruestung an seinem Leib ist nicht zu erbeuten, indem man ihn
 * erschlaegt. Der Port macht dasselbe ueber dropCustomDeathLoot, und eine Beutetabelle
 * bekommt er nicht (eine fehlende Tabelle ist in 1.21 die leere).
 *
 * WAS DER PORT ZUSAETZLICH BRAUCHT: ein MeleeAttackGoal. In 1.7.10 steckt das Zulaufen und
 * Zuschlagen in EntityMob.attackEntity und braucht keine Aufgabe; in 1.21 gibt es das nicht
 * mehr, und ohne diese Aufgabe stuende der Soldat nur da und saehe seinem Ziel zu.
 *
 * NICHT UEBERNOMMEN: getCanSpawnHere(). Das Original prueft dort Schwierigkeitsgrad, freie
 * Stelle und keine Fluessigkeit -- genau das, was Monster.checkSpawnRules und
 * Mob.checkSpawnObstruction in 1.21 von sich aus tun. Eine Wiederholung waere toter Code.
 * Ebenso wenig uebernommen: das Spawn-Ei aus EntityMappings.addMob. Der Port hat fuer keine
 * seiner Kreaturen eines, auch nicht fuer den nuklearen Creeper; gerufen wird per /summon.
 */
public class UndeadSoldier extends Monster {

    public enum Variante { ZOMBIE, SKELETT }

    private static final EntityDataAccessor<Byte> VARIANTE = SynchedEntityData.defineId(UndeadSoldier.class, EntityDataSerializers.BYTE);

    public UndeadSoldier(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        /* Kein WaterAvoidingRandomStrollGoal: das Original nimmt EntityAIWander, und das
         * meidet kein Wasser. Dafuer schwimmt er (FloatGoal oben). */
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANTE, (byte) Variante.ZOMBIE.ordinal());
    }

    public Variante getVariante() {
        byte wert = this.entityData.get(VARIANTE);
        return wert == (byte) Variante.SKELETT.ordinal() ? Variante.SKELETT : Variante.ZOMBIE;
    }

    public void setVariante(Variante variante) {
        this.entityData.set(VARIANTE, (byte) variante.ordinal());
    }

    /**
     * Das Original wuerfelt die Gestalt in onSpawnWithEgg aus und ruestet dort aus. Der Port
     * tut beides in finalizeSpawn, dem Gegenstueck.
     */
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        SpawnGroupData ergebnis = super.finalizeSpawn(level, difficulty, type, data);
        this.populateDefaultEquipmentSlots(this.random, difficulty);
        this.setVariante(this.random.nextBoolean() ? Variante.ZOMBIE : Variante.SKELETT);
        return ergebnis;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(NtmItems.TAURUN_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(NtmItems.TAURUN_PLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(NtmItems.TAURUN_LEGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(NtmItems.TAURUN_BOOTS.get()));

        switch(random.nextInt(5)) {
        case 0: this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NtmItems.GUN_HEAVY_REVOLVER.get())); break;
        case 1: this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NtmItems.GUN_LIGHT_REVOLVER.get())); break;
        case 2: this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NtmItems.GUN_CARBINE.get())); break;
        case 3: this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NtmItems.GUN_MARESLEG.get())); break;
        default: this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NtmItems.GUN_GREASEGUN.get())); break;
        }
    }

    /**
     * Die ausgewuerfelte Gestalt haelt ueber das Neuladen hinweg. Im Original tut sie das
     * nicht: der DataWatcher wird nicht gespeichert, und ein nachgeladener Soldat ist wieder
     * ein Zombie, auch wenn er als Skelett aufgestellt wurde. Das ist kein Verhalten, das
     * jemand gewollt hat, sondern eine Luecke der alten Datenhaltung.
     */
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("variante", (byte) this.getVariante().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariante(tag.getByte("variante") == (byte) Variante.SKELETT.ordinal() ? Variante.SKELETT : Variante.ZOMBIE);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getVariante() == Variante.SKELETT ? SoundEvents.SKELETON_AMBIENT : SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.getVariante() == Variante.SKELETT ? SoundEvents.SKELETON_HURT : SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getVariante() == Variante.SKELETT ? SoundEvents.SKELETON_DEATH : SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.getVariante() == Variante.SKELETT ? SoundEvents.SKELETON_STEP : SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    /** Siehe Kopf: er laesst nichts fallen, auch nicht, was er traegt. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) { }
}
