package com.hbm.items.weapon.sedna.impl;

import api.hbm.fluidmk2.IFillableItem;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineLiquidEngine;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.impl.ItemGunDrill.
 *
 * Der Bergbaubohrer. Nach aussen eine Waffe wie jede andere, nach innen ein Geraet mit
 * Kraftstofftank: sein Magazin ist ein MagazineLiquidEngine, und befuellt wird es nicht mit
 * Patronen, sondern an der Zapfsaeule -- dafuer ist der Gegenstand ein IFillableItem.
 *
 * ER BRICHT ALLES. isCorrectToolForDrops gibt immer wahr zurueck. Das ist im Original die
 * Zeile canHarvestBlock -> true, mit derselben Begruendung: die meisten Bloecke dieser
 * Erweiterung tragen gar keine Abbaustufe, und ohne diese Zeile wuerde der Bohrer an ihnen
 * nichts herausbekommen.
 *
 * NICHT UEBERNOMMEN: die Batteriehaelfte. Im Original ist der Bohrer zugleich ein
 * IBatteryItem, weil ein Aufsatz (ENGINE_ELECTRIC) den Verbrennungsmotor gegen einen
 * Elektromotor tauscht. Diesen Aufsatz gibt es im Port nicht, und MagazineElectricEngine
 * ebenso wenig -- die Haelfte waere Code ohne Wirkung.
 */
public class GunDrillItem extends GunBaseNTItem implements IFillableItem {

    public GunDrillItem(WeaponQuality quality, GunConfig... cfg) {
        super(quality, cfg);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    /** Der Tank dieses Bohrers, oder null, wenn dort etwas anderes steckt. */
    private MagazineLiquidEngine getTank(ItemStack stack) {
        IMagazine<?> mag = this.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        return mag instanceof MagazineLiquidEngine tank ? tank : null;
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        MagazineLiquidEngine tank = this.getTank(stack);
        if(tank == null) return false;
        for(FluidType erlaubt : tank.acceptedTypes) if(type == erlaubt) return true;
        return false;
    }

    /**
     * Betankt den Bohrer und gibt zurueck, was nicht hineingepasst hat. Hoechstens 50 Millibar
     * je Aufruf -- so tropft es hinein statt auf einen Schlag, wie im Original.
     */
    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        MagazineLiquidEngine tank = this.getTank(stack);
        if(tank == null) return amount;

        int rein = Math.min(amount, 50);
        rein = Math.min(rein, tank.getCapacity(stack) - this.getFill(stack));
        tank.setAmount(stack, this.getFill(stack) + rein);
        return amount - rein;
    }

    @Override public boolean providesFluid(FluidType type, ItemStack stack) { return false; }
    @Override public int tryEmpty(FluidType type, int amount, ItemStack stack) { return amount; }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        MagazineLiquidEngine tank = this.getTank(stack);
        return tank != null ? tank.getType(stack, null) : Fluids.NONE;
    }

    @Override
    public int getFill(ItemStack stack) {
        MagazineLiquidEngine tank = this.getTank(stack);
        return tank != null ? tank.getAmount(stack, null) : 0;
    }
}
