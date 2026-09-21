package com.hbm.entity.mob.glyphid;

import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.main.ResourceManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidBlaster.
 *
 * Der Blaster ist der grosse Bombardier: dieselbe Rechnung, dieselbe Bahn, aber zehn
 * Bomben statt fuenf, fuenfzehn Schaden statt fuenf, halbe Streuung und ein Viertel mehr
 * Anfangsgeschwindigkeit. Eine Salve von ihm deckt eine Flaeche ab, und wer darin steht,
 * bekommt mehr als einen Klecks ab.
 *
 * SEIN PANZER SPRINGT SCHWERER AB als der des gewoehnlichen Glyphiden: 0,25 statt 0,6 als
 * Faktor im Quadrat, also braucht es etwa den zweieinhalbfachen Schaden fuer dieselbe
 * Wahrscheinlichkeit. Denselben Wert traegt der Brawler.
 */
public class GlyphidBlaster extends GlyphidBombardier {

    public GlyphidBlaster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().blaster;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_BLASTER_TEX;
    }

    @Override
    public double getScale() {
        return 1.25D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().blaster;
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.25, 2), 100);
    }

    @Override
    public float getBombDamage() {
        return 15F;
    }

    @Override
    public int getBombCount() {
        return 10;
    }

    @Override
    public float getSpreadMult() {
        return 0.5F;
    }

    @Override
    public double getV0() {
        return 1.25D;
    }
}
