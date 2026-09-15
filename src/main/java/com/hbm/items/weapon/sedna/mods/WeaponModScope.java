package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.XFactory556mm;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModScope.
 *
 * Das Zielfernrohr. Es blendet beim Anlegen das Bild der Optik ueber den Schirm und nimmt dafuer
 * das Fadenkreuz weg -- wer durch das Rohr sieht, braucht keines.
 *
 * ABWEICHUNG: das Original waehlt das Bild nach Waffe aus -- eines fuer den schweren Revolver,
 * eines fuer die Werkzeuge, das Standardbild fuer alles andere. Weder der schwere Revolver noch
 * die Werkzeuge stehen im Port; die Sonderfaelle kommen mit ihnen. Bis dahin traegt jede Waffe
 * das Standardbild.
 */
public class WeaponModScope extends WeaponModBase {

    public WeaponModScope(int id) {
        super(id, "SCOPE");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(GunConfig.O_SCOPETEXTURE)) return (T) XFactory556mm.SCOPE;
        if(key.equals(GunConfig.B_HIDECROSSHAIR)) return cast(true, base);

        return base;
    }
}
