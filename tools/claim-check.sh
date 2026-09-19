#!/usr/bin/env bash
#
# Behauptungs-Gate: findet Kommentare, die etwas fuer FEHLEND erklaeren, das es laengst gibt.
#
# WARUM DAS EXISTIERT
# -------------------
# Alle uebrigen Tore pruefen, ob der Quelltext in sich stimmt. Keines prueft, ob das, was in
# den Kommentaren BEHAUPTET wird, noch wahr ist. Genau dort sitzt aber ein Fehler, der sich
# ueber Runden hinweg aufsummiert: beim Portieren wird notiert "Gegenstand X fehlt dem Port,
# kommt spaeter", spaeter kommt X tatsaechlich dazu -- und der Satz bleibt stehen. Die naechste
# Runde liest ihn, glaubt ihn und portiert die davon abhaengige Mechanik nicht.
#
# NACHGEMESSEN: in einer einzigen Sitzung sind fuenf solcher Saetze aufgefallen, und zwar nur,
# weil jemand zufaellig nachgesehen hat -- Schredderflinte, DetonatableBlock, Protege, C-130
# und die Oellache des Ablasses. Alle fuenf waren beim Schreiben wahr und beim Lesen falsch.
# Dieses Tor hat beim ersten Lauf zwei weitere gefunden, die niemandem aufgefallen waren:
# flame_pony im Turbofan (Nachbrennerstufe 100) und ingot_cft im Kristallisator.
#
# WIE ES MISST
# ------------
# 1. Es sammelt alle Registriernamen, die der Port WIRKLICH vergibt (.register("...")).
# 2. Es zerlegt jeden Kommentar in Saetze und sucht Saetze mit einer Verneinung ("fehlt",
#    "gibt es nicht", "nicht uebernommen", "blockiert", ...).
# 3. Steht in so einem Satz ein Registriername, den es gibt, ist die Behauptung verdaechtig.
#
# DREI FILTER halten die Fehlalarme heraus, und jeder war noetig:
#   * Kommentare ueber REZEPTE und TABELLENEINTRAEGE. "insert_doxium hat auch im Original kein
#     Rezept" sagt nichts ueber den Gegenstand aus -- den gibt es, das Rezept nicht. Gewertet
#     wird der ganze Kommentar, nicht der einzelne Satz: in einer Aufzaehlung steht das Wort
#     "Rezept" oft erst in der Ueberschrift oder zwei Saetze weiter.
#   * Saetze, in denen AUCH ein unbekannter Name steht. "ore_nether_fire -> crystal_phosphorus
#     -- der Block fehlt im Port" meint den Eingang, nicht den Ausgang.
#   * Nur Registriernamen zaehlen. CamelCase-Klassennamen liefen in einem Vorversuch auf sieben
#     Fehlalarme hinaus, weil Kommentare staendig ueber Klassen des ORIGINALS reden.
#
# WAS ES NICHT SIEHT -- und das gehoert gesagt:
#   * Behauptungen ueber Klassen, Mechaniken und Teilsysteme, also alles ohne Registriernamen.
#     Vier der fuenf Fehlurteile oben waren von dieser Art; dieses Tor haette nur das fuenfte
#     gefunden. Es deckt den billigsten Teil des Problems ab, nicht den ganzen.
#   * docs/ROADMAP.md. Dort stehen dieselben Behauptungen in Prosa; das ist eine eigene Runde.
#
# Exit-Code 0 = sauber, 1 = mindestens eine Behauptung, die nicht mehr stimmt.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

SRC = 'src/main/java'

