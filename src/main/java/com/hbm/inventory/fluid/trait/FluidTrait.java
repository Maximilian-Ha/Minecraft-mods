package com.hbm.inventory.fluid.trait;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FluidTrait {
    public static List<Class<? extends FluidTrait>> traitList = new ArrayList<>();
    public static HashBiMap<String, Class<? extends FluidTrait>> traitNameMap = HashBiMap.create();

    static {
        //complex traits with values
        registerTrait("corrosive", FT_Corrosive.class);
        registerTrait("flammable", FT_Flammable.class);
        registerTrait("heatable", FT_Heatable.class);
        registerTrait("coolable", FT_Coolable.class);
        registerTrait("combustible", FT_Combustible.class);
        registerTrait("polluting", FT_Polluting.class);
        registerTrait("pwrmoderator", FT_PWRModerator.class);
        registerTrait("poison", FT_Poison.class);
        /* Diese Zeile stand auskommentiert da, seit FT_Toxin in Runde 14 portiert wurde -- und
         * das war ein Absturz beim ERSTSTART: writeDefaultTraits schlaegt jeden Trait in dieser
         * Karte nach, und fuer einen nicht eingetragenen kommt null zurueck, was der JsonWriter
         * mit einer NullPointerException quittiert. Vier Fluide tragen ihn (Chlor, Phosgen,
         * Senfgas, Rotschlamm), also traf es jede frische Installation. */
        registerTrait("toxin", FT_Toxin.class);
        registerTrait("ventradiation", FT_VentRadiation.class);
        registerTrait("pheromone", FT_Pheromone.class);
        //simple traits, "tags"
        registerTrait("gaseous", FT_Gaseous.class);
        registerTrait("gaseous_art", FT_Gaseous_ART.class);
        registerTrait("liquid", FT_Liquid.class);
        registerTrait("viscous", FT_Viscous.class);
        registerTrait("plasma", FT_Plasma.class);
        registerTrait("amat", FT_Amat.class);
        registerTrait("leadcontainer", FT_LeadContainer.class);
        registerTrait("delicious", FT_Delicious.class);
        registerTrait("noid", FT_NoID.class);
        registerTrait("nocontainer", FT_NoContainer.class);
        registerTrait("unsiphonable", FT_Unsiphonable.class);
    }

    private static void registerTrait(String name, Class<? extends FluidTrait> clazz) {
        traitNameMap.put(name, clazz);
        traitList.add(clazz);
    }

    /** Important information that should always be displayed */
    public void addInfo(List<Component> info) { }
    /* General names of simple traits which are displayed when holding shift */
    public void addInfoHidden(List<Component> info) { }

    public void onFluidRelease(Level level, BlockPos pos, FluidTank tank, int overflowAmount, FluidReleaseType type) { }

    public void serializeJSON(JsonWriter writer) throws IOException { }
    public void deserializeJSON(JsonObject obj) { }

    public enum FluidReleaseType {
        VOID,	//if fluid is deleted entirely, shouldn't be used
        BURN,	//if fluid is burned or combusted
        SPILL	//if fluid is spilled via leakage or the container breaking
    }

    /**
     * Verteiler: reicht ein Freisetzungsereignis an jede Eigenschaft des Fluids weiter.
     *
     * Portiert aus 1.7.10: FluidTrait.onRelease. Im Port fehlte genau diese eine Methode --
     * die Eigenschaften selbst (FT_Polluting, FT_VentRadiation) waren vollstaendig da, nur rief
     * sie niemand auf. Damit waren beide toter Code: aus einem geborstenen Tank lief Rauch
     * und Radioaktivitaet folgenlos aus.
     */
    public static void onRelease(Level level, BlockPos pos, FluidType type, FluidTank tank, FluidReleaseType release, int mB) {
        if(level == null || type == null) return;

        for(FluidTrait trait : type.traits.values()) {
            trait.onFluidRelease(level, pos, tank, mB, release);
        }
    }
}
