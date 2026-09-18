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

## Die Mekanism-Schnittstellen kommen über ihre Namen

Mekanism führt Chemikalien, Joule und Wärme über drei eigene Capabilities. Die Brücke holt sie
nicht über Mekanisms interne Klasse `Capabilities`, sondern legt sie selbst über ihren Namen an:

```java
BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"),
                            IChemicalHandler.class)
```

Das Register von NeoForge gibt dieselbe Kennung zurück, wenn sie schon da ist — wer zuerst
kommt, legt sie an. Damit hängt die Brücke an drei API-Schnittstellen statt an einer internen
Klasse, und ihre Zugriffe stehen alle unter `mekanism.api`.

## Ein richtiger Rezepttyp für die Bausatzmontage

Das Original brachte für die Montage ein eigenes Rezeptregister mit, samt eigener Fabrik und
eigener JSON-Form (`KitAssemblerRecipe`, `KitAssemblerRecipeFactory`, `_factories.json`). Auf
1.21.1 gibt es dafür die Rezepttypen von Minecraft: ein `RecipeType`, ein `RecipeSerializer`
mit `MapCodec` und `StreamCodec`, und die Rezepte selbst als Dateien im Datenpaket.

Das kostet ein paar Zeilen mehr als eine fest verdrahtete Tabelle und bringt dafür alles mit,
was man davon erwartet: der Server liest sie aus dem Datenpaket, schickt sie an den Client, ein
Datenpaket kann sie ändern, und Rezeptbrowser wie JEI oder EMI finden sie ohne eigene
Anbindung.

Die Zutaten dürfen dabei liegen, wo sie wollen — im Original standen sie auf drei festen
Fächern. Zugeordnet wird durch Durchprobieren; bei höchstens drei Zutaten auf sechs Fächern
kostet das nichts.

## Karten im Gegenstand liegen in `custom_data`, nicht in `minecraft:container`

Kartenhalter und tragbare Tafel führen ein Inventar im Gegenstand. Der vorgesehene Weg dafür
wäre der Datenbestandteil `minecraft:container` — der hat aber zwei Eigenheiten, die hier
stören: er führt höchstens 256 Fächer, und er zeigt seinen Inhalt in der Kurzinfo des
Gegenstands an. Beides passt zu einem Halter mit vierundfünfzig Karten schlechter als der
eigene Beutel in `custom_data`, den der Mod für jede Karte ohnehin schon benutzt.

Der Gegenstand wird bei jedem Zugriff frisch aus der Hand geholt, nicht gemerkt: der Client
tauscht seinen `ItemStack` aus, sobald der Server ihn neu schickt. Sein Fach im
Spielerinventar ist gesperrt, solange die Oberfläche offen steht.

## Die fortgeschrittene Tafel ist ein gewöhnlicher Block

Im Original zeichnete ein eigener Renderer den ganzen Block als Netz aus Vierecken
(`RotationOffset`, rund 220 Zeilen, plus zwei Renderer). Nur deshalb war dort sowohl die Dicke
stufenlos als auch die Neigung frei.

Hier ist die Tafel ein gewöhnlicher Block mit gewöhnlichem Modell: die Dicke steht in sechzehn
Stufen im Blockzustand, je ein Modell. Dafür sieht sie aus wie jeder andere Block — mit Licht,
mit Schatten, und in jedem Ressourcenpaket austauschbar.

Die freie Neigung fehlt deshalb ganz. Eine geneigte *Schrift* auf einem ungeneigten Block wäre
schlechter als keine Neigung, und ein eigener Renderer für den Blockkörper lässt sich in dieser
Arbeitsumgebung nicht ansehen — also auch nicht verantworten.

## Die Blockmodelle sind Kästen mit Bildausschnitten, keine Würfel mit Kacheln

