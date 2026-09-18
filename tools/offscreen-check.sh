#!/usr/bin/env bash
#
# offscreen-check.sh -- prueft, ob jeder Darsteller, dessen Vorlage in 1.7.10
# INFINITE_EXTENT_AABB nimmt, in 1.21 shouldRenderOffScreen setzt.
#
# WARUM ES DIESES TOR GIBT: genau hier ist die Leviathan-Turbine zweimal verschwunden.
# Runde 153 hat einen grossen getRenderBoundingBox eingebaut und damit die falsche Ebene
# repariert -- Minecraft sammelt die Blockentitaeten aus den SICHTBAREN Chunk-Abschnitten
# ein, und der Sichtkasten danach kann nur zusaetzlich wegschneiden, nie hinzufuegen.
# Runde 160 hat es mit shouldRenderOffScreen richtig gemacht. Dieses Tor haelt das fest.
#
# Die Vergleichsliste steht in tools/offscreen-list.txt, weil die CI den Fernzweig
# hbm-upstream/master nicht hat; im Kopf der Liste steht, wie sie erzeugt wurde.
#
# NACHGEMESSEN (Runde 162): 44 verschiedene Darsteller in der Liste, 17 davon gibt es im
# Port, alle 17 setzen shouldRenderOffScreen -- 0 Befunde. Nimmt man die Methode aus
# RenderChungus heraus, meldet das Tor genau RenderChungus und endet mit 1 (Exit-Code direkt
# geprueft, nicht durch eine Pipe).

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

def ohne_kommentare(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

erwartet = []
for zeile in open('tools/offscreen-list.txt', encoding='utf-8'):
    zeile = zeile.strip()
    if not zeile or zeile.startswith('#'):
        continue
    erwartet.append(zeile.split()[0])
erwartet = sorted(set(erwartet))

ordner = 'src/main/java/com/hbm/render/blockentity'
vorhanden = {f[:-5] for f in os.listdir(ordner) if f.endswith('.java')}

geprueft = [n for n in erwartet if n in vorhanden]
fehlend = []
for n in geprueft:
    quelle = ohne_kommentare(open(os.path.join(ordner, n + '.java'), encoding='utf-8').read())
    if 'shouldRenderOffScreen' not in quelle:
        fehlend.append(n)

print('Pruefe Dauerzeichnung ... %d Darsteller in der Liste, %d davon im Port'
      % (len(erwartet), len(geprueft)))
print('  ohne shouldRenderOffScreen : %d' % len(fehlend))

if not fehlend:
    print('OK - jeder Darsteller mit INFINITE_EXTENT_AABB im Original wird dauernd gezeichnet.')
    sys.exit(0)

print()
print('DIESE DARSTELLER VERSCHWINDEN, WENN MAN ZUR SEITE SCHAUT:')
for n in fehlend:
    print('   %s' % n)
print()
print('Das Original nimmt fuer ihre Vorlage INFINITE_EXTENT_AABB. In 1.21 heisst das:')
print('    @Override')
print('    public boolean shouldRenderOffScreen(XBlockEntity be) { return true; }')
print('Ein grosser getRenderBoundingBox reicht NICHT -- er kann nur zusaetzlich wegschneiden.')
sys.exit(1)
PYEOF
