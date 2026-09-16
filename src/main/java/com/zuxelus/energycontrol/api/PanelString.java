package com.zuxelus.energycontrol.api;

import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.PanelString.
 *
 * Eine Zeile auf dem Schirm einer Informationstafel: bis zu drei Textstuecke (links,
 * mittig, rechts) mit je einer Farbe. Unterschied zum Original: dort waren das
 * fertig uebersetzte Zeichenketten, hier sind es {@link Component}s. Auf 1.21.1 ist
 * die Uebersetzung Sache des Clients -- der Server kennt die Sprache des Spielers
 * nicht, und die Tafel erzeugt ihre Zeilen auf beiden Seiten.
 */
public class PanelString {

    /** Zahlenformat des Originals: Tausender durch Leerzeichen getrennt, drei Nachkommastellen. */
    private static DecimalFormat formatter;

    public Component textLeft;
    public Component textCenter;
    public Component textRight;

    public int colorLeft;
    public int colorCenter;
    public int colorRight;

    public PanelString() { }

    public PanelString(Component text) {
        this.textLeft = text;
    }

    public static DecimalFormat getFormatter() {
        if(formatter == null) {
            DecimalFormat local = new DecimalFormat("#,###.###");
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            local.setDecimalFormatSymbols(symbols);
            formatter = local;
        }
        return formatter;
    }

    public static String format(double value) {
        return getFormatter().format(value);
    }

    /** Beschriftete Zeile, etwa "Energie: 1 200 HE". Ohne Beschriftung bleibt nur der Wert. */
    public static PanelString of(String key, Component value, boolean showLabels) {
        return new PanelString(showLabels ? Component.translatable(key, value) : value);
    }

    public static PanelString of(String key, String value, boolean showLabels) {
        return of(key, Component.literal(value), showLabels);
    }

    public static PanelString of(String key, double value, boolean showLabels) {
        return of(key, Component.literal(format(value)), showLabels);
    }

    /** Wie oben, aber mit Einheit hinter der Zahl -- "1 200 HE". */
    public static PanelString of(String key, double value, String unit, boolean showLabels) {
        return of(key, Component.literal(format(value) + " " + unit), showLabels);
    }
}