Die Dateien `*_all.png` des Originals sehen aus wie Blocktexturen, sind aber keine: sie sind
**Abwicklungen** eines Würfels, 128 × 128 groß, mit fünf Kacheln zu je 32 × 32 in Kreuzform.
Das Feld in der Mitte bleibt frei — dort sitzt die Schauseite, die als eigene Datei
`*_face.png` danebenliegt.

Die erste Fassung dieses Ports hat sie wie gewöhnliche Kacheln benutzt
(`models().orientable(...)`, `cubeAll(...)`). Das quetscht das ganze Kreuz auf jede Fläche:
im Spiel stehen dann schwarz-weiße Karos statt eines Geräts.

Die Modelle stehen deshalb als eigene Kästen im Datengenerator, mit denselben
Bildausschnitten wie im Original — `full_box`, `medium_box`, `small_box`. Die Texturen
bleiben unverändert; geändert hat sich nur, wie sie gelesen werden.

Daraus folgen zwei Bauformen mit zwei Drehkonventionen:

- **Schauseite im Norden** (`full_box`): Informationstafel, Erweiterungen, Bereichsmelder,
  Bausatzmontage, Fernwärmeanzeige. Waagerecht ist die Drehung `toYRot + 180` — dieselbe,
  die Minecraft für den Ofen benutzt und die NeoForge als Vorgabewinkel führt. Senkrecht
  dreht `facingBlock` um die X-Achse: `x = 270` hebt die Nordfläche nach oben, `x = 90` legt
  sie nach unten.
- **Schauseite oben** (`medium_box`, `small_box`, Energiezähler): Wärmemelder, Heuler,
  Warnleuchte. Das sind flache Kästen, die auf dem Boden liegen. Für sie passt
  `directionalBlock` von NeoForge unverändert — seine Drehungen sind Zeichen für Zeichen die
  Blockzustände des Originals.

Dass diese drei Blöcke flach sind, sagt jetzt auch ihr Körper (`BoxShape.slab`) und ihre
Blockeigenschaft `noOcclusion` — ein flacher Block, den Minecraft für einen vollen Würfel
hält, wirft Schatten, wo keine hingehören.


## Ein gesetzter Block schaut zum Spieler, nicht auf die angeklickte Fläche

Das Original richtet seine Blöcke nach dem **Blick des Spielers** aus: wer steil nach unten
schaut (ab 65 Grad), bekommt eine Tafel, die nach oben zeigt, sonst zeigt die Schauseite dem
Spieler entgegen (`FacingBlock#getStateForPlacement`).

Die erste Fassung dieses Ports nahm stattdessen die angeklickte Fläche. Das klingt
naheliegend und macht große Schirme unbaubar: wer eine Erweiterung oben auf die vorige setzt,
klickt deren Deckfläche an — der neue Block schaut dann nach oben und gehört nicht mehr zum
Schirm, der zur Seite zeigt. Genau so sah es im Spiel aus: die untere Reihe bildete einen
Schirm, die obere nicht.

Wer einen Block doch einmal falsch gesetzt hat, dreht ihn mit dem Tafelwerkzeug, statt ihn
abbauen zu müssen.

## Die Schrift wird auf den Schirm eingepasst

Der Renderer der ersten Fassung zeichnete in fester Größe (ein Block = 64 Schriftpunkte) und
brach die Zeilen nach Platz ab. Lange Zahlen liefen damit über den Rand der Tafel hinaus.

Das Original rechnet stattdessen einen Maßstab aus: die breiteste Zeile und die Zahl der
Zeilen gegen die Fläche, das Kleinere von beiden gewinnt. Dieselbe Rechnung steht jetzt hier
— mit der Folge, dass die Schrift auf einem großen Schirm größer wird statt bloß mehr Platz
zu haben.

## Den Schirm zeichnet der Renderer, nicht die Blocktextur

Die Schauseiten von Tafel und Erweiterung (`*_face.png`) sind ein zwei Pixel breiter
dunkelgrüner Rahmen um eine hellgrüne Füllung. Ließe man es dabei, sähe eine Tafel mit
Erweiterungen aus wie eine Mauer aus einzeln eingerahmten Kästen — und genau so sah es im
Spiel aus, solange dieser Port den Schirm der Blocktextur überließ.

