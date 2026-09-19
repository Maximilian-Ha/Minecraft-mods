#!/usr/bin/env bash
#
# Prueft, ob jede benutzte Aufzaehlungskonstante des Projekts auch erklaert ist.
#
# WARUM DAS EXISTIERT
# -------------------
# Beim Uebertragen einer Aufzaehlung aus 1.7.10 faellt leicht ein Wert weg -- die Liste sieht
# vollstaendig aus, und niemand zaehlt nach. Wer den Wert spaeter benutzt, schreibt einen Namen
# auf, den es nicht gibt. syntax-check kann das nicht sehen: es uebersetzt ohne
# Minecraft-Klassenpfad und filtert "cannot find symbol" heraus, weil diese Meldung dabei
# zehntausendfach als Folgefehler entsteht. Gefunden hat es deshalb bisher erst die CI, und das
# kostet jedes Mal einen Durchlauf.
#
# Genau so ist DamageClass.PLASMA durchgerutscht: das Original hat den Wert zwischen ELECTRIC
# und LASER, der Port hatte ihn nicht, und die Plasmafuellung der Granaten hat ihn benutzt.
#
# WIE ES PRUEFT
# -------------
# Erst werden alle Aufzaehlungen unter src/main/java gelesen und ihre Konstanten gesammelt --
# dazu alles, was im Rumpf sonst noch in Grossschreibung erklaert ist (statische Felder etwa),
# damit daraus kein Fehlalarm wird. Dann wird jeder Verweis der Form Name.KONSTANTE gesucht und
# gegen diese Liste gehalten.
#
# Aufzaehlungsnamen, die es mehr als einmal gibt, bleiben aussen vor: welche gemeint ist, liesse
# sich nur raten, und ein Tor, das raet, ist schlimmer als keines.
#
# Exit-Code 0 = jeder Verweis trifft, 1 = mindestens einer nicht.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys, collections

ROOT = 'src/main/java'

dateien = []
for wurzel, _, namen in os.walk(ROOT):
    for name in namen:
        if name.endswith('.java'): dateien.append(os.path.join(wurzel, name))
dateien.sort()

# Name -> (Menge erlaubter Mitglieder, Datei). Mehrfach vergebene Namen fliegen wieder raus.
erklaert = {}
konstantenliste = {}
mehrdeutig = set()

ENUM_KOPF = re.compile(r'\benum\s+([A-Z]\w*)\b')

def konstanten(text, start):
    """Liest die Konstantenliste eines Aufzaehlungsrumpfs ab der oeffnenden Klammer."""
    tiefe = 0
    i = start
    rumpf_start = None

    while i < len(text):
        z = text[i]
        if z == '{':
            tiefe += 1
            if tiefe == 1: rumpf_start = i + 1
        elif z == '}':
            tiefe -= 1
            if tiefe == 0: return text[rumpf_start:i], text[rumpf_start:i]
        elif z == ';' and tiefe == 1:
            # Die Konstantenliste endet am ersten Semikolon auf Rumpfebene.
            return text[rumpf_start:i], text[rumpf_start:]
        i += 1
    return '', ''

def entkommentiert(text):
    text = re.sub(r'//[^\n]*', '', text)
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    return re.sub(r'"(?:\\.|[^"\\])*"', '""', text)

# Wo welcher Typ herkommt -- gebraucht, um Namensgleichheit mit Minecraft-Typen zu erkennen.
paket_von = {}
for pfad in dateien:
    kopf = open(pfad, encoding='utf-8', errors='replace').read(4096)
    m = re.search(r'^\s*package\s+([\w.]+)\s*;', kopf, re.M)
    paket_von[pfad] = m.group(1) if m else ''

