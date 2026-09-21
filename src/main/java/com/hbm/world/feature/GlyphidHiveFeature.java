package com.hbm.world.feature;

import com.hbm.config.NtmConfig;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Portiert aus 1.7.10: der Glyphidenbau-Zweig aus com.hbm.lib.HbmWorldGen (Z. 191-202).
 *
 * OHNE DIESE STELLE WAERE DIE GANZE FAMILIE UNERREICHBAR: der Spaeher baut zwar neue Nester,
 * aber nur ausgehend von einem, das schon steht. Das erste muss die Welt mitbringen.
 *
 * WIE DAS ORIGINAL DIE HOEHE SUCHT: vom Oberflaechenwert aus drei Bloecke hoch und einen
 * tief, von oben nach unten, bis der Block DARUNTER ein voller Block ist -- dort steht der
 * Bau. Findet sich keiner, entsteht nichts. Uebernommen, Schleife fuer Schleife.
 *
 * JEDER ZEHNTE BAU IST VERSEUCHT (rand.nextInt(10) == 0), und Beute gibt es immer -- anders
 * als bei den Nestern, die der Spaeher hinterlaesst, denn die haben schon jemanden gehabt,
 * der sie ausraeumen konnte.
 *
 * SELTENHEIT: das Original nimmt einen Bau je 256 Chunks im Mittel. Auf 1.21 steht diese
 * Zahl nicht mehr hier, sondern im Platzierer (NtmPlacedFeatures) -- hier bleibt nur der
 * Schalter, ob es Baue ueberhaupt geben soll.
 */
public class GlyphidHiveFeature extends Feature<NoneFeatureConfiguration> {

    public GlyphidHiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

        if(!NtmConfig.COMMON.ENABLE_HIVES.get()) return false;

        WorldGenLevel level = context.level();
        BlockPos ursprung = context.origin();

        int x = ursprung.getX();
        int z = ursprung.getZ();
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        for(int k = 3; k >= -1; k--) {

            BlockPos darunter = new BlockPos(x, y - 1 + k, z);
            BlockState zustand = level.getBlockState(darunter);

            if(!zustand.isFaceSturdy(level, darunter, Direction.UP)) continue;

            GlyphidHive.generateSmall(level.getLevel(), new BlockPos(x, y + k, z),
                    context.random(), context.random().nextInt(10) == 0, true);
            return true;
        }

        return false;
    }
}
