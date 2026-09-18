#!/usr/bin/env bash
#
# inventory-check.sh -- prueft, ob jedes Gegenstandsbild die Zahlen des Originals traegt.
#
# WARUM ES DIESES TOR GIBT: ItemRenderBase verkleinert im Inventar auf ein Sechzehntel
# (scale(0.062)) und erwartet, dass der Darsteller wieder hochskaliert. Diese Zahl laesst sich
# ohne Spielstart nicht erraten. Gemeldet wurde es an der RBMK-Konsole -- dort stand
# scale(0.35) statt 2.5, also ein Puenktchen statt eines Bildes. Die Messung ueber alle
# Darsteller fand 41 Abweichungen, darunter sieben, die als Puenktchen unsichtbar waren
# (Konsole und fuenf Geschuetze). Runde 164 hat alle 41 auf die Zahlen des Originals gesetzt.
#
# ZWEI ANMELDEWEGE IM ORIGINAL -- und daran ist Runde 162/163 gescheitert:
#   1. com/hbm/render/item/ItemRenderLibrary.java  -- eine Tabelle Block -> ItemRenderBase
#   2. jeder TESR, der IItemRendererProvider umsetzt
# Wer nur den zweiten ansieht, haelt Zyklotron, Bergbaulaser und Radiothermalgenerator
# faelschlich fuer darstellerlos und gibt ihnen ein flaches Sinnbild. Die Liste liest beide.
#
# Die Vergleichsliste steht in tools/inventory-list.txt, weil die CI den Fernzweig
# hbm-upstream/master nicht hat; wie sie erzeugt wurde, steht in ihrem Kopf.
#
# NACHGEMESSEN (Runde 164): 199 Eintraege im Original, 117 Paare vergleichbar, 0 abweichend.
# Setzt man die Konsole wieder auf scale(0.35), meldet das Tor genau RBMK_CONSOLE und endet
# mit 1 (Exit-Code direkt geprueft, nicht durch eine Pipe).

set -uo pipefail
cd "$(dirname "$0")/.."
exec python3 tools/inventory-check.py
