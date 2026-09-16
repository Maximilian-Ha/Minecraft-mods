# Entscheidungen

Was dieser Port bewusst anders macht als das Original, und warum.

## Die Tafeln brauchen Strom -- über Forge Energy

Im Original zog jede Informationstafel EU aus einem IC2-Netz und blieb ohne Strom dunkel.
IC2 gibt es auf 1.21.1 nicht. An seine Stelle tritt die Energie-Schnittstelle von NeoForge:
Tafel und Bereichsmelder führen einen Stromspeicher und melden ihn als
`Capabilities.EnergyStorage.BLOCK` an. Damit speist jede Mod ein, die Forge Energy abgibt --
Mekanism, Thermal, Immersive Engineering und alle anderen --, ohne dass hier eine Zeile je Mod
steht. Forge Energy steckt in NeoForge selbst, der Mod bekommt dadurch also **keine** neue
Abhängigkeit: eine Tafel ohne Kabel verhält sich wie eine Tafel ohne Strom.

Abgegeben wird nichts. Ein Kartenleser verbraucht Strom, er verteilt ihn nicht; `maxExtract`
ist null.

Wer den Strombedarf nicht will, schaltet ihn in der Konfiguration ab (`requirePower`). Dann
arbeiten beide Blöcke wie in der ersten Fassung dieses Ports.

**Die erste Fassung entschied anders, und das war falsch.** Die Begründung damals: eine eigene
Stromaufnahme stelle in einer Welt mit HBM zwei Systeme nebeneinander. Das tut sie -- aber das
ist kein Einwand, denn HBM stellt selbst zwei Systeme nebeneinander und hält dafür genau zwei
Wandlerblöcke bereit (`machine_converter_he_rf`, `machine_converter_rf_he`; im ganzen Port die
einzigen Übergänge). Richtig an der alten Überlegung war nur der Teil, der geblieben ist: die
Tafel an HBMs Stromnetz zu hängen hätte den **Kern** des Mods von HBM abhängig gemacht.

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

## Ein Paket für Werte, `clickMenuButton` für Schalter

Das Original brauchte für jeden Schalter in einer Oberfläche ein eigenes Netzwerkpaket
(`PacketCard`, `PacketKeys`, `PacketAlarm`). Minecraft hat dafür längst einen Weg:
`AbstractContainerMenu.clickMenuButton`. Alles, was nur "dieser Schalter wurde gedrückt"
bedeutet, läuft hier darüber -- Beschriftung an/aus, Takt weiterschalten, Redstone umkehren,
ein Ankreuzfeld einer Karte.

Für alles, was einen **Wert** mitbringt, genügt das nicht: eingetippter Text, eine Farbe, eine
Zahl. Dafür gibt es genau **ein** Paket, `PanelControl`, mit Blockposition und einem
NBT-Beutel; was gemeint ist, steht darin unter `action`. Drei Pakete wie im Original braucht
es nicht.

Die Rechteprüfung liegt beim Empfänger, nicht beim Paket (`IControlReceiver.hasPermission`):
nur der Block weiß, wer ihn bedienen darf. In der Regel ist das dieselbe Grenze, die Minecraft
für die Fächer zieht -- `stillValid`, also acht Blöcke. Ein Paket auf einen nicht geladenen
Chunk wird verworfen, statt ihn auf Zuruf vom Client zu laden.

**Die erste Fassung dieses Ports kam ganz ohne Paket aus** und behalf sich mit Knöpfen:
Schrittknöpfe statt Zahlenfeldern, sechzehn feste Farben zum Durchschalten, und der Text einer
Textkarte kam aus ihrem Namen im Amboss. Mit Stufe 4 ist das ersetzt.

## Zeilen sind `Component`, nicht `String`

`PanelString` trug im Original fertig übersetzte Zeichenketten. Auf 1.21.1 erzeugt die Tafel
ihre Zeilen auf **beiden** Seiten — der Server kennt die Sprache des Spielers nicht. Die
Zeilen sind deshalb `Component`; übersetzt wird erst beim Zeichnen.

## Eigene Übersetzungsschlüssel statt HBMs

Die HBM-Karte könnte HBMs eigene Schlüssel benutzen (`trait.rbmk.melt`, `geiger.chunkRad`).
Sie tut es nicht: zwei Mods, die denselben Schlüssel belegen, überschreiben einander in nicht
festgelegter Reihenfolge. Alle Zeilen dieses Mods stehen unter `msg.ec.…`.

## Der Heulalarm hat keine Oberfläche

Im Original wählte man Ton und Hörweite in einer eigenen Oberfläche mit Listenfeld und
Schieberegler. Hier geht beides am Block: Rechtsklick mit leerer Hand schaltet den Ton weiter,
Rechtsklick im Schleichen die Hörweite; was eingestellt ist, sagt eine Meldung über der
Schnellleiste. Für zwei Werte lohnt keine Oberfläche, und der Block braucht dadurch weder
Menü noch Fächer.

## Reflexion nur an einer Stelle

Die Anbindung greift typsicher auf HBM zu — mit einer Ausnahme: `progress`, `maxProgress`,
`consumption`, `heat` und `isOn` gewöhnlicher Maschinen. Die Begründung steht in
[`HBM-KOMPATIBILITAET.md`](HBM-KOMPATIBILITAET.md).
