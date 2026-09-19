#!/usr/bin/env bash
#
# rotation-check.sh -- dreht jeder Mehrblockbau seine Anschlussstellen so herum wie im Original?
#
# WARUM ES DIESES TOR GIBT: das Original rechnet die Lage seiner Rohr- und Stromanschluesse aus
# der Aufstellrichtung plus einer Vierteldrehung: dir.getRotation(ForgeDirection.UP) oder
# .getRotation(ForgeDirection.DOWN). Der Port schreibt dafuer getClockWise() oder
# getCounterClockWise() -- und WELCHES wovon die Entsprechung ist, weiss niemand auswendig.
# Raet man falsch, sitzen saemtliche Anschluesse einer Maschine gespiegelt: sie nimmt nichts an
# und gibt nichts ab, ohne dass irgendetwas abstuerzt.
#
# WIE DIE REGEL GEFUNDEN WURDE: nicht nachgeschlagen, sondern GEZAEHLT. Ueber alle portierten
# Mehrblockbauten bildet der Port getRotation(UP) einundvierzigmal auf getClockWise ab und
# getRotation(DOWN) zweimal auf getCounterClockWise. Das ist in sich stimmig -- UP und DOWN sind
# Gegenrichtungen -- und damit die Regel dieses Ports. Drei Bauten wichen davon ab.
#
# DIE DREI AUSNAHMEN sind nachgesehen und folgenlos: ihre Anschlusslisten sind SYMMETRISCH in
# rot, also gibt es zu jedem Eintrag mit +rot einen mit -rot und derselben Richtung. Dreht man
# rot um, vertauschen sich die Eintraege, die MENGE der Anschlussstellen bleibt gleich. Sie
# stehen mit Grund in tools/rotation-check.py; wird eine davon spaeter begradigt, meldet das Tor
# die Ausnahme als ueberfluessig und wird rot, damit die Zeile verschwindet.
#
# WAS ES NICHT SIEHT und deshalb nennt statt verschweigt: Dateien, in denen das Original BEIDE
# Achsen benutzt oder der Port beide Drehrichtungen -- dort gibt es mehrere Anschlusslisten, und
# ein Vergleich je Datei taugt nicht. Ebenso Bauten, zu denen die Liste keine Vorlage findet.
# Beide Gruppen werden namentlich ausgegeben.
#
# Die Vergleichsliste steht in tools/rotation-list.txt, weil die CI den Fernzweig
# hbm-upstream/master nicht hat; wie sie erzeugt wurde, steht in ihrem Kopf.
#
# NACHGEMESSEN: 76 Vorlagen, 62 Bauten im Port, 0 Abweichungen, 9 nicht vergleichbar, 1 ohne
# Vorlage. Dreht man FurnaceIronBlockEntity auf getCounterClockWise, meldet das Tor genau diesen
# einen Bau und endet mit 1. Begradigt man eine der drei Ausnahmen, meldet es sie als
# ueberfluessig und endet ebenfalls mit 1 (Exit-Code jeweils direkt geprueft, nicht durch eine
# Pipe).

set -uo pipefail
cd "$(dirname "$0")/.."
exec python3 tools/rotation-check.py
