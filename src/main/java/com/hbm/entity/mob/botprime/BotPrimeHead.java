package com.hbm.entity.mob.botprime;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmCriteria;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.botprime.EntityBOTPrimeHead.
 *
 * Der Kopf. Er ist das einzige Teil mit Lebensenergie, das einzige, das ein Ziel sucht, und
 * das einzige, dessen Tod den Erfolg gibt.
 *
 * BEIM ERSCHEINEN LEGT ER SEINE KETTE AN: vierundsiebzig Glieder auf einen Schlag, alle mit
 * seiner Kennung und durchnummeriert. Zusammen mit ihm sind das fuenfundsiebzig Teile.
 *
 * ER HEILT, SOLANGE MAN IHN IN RUHE LAESST: alle sechs Ticks einen Punkt, wenn er ein Ziel
 * hat -- und VIER, wenn ihn laengere Zeit niemand getroffen hat.
 *
 * SEIN WEG. Ohne Ziel kreist er um seinen Erscheinungsort, hundert Bloecke breit und sechzig
 * hoch. Mit Ziel taucht er erst auf zehn Bloecke Hoehe herunter und geht dann auf das Ziel zu;
 * einmal unter fuenfzehn Bloecken gilt er als "war am Boden" und darf von da an gerade
 * ansteuern. Mit einer Wahrscheinlichkeit von eins zu achtzig vergisst er das wieder.
 */
public class BotPrimeHead extends BotPrimeBase {

    /** Wie viele Glieder die Kette hat. */
    public static final int GLIEDER = 74;

    /** Wie weit er schiesst. */
    private static final double ANGRIFFSWEITE = 150.0D;

    private BlockPos ursprung = BlockPos.ZERO;
    private boolean warAmBoden = false;
    private int kursZaehler = 0;
    private int zielZaehler = 0;
    private int schadensPause = 0;

    /** Wie hoch er von selbst steigt -- im Original surfaceY. */
    private static final int OBERFLAECHE = 60;

    private static final double HOECHSTTEMPO = 1.0D;
    private static final double SINKEN = 0.006D;

    public BotPrimeHead(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 1000;
        this.noCulling = true;
    }

    public BotPrimeHead(Level level) {
        this(NtmEntityTypes.BOT_PRIME_HEAD.get(), level);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public boolean istKopf() {
        return true;
    }

    @Override
    public float getAngriffsstaerke(Entity ziel) {
        return 1000F;
    }

    /**
     * Wenn er in die Welt kommt, legt er seine Kette an. Das Original haengt das an
     * onSpawnWithEgg; auf 1.21 ist finalizeSpawn die Stelle, durch die jedes Erscheinen geht --
     * ob per Ei, per Befehl oder aus einem Erzeuger.
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance schwierigkeit,
            MobSpawnType grund, SpawnGroupData daten) {

        this.ketteAnlegen();
        return super.finalizeSpawn(level, schwierigkeit, grund, daten);
    }

    /** Legt die Kette an. Das Original tut das in onSpawnWithEgg. */
    public void ketteAnlegen() {

        if(this.level().isClientSide) return;

        this.setKopfKennung(this.getId());
        this.ursprung = this.blockPosition();

        for(int i = 0; i < GLIEDER; i++) {
            BotPrimeBody glied = new BotPrimeBody(NtmEntityTypes.BOT_PRIME_BODY.get(), this.level());
            glied.setTeilNummer(i);
            glied.setKopfKennung(this.getId());
            glied.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(glied);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.wurmTick();

        if(this.level().isClientSide) return;

        this.kopfBewegen();
        this.heilen();
        this.schiessen();
    }

    /** Die Heilung: mit Ziel ein Punkt, ohne frischen Treffer vier. */
    private void heilen() {

        if(this.getHealth() >= this.getMaxHealth()) return;
        if(this.tickCount % 6 != 0) return;

        if(this.gefolgt != null) {
            this.heal(1.0F);
        } else if(this.getLastHurtByMob() == null) {
            this.heal(4.0F);
        }
    }

    /** Alle dreissig Ticks eine Salve, solange das Ziel in Sicht und in Reichweite ist. */
    private void schiessen() {

        LivingEntity ziel = this.getTarget();

        if(ziel == null || this.distanceToSqr(ziel) >= ANGRIFFSWEITE * ANGRIFFSWEITE || !this.siehtDurchNichtfeste(ziel)) {
            this.angriffsZaehler = 0;
            return;
        }

        this.angriffsZaehler++;

        if(this.angriffsZaehler >= 30) {
            this.laserAngriff(ziel, true);
            this.angriffsZaehler = 0;
        }
    }

    /**
     * Der Flug. Er beschleunigt in Richtung Wegpunkt, solange er unter Hoechsttempo ist, und
     * sinkt, wo kein Block ihn traegt.
     */
    private void kopfBewegen() {

        double dx = this.wegpunktX - this.getX();
        double dy = this.wegpunktY - this.getY();
        double dz = this.wegpunktZ - this.getZ();
        double weite = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if(--this.kursZaehler <= 0) {

            this.kursZaehler += this.random.nextInt(5) + 2;

            if(this.getDeltaMovement().lengthSqr() < HOECHSTTEMPO && weite > 1.0E-4D) {

                /* Steckt er nicht im Boden, macht das Original den Weg achtfach lang -- er
                 * beschleunigt dann also viel traeger. */
                double strecke = this.isInWall() ? weite : weite * 8.0D;
                double tempo = this.getAttributeValue(Attributes.MOVEMENT_SPEED);

                this.setDeltaMovement(this.getDeltaMovement().add(
                        dx / strecke * tempo, dy / strecke * tempo, dz / strecke * tempo));
            }
        }

        if(!this.isInWall()) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0, SINKEN, 0));
        }

