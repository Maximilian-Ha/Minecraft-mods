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
#
# DRITTE PRUEFUNG (Runde 156): das richtige Werkzeug.
#
# requiresCorrectToolForDrops() allein bewirkt in 1.21 nichts Gutes. Ob ein Werkzeug als das
# richtige gilt, entscheiden die Regeln seiner Tool-Komponente, und die haengen alle an einem
# mineable-Tag. Steht ein Block in KEINEM solchen Tag, passt keine Regel, Tool.isCorrectForDrops
# liefert falsch -- und der Block faellt mit keinem Werkzeug, auch nicht mit der Netheritspitzhacke.
# Die Beutetabelle ist dann tote Ladung.
#
# Das ist im Spiel nicht zu uebersehen und trotzdem leicht zu uebersehen beim Anmelden: die
# Eigenschaft steht in NtmBlocks, der Tag in NtmBlockTagProvider.
#
# GEMESSEN (Runde 156): einundvierzig Bloecke waren so angemeldet -- die ganze RBMK-Saeule, die
# halbe Erdoelkette, das Lichtbogenschweissgeraet und die EMP-Bombe, alle mit dropSelf. Nach dem
# Nachtragen der Tag-Eintraege meldet die Pruefung null. Nimmt man einen Eintrag wieder heraus,
# meldet sie genau ihn.

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

braucht_werkzeug = set()
for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\s*=\s*([^;]{0,800}?);', bloecke, re.S):
    if 'requiresCorrectToolForDrops()' in m.group(2): braucht_werkzeug.add(m.group(1))

tags = entkommentieren(open('src/main/java/com/hbm/datagen/NtmBlockTagProvider.java', encoding='utf-8').read())
# nur die mineable-Listen, nicht die uebrigen Tags derselben Datei
abbaubar = set()
for m in re.finditer(r'this\.tag\(BlockTags\.MINEABLE_WITH_\w+\)(.*?);', tags, re.S):
    abbaubar |= set(re.findall(r'NtmBlocks\.([A-Z][A-Z0-9_]*)', m.group(1)))

prov = entkommentieren(open('src/main/java/com/hbm/datagen/NtmBlockLootTableProvider.java', encoding='utf-8').read())
mit_tabelle = set(re.findall(
    r'\b(?:dropSelf|add|dropOther|dropWhenSilkTouch|otherWhenSilkTouch|dropPottedContents)\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)',
    prov))

fehlend = sorted(f for f in namen if f not in mit_tabelle and f not in ohne_tabelle)
ueberzaehlig = sorted(f for f in namen if f in mit_tabelle and f in ohne_tabelle)
unabbaubar = sorted(f for f in namen
                    if f in braucht_werkzeug and f not in ohne_tabelle and f not in abbaubar)

print("Pruefe Beutetabellen ... %d Bloecke, %d Tabellen im Erzeuger, %d mit noLootTable()"
      % (len(namen), len(mit_tabelle & set(namen)), len(ohne_tabelle)))
print("  ohne Tabelle und ohne noLootTable() : %d" % len(fehlend))
print("  Tabelle TROTZ noLootTable()         : %d" % len(ueberzaehlig))
print("  Werkzeug noetig, aber in keinem Tag : %d" % len(unabbaubar))

if not fehlend and not ueberzaehlig and not unabbaubar:
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

if unabbaubar:
    print()
    print("requiresCorrectToolForDrops() OHNE mineable-Tag -- diese Bloecke fallen mit keinem")
    print("Werkzeug, ihre Beutetabelle ist tote Ladung. Eintrag in NtmBlockTagProvider nachtragen:")
    for f in unabbaubar: print("   NtmBlocks.%s (%s)" % (f, namen[f]))

sys.exit(1)
PY
