#!/usr/bin/env bash
#
# Prueft die Bauwerksdateien gegen die Pools, die sie benutzen.
#
# Hintergrund: eine Strukturvorlage nennt ihre Anschlusspools als Zeichenkette IM JIGSAW-BLOCK
# der .nbt-Datei -- "hbmsntm:meteor/default". Der Pool selbst entsteht woanders, in
# NtmTemplatePools.java. Zwischen beiden prueft nichts: ein Tippfehler auf einer der beiden
# Seiten uebersetzt sauber, besteht die Datenerzeugung und faellt erst beim Erzeugen einer
# Welt auf -- als Bauwerk, das nach dem ersten Stueck aufhoert, oder als Logzeile, die
# niemand liest.
#
# DREI REGELN:
#   1. Jeder Pool, den ein Jigsaw-Block einer Vorlage nennt, muss in NtmTemplatePools
#      angemeldet sein.
#   2. Jeder Pfad, den NtmTemplatePools als Stueck einsetzt, muss als Datei vorhanden sein.
#   3. Jede vorhandene Vorlagendatei muss von mindestens einem Pool benutzt werden -- sonst
#      liegt sie tot im Jar.
#
# NACHGEMESSEN (Runde 251): 38 Vorlagen, 9 Pools, 66 Anschlussstellen, null Funde. Nimmt man
# in NtmTemplatePools einen Buchstaben aus einem Pfad heraus, meldet die Pruefung genau ihn.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os
import re
import sys

sys.path.insert(0, 'tools')
import types

# Den Umsetzer aus dem QUELLTEXT laden, nicht ueber den Bytecode-Zwischenspeicher.
# Python haelt ein .pyc fuer gueltig, wenn Groesse und Aenderungszeit auf die Sekunde
# genau passen. Beim Gegenversuch zu diesem Tor -- eine Eigenschaft falsch schreiben,
# messen, zurueckkopieren -- stimmt beides, und das Tor misst danach eine Datei, die es
# auf der Platte nicht mehr gibt. Genau so hat es sich in Runde 257 verlaufen.
sys.dont_write_bytecode = True
_quelle = open('tools/nbt2structure.py', encoding='utf-8').read()
nbt = types.ModuleType('nbt2structure')
nbt.__file__ = 'tools/nbt2structure.py'
exec(compile(_quelle, 'tools/nbt2structure.py', 'exec'), nbt.__dict__)

WURZEL = 'src/main/resources/data/hbmsntm/structure'
POOLS = 'src/main/java/com/hbm/world/gen/NtmTemplatePools.java'

quelle = open(POOLS, encoding='utf-8').read()

# Angemeldete Pools: registerKey("meteor/default")
angemeldet = set(re.findall(r'registerKey\("([^"]+)"\)', quelle))

# Eingesetzte Stuecke: add("meteor/meteor-core", ...) -- in der Schleife ueber die
# Beutestuecke steht der Name zusammengesetzt, deshalb auch die Namensliste einsammeln.
eingesetzt = set(re.findall(r'\.add\("([^"]+)"', quelle))
# Und die Einzelbauwerke, deren Pfad durch die Hilfe einzeln(...) laeuft statt durch add(...).
# Ohne diese Zeile meldet das Tor jedes von ihnen als "Datei ohne Benutzer" -- die erste
# Fassung tat genau das, und die Datei lag trotzdem richtig im Pool.
eingesetzt |= set(re.findall(r'einzeln\(context, \w+, \w+, "([^"]+)"\)', quelle))
schleife = re.search(r'for\(String name : new String\[\] \{(.*?)\}\)', quelle, re.S)
if schleife:
    namen = re.findall(r'"([^"]+)"', schleife.group(1))
    vorlage = re.search(r'beute\.add\("([^"]+)" \+ name, 1\);', quelle)
    if vorlage:
        for name in namen:
            eingesetzt.add(vorlage.group(1) + name)
        eingesetzt.discard(vorlage.group(1))

dateien = set()
for verzeichnis, _, namen in os.walk(WURZEL):
    for name in namen:
        if name.endswith('.nbt'):
            voll = os.path.join(verzeichnis, name)
            dateien.add(os.path.relpath(voll, WURZEL)[:-4])

genannt = {}
anschluesse = 0
for pfad in sorted(dateien):
    wurzel = nbt.laden(os.path.join(WURZEL, pfad + '.nbt'))
    for block in wurzel['blocks']:
        daten = block.get('nbt')
        if not daten or daten.get('id') != 'minecraft:jigsaw':
            continue
        anschluesse += 1
        genannt.setdefault(daten['pool'], set()).add(pfad)

fehlende_pools = sorted(p for p in genannt if p.removeprefix('hbmsntm:') not in angemeldet)
fehlende_dateien = sorted(s for s in eingesetzt if s not in dateien)
tote_dateien = sorted(dateien - eingesetzt)

print('Pruefe Bauwerksvorlagen ... %d Vorlagen, %d Pools, %d Anschlussstellen'
      % (len(dateien), len(angemeldet), anschluesse))
print('  Pools ohne Anmeldung   : %d' % len(fehlende_pools))
print('  Stuecke ohne Datei     : %d' % len(fehlende_dateien))
print('  Dateien ohne Benutzer  : %d' % len(tote_dateien))

fund = False

if fehlende_pools:
    fund = True
    print()
    print('POOL OHNE ANMELDUNG -- der Jigsaw-Block zeigt ins Leere, das Bauwerk hoert dort auf:')
    for p in fehlende_pools:
        print('   %s   genannt in: %s' % (p, ', '.join(sorted(genannt[p]))))

if fehlende_dateien:
    fund = True
    print()
    print('STUECK OHNE DATEI -- der Pool kann nichts setzen:')
    for s in fehlende_dateien:
        print('   %s' % s)

if tote_dateien:
    fund = True
    print()
    print('DATEI OHNE BENUTZER -- liegt im Jar, wird nie gesetzt:')
    for s in tote_dateien:
        print('   %s' % s)

if fund:
    sys.exit(1)

print('OK - jede Vorlage hat ihren Pool und jeder Pool seine Vorlagen.')
PYEOF
