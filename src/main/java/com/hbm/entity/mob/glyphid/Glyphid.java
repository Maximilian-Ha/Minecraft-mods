package com.hbm.entity.mob.glyphid;

import com.hbm.config.NtmConfig;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.ai.ConditionalStrollGoal;
import com.hbm.entity.logic.Waypoint;
import com.hbm.entity.mob.ParasiteMaggot;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorGlyphidDig;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.items.NtmItems;
import com.hbm.main.ResourceManager;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmDamageTypes;

import api.hbm.entity.IResistanceProvider;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphid.
 *
 * Der Glyphid, die Grundform eines ganzen Volkes. Er ist kein gewoehnliches Untier: er hat
 * ein Zuhause, eine Aufgabe, eine Panzerung aus fuenf Stuecken und einen Draht zu seinen
 * Artgenossen.
 *
 * DIE PANZERUNG IST EIN BITMUSTER: fuenf Bits, fuenf Stuecke. Jeder Treffer kann eines
 * absprengen -- die Wahrscheinlichkeit dafuer ist (Schaden mal 0,6) zum Quadrat in Prozent,
 * bei zehn Schaden also sechsunddreissig. Was noch dran ist, zaehlt fuer Schadensschwelle und
 * Widerstand: beide werden mit der Zahl der Stuecke geteilt durch fuenf gewichtet.
 *
 * WELCHER SCHADEN DURCHKOMMT, ist fein abgestuft und Zahl fuer Zahl aus dem Original:
 *   Atomsprengungen   ein Viertel Schwelle, kein Widerstand -- sie zerreissen ihn
 *   Laser             halbe Schwelle, halber Widerstand
 *   Strom             ein Viertel Schwelle, ein Viertel Widerstand
 *   Teilchen          keine Schwelle, ein Zehntel Widerstand
 *   Feuer             keine Schwelle, ein Fuenftel Widerstand
 *   Sprengungen       halbe Schwelle, gut ein Drittel Widerstand
 *   alles andere      volle Schwelle, voller Widerstand
 *
 * ER GREIFT SEINESGLEICHEN NICHT AN, und Saeure eines anderen Glyphiden prallt ganz ab.
 *
 * DREI UNTERARTEN: gewoehnlich, verseucht, verstrahlt. Der verseuchte vergiftet, wen er
 * schlaegt, und laesst beim Sterben zwei bis vier Maden frei. Der verstrahlte ist doppelt so
 * schnell und schlaegt fuenfmal so hart.
 *
 * DAS AUFGABENSYSTEM ist der Kern: ein Glyphid traegt eine Aufgabe und einen Merkpunkt, und
 * beides gibt er per communicate() an alle Artgenossen im Umkreis weiter. So entsteht aus
 * einzelnen Untieren ein Zug.
 *
 * ABWEICHUNGEN, und jede hat einen Grund:
 *   * Der eigene Wegfinder des Originals (PathFinderUtils, der Teilwege annimmt) fehlt im
 *     Port; hier laeuft die gewoehnliche Navigation. Der Unterschied faellt dort auf, wo kein
 *     vollstaendiger Weg existiert -- und genau dann greift das Graben.
 *   * Das Zerschlagen der Laternen beim Blindsein ist nicht portiert: den Block lantern gibt
 *     es im Port nicht. Was bleibt, ist die Flucht.
 *   * fleeingTick, mit dem das Original den Blinden wegtreibt, hat auf 1.21 kein Gegenstueck;
 *     der Port loescht stattdessen Ziel und Weg, wie es das Original auch tut.
 *   * Spaeher und Atomglyphid lehnen Merkpunkte ab. Das Original fragt dafuer im Merkpunkt
 *     mit instanceof nach den beiden Unterklassen; der Port fragt die Grundklasse
 *     (nimmtWegpunkteAn), damit der Merkpunkt seine Nachkommen nicht kennen muss.
 */
public class Glyphid extends Monster implements IResistanceProvider {

