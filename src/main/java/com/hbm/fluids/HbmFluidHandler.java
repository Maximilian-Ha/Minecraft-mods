package com.hbm.fluids;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Zeigt die Tanks einer Maschine als NeoForge-Fluidbehaelter.
 *
 * Damit koennen Rohre und Pumpen fremder Mods die Maschinen des Mods befuellen und leeren. Die
 * Maschinen selbst merken davon nichts -- sie arbeiten weiter mit ihren eigenen Tanks, und
 * dieser Adapter uebersetzt nur hin und her.
 *
 * WELCHER TANK WAS DARF: was die Maschine als Eingang fuehrt, laesst sich befuellen; was sie
 * als Ausgang fuehrt, laesst sich leeren. Das ist dieselbe Aufteilung, die auch das eigene
 * Rohrnetz des Mods benutzt, also verhaelt sich ein fremdes Rohr wie ein eigenes.
 *
 * ABWEICHUNG: ein Tank ohne festgelegte Sorte nimmt die erste an, die hineinlaeuft -- so wie
 * eine leere Maschine im Original auch. Ein Tank mit fester Sorte weist alles andere ab.
 */
public class HbmFluidHandler implements IFluidHandler {

    private final FluidTank[] fillable;
    private final FluidTank[] drainable;
    /** Alle Tanks hintereinander: erst die befuellbaren, dann die leerbaren. */
    private final FluidTank[] all;

    public HbmFluidHandler(FluidTank[] fillable, FluidTank[] drainable) {
        this.fillable = fillable == null ? new FluidTank[0] : fillable;
        this.drainable = drainable == null ? new FluidTank[0] : drainable;

        this.all = new FluidTank[this.fillable.length + this.drainable.length];
        System.arraycopy(this.fillable, 0, this.all, 0, this.fillable.length);
        System.arraycopy(this.drainable, 0, this.all, this.fillable.length, this.drainable.length);
    }

    @Override public int getTanks() { return this.all.length; }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if(tank < 0 || tank >= this.all.length) return FluidStack.EMPTY;
        return toStack(this.all[tank]);
    }

    @Override
    public int getTankCapacity(int tank) {
        if(tank < 0 || tank >= this.all.length) return 0;
        return this.all[tank].getMaxFill();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if(tank < 0 || tank >= this.all.length) return false;

        com.hbm.inventory.fluid.FluidType type = NtmFluidBridge.fromVanilla(stack.getFluid());
        if(type == Fluids.NONE) return false;

        com.hbm.inventory.fluid.FluidType held = this.all[tank].getTankType();
        return held == Fluids.NONE || held == type;
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {

        if(resource.isEmpty()) return 0;

        com.hbm.inventory.fluid.FluidType type = NtmFluidBridge.fromVanilla(resource.getFluid());
        if(type == Fluids.NONE) return 0;

        int remaining = resource.getAmount();
        int filled = 0;

        for(FluidTank tank : this.fillable) {

            com.hbm.inventory.fluid.FluidType held = tank.getTankType();
            if(held != Fluids.NONE && held != type) continue;

            int space = tank.getMaxFill() - tank.getFill();
            if(space <= 0) continue;

            int take = Math.min(space, remaining);

            if(action.execute()) {
                if(held == Fluids.NONE) tank.setTankType(type);
                tank.setFill(tank.getFill() + take);
            }

            filled += take;
            remaining -= take;
            if(remaining <= 0) break;
        }

        return filled;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {

        if(resource.isEmpty()) return FluidStack.EMPTY;

        com.hbm.inventory.fluid.FluidType type = NtmFluidBridge.fromVanilla(resource.getFluid());
        if(type == Fluids.NONE) return FluidStack.EMPTY;

        return drainType(type, resource.getAmount(), action);
    }

    /** Ohne Sortenangabe wird der erste Ausgabetank angezapft, in dem ueberhaupt etwas steht. */
    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {

        for(FluidTank tank : this.drainable) {
            if(tank.getFill() > 0 && tank.getTankType() != Fluids.NONE) {
                return drainType(tank.getTankType(), maxDrain, action);
            }
        }

        return FluidStack.EMPTY;
    }

    private FluidStack drainType(com.hbm.inventory.fluid.FluidType type, int maxDrain, FluidAction action) {

        Fluid vanilla = NtmFluidBridge.toVanilla(type);
        if(vanilla == null || maxDrain <= 0) return FluidStack.EMPTY;

        int drained = 0;

        for(FluidTank tank : this.drainable) {

            if(tank.getTankType() != type || tank.getFill() <= 0) continue;

            int take = Math.min(tank.getFill(), maxDrain - drained);
            if(take <= 0) continue;

            if(action.execute()) tank.setFill(tank.getFill() - take);

            drained += take;
            if(drained >= maxDrain) break;
        }

        return drained <= 0 ? FluidStack.EMPTY : new FluidStack(vanilla, drained);
    }

    private static FluidStack toStack(FluidTank tank) {
        Fluid vanilla = NtmFluidBridge.toVanilla(tank.getTankType());
        if(vanilla == null || tank.getFill() <= 0) return FluidStack.EMPTY;
        return new FluidStack(vanilla, tank.getFill());
    }
}
