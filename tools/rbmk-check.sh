#!/usr/bin/env bash
#
# rbmk-check.sh -- prueft die Aufbauten auf den RBMK-Saeulen.
#
# WARUM ES DIESES TOR GIBT: gemeldet wurde, dass die selbsttaetigen Steuerstaebe "oben einfach
# flach" sind. Das Original zeichnet die Saeulen naemlich nicht mit gewoehnlichen Wuerfeln,
# sondern mit eigenen Blockzeichnern (RenderRBMKRod, RenderRBMKControl, RenderRBMKReflector).
# Die setzen auf den OBERSTEN Block der Saeule, im Blockraum DARUEBER:
#
#   ohne Deckel, Rohrsaeule : vier Stutzen, je 6x2x6, in den Ecken mit einem Pixel Rand
#   mit Deckel              : eine Platte ueber die volle Flaeche, vier Pixel hoch
#
# Beides schliesst sich aus. Der Port hatte weder das eine noch das andere: oben blieb eine
# glatte Flaeche, und der Deckel war eine Umtexturierung der Oberseite statt einer Platte
# darueber -- auch das war gemeldet worden und war richtig beobachtet.
#
# Die Vergleichsliste steht in tools/rbmk-list.txt, weil die CI den Fernzweig
# hbm-upstream/master nicht hat; wie sie erzeugt wurde, steht in ihrem Kopf.
#
# ZWEITER TEIL (Runde 167): der Brennstoffkanal ist ein Rohr, kein Wuerfel. Sein Blockmodell
# hat weder Deck- noch Bodenflaeche -- dort legt RenderRBMKFuelChannel Kappe und Innenrohr aus
# rbmk_element darueber, so wie das Original ueber overrideOnlyRenderSides. Fehlt eines der
# beiden Bilder, saehe man in den Block hinein; das prueft dieses Tor mit.
#
# NACHGEMESSEN (Runden 165/167): 7 Rohrsaeulen im Original, alle sieben tragen im Port
# hasPipes() und haben ihre beiden Rohrbilder; alle vier Kanalsorten haben Kappe und
# Innenrohr. 0 Befunde. Nimmt man hasPipes() aus RBMKHeaterBlock heraus, meldet das Tor genau
# rbmk_heater; nimmt man rbmk_element_inner.png weg, meldet es genau dieses Bild. Beide Male
# Exit-Code 1, direkt geprueft.

set -uo pipefail
cd "$(dirname "$0")/.."
exec python3 tools/rbmk-check.py
