package com.hbm.items.weapon.sedna.impl;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.impl.ItemGunChargeThrower.
 *
 * Der Ladungswerfer. Nach aussen eine Waffe mit drei Munitionsarten, nach innen ein SEIL: die
 * eine davon, der Enterhaken, bleibt in der Wand stecken, und solange er dort haengt, zieht
 * diese Klasse den Schuetzen an ihm entlang.
 *
 * DREI ZUSTAENDE, und der Unterschied macht die Waffe aus:
 *
 *   Linke Taste gedrueckt -- der Schuetze wird zum Haken gezogen. Kommt er ihm naeher als
 *     zwei Bloecke, loest sich der Haken auf; sonst klebte man an der Wand.
 *   Rechte Taste gedrueckt -- das Seil ist lose, der Schuetze faellt frei.
 *   Keine von beiden -- das Seil ist STRAFF. Wuerde der naechste Schritt den Abstand
 *     vergroessern, wird er auf die alte Seillaenge zurueckgeholt: daraus wird ein Pendel.
 *
 * Solange das Seil zieht, faellt der Schuetze nicht -- fallDistance geht auf null, sobald er
 * sich nicht mehr schnell abwaerts bewegt.
 *
 * NICHT UEBERNOMMEN: das Original setzt zusaetzlich die Flugzeit des Jetpacks zurueck
 * (ArmorUtil.resetFlightTime). Diese Zeitrechnung gibt es im Port nicht -- die einzige Stelle,
 * die sie je gerufen haette, steht in EntityEffectHandler auskommentiert.
 */
public class GunChargeThrowerItem extends GunBaseNTItem {

    public static final String KEY_LASTHOOK = "lasthook";

    public GunChargeThrowerItem(WeaponQuality quality, GunConfig... cfg) {
        super(quality, cfg);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        /* Beim Nachladen wird das Seil gekappt -- eine neue Ladung, ein neuer Haken. */
        if(getState(stack, 0) == GunState.RELOADING) {
            if(getLastHook(stack) != -1) setLastHook(stack, -1);
        }

        if(!isSelected || !(entity instanceof Player spieler)) {
            if(getLastHook(stack) != -1) setLastHook(stack, -1);
            return;
        }

        Entity haken = level.getEntity(getLastHook(stack));

        if(!(haken instanceof BulletBaseMK4 geschoss) || !haken.isAlive()
                || geschoss.config != XFactoryTool.ct_hook || geschoss.velocity >= 0.01F) return;

        HbmPlayerAttachments daten = HbmPlayerAttachments.getData(spieler);

        Vec3 augen = new Vec3(spieler.getX(), spieler.getY() + spieler.getEyeHeight(), spieler.getZ());
        Vec3 zumHaken = haken.position().subtract(augen);
        double seil = zumHaken.length();

        if(daten.getKeyPressed(EnumKeybind.GUN_PRIMARY)) {

            Vec3 zug = zumHaken.normalize().scale(0.1D);
            spieler.setDeltaMovement(spieler.getDeltaMovement().add(zug.x, zug.y + 0.04D, zug.z));
            if(!level.isClientSide && seil < 2D) haken.discard();

        } else if(!daten.getKeyPressed(EnumKeybind.GUN_SECONDARY)) {

            /*
             * Das straffe Seil. Wohin der naechste Schritt fuehren WUERDE, davon wird der
             * Abstand zum Haken genommen; ist er groesser als die Seillaenge, wird der Punkt
             * auf den Kreis um den Haken zurueckgeholt und die Bewegung neu daraus gerechnet.
             * Die Schranke von drei Bloecken je Zug faengt den Fall ab, dass das Seil in
             * einem Zug straff wird und den Schuetzen sonst wegschleuderte.
             */
            Vec3 naechster = augen.add(spieler.getDeltaMovement());
            Vec3 delta = haken.position().subtract(naechster);

            if(delta.length() > seil) {
                Vec3 geholt = haken.position().subtract(delta.normalize().scale(seil));
                Vec3 neu = geholt.subtract(augen);
                if(neu.length() < 3D) spieler.setDeltaMovement(neu);
            }

        } else {
            /* Loses Seil: der Schwung laeuft aus. */
            spieler.setDeltaMovement(spieler.getDeltaMovement().scale(0.5D));
        }

        if(spieler.getDeltaMovement().y > -0.1D) spieler.fallDistance = 0F;
        spieler.hurtMarked = true;
    }

    public static int getLastHook(ItemStack stack) { return getValueInt(stack, KEY_LASTHOOK); }
    public static void setLastHook(ItemStack stack, int value) { setValueInt(stack, KEY_LASTHOOK, value); }
}
