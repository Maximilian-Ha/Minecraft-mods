#!/usr/bin/env bash
#
# Prueft, dass es jedes Bild gibt, das ein Metadaten-Gegenstand fuer seine Spielarten
# erzeugen laesst.
#
# Hintergrund: EnumMultiItem legt beim Datenerzeugen je Aufzaehlungswert ein eigenes Modell
# an, und dessen layer0 zeigt auf "item/<Registername>.<wert>". Diese Modelle stehen NIRGENDS
# im Baum -- sie entstehen erst beim Lauf. tools/asset-check.sh geht die Modell-JSONs auf der
# Platte durch und sieht sie deshalb nicht; tools/model-check.sh ebenso wenig. Fehlt eines der
# Bilder, zeigt der Gegenstand im Spiel das schwarz-violette Ersatzmuster, und kein Tor sagt
# etwas.
#
# WAS GEPRUEFT WIRD. Aus jeder Klasse mit registerItemModel wird der Ausdruck fuer layer0
# gelesen und auf drei Bausteine zurueckgefuehrt: Zeichenketten, modelLocation.getPath() (der
# Registername) und num.name().toLowerCase(...) (der Aufzaehlungswert, klein geschrieben).
# Laesst er sich darauf zurueckfuehren, wird fuer jede Registrierung dieser Klasse ueber alle
# Werte der Aufzaehlung geprueft, ob die PNG-Datei da ist.
#
# WAS NICHT GEPRUEFT WIRD, steht am Ende der Ausgabe MIT NAMEN. Einige Gegenstaende bauen ihren
# Bildnamen aus Feldern oder Rechnungen (rbmk_pellet_overlay_e0 bis _e4, DepletedFuelItem mit
# einem Feld); solche Ausdruecke sind statisch nicht aufzuloesen. Sie werden aufgezaehlt statt
# verschwiegen -- ein Tor, das stillschweigend woanders hinschaut, ist schlimmer als keins.
#
# NACHGEMESSEN (Runde 181): 30 Registrierungen mit eigenem Bild je Wert, 505 Bilder, null
# fehlend. Benennt man eine beliebige davon um, meldet die Pruefung genau sie.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

JAVA = 'src/main/java'
TEX  = 'src/main/resources/assets/hbmsntm/textures'


def strip(src):
    """Kommentare entfernen, Zeichenketten aber BEHALTEN -- hier sind sie die Nutzlast."""
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '"':
            j = i + 1
            while j < n:
                if src[j] == '\\': j += 2; continue
                if src[j] == '"': j += 1; break
                j += 1
            out.append(src[i:j]); i = j; continue
        if c == '/' and i + 1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n': i += 1
            continue
        if c == '/' and i + 1 < n and src[i+1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i+1] == '/'): i += 1
            i += 2; out.append(' '); continue
        out.append(c); i += 1
    return ''.join(out)


quellen = {}
for wurzel, _dirs, namen in os.walk(JAVA):
    for fn in namen:
        if not fn.endswith('.java'): continue
        p = os.path.join(wurzel, fn)
        quellen[p] = strip(open(p, encoding='utf-8', errors='replace').read())


def konstanten(body, name):
    """Die Konstanten einer Aufzaehlung, in Reihenfolge."""
    m = re.search(r'\benum\s+' + re.escape(name) + r'\b[^{]*\{', body)
    if not m: return None
    i, tiefe, n = m.end(), 1, len(body)
    start = i
    while i < n and tiefe > 0:
        if body[i] == '{': tiefe += 1
        elif body[i] == '}': tiefe -= 1
        elif body[i] == ';' and tiefe == 1: break
        i += 1
    werte = []
    for teil in re.split(r',(?![^(]*\))', body[start:i]):
        mm = re.match(r'\s*([A-Z][A-Z0-9_]*)\s*(\(|$)', teil)
        if mm: werte.append(mm.group(1))
    return werte or None


def enum_suchen(name, eigene):
    """Erst in der eigenen Datei, dann projektweit -- und nur, wenn eindeutig."""
    k = konstanten(eigene, name)
    if k: return k
    treffer = [konstanten(b, name) for b in quellen.values()]
    treffer = [t for t in treffer if t]
    return treffer[0] if len(treffer) == 1 else None


# --- 1. Bildvorlage je Klasse aus dem layer0-Ausdruck ------------------------------------
# Ergebnis: Klassenname -> Vorlage mit {name} und {wert}, oder None wenn nicht aufloesbar.
AUSDRUCK = re.compile(r'\.texture\(\s*"layer0"\s*,\s*ResourceLocation\.fromNamespaceAndPath\(')


def argument(body, start):
    """Den zweiten Parameter von fromNamespaceAndPath( holen -- klammerweise, nicht bis zur
    ersten schliessenden Klammer. modelLocation.getPath() hat selbst welche."""
    tiefe, i, n = 1, start, len(body)
    komma = None
    while i < n and tiefe > 0:
        c = body[i]
        if c == '(': tiefe += 1
        elif c == ')': tiefe -= 1
        elif c == ',' and tiefe == 1 and komma is None: komma = i
        i += 1
    if komma is None: return None
    return body[komma + 1:i - 1]

def vorlage_aus(ausdruck):
    teile = []
    for stueck in re.split(r'\+', ausdruck):
        stueck = stueck.strip()
        if not stueck: return None
        if stueck.startswith('"') and stueck.endswith('"'):
            teile.append(stueck[1:-1]); continue
        if 'modelLocation.getPath()' in stueck: teile.append('{name}'); continue
        if re.search(r'\bnum\.name\(\)\.toLowerCase', stueck): teile.append('{wert}'); continue
        return None
    return ''.join(teile)


