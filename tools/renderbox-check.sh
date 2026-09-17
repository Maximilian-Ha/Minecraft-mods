#!/usr/bin/env bash
#
# Prueft, ob jeder Mehrblockbau mit eigenem Darsteller einen Sichtkasten hat.
#
# Hintergrund: Minecraft prueft den Sichtstumpf nicht nur gegen den Abschnitt, in dem eine
# Blockentitaet steht, sondern ueber die NeoForge-Erweiterung getRenderBoundingBox auch gegen
# einen Kasten je Blockentitaet. Ohne eigene Angabe ist dieser Kasten genau ein Block gross.
# Bei einem Mehrblockbau steht der Kern aber irgendwo im Bauwerk, und das Modell reicht weit
# darueber hinaus -- die Leviathan-Turbine etwa ist 15 Bloecke lang. Dreht der Spieler den
# Kopf so, dass der eine Kernblock aus dem Bild faellt, verschwindet das ganze Modell
# schlagartig, obwohl es noch zur Haelfte vor ihm steht.
#
# Genau das hat ein Spieler in Runde 153 gemeldet ("die Leviathan Turbine wird nach dem
# Platzieren unsichtbar, wenn man zur Seite schaut"). Siebzehn weitere Klassen hatten
# denselben Fehler, darunter die halbe Erdoelkette.
#
# DIE REGEL. Eine Blockentitaet muss einen Sichtkasten haben, wenn beides zutrifft:
#   1. sie hat einen eigenen Darsteller (eine Klasse extends BlockEntityRendererNT<X>), und
#   2. mindestens einer der Bloecke, fuer die ihr BlockEntityType angemeldet ist, erbt
#      von DummyableBlock, ist also ein Mehrblockbau.
# Der Kasten darf an drei Stellen stehen: als getRenderBoundingBox() an der Blockentitaet
# selbst oder an einer ihrer Projekt-Oberklassen, als getRenderBoundingBox(be) im Darsteller,
# oder -- fuer Bauten, die von ueberall sichtbar sein sollen -- als shouldRenderOffScreen
# im Darsteller. Der Port benutzt alle drei Formen.
#
# WARUM DIE ZWEITE BEDINGUNG. Ein Mehrblockbau ist per Definition groesser als ein Block;
# damit ist der voreingestellte Kasten immer zu klein. Bei Ein-Block-Maschinen gilt das
# nicht: das Modell des Dieselgenerators etwa misst genau X/Z -0,5 bis 0,5 und Y 0 bis 1
# und braucht nichts. Die Bedingung ist also keine Faustregel, sondern die Grenze, ab der
# die Aussage sicher stimmt.
#
# NACHGEMESSEN (Runde 153): 120 Blockentitaeten haben einen eigenen Darsteller, 84 davon
# haengen an einem Mehrblockbau. Nach den Ergaenzungen dieser Runde meldet die Pruefung
# davon null. Nimmt man ChungusBlockEntity.getRenderBoundingBox wieder heraus, meldet sie
# genau diese eine Klasse.
#
# VERWORFEN: die naheliegendere Fassung "jede Blockentitaet mit Darsteller braucht einen
# Kasten". Nachgemessen sind das achtzehn Falschmeldungen -- Wackelkopf, Geigerzaehler,
# Plueschtier, die neun RBMK-Anzeigen und die drei Teile des Reaktorstapels zeichnen
# alle innerhalb ihres Blocks. Eine Ausnahmeliste dafuer waere eine Attrappe.
#
# ZWEITE REGEL (Runde 157): kein Darsteller darf einen Sichtkasten in einem FELD merken.
#
# BlockEntityRenderers legt je BlockEntityType genau EINEN Darsteller an, den sich alle
# Maschinen dieses Typs teilen. Ein Kasten in Weltkoordinaten gilt aber nur fuer eine einzige
# Maschine. Wer ihn im Darsteller merkt, gibt ab der zweiten Maschine den Kasten der ersten
# zurueck -- und die zweite verschwindet, sobald der Spieler nicht zufaellig auch in Richtung
# der ersten schaut. Genau der Fehler aus Runde 153, nur an der anderen Seite.
#
# In der Blockentitaet ist dasselbe Merken richtig: jede hat ihr eigenes Feld. Die Regel gilt
# deshalb nur fuer Klassen unter src/main/java/com/hbm/render.
#
# NACHGEMESSEN (Runde 157): RenderCombustionEngine war der einzige Darsteller im Baum, der ein
# AABB-Feld hielt. Nach dem Ausbau meldet die Regel null; setzt man das Feld wieder ein,
# meldet sie genau ihn.
#
# NACHTRAG (Runde 157): die erste Fassung sah nur Darsteller, die DIREKT
# "extends BlockEntityRendererNT<X>" schreiben. Neun taten das nicht -- die sechs Masten und
# Verbinder ueber RenderPylonBase, die drei Geschuetze ueber RenderTurretBase -- und fielen
# still aus der Pruefung. Die Typangabe wird jetzt durch die Oberklassenkette verfolgt, bis
# sie bei einem echten Typnamen ankommt.
#
# Absichtliche Grenze: die Zuordnung Blockentitaet -> Block -> Klasse laeuft rein textuell
# ueber NtmBlockEntityTypes und NtmBlocks. Wer einen Block anders anmeldet als mit
# "register...("name", () -> new Klasse(", faellt aus der Pruefung heraus, statt eine
# Falschmeldung zu erzeugen.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

