#!/usr/bin/env bash
#
# Prueft Vererbung INNERHALB des Projekts, ohne Minecraft-Klassenpfad.
#
# Hintergrund: tools/syntax-check.sh ist fuer diese Fehlerklasse blind. Die javac-Meldungen
# "is not abstract and does not override abstract method", "does not override or implement a
# method from a supertype" und "incompatible types" entstehen zu Tausenden als Folgefehler der
# fehlenden Minecraft-API und werden dort weggefiltert -- echte Treffer gehen darin unter.
# In Runde 5 kostete genau das einen CI-Fehlschlag: zwei Portier-Agenten hatten dieselbe
# Basisklasse unterschiedlich gebaut, und die Ableitungen passten nicht zu der Fassung,
# die am Ende im Baum landete.
#
# Dieser Check arbeitet rein textuell auf den Projektquellen und meldet nur Faelle, in denen
# BEIDE Seiten Projektklassen sind:
#   1. abstrakte Methode der Oberklasse ohne Implementierung in der konkreten Ableitung
#   2. gleichnamige Methode mit abweichendem Rueckgabetyp
#
# Ein zweiter Durchgang (ganz unten) macht dasselbe fuer Schnittstellen: jede konkrete
# Projektklasse muss jede abstrakte Methode ihrer Projekt-Schnittstellen bedienen.
# Runde 56 zeigte, warum das noetig ist: PWRBlockEntity erbte ueber
# IFluidStandardTransceiverMK2 die Methode ILoadedBE.isLoaded() und implementierte sie nicht.
# syntax-check.sh sah davon nichts -- ohne Minecraft-Klassenpfad ist die Oberklasse
# BlockEntity unbekannt, und javac prueft die Vollstaendigkeit einer Klasse mit unbekannter
# Oberklasse gar nicht erst. Der Fehler fiel erst im CI auf.
#
# Absichtliche Grenzen: keine Generika-Aufloesung, keine Ueberladungen nach Parametertypen.
# Lieber wenige, sichere Treffer als eine Flut von Falschmeldungen.

set -uo pipefail
cd "$(dirname "$0")/.."

rc=0

python3 - <<'PYEOF' || rc=1
import os, re, sys

ROOT = 'src/main/java'

# Klassenkopf: Modifikatoren, Name, optionales extends
CLASS_RE = re.compile(
    r'^\s*(?P<mods>(?:public|final|abstract|\s)*)class\s+(?P<name>\w+)(?:<[^>]*>)?\s*'
    r'(?:extends\s+(?P<super>[\w.]+)(?:<[^>]*>)?)?',
    re.M)

# Methodenkopf. Rueckgabetyp wird mitsamt [] und Generika erfasst.
#
# Runde 103: Annotationen stehen JETZT AUCH ZWISCHEN Modifikator und Rueckgabetyp. Die Form
# "public @Nullable AbstractContainerMenu createMenu(...)" ist im Port ueblich, und bis hierher
# hat dieses Muster sie gar nicht erkannt -- die Methode galt als nicht erklaert. Aufgefallen
# ist es, als die neue Schnittstellenpruefung unten dreizehn Klassen anmahnte, die sie sehr wohl
# haben.
METHOD_RE = re.compile(
    r'^[ \t]*(?:@\w+(?:\([^)]*\))?[ \t]*)*'                      # Annotationen, auch einzeilig
    r'(?P<mods>(?:public|protected|private|static|final|abstract|synchronized|native|default|\s)+)'
    r'(?:@\w+(?:\([^)]*\))?[ \t]*)*'                              # ... und vor dem Rueckgabetyp
    r'(?P<ret>[\w.$]+(?:<[^;{]*?>)?(?:\[\])*)\s+'
    r'(?P<name>\w+)\s*\((?P<params>[^)]*)\)',
    re.M)

classes = {}   # Name -> dict

def strip_comments(src):
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    src = re.sub(r'//[^\n]*', '', src)
    return src

