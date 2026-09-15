package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.IMetaItem;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemCassette.
 *
 * Eine Tonspur zum Einlegen in die Sirene. Jede Kassette traegt eine Farbe, eine Reichweite und
 * die Angabe, ob ihr Ton in der Schleife laeuft oder einmal durchspielt.
 *
 * DIE REIHENFOLGE DER AUFZAEHLUNG IST DIE DES ORIGINALS und darf nicht umgestellt werden: die
 * Stelle darin ist der Metawert im Gegenstandsstapel und steht in jedem gespeicherten Stand.
 *
 * DIE FARBE KOMMT ZUR LAUFZEIT: das Modell hat zwei Lagen -- unten das Gehaeuse, oben der
 * Aufkleber, und der wird vom Farbgeber eingefaerbt (siehe NuclearTechModClient). Das Original
 * legt dafuer zwei Renderdurchgaenge uebereinander.
 */
public class CassetteItem extends Item implements IMetaItem {

    public CassetteItem(Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        /* Die leere Kassette bleibt draussen -- sie ist der Platzhalter fuer "nichts eingelegt". */
        for(TrackType type : TrackType.values()) {
            if(type != TrackType.NULL) stacks.add(MetaHelper.newStack(item, 1, type.ordinal()));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(TrackType.fromMeta(MetaHelper.getMeta(stack)).title);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        TrackType type = TrackType.fromMeta(MetaHelper.getMeta(stack));

        components.add(Component.translatable("cassette.range", type.volume).withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable(type.type == SoundType.LOOP ? "cassette.loop" : "cassette.once").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Ob der Ton in der Schleife laeuft oder einmal durchspielt. */
    public enum SoundType {
        LOOP, PASS, SOUND
    }

    public enum TrackType {

        NULL(              " ",                         null,                             SoundType.SOUND, 0x000000,   0),
        HATCH(             "Hatch Siren",               NtmSoundEvents.ALARM_HATCH,       SoundType.LOOP,  0x333D77,  250),
        AUTOPILOT(         "Autopilot Disconnected",    NtmSoundEvents.ALARM_AUTOPILOT,   SoundType.LOOP,  0xB5AE35,   50),
        AMS_SIREN(         "AMS Siren",                 NtmSoundEvents.ALARM_AMS_SIREN,   SoundType.LOOP,  0xE5C112,   50),
        BLAST_DOOR(        "Blast Door Alarm",          NtmSoundEvents.ALARM_BLAST_DOOR,  SoundType.LOOP,  0xB20000,   50),
        APC_LOOP(          "APC Siren",                 NtmSoundEvents.ALARM_APC_LOOP,    SoundType.LOOP,  0x366BE0,   50),
        KLAXON(            "Klaxon",                    NtmSoundEvents.ALARM_KLAXON,      SoundType.LOOP,  0x808080,   50),
        KLAXON_A(          "Vault Door Alarm",          NtmSoundEvents.ALARM_FO_KLAXON_A, SoundType.LOOP,  0x8C810B,   50),
        KLAXON_B(          "Security Alert",            NtmSoundEvents.ALARM_FO_KLAXON_B, SoundType.LOOP,  0x76818E,   50),
        SIREN(             "Standard Siren",            NtmSoundEvents.ALARM_REGULAR_SIREN, SoundType.LOOP, 0x660000, 100),
        CLASSIC(           "Classic Siren",             NtmSoundEvents.ALARM_CLASSIC,     SoundType.LOOP,  0xC0CFE8,  100),
        BANK_ALARM(        "Bank Alarm",                NtmSoundEvents.ALARM_BANK,        SoundType.LOOP,  0x3682E2,  100),
        BEEP_SIREN(        "Beep Siren",                NtmSoundEvents.ALARM_BEEP_SIREN,  SoundType.LOOP,  0xD3D3D3,  100),
        CONTAINER_ALARM(   "Container Alarm",           NtmSoundEvents.ALARM_CONTAINER,   SoundType.LOOP,  0xE0BB9F,  100),
        SWEEP_SIREN(       "Sweep Siren",               NtmSoundEvents.ALARM_SWEEP_SIREN, SoundType.LOOP,  0xEDF1DA,  500),
        STRIDER_SIREN(     "Missile Silo Siren",        NtmSoundEvents.ALARM_STRIDER_SIREN, SoundType.LOOP, 0xABAE5A, 500),
        AIR_RAID(          "Air Raid Siren",            NtmSoundEvents.ALARM_AIR_RAID,    SoundType.LOOP,  0xDF3795,  500),
        NOSTROMO_SIREN(    "Nostromo Self Destruct",    NtmSoundEvents.ALARM_NOSTROMO,    SoundType.LOOP,  0x5DD800,  100),
        EAS_ALARM(         "EAS Alarm Screech",         NtmSoundEvents.ALARM_EAS,         SoundType.LOOP,  0xB3A8C1,   50),
        APC_PASS(          "APC Pass",                  NtmSoundEvents.ALARM_APC_PASS,    SoundType.PASS,  0x343E53,   50),
        RAZORTRAIN(        "Razortrain Horn",           NtmSoundEvents.ALARM_RAZORTRAIN,  SoundType.SOUND, 0x775C6D,  250);

        public final String title;
        private final Supplier<SoundEvent> sound;
        public final SoundType type;
        public final int color;
        public final int volume;

        TrackType(String title, DeferredHolder<SoundEvent, SoundEvent> sound, SoundType type, int color, int volume) {
            this.title = title;
            this.sound = sound;
            this.type = type;
            this.color = color;
            this.volume = volume;
        }

        public SoundEvent getSound() {
            return this.sound == null ? null : this.sound.get();
        }

        /**
         * ABWEICHUNG VOM ORIGINAL: dort greift getEnum bei einem unbekannten Metawert daneben.
         * Hier faellt alles ausserhalb der Aufzaehlung auf NULL zurueck -- eine Kassette aus
         * einem aelteren Stand macht die Sirene dann still statt den Server umzuwerfen.
         */
        public static TrackType fromMeta(int meta) {
            TrackType[] werte = values();
            return meta >= 0 && meta < werte.length ? werte[meta] : NULL;
        }
    }
}
