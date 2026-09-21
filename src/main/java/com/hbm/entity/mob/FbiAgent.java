package com.hbm.entity.mob;

import com.hbm.config.NtmConfig;
import com.hbm.entity.ai.BreakingGoal;
import com.hbm.items.NtmItems;
import com.hbm.blocks.NtmBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityFBI.
 *
 * Der Beamte. Kommt in Gruppen von fuenfzehn, wenn die Razzia eingeschaltet ist, und hat es
 * nicht auf den Spieler abgesehen, sondern auf seine Anlage: er reisst Maschinen aus einer
 * Liste nieder und steckt alles in Brand, was am Boden liegt.
 *
 * ER SCHIESST NICHT, obwohl er einen Revolver oder eine Spas-12 traegt.
 * attackEntityWithRangedAttack ist im Original LEER -- der Rumpf der Methode enthaelt keine
 * einzige Zeile. Die Fernkampf-Aufgabe laeuft trotzdem: sie laesst ihn auf fuenfzehn Bloecke
 * herangehen, stehenbleiben und alle zwanzig bis fuenfundzwanzig Takte nichts tun. Der Port
 * uebernimmt das, wie es dasteht, samt der leeren Methode -- wer es aendert, aendert das
 * Spiel, nicht den Port. (Der Port hat mit FireGunGoal durchaus eine Aufgabe, die eine Waffe
 * abfeuert; sie hier einzusetzen waere eine Erfindung.)
 *
 * WAS IHN AUSZEICHNET:
 *   * Er verschwindet nie von selbst.
 *   * Er ist feuerfest und haelt zur Haelfte gegen Rueckstoss.
 *   * Seine Ruestung zaehlt fest zwanzig -- im Original "combat vest = full diamond set".
 *   * Kein Trank wirkt auf ihn. Und wenn er dabei keinen Kopfschutz traegt, setzt er sich
 *     zuerst die M65-Gasmaske auf: so steht es im Original in isPotionApplicable, an einer
 *     Stelle, an der man eine Nebenwirkung nicht erwartet.
 *   * Schaden von einem anderen Beamten prallt ab, wenn er ueber ein Geschoss kommt.
 *
 * NICHT UEBERNOMMEN: die Glasscheibe auf dem Kopf schuetzt im Original zusaetzlich gegen die
 * Schadensarten "oxygenSuffocation" und "thermal". Beide gibt es im Port nicht -- eine
 * Abfrage darauf waere eine Bedingung, die nie wahr wird.
 *
 * ABWEICHUNG: das Original faedelt seinen Weg mit einem eigenen Wegfinder
 * (PathFinderUtils.getPathEntityToEntityPartial), der auch Teilwege annimmt. Den gibt es im
 * Port nicht; hier laeuft die gewoehnliche Navigation auf das Ziel zu. Der Unterschied faellt
 * dort auf, wo kein vollstaendiger Weg existiert -- und genau dann greift die Grabe-Aufgabe.
 */
public class FbiAgent extends Monster implements RangedAttackMob {

    /** Die Bloecke, die er niederreisst. Unveraendert die Liste des Originals. */
    private static final Set<Block> ZIELE = new HashSet<>();

    public FbiAgent(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakingGoal(this));
        /* Die Aufgabe, die nichts ausrichtet -- siehe Kopf. Sie bleibt, weil sie sein
         * Verhalten bestimmt: er geht auf fuenfzehn Bloecke heran und bleibt stehen. */
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 20, 25, 15.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Leer, und das ist der Punkt: so steht sie im Original. */
    @Override
    public void performRangedAttack(LivingEntity ziel, float staerke) { }

    @Override
    public boolean removeWhenFarAway(double abstand) {
        return false;
    }

    /** Die Weste zaehlt wie ein volles Diamantset, ohne dass etwas davon im Ruestungsplatz steckt. */
    @Override
    public int getArmorValue() {
        return 20;
    }

    @Override
    public boolean hurt(DamageSource quelle, float menge) {

        /* Ein Geschoss eines anderen Beamten tut ihm nichts. Das Original prueft auf
         * EntityDamageSourceIndirect -- die Quelle hat also einen Verursacher UND ein
         * Geschoss, und beide sind verschieden. */
        if(quelle.getEntity() instanceof FbiAgent && quelle.getDirectEntity() != quelle.getEntity()) return false;

        return super.hurt(quelle, menge);
    }

