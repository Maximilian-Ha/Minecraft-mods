package com.hbm.items.weapon.sedna.impl;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.fluidmk2.IFillableItem;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineElectricEngine;
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
 * ER HAT ZWEI HAELFTEN, und welche gilt, entscheidet der Motoraufsatz: steckt ein
 * Verbrennungsmotor darin, ist sein Magazin ein MagazineLiquidEngine und er nimmt Kraftstoff
 * (IFillableItem); steckt der Elektromotor darin, ist es ein MagazineElectricEngine und er
 * nimmt Strom (IBatteryItem). Beide Haelften fragen dasselbe Magazin ab und geben null
 * beziehungsweise 0 zurueck, wenn gerade die andere Sorte steckt -- so stoeren sie einander
 * nicht.
 *
 * Die Batteriehaelfte kam mit Runde 191, zusammen mit dem Aufsatz, der sie ueberhaupt
 * erreichbar macht.
 */
public class GunDrillItem extends GunBaseNTItem implements IFillableItem, IBatteryItem {

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

    /** Der Akku dieses Bohrers, oder null, wenn dort ein Verbrennungsmotor steckt. */
    private MagazineElectricEngine getAkku(ItemStack stack) {
        IMagazine<?> mag = this.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        return mag instanceof MagazineElectricEngine akku ? akku : null;
    }

    @Override
    public void chargeBattery(ItemStack stack, long i) {
        MagazineElectricEngine akku = this.getAkku(stack);
        if(akku == null) return;
        akku.setAmount(stack, (int) Math.min(akku.capacity, akku.getAmount(stack, null) + i));
    }

    @Override
    public void setCharge(ItemStack stack, long i) {
        MagazineElectricEngine akku = this.getAkku(stack);
        if(akku == null) return;
        akku.setAmount(stack, (int) i);
    }

    @Override
    public void dischargeBattery(ItemStack stack, long i) {
        MagazineElectricEngine akku = this.getAkku(stack);
        if(akku == null) return;
        akku.setAmount(stack, (int) Math.max(0, akku.getAmount(stack, null) - i));
    }

    @Override
    public long getCharge(ItemStack stack) {
        MagazineElectricEngine akku = this.getAkku(stack);
        return akku != null ? akku.getAmount(stack, null) : 0;
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        MagazineElectricEngine akku = this.getAkku(stack);
        return akku != null ? akku.getCapacity(stack) : 0;
    }

    @Override public long getChargeRate(ItemStack stack) { return 50_000; }
    /** Der Bohrer gibt nichts ab: er ist Verbraucher, kein Akku zum Weiterreichen. */
    @Override public long getDischargeRate(ItemStack stack) { return 0; }
}
