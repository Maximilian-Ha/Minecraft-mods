#!/usr/bin/env bash
#
# model-check.sh -- prueft, ob jede Textur da ist, die die Datengenerierung STILLSCHWEIGEND
# erwartet.
#
# WARUM ES DIESES TOR GIBT: basicItem(NtmItems.X) nennt keine Textur; der Erzeuger leitet sie
# aus dem Registriernamen ab und sucht item/<name>.png. Fehlt sie, bricht runData ab:
#
#   IllegalArgumentException: Texture hbmsntm:item/rbmk_link does not exist in any known resource pack
#
# Der asset-check findet das nicht -- er prueft Referenzen, die im Quelltext AUSGESCHRIEBEN
# stehen, und hier steht keine. Gemessen wurde die Luecke an rbmk_link: der Gegenstand war seit
# seiner Portierung ohne Bild, und erst der erste runData-Lauf hat es gezeigt.
#
# basicItem steht nicht nur im Gegenstands-Erzeuger: der Block-Erzeuger ruft es fuer Bloecke
# auf, deren Gegenstandsform ein flaches Bild bekommt (Scheibe, Leiter, Kette, Schiene), teils
# ueber eine Hilfsfunktion mit dem Block als Parameter. Die erste Fassung des Tors sah nur
# NtmItemModelProvider an und hat vier fehlende Bilder uebersehen; runData hat sie gefunden.
#
# Zweiter stillschweigender Weg: EnumMultiItem mit multiTexture leitet fuer JEDEN Wert der
# Aufzaehlung eine eigene Textur ab, item/<registriername>.<wert>.png. Eine Klasse darf
# registerItemModel ueberschreiben und eine andere Vorlage waehlen -- ConserveItem tut das und
# zeigt auf item/canned_<wert>.png. Das Tor liest die Vorlage deshalb aus dem Quelltext, statt
# sie zu raten; sonst meldet es 27 Fehlstellen, die keine sind.
#
# Dritter Weg: der Block-Erzeuger leitet Blocktexturen ebenso aus dem Registriernamen ab,
# teils mit Endungen (_side, _bottom, _top). Auch das hat erst runData gezeigt, an struct_icf.
#
# GEMESSEN: ueber den ganzen Baum null Funde (798 basicItem-Aufrufe, 216 abgeleitete
# Meta-Texturen, 322 abgeleitete Blocktexturen). Mit je einer geloeschten Textur genau ein
# Fund: rbmk_link.png (Gegenstands-Erzeuger), reinforced_glass_pane.png (Block-Erzeuger),
# rail_narrow.png (nur ueber eine Hilfsfunktion erreichbar), pellet_rtg.polonium.png
# (abgeleitete Meta-Textur), canned_tuna.png (ueberschriebene Vorlage), struct_icf.png und
# c4_side.png (abgeleitete Blocktexturen).

set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

python3 - "$ROOT" <<'PY'
import re, os, sys

root = sys.argv[1]
java = os.path.join(root, 'src/main/java')
texdir = os.path.join(root, 'src/main/resources/assets/hbmsntm/textures/item')

if not os.path.isdir(texdir):
    print("FEHLER: %s nicht gefunden." % texdir); sys.exit(2)

have = {f[:-4] for f in os.listdir(texdir) if f.endswith('.png')}

files = {}
for dirpath, _, fs in os.walk(java):
    for f in fs:
        if f.endswith('.java'):
            files[f[:-5]] = os.path.join(dirpath, f)

def args_at(src, open_idx):
    """Argumente eines Aufrufs auf oberster Ebene; open_idx zeigt auf die oeffnende Klammer."""
    depth, out, cur, i = 0, [], '', open_idx
    while i < len(src):
        c = src[i]
        if c == '"':
            j = i + 1
            while j < len(src) and not (src[j] == '"' and src[j-1] != '\\'): j += 1
            cur += src[i:j+1]; i = j + 1; continue
        if c in '([': depth += 1
        if c in ')]': depth -= 1
        if depth == 0 and c == ')':
            out.append(cur.strip()); return out, i
        if depth == 1 and c == ',':
            out.append(cur.strip()); cur = ''; i += 1; continue
        if not (depth == 1 and c == '('):
            cur += c
        i += 1
    return out, i

# ---------------------------------------------------------------- Teil 1: basicItem

# Registriernamen aller Gegenstaende einsammeln -- sie werden ueber mehrere Wege vergeben:
# direkt ueber ITEMS.register, ueber Hilfsfunktionen wie registerNugget, und in der
# Waffenfabrik ueber itemRegistry.register.
items = {}
for path in sorted(set(files.values())):
    src = open(path, encoding='utf-8').read()
    for m in re.finditer(r'(?:NtmItems\.)?\b([A-Z][A-Z0-9_]+)\s*=\s*[^;]{0,300}?\bregister\w*\(\s*"([^"]+)"', src, re.S):
        items.setdefault(m.group(1), m.group(2))

