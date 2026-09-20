package com.hbm.items.armor;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorNCRPARanged.
 *
 * DIE SCHULTERRAKETE DER NCR-RUESTUNG. Links eine gelenkte, rechts eine ungelenkte -- sonst
 * sind beide gleich. Die Munition kommt aus dem Rucksack des Traegers, nicht aus der Waffe:
 * ein Gurtmagazin greift unmittelbar ins Inventar.
 *
 * DER LEERSCHLAG KOSTET GENAUSO VIEL ZEIT wie der Schuss (zehn Zuege Abklingen). Das steht so
 * im Original und verhindert, dass man mit leerem Rucksack schneller klicken kann als mit
 * vollem.
 *
 * Die beiden Magazine sind STATISCH und werden erst beim ersten Schuss gefuellt. Das ist
 * uebernommen: die Raketensaetze entstehen in XFactoryRocket.initAmmo, und diese Klasse wird
 * frueher geladen als jene laeuft.
 */
public class ArmorNCRPARanged implements IPARanged {

    public static final MagazineBelt GELENKT = new MagazineBelt();
    public static final MagazineBelt UNGELENKT = new MagazineBelt();

    @Override public void clickPrimary(ItemStack stack, LambdaContext ctx) { feuern(stack, ctx, true); }
    @Override public void clickSecondary(ItemStack stack, LambdaContext ctx) { feuern(stack, ctx, false); }

    public static void feuern(ItemStack stack, LambdaContext ctx, boolean gelenkt) {

        Player spieler = ctx.getPlayer();
        if(spieler == null) return;
        if(GunBaseNTItem.getState(stack, 0) != GunState.IDLE) return;

        Level level = spieler.level;
        MagazineBelt magazin = gelenkt ? GELENKT : UNGELENKT;

        if(magazin.acceptedBullets.isEmpty()) {
            magazin.addConfigs(gelenkt ? XFactoryRocket.rocket_ncrpa_steer : XFactoryRocket.rocket_ncrpa);
        }

        BulletConfig config = magazin.getType(stack, spieler.getInventory());
        int vorrat = magazin.getAmount(stack, spieler.getInventory());

        GunBaseNTItem.setState(stack, 0, GunState.COOLDOWN);
        GunBaseNTItem.setTimer(stack, 0, 10);

        if(vorrat <= 0) {
            SoundUtils.playAtVec3(level, spieler.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), spieler.getSoundSource());
            return;
        }

        magazin.useUpAmmo(stack, spieler.getInventory(), 1);

        /*
         * Der Versatz: die Rakete verlaesst den Traeger nicht aus der Mitte, sondern
         * abwechselnd links und rechts von der Schulter.
         */
        float seite = 0.25F * (spieler.getRandom().nextBoolean() ? -1F : 1F);
        BulletBaseMK4 rakete = new BulletBaseMK4(spieler, config, 25F, 0F, seite, 0F, 0F);
        level.addFreshEntity(rakete);

        SoundUtils.playAtVec3(level, spieler.position(), NtmSoundEvents.GUN_ROCKET_FIRE.get(), spieler.getSoundSource(),
                0.5F, 0.9F + spieler.getRandom().nextFloat() * 0.2F);
    }
}
