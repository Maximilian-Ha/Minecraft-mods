package com.hbm.items.weapon.sedna.impl;

import api.hbm.fluidmk2.IFillableItem;

import com.hbm.entity.projectile.Chemical;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineFluid;
import com.hbm.render.anim.AnimationEnums.GunAnimation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.impl.ItemGunChemthrower.
 *
 * Der Chemiewerfer. Er verschiesst keine Munition, sondern den Inhalt seines Tanks -- drei
 * Millibar je Zug --, und was dabei herauskommt, entscheidet das Fluid: ein Spritzer Saeure,
 * eine Stichflamme, eine Giftwolke.
 *
 * DER TANK NIMMT NUR EINE SORTE. Wer etwas anderes einfuellen will, muss ihn erst leeren --
 * acceptsFluid laesst eine neue Sorte nur bei leerem Tank zu.
 */
public class GunChemthrowerItem extends GunBaseNTItem implements IFillableItem {

    /** Verbrauch je Zug, in Millibar. */
    public static final int CONSUMPTION = 3;
    /** Wieviel je Aufruf hinein- oder herausgeht -- so tropft es statt zu springen. */
    public static final int TRANSFER_SPEED = 50;

    public GunChemthrowerItem(WeaponQuality quality, GunConfig... cfg) {
        super(quality, cfg);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return this.getFluidType(stack) == type || getMagCount(stack) == 0;
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {

        if(!this.acceptsFluid(type, stack)) return amount;
        if(getMagCount(stack) == 0) setMagType(stack, type.getID());

        int drin = getMagCount(stack);
        int platz = this.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getCapacity(stack) - drin;
        int rein = Math.min(Math.min(amount, platz), TRANSFER_SPEED);
        setMagCount(stack, drin + rein);

        return amount - rein;
    }

    public FluidType getFluidType(ItemStack stack) { return Fluids.fromID(getMagType(stack)); }

    @Override public boolean providesFluid(FluidType type, ItemStack stack) { return this.getFluidType(stack) == type; }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        int drin = getMagCount(stack);
        int raus = Math.min(Math.min(drin, amount), TRANSFER_SPEED);
        setMagCount(stack, drin - raus);
        return raus;
    }

    @Override public FluidType getFirstFluidType(ItemStack stack) { return Fluids.fromID(getMagType(stack)); }
    @Override public int getFill(ItemStack stack) { return getMagCount(stack); }

    public static int getMagType(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, MagazineFluid.KEY_MAG_TYPE + 0); }
    public static void setMagType(ItemStack stack, int value) { GunBaseNTItem.setValueInt(stack, MagazineFluid.KEY_MAG_TYPE + 0, value); }
    public static int getMagCount(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, MagazineFluid.KEY_MAG_COUNT + 0); }
    public static void setMagCount(ItemStack stack, int value) { GunBaseNTItem.setValueInt(stack, MagazineFluid.KEY_MAG_COUNT + 0, value); }

    public static BiFunction<ItemStack, LambdaContext, Boolean> LAMBDA_CAN_FIRE = (stack, ctx) ->
            ctx.config.getReceivers(stack)[0].getMagazine(stack).getAmount(stack, ctx.container) >= CONSUMPTION;

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_FIRE = (stack, ctx) -> {

        LivingEntity schuetze = ctx.entity;
        Player spieler = ctx.getPlayer();
        int index = ctx.configIndex;

        GunBaseNTItem.playAnimation(spieler, stack, GunAnimation.CYCLE, index);

        Receiver primary = ctx.config.getReceivers(stack)[0];
        IMagazine<?> mag = primary.getMagazine(stack);

        Vec3 versatz = primary.getProjectileOffset(stack);

        Chemical wolke = new Chemical(schuetze, versatz.z, versatz.y, versatz.x, primary.getInnateSpread(stack));
        wolke.setFluid((FluidType) mag.getType(stack, ctx.container));
        schuetze.level.addFreshEntity(wolke);

        mag.useUpAmmo(stack, ctx.container, CONSUMPTION);
        GunBaseNTItem.setWear(stack, index, Math.min(GunBaseNTItem.getWear(stack, index) + 1F, ctx.config.getDurability(stack)));
    };
}