vorlagen = {}       # Klassenname -> Vorlage
ungeklaert = {}     # Klassenname -> Rohausdruck
for p, b in quellen.items():
    if 'registerItemModel' not in b: continue
    m = re.search(r'\bclass\s+(\w+)\b', b)
    if not m: continue
    kls = m.group(1)
    treffer = AUSDRUCK.search(b)
    if not treffer: continue
    ausdruck = argument(b, treffer.end())
    if ausdruck is None: continue
    v = vorlage_aus(ausdruck)
    if v: vorlagen[kls] = v
    else: ungeklaert[kls] = ' '.join(ausdruck.split())


# --- 2. Unterklassen von EnumMultiItem: Aufzaehlung und ob sie eigene Bilder wollen -------
# Der erste Parameter ist nicht immer ein blosser Name -- AmmoContainerItem reicht
# properties.stacksTo(1) durch. Deshalb bis zum X.class alles zulassen, was kein Semikolon
# und keine geschweifte Klammer ist: so bleibt die Suche im selben Konstruktorkopf.
SUPERRUF = re.compile(r'super\s*\([^;{}]*?(\w+)\.class\s*,\s*(true|false)\s*,\s*(true|false)\s*\)', re.S)

unterklassen = {}   # Klassenname -> (Konstanten, multiTexture, Aufzaehlungsname)
stumme = []         # Unterklassen, deren super-Aufruf sich nicht lesen laesst
for p, b in quellen.items():
    m = re.search(r'\bclass\s+(\w+)\s+extends\s+EnumMultiItem\b', b)
    if not m: continue
    s = SUPERRUF.search(b)
    if not s:
        stumme.append('Klasse %s: super-Aufruf nicht lesbar, ihre Bilder bleiben ungeprueft' % m.group(1))
        continue
    unterklassen[m.group(1)] = (enum_suchen(s.group(1), b), s.group(3) == 'true', s.group(1))


# Alle Unterklassen -- auch die, deren super-Aufruf nicht lesbar war. Eine Registrierung
# davon darf nicht stillschweigend durchrutschen.
alle_unterklassen = set()
for p, b in quellen.items():
    m = re.search(r'\bclass\s+(\w+)\s+extends\s+EnumMultiItem\b', b)
    if m: alle_unterklassen.add(m.group(1))

# --- 3. Registrierungen -------------------------------------------------------------------
geprueft, fehlend, uebersprungen = 0, [], list(stumme)
bilder = 0

for p in sorted(quellen):
    roh = re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', open(p, encoding='utf-8', errors='replace').read(), flags=re.S))

    for m in re.finditer(r'register\w*\(\s*"([a-z0-9_.]+)"\s*,\s*\(\)\s*->\s*new\s+(\w+)\s*\(', roh):
        name, kls = m.group(1), m.group(2)

        if kls == 'EnumMultiItem':
            a = re.search(r'(\w+)\.class\s*,\s*(true|false)\s*,\s*(true|false)', roh[m.end():m.end() + 400])
            if not a: continue
            if a.group(3) != 'true': continue
            werte = enum_suchen(a.group(1), quellen[p])
            if werte is None:
                uebersprungen.append('%s: Aufzaehlung %s nicht eindeutig aufloesbar' % (name, a.group(1)))
                continue
            vorlage = vorlagen.get('EnumMultiItem')
        elif kls in unterklassen:
            werte, multiTex, enumName = unterklassen[kls]
            if not multiTex: continue
            if werte is None:
                uebersprungen.append('%s: Aufzaehlung %s nicht eindeutig aufloesbar' % (name, enumName))
                continue
            vorlage = vorlagen.get(kls, vorlagen.get('EnumMultiItem'))
        elif kls in alle_unterklassen:
            uebersprungen.append('%s (%s): Unterklasse von EnumMultiItem, aber nicht auswertbar' % (name, kls))
            continue
        else:
            continue

        if vorlage is None:
            uebersprungen.append('%s (%s): layer0-Ausdruck nicht aufloesbar -- %s'
                                 % (name, kls, ungeklaert.get(kls, 'kein layer0 gefunden')))
            continue

        geprueft += 1
        for wert in werte:
            pfad = os.path.join(TEX, vorlage.format(name=name, wert=wert.lower()) + '.png')
            bilder += 1
            if not os.path.exists(pfad): fehlend.append(pfad)

for kls, ausdruck in sorted(ungeklaert.items()):
    if kls in unterklassen or kls == 'EnumMultiItem': continue
    uebersprungen.append('Klasse %s: layer0-Ausdruck nicht aufloesbar -- %s' % (kls, ausdruck))

print('Pruefe Metadaten-Bilder ... %d Registrierungen mit eigenem Bild je Wert, %d Bilder'
      % (geprueft, bilder))

if uebersprungen:
    print('  NICHT GEPRUEFT (%d) -- ihr Bildname steht nicht im Ausdruck:' % len(uebersprungen))
    for u in uebersprungen: print('    ' + u)

if fehlend:
    print('  FEHLEND: %d' % len(fehlend))
    for f in fehlend: print('  ' + f)
    sys.exit(1)

print('OK - jedes Metadaten-Bild ist da.')
PYEOF
