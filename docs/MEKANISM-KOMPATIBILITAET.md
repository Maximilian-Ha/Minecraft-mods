# Die Anbindung an Mekanism

Mekanism ist die zweite Mod mit einer eigenen Anbindung. Sie folgt demselben Muster wie
die [HBM-Anbindung](HBM-KOMPATIBILITAET.md) — der Kern kennt keine Mekanism-Klasse,
`CrossModLoader` lädt die Brücke über ihren Namen, und ohne die JARs fällt das Paket
`crossmod/mekanism` beim Bauen aus dem Quelltextsatz.

```java
Class.forName("com.zuxelus.energycontrol.crossmod.mekanism.CrossMekanism")
```

## Warum überhaupt eine eigene Anbindung?

Strom, Flüssigkeiten und Inventare einer Mekanism-Maschine liest der Mod schon **ohne**
jede Zeile Mekanism-Code: Mekanism bietet Forge Energy, `IFluidHandler` und `IItemHandler`
an, und darüber gehen seit Stufe 2 die Strom-, Flüssigkeits- und Inventarkarte.

Drei Dinge gibt es dort aber nicht:

1. **Chemikalien.** Gas, Schlamm, Pigment und Infusion sind bei Mekanism kein Fluid,
   sondern ein eigenes Register. Seit 10.7 sind die vier zu einem Typ `Chemical`
   verschmolzen — die vier getrennten Tankarten des Originals von 1.12.2 gibt es nicht mehr.
2. **Große Energiemengen.** Forge Energy zählt in `int`; `getEnergyStored()` kann nicht mehr
   als etwa 2,1 Milliarden melden und klemmt darüber ab. Eine Induktionsmatrix speichert
   leicht ein Vielfaches davon, und ihre Anzeige stünde dann fest am Anschlag. Mekanisms
   eigene Schnittstelle `IStrictEnergyHandler` rechnet in `long`, deshalb liest die Brücke
   dort — und rechnet die Joule anschließend selbst in FE um (siehe unten).
3. **Die Mehrblockbauten.** Spaltreaktor, Fusionsanlage, Turbine, Kessel, Induktionsmatrix,
   Verdunstungsanlage und SPS führen ihre Zahlen im Kern des Baus, nicht am angeklickten
   Block.

## Woher die Werte kommen

Die drei Schnittstellen holt die Brücke über ihre **Namen** aus dem Capability-Register von
NeoForge, nicht über Mekanisms interne Klasse `Capabilities`:

```java
BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"),
                            IChemicalHandler.class)
```

Das ist dieselbe Kennung, die Mekanism selbst anlegt; wer zuerst kommt, legt sie an, der
andere bekommt sie zurück. Damit hängt die Brücke an drei API-Schnittstellen statt an einer
internen Klasse.

| Was | Woher |
| --- | --- |
| Strom aller Maschinen, in Joule | `IStrictEnergyHandler` (`mekanism:strict_energy_handler`) |
| Umrechnungsfaktor Joule → FE | `IEnergyConversionHelper.INSTANCE.feConversion()` |
| Chemikalientanks aller Maschinen | `IChemicalHandler` (`mekanism:chemical_handler`) |
| Flüssigkeitstanks | `IFluidHandler` von NeoForge |
| Wärmespeicher | `IHeatHandler` (`mekanism:heat_handler`) |
| Fortschritt einer Maschine | `TileEntityProgressMachine` |
| Ein/Aus | `TileEntityMekanism.getActive()` |
| Kessel | `BoilerMultiblockData`: Temperatur, Verdampfung, Überhitzer, vier Tanks |
| Induktionsmatrix | `MatrixMultiblockData`: Stand, Fassung, Zu- und Abfluss, Zellen, Übertrager |
| Verdunstungsanlage | `EvaporationMultiblockData`: Temperatur, Ausbeute, zwei Tanks |
| SPS | `SPSMultiblockData`: Fortschritt, Durchsatz, aufgenommene Energie |
| Digitaler Bergmann | `TileEntityDigitalMiner`: Restmenge, Zustand, Radius, Schichten |
| Spaltreaktor | `FissionReactorMultiblockData`: Temperatur, Abbrandrate, Schaden, Verdampfung, Brennelemente |
| Fusionsanlage | `FusionReactorMultiblockData`: Plasma- und Gehäusetemperatur, Einspeisung, Ruheleistung |
| Turbine | `TurbineMultiblockData`: Leistung, Durchfluss, Schaufeln, Spulen, Auslässe, Kondensatoren |

