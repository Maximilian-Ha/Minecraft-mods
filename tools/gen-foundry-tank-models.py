#!/usr/bin/env python3
"""
gen-foundry-tank-models.py -- erzeugt Modelle und Zustandsdatei des Giessereitanks.

WARUM ES DIESES WERKZEUG GIBT: der Tank hat im Original keine Modelldatei, sondern einen
ISimpleBlockRenderingHandler, der seine Flaechen je nach Nachbarschaft einzeln zeichnet. Auf
1.21 wird daraus eine Multipart-Zustandsdatei -- und die hat 81 Zeilen und 23 Modelle, weil
sich vier Dinge unabhaengig voneinander auswirken:

  * an welchen der sechs Seiten ein weiterer Tank steht -- dort faellt die Wand weg, damit
    beide Behaelter ineinander laufen,
  * ob unten einer steht          -- dann traegt die Aussenwand das Bild ohne Sockel (_upper),
  * ob oben einer steht           -- dann tragen die Innenflaechen _bottom statt _inner,
  * ob seitlich ein Ausguss haengt, der zu uns zeigt -- dann hat die Wand ein Loch (_outlet).

Von Hand waere das nicht zu pflegen. Das Skript schreibt die Dateien; im Baum liegen sie
trotzdem, wie bei der ganzen uebrigen Giesserei auch.

Aufbau: Boden, vier Wandstuecke und vier Ecken, alle ueberschneidungsfrei. Das Original laesst
seine Waende ueber die volle Breite laufen und regelt die Ecken ueber bedingte Flaechen; in
einer Modelldatei ginge das nicht, weil zwei deckungsgleiche Flaechen MIT GLEICHER NORMALEN
flackern. Mit Ecken als eigenen Stuecken stossen nur Flaechen mit ENTGEGENGESETZTER Normalen
aneinander, und die schluckt die Rueckseitenaussonderung.

Aufruf: tools/gen-foundry-tank-models.py
"""

import json
import os

WURZEL = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
MODELLE = os.path.join(WURZEL, 'src/main/resources/assets/hbmsntm/models/block')
ZUSTAENDE = os.path.join(WURZEL, 'src/main/resources/assets/hbmsntm/blockstates')
GEGENSTAND = os.path.join(WURZEL, 'src/main/resources/assets/hbmsntm/models/item')

NS = 'hbmsntm:block/'

SEITEN = ['north', 'east', 'south', 'west']
DREHUNG = {'north': 0, 'east': 90, 'south': 180, 'west': 270}

BODEN = {'from': [0, 0, 0], 'to': [16, 2, 16],
         'faces': {'down': {'texture': '#bottom', 'cullface': 'down'},
                   'up': {'texture': '#bottom'}}}

WAND = {'from': [2, 0, 0], 'to': [14, 16, 2],
        'faces': {'north': {'texture': '#outer', 'cullface': 'north'},
                  'south': {'texture': '#inner'},
                  'up': {'texture': '#top'}}}

ECKE = {'from': [14, 0, 0], 'to': [16, 16, 2],
        'faces': {'north': {'texture': '#a', 'cullface': 'north'},
                  'east': {'texture': '#b', 'cullface': 'east'},
                  'south': {'texture': '#c'},
                  'west': {'texture': '#c'},
                  'up': {'texture': '#top'}}}


def aussen(unten, ausguss):
    """Aussenbild der Wand: mit Sockel oder ohne, mit Loch fuer den Ausguss oder ohne."""
    return ('foundry_tank_upper' if unten else 'foundry_tank_side') + ('_outlet' if ausguss else '')


def innen(oben):
    """Innenbild: steht oben ein Tank, laeuft der Behaelter weiter und die Lippe waere falsch."""
    return 'foundry_tank_bottom' if oben else 'foundry_tank_inner'


def schreibe(ordner, name, inhalt):
    with open(os.path.join(ordner, name + '.json'), 'w') as f:
        f.write(json.dumps(inhalt, indent=2) + '\n')


def eltern(name, element):
    schreibe(MODELLE, name, {
        'parent': 'block/block',
        'textures': {'particle': NS + 'foundry_tank_side', 'top': NS + 'foundry_tank_top'},
        'elements': [element],
    })


def kind(name, elternname, texturen):
    schreibe(MODELLE, name, {
        'parent': NS + elternname,
        'textures': {platz: NS + bild for platz, bild in texturen.items()},
    })


def anwenden(modell, drehung):
    eintrag = {'model': NS + modell}
    if drehung:
        eintrag['y'] = drehung
    return eintrag


