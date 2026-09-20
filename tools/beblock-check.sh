#!/usr/bin/env bash
# ---------------------------------------------------------------------------------------
# Tor 41: jeder Block, an den eine Blockentitaets-Art gebunden ist, muss auch eine
# Blockentitaet herausgeben koennen.
#
# Warum: BlockEntityType.Builder.of(X::new, NtmBlocks.Y.get()) sagt nur, WELCHE Bloecke
# diese Art tragen duerfen. Ob der Block selbst eine Blockentitaet erzeugt -- also
# EntityBlock ist und newBlockEntity ausfuellt --, prueft niemand. Ist er es nicht, laeuft
# alles weiter: es uebersetzt, es startet, und der Block steht still in der Welt. Aus einer
# Maschine wird ein Stein, und man sieht es erst im Spiel.
#
# Genau das ist in Runde 240 passiert: beim Anlegen des festen Schlackeblocks wurde die
# Datei SlagBlock.java ueberschrieben -- der Name war laengst vergeben, naemlich an die
# Schlackenpfuetze unter dem Abstich. Die neue Fassung war gueltiges Java und haette
# uebersetzt; aus der Pfuetze waere still ein gewoehnlicher Block geworden. Gefunden hat es
# nur git status, weil die Datei als geaendert statt als neu dastand.
#
# DIE REGEL. Fuer jeden Block, den NtmBlockEntityTypes nennt, wird aus NtmBlocks die Klasse
# herausgesucht, mit der er angemeldet ist. Diese Klasse muss in ihrer Vererbungskette im
# Projekt entweder newBlockEntity ausfuellen oder von einer der Vanilla-Klassen unten
# abstammen, die das ihrerseits tun.
#
# WORAUF DAS TOR NICHT SCHAUT: Bloecke, die nicht ueber NtmBlocks laufen, und Klassen aus
# Vanilla ausser den unten genannten. Geraten wird nichts.
#
# NACHGEMESSEN (Runde 240): 260 Arten, 283 gebundene Bloecke, null Funde. Zwei
# Gegenproben: meldet man den Schlackenabstich (foundry_slagtap) als schlichten Block an,
# meldet das Tor genau ihn; stellt man den Unfall selbst nach und nimmt SlagBlock sein
# newBlockEntity, meldet es die Schlackenpfuetze.
# ---------------------------------------------------------------------------------------
set -u
cd "$(dirname "$0")/.."

echo -n "Pruefe Blockentitaets-Bindungen ... "

funde=$(python3 - <<'PY'
import os, re

def ohne_kommentare(text):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

# Vanilla-Klassen, die newBlockEntity mitbringen. Wer von ihnen abstammt, ist versorgt.
VANILLA_MIT = {'BaseEntityBlock', 'BeaconBeamBlock', 'ChestBlock', 'AbstractFurnaceBlock',
               'AbstractChestBlock', 'SignBlock', 'SpawnerBlock', 'BedBlock'}

blocks_src = ohne_kommentare(open('src/main/java/com/hbm/blocks/NtmBlocks.java', encoding='utf-8').read())
betyp_src  = ohne_kommentare(open('src/main/java/com/hbm/blockentity/NtmBlockEntityTypes.java', encoding='utf-8').read())

# FELD -> Klassenname, mit dem der Block angemeldet ist.
feld_klasse = {}
for m in re.finditer(r'(?:DeferredBlock<[^>]*>|Supplier<[^>]*>)\s+([A-Z][A-Z0-9_]*)\s*=\s*[\w.]*register\w*\(\s*"[a-z0-9_]+"\s*,\s*\(\)\s*->\s*new\s+([\w.]+)', blocks_src):
    # Manche Anmeldungen schreiben den Klassennamen voll aus -- nur der letzte Teil zaehlt.
    feld_klasse[m.group(1)] = m.group(2).split('.')[-1]

# Alle Projektklassen: Name -> (Pfad, Elternname, fuellt newBlockEntity aus?)
klassen = {}
for wurzel, _, dateien in os.walk('src/main/java'):
    for d in dateien:
        if not d.endswith('.java'): continue
        pfad = os.path.join(wurzel, d)
        sauber = ohne_kommentare(open(pfad, encoding='utf-8', errors='replace').read())
        m = re.search(r'\n(?:public\s+)?(?:abstract\s+)?(?:final\s+)?class\s+(\w+)([^{]*)\{', sauber)
        if not m: continue
        name = m.group(1)
        kopf = m.group(2)
        vorher = None
        while vorher != kopf:
            vorher = kopf
            kopf = re.sub(r'<[^<>]*>', '', kopf)
        e = re.search(r'\bextends\s+([\w.]+)', kopf)
        eltern = e.group(1).split('.')[-1] if e else None
        hat = re.search(r'\bnewBlockEntity\s*\(', sauber) is not None
        klassen[name] = (pfad, eltern, hat)

# Jede Bindung: BlockEntityType.Builder.of(BE::new, NtmBlocks.A.get(), NtmBlocks.B.get(), ...)
gebunden = []
for m in re.finditer(r'BlockEntityType\.Builder\.of\((.*?)\)\s*\.build', betyp_src, re.S):
    for f in re.findall(r'NtmBlocks\.([A-Z][A-Z0-9_]*)\s*\.\s*get\(\)', m.group(1)):
        gebunden.append(f)

arten = len(re.findall(r'BlockEntityType\.Builder\.of\(', betyp_src))

funde = []
geprueft = 0
for feld in sorted(set(gebunden)):
    klasse = feld_klasse.get(feld)
    if klasse is None: continue          # Anmeldung nicht lesbar -- nicht geraten.
    geprueft += 1

    aktuell = klasse
    gesehen = set()
    versorgt = False
    while aktuell and aktuell not in gesehen:
        gesehen.add(aktuell)
        if aktuell in VANILLA_MIT:
            versorgt = True
            break
        if aktuell not in klassen: break
        _, eltern, hat = klassen[aktuell]
        if hat:
            versorgt = True
            break
        aktuell = eltern

    if not versorgt:
        pfad = klassen.get(klasse, ('unbekannt',))[0]
        funde.append('NtmBlocks.%s ist als %s angemeldet -- diese Klasse gibt keine Blockentitaet heraus (%s)'
                     % (feld, klasse, pfad))

print('ARTEN %d' % arten)
print('GEPRUEFT %d' % geprueft)
print('UNGELESEN %d' % (len(set(gebunden)) - geprueft))
for f in funde: print('FUND ' + f)
PY
)

arten=$(echo "$funde" | grep '^ARTEN ' | awk '{print $2}')
geprueft=$(echo "$funde" | grep '^GEPRUEFT ' | awk '{print $2}')
ungelesen=$(echo "$funde" | grep '^UNGELESEN ' | awk '{print $2}')
treffer=$(echo "$funde" | grep -c '^FUND ' || true)

echo "$arten Arten, $geprueft gebundene Bloecke geprueft ($ungelesen Anmeldungen nicht lesbar)"
echo "  ohne Blockentitaet : $treffer"

if [ "$treffer" -gt 0 ]; then
    echo
    echo "DIESE BLOECKE TRAGEN EINE BLOCKENTITAETS-ART, KOENNEN ABER KEINE HERAUSGEBEN:"
    echo "$funde" | grep '^FUND ' | sed 's/^FUND /  /'
    exit 1
fi

echo "OK - jeder gebundene Block gibt auch eine Blockentitaet heraus."