Der **Wärmemelder** findet zusätzlich einen Spaltreaktor in der Nachbarschaft und meldet
dessen Hüllentemperatur — dieselbe Suche wie bei den HBM-Reaktoren (sechs angrenzende
Blöcke, dann ein Kasten von 7 × 3 × 7).

## Die Tafel zeigt FE, nicht Joule

Mekanism zählt intern in Joule. Auf der Tafel steht diese Zahl aber neben denen anderer
Mods, und die rechnen in FE — die Kabel, die Speicher, die Maschinen. Eine Tafel, die „J"
schreibt, wo das Kabel daneben FE führt, nennt zwei verschiedene Dinge gleich.

`MekEnergy` rechnet deshalb jede Energiezahl der Brücke um, bevor sie in den Beutel der
Karte geht: Ladung, Kapazität, Differenz, Ein- und Ausgang der Matrix, Durchsatzgrenze,
aufgenommene Energie der SPS, passive Erzeugung der Fusionsanlage, Erzeugung der Turbine.
Die Zahlen sind Gleitkommazahlen, denn bei 2,5 J je FE geht jede zweite nicht glatt auf.
Was keine Energie ist — Dampf, Brennrate, Temperatur —, bleibt unverändert.

Der Faktor steht **nicht** im Quelltext. Er ist bei Mekanism einstellbar
(`feConversionRate`, Vorgabe 2,5 J je FE), und `IEnergyConversionHelper` ist Mekanisms
öffentlicher Zugang zu genau diesem Wert — dieselbe Einstellung, mit der Mekanisms eigene
Kabel rechnen. Wer die Forge-Energie bei Mekanism ganz abschaltet (`blacklistForge`),
bekommt weiter Joule zu sehen; dann gibt es im Spiel keine FE, in die umzurechnen wäre.

Welche Einheit gilt, steht als `euType` im Beutel. `ItemCardMekanism` kennt Mekanism nicht
und darf sie nicht raten — sie kommt mit den Zahlen zusammen an.

## Zwei Mods, eine Brücke

Spaltreaktor, Fusionsanlage und Turbine gehören zu **Mekanism: Generators**, einer eigenen
Mod. Zum **Übersetzen** braucht die Brücke deshalb beide JARs; ist eine davon nicht da,
fällt das ganze Paket weg.

Im **Spiel** genügt Mekanism selbst. Alles, was die Generatoren betrifft, steht in
`MekGenerators`, und `CrossMekanism` spricht diese Klasse nur an, wenn
`ModList.get().isLoaded("mekanismgenerators")` gilt. Bleibt der Aufruf aus, lädt die JVM
die Klasse nie und löst ihre Verweise nie auf — der übliche Weg für eine weiche
Abhängigkeit.

## Wie das geprüft wird

Ein Bau gegen Mekanism ist in dieser Arbeitsumgebung nicht möglich (kein Netz zu den
Maven-Servern), CI übernimmt das. Dort läuft:

- `tools/mekanism-api-check.sh` gegen die **Quellen** von Mekanism (Zweig `1.21.x`):
  jeder Import und jeder Feld- und Methodenzugriff der Brücke muss dort sein Ziel finden.
  Dasselbe Skript prüft auch die HBM-Brücke — `tools/api_check.py` bekommt Verzeichnis,
  Quellen und Paketvorsätze von außen.
- Ein Bau **ohne** die JARs: beweist, dass der Kern Mekanism nicht braucht.
- Ein Bau **mit** den JARs (von Modrinth geholt, nur zum Übersetzen) plus die Prüfung, dass
  `CrossMekanism.class` und `MekGenerators.class` in der fertigen JAR liegen.
- Ein Serverstart mit den Brücken in der JAR, aber **ohne** Mekanism und HBM — genau der
  Fall, in dem `CrossModLoader` sie nicht laden darf.

## Was die Karte zeigt

Die **Mekanism-Sensorkarte** (`card_mekanism`, Bausatz `kit_mekanism`) legt ihre Zeilen in
acht Gruppen ab, je eine Ankreuzfläche in der Tafel: Energie, Tanks, Wärme, Reaktor,
Turbine, Induktionsmatrix, Fortschritt, Bergmann. Eine Gruppe zeigt nur, was der gemessene
Block wirklich hergibt — steht die Karte auf einem Kessel, bleiben die Turbinenzeilen leer.

Die Beschriftungen sind eigene Schlüssel (`msg.ec.*`) in Englisch und Deutsch; der Mod
greift nie in die Sprachdateien von Mekanism.
