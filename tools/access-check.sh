#!/usr/bin/env bash
#
# Prueft, ob eine mit @Override gekennzeichnete Methode dieselbe SICHTBARKEIT hat wie alle
# anderen Stellen im Projekt, die denselben Namen ueberschreiben.
#
# Hintergrund: javac meldet dafuer "attempting to assign weaker access privileges". Ohne
# Minecraft-Klassenpfad kann tools/syntax-check.sh das nicht sehen -- die Oberklasse fehlt.
# In Runde 288 kostete diese Fehlerart einen CI-Lauf: Quackos schrieb mobInteract als
# protected, weil Mob es so deklariert; Animal hebt es aber auf public.
#
# DIESEN EINEN FALL FAENGT DAS TOR NICHT, und das ist gemessen, nicht vermutet: mobInteract
# steht im ganzen Port genau EINMAL. Gegen eine Mehrheit von eins laesst sich nichts pruefen.
# Das ist dieselbe Grenze, die im Kopf von signature-check steht, und sie bleibt bestehen.
#
# WAS ES FAENGT, ist die naechste Stelle dieser Art, an der das Projekt sich selbst
# widerspricht -- und das sind die haeufigen Namen. Gegengeprobt in Runde 289: setzt man das
# render einer einzigen Oberflaeche auf protected, meldet das Tor genau diese Zeile gegen
# 149 andere, und sonst nichts.
#
# DASSELBE VERFAHREN WIE signature-check: die richtige Sichtbarkeit steht vielfach im Projekt
# selbst. Weicht eine EINZIGE Stelle ab, ist sie es, die falsch ist.
#
# DREI BEDINGUNGEN halten die Fehlalarme heraus:
#   * mindestens fuenf Belege fuer den Namen, sonst ist "die Mehrheit" nichts wert;
#   * mindestens neunzig Prozent auf einer Sichtbarkeit;
#   * GLEICHE STELLIGKEIT -- eine andere Anzahl Parameter ist eine echte Ueberladung.
#
# NUR IN EINE RICHTUNG: gemeldet wird, wer ENGER ist als die Mehrheit (protected statt public,
# oder paketprivat statt protected). Weiter zu oeffnen ist erlaubt und kommt im Port vor.
#
# EINE SCHNITTSTELLE HAT KEINE SICHTBARKEITSWOERTER: ihre Methoden sind immer oeffentlich.
# Ohne diese Ausnahme meldete die erste Fassung vier Stellen, die alle richtig waren.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys, collections

JAVA = 'src/main/java'

# Je enger, desto kleiner.
RANK = {'private': 0, '': 1, 'protected': 2, 'public': 3}


def strip(src):
    """Kommentare und Zeichenketten entfernen, damit nichts aus ihnen mitgelesen wird."""
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n':
                i += 1
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i + 1] == '/'):
                if src[i] == '\n':
                    out.append('\n')
                i += 1
            i += 2
        elif c in '"\'':
            quote = c
            i += 1
            while i < n and src[i] != quote:
                if src[i] == '\\':
                    i += 1
                i += 1
            i += 1
            out.append('""')
        else:
            out.append(c)
            i += 1
    return ''.join(out)


METHOD = re.compile(
    r'@Override\s*'
    r'(?:@\w+(?:\([^)]*\))?\s*)*'                        # weitere Anmerkungen
    r'((?:(?:public|protected|private|static|final|synchronized|abstract|default|native|strictfp)\s+)*)'
    r'(?:<[^>]+>\s*)?'                                   # Typparameter
    r'[\w.<>\[\],?\s]+?\s+'                              # Rueckgabetyp
    r'(\w+)\s*\(([^)]*)\)')

# Eine Schnittstelle hat keine Sichtbarkeitswoerter: ihre Methoden sind immer oeffentlich.
INTERFACE = re.compile(r'\b(?:public\s+)?interface\s+\w+')

MODIFIER = re.compile(r'\b(public|protected|private)\b')


def arity(params):
    params = params.strip()
    if not params:
        return 0
    tiefe = 0
    zahl = 1
    for c in params:
        if c in '<([':
            tiefe += 1
        elif c in '>)]':
            tiefe -= 1
        elif c == ',' and tiefe == 0:
            zahl += 1
    return zahl


stellen = collections.defaultdict(list)   # (name, stelligkeit) -> [(sichtbarkeit, datei, zeile)]

for wurzel, _, dateien in os.walk(JAVA):
    for datei in dateien:
        if not datei.endswith('.java'):
            continue
        pfad = os.path.join(wurzel, datei)
        with open(pfad, encoding='utf-8') as f:
            roh = f.read()
        text = strip(roh)

        schnittstelle = INTERFACE.search(text) is not None

        for treffer in METHOD.finditer(text):
            modifikatoren = treffer.group(1) or ''
            name = treffer.group(2)
            sicht = MODIFIER.search(modifikatoren)
            sicht = sicht.group(1) if sicht else ('public' if schnittstelle else '')
            zeile = text[:treffer.start()].count('\n') + 1
            stellen[(name, arity(treffer.group(3)))].append((sicht, pfad, zeile))

funde = []
geprueft = 0

for (name, stelligkeit), liste in sorted(stellen.items()):
    if len(liste) < 5:
        continue
    geprueft += 1

    zaehler = collections.Counter(s for s, _, _ in liste)
    mehrheit, anzahl = zaehler.most_common(1)[0]
    if anzahl / len(liste) < 0.9:
        continue

    for sicht, pfad, zeile in liste:
        if sicht == mehrheit:
            continue
        # Nur was ENGER ist als die Mehrheit, ist ein Uebersetzungsfehler.
        if RANK[sicht] < RANK[mehrheit]:
            funde.append((pfad, zeile, name, sicht or 'paketprivat', mehrheit, anzahl, len(liste)))

print('Pruefe Sichtbarkeit von Ueberschreibungen ... %d Namen mit Mehrheit' % geprueft)
print('  AUFFAELLIG: %d' % len(funde))

for pfad, zeile, name, sicht, mehrheit, anzahl, gesamt in funde:
    print('  %s:%d: %s ist %s, %d von %d anderen Stellen schreiben %s'
          % (pfad, zeile, name, sicht, anzahl, gesamt, mehrheit))

if funde:
    print()
    print('Eine engere Sichtbarkeit als die der Oberklasse ist unzulaessig:')
    print('    error: attempting to assign weaker access privileges')
    sys.exit(1)

print('OK - keine Ueberschreibung ist enger als die Mehrheit.')
PYEOF
