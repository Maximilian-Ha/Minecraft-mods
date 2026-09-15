package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JEI-Kategorie fuer die Gaszentrifuge. Im Original uebernahm das der NEI-Handler
 * GasCentrifugeRecipeHandler.
 *
 * EIN EINTRAG JE ANREICHERUNGSSTUFE, nicht je Fluid. Das ist der Unterschied zu allen anderen
 * Kategorien und der ganze Punkt: was die Maschine kann, haengt nicht am Fluid im Rohr, sondern
 * daran, wie oft es schon durch eine Zentrifuge gelaufen ist. Vier Eintraege fuer Uran
 * beschreiben die Kaskade von Natururan bis zum hochangereicherten Stoff.
 *
 * DIE ERSTE STUFE EINER KETTE ZEIGT DAS ROHRFLUID, die spaeteren nicht -- was dort hineingeht,
 * kommt nicht aus einem Rohr, sondern aus der Zentrifuge davor. Statt eines Eingangsfeldes
 * steht dort der Name der Stufe.
 *
 * ABWEICHUNG: das Original zeigt zusaetzlich, wie viele Zentrifugen eine Kette braucht. Das
 * steht hier nicht als Zahl, sondern ergibt sich aus der Kette selbst: jeder Eintrag nennt
 * seinen Nachfolger, und wer sie aneinanderreiht, hat die Laenge.
 */
public class GasCentrifugeRecipeHandler implements IRecipeCategory<GasCentrifugeRecipeHandler.GasCentJeiRecipe> {

    public static final RecipeType<GasCentJeiRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "gas_centrifuge",
            GasCentJeiRecipe.class
    );

    public static List<GasCentJeiRecipe> getRecipes() {

        List<GasCentJeiRecipe> list = new ArrayList<>();

        for(PseudoFluidType type : PseudoFluidType.types.values()) {

            ItemStack[] output = type.getOutput();
            if(output.length == 0) continue;

            /* Nur die Stufen, die eine Kette beginnen, haben ein Rohrfluid. */
            FluidType pipe = null;
            for(Map.Entry<FluidType, PseudoFluidType> entry : GasCentrifugeRecipes.fluidConversions.entrySet()) {
                if(entry.getValue() == type) { pipe = entry.getKey(); break; }
            }

            list.add(new GasCentJeiRecipe(type, pipe, output));
        }

        /* Feste Reihenfolge: erst die Kettenanfaenge, dann der Rest. Sonst stuende die Kaskade
         * in der Reihenfolge einer HashMap, also in keiner. */
        list.sort((a, b) -> {
            if((a.pipe != null) != (b.pipe != null)) return a.pipe != null ? -1 : 1;
            return a.type.name.compareTo(b.type.name);
        });

        return list;
    }

    private final IDrawable background;
    private final IDrawable icon;

    public GasCentrifugeRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_GAS_CENT.asItem());
    }

    @Override public RecipeType<GasCentJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.category.hbmsntm.gas_centrifuge"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GasCentJeiRecipe recipe, IFocusGroup focuses) {

        if(recipe.pipe != null) {
            builder.addInputSlot(20, 23)
                    .setStandardSlotBackground()
                    .addItemStack(FluidIconItem.make(new FluidStack(recipe.pipe, recipe.type.getFluidConsumed())));
        }

        if(recipe.type.getIfHighSpeed()) {
            builder.addInputSlot(20, 45)
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(NtmItems.UPGRADE_GC_SPEED.get()));
        }

        for(int i = 0; i < recipe.output.length && i < 4; i++) {
            builder.addOutputSlot(92 + i * 18, 23)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.output[i]);
        }
    }

    @Override
    public void draw(GasCentJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

        var font = Minecraft.getInstance().font;

        Component stage = recipe.type.getName().copy()
                .append(Component.literal(" -> "))
                .append(recipe.type.getOutputType() == PseudoFluidType.NONE
                        ? Component.translatable("jei.hbmsntm.gas_centrifuge.exhausted")
                        : recipe.type.getOutputType().getName());

        guiGraphics.drawString(font, stage, 83 - font.width(stage) / 2, 5, 0x404040, false);

        String amount = String.format(Locale.US, "%,d mB -> %,d mB", recipe.type.getFluidConsumed(), recipe.type.getFluidProduced());
        guiGraphics.drawString(font, amount, 83 - font.width(amount) / 2, 47, 0x404040, false);

        if(recipe.type.getIfHighSpeed()) {
            Component note = Component.translatable("jei.hbmsntm.gas_centrifuge.fast").withStyle(ChatFormatting.DARK_RED);
            guiGraphics.drawString(font, note, 83 - font.width(note) / 2, 57, 0x404040, false);
        }
    }

    public static class GasCentJeiRecipe {

        public final PseudoFluidType type;
        /** Das Rohrfluid, falls diese Stufe eine Kette beginnt; sonst null. */
        public final FluidType pipe;
        public final ItemStack[] output;

        public GasCentJeiRecipe(PseudoFluidType type, FluidType pipe, ItemStack[] output) {
            this.type = type;
            this.pipe = pipe;
            this.output = output;
        }
    }
}
