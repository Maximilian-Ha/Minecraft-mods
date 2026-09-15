package com.hbm.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.machine.*;
import com.hbm.blockentity.machine.fusion.FusionMHDTBlockEntity;
import com.hbm.blockentity.machine.boiler.MachineHeatBoilerBlockEntity;
import com.hbm.blockentity.machine.boiler.MachineIndustrialBoilerBlockEntity;
import com.hbm.blockentity.machine.heater.HeaterFireboxBlockEntity;
import com.hbm.blockentity.machine.heater.HeaterOvenBlockEntity;
import com.hbm.blockentity.machine.oil.MachineFrackingTowerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineOilWellBlockEntity;
import com.hbm.blockentity.machine.oil.MachinePumpjackBlockEntity;
import com.hbm.blockentity.network.ConverterHeRfBlockEntity;
import com.hbm.blockentity.network.ConverterRfHeBlockEntity;
import com.hbm.config.IConfigurableMachine.ConfigWriter;
import com.hbm.main.NuclearTechMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.config.MachineDynConfig.
 *
 * Erzeugt hbmConfig/hbmMachines.json und liest sie wieder ein. Der Ablauf ist der des
 * Originals und beim Bearbeiten der Datei wichtig zu wissen:
 *
 * 1. Die Maschinen tragen ihre Standardwerte fest im Quelltext.
 * 2. Existiert die Datei, werden vorhandene Werte daraus uebernommen; was fehlt, bleibt
 *    auf dem Standardwert.
 * 3. Danach wird die Datei vollstaendig neu geschrieben. Fehlende Eintraege kommen also von
 *    selbst dazu, geaenderte bleiben erhalten -- und wer einen Wert zuruecksetzen will,
 *    loescht ihn einfach.
 *
 * Punkt 3 heisst auch: eigene, nicht vorgesehene Eintraege in der Datei verschwinden beim
 * naechsten Start wieder.
 *
 * Abweichung vom Original: dort sammelt MachineDynConfig die Maschinen ueber die
 * TileEntity-Registry ein und legt von jeder eine Wegwerf-Instanz an. Auf 1.21 hat ein
 * BlockEntity keinen parameterlosen Konstruktor mehr; da die Konfigurationswerte ohnehin
 * statisch sind, braucht es die Instanz auch nicht. Stattdessen steht hier eine ausdrueckliche
 * Liste. Sie ist die einzige Stelle, an die eine neue konfigurierbare Maschine eingetragen
 * werden muss.
 */
public class MachineDynConfig {

    public static final Gson GSON = new Gson();

    private record Entry(String name, Consumer<JsonObject> read, ConfigWriter write) { }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    /**
     * @param name  der Name des JSON-Objekts, identisch mit dem des Originals
     * @param read  uebernimmt vorhandene Werte, laesst fehlende auf dem Standardwert
     * @param write schreibt saemtliche Werte der Maschine
     */
    public static void register(String name, Consumer<JsonObject> read, ConfigWriter write) {
        ENTRIES.add(new Entry(name, read, write));
    }

