package com.hbm.module.machine;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.util.BobMathUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.module.machine.ModuleMachinePlasma.
 *
 * Das Rezeptmodul der Plasmaschmiede: zwoelf Eingabefaecher, ein Ausgabefach, ein Tank.
 *
 * Die Besonderheit steckt in setupTanks: der Tank waechst mit dem Rezept mit. Die Basisklasse
 * passt nur die Fluessigkeitssorte an, die Schmiede verdoppelt zusaetzlich das Fassungsvermoegen,
 * sobald ein Rezept mehr als 8.000 mB je Durchgang braucht -- sonst passte etwa das Osmiridium
 * mit seinen 16.000 mB Reformgas nie hinein.
 */
public class ModuleMachinePlasma extends ModuleMachineBase {

    /** Das Fassungsvermoegen, unter das der Tank nie faellt. */
    public static final int MINIMUM_TANK_SIZE = 16_000;

    public ModuleMachinePlasma(int index, IEnergyHandlerMK2 battery, NonNullList<ItemStack> slots) {
        super(index, battery, slots);
        this.inputSlots = new int[12];
        this.outputSlots = new int[1];
        this.inputTanks = new FluidTank[1];
        this.outputTanks = new FluidTank[0];
    }

    @Override
    public GenericRecipes<?> getRecipeSet() {
        return PlasmaForgeRecipes.INSTANCE;
    }

    @Override
    public void setupTanks(GenericRecipe recipe) {
        super.setupTanks(recipe);
        if(recipe == null) return;

        for(int i = 0; i < this.inputTanks.length; i++) {
            if(recipe.inputFluid != null && recipe.inputFluid.length > i) {
                this.inputTanks[i].changeTankSize(BobMathUtil.max(
                        this.inputTanks[i].getFill(), recipe.inputFluid[i].fill * 2, MINIMUM_TANK_SIZE));
            }
        }
    }

    public ModuleMachinePlasma itemInput(int from) {
        for(int i = 0; i < this.inputSlots.length; i++) this.inputSlots[i] = from + i;
        return this;
    }

    public ModuleMachinePlasma itemOutput(int a) {
        this.outputSlots[0] = a;
        return this;
    }

    public ModuleMachinePlasma fluidInput(FluidTank a) {
        this.inputTanks[0] = a;
        return this;
    }
}
