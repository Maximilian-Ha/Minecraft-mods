package com.zuxelus.energycontrol.crossmod.hbm;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Liest gleichnamige oeffentliche Felder verschiedener HBM-Maschinen aus.
 *
 * HBM hat fuer Fortschritt, Verbrauch und Temperatur keine gemeinsame Schnittstelle -- die
 * Felder heissen in jeder Maschine gleich, gehoeren aber zu keiner gemeinsamen Oberklasse.
 * Das Original von Energy Control loeste das genauso (DataHelper.getInt/getDouble). Die
 * Alternative waere eine Liste aller Maschinenklassen, die bei jeder neuen Maschine im Port
 * nachgezogen werden muesste -- und genau daran ist die 1.12.2-Fassung mit ihren 120
 * Einzelfaellen erstickt.
 *
 * Gefundene Felder werden je Klasse gemerkt, fehlende ebenso: eine Tafel liest ihr Ziel
 * mehrmals je Sekunde aus, eine Suche ueber die Klassenhierarchie je Aufruf waere zu teuer.
 */
public final class HbmFields {

    private static final Field MISSING;

    static {
        try {
            MISSING = HbmFields.class.getDeclaredField("MISSING");
        } catch(NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private static final Map<String, Field> CACHE = new ConcurrentHashMap<>();

    private HbmFields() { }

    private static Field find(Class<?> type, String name) {
        String key = type.getName() + "#" + name;
        Field cached = CACHE.get(key);
        if(cached != null) return cached == MISSING ? null : cached;

        Field found = null;
        for(Class<?> current = type; current != null && found == null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                if(Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) found = field;
            } catch(NoSuchFieldException ignored) {
                // weiter in der Oberklasse
            }
        }

        CACHE.put(key, found != null ? found : MISSING);
        return found;
    }

    public static Integer readInt(BlockEntity be, String name) {
        Field field = find(be.getClass(), name);
        if(field == null) return null;
        try {
            Class<?> type = field.getType();
            if(type == int.class) return field.getInt(be);
            if(type == long.class) return (int) field.getLong(be);
            if(type == double.class) return (int) field.getDouble(be);
            if(type == float.class) return (int) field.getFloat(be);
        } catch(IllegalAccessException ignored) {
            // Feld ist nicht zugaenglich -- dann eben nicht.
        }
        return null;
    }

    public static Boolean readBoolean(BlockEntity be, String name) {
        Field field = find(be.getClass(), name);
        if(field == null || field.getType() != boolean.class) return null;
        try {
            return field.getBoolean(be);
        } catch(IllegalAccessException ignored) {
            return null;
        }
    }
}
