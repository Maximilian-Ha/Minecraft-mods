package com.hbm.entity.mob.glyphid;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.effect.Mist;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.entity.projectile.Chemical;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import com.hbm.main.ResourceManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidBehemoth.
 *
 * Der Behemoth speit Saeure. Alle sechs Sekunden holt er Luft und speit dann sechs Sekunden
 * lang ununterbrochen -- in jedem dieser hundertzwanzig Takte eine Chemikalienwolke aus
 * Schwefelsaeure.
 *
 * WAEHREND ER SPEIT, STEHT ER FEST: er legt sich selbst Langsamkeit VI auf und haelt seine
 * Blickrichtung auf dem Wert des vorigen Takts. Das Original schreibt dafuer
 * rotationYaw = prevRotationYaw -- ein Strahl, der sich nicht mitdreht, ist ein Strahl, dem
 * man ausweichen kann.
 *
 * SEIN TOD IST EINE WOLKE: zehn Bloecke breit, vier hoch, sechs Sekunden Schwefelsaeure.
 * Wer ihn im Nahkampf erlegt, steht mittendrin.
 *
 * SEIN PANZER IST DER ZAEHESTE der Familie: Faktor 0,15 statt 0,6 beim gewoehnlichen
 * Glyphiden. Und sein Schlag dauert hundert Takte statt fuenfzehn -- er holt weit aus.
 */
public class GlyphidBehemoth extends Glyphid {

    /** Takte bis zum naechsten Atemzug. */
    public int timer = 120;
    /** Verbleibende Takte des laufenden Speiens. */
    private int speiZeit = 0;

    public GlyphidBehemoth(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().behemoth;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_BEHEMOTH_TEX;
    }

    @Override
    public double getGlyphidScale() {
        return 1.5D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().behemoth;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.getTarget() == null) {
            this.timer = 120;
            this.speiZeit = 0;
            return;
        }

        if(this.speiZeit > 0) {
            if(!this.swinging) this.swing(InteractionHand.MAIN_HAND);
            this.saeureAngriff();
            this.setYRot(this.yRotO);
            this.speiZeit--;
        } else if(--this.timer <= 0) {
            this.speiZeit = 120;
            this.timer = 120;
        }
    }

    /** Ein Takt Speien. */
    public void saeureAngriff() {

        if(this.level().isClientSide) return;

        Entity ziel = this.getTarget();
        if(!(ziel instanceof LivingEntity) || this.distanceTo(ziel) >= 20) return;

        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2 * 20, 6));

        /*
         * NICHT der Bauweg des Chemiewerfers: dessen Bauform setzt die Wolke an die Duese
         * und schiesst mit Geschwindigkeit 1. Das Original gibt dem Behemoth die Wolke ueber
         * EntityChemical(world, this, 0, 0, 0) -- und dieser Bauweg wirft die drei Versaetze
         * WEG und reicht nur an EntityThrowable(world, thrower) weiter, also Augenhoehe,
         * Geschwindigkeit 1,5 und Streuung 1. Genau das steht hier.
         */
        Chemical wolke = new Chemical(this.level());
        wolke.setOwner(this);
        wolke.moveTo(this.getX(), this.getEyeY(), this.getZ(), this.getYRot(), this.getXRot());
        wolke.setFluid(Fluids.SULFURIC_ACID);

        Vec3 blick = this.getLookAngle();
        wolke.shoot(blick.x, blick.y, blick.z, 1.5F, 1.0F);

        this.level().addFreshEntity(wolke);
    }

    @Override
    public void die(DamageSource quelle) {
        super.die(quelle);

        if(this.level().isClientSide) return;

        Mist wolke = new Mist(this.level());
        wolke.setFluidType(Fluids.SULFURIC_ACID);
        wolke.setPos(this.getX(), this.getY(), this.getZ());
        wolke.setArea(10F, 4F);
        wolke.setDuration(120);
        this.level().addFreshEntity(wolke);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource quelle, boolean kuerzlichGetroffen) {
        /* Die Druese faellt IMMER, nicht nur beim Tod durch Spielerhand -- das Original
         * legt sie vor den Aufruf der Oberklasse, ohne die byPlayer-Abfrage. */
        this.spawnAtLocation(MetaHelper.newStack(NtmItems.GLYPHID_GLAND.get(), 1, Fluids.SULFURIC_ACID.getID()));
        super.dropCustomDeathLoot(level, quelle, kuerzlichGetroffen);
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.15, 2), 100);
    }

    @Override
    public int schlagDauer() {
        return 100;
    }
}
