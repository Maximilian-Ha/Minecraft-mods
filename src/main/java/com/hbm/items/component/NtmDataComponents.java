package com.hbm.items.component;

import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NtmDataComponents {

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NuclearTechMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> META = DATA_COMPONENT_TYPES.register("meta", () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.INT).build());

    /** Verbrauch einer Lichtbogenelektrode, gezaehlt in Schmelzvorgaengen. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ELECTRODE_DURABILITY = DATA_COMPONENT_TYPES.register("electrode_durability", () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.INT).build());

    /** Inhalt eines Schrottbrockens in Quanten; 1 Quantum ist 1/72 Barren. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SCRAPS_AMOUNT = DATA_COMPONENT_TYPES.register("scraps_amount", () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.INT).build());

    /** Ob der Schrottbrocken fluessig ist -- dann ist er nur eine Anzeige, kein giessbarer Gegenstand. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SCRAPS_LIQUID = DATA_COMPONENT_TYPES.register("scraps_liquid", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    /** Zustand eines RBMK-Brennstabs: Abbrand, Xenon, Kern- und Huellentemperatur. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RBMKFuelData>> RBMK_FUEL = DATA_COMPONENT_TYPES.register("rbmk_fuel", () -> DataComponentType.<RBMKFuelData>builder().persistent(RBMKFuelData.CODEC).networkSynchronized(RBMKFuelData.STREAM_CODEC).build());

    /** Aufgelaufene Reaktionen einer Brennstoffplatte des Forschungsreaktors. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FUEL_ROD_LIFE = DATA_COMPONENT_TYPES.register("fuel_rod_life", () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.INT).build());

    /** Der Reaktor, auf den ein Reaktorfuehler eingemessen ist. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> REACTOR_LINK = DATA_COMPONENT_TYPES.register("reactor_link", () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());

    /** Die Saeule, die der Verbindungsstab sich gemerkt hat. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> RBMK_LINK = DATA_COMPONENT_TYPES.register("rbmk_link", () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());

    /** Die Namen, auf die ein Geschuetzturm mit diesem Zielchip nicht schiesst. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<String>>> TURRET_WHITELIST = DATA_COMPONENT_TYPES.register("turret_whitelist", () -> DataComponentType.<java.util.List<String>>builder().persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())).build());

    /**
     * Der Wurf der Tontafel, Runde 234. Das Original schreibt ihn als tabletSeed in die NBT
     * des Stapels; in 1.21 ist so etwas eine Datenkomponente. Sie muss zum Client, denn
     * gezeichnet wird die Tafel dort -- deshalb networkSynchronized.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> TABLET_SEED = DATA_COMPONENT_TYPES.register("tablet_seed", () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());

    public static void register(IEventBus eventBus) { DATA_COMPONENT_TYPES.register(eventBus); }
}