        if(this.schadensPause > 0) this.schadensPause--;
        this.zielZaehler--;

        LivingEntity ziel = this.getTarget();

        if(ziel != null) {
            if(this.zielZaehler <= 0) {
                this.gefolgt = ziel;
                this.zielZaehler = 20;
            }
        } else if(this.gefolgt == null) {
            this.wegpunktX = this.ursprung.getX() - 50 + this.random.nextInt(100);
            this.wegpunktY = this.ursprung.getY() - 30 + this.random.nextInt(60);
            this.wegpunktZ = this.ursprung.getZ() - 50 + this.random.nextInt(100);
        }

        this.blickAusBewegung();

        if(this.gefolgt == null) return;
        if(this.gefolgt.distanceToSqr(this) >= ANGRIFFSWEITE * ANGRIFFSWEITE) return;

        if(this.warAmBoden) {

            this.wegpunktX = this.gefolgt.getX();
            this.wegpunktY = this.gefolgt.getY();
            this.wegpunktZ = this.gefolgt.getZ();

            if(this.random.nextInt(80) == 0 && this.getY() > OBERFLAECHE && !this.isInWall()) {
                this.warAmBoden = false;
            }

        } else {

            this.wegpunktX = this.gefolgt.getX();
            this.wegpunktY = 10.0D;
            this.wegpunktZ = this.gefolgt.getZ();

            if(this.getY() < 15.0D) this.warAmBoden = true;
        }
    }

    /** Kopf und Neigung folgen der Flugrichtung, nicht dem Blick. */
    private void blickAusBewegung() {

        Vec3 bewegung = this.getDeltaMovement();
        float flach = (float) Math.sqrt(bewegung.x * bewegung.x + bewegung.z * bewegung.z);

        float gier = (float) (Math.atan2(bewegung.x, bewegung.z) * 180.0D / Math.PI);
        float neigung = (float) -(Math.atan2(bewegung.y, flach) * 180.0D / Math.PI);

        this.setYRot(gier);
        this.yRotO = gier;
        this.setXRot(neigung);
        this.xRotO = neigung;
    }

    @Override
    public void die(DamageSource quelle) {
        super.die(quelle);

        if(this.level().isClientSide) return;

        /* Zweihundert Bloecke im Umkreis: der Erfolg und eine Muenze fuer jeden. */
        List<Player> nahe = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(200));

        for(Player spieler : nahe) {
            if(spieler instanceof ServerPlayer server) NtmCriteria.marke(server, "boss_worm");
            spieler.getInventory().add(new ItemStack(NtmItems.COIN_WORM.get()));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpawnX", this.ursprung.getX());
        tag.putInt("SpawnY", this.ursprung.getY());
        tag.putInt("SpawnZ", this.ursprung.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ursprung = new BlockPos(tag.getInt("SpawnX"), tag.getInt("SpawnY"), tag.getInt("SpawnZ"));
    }
}
