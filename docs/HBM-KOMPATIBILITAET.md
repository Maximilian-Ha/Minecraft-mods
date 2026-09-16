# Die Anbindung an HBM's Nuclear Tech Mod

Das ist der Schwerpunkt dieses Ports: Energy Control soll die Maschinen und Reaktoren des
HBM-Ports auslesen können, der im Zweig `claude/intelligent-meitner-ashog3` desselben
Repositories liegt.

## Wie die Anbindung eingehängt ist

Der Kern von Energy Control kennt **keine einzige HBM-Klasse**. Er kennt nur
`CrossModBase` — eine Klasse, deren Methoden alle mit "kenne ich nicht" antworten — und
`CrossModLoader`, der die wirkliche Anbindung beim Hochfahren über ihren Namen lädt:

```java
Class.forName("com.zuxelus.energycontrol.crossmod.hbm.CrossHbm")
```

Daraus folgt dreierlei:

1. **Ohne HBM lädt der Mod unverändert.** `ModList.get().isLoaded("hbmsntm")` entscheidet,
   ob überhaupt geladen wird.
2. **Ohne HBM-JAR übersetzt der Mod unverändert.** `build.gradle` nimmt das Paket
   `crossmod/hbm` aus dem Quelltextsatz, wenn keine JAR da ist. Da niemand die Klasse direkt
   nennt, fehlt danach nichts.
3. **Die Anbindung selbst ist typsicher.** Sie greift nicht über Reflexion auf HBM zu,
   sondern über dessen Klassen — mit einer Ausnahme, siehe unten.

In `neoforge.mods.toml` steht `hbmsntm` als **optionale** Abhängigkeit mit
`ordering="AFTER"`: ist der Port da, wird er zuerst geladen.

## Strom für die Tafeln

HBMs Stromnetz (HE) ist bewusst von Forge Energy getrennt. `NtmCapabilities` meldet
`EnergyStorage.BLOCK` an genau zwei Blöcken an, mit dem Kommentar *"das Stromnetz des Mods
selbst bleibt bewusst getrennt -- so hält es auch das Original"*:

- `machine_converter_he_rf` — HE hinein, Forge Energy heraus
- `machine_converter_rf_he` — umgekehrt

Eine Informationstafel an einem HBM-Netz hängt deshalb **hinter dem HE→RF-Wandler**. Das ist
kein Umweg dieses Ports, sondern die Bauweise, die das Original schon hatte.

*Später denkbar:* ein eigener HE-Anschlussblock, der ohne Wandler auskommt. Der müsste
`IEnergyReceiverMK2` umsetzen und damit im Paket `crossmod/hbm` liegen, das ohne HBM gar nicht
übersetzt wird -- die Anmeldung des Blocks müsste ihn über seinen Namen laden und ohne HBM auf
eine leere Block-Entität zurückfallen. Machbar, aber umständlich; der Wandler tut es auch.

## Woher die Werte kommen

### Strom

Alle stromführenden Maschinen des HBM-Ports bieten `api.hbm.energymk2.IEnergyHandlerMK2` an,
mit `getPower()` und `getMaxPower()`. Ein einziger `instanceof` genügt also.

Zum Vergleich: die 1.12.2-Fassung von `CrossHBMCE` hatte dafür rund **120 einzelne
`instanceof`-Zweige**, einen je Maschine — weil auf 1.7.10 jede Maschine ihr eigenes
`getPower()` ohne gemeinsame Oberklasse hatte. Die Liste war der Hauptgrund, warum die
Anbindung mit jeder HBM-Fassung neu nachgezogen werden musste. Im Port entfällt sie
vollständig.

### Flüssigkeiten

`api.hbm.fluidmk2.IFluidUserMK2.getAllTanks()` liefert alle Tanks einer Maschine als
`FluidTank[]`. Name, Füllstand und Fassung stehen am Tank selbst; HBM führt seine
Flüssigkeiten in einem eigenen Register (`FluidType`), nicht im Fluid-Register von Minecraft,
deshalb geht hier nichts über `FluidStack`.

Leere Tanks ohne zugewiesene Flüssigkeit lässt die Anbindung weg — eine Maschine hat oft mehr
Tanks, als gerade benutzt sind, und fünf Zeilen "Leer" helfen niemandem.

### Mehrblockmaschinen

Wer auf einen Platzhalterblock einer Mehrblockmaschine klickt, soll die Werte des Kerns sehen.
Den Weg dorthin bietet HBM selbst an:

```java
com.hbm.util.CompatExternal.getCoreFromPos(level, pos)
```

