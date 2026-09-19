#!/usr/bin/env bash
#
# Prueft, ob eine mit @Override gekennzeichnete Methode dieselbe Parameterliste hat wie alle
# anderen Stellen im Projekt, die denselben Namen ueberschreiben.
#
# Hintergrund: javac meldet dafuer "method does not override or implement a method from a
# supertype". Ohne Minecraft-Klassenpfad kann tools/syntax-check.sh das nicht sehen -- die
# Oberklasse fehlt, der Rumpf wird gar nicht erst geprueft. In Runde 181 kostete das einen
# CI-Lauf: LootDecoBlock schrieb getShape mit LevelReader statt BlockGetter, und
# achtundvierzig andere Bloecke des Ports machen es richtig.
#
# GENAU DAS macht den Fall entscheidbar, ganz ohne Klassenpfad: die richtige Signatur steht
# vielfach im Projekt selbst. Weicht eine EINZIGE Stelle ab, ist sie es, die falsch ist.
#
# VIER BEDINGUNGEN halten die Fehlalarme heraus, und jede war noetig:
#   * mindestens fuenf Belege fuer den Namen, sonst ist "die Mehrheit" nichts wert;
#   * mindestens neunzig Prozent auf einer Signatur;
#   * GLEICHE STELLIGKEIT -- eine andere Anzahl Parameter ist eine echte Ueberladung
#     (getMaxStackSize(), getMaxStackSize(ItemStack)), kein Tippfehler;
#   * GENAU EIN abweichender Typ -- zwei und mehr heisst, es ist eine andere Methode.
# Paketpraefixe fallen vor dem Vergleich weg, auch innerhalb spitzer Klammern.
#
# NACHGEMESSEN (Runde 181): ueber den ganzen Baum null Funde. Setzt man LevelReader wieder in
# LootDecoBlock ein, meldet die Pruefung genau diese Zeile -- und sonst nichts.
#
# GRENZE, die bleibt: ein Name, den das Projekt nur EINMAL ueberschreibt, hat keine Mehrheit,
# gegen die er sich messen liesse. Dort hilft nur CI.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys, collections

JAVA = 'src/main/java'


def strip(src):
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c in '"\'':
            q = c; i += 1
            while i < n:
                if src[i] == '\\': i += 2; continue
                if src[i] == q: i += 1; break
                i += 1
            out.append('""'); continue
        if c == '/' and i + 1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n': i += 1
            continue
        if c == '/' and i + 1 < n and src[i+1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i+1] == '/'): i += 1
            i += 2; out.append(' '); continue
        out.append(c); i += 1
    return ''.join(out)


METHODE = re.compile(r'@Override\s*(?:@\w+(?:\([^)]*\))?\s*)*'
                     r'(?:public|protected|private)?\s*(?:static\s+)?(?:final\s+)?'
                     r'[\w.<>\[\],?\s]+?\b(\w+)\s*\(([^)]*)\)')


def parameter(params):
    """Parametertypen, ohne Namen, ohne Paketpraefixe, ohne Leerzeichen."""
    teile, tiefe, akt = [], 0, ''
    for ch in params:
        if ch == '<': tiefe += 1
        elif ch == '>': tiefe -= 1
        if ch == ',' and tiefe == 0:
            teile.append(akt); akt = ''
        else:
            akt += ch
    teile.append(akt)

    typen = []
    for teil in teile:
        teil = teil.strip()
        if not teil: continue
        m = re.match(r'(?:final\s+)?(?:@\w+\s+)*([\w.<>\[\],?\s]+?)\s+\w+$', teil)
        if not m:
            typen.append('?'); continue
        t = re.sub(r'\b[a-z]\w*(?:\.[a-z]\w*)*\.', '', m.group(1).strip())
        typen.append(re.sub(r'\s+', '', t))
    return typen


signaturen = collections.defaultdict(collections.Counter)
stellen = collections.defaultdict(list)
dateien = 0

for wurzel, _dirs, namen in os.walk(JAVA):
    for fn in sorted(namen):
        if not fn.endswith('.java'): continue
        pfad = os.path.join(wurzel, fn)
        dateien += 1
        body = strip(open(pfad, encoding='utf-8', errors='replace').read())
        for m in METHODE.finditer(body):
            key = ','.join(parameter(m.group(2)))
            signaturen[m.group(1)][key] += 1
            stellen[m.group(1)].append((key, pfad, body.count('\n', 0, m.start()) + 1))

probleme = []
geprueft = 0

for name, zaehler in signaturen.items():
    gesamt = sum(zaehler.values())
    if len(zaehler) < 2 or gesamt < 5: continue
    haeufig, anzahl = zaehler.most_common(1)[0]
    if anzahl / gesamt < 0.9: continue

    geprueft += 1
    mehrheit = haeufig.split(',') if haeufig else []

    for key, pfad, zeile in stellen[name]:
        if key == haeufig or zaehler[key] != 1: continue
        eigen = key.split(',') if key else []
        if len(eigen) != len(mehrheit): continue
        anders = [i for i in range(len(eigen)) if eigen[i] != mehrheit[i]]
        if len(anders) != 1: continue
        probleme.append('%s:%d: %s(%s) -- %d andere Stellen schreiben (%s), abweichend ist der '
                        '%d. Parameter' % (pfad, zeile, name, key, anzahl, haeufig, anders[0] + 1))

print('Pruefe @Override-Signaturen ... %d Dateien, %d Methodennamen mit klarer Mehrheit'
      % (dateien, geprueft))

if probleme:
    print('  AUFFAELLIG: %d' % len(probleme))
    for p in probleme: print('  ' + p)
    sys.exit(1)

print('OK - keine ueberschriebene Methode weicht als einzige von der Projektsignatur ab.')
PYEOF
