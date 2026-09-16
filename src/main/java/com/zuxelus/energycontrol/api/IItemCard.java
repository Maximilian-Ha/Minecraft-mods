package com.zuxelus.energycontrol.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.IItemCard.
 *
 * Das Verhalten einer Sensorkarte. Die Karte selbst ist ein Gegenstand; diese
 * Schnittstelle sagt, wie sie ihr Ziel ausliest und was davon auf dem Schirm landet.
 */
public interface IItemCard {

    /**
     * Liest das Ziel aus und legt das Ergebnis im Kartenspeicher ab. Laeuft auf dem Server.
     *
     * @param range Reichweite der Tafel; -1 bedeutet unbegrenzt
     * @param pos   Koordinaten der Tafel, fuer die Reichweitenpruefung
     */
    CardState update(Level level, ICardReader reader, int range, BlockPos pos);

    /** Die Zeilen, die die Tafel anzeigt. Laeuft auf beiden Seiten. */
    List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels);

    /** Die Ankreuzfelder in den Einstellungen dieser Karte. */
    List<PanelSetting> getSettingsList(ItemStack stack);

    /** Welche Stellen des Einstellungswortes anfangs gesetzt sind. */
    default int getDefaultSettings() {
        return Integer.MAX_VALUE - 1024;
    }

    /** Ob die Karte nur innerhalb der Reichweite der Tafel arbeitet. */
    default boolean isRemoteCard(ItemStack stack) {
        return true;
    }
}