for dirpath, _, files in os.walk(ROOT):
    for fn in files:
        if not fn.endswith('.java'): continue
        path = os.path.join(dirpath, fn)
        raw = open(path, encoding='utf-8', errors='replace').read()
        src = strip_comments(raw)
        m = CLASS_RE.search(src)
        if not m: continue
        name = m.group('name')
        if name != fn[:-5]: continue          # nur die Hauptklasse der Datei
        sup = m.group('super')
        if sup: sup = sup.split('.')[-1]
        methods = {}
        for mm in METHOD_RE.finditer(src):
            mods = mm.group('mods')
            ret = mm.group('ret')
            mname = mm.group('name')
            if ret in ('return', 'new', 'class', 'else'): continue
            # Ueberladungen nach Parameterzahl trennen, sonst vergleicht man Aepfel mit Birnen.
            params = mm.group('params').strip()
            arity = 0 if not params else params.count(',') + 1
            static = 'static' in mods
            vis = 3 if 'public' in mods else 2 if 'protected' in mods else 0 if 'private' in mods else 1
            methods.setdefault((mname, arity), []).append((ret.strip(), 'abstract' in mods, static, vis))
        classes[name] = {
            'path': path,
            'super': sup,
            'abstract': 'abstract' in m.group('mods'),
            'methods': methods,
        }

def chain(name):
    """Oberklassen, solange sie Projektklassen sind."""
    out, seen = [], set()
    cur = classes.get(name, {}).get('super')
    while cur and cur in classes and cur not in seen:
        seen.add(cur)
        out.append(cur)
        cur = classes[cur].get('super')
    return out

problems = []

for name, info in sorted(classes.items()):
    sups = chain(name)
    if not sups: continue

    # 1) abstrakte Methoden der Projekt-Oberklassen muessen implementiert sein
    if not info['abstract']:
        required = {}
        for sup in sups:
            for key, sigs in classes[sup]['methods'].items():
                for ret, is_abs, _, _ in sigs:
                    if is_abs: required.setdefault(key, (ret, sup))
        for key, (ret, sup) in required.items():
            implemented = any(key in classes[c]['methods'] and
                              any(not a for _, a, _, _ in classes[c]['methods'][key])
                              for c in [name] + sups)
            if not implemented:
                problems.append('%s: abstrakte Methode %s() aus %s ist nicht implementiert'
                                % (info['path'], key[0], sup))

    # 2) Rueckgabetyp muss zur Oberklasse passen.
    #    Kovariante Rueckgabetypen sind in Java erlaubt, solange der eigene Typ eine
    #    Projekt-Unterklasse des deklarierten ist -- solche Faelle bleiben unbeanstandet.
    for key, sigs in info['methods'].items():
        own = [(r, st) for r, _, st, _ in sigs]
        for sup in sups:
            if key not in classes[sup]['methods']: continue
            sup_sigs = classes[sup]['methods'][key]
            sup_rets = {r for r, _, st, _ in sup_sigs if not st}
            own_rets = {r for r, st in own if not st}
            if len(sup_rets) != 1 or len(own_rets) != 1: break
            if own_rets & sup_rets: break
            own_ret, sup_ret = next(iter(own_rets)), next(iter(sup_rets))
            base_own = own_ret.split('<')[0].rstrip('[]')
            base_sup = sup_ret.split('<')[0].rstrip('[]')
            # kovariant: eigener Rueckgabetyp erbt (ueber Projektklassen) vom deklarierten
            if base_own in classes and base_sup in chain(base_own): break
            # Generika-Schranken koennen wir nicht aufloesen -- gleicher Rohtyp genuegt
            if base_own == base_sup: break
            # Typvariablen (T, E, K1 ...) sind ohne Typsystem nicht aufloesbar
            if re.fullmatch(r'[A-Z]\d?', base_sup) or re.fullmatch(r'[A-Z]\d?', base_own): break
            problems.append('%s: %s() liefert %s, %s deklariert %s'
                            % (info['path'], key[0], own_ret, sup, sup_ret))
            break

    # 3) Eine Ueberschreibung darf den Zugriff nicht verengen. Java verbietet das, javac
    #    meldet "attempting to assign weaker access privileges". In Runde 19 kostete genau
    #    das einen CI-Durchlauf: MachineCrucibleBlock.onRemove war protected, in
    #    DummyableBlock ist die Methode public.
    VIS_NAME = {0: 'private', 1: 'paketweit', 2: 'protected', 3: 'public'}
    for key, sigs in info['methods'].items():
        own_vis = [v for _, _, st, v in sigs if not st]
        if not own_vis: continue
        for sup in sups:
            if key not in classes[sup]['methods']: continue
            sup_vis = [v for _, _, st, v in classes[sup]['methods'][key] if not st]
            if not sup_vis: break
            if min(own_vis) < max(sup_vis):
                problems.append('%s: %s() ist %s, in %s aber %s -- eine Ueberschreibung darf den Zugriff nicht verengen'
                                % (info['path'], key[0], VIS_NAME[min(own_vis)], sup, VIS_NAME[max(sup_vis)]))
            break

