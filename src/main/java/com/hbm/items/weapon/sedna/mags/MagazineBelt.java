package com.hbm.items.weapon.sedna.mags;

import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.particle.SpentCasing;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mags.MagazineBelt.
 *
 * Das Gurtmagazin. Es hat gar kein Magazin: die Waffe frisst unmittelbar aus dem Rucksack, und
 * nachgeladen wird nie. Alles, was mit Nachladen zu tun hat, ist deshalb leer -- die Waffe kommt
 * nie in den Ladezustand.
 *
 * Welche Patronenart gerade laeuft, entscheidet sich Schuss fuer Schuss neu: es ist die erste
 * zulaessige, die im Rucksack liegt. Wer die Munition wechseln will, wirft die alte weg.
 *
 * ABWEICHUNG: die Munitionstasche des Originals ist NICHT UEBERNOMMEN -- sie fehlt im Port, wie
 * schon bei den uebrigen Magazinen.
 */
public class MagazineBelt implements IMagazine<BulletConfig> {

    public List<BulletConfig> acceptedBullets = new ArrayList<>();

    public MagazineBelt addConfigs(BulletConfig... cfgs) { acceptedBullets.addAll(Arrays.asList(cfgs)); return this; }

    @Override
    public BulletConfig getType(ItemStack stack, Container container) {

        BulletConfig config = this.getFirstConfig(stack, container);

        /* Der gemerkte Typ dient nur dem Bild -- er wird nachgezogen, sobald sich etwas aendert. */
        if(getMagType(stack) != config.id) setMagType(stack, config.id);

        return config;
    }

    @Override
    public void useUpAmmo(ItemStack stack, Container container, int amount) {

        if(container == null) return;
        if(!IMagazine.shouldUseUpTrenchie(container)) return;

        BulletConfig first = this.getFirstConfig(stack, container);

        for(int i = 0; i < container.getContainerSize(); i++) {

            ItemStack slot = container.getItem(i);
            if(slot.isEmpty()) continue;

            if(first.getAmmo().matchesRecipe(slot, true)) {
                int toRemove = Math.min(slot.getCount(), amount);
                amount -= toRemove;
                container.removeItem(i, toRemove);
                IMagazine.handleAmmoBag(container, first, toRemove);
                if(amount <= 0) return;
            }
        }
    }

    @Override
    public int getAmount(ItemStack stack, Container container) {

        /* Ohne Rucksack -- etwa fuer die Zielsuche von Kreaturen -- gilt der Gurt als voll. */
        if(container == null) return 1;

        BulletConfig first = this.getFirstConfig(stack, container);
        int count = 0;

        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            if(!slot.isEmpty() && first.getAmmo().matchesRecipe(slot, true)) count += slot.getCount();
        }

        return count;
    }

    /** Die erste zulaessige Patronenart, die im Rucksack liegt. */
    public BulletConfig getFirstConfig(ItemStack stack, Container container) {

        if(container == null) return this.acceptedBullets.get(0);

        for(int i = 0; i < container.getContainerSize(); i++) {

            ItemStack slot = container.getItem(i);
            if(slot.isEmpty()) continue;

            for(BulletConfig config : this.acceptedBullets) {
                if(config.getAmmo().matchesRecipe(slot, true)) return config;
            }
        }

        /* Nichts da: der zuletzt gemerkte Typ, sonst der erste -- damit das Bild nicht springt. */
        int type = getMagType(stack);
        if(type >= 0 && type < BulletConfig.configs.size()) {
            BulletConfig cached = BulletConfig.configs.get(type);
            if(this.acceptedBullets.contains(cached)) return cached;
        }

        return this.acceptedBullets.get(0);
    }

    @Override
    public ItemStack getIconForHUD(ItemStack stack, Player player) {
        return this.getFirstConfig(stack, player.getInventory()).getAmmo().toStack();
    }

    @Override
    public String reportAmmoStateForHUD(ItemStack stack, Player player) {
        return "x" + this.getAmount(stack, player.getInventory());
    }

    @Override
    public SpentCasing getCasing(ItemStack stack, Container container) {
        return this.getFirstConfig(stack, container).casing;
    }

    /* Ein Gurt wird nicht nachgeladen -- alles Folgende ist deshalb wirkungslos. */
    @Override public void setType(ItemStack stack, BulletConfig type) { }
    @Override public int getCapacity(ItemStack stack) { return 0; }
    @Override public void setAmount(ItemStack stack, int amount) { }
    @Override public boolean canReload(ItemStack stack, Container container) { return false; }
    @Override public void initNewType(ItemStack stack, Container container) { }
    @Override public void reloadAction(ItemStack stack, Container container) { }
    @Override public void setAmountBeforeReload(ItemStack stack, int amount) { }
    @Override public int getAmountBeforeReload(ItemStack stack) { return 0; }
    @Override public void setAmountAfterReload(ItemStack stack, int amount) { }
    @Override public int getAmountAfterReload(ItemStack stack) { return 0; }

    public static final String KEY_MAG_TYPE = "magtype";
    public static int getMagType(ItemStack stack) { return GunBaseNTItem.getValueInt(stack, KEY_MAG_TYPE); }
    public static void setMagType(ItemStack stack, int value) { GunBaseNTItem.setValueInt(stack, KEY_MAG_TYPE, value); }
}
