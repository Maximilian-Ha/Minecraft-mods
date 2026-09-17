package com.zuxelus.energycontrol.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Die Dicke der fortgeschrittenen Tafel, in Sechzehnteln eines Blocks.
 *
 * Im Original war die Dicke stufenlos, weil dort der ganze Block vom Renderer gezeichnet
 * wurde. Hier ist die Tafel ein gewoehnlicher Block mit gewoehnlichem Modell, und die
 * Dicke steckt im Blockzustand -- sechzehn Stufen, eine je Sechzehntel. Dafuer sieht sie
 * aus wie jeder andere Block: mit Licht, mit Schatten, und in jedem Ressourcenpaket
 * austauschbar.
 */
public final class PanelThickness {

    public static final IntegerProperty THICKNESS = IntegerProperty.create("thickness", 1, 16);

    /** Die Stufen, die das Werkzeug durchschaltet. */
    public static final int[] STEPS = { 16, 12, 8, 4, 2, 1 };

    private PanelThickness() { }

    public static int next(int thickness) {
        for(int i = 0; i < STEPS.length; i++) {
            if(STEPS[i] == thickness) return STEPS[(i + 1) % STEPS.length];
        }
        return STEPS[0];
    }

    /**
     * Der Koerper der Tafel: sie liegt an dem Block an, auf den sie gesetzt wurde, also auf
     * der Gegenseite ihrer Schauseite.
     */
    public static VoxelShape shape(Direction facing, int thickness) {
        return BoxShape.slab(facing, 0, thickness);
    }
}
