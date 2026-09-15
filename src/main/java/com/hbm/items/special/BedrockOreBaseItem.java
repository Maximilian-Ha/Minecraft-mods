package com.hbm.items.special;

import com.hbm.items.special.BedrockOreItem.BedrockOreType;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemBedrockOreBase.
 *
 * Die Rohprobe. Der Bagger holt sie aus dem Grundgestein, und sie merkt sich, WO sie herkam --
 * genauer: wie viel von jeder der sechs Sorten an dieser Stelle im Boden steckt. Die Zahlen
 * stehen im Datenanhang, je eine Kommazahl unter dem Kuerzel der Sorte.
 *
 * DIE VERTEILUNG STAMMT AUS ZWEI RAUSCHFELDERN, nicht aus der Weltgenerierung: ein gemeinsames
 * Ergiebigkeitsfeld mal ein Feld je Sorte. Beide sind fest gesaet, damit dieselbe Stelle immer
 * dasselbe liefert -- auch in einer Welt, die es beim Ziehen der Probe noch gar nicht gab.
 *
 * ABWEICHUNG: das Original nimmt NoiseGeneratorPerlin(Random, 4). Auf 1.21 heisst die Klasse
 * PerlinSimplexNoise und nimmt statt einer Oktavenzahl eine Liste von Oktavenstufen; vier
 * Oktaven sind hier also -3 bis 0. Die Saatzahlen sind die des Originals.
 */
public class BedrockOreBaseItem extends Item {

    public BedrockOreBaseItem(Properties properties) {
        super(properties);
    }

    public static double getOreAmount(ItemStack stack, BedrockOreType type) {
        if(!TagsUtil.hasCustomData(stack)) return 0D;
        return TagsUtil.getCustomData(stack).getDouble(type.suffix);
    }

    /** Fuellt die Probe mit dem, was an dieser Stelle steckt. mult ist der Glueckszuschlag des Bohrers. */
    public static void setOreAmount(ItemStack stack, int x, int z, double mult) {

        CompoundTag data = TagsUtil.hasCustomData(stack) ? TagsUtil.getCustomData(stack) : new CompoundTag();

        for(BedrockOreType type : BedrockOreType.values()) {
            data.putDouble(type.suffix, getOreLevel(x, z, type) * mult);
        }

        TagsUtil.putCustomData(stack, data);
    }

    public static void setOreAmount(ItemStack stack, BlockPos pos, double mult) {
        setOreAmount(stack, pos.getX(), pos.getZ(), mult);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        for(BedrockOreType type : BedrockOreType.values()) {
            double amount = getOreAmount(stack, type);
            components.add(Component.translatable("item.hbmsntm.bedrock_ore.type." + type.suffix)
                    .append(Component.literal(": " + ((int) (amount * 100)) / 100D + " ("))
                    .append(Component.translatable(translateDensity(amount)).withStyle(getColor(amount)))
                    .append(Component.literal(")")).withStyle(ChatFormatting.GRAY));
        }
    }

    /* ---- das Rauschen ---- */

    private static final PerlinSimplexNoise[] ORES = new PerlinSimplexNoise[BedrockOreType.values().length];
    private static PerlinSimplexNoise level;

    /** Vier Oktaven, wie im Original -- dort die Zahl 4, hier die Stufenliste -3..0. */
    private static final List<Integer> OCTAVES = List.of(-3, -2, -1, 0);

    public static double getOreLevel(int x, int z, BedrockOreType type) {

        if(level == null) level = new PerlinSimplexNoise(new LegacyRandomSource(2114043L), OCTAVES);

        int i = type.ordinal();
        if(ORES[i] == null) ORES[i] = new PerlinSimplexNoise(new LegacyRandomSource(2082127L + i), OCTAVES);

        double scale = 0.01D;

        return Mth.clamp(Math.abs(level.getValue(x * scale, z * scale, false) * ORES[i].getValue(x * scale, z * scale, false)) * 0.05D, 0D, 2D);
    }

    /* ---- die Einstufung, Schwellen und Farben unveraendert aus ItemOreDensityScanner ---- */

    public static String translateDensity(double density) {
        if(density <= 0.1D) return "item.hbmsntm.ore_density_scanner.verypoor";
        if(density <= 0.35D) return "item.hbmsntm.ore_density_scanner.poor";
        if(density <= 0.75D) return "item.hbmsntm.ore_density_scanner.low";
        if(density >= 1.9D) return "item.hbmsntm.ore_density_scanner.excellent";
        if(density >= 1.65D) return "item.hbmsntm.ore_density_scanner.veryhigh";
        if(density >= 1.25D) return "item.hbmsntm.ore_density_scanner.high";
        return "item.hbmsntm.ore_density_scanner.moderate";
    }

    /** Ueber zwei liegt nur, was mit Glueckszuschlag gefoerdert wurde -- daher die eigene Farbe. */
    public static ChatFormatting getColor(double density) {
        if(density <= 0.1D) return ChatFormatting.DARK_RED;
        if(density <= 0.35D) return ChatFormatting.RED;
        if(density <= 0.75D) return ChatFormatting.GOLD;
        if(density > 2D) return ChatFormatting.LIGHT_PURPLE;
        if(density >= 1.9D) return ChatFormatting.AQUA;
        if(density >= 1.65D) return ChatFormatting.BLUE;
        if(density >= 1.25D) return ChatFormatting.GREEN;
        return ChatFormatting.YELLOW;
    }
}
