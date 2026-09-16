# Arbeitsstand und Stufenplan

## Fertig (Stufe 0)

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
| **Stufe 1:** Strom für die Tafeln | Stromspeicher als `Capabilities.EnergyStorage.BLOCK`, Energiebalken, abschaltbar |
| **Stufe 2:** Karten anderer Mods | Inventar auch über `Capabilities.ItemHandler.BLOCK`, Durchsatz auf der Stromkarte |
| **Stufe 3:** Große Schirme | Tafelerweiterung, Flächenerkennung ohne Verwaltung, Schrift über das ganze Rechteck |
| **Stufe 4:** Bedienung | Steuerpaket, Texteingabe, Farbtafel, Berührungsbetrieb mit Umschaltkarte, freie Zahleneingabe |
| **Stufe 5:** Mekanism | Chemikalien, Joule, Wärme, alle sieben Mehrblockbauten, digitaler Bergmann — siehe [`MEKANISM-KOMPATIBILITAET.md`](MEKANISM-KOMPATIBILITAET.md) |

---

# Die Stufen

Jede Stufe ist für sich lauffähig, wird für sich gebaut, geprüft und übergeben. Die
Reihenfolge ist keine Willkür: Stufe 1 und 2 sind ausdrücklich gewünscht, Stufe 3 ist das
Kennzeichen des Mods, und Stufe 4 bündelt drei Dinge, die alle dasselbe fehlende Stück
brauchen.

## Stufe 1 — Die Tafeln brauchen Strom &nbsp;&nbsp;**[fertig]**

**Ziel:** Eine Informationstafel ohne Strom bleibt dunkel, wie im Original. Der Strom kommt
über die Energie-Schnittstelle von NeoForge, also aus jeder Mod, die Forge Energy abgibt.

**Warum so:** Das Original zog EU aus einem IC2-Netz. IC2 gibt es auf 1.21.1 nicht. Forge
Energy ist auf dieser Fassung der gemeinsame Nenner — sie steckt in NeoForge selbst, nicht in
einer fremden Mod. Der Mod bekommt damit **keine neue Abhängigkeit**: eine Tafel ohne
angeschlossenes Kabel verhält sich einfach wie eine Tafel ohne Strom.

**Inhalt:**

- Ein Stromspeicher in der Block-Entität der Tafel, angemeldet als
  `Capabilities.EnergyStorage.BLOCK`. Damit schieben Mekanisms Universalkabel, Thermals
  Leitungen, Immersive Engineerings Seile und alles andere mit Forge Energy hinein, ohne dass
  hier eine Zeile je Mod steht.
- `powered` wieder wie im Original: reicht der Puffer für den Verbrauch dieser Runde, misst die
  Tafel und zeigt an; sonst bleibt der Schirm leer. Der Renderer fragt das ab.
- Ein Energiebalken in der Oberfläche der Tafel.
- Einstellungen: Verbrauch je Tick, Puffergröße, und ein Schalter, der den Strombedarf ganz
  abschaltet (für Spielstände ohne Energie-Mod).
- Der Bereichsmelder liest ebenfalls Karten aus und bekommt denselben Bedarf; Wärmemelder,
  Heuler und Warnleuchte bleiben stromlos — so hält es auch das Original.

**Für HBM-Spieler:** HBMs Stromnetz (HE) ist bewusst von Forge Energy getrennt; im ganzen Port
gibt es genau zwei Übergänge, `machine_converter_he_rf` und `machine_converter_rf_he`. Eine
Tafel an einem HBM-Netz hängt also hinter dem HE→RF-Wandler. Das ist kein Umweg dieses Ports,
sondern die Bauweise des Originals, und es wird in `HBM-KOMPATIBILITAET.md` dokumentiert.

*Später denkbar:* ein eigener HE-Anschlussblock, der die Tafel ohne Wandler direkt ans
HBM-Netz hängt. Der müsste `IEnergyReceiverMK2` umsetzen und damit im Paket `crossmod/hbm`
liegen, das ohne HBM gar nicht übersetzt wird — die Anmeldung des Blocks müsste ihn deshalb
über seinen Namen laden und ohne HBM auf eine leere Block-Entität zurückfallen. Machbar, aber
umständlich; der Wandler tut es zunächst auch.

**Aufwand:** klein. **Risiko:** gering — eine Capability, ein Feld, ein Balken.

## Stufe 2 — Was die Karten von anderen Mods sehen &nbsp;&nbsp;**[fertig]**

**Ziel:** Die vorhandenen Karten sollen bei jeder gängigen Mod etwas anzeigen, ohne dass es je
Mod eine eigene Anbindung braucht.

**Inhalt:**

- **Lücke schließen:** Die Inventarkarte und ihr Bausatz prüfen heute nur auf `Container` —
  also auf das alte Inventar am Block. Viele neuere Mods bieten ausschließlich
  `Capabilities.ItemHandler.BLOCK` an und werden deshalb gar nicht erkannt. Beide bekommen den
  Weg über die Capability als Rückfall. (Bei HBM fällt das nicht auf: dessen Maschinen führen
  beides.)
