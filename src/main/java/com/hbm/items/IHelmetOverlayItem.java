package com.hbm.items;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Ein Kopfteil, das sich ueber den ganzen Schirm legt, solange es getragen wird.
 *
 * Im Original heisst das renderHelmetOverlay und wird von Forge direkt am Ruestungsteil
 * aufgerufen. In 1.21 gibt es diesen Aufhaenger nicht mehr, deshalb schaut
 * NuclearTechModClient beim Zeichnen der Oberflaeche selbst in den Kopfschlitz.
 */
public interface IHelmetOverlayItem {

    /** Zeichnet den Vorsatz. Wird nur aufgerufen, wenn das Teil wirklich im Kopfschlitz steckt. */
    @OnlyIn(Dist.CLIENT) void renderHelmetOverlay(GuiGraphics guiGraphics, ItemStack stack);
}
