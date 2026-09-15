package com.hbm.blockentity.machine.rbmk;

import net.minecraft.ChatFormatting;

/**
 * Portiert aus 1.7.10: TileEntityRBMKControlManual.RBMKColor.
 *
 * Farbgruppen fuer die Steuerstaebe. Am Pult lassen sich damit mehrere Staebe auf einen Schlag
 * auswaehlen und gemeinsam fahren.
 */
public enum RBMKColor {

    RED(ChatFormatting.RED),
    YELLOW(ChatFormatting.YELLOW),
    GREEN(ChatFormatting.GREEN),
    BLUE(ChatFormatting.BLUE),
    PURPLE(ChatFormatting.LIGHT_PURPLE);

    public final ChatFormatting format;

    RBMKColor(ChatFormatting format) {
        this.format = format;
    }

    /** Liefert die Farbe zum Index, oder null ausserhalb des gueltigen Bereichs. */
    public static RBMKColor byIndex(int index) {
        if(index < 0 || index >= values().length) return null;
        return values()[index];
    }
}