- **Durchsatz statt nur Füllstand:** Die Stromkarte zeigt heute Stand und Fassung. Dazu kommt
  die Änderung je Tick, aus einem kleinen Ringpuffer über die letzten zwanzig Messungen — also
  das, was die HBM-Karte aus `delta` schon zeigt, nur mod-unabhängig. Das ist die Zahl, wegen
  der man eine Tafel an einen Akku hängt.
- **Bausätze großzügiger:** `kit_energy` greift schon über die Capability, `kit_liquid` auch.
  Nach der Änderung oben gilt das für `kit_inventory` ebenso.
- Eine Prüfung im Torwächter-Satz, die eine Karte ohne Rückfall auf die Capability meldet.

**Was das für Mekanism heißt:** Stromstand, Durchsatz, Flüssigkeitstanks und Inventar einer
Mekanism-Maschine sind damit lesbar, ohne eine Zeile Mekanism-Code — die Mod bietet all das
über die Standard-Schnittstellen an. **Nicht** erfasst sind Mekanisms eigene Chemikalien (Gase,
Schlämme, Pigmente) und die Kennzahlen von Reaktor, Fusionsanlage und Digital Miner; die
hängen an Mekanisms eigener API und stehen in Stufe 5.

**Aufwand:** klein. **Risiko:** gering.

## Stufe 3 — Große Schirme (Tafelerweiterungen) &nbsp;&nbsp;**[fertig]**

**Ziel:** Mehrere Tafeln zu einer Fläche zusammenschalten, wie im Original.

**Inhalt:** Erweiterungsblock, Flächenerkennung (welche Blöcke bilden ein sauberes Rechteck mit
gleicher Blickrichtung?), Weiterleitung von Klick und Oberfläche an die Haupttafel, und ein
Renderer, der die Schrift über das ganze Rechteck streckt statt über einen Block. Die Texturen stammen aus dem
Original (`extender_all`, `extender_face`).

Im Original steckt das in `ScreenManager` (rund 430 Zeilen), der die Schirme je Welt in einer
Liste führte und beim Setzen, Abbauen und Laden von Hand nachziehen musste — der häufigste
Fehlerort des ganzen Mods. Der Port verwaltet nichts: die Tafel rechnet ihre Fläche in ihrem
eigenen Takt neu aus. Eine Suche über höchstens vierundsechzig Blöcke einmal je Sekunde kostet
nichts, und dafür gibt es keinen Zustand, der veralten kann.

Drei Regeln halten die Fläche sauber: gleiche Ebene und gleiche Blickrichtung, volles Rechteck
(sonst bleibt es beim Einzelblock), und **genau eine** Tafel je Schirm — reichen zwei Tafeln
über dieselben Erweiterungen, gibt jede ihren Anspruch auf, statt übereinanderzuschreiben.

## Stufe 4 — Bedienung: Textfeld, Farbwahl, Berührung &nbsp;&nbsp;**[fertig]**

**Ziel:** Die drei Stellen, an denen der Port heute mit Knöpfen behilft, bekommen ihre richtige
Bedienung.

**Warum zusammen:** Alle drei brauchen dasselbe fehlende Stück — ein eigenes Netzwerkpaket vom
Client zum Server. Der Port kommt bisher mit `clickMenuButton` aus (siehe
[`ENTSCHEIDUNGEN.md`](ENTSCHEIDUNGEN.md)); sobald es das Paket gibt, sind alle drei billig.

**Inhalt:**

- Ein Steuerpaket (`CustomPacketPayload` mit `CompoundTag` und Blockposition), nach dem Muster,
  das der HBM-Port fährt.
- **Textkarte** mit echtem Textfeld statt Amboss-Name; mehrere Zeilen mit eigener Farbe.
- **Farbwahl** über die Farbtafel des Originals (`gui_colors`, `gui_color_picker`) statt
  sechzehn fester Farben.
- **Berührungsbetrieb**: Rechtsklick auf den Schirm wirkt aufs Ziel. Dazu die Umschaltkarte
  (`card_toggle`) und die Vanilla-Umschaltung. Die Berührungsaufwertung gibt es schon, sie tut
  bis dahin nichts.
- Freie Zahleneingabe im Bereichs- und Wärmemelder statt der Schrittknöpfe.

Umgesetzt mit **einem** Paket (`PanelControl`) statt der drei des Originals: was gemeint ist,
steht im NBT-Beutel unter `action`. Die Rechteprüfung liegt beim Empfänger
(`IControlReceiver.hasPermission`, in der Regel `stillValid` — acht Blöcke), und ein Paket auf
einen nicht geladenen Chunk wird verworfen, statt ihn auf Zuruf vom Client zu laden.

