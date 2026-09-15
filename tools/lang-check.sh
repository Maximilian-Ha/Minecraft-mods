#!/usr/bin/env bash
#
# lang-check.sh -- prueft, dass kein Uebersetzungsschluessel zweimal vergeben wird.
#
# WARUM ES DIESES TOR GIBT: der Sprach-Erzeuger bricht bei einer Dopplung ab:
#
#   IllegalStateException: Duplicate translation key item.hbmsntm.wiring_tool.desc
#
# Gefunden hat das erst der fuenfte runData-Lauf, nach sieben Minuten. Die Dopplung war
# unauffaellig, weil die beiden Zeilen NICHT gleich aussehen: einmal entsteht der Schluessel aus
# dem Gegenstand plus Endung, einmal steht er ausgeschrieben da.
#
#   this.add(NtmItems.WIRING_TOOL, DESC, "...");      -> item.hbmsntm.wiring_tool.desc
#   this.add("item.hbmsntm.wiring_tool.desc", "...");  -> derselbe Schluessel
#
# Das Tor loest daher alle Schreibweisen auf denselben Schluessel auf:
#   add("schluessel", ...)        -- ausgeschrieben, auch zusammengesetzt
#   add(NtmItems.X, ...)          -- item.hbmsntm.<registriername>
#   add(NtmBlocks.X, ...)         -- block.hbmsntm.<registriername>
#   add(Ntm..., ENDUNG, ...)      -- Schluessel + Endung: DESC, P11, ein Literal, getName(Wert)
#                                    oder eine Summe daraus
#   Schleifen ueber Aufzaehlungen -- type.key wird aus der Aufzaehlung selbst aufgeloest, eine
#                                    Zeile vergibt dann so viele Schluessel wie es Werte gibt
#   addDamage / addDamagePlayer   -- death.attack.<kennung>[.player]
#
# Was es nicht aufloesen kann, meldet es als blinde Stelle und faellt durch -- ein Tor, das
# schweigt, wo es nicht hinsieht, ist schlimmer als keines.
#
# Zweitens prueft es die Gegenrichtung: ein registrierter Block oder Gegenstand OHNE
# Namenszeile zeigt im Spiel den rohen Schluessel. Das ist kein Absturz und faellt deshalb
# nirgends auf -- so gefunden wurden 28 Faelle, darunter neun Erze, der ZIRNOX und die beiden
# Funkfackeln. Die Ausnahmen leitet das Tor aus dem Quelltext her: multiName haengt den
# Aufzaehlungswert an, eine Klasse mit eigenem getDescriptionId bestimmt ihren Schluessel selbst.
#
# GEMESSEN: 3353 Schluessel, null Dopplungen, null fehlende Namen, null blinde Stellen. Mit
# einer wieder eingesetzten zweiten Zeile fuer wiring_tool.desc genau ein Fund, mit geloeschter
# Namenszeile fuer ore_tikite genau einer.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, sys, collections, os, glob

def entkommentieren(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

# Registriernamen aller Gegenstaende und Bloecke -- sie werden an mehreren Stellen vergeben,
# etwa in NtmItems, in Hilfsfunktionen und in der Waffenfabrik.
items, blocks = {}, {}
for pfad in glob.glob('src/main/java/**/*.java', recursive=True):
    quelle = entkommentieren(open(pfad, encoding='utf-8').read())
    ziel = blocks if 'NtmBlocks.java' in pfad else items
    for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\s*=\s*[^;]{0,500}?\bregister\w*\(\s*"([^"]+)"', quelle, re.S):
        ziel.setdefault(m.group(1), m.group(2))

quelle = entkommentieren(open('src/main/java/com/hbm/datagen/NtmLanguageProvider.java', encoding='utf-8').read())
# Nur der Rumpf von addTranslations: die Hilfsmethoden darunter rufen add() selbst auf.
schnitt = quelle.find('public void add(DeferredBlock')
rumpf = quelle[:schnitt] if schnitt > 0 else quelle

konstanten = dict(re.findall(r'static final String\s+([A-Z0-9_]+)\s*=\s*"([^"]*)"', quelle))

def argumente(text, start):
    """Argumente eines Aufrufs; start zeigt auf die oeffnende Klammer. Zeichenketten bleiben heil."""
    out, tiefe, cur, i = [], 0, '', start
    while i < len(text):
        c = text[i]
        if c == '"':
            j = i + 1
            while j < len(text) and not (text[j] == '"' and text[j-1] != '\\'): j += 1
            cur += text[i:j+1]; i = j + 1; continue
        if c in '([':
            tiefe += 1
            if tiefe == 1: i += 1; continue
        elif c in ')]':
            tiefe -= 1
            if tiefe == 0:
                out.append(cur.strip()); return out, i
        if tiefe == 1 and c == ',':
            out.append(cur.strip()); cur = ''; i += 1; continue
        cur += c; i += 1
    return out, i

