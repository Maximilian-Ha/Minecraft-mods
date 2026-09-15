package com.hbm.inventory.recipes;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.Tuple.Pair;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.RadiolysisRecipes.
 *
 * Was harte Strahlung aus einer Fluessigkeit macht. Wasser zerfaellt in Wasserstoffperoxid und
 * Wasserstoff; alles Uebrige uebernimmt die Liste des Krackturms unveraendert, weil Strahlung
 * dieselben Bindungen aufbricht wie Hitze. Das Original begruendet das damit, dass beide Wege so
 * auf Dauer deckungsgleich bleiben.
 *
 * Diese Liste ist absichtlich KEIN SerializableRecipe: sie wird aus der Krackliste abgeleitet,
 * die ihrerseits schon aus der Rezeptdatei kommt. Zwei Dateien fuer dieselben Zahlen waeren eine
 * Gelegenheit, sie auseinanderlaufen zu lassen.
 */
public class RadiolysisRecipes {

    private static final Map<FluidType, Pair<FluidStack, FluidStack>> radiolysis = new LinkedHashMap<>();

    public static void registerRadiolysis() {

        radiolysis.clear();
        radiolysis.put(Fluids.WATER, new Pair<>(new FluidStack(Fluids.PEROXIDE, 80), new FluidStack(Fluids.HYDROGEN, 20)));

        Map<FluidType, Pair<FluidStack, FluidStack>> cracking = CrackingRecipes.getCrackingRecipes();

        if(cracking.isEmpty()) {
            throw new IllegalStateException("CrackingRecipes hat beim Anlegen der Radiolyse-Rezepte eine leere Liste geliefert! "
                    + "Entweder stimmt die Ladereihenfolge nicht, oder die Krackrezepte sind entfernt worden.");
        }

        radiolysis.putAll(cracking);
    }

    public static @Nullable Pair<FluidStack, FluidStack> getRadiolysis(FluidType input) {
        return radiolysis.get(input);
    }

    public static Map<FluidType, Pair<FluidStack, FluidStack>> getRecipes() {
        return radiolysis;
    }
}
