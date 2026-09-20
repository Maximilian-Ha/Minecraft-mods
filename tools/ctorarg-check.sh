#!/usr/bin/env bash
#
# Prueft, ob ein Erzeuger-Aufruf zu irgendeinem Erzeuger der gerufenen Klasse passt -- nicht
# nur in der Stelligkeit, sondern im TYP der Argumente.
#
# Hintergrund: tools/override-check.sh zaehlt seit Runde 193 die Argumente, und das reicht
# nicht. In Runde 197 stimmte die Zahl und der Typ nicht: "new Vec3NT(blick)" mit einem
# Vec3NT in der Hand, und Vec3NT kennt nur (), (x,y,z) und (Vec3) -- ein Vec3NT ist kein
# Vec3. Das kostete einen CI-Lauf, und es war der dritte Typfehler in fuenf Runden, den kein
# Tor sah.
#
# WAS DEN FALL ENTSCHEIDBAR MACHT: die Erzeuger stehen im Projekt, und der Typ des Arguments
# steht in derselben Datei -- als Erklaerung, als Parameter oder im Kopf einer for-Schleife.
# Gemeldet wird nur, wo BEIDES bekannt ist und KEIN Erzeuger passen kann.
#
# VIER SPERREN halten die Fehlalarme heraus, und jede war noetig -- die ersten elf Funde des
# Entwurfs waren allesamt falsch:
#   * TYPVARIABLEN SIND KEINE KLASSEN. "Pair<X, Y>" hat einen Erzeuger (X, Y); X ist kein
#     Typ, gegen den sich etwas pruefen liesse. Die Typvariablen der Klasse zaehlen als
#     unbekannt.
#   * EINE AUFZAEHLUNG ERBT VON Enum. ComparableStack(Item, int, Enum) nimmt jeden
#     Aufzaehlungswert; ohne diese Regel meldete das Tor vier Rezepte.
#   * DIE NAECHSTLIEGENDE ERKLAERUNG GILT. Ein Name wird in einer Datei mehrfach erklaert;
#     massgeblich ist die letzte VOR der Aufrufstelle. Und "var" sagt den Typ nicht -- es
#     wird trotzdem vermerkt, damit es eine frueher stehende Erklaerung verdeckt.
#   * KLAMMERN UND DOPPELPUNKTE WERDEN NICHT MITVERBRAUCHT. Sonst findet in
#     "(Level level, BulletConfig art)" der zweite Parameter kein Komma mehr vor sich, weil
#     der erste es aufgebraucht hat, und "for(ItemStack mod : mods)" faellt ganz heraus.
#
# NUR VOLL BEKANNTE AHNENREIHEN. Wessen Ahnenreihe das Projekt verlaesst -- jeder Block, jeder
# Gegenstand --, faellt heraus: was ueber Item steht, weiss der Port nicht, und ein Erzeuger
# koennte eine Schnittstelle dort oben verlangen. Entscheidbar sind damit vor allem die
# eigenen Wertklassen, und genau dort lag der Fehler.
#
# NACHGEMESSEN (Runde 198): 8949 Erzeuger-Aufrufe, null Funde. Nimmt man den
# Abschreibe-Erzeuger aus Vec3NT wieder heraus, meldet die Pruefung genau die drei Zeilen in
# XFactoryRocket, die ihn brauchen -- und sonst nichts.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys, collections

JAVA = 'src/main/java'

def strip(src):
    out = []; i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c in '"\'':
            q = c; out.append(c); i += 1
            while i < n:
                if src[i] == '\\': out.append(' '); out.append(' '); i += 2; continue
                out.append(src[i])
                if src[i] == q: i += 1; break
                i += 1
            continue
        if c == '/' and i+1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n': out.append(' '); i += 1
            continue
        if c == '/' and i+1 < n and src[i+1] == '*':
            while i < n and not (src[i] == '*' and i+1 < n and src[i+1] == '/'):
                out.append('\n' if src[i] == '\n' else ' '); i += 1
            out.append(' '); out.append(' '); i += 2
            continue
        out.append(c); i += 1
    return ''.join(out)

