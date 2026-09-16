package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blocks.RangeTriggerBlock;
import com.zuxelus.energycontrol.blocks.ThermalMonitorBlock;
import com.zuxelus.energycontrol.init.ECBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Blockzustaende und Blockmodelle.
 *
 * Alle Texturnamen stehen hier als Zeichenkette in einem {@code block(...)}-Aufruf --
 * nie zusammengesetzt. Nur so kann tools/asset-check.sh ohne Bau nachzaehlen, ob jede
 * angesprochene Textur da ist und keine ungenutzt herumliegt.
 */
public class ECBlockStateProvider extends BlockStateProvider {

    public ECBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, EnergyControl.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        infoPanel();
        thermalMonitor();
        remoteThermalMonitor();
        rangeTrigger();
        energyCounter();

        alarm(ECBlocks.HOWLER_ALARM.get(), "howler_alarm",
                block("howler_alarm_side"), block("howler_alarm_face"), block("howler_alarm_back"));
        alarm(ECBlocks.INDUSTRIAL_ALARM.get(), "industrial_alarm",
                block("industrial_alarm_side"), block("industrial_alarm_face"), block("industrial_alarm_back"));
    }

    private void infoPanel() {
        ModelFile model = models().orientable("info_panel",
                block("info_panel_panel_all"), block("info_panel_panel_face"), block("info_panel_panel_all"));
        directionalBlock(ECBlocks.INFO_PANEL.get(), state -> model);
        simpleBlockItem(ECBlocks.INFO_PANEL.get(), model);

        ModelFile extender = models().orientable("info_panel_extender",
                block("info_panel_extender_all"), block("info_panel_extender_face"), block("info_panel_extender_all"));
        directionalBlock(ECBlocks.INFO_PANEL_EXTENDER.get(), state -> extender);
        simpleBlockItem(ECBlocks.INFO_PANEL_EXTENDER.get(), extender);
    }

    /**
     * Der Waermemelder sieht von allen Seiten gleich aus; sein Zustand steckt in der
     * Textur, nicht in der Ausrichtung.
     */
    private void thermalMonitor() {
        ModelFile[] models = {
                models().cubeAll("thermal_monitor_0", block("thermal_monitor_all0")),
                models().cubeAll("thermal_monitor_1", block("thermal_monitor_all1")),
                models().cubeAll("thermal_monitor_2", block("thermal_monitor_all2"))
        };

        getVariantBuilder(ECBlocks.THERMAL_MONITOR.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(models[state.getValue(ThermalMonitorBlock.STATUS)])
                        .build());

        simpleBlockItem(ECBlocks.THERMAL_MONITOR.get(), models[0]);
    }

    /**
     * Die Fernwaermeanzeige traegt ihren Zustand nicht in der Textur -- das Original hat
     * dafuer nur zwei Bilder. Die Schauseite zeigt dorthin, wo der Block gesetzt wurde.
     */
    private void remoteThermalMonitor() {
        ModelFile model = models().orientable("remote_thermal_monitor",
                block("remote_thermal_monitor_all"), block("remote_thermal_monitor_face"), block("remote_thermal_monitor_all"));
        directionalBlock(ECBlocks.REMOTE_THERMAL_MONITOR.get(), state -> model);
        simpleBlockItem(ECBlocks.REMOTE_THERMAL_MONITOR.get(), model);
    }

    /** Der Zaehler gibt auf der Schauseite ab; dort steht "output", sonst "input". */
    private void energyCounter() {
        ModelFile model = models().orientable("energy_counter",
                block("energy_counter_input"), block("energy_counter_output"), block("energy_counter_input"));
        directionalBlock(ECBlocks.ENERGY_COUNTER.get(), state -> model);
        simpleBlockItem(ECBlocks.ENERGY_COUNTER.get(), model);
    }

    private void rangeTrigger() {
        ResourceLocation side = block("range_trigger_all");
        ModelFile[] models = {
                models().orientable("range_trigger_0", side, block("range_trigger_face_gray"), side),
                models().orientable("range_trigger_1", side, block("range_trigger_face_green"), side),
                models().orientable("range_trigger_2", side, block("range_trigger_face_red"), side)
        };

        getVariantBuilder(ECBlocks.RANGE_TRIGGER.get()).forAllStates(state -> {
            int yRot = (int) state.getValue(RangeTriggerBlock.FACING).toYRot();
            return ConfiguredModel.builder()
                    .modelFile(models[state.getValue(RangeTriggerBlock.STATUS)])
                    .rotationY((yRot + 180) % 360)
                    .build();
        });

        simpleBlockItem(ECBlocks.RANGE_TRIGGER.get(), models[0]);
    }

    /** Heuler und Warnleuchte teilen sich den Aufbau: Schauseite vorn, Rueckseite hinten. */
    private void alarm(Block block, String name, ResourceLocation side, ResourceLocation front, ResourceLocation back) {
        ModelFile model = models().orientable(name, side, front, back);
        directionalBlock(block, state -> model);
        simpleBlockItem(block, model);
    }

    private ResourceLocation block(String texture) {
        return EnergyControl.loc("block/" + texture);
    }
}
