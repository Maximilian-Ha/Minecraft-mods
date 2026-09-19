#!/usr/bin/env bash
#
# Prueft, dass jeder angemeldete Block und Gegenstand in einem Kreativreiter steht.
#
# Hintergrund: in diesem Port sind die Reiter handgepflegte Listen in NtmCreativeTabs. Wer
# einen Block anmeldet und die Zeile dort vergisst, bekommt kein Fehlerbild -- der Block ist
# einfach nirgends zu finden. Auch JEI zeigt nur, was in einem Reiter steht; fuer den Spieler
# sieht es aus, als gaebe es die Maschine nicht.
#
# Genau das hat ein Spieler in Runde 154 gemeldet ("ich kann den big ass tank noch nicht in
# JEI finden"). Dort fehlte der ganze Block. Beim Nachzaehlen standen ausserdem
# neunundzwanzig Bloecke und dreiundvierzig Gegenstaende in keinem Reiter, obwohl sie
# vollstaendig portiert waren -- unter anderem saemtliche ZIRNOX-Brennstaebe, die
# Schraubendreher, die Schluessel und ein Dutzend Erze.
#
# DIE REGEL. Jeder Name aus NtmBlocks und NtmItems muss in NtmCreativeTabs in einer Zeile
# vorkommen, die output.accept oder addMetaItems enthaelt. Ein blosses Vorkommen genuegt
# nicht: das Reitersymbol nennt ebenfalls einen Block, und ein Block, den nur das Symbol
# nennt, ist trotzdem nirgends abzuholen.
#
# DIE AUSNAHMEN stehen unten mit Begruendung. Sie sind nicht gegriffen, sondern aus dem
# Original uebernommen: dort tragen genau diese Eintraege setCreativeTab(null) oder gar kein
# setCreativeTab. Kommt ein neuer Eintrag hinzu, muss er entweder in einen Reiter oder mit
# einem Satz hierher -- geraten wird nichts.
#
# BOOK_OF_ und die drei Haemmer sind wie im Original in keinem Reiter. Seit Runde 170 sind
# sie im Ueberleben trotzdem erreichbar: das Buch ueber ein Werkbankrezept, der Diamanthammer
# ueber die Rezepte im Buch (MagicRecipes), Holz- und Bleihammer an der Werkbank.
# Das ist keine Ausnahme im Sinne von "alles in Ordnung", sondern eine offene Aufgabe.
#
# NACHGEMESSEN (Runde 163): 612 Bloecke, 1088 Gegenstaende, null Funde. Nimmt man eine
# beliebige accept-Zeile aus NtmCreativeTabs heraus, meldet die Pruefung genau sie.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import re, sys