Die Farbwahl ist gegenüber dem Original **freier**: die sechzehn Farben stehen als Felder zur
Wahl, und wer eine andere will, tippt sie als Hexwert ein. Die Berührung schaltet über
`useWithoutItem` des Zielblocks — also genau so, als hätte der Spieler danebengestanden und
geklickt; erlaubt sind Hebel, Knöpfe, Türen, Falltüren und Zauntore.

## Stufe 5 — Benannte Anbindungen (Mekanism und andere) &nbsp;&nbsp;**[fertig für Mekanism]**

**Ziel:** Für eine Mod, die mehr hergibt als die Standard-Schnittstellen, eine eigene Karte —
so, wie es `crossmod/hbm` für HBM vormacht.

**Inhalt je Mod:** eine Klasse unter `crossmod/<mod>`, gegen deren API mit `compileOnly`
übersetzt, über `CrossModLoader` per Namen geladen, und aus dem Quelltextsatz genommen, wenn
die API beim Bauen fehlt — dasselbe Muster, dieselbe Torwächter-Prüfung (`hbm-api-check.sh`
lässt sich dafür verallgemeinern).

**Mekanism ist umgesetzt** und in [`MEKANISM-KOMPATIBILITAET.md`](MEKANISM-KOMPATIBILITAET.md)
beschrieben: Chemikalientanks, Strom in Joule, Wärmespeicher, digitaler Bergmann sowie alle
sieben Mehrblockbauten — Kessel, Induktionsmatrix, Verdunstungsanlage, SPS, Spaltreaktor,
Fusionsanlage, Turbine. Die vier Chemikalienarten des Originals (Gas, Schlamm, Pigment,
Infusion) sind seit Mekanism 10.7 ein einziger Typ und damit eine Tankart statt vier.

Dabei ist aus dem Muster ein Werkzeug geworden: `tools/api_check.py` bekommt Verzeichnis,
Quellen und Paketvorsätze von außen und prüft jede Anbindung; `build.gradle` sucht die JARs
über eine gemeinsame Funktion; CI baut einmal ohne und einmal mit allen fremden JARs.

**Offen bleiben die weiteren Kandidaten** — Thermal Expansion, Immersive Engineering,
Applied Energistics, Ad Astra. Der Grund ist nicht der Aufwand, sondern der Nutzen:

- Was diese Mods anbieten, ist überwiegend Forge Energy, `IFluidHandler` und
  `IItemHandler` — das liest der Mod seit Stufe 2 **ohne** eigene Anbindung. Eine eigene
  Karte lohnt erst, wo eine Mod etwas Eigenes führt, wie Mekanisms Chemikalien oder HBMs
  Reaktoren.
- Jede weitere Anbindung braucht ihre JAR im Bau und ihre Quellen in der Prüfung. Das ist
  Ballast, solange niemand die Karte vermisst.

Kommt eine dieser Mods als Wunsch, ist Mekanism die Vorlage: eine Klasse unter
`crossmod/<mod>`, ein Eintrag in `CrossModLoader`, ein Aufruf von `tools/api_check.py`, ein
Block in `build.gradle`.

## Stufe 6 — Der Rest aus dem Original

Kleinteile, die keinen eigenen Unterbau brauchen:

| Teil | Was es ist |
| --- | --- |
| Fernwärmeanzeige | Wärmemelder, der seinen Reaktor über eine Karte findet statt über Nachbarschaft |
| Zählerkarte und Energiezähler | Durchsatzmessung mit einem Block in der Leitung |
| Bausatzmontage (`kit_assembler`) | Maschine, in der Bausätze entstehen, statt an der Werkbank |
| Kartenhalter, tragbare Tafel | Gegenstände, die Karten führen bzw. eine Tafel in der Hand sind |
| Fortgeschrittene Tafel | Größere Bauform des Originals mit Neigung und eigener Oberfläche |

**Aufwand:** je Teil klein bis mittel.

---

## Nicht vorgesehen

| Teil | Warum |
| --- | --- |
| Anbindungen an IC2, Draconic Evolution, GregTech, EnderIO, PneumaticCraft, NuclearCraft, Galacticraft, Railcraft, Extreme Reactors | Diese Mods gibt es auf 1.21.1 nicht. Kommt eine davon, ist Stufe 5 das Muster. |
| ComputerCraft- und OpenComputers-Anbindung | Brauchen die jeweilige Mod; nachrüstbar, sobald sie da sind. |
| WebSocket-Server | Das Original schickt Tafelinhalte an eine Webseite. Steht keinem Spielinhalt im Weg und ist den Aufwand vorerst nicht wert. |
| `com.zuxelus.hooklib` | Ein eigener Bytecode-Weber aus 1.12.2-Zeiten. Auf 1.21.1 gibt es dafür Mixins, und der Port braucht ihn an keiner Stelle. |
| Saatgutanalyse und -bibliothek | Hängt im Original an IC2-Saatgut und hat auf 1.21.1 keine Entsprechung. |
