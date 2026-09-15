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
#   - Schluessel und Dateiverweise in sounds.json
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
import re, os, sys, json

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

# 2. sounds.json: Schluessel und Dateiverweise.
sounds = os.path.join(assets, 'sounds.json')
if os.path.isfile(sounds):
    rel = os.path.relpath(sounds, root)
    data = json.load(open(sounds, encoding='utf-8'))
    for key, entry in data.items():
        check(key, rel, "Tonschluessel")
        for s in entry.get('sounds', []):
            name = s if isinstance(s, str) else s.get('name', '')
            # Ein Verweis darf einen Namensraum tragen: "hbmsntm:alarm/air_raid"
            check(name.split(':', 1)[-1], rel, "Tonverweis")

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

print("Pruefe ResourceLocation-Namen ... %d geprueft, %d Gegenstaende + %d Bloecke auf Dopplung"
      % (checked, len(items), len(blocks)))

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
