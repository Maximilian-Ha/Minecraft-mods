package com.zuxelus.energycontrol.crossmod.mekanism;

import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Uebersetzt Mekanism-Tanks in das, was die Tafel anzeigt.
 *
 * Mekanism fuehrt zweierlei Inhalte: Fluessigkeiten im Register von Minecraft (Wasser,
 * Dampf als Fluid gibt es dort nicht -- Dampf ist bei Mekanism eine Chemikalie) und
 * Chemikalien in einem eigenen Register. Seit 10.7 sind Gas, Schlamm, Pigment und
 * Infusion dort zu einem Typ {@code Chemical} verschmolzen; die vier getrennten Tanks
 * des Originals von 1.12.2 gibt es also nicht mehr.
 *
 * Beides kommt hier auf denselben Nenner: Name, Fuellstand, Fassungsvermoegen in mB.
 */
public final class MekTanks {

    /** Bis hierhin legt eine Karte Tanks ab; mehr passt ohnehin auf keinen Schirm. */
    public static final int MAX_TANKS = 5;

    private MekTanks() { }

    public static FluidInfo of(IChemicalTank tank) {
        if(tank == null) return null;
        ChemicalStack stack = tank.getStack();
        if(stack == null || stack.isEmpty()) return FluidInfo.empty(tank.getCapacity());
        return new FluidInfo(stack.getTextComponent(), stack.getAmount(), tank.getCapacity());
    }

    public static FluidInfo of(IFluidTank tank) {
        if(tank == null) return null;
        return FluidInfo.of(tank.getFluid(), tank.getCapacity());
    }

    /** Ein einzelner Tank eines Chemikalien-Griffs. */
    public static FluidInfo of(IChemicalHandler handler, int index) {
        ChemicalStack stack = handler.getChemicalInTank(index);
        long capacity = handler.getChemicalTankCapacity(index);
        if(stack == null || stack.isEmpty()) return FluidInfo.empty(capacity);
        return new FluidInfo(stack.getTextComponent(), stack.getAmount(), capacity);
    }

    /**
     * Alle Tanks, die ein Block ueber die Schnittstellen anbietet -- erst Fluessigkeiten
     * (die Schnittstelle von NeoForge), dann Chemikalien (die von Mekanism). Damit
     * erwischt man jede Mekanism-Maschine, ohne sie einzeln aufzuzaehlen.
     */
    public static List<FluidInfo> around(Level level, BlockPos pos) {
        List<FluidInfo> result = new ArrayList<>();

        IFluidHandler fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if(fluids != null) {
            for(int i = 0; i < fluids.getTanks(); i++) result.add(FluidInfo.of(fluids, i));
        }

        IChemicalHandler chemicals = level.getCapability(CrossMekanism.CHEMICAL, pos, null);
        if(chemicals != null) {
            for(int i = 0; i < chemicals.getChemicalTanks(); i++) result.add(of(chemicals, i));
        }

        return result;
    }

    /**
     * Legt bis zu fuenf Tanks als fertige Zeilen im Kartenspeicher ab. Leere Tanks bleiben
     * weg, solange noch gefuellte folgen -- eine Mekanism-Maschine hat oft mehr Taenke, als
     * gerade benutzt sind, und fuenf Zeilen "Leer" helfen niemandem.
     */
    public static boolean writeTanks(CompoundTag tag, List<FluidInfo> tanks) {
        if(tanks == null || tanks.isEmpty()) return false;

        List<FluidInfo> filled = new ArrayList<>(tanks.size());
        for(FluidInfo tank : tanks) {
            if(tank != null && tank.amount() > 0) filled.add(tank);
        }
        List<FluidInfo> source = filled.isEmpty() ? tanks : filled;

        int written = 0;
        for(FluidInfo tank : source) {
            if(written >= MAX_TANKS) break;
            if(tank == null) continue;
            tag.putString(DataHelper.tank(written), tank.format());
            written++;
        }

        if(written > 0) tag.putInt(DataHelper.TANK_COUNT, written);
        return written > 0;
    }
}
