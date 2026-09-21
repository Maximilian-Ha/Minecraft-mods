package com.hbm.inventory;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class NtmFoods {

    public static final FoodProperties SMORE = new FoodProperties.Builder().nutrition(10).saturationModifier(20F).build();
    /* Portiert aus 1.7.10: ItemLemon(3, 0.5F, true). BERICHTIGT in Runde 298 -- hier standen
     * vorher fuenf Punkte ohne Saettigung, und weder das Original noch die CE-Abspaltung
     * geben das her: beide sagen drei Punkte und Faktor ein halb. */
    public static final FoodProperties GLYPHID_MEAT = new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build();
    /* Portiert aus 1.7.10: ItemLemon(8, 0.75F, true) mit Staerke II fuer neun Sekunden. */
    public static final FoodProperties GLYPHID_MEAT_GRILLED = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.75F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 180, 1), 1.0F)
            .build();
    /* Portiert aus 1.7.10: ItemLemon(4, 2F, false) -- vier Punkte, Saettigungsfaktor zwei. */
    public static final FoodProperties BIO_WAFER = new FoodProperties.Builder().nutrition(4).saturationModifier(2F).build();
    /* Portiert aus 1.7.10: ItemLemon(6, 1F, false). Der Pudding aus der Dosenkiste. */
    public static final FoodProperties PUDDING = new FoodProperties.Builder().nutrition(6).saturationModifier(1F).build();
    /* Portiert aus 1.7.10: ItemLemon(3, 0.5F, false) -- die Feldration aus der C-130. */
    public static final FoodProperties DEFINITELY_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build();
    /* Portiert aus 1.7.10: ItemPill(0) mit setAlwaysEdible. Eine Tablette saettigt nicht. */
    public static final FoodProperties PILL = new FoodProperties.Builder().nutrition(0).saturationModifier(0F).alwaysEdible().build();
    public static final FoodProperties CHOCOLATE = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0F)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 3), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 60 * 20, 3), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 60 * 20, 3), 1.0F)
            .build();
}