Das Original legt über jede Blockfläche des Schirms ein Viereck aus `panel_screen.png`. Das
ist eine Kachelkarte aus vier mal vier Feldern: jedes Feld trägt an null bis vier Kanten
einen ein Pixel breiten Rahmen. Jeder Block bekommt das Feld, dessen Rahmen zu seiner Lage
im Schirm passt (`TileEntityInfoPanel#findTexture`), innen liegen die Felder rahmenlos
aneinander. Erst dadurch wird aus mehreren Blöcken ein Bildschirm.

Dasselbe macht jetzt `InfoPanelRenderer#drawScreen`. Die Kachelnummern des Originals sind
übernommen: 1 rechter Rand, 2 linker, 4 oberer, 8 unterer; die waagerechten Ränder wählen
die Spalte der Karte, die senkrechten die Zeile. Ohne Erweiterung ergibt das die 15, also
das Feld mit allen vier Rändern.

Das Viereck bekommt die Hintergrundfarbe der Tafel (die weißen Felder der Kachelkarte nehmen
sie voll an, die grauen Ränder als dunklere Abstufung) und, solange Strom da ist, volles
Licht. Das Original schaltete an dieser Stelle die Beleuchtung ab (`GlStateManager
.disableLighting()`); auf 1.21.1 gibt es keinen festen Beleuchtungsschalter mehr, wohl aber
`LightTexture.FULL_BRIGHT` — das Ergebnis ist dasselbe: die Tafel ist auch nachts lesbar.
Ohne Strom bekommt sie die Helligkeit ihres Platzes, ist also eine Fläche wie jede andere.

Gezeichnet wird über `RenderType.text(...)`, denn dessen Format
(`POSITION_COLOR_TEX_LIGHTMAP`) ist genau das, was hier gebraucht wird: eingefärbte Textur
mit Lichtkarte, aber ohne die richtungsabhängige Schattierung der Entity-Typen — sonst wäre
ein Schirm an der Wand dunkler als einer an der Decke.

## Mekanism-Werte stehen in FE, und der Faktor kommt von Mekanism

Mekanism zählt intern in Joule. Die erste Fassung der Anbindung schrieb diese Zahl samt
Einheit „J" auf die Tafel — richtig gegenüber Mekanisms eigener Anzeige, aber auf einer Tafel
irreführend: dort steht sie neben den Zahlen anderer Mods, und die rechnen in FE. Zwei
verschiedene Einheiten unter demselben Wort „Energie" sind eine Zahl, die man nicht
vergleichen kann.

`MekEnergy` rechnet deshalb um, bevor etwas in den Beutel der Karte geht. Der Faktor steht
nicht im Quelltext: er ist bei Mekanism einstellbar (Vorgabe 2,5 J je FE), und
`IEnergyConversionHelper.INSTANCE.feConversion()` ist Mekanisms öffentlicher Zugang zu genau
dem Wert, mit dem auch seine eigenen Kabel rechnen. Ist die Forge-Energie dort abgeschaltet,
bleibt es bei Joule — dann gibt es im Spiel nichts, worin umzurechnen wäre.

Die Einheit reist als `euType` im Beutel mit. `ItemCardMekanism` wird immer übersetzt, auch
ohne Mekanism-JARs, und darf deshalb keine Mekanism-Klasse anfassen; raten darf sie die
Einheit auch nicht.

Nicht behoben wird damit, warum die Brücke überhaupt in Joule liest statt über Forge Energy:
Forges `IEnergyStorage` zählt in `int` und klemmt bei gut zwei Milliarden ab. Eine
Induktionsmatrix speichert ein Vielfaches davon und stünde fest am Anschlag. Deshalb liest
die Brücke `IStrictEnergyHandler` (`long`) und rechnet selbst um.
