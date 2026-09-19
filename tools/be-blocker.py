#!/usr/bin/env python3
"""Welche der fehlenden Blockentitaeten lassen sich SOFORT portieren?

WARUM: tools/port-gap.py sagt, WIE VIELE fehlen, aber nicht, welche davon an etwas anderem
haengen. Wer die naechste Runde waehlt, will genau das wissen -- eine Blockentitaet, deren
Vorlage einen Mob braucht, den es im Port nicht gibt, kostet eine halbe Stunde Lesen und
endet dann doch in der Schublade.

WIE: fuer jede fehlende Blockentitaet werden die com.hbm-Importe des Originals aufgeloest und
gegen den Port gehalten. Gefunden wird dabei jede Klasse, jede Schnittstelle, jedes Enum und
jeder Record im Port -- auch die verschachtelten, denn Mats.MaterialStack steht in keiner
eigenen Datei.

DIE ABBILDUNG in tools/portmap.py ist der Kern. Der Port hat die Infrastruktur des Originals umbenannt, und
ohne sie meldet das Werkzeug ModItems, ModBlocks und BlockPos als fehlend -- also ausgerechnet
das, was es ueberall gibt. Jede Zeile ist nachgesehen, nicht geraten.

WARUM DIE ZAHL VON port-gap.py ABWEICHT: dieses Werkzeug kennt auch die verschachtelten Typen
des Ports, port-gap.py nur die Dateinamen. Wo eine Sache des Originals im Port als innere
Klasse steht, sieht sie nur dieses Werkzeug. Der Unterschied betraegt derzeit eins.

NICHT ERFASST wird, was ohne Import erreichbar ist: Vanilla-Klassen, Methoden, Felder. Die
Liste der sofort Portierbaren ist deshalb eine ANNAEHERUNG von unten -- wer eine davon
aufgreift, findet im Zweifel doch noch eine Luecke, aber selten.

UND SIE GILT DER BLOCKENTITAET, NICHT DEM BLOCK. Beim ersten Versuch nach dieser Liste ist das
gleich aufgefallen: TileEntityPipeExhaust haengt an nichts, aber der Block darum ist ein
FluidDuctBox -- ein Kastenrohr, das seine Textur aus der Nachbarschaft waehlt, und diese ganze
Familie fehlt im Port noch. Wer einen Eintrag aufgreift, sieht also zuerst nach, welcher Block
die Entitaet traegt.

Aufruf:
    tools/be-blocker.py           -- Uebersicht
    tools/be-blocker.py --list    -- dazu die vollstaendige Liste der Blockierten
"""

import os
import re
import subprocess
import sys
from collections import defaultdict

UP = 'hbm-upstream/master'

# Die Abbildungstabellen stehen in tools/portmap.py -- zwei Werkzeuge brauchen sie, und sie
# duerfen nicht auseinanderlaufen.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from portmap import INFRASTRUKTUR, UMBENANNT


def up_dateien():
    aus = subprocess.run(['git', 'ls-tree', '-r', '--name-only', UP], capture_output=True, text=True)
    if aus.returncode != 0:
        print("FEHLER: %s ist nicht erreichbar" % UP)
        sys.exit(2)
    return [z for z in aus.stdout.split('\n') if z.endswith('.java')]


def kern(name):
    for v in ('TileEntity', 'Entity'):
        if name.startswith(v): name = name[len(v):]
    for h in ('BlockEntity', 'Entity'):
        if name.endswith(h): name = name[:-len(h)]
    return name.lower().replace('_', '')


def port_typen():
    """Jeden im Port deklarierten Typ einsammeln -- auch die verschachtelten."""
    typen = set()
    muster = re.compile(r'\b(?:class|interface|enum|record)\s+(\w+)')
    for wurzel, _, dateien in os.walk('src/main/java'):
        for d in dateien:
            if not d.endswith('.java'): continue
            typen.add(d[:-5])
            text = open(os.path.join(wurzel, d), encoding='utf-8', errors='replace').read()
            typen.update(muster.findall(text))
    return typen


def main():
    up = up_dateien()
    be_up = [p for p in up if '/com/hbm/tileentity/' in p and os.path.basename(p).startswith('TileEntity')]

    typen = port_typen()
    kerne = {kern(t) for t in typen}

    def vorhanden(name):
        if name in typen or name in INFRASTRUKTUR: return True
        if name in UMBENANNT and UMBENANNT[name] in typen: return True
        k = kern(name)
        return k in kerne or ('machine' + k) in kerne or k.replace('machine', '') in kerne

    frei, blockiert = [], {}

    for p in be_up:
        name = os.path.basename(p)[:-5]
        if vorhanden(name): continue

        text = subprocess.run(['git', 'show', UP + ':' + p], capture_output=True, text=True).stdout
        importe = re.findall(r'^import (?:static )?(com\.hbm\.[\w.]+)\.(\w+);', text, re.M)
        fehlt = sorted({k for _, k in importe if not vorhanden(k)})

        if fehlt: blockiert[name] = fehlt
        else: frei.append(name)

    print("Fehlende Blockentitaeten und was sie aufhaelt")
    print()
    print("  fehlend gesamt          : %d" % (len(frei) + len(blockiert)))
    print("  ohne fehlende Vorlage   : %d" % len(frei))
    print("  mit fehlender Vorlage   : %d" % len(blockiert))
    print()
    print("SOFORT PORTIERBAR -- kein com.hbm-Import fehlt:")
    for n in sorted(frei): print("   " + n)

    zaehler = defaultdict(list)
    for name, fehlt in blockiert.items():
        for k in fehlt: zaehler[k].append(name)

    print()
    print("WORAN DIE UEBRIGEN HAENGEN -- die zwanzig haeufigsten:")
    for k, wer in sorted(zaehler.items(), key=lambda x: -len(x[1]))[:20]:
        print("   %3d x  %s" % (len(wer), k))

    if '--list' in sys.argv:
        print()
        print("DIE BLOCKIERTEN EINZELN:")
        for name in sorted(blockiert):
            print("   %-38s %s" % (name, ", ".join(blockiert[name])))


main()
