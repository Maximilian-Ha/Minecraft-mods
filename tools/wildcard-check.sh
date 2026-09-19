#!/usr/bin/env bash
# ---------------------------------------------------------------------------------------
# Tor 25: keine Datei darf gleichzeitig ein Projekt-Paket und ein net.minecraft-Paket
# mit Stern importieren.
#
# Warum: trifft in beiden Paketen derselbe einfache Klassenname zu, bricht der Uebersetzer
# mit "reference to X is ambiguous" ab. Das ist in dieser Portierung zweimal passiert --
# BookItem gegen net.minecraft.world.item.BookItem (Runde 170, hat die CI gekostet) und
# beinahe BarrierBlock gegen net.minecraft.world.level.block.BarrierBlock.
#
# Eine Pruefung auf die Namen selbst waere ehrlicher, ist aber nicht zu messen: dafuer
# braeuchte es die Liste aller Vanilla-Klassennamen, und offline gibt es keinen
# Minecraft-Classpath. Diese Regel geht deshalb an die Ursache statt an das Symptom.
# ---------------------------------------------------------------------------------------
set -u
cd "$(dirname "$0")/.."

echo -n "Pruefe Stern-Importe ... "

funde=$(python3 - <<'PY'
import os, re

treffer = []
dateien = 0

for root, _, files in os.walk('src/main/java'):
    for f in files:
        if not f.endswith('.java'): continue
        dateien += 1
        pfad = os.path.join(root, f)
        text = open(pfad, encoding='utf-8').read()
        sterne = re.findall(r'^import ([\w.]+)\.\*;', text, re.M)
        projekt = [p for p in sterne if p.startswith('com.hbm') or p.startswith('api.hbm')]
        vanilla = [p for p in sterne if p.startswith('net.minecraft')]
        if projekt and vanilla:
            treffer.append((pfad, projekt, vanilla))

print("DATEIEN %d" % dateien)
for pfad, projekt, vanilla in sorted(treffer):
    print("%s | %s | %s" % (pfad, ', '.join(projekt), ', '.join(vanilla)))
PY
)

dateien=$(echo "$funde" | sed -n 's/^DATEIEN //p')
liste=$(echo "$funde" | grep -v '^DATEIEN ')
anzahl=$(test -z "$liste" && echo 0 || echo "$liste" | wc -l)

echo "$dateien Dateien durchgesehen, $anzahl mit gemischten Stern-Importen"

if [ "$anzahl" -eq 0 ]; then
    echo "OK - keine Datei mischt Projekt- und Vanilla-Sternimporte."
    exit 0
fi

echo
echo "GEMISCHTE STERN-IMPORTE -- ein gleichnamiger Typ auf beiden Seiten bricht den Uebersetzer:"
echo "$liste" | while IFS='|' read -r pfad projekt vanilla; do
    echo "  ${pfad}"
    echo "      Projekt: ${projekt}"
    echo "      Vanilla: ${vanilla}"
done
exit 1
