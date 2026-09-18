#!/usr/bin/env bash
#
# Prueft die Texturen: jede vom Datengenerator angesprochene Datei muss vorhanden sein,
# und jede vorhandene Datei muss angesprochen werden.
#
# Beides faellt sonst erst im Spiel auf -- eine fehlende Textur als schwarz-violettes
# Karo, eine ueberzaehlige gar nicht. Der Datengenerator selbst meldet fehlende
# Blocktexturen zwar (ExistingFileHelper), aber nur, wenn er ueberhaupt laeuft; der
# blosse Bau tut das nicht.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

ASSETS = 'src/main/resources/assets/energycontrol'
DATAGEN = 'src/main/java/com/zuxelus/energycontrol/datagen'
INIT = 'src/main/java/com/zuxelus/energycontrol/init'

def read(path):
    return open(path, encoding='utf-8').read()

# --- Blocktexturen: was ECBlockStateProvider anspricht ---------------------------------
state = read(os.path.join(DATAGEN, 'ECBlockStateProvider.java'))
wanted_blocks = set(re.findall(r'block\(\s*"([^"]+)"', state))

# Nicht jede Blocktextur haengt an einem Modell: den Schirm der Tafel bindet der
# Renderer selbst, ueber EnergyControl.loc("textures/block/....png").
for dirpath, _dirs, names in os.walk('src/main/java'):
    for name in names:
        if not name.endswith('.java'): continue
        for m in re.finditer(r'loc\(\s*"textures/block/([\w/]+)\.png"\s*\)', read(os.path.join(dirpath, name))):
            wanted_blocks.add(m.group(1))

have_blocks = {f[:-4] for f in os.listdir(os.path.join(ASSETS, 'textures/block')) if f.endswith('.png')}

# --- Gegenstandstexturen: jeder registrierte Gegenstand ausser Blockgegenstaenden ------
items = read(os.path.join(INIT, 'ECItems.java'))
wanted_items = set(re.findall(r'ITEMS\.register\(\s*"([^"]+)"', items))
wanted_items |= set(re.findall(r'kit\(\s*"([^"]+)"', items))

have_items = {f[:-4] for f in os.listdir(os.path.join(ASSETS, 'textures/item')) if f.endswith('.png')}

problems = []
for name in sorted(wanted_blocks - have_blocks):
    problems.append('Blocktextur fehlt: textures/block/%s.png' % name)
for name in sorted(have_blocks - wanted_blocks):
    problems.append('Blocktextur unbenutzt: textures/block/%s.png' % name)
for name in sorted(wanted_items - have_items):
    problems.append('Gegenstandstextur fehlt: textures/item/%s.png' % name)
for name in sorted(have_items - wanted_items):
    problems.append('Gegenstandstextur unbenutzt: textures/item/%s.png' % name)

# --- Oberflaechen: jede im Quelltext genannte GUI-Textur muss es geben ----------------
gui_used = set()
for dirpath, _dirs, names in os.walk('src/main/java'):
    for name in names:
        if not name.endswith('.java'): continue
        # Nur was ueber EnergyControl.loc(...) geht, liegt in unserem Namensraum;
        # ein Bild von Minecraft (etwa die grosse Truhe) wird hier nicht gesucht.
        for m in re.finditer(r'loc\(\s*"textures/gui/([\w/]+)\.png"\s*\)', read(os.path.join(dirpath, name))):
            gui_used.add(m.group(1))
have_gui = {f[:-4] for f in os.listdir(os.path.join(ASSETS, 'textures/gui')) if f.endswith('.png')}
for name in sorted(gui_used - have_gui):
    problems.append('Oberflaechentextur fehlt: textures/gui/%s.png' % name)

# --- Klaenge: jeder Eintrag in sounds.json muss eine Datei haben ----------------------
sounds_json = read(os.path.join(ASSETS, 'sounds.json'))
for m in re.finditer(r'"energycontrol:([\w/]+)"', sounds_json):
    path = os.path.join(ASSETS, 'sounds', m.group(1) + '.ogg')
    if not os.path.isfile(path):
        problems.append('Klangdatei fehlt: %s' % path)

print('Pruefe Texturen und Klaenge ... %d Blocktexturen, %d Gegenstandstexturen'
      % (len(have_blocks), len(have_items)))

if problems:
    print('  AUFFAELLIG: %d' % len(problems))
    for p in problems:
        print('  ' + p)
    sys.exit(1)

print('OK - jede angesprochene Datei ist da, und keine liegt ungenutzt herum.')
PYEOF
