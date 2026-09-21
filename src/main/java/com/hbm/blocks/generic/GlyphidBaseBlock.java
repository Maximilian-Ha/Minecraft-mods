package com.hbm.blocks.generic;

import com.hbm.entity.mob.glyphid.Glyphid;

import net.minecraft.world.level.block.Block;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockGlyphid.
 *
 * Das Fleisch, aus dem ein Glyphidenbau besteht. Weich (Haerte 0,5), und es faellt nichts:
 * das Original gibt getItemDropped null zurueck, hier steht dafuer noLootTable an den
 * Eigenschaften.
 *
 * DREI UNTERARTEN, wie im Original: gewoehnlich, verseucht, radioaktiv. Auf 1.7.10 waren das
 * drei Metawerte EINES Blocks; auf 1.21 sind es drei Bloecke, denn ein Metawert ist dort
 * nichts anderes als ein eigener Block mit eigenem Namen. Welche Unterart wo steht,
 * entscheidet der Bau beim Setzen.
 *
 * ZWEI TEXTUREN JE UNTERART, nach dem Ort gewuerfelt. Das Original rechnet dafuer selbst
 * einen Hash aus den Koordinaten; auf 1.21 kann der Blockzustand mehrere Modelle auflisten,
 * und das Spiel wuerfelt nach dem Ort -- dieselbe Wirkung, ohne eigene Rechnung.
 *
 * WELCHE UNTERART EIN GLYPHID WIRD, der hier schluepft, steht in subtype(). Die Zahlen sind
 * die des Originals (EntityGlyphid.TYPE_NORMAL und so fort).
 */
public class GlyphidBaseBlock extends Block {

    private final int unterart;

    public GlyphidBaseBlock(Properties properties, int unterart) {
        super(properties);
        this.unterart = unterart;
    }

    /** Glyphid.TYPE_NORMAL, TYPE_INFECTED oder TYPE_RADIOACTIVE. */
    public int subtype() {
        return this.unterart;
    }

    /** Ob dieser Bauteil verseucht ist -- der Bau setzt danach seine uebrigen Bloecke. */
    public boolean isInfected() {
        return this.unterart == Glyphid.TYPE_INFECTED;
    }
}
