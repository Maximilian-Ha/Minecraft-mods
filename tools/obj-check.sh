#!/usr/bin/env bash
#
# Prueft jedes OBJ-Modell, das der Code mit new HFRWavefrontObject("...") laedt, gegen die
# Regeln des Laders -- so, wie der Client es beim Start tut.
#
# WARUM: die Modelle werden in NuclearTechModClient.onClientSetup geladen, also NUR auf dem
# Client. Der Rauchtest in CI startet einen dedizierten Server und sieht davon nichts. Ein
# einziges Modell, das der Lader ablehnt, bricht die verzoegerte Aufgabe ab, und das Spiel
# startet ueberhaupt nicht mehr.
#
# Gefunden im Protokoll eines echten Spielstarts (Runde 306): rbmk_element.obj enthaelt in
# EINER Gruppe Drei- und Vierecke. Das Original laedt die Datei darum mit mixedMode=true;
# der Port hatte das Flag weggelassen, und der Lader warf
#   "Invalid number of points for face (expected 4, found 3)".
#
# GEPRUEFT wird, was HFRWavefrontObject.loadObjModel/parseFace ablehnt oder was beim
# Hochladen in einen Puffer (baueGruppe) scheitert:
#   - v/vn/vt/f/g/o-Zeilen, die nicht auf die Muster des Laders passen (etwa 1e-05)
#   - Flaechenindizes jenseits der bis dahin gelesenen Ecken, Normalen, Texturkoordinaten
#   - Drei- und Vierecke in einer Gruppe, wenn der Aufruf nicht mixedMode (", true") setzt
#   - bei Modellen, die per .asVBO() oder .getRenderer() hochgeladen werden (beide gehen
#     durch baueGruppe): Flaechen ohne Normale (die Ecke liest face.vertexNormals[i]) und
#     Gruppen ohne Flaeche (ein leerer Puffer wirft)
#
# NACHGEMESSEN (Runde 306): 0 Funde bei 312 Ladeaufrufen. Gegenproben:
#   - bei rbmk_element das ", true" heraus -> erster Fund Zeile 738, dieselbe Zeile, die im
#     Absturzprotokoll steht (dazu 23 weitere Dreiecke der Gruppe 'Cap')
#   - den auskommentierten shimmer_sledge-Aufruf einkommentieren -> Gruppe ohne Flaeche
#   - in sphere.obj eine Ecke als 1e-05, eine Flaeche ohne Normale und einen Index 99999
#     einsetzen -> alle drei gemeldet
#
set -u

cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, pathlib, sys

A = re.ASCII
NUM = r'(\-){0,1}\d+(\.\d+)?'
V   = re.compile(r'v( ' + NUM + r'){3,4} *', A)
VN  = re.compile(r'vn( ' + NUM + r'){3,4} *', A)
VT  = re.compile(r'(vt( (\-){0,1}\d+\.\d+){2,3} *)|(vt( ' + NUM + r'){2,3} *)', A)
F_VTN = re.compile(r'f( \d+/\d+/\d+){3,4} *', A)
F_VT  = re.compile(r'f( \d+/\d+){3,4} *', A)
F_VN  = re.compile(r'f( \d+//\d+){3,4} *', A)
F_V   = re.compile(r'f( \d+){3,4} *', A)
G   = re.compile(r'[go]( [\w\d\.]+) *', A)

ASSETS = pathlib.Path('src/main/resources/assets/hbmsntm')
AUFRUF = re.compile(r'new HFRWavefrontObject\("([^"]+)"(\s*,\s*(true|false))?\)(\S*)')

