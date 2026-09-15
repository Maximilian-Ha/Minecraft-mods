#!/usr/bin/env bash
#
# state-check.sh -- prueft, dass jeder Block GENAU EINE Zustandsbeschreibung bekommt.
#
# WARUM ES DIESES TOR GIBT: der Block-Erzeuger fuehrt je Block einen VariantBlockStateBuilder.
# Wer denselben Block zweimal beschreibt -- etwa erst simpleBlockWithItem und dann noch einmal
# getVariantBuilder --, bringt runData zum Absturz:
#
#   IllegalArgumentException: Cannot set models for a state for which a partial match has
#   already been configured
#
# Gemessen an der PWR-Steuerung, die genau so zweimal beschrieben war. Der Fehler kostet einen
# vollen CI-Lauf, denn er zeigt sich erst, wenn die Datengenerierung an dieser Stelle ankommt.
#
# Die Gegenrichtung ist genauso falsch, nur leiser: ein Block ganz ohne Zustandsbeschreibung
# hat im Spiel kein Modell und wird schwarz-violett gezeichnet. So sind die vier
# Fluessigkeitsbloecke aufgefallen (corium, mud, rad_lava, volcanic_lava).
#
# Eine Beschreibung kann aus drei Quellen kommen:
#   1. ein zustandsdefinierender Aufruf im NtmBlockStateProvider,
#   2. ICustomBlockModelRegister an der Blockklasse -- der Erzeuger ruft das fuer jeden Block,
#   3. eine handgeschriebene Datei unter assets/hbmsntm/blockstates.
#
# Die beiden Haelften messen unterschiedlich scharf, und das mit Absicht. Die Doppelung wird
# streng gezaehlt, ueber eine Liste zustandsdefinierender Aufrufe -- nur so faellt der Absturz
# auf. Fuer die Gegenrichtung reicht das nicht: RBMK, Baender, Rohre und Tueren beschreibt der
# Erzeuger in eigenen Methoden ueber Zwischenvariablen, und eine strenge Zaehlung meldete dort
# 115 Bloecke, die laengst ein Modell haben. Dort wird deshalb nur gefragt, ob der Block im
# Erzeuger ueberhaupt vorkommt. Grob -- aber ohne Fehlalarm, und die vier Fluessigkeitsbloecke
# hat genau das gefunden.
#
# GEMESSEN: 608 Bloecke, null Funde in beiden Haelften. Mit der wieder eingebauten doppelten
# PWR-Steuerung genau ein Fund, mit entferntem fluidBlock-Aufruf fuer mud genau einer.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, os, sys, glob

def entkommentieren(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

blocks_src = entkommentieren(open('src/main/java/com/hbm/blocks/NtmBlocks.java', encoding='utf-8').read())

# Feldname -> (Registriername, Blockklasse)
felder = {}
for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\s*=\s*[^;]{0,500}?\bregister\w*\(\s*"([^"]+)"\s*,\s*\(\)\s*->\s*new\s+([A-Za-z0-9_.]+)', blocks_src, re.S):
    felder.setdefault(m.group(1), (m.group(2), m.group(3).split('.')[-1]))
for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\s*=\s*[^;]{0,500}?\bregister\w*\(\s*"([^"]+)"', blocks_src, re.S):
    felder.setdefault(m.group(1), (m.group(2), None))

# Welche Klassen beschreiben ihr Modell selbst?
selbst = set()
for pfad in glob.glob('src/main/java/**/*.java', recursive=True):
    quelle = open(pfad, encoding='utf-8').read()
    if 'ICustomBlockModelRegister' in quelle and 'implements' in quelle:
        for m in re.finditer(r'class\s+(\w+)[^{]*implements[^{]*ICustomBlockModelRegister', quelle):
            selbst.add(m.group(1))

hand = {os.path.basename(p)[:-5] for p in glob.glob('src/main/resources/assets/hbmsntm/blockstates/*.json')}

prov = entkommentieren(open('src/main/java/com/hbm/datagen/NtmBlockStateProvider.java', encoding='utf-8').read())

# Aufrufe, die eine Zustandsbeschreibung anlegen.
aufrufe = ['getVariantBuilder', 'getMultipartBuilder', 'simpleBlock', 'simpleBlockWithItem',
           'simpleCubeAllBlock', 'simpleCubeBottomTopBlock', 'cubeTop', 'horizontalBlock',
           'directionalBlock', 'paneBlock', 'slabBlock', 'stairsBlock', 'doorBlock',
           'trapdoorBlock', 'fenceBlock', 'fenceGateBlock', 'wallBlock', 'logBlock', 'axisBlock',
           'particleOnlyBlock', 'sellafieldSlaked', 'layeringBlock', 'railBlock', 'fluidBlock']

zaehler = {}
for name in aufrufe:
    for m in re.finditer(r'\b' + name + r'\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)', prov):
        zaehler[m.group(1)] = zaehler.get(m.group(1), 0) + 1

# Fuer die Gegenrichtung reicht die Aufrufliste nicht: viele Bloecke beschreibt der Erzeuger
# in eigenen Methoden ueber Zwischenvariablen oder Schleifen (RBMK, Baender, Rohre, Tueren).
# Dort wird nur gefragt, OB der Block im Erzeuger ueberhaupt vorkommt -- grob, aber ohne
# Fehlalarm. Die Messung oben bleibt streng, denn nur sie deckt den Absturz auf.
erwaehnt = set(re.findall(r'NtmBlocks\.([A-Z][A-Z0-9_]*)', prov))

doppelt, ohne = [], []
for feld, (regname, klasse) in sorted(felder.items()):
    if zaehler.get(feld, 0) + (1 if klasse in selbst else 0) + (1 if regname in hand else 0) > 1:
        doppelt.append((feld, regname, zaehler.get(feld, 0) + (1 if klasse in selbst else 0) + (1 if regname in hand else 0)))
    if feld not in erwaehnt and klasse not in selbst and regname not in hand:
        ohne.append((feld, regname))

print("Pruefe Blockzustaende ... %d Bloecke, %d Aufrufe im Erzeuger, %d Klassen mit eigenem Modell, %d Handdateien"
      % (len(felder), sum(zaehler.values()), len(selbst), len(hand)))
print("  nirgends erwaehnt   : %d" % len(ohne))
print("  mehrfach beschrieben: %d" % len(doppelt))

if not ohne and not doppelt:
    print("OK - jeder Block wird genau einmal beschrieben.")
    sys.exit(0)

if doppelt:
    print()
    print("MEHRFACH BESCHRIEBEN -- runData bricht damit ab:")
    for feld, regname, n in doppelt:
        print("   NtmBlocks.%s (%s): %d Quellen" % (feld, regname, n))

if ohne:
    print()
    print("NIRGENDS ERWAEHNT -- im Spiel ohne Modell:")
    for feld, regname in ohne:
        print("   NtmBlocks.%s (%s)" % (feld, regname))

sys.exit(1)
PY
