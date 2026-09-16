package com.zuxelus.energycontrol.utils;

import com.zuxelus.energycontrol.api.PanelString;

/** Zahlenformat der Tafel an einer Stelle, damit Anbindungen es nicht nachbauen. */
public final class PanelFormat {

    private PanelFormat() { }

    public static String number(double value) {
        return PanelString.format(value);
    }
}