def args_split(s):
    teile, tiefe, akt, i, n = [], 0, [], 0, len(s)
    while i < n:
        c = s[i]
        if c in '"\'':
            q = c; akt.append(c); i += 1
            while i < n:
                if s[i] == '\\': akt.append(s[i]); i += 2; continue
                akt.append(s[i])
                if s[i] == q: i += 1; break
                i += 1
            continue
        if c in '([{<': tiefe += 1
        elif c in ')]}>': tiefe -= 1
        if c == ',' and tiefe == 0:
            teile.append(''.join(akt)); akt = []; i += 1; continue
        akt.append(c); i += 1
    rest = ''.join(akt)
    if rest.strip() or teile: teile.append(rest)
    return [t.strip() for t in teile]

def param_split(s):
    """Parameterliste einer Erklaerung -- auch <>-fest."""
    return args_split(s)

def basistyp(t):
    """Gibt den nackten Typnamen zurueck: java.util.List<X>[] -> List. None bei Varargs/Array."""
    t = t.strip()
    if '...' in t or '[]' in t: return None
    t = re.sub(r'<.*>', '', t).strip()
    t = t.split('.')[-1]
    if not re.fullmatch(r'[A-Za-z_]\w*', t): return None
    return t

KLASSE = re.compile(r'(?:^|\n)\s*(?:public\s+|final\s+|abstract\s+|static\s+)*'
                    r'(class|interface|enum|record)\s+([A-Za-z_]\w*)([^{]*)\{')

klassen = {}      # Name -> dict(art, extends, implements, ctors=[ [typen] ], datei)
dateien = 0

for wurzel, _, namen in os.walk(JAVA):
    for name in sorted(namen):
        if not name.endswith('.java'): continue
        pfad = os.path.join(wurzel, name); dateien += 1
        roh = strip(open(pfad, encoding='utf-8').read())
        for m in KLASSE.finditer(roh):
            art, kname, kopf = m.group(1), m.group(2), m.group(3)
            ext = re.search(r'\bextends\s+([\w.<>,\s]+?)(?:\bimplements\b|$)', kopf)
            imp = re.search(r'\bimplements\s+([\w.<>,\s]+)$', kopf)
            oben = []
            if ext:
                for t in args_split(ext.group(1)):
                    b = basistyp(t)
                    if b: oben.append(b)
            if imp:
                for t in args_split(imp.group(1)):
                    b = basistyp(t)
                    if b: oben.append(b)
            tv = re.match(r'\s*<([^>]*)>', kopf)
            typvars = set()
            if tv:
                for t in tv.group(1).split(','):
                    t = t.strip().split()[0] if t.strip() else ''
                    if re.fullmatch(r'[A-Za-z_]\w*', t): typvars.add(t)
            klassen.setdefault(kname, {'art': art, 'oben': oben, 'ctors': [], 'datei': pfad, 'typvars': typvars})

# Konstruktoren einsammeln
CTOR = re.compile(r'(?:^|[;{}\n])\s*(?:public|protected|private)\s+([A-Za-z_]\w*)\s*\(([^()]*)\)\s*(?:throws [\w, .]+)?\{')
for wurzel, _, namen in os.walk(JAVA):
    for name in sorted(namen):
        if not name.endswith('.java'): continue
        pfad = os.path.join(wurzel, name)
        roh = strip(open(pfad, encoding='utf-8').read())
        for m in CTOR.finditer(roh):
            kname, params = m.group(1), m.group(2)
            if kname not in klassen: continue
            if klassen[kname]['datei'] != pfad: continue
            liste = []
            for p in param_split(params):
                p = p.strip()
                if not p: continue
                p = re.sub(r'^(?:final\s+|@[\w.]+(?:\([^)]*\))?\s+)+', '', p).strip()
                stuecke = p.rsplit(None, 1)
                bt = basistyp(stuecke[0]) if len(stuecke) == 2 else None
                if bt in klassen[kname]['typvars']: bt = None
                liste.append(bt)
            klassen[kname]['ctors'].append(liste)

def voll_intern(name, tiefe=0):
    """Ist die gesamte Ahnenreihe dieser Klasse im Projekt bekannt?"""
    if tiefe > 10: return False
    k = klassen.get(name)
    if k is None: return False
    for o in k['oben']:
        if o not in klassen: return False
        if not voll_intern(o, tiefe+1): return False
    return True

