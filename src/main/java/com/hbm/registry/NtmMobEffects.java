package com.hbm.registry;

import com.hbm.main.NuclearTechMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Die eigenen Zustandswirkungen des Mods. Im Original ist das com.hbm.potion.HbmPotion, das
 * sich seine Kennungen per Reflexion ins Vanille-Feld potionTypes schreibt und seine Symbole
 * aus einem einzigen Blatt (textures/gui/potions.png) schneidet. Auf 1.21 ist MobEffect ein
 * gewoehnliches Register, und jedes Symbol ist eine eigene Datei.
 *
 * Bisher steht hier nur die Trankuebelkeit. Die uebrigen zehn des Originals -- Strahlung,
 * Verseuchung, Mutation und so weiter -- haengen an Teilsystemen, die der Port anders loest:
 * Strahlung etwa steht in HbmLivingAttachments und nicht als Zustandswirkung.
 */
public class NtmMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, NuclearTechMod.MODID);

    /**
     * Trankuebelkeit: solange sie anliegt, wirkt keine weitere Spritze. Sie haelt den Spieler
     * davon ab, sich mit einem Stapel Stimpaks unsterblich zu machen.
     *
     * ABWEICHUNG: das Original kennt nur "gut" und "boese" und traegt hier "gut" ein. Auf 1.21
     * gibt es dafuer NEUTRAL, und das trifft es besser -- die Uebelkeit hilft nicht, sie
     * schadet aber auch nicht, sie sperrt nur.
     *
     * Die Farbe 0xff8080 ist die des Originals. Das Symbol ist aus dessen Trankblatt
     * geschnitten (Feld x=3, y=1).
     */
    public static final Holder<MobEffect> POTION_SICKNESS = MOB_EFFECTS.register(
            "potion_sickness", () -> new NtmMobEffect(MobEffectCategory.NEUTRAL, 0xFF8080));

    /** Wie lange die Uebelkeit nach einer Spritze anliegt, in Sekunden. Werte aus dem Original. */
    public static void applyPotionSickness(LivingEntity entity, int sekunden) {
        entity.addEffect(new MobEffectInstance(POTION_SICKNESS, sekunden * 20, 0));
    }

    public static boolean hasPotionSickness(LivingEntity entity) {
        return entity.hasEffect(POTION_SICKNESS);
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    /** MobEffect hat einen geschuetzten Konstruktor; dafuer genuegt diese Huelle. */
    private static class NtmMobEffect extends MobEffect {
        private NtmMobEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