    /* Die Aufgaben. */
    /** Herumlaufen, sonst nichts. */
    public static final int TASK_IDLE = 0;
    /** Zum Merkpunkt laufen und dort Verstaerkung rufen. */
    public static final int TASK_RETREAT_FOR_REINFORCEMENTS = 1;
    /** Aufgabe der Spaeher: am Merkpunkt einen neuen Bau setzen. */
    public static final int TASK_BUILD_HIVE = 2;
    /** Merkpunkt am Zuhause setzen und sofort den Rueckzug antreten. */
    public static final int TASK_INITIATE_RETREAT = 3;
    /** Einfach zum Merkpunkt laufen. */
    public static final int TASK_FOLLOW = 4;
    /** Atomglyphiden sprengen sich sofort. */
    public static final int TASK_TERRAFORM = 5;
    /** Steht etwas im Weg, wird es weggegraben. */
    public static final int TASK_DIG = 6;

    /* Die Unterarten. */
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_INFECTED = 1;
    public static final int TYPE_RADIOACTIVE = 2;

    private static final EntityDataAccessor<Byte> WALL =
            SynchedEntityData.defineId(Glyphid.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> ARMOR =
            SynchedEntityData.defineId(Glyphid.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> SUBTYPE =
            SynchedEntityData.defineId(Glyphid.class, EntityDataSerializers.BYTE);

    public boolean hasHome = false;
    public BlockPos home = BlockPos.ZERO;

    protected int currentTask = TASK_IDLE;
    protected int previousTask = TASK_IDLE;
    @Nullable protected Waypoint previousWaypoint;
    @Nullable protected Waypoint taskWaypoint;
    protected boolean hasWaypoint = false;

    public BlockPos taskPos = BlockPos.ZERO;
    public boolean shouldDig;

    public Glyphid(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().grunt;

        /* DIE EINS IST WIRKLICH EINE EINS. Ein Spieler hat 0,1, ein Zombie 0,23 -- der
         * Glyphid ist damit um ein Vielfaches schneller als alles Gewohnte, und genau so
         * steht es im Original. Wer hier teilt, macht ein anderes Spiel daraus. */
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage())
                .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        /* Herumlaufen nur im Leerlauf -- das Original erlaubt updateWanderPath nur bei
         * TASK_IDLE. */
        this.goalSelector.addGoal(5, new ConditionalStrollGoal(this, 1.0D, wesen -> this.getCurrentTask() == TASK_IDLE));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WALL, (byte) 0);
        builder.define(ARMOR, (byte) 0b11111);
        builder.define(SUBTYPE, (byte) TYPE_NORMAL);
    }

