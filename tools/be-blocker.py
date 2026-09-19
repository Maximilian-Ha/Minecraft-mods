#!/usr/bin/env python3
"""Welche der fehlenden Blockentitaeten lassen sich SOFORT portieren?

WARUM: tools/port-gap.py sagt, WIE VIELE fehlen, aber nicht, welche davon an etwas anderem
haengen. Wer die naechste Runde waehlt, will genau das wissen -- eine Blockentitaet, deren
Vorlage einen Mob braucht, den es im Port nicht gibt, kostet eine halbe Stunde Lesen und
endet dann doch in der Schublade.

WIE: fuer jede fehlende Blockentitaet werden die com.hbm-Importe des Originals aufgeloest und
gegen den Port gehalten. Gefunden wird dabei jede Klasse, jede Schnittstelle, jedes Enum und
jeder Record im Port -- auch die verschachtelten, denn Mats.MaterialStack steht in keiner
eigenen Datei.

DIE ABBILDUNG in tools/portmap.py ist der Kern. Der Port hat die Infrastruktur des Originals umbenannt, und
ohne sie meldet das Werkzeug ModItems, ModBlocks und BlockPos als fehlend -- also ausgerechnet
das, was es ueberall gibt. Jede Zeile ist nachgesehen, nicht geraten.

WARUM DIE ZAHL VON port-gap.py ABWEICHT: dieses Werkzeug kennt auch die verschachtelten Typen
des Ports, port-gap.py nur die Dateinamen. Wo eine Sache des Originals im Port als innere
Klasse steht, sieht sie nur dieses Werkzeug. Der Unterschied betraegt derzeit eins.

NICHT ERFASST wird, was ohne Import erreichbar ist: Vanilla-Klassen, Methoden, Felder. Die
Liste der sofort Portierbaren ist deshalb eine ANNAEHERUNG von unten -- wer eine davon
aufgreift, findet im Zweifel doch noch eine Luecke, aber selten.

UND SIE GILT DER BLOCKENTITAET, NICHT DEM BLOCK. Beim ersten Versuch nach dieser Liste ist das
gleich aufgefallen: TileEntityPipeExhaust haengt an nichts, aber der Block darum ist ein
FluidDuctBox -- ein Kastenrohr, das seine Textur aus der Nachbarschaft waehlt, und diese ganze
Familie fehlt im Port noch. Wer einen Eintrag aufgreift, sieht also zuerst nach, welcher Block
die Entitaet traegt.

EINE DRITTE GRUPPE braucht im Port GAR KEINE Blockentitaet, und die zaehlte bisher mit. Auf
1.7.10 gibt es Blockentitaeten, die ausser getRenderBoundingBox und getMaxRenderDistanceSquared
nichts enthalten -- sie existieren nur, damit ein TESR zeichnen darf und nicht weggeschnitten
wird. Auf 1.21 zeichnet dort ein Blockmodell, das weder das eine noch das andere braucht. Das
Werkzeug erkennt sie daran, dass der Rumpf KEIN Feld und ausser diesen Zeichenhilfen keine
Methode enthaelt; Kommentare zaehlen nicht mit (der gelbe Fass-Block hat sein updateEntity
auskommentiert). Ein ganz leerer Rumpf zaehlt ebenso mit -- eine Blockentitaet ohne Feld und
ohne Methode kann nichts anderes sein als ein Aufhaenger. Gemessen in beiden Richtungen: reine
Zeichenhilfe und leerer Rumpf werden erkannt, dieselbe Klasse mit einem updateEntity oder einem
einzigen Feld nicht mehr. Dazu drei namentlich gefuehrte Faelle, die der Port anders loest --
nachgesehen, nicht geraten.

UND EINE VIERTE: Bloecke, die es im Original selbst nicht mehr gibt. ModBlocks fuehrt 59
Felder als @Deprecated und 74 mit setCreativeTab(null). Ist JEDER Block, der eine
Blockentitaet erzeugt, so gekennzeichnet, ist sie keine Luecke -- die FEnSU etwa ist
ausgemustert und durch machine_battery_redd ersetzt, das im Port laengst steht. Die beiden
Stufen werden getrennt gemeldet, denn sie sagen Verschiedenes: @Deprecated heisst
ausgemustert, setCreativeTab(null) allein heisst nur, dass kein Spieler herankommt -- Bauwerke
und andere Bloecke setzen solche Bloecke trotzdem, und die gehoeren nachgesehen statt
abgehakt.

Aufruf:
    tools/be-blocker.py           -- Uebersicht
    tools/be-blocker.py --list    -- dazu die vollstaendige Liste der Blockierten
"""

