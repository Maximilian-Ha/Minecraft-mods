package com.hbm.entity.mob.glyphid;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.effect.Mist;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import com.hbm.main.ResourceManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidBrenda.
 *
 * Brenda ist die Koenigin: doppelt so gross wie ein gewoehnlicher Glyphid, feuerfest, und
 * sie stirbt nicht allein. Ihr Tod setzt eine Pheromonwolke frei -- vierzehn Bloecke breit,
 * sechs hoch, vier Sekunden -- und laesst ZWOELF Glyphiden schluepfen, die sie in alle
 * Richtungen von sich stossen.
 *
 * WER SIE IM NAHKAMPF ERLEGT, STEHT IN DER MITTE. Die Wolke ruft, was in Hoerweite ist, und
 * die zwoelf Neuen sind schon da.
 *
 * MIT EINEM DRITTEL WAHRSCHEINLICHKEIT faellt ihre Pheromondruese -- anders als beim
 * Behemoth, dessen Saeuredruese immer faellt.
 *
 * IHR PANZER IST NOCH ZAEHER ALS DER DES BEHEMOTH: Faktor 0,12 statt 0,15.
 *
 * FEUERFEST: im Original setzt das ihr Bauweg (isImmuneToFire), auf 1.21 steht es am
 * EntityType -- siehe NtmEntityTypes.GLYPHID_BRENDA, Builder.fireImmune().
 */
public class GlyphidBrenda extends Glyphid {

    public GlyphidBrenda(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().brenda;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_BRENDA_TEX;
    }

    @Override
    public double getGlyphidScale() {
        return 2D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().brenda;
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.12, 2), 100);
    }

    @Override
    public void die(DamageSource quelle) {
        super.die(quelle);

        if(this.level().isClientSide) return;
        if(this.getHealth() > 0.0F) return;

        Mist wolke = new Mist(this.level());
        wolke.setFluidType(Fluids.PHEROMONE);
        wolke.setPos(this.getX(), this.getY(), this.getZ());
        wolke.setArea(14F, 6F);
        wolke.setDuration(80);
        this.level().addFreshEntity(wolke);

        for(int i = 0; i < 12; i++) {
            Glyphid brut = new Glyphid(NtmEntityTypes.GLYPHID.get(), this.level());
            brut.moveTo(this.getX(), this.getY() + 0.5D, this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
            this.level().addFreshEntity(brut);
            /* Das Original stoesst sie mit moveEntity auseinander -- hier derselbe Stoss als
             * Bewegung, denn moveEntity gibt es auf 1.21 nicht mehr. */
            brut.setDeltaMovement(this.random.nextGaussian(), 0, this.random.nextGaussian());
            brut.hasImpulse = true;
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource quelle, boolean kuerzlichGetroffen) {
        super.dropCustomDeathLoot(level, quelle, kuerzlichGetroffen);
        if(this.random.nextInt(3) == 0) {
            this.spawnAtLocation(MetaHelper.newStack(NtmItems.GLYPHID_GLAND.get(), 1, Fluids.PHEROMONE.getID()));
        }
    }
}
