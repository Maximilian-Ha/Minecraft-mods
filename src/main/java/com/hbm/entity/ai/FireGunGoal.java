package com.hbm.entity.ai;

import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIFireGun.
 *
 * WOZU: ein Skelett, dem man eine Schusswaffe des Mods in die Hand legt, weiss von sich aus
 * nichts damit anzufangen -- die Waffen werden ueber Tastendruecke bedient, nicht ueber den
 * Bogen-Angriff der Vanilla-KI. Dieses Ziel drueckt die Tasten stellvertretend.
 *
 * DER ABLAUF ist eine kleine Zustandsmaschine mit vier Zustaenden. Aus RUHE geht es je nach
 * Magazinstand nach NACHLADEN oder FEUERN; beide laufen nach ihrer Zeit in WARTEN, und WARTEN
 * faellt nach seiner Zeit zurueck nach RUHE.
 *
 * DAS DURCHFALLEN IM SCHALTER IST ABSICHT UND STEHT SO IM ORIGINAL
 * (EntityAIFireGun.java:98-110): in zustand() hat kein Zweig ein break, jeder endet also in
 * tastenLoesen(). Druck und Loesen derselben Taste landen damit in EINEM Tick. Wirkungslos ist
 * das nicht -- das Press-Lambda einer Waffe feuert unmittelbar (Lego.LAMBDA_STANDARD_FIRE ruft
 * doStandardFire direkt auf) --, es haelt die Taste nur nicht. Dauerfeuer kommt deshalb nicht
 * aus dem gehaltenen Druck, sondern daraus, dass tick() waehrend FEUERN jeden Tick erneut
 * drueckt. Ein nachgetragenes break wuerde das Verhalten aendern; deshalb steht hier dasselbe.
 */
public class FireGunGoal extends Goal {

    private final Mob wirt;

    public double angriffsTempo = 1.0D;  // wie schnell wir uns in diesem Zustand bewegen
    public double maximalWeite = 20;     // ab welcher Entfernung wir das Feuer einstellen
    public int feuerdauer = 10;          // hoechste Zahl Ticks je Feuerstoss
    public int mindestPause = 10;        // kleinste Zahl Ticks zwischen zwei Stoessen
    public int hoechstPause = 40;        // groesste Zahl Ticks zwischen zwei Stoessen
    public float streuung = 30;          // wie viel Grad die KI danebenhaelt
    public boolean zufallsStoss = true;  // ob die Stossdauer fest ist oder gewuerfelt wird

    private int angriffsZaehler = 0;
    private Zustand zustand = Zustand.RUHE;
    private int zustandsZeit = 0;

    private enum Zustand {
        RUHE,
        WARTEN,
        FEUERN,
        NACHLADEN,
    }

    public FireGunGoal(Mob wirt) {
        this.wirt = wirt;
    }

    /* Das Original setzt keine Mutex-Bits, das Ziel vertraegt sich also mit jedem anderen. */
    @Override
    public boolean canUse() {
        return wirt.getTarget() != null && waffe() != null;
    }

