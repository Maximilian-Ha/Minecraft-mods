package com.hbm.items.special;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.IMetaItem;
import com.hbm.items.NtmItems;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.EnumUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.List;
import java.util.Locale;

import static com.hbm.inventory.material.Mats.*;
import static com.hbm.items.special.BedrockOreItem.ProcessingTrait.*;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemBedrockOreNew.
 *
 * Das Grundgesteinserz. Es gibt sechs SORTEN -- Leichtmetall, Schwermetall, Seltene Erden,
 * Aktinide, Nichtmetall, Kristallin -- und sechsundzwanzig STUFEN der Aufbereitung. Beides steckt
 * im selben Zahlenwert: Stufe mal sechzehn plus Sorte, wie im Original.
 *
 * WAS DABEI HERAUSKOMMT, HAENGT AN DER SORTE. Jede fuehrt elf Ausbeuten mit sich: zwei
 * Hauptmetalle und je drei Nebenprodukte fuer Saeure, Loesungsmittel und radioaktives
 * Loesungsmittel. Die Zahlen sind Zeile fuer Zeile die des Originals.
 *
 * ABWEICHUNG BEI DER DARSTELLUNG: das Original baut sich beim Start 156 Bilder zusammen, indem es
 * eine Graustufenvorlage je Sorte umfaerbt (TextureAtlasSpriteMutatable). Auf 1.21 gibt es diese
 * Atlas-Bastelei nicht mehr, und sie waere auch unnoetig: der Gegenstandsrenderer kann Schichten
 * einzeln einfaerben. Der Port hat daher sechs Vorlagen -- eine je Stufenpraefix -- und faerbt
 * sie ueber einen Farbgeber nach Sorte und Stufe. Das Ergebnis ist dasselbe, die Zahl der
 * Dateien nicht.
 */
public class BedrockOreItem extends Item implements IMetaItem, ICustomItemModelRegister {

    public BedrockOreItem(Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
    }

    /** Eine Ausbeute: ein Material und wie viele Bruchstuecke davon. */
    public record BedrockOreOutput(NTMMaterial mat, int amount) { }

    public static BedrockOreOutput o(NTMMaterial mat, int amount) {
        return new BedrockOreOutput(mat, amount);
    }

