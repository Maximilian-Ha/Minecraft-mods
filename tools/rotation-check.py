#!/usr/bin/env python3
"""rotation-check -- dreht jeder Mehrblockbau seine Anschlussstellen so herum wie im Original?

Die Erklaerung steht im Kopf von tools/rotation-check.sh.
"""

import os
import re
import sys

WURZEL = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
LISTE = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'rotation-list.txt')

ERWARTET = {'UP': 'cw', 'DOWN': 'ccw'}

# Bauten, deren Anschlussliste SYMMETRISCH in rot ist: zu jedem Eintrag mit +rot gibt es einen
# mit -rot und derselben Richtung. Dreht man rot um, vertauschen sich die Eintraege, die MENGE
# der Anschlussstellen bleibt gleich -- die Abweichung ist also folgenlos. Jede Zeile hier ist
# nachgesehen, nicht geraten; wer eine neue eintraegt, schreibt dazu, warum sie folgenlos ist.
AUSNAHMEN = {
    'CondenserPoweredBlockEntity':       'Liste symmetrisch in rot (je +rot/-rot mit gleicher Richtung)',
    'MachineCompressorBlockEntity':      'Liste symmetrisch in rot (je +rot/-rot mit gleicher Richtung)',
    'MachineCompressorCompactBlockEntity': 'Liste symmetrisch in rot (je +rot/-rot mit gleicher Richtung)',
}


def kern(name):
    for v in ('TileEntity',):
        if name.startswith(v): name = name[len(v):]
    for h in ('BlockEntity',):
        if name.endswith(h): name = name[:-len(h)]
    return name.lower().replace('_', '')


def finde(erwartung, k):
    """Dieselben Ausweichnamen wie in tools/be-blocker.py: der Port stellt manchen Maschinen
    ein "Machine" voran, das Original nicht -- TileEntityStirling wird MachineStirlingBlockEntity."""
    if k in erwartung: return erwartung[k]
    if ('machine' + k) in erwartung: return erwartung['machine' + k]
    ohne = k.replace('machine', '', 1)
    if ohne in erwartung: return erwartung[ohne]
    return None


def main():

    erwartung = {}
    for zeile in open(LISTE, encoding='utf-8'):
        zeile = zeile.strip()
        if not zeile or zeile.startswith('#'): continue
        name, achse = zeile.rsplit(' ', 1)
        erwartung[kern(name)] = achse

    port = {}
    for wurzel, _, dateien in os.walk(os.path.join(WURZEL, 'src/main/java/com/hbm/blockentity')):
        for d in dateien:
            if not d.endswith('.java'): continue
            text = open(os.path.join(wurzel, d), encoding='utf-8', errors='replace').read()
            cw = len(re.findall(r'\.getClockWise\(', text))
            ccw = len(re.findall(r'\.getCounterClockWise\(', text))
            if cw or ccw: port[d[:-5]] = (cw, ccw)

    abweichend, unvergleichbar, ohne_vorlage, erledigte_ausnahmen = [], [], [], []

    for name, (cw, ccw) in sorted(port.items()):

        achse = finde(erwartung, kern(name))

        if achse is None:
            ohne_vorlage.append(name)
            continue

        if achse == 'BEIDE' or (cw and ccw):
            unvergleichbar.append('%s (Original %s, Port %s)' % (
                name, achse, 'beide' if (cw and ccw) else ('cw' if cw else 'ccw')))
            continue

        ist = 'cw' if cw else 'ccw'
        soll = ERWARTET[achse]

        if ist == soll:
            if name in AUSNAHMEN: erledigte_ausnahmen.append(name)
            continue

        if name in AUSNAHMEN: continue

        abweichend.append('%s: Original dreht um %s, erwartet %s, im Port steht %s' % (name, achse, soll, ist))

    print("Pruefe Drehrichtung der Anschlussstellen ... %d Vorlagen in der Liste, %d Bauten im Port"
          % (len(erwartung), len(port)))
    print("  abweichend                 : %d" % len(abweichend))
    print("  nicht vergleichbar         : %d" % len(unvergleichbar))
    print("  ohne Vorlage in der Liste  : %d" % len(ohne_vorlage))
    print("  Ausnahme erledigt, Zeile kann weg : %d" % len(erledigte_ausnahmen))

    for z in abweichend: print("  ABWEICHEND: " + z)
    for z in erledigte_ausnahmen: print("  AUSNAHME UEBERFLUESSIG: " + z)

    # Die beiden anderen Gruppen sind keine Fehler, werden aber genannt -- ein Tor, das
    # schweigt, wo es nicht hinsieht, ist schlimmer als keines.
    if unvergleichbar:
        print("  Nicht vergleichbar (mehrere Achsen oder beide Drehrichtungen in einer Datei):")
        for z in unvergleichbar: print("     " + z)
    if ohne_vorlage:
        print("  Im Port gedreht, im Original ohne getRotation:")
        for z in ohne_vorlage: print("     " + z)

    if abweichend or erledigte_ausnahmen:
        print()
        print("Das Original schreibt dir.getRotation(ForgeDirection.UP) bzw. .DOWN, der Port")
        print("getClockWise() bzw. getCounterClockWise(). UP entspricht getClockWise.")
        sys.exit(1)

    print("OK - jeder Bau dreht so herum wie seine Vorlage.")


main()
