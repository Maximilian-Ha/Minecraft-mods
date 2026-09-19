#!/usr/bin/env bash
# ---------------------------------------------------------------------------------------
# Tor 27: jede in NtmBlockEntityTypes deklarierte Blockentitaets-Art muss anderswo
# im Quelltext benutzt werden.
#
# Warum: eine Registrierung, die niemand abruft, laesst sich nicht bemerken. Sie
# uebersetzt sauber, sie startet sauber, und sie bindet trotzdem eine Blockentitaet
# an einen Block -- notfalls an den falschen. Genau das lag hier vor: "tower_small"
# war ein zweites Mal registriert und dabei an FLUID_DUCT_NEO gebunden statt an
# MACHINE_TOWER_SMALL. Der Kuehlturm lief nur deshalb, weil die richtige
# Registrierung daneben stand.
# ---------------------------------------------------------------------------------------
set -u
cd "$(dirname "$0")/.."

echo -n "Pruefe Blockentitaets-Arten ... "

funde=$(python3 - <<'PY'
import os, re

quelle = 'src/main/java/com/hbm/blockentity/NtmBlockEntityTypes.java'
text = open(quelle, encoding='utf-8').read()

# Deklarationen der Form "...>> NAME = BLOCK_ENTITY_TYPES.register("
namen = re.findall(r'>\s*([A-Z][A-Z0-9_]+)\s*=\s*BLOCK_ENTITY_TYPES\.register\(', text)

# Gegenprobe: jeder register-Aufruf ausser dem Bus-Anschluss muss eine Deklaration sein.
aufrufe = len(re.findall(r'BLOCK_ENTITY_TYPES\.register\(', text))
bus = len(re.findall(r'BLOCK_ENTITY_TYPES\.register\(\s*eventBus\s*\)', text))
nicht_erfasst = aufrufe - bus - len(namen)

rest = []
for root, _, files in os.walk('src/main/java'):
    for f in files:
        if not f.endswith('.java'): continue
        pfad = os.path.join(root, f)
        if pfad == quelle: continue
        rest.append(open(pfad, encoding='utf-8').read())
alles = '\n'.join(rest)

print("ZAHLEN %d %d" % (len(namen), nicht_erfasst))
for n in namen:
    if not re.search(r'\b%s\b' % n, alles):
        print(n)
PY
)

zahlen=$(echo "$funde" | sed -n 's/^ZAHLEN //p')
deklariert=$(echo "$zahlen" | cut -d' ' -f1)
nicht_erfasst=$(echo "$zahlen" | cut -d' ' -f2)
liste=$(echo "$funde" | grep -v '^ZAHLEN ')
anzahl=$(test -z "$liste" && echo 0 || echo "$liste" | wc -l)

echo "$deklariert Arten deklariert, $anzahl ohne Verwendung"

if [ "$nicht_erfasst" -ne 0 ]; then
    echo
    echo "MUSTER GREIFT NICHT -- $nicht_erfasst Registrierung(en) passen nicht auf die"
    echo "erwartete Form. Das Tor waere blind; bitte das Muster nachziehen."
    exit 1
fi

if [ "$anzahl" -eq 0 ]; then
    echo "OK - jede Art wird benutzt."
    exit 0
fi

echo
echo "UNBENUTZTE BLOCKENTITAETS-ARTEN -- toter Code, moeglicherweise an den falschen Block gebunden:"
echo "$liste" | while read -r name; do
    echo "  ${name}"
done
exit 1
