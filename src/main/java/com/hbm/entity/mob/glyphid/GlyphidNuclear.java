package com.hbm.entity.mob.glyphid;

import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.logic.Waypoint;
import com.hbm.entity.mob.ParasiteMaggot;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorDebris;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.special.PolaroidItem;
import com.hbm.main.ResourceManager;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidNuclear.
 *
 * "Big Man Johnson" -- so heisst er im Original. Er greift nicht an, er wird gebracht: sein
 * Tod ist eine Kernexplosion vom Radius fuenfundzwanzig.
 *
 * FUENF SEKUNDEN PIEPSEN, dann der Knall. Ab dem Tod zaehlt deathTicks; alle zehn Takte
 * piepst es, bei neunzig bekommt jeder Glyphid im Umkreis von acht Bloecken Widerstand und
 * Feuerschutz -- seine eigenen ueberleben, was er anrichtet --, und bei hundert geht er hoch.
 *
 * WAS DABEI ZURUECKBLEIBT, haengt an seiner Unterart: der gewoehnliche und der
 * radioaktive reissen einen Krater und lassen vulkanische Lava stehen; der VERSEUCHTE
 * sprengt stattdessen fuenfzehn bis zwanzig Maden heraus und laesst die Bloecke in Ruhe.
 *
 * ER SPRICHT NUR MIT SPAEHERN. Die Grundform gibt ihre Aufgabe an jeden Artgenossen weiter,
 * der Merkpunkte annimmt; er nur an die, die spaehen. Beim Sterben schickt er ihnen als
 * erstes den Rueckzugsbefehl -- wer neben einer Bombe steht, soll laufen.
 *
 * DREI AUFGABEN BEHANDELT ER EIGEN: am Ziel angekommen geht er in den Leerlauf, beim Bauen
 * ohne Angreifer bekommt er Eile IV, und beim Umformen der Landschaft setzt er seine
 * Lebenspunkte auf null -- das ist das Umformen: er geht hoch.
 */
public class GlyphidNuclear extends Glyphid {

    /** Takte seit dem Tod. Das Original zaehlt sie selbst, weil es den Zuender braucht. */
    public int deathTicks;

    /** Ob der Rueckzugsbefehl schon raus ist. */
    private boolean rueckzugGesendet = false;

    public GlyphidNuclear(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().nuclear;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_NUCLEAR_TEX;
    }

    @Override
    public double getGlyphidScale() {
        return 2D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().nuclear;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) return;
        if(this.tickCount % 20 != 0) return;

        if(this.isAtDestination() && this.getCurrentTask() == TASK_FOLLOW) {
            this.setCurrentTask(TASK_IDLE, null);
        }

        if(this.getCurrentTask() == TASK_BUILD_HIVE && this.getLastHurtByMob() == null) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10 * 20, 3));
        }

        /* Umformen heisst bei ihm: hochgehen. */
        if(this.getCurrentTask() == TASK_TERRAFORM) {
            this.setHealth(0);
        }
    }

    /**
     * Anders als die Grundform spricht er NUR mit Spaehern -- das Original prueft dort
     * instanceof EntityGlyphidScout. Im Port steht dieselbe Frage als istSpaeher().
     */
    @Override
    public void communicate(int aufgabe, @Nullable Waypoint waypoint) {

        int radius = waypoint != null ? waypoint.radius : 4;
        AABB kasten = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
                .inflate(radius, radius, radius);

        for(Entity e : this.level().getEntities(this, kasten)) {

            if(!(e instanceof Glyphid artgenosse)) continue;
            if(!artgenosse.istSpaeher()) continue;

            if(artgenosse.getCurrentTask() != aufgabe) artgenosse.setCurrentTask(aufgabe, waypoint);
        }
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.12, 2), 100);
    }

    @Override
    public boolean doesInfectedSpawnMaggots() {
        return false;
    }

    @Override
    protected void tickDeath() {

        this.deathTicks++;

        if(!this.rueckzugGesendet) {
            this.communicate(TASK_INITIATE_RETREAT, null);
            this.rueckzugGesendet = true;
        }

        if(this.deathTicks == 90) this.artgenossenSchuetzen();

        if(this.deathTicks != 100) {
            if(!this.level().isClientSide && this.deathTicks % 10 == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        NtmSoundEvents.FSTBMB_PING.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
            }
            return;
        }

        if(!this.level().isClientSide) this.hochgehen();

        this.remove(RemovalReason.KILLED);
    }

    /**
     * Widerstand und Feuerschutz fuer alle Glyphiden im Umkreis von acht Bloecken.
     *
     * ACHTUNG, EIN FEHLER DES ORIGINALS, BEWUSST UEBERNOMMEN: es legt die beiden Wirkungen
     * auf SICH SELBST (addPotionEffect ohne Empfaenger), obwohl es die Nachbarn zaehlt --
     * die Schleife entscheidet also nur, WIE OFT er sich selbst staerkt, nicht wen er
     * schuetzt. Wer das geradezieht, aendert das Spiel: dann ueberlebt die ganze Schar
     * seinen Knall statt nur er selbst, der ohnehin stirbt.
     */
    private void artgenossenSchuetzen() {

        AABB kasten = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
                .inflate(8, 8, 8);

        for(Entity e : this.level().getEntities(this, kasten)) {
            if(!(e instanceof Glyphid)) continue;
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 6));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 15 * 20, 1));
        }
    }

    /** Der Knall. */
    private void hochgehen() {

        ExplosionVNT vnt = new ExplosionVNT(this.level(), this.getX(), this.getY(), this.getZ(), 25, this);

        if(this.getSubtype() == TYPE_INFECTED) {

            int anzahl = 15 + this.random.nextInt(6);

            for(int k = 0; k < anzahl; k++) {
                float vx = ((float) (k % 2) - 0.5F) * 0.5F;
                float vz = ((float) (k / 2) - 0.5F) * 0.5F;

                ParasiteMaggot made = new ParasiteMaggot(NtmEntityTypes.PARASITE_MAGGOT.get(), this.level());
                made.moveTo(this.getX() + vx, this.getY() + 0.5D, this.getZ() + vz, this.random.nextFloat() * 360.0F, 0.0F);
                made.setDeltaMovement(vx, 0, vz);
                made.hasImpulse = true;
                this.level().addFreshEntity(made);
            }

        } else {
            vnt.setBlockAllocator(new BlockAllocatorStandard(24));
            vnt.setBlockProcessor(new BlockProcessorStandard()
                    .withBlockEffect(new BlockMutatorDebris(NtmBlocks.VOLCANIC_LAVA.get()))
                    .setNoDrop());
        }

        vnt.setEntityProcessor(new EntityProcessorStandard());
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                NtmSoundEvents.MUKE_EXPLOSION.get(), SoundSource.HOSTILE, 15.0F, 1.0F);

        if(!(this.level() instanceof ServerLevel serverLevel)) return;

        CompoundTag tag = new CompoundTag();
        tag.putString("type", "muke");
        tag.putBoolean("balefire", PolaroidItem.polaroidID == 11 || this.random.nextInt(100) == 0);
        PacketDistributor.sendToPlayersNear(serverLevel, null, this.getX(), this.getY() + 0.5, this.getZ(), 250,
                new AuxParticle(tag, this.getX(), this.getY() + 0.5, this.getZ()));
    }
}
