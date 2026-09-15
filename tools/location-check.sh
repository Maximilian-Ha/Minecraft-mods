#!/usr/bin/env bash
#
# location-check.sh -- prueft, ob jeder Name, aus dem Minecraft eine ResourceLocation baut,
# nur erlaubte Zeichen enthaelt: [a-z0-9/._-] im Pfad.
#
# WARUM ES DIESES TOR GIBT: die sechzehn Sirenentonspuren aus Runde 127 trugen die Namen des
# Originals, also camelCase -- "alarm.airRaid". ResourceLocation.assertValidPath laesst das
# nicht durch und wirft beim Registrieren:
#
#   ResourceLocationException: Non [a-z0-9/._-] character in path of location: hbmsntm:alarm.airRaid
#
# Das riss den Mod beim Start mit. Kein Compiler findet es -- ein String ist ein String --,
# und auch der sound-check sah es nicht: dort war die Zuordnung Ereignis/Datei ja stimmig.
#
# GEPRUEFT WERDEN:
#   - die Namen in NtmSoundEvents.reg("...")
#   - die Registriernamen der DeferredRegister (Items, Bloecke, Blockeintraege, Menues, ...)
#   - Tonverweise im NtmSoundDefinitionsProvider (Quelle der erzeugten sounds.json)
#   - Materialnamen in Mats.java, aus denen zur Laufzeit Tag- und Gegenstandsnamen entstehen
#   - jede ausgeschriebene Kennung in withDefaultNamespace("...")
#   - doppelte Namen, und zwar in JEDEM Verzeichnis, nicht nur bei Bloecken und Gegenstaenden
#   - die Dateinamen unter assets/hbmsntm (Texturen, Modelle, Tondateien)
#
# AUSSERDEM: doppelt vergebene Registriernamen. Ein Block bekommt auf 1.21 ein BlockItem im
# SELBEN Verzeichnis wie jeder andere Gegenstand -- auf 1.7.10 waren das noch zwei getrennte.
# "pwr_fuel" gab es im Original als Block UND als Gegenstand; im Port brach NeoForge dafuer mit
# "IllegalArgumentException: Duplicate registration pwr_fuel" beim Start ab.
#
# GEMESSEN: ueber den ganzen Baum null Funde. Mit "alarm.airRaid" wieder eingesetzt genau ein
# Fund je betroffener Stelle, der Datei, Name und das stoerende Zeichen benennt.

set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

python3 - "$ROOT" <<'PY'
import re, os, sys

root = sys.argv[1]
java = os.path.join(root, 'src/main/java')
assets = os.path.join(root, 'src/main/resources/assets/hbmsntm')

VALID = re.compile(r'^[a-z0-9/._-]*$')
findings = []
checked = 0

def check(value, where, what):
    global checked
    checked += 1
    if not VALID.match(value):
        bad = sorted({c for c in value if not re.match(r'[a-z0-9/._-]', c)})
        findings.append((where, what, value, "".join(bad)))

# 1. Tonereignisse und Registriernamen aus dem Java-Quelltext.
#    reg("...") in NtmSoundEvents, sowie X.register("...", ...) der DeferredRegister.
pat_reg = re.compile(r'(?<![A-Za-z0-9_])reg\("([^"]*)"')
pat_deferred = re.compile(r'\b[A-Z_][A-Z0-9_]*\.register\(\s*"([^"]*)"\s*,')

for dirpath, _, files in os.walk(java):
    for f in files:
        if not f.endswith('.java'): continue
        p = os.path.join(dirpath, f)
        rel = os.path.relpath(p, root)
        src = open(p, encoding='utf-8').read()
        if f == 'NtmSoundEvents.java':
            for m in pat_reg.finditer(src):
                check(m.group(1), rel, "Tonereignis")
        for m in pat_deferred.finditer(src):
            check(m.group(1), rel, "Registriername")

# 1b. Die Materialnamen. Aus ihnen baut das Materialsystem zur Laufzeit Tag- und
# Gegenstandsnamen (toTagName: camelCase wird zu snake_case). Sie stehen nirgends als fertiger
# Pfad im Quelltext und entgehen deshalb der Pruefung oben -- gefunden wurde so, dass bei
# MAT_URANIUM ein ganzer Dreisatz des Originals in die Zeichenkette gerutscht war:
#
#   ResourceLocationException: Non [a-z0-9/._-] character in path of location:
#   hbmsntm:ingot_compat.is_mod_loaded(_compat._mod__gt6
def to_tag_name(name):
    out = []
    for i, c in enumerate(name):
        if c.isupper() and i > 0 and not name[i-1].isupper() and not name[i-1].isdigit():
            out.append('_')
        out.append(c.lower())
    return ''.join(out)

mats = os.path.join(java, 'com/hbm/inventory/material/Mats.java')
if os.path.isfile(mats):
    rel = os.path.relpath(mats, root)
    src = re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', open(mats, encoding='utf-8').read(), flags=re.S))
    for m in re.finditer(r'\bdf\(([^)]*)\)', src):
        for arg in re.findall(r'"([^"]*)"', m.group(1)):
            check(to_tag_name(arg), rel, "Materialname")

