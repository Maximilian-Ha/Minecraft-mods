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
# GEMESSEN: ueber den ganzen Baum null Funde. Mit geloeschter rbmk_link.png genau ein Fund.

set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

python3 - "$ROOT" <<'PY'
import re, os, sys

root = sys.argv[1]
java = os.path.join(root, 'src/main/java')
texdir = os.path.join(root, 'src/main/resources/assets/hbmsntm/textures/item')

if not os.path.isdir(texdir):
    print("FEHLER: %s nicht gefunden." % texdir); sys.exit(2)

# Registriernamen aller Gegenstaende einsammeln -- sie werden ueber mehrere Wege vergeben:
# direkt ueber ITEMS.register, ueber Hilfsfunktionen wie registerNugget, und in der
# Waffenfabrik ueber itemRegistry.register.
items = {}
for dirpath, _, files in os.walk(java):
    for f in files:
        if not f.endswith('.java'): continue
        src = open(os.path.join(dirpath, f), encoding='utf-8').read()
        for m in re.finditer(r'(?:NtmItems\.)?\b([A-Z][A-Z0-9_]{2,})\s*=\s*[^;]{0,200}?\bregister\w*\(\s*"([^"]+)"', src, re.S):
            items.setdefault(m.group(1), m.group(2))

prov_path = os.path.join(java, 'com/hbm/datagen/NtmItemModelProvider.java')
prov = open(prov_path, encoding='utf-8').read()

have = {f[:-4] for f in os.listdir(texdir) if f.endswith('.png')}

calls = re.findall(r'this\.basicItem\(NtmItems\.([A-Z][A-Z0-9_]*)\.get\(\)\)', prov)

missing, unknown = [], []
for field in calls:
    name = items.get(field)
    if name is None:
        unknown.append(field)
    elif name not in have:
        missing.append((field, name))

print("Pruefe Modell-Texturen ... %d basicItem-Aufrufe, %d Texturen vorhanden" % (len(calls), len(have)))

if unknown:
    print("  HINWEIS: fuer %d Felder liess sich der Registriername nicht aufloesen (%s)"
          % (len(unknown), ", ".join(unknown[:5])))

if not missing:
    print("OK - jede stillschweigend erwartete Textur ist da.")
    sys.exit(0)

print("  AUFFAELLIG: %d" % len(missing))
for field, name in missing:
    print("  NtmItems.%s erwartet item/%s.png -- die Datei fehlt" % (field, name))
print()
print("runData bricht dafuer ab: \"Texture hbmsntm:item/<name> does not exist in any known")
print("resource pack\". Entweder die Textur nachlegen oder den basicItem-Aufruf entfernen.")
sys.exit(1)
PY
