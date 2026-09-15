package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory45.
 *
 * Die Fabrik fuer die .45-Patronen. Sie hat keine eigene Waffe -- im Original verschiessen sie
 * die Thompson und die Liberator, die beide noch fehlen. Die Patronen stehen trotzdem schon, weil
 * sie ueber GunFactory.Ammo bereits Gegenstaende sind; ohne diese Zeilen haetten sie keine Werte.
 */
public class XFactory45 {

    public static BulletConfig p45_sp;
    public static BulletConfig p45_fmj;
    public static BulletConfig p45_jhp;
    public static BulletConfig p45_ap;
    public static BulletConfig p45_du;

    public static void init() {

        SpentCasing casing45 = new SpentCasing(SpentCasingType.STRAIGHT).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(1F, 1F, 0.75F);

        p45_sp = new BulletConfig().setItem(Ammo.P45_SP).setCasing(CasingType.SMALL, 8)
                .setCasing(casing45.clone().register("p45"));
        p45_fmj = new BulletConfig().setItem(Ammo.P45_FMJ).setCasing(CasingType.SMALL, 8).setDamage(0.8F).setThresholdNegation(2F).setArmorPiercing(0.1F)
                .setCasing(casing45.clone().register("p45fmj"));
        p45_jhp = new BulletConfig().setItem(Ammo.P45_JHP).setCasing(CasingType.SMALL, 8).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing45.clone().register("p45jhp"));
        p45_ap = new BulletConfig().setItem(Ammo.P45_AP).setCasing(CasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(5F).setArmorPiercing(0.15F)
                .setCasing(casing45.clone().setColor(SpentCasing.COLOR_CASE_44).register("p45ap"));
        p45_du = new BulletConfig().setItem(Ammo.P45_DU).setCasing(CasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(2.5F).setThresholdNegation(15F).setArmorPiercing(0.25F)
                .setCasing(casing45.clone().setColor(SpentCasing.COLOR_CASE_44).register("p45du"));
    }
}