import os
import re
import subprocess
import sys
from collections import defaultdict

UP = 'hbm-upstream/master'

# Die Abbildungstabellen stehen in tools/portmap.py -- zwei Werkzeuge brauchen sie, und sie
# duerfen nicht auseinanderlaufen.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from portmap import INFRASTRUKTUR, UMBENANNT


def up_dateien():
    aus = subprocess.run(['git', 'ls-tree', '-r', '--name-only', UP], capture_output=True, text=True)
    if aus.returncode != 0:
        print("FEHLER: %s ist nicht erreichbar" % UP)
        sys.exit(2)
    return [z for z in aus.stdout.split('\n') if z.endswith('.java')]


def kern(name):
    for v in ('TileEntity', 'Entity'):
        if name.startswith(v): name = name[len(v):]
    for h in ('BlockEntity', 'Entity'):
        if name.endswith(h): name = name[:-len(h)]
    return name.lower().replace('_', '')


def port_typen():
    """Jeden im Port deklarierten Typ einsammeln -- auch die verschachtelten."""
    typen = set()
    muster = re.compile(r'\b(?:class|interface|enum|record)\s+(\w+)')
    for wurzel, _, dateien in os.walk('src/main/java'):
        for d in dateien:
            if not d.endswith('.java'): continue
            typen.add(d[:-5])
            text = open(os.path.join(wurzel, d), encoding='utf-8', errors='replace').read()
            typen.update(muster.findall(text))
    return typen


# Methoden, die auf 1.7.10 NUR dazu da sind, einem TESR das Zeichnen zu erlauben. Eine
# Blockentitaet, die nichts anderes enthaelt, hat im Port kein Gegenstueck und braucht auch
# keines: dort zeichnet ein Blockmodell, und das braucht weder Zeichengrenze noch Sichtweite.
NUR_ZUM_ZEICHNEN = {
    'getRenderBoundingBox',
    'getMaxRenderDistanceSquared',
    'shouldRenderInPass',
    'getBlockMetadata',
}

# Was der Port anders loest, statt es zu portieren -- nachgesehen, nicht geraten.
OHNE_ENTSPRECHUNG = {
    'TileEntityData':          'zwei Zusatzbits fuers Metadatum; auf 1.21 traegt der Blockzustand beliebig viele',
    'TileEntityDummy':         'Platzhalter eines Mehrblockbaus; im Port macht das DummyableBlock ohne Blockentitaet',
    'TileEntityInventoryBase': 'abstrakte Grundklasse; im Port heisst sie MachineBaseBlockEntity',
}


def rumpf_von(text):
    """Der Klassenrumpf ohne Kommentare und ohne Annotationen."""
    rumpf = text[text.index('{') + 1:text.rindex('}')] if '{' in text and '}' in text else ''
    rumpf = re.sub(r'/\*.*?\*/', '', rumpf, flags=re.S)
    rumpf = re.sub(r'//[^\n]*', '', rumpf)
    return re.sub(r'@\w+(\([^)]*\))?', '', rumpf)


def leerer_rumpf(text):
    return not rumpf_von(text).strip()


