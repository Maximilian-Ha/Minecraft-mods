package com.hbm.saveddata;

import com.hbm.saveddata.satellite.SatelliteBase;
import com.hbm.saveddata.satellite.XSatelliteRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SatelliteSavedData extends SavedData {

    public static Factory<SatelliteSavedData> factory() {
        return new Factory<>(
                SatelliteSavedData::new, SatelliteSavedData::load
        );
    }

    public final Map<Integer, SatelliteBase> sats = new HashMap<>();
    
    public SatelliteSavedData() {
        this.setDirty();
    }

    public boolean isFreqTaken(int freq) {
        return getSatFromFreq(freq) != null;
    }

    public SatelliteBase getSatFromFreq(int freq) {
        return sats.get(freq);
    }

    public static SatelliteSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SatelliteSavedData data = new SatelliteSavedData();

        int satCount = tag.getInt("satCount");

        for(int i = 0; i < satCount; i++) {
            SatelliteBase sat = XSatelliteRegistry.createFromId(tag.getInt("sat_id_" + i));
            sat.readFromNBT((CompoundTag) tag.get("sat_data_" + i), registries);

            int freq = tag.getInt("sat_freq_" + i);
            data.sats.put(freq, sat);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("satCount", sats.size());

        int i = 0;

        for(Entry<Integer, SatelliteBase> struct : sats.entrySet()) {
            CompoundTag data = new CompoundTag();
            struct.getValue().writeToNBT(data, registries);

            tag.putInt("sat_id_" + i, struct.getValue().getID());
            tag.put("sat_data_" + i, data);
            tag.putInt("sat_freq_" + i, struct.getKey());
            i++;
        }

        return tag;
    }

    /**
     * Ein Tick fuer jeden Satelliten in der Umlaufbahn, seit Runde 129. Bis dahin hatte der Port
     * gar keinen Satelliten-Tick -- onUpdateTick stand im Original da und wurde hier nie
     * gerufen, weil es bis zu den Bergbausatelliten auch niemanden gab, der ihn brauchte.
     */
    public void tickAll(ServerLevel level) {
        for(SatelliteBase sat : this.sats.values()) sat.onUpdateTick(level);
    }

    public static SatelliteSavedData getData(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(factory(), "satellites");
    }
}