Die Klasse trägt im Original den Hinweis *"EXTERNAL COMPATIBILITY CLASS — DO NOT CHANGE METHOD
NAMES/PARAMS ONCE CREATED"*; sie ist genau für diesen Zweck da.

### Reaktoren

| Reaktor | Was die Karte zeigt |
| --- | --- |
| RBMK-Säule (alle Bauformen) | Säulentemperatur, Schmelzpunkt |
| RBMK-Brennkanal | dazu Fluss (schnell/langsam), Abbrand, Xenon, Hüllen- und Kerntemperatur, Brennstoffname, geladen ja/nein |
| ZIRNOX | Temperatur, Druck, Ein/Aus |
| Forschungsreaktor | Temperatur, Regelstellung, Gesamtfluss |
| Watz | Temperatur, Fluss, Ein/Aus |

Bei der RBMK-Säule geht die Anbindung über `getNBTForConsole()` — dieselbe Methode, mit der
die Säule die Reaktorkonsole beliefert. Das ist wichtig, weil es auf dem **Server** rechnet.
Die Felder `fuelYield`, `fuelXenon` und `fuelHeat` der Säule sehen verlockend aus, füllt aber
erst das Anzeigepaket; serverseitig sind sie leer, und die Karte läuft auf dem Server.

Ebenso der Fluss: `fluxQuantity` wird während einer Runde aufsummiert und ist beim Auslesen
meist noch null. Die Anbindung liest deshalb `lastFluxQuantity` und `lastFluxRatio` — den
Fluss der abgeschlossenen Runde, denselben Wert, den HBM auch speichert.

### Batterieblock

`MachineBatteryBlockEntity.delta` ist die Lade- beziehungsweise Entladeleistung über die
letzten zwanzig Ticks. Das Original rechnete diese Differenz mit einem eigenen Ringpuffer und
einem Bytecode-Weber nach; der Port nimmt einfach, was HBM schon führt.

### Strahlung

`ChunkRadiationManager.proxy.getRadiation(level, pos)` gibt die Strahlung am Kartenziel.

### Funkwerte

Maschinen, die `api.hbm.redstoneoverradio.IRORValueProvider` anbieten, geben über
`getFunctionInfo()` selbst bekannt, welche Werte sie herausrücken. Die Anbindung fragt sie
alle ab und legt sie unter `ror_<name>` ab. Das kostet nichts und wirkt für jede Maschine, die
der HBM-Port künftig dafür einrichtet — ohne dass hier eine Zeile dazukommt.

### Fortschritt, Verbrauch, Temperatur gewöhnlicher Maschinen

Dafür hat HBM **keine** gemeinsame Schnittstelle: die Felder `progress`, `maxProgress`,
`consumption`, `heat` und `isOn` heißen in jeder Maschine gleich, gehören aber zu keiner
gemeinsamen Oberklasse. Hier — und nur hier — liest die Anbindung über Reflexion
(`HbmFields`).

Das ist bewusst so: die Alternative wäre eine Liste aller Maschinenklassen, die bei jeder
neuen Maschine im HBM-Port nachgezogen werden müsste. Genau daran ist die 1.12.2-Fassung mit
ihren 120 Einzelfällen erstickt. Gefundene wie fehlende Felder werden je Klasse gemerkt, damit
die Suche nicht bei jedem Auslesen erneut durch die Klassenhierarchie läuft.

## Wie die Anbindung geprüft wird

Zwei Stufen:

1. **`tools/hbm-api-check.sh`** liest die Quellen des HBM-Ports und prüft, dass jeder Import
   und jedes angesprochene Feld und jede Methode dort wirklich existiert — auch in den
   Oberklassen. Das braucht keinen Bau und kein Netz, nur eine Auscheckung des HBM-Zweigs.
   Signaturen und Typen prüft es nicht.
2. **Die CI baut beides wirklich**: erst den HBM-Port, dann Energy Control gegen dessen JAR,
   und prüft anschließend, dass `crossmod/hbm/CrossHbm.class` in der fertigen JAR liegt.
   Davor läuft ein Bau **ohne** HBM — damit auffällt, wenn sich in den Kern versehentlich ein
   Verweis auf eine HBM-Klasse schleicht.

## Was noch aussteht

- Die Fernwärmeanzeige (`Remote Thermal Monitor`) des Originals, die einen Reaktor über eine
  Karte statt über Nachbarschaft findet.
- Eine Karte für HBMs Fernleitungsnetz (Umspannwerk, Kabelanzeige) mit Netzdurchsatz.
- Der Watz kennt keine feste Temperaturobergrenze; der Wärmemelder liefert für ihn deshalb nur
  den Istwert, keine Schwelle in Prozent.

Siehe [`ROADMAP.md`](ROADMAP.md).
