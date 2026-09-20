package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineElectricEngine;
import com.hbm.items.weapon.sedna.mags.MagazineLiquidEngine;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModCanisters.
 *
 * Die Zusatzkanister: dreimal soviel Kraftstoff, sonst nichts.
 *
 * DER KNIFF IST DIE VORLAGE. Statt bei jedem Aufruf ein neues Magazin zu bauen, hat dieser
 * Aufsatz je eine geteilte Vorlage fuer Fluid und Strom und setzt deren Werte auf die des
 * durchgereichten Magazins um -- mit verdreifachter Groesse. eval laeuft bei jedem Bild der
 * Anzeige; ein neues Objekt je Bild waere Muell im Minutentakt. Das ist im Original genauso
 * geloest, und deshalb sind index und capacity in beiden Magazinen nicht final.
 *
 * Er steht auf PRIORITY_MULT_FINAL, also ganz am Ende: er muss das Magazin sehen, das der
 * Motoraufsatz gesetzt hat, nicht das der Waffe.
 */
public class WeaponModCanisters extends WeaponModBase {

    protected static final MagazineLiquidEngine VORLAGE_FLUID = new MagazineLiquidEngine(0, 0);
    protected static final MagazineElectricEngine VORLAGE_STROM = new MagazineElectricEngine(0, 0);

    public WeaponModCanisters(int id) {
        super(id, "CANISTERS");
        this.setPriority(PRIORITY_MULT_FINAL);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(!key.equals(Receiver.O_MAGAZINE)) return base;

        if(base instanceof MagazineLiquidEngine original) {
            VORLAGE_FLUID.acceptedTypes = original.acceptedTypes;
            VORLAGE_FLUID.capacity = original.capacity * 3;
            VORLAGE_FLUID.index = original.index;
            return cast(VORLAGE_FLUID, base);
        }

        if(base instanceof MagazineElectricEngine original) {
            VORLAGE_STROM.capacity = original.capacity * 3;
            VORLAGE_STROM.index = original.index;
            return cast(VORLAGE_STROM, base);
        }

        return base;
    }

    @Override public void onInstall(Level level, ItemStack gun, ItemStack mod, int index) { XWeaponModManager.changedMagState(); }
    @Override public void onUninstall(Level level, ItemStack gun, ItemStack mod, int index) { XWeaponModManager.changedMagState(); }
}
