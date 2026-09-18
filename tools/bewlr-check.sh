#!/usr/bin/env bash
#
# Prueft, dass jedes Gegenstandsmodell auf builtin/entity auch jemanden hat, der es zeichnet.
#
# Hintergrund: particleOnlyBlock und entityItem geben einem Gegenstand ein Modell mit dem
# Elternteil "builtin/entity". Ein solches Modell hat keine eigene Geometrie -- es sagt
# Minecraft nur: frag den BlockEntityWithoutLevelRenderer. Gibt es keinen, wird NICHTS
# gezeichnet. Der Gegenstand liegt dann unsichtbar im Inventar und in JEI: das Feld ist
# belegt, aber leer.
#
# Genau das hat ein Spieler am 18.09. gemeldet ("Diverse Geraete sind in JEI unsichtbar").
# Nachgezaehlt waren es einundzwanzig Bloecke. Bei zweien lag es an einer vergessenen Zeile
# (der grosse Radarschirm stand nicht in getItemsForRenderer, die acht RBMK-Tafeln brauchten
# gar kein builtin/entity), bei den uebrigen daran, dass der Darsteller NIE GESCHRIEBEN WURDE.
#
# DIE REGEL. Jeder Block, der ueber particleOnlyBlock oder entityBlockItem ein
# builtin/entity-Modell bekommt, und jeder Gegenstand, der ueber entityItem eines bekommt,
# muss in einem BEWLR auftauchen: entweder in getItemForRenderer/getItemsForRenderer eines
# IBEWLRProvider-Darstellers, der in ClientProxy auch angemeldet ist, oder in einem der
# registerItemRenderer-Aufrufe von ClientProxy bzw. GunFactoryClient.
#
# DIE AUSNAHMELISTE ist eine SCHULDENLISTE, keine Begruendung: sie nennt die Maschinen, deren
# Darsteller noch fehlt. Diese Bloecke sind heute auch IN DER WELT unsichtbar, denn ihr
# Blockmodell traegt nur eine Partikeltextur und DummyableBlock zeichnet ueber
# ENTITYBLOCK_ANIMATED ausschliesslich durch den Darsteller. Wer einen davon nachreicht,
# streicht die Zeile hier.
#
# NACHGEMESSEN (Runde 161): 208 builtin/entity-Modelle, zwoelf davon auf der Schuldenliste,
# null unerklaerte. Nimmt man die Anmeldung von RenderSatLink aus ClientProxy wieder heraus,
# meldet die Pruefung genau MACHINE_SAT_LINK und endet mit 1 (Exit-Code direkt geprueft).

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

