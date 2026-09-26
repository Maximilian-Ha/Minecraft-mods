#!/usr/bin/env bash
#
# Prueft, dass keine Textur im Block-Atlas die Mipmap-Stufe des GANZEN Atlas herabsetzt.
#
# Hintergrund: Minecraft naeht alles unter textures/block/ und textures/item/ in einen
# gemeinsamen Atlas. Die Mipmap-Stufe gilt fuer den Atlas als Ganzes und richtet sich nach
# der schlechtesten Textur: min(niedrigstes gesetztes Bit von Breite, Hoehe). Standard ist
# Stufe 4, dafuer muessen beide Seiten ein Vielfaches von 16 sein.
#
# Gefunden im Protokoll eines echten Spielstarts (Runde 306):
#   Texture hbmsntm:block/geiger with size 55x55 limits mip level from 4 to 0
# Eine einzige ungerade Modelltextur schaltete damit die Mipmaps fuer JEDEN Block im Spiel
# ab -- entfernte Bloecke flimmern. Kein Absturz, nur eine Warnzeile; kein anderes Tor
# und kein Server-Test sieht das.
#
# Abhilfe ohne Qualitaetsverlust: ganzzahlig vergroessern (jedes Pixel wird ein k*k-Block).
# Die Modelle rechnen ihre UV-Koordinaten relativ zum Sprite, das Bild bleibt identisch.
#
# NACHGEMESSEN (Runde 306): vorher 7 Funde (geiger 55x55, deco_computer 66x66,
# deco_pole_top 20x20, rtg 84x84, deco_tape_recorder 56x56, nuke_little_boy 184x112,
# ingot_nikonium 200x200), nach dem Vergroessern 0 bei 2879 Texturen. Stellt man geiger.png
# aus dem Stand davor wieder her, meldet das Tor genau diese Datei.
#
set -u

cd "$(dirname "$0")/.."

python3 - <<'PY'
import struct, pathlib, sys

WURZEL = pathlib.Path('src/main/resources/assets/hbmsntm/textures')
funde = []
geprueft = 0
for ordner in ('block', 'item'):
    for p in sorted((WURZEL / ordner).rglob('*.png')):
        kopf = p.read_bytes()[:24]
        if kopf[:8] != b'\x89PNG\r\n\x1a\n':
            funde.append(f'{p}: keine PNG-Datei')
            continue
        geprueft += 1
        w, h = struct.unpack('>II', kopf[16:24])
        bit = min(w & -w, h & -h)
        if bit < 16:
            stufe = bit.bit_length() - 1
            funde.append(f'{p}: {w}x{h} senkt die Mipmap-Stufe des Block-Atlas auf {stufe} '
                         f'-- ganzzahlig um {16 // bit} vergroessern')

for f in funde:
    print(f)
print(f'{geprueft} Texturen im Block-Atlas geprueft, {len(funde)} Funde')
sys.exit(1 if funde else 0)
PY
