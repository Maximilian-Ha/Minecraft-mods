# Roadmap

Priorisierte Reihenfolge für die weitere Portierung. Grundlage ist die
[Gap-Analyse](GAP-ANALYSIS.md); das Vorgehen je Maschine steht im
[Portierungs-Leitfaden](PORTING-GUIDE.md).

Leitgedanke: **erst die Blocker, die viele Maschinen gleichzeitig entsperren, dann Inhalt.**
Wer die Maschinen einzeln nachzieht, ohne die neun wiederkehrenden Blocker zu lösen, baut
jedes Mal denselben Umweg neu.

## Stufe 0 — erledigt

- [x] Projektbasis: NEO Edition als Fork mit Historie, Upstream-Remotes eingerichtet
- [x] Syntax-Gate (`tools/syntax-check.sh`) für Umgebungen ohne Maven-Zugriff
- [x] CI baut gegen NeoForge 21.1.228 und liefert das Jar als Artefakt
- [x] Portierungs-Leitfaden aus zwei durchgeführten Portierungen abgeleitet
- [x] **Elektroofen** (`machine_electric_furnace`)
- [x] **Diesel-Generator** (`machine_diesel`) samt `MachinePollutingBlockEntity`
- [x] **Verdichter** (`machine_compressor`) samt `CompressorRecipes`
- [x] **Dampfmaschine** (`machine_steam_engine`)
- [x] **Stirlingmotor** (`machine_stirling`)
- [x] **Kompakter Verdichter** (`machine_compressor_compact`)
- [x] **Mischer** (`machine_mixer`) samt `MixerRecipes`
- [x] **Steinmühle** (`machine_rock_mill`) samt `ModuleMachineRockMill` und `RockMillRecipes`
- [x] **Dampfturbine** (`machine_turbine`)
- [x] **Solarkessel und Heliostatspiegel** (`machine_solar_boiler`, `solar_mirror`) samt Ausrichtwerkzeug
- [x] **Eisenofen und Stahlofen** (`furnace_iron`, `furnace_steel`)

## Stufe 1 — Maschinen-Grundkette

Ziel: der Mod wird spielbar. Strom erzeugen, Erz verarbeiten, Öl verwerten.

> Die ursprüngliche Annahme „alle hier genannten Maschinen haben ihre Abhängigkeiten bereits
> im Port" hat sich für zwei Einträge als falsch erwiesen: Lichtbogenofen und Drehrohrofen
> hängen am NTM-Materialsystem (Schmelze/Tiegel), das im Port nur als Rumpf existiert. Sie
> sind deshalb nach Stufe 2 verschoben.

**Stromerzeugung**
- [x] Verbrennungsmotor samt den vier Kolbensätzen — Runde 6
- [x] Gasturbine — Runde 6
- [x] Turbofan samt eigener Schadensart — Runde 7
- [x] RTG mit zehn Pelletsorten und sechs Zerfallsprodukten — Runde 7. Dafür kamen auch
      `thermo_element` und `rtg_unit` dazu, ohne die es kein Rezept gegeben hätte.

**Verarbeitung**
- [x] Kristallisator samt `CrystallizerRecipes` — Runde 8. `ItemMachineUpgrade` war als Blocker
      veraltet (heißt im Port `MachineUpgradeItem`); von `HbmPlayerProps` brauchte es nur eine
      Stelle, die gemeldet und weggelassen ist.
- [x] Drescher und Sägewerk samt Sägeblatt-Entität — Runde 8. Von den vier genannten Blockern
      war keiner echt: `ModDamageSource.rubble` hat im Port längst ein Gegenstück
      (`NtmDamageTypes.RUBBLE`), `PacketThreading`/`AuxParticlePacketNT` werden durch
      `IParticleCreator` ersetzt, und `InventoryCraftingAuto` braucht das Sägewerk gar nicht.
- [x] **Lichtbogenofen groß und Drehrohrofen → nach Stufe 2 verschoben.** Beide stehen:
      der Lichtbogenofen seit Runde 17, der Drehrohrofen seit Runde 20. Der Versuch in
      Runde 8 wurde begründet abgebrochen, und die Begründung habe ich nachgeprüft: Beide sind
      Schmelzmaschinen, deren Ergebnis kein `ItemStack` ist, sondern eine `Mats.MaterialStack`,
      die als Strahl in einen Tiegel gegossen wird. Im Port ist `Mats.java` ein 42-Zeilen-Rumpf
      mit leeren Maps und dem Vermerk `todo everything???`, der Konstruktor von `NTMMaterial`
      ist auskommentiert, `CrucibleUtil` und `ItemScraps` fehlen ganz. Portiert man sie
      trotzdem, verbrauchen sie Eingang und Strom und geben nie etwas aus.
      Nebenbei korrigiert: es gibt keinen „kleinen" Lichtbogenofen — `machine_arc_furnace` **ist**
      der große. Was im Port liegt, ist der Lichtbogen*schweißer*, eine andere Maschine.

**Verschmutzung**
- [x] Aschegrube, Ziegelschornstein und Industrieschornstein — Runde 10. Damit hat der Rauch
      aus Dieselmotor, Verbrennungsmotor und Doppelofen erstmals einen Abnehmer: der
      Schornstein nimmt ihn über Abgasleitungen entgegen, mindert die Emission auf ein Viertel
      bzw. ein Zehntel und legt den Rückstand als Flugasche und Feinruß in der Aschegrube ab.
      Brennkasten und Backofen füllen dieselbe Grube mit Holz-, Kohle- und Mischasche.
      Dazu kamen `steel_grate` (`BlockGrate` mit zehn Einbauhöhen) und `filter_coal`, beide
      nur als Rezeptbausteine der Schornsteine.

**Energieverteilung**
- [x] Batterieblöcke (5 Stufen) mit Leistungsdiagramm-GUI — Runde 6. Wie im Original ohne
      Rezept und ohne Kreativ-Tab-Eintrag (dort `@Deprecated` und `setCreativeTab(null)`).
- [x] Kondensatoren (einfach und angetrieben) — Runde 7. Die Notiz „braucht eine
      Neuarchitektur" war überholt; `CondenserBaseBlockEntity` lag längst im Port.
- [x] Masten, Umspannwerk, Anschlusskästen (UNINOS-Fernverbindungen) — Runde 5
- [x] Kabelschalter, ummantelter Draht, Kabelspule (`WiringToolItem`) — Runde 5
- [x] Kabeldiode, Kabeldetektor, Kabelanzeige — Runde 7

### Offene Punkte aus den bisherigen Portierungen

Beim Portieren bewusst weggelassen, weil die Grundlage im Port fehlt:

- [x] `EntityCog` und `gear_large` portiert — Runde 9. Der Stirlingmotor wirft beim Überdrehen
      wieder sein Zahnrad aus und lässt sich mit einem neuen reparieren.
      Zwei Einschränkungen, beide aus fehlenden Blöcken: `machine_stirling_steel` und
      `machine_stirling_creative` gibt es im Port nicht, deshalb liefert `getGearMeta()` fest
      das Eisenzahnrad und die Untertypen Stahl und Kreativ sind ohne Funktion (portiert sind
      sie trotzdem, damit beim Nachziehen der Blöcke nur `getGearMeta()` zu erweitern ist).
      Ein Stirlingmotor ohne Zahnrad droppt im Port außerdem einen vollständigen Motor — das
      Original gibt dem Block-Item dafür einen zweiten Untertyp, den der Port nicht hat.
- [x] JEI-Kategorie für die Verdichter-Rezepte nachgetragen (`CompressorRecipeHandler`, Muster:
      `BoilerRecipeHandler`). Die Eingangsdruckstufe steht mit in der Anzeige — ohne sie stünden
      dort scheinbar widersprüchliche Einträge, weil dasselbe Fluid je nach anliegendem Druck ein
      anderes Ergebnis liefert. Beide Bauformen sind als Katalysator hinterlegt.
- [x] Alle vier fehlenden Sounds nachgetragen: `steamEngineOperate` (Dampfmaschine, ein
      Schlag je Umdrehung, Tonhöhe steigt mit der Drehzahl), `warnOverspeed` (Stirling,
      Überdrehzahl-Warnung), Kolbengeräusch des Verdichters (`boltgun`) und das Zischen beim
      Überlauf der Rauchtanks. Letzteres brauchte keine neue Datei: `NTMSounds.VANILLA_HISS`
      ist `random.fizz`, in 1.21 `SoundEvents.FIRE_EXTINGUISH`.
- [x] `RecipeManager.createCheck` für den Elektroofen eingeführt. Vorher lief eine lineare
      Schleife über sämtliche Schmelzrezepte — einmal pro Tick und zusätzlich bei jedem
      Einlagerungsversuch durch einen Trichter.
- [x] Fehlendes Culling in der NEO-Basis behoben (Zentrifuge, Holzbrenner, beide Kessel,
      alle vier Heizer).
- [x] `steel_beam` und `stone_gneiss` nachgereicht.
      Damit hat der Heliostatspiegel wieder sein Originalrezept (ihm fehlte bis jetzt eines),
      und die Steinmühle mahlt wieder Gneis (`rock.schist`).
      **Berichtigung (Stufe 5):** der Satz „beide sind im Original schlichte Blöcke" stimmte
      für `steel_beam` nicht. Es ist dort ein `DecoBlock` mit eigenem Darsteller
      (`RenderSteelBeam` zeichnet `ResourceManager.beam`, ein OBJ-Modell) und der Kollisionsform
      (7\|0\|7)-(9\|16\|9) — eine dünne Säule, kein Würfel. Im Port ist er beides nicht.
      Kollision und Aussehen werden zusammen richtiggestellt, sobald der OBJ-Modelllader steht;
      einzeln geändert sähe man einen Würfel und liefe hindurch.
- [x] `KEY_BLACK` beim Solarkessel: das Original meint den OreDict-Sammelbegriff `dyeBlack`,
      der Port nahm nur `Items.BLACK_DYE`. Jetzt über den Tag `c:dyes/black`.
- [x] Ruß (Fulleren) nachgetragen — Runde 10. Die Notiz „hängt an einer Chemiekette" war
      **falsch**: Ruß entsteht im Original nicht chemisch, sondern im *Industrieschornstein*
      (`TileEntityChimneyIndustrial.cpaturesSoot`), der ihn zusammen mit der Flugasche in die
      Aschegrube darunter ablegt. Portiert sind deshalb Aschegrube, Ziegel- und
      Industrieschornstein samt Flugasche, Feinruß, Stahlgitter und Kohlefilter.
      Das schließt nebenbei die halb gebaute Verschmutzungskette: die drei Rauchtanks in
      `MachinePollutingBlockEntity` hatten bis jetzt überhaupt keinen Abnehmer, ihr Inhalt
      lief nur über und zischte.
- [ ] Kräuterpille (Pheromon) bleibt — geprüft, und die Begründung ist eine andere als
      bisher notiert: `ModItems.pill_herbal` ist ein `ItemPill`, dessen Wirkung komplett auf
      `HbmLivingProps` (Asbest, Staublunge, Strahlung) und `HbmPotion.potionsickness` steht,
      und `Fluids.PHEROMONE` selbst fällt ausschließlich bei den Glyphiden an
      (`EntityGlyphidBrenda`). Beides gehört zu Stufe 4 (Strahlung/Gefahren bzw. Entities),
      nicht zu einer Chemiekette. **Nach Stufe 4 verschoben.**
- [x] Toter Code entfernt: sechs BlockEntities trugen ein `getMaxRenderDistanceSquared()` ohne
      `@Override`. Die Methode gibt es auf `BlockEntity` in 1.21 nicht mehr, sie hatte keinen
      einzigen Aufrufer, und ihr Rückgabewert 65536 entsprach ohnehin exakt dem Quadrat der
      tatsächlich wirksamen `BlockEntityRendererNT.getViewDistance()` von 256.
- [x] `MirrorToolItem`: der Tooltip benutzte `Component.translatable` direkt, das trennt nicht
      am `$`. Das Trennzeichen wäre wörtlich im Tooltip gelandet. Jetzt über
      `I18nUtil.resolveKeyArray` wie die übrigen Items; alle fünf Texte tragen wieder den
      Wortlaut des Originals.
- [x] Amboss-Rezepte für großen Mast und Umspannwerk nachgetragen. Die Notiz aus Runde 5,
      `AnvilRecipes` fehle im Port, war **falsch** — ich hatte die Weglass-Meldung des
      Port-Agenten ungeprüft übernommen. Die Klasse liegt unter
      `inventory/recipes/anvil/AnvilRecipes.java`, samt JEI-Kategorie. Beide Rezepte sind
      jetzt wortgetreu portiert; dafür kam der Tag `ntm:any_concrete` dazu (Gegenstück zu
      `OreDictManager.ANY_CONCRETE`, ohne die gefärbten Betonvarianten, die es im Port
      nicht gibt).
- [x] Doppelofen und Schlacke — **kein Fehler, die Notiz war falsch.** Im Original ist
      `BlastFurnaceRecipes` eine Liste von `Triplet<Object, Object, ItemStack>`, also mit
      genau **einem** Ausgabeprodukt; Schlacke gibt es dort überhaupt nicht. Sie stammt aus
      der Rezeptliste, die der Port für den großen Hochofen neu auf `GenericRecipes`
      aufgebaut hat. Dass der Doppelofen nur `outputItem[0]` nimmt, ist damit näher am
      Original als das Gegenteil. Einziger Rest: JEI zeigt für die geteilten Rezepte eine
      Schlacke, die dieser Ofen nicht ausgibt.
- [x] Doppelofen ohne Werkbankrezept — **keine Lücke, sondern originalgetreu**: auch das
      Original registriert für `machine_difurnace` und `machine_difurnace_extension` keines.
- [x] `ANY_RESISTANTALLOY.ingot()` beim schweren Anschlusskasten zurückgenommen: die Gruppe
      besteht im Original aus Technetium- und Cadmiumlegierung, **beide liegen im Port**
      (`INGOT_TCALLOY`, `INGOT_CDALLOY`). Die Verengung auf `INGOT_DURA_STEEL` aus Runde 5 war
      unnötig; jetzt über den neuen Tag `ntm:any_resistant_alloy`.
- [x] `MINGRADE.wireFine()` → `WIRE_RED_COPPER`: **keine Verengung, die Notiz war falsch.**
      Im Original gibt es gar keinen „normalen" Rotkupferdraht neben einem Feindraht — der
      Feindraht *ist* `wire_fine`, ein `ItemAutogen` über `Mats`, und seine Mingrade-Variante
      heißt dort wörtlich `wire_red_copper` (`ModItems.java:2767`). Der Port hat aus den
      Metadaten-Untertypen eigene Items gemacht; `WIRE_RED_COPPER` ist damit das exakte
      Gegenstück. Die drei Kommentare im Code sind berichtigt.
- [x] Das große Zahnrad fehlt im Amboss-Rezept des Stirlingmotors nicht mehr. Runde 9 hat
      `gear_large` nachgereicht, das Rezept trug den Verzicht von Runde 0 aber noch als
      Kommentar; jetzt wieder wortgetreu (`gear_large` mit Metadaten 0, also Eisen).

## Stufe 2 — die Blocker zentral lösen

Jeder Punkt entsperrt Dutzende weitere Maschinen.

- [x] **`IConfigurableMachine`** wieder eingeführt — Runde 12. `hbmConfig/hbmMachines.json`
      entsteht wieder, mit demselben Dateiformat, denselben Objektnamen und denselben
      Schlüsseln wie im Original — eine Datei aus 1.7.10 lässt sich unverändert
      weiterverwenden. Verdrahtet sind 15 Maschinen: Aschegrube, Zentrifuge, beide
      Kondensatoren, Ölbohrturm, Dieselgenerator, Brennkasten, Frackingturm, Backofen,
      Pumpjack, Radar, Dampfmaschine, Dampfturbine, Leviathan, Stirlingmotor, Heizkessel
      und Industriekessel.
      Bei fünf davon waren die Werte `static final` und mussten dafür erst zu echten
      Feldern werden.
      **Abweichung vom Original**, bewusst: dort sammelt `MachineDynConfig` die Maschinen aus
      der TileEntity-Registry und legt von jeder eine Wegwerf-Instanz an, nur um drei Methoden
      aufrufen zu können. Auf 1.21 hat ein BlockEntity keinen parameterlosen Konstruktor mehr
      — und weil die Konfigurationswerte ohnehin statisch sind, braucht es die Instanz gar
      nicht. Der Port meldet stattdessen ein Paar statischer Methoden je Maschine an.
      Nachgezogen im selben Zug: **Kessel und Industriekessel** sowie das Unterobjekt
      **`M:burnModule`** von Brennkasten und Backofen. Dabei kamen zwei Fehler heraus, die
      beide daher rührten, dass der Port zusammengelegt hat, was im Original getrennt ist:
      * `AbstractBoilerBlockEntity` hatte **eine gemeinsame** Konstante `MAX_HEAT` mit dem
        Wert des großen Kessels. Der kleine Heizkessel bekam damit den **vierfachen**
        Wärmepuffer (12.800.000 statt 3.200.000).
      * Brennkasten und Backofen teilten sich **ein** `ModuleBurnTime`. Im Original hat jeder
        sein eigenes und holt es über `getModule()` — sonst hätte eine Änderung an der
        Konfiguration des Backofens den Brennkasten gleich mit verstellt.
      Damit sind 17 Maschinen verdrahtet. Noch nicht dabei:
      * Maschinen, die es im Port noch gar nicht gibt (Drehrohrofen, Tiegel, Kraftfeld,
        MHD-Turbine, ICF-Steuerung, die beiden RF-Wandler, Industrieturbine, großes Radar,
        Kühltürme, Wasserpumpe).
- [x] **`FluidTrait.onRelease`-Verteiler** wiederhergestellt — Runde 11. Es fehlte genau diese
      eine statische Methode; `FT_Polluting` und `FT_VentRadiation` waren vollständig portiert,
      nur rief sie niemand auf. Mitgezogen wurden die beiden Stellen, an denen der Port
      Freisetzung überhaupt vorsah:
      * Der **Hochofen** klemmte sein Rauchgas mit `Math.min()` am Tankrand ab, statt den
        Überschuss freizusetzen. Dabei fiel auf, dass `FLUE_GAS` im Port 100 war statt 8 —
        im Original sind das 8 **pro Tick**, die Menge je Durchlauf ergibt sich erst aus der
        Laufzeit des Rezepts. Beides berichtigt.
      * `updateLeak` des **Fluidtanks** brach nach zwei Zeilen ab. Der ganze Rest des Originals
        fehlte: Antimaterie-Explosion, brennender Tank samt Entzünden der Umstehenden und die
        Gaswolke — und mit ihnen die einzigen beiden `onRelease`-Aufrufe der Klasse. Ein
        geborstener Tank lief bis jetzt völlig folgenlos aus.
- [x] **`FT_Toxin`** portiert — Runde 14, zusammen mit seinem Abnehmer und dessen ganzer
      Kette, damit es nicht als toter Code endet:
      `Mist` (die Gaswolken-Entität) → Zeta-Splitterbombe Typ 2 → Bomber-Voreinstellung
      `statFacChlorine` → Luftschlag-Designator. Dabei kamen drei stille Lücken heraus:
      * `BombletZeta` im Port kennt die Typen 0, 1 und 4 — **Typ 2, die Chlorbombe, fehlte
        ganz**.
      * `Bomber` hatte keine Chlor-Voreinstellung (im Original gibt es acht, der Port hat drei).
      * Der Luftschlag-Designator legte die **Atombombe auf Untertyp 2**; dort liegt im
        Original das Chlor, die Atombombe auf 4. Jetzt wieder wie im Original nummeriert.
      Chlor, Phosgen, Senfgas und Rotschlamm haben ihre Gifteigenschaften zurück.
      Nicht portiert: der Pheromon-Zweig der Wolke (wirkt nur auf Glyphiden) und
      `EntityChemical`.
      **Abweichung mit Grund**: `ToxinDirectDamage` hält den `ResourceKey<DamageType>` statt
      einer fertigen `DamageSource`. Auf 1.21 hängt eine `DamageSource` an der Registry der
      Welt und lässt sich beim statischen Aufbau der Fluidliste gar nicht bilden.
- [x] **`Capabilities.ItemHandler.BLOCK`** registriert — Runde 11 (`com.hbm.lib.NtmCapabilities`).
      Vorher war **keine einzige** Capability angemeldet: Trichter kamen an die Maschinen heran,
      weil sie direkt gegen `Container` arbeiten, Rohre und Automatisierung anderer Mods fragen
      aber ausschließlich über Capabilities und fanden gar nichts. Angemeldet wird pauschal für
      jeden `BlockEntityType` des Mods; ob ein Inventar dahintersteht, entscheidet der Anbieter
      zur Laufzeit — so hängt jede künftige Maschine ohne Zutun mit drin.
- [ ] **`Capabilities.FluidHandler.BLOCK`** — geht noch nicht, und zwar aus einem konkreten
      Grund: HBM führt seine Fluide in einer eigenen Liste (`com.hbm.inventory.fluid.FluidType`),
      die mit der Fluid-Registry von Minecraft nichts zu tun hat. `NtmFluids` meldet dort nur die
      beiden Weltfluide Vulkanlava an. Eine Fluid-Capability braucht deshalb zuerst eine
      Zuordnung `FluidType` → `net.minecraft.world.level.material.Fluid` für rund 150 Fluide.
      Das ist ein eigener Arbeitsschritt und zieht Eimer, Fluid-Rendering und Fremdmod-Rohre
      gleich mit.
- [x] **RF/FE-Anbindung** — Runde 13, und der Eintrag war in der Formulierung irreführend.
      Eine „Brücke" zu *entwerfen* wäre falsch gewesen: das Original hat bewusst **keine**
      durchgehende Verbindung zwischen seinem Stromnetz und RF. Der einzige Übergang sind
      zwei ausdrückliche Wandlerblöcke, `machine_converter_he_rf` und
      `machine_converter_rf_he`, und die sind jetzt portiert — mit den Umrechnungen des
      Originals (5 HE → 1 RF, 2 RF → 5 HE), beide über `MachineDynConfig` einstellbar.
      An die Stelle der COFH-Schnittstelle tritt NeoForges `IEnergyStorage`, angemeldet über
      `Capabilities.EnergyStorage.BLOCK` — und zwar **nur** an diesen beiden Blöcken, damit
      die Trennung erhalten bleibt.
- [x] **Kondensatoren** — der Eintrag war doppelt falsch. Erstens sind sie seit Runde 7 im Port
      (`CondenserBlockEntity`, `CondenserPoweredBlockEntity` auf `CondenserBaseBlockEntity`).
      Zweitens stimmte schon die Beschreibung nicht: `MachineCondenser` ist im Original ein
      schlichter `BlockContainer` mit TileEntity und hält überhaupt nichts in Metadaten. Die
      Notiz beschrieb offenbar eine andere Maschine.
- [x] **`ForgeDirection.UNKNOWN`** — bereits in Runde 5 gelöst, der Eintrag war nur nie
      abgehakt worden. Richtungslose Verbindungen laufen im Port über `dir == null`;
      `DirPos.getStepX/Y/Z` liefern dafür 0 und `getOppositeDir` gibt `null` zurück, und
      `UniNodespace.checkConnection` rechnet damit. Ohne das wäre jede Fernverbindung von
      Masten und Umspannwerk mit einer NPE abgebrochen — sie funktionieren seit Runde 5.
- [x] **Dummy-Blöcke ohne Proxy** — erledigt in Runde 11, und die Notiz aus Runde 10 war dabei
      **zu pauschal**: sie behauptete, der Port bilde `DUMMY` „durchweg" auf `null` ab und das
      betreffe „alle bisher portierten Maschinen gleichermaßen". Der Abgleich aller 44
      `DummyableBlock`-Ableitungen gegen ihre Originale zeigt etwas anderes — im Original
      vergeben nur **6** davon überhaupt einen Proxy an reine Dummy-Felder, und in 38 Fällen
      stimmte der Port bereits. Die sechs Abweichungen sind jetzt korrigiert:
      Lichtbogenschweißer, Lötstation, Aschegrube, Holzbrenner, Brennkasten und Backofen.
      Bei vier davon (Aschegrube, Lötstation, Brennkasten, Backofen) wog das schwerer als
      gedacht: sie überschreiben `fillSpace` gar nicht, erzeugen also nie ein `EXTRA`-Feld —
      ihr Proxy-Zweig war damit unerreichbar und die gesamte Hülle dieser vier Maschinen
      schlicht tot. Ein Trichter kam nur an den Kernblock heran.

- [~] **NTM-Materialsystem (`Mats`/`NTMMaterial`).** Fundament steht seit Runde 15.
      `Mats.java` war ein 42-Zeilen-Rumpf mit leeren Maps und dem Vermerk `todo everything???`;
      jetzt stehen dort alle **108** Materialdeklarationen des Originals, `NTMMaterial` hat
      wieder einen Konstruktor, und `MaterialShapes` kann aus Form und Material einen Tag
      bilden.
      **Die architektonische Entscheidung dahinter**: im Original hängt jedes Material an einem
      `DictFrame` des OreDictionary, und die Zuordnung Gegenstand→Material läuft über dessen
      Namen — `"ingot" + "Steel"` ergibt `ingotSteel`. Der Port hat keinen OreDictionary; auf
      1.21 treten Item-Tags an seine Stelle. Aus demselben Paar wird hier `c:ingots/steel`.
      Der Namensraum ist bewusst `c` und nicht `ntm`, damit der Tiegel später auch Barren
      anderer Mods findet — genau das leistete der OreDictionary im Original.
      Die Tags entstehen im Datengenerator aus den Namen der Gegenstände, weil der Port sie
      ohnehin nach Form und Material benennt. Derzeit ergibt das **178 Tags** über elf Formen.
      Seit Runde 16 steht auch `MatDistribution`: die Einträge, die sich nicht aus Form und
      Material ergeben — Loren, Klingen, Rohre, Asche und vor allem die **Erze samt ihrer
      Nebenprodukte**. Die Erze des Mods hängen dafür jetzt in den gemeinsamen Tags
      (`c:ores/uranium` und so fort), was sie nebenbei auch für andere Mods sichtbar macht.
      Die Liste ist wie im Original über `hbmCrucibleSmelting.json` einstellbar.
      Seit Runde 17 steht das Giessen: `ICrucibleAcceptor` und `CrucibleUtil` (der Strahl von
      oben nach unten und was ihn auffängt), `ScrapsItem` als Materialklumpen und als erste
      Maschine darauf der **große Lichtbogenofen** — damit ist das in Runde 8 gegebene
      Versprechen zur Hälfte eingelöst.
      Bei den Rezepten weicht der Port an drei Stellen vom Original ab, alle drei aus einem
      Grund: auf 1.21 gibt es zum Registrierzeitpunkt (`FMLCommonSetup`) weder gebundene Tags
      noch geladene Rezepte. Die Autogenerierung aus Form und Material und die Übernahme der
      Schmelzofenrezepte laufen deshalb beim Nachschlagen statt beim Registrieren, und die
      Kollisionsprüfung greift nur noch bei Gegenstandsrezepten. Das Ergebnis ist dasselbe,
      nur ohne zweitausend Leerrezepte in der Vorlagendatei.
      Runde 18 schliesst den Kreis: **Gießform und Gießbecken** nehmen den Strahl auf, den der
      Lichtbogenofen abgibt. Damit hat `ICrucibleAcceptor` seinen ersten Implementierer, und aus
      geschmolzenem Metall wird wieder ein Gegenstand.
      Zwei Dinge mussten dafür anders gelöst werden als im Original: Der Strahl fragt jetzt den
      **Umriss** eines Blocks ab statt seines Kollisionskörpers — der Trog ist in der Mitte hohl,
      der Guss fiele sonst hindurch; das Original benutzt dort die Blockgrenzen, was auf 1.21 dem
      Umriss entspricht. Und `IRenderFoundry.getLevel` heißt im Port `getFillLevel`, weil der Name
      auf 1.21 in `BlockEntity` mit der Welt belegt ist.
      Von den 24 Formen des Originals sind **elf** übernommen — genau die, deren Ergebnis es im
      Port gibt. Gussplatte, dichter Draht, Rohr, Block, Hülsen und die sieben Waffenteile bleiben
      draußen, solange ihre Form keine Gegenstände hat: man könnte sonst hineingießen, ohne je
      etwas herauszubekommen.
      Runde 19 setzt den **Tiegel** und den **Gießkanal** darüber. Der Tiegel zieht Hitze aus dem
      Block unter sich, schmilzt ein, was man hineinwirft, trennt dabei das Rezeptband vom Abraum
      und gießt beides aus je einem eigenen Ausguss. Der Kanal führt die Schmelze waagerecht
      weiter; sein Netz (`FoundryNetwork` über UNINOS) dient allein der Sortenreinheit, damit
      nicht zwei Metalle von zwei Seiten in denselben Strang laufen.
      Nicht übernommen: die drei Stahlvarianten für GregTech 6 — sie hängen an einer Modabfrage,
      die der Port nicht hat.
      Runde 20 holt den **Drehrohrofen** nach. Damit ist das Versprechen aus Runde 8 ganz
      eingelöst: beide dort begründet zurückgestellten Öfen stehen. Er verlangt dreierlei —
      Zutaten, Dampf und ein Feuer darunter —, und das Feuer bestimmt nicht die Temperatur,
      sondern das Tempo: ein besserer Brennstoff lässt die Trommel schneller laufen und frisst
      dafür überproportional mehr Dampf.
      Nicht übernommen: die drei Rezepte auf Eisenbruchstücke, die es im Port als Gegenstände
      noch nicht gibt.
      Runde 21 schließt die letzten beiden Lücken dieses Blocks.
      **Gussplatte und dichter Draht** hängen jetzt am Materialsystem. Sie liegen im Port als je
      EIN Gegenstand mit Metadaten vor, und ein Tag kann keine Metadaten unterscheiden —
      `c:triple_plates/iron` müsste `cast_plate` als Ganzes enthalten und träfe damit auch die
      Goldplatte. Statt Tags gehen diese beiden Formen deshalb über ausdrückliche Einträge
      (`ComparableStack` kennt die Metadaten); die Zuordnung Untertyp→Material steht in
      `MatShapeItems`. Damit lassen sie sich einschmelzen, und die vier zugehörigen Formen
      (Gussplatte einzeln und zu dritt, dichter Draht einzeln und zu neunt) sind wieder da.
      **Drei JEI-Ansichten** kommen dazu: Lichtbogenofen, Legieren im Tiegel und Gießen.
      Was noch offen bleibt: der Schrottklumpen trägt weiterhin eine eingefärbte
      Graustufentextur statt der 108 eigenen des Originals. Das Original erzeugt sie zur
      Laufzeit aus einer Graustufenvorlage (`ItemAutogen`); auf 1.21 gibt es dafür keinen
      einfachen Weg, und der Nutzen wäre rein kosmetisch.
- [x] Entscheidung zu **OpenComputers** (57 Maschinen mit Callbacks): ersatzlos gestrichen,
      siehe [`ENTSCHEIDUNGEN.md`](ENTSCHEIDUNGEN.md). Ursprüngliche Formulierung: streichen oder eine
      eigene Automatisierungs-API entwerfen.

## Stufe 3 — Reaktoren

Erst sinnvoll, wenn Stufe 2 steht. Das Fundament steht seit Runde 22: Neutronen-Nodespace,
Dials und der flussführende Kern des RBMK laufen.

- [x] **Neutronenfluss-Framework** (Runde 22). `NeutronNode`, `NeutronStream`, `NeutronNodeWorld`,
      `NeutronHandler` und `RBMKNeutronHandler` laufen serverseitig im `ServerTickEvent.Pre`.
      Knoten werden sowohl in `setRemoved` als auch in `onChunkUnloaded` abgeräumt; der Unterschied
      zwischen Abbau und Chunk-Entladung hängt am `isLoaded`-Flag der Basisklasse, damit ein
      entladener Chunk keine Kernschmelze auslöst.
- [x] **`RBMKDials`** (Runde 22). Die 24 Stellgrößen liegen in einem `SavedData` pro Welt und
      werden über `/ntmrbmk list|get|set|reset` gestellt — 1.21 kennt nur statisch registrierte
      `boolean`/`int`-Gamerules, 18 der Dials sind aber Kommazahlen.
- [ ] RBMK: 33 BlockEntities, 34 Blöcke, 17 Renderer, Brennstab-Items mit Xenonvergiftung
      - [x] Runde 22: Saeulenbasis (`RBMKBaseBlock`/`RBMKBaseBlockEntity`) mit Wärmeausgleich,
        passiver Kühlung, Deckelsystem und Kernschmelze
      - [x] Runde 22: Leersäule, Moderator, Absorber, Reflektor
      - [x] Runde 22: Brennkanal (normal und moderiert) samt Oberfläche
      - [x] Runde 22: Steuerstab von Hand (normal und moderiert) samt Oberfläche
      - [x] Runde 22: 33 Brennstab-Items mit Abbrand, Xenonvergiftung, Kern- und Hüllentemperatur
      - [x] Runde 22: Reaktorschutt (`rbmk_debris`, `rbmk_debris_burning`)
      - [x] Runde 23: Dampferzeuger mit Verdichter, Kühlsäule, Lagersäule
      - [x] Runde 24: Wärmetauscher, Wassereinlass, Dampfauslass
      - [x] Runde 25: Bestrahlungssäule samt Bestrahlungsrezepten
      - [x] Runde 27: ReaSim-Brennkanal (beide Bauformen) und selbsttätiger Steuerstab
      - [x] Runde 28: Einblendung beim Hinsehen (Säulentemperatur, Fluss, Abbrand, Fahrhöhe)
      - [x] Runde 29: Reaktorpult samt Payload-und-Öffnen-Handler für containerlose
        Oberflächen, `RBMKColumnType`, `RBMKColor`, Konsolenanbindung aller Säulen und
        Verbindungsstab. Der Öffnen-Handler gilt ab jetzt auch für die Anzeigetafeln.
      - [x] Runde 33: Kranpult und Laufkran. Der Zielpunkt kommt vom Verbindungsstab, die vier
        Reichweiten misst das Pult selbst bis zur nächsten Wand; gefahren wird mit den vier
        Richtungstasten, geladen mit der Ladetaste. Dafür sind die fünf Krantasten nachgereicht
        (sie standen in `EnumKeybind`, hatten aber keine Belegung), `IRBMKLoadable` liegt auf
        Brennkanal und Lagersäule, und der Renderer zeichnet Pult, Zeiger, Lampen und den
        Laufkran über dem Reaktorsaal. **Abweichungen:** kein OpenComputers-Bauteil (gibt es für
        1.21 nicht); der Tisch ist einlagig statt hinten zweilagig, weil die Dummyable-Blöcke des
        Ports nur ein Maß je Bau kennen.
      - [x] Runde 34: der selbsttätige Lader. Er steht auf einem Brennkanal, tauscht dessen Stab,
        sobald die Restanreicherung unter den eingestellten Wert fällt, und zieht den alten in
        eines von neun Ausgabefächern. **Abweichungen:** keine Fahrschleife aus der Tonsammlung
        und keine Dampfsäule am unteren Umkehrpunkt -- Schleifengeräusche und die Partikelsorte
        `tower` sind nicht portiert.
      - [x] Runde 36: Werkbankrezepte für den RBMK-Zweig. 27 Rezepte, wortgetreu aus
        `CraftingManager.java`, dazu das Montagemaschinen-Rezept `ass.rbmk` für die Leersäule --
        ohne das bleiben alle anderen wirkungslos. Neu dafür: `block_graphite`, `glass_lead`,
        `glass_boron`, `deco_rbmk`, `deco_rbmk_smooth`.
        **Nicht übernommen:** `rbmk_control_reasim` und `rbmk_control_reasim_auto` -- im Original
        stehen sie im `else`-Zweig von `if(!GeneralConfig.enable528)` und sind nie gleichzeitig
        mit ihren Gegenstücken registriert; ihre Muster sind zeichengleich. Rezept-JSONs können
        zur Laufzeit nicht umschalten, und `enable528` ist voreingestellt aus. Ebenfalls offen,
        weil die Blöcke fehlen: `rbmk_loader`, `rbmk_tool` und die neun Tafeln.
      - [x] Runden 43-46: **alle neun Anzeigetafeln**. Zeiger (43), Leuchte (45), Ziffern,
        Hebel, Tasten, Kurve, Raster, Terminal und die Blankotafel (46). Darunter der gemeinsame
        Basisblock `RBMKMiniPanelBlock` mit den vier Formen aus dem Original.
        Die Blankotafel ist die einzige mit einem richtigen Blockmodell -- alle anderen zeichnet
        ihr Renderer. **Nicht übernommen:** die OpenComputers-Anbindung jeder Tafel; beim Terminal
        fällt damit auch der `ocMode` weg. Die Funk-Redstone-Anbindung des Terminals ist dagegen
        mitportiert.
      - [x] Runde 30: Steuerstab-Bauformen mit Strom (ReaSim und ReaSim-Auto)
      - [~] Runde 35: die beiden Renderer, an denen der Reaktor tatsächlich ablesbar wird --
        `RenderRBMKControlRod` (Deckelhöhe = Fahrhöhe des Steuerstabs, eingefärbt nach Farbgruppe)
        und `RenderRBMKFuelChannel` (Stabbündel in der Brennstofffarbe, dazu das
        Tscherenkow-Leuchten ab fünf Einheiten Fluss). Dafür kam `RBMKBaseBlock.columnHeight`
        dazu. **Offen bleiben neun Renderer**: `RenderRBMKConsole` sowie die acht Tafel-Renderer,
        die ohne die Tafel-Block-Entitäten nicht einmal übersetzen.
      - [~] Runde 48: Trümmer-Entitäten (Runde 42) und **Corium** (Runde 48). Ein geladener
        Brennkanal läuft bei der Kernschmelze wieder von oben bis unten mit Corium voll, statt
        einzustürzen -- das war seit Runde 42 als Abweichung vermerkt. Dazu die beiden
        Erstarrungsstufen (dichter Block aus Quellen, poröser Schutt aus dem Ausgelaufenen, der
        Radon ausgast). Runde 49: **strahlender Schutt** -- der Schutt um eine ausgelaufene
        Brennstoffsäule wird selbst zur Strahlenquelle. Runde 50: **Überdruckereignis und
        Pilzwolke** -- das Dampfnetz der geschmolzenen Anlage zerreißt, und über dem Reaktor steht
        eine Wolke. Runde 65: **Digamma** -- schmilzt ein Kanal mit DRX-Brennstoff, wird aus dem
        Schutt ringsum kein strahlender, sondern Digamma-Schutt, der nicht abklingt, sondern
        sich weiterfrisst. **Offen bleibt daran nur der Digamma-Speer**, der im Original nach
        der Kernschmelze vom Himmel sinkt: er braucht `ExplosionNT` samt seinen
        DIGAMMA-Attributen und den Aschenblock `ash_digamma`, beides nicht portiert.
      - [x] Runde 26: Brennstäbe sind herstellbar (leerer Stab plus acht Billets)
      - [x] Runde 32: Brennstoffpellets und das Zerlegen abgebrannter Stäbe. 32 Pellets mit den
        zehn Zuständen des Originals (fünf Abbrandstufen, jeweils mit und ohne Xenonvergiftung);
        die drei Render-Durchgänge des Originals sind Modellüberschreibungen über `item_meta`
        geworden. Das Zerlegen ist ein `CustomRecipe` im Werkbankraster — dafür gibt es jetzt
        `NtmRecipeSerializers`. **Abweichung:** das Pellet leitet nicht von `ItemNuclearWaste` ab,
        weil `EntityItemWaste` nicht portiert ist; fallen gelassene Pellets verfallen also
        normal. Ebenso fehlt der `HazardSystem`-Eintrag — die Konstante `rod_rbmk` liegt bereit,
        aber weder Stäbe noch Pellets sind bisher als Strahlungsquelle eingetragen.
- [x] Containerlose RBMK-Oberflächen: der Weg steht (Runde 29, `OpenScreenPacket` plus
      `NoContainerScreens`); die übrigen zehn folgen mit ihren Blöcken
### Was in Stufe 3 noch fehlt — vollständige Aufnahme (Stand Runde 36)

Kartiert mit zehn parallelen Lese-Agenten plus einem Vollständigkeitskritiker, jede Angabe am
Original **und** am Port nachgeprüft. Zeilenangaben sind geschätzter Portier-Aufwand inklusive
Registrierung, Datengeneratoren und Sprachtexten, ohne den ersatzlos entfallenden
OpenComputers-Code.

**Die zehn kartierten Teilsysteme — zusammen rund 34.400 Zeilen:**

| Zeilen | Stand | Teilsystem |
|---|---|---|
| ~~9.150~~ 0 | **fertig** | **Fusion** — Runde 66: Torus und Klystron samt den beiden Netzen. Runde 68: Kollektor, Koppler, Boiler und MHD-Turbine. Runde 69: Brutreaktor. Runde 70: Plasmaschmiede |
| ~~4.940~~ | **erledigt** | ~~**RBMK-Anzeigetafeln**: Zeiger, Leuchte, Numitron, Graph, Tastenfeld, Hebel, Terminal, Rasteranzeige, Blankotafel~~ (Runden 43-46) |
| ~~4.380~~ | **erledigt** | ~~**Chicago Pile MK2** (`pile_block`/`pile_brick`/`pile_device`)~~ (Runden 60-61), Wiederaufbereitung in Runde 63. Der alte, veraltete CP-1 aus Graphitblöcken bleibt draußen — er trägt im Original durchgehend `@Deprecated` |
| ~~4.010~~ | **erledigt** | ~~**ICF**: Trägheitsfusionsreaktor, Laseranlage, Pelletpresse, Brenner~~ (Runden 62-63) |
| ~~3.395~~ | **erledigt** | ~~**PWR** (Druckwasserreaktor)~~ — Runde 54 die Anlage, Runde 63 die 15 Wiederaufbereitungsrezepte. Der PWR-Drucker ist gestrichen (ENTSCHEIDUNGEN.md, Punkt 5) |
| ~~2.910~~ | **erledigt** | ~~**Watz-Reaktor**~~ (Runden 57-59) |
| ~~2.225~~ 0 | **fertig** | **Forschungs- und Brutreaktor** — Runde 71. **Im Original totes Altlastenwerk, siehe Befund unten** |
| ~~1.495~~ | **erledigt** | ~~**RBMK-Renderer** — alle 13~~ (Kranpult, Lader, Steuerstab, Brennkanal, die acht Tafeln und zuletzt das Reaktorpult, Runde 47) |
| ~~1.300~~ ~~850~~ ~~450~~ ~~300~~ ~~100~~ | **erledigt** | ~~**RBMK-Kernschmelze und Folgen**~~ — Trümmerflug (42), Corium (48), strahlender Schutt (49), Überdruck und Pilzwolke (50), Digamma-Schutt (65). Der Digamma-Speer hängt an `ExplosionNT` und kommt mit diesem Teilsystem |
| ~~580~~ | **erledigt** | ~~Werkbankrezepte für alle RBMK-Blöcke~~ (Runde 36) |

**Acht Teilsysteme, die die Roadmap bisher gar nicht führte — zusammen rund 8.300 Zeilen.**
Der Kritiker fand eine systematische Blindstelle: die Roadmap listet die *Reaktoren*, aber nicht
den *Brennstoffkreislauf* um sie herum.

- [x] **Runde 40 — Radiolyse-Kammer.** Zehn RTG-Pellets bestrahlen eine Flüssigkeit und spalten
  sie in zwei Fraktionen; zehn HE je Wärmepunkt und Tick. Die Krackliste (`CrackingRecipes`,
  zwölf Einträge) kam mit, weil die Radiolyse sie vollständig übernimmt — der Krackturm selbst
  findet sie später vor. **Nicht übernommen:** die Entkeimung. Sie hängt am
  `ntmContagion`-Vermerk des MKU-Seuchenzweigs, den es im Port nicht gibt; die beiden Fächer
  kommen mit diesem Zweig nach.
- [x] **Runde 41 — PUREX-Wiederaufbereitung.** Fünf mal fünf Felder, drei Eingabe- und sechs
  Ausgabefächer, drei Eingabetanks und ein Ausgabetank. 22 der 59 Rezepte portiert: die neun
  ZIRNOX-Sorten, die sieben Brennstoffplatten, Schraranium, die Schrabidium-Säurewäsche und die
  drei Sonderrezepte. Neu dazu der Abfallkrumen (`nuclear_waste_tiny`) — und die
  Strahlungswerte der ganzen Abfallfamilie, die im Port noch fehlten.
  **Nicht übernommen, jede Gruppe kommt mit ihrem Reaktor nach:** 4 Pile-, 15 PWR-, 12 Watz-,
  2 Schrabidium-PWR-Rezepte, das ICF-Rezept, die drei Verglasungsrezepte (brauchen Bleisand,
  `sand_mix`) und das Thoriumsalz (Flüssigsalzreaktor).
- [x] **Runden 52-53 — RTG-Kreislauf.** Der RTG-Ofen (drei Pellets, normale Schmelzrezepte) und
  der RTG-Doppelofen (sechs Pellets, Hochofenrezepte, ab 15 Wärmepunkten). Beide ohne Brennstoff
  und ohne Strom; die Pellets altern auch im Leerlauf, Zerfall kennt keine Pause.
  Die Rezeptrechnung des Hochofens steht dabei einmal statt zweimal: `consumeInputs` und
  `getPrimaryOutput` sind aus `MachineDiFurnaceBlockEntity` herausgezogen, im Original stehen
  sie in beiden Öfen.
  **Befund:** der RTG-Ofen ist im Original `@Deprecated`, steht in **keinem** Kreativreiter und
  hat **kein** Rezept — er ist dort überhaupt nicht erreichbar; der Doppelofen ist wenigstens im
  Maschinenreiter. **Abweichung:** im Port stehen beide im Kreativreiter, sonst wären sie totes
  Inventar. Ein Rezept bekommen sie nicht — das wäre erfundener Inhalt.
- [x] **Runde 38 — Brennstoffbecken und Abfall-Items.** Die 16 `waste_*`-Items mit heißem und
  abgekühltem Zustand, `FuelPoolRecipes` und die Trommel (`machine_waste_drum`): zwölf Fächer,
  zählt die angrenzenden Wasserblöcke, kühlt RBMK-Stäbe über ihre Wärmewerte und alles andere
  über die Rezeptliste. Eine Stunde bei einer Wasserseite, zehn Minuten bei sechs.
  **Nicht übernommen:** die Vorratstrommel (`machine_storage_drum`) — sie zerfällt Lang- und
  Kurzzeitabfall und hängt an `ItemWasteLong`/`ItemWasteShort`, einem eigenen Zweig, der im Port
  ganz fehlt. Ebenso die PWR-Einträge (Reaktor fehlt) und die RBMK-Stab-Einträge der Rezeptliste
  (das Original erreicht sie nie — die Trommel prüft zuerst auf einen Stab)
- [x] **Runde 71 — Reaktorsteuerung** (`MachineReactorControl`, `ItemReactorSensor`, 700 Zeilen).
  **Gehört zum toten Zweig des Forschungsreaktors** (Runde 51): auch sie ist `@Deprecated` und
  bezieht ihre Werte ausschließlich von `TileEntityReactorResearch`. Sie kam deshalb mit ihm.
- [x] **Runde 64 — JEI-Ansichten des Reaktorzweigs.** Acht Ansichten: die PUREX-Wiederaufbereitung
  (alle 54 Rezepte), die Radiolyse, der ZIRNOX, der Watz, der PWR, das Abklingbecken, der
  RTG-Zerfall und die Zerlegung der RBMK-Brennstäbe. Die sechs einfachen „eins rein, eins raus"-
  Ansichten teilen sich eine gemeinsame Basisklasse, wie im Original der `NEIUniversalHandler`.
  **Nicht übernommen:** der Abfallzerfall (`ItemWasteShort`/`ItemWasteLong` fehlen im Port), das
  Kracken (der Krackturm fehlt, und seine Rezepte stehen ohnehin schon in der Radiolyse-Ansicht)
  und der `ConstructionHandler`, der Mehrblockbauten als Geistervorschau zeigt — dieselbe
  gestrichene Familie wie `SmallBlockPronter` (ENTSCHEIDUNGEN.md, Punkt 4).
  **Nebenbei repariert:** die ZIRNOX-Brennstofftabelle stand im Konstruktor der Blockentität und
  war damit erst gefüllt, sobald irgendwo ein ZIRNOX stand. Sie steht jetzt in einer eigenen
  Methode, die auch die JEI-Ansicht anstößt.
- [x] **Runde 42 — Trümmerflug bei RBMK-Kernschmelze und ZIRNOX-Zerlegung.** Die beiden
  Trümmer-Entitäten (je sechs Sorten) samt Renderern, sieben aufsammelbare Trümmer-Items und
  elf Modelle. `DebrisBase` lag schon im Port und hatte bis dahin keinen einzigen Nutzer.
  Der ZIRNOX hinterlässt jetzt auch sein Verstrahlungsfeld.
  **Abweichungen:** die Prallbewegung nutzt die Kollisionsmeldung von `move()` statt einer
  eigenen `moveEntity`-Kopie; die Strahlung wirkt unmittelbar statt über einen
  Strahlungs-Statuseffekt, den es im Port nicht gibt; ein geladener Brennkanal stürzt ein,
  statt mit Corium ausgegossen zu werden — Corium gehört zur Kernschmelze-Einheit.
- [x] **Runde 39 — Strahlungsbewertung des Reaktorzweigs.** Die vier `HazardModifier` des
  Originals und 128 Einträge: 33 RBMK-Stäbe, 32 Pellets, die elf ZIRNOX-Untertypen samt
  abgebrannten Gegenstücken, sieben Brennstoffplatten, 16 Abfallsorten, zehn RTG-Pellets.
  Nebenbei repariert: `ZirnoxRodItem.getBarWidth` rechnete mit einer Ganzzahldivision und
  lieferte den Balken genau andersherum.

`FuelPoolRecipes` ist die **einzige** Stelle, an der ein heißer RBMK-Stab abkühlt und an der
`pwr_fuel_hot` zu `pwr_fuel_depleted` wird. Wer das Becken später baut, muss jeden vorher
gebauten Reaktor noch einmal anfassen.

**Querliegende Infrastruktur (Runde 37 -- weitgehend erledigt):**

- [x] `com.hbm.util.function.Function` (+ Passive/Linear/Log/Sqrt/SqrtFalling/Quadratic) → PWR, Watz, ICF
- [x] `ToolConversionBlock` (+ `Pillar`) → Watz, ICF, Fusion. Der Umbaukatalog ist noch leer; die
      vier Einträge des Originals stehen als Kommentar bereit, weil ihre Blöcke fehlen.
- [x] Fehlermarker beim Assemblieren: `RenderMarkers` + `MarkerCreator.sendError` → PWR, Pile, ICF, Watz
- [x] Sieben Reaktor-Geräusche (`lever_start`, `lever_stop`, `spark` ×6, `large_turbine_running`,
      `fel`, `fusion_reactor_running`, `reactor_loop`) → Tafeln, Fusion, PWR, Pile
- [x] `ExplosionNukeGeneric.waste` / `wasteNoSchrab` / `wasteDest` → ZIRNOX-Zerlegung, RBMK-Trümmer
- [x] `IInsertable`, `IBlowable`, `TickingBaseBlockEntity` → Chicago Pile, ICF
- [x] `RenderSparks`, Schaltkreis `NUMITRON`, `block_boron` → Tafeln, Brutreaktor, ICF, Pile
- [x] `NumberDisplay` (Siebensegmentanzeige für Oberflächen) → Brutreaktor, ICF
- [x] `NoteBuilder` → Anzeigetafeln
- [x] Endliches Fluid und `corium_block` → **Runde 48**. Ein eigenes Fluid mit Quelle, Fluss,
      zwei Erstarrungsstufen und Strahlung. Es stand hier als „gehört zur Kernschmelze“, und
      genau dort ist es gelandet.
- [ ] ~~`ExplosionNukeGeneric.vapor` → gehört zur Kernschmelze~~ **BERICHTIGT (Runde 52):** Die
      Zuordnung war falsch. `vapor`/`vaporDest` wird im Original an **genau einer** Stelle
      benutzt, nämlich in `ExplosionNukeAdvanced` — die RBMK-Kernschmelze ruft es nirgends auf.
      Der Punkt gehört zu den Sprengköpfen in Stufe 4, nicht hierher.

Damit sind die querliegenden Kleinteile **abgeschlossen**.

**Die vier Entscheidungen sind getroffen** und stehen in [`ENTSCHEIDUNGEN.md`](ENTSCHEIDUNGEN.md):
OpenComputers, Connected Textures, `SatelliteRayScan` und die `SmallBlockPronter`-Vorschauen
werden ersatzlos gestrichen; `IBlockMulti` wird durch Blockzustände ersetzt. Zusammen sparen sie
über 1.500 Zeilen.

**Empfohlene Reihenfolge** (so wartet nichts auf Unfertiges): ~~querliegende Kleinteile →
Entscheidungen → Abfall-Items + Brennstoffbecken → PUREX/Radiolyse → Strahlungsbewertung~~ →
~~RBMK-Trümmer zusammen mit der ZIRNOX-Zerlegung~~ → ~~Tafeln~~ (Runden 43-46) →
~~RBMK-Renderer~~ (Runde 47) → ~~Corium~~ (48) → ~~strahlender Schutt~~ (49) → ~~Überdruck und Pilzwolke~~ (50) → **als Nächstes:**
~~PWR~~ (Runde 54) → ~~Watz~~ (Runde 58) → ~~Chicago Pile~~ (Runde 60) → ~~ICF~~ (Runden 62-63) →
~~Fusion~~ (Runden 66-70) → ~~Forschungs-/Brutreaktor mit der Reaktorsteuerung~~ (Runde 71).
**Stufe 3 ist damit abgeschlossen.**

**Warum der Forschungsreaktor ans Ende gerutscht ist (Befund aus Runde 51).** Die Reihenfolge
hatte ihn als Nächstes; bei der Vorbereitung fiel auf, dass er im Original nicht spielbar ist:

- `TileEntityReactorResearch` trägt `@Deprecated`, ebenso alle sieben `plate_fuel_*`-Items.
- Die Brennstoffplatten haben **kein einziges Rezept** — weder Werkbank noch Maschine. Sie stehen
  nur in der Registry, in `HazardRegistry` und in der Brennstofftabelle des Reaktors selbst.
- `setCreativeTab(null)` nimmt sie zusätzlich aus dem Kreativinventar.

Der Reaktor selbst steht im Kreativreiter, lässt sich also bauen, aber nicht betreiben. Und weil
der Brutreaktor seinen Fluss ausschließlich vom Forschungsreaktor bezieht, hängt der ganze Zweig
daran. Er wird trotzdem portiert — das Ziel ist Vollständigkeit —, aber nach den Reaktoren, die
im Original wirklich laufen.

**Stufe 3 ist vollständig** (Stand nach Runde 71).

**Der tote Zweig (Runde 71).** Er ist trotzdem portiert worden -- das Ziel ist Vollständigkeit.
Was dazugehört:

- Die sieben Brennstoffplatten sind jetzt echte Gegenstände mit Kennlinie und Lebensdauer
  (`PlateFuelItem`, `FuelRodItem`) statt leerer Hüllen. Ihr Verbrauch steht in einer
  Datenkomponente, nicht mehr in losem NBT.
- Der **Forschungsreaktor**: zwölf Platten in einem Becken, das man selbst fluten muss. Jede Platte
  bekommt den Fluss ihrer Nachbarn, reagiert nach ihrer Kennlinie und gibt das Ergebnis weiter --
  multipliziert mit der Stabstellung. Gekühlt wird mit dem Wasser ringsum; bei 50.000 Wärme fliegt
  er auseinander und hinterlässt Korium. Wer ihn nicht abschirmt, verstrahlt die Umgebung.
- Der **Brutreaktor**: zählt den Fluss aller Forschungsreaktoren, die ihn berühren, und wandelt
  damit Brutstäbe um -- zehn Umwandlungen für je drei Stabgrößen (`BreederRecipes`).
- Das **Reaktorpult** samt Reaktorfühler: regelt einen Forschungsreaktor auf Temperatur, nach einer
  von drei Kennlinien zwischen zwei Eckpunkten. Ein Komparator daran liest die Temperatur.

**Abweichungen:** Der unsymmetrische Nachbarschaftsgraph des Originals ist unverändert übernommen
(er bestimmt das Gleichgewicht der Anlage). Ausgebessert sind dagegen zwei Stellen, an denen das
Original sich selbst widerspricht: die Fortschrittsrechnung des Brutreaktors teilte ganzzahlig, und
seine Auswurfprüfung verglich `ItemStack`s mit `containsValue`, was nie zutreffen kann.
**Nicht übernommen:** das Tscherenkow-Leuchten im Renderer (handgeschriebener Verlauf ohne Textur),
der Sonderfall um das Meteoritenschwert (Gegenstände fehlen im Port) und die
OpenComputers-Anbindung.

**Fusion, erster Teil (Runde 66).** Der Fusionstorus und das Klystron, dazu die beiden Netze, auf
denen sie miteinander reden: das Klystronnetz zum Torus hin, das Plasmanetz von ihm weg. Beide
rechnen selbst nichts -- wer liefert, schiebt jeden Tick selbst.

Mitgekommen sind die Kuehlbasis (`CooledBaseBlockEntity`, im Original aus dem
Teilchenbeschleuniger-Zweig), das Fusionsmodul und die elf Brennstoffrezepte, die den ganzen
Fortschrittsbaum der Fusion bilden -- von Deuterium-Deuterium, das ein Klystron zuendet, bis zum
Sternenfluss, der zwanzig braucht.

**Die Abnehmer (Runde 68).** Der Kollektor, der nichts tut, ausser vom Torus gezählt zu werden
— jeder beschleunigt dessen Ausbeute um die Hälfte. Der Koppler, der die Wärme eines Torus als
Zündenergie in den nächsten schiebt, sodass eine kleine Stufe die große zündet. Der Boiler, der
Wasser zu überhitztem Dampf kocht. Und die MHD-Turbine, die aus Plasmawärme unmittelbar Strom
macht — 135 Prozent Ausbeute, aber unter fünf Millionen Wärme je Tick nur die Hälfte davon.

Alle vier haben ihr Montagerezept; der Torus selbst kommt im Original aus der Plasmaschmiede und
kam deshalb erst mit Runde 70 an ein Rezept.

**Der Brutreaktor (Runde 69).** Der einzige Abnehmer, der nicht die Plasmawärme nimmt, sondern
den Neutronenfluss — und der wird nicht geteilt: jeder Brutreaktor bekommt ihn ganz. Dafür hängt
er an der Brennstoffwahl des Torus, denn nicht jedes Rezept gibt Fluss ab; Helium-3 fusioniert
aneutronisch und lässt ihn leer ausgehen. Er kann Gegenstände ausgasen — dieselbe Liste wie der
Ausgaser — und Fluide umwandeln; dafür kamen die drei `FluidBreederRecipes` mit.

**Nicht übernommen:** der Sonderfall, der ein bestrahltes Meteoritenschwert zum verschmolzenen
macht. Beide Gegenstände gibt es im Port nicht.

**Die Plasmaschmiede (Runde 70).** Der Ofen, der an der Plasmaleitung hängt und alles verarbeitet,
was nur bei Sternentemperaturen geht. Die Plasmaleistung wird nicht verbraucht, sondern geprüft:
erreicht sie die Zündtemperatur des Rezepts, läuft der Ofen; drei Viertel gibt er hinter sich
weiter, sodass mehrere Schmieden an einem Reaktor hängen können — jede nächste mit weniger. Ein
radioaktives Isotop im Boosterfach lässt ihn für eine begrenzte Zahl von Ticks viermal so schnell
laufen.

Mitgekommen sind 24 der 36 Rezepte, das Fusionsbauteil `fusion_component` samt seinen drei
Montagerezepten und der Schweißstufe am gesetzten Block, und eine JEI-Ansicht. **Damit schließen
sich zwei Lücken, die seit Runde 63 und 66 offenstanden:** die zwölf `plsm.icf*`-Rezepte machen die
Trägheitsfusion baubar, `plsm.fusionvessel` den Torus.

**Nicht übernommen** (die Erzeugnisse fehlen im Port): der Schrabidiumhammer, die Redd-Batterie
(braucht das Quanten-Batteriepaket), der Gerald-Satellit (Datenträger und UFO-Münze) und die fünf
Bauteile der Dark-Fusion-Chamber. Ebenso die Flamme des Brennerarms im Renderer — ein
handgeschriebener Farbverlauf ohne Textur, der an die alte Pipeline gebunden ist; der Strahl
zwischen Plasma und Werkstück steht dagegen.

**Offen:** nichts mehr in der Fusion.

**Trägheitsfusion abgeschlossen (Runden 62-63).** Der Laser (Steuerung, sechs Bauteile, Einlesen,
Strahl samt Strahlrenderer), die Brennkammer samt Zusammenbauklotz, Oberfläche und Renderer, das
Brennstoffkügelchen mit seinen dreizehn Stoffen — und in Runde 63 die Presse, die aus zwei Stoffen
ein Kügelchen formt, dazu `purex.icf`, das das abgebrannte Kügelchen wieder auftrennt.

Zwei Befunde dazu:

- **Alle ICF-Bauteile kommen im Original ausschließlich aus der Plasmaschmiede** (zwölf
  `plsm.icf*`-Rezepte, die Presse eingeschlossen). Die Plasmaschmiede gehört zum Fusionszweig; sie
  hier als Werkbankrezepte nachzubauen hieße, den Fortschrittsbaum des Originals zu verbiegen.
  **Erledigt in Runde 70:** mit der Schmiede kamen alle zwölf Rezepte, die ICF-Blöcke sind baubar.
- **Das Myon** braucht den Teilchenbeschleuniger, den die Roadmap bisher gar nicht führte. Die
  Presse verbraucht es, also stehen `particle_muon` und `particle_empty` jetzt im Port — vorerst
  nur im Kreativreiter, bis der Beschleuniger nachkommt. Er gehört nicht zu Stufe 3; er wird beim
  nächsten Kartieren von Stufe 4 mit aufgenommen.

**Die Wiederaufbereitung ist vollständig (Runde 63).** Mit dem ICF-Kügelchen waren auch die
Rezepte fällig, die frühere Runden liegen lassen mussten, weil ihre Eingänge fehlten: die vier
Chicago-Pile-Stäbe (Runde 60 brachte `pile_rod`) und die fünfzehn PWR-Sorten samt den beiden
Schrabidium-Rezepten (Runde 54 brachte `pwr_fuel_depleted`). `PUREXRecipes` führt jetzt 54 der 59
Rezepte des Originals; die fünf fehlenden hängen sämtlich an Dingen, die es im Port nicht gibt —
Naquadria-Klumpen aus einem fremden Mod, Bleisand für die drei Verglasungen und der Salzkreislauf
des Flüssigsalzreaktors.

**Chicago Pile MK2 abgeschlossen (Runde 60).** Graphitziegel, Quader, Kanalbohrung,
Neutronenrechnung, Kernschmelze, die neun Staebe und die drei Geräte samt Renderern und Rezepten.
Offen bleibt dreierlei: der Chicago Pile MK1 (`BlockGraphite*`, `TileEntityPile*`, alles
`@Deprecated`), die Ein- und Auslass-Texturen (sie liegen im Original nur als
Connected-Texture-Blätter vor, und die sind gestrichen). Eine eigene JEI-Ansicht hat der Pile
auch im Original nicht — seine Stäbe erscheinen in der PUREX-Ansicht (Runde 64).

**Watz abgeschlossen (Runden 58-59).** Der Reaktor, seine Bauteile, die Oberfläche, die
Renderer und alle Rezepte stehen. Nicht übernommen: die OpenComputers-Anbindung, die Radio-Werte
und die `SmallBlockPronter`-Bauvorschau (alle drei in `ENTSCHEIDUNGEN.md`), dazu `purex.watznaqadah`,
das Naquadria-Klumpen aus einem fremden Mod braucht. Die JEI-Ansicht des Watz steht seit
Runde 64.

## Stufe 4 — Waffen und Weltinhalt

Der Sedna-Kern ist portiert, die Inhalte fehlen. Vor Runde 31 lagen 3 der 24 `XFactory`-Klassen
und 4 der 70 Waffen vor.

### Runde 31 — die M3 und die 9-mm-Munition

- [x] `XFactory9mm` mit den vier Patronen `p9_sp`, `p9_fmj`, `p9_jhp`, `p9_ap`. Eigene
      Munitions-*Items* brauchte es nicht: `GunFactory.Ammo` führt `P9_SP` … `P9_AP` bereits,
      und alle Standardpatronen hängen an dem einen `EnumMultiItem` `AMMO_STANDARD`.
- [x] Die M3 (`gun_greasegun`) samt `GunConfig`, Vollmagazin-Nachladen (30 Schuss),
      Rückstoß-, Rauch- und Animationslambdas.
- [x] `Orchestras.ORCHESTRA_GREASEGUN` — Mündungsfeuerpaket, Hülsenauswurf und die
      Nachlade-, Ladehemmungs- und Inspektionsgeräusche.
- [x] Drei neue Geräusche (`weapon/reload/mag_remove`, `mag_insert`, `latch_open`) und der
      Schuss (`weapon/fire/greasegun`), jeweils in `NtmSoundEvents` und im
      `NtmSoundDefinitionsProvider`.
- [x] `ItemRenderGreasegun` als 1.21-Fassung des Originalrenderers, dazu Modell
      (`models/obj/weapons/greasegun.obj`), Textur und die Registrierung in `ResourceManager`.
- [ ] **Abweichung:** das Original wechselt über `setNameMutator` auf den Namen „M3" und über
      `isRefurbished` auf eine zweite Textur (`greasegun_clean`), sobald der Aufsatz
      `ID_GREASEGUN_CLEAN` steckt. Die Waffenaufsätze sind nicht portiert; beides kommt mit
      dem Aufsatzsystem zurück. `setupModTable` entfällt aus demselben Grund.
- [ ] **Vorbestehende Lücke, nicht in dieser Runde geschlossen:** keine der Waffen liegt in
      einem Kreativtab, und außer der M3 hat keine einen Anzeigenamen. Ein eigener Waffentab
      gehört zusammen mit den restlichen Waffen aufgesetzt.

### Runde 72 — Schwarzpulver, .357 und .45

- [x] `XFactory45` — fünf Patronen, keine eigene Waffe. Im Original verschießen sie die Thompson
      und die Liberator, die beide noch fehlen; die Patronen stehen trotzdem, weil sie über
      `GunFactory.Ammo` bereits Gegenstände sind und ohne diese Zeilen keine Werte hätten.
- [x] `XFactoryBlackPowder` — die vier Steinmunitionen und die **Pfefferbüchse**, die erste Waffe
      des Spiels. Sechs Läufe, von Hand weitergedreht, mit einem Schnelllader nachgeladen.
- [x] `XFactory357` — sechs Patronen und der **leichte Revolver** in zwei Ausführungen (gewöhnlich
      und Atlas). Beide teilen sich Modell und Animationen; nur Textur und Schaden unterscheiden
      sich, weshalb im Port eine gemeinsame `revolver(damage)`-Methode beides aufsetzt.
- [x] Zwei Orchester (`ORCHESTRA_PEPPERBOX`, `ORCHESTRA_ATLAS`), zwei Schussgeräusche und die
      beiden Renderer `ItemRenderPepperbox` und `ItemRenderAtlas`.
- [x] **Vorbestehende Lücke geschlossen:** es gibt jetzt einen Waffenreiter im Kreativinventar.
      Bis hierher lag keine der portierten Waffen in einem Reiter — sie waren nur über Befehle zu
      bekommen. Anzeigenamen haben jetzt alle fünf.
- [ ] **Nicht übernommen:** die dritte .357-Ausführung, die **DANI** — ein beidhändiges Paar mit
      zwei Empfängern, eigenem Orchester und eigenem Renderer. Sie gehört zu den beidhändigen
      Waffen (Uzi, Star-F, Mare's Leg) und kommt mit ihnen; einzeln portiert wäre ihr Renderer die
      einzige Stelle im Port, die zwei Waffen gleichzeitig zeichnet.
- [ ] **Weiterhin offen aus Runde 31:** `setNameMutator`/`isRefurbished` und `setupModTable`
      hängen am Aufsatzsystem und kommen mit ihm.

### Runde 73 — .22 LR: AM180 und Star-F

- [x] `XFactory22lr` — vier Patronen und zwei Waffen. Die **AM180** ist ein Trommelmagazingewehr
      mit 177 Schuss und einem Schuss je Tick, die schnellste Waffe des Originals; jede Kugel macht
      nur 2 Schaden, und ihr Rückstoß ist reines Rauschen in beide Richtungen. Ihre Patronen stoßen
      nicht zurück, sonst schöbe ein Dauerfeuer das Ziel aus der Welt. Die **Star-F** ist die
      zugehörige Pistole: 15 Schuss, halbautomatisch, deutlich mehr Schaden je Treffer.
- [x] Die Animationsdatei `am180.json` über den schon vorhandenen `AnimationLoader` — derselbe Weg,
      den die SPAS-12 seit ihrer Runde geht. In der Trommel dreht sich das Magazin mit dem
      Füllstand: bei 59 verbleibenden Patronen genau einmal ganz herum.
- [x] Zwei Orchester, zwei Renderer, das Schussgeräusch der Star-F und das Aufprallgeräusch für
      das zu Boden fallende Trommelmagazin.
- [ ] **Nicht übernommen:** die dritte Waffe, die beidhändige **Star-F-Akimbo** — sie gehört zu den
      beidhändigen Waffen und kommt mit ihnen, zusammen mit der DANI aus Runde 72. Ebenso alle
      Schalldämpferzweige (`setNameMutator`, `hasSilencer` in beiden Renderern): sie hängen am
      Aufsatzsystem.
- [ ] **Abweichung:** das Original hält für die AM180 zwei Animationssätze bereit, umgeschaltet
      über `ClientConfig.GUN_ANIMS_LEGACY`. Den Schalter gibt es im Port nicht; übernommen ist der
      neue Satz.

### Runde 74 — das Aufsatzsystem und der Waffentisch

Der Kern lag schon im Port: `XWeaponModManager.eval` ist seit dem Sedna-Gerüst in **jede**
Wertabfrage von `GunConfig` und `Receiver` eingehängt. Gefehlt haben die Aufsätze selbst, ihre
Gegenstände, der Tisch — und damit jede Möglichkeit, einen Aufsatz anzubauen.

- [x] `WeaponModBase` samt Prioritäten. Die Reihenfolge entscheidet, wie Aufsätze sich stapeln:
      was multipliziert, wird vor dem ausgewertet, was addiert.
- [x] Vier Aufsätze: `WeaponModGenericDamage` (+15 %), `WeaponModGenericDurability` (×2),
      `WeaponModSilencer` (tauscht das Schussgeräusch) und `WeaponModGreasegun` (dreifache
      Haltbarkeit, +2 Schaden, keine Streuung, halbe Wartezeit — dazu ein eigenes Orchester).
- [x] Drei Aufsatz-Gegenstände (`weapon_mod_generic`, `_special`, `_caliber`) mit allen 55 Texturen.
- [x] `XWeaponModManager.init()`, `install`, `uninstall`, `isApplicable`, `getUpgradeItems`,
      `hasUpgrade` — und die Rettung des Magazinzustands über den Umbau hinweg.
- [x] Der **Waffentisch**: Block, Menü und Oberfläche. Wer einen Aufsatz hineinlegt, baut ihn sofort
      an; beim Schließen fällt alles heraus, wie im Original. Gerade deshalb ist er sicher — es gibt
      keinen Zustand, in dem Aufsätze verlorengehen.
- [x] **Drei aufgeschobene Zweige sind zurück:** der Namenswechsel der M3 auf „M3", sobald der
      aufgearbeitete Schaft steckt (offen seit Runde 31), und der Namenswechsel von AM180 und Star-F
      bei gestecktem Schalldämpfer (offen seit Runde 73).
- [ ] **Abweichung:** das Original trägt hier rund vierzig Waffen ein. Im Port stehen sieben; die
      übrigen Einträge kommen mit ihren Waffen. Die Nummern der Aufsätze sind trotzdem dieselben wie
      im Original — sie stehen in gespeicherten Waffen und dürfen sich nie verschieben.
- [ ] **Nicht übernommen:** die Testaufsätze (`weapon_mod_test`) — Werkzeug des Autors, kein
      Spielinhalt. ~~Der Umschalter des Tisches zwischen zwei Empfängern entfällt, solange es keine
      beidhändige Waffe im Port gibt.~~ — nachgeholt in Runde 75. `setupModTable` in den Renderern
      bleibt: es stellt die Waffe im Tisch dar, wofür der Port keine Entsprechung hat.

### Runde 75 — die beidhändigen Waffen

Vier Waffen, die alle dasselbe können, was im Port bisher niemand konnte: zwei Empfänger in einem
Gegenstand, jeder mit eigenem Magazin, eigenem Ladezustand und eigenem Aufsatzsatz. Der linke hängt
an der linken Maustaste, der rechte an der rechten. Das Gerüst dafür trug der Port längst —
`GunBaseNTItem` nimmt seit jeher beliebig viele `GunConfig` entgegen und tickt jede einzeln.
Gefehlt haben die Waffen und die Renderer, die zwei Waffen gleichzeitig zeichnen.

- [x] Die **Uzi** und die **beidhändige Uzi** (`XFactory9mm`) samt Modell, zwei Texturen und dem
      Schussgeräusch. Der zweite Empfänger braucht einen eigenen Entscheider: der Standardentscheider
      lädt beim Dauerfeuer nur nach, wenn die *linke* Maustaste hält.
- [x] Die **beidhändige Mare's Leg** und die **zerschossene Mare's Leg** (`XFactory12ga`), dazu der
      kurze Animationssatz `LAMBDA_MARESLEG_SHORT_ANIMS` und die Geheimpatrone `g12_equestrian_tkr`.
- [x] Die **beidhändige Star-F** (`XFactory22lr`) in der Elite-Ausführung.
- [x] Die **DANI** (`XFactory357`) — zwei Revolver, die sich nicht einmal die Textur teilen: einer
      trägt die Sonne, einer den Mond, und sie schießen einen Zehntel über und unter dem Ton.
- [x] Sechs Orchester und fünf Renderer. In den beidhändigen Renderern sitzt die Schleife über die
      zwei Empfänger; jede Hälfte zieht ihre eigenen Animationen über den Empfängerindex.
- [x] **Der Umschalter des Waffentisches** — der aufgeschobene Punkt aus Runde 74. In 1.21 läuft er
      über `clickMenuButton` statt über den Sondermodus 999 999 des Originals. Beim Umschalten wird
      angebaut, was liegt, und herausgelegt, was am anderen Empfänger steckt.
- [x] **Drei aufgeschobene Zweige sind zurück:** der Schalldämpferzweig der AM180 und der Star-F in
      den Renderern (offen seit Runde 73 und 31) und der Sägeaufsatz, mit dem `getShort` der
      Mare's Leg endlich etwas entscheidet.
- [x] Zwei Aufsätze mehr: `WeaponModSawedOff` und `WeaponModUziSaturnite`; vier Werkstoffgruppen
      mehr im Aufsatzkatalog (Desh, Waffenstahl) samt Rezepten.
- [x] **Vorbestehende Lücke geschlossen:** Mare's Leg und SPAS-12 lagen seit Runde 72 in keinem
      Kreativreiter und hatten keinen Anzeigenamen.
- [ ] **Abweichung:** in dritter Person zeichnet der Port nur die Waffe in der Haupthand. Die zweite
      hängt im Original an `RenderPlayerEvent.Specials` — einer Ereignisreihe, die es in 1.21 nicht
      mehr gibt; der Port hat die zugehörige Schicht (Arme ausblenden, zweite Waffe an den linken
      Arm hängen) noch gar nicht. Das gilt für alle vier Waffen gleichermaßen.

### Runde 76 — die Geschütztürme: Grundlage und Wachturm

Ein ganzes Subsystem, von dem im Port bisher **keine einzige Zeile** stand. Die Grundlage ist im
Original mit 1.107 Zeilen die größte einzelne Klasse des Waffenzweigs; sie sucht Ziele, dreht sich
darauf und feuert, sobald sie ausgerichtet ist. Was dabei geschieht, sagt die einzelne Bauart.

- [x] `TurretBaseBlockEntity` — Zielsuche, Sichtlinie, Schwenkbereich, Drehung in Bogenmaß samt der
      Sonderrechnung um die 360-Grad-Grenze, Munitionsfächer, Stromverbrauch, Hülsenauswurf und die
      Strichliste der erledigten Ziele.
- [x] Der **Wachturm** und der **zerschossene Wachturm** — der eine braucht Strom und lässt sich
      ein- und ausschalten, der andere läuft immer, dreht langsamer, schießt auf alles was lebt und
      hat einen verbogenen zweiten Lauf, der nur noch klickt.
- [x] Der **Zielchip**: wer ihn anklickt, trägt sich ein; ein Turm mit diesem Chip lässt alle
      Eingetragenen in Ruhe. Die Liste steht in einer Datenkomponente statt in losem NBT.
- [x] Oberfläche samt Namensliste, den vier Zielschaltern, der Stromsäule und der Strichliste;
      dazu der Renderer mit den zwei zurückfahrenden Läufen.
- [ ] **Abweichung:** die Zielklassen des Originals, die es im Port noch nicht gibt — Raketen,
      Bomber, Eisenbahn — fehlen im Maschinenzweig; Loren und alles, was sich dem Radar zeigt, sind
      da. Ebenso die Listen aus `CompatExternal`, über die fremde Mods eigene Ziele eintragen.
- [ ] **Ausgebessert:** das Original reicht die Winkel des Turms im Bogenmaß an den Hülsenwerfer
      weiter, der mit Grad rechnet — dort fliegen die Hülsen in eine beliebige Richtung.
- [ ] **Nicht übernommen:** die OpenComputers- und RedstoneOverRadio-Anbindung
      (`ENTSCHEIDUNGEN.md`).

### Runde 77 — die großen Geschütztürme: Kanone und Nahbereich

Der Wachturm aus Runde 76 steht auf einem einzigen Block. Alle übrigen Türme stehen auf einem
2x2-Fuß — und das ist mehr als ein Größenunterschied: ihr Drehpunkt sitzt nicht in der Blockmitte,
und ihren Strom ziehen sie an acht Punkten ringsum statt von den sechs Nachbarn.

- [x] `TurretDummyableBlockEntity` und `TurretBaseNTBlock` — die Grundlage der großen Türme. Wo
      der Drehpunkt sitzt, rechnet sich aus den Maßen des Mehrfachblocks und seiner Richtung aus,
      statt wie im Original aus einer Tabelle nach Metadatum.
- [x] `XFactoryTurret` — die Munition, die es nur in Türmen gibt: fünf 240-mm-Granaten und die
      20-mm-Patrone des Nahbereichsgeschützes, samt Gegenständen, Texturen und Rezepten.
- [x] Der **Kanonenturm**: eine 240-mm-Kanone, achtzig Blöcke weit, alle zwei Sekunden ein Schuss.
      Unter sechzehn Blöcken sieht er nichts — so nah kann er den Lauf nicht mehr senken.
- [x] Das **Nahbereichsgeschütz** und sein verrostetes Gegenstück: zwei gegenläufige Gatlings, die
      schnell genug drehen, um Raketen im Flug zu treffen. Es verschießt keine Geschosse, sondern
      trifft unmittelbar mit fester Wahrscheinlichkeit — bei dieser Kadenz wären Geschosse nicht zu
      berechnen.
- [x] Die **Anschlussstutzen** im Renderer: an jeder der acht Stellen, an denen der Turm Strom
      zieht, wird einer gezeichnet — aber nur, wo auch wirklich eine Leitung liegt.
- [x] Alle Bauarten teilen sich ein Menü; welches Bild darunter liegt, sagt der Turm selbst.
- [ ] **Nicht übernommen:** die Reichweitenerhöhung der Atomgranate (`withRangeMod`) und der
      `PlayerProcessorStandard`, die beide im Port keine Entsprechung haben. Die
      Flüssigkeitsstutzen im Renderer kommen mit den Türmen, die Treibstoff verbrennen.
- [x] **Ausgebessert aus Runde 76:** `EnderDragonPart` liegt in 1.21 in `entity.boss`, nicht in
      `entity.boss.enderdragon` wie der Drache selbst. Die CI hat es gefangen; **kein Gate des
      Ports kann das**, weil offline kein Minecraft-Klassenpfad zur Verfügung steht. Eine
      Heuristik über seltene Paketnamen wurde geprüft und verworfen: sie hätte auf über vierzig
      bestehende, richtige Importe angeschlagen und diesen einen gerade nicht gefangen, weil das
      Paket sonst durchaus benutzt wird.

### Runde 78 — .50 BMG und 5,56 mm: die beiden Gatling-Türme

Die Munition beider Kaliber stand im Port schon als Gegenstand da — die Einträge im `Ammo`-Enum
sind seit dem Sedna-Gerüst vorhanden —, aber ohne Werte: kein Schaden, keine Durchschlagskraft,
keine Hülse. Damit waren sie unverschießbar.

- [x] `XFactory50` — acht .50-BMG-Patronen, die schwerste Gewehrmunition des Spiels. Schon die
      gewöhnliche durchschlägt siebeneinhalb Punkte Panzerung, die Wolframkerne fast das Dreifache.
- [x] `XFactory556mm` — vier 5,56-mm-Patronen, jede zusätzlich als Brandsatz. Die Brandsätze sind
      keine eigenen Gegenstände, sondern entstehen aus denselben Patronen.
- [x] Die **Gatling** und der **Begleitturm**: derselbe Turm in zwei Kalibern. Beide laufen erst
      eine Sekunde hoch, ehe sie schießen — wer nur kurz ins Blickfeld läuft, wird nicht
      beschossen. Im Bild beschleunigt der Laufkranz entsprechend und läuft danach aus.
- [ ] **Nicht übernommen:** die Equestrian-Patrone der .50, die beim Aufschlag ein ganzes Gebäude
      vom Himmel fallen lässt — die zugehörige Entität fehlt im Port.
- [x] **Nachgezogen:** die Renderbox der Türme ist jetzt ein Kasten statt der unendlichen Box des
      Originals. Die unendliche Box lässt den Renderer bei jedem Bild laufen, auch wenn der Turm
      längst hinter dem Spieler liegt.

### Runde 79 — die Zieloptik und das Antimateriegewehr

`GunConfig` trägt seit dem Sedna-Gerüst ein `scopeTexture` — aber niemand hat es je gelesen. Die
Zieloptik ist der Verbraucher, der aus diesem toten Feld etwas macht.

- [x] `RenderScreenOverlay.renderScope` — das Bild der Optik über dem ganzen Schirm. Die Optik ist
      quadratisch, der Schirm ist es nicht: das Bild wird auf die kürzere Seite gepasst und auf der
      längeren über den Rand gezogen, statt verzerrt zu werden. So bleibt der Kreis rund.
- [x] Eingehängt im Fadenkreuz-Zweig von `GunBaseNTItem`: ganz angelegt tritt die Optik an die
      Stelle des Fadenkreuzes, dazwischen bleibt es stehen.
- [x] Das **Antimateriegewehr** in drei Ausführungen — die gewöhnliche, die *Subtlety* mit zwei
      Dritteln mehr Schaden und dreifacher Haltbarkeit, und die *Penance*, die von Haus aus einen
      Schalldämpfer trägt, keine Hüftstreuung hat, eine Wärmebildoptik führt und als einzige Waffe
      des Spiels die schwarze Patrone frisst.
- [x] Der Renderer: Verschluss, Magazin, Zweibein und das Zielfernrohr, das der Schütze beim
      Betrachten in die Luft wirft und wieder auffängt. Das Zweibein steht ausgeklappt, sobald die
      laufende Animation keinen `BIPOD`-Kanal hat — so klappt es beim Ziehen aus und bleibt stehen,
      ohne dass irgendwo ein Zustand dafür mitgeführt werden müsste.
- [x] ~~**Offen:** die M2 desselben Kalibers braucht das Gurtmagazin (`MagazineBelt`)~~ — nachgeholt
      in Runde 80.

### Runde 80 — das Gurtmagazin und die M2

- [x] `MagazineBelt` — ein Magazin, das keines ist: die Waffe frisst unmittelbar aus dem Rucksack,
      und nachgeladen wird nie. Welche Patronenart gerade läuft, entscheidet sich Schuss für
      Schuss neu; wer wechseln will, wirft die alte weg.
- [x] Die **M2 Browning** aus der Hand: dreitausend Schuss Haltbarkeit, alle zwei Ticks einer,
      kein Nachladen. Ihr Schussgeräusch ist dasselbe wie das der Gatling — es ist dieselbe
      Patrone im selben Kaliber.
- [ ] **Nicht übernommen:** die Munitionstasche des Originals, an die das Gurtmagazin ebenfalls
      geht. Sie fehlt im Port, wie schon bei den übrigen Magazinen.
- [x] **Ausgebessert aus Runde 79:** die Abbildung der Zieloptik nahm die *längere* statt der
      *kürzeren* Bildschirmseite als Bezug — der Zielkreis wäre auf breiten Fenstern oben und unten
      beschnitten worden. Er füllt jetzt die kürzere Seite aus, und was daneben übrigbleibt, wird
      schwarz zugemalt, statt das Bild über den Rand zu ziehen.

### Runde 81 — die drei Waffen der 5,56 mm

- [x] Die **G3** in zwei Ausführungen. Die gewöhnliche ist das Arbeitspferd des Kalibers und die
      wandelbarste Waffe des Ports: Schalldämpfer, Zielfernrohr, abgesägter Schaft und zwei
      Kunststoffschäfte lassen sich frei zusammenstellen, und jede Kombination zeichnet sich
      anders. Zwei Zusammenstellungen tragen eigene Namen — *Infiltrator* und *G3A3*. Die
      **Zebra** bringt Dämpfer und Optik von Haus aus mit und lädt ausschließlich Brandmunition.
- [x] Die **StG 77** — die erste Waffe des Ports, deren Nachladen, Durchladen und Betrachten
      nicht von Hand geschrieben, sondern aus einer Animationsdatei geladen sind. Beim Betrachten
      zerlegt der Schütze die Waffe halb.
- [x] Drei neue Waffenaufsätze: das **Zielfernrohr** (Bild statt Fadenkreuz), der **abgesägte
      Schaft** der G3 (doppelt so schnelles Ziehen) und der **Kunststoffschaft** in Grün und
      Schwarz (halber Rückstoß, andere Textur).
- [x] `Lego.LAMBDA_STANDARD_CLICK_SECONDARY` — der **Feuerwahlschalter**. Er fehlte im Port, weil
      bisher keine Waffe ihn brauchte; der Standardentscheider fragt ihn aber längst ab, und ohne
      ihn lässt sich Dauerfeuer nirgends abschalten.
- [ ] **Abweichung:** die StG 77 zieht im Original beim Anlegen zusätzlich das Blickfeld der
      *Handdarstellung* von 70 auf 5 Grad zusammen (`getBaseFOV`) — das ist die eigentliche
      Zoomwirkung ihres Rohrs. Der Port hat dafür keinen Haken; es bleibt beim Weltblickfeld.
- [ ] **Abweichung:** der Feuerwahlschalter des Originals liest den alten Modus immer aus
      Konfiguration 0, schreibt ihn aber in die aktuelle zurück. Bei einer beidhändigen Waffe legt
      der rechte Schalter damit um, was am linken steht. Hier wird durchgängig die aktuelle
      Konfiguration gelesen.
- [x] **Ausgebessert aus den Runden 75, 79 und 80:** zehn Waffen hatten einen Renderer, aber
      keinen Eintrag im `NtmItemModelProvider` — ihnen fehlte das Gegenstandsmodell `builtin/entity`,
      auf dem der Renderer aufsetzt. Alle nachgetragen.

### Runde 82 — 7,62 mm: Karabiner und MAS-36

- [x] Die sechs Patronen des Kalibers. Sie stehen zwischen der 5,56 und der .50: mehr Durchschlag
      als die eine, handlicher als die andere. Die Sprengpatrone bekommt die **kleine
      Sprengwirkung** (`Lego.tinyExplode` mit `ExplosionEffectTiny`), die dem Port bisher fehlte.
- [x] Der **Karabiner** — vierzehn Schuss im Röhrenmagazin, einzeln nachgeladen. Er ist die erste
      Waffe des Ports, die `RELOAD_END` benutzt: nach dem letzten Einzelschuss schiebt eine eigene
      Bewegung den Verschluss wieder vor.
- [x] Die **MAS-36** — sieben Schuss, dreißig Schaden, von Hand durchgeladen, mit Ladestreifen.
- [x] Das **Bajonett** als Aufsatz: es macht aus der Betrachtungsbewegung einen Stoß, der auf drei
      Block Reichweite fünfzehn Schaden macht. Das Original hat dafür zwei Klassen, die sich in
      nichts als dem durchgereichten Animationssatz unterscheiden; hier ist es eine, die beides
      im Konstruktor bekommt. Die Nummern bleiben getrennt.
- [ ] **Nicht übernommen:** die drei Miniguns des Kalibers. *(Richtiggestellt in Runde 83: die
      Begründung stimmte nicht — die Miniguns brauchen keine Schleifgeräusche, ihr Orchester
      spielt nur Einzeltöne. Zwei von dreien sind in Runde 83 nachgereicht.)*
- [ ] **Abweichung:** das Original schneidet die Patronen des MAS-36-Ladestreifens beim
      Herunterdrücken mit einer Schnittebene (`glClipPlane`) ab. Schnittebenen gibt es im festen
      Renderweg von 1.21 nicht mehr; die Patronen bleiben ganz, der Tiefenpuffer verdeckt sie über
      den größten Teil der Bewegung ohnehin.

### Runde 83 — die beiden Miniguns

- [x] Die **Minigun**: fünfzigtausend Schuss Haltbarkeit, einer je Tick, aus dem Gurt. Der
      Laufkranz bekommt mit jedem Schuss sechzig Grad mit und läuft danach über zwei volle
      Umdrehungen aus — bei Dauerfeuer dreht er deshalb durchgehend.
- [x] Die **Doppel-Minigun**: zwei vollständige Konfigurationen, jede Hälfte auf ihrer Maustaste.
      Sie steht wie im Original auf `WeaponQuality.DEBUG` — Werkzeug des Autors, nicht zu bauen.
- [x] **Drossel** und **Schnellauf** als Aufsätze: die eine halbiert die Schussfolge und nimmt
      dafür die ganze Streuung, der andere verdreifacht die Geschosse je Schuss und nimmt die
      halbe Treffgenauigkeit. Beide belegen denselben Platz und schließen einander aus.
- [x] **Richtiggestellt aus Runde 82:** dort stand als Begründung fürs Auslassen, die Miniguns
      brauchten „ein Orchester mit Schleifgeräuschen". Das war falsch und ungeprüft — ihr
      Orchester spielt nur Einzeltöne, und das einzige neue davon ist das Schussgeräusch. Die
      eigentliche Hürde lag nur beim Lacunae-Lasergatling.
- [ ] **Nicht übernommen:** das **Lacunae-Lasergatling**. Es verschießt Kondensatoren aus
      `XFactoryEnergy`, die der Port noch nicht hat; sein Renderer braucht außerdem
      `renderLaserFlash`, das der Port ebenfalls noch nicht hat.
- [ ] **Abweichung:** in dritter Person zeichnet der Port von der Doppel-Minigun nur die Waffe in
      der Haupthand — dieselbe Lücke wie bei allen beidhändigen Waffen seit Runde 75.

### Runde 84 — 10 gauge: Doppelflinte und Heiliger Drache

- [x] Die fünf Schrotpatronen des schwersten Schrotkalibers. Die **Splitterschrote** prallt bis zu
      fünfzehnmal und in fast jedem Winkel ab — sie ist dafür gedacht, in einen Gang geschossen zu
      werden und dort eine Weile zu bleiben.
- [x] Die **Doppelflinte**: zwei Läufe auf zwei Maustasten, links einer, rechts der andere, beide
      zugleich beide Schuss. Sie nimmt die Säge, die seit Runde 75 an der Mare's Leg hängt.
- [x] Der **Heilige Drache**: halb so viel Schaden mehr, sechsfache Haltbarkeit, ein Drittel mehr
      Streuung — und von Haus aus abgesägt.
- [x] **Aufgeräumt:** Modell (`sacred_dragon.obj`) und Drachen-Textur lagen seit dem
      Sedna-Gerüst ungenutzt im Port. Sie haben jetzt ihren Verbraucher.
- [ ] **Nicht übernommen:** die Ketzer-Selbstladeflinte desselben Kalibers. Sie borgt sich
      Animationen, Rückstoß und Orchester von der Schredder-Flinte der 12 Gauge, die noch fehlt.
- [ ] **Nicht übernommen:** der Würger (`WeaponModChoke`), der im Original auch an die
      Doppelflinte geht — er hängt an Waffen, die noch fehlen.

### Runde 85 — der Bolter

- [x] Die drei **75-mm-Bolzen**. Sie sind keine Patronen im üblichen Sinn: jeder Bolzen ist eine
      kleine Rakete mit eigenem Antrieb, und jeder geht beim Aufschlag hoch — der gewöhnliche
      zwei Blöcke weit, der Sprengbolzen fünf, der Brandbolzen setzt das Ziel in Phosphorbrand.
- [x] Der **Bolter**: dreißig Bolzen im Dauerfeuer, Rückstoß in jede Richtung. Er ist die erste
      Waffe des Ports mit einem **Zählwerk** — die Zahl der verbleibenden Bolzen steht in roten
      Ziffern auf dem Gehäuse, in voller Helligkeit, damit sie auch im Dunkeln lesbar bleibt.

### Runde 86 — der Aberrator und das stehende Feuer

- [x] Die **.35-800** ist die erste **Strahlmunition** des Ports. Sie fliegt nicht, sie ist sofort
      da; sie durchschlägt fünfzig Punkte Panzerung und halbiert, was davon übrigbleibt. Dafür
      bekommt `BulletConfig` den Standard-Strahlaufschlag, der bisher fehlte — `BulletBeamBase`
      stand seit dem Sedna-Gerüst ohne Verbraucher da.
- [x] **`FireLingering`** — ein Feuer, das steht statt zu brennen: eine Entität mit eigenem
      Kasten, in vier Spielarten (gewöhnlich, Phosphor, Bannfeuer, schwarzes Feuer). Sie sucht
      sich mit einem Strahl nach unten den Boden, damit ihre Flammen aufsitzen statt in der Luft
      zu hängen, und wird nicht gespeichert.
- [x] Der **Aberrator**: fünf Schuss, hundert Schaden je Stück, und er nutzt sich nicht ab. Um
      seine Mündung stehen **sechzehn goldene Schwerter** im Kreis und drehen sich unablässig;
      beim Anlegen klappen sie nach vorn und geben den Blick frei.
- [x] Die **EOTT**-Ausführung: derselbe Aberrator zweimal, jede Hälfte auf ihrer Maustaste, mit
      getrennten Magazinen.
- [ ] **Abweichung:** angelegt wirft auch die linke Hälfte der EOTT nach rechts aus. Das ist im
      Original so und sieht nach einem Versehen aus — die Klammersetzung dort lässt die Seite nur
      im Hüftschuss durchschlagen. Übernommen, weil es das Verhalten des Originals ist.
- [ ] **Nicht übernommen:** das Stapelmagazin (`WeaponModStackMag`), das im Original auch an den
      Aberrator geht.

### Runde 87 — 40 mm: Leuchtpistole und Granatwerfer

- [x] Die **fünf 40-mm-Granaten**. Die Sprenggranate reißt fünf Blöcke weit; die **Hohlladung**
      nur dreieinhalb, setzt dem getroffenen Wesen aber das Dreifache obendrauf, weil ihr Strahl
      nur dort wirkt, wo er auftrifft; die **Abbruchgranate** ist als einzige der fünf, die
      wirklich Blöcke herausreißt; **Brand- und Phosphorgranate** stellen ein stehendes Feuer aus
      Runde 86 hin und zünden alles Brennbare ringsum an.
- [x] Die **Leuchtpistole** — ein Einzellader mit hundert Schuss Haltbarkeit, zum Leuchten da,
      nicht zum Kämpfen.
- [x] Der **Congo Lake**: vier Granaten im Röhrenmagazin, einzeln nachgeladen, und beim Nachladen
      lässt sich die Granatenart wechseln. Er ist die zweite Waffe des Ports mit Animationen aus
      einer Datei — und die erste, deren geladene Granate in der Farbe ihrer Munition gezeichnet
      wird, damit der Schütze sieht, was im Rohr steckt.
- [ ] **Nicht übernommen:** die beiden Signalkugeln (Nachschub und Bewaffnung). Sie rufen eine
      C-130 herbei, die den Nachschub abwirft — das Flugzeug fehlt im Port, und eine Signalkugel
      ohne Flugzeug wäre eine gewöhnliche Leuchtkugel mit irreführendem Namen.
- [ ] **Abweichung:** die Abbruchgranate setzt im Original einen `PlayerProcessorStandard`, der
      dem Spieler eigene Regeln für Schaden und Rückstoß gibt. Der fehlt im Port.

### Runde 88 — die MK 108

- [x] Dreißig Granaten im Gurt, alle zehn Ticks eine. Der Lauf selbst fährt zurück, nicht die
      ganze Waffe.
- [x] **Der Gurt ist das aufwendigste Stück Darstellung im ganzen Port.** Er besteht nicht aus
      einem Modellteil, sondern aus neun Gliedern, deren Lage der Renderer bei jedem Bild neu
      ausrechnet: ein Vektor der Gliedlänge wandert von Glied zu Glied und wird dabei jedes Mal um
      den Winkel des nächsten gedreht, sodass der Gurt seiner eigenen Krümmung folgt. Zwei
      Winkelreihen stehen fest — eine für den eingelegten Gurt, eine für den herausgezogenen — und
      zwischen beiden wird nach dem Nachladefortschritt geblendet. Welche Glieder eine Granate
      tragen, entscheidet der Magazinstand: der Gurt läuft von hinten nach vorn leer.
- [x] **Beim Betrachten** wirft der Schütze drei Granaten nacheinander in die Luft und fängt sie
      wieder auf — jede an eigenen Kanälen, im gleichen Takt versetzt.

### Offen

- [ ] 11 der 24 `XFactory`-Munitionsfabriken, 37 der 70 Waffen
- [ ] 6 der 13 Geschütztürme — es fehlen Richard, Tauon, Fritz, Maxwell, Arty und HIMARS; alle
      hängen an Munitionsfabriken oder Entitäten, die noch fehlen
- [ ] 20 der 33 Waffenmodifikationen — die übrigen hängen an Waffen, die noch fehlen
- [ ] Entities: 65 der 122 Typen fehlen, darunter 56 der 58 Kreaturen
- [ ] Der **Teilchenbeschleuniger** (`XFactoryAccelerator`, `XFactoryPA`) — beim ICF in Runde 63
      als blinder Fleck aufgefallen: die ICF-Presse verbraucht sein Myon
- [x] ~~Weltgenerierung, Strukturen, Strahlungssystem — noch nicht analysiert~~ — nachgeholt.
      Die Weltgenerierung hat als eigene **Stufe 5** einen Platz bekommen; das Strahlungssystem
      ist mit 100 % Gerüstabdeckung das am besten portierte Subsystem überhaupt.

## Stufe 5 — Weltgenerierung

### Runde 89 — der erste Blockstapel

Nach der eigenen Empfehlung angefangen: nicht bei der Weltgenerierung, sondern bei den Blöcken.

- [x] **Die Panzerfamilie** — Panzerstein, -ziegel, -sandstein, -licht, -lampe, -glas und
      Glasscheibe. Das Baumaterial der Bunker und Silos: sehr hart, sehr sprengfest, ohne jedes
      Verhalten. Nur die Lampe hat Regung.
- [x] **Verbundziegel** (`brick_compound`).
- [x] **Sieben Stufenblöcke** — genau die Werkstoffe, die in den 79 Bauwerken wirklich vorkommen.
- [x] **Acht Treppenblöcke** — sie leihen sich die Textur ihres Grundblocks, wie im Original.
- [ ] **Abweichung:** das Original hat für die Lampe zwei Blöcke (`reinforced_lamp_off` und
      `_on`) und tauscht sie beim Schalten aus. In 1.21 ist das ein Block mit der Eigenschaft
      `LIT`; der Umsetzer bildet den einen Namen auf `lit=false` ab, den anderen auf `lit=true`.

Damit sind 18 der 115 fehlenden Namen abgedeckt (siehe die Berichtigung bei Runde 92).

### Runde 90 — die Rohrfamilie

Vier Formen in je sechs Farben: das nackte Rohr, das Rohr mit Bund, das Vierlingsrohr und das
eingehauste Rohr, jeweils in Stahl, Rost, Grün, verrostetem Grün, Rot und Gasmarkierung.

- [x] **24 Zierrohre** als Säulenblöcke — beim Setzen richten sie sich nach der angeklickten
      Fläche aus, wie im Original.
- [x] **Das Achteck aus Modellkästen.** Das Original zeichnet die Rohre mit einem eigenen
      Renderer aus OBJ-Modellen; auf 1.21 gibt es dafür keine Entsprechung. Die achteckige Form
      entsteht aus vier Bändern — zwei achsenparallelen und denselben beiden um 45 Grad
      gedreht. Ihre Vereinigung ist exakt das Achteck: die vier Ecken jedes Bandes liegen auf
      vier der acht Achteckspitzen, seine Längsseiten auf zwei der acht Achteckflächen.
- [x] **Das Gehäuse** — vier Eckpfosten über die volle Länge und vier Gitterwände ohne Dicke
      dazwischen, genau nach den Maßen des Originalmodells.
- [ ] **Abweichung:** das Original nennt den Block `deco_pipe_rusted`, seine Textur aber
      `pipe_side_rusty`. Die Blocknamen sind übernommen, weil die Bauwerke sie so nennen.

Acht der vierundzwanzig kommen in keinem der 79 Bauwerke vor — sie sind trotzdem dabei, weil
eine halbe Familie seltsamer wäre als eine ganze. Für die Bauwerke zählen **16**.

### Runde 91 — Meteoritenbau, Laborfliesen und Leuchtstein

- [x] **Der Meteoritenbau** — poliertes Meteoritgestein, Ziegel, gemeißelte Ziegel, Säule und
      der Statikgenerator. Das Baumaterial der Sternenmetall-Ruinen: sehr hart, sehr sprengfest.
- [x] **Drei Laborfliesen** — ganz, rissig, zerbrochen. Sie gasen Asbest aus wie der
      Asbestziegel; nur die zerbrochene tut das auch von selbst, wenn man über sie läuft.
- [x] **Der Leuchtstein in fünf Spielarten** plus die Ziegeltreppe. Im Original ist das *ein*
      Block, dessen Metadaten-Zahl die Spielart wählt (`BlockEnumMulti`); in 1.21 wird daraus je
      Spielart ein eigener Block. Alle fünf kommen in den 79 Bauwerken vor, also stehen auch
      alle fünf. Er leuchtet trotz seines Namens nicht — das Original setzt keine Lichtstärke.
- [ ] **Abweichung:** die Texturnamen des Originals enthalten Punkte (`lightstone.bricks.top`),
      die 1.21 in Ressourcenpfaden nicht erlaubt. Sie sind auf Unterstriche umgeschrieben.

Zehn Namen aus der Fehlliste, vierzehn Blöcke im Port.

### Runde 92 — der Rest der Betonfamilie

- [x] **Farbbeton in allen sechzehn Farben.** Im Original *ein* Block mit Metadaten-Zahl; in
      1.21 je Farbe ein eigener. Die Bauwerke benutzen elf der sechzehn — angelegt sind alle.
- [x] **Sonderbeton in acht Tönen** (`concrete_colored_ext`) — die zweite Farbreihe des
      Originals, Töne, die es im Farbeimer nicht gibt. Zwei ihrer Namen, Purpur und Rosa, gibt
      es in der ersten Reihe schon; deshalb tragen alle acht das Kürzel `ext`.
- [x] **Betonsäule** und **Stahlbeton**.
- [x] **Der Hochleistungsbeton**, der von selbst zerfällt — der erste Block dieser Stufe mit
      echtem Verhalten. Sechzehn Zerfallsgrade, fünf Bilder. Der Zerfall wird langsamer, je
      weiter er ist: bei jedem Zufallstakt geschieht nur mit 1/(Grad+1) überhaupt etwas. Am Ende
      bricht er zusammen — steht er auf festem Grund, wirft er seinen Bruch zur Seite, statt in
      sich zusammenzufallen.
- [x] **Bruchbeton** als Fallblock.

Sechs Namen aus der Fehlliste, achtundzwanzig Blöcke im Port.

### Berichtigung nach Runde 92 — die Zahlen stimmten nicht

Die Laufzahlen der Runden 89 bis 92 („24 der 119", „40", „50", „56") waren falsch, und der
Nenner war es auch.

- **Der Nenner.** 119 enthielt `#undef`, das gar kein Block ist, und drei Namen, die das
  Original selbst nicht mehr kennt (`barrel_iron`, `ladder_tungsten`, `ore_coal_oil` stehen
  dort in `ignoreMappings`). Richtig sind **115**.
- **Der Zähler.** Er zählte mal angelegte Blöcke des Ports, mal Namen des Originals. Das ist
  nicht dasselbe: aus `concrete_colored` wurden sechzehn Blöcke, aus `lightstone` fünf.

Gemessener Stand nach Runde 92: von **115** Namen sind **50** abgedeckt, **65** fehlen noch.
Realisiert wurden sie durch 90 neu angelegte Blöcke.

### Runde 93 — der RBMK wird in der Überlebensrunde baubar

Beim Nachsehen, ob sich ein vollautomatischer RBMK bauen ließe, kam heraus: die Maschinerie
steht vollständig, aber vier Stücke ließen sich nicht herstellen.

- [x] **Der selbsttätige Lader** hatte als einziger RBMK-Block überhaupt keinen Herstellungsweg
      — genau das Stück, an dem ein automatischer Brennstoffwechsel hing. Jetzt
      `ass.rbmkautoloader` in der Montagemaschine, wie im Original.
- [x] **Der Verbindungsstab** (im Original `rbmk_tool`) hatte kein Rezept. Ohne ihn lässt sich
      kein Reaktorpult auf den Reaktor ausrichten — er ist der Schlüssel zum ganzen Zweig.
- [x] **Die neun Anzeige- und Bedientafeln** hatten keine Rezepte. Die Blöcke stehen seit den
      Runden 43 bis 47, ihre Muster fehlten. Damit ist der Reaktor an Redstone anschließbar.
- [x] **Der Ladeblock `rbmk_loader`** war gar nicht portiert. Der Name führt in die Irre: das
      ist kein Kran, sondern der Rohranschluss **unter** der Säule. Ohne ihn hat eine Kessel-,
      Heiz- oder Kühlsäule genau einen Anschluss, oben auf dem Deckel; mit ihm kommen die vier
      Seiten des Ladeblocks und der Platz darunter hinzu, und die ganze Rohrführung lässt sich
      unter den Reaktorboden legen — so wie es die Anlagen des Originals tun.

Er ist der erste Block des Ports, der `IFluidConnectorBlockMK2` benutzt; die Schnittstelle lag
seit dem Fluidnetz bereit und hatte bis jetzt keinen Abnehmer.

Die Ausgabestellen der drei fluidführenden Säulen standen dreimal fast gleich da und liegen
jetzt einmal in `RBMKBaseBlockEntity`.

**Was damit geht:** Reaktor mit selbsttätigen Steuerstäben (`rbmk_control_auto`, sie folgen der
Temperatur ihrer eigenen Säule), selbsttätigem Brennstoffwechsel (der Lader tauscht ab
eingestellter Restanreicherung; seine Fächer sind seitenscharf freigegeben, ein Vanilla-Trichter
kann also nachfüllen und leeren), Dampfkreis über Kessel, Turbine und Kondensator, und der
Warte aus Pult, Zeigern und Hebeln.

### Runde 94 — das Band

Erster Teil des Fördernetzes. NTMs Gegenstandstransport fehlte im Port ganz; gefunden habe ich
dabei ein halb gelegtes Fundament — die vier Schnittstellen unter `api/hbm/conveyor`, die
Basisklasse und die beiden Entitätsklassen lagen da, ohne Abnehmer und mit leeren Rümpfen.

- [x] **Vier Bänder** — einfach, Schnellband, Doppel- und Dreifachband. Das Schnellband fährt
      dreifache Schrittweite, das Doppelband hat zwei Spuren, das Dreifachband drei mit einem
      Totbereich um die Mitte, damit nichts zwischen den Spuren springt.
- [x] **Richtung und Kurve** stecken im Original in *einer* Metadaten-Zahl (2 bis 5 sind die
      Himmelsrichtungen, +4 Linkskurve, +8 Rechtskurve). In 1.21 sind daraus zwei
      Blockstate-Eigenschaften geworden.
- [x] **Der fahrende Gegenstand.** Das Band bewegt nichts — es sagt nur, wohin. Bewegt wird der
      Gegenstand von sich selbst: er ist eine eigene Entität und fragt bei jedem Takt den Block
      unter sich. Fällt er von der Strecke, wird wieder gewöhnliche Beute daraus.
- [x] **Drei leere Rümpfe gefüllt**: das Aufnehmen abgelegter Gegenstände, das Betreten einer
      annehmenden Maschine und das Herunterschlagen vom Band waren angelegt, aber leer.
- [x] **Ein Fehler im Fundament berichtigt:** das Weiterfahren stand im falschen Zweig, nämlich
      *innerhalb* von „liegt auf einem Band". Wer die Strecke verlässt und das überlebt — ein
      Paket etwa — wäre stehengeblieben, statt mit seinem Schwung noch in eine Maschine zu
      fahren. Im Original steht es außerhalb.
- [ ] **Abweichung:** im Original haben Bänder keine Werkbankrezepte; man stellt den Bandstab
      her, und der setzt sie. Der Stab kommt in eigener Runde; bis dahin liegen seine Muster
      unmittelbar auf den Bändern.
- [ ] **Abweichung:** der Schraubenzieher schaltet gerade → Linkskurve → Rechtskurve → gerade.
      Im Original wird das einfache Band an der letzten Stelle zum Steigband; das gibt es im
      Port noch nicht.

**Noch offen im Fördernetz:** der Bandstab, die Kranmaschinen (Einleger, Auszieher, Greifer)
und die Paketstrecke (Packer, Entpacker, Verteiler, Weiche).

### Runde 95 — Schacht und Steigband

Die senkrechten Strecken. Damit kann eine Bandstrecke Stockwerke wechseln.

- [x] **Das Steigband** trägt nach oben. Es weiß selbst, wo im Stapel es steht: ist über ihm
      kein Band und keine annehmende Maschine mehr, ist es das oberste Stück — dort wird der
      Gegenstand waagerecht abgesetzt und der Block ist nur halb hoch.
- [x] **Der Schacht** ist das Gegenstück und lässt nach unten fallen. Geht es unter ihm weiter,
      fällt der Gegenstand mit fünffacher Schrittweite durch; im letzten Stück wird er
      abgebremst und waagerecht abgesetzt.
- [x] **Der Ring des Schraubenziehers ist geschlossen**: gerade → Linkskurve → Rechtskurve →
      Steigband → Schacht → wieder gerade. Damit ist die Abweichung aus Runde 94 erledigt.
- [x] **Die Basisklasse aufgeteilt**, wie im Original: `ConveyorBaseBlock` führt nur die
      Richtung, `ConveyorBendableBlock` die Kurve. Schacht und Steigband kennen keine Kurve und
      hätten sonst dreimal so viele Blockstates mit sich geschleppt.
- [ ] **Abweichung:** das Original liest für das Steigband die Nachbarn bei jeder Abfrage neu.
      In 1.21 steht das Ergebnis in einer Blockstate-Eigenschaft und wird nachgeführt — anders
      ginge es nicht, denn Modell und Umriss werden nach dem Blockstate gewählt.
- [ ] **Abweichung:** beide zeichnet das Original mit einem eigenen Renderer, der Rollen und
      Kette zeigt. Hier stehen zwei schlichte Kästen; die Form stimmt, das Bild ist einfacher.

### Runde 96 — Kranbasis und Einleger

Die Kranmaschinen sind das Bindeglied zwischen Band und Maschine — ohne sie kommt eine
Bandstrecke nirgends an.

- [x] **Die Kranbasis.** Jede Kranmaschine hat eine Eingangs- und eine Ausgangsseite; der
      Schraubenzieher stellt sie ein, ohne Schleichtaste die eine, mit ihr die andere. Beide
      können nicht dieselbe sein — wer eine auf die andere legt, schiebt die andere auf die
      Gegenseite, wie im Original.
- [x] **Der Einleger** nimmt an, was vom Band in ihn hineinfährt, und schiebt es in die Maschine
      an seiner Ausgangsseite. Was dort keinen Platz findet, bleibt in seinen einundzwanzig
      eigenen Fächern und wird bei jedem Takt erneut angeboten. Bleibt auch dafür kein Platz,
      entscheidet der Schalter: vernichten oder fallen lassen.
- [ ] **Abweichung:** im Original steckt die Eingangsseite in den Blockmetadaten, die
      Ausgangsseite in der Blockentität. In 1.21 stehen beide im Blockstate — nicht nur weniger
      Code, sondern nötig, weil das Modell danach gewählt wird.
- [ ] **Abweichung:** das Original spricht die Nachbarmaschine über `IInventory` an. Hier läuft
      es über die Item-Capability — damit erreicht der Einleger auch Kisten und Maschinen
      fremder Mods, was dort nicht ging.
- [ ] **Abweichung:** das Original hat dreizehn Pfeilbilder je Maschine, mit Abbiegungen nach
      allen Seiten. Hier stehen zwei: Eingang und Ausgang. Man sieht, *wo* es hinein- und
      hinausgeht, nicht wie der Weg im Inneren verläuft.

### Runde 97 — der Auszieher

Das Gegenstück zum Einleger: er holt aus der Maschine an seiner einen Seite und setzt das
Geholte auf das Band an seiner anderen. Damit schließt sich die Strecke — Maschine, Band,
Maschine.

- [x] **Der Auszieher.** Man beachte die Vertauschung: gezogen wird an der *Ausgangs*seite,
      abgelegt an der *Eingangs*seite. Das steht so im Original — die beiden Seiten heißen dort
      nach der Sicht des Bandes, nicht nach der der Maschine.
- [x] **Neun Fächer als Zwischenlager**, falls gerade kein Band da ist; sobald wieder eines
      steht, werden sie zuerst geleert.
- [x] **Ist das Ziel zugleich eine annehmende Maschine** — ein Einleger etwa —, wird der
      Gegenstand unmittelbar hineingegeben, statt erst ein Stück zu fahren.
- [ ] **Nicht übernommen: Filter und Aufwertungen.** Im Original lassen sich neun Muster
      hinterlegen und als Weiß- oder Schwarzliste führen, und zwei Aufwertungen erhöhen Takt und
      Menge. Beides hängt an Teilen, die der Port nicht hat — dem Mustervergleicher
      (`ModulePatternMatcher`) und den Gegenständen `upgrade_ejector` und `upgrade_stack`. Ohne
      sie verhält sich der Auszieher wie der frisch gesetzte, unaufgewertete des Originals.
- [ ] **Der Greifer wartet.** Er nimmt vom einen Band und setzt auf ein anderes — und tut das
      *nur*, um dabei zu filtern. Ohne den Mustervergleicher wäre er ein Block ohne Zweck. Er
      kommt mit dem Vergleicher.

**Das Fördernetz ist damit benutzbar:** Auszieher → Band (gerade, Kurve, Steigband, Schacht,
einfach bis dreifach) → Einleger. Offen bleiben der Greifer, die Paketstrecke (Packer,
Entpacker, Verteiler, Weiche) und der Bandstab — alle drei hängen an Teilen, die dem Port noch
fehlen, oder sind Bequemlichkeit.

### Runde 98 — Türen, Leitern, Zaun, Falltür

Zurück zu Stufe 5. Diese Gruppe ist die, die sich am saubersten abbilden lässt: 1.21 bringt
alle sechs Blockarten fertig mit, das Original baut sie sich noch selbst zusammen.

- [x] **Drei Türen** — Metall, Büro, Bunker. Sie lassen sich von Hand öffnen, obwohl sie aus
      Metall sind; im Original überschreiben sie dafür eigens die Prüfung, die Minecraft
      eingebaut hat. In 1.21 hängt das am Klangsatz, also steht dafür ein eigener bereit.
- [x] **Die Falltür** dagegen geht nur mit Redstone auf — die des Originals übernimmt die
      Prüfung unverändert. Sie nimmt den Eisensatz.
- [x] **Stahlleiter**, **Maschendrahtzaun** in den beiden Bauformen Feld und Pfosten (im
      Original zwei Metadaten-Werte), **Kette** und **Schmalspurgleis**.
- [x] **Die Kette lässt sich erklettern** — anders als die von Minecraft. Im Original ist das
      ihr ganzer Zweck: sie hängt in Schächten, durch die man hinauf und hinunter kommen soll.
- [ ] **Abweichung:** die Kette hängt im Original in alle sechs Richtungen, in 1.21 auf drei
      Achsen. Die Umsetzungstabelle bildet das ab.
- [ ] **Abweichung:** die Kette hat im Original kein Rezept — sie kommt nur in Verliesen vor.
      Hier bekommt sie eines, weil der Port ihre Verliese noch nicht baut.

Gemessener Stand: von 115 Namen sind **58** abgedeckt, **57** fehlen noch
(`tools/structure-gap.py`).

Von Hand ist das offenbar nicht zuverlässig zu führen, also wird es ab jetzt gemessen:
[`tools/structure-gap.py`](../tools/structure-gap.py) liest die 79 Bauwerke aus
`hbm-upstream/master`, zieht die in `NtmBlocks.java` angelegten Blöcke ab und nennt mit
`--list` die Namen, die noch fehlen.


Aus der nachgeholten Analyse hervorgegangen (siehe
[`gap-analysis/restliche-subsysteme.md`](gap-analysis/restliche-subsysteme.md)). Die
Weltgenerierung stand bisher nirgends in der Roadmap und ist mit **15 % die schlechteste
Abdeckung im ganzen Projekt**.

Im Port gibt es **kein einziges Gebäude**. Vorhanden sind sechs Features (Ölflecken, Ölblasen,
Bodenschätze, Landminen, abgestürzte Bombe) und die Meteoritenstruktur — das war es.

- [ ] **28 Strukturklassen** (`world/gen`) — Bunker, Silos, Raketenbasen
- [ ] **13 Verliese** (`world/dungeon`) — Labore, Forschungsstationen
- [ ] **22 Generatoren** (`world/generator`) — Erzverteilung, Ölfelder, Sellafield
- [ ] **3 eigene Biome** (`world/biome`)

> **Warum das keine gewöhnliche Portierung ist:** 1.21 hat die Weltgenerierung auf
> datengetriebene JSON-Strukturen und Jigsaw-Pools umgestellt. Die Strukturklassen des
> Originals setzen Block für Block in handgeschriebenem Java. Das ist eine Neuentwicklung in
> einem anderen Paradigma — die Zeilenzahl des Originals taugt hier nicht als Aufwandsschätzer,
> und zwar nach oben *wie* nach unten.

### Die 79 fertigen Bauwerke sind nicht der billige Teil

Das Original hat **79 `.nbt`-Dateien** unter `assets/hbm/structures/` — fertig gebaute Häuser,
Fabriken, Wracks, Ruinen. Auf den ersten Blick sind sie die Abkürzung in diese Stufe: fertige
Bauwerke, die 1.21 mit einem `StructureTemplate` nur noch setzen müsste.

**Das geht nicht.** Geprüft an `desert_shack_1.nbt`: die Dateien sind zwar gzip-NBT, aber in einem
*eigenen* Format des Originals (`version: 1`), nicht im Format der Strukturblöcke von 1.21. Ihre
Palette speichert Blöcke im Namensschema von vor der Flattening-Umstellung, mit der
Metadaten-Zahl als Eigenschaft:

```
minecraft:planks      + meta "2"   →  1.21: minecraft:birch_planks
minecraft:fence                    →  1.21: minecraft:oak_fence
minecraft:double_stone_slab        →  1.21: minecraft:smooth_stone
minecraft:wall_sign                →  1.21: minecraft:oak_wall_sign
hbm:tile              + meta       →  ein anderer Blockname je Metadaten-Wert
```

Jede der 79 Dateien braucht also einen **Umsetzer**: die vollständige Flattening-Tabelle von
1.7.10 nach 1.13 für die Vanilla-Blöcke, dazu die eigene Umbenennungstabelle des Ports für alles
mit `hbm:`-Präfix. Das ist zu schreiben, bevor das erste Gebäude in der Welt steht — aber es ist
einmalige Arbeit, die danach für alle 79 gilt.

### Und der Umsetzer ist erst das zweite Problem

Nach dem Umsetzer wäre die Frage, welche Blöcke die Bauwerke überhaupt brauchen. Ausgezählt über
alle 79 Dateien (siehe [`gap-analysis/bauwerke.md`](gap-analysis/bauwerke.md)):

| | |
|---|---:|
| verschiedene (Blockname, Metadaten)-Paare | 664 |
| davon `hbm:` | 481 Paare auf **185** Blocknamen |
| `hbm:`-Blöcke, die der Port hat | 59 |
| davon nur unter anderem Namen (`concrete_brick_slab` → `brick_concrete_slab`) | 4 |
| davon in 1.21 kein eigener Block mehr (Doppelstufen → `type=double`) | 3 |
| davon kein Block bzw. im Original selbst nicht mehr vorhanden | 4 |
| **neu anzulegende Blöcke** | **115** |
| davon nach Runde 92 erledigt | 50 |
| **Bauwerke, die sich heute vollständig bauen ließen** | **0 von 79** |

Die letzte Zeile ist die entscheidende. Selbst das einfachste Stück — ein 3×3-Beutestück aus dem
Meteoritenfeld — braucht eine Blockart, die es im Port nicht gibt. Es gibt kein Bauwerk, mit dem
sich anfangen ließe, ohne vorher Blöcke nachzuziehen.

Die 115 fehlenden sind fast durchweg Bauwerk und Zierat: Beton in allen Spielarten, Ziegel,
Treppen, Stufen, Kisten, Rohre, Schilder, Türen. Sie sind im Port bisher übersprungen worden,
weil sie nichts *tun* — genau deshalb stehen sie jetzt im Weg.

**Empfohlene Reihenfolge für Stufe 5:**

1. **Die 115 fehlenden Blöcke** — viel Arbeit, aber mechanische: die meisten sind einfache Blöcke
   ohne Verhalten. Treppen und Stufen brauchen nicht einmal eigene Texturen; sie leihen sich die
   des Grundblocks, so wie es der Port bei `brick_concrete_stairs` schon macht.
2. **Der Umsetzer** für das Dateiformat, samt Flattening-Tabelle (183 Vanilla-Paare) und
   Umbenennungstabelle (481 `hbm:`-Paare). Einmalige Arbeit, danach gilt sie für alle 79.
3. **Die 79 Bauwerke** in einem Zug.
4. Erst dann die handgeschriebenen Strukturklassen, Verliese, Biome und Erzverteilung.

Wer diese Reihenfolge umdreht, schreibt zuerst die aufwendigste Arbeit und hat am Ende immer noch
kein Gebäude in der Welt.

## Analysestand

Alle fünfzehn Subsysteme sind untersucht: die fünf umfangreichsten in der Erstanalyse
(Maschinen, Reaktoren, Waffen, Energie, Fluide), die übrigen zehn nachgezogen.

**Die beiden Analysen sind unterschiedlich belastbar.** Die ersten fünf beruhen auf
Mechanik-für-Mechanik-Vergleichen und nennen Personentage; die zehn nachgezogenen beruhen auf
Zählungen und nennen bewusst keine. Wer eine Gesamtzahl in Personentagen braucht, muss die zehn
erst auf demselben Weg untersuchen wie die fünf.

---

## Stand nach Runde 104 — das Fördernetz ist fertig

Die Runden 94 bis 104 haben NTMs Gegenstandstransport vollständig nachgezogen. Er fehlte im Port
zuvor ganz; was dalag, waren vier Schnittstellen und ein paar leere Rumpfmethoden.

**Vollständig portiert:**

| Teil | Runde |
|---|---|
| Band: gerade, Kurve, Schnellband, Doppel-, Dreifachband | 94 |
| Steigband und Schacht | 95 |
| Kranbasis und Einleger | 96 |
| Auszieher | 97 |
| Mustervergleicher und Greifer | 99 |
| Paket, Packer, Entpacker | 100 |
| Verteiler (sechs Seiten, je eigener Filter) | 101 |
| Portionierer | 102 |
| Weiche (zwei Blöcke, einstellbares Verhältnis) | 103 |
| Bandstab | 104 |

**Zwei Funde, die schwerer wiegen als die Runden selbst:**

- **Der Schraubenzieher hat nie geschraubt** (Runde 99). Er war ein gewöhnlicher Gegenstand;
  nichts im Port hat je `onScrew` aufgerufen. Damit war *jede* der fünfzehn Maschinen mit
  `onScrew(ToolType.SCREWDRIVER)` in Wahrheit nicht einstellbar — die RBMK-Säulen, die Heizer,
  das Gießbecken, der Drescher und das ganze Förderband-System. Behoben an zwei Stellen: die
  Schraubenzieher sind jetzt `ToolingItem`, und das Werkzeug greift über `onItemUseFirst`, also
  *vor* der Blockabfrage.
- **Die Maschinen der Runden 99 bis 103 standen nicht im Kreativreiter** (nachgetragen in
  Runde 104).

**Drei Löcher in den Torwächtern geschlossen:**

- Der Importwächter beurteilt jetzt auch **Fremdtypen mit bekanntem Paket** — die Paketzuordnung
  baut er sich aus den eigenen expliziten Importen des Projekts auf (Berichtigung zu Runde 98).
- Der Vererbungswächter kennt **Außenpflichten über Schnittstellen**, nicht nur über Oberklassen
  (`MenuProvider` → `createMenu`).
- Sein Methodenleser erkannte die Form `public @Nullable Typ name(...)` gar nicht. Das war das
  ältere der beiden Löcher und hat die neue Regel prompt dreizehnmal falsch anschlagen lassen.

## Gemessener Gesamtstand (Runde 104)

| Bereich | Port | Original | Anmerkung |
|---|---:|---:|---|
| Fluide | 156 | 156 | vollständig |
| Blöcke | 566 | 919 | |
| Gegenstände | 958 | 1690 | |
| Entitätstypen | 61 | 185 | |
| Maschinen (`TileEntityMachine*`) | 47 | 68 | |
| Rezeptklassen | 31 | 58 | |
| Geschütze | 9 | 15 | fehlen: arty, baseartillery, basent, fritz, himars, maxwell, richard, tauon |
| Munitionsfamilien (`XFactory*`) | 16 | 24 | fehlen: Accelerator, Drill, Energy, Flamer, Folly, PA, Rocket, Tool |
| Kreaturen | 2 | ~29 | nur Ente und Atomcreeper |
| Bauwerksblöcke (Stufe 5) | 58 | 115 | nachzurechnen mit `tools/structure-gap.py` |
| **Bauwerke in der Welt** | **0** | **79** | kein einziges wird platziert |

Die Erzgenerierung dagegen **steht**: 28 konfigurierte Merkmale unter
`data/hbmsntm/worldgen/`, Erze aller Art erscheinen im Boden. Was fehlt, sind die Gebäude.

---

## Stand nach Runde 112 — die Maschinen

Das Ziel dieser Runden lautete „erst die Maschinen und dann die Bauwerke". Die Runden 105 bis 112
haben zwölf Maschinen nachgezogen; drei weitere sind begründet **nicht** portiert worden.

**Portiert:**

| Maschine | Runde | Was sie ausmacht |
|---|---|---|
| Absauger, Gebläse, Wasserpumpe elektrisch und Dampf | 105 | der Übergang zwischen Rohrnetz und Welt |
| Selbstbauer (Autocrafter) | 106 | zweiter Abnehmer des Mustervergleichers |
| Elektrische Presse | 107 | Kohlepresse mit Strom, ohne Anlauf |
| Trichter | 107 | presst 4er- und 9er-Rezepte selbst zusammen |
| Industrieturbine | 108 | Dampfturbine mit Schwungrad |
| Strangguss | 109 | neun Güsse auf einmal, mit Wasserkühlung |
| Hephaestus | 110 | Wärmetauscher, der seine Wärme aus dem Boden holt |
| Teleporter samt Verbindungsstück | 111 | versetzt auch über Weltgrenzen |
| Schlüsselschmiede | 112 | kopiert und würfelt Schlüsselzahnungen |
| Strommelder | 112 | Rotsteinsignal, solange Strom ankommt |

**Nicht portiert, mit Begründung:**

- `machine_igenerator` und `machine_lpw2` sind im Original **tot**: `updateEntity` ist leer, LPW2
  hat nicht einmal eine Maschine. Sie zu übersetzen hieße, toten Code zu portieren.
- `machine_large_turbine` ist im Original ausdrücklich als **veraltet** gekennzeichnet — kein
  Rezept, keine Montagevorschrift, nur noch in der Schöpferrunde zu holen. Sie steht dort allein,
  damit alte Welten nicht zerbrechen; eine Neuportierung hat keine alten Welten zu schützen.
- Die **Munitionspresse** braucht `AmmoPressRecipes`, ein eigenes Rezeptwerk. Sie wäre nicht eine
  Maschine, sondern ein halbes System, und kommt mit ihren Rezepten.
- Der **Strahlungsgenerator** braucht `nuclear_waste_short` und `nuclear_waste_long` samt ihren
  fünfzehn Klassen; die **Sirene** braucht das Kassettenwerk. Beides fehlt dem Port noch.

**Drei Funde in fremdem Code:**

- **Der Kolossturbinenblock trug den Codec der Chemiefabrik** (Runde 108). `MachineChungusBlock.CODEC`
  war ein `simpleCodec(MachineChemicalPlantBlock::new)` — eine Verwechslung beim Abschreiben, die
  übersetzt, weil `Block.codec()` ohnehin nur `MapCodec<? extends Block>` verlangt. Der ganze Baum
  wurde abgesucht: es war der einzige.
- **Von den acht Rohranschlüssen des Hephaestus arbeiten im Original nur zwei** (Runde 110). Das
  Original setzt sie um einen Punkt, der zwei Blöcke hinter dem Kern und damit außerhalb des
  Bauwerks liegt; `makeExtra` tut dort nichts. Der Port setzt sie richtig.
- **Ein `@Override` ohne Ziel** kostete Runde 109 einen CI-Lauf und deckte ein Loch im
  Vererbungswächter auf (siehe unten).

**Ein Loch im Torwächter geschlossen (Runde 110):** `override-check.sh` prüfte in drei Durchgängen,
ob eine geforderte Methode *fehlt* — nicht, ob eine vorhandene ins Leere zeigt. Die neue, vierte
Regel fragt nach dem **Ausreißer**: für jede Methode, die eine Projekt-Schnittstelle erklärt, werden
alle Klassen gesammelt, die sie über `@Override` erklären; erreichen alle bis auf eine die Methode
über jene Schnittstelle, ist diese eine der Fehler. Gemessen: über den ganzen Baum null Funde, mit
dem Fehler aus Runde 109 wieder eingesetzt genau ein Fund. Die naheliegendere Fassung („ein
`@Override`, dessen Name das Projekt kennt, aber nicht in der eigenen Verwandtschaft") liefert über
vierzig Falschmeldungen und steht jetzt als *verworfen* in der Datei.

## Gemessener Maschinenstand (Runde 112)

Gezählt werden die Klassen `src/main/java/com/hbm/tileentity/machine/TileEntityMachine*.java` des
Originals — **ohne** Unterordner, derselbe Nenner wie in der Tabelle nach Runde 104. Als vorhanden
gilt eine Maschine, wenn es im Port eine Klasse `<Name>BlockEntity` oder `Machine<Name>BlockEntity`
gibt, gleich in welchem Ordner.

| | |
|---|---:|
| Maschinen im Original | 68 |
| davon im Port | **42** |
| fehlen | 26 |

**BERICHTIGUNG zur Tabelle nach Runde 104:** dort steht „47 von 68". Nachgezählt mit dem oben
genannten Verfahren waren es vor dieser Runde **31**. Die 47 ist nicht nachvollziehbar und wird
hiermit ersetzt; nachgezählt wird ab jetzt mit dem beschriebenen Verfahren, damit die Zahl
reproduzierbar bleibt.

**Es fehlen noch:** AmmoPress, Annihilator, AssemblyFactory, Autosaw, ChemicalFactory,
CompressorBase, Cyclotron, Excavator, ExposureChamber, GasCent, IGenerator\*, LPW2\*,
LargeTurbine\*, MiningLaser, MissileAssembly, OreSlopper, PrecAss, RadGen, RadarLarge, RadarNT,
RadarScreen, SatDock, SatLink, Siren, SuperComputer, TapeDrive.
(\* = begründet verworfen, siehe oben. Damit sind es der Sache nach 23.)

**Als Nächstes:** der Rest der Maschinen, dann die Bauwerke — 57 der 115 Bauwerksblöcke fehlen
noch, und platziert wird bis heute keines der 79.

## Gemessener Maschinenstand (Runde 120)

Dasselbe Verfahren wie nach Runde 112, unverändert, damit die Zahlen vergleichbar bleiben.

| | |
|---|---:|
| Maschinen im Original | 68 |
| davon im Port | **49** |
| das Verfahren meldet als fehlend | 19 |
| davon begründet verworfen | 3 |
| davon Falschmeldungen des Verfahrens | 2 |
| **der Sache nach fehlen** | **14** |

Dazugekommen seit Runde 112: ChemicalFactory (113), AssemblyFactory (114), GasCent (115),
Cyclotron (116), MiningLaser (118), Autosaw (119), RadarLarge (120).

**ZWEI FALSCHMELDUNGEN DES VERFAHRENS**, beide in dieser Runde nachgewiesen und hiermit
festgehalten — das Verfahren sucht nach Klassennamen, und zwei Maschinen stehen im Port unter
einem anderen:

- **CompressorBase** ist im Original eine *abstrakte* Basisklasse. Der Port hat sie in
  `MachineCompressorBlockEntity` hineingefaltet; der Klassenkommentar dort sagt es ausdrücklich
  („TileEntityMachineCompressor + TileEntityMachineCompressorBase … beide Klassen sind hier
  zusammengefasst"). Es fehlt nichts.
- **RadarNT** ist im Port `MachineRadarBlockEntity` — dieselbe Klasse, nur ohne das „NT" im
  Namen. Nachweis: sie trägt den Kommentar des Originals („Now with SmЯt™ lag-free entity
  detection!") und dieselben Felder (`scanMissiles`, `smartMode`, `redMode`, `jammed`, …).

**Es fehlen der Sache nach noch:** AmmoPress, Annihilator, Excavator, ExposureChamber,
MissileAssembly, OreSlopper, PrecAss, RadGen, RadarScreen, SatDock, SatLink, Siren,
SuperComputer, TapeDrive.

**Fortgeschrieben, Runde 122:** SatLink ist portiert — die Bodenstation war die einzige der
vierzehn, deren Teilsystem im Port schon stand und der nur ein `getInfo(Level)` an den
Satelliten fehlte. Damit sind es **dreizehn**. Die Zahlen der Tabelle oben bleiben, wie sie
gemessen wurden; sie sind der Stand nach Runde 120.

**Fortgeschrieben, Runde 123:** TapeDrive ist portiert, und mit ihm das Teilsystem, an dem drei
Maschinen hingen: `DriveItem` samt `DriveType`. Damit sind es **zwölf**. Was sich dadurch an der
Tabelle oben ändert:

| Maschine | Stand nach Runde 123 |
|---|---|
| TapeDrive | **portiert** |
| SuperComputer | wartet nur noch auf `ModuleMachineSuperComputer` und `SuperComputerRecipes`; die Laufwerksarten stehen |
| Annihilator | wartet weiter auf `AnnihilatorSavedData` und `EnumAmmo`; die Laufwerksarten stehen |

Neu ist außerdem der **Wissenschaftssatellit** (`SatelliteScience`) — nicht als eigene Maschine,
sondern weil er der einzige Datenlieferant des Originals ist. Ohne ihn hätte die Laufwerkskiste
nichts zu schreiben. Portiert ist davon die Messreihe alle fünfzehn Minuten; die Messfühler und
die Weltraumfabrik hängen an `SpaceAssemblerRecipes`, den Abholfächern und der Abwurfkapsel und
bleiben vorerst draußen. Die Stelle dafür steht im Klassenkommentar.

**Fortgeschrieben, Runde 124:** SuperComputer ist portiert — der Abnehmer der beschriebenen
Datenträger, und damit schließt sich die Kette aus den Runden 122 bis 124: Satellit → Bodenstation
→ Laufwerkskiste → Großrechner. Damit sind es **elf**.

Nicht mitportiert sind die beiden Rezepte, die aus Papier und Farbstoff eine Blaupausenmappe
ziehen (`com.blueprints`, `com.beigeprints`). Die Mappe ist im Original ein eigener Gegenstand,
aus dem man eine *zufällige* Blaupause zieht; der Port kennt nur die Blaupause selbst, an einen
festen Rezeptvorrat gebunden. Steht im Klassenkommentar.

**Fortgeschrieben, Runde 125:** AmmoPress ist portiert. Damit sind es **zehn**.

**BERICHTIGUNG zur Abhängigkeitstabelle:** dort steht, die Munitionspresse warte auf
„`GunFactory.EnumAmmo` und `AmmoPressRecipes`". Die Hälfte davon stimmt nicht — `GunFactory.Ammo`
steht im Port seit den Waffenrunden vollständig da, Eintrag für Eintrag wie im Original. Gefehlt
hat nur `AmmoPressRecipes`. Die Tabelle stammt aus Runde 120 und ist an dieser Stelle gegen den
falschen Klassennamen (`EnumAmmo` statt `Ammo`) geprüft worden.

Neunundsiebzig der neunundachtzig Rezepte des Originals sind dabei — und es sind die **ersten
Munitionsrezepte des Ports überhaupt**: die Waffen der Runden 81 bis 87 standen bis hierher ohne
jeden Weg, ihre Munition herzustellen. Zehn Rezepte fehlen, weil ihnen ein Gegenstand fehlt: der
Diesel-, Gas- und Balefire-Kanister (fünf Rezepte) und die Minibomben-Hülle (vier). Steht im
Klassenkommentar.

**Fortgeschrieben, Runde 126:** RadarScreen ist portiert. Damit sind es **neun**. Mit ihm kommen
der Radar-Verbinder und — endlich — das Behälter-Menü des Radars: der Nebenbefund aus Runde 120,
dass die zehn Fächer des Radars nur über Trichter erreichbar sind, ist damit erledigt. Der
Verbinder gehört ins neunte Fach, also musste das Fach erreichbar werden.

**FEHLER IM PORT, dabei gefunden und behoben:** `RadarEntry.decode` las den Namen des Eintrags aus
dem Puffer, wies ihn aber nicht zu — das Feld blieb auf der Client-Seite immer `null`. Aufgefallen
ist es erst, weil der Schirm dieselben Einträge weiterreicht; im Original steht die Zuweisung da.
Bisher fiel es niemandem auf, weil die Kartenoberfläche den Namen nicht anzeigt.

**RADGEN IST GEPRÜFT UND BLEIBT LIEGEN.** Die Tabelle nennt `ItemWasteShort` und `ItemWasteLong`,
und das stimmt — sie untertreibt sogar. Der Strahlungsgenerator kennt vier Brennstoffgruppen:
den kurz- und den langlebigen Müll (beide fehlen im Port, sie kämen aus SILEX und dem
RBMK-Müllzerfall, die es beide nicht gibt), den radioaktiven Schrott (fehlt) und den
Strahlenkristall `GEM_RAD`. Letzterer **steht** im Port — aber ohne jede Quelle, er liegt nur in
einer Schöpferrunde. Der Generator hätte also keinen einzigen beschaffbaren Brennstoff und wäre
eine Maschine, die niemand anwerfen kann. Er wartet damit auf ein Teilsystem, nicht auf sich
selbst.

**Fortgeschrieben, Runde 127:** Siren ist portiert, samt der Kassetten und ihrer einundzwanzig
Tonspuren. Damit sind es **acht**.

**EIN LOCH IN DER TONKULISSE, dabei gefunden und gemessen.** Ein Tonereignis wird an zwei Stellen
erklärt: als `SoundEvent` in `NtmSoundEvents` und als Eintrag in `sounds.json`, der auf eine Datei
zeigt. Fehlt die zweite Hälfte, ist das Ereignis anstandslos registriert, spielbar — und **stumm**.
Keine Meldung, kein Absturz, es passiert einfach nichts.

Gemessen bei der Aufnahme: **110 von 157 registrierten Ereignissen waren stumm.** Die Tondateien
liegen alle im Baum — 209 `.ogg`-Dateien, davon 87 für Waffen und 75 für Blöcke —, es fehlen nur
die Einträge. Betroffen sind die Waffen aus den Runden 81–87, die Türen, die Türme und ein
gutes Dutzend Maschinen.

Behoben sind in dieser Runde die drei Alarme, die die Sirene braucht (`alarm.hatch`,
`alarm.soyuzed`, `alarm.chime`), dazu kommen 19 neue Alarmtöne. Die restlichen **107** stehen in
`tools/sound-baseline.txt` — als Liste offener Arbeit, nicht als Freibrief. Der neue sechste
Torwächter `tools/sound-check.sh` prüft, dass kein *neues* stummes Ereignis dazukommt, dass jeder
Eintrag auf eine vorhandene Datei zeigt und dass kein Eintrag ohne Registrierung dasteht.
Gemessen: sauber null Funde, mit einem künstlich eingefügten stummen Ereignis genau ein benannter
Fund.

**Fortgeschrieben, Runde 128:** Annihilator ist portiert. Damit sind es **sieben**.

**DREI WEITERE GEPRÜFT UND ZURÜCKGESTELLT**, jeweils gegen den Quelltext beider Seiten:

| Maschine | Befund |
|---|---|
| OreSlopper | hängt am Grundgesteinserz-Teilsystem (`ItemBedrockOreNew`, `BlockBedrockOreTE`). Der Port sagt es selbst — die Kommentare in `ArcFurnaceRecipes` und `CrystallizerRecipes` halten es fest. |
| Excavator | dasselbe Teilsystem, dazu `BlockDepth`. Die beiden sind nicht einzeln zu lösen. |
| ExposureChamber | braucht `particle_higgs`, `particle_dark` und `particle_sparkticle`. Der Port hat `PARTICLE_EMPTY`, `MUON`, `DIGAMMA` und `LUTECE` — keines der drei. |

**Der Annihilator dagegen war erreichbar**, und er schließt eine Lücke, die dem Port bisher nicht
anzusehen war: er ist der **einzige Weg an die 528er-Blaupausen**. Gemessen: das Original verteilt
60 Rezepte auf 13 solcher Pools, der Port hat davon **17 Rezepte in 6 Pools** — gascent,
ferrouranium, chlorophyte, tcalloy, bmg und controller. Diese sechs Schwellen sind portiert; die
übrigen sieben hätten im Port einen leeren Vorrat ausgeschüttet und stehen mit ihren
Originalzahlen als Kommentar daneben.

**Zwei Abweichungen, beide dokumentiert:** die vierte Schlüsselart der Zähler (Erzwörterbuch-Name)
fällt weg, weil der Port kein Erzwörterbuch hat — die Stelle im Speicherformat bleibt aber
erhalten und wird beim Lesen übersprungen statt fehlzuschlagen. Und die Strahlung, die das
Original beim Vernichten in die Umgebung bläst, bleibt draußen.

**Runde 127 ist rot in die CI gelaufen** — ein fehlender Import (`CassetteItem` in
`NuclearTechModClient`), und der Import-Torwächter hatte ihn durchgelassen. Der Grund ist
gemessen: die Regel erkannte einen Typ nur an Konstruktor, Vererbung, Deklaration,
Generikum-Argument und Methodenreferenz — nicht am **statischen Zugriff**
(`CassetteItem.TrackType.fromMeta(...)` nennt den Typ bloß als Präfix). Das Muster ist ergänzt;
gemessen: baumweit null Funde, mit dem Fehler aus Runde 127 genau ein benannter Fund.

**Nebenbei behoben:** `SerializableRecipe` warf beim Schreiben der Vorlage, sobald ein Rezeptsatz
leer war. Bei ausgeschaltetem 528-Schalter wäre das der Annihilator gewesen. Der Haken
`allowEmptyRecipeList()` heißt im Original genauso und fehlte im Port.

**Fortgeschrieben, Runde 129:** SatDock ist portiert — samt allem, was daran hing. Damit sind es
**sechs**.

Die Station selbst ist der kleinste Teil dieser Runde: 3×3 flach auf dem Boden, fünfzehn
Entnahmefächer, ein Fach für den Satellitenchip, kein Strom, kein Schalter. Sie fragt einmal je
Sekunde beim Satelliten mit ihrer Frequenz nach; hat der etwas bereit, fällt eine Kapsel vom
Himmel.

**Alles andere daran fehlte im Port.** Der Reihe nach:

| Stück | Befund |
|---|---|
| `SatelliteBase.requestableSlots` / `tryRequestItems` | fehlten; die Fächer sind jetzt am Satelliten, nicht an der Station |
| `SatelliteMiner`, `SatelliteLunarMiner` | fehlten. In `XSatelliteRegistry` standen an ihrer Stelle **zweimal `SatelliteRelay`** mit einem `// todo miner sats` daneben — die beiden Schürfgegenstände taten also so, als wären sie Relais |
| `EntitySatellitePod` | fehlte, samt Modell und Textur (`dropship.obj`) |
| `ItemPool` | fehlte. Das Original stützt sich auf `WeightedRandomChestContent` aus 1.7.10; die Klasse gibt es nicht mehr, der Nachbau ist kürzer als die Anpassung wäre |
| **der Satellitentick** | **fehlte vollständig.** `SatelliteSavedData` wurde geladen, gespeichert und abgefragt — aber kein Satellit des Ports hat je einen Tick bekommen. Keiner brauchte bisher einen |

Der letzte Punkt ist der, der über die Runde hinausreicht: `NtmEventHandler.levelTick` ruft jetzt
`SatelliteSavedData.tickAll`, und `SatelliteBase.onUpdateTick(ServerLevel)` steht allen Satelliten
offen. Der Schürfer ist der erste, der ihn nutzt.

**Zwei Abweichungen, beide bewusst.** Die Kapsel leitet sich im Original von der
Wurfgeschoss-Basis ab, vor allem wegen der Positionsglättung auf der Client-Seite — auf 1.21 kann
das jede Entität von Haus aus, also ist sie eine gewöhnliche `Entity`. Und die Station baut ihre
eigene 3×3-Fläche nicht in jedem Tick nach; das Original tut es als Notbehelf aus einer Zeit, in
der das Setzen unzuverlässig war.

**ACHTUNG, die Kennzahlen der Satelliten verschieben sich.** Gespeichert wird der Listenplatz aus
`XSatelliteRegistry.satellites`, und `registerSatellite` hatte die beiden Relais-Platzhalter
stillschweigend verworfen — die Liste war also zwei Einträge kürzer als die Aufrufe. Mit den
echten Schürfern rückt alles ab `SatelliteHorizons` um zwei nach hinten. Ältere Spielstände des
Ports lesen ihre Satelliten danach falsch. Die Reihenfolge ist jetzt die des Originals; die
Alternative wäre gewesen, die beiden ans Ende zu hängen und dauerhaft von der Vorlage abzuweichen.

**Die Schürfsatelliten sind auch baubar**, nicht nur schöpfbar: `ass.astrominer` und
`ass.lunarminer` sind aus der Montagemaschine des Originals übernommen. Ohne sie wäre die Station
totes Werk gewesen — sie holt ihre Ladung bei einem Schürfer ab und bei keinem anderen.
Abweichungen nach bekanntem Muster: `BIGMT.plateCast()` ist die Gussplatte aus Saturnit,
`motor_bismuth` gibt es im Port nicht (dort steht der gewöhnliche Motor), und
`CONTROLLER_ADVANCED` heißt hier `CIRCUIT_ADVANCED_CONTROL_UNIT`.

**Fortgeschrieben, Runde 130:** PrecAss ist portiert. Damit sind es **fünf**.

Die Präzisionsmontage ist die einzige Maschine des Mods, die **misslingen** kann. Jedes ihrer
Rezepte ist ein Paar: gewichtet fällt entweder das Werkstück heraus oder ein **Ausschuss** — und
den nimmt sie zurück und gibt einen Teil der Zutaten wieder her. Mit dem 528er-Schalter wandert
die ganze Schaltkreisfertigung hierher; ohne ihn baut sie nur den Kristallschaltkreis und die
beiden Blaupausenmappen.

**Was dafür fehlte:**

| Stück | Befund |
|---|---|
| `NBTStack` | fehlte. Der Port hatte die Lese- und Schreibzweige dafür in `SerializableRecipe` **auskommentiert** stehen, seit er sie ohne Nutzer übernommen hatte. Die Präzisionsmontage ist der erste Nutzer — ohne ihn passte jeder Ausschuss auf jedes Rückgewinnungsrezept, und man könnte den billigsten Fehlschlag gegen die teuersten Zutaten tauschen |
| `BrokenItem` | fehlte |
| `ItemOrbitalAssembly` (Kristallschaltkreis) | fehlte |
| `registerPair` | fehlte; steht im Original an `PrecAssRecipes` selbst, nicht am Rezeptlader |

**Sie füllt drei Vorräte, die Runde 128 als leer vermerkt hatte.** Der Annihilator-Kommentar
listete `chip_bismoid`, `chip_quantum` und `strontium` unter „nicht portiert, weil der Port keine
Rezepte in diesen Poolen hat" — genau die Rezepte der Präzisionsmontage. Die drei Schwellen sind
jetzt da. `chip` bleibt offen: das Rezept gibt es, aber `BILLET_SILICON` nicht.

**Nicht übernommen:** die vier Rezepte der RPA-Rüstung. Es fehlen die Rüstung selbst, die
Legendenteile (`parts_legendary`, `EnumLegendaryType`) und der Desh-Motor. Die Originalzahlen
stehen als Kommentar in `PrecAssRecipes`, damit sie beim Nachliefern nicht neu erhoben werden
müssen.

**EIN LOCH IN DEN TORWÄCHTERN, dabei gemessen und geschlossen.** Ein absichtlich eingefügtes
`NtmItems.WIRE_GIBTESNICHT.get()` lief durch **alle sechs** bestehenden Tore, ohne dass eines
anschlug. Der Grund ist derselbe wie bei den doppelten Konstanten aus Runde 121: javac kennt die
Klasse `NtmItems`, aber der Feldtyp `DeferredItem<Item>` ist ohne NeoForge-Klassenpfad ein
Fehlertyp — und sobald der Typ fehlerhaft ist, bricht javac die Attributierung des Feldzugriffs
ab und meldet gar nichts mehr. Offline ist so ein Zugriff also unsichtbar; der Fehler fällt erst
in der CI auf.

Der neue siebte Torwächter `tools/registry-check.sh` prüft deshalb **textuell**: jeder Zugriff der
Form `Registrierklasse.NAME` wird gegen die Felder nachgeschlagen, die die Klasse wirklich
erklärt. Sechs Klassen, 2163 Felder, 14328 Zugriffe im Baum. Gemessen: baumweit null Funde, mit
dem eingefügten Fehler genau ein benannter Fund — und beim allerersten Lauf zwei **echte** Funde,
die beiden Gegenstände dieser Runde, die noch nicht registriert waren.

**Fortgeschrieben, Runde 131:** das Grundgesteinserz ist portiert, und mit ihm **Excavator und
OreSlopper**. Damit sind es **drei**.

Das war das grösste Teilsystem der letzten Runden — und das einzige, das **zwei** Maschinen auf
einmal löst. Der Port hat es seit Langem als Lücke geführt; die Kommentare in `ArcFurnaceRecipes`
und `CrystallizerRecipes` sagen es selbst.

**Die Kette, von unten nach oben:**

| Stück | was es tut |
|---|---|
| `BedrockOreFeature` | legt das Erz in die unterste Lage und mauert es mit Tiefengestein zu |
| `BedrockOreBlock` + Blockentität | unzerstörbar; weiß, was es hergibt, welche Bohrerstufe es verlangt und welche Säure |
| `MachineExcavatorBlockEntity` | frisst sich Lage für Lage nach unten, Ring für Ring nach außen, und holt das Erz heraus |
| `BedrockOreBaseItem` | die Rohprobe — sie merkt sich, wie reich die Fundstelle war |
| `MachineOreSlopperBlockEntity` | spült die Proben aus und zählt sortenweise zusammen |
| `BedrockOreItem` | sechs Sorten mal sechsundzwanzig Aufbereitungsstufen |
| `BedrockOreFragmentItem` | was am Ende herausfällt, ein Meta-Gegenstand je Material |
| `DrillbitItem` | zehn Bohrköpfe in fünf Stufen; die Stufe entscheidet, an welches Erz man herankommt |

**Drei Abweichungen, alle dokumentiert.** Das Original baut sich beim Start **156 Bilder**
zusammen, indem es eine Graustufenvorlage je Sorte umfärbt (`TextureAtlasSpriteMutatable`); auf
1.21 gibt es diese Atlas-Bastelei nicht mehr, und der Port nimmt stattdessen eine Vorlage,
sechsundzwanzig Modelle und einen Farbgeber. Erze erkennt der Bagger am Sammelbegriff `c:ores`
statt am Erzwörterbuch. Und die Seidenberührung läuft über ein verzaubertes Werkzeug statt über
Reflexion auf `createStackedBlock`.

**Eine Abweichung ist eine Verbesserung:** das Original speichert den Teilstand des Schlämmers je
Sorte **nicht**. Wer die Maschine mitten im Zählen verlässt, fängt dort von vorn an — die halb
gesammelten Bruchteile sind weg. Der Port hält sie fest.

**Fortgeschrieben, Runde 132:** MissileAssembly ist portiert. Damit sind es **zwei**.

Der Baukasten der Eigenbau-Raketen: **hundertzweiundzwanzig Bauteile**, aus denen sich eine
Rakete in fünf Teilen zusammensetzen lässt — Chip, Sprengkopf, Rumpf, Leitwerk, Triebwerk.

**Warum es so viele sind:** die Kennwerte hängen am Gegenstand, nicht am Zahlenwert des Stapels.
Jedes Teil ist ein eigener Gegenstand. Einundsechzig davon sind reine **Abschriften** —
Tarnanstriche, Flammen, Blech —, die die Kennwerte des Grundteils übernehmen und höchstens die
Haltbarkeit ändern. Neun Bilder reichen für alle hundertzweiundzwanzig.

**Die Montage prüft die Übergänge.** Ein Sprengkopf muss unten so breit sein wie der Rumpf oben,
ein Leitwerk oben so breit wie der Rumpf unten; das Triebwerk muss denselben Treibstoff
verbrennen, den der Rumpf mitführt, und stark genug sein, den Sprengkopf zu heben. Über jedem
Fach sitzt eine Leuchte. Das Leitwerk **darf fehlen** — dann bleibt seine Leuchte dunkel und die
Rakete trifft nichts, aber sie lässt sich bauen.

**Die Rakete fliegt nur, solange der Tank reicht.** Das unterscheidet sie von allen anderen
Raketen des Mods, die immer ankommen.

**Zwei Dinge fehlen noch, beide sichtbar:**

- **Die Modelle je Bauteil.** Das Original setzt die Rakete aus einem Modell je Teil zusammen
  (`MissilePart`, `MissileMultipart`, `MissilePronter` — gut dreihundert Zeilen plus ein Modell
  je Teil). Bis die portiert sind, trägt die Eigenbau-Rakete das allgemeine Raketenmodell, und
  die drehende Vorschau in der Oberfläche fehlt. Sie **fliegt und schlägt richtig ein**.
- **Vier Sprengkopfwirkungen**: CLUSTER (im Original ohnehin leer), CLOUD, TURBINE und SCHRAB.
  Ihnen fehlt im Port die Wirkung; die Bauteile gibt es trotzdem, damit die Liste vollständig
  bleibt.

**Abweichungen:** die Bauteile werden als Registriername übertragen und abgelegt, nicht als
Zahlenkennung — die ist auf 1.21 nicht mehr stabil. Der Schraubenschlüssel des Bauwegs fehlt im
Port; an seiner Stelle steht der Schraubenzieher aus Runde 99. Und der Aufruf, der sich im
Original merkt, wer an Raketen baut, um ihm später Besuch zu schicken, entfällt — den
Gegenspieler gibt es im Port nicht.

**Angefangen, Runde 133: der Teilchenbeschleuniger.** Er steht in dieser Liste nicht als Maschine
— die Roadmap führt ihn seit Runde 63 als eigenen offenen Punkt —, aber er ist der **einzige Weg**
zu ExposureChamber: deren fünf Rezepte brauchen `particle_higgs`, `particle_dark` und
`particle_sparkticle`, und die entstehen nirgendwo sonst.

Gemessen: das Albion-Teilsystem sind rund **2200 Zeilen** Kernklassen (Quelle 492, Dipol 419,
Detektor 297, Quadrupol 209, Hochfrequenzkavität 201, Strahlführung 107, gekühlte Basis 113) plus
sechs Blöcke, sechs Oberflächen, sechs Modelle und ein 204-zeiliger Rezeptsatz. Das sind zwei bis
drei Runden.

**Erster Teil:** die zehn fehlenden Teilchen — Wasserstoff, Kupfer, Blei, Antimaterie,
Antischrabidium, Higgs, Tachyon, Strangelet, Dunkle Materie und Sparkticle. Vier standen schon da
(`PARTICLE_EMPTY`, `MUON`, `DIGAMMA`, `LUTECE`).

**Zweiter Teil: der Ring steht.** Alle sechs Bauteile sind portiert — Quelle, Strahlführung,
Hochfrequenzkavität, Quadrupol, Dipol, Detektor —, dazu die Spulen (`PA_COIL`, vier Stufen), die
neun Beschleunigerrezepte, zehn Baurezepte an der Montagefabrik und fünf Oberflächen.

**Wie der Strahl läuft.** Er ist **keine Entität**: die Quelle führt ein `Particle`-Objekt und
fragt jeden Tick das Bauteil ab, auf dem es gerade steht (`steppy()`). Wer im Weg steht und keine
Strahlführung ist, bringt ihn zum Absturz — dreizehn Zustände, zehn davon Fehler, jeder mit
eigener Meldung an der Quelle. Ein Schritt je Tick am Anfang, bis zu **zehn** bei hohem Impuls;
ohne das bräuchte eine Runde durch einen großen Ring Minuten.

**Die eigentliche Schwierigkeit ist das Verhältnis 1:1.** Jede Kavität gibt 100 Impuls *und* 100
Streuung, jeder Quadrupol nimmt 100 Streuung. Bei 1000 Streuung reißt der Strahl ab, und der
Detektor verlangt am Ende **null**. Wer eine Kavität mehr aufstellt als Quadrupole, fährt
hunderte Runden und verliert alles im letzten Block.

**Der Dipol ist die Weiche und trägt die Größenschranke.** Drei Ausgänge — unter der Schwelle,
darüber, bei Redstone —, und die Spule verlangt eine Mindestkantenlänge: fünfzehn Blöcke bei Gold,
**einundfünfzig** bei Chlorophyt. Eine bessere Spule macht den Ring also nicht kleiner, sondern
größer; das Digamma-Teilchen bei 70.000 Impuls ist damit ein Bauwerk, keine Maschine.

**Nicht übernommen, mit Begründung:**

- Die **sechs Modelle**. Die Blöcke stehen als `particleOnlyBlock` — dieselbe Lösung wie beim
  Zyklotron (Runde 116) und den übrigen Großmaschinen des Ports. Sie arbeiten vollständig.
- Zwei der elf Beschleunigerrezepte: **Goldstaub + Schrabidatbarren → entartete Materie** (hängt
  an der Gegenstandsfamilie `item_expensive` des Weltraumbaus) und **Hähnchen + Hähnchen → zwei
  Nuggets** (ein Scherzgegenstand, den der Port nicht hat). Beide stehen mit ihren Zahlen als
  Kommentar im Rezeptsatz.
- Die **zweiten Zutatenlisten** (`inputItemsEx`) aller sechs Baurezepte — sie nennen durchweg
  `item_expensive`.
- Die Meldung an den **Strahlenscanner-Satelliten** (gibt es im Port nicht) und die Errungenschaft
  für Digamma. Die Meldung an den Detektorsatelliten steht.
- OpenComputers und Redstone-über-Funk, wie in jeder Runde davor.

**Gemessene Abweichung im Zeichnen:** das Original zeichnet die drei Kompasszeiger des Dipols mit
dem Tessellator und rechnet den Winkel selbst in einen Vektor um. Der Port dreht ein Rechteck über
den Posenstapel — dasselbe Bild, der Weg dorthin ist der von 1.21.

**Runde 134: die Bestrahlungskammer (ExposureChamber).** Der Abnehmer des Beschleunigers, und
damit der Schlussstein: vier Rezepte, und es gibt **keinen anderen Weg** zu Schraranium,
Schrabidium, Euphemium und Dineutronium.

- Higgs auf Uran → Schraranium
- Higgs auf Uran-238 → Schrabidium
- Dunkle Materie auf Plutonium → Euphemium
- Sparkticle auf Schrabidat → Dineutronium

**Eine Kapsel reicht für acht Durchgänge.** Die Kammer zieht sie ein, merkt sich acht Ladungen und
legt die leere Hülle sofort zurück — und sie zieht erst nach, wenn nichts mehr da ist, damit nicht
zwei verschiedene Sorten hineingeraten. Die Fachprüfung arbeitet über Kreuz: was hinein darf,
hängt davon ab, was schon drin liegt. So kann ein Trichter die Kammer nicht verstopfen.

**Abweichungen:** das Original setzt im teuren Modus für Dineutronium *entartete Materie* statt
Schrabidat ein — sie gehört zur Gegenstandsfamilie `item_expensive` des Weltraumbaus, die der Port
nicht hat; hier steht der Schrabidatbarren in beiden Fällen. Das Modell fehlt wie bei den übrigen
Großmaschinen; die Abmessungen stimmen (neun Blöcke lang, fünf hoch, zwei Flügel und ein
Kopfstück).

**Offen, und hier festgehalten statt übersehen:** weder der Beschleuniger noch die Kammer haben
eine JEI-Ansicht. Ihre Rezepte stehen, sind aber im Rezeptbuch nicht nachschlagbar — man muss sie
kennen. Das gehört in eine eigene Runde zusammen mit den übrigen fehlenden Ansichten.

**Runde 135: der Radiothermalgenerator (RadGen) — die letzte offene Maschine.** Er macht aus
Zerfallswärme Strom, ohne Kühlung, ohne Aufwertungen, ohne Schalter: einlegen, Jahre später den
Rest herausholen.

**Zwölf Bahnen, die unabhängig voneinander laufen**, jede mit eigenem Fortschritt und eigener
Leistung. Die Eingabe verteilt sich von selbst: ein Fach nimmt nichts an, solange eine andere
Bahn mit demselben Brennstoff weniger hat — wer einen Stapel hineinschiebt, füllt damit alle
zwölf gleichmäßig.

**Die Leistung ist das Umgekehrte der Laufzeit:** der Edelstein bringt 25.000 HE/t und ist nach
einer halben Stunde durch, langlebiger Abfall 500 über zwei Stunden, Schrott 50 über fünf
Minuten.

**Das Teilsystem, auf das er seit Runde 126 wartete, ist damit da:**

- `NUCLEAR_WASTE_SHORT` / `_TINY` / `_DEPLETED` / `_DEPLETED_TINY` (acht Abfallklassen)
- `NUCLEAR_WASTE_LONG` / `_TINY` / `_DEPLETED` / `_DEPLETED_TINY` (fünf Abfallklassen)
- `SCRAP_NUCLEAR` und `REACTOR_CORE`

Die Abfallklasse steckt als Metawert im Gegenstand, nicht in eigenen Registriernamen — so hält es
auch das Original, mit einer Textur je Gegenstand statt je Klasse. Abfall **verfällt nicht**, wenn
er auf dem Boden liegt; die eigene Entität des Originals, die leuchtet und die Umgebung
verstrahlt, gibt es im Port nicht.

**Gemessen und hier festgehalten: nur EINER der vier Brennstoffe ist im Port wirklich
erreichbar.** Der Schredder macht radioaktiven Schrott aus Betontrümmern (×2) und
Elementtrümmern (×4), und beide wirft die ZIRNOX-Explosion ab — das ist eine geschlossene
Kette. Die beiden Abfallfamilien entstehen im Original in der **SILEX-Anlage**, die der Port nicht
hat; der Edelstein entsteht am **Lemegeton**, den er ebenfalls nicht hat. Beide Brennstoffe stehen
in der Tafel und arbeiten, sind aber bis dahin nur im Schöpferreiter zu bekommen. Das ist eine
Lücke im Weg dorthin, keine in der Maschine.

**Abweichungen:** die sechs Schredderrezepte des Originals auf die Sellafield-Blöcke fehlen — deren
sechsstufige Fassung gibt es im Port nicht, nur die abgeklungene. Das Baurezept nennt statt des
roten Farbstoffs aus dem Erzwörterbuch den roten Farbstoff von Minecraft. Modell und
Energy-Control-Anbindung fehlen wie bei den übrigen Großmaschinen.

**Damit ist keine Maschine mehr offen.**

### Woran jede von ihnen hängt

Erhoben in Runde 120 gegen den Quelltext beider Seiten. Fast keine war „einfach noch nicht dran"
— fast jede wartete auf ein Teilsystem, das der Port nicht hatte.

**Stand Runde 135: jede Zeile dieser Liste ist abgearbeitet.** Sieben Einträge waren bis hierher
nicht durchgestrichen, obwohl ihre Maschine längst stand (AmmoPress, Annihilator, RadarScreen,
SatLink, Siren, SuperComputer, TapeDrive) — gegen `NtmBlocks` geprüft und nachgetragen.

| Maschine | wartet auf |
|---|---|
| ~~AmmoPress~~ (Runde 125) | `GunFactory.EnumAmmo` und `AmmoPressRecipes` (528 Zeilen) |
| ~~Annihilator~~ (Runde 128) | `AnnihilatorSavedData` und `AnnihilatorRecipes` (die ihrerseits `EnumAmmo` und `EnumDriveType` brauchen) |
| ~~Excavator~~ (Runde 131) | `BlockDepth` (Tiefengestein) und `BlockBedrockOreTE` |
| ~~ExposureChamber~~ (Runde 134) | die Teilchen (`particle_higgs`, `particle_dark`, `particle_sparkticle`) — also den Teilchenbeschleuniger, seit Runde 133 vorhanden |
| ~~MissileAssembly~~ (Runde 132) | `MissileStruct` und `ItemCustomMissilePart` |
| ~~OreSlopper~~ (Runde 131) | `ItemBedrockOreNew` (den Gegenstand, nicht nur den Namen) |
| ~~PrecAss~~ (Runde 130) | `PrecAssRecipes` → `EnumOrbitalAssembly`, `BrokenItem`, Legendenteile |
| ~~RadGen~~ (Runde 135) | `ItemWasteShort` und `ItemWasteLong` |
| ~~RadarScreen~~ (Runde 126) | `ItemCoordinateBase` und den Radar-Verbinder; dazu ein Behälter-Menü am Radar (siehe unten) |
| ~~SatDock~~ (Runde 129) | `requestableSlots`/`tryRequestItems` an `SatelliteBase`, die Abwurfkapsel `EntitySatellitePod` — **und** einen Satelliten, der Güter erzeugt (`SatelliteMiner`, `SatelliteScience`); ohne den wäre die Station totes Werk |
| ~~SatLink~~ (Runde 122) | `getInfo(Level)` an `SatelliteBase` und den zehn Satelliten des Ports |
| ~~Siren~~ (Runde 127) | `ItemCassette` samt Tonspuren |
| ~~SuperComputer~~ (Runde 124) | `ModuleMachineSuperComputer`, `SuperComputerRecipes`, `ItemDrive.EnumDriveType` |
| ~~TapeDrive~~ (Runde 125) | `ItemDrive.EnumDriveType` |

**NEBENBEFUND, Runde 120:** das Radar des Ports öffnet einen reinen `Screen` (die Karte), keinen
Behälter — seine zehn Fächer sind deshalb nur über Trichter erreichbar, Batterie und
Radar-Verbinder eingeschlossen. Das Original hat dort einen `ContainerMachineRadar`. Solange das
so bleibt, wäre der Radarschirm nur mit Trichtern zu verbinden; das Behälter-Menü gehört deshalb
vor ihn.

**Ebenfalls in Runde 120 behoben:** das Radar stand seit seiner Portierung in keiner
Schöpferrunde, hatte keinen Namen (es zeigte den rohen Schlüssel) und kein Rezept. Jetzt hat es
alle drei, und das große gleich mit.

**RICHTIGSTELLUNG, Runde 121:** der Absatz oben liest sich, als sei der Block des großen Radars
in Runde 120 neu entstanden. Das stimmt nicht. Den Block `machine_radar_large` gab es schon —
registriert, mit Beute- und Zustandseintrag —, nur stand dahinter die Klasse des *gewöhnlichen*
Radars. Er war also bloß dem Namen nach groß: gleiche Reichweite, gleicher Block-Entitätstyp.
Neu sind in Runde 120 deshalb das Verhalten (`MachineRadarLargeBlockEntity` mit 3 000 Blöcken
Reichweite und eigenen Anschlusspunkten) und die eigene Blockklasse, nicht der Eintrag in der
Registrierung. Genau dieses Übersehen hat Runde 120 rot gemacht: ein zweites Feld
`MACHINE_RADAR_LARGE` neben dem, das schon da war.

**Ein Loch im Torwächter geschlossen (Runde 121):** dass fünf Torwächter grün meldeten und der
Bau trotzdem rot wurde, hat einen nachweisbaren Grund. javacs `Check.checkUnique` springt
vorzeitig zurück, sobald der **Typ** des doppelt erklärten Feldes fehlerhaft ist. Gemessen an
einem Minimalbeispiel: zwei Felder `public static final String BAR` werden gemeldet, zwei Felder
`public static final some.missing.Type FOO` nicht. Da offline die gesamte Minecraft-/NeoForge-API
fehlt, hat *jedes* Registrierungsfeld des Ports einen solchen Typ (`DeferredBlock`,
`DeferredItem`, …) — javac schweigt also genau dort, wo ein Duplikat weh tut. Kein
javac-gestützter Torwächter kann das je finden. `syntax-check.sh` hat deshalb einen zweiten,
rein textlichen Durchgang bekommen: doppelt erklärte Konstanten im Rumpf einer äußeren Klasse
(genau vier Leerzeichen Einzug; verschachtelte Klassen bleiben aussen vor, dort wäre derselbe
Name erlaubt). Gemessen: über den ganzen Baum null Funde, mit dem Fehler aus Runde 120 wieder
eingesetzt genau ein Fund, der Datei, Name und beide Zeilen benennt.

## Gemessener Maschinenstand (Runde 135) — der Abschluss

Dasselbe Verfahren wie nach Runde 112 und 120, unverändert, damit die Zahlen vergleichbar
bleiben: gezählt werden die Klassen `src/main/java/com/hbm/tileentity/machine/TileEntityMachine*.java`
des Originals **ohne** Unterordner; als vorhanden gilt eine Maschine, wenn es im Port eine Klasse
`<Name>BlockEntity` oder `Machine<Name>BlockEntity` gibt, gleich in welchem Ordner.

| | |
|---|---:|
| Maschinen im Original | 68 |
| davon im Port | **63** |
| das Verfahren meldet als fehlend | 5 |
| davon begründet verworfen | 3 |
| davon Falschmeldungen des Verfahrens | 2 |
| **der Sache nach fehlen** | **0** |

Die fünf Meldungen sind namentlich dieselben, die schon in Runde 120 einzeln nachgewiesen wurden,
und keine neue:

- **begründet verworfen:** `IGenerator` und `LPW2` (im Original tot — `updateEntity` ist leer),
  `LargeTurbine` (im Original ausdrücklich als veraltet gekennzeichnet, ohne Rezept).
- **Falschmeldungen des Verfahrens:** `CompressorBase` steht im Port in
  `MachineCompressorBlockEntity` (die abstrakte Basis ist dort hineingefaltet), `RadarNT` heißt
  im Port `MachineRadarBlockEntity`. Beide Dateien nachgeprüft, sie sind da.

Dazugekommen seit Runde 120: SatLink (122), ItemDrive/TapeDrive (123), SuperComputer (124),
AmmoPress (125), RadarScreen (126), Siren (127), Annihilator (128), SatDock (129), PrecAss (130),
OreSlopper und Excavator (131), MissileAssembly (132), Teilchenbeschleuniger (133),
ExposureChamber (134), RadGen (135).

**Damit ist Stufe 4 abgeschlossen.** Als Nächstes die Bauwerke (Stufe 5): 57 der 115
Bauwerksblöcke fehlen noch, und platziert wird bis heute keines der 79.

**Offen geblieben und hier festgehalten, damit es nicht untergeht:**

- Weder der Teilchenbeschleuniger noch die Bestrahlungskammer haben eine JEI-Ansicht.
- Die beiden Abfallfamilien des Radiothermalgenerators und der radioaktive Edelstein haben im
  Port keinen Erzeuger (SILEX bzw. Lemegeton fehlen).
- 107 Tonereignisse sind stumm, obwohl ihre `.ogg` vorliegt (`tools/sound-baseline.txt`).

## Ein teurer Befund: die Tore ersetzen CI nicht

Nach Runde 135 stellte sich heraus, dass **der Bau seit mindestens Runde 128 abbricht** — 38
Compile-Fehler in acht Dateien, verteilt über die Runden 128, 129, 131 und 132. In derselben Zeit
meldeten alle sieben Tore grün, und ich habe das in jeder Commit-Nachricht so geschrieben.

**Beides stimmte.** Die Tore prüfen, was sich ohne Minecraft-Klassenpfad prüfen lässt; sie sind
Ersatz für einen Compiler, den es hier nicht gibt. Genau die Fehlerklasse, die sie nicht sehen
können, ist diesmal aufgelaufen:

| Fehler | warum kein Tor ihn sieht |
|---|---|
| `label(...)` gab `Component` statt `MutableComponent` zurück | Rückgabetyp einer Minecraft-Klasse |
| `import …CustomMissilePartItem.Rarity` verdeckte Minecrafts `Rarity` | der verdeckte Typ ist offline ein Fehlertyp, javac schweigt |
| `setCreativeTab(null)`, `MovingItem(Level)`, `KEY_RADIUS` | Methode/Erbauer/Konstante existiert nicht |
| `FluidType.getName()` gibt `Component`, gebraucht war `getUnlocalizedName()` | Rückgabetyp |
| `TIER10`…`TIER20` standen auskommentiert | javac bricht die Attribuierung nach dem ersten Fehlertyp ab |
| `constructionRecipes` statt `CONSTRUCTION_RECIPES` | ein Tippfehler in einem Ausdruck, der ohnehin nicht auflöst |

**Die Schlussfolgerung ist ausdrücklich NICHT „ein achtes Tor".** Ich habe geprüft, ob sich die
teuerste dieser Ursachen — der verdeckende Import — offline erkennen ließe: nein. javac müsste
dafür beide Typen auflösen können, und der verdeckte ist genau der, den es nicht kennt. Ein Tor,
das hier rät, wäre ein Tor, das nicht nachweislich wirkt — und davon gibt es in diesem Projekt
keines.

**Die Schlussfolgerung ist: nach jeder Runde den CI-Lauf nachsehen, bevor die nächste anfängt.**
Die Tore sagen „nichts offensichtlich kaputt", nicht „es baut". Nur der Lauf sagt das, und nur er
liefert die .jar.

## Der erste Start: ein Absturz aus Runde 14

Die erste .jar seit Runde 128 lief nicht an. Der Grund lag weder in den letzten Runden noch an
den 38 Compile-Fehlern, sondern seit **Runde 14** im Baum:

```
NullPointerException: name == null
  at com.google.gson.stream.JsonWriter.name(JsonWriter.java:388)
  at com.hbm.inventory.fluid.Fluids.writeDefaultTraits(Fluids.java:832)
  at com.hbm.inventory.fluid.Fluids.init(Fluids.java:758)
  at com.hbm.main.NuclearTechMod.<init>(NuclearTechMod.java:55)
```

`FT_Toxin` war in Runde 14 vollständig portiert — Klasse, `serializeJSON`, `deserializeJSON` —,
aber seine Zeile in `FluidTrait` stand **auskommentiert** da. Vier Fluide tragen ihn (Chlor,
Phosgen, Senfgas, Rotschlamm). `writeDefaultTraits` schlägt jeden Trait in `traitNameMap` nach;
für einen nicht eingetragenen kommt `null` zurück, und `JsonWriter.name(null)` wirft.

**Das traf jede frische Installation**, denn die Vorlage wird nur geschrieben, wenn
`hbmFluidTraits.json` noch nicht existiert — also genau beim ersten Start.

Drei Dinge daraus:

1. **Registriert.** Die Zeile steht wieder da, mit der Begründung daneben. Nebenbei sind zwei
   doppelte Einträge (`heatable`, `coolable` standen zweimal) verschwunden.
2. **Gehärtet.** `writeDefaultTraits` überspringt einen unbekannten Trait jetzt und schreibt in
   das Protokoll, welche Klasse nachzutragen ist, statt den ganzen Mod mitzureißen. Aus
   „NullPointerException: name == null" wird ein Satz, der die Ursache benennt.
3. **Ein achtes Tor, und diesmal eines, das messbar wirkt.** `tools/trait-check.sh` vergleicht die
   in `Fluids` benutzten Trait-Klassen mit den in `FluidTrait` registrierten. Gemessen: über den
   ganzen Baum null Funde; mit der Registrierung wieder auskommentiert genau ein Fund, der die
   Klasse, die auskommentierte Zeile und die vier betroffenen Fluide benennt. Es läuft ab jetzt
   auch in CI mit.

Der Unterschied zum verdeckenden Import aus dem Abschnitt davor ist der Grund, warum es hier ein
Tor gibt und dort keines: **diese Lücke ist rein textlich entscheidbar.** Beide Listen stehen im
Projekt, keine Minecraft-Klasse ist daran beteiligt.

## Der zweite Start: camelCase in einer ResourceLocation

Mit registriertem `FT_Toxin` kam der Mod weiter — und fiel an der nächsten Stelle um:

```
ResourceLocationException: Non [a-z0-9/._-] character in path of location: hbmsntm:alarm.airRaid
  at net.minecraft.resources.ResourceLocation.assertValidPath
  at net.neoforged.neoforge.registries.DeferredRegister.register
  at com.hbm.registry.NtmSoundEvents.<clinit>(NtmSoundEvents.java:191)
```

Die sechzehn Sirenentonspuren aus Runde 127 trugen die Namen des Originals, also camelCase.
Auf 1.7.10 ging das; auf 1.21 lässt `assertValidPath` im Pfad nur `[a-z0-9/._-]` zu. Betroffen
waren drei Ebenen, alle mit demselben Namen:

- 16 Ereignisse in `NtmSoundEvents`
- 16 Schlüssel und 18 Dateiverweise in `sounds.json`
- 18 `.ogg`-Dateien unter `sounds/alarm/`

Alle drei heißen jetzt durchgehend `snake_case` — `alarm.airRaid` → `alarm.air_raid` —, wie die
übrigen 163 Tonereignisse des Ports ohnehin schon. Wo der Ereignisname vom Dateinamen abweicht,
steht das weiterhin so (`alarm.hatch` liegt als `lpfhaiwg`).

**Warum kein vorhandenes Tor das sah:** für den Compiler ist ein String ein String, und der
`sound-check` prüft die Zuordnung Ereignis ↔ Datei — die war ja stimmig, nur eben falsch
geschrieben. Die Regel steht in Minecraft, nicht im Projekt.

**Das neunte Tor: `tools/location-check.sh`.** Es prüft jeden Namen, aus dem eine
ResourceLocation wird — Tonereignisse, die Registriernamen aller `DeferredRegister`, Schlüssel
und Verweise in `sounds.json`, und die Dateinamen unter `assets/hbmsntm`. Gemessen: 5474 Namen,
null Funde; mit `alarm.airRaid` wieder eingesetzt genau zwei Funde (Java und JSON), jeder mit
Datei, Name und dem störenden Zeichen. Läuft ab jetzt in CI mit.

Auch diese Lücke ist rein textlich entscheidbar — deshalb gibt es dafür ein Tor. Die Reihenfolge
der Befunde ist übrigens kein Zufall: **jeder Absturz beim Start deckt genau einen Fehler auf**,
weil der Mod danach abbricht. Erst der nächste Start zeigt den nächsten.

## Der dritte Start: ein Name, zwei Verzeichnisse

```
IllegalArgumentException: Duplicate registration pwr_fuel
  at net.neoforged.neoforge.registries.DeferredRegister$Items.register
  at com.hbm.blocks.NtmBlocks.<clinit>(NtmBlocks.java:915)
```

Im Original gibt es `pwr_fuel` **zweimal**: als Block (`BlockPillarPWR`, der Brennstoffkanal im
Reaktorbau) und als Gegenstand (`ItemPWRFuel`, der Brennstab). Auf 1.7.10 war das erlaubt, weil
Blöcke und Gegenstände getrennte Verzeichnisse hatten. Auf 1.21 bekommt jeder Block ein
`BlockItem` im **selben** Verzeichnis wie jeder andere Gegenstand — der Name ist dann doppelt
vergeben, und NeoForge bricht beim Start ab.

Der Port hat beide Namen unverändert übernommen. Gemessen: über 1034 Gegenstände und 498 Blöcke
ist das die **einzige** Kollision, und innerhalb der beiden Listen gibt es keine Dopplung.

**Aufgelöst über die Anzeige**, die den Unterschied längst benennt: der Block heißt „PWR Fuel
Channel" (Geschwister: „PWR Control Rod Channel", „PWR Coolant Channel"), das Item „… PWR Fuel
Rod". Der Block heißt jetzt `pwr_fuel_channel`, der Brennstab behält `pwr_fuel` wie im Original
und wie seine Geschwister `pwr_fuel_hot` und `pwr_fuel_depleted`. Das Feld heißt mit, denn
`NtmBlocks.PWR_FUEL` neben `NtmItems.PWR_FUEL` war genau die Verwechslung, die den Fehler
erzeugt hat.

**`location-check.sh` prüft das ab jetzt mit**: doppelte Namen innerhalb einer Liste und
Kollisionen zwischen Blockname und Gegenstandsname. Gemessen: null Funde; mit `pwr_fuel` wieder
eingesetzt genau ein Fund, der die Kollision benennt.

### Die drei Startabstürze im Rückblick

| # | Fehler | aus Runde | warum kein Tor ihn vorher sah |
|---|---|---|---|
| 1 | `FT_Toxin` nicht registriert | 14 | die auskommentierte Zeile ist gültiges Java |
| 2 | `alarm.airRaid` in camelCase | 127 | für den Compiler ist ein String ein String |
| 3 | `pwr_fuel` doppelt vergeben | früh | auf 1.7.10 war es erlaubt |

Alle drei sind **Portierungsfallen**, keine Flüchtigkeitsfehler: jeder war auf 1.7.10 korrekt und
ist es auf 1.21 nicht mehr. Alle drei sind jetzt durch ein Tor abgedeckt, und alle drei Tore sind
gemessen — null Funde sauber, genau ein Fund mit wieder eingesetztem Fehler.

## Der Start lässt sich nicht hier, aber in CI nachstellen

Auf die Frage, ob sich der Client-Start simulieren lässt: **hier nicht.** `maven.neoforged.net`
ist für diese Umgebung gesperrt (nachgeprüft: der Proxy antwortet mit 403), es gibt also keinen
Minecraft-Klassenpfad und damit weder `runClient` noch `runData`. Genau deshalb existieren die
neun textlichen Tore.

**In CI geht es**, und das Projekt hatte die Konfiguration die ganze Zeit: `runs { client,
server, gameTestServer, data }`. Ab jetzt läuft `./gradlew runData` dort vor dem Bau. Das ist der
beste Ersatz für einen Start, den ein Rechner ohne Bildschirm leisten kann:

| Phase | von `runData` durchlaufen? |
|---|---|
| Mod-Konstruktor (`NuclearTechMod.<init>`) | ja — hier lag Absturz 1 (`FT_Toxin`) |
| alle `DeferredRegister` | ja — hier lagen Absturz 2 und 3 (`alarm.airRaid`, `pwr_fuel`) |
| Datengeneratoren (Modelle, Sprache, Beute, Tags) | ja |
| Welt laden, Rendern, Spielen | nein |

**Alle drei Startabstürze wären damit in CI aufgefallen**, ohne dass jemand das Spiel startet.

### Und ein zweiter Befund, beim Nachsehen gefunden

`src/generated/resources` liegt **nicht** im Baum, wird aber von
`sourceSets.main.resources` eingezogen — erzeugt wird es nur von `runData`, und das lief im Bau
nie. Gemessen:

| | |
|---|---:|
| registrierte Blöcke | 498 |
| Blockzustände im Baum | **5** |
| Sprachdatei `lang/` | **fehlt ganz** |

Die .jar war also auch nach den Absturzfixes unvollständig: 493 Blöcke ohne Modell, jeder Name
als roher Schlüssel, keine Beutetabellen. Dass `runData` jetzt vor dem Bau läuft, behebt beides
mit demselben Schritt.

## Der erste `runData`-Lauf: die Registrierung steht

Der erste Lauf mit Datengenerierung in CI kam **durch den Mod-Konstruktor und durch sämtliche
Registrierungen** — die drei Startabstürze sind damit bestätigt behoben, ohne dass jemand das
Spiel starten musste. Er scheiterte erst beim Erzeuger der Gegenstandsmodelle:

```
IllegalArgumentException: Texture hbmsntm:item/rbmk_link does not exist in any known resource pack
  at NtmItemModelProvider.registerModels(NtmItemModelProvider.java:39)
```

`basicItem(NtmItems.X)` nennt keine Textur: der Erzeuger leitet sie aus dem Registriernamen ab
und sucht `item/<name>.png`. Der `asset-check` findet das nicht — er prüft Referenzen, die im
Quelltext **ausgeschrieben** stehen, und hier steht keine.

Statt auf den nächsten Lauf zu warten, ließ sich die Frage lokal vollständig beantworten:
**784 `basicItem`-Aufrufe gegen 1546 vorhandene Texturen, genau eine Lücke** — `rbmk_link`, seit
seiner Portierung ohne Bild. Die Textur heißt im Original `rbmk_tool` und ist jetzt unter dem
Namen des Ports abgelegt.

**Das zehnte Tor, `tools/model-check.sh`**, schließt diese Klasse: es löst für jeden
`basicItem`-Aufruf den Registriernamen auf (über alle drei Wege — `ITEMS.register`,
Hilfsfunktionen wie `registerNugget`, und die Waffenfabrik) und prüft, ob die Datei da ist.
Gemessen: null Funde; mit gelöschter `rbmk_link.png` genau ein Fund.

### Nebenbefund: zwei Blöcke ohne Modell

Beim Abgleich der 498 registrierten Blöcke gegen den Blockzustandsgeber fielen
`RADIO_TORCH_SENDER` und `RADIO_TORCH_RECEIVER` auf: Beutetabelle ja, Rezepte ja, **Modell nein**
— im Spiel wären sie der schwarz-violette Ersatzwürfel gewesen. Beide haben jetzt eines. Die
übrigen drei ohne Eintrag (`FOUNDRY_*`) haben handgeschriebene Blockzustände und sind in Ordnung.

*Abweichung:* das Original hat für die Funkfackeln je ein Bild für an und aus. Der Block des
Ports führt nur `FACING` und keinen Leuchtzustand, deshalb steht dort das Bild für „aus".

## Der zweite bis fünfte Datenlauf: was zwei Quellverzeichnisse anrichten

Die folgenden Läufe brachten eine Reihe, die sich rückblickend als **eine einzige Ursache** lesen
lässt: `runData` schreibt nach `src/generated/resources`, und dieses Verzeichnis war bis dahin
leer. Alles, was der Datengenerator erzeugt, lag zugleich handgeschrieben unter
`src/main/resources` — und beide Verzeichnisse sind Quellen desselben Ressourcenpfads.

| Lauf | Abbruch bei | Ursache |
|---|---|---|
| 2 | `Texture hbmsntm:item/reinforced_glass_pane` | `basicItem` steht auch im **Block**erzeuger |
| 3 | `Texture hbmsntm:block/struct_icf` | Blocktexturen werden ebenso still abgeleitet |
| 4 | `Cannot set models for a state ...` | die PWR-Steuerung war **zweimal** beschrieben |

### Was das Modell-Tor nicht sah

Das zehnte Tor prüfte nur `NtmItemModelProvider`. `basicItem` steht aber auch im Blockerzeuger —
für Blöcke, deren Gegenstandsform ein flaches Bild bekommt (Scheibe, Stahlleiter, Verlieskette,
Schmalspurschiene), teils hinter einer Hilfsfunktion, die den Block als Parameter nimmt. Alle vier
haben in 1.7.10 nur eine Blocktextur; die Gegenstandsform wurde dort flach aus ihr gezeichnet, also
übernimmt der Port genau diese vier Bilder in den `item`-Ordner.

Dieselbe Blindheit auf der Blockseite: `simpleCubeAllBlock` und Geschwister leiten
`block/<name>.png` ab, teils mit Endungen `_side`, `_bottom`, `_top`. Dort fehlte `struct_icf` —
der Block heißt upstream `struct_icf_core` und trägt dort die Textur gleichen Namens; der Port hat
den Block übernommen, das Bild nicht.

Und ein drittes Mal: `EnumMultiItem` mit `multiTexture` leitet **pro Aufzählungswert** eine Textur
ab. Die erste Messung meldete dort 27 Fehlstellen — alle falsch: `ConserveItem` überschreibt
`registerItemModel` und zeigt auf `canned_<wert>`, nicht auf `canned_conserve.<wert>`. Das Tor liest
die Vorlage jetzt aus dem Quelltext, statt sie zu raten. Beim Bauen fiel außerdem auf, dass sein
Feldmuster **mindestens drei Zeichen** verlangte: `C4` war unsichtbar.

Das Tor deckt jetzt 798 `basicItem`-Aufrufe, 216 abgeleitete Meta- und 322 abgeleitete
Blocktexturen ab. Jede der drei Formen ist einzeln gemessen: null Funde sauber, genau ein Fund je
entfernter Datei.

### 149 Dateien lagen doppelt

Der Reihe nach durchgezählt, was der Erzeuger schreibt und was daneben handgeschrieben liegt:

| Art | doppelt | bleibt |
|---|---|---|
| `sounds.json` | 1 | – |
| Beutetabellen | 90 | `c4`, `taint` → beide ebenfalls überholt, siehe unten |
| Rezepte + Fortschritte | 29 | – |
| Tag-Dateien | 19 | `actually_stone`, `ground`, `plants`, `no_impact` |
| Schadensarten | 21 | – |
| Modelle und Blockzustände | 7 | die Gießerei |
| Weltgenerierung | 0 | alle 90 |

Jede einzelne ist vor dem Löschen geprüft worden. Die Beutetabellen etwa: 14 der 94
handgeschriebenen tragen Sonderverhalten (Stufen, Schnee statt Holz, Scheren, Erzbonus) — der
Erzeuger bildet alle 14 nach. `c4.json` war beim ersten Durchgang nur deshalb stehen geblieben,
weil das Feld `C4` durch dasselbe Dreizeichen-Muster fiel; `taint.json` ist wirkungslos, denn der
Block trägt `noLootTable()`.

### Das Ton-Tor maß die falsche Datei

Der auffälligste Fund: die handgeschriebene `sounds.json` führte 72 Ereignisse, der Erzeuger 160.
Ihre 53 gemeinsamen Einträge stimmen Datei für Datei überein; die 19 Sirenen der Runde 127 gab es
nur in der Handdatei. Damit war auch die **Baseline der 107 stummen Ereignisse falsch**: sie maß
die Handdatei, während der Erzeuger sie längst abdeckte.

Die 19 Sirenen stehen jetzt im Erzeuger, die Handdatei ist fort, und alle **179 registrierten
Ereignisse** haben einen Eintrag. `tools/sound-baseline.txt` entfällt. Das Tor liest jetzt den
Erzeuger und streicht vorher die Kommentare weg — ein auskommentierter Eintrag ist gültiges Java
und sah bisher aus wie ein vorhandener. Genau so hatte sich `FT_Toxin` drei Runden lang versteckt.

### Das elfte Tor: `state-check`

Der vierte Abbruch war die PWR-Steuerung: `simpleBlockWithItem` setzt bereits eine Variante, und
der folgende `getVariantBuilder` beschrieb denselben Block ein zweites Mal. Das kostet einen vollen
CI-Lauf, denn es zeigt sich erst, wenn die Datengenerierung an dieser Stelle ankommt.

`tools/state-check.sh` misst beide Richtungen, und zwar bewusst unterschiedlich scharf:

* **Doppelte Beschreibung** streng, über eine Liste zustandsdefinierender Aufrufe. Nur so fällt der
  Absturz auf.
* **Gar keine Beschreibung** grob — gefragt wird nur, ob der Block im Erzeuger überhaupt vorkommt,
  eine eigene Modellanmeldung trägt oder eine handgeschriebene Datei hat. Eine strenge Zählung
  meldete hier 115 Blöcke, die der Erzeuger in eigenen Methoden über Zwischenvariablen beschreibt.

Gefunden hat die grobe Hälfte sofort etwas: **die vier Flüssigkeitsblöcke** (Corium, Schlamm,
Radiolava, vulkanische Lava) standen in keiner Blockstate. Ihr Aussehen zeichnet der
Fluid-Renderer, aber die Partikeltextur holt sich das Spiel aus der Blockstate — ohne sie wäre dort
das schwarz-violette Ersatzmuster gestoben.

### Was daraus folgt

`src/generated/resources` ist **nicht versioniert**. Eine .jar ohne vorherigen `runData`-Lauf ist
damit unvollständig — es fehlen Blockzustände, Modelle, Sprachdatei, Beutetabellen, Rezepte und die
Tonliste. `docs/BUILDING.md` sagt das jetzt deutlich.

### Lauf sechs und sieben: die Sprachdatei und die Beutetabellen

Nach den Blockzuständen kamen die nächsten beiden Stufen dran, und jede brachte einen Fehler, den
kein bisheriges Tor sehen konnte.

**`Duplicate translation key item.hbmsntm.wiring_tool.desc`.** Die beiden Zeilen sahen nicht gleich
aus — einmal aus dem Gegenstand plus Endung gebaut, einmal ausgeschrieben:

```java
this.add(NtmItems.WIRING_TOOL, DESC, "Right-click a pylon to memorise it,$then ...");
this.add("item.hbmsntm.wiring_tool.desc", "Right-click two pylons to connect them.");
```

Geblieben ist die zweite. Die erste trug `$` als Zeilentrenner, und den setzt nur
`ITooltipProvider` um — das Kabelwerkzeug baut seinen Hinweis aber direkt aus
`Component.translatable`, dort wäre das Zeichen stehen geblieben.

**Das zwölfte Tor, `lang-check`**, löst alle Schreibweisen auf denselben Schlüssel auf: den
ausgeschriebenen, den aus Gegenstand oder Block gebauten, die Endungen (`DESC`, `P11`, ein Literal,
`getName(Wert)` oder eine Summe daraus) und `addDamage`. Auch Schleifen über Aufzählungen: `type.key`
wird aus der Aufzählung selbst gelesen, eine Zeile vergibt dort so viele Schlüssel wie es Werte gibt.
Gemessen: **3324 Schlüssel, keine Dopplung, keine blinde Stelle.**

**`Created block loot tables for non-blocks: [minecraft:empty]`.** Die Barrikade trägt
`noLootTable()` — ihr Tabellenschlüssel ist damit der leere von Minecraft — und der Erzeuger legte
ihr zusätzlich eine leere Tabelle an. Die blieb übrig, weil kein Block sie abholt. Im Original ist
sie ein `BlockNoDrop`; die Eigenschaft am Block genügt, die Zeile im Erzeuger ist fort.

**Das dreizehnte Tor, `loot-check`**, prüft beide Richtungen: kein Block ohne Tabelle *und* ohne
`noLootTable()` (sonst `Missing loottable`), und kein Block mit beidem. Gezählt wird nur die erste
Stelle eines Aufrufs — sonst zählte ein Block mit, der bloß als Beute eines anderen vorkommt.
Gemessen: **608 Blöcke, 589 Tabellen, 19 mit `noLootTable()`, null Funde.**

### Lauf acht: ein Dreisatz in einer Zeichenkette

```
ResourceLocationException: Non [a-z0-9/._-] character in path of location:
hbmsntm:ingot_compat.is_mod_loaded(_compat._mod__gt6
```

Im Original heißt das Uran-Material `Uraninite`, sobald GregTech 6 geladen ist, sonst `Uranium`:

```java
public static final DictFrame U = new DictFrame(Compat.isModLoaded(Compat.MOD_GT6) ? "Uraninite" : "Uranium");
```

Bei der Portierung ist der ganze Dreisatz **in die Zeichenkette gerutscht** — `df("Compat.isModLoaded(Compat.MOD_GT6")`. GregTech 6 gibt es für 1.21 nicht, also steht dort jetzt schlicht `df("Uranium")`.

Der Name entsteht erst zur Laufzeit (`toTagName` macht aus camelCase snake_case) und stand deshalb
nirgends als fertiger Pfad im Quelltext — genau die Lücke, durch die er acht Runden lang gefallen
ist. Das Ortungs-Tor wendet die Umformung jetzt selbst an und prüft das Ergebnis; dazu nimmt es
jede ausgeschriebene Kennung aus `withDefaultNamespace("...")` mit. Von 5474 auf **6439 geprüfte
Namen**.

### Zwei Befunde nebenbei, beide ohne Absturz

Die Suche nach fehlenden Namen und Modellen hat zwei stille Lücken gehoben:

* **28 Blöcke und Gegenstände ohne Namen** — neun Erze, der Schrottblock, der ZIRNOX samt Ruine,
  die beiden Funkfackeln, elf ZIRNOX-Stäbe, Bergbauhelm, Plan C, Kassette und Fluid-Sinnbild. Im
  Spiel hätte dort der rohe Schlüssel gestanden.
* **Drei Gegenstände ganz ohne Modell** — Blaupausen, Bergbauhelm, Plan C. Im Spiel der
  schwarz-violette Würfel.

Beides prüfen die Tore jetzt mit, und beide Male leiten sie die zulässigen Ausnahmen aus dem
Quelltext her statt aus einer gepflegten Liste: `multiName` hängt den Aufzählungswert an, eine
Klasse mit eigenem `getDescriptionId` oder eigener Modellanmeldung bestimmt ihren Schlüssel selbst.

### Lauf neun: die Schadensart-Tags sahen ihre eigenen Schadensarten nicht

```
IllegalArgumentException: Couldn't define tag minecraft:is_explosion as it is missing
following references: hbmsntm:nuclear_blast
```

Schadensarten, Biome und Vorkommen stehen nicht in einer Registry des Codes, sondern im
**Datapack**, das der `DatapackBuiltinEntriesProvider` erzeugt. Die Nachschlagetabelle aus dem
`GatherDataEvent` kennt sie deshalb nicht — nur die des Datapack-Erzeugers
(`getRegistryProvider()`) tut das. Der Tag-Erzeuger bekam die falsche.

**Das vierzehnte Tor, `datagen-check`**, liest aus `NtmDataGenerators`, welche Klassen das
Datapack füllen (`builder.add(..., X::bootstrap)`), sucht die Erzeuger, die deren Einträge nennen,
und prüft, mit welcher Tabelle sie gebaut werden. Gemessen: ein solcher Erzeuger, null Funde; mit
der alten Tabelle genau ein Fund.

## Grün: der erste vollständige Lauf

Lauf zehn ist durchgelaufen — **alle vierzehn Tore, `runData`, die Doppelpfad-Prüfung und
`./gradlew build`**. Das Artefakt `hbmsntm` liegt mit 25,4 MB im Lauf 35019529242.

Was damit belegt ist, und was nicht:

| belegt | nicht belegt |
|---|---|
| Der Mod-Konstruktor läuft durch | Dass im Spiel etwas richtig **aussieht** |
| Jede Registrierung greift — Blöcke, Gegenstände, Fluide, Tonereignisse, Entitäten, Menüs | Dass die Blockentitäten-Renderer zeichnen |
| Jeder Datenerzeuger schreibt seine Dateien | Dass Maschinen **rechnen** wie das Original |
| Blockzustände, Modelle, Sprachdatei, Beutetabellen, Rezepte, Tags sind vollständig erzeugt | Dass eine Welt lädt (`runServer` steht noch aus) |
| Keine Datei liegt doppelt vor | |
| Die .jar lässt sich packen | |

Zehn Läufe, zehn Ursachen — und keine davon hätte ein Übersetzungsfehler sein können, denn
übersetzt hat es die ganze Zeit. Das ist die Lehre aus dieser Runde: **„es baut" und „es läuft"
sind zwei verschiedene Aussagen**, und zwischen ihnen lagen hier zehn Abbrüche.

Die Tore sind von zehn auf vierzehn gewachsen; jedes einzelne ist an einem echten Absturz
gemessen worden, nicht an einer Vermutung.

## Der Server läuft

`runServer` gehört jetzt zur Kette, und der dritte Anlauf ist durchgekommen:

```
[21:35:23] [Server thread/INFO] [minecraft/DedicatedServer]: Done (15.548s)! For help, type "help"
[21:38:39] [Server thread/INFO] [minecraft/MinecraftServer]: Stopping the server
[21:38:40] ThreadedAnvilChunkStorage: All dimensions are saved
OK - Welt geladen, Server sauber beendet.
```

Der Server hat eine Welt **erzeugt** (samt der 30 Erzvorkommen aus den handgeschriebenen
Weltgenerations-Dateien), sie drei Minuten lang **getickt** und sich sauber beendet. Damit ist
zum ersten Mal auch `FMLCommonSetupEvent` gelaufen — der ganze Block mit Fluid-Neuladen,
Rezept-Serialisierern, `FalloutConfigJSON`, Gefahren- und Hazmat-Registern, Geschütz-Munition
und der Brennstofftafel des Radiothermalgenerators. Die Datengenerierung führt ihn **nicht** aus;
er war bis hierher der größte ungetestete Block des Ports.

### Der Mod lief auf keinem Server — 65 Methoden

```
RuntimeException: Attempted to load class net/minecraft/client/player/LocalPlayer
for invalid dist DEDICATED_SERVER
  at com.hbm.blocks.NtmBlocks.lambda$static$486(NtmBlocks.java:801)
```

Ein dedizierter Server hat die Clientklassen nicht, und **es reicht, dass eine Methode da ist** —
ausgeführt werden muss sie nicht. Die Prüfung der Klasse lädt die Typen ihrer Zuweisungen, und
über `Player player = Minecraft.getInstance().player` fällt der Dist-Cleaner. Es traf den
Umbaublock und das Waffen-Grundstück, und beide rissen die ganze Registrierung mit.

Nachgemessen: **65 Methoden in 63 Dateien.** 40 × `printHook` der Blockeinblendung, zehnmal die
Modellanmeldung der Datengeneratoren, drei Fusions-Blockentitäten, drei Pakete an den Client,
Rüstung, Werkzeugtafel, Einzelfälle. Alle tragen jetzt `@OnlyIn(Dist.CLIENT)`.

**Eine Stelle war ausdrücklich kein Fall dafür:** `Library.getChunkForBlockTrace` läuft auf beiden
Seiten — der Strahlengang wird auf dem Server genauso gebraucht wie beim Zeichnen. Gekennzeichnet
wäre sie dort verschwunden und beim ersten Schuss mit `NoSuchMethodError` aufgeschlagen. Dort ist
stattdessen die Clientklasse aus dem Rumpf verschwunden; `getChunk(...)` steht schon in
`ChunkSource`.

### Und einer, bei dem die Kennzeichnung selbst das Problem war

```
NoSuchMethodError: 'void VanillaExplosionLike.handleClient(VanillaExplosionLike, IPayloadContext)'
  at NtmNetwork.registerPackets(NtmNetwork.java:25)
```

`NtmNetwork` verweist mit einer **Methodenreferenz** auf `handleClient`, und die löst auch der
Server auf — er muss die Pakete kennen, um sie senden zu können. Die gekennzeichnete Methode war
dort aber entfernt. Die drei Pakete behalten sie deshalb und rufen nur noch weiter: was der Client
tut, steht in `ClientPacketEffects`. Der Aufruf steht in einem Lambda und nennt ausschließlich
gemeinsame Typen, also braucht die Prüfung die Clientklasse nicht zu laden — geladen wird sie
erst, wenn jemand das Lambda ausführt.

**Das fünfzehnte Tor, `dist-check`**, hält die Regel fest. Seine beiden Ausnahmen sind hergeleitet
statt gepflegt: Klassen aus dem `client`-Abschnitt der Mixin-Datei, und Klassen, die außer sich
selbst nur in Clientdateien vorkommen. Gemessen: 944 angesehene Methoden, null Funde; mit einer
entfernten Kennzeichnung genau ein Fund.

### Was jetzt belegt ist

| belegt | nicht belegt |
|---|---|
| Mod-Konstruktor, alle Registrierungen, alle Datenerzeuger | dass im Spiel etwas richtig **aussieht** |
| `commonSetup` — Rezepte, Register, Konfigurationen | dass die Blockentitäten-Renderer zeichnen |
| Eine Welt entsteht, wird getickt und gespeichert | dass Maschinen **rechnen** wie das Original |
| Die Erzvorkommen des Ports erzeugen sich | dass der Client startet (kein Bildschirm in CI) |
| Die .jar lässt sich packen | |

Elf Läufe, elf Ursachen — und keine davon hätte ein Übersetzungsfehler sein können.

## Stufe 6 — das Ziel: Schutzkleidung, Erdöl, Lagerung, RBMK-Reste, Einzelstücke, Welt

> **Ziel dieser Stufe:** Schutzkleidung, Erdölkette, Lagerung, RBMK-Reste, Einzelstücke und
> Weltgenerierung zu Ende portieren.

Nicht in dieser Stufe, damit die Abgrenzung festgehalten ist: Geschütze und Bomben, der
Dunkelfusionskern, Drohnen, Rohrpost, Deko und die 142 Entitätsklassen. Die bleiben für später.

### Der gemessene Ausgangsstand

Die Zahlen rechnet [`tools/port-gap.py`](../tools/port-gap.py) jedes Mal neu aus, die Bauwerke
[`tools/structure-gap.py`](../tools/structure-gap.py). Abgeschriebene Zahlen veralten —
genau das ist dem Maschinenstand passiert (siehe die Berichtigung unten).

| Klassenvergleich — belastbar | Original | Port | fehlend |
|---|---:|---:|---:|
| Blockentitäten (`TileEntity*`) | 370 | 269 | **153** |
| Entitätsklassen | 185 | 51 | **142** |
| Rüstungsklassen | 83 | 4 | **80** |
| Weltgenerierungsklassen | 86 | 14 | **84** |

| Namensvergleich — nur eine obere Schranke | Original | Port | höchstens |
|---|---:|---:|---:|
| Blocknamen | 977 | 608 | 489 |
| Gegenstandsnamen | 1597 | 1169 | 619 |

Die Namenszahlen sind **keine Ist-Werte**. Der Port faltet zusammen, was das Original über
Metadaten trennt: aus sechzehn Metadaten-Werten wird eine Blockzustands-Eigenschaft, aus einem
Metadaten-Gegenstand ein `EnumMultiItem`. `can_smart` heißt hier `drink.smart` und zählt im rohen
Vergleich als fehlend, obwohl es da ist.

**Berichtigung zum Maschinenstand der Runde 135.** Dort standen „68 Maschinen, davon der Sache
nach 0 fehlend". Das galt für eine *enge* Zählung — `TileEntityMachine*.java` direkt im Ordner
`/machine/`, ohne Unterordner. Die breite Aufnahme zählt 370 Blockentitäten, davon 153 ohne
Entsprechung. Beide Zahlen sind richtig, sie messen nur nicht dasselbe. Für die Planung zählt die
breite.

### 1. Schutzkleidung — 80 Klassen

Die größte inhaltliche Lücke, und für einen Mod, dessen Kern Strahlung ist, die wichtigste: **im
Port gibt es keinen einzigen Schutzanzug.** Vorhanden sind nur `ArmorNo9`, `ItemArmorMod`,
`ItemModCladding` und `ModCharmItem`.

Die 80 Klassen zerfallen in drei Gruppen:

* **Anzüge (~30)** — Hazmat und Hazmat-Maske, Liquidator, Envsuit, Gasmaske, Asbest, HEV, T51,
  Desh, Bismut, Euphemium, Digamma, AJR, FSB in drei Spielarten, RPA und NCRPA je mit Nah- und
  Fernkampfvariante, Taurun, Trenchmaster, Maske der Schande.
* **Rüstungsmodule (~35 `ItemMod*`)** — das Baukastensystem: Batterie, Gasmaske, Nachtsicht,
  Servos, Schild, Tesla, Lodestone, Serum, Pads und so fort.
* **Jetpacks und Schnittstellen (~15)** — `JetpackBase` und vier Spielarten, dazu
  `IArmorDisableModel`, `IAttackHandler`, `IDamageHandler`, `IPAMelee`, `IPARanged`.

Das Gerüst steht schon: `ArmorRegistry`, `HazmatRegistry`, `ArmorUtil` und `ItemModCladding`
laufen im Port und werden in `commonSetup` aufgerufen. Was fehlt, sind die Träger.

**Vorschlag für die Reihenfolge:** erst Hazmat und Gasmaske (sie schließen den Kreis mit dem
Strahlungssystem, das längst läuft), dann das Modulsystem, dann die Kraftanzüge mit ihren
Schnittstellen, zuletzt die Jetpacks.

### 2. Erdölkette — 11 Blockentitäten

`MachineCatalyticCracker`, `MachineCatalyticReformer`, `MachineCoker`, `MachineFractionTower`,
`MachineGasFlare`, `MachineHydrotreater`, `MachineLiquefactor`, `MachinePyroOven` und drei
weitere aus `tileentity/machine/oil`.

Die Grundlagen liegen: Fluidsystem, `FluidTrait`, Bohrturm und Pumpe sind portiert, die
Raffinerie selbst nicht. Damit wird aus Rohöl bis heute nichts.

### 3. Lagerung — 10 Blockentitäten

`MachineBigAssTank`, `MachineBAT9000`, `MachineFENSU`, `MachineOrbus`, `MachineUF6Tank`,
`MachinePuF6Tank`, `MassStorage`, `Safe`, `FileCabinet`, `SoyuzCapsule`.

Mechanisch die einfachste der sechs Gruppen: Behälter mit Fassungsvermögen und Oberfläche, ohne
eigene Rechnung. Die beiden Hexafluorid-Tanks hängen an der Gaszentrifuge, die steht.

### 4. RBMK-Reste — 6 Blockentitäten

`RBMKAbsorber`, `RBMKBlank`, `RBMKControlManual`, `RBMKModerator`, `RBMKReflector`,
`RBMKActiveBase`.

Der Reaktor selbst läuft seit Stufe 3; was fehlt, sind die passiven Säulen — die Blöcke sind
angelegt (`rbmk_absorber`, `rbmk_blank`, `rbmk_moderator`, `rbmk_reflector`, `rbmk_control`), ihre
Blockentitäten nicht. Eine Säule ohne Blockentität nimmt am Wärme- und Flussaustausch nicht teil,
der Reaktor rechnet also mit Löchern.

### 5. Einzelstücke — rund 20 Blockentitäten

Was in keine Familie gehört, aber je für sich zählt:

* **SILEX** — schließt die Lücke aus Runde 135: die beiden Abfallfamilien und der radioaktive
  Edelstein haben im Port bis heute keinen Erzeuger.
* **Türen** — `BlastDoor`, `VaultDoorMigration`, `Hatch`.
* **Funk** — `Broadcaster`, `Radiobox`, `RadioRec`, dazu der Telex-Block.
* **Gießerei** — `FoundryOutlet`, `FoundrySlagtap`, `FoundryTank`; Becken, Form und Kanal stehen
  schon.
* **Wärme** — `FireboxBase`, `FurnaceBrick`, `HeatBoilerIndustrial`.
* **Startrampen** — `LaunchpadLambda`, `LaunchpadSoyuz`.
* **Rest** — `Electrolyser`, `Microwave`, `Tesla`, `ForceField`/`FF`, `Charger`,
  `CargoElevator`, `ConveyorPress`, `Refueler`, `StorageDrum`, `Decon`, `ChlorineSeal`,
  `DemonLamp`.

### 6. Weltgenerierung — 84 Klassen, 56 Blöcke, 79 Bauwerke

Die größte und die einzige, die nicht mechanisch ist. Drei Teile:

1. **56 Bauwerksblöcke** (`tools/structure-gap.py --list`) — Kisten und Vorräte, Scheinwerfer in
   vier Spielarten, Stahlbau, Deko-Rechner, Funkgeräte, Amboss aus Blei, Jigsaw- und Loot-Stäbe.
   Mechanisch, aber Voraussetzung für alles Weitere: ohne sie lassen sich die Bauwerke nicht
   setzen.
2. **Die Erzeuger** — 13 Features (`OreLayer`, `OreLayer3D`, `OreCave`, `BiomeCave`,
   `DepthDeposit`, `SchistStratum`, `Geyser`, `HugeMush`, `GlyphidHive`, `Meteorite`, `Dud`),
   drei Biome (`NoMansLand`, `CraterBase`), die Höhlen- und Erdölschichten.
3. **Die Bauwerke** — 13 Verliese (`AncientTomb`, `ArcticVault`, `Silo`, `Spaceship`,
   `LibraryDungeon`, `DesertAtom`, `Ruin`), 16 Räume des Dschungelverlieses, die
   Jigsaw-Maschinerie (`NBTStructure`, `JigsawPiece`, `JigsawPool`, `SpawnCondition`) und die
   Auswahlregeln für Biome und Baustoffe.

**Der Bruch liegt hier, nicht in der Menge.** Das Original erzeugt seine Welt mit
`IWorldGenerator` und `MapGen*`-Klassen, die es auf 1.21 nicht mehr gibt. Die Entsprechung sind
`ConfiguredFeature`/`PlacedFeature` (steht im Port bereits für die Erzadern) und
`Structure`/`StructureTemplate`/`JigsawStructure` für die Bauwerke. Die 79 `.nbt`-Dateien des
Originals liegen in dessen eigenem Format vor — sie müssen gelesen und in Vorlagen für 1.21
umgeschrieben werden, wozu `structure-gap.py` den Leser bereits mitbringt.

**Vorschlag:** erst die 56 Blöcke (mechanisch, gut prüfbar), dann die Features (sie brauchen nur
Blöcke und haben im Port schon ein Vorbild), zuletzt die Bauwerke mitsamt Umschreiber.

### Wie jede Runde abläuft

Unverändert, und seit dem Serverlauf um eine Stufe reicher:

1. Original lesen, portieren, Abweichungen im Quelltext begründen.
2. Die fünfzehn Tore lokal grün.
3. CI: Tore → `runData` → Doppelpfadprüfung → `build` → **dedizierter Server**.
4. Erst dann gilt die Runde als fertig — „es baut" und „es läuft" sind zwei Aussagen.

---

## Stufe 6 — Runde 136: Hazmat-Familie, Gasmasken und Filter

Der erste Schritt der Schutzkleidung, und zugleich der, der am meisten geschlossen hat.

**Ausgangslage.** Der Gefahren-Unterbau stand vollständig: `ArmorRegistry` mit neun
Gefahrenklassen, `ArmorUtil` mit dem ganzen Filter-Schriftverkehr, `HazmatRegistry` mit der
Strahlungsminderung, und als Verbraucher `HazardTypeCoal`, `HazardTypeAsbestos`,
`HazardTypeBlinding`, die acht Gasblöcke und `FT_Toxin`. Nur schützte nichts davon irgendetwas:
`ArmorUtil.register()` hat genau eine Zeile gehabt, und die hat dem **Diamanthelm** zwei
Gefahrenklassen angehängt — ein Platzhalter aus der Zeit, als es noch keine Masken gab.

**Was dazugekommen ist.**

| | |
|---|---|
| `NtmArmorMaterials` | Hazmat gelb/rot/grau, Hazmat-PAA, Lappen, Maske |
| `GasMaskItem` | fasst `ArmorGasMask`, `ArmorHazmat` und `ArmorHazmatMask` zusammen |
| `FilterItem` | Filtereinsatz, einschraubbar auch in eine Maske im Helmaufsatz |
| `RagItem` | Lappen, nasser Lappen, angepinkelter Lappen |
| `IHelmetOverlayItem` | Helmvorsatz, weil Forges `renderHelmetOverlay` in 1.21 fehlt |

Registriert sind 16 Anzugteile, drei Schutztücher, drei Lappen, vier Vollmasken, zwei
Lappenmasken und fünf Filtereinsätze.

**Warum drei Klassen des Originals zu einer geworden sind.** `ArmorGasMask`, `ArmorHazmat` und
`ArmorHazmatMask` unterscheiden sich nur in zwei Dingen: was sie trotz eingesetztem Filter nicht
durchlassen, und welches Bild sie über den Schirm legen. Beides kommt jetzt aus dem Bauaufruf.
`ArmorFSB` — die Basisklasse mit den Satzboni — ist **bewusst nicht** portiert worden: kein
einziges Teil dieser Runde benutzt einen Satzbonus, die Klasse wäre eine leere Hülle geworden.
Sie kommt mit dem ersten Satz, der sie braucht (Liquidator, Envsuit, T-51).

**Zwei Fehler auf demselben Weg.** Beide wären erst mit echten Masken sichtbar geworden:

* `ArmorUtil` hat an drei Stellen `isEmpty()` statt `!isEmpty()` geprüft, bevor es einen
  Helmaufsatz als Maske behandelt. Die Bedingung konnte nie zutreffen — der ganze Aufsatzpfad
  war tot.
* `ArmorRegistry.getProtectionFromItem` hat `clone()` auf dem Ergebnis von
  `hazardClasses.get(filter)` gerufen, ohne auf `null` zu prüfen. Sobald etwas ohne eigene
  Gefahrenklassen im Gewinde sitzt, wäre das ein Absturz gewesen.
  `GasMaskItem.isFilterApplicable` lässt jetzt zusätzlich nur noch echte Filtereinsätze zu.

**Noch offen.** Die Kopfmodelle (`ModelGasMask`, `ModelM65`) sind nicht portiert; die Masken
bleiben am Körper unsichtbar. Statt der lilaschwarzen Ersatztextur einer fehlenden Datei liegt
eine durchsichtige Rüstungsschicht bei. Schutzbrille und Aschebrille warten auf dieselben
Modelle und folgen mit ihnen.

---

## Stufe 6 — Runde 137: der Rüstungstisch und die Module

Der zweite Schritt der Schutzkleidung. Wieder einer, bei dem der Unterbau längst stand und nur
niemand hinkam.

**Ausgangslage.** `ItemArmorMod`, `ArmorModHandler` und `ItemModCladding` liegen seit langem im
Port. Benutzbar war davon **nichts**: kein Tisch, an dem sich ein Modul einsetzen ließe, kein
einziges registriertes Modul, und die Haken `modUpdate`, `modDamage` und `getModifiers` hat
niemand gerufen.

**Was dazugekommen ist.** Der Rüstungstisch (Block, Menü, Oberfläche) mit den neun Modulplätzen
rings um das Rüstungsteil, sieben Auskleidungen und elf Einlagen, dazu die Anbindung an den
Tick, an `LivingDamageEvent.Pre` und — für die Obsidianauskleidung — an
`EntityJoinLevelEvent`.

**Die eine echte Abweichung.** Das Original hängt die Eigenschaftswerte eines Moduls als
`Multimap` an das Rüstungsteil und lässt Forge sie beim Anlegen übernehmen. In 1.21 stehen die
Eigenschaften eines Gegenstands in einer Datenkomponente und dürfen nicht vom NBT des Stapels
abhängen. Stattdessen sammelt `ArmorModHandler.updateMods` jeden Tick die Werte aller vier Teile
ein und setzt sie als vorübergehende Werte am Träger; `ItemArmorMod.getModifiers` ist durch
`addAttributes` ersetzt. Die Rechenart steht je Eigenschaft fest — Tempo anteilig, Rückstoß
absolut, beides wie im Original.

**Drei Fehler, alle auf dem Weg, den diese Runde erst begehbar macht:**

* `ArmorModHandler.removeMod` hat die **innere** Modultafel als ganze Nutzdatentafel
  zurückgeschrieben. Danach lagen die Modulplätze auf oberster Ebene und `hasMods` sah gar
  keine Module mehr.
* `ArmorModHandler.pryMods` hat leere Plätze als `null` zurückgegeben, während alle Aufrufer
  reihum `isEmpty()` fragen. Das erste Rüstungsteil mit Modulen hätte den Server abgeräumt.
* `ItemModCladding` hat den übergebenen Strahlungswert nie zugewiesen — die Auskleidung hat
  also nichts abgehalten.

**Ein CI-Durchlauf verloren**, und daraus ein neues Tor: `percent(...)` war als `Component`
erklärt und wurde viermal mit `.withStyle(...)` aufgerufen. `api-check.sh` sammelt jetzt die
Namen aller projekteigenen Methoden ein, die im ganzen Quelltext **nur** als `Component`
erklärt sind, und beanstandet jede Aufrufstelle, an der direkt danach `withStyle` oder `append`
folgt. Namen, die anderswo auch als `MutableComponent` erklärt sind, fallen heraus.
Empfindlichkeit gemessen: mit der wiederhergestellten Fehlerstelle genau vier Meldungen, ohne
sie keine.

## Stufe 6 — Runde 138: der Fraktionierturm

Erster Schritt der Erdölkette. Raffinerie, Bohrturm und Pumpe stehen seit Stufe 2 — aber aus
Schweröl wurde danach nichts mehr, weil keine einzige Maschine es weiterverarbeitet.

Dazugekommen sind `FractionRecipes` (neunzehn Umsetzungen, Eingabe immer 100 mB), der Turm
selbst und sein Zwischenstück. Der Turm braucht keinen Strom und hat keine Oberfläche;
angeschlossen wird am Fuß. Türme lassen sich stapeln: steht drei Blöcke darüber ein zweiter,
schiebt der untere sein Öl hinauf und zieht die Fraktionen wieder herunter.

**Nicht in dieser Runde: Krackturm und Reformer.** Der Krackturm ist im Original kein Turm,
sondern ein großer, richtungsabhängiger Verbund mit fünf Teilkörpern (`getAllDimensions`) und
richtungsbezogenen Anschlusspunkten; der Reformer braucht Strom, ein Inventar und eine
Oberfläche. `ReformingRecipes` war schon geschrieben und ist wieder entfernt worden — ohne den
Reformer wäre es toter Code.

**Zwei Torwächter haben vor der CI angeschlagen**, beide zu Recht: `@Override` auf
`getRenderBoundingBox()` (die Methode stammt aus der NeoForge-Erweiterung und gilt dem Compiler
nicht als überschrieben), und die bekannte Scheinmeldung „types IFluidStandardSenderMK2 and
IFluidStandardSenderMK2 are incompatible", die für zwölf andere Klassen längst in der Baseline
steht.

### Stand der Erdölkette nach Runde 138

| | |
|---|---|
| portiert | Bohrturm, Pumpe, Frackingturm, Raffinerie, **Fraktionierturm**, Zwischenstück |
| offen (10) | Krackturm, Reformer, Coker, Gasfackel, Hydrotreater, Verflüssiger, Pyroofen, Verfestiger, Vakuumdestille, Abstandshalter des Krackturms |

## Stufe 6 — Runden 139 bis 141: Reformer, Hydrotreater, Vakuumdestille

Drei Maschinen der Erdölkette nach demselben Muster: Blockentität mit Strom und Tanks, Block,
Menü, Oberfläche, Darsteller, dazu je eine Rezeptliste als `SerializableRecipe`.

| Runde | Maschine | Was sie tut |
|---|---|---|
| 139 | Katalytischer Reformer | baut lange Ketten zu ringförmigen Verbindungen um; braucht einen Katalysator |
| 140 | Hydrotreater | wäscht unter Wasserstoffdruck den Schwefel aus einem Öl |
| 141 | Vakuumdestille | zieht aus einem Öl vier Fraktionen statt zwei; das Öl muss unter Druck ankommen |

Neu dazugekommen ist der Gegenstand `catalytic_converter`, den Reformer und Hydrotreater
beide brauchen und den es im Port noch nicht gab.

**Eine Abweichung kommt in allen dreien vor.** Das Original sperrt die Kanisterplätze für
Stoffe, die unter Druck stehen müssen, mit dem Slot-Typ `SlotDeprecated`. Den gibt es im Port
nicht; stattdessen weist die Blockentität sie in `canPlaceItem` ab, und `SlotNonRetarded`
fragt genau danach. Die Plätze bleiben erhalten, damit die Nummerierung dieselbe ist wie im
Original.

Die üblichen Abbildungen gelten weiter: `ANY_RESISTANTALLOY` → DURA, Desh-Motor → gewöhnlicher
Motor, `CHIP_BISMOID` → Mikrochip, `ANY_HARDPLASTIC` → Polycarbonat, `ANY_BISMOID` →
Wismutbronze; die `inputItemsEx`-Variante der Montagerezepte entfällt wie überall.

### Stand der Erdölkette nach Runde 141

| | |
|---|---|
| portiert (9) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, **Reformer**, **Hydrotreater**, **Vakuumdestille** |
| offen (6) | Krackturm, Coker, Gasfackel, Verflüssiger, Pyroofen, Verfestiger |

**Was den sechs noch fehlt.** Verflüssiger und Verfestiger brauchen Gegenstände, die es im
Port noch nicht gibt — vor allem `oil_tar` in seinen fünf Spielarten, dazu `solid_fuel_bf`,
`ingot_mercury`, `biomass_compressed` und `bio_wafer`. Die gehören in eine eigene, kleine
Runde vorweg, sonst müsste jede der beiden Maschinen ihre halbe Rezeptliste auslassen. Der
Krackturm ist der aufwendigste: im Original kein Turm, sondern ein großer,
richtungsabhängiger Verbund mit fünf Teilkörpern und richtungsbezogenen Anschlusspunkten.

## Stufe 6 — Runde 142: der Verfestiger

Die Maschine, die die Kette hinten schließt: sie macht aus einer Flüssigkeit wieder einen festen
Gegenstand. Wasser wird Eis, Lava wird Obsidian, Öl wird Teer, und alles, was brennt, lässt sich
zu Brennstoffwürfeln pressen. Vier Blöcke hoch auf einem Feld; angeschlossen wird oben an der
Haube und an den vier Auslegern auf mittlerer Höhe.

Wie viel von einem brennbaren Stoff ein Würfel kostet, steht nicht in der Liste, sondern rechnet
sich aus seinem Wärmewert: so viel, dass die Wärme dem Würfel entspricht, plus fünfundzwanzig
Prozent Aufschlag fürs Pressen, danach auf eine glatte Zahl gerundet. Das ist unverändert aus dem
Original übernommen — dadurch bleiben die sechsundzwanzig Einträge automatisch stimmig, wenn sich
einmal ein Wärmewert ändert.

**Drei Gegenstände sind neu**, weil die Rezeptliste sie braucht: `solid_fuel_bf` (der
Balefire-Würfel), `biomass_compressed` und `bio_wafer`. Die fünf Spielarten von `oil_tar` waren
entgegen der Notiz nach Runde 141 bereits im Port vorhanden — als sechs einzelne Gegenstände
statt als einer mit Metadaten.

**Zwei Abweichungen vom Original.**

- **Quecksilber fehlt.** `ingot_mercury` gibt es im Port nicht und im Original auch keine Textur
  dafür; der Eintrag entfällt, wie schon in `CrystallizerRecipes` vermerkt.
- **Balefire steht nur einmal in der Liste.** Das Original trägt es zweimal ein: erst fest mit
  250 mB, danach über dieselbe Automatik, die den festen Wert wieder überschreibt. Hier steht
  nur die Automatik — der feste Eintrag wäre ohne jede Wirkung.

**Drei Abweichungen gegenüber dem ersten Entwurf dieser Runde**, alle beim Vergleich mit dem
Original gefunden: der Fortschrittsbalken teilte durch 24 statt durch 42, die Lampe über der
Batterieanzeige fehlte ganz, und die Rezeptdatei hieß `hbmSolidification.json` statt
`hbmSolidifier.json` und schrieb die Eingabemenge als eigenes Feld statt als Füllstand des
Eingabestoffs.

### Stand der Erdölkette nach Runde 142

| | |
|---|---|
| portiert (10) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, Reformer, Hydrotreater, Vakuumdestille, **Verfestiger** |
| offen (5) | Krackturm, Coker, Gasfackel, Verflüssiger, Pyroofen |

**Noch offen für die ganze Kette: die JEI-Ansichten.** Keine der Maschinen aus den Runden 138
bis 142 hat bisher eine — im Original gibt es für Fraktionierung, Reformierung, Hydrotreating,
Vakuumdestillation und Verfestigung je einen NEI-Handler. Das gehört in eine eigene Runde, wenn
die Kette vollständig ist.

## Stufe 6 — Runde 143: der Pyroofen

Der Pyroofen zerlegt unter Hitze und Sauerstoffabschluss. Anders als die übrigen Maschinen der
Kette nimmt er wahlweise ein Fluid, einen Gegenstand oder beides und gibt wahlweise beides
zurück — daher die zwei Tanks neben zwei Gegenstandsplätzen. Ein Kasten von sieben mal fünf
Blöcken, drei hoch; die Anschlüsse liegen als Reihe an der einen Längsseite, der Schornstein
oben auf der anderen.

Er ist die erste Maschine des Ports, die **Grundgesteinserz röstet**: alle fünf Vorstufen jeder
Sorte, jedes Mal fällt Vitriol an. Bisher gab es im Port keinen Weg zu den gerösteten Erzen,
obwohl die Stufen seit Runde 131 existieren.

Dazu die Kohlenchemie des Originals: Synthesegas aus Kohle, Koks oder Biomasse, Schweröl und
Kohlegas aus Kohle, Ruß aus Teer, Reformgas aus Kokergas, Wasserstoff und Graphit aus Erdgas,
und Wolframcarbid aus Wolframstaub und Synthesegas. Wie der Verfestiger presst auch er alles
Brennbare zu Brennstoffwürfeln — nur doppelt so sparsam.

**Abweichungen.** Für Kohle steht der Gegenstand selbst, für Kohlenstaub der eigene Gegenstand,
für Koks und Teer je ein Sammeltag und für Wolframstaub der Materialtag; das Original nimmt
überall OreDictionary-Namen. Im Montagerezept gelten die üblichen Abbildungen (Polycarbonat,
gewöhnlicher Motor, Mikrochip) und statt Kupferrohren stehen Stahlrohre, die einzigen des Ports.

**Nebenbei zwei Löcher gestopft.** Das kleine Infofeld neben den Aufwertungsplätzen war beim
Lichtbogenschweißer und bei der Lötstation leer: beide sammelten die eingesetzten Aufwertungen
korrekt ein, riefen dann aber `provideInfo` gar nicht auf (die Zeile stand auskommentiert, weil
die Signatur damals noch `List<String>` nahm) und schoben eine immer leere Liste durch. Die
Maschine beschreibt die Wirkung ihrer Aufwertungen selbst über `IUpgradeInfoProvider`; das
Einsammeln ist für alle Maschinen dasselbe und steht jetzt einmal in `InfoScreen.upgradeInfo`.
Beide Schirme und der neue Pyroofen benutzen es.

### Stand der Erdölkette nach Runde 143

| | |
|---|---|
| portiert (11) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, Reformer, Hydrotreater, Vakuumdestille, Verfestiger, **Pyroofen** |
| offen (4) | Krackturm, Coker, Gasfackel, Verflüssiger |

## Stufe 6 — Runde 144: der Verflüssiger

Das Gegenstück zum Verfestiger, im Aufbau bis auf einen Platz identisch: vier Blöcke hoch auf
einem Feld, dieselben sechs Anschlüsse. Er macht aus einem festen Gegenstand eine Flüssigkeit —
Kohle zu Kohlenöl, Teer zu Bitumen, Holz zu Holzessig, Blei und Natrium zu ihren Schmelzen,
Eis und Schnee zu Wasser, Stein und Obsidian zu Lava. Was in keiner Liste steht, aber essbar
ist, wird zu Nährflüssigkeit, so viel wie sein Sättigungswert hergibt.

**Abweichungen.**

- **Die leere Glyphidendrüse fehlt** — die gibt es im Port noch nicht.
- **Das Original kennt zwei Blumen** (`plant_flower` mit den Metadaten 3 und 4) mit 100 und
  50 mB. Der Port hat nur die eine; sie steht mit den 100 mB der ersten.
- **Fischöl** kam im Original aus einem Eintrag mit Platzhalter-Metadaten. Im Port sind die vier
  Fische eigene Gegenstände und stehen einzeln.
- Die Rezeptliste wird über `AStack` geführt statt über gemischte Schlüssel aus Gegenstand oder
  OreDictionary-Name; Gegenstand und Tag werden dadurch gleich behandelt.
- Die Nährflüssigkeit rechnet der Port direkt aus `FoodProperties.saturation()`. In 1.21 steckt
  in diesem Wert bereits Nährwert mal Faktor mal zwei, die Rechnung des Originals entfällt
  deshalb.

### Stand der Erdölkette nach Runde 144

| | |
|---|---|
| portiert (12) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, Reformer, Hydrotreater, Vakuumdestille, Verfestiger, Pyroofen, **Verflüssiger** |
| offen (3) | Krackturm, Coker, Gasfackel |

## Stufe 6 — Runde 145: die Gasfackel

Der Schornstein, an dem in der Raffinerie abgelassen wird, was sonst nirgends hinpasst. Zwölf
Blöcke hoch auf einem Feld von drei mal drei, angeschlossen wird unten an den vier Füßen.

Zwei Schalter in der Oberfläche, beide über `IControlReceiver`: das **Ventil** lässt überhaupt
etwas hinauf, die **Zündung** entscheidet, ob es oben abgefackelt oder nur abgeblasen wird.
Abgefackelt wird nur Brennbares — dabei fällt Strom an, fünfmal so viel aus Gas wie aus einer
Flüssigkeit. Abgeblasen wird nur Gasförmiges, dafür fünfmal so schnell. Wer der Flamme zu nahe
kommt, brennt.

Sie ist die erste Maschine der Kette, die **kippt**: steht sie nicht auf vier tragenden Ecken,
neigt sie sich und stellt den Betrieb ein. Das Kippsystem gibt es im Port seit dem
Hochofen; hier wird es zum ersten Mal in der Erdölkette benutzt.

**Zwei Abweichungen.**

- Das Original spielt beim Abfackeln `hbm:weapon.flamethrowerShoot`. Diesen Ton gibt es im Port
  nicht; an seiner Stelle steht das Feuerknistern von Vanilla.
- Beim Verbrennen zeigt das Original zusätzlich eine Rauchfahne aus einer Partikelsorte, die der
  Port nicht kennt — sie bleibt weg. Die Gasfahne beim Abblasen und die Flamme selbst sind da.

### Stand der Erdölkette nach Runde 145

| | |
|---|---|
| portiert (13) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, Reformer, Hydrotreater, Vakuumdestille, Verfestiger, Pyroofen, Verflüssiger, **Gasfackel** |
| offen (2) | Krackturm, Coker |

Die beiden letzten sind die aufwendigsten: beides große, richtungsabhängige Verbunde mit
mehreren Teilkörpern (`getAllDimensions`), der Coker zusätzlich am Wärmenetz statt am Stromnetz.

## Stufe 6 — Runde 146: der Verkoker und ein neuer Torwächter

Die vorletzte Maschine der Kette. Der Verkoker treibt aus einem Öl den Kohlenstoff aus: es
bleibt Petrolkoks, und was sich dabei leichter verflüchtigt, geht als Gas oder leichteres Öl
weiter. Ein schlanker Turm von dreiundzwanzig Blöcken auf einem Sockel von drei mal drei, dazu
ein Absatz von fünf mal fünf und vier Stützen an dessen Ecken — im Original wie hier stehen alle
Teilkörper fest nach Norden, keiner hängt an der Blickrichtung.

Er ist die einzige Maschine der Erdölkette, die **nicht am Stromnetz hängt, sondern am
Wärmenetz**: was unter ihm an Wärme anliegt, zieht er zu einem Viertel ab; ein Durchgang kostet
20.000 Wärmeeinheiten. Die Rauchfahne aus dem Schlot benutzt dieselbe Partikelsorte wie der
Drehrohrofen.

### Runde 145 ist in der CI gescheitert, und das hat einen Torwächter ergeben

`MachineGasFlareBlock.appendHoverText` übernahm die Signatur von einer Nachbarklasse, aber ohne
deren Zeile `import net.minecraft.world.item.Item.TooltipContext;`. Ein Paket-Sternimport bringt
geschachtelte Typen nicht mit — der Typ fehlte also, und javac meldete nur „cannot find symbol".

Die drei vorhandenen Torwächter sind dafür blind: `import-check.sh` kennt nur Projekttypen, und
`syntax-check.sh` muss „cannot find symbol" wegwerfen, weil ohne Minecraft-Klassenpfad
zehntausende davon entstehen. `api-check.sh` hat jetzt eine sechste Regel dafür.

**Ein erster, allgemeiner Entwurf der Regel wurde verworfen.** Er suchte im ganzen Baum nach
geschachtelten Typen ohne Import und fand über vierzig Fehlalarme: geschachtelte Typen einer
Oberklasse stehen ohne Import im Geltungsbereich (jeder Block-Nachfahre benutzt `Properties` aus
`BlockBehaviour`), und welche Minecraft-Oberklasse welche mitbringt, lässt sich ohne Klassenpfad
nicht feststellen. Dieselbe Falle beim zweiten Versuch: `TooltipContext` ist in `Item`
geschachtelt und steht in jeder Item-Unterklasse im Geltungsbereich — siebzig Dateien dieses
Baums. Die Regel gilt deshalb nur für Blöcke, und sie steht als Liste, die mit jedem
CI-Fehlschlag wächst, nicht als allgemeine Suche.

Gemessen: mit dem wiederhergestellten Fehler zwei Funde, ohne ihn keiner.

### Stand der Erdölkette nach Runde 146

| | |
|---|---|
| portiert (14) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, Reformer, Hydrotreater, Vakuumdestille, Verfestiger, Pyroofen, Verflüssiger, Gasfackel, **Verkoker** |
| offen (1) | Krackturm |

## Stufe 6 — Runde 147: der Krackturm, und die Erdölkette ist vollständig

Die letzte und größte Maschine der Kette. Der Krackturm spaltet ein schweres Öl unter Dampf in
ein leichteres und ein Gas; was woraus wird, steht in `CrackingRecipes`, die es im Port schon
gab. Fünf Tanks: schweres Öl und Dampf hinein, leichteres Öl, Gas und Altdampf hinaus.

Er hat als einzige Maschine der Kette **keine Oberfläche**: eingestellt wird er mit einem
Fluidkennzeichner in der Hand, abgelesen über die Einblendung beim Hinsehen (`ILookOverlay`).

Sein Verbund ist der größte des Ports: **fünf Teilkörper**, alle an der Blickrichtung
ausgerichtet, dazu acht Anschlussblöcke rings um den Sockel. Die Maße sind unverändert aus dem
Original übernommen.

**Zwei Torwächter haben vor der CI angeschlagen**, beide zu Recht. Die bekannte Scheinmeldung zu
`IFluidStandardSenderMK2` ist wie üblich in der Baseline gelandet. Der zweite Fund war echt:
`dist-check.sh` hat gemerkt, dass `printHook` die Client-Klasse `RenderGuiEvent` im Rumpf hat,
ohne `@OnlyIn(Dist.CLIENT)` davor — der Server wäre beim Laden der Klasse abgebrochen.

### Stand der Erdölkette nach Runde 147

| | |
|---|---|
| portiert (15) | Bohrturm, Pumpe, Frackingturm, Raffinerie, Fraktionierturm, Zwischenstück, Reformer, Hydrotreater, Vakuumdestille, Verfestiger, Pyroofen, Verflüssiger, Gasfackel, Verkoker, **Krackturm** |
| offen | — |

**Die Kette ist vollständig.** Vom Bohrturm bis zum Brennstoffwürfel ist jede Maschine des
Originals im Port; damit ist auch die zweite Hälfte des Ziels „der nächste Schutzkleidungsblock
und die Erdölkette" erfüllt (die erste war die Hazmat-Familie in den Runden 136/137).

**Was für die ganze Kette noch offen ist: die JEI-Ansichten.** Keine der Maschinen aus den
Runden 138 bis 147 hat eine — im Original gibt es für Fraktionierung, Reformierung,
Hydrotreating, Vakuumdestillation, Verfestigung, Verflüssigung, Pyrolyse, Verkokung und Kracken
je einen NEI-Handler. Das ist die nächste sinnvolle Runde.

## Stufe 6 — Runde 148: die JEI-Ansichten der Erdölkette

Der letzte offene Punkt aus Runde 147. Neun neue Ansichten, für jede Rezeptart der Kette eine:

| Ansicht | zeigt |
|---|---|
| Fraktionierung | ein Öl → zwei Fraktionen |
| Reformierung | ein Öl → drei Erzeugnisse |
| Hydrotreating | Öl + Wasserstoff → entschwefeltes Öl + Sauergas |
| Vakuumdestillation | ein Öl → vier Fraktionen |
| Verfestigung | eine Flüssigkeit → ein Gegenstand |
| Verflüssigung | ein Gegenstand → eine Flüssigkeit |
| Pyrolyse | Gegenstand und/oder Fluid → Gegenstand und/oder Fluid |
| Verkokung | ein Öl → Koks + Nebenprodukt |
| Kracken | Öl + Dampf → zwei Erzeugnisse + Altdampf |

Alle neun benutzen denselben allgemeinen Hintergrund wie die übrigen Fluid-Ansichten des Ports
und nehmen ihre Einträge direkt aus den Rezeptlisten, die in den Runden 138 bis 147 entstanden
sind — eine eigene Datenhaltung braucht keine davon.

Der Krackturm ist der einzige, dessen Überschrift nicht auf einen `container.*`-Schlüssel zeigt:
er hat keine Oberfläche, also gibt es keinen. Seine Ansicht trägt den Blocknamen.

**Damit ist die Erdölkette in jedem Sinn fertig:** jede Maschine des Originals ist portiert, und
jede Umwandlung ist im Spiel nachschlagbar.

### CI-Stand der Runden 142 bis 148

| Runde | Inhalt | CI |
|---|---|---|
| 142 | Verfestiger | ✅ |
| 143 | Pyroofen | ✅ |
| 144 | Verflüssiger | ✅ |
| 145 | Gasfackel | ❌ (fehlender Import, in 146 behoben) |
| 146 | Verkoker, Berichtigung zu 145, sechste api-check-Regel | ✅ |
| 147 | Krackturm | ✅ |
| 148 | neun JEI-Ansichten | ✅ |

## Runde 149: der Ladeabsturz aus dem Spielprotokoll

Aus einem echten Spielprotokoll (16.09., AllTheForge10 mit 165 Mods, hbmsntm-198A):

```
Mod 'hbmsntm' encountered an error in a deferred task:
java.lang.IllegalStateException: Duplicate client extensions registration for
hbmsntm:turret_howard (old: com.hbm.main.ClientProxy$1@..., new: ...@...)
```

Danach war der Mod im Zustand „broken" und der Client brach beim Laden ab.

**Die Ursache.** `ClientProxy.registerClientExtensions` läuft über alle
Blockentitäten-Darsteller und gibt jedem, der `IBEWLRProvider` ist, auch einen Darsteller für
seinen Gegenstand. **Sieben Darsteller hängen an zwei Blockentitätsarten** — Turm und
beschädigter Turm bei Howard, Chekhov und Sentry, RBMK-Säule und ihre Zwillingsform, Radarschirm
und Radar-Fächer, Gießerei und Becken. Der Lauf trifft sie deshalb zweimal, und ihr
`getItemsForRenderer` nennt beide Male beide Gegenstände. NeoForge lässt eine zweite
Registrierung für denselben Gegenstand nicht durchgehen.

Der Lauf siebt jetzt: jeder Gegenstand wird höchstens einmal angemeldet, und ein Darsteller,
dessen Gegenstände schon alle vergeben sind, wird übersprungen.

**Warum kein Torwächter das finden konnte.** Es ist kein Übersetzungsfehler und keine
Assetlücke, sondern eine Doppelanmeldung zur Laufzeit — sichtbar erst, wenn ein echter Client
lädt. Der `runServer`-Rauchtest der CI läuft ohne Client und kommt an
`RegisterClientExtensionsEvent` gar nicht vorbei.

## Runde 150: die fehlenden Modelle aus dem Spielprotokoll, und ein Tor dafür

Dasselbe Protokoll vom 16.09. meldete neun Gegenstände mit dem fehlenden-Modell-Würfel und acht
fehlende Partikelbilder. Alle neun hatten verschiedene Ursachen:

| Fundstelle | Ursache |
|---|---|
| `door_metal`, `door_office`, `door_bunker` | `doorBlockWithRenderType` erzeugt kein Gegenstandsmodell — Türen tragen es flach aus eigener Textur |
| `fence_metal`, `fence_metal_post` | `fenceBlock` erzeugt kein `_inventory`; die vorhandenen `blockItem`-Zeilen zeigten auf ein Modell, das nie entstand |
| `pwr_block` | Blockmodell war da, das `blockItem` fehlte |
| `pwr_fuel_hot`, `pwr_fuel_depleted` | `EnumMultiItem.registerItemModel` schrieb für `multiTexture == false` **gar nichts** |
| `ammo_debug` | in keinem Erzeuger genannt; es gibt auch im Original keine Textur dafür → läuft auf den Platzhalter `nothing` |

Dazu die Partikel: `vanilla_cloud.json` und `gas_flame.json` zeigten auf
`hbmsntm:vanilla/generic_0..7` — einen Unterordner, den es nie gab. Beide sind Kopien von
Vanillas `cloud.json`, bei denen der Namensraum mit umgeschrieben wurde. Sie zeigen jetzt wieder
auf `minecraft:generic_0..7`.

### Ein sechzehntes Tor, und warum es nicht bei den anderen steht

Die fünfzehn Tore arbeiten auf dem Quelltext. Sechs der neun Fundstellen hängen aber nicht
daran, was im Quelltext **steht**, sondern daran, was der Erzeuger **tut** — und der braucht den
Minecraft-Klassenpfad, der in der Entwicklungsumgebung gesperrt ist.

`tools/model-resolve-check.sh` läuft deshalb in der CI, direkt hinter `runData`, und sieht das
Ergebnis: löst jedes Elternmodell auf, und hat jeder Eintrag der erzeugten Sprachdatei ein
Gegenstandsmodell? Das hätte alle neun gefunden.

**Es misst seine eigene Empfindlichkeit.** `--selbstprobe` baut eine Nachbildung der Fundstellen
in einem Temp-Verzeichnis und besteht nur, wenn die Regel genau ein fehlendes Elternmodell und
drei Gegenstände ohne Modell meldet. Die CI führt erst die Selbstprobe aus, dann die Prüfung —
ein Tor, dessen Empfindlichkeit niemand misst, ist eine Attrappe.

**Neun Blöcke sind ausgenommen.** `balefire`, `barricade`, `corium`, `fire_digamma`,
`icf_block`, `mud`, `pile_block`, `rad_lava` und `volcanic_lava` stehen mit dem blanken
`BLOCKS.register` im Quelltext statt mit einem der `register`-Helfer, die sonst jedem Block
einen `BlockItem` mitgeben — Flüssigkeiten, Feuer und Wrapper sollen nicht in der Hand liegen.
Eine Namenszeile haben sie trotzdem, also müssen sie in der Liste stehen; kommt einer hinzu,
fällt er auf und gehört mit Begründung dazu.

### Nachtrag: der erste Lauf des neuen Tors hat einen eigenen Fehler aufgedeckt

`runData` brach ab mit `Texture hbmsntm:item/battery_pack does not exist in any known resource
pack`. Ursache war die Änderung an `EnumMultiItem.registerItemModel` aus dieser Runde: sie
schrieb für **jedes** `multiTexture == false` ein flaches Modell auf `item/<name>` — auch für
`battery_pack`, das gar keine Textur hat, weil ein eigener Darsteller es zeichnet.

Die Änderung ist zurückgenommen. Richtig ist die Aufteilung, die der Port ohnehin verwendet:
`NtmItemModelProvider` nennt jeden Gegenstand einzeln — `basicItem`, wo es eine Textur gibt,
`entityItem`, wo ein Darsteller zeichnet. Die beiden PWR-Brennstoffe waren dort schlicht
vergessen worden und stehen jetzt drin.

### Zweiter Nachtrag: das Tor meldete 27 Fehlalarme

Der nächste Lauf kam durch `runData` und blieb am neuen Tor hängen: **27 `canned_*`-Gegenstände
ohne Modell**. Alle 27 waren falsch.

`canned_conserve` ist **ein** registrierter Gegenstand mit siebenundzwanzig Metawerten, und
`ConserveItem.getDescriptionId` gibt für jeden eine eigene Namenszeile aus —
`item.hbmsntm.canned_asbestos`, `canned_spam` und so weiter. Meine Annahme, die erzeugte
Sprachdatei sei das Verzeichnis der Gegenstände, stimmt für Metagegenstände nicht.

Die Sprachdatei wird jetzt gegen die **tatsächlichen Registrierungsnamen** gesiebt: alles, was
im Quelltext als `register("name", ...)` auftaucht. Das erfasst `NtmItems`, die
`register`-Helfer von `NtmBlocks` und auch, was `GunFactory` zur Laufzeit anmeldet — daher kam
`ammo_debug`, und genau deshalb sah `model-check.sh` ihn nicht. Gemessen: alle neun echten
Fundstellen bleiben drin, die sechsundzwanzig Metawerte fallen heraus.

Die Selbstprobe deckt den Fehlalarm jetzt mit ab: ihre Nachbildung enthält eine
`canned_asbestos`-Zeile, die **nicht** gemeldet werden darf. Entfernt man das Sieb, meldet sie
vier statt drei Fundstellen und schlägt fehl — gegengeprüft.

## Runde 153: die Leviathan-Turbine verschwindet beim Wegschauen

> „die Leviathan Turbine wird nach dem platzieren unsichtbar, wenn man zur Seite schaut"

Der Befund ist eindeutig und die Klasse gross: **`ChungusBlockEntity` hat kein
`getRenderBoundingBox`** — und siebzehn weitere Blockentitäten auch nicht.

### Warum das ein Block-Entity-Problem ist und kein Modellproblem

Minecraft schneidet Blockentitäten zweimal gegen den Sichtstumpf: einmal grob über den
16×16×16-Abschnitt, in dem sie stehen, und einmal fein über einen Kasten je Blockentität.
Diesen Kasten liefert die NeoForge-Erweiterung `getRenderBoundingBox()`; ohne eigene Angabe
ist er **genau ein Block gross**.

Bei einer Ein-Block-Maschine stimmt das. Bei einem Mehrblockbau steht der Kern irgendwo im
Bauwerk, und das Modell reicht weit darüber hinaus. `chungus.obj` misst in Z −7,5 bis 7,49,
und `RenderChungus` schiebt es zusätzlich um drei Blöcke — die Turbine ist rund fünfzehn
Blöcke lang, ihr Sichtkasten war einer. Dreht der Spieler den Kopf so, dass dieser eine Block
aus dem Bild fällt, verschwindet das ganze Modell schlagartig, obwohl es noch zur Hälfte vor
ihm steht. Genau das hat der Spieler beschrieben.

Das Original löst es mit `TileEntity.INFINITE_EXTENT_AABB` — die Turbine wird also immer
gezeichnet. Hier steht stattdessen ein fester Kasten von 23×7×23 um den Kern: das deckt jede
der vier Aufstellrichtungen ab und lässt das Aussortieren auf Entfernung intakt.

### Siebzehn Klassen, nicht eine

Alle 122 Darsteller wurden durchgesehen. Ergänzt wurde der Sichtkasten bei:

| Klasse | Kasten | Herkunft |
|---|---|---|
| `ChungusBlockEntity` | 23×7×23 | Original: unendlich |
| `FusionTorusBlockEntity` | 17×5×17 | Original wörtlich |
| `MachineFrackingTowerBlockEntity` | 9×25×9 | Original: unendlich, Masse aus dem Modell |
| `MachineVacuumDistillBlockEntity` | 3×9×3 | Original wörtlich |
| `MachineRefineryBlockEntity` | 3×10×3 | Original: unendlich |
| `MachineHydrotreaterBlockEntity` | 3×7×3 | Original wörtlich |
| `MachineCatalyticReformerBlockEntity` | 5×7×5 | Original wörtlich |
| `MachineBlastFurnaceBlockEntity` | 3×7×3 | Original wörtlich |
| `ReactorZirnoxBlockEntity` | 5×5×5 | Original wörtlich |
| `ZirnoxDestroyedBlockEntity` | 7×3×7 | Original wörtlich |
| `MachineArcWelderBlockEntity` | 3×3×3 | Original wörtlich |
| `MachineSolderingStationBlockEntity` | 3×3×3 | Original wörtlich |
| `MachineReactorBreedingBlockEntity` | 1×4×1 | Original +1, Modell ist 3,25 hoch |
| `BatterySocketBlockEntity` | 3×2×3 | Original wörtlich |
| `MachineFurnaceCombinationBlockEntity` | 3×2,125×3 | Original wörtlich |
| `PlushieBlockEntity` | 3×2×3 | Hundun ragt 0,7 Blöcke heraus |
| `LandmineBlockEntity` | 3×3×3 | Seemine ragt 0,7 Blöcke heraus |

Der **Torwächter hat sich zweimal geirrt**, und beide Male in dieselbe Richtung: er sah nur
die Blockentität und übersah, dass der Port den Kasten auch im **Darsteller** setzen kann
(`getRenderBoundingBox(be)`). Zwölf der neunundzwanzig gemeldeten Klassen — darunter der
Tank, die Presse, der Bagger, das Radar — hatten ihn längst, nur an der anderen Stelle.
Nachgezählt statt geglaubt.

### Das sechzehnte Tor: `tools/renderbox-check.sh`

**Die Regel.** Eine Blockentität braucht einen Sichtkasten, wenn sie einen eigenen Darsteller
hat **und** mindestens einer ihrer Blöcke von `DummyableBlock` erbt. Der Kasten darf an drei
Stellen stehen: an der Blockentität, im Darsteller, oder als `shouldRenderOffScreen` — der
Port benutzt alle drei.

**Warum die zweite Bedingung.** Ein Mehrblockbau ist per Definition grösser als ein Block,
der voreingestellte Kasten also immer zu klein. Das ist keine Faustregel, sondern die Grenze,
ab der die Aussage sicher gilt.

**Nachgemessen.** 120 Blockentitäten haben einen eigenen Darsteller, 84 hängen an einem
Mehrblockbau. Nach dieser Runde meldet das Tor davon null. Nimmt man
`ChungusBlockEntity.getRenderBoundingBox` wieder heraus, meldet es genau diese eine Klasse.

**Verworfen** ist die naheliegendere Fassung „jede Blockentität mit Darsteller braucht einen
Kasten": achtzehn Fehlalarme — Wackelkopf, Geigerzähler, Plüschtier, die neun RBMK-Anzeigen
und die drei Teile des Reaktorstapels zeichnen alle innerhalb ihres Blocks. Eine
Ausnahmeliste dafür wäre eine Attrappe gewesen.

## Runde 154: der Big-Ass Tank

> „ich kann den big ass tank noch nicht in JEI finden und platzieren"

Er war nicht versteckt, sondern **gar nicht portiert**: `machine_bigasstank` kam im ganzen
Port kein einziges Mal vor. Kein Block, keine Blockentität, kein Darsteller, kein Modell,
keine Textur, kein Rezept. JEI zeigt nur, was in einem Kreativreiter steht — und dort stand
nichts, weil es nichts gab.

Die übrige Fassfamilie ist vollständig da (`barrel_plastic/steel/corroded/tcalloy`,
`machine_fluid_tank`); es fehlen nur der Big-Ass Tank und der schon im Original als veraltet
markierte `machine_bat9000`.

### Eigene Klasse statt Ableitung

Im Original erbt `TileEntityMachineBigAssTank` von `TileEntityBarrel`. Im Port geht das
nicht, und zwar an vier Stellen gleichzeitig: `BarrelBlockEntity` meldet im Konstruktor fest
`NtmBlockEntityTypes.BARREL` an, holt das Fassungsvermögen aus `BarrelBlock`, trägt die
Fassregeln (Kunststoff, Sprengstärke 5) in einer **privaten** Methode und kippt nur nach
Einstellung. Der Tank braucht an allen vier Stellen etwas anderes — und steht zusätzlich auf
einem Mehrblockbau statt auf einem einzelnen Block.

Man hätte `BarrelBlockEntity` aufbohren können. Das hätte eine ausgelieferte Klasse geändert,
an der vier Fässer hängen, für einen Bau, der sich in Fassungsvermögen, Anschlüssen,
Kippverhalten, Zerstörung, Darstellung und Beute ohnehin unterscheidet. Der Port hat den
gleichen Fall schon einmal entschieden: `MachineFluidTankBlockEntity` ist ein Geschwister von
`BarrelBlockEntity`, kein Nachfahre. Dieser Bau folgt dem.

### Die Masse

`getDimensions() = {5, 0, 4, 4, 4, 4}`, `getOffset() = 6` — ein 9×9-Rumpf, sechs Blöcke hoch.
Dazu sechs weitere Teilkörper: vier Ausbuchtungen an den Seiten und zwei Stutzen auf der
Blickachse. Insgesamt misst der Tank **dreizehn Blöcke auf der Blickachse, elf quer dazu und sechs in der Höhe**, und der Kern liegt sechs
Blöcke vor der angeklickten Stelle.

`getAllDimensions()` des Originals fällt weg: es dient dort allein der grün/roten Bauvorschau
(`BlockDummyable.drawPlacementHighlight`), die der Port nicht kennt. Die Zusatzgrundrisse
stehen deshalb als Konstanten da, wie beim Reformer und beim Bagger.

Das Original hat in `fillSpace` einen Tippfehler — in der Z-Komponente der beiden
`makeExtra`-Aufrufe steht `o` statt `6`. Nachgerechnet ist er folgenlos: bei Ost/West ist
`offsetZ` null, bei Nord/Süd `offsetX`, die beiden Zeilen tauschen dann nur die Plätze.
Hier steht die saubere Form, verhaltensgleich.

### Zwei Kleinigkeiten, die dabei auffielen

**Der Sichtkasten des Originals ist zu klein.** Es nimmt `x−6 … x+7, y+5`; das Modell reicht
aber bis ±6,5 und y 6,5 — die Kuppel würde abgeschnitten, und der Tank verschwände, sobald nur
noch sie im Bild ist. Genau die Fehlerklasse aus Runde 153. Hier steht ein Kasten, der passt.

**Der gewöhnliche Tank hatte kein Rezept.** `machine_fluid_tank` ist seit Runde 1 im Port, aber
`ass.tank` fehlte — in der Überlebensrunde war er damit unerreichbar. Steht jetzt drin,
zusammen mit `ass.bigasstank`.

### Was der Port bewusst weglässt

OpenComputers und Redstone-over-Radio hängen im Original an `TileEntityBarrel`. Der Port hat
beides bei den Fässern schon weggelassen; hier ebenso, im Klassenkommentar vermerkt.

## Runde 155: die überarbeiteten Texturen aus der CE-Abspaltung

> „in diesem hbm port wurde einige Texturen überarbeitet, diese hätte ich auch gerne:
> Warfactory-Official/Hbm-s-Nuclear-Tech-CE"

### Erst messen, dann kopieren

Blindes Überkopieren wäre falsch gewesen: CE ist eine eigenständige Abspaltung mit über
zweitausend *neuen* Dateien und mehr als tausend gelöschten, und der Port hat seinerseits
Texturen für 1.21 angepasst. Deshalb ein **Dreifachvergleich** über die git-Blob-Prüfsummen
von Port, Original (`hbm-upstream/master`) und CE, mit Pfadumrechnung `items/`→`item/` und
`blocks/`→`block/`:

| Klasse | Zahl | Bedeutung |
|---|---|---|
| gleich | 2388 | CE hat nichts geändert |
| **CE überarbeitet** | **214** | Port trägt das Original, CE eine neue Fassung |
| Port weicht schon ab | 35 | der Port hat selbst geändert |
| nicht in CE | 704 | Pfad existiert dort nicht |
| nicht im Original | 5 | |

Die 214 sind die Antwort auf die Frage. **208 davon sind übernommen**, 6 nicht.

### Warum die Oberflächenbilder einzeln geprüft wurden

Ein Gegenstands- oder Blockbild ist ein Sprite: derselbe Name, dieselbe Grösse, fertig. Ein
Oberflächenbild ist ein **Atlas** — der Bildschirm liest daraus feste Rechtecke. Malt CE nur
neu, ist die Übernahme harmlos; verschiebt CE etwas, zeichnet der Port danach ins Leere.

Jede der 60 betroffenen Oberflächen wurde deshalb gegen ihre 1.7.10-Java-Klasse in beiden
Bäumen gehalten und die gelesenen Rechtecke pixelweise verglichen. Ergebnis: 52 sind reines
Neumalen (meist nur der Energiebalken samt Mulde umgefärbt, Deckungsmaske Pixel für Pixel
identisch).

**Zwei weitere sind mitgenommen worden, samt Verschiebung.** Beim Detektor des
Teilchenbeschleunigers wandern die beiden Lämpchen um einen Pixel nach rechts, bei der Quelle
die obere Hälfte um zwei Pixel nach unten — dort ziehen vier Schächte und zwei Trefferfelder
mit. Beides ist ein reiner Versatz ohne Umbau, aus CEs eigenem Quelltext Zeile für Zeile
abgelesen.

### Die sechs, die draussen bleiben

**Drei, weil CE Anzeigen ersatzlos gestrichen hat.** Bei `gui_dipole` und `gui_quadrupole`
sind die vier 28×28-Felder der Spulenanzeige im Atlas leer (der Port hat dort je 784 deckende
Pixel, CE null), bei `gui_rbmk_heater` die beiden 10×10-Felder der Flüsseanzeige. Der Port
zeichnet diese Anzeigen weiterhin — mit CEs Bild blieben sie unsichtbar.

**Drei, weil CE die Oberfläche umgebaut hat.** `gui_mixer` verschiebt Strombalken,
Fortschrittsbalken, alle drei Tanks und alle fünf Schächte und lässt die Aufwertungsanzeige
ganz weg; `gui_rbmk_outgasser` verschiebt beide Balkenquellen und zwei Schächte;
`gui_battery` verschmälert den Ladebalken von 52 auf 34 Pixel und versetzt Symbole und
Schächte. Diese drei zu übernehmen hiesse, die Bildschirme des Ports auf CEs Entwurf
umzubauen — dabei verlöre der Mischer eine Anzeige, die er hat. Das ist eine eigene
Entscheidung und keine Texturübernahme; deshalb bleiben sie vorerst, wie sie sind.

### Zwei Sonderfälle

`block/block_meteor_molten.png` wächst von 16×16 auf 16×48. Der Port hatte die `.mcmeta` mit
`frametime: 4` von CE bereits übernommen, aber das einbildrige Original-PNG behalten — er
erklärte also eine Lauftextur mit genau einem Bild. Jetzt sind es die drei, für die die
`.mcmeta` gedacht war.

`block/solar_mirror.png` schrumpft von 36×36 auf 16×16. 36 ist keine Blockgrösse; der Port
liest die Datei nur als Partikeltextur aus dem Blockatlas (die Modellhaut des Spiegels ist
eine andere Datei unter `models/machines/`). CEs 16×16 passt dort besser.

**`.mcmeta`-Dateien werden grundsätzlich nicht mitkopiert** — die des Ports sind an 1.21
angepasst. Geprüft: bei allen vier übernommenen Lauftexturen stimmen beide Fassungen ohnehin
überein.

### Was noch offen ist

Der Dreifachvergleich ordnet über Pfadregeln zu. CE hat aber zusätzlich Unterordner
eingezogen (`gui/processing/`, `blocks/rbmk/`) und Ordner umbenannt
(`models/weapon`→`models/weapons`), und der Port hat seinerseits Dateien umbenannt
(`igniter`←`trigger`, `drink.fritz`←`bottle2_fritz`). Dadurch sind **41 weitere
CE-Überarbeitungen** nie verglichen worden — darunter die sechs Hazmat-Anzüge, `gui_centrifuge`
und drei RBMK-Partikel. Die brauchen eine Zuordnung über den Inhalt statt über den Pfad und
sind damit eine eigene Runde.

## Runde 156: einundvierzig Blöcke, die man gar nicht abbauen konnte

Beim Portieren des Big-Ass Tanks fiel im Vorbeigehen auf, dass `machine_coker` mit
`requiresCorrectToolForDrops()` angemeldet ist, aber in keinem `mineable`-Tag steht.
Nachgezählt: **einundvierzig Blöcke** sind so angemeldet — und alle einundvierzig haben eine
`dropSelf`-Tabelle, waren also sehr wohl zum Abbauen gedacht.

### Warum das nichts fallen lässt

In 1.21 entscheidet nicht mehr das Material über das richtige Werkzeug, sondern die
`Tool`-Komponente des Werkzeugs. Deren Regeln hängen ausnahmslos an einem `mineable`-Tag:

```
Player.hasCorrectToolForDrops(state)
  -> !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state)
  -> Tool.isCorrectForDrops(state)   // geht die Regeln durch
```

Steht der Block in **keinem** dieser Tags, passt keine Regel, und die Methode liefert falsch —
mit jedem Werkzeug, auch mit der Netheritspitzhacke. `ServerPlayerGameMode.destroyBlock` ruft
`playerDestroy` dann gar nicht erst auf, und die Beutetabelle bleibt tote Ladung.

Betroffen waren: die **ganze RBMK-Säule** (Brennstoffkanäle, Steuerstäbe, Moderator,
Reflektor, Absorber, Kessel, Kühler, Speicher, Heizer, Ein- und Auslass, Ausgaser, Konsolen,
Kran, Lader, alle vier Trümmersorten), **die halbe Erdölkette** (Fraktionierturm und
Abstandhalter, Reformer, Hydrotreater, Vakuumdestillation, Pyrolyseofen, Kracker, Verkoker,
Gasfackel, Verflüssiger, Verfestiger), dazu das Lichtbogenschweissgerät, die Mülltonne und die
EMP-Bombe.

### Das Tor dazu

`tools/loot-check.sh` prüfte bisher zwei Dinge — dass jeder Block ohne `noLootTable()` eine
Tabelle bekommt, und dass keiner mit `noLootTable()` eine bekommt. Jetzt prüft es ein drittes:
**wer `requiresCorrectToolForDrops()` sagt und eine Tabelle hat, muss in mindestens einem
`mineable`-Tag stehen.**

Die Prüfung sitzt hier richtig, weil sie genau dieselbe Frage stellt wie die beiden anderen:
passen Blockeigenschaft und Beutetabelle zusammen? Die Eigenschaft steht in `NtmBlocks`, der
Tag in `NtmBlockTagProvider`, die Tabelle in `NtmBlockLootTableProvider` — drei Dateien, die
niemand beim Anmelden eines Blocks alle drei im Kopf hat.

**Nachgemessen:** einundvierzig Funde vorher, null nachher. Nimmt man einen Tag-Eintrag wieder
heraus, meldet das Tor genau ihn (Exit-Code 1, direkt geprüft, nicht durch eine Pipe).

## Runde 157: was die adversariale Durchsicht der Runden 153–156 gefunden hat

Sechs unabhängige Durchsichten des Unterschieds `44fc7735..HEAD`, jede anschliessend von einer
zweiten Instanz **angegriffen** statt bestätigt. Von fünfzehn gemeldeten Befunden haben neun
die Widerlegung überstanden. Sechs sind gefallen — darunter drei angebliche Lücken im neuen
Torwächter, von denen sich eine bei eigener Nachprüfung doch als echt erwies (siehe unten).

### Ein Rückschlag, den ich selbst verursacht habe

**Der fliegende Meteorit.** `block/block_meteor_molten.png` ist in Runde 155 von 16×16 auf
16×48 gewachsen — drei Einzelbilder untereinander. Der Blockatlas wertet die `.mcmeta` aus und
animiert sauber. `RenderMeteor` aber lädt dieselbe Datei **roh** über `setShaderTexture` und
legt sie mit UV 0…1 auf einen Würfel: seit Runde 155 trug das Wurfgeschoss alle drei Bilder
übereinandergestaucht. Der Darsteller bildet jetzt nur noch das erste Einzelbild ab
(`BILD = 1F/3F`).

Das ist genau die Art Folgeschaden, nach der ich beim Übernehmen der Texturen gesucht habe —
und die Suche lief nur über die *Oberflächen*bilder, nicht über die Blocktexturen. Zwei der
208 Bilder haben die Grösse geändert; eins davon hatte einen zweiten Leser.

### Drei Fehler, die schon vorher im Baum lagen

**Der Verbrennungsmotor merkt sich seinen Sichtkasten.** `RenderCombustionEngine` hielt den
Kasten in einem Feld. `BlockEntityRenderers` legt aber je `BlockEntityType` **einen**
Darsteller an, den sich alle Motoren teilen: ab dem zweiten Motor bekam jeder den Kasten des
ersten — und verschwand, sobald der Spieler nicht zufällig auch in dessen Richtung schaute.
Dieselbe Fehlerklasse wie Runde 153, nur von der anderen Seite. Er war der einzige im Baum.

**Der Sichtkasten der FEnSU** war 4×10×4 gross, `fensu2.obj` misst aber ±3,38 / ±4,5 bei 10,25
Höhe, und der Mehrblockbau selbst ist `{9,0,2,2,4,4}`. Jetzt die Masse des Originals.

**Der Bergbaulaser** las seine Betriebslampe aus (176, 88) — dort liegt die Quelle des
Strombalkens, und die Textur ist an dieser Stelle zu 100 % durchsichtig (324 von 324 Pixeln,
nachgemessen). Die Lampe war also unsichtbar, egal ob der Laser lief. Das Original nimmt
(200, 0). Dazu wurde der Fortschrittsbalken quer statt senkrecht gezogen und las dabei in das
Nachbarsymbol hinein.

### Drei am Big-Ass Tank

**`getFluidPriority()` fehlte.** Im Puffermodus meldet sich der Tank als `NORMAL`-Empfänger am
Netz an statt als `LOW` — mit einem Bedarf von 160.000 mB/t steht er dann im selben Topf wie
eine Maschine mit 24.000 und nimmt ihr vier Fünftel des Durchsatzes weg. Das Original hat es an
`TileEntityBarrel`. **Dem Fass im Port fehlte es ebenso** — auch nachgetragen.

**`stillValid` prüfte nur noch die Entfernung.** Meine grosszügigere Fassung (16 statt 8 Blöcke,
weil der Bau dreizehn breit ist) hatte die zweite Hälfte von `Container.stillValidBlockEntity`
verloren: dass die Blockentität überhaupt noch dasteht. Die Oberfläche wäre nach dem Abriss
offen geblieben und ihr Inhalt vervielfachbar gewesen.

**Der Klassenkopf** gab den Platzbedarf mit 13×13×6 an; quer zur Blickachse sind es elf.

### Das Tor hatte zwei Löcher

**Neun Darsteller fielen still heraus.** `renderbox-check.sh` suchte nur nach
`extends BlockEntityRendererNT<X>`. Die sechs Masten und Verbinder erben über `RenderPylonBase`,
die drei Geschütze über `RenderTurretBase` — sie standen nie in der Prüfung. Die Typangabe wird
jetzt durch die Oberklassenkette verfolgt: **130 Darsteller statt 120, 92 Mehrblockbauten statt
84.** (Alle neun hatten ihren Kasten; das Loch hätte nur den nächsten Fall verschluckt.)

**Eine zweite Regel ist dazugekommen:** kein Darsteller darf einen Sichtkasten in einem Feld
merken. Nachgemessen: `RenderCombustionEngine` war der einzige; nach dem Ausbau null Funde,
mit wieder eingesetztem Feld genau einer (Exit-Code direkt geprüft).

Die Widerlegung hatte beide Löcher als „kein Befund" verworfen. Nachgezählt waren sie echt —
das ist der Grund, warum ich jeden Befund selbst nachmesse, bevor ich ihn annehme **oder**
verwerfe.

## Runde 158: 30 Blöcke und 44 Gegenstände, die in keinem Kreativreiter standen

In diesem Port sind die Reiter **handgepflegte Listen** in `NtmCreativeTabs`. Wer einen Block
anmeldet und die Zeile dort vergisst, bekommt kein Fehlerbild — der Block ist einfach nirgends
zu finden. JEI zeigt ebenfalls nur, was in einem Reiter steht. Genau das war die Beschwerde
beim Big-Ass Tank, nur dass dort der ganze Block fehlte.

Nachgezählt standen **29 Blöcke und 44 Gegenstände** in keinem Reiter, obwohl vollständig
portiert.

### Die Entscheidung je Eintrag kam aus dem Original

Nicht geraten: für jeden der 73 Einträge steht im Original, auf welchem Reiter er liegt
(`setCreativeTab(...)`) oder dass er auf keinem liegen soll (`null` oder gar kein Aufruf).
Danach sind 49 aufgenommen worden und 24 bleiben absichtlich verborgen.

**Aufgenommen** — je nach Vorlage auf dem passenden Reiter:

| Reiter | Einträge |
|---|---|
| Blöcke | Schrott, Tiefenstein, 3 Gneis-Erze, 4 Nether-Erze, Schrabidium-, Tikit- und verbranntes Uranerz, Braunkohle im Tiefenschiefer, beide ölige Sande |
| Maschinen | ICF, Sender und Empfänger der Funkfackel |
| Kernwaffen | EMP-Bombe, Entschärfer |
| Bauteile | Gehäuse |
| Steuerung | 12 ZIRNOX-Stäbe, beide PWR-Brennstoffe, zwei Schraubendreher, Lötlampe, Schweissbrenner, drei Sägeblätter, Universal-Fluidkennzeichner |
| Verbrauch | Schlüssel, Schlüsselrohling, Falschschlüssel, Stift, Plan C |

Die ZIRNOX-Stäbe sind der grösste Einzelposten: der Reaktor stand längst im Maschinenreiter,
seine zwölf Brennstäbe in keinem.

**Verborgen geblieben** sind die 24, die auch das Original nicht zeigt. Darunter — anders als
ich zunächst vermutet hatte — die **fünf Batterieblöcke**: sie tragen im Namen selbst das
Wort `LEGACY` und sind durch den Batteriesockel abgelöst. Das Original setzt sie ausdrücklich
auf `null`; sie zu zeigen wäre kein Fehlerausgleich gewesen, sondern eine Abweichung. Ebenso
`ore_bedrock` und `stone_depth`-Verwandte, die nur der Tiefbohrer abbaut, die Taint-Ausbreitung,
die ZIRNOX-Ruine, die PWR-Hülle, sieben Abbrandrückstände und die reinen Anzeigehilfen
(`nothing`, `fluid_icon`).

### Das siebzehnte Tor: `tools/tab-check.sh`

Jeder Name aus `NtmBlocks` und `NtmItems` muss in `NtmCreativeTabs` in einer Zeile mit
`output.accept` oder `addMetaItems` vorkommen. **Ein blosses Vorkommen genügt nicht** — das
Reitersymbol nennt ebenfalls einen Block, und ein Block, den nur das Symbol nennt, ist trotzdem
nirgends abzuholen. Diese Verschärfung hat sofort einen dreissigsten Fall gefunden: das
ICF-Kügelchen kommt in der Datei vor, aber nur als Bauteil von `icfPellet()` — der Reiter legt
fünf fertig bestückte Kügelchen ab, ein leeres wäre sinnlos. Das steht jetzt als begründete
Ausnahme da statt als stiller Zufallstreffer.

**Die Ausnahmeliste ist selbst geprüft:** wandert ein Eintrag später in einen Reiter oder fällt
er weg, meldet das Tor die Ausnahme als grundlos. Sonst bliebe eine Attrappe stehen.

**Nachgemessen:** 612 Blöcke, 1085 Gegenstände, 25 Ausnahmen, null Funde. Nimmt man eine
beliebige `accept`-Zeile heraus, meldet das Tor genau sie (Exit-Code direkt geprüft).

## Runde 159: die 40 Texturen, die der Pfadvergleich übersehen hatte

Der Dreifachvergleich aus Runde 155 ordnet Dateien über **Pfadregeln** zu (`items/`→`item/`,
`blocks/`→`block/`). Umbenannt haben aber beide Seiten: CE hat Unterordner eingezogen
(`gui/processing/`, `blocks/rbmk/`) und Ordner umbenannt (`models/weapon`→`models/weapons`),
und der Port hat seinerseits Dateien umbenannt (`igniter`←`trigger`,
`drink.fritz`←`bottle2_fritz`, `ivy_mike_core`←`mike_core`).

Die richtige Zuordnung geht über den **Inhalt**: Port-Datei → gleiche git-Blob-Prüfsumme im
Original → deren Originalpfad → CEs Fassung desselben Pfades. Das findet **40 weitere**
Überarbeitungen, alle bei gleicher Bildgrösse, keine mit `.mcmeta`.

**31 sind übernommen, 9 nicht.**

### Diesmal wurde nach *allen* Lesern gesucht

Das ist die Lehre aus dem Meteoriten (Runde 157): dort war nur der naheliegende Leser geprüft
worden, und der zweite — ein Partikel, der die Datei roh lädt — ging kaputt. Jede der 40
Dateien ist deshalb daraufhin abgesucht worden, wer sie liest: Atlas, `ResourceManager`,
Bildschirm, JEI-Handler, Partikelklasse, Rüstungsschicht. Das hat sich zweimal ausgezahlt:

- `gui/gui_centrifuge.png` hat **zwei** Leser — den Bildschirm und den JEI-Handler.
- `models/explosion/tom_blast.png` wird nicht als Modellhaut gelesen, sondern von
  `CloudTomParticle` roh über einen 16-teiligen Zylindermantel.

### Neun bleiben draussen

**Zwei, weil CE die Oberfläche umgebaut hat.** Beim Bagger hat CE die fünf Funktionsschalter
umsortiert — CEs Reihenfolge ist Bohrer/Seide/Brecher/Adern/Mauern, der Port hat
Bohrer/Brecher/Mauern/Adern/Seide. Die drei geänderten Bildflächen liegen genau über den
Schaltern 2, 3 und 5. Mit CEs Bild trüge der Port falsche Sinnbilder auf den Schaltern. Beim
Abfallfass hat CE die Oberfläche um 5 px verlängert und das Spielerinventar nach unten
geschoben (`ySize` 194 statt 189, Fächer bei y 112/130/148/170 statt 107/125/143/165); der Port
hält am alten Raster fest.

**Sechs Hazmat-Schichten, weil CE den Zuschnitt geändert hat.** Bei den Kopfschichten wandert
deckende Fläche: `hazmat_layer_1` **füllt** das offene Sichtfenster mit Alpha 128 — und der
Rüstungs-Rendertyp `armorCutoutNoCull` schneidet nur unter Alpha 0,1 ab und mischt nicht, das
Fenster wäre also zu. Bei den grauen und roten Fassungen vergrössert CE es umgekehrt. Bei den
Beinschichten löscht CE die komplette Unterseite des Rumpfquaders (32 Pixel bei x28–35/y16–19),
was als Loch zwischen Gürtel und Beinen auftauchen kann. Alles vermutlich CE-Absicht, aber
kein Neuanstrich.

**Eine, weil CE die Deckkraft halbiert.** `tom_blast.png` hat im Port durchgehend Alpha 255,
in CE durchgehend Alpha 128 — bei gleicher Mischung wird die Wolkenwand halb so dicht. Eine
Gestaltungsentscheidung, keine Texturübernahme.

### Dabei aufgefallen: die Zentrifuge war seit dem Portieren verrutscht

Beim Prüfen von `gui_centrifuge.png` kam heraus, dass **der Port die Datei schon vorher an den
falschen Stellen liest** — unabhängig von CE. Das Original arbeitet mit 182×189 und holt die
Füllbalken bei u = 182; der Port stand auf 176×186 und u = 176, also überall auf den
gewöhnlichen Werten.

| | Original | Port (falsch) |
|---|---|---|
| Oberflächengrösse | 182×189 | 176×186 |
| Strombalken | (8, 55−i), Quelle 182 | (9, 48−p), Quelle 176 |
| Fortschritt | (72+i·20, 57−h), Quelle 182 | (65+i·20, 50−h), Quelle 176 |
| Maschinenfächer | 44/8/70/90/110/130 bei y 57 | −8 x, −7 y |
| Spielerinventar | 11, 107 / 11, 165 | 8, 104 / 8, 162 |

Sichtbar war das als abgeschnittener rechter und unterer Rand, als Fächer, die sieben Pixel
über ihren gemalten Rahmen sassen, und als Balken, die sechs Pixel neben ihrer Mulde liefen.
Alles auf die Werte des Originals gesetzt. Dabei sind zwei weitere Lücken derselben
Abschrift mitgeschlossen worden: der **Maschinenname** wurde gar nicht gezeichnet, und die
**Aufwertungsanzeige** fehlte — die Blockentität gibt `IUpgradeInfoProvider` an und die beiden
Schächte sind da, nur sagte es niemand. Beides nutzt jetzt den `upgradeInfo`-Helfer aus
Runde 148.

## Runde 160: die Turbine verschwindet weiter — Runde 153 hat an der falschen Stelle repariert

> „die Leviathan Turbine wird immer noch unsichtbar, wenn man zu Seite schaut"

**Der Sichtkasten war die falsche Stelle.** Die Annahme aus Runde 153 lautete: Minecraft
prüft je Blockentität einen Kasten gegen den Sichtstumpf, also hilft ein grösserer Kasten.
Der erste Teil stimmt, der Schluss nicht.

Minecraft sammelt die Blockentitäten **aus den sichtbaren Chunk-Abschnitten** ein
(`LevelRenderer` läuft über `visibleSections` und zeichnet deren
`getRenderableBlockEntities()`). Fällt der Abschnitt, in dem der **Kern** steht, aus dem
Sichtstumpf, wird die Blockentität gar nicht erst angefasst. Der Kasten aus Runde 153 kommt
danach und kann nur **zusätzlich wegschneiden** — er kann nichts sichtbar machen, was die
Abschnittsprüfung schon verworfen hat.

Bei der Leviathan ist genau das der Fall: das Modell ist fünfzehn Blöcke lang und der Kern
sitzt an einem Ende. Dreht der Spieler den Kopf, liegt der Kern schnell in einem Abschnitt
ausserhalb des Bildes, während die halbe Turbine noch vor ihm steht.

### Die richtige Entsprechung heisst `shouldRenderOffScreen`

Das Original nimmt für die Turbine `TileEntity.INFINITE_EXTENT_AABB`. In 1.7.10 **war** der
Kasten der einzige Mechanismus, „unendlich" hiess dort also schlicht „immer zeichnen". Das
Gegenstück auf 1.21 ist nicht ein grosser Kasten, sondern
`BlockEntityRenderer.shouldRenderOffScreen` — damit landet die Blockentität in der Liste der
immer gezeichneten, unabhängig von der Abschnittssichtbarkeit. Genau so hält es der Port
schon bei zwölf Darstellern (den zehn Kernwaffen, dem Bombenwrack und der Sojus-Rampe) —
und deren Vorlagen sind ausnahmslos die mit `INFINITE_EXTENT_AABB`.

**Die Zuordnung lautet also: 1.7.10 `INFINITE_EXTENT_AABB` → 1.21 `shouldRenderOffScreen`.**
Das hätte schon in Runde 153 dastehen müssen.

Ergänzt bei dreizehn Darstellern: Leviathan, Raffinerie, Frackingturm, Bohrturm, Pumpjack,
Bagger, FEnSU, Dampfmaschine, Kranpult, Gasfackel, Forschungsreaktor, Turbofan und
Fusionstorus. Die Entfernung bleibt über `getViewDistance() = 256` begrenzt, wie im Original
(dort `65536`, also 256²). Die Kästen aus Runde 153 bleiben stehen: sie sind richtig portiert
und schaden nicht.

## Runde 160, zweiter Teil: unsichtbare Geräte in JEI

> „Diverse Geräte sind in JEI unsichtbar: z.B. das RBMK Terminal"

`particleOnlyBlock` gibt einem Block ein Gegenstandsmodell mit dem Elternteil
`builtin/entity`. Ein solches Modell hat **keine eigene Geometrie** — es sagt Minecraft nur:
frag den `BlockEntityWithoutLevelRenderer`. Gibt es keinen, wird nichts gezeichnet: das Feld
im Inventar ist belegt, aber leer.

Nachgezählt waren **einundzwanzig Blöcke** betroffen, aus drei verschiedenen Gründen.

### Acht RBMK-Tafeln: das Original zeichnet sie gar nicht

Terminal, Anzeige, Messuhr, Numitron, Kurvenschreiber, Hebel, Tastenfeld und Lämpchen hatten
zwar einen Gegenstandsdarsteller, aber keiner von ihnen setzte `renderInventory`.
`ItemRenderBase` verkleinert im Inventar auf **ein Sechzehntel** (`scale(0.062)`) und erwartet,
dass der Darsteller wieder hochskaliert — 114 der 121 tun das. Ohne die Zeile bleibt ein
Punkt übrig; im Bildschirmfoto sind genau solche Pünktchen zu sehen.

Statt eine Grösse zu erraten, die ich nicht nachprüfen kann: **das Original hat für diese acht
gar keinen Gegenstandsdarsteller.** Es gibt ihnen schlicht ein flaches Sinnbild —
`setBlockTextureName(":rbmk/rbmk_display")`. Genau das steht jetzt hier, über einen neuen
Helfer `particleOnlyBlockFlatItem`. Der tote BEWLR-Pfad in den sieben Darstellern ist
ausgebaut. Dasselbe gilt für die ZIRNOX-Ruine, die im Original `block_steel` als Sinnbild
trägt.

### Der grosse Radarschirm: eine vergessene Zeile

`RenderRadar` hängt an **beiden** Radaren und zeichnet für beide dasselbe Modell, nannte in
`getItemForRenderer` aber nur den kleinen. Der grosse hatte damit ein `builtin/entity`-Modell
ohne Zeichner.

### Achtzehn Maschinen haben überhaupt keinen Darsteller

Das ist der eigentliche Befund. Für diese achtzehn wurde in den Runden 115–135 der Block, die
Blockentität, das Menü und die Oberfläche portiert — **der Darsteller aber nie geschrieben**:

`machine_ammo_press`, `machine_annihilator`, `machine_autosaw`, `machine_cyclotron`,
`machine_exposure_chamber`, `machine_gas_cent`, `machine_mining_laser`, `radar_screen`,
`machine_rad_gen`, `machine_sat_link`, `machine_super_computer`, `machine_tape_drive` und die
sechs Teile des Teilchenbeschleunigers.

Sie sind deshalb **nicht nur im Inventar unsichtbar, sondern auch in der Welt**: ihr
Blockmodell trägt nur eine Partikeltextur, und `DummyableBlock` zeichnet über
`ENTITYBLOCK_ANIMATED` ausschliesslich durch den Darsteller. Wer eine davon setzt, sieht
nichts. Das ist offene Portierungsarbeit, keine Fehlbedienung.

### Das achtzehnte Tor: `tools/bewlr-check.sh`

Jedes `builtin/entity`-Modell muss jemanden haben, der es zeichnet. Die Ausnahmeliste ist hier
ausdrücklich eine **Schuldenliste**: sie nennt die achtzehn fehlenden Darsteller mit der Runde,
in der die Maschine portiert wurde. Wer einen nachreicht, streicht die Zeile — und das Tor
schlägt an, wenn eine Zeile stehen bleibt, die nicht mehr nötig ist.

**Nachgemessen:** 208 `builtin/entity`-Modelle, achtzehn auf der Schuldenliste, null
unerklärte. Nimmt man die neue Zeile aus `RenderRadar` wieder heraus, meldet das Tor genau
`MACHINE_RADAR_LARGE`.

## Runde 161: sechs der achtzehn nachgereichten Darsteller

Runde 160 hat die Schuldenliste aufgestellt: achtzehn Maschinen, für die Block, Blockentität,
Menü und Oberfläche stehen, deren Darsteller aber nie geschrieben wurde. Sie sind nicht nur in
JEI unsichtbar, sondern auch in der Welt — ihr Blockmodell trägt nur eine Partikeltextur.

Diese Runde reicht sechs davon nach:

| Maschine | portiert in | Modell | Besonderheit |
| --- | --- | --- | --- |
| `radar_screen` | Runde 126 | `radar_screen.obj` | Suchstrahl, Blips, Bildrauschen — alles in rohen Quads |
| `machine_satlink` | Runde 122 | `satlink.obj` | Schüssel dreht (`rot`) und kippt (`lift`) |
| `machine_tape_drive` | Runde 123 | `tape_drive.obj` | zwölf Schächte, Lämpchen je nach Bandsorte |
| `machine_supercomputer` | Runde 124 | `supercomputer.obj` | Leuchtband, schwarz solange nichts läuft |
| `machine_ammo_press` | Runde 125 | `ammo_press.obj` | Stempel senkt sich, Hülsenteller hebt |
| `machine_autosaw` | Runde 119 | `autosaw.obj` | dreigliedriger Arm, Sägeblatt dreht |

Das Modell des Radarschirms lag schon seit Runde 126 im Ordner; die fünf übrigen OBJ-Dateien
und ihre Texturen stammen aus `hbm-upstream/master`. Ich habe sie vorher gegen die
CE-Abspaltung gehalten: **byteweise identisch** in allen drei Bäumen (Port, Original, CE), also
gab es hier nichts zu entscheiden.

### Was aus dem Original wörtlich übernommen wurde

Die Ausrichtung, die Drehachsen, die Verschiebungen und die Werte in `renderInventory` sind
Zeile für Zeile aus 1.7.10 übertragen. Das ist wichtiger, als es klingt: `ItemRenderBase`
verkleinert im Inventar auf ein Sechzehntel und erwartet, dass der Darsteller wieder
hochskaliert — die Zahl dafür steht im Original und lässt sich hier ohne Spielstart nicht
erraten. Zwei Entwürfe dieser Runde hatten eigene Zahlen; beide sind auf die Werte des
Originals zurückgesetzt worden (SatLink `-5 / 3,5` statt `-4 / 1,5`, Bandlaufwerk `-3 / 5`
statt `-0,5 / 2,5`).

### Eine bewusste Abweichung: das Leuchtband des Grossrechners

Im Original läuft das Band `Lights` über die **GL-Texturmatrix** durch das Bild:
`glMatrixMode(GL_TEXTURE)` und dann `glTranslatef(-scroll, 0, 0)`. 1.21 kennt keine
Texturmatrix mehr; für dieselbe Wirkung müssten die Zwischenbilder einzeln erzeugt werden.
Das Band wird darum unbewegt gezeichnet — weiterhin voll erhellt und weiterhin schwarz,
solange der Rechner nichts verarbeitet, sodass der Betriebszustand am Block ablesbar bleibt.
Die Abweichung steht als Kommentar im Darsteller.

### Kein `shouldRenderOffScreen` für diese sechs

Runde 160 hat die Regel aufgestellt: `INFINITE_EXTENT_AABB` aus 1.7.10 entspricht
`shouldRenderOffScreen`, ein grosser Kasten reicht nicht. Umgekehrt gilt sie genauso — keine
der sechs Blockentitäten nimmt im Original `INFINITE_EXTENT_AABB`, alle sechs geben einen
konkreten Kasten an. Sie bekommen darum auch hier nur ihren Kasten, der bereits in den Runden
119–126 mitportiert wurde.

### Nachgemessen

`tools/bewlr-check.sh`: 208 `builtin/entity`-Modelle, **zwölf** auf der Schuldenliste (vorher
achtzehn), null unerklärte. Nimmt man die Anmeldung von `RenderSatLink` aus `ClientProxy`
wieder heraus, meldet das Tor genau `MACHINE_SAT_LINK` und endet mit 1 — Exit-Code direkt
geprüft, nicht durch eine Pipe.

Offen bleiben: Annihilator, Zyklotron, Bestrahlungskammer, Gaszentrifuge, Bergbaulaser,
Radiothermalgenerator und die sechs Teile des Teilchenbeschleunigers.

### Die Leviathan-Turbine, noch einmal nachgeprüft

Ohne Client lässt sich der Fehler nicht nachstellen, prüfbar ist aber die Kette:

1. `NtmLangProvider` gibt `MACHINE_CHUNGUS` den Namen „Leviathan Steam Turbine“ — es geht also
   wirklich um diesen Block.
2. `ClientProxy` meldet für `MACHINE_CHUNGUS` `new RenderChungus()` an. Weil
   `BlockEntityRenderers.register` einen Erzeuger nimmt, ist der tatsächlich benutzte
   Darsteller das Ergebnis von `create(Context)` — ebenfalls ein `RenderChungus`, also **mit**
   `shouldRenderOffScreen`.
3. Das Original nimmt in `TileEntityChungus.getRenderBoundingBox()` tatsächlich
   `TileEntity.INFINITE_EXTENT_AABB`. Die Entsprechung in 1.21 ist `shouldRenderOffScreen`,
   nicht ein grosser Kasten: Minecraft sammelt die Blockentitäten beim Übersetzen eines
   Chunk-Abschnitts ein und legt die mit `shouldRenderOffScreen` zusätzlich in die Liste der
   immer gezeichneten, die den Sichtbarkeitstest des Abschnitts gar nicht durchläuft.
4. Der Grund, warum es überhaupt auffällt: der Vielblock belegt nur `{3,0,0,3,2,2}`, das Modell
   ist aber **15 Blöcke lang**. Blickt man zur Seite, fällt der Abschnitt des Kerns aus dem
   Sichtstumpf, während die Blätter noch mitten im Bild stehen.
5. Die Entfernung bleibt über `getViewDistance() = 256` begrenzt, der Kasten der Blockentität
   (± 11 Blöcke) bleibt als zweite Absicherung stehen.

Was hier nicht geprüft werden kann, ist das Bild selbst. Das bleibt beim Spieltest.

## Runde 162: fünf weitere Darsteller, ein neues Tor, zwei Befunde

Nach Runde 161 standen noch zwölf Maschinen auf der Schuldenliste. Diese Runde reicht fünf
davon nach und klärt, warum eine sechste noch nicht geht.

| Maschine | portiert in | Besonderheit |
| --- | --- | --- |
| `machine_gascent` | Runde 115 | eigener Zweig des Zentrifugen-Darstellers, halbe Drehung versetzt |
| `machine_annihilator` | Runde 128 | drehende Rolle, Förderband |
| `machine_exposure_chamber` | Runde 134 | Magnetring, schwebender Kern, sechs Blitze |
| `machine_mining_laser` | Runde 118 | Kopf zielt auf den Zielblock, dreifacher Spiralstrahl |
| `machine_rad_gen` | Runde 135 | drehender Rotor, Lämpchen, durchscheinende Glashaube |

Der Bergbaulaser und der Radiothermalgenerator haben im Original **keinen**
Gegenstandsdarsteller — sie tragen im Inventar ein flaches Sinnbild. Ihre Blöcke sind darum
von `particleOnlyBlock` auf `particleOnlyBlockFlatItem` umgestellt, genau wie die RBMK-Tafeln
in Runde 160.

### Zwei, bei denen der Sichtkasten nicht reicht

`TileEntityMachineMiningLaser` und `TileEntityMachineRadGen` nehmen im Original
`INFINITE_EXTENT_AABB`. Nach der Regel aus Runde 160 heisst das in 1.21
`shouldRenderOffScreen`, nicht ein grosser Kasten — beide Darsteller haben es bekommen.

### Das neunzehnte Tor: `tools/offscreen-check.sh`

Genau hier ist die Leviathan-Turbine zweimal verschwunden. Bisher stand die Regel nur als
Kommentar in `RenderChungus`. Jetzt prüft ein Tor sie: `tools/offscreen-list.txt` nennt jeden
Darsteller, dessen Vorlage in 1.7.10 `INFINITE_EXTENT_AABB` nimmt (aus dem Original erzeugt,
der Befehl steht im Kopf der Liste — die CI hat den Fernzweig nicht, darum liegt sie als
Datei). Wer davon im Port existiert, muss `shouldRenderOffScreen` setzen.

**Nachgemessen:** 44 verschiedene Darsteller in der Liste, 17 davon gibt es im Port, alle 17
setzen die Methode — 0 Befunde. Nimmt man sie aus `RenderChungus` wieder heraus, meldet das
Tor genau `RenderChungus` und endet mit 1. Die 27 Darsteller, die es im Port noch nicht gibt,
stehen trotzdem in der Liste: das Tor greift von selbst, sobald einer geschrieben wird.

### Befund 1: das Zyklotron braucht erst seine Stecker

Der Darsteller des Zyklotrons zeichnet vier Sockel (`B1` bis `B4`) und wählt je Sockel
zwischen leerer und gefüllter Textur — `cyc.getPlug(0..3)`. Wenn alle vier stecken, dreht
sich ein Ring aus Standard-Galactic-Schrift um die Maschine.

**Diese Zustände gibt es im Port nicht.** `MachineCyclotronBlockEntity` hat weder das
`plugs`-Byte noch `setPlug`/`getPlug`, und von den vier Steckern ist nur `powder_balefire`
portiert; `book_of_`, `diamond_gavel` und `coin_maskman` fehlen. Das ist keine fehlende
Darstellung, sondern ein nicht portiertes Spielsystem aus Runde 116. Die Zeile bleibt darum
auf der Schuldenliste stehen, mit geändertem Grund.

### Befund 2: vierzehn Darsteller stehen anders herum als im Original

Beim Blick auf `RenderCentrifuge` ist mir eine abweichende Ausrichtung aufgefallen. Ich habe
daraufhin für **jeden** Darsteller mit Ausrichtungsschalter die Gesamtdrehung ausgerechnet
(Vordrehung vor dem Schalter plus Schalterwert) und gegen das Original gehalten.

**68 Paare vergleichbar: 54 stimmen überein, 14 weichen ab**, in zwei sauber getrennten
Mustern:

- **Osten und Westen vertauscht**, Norden und Süden richtig — die Maschine steht gespiegelt:
  `RenderArcFurnace`, `RenderBatteryREDD`, `RenderBlastFurnace`, `RenderCrucible`,
  `RenderExcavator`, `RenderOreSlopper`, `RenderRotaryFurnace`.
- **die ganze Zuordnung um genau −90° verdreht**: `RenderCentrifuge`, `RenderDerrick`,
  `RenderNukeFleija`, `RenderNukeGadget`, `RenderNukeN2`, `RenderNukePrototype`,
  `RenderNukeSolinium`.

Dass beide Muster so regelmässig sind — siebenmal dieselbe Spiegelung, siebenmal dieselben
−90° — spricht für zwei übernommene Fehlmuster, nicht für vierzehn Einzelfehler.

Zwei Kandidaten habe ich dabei **entlastet**: `RenderRockMill` und `RenderSolarBoiler` sehen
auf den ersten Blick abweichend aus, aber das Original dreht dort vor dem Schalter um 90°,
und der Port hat diese Drehung in den Schalter hineingerechnet. Beide sind richtig.

Dass die Zuordnung überhaupt vergleichbar ist, hängt an einem Punkt, den ich geprüft habe:
das Original setzt die Ausrichtung über `i = floor(yaw*4/360 + 0.5) & 3` auf die Gegenrichtung
des Spielers, der Port über `context.getHorizontalDirection().getOpposite()` — dasselbe. Und
keiner der vierzehn Blöcke überschreibt `getDirModified`.

Repariert ist davon **nichts** — das gehört in eine eigene Runde mit eigenem Tor, nicht
nebenbei. Für die neue Gaszentrifuge habe ich bewusst denselben Schalter genommen wie die
Schwester `RenderCentrifuge`, damit die beiden gleich stehen; bei quadratischem Grundriss von
einem Block fällt es dort ohnehin kaum auf.

### Bewusste Abweichungen

- **Annihilator:** das Förderband läuft im Original über die GL-Texturmatrix. 1.21 hat keine
  mehr (dieselbe Lage wie beim Grossrechner in Runde 161), das Band steht darum still. Die
  Rolle dreht sich weiter, man sieht der Maschine ihren Lauf also an.
- **Bergbaulaser-Textur:** die CE-Abspaltung hat `mining_laser_laser.png` überarbeitet und
  dazu eine Leuchtschicht `mining_laser_laser_e.png` samt Darstelleränderung eingeführt. Nur
  die Textur zu übernehmen wäre falsch — hier steht die Fassung des Originals. Alle anderen
  Modelle und Modelltexturen dieser Runde sind in Port, Original und CE byteweise identisch;
  `gascent.png` und `radgen.obj`/`radgen.png` heissen in CE nur anders
  (`centrifuge_gas.png`, `models/radgen.obj`, `rad_gen_body.png`), der Inhalt stimmt überein.
  Die beiden flachen Sinnbilder `machine_mining_laser.png` und `machine_radgen.png` gibt es in
  CE unter keinem Namen und mit keinem Inhalt — für sie gab es also nichts zu vergleichen.

### Nachtrag zu Runde 162: ein Compile-Fehler in der CI, und warum kein Tor ihn fand

Der erste Anlauf ist rot geworden — eine Zeile:

```
ResourceManager.java:821: error: incompatible types:
    HFRWavefrontObject cannot be converted to IModelCustom
```

Ich hatte das Laden des Radiothermalgenerators wörtlich aus dem Original übernommen. Dort
steht es als einziges Modell ohne `asVBO()`, und das ist dort auch richtig: in 1.7.10
implementiert die Laderklasse `IModelCustom` selbst. In diesem Port tut sie das nicht — erst
`asVBO()` liefert ein `IModelCustom`, `getRenderer()` einen `IObjRenderer`. Die
Unterscheidung, die ich treu übertragen wollte, gibt es hier also gar nicht. Behoben mit
`.asVBO()` und einem Kommentar, der genau das festhält.

**Interessanter ist, warum alle neunzehn Tore grün waren.** `syntax-check.sh` führt
`incompatible types` in seiner Ausnahmeliste — pauschal, weil solche Meldungen normalerweise
Folge der fehlenden Minecraft-API sind. Mein erster Reparaturversuch war deshalb, die Regel
zu verfeinern: melden, wenn **beide** genannten Typen aus `src/main/java` stammen, denn dann
kann die fehlende Fremd-API nichts damit zu tun haben.

Das habe ich gemessen, und es half nicht: mit dem Fehler im Baum erzeugt der Offline-javac zu
dieser Zeile **überhaupt keine Meldung**, und im ganzen Baum steht `cannot be converted to`
null mal. Sobald ein Typ wegen der fehlenden API fehlerhaft ist, unterdrückt javac die
Folgeprüfungen — dieselbe Mechanik, an der in Runde 121 schon die doppelt erklärten
Konstanten vorbeikamen. Eine Regel, die in beiden Richtungen 0 misst, darf nicht ausgeliefert
werden; der Versuch ist wieder raus.

Stattdessen, wie damals, ein **Textdurchgang**: für jedes Feld in `ResourceManager` vom Typ
`IModelCustom` oder `IObjRenderer` muss eine Zuweisung aus `new HFRWavefrontObject(...)` auf
`.asVBO()` beziehungsweise `.getRenderer()` enden.

**Nachgemessen:** 0 Befunde im sauberen Baum; setzt man das `.asVBO()` hinter `radgen` wieder
ab, meldet das Tor genau diese Zeile und endet mit 1 — Exit-Code direkt geprüft.

## Runde 163: die zwei offenen Punkte aus Runde 162

### Punkt 1: fünfzehn Darsteller standen anders herum als im Original

Runde 162 hatte den Befund gemessen, aber nichts repariert. Jetzt ist er zu — mit einem Tor,
das ihn festhält.

**`tools/facing-check.sh`, das zwanzigste Tor.** `tools/facing-list.txt` nennt für jeden
Darsteller die erwartete **Gesamtdrehung** je Blickrichtung, aus dem Original erzeugt. Das Tor
rechnet die des Ports nach und vergleicht. Gesamtdrehung heisst: eine Y-Drehung *vor* dem
Schalter zählt mit — manche Darsteller haben die Vordrehung des Originals in den Schalter
hineingerechnet (`RenderRockMill`, `RenderSolarBoiler`), und das ist richtig.

**Das Tor hat sich selbst einen Fehler nachgewiesen.** Seine erste Fassung las nur eine von
drei Schreibweisen des Schalters — die mit vier ausgeschriebenen Fällen. Der Port schreibt ihn
aber auch mit drei Fällen plus `default`, und als *Ausdruck*, der die Gradzahl liefert; der
Schalterkopf ist mal eine Variable, mal ein Ruf (`switch(getFacing(be))`). 15 Darsteller
wurden dadurch **stillschweigend übersprungen** — der schlimmste Fehler, den ein Torwächter
machen kann, weil er falsche Sicherheit gibt. Nach der Erweiterung waren es 81 von 81
vergleichbar, und darunter kam ein **fünfzehnter** Fall zum Vorschein: `RenderFusionCoupler`,
dem der Port die 90°-Vordrehung des Originals schlicht unterschlagen hatte. Das Tor meldet
seither jeden Schalter, den es nicht lesen kann, als eigenen Befund.

Begradigt wurden damit:

- **Osten und Westen vertauscht** (die Maschine stand gespiegelt): `RenderArcFurnace`,
  `RenderBatteryREDD`, `RenderBlastFurnace`, `RenderCrucible`, `RenderExcavator`,
  `RenderOreSlopper`, `RenderRotaryFurnace`
- **um genau −90° verdreht**: `RenderCentrifuge`, `RenderDerrick`, `RenderNukeFleija`,
  `RenderNukeGadget`, `RenderNukeN2`, `RenderNukePrototype`, `RenderNukeSolinium`
- **Vordrehung fehlte**: `RenderFusionCoupler`

Geändert haben sich nur Zahlen — 42 Zeilen in 14 Dateien, plus der Fusionskoppler.

**Nachgemessen:** vor der Begradigung genau 15 Befunde, danach 0, 0 unlesbar. Dreht man
`RenderCentrifuge` wieder zurück, meldet das Tor genau `RenderCentrifuge` und endet mit 1.

### Punkt 2: das Zyklotron und seine vier Stecker

Runde 162 musste es zurückstellen: sein Darsteller liest vier Sockel über `getPlug()`, und das
Steckersystem gab es im Port gar nicht. Jetzt ist beides da.

**Das Spielsystem.** `MachineCyclotronBlockEntity` bekommt das `plugs`-Byte samt NBT und
Synchronisierung, dazu `setPlug`, `getPlug` und `getItemForPlug`. `MachineCyclotronBlock`
bekommt ein `useItemOn`: hält der Spieler den passenden Gegenstand, wandert er in den Sockel
und die Oberfläche geht **nicht** auf — so steht es im Original. Einmal gesteckt, bleibt er
drin; ein Herausnehmen kennt auch das Original nicht.

**Die drei fehlenden Stecker** sind nachgezogen — `powder_balefire` stand schon:

| Gegenstand | im Original | im Port |
| --- | --- | --- |
| `book_of_` | `ItemBook`, öffnet eine Lesemaske | einfacher Gegenstand mit seinem Spruch |
| `diamond_gavel` | `WeaponSpecial`, nimmt ein Drittel der Höchst-LP | eigenes `SpecialSwordItem`-Lambda, gleiche Wirkung |
| `coin_maskman` | `ItemCustomLore`, selten | einfacher Gegenstand, Seltenheit *uncommon* |

Der Schlag des Hammers braucht den Klang `weapon.whack`; er ist aus dem Original übernommen
und als `NtmSoundEvents.WEAPON_WHACK` angemeldet. Die Lesemaske des Buchs (`GUIBook`,
`ContainerBook`) ist **nicht** portiert — für den Sockel wird sie nicht gebraucht, und sie ist
ein eigenes Stück Arbeit.

**Der Darsteller.** `RenderCyclotron` zeichnet den Körper und je Sockel eine von zwei Texturen,
leer oder gefüllt. Stecken alle vier, dreht sich ein Ring aus Standard-Galactic-Schrift um die
Maschine: *plures necat crapula quam gladius*. Im Original macht das der
`standardGalacticFontRenderer`; in 1.21 ist es dieselbe Schrift über
`Style.withFont(minecraft:alt)` und `Font.drawInBatch`. Kein Gegenstandsdarsteller — das
Zyklotron trägt im Inventar wie im Original ein flaches Sinnbild.

### Was dabei offen bleibt, und ehrlich benannt

`book_of_` und `diamond_gavel` stehen wie im Original **in keinem Kreativreiter**. Im Original
macht das nichts: das Buch gibt es über ein verstecktes Bobmazon-Angebot, den Hammer über
`MagicRecipes`. **Beide Bezugswege sind im Port nicht portiert** — und damit sind diese zwei
Gegenstände im Überleben derzeit unerreichbar, und mit ihnen zwei der vier Sockel. Das
Steckersystem selbst ist vollständig und nimmt jeden der vier Gegenstände an, woher auch immer
er kommt. Der Hinweis steht als Warnung im Kopf von `tools/tab-check.sh`, damit die beiden
Ausnahmen dort nicht als „alles in Ordnung" gelesen werden.

Damit ist die Schuldenliste aus Runde 160 von achtzehn auf **sechs** geschrumpft; übrig sind
nur noch die sechs Teile des Teilchenbeschleunigers.

## Runde 164: die RBMK-Konsole war ein Pünktchen — und einundvierzig andere Bilder stimmten auch nicht

Gemeldet wurde: *„die RBMK Konsole ist in JEI als sehr kleines Vorschaubild vorhanden."*

Die Ursache ist die aus Runde 160 bekannte: `ItemRenderBase` verkleinert im Inventar auf ein
Sechzehntel (`scale(0.062)`) und erwartet, dass der Darsteller wieder hochskaliert. Die
Konsole stand auf `scale(0.35)` statt auf den **2,5** des Originals — also bei rund zwei
Prozent. Das ist kein Bild mehr, das ist ein Punkt.

### Der eigentliche Fund: es waren einundvierzig

Statt die eine Zahl zu berichtigen, habe ich **alle** Inventarbilder gegen das Original
gehalten. Von 117 vergleichbaren Paaren wichen **41** ab. Sieben davon waren so klein, dass
man sie nicht sehen konnte:

| Darsteller | war | Original |
| --- | --- | --- |
| RBMK-Konsole | 0,35 | 2,5 |
| Chekhov-Geschütz, Freundliches Geschütz | 0,3125 | 4 |
| Howard-Geschütz, beschädigt | 0,25 | 4 |
| Jeremy-Geschütz | 0,3125 | 2,5 |
| Wachgeschütz | 0,4375 | 7 |

Die übrigen 34 waren milder — um 20 bis 100 % zu klein, zu groß oder verschoben. Alle 41
tragen jetzt die Zahl des Originals.

### Warum kein Tor das gefunden hat, und warum ich zweimal falsch gemessen habe

Das Tor aus Runde 160 (`bewlr-check`) prüft nur, *ob* ein `builtin/entity`-Modell einen
Zeichner hat — nicht, *womit* er zeichnet. Die Zahl war nie geprüft.

Beim Nachmessen bin ich zweimal in dieselbe Falle gelaufen, und beide Male hat die Messung
selbst es gezeigt:

1. **Erster Versuch:** ich habe nur nach `IItemRendererProvider` gesucht und kam auf 31
   Darsteller, die der Port „erfunden" habe. Das war falsch — das Original hat einen
   **zweiten** Anmeldeweg, `ItemRenderLibrary`, eine Tabelle mit 79 Einträgen. Nach dem
   Einrechnen blieben statt 31 nur noch drei.
2. **Vier vermeintliche Befunde** (`RED_CONNECTOR`, `RED_CONNECTOR_SUPER`, Bandlaufwerk,
   Watz-Pumpe) waren Auslesefehler: das Original schreibt die Größe dort über eine
   Hilfsvariable (`double scale = 5; glScaled(scale, scale, scale)`), die mein Muster nicht
   traf. Eine Liste mit falschen Sollwerten wäre schlimmer als keine — das Auslesen kann das
   jetzt.
3. **Fünf weitere** waren Fehlalarme an `RenderLandmine`: der Darsteller bedient fünf Minen
   und verzweigt korrekt je Gegenstand, mein Auslesen las nur den ersten Zweig. Das Tor liest
   jetzt Verzweigungen.

### Eine Regression von mir, aus den Runden 162 und 163

Derselbe erste Denkfehler hat mich in Runde 162/163 dazu gebracht, **Zyklotron, Bergbaulaser
und Radiothermalgenerator** für darstellerlos zu halten und ihnen ein flaches Sinnbild zu
geben. Sie stehen alle drei in `ItemRenderLibrary`. Zurückgenommen: alle drei haben wieder
einen Gegenstandsdarsteller, mit den Zahlen und dem Aufbau des Originals — beim
Radiothermalgenerator leuchtet das Lämpchen im Inventarbild grün, beim Zyklotron sind die
vier Sockel leer, beim Bergbaulaser liegt der Kopf waagerecht.

### Das einundzwanzigste Tor: `tools/inventory-check.sh`

`tools/inventory-list.txt` nennt für jeden Block die `renderInventory`-Werte des Originals,
aus **beiden** Anmeldewegen erzeugt. Das Tor rechnet die des Ports nach und weist auf zu
kleine (`winziges Pünktchen`) und zu große (`läuft aus dem Rahmen`) Bilder eigens hin.

**Nachgemessen:** 199 Einträge im Original, 117 Paare vergleichbar, vorher 41 abweichend,
jetzt 0. Setzt man die Konsole wieder auf `scale(0.35)`, meldet das Tor genau `RBMK_CONSOLE`
mit dem Zusatz „winziges Pünktchen" und endet mit 1.

### Zur Unschärfe im selben Bericht

Das Bildschirmfoto zeigt die Oberfläche weich gezeichnet. Die Textur ist es nicht: sie ist
byteweise die der CE-Abspaltung, palettiert und damit verlustfrei, 256×256 wie das Original.
Auffällig ist, dass der Fenstertitel „Display 1: none" genauso weich ist — den zeichnet
Minecraft selbst, nicht unsere Textur. Das spricht dafür, dass das Bild vergrößert wurde und
nicht das Spiel unscharf zeichnet. Falls es im Spiel wirklich weich aussieht, wäre es ein
eigener Fund; dann bitte noch einmal melden.

## Runde 165: die RBMK-Säulen — Rohrstutzen und Deckelplatte

Zwei Meldungen, eine Wurzel:

> *„Die automatic control rods sind teilweise schwarz und teilweise oben einfach flach"*
> *„die Abdeckplatten für den Reaktor werden nicht oben drauf platziert sondern ersetzen nur die obere Textur"*

Beide stimmen, und beide kommen daher, dass das Original die RBMK-Säulen **nicht** mit
gewöhnlichen Würfelmodellen zeichnet, sondern mit drei eigenen Blockzeichnern
(`RenderRBMKRod`, `RenderRBMKControl`, `RenderRBMKReflector`). Die setzen auf den **obersten**
Block einer Säule etwas in den Blockraum **darüber** — jenseits des eigenen Würfels:

```java
if(!hasLid) {                                     // Rohrsäule ohne Deckel
    renderer.setRenderBounds(0.0625, 0, 0.0625, 0.4375, 0.125, 0.4375);
    renderer.renderStandardBlock(block, x, y + 1, z);   // viermal, in den vier Ecken
}
```

```java
if(lid != RBMKBase.LID_NONE) {                    // mit Deckel
    renderer.setRenderBounds(0, 0, 0, 1, 0.25, 1);
    renderer.renderStandardBlock(block, x, y + 1, z);
}
```

Also: **vier Stutzen zu je 6×2×6 in den Ecken**, oder **eine Platte über die volle Fläche,
vier Pixel hoch** — beides einen Block höher als die Säule selbst, und beides schließt
einander aus.

Der Port hatte weder das eine noch das andere. Oben blieb eine glatte Fläche (daher „einfach
flach"; das Schwarze ist das nackte `_top`-Bild ohne die Aufbauten drumherum), und der Deckel
war eine Umtexturierung der obersten Säulenscheibe statt einer Platte darüber.

### Was jetzt da ist

In 1.21 braucht das keinen eigenen Blockzeichner: ein Blockmodell darf über seinen Würfel
hinausragen. Die Modelle sind entsprechend neu gebaut — `rbmkPipes` für die Stutzen,
`rbmkLid` für die Platte, beide über dem Würfel bei y = 16.

Damit ein Modell überhaupt weiß, dass es oben sitzt, hat `RBMKBaseBlock` eine neue Eigenschaft
**`TOP`** bekommen. Im Original entscheidet das die Metadaten-Spanne (`meta >= 6 && meta < 12`)
zur Zeichenzeit; in 1.21 muss es am Blockzustand hängen, weil die Modelle datengetrieben sind.
Gesetzt wird sie beim Bauen der Säule — und zusätzlich in `updateShape`, damit **Säulen aus
bestehenden Spielständen sich selbst richten**, sobald sich über ihnen etwas rührt, statt neu
gebaut werden zu müssen.

Dazu zwei Abfragen, die die Klassenhierarchie des Originals abbilden:

- **`hasPipes()`** — die vier `RBMKPipedBase`-Klassen: Steuerstab, selbsttätiger Steuerstab,
  Boiler, Heizer. Das sind sieben Blöcke.
- **`hasOwnLid()`** — die fünf Steuerstab-Säulen. Sie nehmen keinen Deckel an und zeigen nie
  eine Deckeltextur; sie tragen also immer ihre Stutzen. Das hatte der Port schon richtig.

Die **vierzehn fehlenden `_pipe_*`-Texturen** sind aus dem Original übernommen, alle vierzehn
byteweise identisch mit der CE-Abspaltung.

### Das zweiundzwanzigste Tor: `tools/rbmk-check.sh`

`tools/rbmk-list.txt` nennt die sieben Rohrsäulen des Originals. Das Tor prüft, dass jede im
Port `hasPipes()` meldet, dass keine es meldet, die es im Original nicht ist, und dass jede
ihre beiden Rohrbilder hat.

**Nachgemessen:** 7 Rohrsäulen, 0 Befunde. Nimmt man `hasPipes()` aus `RBMKHeaterBlock`
heraus, meldet das Tor genau `rbmk_heater` und endet mit 1.

### Was hier noch offen bleibt

Die Brennstoffkanäle bekommen im Original zusätzlich eine OBJ-Auflage (`rbmk_element`, Teile
„Cap" und „Inner", gezeichnet über `ObjUtil.renderPartWithIcon`). Die fehlt weiterhin, und mit
ihr elf weitere Texturen (`rbmk_element*`, `rbmk_control_base`, die beiden
`rbmk_control_reasim*_bottom`). Das ist ein eigener Schritt — er braucht den OBJ-Pfad im
Blockmodell, nicht nur ein paar Kästchen mehr.

## Runde 166: der Leviathan-Hebel las die Ausrichtung am falschen Block

Gemeldet waren zwei Dinge. Eines ist ein Fehler, eines nicht — und das getrennt zu halten war
der eigentliche Teil der Arbeit.

### Die Turbine liess sich nicht auf ultradichten Dampf umstellen — Fehler, behoben

Der Verdichter wird über einen Hebel weitergeschaltet, und der Hebel ist kein eigener Block:
`MachineChungusBlock` rechnet aus der Aufstellrichtung aus, **welche** Stelle der Säule als
Hebel gilt. Diese Richtung las der Port aus dem **angeklickten** Block:

```java
Direction dir = state.getValue(FACING);        // falsch
```

Die Hilfsblöcke eines Vielblocks tragen in `FACING` aber die Richtung **zum Kern hin**
(`MultiblockHandlerXR.fillSpace` setzt sie je nach Lage auf UP/DOWN/NORTH/…), nicht die
Aufstellrichtung der Maschine. Nur der Kern trägt die. Damit lag die errechnete Hebelstelle
fast immer daneben; je nachdem, welchen Block man erwischte, ging mal ein Schritt und dann
nichts mehr — genau das Bild, das gemeldet wurde. Das Original nimmt hier die Metadaten des
Kerns (`entity.getBlockMetadata()`), und die Industrieturbine im Port macht es auch richtig.

```java
Direction dir = be.getBlockState().getValue(FACING);   // richtig, be ist der Kern
```

Die Schaltkette selbst (Dampf → heiss → überhitzt → ultradicht → zurück) stimmt mit dem
Original Zeile für Zeile überein und war nie das Problem.

### Das dreiundzwanzigste Tor: `tools/dummyfacing-check.sh`

Das ist eine Fehlerart, kein Einzelfall: jeder Vielblock, der beim Anklicken etwas aus der
Aufstellrichtung berechnet, kann sie am falschen Block holen. Das Tor durchsucht alle Blöcke,
die von `DummyableBlock` erben, nach `state.getValue(FACING)` innerhalb einer
`use…`-Methode.

**Nachgemessen:** 109 Vielblock-Blöcke, **1 Befund** vor der Berichtigung
(`MachineChungusBlock`), **0** danach. Gegen die Fassung aus dem vorigen Commit gehalten
meldet die Erkennung genau diese eine Datei.

### Der Big-Ass Tank sinkt ein — kein Fehler, sondern der Untergrund

Der Tank hat einen Kipp-Zustand: `checkTilt(TiltType.UNAVOIDABLE, true)`. `UNAVOIDABLE` heisst,
dass er **unabhängig von jeder Einstellung** prüft, worauf er steht — so steht es auch im
Original. Geprüft werden sechzehn Blöcke unter der Grundfläche, einer alle 20 Ticks; nach rund
sechzehn Sekunden ist die Runde durch. Fallen mehr als fünf Prozent durch, kippt er: zehn Grad
Neigung und einen Block tiefer. Das sieht aus, als sänke er in den Boden.

Für „extra schwere" Maschinen verlangt die Prüfung einen Untergrund mit mindestens der
Sprengfestigkeit von Stein. **Sand hat 0,5, Stein hat 6.** Auf dem Strand im Bildschirmfoto
kippt der Tank also zu Recht — im Original genauso, dort zusätzlich über eine ausdrückliche
Abfrage auf `Material.sand`.

Ich habe die Prüfung Zeile für Zeile gegen das Original gehalten: Kipp-Bedingung,
Zähl-Takt, Fünf-Prozent-Schwelle, die sechzehn Bodenstellen (`standardFloor7x7`) und die
beiden Einstellungen `enableMachineGravity` (Standard aus) und `enable528MachineGravity`
(Standard an, aber 528 selbst ist aus) — alles deckungsgleich. **Der Tank braucht ein
Fundament aus Stein, Beton oder Ähnlichem.**

Eine Kleinigkeit ist dabei doch aufgefallen und behoben: der Einstellungsschlüssel hiess
`"enableMachineGravity "` — mit einem Leerzeichen am Ende, auch im Übersetzungsschlüssel. In
der Konfigurationsdatei steht er damit als `"enableMachineGravity " = false` und ist schwer zu
finden. Wer ihn schon angefasst hat, muss ihn nach diesem Update einmal neu setzen.

Offen und ausdrücklich nicht übernommen: die Material-Abfragen des Originals (`Material.sand`,
`cloth`, `ground`) stehen im Port als `// todo materials`. In der Sache ändert das hier nichts
— Sand fällt schon über die Sprengfestigkeit durch —, aber es macht den Port an anderer
Stelle nachgiebiger als das Original.

## Runde 167: der Brennstoffkanal ist ein Rohr, kein Würfel

Der letzte offene Punkt aus Runde 165. Das Original zeichnet die Brennstoffkanäle so:

```java
rod.overrideOnlyRenderSides = true;
renderer.renderStandardBlock(block, x, y, z);          // NUR die vier Seiten
rod.overrideOnlyRenderSides = false;
ObjUtil.renderPartWithIcon(rbmk_element, "Cap",   block.getIcon(0, meta), ...);
ObjUtil.renderPartWithIcon(rbmk_element, "Inner", rod.inner, ...);
```

Der Block selbst hat also **keine Deck- und keine Bodenfläche**; dort liegen Kappe und
Innenrohr aus `rbmk_element.obj`. Genau das macht aus dem Kanal ein Rohr statt eines
Klotzes — und genau das fehlte, weshalb die Kanäle oben flach und dunkel aussahen.

### Beides gehört zusammen

Das Blockmodell des Kanals (`rbmkTube`) hat jetzt nur noch die vier Seitenflächen, und
`RenderRBMKFuelChannel` legt Kappe und Innenrohr über die ganze Säulenhöhe darüber. Eines ohne
das andere wäre schlimmer als vorher: ohne Auflage sähe man in den Block hinein, ohne die
entfernte Deckfläche stießen Fläche und Kappe auf derselben Höhe zusammen. Deshalb prüft das
Tor beides.

Der Darsteller zeichnet die Auflage jetzt **immer**. Vorher stieg er gleich zu Beginn aus,
wenn kein Brennstab steckte und der Fluss klein war — für das Stabbündel und das Tscherenkow-
Leuchten ist das richtig, für die Kappe nicht.

Jeder Kanal bringt seine eigenen Bilder mit, über `getTextureBase()` am Block: `rbmk_element`,
`rbmk_element_mod`, `rbmk_element_reasim`, `rbmk_element_reasim_mod` — davon abgeleitet `_top`
für die Kappe und `_inner` für das Rohr, wie im Original `block.getIcon(0, meta)` und
`RBMKRod.inner`.

Auch der Deckel sitzt beim Kanal auf dem Rohrkörper, nicht auf einem Würfel: sonst stieße
seine Deckfläche mit der Kappe zusammen, die auf genau derselben Höhe endet (der OBJ-Teil
„Cap" reicht von y 0 bis 1).

### Übernommen

`rbmk_element.obj` (Teile Cap, Inner, Rods) und acht Texturen. Das Modell und die beiden
`rbmk_element`-Bilder sind byteweise gleich mit der CE-Abspaltung; die sechs übrigen
(`_mod_fuel`, `_mod_inner`, `_reasim_*`) **gibt es in CE gar nicht**, dort stand also nichts
zum Vergleichen — sie kommen unverändert aus dem Original.

### Das Tor mitgewachsen

`tools/rbmk-check.sh` prüft jetzt zusätzlich, dass jede der vier Kanalsorten ihr Kappen- und
ihr Innenbild hat.

**Nachgemessen:** 7 Rohrsäulen, 4 Kanalsorten, 0 Befunde. Nimmt man `rbmk_element_inner.png`
weg, meldet das Tor genau dieses Bild und endet mit 1.

### Damit ist die RBMK-Säule vollständig

Rohrstutzen (Runde 165), Deckelplatte (165) und Rohrform der Kanäle (167) sind da. Von den 25
RBMK-Texturen, die dem Port fehlten, sind jetzt 22 übernommen; übrig bleiben drei
(`rbmk_control_base`, `rbmk_control_reasim_bottom`, `rbmk_control_reasim_auto_bottom`), die im
Original zur Bodenplatte der Steuerstäbe gehören — ein eigener, kleiner Schritt.

## Runde 168: die Schuldenliste ist leer

Zwei offene Punkte auf einmal — der Rest der RBMK-Säule und die sechs Teile des
Teilchenbeschleunigers.

### Der RBMK-Rest: die Bodenplatte der ReaSim-Steuerstäbe

Von den drei übrig gebliebenen Texturen ist **eine gar keine Aufgabe**: `rbmk_control_base.png`
liegt im Original im Ordner, wird aber von keiner einzigen Stelle im Quelltext benutzt — eine
verwaiste Datei. Sie ist auch hier nicht übernommen.

Die anderen beiden gehören zusammen. Im Original zeigen **nur** die beiden ReaSim-Steuerstäbe
auf ihrer **Unterseite** ein eigenes Bild statt der üblichen Deckfläche — dort sitzt ihr
Stromanschluss, denn diese Bauform fährt mit Strom von unten:

```java
if(this.renderLid == LID_NONE && this == ModBlocks.rbmk_control_reasim && side == 0)
    return textureBottom;
```

Im Port ist das jetzt `hasOwnBottom()` am Basisblock, wahr genau dann, wenn der Steuerstab
`powered` ist — das ist im Port schon die Kennzeichnung der ReaSim-Bauformen. Die Modelle
nehmen die Unterseite entsprechend.

Zwei Kleinigkeiten dabei: `RBMKControlAutoBlock` hatte die Abfrage doppelt, die Oberklasse
deckt sie ab — raus damit. Und das Tor prüft jetzt mit, dass jede so gekennzeichnete Säule ihr
`_bottom`-Bild hat.

### Die sechs Teile des Teilchenbeschleunigers

| Teil | Besonderheit |
| --- | --- |
| Teilchenquelle | — |
| Strahlrohr | zwei Bauformen; mit Fenster leuchtet die Scheibe auf, wenn ein Teilchen durchfliegt |
| Hohlraumresonator | — |
| Quadrupolmagnet | — |
| Dipolmagnet | wird als einziger **nicht** nach der Aufstellrichtung gedreht — er steht rund |
| Detektor | sitzt zwei Blöcke tiefer als sein Kern |

Alle sechs standen seit Runde 133 mit Block, Blockentität und Oberfläche, aber ohne
Darsteller — sie waren in der Welt wie im Inventar unsichtbar.

**Eine Vorlage weicht ab:** `beamline.obj` ist in der CE-Abspaltung ein Rumpf von 44 Zeilen mit
einem einzigen Teil `Cube_Cube.001`; im Original sind es 184 Zeilen mit `Beamline`,
`BeamlineWindow` und `BeamlineGlass`. CE hat die Teile also verloren, nicht überarbeitet — hier
steht die Fassung des Originals. Die übrigen elf Dateien sind in Port, Original und CE
byteweise identisch.

### Damit ist die Schuldenliste aus Runde 160 leer

Achtzehn Maschinen standen darauf, für die Block, Blockentität, Menü und Oberfläche portiert
waren, der Darsteller aber nie geschrieben wurde. Über die Runden 161, 162, 163 und 168 sind
alle achtzehn nachgereicht.

`tools/bewlr-check.sh` misst jetzt **208 `builtin/entity`-Modelle, null auf der Schuldenliste,
null unerklärte.** Die Liste selbst bleibt als Gestell stehen: wer künftig eine Maschine
portiert und ihren Darsteller schuldig bleibt, trägt sie dort mit Rundennummer ein, statt das
Tor abzuschalten. Nimmt man die Anmeldung von `RenderSatLink` heraus, meldet das Tor weiterhin
genau `MACHINE_SAT_LINK`.

## Runde 169: die beiden Kühltürme

### Vorweg: zwei `// todo materials` in der Standprüfung

Beim Nachrechnen des einsinkenden Big-Ass Tanks fielen in `LoadedBaseBlockEntity.checkTilt`
zwei offene Stellen auf. Das Original prüft dort die 1.7.10-Materialien `Material.sand`,
`Material.cloth` und `Material.ground`; die gibt es in 1.21 nicht mehr, und der Port hatte die
Prüfung schlicht ausgelassen. Ein Fundament aus Wolle oder Erde trug damit eine schwere
Maschine, was es im Original nicht tut.

Die Entsprechung sind die Sammelbegriffe: `BlockTags.SAND`, `BlockTags.WOOL`, `BlockTags.DIRT`
und Kies. Für die leichteren Maschinen kam zusätzlich die im Original auskommentiert
mitgelieferte Zeile dazu — toter Boden, öliger Boden und rissiger Stein taugen nicht.

Am gemeldeten Verhalten des Tanks ändert das nichts: der sank auf Sand ein, und Sand war schon
vorher ausgeschlossen. Das ist so gewollt, siehe Runde 167.


Auf die Frage, ob es die Kühltürme schon gibt, lautete die Antwort: nein. Jetzt gibt es sie —
`machine_tower_small` und `machine_tower_large`. Beide rechnen dasselbe wie der Kondensator
(Abdampf zurück zu Wasser), brauchen aber keinen Strom; die Rechnung steht unverändert in
`CondenserBaseBlockEntity`, die Türme setzen nur Tankgröße, Bauform und Anschlüsse.

| | kleiner Turm | großer Turm |
| --- | --- | --- |
| Höhe / Grundriss | 18 hoch, 5×5 | 12 hoch, 9×9 |
| Tanks | 1.000 / 1.000 | 10.000 / 10.000 |
| Anschlüsse | 4, drei Blöcke vom Kern | 12, fünf Blöcke vom Kern |
| Dampffahne | oben aus dem Schlot, y+18 | aus dem offenen Becken, y+1 |

**Die zwölf Anschlüsse des großen Turms** waren beim ersten Anlauf vier — ich hatte die Form
des kleinen Turms übernommen. Das Original legt sie anders: je Himmelsrichtung drei Stellen,
fünf Blöcke vom Kern entfernt und dort mittig sowie je drei Blöcke nach beiden Seiten versetzt.
Dazu gehört, dass `fillSpace` die Hülle an denselben zwölf Stellen vier Blöcke vom Kern setzt
und `getOffset()` **4** ist, nicht 2 — mit 2 wäre der Turm beim Aufstellen um zwei Blöcke
verrutscht. Alle drei Zahlen stehen jetzt so wie im Original.

**Die Dampffahne** fehlte zunächst ganz. Sie hängt an `waterTimer`, den die Basis schon
synchronisiert, und braucht einen clientseitigen Zweig — den hat `CondenserBaseBlockEntity`
nicht, weil `updateEntity` dort vollständig im Server-Zweig liegt. Beide Türme überschreiben
`updateEntity` jetzt und setzen den Effekt nach den Werten des Originals: der kleine alle zwei
Ticks vom Schlot aus (`lift` 1, `base` 0.5, `max` 4, Lebensdauer 250–500), der große alle vier
Ticks aus dem Becken, über drei Blöcke Breite gestreut (`lift` 0.5, `base` 1, `max` 10,
Lebensdauer 750–1000). Die Partikelsorte heißt im Port `COOLING_TOWER` statt `"tower"`; dazu
kam die Einstellung `coolingTowerParticles`, die das Original als `COOLING_TOWER_PARTICLES`
führt.

**Eine Texturvorlage stammt aus der CE-Abspaltung:** `tower_small.png` ist dort eine echte
Überarbeitung, `tower_large.png` und beide `.obj` sind in Port, Original und CE identisch.

### Zwei Tore haben dabei zugeschlagen

`tools/dist-check.sh` fand `printHook` in beiden Turmblöcken ohne `@OnlyIn(Dist.CLIENT)` —
`RenderGuiEvent` gibt es auf dem Server nicht, das Laden der Klasse wäre dort abgebrochen.

`tools/inventory-check.sh` meldete am großen Turm `ist 3.8 | soll 4`. Das war **kein Fehler im
Port, sondern in der Liste**: das Original schreibt `glScaled(4 * 0.95, …)`, und der Generator
der Sollwerte las nur die erste Zahl. Er wertet solche Produkte jetzt aus; `tools/inventory-list.txt`
ändert sich dadurch in genau einer Zeile. Eine Liste mit einem Wert, von dem ich weiß, dass er
falsch ist, wird nicht ausgeliefert — dieselbe Regel wie in Runde 164.

## Runde 170: das schwarze Buch und die Hammerkette

Nach Runde 169 waren als offene Punkte noch `book_of_` und `diamond_gavel` gemeldet: beide
waren registriert, aber im Überlebensmodus **nicht zu bekommen**, und mit ihnen zwei der vier
Zyklotron-Sockel. Diese Runde schließt das.

### Was dem Buch fehlte

Drei Dinge, und nur eins davon war das Buch selbst:

1. **Die Lese-Oberfläche.** Das Buch ist im Original keine Lektüre, sondern eine Werkbank:
   Rechtsklick öffnet vier Plätze und ein Ergebnis. Portiert als `BookMenu` / `BookScreen`,
   alle Platzkoordinaten unverändert aus `ContainerBook` und `GUIBook`.
2. **Die Rezepte.** `MagicRecipes` kennt keine Form — es zählt allein, welche Gegenstände in
   den belegten Plätzen liegen, in Leserichtung. Das ist kein Werkbankrezept und passt in kein
   Rezeptdatenblatt, deshalb steht die Liste wie im Original fest im Quelltext.
3. **Ein Weg zum Buch.** Das Original hat dafür drei: ein verstecktes Bobmazon-Angebot, zwei
   Beutetöpfe und ein Werkbankrezept (Balefire-Scherben in den Ecken, Gold an den Kanten, ein
   Buch in der Mitte). Bobmazon und die Beutetöpfe sind nicht portiert, **das Rezept schon** —
   es reicht.

Die Oberfläche hängt an keinem Block. Sie ist damit das erste Menü im Port, das ein Gegenstand
öffnet; `stillValid` prüft deshalb, ob der Spieler das Buch noch bei sich hat, und beim
Schließen fällt heraus, was auf den vier Plätzen liegt — wie im Original.

### Der Diamanthammer brauchte eine ganze Kette

Sein Rezept im Buch lautet: drei Diamantkies und **ein Bleihammer**. Den gab es im Port nicht,
und der Bleihammer wiederum braucht einen Holzhammer und Schrotkugeln. Also drei neue
Gegenstände:

| Gegenstand | Herkunft | Wirkung |
| --- | --- | --- |
| Holzhammer | Werkbank (Holzstufe, Stamm, zwei Stöcke) | "Thunk!" — nur Krach |
| Bleihammer | Werkbank (vier Schrotkugeln, vier Bleibarren, Holzhammer) | 15 s Bleivergiftung, Stufe 5 |
| Schrotkugeln | Montagefabrik (sechs Bleinuggets) | — |

Die drei Hämmer sind wie im Original in keinem Kreativreiter; `tools/tab-check.sh` führt sie
als begründete Ausnahmen. Der Hinweis dort, sie seien im Überleben unerreichbar, ist mit dieser
Runde hinfällig und wurde ersetzt.

### Drei der sieben Buchrezepte fehlen noch

Portiert sind Balefire-Zünder, Elektronium und der Diamanthammer. Die übrigen vier hängen an
Gegenständen, die es im Port noch nicht gibt: `rod_of_discord`, `mysteryshovel` und der
Mese-Hammer samt `shimmer_handle`. Das vierte ist ein Sonderfall — das Original verlangt
dreimal `ingot_u238m2` mit **verschiedenen Metadaten**; Metadaten gibt es in 1.21 nicht mehr,
und ohne die Vorlage, wie die drei Zustände im Port heißen sollen, wird hier nichts geraten.

Für die Übersicht kam die JEI-Kategorie **Black Book** dazu, die das Original als NEI-Handler
führt. Als Hintergrund dient wie dort die Oberfläche des Buchs; der Ausschnitt beginnt bei
(5|11), und genau darauf beziehen sich die Platzkoordinaten des Originals — die vier Eingänge
bei (25|6) im Abstand 36, das Ergebnis bei (119|24). Nachgerechnet gegen die Oberfläche:
30−5 = 25, 17−11 = 6, 124−5 = 119, 35−11 = 24.

### Nachtrag: `BookItem` war mehrdeutig

Der erste Anlauf ist in der CI am Übersetzer gescheitert, mit genau einem Fehler:

```
NtmItems.java:1112: error: reference to BookItem is ambiguous
  both class net.minecraft.world.item.BookItem in net.minecraft.world.item
  and class com.hbm.items.special.BookItem in com.hbm.items.special match
```

`NtmItems` importiert beide Pakete mit Stern — `com.hbm.items.special.*` und
`net.minecraft.world.item.*` —, und Vanilla hat selbst eine Klasse `BookItem`. Die Klasse
heißt jetzt `BlackBookItem`, nach der Kategorie, unter der das Original sie in NEI führt.

**Ein Tor dafür gibt es nicht, und zwar begründet.** Die Prüfung bräuchte die Liste der
Vanilla-Klassennamen; offline steht kein Minecraft-Classpath zur Verfügung (`maven.neoforged.net`
ist gesperrt). Die naheliegende Ersatzquelle — die Vanilla-Klassen, die der Port selbst
irgendwo explizit importiert — hätte hier nichts gefunden: `net.minecraft.world.item.BookItem`
wird im ganzen Port kein einziges Mal importiert. Eine Regel, die den eigenen Anlassfall nicht
findet, wird nicht ausgeliefert. Es bleibt bei der Merkregel: **klingt ein Klassenname nach
Vanilla, bekommt er ein Präfix** — und bei der CI als Fangnetz.

Die übrigen fünf neuen Klassen dieser Runde sind unkritisch: keine von ihnen importiert
irgendetwas mit Stern.

## Stufe 5, Schritt 2: die ersten Bauteile der Bauwerke

Nach Runde 170 ist die Weltgenerierung der letzte offene Punkt. `tools/structure-gap.py` misst
die Lücke: **79 Bauwerke, 185 darin benutzte Blocknamen, davon 56 im Port gar nicht vorhanden.**
Diese 56 zerfallen in Gruppen — Beutekisten, Bauteile, Beleuchtung, Deko und Elektronik,
Fässer, Weltgen-Werkzeug. Abgearbeitet wird gruppenweise, und zwar die vollständig portierbaren
zuerst.

Den Anfang machen fünf Blöcke ohne jede Abhängigkeit:

| Block | Form |
| --- | --- |
| `steel_grate_wide` | dieselbe Platte wie `steel_grate`, nur mit dem groberen Bild |
| `wood_barrier` | zwei Pixel dicke Bohle an einer der vier Seitenflächen |
| `wood_structure_roof` | Brett am Boden, drei Pixel hoch |
| `wood_structure_scaffold` | Pfosten über die ganze Höhe, einen Pixel von jeder Seite |
| `wood_structure_ceiling` | Platte oben, zwei Pixel dick |

Das breite Gitter brauchte keine neue Klasse: `registerSteelGrate` im Blockstate-Geber nimmt
jetzt Block und Oberseitentextur als Parameter und bedient beide Gitter. `wood_structure` hält
das Original als drei Metadaten eines Blocks; im Port sind es drei Blöcke, wie bei allen
Metadatenfamilien — im Messskript steht der Name deshalb jetzt unter `FAMILIEN`.

Die Ausrichtung der Bohle ist übernommen, nicht neu erfunden: `FACING` benennt die Seite, **an
der** sie klebt. Klickt man eine Seitenfläche an, sitzt sie an dieser Fläche
(`getClickedFace().getOpposite()`); von oben oder unten gesetzt, richtet sie sich nach der
Blickrichtung. Das entspricht Zeile für Zeile dem `onBlockPlaced`/`onBlockPlacedBy` des
Originals, dessen Metadaten 2–5 dieselbe Zuordnung tragen.

**Beinahe derselbe Fehler wie in Runde 170:** die Klasse hieß zuerst `BarrierBlock` — und
`NtmBlocks` importiert `com.hbm.blocks.generic.*` **und** `net.minecraft.world.level.block.*`
mit Stern, wo Vanilla ein `BarrierBlock` hat. Diesmal vor dem Übersetzen bemerkt; sie heißt
`WoodBarrierBlock`. Zwei Beinahe-Fehler derselben Art sind genug: die Vanilla-Wildcards in
`NtmBlocks` und `NtmItems` werden in einer eigenen Runde durch explizite Importe ersetzt. Dann
ist die Fehlerklasse strukturell ausgeschlossen, und daraus wird ein Tor, das sich messen lässt
— anders als die Namensprüfung, für die offline die Vanilla-Klassenliste fehlt.

Die Lücke steht damit bei **53**.

### Wand und Außenecke aus Stahl

Zwei weitere Bauteile derselben Gruppe. Beide Geometrien sind belegt, nicht geschätzt:
`DecoBlock.setBlockBoundsBasedOnState` liefert die Kollisionsform, `RenderSteelWall` und
`RenderSteelCorner` die sichtbare. Die Ecke besteht aus drei Quadern — für Norden

```
(4|0|14)-(16|16|16)   die lange Wand
(0|0|12)-(4|16|16)    das dickere Eckstück
(0|0|0)-(2|16|12)     der kurze Schenkel
```

Ich habe nachgerechnet, ob die drei anderen Richtungen im Original dieselbe Form gedreht sind:
Norden um 180° gedreht ergibt Zeile für Zeile Süden, um 90° gegen den Uhrzeigersinn Westen,
im Uhrzeigersinn Osten. Deckungsgleich — deshalb steht im Port **ein** Modell mit vier
Drehungen statt vier Modellen. Der Versatz um 180° in der Drehung kommt daher, dass
`Direction.toYRot()` bei Süden null zählt, das Modell aber für Norden gebaut ist.

**Die Ausrichtung folgt hier einer anderen Regel als bei der Holzbohle**, und das ist kein
Versehen: `BlockBarrier` setzt für Blick nach Süden Metadatum 2 (Norden) und zeichnet die Bohle
an der Südkante — die Form liegt also auf der zugewandten Seite. `DecoBlock` setzt für
denselben Blick Metadatum 3 (Süden) und zeichnet die Wand an der Nordkante, also abgewandt.
Beide Zuordnungen sind unverändert übernommen.

Der Schraubendreher dreht Wand und Ecke weiter; das Original geht dabei 3 → 4 → 2 → 5, was
genau `getClockWise()` entspricht, mit Schleichtaste andersherum.

**Eine Textur bleibt beim Original:** CE hat `steel_wall.png` ersetzt — statt des Rippenblechs
eine glatte Platte mit Rand. Das ist keine Überarbeitung derselben Vorlage, sondern ein anderes
Bild; ohne Beleg, dass es denselben Block meint, bleibt die Fassung des Originals stehen.

Die Lücke steht bei **51**.

### Sternmetallblock, Elektroschrott und die Erde der Bauwerke

Drei weitere Blöcke, alle ohne Sondermodell:

- **`block_starmetal`** ist im Original ein `BlockBeaconable` — er trägt ein Leuchtfeuer. In
  1.21 gibt es diese Klasse nicht mehr; das macht der Tag `minecraft:beacon_base_blocks`. Dazu
  die beiden Rezepte des Originals (neun Barren zum Block und zurück).
- **`block_electrical_scrap`** ist ein fallender Block; `SimpleFallingBlock` stand schon.
- **`ntm_dirt`** ist ein Kuriosum: Erde, die aussieht wie gewöhnliche Erde, so heißt und auch
  als solche abfällt. Das Original legt sie an, damit die Bauwerke eine eigene Erde setzen
  können, ohne dass der Spieler etwas davon merkt. Sie steht wie dort in keinem Kreativreiter
  und ist im Tor mit Begründung eingetragen.

Die Lücke steht bei **48**.

### Zwei Scheinlücken in der Messung

Beim Durchgehen der restlichen Namen waren zwei gar keine Arbeit, sondern Messfehler:

- **`machine_weapon_table`** heißt im Port `weapon_table`, ohne das `machine_` davor — der
  Block ist seit Runde 74 da. Er steht jetzt bei den Namen, die der Port anders schreibt.
- **`machine_electric_furnace_off`** ist im Original ein eigener Block für den ausgeschalteten
  Ofen. Im Port ist das `machine_electric_furnace` mit `lit=false`, also ein Familienfall.

Damit steht die Lücke bei **46** — ohne dass eine Zeile Code dazugekommen wäre. Solche Einträge
gehören aufgelöst, sobald sie auffallen: eine Messung, die Erledigtes als fehlend führt, macht
die Zahl wertlos.

### Ein Fund nebenbei: `steel_beam` stimmt nicht

Beim Durchsehen der Bauteile ist aufgefallen, dass die Roadmap-Zeile aus Runde 9 —
„`steel_beam` und `stone_gneiss` nachgereicht, beide sind im Original schlichte Blöcke" — für
`steel_beam` falsch ist. Im Original ist er ein `DecoBlock` mit eigenem Darsteller:
`RenderSteelBeam` zeichnet `ResourceManager.beam`, ein OBJ-Modell, und die Kollisionsform ist
(7\|0\|7)-(9\|16\|9), eine dünne Säule. Im Port ist er ein Vollwürfel.

Die Zeile ist berichtigt, der Block noch nicht: **Kollision und Aussehen müssen zusammen
geändert werden.** Nur die Kollision zu verschmälern wäre schlimmer als der jetzige Zustand —
man sähe einen Würfel und liefe hindurch.

Dafür fehlt der Weg, OBJ-Modelle als Blockmodelle zu laden (`neoforge:obj`), den der Port
bisher nirgends benutzt. Er wird in einer eigenen Runde eingeführt und bringt dann drei Dinge
auf einmal: die Berichtigung von `steel_beam`, dazu `steel_poles` und `steel_roof`. Für
`steel_poles` ist das der einzige Weg — sein Modell hat 84 Dreiecke, davon 40 schräge; als
Quader im JSON-Format ist es nicht nachbaubar, das wäre Raten.

## Das 25. Tor: keine gemischten Stern-Importe

Zweimal in Folge hat derselbe Fehler zugeschlagen: `NtmItems` importiert
`com.hbm.items.special.*` **und** `net.minecraft.world.item.*`, und Vanilla hat eine Klasse
`BookItem` — der Übersetzer bricht mit `reference to BookItem is ambiguous` ab. Das hat Runde
170 einen CI-Lauf gekostet. Zwei Stunden später hätte `BarrierBlock` in `NtmBlocks` dasselbe
getan, diesmal vorher bemerkt.

Eine Prüfung auf die Namen selbst wäre das Naheliegende, ist aber **nicht messbar**: dafür
bräuchte es die Liste aller Vanilla-Klassennamen, und offline gibt es keinen
Minecraft-Classpath. Die Ersatzquelle — Vanilla-Klassen, die der Port selbst irgendwo explizit
importiert — hätte ausgerechnet den Anlassfall verfehlt, denn
`net.minecraft.world.item.BookItem` wird nirgends importiert.

Also geht das Tor an die Ursache statt an das Symptom: **keine Datei darf gleichzeitig ein
Projekt-Paket und ein `net.minecraft`-Paket mit Stern importieren.** Damit kann der Konflikt
gar nicht erst entstehen.

Beide Sammeldateien sind entsprechend aufgelöst. Die Vanilla-Sternimporte standen für
erstaunlich wenig: `NtmBlocks` braucht aus `net.minecraft.world.level.block` genau **vierzehn**
Klassen, `NtmItems` aus `net.minecraft.world.item` **dreizehn**. Ermittelt wurden sie nicht von
Hand, sondern durch Abgleich aller benutzten Typnamen gegen die Dateinamen des Projekts —
was nicht im Projekt liegt, kam aus dem Stern.

**Gemessen:** 1929 Dateien durchgesehen, null Funde. Setzt man den Stern in `NtmBlocks` wieder
ein, meldet das Tor genau diese eine Datei und nennt beide Seiten.

### Das 25. Tor hat sich beim Einbau selbst ein 26. eingebrockt

Der erste Anlauf zu Tor 25 ist in der CI durchgefallen — mit vier Mal demselben Fehler:

```
NtmBlocks.java:989: error: cannot find symbol
  symbol:   class LiquidBlock
```

Beim Auflösen der Sternimporte habe ich die Liste der gebrauchten Vanilla-Klassen
automatisch ermittelt. Die Suche verlangte hinter dem Namen einen Punkt, eine Klammer oder
ein Leerzeichen — `LiquidBlock` kommt aber ausschließlich in `DeferredBlock<LiquidBlock>` vor,
also mit einem `>` dahinter. Vierzehn Klassen gefunden, die fünfzehnte übersehen.

Das ist genau die Art Fehler, die ein Tor abfangen soll, und er wäre lokal auffindbar gewesen.
Also gibt es jetzt **Tor 26: jeder benutzte Block- oder Gegenstandstyp muss gedeckt sein** —
durch einen expliziten Import, eine Klasse des Projekts oder eine Deklaration in derselben
Datei.

**Warum nur `-Block` und `-Item`?** Weil ein Tor über alle Typnamen nicht zu gebrauchen ist:
der erste Entwurf meldete hunderte Fehlalarme — innere Klassen (`Tuple.Pair`), Paketgenossen,
jeden `java.lang`-Typ, den die Liste nicht kennt. Ein Tor, das man wegsehen muss, ist keines.
Blöcke und Gegenstände sind aber genau die Stelle, an der die Sammeldateien hängen, und dort
misst es sauber.

**Gemessen:** 1893 Dateien, null Funde. Nimmt man den `LiquidBlock`-Import wieder heraus,
meldet das Tor genau diese eine Stelle.

## Stufe 5: die Giftbrühe und die beiden Strahlenfässer

Drei Blöcke, die zusammenhängen: das gelbe Fass hinterlässt beim Bersten mit einem Drittel
Wahrscheinlichkeit `toxic_block` — ohne die Giftbrühe wäre das Fass unvollständig, und ohne das
Fass hätte die Brühe keine Quelle.

### Die Giftbrühe

Ein neues Fluid, das erste seit dem Rotschlamm. Die Werte stehen im Original (`ToxicFluid`):
Dichte 2500, Zähigkeit 2000, Leuchtkraft 15, Temperatur 2773. Der Block hält fest wie ein
Spinnennetz, verstrahlt wer darin steht, und erstarrt zu gelöschtem Sellafit, sobald er eine
andere Flüssigkeit berührt.

Beide Texturen sind aus dem Original übernommen (in CE unverändert) und haben dasselbe Maß wie
die des Rotschlamms — 16×320 für die ruhende, 32×512 für die fließende, also 20 und 16 Bilder.
Die Animationsdaten sind entsprechend angelegt.

**Abweichung wie beim Rotschlamm:** das Original ist ein `BlockFluidClassic` mit vier Stufen
und eigener Verdrängungslogik; auf 1.21 übernimmt das Fluidsystem das Fließen, die vier Stufen
entsprechen dem Stufenabfall von zwei je Block.

### Die beiden Fässer

Im Original sind beide dieselbe Klasse `YellowBarrel`, die sich an drei Stellen selbst
abfragt. Im Port ist daraus **ein** Block mit zwei Parametern geworden:

| | gelbes Fass | verglastes Fass |
| --- | --- | --- |
| Strahlung je Takt | 5 | 0,5 |
| zündet mit, wenn nebenan etwas hochgeht | ja | **nein** |

Das dritte Verhalten teilen sie: beim Bersten wird zu einem Drittel Giftbrühe gesetzt, sonst
eine Explosion der Stärke 12 ausgelöst; dazu Fallout im Radius 35 und Radongas im Umkreis von
fünf Blöcken, jeder Platz mit einer Chance von 1 zu 5.

Dass das verglaste Fass nicht mitzündet, steht im Original als
`if(this != ModBlocks.yellow_barrel) return;` in `onBlockDestroyedByExplosion`. In 1.21 heißt
die Stelle `wasExploded`; das verglaste Fass kehrt dort einfach zurück und wird von der
Explosion nur zerstört.

**Beinahe falsch abgeschrieben:** Härte und Widerstand hatte ich vom roten Fass übernommen
(0,1 / 2,5). Das Original setzt für beide Strahlenfässer 0,5 / 2,5 — berichtigt, bevor es in
die CI ging.

Die Lücke steht bei **43**.

### Nachtrag: der Giftblock hatte kein Item — und das Tor sah es nur in der CI

Der Lauf ist an `tools/model-resolve-check.sh` gescheitert:

```
OHNE MODELL: block toxic_block -- models/item/toxic_block.json fehlt
```

Das war kein Fehler im Block, sondern ein fehlender Eintrag: Flüssigkeiten werden mit blankem
`BLOCKS.register` angelegt und bekommen bewusst **keinen** Gegenstand. Neun solche Blöcke standen
in der Ausnahmeliste `OHNE_GEGENSTAND`, der zehnte fehlte.

Ärgerlich war nicht der Eintrag, sondern dass er mir lokal nicht auffallen konnte: **dieses Tor
ist auf dem Entwicklungsrechner blind.** Es prüft die *erzeugten* Modelle, und die entstehen
erst durch `runData` in der CI — lokal findet es 13 Modelle statt tausender und meldet
zufrieden „OK".

Deshalb hat das Tor jetzt einen zweiten Teil, der **ohne** erzeugte Modelle auskommt: Er liest
aus dem Quelltext, welche Blöcke mit blankem `BLOCKS.register` angelegt sind, und hält das gegen
die Liste — in beide Richtungen. Fehlt einer, sagt er das; steht einer zu viel darin, auch.

**Gemessen** (Exit-Code direkt, nicht durch eine Pipe): 10 Blöcke im Quelltext, alle in der
Liste, `rc=0`. Nimmt man `toxic_block` heraus, meldet er genau diesen einen und gibt `rc=1`.
