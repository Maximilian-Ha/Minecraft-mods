package com.hbm.module.machine;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.FusionRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.module.machine.ModuleMachineFusion.
 *
 * Der Fusionstorus ist die einzige Maschine des Mods, die ihren Brennstoff schon waehrend des
 * Laufens verbraucht und nicht erst beim Fertigwerden. Und sie laeuft nicht einfach an oder nicht:
 * ihr Tempo haengt davon ab, wie voll die Tanks und der Stromspeicher sind.
 *
 * Dazu kommt der Bonus. Jeder angeschlossene Kollektor beschleunigt die Ausbeute um die Haelfte;
 * was sich dabei ansammelt, faellt als zusaetzliches Erzeugnis an.
 */
public class ModuleMachineFusion extends ModuleMachineBase {

    /** Wie schnell gerade gearbeitet wird -- aus Strom- und Tankfuellstand. */
    public double processSpeed = 1D;
    /** Wie viel Bonus je Arbeitsschritt anfaellt -- eine halbe Einheit je Kollektor. */
    public double bonusSpeed = 0D;
    /** Der angesammelte Bonus. Bei eins faellt ein zusaetzliches Erzeugnis an. */
    public double bonus;

    public ModuleMachineFusion(int index, IEnergyHandlerMK2 battery, NonNullList<ItemStack> slots) {
        super(index, battery, slots);
        this.inputSlots = new int[0];
        this.outputSlots = new int[1];
        this.inputTanks = new FluidTank[3];
        this.outputTanks = new FluidTank[1];
    }

    @Override
    public GenericRecipes<?> getRecipeSet() {
        return FusionRecipes.INSTANCE;
    }

    public ModuleMachineFusion itemOutput(int slot) { this.outputSlots[0] = slot; return this; }

    public ModuleMachineFusion fluidInput(FluidTank a, FluidTank b, FluidTank c) {
        this.inputTanks[0] = a;
        this.inputTanks[1] = b;
        this.inputTanks[2] = c;
        return this;
    }

    public ModuleMachineFusion fluidOutput(FluidTank a) { this.outputTanks[0] = a; return this; }

    /** Muss vor update() laufen: was die Basisklasse nicht kennt, kommt hier herein. */
    public void preUpdate(double processSpeed, double bonusSpeed) {
        this.processSpeed = processSpeed;
        this.bonusSpeed = bonusSpeed;
    }

    /**
     * Anders als sonst reicht hier ein ANGEFANGENER Tank: steht noch etwas darin, aber weniger als
     * ein voller Schritt braucht, laeuft die Anlage trotzdem weiter -- sie wird nur langsamer.
     * Erst ein voellig leerer Tank stoppt sie.
     */
    @Override
    protected boolean hasInput(GenericRecipe recipe) {

        if(this.processSpeed <= 0) return false;

        if(recipe.inputFluid != null) {
            for(int i = 0; i < Math.min(recipe.inputFluid.length, this.inputTanks.length); i++) {
                if(this.inputTanks[i].getFill() > 0
                        && this.inputTanks[i].getFill() < (int) Math.ceil(recipe.inputFluid[i].fill * this.processSpeed)) return false;
            }
        }

        return true;
    }

    @Override
    public void process(GenericRecipe recipe, double speed, double power) {

        this.battery.setPower(this.battery.getPower()
                - (long) Math.ceil((power == 1 ? recipe.power : (long) (recipe.power * power)) * this.processSpeed));

        double step = Math.min(speed / recipe.duration * this.processSpeed, 1D);
        this.progress += step;
        this.bonus += step * this.bonusSpeed;
        /* Der Bonus wird nicht immer sofort abgerufen; ein halber Puffer darueber ist erlaubt. */
        this.bonus = Math.min(this.bonus, 1.5D);

        /* Der Brennstoff geht schon waehrend des Laufens weg, nicht erst beim Fertigwerden. */
        if(recipe.inputFluid != null) {
            for(int i = 0; i < Math.min(recipe.inputFluid.length, this.inputTanks.length); i++) {
                this.inputTanks[i].setFill(Math.max(
                        this.inputTanks[i].getFill() - (int) Math.ceil(recipe.inputFluid[i].fill * this.processSpeed), 0));
            }
        }

        if(this.progress >= 1D) {
            this.produceItem(recipe);

            if(this.canProcess(recipe, speed, power)) this.progress -= 1D;
            else this.progress = 0D;
        }

        if(this.bonus >= 1D && this.canFitOutput(recipe)) {
            this.produceItem(recipe);
            this.bonus -= 1D;
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.bonus);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.bonus = buf.readDouble();
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        this.bonus = nbt.getDouble("bonus" + this.index);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        nbt.putDouble("bonus" + this.index, this.bonus);
    }
}
