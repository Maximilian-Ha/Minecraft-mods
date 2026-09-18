"""Teil von tools/facing-check.sh -- vergleicht die Ausrichtung mit dem Original.

Der Port schreibt den Ausrichtungsschalter in drei Formen, alle drei werden gelesen:

  (a) Anweisung, alle vier Faelle
        switch(facing) { case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F)); ... }
  (b) Anweisung, drei Faelle plus default
        switch(facing) { case NORTH -> ...(180F); ... default -> ...(0F); }
  (c) Ausdruck, der die Gradzahl liefert
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) { case NORTH -> 0F; ... default -> 270F; }));

Form (b) und (c) hatte die erste Fassung dieses Tors nicht gelesen und dreizehn Darsteller
stillschweigend uebersprungen -- der schlimmste Fehler, den ein Torwaechter machen kann.
"""
import os
import re
import sys

RICHTUNGEN = ['NORTH', 'EAST', 'SOUTH', 'WEST']
ORDNER = 'src/main/java/com/hbm/render/blockentity'
LISTE = 'tools/facing-list.txt'

# Der Schalter laeuft mal ueber eine Variable, mal ueber einen Ruf (switch(getFacing(be))).
SCHALTER_ANFANG = re.compile(r'switch\s*\([^;{]*\)\s*\{')
FALL = re.compile(r'(?:case\s+(NORTH|SOUTH|WEST|EAST)|(default))\s*->\s*([^;\n]*)')
GRAD_IM_RUF = re.compile(r'rotationDegrees\(\s*(-?[\d.]+)F?\s*\)')
GRAD_BLANK = re.compile(r'^\s*(-?[\d.]+)F?\s*$')
VORDREHUNG = re.compile(r'Axis\.Y(P|N)\.rotationDegrees\(\s*(-?[\d.]+)F?\s*\)')
# Form (c): der mulPose-Ruf steht VOR dem switch und gehoert nicht zur Vordrehung.
AUSDRUCKSFORM = re.compile(r'mulPose\(\s*Axis\.YP\.rotationDegrees\(\s*$')


def erwartung():
    werte = {}
    for zeile in open(LISTE, encoding='utf-8'):
        zeile = zeile.strip()
        if not zeile or zeile.startswith('#'):
            continue
        teile = zeile.split()
        werte[teile[0]] = {r: int(w) % 360 for r, w in zip(RICHTUNGEN, teile[1:5])}
    return werte


def block(quelle, ab):
    """Text zwischen der oeffnenden und der zugehoerigen schliessenden Klammer."""
    tiefe = 0
    for i in range(ab, len(quelle)):
        if quelle[i] == '{':
            tiefe += 1
        elif quelle[i] == '}':
            tiefe -= 1
            if tiefe == 0:
                return quelle[ab + 1:i]
    return None


def grad(text):
    m = GRAD_IM_RUF.search(text)
    if m:
        return float(m.group(1))
    m = GRAD_BLANK.match(text)
    return float(m.group(1)) if m else None


def gemessen(quelle):
    """Gesamtdrehung je Richtung, oder None wenn die Datei keinen lesbaren Schalter hat."""
    for treffer in SCHALTER_ANFANG.finditer(quelle):
        rumpf = block(quelle, treffer.end() - 1)
        if rumpf is None:
            continue
        werte = {}
        weite = None
        for m in FALL.finditer(rumpf):
            g = grad(m.group(3))
            if g is None:
                continue
            if m.group(2):
                weite = g
            else:
                werte[m.group(1)] = g
        if not werte:
            continue
        fehlend = [r for r in RICHTUNGEN if r not in werte]
        if fehlend:
            if weite is None:
                continue
            for r in fehlend:
                werte[r] = weite
        kopf = quelle[:treffer.start()]
        if AUSDRUCKSFORM.search(kopf):
            # Form (c): der umschliessende Ruf IST der Schalter, keine Vordrehung.
            kopf = kopf[:AUSDRUCKSFORM.search(kopf).start()]
        vor = sum(float(m.group(2)) * (1 if m.group(1) == 'P' else -1)
                  for m in VORDREHUNG.finditer(kopf))
        return {r: int((werte[r] + vor) % 360) for r in RICHTUNGEN}
    return None


def main():
    soll = erwartung()
    vorhanden = {f[:-5] for f in os.listdir(ORDNER) if f.endswith('.java')}

    verglichen = 0
    abweichend = []
    unlesbar = []

    for name in sorted(soll):
        if name not in vorhanden:
            continue
        quelle = open(os.path.join(ORDNER, name + '.java'), encoding='utf-8').read()
        ist = gemessen(quelle)
        if ist is None:
            unlesbar.append(name)
            continue
        verglichen += 1
        if ist != soll[name]:
            abweichend.append((name, ist, soll[name]))

    print('Pruefe Ausrichtung ... %d Darsteller in der Liste, %d davon vergleichbar'
          % (len(soll), verglichen))
    print('  abweichend          : %d' % len(abweichend))
    print('  Schalter unlesbar   : %d' % len(unlesbar))

    if not abweichend and not unlesbar:
        print('OK - jeder Darsteller steht so herum wie im Original.')
        return 0

    if unlesbar:
        print()
        print('SCHALTER NICHT LESBAR -- diese Darsteller wuerden stillschweigend')
        print('uebersprungen. Entweder die Schreibweise angleichen oder dieses Tor erweitern:')
        for name in unlesbar:
            print('   %s' % name)

    if abweichend:
        print()
        print('DIESE DARSTELLER STEHEN ANDERS HERUM ALS IM ORIGINAL:')
        for name, ist, sollwert in abweichend:
            if (ist['NORTH'] == sollwert['NORTH'] and ist['SOUTH'] == sollwert['SOUTH']
                    and ist['EAST'] == sollwert['WEST'] and ist['WEST'] == sollwert['EAST']):
                art = 'Osten/Westen vertauscht'
            else:
                versatz = {(ist[r] - sollwert[r]) % 360 for r in RICHTUNGEN}
                art = ('um %d Grad verdreht' % versatz.pop()) if len(versatz) == 1 else 'uneinheitlich'
            print('   %-24s %s' % (name, art))
            print('      ist : ' + '  '.join('%s %d' % (r[0] + r[1:3].lower(), ist[r]) for r in RICHTUNGEN))
            print('      soll: ' + '  '.join('%s %d' % (r[0] + r[1:3].lower(), sollwert[r]) for r in RICHTUNGEN))
        print()
        print('Gemessen wird die GESAMTDREHUNG -- eine Y-Drehung vor dem Schalter zaehlt mit.')
    return 1


sys.exit(main())
