#!/usr/bin/env bash
#
# Prueft, ob ein statisches Feld in seiner Initialisierung ein statisches Feld DERSELBEN
# Klasse benutzt, das erst weiter unten erklaert wird. Java verbietet das: "illegal forward
# reference".
#
# Hintergrund: tools/syntax-check.sh uebersetzt ohne Minecraft-Klassenpfad und kann deshalb
# nur wenige Meldungen glauben; tools/import-check.sh kennt nur Erreichbarkeit von Typen.
# Ein Vorwaertsverweis ist keins von beidem -- er kostete in Runde 179 einen CI-Lauf:
# LAMBDA_LILMAC_ANIMS stand in XFactory44 ueber LAMBDA_NOPIP_ANIMS und griff darauf zu.
#
# Der Fall ist ohne Klassenpfad entscheidbar, denn beide Seiten stehen in derselben Datei.
# Gebraucht wird nur eine saubere Trennung von Feldinitialisierern und Methodenruempfen:
# ein '}' auf Klassenebene, dem ein ';' folgt, beendet einen Feldinitialisierer; ein '}'
# ohne folgendes ';' beendet eine Methode oder innere Klasse, und was davor angesammelt
# wurde, ist kein Feld.
#
# NACHGEMESSEN (Runde 180): ueber den ganzen Baum null Funde. Setzt man das Feld aus
# Runde 179 wieder an seinen alten Platz, meldet das Gate genau es -- und sonst nichts.
#
# GRENZE, die bleibt: geprueft wird nur die aeussere Klasse jeder Datei. Innere Klassen mit
# eigenen statischen Feldern sieht dieses Werkzeug nicht.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

ROOT = 'src/main/java'


def strip(src):
    """Zeichenketten, Zeichenliterale und Kommentare in einem Durchgang entfernen -- dieselbe
    Aufbereitung wie in tools/import-check.sh, damit ein Semikolon in einem Kommentar die
    Zerlegung nicht zerreisst."""
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '"' or c == "'":
            quote = c
            i += 1
            while i < n:
                if src[i] == '\\': i += 2; continue
                if src[i] == quote: i += 1; break
                i += 1
            out.append(' ')
            continue
        if c == '/' and i + 1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n': i += 1
            continue
        if c == '/' and i + 1 < n and src[i+1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i+1] == '/'): i += 1
            i += 2
            out.append(' ')
            continue
        out.append(c)
        i += 1
    return ''.join(out)


# Name eines statischen Feldes MIT Initialisierung, aus dem Text vor dem ersten '='.
FELD = re.compile(r'\b(?:public|protected|private)?\s*static\s+(?:final\s+)?[A-Za-z_][\w.<>,\[\]\s?]*?\b(\w+)\s*=')

probleme = []
dateien = 0
felder_gesamt = 0

for wurzel, _dirs, namen in os.walk(ROOT):
    for fn in sorted(namen):
        if not fn.endswith('.java'): continue
        path = os.path.join(wurzel, fn)
        body = strip(open(path, encoding='utf-8').read())
        klasse = fn[:-5]

        kopf = re.search(r'\b(?:class|enum|interface|record)\s+' + re.escape(klasse) + r'\b[^{]*\{', body)
        if not kopf: continue
        dateien += 1

        # Den Klassenrumpf auf Ebene 1 in Stuecke zerlegen. Jedes ';' auf Ebene 1 schliesst
        # ein Stueck ab; ein Rumpf, der nicht zu einem Feld gehoert, wird verworfen.
        i, tiefe, n = kopf.end(), 1, len(body)
        stueck = []
        felder = []                      # (name, text der initialisierung)

        while i < n and tiefe > 0:
            c = body[i]
            if c == '{':
                tiefe += 1
                stueck.append(c)
            elif c == '}':
                tiefe -= 1
                if tiefe == 1:
                    j = i + 1
                    while j < n and body[j].isspace(): j += 1
                    if j < n and body[j] == ';':
                        stueck.append(c)
                    else:
                        stueck = []      # Methode oder innere Klasse -- kein Feld
                elif tiefe >= 1:
                    stueck.append(c)
            elif c == ';' and tiefe == 1:
                text = ''.join(stueck)
                treffer = FELD.search(text)
                if treffer and 'static' in text.split('=')[0]:
                    felder.append((treffer.group(1), text[text.index('=') + 1:]))
                stueck = []
            else:
                stueck.append(c)
            i += 1

        felder_gesamt += len(felder)
        namen_der_klasse = [f[0] for f in felder]

        for idx, (name, text) in enumerate(felder):
            spaeter = set(namen_der_klasse[idx+1:]) - {name}
            for ref in sorted(spaeter):
                if re.search(r'(?<![.\w])' + re.escape(ref) + r'\b', text):
                    probleme.append('%s: %s greift auf %s zu, das erst weiter unten erklaert '
                                    'wird -- illegal forward reference' % (path, name, ref))

print('Pruefe Vorwaertsverweise ... %d Klassen, %d statische Felder mit Initialisierung'
      % (dateien, felder_gesamt))
if probleme:
    print('  AUFFAELLIG: %d' % len(probleme))
    for p in probleme: print('  ' + p)
    sys.exit(1)
print('OK - kein statisches Feld greift auf ein spaeter erklaertes zu.')
PYEOF
