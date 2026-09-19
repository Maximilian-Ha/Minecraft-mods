package com.hbm.items.weapon.sedna.mags;

import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.particle.SpentCasing;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mags.MagazineInfinite.
 *
 * Ein Magazin, das nie leer wird. Es traegt EINE feste Patronenart, gibt auf jede Frage nach
 * dem Fuellstand 9999 zurueck und nimmt nie etwas weg -- alle Setzer sind leer.
 *
 * DER ANZEIGEWERT IST EIN UNENDLICHKEITSZEICHEN, keine Zahl. Das HUD zeigt bei dieser Waffe
 * also nicht "9999", obwohl intern so gezaehlt wird.
 *
 * DAS SYMBOL IM HUD BLEIBT LEER: das Original zeigt dort ModItems.nothing, einen Gegenstand,
 * den es nur zu diesem Zweck gibt. Der Port hat ihn nicht, und ein leerer Stapel tut dasselbe,
 * ohne dass dafuer ein Gegenstand angemeldet werden muss.
 */
public class MagazineInfinite implements IMagazine<BulletConfig> {

    public BulletConfig type;

    public MagazineInfinite(BulletConfig type) {
        this.type = type;
    }

    @Override public BulletConfig getType(ItemStack stack, Container container) { return this.type; }
    @Override public void setType(ItemStack stack, BulletConfig type) { }
    @Override public int getCapacity(ItemStack stack) { return 9999; }
    @Override public int getAmount(ItemStack stack, Container container) { return 9999; }
    @Override public void setAmount(ItemStack stack, int amount) { }
    @Override public void useUpAmmo(ItemStack stack, Container container, int amount) { }
    @Override public boolean canReload(ItemStack stack, Container container) { return false; }
    @Override public void initNewType(ItemStack stack, Container container) { }
    @Override public void reloadAction(ItemStack stack, Container container) { }
    @Override public ItemStack getIconForHUD(ItemStack stack, Player player) { return ItemStack.EMPTY; }
    @Override public String reportAmmoStateForHUD(ItemStack stack, Player player) { return "∞"; }
    @Override public SpentCasing getCasing(ItemStack stack, Container container) { return this.type.casing; }
    @Override public void setAmountBeforeReload(ItemStack stack, int amount) { }
    @Override public int getAmountBeforeReload(ItemStack stack) { return 9999; }
    @Override public void setAmountAfterReload(ItemStack stack, int amount) { }
    @Override public int getAmountAfterReload(ItemStack stack) { return 9999; }
}
