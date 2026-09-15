package com.hbm.saveddata.satellite;

import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSatellite;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.saveddata.satellites.SatelliteMiner.
 *
 * Der Asteroidenschuerfer. Er braucht eine Viertelstunde je Ladung und legt dann zehn bis
 * fuenfzehn Fuellungen aus seinem Beutevorrat zur Abholung bereit -- die holt die
 * Satellitenstation mit einer Abwurfkapsel.
 *
 * ER SCHUERFT NUR, SOLANGE NICHTS BEREITLIEGT. Wer nicht abholt, haelt ihn an; das ist im
 * Original genauso und verhindert, dass ein vergessener Satellit ins Unendliche zaehlt.
 */
public class SatelliteMiner extends SatelliteBase {

    /** Eine Viertelstunde je Ladung. */
    public static final double SPEED = 1D / (15 * 60 * 20);

    public double progress;

    @Override public String getType() { return "ASTEROID_MINER"; }

    /** Aus welchem Vorrat geschuerft wird -- der Mondschuerfer nennt hier einen anderen. */
    protected String getCargo() { return ItemPoolsSatellite.POOL_SAT_MINER; }

    @Override
    public List<Component> getInfo(Level level) {
        List<Component> info = super.getInfo(level);
        info.add(Component.translatable("satellite.minerprogress", Math.round(this.progress * 100) + "%"));
        return info;
    }

    @Override
    public void onUpdateTick(ServerLevel level) {

        if(!this.requestableSlots.isEmpty()) return;

        this.progress += SPEED;

        if(this.progress < 1D) return;

        this.progress = 0D;

        ItemPool pool = ItemPool.get(this.getCargo());
        if(pool == null) return;

        int amount = 10 + level.random.nextInt(6);
        this.requestableSlots = NonNullList.withSize(amount, ItemStack.EMPTY);

        for(int i = 0; i < amount; i++) {
            this.requestableSlots.set(i, pool.draw(level.random));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        tag.putDouble("progress", this.progress);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        this.progress = tag.getDouble("progress");
    }
}