    public enum BedrockOreType {
        //                                      hell        dunkel      Kuerzel     primaer 1               primaer 2                   Saeure 1                    Saeure 2                    Saeure 3                    Loesung 1                   Loesung 2                   Loesung 3                   Rad 1                       Rad 2                       Rad 3
        LIGHT_METAL(  0xFFFFFF, 0x353535, "light",    o(MAT_IRON, 9),     o(MAT_COPPER, 9),   o(MAT_TITANIUM, 6),     o(MAT_BAUXITE, 9),      o(MAT_CRYOLITE, 3),     o(MAT_CHLOROCALCITE, 5),    o(MAT_LITHIUM, 5),      o(MAT_SODIUM, 3),       o(MAT_CHLOROCALCITE, 6),    o(MAT_LITHIUM, 6),      o(MAT_SODIUM, 6)),
        HEAVY_METAL(  0x868686, 0x000000, "heavy",    o(MAT_TUNGSTEN, 9), o(MAT_LEAD, 9),     o(MAT_GOLD, 2),         o(MAT_GOLD, 2),         o(MAT_BERYLLIUM, 3),    o(MAT_TUNGSTEN, 9),         o(MAT_LEAD, 9),         o(MAT_GOLD, 5),         o(MAT_BISMUTH, 2),          o(MAT_TANTALIUM, 2),    o(MAT_GOLD, 6)),
        RARE_EARTH(   0xE6E6B6, 0x1C1C00, "rare",     o(MAT_COBALT, 5),   o(MAT_RAREEARTH, 5),o(MAT_BORON, 5),        o(MAT_LANTHANIUM, 3),   o(MAT_NIOBIUM, 4),      o(MAT_NEODYMIUM, 3),        o(MAT_STRONTIUM, 3),    o(MAT_ZIRCONIUM, 3),    o(MAT_NIOBIUM, 5),          o(MAT_NEODYMIUM, 5),    o(MAT_STRONTIUM, 3)),
        ACTINIDE(     0xC1C7BD, 0x2B3227, "actinide", o(MAT_URANIUM, 4),  o(MAT_THORIUM, 4),  o(MAT_RADIUM, 2),       o(MAT_RADIUM, 2),       o(MAT_POLONIUM, 2),     o(MAT_RADIUM, 2),           o(MAT_RADIUM, 2),       o(MAT_POLONIUM, 2),     o(MAT_TECHNETIUM, 1),       o(MAT_TECHNETIUM, 1),   o(MAT_U238, 1)),
        NON_METAL(    0xAFAFAF, 0x0F0F0F, "nonmetal", o(MAT_COAL, 9),     o(MAT_SULFUR, 9),   o(MAT_LIGNITE, 9),      o(MAT_KNO, 6),          o(MAT_FLUORITE, 6),     o(MAT_PHOSPHORUS, 5),       o(MAT_FLUORITE, 6),     o(MAT_SULFUR, 6),       o(MAT_CHLOROCALCITE, 6),    o(MAT_SILICON, 2),      o(MAT_SILICON, 2)),
        CRYSTALLINE(  0xE2FFFA, 0x1E8A77, "crystal",  o(MAT_REDSTONE, 9), o(MAT_CINNABAR, 4), o(MAT_SODALITE, 9),     o(MAT_ASBESTOS, 6),     o(MAT_DIAMOND, 3),      o(MAT_CINNABAR, 3),         o(MAT_ASBESTOS, 5),     o(MAT_EMERALD, 3),      o(MAT_BORAX, 3),            o(MAT_MOLYSITE, 3),     o(MAT_SODALITE, 9));

        public final int light;
        public final int dark;
        public final String suffix;
        public final BedrockOreOutput primary1, primary2;
        public final BedrockOreOutput byproductAcid1, byproductAcid2, byproductAcid3;
        public final BedrockOreOutput byproductSolvent1, byproductSolvent2, byproductSolvent3;
        public final BedrockOreOutput byproductRad1, byproductRad2, byproductRad3;

        BedrockOreType(int light, int dark, String suffix,
                       BedrockOreOutput p1, BedrockOreOutput p2,
                       BedrockOreOutput bA1, BedrockOreOutput bA2, BedrockOreOutput bA3,
                       BedrockOreOutput bS1, BedrockOreOutput bS2, BedrockOreOutput bS3,
                       BedrockOreOutput bR1, BedrockOreOutput bR2, BedrockOreOutput bR3) {
            this.light = light;
            this.dark = dark;
            this.suffix = suffix;
            this.primary1 = p1; this.primary2 = p2;
            this.byproductAcid1 = bA1; this.byproductAcid2 = bA2; this.byproductAcid3 = bA3;
            this.byproductSolvent1 = bS1; this.byproductSolvent2 = bS2; this.byproductSolvent3 = bS3;
            this.byproductRad1 = bR1; this.byproductRad2 = bR2; this.byproductRad3 = bR3;
        }
    }

    /** Was einer Stufe schon widerfahren ist. Steht als Bildchen ueber dem Erz. */
    public enum ProcessingTrait {
        ROASTED,
        ARC,
        WASHED,
        CENTRIFUGED,
        SULFURIC,
        SOLVENT,
        RAD
    }

    public static final int NONE_TINT = 0xFFFFFF;
    public static final int ROASTED_TINT = 0xCFCFCF;
    public static final int ARC_TINT = 0xC3A2A2;
    public static final int WASHED_TINT = 0xDBE2CB;

