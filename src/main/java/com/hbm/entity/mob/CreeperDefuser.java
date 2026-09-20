package com.hbm.entity.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;

/**
 * Portiert aus 1.7.10: die statische Methode ItemModDefuser.castrateCreeper.
 *
 * Das Entschaerfen eines Creepers. Wer es tut, nimmt ihm die Zuendschnur heraus -- sie faellt
 * als safety_fuse zu Boden -- und der Creeper laeuft von da an nur noch herum.
 *
 * WARUM DAS IM PORT ANDERS FUNKTIONIERT ALS IM ORIGINAL. Das Original entfernt dem Creeper
 * sein EntityAICreeperSwell aus der Aufgabenliste; damit setzt niemand mehr den Zaehler
 * hoch. Zwei Dinge stehen dem auf 1.21 im Weg:
 *
 *   1. Mob.goalSelector ist geschuetzt, und ein SwellGoal aus der Liste zu suchen und zu
 *      streichen ginge nur ueber einen weiteren Zugriffstransformer.
 *   2. Aufgaben ueberleben das Speichern der Welt nicht. Das Original weiss das und setzt
 *      deshalb die Marke hfr_defused, um das Entfernen nach jedem Laden zu wiederholen --
 *      in ModEventHandler.onLivingUpdate, bei jedem Takt jedes markierten Creepers.
 *
 * Der Port behaelt die Marke und macht sie zum eigentlichen Mechanismus: CommonEvents haelt
 * markierte Creeper in jedem Takt nieder, indem es setSwellDir(-1) setzt, BEVOR Creeper.tick()
 * laeuft. Das Ergebnis ist dasselbe -- der Zaehler kommt nie ueber null -- und es braucht
 * keinen Griff in die Aufgabenliste. Die Marke steht in den bestaendigen Daten der Entitaet
 * und ueberlebt damit auch das Speichern.
 *
 * DER ZUGRIFFSTRANSFORMER, DEN ES TROTZDEM BRAUCHT: Creeper.DATA_IS_IGNITED. Ein Creeper,
 * den jemand mit Feuerzeug und Stein angezuendet hat, setzt in tick() selbst wieder auf +1,
 * und dagegen hilft kein Niederhalten davor. Es gibt oeffentlich nur ignite(), kein
 * Gegenstueck. Ein entschaerfter Creeper muss dieses Merkmal loswerden, sonst brennt er
 * weiter.
 */
public class CreeperDefuser {

    /** Die Marke in den bestaendigen Daten. Der Name ist der des Originals. */
    public static final String MARKE = "hfr_defused";

    /**
     * Entschaerft einen Creeper. Gibt zurueck, ob dabei etwas geschehen ist -- also ob der
     * Creeper vorher noch scharf war.
     *
     * @param urheber wer entschaerft; darf null sein (Sockel, Wiederherstellung nach dem
     *                Laden). Bestimmt nur, woher der eine Schadenspunkt kommt.
     * @param ausgeben ob Zuendschnur, Ton, Schadenspunkt und Schwaeche kommen. Beim
     *                 Wiederherstellen nach dem Laden ist das false, sonst gaebe es bei
     *                 jedem Weltstart eine neue Zuendschnur.
     */
    public static boolean entschaerfe(Creeper creeper, LivingEntity urheber, boolean ausgeben) {

        Level level = creeper.level();
        if(level.isClientSide) return false;

        boolean warScharf = !istEntschaerft(creeper);

        creeper.setSwellDir(-1);
        creeper.entityData.set(Creeper.DATA_IS_IGNITED, false);
        creeper.getPersistentData().putBoolean(MARKE, true);

        if(warScharf && ausgeben) {

            BlockPos pos = creeper.blockPosition();
            level.playSound(null, pos, NtmSoundEvents.PIN_BREAK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

            creeper.spawnAtLocation(new ItemStack(NtmItems.SAFETY_FUSE.get()));

            /* Der eine Schadenspunkt des Originals: er macht sichtbar, dass etwas geschehen
             * ist, und setzt den Creeper in Kampf mit dem Entschaerfer. */
            creeper.hurt(urheber != null
                    ? creeper.damageSources().mobAttack(urheber)
                    : creeper.damageSources().magic(), 1.0F);

            /* Zehn Sekunden Schwaeche. Im Original stehen Staerke und Dauer vertauscht
             * (new PotionEffect(id, 0, 200) -- null Takte, Stufe 201); das ist dort ein
             * Fehler, der den Effekt sofort wieder verfallen laesst. Der Port dreht es um. */
            creeper.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }

        return warScharf;
    }

    /** Traegt dieser Creeper die Marke? */
    public static boolean istEntschaerft(Creeper creeper) {
        return creeper.getPersistentData().getBoolean(MARKE);
    }

    /**
     * Der Takt: haelt einen markierten Creeper nieder. Gehoert vor Creeper.tick(), also in
     * EntityTickEvent.Pre -- danach waere der Zaehler schon gestiegen.
     */
    public static void haltNieder(Creeper creeper) {

        if(creeper.level().isClientSide) return;
        if(!istEntschaerft(creeper)) return;

        creeper.setSwellDir(-1);

        if(creeper.isIgnited()) creeper.entityData.set(Creeper.DATA_IS_IGNITED, false);
    }
}
