#!/usr/bin/env bash
#
# Prueft, dass jeder Blockzustand, den tools/nbt2structure.py erzeugt, am jeweiligen Block
# des Ports ueberhaupt vorkommt.
#
# WARUM: der Umsetzer schreibt Eigenschaften als Zeichenketten in die .nbt-Datei --
# "facing", "axis", "layer". Ob der Block sie hat, prueft dort niemand. Minecraft
# ueberliest eine unbekannte Eigenschaft stillschweigend (NbtUtils.readBlockState geht vom
# Standardzustand aus und setzt nur, was es kennt): aus einer Treppe mit falsch
# geschriebenem "facng" wird eine Treppe nach Norden, aus einer Saeule mit falscher Achse
# eine stehende. Kein Absturz, kein Protokoll -- das Bauwerk sieht nur falsch aus, und
# niemand weiss warum.
#
# WIE: die Eigenschaftsnamen jeder Blockklasse des Ports stehen in ihrem
# createBlockStateDefinition, und die Klasse je Blockname in NtmBlocks.java. Beides liest
# dieses Tor und haelt es gegen die Tabelle des Umsetzers.
#
# WAS ES NICHT PRUEFT: Vanilla-Bloecke. Deren Klassen liegen nicht auf der Platte -- die
# Tore laufen ohne Minecraft. Fuer sie steht in der Tabelle des Umsetzers die
# Flattening-Regel, und die ist nachlesbar, nicht nachmessbar.
#
# NACHGEMESSEN (Runde 257): 876 der 1367 Tabelleneintraege zeigen auf einen Block des Ports,
# zusammen 228 verschiedene; null Funde. Schreibt man in nbt2structure.py eine Eigenschaft
# falsch, meldet die Pruefung genau sie -- gemessen an dungeon_chain mit "axsi" statt "axis".

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os
import re
import sys
import types

# Den Umsetzer aus dem QUELLTEXT laden, nicht ueber den Bytecode-Zwischenspeicher.
# Python haelt ein .pyc fuer gueltig, wenn Groesse und Aenderungszeit auf die Sekunde
# genau passen. Beim Gegenversuch zu diesem Tor -- eine Eigenschaft falsch schreiben,
# messen, zurueckkopieren -- stimmt beides, und das Tor misst danach eine Datei, die es
# auf der Platte nicht mehr gibt. Genau so hat es sich in Runde 257 verlaufen.
sys.dont_write_bytecode = True
_quelle = open('tools/nbt2structure.py', encoding='utf-8').read()
umsetzer = types.ModuleType('nbt2structure')
umsetzer.__file__ = 'tools/nbt2structure.py'
exec(compile(_quelle, 'tools/nbt2structure.py', 'exec'), umsetzer.__dict__)

MODID = umsetzer.MODID

# Eigenschaften, die Vanilla-Oberklassen mitbringen. Sie stehen nicht im Quelltext des
# Ports, gehoeren dem Block aber trotzdem.
VANILLA_KLASSEN = {
    'Block': set(),
    'StairBlock': {'facing', 'half', 'shape', 'waterlogged'},
    'SlabBlock': {'type', 'waterlogged'},
    'DoorBlock': {'facing', 'half', 'hinge', 'open', 'powered'},
    'TrapDoorBlock': {'facing', 'half', 'open', 'powered', 'waterlogged'},
    'LadderBlock': {'facing', 'waterlogged'},
    'FenceBlock': {'north', 'east', 'south', 'west', 'waterlogged'},
    'RotatedPillarBlock': {'axis'},
    'RailBlock': {'shape', 'waterlogged'},
    'LiquidBlock': {'level'},
    'RedstoneLampBlock': {'lit'},
    'HorizontalDirectionalBlock': {'facing'},
    'BaseFireBlock': set(),
    'ChainBlock': {'axis', 'waterlogged'},
    'FallingBlock': set(),
    'BaseEntityBlock': set(),
    'BushBlock': set(),
    'IronBarsBlock': {'north', 'east', 'south', 'west', 'waterlogged'},
    'CrossCollisionBlock': {'north', 'east', 'south', 'west', 'waterlogged'},
    'ColoredFallingBlock': set(),
    'SimpleFallingBlock': set(),
}

# Die Eigenschaften, die der Port selbst in NtmBlockStateProperties fuehrt.
EIGENE_PROPS = {'META': 'meta'}

# Namen der Eigenschaften aus BlockStateProperties, soweit der Port sie benutzt.
BSP = {
    'FACING': 'facing', 'HORIZONTAL_FACING': 'facing', 'AXIS': 'axis',
    'HORIZONTAL_AXIS': 'axis', 'LIT': 'lit', 'WATERLOGGED': 'waterlogged',
    'POWERED': 'powered', 'OPEN': 'open', 'HALF': 'half', 'DOUBLE_BLOCK_HALF': 'half',
    'STAIRS_SHAPE': 'shape', 'SLAB_TYPE': 'type', 'RAIL_SHAPE': 'shape',
    'DOOR_HINGE': 'hinge', 'LEVEL': 'level', 'LEVEL_CAULDRON': 'level',
    'NORTH': 'north', 'EAST': 'east', 'SOUTH': 'south', 'WEST': 'west',
    'UP': 'up', 'DOWN': 'down', 'ATTACH_FACE': 'face', 'TRIGGERED': 'triggered',
    'AGE_15': 'age', 'AGE_7': 'age', 'ROTATION_16': 'rotation',
}

