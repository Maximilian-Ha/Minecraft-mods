package com.hbm.saveddata;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.saveddata.AnnihilatorSavedData.
 *
 * Das Gedaechtnis des Annihilators. Es merkt sich je Vorrat ("Pool"), wie viel von jeder Sorte
 * schon vernichtet worden ist -- und schuettet aus, sobald eine Schwelle ueberschritten wird.
 *
 * DIE ZAEHLER SIND BELIEBIG GROSS. Das Original nimmt dafuer BigInteger, und das ist kein
 * Uebermut: wer eine Fabrik an den Annihilator haengt, kommt auf Zahlen, an denen ein long
 * irgendwann zerbricht. Gespeichert werden sie als Byte-Feld.
 *
 * VIER SCHLUESSELARTEN kennt ein Pool: den Gegenstand (fuer alle Metawerte auf einmal), den
 * ComparableStack (Gegenstand samt Metawert), den Fluidtyp und den Erzwoerterbuch-Namen.
 *
 * ABWEICHUNG: die vierte Art faellt hier weg. Ein Erzwoerterbuch hat der Port nicht, und ohne
 * das gibt es auch keine Namen, die man zaehlen koennte. Die Stelle im Format bleibt erhalten --
 * Schluesselart 3 wird beim Lesen uebersprungen statt fehlzuschlagen, damit ein Stand aus einer
 * spaeteren Fassung nicht daran zerbricht.
 */
public class AnnihilatorSavedData extends SavedData {

    public static final String KEY = "annihilator";

    public static Factory<AnnihilatorSavedData> factory() {
        return new Factory<>(AnnihilatorSavedData::new, AnnihilatorSavedData::load);
    }

    public final Map<String, AnnihilatorPool> pools = new HashMap<>();

    public AnnihilatorSavedData() {
        this.setDirty();
    }

    public static AnnihilatorSavedData load(CompoundTag tag, HolderLookup.Provider registries) {

        AnnihilatorSavedData data = new AnnihilatorSavedData();
        ListTag pools = tag.getList("pools", Tag.TAG_COMPOUND);

        for(int i = 0; i < pools.size(); i++) {
            CompoundTag poolTag = pools.getCompound(i);
            AnnihilatorPool pool = new AnnihilatorPool();
            pool.deserialize(poolTag.getList("pool", Tag.TAG_COMPOUND));
            data.pools.put(poolTag.getString("poolname"), pool);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        ListTag pools = new ListTag();

        for(Entry<String, AnnihilatorPool> entry : this.pools.entrySet()) {
            CompoundTag poolTag = new CompoundTag();
            ListTag inhalt = new ListTag();
            entry.getValue().serialize(inhalt);
            poolTag.putString("poolname", entry.getKey());
            poolTag.put("pool", inhalt);
            pools.add(poolTag);
        }

        tag.put("pools", pools);
        return tag;
    }

    public static AnnihilatorSavedData getData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), KEY);
    }

    public AnnihilatorPool grabPool(String pool) {
        return this.pools.computeIfAbsent(pool, k -> new AnnihilatorPool());
    }

    /** Fuer Fluide. */
    public ItemStack pushToPool(String pool, FluidType type, long amount, boolean alwaysPayOut) {
        ItemStack payout = this.grabPool(pool).increment(type, amount, alwaysPayOut);
        this.setDirty();
        return payout;
    }

    /**
     * Fuer Gegenstaende. Gezaehlt wird ZWEIMAL: einmal auf den Gegenstand allein, einmal auf
     * Gegenstand samt Metawert. So laesst sich eine Schwelle wahlweise fuer eine einzelne Sorte
     * oder fuer alle Sorten zusammen setzen.
     */
    public ItemStack pushToPool(String pool, ItemStack stack, boolean alwaysPayOut) {

        AnnihilatorPool poolInstance = this.grabPool(pool);

        ItemStack itemPayout = poolInstance.increment(stack.getItem(), stack.getCount(), alwaysPayOut);
        ItemStack compPayout = poolInstance.increment(new ComparableStack(stack).makeSingular(), stack.getCount(), alwaysPayOut);

        this.setDirty();

        return !compPayout.isEmpty() ? compPayout : itemPayout;
    }

    public static class AnnihilatorPool {

        /** Schluessel sind Item, ComparableStack oder FluidType -- siehe Klassenkommentar. */
        public final Map<Object, BigInteger> items = new HashMap<>();

        public ItemStack increment(Object type, long amount, boolean alwaysPayOut) {

            BigInteger counter = this.items.get(type);
            ItemStack payout;

            if(counter == null) {
                counter = BigInteger.valueOf(amount);
                payout = AnnihilatorRecipes.getHighestPayoutFromKey(type, BigInteger.ZERO, counter);
            } else {
                BigInteger prev = counter;
                counter = counter.add(BigInteger.valueOf(amount));
                payout = AnnihilatorRecipes.getHighestPayoutFromKey(type, alwaysPayOut ? null : prev, counter);
            }

            this.items.put(type, counter);
            return payout;
        }

        public void serialize(ListTag list) {
            for(Entry<Object, BigInteger> entry : this.items.entrySet()) {
                CompoundTag tag = new CompoundTag();
                if(!serializeKey(tag, entry.getKey())) continue;
                tag.putByteArray("amount", entry.getValue().toByteArray());
                list.add(tag);
            }
        }

        public void deserialize(ListTag list) {
            for(int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                Object key = deserializeKey(tag);
                if(key != null) this.items.put(key, new BigInteger(tag.getByteArray("amount")));
            }
        }

        private static boolean serializeKey(CompoundTag tag, Object key) {

            if(key instanceof Item item) {
                tag.putByte("key", (byte) 0);
                tag.putString("item", BuiltInRegistries.ITEM.getKey(item).toString());
                return true;
            }

            if(key instanceof ComparableStack comp) {
                tag.putByte("key", (byte) 1);
                tag.putString("item", BuiltInRegistries.ITEM.getKey(comp.item).toString());
                tag.putShort("meta", (short) comp.meta);
                return true;
            }

            if(key instanceof FluidType type) {
                tag.putByte("key", (byte) 2);
                tag.putString("fluid", type.getUnlocalizedName());
                return true;
            }

            return false;
        }

        private static Object deserializeKey(CompoundTag tag) {

            try {
                byte key = tag.getByte("key");

                if(key == 0) return BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("item")));
                if(key == 1) return new ComparableStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("item"))), 1, tag.getShort("meta"));
                if(key == 2) return Fluids.fromName(tag.getString("fluid"));

                /* Schluesselart 3 war im Original der Erzwoerterbuch-Name. Den gibt es hier
                 * nicht; die Zeile wird uebersprungen, nicht als Fehler behandelt. */

            } catch(Throwable ignored) {
                /* Weltdaten koennen beliebig zerdellt sein -- eine unlesbare Zeile kostet einen
                 * Zaehler, nicht den ganzen Stand. So steht es auch im Original. */
            }

            return null;
        }
    }
}