for pfad in dateien:
    text = entkommentiert(open(pfad, encoding='utf-8', errors='replace').read())
    for treffer in ENUM_KOPF.finditer(text):
        name = treffer.group(1)
        klammer = text.find('{', treffer.end())
        if klammer < 0: continue

        liste, rest = konstanten(text, klammer)

        # Konstanten: Grossbuchstaben-Bezeichner auf Ebene null der Liste.
        mitglieder = set()
        nur_konstanten = set()
        tiefe = 0
        for stueck in re.finditer(r'[(){}\[\]]|\b[A-Za-z_]\w*\b', liste):
            s = stueck.group(0)
            if s in '({[':
                tiefe += 1
            elif s in ')}]':
                tiefe -= 1
            elif tiefe == 0 and re.fullmatch(r'[A-Z][A-Z0-9_]*', s):
                mitglieder.add(s)
                nur_konstanten.add(s)

        # Alles, was der Rumpf sonst in Grossschreibung erklaert, gilt ebenfalls als bekannt.
        for feld in re.finditer(r'\b([A-Z][A-Z0-9_]*)\s*(?:=|\()', rest):
            mitglieder.add(feld.group(1))

        if name in erklaert and erklaert[name][1] != pfad:
            mehrdeutig.add(name)
        erklaert[name] = (mitglieder, pfad)
        konstantenliste[name] = set(nur_konstanten)

for name in mehrdeutig:
    erklaert.pop(name, None)
    konstantenliste.pop(name, None)

# Verweise pruefen.
VERWEIS = re.compile(r'\b([A-Z]\w*)\s*\.\s*([A-Z][A-Z0-9_]*)\b')

fehlend = []
geprueft = 0

for pfad in dateien:
    text = open(pfad, encoding='utf-8', errors='replace').read()
    # Zeilenkommentare und Zeichenketten raus, damit Prosa keine Fehlalarme macht.
    sauber = re.sub(r'//[^\n]*', '', text)
    sauber = re.sub(r'/\*.*?\*/', '', sauber, flags=re.S)
    sauber = re.sub(r'"(?:\\.|[^"\\])*"', '""', sauber)

    # Sichtbar ist eine Aufzaehlung nur, wenn sie in derselben Datei steht, im selben Paket,
    # oder ausdruecklich eingefuehrt wird. Sonst ist ein gleichnamiger Minecraft-Typ gemeint --
    # SoundType etwa gibt es hier wie dort.
    eingefuehrt = set(re.findall(r'^\s*import\s+(?:static\s+)?([\w.]+)\s*;', sauber, re.M))
    eingefuehrte_namen = {e.rsplit('.', 1)[-1] for e in eingefuehrt}
    wildcards = {e[:-2] for e in eingefuehrt if e.endswith('.*')}

    def sichtbar(typ):
        quelle = erklaert[typ][1]
        if quelle == pfad: return True

        # Fuehrt die Datei einen gleichnamigen Typ ausdruecklich ein, der NICHT unsere
        # Aufzaehlung ist, dann ist dieser gemeint -- SoundType und ConnectionType gibt es
        # sowohl hier als auch in Minecraft bzw. NeoForge. Ein ausdruecklicher Import sticht
        # jeden Sammelimport.
        for e in eingefuehrt:
            if e.rsplit('.', 1)[-1] != typ: continue
            return e.startswith(paket_von.get(quelle, '\x00'))

        if paket_von.get(quelle) == paket_von.get(pfad): return True
        if typ in eingefuehrte_namen: return True
        # Der aeussere Typ kann eingefuehrt sein, die Aufzaehlung steckt darin.
        aussen = os.path.basename(quelle)[:-5]
        if aussen in eingefuehrte_namen: return True
        return paket_von.get(quelle) in wildcards

    for zeile_nr, zeile in enumerate(sauber.split('\n'), 1):
        for treffer in VERWEIS.finditer(zeile):
            typ, mitglied = treffer.group(1), treffer.group(2)
            if typ not in erklaert: continue
            if not sichtbar(typ): continue
            geprueft += 1
            if mitglied not in erklaert[typ][0]:
                fehlend.append((pfad, zeile_nr, typ, mitglied, erklaert[typ][1]))

