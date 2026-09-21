package com.hbm.util;

import com.hbm.blockentity.LootDecoBlockEntity;
import com.hbm.inventory.MetaHelper;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsPile;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Portiert aus 1.7.10: com.hbm.util.LootGenerator.
 *
 * Was auf einem Beutesockel liegt, ist kein Vorrat, sondern ein REZEPT: eine feste Anordnung
 * mit gewuerfeltem Inhalt. "Vier Kronkorken rechts, zwei Spritzen links, eine Rakete davor" --
 * die Stellen stehen fest, gezogen wird nur, was dort liegt.
 *
 * VIER REZEPTE BRAUCHT DAS METEORITENVERLIES. Drei davon stehen hier; das vierte,
 * LOOT_METEOR, braucht das MKU-Raetsel des Originals (MKUCraftingHandler) -- ein je Welt neu
 * gewuerfeltes Rezept samt Buch, das darauf hinweist. Weder das Raetsel noch seine Zutaten
 * (ingot_mercury, syringe_mkunicorn) noch das Lorebuch gibt es im Port. Bis dahin bleibt der
 * Sockel im MKU-Stueck leer; er wird nicht mit etwas anderem gefuellt.
 *
 * DAS ORIGINAL HAT VIERZEHN REZEPTE. Die uebrigen zehn gehoeren zu Bauwerken, die der Port
 * noch nicht baut.
 */
public class LootGenerator {

    public static final String LOOT_CAPNUKE = "LOOT_CAPNUKE";
    public static final String LOOT_MEDICINE = "LOOT_MEDICINE";
    public static final String LOOT_CAPSTASH = "LOOT_CAPSTASH";
    public static final String LOOT_METEOR = "LOOT_METEOR";
    /** Runde 303: der Glyphidenbau. Knochen in der Kammer, Beute im Kern. */
    public static final String LOOT_BONES = "LOOT_BONES";
    public static final String LOOT_GLYPHID_HIVE = "LOOT_GLYPHID_HIVE";

    /**
     * Legt die Beute auf den Sockel an der angegebenen Stelle. Steht dort kein Sockel oder
     * liegt schon etwas darauf, geschieht nichts -- so wie im Original.
     */
    public static void applyLoot(Level level, BlockPos pos, String name) {

        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof LootDecoBlockEntity sockel)) return;
        if(!sockel.items.isEmpty()) return;

        RandomSource random = level.getRandom();

        switch(name) {
            case LOOT_CAPNUKE -> lootCapNuke(sockel, random);
            case LOOT_MEDICINE -> lootMedicine(sockel, random);
            case LOOT_CAPSTASH -> lootCapStash(sockel, random);
            case LOOT_BONES -> haufen(sockel, random, ItemPoolsPile.POOL_PILE_BONES);
            case LOOT_GLYPHID_HIVE -> haufen(sockel, random, ItemPoolsPile.POOL_PILE_HIVE);
            /* LOOT_METEOR: siehe Klassenkopf. Der Sockel bleibt leer. */
            default -> { return; }
        }

        sockel.setChanged();
    }

    /**
     * Der Versatz bekommt in x und z ein wenig Streuung, damit ein Stapel nicht genau auf dem
     * anderen liegt. Das Original nimmt dafuer eine Normalverteilung mit Faktor 0,02.
     */
    private static void addItemWithDeviation(LootDecoBlockEntity sockel, RandomSource random,
                                            ItemStack stack, double x, double y, double z) {
        if(stack == null || stack.isEmpty()) return;
        sockel.addItem(stack, x + random.nextGaussian() * 0.02, y, z + random.nextGaussian() * 0.02);
    }

    /** Eine Rakete oder -- in einem von fuenf Faellen -- eine Minikernwaffe, dazu Kronkorken. */
    private static void lootCapNuke(LootDecoBlockEntity sockel, RandomSource random) {

        if(random.nextInt(5) == 0) {
            addItemWithDeviation(sockel, random, munition(Ammo.NUKE_STANDARD), -0.25, 0, -0.125);
        } else {
            addItemWithDeviation(sockel, random, munition(Ammo.ROCKET_HEAT), -0.25, 0, -0.25);
        }

        for(int i = 0; i < 4; i++)
            addItemWithDeviation(sockel, random, new ItemStack(NtmItems.CAP_NUKA.get(), 2), 0.125, i * 0.03125, 0.25);
        for(int i = 0; i < 2; i++)
            addItemWithDeviation(sockel, random, new ItemStack(NtmItems.SYRINGE_METAL_STIMPAK.get(), 1), -0.25, i * 0.03125, 0.25);
        for(int i = 0; i < 6; i++)
            addItemWithDeviation(sockel, random, new ItemStack(NtmItems.CAP_NUKA.get(), 2), 0.125, i * 0.03125, -0.25);
    }

    /**
     * Ein schlichter Haufen: drei bis fuenf Zuege aus dem Vorrat, jeder an einer zufaelligen
     * Stelle des Sockels und ein Zweiunddreissigstel hoeher als der davor. Das Original hat
     * dafuer zwei Methoden mit demselben Rumpf (lootBones und lootGlyphidHive); hier ist es
     * eine mit dem Vorrat als Angabe.
     */
    private static void haufen(LootDecoBlockEntity sockel, RandomSource random, String vorrat) {

        ItemPool pool = ItemPool.get(vorrat);
        if(pool == null) return;

        int anzahl = random.nextInt(3) + 3;

        for(int i = 0; i < anzahl; i++) {
            addItemWithDeviation(sockel, random, pool.draw(random),
                    random.nextDouble() - 0.5, i * 0.03125, random.nextDouble() - 0.5);
        }
    }

    /** Vier Spritzen und eine Handvoll Pillen. */
    private static void lootMedicine(LootDecoBlockEntity sockel, RandomSource random) {

        ItemPool spritzen = ItemPool.get(ItemPoolsPile.POOL_PILE_MED_SYRINGE);
        ItemPool pillen = ItemPool.get(ItemPoolsPile.POOL_PILE_MED_PILLS);
        if(spritzen == null || pillen == null) return;

        for(int i = 0; i < 4; i++)
            addItemWithDeviation(sockel, random, spritzen.draw(random), 0.125, i * 0.03125, 0.25);
        addItemWithDeviation(sockel, random, pillen.draw(random), -0.25, 0, -0.125);
    }

    /** Neun Haeufchen Kronkorken im Dreierraster, jedes drei bis sieben Zuege hoch. */
    private static void lootCapStash(LootDecoBlockEntity sockel, RandomSource random) {

        ItemPool korken = ItemPool.get(ItemPoolsPile.POOL_PILE_CAPS);
        if(korken == null) return;

        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                int anzahl = random.nextInt(5) + 3;
                for(int k = 0; k < anzahl; k++) {
                    addItemWithDeviation(sockel, random, korken.draw(random), i * 0.3125, k * 0.03125, j * 0.3125);
                }
            }
        }
    }

    /** Die Munition liegt im Port als Spielart eines einzigen Gegenstands. */
    private static ItemStack munition(Ammo art) {
        return MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 1, art.ordinal());
    }
}