quelle_bloecke = open('src/main/java/com/hbm/blocks/NtmBlocks.java', encoding='utf-8').read()

# Blockname -> Klassenname des Erbauers
klasse_je_block = {}
for treffer in re.finditer(r'\b(?:BLOCKS\.)?register[A-Za-z]*\(\s*"([a-z0-9_]+)"\s*,\s*\(\)\s*->\s*new\s+([A-Za-z0-9_.]+)\(', quelle_bloecke):
    klasse_je_block.setdefault(treffer.group(1), treffer.group(2).split('.')[-1])

# Bloecke, die ueber eine Hilfe angelegt werden, die die Klasse selbst kennt.
for treffer in re.finditer(r'\bregisterPipe\(\s*"([a-z0-9_]+)"', quelle_bloecke):
    klasse_je_block.setdefault(treffer.group(1), 'RotatedPillarBlock')

# Alle Klassendateien des Ports einsammeln.
datei_je_klasse = {}
for verzeichnis, _, namen in os.walk('src/main/java/com/hbm'):
    for name in namen:
        if name.endswith('.java'):
            datei_je_klasse.setdefault(name[:-5], os.path.join(verzeichnis, name))


def eigenschaften(klassenname, tiefe=0):
    """Die Eigenschaftsnamen einer Blockklasse, samt denen ihrer Oberklassen."""

    if tiefe > 8:
        return set()
    if klassenname in VANILLA_KLASSEN:
        return set(VANILLA_KLASSEN[klassenname])

    pfad = datei_je_klasse.get(klassenname)
    if pfad is None:
        return None

    text = open(pfad, encoding='utf-8').read()

    gefunden = set()

    # Eigene Eigenschaften: XProperty NAME = ... create("name" ...)
    eigene = {}
    for treffer in re.finditer(r'\b([A-Z][A-Z0-9_]*)\s*=\s*[A-Za-z]*Property\.create\(\s*"([a-z0-9_]+)"', text):
        eigene[treffer.group(1)] = treffer.group(2)
    # Uebernommene: XProperty NAME = BlockStateProperties.Y
    for treffer in re.finditer(r'\b([A-Z][A-Z0-9_]*)\s*=\s*BlockStateProperties\.([A-Z0-9_]+)', text):
        if treffer.group(2) in BSP:
            eigene[treffer.group(1)] = BSP[treffer.group(2)]
    # Und die des Ports: XProperty NAME = NtmBlockStateProperties.Y
    for treffer in re.finditer(r'\b([A-Z][A-Z0-9_]*)\s*=\s*NtmBlockStateProperties\.([A-Z0-9_]+)', text):
        eigene[treffer.group(1)] = EIGENE_PROPS.get(treffer.group(2), treffer.group(2).lower())

    for block in re.findall(r'builder\.add\(([^;]*?)\)\s*;', text, re.S):
        for teil in block.split(','):
            teil = teil.strip()
            if not teil:
                continue
            kurz = teil.split('.')[-1]
            if teil.startswith('BlockStateProperties.'):
                if kurz in BSP:
                    gefunden.add(BSP[kurz])
            elif kurz in eigene:
                gefunden.add(eigene[kurz])
            elif kurz in BSP:
                gefunden.add(BSP[kurz])

    oberklasse = re.search(r'class\s+' + re.escape(klassenname) + r'\b[^{]*?\bextends\s+([A-Za-z0-9_]+)', text)
    if oberklasse:
        geerbt = eigenschaften(oberklasse.group(1), tiefe + 1)
        if geerbt is None:
            return None
        gefunden |= geerbt

    return gefunden


geprueft = 0
unbekannt = []
funde = []

for (_, _), (blockname, eig) in sorted(umsetzer.TABELLE.items()):
    if not blockname.startswith(MODID + ':'):
        continue
    kurz = blockname.split(':', 1)[1]
    klasse = klasse_je_block.get(kurz)
    if klasse is None:
        unbekannt.append((kurz, 'kein register-Aufruf in NtmBlocks.java'))
        continue
    vorhanden = eigenschaften(klasse)
    if vorhanden is None:
        unbekannt.append((kurz, 'Oberklasse ausserhalb des Ports: ' + klasse))
        continue
    geprueft += 1
    for name in eig:
        if name not in vorhanden:
            funde.append((kurz, klasse, name, sorted(vorhanden)))

# Doppelte zusammenfassen.
funde = sorted(set(funde and [(a, b, c, tuple(d)) for a, b, c, d in funde]))
unbekannt = sorted(set(unbekannt))

print('Pruefe Bauwerkszustaende ... %d Eintraege der Umsetzungstabelle' % geprueft)
print('  Eigenschaften, die es am Block nicht gibt : %d' % len(funde))
print('  Bloecke ohne auffindbare Klasse           : %d' % len(unbekannt))

if unbekannt:
    print()
    print('BLOCK OHNE KLASSE -- das Tor kann ihn nicht pruefen:')
    for name, grund in unbekannt:
        print('   %-32s %s' % (name, grund))

if funde:
    print()
    print('EIGENSCHAFT, DIE ES NICHT GIBT -- Minecraft ueberliest sie stillschweigend:')
    for name, klasse, eigenschaft, vorhanden in funde:
        print('   %s (%s): "%s"' % (name, klasse, eigenschaft))
        print('      der Block hat: %s' % (', '.join(vorhanden) or '(keine)'))

if funde or unbekannt:
    sys.exit(1)

print('OK - jede erzeugte Eigenschaft gibt es am jeweiligen Block.')
PYEOF
