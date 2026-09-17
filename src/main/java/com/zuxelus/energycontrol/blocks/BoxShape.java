package com.zuxelus.energycontrol.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Die Koerper der flachen Bloecke dieses Mods.
 *
 * Mehrere Bloecke fuellen ihren Wuerfel nicht aus: die Tafel ist duenn, Waermemelder und
 * Alarme sind flache Kaesten. Alle liegen nach derselben Regel an -- an dem Block, auf den
 * sie gesetzt wurden, also auf der Gegenseite ihrer Schauseite.
 */
public final class BoxShape {

    private BoxShape() { }

    /**
     * Ein Kasten von {@code depth} Sechzehnteln Tiefe, der an der Rueckseite anliegt und
     * ringsum um {@code inset} Sechzehntel eingerueckt ist.
     */
    public static VoxelShape slab(Direction facing, double inset, double depth) {
        double a = inset;
        double b = 16D - inset;
        return switch(facing) {
            case UP -> Block.box(a, 0, a, b, depth, b);
            case DOWN -> Block.box(a, 16D - depth, a, b, 16, b);
            case NORTH -> Block.box(a, a, 16D - depth, b, b, 16);
            case SOUTH -> Block.box(a, a, 0, b, b, depth);
            case EAST -> Block.box(0, a, a, depth, b, b);
            case WEST -> Block.box(16D - depth, a, a, 16, b, b);
        };
    }
}
