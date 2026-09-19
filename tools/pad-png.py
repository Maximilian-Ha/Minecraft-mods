#!/usr/bin/env python3
"""Polstert ein PNG nach unten auf eine gewuenschte Hoehe -- ohne Fremdbibliothek.

WOFUER: Techne-Modelle aus 1.7.10 tragen haeufig eine 64x32 grosse Haut. Der Blockatlas von
1.21 nimmt nur Texturen an, deren Hoehe ein Vielfaches der Breite ist; eine 64x32 wird beim
Zusammenbau des Atlas abgelehnt, und das Modell bleibt unsichtbar. Das Bild muss also auf
64x64 wachsen, ohne dass sich an den vorhandenen Bildpunkten etwas aendert -- die
UV-Koordinaten des Modells rechnen weiterhin mit der alten Aufteilung.

Der neue Bereich ist vollstaendig durchsichtig.

Aufruf:
    tools/pad-png.py <eingabe.png> <ausgabe.png> [hoehe]
Ohne Hoehe wird auf ein Quadrat gepolstert.
"""

import struct
import sys
import zlib


def lies(pfad):
    daten = open(pfad, 'rb').read()
    if daten[:8] != b'\x89PNG\r\n\x1a\n':
        raise SystemExit('Kein PNG: ' + pfad)

    bloecke = []
    i = 8
    while i < len(daten):
        laenge = struct.unpack('>I', daten[i:i + 4])[0]
        art = daten[i + 4:i + 8]
        inhalt = daten[i + 8:i + 8 + laenge]
        bloecke.append((art, inhalt))
        i += 12 + laenge
    return bloecke


def main():
    if len(sys.argv) < 3:
        raise SystemExit(__doc__)

    quelle, ziel = sys.argv[1], sys.argv[2]
    bloecke = lies(quelle)

    kopf = dict(bloecke)[b'IHDR']
    breite, hoehe, tiefe, farbart, kompression, filter_, verschraenkt = struct.unpack('>IIBBBBB', kopf)

    neue_hoehe = int(sys.argv[3]) if len(sys.argv) > 3 else breite

    if farbart != 6 or tiefe != 8 or verschraenkt != 0:
        raise SystemExit('Nur 8-Bit-RGBA ohne Verschraenkung -- hier: Farbart %d, Tiefe %d, verschraenkt %d'
                         % (farbart, tiefe, verschraenkt))
    if neue_hoehe < hoehe:
        raise SystemExit('Die Zielhoehe ist kleiner als das Bild')

    roh = zlib.decompress(b''.join(inhalt for art, inhalt in bloecke if art == b'IDAT'))

    # Jede Zeile traegt vorn ein Filterbyte. Die vorhandenen Zeilen bleiben unveraendert,
    # die neuen sind durchsichtig und ungefiltert.
    zeilenlaenge = breite * 4 + 1
    leer = b'\x00' + b'\x00' * (breite * 4)
    neu = roh[:hoehe * zeilenlaenge] + leer * (neue_hoehe - hoehe)

    kopf_neu = struct.pack('>IIBBBBB', breite, neue_hoehe, tiefe, farbart, kompression, filter_, verschraenkt)

    ausgabe = bytearray(b'\x89PNG\r\n\x1a\n')

    def schreibe(art, inhalt):
        ausgabe.extend(struct.pack('>I', len(inhalt)))
        ausgabe.extend(art)
        ausgabe.extend(inhalt)
        ausgabe.extend(struct.pack('>I', zlib.crc32(art + inhalt) & 0xffffffff))

    schreibe(b'IHDR', kopf_neu)
    schreibe(b'IDAT', zlib.compress(neu, 9))
    schreibe(b'IEND', b'')

    open(ziel, 'wb').write(bytes(ausgabe))
    print('%s: %dx%d -> %dx%d' % (ziel, breite, hoehe, breite, neue_hoehe))


main()
