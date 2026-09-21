package com.hbm.items.special;

import com.hbm.entity.item.WasteItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemNuclearWaste.
 *
 * Die gemeinsame Grundlage aller Gegenstaende, die man nicht los wird: langlebiger Atommuell,
 * abgebrannter Brennstoff und die RBMK-Pellets. Zwei Dinge macht sie, und im Original macht
 * sie auch nicht mehr:
 *
 *   * DER GEGENSTAND VERFAELLT NICHT. Fuenf Minuten reichen nicht, um ein Fass Atommuell
 *     loszuwerden.
 *   * DER FALLENGELASSENE GEGENSTAND IST UNZERSTOERBAR. Er wird durch eine WasteItemEntity
 *     ersetzt, an der Feuer und Explosionen abprallen.
 *
 * Kurzlebiger Abfall gehoert NICHT hierher -- im Original leitet ItemWasteShort von Item ab,
 * nicht von ItemNuclearWaste. Er verfaellt wie jeder andere Gegenstand.
 */
public class WasteDropItem extends Item {

    public WasteDropItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {

        WasteItemEntity entity = new WasteItemEntity(level, location.getX(), location.getY(), location.getZ(), stack);
        entity.setDeltaMovement(location.getDeltaMovement());
        entity.setPickUpDelay(10);

        location.discard();

        return entity;
    }
}
