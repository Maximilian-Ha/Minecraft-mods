package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.XFactoryDrill;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModDrill.
 *
 * Der Bohrkopf. Er ist der einzige Aufsatz, der wirklich ALLES am Bohrer anfasst: Schaden,
 * Reichweite, Panzerbruch und die Kantenlaenge des Wuerfels, den ein Schlag heraushebt.
 *
 * DER SCHADEN IST EIN FAKTOR, alles andere ein fester Wert. Deshalb steht bei damage und
 * reach eine Multiplikation und bei den uebrigen eine Zuweisung -- und deshalb sind die
 * uebrigen mit -1 vorbelegt: ein Bohrkopf, der einen Wert nicht setzt, laesst ihn stehen.
 *
 * ABWEICHUNG: die Abbaustufe (I_HARVEST) ist NICHT dabei. Das Original hebt mit dem Bohrkopf
 * an, was der Bohrer ueberhaupt abbauen darf; im Port bricht er ohnehin alles
 * (GunDrillItem.isCorrectToolForDrops gibt stets wahr), weil 1.21 keine Abbaustufe als Zahl
 * mehr kennt. Ein Aufsatz auf eine Zahl, die niemand liest, waere Code ohne Wirkung.
 */
public class WeaponModDrill extends WeaponModBase {

    protected float damage = 1F;
    protected double reach = 1D;
    protected float dt = -1F;
    protected float pierce = -1F;
    protected int aoe = -1;

    public WeaponModDrill(int id) {
        super(id, "DRILL");
        this.setPriority(PRIORITY_SET);
    }

    public WeaponModDrill damage(float damage) { this.damage = damage; return this; }
    public WeaponModDrill reach(double reach) { this.reach = reach; return this; }
    public WeaponModDrill dt(float dt) { this.dt = dt; return this; }
    public WeaponModDrill pierce(float pierce) { this.pierce = pierce; return this; }
    public WeaponModDrill aoe(int aoe) { this.aoe = aoe; return this; }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.F_BASEDAMAGE)) return cast((Float) base * this.damage, base);
        if(key.equals(XFactoryDrill.D_REACH)) return cast((Double) base * this.reach, base);
        if(key.equals(XFactoryDrill.F_DTNEG) && this.dt >= 0F) return cast(this.dt, base);
        if(key.equals(XFactoryDrill.F_PIERCE) && this.pierce >= 0F) return cast(this.pierce, base);
        if(key.equals(XFactoryDrill.I_AOE) && this.aoe >= 0) return cast(this.aoe, base);

        return base;
    }
}