def summanden(ausdruck):
    out, tiefe, cur, i = [], 0, '', 0
    while i < len(ausdruck):
        c = ausdruck[i]
        if c == '"':
            j = i + 1
            while j < len(ausdruck) and not (ausdruck[j] == '"' and ausdruck[j-1] != '\\'): j += 1
            cur += ausdruck[i:j+1]; i = j + 1; continue
        if c in '([': tiefe += 1
        elif c in ')]': tiefe -= 1
        if c == '+' and tiefe == 0:
            out.append(cur.strip()); cur = ''
        else: cur += c
        i += 1
    out.append(cur.strip())
    return out

# Aufzaehlungen, ueber die der Erzeuger in einer Schleife laeuft: dort steht im Schluessel ein
# Feld des Schleifenwertes, etwa type.key. Aufgeloest wird es aus der Aufzaehlung selbst --
# eine Zeile in der Schleife vergibt so viele Schluessel, wie die Aufzaehlung Werte hat.
def enum_feldwerte(typname, feld):
    kurz = typname.split('.')[-1]
    aussen = typname.split('.')[0] if '.' in typname else kurz
    for kandidat in {kurz, aussen}:
        treffer = glob.glob('src/main/java/**/%s.java' % kandidat, recursive=True)
        if not treffer: continue
        src = entkommentieren(open(treffer[0], encoding='utf-8').read())
        m = re.search(r'\benum\s+' + re.escape(kurz) + r'\b[^{]*\{', src)
        if not m: continue
        # Stelle des gesuchten Feldes in der Parameterliste des Konstruktors
        mk = re.search(re.escape(kurz) + r'\s*\(([^)]*)\)\s*\{', src[m.end():])
        if not mk: continue
        params = [x.strip().split()[-1] for x in mk.group(1).split(',') if x.strip()]
        if feld not in params: continue
        stelle = params.index(feld)
        # Konstanten bis zum ersten Semikolon auf oberster Ebene
        i, tiefe, body = m.end(), 1, ''
        while i < len(src) and tiefe > 0:
            c = src[i]
            if c == '{': tiefe += 1
            elif c == '}':
                tiefe -= 1
                if tiefe == 0: break
            if tiefe == 1 and c == ';': break
            body += c; i += 1
        werte = []
        for eintrag in summanden_komma(body):
            me = re.match(r'\s*[A-Z][A-Z0-9_]*\s*\((.*)\)\s*$', eintrag, re.S)
            if not me: continue
            args = summanden_komma(me.group(1))
            if stelle >= len(args): continue
            a = args[stelle].strip()
            if a.startswith('"') and a.endswith('"'): werte.append(a[1:-1])
        if werte: return werte
    return None

def summanden_komma(text):
    out, tiefe, cur, i = [], 0, '', 0
    while i < len(text):
        c = text[i]
        if c == '"':
            j = i + 1
            while j < len(text) and not (text[j] == '"' and text[j-1] != '\\'): j += 1
            cur += text[i:j+1]; i = j + 1; continue
        if c in '({[': tiefe += 1
        elif c in ')}]': tiefe -= 1
        if c == ',' and tiefe == 0:
            out.append(cur); cur = ''
        else: cur += c
        i += 1
    out.append(cur)
    return out

def wert(term):
    """Ein Summand als Zeichenkette -- oder None, wenn er sich nicht aufloesen laesst."""
    term = term.strip()
    if term.startswith('"') and term.endswith('"'): return term[1:-1]
    if term in konstanten: return konstanten[term]
    m = re.match(r'(?:this\.)?getName\(\s*[A-Za-z0-9_.]*?([A-Za-z0-9_]+)\s*\)$', term)
    if m: return '.' + m.group(1).lower()
    m = re.match(r'[A-Za-z0-9_.]*?\b([A-Z][A-Z0-9_]*)\.name\(\)\.toLowerCase\(', term)
    if m: return m.group(1).lower()
    return None

def ausdruckswert(ausdruck):
    teile = []
    for t in summanden(ausdruck):
        w = wert(t)
        if w is None: return None
        teile.append(w)
    return ''.join(teile)

schluessel, blind = [], []

