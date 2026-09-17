package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blocks.KitAssemblerBlock;
import com.zuxelus.energycontrol.blocks.PanelThickness;
import com.zuxelus.energycontrol.blocks.RangeTriggerBlock;
import com.zuxelus.energycontrol.blocks.ThermalMonitorBlock;
import com.zuxelus.energycontrol.init.ECBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder.FaceRotation;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Function;

/**
 * Blockzustaende und Blockmodelle.
 *
 * WIE DIE TEXTUREN AUFGEBAUT SIND. Die Dateien {@code *_all.png} des Originals sind keine
 * einzelnen Kacheln, sondern 128x128 grosse **Abwicklungen** eines Wuerfels: fuenf Kacheln
 * zu je 32x32 in Kreuzform, das Feld in der Mitte bleibt frei -- dort sitzt die Schauseite,
 * die als eigene Datei {@code *_face.png} danebenliegt.
 *
 * Wer so eine Abwicklung wie eine gewoehnliche Blocktextur benutzt, bekommt das ganze Kreuz
 * auf jede Flaeche gequetscht: schwarz-weisse Karos statt eines Geraets. Deshalb stehen die
 * Modelle hier als eigene Kaesten mit ausdruecklichen Bildausschnitten, genau wie im
 * Original ({@code full_box}, {@code medium_box}, {@code small_box}).
 *
 * WIE GEDREHT WIRD. Zwei Bauformen, zwei Konventionen:
 *
 * - {@link #fullBox} legt die Schauseite in den **Norden**. Die Drehung dafuer rechnet
 *   {@link #facingBlock} aus.
 * - {@link #mediumBox} und {@link #smallBox} legen sie nach **oben** -- das sind flache
 *   Kaesten, die auf dem Boden liegen. Fuer die passt {@code directionalBlock} von
 *   NeoForge, dessen Drehungen genau die des Originals sind (Vorgabewinkel 180 Grad).
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
        ModelFile model = fullBox("info_panel",
                block("info_panel_panel_face"), block("info_panel_panel_all"));
        facingBlock(ECBlocks.INFO_PANEL.get(), state -> model);
        simpleBlockItem(ECBlocks.INFO_PANEL.get(), model);

        ModelFile extender = fullBox("info_panel_extender",
                block("info_panel_extender_face"), block("info_panel_extender_all"));
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
            panels[t] = thinBox("advanced_info_panel_" + t,
                    block("info_panel_panel_advanced_face"), block("info_panel_panel_advanced_all"), t);
            extenders[t] = thinBox("advanced_info_panel_extender_" + t,
                    block("info_panel_extender_advanced_face"), block("info_panel_extender_advanced_all"), t);
        }

        facingBlock(ECBlocks.ADVANCED_INFO_PANEL.get(), state -> panels[state.getValue(PanelThickness.THICKNESS)]);
        simpleBlockItem(ECBlocks.ADVANCED_INFO_PANEL.get(), panels[16]);

        facingBlock(ECBlocks.ADVANCED_INFO_PANEL_EXTENDER.get(), state -> extenders[state.getValue(PanelThickness.THICKNESS)]);
        simpleBlockItem(ECBlocks.ADVANCED_INFO_PANEL_EXTENDER.get(), extenders[16]);
    }

    /**
     * Der Waermemelder ist ein flacher Kasten auf dem Boden: oben die Schauseite, ringsum
     * die Abwicklung. Sein Zustand steckt in der Abwicklung -- all0 bis all2.
     */
    private void thermalMonitor() {
        ResourceLocation face = block("thermal_monitor_face_green");
        ModelFile[] models = {
                mediumBox("thermal_monitor_0", face, block("thermal_monitor_all0")),
                mediumBox("thermal_monitor_1", face, block("thermal_monitor_all1")),
                mediumBox("thermal_monitor_2", face, block("thermal_monitor_all2"))
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
        ModelFile model = fullBox("remote_thermal_monitor",
                block("remote_thermal_monitor_face"), block("remote_thermal_monitor_all"));
        facingBlock(ECBlocks.REMOTE_THERMAL_MONITOR.get(), state -> model);
        simpleBlockItem(ECBlocks.REMOTE_THERMAL_MONITOR.get(), model);
    }

    /**
     * Der Zaehler ist ein voller Wuerfel mit zwei gewoehnlichen Kacheln: er gibt auf der
     * Schauseite ab ("output") und nimmt ringsum an ("input"). Die Schauseite liegt im
     * Modell oben, deshalb dreht ihn directionalBlock.
     */
    private void energyCounter() {
        ResourceLocation input = block("energy_counter_input");
        ResourceLocation output = block("energy_counter_output");

        ModelFile model = models().withExistingParent("energy_counter", "block/cube")
                .texture("particle", input)
                .texture("down", input)
                .texture("up", output)
                .texture("north", input)
                .texture("south", input)
                .texture("east", input)
                .texture("west", input);

        directionalBlock(ECBlocks.ENERGY_COUNTER.get(), model);
        simpleBlockItem(ECBlocks.ENERGY_COUNTER.get(), model);
    }

    private void rangeTrigger() {
        ResourceLocation all = block("range_trigger_all");
        ModelFile[] models = {
                fullBox("range_trigger_0", block("range_trigger_face_gray"), all),
                fullBox("range_trigger_1", block("range_trigger_face_green"), all),
                fullBox("range_trigger_2", block("range_trigger_face_red"), all)
        };

        getVariantBuilder(ECBlocks.RANGE_TRIGGER.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(models[state.getValue(RangeTriggerBlock.STATUS)])
                        .rotationY(yaw(state.getValue(RangeTriggerBlock.FACING)))
                        .build());

        simpleBlockItem(ECBlocks.RANGE_TRIGGER.get(), models[0]);
    }

    /**
     * Die Bausatzmontage steht waagerecht wie ein Ofen; ihre Schauseite zeigt an, ob sie
     * arbeitet. Der Blockzustand traegt beides, Richtung und Betrieb.
     */
    private void kitAssembler() {
        ResourceLocation all = block("kit_assembler_all");
        ModelFile off = fullBox("kit_assembler", block("kit_assembler_face"), all);
        ModelFile on = fullBox("kit_assembler_active", block("kit_assembler_face_active"), all);

        getVariantBuilder(ECBlocks.KIT_ASSEMBLER.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(state.getValue(KitAssemblerBlock.ACTIVE) ? on : off)
                        .rotationY(yaw(state.getValue(KitAssemblerBlock.FACING)))
                        .build());

        simpleBlockItem(ECBlocks.KIT_ASSEMBLER.get(), off);
    }

    /**
     * Heuler und Warnleuchte sind flache Kaesten mit eigenen Kacheln -- keine Abwicklung,
     * je ein Bild fuer Seite, Schauseite und Rueckseite.
     */
    private void alarm(Block block, String name, ResourceLocation side, ResourceLocation face, ResourceLocation back) {
        ModelFile model = smallBox(name, side, face, back);
        directionalBlock(block, model);
        simpleBlockItem(block, model);
    }

    // ------------------------------------------------------------------ Bauformen

    /**
     * Portiert aus 1.12.2: models/block/full_box.json.
     *
     * Ein voller Wuerfel: die Schauseite im Norden als eigene Kachel, die uebrigen fuenf
     * Flaechen als Ausschnitte der Abwicklung. Die Ausschnitte sind die des Originals.
     */
    private ModelFile fullBox(String name, ResourceLocation face, ResourceLocation all) {
        return box(name, face, all, 0F);
    }

    /**
     * Dieselbe Bauform, nur duenner: die Tafel liegt hinten im Block, damit sie an dem
     * Block anliegt, auf den sie gesetzt wurde. Die Schauseite bleibt im Norden.
     */
    private ModelFile thinBox(String name, ResourceLocation face, ResourceLocation all, int thickness) {
        return box(name, face, all, 16F - thickness);
    }

    private ModelFile box(String name, ResourceLocation face, ResourceLocation all, float back) {
        return models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", face)
                .texture("face", face)
                .texture("all", all)
                .element()
                    .from(0, 0, back).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#face").uvs(0, 0, 16, 16).end()
                    .face(Direction.EAST).texture("#all").uvs(0, 4, 4, 8).end()
                    .face(Direction.SOUTH).texture("#all").uvs(12, 4, 16, 8).cullface(Direction.SOUTH).end()
                    .face(Direction.WEST).texture("#all").uvs(8, 4, 12, 8).end()
                    .face(Direction.UP).texture("#all").uvs(4, 0, 8, 4).rotation(FaceRotation.UPSIDE_DOWN).end()
                    .face(Direction.DOWN).texture("#all").uvs(4, 8, 8, 12).rotation(FaceRotation.UPSIDE_DOWN).end()
                .end();
    }

    /**
     * Portiert aus 1.12.2: models/block/medium_box.json.
     *
     * Ein flacher Kasten mit der Schauseite oben -- so steht der Waermemelder auf dem Boden.
     */
    private ModelFile mediumBox(String name, ResourceLocation face, ResourceLocation all) {
        return models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", face)
                .texture("face", face)
                .texture("all", all)
                .element()
                    .from(1, 0, 1).to(15, 7, 15)
                    .face(Direction.NORTH).texture("#all").uvs(1.75F, 5.25F, 5.25F, 7F).end()
                    .face(Direction.EAST).texture("#all").uvs(0, 1.75F, 1.75F, 5.25F).rotation(FaceRotation.COUNTERCLOCKWISE_90).end()
                    .face(Direction.SOUTH).texture("#all").uvs(1.75F, 0, 5.25F, 1.75F).rotation(FaceRotation.UPSIDE_DOWN).end()
                    .face(Direction.WEST).texture("#all").uvs(5.25F, 1.75F, 7F, 5.25F).rotation(FaceRotation.CLOCKWISE_90).end()
                    .face(Direction.UP).texture("#face").uvs(1, 1, 15, 15).end()
                    .face(Direction.DOWN).texture("#all").uvs(7F, 1.75F, 10.5F, 5.25F).cullface(Direction.DOWN).end()
                .end();
    }

    /**
     * Portiert aus 1.12.2: models/block/small_box.json.
     *
     * Der kleinere flache Kasten der beiden Alarme, ebenfalls mit der Schauseite oben.
     */
    private ModelFile smallBox(String name, ResourceLocation side, ResourceLocation face, ResourceLocation back) {
        return models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", back)
                .texture("side", side)
                .texture("face", face)
                .texture("back", back)
                .element()
                    .from(2, 0, 2).to(14, 7, 14)
                    .face(Direction.NORTH).texture("#side").uvs(2, 4, 14, 11).end()
                    .face(Direction.EAST).texture("#side").uvs(2, 4, 14, 11).end()
                    .face(Direction.SOUTH).texture("#side").uvs(2, 4, 14, 11).end()
                    .face(Direction.WEST).texture("#side").uvs(2, 4, 14, 11).end()
                    .face(Direction.UP).texture("#face").uvs(2, 2, 14, 14).end()
                    .face(Direction.DOWN).texture("#back").uvs(2, 2, 14, 14).cullface(Direction.DOWN).end()
                .end();
    }

    // -------------------------------------------------------------------- Drehung

    /**
     * Die Drehung eines Blocks mit sechs Blickrichtungen -- fuer Modelle, deren Schauseite
     * im **Norden** liegt, also fuer {@link #fullBox}.
     *
     * NICHT directionalBlock von NeoForge: das legt bei waagerechter Blickrichtung ein x
     * von 90 Grad an, was zu einem Modell mit der Schauseite **oben** passt (Fass,
     * Spender-Senkrecht), nicht zu einem Nordmodell. Waagerecht ist die Rechnung dieselbe
     * (Vorgabewinkel 180 Grad, wie beim Ofen), senkrecht dreht dieses hier um die X-Achse
     * in die andere Richtung: x=270 hebt die Nordflaeche nach oben, x=90 legt sie nach unten.
     */
    private void facingBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                    .modelFile(modelFunc.apply(state))
                    .rotationX(switch(facing) {
                        case UP -> 270;
                        case DOWN -> 90;
                        default -> 0;
                    })
                    .rotationY(facing.getAxis().isVertical() ? 0 : yaw(facing))
                    .build();
        });
    }

    /** Waagerechte Drehung eines Nordmodells: Norden bleibt Norden. */
    private static int yaw(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }

    private ResourceLocation block(String texture) {
        return EnergyControl.loc("block/" + texture);
    }
}