def gedreht(element, drehung):
    """Dreht ein Element um die Y-Achse -- gebraucht fuers Gegenstandsmodell, das alle neun
    Stuecke in EINER Datei fuehrt und deshalb nicht ueber die Zustandsdatei drehen kann."""

    if drehung == 0:
        return json.loads(json.dumps(element))

    umlauf = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north'}
    schritte = drehung // 90

    def dreh_richtung(richtung):
        for _ in range(schritte):
            richtung = umlauf[richtung]
        return richtung

    def dreh_punkt(x, z):
        for _ in range(schritte):
            x, z = 16 - z, x
        return x, z

    x1, y1, z1 = element['from']
    x2, y2, z2 = element['to']
    ax, az = dreh_punkt(x1, z1)
    bx, bz = dreh_punkt(x2, z2)

    neu = {'from': [min(ax, bx), y1, min(az, bz)], 'to': [max(ax, bx), y2, max(az, bz)], 'faces': {}}
    for richtung, flaeche in element['faces'].items():
        neue_richtung = dreh_richtung(richtung) if richtung in umlauf else richtung
        kopie = dict(flaeche)
        if 'cullface' in kopie and kopie['cullface'] in umlauf:
            kopie['cullface'] = dreh_richtung(kopie['cullface'])
        neu['faces'][neue_richtung] = kopie
    return neu


def gegenstandsmodell():
    """Der Tank in der Hand: alle vier Waende, alle vier Ecken, Boden -- also der Zustand ohne
    jeden Nachbarn. Ein Multipart laesst sich nicht als Gegenstand zeichnen."""

    elemente = [BODEN]
    for seite in SEITEN:
        elemente.append(gedreht(WAND, DREHUNG[seite]))
        elemente.append(gedreht(ECKE, DREHUNG[seite]))

    schreibe(MODELLE, 'foundry_tank_inventory', {
        'parent': 'block/block',
        'textures': {
            'particle': NS + 'foundry_tank_side',
            'top': NS + 'foundry_tank_top',
            'bottom': NS + 'foundry_tank_bottom',
            'outer': NS + aussen(False, False),
            'inner': NS + innen(False),
            'a': NS + aussen(False, False),
            'b': NS + aussen(False, False),
            'c': NS + innen(False),
        },
        'elements': elemente,
    })

    schreibe(GEGENSTAND, 'foundry_tank', {'parent': NS + 'foundry_tank_inventory'})


def main():

    eltern('foundry_tank_floor', BODEN)
    eltern('foundry_tank_wall', WAND)
    eltern('foundry_tank_corner', ECKE)

    # Der Boden traegt sein Bild selbst, er hat keine Spielarten.
    kind('foundry_tank_floor_plain', 'foundry_tank_floor', {'bottom': 'foundry_tank_bottom'})

    teile = [{'when': {'down': 'false'}, 'apply': anwenden('foundry_tank_floor_plain', 0)}]

    # Die vier Wandstuecke.
    for seite in SEITEN:
        for unten in (False, True):
            for oben in (False, True):
                for ausguss in (False, True):
                    name = 'foundry_tank_wall_%s_%s%s' % (
                        'upper' if unten else 'lower',
                        'open' if oben else 'closed',
                        '_outlet' if ausguss else '')
                    kind(name, 'foundry_tank_wall', {'outer': aussen(unten, ausguss), 'inner': innen(oben)})
                    teile.append({
                        'when': {seite: 'false', 'down': str(unten).lower(), 'up': str(oben).lower(),
                                 'outlet_' + seite: str(ausguss).lower()},
                        'apply': anwenden(name, DREHUNG[seite]),
                    })

    # Die vier Ecken. Jede steht, sobald mindestens eine der beiden Waende neben ihr steht --
    # ausgeschrieben als die drei Faelle, damit die Zustandsdatei ohne "OR" auskommt.
    for i, seite in enumerate(SEITEN):
        naechste = SEITEN[(i + 1) % 4]          # #a liegt an "seite", #b an "naechste"
        for a_verbunden, b_verbunden in ((False, False), (False, True), (True, False)):
            for unten in (False, True):
                for oben in (False, True):
                    name = 'foundry_tank_corner_%s%s_%s_%s' % (
                        'i' if a_verbunden else 'o', 'i' if b_verbunden else 'o',
                        'upper' if unten else 'lower',
                        'open' if oben else 'closed')
                    kind(name, 'foundry_tank_corner', {
                        'a': innen(oben) if a_verbunden else aussen(unten, False),
                        'b': innen(oben) if b_verbunden else aussen(unten, False),
                        'c': innen(oben),
                    })
                    teile.append({
                        'when': {seite: str(a_verbunden).lower(), naechste: str(b_verbunden).lower(),
                                 'down': str(unten).lower(), 'up': str(oben).lower()},
                        'apply': anwenden(name, DREHUNG[seite]),
                    })

    gegenstandsmodell()

    schreibe(ZUSTAENDE, 'foundry_tank', {'multipart': teile})

    dateien = len([n for n in os.listdir(MODELLE) if n.startswith('foundry_tank')])
    print('%d Multipart-Zeilen, %d Modelle geschrieben.' % (len(teile), dateien))


if __name__ == '__main__':
    main()
