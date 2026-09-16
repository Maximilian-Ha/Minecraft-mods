# Arbeitsstand

## Fertig

| Teil | Bemerkung |
| --- | --- |
| Projektgerüst, Bau, CI | NeoForge 21.1.228, Java 21, Parchment 2024.11.17 — wie der HBM-Port |
| Kartenwesen (`api`, `ItemCardReader`) | Datenspeicher der Karte liegt im Datenbestandteil `custom_data` |
| Informationstafel | Kartenfach, drei Aufwertungsfächer, Anzeige auf der Schauseite |
| Wärmemelder | findet HBM-Reaktoren in der Nachbarschaft |
| Bereichsmelder | vergleicht `energy`, ersatzweise `amount` |
| Heulalarm, Warnleuchte | Ton und Hörweite am Block einstellbar |
| Karten | Strom, Flüssigkeit, Inventar, Redstone, Vanilla, Zeit, Text, HBM |
| Bausätze | zu allen Karten mit Ziel |
| HBM-Anbindung | siehe [`HBM-KOMPATIBILITAET.md`](HBM-KOMPATIBILITAET.md) |
| Datengeneratoren | Blockzustände, Modelle, Sprache (en/de), Rezepte, Loot, Tags |

## Als Nächstes

| Teil | Warum es fehlt | Aufwand |
| --- | --- | --- |
| **Tafelerweiterungen** (`info_panel_extender`) | Große Schirme aus mehreren Blöcken. Braucht die Flächenerkennung (`ScreenManager` im Original, rund 430 Zeilen) und einen Renderer, der die Schrift über das Rechteck streckt. Die Texturen dafür liegen im Original bereit. | mittel |
| **Berührungsbetrieb** (`ITouchAction`, Umschaltkarte) | Rechtsklick auf den Schirm schaltet etwas am Ziel. Braucht einen Strahlentest auf die Schauseite und ein eigenes Netzwerkpaket. Die Berührungsaufwertung gibt es schon, sie tut nur noch nichts. | mittel |
| **Fernwärmeanzeige** | Wärmemelder, der seinen Reaktor über eine Karte findet statt über Nachbarschaft. | klein |
| **Zählerkarte und Energiezähler** | Durchsatzmessung. Braucht einen Block im Leitungsweg; bei HBM wäre das ein Anschluss ans Fernleitungsnetz. | mittel |
| **Bausatzmontage** (`kit_assembler`) | Im Original entstehen Bausätze in einer eigenen Maschine mit Strom und Rezeptbuch. Hier werden sie vorerst an der Werkbank gebaut. | mittel |
| **Textkarte mit Textfeld** | Zurzeit kommt der Text aus dem Namen der Karte (Amboss). Ein richtiges Textfeld braucht ein eigenes Netzwerkpaket. | klein |
| **Farbwahl mit Farbtafel** | Zurzeit schalten zwei Knöpfe durch sechzehn feste Farben. Das Original hat eine Farbtafel mit freier Wahl. | klein |
| **Kartenhalter, tragbare Tafel** | Gegenstände, die mehrere Karten führen bzw. eine Tafel in der Hand sind. | mittel |
| **Saatgutanalyse und -bibliothek** | Hängt im Original an IC2-Saatgut und hat auf 1.21.1 keine Entsprechung. | offen |

## Nicht vorgesehen

| Teil | Warum |
| --- | --- |
| Anbindungen an IC2, Mekanism, Thermal Expansion, Draconic Evolution, GregTech, AE2, EnderIO, PneumaticCraft, NuclearCraft, Galacticraft, Railcraft, Extreme Reactors | Keine dieser Mods ist auf 1.21.1 verfügbar. Wer eine davon portiert, kann eine Anbindung nach dem Muster von `crossmod/hbm` nachrüsten — dafür ist `CrossModBase` da. Was Forge-Energie und die Fluid-Schnittstelle von NeoForge anbietet, liest der Port ohnehin schon ohne eigene Anbindung. |
| ComputerCraft- und OpenComputers-Anbindung | Beide brauchen die jeweilige Mod; nachrüstbar, sobald sie da sind. |
| WebSocket-Server | Das Original schickt Tafelinhalte an eine Webseite. Steht keinem Spielinhalt im Weg und ist der Aufwand vorerst nicht wert. |
| `com.zuxelus.hooklib` | Ein eigener Bytecode-Weber aus 1.12.2-Zeiten. Auf 1.21.1 gibt es dafür Mixins, und der Port braucht ihn an keiner Stelle. |