# ---------------------------------------------------------------------------------------
# Zweitens: erschoepfende Schalter.
#
# Ein switch-AUSDRUCK ueber eine Aufzaehlung muss jeden Wert abdecken, sonst uebersetzt er
# nicht. Kommt ein Wert zur Aufzaehlung dazu, faellt das erst in der CI auf -- genau so beim
# Nachtragen von DamageClass.PLASMA, wo BulletConfig.getDamage einen solchen Ausdruck haelt.
#
# Geprueft werden nur AUSDRUECKE (hinter '=' oder 'return') ohne default-Zweig. Ein
# switch-BEFEHL darf eine Teilmenge behandeln -- ConfettiUtil tut das mit Absicht.
# ---------------------------------------------------------------------------------------

SCHALTER = re.compile(r'(=|return)\s*switch\s*\(([^)]*)\)\s*\{')

luecken = []
schalter_geprueft = 0

for pfad in dateien:
    sauber = entkommentiert(open(pfad, encoding='utf-8', errors='replace').read())

    for treffer in SCHALTER.finditer(sauber):
        # Rumpf des Schalters einlesen.
        i = treffer.end() - 1
        tiefe = 0
        ende = i
        while ende < len(sauber):
            if sauber[ende] == '{': tiefe += 1
            elif sauber[ende] == '}':
                tiefe -= 1
                if tiefe == 0: break
            ende += 1
        rumpf = sauber[i:ende]

        if re.search(r'\bdefault\s*(->|:)', rumpf): continue

        labels = set()
        for fall in re.finditer(r'\bcase\s+([^:>]+?)\s*(?:->|:)', rumpf):
            for name in re.findall(r'\b([A-Z][A-Z0-9_]*)\b', fall.group(1)):
                labels.add(name)
        if not labels: continue

        # Welche Aufzaehlung deckt alle Marken ab? Nur bei genau einer wird geprueft.
        passend = [n for n, (m, _) in erklaert.items() if labels <= m]
        if len(passend) != 1: continue

        name = passend[0]
        fehlt = erklaert[name][0] - labels
        # Nur echte Konstanten zaehlen, keine sonstigen Grossschreib-Mitglieder: als Marke
        # taugt nur, was auch in der Konstantenliste steht.
        fehlt = {f for f in fehlt if f in konstantenliste.get(name, set())}
        schalter_geprueft += 1

        if fehlt:
            zeile = sauber[:treffer.start()].count('\n') + 1
            luecken.append((pfad, zeile, name, sorted(fehlt)))

print('Pruefe Aufzaehlungen ... %d Aufzaehlungen (%d mehrdeutig uebersprungen), %d Verweise, %d erschoepfende Schalter'
      % (len(erklaert), len(mehrdeutig), geprueft, schalter_geprueft))

if luecken:
    print('  SCHALTER MIT LUECKE : %d' % len(luecken))
    print()
    print('ERSCHOEPFENDE SCHALTER, DENEN EIN ZWEIG FEHLT:')
    for pfad, nr, name, fehlt in luecken:
        print('  %s: kein Zweig fuer %s' % (name, ', '.join(fehlt)))
        print('      %s:%d' % (pfad, nr))
    sys.exit(1)

if fehlend:
    print('  FEHLEND : %d' % len(fehlend))
    print()
    print('VERWEISE AUF KONSTANTEN, DIE ES NICHT GIBT:')
    for pfad, nr, typ, mitglied, quelle in fehlend:
        print('  %s.%s' % (typ, mitglied))
        print('      benutzt in %s:%d' % (pfad, nr))
        print('      erklaert in %s' % quelle)
    sys.exit(1)

print('OK - jede benutzte Konstante ist erklaert.')
sys.exit(0)
PYEOF
