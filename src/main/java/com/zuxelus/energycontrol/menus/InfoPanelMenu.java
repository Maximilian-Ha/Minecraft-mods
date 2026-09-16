package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerInfoPanel.
 *
 * Die Fachpositionen sind unveraendert aus dem Original uebernommen.
 *
 * Alle Schalter der Oberflaeche laufen ueber {@link #clickMenuButton} -- den Weg, den
 * Minecraft fuer Knoepfe in Oberflaechen ohnehin vorsieht. Das Original brauchte dafuer
 * eigene Netzwerkpakete; die sind hier nicht noetig.
 */
public class InfoPanelMenu extends ECMenuBase<InfoPanelBlockEntity> {

    /** Knopfkennungen ab 100, damit sie nie mit einer Ankreuzstelle zusammenfallen. */
    public static final int BUTTON_LABELS = 100;
    public static final int BUTTON_TICKRATE = 101;
    public static final int BUTTON_COLOR_TEXT = 102;
    public static final int BUTTON_COLOR_BACKGROUND = 103;

    public InfoPanelMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (InfoPanelBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public InfoPanelMenu(int id, Inventory inventory, InfoPanelBlockEntity be) {
        super(ECMenuTypes.INFO_PANEL.get(), id, be);

        addSlot(new SlotFiltered(be, InfoPanelBlockEntity.SLOT_CARD, 8, 42));
        addSlot(new SlotFiltered(be, InfoPanelBlockEntity.SLOT_UPGRADE_RANGE, 8, 60));
        addSlot(new SlotFiltered(be, InfoPanelBlockEntity.SLOT_UPGRADE_COLOR, 8, 78));
        addSlot(new SlotFiltered(be, InfoPanelBlockEntity.SLOT_UPGRADE_TOUCH, 8, 96));

        addPlayerInventory(inventory, 8, 119);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch(id) {
            case BUTTON_LABELS -> be.toggleShowLabels();
            case BUTTON_TICKRATE -> be.cycleTickRate();
            case BUTTON_COLOR_TEXT -> be.cycleColorText();
            case BUTTON_COLOR_BACKGROUND -> be.cycleColorBackground();
            default -> {
                if(id < 0 || id >= 32) return false;
                be.toggleSetting(1 << id);
            }
        }
        return true;
    }
}