    @Override
    public void tick() {

        LivingEntity ziel = wirt.getTarget();
        ItemStack stapel = wirt.getMainHandItem();
        GunBaseNTItem waffe = waffe();
        if(ziel == null || waffe == null) return;

        /* Kreaturen ticken ihre Ausruestung nicht von selbst -- ohne diesen Aufruf laufen die
         * Zustandszeiten der Waffe nie ab. */
        waffe.inventoryTick(stapel, wirt.level(), wirt, 0, true);

        double abstandQuadrat = wirt.distanceToSqr(ziel.getX(), ziel.getY(), ziel.getZ());
        boolean sichtbar = wirt.getSensing().hasLineOfSight(ziel);

        if(sichtbar) {
            angriffsZaehler++;
        } else {
            angriffsZaehler = 0;
        }

        if(abstandQuadrat < maximalWeite * maximalWeite && angriffsZaehler > 20) {
            wirt.getNavigation().stop();
        } else {
            wirt.getNavigation().moveTo(ziel, angriffsTempo);
        }

        wirt.getLookControl().setLookAt(ziel, 30.0F, 30.0F);

        zustandsZeit--;
        if(zustandsZeit < 0) {
            zustandsZeit = 0;

            if(zustand == Zustand.WARTEN) {
                zustand(Zustand.RUHE, 0, waffe, stapel);
            } else if(zustand != Zustand.RUHE) {
                zustand(Zustand.WARTEN, wirt.getRandom().nextInt(hoechstPause - mindestPause) + mindestPause, waffe, stapel);
            }
        } else if(zustand == Zustand.FEUERN) {
            // Waehrend des Stosses jeden Tick erneut druecken
            taste(waffe, stapel, EnumKeybind.GUN_PRIMARY);
        }

        if(sichtbar && abstandQuadrat < maximalWeite * maximalWeite) {
            if(zustand == Zustand.RUHE) {
                GunConfig aufbau = waffe.getConfig(stapel, 0);
                Receiver empfaenger = aufbau.getReceivers(stapel)[0];
                if(empfaenger.getMagazine(stapel).getAmount(stapel, null) <= 0) {
                    zustand(Zustand.NACHLADEN, 20, waffe, stapel);
                } else if(GunBaseNTItem.getState(stapel, 0) == GunState.IDLE) {
                    int zeit = zufallsStoss ? wirt.getRandom().nextInt(feuerdauer) : feuerdauer;
                    zustand(Zustand.FEUERN, zeit, waffe, stapel);
                }
            }
        }
    }

    private void zustand(Zustand nach, int zeit, GunBaseNTItem waffe, ItemStack stapel) {
        zustand = nach;
        zustandsZeit = zeit;

        /* Ohne break -- siehe Klassenkommentar, das ist der Ablauf des Originals. */
        switch(zustand) {
        case FEUERN: taste(waffe, stapel, EnumKeybind.GUN_PRIMARY);
        case NACHLADEN: taste(waffe, stapel, EnumKeybind.RELOAD);
        default: tastenLoesen(waffe, stapel); break;
        }
    }

    private void tastenLoesen(GunBaseNTItem waffe, ItemStack stapel) {
        taste(waffe, stapel, null);
    }

    private void taste(GunBaseNTItem waffe, ItemStack stapel, EnumKeybind welche) {

        /* Den Rumpf in Schussrichtung drehen -- die Waffe haengt am Rumpf, nicht am Kopf --
         * und unmittelbar vor dem Schuss danebenhalten. */
        if(welche != null && welche != EnumKeybind.RELOAD) {
            float daneben = streuung * (waffe.getConfig(stapel, 0).getReceivers(stapel)[0].getHipfireSpread(stapel) * 20);
            wirt.yHeadRot += (wirt.getRandom().nextFloat() - 0.5F) * daneben;
            wirt.setXRot(wirt.getXRot() + (wirt.getRandom().nextFloat() - 0.5F) * daneben);
            wirt.setYRot(wirt.yHeadRot);
        }

        waffe.handleKeybind(wirt, null, stapel, EnumKeybind.GUN_PRIMARY, welche == EnumKeybind.GUN_PRIMARY);
        waffe.handleKeybind(wirt, null, stapel, EnumKeybind.GUN_SECONDARY, welche == EnumKeybind.GUN_SECONDARY);
        waffe.handleKeybind(wirt, null, stapel, EnumKeybind.GUN_TERTIARY, welche == EnumKeybind.GUN_TERTIARY);
        waffe.handleKeybind(wirt, null, stapel, EnumKeybind.RELOAD, welche == EnumKeybind.RELOAD);
    }

    /** Die Waffe in der Hand des Wirts, oder null, wenn dort keine steckt. */
    public GunBaseNTItem waffe() {
        ItemStack stapel = wirt.getMainHandItem();
        if(stapel.isEmpty() || !(stapel.getItem() instanceof GunBaseNTItem gun)) return null;
        return gun;
    }
}
