#!/usr/bin/env bash
# Prueft die Texturverweise der Datenerzeuger: zeigt jedes modLoc(...)-Literal auf etwas,
# das es gibt?
#
# WARUM ES DIESES TOR GIBT
# Runde 279 hat einen CI-Durchgang gekostet (rund fuenfzehn Minuten), weil eine Textur mit
# dem Pfad angegeben wurde, unter dem sie auf der Platte liegt:
#     particleOnlyBlock(SOYUZ_CAPSULE, withDefaultNamespace("textures/models/.../soyuz_lander.png"))
# ModelBuilder.texture haengt "textures/" und ".png" aber SELBST an. Richtig ist
#     modLoc("models/soyuz_capsule/soyuz_lander")
# Der Fehler steht im Quelltext und war in Sekunden zu finden -- kein Tor hat hingeschaut.
# model-resolve-check laeuft erst HINTER runData, und runData war genau daran gescheitert.
#
# WAS DIE REGEL TUT
# Sie sammelt jedes modLoc("X")-Literal aus src/main/java/com/hbm/datagen und fragt der Reihe
# nach:
#   1. liegt assets/hbmsntm/textures/X.png auf der Platte?        -> Textur, in Ordnung
#   2. liegt assets/hbmsntm/models/X.json auf der Platte?         -> Modell von Hand
#   3. baut ein Erzeuger ein Modell dieses Namens?                -> erzeugtes Modell
#      (getBuilder("X"), ein Blockname aus NtmBlocks, oder eine Schicht <blockname>_<ziffer>,
#       wie sie layeringBlock anlegt)
# Bleibt eines uebrig, zeigt es ins Leere.
#
# WAS SIE NICHT SIEHT
# Verweise, die nicht als Literal dastehen -- modLoc("block/" + name) etwa. Die Pfade, die
# dieses Tor prueft, sind die ausgeschriebenen; zusammengesetzte bleiben Sache von
# model-resolve-check in der CI.
#
# Exit-Code 0 = sauber, 1 = Fundstellen.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - "$@" <<'PYEOF'
import re, os, glob, sys

RES = "src/main/resources/assets/hbmsntm"
QUELLEN = sorted(glob.glob("src/main/java/com/hbm/datagen/*.java"))

# --selbstprobe legt einen Verweis ins Leere an und prueft, dass die Regel ihn sieht.
# Ein Tor, dessen Empfindlichkeit niemand misst, ist eine Attrappe.
selbstprobe = "--selbstprobe" in sys.argv

literale = []
for pfad in QUELLEN:
    text = open(pfad, encoding="utf-8").read()
    for m in re.finditer(r'modLoc\(\s*"([^"]+)"\s*\)', text):
        zeile = text.count("\n", 0, m.start()) + 1
        literale.append((pfad, zeile, m.group(1)))

erzeugt = set()
for pfad in QUELLEN:
    text = open(pfad, encoding="utf-8").read()
    erzeugt.update(m.group(1) for m in re.finditer(r'getBuilder\(\s*"([^"]+)"', text))

bloecke = open("src/main/java/com/hbm/blocks/NtmBlocks.java", encoding="utf-8").read()
blocknamen = {m.group(1) for m in re.finditer(r'=\s*\w+\(\s*"([a-z0-9_]+)"', bloecke)}

def aufloesbar(x):
    if os.path.exists(f"{RES}/textures/{x}.png"): return True
    if os.path.exists(f"{RES}/models/{x}.json"): return True
    if x in erzeugt: return True
    kurz = x.split("/", 1)[1] if "/" in x else x
    if kurz in erzeugt or kurz in blocknamen: return True
    # layeringBlock legt <blockname>_1 bis _8 an.
    stamm = re.sub(r'_\d+$', '', kurz)
    return stamm != kurz and stamm in blocknamen

if selbstprobe:
    probe = "models/gibt_es_nicht/auch_nicht"
    if aufloesbar(probe):
        print("SELBSTPROBE FEHLGESCHLAGEN: ein erfundener Pfad gilt als aufloesbar.")
        sys.exit(1)
    print("Selbstprobe bestanden: ein erfundener Pfad wird als Fund erkannt.")
    sys.exit(0)

funde = [(p, z, x) for p, z, x in literale if not aufloesbar(x)]

print(f"Pruefe Texturverweise ... {len(literale)} modLoc-Literale in {len(QUELLEN)} Erzeugern")
print(f"  zeigen ins Leere : {len(funde)}")

if funde:
    print()
    print("VERWEISE INS LEERE -- weder Textur noch Modell. Beachte: modLoc nimmt den Pfad OHNE")
    print("\"textures/\" und OHNE \".png\"; ModelBuilder.texture haengt beides selbst an.")
    for p, z, x in funde:
        print(f"   {p}:{z}: {x}")
    sys.exit(1)

sys.exit(0)
PYEOF