    /** Die Haut. Jede Art bringt ihre eigene mit. */
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_TEX;
    }

    /**
     * Wie gross er im Vergleich zum gewoehnlichen Glyphiden ist. Im Original heisst das
     * getScale -- DIESER NAME IST AUF 1.21 BELEGT: LivingEntity.getScale() liefert ein
     * float, und ein double dazu ist kein Ueberschreiben, sondern ein Uebersetzungsfehler.
     */
    public double getGlyphidScale() {
        return 1.0D;
    }

    /** Die Werte seiner Art. */
    public StatBundle getStats() {
        return GlyphidStats.getStats().grunt;
    }

    /** Wie weit er beim Graben sprengt -- groessere Glyphiden reissen groessere Loecher. */
    public int blastSize() {
        return Math.min((int) (3 * this.getGlyphidScale()) / 2, 5);
    }

    /** Wie hart ein Block hoechstens sein darf, damit er ihn weggraebt. */
    public int blastResToDig() {
        return Math.min((int) (50 * (this.getGlyphidScale() * 2)), 150);
    }

    /** Ob er Merkpunkte annimmt. Spaeher und Atomglyphid tun das nicht. */
    public boolean nimmtWegpunkteAn() {
        return true;
    }

    /** Ob er ein Spaeher ist -- nur die bauen einen neuen Bau. */
    public boolean istSpaeher() {
        return false;
    }

    /** Ob aus dem verseuchten beim Sterben Maden kriechen. */
    public boolean doesInfectedSpawnMaggots() {
        return true;
    }

    public int getSubtype() {
        return this.entityData.get(SUBTYPE);
    }

    public void setSubtype(int subtype) {
        this.entityData.set(SUBTYPE, (byte) subtype);
        this.applySubtype();
    }

    /**
     * Der verstrahlte Glyphid ist doppelt so schnell und schlaegt fuenfmal so hart. Das
     * Original rechnet das in applyEntityAttributes, also EINMAL beim Erschaffen -- und liest
     * dort einen Wert, den es noch gar nicht gesetzt haben kann. Der Port setzt die Werte
     * dann, wenn die Unterart gesetzt wird, und trifft damit das, was gemeint war.
     */
    protected void applySubtype() {

        StatBundle stats = this.getStats();
        boolean verstrahlt = this.getSubtype() == TYPE_RADIOACTIVE;

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.health());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stats.speed() * (verstrahlt ? 2D : 1D));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.damage() * (verstrahlt ? 5D : 1D));

        this.setHealth(this.getMaxHealth());
    }

    /// PANZERUNG ///

    public int getGlyphidArmor() {
        int gesamt = 0;
        byte panzer = this.entityData.get(ARMOR);
        for(int i = 0; i < 5; i++) if((panzer & (1 << i)) != 0) gesamt++;
        return gesamt;
    }

    public byte getArmorBits() {
        return this.entityData.get(ARMOR);
    }

    /** Die Wahrscheinlichkeit, dass ein Stueck abspringt: (Schaden mal 0,6) zum Quadrat, in Prozent. */
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.6, 2), 100);
    }

    /** Sprengt ein zufaelliges noch vorhandenes Stueck ab. */
    public void breakOffArmor() {

        byte panzer = this.entityData.get(ARMOR);

        List<Integer> stellen = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        Collections.shuffle(stellen);

        for(int i : stellen) {

            byte bit = (byte) (1 << i);

            if((panzer & bit) > 0) {
                panzer &= ~bit;
                panzer = (byte) (panzer & 0b11111);
                this.entityData.set(ARMOR, panzer);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.0F, 1.25F);
                break;
            }
        }
    }

    @Override
    public float[] getCurrentDTDR(DamageSource schaden, float menge, float pierceDT, float pierce) {

        if(schaden.is(DamageTypeTags.BYPASSES_ARMOR) || schaden.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return new float[] { 0F, 0F };
        }

        StatBundle stats = this.getStats();
        float schwelle = stats.thresholdMultForArmor() * this.getGlyphidArmor() / 5F;

        if(schaden.is(NtmDamageTypes.NUCLEAR_BLAST)) return new float[] { schwelle * 0.25F, 0F };
        if(schaden.is(NtmDamageTypes.LASER)) return new float[] { schwelle * 0.5F, stats.resistanceMult() * 0.5F };
        if(schaden.is(NtmDamageTypes.ELECTRIC)) return new float[] { schwelle * 0.25F, stats.resistanceMult() * 0.25F };
        if(schaden.is(NtmDamageTypes.SUBATOMIC)) return new float[] { 0F, stats.resistanceMult() * 0.1F };

        if(schaden.is(DamageTypeTags.IS_FIRE)) return new float[] { 0F, stats.resistanceMult() * 0.2F };
        if(schaden.is(DamageTypeTags.IS_EXPLOSION)) return new float[] { schwelle * 0.5F, stats.resistanceMult() * 0.35F };

        return new float[] { schwelle, stats.resistanceMult() };
    }

    @Override
    public void onDamageDealt(DamageSource schaden, float menge) {
        if(this.isArmorBroken(menge)) this.breakOffArmor();
    }

    @Override
    public boolean hurt(DamageSource quelle, float menge) {

        /* Kein Glyphid tut einem anderen etwas -- weder mit den Kiefern noch mit Saeure. */
        if(quelle.getEntity() instanceof Glyphid) return false;
        if(quelle.is(NtmDamageTypes.ACID) && quelle.getDirectEntity() instanceof Glyphid) return false;

        return super.hurt(quelle, menge);
    }

    /// DER TAKT ///

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) return;

        if(!this.hasHome) {
            this.home = this.blockPosition();
            this.hasHome = true;
        }

        if(this.hasEffect(MobEffects.BLINDNESS)) this.onBlinded();

        if(this.getCurrentTask() == TASK_FOLLOW) {

            if(this.isAtDestination() && !this.hasWaypoint) this.setCurrentTask(TASK_IDLE, null);

        } else if(this.getCurrentTask() == TASK_DIG && this.tickCount % 20 == 0 && this.isAtDestination()) {

            this.swing(InteractionHand.MAIN_HAND);

            ExplosionVNT graben = new ExplosionVNT(this.level(),
                    this.taskPos.getX(), this.taskPos.getY() + 2, this.taskPos.getZ(), this.blastSize(), this);
            graben.setBlockAllocator(new BlockAllocatorGlyphidDig(this.blastResToDig()));
            graben.setBlockProcessor(new BlockProcessorStandard().setNoDrop());
            graben.setEntityProcessor(null);
            graben.setPlayerProcessor(null);
            graben.explode();

            this.setCurrentTask(this.previousTask, this.previousWaypoint);
        }

        this.setBesideClimbableBlock(this.horizontalCollision);

        if(this.tickCount % 100 == 0) this.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        /* Alle hundert Takte sucht er sich ein neues Ziel -- aber jeder dritte tut das nicht,
         * damit man eine Horde nicht mit Absicht hin und her locken kann. */
        if(this.getId() % 3 > 0 && (this.getId() + this.tickCount) % 100 == 0) {
            Player naechster = this.level().getNearestPlayer(this, this.useExtendedTargeting() ? 128D : 16D);
            if(naechster != null && !naechster.isCreative() && !naechster.isSpectator()) this.setTarget(naechster);
        }

        if(this.hasEffect(MobEffects.BLINDNESS)) return;
        if(!this.getNavigation().isDone()) return;
        if(this.getCurrentTask() == TASK_IDLE) return;
        if(this.isAtDestination()) return;

        if(this.taskWaypoint != null) {

            this.taskPos = this.taskWaypoint.blockPosition();

            if(this.taskWaypoint.highPriority) this.setTarget(null);
        }

        if(!this.hasWaypoint) return;

        if(this.canDig() && this.getGlyphidScale() >= 1 && this.getCurrentTask() != TASK_DIG) {

            BlockHitResult hindernis = this.findWaypointObstruction();

            if(hindernis != null) {
                this.digToWaypoint(hindernis);
                return;
            }
        }

        this.getNavigation().moveTo(this.taskPos.getX(), this.taskPos.getY(), this.taskPos.getZ(), 1.0D);
    }

    protected boolean canDig() {
        return NtmConfig.COMMON.RAMPANT_DIG.get();
    }

    /**
     * Blind wird er von der Blendung: er laesst von seinem Ziel ab und bleibt stehen. Das
     * Original treibt ihn zusaetzlich mit fleeingTick fort und laesst grosse Glyphiden nach
     * Laternen schlagen -- beides fehlt hier, siehe Kopf.
     */
    public void onBlinded() {
        this.setTarget(null);
        this.getNavigation().stop();
    }

    /** Ab wieviel Russ er ueber die ganze Karte sieht. */
    public boolean useExtendedTargeting() {
        if(NtmConfig.COMMON.RAMPANT_EXTENDED_TARGETING.get()) return true;
        return PollutionHandler.getPollution(this.level(), this.blockPosition(), PollutionType.SOOT)
                >= NtmConfig.COMMON.TARGETING_THRESHOLD.get();
    }

    @Override
    public boolean removeWhenFarAway(double abstand) {
        return this.getTarget() == null && this.getCurrentTask() == TASK_IDLE && this.tickCount > 100;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource quelle, boolean kuerzlichGetroffen) {
        super.dropCustomDeathLoot(level, quelle, kuerzlichGetroffen);

        if(this.random.nextInt(2) != 0) return;

        ItemStack fleisch = new ItemStack(this.isOnFire()
                ? NtmItems.GLYPHID_MEAT_GRILLED.get()
                : NtmItems.GLYPHID_MEAT.get(), (int) this.getGlyphidScale() * 2);

        if(!fleisch.isEmpty()) this.spawnAtLocation(fleisch);
    }

    @Override
    public void die(DamageSource quelle) {
        super.die(quelle);

        if(this.level().isClientSide) return;
        if(!this.doesInfectedSpawnMaggots()) return;
        if(this.getSubtype() != TYPE_INFECTED) return;

        int anzahl = 2 + this.random.nextInt(3);

        for(int i = 0; i < anzahl; i++) {

            float dx = ((float) (i % 2) - 0.5F) * 0.5F;
            float dz = ((float) (i / 2) - 0.5F) * 0.5F;

            ParasiteMaggot made = NtmEntityTypes.PARASITE_MAGGOT.get().create(this.level());
            if(made == null) continue;

            made.moveTo(this.getX() + dx, this.getY() + 0.5D, this.getZ() + dz, this.random.nextFloat() * 360.0F, 0.0F);
            made.setDeltaMovement(dx, 0, dz);

            this.level().addFreshEntity(made);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE,
                2.0F, 0.95F + this.random.nextFloat() * 0.2F);

        /* Die Fetzen gehen wie ueberall im Port ueber das AuxParticle-Paket, nicht ueber einen
         * eigenen Partikeltyp -- dieselbe Stelle wie beim Turbofan. */
        if(this.level() instanceof ServerLevel server) {

            CompoundTag tag = new CompoundTag();
            tag.putString("type", "giblets");
            tag.putInt("ent", this.getId());

            double px = this.getX();
            double py = this.getY() + this.getBbHeight() * 0.5;
            double pz = this.getZ();

            PacketDistributor.sendToPlayersNear(server, null, px, py, pz, 150, new AuxParticle(tag, px, py, pz));
        }
    }

    @Override
    public boolean doHurtTarget(Entity opfer) {

        if(this.swinging) return false;
        this.swing(InteractionHand.MAIN_HAND);

        if(this.getSubtype() == TYPE_INFECTED && opfer instanceof LivingEntity lebendes) {
            lebendes.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2));
            lebendes.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }

        return super.doHurtTarget(opfer);
    }

    /// KLETTERN ///

    public boolean isBesideClimbableBlock() {
        return (this.entityData.get(WALL) & 1) != 0;
    }

    public void setBesideClimbableBlock(boolean klettert) {
        byte wert = this.entityData.get(WALL);
        wert = klettert ? (byte) (wert | 1) : (byte) (wert & -2);
        this.entityData.set(WALL, wert);
    }

    @Override
    public boolean onClimbable() {
        return this.isBesideClimbableBlock();
    }

    @Override
    public void makeStuckInBlock(net.minecraft.world.level.block.state.BlockState state, Vec3 verzoegerung) {
        /* Spinnweben halten ihn nicht auf -- das Original ueberschreibt setInWeb leer. */
    }

    /// DAS AUFGABENSYSTEM ///

    public int getCurrentTask() {
        return this.currentTask;
    }

    @Nullable
    public Waypoint getWaypoint() {
        return this.taskWaypoint;
    }

    /** Setzt Aufgabe und Merkpunkt und fuehrt sofort aus, was einmalig zu tun ist. */
    public void setCurrentTask(int aufgabe, @Nullable Waypoint waypoint) {

        this.currentTask = aufgabe;
        this.taskWaypoint = waypoint;
        this.hasWaypoint = waypoint != null;

        if(waypoint != null) {

            this.taskPos = waypoint.blockPosition();

            if(waypoint.highPriority) {
                this.setTarget(null);
                this.getNavigation().stop();
            }
        }

        this.carryOutTask();
    }

    /** Was bei einer neuen Aufgabe einmalig geschieht. */
    public void carryOutTask() {

        switch(this.getCurrentTask()) {

        case TASK_RETREAT_FOR_REINFORCEMENTS:
            if(this.taskWaypoint != null) {
                this.communicate(TASK_FOLLOW, this.taskWaypoint);
                this.setCurrentTask(TASK_FOLLOW, this.taskWaypoint);
            }
            break;

        case TASK_INITIATE_RETREAT:
            if(!this.level().isClientSide && this.taskWaypoint == null) {

                /* Zuerst nach Hause, dann wieder hierher: zwei Merkpunkte, der zweite haengt
                 * am ersten. */
                Waypoint zurueck = new Waypoint(this.level());
                zurueck.moveTo(this.getX(), this.getY(), this.getZ(), 0, 0);

                Waypoint daheim = new Waypoint(this.level());
                daheim.setWaypointType(TASK_RETREAT_FOR_REINFORCEMENTS);
                daheim.setAdditionalWaypoint(zurueck);
                daheim.setHighPriority();
                daheim.moveTo(this.home.getX(), this.home.getY(), this.home.getZ(), 0, 0);
                this.level().addFreshEntity(daheim);

                this.taskWaypoint = daheim;
                this.communicate(TASK_FOLLOW, daheim);
                this.setCurrentTask(TASK_FOLLOW, this.taskWaypoint);
            }
            break;

        case TASK_DIG:
            this.shouldDig = true;
            break;

        default:
            break;
        }
    }

    /** Gibt Aufgabe und Merkpunkt an alle Artgenossen im Umkreis weiter. */
    public void communicate(int aufgabe, @Nullable Waypoint waypoint) {

        int radius = waypoint != null ? waypoint.radius : 4;
        AABB kasten = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
                .inflate(radius, radius, radius);

        for(Entity e : this.level().getEntities(this, kasten)) {

            if(!(e instanceof Glyphid artgenosse)) continue;
            if(!artgenosse.nimmtWegpunkteAn()) continue;

            if(artgenosse.getCurrentTask() != aufgabe) artgenosse.setCurrentTask(aufgabe, waypoint);
        }
    }

    /** Was jede Art tut, wenn der Bau wachsen soll. Die Grundform kann es nicht. */
    public boolean expandHive() {
        return false;
    }

    public boolean isAtDestination() {
        int radius = this.taskWaypoint != null ? (int) Math.pow(this.taskWaypoint.radius, 2) : 25;
        return this.distanceToSqr(this.taskPos.getX(), this.taskPos.getY(), this.taskPos.getZ()) <= radius;
    }

    /// DAS GRABEN ///

    /** Der erste Block zwischen ihm und dem Merkpunkt, den er wegsprengen kann. */
    @Nullable
    public BlockHitResult findWaypointObstruction() {

        Vec3 von = new Vec3(this.getX(), this.getY() + this.getEyeHeight(), this.getZ());
        Vec3 nach = new Vec3(this.taskPos.getX(), this.taskPos.getY(), this.taskPos.getZ());

        BlockHitResult treffer = this.level().clip(new ClipContext(
                von, nach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if(treffer.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = treffer.getBlockPos();

        if(this.level().getBlockState(pos).getBlock().getExplosionResistance() <= this.blastResToDig()) return treffer;

        return null;
    }

    /** Setzt einen Merkpunkt auf das Hindernis und gibt den Graubefehl weiter. */
    public void digToWaypoint(BlockHitResult hindernis) {

        Waypoint ziel = new Waypoint(this.level());
        ziel.moveTo(hindernis.getBlockPos().getX(), hindernis.getBlockPos().getY(), hindernis.getBlockPos().getZ(), 0, 0);
        ziel.radius = 5;
        this.level().addFreshEntity(ziel);

        this.previousTask = this.getCurrentTask();
        this.previousWaypoint = this.getWaypoint();

        this.setCurrentTask(TASK_DIG, ziel);

        this.getNavigation().moveTo(this.taskPos.getX(), this.taskPos.getY(), this.taskPos.getZ(), 1.0D);

        this.communicate(TASK_DIG, ziel);
    }

    /// SPEICHERN ///

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putByte("armor", this.entityData.get(ARMOR));
        tag.putByte("subtype", this.entityData.get(SUBTYPE));

        tag.putBoolean("hasHome", this.hasHome);
        tag.putInt("homeX", this.home.getX());
        tag.putInt("homeY", this.home.getY());
        tag.putInt("homeZ", this.home.getZ());

        tag.putBoolean("hasWaypoint", this.hasWaypoint);
        tag.putInt("taskX", this.taskPos.getX());
        tag.putInt("taskY", this.taskPos.getY());
        tag.putInt("taskZ", this.taskPos.getZ());

        tag.putInt("task", this.currentTask);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.entityData.set(ARMOR, tag.getByte("armor"));
        this.entityData.set(SUBTYPE, tag.getByte("subtype"));

        this.hasHome = tag.getBoolean("hasHome");
        this.home = new BlockPos(tag.getInt("homeX"), tag.getInt("homeY"), tag.getInt("homeZ"));

        this.hasWaypoint = tag.getBoolean("hasWaypoint");
        this.taskPos = new BlockPos(tag.getInt("taskX"), tag.getInt("taskY"), tag.getInt("taskZ"));

        this.currentTask = tag.getInt("task");
    }
}
