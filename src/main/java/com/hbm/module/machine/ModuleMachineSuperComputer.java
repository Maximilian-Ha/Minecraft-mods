package com.hbm.module.machine;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.util.BobMathUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.module.machine.ModuleMachineSuperComputer.
 *
 * Drei Schaechte hinein, drei heraus, ein Tank je Richtung.
 *
 * DIE TANKS WACHSEN MIT DEM REZEPT. Das ist die Besonderheit dieses Moduls: die Kuehlmittel
 * gehen in Sechzehntausendern hinein, der Klaus-Lauf in Millionen. Ein fester Tank muesste
 * entweder fuer alles zu klein oder fuer alles zu gross sein, also richtet er sich nach dem
 * eingestellten Rezept -- mindestens viertausend, sonst das Doppelte dessen, was das Rezept
 * braucht, und nie weniger als das, was schon drinsteht.
 */
public class ModuleMachineSuperComputer extends ModuleMachineBase {

    public ModuleMachineSuperComputer(int index, IEnergyHandlerMK2 battery, NonNullList<ItemStack> slots) {
        super(index, battery, slots);
        this.inputSlots = new int[3];
        this.outputSlots = new int[3];
        this.inputTanks = new FluidTank[1];
        this.outputTanks = new FluidTank[1];
    }

    @Override
    public GenericRecipes<GenericRecipe> getRecipeSet() {
        return SuperComputerRecipes.INSTANCE;
    }

    @Override
    public void setupTanks(GenericRecipe recipe) {
        super.setupTanks(recipe);
        if(recipe == null) return;

        for(int i = 0; i < this.inputTanks.length; i++) {
            if(recipe.inputFluid != null && recipe.inputFluid.length > i) {
                this.inputTanks[i].changeTankSize(BobMathUtil.max(this.inputTanks[i].getFill(), recipe.inputFluid[i].fill * 2, 4_000));
            }
        }

        for(int i = 0; i < this.outputTanks.length; i++) {
            if(recipe.outputFluid != null && recipe.outputFluid.length > i) {
                this.outputTanks[i].changeTankSize(BobMathUtil.max(this.outputTanks[i].getFill(), recipe.outputFluid[i].fill * 2, 4_000));
            }
        }
    }

    public ModuleMachineSuperComputer itemInput(int from) { for(int i = 0; i < this.inputSlots.length; i++) this.inputSlots[i] = from + i; return this; }
    public ModuleMachineSuperComputer itemOutput(int from) { for(int i = 0; i < this.outputSlots.length; i++) this.outputSlots[i] = from + i; return this; }
    public ModuleMachineSuperComputer fluidInput(FluidTank a) { this.inputTanks[0] = a; return this; }
    public ModuleMachineSuperComputer fluidOutput(FluidTank a) { this.outputTanks[0] = a; return this; }
}