    /** Die sechsundzwanzig Stufen. Kommentare und Farben sind die des Originals. */
    public enum BedrockOreGrade {
        BASE(NONE_TINT, "base"),                                            // aus dem Schlaemmer
        BASE_ROASTED(ROASTED_TINT, "base", ROASTED),                        // Doppelofen, liefert Vitriol
        BASE_WASHED(WASHED_TINT, "base", WASHED),                           // einfacher Saeurer mit Wasser
        PRIMARY(NONE_TINT, "primary", CENTRIFUGED),                         // Zentrifuge, mehr Hauptmetall
        PRIMARY_ROASTED(ROASTED_TINT, "primary", ROASTED),                  // Doppelofen
        PRIMARY_SULFURIC(0xFFFFD3, "primary", SULFURIC),                    // Schwefelsaeure
        PRIMARY_NOSULFURIC(0xD3D4FF, "primary", CENTRIFUGED, SULFURIC),     // Schwefelanteil ausgezogen
        PRIMARY_SOLVENT(0xD3F0FF, "primary", SOLVENT),                      // Loesungsmittel
        PRIMARY_NOSOLVENT(0xFFDED3, "primary", CENTRIFUGED, SOLVENT),       // Loesungsanteil ausgezogen
        PRIMARY_RAD(0xECFFD3, "primary", RAD),                              // radioaktives Loesungsmittel
        PRIMARY_NORAD(0xEBD3FF, "primary", CENTRIFUGED, RAD),               // Radanteil ausgezogen
        PRIMARY_FIRST(0xFFD3D4, "primary", CENTRIFUGED),                    // mehr vom ersten Material
        PRIMARY_SECOND(0xD3FFEB, "primary", CENTRIFUGED),                   // mehr vom zweiten Material
        CRUMBS(NONE_TINT, "crumbs", CENTRIFUGED),                           // Endpunkt, Wiederverwertung

        SULFURIC_BYPRODUCT(NONE_TINT, "sulfuric", CENTRIFUGED, SULFURIC),
        SULFURIC_ROASTED(ROASTED_TINT, "sulfuric", ROASTED, SULFURIC),
        SULFURIC_ARC(ARC_TINT, "sulfuric", ARC, SULFURIC),
        SULFURIC_WASHED(WASHED_TINT, "sulfuric", WASHED, SULFURIC),         // Endpunkt Schwefel

        SOLVENT_BYPRODUCT(NONE_TINT, "solvent", CENTRIFUGED, SOLVENT),
        SOLVENT_ROASTED(ROASTED_TINT, "solvent", ROASTED, SOLVENT),
        SOLVENT_ARC(ARC_TINT, "solvent", ARC, SOLVENT),
        SOLVENT_WASHED(WASHED_TINT, "solvent", WASHED, SOLVENT),            // Endpunkt Loesungsmittel

        RAD_BYPRODUCT(NONE_TINT, "rad", CENTRIFUGED, RAD),
        RAD_ROASTED(ROASTED_TINT, "rad", ROASTED, RAD),
        RAD_ARC(ARC_TINT, "rad", ARC, RAD),
        RAD_WASHED(WASHED_TINT, "rad", WASHED, RAD);                        // Endpunkt Rad

        public final int tint;
        public final String prefix;
        public final ProcessingTrait[] traits;

        BedrockOreGrade(int tint, String prefix, ProcessingTrait... traits) {
            this.tint = tint;
            this.prefix = prefix;
            this.traits = traits;
        }
    }

    /** Die Stufe steckt im oberen Halbbyte, die Sorte im unteren -- wie im Original. */
    public static ItemStack make(BedrockOreGrade grade, BedrockOreType type) { return make(grade, type, 1); }

    public static ItemStack make(BedrockOreGrade grade, BedrockOreType type, int amount) {
        return MetaHelper.newStack(NtmItems.BEDROCK_ORE.get(), amount, grade.ordinal() << 4 | type.ordinal());
    }

    public static BedrockOreGrade getGrade(int meta) { return EnumUtil.grabEnumSafely(BedrockOreGrade.class, meta >> 4); }
    public static BedrockOreType getType(int meta) { return EnumUtil.grabEnumSafely(BedrockOreType.class, meta & 15); }

