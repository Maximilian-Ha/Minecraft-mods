package com.hbm.itempool;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
        return this.add(() -> new ItemStack(item, 1), min, max, weight);
    }

    /**
     * Fuer Eintraege, die kein blosser Gegenstand sind -- eine Spielart eines Metagegenstands
     * etwa. Der Lieferant bestimmt Gegenstand UND Stueckzahl; hier wird nicht mehr gewuerfelt.
     *
     * Runde 183: die Munition der C-130 liegt im Port als Spielart eines einzigen Gegenstands,
     * und die laesst sich ueber ItemLike nicht ausdruecken.
     */
    public ItemPool add(Supplier<ItemStack> supplier, int weight) {
        this.entries.add(new Entry(supplier, 0, 0, weight));
        this.totalWeight += weight;
        return this;
    }

    /**
     * Wie oben, aber mit gewuerfelter Stueckzahl: der Lieferant bestimmt nur, WAS gezogen
     * wird, der Vorrat wie viel davon.
     *
     * Runde 187: war bis hierher privat und wurde nur von der ItemLike-Fassung darueber
     * benutzt. Der Kanister der C-130 braucht beides -- eine Spielart (die Fluessigkeit) UND
     * eine Stueckzahl von eins bis vier.
     */
    public ItemPool add(Supplier<ItemStack> supplier, int min, int max, int weight) {
        this.entries.add(new Entry(supplier, min, max, weight));
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
                ItemStack stack = entry.supplier.get();
                /* min == 0 heisst: der Lieferant hat die Stueckzahl schon gesetzt. */
                if(entry.min > 0) {
                    int count = entry.min + (entry.max > entry.min ? random.nextInt(entry.max - entry.min + 1) : 0);
                    stack.setCount(Math.max(1, count));
                }
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private record Entry(Supplier<ItemStack> supplier, int min, int max, int weight) { }
}
