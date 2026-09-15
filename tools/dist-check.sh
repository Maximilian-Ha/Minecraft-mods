#!/usr/bin/env bash
#
# dist-check.sh -- prueft, dass Serverklassen keine Clientklassen anfassen.
#
# WARUM ES DIESES TOR GIBT: ein dedizierter Server hat die Clientklassen nicht. Wer sie in einer
# Methode nennt, die auf dem Server geladen wird, bekommt beim Start:
#
#   RuntimeException: Attempted to load class net/minecraft/client/player/LocalPlayer
#   for invalid dist DEDICATED_SERVER
#
# Es reicht, dass die Methode DA IST -- ausgefuehrt werden muss sie nicht. Die Pruefung der
# Klasse laedt die Typen ihrer Zuweisungen, und schon faellt der Dist-Cleaner darueber.
# Gemessen am ersten Serverlauf: der Umbaublock (Minecraft.getInstance().player in printHook)
# und das Waffen-Grundstueck rissen die Registrierung ab, noch bevor eine Welt entstand.
#
# ABHILFE: @OnlyIn(Dist.CLIENT) an die Methode. Der Dist-Cleaner entfernt sie dann auf dem
# Server, und was nicht da ist, kann nichts laden. Das geht nur, wenn die Methode WIRKLICH nur
# auf dem Client laeuft -- sonst fehlt sie dort, wo sie gebraucht wird. Library.rayTrace ist so
# ein Fall: der Strahlengang laeuft auf beiden Seiten, dort musste stattdessen die Clientklasse
# aus dem Rumpf verschwinden.
#
# WAS ALS CLIENTKLASSE GILT: net.minecraft.client, net.neoforged.neoforge.client, blaze3d.
# Die Datengeneratoren gehoeren dazu (ItemModelProvider und Geschwister liegen unter
# neoforge.client) -- die Datengenerierung selbst laeuft als Client, deshalb stoert die
# Kennzeichnung dort nicht.
#
# AUSNAHMEN, und zwar hergeleitete statt gepflegte:
#   - Klassen im "client"-Abschnitt der Mixin-Datei: die laedt der Server nicht.
#   - Klassen, die ausser sich selbst nur Clientdateien nennen (Renderer, Screens, *Client).
#
# GEMESSEN: 65 Methoden in 63 Dateien waren offen, alle sind gekennzeichnet; jetzt null Funde.
# Mit einer entfernten Kennzeichnung genau ein Fund.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, os, sys, json, glob

CLIENT_PKGS = ('net.minecraft.client', 'net.neoforged.neoforge.client', 'com.mojang.blaze3d')
SERVER_PKGS = ('com/hbm/blocks/', 'com/hbm/items/', 'com/hbm/blockentity/', 'com/hbm/entity/',
               'com/hbm/inventory/', 'com/hbm/fluids/', 'com/hbm/world/', 'com/hbm/saveddata/',
               'com/hbm/util/', 'com/hbm/config/', 'com/hbm/registry/', 'com/hbm/lib/',
               'com/hbm/interfaces/', 'com/hbm/extprop/', 'com/hbm/packet/', 'com/hbm/network/')

def entkommentieren(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

dateien = sorted(glob.glob('src/main/java/**/*.java', recursive=True))
quelle = {p: open(p, encoding='utf-8').read() for p in dateien}

def ist_clientdatei(p):
    return ('/render/' in p or '/particle/' in p or '/screens/' in p
            or os.path.basename(p).endswith('Client.java') or '/hud/' in p)

# Ausnahme 1: der client-Abschnitt der Mixin-Datei.
nur_client = set()
for mix in glob.glob('src/main/resources/*.mixins.json'):
    d = json.load(open(mix, encoding='utf-8'))
    for name in d.get('client', []):
        nur_client.add(name.split('.')[-1])

# Ausnahme 2: Klassen, die ausserhalb ihrer selbst nur in Clientdateien vorkommen.
def nur_von_client_benutzt(p):
    name = os.path.basename(p)[:-5]
    gefunden = False
    for q in dateien:
        if q == p: continue
        if re.search(r'\b' + re.escape(name) + r'\b', quelle[q]):
            gefunden = True
            if not ist_clientdatei(q): return False
    return gefunden

funde, geprueft = [], 0
for p in dateien:
    if ist_clientdatei(p): continue
    if os.path.basename(p)[:-5] in nur_client: continue
    if not any(s in p for s in SERVER_PKGS): continue

    roh = quelle[p]
    src = entkommentieren(roh)
    if re.search(r'@OnlyIn\(\s*Dist\.CLIENT\s*\)\s*(public\s+)?(final\s+)?(abstract\s+)?(class|interface)', src):
        continue

    typen = set()
    for m in re.finditer(r'^import\s+((?:' + '|'.join(x.replace('.', '\\.') for x in CLIENT_PKGS) + r')[\w.]*);', src, re.M):
        typen.add(m.group(1).split('.')[-1])
    if not typen: continue

    offen = []
    for m in re.finditer(r'((?:@[\w.]+(?:\([^)]*\))?[ \t]*\n[ \t]*)*)((?:public|protected|private)\s[^;{}()\n]*\([^)]*\)\s*(?:throws [\w, .]+)?)\{', roh):
        anns, sig = m.group(1), m.group(2)
        i = m.end() - 1
        tiefe = 0
        while i < len(roh):
            if roh[i] == '{': tiefe += 1
            elif roh[i] == '}':
                tiefe -= 1
                if tiefe == 0: break
            i += 1
        text = entkommentieren(sig + roh[m.end():i])
        benutzt = sorted({t for t in typen if re.search(r'\b' + t + r'\b', text)})
        geprueft += 1
        if benutzt and 'Dist.CLIENT' not in anns:
            offen.append((sig.strip()[:70], benutzt[:3]))

    if offen and not nur_von_client_benutzt(p):
        funde.append((p, offen))

print("Pruefe Dist-Trennung ... %d Methoden in Serverklassen angesehen" % geprueft)
print("  ohne @OnlyIn(Dist.CLIENT), aber mit Clientbezug: %d in %d Dateien"
      % (sum(len(o) for _, o in funde), len(funde)))

if not funde:
    print("OK - keine Serverklasse fasst eine Clientklasse ungeschuetzt an.")
    sys.exit(0)

print()
print("UNGESCHUETZT -- der Server bricht beim Laden dieser Klasse ab:")
for p, offen in funde:
    print("  " + os.path.relpath(p, 'src/main/java'))
    for sig, benutzt in offen:
        print("      %s   [%s]" % (sig, ", ".join(benutzt)))
print()
print("Entweder @OnlyIn(Dist.CLIENT) an die Methode -- wenn sie wirklich nur auf dem Client")
print("laeuft --, oder die Clientklasse aus dem Rumpf nehmen, wenn beide Seiten sie brauchen.")
sys.exit(1)
PY
