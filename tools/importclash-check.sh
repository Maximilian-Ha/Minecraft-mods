#!/usr/bin/env bash
#
# Prueft, dass derselbe Minecraft-Klassenname im ganzen Port aus DEMSELBEN Paket kommt.
#
# Hintergrund: tools/import-check.sh kann nur com.hbm-/api.hbm-Typen beurteilen, weil ohne
# Minecraft-Klassenpfad nicht feststellbar ist, ob ein net.minecraft-Paket eine Klasse
# wirklich enthaelt. Ein falscher Minecraft-Import faellt deshalb erst in der CI auf.
#
# GENAU DAS IST AM 19.09. PASSIERT: RenderSkeletonHolder importierte
# net.minecraft.client.renderer.ItemRenderer. Die Klasse liegt in 1.21 aber in
# net.minecraft.client.renderer.entity -- und genau so steht sie in RenderPrecAss,
# RenderPlushie, RenderPress und RenderBobble. Der Bau brach mit "cannot find symbol" ab,
# nachdem alle siebenundzwanzig Tore gruen gemeldet hatten.
#
# DIE REGEL. Steht derselbe einfache Klassenname in zwei verschiedenen net.minecraft-Paketen,
# ist einer der beiden Importe falsch -- der Port benutzt jede Minecraft-Klasse nur in einer
# Fassung. Das ist ohne Klassenpfad pruefbar, weil die Aussage relativ ist: sie vergleicht
# den Port mit sich selbst.
#
# AUSGENOMMEN sind verschachtelte Klassen: bei "SynchedEntityData.Builder" ist das letzte
# Segment vor dem Namen kein Paket, sondern die aeussere Klasse, erkennbar am Grossbuchstaben.
# Zwei verschiedene aeussere Klassen duerfen dieselbe innere Klasse tragen -- Builder und
# Context tun das im Port heute schon.
#
# NACHGEMESSEN (19.09.): 0 Funde. Schreibt man in RenderSkeletonHolder wieder
# net.minecraft.client.renderer.ItemRenderer, meldet die Pruefung genau diesen einen Namen
# und endet mit 1 (Exit-Code direkt geprueft, nicht durch eine Pipe).

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys
from collections import defaultdict

quellen = defaultdict(lambda: defaultdict(list))

for wurzel, _, dateien in os.walk('src/main/java'):
    for datei in dateien:
        if not datei.endswith('.java'): continue
        pfad = os.path.join(wurzel, datei)
        text = open(pfad, encoding='utf-8', errors='replace').read()
        for m in re.finditer(r'^import (net\.minecraft\.[\w.]+)\.(\w+);', text, re.M):
            paket, name = m.group(1), m.group(2)
            # Verschachtelte Klasse: das letzte Segment ist eine Klasse, kein Paket.
            if paket.rsplit('.', 1)[-1][0].isupper(): continue
            quellen[name][paket].append(pfad)

streit = {name: pakete for name, pakete in quellen.items() if len(pakete) > 1}

print('Pruefe Minecraft-Importe ... %d Klassennamen, %d mehrfach belegt'
      % (len(quellen), len(streit)))

if not streit:
    sys.exit(0)

print()
print('DERSELBE NAME AUS ZWEI PAKETEN -- einer der beiden Importe ist falsch:')
for name in sorted(streit):
    print('   %s' % name)
    for paket, dateien in sorted(streit[name].items()):
        print('      %s  (%dx, z.B. %s)' % (paket, len(dateien), dateien[0]))

sys.exit(1)
PYEOF