# 4) Kollisionen mit Methoden der Minecraft-Oberklassen.
#    Das Skript kennt nur Projektklassen; wo eine Kette bei einer Minecraft-Klasse endet, kann
#    es nichts mehr vergleichen. Genau dort ist aber ein Fehler leicht gemacht: die Vorlage aus
#    1.7.10 benutzt einen Methodennamen, den es auf 1.21 in der Oberklasse laengst gibt, und der
#    Build faellt mit "cannot override" um. Deshalb hier eine kleine Liste der Namen, die in den
#    Basisklassen belegt sind, die dieser Port am haeufigsten beerbt.
#    Gemeldet wird nur, wenn der Rueckgabetyp NICHT passt -- eine echte Ueberschreibung ist
#    voellig in Ordnung.
MC_RESERVED = {
    'Entity':      {'getType': 'EntityType', 'getName': 'Component', 'getDisplayName': 'Component',
                    'getBoundingBox': 'AABB', 'getDimensions': 'EntityDimensions'},
    'BlockEntity': {'getType': 'BlockEntityType', 'getLevel': 'Level', 'getBlockPos': 'BlockPos',
                    'getBlockState': 'BlockState'},
    'Item':        {'getName': 'Component', 'getDescriptionId': 'String'},
    'Block':       {'getName': 'MutableComponent', 'getDescriptionId': 'String', 'defaultBlockState': 'BlockState'},
}

for name, info in classes.items():
    # Wurzel der Vererbungskette bestimmen; endet sie bei einer unbekannten (= Minecraft-)Klasse,
    # nehmen wir deren Namen als Anhaltspunkt.
    root, seen = name, set()
    while root in classes and classes[root]['super'] and root not in seen:
        seen.add(root)
        root = classes[root]['super'].split('.')[-1].split('<')[0]
    if root in classes: continue

    reserved = MC_RESERVED.get(root)
    if not reserved: continue

    for key, sigs in info['methods'].items():
        expected = reserved.get(key[0])
        if expected is None or key[1] != 0: continue
        for ret, _, st, _ in sigs:
            if st: continue
            base = ret.split('<')[0].rstrip('[]')
            if base != expected:
                problems.append('%s: %s() liefert %s, in der Minecraft-Oberklasse %s ist der Name mit %s belegt'
                                % (info['path'], key[0], ret, root, expected))

print('Pruefe Vererbung innerhalb des Projekts ... %d Klassen' % len(classes))
if problems:
    print('  AUFFAELLIG: %d' % len(problems))
    for p in problems: print('  ' + p)
    sys.exit(1)
print('OK - keine Abweichungen zwischen Projektklassen und ihren Oberklassen.')
PYEOF

# ---------------------------------------------------------------------------
# Zweiter Durchgang: Schnittstellen.
#
# Gemeldet wird, wenn eine konkrete Projektklasse eine abstrakte Methode einer
# Projekt-Schnittstelle nirgends bedient -- weder selbst, noch in einer Projekt-Oberklasse,
# noch als default-Methode einer anderen Schnittstelle derselben Hierarchie.
#
# Anders als der erste Durchgang loest dieser Namen ueber Paket und import-Zeilen auf.
# Das ist noetig, weil es denselben einfachen Namen zweimal gibt (IParticleCreator liegt
# sowohl in com.hbm.util.particle als auch in com.hbm.particle.helper); ohne Aufloesung
# haette der Check die beiden vermischt und drei Falschmeldungen erzeugt.
python3 - <<'PYEOF' || rc=1
import os, re, sys