    /**
     * Noch nicht dabei, jeweils mit konkretem Grund:
     *
     * - Maschinen, die es im Port noch gar nicht gibt: Drehrohrofen, Tiegel, Kraftfeld,
     *   MHD-Turbine, ICF-Steuerung, die beiden RF-Wandler, Industrieturbine, grosses Radar,
     *   Kuehltuerme und die Wasserpumpe.
     */
    private static void registerAll() {
        ENTRIES.clear();

        register("ashpit", AshpitBlockEntity::readConfig, AshpitBlockEntity::writeConfig);
        register("mhd-turbine", FusionMHDTBlockEntity::readConfig, FusionMHDTBlockEntity::writeConfig);
        register("boiler", MachineHeatBoilerBlockEntity::readConfig, MachineHeatBoilerBlockEntity::writeConfig);
        register("boilerIndustrial", MachineIndustrialBoilerBlockEntity::readConfig, MachineIndustrialBoilerBlockEntity::writeConfig);
        register("centrifuge", MachineCentrifugeBlockEntity::readConfig, MachineCentrifugeBlockEntity::writeConfig);
        register("crucible", MachineCrucibleBlockEntity::readConfig, MachineCrucibleBlockEntity::writeConfig);
        register("rotaryfurnace", MachineRotaryFurnaceBlockEntity::readConfig, MachineRotaryFurnaceBlockEntity::writeConfig);
        register("condenser", CondenserBaseBlockEntity::readConfig, CondenserBaseBlockEntity::writeConfig);
        register("condenserPowered", CondenserPoweredBlockEntity::readConfig, CondenserPoweredBlockEntity::writeConfig);
        register("derrick", MachineOilWellBlockEntity::readConfig, MachineOilWellBlockEntity::writeConfig);
        register("dieselgen", MachineDieselBlockEntity::readConfig, MachineDieselBlockEntity::writeConfig);
        register("firebox", HeaterFireboxBlockEntity::readConfig, HeaterFireboxBlockEntity::writeConfig);
        register("frackingtower", MachineFrackingTowerBlockEntity::readConfig, MachineFrackingTowerBlockEntity::writeConfig);
        register("HEToRFConverter", ConverterHeRfBlockEntity::readConfig, ConverterHeRfBlockEntity::writeConfig);
        register("heatingoven", HeaterOvenBlockEntity::readConfig, HeaterOvenBlockEntity::writeConfig);
        register("pumpjack", MachinePumpjackBlockEntity::readConfig, MachinePumpjackBlockEntity::writeConfig);
        register("radar", MachineRadarBlockEntity::readConfig, MachineRadarBlockEntity::writeConfig);
        register("radar_large", MachineRadarLargeBlockEntity::readConfig, MachineRadarLargeBlockEntity::writeConfig);
        register("RFToHEConverter", ConverterRfHeBlockEntity::readConfig, ConverterRfHeBlockEntity::writeConfig);
        register("steamengine", MachineSteamEngineBlockEntity::readConfig, MachineSteamEngineBlockEntity::writeConfig);
        register("steamturbine", MachineTurbineBlockEntity::readConfig, MachineTurbineBlockEntity::writeConfig);
        register("steamturbineLeviathan", ChungusBlockEntity::readConfig, ChungusBlockEntity::writeConfig);
        register("steamturbineIndustrialMk2", MachineIndustrialTurbineBlockEntity::readConfig, MachineIndustrialTurbineBlockEntity::writeConfig);
        register("stirling", MachineStirlingBlockEntity::readConfig, MachineStirlingBlockEntity::writeConfig);
    }

    public static void initialize() {
        registerAll();

        File dir = NuclearTechMod.configHbmDir;

        if(!dir.exists() && !dir.mkdirs()) {
            NuclearTechMod.LOGGER.error("Konnte das Konfigurationsverzeichnis nicht anlegen: {}", dir.getAbsolutePath());
            return;
        }

        File file = new File(dir, "hbmMachines.json");

        if(file.exists()) {
            try(FileReader reader = new FileReader(file)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);

                if(json != null) {
                    for(Entry entry : ENTRIES) {
                        // Je Maschine ein eigener Fangblock, damit ein kaputter Eintrag nicht
                        // alle uebrigen mitreisst -- so haelt es auch das Original.
                        try {
                            JsonElement element = json.get(entry.name());
                            entry.read().accept(element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject());
                        } catch(Exception ex) {
                            NuclearTechMod.LOGGER.error("Konfiguration der Maschine '{}' ist fehlerhaft, benutze Standardwerte.", entry.name(), ex);
                        }
                    }
                }
            } catch(Exception ex) {
                NuclearTechMod.LOGGER.error("hbmMachines.json liess sich nicht lesen, benutze Standardwerte.", ex);
            }
        }

        try(JsonWriter writer = new JsonWriter(new FileWriter(file))) {
            writer.setIndent("  ");
            writer.beginObject();

            writer.name("info").beginArray();
            for(String line : getComment()) writer.value(line);
            writer.endArray();

            for(Entry entry : ENTRIES) {
                try {
                    writer.name(entry.name()).beginObject();
                    entry.write().write(writer);
                    writer.endObject();
                } catch(Exception ex) {
                    NuclearTechMod.LOGGER.error("Konfiguration der Maschine '{}' liess sich nicht schreiben.", entry.name(), ex);
                }
            }

            writer.endObject();
        } catch(Exception ex) {
            NuclearTechMod.LOGGER.error("hbmMachines.json liess sich nicht schreiben.", ex);
        }
    }

    private static String[] getComment() {
        return new String[] {
                "Unlike other JSON configs, this one does not use a variable amount of options (like recipes), rather all config options are fixed.",
                "This means that there is no distinction between template and used config, you can simply edit this file and it will use the new values.",
                "If you wish to reset one or multiple values to default, simply delete them, the file is re-created every time the game starts (but changed values persist!)",
                "How this works in detail:",
                "- Machines have default values on init",
                "- The config system will try to read the config file. It will replace the default values where applicable, and keep them when an option is missing.",
                "- The config system will then use the full set of values - configured or default if missing - and re-create the config file to include any missing entries.",
                "This final step also means that any custom non-config values added to the JSON, while not causing errors, will be deleted when the config is re-created.",
                "It also means that should an update add more values to an existing machines, those will be retroactively added to the config using the default value."
        };
    }
}
