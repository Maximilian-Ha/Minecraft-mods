package com.hbm.entity.item;

import com.hbm.entity.NtmEntityTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.item.EntityItemWaste.
 *
 * Der Gegenstand, zu dem fallengelassener Atommuell wird. Er tut genau EINE Sache: er laesst
 * sich nicht zerstoeren. Feuer, Lava, Kakteen und Explosionen gehen wirkungslos an ihm vorbei,
 * und das ist im Original auch alles -- er leuchtet nicht, er verstrahlt nichts, und dass er
 * nicht verfaellt, kommt nicht von ihm, sondern von getEntityLifespan des Gegenstandes
 * (siehe WasteDropItem).
 *
 * Nachgemessen am Original: zwei Ueberschreibungen, isEntityInvulnerable und attackEntityFrom,
 * beide mit fester Antwort. Mehr steht dort nicht.
 */
public class WasteItemEntity extends ItemEntity {

    public WasteItemEntity(EntityType<? extends WasteItemEntity> type, Level level) {
        super(type, level);
    }

    /**
     * Baut den Ersatz fuer einen gewoehnlichen Gegenstand an derselben Stelle. Die Lebensdauer
     * setzt setItem aus dem Gegenstand heraus, der Schwung wird vom Ersetzten uebernommen.
     */
    public WasteItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(NtmEntityTypes.WASTE_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360F);
        this.setItem(stack);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