def ohne_kommentare(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

def lies(pfad):
    return ohne_kommentare(open(pfad, encoding='utf-8', errors='replace').read())

bloecke_src = lies('src/main/java/com/hbm/blocks/NtmBlocks.java')
items_src   = lies('src/main/java/com/hbm/items/NtmItems.java')
tabs_src    = lies('src/main/java/com/hbm/inventory/NtmCreativeTabs.java')

bloecke = {m.group(1): m.group(2) for m in
           re.finditer(r'DeferredBlock<[^>]*>\s+(\w+)\s*=\s*\w+\(\s*"([a-z0-9_]+)"', bloecke_src)}
items   = {m.group(1): m.group(2) for m in
           re.finditer(r'DeferredItem<[^>]*>\s+(\w+)\s*=\s*\w*\.?\w*register\(\s*"([a-z0-9_.]+)"', items_src)}

# Nur Zeilen, die tatsaechlich etwas in einen Reiter legen.
im_reiter_b, im_reiter_i = set(), set()
for zeile in tabs_src.split('\n'):
    # addGrenadeCombinations legt jede Zusammenstellung der Granate ab; die Zusammenstellung
    # steht in den Zusatzdaten, nicht in einer Ordnungszahl, deshalb ein eigener Helfer.
    if not any(h in zeile for h in ('output.accept', 'addMetaItems', 'addGrenadeCombinations')): continue
    im_reiter_b |= set(re.findall(r'NtmBlocks\.([A-Z][A-Z0-9_]*)', zeile))
    im_reiter_i |= set(re.findall(r'NtmItems\.([A-Z][A-Z0-9_]*)', zeile))

# ---------------------------------------------------------------------------------------
# Absichtlich in keinem Reiter. Der Klammerzusatz ist der Stand des Originals.
# ---------------------------------------------------------------------------------------
VERBORGENE_BLOECKE = {
    'NTM_DIRT':                     'Erde der Bauwerke; sieht aus wie gewoehnliche Erde, heisst so und faellt als solche ab (Original: setCreativeTab(null))',
    'MACHINE_BATTERY':              'veraltet, im Namen als LEGACY gefuehrt (Original: setCreativeTab(null))',
    'MACHINE_BATTERY_POTATO':       'veraltet, im Namen als LEGACY gefuehrt (Original: null)',
    'MACHINE_LITHIUM_BATTERY':      'veraltet, im Namen als LEGACY gefuehrt (Original: null)',
    'MACHINE_SCHRABIDIUM_BATTERY':  'veraltet, im Namen als LEGACY gefuehrt (Original: null)',
    'MACHINE_DINEUTRONIUM_BATTERY': 'veraltet, im Namen als LEGACY gefuehrt (Original: null)',
    'OIL_PIPE':                     'Teil eines Weltbauwerks, nicht zum Setzen gedacht (Original: null)',
    'ORE_BEDROCK':                  'unzerstoerbar, wird vom Tiefbohrer abgebaut (Original: null)',
    'PWR_BLOCK':                    'Huelle des Druckwasserreaktors, wird vom Regler gesetzt (Original: null)',
    'TAINT':                        'breitet sich selbst aus, kein Bauklotz (Original: null)',
    'ZIRNOX_DESTROYED':             'Ruine nach der Kernschmelze, entsteht nur dort (Original: null)',
    'SKELETON_HOLDER':              'Sockel aus den Weltbauwerken, wird nur von der Generierung gesetzt (Original: setCreativeTab(null))',
}

VERBORGENE_ITEMS = {
    'ICF_PELLET':          'liegt im Reiter, aber nur fertig bestueckt -- icfPellet() legt fuenf belegte Kuegelchen ab, ein leeres waere sinnlos (Original: controlTab, dort auch leer)',
    'NOTHING':             'Platzhalter fuer fehlende Bilder (Original: kein setCreativeTab)',
    'FLUID_ICON':          'reines Anzeigebild fuer JEI (Original: null)',
    'TEMPLATE_FOLDER':     'unbenutzter Rest aus 1.7.10 (Original: kein setCreativeTab)',
    'BURNT_BARK':          'Abfall beim Verkohlen, nur als Beute (Original: null)',
    'INGOT_U238M2':        'Zwischenstufe im Zerfall, nur aus der Maschine; die drei weiteren Spielarten sind Ostereier aus der Erde der Bauwerke (Original: setCreativeTab(null) fuer den ganzen Gegenstand)',
    'KEY_RED':             'faellt nur in Weltbauwerken an (Original: null)',
    'MYSTERYSHOVEL':       'liegt nur in der roten Kiste (Original: kein setCreativeTab)',
    'KEY_RED_CRACKED':     'faellt nur in Weltbauwerken an (Original: null)',
    'WASTE_PLATE_MOX':     'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'WASTE_PLATE_PU238BE': 'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'WASTE_PLATE_PU239':   'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'WASTE_PLATE_RA226BE': 'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'WASTE_PLATE_SA326':   'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'WASTE_PLATE_U233':    'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'WASTE_PLATE_U235':    'Abbrandrueckstand, nur aus dem Reaktor (Original: null)',
    'BOOK_OF_':            'Stecker des Zyklotrons; im Original ueber ein verstecktes Bobmazon-Angebot zu haben, nicht aus einem Reiter (Original: setCreativeTab(null))',
    'DIAMOND_GAVEL':       'Stecker des Zyklotrons; im Original nur ueber MagicRecipes herzustellen (Original: kein setCreativeTab)',
    'WOOD_GAVEL':          'Vorstufe des Bleihammers, an der Werkbank zu bauen (Original: kein setCreativeTab)',
    'LEAD_GAVEL':          'Vorstufe des Diamanthammers, an der Werkbank zu bauen (Original: kein setCreativeTab)',
}

fehlend_b = sorted(f for f in bloecke if f not in im_reiter_b and f not in VERBORGENE_BLOECKE)
fehlend_i = sorted(f for f in items   if f not in im_reiter_i and f not in VERBORGENE_ITEMS)

# Eine Ausnahme, die gar nicht mehr noetig ist, ist genauso ein Fehler wie eine fehlende Zeile:
# sonst wandert ein Eintrag in einen Reiter und die Begruendung bleibt als Attrappe stehen.
ueberfluessig  = sorted(f for f in VERBORGENE_BLOECKE if f in im_reiter_b or f not in bloecke)
ueberfluessig += sorted(f for f in VERBORGENE_ITEMS   if f in im_reiter_i or f not in items)

print('Pruefe Kreativreiter ... %d Bloecke, %d Gegenstaende, %d Ausnahmen'
      % (len(bloecke), len(items), len(VERBORGENE_BLOECKE) + len(VERBORGENE_ITEMS)))
print('  Bloecke in keinem Reiter      : %d' % len(fehlend_b))
print('  Gegenstaende in keinem Reiter : %d' % len(fehlend_i))
print('  Ausnahmen ohne Grund          : %d' % len(ueberfluessig))

if not fehlend_b and not fehlend_i and not ueberfluessig:
    print('OK - jeder angemeldete Block und Gegenstand ist im Spiel zu finden.')
    sys.exit(0)

if fehlend_b:
    print()
    print('BLOECKE OHNE REITER -- in JEI und im Kreativbau nicht auffindbar:')
    for f in fehlend_b: print('   NtmBlocks.%s (%s)' % (f, bloecke[f]))

if fehlend_i:
    print()
    print('GEGENSTAENDE OHNE REITER -- in JEI und im Kreativbau nicht auffindbar:')
    for f in fehlend_i: print('   NtmItems.%s (%s)' % (f, items[f]))

if ueberfluessig:
    print()
    print('AUSNAHME OHNE GRUND -- steht jetzt in einem Reiter oder gibt es nicht mehr, die')
    print('Zeile in tools/tab-check.sh gehoert geloescht:')
    for f in ueberfluessig: print('   %s' % f)

sys.exit(1)
PYEOF
