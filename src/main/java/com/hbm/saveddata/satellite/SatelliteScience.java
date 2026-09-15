package com.hbm.saveddata.satellite;

import com.hbm.items.machine.DriveItem.DriveType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.saveddata.satellites.SatelliteScience.
 *
 * Das Weltraumlabor. Es misst, und alle fuenfzehn Minuten liegt eine Messreihe zum Abholen
 * bereit: wer dann eine leere Platte ins Bandlaufwerk legt, bekommt sie beschrieben zurueck.
 *
 * ES IST DER EINZIGE DATENLIEFERANT. Alle anderen Satelliten koennen zwar Daten fuehren -- das
 * Feld dafuer steht in SatelliteBase --, aber keiner erzeugt welche. Ohne dieses Labor waere
 * das Bandlaufwerk ein Geraet ohne Zweck.
 *
 * TEILWEISE PORTIERT, und das ist Absicht. Das Original kann zusaetzlich zweierlei:
 *
 *   - MESSFUEHLER aufnehmen, die ueber hundert Spielstunden hinweg eine zweite Datenart
 *     erarbeiten (DISK_ORBITDATA), und
 *   - als WELTRAUMFABRIK arbeiten, die Bauplaene abarbeitet und die Erzeugnisse zur Abholung
 *     bereitlegt.
 *
 * Beides haengt an Teilen, die der Port nicht hat: den Satellitenbauteilen SCIENCE_SENSOR und
 * SCIENCE_ASSEMBLER, den Rezepten der Weltraumfabrik und den Abholfaechern samt Abwurfkapsel.
 * Wer sie nachreicht, findet hier die Stelle vor; bis dahin waere jede Zeile davon toter Code.
 */
public class SatelliteScience extends SatelliteBase {

    /** Fuenfzehn Minuten zwischen zwei Messreihen. */
    public static final int COOLDOWN = 15 * 60 * 20;

    public long lastScience;

    @Override public String getType() { return "SCIENCE_PROBE"; }

    /**
     * ABWEICHUNG VON EINER REINEN ABFRAGE, wie im Original: ist die Wartezeit abgelaufen, legt
     * der Satellit hier die naechste Messreihe an. Der Aufruf kommt vom Bandlaufwerk, also
     * genau dann, wenn jemand danach fragt -- der Satellit braucht dafuer keine eigene Uhr.
     */
    @Override
    public boolean hasData(Level level) {

        if(super.hasData(level)) return true;

        if(level.getGameTime() > this.lastScience + COOLDOWN) {
            this.produceData(DriveType.DISK_EMPTY, DriveType.DISK_FLIGHTDATA);
            this.lastScience = level.getGameTime();
        }

        return super.hasData(level);
    }

    @Override
    public List<Component> getInfo(Level level) {

        List<Component> info = super.getInfo(level);

        long ready = this.lastScience + COOLDOWN;

        if(ready <= level.getGameTime()) {
            info.add(Component.translatable("satellite.ready"));
        } else {
            int seconds = (int) ((ready - level.getGameTime()) / 20);
            info.add(Component.translatable("satellite.cooldown", (seconds / 60) + "m" + (seconds % 60) + "s"));
        }

        return info;
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        tag.putLong("lastScience", this.lastScience);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        this.lastScience = tag.getLong("lastScience");
    }
}
