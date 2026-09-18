#!/usr/bin/env bash
#
# dummyfacing-check.sh -- prueft, dass Vielblock-Bloecke die Aufstellrichtung beim KERN holen.
#
# WARUM ES DIESES TOR GIBT: gemeldet wurde, dass sich die Leviathan-Turbine nicht auf
# ultradichten Dampf umstellen laesst. Der Hebel sitzt an einer aus der Aufstellrichtung
# berechneten Stelle -- und MachineChungusBlock las diese Richtung aus dem ANGEKLICKTEN Block.
# Die Hilfsbloecke eines Vielblocks tragen in FACING aber die Richtung ZUM KERN
# (MultiblockHandlerXR.fillSpace), nicht die Aufstellrichtung. Damit lag die errechnete
# Hebelstelle fast immer daneben und der Verdichter liess sich nicht weiterschalten. Das
# Original liest an dieser Stelle die Metadaten des Kerns.
#
# NACHGEMESSEN (Runde 166): 1 Befund vor der Berichtigung (MachineChungusBlock), 0 danach.
# Gegen die Fassung aus dem vorigen Commit gehalten meldet die Erkennung genau diese eine
# Datei (Exit-Code direkt geprueft, nicht durch eine Pipe).

set -uo pipefail
cd "$(dirname "$0")/.."
exec python3 tools/dummyfacing-check.py