    /**
     * Sortenfarbe mal Stufenton, kanalweise. Das Original faerbt die Graustufenvorlage zwischen
     * hell und dunkel der Sorte um und legt den Stufenton darueber; die Multiplikation kommt
     * demselben Ergebnis nahe genug und braucht kein einziges zusaetzliches Bild.
     */
    public static int blendTint(int typeColor, int gradeTint) {
        int r = ((typeColor >> 16 & 0xFF) * (gradeTint >> 16 & 0xFF)) / 255;
        int g = ((typeColor >> 8 & 0xFF) * (gradeTint >> 8 & 0xFF)) / 255;
        int b = ((typeColor & 0xFF) * (gradeTint & 0xFF)) / 255;
        return r << 16 | g << 8 | b;
    }

    public static BedrockOreGrade getGrade(ItemStack stack) { return getGrade(MetaHelper.getMeta(stack)); }
    public static BedrockOreType getType(ItemStack stack) { return getType(MetaHelper.getMeta(stack)); }

    /**
     * Was eine Ausbeute fluessig wiegt -- nur fuer schmelzbare Materialien, sonst nichts.
     * Der Anteil kommt von der Stufe: eine halb ausgezogene Ladung liefert weniger.
     */
    public static MaterialStack toFluid(BedrockOreOutput o, double amount) {
        if(o.mat() != null && o.mat().smeltable == SmeltingBehavior.SMELTABLE) {
            return new MaterialStack(o.mat(), (int) Math.ceil(MaterialShapes.FRAGMENT.q(o.amount()) * amount));
        }
        return null;
    }

    /** Dieselbe Ausbeute als Bruchstuecke. Mehr als ein Stapel wird nicht daraus. */
    public static ItemStack extract(BedrockOreOutput o, double amount) {
        return MetaHelper.newStack(NtmItems.BEDROCK_ORE_FRAGMENT.get(), Math.min((int) Math.ceil(o.amount() * amount), 64), o.mat().id);
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(BedrockOreType type : BedrockOreType.values()) {
            for(BedrockOreGrade grade : BedrockOreGrade.values()) {
                stacks.add(make(grade, type));
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        int meta = MetaHelper.getMeta(stack);
        Component type = Component.translatable(this.getDescriptionId() + ".type." + getType(meta).suffix);
        return Component.translatable(this.getDescriptionId() + ".grade." + getGrade(meta).name().toLowerCase(Locale.US), type);
    }

    /**
     * Sechsundzwanzig Modelle, eines je Stufe -- nicht 156.
     *
     * Der Zahlenwert ist Stufe mal sechzehn plus Sorte, also liegen alle Sorten einer Stufe
     * zwischen g*16 und g*16+15. Ein Schwellenvergleich auf g*16 trifft damit genau die Stufe:
     * die hoechste passende Schwelle gewinnt. Die Sorte macht kein eigenes Modell noetig, weil
     * sie ueber den Farbgeber kommt.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {

        ItemModelBuilder builder = provider.getBuilder(modelLocation.toString());

        for(BedrockOreGrade grade : BedrockOreGrade.values()) {

            ItemModelBuilder model = provider.getBuilder(modelLocation.getPath() + "_" + grade.ordinal())
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "item/" + modelLocation.getPath()));

            /* Je Merkmal ein Bildchen darueber -- hoechstens zwei, wie die Stufenliste zeigt. */
            for(int i = 0; i < grade.traits.length; i++) {
                model.texture("layer" + (i + 1), ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(),
                        "item/" + modelLocation.getPath() + "_overlay." + grade.traits[i].name().toLowerCase(Locale.US)));
            }

            builder.override()
                    .predicate(NuclearTechMod.withDefaultNamespace("item_meta"), grade.ordinal() * 16)
                    .model(model)
                    .end();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        for(ProcessingTrait trait : getGrade(stack).traits) {
            components.add(Component.translatable(this.getDescriptionId() + ".trait." + trait.name().toLowerCase(Locale.US)));
        }
    }
}
