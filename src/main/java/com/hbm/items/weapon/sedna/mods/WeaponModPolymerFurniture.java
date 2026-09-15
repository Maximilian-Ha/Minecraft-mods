package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.Receiver;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModPolymerFurniture.
 *
 * Der Kunststoffschaft. Er wiegt weniger und liegt besser: der Ruecklauf faellt auf die Haelfte.
 * Am Aussehen aendert er mehr als am Verhalten -- gruen und schwarz sind zwei Aufsaetze mit
 * demselben Verhalten und verschiedenen Texturen, die der Renderer auswaehlt.
 */
public class WeaponModPolymerFurniture extends WeaponModBase {

    public WeaponModPolymerFurniture(int id) {
        super(id, "FURNITURE");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(Receiver.CON_ONRECOIL)) return (T) LAMBDA_RECOIL_G3;

        return base;
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_G3 = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 0.125), (float) (ctx.getPlayer().random.nextGaussian() * 0.125));
    };
}
