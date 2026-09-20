package com.hbm.items.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.IPAWeaponsProvider.
 *
 * DIE WESTE ENTSCHEIDET. Beide Panzerruestungswaffen fragen ueber diese Schnittstelle, was sie
 * duerfen -- und gefragt wird ausschliesslich die BRUSTPLATTE. Das ist nicht willkuerlich: die
 * Brustplatte ist auch sonst das Stueck, das im FSB-Satz den Ton angibt (sie kennt den
 * Werkstoff, sie weiss, ob ein Helm dazugehoert, und aus ihr kommt der Geigerton).
 *
 * Wer keine solche Weste traegt, bekommt null, und die Waffe tut nichts.
 */
public interface IPAWeaponsProvider {

    @Nullable IPAMelee getMeleeComponent(Player player);
    @Nullable IPARanged getRangedComponent(Player player);

    static @Nullable IPAMelee getMeleeComponentCommon(@Nullable Player player) {
        if(player == null) return null;
        if(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IPAWeaponsProvider provider) {
            return provider.getMeleeComponent(player);
        }
        return null;
    }

    static @Nullable IPARanged getRangedComponentCommon(@Nullable Player player) {
        if(player == null) return null;
        if(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IPAWeaponsProvider provider) {
            return provider.getRangedComponent(player);
        }
        return null;
    }

    /**
     * Fuer den Zeichner und die Bewegungssaetze: dort gibt es keinen Zusammenhang, aus dem der
     * Spieler herauszulesen waere, also nimmt man den am Bildschirm. Im Original heisst diese
     * Stelle MainRegistry.proxy.me().
     */
    @OnlyIn(Dist.CLIENT)
    static @Nullable IPAMelee getMeleeComponentClient() {
        return getMeleeComponentCommon(Minecraft.getInstance().player);
    }
}