def nur_zeichenhilfe(text):
    """Enthaelt diese Blockentitaet NUR Zeichenhilfen und kein einziges Feld?"""

    rumpf = rumpf_von(text)

    # Alles auf der aeussersten Ebene, was mit Semikolon endet, ist ein Feld.
    tiefe = 0
    aussen = []
    for zeichen in rumpf:
        if zeichen == '{': tiefe += 1
        elif zeichen == '}': tiefe -= 1
        elif tiefe == 0: aussen.append(zeichen)
    if ';' in ''.join(aussen): return False

    # Ein leerer Rumpf zaehlt mit: eine Blockentitaet ohne Feld und ohne Methode kann nichts
    # anderes sein als ein Aufhaenger fuer den TESR. Gemessen: genau zwei der fehlenden sind so
    # (machine_uf6_tank und machine_puf6_tank), beide nachgesehen.
    #
    # Ein blosser Konstruktor zaehlt ebenfalls mit -- das Muster unten hat keinen Rueckgabetyp
    # zu fassen und geht daran vorbei. Auf 1.7.10 ist das richtig so: dort hat ein Konstruktor
    # nichts zu tun, was nicht ueber ein Feld liefe, und ein Feld schliesst die Pruefung oben
    # ohnehin aus.
    methoden = re.findall(r'\b(?:public|protected|private)\s+[\w<>\[\], .]+?\s+(\w+)\s*\(', rumpf)
    return all(m in NUR_ZUM_ZEICHNEN for m in methoden)


def stillgelegte_bloecke():
    """Welche Blockentitaeten gehoeren zu Bloecken, die im Original gar nicht mehr im Spiel sind?

    Zwei Stufen, und der Unterschied ist wichtig:
      * @Deprecated an der Felddeklaration -- der Block ist ausgemustert. Wer ihn portiert,
        portiert etwas, das HBM selbst herausgenommen hat.
      * nur setCreativeTab(null) -- kein Spieler kommt an ihn heran. Das heisst NICHT, dass es
        ihn nicht gibt: Bauwerke und andere Bloecke setzen ihn trotzdem.
    """

    mb = subprocess.run(['git', 'show', UP + ':src/main/java/com/hbm/blocks/ModBlocks.java'],
                        capture_output=True, text=True).stdout

    ausgemustert, ohne_reiter, feld_klasse = set(), set(), {}

    for m in re.finditer(r'@Deprecated\s+public\s+static\s+Block\s+([\w,\s]+);', mb):
        for f in m.group(1).split(','):
            ausgemustert.add(f.strip())

    for m in re.finditer(r'^\s*(\w+)\s*=\s*new\s+(\w+)\(.*$', mb, re.M):
        feld_klasse.setdefault(m.group(1), m.group(2))
        if 'setCreativeTab(null)' in m.group(0):
            ohne_reiter.add(m.group(1))

    # Welche Blockklasse erzeugt welche Blockentitaet? Ein einziger Durchlauf ueber alle Bloecke.
    aus = subprocess.run(['git', 'grep', '-n', '-E', r'new\s+TileEntity\w+\s*\(',
                          UP, '--', 'src/main/java/com/hbm/blocks'], capture_output=True, text=True).stdout
    be_von_klasse = defaultdict(set)
    for zeile in aus.split('\n'):
        teile = zeile.split(':', 3)
        if len(teile) < 4: continue
        klasse = os.path.basename(teile[1])[:-5]
        be_von_klasse[klasse].update(re.findall(r'new\s+(TileEntity\w+)\s*\(', teile[3]))

    urteil = {}
    for feld, klasse in feld_klasse.items():
        for be in be_von_klasse.get(klasse, ()):
            urteil.setdefault(be, []).append(feld)

    ausgemustert_be, ohne_reiter_be = {}, {}
    for be, felder in urteil.items():
        if all(f in ausgemustert for f in felder):
            ausgemustert_be[be] = 'im Original ausgemustert (@Deprecated): ' + ", ".join(sorted(felder))
        elif all(f in ausgemustert or f in ohne_reiter for f in felder):
            ohne_reiter_be[be] = 'kein Kreativreiter: ' + ", ".join(sorted(felder))
    return ausgemustert_be, ohne_reiter_be


