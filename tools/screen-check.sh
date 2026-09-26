#!/usr/bin/env bash
#
# Prueft, dass kein Bildschirm seinen Hintergrund zweimal zeichnet.
#
# Hintergrund: in 1.21 ruft Screen.render (und ueber super.render auch
# AbstractContainerScreen.render) renderBackground SELBST auf -- bei einem gewoehnlichen
# Screen samt Weichzeichner. Ruft render() vorher schon einmal renderBackground auf, zeichnet
# den Inhalt und ruft dann super.render, legt sich der zweite Hintergrund ueber das bereits
# Gezeichnete.
#
# Gefunden im Spiel (Runde 309): das Menue der RBMK-Konsole war vollstaendig verschwommen.
#
# WIE GEMESSEN WIRD: jede render(GuiGraphics ...)-Methode, die sowohl renderBackground( als
# auch super.render( aufruft, ist ein Fund -- ausserhalb von Kommentaren.
#
# NACHGEMESSEN (Runde 309): vorher 3 Funde (RBMKConsoleScreen, ClayTabletScreen,
# FurnaceBrickScreen), danach 0. Setzt man in RBMKConsoleScreen den alten Aufruf wieder ein,
# meldet das Tor genau diese Datei.
#
set -u

cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, pathlib, sys

def ohne_kommentare(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

funde = []
geprueft = 0
for datei in sorted(pathlib.Path('src/main/java').rglob('*.java')):
    text = ohne_kommentare(datei.read_text(encoding='utf-8', errors='replace'))
    for m in re.finditer(r'public void render\(GuiGraphics[^)]*\)\s*\{(.*?)\n    \}', text, re.S):
        geprueft += 1
        rumpf = m.group(1)
        if 'renderBackground(' in rumpf and 'super.render(' in rumpf:
            funde.append(str(datei))

for f in funde:
    print(f'{f}: render() ruft renderBackground UND super.render -- der Hintergrund kommt zweimal, '
          'der zweite liegt ueber dem Inhalt (bei Screen: verschwommen)')
print(f'{geprueft} render-Methoden geprueft, {len(funde)} Funde')
sys.exit(1 if funde else 0)
PY
