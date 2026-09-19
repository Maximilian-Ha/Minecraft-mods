package com.hbm.items.weapon.sedna.impl;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.impl.ItemGunNI4NI.
 *
 * Die Waffe laedt SICH SELBST MIT MUENZEN, und zwar unabhaengig davon, ob man schiesst: alle
 * achtzig Ticks kommt eine dazu, bis vier daliegen. Der Zweitdruck wirft eine davon in die
 * Luft, und der Strahl knickt an ihr ab -- das ist die ganze Waffe.
 *
 * NICHT UEBERNOMMEN: die zwei Waffenmodule des Originals (Nickel, Dublonen), die den Vorrat
 * auf sechs und acht anheben. Das Modulsystem des Ports kennt sie nicht, und ein Zuschlag auf
 * ein Modul, das es nicht gibt, waere toter Code -- der Vorrat bleibt bei vier.
 *
 * EBENFALLS NICHT UEBERNOMMEN: die drei frei einstellbaren Farben. Sie haengen im Original an
 * ICustomizable und einem Befehl, der die Farbwerte entgegennimmt; beides gibt es im Port
 * nicht. Der Zeichner nimmt deshalb immer die gewoehnliche Textur.
 */
public class NI4NIGunItem extends GunBaseNTItem {

    /** Wie lange eine Muenze zum Nachwachsen braucht, in Ticks. Zahl des Originals. */
    public static final int LADEDAUER = 80;
    /** Wie viele gleichzeitig dalegen koennen. */
    public static final int VORRAT = 4;

    public static final String KEY_COIN_COUNT = "coincount";
    public static final String KEY_COIN_CHARGE = "coincharge";

    public NI4NIGunItem(WeaponQuality quality, GunConfig... cfg) {
        super(quality, cfg);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide) return;
        if(getCoinCount(stack) >= VORRAT) return;

        int geladen = getCoinCharge(stack) + 1;

        if(geladen < LADEDAUER) {
            setCoinCharge(stack, geladen);
            return;
        }

        setCoinCharge(stack, 0);
        int neu = getCoinCount(stack) + 1;
        setCoinCount(stack, neu);

        /* Der Ton steigt mit dem Vorrat -- man hoert, wie voll die Waffe ist, ohne
         * hinzusehen. Nur wenn sie in der Hand liegt; im Rucksack laedt sie still. */
        if(isSelected && entity instanceof LivingEntity traeger) {
            SoundUtils.playAtVec3(level, traeger.position(), NtmSoundEvents.TECH_BOOP.get(),
                    traeger.getSoundSource(), 1.0F, 1F + neu / (float) VORRAT);
        }
    }

    public static int getCoinCount(ItemStack stack) { return getValueInt(stack, KEY_COIN_COUNT); }
    public static void setCoinCount(ItemStack stack, int value) { setValueInt(stack, KEY_COIN_COUNT, value); }
    public static int getCoinCharge(ItemStack stack) { return getValueInt(stack, KEY_COIN_CHARGE); }
    public static void setCoinCharge(ItemStack stack, int value) { setValueInt(stack, KEY_COIN_CHARGE, value); }
}
