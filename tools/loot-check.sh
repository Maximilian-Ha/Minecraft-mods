#!/usr/bin/env bash
#
# loot-check.sh -- prueft, dass Beutetabelle und Blockeigenschaft zusammenpassen.
#
# WARUM ES DIESES TOR GIBT: BlockLootSubProvider zaehlt am Ende ab. Jeder Block, dessen
# Tabellenschluessel nicht der leere ist, MUSS eine Tabelle bekommen haben:
#
#   IllegalStateException: Missing loottable 'hbmsntm:blocks/x' for 'hbmsntm:x'
#
# Und keine Tabelle darf uebrig bleiben. Traegt ein Block noLootTable(), ist sein Schluessel
# minecraft:empty; legt der Erzeuger ihm trotzdem eine an, bleibt sie liegen:
#
#   IllegalStateException: Created block loot tables for non-blocks: [minecraft:empty]
#
# Genau daran ist der sechste runData-Lauf gescheitert, an der Barrikade: der Block sagt
# noLootTable(), und der Erzeuger legte ihm zusaetzlich eine leere Tabelle an. Beides zusammen
# ist einmal zu viel.
#
# Geprueft wird deshalb beides:
#   1. Jeder Block ohne noLootTable() wird im Beute-Erzeuger genannt.
#   2. Kein Block mit noLootTable() wird dort genannt.
#
# Gezaehlt wird nur die ERSTE Stelle eines Aufrufs -- ein Block, der bloss als Beute eines
# anderen vorkommt, bekommt dadurch keine Tabelle und zaehlt hier auch nicht.
#
# GEMESSEN: 608 Bloecke, 589 Tabellen, 19 mit noLootTable, null Funde. Mit wieder eingesetzter
# Zeile fuer die Barrikade genau ein Fund, mit entfernter Zeile fuer machine_rad_gen genau einer.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, sys

def entkommentieren(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

bloecke = entkommentieren(open('src/main/java/com/hbm/blocks/NtmBlocks.java', encoding='utf-8').read())

namen, ohne_tabelle = {}, set()
for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\s*=\s*([^;]{0,800}?);', bloecke, re.S):
    feld, rumpf = m.group(1), m.group(2)
    rn = re.search(r'\bregister\w*\(\s*"([^"]+)"', rumpf)
    if not rn: continue
    namen[feld] = rn.group(1)
    if 'noLootTable()' in rumpf: ohne_tabelle.add(feld)

prov = entkommentieren(open('src/main/java/com/hbm/datagen/NtmBlockLootTableProvider.java', encoding='utf-8').read())
mit_tabelle = set(re.findall(
    r'\b(?:dropSelf|add|dropOther|dropWhenSilkTouch|otherWhenSilkTouch|dropPottedContents)\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)',
    prov))

fehlend = sorted(f for f in namen if f not in mit_tabelle and f not in ohne_tabelle)
ueberzaehlig = sorted(f for f in namen if f in mit_tabelle and f in ohne_tabelle)

print("Pruefe Beutetabellen ... %d Bloecke, %d Tabellen im Erzeuger, %d mit noLootTable()"
      % (len(namen), len(mit_tabelle & set(namen)), len(ohne_tabelle)))
print("  ohne Tabelle und ohne noLootTable() : %d" % len(fehlend))
print("  Tabelle TROTZ noLootTable()         : %d" % len(ueberzaehlig))

if not fehlend and not ueberzaehlig:
    print("OK - Tabelle und Blockeigenschaft passen ueberall zusammen.")
    sys.exit(0)

if fehlend:
    print()
    print("OHNE TABELLE -- runData bricht damit ab (Missing loottable):")
    for f in fehlend: print("   NtmBlocks.%s (%s)" % (f, namen[f]))

if ueberzaehlig:
    print()
    print("TABELLE TROTZ noLootTable() -- runData bricht damit ab (Created block loot tables")
    print("for non-blocks): entweder die Zeile im Erzeuger streichen oder noLootTable() am Block:")
    for f in ueberzaehlig: print("   NtmBlocks.%s (%s)" % (f, namen[f]))

sys.exit(1)
PY
