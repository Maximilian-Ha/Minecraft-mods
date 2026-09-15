package com.hbm.fluids;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.NuclearTechMod;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Die Bruecke zwischen der Fluidliste des Mods und der Fluid-Registry von Minecraft.
 *
 * HBM fuehrt seine rund 150 Fluide in einer eigenen Liste ({@link com.hbm.inventory.fluid.FluidType}),
 * die mit Minecraft nichts zu tun hat. Solange das so bleibt, kann der Mod keine
 * Fluid-Capability anbieten: ein NeoForge-FluidStack besteht aus einem REGISTRIERTEN Fluid und
 * einer Menge, und ein nicht registriertes Fluid laesst sich darin nicht benennen. Fremde Rohre
 * finden deshalb an jeder Maschine des Mods gar nichts.
 *
 * Diese Klasse meldet zu jedem Eintrag der Fluidliste ein Minecraft-Fluid an und haelt die
 * Zuordnung in beide Richtungen bereit.
 *
 * ZWEI ENTSCHEIDUNGEN, die man kennen sollte:
 *
 * Erstens sind das SCHATTENFLUIDE. Sie haben keinen Block, keinen Eimer und keine Textur und
 * kommen in der Welt nie vor -- ihr einziger Zweck ist, einem FluidStack einen Namen zu geben.
 * Die Fluide des Mods leben weiterhin ausschliesslich in seinen eigenen Tanks. Wer spaeter
 * echte Welt-Fluide will (Eimer, fliessende Bloecke, Fluid-Rendering), baut sie auf dieser
 * Zuordnung auf, statt sie zu ersetzen.
 *
 * Zweitens dient jedes Fluid sich selbst als "fliessende" Form. BaseFlowingFluid.Properties
 * verlangt beide, aber ohne Block kann nichts fliessen; eine zweite Registrierung je Fluid
 * waere Ballast, den niemand je anfasst.
 */
public class NtmFluidBridge {

    private static final DeferredRegister<net.neoforged.neoforge.fluids.FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NuclearTechMod.MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, NuclearTechMod.MODID);

    /** HBM-Fluid zu Minecraft-Fluid. IdentityHashMap, weil FluidType Einzelstuecke sind. */
    private static final Map<com.hbm.inventory.fluid.FluidType, DeferredHolder<Fluid, ?>> TO_VANILLA = new IdentityHashMap<>();
    /** Minecraft-Fluid zurueck zu HBM-Fluid. Wird erst beim ersten Zugriff gefuellt. */
    private static final Map<Fluid, com.hbm.inventory.fluid.FluidType> FROM_VANILLA = new HashMap<>();

    /**
     * Legt fuer jeden Eintrag der Fluidliste ein Schattenfluid an.
     *
     * Muss NACH {@link Fluids#init()} laufen -- vorher ist die Liste leer. In
     * {@link com.hbm.main.NuclearTechMod} ist das sichergestellt.
     */
    public static void register(IEventBus eventBus) {

        for(com.hbm.inventory.fluid.FluidType type : Fluids.metaOrder) {

            /* NONE ist der Platzhalter fuer "leer" und braucht keinen Namen. */
            if(type == Fluids.NONE) continue;

            String name = "shadow_" + type.getInternalName().toLowerCase(Locale.ROOT);
            boolean gaseous = type.hasTrait(FT_Gaseous.class);

            var neoType = FLUID_TYPES.register(name, () -> new net.neoforged.neoforge.fluids.FluidType(
                    net.neoforged.neoforge.fluids.FluidType.Properties.create()
                            /* HBM rechnet in Grad Celsius, NeoForge in Kelvin. */
                            .temperature(Math.max(0, type.temperature + 273))
                            .density(gaseous ? 1 : 1000)
                            .viscosity(gaseous ? 1 : 1000)
                            .canDrown(!gaseous)
                            .canSwim(!gaseous)
            ));

            /*
             * Das Fluid muss sich in seinen eigenen Eigenschaften nennen. Ein Feld der Laenge
             * eins bricht den Ring auf: der Anbieter laeuft erst lange nach der Zuweisung.
             */
            @SuppressWarnings("unchecked")
            DeferredHolder<Fluid, BaseFlowingFluid.Source>[] ref = new DeferredHolder[1];

            ref[0] = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(
                    new BaseFlowingFluid.Properties(neoType, ref[0], ref[0])));

            TO_VANILLA.put(type, ref[0]);
        }

        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }

    /** Das Minecraft-Fluid zu einem HBM-Fluid, oder null fuer NONE und Unbekanntes. */
    public static @Nullable Fluid toVanilla(@Nullable com.hbm.inventory.fluid.FluidType type) {
        if(type == null || type == Fluids.NONE) return null;
        DeferredHolder<Fluid, ?> holder = TO_VANILLA.get(type);
        return holder == null ? null : holder.get();
    }

    /** Das HBM-Fluid zu einem Minecraft-Fluid, oder NONE fuer alles Fremde. */
    public static com.hbm.inventory.fluid.FluidType fromVanilla(@Nullable Fluid fluid) {

        if(fluid == null) return Fluids.NONE;

        if(FROM_VANILLA.isEmpty()) {
            for(Map.Entry<com.hbm.inventory.fluid.FluidType, DeferredHolder<Fluid, ?>> entry : TO_VANILLA.entrySet()) {
                FROM_VANILLA.put(entry.getValue().get(), entry.getKey());
            }
        }

        return FROM_VANILLA.getOrDefault(fluid, Fluids.NONE);
    }
}