# basicItem-Aufrufe in allen Erzeugern einsammeln. Drei Formen kommen vor: der Gegenstand
# direkt (NtmItems.X.get()), die Gegenstandsform eines Blocks (NtmBlocks.X.asItem()) und der
# Umweg ueber eine Hilfsfunktion, die den Block als Parameter bekommt -- dann steht im Aufruf
# nur der Parametername, und das Feld steckt in den Aufrufstellen der Hilfsfunktion.
datagen = os.path.join(java, 'com/hbm/datagen')
calls, unresolved = [], []
for dirpath, _, fs in os.walk(datagen):
    for f in sorted(fs):
        if not f.endswith('.java'): continue
        path = os.path.join(dirpath, f)
        src = open(path, encoding='utf-8').read()
        for m in re.finditer(r'\bbasicItem\(', src):
            arg = args_at(src, m.end() - 1)[0][0] if args_at(src, m.end() - 1)[0] else ''
            d = re.match(r'(?:Ntm(?:Items|Blocks)\.)?([A-Za-z][A-Za-z0-9_]*)', arg.strip())
            if not d: continue
            token = d.group(1)
            if re.fullmatch(r'[A-Z][A-Z0-9_]*', token):
                calls.append((f, token)); continue
            # Parameter einer Hilfsfunktion: die Aufrufstellen liefern die Felder.
            mm = None
            for c in re.finditer(r'(?:private|public|protected)[^;{}]*?\b(\w+)\s*\([^;{}]*\)\s*\{', src[:m.start()]):
                mm = c
            if mm is None:
                unresolved.append("%s: basicItem(%s) -- Hilfsfunktion nicht bestimmbar" % (f, arg.strip()[:40])); continue
            helper = mm.group(1)
            sites = [a for a in re.findall(r'\b' + re.escape(helper) + r'\(\s*Ntm(?:Items|Blocks)\.([A-Z][A-Z0-9_]*)', src)]
            if not sites:
                unresolved.append("%s: basicItem(%s) in %s -- keine Aufrufstelle gefunden" % (f, arg.strip()[:40], helper)); continue
            for sfield in sites: calls.append((f, sfield))

missing, unknown = [], []
for f, field in calls:
    name = items.get(field)
    if name is None:
        unknown.append(field)
    elif name not in have:
        missing.append((field, name))

# ---------------------------------------------------------------- Teil 2: Meta-Texturen

def strip_comments(src):
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    return re.sub(r'//[^\n]*', '', src)

def split_top(text, sep):
    out, depth, cur = [], 0, ''
    for c in text:
        if c in '({[': depth += 1
        elif c in ')}]': depth -= 1
        if c == sep and depth == 0:
            out.append(cur); cur = ''
        else:
            cur += c
    out.append(cur)
    return out

def enum_consts(clsfile, enumname):
    """Die Werte einer Aufzaehlung -- erst in der Klassendatei, dann in einer gleichnamigen."""
    for p in [clsfile] + ([files[enumname]] if enumname in files else []):
        src = strip_comments(open(p, encoding='utf-8').read())
        m = re.search(r'\benum\s+' + re.escape(enumname) + r'\b[^{]*\{', src)
        if not m: continue
        i, depth, body = m.end(), 1, ''
        while i < len(src) and depth > 0:
            c = src[i]
            if c == '{': depth += 1
            elif c == '}':
                depth -= 1
                if depth == 0: break
            if depth == 1 and c == ';': break
            body += c; i += 1
        res = []
        for e in split_top(body, ','):
            mm = re.match(r'\s*([A-Z][A-Z0-9_]*)', e)
            if mm: res.append(mm.group(1))
        if res: return res
    return None

# Registriername -> Klasse, aber nur dort, wo der Gegenstand direkt gebaut wird.
built = {}
for path in sorted(set(files.values())):
    src = open(path, encoding='utf-8').read()
    for m in re.finditer(r'\bregister\w*\(\s*"([^"]+)"\s*,\s*\(\)\s*->\s*new\s+([A-Za-z0-9_.]+)\s*\(', src):
        built.setdefault(m.group(1), m.group(2).split('.')[-1])

meta_missing, blind = [], []
meta_count = 0
for name, cls in sorted(built.items()):
    path = files.get(cls)
    if not path: continue
    src = open(path, encoding='utf-8').read()
    if 'extends EnumMultiItem' not in src: continue
    ms = re.search(r'\bsuper\s*\(', src)
    if not ms: continue
    a, _ = args_at(src, ms.end() - 1)
    if len(a) != 4 or not a[1].endswith('.class') or a[3] != 'true': continue

    enumname = a[1][:-len('.class')].split('.')[-1]
    consts = enum_consts(path, enumname)
    if consts is None:
        blind.append("%s: Aufzaehlung %s nicht auffindbar" % (cls, enumname)); continue

    # Vorlage: die Fassung aus EnumMultiItem, sofern die Klasse sie nicht ueberschreibt.
    tmpl = "item/{path}.{enum}"
    mo = re.search(r'public void registerItemModel\b', src)
    if mo:
        mt = re.search(r'\.texture\(\s*"layer0"\s*,', src[mo.end():])
        if not mt:
            blind.append("%s: registerItemModel ueberschrieben, layer0 nicht lesbar" % cls); continue
        rest = src[mo.end() + mt.end() - 1:]
        targs, _ = args_at(rest, rest.index('('))
        expr = targs[-1]
        mrl = re.search(r'fromNamespaceAndPath\s*\(', expr)
        if mrl:
            rl, _ = args_at(expr, mrl.end() - 1)
            expr = rl[-1]
        parts, ok = [], True
        for t in split_top(expr, '+'):
            t = t.strip()
            if t.startswith('"') and t.endswith('"'): parts.append(t[1:-1])
            elif 'modelLocation.getPath()' in t: parts.append('{path}')
            elif '.name().toLowerCase' in t: parts.append('{enum}')
            else: ok = False
        if not ok:
            blind.append("%s: Texturausdruck nicht auswertbar (%s)" % (cls, expr.strip()[:60])); continue
        tmpl = ''.join(parts)

    for e in consts:
        tex = tmpl.replace('{path}', name).replace('{enum}', e.lower())
        meta_count += 1
        if not tex.startswith('item/'):
            blind.append("%s: unerwarteter Texturpfad %s" % (cls, tex)); continue
        if tex[len('item/'):] not in have:
            meta_missing.append((cls, tex))

