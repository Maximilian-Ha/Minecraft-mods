#!/usr/bin/env bash
#
# datagen-check.sh -- prueft die Verdrahtung der Datengeneratoren.
#
# WARUM ES DIESES TOR GIBT: Schadensarten, Biome und Vorkommen stehen nicht in einer
# Registry des Codes, sondern im Datapack, das der DatapackBuiltinEntriesProvider erzeugt.
# Die Nachschlagetabelle aus dem GatherDataEvent kennt sie deshalb NICHT -- nur die des
# Datapack-Erzeugers tut das. Wer einen Tag auf so einen Eintrag setzt und die falsche
# benutzt, bekommt beim Erzeugen:
#
#   IllegalArgumentException: Couldn't define tag minecraft:is_explosion as it is missing
#   following references: hbmsntm:nuclear_blast
#
# Gemessen am neunten runData-Lauf. Der Fehler zeigt sich erst ganz hinten in der
# Datengenerierung -- sieben Minuten pro Versuch.
#
# GEPRUEFT WIRD: jeder Erzeuger, der Eintraege aus dem Datapack nennt, muss in
# NtmDataGenerators die erweiterte Nachschlagetabelle bekommen.
#
# GEMESSEN: ein solcher Erzeuger, richtig verdrahtet, null Funde. Mit der alten Tabelle
# genau ein Fund.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, os, sys, glob

def entkommentieren(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

gen = entkommentieren(open('src/main/java/com/hbm/datagen/NtmDataGenerators.java', encoding='utf-8').read())

# Welche Klassen fuellen das Datapack? builder.add(..., X::bootstrap)
bootstraps = set(re.findall(r'builder\.add\([^,]+,\s*([A-Za-z0-9_]+)::bootstrap', gen))
if not bootstraps:
    print("FEHLER: keine builder.add(...)-Zeilen gefunden -- das Tor sieht nichts."); sys.exit(2)

# Die erweiterte Tabelle: die Variable, der getRegistryProvider() zugewiesen wird.
m = re.search(r'(\w+)\s*=\s*\w+\.getRegistryProvider\(\)', gen)
erweitert = m.group(1) if m else None

findings, geprueft = [], 0
for pfad in sorted(glob.glob('src/main/java/com/hbm/datagen/*.java')):
    name = os.path.basename(pfad)[:-5]
    if name == 'NtmDataGenerators': continue
    quelle = entkommentieren(open(pfad, encoding='utf-8').read())
    benutzt = sorted(b for b in bootstraps if re.search(r'\b' + re.escape(b) + r'\.', quelle))
    if not benutzt: continue
    geprueft += 1

    mc = re.search(r'new\s+' + re.escape(name) + r'\(([^;]*?)\)\s*\)?\s*;', gen)
    if mc is None:
        findings.append((name, benutzt, "wird in NtmDataGenerators nicht erzeugt"))
        continue
    args = mc.group(1)
    if erweitert is None or not re.search(r'\b' + re.escape(erweitert) + r'\b', args):
        findings.append((name, benutzt, "bekommt die Nachschlagetabelle aus dem Ereignis, nicht die des Datapacks"))

print("Pruefe Datengenerator-Verdrahtung ... %d Datapack-Registries, %d Erzeuger nennen deren Eintraege"
      % (len(bootstraps), geprueft))
print("  falsch verdrahtet : %d" % len(findings))

if not findings:
    print("OK - jeder Erzeuger sieht, was er braucht.")
    sys.exit(0)

print()
print("FALSCH VERDRAHTET -- runData bricht damit ab:")
for name, benutzt, grund in findings:
    print("   %s nennt %s und %s" % (name, ", ".join(benutzt), grund))
sys.exit(1)
PY
