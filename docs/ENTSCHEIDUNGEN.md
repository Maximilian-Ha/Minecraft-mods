# Entscheidungen

Was dieser Port bewusst anders macht als das Original, und warum.

## Die Tafeln brauchen keinen Strom

Im Original zog jede Informationstafel EU aus einem IC2-Netz und blieb ohne Strom dunkel.

IC2 gibt es auf 1.21.1 nicht. Die Tafel stattdessen an HBMs Stromnetz zu hängen, hätte den
**Kern** des Mods von HBM abhängig gemacht — dann ließe er sich ohne HBM nicht mehr laden, und
die weiche Anbindung wäre hinfällig. Eine eigene Energie über die Forge-Energie-Schnittstelle
wäre möglich, hätte aber in einer Welt mit HBM zwei Stromsysteme nebeneinander gestellt.

Die Tafeln arbeiten deshalb ohne Strom. Sollte sich das als zu billig erweisen, ist der
naheliegende Weg eine optionale Stromaufnahme über die NeoForge-Schnittstelle, abschaltbar in
der Konfiguration.

## Eine Kartenart ist ein eigener Gegenstand

Im Original war jede Kartenart ein Schadenswert desselben Gegenstands (`itemCard` mit
`ItemCardType.CARD_ENERGY = 0` und so fort). Schadenswerte als Unterscheidung gibt es auf
1.21.1 nicht mehr. Jede Karte ist deshalb ein eigener Gegenstand mit eigenem Registriernamen.

Folge: alte Welten lassen sich nicht übernehmen — was ohnehin nicht ginge, zwölf
Minecraft-Fassungen später.

## Der Datenspeicher der Karte liegt in `custom_data`

Auf 1.21.1 hat ein Gegenstand kein freies NBT mehr, sondern Datenbestandteile. Der Beutel der
Karte liegt daher in `minecraft:custom_data` — derselbe Weg, den auch der HBM-Port für seine
gegenstandsgebundenen Daten nimmt. Die Karten merken davon nichts; sie sehen weiter nur
benannte Felder über `ICardReader`.

## Alle Schalter laufen über `clickMenuButton`

Das Original brauchte für jeden Schalter in einer Oberfläche ein eigenes Netzwerkpaket
(`PacketCard`, `PacketKeys`, `PacketAlarm`). Minecraft hat dafür längst einen Weg:
`AbstractContainerMenu.clickMenuButton`. Der Port benutzt ihn durchgehend und kommt damit
**ganz ohne eigene Netzwerkpakete** aus.

Der Preis: wo das Original Textfelder und Schieberegler hatte, stehen hier Knöpfe.

- Der **Bereichsmelder** bekommt statt zweier Zahlenfelder je ein Knopfpaar pro Schrittweite.
- Der **Wärmemelder** schaltet durch neun feste Schwellen statt freier Eingabe.
- Der **Heulalarm** hat gar keine Oberfläche mehr: Rechtsklick schaltet den Ton weiter,
  Rechtsklick im Schleichen die Hörweite.
- Die **Überschrift einer Karte** kommt aus ihrem Namen — man benennt sie im Amboss. Das gilt
  auch für die Textkarte, deren ganzer Zweck eine feste Zeile ist.

Ein eigenes Paket nachzurüsten ist jederzeit möglich; siehe [`ROADMAP.md`](ROADMAP.md).

## Zeilen sind `Component`, nicht `String`

`PanelString` trug im Original fertig übersetzte Zeichenketten. Auf 1.21.1 erzeugt die Tafel
ihre Zeilen auf **beiden** Seiten — der Server kennt die Sprache des Spielers nicht. Die
Zeilen sind deshalb `Component`; übersetzt wird erst beim Zeichnen.

## Eigene Übersetzungsschlüssel statt HBMs

Die HBM-Karte könnte HBMs eigene Schlüssel benutzen (`trait.rbmk.melt`, `geiger.chunkRad`).
Sie tut es nicht: zwei Mods, die denselben Schlüssel belegen, überschreiben einander in nicht
festgelegter Reihenfolge. Alle Zeilen dieses Mods stehen unter `msg.ec.…`.

## Reflexion nur an einer Stelle

Die Anbindung greift typsicher auf HBM zu — mit einer Ausnahme: `progress`, `maxProgress`,
`consumption`, `heat` und `isOn` gewöhnlicher Maschinen. Die Begründung steht in
[`HBM-KOMPATIBILITAET.md`](HBM-KOMPATIBILITAET.md).