def main():
    up = up_dateien()
    be_up = [p for p in up if '/com/hbm/tileentity/' in p and os.path.basename(p).startswith('TileEntity')]

    typen = port_typen()
    kerne = {kern(t) for t in typen}

    def vorhanden(name):
        if name in typen or name in INFRASTRUKTUR: return True
        if name in UMBENANNT and UMBENANNT[name] in typen: return True
        k = kern(name)
        return k in kerne or ('machine' + k) in kerne or k.replace('machine', '') in kerne

    frei, blockiert, unnoetig, nachsehen = [], {}, {}, {}
    stillgelegt, ohne_reiter = stillgelegte_bloecke()

    for p in be_up:
        name = os.path.basename(p)[:-5]
        if vorhanden(name): continue

        text = subprocess.run(['git', 'show', UP + ':' + p], capture_output=True, text=True).stdout

        if name in OHNE_ENTSPRECHUNG:
            unnoetig[name] = OHNE_ENTSPRECHUNG[name]
            continue

        if name in stillgelegt:
            unnoetig[name] = stillgelegt[name]
            continue

        if name in ohne_reiter:
            nachsehen[name] = ohne_reiter[name]
            continue

        if nur_zeichenhilfe(text):
            unnoetig[name] = 'leerer Rumpf, nur Aufhaenger fuer den TESR' if leerer_rumpf(text) \
                else 'nur Zeichengrenze und Sichtweite fuer den TESR'
            continue

        importe = re.findall(r'^import (?:static )?(com\.hbm\.[\w.]+)\.(\w+);', text, re.M)
        fehlt = sorted({k for _, k in importe if not vorhanden(k)})

        if fehlt: blockiert[name] = fehlt
        else: frei.append(name)

    print("Fehlende Blockentitaeten und was sie aufhaelt")
    print()
    print("  fehlend gesamt          : %d" % (len(frei) + len(blockiert) + len(unnoetig) + len(nachsehen)))
    print("  gar keine Luecke        : %d" % len(unnoetig))
    print("  erst nachsehen          : %d" % len(nachsehen))
    print("  ohne fehlende Vorlage   : %d" % len(frei))
    print("  mit fehlender Vorlage   : %d" % len(blockiert))
    print()
    print("GAR KEINE LUECKE -- entweder braucht der Port keine Blockentitaet, oder es gibt den")
    print("Block im Original selbst nicht mehr:")
    for n in sorted(unnoetig): print("   %-30s %s" % (n, unnoetig[n]))
    print()
    print("ERST NACHSEHEN -- der Block steht im Original in keinem Kreativreiter. Das heisst")
    print("nicht, dass es ihn nicht gibt: Bauwerke und andere Bloecke setzen ihn trotzdem.")
    for n in sorted(nachsehen): print("   %-30s %s" % (n, nachsehen[n]))
    print()
    print("SOFORT PORTIERBAR -- kein com.hbm-Import fehlt:")
    for n in sorted(frei): print("   " + n)

    zaehler = defaultdict(list)
    for name, fehlt in blockiert.items():
        for k in fehlt: zaehler[k].append(name)

    print()
    print("WORAN DIE UEBRIGEN HAENGEN -- die zwanzig haeufigsten:")
    for k, wer in sorted(zaehler.items(), key=lambda x: -len(x[1]))[:20]:
        print("   %3d x  %s" % (len(wer), k))

    if '--list' in sys.argv:
        print()
        print("DIE BLOCKIERTEN EINZELN:")
        for name in sorted(blockiert):
            print("   %-38s %s" % (name, ", ".join(blockiert[name])))


main()
