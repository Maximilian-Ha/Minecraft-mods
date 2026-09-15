package com.hbm.itempool;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.itempool.ItemPool.
 *
 * Ein benannter Vorrat gewichteter Gegenstaende. Das Original stuetzt sich dafuer auf
 * WeightedRandomChestContent aus 1.7.10; auf 1.21 gibt es die Klasse nicht mehr, und der
 * Nachbau hier ist ohnehin kuerzer als die Anpassung waere.
 *
 * DIE GEWICHTE SIND RELATIV, nicht Prozente: ein Eintrag mit Gewicht 15 kommt dreimal so oft
 * wie einer mit Gewicht 5. Die Stueckzahl wird je Zug zwischen min und max gewuerfelt.
 */
public class ItemPool {

    private static final Map<String, ItemPool> POOLS = new HashMap<>();

    private final List<Entry> entries = new ArrayList<>();
    private int totalWeight = 0;

    private ItemPool() { }

    public static ItemPool getOrCreate(String name) {
        return POOLS.computeIfAbsent(name, k -> new ItemPool());
    }

    public static ItemPool get(String name) {
        return POOLS.get(name);
    }

    public ItemPool add(ItemLike item, int min, int max, int weight) {
        this.entries.add(new Entry(item, min, max, weight));
        this.totalWeight += weight;
        return this;
    }

    /** Ein Zug aus dem Vorrat. Ein leerer Vorrat liefert einen leeren Stapel, keinen Fehler. */
    public ItemStack draw(RandomSource random) {

        if(this.entries.isEmpty() || this.totalWeight <= 0) return ItemStack.EMPTY;

        int roll = random.nextInt(this.totalWeight);

        for(Entry entry : this.entries) {
            roll -= entry.weight;
            if(roll < 0) {
                int count = entry.min + (entry.max > entry.min ? random.nextInt(entry.max - entry.min + 1) : 0);
                return new ItemStack(entry.item, Math.max(1, count));
            }
        }

        return ItemStack.EMPTY;
    }

    private record Entry(ItemLike item, int min, int max, int weight) { }
}
