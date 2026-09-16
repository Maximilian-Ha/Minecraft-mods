#!/usr/bin/env bash
#
# Prueft, ob jeder Uebersetzungsschluessel, den der Quelltext benutzt, auch in
# ECLanguageProvider steht -- und umgekehrt, ob dort nichts Unbenutztes liegt.
#
# Ein fehlender Schluessel faellt beim Bauen nicht auf: Minecraft zeigt dann einfach den
# Schluessel selbst an. Im Spiel steht dann "msg.ec.InfoPanelXenon" auf der Tafel.
#
# Schluessel fremder Herkunft (Minecraft selbst, HBM) werden nicht verlangt; sie stehen
# in der Ausnahmeliste unten.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

SRC = 'src/main/java/com/zuxelus/energycontrol'
LANG = os.path.join(SRC, 'datagen', 'ECLanguageProvider.java')

# Schluessel, die Minecraft selbst mitbringt.
FOREIGN_PREFIXES = ('options.', 'container.inventory', 'gui.', 'key.')

def strip_comments(text):
    return re.sub(r'//[^\n]*|/\*.*?\*/', '', text, flags=re.S)

# 1) Benutzte Schluessel einsammeln.
used = {}
for dirpath, _dirs, names in os.walk(SRC):
    for name in names:
        if not name.endswith('.java'): continue
        path = os.path.join(dirpath, name)
        if os.path.abspath(path) == os.path.abspath(LANG): continue
        text = strip_comments(open(path, encoding='utf-8').read())
        # Jede Zeichenkette, die wie ein eigener Schluessel aussieht -- egal, wie sie
        # weitergereicht wird. Ein Schluessel steht oft nicht direkt im Aufruf, sondern
        # in einem Bedingungsausdruck oder in einer Namensliste (ECSounds).
        for m in re.finditer(r'"((?:msg\.ec|item\.ec|container\.energycontrol'
                             r'|itemGroup\.energycontrol|subtitles\.energycontrol)[\w.]*)"', text):
            used.setdefault(m.group(1), set()).add(path)
        # Und zusaetzlich alles, was ausdruecklich als Uebersetzung aufgerufen wird --
        # so faellt auch ein Schluessel fremder Herkunft auf.
        for m in re.finditer(r'Component\.translatable\(\s*"([^"]+)"', text):
            used.setdefault(m.group(1), set()).add(path)

# 2) Erklaerte Schluessel einsammeln.
lang = strip_comments(open(LANG, encoding='utf-8').read())
declared = set(re.findall(r'both\(\s*"([^"]+)"', lang))
# Block- und Gegenstandsnamen entstehen aus dem Registrierungsnamen, nicht aus einem
# Schluessel im Quelltext -- die stehen hier als block(...)/item(...).
auto = len(re.findall(r'\b(?:block|item)\(EC', lang))

missing = sorted(k for k in used
                 if k not in declared and not k.startswith(FOREIGN_PREFIXES))
unused = sorted(k for k in declared if k not in used and not k.startswith(
    ('itemGroup.', 'container.energycontrol.', 'subtitles.')))

print('Pruefe Uebersetzungsschluessel ... %d benutzt, %d erklaert, %d automatisch'
      % (len(used), len(declared), auto))

status = 0
if missing:
    status = 1
    print('  FEHLEN in ECLanguageProvider: %d' % len(missing))
    for key in missing:
        print('    %s  (%s)' % (key, ', '.join(sorted(used[key]))))
if unused:
    status = 1
    print('  UNBENUTZT in ECLanguageProvider: %d' % len(unused))
    for key in unused:
        print('    %s' % key)

if status == 0:
    print('OK - jeder benutzte Schluessel ist uebersetzt.')
sys.exit(status)
PYEOF
