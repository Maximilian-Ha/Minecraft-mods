package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PUREXRecipe.
 *
 * Die eigene Rezeptklasse gibt es im Original nur, damit die Rezeptansicht Dauer und
 * Stromverbrauch in einer eigenen Zeile ausgibt (printNEIExtras). Die Ansicht selbst ist im Port
 * noch nicht da; die Klasse bleibt trotzdem, weil PUREXRecipes ueber sie typisiert ist und die
 * Rezeptdatei ihren Namen traegt.
 */
public class PUREXRecipe extends GenericRecipe {

    public PUREXRecipe(String name) {
        super(name);
    }
}
