package com.hbm.blocks.generic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.CastPlateItem;
import com.hbm.items.NtmItems;
import com.hbm.util.InventoryUtil;
import com.hbm.util.Tuple.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockToolConversion.
 *
 * Ein Bauteil, das sich mit dem passenden Werkzeug und etwas Material zur naechsten Baustufe
 * umbauen laesst -- so werden im Original die Wandsegmente von Watz, Fusionsanlage und ICF
 * zusammengesetzt. Wer mit dem Werkzeug draufsieht, bekommt die Anforderungen eingeblendet.
 *
 * ABWEICHUNG: das Original haelt die Baustufen in den Blockmetadaten und schlaegt ueber
 * MetaBlock nach. In 1.21 ist die Baustufe eine Blockzustands-Eigenschaft (STAGE); der
 * Umbaukatalog schlaegt daher ueber (Werkzeug, Block, Stufe) nach.
 *
 * Alle drei Bauteile des Originals stehen im Katalog: watz_end, fusion_component und
 * icf_component. Bis Runde 198 stand hier, es sei nur watz_end -- das war seit der
 * Fusionsanlage und dem ICF ueberholt.
 */
public class ToolConversionBlock extends Block implements IToolable, ILookOverlay {

    /** Die Baustufe. Null ist der Rohzustand, jede weitere ein Umbauschritt. */
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 7);

    public static final MapCodec<ToolConversionBlock> CODEC = simpleCodec(ToolConversionBlock::new);

    /** Wie viele Baustufen dieser Block kennt. Wird beim Anlegen der Zustaende gebraucht. */
    private final int stages;

    public ToolConversionBlock(Properties properties) {
        this(properties, 8);
    }

    public ToolConversionBlock(Properties properties, int stages) {
        super(properties);
        this.stages = stages;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends ToolConversionBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    /**
     * Ein Bauteil, das schon als fertige Stufe aus der Maschine kommt, wird auch als diese Stufe
     * gesetzt. Ohne das faellt etwa die Reaktordecke beim Platzieren auf die Rohstufe zurueck --
     * und liesse sich dann zu etwas ganz anderem umbauen.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int meta = MetaHelper.getMeta(context.getItemInHand());
        return this.defaultBlockState().setValue(STAGE, Math.max(0, Math.min(meta, this.stages - 1)));
    }

    public int getStages() { return this.stages; }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(level.isClientSide) return false;

        BlockState state = level.getBlockState(pos);
        Conversion result = CONVERSIONS.get(new Key(tool, this, state.getValue(STAGE)));
        if(result == null) return false;

        List<AStack> required = new ArrayList<>(List.of(result.requirements));

        if(required.isEmpty() || InventoryUtil.doesPlayerHaveAStacks(player, required, true)) {
            level.setBlock(pos, state.setValue(STAGE, result.stage), 3);
            return true;
        }

        return false;
    }

    /** Blendet ein, welches Werkzeug und welches Material der naechste Umbauschritt braucht. */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        ItemStack held = player.getMainHandItem();
        if(held.isEmpty()) return;

        ToolType tool = ToolType.getType(held);
        if(tool == null) return;

        BlockState state = level.getBlockState(pos);
        Conversion result = CONVERSIONS.get(new Key(tool, this, state.getValue(STAGE)));
        if(result == null || result.requirements.length == 0) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal("Requires:").withStyle(ChatFormatting.GOLD));

        List<ItemStack> tools = tool.stacksForDisplay;
        if(!tools.isEmpty()) {
            // Das Original blaettert im Sekundentakt durch die Werkzeuge, die passen.
            ItemStack displayTool = tools.get((int) (Math.abs(level.getGameTime() / 20) % tools.size()));
            text.add(Component.literal("- ").append(displayTool.getHoverName()).withStyle(ChatFormatting.BLUE));
        }

        for(AStack stack : result.requirements) {
            ItemStack display = stack.extractForCyclingDisplay(20);
            if(display.isEmpty()) {
                text.add(Component.literal("- ERROR").withStyle(ChatFormatting.RED));
            } else {
                text.add(Component.literal("- ").append(display.getHoverName()).append(" x" + display.getCount()));
            }
        }

        ILookOverlay.printGeneric(event, state.getBlock().getName(), 0xffff00, 0x404000, text);
    }

    /* Der Umbaukatalog. */

    /** Schluessel: welches Werkzeug an welchem Block in welcher Baustufe. */
    public record Key(ToolType tool, Block block, int stage) { }

    /** Ergebnis: was es kostet und welche Baustufe danach steht. */
    public record Conversion(AStack[] requirements, int stage) { }

    public static final Map<Key, Conversion> CONVERSIONS = new HashMap<>();

    public static void register(ToolType tool, Block block, int from, int to, AStack... requirements) {
        CONVERSIONS.put(new Key(tool, block, from), new Conversion(requirements, to));
    }

    /** Traegt die Umbauschritte des Originals ein. Wird beim Rezept-Aufbau aufgerufen. */
    public static void registerRecipes() {

        CONVERSIONS.clear();

        /* Die Aussenwand des Watz wird mit der Bolzenpistole verschraubt. */
        register(ToolType.BOLT, NtmBlocks.WATZ_END.get(), 0, 1,
                new TagStack(MaterialShapes.BOLT.getTag(Mats.MAT_DURA), 4));

        /* Die BSCCO-Spulen der Fusion werden mit dem Schweissbrenner verschweisst. */
        register(ToolType.TORCH, NtmBlocks.FUSION_COMPONENT.get(), 0, 1,
                NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 1));

        /* Das Kammerbauteil des ICF: erst das Gefaess verschweissen, dann die Struktur verschrauben. */
        register(ToolType.TORCH, NtmBlocks.ICF_COMPONENT.get(), 1, 2,
                NtmItems.castPlateIngredient(CastPlateItem.Type.BISMUTH_BRONZE, 1));
        register(ToolType.BOLT, NtmBlocks.ICF_COMPONENT.get(), 3, 4,
                NtmItems.castPlateIngredient(CastPlateItem.Type.STEEL, 1),
                new TagStack(MaterialShapes.BOLT.getTag(Mats.MAT_DURA), 4));
    }

    /** Die Sorte, die oben und unten eine eigene Textur je Baustufe traegt (Original: Pillar). */
    public static class Pillar extends ToolConversionBlock {

        public static final MapCodec<Pillar> PILLAR_CODEC = simpleCodec(Pillar::new);

        public Pillar(Properties properties) { super(properties); }
        public Pillar(Properties properties, int stages) { super(properties, stages); }

        @Override
        protected MapCodec<? extends Pillar> codec() { return PILLAR_CODEC; }
    }
}
