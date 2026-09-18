package com.zuxelus.energycontrol.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * Wohin ein frisch gesetzter Block schaut.
 *
 * Portiert aus 1.12.2: com.zuxelus.zlib.blocks.FacingBlock#getStateForPlacement.
 *
 * Nicht auf die angeklickte Flaeche, sondern **zum Spieler hin**. Das ist der
 * Unterschied, der eine Wand aus Tafeln ueberhaupt erst baubar macht: wer eine
 * Erweiterung an die Oberseite der darunterliegenden setzt, klickt deren Deckflaeche an
 * -- der Block schaute dann nach oben und gehoerte nicht mehr zum selben Schirm.
 *
 * Die Schwelle von 65 Grad ist die des Originals: erst wer deutlich nach unten schaut,
 * bekommt eine Tafel, die nach oben zeigt.
 */
public final class Facings {

    private static final float STEEP = 65F;

    private Facings() { }

    public static Direction towardsPlayer(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if(player != null) {
            float pitch = player.getXRot();
            if(pitch >= STEEP) return Direction.UP;
            if(pitch <= -STEEP) return Direction.DOWN;
        }
        return context.getHorizontalDirection().getOpposite();
    }
}