def ahnen(name, tiefe=0):
    aus = {name}
    if tiefe > 10: return aus
    k = klassen.get(name)
    if k is None: return aus
    if k['art'] == 'enum': aus |= {'Enum', 'Comparable'}
    if k['art'] == 'record': aus |= {'Record'}
    for o in k['oben']:
        aus |= ahnen(o, tiefe+1)
    return aus

NEU = re.compile(r'\bnew\s+([A-Z]\w*)\s*\(')
VAR = re.compile(r'(?<=[;{}(,:])\s*var\s+([a-z_]\w*)\s*(?=[=;,):])')
# Die Klammern und Doppelpunkte werden NICHT mitverbraucht: sonst kann bei
# "(Level level, BulletConfig art, ...)" der zweite Parameter nicht mehr passen,
# weil der erste sein Komma schon aufgebraucht hat. Der Doppelpunkt gehoert dazu,
# damit "for(ItemStack mod : mods)" gelesen wird.
ERKL = re.compile(r'(?<=[;{}(,:])\s*(?:final\s+)?([A-Z]\w*)\s+([a-z_]\w*)\s*(?=[=;,):])')

funde = []
stellen = 0

for wurzel, _, namen in os.walk(JAVA):
    for name in sorted(namen):
        if not name.endswith('.java'): continue
        pfad = os.path.join(wurzel, name)
        roh = strip(open(pfad, encoding='utf-8').read())

        # Erklaerungen der ganzen Datei -- grob, aber es zaehlt nur, was eindeutig ist:
        # ein Name, der in derselben Datei mit ZWEI verschiedenen Typen erklaert wird,
        # faellt ganz heraus.
        erklaerungen = collections.defaultdict(list)
        for m in ERKL.finditer(roh):
            erklaerungen[m.group(2)].append((m.start(), m.group(1)))
        # "var" sagt den Typ nicht. Es wird trotzdem eingetragen -- mit None --, damit es
        # eine frueher im selben Datei stehende, gleichnamige Erklaerung verdeckt.
        for m in VAR.finditer(roh):
            erklaerungen[m.group(1)].append((m.start(), None))
        for v in erklaerungen: erklaerungen[v].sort()

        def typ_bei(name, pos):
            """Der Typ, mit dem dieser Name ZULETZT VOR dieser Stelle erklaert wurde."""
            beste = None
            for start, t in erklaerungen.get(name, ()):
                if start < pos: beste = t
                else: break
            return beste

        for m in NEU.finditer(roh):
            kname = m.group(1)
            k = klassen.get(kname)
            if k is None or not k['ctors']: continue

            i, tiefe, n = m.end()-1, 0, len(roh)
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
            argtexte = args_split(roh[m.end():i])
            argtexte = [a for a in argtexte if a != '']
            stellen += 1

            # Argumenttypen, soweit entscheidbar
            argtypen = []
            for a in argtexte:
                if re.fullmatch(r'[a-z_]\w*', a):
                    t = typ_bei(a, m.start())
                    if t and voll_intern(t): argtypen.append(t); continue
                argtypen.append(None)

            if all(t is None for t in argtypen): continue

            passt = False
            for ct in k['ctors']:
                if len(ct) != len(argtypen): continue
                ok = True
                for pt, at in zip(ct, argtypen):
                    if pt is None or at is None: continue
                    if pt == 'Object': continue
                    if pt not in ahnen(at): ok = False; break
                if ok: passt = True; break

            if not passt:
                zeile = roh[:m.start()].count('\n') + 1
                funde.append((pfad, zeile, kname, argtypen,
                              [', '.join(str(x) for x in c) for c in k['ctors']]))

print("Erzeuger-Tor: %d Dateien, %d Klassen, %d new-Stellen" % (dateien, len(klassen), stellen))
for pfad, zeile, kname, at, ctors in funde:
    print('%s:%d: new %s(%s) -- kein Erzeuger passt. Vorhanden: %s'
          % (pfad, zeile, kname, ', '.join(str(x) for x in at), ' | '.join('(' + c + ')' for c in ctors)))
if funde:
    print('%d Fund(e).' % len(funde))
    sys.exit(1)
print('Keine Funde.')
PYEOF
