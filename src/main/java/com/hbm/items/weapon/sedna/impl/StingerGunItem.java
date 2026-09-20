package com.hbm.items.weapon.sedna.impl;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.util.RenderScreenOverlay;
import com.hbm.util.SoundUtils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.impl.ItemGunStinger.
 *
 * DER STINGER IST DIE EINZIGE WAFFE DES PORTS MIT EINER AUFSCHALTUNG. Die rechte Taste haelt
 * den Sucher an; solange sie gedrueckt bleibt, gezielt wird und eine Rakete im Rohr steckt,
 * sucht er sich ein Ziel und laeuft darauf zu. Nach sechzig Zuegen -- drei Sekunden -- rastet
 * er ein, und erst dann feuert die linke Taste ueberhaupt.
 *
 * DREI REGELN AUS DEM ORIGINAL, und jede hat einen Grund:
 *
 *   * WER DIE WAFFE WEGSTECKT, verliert den Sucher. Ohne das liefe er im Rucksack weiter.
 *   * EIN NEUES ZIEL SETZT DEN FORTSCHRITT ZURUECK -- aber nur, solange noch nicht
 *     eingerastet ist. Wer eingerastet hat, behaelt sein Ziel, auch wenn der Blick abgleitet.
 *   * FINDET DER SUCHER NICHTS, faellt der Fortschritt zurueck; auch hier nur, solange noch
 *     nicht eingerastet ist.
 *
 * DER BALKEN UNTER DEM FADENKREUZ zeigt den Fortschritt. Er steht in derselben Bildleiste wie
 * das Fadenkreuz und wird deshalb hier gezeichnet, nicht als HUD-Bauteil: er gehoert zur
 * Mitte des Schirms, nicht an den Rand.
 *
 * DIE ZIELSUCHE SELBST steht in XFactoryRocket.sucheZiel -- sie kam mit dem Raketenwerfer in
 * Runde 197, der sie ebenfalls braucht.
 */
public class StingerGunItem extends GunBaseNTItem {

    public static final String KEY_LOCKINGON = "lockingon";
    public static final String KEY_LOCKONPROGRESS = "lockonprogress";

    /** Die Zuege bis zum Einrasten. Im Original dieselbe Zahl. */
    public static final int LOCKON_DAUER = 60;

    /**
     * Der Fortschritt, wie ihn der Client zeichnet. Er ist statisch, weil der Balken immer nur
     * fuer die Waffe in der eigenen Hand gilt -- genau wie aimingProgress in der Oberklasse.
     *
     * ABWEICHUNG: das Original fuehrt daneben ein prevLockon fuer die Zwischenrechnung und
     * BENUTZT ES NIRGENDS -- renderStingerLockon rechnet mit lockon allein. Ein totes Feld
     * wird hier nicht uebernommen.
     *
     * OHNE @OnlyIn, UND DAS MIT ABSICHT: das Feld und die Methode, die es fuehrt, werden aus
     * inventoryTick gerufen, und inventoryTick laeuft auf beiden Seiten. Waere beides
     * client-seitig gekennzeichnet, fehlte es auf dem Server, und der Aufruf ginge ins Leere.
     * Ein float braucht dort auch nichts, was es nicht gibt.
     */
    public static float lockon;

    public StingerGunItem(WeaponQuality quality, GunConfig... cfg) {
        super(quality, cfg);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(!(entity instanceof Player player)) return;

        if(level.isClientSide) {
            this.tickLockonDisplay(stack, isSelected);
            return;
        }

        if(!isSelected && getIsLockingOn(stack)) setIsLockingOn(stack, false);

        int vorherigesZiel = getLockonTarget(stack);
        boolean sucht = isSelected && getIsLockingOn(stack) && getIsAiming(stack)
                && this.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, player.inventory) > 0;

        if(!sucht) {
            this.resetLockon(stack);
            return;
        }

        int neuesZiel = XFactoryRocket.sucheZiel(player, 150D, 10D);

        if(neuesZiel == -1) {
            if(!getIsLockedOn(stack)) this.resetLockon(stack);
            return;
        }

        if(!getIsLockedOn(stack) && neuesZiel != vorherigesZiel) {
            this.resetLockon(stack);
            setLockonTarget(stack, neuesZiel);
        }

        setLockonProgress(stack, getLockonProgress(stack) + 1);

        if(getLockonProgress(stack) >= LOCKON_DAUER && !getIsLockedOn(stack)) {
            SoundUtils.playAtVec3(level, player.position(), NtmSoundEvents.TECH_BLEEP.get(), player.getSoundSource());
            setIsLockedOn(stack, true);
        }
    }

    /**
     * Der gezeichnete Fortschritt. Der Client zaehlt ihn selbst hoch, ein Sechzigstel je Zug,
     * solange der Server einen laufenden Sucher meldet -- so laeuft der Balken fluessig,
     * obwohl der Zaehler nur mit den Zugpaketen ankommt.
     *
     * ABWEICHUNG: das Original begrenzt ihn nicht nach oben. Nach dem Einrasten zaehlt der
     * Server weiter, und der Balken waere ueber seinen Rahmen hinausgewachsen. Hier ist er
     * bei eins gedeckelt.
     */
    private void tickLockonDisplay(ItemStack stack, boolean isSelected) {

        if(!isSelected || getLockonProgress(stack) <= 1) { lockon = 0F; return; }

        lockon = Math.min(lockon + 1F / LOCKON_DAUER, 1F);
    }

    private void resetLockon(ItemStack stack) {
        setLockonProgress(stack, 0);
        setIsLockedOn(stack, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHUD(Pre event, Player player, ItemStack stack) {
        super.renderHUD(event, player, stack);

        if(!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;
        if(aimingProgress < 1F) return;

        RenderScreenOverlay.renderStingerLockon(event.getGuiGraphics(), lockon);
    }

    public static boolean getIsLockingOn(ItemStack stack) { return getValueBool(stack, KEY_LOCKINGON); }
    public static void setIsLockingOn(ItemStack stack, boolean value) { setValueBool(stack, KEY_LOCKINGON, value); }
    public static int getLockonProgress(ItemStack stack) { return getValueInt(stack, KEY_LOCKONPROGRESS); }
    public static void setLockonProgress(ItemStack stack, int value) { setValueInt(stack, KEY_LOCKONPROGRESS, value); }
}