def pruefe(pfad, gemischt_erlaubt, vbo):
    funde = []
    nv = nvn = nvt = 0
    gruppen = []          # [name, modus, flaechen, zeile]
    aktuell = None
    for nr, roh in enumerate(pfad.read_text(encoding='utf-8', errors='replace').splitlines(), 1):
        z = ' '.join(roh.split())
        if not z or z.startswith('#'):
            continue
        def falsch():
            funde.append(f'{pfad}:{nr}: Zeile passt nicht auf das Muster des Laders: {z!r}')
        if z.startswith('v '):
            if not V.fullmatch(z): falsch()
            elif len(z.split()) == 4: nv += 1
        elif z.startswith('vn '):
            if not VN.fullmatch(z): falsch()
            elif len(z.split()) == 4: nvn += 1
        elif z.startswith('vt '):
            if not VT.fullmatch(z): falsch()
            elif len(z.split()) in (3, 4): nvt += 1
        elif z.startswith('f '):
            if aktuell is None:
                aktuell = ['Default', None, 0, nr]
            teile = z.split()[1:]
            if F_VTN.fullmatch(z):   art, grenzen = '/',  (nv, nvt, nvn)
            elif F_VT.fullmatch(z):  art, grenzen = '/',  (nv, nvt)
            elif F_VN.fullmatch(z):  art, grenzen = '//', (nv, nvn)
            elif F_V.fullmatch(z):   art, grenzen = None, (nv,)
            else:
                falsch(); continue
            for t in teile:
                idx = t.split(art) if art else [t]
                for i, g in zip(idx, grenzen):
                    if int(i) < 1 or int(i) > g:
                        funde.append(f'{pfad}:{nr}: Index {i} ausserhalb der {g} bisher gelesenen Eintraege')
            if vbo and not (F_VTN.fullmatch(z) or F_VN.fullmatch(z)):
                funde.append(f'{pfad}:{nr}: Flaeche ohne Normale, das Hochladen liest vertexNormals[i]')
            n = len(teile)
            modus = 'T' if n == 3 else 'Q'
            if not gemischt_erlaubt:
                if aktuell[1] is None:
                    aktuell[1] = modus
                elif aktuell[1] != modus:
                    funde.append(f'{pfad}:{nr}: Drei- und Vierecke in Gruppe {aktuell[0]!r}, '
                                 'aber der Aufruf setzt kein mixedMode (", true")')
            aktuell[2] += 1
        elif z.startswith('g ') or z.startswith('o '):
            if not G.fullmatch(z):
                falsch(); continue
            if aktuell is not None:
                gruppen.append(aktuell)
            aktuell = [z[2:], None, 0, nr]
    gruppen.append(aktuell)
    if vbo:
        for g in gruppen:
            if g is None:
                funde.append(f'{pfad}: keine einzige Gruppe/Flaeche, das Hochladen scheitert')
            elif g[2] == 0:
                funde.append(f'{pfad}:{g[3]}: Gruppe {g[0]!r} ohne Flaeche, ein leerer Puffer wirft')
    return funde

# Auskommentierte Aufrufe laedt niemand -- etwa shimmer_sledge, dessen Datei eine Gruppe
# ohne Flaeche hat und darum nicht hochgeladen werden koennte.
def ohne_kommentare(text):
    return re.sub(r'/\*.*?\*/|//[^\n]*', '', text, flags=re.S)

quellen = {d: ohne_kommentare(d.read_text(encoding='utf-8', errors='replace'))
           for d in sorted(pathlib.Path('src/main/java').rglob('*.java'))}

funde = []
aufrufe = 0
for datei, text in quellen.items():
    for m in AUFRUF.finditer(text):
        aufrufe += 1
        pfad = ASSETS / m.group(1)
        if not pfad.is_file():
            continue  # fehlende Dateien meldet asset-check.sh
        funde += pruefe(pfad, m.group(3) == 'true', m.group(4).startswith(('.asVBO()', '.getRenderer()')))

gesamt = sum(len(re.findall(r'new HFRWavefrontObject\(', text)) for text in quellen.values())
if gesamt != aufrufe:
    funde.append(f'{gesamt - aufrufe} Aufruf(e) von new HFRWavefrontObject( ohne Pfad als Zeichenkette -- '
                 'das Tor sieht sie nicht an; Muster erweitern')

for f in sorted(set(funde)):
    print(f)
print(f'{aufrufe} Ladeaufrufe geprueft, {len(set(funde))} Funde')
sys.exit(1 if funde else 0)
PY
