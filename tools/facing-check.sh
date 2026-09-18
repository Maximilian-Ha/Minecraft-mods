#!/usr/bin/env bash
#
# facing-check.sh -- prueft, ob jeder Darsteller so herum steht wie im Original.
#
# WARUM ES DIESES TOR GIBT: in Runde 162 ist beim Nachreichen der Gaszentrifuge
# aufgefallen, dass RenderCentrifuge um 90 Grad verdreht steht. Die anschliessende
# Messung ueber ALLE Darsteller mit Ausrichtungsschalter fand 14 Abweichungen in zwei
# regelmaessigen Mustern -- siebenmal Osten und Westen vertauscht (die Maschine steht
# gespiegelt), siebenmal die ganze Zuordnung um genau -90 Grad verdreht. Das sind keine
# vierzehn Einzelfehler, sondern zwei uebernommene Fehlmuster. Runde 163 hat sie
# begradigt; dieses Tor haelt das fest.
#
# WAS VERGLICHEN WIRD: die GESAMTDREHUNG je Blickrichtung, also eine Y-Drehung vor dem
# Schalter plus der Schalterwert. Manche Darsteller des Ports haben die Vordrehung des
# Originals in den Schalter hineingerechnet (RenderRockMill, RenderSolarBoiler); ein
# Vergleich der Summen haelt beide Schreibweisen fuer richtig, und das sind sie auch.
#
# WARUM DIE ZUORDNUNGEN UEBERHAUPT VERGLEICHBAR SIND: das Original setzt die Ausrichtung
# ueber i = floor(yaw*4/360 + 0.5) & 3 auf die Gegenrichtung des Spielers, der Port ueber
# context.getHorizontalDirection().getOpposite(). Das ist dasselbe. Ein Block, der
# getDirModified ueberschreibt, waere eine Ausnahme -- im Original tun das nur
# MachineHeatBoiler, MachineSawmill, MachineStirling, SoyuzLauncher und RBMKBase, und
# keiner davon steht in der Liste.
#
# Die Vergleichsliste steht in tools/facing-list.txt, weil die CI den Fernzweig
# hbm-upstream/master nicht hat; wie sie erzeugt wurde, steht in ihrem Kopf.
#
# DREI SCHREIBWEISEN: der Port schreibt den Schalter als Anweisung mit vier Faellen, als
# Anweisung mit drei Faellen plus default, und als Ausdruck, der die Gradzahl liefert; der
# Schalterkopf ist mal eine Variable, mal ein Ruf (switch(getFacing(be))). Die erste
# Fassung dieses Tors las nur die erste Form und uebersprang damit 15 Darsteller
# STILLSCHWEIGEND -- darunter eine echte Abweichung. Darum meldet es jetzt jeden Schalter,
# den es nicht lesen kann, als eigenen Befund und wird rot.
#
# NACHGEMESSEN (Runde 163): 81 Darsteller in der Liste, 81 davon im Port vergleichbar,
# 0 unlesbar. VOR der Begradigung meldete das Tor genau die 15 bekannten Abweichungen,
# danach 0. Dreht man RenderCentrifuge wieder zurueck, meldet es genau RenderCentrifuge
# und endet mit 1 (Exit-Code direkt geprueft, nicht durch eine Pipe).

set -uo pipefail
cd "$(dirname "$0")/.."
exec python3 tools/facing-check.py
