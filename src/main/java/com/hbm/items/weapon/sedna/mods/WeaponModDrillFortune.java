package com.hbm.items.weapon.sedna.mods;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModDrillFortune.
 *
 * Magnet und Sieb. Sie greifen nicht in die Waffenwerte ein -- eval reicht alles unveraendert
 * durch --, sondern SCHREIBEN EINE VERZAUBERUNG IN DEN GEGENSTAND: der Magnet zwei Stufen
 * Glueck, das Sieb eine. Beim Abbauen wird dieselbe Zahl wieder abgezogen.
 *
 * Beide belegen verschiedene Plaetze (MAGNET, SIFTER) und lassen sich deshalb stapeln.
 *
 * ABWEICHUNG: in 1.21 sind Verzauberungen datengetrieben und nur ueber die Registry der
 * laufenden Welt zu bekommen -- deshalb reichen die beiden Haken die Welt mit. Das Original
 * kommt ohne aus, weil es dort ein festes Enchantment.fortune-Feld gibt.
 */
public class WeaponModDrillFortune extends WeaponModBase {

    protected final int addFortune;

    public WeaponModDrillFortune(int id, String slot, int fortune) {
        super(id, slot);
        this.setPriority(PRIORITY_ADDITIVE);
        this.addFortune = fortune;
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {
        return base;
    }

    @Override
    public void onInstall(Level level, ItemStack gun, ItemStack mod, int index) {
        this.verschiebeGlueck(level, gun, this.addFortune);
    }

    @Override
    public void onUninstall(Level level, ItemStack gun, ItemStack mod, int index) {
        this.verschiebeGlueck(level, gun, -this.addFortune);
    }

    /**
     * Zieht die vorhandene Glueckstufe heraus, rechnet den Betrag darauf und schreibt sie
     * zurueck. Faellt sie dabei auf null oder darunter, wird die Verzauberung ganz entfernt --
     * sonst bliebe ein "Glueck 0" im Gegenstand stehen, das der Spieler in der Beschreibung
     * sieht.
     */
    private void verschiebeGlueck(Level level, ItemStack gun, int betrag) {

        Holder<Enchantment> glueck = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE);

        int stufe = EnchantmentHelper.getItemEnchantmentLevel(glueck, gun) + betrag;

        EnchantmentHelper.updateEnchantments(gun, mutable -> {
            if(stufe > 0) mutable.set(glueck, stufe);
            else mutable.set(glueck, 0);
        });
    }
}
