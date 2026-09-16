package com.hbm.inventory.screens;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.menus.ArmorTableMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIArmorTable.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Die Oberflaeche ist um 22 Bildpunkte breiter als das Bild: links haengt ein schmaler
 * Streifen mit den vier getragenen Teilen daran, den das Original aus einer zweiten Stelle
 * derselben Datei zeichnet.
 */
public class ArmorTableScreen extends InfoScreen<ArmorTableMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_armor_table.png");

    /** Die Breite des angehaengten Streifens fuer die getragenen Teile. */
    private static final int SIDEBAR = 22;

    /** Was in welchen Platz gehoert -- in der Reihenfolge, in der die Plaetze angelegt sind. */
    private static final String[] SLOT_NAMES = {
            "armorMod.type.helmet",
            "armorMod.type.chestplate",
            "armorMod.type.leggings",
            "armorMod.type.boots",
            "armorMod.type.servo",
            "armorMod.type.cladding",
            "armorMod.type.insert",
            "armorMod.type.special",
            "armorMod.type.battery",
            "armorMod.insertHere"
    };

    public ArmorTableScreen(ArmorTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176 + SIDEBAR;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        /* Der Hinweis, was in einen leeren Platz gehoert -- nur solange nichts an der Maus haengt. */
        if(this.menu.getCarried().isEmpty()) {

            for(int i = 0; i < SLOT_NAMES.length; i++) {

                Slot slot = this.menu.slots.get(i);
                if(slot.hasItem()) continue;

                this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + slot.x, this.topPos + slot.y, 16, 16,
                        Component.translatable(SLOT_NAMES[i]).withStyle(i < ArmorModHandler.MOD_SLOTS ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.YELLOW));
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos + SIDEBAR, this.topPos, 0, 0, this.imageWidth - SIDEBAR, this.imageHeight);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos + 31, 176, 96, SIDEBAR, 100);

        ItemStack armor = this.menu.slots.get(ArmorTableMenu.ARMOR_SLOT).getItem();

        if(!armor.isEmpty()) {
            /* Ein Ruestungsteil bekommt den ruhigen Rahmen, alles andere den warnenden. */
            int v = armor.getItem() instanceof ArmorItem ? 74 : 52;
            guiGraphics.blit(TEXTURE, this.leftPos + 41 + SIDEBAR, this.topPos + 60, 176, v, 22, 22);
        } else if(System.currentTimeMillis() % 1000 < 500) {
            /* Leer blinkt der Rahmen, damit man sieht, wo das Teil hingehoert. */
            guiGraphics.blit(TEXTURE, this.leftPos + 41 + SIDEBAR, this.topPos + 60, 176, 52, 22, 22);
        }

        for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {

            Slot slot = this.menu.slots.get(i);
            if(!slot.hasItem()) continue;

            /* Gruen, wenn das Modul in dieses Teil passt, sonst rot. */
            int v = ArmorModHandler.isApplicable(armor, slot.getItem()) ? 34 : 16;
            guiGraphics.blit(TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 176, v, 18, 18);
        }
    }
}
