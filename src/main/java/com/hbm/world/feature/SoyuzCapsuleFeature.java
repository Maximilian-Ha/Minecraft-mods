package com.hbm.world.feature;

import com.hbm.blockentity.machine.storage.SoyuzCapsuleBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.SoyuzCapsuleBlock;
import com.hbm.config.NtmConfig;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Portiert aus 1.7.10: der Kapselzweig in HbmWorldGen.generateSurface (Zeile 341 ff.).
 *
 * Eine verrostete Landekapsel im Strandsand -- und die einzige Schallplatte des Mods darin,
 * in einem zufaelligen ihrer neunzehn Faecher. Anders kommt niemand an sie heran: im Original
 * traegt record_glass setCreativeTab(null).
 *
 * VIER BLOECKE TIEF: das Original setzt sie auf getHeightValue - 4 und prueft, ob drei Bloecke
 * ueber ihr noch fester Grund steht. Sie liegt also begraben; was man am Strand sieht, ist
 * Sand, und die Kapsel findet nur, wer graebt.
 */
public class SoyuzCapsuleFeature extends Feature<NoneFeatureConfiguration> {

    /** Wie tief unter der Oberflaeche sie steckt. */
    private static final int TIEFE = 4;

    public SoyuzCapsuleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

        WorldGenLevel level = context.level();
        BlockPos pos = context.origin().below(TIEFE);

        /* Das Original nimmt canPlaceTorchOnTop des Blocks darueber: es muss fester Grund
         * sein, kein Wasser und kein Hohlraum. */
        if(!level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.UP)) return false;

        BlockState zustand = NtmBlocks.SOYUZ_CAPSULE.get().defaultBlockState().setValue(SoyuzCapsuleBlock.RUSTY, true);
        level.setBlock(pos, zustand, 2);

        if(level.getBlockEntity(pos) instanceof SoyuzCapsuleBlockEntity kapsel) {
            int fach = level.getRandom().nextInt(kapsel.getContainerSize());
            kapsel.setItem(fach, new ItemStack(NtmItems.RECORD_GLASS.get()));
        }

        if(NtmConfig.COMMON.ENABLE_DEBUG_MODE.get()) NuclearTechMod.LOGGER.info("[Debug] Successfully spawned capsule at {}", pos);

        return true;
    }
}
