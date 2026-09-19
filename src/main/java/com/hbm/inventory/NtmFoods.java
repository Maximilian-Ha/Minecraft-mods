package com.hbm.inventory;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class NtmFoods {

    public static final FoodProperties SMORE = new FoodProperties.Builder().nutrition(10).saturationModifier(20F).build();
    public static final FoodProperties GLYPHID_MEAT = new FoodProperties.Builder().nutrition(5).saturationModifier(0F).build();
    /* Portiert aus 1.7.10: ItemLemon(4, 2F, false) -- vier Punkte, Saettigungsfaktor zwei. */
    public static final FoodProperties BIO_WAFER = new FoodProperties.Builder().nutrition(4).saturationModifier(2F).build();
    /* Portiert aus 1.7.10: ItemLemon(6, 1F, false). Der Pudding aus der Dosenkiste. */
    public static final FoodProperties PUDDING = new FoodProperties.Builder().nutrition(6).saturationModifier(1F).build();
    public static final FoodProperties CHOCOLATE = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0F)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 3), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 60 * 20, 3), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 60 * 20, 3), 1.0F)
            .build();
}
