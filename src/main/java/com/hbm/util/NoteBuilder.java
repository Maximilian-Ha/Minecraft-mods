package com.hbm.util;

import com.hbm.util.Tuple.Triplet;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.util.NoteBuilder.
 *
 * Schreibt eine Tonfolge in eine Zeichenkette und liest sie wieder heraus. Jeder Anschlag steht
 * als "Instrument:Ton:Oktave", die Anschlaege trennt ein Bindestrich.
 *
 * Gebraucht vom RTTY-System: darueber schickt das Original auf dem Kanal "2012-08-06" eine
 * abspielbare Melodie. Die Melodie selbst steht in RTTYSystem.getTestSender und ist im Port noch
 * nicht uebernommen.
 *
 * ABWEICHUNG: das Original gibt beim Lesen ein Array zurueck und faengt jeden Fehler mit einem
 * leeren Array ab. Hier ist es eine Liste -- ein Array generischer Paare laesst sich in Java
 * nicht ohne Warnung anlegen, und der Port faengt Fehler ohnehin lieber je Anschlag ab, statt
 * die ganze Folge wegen eines krummen Anschlags zu verwerfen.
 */
public class NoteBuilder {

    private final StringBuilder beat = new StringBuilder();

    public static NoteBuilder start() {
        return new NoteBuilder();
    }

    public NoteBuilder add(Instrument instrument, Note note, Octave octave) {

        if(this.beat.length() > 0) this.beat.append('-');
        this.beat.append(instrument.ordinal()).append(':').append(note.ordinal()).append(':').append(octave.ordinal());

        return this;
    }

    public String end() {
        return this.beat.toString();
    }

    /** Liest eine Tonfolge. Krumme Anschlaege werden uebersprungen. */
    public static List<Triplet<Instrument, Note, Octave>> translate(String beat) {

        List<Triplet<Instrument, Note, Octave>> notes = new ArrayList<>();
        if(beat == null || beat.isEmpty()) return notes;

        for(String hit : beat.split("-")) {

            String[] parts = hit.split(":");
            if(parts.length != 3) continue;

            try {
                Instrument instrument = Instrument.values()[Integer.parseInt(parts[0])];
                Note note = Note.values()[Integer.parseInt(parts[1])];
                Octave octave = Octave.values()[Integer.parseInt(parts[2])];
                notes.add(new Triplet<>(instrument, note, octave));
            } catch(NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                // Ein krummer Anschlag kostet nur sich selbst, nicht die ganze Folge.
            }
        }

        return notes;
    }

    public enum Instrument {
        PIANO,
        BASSDRUM,
        SNARE,
        CLICKS,
        BASSGUITAR
    }

    /** Die zwoelf Halbtonschritte, beginnend bei Fis -- die Reihenfolge des Notenblocks. */
    public enum Note {
        F_SHARP,
        G,
        G_SHARP,
        A,
        A_SHARP,
        B,
        C,
        C_SHARP,
        D,
        D_SHARP,
        E,
        F
    }

    public enum Octave {
        LOW, MID, HIGH
    }
}
