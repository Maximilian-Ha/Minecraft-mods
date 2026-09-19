#!/usr/bin/env bash
# ---------------------------------------------------------------------------------------
# Tor 26: jeder benutzte Block- oder Gegenstandstyp muss gedeckt sein -- durch einen
# expliziten Import, durch eine Klasse des Projekts oder durch eine Deklaration in
# derselben Datei.
#
# Warum: beim Aufloesen der Sternimporte (Tor 25) habe ich die Liste der gebrauchten
# Vanilla-Klassen automatisch ermittelt und dabei LiquidBlock uebersehen -- es kommt nur
# in DeferredBlock<LiquidBlock> vor, und die damalige Suche verlangte hinter dem Namen
# einen Punkt, eine Klammer oder ein Leerzeichen. Das waren vier Uebersetzerfehler in der
# CI, die hier lokal aufgefallen waeren.
#
# Bewusst eng gefasst: geprueft werden nur Namen auf -Block und -Item. Ein Tor ueber ALLE
# Typnamen waere nicht zu gebrauchen -- es meldete innere Klassen, Paketgenossen und jeden
# java.lang-Typ, den die Liste nicht kennt, also hunderte Fehlalarme. Die Bloecke und
# Gegenstaende sind aber genau die Stelle, an der die Sammeldateien des Ports haengen.
#
# Dateien mit Fremd-Sternimport bleiben aussen vor: dort ist ohnehin alles gedeckt, und
# der gemischte Fall faellt in Tor 25 auf.
# ---------------------------------------------------------------------------------------
set -u
cd "$(dirname "$0")/.."

echo -n "Pruefe Import-Deckung ... "

ausgabe=$(python3 - <<'PY'
import os, re

projekt = set()
for root, _, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            projekt.add(f[:-5])

geprueft = 0
funde = []

for root, _, files in os.walk('src/main/java'):
    for f in files:
        if not f.endswith('.java'): continue
        pfad = os.path.join(root, f)
        text = open(pfad, encoding='utf-8').read()

        sterne = re.findall(r'^import ([\w.]+)\.\*;', text, re.M)
        if any(s.startswith(('net.minecraft', 'net.neoforged', 'mezz.jei', 'com.mojang'))
               for s in sterne):
            continue

        geprueft += 1
        explizit = {m.group(1) for m in re.finditer(r'^import (?:static )?[\w.]*?\.(\w+);', text, re.M)}

        rumpf = re.sub(r'//[^\n]*', '', text)
        rumpf = re.sub(r'/\*.*?\*/', '', rumpf, flags=re.S)
        rumpf = re.sub(r'"(?:\\.|[^"\\])*"', '""', rumpf)
        rumpf = re.sub(r'^import[^\n]*\n', '', rumpf, flags=re.M)

        # In dieser Datei selbst deklarierte Typen zaehlen als gedeckt.
        eigen = {m.group(1) for m in re.finditer(r'\b(?:class|interface|enum|record)\s+(\w+)', rumpf)}

        # Nur Namen, vor denen kein Punkt steht: Aussen.Innen ist ueber Aussen gedeckt.
        benutzt = {m.group(1) for m in re.finditer(r'(?<![.\w])([A-Z][A-Za-z0-9]*(?:Block|Item))\b', rumpf)}

        for name in sorted(benutzt - explizit - projekt - eigen):
            funde.append((pfad, name))

print("GEPRUEFT %d" % geprueft)
for pfad, name in sorted(funde):
    print("%s|%s" % (pfad, name))
PY
)

geprueft=$(echo "$ausgabe" | sed -n 's/^GEPRUEFT //p')
liste=$(echo "$ausgabe" | grep -v '^GEPRUEFT ')
anzahl=$(test -z "$liste" && echo 0 || echo "$liste" | wc -l)

echo "$geprueft Dateien geprueft, $anzahl ungedeckte Block-/Gegenstandstypen"

if [ "$anzahl" -eq 0 ]; then
    echo "OK - jeder benutzte Block- und Gegenstandstyp ist gedeckt."
    exit 0
fi

echo
echo "UNGEDECKT -- weder importiert noch im Projekt noch in derselben Datei erklaert:"
echo "$liste" | while IFS='|' read -r pfad name; do
    printf "  %-72s %s\n" "$pfad" "$name"
done
exit 1
