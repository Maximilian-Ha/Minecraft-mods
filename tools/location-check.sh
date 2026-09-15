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

print("Pruefe ResourceLocation-Namen ... %d geprueft" % checked)

if not findings:
    print("OK - jeder Name besteht nur aus [a-z0-9/._-].")
    sys.exit(0)

print("  AUFFAELLIG: %d" % len(findings))
for where, what, value, bad in findings:
    print("  %s: %s \"%s\" -- unerlaubt: %s" % (where, what, value, bad))
print()
print("Minecraft wirft dafuer eine ResourceLocationException, sobald der Name registriert")
print("oder nachgeschlagen wird. Erlaubt im Pfad ist nur [a-z0-9/._-].")
sys.exit(1)
PY
