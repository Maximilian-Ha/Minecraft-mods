# Gap-Analyse: was ist portiert, was fehlt

Stand: Vergleich des 1.7.10-Originals (Commit `7c60926`) mit dem Port nach Runde 43.

## Gesamtumfang

| | Original 1.7.10 | Port, Erstanalyse | Port nach Runde 43 |
| --- | ---: | ---: | ---: |
| Java-Dateien | 3.477 | 991 | **1.326** |
| Zeilen Java | ca. 464.000 | ca. 110.000 | **ca. 155.500** |
| Texturen | 5.978 | 2.303 | **2.696** |
| OBJ-Modelle | 514 | 112 | **160** |
| TileEntities / BlockEntities | 370 | 59 | **145** |

Seit der Erstanalyse sind 335 Java-Dateien und rund 45.500 Zeilen dazugekommen; die Zahl der
BlockEntities hat sich mehr als verdoppelt.

## Pro Subsystem

Ermittelt von je einem Analyse-Agenten, der beide Quellbäume gelesen und die Mechaniken
verglichen hat — nicht durch bloßes Zählen von Dateinamen.

| Subsystem | Dateien Original | Dateien Port | Funktionale Abdeckung | Aufwand (Personentage) | Schwierigkeit |
| --- | ---: | ---: | ---: | ---: | --- |
| Maschinen (Blöcke + BlockEntities) | 536 | 81 | 16 % | 322 | extrem |
| Reaktoren (RBMK, PWR, Fusion, ICF, Pile, Watz) | 269 | 13 | 5 % | 160 | extrem |
| Waffen (Sedna-Waffen, Munition, Geschütze) | 242 | 31 | 15 % | 155 | extrem |
| Energiesystem (HE, UNINOS, Kabel, Batterien) | 183 | 46 | 22 % | 85 | hoch |
| Fluidsystem (Typen, Tanks, Rohre, Druck) | 115 | 61 | 45 % | 48 | hoch |
| **Summe dieser fünf** | **1.345** | **232** | | **770** | |

> **Zu diesen fünf Zeilen:** die Zahlen sind Einzeleinschätzungen der Analyse-Agenten und
> nicht unabhängig gegengeprüft. Die Spalte „Dateien Port" ist zudem der Stand der
> Erstanalyse und inzwischen überholt.

## Die übrigen zehn Subsysteme

Die Erstanalyse ließ zehn von fünfzehn Subsystemen offen. Sie sind jetzt nachgezogen — mit
**Zählungen statt Einschätzungen**, was die Zahlen reproduzierbar, aber auch gröber macht. Der
ausführliche Bericht samt Methodenkritik steht in
[`gap-analysis/restliche-subsysteme.md`](gap-analysis/restliche-subsysteme.md).

| Subsystem | gezählte Einheit | Original | Port | Abdeckung |
| --- | --- | ---: | ---: | ---: |
| Strahlung/Gefahren | Gefahrentypen + Modifikatoren | 14 | 14 | **100 %** |
| Speicherdaten | Dateien | 18 | 13 | 72 % |
| Netzwerkpakete | Pakete | 29 | 16 | 55 % |
| Items | registrierte Items | 1.690 | 936 | 55 % |
| Entities | registrierte Typen | 122 | 57 | 47 % |
| Blöcke (generisch) | registrierte Blöcke | 918 | 388 | 42 % |
| Rezepte | Rezepthandler | 48 | 20 | 42 % |
| GUIs | Container + Oberflächen | 396 | 149 | 38 % |
| Rendering | Block-Renderer | 249 | 89 | 36 % |
| Partikel | Partikelklassen | 38 | 13 | 34 % |
| Infrastruktur | Handler | 137 | 46 | 34 % |
| **Weltgenerierung** | **Dateien** | **86** | **13** | **15 %** |

**Der Befund in einem Satz:** diese zehn liegen im Mittel bei rund 45 % und damit deutlich
besser als die fünf zuerst analysierten (5–45 %) — mit einer Ausnahme, die aus der Reihe
fällt.

### Die Weltgenerierung ist das eigentliche Loch

15 % ist der schlechteste Wert im ganzen Projekt, und die Zahl untertreibt sogar noch. Im Port
gibt es **kein einziges Gebäude**: keinen Bunker, kein Silo, kein Labor, nichts von den 28
Strukturen und 13 Verliesen des Originals. Wer den Mod heute spielt, findet Öl und Erze — und
sonst nichts zu erkunden.

Dazu kommt, dass 1.21 die Weltgenerierung auf datengetriebene JSON-Strukturen und Jigsaw-Pools
umgestellt hat. Die Strukturklassen des Originals setzen Block für Block in handgeschriebenem
Java. Das ist **keine Portierung, sondern eine Neuentwicklung in einem anderen Paradigma** —
die Zeilenzahl des Originals ist hier ein besonders schlechter Schätzer für den Aufwand.

