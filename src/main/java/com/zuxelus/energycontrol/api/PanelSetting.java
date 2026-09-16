package com.zuxelus.energycontrol.api;

import net.minecraft.network.chat.Component;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.PanelSetting.
 *
 * Ein Ankreuzfeld in den Einstellungen einer Karte. {@link #displayBit} ist die Stelle
 * im Einstellungswort der Tafel, die dieses Feld schaltet -- zulaessig sind 0 bis 31.
 */
public class PanelSetting {

    public final Component title;
    public final int displayBit;

    public PanelSetting(Component title, int displayBit) {
        this.title = title;
        this.displayBit = displayBit;
    }

    public PanelSetting(String translationKey, int displayBit) {
        this(Component.translatable(translationKey), displayBit);
    }
}
