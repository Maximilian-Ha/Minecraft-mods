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
# ZWEITE REGEL (Runde 309): ein Sichtkasten, der mehr als 16 Bloecke hoch reicht, braucht
# shouldRenderOffScreen ebenfalls. Gemessen: genau zwei solche Darsteller, RenderRBMKControlRod
# und RenderRBMKFuelChannel (je y + 17), beide gesetzt. Nimmt man die Methode aus
# RenderRBMKFuelChannel heraus, meldet das Tor genau diese Datei.
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

# Zweite Regel (Runde 309): ein Sichtkasten, der mehr als 16 Bloecke ueber die Blockentitaet
# hinaufreicht, liegt IMMER zum Teil in einem anderen Chunk-Abschnitt. Ist der Abschnitt der
# Blockentitaet verdeckt, faellt der ganze Renderer heraus -- beim RBMK sitzt sie ganz unten im
# undurchsichtigen Reaktor, der Kopf verschwand je nach Blickwinkel. Die Hoehe wird aus
# "y + N" im Rumpf von getRenderBoundingBox gelesen.
hoch = []
for f in sorted(vorhanden):
    quelle = ohne_kommentare(open(os.path.join(ordner, f + '.java'), encoding='utf-8').read())
    m = re.search(r'getRenderBoundingBox\([^)]*\)\s*\{(.*?)\n    \}', quelle, re.S)
    if not m:
        continue
    hoehen = [int(x) for x in re.findall(r'\by \+ (\d+)', m.group(1))]
    if hoehen and max(hoehen) > 16 and 'shouldRenderOffScreen' not in quelle:
        hoch.append('%s (Sichtkasten bis y + %d)' % (f, max(hoehen)))
print('  mehr als 16 Bloecke hoch ohne shouldRenderOffScreen : %d' % len(hoch))
fehlend += hoch

if not fehlend:
    print('OK - jeder Darsteller mit INFINITE_EXTENT_AABB im Original oder mit einem Sichtkasten')
    print('     ueber mehr als 16 Bloecke wird dauernd gezeichnet.')
    sys.exit(0)

print()
print('DIESE DARSTELLER VERSCHWINDEN, WENN MAN ZUR SEITE SCHAUT:')
for n in fehlend:
    print('   %s' % n)
print()
print('Das Original nimmt fuer ihre Vorlage INFINITE_EXTENT_AABB, oder ihr Sichtkasten reicht')
print('ueber mehr als einen Chunk-Abschnitt hinaus. In 1.21 heisst das:')
print('    @Override')
print('    public boolean shouldRenderOffScreen(XBlockEntity be) { return true; }')
print('Ein grosser getRenderBoundingBox reicht NICHT -- er kann nur zusaetzlich wegschneiden.')
sys.exit(1)
PYEOF
