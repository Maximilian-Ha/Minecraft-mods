package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.crossmod.ModIDs;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.ItemUpgrade.UpgradeType;
import com.zuxelus.energycontrol.items.cards.*;
import com.zuxelus.energycontrol.items.kits.ItemKitBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/** Alle Gegenstaende des Mods. */
public class ECItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnergyControl.MODID);

    // ------------------------------------------------------------------ Karten
    public static final DeferredItem<Item> CARD_ENERGY = ITEMS.register("card_energy", () -> new ItemCardEnergy(new Item.Properties()));
    public static final DeferredItem<Item> CARD_LIQUID = ITEMS.register("card_liquid", () -> new ItemCardLiquid(new Item.Properties()));
    public static final DeferredItem<Item> CARD_INVENTORY = ITEMS.register("card_inventory", () -> new ItemCardInventory(new Item.Properties()));
    public static final DeferredItem<Item> CARD_REDSTONE = ITEMS.register("card_redstone", () -> new ItemCardRedstone(new Item.Properties()));
    public static final DeferredItem<Item> CARD_VANILLA = ITEMS.register("card_vanilla", () -> new ItemCardVanilla(new Item.Properties()));
    public static final DeferredItem<Item> CARD_TIME = ITEMS.register("card_time", () -> new ItemCardTime(new Item.Properties()));
    public static final DeferredItem<Item> CARD_TEXT = ITEMS.register("card_text", () -> new ItemCardText(new Item.Properties()));
    public static final DeferredItem<Item> CARD_HBM = ITEMS.register("card_hbm", () -> new ItemCardHBM(new Item.Properties()));

    // ---------------------------------------------------------------- Bausaetze
    public static final DeferredItem<Item> KIT_ENERGY = kit("kit_energy", CARD_ENERGY,
            (level, pos) -> CrossModLoader.getEnergyData(level.getBlockEntity(pos)) != null);
    public static final DeferredItem<Item> KIT_LIQUID = kit("kit_liquid", CARD_LIQUID,
            (level, pos) -> CrossModLoader.getAllTanks(level.getBlockEntity(pos)) != null);
    public static final DeferredItem<Item> KIT_INVENTORY = kit("kit_inventory", CARD_INVENTORY,
            (level, pos) -> level.getBlockEntity(pos) instanceof Container);
    public static final DeferredItem<Item> KIT_REDSTONE = kit("kit_redstone", CARD_REDSTONE,
            (level, pos) -> true);
    public static final DeferredItem<Item> KIT_VANILLA = kit("kit_vanilla", CARD_VANILLA,
            (level, pos) -> level.getBlockEntity(pos) != null);
    public static final DeferredItem<Item> KIT_HBM = kit("kit_hbm", CARD_HBM,
            (level, pos) -> CrossModLoader.getCrossMod(ModIDs.HBM).getCardData(level, pos) != null);

    // -------------------------------------------------------------- Aufwertungen
    public static final DeferredItem<Item> UPGRADE_RANGE = ITEMS.register("upgrade_range", () -> new ItemUpgrade(new Item.Properties(), UpgradeType.RANGE));
    public static final DeferredItem<Item> UPGRADE_COLOR = ITEMS.register("upgrade_color", () -> new ItemUpgrade(new Item.Properties(), UpgradeType.COLOR));
    public static final DeferredItem<Item> UPGRADE_TOUCH = ITEMS.register("upgrade_touch", () -> new ItemUpgrade(new Item.Properties(), UpgradeType.TOUCH));

    // ------------------------------------------------------------------ Bauteile
    public static final DeferredItem<Item> MACHINE_CASING = ITEMS.register("machine_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BASIC_CIRCUIT = ITEMS.register("basic_circuit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ADVANCED_CIRCUIT = ITEMS.register("advanced_circuit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> THERMOMETER = ITEMS.register("thermometer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PANEL_TOOLKIT = ITEMS.register("panel_toolkit", () -> new Item(new Item.Properties()));

    private static DeferredItem<Item> kit(String name, Supplier<? extends Item> card, BiPredicate<Level, BlockPos> suitable) {
        return ITEMS.register(name, () -> new ItemKitBase(new Item.Properties(), card, suitable));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