ROOT = 'src/main/java'

TYPE_RE = re.compile(
    r'^\s*(?P<mods>(?:public|final|abstract|\s)*)(?P<kind>class|interface)\s+(?P<name>\w+)(?:<[^>]*>)?\s*'
    r'(?P<rest>(?:extends|implements)[^{]*)?\{', re.M)

METHOD_RE = re.compile(
    r'^[ \t]*(?:@\w+(?:\([^)]*\))?[ \t]*)*'
    r'(?P<mods>(?:public|protected|private|static|final|abstract|synchronized|native|default|\s)*)'
    r'(?:@\w+(?:\([^)]*\))?[ \t]*)*'                              # siehe oben
    r'(?P<ret>[\w.$]+(?:<[^;{]*?>)?(?:\[\])*)\s+'
    r'(?P<name>\w+)\s*\((?P<params>[^)]*)\)\s*(?P<tail>[;{])', re.M)

PKG_RE = re.compile(r'^\s*package\s+([\w.]+)\s*;', re.M)
IMP_RE = re.compile(r'^\s*import\s+(?:static\s+)?([\w.]+)\s*;', re.M)

# Namen, die die Minecraft-Basisklassen ohnehin mitbringen. Endet die Vererbungskette einer
# Klasse ausserhalb des Projekts, kann eine Schnittstellenmethode dieses Namens von dort
# kommen -- Entity.getName() etwa bedient IRadarDetectableNT.getName() bei den Raketen.
MC_PROVIDED = {'getName'}

def strip_comments(src):
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    return re.sub(r'//[^\n]*', '', src)

def split_types(s):
    """Typliste hinter extends/implements trennen, ohne an Generika-Kommas zu zerbrechen."""
    out, depth, cur = [], 0, ''
    for ch in s:
        if ch == '<': depth += 1
        elif ch == '>': depth -= 1
        if ch == ',' and depth == 0:
            out.append(cur); cur = ''
        else:
            cur += ch
    if cur.strip(): out.append(cur)
    return [t.strip().split('<')[0] for t in out if t.strip()]

types = {}   # voll qualifizierter Name -> dict

for dirpath, _, files in os.walk(ROOT):
    for fn in files:
        if not fn.endswith('.java'): continue
        path = os.path.join(dirpath, fn)
        src = strip_comments(open(path, encoding='utf-8', errors='replace').read())
        m = TYPE_RE.search(src)
        if not m or m.group('name') != fn[:-5]: continue      # nur der Haupttyp der Datei

        pm = PKG_RE.search(src)
        pkg = pm.group(1) if pm else ''
        rest = m.group('rest') or ''
        em = re.search(r'extends\s+(.*?)(?:\simplements\s|$)', rest, re.S)
        im = re.search(r'implements\s+(.*)$', rest, re.S)

        sup, ifaces = None, []
        if m.group('kind') == 'class':
            if em:
                t = split_types(em.group(1))
                sup = t[0] if t else None
            if im: ifaces = split_types(im.group(1))
        elif em:
            ifaces = split_types(em.group(1))                  # interface extends ...

        methods = {}
        for mm in METHOD_RE.finditer(src):
            if mm.group('ret') in ('return', 'new', 'class', 'else', 'if', 'while', 'for', 'catch', 'switch'): continue
            params = mm.group('params').strip()
            arity = 0 if not params else params.count(',') + 1
            # Rumpf vorhanden oder static => bedient; nur ein Semikolon => abstrakt
            concrete = mm.group('tail') == '{' or 'static' in mm.group('mods')
            methods.setdefault((mm.group('name'), arity), []).append(concrete)

        types[pkg + '.' + m.group('name')] = {
            'path': path, 'pkg': pkg, 'kind': m.group('kind'),
            'super': sup, 'ifaces': ifaces,
            'abstract': 'abstract' in m.group('mods'),
            'methods': methods, 'imports': IMP_RE.findall(src),
        }

