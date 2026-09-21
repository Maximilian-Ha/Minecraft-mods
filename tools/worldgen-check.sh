#!/usr/bin/env bash
#
# Prueft, dass eine Weltgenerator-Klasse (Feature) sich nicht den echten ServerLevel holt.
#
# Hintergrund: ein Feature laeuft auf dem worldgen-Faden und bekommt einen WorldGenLevel.
# Der ist absichtlich eingeschraenkt -- er schreibt in die Chunks, die gerade entstehen.
# Wer sich von dort mit getLevel() den echten ServerLevel holt und darauf schreibt, loest
# Chunk-Ladungen aus, die ihrerseits auf den worldgen-Faden warten. Das Ergebnis ist kein
# Absturz, sondern STILLSTAND: der Server bleibt in der Vorbereitung des Startgebiets
# stehen und kommt nie heraus.
#
# Gefunden in CI 493: GlyphidHiveFeature rief GlyphidHive.generateSmall(level.getLevel(),...).
# Zwanzig Minuten Laufzeit, keine einzige Ausnahme im Protokoll, "Preparing spawn area: 34%"
# als letzte Zeile. Ein Fehler, den man an nichts festmachen kann, wenn man nicht weiss,
# wonach man sucht -- darum dieses Tor.
#
# WARUM KEIN ANDERES TOR DAS SIEHT: es uebersetzt sauber, es ist kein Namensproblem, kein
# fehlender Verweis. Erst der Server-Test in CI faellt darueber, und der braucht zwanzig
# Minuten, um in die Zeitgrenze zu laufen.
#
# WIE GEMESSEN WIRD: jede Datei unter world/feature, die von Feature erbt, wird nach
# getLevel() durchsucht -- ausserhalb von Kommentaren. Die Abgrenzung ist grob, aber in die
# sichere Richtung: sie meldet nur, was wirklich dasteht.
#
# NACHGEMESSEN (Runde 305): 0 Funde bei 8 geprueften Features. Setzt man den Aufruf aus
# CI 493 wieder ein, meldet das Tor genau GlyphidHiveFeature.java.
#
set -u

cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, pathlib, sys

funde = []
geprueft = 0

for datei in sorted(pathlib.Path('src/main/java/com/hbm/world').rglob('*.java')):

    text = datei.read_text(encoding='utf-8', errors='replace')

    if not re.search(r'\bextends\s+Feature\s*<', text): continue
    geprueft += 1

    # Kommentare fort, damit eine Warnung ueber getLevel nicht selbst anschlaegt.
    ohne = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    ohne = re.sub(r'//[^\n]*', '', ohne)

    for nr, zeile in enumerate(ohne.split('\n'), 1):
        if re.search(r'\.\s*getLevel\s*\(\s*\)', zeile):
            funde.append((str(datei), nr, zeile.strip()[:100]))

if funde:
    print('Weltgenerator greift auf die echte Welt zu:')
    for datei, nr, zeile in funde:
        print(f'  {datei}:{nr}: {zeile}')
    print()
    print('Ein Feature laeuft auf dem worldgen-Faden. getLevel() liefert dort den echten')
    print('ServerLevel; darauf zu schreiben laesst den Server in der Vorbereitung des')
    print('Startgebiets haengen -- ohne Ausnahme, ohne Hinweis. Den WorldGenLevel benutzen.')
    sys.exit(1)

print(f'worldgen-check: {geprueft} Features geprueft, keiner greift auf die echte Welt zu.')
PY
