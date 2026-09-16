package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blocks.KitAssemblerBlock;
import com.zuxelus.energycontrol.blocks.PanelThickness;
import com.zuxelus.energycontrol.blocks.RangeTriggerBlock;
import com.zuxelus.energycontrol.blocks.ThermalMonitorBlock;
import com.zuxelus.energycontrol.init.ECBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Function;

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
        advancedInfoPanel();
        thermalMonitor();
        remoteThermalMonitor();
        rangeTrigger();
        energyCounter();
        kitAssembler();

        alarm(ECBlocks.HOWLER_ALARM.get(), "howler_alarm",
                block("howler_alarm_side"), block("howler_alarm_face"), block("howler_alarm_back"));
        alarm(ECBlocks.INDUSTRIAL_ALARM.get(), "industrial_alarm",
                block("industrial_alarm_side"), block("industrial_alarm_face"), block("industrial_alarm_back"));
    }

    private void infoPanel() {
        ModelFile model = models().orientable("info_panel",
                block("info_panel_panel_all"), block("info_panel_panel_face"), block("info_panel_panel_all"));
        facingBlock(ECBlocks.INFO_PANEL.get(), state -> model);
        simpleBlockItem(ECBlocks.INFO_PANEL.get(), model);

        ModelFile extender = models().orientable("info_panel_extender",
                block("info_panel_extender_all"), block("info_panel_extender_face"), block("info_panel_extender_all"));
        facingBlock(ECBlocks.INFO_PANEL_EXTENDER.get(), state -> extender);
        simpleBlockItem(ECBlocks.INFO_PANEL_EXTENDER.get(), extender);
    }

    /**
     * Die fortgeschrittene Tafel gibt es in sechzehn Dicken -- je Sechzehntel eines Blocks
     * ein Modell. Erweiterung wie Tafel; welche Dicke gilt, steht im Blockzustand.
     */
    private void advancedInfoPanel() {
        ModelFile[] panels = new ModelFile[17];
        ModelFile[] extenders = new ModelFile[17];

        for(int t = 1; t <= 16; t++) {
            panels[t] = thinPanel("advanced_info_panel_" + t,
                    block("info_panel_panel_advanced_all"), block("info_panel_panel_advanced_face"), t);
            extenders[t] = thinPanel("advanced_info_panel_extender_" + t,
                    block("info_panel_extender_advanced_all"), block("info_panel_extender_advanced_face"), t);
        }

        facingBlock(ECBlocks.ADVANCED_INFO_PANEL.get(), state -> panels[state.getValue(PanelThickness.THICKNESS)]);
        simpleBlockItem(ECBlocks.ADVANCED_INFO_PANEL.get(), panels[16]);

        facingBlock(ECBlocks.ADVANCED_INFO_PANEL_EXTENDER.get(), state -> extenders[state.getValue(PanelThickness.THICKNESS)]);
        simpleBlockItem(ECBlocks.ADVANCED_INFO_PANEL_EXTENDER.get(), extenders[16]);
    }

    /**
     * Eine Tafel von {@code thickness} Sechzehnteln Dicke. Sie liegt hinten im Block, damit
     * sie an dem Block anliegt, auf den sie gesetzt wurde; die Schauseite bleibt im Norden,
     * wo {@link #facingBlock} sie erwartet.
     */
    private ModelFile thinPanel(String name, ResourceLocation all, ResourceLocation face, int thickness) {
        return models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", all)
                .texture("all", all)
                .texture("face", face)
                .element()
                    .from(0, 0, 16 - thickness).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#face").end()
                    .face(Direction.SOUTH).texture("#all").cullface(Direction.SOUTH).end()
                    .face(Direction.EAST).texture("#all").end()
                    .face(Direction.WEST).texture("#all").end()
                    .face(Direction.UP).texture("#all").end()
                    .face(Direction.DOWN).texture("#all").end()
                .end();
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
        facingBlock(ECBlocks.REMOTE_THERMAL_MONITOR.get(), state -> model);
        simpleBlockItem(ECBlocks.REMOTE_THERMAL_MONITOR.get(), model);
    }

    /** Der Zaehler gibt auf der Schauseite ab; dort steht "output", sonst "input". */
    private void energyCounter() {
        ModelFile model = models().orientable("energy_counter",
                block("energy_counter_input"), block("energy_counter_output"), block("energy_counter_input"));
        facingBlock(ECBlocks.ENERGY_COUNTER.get(), state -> model);
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

    /**
     * Die Bausatzmontage steht waagerecht wie ein Ofen; ihre Schauseite zeigt an, ob sie
     * arbeitet. Der Blockzustand traegt beides, Richtung und Betrieb.
     */
    private void kitAssembler() {
        ResourceLocation side = block("kit_assembler_all");
        ModelFile off = models().orientable("kit_assembler", side, block("kit_assembler_face"), side);
        ModelFile on = models().orientable("kit_assembler_active", side, block("kit_assembler_face_active"), side);

        getVariantBuilder(ECBlocks.KIT_ASSEMBLER.get()).forAllStates(state -> {
            int yRot = (int) state.getValue(KitAssemblerBlock.FACING).toYRot();
            return ConfiguredModel.builder()
                    .modelFile(state.getValue(KitAssemblerBlock.ACTIVE) ? on : off)
                    .rotationY((yRot + 180) % 360)
                    .build();
        });

        simpleBlockItem(ECBlocks.KIT_ASSEMBLER.get(), off);
    }

    /** Heuler und Warnleuchte teilen sich den Aufbau: Schauseite vorn, Rueckseite hinten. */
    private void alarm(Block block, String name, ResourceLocation side, ResourceLocation front, ResourceLocation back) {
        ModelFile model = models().orientable(name, side, front, back);
        facingBlock(block, state -> model);
        simpleBlockItem(block, model);
    }

    /**
     * Die Drehung eines Blocks mit sechs Blickrichtungen -- fuer Modelle, deren Schauseite
     * im Norden liegt. Genau dort legt {@code models().orientable(...)} sie ab.
     *
     * NICHT directionalBlock von NeoForge: das dreht Modelle, deren Schauseite **oben**
     * liegt (Fass, Spender-Senkrecht), und legt die Schauseite eines Nordmodells bei
     * facing=NORTH auf die Unterseite. Die Drehungen unten sind die, die Minecraft selbst
     * fuer den Ofen und den Endstab benutzt: waagerecht ueber die Y-Achse, senkrecht ueber
     * die X-Achse.
     */
    private void facingBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                    .modelFile(modelFunc.apply(state))
                    .rotationX(switch(facing) {
                        case UP -> 90;
                        case DOWN -> 270;
                        default -> 0;
                    })
                    .rotationY(facing.getAxis().isVertical() ? 0 : ((int) facing.toYRot() + 180) % 360)
                    .build();
        });
    }

    private ResourceLocation block(String texture) {
        return EnergyControl.loc("block/" + texture);
    }
}