by_simple = {}
for fq in types: by_simple.setdefault(fq.rsplit('.', 1)[1], []).append(fq)

def resolve(ref, holder):
    """Einfachen Namen im Kontext seiner Datei aufloesen. None = nicht im Projekt."""
    if '.' in ref:
        return ref if ref in types else None
    info = types[holder]
    same_pkg = info['pkg'] + '.' + ref
    if same_pkg in types: return same_pkg
    for imp in info['imports']:
        if imp.rsplit('.', 1)[-1] == ref:
            return imp if imp in types else None
    hits = by_simple.get(ref, [])
    return hits[0] if len(hits) == 1 else None

def supchain(fq):
    """Oberklassen, solange sie Projektklassen sind."""
    out, seen = [], set()
    cur = resolve(types[fq]['super'], fq) if types[fq]['super'] else None
    while cur and cur not in seen:
        seen.add(cur); out.append(cur)
        cur = resolve(types[cur]['super'], cur) if types[cur]['super'] else None
    return out

def iface_closure(fq):
    """Alle Projekt-Schnittstellen eines Typs, auch die ererbten."""
    out, seen, stack = [], set(), [(i, fq) for i in types[fq]['ifaces']]
    while stack:
        ref, holder = stack.pop()
        r = resolve(ref, holder)
        if not r or r in seen or types[r]['kind'] != 'interface': continue
        seen.add(r); out.append(r)
        stack.extend((i, r) for i in types[r]['ifaces'])
    return out

def has_external_root(fq):
    """Ob die Vererbungskette ausserhalb des Projekts endet."""
    cur = fq
    while True:
        sup = types[cur]['super']
        if not sup: return False
        r = resolve(sup, cur)
        if r is None: return True
        cur = r

problems = []

for fq, info in sorted(types.items()):
    if info['kind'] != 'class' or info['abstract']: continue

    chain = [fq] + supchain(fq)
    closure = set()
    for c in chain: closure.update(iface_closure(c))
    if not closure: continue

    provided = MC_PROVIDED if has_external_root(fq) else set()

    required = {}
    for i in closure:
        for key, sigs in types[i]['methods'].items():
            if all(not c for c in sigs): required.setdefault(key, i)

    for key, iface in required.items():
        if key[0] in provided: continue
        if any(key in types[c]['methods'] and any(types[c]['methods'][key]) for c in chain): continue
        if any(key in types[i]['methods'] and any(types[i]['methods'][key]) for i in closure): continue
        problems.append('%s: %s() aus %s ist nicht implementiert'
                        % (info['path'], key[0], iface.rsplit('.', 1)[1]))

# ---------------------------------------------------------------------------------------
# Runde 67: ein dritter Durchgang fuer EINE Minecraft-Vorgabe, die der Port ausnahmslos
# erfuellen muss.
#
# In Runde 66 kostete das einen CI-Lauf: zwei neue Bloecke erbten ueber DummyableBlock von
# BaseEntityBlock und erklaerten kein codec(). javac meldet dazu "is not abstract and does not
# override abstract method codec()" -- eine Meldung, die das Syntaxgate wegfiltern muss, weil
# sie ohne Minecraft-Klassenpfad zu Tausenden als Folgefehler entsteht.
#
# Die beiden anderen Durchgaenge sehen das nicht: codec() ist in keiner PROJEKTklasse und in
# keiner Projektschnittstelle abstrakt erklaert, sondern in BaseEntityBlock selbst.
#
# NACHGEMESSEN: alle 113 konkreten Projektklassen, deren Vererbungskette ausserhalb bei
# BaseEntityBlock endet, erklaeren codec() -- ausnahmslos. Das ist keine Faustregel, sondern
# eine Vorgabe der API, und sie laesst sich deshalb hart pruefen.
#
# VERWORFEN: der naheliegendere Gedanke, solche Vorgaben aus den Geschwisterklassen zu
# ERRATEN ("alle anderen Ableitungen erklaeren die Methode, du nicht"). Nachgemessen liefert
# das bei der Schwelle, die den codec()-Fall findet, sieben Falschmeldungen auf einem Baum,
# der uebersetzt -- eine Attrappe mit Ausnahmeliste. Hier steht stattdessen genau die eine
# Vorgabe, die nachweisbar gilt.
# ---------------------------------------------------------------------------------------

