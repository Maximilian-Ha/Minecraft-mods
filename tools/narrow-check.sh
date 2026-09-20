#!/usr/bin/env bash
#
# Prueft, ob ein Aufruf ein double uebergibt, wo alle anderen Aufrufstellen desselben
# Empfaengertyps ein float uebergeben.
#
# Hintergrund: javac meldet dafuer "incompatible types: possible lossy conversion from double
# to float". Ohne Minecraft-Klassenpfad sieht tools/syntax-check.sh das nicht -- die Signatur
# der gerufenen Methode steht in der Bibliothek, nicht im Port. In Runde 196 kostete das einen
# CI-Lauf: ModelArmorRPA schrieb Axis.ZP.rotationDegrees(-(System.currentTimeMillis() / 2D %
# 360D)), und tausendneunzig andere Aufrufstellen derselben Methode machen es richtig.
#
# DAS MACHT DEN FALL ENTSCHEIDBAR, ganz ohne Klassenpfad -- dasselbe Prinzip wie in
# tools/signature-check.sh: die richtige Argumentart steht vielfach im Projekt selbst.
#
# VIER BEDINGUNGEN halten die Fehlalarme heraus, und jede war noetig:
#   * DER EMPFAENGERTYP GEHOERT ZUM SCHLUESSEL. Ueber den nackten Namen gemessen kollidieren
#     Vec3.scale(double) und RenderContext.scale(float) -- das gab zwoelf Fehlalarme. Nur
#     Aufrufe, deren Kette mit einem gross geschriebenen Namen beginnt, kommen daher in die
#     Zaehlung; bei "vec.scale(...)" steht der Typ nicht im Text.
#   * mindestens acht Belege fuer float an genau dieser Stelle, sonst ist "die Mehrheit"
#     nichts wert;
#   * hoechstens ein Zehntel Abweichler, sonst ist es keine Ausnahme, sondern eine
#     Ueberladung, die beides nimmt;
#   * NUR ZWEIFELSFREIE AUSDRUECKE. Ein Argument zaehlt als double, wenn auf Klammertiefe null
#     ein Dezimalliteral ohne f-Endung steht und kein Cast im Ausdruck vorkommt; als float,
#     wenn dort ein f-Literal steht und keine double-Zahl. Alles andere zaehlt gar nicht.
#
# GRUPPIERUNGSKLAMMERN SIND KEINE AUFRUFKLAMMERN. -(a / 2D) ist ein double, obwohl die Zahl
# in Klammern steht; foo(2D) ist es nicht. Der erste Entwurf verwechselte beides und fand den
# Fehler aus Runde 196 nicht, den er finden sollte.
#
# NACHGEMESSEN (Runde 197): 38980 Aufrufstellen mit Empfaengertyp, null Funde. Setzt man den
# fehlenden Cast in ModelArmorRPA wieder weg, meldet die Pruefung genau diese Zeile -- und
# sonst nichts.
#
# GRENZE, die bleibt: mehrzeilige Aufrufe bleiben draussen, ebenso jeder Aufruf ohne
# Empfaengertyp im Text und jede Methode mit weniger als acht float-Belegen. Dort hilft nur CI.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys, collections

JAVA = 'src/main/java'


def strip(src):
    """Entfernt Kommentare zeichenweise -- zeichenkettenfest und zeilentreu."""
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '"' or c == "'":
            q = c; out.append(c); i += 1
            while i < n:
                if src[i] == '\\': out.append(' '); out.append(' '); i += 2; continue
                out.append(src[i])
                if src[i] == q: i += 1; break
                i += 1
            continue
        if c == '/' and i + 1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n': out.append(' '); i += 1
            continue
        if c == '/' and i + 1 < n and src[i+1] == '*':
            while i < n and not (src[i] == '*' and i + 1 < n and src[i+1] == '/'):
                out.append('\n' if src[i] == '\n' else ' '); i += 1
            out.append(' '); out.append(' '); i += 2
            continue
        out.append(c); i += 1
    return ''.join(out)


def arg_stellen(s):
    """Zerlegt eine Argumentliste (ohne aeussere Klammern) in die einzelnen Ausdruecke."""
    teile, tiefe, akt, i, n = [], 0, [], 0, len(s)
    while i < n:
        c = s[i]
        if c in '"\'':
            q = c; akt.append(c); i += 1
            while i < n:
                if s[i] == '\\': akt.append(s[i]); akt.append(s[i+1] if i+1 < n else ''); i += 2; continue
                akt.append(s[i])
                if s[i] == q: i += 1; break
                i += 1
            continue
        if c in '([{': tiefe += 1
        elif c in ')]}': tiefe -= 1
        if c == ',' and tiefe == 0:
            teile.append(''.join(akt)); akt = []; i += 1; continue
        akt.append(c); i += 1
    if ''.join(akt).strip() or teile: teile.append(''.join(akt))
    return [t.strip() for t in teile]


# Ein Dezimalliteral ohne f-Endung ist ein double; 2D und 0.5d ebenso.
DOUBLE_LIT = re.compile(r'(?<![\w.])(?:\d+\.\d*|\.\d+|\d+)(?:[eE][+-]?\d+)?[dD](?![\w.])'
                        r'|(?<![\w.])(?:\d+\.\d*|\.\d+)(?:[eE][+-]?\d+)?(?![\w.fFdD])')
