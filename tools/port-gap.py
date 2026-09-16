#!/usr/bin/env python3
"""Misst, was vom Original noch nicht portiert ist -- jedes Mal neu, statt von Hand gefuehrt.

WARUM: die Zahlen in der Roadmap sind mehrfach veraltet, weil sie abgeschrieben wurden. Die
Maschinenzaehlung der Runde 135 etwa galt fuer TileEntityMachine*.java OHNE Unterordner und
meldete null fehlende Maschinen -- die breite Aufnahme zeigt ueber hundert. Dieses Skript
rechnet beides aus und sagt dazu, wie belastbar die jeweilige Zahl ist.

BELASTBAR sind die Klassenvergleiche. Dort steht im Original je Sache eine Datei, im Port
ebenso, und die Namen unterscheiden sich nur nach festen Regeln (TileEntityX -> XBlockEntity,
oft mit vorangestelltem "Machine"). Wo die Zuordnung scheitert, ist die Sache mit grosser
Wahrscheinlichkeit wirklich nicht da.

NUR EINE OBERE SCHRANKE sind die Namensvergleiche fuer Bloecke und Gegenstaende. Der Port
faltet zusammen, was das Original ueber Metadaten trennt: aus sechzehn Metadaten-Werten eines
Blocks wird eine Blockzustands-Eigenschaft, aus einem Metadaten-Gegenstand ein EnumMultiItem.
"can_smart" heisst hier "drink.smart" und zaehlt im rohen Vergleich als fehlend, obwohl es da
ist. Die Zahl ist also eine Obergrenze, kein Ist-Wert.

Aufruf:
    tools/port-gap.py                 -- Uebersicht
    tools/port-gap.py --list BEREICH  -- Einzelnamen (blockentities, entities, armor, world)
"""

import os
import re
import subprocess
import sys
from collections import defaultdict

UPSTREAM = 'hbm-upstream/master'


def upstream_dateien():
    aus = subprocess.run(['git', 'ls-tree', '-r', '--name-only', UPSTREAM],
                         capture_output=True, text=True)
    if aus.returncode != 0:
        print("FEHLER: %s ist nicht erreichbar -- git remote add hbm-upstream ... && git fetch" % UPSTREAM)
        sys.exit(2)
    return [z for z in aus.stdout.split('\n') if z.endswith('.java')]


def port_dateien(unterordner):
    treffer = []
    for wurzel, _, dateien in os.walk(os.path.join('src/main/java/com/hbm', unterordner)):
        for d in dateien:
            if d.endswith('.java'):
                treffer.append(os.path.join(wurzel, d))
    return treffer


def kern(name):
    """Auf den gemeinsamen Nenner bringen: TileEntityMachineFoo, FooBlockEntity -> foo."""
    for vorn in ('TileEntity', 'Entity'):
        if name.startswith(vorn):
            name = name[len(vorn):]
    for hinten in ('BlockEntity', 'Entity'):
        if name.endswith(hinten):
            name = name[:-len(hinten)]
    return name.lower().replace('_', '')


def vergleiche(up_pfade, port_pfade, up_filter=lambda n: True):
    port_kerne = {kern(os.path.basename(p)[:-5]) for p in port_pfade}
    fehlend = defaultdict(list)
    gesamt = 0
    for p in up_pfade:
        name = os.path.basename(p)[:-5]
        if not up_filter(name):
            continue
        gesamt += 1
        k = kern(name)
        # Der Port stellt Maschinen haeufig ein "Machine" voran -- und laesst es manchmal weg.
        if k in port_kerne or ('machine' + k) in port_kerne or k.replace('machine', '') in port_kerne:
            continue
        gruppe = os.path.dirname(p).split('/com/hbm/', 1)[-1]
        fehlend[gruppe].append(name)
    return gesamt, fehlend


def namen_original(pfad, muster):
    aus = subprocess.run(['git', 'show', UPSTREAM + ':' + pfad], capture_output=True, text=True).stdout
    aus = re.sub(r'/\*.*?\*/', '', re.sub(r'//[^\n]*', '', aus), flags=re.S)
    return set(re.findall(muster, aus))