MC_ABSTRACT_BY_ROOT = {
    'BaseEntityBlock': [('codec', 0)],
}

# ---------------------------------------------------------------------------------------
# Runde 103: dieselbe Vorgabe, nur ueber eine SCHNITTSTELLE statt ueber eine Oberklasse.
#
# In Runde 102 kostete das einen CI-Lauf: CranePartitionerBlockEntity erbt von
# MachineBaseBlockEntity, und die gibt "implements MenuProvider" an, ohne createMenu zu
# erklaeren -- die Methode bleibt also abstrakt. Fuenfundfuenfzig konkrete Ableitungen
# erklaeren sie, die sechsundfuenfzigste hat keine Oberflaeche und hat sie vergessen.
#
# Die Tabelle oben greift hier nicht: die aeussere Oberklasse ist BlockEntity, nicht
# BaseEntityBlock, und BlockEntity verlangt nichts dergleichen. Die Vorgabe haengt an der
# Schnittstelle, und die kann irgendwo in der Kette stehen.
#
# NACHGEMESSEN (Runde 103): ueber den ganzen Baum meldet die Regel null Funde. Nimmt man
# createMenu aus dem Portionierer wieder heraus, meldet sie genau ihn.
#
# Die Tabelle waechst nur, wenn ein Fall wirklich auftritt. Geraten wird nichts -- aus
# demselben Grund, aus dem der Gedanke oben verworfen ist.
# ---------------------------------------------------------------------------------------

MC_ABSTRACT_BY_INTERFACE = {
    'MenuProvider': [('createMenu', 3)],
}

def external_root(fq):
    """Die erste Oberklasse ausserhalb des Projekts, als einfacher Name -- oder None."""
    cur = fq
    seen = set()
    while cur not in seen:
        seen.add(cur)
        sup = types[cur]['super']
        if not sup: return None
        r = resolve(sup, cur)
        if r is None: return sup.split('.')[-1].split('<')[0]
        cur = r

for fq, info in sorted(types.items()):
    if info['kind'] != 'class' or info['abstract']: continue

    root = external_root(fq)
    if root not in MC_ABSTRACT_BY_ROOT: continue

    chain = [fq] + supchain(fq)

    for key in MC_ABSTRACT_BY_ROOT[root]:
        if any(key in types[c]['methods'] for c in chain): continue
        problems.append('%s: %s() aus %s ist nicht implementiert'
                        % (info['path'], key[0], root))

for fq, info in sorted(types.items()):
    if info['kind'] != 'class' or info['abstract']: continue

    chain = [fq] + supchain(fq)

    # alle Schnittstellennamen der ganzen Kette, aufgeloest wie im Projekt ueblich
    named = set()
    for c in chain:
        for i in types[c]['ifaces']:
            named.add(i.split('.')[-1].split('<')[0])

    for iface, keys in MC_ABSTRACT_BY_INTERFACE.items():
        if iface not in named: continue

        for key in keys:
            if any(key in types[c]['methods'] for c in chain): continue
            problems.append('%s: %s() aus %s ist nicht implementiert'
                            % (info['path'], key[0], iface))

