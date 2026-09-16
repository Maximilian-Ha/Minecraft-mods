package com.zuxelus.energycontrol.crossmod.hbm;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;
import com.zuxelus.energycontrol.utils.PanelFormat;
import net.minecraft.nbt.CompoundTag;

/**
 * Uebersetzt HBM-Tanks in das, was die Tafel anzeigt.
 *
 * HBM fuehrt seine Fluessigkeiten in einem eigenen Register ({@code FluidType}), nicht im
 * Fluid-Register von Minecraft. Deshalb geht hier nichts ueber {@code FluidStack} -- Name
 * und Fuellstand kommen direkt vom Tank.
 */
public final class HbmFluids {

    /** Bis hierhin legt eine Karte Tanks ab; mehr passt ohnehin auf keinen Schirm. */
    public static final int MAX_TANKS = 5;

    private HbmFluids() { }

    public static FluidInfo toInfo(FluidTank tank) {
        return new FluidInfo(tank.getTankType().getName(), tank.getFill(), tank.getMaxFill());
    }

    /** Die Zeile, die auf dem Schirm landet: "Dampf: 4 000 / 16 000 mB". */
    public static String format(FluidTank tank) {
        return tank.getTankType().getName().getString() + ": "
                + PanelFormat.number(tank.getFill()) + " / "
                + PanelFormat.number(tank.getMaxFill()) + " mB";
    }

    /**
     * Legt bis zu fuenf Tanks als fertige Zeilen im Kartenspeicher ab. Leere Tanks ohne
     * zugewiesene Fluessigkeit bleiben weg -- eine Maschine hat oft mehr Tanks, als gerade
     * benutzt sind, und fuenf Zeilen "Leer" helfen niemandem.
     */
    public static boolean writeTanks(CompoundTag tag, FluidTank[] tanks) {
        if(tanks == null) return false;

        int written = 0;
        for(FluidTank tank : tanks) {
            if(written >= MAX_TANKS) break;
            if(tank == null) continue;
            if(tank.getTankType() == Fluids.NONE && tank.getFill() <= 0) continue;
            tag.putString(DataHelper.tank(written), format(tank));
            written++;
        }

        if(written > 0) tag.putInt(DataHelper.TANK_COUNT, written);
        return written > 0;
    }
}