# ---------------------------------------------------------------- Teil 3: Blocktexturen

# Der Block-Erzeuger leitet Texturen genauso still ab wie der Gegenstands-Erzeuger: aus dem
# Registriernamen des Blocks. Gefunden an struct_icf, das seit seiner Portierung ohne Bild war
# -- die Vorlage heisst upstream struct_icf_core.
texdir_block = os.path.join(root, 'src/main/resources/assets/hbmsntm/textures/block')
have_block = {f[:-4] for f in os.listdir(texdir_block) if f.endswith('.png')}

bsp = os.path.join(java, 'com/hbm/datagen/NtmBlockStateProvider.java')
bsrc = re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', open(bsp, encoding='utf-8').read(), flags=re.S))

# Je Form: der Aufruf, sein Muster und die Endungen, die er an den Namen haengt.
formen = [
    ('simpleCubeAllBlock',       r'\bsimpleCubeAllBlock\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)\s*\)',           ['']),
    ('simpleCubeBottomTopBlock', r'\bsimpleCubeBottomTopBlock\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)\s*\)',     ['_side', '_bottom', '_top']),
    ('cubeTop',                  r'(?<!\.)\bcubeTop\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)\s*\)',               ['_side', '_top']),
    ('simpleBlock',              r'\bsimpleBlock\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)\.get\(\)\s*\)',         ['']),
    ('cubeAll',                  r'\bcubeAll\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)\.get\(\)\s*\)',             ['']),
    ('blockTexture',             r'\bblockTexture\(\s*NtmBlocks\.([A-Z][A-Z0-9_]*)(?:\.get\(\))?\s*\)',   ['']),
]

block_missing = []
block_count = 0
for aufruf, muster, endungen in formen:
    for feld in re.findall(muster, bsrc):
        name = items.get(feld)
        for e in endungen:
            block_count += 1
            if name is None:
                blind.append("%s(%s): Registriername nicht aufloesbar" % (aufruf, feld)); continue
            if (name + e) not in have_block:
                block_missing.append((aufruf, feld, name + e))
block_missing = sorted(set(block_missing))

# ---------------------------------------------------------------- Bericht

# Was Teil 1 nicht aufloesen konnte, zaehlt wie eine blinde Stelle: ein Tor, das schweigt,
# wo es nicht hinsieht, ist schlimmer als keines.
blind = unresolved + blind

print("Pruefe Modell-Texturen ... %d basicItem-Aufrufe, %d abgeleitete Meta-Texturen, %d abgeleitete Blocktexturen"
      % (len(calls), meta_count, block_count))
print("                          %d Gegenstands- und %d Blocktexturen vorhanden" % (len(have), len(have_block)))

if unknown:
    print("  HINWEIS: fuer %d Felder liess sich der Registriername nicht aufloesen (%s)"
          % (len(unknown), ", ".join(unknown[:5])))

if not missing and not meta_missing and not block_missing and not blind:
    print("OK - jede stillschweigend erwartete Textur ist da.")
    sys.exit(0)

if blind:
    print("  BLINDE STELLEN: %d -- das Tor kann diese Faelle nicht pruefen" % len(blind))
    for b in blind: print("  " + b)

if missing or meta_missing or block_missing:
    print("  AUFFAELLIG: %d" % (len(missing) + len(meta_missing) + len(block_missing)))
    for field, name in missing:
        print("  %s erwartet item/%s.png -- die Datei fehlt" % (field, name))
    for cls, tex in meta_missing:
        print("  %s erwartet %s.png -- die Datei fehlt" % (cls, tex))
    for aufruf, feld, tex in block_missing:
        print("  %s(NtmBlocks.%s) erwartet block/%s.png -- die Datei fehlt" % (aufruf, feld, tex))
    print()
    print("runData bricht dafuer ab: \"Texture hbmsntm:item/<name> does not exist in any known")
    print("resource pack\". Entweder die Textur nachlegen oder den Aufruf entfernen.")

sys.exit(1)
PY