# ---------------------------------------------------------------------------------------
# Runde 110: ein @Override, das NICHTS UEBERSCHREIBT.
#
# In Runde 109 kostete das einen CI-Lauf: MachineStrandCasterBlockEntity trug
# "@Override public FluidTank getTankToPaste()", ohne IFluidCopiable anzugeben -- die
# Schnittstelle, in der diese Methode steht. javac meldet dazu "method does not override or
# implement a method from a supertype".
#
# Die Durchgaenge davor sehen das nicht: sie pruefen, ob eine geforderte Methode FEHLT, nicht
# ob eine vorhandene ins Leere zeigt.
#
# DIE REGEL BRAUCHTE ZWEI ANLAEUFE. Der erste lautete: ein @Override, dessen Name das Projekt
# zwar kennt, aber nicht in der eigenen Verwandtschaft. Nachgemessen sind das ueber vierzig
# Falschmeldungen -- tick(), equals(), place(), getBlockState() und dergleichen stehen zufaellig
# auch irgendwo im Projekt, kommen in diesen Klassen aber aus Minecraft. Diese Fassung ist
# verworfen; sie waere eine Attrappe mit Ausnahmeliste geworden, und genau das steht schon
# weiter oben als verworfen.
#
# DIE FASSUNG, DIE HIER STEHT, fragt stattdessen nach dem AUSREISSER: fuer jede Methode, die
# eine Projekt-Schnittstelle erklaert, werden alle Projektklassen gesammelt, die sie ebenfalls
# erklaeren. Erreichen ALLE bis auf EINE die Methode ueber jene Schnittstelle, ist diese eine
# der Fehler. Bei tick() und equals() erreicht sie KEINE ueber eine Schnittstelle -- die Regel
# schweigt. Bei getTankToPaste() erreichen sie alle ueber IFluidCopiable, nur der Strangguss
# nicht.
#
# NACHGEMESSEN: ueber den ganzen Baum null Funde. Setzt man die Zeile aus Runde 109 wieder ein,
# meldet die Regel genau sie.
# ---------------------------------------------------------------------------------------

OVERRIDE_RE = re.compile(
    r'@Override\b[ \t\r\n]*'
    r'(?:@\w+(?:\([^)]*\))?[ \t\r\n]*)*'
    r'(?:(?:public|protected|private|static|final|abstract|synchronized|native|default)[ \t\r\n]+)*'
    r'(?:@\w+(?:\([^)]*\))?[ \t\r\n]*)*'
    r'[\w.$]+(?:<[^;{]*?>)?(?:\[\])*[ \t\r\n]+'
    r'(?P<name>\w+)[ \t]*\((?P<params>[^)]*)\)')

# Was jede Klasse ueber @Override erklaert, und was ihre Verwandtschaft hergibt.
own_overrides = {}     # fq -> Menge von (Name, Stelligkeit)
own_closure = {}       # fq -> Menge der Projekt-Schnittstellen der ganzen Kette

for fq, info in types.items():
    if info['kind'] != 'class': continue

    chain = [fq] + supchain(fq)
    closure = set()
    for c in chain: closure.update(iface_closure(c))
    own_closure[fq] = closure

    src = strip_comments(open(info['path'], encoding='utf-8', errors='replace').read())
    keys = set()
    for mm in OVERRIDE_RE.finditer(src):
        params = mm.group('params').strip()
        keys.add((mm.group('name'), 0 if not params else params.count(',') + 1))
    own_overrides[fq] = keys

# Methoden, die eine Projekt-Schnittstelle erklaert
iface_keys = {}        # (Name, Stelligkeit) -> Menge der Schnittstellen
for fq, info in types.items():
    if info['kind'] != 'interface': continue
    for key in info['methods']:
        iface_keys.setdefault(key, set()).add(fq)

for key, ifaces in sorted(iface_keys.items()):

    declarers = [fq for fq, keys in own_overrides.items() if key in keys]
    if len(declarers) < 2: continue

    without = [fq for fq in declarers if not (own_closure[fq] & ifaces)]
    if len(without) != 1: continue
    if len(declarers) - 1 < 1: continue

    fq = without[0]
    problems.append('%s: @Override an %s() -- alle anderen Klassen erreichen die Methode ueber %s, diese erklaert sie nicht'
                    % (types[fq]['path'], key[0], sorted(ifaces)[0].rsplit('.', 1)[1]))

print('Pruefe Schnittstellen innerhalb des Projekts ... %d Typen' % len(types))
if problems:
    print('  AUFFAELLIG: %d' % len(problems))
    for p in problems: print('  ' + p)
    sys.exit(1)
print('OK - jede Projekt-Schnittstelle ist vollstaendig implementiert.')
PYEOF

exit $rc
