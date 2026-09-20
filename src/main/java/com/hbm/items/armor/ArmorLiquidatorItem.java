package com.hbm.items.armor;

import com.hbm.items.IHelmetOverlayItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.util.RenderScreenOverlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorLiquidator.
 *
 * Der Bleianzug der Liquidatoren. Er ist so schwer, dass ihn nichts mehr umwirft und sein
 * Traeger merklich langsamer geht -- das sind die beiden Merkmalsaenderungen des Originals
 * (Rueckstossfestigkeit +100, Tempo -0,1), und sie haengen an jedem der vier Teile.
 *
 * DAS DUNKLE BILD vor dem Schirm kommt vom Helm: eine gleichbleibende Verdunkelung, damit man
 * durch das schmale Sichtfenster nicht mehr sieht als durch ein schmales Sichtfenster.
 *
 * ABWEICHUNG bei den Merkmalen: das Original baut die Multimap in getItemAttributeModifiers
 * von Hand zusammen und benutzt dafuer ArmorModHandler.fixedUUIDs. In 1.21 gehoeren
 * Merkmalsaenderungen in die Item.Properties; sie stehen deshalb bei der Anmeldung in
 * NtmItems, nicht hier. Der Wert ist derselbe.
 *
 * NICHT UEBERNOMMEN: setStep, setJump und setFall des Originals. Die drei Geraeusche haengen
 * an einem Teilsystem, das der Port nicht hat -- ArmorFSBItem sagt das im Kopf ausdruecklich
 * fuer alle FSB-Anzuege.
 */
public class ArmorLiquidatorItem extends ArmorFSBItem implements IHelmetOverlayItem {

    /** Die Verdunkelung vor dem Schirm. Im Original heisst diese Datei ebenso. */
    private static final ResourceLocation OVERLAY_DARK =
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_dark.png");

    public ArmorLiquidatorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHelmetOverlay(GuiGraphics guiGraphics, ItemStack stack) {
        RenderScreenOverlay.renderHelmetOverlay(guiGraphics, OVERLAY_DARK);
    }
}