# 1c. Jede ausgeschriebene Ressourcenkennung: withDefaultNamespace("...") baut daraus direkt
# eine ResourceLocation, gleich wofuer -- Texturen, Modelle, Tags, Schadensarten, Kennungen von
# Modellvorhersagen. Das kostet nichts und deckt alles ab, was nicht erst zur Laufzeit entsteht.
for dirpath, _, files in os.walk(java):
    for f in files:
        if not f.endswith('.java'): continue
        p_ = os.path.join(dirpath, f)
        src = open(p_, encoding='utf-8').read()
        for m in re.finditer(r'withDefaultNamespace\(\s*"([^"]*)"', src):
            check(m.group(1), os.path.relpath(p_, root), "Ressourcenkennung")

# 2. Die Tonverweise. Die sounds.json wird erzeugt, nicht geschrieben -- geprueft wird
# deshalb ihre Quelle, der NtmSoundDefinitionsProvider.
prov = os.path.join(java, 'com/hbm/datagen/NtmSoundDefinitionsProvider.java')
if os.path.isfile(prov):
    rel = os.path.relpath(prov, root)
    src = open(prov, encoding='utf-8').read()
    # Beide Schreibweisen: sound("hbmsntm:pfad") und sound(withDefaultNamespace("pfad")).
    for m in re.finditer(r'sound\(\s*(?:NuclearTechMod\.withDefaultNamespace\(\s*)?"([^"]+)"', src):
        # Ein Verweis darf einen Namensraum tragen: "hbmsntm:alarm/air_raid"
        check(m.group(1).split(':', 1)[-1], rel, "Tonverweis")

# 3. Dateinamen unter assets: sie werden selbst zu ResourceLocation-Pfaden.
for dirpath, _, files in os.walk(assets):
    for f in files:
        p = os.path.join(dirpath, f)
        rel_asset = os.path.relpath(p, assets).replace(os.sep, '/')
        # lang/ ist ausgenommen: en_us.json ist ein Dateiname, kein Ressourcenpfad,
        # und Sprachdateien duerfen Grossbuchstaben im Regionsteil tragen.
        if rel_asset.startswith('lang/'): continue
        check(rel_asset, os.path.relpath(p, root), "Dateiname")

# 4. Doppelte Registriernamen. Bloecke und Gegenstaende teilen sich auf 1.21 das
#    Gegenstandsverzeichnis, also darf kein Blockname einen Gegenstandsnamen wiederholen.
import collections

def names_in(path, pattern):
    f = os.path.join(java, path)
    if not os.path.isfile(f): return []
    return re.findall(pattern, open(f, encoding='utf-8').read())

items  = names_in('com/hbm/items/NtmItems.java',  r'ITEMS\.register\(\s*"([^"]+)"')
blocks = names_in('com/hbm/blocks/NtmBlocks.java', r'(?<![A-Za-z0-9_])register\(\s*"([^"]+)"')

dups = []
for label, names in (("NtmItems", items), ("NtmBlocks", blocks)):
    for name, c in collections.Counter(names).items():
        if c > 1:
            dups.append("%s vergibt \"%s\" %dmal" % (label, name, c))
for name in sorted(set(items) & set(blocks)):
    dups.append("\"%s\" ist Gegenstand UND Block -- das BlockItem kollidiert mit dem Gegenstand" % name)

# 4b. Dieselbe Frage fuer alle uebrigen Verzeichnisse: Blockentitaeten, Entitaeten,
# Effekte, Fluide, Datenbestandteile und so fort. Zwei gleiche Namen in EINEM Verzeichnis
# brechen den Start genauso ab -- nur faellt es dort noch spaeter auf.
je_register = collections.defaultdict(list)
for dirpath, _, files in os.walk(java):
    for f in files:
        if not f.endswith('.java'): continue
        p_ = os.path.join(dirpath, f)
        src = re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', open(p_, encoding='utf-8').read(), flags=re.S))
        for m in re.finditer(r'\b([A-Z][A-Z0-9_]*)\.register\(\s*"([^"]+)"', src):
            je_register[m.group(1)].append(m.group(2))

weitere = 0
for reg, namen in sorted(je_register.items()):
    if reg in ('ITEMS', 'BLOCKS'): continue   # oben schon vollstaendig geprueft
    weitere += len(namen)
    for name, c in collections.Counter(namen).items():
        if c > 1:
            dups.append("%s vergibt \"%s\" %dmal" % (reg, name, c))

print("Pruefe ResourceLocation-Namen ... %d geprueft, %d Gegenstaende + %d Bloecke + %d weitere Eintraege auf Dopplung"
      % (checked, len(items), len(blocks), weitere))

if not findings and not dups:
    print("OK - jeder Name besteht nur aus [a-z0-9/._-], keiner doppelt vergeben.")
    sys.exit(0)

if dups:
    print("  DOPPELT VERGEBEN: %d" % len(dups))
    for d in dups:
        print("  " + d)
    print()
    print("NeoForge bricht dafuer beim Start mit \"Duplicate registration\" ab.")
    if not findings:
        sys.exit(1)
    print()

print("  AUFFAELLIG: %d" % len(findings))
for where, what, value, bad in findings:
    print("  %s: %s \"%s\" -- unerlaubt: %s" % (where, what, value, bad))
print()
print("Minecraft wirft dafuer eine ResourceLocationException, sobald der Name registriert")
print("oder nachgeschlagen wird. Erlaubt im Pfad ist nur [a-z0-9/._-].")
sys.exit(1)
PY