### Was das für den Gesamtaufwand heißt

Für die zehn nachgezogenen Subsysteme lässt sich **kein Personentag-Wert angeben**. Dafür
müsste man Mechanik für Mechanik vergleichen, wie es die ersten fünf Berichte getan haben, und
nicht zählen. Wer eine Gesamtzahl braucht, muss diese zehn erst auf demselben Weg untersuchen
wie die fünf.

Was sich sagen lässt: die ursprüngliche Schätzung von „deutlich über 1.000 Personentagen" für
den vollständigen Port steht weiterhin, und die Weltgenerierung ist der Posten, den sie am
wahrscheinlichsten unterschätzt.

Die ausführlichen Berichte je Subsystem — mit Dateilisten, Mechanik-für-Mechanik-Vergleich und
Begründungen — liegen unter [`gap-analysis/`](gap-analysis/).

## Die wiederkehrenden Blocker

Dieselben Ursachen tauchen in jedem Subsystem auf. Sie einmal zentral zu lösen, entsperrt
jeweils Dutzende Maschinen:

1. **Metadaten → Blockstates.** 1.7.10 packte Ausrichtung, Variante und Multiblock-Versatz in
   4 Bit Metadaten. 124 der 278 Maschinenblöcke sind Multiblöcke auf `BlockDummyable`.
2. **`IIcon` → Texturatlas und JSON-Modelle.** 68 Maschinenblöcke nutzen `registerBlockIcons` /
   `getIcon(side, meta)`. Von 514 OBJ-Modellen sind 112 übernommen.
3. **TESR → `BlockEntityRenderer`.** 249 Renderer im Original, 47 im Port. Vier RBMK-Renderer
   sind `ISimpleBlockRenderingHandler` — dafür gibt es auf 1.21 überhaupt kein Gegenstück.
4. **Eigene Pakete → `CustomPacketPayload` + `StreamCodec`.** Betrifft jeden GUI-Knopf und jede
   Partikelaussendung.
5. **Schadenswert als Untertyp → Data Components.** RBMK-Brennstäbe, Batterien, Munition,
   Blaupausen, Fluidbehälter.
6. **Rezepte als Code → datengetrieben.** 49 der 67 Rezeptsysteme fehlen; jedes braucht
   zusätzlich eine JEI-Kategorie (13 von ~57 NEI-Handlern portiert).
7. **Keine Capability-Registrierung.** Der Port behält die `instanceof`-Erkennung aus 1.7.10 bei,
   registriert aber keine `Capabilities.ItemHandler.BLOCK` / `FluidHandler.BLOCK`. Es gibt auch
   keine RF/FE-Brücke — andere Mods können nicht andocken.
8. **Ersatzlos entfallene Fremd-APIs.** OpenComputers (57 Maschinen mit Callbacks), Energy
   Control, Microblocks. Entweder streichen (sichtbarer Verlust) oder neu entwerfen.
9. **Fehlende Infrastruktur im Port.** `IConfigurableMachine` (JSON-Konfiguration, 28 Maschinen)
   und `FluidTrait.onRelease` (die Kopplung von Fluiden an Verschmutzung, Verstrahlung und Gifte
   — `FT_VentRadiation` ist deshalb toter Code).

## Besonders harte Einzelfälle

- **Neutronenfluss-Framework** (`handler/neutron/*`): hält statische, pro Welt gehaltene
  `HashMap<BlockPos, NeutronNode>`, die aus `updateEntity`/`invalidate`/`onChunkUnload` mutiert
  werden. Muss auf `ServerLevel` mit Attachment oder `SavedData` neu gebaut werden. Harte
  Voraussetzung für RBMK **und** den Graphitreaktor.
- **`RBMKDials`**: 24 Stellgrößen als frei angelegte 1.7.10-Gamerules. 1.21 kennt nur statisch
  registrierte Gamerules und nur `boolean`/`int` — 18 der 24 sind aber `double`. Alles muss in
  Konfiguration oder `SavedData` umziehen, und `getGameRule()` wird aus fast jeder
  RBMK-Tick-Methode aufgerufen.
- **Containerlose GUIs**: 11 RBMK-Oberflächen (Konsole, Terminal, Graph, Anzeige, Tastenfeld …)
  öffneten im Original einen rohen `GuiScreen` ohne Container. Auf 1.21 ist das nicht zulässig;
  jede braucht ein eigenes Payload plus clientseitigen Öffnen-Handler.

## Bewertung

Ein vollständiger Port ist ein Mehr-Personenjahr-Projekt. Sinnvoll ist, ihn als solchen zu führen:
zuerst die neun Blocker oben zentral lösen, dann die Maschinen subsystemweise nachziehen. Die
Reihenfolge steht in [`ROADMAP.md`](ROADMAP.md), das erprobte Vorgehen je Maschine in
[`PORTING-GUIDE.md`](PORTING-GUIDE.md).
