# Portier-Entscheidungen

Festlegungen, die **einmal** getroffen und danach überall gleich angewendet werden. Jede hat
einen Präzedenzfall im Port, auf den man sich berufen kann, statt sie je Teilsystem neu zu
verhandeln.

Wer eine dieser Entscheidungen umstößt, ändert sie hier — und trägt die Folgen für alle bereits
portierten Stellen.

---

## 1. OpenComputers wird ersatzlos gestrichen

**Entscheidung:** Alle `@Optional.Interface(SimpleComponent)`, `CompatHandler.OCComponent`,
`@Callback`-Methoden und `getComponentName()` entfallen beim Portieren ohne Ersatz.

**Begründung:** Es gibt kein OpenComputers für 1.21.1. Der Code ließe sich nicht übersetzen und
hätte keinen Abnehmer.

**Präzedenz:** Runde 33, Kranpult (`CraneConsoleBlockEntity`) — dort sind sechs `@Callback`-Methoden
ersatzlos entfallen, im Klassenkommentar vermerkt.

**Umfang:** rund 640 Zeilen allein in den RBMK-Anzeigetafeln, insgesamt weit über 1.000.

---

## 2. Connected Textures werden ersatzlos gestrichen

**Entscheidung:** `com.hbm.render.block.ct` (`CT`, `IBlockCT`, `CTStitchReceiver`) wird nicht
portiert. Blöcke, die im Original nahtlos zusammenwachsen, bekommen ihre gewöhnliche Textur.

**Begründung:** Das Original stitcht die Übergangstexturen zur Ladezeit selbst in den Atlas.
In 1.21 müsste man dafür ein eigenes Modellsystem bauen; der Aufwand steht in keinem Verhältnis
zum rein optischen Gewinn.

**Präzedenz:** `WireCoatedBlock.java:19` — dort ist es schon so gehandhabt.

**Betrifft:** PWR, ICF, Chicago Pile.

---

## 3. `SatelliteRayScan` wird ersatzlos gestrichen

**Entscheidung:** `SatelliteRayScan` und die `RayEvent`-Typen (`INFO_NUCLEAR`, `INFO_PARTICLE`)
werden nicht portiert. Maschinen, die im Original ihren Zustand an einen Satelliten melden,
melden ihn nicht.

**Begründung:** Der Satellitenscanner hängt am gesamten Satellitensystem des Originals. Das ist
ein eigener Zweig, kein Reaktorzubehör.

**Präzedenz:** `ReactorZirnoxBlockEntity.java:174` — dort ist der Aufruf schon weggelassen.

**Betrifft:** PWR, ICF, Fusion, ZIRNOX-Zerlegung.

---

## 4. `SmallBlockPronter`-Vorschauen entfallen

**Entscheidung:** Die durchscheinenden „Geistervorschauen", die im Original zeigen, wie ein
Mehrblockbau aufgestellt gehört, werden nicht portiert.

**Begründung:** `SmallBlockPronter` rendert Blöcke außerhalb der Welt über einen eigenen
Fake-Renderpfad. In 1.21 bräuchte das einen eigenen Renderdurchgang. Der Fehlermarker (siehe
unten) erfüllt denselben Zweck — er zeigt, *wo* es klemmt — mit einem Bruchteil des Aufwands.

**Ersatz:** `MarkerCreator.sendError` (Runde 37) rahmt den störenden Block rot ein und schreibt
den Grund daneben.

**Betrifft:** Watz, Fusion, ICF.

---

## 5. Der PWR-Drucker entfällt

**Entscheidung:** `ItemPWRPrinter` und `GUIScreenSlicePrinter` werden nicht portiert.

**Begründung:** Der Drucker zeichnet einen fertig gebauten Druckwasserreaktor Schicht für Schicht
in eine Reihe von PNG-Dateien — als Bauanleitung zum Nachbauen. Er tut das über `RenderBlocks`,
den Blockrenderer von 1.7.10, den es in 1.21 nicht mehr gibt; der Ersatz wäre ein eigener
Offscreen-Renderdurchgang, der gebackene Modelle in einen Framebuffer zeichnet und ihn
abfotografiert. Das ist dieselbe Baustelle wie bei `SmallBlockPronter` (Punkt 4) und aus demselben
Grund gestrichen: viel neuer Rendercode für eine Funktion, die am Spiel nichts ändert.

**Kein Ersatz.** Der Drucker ist ein reines Dokumentationswerkzeug — er baut nichts, er prüft
nichts, er verändert die Welt nicht. Wer seinen Reaktor festhalten will, macht einen Screenshot.

**Betrifft:** nur den PWR.

---

## Was NICHT gestrichen wird

- **Der Fehlermarker selbst** ist portiert (Runde 37) und ersetzt sowohl die Geistervorschau als
  auch die Chatmeldungen des Originals.
- **`IBlockMulti`** wird nicht portiert, aber auch nicht ersetzt: es ist eine reine
  Metadaten-Krücke. Blöcke mit Varianten benutzen im Port Blockzustands-Eigenschaften, Items
  benutzen `IMetaItem`/`EnumMultiItem`. Beides ist bereits durchgängig etabliert.
