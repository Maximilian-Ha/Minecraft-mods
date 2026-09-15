package com.hbm.blocks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * Ersatz fuer BlockPistonBase.determineOrientation aus 1.7.10.
 *
 * Das Original richtete Diode und Kabelanzeige beim Setzen wie einen Kolben aus: steht der
 * Spieler dicht daneben und deutlich ueber oder unter dem Block, zeigt der Block nach oben
 * bzw. unten, sonst waagerecht vom Spieler weg. Die Zahlenwerte (2 Bloecke Abstand, 2 Bloecke
 * Hoehenunterschied) sind 1:1 uebernommen.
 */
public class NetworkPlacement {

    public static Direction determineOrientation(BlockPlaceContext context) {

        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if(player != null && Math.abs(player.getX() - pos.getX()) < 2.0F && Math.abs(player.getZ() - pos.getZ()) < 2.0F) {
            double eyeY = player.getEyeY();

            if(eyeY - pos.getY() > 2.0D) return Direction.UP;
            if(pos.getY() - eyeY > 0.0D) return Direction.DOWN;
        }

        return context.getHorizontalDirection().getOpposite();
    }
}
