package com.hbm.blocks.states;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

/**
 * Welche Seite eines roten Ziegels das Ziegelbild traegt -- oder keine.
 *
 * DIE REIHENFOLGE IST DER METADATENWERT DES ORIGINALS, Stelle fuer Stelle: unten null, oben
 * eins, Nord zwei, Sued drei, West vier, Ost fuenf. Sechs heisst "keine Seite", und genau
 * diesen Wert setzt der Zimmererzeuger fuer alle Kanten und fuer das Zimmer des
 * Ziegel-Schluessellochs. Wer hier umsortiert, baut andere Zimmer.
 */
public enum BrickFace implements StringRepresentable {

    DOWN("down", Direction.DOWN),
    UP("up", Direction.UP),
    NORTH("north", Direction.NORTH),
    SOUTH("south", Direction.SOUTH),
    WEST("west", Direction.WEST),
    EAST("east", Direction.EAST),
    NONE("none", null);

    private final String name;
    private final Direction direction;

    BrickFace(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** Die Seite, die das Ziegelbild traegt -- oder null, wenn keine. */
    public Direction direction() {
        return this.direction;
    }

    /** Das Gegenstueck zu ForgeDirection.getOrientation(meta) des Originals. */
    public static BrickFace of(Direction direction) {
        for(BrickFace face : values()) if(face.direction == direction) return face;
        return NONE;
    }
}
