package com.hbm.blocks.machine.icf;

import com.hbm.blocks.generic.ToolConversionBlock;
import com.mojang.serialization.MapCodec;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockICFComponent.
 *
 * Das Wandbauteil der Brennkammer. Vier Baustufen, die mit Werkzeug und Material nacheinander
 * erreicht werden: roh, Gefaess, verschweisstes Gefaess, Struktur, verschraubte Struktur.
 * Welche Stufe wo stehen muss, prueft der Zusammenbauklotz.
 */
public class ICFComponentBlock extends ToolConversionBlock {

    /** Roh, Gefaess, verschweisst, Struktur, verschraubt. */
    public static final int STAGES = 5;

    public ICFComponentBlock(Properties properties) {
        super(properties, STAGES);
    }

    public static final MapCodec<ICFComponentBlock> ICF_CODEC = simpleCodec(ICFComponentBlock::new);
    @Override protected MapCodec<? extends ICFComponentBlock> codec() { return ICF_CODEC; }
}
