package com.hbm.world.feature;

import com.hbm.blockentity.BedrockOreBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import com.hbm.items.special.BedrockOreBaseItem;
import com.hbm.items.special.BedrockOreItem.BedrockOreType;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Portiert aus 1.7.10: com.hbm.world.feature.BedrockOre, die Methode generateAuto.
 *
 * Legt ein Grundgesteinserz in die unterste Lage und mauert es mit Tiefengestein zu. Was es
 * hergibt, ist immer die Rohprobe; wie ERGIEBIG die Stelle ist, entscheidet dasselbe Rauschen,
 * das die Probe spaeter ausliest -- der Mittelwert ueber alle sechs Sorten.
 *
 * DIE ERGIEBIGKEIT BESTIMMT DIE STUFE UND DIE SAEURE: je reicher die Stelle, desto besser muss
 * der Bohrer sein und desto mehr muss vorher hineingegossen werden. Die Schwellen sind die des
 * Originals.
 *
 * ABWEICHUNG: das Original setzt das Erz auf y=0 und das Tiefengestein darueber bis y=6, weil
 * dort die Welt bei null endet. Auf 1.21 liegt der Boden bei getMinBuildHeight(); die Hoehen
 * sind entsprechend relativ dazu gerechnet, die Abstaende bleiben dieselben.
 */
public class BedrockOreFeature extends Feature<NoneFeatureConfiguration> {

    /** Was vor dem Bohren in den Tank muss. Stufe eins braucht nichts. */
    public static final FluidStack BORE_TIER_1 = null;
    public static final FluidStack BORE_TIER_2 = new FluidStack(Fluids.WATER, 1_000);
    public static final FluidStack BORE_TIER_3 = new FluidStack(Fluids.SULFURIC_ACID, 1_000);
    public static final FluidStack BORE_TIER_4 = new FluidStack(Fluids.SOLVENT, 2_000);

    /** Die Farbe der Rohprobe im Original -- ein Sandton. */
    public static final int BASE_COLOR = 0xD78A16;

    public BedrockOreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static FluidStack getBoreFluid(double density) {
        if(density > 1.5D) return BORE_TIER_4;
        if(density > 1.0D) return BORE_TIER_3;
        if(density > 0.75D) return BORE_TIER_2;
        return BORE_TIER_1;
    }

    public static int getTier(double density) {
        if(density > 1.5D) return 4;
        if(density > 1.0D) return 3;
        if(density > 0.75D) return 2;
        return 1;
    }

    /** Der Mittelwert ueber alle sechs Sorten an dieser Stelle. */
    public static double getDensity(int x, int z) {
        double total = 0D;
        for(BedrockOreType type : BedrockOreType.values()) total += BedrockOreBaseItem.getOreLevel(x, z, type);
        return total / BedrockOreType.values().length;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int x = origin.getX();
        int z = origin.getZ();
        int minY = level.getMinBuildHeight();

        double density = getDensity(x, z);
        FluidStack acid = getBoreFluid(density);
        int tier = getTier(density);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        /* Der Erzfleck: die Mitte immer, die acht Nachbarn je nach Wurf. */
        for(int ix = x - 1; ix <= x + 1; ix++) for(int iz = z - 1; iz <= z + 1; iz++) {

            pos.set(ix, minY, iz);
            if(!level.getBlockState(pos).is(Blocks.BEDROCK)) continue;
            /* Die Mitte immer, die acht Nachbarn mit halber Aussicht -- wie im Original. */
            if(!(ix == x && iz == z) && !context.random().nextBoolean()) continue;

            level.setBlock(pos, NtmBlocks.ORE_BEDROCK.get().defaultBlockState(), 3);

            if(level.getBlockEntity(pos) instanceof BedrockOreBlockEntity ore) {
                ItemStack resource = new ItemStack(NtmItems.BEDROCK_ORE_BASE.get());
                ore.resource = resource;
                ore.acidRequirement = acid;
                ore.tier = tier;
                ore.setStyle(BASE_COLOR, context.random().nextInt(10));
                ore.setChanged();
            }

            placed = true;
        }

        if(!placed) return false;

        /* Die Mauer aus Tiefengestein: sieben mal sieben, die unteren zwei Lagen immer, darueber
         * nur, wo ohnehin Grundgestein steht. */
        for(int ix = x - 3; ix <= x + 3; ix++) for(int iz = z - 3; iz <= z + 3; iz++) {
            for(int iy = 1; iy < 7; iy++) {

                pos.set(ix, minY + iy, iz);
                if(iy >= 3 && !level.getBlockState(pos).is(Blocks.BEDROCK)) continue;

                if(level.getBlockState(pos).is(Blocks.BEDROCK) || level.getBlockState(pos).is(Blocks.STONE) || level.getBlockState(pos).is(Blocks.DEEPSLATE)) {
                    level.setBlock(pos, NtmBlocks.STONE_DEPTH.get().defaultBlockState(), 3);
                }
            }
        }

        return true;
    }
}
