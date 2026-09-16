package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.init.ECBlocks;
import com.zuxelus.energycontrol.init.ECItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Sprachdateien. Englisch ist aus der en_US.lang des Originals uebernommen, Deutsch ist
 * neu -- das Original hatte nur Englisch, Spanisch, Russisch und Chinesisch.
 */
public class ECLanguageProvider extends LanguageProvider {

    private final boolean german;

    public ECLanguageProvider(PackOutput output, String locale) {
        super(output, EnergyControl.MODID, locale);
        this.german = locale.equals("de_de");
    }

    private void both(String key, String english, String deutsch) {
        add(key, german ? deutsch : english);
    }

    @Override
    protected void addTranslations() {
        both("itemGroup.energycontrol", "Energy Control", "Energy Control");

        // ------------------------------------------------------------- Bloecke
        block(ECBlocks.INFO_PANEL.get(), "Industrial Information Panel", "Industrielle Informationstafel");
        block(ECBlocks.INFO_PANEL_EXTENDER.get(), "Information Panel Extender", "Tafelerweiterung");
        block(ECBlocks.THERMAL_MONITOR.get(), "Thermal Monitor", "Waermemelder");
        block(ECBlocks.REMOTE_THERMAL_MONITOR.get(), "Remote Thermal Monitor", "Fernwaermeanzeige");
        block(ECBlocks.RANGE_TRIGGER.get(), "Range Trigger", "Bereichsmelder");
        block(ECBlocks.ENERGY_COUNTER.get(), "Energy Counter", "Energiezaehler");
        block(ECBlocks.KIT_ASSEMBLER.get(), "Kit Assembler", "Bausatzmontage");
        block(ECBlocks.HOWLER_ALARM.get(), "Howler Alarm", "Heulalarm");
        block(ECBlocks.INDUSTRIAL_ALARM.get(), "Industrial Alarm", "Warnleuchte");

        both("container.energycontrol.info_panel", "Information Panel", "Informationstafel");
        both("container.energycontrol.thermal_monitor", "Thermal Monitor", "Waermemelder");
        both("container.energycontrol.range_trigger", "Range Trigger", "Bereichsmelder");
        both("container.energycontrol.remote_thermal_monitor", "Remote Thermal Monitor", "Fernwaermeanzeige");
        both("container.energycontrol.energy_counter", "Energy Counter", "Energiezaehler");
        both("container.energycontrol.kit_assembler", "Kit Assembler", "Bausatzmontage");

        // ------------------------------------------------------------- Karten
        item(ECItems.CARD_ENERGY.get(), "Energy Sensor Card", "Stromsensorkarte");
        item(ECItems.CARD_LIQUID.get(), "Liquid Sensor Card", "Fluessigkeitssensorkarte");
        item(ECItems.CARD_INVENTORY.get(), "Inventory Sensor Card", "Inventarsensorkarte");
        item(ECItems.CARD_REDSTONE.get(), "Redstone Sensor Card", "Redstone-Sensorkarte");
        item(ECItems.CARD_VANILLA.get(), "Vanilla Sensor Card", "Vanilla-Sensorkarte");
        item(ECItems.CARD_TIME.get(), "Time Card", "Zeitkarte");
        item(ECItems.CARD_TEXT.get(), "Text Card", "Textkarte");
        item(ECItems.CARD_TOGGLE.get(), "Remote Toggle Card", "Fernschaltkarte");
        item(ECItems.CARD_HBM.get(), "HBM Sensor Card", "HBM-Sensorkarte");
        item(ECItems.CARD_MEKANISM.get(), "Mekanism Sensor Card", "Mekanism-Sensorkarte");
        item(ECItems.CARD_COUNTER.get(), "Counter Sensor Card", "Zaehlerkarte");

        item(ECItems.KIT_ENERGY.get(), "Energy Sensor Kit", "Stromsensor-Bausatz");
        item(ECItems.KIT_LIQUID.get(), "Liquid Sensor Kit", "Fluessigkeitssensor-Bausatz");
        item(ECItems.KIT_INVENTORY.get(), "Inventory Sensor Kit", "Inventarsensor-Bausatz");
        item(ECItems.KIT_REDSTONE.get(), "Redstone Sensor Kit", "Redstone-Sensor-Bausatz");
        item(ECItems.KIT_VANILLA.get(), "Vanilla Sensor Kit", "Vanilla-Sensor-Bausatz");
        item(ECItems.KIT_TOGGLE.get(), "Remote Toggle Kit", "Fernschalt-Bausatz");
        item(ECItems.KIT_HBM.get(), "HBM Sensor Kit", "HBM-Sensor-Bausatz");
        item(ECItems.KIT_MEKANISM.get(), "Mekanism Sensor Kit", "Mekanism-Sensor-Bausatz");
        item(ECItems.KIT_COUNTER.get(), "Counter Sensor Kit", "Zaehler-Bausatz");

        item(ECItems.UPGRADE_RANGE.get(), "Range Upgrade", "Reichweitenaufwertung");
        item(ECItems.UPGRADE_COLOR.get(), "Color Upgrade", "Farbaufwertung");
        item(ECItems.UPGRADE_TOUCH.get(), "Touch Upgrade", "Beruehrungsaufwertung");

        item(ECItems.MACHINE_CASING.get(), "Machine Casing", "Maschinengehaeuse");
        item(ECItems.BASIC_CIRCUIT.get(), "Basic Circuit", "Einfache Schaltung");
        item(ECItems.ADVANCED_CIRCUIT.get(), "Advanced Circuit", "Fortgeschrittene Schaltung");
        item(ECItems.THERMOMETER.get(), "Thermometer", "Thermometer");
        item(ECItems.PANEL_TOOLKIT.get(), "Panel Toolkit", "Tafelwerkzeug");

        both("item.ec.card.target", "Target: %s %s %s", "Ziel: %s %s %s");

        // ------------------------------------------------------------ Zeilen
        both("msg.ec.InfoPanelEnergy", "Energy: %s", "Energie: %s");
        both("msg.ec.InfoPanelFree", "Free: %s", "Frei: %s");
        both("msg.ec.InfoPanelCapacity", "Capacity: %s", "Fassung: %s");
        both("msg.ec.InfoPanelPercentage", "Percentage: %s", "Anteil: %s");
        both("msg.ec.InfoPanelTank", "Tank: %s", "Tank: %s");
        both("msg.ec.InfoPanelEmpty", "Empty", "Leer");
        both("msg.ec.InfoPanelConsumption", "Consumption: %s", "Verbrauch: %s");
        both("msg.ec.InfoPanelDifference", "Difference: %s", "Aenderung: %s");
        both("msg.ec.InfoPanelHeat", "Heat: %s", "Temperatur: %s");
        both("msg.ec.InfoPanelMeltingPoint", "Melting point: %s", "Schmelzpunkt: %s");
        both("msg.ec.InfoPanelHullHeat", "Hull heat: %s", "Huellentemperatur: %s");
        both("msg.ec.InfoPanelCoreHeat", "Core heat: %s", "Kerntemperatur: %s");
        both("msg.ec.InfoPanelDepletion", "Depletion: %s", "Abbrand: %s");
        both("msg.ec.InfoPanelXenon", "Xenon: %s", "Xenon: %s");
        both("msg.ec.InfoPanelRadiation", "Radiation: %s", "Strahlung: %s");
        both("msg.ec.InfoPanelFluxSlow", "Slow flux: %s", "Langsamer Fluss: %s");
        both("msg.ec.InfoPanelFluxFast", "Fast flux: %s", "Schneller Fluss: %s");
        both("msg.ec.InfoPanelOperatingLevel", "Operating level: %s", "Regelstellung: %s");
        both("msg.ec.InfoPanelPressure", "Pressure: %s", "Druck: %s");
        both("msg.ec.InfoPanelFuel", "Fuel: %s", "Brennstoff: %s");
        both("msg.ec.InfoPanelProgress", "Progress: %s", "Fortschritt: %s");
        both("msg.ec.InfoPanelBlock", "Block: %s", "Block: %s");
        both("msg.ec.InfoPanelBurnTime", "Burn time: %s", "Brenndauer: %s");
        both("msg.ec.InfoPanelComparator", "Comparator: %s", "Vergleicher: %s");
        both("msg.ec.InfoPanelSignal", "Signal: %s", "Signal: %s");
        both("msg.ec.InfoPanelSlots", "Slots: %s", "Faecher: %s");
        both("msg.ec.InfoPanelTotalItems", "Items: %s", "Gegenstaende: %s");
        both("msg.ec.InfoPanelTime", "Time: %s", "Uhrzeit: %s");
        both("msg.ec.InfoPanelDay", "Day: %s", "Tag: %s");
        both("msg.ec.InfoPanelThunder", "Thunderstorm", "Gewitter");
        both("msg.ec.InfoPanelRain", "Rain", "Regen");
        both("msg.ec.InfoPanelClear", "Clear", "Klar");
        both("msg.ec.InfoPanelOn", "On", "An");
        both("msg.ec.InfoPanelOff", "Off", "Aus");
        both("msg.ec.InfoPanelEnvLoss", "Environment loss: %s", "Umgebungsverlust: %s");
        both("msg.ec.InfoPanelBurnRate", "Burn rate: %s", "Abbrandrate: %s");
        both("msg.ec.InfoPanelMaxBurnRate", "Max burn rate: %s", "Hoechste Abbrandrate: %s");
        both("msg.ec.InfoPanelRateLimit", "Rate limit: %s", "Sollrate: %s");
        both("msg.ec.InfoPanelDamage", "Damage: %s", "Schaden: %s");
        both("msg.ec.InfoPanelBoilRate", "Boil rate: %s", "Verdampfung: %s");
        both("msg.ec.InfoPanelMaxBoilRate", "Max boil rate: %s", "Hoechste Verdampfung: %s");
        both("msg.ec.InfoPanelSuperheaters", "Superheaters: %s", "Ueberhitzer: %s");
        both("msg.ec.InfoPanelAssemblies", "Fuel assemblies: %s", "Brennelemente: %s");
        both("msg.ec.InfoPanelSurfaceArea", "Surface area: %s", "Oberflaeche: %s");
        both("msg.ec.InfoPanelPlasma", "Plasma: %s", "Plasma: %s");
        both("msg.ec.InfoPanelCaseHeat", "Case heat: %s", "Gehaeusetemperatur: %s");
        both("msg.ec.InfoPanelInjectionRate", "Injection rate: %s", "Einspeisung: %s");
        both("msg.ec.InfoPanelPassiveGeneration", "Passive output: %s", "Ruheleistung: %s");
        both("msg.ec.InfoPanelSteamPerTick", "Steam: %s", "Dampf: %s");
        both("msg.ec.InfoPanelProduction", "Production: %s", "Leistung: %s");
        both("msg.ec.InfoPanelMaxProduction", "Max production: %s", "Hoechstleistung: %s");
        both("msg.ec.InfoPanelFlow", "Flow: %s", "Durchfluss: %s");
        both("msg.ec.InfoPanelMaxFlow", "Max flow: %s", "Hoechster Durchfluss: %s");
        both("msg.ec.InfoPanelBlades", "Blades: %s", "Schaufeln: %s");
        both("msg.ec.InfoPanelCoils", "Coils: %s", "Spulen: %s");
        both("msg.ec.InfoPanelVents", "Vents: %s", "Auslaesse: %s");
        both("msg.ec.InfoPanelCondensers", "Condensers: %s", "Kondensatoren: %s");
        both("msg.ec.InfoPanelDispersers", "Dispersers: %s", "Verteiler: %s");
        both("msg.ec.InfoPanelInput", "Input: %s", "Zufluss: %s");
        both("msg.ec.InfoPanelOutput", "Output: %s", "Abfluss: %s");
        both("msg.ec.InfoPanelTransferCap", "Transfer cap: %s", "Durchsatzgrenze: %s");
        both("msg.ec.InfoPanelCells", "Cells: %s", "Zellen: %s");
        both("msg.ec.InfoPanelProviders", "Providers: %s", "Uebertrager: %s");
        both("msg.ec.InfoPanelGain", "Gain: %s", "Ausbeute: %s");
        both("msg.ec.InfoPanelProcessRate", "Process rate: %s", "Verarbeitung: %s");
        both("msg.ec.InfoPanelProcessed", "Processed: %s", "Verarbeitet: %s");
        both("msg.ec.InfoPanelReceivedEnergy", "Received: %s", "Aufnahme: %s");
        both("msg.ec.InfoPanelToMine", "Blocks to mine: %s", "Zu graben: %s");
        both("msg.ec.InfoPanelMinerState", "State: %s", "Zustand: %s");
        both("msg.ec.InfoPanelRadius", "Radius: %s", "Radius: %s");
        both("msg.ec.InfoPanelMinY", "Lowest layer: %s", "Unterste Schicht: %s");
        both("msg.ec.InfoPanelMaxY", "Highest layer: %s", "Oberste Schicht: %s");
        both("msg.ec.InfoPanelCounter", "Counter: %s", "Zaehlerstand: %s");
        both("msg.ec.CounterTotal", "Counter: %s FE", "Zaehlerstand: %s FE");
        both("msg.ec.CounterRate", "Throughput: %s FE/t", "Durchsatz: %s FE/t");
        both("msg.ec.CounterReset", "Reset counter", "Zaehler zuruecksetzen");
        both("msg.ec.InfoPanelOutOfRange", "Out of range", "Ausser Reichweite");
        both("msg.ec.InfoPanelInvalidCard", "Invalid card", "Ungueltige Karte");
        both("msg.ec.InfoPanelNoTarget", "Target not found", "Ziel nicht gefunden");

        // ------------------------------------------------------- Ankreuzfelder
        both("msg.ec.cbInfoPanelEnergy", "Energy", "Energie");
        both("msg.ec.cbInfoPanelFree", "Free", "Frei");
        both("msg.ec.cbInfoPanelCapacity", "Capacity", "Fassung");
        both("msg.ec.cbInfoPanelPercentage", "Percentage", "Anteil");
        both("msg.ec.cbInfoPanelDifference", "Throughput", "Durchsatz");
        both("msg.ec.cbInfoPanelTankNo", "Tank %s", "Tank %s");
        both("msg.ec.cbInfoPanelStatus", "Status", "Zustand");
        both("msg.ec.cbInfoPanelReactor", "Reactor data", "Reaktordaten");
        both("msg.ec.cbInfoPanelTanks", "Tanks", "Tanks");
        both("msg.ec.cbInfoPanelProgress", "Progress", "Fortschritt");
        both("msg.ec.cbInfoPanelRadiation", "Radiation", "Strahlung");
        both("msg.ec.cbInfoPanelRor", "Radio values", "Funkwerte");
        both("msg.ec.cbInfoPanelHeat", "Heat", "Waerme");
        both("msg.ec.cbInfoPanelTurbine", "Turbine", "Turbine");
        both("msg.ec.cbInfoPanelMatrix", "Induction matrix", "Induktionsmatrix");
        both("msg.ec.cbInfoPanelMiner", "Digital miner", "Digitaler Bergmann");
        both("msg.ec.cbInfoPanelCounter", "Counter", "Zaehlerstand");
        both("msg.ec.cbInfoPanelSlots", "Slots", "Faecher");
        both("msg.ec.cbInfoPanelTotalItems", "Item count", "Anzahl");
        both("msg.ec.cbInfoPanelItemList", "Item list", "Bestandsliste");
        both("msg.ec.cbInfoPanelSignal", "Signal", "Signal");
        both("msg.ec.cbInfoPanelComparator", "Comparator", "Vergleicher");
        both("msg.ec.cbInfoPanelBlock", "Block name", "Blockname");
        both("msg.ec.cbInfoPanelBurnTime", "Burn time", "Brenndauer");
        both("msg.ec.cbInfoPanelTime", "Time", "Uhrzeit");
        both("msg.ec.cbInfoPanelDay", "Day", "Tag");
        both("msg.ec.cbInfoPanelWeather", "Weather", "Wetter");
        both("msg.ec.cbInfoPanelLineNo", "Line %s", "Zeile %s");
        both("msg.ec.cbShowLabels", "Labels", "Beschriftung");

        // ------------------------------------------------------------ Schalter
        both("msg.ec.EnergyStored", "%s / %s FE", "%s / %s FE");
        both("msg.ec.PanelRefreshRate", "Refresh", "Takt");
        both("msg.ec.Ticks", "%s tick(s)", "%s Tick(s)");
        both("msg.ec.ColorText", "Text colour", "Schriftfarbe");
        both("msg.ec.ColorBackground", "Background", "Hintergrund");
        both("msg.ec.ColorTextShort", "Font", "Schrift");
        both("msg.ec.ColorBackgroundShort", "Back", "Grund");
        both("gui.energycontrol.edit_text", "Lines", "Zeilen");
        both("gui.energycontrol.card_text", "Text card", "Textkarte");
        both("msg.ec.InvertRedstone", "Invert redstone", "Redstone umkehren");
        both("msg.ec.Thermo", "Hull heat: %s", "Huellentemperatur: %s");
        both("msg.ec.ThermalMonitorSignalAtShort", "Signal at:", "Signal ab:");
        both("msg.ec.InvertRedstoneShort", "Invert", "Umkehr");
        both("msg.ec.RangeTriggerStartShort", "Lower limit:", "Untere Grenze:");
        both("msg.ec.RangeTriggerEndShort", "Upper limit:", "Obere Grenze:");
        both("msg.ec.HowlerAlarmSound", "Sound", "Ton");
        both("msg.ec.HowlerAlarmSoundRange", "Sound range: %s", "Hoerweite: %s");
        both("msg.ec.AlarmDefault", "Default", "Standard");
        both("msg.ec.AlarmSciFi", "Sci-fi", "Science-Fiction");
        both("msg.ec.AlarmSiren", "Siren", "Sirene");

        both("subtitles.energycontrol.alarm", "Alarm howls", "Alarm heult");
    }

    private void block(net.minecraft.world.level.block.Block block, String english, String deutsch) {
        add(block, german ? deutsch : english);
    }

    private void item(net.minecraft.world.item.Item item, String english, String deutsch) {
        add(item, german ? deutsch : english);
    }
}
