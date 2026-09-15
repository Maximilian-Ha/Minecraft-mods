package com.hbm.blocks.machine.fusion;

import com.hbm.blocks.generic.ToolConversionBlock;
import com.mojang.serialization.MapCodec;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockFusionComponent.
 *
 * Der Baustein der Fusionsanlage. Vier Baustufen:
 *
 * 0 supraleitende BSCCO-Spulen (kommen aus der Montagemaschine),
 * 1 verschweisste Spulen (mit dem Schweissbrenner und einer Stahlgussplatte aus Stufe 0),
 * 2 die Reaktordecke,
 * 3 die Verrohrung.
 *
 * Stufe 2 und 3 haben mit 0 und 1 nichts zu tun -- sie kommen ebenfalls aus der Montagemaschine.
 * Der Block fasst also vier verschiedene Bauteile zusammen; das Original tut es genauso, weil es
 * dort vier Metadatenwerte eines Blocks sind.
 *
 * Gebraucht wird er nur als Zutat: die Plasmaschmiede macht aus 320 Spulen, 192 Verrohrungen,
 * 128 Reaktordecken und vier Quantenrechnern den Torus.
 */
public class FusionComponentBlock extends ToolConversionBlock {

    /** Spulen, verschweisste Spulen, Decke, Verrohrung. */
    public static final int STAGES = 4;

    public FusionComponentBlock(Properties properties) {
        super(properties, STAGES);
    }

    public static final MapCodec<FusionComponentBlock> FUSION_CODEC = simpleCodec(FusionComponentBlock::new);
    @Override protected MapCodec<? extends FusionComponentBlock> codec() { return FUSION_CODEC; }
}
