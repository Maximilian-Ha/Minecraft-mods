package com.hbm.inventory;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.BatteryPackItem.BatteryPackType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: die Beutelisten aus com.hbm.blocks.generic.BlockCrate.
 *
 * Das Original legt jeden Eintrag so oft in eine Liste, wie sein Gewicht angibt, und zieht
 * dann gleichverteilt. Hier steht das Gewicht als Zahl daneben und wird beim Ziehen
 * aufsummiert -- dieselbe Verteilung, ohne die Liste aufzublaehen. Das Original baut die
 * Listen ausserdem bei jedem Oeffnen neu auf; hier entstehen sie einmal beim ersten Zugriff.
 *
 * NICHT UEBERNOMMEN, weil es die Gegenstaende im Port noch nicht gibt:
 *   Bleikiste   : pellet_rtg_weak (Gewicht 7 von 155)
 *   Metallkiste : centrifuge_element (6), piston_selenium (6) von 125
 * Die uebrigen drei Listen des Originals (Nachschub-, Waffen- und rote Kiste) sind noch gar
 * nicht zu portieren: dort fehlen die Spritzen, die Granaten und die meisten Sonderwaffen.
 */
public class CrateLoot {

    /** Ein Eintrag: was gezogen werden kann und mit welchem Gewicht. */
    private record Eintrag(Supplier<ItemStack> stapel, int gewicht) {}

    private static List<Eintrag> blei;
    private static List<Eintrag> metall;

    private static void lege(List<Eintrag> liste, Supplier<? extends ItemLike> was, int gewicht) {
        liste.add(new Eintrag(() -> new ItemStack(was.get()), gewicht));
    }

    private static List<Eintrag> blei() {
        if(blei != null) return blei;

        List<Eintrag> liste = new ArrayList<>();
        lege(liste, NtmItems.INGOT_URANIUM, 10);
        lege(liste, NtmItems.INGOT_U238, 8);
        lege(liste, NtmItems.INGOT_PLUTONIUM, 7);
        lege(liste, NtmItems.INGOT_PU240, 6);
        lege(liste, NtmItems.INGOT_NEPTUNIUM, 7);
        lege(liste, NtmItems.INGOT_URANIUM_FUEL, 8);
        lege(liste, NtmItems.INGOT_PLUTONIUM_FUEL, 7);
        lege(liste, NtmItems.INGOT_MOX_FUEL, 6);
        lege(liste, NtmItems.NUGGET_URANIUM, 10);
        lege(liste, NtmItems.NUGGET_U238, 8);
        lege(liste, NtmItems.NUGGET_PLUTONIUM, 7);
        lege(liste, NtmItems.NUGGET_PU240, 6);
        lege(liste, NtmItems.NUGGET_NEPTUNIUM, 7);
        lege(liste, NtmItems.NUGGET_URANIUM_FUEL, 8);
        lege(liste, NtmItems.NUGGET_PLUTONIUM_FUEL, 7);
        lege(liste, NtmItems.NUGGET_MOX_FUEL, 6);
        lege(liste, NtmItems.CELL_DEUTERIUM, 8);
        lege(liste, NtmItems.CELL_TRITIUM, 8);
        lege(liste, NtmItems.CELL_UF6, 8);
        lege(liste, NtmItems.CELL_PUF6, 8);
        lege(liste, NtmItems.PELLET_RTG, 6);
        lege(liste, NtmItems.POWDER_YELLOWCAKE, 10);
        return blei = liste;
    }

    private static List<Eintrag> metall() {
        if(metall != null) return metall;

        List<Eintrag> liste = new ArrayList<>();
        lege(liste, NtmBlocks.MACHINE_PRESS, 10);
        lege(liste, NtmBlocks.MACHINE_REACTOR_BREEDING, 6);
        lege(liste, NtmBlocks.MACHINE_WOOD_BURNER, 10);
        lege(liste, NtmBlocks.MACHINE_DIESEL, 8);
        lege(liste, NtmBlocks.MACHINE_RTG, 4);
        lege(liste, NtmBlocks.RED_PYLON, 9);
        liste.add(new Eintrag(
                () -> MetaHelper.newStack(NtmItems.BATTERY_PACK.get(), 1, BatteryPackType.BATTERY_LEAD.ordinal()), 10));
        lege(liste, NtmBlocks.MACHINE_ELECTRIC_FURNACE, 8);
        lege(liste, NtmBlocks.MACHINE_ASSEMBLY_MACHINE, 10);
        lege(liste, NtmBlocks.MACHINE_FLUID_TANK, 7);
        lege(liste, NtmItems.MOTOR, 8);
        lege(liste, NtmItems.COIL_TUNGSTEN, 7);
        lege(liste, NtmItems.PHOTO_PANEL, 3);
        lege(liste, NtmItems.COIL_COPPER, 10);
        lege(liste, NtmItems.BLADE_TITANIUM, 3);
        return metall = liste;
    }

    /** Zieht einen Gegenstand aus der Bleikiste. */
    public static ItemStack ziehBlei(RandomSource zufall) { return zieh(blei(), zufall); }

    /** Zieht einen Gegenstand aus der Metallkiste. */
    public static ItemStack ziehMetall(RandomSource zufall) { return zieh(metall(), zufall); }

    private static ItemStack zieh(List<Eintrag> liste, RandomSource zufall) {
        int summe = 0;
        for(Eintrag e : liste) summe += e.gewicht();

        int wurf = zufall.nextInt(summe);
        for(Eintrag e : liste) {
            wurf -= e.gewicht();
            if(wurf < 0) return e.stapel().get();
        }
        return liste.getLast().stapel().get();
    }
}
