package com.hbm.saveddata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Speicher fuer die RBMK-Dials.
 *
 * Abweichung vom Original: in 1.7.10 lagen die Dials als Gamerules in der Welt. Seit 1.13 muessen
 * Gamerules statisch registriert werden und kennen nur boolean und int -- 18 der 24 Dials sind
 * aber Kommazahlen. Deshalb liegen die Werte hier in einem SavedData pro Level; gesetzt werden
 * sie ueber /ntmrbmk.
 */
public class RBMKDialsSavedData extends SavedData {

    public static final String DATA_NAME = "ntm_rbmk_dials";

    /** Rohwerte, Schluessel ist der Dial-Name. Nicht gesetzte Dials fehlen und liefern ihren Standardwert. */
    private final CompoundTag values;

    public RBMKDialsSavedData() {
        this.values = new CompoundTag();
    }

    private RBMKDialsSavedData(CompoundTag values) {
        this.values = values;
    }

    public static Factory<RBMKDialsSavedData> factory() {
        return new Factory<>(RBMKDialsSavedData::new, RBMKDialsSavedData::load);
    }

    public static RBMKDialsSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new RBMKDialsSavedData(tag.getCompound("dials"));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("dials", this.values);
        return tag;
    }

    public boolean has(String key) {
        return this.values.contains(key);
    }

    public double getDouble(String key, double def) {
        return this.values.contains(key) ? this.values.getDouble(key) : def;
    }

    public int getInt(String key, int def) {
        return this.values.contains(key) ? this.values.getInt(key) : def;
    }

    public boolean getBoolean(String key, boolean def) {
        return this.values.contains(key) ? this.values.getBoolean(key) : def;
    }

    public void setDouble(String key, double value) {
        this.values.putDouble(key, value);
        this.setDirty();
    }

    public void setInt(String key, int value) {
        this.values.putInt(key, value);
        this.setDirty();
    }

    public void setBoolean(String key, boolean value) {
        this.values.putBoolean(key, value);
        this.setDirty();
    }

    public void remove(String key) {
        this.values.remove(key);
        this.setDirty();
    }

    public void clear() {
        for(String key : this.values.getAllKeys().toArray(new String[0])) this.values.remove(key);
        this.setDirty();
    }
}