VERNEINUNG = re.compile(
    r'(fehlt|fehlen|fehlend|nicht uebernommen|nicht portiert|noch nicht|blockiert|'
    r'ebenso wenig|ist nicht da|kommt (spaeter|mit ihm|mit ihr)|'
    # "gibt es NICHT", aber auch "gibt es IM PORT nicht" und "kennt der Port NICHT".
    # Die erste Fassung verlangte die Woerter unmittelbar hintereinander und hat genau
    # deshalb bei der Gegenprobe zwei von drei historischen Saetzen nicht gesehen.
    # "gibt ES nicht", aber auch "ES gibt ... nicht" -- die Wortstellung dreht sich, das
    # Verb bleibt. Die erste Fassung verlangte "gibt es" als Paar und sah die zweite nicht.
    r'(gibt|kennt|hat|existiert|steht)\b[^.;!?]{0,40}?\bnicht\b)', re.I)

# Saetze ueber Rezepte und Tabelleneintraege reden nicht ueber den Gegenstand.
UEBER_REZEPT = re.compile(r'(rezept|eintrag|eintraege|originalzahl|zahlen|tabelle)', re.I)

NAME = re.compile(r'\b[a-z][a-z0-9]*(?:_[a-z0-9]+)+\b')

# Woerter, die wie ein Registriername aussehen, aber keiner sind.
KEIN_NAME = re.compile(r'^(class_|set_|get_|is_|to_)')

def dateien():
    for root, _dirs, fs in os.walk(SRC):
        for f in sorted(fs):
            if f.endswith('.java'):
                yield os.path.join(root, f)

# ---- 1. Was der Port wirklich registriert -------------------------------------------------
REG = set()
for p in dateien():
    s = open(p, encoding='utf-8', errors='replace').read()
    # OHNE fuehrenden Punkt: Bloecke laufen ueber den blossen Helfer register("name", ...).
    # Die erste Fassung verlangte den Punkt und war damit fuer den GESAMTEN Blockbestand
    # blind -- also genau dort, wo der Satz ueber oil_spill stand, der dieses Tor ausgeloest
    # hat. Aufgefallen ist das erst bei der Gegenprobe.
    for m in re.finditer(r'(?<![\w])register\(\s*"([a-z0-9_/]+)"', s):
        REG.add(m.group(1))

# ---- 2. Kommentare durchgehen -------------------------------------------------------------
funde = []
geprueft = 0

for p in dateien():
    s = open(p, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'/\*.*?\*/|//[^\n]*', s, re.S):
        text = re.sub(r'^\s*[*/]+', ' ', m.group(0), flags=re.M)
        zeile = s[:m.start()].count('\n') + 1

        # Der GANZE Kommentar entscheidet, ob es um Rezepte geht, nicht nur der einzelne Satz.
        # In NtmRecipeProvider steht "NICHT UEBERNOMMEN: - rbmk_control_reasim" als Listenpunkt;
        # das Wort "Rezept" faellt erst zwei Saetze weiter. Die Bloecke gibt es, ihre Bauplaene
        # nicht -- und die Behauptung ist damit wahr.
        block_ueber_rezept = UEBER_REZEPT.search(text) is not None

        for satz in re.split(r'(?<=[.;!?])\s+|\n\s*\n', text):
            if not VERNEINUNG.search(satz): continue
            geprueft += 1
            if block_ueber_rezept or UEBER_REZEPT.search(satz): continue

            namen = {n for n in NAME.findall(satz) if not KEIN_NAME.match(n)}
            if not namen: continue
            # Steht ein unbekannter Name im Satz, meint die Verneinung wahrscheinlich ihn.
            if any(n not in REG for n in namen): continue

            for n in sorted(namen):
                funde.append((p, zeile, n, ' '.join(satz.split())[:160]))

print('Pruefe Behauptungen ... %d Registriernamen, %d Saetze mit einer Verneinung'
      % (len(REG), geprueft))

if not funde:
    print('OK - keine Behauptung, die von einem vorhandenen Registriernamen widerlegt wird.')
    sys.exit(0)

print('  AUFFAELLIG: %d' % len(funde))
for p, zeile, n, satz in funde:
    print('  %s:%d: "%s" gibt es im Port -- der Satz behauptet das Gegenteil' % (p, zeile, n))
    print('      %s' % satz)
sys.exit(1)
PYEOF
