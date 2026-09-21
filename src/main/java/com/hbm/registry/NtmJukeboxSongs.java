package com.hbm.registry;

import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

/**
 * Die Schallplatten des Mods -- eine einzige.
 *
 * In 1.7.10 war eine Platte ein Gegenstand, der seinen Klangnamen selbst kannte
 * (ItemModRecord("glass")). Auf 1.21 ist sie ein Datenpackeintrag: Klang, Anzeigename,
 * Spieldauer und die Zahl, die ein Komparator neben dem Plattenspieler ausgibt. Der
 * Gegenstand zeigt nur noch auf diesen Eintrag.
 */
public class NtmJukeboxSongs {

    public static final ResourceKey<JukeboxSong> GLASS = key("glass");

    public static void bootstrap(BootstrapContext<JukeboxSong> context) {
        /*
         * 62,23 Sekunden, nachgemessen an recordglass.ogg (2986977 Abtastwerte bei 48 kHz).
         * Die Komparatorzahl ist frei waehlbar; Vanilla vergibt sie in der Reihenfolge, in
         * der die Platten dazukamen, und 15 ist die einzige, die keine Vanilla-Platte hat.
         */
        context.register(GLASS, new JukeboxSong(
                NtmSoundEvents.MUSIC_DISC_GLASS,
                Component.translatable("jukebox_song.hbmsntm.glass"),
                62.23F,
                15));
    }

    private static ResourceKey<JukeboxSong> key(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, NuclearTechMod.withDefaultNamespace(name));
    }
}
