package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.Orchestras;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModGreasegun.
 *
 * Der aufgearbeitete Schaft der M3. Er macht aus der zusammengeschusterten Kriegsware eine saubere
 * Waffe: dreifache Haltbarkeit, zwei Schaden mehr, keine Streuung und die halbe Wartezeit zwischen
 * zwei Schuessen.
 *
 * Er bringt ausserdem ein eigenes Orchester mit -- der Huelsenauswurf kommt einen Tick spaeter,
 * weil das Schloss schneller laeuft. Alles uebrige reicht es an das gewoehnliche Orchester weiter.
 */
public class WeaponModGreasegun extends WeaponModBase {

    public WeaponModGreasegun(int id) {
        super(id, "FURNITURE");
        this.setPriority(PRIORITY_ADDITIVE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(GunConfig.F_DURABILITY)) return this.cast((Float) base * 3F, base);
        if(key.equals(Receiver.F_BASEDAMAGE)) return this.cast((Float) base + 2F, base);
        if(key.equals(Receiver.F_SPREADINNATE)) return this.cast(0F, base);
        if(key.equals(Receiver.I_DELAYAFTERFIRE)) return this.cast((Integer) base / 2, base);
        if(key.equals(GunConfig.CON_ORCHESTRA)) return (T) ORCHESTRA_GREASEGUN_CLEAN;

        return base;
    }

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_GREASEGUN_CLEAN = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 1) {
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.55, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, 0.18, -0.12, 0.01,
                        -7.5F + (float) entity.random.nextGaussian() * 5F, 12F + (float) entity.random.nextGaussian() * 5F, casing.getName());
            }
            return;
        }

        Orchestras.ORCHESTRA_GREASEGUN.accept(stack, ctx);
    };
}