FLOAT_LIT = re.compile(r'(?<![\w.])(?:\d+\.\d*|\.\d+|\d+)(?:[eE][+-]?\d+)?[fF](?![\w.])')


def tiefe_null(s):
    """
    Gibt die Zeichen zurueck, die zum Typ des GANZEN Ausdrucks beitragen.

    Eine Klammer hinter einem Namen ist ein Aufruf -- was darin steht, bestimmt den Typ des
    Ergebnisses nicht. Eine Klammer ohne Namen davor gruppiert nur.
    """
    out, stapel, i, n = [], [], 0, len(s)
    while i < n:
        c = s[i]
        if c in '"\'':
            q = c; i += 1
            while i < n:
                if s[i] == '\\': i += 2; continue
                if s[i] == q: i += 1; break
                i += 1
            out.append(' '); continue
        if c in '([':
            vorher = s[:i].rstrip()
            ist_aufruf = bool(vorher) and (vorher[-1].isalnum() or vorher[-1] in '_$)]')
            stapel.append(ist_aufruf)
            out.append(' '); i += 1; continue
        if c in ')]':
            if stapel: stapel.pop()
            out.append(' '); i += 1; continue
        out.append(' ' if any(stapel) else c); i += 1
    return ''.join(out)


def art(ausdruck):
    """DOUBLE, FLOAT oder None -- nur was der Ausdruck ZWEIFELSFREI ist."""
    a = ausdruck.strip()
    if not a or '"' in a or "'" in a: return None
    rumpf = tiefe_null(a)
    hat_d = DOUBLE_LIT.search(rumpf) is not None
    hat_f = FLOAT_LIT.search(rumpf) is not None
    if a.startswith('(float)'):
        return None if hat_d else 'FLOAT'
    if '(float)' in a or '(double)' in a: return None
    if hat_d and not hat_f: return 'DOUBLE'
    if hat_f and not hat_d: return 'FLOAT'
    return None


AUFRUF = re.compile(r'(?<![\w])([A-Za-z_]\w*)\s*\(')
SCHLUESSELWORT = {'if', 'for', 'while', 'switch', 'catch', 'return', 'new', 'synchronized'}

stellen = collections.defaultdict(lambda: collections.defaultdict(list))
dateien = aufrufe = 0

for wurzel, _, namen in os.walk(JAVA):
    for name in sorted(namen):
        if not name.endswith('.java'): continue
        pfad = os.path.join(wurzel, name); dateien += 1
        roh = strip(open(pfad, encoding='utf-8').read())
        for m in AUFRUF.finditer(roh):
            methode = m.group(1)
            if methode in SCHLUESSELWORT: continue

            # Der Empfaenger entscheidet die Ueberladung. Nur wo die Kette mit einem gross
            # geschriebenen Namen beginnt, steht der Typ im Text.
            j = m.start()
            while j > 0 and (roh[j-1].isalnum() or roh[j-1] in '_$.'): j -= 1
            vorlauf = roh[j:m.start()]
            if '.' not in vorlauf: continue
            kopf = vorlauf.split('.')[0]
            if not kopf[:1].isupper(): continue
            schluessel = kopf + '.' + methode

            # zugehoerige schliessende Klammer suchen
            i, tiefe, n = m.end() - 1, 0, len(roh)
            while i < n:
                c = roh[i]
                if c in '"\'':
                    q = c; i += 1
                    while i < n:
                        if roh[i] == '\\': i += 2; continue
                        if roh[i] == q: i += 1; break
                        i += 1
                    continue
                if c == '(': tiefe += 1
                elif c == ')':
                    tiefe -= 1
                    if tiefe == 0: break
                i += 1
            if i >= n: continue
            inner = roh[m.end():i]
            if '\n' in inner: continue          # mehrzeilige Aufrufe bleiben draussen
            aufrufe += 1
            zeile = roh[:m.start()].count('\n') + 1
            for pos, a in enumerate(arg_stellen(inner)):
                k = art(a)
                if k: stellen[schluessel][pos].append((k, pfad, zeile, a))

MINDEST_BELEGE = 8
funde = []
for schluessel, nach_pos in sorted(stellen.items()):
    for pos, liste in sorted(nach_pos.items()):
        fl = [x for x in liste if x[0] == 'FLOAT']
        db = [x for x in liste if x[0] == 'DOUBLE']
        if len(fl) >= MINDEST_BELEGE and db and len(db) * 10 <= len(fl):
            for _, pfad, zeile, a in db:
                funde.append((pfad, zeile, schluessel, pos, a, len(fl), len(db)))

print('Verengungs-Tor: %d Dateien, %d Aufrufstellen mit Empfaengertyp' % (dateien, aufrufe))
for pfad, zeile, schluessel, pos, a, nf, nd in sorted(funde):
    print('%s:%d: %s Argument %d ist ein double, aber %d von %d Aufrufstellen uebergeben ein float'
          % (pfad, zeile, schluessel, pos, nf, nf + nd))
    print('    %s' % a[:100])

if funde:
    print('%d Fund(e).' % len(funde))
    sys.exit(1)
print('Keine Funde.')
PYEOF