for m in re.finditer(r'this\.(add|addDamage|addDamagePlayer)\s*\(', rumpf):
    name = m.group(1)
    a, _ = argumente(rumpf, m.end() - 1)
    if not a: continue

    if name in ('addDamage', 'addDamagePlayer'):
        w = ausdruckswert(a[0])
        if w is None: blind.append("%s(%s): Kennung nicht aufloesbar" % (name, a[0][:40])); continue
        schluessel.append('death.attack.' + w + ('.player' if name == 'addDamagePlayer' else ''))
        continue

    # Laeuft der Aufruf in einer Schleife ueber eine Aufzaehlung?
    schleife = None
    for ms in re.finditer(r'for\s*\(\s*([A-Za-z0-9_.]+)\s+(\w+)\s*:', rumpf[:m.start()]):
        schleife = ms

    ziel = a[0].strip()
    mm = re.match(r'Ntm(Items|Blocks)\.([A-Z][A-Z0-9_]*)\s*$', ziel)
    if mm:
        tabelle = items if mm.group(1) == 'Items' else blocks
        regname = tabelle.get(mm.group(2))
        if regname is None:
            blind.append("add(%s): Registriername nicht aufloesbar" % ziel[:40]); continue
        basis = ('item.hbmsntm.' if mm.group(1) == 'Items' else 'block.hbmsntm.') + regname
    else:
        w = ausdruckswert(ziel)
        if w is not None:
            basis = w
        elif re.match(r'^[A-Za-z0-9_.]+$', ziel):
            # Fluidtypen und Aehnliches: der Schluessel steht nicht im Quelltext, aber eine
            # doppelte Zeile faellt ueber das Ziel selbst auf.
            basis = 'ziel:' + ziel
        else:
            basis = None
            if schleife is not None:
                mf = re.search(r'\b' + re.escape(schleife.group(2)) + r'\.(\w+)\b', ziel)
                if mf:
                    werte = enum_feldwerte(schleife.group(1), mf.group(1))
                    if werte:
                        vorn = ausdruckswert(summanden(ziel)[0])
                        if vorn is not None:
                            for w in werte: schluessel.append(vorn + w)
                            continue
            blind.append("add(%s): Schluessel nicht aufloesbar" % ziel[:50]); continue

    if len(a) >= 3:
        e = ausdruckswert(a[1])
        if e is None:
            blind.append("add(%s, %s): Endung nicht aufloesbar" % (ziel[:30], a[1][:40])); continue
        basis += e

    schluessel.append(basis)


# ---------------------------------------------------------------- Teil 2: fehlende Namen

# Ein registrierter Block oder Gegenstand ohne Namenszeile zeigt im Spiel den rohen Schluessel
# ("block.hbmsntm.ore_tikite"). Das ist kein Absturz und faellt deshalb nirgends auf -- gefunden
# wurden so 28 Faelle, darunter neun Erze und der ZIRNOX.
#
# Drei Ausnahmen sind KEIN Fund, und sie werden aus dem Quelltext hergeleitet, nicht gepflegt:
# ein EnumMultiItem mit multiName haengt den Aufzaehlungswert an, eine Klasse mit eigenem
# getDescriptionId bestimmt ihren Schluessel selbst.
import os, glob as _glob

dateien = {os.path.basename(p)[:-5]: p for p in _glob.glob('src/main/java/**/*.java', recursive=True)}
genannt_i = set(re.findall(r'add\(\s*NtmItems\.([A-Z][A-Z0-9_]*)\s*[,)]', rumpf))
genannt_b = set(re.findall(r'add\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)\s*[,)]', rumpf))
literale = set(re.findall(r'add\(\s*"((?:item|block)\.hbmsntm\.[a-z0-9_.]+)"', rumpf))

def ohne_namen(quelldatei, praefix, genannt):
    quelle = entkommentieren(open(quelldatei, encoding='utf-8').read())
    offen = []
    for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\s*=\s*([^;]{0,500}?);', quelle, re.S):
        feld, roh = m.group(1), m.group(2)
        rn = re.search(r'\bregister\w*\(\s*"([^"]+)"', roh)
        if not rn: continue
        name = rn.group(1)
        if feld in genannt or (praefix + name) in literale: continue
        kl = re.search(r'new\s+([A-Za-z0-9_]+)\s*\(', roh)
        kl = kl.group(1) if kl else None
        if kl == 'EnumMultiItem' and re.search(r'EnumMultiItem\([^)]*,\s*true\s*,', roh): continue
        if kl and kl in dateien:
            src = open(dateien[kl], encoding='utf-8').read()
            if re.search(r'super\(\s*[^,]+,\s*\w+\.class\s*,\s*true\s*,', src): continue
            if 'getDescriptionId' in src: continue
        offen.append((feld, praefix + name))
    return offen

namenlos = ohne_namen('src/main/java/com/hbm/items/NtmItems.java', 'item.hbmsntm.', genannt_i)
namenlos += ohne_namen('src/main/java/com/hbm/blocks/NtmBlocks.java', 'block.hbmsntm.', genannt_b)

zaehler = collections.Counter(schluessel)
doppelt = sorted(k for k, v in zaehler.items() if v > 1)

print("Pruefe Uebersetzungsschluessel ... %d Zeilen, %d verschiedene Schluessel" % (len(schluessel), len(zaehler)))
print("  doppelt vergeben : %d" % len(doppelt))
print("  ohne Namenszeile : %d" % len(namenlos))
print("  blinde Stellen   : %d" % len(blind))

if not doppelt and not namenlos and not blind:
    print("OK - jeder Schluessel wird genau einmal vergeben.")
    sys.exit(0)

if doppelt:
    print()
    print("DOPPELT VERGEBEN -- runData bricht damit ab:")
    for k in doppelt: print("   %s (%dx)" % (k, zaehler[k]))

if namenlos:
    print()
    print("OHNE NAMENSZEILE -- im Spiel steht dort der rohe Schluessel:")
    for feld, k in namenlos: print("   %s  (%s)" % (k, feld))

if blind:
    print()
    print("BLINDE STELLEN -- hier kann das Tor nicht pruefen:")
    for b in blind: print("   " + b)

sys.exit(1)
PY
