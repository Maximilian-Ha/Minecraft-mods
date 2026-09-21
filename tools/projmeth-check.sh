#!/usr/bin/env bash
#
# Prueft, ob ein Aufruf der Form Projektklasse.methode(...) eine Methode nennt, die es in
# dieser Klasse (und ihren Projekt-Oberklassen) gar nicht gibt.
#
# Hintergrund: javac meldet dafuer "cannot find symbol: method ...". tools/syntax-check.sh
# kann das nicht sehen, denn es filtert genau diese Meldung heraus -- ohne Minecraft-
# Klassenpfad entsteht sie zehntausendfach als Folgefehler. tools/import-check.sh prueft nur,
# ob der TYP erreichbar ist, nicht ob die METHODE existiert. In Runde 289 kostete das einen
# CI-Lauf: MobSpawnSystem rief ContaminationUtil.getRads(player) -- der Name stammt aus dem
# Original von 1.7.10, im Port heisst die Lesestelle HbmLivingAttachments.getRadiation.
#
# DAS MACHT DEN FALL ENTSCHEIDBAR, ganz ohne Klassenpfad: Empfaenger UND Methode liegen beide
# im Port. Was hier fehlt, fehlt wirklich.
#
# DREI BEDINGUNGEN halten die Fehlalarme heraus:
#   * DER EMPFAENGER MUSS EIN PROJEKTTYP SEIN -- in derselben Datei erklaert, im selben Paket,
#     oder aus com.hbm/api.hbm eingefuehrt. Ueber den nackten Namen gemessen schlaegt sonst
#     jede Namensgleichheit mit Minecraft zu (Item.getId, Pair.of): das gab 62 Fehlalarme.
#   * KLASSEN, DIE VON EINER FREMDKLASSE ERBEN, BLEIBEN AUSSEN VOR. Was sie erben, steht in
#     der Bibliothek und liegt hier nicht vor. Das ist die bewusste Luecke dieses Tors.
#   * Die eingebauten Namen der Aufzaehlungen und von Object (values, valueOf, ordinal, name,
#     toString, equals, hashCode, getClass, clone, compareTo) zaehlen immer als vorhanden.
#
# Die Methodenmenge einer Datei wird allen in ihr erklaerten Typen zugeschlagen. Das ist grob,
# aber in die sichere Richtung: es erzeugt keine Fehlalarme, nur Blindheit bei inneren Klassen.
#
# NACHGEMESSEN (Runde 290): 11557 Aufrufstellen geprueft, null Funde; 176 Stellen bleiben
# wegen der Fremdvererbung aussen vor (1338 der 2680 Typen erben von ausserhalb). Setzt man
# den Fehler aus Runde 289 wieder ein -- getRads aus ContaminationUtil entfernt --, meldet die
# Pruefung genau MobSpawnSystem.java:119 und sonst nichts.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys, collections

ROOT = 'src/main/java'

files = []
for dp, _, fn in os.walk(ROOT):
    for f in fn:
        if f.endswith('.java'): files.append(os.path.join(dp, f))

ANN = r'(?:@[\w.]+(?:\([^()]*\))?\s*)'
MOD = r'(?:public|protected|private|static|final|abstract|synchronized|native|default|strictfp|\s)'
METHODE = re.compile(r'^\s*' + ANN + r'*' + MOD + r'*(?:' + ANN + r'\s*)*[\w.<>\[\],?\s&]+?\s+([a-z][A-Za-z0-9_]*)\s*\(', re.M)
TYP = re.compile(r'\b(?:class|interface|enum|record)\s+([A-Z][A-Za-z0-9_]*)(?:<[^{]*?>)?(?:\s*\([^)]*\))?\s*(?:extends\s+([A-Za-z0-9_.]+))?')
PAKET = re.compile(r'^package\s+([\w.]+);', re.M)
EINFUHR = re.compile(r'^import\s+(?:static\s+)?([\w.]+);', re.M)
AUFRUF = re.compile(r'(?<![\w.])([A-Z][A-Za-z0-9_]*)\.([a-z][A-Za-z0-9_]*)\s*\(')

EINGEBAUT = {'values', 'valueOf', 'ordinal', 'name', 'toString', 'equals', 'hashCode',
             'getClass', 'clone', 'compareTo'}

quelle = {}; paket = {}; einfuhr = {}; typen_der_datei = {}
methoden = collections.defaultdict(set); oberklasse = {}; bekannt = set()
typen_des_pakets = collections.defaultdict(set)

for p in files:
    s = open(p, encoding='utf-8').read()
    quelle[p] = s
    m = PAKET.search(s); pk = m.group(1) if m else ''
    paket[p] = pk
    einfuhr[p] = {i.split('.')[-1]: i for i in EINFUHR.findall(s)}
    ts = set()
    for m in TYP.finditer(s):
        c = m.group(1); ts.add(c); bekannt.add(c); typen_des_pakets[pk].add(c)
        if m.group(2): oberklasse[c] = m.group(2).split('.')[-1].split('<')[0]
    typen_der_datei[p] = ts
    ms = {mm.group(1) for mm in METHODE.finditer(s)}
    for c in ts: methoden[c] |= ms

def erbt_fremd(c, tiefe=0):
    if tiefe > 20: return True
    o = oberklasse.get(c)
    if o is None: return False
    if o not in bekannt: return True
    return erbt_fremd(o, tiefe + 1)

def alle_methoden(c, tiefe=0):
    s = set(methoden.get(c, ()))
    o = oberklasse.get(c)
    if o in bekannt and tiefe < 20: s |= alle_methoden(o, tiefe + 1)
    return s

geprueft = 0
uebersprungen = 0
funde = []

for p, s in quelle.items():
    for m in AUFRUF.finditer(s):
        c, f = m.group(1), m.group(2)
        if f in EINGEBAUT: continue
        voll = einfuhr[p].get(c)
        ist_projekt = (c in typen_der_datei[p]) or (c in typen_des_pakets[paket[p]]) or \
                      (voll is not None and (voll.startswith('com.hbm') or voll.startswith('api.hbm')))
        if not ist_projekt or c not in bekannt: continue
        if erbt_fremd(c):
            uebersprungen += 1
            continue
        geprueft += 1
        if f in alle_methoden(c): continue
        zeile = s[:m.start()].count('\n') + 1
        funde.append((p, zeile, c, f))

print('Pruefe Aufrufe auf Projektklassen ... %d Stellen geprueft, %d wegen Fremdvererbung ausgelassen'
      % (geprueft, uebersprungen))
print('  AUFFAELLIG: %d' % len(funde))
for p, zeile, c, f in sorted(funde):
    print('  %s:%d: %s.%s gibt es in der Klasse nicht' % (p, zeile, c, f))

sys.exit(1 if funde else 0)
PYEOF