def ohne_kommentare(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

def lies(pfad):
    return ohne_kommentare(open(pfad, encoding='utf-8', errors='replace').read())

zustaende = lies('src/main/java/com/hbm/datagen/NtmBlockStateProvider.java')
gegenstaende = lies('src/main/java/com/hbm/datagen/NtmItemModelProvider.java')
proxy = lies('src/main/java/com/hbm/main/ClientProxy.java')
gunfactory = lies('src/main/java/com/hbm/items/weapon/sedna/factory/GunFactoryClient.java')

# 1) Wer bekommt ein builtin/entity-Modell?
braucht_b = set(re.findall(r'particleOnlyBlock\(\s*NtmBlocks\.(\w+)', zustaende))
braucht_b |= set(re.findall(r'entityBlockItem\(\s*NtmBlocks\.(\w+)', zustaende))
braucht_i = set(re.findall(r'entityItem\(\s*NtmItems\.(\w+)', gegenstaende))
braucht_i |= set(re.findall(r'entityItem\(\s*NtmBlocks\.(\w+)', gegenstaende))

# 2) Wer wird gezeichnet?
angemeldet = set(re.findall(
    r'BlockEntityRenderers\.register\(\s*NtmBlockEntityTypes\.\w+\.get\(\)\s*,\s*new\s+(\w+)\(', proxy))

gezeichnet = set()
for dirpath, _, files in os.walk('src/main/java/com/hbm/render'):
    for fn in files:
        if not fn.endswith('.java'): continue
        if fn[:-5] not in angemeldet: continue
        src = lies(os.path.join(dirpath, fn))
        if 'IBEWLRProvider' not in src: continue
        gezeichnet |= set(re.findall(r'NtmBlocks\.(\w+)\.asItem\(\)', src))
        gezeichnet |= set(re.findall(r'NtmItems\.(\w+)\.get\(\)', src))

for quelle in (proxy, gunfactory):
    for m in re.finditer(r'register\w*ItemRenderer\([^;]*?;', quelle, re.S):
        gezeichnet |= set(re.findall(r'NtmBlocks\.(\w+)\.asItem\(\)', m.group(0)))
        gezeichnet |= set(re.findall(r'NtmItems\.(\w+)\.get\(\)', m.group(0)))

# ---------------------------------------------------------------------------------------
# SCHULDENLISTE: diese Maschinen haben ueberhaupt keinen Darsteller. Sie sind im Inventar
# UND in der Welt unsichtbar. Jede Zeile hier ist eine offene Aufgabe, keine Ausnahme.
# ---------------------------------------------------------------------------------------
OHNE_DARSTELLER = {
    'MACHINE_ANNIHILATOR':      'Runde 128 portiert, Darsteller fehlt',
    'MACHINE_CYCLOTRON':        'Runde 116 portiert, Darsteller fehlt',
    'MACHINE_EXPOSURE_CHAMBER': 'Runde 134 portiert, Darsteller fehlt',
    'MACHINE_GAS_CENT':         'Runde 115 portiert, Darsteller fehlt',
    'MACHINE_MINING_LASER':     'Runde 118 portiert, Darsteller fehlt',
    'MACHINE_PA_BEAMLINE':      'Runde 133 portiert, Darsteller fehlt',
    'MACHINE_PA_DETECTOR':      'Runde 133 portiert, Darsteller fehlt',
    'MACHINE_PA_DIPOLE':        'Runde 133 portiert, Darsteller fehlt',
    'MACHINE_PA_QUADRUPOLE':    'Runde 133 portiert, Darsteller fehlt',
    'MACHINE_PA_RFC':           'Runde 133 portiert, Darsteller fehlt',
    'MACHINE_PA_SOURCE':        'Runde 133 portiert, Darsteller fehlt',
    'MACHINE_RAD_GEN':          'Runde 135 portiert, Darsteller fehlt',
}

fehlend_b = sorted(f for f in braucht_b if f not in gezeichnet and f not in OHNE_DARSTELLER)
fehlend_i = sorted(f for f in braucht_i if f not in gezeichnet)
erledigt  = sorted(f for f in OHNE_DARSTELLER if f in gezeichnet or f not in braucht_b)

print('Pruefe Gegenstandsdarsteller ... %d builtin/entity-Modelle, %d auf der Schuldenliste'
      % (len(braucht_b) + len(braucht_i), len(OHNE_DARSTELLER)))
print('  Bloecke ohne Darsteller und ohne Eintrag      : %d' % len(fehlend_b))
print('  Gegenstaende ohne Darsteller                  : %d' % len(fehlend_i))
print('  Schuldenliste erledigt, Zeile kann weg        : %d' % len(erledigt))

if not fehlend_b and not fehlend_i and not erledigt:
    print('OK - jedes builtin/entity-Modell hat jemanden, der es zeichnet.')
    sys.exit(0)

for titel, liste, praefix in (('BLOECKE', fehlend_b, 'NtmBlocks.'), ('GEGENSTAENDE', fehlend_i, 'NtmItems.')):
    if not liste: continue
    print()
    print('%s MIT builtin/entity UND OHNE DARSTELLER -- sie liegen unsichtbar im Inventar:' % titel)
    for f in liste: print('   %s%s' % (praefix, f))

if erledigt:
    print()
    print('SCHULDENLISTE VERALTET -- diese Eintraege haben jetzt einen Darsteller oder kein')
    print('builtin/entity-Modell mehr, die Zeile in tools/bewlr-check.sh gehoert geloescht:')
    for f in erledigt: print('   %s' % f)

sys.exit(1)
PYEOF