ROOT = 'src/main/java'

dateien = {}
for dirpath, _, files in os.walk(ROOT):
    for fn in files:
        if fn.endswith('.java'):
            dateien.setdefault(fn[:-5], os.path.join(dirpath, fn))

def quelle(name):
    return open(dateien[name], encoding='utf-8', errors='replace').read()

def ohne_kommentare(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

# 1) BlockEntityType -> Bloecke. Die Anmeldung steht ein- oder mehrzeilig, darum \s* vor .build.
bet = quelle('NtmBlockEntityTypes')
be_zu_bloecken = {}
for m in re.finditer(r'BlockEntityType\.Builder\.of\(\s*(\w+)::new\s*,(.*?)\)\s*\.build', bet, re.S):
    be_zu_bloecken.setdefault(m.group(1), set()).update(re.findall(r'NtmBlocks\.(\w+)', m.group(2)))

# 2) Block-Konstante -> Blockklasse
block_zu_klasse = {}
for m in re.finditer(r'DeferredBlock<[^>]*>\s+(\w+)\s*=\s*\w+\(\s*"[^"]*"\s*,\s*\(\)\s*->\s*new\s+([\w.]+)',
                     quelle('NtmBlocks')):
    block_zu_klasse[m.group(1)] = m.group(2).split('.')[-1]

def oberklasse(name):
    if name not in dateien: return None
    m = re.search(r'class\s+%s\b[^{]*?extends\s+([\w.<>]+)' % re.escape(name), quelle(name))
    return m.group(1).split('<')[0].split('.')[-1] if m else None

def ist_mehrblockbau(klasse):
    cur, tiefe = klasse, 0
    while cur in dateien and tiefe < 8:
        if re.search(r'class\s+%s\b[^{]*extends\s+DummyableBlock' % re.escape(cur), quelle(cur)): return True
        cur, tiefe = oberklasse(cur), tiefe + 1
    return False

# 3) Darsteller einsammeln. Die Typangabe steht entweder direkt an BlockEntityRendererNT
#    oder an einer Zwischenklasse (RenderPylonBase, RenderTurretBase), die ihrerseits von
#    BlockEntityRendererNT erbt. Beide Wege werden verfolgt.
darsteller = {}   # Klassenname -> (Pfad, Oberklasse, Typangabe)
for dirpath, _, files in os.walk('src/main/java/com/hbm/render'):
    for fn in files:
        if not fn.endswith('.java'): continue
        pfad = os.path.join(dirpath, fn)
        src = open(pfad, encoding='utf-8', errors='replace').read()
        m = re.search(r'class\s+(\w+)(?:<[^>]*>)?[^{]*?extends\s+(\w+)\s*(?:<\s*([\w.]+)\s*>)?', src)
        if not m: continue
        if m.group(1) != fn[:-5]: continue
        darsteller[m.group(1)] = (pfad, m.group(2), m.group(3))

def zeichnet_fuer(name, tiefe=0):
    """Die Blockentitaet, die dieser Darsteller zeichnet -- oder None."""
    if name not in darsteller or tiefe > 6: return None
    pfad, sup, typ = darsteller[name]
    if typ and len(typ.split('.')[-1]) > 2: return typ.split('.')[-1]
    if sup == 'BlockEntityRendererNT': return None    # Typvariable, also eine Zwischenklasse
    return zeichnet_fuer(sup, tiefe + 1)

def erbt_von_nt(name, tiefe=0):
    if name == 'BlockEntityRendererNT': return True
    if name not in darsteller or tiefe > 6: return False
    return erbt_von_nt(darsteller[name][1], tiefe + 1)

paare = set()
for name, (pfad, sup, typ) in darsteller.items():
    if not erbt_von_nt(name): continue
    be = zeichnet_fuer(name)
    if be: paare.add((be, pfad))

def hat_sichtkasten(be, darsteller_pfad):
    cur, tiefe = be, 0
    while cur in dateien and tiefe < 8:
        if re.search(r'\bAABB\s+getRenderBoundingBox\s*\(\s*\)', quelle(cur)): return True
        cur, tiefe = oberklasse(cur), tiefe + 1
    src = ohne_kommentare(open(darsteller_pfad, encoding='utf-8', errors='replace').read())
    if re.search(r'AABB\s+getRenderBoundingBox\s*\([^)]+\)', src): return True
    if re.search(r'boolean\s+shouldRenderOffScreen\s*\(', src): return True
    return False

geprueft, fehlend = 0, []
for be, pfad in sorted(paare):
    bloecke = be_zu_bloecken.get(be, ())
    if not any(ist_mehrblockbau(block_zu_klasse.get(b, '')) for b in bloecke): continue
    geprueft += 1
    if not hat_sichtkasten(be, pfad):
        fehlend.append((be, os.path.basename(pfad)))

# Zweite Regel: kein Darsteller haelt einen Sichtkasten in einem Feld.
FELD_RE = re.compile(r'^[ \t]*(?:private|protected|public)?[ \t]*(?:static[ \t]+)?(?:final[ \t]+)?AABB[ \t]+\w+[ \t]*(?:=|;)', re.M)
gemerkt = []
for dirpath, _, files in sorted(os.walk('src/main/java/com/hbm/render')):
    for fn in sorted(files):
        if not fn.endswith('.java'): continue
        pfad = os.path.join(dirpath, fn)
        src = ohne_kommentare(open(pfad, encoding='utf-8', errors='replace').read())
        if 'getRenderBoundingBox' not in src: continue
        for m in FELD_RE.finditer(src):
            gemerkt.append((fn[:-5], m.group(0).strip()))

print('Pruefe Sichtkaesten der Mehrblockbauten ... %d Darsteller, %d an einem Mehrblockbau'
      % (len(paare), geprueft))
if gemerkt:
    print('  AUFFAELLIG: %d Darsteller merken sich einen Kasten im Feld' % len(gemerkt))
    for name, zeile in gemerkt:
        print('  %s: "%s" -- ein Darsteller wird von allen Maschinen seines Typs geteilt, ein Kasten in Weltkoordinaten gilt nur fuer eine' % (name, zeile))
if fehlend:
    print('  AUFFAELLIG: %d' % len(fehlend))
    for be, dar in fehlend:
        print('  %s (%s): kein getRenderBoundingBox -- das Modell verschwindet, sobald der Kernblock aus dem Bild faellt' % (be, dar))

if fehlend or gemerkt:
    sys.exit(1)
print('OK - jeder Mehrblockbau mit eigenem Darsteller hat einen Sichtkasten, und keiner merkt ihn sich.')
PYEOF