    /**
     * Kein Trank wirkt. Die Nebenwirkung steht so im Original: wer keinen Kopfschutz traegt,
     * setzt sich in diesem Augenblick die Gasmaske auf.
     */
    @Override
    public boolean canBeAffected(MobEffectInstance wirkung) {

        if(this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(NtmItems.GAS_MASK_M65.get()));
        }

        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance schwierigkeit,
            MobSpawnType grund, @Nullable SpawnGroupData daten) {

        SpawnGroupData ergebnis = super.finalizeSpawn(level, schwierigkeit, grund, daten);
        this.populateDefaultEquipmentSlots(this.random, schwierigkeit);
        return ergebnis;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource zufall, DifficultyInstance schwierigkeit) {

        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(zufall.nextInt(2) == 0
                ? NtmItems.GUN_HEAVY_REVOLVER.get()
                : NtmItems.GUN_SPAS12.get()));

        /* Jeder fuenfte traegt die Sicherheitsruestung. */
        if(zufall.nextInt(5) == 0) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(NtmItems.SECURITY_HELMET.get()));
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(NtmItems.SECURITY_PLATE.get()));
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(NtmItems.SECURITY_LEGS.get()));
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(NtmItems.SECURITY_BOOTS.get()));
        }

        /* Ausserhalb der Oberwelt kommt der Raumanzug, und die Glasscheibe als Helm. */
        if(this.level().dimension() != Level.OVERWORLD) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.GLASS));
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(NtmItems.PAA_PLATE.get()));
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(NtmItems.PAA_LEGS.get()));
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(NtmItems.PAA_BOOTS.get()));
        }
    }

    /**
     * Das Original sucht sich in updateAITasks von sich aus ein Ziel, wenn es keines hat --
     * bis auf hundertachtundzwanzig Bloecke, quer durch Waende. Die Aufgabenliste allein
     * wuerde nur sehen, was in Sichtweite steht.
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if(this.getTarget() == null) {
            Player spieler = this.level().getNearestPlayer(this, 128.0D);
            if(spieler != null && !spieler.isCreative() && !spieler.isSpectator()) this.setTarget(spieler);
        }

        LivingEntity ziel = this.getTarget();
        if(ziel != null) this.getNavigation().moveTo(ziel, 1.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if(this.level().isClientSide || this.getHealth() <= 0) return;

        if(this.tickCount % NtmConfig.COMMON.RAID_ATTACK_DELAY.get() == 0) this.maschineNiederreissen();

        /* Alles, was in eineinhalb Bloecken Umkreis am Boden liegt, brennt. */
        double reichweite = 1.5D;
        List<ItemEntity> liegendes = this.level().getEntitiesOfClass(ItemEntity.class,
                new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
                        .inflate(reichweite, reichweite, reichweite));

        for(ItemEntity gegenstand : liegendes) gegenstand.setRemainingFireTicks(10 * 20);
    }

    /**
     * Ein Strahl in eine zufaellige Richtung, waagerecht, so weit wie die Einstellung erlaubt.
     * Trifft er eine Maschine aus der Liste, ist sie weg.
     */
    private void maschineNiederreissen() {

        double reichweite = NtmConfig.COMMON.RAID_ATTACK_REACH.get();
        float winkel = (float) (Math.PI * 2) * this.random.nextFloat();

        Vec3 richtung = new Vec3(-Math.sin(winkel) * reichweite, 0, Math.cos(winkel) * reichweite);
        Vec3 start = new Vec3(this.getX(), this.getY() + 0.5 + this.random.nextFloat(), this.getZ());

        BlockHitResult treffer = this.level().clip(new ClipContext(
                start, start.add(richtung), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if(treffer.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = treffer.getBlockPos();

        if(ziele().contains(this.level().getBlockState(pos).getBlock())) {
            this.level().destroyBlock(pos, false);
        }
    }

    /**
     * Fuellt die Liste beim ersten Gebrauch. Erst dann stehen die Bloecke der Mod sicher in
     * der Registrierung -- dieselbe Vorsicht wie in ContaminationUtil.isRadImmune.
     */
    private static Set<Block> ziele() {

        if(!ZIELE.isEmpty()) return ZIELE;

        ZIELE.add(Blocks.OAK_DOOR);
        ZIELE.add(Blocks.IRON_DOOR);
        ZIELE.add(Blocks.OAK_TRAPDOOR);
        ZIELE.add(Blocks.CHEST);
        ZIELE.add(Blocks.TRAPPED_CHEST);
        ZIELE.add(NtmBlocks.MACHINE_PRESS.get());
        ZIELE.add(NtmBlocks.MACHINE_EPRESS.get());
        ZIELE.add(NtmBlocks.MACHINE_CHEMICAL_PLANT.get());
        ZIELE.add(NtmBlocks.MACHINE_CHEMICAL_FACTORY.get());
        ZIELE.add(NtmBlocks.MACHINE_CRYSTALLIZER.get());
        ZIELE.add(NtmBlocks.MACHINE_TURBINE.get());
        ZIELE.add(NtmBlocks.MACHINE_INDUSTRIAL_TURBINE.get());
        ZIELE.add(NtmBlocks.MACHINE_CHUNGUS.get());
        ZIELE.add(NtmBlocks.MACHINE_PUREX.get());
        ZIELE.add(NtmBlocks.CRATE_IRON.get());
        ZIELE.add(NtmBlocks.CRATE_STEEL.get());
        ZIELE.add(NtmBlocks.MACHINE_DIESEL.get());
        ZIELE.add(NtmBlocks.MACHINE_RTG.get());
        ZIELE.add(NtmBlocks.MACHINE_CYCLOTRON.get());

        return ZIELE;
    }
}
