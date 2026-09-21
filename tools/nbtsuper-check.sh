#!/usr/bin/env bash
#
# Prueft, ob addAdditionalSaveData und readAdditionalSaveData den RICHTIGEN Aufruf an die
# Oberklasse machen -- also ob das Speichern super.addAdditionalSaveData ruft und das Laden
# super.readAdditionalSaveData, und nicht umgekehrt.
#
# Hintergrund: die beiden Methoden sehen sich zum Verwechseln aehnlich, sie nehmen dasselbe
# CompoundTag und stehen in jeder Entitaetsklasse direkt untereinander. Ein vertauschtes
# super ist syntaktisch einwandfrei, uebersetzt fehlerfrei und faellt in keinem der anderen
# 46 Tore auf. Es ist trotzdem ein Fehler mit Wirkung: ein super.readAdditionalSaveData im
# Speicherzweig liest aus dem noch leeren Etikett, schreibt also nichts aus der Oberklasse
# weg UND setzt deren Felder auf die Vorgabewerte zurueck. Bei einem Projectile ist das der
# Werfer, der damit beim Speichern verlorengeht.
#
# Gefunden in Runde 299 an com.hbm.entity.projectile.Rubble -- dort stand genau das.
#
# WIE GEMESSEN WIRD: zu jeder Erklaerung der beiden Methoden wird der Rumpf ueber die
# Klammerbilanz abgegrenzt und darin nach einem super-Aufruf der jeweils ANDEREN Methode
# gesucht. Eine Klasse, die beide Rumpfe hat, wird zweimal geprueft. Es gibt nichts zu raten:
# beide Namen stehen im Quelltext, der Fall ist ohne Klassenpfad entscheidbar.
#
# NACHGEMESSEN (Runde 299): 0 Funde bei 96 geprueften Rumpfen. Setzt man den Fehler aus
# Runde 299 wieder ein, meldet das Tor genau Rubble.java.
#
set -u

cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, pathlib, sys

GEGEN = {
    'addAdditionalSaveData': 'readAdditionalSaveData',
    'readAdditionalSaveData': 'addAdditionalSaveData',
}

erklaerung = re.compile(
    r'(?:public|protected|private)\s+void\s+(addAdditionalSaveData|readAdditionalSaveData)\s*\([^)]*\)\s*\{')

funde = []
geprueft = 0

for datei in sorted(pathlib.Path('src/main/java').rglob('*.java')):
    text = datei.read_text(encoding='utf-8', errors='replace')

    for treffer in erklaerung.finditer(text):
        name = treffer.group(1)
        anfang = treffer.end() - 1

        tiefe = 0
        ende = len(text)
        for i in range(anfang, len(text)):
            if text[i] == '{':
                tiefe += 1
            elif text[i] == '}':
                tiefe -= 1
                if tiefe == 0:
                    ende = i
                    break

        geprueft += 1
        rumpf = text[anfang:ende]
        gegen = GEGEN[name]

        if re.search(r'super\s*\.\s*' + gegen + r'\s*\(', rumpf):
            zeile = text[:treffer.start()].count('\n') + 1
            funde.append((str(datei), zeile, name, gegen))

if funde:
    print('Vertauschter Aufruf an die Oberklasse:')
    for datei, zeile, name, gegen in funde:
        print(f'  {datei}:{zeile}: {name} ruft super.{gegen} -- erwartet super.{name}')
    print(f'\n{len(funde)} Fund(e) bei {geprueft} geprueften Rumpfen.')
    sys.exit(1)

print(f'nbtsuper-check: {geprueft} Rumpfe geprueft, keine vertauschten Aufrufe.')
PY