def main():
    up = upstream_dateien()
    bereiche = {}

    # --- Klassenvergleiche: belastbar --------------------------------------------------
    bereiche['blockentities'] = vergleiche(
        [p for p in up if '/com/hbm/tileentity/' in p],
        port_dateien('blockentity'),
        lambda n: n.startswith('TileEntity'))

    bereiche['entities'] = vergleiche(
        [p for p in up if p.startswith('src/main/java/com/hbm/entity/')],
        port_dateien('entity'))

    bereiche['armor'] = vergleiche(
        [p for p in up if '/com/hbm/items/armor/' in p],
        port_dateien('items/armor'))

    bereiche['world'] = vergleiche(
        [p for p in up if p.startswith('src/main/java/com/hbm/world/')],
        port_dateien('world'))

    if len(sys.argv) > 2 and sys.argv[1] == '--list':
        gewaehlt = sys.argv[2]
        if gewaehlt not in bereiche:
            print("Unbekannter Bereich. Moeglich: " + ", ".join(bereiche)); sys.exit(2)
        gesamt, fehlend = bereiche[gewaehlt]
        for gruppe in sorted(fehlend, key=lambda g: -len(fehlend[g])):
            print("%s (%d)" % (gruppe, len(fehlend[gruppe])))
            for n in sorted(fehlend[gruppe]):
                print("    " + n)
        sys.exit(0)

    print("Portierungsstand gegen %s" % UPSTREAM)
    print()
    print("KLASSENVERGLEICH (belastbar)")
    print("  %-18s %8s %8s %8s" % ("Bereich", "Original", "Port", "fehlend"))
    for name, ordner in (('blockentities', 'blockentity'), ('entities', 'entity'),
                         ('armor', 'items/armor'), ('world', 'world')):
        gesamt, fehlend = bereiche[name]
        offen = sum(len(v) for v in fehlend.values())
        print("  %-18s %8d %8d %8d" % (name, gesamt, len(port_dateien(ordner)), offen))

    # --- Namensvergleiche: obere Schranke ----------------------------------------------
    ub = namen_original('src/main/java/com/hbm/blocks/ModBlocks.java', r'setBlockName\("([^"]+)"\)')
    ui = namen_original('src/main/java/com/hbm/items/ModItems.java', r'setUnlocalizedName\("([^"]+)"\)')

    pb = re.sub(r'//[^\n]*', '', open('src/main/java/com/hbm/blocks/NtmBlocks.java', encoding='utf-8').read())
    nb = {m.group(1) for m in re.finditer(r'\bregister\w*\(\s*"([^"]+)"', pb)}

    ni = set()
    for wurzel, _, dateien in os.walk('src/main/java'):
        if '/blocks' in wurzel:
            continue
        for d in dateien:
            if not d.endswith('.java'):
                continue
            s = re.sub(r'//[^\n]*', '', open(os.path.join(wurzel, d), encoding='utf-8').read())
            for m in re.finditer(r'\b(?:ITEMS|itemRegistry)\.register\(\s*"([^"]+)"', s):
                ni.add(m.group(1))
            for m in re.finditer(r'\bregister\w+\(\s*"([^"]+)"', s):
                ni.add(m.group(1))

    print()
    print("NAMENSVERGLEICH (obere Schranke -- der Port faltet Metadaten zusammen)")
    print("  %-18s %8s %8s %8s" % ("Bereich", "Original", "Port", "hoechstens"))
    print("  %-18s %8d %8d %8d" % ("blocknamen", len(ub), len(nb), len(ub - nb)))
    print("  %-18s %8d %8d %8d" % ("gegenstandsnamen", len(ui), len(ni), len(ui - ni)))

    print()
    print("Einzelnamen: tools/port-gap.py --list blockentities|entities|armor|world")
    print("Bauwerke gesondert: tools/structure-gap.py --list")


if __name__ == '__main__':
    main()
