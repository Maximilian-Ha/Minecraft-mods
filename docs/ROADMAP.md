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
      Hier bekommt sie eines, weil der Port erst eines der 79 Bauwerke setzt (Runde 251).

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

> **Überholt seit Runde 251.** Das Meteoritenverlies steht; der Umsetzer für das
> Dateiformat ist gebaut und gilt für alle 79. Siehe *Runde 250/251* am Ende.

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

> **Nachgereicht in Runde 206.** Beide Kopfmodelle sind portiert, die drei Masken mit Modell
> (`gas_mask`, `gas_mask_m65`, `gas_mask_mono`) sind am Körper sichtbar. Siehe dort.

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

## Stufe 5: der Stahlträger stimmt wieder, die Masten kommen dazu

Die Berichtigung aus dem Fund weiter oben ist eingelöst — und sie war leichter als gedacht,
weil der Port das Werkzeug längst hat: `NtmGeometryLoader` samt `SimpleWavefrontBakedModel`
backt OBJ-Dateien als ganz gewöhnliche Blockmodelle. Genutzt hatten das bisher Stacheldraht,
Fass, Kabel, Rohr und Amboss; jetzt auch Träger und Masten. Ein `neoforge:obj` braucht es
dafür nicht.

**`steel_beam`** war seit Runde 9 ein Vollwürfel. Im Original ist er eine dünne Säule:
Kollisionsform (7\|0\|7)-(9\|16\|9) aus `DecoBlock`, Aussehen aus `beam.obj`, das
`RenderSteelBeam` zeichnet. Nachgesehen, was in dieser Datei steht: ein Quader von
−0,0625 bis 0,0625 in x und z, 0 bis 1 in y — also genau dieselben zwei mal sechzehn mal zwei
Pixel. Form und Modell stimmen jetzt beide und, was wichtiger ist, sie stimmen **miteinander**.

**`steel_poles`** ist neu: zwei Masten mit Querstrebe, gedreht nach der Blickrichtung. Sein
Modell hat 84 Dreiecke, davon 40 schräge — als JSON-Quader wäre das nicht nachzubauen gewesen,
über den OBJ-Lader ist es eine Zeile. Das Original gibt ihm keine eigene Kollisionsform; er
behält deshalb volle Würfelmaße, obwohl das Modell schlank ist.

Damit ist von der Gruppe nur noch `steel_roof` offen, und der geht einen anderen Weg: sein
Aussehen steckt nicht in einer OBJ-Datei, sondern in einer Java-Modellklasse, die das Original
über eine Blockentität zeichnet.

**Was die CI hier nicht prüfen kann:** dass das Modell im Spiel richtig aussteht. Sie prüft,
dass die Datengenerierung das Modell schreibt, dass es auflöst und dass der Server lädt — das
Bild selbst sieht nur, wer es sich ansieht.

Die Lücke steht bei **42**.

### Totes Gewächs

Fünf Formen — Gewächs, Gras, Blume, große Blume, Farn. Im Original ein Block mit fünf
Metadaten, im Port fünf Blöcke, wie bei allen Metadatenfamilien; im Messskript steht der Name
entsprechend unter `FAMILIEN`.

Wo sie stehen dürfen, steht in `canPlaceBlockOn` des Originals und ist unverändert übernommen:
Gras, Erde, Ödland, öliger und toter Boden. Dazu kam `ntm_dirt`, weil die Bauwerke genau diese
Erde setzen — ohne sie stünde das Gewächs in den eigenen Ruinen auf unzulässigem Grund.

**Das Modell-Tor hat hier gearbeitet**, und zwar lokal statt in der CI: `basicItem` erwartet
eine Item-Textur `item/plant_dead_*.png`, die es nicht gibt — im Original trägt der Gegenstand
die Blocktextur. Das Tor nennt beim Fund gleich den Grund („runData bricht dafür ab") und die
zwei Auswege. Item-Modell auf `block/plant_dead_*` gezeigt, Sache erledigt, kein Lauf verbrannt.

Die Lücke steht bei **41**.

Der Pilz (`mush`) bleibt vorerst liegen: Er wächst im Original zu einem Riesenpilz, und der
Generator dafür gehört zur Weltgenerierung, die noch nicht portiert ist. Ihn ohne sein Wachstum
hinzustellen wäre eine halbe Sache — er kommt, wenn die Generatoren dran sind.

## Stufe 5: die Scheinwerfer und ihr Lichtkegel

Drei Scheinwerfer — Glühbirne, Leuchtstoffröhre, Halogenstrahler — und der Lichtkegel, den sie
werfen. Reichweiten wie im Original: zwei, acht und zweiunddreißig Blöcke.

### Zwei Eigenheiten, die man kennen muss

**Das Rotsteinsignal schaltet sie AUS.** Das ist kein Versehen, sondern ausdrücklich so gebaut
— der Kommentar im Original sagt, die Lampen sollten ohne Verkabelung nützlich sein und sich
wie Redstone-Fackeln verhalten. Dafür hat das Original je zwei Blöcke (`spotlight_x` und
`spotlight_x_off`); im Port ist es die Eigenschaft `LIT`, wie beim elektrischen Ofen, und
`spotlight_incandescent_off` steht im Messskript als Familienfall.

**Eine zerschossene Lampe** bleibt dunkel, bis jemand sie mit einem Rechtsklick ersetzt — und
das Original repariert dabei gleich alle anliegenden Lampen mit, rekursiv über alle sechs
Seiten. Auch das ist übernommen.

### Der Lichtkegel merkt sich, woher er beleuchtet wird

Ein Platz kann von mehreren Scheinwerfern zugleich getroffen werden. Das Original hält die
Herkunftsrichtungen als **sechs Bits in einer Blockentität**; im Port sind es sechs Ja-Nein-
Eigenschaften im Blockzustand — dieselbe Auskunft, ohne dass für jeden Lichtblock eine
Blockentität nötig wäre. Erst wenn die letzte Richtung wegfällt, verschwindet der Strahl.

Dazu gehören drei Wege, alle aus dem Original:

| | |
| --- | --- |
| `propagateBeam` | setzt den Kegel, bis die Reichweite aufgebraucht ist oder etwas im Weg steht |
| `unpropagateBeam` | nimmt ihn zurück, lässt aber stehen, was von woanders noch leuchtet |
| `backPropagate` | sucht vom Strahl aus rückwärts die Lampe, wenn ein Hindernis wegfällt |

### Zwei Tore haben lokal zugeschlagen

`import-check` fand, dass `SpotlightBlock` im Blockstate-Geber benutzt, aber nicht importiert
war. `lang-check` fand, dass der Lichtkegel keine Namenszeile hatte — unsichtbar hin oder her,
ohne sie stünde im Spiel der rohe Schlüssel. Beides vor dem Push behoben, kein Lauf verbrannt.

Die Lücke steht bei **36**.

### Der Mastaufsatz

`pole_top` folgt genau dem Muster von Träger und Masten: ein OBJ-Modell (`antenna_top.obj`)
über den Lader, der im Port längst steht. Das Original dreht ihn nicht — er steht rund.

Die beiden übrigen Stücke der Beleuchtungsgruppe brauchen mehr: `floodlight` und
`pole_satellite_receiver` sind im Original Blockentitäten mit einstellbarem Winkel — beim
Flutlicht stellt man ihn mit dem Werkzeug, und beides wird über eine eigene Darstellerklasse
gezeichnet, nicht über ein festes Modell. Das ist eine eigene Runde.

Die Lücke steht bei **35**.

## Stufe 5: Bildschirm, Toaster, Tonbandgerät

Acht Blöcke, eine Klasse. Der Röhrenbildschirm steht im Original in vier Zuständen — sauber,
zerbrochen, blinkend und mit Absturzbild —, der Toaster in drei Werkstoffen, dazu das
Tonbandgerät. Alle sind Metadaten eines Blocks; im Port ist jede Ausführung ein eigener Block,
und im Messskript stehen `deco_crt` und `deco_toaster` als Familien.

Eine Klasse reicht für alle acht: Sie tun nichts, stehen nur herum und richten sich beim Setzen
nach der Blickrichtung. Was sie unterscheidet, steckt im Bild, nicht im Verhalten — genau wie
im Original, wo die Metadaten nur die Textur austauschen. Entsprechend teilen sich die vier
Bildschirme ein OBJ-Modell und die drei Toaster ebenfalls.

**Drei Tore haben gleichzeitig zugeschlagen**, alle mit demselben Grund: Die acht Blöcke waren
registriert, aber nirgends angemeldet — keine Namenszeile, keine Beutetabelle, kein
Kreativreiter. `lang-check`, `loot-check` und `tab-check` haben das je aus ihrer Richtung
gemeldet, bevor etwas in die CI ging. Der Beutetabellen-Fund hätte `runData` abgebrochen.

Die Lücke steht bei **32**.

### Der Bürorechner

`deco_computer` sieht im Original nach einer Metadatenfamilie aus — `BlockDecoModel` mit einer
Aufzählungsklasse. Nachgesehen, was darin steht: **ein einziger Wert**, `IBM_300PL`. Also ein
Block, keine Familie.

Er ist das erste Stück dieser Gruppe mit eigenen Maßen: (2\|0\|0)-(14\|14\|10), aus
`setBlockBoundsTo` des Originals. `DecoFacingBlock` nimmt sie jetzt als zweiten Parameter und
dreht sie einmal beim Anlegen auf alle vier Richtungen vor, statt bei jedem Kollisionstest zu
rechnen. Die Drehformel — (x\|z) wird zu (1−z\|x) — ist dieselbe, mit der auch die Modelle
gedreht werden; sonst stünde die Form quer zum Bild.

Die Lücke steht bei **31**.

### Der alte Kessel — und zwei weitere Scheinlücken

**`machine_boiler_off`** ist ein Kuriosum: Im Original gibt es ihn **nur** in der
Aus-Fassung. Ich habe nach dem Gegenstück gesucht, `machine_boiler_on` gibt es nicht — die
Klasse `MachineBoiler` nimmt zwar einen Schalter entgegen, aber angelegt wird sie einmal, mit
`false`. Er tut nichts, steht in den Zivilbauten herum und heißt im Original `machine_boiler`
etwas ganz anderes (das ist der Wärmekessel, im Port `heat_boiler`). Deckel und Boden tragen
das Grundbild, die Vorderseite ihr eigenes, die drei übrigen Seiten das Seitenbild — wie in
`getIcon`.

Zwei weitere Einträge waren wieder keine Arbeit, sondern Messfehler:

- **`tnt_ntm`** heißt im Port schlicht `tnt`. Das Original nennt die Variable `tnt` und den
  Registriernamen `tnt_ntm`; der Port hat es andersherum aufgelöst.
- **`anvil_lead`** ist im Port eine Variante des Ambosses (`NTMAnvilBlock.Variant.LEAD`), und
  `addMetaItems` legt alle Varianten in den Kreativreiter — nachgesehen, nicht angenommen.

Die Lücke steht bei **28**.

### Zwei Registrierungen für denselben Kühlturm

Auf dem Weg zum Messrohr bin ich in `NtmBlockEntityTypes` über etwas gestolpert, das mit dem
Messrohr nichts zu tun hatte:

```java
// Zeile 876
MACHINE_TOWER_SMALL = ... BlockEntityType.Builder.of(TowerSmallBlockEntity::new, NtmBlocks.MACHINE_TOWER_SMALL.get())
// Zeile 890
TOWER_SMALL        = ... BlockEntityType.Builder.of(TowerSmallBlockEntity::new, NtmBlocks.FLUID_DUCT_NEO.get())
```

Dieselbe Blockentität, zweimal registriert — und die zweite Registrierung an den **Rohrblock**
gebunden statt an den Kühlturm. Mein erster Verdacht war, dass ich das in Runde 169 selbst
hineingeschrieben habe. `git log -S` sagt etwas anderes: die Zeile stammt aus `aa7cc6cc`
(»Absturz beim Start: camelCase in einer ResourceLocation«, 15. September) — sie ist älter als
alles, was ich hier gemacht habe.

Der Kühlturm funktioniert trotzdem, weil `TowerSmallBlockEntity` und `ClientProxy` beide
`MACHINE_TOWER_SMALL` benutzen; `TOWER_SMALL` ruft niemand ab. Das ist der unangenehme Teil:
toter Code, der sauber übersetzt, sauber startet und sich durch nichts bemerkbar macht — bis
jemand ihn irgendwann versehentlich benutzt und sich wundert, warum seine Blockentität am
Rohr hängt. Also weg damit.

Und danach ein Tor, damit es nicht wiederkommt. **Tor 27: jede in `NtmBlockEntityTypes`
deklarierte Art muss anderswo im Quelltext benutzt werden.** Gemessen in beide Richtungen:
sauber 230 Arten, 0 ohne Verwendung, rc=0; mit der wieder eingesetzten Zeile 231 Arten, genau
ein Fund (`TOWER_SMALL`), rc=1. Exit-Codes direkt abgefragt, nicht durch eine Pipe.

Das Tor hat noch eine zweite Hälfte, die nichts findet, sondern sich selbst prüft: es zählt
alle `BLOCK_ENTITY_TYPES.register(`-Aufrufe (231) und vergleicht sie mit den erkannten
Deklarationen (230) plus dem einen Bus-Anschluss. Passt das nicht auf, bricht es mit »Muster
greift nicht« ab, statt stillschweigend blind zu werden. Ein Torwächter, der die Hälfte der
Tür nicht sieht, ist schlimmer als keiner.

Nebenbei noch eine Scheinlücke weniger: **`machine_fluidtank`** heißt im Port
`machine_fluid_tank`, mit Unterstrich. Die Lücke steht bei **27**.

### Das Messrohr

`fluid_duct_gauge` ist das Gegenstück zum schon portierten Strommesser: ein Rohrstück, das
mitschreibt, wie viel durch das Netz läuft. Die Zählerei selbst ist zwei Zeilen — `FluidNetMK2`
führt bereits einen `fluidTracker`, genau wie `PowerNetMK2` seinen `energyTracker` —, und die
Sekundensumme entsteht wie im Original: jeden Tick aufaddieren, alle zwanzig Ticks umschalten.

Beim Abschreiben ist mir aufgefallen, dass das Original ein Paket zu viel verschickt: die
Blockentität ruft erst `super.updateEntity()` (das am Ende selbst sendet) und danach noch einmal
`networkPackNT(25)` mit den frisch gezählten Werten. Zwei Pakete pro Tick, pro Messrohr. Ich habe
die Reihenfolge umgedreht — erst zählen, dann die Basis senden lassen —, damit die Zähler im
ohnehin abgehenden Paket mitreisen. Das ist kein Abweichen vom Original, das ist dasselbe
Ergebnis für die Hälfte des Netzverkehrs.

Das Aussehen hat mich länger beschäftigt als die Mechanik. Der Block ist im Original kein Rohr,
sondern ein voller Stahlwürfel mit zwei Renderdurchgängen: unten `deco_steel`, darüber auf der
Setzseite die Anzeige und auf den übrigen fünf das Rohr-Overlay. Mein erster Reflex war, das
Overlay wie beim bemalbaren Rohr nach Fluidfarbe einzufärben — der Port hat dafür schon einen
Farbgeber, der auf `PipeBaseBlockEntity` hört, und die Versuchung war groß, den Block einfach in
die Liste zu schreiben.

Nachgesehen, statt angenommen: `FluidDuctGauge` erbt von `FluidDuctBase`, nicht von
`FluidDuctPaintable` — und nur letzterer überschreibt `colorMultiplier`. Der Anzeigewürfel des
Originals ist also **nicht** eingefärbt, er trägt das Overlay in Weiß. Hätte ich dem Reflex
nachgegeben, wäre ein bunter Block herausgekommen, den das Original nie hatte. Das Modell hat
deshalb zwei ungetönte Schichten, genau wie die Sellafield-Erze im Port sie schon benutzen.

Die Fluidsorte steht trotzdem in der Blickleiste, zusammen mit mB/t und mB/s — das steht so auch
im Original.

Damit ist die Lücke bei **26**.

### Das Dachblech — ein Techne-Modell von Hand übersetzt

`steel_roof` war der letzte offene Punkt aus der Bauteil-Gruppe, und der unangenehmste: sein
Aussehen steckt nicht in einer OBJ-Datei, sondern in einer Java-Klasse. `ModelSteelRoof` ist ein
Techne-Export von 2015 — drei `ModelRenderer`-Kästen mit Texturversätzen, gezeichnet von einem
TileEntitySpecialRenderer.

In 1.21 braucht es dafür keine Blockentität. Drei Kästen sind drei Modellelemente; die Arbeit
liegt darin, die Koordinaten richtig zu übersetzen. Zwei Fallen:

**Erstens die Achsen.** Entitätsmodelle zählen Y nach unten, deshalb dreht der Renderer das
Modell um 180° um Z und verschiebt es um (0,5\|1,5\|0,5). Aus einem Modellpunkt (mx\|my\|mz)
wird also (8−mx \| 24−my \| 8+mz) in Pixeln. Das ergibt für die Platte genau
(0\|0\|0)-(16\|1\|16) — und das ist dieselbe Box, die `setBlockBoundsBasedOnState` als
Kollision setzt. Diese Übereinstimmung war die Gegenprobe: hätte ich die Drehung falsch
herum gerechnet, läge die Platte oben und die Kollision unten.

**Zweitens die Textur.** Techne legt die sechs Seiten eines Kastens in einem festen Kreuzmuster
um den Versatz (u\|v). Ich habe das Muster einmal als Kommentar hingeschrieben und in eine
Hilfsmethode gegossen, statt achtzehn Zahlenpaare von Hand einzutippen — die Formel ist die
Begründung, die Zahlen wären nur ihr Ergebnis.

Und die Textur selbst musste wachsen: sie ist 64×32, und Minecraft nimmt in den Blockatlas nur
Bahnen auf, deren Höhe ein Vielfaches der Breite ist — sonst hält es sie für eine Animation und
bricht ab. Also auf 64×64 aufgefüllt, untere Hälfte durchsichtig. Das Auffüllen habe ich mit
einem selbstgeschriebenen PNG-Ent- und -Packer gemacht (weder PIL noch ImageMagick sind hier
installiert) und danach gegengeprüft: obere Hälfte Byte für Byte gleich, untere Hälfte leer.

Nebenbei eine Bestätigung, dass die UV-Rechnung stimmt: im Muster bleibt oben links ein
ungenutztes Feld frei — und genau dort ist die Originaltextur durchsichtig, während die Felder,
die die Formel als Ober- und Unterseite ausweist, undurchsichtiges Blech zeigen.

**Nicht mitportiert:** das Schredderrezept (`steel_roof` → 9× Stahlstaub-Krümel). Der Port hat in
`ShredderRecipes` bisher überhaupt keine Mod-Blöcke stehen, nur Erze und Trümmer; ein einzelner
Eintrag für dieses eine Blech wäre willkürlich. Das ist eine eigene Lücke, keine dieses Blocks.

Die Lücke steht bei **25**.

### Die Beutekisten — und warum nur zwei davon

Acht Kisten fehlen dem Port. Ich habe zwei davon portiert und sechs stehen lassen, und das
ist bewusst so.

Der Grund steht in `BlockCrate` des Originals: fünf gewichtete Beutelisten, gefüllt mit
Gegenständen aus dem ganzen Mod. Ich habe ausgezählt, wie viele davon es im Port überhaupt
schon gibt — nicht geschätzt, sondern jeden Registriernamen einzeln gegen den Quelltext
gehalten:

| Liste | Einträge | im Port vorhanden |
|---|---|---|
| Bleikiste | 23 | 22 |
| Metallkiste | 17 | 15 |
| Waffenkiste | 7 | 4 |
| rote Kiste | 14 | 4 |
| Nachschubkiste | 6 | **0** |

Der Bleikiste fehlt ein einziger Eintrag (`pellet_rtg_weak`), der Metallkiste zwei
(`centrifuge_element`, `piston_selenium`). ~~Die beiden habe ich portiert.~~ **Das stimmte
nicht** — siehe die Berichtigung in Runde 172 weiter unten; nachgereicht sind sie erst dort.
Der Nachschubkiste
fehlt **jeder** Eintrag — sie zieht aus Spritzen und Granaten, und beides gibt es im Port
noch gar nicht. Eine Kiste, die nichts ausspuckt, ist schlimmer als keine Kiste; deshalb
bleiben `crate`, `crate_weapon`, `crate_red`, `crate_can`, `crate_ammo` und `crate_supply`
liegen, bis ihre Inhalte da sind.

Zwei Kleinigkeiten am Rand:

**Die Brechstange** gab es im Port noch nicht — und ohne sie lässt sich keine Kiste öffnen.
Sie ist im Original schlicht ein Stahlschwert mit eigenem Bild, also auch hier: `SwordItem`
auf `NtmTiers.STEEL`, Rezept `"II"/" I"/" I"` aus `ToolRecipes` Zeile 87.

**Das Gewicht** habe ich anders gelöst als das Original. Dort wird jeder Eintrag so oft in
eine `ArrayList` gelegt, wie sein Gewicht sagt — die Bleikiste ist also eine Liste mit 155
Elementen, und sie wird bei **jedem** Öffnen neu aufgebaut. Hier steht das Gewicht als Zahl
daneben und wird beim Ziehen aufsummiert; die Verteilung ist dieselbe, die Liste entsteht
einmal.

Die Lücke steht bei **23**.

### Das Flutlicht

Von den 22 verbliebenen Blöcken war das Flutlicht der erste mit richtiger Mechanik: ein
Stromverbraucher, der fünfzehn Strahlen nach vorn wirft und dort, wo sie auf etwas Festes
treffen, einen Lichtfleck setzt. Fünf Höhenlagen zu je drei Seitenlagen, gestreut um 7,5°
beziehungsweise 15° — die Formel steht unverändert im Port.

Drei Stellen haben Kopfarbeit gekostet:

**Die Metadaten.** Das Original legt die Aufstellseite in die unteren drei Bit und benutzt
zusätzlich die Werte 6 und 7 für einen Sonderfall: hängt das Flutlicht an Decke oder Boden,
gibt es zwei Lagen, je nachdem in welche Himmelsrichtung man beim Setzen schaut. Im Port sind
das eine Richtung und ein Ja/Nein — `FACING` plus `FLIPPED`. Jede der sechs
`meta`-Abfragen im Strahlengang und im Darsteller musste einzeln zurückübersetzt werden; beim
ersten Durchgang hatte ich `UP` unbedingt um 90° gedreht, obwohl das Original nur `meta == 7`
dreht, also `UP` **mit** gesetztem Flip. Beim Gegenlesen gefunden und berichtigt.

**Der Strom kommt von hinten.** Das Original rechnet `getOrientation(meta).getOpposite()` und
hängt sich dort ans Kabel. `FACING` ist die angeklickte Fläche, das Kabel steckt also in der
Gegenrichtung — auch das stand im ersten Entwurf falsch herum.

**Die Lichtundurchlässigkeit.** 1.7.10 zählt sie von 0 bis 255 und lässt alles unter 127
durch; 1.21 zählt von 0 bis 15. Aus `< 127` wird hier `< 15`: alles außer einem voll
deckenden Block. Das steht als Kommentar an der Stelle, damit die Zahl nicht wie ein
Tippfehler aussieht.

Der Lichtfleck braucht — anders als der des Scheinwerfers — eine eigene Blockentität: er merkt
sich nicht bloß Richtungen, sondern **welches** Flutlicht ihn gesetzt hat und der wievielte
Strahl er ist. Nur so kann er sich selbst löschen, wenn das Flutlicht verschwindet, ohne sich
abmelden zu können. Eine Kleinigkeit habe ich dabei gegenüber dem Original geändert: er
wartet, bis der Abschnitt der Quelle geladen ist, bevor er über sein eigenes Fortbestehen
entscheidet — sonst löschen sich Lichtflecke am Rand der Sichtweite selbst.

Das Aussehen kommt aus `floodlight.obj` über einen Blockentitäts-Darsteller, denn die Neigung
ist stufenlos und lässt sich nicht in Blockzustände packen.

Die Lücke steht bei **22**.

### Der Mastaufsatz mit Richtfunkschüssel

`pole_satellite_receiver` steht auf den Antennenmasten der Bunker und tut nichts — er sieht
nur aus. Trotzdem braucht er eine Blockentität, und zwar aus einem einzigen Grund: die
Schüssel steht schräg, −15° nach oben und −25° zur Seite. Ein Blockmodell kann Elemente nur um
±22,5° und ±45° um **eine** Achse drehen; zwei Achsen mit krummen Winkeln gehen nicht.

Das Original löst es mit einem Techne-Modell aus neun Kästen. In 1.21 heißt dasselbe
`LayerDefinition` mit `CubeListBuilder` — die Übersetzung ist fast wörtlich:
`new ModelRenderer(this, u, v)` wird `texOffs(u, v)`, `addBox` bleibt `addBox`,
`setRotationPoint` plus `setRotation` werden `PartPose.offsetAndRotation`. Und die
Reihenfolge der Drehungen ist in beiden Fassungen Z, dann Y, dann X — nachgesehen, nicht
angenommen.

Eine Falle gab es doch: das Original setzt auf jedem Kasten `mirror = true`, aber **nach**
`addBox`. In 1.7.10 liest `addBox` das Feld, die Zeile kommt also zu spät und tut nichts. Ein
mechanisches `.mirror()` hätte das Modell gespiegelt.

Und noch einmal hat ein Tor gegriffen, das ich früher gebaut hatte: `particleOnlyBlock` gibt
dem Blockgegenstand ein `builtin/entity`-Modell, das ohne eigenen Gegenstandsdarsteller
schlicht unsichtbar ist. Das achtzehnte Tor (`bewlr-check`) hat den fehlenden Darsteller gemeldet, bevor die
CI überhaupt lief.

Die Lücke steht bei **21**.

### Die Ladestation

`charger` hängt an der Wand und lädt die Batterien dessen, der davortritt. Mechanisch ist sie
eine Merkwürdigkeit: **sie hat keinen eigenen Speicher**. `getPower()` gibt null zurück,
`setPower()` tut nichts, und `getMaxPower()` meldet genau so viel, wie die Batterien der
davorstehenden Spieler in diesem Tick aufnehmen können. Das Netz sieht also einen Verbraucher,
dessen Fassungsvermögen sich jeden Tick neu ergibt, und `transferPower` reicht den Strom
unmittelbar an die Batterien weiter. Das ist nicht hübsch, aber es ist das Original, und es
funktioniert.

Der Arm braucht zwanzig Ticks: in der ersten Hälfte fährt er heraus, in der zweiten schwenken
die beiden Backen auf. Geladen wird erst, wenn er ganz draußen ist — deshalb die Abfrage
`ausfahrt < DAUER` gleich am Anfang von `transferPower`.

Eine Abweichung, bewusst: das Original ruft `playSoundEffect` in `updateEntity`, und
`updateEntity` läuft auf beiden Seiten. Der Kolbenton kommt dort also doppelt — einmal lokal
vom Client, einmal vom Server verteilt. Hier hängen die Töne an `!isClientSide`, die Bewegung
selbst läuft weiter auf beiden Seiten mit.

**Nicht mitportiert:** die Großfassung des Rezepts (16 Stück aus Glowstone-Block, Stahlblock
und Toroidspule). `coil_copper_torus` gibt es im Port noch nicht.

Die Lücke steht bei **20**.

### Der Pilz — und ein vertauschtes Koordinatenpaar

`mush` ist einer der wenigen lebendigen Blöcke des Mods: er wächst auf verseuchter Erde,
breitet sich langsam aus, verwandelt `waste_earth` mit der Zeit in `waste_mycelium` und lässt
sich mit Knochenmehl zum Riesenpilz treiben. Dafür brauchte es drei neue Blöcke — `mush`,
`mush_block` und `mush_block_stem` — und den Generator `HugeMush`, der im Original sechs
geschachtelte Schleifen ist und hier sechs geschachtelte Schleifen bleibt.

Beim Abschreiben der Ausbreitung bin ich gestolpert. Das Original zählt die Nachbarn so:

```java
for(ix = x - range; ix <= x + range; ++ix)
  for(iy = y - range; iy <= y + range; ++iy)
    for(iz = z - 1; iz <= z + 1; ++iz)
      if(world.getBlock(ix, iz, iy) == this) ...
```

Die Schleifenvariable `iy` läuft über **y**, `iz` über **z** — aber der Aufruf setzt `iz` an
die Y-Stelle und `iy` an die Z-Stelle. Dasselbe beim Setzen: `iy = z + …`, `iz = y + …`, und
dann `setBlock(ix, iy, iz)`. Die Höhe wird also aus einer waagerechten Koordinate gezogen und
umgekehrt.

Das ist keine Absicht, sondern eine Spur: die Routine ist Vanillas `BlockMushroom.updateTick`
mit umbenannten Variablen, und beim Umbenennen ist die Argumentreihenfolge stehen geblieben.
Wörtlich übernommen würde der Port Pilze in willkürlichen Höhen setzen und Nachbarn an
Stellen zählen, an denen keine sein können.

Ich habe die Vertauschung aufgelöst und die Zahlen des Originals behalten — ±2 waagerecht,
±1 senkrecht, höchstens drei Pilze im Umkreis von vier Blöcken, 1:25 pro Zufallstick. Das
steht so auch im Kommentar der Klasse, damit später niemand meint, ich hätte hier etwas
erfunden.

Zwei kleinere Übersetzungen: `EnumPlantType.Cave` beantwortet Vanilla mit „hat der Block oben
eine feste Fläche?" — in 1.21 heißt das `isFaceSturdy`. Und `quantityDropped` mit
`nextInt(10) - 7` (negative Zahlen zählen als nichts) ist dieselbe Verteilung wie eine
Gleichverteilung von −7 bis 2 in der Beutetabelle.

Die Lücke steht bei **19**.

### Wo die restlichen neunzehn stehen

Damit nicht jede Sitzung von vorn anfängt, hier der Stand der Lücke — sortiert danach, was
sie blockiert, nicht danach, wie sie heißen:

**Wartet auf fehlende Gegenstände** (sechs Blöcke): `crate`, `crate_weapon`, `crate_red`,
`crate_can`, `crate_ammo`, `crate_supply`. Die Zählung steht weiter oben; es fehlen die
Spritzen, die Granaten, die Dosen und ein gutes Dutzend Sonderwaffen. `crate_can` braucht
zusätzlich `conservecrate.obj` als eigenen Modelltyp, `crate_supply` eine Blockentität mit
NBT am abgeworfenen Gegenstand.

**Wartet auf das FSB-Rüstungssystem** (zwei Blöcke): `tesla` und `hev_battery`. Beide fragen
Rüstung ab, die es im Port nicht gibt — `ArmorUtil.checkForFaraday` beziehungsweise
`ArmorFSB.hasFSBArmorIgnoreCharge`. Bei der Teslaspule ist das kein Schönheitsfehler: ohne
den Faraday-Schutz würde sie Spieler töten, die im Original geschützt wären. Der Spule fehlen
außerdem `ModDamageSource.electricity` und zwei der drei Krabbenarten.

**Braucht eine eigene Oberfläche** (drei Blöcke): `machine_microwave` (drei Plätze, Tempo\-
regler, Explosion bei Vollgas), `radiorec` und `radio_telex` (Kanaleingabe). Die
Funkgrundlage — `RTTYSystem`, `RTTYChannel`, `NoteBuilder` — steht im Port bereits.

**Gehört zur Weltgenerierung** (sechs Blöcke): `wand_jigsaw`, `wand_logic`, `wand_loot`,
`wand_tandem`, `dungeon_spawner`, `meteor_spawner`. Das sind die Werkzeuge, mit denen das
Original seine Bauwerke zusammensetzt; sie sinnvoll zu portieren heißt, die Bauwerksgenerierung
selbst zu portieren.

**Steht für sich, aber ohne Kreativreiter** (zwei Blöcke): `deco_loot` und `skeleton_holder`.
Beide setzt im Original nur die Weltgenerierung; im Kreativmodus sind sie nicht zu bekommen.

### Die Mikrowelle

`machine_microwave` ist die erste der drei Oberflächen-Maschinen aus der Restliste. Sie nimmt
die ganz normalen Ofenrezepte — aber nur, wenn entweder das Eingelegte oder das Ergebnis
**essbar** ist; in 1.21 ist das die `FOOD`-Komponente statt `instanceof ItemFood`.

Der Regler geht von null bis fünf und bestimmt, wie schnell die Zeit läuft (`time += speed *
2`). Auf Stufe fünf fliegt sie in die Luft — das ist kein Fehler, das steht so im Original,
gleich als erste Abfrage in der Arbeitsschleife.

Zwei Kleinigkeiten beim Übersetzen:

**Die Drehrichtung.** Das Original setzt `rotatable = true` und legt beim Setzen die
Blickrichtung in die Metadaten. Benutzt wird sie nirgends: `BlockMachineBase` überschreibt
keine Bildauswahl, und es gibt nur eine einzige Textur für alle sechs Seiten. Der Block hat
hier deshalb gar keine Richtungseigenschaft — ein Blockzustand, den niemand liest, wäre toter
Zustand, und tote Zustände kosten Blockstate-Einträge.

**Die Explosion.** Das Original hängt ihr zusätzlich einen `PlayerProcessorStandard` an, der
Spielern eigene Schadens- und Rückstoßregeln gibt. Den gibt es im Port nicht; ohne ihn
behandelt die Explosion den Spieler wie jedes andere Wesen. Das ist dieselbe Abweichung, die
der Port an allen anderen `ExplosionVNT`-Stellen schon trägt.

Die beiden Knöpfe der Oberfläche laufen über `CompoundTagControl` und `IControlReceiver` —
die Mechanik, die der Port für alle Knopfpakete benutzt.

Die Lücke steht bei **18**.

### Der Funkempfänger — und ein Helfer, der sich auszahlt

`radiorec` hört auf einem Kanal des Fernschreibfunks mit und setzt das Empfangene in
Notenblocktöne um. Die Grundlagen lagen alle schon da: `RTTYSystem`, `NoteBuilder`,
`CompoundTagControl` für die Knöpfe und `IScreenProvider` für eine Oberfläche ohne Behälter.

Interessant war das Aussehen. Das Original zeichnet ihn mit einem Blockentitäts-Darsteller aus
vier Techne-Kästen — und die sind **alle achsenparallel**, ohne die schrägen Drehungen, die
den Mastaufsatz zur Modellschicht gezwungen haben. Achsenparallele Kästen kann ein Blockmodell,
also braucht dieser Block gar keinen Darsteller.

Dafür ließ sich `techneKasten` wiederverwenden — die Hilfsmethode, die ich fürs Dachblech
geschrieben hatte. Sie musste nur eine Kleinigkeit lernen: die Antenne sitzt auf **halben**
Pixeln (`setRotationPoint(-4.5F, 0F, -0.5F)`), also nehmen x, y und z jetzt Gleitkommawerte.
Das Dachblech ruft sie weiterhin mit ganzen Zahlen auf.

Die Drehung war die einzige Stelle zum Nachdenken. Der Darsteller dreht erst 180° um Z und
dann um Y; in meinem Modell steckt die Z-Drehung schon in den Koordinaten. Es gilt
Rz(180)·Ry(θ) = Ry(−θ)·Rz(180), und Minecraft dreht im Blockzustand im Uhrzeigersinn, wo
OpenGL gegen ihn dreht — beides zusammen hebt sich auf, und `rotationY` ist schlicht θ. Zur
Gegenprobe: das Grundmodell steht damit in der Süd-Lage, und dessen Gehäusekasten
(1\|0\|4)-(15\|10\|12) ist genau der Kollisionskasten, den `setBlockBoundsBasedOnState` für
Metadaten 3 setzt.

Die Lücke steht bei **17**.

### Der Fernschreiber

`radio_telex` ist der größte Einzelblock dieser Runde: fünf Sendezeilen, fünf Empfangszeilen,
zwei Kanäle, Steuerzeichen mit eigenen Knöpfen — und er ist zwei Blöcke breit.

Die Mechanik ließ sich wörtlich übernehmen, weil alle Grundlagen standen. Gesendet wird ein
Zeichen pro Tick über `RTTYSystem`; die Steuerzeichen sind echte ASCII-Steuerzeichen und
stehen jetzt als benannte Konstanten in der Blockentität statt als Zahlen im Code:

| Zeichen | ASCII | Wirkung |
|---|---|---|
| `\u0004` | EOT | Ende der Übertragung |
| `\n` | EOL | Zeilenwechsel |
| `\u0007` | BEL | lässt beim Empfänger eine Glocke schlagen |
| `\u000c` | FF | Empfänger soll nach dem Ende ausdrucken |
| `\u0016` | SYN | eine Sekunde Pause beim Senden |
| `\u007f` | DEL | löscht den Empfangsspeicher |

**Eine Abweichung, die sein musste:** das Original zeichnet den Ausschlag des Senders als
`GL_LINES`-Zug mit dem Tessellator. In 1.21 gibt es im Oberflächenzeichner keine Linien mehr.
Die Kurve entsteht hier aus lauter kleinen Rechtecken, eines je Schritt — dieselbe Kurve,
andere Grundfigur. Der Zufallsstrom hängt weiterhin am gerade gesendeten Zeichen, damit
dieselbe Sendung immer denselben Zug ergibt.

**Und ein Tor, das mich erwischt hat:** `api-check` hat fünf Stellen gemeldet, an denen ich
`.length()` auf einer Variablen namens `text` aufrufe. Das Tor weiß, dass `text` im Port
überall eine `List<Component>` ist, und hielt das für den bekannten Fehlgriff. Hier war es ein
Fehlalarm — meine Variable war ein `String` —, aber die richtige Antwort war trotzdem nicht,
das Tor aufzuweichen, sondern die Variable umzubenennen. Sie heißt jetzt `inhalt` und passt
damit zur Namensgebung des übrigen Ports.

Beim Aufräumen ist noch etwas aufgefallen: die Paragraphenzeichen für die Farbcodes waren als
echte UTF-8-Bytes in die Datei geraten. Der Port schreibt seinen Quelltext in reinem ASCII;
sie stehen jetzt als `§`. Gegengeprüft: keine der elf Dateien dieser Sitzung enthält noch
ein Byte über 0x7F. (Die 34 Stellen in `NtmLanguageProvider` sind englische Anzeigetexte und
standen schon vorher dort.)

Die Lücke steht bei **16**.

### Der Schredder lernt die Bauklötze

Bisher kannte der Schredder Erze, Barren, Kristalle und Platten — also alles, was aus dem
Materialkreislauf kommt. Was er nicht kannte, waren die Blöcke, die man daraus *baut*.
Im Original stehen diese Rezepte verstreut zwischen den anderen; hier sind sie als
geschlossene Gruppe in `registerBuildingRecipes()` zusammengefasst.

53 Rezepte sind dazugekommen:

| Gruppe | Ergebnis |
|---|---|
| Beton, Betonziegel (sechs Fassungen) | Kies |
| Obsidianziegel | Obsidiankies |
| leeres Ölerz, abgeklungenes Sellafield | Kies |
| Sellafield-Diamanterz | Diamantkies ×2 |
| Kalkstein | Kalksteinpulver ×4 |
| Gneis | Lithiumkrümel |
| Schlacke | Zementpulver ×4 |
| vier behauene Meteoritblöcke | Meteoritpulver |
| acht Stahlbauteile | Stahlkrümel, je nach Bauaufwand 1 bis 18 |
| drei Kisten | Eisen-, Stahl-, Wolframpulver |
| 24 Deko-Rohre | Stahlpulver |

Die Mengen stammen unverändert aus dem Original — auch die Auffälligkeiten: die Ecke gibt
mit 18 Krümeln doppelt so viel zurück wie Dach oder Wand, obwohl sie nicht größer ist, und
die Wolframkiste gibt 36 Pulver, also viermal so viel wie die anderen beiden Kisten.

**Was nicht mitkonnte und warum** steht als Kommentar über der Methode, damit es beim
Nachziehen nicht gesucht werden muss. Acht Rezepte scheitern daran, dass der Block im Port
noch fehlt (`ore_nether_fire`, zwei Meteoritziegel, `boxcar`, `ore_tektite_osmiridium`,
`sand_dirty`, `sand_dirty_red`, `stone_porous`); drei daran, dass es das Ergebnis `scrap_oil`
noch nicht gibt; die Wackelkopf-Reihe an `scrap_plastic`. Beim Aluminiumerz gibt das Original
Kryolithbrocken aus — die gibt es im Port nicht, und da das selbsterzeugte Pulverrezept
bereits greift, bleibt es stehen.

Die Lücke bei den Blöcken bleibt bei **16** — diese Runde hat keine Blöcke nachgereicht,
sondern eine Lücke *neben* der Liste geschlossen.

### Die Teslaspule

`tesla` galt bisher als blockiert — angeblich brauchte sie die FSB-Rüstung. Das war nur
zur Hälfte richtig: **`hev_battery`** braucht sie (`ArmorFSB.hasFSBArmorIgnoreCharge` plus
`ArmorFSBPowered` am Helm, beides im Port nicht vorhanden), die Spule dagegen nicht. Sie
braucht nur `ArmorUtil.checkForFaraday`, und das ist keine Rüstungsklasse, sondern eine
Namensprüfung: enthält der Name eines Rüstungsteils eines von 27 Wörtern — `iron`, `steel`,
`rubber`, `hazmat` und so weiter —, leitet oder isoliert es. Erst wenn alle vier Teile
bestehen, steht der faradaysche Käfig.

Beim Nachbauen ist eine Feinheit aufgefallen: das Original prüft den *unlokalisierten*
Namen. Der Kettenhelm heißt dort `item.helmetChain` — und `chain` trifft den Listeneintrag
`chainmail` nicht. Kettenrüstung schützt im Original also nicht, obwohl sie in der Liste
steht. Der Port prüft stattdessen den Pfad im Gegenstandsverzeichnis, wo sie
`chainmail_helmet` heißt und folglich schützt. Das ist die Abweichung, und sie ist
beabsichtigt: die Liste sagt, was gemeint war.

Was sonst dazukam:

- **Eine neue Schadensart** `electricity`. Das Original nimmt
  `setDamageIsAbsolute().setDamageBypassesArmor()`; im Port sind das die drei Tags
  `BYPASSES_ARMOR`, `BYPASSES_EFFECTS` und `BYPASSES_RESISTANCE`. Die vorhandene
  `sednaElectric` passt nicht — die gehört zum Waffensystem und geht durch die Rüstung
  *nicht* hindurch.
- **Der Darsteller** zeichnet das OBJ-Modell und je einen `BeamPronter`-Blitz zu jedem
  Ziel, das der Server gemeldet hat. Die Zahl der Abschnitte wächst mit der Entfernung.
- **Vier Tondateien** im Wechsel, wie im Original.

**Drei Tore haben zugeschlagen**, und alle drei zu Recht:

| Tor | Befund |
|---|---|
| `inventory-check` | Inventarbild mit `scale 1.5` statt `scale 6` — ein Pünktchen statt einer Spule |
| `offscreen-check` | `shouldRenderOffScreen` fehlte: die Blitze wären verschwunden, sobald man von der Spule wegschaut |
| `tab-check` | der Block war in keinem Kreativreiter |

Der Schaden folgt weiter der Rechnung des Originals: die halbe Lebenskraft des Getroffenen,
begrenzt auf drei bis zwanzig, geteilt durch die Zahl **aller** Lebewesen im Suchkasten —
auch derer, die der Blitz gar nicht erreicht, weil eine Wand dazwischensteht. Je voller der
Raum, desto schwächer der einzelne Schlag. Das ist im Original so und bleibt so.

**Nicht übernommen:** die drei Sonderfälle für Krabben. Im Original heilen Taint- und
Teslakrabbe am Blitz, die Cyberkrabbe bleibt unbehelligt; die drei Wesen gibt es im Port
noch nicht. Der Kommentar in der Blockentität sagt, wohin sie gehören, wenn sie kommen.

Die Lücke steht bei **15**.

### Der Sockel, auf dem ein Toter liegt

`skeleton_holder` heißt im Original „Oh, that's a dead guy“ und steht in den Weltbauwerken:
ein kleiner Sockel, der genau einen Gegenstand hält. Rechtsklick mit vollem Beutel legt ab,
Rechtsklick mit leerer Hand nimmt wieder mit, geduckt geschieht nichts. Beim Abbauen fällt
der Gegenstand heraus — er gehört zum Inhalt, nicht zur Beutetabelle.

In 1.21 zerfällt der eine Rechtsklick des Originals in zwei Methoden: `useItemOn` für die
volle Hand, `useWithoutItem` für die leere. Die Bedingungen des Originals verteilen sich
dabei sauber auf beide — mit Gegenstand wird nur ein leerer Sockel belegt, ohne Gegenstand
nur ein voller geleert.

**Die Drehung bleibt schief, und das mit Absicht.** Der Sockel zeigt nicht dorthin, wo man
hinsieht, sondern eine Vierteldrehung gegen den Uhrzeigersinn davon. Im Original steht das
als Tabelle von Blickviertel auf Metadatenwert da, versehen mit dem Kommentar des Urhebers,
dass er das nicht mehr aufräumen werde. Die Tabelle bleibt, weil die 79 Bauwerke des
Originals sich darauf verlassen — geradegerückt stünde in jeder Ruine der Sockel verdreht.

Ein Wert, der nicht abgeschrieben werden konnte: das Inventarbild. Das Original hat für den
Sockel keines, weil er in keinem Kreativreiter steht. Hergeleitet statt geraten:
`ItemRenderBase` verkleinert auf ein Sechzehntel, das Modell ist 1,43 hoch, und mit dem
Faktor acht kommt es auf dieselbe Bildhöhe wie die Teslaspule (1,94 bei sechs).

Er steht wie im Original in keinem Reiter — dafür jetzt mit Begründung in der Ausnahmeliste
von `tab-check`, statt als stiller Sonderfall.

Die Lücke steht bei **14**.

### Drei Hähne für das Rohrnetz

`fluid_valve`, `fluid_switch` und `fluid_counter_valve` schließen eine Lücke, die seit dem
Rohrnetz offenstand: bisher ließ sich eine Leitung nur abreißen, nicht absperren.

Alle drei teilen sich dieselbe Mechanik, und sie ist knapper, als man denkt: ein
geschlossenes Ventil bildet **gar keinen Netzknoten**. Damit ist das Netz an dieser Stelle
wirklich getrennt und nicht bloß gedrosselt — die beiden Hälften sind für das Fluidsystem
zwei verschiedene Netze. Das Vorbild stand schon im Port: der Kabelschalter macht es mit
dem Stromnetz genauso.

| Block | Wie er schaltet |
|---|---|
| `fluid_valve` | von Hand, Rechtsklick |
| `fluid_switch` | vom Redstone; von Hand gar nicht |
| `fluid_counter_valve` | von Hand, und zählt mit, wie viel durchging |

Der Zähler des dritten hat eine Feinheit, die leicht verlorengeht: beim Zudrehen zählt das
Original den angefangenen Tick noch zu Ende, **bevor** es den Knoten abbaut. Ohne das ginge
bei jedem Zudrehen der letzte Tick verloren. Das steht hier genauso.

**Zwei Abweichungen:**

- Der Klang. Das Original nimmt `hbm:block.reactorStart` mit Tonhöhe 1,0 beim Aufdrehen und
  0,85 beim Zudrehen. Den Klang gibt es im Port nicht — wie schon beim Kabelschalter steht
  hier der Hebelklang der Mod, mit denselben beiden Tonhöhen.
- Die OpenComputers-Anbindung des Zählventils (fünf Callbacks) fehlt. Die Mod ist im Port
  nicht angebunden. Dieselben Werte stehen über Redstone-über-Funk bereit, und das ist
  portiert: `value`, `state`, `reset` und `setstate` gibt es alle.

### Ein Tor mehr: widersprüchliche Minecraft-Importe

Die Runde davor ist in der CI durchgefallen, obwohl alle siebenundzwanzig Tore grün gemeldet
hatten. Der Grund: `RenderSkeletonHolder` importierte
`net.minecraft.client.renderer.ItemRenderer`. Die Klasse liegt in 1.21 aber in
`net.minecraft.client.renderer.entity` — und genau so steht sie in vier anderen Darstellern
des Ports.

`import-check` kann das nicht sehen: es beurteilt nur `com.hbm`- und `api.hbm`-Typen, weil
ohne Minecraft-Klassenpfad nicht feststellbar ist, ob ein Paket eine Klasse wirklich enthält.

Aber eine *relative* Aussage ist prüfbar: **steht derselbe einfache Klassenname in zwei
verschiedenen `net.minecraft`-Paketen, ist einer der beiden Importe falsch.** Der Port
benutzt jede Minecraft-Klasse nur in einer Fassung. Das vergleicht den Port mit sich selbst
und braucht keinen Klassenpfad.

Ausgenommen sind verschachtelte Klassen — bei `SynchedEntityData.Builder` ist das letzte
Segment vor dem Namen keine Paketebene, sondern die äußere Klasse, erkennbar am
Großbuchstaben. `Builder` und `Context` kommen im Port heute schon doppelt vor, beide
zu Recht.

Gemessen in beide Richtungen: 411 Klassennamen, 0 Funde; mit dem wiedereingesetzten Fehler
genau ein Fund und Rückgabewert 1. Damit sind es **28 Tore**.

### Der Ziegelofen

`machine_brick_furnace` ist ein gemauerter Ofen, der zwei Dinge anders macht als der
Vanilla-Ofen: er ist **schneller bei dem, wofür er gebaut ist**, und er **hinterlässt Asche**.

| Eingelegt | Tempo |
|---|---|
| Ton, Netherrack | vierfach |
| Bruchstein, Sand, Stammholz | doppelt |
| alles andere | normal |

Die Asche folgt derselben Einteilung wie beim Feuerraum — Holz, Kohle, Sonstiges, je Sorte
ein eigener Zähler. Bei 2000 gesammelten Brennticks einer Sorte springt eine Portion Pulver
ins Aschefach. Die Routine dafür (`getAshFromFuel`) stand schon im Port und wird hier
wiederverwendet, statt sie ein zweites Mal zu schreiben.

Zwei Stellen, an denen 1.7.10 und 1.21 auseinandergehen:

- **Stammholz.** Das Original nennt `log` und `log2`, also die beiden Stammholzblöcke von
  1.7.10. In 1.21 ist daraus eine Familie mit acht Sorten geworden; sie steht jetzt
  vollständig in der Tabelle, sonst wäre Kirsche oder Mangrove ohne Grund langsamer als Eiche.
- **Die Partikel.** Das Original schreibt vier Fälle aus, einen je Metadatenwert. Es ist
  viermal dieselbe Formel mit gedrehten Achsen — mit `FACING` steht sie einmal da.

**Nicht übernommen:** die Schamottekugel `ball_fireclay` in der Tempo-Tabelle (gibt es im
Port nicht), und die Erfahrung am Ausgabeplatz. Das Original benutzt dort `SlotSmelting`,
also den Vanilla-Ofenausgang mit XP. Der Port hat diesen Platztyp nicht; seine Öfen
benutzen durchgehend `SlotTakeOnly`, und dabei bleibt es auch hier — sonst verhielte sich
der Ziegelofen anders als der Eisen- oder Stahlofen daneben.

Ein Tor hat wieder etwas gefunden: `loot-check` meldete, dass der Block
`requiresCorrectToolForDrops()` trägt, aber in keinem `mineable`-Tag steht — er wäre mit
keinem Werkzeug abbaubar gewesen, und seine Beutetabelle wäre tote Ladung. Nachgetragen.

### Eine Kennzahl, die überschätzt hat

Beim Suchen des nächsten Kandidaten ist etwas aufgefallen: `port-gap.py` führte
`TileEntityDecon` als fehlend — dabei steht der Dekontaminator längst im Port, nur unter dem
Namen `DecontaminatorBlockEntity`. Das Werkzeug ordnet über eine Kernregel zu
(`TileEntityFoo` → `FooBlockEntity`, mit und ohne `Machine` davor), und die greift bei einer
echten Umbenennung nicht.

Nachgemessen, wie viele solcher Fälle es gibt: von 133 als fehlend gemeldeten
Blockentitäten haben 19 einen Namensverwandten im Port. Aber nur fünf davon sind wirklich
dieselbe Sache — die übrigen vierzehn sind Zufallstreffer der Suche. `TileEntityCore` ist
der Bombenkern und **nicht** `PileCoreBlockEntity`; `TileEntityCharge` ist die Sprengladung
und **nicht** das Ladegerät.

Die fünf belegten Umbenennungen stehen jetzt als Tabelle im Werkzeug, jede einzeln
nachgesehen:

| Original | Port | Warum |
|---|---|---|
| `TileEntityCableBaseNT` | `CableBaseBlockEntity` | das „NT" fällt im Port weg |
| `TileEntityPipeBaseNT` | `PipeBaseBlockEntity` | dito |
| `TileEntityTurretBaseNT` | `TurretBaseBlockEntity` | dito, im Klassenkommentar belegt |
| `TileEntityDecon` | `DecontaminatorBlockEntity` | ausgeschriebener Name, gleiche Wirkung |
| `TileEntityRBMKControlManual` | `RBMKControlBlockEntity` | der Port fasst Hand- und Normalsteuerung zusammen |

Der Kommentar über der Tabelle sagt ausdrücklich, was **nicht** hineingehört: bloße
Namensähnlichkeit. Sonst schrumpft die Lücke auf dem Papier, während die Arbeit bleibt.

Blockentitäten stehen damit bei **128** statt 133 — und der Ziegelofen ist da schon
abgezogen.

### Der verseuchte Sender

`broadcaster_pc` sendet nichts Gutes: in fünfundzwanzig Blöcken Umkreis wird jedem Lebewesen
übel, in fünfzehn Blöcken tut es zusätzlich weh — und zwar umso mehr, je näher man steht.
Aus voller Entfernung nichts, direkt davor zehn Schaden je Tick, dazwischen linear.

Beim Nachsehen kam eine angenehme Überraschung: **Modell und Umriss sind dieselben wie beim
Funkempfänger.** Das Original benutzt für beide Blöcke dasselbe Techne-Modell
(`ModelBroadcaster`) und wechselt nur die Haut — `RenderDecoBlock` zeichnet `radiorec` und
`broadcaster_pc` mit derselben Instanz. Die vier Kästen waren also schon ausgerechnet; sie
stehen jetzt in einer gemeinsamen Methode, die beide Blöcke aufrufen, statt zweimal
dieselben zwölf Zahlen.

Nachgerechnet, nicht angenommen: alle vier Kästen des Senders ergeben über die
Techne-Umrechnung `(mx|my|mz) → (8-mx-breite | 24-my-höhe | 8+mz)` exakt die Werte, die für
den Empfänger schon im Erzeuger standen.

Neu waren nur drei Dinge: die Schadensart `broadcast` (rüstungsdurchdringend und absolut wie
im Original), die drei Klangschleifen, und die Regel, welche davon ein Sender spielt — sie
hängt am Ort, damit zwei Sender nebeneinander verschieden klingen, derselbe Sender aber
immer gleich.

**Eine Abweichung:** das Original lässt die Schleife über die Lebewesen auf *beiden* Seiten
laufen. Auf dem Client bewirkt weder der Schaden noch der Effekt etwas — beides gehört dem
Server. Hier steht der Teil serverseitig und der Klang clientseitig; dasselbe Ergebnis, nur
ohne die halbe Arbeit doppelt.

### Ein Werkzeug fürs Polstern

Die Haut des Senders ist 64×32 groß, wie die meisten Techne-Häute aus 1.7.10. Der Blockatlas
von 1.21 nimmt nur Texturen an, deren Höhe ein Vielfaches der Breite ist — eine 64×32 wird
abgelehnt, und das Modell bleibt unsichtbar. Beim Funkempfänger hatte ich das von Hand
gelöst; jetzt steht es als `tools/pad-png.py` da und polstert ein PNG durchsichtig nach
unten, ohne Fremdbibliothek und ohne einen einzigen vorhandenen Bildpunkt anzufassen.

Geprüft wurde es an der Haut, die schon im Port liegt: `tools/pad-png.py` auf die
Originaldatei angewandt ergibt Bildpunkt für Bildpunkt dieselbe 64×64 wie
`radiorec.png` — und die oberen 32 Zeilen sind unverändert die der Quelle.

### Die Dämonenkern-Lampe

`lamp_demon` ist die vielleicht boshafteste Deko des Mods: eine Lampe mit einem Dämonenkern
darin, die alles Lebende im Umkreis von fünfundzwanzig Blöcken bestrahlt. Und zwar nicht
gleichmäßig — der Strahl wird von allem gebremst, was auf der Sichtlinie steht, nach der
**Sprengfestigkeit** der Blöcke dazwischen. Hunderttausend Rad durch Luft sind etwas anderes
als hunderttausend Rad durch eine Betonwand. Wer näher als zwei Blöcke steht, verbrennt
zusätzlich.

Der Lichtkegel besteht aus zwei Ringen zu je sechzehn Segmenten, die vom Lampenrand nach
außen laufen und dabei durchsichtig werden — einer nach oben geneigt, einer nach unten.
Additiv gemischt, damit sich überlappende Segmente aufhellen statt einander zu verdecken;
im Port ist das `NtmRenderTypes.GLOW`, der genau die Mischung des Originals nachbildet.

Das Original dreht den Kegel Segment für Segment mit `Vec3.rotateAroundY`. Hier steht
stattdessen der Winkel direkt in Sinus und Kosinus — dasselbe Sechzehneck, nur ohne den
Umweg über einen mitwandernden Vektor.

**Kein Rezept:** das Original baut die Lampe aus `demon_core_closed`, und den Dämonenkern
gibt es im Port noch nicht. Sie steht wie im Original im Kreativreiter und ist damit
erreichbar, aber im Überleben noch nicht herstellbar.

Die Lücke bei den Blockentitäten steht bei **126**.

### Der Dämonenkern

Damit die Lampe der vorigen Runde auch im Überleben gebaut werden kann, fehlte ihr Herzstück:
`demon_core_open` und `demon_core_closed`.

Der Mod baut hier einen echten Unfall nach. 1946 hielt Louis Slotin die beiden
Berylliumhalbschalen um einen Plutoniumkern mit einem **Schraubenzieher** auseinander — der
abrutschte. Genau das passiert hier: am Amboss der dritten Stufe setzt man aus einem
Plutoniumkern, vier Berylliumbarren und einem Schraubenzieher den *offenen* Kern zusammen.
Er strahlt mit 5 Rad. Und sobald er als Gegenstand den Boden berührt, rutscht der
Schraubenzieher heraus: der Kern schließt sich, das Werkzeug liegt daneben.

`onEntityItemUpdate` gibt es in 1.21 als NeoForge-Erweiterung mit derselben Bedeutung; der
Port benutzt sie schon für den nassen Lappen. Die Umsetzung ist daher wörtlich — bis auf
`entityItem.onGround`, das in 1.21 `entity.onGround()` heißt.

Ein Detail, das fast verlorengegangen wäre: die Textur des geschlossenen Kerns ist 16×64 groß
und bringt eine `.mcmeta` mit — sie ist **animiert**, vier Bilder mit einem Tick Standzeit.
Die Datei ist mitgekommen, sonst hätte Minecraft die Textur als vierfach zu hoch abgelehnt.

Damit ist auch das Rezept der Lampe nachgereicht.

### Ein Wegweiser statt einer Zahl

Nach der Dämonenkern-Runde stand die Frage, was als nächstes drankommt — und beim Durchsehen
der kleinen Kandidaten fiel ein Muster auf: `TileEntityLantern` braucht eine Glyphide,
`TileEntityVent` zwei Partikel-Entitäten, `TileEntityRadiobox` die FBI-Mobs. Alles Dinge, die
der Port noch nicht hat.

`port-gap.py` sagt, **wie viele** Blockentitäten fehlen. Es sagt nicht, welche davon man
heute anfangen kann. Das ist der Unterschied zwischen einer Zahl und einem Wegweiser — und
eine halbe Stunde Lesen, die in der Schublade endet, ist genau das, was eine Kennzahl
verhindern sollte.

Also gemessen, statt weiter zu probieren: `tools/be-blocker.py` löst für jede fehlende
Blockentität die `com.hbm`-Importe des Originals auf und hält sie gegen den Port.

Die erste Fassung war unbrauchbar und hat das auch gezeigt: **41×`IGUIProvider`,
25×`ModItems`, 21×`ModBlocks`, 17×`BlockPos`** als häufigste „Blocker". Das sind keine — das
ist die Infrastruktur, die im Port nur anders heißt, und `BlockPos` ist sogar eine
Vanilla-Klasse, die im Original noch selbstgebaut war. Eine Messung, deren Spitzenwerte
allesamt Artefakte sind, misst nichts.

Mit einer Abbildungstabelle für die sechzehn Infrastrukturklassen und einer Typensuche, die
auch verschachtelte Klassen findet (`Mats.MaterialStack` steht in keiner eigenen Datei),
sieht es anders aus:

| | |
|---|---|
| fehlende Blockentitäten | 120 |
| ohne fehlende Vorlage | **44** |
| mit fehlender Vorlage | 76 |

Und die häufigsten echten Blocker sind jetzt Dinge, an denen man wirklich hängenbleibt:
`PathNode` (5×, die Drohnenwegfindung), `ColumnType` (4×, RBMK), `PneumaticNetworkProvider`
und `PneumaticNode` (7× zusammen, die Rohrpost).

Die Tabellen beider Werkzeuge stehen jetzt gemeinsam in `tools/portmap.py`, damit sie nicht
auseinanderlaufen. Der Kommentar darüber sagt, was hineingehört und was nicht — jede Zeile
nachgesehen, Namensähnlichkeit reicht nicht.

### Die erste Runde nach dem Wegweiser: der Zähler

`radio_torch_counter` stand als erster auf der Liste der sofort portierbaren — und die Liste
hat recht behalten: es fehlte kein einziger Baustein.

Die Fackel sieht in das Inventar hinter sich, zählt darin alles, was auf eines ihrer drei
Muster passt, und funkt jede der drei Zahlen auf einen eigenen Kanal. Zwei Betriebsarten:
standardmäßig sendet sie nur bei einer Änderung, auf Knopfdruck jeden Tick.

Bemerkenswert ist, wie wenig zu schreiben war. Der Port hat für Musterfilter längst einen
eigenen Unterbau — `FilterMenuBase` fängt den Klick auf ein Musterfach ab, `FilterScreen`
zeichnet den Hinweis, welche Vergleichsart eingestellt ist, `ModulePatternMatcher` macht den
Vergleich, `SlotPattern` das Fach. Vom Container und der Oberfläche des Originals blieb
danach fast nichts übrig, das hier noch einmal hätte stehen müssen: das Menü sind
**vier Zeilen**, und die kleine Eigenheit des Zählers (er schaltet `forceUpdate`, wenn man
ein Muster oder einen Kanal ändert) sitzt in der Blockentität, wo sie hingehört.

Die zwei verbleibenden Funkfackeln des Originals — `radio_torch_logic` und
`radio_torch_reader` — stehen weiter offen; sie bringen je eigene Oberflächen mit.

### Der Werteleser — und vier Rezepte, die nie geschrieben wurden

`radio_torch_reader` ist die Gegenrichtung des Zählers. Statt Gegenstände zu zählen, fragt
diese Fackel die Maschine hinter sich nach **benannten Werten** und funkt jeden auf einen
eigenen Kanal — acht Zeilen aus Kanal und Wertname. Das Gegenstück dazu steht seit Runde 112
im Port: `IRORValueProvider` mit `provideRORValue(String)`. Acht Blockentitäten geben heute
Werte her, darunter zwei aus den letzten Runden — das Messrohr (`deltatick`, `deltasecond`)
und das Zählventil (`value`, `state`). Bis jetzt gab es niemanden, der sie liest.

Die Oberfläche kennt das Problem, dass die Namen nirgends stehen: fährt man über das Feld
links oben, listet sie auf, was die Maschine dahinter überhaupt hergibt. Das kommt aus
`getFunctionInfo()`, und das Original macht es genauso.

Eine Kleinigkeit an der Basisklasse war nötig. `RadioTorchBaseBlock.updateShape` prüfte beim
Nachbarwechsel `canAttachTo` **direkt** statt über `canSurvive` — für die drei Fackeln, die an
jeder festen Wand halten, ist das dasselbe, aber der Leser braucht hinter sich eine Maschine
und muss abfallen, sobald sie verschwindet. Seine Überschreibung wäre ins Leere gelaufen. Im
Original ruft `onNeighborBlockChange` genauso das überschreibbare `canBlockStay`.

Beim Nachsehen, wie die Fackel zu bauen ist, fiel dann etwas Größeres auf: **keine der vier
Funkfackeln hatte ein Rezept.** Sie standen nur im Kreativreiter — und damit waren auch der
Fernschreiber und die sieben RBMK-Pulte nicht herstellbar, die sie als Zutat brauchen. Alle
vier sind jetzt nachgereicht (CraftingManager Z. 214, 215, 217, 218).

Dasselbe Nachsehen brachte sieben fehlende Übersetzungsschlüssel ans Licht: `RadioTorchScreen`
verlangt `container.rtty_sender`, `container.rtty_receiver` und fünf Hinweise, die nie jemand
eingetragen hat. Sender und Empfänger zeigten im Titel und in jedem Hinweis den rohen
Schlüssel. Das Sprach-Tor sieht so etwas nicht — es prüft Dopplungen und fehlende Block- und
Gegenstandsnamen, aber nicht, ob ein frei geschriebener Schlüssel auch eine Zeile hat.

Nicht übernommen: die sieben OpenComputers-Rückrufe, wie schon beim Zählventil.

Stand danach: fehlende Blockentitäten 119, Bauwerkslücke unverändert 14.

### Die Logikfackel — und ein Puffer, der nicht aufging

`radio_torch_logic` ist die letzte Funkfackel des Originals mit eigener Blockentität. Sie hört
auf einem Kanal, vergleicht die empfangene Nachricht der Reihe nach gegen **sechzehn
Bedingungen** und gibt die Nummer der ersten zutreffenden als Redstone-Stärke aus. Jede Zeile
ist eine Vergleichsart plus eine Konstante: die Arten 0 bis 5 rechnen (`<`, `<=`, `>=`, `>`,
`==`, `!=`) und überspringen die Zeile, wenn sich Nachricht oder Konstante nicht als Zahl lesen
lassen; 6 bis 9 vergleichen Zeichenketten (gleich, ungleich, enthält, enthält nicht). Die
Reihenfolge lässt sich umdrehen — aufsteigend gewinnt die kleinste zutreffende Zeile,
absteigend die größte.

Anders als das Original leitet die Blockentität hier von `RadioTorchBaseBlockEntity` ab statt
deren sechs Felder zu wiederholen. Sie benutzt alle davon außer `customMap`; neu sind nur
`descending` und die sechzehn Vergleichsarten.

Beim Erben fiel dann auf, warum das keine bloße Kosmetik war: **`serialize` und `deserialize`
der Basisklasse passten nicht zusammen.**

```java
for(int i = 0; i < 16; i++) if(mapping[i] != null) buf.writeUtf(this.mapping[i]);  // Schreiben
for(int i = 0; i < 16; i++) this.mapping[i] = buf.readUtf();                       // Lesen
```

`mapping` ist `new String[16]` und damit anfangs sechzehnmal `null`. Eine frisch gesetzte
Fackel schrieb also **gar keine** Zeichenkette, während die Gegenseite sechzehn las und im
Puffer ins Leere griff — bei jedem `networkPackNT`, also jeden Tick. Das `if` war offenbar dazu
da, den `NullPointerException` von `writeUtf(null)` abzufangen; das Original hat das Problem
nicht, weil sein `BufferUtil.writeString` `null` verträgt. Richtig ist, alle sechzehn zu
schreiben und `null` als leere Zeichenkette zu behandeln.

Das ist ein Fehler, den kein Tor auf dem Quelltext finden kann und den auch die
Datengenerierung nicht sieht: er braucht einen Client, der einem Server zuhört. Gefunden hat
ihn erst das Lesen der Klasse, in die man erbt.

Nachgereicht wurde außerdem das Rezept der Logikfackel (CraftingManager Z. 216), womit jetzt
alle fünf Funkfackeln herstellbar sind.

Stand danach: fehlende Blockentitäten 118, Bauwerkslücke unverändert 14.

### Der Ausguss der Gießerei — und wie man aus Löchern in der Textur ein Modell macht

`foundry_outlet` ist der Block, der die Schmelze aus einer Rinne nach unten fallen lässt statt
sie weiterzureichen. Er lagert selbst nichts (Fassungsvermögen null), nimmt nur von der Seite
an, an der er hängt, und sucht sein Ziel per Strahl vier Blöcke nach unten — dafür gibt es
`CrucibleUtil.getPouringTarget` seit Runde 19.

Davor sitzen zwei Sperren: ein Materialfilter (mit einem Schrottstück in der Hand gesetzt, mit
dem Schraubendreher gelöscht, mit dem Handbohrer umgekehrt) und ein Riegel, den ein
Redstonesignal schließt — auch der umkehrbar.

Interessant war das **Modell**. Der Ausguss hat im Original keinen Blockrenderer im üblichen
Sinn, sondern einen `ISimpleBlockRenderingHandler`, der neun Flächen einzeln zeichnet und dabei
UV-Ausschnitte aus 16×16-Bildern nimmt. Wie der Block aussieht, steht deshalb nicht im Code,
sondern **in der Transparenz der Texturen**. Das Alphabild von `foundry_outlet_front` liest
sich als Querschnitt:

```
.....#....#.....   <- zwei Wände,
.....#....#.....      dazwischen der Trog
.....######.....   <- Boden
```

und `foundry_outlet_top` ist eine U-Form mit einem Loch in der Mitte. Ausgemessen ergibt das
genau drei Kästen: Boden `[5,0,10]–[11,2,16]` und zwei Wände von je einem Pixel. Damit ist das
JSON-Modell dasselbe wie das der Rinne, nur mit `#front` auf den Z-Flächen — und es braucht
**keinen** Cutout, weil die Löcher in den Texturen genau dort sitzen, wo ohnehin keine Geometrie
ist. Nur die beiden Zusatzflächen (Filter und Riegel) sind echte Fensterscheiben und tragen
`render_type: cutout`.

ABWEICHUNG: Filter und Riegel stehen im **Blockzustand** (`filtered`, `closed`), nicht wie im
Original nur in der Blockentität. Das Original liest sie beim Zeichnen jedes Bild neu; auf 1.21
bräuchte das einen eigenen Renderer, als Zustand kennt sie das Modell unmittelbar. Die
Zustandsdatei ist dafür ein Multipart mit zwölf Zeilen — vier Drehungen für den Trog, vier für
den Filter, vier für den Riegel —, genau wie die Rinne ihre Anschlüsse hat.

Und wieder dasselbe Bild wie bei den Funkfackeln: **die ganze Gießerei hatte kein einziges
Rezept.** Rinne, Form und Becken stehen seit Runde 18 und 19 im Port und waren nur im
Kreativreiter zu haben. Alle vier sind jetzt nachgereicht (CraftingManager Z. 920 bis 924);
`Blocks.stone_slab` mit Metadatum 0 ist auf 1.21 `SMOOTH_STONE_SLAB`.

Offen aus derselben Aufgabe bleiben der Gießereitank (eigener Blockrenderer, der Wände
weglässt, wo ein Nachbartank steht, und den Füllstand über Blockgrenzen laufen lässt) und der
Schlackenabstich, der weiter an `BlockDynamicSlag` hängt.

Stand danach: fehlende Blockentitäten 117.

### Der Gießereitank — 81 Multipart-Zeilen, und warum es keine 129 sind

`foundry_tank` lagert vier Blöcke Schmelze und gibt sie in drei Stufen weiter: erst nach unten,
dann waagerecht an alles, was den Guss annimmt, zuletzt an die Nachbartanks, deren Füllstand er
dabei angleicht. Jeder fünfte Ausgleich **tauscht** die Stände statt sie zu halbieren — sonst
bliebe ein langer Strang auf halber Strecke stehen, weil die Hälfte der Hälfte irgendwann null
ist. Steht ein Tank neben einem anderen, fällt die Wand dazwischen weg: beide sind ein Behälter,
und die Oberfläche läuft über die Blockgrenze.

Das Aussehen hängt an vier unabhängigen Dingen, und das ist der ganze Aufwand:

| hängt ab von | was sich ändert |
|---|---|
| Tank an einer der sechs Seiten | die Wand dort fällt weg |
| Tank **unten** | die Außenwand trägt das Bild ohne Sockel (`_upper`) |
| Tank **oben** | die Innenflächen tragen `_bottom` statt `_inner` |
| Ausguss seitlich, der hierher zeigt | die Wand bekommt ein Loch (`_outlet`) |

Zusammen zehn Wahrheitswerte, also **1024 Blockzustände** — im Rahmen dessen, was Vanilla
selbst tut (Redstone-Staub hat 1296). Die Zustandsdatei erzeugt
`tools/gen-foundry-tank-models.py`: 81 Multipart-Zeilen und 23 Modelle.

Der interessante Teil war die **Zerlegung**. Das Original lässt seine vier Wände über die volle
Breite laufen und regelt die Ecken über bedingte Flächen — in einer Modelldatei geht das nicht,
weil zwei deckungsgleiche Flächen **mit gleicher Normalen** flackern. Ein Aufbau aus vier
Wandstücken (je 12 Pixel breit) und vier Ecken (je 2×2) ist überschneidungsfrei: dort, wo
Stücke aneinanderstoßen, zeigen die Normalen **auseinander**, und die Rückseitenaussonderung
nimmt jeweils eine weg. Jede Ecke hat dafür drei Fälle statt eines — sie steht, sobald
mindestens eine der beiden Wände neben ihr steht, und ihre beiden äußeren Flächen tragen je
nachdem das Außen- oder das Innenbild.

Die naive Rechnung wären 129 Zeilen gewesen. Dass es 81 sind, liegt an einer Messung: das Loch
für den Ausguss sitzt in `foundry_tank_side_outlet` genau in der Mitte (u 6–10, v 10–14),
also im 12 Pixel breiten Wandstück und **nie** in einer Ecke. Die Ecken brauchen die
Ausguss-Spielart daher nicht.

Vom Renderer blieb nur die Schmelze übrig: eine Fläche auf Höhe
`0.75 + je ein Achtel für einen Tank oben und unten`, mal dem Füllgrad, dazu die Seitenflächen
**dort, wo ein Tank steht**. Das klingt verkehrt herum, ist aber richtig: an einer geschlossenen
Wand sieht sie ohnehin niemand, und zwischen zwei Tanks mit ungleichem Stand wäre sonst eine
Lücke zu sehen.

Damit ist Aufgabe #90 bis auf den Schlackenabstich abgearbeitet; der hängt weiter an
`BlockDynamicSlag` mit `TileEntitySlag`, das der Port nicht hat.

Stand danach: fehlende Blockentitäten 116.

### Zwölf Blockentitäten, die im Port gar keine sind

`tools/be-blocker.py` hat bisher eine Gruppe mitgezählt, die es nicht gibt. Auf 1.7.10 gibt es
Blockentitäten, deren ganzer Inhalt so aussieht:

```java
public class TileEntityDecoBlock extends TileEntity {
    @Override public AxisAlignedBB getRenderBoundingBox() { return TileEntity.INFINITE_EXTENT_AABB; }
    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }
}
```

Sie existieren **nur**, damit ein TESR überhaupt zeichnen darf und nicht weggeschnitten wird.
Auf 1.21 zeichnet dort ein gebackenes Blockmodell, das weder Zeichengrenze noch Sichtweite
braucht — die Entität hat schlicht kein Gegenstück und soll auch keines bekommen.

Das Werkzeug erkennt sie jetzt mechanisch: kein Feld im Rumpf, und außer
`getRenderBoundingBox`, `getMaxRenderDistanceSquared`, `shouldRenderInPass` und
`getBlockMetadata` keine Methode. Kommentare zählen nicht mit, und das ist kein Detail — das
gelbe Fass hat sein ganzes `updateEntity` auskommentiert stehen. Gemessen in beiden Richtungen:
reine Zeichenhilfe wird erkannt, dieselbe Klasse mit einem `updateEntity` oder einem einzigen
Feld nicht mehr, eine leere Klasse ebenfalls nicht.

Dazu drei namentlich geführte Fälle, die der Port anders löst: `TileEntityData` (im Original
eine ganze Entität für zwei zusätzliche Metadatenbits — auf 1.21 trägt der Blockzustand
beliebig viele), `TileEntityDummy` (Platzhalter eines Mehrblockbaus, im Port macht das
`DummyableBlock` ohne Entität) und `TileEntityInventoryBase` (abstrakte Grundklasse, im Port
`MachineBaseBlockEntity`).

Damit sind von den 115 fehlenden Blockentitäten **zwölf gar keine Lücke**. Die Liste der sofort
Portierbaren schrumpft von 39 auf 27 — und die verbleibenden 27 sind echte Arbeit statt
Buchhaltung.

**Nachtrag, gleich beim ersten Gebrauch:** die Zahl ist nicht zwölf, sondern **vierzehn**. Der
nächste Blick auf die Liste fiel auf `TileEntityMachineUF6Tank`, und die Klasse ist *ganz leer*:

```java
public class TileEntityMachineUF6Tank extends TileEntity { }
```

Auch das ist ein reiner Aufhänger — der Block dazu hat `getRenderType() == -1`, wird also
ausschließlich vom TESR gezeichnet. Die Prüfung verlangte bisher mindestens eine Methode und
ließ genau diesen Fall durch. Gemessen: in der ganzen Liste gibt es zwei solche Klassen
(`machine_uf6_tank` und `machine_puf6_tank`), beide nachgesehen. Damit sind es 14 von 115, und
die Liste der sofort Portierbaren steht bei 25.

Dass die *Blöcke* `machine_uf6_tank` und `machine_puf6_tank` weiter fehlen, bleibt davon
unberührt — die Lücke ist ein Block, keine Blockentität. Genau dafür steht der Hinweis im Kopf
des Werkzeugs.

### Und eine vierte Gruppe: Blöcke, die es im Original nicht mehr gibt

Die nächste Runde sollte die **FEnSU** werden — der gewaltige Energiespeicher mit der
rotierenden Scheibe. Das Lesen des Originals brachte stattdessen:

```java
@Deprecated public static Block machine_fensu;
...
machine_fensu = new MachineFENSU(Material.iron).setBlockName("machine_fensu")
        .setHardness(5.0F).setResistance(10.0F).setCreativeTab(null)
```

Ausgemustert und durch `machine_battery_redd` ersetzt — den der Port längst hat, samt
`fensu2.obj` als Modell. Eine ganze Runde wäre in einen Block geflossen, den HBM selbst
herausgenommen hat.

`ModBlocks` führt **59** Felder als `@Deprecated` und **74** mit `setCreativeTab(null)`. Das
Werkzeug verfolgt jetzt die Kette Blockentität → Blockklasse → Feld in `ModBlocks` und meldet
zwei Stufen getrennt, denn sie sagen Verschiedenes:

* **ausgemustert** (`@Deprecated`) — acht Fälle, darunter die FEnSU, der BAT-9000, die alte
  große Turbine, der Sojus-Kern und die vier Chicago-Pile-Blöcke. Keine Lücke.
* **kein Kreativreiter** allein — fünf Fälle (`seal_hatch`, `lantern_behemoth`, die beiden
  UF6-Tanks, `obj_tester`). Das heißt *nicht*, dass es sie nicht gibt: Bauwerke und andere
  Blöcke setzen sie trotzdem. Sie stehen in einer eigenen Liste mit der Aufschrift
  „erst nachsehen“.

Damit liest sich die Bilanz der 115 fehlenden Blockentitäten so: **19 sind gar keine Lücke**,
5 gehören nachgesehen, 21 sind sofort portierbar, 70 hängen an etwas anderem. Vorher hieß es
39 sofort portierbar — und ein Drittel davon wäre Arbeit an Blöcken gewesen, die niemand je zu
sehen bekommt.

### Die Deuteriumkette — und zwei Tore, die zugeschlagen haben

`machine_deuterium_extractor` macht aus **fünfzig** Millilitern Wasser **einen** Milliliter
schweres Wasser und zieht dafür jede Sekunde ein Zwanzigstel seines Stromspeichers. Der
`machine_deuterium_tower` ist derselbe Vorgang im Großen: zehn Blöcke hoch, fünfzig Eimer
Wasser im Tank, fünf Eimer schweres Wasser, zehnmal so viel Strom — und acht Anschlussstellen
rings um seinen Fuß statt der sechs Würfelseiten.

Der Extraktor rechnet erst und multipliziert dann:

```java
int menge = Math.min(tanks[1].getMaxFill(), tanks[0].getFill()) / 50;
tanks[0].setFill(tanks[0].getFill() - menge * 50);
```

Das ist kein Umweg — so bleibt kein Rest im Wassertank hängen, den die Ganzzahldivision sonst
verschluckt hätte.

**Eine Frage musste gemessen werden.** Das Original dreht seine Anschlussstellen mit
`dir.getRotation(ForgeDirection.DOWN)`, und ob das nun `getClockWise` oder
`getCounterClockWise` ist, weiß man nicht auswendig. Also über alle portierten Mehrblockbauten
gezählt, welche Abbildung der Port bisher benutzt:

| Original | Port | Fälle |
|---|---|---|
| `getRotation(UP)` | `getClockWise()` | **41** |
| `getRotation(UP)` | `getCounterClockWise()` | 3 |
| `getRotation(DOWN)` | `getCounterClockWise()` | 2 |

Eindeutig — und die drei Ausreißer (`CondenserPowered`, `MachineCompressor`,
`MachineCompressorCompact`) sind jetzt eine eigene Aufgabe, denn wenn sie ihre Vorzeichen nicht
anderweitig ausgleichen, sitzen ihre Rohr- und Stromanschlüsse gespiegelt.

**Zwei Tore haben zugeschlagen**, beide zu Recht:

* `override-check`: der Extraktor erbte von `MachineBaseBlockEntity`, und die ist ein
  `MenuProvider` — nur hat diese Maschine gar keine Oberfläche. Das Original leitet von
  `TileEntityMachineBase` mit **null Fächern** ab; im Port wäre das ein Menüanbieter ohne Menü.
  Jetzt steht dort `LoadedBaseBlockEntity`.
* `renderbox-check`: der Turm hatte keinen eigenen Sichtkasten. Sein Modell ist zehn Blöcke
  hoch, der Kern sitzt unten — ohne Sichtkasten verschwindet der ganze Turm, sobald man zu weit
  nach oben schaut. Das ist genau der Fehler, für den das Tor nach der Leviathan-Turbine
  gebaut wurde.

Die beiden Ambossrezepte sind mit übernommen. `Fluids.SOURGAS.getDict(1_000)` des Originals —
ein Eintrag für einen vollen Fluidtank — ist im Port `FLUID_TANK_FULL`, dessen Fluid im
Metadatum steht; `CU.plateCast()` die kupferne Gussplatte, `EnumCircuitType.BASIC` die
integrierte Leiterplatte.

Stand danach: fehlende Blockentitäten 114.

**Nachtrag zur vierten Gruppe:** beim Durchsehen der verbliebenen Liste kamen noch drei Fälle
dazu, alle drei einzeln nachgesehen.

`TileEntityMachineLPW2` ist dieselbe reine Zeichenhilfe wie die Deko-Blöcke, merkt sich ihren
Sichtkasten aber in einem Feld — und die Prüfung weist jedes Feld ab. Namentlich eingetragen
statt die Regel aufzuweichen.

`TileEntitySellafield` und `TileEntityFF` erzeugt **kein einziger registrierter Block**: ihre
Blockklassen (`Sellafield`, `BlockFF`) stehen in keiner Zeile von `ModBlocks` und haben auch
keine Unterklasse. Die Sellafield-Blockentität wird sogar noch angesprochen — `world/dungeon/
Barrel.java` setzt ihren Radius auf 2.5 —, nur kann sie nie entstehen.

Daraus wäre beinahe eine fünfte automatische Regel geworden: „kein Block erzeugt sie“. Gemessen
liefert sie 24 Treffer — und **taugt nicht**, denn zehn davon sind abstrakte Grundklassen, und
bei `TileEntityCharge` hebelt Vererbung sie aus: `BlockChargeBase` steht selbst in keiner Zeile
von `ModBlocks`, hat aber vier Unterklassen (C4, Dynamit, Semtex, Bohrladung), die alle live
sind. Eine Regel, die eine Sprengladung als tot meldet, ist keine Regel. Deshalb drei
namentliche Einträge statt einer schlechten Automatik.

Stand: 113 fehlende Blockentitäten, davon 22 gar keine Lücke, 5 zum Nachsehen, 16 sofort
portierbar, 70 blockiert.

### Die Zapfsäule — und zwei Techniken, die es auf 1.21 nicht mehr gibt

`machine_refueler` betankt alles, was sich betanken lässt, an dem, der auf ihr steht — **auch
die Module in den Rüstungsteilen**, denn genau dort steckt der Treibstoff des Jetpacks. Der Tank
fasst hundert Milliliter; er ist kein Lager, sondern ein Durchlauf. Was gezapft wird, stellt ein
Fluidkennzeichner am Block ein.

Zweimal musste eine 1.7.10-Technik ersetzt werden, und beide Male ist das Ergebnis eine
dokumentierte Abweichung, keine Nachbildung:

**Die Klipp-Ebene.** Das Original schiebt die Flüssigkeitssäule nach unten aus dem Gehäuse
heraus und schneidet sie mit `GL_CLIP_PLANE0` bei `y = 0.125` ab — so scheint sie von unten
aufzusteigen. Klipp-Ebenen gibt es auf 1.21 nicht mehr. Stattdessen wird die Säule um dieselbe
Höhe **gestaucht**, um genau diese Schnitthöhe herum. Für einen Quader wäre das dasselbe Bild;
das Teil hat aber vierzehn Ecken, also bleibt beim Stand null hier nichts stehen, wo das
Original noch einen schmalen Rest zeigt.

**Der eingefärbte Partikel.** Das Original nimmt einen Kritzel-Partikel (`EntityCritFX`) und
setzt dessen Farbe von Hand — beides aus der Blockentität heraus, weil der ganze Zweig ohnehin
nur auf dem Client läuft. Auf 1.21 ginge das nicht, ohne aus einer Blockentität eine
Clientklasse anzufassen, und genau das verbietet `dist-check`. Der Staubpartikel trägt seine
Farbe dagegen **im Partikeltyp** und läuft über `level.addParticle`, das auf beiden Seiten
steht. Lage und Bewegung sind unverändert übernommen.

Die Fächerschleife des Originals (`for(int i = 0; i < 5; i++) player.getEquipmentInSlot(i)`)
ist auf 1.7.10 „Hand plus vier Rüstungsteile“ — im Port also die vier Rüstungsfächer und
zusätzlich die Haupthand.

Stand danach: fehlende Blockentitäten 113.

### Tor 29: dreht jeder Bau seine Anschlüsse so herum wie das Original?

Der Verdacht aus der Deuteriumrunde — drei Maschinen mit der falschen Drehrichtung — hat sich
beim Nachsehen **nicht bestätigt**, und zwar aus einem Grund, den man der Zeile nicht ansieht.
Alle drei Anschlusslisten sind **symmetrisch in `rot`**:

```java
new DirPos(x + rot.getStepX() * 2, y, z + rot.getStepZ() * 2, rot),
new DirPos(x - rot.getStepX() * 2, y, z - rot.getStepZ() * 2, rot.getOpposite()),
```

Zu jedem Eintrag mit `+rot` gibt es einen mit `-rot` und derselben Richtung. Dreht man `rot`
um, vertauschen sich die beiden — die **Menge** der Anschlussstellen bleibt gleich. Folgenlos.

Was aber bleibt: die Frage „ist `getRotation(UP)` nun `getClockWise` oder `getCounterClockWise`“
stellt sich bei **jedem** Mehrblockbau neu, man weiß es nicht auswendig, und rät man falsch,
sitzen sämtliche Anschlüsse gespiegelt — die Maschine nimmt nichts an und gibt nichts ab, ohne
dass irgendetwas abstürzt. Genau dafür ist ein Tor da.

`tools/rotation-check.sh` vergleicht jetzt jeden Bau gegen seine Vorlage. Die Regel ist nicht
nachgeschlagen, sondern **gezählt**: 41 zu 2 für „UP ist im Uhrzeigersinn“. Die drei
symmetrischen Fälle stehen als Ausnahmen mit Begründung im Skript — und wenn eine davon später
begradigt wird, meldet das Tor die Ausnahme als überflüssig und wird rot, damit die Zeile
verschwindet. Dieselbe Schuldenlisten-Mechanik wie bei `bewlr-check` und `inventory-check`.

Gemessen in beiden Richtungen: 76 Vorlagen, 62 Bauten im Port, 0 Abweichungen. Dreht man
`FurnaceIronBlockEntity` zurück, meldet das Tor genau diesen einen Bau und endet mit 1.

Was es nicht sieht, nennt es beim Namen statt zu schweigen: neun Dateien, in denen Original
oder Port mehrere Achsen benutzen (dort gibt es mehrere Anschlusslisten, ein Vergleich je Datei
taugt nicht), und eine, zu der die Liste keine Vorlage findet
(`MachineIndustrialBoilerBlockEntity` — im Original heißt sie mit vertauschten Wörtern).

Damit stehen 29 gemessene Tore.

### Ein blinder Fleck im Werkzeug: Erben ohne Import

`tools/be-blocker.py` liest bisher nur die `import com.hbm...`-Zeilen einer Klasse und meldet
sie als *sofort portierbar*, wenn davon keine im Port fehlt. Beim Durchsehen der Liste fiel
`TileEntityPipeAnchor` auf — angeblich sofort portierbar, tatsächlich:

```java
public class TileEntityPipeAnchor extends TileEntityPipelineBase {
```

`TileEntityPipelineBase` liegt im **selben Paket**, steht also in keiner import-Zeile und war
für das Werkzeug unsichtbar. Der Port hat diese ganze Grundklasse nicht. Genau die Klasse, an
der die meiste Arbeit hängt, fehlte in der Bilanz.

Das Werkzeug liest jetzt zusätzlich den Klassenkopf zwischen dem eigenen Namen und der
öffnenden Klammer. Zwei Versuche davor waren falsch und sind verworfen:

* Der erste nahm jeden Grossbuchstabennamen ab `class` — damit zählte die Klasse ihren *eigenen*
  Namen als fehlende Abhängigkeit, und die Liste der Portierbaren fiel auf **null**.
* Der zweite nahm jeden Grossbuchstabennamen nach dem eigenen — damit zählte das vanilla
  `TileEntity` mit, und `DecoBlockAltF` und `ChlorineSeal` fielen zu Unrecht heraus.

Die dritte Fassung schneidet die Namen aus dem Kopf mit den Klassennamen, die es im Original
unterhalb von `com/hbm/` als Datei *gibt*. Vanilla-Oberklassen fallen damit heraus,
Projekttypen nicht.

Gemessen: sofort portierbar **15 → 13**. Die zwei, die herausfallen, sind beide echte Treffer —
`TileEntityPipeAnchor` (erbt `TileEntityPipelineBase`) und `TileEntityRequestNetworkContainer`
(erbt `TileEntityRequestNetwork`), beide gleiches Paket, beide im Port nicht vorhanden. Kein
Eintrag fällt zu Unrecht heraus.

## Der Schlackenabstich und die Pfütze darunter (Aufgabe #96)

Der letzte offene Punkt der Gießerei. Der Abstich selbst ist im Original nichts als der
Ausguss mit fünf anderen Texturen und einer anderen Blockentität — im Port genauso: er erbt
von `FoundryOutletBlock`, Form, Filter, Riegel, Werkzeugverhalten und Kopiervorlage kommen
unverändert von dort. Blockiert war er nie an sich selbst, sondern an dem, wohin er abläuft:
`BlockDynamicSlag` samt `TileEntitySlag`, die der Port gar nicht hatte.

### Die Pfütze

`slag` ist eine Lache, die nach unten fällt, sich mit der Lache darunter vereinigt und sich zur
Seite ausbreitet, sobald ein Fünftel beisammen ist. Sie hat keinen Gegenstand und steht in
keinem Reiter; abgebaut gibt sie ihren ganzen Inhalt als Schrottklumpen zurück, nicht sich
selbst.

**Zwei Abweichungen, beide bewusst.**

*Die Höhe.* Das Original liest die Blockgrenzen bei jedem Bild aus der Blockentität. Auf 1.21
wäre das ein dynamischer Umriss samt eigenem Renderer. Im Port steht die Höhe in Sechzehnteln
als `LEVEL` im Blockzustand: sechzehn winzige Modelle, ein normal zwischenspeicherbarer Umriss,
kein Renderer. Die genaue Menge bleibt in der Blockentität — gerundet wird nur, was man sieht.

*Die Farbe.* Das Original baut sich beim Laden für jedes Material eine eigene Textur und bildet
Weiß auf die helle, `0x505050` auf die dunkle Materialfarbe ab. Ein Farbhandler kann nur
multiplizieren, nicht zwei Stützstellen abbilden, also bleibt es bei der hellen Farbe auf einer
Graustufentextur. Das ist dasselbe Verfahren, das der Port schon für den Schrottklumpen benutzt,
und dort schon so begründet.

### Der Strahl, der auch ins Leere treffen darf

Der Abstich sucht sein Ziel fünfzehn Blöcke weit nach unten. Das Original benutzt dafür
`func_147447_a(..., returnLastUncollidedBlock = true)` — der entscheidende Teil ist das letzte
Argument: läuft der Strahl ins Leere, kommt trotzdem eine Blockstelle zurück, nämlich die
zuletzt durchquerte. Genau darauf beruht der Zweig `hit.isReplaceable(...)`, der sonst nie
anspringen würde.

Auf 1.21 tut `level.clip` dasselbe: ein Fehlschlag trägt die Stelle am Strahlende. Deshalb
prüft der Port hier **nicht** auf `HitResult.Type.BLOCK`, sondern benutzt Treffer und Fehlschlag
gleichermaßen. Wer die Prüfung einbaute, hätte den ganzen Freifall-Zweig stillgelegt und es nie
gemerkt — die Schlacke bliebe einfach am Abstich hängen.

Dazu eine Schranke, die das Original nicht braucht: `setze` setzt nichts außerhalb der
Weltgrenzen. Auf 1.7.10 liegt der Boden bei y = 0, auf 1.21 kann der Strahl unter die
Untergrenze laufen — `setBlock` scheitert dann still, und das anschließende `getBlockEntity`
griffe ins Leere.

### Ein Loch aus Runde #90, das erst jetzt auffiel

Die Rinne hat sich im Port nie mit dem Ausguss verbunden. Die Schmelze floss hinein — das läuft
über `ICrucibleAcceptor` am Block und war richtig —, aber `canConnectTo` kannte nur Rinnen und
Formen, zeichnete also keinen Stutzen dorthin. Das Original prüft an derselben Stelle
`meta == dir.ordinal()`: der Ausguss zählt, wenn er von der Rinne **wegzeigt**, denn nur dann
hängt sein Trog auf der der Rinne zugewandten Seite, und nur dann nimmt seine Blockentität den
Zulauf überhaupt an. Jetzt prüft der Port dasselbe, und der Abstich zählt mit.

### Zwei Tore haben zugeschlagen

`api-check` fand, dass `FoundryOutletBlock.codec()` den eigenen Typ ohne Platzhalter festlegt.
Solange niemand von der Klasse erbte, war das harmlos; mit dem Abstich als Ableitung nicht mehr,
denn `MapCodec` ist invariant. Richtig ist `MapCodec<? extends FoundryOutletBlock>`.

`model-resolve-check` fand, dass `slag` mit blankem `BLOCKS.register` angelegt ist, also keinen
Gegenstand bekommt, und verlangte den Eintrag samt Begründung in `OHNE_GEGENSTAND` — genau
wofür die Prüfung seit dem Giftblock da ist.

### Ein Nachtrag zur Baseline des Syntax-Tors

Beim Durchlauf meldete `syntax-check` zwei neue Fundstellen, beide aus der Deuteriumrunde:
`DeuteriumExtractorBlockEntity` und `DeuteriumTowerBlockEntity`, jeweils *types
IFluidStandardSenderMK2 and IFluidStandardSenderMK2 are incompatible*. Dieselbe Schnittstelle
auf beiden Seiten — die längst belegte Kategorie 1 der Baseline, ein Folgefehler der fehlenden
Fremd-API. Gegenprobe wie dort beschrieben: `javac -sourcepath src/main/java
src/main/java/api/hbm/fluidmk2/*.java` übersetzt dieselbe Hierarchie fehlerfrei, null Meldungen.

Bemerkenswert ist, dass die CI die Deuteriumrunde grün gemeldet hat. Die Meldung hängt daran,
in welcher Reihenfolge `find` die Dateien liefert; javac meldet den vermeintlichen Konflikt nur
in einer der Übersetzungseinheiten, und welche das ist, entscheidet die Dateireihenfolge. Mit
den beiden Zeilen in der Baseline ist das dauerhaft erledigt.

Stand danach: Aufgabe #96 abgeschlossen, die Gießerei damit vollständig.

## Die FSB-Rüstung, der HEV-Anzug und der Akku (Aufgaben #101 und #84)

`hev_battery` war der letzte Block, der an einem ganzen Teilsystem hing: der
Vollsatz-Rüstung. Der Block prüft `ArmorFSB.hasFSBArmorIgnoreCharge` und ob am Körper ein
bestromtes Teil sitzt — beides gab es im Port nicht. Diese Runde legt die Grundlage und
reicht mit dem HEV-Anzug den Satz nach, für den der Akku gemacht ist.

### Was „Full Set Bonus" heißt

`ArmorFSBItem` ist die Wurzel. Getragen werden die Teile einzeln, aber Trankwirkungen und
Geigerton springen erst an, wenn alle vier aus demselben Material am Körper sind. Geprüft wird
das an der **Brustplatte** — nur sie weiß, ob ihr Satz überhaupt einen Helm vorsieht
(`noHelmet`). Zwei Prüfungen gibt es: `hasFSBArmor` verlangt zusätzlich, dass jedes Teil
arbeitsfähig ist (bei bestromten also Ladung hat), `hasFSBArmorIgnoreCharge` nicht. Genau diese
zweite fragt der Akku ab, denn einen leeren Anzug soll man ja gerade aufladen können.

**Zwei Abweichungen bei der Anmeldung.**

Das Original hängt Gefahrenklassen und Strahlenschutz mit Baukastenmethoden an den Gegenstand
(`setHazardClass`, `setRadResist`) und sammelt sie in Listen, die später abgearbeitet werden.
Der Port hat dafür längst zwei zentrale Stellen — `ArmorUtil.register` und
`HazmatRegistry.initDefault` —, und dort steht der HEV-Anzug jetzt auch. Zwei Wege für
dieselbe Sache wären einer zu viel.

Das Original schreibt außerdem die Eigenschaften nur am Helm aus und lässt die drei anderen
Teile sie mit `cloneStats` vom **fertigen** Helm abschreiben. Das setzt eine Reihenfolge
voraus. Im Port baut eine private Vorschrift alle vier gleich, `cloneStats` gibt es gar nicht
erst — eine Methode ohne Aufrufer wäre toter Code.

**Was bewusst fehlt:** VATS, Wärmesicht, der Sprung („dash"), Schritt- und Sprunggeräusche und
die Helmscheibe. Alle fünf hängen an Teilsystemen, die der Port nicht hat. Sie stehen hier auch
nicht als Schalter herum — ein Feld, das niemand liest, ist toter Zustand.

### Der Akku als Haltbarkeit

`ArmorFSBPoweredItem` ist die bestromte Fassung. Ohne Ladung liefert `isArmorEnabled` falsch,
und damit fällt der ganze Satzbonus weg.

Die Feinheit steckt im Schaden: das Original leitet `setDamage` auf den Akku um, ein Teil
nutzt sich also nie ab, sondern verliert Ladung (2500 HE je Schadenspunkt). Auf 1.21 ist das
`damageItem`, das schlicht null zurückgibt und stattdessen entlädt. Damit der Balken nicht
lügt, zeigen `isBarVisible`, `getBarWidth` **und `getBarColor`** die Ladung statt der
Haltbarkeit — die Farbe gehört dazu, sonst bliebe der Balken immer grün, weil der Schadenswert
sich nie ändert.

Der Akku-Aufsatz `ItemModBattery`, der im Original das Fassungsvermögen vergrößert, fehlt dem
Port noch; `getMaxCharge` gibt darum den festen Wert.

### Der HEV-Anzug

Das Modell war der einfachste Teil: `armor_hev` samt vier Texturen liegt seit der
Texturrunde im Port, und `ModelArmorBase` bringt sogar schon `leftFoot`/`rightFoot` und die
Kniebeuge mit — es war für genau solche Anzüge geschrieben. `ModelArmorHEV` ist deshalb
kaum mehr als die Zuordnung Platz → Modellteile.

Eine Abweichung: das Original setzt die Drehpunkte von Armen und Beinen von Hand
(`setRotationPoint(5, 2, 0)` und so weiter). Der Port übernimmt sie aus dem Spielermodell, und
damit sitzen sie auch beim knienden Spieler und beim Kindmodell richtig, was die festen Werte
nicht leisten.

Die **Anzeige** ersetzt Herzen und Rüstungsbalken durch Lebenspunkte und Ladung als Zahl, dazu
einen Strahlungsbalken und die Dosisrate. Das Original fängt dafür die Teilereignisse `ARMOR`
und `HEALTH` ab; auf 1.21 sind das `VanillaGuiLayers.ARMOR_LEVEL` und
`VanillaGuiLayers.PLAYER_HEALTH` in `RenderGuiLayerEvent.Pre`, beide `setCanceled`. Eine
Kleinigkeit: der Balken beginnt im Original mit dem Zeichen ☢. Quelldateien dieses Ports
enthalten keine Sonderzeichen, hier steht deshalb `RAD [`.

### Der Akku: zwei Dinge mit einem Namen

Und hier lag die eigentliche Überraschung. Im Original sind `hev_battery` **zwei** Einträge:
ein Block (`ModBlocks.hev_battery`) und ein Gegenstand (`ModItems.hev_battery`), die sich per
formlosem Rezept ineinander umwandeln lassen. Auf 1.7.10 geht das, weil Blöcke und Gegenstände
getrennte Namensräume haben (`tile.` und `item.`); auf 1.21 wären es zwei Einträge auf
demselben Schlüssel `hbmsntm:hev_battery`.

Der Port legt beides in einen Gegenstand: `HEVBatteryItem` ist der Blockgegenstand des
Wandakkus und lädt, in die Luft geklickt, denselben Satz auf wie der Block. Die beiden
Umwandlungsrezepte entfallen — sie wären Rezepte von einem Gegenstand auf sich selbst.

Beim Nachlesen fiel noch ein Widerspruch im Original auf: der **Block** prüft
`armorInventory[3]`, also den Helm, auf `ArmorFSBPowered`, der **Gegenstand** dagegen
`armorInventory[2]`, die Brustplatte. Bei einem einheitlichen Satz ist das dasselbe; der Port
prüft an beiden Stellen die Brustplatte, weil sie ohnehin über den Satz entscheidet.

Das Modell des Blocks ist im Original ein Wellenfrontmodell ohne Blockentität
(`ISimpleBlockRenderingHandler`). Auf 1.21 gibt es diesen Weg nicht; eine Blockentität nur
zum Zeichnen lehnt der Port ab (siehe die Gruppe „nur zum Zeichnen" in `be-blocker.py`). Also
ein gebackenes JSON-Modell: zwei um 45 Grad gegeneinander gedrehte Kästen für das
achteckige Prisma, ein Kasten für den Stutzen, einer für das Wandblech.

### Was den HEV-Anzug noch nicht baubar macht

Die vier Rezepte des Originals brauchen die Titanrüstung und den Deshmotor, beides fehlt dem
Port. Solange steht der Anzug im Kreativreiter — im Original steht er in keinem, weil man ihn
dort bauen kann. Ohne beides wäre er sonst gar nicht zu bekommen, und genau dafür gibt es das
Reiter-Tor.

Stand danach: Aufgabe #84 abgeschlossen, die Liste der blockierten Blöcke steht bei elf.

## Berichtigung: die zwei Bauteile, die gar nicht portiert waren

Beim Nachmessen für Aufgabe #80 — welche Beuteeinträge der Kisten hat der Port inzwischen? —
stand oben im Abschnitt über die Beutekisten der Satz „der Metallkiste [fehlen] zwei
(`centrifuge_element`, `piston_selenium`). **Die beiden habe ich portiert.**"

Das war schlicht falsch. Keiner der beiden Gegenstände existierte im Port, und die Liste der
Metallkiste in `CrateLoot` zählte entsprechend fünfzehn Einträge statt siebzehn. Der Satz hat
seitdem dagestanden und niemanden gestört, weil niemand nachgezählt hat.

Beim Nachreichen kam ein zweiter Fund dazu: das Rezept für den **Zentrifugenaufsatz** nahm
ersatzweise die Durastahlplatte, wo das Original das Zentrifugenelement verlangt — ohne
Vermerk. Eine stille Ersetzung ist schlimmer als eine offene Lücke, denn sie sieht im
Rezeptbuch aus wie das Original.

Nachgereicht sind daher:

- `centrifuge_element` samt Montagerezept (`ass.centrifugetower`: vier Durastahlplatten, vier
  Titanplatten, ein Motor) und das berichtigte Rezept des Zentrifugenaufsatzes.
- `piston_selenium` samt Werkbankrezept (`CraftingManager` Z. 596).
- Beide in der Metallkiste, mit den Gewichten des Originals (je 6).

**Der dritte Eintrag, `pellet_rtg_weak`, war kein Fehler, sondern eine Namensfrage.** Im Port
ist das schwache Pellet kein eigener Gegenstand, sondern der Untertyp `WEAK` von `pellet_rtg`
— die Metadatenfaltung, die der Port überall anwendet. Es fehlte trotzdem in der Bleikiste,
weil dort nur der Grundtyp stand. Jetzt steht es als Metadatenstapel daneben, Gewicht 7 wie im
Original.

Damit sind Blei- und Metallkiste **vollständig**: 23 und 17 Einträge, genau die des Originals.
Die sechs blockierten Kisten hängen weiter an Spritzen, Granaten, `ammo_container`, drei Waffen
(`gun_heavy_revolver`, `gun_liberator`, `gun_panzerschreck`) und den zehn Sonderstücken der
roten Kiste.

## Die Spritzen, die Kronkorken und die Munitionskiste

Erster echter Fortschritt an Aufgabe #80. Die sechs blockierten Kisten hängen an sehr
unterschiedlich großen Brocken; `crate_ammo` war der kleinste, und er ist jetzt weg.

### Was die Munitionskiste brauchte

Anders als die Beute- und Metallkiste zieht sie nicht aus einer gewichteten Liste: ihr Inhalt
steht fest und wird nur ausgewürfelt. Kronkorken und Stimpaks liegen immer drin, zwölf Sorten
Handfeuermunition je mit halber Wahrscheinlichkeit, zwei schwere ebenso, und mit einem Zehntel
noch zwei Superstimpaks.

Von alldem fehlte im Port genau dreierlei: `cap_nuka`, die Stimpaks und die Kiste selbst. Die
vierzehn Munitionssorten gibt es längst — `ammo_standard` steht seit der Waffenrunde als
Metadaten-Gegenstand da.

### Die Trankübelkeit — die erste eigene Zustandswirkung des Ports

Eine Spritze wirkt nur, wenn keine Übelkeit anliegt; ohne diese Sperre ließe sich mit einem
Stapel Stimpaks jede Verletzung wegklicken. Im Original ist das `HbmPotion.potionsickness`,
eines von elf eigenen Tränken, die sich ihre Kennungen **per Reflexion** ins Vanille-Feld
`potionTypes` schreiben und ihre Symbole aus einem einzigen Blatt schneiden.

Auf 1.21 ist `MobEffect` ein gewöhnliches Register. `NtmMobEffects` ist damit die erste
Registrierung dieser Art im Port, und vorerst steht nur dieser eine Eintrag darin — die
übrigen zehn des Originals löst der Port anders (Strahlung etwa steht in
`HbmLivingAttachments`, nicht als Zustandswirkung).

Zwei Kleinigkeiten:

*Die Kategorie.* Das Original kennt nur „gut" und „böse" und trägt hier „gut" ein. Auf 1.21
gibt es `NEUTRAL`, und das trifft es besser — die Übelkeit hilft nicht, sie schadet aber auch
nicht, sie sperrt nur.

*Das Symbol.* Jede Wirkung braucht auf 1.21 eine eigene Datei unter `textures/mob_effect/`.
Das Bild ist aus dem Trankblatt des Originals geschnitten. Die Fundstelle war nicht offensichtlich:
`registerPotion(..., x, y)` schreibt den Index `x + y*8`, und der Zeichner liest ihn als
`((i % 8) * 18, 198 + (i / 8) * 18)` — die Symbole liegen also **unten** im 256×256-Blatt, nicht
oben. Der erste Schnitt bei (54, 18) ergab ein leeres Feld; richtig ist (54, 216).

### Die Spritzen

`ItemSyringe` ist im Original **eine** Klasse mit einer Kette aus `if(this == ModItems.xyz)`,
eine Abfrage je Spritze — der Quelltext trägt dort nicht umsonst die Marke `@Spaghetti`. Der
Port hängt die Wirkung stattdessen an den Gegenstand: jede Spritze bekommt bei der Anmeldung
ihre eigene. Die Zahlen sind unverändert (Stimpak 5 Leben, Med-X Resistenz III vier Minuten,
Psycho Resistenz I und Stärke I je zwei Minuten, Superstimpak 25 Leben und Langsamkeit I).

Portiert sind die vier Metallspritzen und die beiden leeren Hülsen. Nicht portiert: `syringe_taint`
und `syringe_mkunicorn` (beide brauchen die Verseuchung), die Blutbeutel und der Sanitätsbeutel
(`ItemSimpleConsumable`, ein eigenes Teilsystem).

Beim Rezept fehlen zwei Wege: das formlose Stimpak aus drei Nitra-Krumen (`nitra_small` fehlt)
und das **Superstimpak** (braucht `bottle_nuka` oder `bottle_cherry`). Das Superstimpak bleibt
darum vorerst reine Beute aus der Munitionskiste.

### Ein Tor, das an einem Namen anschlug

`api-check` meldete auf einmal fünf Funde in `RadioTelexScreen` — einer Datei, die diese Runde
gar nicht anfasst. Der Grund: das Tor leitet den Typ eines Bezeichners aus **allen**
Erklärungen im Projekt her, und `inhalt` war bis dahin überall eine Zeichenkette. Meine neue
Methode `inhalt(RandomSource)` gab eine Liste zurück — damit galt `inhalt` als Sammlung, und
jedes `inhalt.length()` im Fernschreiber sah nach einem Fehler aus.

Der Fund war falsch, der Anlass nicht: zwei Bedeutungen desselben Namens im selben Projekt sind
genau das, wovor das Tor warnt. Die Methode heißt jetzt `wuerfleInhalt`.

Stand danach: fünf der sechs Kisten bleiben blockiert. `crate` und `crate_supply` an Spritzen
(teilweise da), Granaten und `ammo_container`; `crate_weapon` an drei Waffen; `crate_red` an
zehn Sonderstücken; `crate_can` an der ganzen Dosen- und Nahrungsfamilie.

## Der schwere Revolver

Erste der drei Waffen, die `crate_weapon` noch fehlen (Aufgabe #102). Sie war die billigste,
denn ihr Modell liegt längst im Port: `lilmac.obj`, dasselbe, das die Debugwaffe benutzt.

### Ein Zeichner für zwei Waffen

Im Original nimmt `ItemRenderHeavyRevolver` seine Textur im Konstruktor entgegen — derselbe
Zeichner bedient den schweren Revolver und den Lilmac. Der Port hatte davon bisher nur eine
Kopie unter dem Namen `ItemRenderDebug`, mit fest eingebauter Debugtextur.

Statt die Kopie ein zweites Mal zu kopieren, ist sie jetzt die Vorlage: `ItemRenderHeavyRevolver`
trägt den Rumpf und nimmt die Textur herein, `ItemRenderDebug` ist auf zwei Zeilen
zusammengeschrumpft. Das ist genau der Aufbau des Originals.

Eine Abweichung: das Original zeichnet auf Wunsch noch ein Zielfernrohr und weitet dafür das
Blickfeld. Den Aufsatz gibt es im Port nicht, also auch das nicht — und aus demselben Grund
entfällt der `setNameMutator`, der bei aufgesetztem Fernrohr „_scoped" an den Namen hängt.

### Ein Tippfehler, der bleiben muss

Die Nachladebewegung benutzt einen Bus namens `RELAOD_TILT`. Das ist ein Vertipper des
Originals — aber er steht auf **beiden** Seiten, in der Animation und im Zeichner. Wer ihn hier
richtigstellte, ohne den Zeichner anzufassen, bekäme eine stumme Bewegung: der Zeichner fragte
nach einem Bus, den niemand mehr bespielt. Er bleibt also stehen, mit einem Kommentar daneben.

### Was die Waffe sonst braucht

`ORCHESTRA_NOPIP` ist neu in `Orchestras`: vier Töne beim Nachladen (Hahn spannen, Trommel
heraus, Trommel zurück, schließen), der Mündungsblitz beim Schuss, das Klicken beim leeren
Abzug. Die Zählerstände sind unverändert. Anders als beim Hangman, wo die Tonzeilen beim
Portieren auskommentiert stehen blieben, sind hier alle Tonereignisse vorhanden und in Betrieb.

Ein Rezept bekommt sie nicht — im Port hat **keine** Waffe eines, weil die Waffenteile des
Materialsystems (`lightBarrel`, `lightReceiver`, `mechanism`, `grip`) noch fehlen. Sie steht
wie ihre Geschwister im Kreativreiter.

Bleiben für `crate_weapon`: `gun_liberator` (Modell und Zeichner sind im Original da, müssen
aber übernommen werden) und `gun_panzerschreck` (braucht die ganze Raketenfabrik
`XFactoryRocket`, die der Port nicht hat).

## Die Liberator

Zweite der drei Waffen für `crate_weapon`. Eine vierläufige Kipplaufwaffe, einzeln geladen —
und damit die erste, deren Bewegung vom Füllstand abhängt.

### Vier Hülsen, vier Zweige, eine Regel

Das Original schreibt für jeden Füllstand einen eigenen Animationszweig aus: vier bis zur
Verwechslung ähnliche Blöcke, in denen nur die Nummer der gerade bewegten Hülse wandert, und
das zweimal (für `RELOAD` und `RELOAD_CYCLE`). Dahinter steht eine einzige Regel:

> Alles unter der bewegten Hülse steckt schon im Lauf, die bewegte fliegt gerade herein, alles
> darüber liegt noch draußen.

Im Port steht genau das als Schleife. Ein Detail entfällt dabei: das Original hängt für die
jeweils nicht gemeinten Hülsen einen Bus namens `NULL` an, den niemand liest — ein Kunstgriff,
um in einer Kette aus `addBus`-Aufrufen einen Zweig stumm zu schalten. In einer Schleife kommt
jede Hülse ohnehin genau einmal vor.

Der Unterschied zwischen `RELOAD` und `RELOAD_CYCLE` ist **ein Index**: beim ersten zeigt das
Magazin noch den Stand vor dem Nachladen, beim zweiten ist die erste Patrone schon verbucht.
Dasselbe gilt für `INSPECT` gegenüber `RELOAD_END`. Beides steht im Original als unterschiedliche
Vergleiche (`ammo >= i` gegen `ammo > i`) und ist hier als unterschiedlicher Zählerstand
übernommen.

### Ein Tippfehler, der bleiben durfte

Beim Nachsehen wirft das Original die Hülsen mit
`-15F * entity.getRNG().nextGaussian() * 7.5F` aus — beim Nachladen an derselben Stelle mit
`-15F + ...`. Das `*` statt `+` ist offensichtlich ein Vertipper; er wirft die Hülsen mal
nach links, mal nach rechts, je nach Vorzeichen der Zufallszahl. Übernommen, weil er nur den
Wurfwinkel betrifft und nichts kaputtmacht — aber mit einem Vermerk, damit ihn niemand für
Absicht hält.

### Der Ton hieß anders

`GUN_LIBERATOR_FIRE` zeigt im Original auf `weapon/fire/shotgunAlt`. Der Port schreibt
Dateinamen klein mit Unterstrich, die Datei heißt hier also `shotgun_alt.ogg`.

Bleibt für `crate_weapon` nur noch `gun_panzerschreck` — und der braucht die ganze
Raketenfabrik `XFactoryRocket`, die der Port nicht hat.

## Der Panzerschreck und die Raketenfabrik

Dritte und letzte der Waffen, die `crate_weapon` fehlten (Aufgabe #102). Sie ist die teuerste
der drei, weil sie nicht nur eine Waffe ist, sondern ein ganzes Kaliber: `XFactoryRocket` —
fünf Raketen mit eigenem Antrieb.

### Eine Rakete ist kein Geschoss

Alle anderen Kaliber des Ports verschießen etwas, das beim Abschuss seine volle Geschwindigkeit
hat und danach nur noch fällt. Eine Rakete macht das Gegenteil: sie verlässt das Rohr mit
Geschwindigkeit null (`setVel(0F)`), schiebt sich im Flug auf Fahrt (vierzig Tausendstel je
Tick, bis das Siebenfache erreicht ist) und fällt gar nicht (`setGrav(0D)`). Sie zerplatzt auch
nicht an Wesen (`setOnEntityHit(null)`), sondern nur beim Aufschlag.

Das Feld dafür — `accel` in `BulletBaseMK4`, in `getMotionMult()` zur Konfigurations-
geschwindigkeit addiert — stand im Port schon; es hatte bisher nur niemanden, der es benutzt.

### Fünf Gefechtsköpfe, fast wie beim 40-mm-Kaliber

Sprengkopf, Hohlladung, Abbruchkopf, Brand- und Phosphorkopf: dieselben fünf Wirkungen wie bei
den 40-mm-Granaten, nur größer. Verlockend wäre gewesen, `XFactory40mm.spawnFire` einfach
mitzubenutzen — der Code sieht Zeile für Zeile gleich aus.

Er ist es nicht. Nachgemessen im Original: die Rakete stellt ein Feuer von sechs Blöcken Breite
hin (die Granate fünf) und zündet in einem Würfel von fünf Blöcken Kantenlänge alles Brennbare
an (die Granate in dreien). Das sind hundertfünfundzwanzig statt siebenundzwanzig geprüfte
Positionen. `XFactoryRocket` bekommt deshalb ein eigenes `spawnFire` mit einem Vermerk, warum
es kein Aufruf des anderen ist.

### Der Selbstschutz, den der Port nicht hat

Das Original schützt den Schützen doppelt. Erstens steht in jedem Aufschlaglambda
`if(typeOfHit == ENTITY && ticksExisted < 3) return;` — in den ersten drei Ticks zählt
überhaupt kein Wesen. Zweitens setzt die Raketenvorlage `setSelfDamageDelay(10)`: zehn Ticks
lang geht die Rakete durch ihren eigenen Schützen hindurch, ohne ihn zu berühren.

Die zweite Regel sitzt im Original tief im Geschoss (`EntityThrowableNT` prüft sie bei der
Kollisionssuche). Der Port hat dort keine Vorlaufzeit; `BulletBaseMK4.canHitEntity` fragt nur,
ob die Konfiguration Wesen trifft. Deshalb steht die Regel hier als Wachposten `zuFrueh(...)`
vor jedem Aufschlag, zusammen mit der ersten — beide zusammen ergeben genau das Verhalten des
Originals, nur an einer anderen Stelle im Code.

### Nicht übernommen: Stinger, Quadro, Raketenwerfer

`XFactoryRocket` stellt im Original vier Waffen her. Drei bleiben draußen:

* **Stinger** — braucht `ItemGunStinger` samt Zielerfassung (`getLockonTarget`) und
  `setupLockonFire`. Letzteres ist im Port bereits auskommentiert vorhanden, die Zielerfassung
  fehlt ganz.
* **Quadro** und **Raketenwerfer** — brauchen lenkbare Raketen (`rocket_qd`, `rocket_ml`).
  Lenkbar heißt: die Rakete fragt jeden Tick, wohin ihr Schütze gerade zielt, und dreht ihren
  Bewegungsvektor dorthin. Eine lenkbare Rakete ohne Lenkung wäre eine gewöhnliche — deshalb
  lieber gar keine.

Die Vorlage `rocket_template` und die Kopie `rocket_rpzb` bleiben trotzdem getrennt, obwohl
`makeRPZB` im Original nichts weiter als `clone()` ist: eine `BulletConfig` trägt eine eigene
Kennung, und zwei Werfer dürfen sich keine teilen.

### Kleinigkeiten

**Der Rückstoß.** Das Original hängt ein leeres `LAMBDA_RECOIL_ROCKET` an, damit der
Standardrückstoß ausfällt. Der Port lässt den Haken schlicht leer — das Feld ist ohnehin leer
vorbelegt, und `HbmAnimation` fragt vor dem Aufruf nach. Eine leere Lambda wäre toter Code mit
zusätzlichem Namen.

**Der Vertipper.** Das Original nennt die Klangfolge `ORCHESTRA_PANERSCHRECK` — ohne Z. Anders
als beim `RELAOD_TILT` der 44er ist das kein Datenname, der irgendwo als Zeichenkette
nachgeschlagen wird, sondern ein reiner Feldname. Er steht hier deshalb richtig.

**Das Schutzschild.** Im Original lässt es sich per Aufsatz abnehmen (`ID_NO_SHIELD`). Den
Aufsatz gibt es im Port nicht — die Abfrage hätte nur einen Zweig, der nie genommen wird. Das
Schild steht deshalb fest, mit Vermerk im Zeichner.

**Der Klang.** `GUN_ROCKET_FIRE` zeigt im Original auf `weapon/rpgShoot`; der Port schreibt
Dateinamen klein mit Unterstrich, hier also `rpg_shoot.ogg`. Modell, Textur und Klang sind
Byte für Byte die Dateien des Originals.

Damit ist `crate_weapon` frei: alle drei fehlenden Waffen stehen. Die Kiste selbst kommt mit
Aufgabe #80, sobald auch die übrigen fünf ihre Gegenstände haben.

Alle 29 Tore grün.

## Die Dosenkiste

Aufgabe #103, die zweite der fünf noch blockierten Beutekisten. Sie war deutlich billiger als
befürchtet: nachgemessen fehlte von der ganzen Dosen- und Nahrungsfamilie genau ein Stück.

### Was schon dastand

Der Topf des Originals hat 36 Einträge: alle 27 Konserven (`EnumFoodType`), acht Dosengetränke
und der Pudding. Im Port stehen die 27 Konserven längst als `ConserveType` (nachgezählt: 27 auf
beiden Seiten), und die acht Getränke sind Spielarten des `DRINK`-Gegenstands — das Original
führt jede Dose als eigenen Gegenstand, der Port hat sie zusammengefasst. Gezogen wird aus
demselben Topf mit denselben Gewichten; nur die Liste im Code sieht anders aus.

Übrig blieb `pudding`: im Original ein `ItemLemon(6, 1F, false)`, also schlichte Nahrung mit
sechs Punkten und Sättigungsfaktor eins, ohne Sonderverhalten. Im Port eine Zeile in `NtmFoods`
und eine in `NtmItems`. Die Textur lag schon bereit.

### Die Kiste

Fünf bis acht Stück, jedes einzeln und gleich wahrscheinlich gezogen — dieselbe Sorte kann
also mehrfach kommen, genau wie im Original. Geöffnet wird sie wie jede Kiste des Mods: nur
mit der Brechstange.

Ihr Aussehen ist im Original ein Wellenfrontmodell ohne Blockentität
(`ISimpleBlockRenderingHandler` mit `conservecrate.obj`). Nach der schon beim HEV-Akku
festgelegten Regel — eine Blockentität nur zum Zeichnen lehnt der Port ab — steht dafür ein
gebackenes JSON-Modell. Nachgemessen am OBJ: ein unterer und ein oberer Rand von je drei Pixeln
auf voller Blockbreite, dazwischen ein um einen Pixel eingezogenes Mittelstück, und davor acht
Latten von drei Pixeln Breite, je zwei vor jeder Seite. Das sind elf Kästen.

Eine Abweichung bleibt: im Original läuft das Mittelstück über eine Schräge in die Ränder. Eine
Schräge lässt sich in einem JSON-Modell nicht achsenparallel ausdrücken, hier trifft es
rechtwinklig auf. Die Textur kommt aus `crate_can_side/top/bottom` — drei 16×16-Bilder, die das
Original mitliefert, aber selbst nirgends benutzt (sein OBJ zieht seine Flächen stattdessen aus
einem 32×32-Atlas, dessen UV-Zuschnitt ein JSON-Modell nicht nachbilden kann).

### Ein Tor, das an dieser Stelle blind war

Beim Nachprüfen, ob das neue Modell auch wirklich von einem Tor erfasst wird, kam heraus: der
Verweis vom Blockzustand auf das Modell wird geprüft (`model-resolve-check` schlägt an, wenn
`crate_can.json` fehlt), der Verweis vom Modell auf seine **Texturen** aber nicht. Ein
handgeschriebenes Modell durfte bis jetzt eine Textur nennen, die es nicht gibt, ohne dass
irgendetwas davon Notiz nahm — sichtbar erst im laufenden Spiel als schwarz-violettes Karo.

`asset-check` prüft jetzt auch die handgeschriebenen Modelldateien unter `models/block` und
`models/item`: jeden Eintrag in `textures` und den `parent`-Verweis, soweit beide im eigenen
Namensraum liegen. Nachgewiesen an beiden Fällen — eine entfernte Textur und ein erfundener
Elternverweis lassen das Tor anschlagen, danach ist es wieder grün. Geprüfte Verweise insgesamt:
1516.

Bleiben für Aufgabe #80 noch `crate`, `crate_supply`, `crate_red` und `crate_weapon` — letztere
nur noch als Block, ihre drei Waffen stehen seit der vorigen Runde.

Alle 29 Tore grün.

## Berichtigung: die Dosenkiste ließ das Spiel nicht starten

Der erste Anlauf der Dosenkiste (Commit `07157e25`) war grün durch alle 29 Tore und ist
trotzdem in der CI durchgefallen — mit einem Absturz beim Laden:

```
Caused by: java.lang.NullPointerException: Trying to access unbound value:
           ResourceKey[minecraft:item / hbmsntm:drink]
    at com.hbm.items.food.DrinkItem$DrinkType.<clinit>(DrinkItem.java:39)
    at com.hbm.blocks.generic.CanCrateBlock.<clinit>(CanCrateBlock.java:45)
    at com.hbm.blocks.NtmBlocks.lambda$static$552(NtmBlocks.java:978)
```

Die Ursache steht Zeile für Zeile in der Spur. `CanCrateBlock` hatte die acht Dosensorten als
statisches Feld:

```java
private static final DrinkType[] DOSEN = { DrinkType.SMART, ... };
```

Das Registrieren des Blocks lädt die Klasse, das Laden führt ihren Klasseninitialisierer aus,
und der greift auf `DrinkType` zu — wodurch **dessen** Klasseninitialisierer läuft. `DrinkType`
legt aber für jede Sorte gleich die leere Dose und den Ringzug als Gegenstandsstapel an, und
zu diesem Zeitpunkt ist `NtmItems.DRINK` noch gar nicht gebunden. Blöcke werden vor
Gegenständen registriert.

Die Behebung ist eine Verschiebung: die Liste entsteht jetzt in `topf()`, also erst beim ersten
Öffnen einer Kiste — lange nach der Registrierung. Ein Verweis im Rumpf einer Methode lädt die
fremde Klasse nicht mit.

### Warum kein Tor das gefunden hat

Die 29 Tore prüfen den ruhenden Quelltext: Syntax, Importe, Modellverweise, Registriernamen.
Dies hier ist kein Fehler im Quelltext, sondern in der **Reihenfolge zur Ladezeit** — sichtbar
erst, wenn wirklich jemand den Mod lädt. Genau das tut die CI, und genau das hat sie gemeldet.
Ein statischer Ersatz dafür müsste der Kette „Blockklasse → Klasseninitialisierer → fremder
Klasseninitialisierer → ungebundener Gegenstand" folgen; das ließe sich nur raten, und ein Tor,
das rät, ist schlimmer als keines. Die Lehre steht deshalb hier und nicht in `tools/`:

> In einer Block- oder Gegenstandsklasse gehört in ein statisches Feld nichts, was auf einen
> anderen registrierten Gegenstand führt — auch nicht mittelbar über eine fremde Aufzählung.

Nachgesehen, ob es die Stelle noch woanders gibt: nein. `AmmoCrateBlock` hält zwar ebenfalls
ein statisches Feld mit Aufzählungswerten (`Ammo`), aber `Ammo` ist eine reine Datenaufzählung
ohne eigenen Initialisierer — sie ist seit ihrer Runde grün.

## Ein Strahl, der sich selbst aufgerufen hat

Beim Nachmessen der Abhängigkeiten für die Granaten (Aufgabe #104) fiel in
`BulletBeamBase.tick()` auf:

```java
if(config.onUpdate != null) config.onUpdate.accept(this);
this.tick();                                    // <-- ruft sich selbst
```

Das Original ruft an dieser Stelle `super.onUpdate()`. Die Übertragung hat daraus `this.tick()`
gemacht — eine Endlosschleife, die beim ersten Tick mit einem Stapelüberlauf abbricht.

Sie ist nie aufgefallen, weil die Klasse im Port bislang nirgends erzeugt wird: `new
BulletBeamBase` kommt kein einziges Mal vor. Der Fehler lag also still da und hätte beim ersten
Strahl zugeschlagen. Jetzt steht dort `super.tick()`, mit Vermerk.

Alle 29 Tore grün.

## Berichtigung: die W9 riss zu klein

Beim Nachmessen der Explosions-Schnittstelle für die Granaten fiel in `XFactoryTurret` dieser
Vermerk auf:

```java
/* ABWEICHUNG: der Reichweitenaufschlag des Originals (withRangeMod) fehlt dem Port. */
vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, bullet.damage));
```

Der Satz stimmt nicht. `withRangeMod` steht in `EntityProcessorCross` (Zeile 183) und wird im
Port an sechs Stellen benutzt — unter anderem von der Atommine und vom Abfangflugkörper. Der
Vermerk hat also nicht eine fehlende Schnittstelle beschrieben, sondern eine weggelassene
Zeile, und die Atomgranate der W9 hat seitdem in einem Drittel zu kleinem Umkreis Schaden
gemacht: das Original setzt dort `withRangeMod(1.5F)`.

Nachgetragen. Der Vermerk entfällt, weil er nichts mehr beschreibt.

Damit ist es die dritte Falschaussage dieser Art, die beim Nachmessen aufgefallen ist (nach
`centrifuge_element`/`piston_selenium` und der veralteten Kopfnotiz in `CrateLoot`). Alle drei
hatten dieselbe Form: ein Vermerk, der einmal richtig war oder nie geprüft wurde, und den
seitdem niemand nachgerechnet hat.

## Die Granaten

Aufgabe #104 ist die größte seit langem: `crate` und `crate_supply` warten auf das
Granatensystem, und das ist im Original kein Gegenstand, sondern ein Baukasten aus vier
Bauteilen, einer Wurfentität und rund 1200 Zeilen.

### Vier Bauteile, eine Granate

```
 __________
| ________ | ______ KÖRPER  – was hineinpasst, wie weit sie fliegt, wie hoch sie abspringt
||        ||
||       __________ FÜLLUNG – der Knall
||________||
 \   /\   /
  \_ || _/
   | |_____________ ZÜNDER  – wann sie hochgeht
   | || | _________ AUFSATZ – wahlweise, das einzige Bauteil, das fehlen darf
   /_||_\
```

Jedes Bauteil gibt es auch einzeln als Gegenstand; welche Zusammenstellung ein Stapel trägt,
steht in seinen Zusatzdaten. Das Original schreibt vier Zahlen ins NBT, der Port legt sie unter
denselben vier Schlüsseln ab.

Die Arbeitsteilung ist streng und kommt an drei Stellen zum Tragen: **jeden Tick** melden sich
Zeit- und Näherungszünder, **beim Anstoßen** Aufschlagzünder und Kleber, **beim Hochgehen** die
Füllung samt Splittermantel und Dreifachteiler. Der Zünder allein entscheidet, *wann* es knallt;
die Füllung allein, *was* dann passiert.

### Eine Wurfentität, die den Aufschlag überlebt

Alle Geschosse des Ports hängen an `ProjectileNT`, und dort beendet der erste Treffer den Flug.
Eine Granate muss aber abspringen und weiterrollen. Dafür steht jetzt `ThrowableNT` daneben —
die Übertragung von `EntityThrowableNT` aus dem Original, aber deutlich kürzer:

* Die Verwaltung des Werfers entfällt: `Projectile` trägt sie auf 1.21 selbst, samt Speicherung.
* Die Zwischenschicht `EntityThrowableInterp`, die im Original nur die Bewegung zwischen zwei
  Netzpaketen glättet, entfällt ebenfalls — das macht 1.21 in `lerpTo`.

Geblieben sind die zwei Zustände: in der Luft zählt `ticksInAir`, festgeklebt `ticksInGround`.
Wird der Block, in dem die Granate steckt, abgebaut, fällt sie wieder und fliegt mit einem
Bruchteil ihrer alten Geschwindigkeit weiter.

Das Abspringen selbst ist die Regel des Originals: senkrecht zur getroffenen Fläche kehrt sich
die Bewegung um und wird um den Absprungwert des Körpers gedämpft, längs der Fläche bleibt ein
Fünftel liegen. Der Stiel springt nur halb so hoch wie die Handgranate — ein Griff am Ende macht
den Wurf weiter und den Aufprall stumpfer.

### Nachgetragen: der elektromagnetische Schlag

Die EMP-Füllung braucht `ExplosionNukeGeneric.empBlast`, und die gab es im Port nicht. Sie ist
jetzt da, mitsamt `emp`: jede Blockentität in der Kugel verliert ihre Ladung, jede fünfte wird
zu Elektroschrott. Übernommen wurde dabei auch eine Eigenheit des Originals — geprüft wird
gegen `r*r/2` statt `r*r`, der Schlag reicht also nur etwa sieben Zehntel so weit, wie die
übergebene Stärke vermuten lässt. Die Stärken sind darauf abgestimmt, also bleibt es so.

Eine Abweichung: das Original entlädt zusätzlich Maschinen fremder Mods über die
RF-Schnittstelle. Der Port kennt nur seine eigene.

### Was nicht mitgekommen ist

**Zwei der dreizehn Füllungen.** `LASER` verschießt beim Hochgehen Strahlen auf alles im
Umkreis — der Port hat mit `BulletBeamBase` zwar die Hülle einer Strahlenentität, aber keine
Abtastung; `performHitscan` fehlt ganz. `SCHRAB` zündet eine Fleija, wofür die Wolkenentität
fehlt; die gehört zur Fleija-Bombe und kommt mit deren Runde. Beide stehen im Original am Ende
der Aufzählung, ihr Fehlen verschiebt also keine Ordnungszahl der übrigen.

**Das Aussehen.** Das Original zeichnet ein Wellenfrontmodell mit vier Körperformen und färbt
Körper, Aufkleber und Zünderring nach den Werten der Bauteile — dazu eine Ziehbewegung je
Körper, mit eigenem Ton und eigenem Abziehring. Der Port zeigt vorerst das Gegenstandsbild: die
Granate fliegt, springt und wirkt richtig, sie sieht nur noch flach aus. Modell und Ziehbewegung
gehören zusammen in eine eigene Runde; die Anlaufzeiten stehen schon im Körper
(`drawDuration`), damit sie dann nicht neu gemessen werden müssen.

**Zwei Partikelarten**, `plasmablast` und `haze`, die es im Port nicht gibt. Der Schlag und das
Feuer sind da, der Schleier fehlt.

### Ein Tor, das den Kreativreiter zu eng gesehen hat

`tab-check` sucht Zeilen mit `output.accept` oder `addMetaItems` und liest daraus die
Gegenstandsnamen. Die Granate wird aber weder einzeln noch nach Ordnungszahl abgelegt, sondern
in allen Zusammenstellungen — der Reiter ruft dafür `addGrenadeCombinations`. Das Tor hat den
Gegenstand deshalb als „in keinem Reiter" gemeldet, obwohl er dort steht.

Naheliegend wäre gewesen, ihn in die Ausnahmeliste zu schreiben. Das wäre aber gelogen: die
Liste sagt „absichtlich in keinem Reiter", und hier ist das Gegenteil der Fall. Stattdessen
kennt das Tor jetzt den dritten Helfer. Nachgewiesen, dass es weiterhin anschlägt, wenn die
Zeile wirklich fehlt.

Alle 29 Tore grün.

## Berichtigung: die Plasmafüllung gab es nicht

Der erste Anlauf der Granaten (`1e47282c`) war grün durch alle 29 Tore und ist in der CI an
einem einzigen Fehler gescheitert:

```
GrenadeFillingItem.java:195: error: cannot find symbol
  ... energieExplosion(granate, 50F, 5F, DamageClass.PLASMA);
                                                     ^
  symbol:   variable PLASMA
  location: class DamageClass
```

Nachgesehen: das Original führt in `DamageClass` neun Werte, der Port acht. `PLASMA` steht dort
zwischen `ELECTRIC` und `LASER` und ist beim Übertragen der Aufzählung weggefallen — lange vor
dieser Runde, und bis jetzt hat es niemand gebraucht.

`DamageClass` ist eine reine Kennzeichnung ohne Verzweigungstabellen und ohne gespeicherte
Ordnungszahlen; nachgezählt wird sie im Port an sechs Stellen benutzt, alle in der Form
`setDamageClass(...)`. Der Wert ist deshalb an seiner ursprünglichen Stelle nachgetragen.

### Ein neues Tor: `enum-check`

Das ist die zweite Runde in Folge, die grün durch alle Tore ging und in der CI fiel. Beim
letzten Mal war es eine Frage der Ladereihenfolge — dafür kann es kein statisches Tor geben.
Diesmal ist es etwas, das ein Tor sehr wohl sehen kann: ein Verweis auf eine Konstante, die
nicht existiert.

`syntax-check` kann es nicht finden, und zwar aus gutem Grund: es übersetzt ohne
Minecraft-Klassenpfad und muss „cannot find symbol" herausfiltern, weil diese Meldung dabei
zehntausendfach als Folgefehler entsteht. Der echte Fall geht in diesem Rauschen unter.

`enum-check` schaut deshalb nur auf das, was der Port selbst erklärt: es liest alle
Aufzählungen unter `src/main/java`, sammelt ihre Konstanten und hält jeden Verweis der Form
`Name.KONSTANTE` dagegen. Gemessen: 137 Aufzählungen, 3825 Verweise.

Zwei Dinge mussten beim Bauen nachgebessert werden, beide durch Nachmessen gefunden:

* **Ein Semikolon im Kommentar** hat die Konstantenliste abgeschnitten. Der Vermerk, den ich
  gerade erst neben `PLASMA` geschrieben hatte, enthielt eines — und prompt hat das Tor seinen
  eigenen Anlass nicht mehr gesehen. Die Deklarationen werden jetzt ohne Kommentare gelesen.
* **Namensgleichheit mit Minecraft.** Der Port hat eigene Aufzählungen namens `SoundType` und
  `ConnectionType`, und beide Namen gibt es auch in Minecraft bzw. NeoForge. Ein ausdrücklicher
  Import sticht jetzt jeden Sammelimport — steht in der Datei `import
  net.minecraft.world.level.block.SoundType;`, dann ist dieser gemeint. Vier Aufzählungen
  bleiben mehrdeutig und werden übersprungen: welche gemeint ist, ließe sich nur raten.

Nachgewiesen an zwei Fällen: das Tor meldet `DamageClass.PLASMA`, sobald der Wert wieder
fehlt, und ebenso ein erfundenes `GrenadeFuze.S4`. Danach ist es wieder grün.

Damit sind es **30 Tore**.

## Berichtigung zur Berichtigung: der erschöpfende Schalter

Das Nachtragen von `DamageClass.PLASMA` hat die CI ein zweites Mal zu Fall gebracht:

```
BulletConfig.java:173: error: the switch expression does not cover all possible input values
    ResourceKey<DamageType> damageType = switch(dmgClass) {
```

**Das war mein Fehler, und zwar ein vermeidbarer.** Im Abschnitt davor steht der Satz
„`DamageClass` ist eine reine Kennzeichnung ohne Verzweigungstabellen". Ich hatte dafür zwei
Dinge abgesucht: `DamageResistanceHandler` nach Verbrauchern, und das ganze Projekt nach
Verweisen der Form `DamageClass.X`. Ein Schalter schreibt seine Marken aber **ohne**
Typnamen — `case PHYSICAL ->`. Beide Suchen mussten ihn übersehen. Die Behauptung war nicht
gemessen, sondern erschlossen, und sie war falsch: es gibt zwei Schalter über `DamageClass`.

Nur einer davon stört. `ConfettiUtil.createConfetti` ist ein Schalter-**Befehl** und darf eine
Teilmenge behandeln; er tut das mit Absicht, denn nicht jede Schadensart hinterlässt Asche.
`BulletConfig.getDamage` ist ein Schalter-**Ausdruck** und muss jeden Wert abdecken.

Nachgetragen ist deshalb eine ganze Schadensart: Schlüssel, Anmeldung als `sednaPlasma`, der
Zweig im Schalter und zwei Todesmeldungen. Der Wortlaut kommt aus dem Original
(`death.attack.plasma=%1$s was immolated by %2$s.`). Das entspricht dem Original auch im Bau:
dort entsteht die Schadensquelle aus `dmgClass.name()`, jede Klasse hat also ohnehin ihre
eigene.

### `enum-check` kann das jetzt auch

Das Tor von vorhin hätte diesen zweiten Fehler nicht gefunden — es prüft Verweise auf
Konstanten, nicht Lücken in Schaltern. Beides sind aber Folgen derselben Ursache: ein Wert
kommt zur Aufzählung dazu oder fehlt in ihr.

Der zweite Teil sucht deshalb Schalter-**Ausdrücke** (hinter `=` oder `return`) ohne
`default`-Zweig, bestimmt anhand der Marken, welche Aufzählung gemeint ist — nur bei genau
einem Treffer, sonst wird nicht geraten — und meldet, welche Konstante keinen Zweig hat.
Schalter-Befehle bleiben außen vor, weil eine Teilmenge dort erlaubt und oft gewollt ist.

Gemessen: 19 erschöpfende Schalter im Port, alle vollständig. Nachgewiesen, dass das Tor
anschlägt, sobald der `PLASMA`-Zweig wieder fehlt.

Damit deckt `enum-check` beide Seiten ab: einen Verweis auf eine Konstante, die es nicht gibt,
und eine Konstante, auf die kein Zweig zeigt.

## Der Munitionsbehälter, das Gegenmittel — und eine Zeile, die auskommentiert war

Die beiden letzten Gegenstände, auf die `crate` und `crate_supply` warten (Aufgabe #104).

### `setDefaultAmmo` hat nichts getan

Der Munitionsbehälter fragt jede Waffe im Gepäck nach ihrer Standardmunition. Beim Nachsehen,
woher die kommt, stand im Port das hier:

```java
public GunBaseNTItem setDefaultAmmo(Ammo ammo, int amount) {
    //this.defaultAmmo = new ItemStack(ModItems.ammo_standard, amount, ammo.ordinal());
    return this;
}
```

Die Zuweisung ist auskommentiert. **38 Waffen** rufen diese Methode auf — jede wirft ihren Wert
seitdem weg, und `defaultAmmo` war ausnahmslos leer. Aufgefallen ist es nie, weil bis jetzt
niemand danach gefragt hat: der Behälter ist der erste und einzige Leser.

Warum die Zeile auskommentiert wurde, lässt sich am Code ablesen: sie hätte an dieser Stelle
gar nicht laufen können. Eine Waffe entsteht in einem Lieferanten, der **während** der
Anmeldung läuft; `new ItemStack(NtmItems.AMMO_STANDARD.get(), ...)` braucht dort einen
gebundenen Gegenstand, und ob `AMMO_STANDARD` zu diesem Zeitpunkt schon gebunden ist, hängt an
der Reihenfolge. Das ist genau der Absturz, den die Dosenkiste ein paar Runden weiter oben
vorgeführt hat.

Die Lösung ist dieselbe wie dort: **nicht den Stapel merken, sondern die Sorte.** `defaultAmmo`
ist jetzt ein `Ammo`-Wert samt Menge, und `getDefaultAmmo()` baut den Stapel erst, wenn er
gebraucht wird — lange nach der Anmeldung. Das Original kann sich den fertigen Stapel leisten,
weil es seine Gegenstände unmittelbar anlegt; auf 1.21 geht das nicht.

### Der Behälter

Zwei Sorten, wie im Original: der gewöhnliche bedient jede Waffe, der behelfsmäßige gibt nur
die Hälfte her und lässt die Waffen aus, deren Munition als teuer gilt (`isDefaultExpensive`).
Gemischt wird die Liste und vorne abgeschnitten — wer mehr als drei Waffen trägt, bekommt für
drei zufällige etwas.

Im Original sind die beiden Sorten zwei Metadaten mit zwei Symbolen (`ammo_container` und
`ammo_container_alt`); im Port sind es zwei Spielarten eines `EnumMultiItem`, das seine Modelle
selbst erzeugt. Die Texturen heißen entsprechend `ammo_container.standard` und
`ammo_container.makeshift`.

### Das Gegenmittel

Eine Glasspritze, die alle Trankwirkungen abräumt — auch die guten, denn sie unterscheidet
nicht. Die Übelkeitssperre gilt wie bei den anderen Spritzen.

Dafür musste `SyringeItem` eine Kleinigkeit lernen: was übrig bleibt, war bisher fest die
Metallhülle. Das Gegenmittel steckt im Original in Glas und lässt `syringe_empty` zurück; die
leere Hülle ist deshalb jetzt eine Eigenschaft der Spritze.

Eine Abweichung bleibt: im Original ist das Gegenmittel kein `ItemSyringe`, sondern ein
`ItemSimpleConsumable`, und das lässt sich auch jemand anderem in den Arm rammen
(`setHitActionServer`). Diesen zweiten Weg hat der Port nicht — hier wirkt die Spritze nur auf
den, der sie hält.

Damit stehen alle Gegenstände, auf die die Nachschubkiste wartet. Was für Aufgabe #104 noch
fehlt, sind die beiden Kisten selbst.

Alle 30 Tore grün.

## Die Nachschubkiste und die Waffenkiste

Mit den Granaten, dem Munitionsbehälter und dem Gegenmittel stehen alle Gegenstände, auf die
Aufgabe #104 gewartet hat. Beide Kisten sind damit nur noch eine Liste und ein Block.

**Die Nachschubkiste** (`crate`) zieht aus sechs Einträgen: zwei Spritzen und drei fertig
zusammengesetzte Granaten, dazu mit geringem Gewicht ein Munitionsbehälter. Die
Zusammenstellungen der Granaten sind die des Originals — Splitterkörper mit Sprengfüllung,
Dreisekundenzünder und Splittermantel; Stiel mit Sprengfüllung und Aufschlagzünder;
Splitterkörper mit Brandfüllung und Siebensekundenzünder.

**Die Waffenkiste** (`crate_weapon`) zieht aus sieben Waffen, vom leichten Revolver bis zum
Panzerschreck. Sie zählt als einzige anders: aus ihr fallen nur ein bis zwei Stücke, denn eine
Waffe ist mehr wert als eine Handvoll Erz. In einem von hundert Fällen kippt sie stattdessen
fünfundzwanzig aus — ein Scherz des Originals (`if(rand.nextInt(100) == 34) i = 25;`), hier
unverändert.

### Berichtigung: `crate_supply` war nie an Gegenständen blockiert

Aufgabe #104 heißt „`crate` und `crate_supply`: Granaten und `ammo_container`". Für `crate`
stimmt das. Für `crate_supply` nicht, und das fiel erst beim Nachlesen auf.

`crate_supply` ist keine Beutekiste. Sie ist ein **Behälter**: `BlockSupplyCrate` hat eine
Blockentität mit einer Gegenstandsliste, trägt sie beim Abbauen ins NBT des abgeworfenen
Gegenstands und lädt sie beim Setzen wieder. Gewürfelt wird nichts.

Wer füllt sie? Nachgesehen — genau eine Stelle im ganzen Original:

```
EntityParachuteCrate.java:45: worldObj.setBlock(..., ModBlocks.crate_supply);
EntityParachuteCrate.java:46: TileEntitySupplyCrate crate = (TileEntitySupplyCrate) ...
```

Der Fallschirmabwurf, den die C-130 hinterherwirft. Und die C-130 ist im Port ausdrücklich
nicht portiert — das steht seit der 40-mm-Runde im Kopf von `XFactory40mm`: die beiden
Signalkugeln fehlen, weil das Flugzeug fehlt.

Eine Nachschubkiste ohne Flugzeug wäre eine Kiste, die niemals etwas enthält. Sie bleibt
deshalb aus, und ihr Blocker steht ab jetzt richtig in der Liste: nicht Granaten und
`ammo_container`, sondern der Fallschirmabwurf samt C-130.

Stand der Beutekisten: `crate_ammo`, `crate_can`, `crate`, `crate_weapon`, `crate_lead` und
`crate_metal` stehen. Offen bleiben `crate_red` (Aufgabe #105) und `crate_supply` (wartet auf
die C-130).

Alle 30 Tore grün.

## Rote Kiste, erster Teil: Spaten, Pony und drei Ostereier

Aufgabe #105 zählt zehn Sonderstücke auf. Nachgemessen sind es weniger, als die Liste vermuten
lässt — und zugleich mehr Arbeit an anderer Stelle.

### Was gar nicht fehlte

`bottle_sparkle` und `bottle_rad` stehen längst im Port: die Flaschen sind Spielarten von
`DrinkType` (`SPARKLE`, `RAD`). Ebenso vorhanden: die drei `EQUESTRIAN`-Patronen,
`gun_maresleg_broken`, `battery_spark`, `ring_starmetal`, `ntm_dirt` und `broadcaster_pc`. Von
den zehn Einträgen fehlten also nur vier.

### Der brüchige Spaten und seine drei Funde

`mysteryshovel` taugt zu genau einer Sache: auf die Erde der Bauwerke geschlagen lässt er den
Block verschwinden und wirft drei Gegenstände aus. Welche drei, war der interessante Teil.

Im Original sind es die Metadaten eins bis drei von `ingot_u238m2`. Der Gegenstand ist dort ein
`ItemUnstable` mit Untertypen: Metadatum null ist der zerfallende Barren, eins bis drei sind
drei Ostereier mit eigenen Bildern (`hs-elements`, `hs-arsenic`, `hs-vault`). Der Port hatte
`ingot_u238m2` als schlichten Einzelgegenstand angemeldet — richtig für seine Rolle als
Zwischenstufe, aber die drei Funde gab es damit nicht.

`ingot_u238m2` ist jetzt ein `EnumMultiItem` mit vier Spielarten, deren Ordnungszahlen die
Metadaten des Originals sind. Die beiden Nuggetrezepte bleiben unberührt: sie erzeugen den
Gegenstand ohne Angabe einer Spielart, und das ist die nullte — neun Nuggets geben weiterhin
einen Barren, kein Osterei.

**Nicht nachgeholt:** der Zerfall. Im Original tickt Metadatum null herunter und geht am Ende
als `EntityNukeExplosionMK5` hoch; der Port hat den Barren seit jeher ohne dieses Verhalten,
und diese Runde ändert daran nichts. Das gehört in die Runde des instabilen Barrens, nicht in
die der roten Kiste.

Der Spaten selbst steht in keinem Kreativreiter — so wie im Original, das ihm keinen gibt. Er
liegt nur in der roten Kiste. Das Reiter-Tor weiß das jetzt, mit Begründung.

### Was noch fehlt

Zwei der zehn bleiben, und beide hängen an etwas Größerem:

* **`gun_heavy_revolver_lilmac`** — seine Signaturpatrone `m44_equestrian_pip` lässt beim
  Aufschlag einen `EntityBoxcar` aus fünfzig Blocken Höhe auf das Ziel fallen. Diese Entität
  gibt es im Port nicht. Ohne sie wäre der Lilmac ein umlackierter schwerer Revolver, und damit
  wäre der ganze Witz der Waffe weg.
* **`gun_autoshotgun_sexy`** — die Grundwaffe, die Autoschrotflinte, fehlt dem Port vollständig;
  eine Sonderfassung braucht erst ihre Grundform.

Die rote Kiste kippt im Gegensatz zu den übrigen **alle** ihre Einträge aus, nicht einen
gezogenen. Sie kann deshalb erst kommen, wenn beide Waffen stehen.

### Eine Lücke, die stehen bleibt — vorerst

Beim Nachprüfen kam heraus: die Einzeltexturen einer Metafamilie prüft kein Tor. `asset-check`
verfolgt Verweise aus dem Java-Quelltext und aus handgeschriebenen Modelldateien; die Modelle
einer `EnumMultiItem` entstehen aber im Datengenerator, und ihre Texturnamen stehen nirgends
als Zeichenkette. Eine fehlende `ingot_u238m2.vault.png` fällt deshalb erst im Spiel auf.

Das ist keine falsche Zusage — `asset-check` behauptet nichts über sie —, aber eine echte
Lücke. Sie ist hier vermerkt und gehört geschlossen, sobald die roten Kiste durch ist.

Alle 30 Tore grün.

## Der Güterwagen und der Li'l Mac

Neunte von zehn Einträgen der roten Kiste — und der aufwendigste, weil die Waffe ohne ihre
Munition sinnlos wäre und die Munition eine eigene Entität braucht.

### Was die Signaturpatrone tut

`m44_equestrian_pip` macht null Schaden. Sie ruft stattdessen fünfzig Blöcke über dem
Getroffenen einen **Güterwagen** herbei, kündigt ihn mit einem Signalhorn an und überlässt den
Rest der Schwerkraft. Beim Aufschlag: ein schwerer Knall, drei Druckwellen übereinander,
tausend Punkte Schaden auf alles im Umkreis von zwei Blöcken — absolut und
rüstungsdurchdringend, dagegen hilft nichts — und wo er liegen bleibt, steht danach ein
Güterwagenblock.

Dafür sind neu: die Entität `Boxcar`, der Block `boxcar`, die Schadensart `boxcar`, zwei Klänge
(`train_impact` und das Signalhorn) und ein Zeichner, der das Wellenfrontmodell des Originals
zeigt.

Der Klang des Horns heißt im Original
`GUN_GO_GO_GADGET_FUCK_EVERYTHING_IN_THIS_GENERAL_DIRECTION`. Im Port steht er schlicht unter
`TRAIN_HORN`; der Name des Originals ist im Vermerk festgehalten, damit die Zuordnung
nachvollziehbar bleibt.

**Zwei Abweichungen beim Fall.** Das Original streut beim Erscheinen fünfzig
Balefire-Partikel um den Wagen — die Partikelart `bf` hat der Port nicht, der Wagen fällt hier
ohne dieses Vorspiel. Und es setzt Ort und Geschwindigkeit von Hand, um die übliche Bewegung zu
umgehen; hier genügt die Schwerkraft von `ProjectileNT`, begrenzt auf dieselbe
Endgeschwindigkeit von anderthalb Blöcken je Tick.

### Die Waffe

Äußerlich der schwere Revolver mit anderer Textur — dasselbe `lilmac.obj`, das der Port seit
dessen Runde hat, und derselbe Zeichner, der seine Textur schon damals im Konstruktor
entgegennahm. Innerlich eine andere Waffe: einunddreißigtausend Schuss Haltbarkeit, doppelter
Schaden, ein Zielfernrohr — und die Patrone oben.

Die Bewegung ist die des schweren Revolvers, nur wirbelt er beim Ziehen einmal um sich selbst;
alles andere reicht `LAMBDA_LILMAC_ANIMS` unverändert weiter.

**Nicht übernommen: der Protégé.** Die Schwesterwaffe verschießt `m44_equestrian_mn7`, und die
lässt statt eines Güterwagens einen Torpedo fallen. `EntityTorpedo` hat der Port nicht, und ein
Protégé ohne seinen Torpedo wäre ein zweiter Li'l Mac mit anderem Namen.

### Nebenbei: ein Vergleich, der zu viel getroffen hätte

Im Partikelverteiler des Ports stand:

```java
if ("muke".contains(type)) {
```

Das ist verdreht. `"muke".contains(type)` prüft, ob **type** eine Teilzeichenkette von "muke"
ist — auf `"uk"`, `"e"` und die leere Zeichenkette trifft das ebenso zu wie auf `"muke"`. Mit
den heutigen Aufrufern fällt es nicht auf, weil alle die vollen Namen schicken; es wartet nur
darauf, dass einmal etwas Kürzeres kommt. Dasselbe stand eine Zeile tiefer für `"tinytot"`.
Beide sind jetzt `equals`.

Nach dieser Runde fehlt der roten Kiste nur noch `gun_autoshotgun_sexy` — und dafür die
Autoschrotflinte, die der Port ganz nicht hat.

Alle 30 Tore grün.

## Die Autoschrotflinte und die Duchess Gambit

Die letzte fehlende Waffe der roten Kiste. `gun_autoshotgun_sexy` ist die legendäre der drei
Autoschrotflinten, und ihre Signaturpatrone lässt ein Luftschiff vom Himmel fallen — dasselbe
Spiel wie beim Li'l Mac, nur eine Nummer größer.

### Das Schiff

`DuchessGambit` fällt wie der Güterwagen: eigene Rechnung, gedeckelt auf anderthalb Blöcke je
Tick. Beim Aufschlag ein Nebelhorn (10 000 Lautstärke, das ist die Zahl des Originals),
tausend Punkte Schaden in einem langgezogenen Kasten — zehn Blöcke breit, achtzehn lang, es ist
ein Schiff —, fünf Explosionen entlang der Längsachse von Heck zu Bug, fünf Druckwellen
übereinander, und wo es liegen bleibt, steht danach ein Schiffsblock.

Der Block ist im Original ein `DecoBlock` mit der Textur `hbm:asphalt`, die der Port schon hat.
**Abweichung:** das Original zeichnet ihn als das OBJ-Modell des Schiffs; hier steht, wie schon
beim Güterwagen, ein Würfel mit dieser Textur.

**Abweichung, wie beim Güterwagen:** die fünfzig Balefire-Partikel beim Erscheinen fehlen, die
Partikelart `bf` hat der Port nicht.

**Berichtigt im Vorbeigehen:** der Güterwagen-Schaden trug im Port den Kommentar „absolut und
rüstungsdurchdringend" — in den Schadensart-Marken stand er aber in keiner der drei Listen. Das
Original setzt für beide, Güterwagen wie Schiff, `setDamageIsAbsolute()` und
`setDamageBypassesArmor()`. Beide stehen jetzt in `BYPASSES_ARMOR`, `BYPASSES_EFFECTS` und
`BYPASSES_RESISTANCE`.

### Die Waffen

Zwei der drei: `gun_autoshotgun` (A-Seite, zwanzig Schuss, vollautomatisch, feuert auch nach
dem letzten Schuss weiter, bis der Abzug losgelassen wird) und `gun_autoshotgun_sexy`
(legendär, hundert Schuss im Gurt, vier Ticks Schussfolge, erstes Magazin die Signaturpatrone).

**Nicht übernommen: `gun_autoshotgun_shredder`.** Ihre Munition zerfällt beim Aufschlag in
Strahlen, die weiterspringen — `makeShredderConfig` mit `setOnBeamImpact` und `setOnRicochet`.
Diese Aufspaltung kennt das Geschossteilsystem des Ports noch nicht.

Der Zeichner zeigt drei Teile: Gehäuse, Trommelmagazin und den Gurt darin. Über dem Lauf steht
ein Schild, `[> <]`; die gewöhnliche Flinte zeigt es nur über Kimme und Korn und in Grün, die
schöne immer und in Rot, und es flackert. Das Original zieht dafür eine eigene Lichtberechnung
hoch und stellt sie danach zurück — im Port genügt `FullBright`.

**Abweichung im Klang, die im Original ein Versehen sein dürfte:** `LAMBDA_BOAT` spielt den
Hornstoß nicht dort, wo das Schiff erscheint, sondern noch einmal fünfzig Blöcke darüber. Hier
steht er am Ort des Schiffs.

### Der CI-Lauf, den der Güterwagen gekostet hat — und drei neue Prüfungen

Der Commit davor fiel mit fünf Fehlern durch, alle in `XFactory44`: drei fehlende Importe
(`HitResult`, `Vec3`, `Level`), ein fehlender Import auf `AmmoSecret`, und ein unerlaubter
Vorwärtsverweis. **Kein einziges der dreißig Tore hat etwas davon gesehen.** Drei Lücken, alle
entscheidbar, alle jetzt geschlossen:

**1. Der Typ in der Deklaration und im Generikum.** `import-check` kennt seit Runde 99 eine
Regel für Fremdtypen: wo eine Minecraft-Klasse wohnt, steht in den tausenden expliziten
Importen, die der Port ohnehin hat. Sie sah aber nur `new X(…)`, `extends`, `implements`,
`instanceof` und den statischen Empfänger — nicht `Vec3 stelle = …` und nicht
`BiConsumer<…, HitResult>`. Beide Formen sind jetzt dabei.

Dabei fiel ein zweiter Fehler auf, der schon länger dort stand: der Ausdruck für
Generikum-Argumente verbrauchte das trennende Komma und fand deshalb in `<A, B>` nur `A`.
Zwei Vorausschauen statt zweier Zeichenklassen — jetzt findet er beide.

**2. Der verschachtelte Projekttyp.** Die erste Prüfung kennt nur Dateinamen. `AmmoSecret`
steht als innere Aufzählung in `GunFactory.java` und fiel deshalb unter „kein Projekttyp →
nicht beurteilbar". Dass `GunFactory` im selben Paket liegt, hilft einem verschachtelten Typ
nicht — der braucht einen eigenen Import. Die neue Regel meldet jeden Namen, den das Projekt
ausschließlich verschachtelt kennt und der weder importiert noch qualifiziert noch geerbt ist.

Vier Filter waren nötig, und jeder einzelne hat sich in der Messung verdient: Namen, die es
auch als eigene Datei gibt (die gehören an die erste Prüfung), Namen mit mehreren möglichen
Hüllen (`Type` steht in acht Klassen), geerbte Hüllen über die ganze Obertypkette (wer
`IToolable` implementiert, sieht `ToolType` ohne Import — das geht über mehrere Stufen), und
Namen, die irgendwo als Fremdimport stehen (`Item`, `Blocks`). Ohne den dritten Filter meldete
die Regel 112 Fehlalarme, mit ihm drei, mit dem vierten null.

**3. Der Vorwärtsverweis** — `tools/forward-check.sh`, das einunddreißigste Tor. Ein statisches
Feld darf im selben Klassenkörper nur auf bereits deklarierte verweisen; `LAMBDA_LILMAC_ANIMS`
stand über `LAMBDA_NOPIP_ANIMS` und griff darauf zu. Ohne Klassenpfad ist das entscheidbar,
denn beide Seiten stehen in derselben Datei. Gebraucht wird nur eine saubere Trennung von
Feldinitialisierern und Methodenrümpfen: eine `}` auf Klassenebene, der ein `;` folgt, beendet
einen Feldinitialisierer; eine ohne folgendes `;` beendet eine Methode, und was davor
angesammelt wurde, ist kein Feld. Vor dieser Unterscheidung meldete der Prototyp 83
Fehlalarme, danach null — über 6088 statische Felder mit Initialisierung.

Jede der drei Regeln ist in beide Richtungen nachgemessen: über den sauberen Baum null Funde,
und mit der alten Fassung von `XFactory44` melden sie genau die vier Fehler, die der Übersetzer
gemeldet hat — und sonst nichts.

Alle 31 Tore grün.

## Die rote Kiste

Vierzehn Sonderstücke, und jedes war schon da — bis auf `gun_autoshotgun_sexy` aus der Runde
davor. Zwei, die ich zwischendurch für fehlend gehalten hatte, sind es nicht: `bottle_sparkle`
und `bottle_rad` stehen im Port nicht als eigene Gegenstände, sondern als Untertypen `SPARKLE`
und `RAD` des Getränks. Die Flaschenfamilie ist seit der Dosenrunde vollständig.

**Die rote Kiste zieht nicht.** Alle anderen vier würfeln eine Anzahl und ziehen so oft aus
ihrer Liste; die rote kippt jeden ihrer vierzehn Einträge genau einmal aus. Das Original tut
das auf einem Umweg — es würfelt erst eine Anzahl und zieht, leert die Liste danach aber wieder
und füllt sie mit dem ganzen Inhalt (`BlockCrate.java:168 ff.`). Hier steht gleich das
Ergebnis.

Daraus folgt, dass `CrateLoot.rot()` keine Gewichte trägt. Die vier anderen Listen sind
gewichtet, weil aus ihnen gezogen wird; aus dieser nicht.

**Kein Kreativ-Reiter.** Im Original hat sie `setCreativeTab(null)` — sie steht nur in
Weltbauwerken. Das ist damit die zweite ehrliche Ausnahme in `tab-check`, neben dem
Geheimspaten, der in ihr liegt.

### Ein Torfehler, den die rote Kiste ans Licht gebracht hat

`api-check` kennt seit Runde 60 die Regel „`.length` auf einer Sammlung statt `.size()`". Sie
meldet einen Namen nur dann, wenn er im ganzen Projekt ausschließlich als Sammlung deklariert
ist — eine vorsichtige Regel, die genau deshalb jahrelang still blieb.

In `LootCrateBlock` entstand jetzt eine `List<ItemStack> inhalt`. Damit galt der Name
projektweit als Sammlung, und die Regel meldete fünf Fundstellen in `RadioTelexScreen` — wo
`inhalt` ein `String` ist und `inhalt.length()` völlig richtig. Der Ausdruck endete auf
`length\b`, und das trifft auch den **Methodenaufruf**.

Eine Vorausschau `(?!\s*\()` dahinter, und der Fall ist sauber getrennt. Nachgemessen: der Baum
ist still, und eine untergeschobene Datei mit echtem `.length` auf einer `List` wird weiterhin
gemeldet.

Der Fehler war die ganze Zeit da; er brauchte nur einen zweiten Namensträger, um sichtbar zu
werden. Das ist die unangenehme Eigenschaft von Regeln, die sich auf den ganzen Baum stützen:
sie können durch eine Änderung an ganz anderer Stelle erst falsch werden.

Damit steht die Kistenfamilie bis auf `crate_supply` vollständig.

Alle 31 Tore grün.

## Die Nachschubkiste am Fallschirm

Die letzte der Kistenfamilie. Sie stand bisher als „blockiert auf die C-130" in der Liste — das
war zu weit gegriffen. Blockiert ist nur, **wer sie abwirft**; die Kiste selbst und ihr
Fallschirm hängen an nichts, was dem Port fehlt.

### Die Kiste

Anders als alle anderen Kisten des Mods würfelt sie ihren Inhalt nicht aus. Sie trägt genau
das, was ihr mitgegeben wurde, und gibt es beim Aufbrechen wieder heraus — dafür ein
Blockobjekt mit einer schlichten Liste von Gegenständen, ohne Oberfläche, ohne Fächer, ohne
Schnittstelle. Es tickt nicht.

Wird sie stattdessen abgebaut, wandert der Inhalt über `IPersistentNBT` in den
Gegenstandsstapel und beim Setzen zurück. Das Original schreibt dafür von Hand `slot0`,
`slot1` … in die Gegenstandsdaten und zählt sie in `amount`; im Port genügt derselbe Weg, den
die Lagerkisten und die Fässer schon gehen.

**Sie teilt sich ihr Aussehen mit der Dosenkiste**, wie im Original: dort verweist ihr
Zeichnertyp auf `BlockCanCrate.renderID` und ihre Textur auf `hbm:crate_can`. Im Port zeigt ihr
Blockzustand auf dasselbe `block/crate_can`, das in der Dosenrunde als gewöhnliches Blockmodell
entstanden ist.

### Der Fallschirm

Sie sinkt mit einem Fünftel Block je Tick — das ist der Schirm. Die Höhengrenze bei 600 ist die
des Originals: wird sie höher eingesetzt, fällt sie sofort auf diese Höhe zurück. Das Original
setzt dafür `posY` direkt; hier geschieht es über `setPos`, sonst wanderte die Begrenzungsbox
nicht mit.

Der Zeichner lässt sie pendeln: zwei Sinuskurven, um eine Viertelwelle gegeneinander versetzt,
kippen sie um bis zu fünf Grad in beide Achsen, mit dem Drehpunkt sieben Blöcke darüber — also
dort, wo der Schirm hängt.

**Abweichung, woraus die Kiste gezeichnet wird:** das Original nimmt dafür `conservecrate.obj`.
Der Port hat die Dosenkiste als gewöhnliches Blockmodell nachgebaut, und die Nachschubkiste
teilt es sich mit ihr — also wird dasselbe Blockmodell gezeichnet statt eines zweiten Modells
in einem anderen Format. Der Schirm selbst ist das `Chute`-Teil aus `soyuz_lander.obj`, das der
Port seit der Sojus-Runde hat.

**Was bleibt:** niemand wirft sie ab. Im Original ist das die C-130, und die ist eine eigene
Runde. Die Kiste lässt sich von jeder künftigen Quelle einsetzen — `ParachuteCrate.items`
füllen, Entität in die Welt setzen, fertig.

Damit ist die Kistenfamilie vollständig: `crate`, `crate_weapon`, `crate_lead`, `crate_metal`,
`crate_ammo`, `crate_can`, `crate_red` und `crate_supply`.

Alle 31 Tore grün.

## Der Beutesockel

Ein Blech von einem Sechzehntel Blockhöhe, das selbst nichts zeigt. Was man sieht, sind die
Gegenstände, die auf ihm liegen — jeder mit seinem eigenen Versatz innerhalb des Blocks. Die
Weltgenerierung streut damit Gerümpel in ihre Bauwerke: ein Gewehr auf einem Tisch, Patronen
daneben, eine Dose auf dem Boden.

Er gibt sich selbst nicht her: was beim Zerschlagen herausfällt, sind die Stapel; den Sockel
bekommt man nicht. Deshalb `noLootTable()` und kein Kreativreiter — im Original
`getItemDropped` auf `null` und `setCreativeTab(null)`.

**Rechtsklick ohne Schleichen räumt ihn weg.** Weil das Wegräumen durch dieselbe Stelle läuft
wie das Zerschlagen, fallen die Stapel dabei ebenso heraus; im Original geht der Rechtsklick
über `setBlockToAir`, und das ruft `breakBlock`. Wer schleicht, geht durch — dann greift, was
unter dem Sockel steht.

Der Zeichner legt den Normalfall als Gegenstandsbild hin, um neunzig Grad gekippt und halbiert;
das Original zeichnet dafür noch ein rohes Zweiecksbild. Zwei Ausnahmen hat es, und beide hat
der Port: eine Minikernwaffe (die fünf Patronen von `NUKE_STANDARD` bis `NUKE_HIVE`) und die
Mare's Leg liegen als körperliche Modelle da. **Zwei weitere sind nicht übernommen** — die
Trenchmaster- und die NCR-Rüstung zeichnet das Original als getragene Rüstungsteile; beide
Rüstungen hat der Port nicht, und sobald sie nachkommen, liegen sie hier zunächst als
gewöhnliches Gegenstandsbild.

### Drei Tore haben zugegriffen

Der Sockel ist klein, und trotzdem haben drei der einunddreißig Tore etwas gefunden, bevor er
überhaupt nach CI ging:

* **`api-check`**: `@Override` auf `getRenderBoundingBox`. Die Methode stammt aus der
  NeoForge-Erweiterung von `BlockEntity`, nicht aus der Minecraft-Klasse, und gilt dem
  Übersetzer nicht als überschrieben.
* **`loot-check`**: keine Beutetabelle. Richtig war nicht, eine anzulegen, sondern
  `noLootTable()` an den Block zu schreiben — er gibt sich selbst ja nicht her.
* **`tab-check`**: in keinem Kreativreiter. Das ist hier korrekt und gehört mit Begründung in
  die Ausnahmeliste, nicht in einen Reiter.

### Stand der kuratierten Lücke

Die Liste aus der Stufe-5-Bestandsaufnahme stand einmal bei neunzehn. Mit den sechs Beutekisten
(vorige Runden), Teslaspule und HEV-Batterie, Mikrowelle, Radioempfänger und Fernschreiber und
jetzt dem Beutesockel sind **sechs übrig** — und alle sechs sind dasselbe: `wand_jigsaw`,
`wand_logic`, `wand_loot`, `wand_tandem`, `dungeon_spawner`, `meteor_spawner`. Das sind die
Werkzeuge, mit denen das Original seine Bauwerke zusammensetzt; sie sinnvoll zu portieren
heißt, die Bauwerksgenerierung selbst zu portieren. Das ist keine Runde, das ist eine Stufe.

Alle 31 Tore grün.

## Berichtigung: ein Parametertyp, und zwei neue Tore

Der Beutesockel fiel in CI durch, mit genau einem Fehler:

```
LootDecoBlock.java:60: error: method does not override or implement a method from a supertype
```

`getShape` nimmt in 1.21 ein `BlockGetter`, ich hatte `LevelReader` geschrieben. Alle
einunddreißig Tore waren grün, und keines hat es gesehen — `syntax-check` übersetzt ohne
Minecraft-Klassenpfad, die Oberklasse fehlt, der Rumpf wird gar nicht erst geprüft.

### `signature-check`, das zweiunddreißigste Tor

Der Fall ist ohne Klassenpfad entscheidbar, und zwar aus dem Projekt selbst heraus: die
richtige Signatur steht **achtundvierzigmal** im Port. Weicht eine einzige Stelle ab, ist sie
es, die falsch ist.

Vier Bedingungen halten die Fehlalarme heraus, und jede hat sich in der Messung verdient. Ohne
sie meldete der Prototyp vier Funde, alle falsch:

* mindestens fünf Belege für den Namen und mindestens neunzig Prozent auf einer Signatur —
  sonst ist „die Mehrheit" nichts wert;
* **gleiche Stelligkeit**: eine andere Anzahl Parameter ist eine echte Überladung
  (`getMaxStackSize()` neben `getMaxStackSize(ItemStack)`), kein Tippfehler;
* **genau ein abweichender Typ**: zwei und mehr heißt, es ist eine andere Methode;
* Paketpräfixe fallen vor dem Vergleich weg, auch innerhalb spitzer Klammern — sonst gilt
  `StateDefinition.Builder<net.minecraft…Block, BlockState>` als eigene Signatur, und
  `MachineShredderBlock` wurde genau dafür gemeldet.

Über den ganzen Baum: null Funde. Mit `LevelReader` wieder eingesetzt: genau diese Zeile.

**Die Grenze, die bleibt:** ein Name, den das Projekt nur einmal überschreibt, hat keine
Mehrheit, gegen die er sich messen ließe. Dort hilft weiter nur CI.

### `metatex-check`, das dreiunddreißigste Tor

In derselben Runde eine Lücke geschlossen, die seit der Granatenrunde offen notiert war:
`EnumMultiItem` legt beim Datenerzeugen je Aufzählungswert ein eigenes Modell an, dessen
`layer0` auf `item/<Registername>.<wert>` zeigt. **Diese Modelle stehen nirgends im Baum** — sie
entstehen erst beim Lauf. `asset-check` geht die Modell-JSONs auf der Platte durch und sieht
sie deshalb nicht, `model-check` ebenso wenig. Fehlt eines der Bilder, zeigt der Gegenstand im
Spiel das schwarz-violette Ersatzmuster, und kein Tor sagt etwas.

Das Tor liest aus jeder Klasse mit `registerItemModel` den `layer0`-Ausdruck und führt ihn auf
drei Bausteine zurück: Zeichenketten, `modelLocation.getPath()` und
`num.name().toLowerCase(…)`. Was sich darauf zurückführen lässt, wird über alle Werte der
Aufzählung geprüft. Stand: 33 Registrierungen, 410 Bilder, null fehlend.

**Zwei Dinge, die beim Bauen des Tors auffielen:**

`ConserveItem` überschreibt `registerItemModel` und benennt seine Bilder `canned_<wert>` statt
`canned_conserve.<wert>`. Ein Tor, das die Überschreibung übersieht, hätte siebenundzwanzig
Fehlalarme gemeldet — und wer sie dann „wegräumt", macht es kaputt.

Umgekehrt war `AmmoContainerItem` zuerst **unsichtbar**: sein `super`-Aufruf reicht
`properties.stacksTo(1)` durch, und mein Ausdruck verlangte dort einen bloßen Namen. Aufgefallen
ist das nur, weil ich die Gegenprobe gemacht habe — ein Bild weggenommen, und das Tor schwieg.
Deshalb zählt es jetzt selbst auf, wo es **nicht** hinschaut: drei Klassen bauen ihren Bildnamen
aus Feldern oder Rechnungen und stehen mit Namen in der Ausgabe. Ein Tor, das stillschweigend
woanders hinschaut, ist schlimmer als keins.

Alle 33 Tore grün.

## Berichtigung: die Schredder-Schrotflinte war nie blockiert

Im Commit der Autoschrotflinte stand, `gun_autoshotgun_shredder` sei nicht übernommen, weil
„das Geschossteilsystem des Ports diese Aufspaltung noch nicht kennt". **Das war falsch, und
zwar ohne dass ich nachgesehen hätte.** Nachgemessen ist jedes einzelne Stück da:

| gebraucht | im Port |
|---|---|
| `setBeam`, `setOnBeamImpact` | da, `BulletBeamBase.onImpact` ruft den Haken |
| `setOnRicochet` | da, `BulletBaseMK4.onHitBlock` ruft den Haken |
| `ricochetAngle`, `maxRicochetCount`, Feld `ricochets` | alle drei da |
| `BulletConfig.clone()`, `getDamage(...)` | da |
| Konstruktor für die Splitter | `BulletBaseMK4(Level, LivingEntity, BulletConfig, float, float, Vec3, Vec3)` |

Es fehlten zwei Schauwerte, mehr nicht. Die Waffe ist jetzt drin.

### Was der Schredder tut

Jede seiner sechs Patronen ist ein **Strahl**, der den Schaden aller Schrotkugeln auf einmal
trägt (`damageMult × projectilesMax`). Wo er auftrifft, richtet er im Umkreis eines
Dreiviertelblocks Laserschaden an und zerfällt in Splitter: an einer Wand fliegen sie von ihr
weg, in einem Getroffenen stieben sie in alle Richtungen. Die Splitter selbst sind langsame,
langlebige Kopien der Ausgangspatrone, die bis zu dreimal von Wänden abspringen — bei jedem
Winkel bis neunzig Grad, also praktisch immer — und bei jedem Absprung Plasmaschaden im
Umkreis eines halben Blocks machen.

Sie kommen vom **Gurt**, nicht aus einem Kasten: `MagazineBelt` statt `MagazineFullReload`.

### Der zweite Fehler, den das aufgedeckt hat

Beim Schreiben hatte ich die Winkelrechnung und das Abprallen von Hand gebaut — und dabei in
den Kommentar geschrieben, `BlockDetonatable` gebe es im Port nicht und der Teleport-Kniff des
Originals sei in 1.21 unnötig. Beides falsch: `BulletConfig.LAMBDA_STANDARD_RICOCHET` steht
seit Langem im Port, benutzt `DetonatableBlock`, `BobMathUtil.getCrossAngle` **und** genau
diesen Teleport-Kniff. Der Schredder-Abpraller ist jetzt derselbe Ablauf mit einem Zusatz
(dem Flächenschaden) statt einer zweiten, abweichenden Fassung.

Zwei falsche Behauptungen in einer Runde, beide aus demselben Grund: ich habe geschrieben, was
ich erwartet habe, statt nachzusehen. Die Messung dauert eine Minute.

### `import-check` hatte ein Loch: `instanceof`

Bei der ersten Fassung stand ein `BulletBeamBase` ohne Import in der Datei, und das Tor schwieg.
Der Grund: sein Ausdruck für die Deklaration sucht `Typ name` gefolgt von `=`, `;` oder `)` —
die Form `instanceof Typ name ?` hat danach ein Fragezeichen. Die Gegenprobe des Tors kannte
`instanceof` zwar, aber nur für „gibt es diesen Namen überhaupt". Jetzt steht er in beiden.

Damit hat dieselbe Runde alle zehn übrigen fehlenden Importe **statisch** gemeldet, darunter
`DamageClass` als verschachtelten Typ — die Regel aus der vorigen Runde bei ihrem ersten
echten Einsatz.

### Und ein Modell, das CI gefunden hat

Der Beutesockel fiel noch einmal durch: `models/item/deco_loot.json` fehlte. Sein BlockItem
liegt zwar nie in einer Hand — kein Reiter, keine Beute —, ein Modell braucht es trotzdem,
sonst zeigt Minecraft den Fehlwürfel. **Dieses Tor lässt sich hier nicht vorab fahren:**
`model-resolve-check` braucht die erzeugte Sprachdatei, um zu wissen, welche Gegenstände es
gibt, und die entsteht erst in `runData`. Lokal meldet es deshalb „ohne Sprachdatei" und
überspringt genau diese Prüfung. Das ist eine Grenze der Werkstatt, kein Fehler des Tors.

Alle 33 Tore grün.

## Die Hälfte des Modelltors, die auch ohne Datengenerator läuft

Der Beutesockel ist zweimal durchgefallen, und beim zweiten Mal an etwas, das lokal **gar nicht
prüfbar war**: `model-resolve-check` braucht die erzeugte Sprachdatei, um zu wissen, welche
Gegenstände es gibt. Die entsteht erst in `runData`. Lokal meldet das Tor „ohne Sprachdatei"
und überspringt genau die Prüfung, die `models/item/deco_loot.json` vermisst hätte.

**Ein Teil davon ist sehr wohl ohne Sprachdatei entscheidbar.** Wer seinen Blockzustand von
Hand nach `src/main/resources/assets/hbmsntm/blockstates` schreibt, bekommt vom Datenerzeuger
auch kein Gegenstandsmodell — der läuft ja gar nicht über ihn. Für diese Blöcke muss das
Gegenstandsmodell ebenfalls von Hand dastehen.

Das sind wenige: elf handgeschriebene Blockzustände zur Zeit. Aber es ist **genau die Gruppe,
in der der Fehler entsteht** — wer alles dem Datenerzeuger überlässt, kann ihn gar nicht
machen. Zehn haben ihr Gegenstandsmodell, der elfte (`slag`) bekommt absichtlich keinen
Gegenstand und steht deshalb schon in `OHNE_GEGENSTAND`.

Nachgemessen in beide Richtungen: null Funde; nimmt man `models/item/deco_loot.json` wieder
heraus, meldet das Tor genau ihn — und zwar lokal, in einer Sekunde, statt nach fünfzehn
Minuten CI.

Alle 33 Tore grün.

## Berichtigung: der Protégé war auch nicht blockiert

Dritte falsche „blockiert"-Behauptung in Folge, und dieselbe Ursache. In `XFactory44` stand:

> **NICHT ÜBERNOMMEN: der Protégé**, die Schwesterwaffe mit `m44_equestrian_mn7`. Die schießt
> Torpedos, und `EntityTorpedo` hat der Port nicht.

Der zweite Halbsatz stimmt — die Klasse gab es nicht. Der Schluss daraus war falsch:
`EntityTorpedo` ist fast dasselbe wie der Güterwagen und das Luftschiff, die ich in derselben
Sitzung portiert habe. Ein Ding fällt vom Himmel, und beim Aufsetzen passiert etwas. Beim
Torpedo ist dieses Etwas eine gewöhnliche Explosion der Stärke zwanzig — `ExplosionVNT`
mit `makeStandard()`, beides seit Langem im Port.

Der Torpedo fällt schneller als seine Geschwister: zweieinhalb Blöcke je Tick statt anderthalb,
Beschleunigung 0,04 statt 0,03. Und er kippt im Fall nach vorn, drei Grad je Tick bis
fünfundachtzig — das ist der einzige Unterschied im Zeichner.

Der Protégé selbst ist der Li'l Mac mit drei Abweichungen: andere Signaturpatrone, kein
Zielfernrohr, und sein Schuss klingt tiefer (dieselbe Aufnahme mit Tonhöhe 0,8 statt 1,0).

### Was ich daraus mitnehme

Dreimal in einer Sitzung habe ich „blockiert" geschrieben, ohne nachzusehen — Schredder,
`DetonatableBlock` samt Teleport-Kniff, Protégé. Jedes Mal war der Baustein schon da, und jedes
Mal hat das Nachmessen unter einer Minute gedauert.

Das Muster ist klar: **ich habe aus dem Namen auf den Zustand geschlossen.** „`EntityTorpedo`
gibt es nicht" stimmte; „also ist der Protégé blockiert" war ein Sprung. Die richtige Frage ist
nicht, ob die Klasse fehlt, sondern **woraus sie besteht** — und das steht im Original, fünfzig
Zeilen weit.

Damit bleibt von den Waffen, die ich als blockiert notiert habe, nur noch eine Gruppe übrig,
und die ist nachgemessen: Stinger, Quadro und Raketenwerfer. Der Stinger braucht
`ItemGunStinger` samt Zielerfassung, die beiden anderen lenkbare Raketen (`rocket_qd`,
`rocket_ml`) — das sind eigene Teilsysteme, keine einzelnen Klassen.

Alle 33 Tore grün.

## Die C-130

Der vierte und letzte „blockiert", der keiner war. Die Nachschubkiste am Fallschirm stand in
der vorigen Runde mit dem Vermerk „niemand wirft sie ab — im Original ist das die C-130, und
die ist eine eigene Runde". Eine Runde ist sie, blockiert war sie nicht: **das Modell
`c130.obj`, die Textur und ihre beiden `ResourceManager`-Einträge lagen längst im Port**, und
`PlaneBase`, `ItemPool` und `AudioWrapper.getLoopedSound` ebenso.

Eine Leuchtpatrone ruft sie. Nicht beim Aufschlag, sondern **vierzig Ticks nach dem Abschuss** —
die Leuchtkugel steigt noch, und das Flugzeug ist schon unterwegs. Wer senkrecht nach oben
schießt, bekommt es über dem eigenen Kopf; das ist im Original genauso. Es wird hundert Blöcke
vor der Stelle und hundert über der Geländeoberkante eingesetzt, fliegt geradeaus und wirft auf
halber Strecke die Kiste ab: sieben Tickschritte hinter sich und zehn Blöcke tiefer, als wäre
sie aus der Heckklappe gerutscht.

Die Propeller drehen sich nach der Uhr, nicht nach der Spielzeit — fünfzehn Grad je
Millisekunde. Sie laufen also auch weiter, wenn das Spiel steht. Auch das ist übernommen.

### Die Ladung, und was ihr fehlt

Drei Vorräte: Nachschub, Waffen, Munition. Blau zieht fünfmal Nachschub, grün ein bis zwei
Waffen und sechsmal Munition.

**Sieben Einträge des Originals fehlen, und zwar weil ihre Gegenstände fehlen** — nicht aus
Nachlässigkeit. Sie stehen namentlich im Kopf von `ItemPoolsC130`, damit die spätere Runde
nicht nachschlagen muss:

* Nachschub: `definitelyfood`, `pill_iodine`, `canister_full` (Diesel), `med_bag`, `radaway`
* Waffen: `gun_henry`, `gun_n_i_4_n_i`

Die übrigen stehen mit den Gewichten des Originals da. Ein gewichteter Vorrat verträgt das —
er zieht aus dem, was drin ist —, aber die Verteilung ist bis dahin eine andere, und das gehört
gesagt statt verschwiegen.

### `ItemPool` kann jetzt Metadatenstapel

Die Munitionstabelle des Originals besteht aus `ammo_standard` mit neun verschiedenen
Metadaten. Der Nachbau im Port nahm bisher nur ein `ItemLike` — ein Gegenstand, eine
Stückzahl —, und damit ließ sich keine einzige Zeile davon ausdrücken. Ein zweiter Zugang
nimmt jetzt einen Lieferanten, der Gegenstand **und** Stückzahl selbst setzt; der alte Weg ist
ein Aufruf des neuen geworden, damit es nur eine Zugmechanik gibt.

### Die vier Fehlurteile dieser Sitzung

Schredder, `DetonatableBlock`, Protégé, C-130 — viermal „blockiert" geschrieben, viermal war
der Baustein da. Beim Schredder und beim Protégé habe ich vom fehlenden Klassennamen auf den
Zustand geschlossen; bei der C-130 habe ich nicht einmal nachgesehen, ob ihr Modell im Baum
liegt. **Es lag da, seit jemand es hineinkopiert hat.**

Alle 33 Tore grün.

## Die Ölpfütze des Ablasses

Das fünfte Fehlurteil dieser Sitzung, und es stand nicht bei einer Waffe, sondern in einer
Maschine. In `MachineDrainBlockEntity` stand seit der Portierung:

> NICHT ÜBERNOMMEN: die Öllache. […] Der Block `oil_spill` fehlt dem Port noch; die Lache
> kommt mit ihm.

`NtmBlocks.OIL_SPILL` liegt im Baum, mit Blockzustand, Modell und Textur. Der Satz war nie
wahr — er war beim Portieren des Ablasses geschrieben und danach nie wieder nachgemessen
worden, obwohl der Block in einer späteren Runde dazukam. Genau dieselbe Mechanik wie bei
Schredder, Protégé und C-130, nur andersherum: dort habe ich aus einem fehlenden Namen auf
einen fehlenden Baustein geschlossen, hier habe ich einen einmal geschriebenen Satz nicht
mehr angefasst.

Jetzt läuft sie. Zähe, brennbare Flüssigkeiten hinterlassen mit einer Wahrscheinlichkeit von
eins zu zwanzig einen Ölfleck — aber nur, wenn auf einmal mindestens hundert Millibar
auslaufen, sonst tropft der Ablass die Welt voll. Die Stelle sucht ein Strahl, der **drei
Blöcke hinter dem Auslass** ansetzt, fünfundzwanzig Blöcke tief fällt und dabei seitlich
streut. Er zählt nur, wenn er auf eine **Oberseite** trifft: an einer Wand läuft nichts
zusammen. Darüber muss Platz sein, kein Fluid stehen, und der Fleck muss dort halten können.

## Der CI-Lauf `fe2afaf0` — und eine Ferndiagnose, die daneben lag

Der C-130-Lauf ist an zwei Übersetzungsfehlern gescheitert. Das Fenster, das ich mir aus dem
Job-Protokoll geholt hatte, endete über den Fehlerzeilen — es zeigte siebenunddreißig
Verfallswarnungen und die Schlusszeile „2 errors", aber nicht die Fehler selbst.

**Und dann habe ich geraten.** Aus der Form der Änderung habe ich mir eine Erklärung gebaut:
das Feld `NtmEntityTypes.C130` heiße wie seine eigene Klasse und verdecke sie, `C130::new`
löse also auf das Feld statt auf den Typ auf. Ich habe das Feld in `C130_PLANE` umbenannt und
die Begründung als Kommentar dazugeschrieben, so als wäre sie gemessen.

Sie war falsch, und zwar zweifach. Erstens steht in `C130::new` links eine `ClassType` —
die Grammatik lässt dort gar nichts anderes zu, ein gleichnamiges Feld kann eine
Konstruktorreferenz nicht verdecken. Zweitens waren es ganz andere Zeilen:

```
XFactory40mm.java:387: error: cannot find symbol
    public static Consumer<Entity> LAMBDA_SPAWN_C130_SUPPLIES = …
                  ^   symbol: class Consumer
```

Ein vergessener Import, `java.util.function.Consumer`. Die Datei importiert `BiConsumer` und
`BiFunction`, und weil die beiden Lambdas der C-130 nur ein Argument nehmen, brauchte es den
dritten — den hatte ich nicht geschrieben.

Die Umbenennung ist zurückgenommen. Ein Kommentar, der einen Übersetzungsfehler behauptet,
den es nicht gibt, ist schlimmer als gar keiner: die nächste Runde hätte ihn geglaubt.

### Das Tor, das danebenschaute

`import-check` kannte seit Runde 180 zwei Deklarationsformen: den nackten Typ
(`Vec3 stelle = …`) und das Generikum-**Argument** (`BiConsumer<A, B>` → `A`, `B`). Dazwischen
klaffte die dritte, und genau sie stand hier: das Generikum als **Kopf** einer Deklaration.
`Consumer<Entity> LAMBDA_X = …` — hinter `Consumer` steht kein Leerzeichen, sondern ein `<`,
also griff die erste Regel nicht; und die zweite liest die Klammer von innen, also nannte sie
`Entity`, nie `Consumer`.

Die Form ist jetzt an allen drei Prüfungen des Werkzeugs nachgetragen (Projekttypen,
Fremdtypen, verschachtelte Projekttypen). Die Zeichenklasse zwischen den spitzen Klammern
enthält weder `=` noch `;` noch `(` — der Ausdruck kann damit keine Anweisungsgrenze
überspringen.

**Nachgemessen in beide Richtungen:** über alle 2.056 Dateien meldet die Regel null Funde.
Nimmt man den Import wieder heraus, meldet sie genau eine Zeile — `Consumer` in
`XFactory40mm` — und sonst nichts.

Alle 33 Tore grün.

## Ein Tor auf die Kommentare

Fünf Fehlurteile in einer Sitzung, alle nach demselben Muster: ein Satz, der beim Schreiben
wahr war und beim Lesen falsch. Dagegen hilft keine Sorgfalt, sondern nur eine Messung — also
gibt es jetzt ein vierunddreißigstes Tor, und es ist das einzige, das nicht den Quelltext
prüft, sondern das, was über ihn **behauptet** wird.

Es sammelt jeden Registriernamen, den der Port wirklich vergibt, zerlegt jeden Kommentar in
Sätze, sucht die Sätze mit einer Verneinung („fehlt", „nicht übernommen", „gibt es nicht") und
schlägt an, wenn in so einem Satz ein Name steht, den es gibt. Drei Filter halten die
Fehlalarme heraus, und jeder war nötig:

* Kommentare über **Rezepte und Tabelleneinträge**. „insert_doxium hat auch im Original kein
  Rezept" sagt nichts über den Gegenstand aus. Gewertet wird der ganze Kommentar, nicht der
  einzelne Satz — in einer Aufzählung steht das Wort „Rezept" oft erst in der Überschrift.
* Sätze, in denen **auch ein unbekannter Name** steht. „ore_nether_fire -> crystal_phosphorus
  — der Block fehlt im Port" meint den Eingang, nicht den Ausgang.
* Nur **Registriernamen**. Ein Vorversuch nahm auch CamelCase-Klassennamen und kam damit auf
  sieben Fehlalarme, weil Kommentare ständig über Klassen des Originals reden.

**Nachgemessen in beide Richtungen:** über den ganzen Baum prüft es 490 Sätze mit einer
Verneinung und meldet keinen. Setzt man die vier historischen Sätze wieder ein — `oil_spill`
im Ablass, `flame_pony` im Turbofan, `ingot_cft` im Kristallisator, `steel_grate_wide` im
Gitter —, meldet es genau diese vier und sonst nichts.

### Die Gegenprobe hat zuerst das Tor widerlegt, nicht den Baum

Die erste Fassung war auf dem sauberen Baum still und sah bei der Gegenprobe **einen von drei**
Sätzen. Zwei Löcher:

1. Sie sammelte Registriernamen aus `.register("...")` — mit Punkt. Blöcke laufen aber über
   den bloßen Helfer `register("oil_spill", …)`. Das Tor war damit für den **gesamten
   Blockbestand** blind, also genau dort, wo der Satz stand, der es überhaupt ausgelöst hat.
   Von 1.569 bekannten Namen wurden es nach der Reparatur 1.992.
2. Sie verlangte „gibt es" als Wortpaar und sah „**Es gibt** den Gegenstand … **nicht**" nicht,
   weil sich die Wortstellung dreht.

Ein Tor, das nur behauptet zu messen, ist genau der Fehler, gegen den es gebaut wurde. Beide
Löcher sind in seinem Kopf vermerkt, damit die nächste Fassung nicht wieder dort hineinfällt.

### Was es nicht sieht

Behauptungen über **Klassen, Mechaniken und Teilsysteme** — alles ohne Registriernamen. Vier
der fünf Fehlurteile dieser Sitzung waren von dieser Art; das Tor hätte nur das fünfte
gefunden. Es deckt den billigsten Teil des Problems ab, nicht den ganzen. Und `docs/ROADMAP.md`
sieht es nicht an; dort stehen dieselben Behauptungen in Prosa, das ist eine eigene Runde.

## Was es im ersten Lauf gefunden hat

Drei Sätze, die niemandem aufgefallen waren.

### Die Nachbrennerstufe 100

Im Turbofan stand, den Gegenstand `flame_pony` gebe es im Port nicht. Er liegt seit der Runde
der roten Kiste da. Im Original setzt er, in den Upgradeschacht gelegt, den Nachbrenner auf
**100** — weit über die drei Stufen hinaus, die es zu kaufen gibt.

Das Bemerkenswerte: der Port hatte die Folgen längst portiert und die Ursache nicht. Zwei
Zweige fragen `afterburner > 90` ab — ein Knirschen im Triebwerk und eine Flammenfahne —, und
beide waren **unerreichbar**, weil der höchste kaufbare Wert 3 ist. Sie standen als toter Code
da, ohne dass es auffiel. Jetzt nimmt der Schacht das Pony an, und die beiden Zweige laufen.

### Das breite Gitter

Im Gitterblock stand, das breite Gitter (`steel_grate_wide`) sei „nicht portiert". Der Block
ist vollständig da: registriert, mit Blockzustand, Beutetabelle, Sprachschlüssel und
Kreativreiter. Was fehlte, war sein einziger Unterschied zum schmalen — **Gegenstände und
Erfahrungskugeln fallen hindurch**. Der Satz ist stehen geblieben, als der Block nachgereicht
wurde.

Das Original unterscheidet die beiden Gitter an der Blockinstanz, und genauso steht es jetzt
auch hier: der Kollisionskasten ist leer, wenn die fragende Entität ein Gegenstand oder eine
Erfahrungskugel ist, und `entityInside` zieht sie nach unten durch, statt sie liegen zu lassen.
Ohne das Zweite bliebe ein abgelegter Gegenstand im Kasten stecken, weil er sich selbst nicht
mehr bewegt.

Nicht übernommen ist ein Kniff des Originals: dort ist der Kasten des breiten Gitters ein
Tausendstel flacher, damit Gegenstände einsinken. Bei einem leeren Kollisionskasten braucht es
ihn nicht, und die Auswahlbox bliebe sonst sichtbar schief.

Dazu die beiden Baupläne aus `CraftingManager` Z. 466/467, die der Port noch nicht hatte: zwei
schmale Gitter ergeben vier breite, zwei breite ein schmales. Die Zahlen sind die des Originals
und nicht ausgeglichen; sie bleiben, wie sie dort stehen.

### `ingot_cft` — halb falsch, und das ist der interessantere Fall

Im Kristallisator stand zu einem ausgelassenen Rezept: „beide Items fehlen im Port". Eines der
beiden gibt es sehr wohl (`NtmItems.INGOT_CTF`). Was wirklich fehlt, ist die **Fullerenasche** —
der Port hat die fünf Aschesorten als eigene Gegenstände, und `FULLERENE` ist nicht darunter.
Ihre einzige Quelle im Original ist der SILEX, und der ist nicht portiert.

Der Satz war also nicht erfunden, sondern **ungenau**, und die Ungenauigkeit hat das Rezept
unter einer falschen Begründung liegen lassen. Die richtige steht jetzt da: mit dem SILEX kommt
die Asche, mit der Asche das Rezept.

## Sieben Tore liefen nie in der CI

Beim Einhängen des neuen Tores ist aufgefallen, dass acht der bisherigen dreiunddreißig in
`build.yml` überhaupt nicht vorkommen — darunter ausgerechnet die vier, die ich in dieser und
der vorigen Sitzung **als Antwort auf einen CI-Fehlschlag** gebaut habe: `registry-check`,
`signature-check`, `forward-check`, `metatex-check`. Sie liefen nur, wenn ich sie von Hand
aufrief. „Alle Tore grün" stand damit auf meinem Wort statt auf einer Messung.

Sieben davon hängen jetzt drin. Gemessen kosten sie zusammen knapp zwei Minuten (`api-check`
64 s und `forward-check` 42 s sind die schweren, die übrigen fünf zusammen sechs Sekunden) —
und sie melden ihren Fund mit einer genauen Zeile, während derselbe Fehler sonst vier Minuten
später als `javac`-Meldung auftaucht.

Das achte, `syntax-check`, bleibt draußen, und zwar begründet: es übersetzt **ohne**
Minecraft-Klassenpfad und ist damit ein schwächerer Abklatsch des Bauschritts, der weiter unten
richtig übersetzt. Örtlich ist es die einzige Möglichkeit, in der CI wäre es nur Wartezeit.

Alle 34 Tore grün.

## Die Radaway-Familie — ein Effekt ohne Quelle

Dieselbe Form wie beim Feuerpony, nur größer. `ModEffect.RADAWAY` liegt seit Langem im Port,
voll ausgeführt: er zieht je Tick Strahlung ab. **Ausgelöst hat ihn nie etwas.** Die einzige
Stelle, die ihn überhaupt erwähnte, war das Radongrab — und zwar um ihn wieder *wegzunehmen*
(`livingEntity.removeEffect(ModEffect.RADAWAY); //get fucked`). Die Wirkung war da, die Ursache
fehlte.

Die Ursache sind sieben Gegenstände, und ich hatte sie in der C-130-Runde als blockiert
notiert. Nachgemessen war das falsch:

* `radaway` stand in der Liste der fehlenden Vorratseinträge. Eine Suche nach dem Namen im Port
  findet ihn — aber als **Trankeffekt**, nicht als Gegenstand. Zwei Registrierungen dürfen
  denselben Namen tragen, wenn sie in verschiedenen Verzeichnissen stehen. (Genau diese
  Zweideutigkeit ist auch eine Schwäche des neuen Behauptungs-Tores: es kennt nur Namen, nicht
  Verzeichnisse. Hier hat sie nichts angerichtet, weil der zweite Filter — „steht auch ein
  unbekannter Name im Satz" — den Satz ohnehin durchgelassen hat.)
* Die Basisklasse `ItemSimpleConsumable` stand im Kopf von `SyringeItem` als **„ein eigenes
  Teilsystem"**. Sie ist 181 Zeilen lang, hat vier Lambda-Felder und drei Hilfsmethoden — und
  `SyringeItem` selbst ist nach genau derselben Bauart gebaut. Das war die sechste Behauptung
  dieser Sitzung, die beim Nachsehen zerfiel.
* Alle sieben Texturen und der Klang lagen in der CE-Abspaltung.

### Was jetzt drin ist

`SimpleConsumableItem` und sieben Gegenstände: drei Radaway-Stärken (140, 350, 500 Ticks) und
vier Beutel. Der leere Blutbeutel wird am eigenen Arm gefüllt — fünf Herzen für eine Konserve,
und wer damit auf null geht, stirbt daran. Der Fakeplayer ist davon ausgenommen, sonst ließe
sich der Beutel von einer Maschine füllen, die kein Leben hat, das sie verlieren könnte.
Dasselbe noch einmal mit Erfahrung statt Blut.

Radaway **addiert** seine Dauer auf einen schon anliegenden Effekt, statt ihn zu überschreiben.
Zwei Beutel wirken also doppelt so lange und nicht nur so lange wie einer; so steht es im
Original.

Dazu die fünf Baupläne aus `ConsumableRecipes` Z. 143–149 und der Vorratseintrag, der in der
C-130-Runde gefehlt hat: `radaway`, ein bis fünf Stück, Gewicht 10.

Die Namen und Hinweiszeilen stehen wie in `en_US.lang` und `ItemSyringe.addInformation` des
Originals — `iv_empty` heißt dort „IV Bag" und `iv_blood` „Blood Bag", nicht umgekehrt, und die
beiden Beutel tragen gar keine Hinweiszeile. Mein erster Entwurf hatte beides falsch: vier
Namen geraten und zwei Hinweiszeilen dazuerfunden. Auch das fällt nur auf, wenn man nachsieht
statt zu schließen. Übernommen ist auch, dass `radaway_flush` im Hinweis 1.000 RAD verspricht,
seine Dauer aber nur 500 Ticks beträgt.

### Zwei bewusste Auslassungen

**Die Trefferwirkung** (`setHitAction`) ist nicht übernommen. Sie gehört zu den drei Spritzen —
Gegenmittel, Gift, Wunderspritze —, die man auch jemand anderem in den Arm rammen kann, und die
stehen noch aus. Ein Lambda-Feld, das niemand setzt, wäre toter Code.

**Der Sanitätsbeutel** `med_bag` fehlt weiter, jetzt aber mit der richtigen Begründung: nicht
weil eine Basisklasse fehlt, sondern weil sein Bauplan die Wunderspritze und Kautschuk aus dem
Erzwörterbuch braucht. Von den sieben Einträgen der C-130 sind damit noch sechs offen.

Alle 34 Tore grün.

## Der Sanitätsbeutel — und die Behauptung, die keine Stunde alt war

Im Commit der Radaway-Familie steht, `med_bag` fehle weiter, „jetzt aber mit der richtigen
Begründung: weil sein Bauplan die Wunderspritze und Kautschuk aus dem Erzwörterbuch braucht".

Das war schon beim Schreiben falsch. `SYRINGE_METAL_SUPER` liegt seit der Spritzenrunde im
Port, `INGOT_RUBBER` seit der Kautschukkette, und `RADAWAY` — die dritte Zutat — war in
derselben Runde dazugekommen, in der ich den Satz geschrieben habe. **Ich habe eine
Blockade behauptet, während ich sie gerade auflöste.**

Das ist innerhalb einer Sitzung die siebte Behauptung dieser Art, und die einzige, die ich
selbst und direkt widerlegen konnte, weil ich beide Seiten in derselben Stunde geschrieben
habe. Der Reflex, eine Lücke mit einer Begründung zu schließen statt mit einer Messung, ist
offenbar unabhängig davon, wie frisch das Wissen ist.

Der Beutel ist jetzt drin. Im Original ist er keine Konserve, sondern eine Spritze: er heilt
**voll**, nicht um einen festen Betrag — auch bei angehobener Höchstgesundheit —, nimmt acht
schädliche Wirkungen samt der Verstrahlung weg, und die Übelkeit danach liegt fünfzehn
Sekunden statt fünf. Er lässt nichts zurück; im Original zählt der Stapel nur herunter. Dafür
darf die Hülle in `SyringeItem` jetzt `null` sein.

**Und das neue Tor hat es sofort noch einmal gefunden.** Beim Nachreichen des Beutels blieb im
Rezeptgeber ein Satz stehen, den ich eine Stunde zuvor selbst geschrieben hatte: „Die beiden
Bauplaene des Originals fuer med_bag stehen noch aus, weil es den Sanitaetsbeutel noch nicht
gibt." Das Tor hat ihn im selben Lauf gemeldet, in dem der Beutel dazukam — genau der Fall,
für den es gebaut ist, und diesmal vor dem Commit statt drei Runden später.

Dazu die beiden Baupläne aus `ConsumableRecipes` Z. 137 und 140 — einer mit Leder, einer mit
Kautschuk, beide im Original vorhanden — und der Vorratseintrag der C-130: ein Stück,
Gewicht 3. Von den sieben Einträgen, die dort als blockiert standen, sind noch fünf offen:
`definitelyfood`, `pill_iodine`, `canister_full`, `gun_henry`, `gun_n_i_4_n_i`.

Alle 34 Tore grün.

## Jodtablette und Feldration — und die Bilanz der sieben

Nach dem Sanitätsbeutel standen noch fünf Einträge der C-130 als blockiert da. Diesmal habe
ich nachgesehen, bevor ich etwas behauptet habe, und zwei davon waren ebenfalls keine Blockade:

* `definitelyfood` ist im Original ein `ItemLemon(3, 0.5F, false)` — drei Punkte,
  Sättigungsfaktor ein halb, sonst nichts. Kein Sondertext, keine Wirkung. Auf 1.21 sind das
  vier Zeilen `FoodProperties`.
* `pill_iodine` ist ein `ItemPill(0)`: eine Tablette, die nicht sättigt und sich deshalb immer
  schlucken lässt, und die dieselben neun Wirkungen wegnimmt wie der Sanitätsbeutel.

Damit steht diese Liste am Ende so da: **von den sieben Einträgen, mit denen sie anfing, waren
vier ein Irrtum und nur drei eine Blockade.** Übrig bleiben `canister_full` (braucht die
Flüssigkeitsbehälter), `gun_henry` und `gun_n_i_4_n_i` (beide eigene Sedna-Runden).

### Die Übelkeitssperre wirkt bei Tabletten anders

Die Spritze prüft vor dem Stechen, ob die Übelkeit schon anliegt, und tut nichts, wenn ja. Die
Tablette prüft **nicht** — sie legt die Sperre nur an. Man kann Tabletten also hintereinander
schlucken; sie sperren bloß die Spritzen aus. Das ist im Original genauso und sieht nach einem
Versehen aus, ist aber übernommen wie es dort steht.

### Eine Liste, die zweimal wortgleich dastand

Die neun Wirkungen, die Beutel und Tablette wegnehmen, stehen im Original zweimal Zeile für
Zeile identisch da — einmal in `ItemSyringe`, einmal in `ItemPill`. Hier stehen sie einmal, als
`NtmMobEffects.clearNegativeEffects`.

### Vier Baupläne, die ich beim ersten Mal übersehen hatte

Im Commit des Sanitätsbeutels steht, das Original habe „die beiden Baupläne aus
`ConsumableRecipes` Z. 137 und 140". Es hat **sechs**: je drei mit Leder und mit Kautschuk, und
in der Mitte steht entweder das Gegenmittel, die Jodtablette oder Radaway. Die beiden mit
Radaway waren die einzigen, die ich gesehen hatte, weil ich nach `med_bag` in der Nähe von
`radaway` gesucht hatte statt nach `med_bag`. Die vier übrigen sind jetzt da — zwei davon
hätten schon in der letzten Runde gehen können, das Gegenmittel liegt seit Langem im Port.

Dazu der Bauplan der Jodtablette (acht Stück aus Jod- und Fluoritstaub) und die beiden der
Feldration, die sich nur darin unterscheiden, ob ein Setzling oder drei Weizensamen hineingehen.

Alle 34 Tore grün.

## Der Henry und der Lincoln Repeater

Der vorletzte C-130-Eintrag, und der erste seit Langem, bei dem die Blockade **echt** war:
Modell, beide Texturen und der Schussklang lagen nur in der CE-Abspaltung, `ORCHESTRA_HENRY`
und die Animationen gab es im Port gar nicht. Eine Runde Arbeit also, keine Fehldiagnose.

Ein Unterhebelrepetierer auf .44, vierzehn Schuss im Röhrenmagazin. Er lädt **einzeln**
nach — daher die drei Nachladezustände `RELOAD`, `RELOAD_CYCLE` und `RELOAD_END` und die vier
Zahlen hinter `reload`, wo ein Kastenmagazin nur eine braucht. Sechs Teile des Modells bewegen
sich einzeln: Kimme, Hahn, Unterhebel, der Vorderschaft (beim Nachladen weggedreht), die
einzelne Patrone und der Rest.

**Der einzige Zweig, der etwas über den Zustand der Waffe wissen muss,** ist `RELOAD_END`: war
das Magazin vorher leer, schnappt der Hebel zum Schluss noch einmal durch und lädt die erste
Patrone; war noch etwas drin, sitzt sie längst im Lauf. Das steht so im Original und ist
übernommen.

Der Lincoln Repeater ist derselbe Bau als `B_SIDE`: doppelter Schaden, keine Streuung aus der
Hüfte, der Schuss ein Viertel höher. Beide teilen sich eine Rendererklasse und unterscheiden
sich nur in der Textur, die der Konstruktor bekommt — genau wie im Original.

### Kein Bauplan, und das ist kein Versäumnis dieser Runde

Der Henry bleibt in der Überlebensrunde unbaubar, und zwar wie **jede** Waffe im Port. Die
Baupläne des Originals stehen alle auf Waffenbauteilen — Läufe, Verschlüsse, Mechaniken,
Schäfte —, und diese Familie ist nicht portiert. Nachgemessen: `NtmRecipeProvider` nennt
**null** `NtmItems.GUN_`. Das ist eine eigene Stufe, keine Zeile in dieser Runde.

### Was von der C-130-Liste bleibt

Zwei Einträge: `canister_full`, das die Flüssigkeitsbehälter braucht, und `gun_n_i_4_n_i`, das
an `XFactoryAccelerator` hängt — eine ganze Fabrik, die der Port nicht hat. Von den sieben, mit
denen die Liste anfing, waren damit **vier ein Irrtum, einer eine Runde Arbeit und zwei eine
echte Blockade**.

Alle 34 Tore grün.

## Der Kanister — die fünfte Fehldiagnose derselben Liste

Der vorletzte Eintrag, und die Begründung, die ich ihm zwei Runden lang mitgegeben hatte
(„braucht die Flüssigkeitsbehälter"), war wieder zu groß. Nachgemessen:

* `CANISTER_EMPTY` liegt seit der Ölrunde im Port.
* `CD_Canister` — die Behälterangabe, die im Original entscheidet, welche Flüssigkeit in einen
  Kanister darf und welche Farbe der Aufdruck bekommt — ist vollständig da. Sie steht an
  Öl, Schweröl, Diesel und den übrigen, und `RenderCombustionEngine` liest sie schon aus.
* Und das ganze Muster für „ein Gegenstand je Flüssigkeit" steht seit dem Fluidfass: das
  Item (`FluidTankItem`, neunzehn Zeilen), das zweischichtige Modell, der Farbgeber, der
  Kreativreiter-Durchlauf und das Behälterregister.

Der Kanister ist also dasselbe wie das Fass, nur kleiner — tausend Millibar statt
sechzehntausend — und mit **zwei** Unterschieden, die beide aus dem Original stammen:

1. Er nimmt **nicht jede** Flüssigkeit, sondern nur die mit einem `CD_Canister`. Das Fass
   nimmt alles, was überhaupt in einen Behälter geht.
2. Er färbt sich **nicht nach der Flüssigkeit**, sondern nach der Farbe, die ihr
   `CD_Canister` trägt. Diesel ist fast weiß und der Kanister trotzdem rot.

### Ein Zugang, der privat war

`ItemPool.add(Supplier, min, max, weight)` stand seit der C-130-Runde als privater Zugang da
und wurde nur von der `ItemLike`-Fassung darüber benutzt. Der Kanister braucht beides — eine
Spielart (welche Flüssigkeit) **und** eine gewürfelte Stückzahl von eins bis vier. Jetzt ist
er offen.

### Die Bilanz der C-130-Liste

Sie fing mit sieben fehlenden Einträgen an. **Fünf davon waren ein Irrtum**: `radaway` lag nur
als Trankeffekt vor, `med_bag` hatte alle Zutaten, `pill_iodine` und `definitelyfood` sind
gewöhnliche Nahrung, und der Kanister brauchte nur das Muster des Fasses. **Einer**, der Henry,
war wirklich eine Runde Arbeit. **Bleibt einer**: `gun_n_i_4_n_i`, und der hängt an
`XFactoryAccelerator` — einer ganzen Fabrik, die der Port nicht hat.

Alle 34 Tore grün.

## Der Strahl feuerte ins Leere

Beim Nachmessen der NI4NI — dem letzten Eintrag der C-130-Liste — bin ich über etwas
gestolpert, das mit ihr nichts zu tun hat und schwerer wiegt: **das Strahlgeschoss des Ports
hat nie funktioniert.**

Nachgemessen, nicht geschlossen:

* `new BulletBeamBase(…)` steht im ganzen Baum **kein einziges Mal**.
* `Lego.shoot` behandelt nur `ProjectileType.BULLET`; der `BEAM`-Zweig war auskommentiert.
* Die Klasse selbst hatte Konfigurationszeiger, Schaden, Richtung und Länge — aber **keine
  Strahlverfolgung**. Das `performHitscan` des Originals, hundert Zeilen, fehlte ganz.

Betroffen sind alle drei `setBeam()`-Konfigurationen des Ports: die beiden der 35800 und
**der Schredder, den ich in dieser Sitzung selbst gebaut habe**. Sie haben Munition
verbraucht, Hülsen ausgeworfen, Rückstoß erzeugt — und nichts getroffen. Beim Schredder ist
mir das nicht aufgefallen, weil ich die Mechanik oberhalb des Geschosses geprüft habe und
darunter nicht weitergefragt.

### Was jetzt läuft

Der Schussweg. Ein Strahl fliegt nicht, er trifft sofort: der Konstruktor legt Ursprung und
Richtung fest und läuft die zweihundertfünfzig Blöcke noch in derselben Zeile ab. Was danach
in der Welt steht, ist nur noch die Zeichnung.

**Die beiden Wege sind nicht gleich**, und das ist der Teil, den man leicht falsch schreibt:
ein durchschlagender Strahl trifft *jedes* Wesen auf der Strecke und rechnet sofort ab; ein
gewöhnlicher merkt sich das nächste und rechnet erst am Ende. Die Länge geht an den Zeichner
und wird deshalb immer gesetzt, auch wenn nichts getroffen wurde.

### Was noch fehlt, und zwar gemessen

**Der Zeichner.** `ClientProxy` meldet für den Strahl keinen an, und `setRendererBeam` ruft im
ganzen Baum niemand auf. Der Strahl wirkt jetzt, aber man sieht ihn nicht. Das ist eine eigene
Runde.

**Der Knick an der Münze.** Er gehört zur NI4NI und steht bewusst nicht drin: die
Münzentität gibt es im Port nicht, und ein Zweig auf eine Entität, die fehlt, wäre toter Code.
`setRotationsFromVector` und `schussweg` stehen aber schon bereit — genau die beiden Methoden,
die der Knick braucht.

### Und damit zur NI4NI

Ihre Begründung in `ItemPoolsC130` war zum dritten Mal zu grob. Es fehlt **nicht** die Fabrik
`XFactoryAccelerator`, sondern die geworfene Münze: die ganze Waffe besteht daraus, dass ihr
Strahl an einer Münze in der Luft abknickt und sich das nächste Ziel sucht, mit einer
Rangfolge — Münze vor Spieler vor Monster vor allem anderen. Dafür braucht es die
Münzentität samt Zeichner und den Knick. Der Strahl selbst läuft seit dieser Runde.

Alle 34 Tore grün.

## Und jetzt sieht man ihn auch

Die vorige Runde hat den Strahl wirken lassen, aber unsichtbar. Der Zeichner fehlte an drei
Stellen gleichzeitig, und keine davon hätte allein gereicht:

* `ClientProxy` meldete für `BULLET_BEAM` keinen Entitätszeichner an.
* Die Klasse `RenderBeam`, die es im Original gibt, war nicht portiert.
* `setRendererBeam` rief im ganzen Baum niemand auf — die drei Strahlkonfigurationen hatten
  also auch keinen, an den der Zeichner hätte weiterreichen können.

Alle drei sind nachgetragen. `RenderBeam` zeichnet dabei selbst nichts: wie ein Strahl
aussieht, hängt an seiner Munition, nicht an seiner Entität, und die Klasse reicht genau wie
`RenderBulletMK4` an die Konfiguration weiter.

### Die Drehung ist eine andere als beim Geschoss

`renderBulletStandard` zeichnet entlang der X-Achse — das passt für ein Geschoss, das ohnehin
in Flugrichtung liegt. Ein Strahl steht dagegen in Weltwinkeln da und ist so lang, wie sein
Schussweg ausfiel. Er wird deshalb erst in die Senkrechte gedreht, um seine eigene Länge
verschoben und dann zurückgekippt; diese drei Schritte macht das Original genauso, und ohne
sie liegt der Strahl quer.

Über seine Lebensdauer zieht er sich zusammen: je älter, desto dünner. Die 35800 bekommt den
Riss in der Luft und ihre Schwarzlicht-Variante, der Schredder denselben Riss in seinem Grün.

Alle 34 Tore grün.

## Die NI4NI — und damit ist die C-130-Liste zu

Der letzte Eintrag, und die Waffe, deren Nachmessen die beiden Strahlrunden ausgelöst hat.

Sie hat unendlich Munition, keine Haltbarkeit und lädt sich **selbst mit Münzen**: alle achtzig
Ticks wächst eine nach, bis vier daliegen, und der Ton beim Nachwachsen steigt mit dem Vorrat.
Der Zweitdruck wirft eine in die Luft — aus Augenhöhe, mit vier Fünfteln der Blickrichtung und
einem halben Block Auftrieb, damit sie erst steigt und einen Moment lang still genug steht.

Und dann knickt der Strahl an ihr ab. **Die Rangfolge des neuen Ziels ist fest**: eine andere
Münze zuerst, dann ein Spieler, dann ein Monster, dann irgendetwas. Damit lässt sich eine Kette
aus mehreren Münzen bauen, und genau das ist der Witz der Waffe — sie schießt um die Ecke. Der
abgeknickte Strahl trägt ein Viertel mehr Schaden. Findet sich in fünfzig Blöcken gar nichts,
fällt er schräg nach unten ins Leere.

**Die Münze hat vor allem anderen Vorrang**, auch vor dem Durchschlagen: hinter ihr wird nicht
mehr abgerechnet, dort endet dieser Strahl und ein neuer beginnt.

### Die vier Münzen am Lauf sind die Anzeige

Sie erscheinen von hinten nach vorn, eine je geladener Münze, und schlagen von grün nach gelb
um, sobald der Vorrat über die Hälfte geht. Eine Zahl braucht die Waffe deshalb nicht — das HUD
zeigt statt eines Munitionsstands ein Unendlichkeitszeichen.

### Drei bewusste Auslassungen

**Die beiden Waffenmodule** des Originals (Nickel, Dublonen), die den Vorrat auf sechs und acht
anheben. Das Modulsystem des Ports kennt sie nicht; der Vorrat bleibt bei vier. Die Grenzen
fünf bis acht stehen im Zeichner trotzdem schon da — sie kosten nichts und stimmen sofort,
sobald die Module kommen.

**Die drei frei einstellbaren Farben.** Sie hängen an `ICustomizable` und einem Befehl, der die
Hexwerte entgegennimmt; beides gibt es im Port nicht, und damit auch die Graustufentextur
nicht.

**Die beiden anderen Waffen der Fabrik** — Tau-Kanone und Spulenkanone. Sie brauchen Munition,
die der Port nicht hat, und gehören in eine eigene Runde. `XFactoryAccelerator` steht deshalb
im Port da und enthält genau eine Waffe.

### Die Bilanz der sieben

Die C-130-Liste fing mit sieben angeblich blockierten Einträgen an:

* **Fünf waren ein Irrtum.** `radaway` lag nur als Trankeffekt vor, `med_bag` hatte alle
  Zutaten, `pill_iodine` und `definitelyfood` sind gewöhnliche Nahrung, und der Kanister
  brauchte nur das Muster des Fasses.
* **Zwei waren wirklich Arbeit.** Der Henry eine Runde, die NI4NI zweieinhalb — sie hat
  unterwegs ans Licht gebracht, dass das Strahlgeschoss des Ports überhaupt nie funktioniert
  hat.

Keiner der sieben war das, wofür ich ihn beim ersten Aufschreiben gehalten habe.

Alle 34 Tore grün.

## Die Waffenbauteile — und warum keine Waffe baubar war

In der Henry-Runde steht: „der Port hat noch **keine** einzige Waffe, die sich bauen lässt",
nachgemessen über `NtmRecipeProvider`, der null `NtmItems.GUN_` nennt. Das lag an sieben
fehlenden Gegenständen — Lauf leicht und schwer, Verschluss leicht und schwer, Mechanik,
Schaft, Griff. Jeder Waffenbauplan des Originals steht auf ihnen.

Was dabei herauskam, war angenehmer als erwartet: **fast alles drumherum lag schon da.**

* `MaterialShapes` kennt alle sieben Formen samt Mengen und Tagnamen — `LIGHTBARREL` sind drei
  Barren, ein `HEAVYRECEIVER` neun.
* `Mats` trägt an jedem Material vollständig, welche Bauteile es hergibt. Stahl kann leichte
  und schwere Läufe, aber nur leichte Verschlüsse; Holz kann Schaft und Griff, Elfenbein nur
  den Griff. Diese Liste war im Port komplett, lange bevor es die Gegenstände gab.
* `MoldSubtype` mit Auflöserfunktion existiert seit der Gussplatte.
* Alle sieben Texturen liegen in der CE-Abspaltung.

Es fehlten also wirklich nur die Gegenstände und ihre Verdrahtung.

### Ein Gegenstand je Bauteil, alle Materialien in den Metadaten

Dieselbe Bauart wie das Grundgesteinsbruchstück: die Zahl **ist** die Materialnummer aus
`Mats`, und welche Varianten es gibt, entscheidet nicht der Gegenstand, sondern das Material
über sein `setAutogen(...)`. Die Graustufenzeichnung bekommt die helle Farbe des Materials.

### Sie kommen aus der Gießform, nicht von der Werkbank

Formnummern 22 bis 28, wie im Original. Damit hat jedes der sieben Bauteile von der ersten
Minute an eine Quelle — kein Gegenstand ohne Weg dorthin.

**Warum kein Tag je Material:** die Tag-Erzeugung des Ports geht über *Namen*
(`ingot_steel` → `c:ingots/steel`), und ein Metagegenstand hat nur einen Namen für alle
Materialien. Für die Baupläne heißt das: sie greifen die Bauteile über
`DataComponentIngredient` mit der Materialnummer ab, nicht über einen Tag. Das Original löst
dasselbe über sein Erzwörterbuch; bei den Waffenbauplänen fällt der Unterschied nicht auf,
weil sie ohnehin ein bestimmtes Material nennen (`STEEL.lightBarrel()`), keine Gruppe.

Alle 34 Tore grün.

## Runde 182 — Die Waffenbaupläne, und eine Textur, die keiner gesucht hat

### Zuerst: die Bauteilrunde ist in der Abnahme durchgefallen

Der Commit der sieben Waffenbauteile kam nicht durch. Nicht am Java — `runData` brach ab:

    Texture hbmsntm:item/mold_barrel_light does not exist in any known resource pack

Sieben neue Gießformen, sieben Bilder für die *Bauteile* kopiert, sieben Bilder für die
*Formen* vergessen. Beide Sätze liegen in der CE-Abspaltung nebeneinander; ich hatte nur den
einen gesehen.

**Das eigentliche Versäumnis ist nicht die Textur, sondern das Tor.** `metatex-check.sh` prüft
Metagegenstände, deren Bildname sich aus dem `layer0`-Ausdruck ableiten lässt. Bei `MoldItem`
steht der Name als Zeichenkette im `registerMold`-Aufruf, nicht in einer Aufzählung — das Tor
konnte ihn nicht auflösen und hat die Klasse (korrekt, aber folgenlos) unter „nicht geprüft"
aufgeführt. Ein Tor, das an einer Stelle bewusst wegschaut, ist genau dort blind.

Jetzt hat es dafür eine eigene Regel: die Formnamen werden direkt aus den `registerMold`-
Aufrufen gelesen und `item/mold_<name>.png` wird geprüft, samt `mold_base.png`. Gegengemessen
in beide Richtungen — mit entferntem `mold_grip.png` meldet es genau diese Datei und geht mit
Rückgabewert 1.

### Und dann die Baupläne

Vor dieser Runde: **null** Waffenbaupläne im Port. Gemessen, nicht geschätzt —
`NtmRecipeProvider` nannte kein einziges `NtmItems.GUN_`.

Von den 46 Bauplänen aus `WeaponRecipes` sind **28** übernommen. Der Rest fällt aus zwei
gemessenen Gründen weg, und beide stehen als Liste im Quelltext:

* **Die Waffe gibt es nicht** (16 Stück): Flammenwerfer und Topaz, Stinger, Chemiewerfer,
  Quadro, LAG, Raketenwerfer, Teslakanone, Laserpistole und Pew Pew, Fat Man, Tau,
  Lasergewehr, Ladungswerfer, Bohrer, die beiden Panzerrüstungswaffen.
* **Die Zutat gibt es nicht** (2 Stück): die Minigun braucht `motor_desh`, den Deshmotor;
  der Heilige Drache braucht `item_secret` in der Ausführung Selenstahl. Beide Waffen sind da,
  beide Zutaten nicht.

Dazu die zwölf Bauteilrezepte von Hand — Schaft und Griff aus Brettern, Polymer, Bakelit,
Polycarbonat, PVC, Gummi und Knochen. Die **metallenen** Bauteile haben auch im Original
bewusst kein Werkbankrezept; sie kommen aus der Gießform.

### Drei alte Rezepte waren stillschweigend umgeschrieben

Schalldämpfer, Säge und Saturnit-Gehäuse standen seit Runde 74 im Port — aber mit
ausgetauschten Zutaten, weil es die Waffenbauteile nicht gab: ein Stahlrohr statt des leichten
Laufs, Stahlplatten statt Bolzen, Barren statt Lauf und Verschluss. Nirgends stand, dass das
Absicht war. Diese Runde stellt die Muster des Originals her.

Beim Schalldämpfer kam ein zweiter Fehler mit heraus: er verlangte Polycarbonat, wo das
Original `AnyPlastic` sagt — und `AnyPlastic` ist **Polymer und Bakelit**. Polycarbonat gehört
zu `AnyHardPlastic`. Das war schlicht der falsche Kunststoff.

Zehn weitere Sonderaufsätze sind neu (Zielfernrohr, Schnellader, Bremse, Beschleuniger,
Grease-Gun-Schaft, Würgebohrung, die beiden Schaftsätze, Doppelmagazin, Seitengewehr), und
die letzten vier Paare der Allgemeinaufsätze. Bei denen ist die Begründung übrigens
zweigeteilt und steht so im Quelltext: die vier `DAMAGE`-Stufen hingen wirklich an der
Mechanik, die vier `DURA`-Stufen brauchten nur Platten — die standen ohne Grund noch aus.

**Nicht übernommen, weil die Waffe dazu fehlt:** `LAS_*` (Lasergewehr), `DRILL_*`, `ENGINE_*`,
`MAGNET`, `SIFTER`, `CANISTERS` (Bohrer). Ihre Zutaten wären großenteils da, aber es gäbe im
Port keine Waffe, an die sie passen — ein Aufsatz ohne Waffe ist ein Gegenstand ohne Wirkung.

### Die Sammelbegriffe

`AnyPlastic`, `AnyHardPlastic`, `AnyResistantAlloy` und `AnyBismoidBronze` sind im Original
Einträge im Erzwörterbuch, die mehrere Materialien zugleich annehmen. Im Port werden daraus
zusammengesetzte Zutaten (`CompoundIngredient`) über die jeweiligen Metadaten — derselbe Sinn,
ohne Erzwörterbuch.

Alle 34 Tore grün.

## Runde 183 — Der Deshmotor, und damit der 29. Bauplan

In der Bauplanrunde stand als gemessener Grund: *„gun_minigun braucht `motor_desh`, den
Deshmotor. Den kennt der Port nicht."* Das war richtig — und ein Gegenstand, der aus vier
vorhandenen Zutaten besteht.

`motor_desh` ist im Original ein schlichter `Item` ohne eigene Klasse. Sein Werkbankmuster
(`CraftingManager` Z. 174) ist `PCP`/`DMD`/`PCP` aus Kunststoff, dichtem Golddraht,
Deshbarren und einem gewöhnlichen Motor; dazu gibt es ein Ambossrezept der Stufe 3. Alle vier
Zutaten hat der Port seit Langem. Es fehlte nur der Motor selbst.

Damit ist die Minigun baubar — Bauplan 29 von 46. Übrig bleibt aus dieser Gruppe nur noch der
Heilige Drache, und dessen Zutat ist eine ganze Familie (`item_secret`), keine Einzelheit.

**Eine Abweichung, benannt:** das Ambossrezept nennt im Original `AnyPlastic`, also Polymer
*oder* Bakelit. Die Ambossrezepte des Ports arbeiten mit `ComparableStack`, und der kann nur
einen Gegenstand nennen — dort steht deshalb Polymer. Das Werkbankrezept kann beides und
nennt auch beides.

Alle 34 Tore grün.

## Runde 184 — Die Flammenwerfer, und Munition, die niemand verschießen konnte

Der Port kannte seit Langem vier Brennstoffe — Diesel, Gas, Napalm, Bannfeuer — als fertige
Munitionsgegenstände. Keine einzige Waffe konnte sie verschießen. Das ist der umgekehrte Fall
von „Wirkung ohne Ursache": hier lag die Ursache herum, und die Wirkung fehlte.

`XFactoryFlamer` ist eine von **sechs** Waffenfabriken des Originals, die der Port nicht hat.
Gemessen, nicht geschätzt — die anderen fünf sind `XFactoryDrill`, `XFactoryEnergy`,
`XFactoryFolly`, `XFactoryPA` und `XFactoryTool`.

### Wie ein Flammenwerfer in diesem System arbeitet

Er verschießt gewöhnliche Geschosse, nur sehr viele, sehr langsame und sehr kurzlebige. Jedes
zieht auf seinem Weg eine Flamme hinter sich her, zündet an, was es trifft, und lässt dort, wo
es auf einen Block schlägt, eine Lache stehenden Feuers zurück. Gas lässt nichts stehen,
Bannfeuer brennt immer.

### Der Schütze war bisher gegen sich selbst nicht geschützt

Die Geschosse des Ports kannten keine Sperre gegen den eigenen Schützen — `canHitEntity` fragte
nur, ob das Ziel getroffen werden *kann*. Bei schnellen Geschossen fällt das nicht auf; ein
Flammenwerfer, dessen Flammen eine Handbreit vor dem Gesicht entstehen, hätte den Spieler bei
jedem Zug selbst angezündet.

Das Original löst das mit `selfDamageDelay` — zwei Ticks für gewöhnliche Geschosse, zwanzig für
Flammen. Diese Zahl gibt es jetzt auch im Port, und `canHitEntity` hält sich daran. Die
Voreinstellung ist dieselbe wie im Original, die Änderung bringt den Port also näher heran,
statt ihn zu verstellen.

### Was mitkommt und was nicht

Neu: die drei Waffen, ihr Modell, ihre drei Texturen, die Nachladeanimation, der Dauerton des
Strahls und das Ventilgeräusch beim Nachladen. Flammenwerfer und Mister Topaz haben ihre
Baupläne aus `WeaponRecipes` — damit sind es **31 von 46**.

**Nicht übernommen:** der Chemiewerfer aus derselben Fabrik. Er schießt keine Geschosse,
sondern Flüssigkeit aus einem Tank, und braucht dafür `MagazineFluid` und eine eigene
Waffenklasse. Ebenso `flame_nograv` und `flame_nograv_bf`: das Original legt sie hier an,
benutzt sie hier aber nicht — sie gehören zum Chemiewerfer und zur Panzerrüstung.

**Der Daybreaker bleibt ohne Fundort.** Im Original kommt er aus dem Sockel
(`PedestalRecipes`); den gibt es im Port nicht. Die Waffe ist fertig, ihr Weg zum Spieler fehlt
— derselbe Stand wie beim Lilmac und beim Protégé.

Alle 34 Tore grün.

## Runde 185 — Die Hülsenkette: eine Maschine ohne Rohstoff

Beim Nachtragen der letzten Einträge aus `WeaponRecipes` stellte sich heraus, dass die drei
abgeleiteten Hülsen (Schrotpatrone, Schrot, Schrot verstärkt) auf einer Zutat stehen, die es im
Port **nirgends** gibt.

Die Kette rückwärts durchgemessen:

* `AmmoPressRecipes` **verbraucht** große und kleine Hülsen — über hundert Rezepte.
* `PressRecipes` hatte **kein einziges** Hülsenrezept. Im Original sind es vier
  (`PressRecipes` Z. 88–91).
* Die vier Kaliber-Prägestempel standen in `NtmItems` **auskommentiert**.

Also: die Munitionspresse, die ganze Patronenherstellung des Ports, hatte keinen Rohstoff. Der
einzige Weg an Hülsen wäre der Kreativreiter gewesen. Aufgefallen ist das nicht beim Bau der
Presse, sondern erst, als ein Werkbankrezept eine Hülse als *Zutat* verlangte.

**Jetzt geschlossen:** die Stempel `stamp_9`, `stamp_50` und ihre Desh-Ausführungen samt
Amboss-Bauplänen (Stufe 2 aus Gunmetal, Stufe 4 aus Waffenstahl), die vier Pressrezepte und die
drei Umbauten an der Werkbank. Die Bilder aller acht Stempel lagen längst im Baum.

**Nicht übernommen:** `stamp_357` und `stamp_44` samt Desh-Ausführungen. Es gibt sie im
Original, aber kein Pressrezept verlangt `C357` oder `C44` — nachgemessen in `PressRecipes` des
Originals, das nur `C9` und `C50` kennt. Es wären Gegenstände ohne Wirkung.

Dazu der Rest von `WeaponRecipes`: die vier Steinzeitpatronen (Kopfsteinpflaster, Feuerstein,
Kies, Eisen — je sechs Schuss) und die Treibladung des Katapults. Damit ist `WeaponRecipes`
**vollständig abgearbeitet**, bis auf das, was in den Runden 182 und 184 mit Grund
stehengelassen wurde.

Alle 34 Tore grün.

## Runde 186 — Der Bohrer, und drei Aufsätze, die nichts taten

### Ein Werkzeug, gebaut wie eine Waffe

`XFactoryDrill` ist die vierte der sechs fehlenden Waffenfabriken. Der Bohrer verschießt
nichts: sein „Schuss" ist ein Griff ins Leere vor dem Spieler. Trifft er ein Wesen, macht er
Schaden; trifft er einen Block, bricht er ihn und die sechsundzwanzig Nachbarn heraus — außer
man schleicht. Sein Magazin ist ein Kraftstofftank, gefüllt wird an der Zapfsäule.

**Die 1.21-Frage, an der die Runde beim ersten Anlauf hängengeblieben war:** `getHarvestLevel`
und `canHarvestBlock` sind keine Item-Methoden mehr. Die Entsprechungen sind
`Item.isCorrectToolForDrops` (der Bohrer gibt immer wahr zurück, wie im Original) und, für das
Brechen selbst, `ServerPlayerGameMode.destroyBlock` — so greifen Beute, Werkzeugverschleiß und
Schutzbereiche genauso wie bei einer Spitzhacke.

Die Bruchwolke verschickt das Original in zwei verschiedenen Paketen. In 1.21 verschickt
`destroyBlock` das Ereignis 2001 schon an alle **außer** den Brechenden; ihm wird es hier
einzeln nachgereicht. Ein Paket statt zweier, dasselbe Bild.

### Und ein Fehler aus Runde 182

Die Aufsatzrunde hat zehn Sonderaufsätze nachgereicht. Drei davon — Schnellader, Würgebohrung,
Doppelmagazin — **stehen nicht im `XWeaponModManager`**. Ein Aufsatz ohne Eintrag dort lässt
sich bauen, lässt sich anbringen und tut nichts. Drei Baupläne für drei Attrappen.

Nachgereicht: `WeaponModLiberatorSpeedloader` (tauscht beim Liberator das Einzelnachladen gegen
einen Wechsel am Stück, samt eigener Bewegung), `WeaponModChoke` (halbiert die Streuung der
Schrotladung) und `WeaponModStackMag` (anderthalbfache Kapazität).

Damit stimmt auch die Begründungsliste im Quelltext wieder: die übrigen nicht übernommenen
Aufsätze fehlen **nicht**, weil die Waffe fehlt — sondern weil der Aufsatz selbst nicht
angemeldet ist. Beim Bohrer war der alte Grund seit dieser Runde ohnehin falsch.

**32 von 46 Bauplänen.**

Alle 34 Tore grün.

## Runde 187 — Die Laserfamilie

Die fünfte der sechs fehlenden Waffenfabriken, zum größeren Teil. Vier Waffen: Laserpistole,
Pew Pew, Morning Glory und Lasergewehr.

**Wieder Munition ohne Waffe.** Die drei Kondensatoren — gewöhnlich, überladen, Brand — lagen
seit Langem als Gegenstände im Port, genau wie die vier Flammenwerferbrennstoffe vor Runde
184. Keine Waffe konnte sie verschießen.

Alle vier schießen Strahlen, nicht Geschosse — dasselbe Hitscan-System, das die 35800 und die
NI4NI benutzen und das vor Runde 183 überhaupt nicht funktionierte. Der Brandstrahl zündet an,
was er trifft; wo weder ein Wesen noch ein brennbarer Block getroffen wird, bleibt eine Lache
stehenden Feuers.

**Nicht in dieser Runde: die Teslakanone** aus derselben Fabrik. Ihr Einschlag ist eine
Explosion mit eigenem Spielerverarbeiter (`PlayerProcessorStandard`) und einem Partikelpaket —
beides fehlt im Port —, dazu kommt ihr Gurtmagazin. Ihre drei Strahlkonfigurationen stehen
bereit, sobald das nachgezogen ist.

**Zwei benannte Abweichungen:**

* Das Original zeichnet den Strahl mit `BeamPronter` als gewellten Schlauch. Den gibt es im
  Port nicht; die Laser nehmen denselben Weg wie alle übrigen Strahlen — dunkler Kern, heller
  Saum —, in den Farben des Originals.
* Die beiden Aufsatzteile am Lasergewehr (Schrotlauf, Unterlaufkondensator) sind nicht
  übernommen: die Aufsätze `LAS_SHOTGUN` und `LAS_CAPACITOR` sind im `XWeaponModManager` nicht
  angemeldet, ein Modellteil das nie erscheint wäre totes Gewicht.

**35 von 46 Bauplänen.**

Alle 34 Tore grün.

## Runde 188 — Der Chemiewerfer und die Chemikalienwolke

Damit ist `XFactoryFlamer` vollständig. Der Chemiewerfer ist die eigenartigste Waffe des
Systems: er verschießt keine Munition, sondern den Inhalt seines Tanks — und **was dabei
herauskommt, entscheidet nicht die Waffe, sondern das Fluid**.

Das Herzstück ist `Chemical`, die Wolke selbst. Sie liest die Eigenschaften des Fluids und
wird danach zu einem von sechs Dingen: Gammastrahl (Antimaterie), Blitz (Iongel), Spritzer,
Gasschwall, Stichflamme oder brennende Flüssigkeit. Daran hängen Flugbahn, Lebensdauer,
Luftwiderstand und Wirkung. Säure ätzt die Rüstung, strahlende Fluide verseuchen Getroffenen
*und* Boden, brennbare Flüssigkeit tränkt statt anzuzünden, Saatbrühe macht aus totem Boden
wieder Gras.

**Drei benannte Auslassungen:**

* Der Glyphid-Zweig des Pheromons — Glyphiden gibt es im Port nicht. Die Wirkung auf Spieler
  und alle übrigen Lebewesen bleibt.
* Die Blockumwandlungen der Saatbrühe für Stufen und Platten: sie hängen im Original an
  Metadaten, die es in 1.21 nicht mehr gibt. Erde, Kopfstein, Steinziegel, Brachland und
  Betonziegel werden umgewandelt wie dort.
* Der Strahl für Antimaterie und Iongel im Zeichner — dasselbe fehlende `BeamPronter`-
  Gegenstück wie bei den Lasern; beide Sorten sind ohnehin nur über den Kreativreiter in
  einen Tank zu bekommen.

**Kein Bauplan, und das ist keine Nachlässigkeit:** das Original verlangt ein Gummirohr und
einen Schraubenschlüssel. `RUBBER` trägt zwar `PIPE` in seinem `autogen`, aber ein
`pipes_rubber` gibt es im Port nicht — und einen `wrench` ebenso wenig. Zwei Ersatzzutaten
wären kein Port mehr, sondern ein eigener Bauplan.

### Ein Torloch, das die Abnahme gefunden hat

Der erste Anlauf dieser Runde fiel durch, und zwar an einer Zeile, die im Original
selbstverständlich war:

    error: getType() in Chemical cannot override getType() in Entity

Die Wolke hieß im Original `EntityChemical` und hatte ein `getType()`, das den Fluidtyp
zurückgab. In 1.21 hat schon `Entity` ein `getType()`, und das liefert `EntityType`.

**Kein bestehendes Tor konnte das sehen.** `signature-check.sh` vergleicht nur Methoden *mit*
`@Override` gegen die Mehrheit im Projekt — hier stand keines, und es sollte auch keines
stehen. `syntax-check.sh` übersetzt ohne Minecraft-Klassenpfad und kennt die Oberklasse gar
nicht.

Dafür gibt es jetzt ein **35. Tor**: `vanilla-name-check.sh` verfolgt die Vererbungskette im
Baum selbst bis zu einer bekannten Vanilla-Wurzel und prüft dort fest belegte Methodennamen
gegen ihren Rückgabetyp. Die Liste ist bewusst kurz — sie enthält nur, was nachweislich schon
schiefging, und wächst mit jedem weiteren Fall. Gegengemessen in beide Richtungen: benennt man
`getFluidType` zurück in `getType`, meldet es genau diese Zeile und sonst nichts.

Alle 35 Tore grün.


## Runde 189 — Die Teslakanone, und vier Dinge, die es längst gab

Die Teslakanone schließt `XFactoryEnergy` ab. Sie ist die einzige Waffe des Ports, die **kein
Magazin hat**: `MagazineBelt` frisst unmittelbar aus dem Rucksack, und welche der drei
Kondensatorarten gerade läuft, entscheidet sich Schuss für Schuss neu. Wer die Munition
wechseln will, wirft die alte weg.

Das sieht man ihr an. Der Bus `COUNT` ihrer Bewegungsvorschrift trägt keine Bewegung, sondern
eine **Zahl** — wie viele Kondensatoren im Rucksack liegen —, und der Zeichner steckt so viele
auf das Zahnrad, höchstens acht. Beim Schuss dreht sich das Zahnrad um 22,5 Grad weiter, und
die aufgesteckten Kondensatoren mit. Obendrauf sitzt ein Yomi-Plüschtier, das beim Begutachten
quietscht.

Der gewöhnliche und der überladene Kondensator schlagen als kleine Explosion ein. Der
**Brandkondensator** bleibt im getroffenen Wesen stecken und springt von dort auf *jedes*
Lebewesen im Umkreis von zwanzig Blöcken weiter — jeder Sprung ein eigener Strahl mit halbem
Schaden, der wiederum trifft.

### Beim Bauen fiel auf, dass drei Bausteine schon im Baum lagen

Die Runde davor hatte die Teslakanone mit der Begründung zurückgestellt, ihr fehlten
Spielerverarbeiter und Partikelpaket. **Beides stimmte nicht.**

**Erstens der Spielerverarbeiter.** `IPlayerProcessor` lag seit jeher im Baum, aber
`ExplosionVNT.explode()` hat ihn nie aufgerufen: es sammelte die getroffenen Spieler ein und
ließ sie dann liegen. Damit hat **keine Explosion dieser Bauart je einen Spieler
zurückgeworfen** — nicht weil der Spieler anders behandelt worden wäre, sondern weil sein
Rechner die vom Server gesetzte Geschwindigkeit verwirft, wenn sie ihm niemand schickt.

Nachgemessen: das Original setzt den Verarbeiter an **55 seiner 65** Explosionen. Im Port sind
es jetzt **30** — überall dort, wo die Explosion eine Entsprechung im Original hat, die ihn
setzt. Die zehn Ausnahmen des Originals (Semtex, Sprengknete, Förderwagen, Flugzeug, Torpedo,
die beiden Abläufe, der Fluidtank) bleiben auch hier ohne. Betroffen sind unter anderem **alle
Sprenggeschosse** (`Lego.standardExplode`/`tinyExplode`), alle Granatenfüllungen, alle Minen,
die Raketen und der Orbitallaser.

*Abweichung:* das Original verschickt dafür ein eigenes Paket (`ExplosionKnockbackPacket`). In
1.21 tut es das Bordmittel `ClientboundSetEntityMotionPacket`.

**Zweitens das Partikelpaket.** `PlasmaBlastParticle` war vollständig geschrieben, samt
Textur — und **niemand hat je eine erzeugt**: kein Verteilereintrag, kein Aufrufer. An drei
Stellen stand im Kommentar, diese Partikelart habe der Port nicht. Das war nie wahr. Mit
`PlasmaBlastCreator` hängt sie jetzt am Verteiler und wird an allen vier Stellen des Originals
gezogen: Teslaeinschlag, EMP- und Plasmagranate, Schredderstrahl und Schreddersplitter.

**Drittens der Ufo-Schlag.** Der Ton kam mit der Teslakanone; die Energiegranaten hatten ihn
im Original schon immer und im Port bis hierher nicht.

### Zwei erfundene Bewegungsvorschriften, berichtigt

`LAMBDA_LASER_PISTOL` und `LAMBDA_LASRIFLE` waren in Runde 187 **nicht übertragen, sondern
erfunden**. Sie sahen plausibel aus und waren falsch: die Laserpistole klappt beim Nachladen
einen Riegel auf, hebt den Lauf, rüttelt die Batterie heraus und schiebt eine neue ein
(`LATCH`/`LIFT`/`JOLT`/`BATTERY`); das Lasergewehr arbeitet mit Hebel und Magazin
(`LEVER`/`MAG`/`EQUIP`). Beiden fehlten außerdem die Zweige für Klemmen und Begutachten ganz.

Das ist jetzt Zeile für Zeile das Original — und weil die Bewegungen fehlten, fehlten auch die
dazugehörigen Töne: `ORCHESTRA_LASER_PISTOL` bekommt seinen `JAMMED`-Block, `ORCHESTRA_LASRIFLE`
seine `INSPECT`- und `JAMMED`-Blöcke.

### Die acht Werkstofflisten, nachgemessen

`XWeaponModManager` ordnet jeder Waffe zu, welcher Werkstoffaufsatz an sie passt. Diese Listen
waren seit Runde 118 nicht mehr mit dem gewachsenen Waffenbestand abgeglichen worden.
**Vierzehn Waffen, die es im Port längst gibt, standen in keiner** — an ihnen ließ sich kein
Aufsatz anbringen, obwohl das Original ihn vorsieht: die beiden Henry, die Leuchtpistole, der
Liberator, der Congo Lake, die beiden ersten Flammenwerfer, der schwere Revolver, die
Panzerschreck, die MK 108, der Chemiewerfer, die beiden Flinten und die beiden Laserpistolen.

Der schwerste Fall war die **Bronzeliste: sie fehlte ganz.** `BRONZE_DAMAGE` und `BRONZE_DURA`
hatten damit überhaupt keinen Eintrag — zwei Aufsätze mit Rezept, die sich bauen ließen und an
keiner einzigen Waffe etwas taten. Genau der Fehler, der in Runde 187 schon einmal bei
`SPEEDLOADER`, `CHOKE` und `STACK_MAG` stand.

Nicht dabei, weil die Waffe im Port fehlt: `gun_quadro`, `gun_lag`, `gun_missile_launcher`,
`gun_fatman`, `gun_tau`.

### Abweichungen

* Der Blitz wird gezeichnet wie jeder andere Strahl hier — dunkler Kern, heller Saum. Das
  Original legt drei gewellte Schläuche übereinander (`BeamPronter`, `EnumWaveType.RANDOM`);
  den gibt es im Port nicht. Die Kernfarbe ist die des Originals, der Saum nimmt die Farbe,
  die derselbe Einschlag seinen Schockfächern mitgibt.
* `setupModTable` ist nicht übernommen — der Waffentisch des Ports zeigt statt der Waffe ihr
  Gegenstandsbild, wie bei allen übrigen Waffen.

**36 von 46 Bauplänen.** Der Bauplan der Teslakanone ist wortgetreu der des Originals: drei
Kupferspulen als Kranz, Lauf und Verschluss aus Technetiumstahl oder Chromdioxid, darunter
Mechanik, Griff und die Militärplatine.

Alle 35 Tore grün.

## Runde 190 — Die Folly, und noch eine Behauptung, die nicht stimmte

`XFactoryFolly` stand in der Restliste mit der Begründung, ihr fehlten
`EntityNukeExplosionMK5` und `EntityNukeTorex`. **Nachgemessen: beides ist da** —
`com.hbm.entity.logic.NukeExplosionMK5` mit `statFac`, und `NukeTorexCreator` mit
`statFacStandard`. Ebenso vorhanden waren `AmmoSecret.FOLLY_SM`/`FOLLY_NUKE` samt Texturen,
`setChunkloading`, `setSpectral`, `Crosshair.NONE`, `GunAnimation.SPINUP`, `GunConfig.pt`,
`Receiver.dryfire`, `WeaponQuality.SECRET` und `MagazineSingleReload`. Es fehlte nichts außer
der Fabrik selbst.

Das ist die zweite Runde in Folge, in der eine Zurückstellung sich als Irrtum erweist. Beide
Male lag die Ursache im selben: die Begründung wurde beim Schreiben geprüft und danach nie
wieder, während der Port darunter weiterwuchs.

### Der SM-Strahl ist kein Strahl

Er tastet nicht ab wie die Laser, er **fährt**: ein spektrales Geschoss mit Schwerkraft, das im
zweiten Tick über seine volle Länge eine drei mal drei Blöcke dicke Röhre aus der Welt löscht
und alles darin erschlägt (100 Punkte Durchschlag, 99 % Panzerbruch). Deshalb steht die ganze
Wirkung in `setOnUpdate` und nicht in einem Einschlaghaken. Der Schütze zahlt mit 150 Punkten
Strahlung — dieselbe Dosis, die das Original verlangt.

Bis zum fünfzigsten Tick wandert dabei ein Schockfächer die Flugbahn entlang und wird immer
größer. Genau der Fächer, der in Runde 189 erst erzeugbar geworden ist.

Der Atomkopf ist dagegen schlicht: Kernexplosion der Stärke 100 samt Pilz.

### Zielen ist ein eigener Zustand

Die Folly schießt nicht aus der Hüfte. Die mittlere Maustaste schaltet das Zielen um, dabei
läuft die `SPINUP`-Bewegung an — und `LAMBDA_CAN_FIRE` verlangt, dass sie die letzte Bewegung
war und mindestens **hundert Ticks** läuft. Vorher passiert gar nichts.

Was in diesen fünf Sekunden geschieht, ist der eigentliche Witz der Waffe: auf dem Visier
fährt ein Rechner hoch. Erst ein Selbsttest (`POST successful - Code 0`, `8,388,608 bytes of
RAM installed`, `No keyboard found!`), dann zeichnet sich `VStarOS` Buchstabe für Buchstabe
auf, und ab der fünften Sekunde zeigt das Gerät laufend Ziel und Winkel an. Das Fadenkreuz ist
ebenfalls Schrift: ein `+`, oder `No ammo`.

### Woher sie kommt — und woher nicht

Im Original stammen Waffe und Munition ausschließlich aus dem **roten Raum**
(`PedestalRecipes`, Vollmond-Bedingung). Den hat der Port nicht, also ist die Folly wie die
übrigen Geheimwaffen des Ports (Aberrator, NI4NI) vorerst nur über den Kreativreiter zu
bekommen. Das ist keine neue Lücke, sondern dieselbe, die der rote Raum überall reißt.

### Abweichungen

* **Der Jingle beim Hochfahren ist nicht übernommen.** Das Original spielt dazu einen eigenen
  Tonschnipsel; die Datei liegt weder im Original noch in der CE-Abspaltung, das Feld zeigt
  dort auf einen Namen ohne Datei.
* Die Modelltextur heißt im Original `moonlight.png`. Der Port benennt sie nach der Waffe, wie
  alle übrigen Waffentexturen.
* `setupModTable` ist nicht übernommen — der Waffentisch des Ports zeigt statt der Waffe ihr
  Gegenstandsbild.

Alle 35 Tore grün.

## Runde 191 — Die elf Aufsätze des Bohrers, und ein Rohr, das es längst gab

Der Bohrer kam in Runde 186 mit vier Haken für Aufsätze — `D_REACH`, `F_DTNEG`, `F_PIERCE`,
`I_AOE` — und liest sie alle vier beim Abbauen aus. **Nur hatte keiner von ihnen einen
Schreiber.** Die elf Aufsätze, die im Original an den Bohrer gehören, standen zwar in der
Aufzählung `ModSpecial`, hatten aber keine Klasse: vier Bohrköpfe, vier Motoren, Magnet, Sieb
und Kanister. Ein Haken ohne Schreiber ist dasselbe wie ein Aufsatz ohne Wirkung, nur von der
anderen Seite.

**Die vier Bohrköpfe** (`WeaponModDrill`) fassen als einzige alles an: Schaden als Faktor,
Reichweite als Faktor, Panzerbruch und Kantenlänge als feste Werte. Je fester das Metall,
desto mehr von allem — vom Schnellarbeitsstahl (1,25× Schaden) bis zum Saturnit (3× Schaden,
doppelte Reichweite, 3×3-Würfel).

**Die vier Motoren** (`WeaponModEngine`) tauschen das Magazin aus und bestimmen damit
zugleich, welchen Kraftstoff das Gerät frisst und wie schnell es arbeitet: Diesel, Kerosin,
Strom, Reformat. Der Elektromotor ist der einzige, der gar kein Fluid mehr nimmt — an ihm
hängt ein Akku.

**Magnet und Sieb** (`WeaponModDrillFortune`) greifen in keinen Waffenwert ein, sondern
schreiben eine Glücksverzauberung in den Gegenstand: zwei Stufen der Magnet, eine das Sieb.
**Die Kanister** (`WeaponModCanisters`) verdreifachen den Tank.

### Drei Dinge, die nebenher fällig wurden

**Der Akku des Bohrers.** In `GunDrillItem` stand: *"NICHT ÜBERNOMMEN: die Batteriehälfte …
diesen Aufsatz gibt es im Port nicht, und `MagazineElectricEngine` ebenso wenig — die Hälfte
wäre Code ohne Wirkung."* Das stimmte, solange der Elektromotor fehlte. Jetzt gibt es ihn,
also gibt es auch `MagazineElectricEngine` und die Batteriehälfte: der Bohrer ist ein
`IBatteryItem`, wenn der Elektromotor steckt, und ein `IFillableItem`, wenn ein
Verbrennungsmotor steckt. Beide Hälften fragen dasselbe Magazin ab und geben null zurück,
wenn gerade die andere Sorte darin ist.

**`I_HARVEST` war eine tote Konstante.** Das Original hebt mit dem Bohrkopf an, was der Bohrer
überhaupt abbauen darf. In 1.21 gibt es keine Abbaustufe als Zahl mehr, und
`GunDrillItem.isCorrectToolForDrops` gibt ohnehin stets wahr zurück — der Bohrer bricht alles.
Die Kennung stand seit Runde 186 im Baum, ohne dass sie irgendwer gelesen hätte. Sie ist
entfernt, mit Begründung an ihrer Stelle.

**Ein Rohr, das es längst gab.** Beim Schreiben des Kanister-Bauplans fiel auf, dass die
Begründung für den fehlenden Chemiewerfer-Bauplan zur Hälfte falsch war: dort stand, es fehle
ein Gummirohr *und* ein Schraubenschlüssel. `pipe_rubber` ist aber angemeldet, hat ein
Ambossrezept, liegt im Kreativreiter und wird von der Montagefabrik verarbeitet. Es fehlt nur
noch der Schraubenschlüssel — `ToolType.WRENCH` steht in der Aufzählung, ein Gegenstand dazu
nicht. Das ist die **dritte** Runde in Folge, in der sich eine Begründung als veraltet
erweist.

### Eine benannte API-Abweichung

`onInstall` und `onUninstall` bekommen die **Welt** mitgereicht, anders als im Original. Der
Grund ist zwingend: in 1.21 sind Verzauberungen datengetrieben und nur über die Registry der
laufenden Welt zu bekommen. Ohne diesen Parameter könnte `WeaponModDrillFortune` gar nichts
tun. Der Weg dorthin ist kurz — der Waffentisch kennt seinen Spieler und damit seine Welt.

### Abweichung im Bauplan

`ANY_HARDPLASTIC` ist im Original `{Polycarbonat, PVC}`. Einen Polycarbonat-**Barren** gibt es
im Port nicht (`MAT_HARDPLASTIC` trägt nur `STOCK` und `GRIP` in seinem `autogen`), also bleibt
für den Saturnit-Bohrkopf PVC — das im Original ebenso zulässig ist. Kein Ersatz, sondern die
kleinere von zwei zulässigen Möglichkeiten.

**24 Aufsätze mit Wirkung** (13 + die elf dieser Runde). Alle 35 Tore grün.

### Nachtrag zu Runde 191 — der CI-Fehlschlag und das Loch, das ihn durchließ

Der erste Anlauf fiel durch, an zwei Zeilen:

    WeaponModStackMag.java:52: error: method does not override or implement a method from a supertype

`IWeaponMod.onInstall` bekam einen Parameter mehr; zwei der drei Aufsätze mit dieser Methode
wurden nachgezogen, der dritte nicht. Das ist ein gewöhnlicher Flüchtigkeitsfehler — die
Frage ist, warum **kein einziges der 35 Tore** ihn gesehen hat.

* `syntax-check.sh` **filtert genau diese Meldung weg**. Der Grund ist gut: ohne
  Minecraft-Klassenpfad entsteht sie zu Tausenden als Folgefehler, weil javac die
  Oberklassen gar nicht kennt. Nur trifft der Filter eben auch die echten Fälle.
* Der Durchgang aus Runde 110 in `override-check.sh` sucht den **Ausreißer** unter den
  Klassen, die dieselbe (Name, Stelligkeit) erklären. Die falsche Stelligkeit war hier
  einmalig — es gab keinen, mit dem sie hätte verglichen werden können.
* Die Durchgänge 1 bis 3 fragen, ob eine geforderte Methode **fehlt**, nicht ob eine
  vorhandene ins Leere zeigt.

**Der fünfte Durchgang schließt das.** Er ist keine Faustregel, sondern exakt: geprüft werden
nur Klassen, deren Vererbungskette *und* deren sämtliche Schnittstellen im Projekt liegen —
für die sieht das Skript dieselbe Methodenmenge wie javac. Was es dort nicht findet, findet
javac auch nicht.

Zwei Einschränkungen, beide gemessen nötig: nur Methoden auf Klammertiefe 1 (ein `@Override`
in einer inneren oder anonymen Klasse gehört nicht der Hauptklasse — ohne diese Grenze
58 Falschmeldungen), und die Methoden von `java.lang.Object` (ohne sie vier weitere).

**Nachgemessen in beide Richtungen:** über 457 Kandidatenklassen null Funde; nimmt man den
Parameter aus `WeaponModStackMag` wieder heraus, meldet der Durchgang genau dessen zwei
Zeilen, und der Rückgabewert ist 1.

**Nebenbei behoben:** die Methodenerkennung des zweiten Durchgangs war für **jede Methode mit
`throws`-Klausel blind** — der Ausdruck verlangte direkt hinter der Parameterklammer ein `;`
oder `{`. Aufgefallen ist das beim Nachmessen des neuen Durchgangs: er meldete 45 Methoden wie
`serializeJSON` und `writeRecipe` als "überschreibt nichts", obwohl die Oberklasse sie sehr
wohl erklärt. Damit war auch der Schnittstellendurchgang für diese Methoden blind.

### Die drei Aufsätze des Lasergewehrs

Im selben Zug: `LAS_SHOTGUN` (drei Strahlen statt einem, je gut ein Drittel Schaden, keine
Hüftstreuung mehr), `LAS_CAPACITOR` (anderthalbfaches Magazin, fünf Prozent mehr Schaden) und
`LAS_AUTO` (Dauerfeuer alle fünf Ticks, zwei Drittel Schaden, kein Fernrohr mehr). Jeder
belegt einen anderen Platz, alle drei lassen sich zugleich anbringen.

**Damit gibt es im Port keinen Aufsatz mehr ohne Klasse.** Bis zu dieser Runde standen
vierzehn in `ModSpecial`, die sich eingetragen hatten, ohne angemeldet zu sein.

## Runde 192 — Der Ladungswerfer, und ein Geschoss, das stehenbleibt

`XFactoryTool` zerfällt sauber in zwei Hälften, und die Messung entscheidet, welche portierbar
ist.

**Der Ladungswerfer ist es.** Alles, was er braucht, lag da — bis auf **eine** Sache: ein
Geschoss, das in der Wand stecken bleibt, statt an ihr zu zerschellen. Der Enterhaken ist der
Ankerpunkt, an dem sich der Schütze heranzieht; fällt er zu Boden, hat die Waffe keinen Zug.

Im Original erbt das Geschoss diesen Zustand: dort steht `EntityBulletBaseMK4` unter
`EntityThrowableInterp` und damit unter `EntityThrowableNT`, wo `getStuck` zuhause ist. **Der
Port hat die Vererbung anders geschnitten** — `ThrowableNT` und `ProjectileNT` sind zwei
getrennte Zweige unter `Projectile`. Also steht derselbe Zustand jetzt ein zweites Mal in
`BulletBaseMK4`.

Der naheliegende Weg dorthin wäre ein früher Ausstieg aus `tick()` gewesen, und er wäre
falsch: dann zählte `tickCount` nicht weiter und das Geschoss liefe nie ab. Stattdessen steht
die Bewegung auf null **und die Schwerkraft setzt aus, solange es steckt** — damit sie das
auch bleibt. Verschwindet der Block, an dem der Haken hängt, fällt das Geschoss wieder; sonst
hinge man an einer Wand, die es nicht mehr gibt.

### Drei Waffen in einer

Was im Rohr steckt, entscheidet alles — und man wechselt es nur über das Nachladen:

* **Enterhaken** — bleibt stecken, 6000 Ticks Lebensdauer, durchschlägt (damit er nicht an
  einem Schwein hängenbleibt), macht keinen Schaden.
* **Mörserladung** — Radius 5, räumt grob auf.
* **Große Ladung** — Radius 15, nichts fällt als Bruchstück, dafür bleibt Schlacke.

Das Seil selbst hat drei Zustände, und der Unterschied macht die Waffe aus: linke Taste zieht
heran (und löst den Haken, wenn man ihm näher als zwei Blöcke kommt), rechte Taste gibt das
Seil frei, **keine von beiden hält es straff** — würde der nächste Schritt den Abstand
vergrößern, wird er auf die alte Seillänge zurückgeholt. Daraus wird ein Pendel.

Der Leerschlag schweigt, solange ein Haken hängt: wer sich am Seil heranzieht, feuert nicht
ins Leere.

### Der Feuerlöscher bleibt, und das ist gemessen

Er verschießt Wasser, Schaum und Sand und braucht dafür `ammo_fireext` sowie die Löschblöcke
`foam_layer`, `sand_boron_layer`, `sand_mix` und `volcanic_lava_block`. Von denen gibt es im
Port nur `block_foam`. Eine Waffe, die Schaum verschießt, der nirgends liegenbleibt, wäre
keine Waffe — das ist eine Blockrunde, keine Waffenrunde.

### Zwei kleine Nachträge

Das **Zielfernrohr** passt im Original auch an den Ladungswerfer; die Liste im
`XWeaponModManager` ist entsprechend ergänzt. Ohne das wäre der Fernrohr-Zweig im Zeichner ein
Ast, den nichts erreicht.

Der **Bauplan** steht im Original zweimal da, einmal mit Leder und einmal mit Gummi als
letztem Stück. Hier steht eine Zutat, die beides annimmt — zwei Rezepte auf dasselbe Erzeugnis
wären in 1.21 nur ein zweiter Eintrag mit gleichem Ergebnis.

**NICHT übernommen:** das Original setzt beim Schwingen zusätzlich die Jetpack-Flugzeit zurück
(`ArmorUtil.resetFlightTime`). Diese Zeitrechnung gibt es im Port nicht — die einzige Stelle,
die sie je gerufen hätte, steht in `EntityEffectHandler` auskommentiert.

**37 von 46 Bauplänen.** Alle 35 Tore grün.

## Runde 193 — Ein Konstruktor mit einem Argument zuviel

Der Ladungswerfer aus Runde 192 fiel im CI um, an genau einer Zeile: `new
BlockMutatorDebris(NtmBlocks.BLOCK_SLAG.get(), 1)`. Die `1` sind die Metadaten aus 1.7.10 —
dort ist Metadaten 1 die gesprungene Fassung derselben Schlackentextur. Der Port kennt nur
`BlockMutatorDebris(Block)` und `BlockMutatorDebris(BlockState)`.

Die Zeile ist berichtigt. Die gesprungene Schlacke bleibt eine Abweichung: die Textur
`block_slag_broken.png` liegt im Baum, aber kein Block zeigt sie — das steht so im Quelltext.

### Warum kein Tor das gesehen hat

`syntax-check.sh` übersetzt ohne Minecraft-Klassenpfad; an einer Klasse mit Minecraft-Typen in
der Signatur kommt es gar nicht so weit. Die fünf Durchgänge in `override-check.sh` prüfen
ausschließlich **Methoden**, nie Konstruktoren.

Dafür gibt es jetzt einen **sechsten Durchgang**. Er ist streng beweisbar, nicht heuristisch:
Konstruktoren werden in Java nicht vererbt, also kennt das Skript bei einer Projektklasse
*alle* ihre Konstruktoren — ohne Vererbungskette, ohne Minecraft. Was dort durchfällt, fällt
auch bei `javac` durch. **Gemessen in beide Richtungen:** 6544 Konstruktoraufrufe, null Funde;
setzt man die Metadatenzahl wieder ein, meldet die Regel genau diese eine Zeile.

### Ein Tor, das blind war, ohne es zu sagen

Beim Messen meldete die neue Regel zunächst **279** Stellen. 275 davon hatten eine gemeinsame
Ursache, und die saß nicht in der neuen Regel, sondern im Kommentar-Entferner, den alle
Durchgänge benutzen: `re.sub(r'//[^\n]*', '', src)` schneidet auch dort, wo das doppelte
Schrägzeichen **innerhalb einer Zeichenkette** steht. In `HFRWavefrontObject` steht es in
einem regulären Ausdruck (`"(f( \d+//\d+){3,4}")`). Zurück blieb ein offenes
Anführungszeichen — und von da an verrutschte für den Rest der Datei jede Klammerzählung. Die
Klasse war für den Klammerzähler komplett unsichtbar, ihre vier Konstruktoren standen
scheinbar auf Tiefe 0.

Der Entferner ist jetzt zeichenkettensicher und zeilentreu. Die restlichen vier Meldungen
waren Generika-Kommas in Parameterlisten (`BiConsumer<ItemStack, Player>` ist ein Parameter,
nicht zwei) — auch das ist berichtigt.

Denselben Regelausdruck benutzen **16 weitere Tore**. Dort richtet er keinen Schaden an, und
das ist nachgezählt statt behauptet: im ganzen Baum gibt es **sechs** Zeilen mit `//` in einer
Zeichenkette (`HTTPHandler` 42/70/80, `HFRWavefrontObject` 35/308, `NtmEventHandler` 55), und
was dort abgeschnitten wird, ist reiner Zeichenketteninhalt — Netzadressen und ein regulärer
Ausdruck, kein einziger Bezeichner. Außerdem zählt keines dieser Tore Klammertiefen; sie
verlieren eine Zeile, nicht den Rest der Datei. Nachzählen:
`grep -n '"[^"]*//' -r src/main/java --include=*.java`

Alle 35 Tore grün.

## Runde 194 — Der Feuerlöscher, und die Blöcke, die er hinterlässt

Damit ist `XFactoryTool` vollständig. Die Behauptung aus Runde 192 — der Löscher brauche vier
Blöcke, die es nicht gibt — hat zwei Teile, und **beide waren falsch**:

- **`LayeringBlock` gibt es längst.** Der Port hat die Klasse samt `layeringBlock`-Helfer im
  Blockstate-Erzeuger; `leaves_layer` und `oil_spill` benutzen sie. `foam_layer` und
  `sand_boron_layer` sind damit zwei Zeilen, keine Runde.
- **`volcanic_lava_block` war nie nötig.** Er kommt in genau einem optionalen Zweig vor, der
  vulkanische Lava zu Obsidian macht — und dieser Zweig prüft im Original den Metadatenwert 0.
  Den gibt es in 1.21 nicht. Der Zweig ist weggelassen, mit Begründung im Quelltext.

Übrig blieb ein einziger echter Bedarf: **Borsand**. Das Original ist eine Metadaten-Spielart
von `sand_mix`; hier steht `sand_boron` als eigener Block, genau wie `sand_quartz` es
vormacht.

### Was der Löscher tut

Drei Tanks, kein Schaden, 300 Schuss. **Wasser** löscht in einem 3×3×3-Würfel alles Feuer und
spült dabei auch Schaum weg — Schicht wie vollen Block. **Schaum** löscht nur den Treffer,
bleibt dafür liegen. **Borsand** löscht gar nicht im Umkreis; er erstickt das Feuer dort, wo er
liegenbleibt.

Schaum und Sand wachsen Lage um Lage. Die **siebte Lage ist die letzte** — der nächste Schuss
macht daraus den vollen Block. Das entspricht dem Original, wo die Metadaten von 0 bis 6 laufen.
Die achte Lage, die `LayeringBlock` zulässt, erreicht der Löscher nie; von Hand gesetzt gibt es
sie, und dann schlägt der nächste Schuss sie ebenfalls um.

### Die einzige Stelle, die `tryExtinguish` je auslöst

`IRepairable.tryExtinguish` samt `EnumExtinguishType` steht seit langem im Port, mit einem
Aufrufer (`Chemical`) und einem Implementierer (`MachineFluidTankBlockEntity`). **`SAND` hatte
bis heute keinen Erzeuger** — keine einzige Zeile im Port konnte diesen Wert je erreichen. Der
Löscher schließt das: alle drei Tanks melden der getroffenen Maschine ihre Sorte.

### Abweichungen

- Der Sandtank wird aus `sand_boron` gebaut statt aus `sand_mix` mit Metadatenwert `BORON` —
  derselbe Sand, anderer Name.
- Das Inventarbild dreht sich nicht (`System.currentTimeMillis` im Original). Der Port zeichnet
  Waffen im Inventar still, wie alle anderen auch.
- Die Spur des Strahls zeichnet der Client selbst, statt ein Partikelpaket vom Server zu
  erwarten: `onUpdate` läuft auf beiden Seiten, und `blockdust` fehlt in `effectNT`.

**38 von 46 Bauplänen.**

## Runde 195 — Werkzeuge ohne Maschinen, Maschinen ohne Werkzeug

Drei Funde derselben Art, alle im Übergang zwischen `ToolType` und `onScrew` — der einzigen
Verbindung zwischen einem Einstellwerkzeug und den Maschinen, die es bedient. Beide Seiten
stehen weit auseinander, und **nichts im Bau verbindet sie**: fällt eine weg, meldet weder
`javac` noch das Spiel etwas.

### Der Handbohrer gab es gar nicht

**Sechs Blöcke** fragen im Port nach `ToolType.HAND_DRILL` — beide Heizer, beide
Reaktorstapel-Blöcke, die elektrische Presse und der Gießereiauslass — und **kein einziger
Gegenstand hat diese Sorte je getragen**. Sechs Zweige, die nichts erreichen konnte.

Das ist die zweite Hälfte des Fundes aus Runde 99. Dort fiel auf, dass die Schraubenzieher
keine Werkzeuge *waren*; hier fällt auf, dass es die Handbohrer nicht *gab*.

### Der Schraubenzieher war nicht herstellbar

Schlimmer: `screwdriver` und `screwdriver_desh` waren im Port **Zutat, aber niemals
Erzeugnis**. Sie gehen in den Dämonenkern und in die Raketenmontage hinein; es gab keinen
Bauplan, keine Beutetabelle, keinen Weg. Fünfzehn Maschinen, das Schloss-System und beide
Baupläne hingen an einem Gegenstand, den man nicht bekommen konnte.

Alle vier Baupläne sind nachgereicht, wortgetreu aus `ToolRecipes` des Originals.

### Der Entschärfer konnte nichts tun — und ein Zustand war unerreichbar

`ToolType.DEFUSER` hatte einen Gegenstand und **keinen einzigen Block, der nach ihm fragt**.
Gleichzeitig liest `TNTBaseBlock.playerWillDestroy` den Zustand `UNSTABLE` — wer einen so
gestellten Sprengsatz abbaut, zündet ihn —, und **nichts konnte ihn auf `true` setzen**.

`onScrew` gab dort nur `false` zurück. Eine einzige Methode schließt beides: der Entschärfer
bricht den Satz ab und gibt ihn zurück, der Schraubenzieher schaltet die Zündung um.

### Das 36. Tor

`tools/tool-check.sh`. Für jede Sorte in `ToolType` gilt: entweder **beide** Seiten oder
**keine**. Eine Sorte ganz ohne Träger und Abfrager ist kein Fehler — sie ist eine
Aufzählungsstelle, die noch niemand benutzt (`WRENCH`). Eine Sorte mit nur einer Seite ist
einer.

**Gemessen in drei Richtungen:** nimmt man die Handbohrer wieder heraus, meldet das Tor sechs
Blöcke namentlich; setzt man `onScrew` zurück auf `false`, meldet es den Entschärfer; trägt
man eine Sorte fälschlich in die Ausnahmeliste ein, meldet es die veraltete Zeile.

Die einzige Ausnahme ist `BOLT`: zwei Umwandlungen warten darauf, aber die Bolzenpistole des
Originals braucht `bolt_spike`, den Klang `RIVET_GUN` und die Schnittstelle `IAnimatedItem` —
nachgemessen, alle drei fehlen.

Alle 36 Tore grün.

## Runde 196 — Die Panzerrüstungen, und eine Behauptung, die zweimal falsch war

`XFactoryPA` galt seit Runde 192 als **blockiert**: seine beiden Waffen delegieren an die
getragene Rüstung, und `ArmorRPA`/`ArmorNCRPA` gibt es im Port nicht. Der erste Teil stimmt,
der zweite ist eine Feststellung, keine Sperre — und die Schlussfolgerung war falsch.

**Gemessen:** `ArmorFSBPoweredItem` steht seit Runde 101 im Port, `ModelArmorBase` und
`ModelRendererObj` ebenso, die CE-Abspaltung hat `remnant.obj`, `ncrpa.obj` und alle acht
Texturen. Es war Arbeit, kein Hindernis.

### Warum die Waffen keine Waffen sind

`gun_pa_melee` und `gun_pa_ranged` tun aus sich heraus **gar nichts**. Sie reichen jeden Klick,
jeden Bewegungssatz und jeden Ton an die Brustplatte weiter. Der Nahkampf einer Panzerrüstung
sind ihre Arme, und die gehören der Rüstung: die Remnant schlägt mit Fäusten, die NCR mit
Klingen — andere Bewegungen, anderer Schaden, andere Klänge. Lägen sie in der Waffe, müsste
jede Waffe jede Rüstung kennen.

**Der Schaden entsteht nicht beim Klicken, sondern im Takt.** Die Orchestra prüft jeden Zug, ob
die Bewegung dort ist, wo die Faust ankommt — Zug 3 und 9 beim Doppelschlag, Zug 8 bei der
Ohrfeige — und schaut *dann* erst, was davorsteht. Man trifft, was beim Aufschlag da ist, nicht
was beim Klicken da war.

### Beide Rüstungen, nicht eine

Die Remnant liefert nur das Nahkampfbauteil; **nur die NCR hat beides**. Hätte ich allein die
Remnant portiert, wäre `gun_pa_ranged` eine Waffe ohne jeden Geber geworden — genau der Fehler,
den die letzten Runden wiederholt aufgeräumt haben. Also beide.

Dafür kamen die NCRPA-Raketen dazu (`rocket_ncrpa`, `rocket_ncrpa_steer`) samt der Lenkung:
über hundert Blöcke Abstand hört sie auf, näher als drei Blöcke am Ziel wird nicht mehr
korrigiert. `Library.rayTrace` stand dafür längst bereit.

### Die Kette bis zum Ende

Eine Rüstung, die man nicht bauen kann, hilft niemandem. Die vier Baupläne standen in
`PrecAssRecipes` als auskommentierte Liste mit drei genannten Gründen — **zwei davon waren
überholt**: den Deshmotor gibt es seit Runde 119, die Rüstung seit dieser Runde. Blieben die
**Legendenteile**, und die sind ein Gegenstand mit drei Stufen und fünf formlosen Bauplänen.
Sie sind mitgekommen.

Ohne sie keine Brustplatte, ohne Brustplatte keine Panzerrüstungswaffen. Jetzt steht die Kette
von Kettenstahl und Alexandrit bis zur Faust.

Die NCR-Rüstung hat **absichtlich kein Rezept**: sie ist im Original ein Fundstück aus den
Schlüsselloch-Truhen.

### Ein Tor, das 131 Methoden nicht ansah

Beim Gegenmessen fiel auf, dass `dist-check.sh` die neue Schnittstelle `IPAWeaponsProvider`
**gar nicht prüft**: nimmt man dort die Kennzeichnung heraus, meldet das Tor nichts. Ursache
ist der Methodenausdruck — er verlangt einen Sichtbarkeitsmodifikator:

```
((?:public|protected|private)\s[^;{}()\n]*\([^)]*\)…)\{
```

**Eine Schnittstellenmethode hat keinen.** `static IPAMelee getMeleeComponentClient() {` beginnt
mit `static`, `default void onInstall(…) {` mit `default`. Damit war jeder `default`- und
`static`-Rumpf im ganzen Baum unsichtbar: **131 Methoden in 45 Dateien**, darunter die
vollständige `api/hbm`-Ebene.

Ein zweiter, kleinerer Fehler steckte im Annotationsteil: er erzwang einen Zeilenumbruch
zwischen `@OnlyIn(Dist.CLIENT)` und der Signatur. `IKeybindReceiver` schreibt beides in eine
Zeile — die Methode galt als ungekennzeichnet.

Beides ist berichtigt, und dahinter lag **ein echter Fund**: `ITooltipProvider.addStandardInfo`
ist eine `default`-Methode, die `Screen.hasShiftDown()` ruft. Diese Schnittstelle implementieren
Blöcke, und Blöcke lädt der dedizierte Server. Sie ist jetzt gekennzeichnet.

**Gemessen in beide Richtungen:** 1079 statt 1066 Methoden angesehen, null Funde; nimmt man in
`IPAWeaponsProvider` die Kennzeichnung wieder heraus, meldet das Tor genau diese Methode.

### Abweichungen

Das Original hängt an beiden Rüstungen VATS, Strahlungsklasse, Strahlenschutz, harte Landung
und eigene Schritt- und Sprungklänge. Diese Baukastenteile gibt es im Port noch nicht —
`ArmorFSBItem` kennt bisher Effekte, Geigerton und die Frage, ob ein Helm dazugehört. Was da
ist, ist übernommen; was fehlt, fehlt sichtbar und nicht still.

Alle 36 Tore grün.

---

## Runde 197 — Eine Zahl zu breit, und zwei Werfer, die nie blockiert waren

Zwei Dinge in einer Runde: ein CI-Lauf, der an einem einzigen Zeichen scheiterte, und die
letzten beiden Raketenwaffen, die seit Runde 192 als blockiert geführt wurden.

### Der CI-Fehler und das 37. Tor

`ModelArmorRPA` ließ den Lüfter der Panzerrüstung mit

```java
Axis.ZP.rotationDegrees(-(System.currentTimeMillis() / 2D % 360D))
```

drehen. `rotationDegrees` nimmt ein `float`, der Ausdruck ist ein `double` — javac lehnt ab.
**Keines der sechsunddreißig Tore sah es**: die Signatur der gerufenen Methode steht in der
Bibliothek, nicht im Port, und `syntax-check.sh` übersetzt ohne Minecraft-Klassenpfad.

Das war der **zweite CI-Lauf in vier Runden**, den eine reine Typfrage kostete — Runde 193 war
die Stelligkeit eines Konstruktors, diese die Breite einer Zahl. Beide Male stand die Antwort
im Projekt selbst.

`tools/narrow-check.sh` misst sie, nach dem Mehrheitsprinzip von `signature-check.sh`:
übergibt eine Aufrufstelle ein `double`, wo alle anderen Aufrufstellen derselben Methode
desselben Empfängertyps ein `float` übergeben, ist sie es, die falsch ist. Bei
`Axis.rotationDegrees` stehen **1090 Belege gegen diesen einen**.

Vier Bedingungen halten die Fehlalarme heraus, und jede war nötig:

* **Der Empfängertyp gehört in den Schlüssel.** Über den nackten Namen gemessen kollidieren
  `Vec3.scale(double)` und `RenderContext.scale(float)` — zwölf Fehlalarme.
* Mindestens acht Belege, sonst ist „die Mehrheit" nichts wert.
* Höchstens ein Zehntel Abweichler, sonst ist es eine Überladung, die beides nimmt.
* Nur zweifelsfreie Ausdrücke — was ein Cast berührt, zählt gar nicht.

**Gruppierungsklammern sind keine Aufrufklammern**, und der erste Entwurf verwechselte beides:
`-(a / 2D)` ist ein `double`, obwohl die Zahl in Klammern steht, `foo(2D)` dagegen nicht. So
verdeckt gemessen fand das Tor genau den Fehler nicht, gegen den es gebaut wurde.

Gemessen: 38 980 Aufrufstellen mit Empfängertyp, null Funde; Cast wieder weg, genau eine Zeile.

### Quadro und Raketenwerfer

Der Kopf von `XFactoryRocket` sagte, beide seien nicht übernommen, weil ihnen die lenkbaren
Raketen fehlten „und Lenkung im Port nicht vorhanden" sei. **Beides nachgemessen und falsch:**

* `makeML` ist im Original eine **reine Kopie** ohne jede Lenkung. Der Raketenwerfer lenkt gar
  nicht über die Rakete, sondern über die **Zielerfassung an der Waffe** — und die liest
  `BulletBaseMK4.lockonTarget` längst aus.
* `Library.rayTrace(Player, double, float)`, das die Lenkung des Quadro braucht, steht im Port
  seit jeher.

Die Zielsuche des Stingers (`sucheZiel`) ist mitgekommen, weil der Raketenwerfer sie braucht:
ein Kasten um den Blickstrahl als Vorauswahl, die Entscheidung fällt der Winkel. Der Stinger
selbst bleibt offen — er braucht eine eigene Klasse mit zweitem Tastenpaar zum Aufschalten.

**Ein Fund in der eigenen Arbeit:** meine erste Fassung ließ die Quadro-Rakete bedingungslos
lenken. Im Original hängt sie am Zielen — lässt der Schütze die Zieltaste los oder wechselt die
Waffe, fliegt sie geradeaus weiter. Das ist der ganze Unterschied zur NCR-Rakete, die ohne
Bedingung lenkt: die Rüstung hat keine Zieltaste, die man loslassen könnte.

### Abweichungen

`setupModTable` ist bei beiden Renderern nicht übernommen — der Waffentisch des Ports zeigt
statt der Waffe ihr Gegenstandsbild, und das ist im Port überall so. Die leuchtende Schrift am
Visier (drehendes `">> <<"` beim Quadro, flackerndes `"AUTO"` beim Raketenwerfer) steht im
Original als Block aus Lichtkarten-Rechnerei; der Port hat dafür `FullBright`.

Alle 37 Tore grün.

---

## Runde 198 — Die Bolzenpistole, und ein Erzeuger, den es nicht gab

### Der CI-Fehler

Die Zielsuche aus Runde 197 schreibt den Blickvektor dreimal ab, bevor sie ihn verrechnet —
`add`, `multiply` und die Drehungen ändern in `Vec3NT` **alle den Vektor selbst** und geben ihn
zurück. Wer ihn mehrfach anders braucht, muss ihn kopieren.

**Den Abschreibe-Erzeuger gab es im Port nicht.** `Vec3NT` kennt `()`, `(x,y,z)` und `(Vec3)` —
und ein `Vec3NT` ist kein `Vec3`. Das Original hat `Vec3NT(Vec3NT)`; er ist nachgereicht.

### Das 38. Tor

Das war der **dritte Typfehler in fünf Runden**, den kein Tor sah: Runde 193 die Stelligkeit
eines Konstruktors, Runde 196 die Breite einer Zahl, Runde 197 der Typ eines Arguments.
`override-check.sh` zählt seit Runde 193 die Argumente — hier stimmte die Zahl und der Typ nicht.

`tools/ctorarg-check.sh` prüft den Typ. Entscheidbar ist der Fall, weil die Erzeuger im Projekt
stehen und der Typ des Arguments in derselben Datei — als Erklärung, als Parameter oder im Kopf
einer `for`-Schleife.

**Die ersten elf Funde des Entwurfs waren allesamt falsch**, und jeder zeigte auf eine eigene
Lücke:

* **Typvariablen sind keine Klassen.** `Pair<X, Y>` hat einen Erzeuger `(X, Y)`; `X` ist kein
  Typ, gegen den sich etwas prüfen ließe.
* **Eine Aufzählung erbt von `Enum`.** `ComparableStack(Item, int, Enum)` nimmt jeden
  Aufzählungswert — ohne diese Regel meldete das Tor vier Rezepte.
* **Die nächstliegende Erklärung gilt**, nicht irgendeine in der Datei. Und `var` sagt den Typ
  nicht — es wird trotzdem vermerkt, damit es eine früher stehende Erklärung verdeckt.
* **Klammern und Doppelpunkte dürfen nicht mitverbraucht werden.** Sonst findet in
  `(Level level, BulletConfig art)` der zweite Parameter kein Komma mehr vor sich, weil der
  erste es aufgebraucht hat — und `for(ItemStack mod : mods)` fällt ganz heraus.

Wessen Ahnenreihe das Projekt verlässt — jeder Block, jeder Gegenstand —, fällt heraus: was
über `Item` steht, weiß der Port nicht. Entscheidbar sind damit vor allem die eigenen
Wertklassen, und genau dort lag der Fehler.

Gemessen: 8949 Erzeuger-Aufrufe, null Funde; Erzeuger wieder weg, genau die drei Zeilen.

### Die Bolzenpistole

Sie stand seit Runde 195 als einzige Ausnahme im Werkzeug-Tor, mit drei genannten Gründen.
**Zwei davon waren falsch:**

* `bolt_spike` **braucht sie gar nicht** — die Zeile, die ihn im Original verschießt, ist dort
  auskommentiert (`//FIXME`). Verschossen werden Stahl-, Wolfram- und Durastahlbolzen.
* Der Klang **„RIVET_GUN" existiert nicht**, auch nicht im Original. Er heißt `tool.boltgun`,
  und `NtmSoundEvents.BOLTGUN` samt `boltgun.ogg` steht seit jeher im Port. Der Name kam aus
  der Übersetzung: das Original nennt die Waffe „Pneumatic Rivet Gun".

**Richtig war der dritte Grund:** `IAnimatedItem` fehlte. Er ist nachgereicht, und das war ein
Zweizeiler an der richtigen Stelle: `HbmAnimations` schlägt eine laufende Bewegung ohnehin über
den Übersetzungsschlüssel nach und fragt nie, ob der Gegenstand eine Waffe ist — nur der **Weg
dorthin** war einer, weil `HbmAnimation` ausschließlich nach `GunBaseNTItem` sah.

Sie kann dreierlei: das Bauteil eine Baustufe weiterbringen, einen gewöhnlichen Block umbauen
(genau ein Eintrag im Original: Stein wird für einen Durastahlbolzen zu Bruchstein), und einem
Wesen einen Bolzen aus dem Rucksack entgegenschießen.

**Das Werkzeug-Tor sah sie zunächst nicht.** Es kennt Träger als `new ToolingItem(ToolType.X`
oder `ToolType.X.register(` — eine Unterklasse, die die Sorte an `super()` weiterreicht, war
ihm neu. Erweitert und gegengemessen.

### Abweichungen

Das Original vergibt beim Töten eines Spielers den Erfolg `achGoFish`; ein Erfolgssystem gibt es
im Port nicht, nachgemessen. `setDamageBypassesArmor()` ist im Port eine eigene Schadensart
(`NtmDamageTypes.BOLTGUN` im Sack `BYPASSES_ARMOR`) — in 1.21 hängt das an der Schadensart, nicht
am einzelnen Schlag. Der Renderer verwendet die Haltungszahlen des Ports statt der
`ItemRenderFrames17`-Matrizen des Originals, die eine Nachbildung von 1.7.10 sind.

Zwei falsche Sätze in `ToolConversionBlock` fielen dabei auf: der Katalog enthalte „bisher nur
watz_end" (alle drei stehen längst drin), und die Watz-Außenwand werde „mit dem
Schraubenschlüssel" verschraubt (es ist die Bolzenpistole).

**Damit hat jede benutzte Werkzeugsorte beide Seiten, und das Werkzeug-Tor hat keine Ausnahme
mehr.**

Alle 38 Tore grün.

---

## Runde 199 — Der Stinger schließt die Raketenfamilie

Er stand seit Runde 197 als einzige offene Stelle im Kopf von `XFactoryRocket`, und die
Begründung war diesmal richtig: er braucht eine eigene Klasse. **Drei Bausteine fehlten**, alle
drei gemessen:

* `Receiver.setupLockonFire()` war im Port **auskommentiert** — es fehlte
  `Lego.LAMBDA_LOCKON_CAN_FIRE`, eine einzige Zeile.
* `Orchestras.ORCHESTRA_STINGER` fehlte samt dem Suchton `weapon.fire.lockon`.
* `RenderScreenOverlay.renderStingerLockon` fehlte.

Alles andere war da: die Zielsuche (`sucheZiel`, Runde 197), `rocket_rpzb`,
`LAMBDA_PANZERSCHRECK_ANIMS`, `NtmSoundEvents.TECH_BLEEP`, `GunConfig.ps`/`rs`, `AudioWrapper`
samt `loopedSounds`, und `stinger.obj`/`stinger.png` in der CE-Abspaltung.

### Was ihn besonders macht

**Er ist die einzige Waffe des Ports, bei der die linke Taste nicht immer feuert.** Die rechte
hält den Sucher an; solange sie gedrückt bleibt, gezielt wird und eine Rakete im Rohr steckt,
läuft er auf ein Ziel zu. Nach sechzig Zügen rastet er ein — und `setupLockonFire` lässt erst
dann überhaupt schießen. Wer ohne Ziel abdrückt, bekommt nichts: keinen Schuss, keinen
Leerschlag.

Die Raketen sind dieselben wie die des Panzerschrecks. Sie lenken nicht selbst, sondern
bekommen beim Abschuss das erfasste Ziel zugewiesen, und `BulletBaseMK4.lockonTarget` fliegt es
an — derselbe Weg wie beim Raketenwerfer aus Runde 197.

**Ein Klang, der an einem Zustand hängt statt an einer Bewegung:** der Suchton liegt auf dem Ohr
des Schützen, solange gesucht und noch nicht eingerastet ist. Jede andere Schleife im Port hängt
an einer Animation.

### Zwei Funde in der eigenen Arbeit

**Das Original führt ein `prevLockon` und benutzt es nirgends** — `renderStingerLockon` rechnet
mit `lockon` allein. Ein totes Feld ist nicht mitgekommen.

**`@OnlyIn(Dist.CLIENT)` an der falschen Stelle.** Mein erster Entwurf kennzeichnete das Feld
`lockon` und die Methode, die es führt, als client-seitig. Beide werden aber aus
`inventoryTick` gerufen, und das läuft auf beiden Seiten: auf einem dedizierten Server wären sie
entfernt gewesen und der Aufruf ins Leere gegangen. Die Kennzeichnung steht jetzt nur an
`renderHUD`, wo sie hingehört — dort überschreibt sie eine ohnehin client-seitige Methode.

### Abweichungen

Der Balken ist bei eins gedeckelt; das Original begrenzt ihn nicht, und weil der Zähler nach dem
Einrasten weiterläuft, wäre er über seinen Rahmen hinausgewachsen. `setupModTable` ist wie bei
allen Waffen des Ports nicht übernommen.

**Damit ist `XFactoryRocket` vollständig:** fünf Gefechtsköpfe, fünf Raketensätze, und alle vier
Werfer — Panzerschreck, Quadro, Raketenwerfer, Stinger — plus die Schulterrakete der NCR-Rüstung.

Alle 38 Tore grün.

---

## Runde 200 — Zwei Waffen, zwei überholte Begründungen

Nach dem Stinger blieb die Frage, was von den Waffenfabriken noch fehlt. **Gemessen**: von 31
`XFactory`-Dateien sind alle da, und in ihnen fehlen sechs Waffen — Ketzer, Lacunae, LAG,
Spulenkanone, Tau-Kanone und Fatman. Zwei davon hatten eine Begründung, und **beide waren
falsch**.

### Die Ketzer-Selbstladeflinte

`XFactory10ga` sagte, sie borge sich alles von der Schredder-Flinte der 12 Gauge, „die im Port
noch fehlt". **Die Schredder-Flinte steht in `XFactory12ga`**, samt `LAMBDA_SEXY_ANIMS`,
`LAMBDA_RECOIL_SEXY` und `ORCHESTRA_SHREDDER_SEXY`. Auch der Klang war da: das Original ruft
`fireShotgunAuto`, und `NtmSoundEvents.GUN_SHREDDER_FIRE` zeigt seit jeher auf dieselbe Datei.

Es fehlte **nichts** — nur die Registrierung.

### Das Lacunae

`XFactory762mm` sagte, es verschieße „Kondensatoren aus `XFactoryEnergy`, die der Port noch
nicht hat". Zweimal falsch: `Ammo.CAPACITOR` steht seit der Laserfamilie im Port, und die drei
Strahlsätze des Lacunae stehen im Original **gar nicht in `XFactoryEnergy`**, sondern in
`XFactory762mm` selbst — es ist eine 7,62er Minigun, der nur der Lauf ausgetauscht wurde.

Wirklich gefehlt haben zwei Kleinigkeiten: `LegoClient.RENDER_LASER_PURPLE` und der Klang
`weapon.fire.lasergatling`.

### Ein dritter überholter Satz

`ItemRenderMinigun` behauptete, „weder die Waffe noch `renderLaserFlash`" stünden im Port.
**`renderLaserFlash` steht seit jeher in `ItemRenderWeaponBase`** — eine Zeile über dem
Kommentar, das ihn für fehlend erklärte. Das Lacunae teilt sich den Renderer und bekommt statt
des Mündungsfeuers zwei ineinanderliegende Laserblitze, wie im Original.

### Abweichungen

Das Lacunae ist im Original ein **Sockelfundstück**; den Sockel gibt es im Port nicht
(nachgemessen). Es ist damit vorerst nur im Kreativreiter zu haben — wie die drei anderen
Legenden des Ports (Dani, Morning Glory, Daybreaker) auch. Die Ketzer-Flinte steht wie im
Original auf `WeaponQuality.DEBUG` und hat weder Haltbarkeit noch Bauplan.

Das Rückstoßlambda des Lacunae ist im Original leer und hängt hier gar nicht erst ein — dieselbe
Abweichung wie bei den Raketen.

Alle 38 Tore grün.

---

## Runde 201 — Die LAG, und ein Modell, das nach einer anderen Waffe heißt

Von den sechs gemessenen Lücken in den Waffenfabriken war die LAG die einzige **ohne jede
Begründung** — `XFactory9mm` sagt nichts über sie. Sie war schlicht unportiert, und nichts stand
ihr im Weg: die vier 9-mm-Sätze (`p9_sp`, `p9_fmj`, `p9_jhp`, `p9_ap`) stehen seit jeher da, der
Animationslader auch, und die CE-Abspaltung hat alles, was sie braucht.

### Das Modell heißt nicht nach ihr

`ItemRenderLAG` zeichnet sie mit **`mike_hawk.obj`** und legt `lag.png` darüber. Der Mike Hawk
ist eine andere Waffe; die LAG teilt sich sein Modell. Das steht so im Original, und beides ist
übernommen — ein umbenanntes Modell wäre eine stille Abweichung gewesen.

### Ihre Bewegungen stehen nicht im Quelltext

Bis auf das Ziehen kommen alle aus `models/animations/lag.json` — dem zweiten Bewegungssystem,
das der Port seit dem Flammenwerfer kennt. Fünf Teile bewegen sich einzeln: Griff, Schlitten,
Hahn, Magazin und die sichtbare Patrone, die nur gezeichnet wird, wenn eine geladen ist.

Beim Nachsehen legt das Original zwei Busse obendrauf, die die ganze Waffe näher heranholen und
kippen (`ADD_TRANS`, `ADD_ROT`); der Renderer liest sie aus. Der Hahn hat einen eigenen
Drehpunkt hinten oben am Schlitten.

### Nebenbei nachgereicht

`anyResistantAlloyLightReceiver` fehlte in `NtmRecipeProvider` — es gab den schweren Empfänger
aus beiden Legierungen, den leichten nicht. Das Rezept der LAG braucht ihn.

Alle 38 Tore grün.

**Damit fehlen von den Waffenfabriken noch drei:** Spulenkanone und Tau-Kanone
(`XFactoryAccelerator`, Begründung noch ungeprüft) und der Fatman (`XFactoryCatapult`) — der
braucht die gesamte Mininuke-Munitionsfamilie und ist eine eigene Runde.

---

## Runde 202 — Die Spulenkanone, und Munition, die es längst gab

`XFactoryAccelerator` sagte, Tau-Kanone und Spulenkanone brauchten „Munition, die der Port nicht
hat (Tau-Ladungen und Spulengeschosse aus Wolfram und Ferrouran)".

**Nachgemessen falsch, und zwar gründlich:** `NtmItems.AMMO_STANDARD` ist ein `EnumMultiItem`
über `GunFactory.Ammo`, und dort stehen `TAU_URANIUM`, `COIL_TUNGSTEN` und `COIL_FERROURANIUM`
seit jeher — samt Eintrag in `ORDER`, also **mit Textur und Platz im Kreativreiter**. Drei
Munitionssorten, die man in die Hand nehmen kann und die nichts verschießt.

Gefehlt haben nur die `BulletConfig`s, die sie an ein Verhalten binden. **Dasselbe gilt für die
ganze `NUKE_*`-Familie des Fatman** — auch die steht in der Aufzählung.

### Das Geschoss räumt sich den Weg frei

Die beiden Spulengeschosse sind die einzigen des Ports, die **Blöcke zerbrechen, durch die sie
fliegen**. Alle halbe Blocklänge wird geprüft; Wolfram räumt alles bis Härte 1,25 weg,
Ferrouran bis 2,5. Sie sind dabei spektral — sie halten an Blöcken gar nicht erst an.

Der Wortlaut des Originals prüft `isAir() && hardness < threshold`, was wie ein Widerspruch
aussieht (Luft hat keine Härte). Er ist trotzdem übernommen: ohne eine Messung, die etwas
anderes belegt, wird ein Original nicht „verbessert".

### Ein Mündungsfeuer, das nicht hingehört

`ORCHESTRA_COILGUN` teilen sich Spulenkanone und NI4NI. Im Original bekommt **nur die NI4NI**
ein Mündungsfeuer — die Spulenkanone schießt mit Magnetfeldern. Diese Wache fehlte im Port,
weil bisher nur die NI4NI portiert war und die Frage sich nicht stellte. Sie ist
wiederhergestellt, bevor die Spulenkanone dazukam.

### Abweichungen

Die Spulenkanone hat **im Original keinen Bauplan und keine Beutetabelle** — sie steht auf
`WeaponQuality.SPECIAL` und ist ein Fundstück; im Port damit vorerst nur im Kreativreiter. Der
Renderer des Originals bindet am Anfang die Textur der Leuchtpistole und überschreibt sie drei
Zeilen später — ein Rest, der nichts tut, und nicht übernommen. Der Funkenpartikel heißt in 1.21
`ParticleTypes.FIREWORK`, im Original `EnumParticleTypes.FIREWORKS_SPARK`.

**Offen bleibt die Tau-Kanone:** sie hat ein Aufladewerk — der Zweitdruck lädt auf, der
Erstdruck feuert, und ein aufgeladener Schuss kommt aus einem zweiten, eigenen Magazin. Das ist
eine eigene Runde.

Alle 38 Tore grün.

---

## Runde 203 — Die Tau-Kanone schließt den Beschleuniger

Die letzte Waffe von `XFactoryAccelerator`, und die einzige des Ports, **die ihren Schützen
töten kann.**

### Zwei Tasten, zwei ganz verschiedene Dinge

Die linke feuert einen gewöhnlichen Strahl. Die rechte **lädt auf** — und je länger man hält,
desto stärker wird der Schuss beim Loslassen: eine Einheit je zehn Züge, bis zu dreizehn. Die
Munition geht dabei im Aufladen drauf, nicht beim Schuss.

Der aufgeladene Schuss kommt **nicht aus dem Magazin der Waffe**, sondern aus `tauChargeMag` —
einem zweiten Gurt, der nur den spektralen Satz kennt. Verschleiß kostet er nach Stärke, nicht
nach Schuss.

**Nach zweihundert Zügen reißt es sie auseinander:** tausend Schaden auf den Schützen,
zehntausend Verschleiß auf die Waffe, ein Plasmafächer und zwei Knalle. Deshalb verstummt die
Ladeschleife bei dreihundert Zügen ohnehin — da lebt niemand mehr.

### Zwei Funde beim Portieren

**Eine Zeile, die nichts tut.** Der Zweitdruck ruft im Original
`MagazineBelt.getMagType(stack)` mit dem Kommentar „caches the last loaded ammo". Diese Methode
**liest nur** — sie schreibt nichts. Gespeichert wird die Sorte ohnehin beim gewöhnlichen
Schuss, über `getType`. Die Zeile ist nicht mitgekommen.

**Die Strahlen sind bernsteinfarben, nicht violett.** Ich hatte sie zunächst auf
`RENDER_LASER_PURPLE` gelegt — geraten, nicht gemessen. Das Original hat für die Tau-Kanone
**zwei eigene Renderer**: der gewöhnliche Schuss zieht einen dunklen Kern (`0x302510`) mit
goldenem Saum (`0xFFBF00`), der aufgeladene einen helleren Kern (`0x605030`) mit fast weißem
Saum (`0xFFF0A0`). Beide sind jetzt als `RENDER_TAU` und `RENDER_TAU_CHARGE` angelegt.

### Abweichungen

Der Kern kommt im Original von `BeamPronter` als gewellter Schlauch; der Port hat den nicht und
nimmt denselben Weg wie für alle übrigen Strahlen — dunkler Kern, heller Saum, die vier Farben
des Originals. Die Tau-Kanone hat wie die Spulenkanone **keinen Bauplan** und ist vorerst nur im
Kreativreiter zu haben.

**Damit ist `XFactoryAccelerator` vollständig** — Tau-Kanone, Spulenkanone und NI4NI.

Alle 38 Tore grün.

## Runde 204 — Der Fatman, und ein Name, den es zweimal gab

Zwei Dinge in einer Runde: der **CI-Fix zu Runde 203**, der einen ganz neuen Fehlertyp
aufdeckte, und **der Fatman** — die letzte Waffenlücke des Ports.

### Der Fehler, den kein Tor sah

Runde 203 war rot, und zwar nicht beim Übersetzen, sondern beim `runData`-Lauf:

```
Caused by: java.lang.IllegalArgumentException: Duplicate registration entity.ufo_blast
    at com.hbm.registry.NtmSoundEvents.reg(NtmSoundEvents.java:283)
    at com.hbm.registry.NtmSoundEvents.<clinit>(NtmSoundEvents.java:105)
```

Der Klang `entity.ufo_blast` stand zweimal da: seit der Teslakanone als `GUN_TESLA_BLAST`, und
seit Runde 203 noch einmal als frisch angelegtes `UFO_BLAST` für die überladene Tau-Kanone. Ein
`DeferredRegister` nimmt jeden Namen genau einmal; das zweite `register()` wirft aus dem
statischen Anfangsblock heraus, **noch bevor der Mod hochfährt**.

Für `javac` ist das unsichtbar — zwei Felder mit verschiedenen Bezeichnern, die zufällig
dieselbe Zeichenkette weiterreichen, sind gültiges Java. Alle 38 Tore waren grün. Auch das
Ton-Tor sah nichts, obwohl es genau diese Datei liest: es schreibt Feldname → Klangname in eine
Abbildung, und zwei Felder auf denselben Namen fallen darin **lautlos zusammen**.

Der Name ist jetzt einmal da und heißt nach dem Klang, nicht nach einer Waffe — drei Stellen
benutzen ihn: Teslakanone, Schockgranate und die überladene Tau-Kanone.

### Das 39. Tor: `dupreg-check.sh`

Es liest alle `DeferredRegister`-Felder des Baums und sammelt jeden Namen, der bei ihnen
ankommt — unmittelbar über `ANMELDER.register("name", …)` und über **Weiterreicher**: Methoden,
deren erster Parameter ein String ist und die genau diesen Parameter weitergeben
(`NtmBlocks.register`, `NtmSoundEvents.reg`, `NtmItems.registerPickaxe` und neun weitere). Ein
Weiterreicher wird bei seiner *erklärenden Datei* geführt, nicht bei seinem bloßen Namen, sonst
zieht irgendein `register(` im Baum Namen in ein Register, mit dem es nichts zu tun hat.

`NtmBlocks.register` meldet in **zwei** Register an — den Block und seinen BlockItem. Beide
Seiten werden geführt, damit auch ein Blockname auffällt, der mit einem Gegenstandsnamen
zusammenfällt.

**Gemessen:** 16 Anmelder, 12 Weiterreicher, 3346 Anmeldenamen, null Funde. Zwei Stellen kann
das Tor nicht lesen — die Schleife in `NtmFluidBridge` über `Fluids.metaOrder` — und **es sagt
sie am Ende selbst an**. Mit dem wieder eingesetzten zweiten `entity.ufo_blast`: genau ein Fund,
an den beiden richtigen Zeilen.

### Der Fatman

`XFactoryCatapult` enthielt nur `cluster_submunition` — das Geschoss, das die Streumunition des
Granatwerfers ausspuckt. Der Werfer selbst fehlte **ohne jede Begründung im Quelltext**.

Die Munition gab es längst: `NtmItems.AMMO_STANDARD` ist ein `EnumMultiItem` über
`GunFactory.Ammo`, und alle sechs `NUKE_`-Werte stehen dort samt Platz in `ORDER` und damit im
Kreativreiter. Es fehlte nur das Sprengverhalten. Auch Modell (`fatman.obj`, sechs Teile) und
alle drei Texturen lagen seit Runde 155 im Baum und in `ResourceManager`.

**Die sechs Sprengköpfe** unterscheiden sich nur im Aufschlag, nicht im Flug:

| Kopf | Wirkung |
|---|---|
| `NUKE_STANDARD` | Kugelblitz ohne Blockschaden, Strahlung über fünf mal fünf Chunks |
| `NUKE_DEMO` | größer, setzt Blöcke in Brand, anderthalbfache Strahlung |
| `NUKE_HIGH` | eine wirkliche Kernexplosion — `NukeExplosionMK5` mit Stärke 35 |
| `NUKE_TOTS` | acht kleine Köpfe auf einmal, je 35 % Schaden |
| `NUKE_HIVE` | zwölf noch kleinere, **ohne Strahlung und ohne Pilz** |
| `NUKE_BALEFIRE` | Blauflamme: verwandelt Blöcke, statt sie wegzureißen |

Der Pilz kommt über das `AuxParticle`-Paket mit `"type"="muke"` beziehungsweise `"tinytot"`,
genau wie bei der Granate und den Bomben — `NukeTorexCreator` bleibt den großen Sprengköpfen
vorbehalten, der Fatman benutzt ihn auch im Original nicht. Blau brennt der Pilz bei Polaroid 11
oder mit einem Prozent Glück; **nur der Blauflammen-Kopf erzwingt ihn.**

**Der Renderer** hat fünf einzeln bewegliche Teile. Der Zeiger sitzt am Griff und geht mit ihm
mit; der Stempel steht drei Einheiten vorn, wenn das Rohr leer ist — daran sieht man der Waffe
an, ob sie geladen ist. Der Blauflammen-Kopf **glitzert**: das Original malt ihn dreimal mit
verschobener Texturmatrix übereinander, der Port hat dafür `RenderMiscEffects.renderClassicGlint`,
das schon am Blauflammen-Sprengsatz hängt.

Zwei Klänge sind nachgereicht (`weapon.fire.fatman`, `weapon.reload.fatmanfull`), und **einen
Bauplan hat er** — anders als Tau- und Spulenkanone: Saturnit durchweg, der Griff aus hartem
Kunststoff.

**Damit stehen alle Waffenfabriken des Ports.**

Zur Bauplan-Lage aus Runde 203, jetzt nachgemessen: die **Spulenkanone hat auch im Original
keinen** — dass sie im Port nur im Kreativreiter liegt, ist also keine Abweichung. Die
**Tau-Kanone hat einen**, und er ist nachgereicht — siehe die Berichtigung unten.

### Berichtigung, noch in derselben Runde

Ich hatte hier zunächst geschrieben, der Tau-Bauplan sei blockiert: er verlangt
`coil_copper_torus`, und der Port kenne nur `coil_copper` und `coil_copper_ring`. **Das war
falsch, und zwar auf die immer gleiche Weise** — `COIL_COPPER_RING` *ist* der Torus, nur
umbenannt, mit demselben Amboss-Rezept wie im Original (zwei Kupferspulen auf Stufe 1). Auch
die übrigen sechs Zutaten lagen alle im Baum. Der Bauplan steht jetzt drin.

Das ist in dieser Runde das zweite Mal, dass eine Begründung beim Nachmessen zerfällt — beim
Fatman stand gar keine da, und die eine, die ich selbst geschrieben habe, hielt keine Stunde.

Alle 39 Tore grün.

## Runde 206 — Die Masken bekommen ihre Köpfe

Der offene Punkt aus Runde 136: „Die Kopfmodelle (`ModelGasMask`, `ModelM65`) sind nicht
portiert; die Masken bleiben am Körper unsichtbar." Er ist zu.

### Warum jede Maske jetzt einen eigenen Werkstoff hat

Das war die eine Stelle, an der 1.21 wirklich anders funktioniert. In 1.7.10 liefert
`getArmorModel` das Modell und `getArmorTexture` die Textur — zwei Methoden am selben
Gegenstand, unabhängig voneinander. In 1.21 liefert `getGenericArmorModel` nur noch das
Modell; **die Textur kommt aus der Rüstungsschicht des Werkstoffs**, die
`HumanoidArmorLayer` bindet, bevor sie das Modell zeichnet.

Ein Modell, das seine Textur selbst binden will, kommt damit nicht durch. Die HEV- und
RPA-Rüstungen umgehen das, weil sie OBJ-Modelle sind und über `RenderContext` unmittelbar
zeichnen; ein Kastenmodell aus `ModelPart` schreibt dagegen in den `VertexConsumer`, den die
Schicht ihm hinhält — und der hängt an der Textur der Schicht.

Also bekommt jede Maske mit Modell ihren eigenen Werkstoff (`mask_gas`, `mask_m65`,
`mask_mono`), gleich in allen Schutzwerten und verschieden allein in der Schichttextur. Die
Modelltexturen des Originals liegen dafür als `gas_mask_layer_1.png` (64×32),
`gas_mask_m65_layer_1.png` und `gas_mask_mono_layer_1.png` (je 32×32) — die Maße stimmen mit
den `LayerDefinition`s überein, das war die Gegenprobe.

### Die Übersetzung aus Techne

Nach demselben Verfahren wie beim Satellitenempfänger in Runde 105:
`new ModelRenderer(this, u, v)` wird `texOffs(u, v)`, `addBox` bleibt `addBox`,
`setRotationPoint` plus `setRotation` werden `PartPose.offsetAndRotation`, Drehreihenfolge
Z→Y→X.

**Dieselbe Falle wie damals, und diesmal auf jedem einzelnen Kasten:** beide Modelle setzen
`mirror = true` — aber *nach* `addBox`. In 1.7.10 liest `addBox` das Feld, die Zeile kommt zu
spät und tut nichts. Ein mechanisches `.mirror()` hätte beide Masken gespiegelt. Es steht
nirgends eines.

`convertToChild` ist nicht mitgekommen, und das ist kein Weglassen: der Elternkasten `mask`
sitzt in beiden Modellen auf (0\|0\|0) ohne Drehung, die Umrechnung zieht also überall null ab.
Die Zahlen des Originals stehen unverändert da.

### Die Filterdose verrät, wer ungefiltert atmet

Die M65 hat zwei Gruppen, und das ist keine Ordnungsfrage: `mask` ist die Haube, `filter` sind
Anschluss und Dose — und **die Dose wird nur gezeichnet, wenn auch eine drinsteckt**. Ohne
Filter bleibt am Gesicht nur der nackte Stutzen. Das Original fragt dafür
`ArmorUtil.getGasMaskFilterRecursively`, und zwar rekursiv, weil der Filter auch im
Helmaufsatz stecken kann; der Port hat diese Methode seit Runde 136.

Zwei Kästen der M65 haben **Tiefe null** — die Sichtscheiben. Das ist Absicht des Originals,
kein Rundungsfehler, und steht wortgetreu so da.

### Was offen bleibt

Schutzbrille und Aschebrille warten weiter — aber nicht mehr auf diese Modelle, sondern auf
ihr eigenes (`ModelGoggles`). `gas_mask_olde` hat im Original gar kein Modell, sondern eine
gewöhnliche Rüstungsschicht; er bleibt deshalb auf dem unsichtbaren Werkstoff, bis seine
Schichttextur nachgereicht ist.

> **Die Schutzbrille kam in Runde 209.** Die Aschebrille gibt es im Original gar nicht als
> eigenen Gegenstand — siehe dort.

Alle 39 Tore grün.

## Runde 207 — Der Bleianzug der Liquidatoren

Die letzte fehlende Rüstungsgarnitur des Ports — und zwar gemessen, nicht geschätzt.

### Warum „73 fehlende Rüstungen" nicht stimmt

`tools/port-gap.py` meldet 73 fehlende Rüstungsklassen. Die Zahl ist eine **obere Schranke aus
einem Klassennamen-Vergleich**, und sie zählt zu hoch: `ArmorFSB` heißt im Port `ArmorFSBItem`,
`ArmorHazmat` und `ArmorGasMask` sind zu einer Klasse `GasMaskItem` zusammengefallen. Der
Vergleich über die **Registriernamen** der Rüstungs-Konstruktoren — also über das, was im Spiel
wirklich ankommt — lässt den Liquidator-Anzug als nächste Lücke übrig: `liquidator_helmet`,
`liquidator_plate`, `liquidator_legs`, `liquidator_boots`.

> **BERICHTIGUNG AUS RUNDE 210.** Hier stand zuerst, der Liquidator sei die **einzige**
> fehlende Garnitur. Das war falsch. Mein Muster las nur einzeilige Konstruktoren aus
> `ModItems.java` und fand dort 17 Namen; die 124 weiteren stehen in `ModItemsArmor.java`,
> vielzeilig, und blieben ungesehen. Richtig gemessen fehlen **103 von 141** Rüstungsnamen —
> T-51, Schrabidium, Euphemium, die AJR-Reihe und zwei Dutzend weitere. Der Liquidator war die
> nächste Lücke, nicht die letzte. `port-gap.py` misst das seit Runde 210 selbst.

### Der Anzug

Er wird nicht neu gebaut, sondern um den grauen Hochleistungs-Schutzanzug herumgelegt: Gummi
außen, Bleiauskleidung innen. Das macht ihn so schwer, dass ihn nichts mehr umwirft
(Rückstoßfestigkeit +100) und sein Träger merklich langsamer geht (Tempo −0,1). Sein
Strahlenschutz ist mit 99,6 % der beste außerhalb des HEV-Anzugs.

Die **Haube ist zugleich Gasmaske**, und als volle Haube kennt sie keine Ausnahme: was der
Filter kann, hält sie ab. Das Original sagt das in einem Einzeiler — `return new ArrayList()`
mit dem Kommentar „full hood has no restrictions".

Am Körper trägt sie das **M65-Kopfmodell**, dasselbe wie die M65-Gasmaske. Dass das überhaupt
geht, ist der Ertrag von Runde 206; vorher wäre der Helm an einem fehlenden Modell gescheitert.
Aus demselben Grund wie dort hat der Helm einen eigenen Werkstoff (`liquidator_hood`), dessen
Schicht auf `liquidator_helmet.png` zeigt, während Weste, Hose und Stiefel sich `liquidator`
mit den gewöhnlichen Schichten teilen. Die Helmtextur ist 32×32 — dieselbe Größe wie die
M65-`LayerDefinition`, was die Zuordnung bestätigt.

### Die Weste ist nicht baubar, und das hat einen gemessenen Grund

Drei der vier Muster sind übernommen. Das vierte, die Weste, verlangt zwei leere Gasflaschen —
und die waren zum Zeitpunkt dieser Runde noch nicht portiert: `CD_Gastank` stand in `Fluids`
und wurde von einem Dutzend Fluiden benutzt, das Flaschenpaar selbst war nie mitgekommen. Das
war eine eigene Lücke, keine dieses Anzugs; bis sie zu war, gab es die Weste nur im
Kreativreiter.

> **Zu in Runde 208.** Die Gasflasche ist nachgereicht, die Weste ist baubar, der Anzug
> vollständig. Siehe dort.

### Zwei Registrierungen, die etwas tun — und eine, die nichts täte

Übernommen sind die Gefahrenklassen der Haube (`LIGHT`, `SAND`) und der Strahlenschutz aller
vier Teile. **Nicht übernommen** ist die dritte Zeile des Originals:
`DamageResistanceHandler.registerSet(..., new ResistanceStats())` — ein leeres Statistikobjekt,
also ein Eintrag ohne jede Wirkung. Im Port kommen die Satzwerte ohnehin aus der
Konfigurationsdatei, nicht aus fest verdrahteten Aufrufen; die Zeile hätte dort nichts getan
und nur so ausgesehen, als täte sie etwas.

Ebenfalls nicht mitgekommen sind `setStep`, `setJump` und `setFall`. Die drei Geräusche hängen
an einem Teilsystem, das der Port nicht hat — `ArmorFSBItem` sagt das im Kopf ausdrücklich für
alle FSB-Anzüge.

Alle 39 Tore grün.


## Runde 208 — Die Gasflasche, und ein Merkmal ohne Wirkung

Die Lücke, die Runde 207 eine Stunde vorher als eigene benannt hatte: das Flaschenpaar aus
leerer und voller Gasflasche. Es ist zu, und damit ist auch die Liquidatorweste baubar.

### Ein Container, den niemand las

`CD_Gastank` stand seit jeher in `Fluids` — **21 Fluide bringen ihn mit**, jedes mit zwei
Farben (`bottleColor`, `labelColor`). Gelesen hat ihn nichts. Das ist genau die Fehlerform, die
dieser Port sonst umgekehrt sucht: kein Effekt ohne Ursache, aber hier lag eine Ursache ohne
jeden Effekt.

Die Flasche ist das Gegenstück des Kanisters: der Kanister nimmt, was ein `CD_Canister` hat,
die Flasche, was ein `CD_Gastank` hat. Beide Abfragen stehen im Original unmittelbar
nebeneinander, und jetzt auch hier.

### Ein Unterschied zum Original, der heute nichts ändert — und trotzdem dasteht

Im Original stehen beide Abfragen **vor** den Wächtern `hasNoContainer` und
`needsLeadContainer`; im Port stehen sie dahinter. Nachgemessen macht das heute keinen
Unterschied: **keines der 21 Fluide mit `CD_Gastank` trägt `FT_NoContainer` oder
`FT_LeadContainer`**. Bekäme eines von beiden je eines dieser Merkmale, verlöre es hier still
seine Flasche — deshalb steht die Messung als Kommentar an der Stelle und nicht bloß in diesem
Text.

### Drei Schichten statt drei Durchgängen

Das Original malt die volle Flasche in drei Darstellungsdurchgängen: Rumpf ungefärbt,
Flaschenkörper in `bottleColor`, Etikett in `labelColor`. In 1.21 sind das drei
Texturschichten mit je einem eigenen Farbton — dieselbe Aufteilung, nur anders benannt. Der
Kanister im Port hat genau zwei; die Flasche ist die erste Stelle, die eine dritte braucht.

Alle 39 Tore grün.


## Runde 209 — Die Schutzbrille, und ein Schirmbild, das zwei Hauben verloren hatten

Der letzte offene Punkt aus Runde 136, und dazu ein Fund, den erst die Suche danach zutage
gebracht hat.

### Die Brille selbst

`goggles` gab es im Port **gar nicht** — nicht nur ihr Modell fehlte, sondern der Gegenstand.
Sie ist kein Filtergerät, sondern Glas vor den Augen: sie hält Licht und Sand ab, und mit
zunehmendem Verschleiß trübt sie die Sicht über dieselbe sechsstufige Bildleiter, die auch die
M65 benutzt.

Das Modell sind fünf Kästen — Blende, Körper, zwei Gläser, Band — nach demselben
Techne-Verfahren wie die beiden Masken in Runde 206, mit derselben `mirror`-Falle auf jedem
Kasten. Anders als Gasmaske und M65 wird sie **unskaliert** gezeichnet: sie sitzt eng am Kopf,
und genau so soll sie sitzen.

Der Tippfehler des Originals ist nicht mitgekommen: dort heißt die Gruppe `google`.

### Der Fund: zwei Hauben ohne Schirmbild

Beim Nachlesen, welche Gegenstände das Brillenbild benutzen, zeigte sich etwas anderes.
`ArmorModel.renderHelmetOverlay` prüft im Original auf **drei** Gegenstände:

```java
if(this != ModItems.goggles && this != ModItems.hazmat_helmet_red && this != ModItems.hazmat_helmet_grey) return;
```

Die rote und die graue Schutzhaube legen dem Träger also dasselbe Brillenbild vor wie die
Brille. Im Port hatten beide **gar kein** Schirmbild — sie waren mit `List.of()` und ohne
Vorsatz angemeldet. Das ist die stille Sorte Abweichung, die kein Tor sieht: der Gegenstand
funktioniert, er sieht nur anders aus als er soll. Beide haben ihr Bild jetzt.

### Die Aschebrille

Die stand in der Liste aus Runde 136 („Schutzbrille und Aschebrille warten auf dieselben
Modelle").

> **BERICHTIGUNG AUS RUNDE 211.** Hier stand, die Aschebrille gebe es im Original gar nicht.
> Das war falsch, und zwar aus demselben Grund wie der Irrtum in Runde 207: **ich habe nach
> der falschen Schreibweise gesucht.** Der Gegenstand heißt `ashglasses`, ohne Unterstrich;
> ich hatte `ash_glasses` gegriffen, nichts gefunden und daraus geschlossen, es gebe ihn
> nicht. Es gibt ihn — mit Feld, Registrierung, Konstruktor, Modell und Textur. Er ist in
> Runde 211 nachgereicht.

Alle 39 Tore grün.


## Runde 210 — Das Messwerkzeug log, und ich habe ihm geglaubt

Keine neue Sache im Spiel, sondern eine Berichtigung und ein schärferes Werkzeug.

### Was falsch war

In Runde 207 steht: „Der Vergleich über die Registriernamen lässt genau eine Garnitur übrig."
Das war **falsch**. Ich hatte die Namen mit einem Muster geholt, das `new Armor…(…)` und
`setUnlocalizedName("…")` **auf derselben Zeile** verlangt. In `ModItems.java` stehen die
Rüstungen so — 17 Stück, im Wesentlichen die Hazmat-Familie und der Liquidator. Die übrigen
**124 stehen in `ModItemsArmor.java`**, über mehrere Zeilen verteilt, und mein Muster hat sie
nicht gesehen.

Richtig gemessen: **141 Rüstungsnamen im Original, 38 davon im Port, 103 fehlen** — T-51,
Schrabidium, Euphemium, Cobalt, Titan, Stahl, die AJR-Reihe, der Trenchmaster und zwei Dutzend
weitere. Der Liquidator war die *nächste* Lücke, nicht die letzte.

Ärgerlich daran ist nicht der Irrtum, sondern dass er dieselbe Form hat wie die Irrtümer, die
diese Runden sonst aufdecken: **eine Zahl, die aus einer zu engen Messung stammt und dann als
Tatsache weitergereicht wird.** Nur kam sie diesmal von mir.

### Was das Werkzeug jetzt tut

`tools/port-gap.py` behauptete im Kopf, der Klassenvergleich sei „belastbar". Für
Blockentitäten stimmt das meistens; für Rüstungen stimmt es nicht, weil der Port
zusammenfasst, wo das Original je Anzug eine Klasse schreibt. Die Überschrift heißt jetzt
**obere Schranke**, und der Kopf nennt die Messung, die das belegt.

Dazu kommt ein dritter Block, **REGISTRIERNAMEN**, der zählt, was im Spiel ankommt statt
Dateien im Baum. Er vergleicht gegen *alle* Gegenstandsnamen des Ports, nicht gegen die, deren
Konstruktor nach Rüstung aussieht — sonst fielen `hev_helmet` und `rpa_helmet` durch, die der
Port über Hilfsmethoden anmeldet.

| Messung | meldet fehlend |
|---|---|
| Klassenvergleich | 73 |
| Registriernamen | **103** |
| meine Handmessung aus Runde 207 | 1 |

Die dritte Zeile ist der Grund für diese Runde.

Alle 39 Tore grün.


## Runde 211 — Die Aschebrille, und schon wieder die falsche Schreibweise

Zwei Runden hintereinander habe ich eine Lücke wegmessen wollen und mich dabei vermessen.
Runde 210 war die Berichtigung der einen, diese ist die Berichtigung der anderen.

### Was falsch war

In Runde 209 steht: „die Zeichenkette `ash_glasses` kommt im ganzen Original nicht ein
einziges Mal vor". Das stimmt sogar — **nur heißt der Gegenstand nicht so.** Er heißt
`ashglasses`, ohne Unterstrich. Ich hatte die Schreibweise aus der Klassenliste von
`port-gap.py` abgeleitet (`ArmorAshGlasses`) und beim Suchen einen Unterstrich eingefügt, den
es nie gab. Der Gegenstand ist im Original vollständig vorhanden: Feld, Registrierung,
Konstruktor, Modell, Item- und Rüstungstextur.

Bemerkenswert daran ist, dass die richtige Schreibweise **die ganze Zeit sichtbar war** — sie
steht in der 103er-Liste, die Runde 210 ausgerechnet hat, an vierzehnter Stelle: `ashglasses`.
Ich hatte sie gelesen, ohne sie mit der Behauptung aus Runde 209 zusammenzubringen.

### Die Brille

Sie kann nichts. Das Original hängt ihr weder Gefahrenklassen noch Strahlenschutz noch ein
Schirmbild an — ein gewöhnlicher Helmgegenstand aus Eisen mit eigenem Kopfmodell.

Anders als die Masken aus Runde 206 und 209 braucht sie **keinen eigenen Werkstoff**: ihr
Modell ist ein Wellenfrontmodell und bindet seine Textur selbst über `RenderContext`, statt sie
von der Rüstungsschicht zu beziehen — derselbe Weg, den HEV- und RPA-Rüstung gehen. Der ganze
Umstand aus Runde 206 entfällt damit.

**Sieben tote Zuweisungen sind nicht mitgekommen.** `ModelGlasses` legt im Original neben dem
Kopf auch Rumpf, Arme, Beine und Füße aus dem BJ-Rüstungsmodell an — und zeichnet davon in
`render()` nie eines, weil die Brille nur den Kopfschlitz belegt. Deshalb braucht der Port die
BJ-Modelldatei für sie auch nicht, sondern allein `goggles.obj`.

Und eine Verwechslungsgefahr, die im Original angelegt ist: die **Aschebrille** (`ashglasses`)
benutzt die Dateien, die dort `goggles.obj` und `goggles.png` heißen — während die
**Schutzbrille** (`goggles`) aus Runde 209 ein Kastenmodell ist. Zwei Gegenstände, deren Namen
und Dateien über Kreuz liegen. Im Port steht das an beiden Klassen als Warnung.

Alle 39 Tore grün.

## Runde 212 — Dreiunddreißig Rüstungsteile, die nichts können

Die erste Familie aus der 103er-Liste von Runde 210, und die einfachste: acht Garnituren plus
eine einzelne Hose, allesamt `ArmorFSB` **ohne einen einzigen Zusatz** — kein Satzbonus, keine
Trankwirkung, kein Schirmbild, kein eigenes Modell. Sie sind genau das, wonach sie aussehen.

| Garnitur | Haltbarkeit | Verzauberbarkeit | Bauplan |
|---|---|---|---|
| Stahl | 30 | 5 | ja |
| Titan | 25 | 9 | ja |
| Legierung | 40 | 12 | **nein** |
| Kobalt | 70 | 60 | ja |
| Sternmetall | 150 | 100 | **nein** |
| Sicherheit | 100 | 15 | ja |
| Dineutronium | 3 | 0 | ja |
| Robe | 15 | 12 | ja |
| Zirkonium (nur Hose) | 1000 | 1000 | ja |

### Drei Stellen, an denen Abschreiben falsch gewesen wäre

**Die Robe hat keinen eigenen Werkstoff.** Das Original reicht `ArmorMaterial.CHAIN` durch —
die Kettenrüstung des Grundspiels, also {2, 5, 4, 1} statt der {3, 8, 6, 3}, die alle anderen
hier haben. Ich hatte zuerst {3, 8, 6, 3} hingeschrieben, weil alle Nachbarzeilen das sagen;
das wäre eine stillschweigend stärkere Robe geworden. Nur die Rüstungsschicht ist bei ihr
eigen.

**Die Legierung lässt sich nicht ausbessern.** `aMatAlloy` hat im Original kein
`customCraftingMaterial` — passend dazu, dass der Werkstoff dort `@Deprecated` ist und
`MAT_ALLOY` in `Mats` auskommentiert. Die Zutat bleibt darum auch hier leer, statt
ersatzweise Eisen einzusetzen.

**`HBM_DNT_LOLOLOL`** heißt wirklich so, und die Werte passen dazu: {1, 1, 1, 1},
Haltbarkeitsfaktor 3, Verzauberbarkeit 0. Die Garnitur ist ein Scherz und soll einer bleiben —
nichts daran ist ein Tippfehler, den man glattziehen müsste.

### Zwei ohne Bauplan, und das ist keine Lücke

Legierung und Sternmetall haben im Original **keinen** Bauplan: die eine ist veraltet, die
andere kommt aus Beute. Beide bleiben deshalb auch hier ohne — das ist der Stand des Originals.

> **BERICHTIGUNG AUS RUNDE 213.** Für das Sternmetall stimmt das nicht. Es hat sogar **zwei**
> Baupläne, je nach Einstellung. Mein Suchbefehl war auf zehn Zeilen gekürzt und hat die
> Sternmetallzeilen gar nicht erst erreicht — derselbe Fehler wie in den Runden 207 und 209,
> nur diesmal durch ein `head -10` statt durch eine falsche Schreibweise. Der Bauplan ist in
> Runde 213 nachgereicht. Für die Legierung bleibt es richtig: sie hat keinen.
Die übrigen sieben haben ihren, wortgetreu übernommen; Kobalt legt sich dabei Stück für Stück
um die Stahlrüstung, was die Reihenfolge im Fortschritt festlegt.

Damit sind von den 103 Namen aus Runde 210 noch **70** offen.

Alle 39 Tore grün.


## Runde 213 — Vier Garnituren mit je einem Zusatz, und die dritte Fehlmessung

Die zweite Familie aus der 103er-Liste: fünfzehn Teile, die sich von den dreiunddreißig aus
Runde 212 in genau einem Punkt unterscheiden — jede bringt etwas mit.

| Garnitur | Zusatz |
|---|---|
| Asbest | ein Schirmbild (`overlay_asbestos`) |
| Kombinationsstahl | Tempo II, Eile II, Stärke V für den Satz |
| Schrabidium | Eile III, Stärke III, Sprungkraft II, Tempo III |
| PAA | Eile I — **und kein Helm** |

**Die PAA-Rüstung hat keinen Helm, und das ist Absicht.** Das Original setzt `setNoHelmet(true)`,
damit der Satzbonus schon mit Weste, Hose und Stiefeln zählt. `ArmorFSBItem` kann das seit
Runde 101; hier wird es zum ersten Mal benutzt. Nicht zu verwechseln mit `hazmat_paa_*`, dem
Schutzanzug aus derselben Legierung — den hat der Port seit Runde 136.

Das Schirmbild war das Einzige, was gefehlt hat: `ArmorFSBItem` kannte `addEffect` und
`setNoHelmet`, aber keinen Vorsatz. Es implementiert jetzt `IHelmetOverlayItem` wie die Masken,
mit einem `setOverlay` für die eine Rüstung, die es braucht.

### Ein Gegenstand kam mit

`asbestos_cloth` fehlte dem Port — die Asbestrüstung braucht ihn zum Bauen *und* zum
Ausbessern. Gewebt wird er wie das Schutztuch in der Montagefabrik, ein Asbestbarren auf acht
Faden (`ass.firecloth`).

### Die dritte Fehlmessung in sieben Runden

Runde 212 schrieb, Sternmetall habe im Original keinen Bauplan. Es hat zwei. Mein Suchbefehl
endete auf `head -10` und hat bei Zeile 82 aufgehört — die Sternmetallzeilen stehen bei 165
und 174.

Das ist derselbe Fehler wie in Runde 207 (zu enges Muster) und Runde 209 (falsche
Schreibweise), zum dritten Mal in sieben Runden, und jedes Mal in dieselbe Richtung: **eine
Lücke wird für kleiner erklärt, als sie ist.** Die Lehre daraus steht schon in Runde 210 —
sie war nur noch nicht auf meine eigenen Suchbefehle angewandt.

### Die Aufstiegskette

Das Original bietet zwei Wege an und stellt über `enableLBSMSimpleArmorRecipes` um: entweder
jede Garnitur schlicht aus ihrem Barren, oder jede aus der vorigen. Der Port nimmt die Kette,
weil sie der Standardfall ist und Runde 212 den Kobaltsatz schon so gebaut hat:

**Stahl → Kobalt → Sternmetall → Schrabidium**

Damit sind von den 103 Namen aus Runde 210 noch ~~55~~ **54** offen.
*(Berichtigung aus Runde 214: nachgemessen auf `0353b37d` sind es 54. Die 55 war
gezählt, nicht gemessen.)*

Alle 39 Tore grün.

## Runde 214 — Die T-51, die erste Panzerrüstung der Liste

Die T-51 ist die erste Garnitur der Restliste, die *nicht* bloß eine Zahlenreihe ist: eine
Panzerrüstung mit OBJ-Modell, eigener Energieversorgung und vier Texturen. Damit beginnt der
teure Teil — die 50 Namen, die danach noch offen sind, brauchen alle dasselbe.

### Was die Rüstung kann

| | |
|---|---|
| Modell | `armor/t51.obj`, acht Teile (Helm, Brust, zwei Arme, zwei Beine, zwei Stiefel) |
| Texturen | vier: Helm, Brust, Arm, Bein |
| Energie | 1 000 000 HE, 10 000 Ladung, 1000 Verbrauch, 5 Abfluss |
| Wirkung | Stärke I, solange sie Strom hat |
| Schutz | `ArmorUtil.FULL_NO_LIGHT` auf allen vier Teilen — alles außer Blendschutz |
| Strahlung | 90 % Abschirmung, mit Geigerton |

`FULL_NO_LIGHT` ist neu: das vollständige Schutzpaket ohne `LIGHT`. Die T-51 ist die erste
Rüstung des Ports, die alles abhält außer dem Blitz.

### Das Gegenstandsbild ist Absicht

Alle vier Teile tragen dasselbe Bild, `armor.png`. Das ist keine Auslassung — das Original
gibt jeder Panzerrüstung dieses eine Bild über `setTextureName(":armor")`, und weder dort
noch in der CE-Abspaltung gibt es ein eigenes Bild pro Garnitur.

### Nicht übernommen

`enableVATS`, `setHasHardLanding`, `setStep`/`setJump`/`setFall` und
`hides(EnumPlayerPart.HAT)` — dieselben fünf, die schon beim HEV-Anzug (Runde 101)
draußen geblieben sind, aus demselben Grund: die zugehörigen Haken gibt es im Port noch nicht.
Sie stehen als Ursache ohne Wirkung im Original und würden hier als Wirkung ohne Ursache
landen.

### Die Baupläne

Jedes Stück wird um das entsprechende Titanstück herumgebaut (`ArmorRecipes.java` Z. 61–64):

| Stück | Muster | Zusätzlich |
|---|---|---|
| Helm | `PPC` / `PBP` / `IXI` | Leiterplatte, Kautschuk, **M65-Maske** |
| Brust | `MPM` / `TBT` / `PPP` | zwei Motoren, **zwei leere Gasflaschen** |
| Hose | `MPM` / `PBP` / `P P` | zwei Motoren |
| Stiefel | `P P` / `PBP` | — |

Die leere Gasflasche in der Brustplatte ist erst seit Runde 208 im Port. Hätte ich die T-51
vorher angefasst, wäre genau dieser Bauplan blockiert gewesen — jetzt ist er es nicht.

### Eine vierte Fehlmessung, diesmal in die andere Richtung

Runde 213 schrieb, es seien noch 55 Namen offen. Nachgemessen auf demselben Commit sind es
54. Die drei Fehlmessungen davor (207, 209, 212) haben die Lücke jeweils **zu klein**
erklärt; diese hier hat sie zu groß erklärt. Beides kommt aus derselben Quelle: eine Zahl
fortschreiben statt sie neu messen.

Nach dieser Runde sind **50** offen: `ajr_*`, `ajro_*`, `bismuth_*`, `bj_*`
(+ `bj_plate_jetpack`), `dieselsuit_*`, `dns_*`, `envsuit_*`, `euphemium_*`, `fau_*`,
`nossy_hat`, `steamsuit_*`, `taurun_*`, `trenchmaster_*`.

*(Berichtigung aus Runde 215: hier stand „alle davon OBJ-Panzerrüstungen". Vier davon,
`euphemium_*`, sind es nicht — sie sind schlichtes `ItemArmor` mit zwei Schichttexturen.
Siehe Runde 215.)*

Alle 39 Tore grün.

## Runde 215 — Der Euphemium-Satz, und eine Behauptung aus der Runde davor

Runde 214 hat die Restliste zu schnell abgeschrieben: „alle davon OBJ-Panzerrüstungen".
Nachgemessen ist das für 46 der 50 richtig und für vier falsch. Der Euphemium-Satz ist im
Original ein schlichtes `ItemArmor` mit zwei Schichttexturen — genau die Form, die Runde 212
dreiunddreißig Mal gebaut hat.

### Woher der Fehler kam

Ich habe nach Modellklassen gesucht, deren Name den **Satznamen** enthält:

    grep -iE "Model.*(Ajr|Bismuth|Bj|Diesel|Dns|Envsuit|Euphemium|Fau|Steam|Taurun|Trench)"

Acht Treffer, siebzehn Namen ohne Treffer, und daraus der Schluss „die haben keine Modelle".
Das war zweifach falsch. Die Modelle von `steamsuit_*`, `fau_*` und `dns_*` heißen nach dem
**Material**, nicht nach der Garnitur: `ModelArmorDesh`, `ModelArmorDigamma`, `ModelArmorDNT`.
Und `nossy_hat` hat auch eins, versteckt in `ArmorModel.getArmorModel`.

Das ist derselbe Fehler wie in Runde 209 — nach der falschen Schreibweise gesucht und das
Fehlen des Treffers für das Fehlen der Sache genommen. Die Lehre aus Runde 210 lautet: über
**Registriernamen** vergleichen, nicht über Klassennamen. Die Klasse, die `dns_helmet`
zeichnet, heißt nirgends `dns`.

Diesmal hat der Irrtum ausnahmsweise Arbeit freigelegt statt versteckt: vier Namen, die ich
für teuer gehalten habe, waren billig.

### Was der Satz kann

| | |
|---|---|
| Schutz | `{3, 8, 6, 3}`, Verzauberbarkeit 100 |
| Satzbonus | Regeneration, Widerstandskraft, Feuerschutz, Sättigung — alle in Stufe 128 |
| Fallschutz | die Fallgeschwindigkeit wird bei −0,25 gekappt, die Fallhöhe zurückgesetzt |
| Haltbarkeit | keine |
| Strahlung | Faktor 10, im Original mit dem Vermerk „<100 %" |

**Er geht nicht kaputt.** Das Original erreicht das mit einem leergelassenen `setDamage`: die
Haltbarkeitsleiste steht da und bewegt sich nie. Der Port gibt dem Gegenstand stattdessen gar
keine Haltbarkeit — dasselbe Ergebnis am Spieler, ohne die Leiste, die ohnehin nie sinkt.

**Er sitzt auf `ArmorFSBItem`, obwohl das Original ein schlichtes `ItemArmor` ist.** Die
Satzprüfung ist dieselbe Sache: das Original zählt die vier Gegenstände einzeln auf,
`ArmorFSBItem` vergleicht das Material der vier getragenen Teile — und `EUPHEMIUM` trägt
genau diese vier und sonst nichts. Zwei Satzprüfungen nebeneinander wären eine zu viel.

### Kein Kreativreiter — und trotzdem erreichbar

`ArmorEuphemium` ruft im Konstruktor `setCreativeTab(null)`. Die vier Teile stehen deshalb in
keinem Reiter, mit einem Satz Begründung in `tools/tab-check.sh`. Blockiert ist damit nichts:
die vier Baupläne an der Werkbank stehen im Port.

### Ein Gegenstand kam mit

Die **kaputte Taschenuhr** (`watch`) fehlte dem Port — sie steckt in der Brustplatte. Ihr
eigenes Rezept ist `LYL` / `EWE` / `LYL` aus blauem Farbstoff, Yharonit-Rohling,
Euphemiumbarren und einer gewöhnlichen Uhr. Wie im Original steht auch sie in keinem Reiter.

Damit sind noch **46** Rüstungsnamen offen — und die sind jetzt tatsächlich alle
OBJ-Panzerrüstungen. Diesmal nicht geschätzt: jeder der 46 Namen wurde auf seine
Konstruktorklasse abgebildet (dreizehn Klassen), und jede dieser dreizehn liefert ein
`getArmorModel` aus einer Klasse mit `ModelRendererObj`. `ArmorHat` erbt seines von
`ArmorModel` — genau die Art Fall, die der Klassennamen-Vergleich oben verschluckt hat.

Alle 39 Tore grün.

## Runde 216 — Der Stahlranger, und warum es nur eine Klasse braucht

Die erste der elf OBJ-Garnituren aus der Restliste — und sie bringt gleich zwei Namen mit:
`ajr_*` (im Spiel „Steel Ranger") und `ajro_*` („AJR Power Armor"). Acht Registriernamen in
einer Runde, weil die beiden Garnituren im Original **Zeile für Zeile dieselben** sind.

### Zwei Dateien, die sich nicht unterscheiden

`ArmorAJR` und `ArmorAJRO` sind im Original 52 Zeilen lang und bis auf vier Texturnamen
identisch. `ModelArmorAJR` und `ModelArmorAJRO` ebenso. Beide laden dasselbe
Wellenfrontmodell, `AJR.obj`.

Der Port schreibt das nicht ab. Es gibt **eine** Item-Klasse und **ein** Modell; welche der
beiden Garnituren ein Stück ist, steht in einem Aufzählungswert, und die vier Texturen dazu
stehen im Modell. Warum im Modell und nicht am Gegenstand: `ResourceManager` ist
clientseitig, die Anmeldung in `NtmItems` läuft auf beiden Seiten. Ein Texturfeld am
Gegenstand hätte den Server an eine Klasse gebunden, die es dort nicht gibt.

### Die orangene Spielart ist ein Anstrich

Im Original gibt es für `ajro_*` keine eigenen Baupläne, sondern vier formlose Rezepte:
fertiges AJR-Stück plus roter und schwarzer Farbstoff. Kein zweiter Bauweg, eine
Umlackierung — und ohne Weg zurück, im Original wie hier.

Die vier AJR-Baupläne selbst sind Muster für Muster dieselben wie bei der T-51 aus Runde 214,
nur mit der AJR-Platte statt der Titan-Panzerplatte, dem Deshmotor statt dem gewöhnlichen und
Kunststoff statt Kautschuk.

### Was der Satz kann

| | |
|---|---|
| Energie | 2 500 000 HE, 10 000 Ladung, 2000 Verbrauch, 25 Abfluss |
| Wirkung | Sprungkraft I und Stärke I, solange er Strom hat |
| Schutz | `FULL_PACKAGE` — anders als die T-51 auch gegen Blendung |
| Strahlung | 95 %, mit Geigerton |

### Ein Teil im Modell, das niemand zeichnet

`AJR.obj` enthält neun Teile, gezeichnet werden acht. Den `RocketBox` rührt auch im Original
keine der beiden Modellklassen an. Er bleibt ungezeichnet, hier wie dort — festgehalten, nicht
repariert.

### Vier Garnituren teilen sich einen Werkstoff

`HBM_T45AJR` trägt im Original AJR, AJRO, RPA und NCRPA. Die Satzprüfung vergleicht das
Material der vier getragenen Teile, ein AJR-Helm über einer RPA-Brustplatte zählt also als
vollständiger Satz. Das ist kein Fehler des Ports: `ArmorFSB.hasFSBArmor` vergleicht im
Original (Z. 226) genauso über `getArmorMaterial`. Der Kommentar am Werkstoff sagt das jetzt,
statt weiter „die beiden Panzerrüstungen" zu behaupten.

### Nebenbei repariert

Der Einschub von `FULL_NO_LIGHT` in Runde 214 hatte die Beschreibung von `FULL_PACKAGE` von
ihrem Feld getrennt — sie stand seitdem über der falschen Liste. Ein Kommentar am falschen
Ding ist schlimmer als keiner; beide stehen wieder bei dem, was sie beschreiben.

Nach dieser Runde sind **38** Rüstungsnamen offen.

Alle 39 Tore grün.

## Runde 217 — Die Taurun-Rüstung, und ein Satz ohne Bauplan

Vier Namen, und zum ersten Mal in dieser Reihe eine Garnitur, die im Original **kein einziges
Werkbankrezept** hat. Das ist keine Lücke: die Taurun-Rüstung ist Beute. Der Untote Soldat
trägt sie (`EntityUndeadSoldier`), und sie liegt in den Beutetöpfen der Halde
(`ItemPoolsPile`). Dazu passt, dass ihr Konstruktor `setMaxDamage(0)` setzt — sie geht nie
kaputt, weil man sie nicht nachbauen kann.

Der Port gibt ihr deshalb keine Haltbarkeit statt einer, die nie sinkt. Gleiches Ergebnis am
Spieler, und der Grund steht in der Klasse.

### Was sie kann

| | |
|---|---|
| Schutz | `{3, 8, 6, 3}`, Verzauberbarkeit 10, Reparatur mit der Eisenplatte |
| Wirkung | Stärke I für den Satz |
| Gefahren | `FULL_PACKAGE` |
| Strahlung | 25 % — der niedrigste Wert aller Wellenfront-Rüstungen |
| Haltbarkeit | keine |

### Die Beine stehen einen Hauch auseinander

Das Original schiebt vor dem linken Bein um −0,01 und vor dem rechten um +0,01 Welteinheiten
zur Seite, damit die Hälften nicht ineinander flimmern; dasselbe bei den Stiefeln. Im Port
zählt ein Drehpunkt in Sechzehnteln einer Welteinheit, also sind das ±0,16 — derselbe
Versatz, andere Einheit. Umgerechnet statt geschätzt, und die Rechnung steht im Modell.

### Nicht übernommen

`setStepSize(1)` und `hides(EnumPlayerPart.HAT)`. Eine Schritthöhe kennt der Port an Rüstung
nirgends (nachgemessen: kein einziges Vorkommen), das Ausblenden von Spielerteilen ebenso
wenig. Beides wäre ein Schalter, den niemand liest.

### Ein Namensdoppel im Original

`aMatTaurun` und `aMatTrench` werden beide als `"HBM_TRENCH"` angelegt. Im Port bekommt jeder
Werkstoff seinen eigenen Namen; festgehalten, weil es beim Grabenmeister wieder auffallen
wird.

### Wismut ist gemessen, aber blockiert

Die Wismut-Garnitur wäre die nächste kleine gewesen. Ihre Brustplatte braucht
`laser_crystal_bismuth`, und die ganze FEL-Kristallfamilie fehlt dem Port noch. Der Kristall
ist **nicht** unerreichbar — er hat ein eigenes Werkbankrezept (`CraftingManager` Z. 341:
Quarzglas, Uranbarren, Thorium-232, Wismutnugget, seltener Kristall) —, aber er ist ein
eigener Gegenstand und gehört in seine eigene Runde, nicht als Beifang in diese.

Nach dieser Runde sind **34** Rüstungsnamen offen.

Alle 39 Tore grün.

## Runde 218 — Die Wismut-Garnitur, und der Kristall, der sie aufgeschlossen hat

Runde 217 hat die Wismut-Garnitur als blockiert zurückgestellt: ihre Brustplatte braucht
`laser_crystal_bismuth`, und die FEL-Kristallfamilie fehlt dem Port. Die Blockade war echt,
aber klein — der Kristall hat ein eigenes Werkbankrezept, und alle fünf Zutaten liegen
längst im Port.

### Einer von fünf, und nur einer

Das Original hat fünf FEL-Kristalle. Der Port bekommt genau den einen, der gebraucht wird:
er steckt in der Wismut-Brustplatte und ist selbst baubar (`CraftingManager` Z. 341 —
Quarzglas, Uranbarren, Thorium-232, Wismutnugget, seltener Kristall). Die vier anderen
gehören zum Freie-Elektronen-Laser, den der Port nicht hat; sie wären Gegenstände ohne Zweck.

Aus demselben Grund bleibt die Wellenlängen-Anzeige der Originalklasse draußen. Sie
beschreibt, was der Kristall **im Laser** tut, und den gibt es hier noch nicht.

### Zierat, nicht Panzerung

Die Garnitur heißt im Spiel „Bismuth Headdress", „Shoulderpads, Necklace & Loincloth",
„Kneeguards" und „Sandals" — und sie verhält sich auch so:

| | |
|---|---|
| Satzbonus | Sprungkraft VII, Tempo VII, Regeneration II, Nachtsicht |
| Gefahrenschutz | **keiner** |
| Strahlung | **keine Abschirmung** |

**Sie schirmt gegen nichts ab, und das ist kein vergessener Eintrag.** Als einzige
Wellenfront-Rüstung des Ports trägt sie im Original weder `setHazardClass` noch
`setRadResist`. Sie steht deshalb weder in `ArmorUtil` noch in `HazmatRegistry` — eine
Abwesenheit mit Grund, festgehalten in der Klasse, damit sie niemand später „repariert".

### Zwei Fallen im Modell

- **Die Fußteile heißen `LeftFoot` und `RightFoot`**, nicht `LeftBoot`/`RightBoot` wie in
  T-51, AJR und Taurun. Wer eines der anderen Modelle abschreibt, bekommt unsichtbare Schuhe.
- **Eine einzige Textur für alle acht Teile**, statt der vier, die alle anderen haben. Sie
  wird einmal am Anfang gebunden.

Dazu zeichnet das Original hier als einzige Rüstung die Arme **vor** dem Rumpf. Übernommen,
ohne sie umzusortieren.

### Die Baupläne sind eigenwillig

Vier Muster, die mit keinem anderen Satz etwas gemein haben. Die Beinschützer bestehen sogar
nur aus Lumpen und Sternmetallringen — ganz ohne Wismut.

`setDashCount(3)` ist nicht übernommen, wie schon bei HEV, T-51 und AJR: einen Sprung nach
vorn kennt der Port nicht.

Nach dieser Runde sind **30** Rüstungsnamen offen.

Alle 39 Tore grün.

## Runde 219 — Der Fau-Anzug, und ein Block aus der Gruft

Wieder eine Garnitur, die an einem fehlenden Stück hing — und wieder war das Stück kleiner
als befürchtet. Die Fau-Brustplatte braucht einen Block **Uraltschrott**, und den hatte der
Port nicht. Die Klasse dahinter, `OutgasBlock`, gibt es aber längst, und das Gruftradon, das
der Schrott ausatmet, auch. Es fehlte nur der Block selbst.

### Der Uraltschrott

Dieselben drei Schalter wie das Koriumgestein (zufälliger Tick, Gas beim Abbau, Gas an
Nachbarn), Härte 100, Sprengfestigkeit 6000, Strahlung 150 — und Gruftradon statt gewöhnlichem
Radon.

**Wer ihn zerschlägt, steht in einer Wolke.** Das Original füllt einen Würfel von 5 × 5 × 5 um
die Bruchstelle, aber nur die Felder, deren Versatzsumme zwischen 1 und 4 liegt: das schneidet
die Ecken ab und lässt die Bruchstelle selbst aus, die der Wächter davor schon gefüllt hat.
Die Abfrage steht im Original mitten in der gemeinsamen Klasse, genau wie `getGas()` — im Port
ebenso, aus demselben Grund: nur dieser eine Block macht es.

### Der Anzug

| | |
|---|---|
| Energie | 10 000 000 HE, 10 000 Ladung, 2500 Verbrauch, **0 Abfluss** |
| Wirkung | Sprungkraft II für den Satz |
| Gefahren | `FULL_PACKAGE`, mit Geigerton |
| Strahlung | 99,99 % — der zweitbeste Wert des Mods, nur der DNT-Anzug ist besser |

### Neun Teile statt acht, und eines davon ist durchscheinend

Über der Brustplatte sitzt eine **Kassette** mit eigener Textur. Sie hat keinen eigenen
Drehpunkt, sondern übernimmt den des Rumpfes. Sie ist der einzige Rüstungsteil des ganzen
Ports, der mit Alphamischung gezeichnet wird.

**Hier weicht der Port bewusst ab.** Das Original schaltet die Mischung vor der Kassette ein
und nie wieder aus — alles, was danach gezeichnet wird, erbt sie. Der Port schaltet sie
hinterher ab. Einen durchgereichten Zeichenzustand nachzubauen wäre kein treuer Port, sondern
ein abgeschriebener Fehler.

### Nicht übernommen

`enableThermalSight`, `setHasHardLanding`, `setStep`/`setJump`/`setFall`, `hides(...)` und
`setFullSetForHide` — dieselbe Liste wie bei HEV, T-51 und AJR, nur um die Wärmesicht länger.

Nach dieser Runde sind **26** Rüstungsnamen offen.

Alle 39 Tore grün.

## Runde 220 — Der Grabenmeister, drei tote Aufrufstellen und eine Lücke aus Runde 215

Die Grabenmeister-Rüstung ist die erste dieser Reihe, die **Verhalten** mitbringt statt nur
Werte — und dafür brauchte der Port zwei Haken, die es noch nicht gab.

### Drei Aufrufstellen warteten schon

Im Port standen drei auskommentierte Zeilen, die auf genau diese Klasse warteten:

| Datei | Wirkung |
|---|---|
| `HbmAnimation` | Nachladeanimation läuft auf halbe Zeit |
| `GunBaseNTItem` | der Nachladedurchlauf läuft zweimal |
| `IMagazine` | ein Drittel der Munition wird gespart |

Das waren Ursachen ohne Wirkung — Platzhalter, die jemand (ich, in früheren Runden) für diese
Runde hinterlassen hat. Alle drei sind jetzt scharf.

Die vierte Hälfte, `hasAoS`, bleibt aus: sie liest die **AoS-Karte**, ein Rüstungsmodul
(`card_aos`, `ItemModCard`), das der Port noch nicht hat. Das steht als Kommentar an der
Stelle, nicht als stiller `false`.

### Zwei neue Haken an `ArmorFSBItem`

`handleHurt` (in 1.21 `LivingDamageEvent.Pre`) und `handleAttack` (`LivingIncomingDamageEvent`).
Beide werden **nur an der Brustplatte** aufgerufen — das ist keine Vereinfachung, sondern
steht so im Original (`ModEventHandler` Z. 680 und 733), das ausschließlich
`armorInventory[2]` fragt.

### Eine Lücke aus Runde 215, nachgereicht

Beim Nachmessen des Verteilers fiel auf: **der Euphemium-Satz bricht im Original Angriffe
ganz ab**, mit demselben Klirren. Diese Abfrage steht nicht in `ArmorEuphemium`, sondern im
Ereignisverteiler (`ModEventHandler` Z. 674) — deshalb habe ich sie in Runde 215 übersehen,
als ich nur die Klasse gelesen habe. Sie kommt jetzt nach, an genau der Stelle, an der sie
auch im Original steht.

Die Lehre reiht sich in die fünf Fehlmessungen davor ein, ist aber eine neue Art: nicht falsch
gesucht, sondern **am falschen Ort gesucht**. Was eine Rüstung tut, steht in diesem Mod nicht
immer in ihrer Klasse.

### Was der Grabenmeister kann

| | |
|---|---|
| Satzbonus | Stärke III, Eile II, Sprungkraft II, Tempo I |
| Eigene Sprengungen | richten **null** Schaden an (fremde treffen normal) |
| Jeder dritte Treffer | prallt ganz ab, mit Klirren |
| Strahlung | 90 %, `FULL_PACKAGE` |
| Haltbarkeit | keine — Beute wie Taurun, kein einziges Werkbankrezept |

### Eine Lampe, die leuchtet

Zum Helm gehört ein neuntes Modellteil, das **voll ausgeleuchtet** gezeichnet wird. Der Port
merkt sich den Lichtwert, setzt ihn auf `FULL_BRIGHT` und legt ihn danach zurück, damit der
Rest der Figur nicht mitleuchtet.

Der Helm selbst ist durchscheinend — und anders als beim Fau-Anzug aus Runde 219 schaltet das
Original die Mischung hier hinterher **selbst** wieder ab. Hier war also nichts zu berichtigen,
nur zu übernehmen.

Die Beine stehen wieder einen Hauch auseinander, dieselben ±0,16 Modelleinheiten wie bei
Taurun.

Nach dieser Runde sind **22** Rüstungsnamen offen.

Alle 39 Tore grün.

## Runde 221 — Der M1TTY-Umgebungsanzug

Ein Taucheranzug mit Landgang, und der erste der Reihe, dessen Wirkung davon abhängt, **wo
der Träger gerade steht**.

| | |
|---|---|
| Energie | 100 000 HE — der sparsamste bestrombare Satz des Ports |
| Satzbonus | Tempo II, Sprungkraft I |
| Im Sprint | ein Zehntel mehr Tempo, das beim Anhalten wieder verschwindet |
| Unter Wasser | voller Atem, Nachtsicht, Antrieb in Blickrichtung |
| Über Wasser | die Nachtsicht wird wieder abgenommen |
| Strahlung | 90 %, `FULL_PACKAGE` |

Der Sprintaufschlag wird **jeden Tick erst abgenommen und dann gegebenenfalls neu aufgelegt**
— so hält das Original ihn an den Sprint gebunden, ohne ihn zu stapeln. Der Port macht es
genauso, nur mit einem festen Namen statt einer festen UUID.

### Eine sichtbare Abweichung, und warum

Zum Helm gehören **Lampen**, die voll ausgeleuchtet und gelblich gezeichnet werden. Das
Original zeichnet sie **ganz ohne Textur**: es schaltet `GL_TEXTURE_2D` ab und setzt eine
reine Farbe. In 1.21 tastet die Zeichenart immer eine Textur ab — der Port färbt deshalb die
Helmtextur ein, statt sie abzuschalten.

Die Lampen leuchten also gelblich wie dort, zeigen aber die Maserung des Helms. Das ist die
erste Abweichung dieser Reihe, die man **sieht**, und sie steht deshalb im Modell, nicht nur
hier.

### Ein Schwanz, den niemand zeichnet

`envsuit.obj` enthält ein Teil namens `Tail`, und `ResourceManager` meldet eine Textur
`envsuit_tail` dafür an. Gebunden wird sie nirgends — auch im Original nicht. Beides bleibt
draußen: kein Zeichnen, keine Textur. Eine Wirkung ohne Ursache wäre hier ein Modellteil ohne
Grund.

### Nicht übernommen

`hides(EnumPlayerPart.HAT)` wie überall. Dazu die Ausnahme für den **Nachtsicht-Aufsatz**: das
Original nimmt die Nachtsicht *nicht* ab, wenn im Helm ein `ItemModNightVision` steckt. Dieses
Modul gibt es im Port noch nicht — solange es fehlt, verhält sich der Anzug wie das Original
ohne Aufsatz, was nachgemessen dasselbe ist.

Nach dieser Runde sind **18** Rüstungsnamen offen.

Alle 39 Tore grün.

## Runde 222 — Der Blackjack-Anzug, und ein eingelöstes Versprechen

Fünf Namen auf einmal: die vier Stücke plus eine zweite Brustplatte mit Rückentriebwerk.

### Wer ihn trägt und ihm geht der Strom aus, stirbt

Trägt der Spieler einen vollständigen Satz, ist aber nicht jedes Teil geladen, dann nimmt ihm
der Helm sich selbst ab, legt sich ins Gepäck (oder fällt zu Boden, wenn dort kein Platz ist)
— und danach folgen **tausend Punkte Mondschaden**, die weder Panzerung noch Widerstandskraft
aufhalten. Die Kybernetik im Schädel hört auf zu arbeiten.

Dafür kam eine neue Schadensart dazu: `NtmDamageTypes.LUNAR`, mit den drei Kennungen, die im
Original `setDamageIsAbsolute()` und `setDamageBypassesArmor()` entsprechen.

### Das Triebwerk war nicht blockiert

Ich hatte den Jetpack für blockiert gehalten: er fragt `HbmPlayerProps.isJetpackActive()`, und
`HbmPlayerProps` gibt es im Port nicht. Nachgemessen ist diese Abfrage nur
`enableBackpack && getKeyPressed(JETPACK)` — und **beides** liegt längst in
`HbmPlayerAttachments`, samt Umschalttaste und Meldung „Jetpack ON". Der Anzug fliegt.

Damit ist auch der Gleitflug da: ist das Triebwerk aus und der Träger geht in die Hocke, wird
der Sturz auf vier Zehntel gebremst und die gewonnene Bewegung in Blickrichtung umgelenkt.

Das ist wieder ein „blockiert", das beim Messen zerfiel — wie die Tau-Kanone, der Gasflaschen-
Deskriptor und der Liquidator-Helm davor.

### Ein Versprechen aus einer früheren Runde eingelöst

In `NtmRecipeProvider` stand seit Langem:

> *Der Desh-Motor fehlt im Port. Die Pumpe nimmt stattdessen den gewöhnlichen Motor […]
> Fällt der Desh-Motor nach, gehören diese beiden Felder zurückgedreht.*

Er kam in Runde 216 nach. Die Felder sind gedreht — die Watz-Pumpe nimmt jetzt den Deshmotor,
wie im Original.

Dieselbe Beschreibung stand außerdem **über der falschen Methode**: über `pileDevices` statt
über `watzParts`, beschrieb also Rezepte, mit denen sie nichts zu tun hat. Das ist der zweite
verirrte Kommentar dieser Reihe nach dem aus Runde 220.

### Ein Tor hat einen echten Fehler gefunden

Der geborgte Triebwerksklang heißt im Original `immolatorShoot`. In 1.21 sind in einem
Ressourcenpfad nur `[a-z0-9/._-]` erlaubt — das große S hätte beim Nachschlagen eine
`ResourceLocationException` geworfen. `location-check` hat das gemeldet, bevor es ins Spiel
kam; Datei und Verweis heißen jetzt `immolator_shoot`.

### Keine Gefahrenklasse, und das ist Absicht

Der Blackjack-Anzug trägt im Original **kein** `setHazardClass` — als einziger der
Wellenfront-Rüstungen mit Strom. Er steht deshalb nicht in `ArmorUtil`, nur in
`HazmatRegistry` (90 %). Eine Abwesenheit mit Grund, wie bei der Wismut-Garnitur.

Nach dieser Runde sind **13** Rüstungsnamen offen — und die DNS-Garnitur ist nicht mehr
blockiert: ihre vier Baupläne brauchen genau diese BJ-Stücke.

Alle 39 Tore grün.

## Runde 223 — Der DNT-Nanoanzug, der stärkste Satz des Mods

Die Garnitur, die Runde 222 freigelegt hat: ihre vier Baupläne bauen jeweils um ein fertiges
Blackjack-Stück herum.

### Praktisch unverwundbar

| | |
|---|---|
| Jeder Angriff außer einer Sprengung | wird **ganz abgebrochen**, mit Klirren |
| Sprengungen | richten ein **Tausendstel** ihres Schadens an |
| Alles andere, was durchkommt | null |
| Energie | 1 000 000 000 HE |
| Satzbonus | Stärke X, Eile VIII, Sprungkraft III |
| Strahlung | 99,999 % — der beste Wert des Mods |

Das ließ sich nur bauen, weil Runde 220 die beiden Kampfhaken nachgerüstet hat. Ohne
`handleAttack` und `handleHurt` wäre dieser Anzug eine Rüstung mit Zahlen und ohne Eigenschaft
gewesen.

### Drei Flugzustände

| Zustand | Bedingung | Wirkung |
|---|---|---|
| Triebwerk | Rückentriebwerk an, Sprungtaste | steigt um 0,2 je Tick bis 0,6 |
| Schwebeflug | Rückentriebwerk an, in der Luft, nicht in der Hocke | Sturz aufgefangen, Waagerechte ×1,05, Blickrichtung zieht |
| Sinken | in der Hocke, in der Luft | −0,1 je Tick |

Auch das war nur möglich, weil Runde 222 gemessen hat, dass `isJetpackActive` im Port schon
vorhanden ist. Zwei Runden Vorarbeit, die sich hier auszahlen.

### Eine Falle im Namen

**Die Texturen heißen `dnt_*`, die Gegenstände `dns_*`.** Das ist kein Vertipper: der Werkstoff
ist Dineutronium (DNT), die Garnitur heißt „DNT Nano Suit" (DNS). Wer nach `dns` sucht, findet
die Texturen nicht — genau die Art Namensverschiebung, die in Runde 215 schon einmal zu einer
Fehlmessung geführt hat.

### Ein Gegenstand kam mit

Die **Balls-O-Tron-Münze** (`coin_worm`) fehlte dem Port; das DNS-Beinzeug braucht sie. Im
Original lässt der BOT-Prime-Kopf sie fallen — diesen Gegner gibt es hier noch nicht, sie ist
also vorerst nur über den Kreativreiter zu haben. Das steht am Gegenstand, damit niemand
später rätselt, warum sie nirgends anfällt.

Nach dieser Runde sind **9** Rüstungsnamen offen: `dieselsuit_*`, `steamsuit_*` und
`nossy_hat`. Die ersten beiden brauchen `ArmorFSBFueled`, eine Grundklasse für
flüssigkeitsbetriebene Anzüge, die der Port noch nicht hat.

Alle 39 Tore grün.

## Runde 224 — Der Dampfanzug, und eine Grundklasse mit Tank

Die letzten neun Namen teilten sich in „braucht nur Fleißarbeit" und „braucht erst eine
Grundklasse". Diese Runde baut die Grundklasse und den ersten Anzug darauf.

### `ArmorFSBFueledItem`

Das Gegenstück zu `ArmorFSBPoweredItem` für Anzüge, die keinen Akku haben, sondern einen
**Tank**. Ohne Füllung ist das Teil totes Blech, der ganze Satzbonus fällt weg. Nachgefüllt
wird an der Zapfsäule, die jeden `IFillableItem` bedient — die gibt es im Port seit Runde 100.

**Ein Unterschied, der leicht verlorengeht:** der Grundverbrauch läuft nur **jeden zehnten
Tick**, nicht jeden. Im Original steht dafür `world.getTotalWorldTime() % 10 == 0`. Wer das
überliest, baut einen Anzug, der zehnmal zu schnell leerläuft.

Der Flüssigkeitstyp kommt als Lieferant herein statt als Feld: die Fluidtypen des Ports stehen
erst nach dem Laden fest, ein Feld im Konstruktor wäre zu früh.

### Der Dampfanzug

| | |
|---|---|
| Tank | 64 000 Dampf, 500 je Füllschritt |
| Verbrauch | 50 je Schadenspunkt, 1 je zehn Ticks |
| Satzbonus | Eile V |
| Strahlung | 95 %, `FULL_PACKAGE` |
| **Temposchaden** | −0,025 an **jedem** Teil, dauerhaft |

Der Temposchaden ist kein Versehen: ein Anzug voller Kessel und Rohre ist schwer. Im Original
hängt er in `getItemAttributeModifiers`, im Port an den Eigenschaften des Gegenstands.

### Noch einmal die Namensverschiebung

Die Klasse heißt `ArmorDesh` und das Modell `ModelArmorDesh` — nach dem **Werkstoff**. Die
Texturen und die Registriernamen heißen `steamsuit_*` — nach der **Garnitur**. Wer nach
`steamsuit` sucht, findet die Klassen nicht.

Das ist derselbe Fall wie `dnt_*` / `dns_*` aus Runde 223 und wie der Fehler aus Runde 215,
bei dem genau diese Verschiebung mich drei Garnituren für modelllos halten ließ. Beim dritten
Mal steht es jetzt in der Klasse.

Nach dieser Runde sind **5** Rüstungsnamen offen: `dieselsuit_*` und `nossy_hat`.

Alle 39 Tore grün.

## Runde 225 — Der Dieselanzug

Der zweite Tankanzug, und der erste, der **zwei Sorten** annimmt: neben Diesel auch
gekrackten Diesel. Das Original überschreibt dafür `acceptsFluid`; die Zapfsäule füllt beide,
der Tankname im Tooltip nennt nur den ersten — dort wie hier.

| | |
|---|---|
| Tank | 64 000, dieselbe Staffelung wie der Dampfanzug |
| Satzbonus | Tempo III, Sprungkraft III |
| Rückstoßfestigkeit | +25 % an **jedem** Teil |
| Gefahren / Strahlung | **keine** |

**Er schirmt gegen nichts ab**, und das ist kein vergessener Eintrag: als einziger der beiden
Tankanzüge trägt er im Original weder `setHazardClass` noch `setRadResist`. Er steht deshalb
weder in `ArmorUtil` noch in `HazmatRegistry` — dieselbe Abwesenheit mit Grund wie bei der
Wismut-Garnitur aus Runde 218.

### Dritter Name für dieselbe Sache

Die Klasse heißt `Diesel`, die Registriernamen `dieselsuit_*`, und Modell, Texturen **und
Werkstoff** heißen `bnuuy`. Wer nach „diesel" sucht, findet weder Modell noch Texturen.

Das ist die dritte Namensverschiebung in drei Runden — nach `dnt`/`dns` (223) und
`Desh`/`steamsuit` (224). In diesem Mod ist der Name, unter dem eine Sache registriert wird,
regelmäßig nicht der Name, unter dem ihre Dateien liegen. Wer über Klassennamen sucht, misst
falsch; genau das ist in Runde 215 passiert.

### Die Baupläne

Rote Wolle und Stahlbarren. Die Brustplatte verlangt einen **ganzen Dieselgenerator** als
Kern, das Beinzeug zwei Motoren. Die Stiefel — vier Felder, zwei Zutaten — sind das
schlichteste Rüstungsrezept des Mods.

Nach dieser Runde ist **ein** Rüstungsname offen: `nossy_hat`.

Alle 39 Tore grün.

## Runde 226 — Der Hut schließt die Rüstungsliste

Der letzte der 141 Rüstungsnamen. Kein Anzugteil, sondern ein Einzelstück:

| | |
|---|---|
| Schadensminderung | −2 von **jedem** Treffer, der nicht unabwendbar ist |
| Kleine Treffer | was höchstens 2 gemacht hätte, wird ganz abgesagt, mit Klirren |
| Fallengelassen | verschwindet auf der Stelle |
| Satzbonus | **keiner** — beides greift, sobald er auf dem Kopf sitzt |

### Zwei Haken, die an allen vier Plätzen greifen

Runde 220 hat `handleHurt` und `handleAttack` an `ArmorFSBItem` nachgerüstet — aber die
werden **nur an der Brustplatte** gefragt, so wie im Original. Der Hut ist ein Helm, und er
hängt an keinem Satz.

Dafür gibt es im Original zwei eigene Schnittstellen, `IAttackHandler` und `IDamageHandler`,
die über **alle vier** Rüstungsplätze abgefragt werden (`ModEventHandler` Z. 682 und 736).
Der Port hat sie jetzt auch, unter den Namen `IAttackHandlerItem` und `IDamageHandlerItem`.
Damit stehen beide Verteilungswege nebeneinander, genau wie dort — und der Unterschied
zwischen ihnen ist gemessen, nicht geraten.

### Modell und Textur lagen schon da

`syntax-check` hat gemeldet, dass `armor_hat` bereits erklärt ist. Nachgesehen: der
**Wackelkopf** (`RenderBobble`) trägt denselben Hut und hat Modell, Wellenfrontdatei und
Textur längst mitgebracht. Auch die Konstante `HAT_TEX` gab es, nur ohne Nutzer.

Es fehlte also nie das Aussehen, sondern nur der Rüstungsgegenstand dazu. Das Tor hat eine
doppelte Erklärung verhindert und nebenbei eine tote Konstante wiederbelebt.

### Zu haben ist er nur im Kreativreiter

Im Original trägt ihn gelegentlich ein Gegner (`MobUtil`) und ein bestimmter Spieler bekommt
ihn geschenkt; ein Werkbankrezept hat er auch dort nicht. Beide Quellen gibt es im Port nicht
— das steht am Gegenstand, damit niemand nach einem Rezept sucht, das es nie gab.

---

## Damit ist die Rüstungsliste geschlossen

**141 von 141 Rüstungsnamen sind portiert.** Der Stand aus Runde 210 — 103 fehlende Namen,
nachgemessen nach einer Fehlmessung, die 17 behauptet hatte — ist abgearbeitet:

| Runde | Was | Namen |
|---|---|---|
| 211–213 | Aschebrille, 33 schlichte Teile, 15 mit Zusatz | 49 |
| 214 | T-51 | 4 |
| 215 | Euphemium + Taschenuhr | 4 |
| 216 | AJR und AJRO | 8 |
| 217 | Taurun | 4 |
| 218 | Wismut + Laserkristall | 4 |
| 219 | Fau + Uraltschrott | 4 |
| 220 | Grabenmeister | 4 |
| 221 | Umgebungsanzug | 4 |
| 222 | Blackjack samt Triebwerk | 5 |
| 223 | DNT-Nano + Münze | 4 |
| 224 | Dampfanzug + `ArmorFSBFueledItem` | 4 |
| 225 | Dieselanzug | 4 |
| 226 | Hut | 1 |

Dazu kamen unterwegs acht Gegenstände und ein Block, die als Zutat gebraucht wurden, drei
neue Grundklassen bzw. Schnittstellen, eine Schadensart und ein Tonereignis.

Alle 39 Tore grün.

## Runde 227 — Das Messwerkzeug hört auf zu lügen, und der nächste Bereich vermessen

Mit der geschlossenen Rüstungsliste meldete `tools/port-gap.py` im Klassenvergleich weiter
**73 fehlende Rüstungsklassen** — während der Registriernamen-Vergleich zwei Zeilen tiefer
**null fehlende Namen** zeigte.

Beide Zahlen waren richtig gerechnet. Die erste war trotzdem unbrauchbar: der Port fasst
Klassen zusammen (eine `ArmorAJRItem` für AJR *und* AJRO, eine `ArmorFSBItem` für
dreiunddreißig schlichte Garnituren), und der Klassenvergleich sagt selbst im Kopf, dass er
nur eine obere Schranke ist. Eine Zahl, die Arbeit behauptet, die es nicht gibt, ist
schlimmer als keine Zahl — dieselbe Regel, nach der in Runde 210 schon einmal das Werkzeug
selbst berichtigt wurde.

Die Rüstungszeile ist deshalb aus dem Klassenvergleich heraus. Gemessen wird sie ab jetzt
ausschließlich über Registriernamen. Die Einzelliste `--list armor` bleibt, sie ist zum
Nachschlagen weiter nützlich.

### Der nächste Bereich, vermessen statt geschätzt

`tools/structure-gap.py` zeigt die Bauwerke in besserem Zustand als erwartet: von **185**
Blocknamen, die die 79 Bauwerke des Originals benutzen, sind 154 angelegt, 11 über Familien
abgedeckt, 14 gar nicht nötig — **echt fehlend sind sechs**.

Diese sechs sind nachgesehen, nicht geschätzt:

| Name | Woran es hängt |
|---|---|
| `dungeon_spawner` | braucht `EntityUndeadSoldier` — **Entität fehlt** |
| `meteor_spawner` | braucht `EntityCyberCrab` — **Entität fehlt** |
| `wand_jigsaw` | Bauwerkzeug, 382 Zeilen |
| `wand_logic` | Bauwerkzeug, 351 Zeilen |
| `wand_loot` | Bauwerkzeug, 443 Zeilen |
| `wand_tandem` | Bauwerkzeug, 431 Zeilen |

Die beiden Spawner sind also **keine** Blockarbeit, sondern Entitätenarbeit: beide sind
Blöcke, deren einziger Zweck das Herbeirufen eines Gegners ist, den es im Port nicht gibt.
Sie zuerst anzulegen hieße, zwei Ursachen ohne Wirkung zu bauen.

Die vier Wände sind mit je 350 bis 440 Zeilen jeweils eine eigene Runde wert und gehören
nicht als Beifang in diese.

Damit steht der nächste Bereich fest: **die Entitäten** (133 Klassen als obere Schranke) sind
die Wurzel, an der Bauwerke und Spawner hängen.

Alle 39 Tore grün.

## Runde 228 — Der Untote Soldat

Die erste Entität nach der geschlossenen Rüstungsliste, und die, an der zwei Dinge hängen:
der `dungeon_spawner` (Runde 227 hat ihn als „braucht `EntityUndeadSoldier`" ausgewiesen)
und die **Taurun-Rüstung** aus Runde 217, die bis heute nur in der Kreativkiste lag. Im
Original trägt sie dieser Soldat — mehr Quelle hat sie nicht, kein Werkbankrezept, nichts.

Der Soldat ist zur Hälfte Zombie, zur Hälfte Skelett. Was er ist, würfelt er beim Erscheinen
aus; es entscheidet über Modell, Textur, Stimme und Schrittgeräusch. Dazu eine von fünf
Handfeuerwaffen — schwerer und leichter Revolver, Karabiner, Mare's Leg, Grease Gun —, alle
fünf gibt es im Port seit den Waffenrunden.

### Was 1.21 anders will

| Original (1.7.10) | Port (1.21) | Warum |
|---|---|---|
| `EntityMob.attackEntity` | `MeleeAttackGoal` | 1.7.10 hat Zulaufen und Zuschlagen fest in `EntityMob`; 1.21 hat es nicht. Ohne diese Aufgabe stünde der Soldat nur da und sähe seinem Ziel zu. |
| `getCreatureAttribute() == UNDEAD` | Tag `minecraft:undead` | In 1.21 sagt keine Kreatur mehr selbst, dass sie untot ist. `sensitive_to_smite` und `inverted_healing_and_harm` zeigen beide auf `undead` — **ein** Eintrag genügt für alle drei Wirkungen. Dafür ist `NtmEntityTypeTagsProvider` neu. |
| `ModelZombie` + `ModelSkeletonNT` | eine Klasse, zwei Modelllagen | `ModelSkeletonNT` ist ein `ModelZombie` mit dünnen Gliedern — genau `ModelLayers.SKELETON`. In 1.21 ist die Geometrie von der Haltung getrennt, also bleibt eine Klasse für die Haltung und zwei Lagen von Minecraft für die Form. |
| `preRenderCallback` tauscht das Modell | `render()` tauscht es | `LivingEntityRenderer` liest `this.model` in 1.21 schon **vor** `scale()`, der Stelle, die `preRenderCallback` entspricht. Wer dort tauschte, setzte den Schlagtakt noch am alten Modell. |
| `getCanSpawnHere()` | *nicht übernommen* | Das Original prüft dort Schwierigkeitsgrad, freie Stelle, keine Flüssigkeit — genau das, was `Monster.checkSpawnRules` und `Mob.checkSpawnObstruction` von sich aus tun. Eine Wiederholung wäre toter Code. |
| Spawn-Ei aus `EntityMappings.addMob` | *nicht übernommen* | Der Port hat für **keine** seiner Kreaturen eines, auch nicht für den nuklearen Creeper. Gerufen wird per `/summon`. |

Eine Stelle ist absichtlich **besser** als das Original: die ausgewürfelte Gestalt wird
gespeichert. In 1.7.10 wird der `DataWatcher` nicht in die NBT geschrieben, ein nachgeladener
Soldat ist also wieder Zombie, auch wenn er als Skelett aufgestellt wurde. Das ist eine Lücke
der alten Datenhaltung, kein Verhalten, das jemand gewollt hat.

Fallen lässt er nichts — auch nicht, was er trägt. So steht es im Original (`dropFewItems` und
`dropEquipment` beide leer), und der Port macht es über `dropCustomDeathLoot`.

### Das Asset-Tor hat sich selbst gemeldet

Die Zombie- und die Skeletttextur sind **Minecraft-Texturen**, geholt über
`ResourceLocation.withDefaultNamespace(...)`. `tools/asset-check.sh` unterschied die beiden
Empfänger nicht und suchte sie unter `assets/hbmsntm/` — zwei Fehlalarme, die keine
Mod-Assets sind.

Das Muster ist jetzt auf den Empfänger eingeengt, und zwar **nur gegen `ResourceLocation`**,
nicht etwa eingeschränkt auf `NuclearTechMod` — sonst sähe das Tor einen künftigen Aufruf
über einen anderen Helfer gar nicht mehr an. Gemessen: 751 Aufrufe über `NuclearTechMod`,
zwei über `ResourceLocation`; geprüfte Referenzen 1662 → 1660, fehlende 2 → 0. Gegenprobe mit
einem absichtlich verdrehten Entenpfad: genau ein Fund. Das Tor sieht also weiter hin.

### Nebenbei berichtigt

Bis zu dieser Runde hatte **keine** Kreatur des Ports einen Namen — Ente und nuklearer Creeper
zeigten im Spiel ihren rohen Schlüssel, im Todesbildschirm wie am Namensschild. Alle drei
haben jetzt einen. (Das Original hat für den Untoten Soldaten selbst keinen.)

Entitäten damit 133 → **132** offene Klassen. Alle 39 Tore grün.

## Runde 229 — Die Waffenbaupläne nachgemessen

Beim Nachsehen, was der `dungeon_spawner` noch braucht, fiel im Kopfkommentar von
`gunRecipes` ein Satz auf, der nicht mehr stimmte. Nachgemessen — und er stimmte gleich
zweifach nicht.

**Erstens.** Der Kommentar nannte acht Waffen als „im Port nicht angelegt": Stinger, Quadro,
LAG, Raketenwerfer, Fat Man, Tau und die beiden Panzerrüstungswaffen. **Alle acht gibt es
längst** — die Panzerrüstungswaffen seit Runde 196, Quadro und Raketenwerfer seit 197,
Stinger seit 199, LAG seit 201, Tau seit 203, Fat Man seit 204. Sechs hatten ihren Bauplan
auch schon; die beiden Panzerrüstungswaffen nicht. Die bekommen ihn jetzt.

**Zweitens.** Der Kommentar behauptete, der Feuerlöscher stehe „nicht in dieser Liste,
sondern oben bei seiner Munition". Er steht mitten in `gunRecipes`, an seinem Platz in der
Reihenfolge des Originals, nur ohne den `gun()`-Helfer — weil er kein einziges Waffenbauteil
braucht. Der Grund stimmte, der Ort war falsch.

### Gemessen statt geschätzt

| | |
|---|---|
| `WeaponRecipes` baut eine Waffe | **51 Mal**, **50 verschiedene** (der Ladungswerfer steht zweimal da, Leder und Gummi) |
| Dieser Erzeuger deckt ab | **46** |
| Echt fehlend | **4** |

Und diese vier haben je einen Grund, der nachgesehen ist:

| Bauplan | Grund |
|---|---|
| `gun_b92`, `gun_b92_ammo` | die Waffe gibt es im Port nicht — der letzte Rest der Gruppe „es gibt die Waffe nicht" |
| `gun_chemthrower` | braucht einen Schraubenschlüssel; `ToolType.WRENCH` steht in der Aufzählung, ein Gegenstand dazu fehlt |
| `gun_double_barrel_sacred_dragon` | braucht `item_secret` in der Ausführung `SELENIUM_STEEL` |

`item_secret` ist damit zum zweiten Mal aufgefallen: es blockiert auch den
`dungeon_spawner`, der ein Geheimstück in den Skeletthalter legt. Zwei Wirkungen, eine
Ursache — die Familie der Geheimstücke ist die nächste Runde wert.

### Die beiden neuen Baupläne

Sie brauchen kein einziges Waffenbauteil, was zu dem passt, was sie sind: Aufsätze auf einen
Anzug, keine Handfeuerwaffen. Motor, Schaltkreis und Dickdraht, mehr nicht — wortgetreu aus
dem Original, wo sie ebenfalls abgesetzt am Ende des Waffenblocks stehen.

Alle 39 Tore grün.

## Runde 230 — Die Geheimstücke, und was sie entsperren

Fünf Sachen ohne Bauplan und ohne Kreativreiter: **Composition SB-26**, **Proprietary
Control Unit**, **Selenium Steel**, **Aberrator Part**, **Folly Part**. Im Original tragen
sie `setCreativeTab(null)` und stehen auf der NEI-Ausschlussliste — man soll nicht sehen,
wozu sie gut sind, bevor man eines in der Hand hat. Sie liegen in Bauwerken, in Beutetöpfen,
im Sockel des Skeletthalters.

Der Port braucht dafür nur eine Aufzählung und eine Zeile: `EnumMultiItem` mit
`multiName`/`multiTexture` ist genau das Gegenstück zu `ItemEnumMulti(…, true, true)`, und
die Modelle entstehen von selbst über `ICustomItemModelRegister`. Die fünf Texturen kommen
aus der CE-Abspaltung und heißen dort schon so, wie der Port sie sucht:
`item_secret.<wert>.png`.

### Zwei Wirkungen, eine Ursache

Runde 229 hatte beim Nachmessen gesehen, dass `item_secret` an **zwei** Stellen als
Begründung steht:

| Was blockiert war | Seit wann |
|---|---|
| Bauplan des **Heiligen Drachen** (`gun_double_barrel_sacred_dragon`) | Runde 118 — die Waffe selbst gibt es seit Runde 84 |
| der **`dungeon_spawner`** (legt ein Aberrator-Teil in den Skeletthalter) | Runde 227 als fehlender Bauwerksblock ausgewiesen |

Der Bauplan des Heiligen Drachen steht jetzt da — ein formloses Rezept, Doppelflinte plus
ein Stück Selenstahl. Damit deckt der Waffenbauplan-Erzeuger **47 von 50** ab; fehlend sind
noch `gun_b92`, `gun_b92_ammo` (Waffe fehlt) und `gun_chemthrower` (Schraubenschlüssel
fehlt).

### Der Spawner bleibt trotzdem stehen — gemessen, nicht geraten

Er ist damit **nicht** frei. Seine letzte Phase legt in vier von fünf Fällen eine
**Tontafel** (`clay_tablet`, Metadaten 1) auf den Skeletthalter, und die Tontafel ist
selbst blockiert: ihr einziger Zweck ist eine Oberfläche, die ein zufälliges
**Sockelrezept** zeigt — `PedestalRecipes` gibt es im Port nicht. Dasselbe System fehlt
schon dem Topas-Flammenwerfer (Notiz in `XFactoryFlamer`).

Damit ist die Kette sauber vermessen:

```
dungeon_spawner  ->  UndeadSoldier   (Runde 228, da)
                 ->  item_secret     (Runde 230, da)
                 ->  clay_tablet     ->  PedestalRecipes   (fehlt)
```

Die Sockelrezepte sind also der nächste echte Knoten, nicht der Spawner.

Alle 39 Tore grün.

## Runde 231 — Das Sockelsystem

Der Knoten, den Runde 230 vermessen hatte. Neun Sockel: einer in der Mitte, acht ringsum
im Abstand **drei** — nicht nebeneinander, sondern so weit auseinander, dass das Ganze
sieben mal sieben Blöcke einnimmt. Wer das Richtige auflegt und den mittleren mit Redstone
beschickt, bekommt darauf ein Einzelstück.

Die Abstandstabelle ist Schritt für Schritt nachgerechnet und nicht geglättet: die vier
geraden Richtungen liegen drei Blöcke entfernt, die vier **Ecken je zwei in beide
Richtungen**. Das ist nicht dasselbe wie drei in beide Richtungen, und es sieht im Spiel
auch anders aus — die Ecken stehen näher. So steht es im Original, und so bleibt es.

### Was heute baubar ist — gemessen, nicht geschätzt

Alle 14 Erzeugnisse der Sockelrezepte gibt es im Port. Von den **17 Rezepten stehen 13**
hier; vier fehlen, und jedes mit benannter Ursache:

| Rezept | Fehlende Zutat |
|---|---|
| `gun_flamer_daybreaker` | `stick_dynamite` |
| `gun_autoshotgun_sexy` | `bolt_spike`, `wild_p`, `card_qos`, `card_aos` |
| `gun_laser_pistol_morning_glory` | `morning_glory` |
| `ammo_secret` FOLLY_SM | `chunk_ore` in der Ausführung MOONSTONE |

Ein Rezept auf eine Zutat, die es nicht gibt, wäre kein Rezept, sondern eine Zeile, die nie
zutrifft.

### Die Namensfalle, noch einmal

Der Protege braucht `ModBlocks.chain` — und dieses Feld meldet sich im Original unter dem
Namen **`dungeon_chain`** an. Wer nach `chain` sucht, findet im Port nichts und hielte das
Rezept für blockiert. Dieselbe Falle wie bei `dnt_*`, `ArmorDesh`/`steamsuit` und
`ArmorHat`/`nossy_hat`: der Feldname ist nicht der Registriername.

### Eine Ursache bekommt ihre Wirkung

Der Sockel führt ein Eintragsregister: liegt ein Schutz- oder Meteoritentalisman darauf,
trägt er sich alle zwanzig Takte ein, und die Einträge verfallen nach drei Sekunden — so
merkt das System, wenn der Talisman fort ist. `MeteorStrikeSystem` kannte bisher **nur** den
Talisman im Helm; der zweite Weg (im Original `BossSpawnHandler` Z. 244) ist jetzt
angeschlossen. Hätte ich das Register ohne diesen Anschluss portiert, stünde eine Ursache
ohne Wirkung da.

### Zwei Dinge bewusst nicht übernommen

* **Die Statistik `statLegendary`**, die das Original jedem Spieler im Umkreis von fünfzig
  Blöcken gutschreibt. Der Port hat kein eigenes Statistiksystem — es gibt nichts, was man
  hier hochzählen könnte.
* **Der goldene Entschärfer** auf dem Sockel, der Creeper im Umkreis entschärft. Der Port
  kennt `defuser_gold` nicht: den Entschärfer gibt es nur als Werkzeug, nicht als
  Rüstungsaufsatz, und `castrateCreeper` hat kein Gegenstück.

Der Sockel selbst steht wie im Original in **keinem** Kreativreiter und hat kein
Werkbankrezept — er wird von der Weltgenerierung gesetzt (Schlüsselloch, roter Backstein,
`LogicBlockConditions`). Der Eintrag im Torwächter sagt das mit dieser Begründung.

Alle 39 Tore grün.

## Runde 232 — Sechs Zutaten, drei Rezepte, zwei eingelöste Versprechen

Runde 231 hatte vier Sockelrezepte mit benannter Ursache offengelassen. Diese Runde reicht
sechs Zutaten nach und setzt drei der vier ein:

| Gegenstand | Was er ist | Welches Rezept er freigibt |
|---|---|---|
| `chunk_ore` (4 Werte) | vier Brocken, darunter der **Mondstein** | `ammo_secret` FOLLY_SM |
| `bolt_spike` | Eisenbahnnagel, ein Stück Metall ohne Fähigkeit | `gun_autoshotgun_sexy` |
| `card_aos` / `card_qos` | Pik-Ass und Pik-Dame, Helmaufsätze | `gun_autoshotgun_sexy` |
| `wild_p` | „Explosive Reactive Plot Armor", drei Leben | `gun_autoshotgun_sexy` |
| `morning_glory` | Sonderaufsatz, Resistenz und Verfallsschutz | `gun_laser_pistol_morning_glory` |

**Sechzehn von siebzehn** Sockelrezepten stehen damit. Übrig bleibt eines:
`gun_flamer_daybreaker`, dem `stick_dynamite` fehlt.

### Zwei Versprechen, die der Port selbst notiert hatte

Beide Stellen standen mit ausgeschriebener Begründung im Quelltext — und beide lösen sich
mit dieser Runde auf:

* **`IMagazine.shouldUseUpTrenchie`** stand auf einem festen `aos = false`, mit dem Satz,
  die AoS-Karte fehle dem Port. Sie fehlt nicht mehr; `ArmorTrenchmasterItem.hasAoS` ist
  nachgereicht und die Zeile liest sie jetzt.
* **`ArmorTrenchmasterItem`** vermerkte im Kopf, `hasAoS` sei nicht übernommen, weil die
  Karte fehle. Auch dieser Satz ist fort.

Dazu kommt eine neue Wirkungsstelle: `CommonEvents.onLivingDeath` fängt das Sterben ab und
lässt den Träger des Wild P wieder aufstehen. Das Original sucht den Aufsatz in **allen
vier** Teilen, obwohl er nur auf die Beinschiene passt — der Port tut dasselbe.

### Das Behauptungs-Tor hat sofort zugeschlagen

Kaum waren die Brocken angemeldet, meldete `claim-check.sh` einen Satz in
`CrystallizerRecipes`, der behauptete, es gebe sie nicht. Er war seit dieser Runde falsch,
und mit ihm fehlte ein Rezept: **sechzehn Blöcke Mondboden ergeben einen Mondstein** — die
zweite Quelle neben der Beute. Ohne sie gäbe es die Folly-Sondermunition nur in Bauwerken.

Beim zweiten Durchgang meldete das Tor **erneut** — diesmal meinen eigenen neuen Kommentar,
der die alte Behauptung wörtlich zitierte. Das Tor kann ein Zitat nicht von einer Aussage
unterscheiden. Die Lösung war nicht, eine Ausnahme einzutragen, sondern den Satz anders zu
schreiben: ein Tor, das man für den eigenen Text stummschaltet, schaut beim nächsten Mal
auch für einen echten Fehler weg.

Alle 39 Tore grün.

### Nachgemessen zu Runde 231: der Sockel ist noch unerreichbar

Nach Runde 232 nachgesehen, und der Befund gehört hierher, weil er eine Lücke ist, die ich
selbst aufgemacht habe: **das Ritual funktioniert, aber niemand kann den Altar bauen.**

Der Sockel hat wie im Original weder Kreativreiter noch Werkbankrezept. Im Original setzt
ihn die Welt an genau drei Stellen — und **keine** davon gibt es im Port:

| Was ihn setzt | Im Port |
|---|---|
| `BlockKeyhole` (235 Zeilen) | fehlt — die Notiz in `XFactoryDrill` Z. 57 sagt es schon: „Diesen Block gibt es im Port nicht" |
| `BlockRedBrickKeyhole` (161 Zeilen) | fehlt |
| `LogicBlockConditions` | fehlt — gehört zu `wand_logic`, einem der vier Bauwerkzeuge aus Runde 227 |

`tools/structure-gap.py` bestätigt es von der anderen Seite: `pedestal` steht in **keinem**
der 79 Bauwerke, weil er dort nie direkt gesetzt wird, sondern immer durch eines dieser
drei Stücke Laufzeitcode.

Das Schlüsselloch ist damit die nächste Runde — es ist nicht bloß ein Platzierer, sondern
ein eigenes Ritual (Kammer ausheben, Lava setzen, mit einem Zwanzigstel Wahrscheinlichkeit
eine Beutekiste statt des Sockels) und hängt an den Beutetöpfen des Roten Zimmers.

## Runde 233 — Das Rote Zimmer

Die Lücke aus dem Nachtrag zu Runde 231 ist zu. Der Sockel hat jetzt eine Quelle: das
**steinerne Schlüsselloch**, das in den Bauwerken herumsteht und aussieht wie Stein — bis
hin zum Mittelklick, der Stein liefert. Wer den roten Schlüssel daran hält, hebt dahinter
ein Zimmer aus, neun mal neun Blöcke und fünf hoch, und bekommt eine Tür.

### Vier Blöcke

| Block | Was er ist |
|---|---|
| `brick_red` | der Ziegel des Zimmers — **sieben** Zustände |
| `stone_keyhole` | das steinerne Schlüsselloch |
| `stone_keyhole_meta` | dasselbe in der Zimmerwand, dahinter das schwarze Zimmer |
| `door_red` | die Tür, die vierte neben Metall, Büro und Bunker |

**Sieben Zustände, nicht sechs.** Das Metadatum des Originals ist die *Seite*, die das
Ziegelbild trägt — und der Zimmererzeuger setzt für alle Kanten den Wert sechs, der gar
keiner Seite entspricht. Eine bloße Richtungseigenschaft könnte das nicht ausdrücken, also
steht dort eine eigene Aufzählung mit einem siebten Wert `NONE`, in genau der Reihenfolge
des Originals.

Und deshalb **sieben einzelne Modelle**: ein gedrehtes `cube_column` wäre kürzer, trägt sein
Bild aber auf zwei gegenüberliegenden Seiten. Hier soll es auf genau einer stehen.

### Ein Erzeuger statt zwei

Im Original ist `generateRoom` in beiden Schlüssellochklassen abgeschrieben, mit
Unterschieden. Hier steht er einmal, mit den Unterschieden als Parameter:

* **Steinernes Zimmer** — Wände tragen das Ziegelbild nach innen, dazu Fackeln und mit je
  einem Viertel Spinnweben, Säulen, Feuer, ein Kreis und Lava. Mit einem Zwanzigstel eine
  Beutekiste mit einem ganzen Panzerrüstungssatz statt des Sockels.
* **Schwarzes Zimmer** — schmucklos, dafür bis zu fünf Sockel, und **die Wand, durch die man
  eintritt, bleibt offen**.

### Was nicht eins zu eins ging, und warum

* **`concrete_colored`** (Säulen und Kreis) gibt es im Port nicht; ein sechzehnfarbiger
  Betonblock wäre eine eigene Runde. Dort steht jetzt Minecrafts rotes Beton: dieselbe
  Farbe, dieselbe Rolle, anderes Bild. Kommt der Farbbeton nach, gehören die beiden Stellen
  berichtigt — sie sagen das selbst.
* **`achRedRoom`** fällt weg. Der Port hat kein Errungenschaftssystem, dasselbe Loch wie bei
  `statLegendary` am Sockel.
* **Der Beutetopf ist unvollständig, und zwar sichtbar.** Von den 25 Einträgen stehen 13;
  zwölf Gegenstände gibt es nicht (`ballistic_gauntlet`, `armor_polish`, `bandaid`, `serum`,
  `quartz_plutonium`, `spider_milk`, `ink`, `heart_container`, `black_diamond`, `scrumpy`,
  `starmetal_sword`, `flask_infusion`). Die Gewichte der übrigen bleiben, wie sie sind: sie
  umzurechnen wäre eine Erfindung, und beim Nachreichen müsste man zweimal rechnen.
* **`POOL_BLACK_SLAB` ist leer.** Er besteht im Original aus einem einzigen Eintrag, der
  Tontafel, und die fehlt noch. Ein leerer Vorrat liefert einen leeren Stapel — der Sockel
  in der Mitte bleibt dann leer. Das ist ehrlicher als ein Ersatz, den das Original nie hatte.

### Das Aufzählungs-Tor hat geraten, und das ist jetzt abgestellt

`BrickFace` heißt `DOWN, UP, NORTH, SOUTH, WEST, EAST, NONE` — und prompt meldete
`enum-check.sh` zwei Schalter über Minecrafts `Direction` als Schalter über `BrickFace`,
denen „`NONE` fehle". Das Tor kennt nur die Aufzählungen des Projekts; eine fremde mit
denselben Marken kann es nicht sehen.

Der erste Versuch — „der Typ muss in der Datei vorkommen" — war zu scharf: er hätte **zwei
echte Prüfungen** in `MissileBase` mit abgeschaltet, wo der Schalter über `missileItem.tier`
läuft und `MissileTier` nirgends ausgeschrieben steht. Gemessen: 21 → 17 geprüfte Schalter.

Die eingebaute Fassung überspringt nur, wenn **beides** zutrifft: die Marken passen auch auf
`Direction`, **und** die Datei nennt den Projekttyp nirgends. Gemessen: 21 → **19** geprüfte
Schalter, genau die zwei Fehlalarme weg, beide `MissileTier`-Prüfungen erhalten. Gegenprobe
mit einem zusätzlichen Wert in `MissileTier`: genau zwei Funde.

Alle 39 Tore grün.

## Runde 234 — Die Tontafel und der Aufrufer: die Kette ist zu

Runde 230 hatte eine Kette aufgeschrieben:

```
dungeon_spawner  ->  UndeadSoldier   (228)
                 ->  item_secret     (230)
                 ->  clay_tablet     ->  PedestalRecipes   (231)
```

Sie ist geschlossen.

### Die Tontafel

Sie zeigt **ein** Sockelrezept — aber nur zum Teil: bei der hellen Tafel bleibt die Hälfte
der neun Plätze verdeckt, bei der dunklen drei Viertel. Man sieht, was dabei herauskommt,
und muss den Rest erraten oder eine zweite Tafel suchen. Sie ist damit der einzige Weg,
ein Ritual überhaupt zu erfahren — die Rezepte stehen in keinem Rezeptbuch.

**Der Wurf steht im Stapel, nicht im Bildschirm.** Beim ersten Rechtsklick wird er
gewürfelt und bleibt; dieselbe Tafel zeigt immer dasselbe. Im Original ist das eine
NBT-Zahl, hier eine Datenkomponente — und sie muss zum Client, denn gezeichnet wird dort.

Die Reihenfolge der Züge ist die des Originals und darf nicht wandern: erst das Rezept aus
der Menge, dann Zeile für Zeile die neun Plätze. Wer dazwischen würfelt, verschiebt alles
Folgende, und die Tafel zeigte ein anderes Bild als im Original.

### Der Aufrufer

Vier Phasen: warten, bis ein Spieler auf zwanzig Blöcke herankommt — zehn Untote Soldaten
im Kreis aufstellen — warten, bis keiner mehr lebt — noch einmal zehn — und zuletzt eine
Belohnung in den Skeletthalter achtzehn Blöcke darüber legen. Danach macht er sich selbst
zu Obsidian: das Ritual geschieht genau einmal.

Die Belohnung ist mit einem Fünftel ein Aberrator-Teil, sonst die **dunkle Tontafel**.
Beides gibt es sonst nirgends: das Teil baut den Aberrator, die Tafel zeigt, wie. Genau
deshalb musste die Tafel vor dem Aufrufer kommen — ohne sie hätte er in vier von fünf
Fällen nichts herzugeben gehabt.

Der Zehnerkreis wird gerechnet wie im Original: ein Vektor der Länge zehn, zehnmal um
sechsunddreißig Grad gedreht, und je Soldat sieben Anläufe auf einen freien Platz.

### Was sich damit noch geschlossen hat

`POOL_BLACK_SLAB` war in Runde 233 **leer** gemeldet worden — mit der Begründung, sein
einziger Eintrag sei die Tontafel. Er ist jetzt vollständig. Von den sechs echt fehlenden
Bauwerksblöcken aus Runde 227 ist damit einer weg:

| Runde 227 | heute |
|---|---|
| `dungeon_spawner` | **da** (Runde 234) |
| `meteor_spawner` | braucht `EntityCyberCrab` |
| `wand_jigsaw`, `wand_logic`, `wand_loot`, `wand_tandem` | vier Bauwerkzeuge, je 350–440 Zeilen |

Alle 39 Tore grün.

## Runde 235 — Die Dynamitstange schließt die Sockelliste

Das siebzehnte und letzte Sockelrezept — der **Daybreaker** — hing allein an einer fehlenden
Zutat. Sie ist da, und damit steht die Liste vollständig:

| | |
|---|---|
| Runde 231 | 13 von 17 Rezepten, vier mit benannter Ursache offen |
| Runde 232 | sechs Zutaten nachgereicht, drei Rezepte eingesetzt → 16 |
| Runde 235 | die Dynamitstange → **17 von 17** |

Keine der vier Ursachen war eine Vermutung: jede nannte den fehlenden Gegenstand beim
Registriernamen.

### Warum sie eine eigene Klasse bekommt

Im Original hängt `stick_dynamite` an `ItemGenericGrenade` — der **alten, einfachen**
Granatenfamilie: ein Zünder, ein Knall, keine Bauteile. Der Port hat diese Familie nicht;
seine Granaten setzen sich aus Körper, Füllung, Zünder und Aufsatz zusammen.

Eine Dynamitstange als Universalgranate auszugeben hieße, ihr eine Zusammenstellung
anzudichten, die es im Original nicht gibt — und sie stünde dann im Spiel neben den
zusammengebauten Granaten, als wäre sie eine von ihnen. Darum eine eigene, kurze Klasse.
Die Sprengwerte sind die des Originals, Zahl für Zahl: Radius fünf,
`EntityProcessorCrossSmooth(1, 15)`, `PlayerProcessorStandard`,
`ExplosionEffectWeapon(10, 2.5F, 1F)`, drei Sekunden Zünder.

Der Aufschlag zündet sie **nicht** — sie springt ab und rollt, bis die Zeit um ist. Das ist
der Unterschied zum Aufschlagzünder der Universalgranate und steht so im Original.

Alle 39 Tore grün.

### Runde 236: die Kybernetische Krabbe und ihr Nest

Der `meteor_spawner` ist der zweite der sechs echt fehlenden Bauwerksblöcke aus Runde 227.
Im Original heißt seine Klasse `BlockCybercrab` -- wieder ein Fall, in dem der
Registriername und der Klassenname auseinanderlaufen.

Portiert:

* **`CyberCrab`** -- vier Lebenspunkte, Tempo 0,75, meidet Wasser (Wasser, Nässe und Feuer
  kosten je Tick zehn Schaden), schießt alle 60 bis 80 Ticks aus fünfzehn Blöcken einen
  Tau-Bolzen, ist gegen Tau- und Strahlenschaden immun und zerplatzt beim Tod
  (Sprengkraft 0,1, kein Blockschaden).
* **`TeslaCrab`** -- zehn Lebenspunkte, Tempo 0,5, schlägt zusätzlich beständig Blitze im
  Umkreis von drei Blöcken; ihr Körper ist das OBJ-Modell `teslacrab.obj`. Fällt mit
  etwa 2,5 Prozent eine Kupferspule.
* **`TauShot`** -- die eine Einstellung der alten `EntityBullet`, die die Krabbe benutzt:
  kritisch und Tau, Schaden drei. "Kritisch" heißt im Original nicht mehr Schaden, sondern
  dass das Geschoss weder in Blöcken steckenbleibt noch beim Treffer stirbt -- hier also
  spektral und durchschlagend, 250 Ticks lang.
* **`ModelCrab`** -- die zwanzig Kästen des Originals, Kasten für Kasten.
* **`MeteorSpawnerBlock` / `MeteorSpawnerBlockEntity`** -- das Nest: alle 200 Ticks eine
  neue Krabbe, wenn der Platz darüber frei ist, ein Spieler binnen 25 Blöcken steht und
  weniger als fünf Krabben in der Nachbarschaft sind; jede fünfte als Teslakrabbe.
* Laute: `entity.cybercrab` (die fünfzehn `radio_random`-Aufnahmen) und `weapon.saw_shoot`.
  Die Datei heißt im Original `sawShoot.ogg`; 1.21 lässt in einem Ressourcenpfad kein
  großes S zu -- derselbe Fall wie `immolator_shoot` in Runde 222.
* `TeslaBlockEntity.zap` hat einen zeichnenden Modus bekommen: die Teslakrabbe rechnet ihre
  Blitze wie im Original auf beiden Seiten aus, und auf dem Client darf davon nichts wirken.

Offene Punkte dieser Runde:

* **Das Meteoritenverlies baut der Port noch nicht.** Im Original setzt der
  `CrabSpawners`-Wähler das Nest dort mit einem Fünftel Wahrscheinlichkeit; bis dahin steht
  der Block -- wie der `dungeon_spawner` aus Runde 234 -- nur zum Setzen bereit.
* **Der Tau-Bolzen hat kein Modell.** Das Original zeichnet ihn über
  `ResourceManager.projectiles`, Teil `BulletRifle`; diese OBJ-Datei liegt nicht im Port.
  Zu sehen ist seine Staubspur.
* **Die Taint-Krabbe fehlt noch.** Sie hängt nicht am Nest, sondern am Taint, und braucht
  das 7,62-mm-Geschoss samt Partikelpaket.

### Runde 237: die Taint-Krabbe schließt die Krabbenfamilie

Sie hängt nicht am Nest, sondern am Taint: wer als Teslakrabbe in den Taint läuft, kommt
als Taint-Krabbe wieder heraus. Genau diesen Zweig hat `TaintBlock.entityInside` bis jetzt
nicht gehabt.

* **`TaintCrab`** -- fünfundzwanzig Lebenspunkte, anderthalb Blöcke Kantenlänge, Blitze im
  Umkreis von zehn statt drei Blöcken, alle fünf Ticks ein brennendes
  7,62-mm-Vollmantelgeschoss aus bis zu fünfzig Blöcken. Sie flieht nicht, sie zerplatzt
  dreißigmal so stark wie die Grundkrabbe (Sprengkraft 3), und sie verseucht alles binnen
  fünf Blöcken -- im Original mit Strahlung der Stärke sechzehn, nicht mit Taint; die
  CE-Abspaltung hat das später geändert, der Port folgt dem Original.
* Sie lässt null bis zwei Kupferspulen fallen und mit etwa 2,5 Prozent eine magnetisierte
  Wolframspule.
* **`TaintCrabRenderer`** -- `taintcrab.obj` mit den Teilen Body, Legs1 und Legs2.
* **`TaintBlock`**: die Verwandlung der Teslakrabbe.

Dabei berichtigt: die Blitze der Teslakrabbe wurden in Runde 236 mitgedreht, wenn sich die
Krabbe drehte. Das Original zeichnet sie vor `super.doRender`, also in Weltrichtung; beide
Krabbenrenderer tun das jetzt auch.

Offen aus dieser Runde:

* **Der verseuchte Creeper fehlt.** Im Taintblock steht direkt neben der Krabbe ein zweiter
  Zweig, der einen gewöhnlichen Creeper in einen `EntityCreeperTainted` verwandelt. Diese
  Klasse gibt es im Port noch nicht.

### Runde 238: der verseuchte Creeper

Der zweite Zweig aus `TaintBlock.entityInside`, den Runde 237 offen gelassen hat: wer als
gewöhnlicher Creeper in den Taint läuft, kommt als verseuchter Creeper wieder heraus.

* **`CreeperTainted`** -- fünfzehn Lebenspunkte, Tempo 0,35, heilt sich alle zehn Ticks um
  einen Punkt, lässt TNT fallen. Sein Knall hat Sprengkraft fünf ohne Feuer und ohne
  Blockschaden; danach sät er Taint: als gewöhnlicher Creeper fünfundachtzig Würfe in einen
  Würfel von sieben Blöcken Kante, als geladener zweihundertfünfundfünfzig in einen von
  fünfzehn. Wie tief der Taint sitzt, hängt an `TAINT_TRAILS` -- und zwar umgekehrt: ist sie
  aus, bekommt der Taint niedrige Stufen und breitet sich weiter aus.
* **`CreeperTaintedRenderer`** -- dasselbe Creeper-Modell, andere Haut. Das Original hat
  dafür einen gemeinsamen Darsteller (`RenderCreeperUniversal`); in 1.21 braucht ohnehin
  jede Entitätsart ihre eigene Anmeldung.
* **`TaintBlock`**: beide Verwandlungen stehen jetzt nebeneinander, wie im Original. Die
  Creeper-Prüfung geht wie dort über `getClass().equals(...)` statt über `instanceof` --
  sonst würde auch der nukleare und der schon verseuchte Creeper noch einmal verwandelt.

Nicht übernommen: `hasPosNeightbour`. Die Methode steht im Original am Ende der Klasse und
wird von niemandem gerufen.

Damit ist der Taintblock vollständig: beide Zweige, die das Original dort hat, sind da.

**Berichtigt, nachgerechnet:** die beiden OBJ-Krabben standen auf dem Kopf. Der Grund ist
eine Falle, die jede OBJ-Entität des Ports betrifft. In 1.7.10 dreht `RendererLivingEntity`
die Szene vor dem Modell um (`glScalef(-1, -1, 1)`) und schiebt sie um 1,5078 nach unten --
1.21 macht in `LivingEntityRenderer` genau dasselbe. Die beiden Zeilen im Modell des
Originals (`glRotatef(180, 0, 0, 1)` und `glTranslatef(0, -1.5, 0)`) sind nichts anderes als
die Umkehrung davon: nachgerechnet ergibt die ganze Kette die Einheitsabbildung plus 0,0078
in der Höhe. Die Krabbenrenderer des Ports sind aber keine `MobRenderer`, sondern schlichte
`EntityRenderer` -- die Vorwärtsdrehung findet also nie statt, und die Umkehrung drehte das
Modell erst auf den Kopf. Beide Zeilen sind jetzt weg; übrig bleibt die Drehung nach der
Blickrichtung des Rumpfes, und beim Taintkrebs seine eigene Vierteldrehung.

### Runde 239: die Sicherheitszündschnur entblockt den Sprengstoffzweig

Ein einziger fehlender Gegenstand hat einen ganzen Zweig festgehalten. `safety_fuse` war im
Port nicht da -- und damit war **kein einziges** Rezept des Sprengstoffzweigs da: nicht die
fünf Granatenzünder, nicht der Annäherungszünder, nicht die Dynamitstange, nicht der
Dynamit-, TNT- oder Semtexblock. Die Zutaten lagen alle schon herum, nur die Schnur nicht.

Neu: `safety_fuse`, `stick_tnt`, `stick_semtex`.

Dreizehn Rezepte nachgereicht (Original: `WeaponRecipes` Z. 264-296 und `CraftingManager`
Z. 749):

* Zündschnur, achtmal aus Faden und Schwarzpulver.
* Granatenzünder S3, S7, S15, Aufschlag und Luftsprengpunkt, je vier Stück.
* Annäherungszünder (ein Aufsatz, kein Zünder), einer je Bau.
* Dynamit-, TNT- und Semtexstange, je vier Stück.
* Dynamit-, TNT- und Semtexblock: acht Stangen um eine Zündschnur.

**Zwei Zünder, nicht einer:** die Dynamitstange wird mit der Zündschnur gebaut, die TNT- und
die Semtexstange mit der Sprengschnur (`det_cord`). Das steht so im Original und ist keine
Nachlässigkeit -- die Blöcke daraus nehmen dann wieder alle drei die Zündschnur.

Dazu die fehlenden Sprengstoff-Gefahren (`HazardRegistry` des Originals, Z. 166-174):
Dynamitkugel 2, Dynamitstange 1, TNT-Stange 1,5, Semtexstange 2,5, Cordit 2.

Offen aus dieser Runde:

* **Ballistit fehlt.** Im Original nimmt der Aufschlagzünder rauchloses Pulver aus dem
  Erzverzeichnis (`AnySmokeless` = Ballistit oder Cordit); im Port bleibt davon das Cordit.
* **Der Entschärfer-Rüstungsaufsatz fehlt.** Im Original lässt er beim Entschärfen eines
  Creepers eine Zündschnur fallen (`ItemModDefuser`); der Port hat den Entschärfer nur als
  Werkzeug.

### Runde 240: die drei übrigen Creeper, ihr Erscheinen -- und eine Lunte, die ins Leere führte

Damit ist die Creeper-Familie vollständig: nuklear, verseucht, golden, flüchtig, Phosgen.

* **`CreeperGold`** -- kein Schaden, kein Feuer: er verwandelt, was er trifft, in Golderz.
  Sieben Blöcke weit, als geladener vierzehn. Erscheint nur unter Y=40. Fällt Goldkristalle:
  vom Spieler erschlagen fünf plus bis zu fünf weitere, sonst drei.
* **`CreeperVolatile`** -- derselbe Knall, nur hinterlässt er gesprungene Schlacke. Fällt
  Schwefel und TNT-Stangen. Er erbt vom Goldcreeper und überschreibt nur, was sich
  unterscheidet.
* **`CreeperPhosgene`** -- kurze Lunte (zwanzig Ticks statt dreißig), schluckt vier Schaden
  von jedem nicht absoluten Treffer, kleiner Knall ohne Blockschaden und danach eine
  Phosgenwolke: zehn Blöcke breit, fünf hoch, hundertfünfzig Ticks lang.
* **Natürliches Erscheinen** -- das erste im Port überhaupt. Das Original meldet die drei in
  `EntityMappings` an (Phosgen 5, flüchtig 10, golden 1, je ein Stück, alle Biome); hier
  sind es ein `AddSpawnsBiomeModifier` auf `BiomeTags.IS_OVERWORLD` und eine
  Platzierungsregel je Art. Die Prüfung auf `dimension == 0` aus `getCanSpawnHere` entfällt
  dadurch -- sie wäre toter Code.
* **Der gesprungene Schlackeblock.** `block_slag` hat den Zustand `broken` bekommen, wie die
  Metadaten 0 und 1 des Originals. Die Textur `block_slag_broken.png` lag seit Runden im
  Baum, ohne dass ein Block sie zeigte; Runde 194 hatte das im Mörserwerfer ausdrücklich
  vermerkt und zurückgestellt. Jetzt zeigen sie beide: der Mörserwerfer und der flüchtige
  Creeper. Die neue Klasse heißt `SolidSlagBlock` und nicht `SlagBlock` -- **wieder eine
  Namensfalle**: `SlagBlock` ist im Port längst vergeben, nämlich an die Schlackenpfütze
  unter dem Abstich (`slag`, mit Blockentität). Im Original heißen die beiden verschieden
  genug, im Port nicht.

**Berichtigt: der Knall kam fast nie.** Alle Creeper dieser Mod ersetzen den Knall des
Originals durch einen eigenen. Im Original geht das über `func_146077_cc`, die Methode am
Ende des Zählers; in 1.21 heißt sie `explodeCreeper` und ist **privat**. Der Port hatte sich
seit dem nuklearen Creeper an `ignite()` gehängt -- aber dort kommt im Spiel fast niemand
vorbei: `SwellGoal` ruft `setSwellDir(1)`, sobald ein Spieler nah genug ist, und
`Creeper.tick()` lässt den Zähler dann bis `maxSwell` laufen und ruft das private
`explodeCreeper`. Durch `ignite()` kommt nur, wer mit Feuerzeug und Stein anzündet. Ein
nuklearer Creeper, der einfach auf einen Spieler zuläuft, machte also den **gewöhnlichen**
Knall.

`CreeperFuse` liest den Zähler stattdessen über das öffentliche `getSwelling` zurück und
löst zwei Ticks vor dem gewöhnlichen Knall aus. Das kostet ein Zehntel einer Sekunde Lunte;
dafür geht jeder Creeper dieser Mod auf jedem Weg so hoch, wie er soll. Der Phosgencreeper
bekommt darüber auch seine kurze Lunte -- `maxSwell` ist in 1.21 ebenfalls privat.

### Das 41. Tor: Blöcke, die eine Blockentität tragen, aber keine herausgeben

Runde 240 hat einen Fehler gemacht, den kein Tor gesehen hat: beim Anlegen des festen
Schlackeblocks wurde `SlagBlock.java` überschrieben -- der Name war längst vergeben, nämlich
an die Schlackenpfütze unter dem Abstich. Die neue Fassung war gültiges Java und hätte
übersetzt; aus der Pfütze wäre still ein gewöhnlicher Block geworden. Gefunden hat es nur
`git status`, weil die Datei als *geändert* statt als *neu* dastand.

`beblock-check.sh` schaut jetzt darauf: `BlockEntityType.Builder.of(X::new, NtmBlocks.Y.get())`
sagt nur, welche Blöcke diese Art tragen dürfen -- ob der Block selbst eine Blockentität
erzeugt, prüfte niemand. Das Tor sucht für jeden gebundenen Block seine Klasse aus
`NtmBlocks` heraus und läuft die Vererbungskette hinauf.

Gemessen: 260 Arten, 283 gebundene Blöcke, null Funde. Zwei Gegenproben: meldet man den
Schlackenabstich als schlichten `Block` an, meldet das Tor genau ihn; stellt man den Unfall
selbst nach und nimmt `SlagBlock` sein `newBlockEntity`, meldet es die Schlackenpfütze.

### Runde 241: die zehn Rüstungsaufsätze des Roten Zimmers

Der Sockel des roten Zimmers hat im Original **fünfundzwanzig** Einträge. Seit Runde 233
standen dreizehn davon, und der Kopf von `ItemPoolsRedRoom` zählte die zwölf fehlenden
namentlich auf, statt sie zu verschweigen. Gemessen: **zehn der zwölf sind
Rüstungsaufsätze**, und das Baukastensystem dafür gibt es seit Runde 137 --
`ItemArmorMod`, `ArmorModHandler` und acht der neununddreißig Aufsatzklassen des Originals.
Es fehlten also nicht zwölf verschiedene Dinge, sondern im Wesentlichen ein Zweig.

| Gegenstand | Klasse | Steckplatz, Teile | Wirkung |
|---|---|---|---|
| `armor_polish` | `ItemModPolish` | EXTRA, alle vier | jeder zwanzigste Treffer prallt ab |
| `bandaid` | `ItemModBandaid` | EXTRA, alle vier | drei von hundert Treffern heilen ganz, statt zu verletzen |
| `serum` | `ItemModSerum` | EXTRA, alle vier | tauscht Gift gegen hundert Ticks Stärke der Stufe fünf |
| `quartz_plutonium` | `ItemModQuartz` | EXTRA, alle vier | jeder Treffer nimmt zehn RAD ab, der Schaden bleibt |
| `spider_milk` | `ItemModMilk` | EXTRA, alle vier | nimmt Tick für Tick jeden schädlichen Trankeffekt |
| `ink` | `ItemModInk` | EXTRA, alle vier | jeder zehnte Treffer prallt ab und wirft Blumen |
| `heart_container` | `ItemModHealth` (20) | EXTRA, Brustplatte | +10 Herzen |
| `black_diamond` | `ItemModHealth` (40) | EXTRA, Brustplatte | +20 Herzen |
| `scrumpy` | `ModReviveItem` (1) | EXTRA, Hose | die Klasse gab es schon |
| `ballistic_gauntlet` | `ItemModTwoKick` | SERVOS, Brustplatte | **absichtlich wirkungslos** |

Die Spalte *Teile* steht für die vier Merker am Ende des `ItemArmorMod`-Konstruktors: an
welche Rüstungsteile sich der Aufsatz überhaupt stecken lässt. Sie sind aus dem Original
übernommen, nicht vereinheitlicht -- deshalb hängen die beiden Herzaufsätze an der
Brustplatte, der Scrumpy an der Hose.

**`ballistic_gauntlet` ist kein vergessener Rest, sondern einer im Original.** Der Name
kommt im gesamten 1.7.10-Quelltext an genau zwei Stellen vor: in seiner eigenen
Anmeldung und in seiner eigenen Klasse. Nirgends fragt eine Schrotflinte ihn ab. Der
Rückstoßschlag, den der Name verspricht, wurde dort nie eingebaut. Die Portklasse trägt
das im Kopf, damit niemand später eine Wirkung dazuerfindet und meint, sie
wiederhergestellt zu haben.

Damit stehen **dreiundzwanzig von fünfundzwanzig** Einträgen mit den Gewichten des
Originals. Was noch fehlt, ist `starmetal_sword` und `flask_infusion` -- zwei andere
Familien, keine Aufsätze. Der Kopf der Klasse nennt beide weiterhin beim Namen.

### Runde 242: der Entschärfer — ein Werkzeug, das seinen Namen bisher nur trug

Runde 195 hat `defuser` angemeldet, als schlichtes `ToolingItem` mit `ToolType.DEFUSER`.
Als Schraubenschlüssel für Maschinen funktionierte er; **Creeper konnte er nicht
entschärfen**, denn der dafür nötige Griff hing an `ItemModDefuser.castrateCreeper`, und
die Klasse gab es im Port nicht. Ein Seitenschneider, der nur so heißt.

`ItemModDefuser.castrateCreeper` hat im Original **vier** Aufrufer, und drei davon fehlten
dem Port vollständig:

| Aufrufer | im Original | im Port vor Runde 242 |
|---|---|---|
| `ItemDefuser` (Werkzeug) | Rechtsklick auf einen Creeper | Werkzeug da, Griff fehlt |
| `ItemModDefuser` (Aufsatz) | jede Sekunde, Umkreis 5 | `defuser_gold` fehlt ganz |
| `BlockPedestal` | alle 60 Takte, Umkreis 25 | ausdrücklich als Lücke vermerkt |
| `ModEventHandler` | Wiederherstellung nach dem Laden | — |

Alle vier stehen jetzt. Der Sockelkommentar, der die Lücke seit Runde 233 benannt hatte
(*"Eine Abfrage auf einen Gegenstand, den es nicht gibt, wäre eine Zeile, die nie
zutrifft"*), ist eingelöst statt gelöscht.

#### Warum das Entschärfen im Port anders gebaut ist

Das Original nimmt dem Creeper sein `EntityAICreeperSwell` aus der Aufgabenliste. Auf 1.21
steht dem zweierlei im Weg: `Mob.goalSelector` ist geschützt, und **Aufgaben überleben das
Speichern der Welt nicht** -- das Original weiß das und setzt deshalb die Marke
`hfr_defused`, um den Entzug bei jedem Takt jedes markierten Creepers zu wiederholen.

Der Port behält die Marke und macht sie zum Mechanismus selbst: `CommonEvents` hält
markierte Creeper in jedem Takt nieder, mit `setSwellDir(-1)` in `EntityTickEvent.Pre` --
also **vor** `Creeper.tick()`, wo der Zähler steigt. Dasselbe Ergebnis, ohne Griff in die
Aufgabenliste, und die Marke steht in den beständigen Daten der Entität.

**Ein Zugriffstransformer war trotzdem nötig: `Creeper.DATA_IS_IGNITED`.** Wer einen Creeper
mit Feuerzeug und Stein anzündet, setzt ein Merkmal, das `Creeper.tick()` selbst wieder auf
+1 übersetzt -- dagegen hilft kein Niederhalten davor. Öffentlich gibt es nur `ignite()`,
kein Gegenstück. Das ist derselbe Befund wie in Runde 240, eine Ebene weiter: die
interessanten Teile des Creepers sind privat.

#### Zwei Stellen, an denen das Original nachlässig ist -- und der Port es nicht nachbaut

* **Der Schwächetrank.** Das Original schreibt `new PotionEffect(Potion.weakness.id, 0, 200)`.
  Die Signatur ist `(id, Dauer, Stufe)` -- nachgemessen an den 335 `new PotionEffect(`-Aufrufen
  im selben Quelltext, wo an zweiter Stelle durchweg `30 * 20` und dergleichen steht. Dort
  stehen also **null Takte Dauer und Stufe 201**: der Effekt verfällt im selben Takt. Der
  Port dreht es um, zehn Sekunden Schwäche.
* **Die Abnutzung.** `itemInteractionForEntity` nutzt keine Haltbarkeit. Ein Seitenschneider
  entschärft beliebig viele Creeper, ohne stumpf zu werden -- vermutlich ein Versehen, aber
  eines nachzubessern hieße, eine Wirkung zu erfinden. Der Port übernimmt es und schreibt
  dazu, dass er es tut.

#### Nicht übernommen

Der zweite Zweig von `ItemDefuser` sprengt einen sterbenden `EntityGlyphidNuclear` an Ort
und Stelle. **Die Glyphiden gibt es im Port nicht** -- kein einziger der rund zwei Dutzend
`EntityGlyphid*`. Der Zweig kommt mit ihnen, nicht vorher.

#### Das Werkzeug-Tor war blind für Unterklassen

`tool-check.sh` prüft seit Runde 195, dass jede `ToolType`-Sorte beide Seiten hat: einen
Gegenstand, der sie trägt, und mindestens einen Block, der sie abfragt. Es suchte Träger
über drei Muster, deren erstes wörtlich `new ToolingItem(ToolType.X` lautete.

`DefuserItem` erbt von `ToolingItem` und **reicht die Sorte durch**, statt sie
festzuschreiben -- also greift weder das erste Muster noch das `super(ToolType.X` der
Bolzenpistole. Das Tor meldete daraufhin, `DEFUSER` habe keinen Träger, während eine Zeile
über der Meldung einer angemeldet war. Ein falscher Fund, aber aus der richtigen Richtung:
das Tor hätte diesen Fall auch dann nicht gesehen, wenn er ein echter Fehler gewesen wäre.

Das Muster ist jetzt `new \w+\(\s*ToolType\.X` -- der Klassenname spielt keine Rolle mehr.
Gemessen: im ganzen Baum gibt es fünf Stellen dieser Form, und alle fünf sind
Gegenstandsanmeldungen; das breitere Muster fängt nichts Falsches ein. Gegenprobe: nimmt
man dem Entschärfer seine Sorte wieder, meldet das Tor ihn sofort.

### Runde 243: die Schwertfamilie — acht Materialien hatten je vier Werkzeuge und kein Schwert

**Gemessen: das Original hat 25 Gegenstände mit `sword` im Registriernamen. Keiner davon
war im Port angemeldet.** `SwordAbilityItem` steht seit der Werkzeugrunde in
`items/tools/` -- eine vollständige Klasse mit Fähigkeitenliste, Trefferbehandlung und
Hinweistext, die **kein einziger Gegenstand benutzte**. Kein Tor hat das gesehen: sie
zählen Blöcke ohne Blockentität, Werkzeugsorten ohne Träger und Sprachschlüssel ohne
Namenszeile, aber keine Item-Klasse ohne Anmeldung.

Sichtbar wurde es von unten: `starmetal_sword` ist eines der beiden letzten fehlenden
Beutestücke des Roten Zimmers. Die Frage „warum fehlt das eine Schwert" war die falsche --
es fehlten alle.

#### Der Schnitt

| Material | Werkzeuge im Port | Schwert |
|---|---|---|
| Stahl, Titan, Desh, Kobalt, verzierter Kobalt, CMB, Sternmetall, Schrabidium | je 4 | fehlte |
| Mese (`TOOL_ZERO_POWER`) | Spitzhacke | fehlte |
| Legierung, Elektrik | keine | — |

Acht Materialien standen mit **genau vier** Werkzeugen da -- Spitzhacke, Axt, Schaufel,
Hacke -- und das fünfte fehlte durchweg. Neun Schwerter schließen diese Lücke, mit den
Schadenswerten und Fähigkeiten des Originals:

`steel` 6F (STUN), `titanium` 6.5F, `desh` 12.5F (STUN), `cobalt` 12F,
`cobalt_decorated` 15F (BOBBLE), `cmb` 35F (STUN, VAMPIRE), `starmetal` 25F (BEHEADER,
STUN 1, BOBBLE), `schrabidium` 75F (RADIATION 1, VAMPIRE, selten), `dnt` 12F.

`alloy_sword` und `elec_sword` bleiben: deren Materialien haben im Port **überhaupt keine**
Werkzeuge, und das Elektroschwert braucht zusätzlich das Energiewerkzeug. `big_sword` und
`redstone_sword` sind eigene Klassen. Die elf Meteoritenschwert-Stufen sind ein eigenes
System (Amboss-Aufwertung) und gehören nicht in dieselbe Runde.

#### Die Angriffsgeschwindigkeit kennt das Original nicht

1.7.10 hat keine: jeder Gegenstand schlägt gleich schnell. Der Port muss eine Zahl setzen
und nimmt `-2.4F`, den Wert, den Vanilla jedem Schwert gibt -- die Werkzeuge nehmen aus
demselben Grund `-2.8F`, den Wert der Vanilla-Werkzeuge. Die Alternative wäre gewesen, eine
Zahl zu erfinden.

#### Die neunte Fähigkeit

`IWeaponAbility` hatte `/*CHAINSAW,*/` und `/*BOBBLE*/` auskommentiert in seiner Liste
stehen -- zurückgestellt, nicht vergessen. `BOBBLE` kommt jetzt nach, weil der Wackelkopf
seit der Blockrunde da ist: ein erschlagenes Ungeheuer lässt mit einer Wahrscheinlichkeit
von eins zu tausend einen fallen, bei mehr als zwanzig Lebenspunkten eins zu
siebenhundertfünfzig. Der Sprachschlüssel `weapon.ability.bobble` („Luck of the
Collector") lag seit Runden bereit. `CHAINSAW` bleibt aus: die Kettensäge ist nicht
portiert.

Der Sockel des Roten Zimmers steht damit bei **24 von 25** Einträgen. Was noch fehlt, ist
`flask_infusion` -- `ItemFlask` mit `EnumInfusion.SHIELD`; das Schildsystem dafür steht
bereits in `HbmPlayerAttachments` (`maxShield`, `shieldCap`).

#### CI-Fix zu Runde 242: `ItemTags.MUSIC_DISCS` gibt es nicht

Der erste Durchgang von Runde 242 ist an **einem** Fehler gescheitert -- nicht am
Zugriffstransformer, der einwandfrei durchging, sondern an der Zutat des Rezepts:

```
NtmRecipeProvider.java:5267: error: cannot find symbol
                .define('R', ItemTags.MUSIC_DISCS)
```

Der Datentag `minecraft:music_discs` existiert, aber **Vanilla führt ihn nur als
Datendatei, nicht als Feld in `ItemTags`** -- der Quelltext referenziert ihn nirgends,
also gibt es keine Konstante dafür. Der Port geht jetzt denselben Weg wie beim
RBMK-Moderator: `ItemTags.create(ResourceLocation…)`.

**Dafür lässt sich kein Tor bauen, und das ist der ehrliche Befund.** Die Tore laufen
ohne Minecraft auf der Platte -- `maven.neoforged.net` ist hier nicht erreichbar, alles
Übersetzen geschieht in der CI. Ein Tor kann nicht wissen, welche Felder `ItemTags`
führt. Was es stattdessen gibt: der Serverlauf der CI zählt `/ERROR]`-Zeilen, und ein
Rezept mit unbekanntem Tag erzeugt eine. Der Weg über `create` ist also nicht stiller als
der über ein Feld -- er scheitert nur später und lauter statt früher.

### Runde 244: das Schildsystem — sechs Teile, von denen der Port zwei hatte

`HbmPlayerAttachments` führt seit Langem `shield`, `maxShield` und `shieldCap`, samt
Netzwerk- und NBT-Behandlung. **Gelesen oder geschrieben hat sie niemand.** Gegengeprüft
über den ganzen Quellbaum: die übrigen Treffer auf „shield" sind Panzerschreck-Darsteller,
ein G3-Waffenaufsatz und dergleichen -- kein einziger betrifft dieses Feld. Zwei Zahlen,
die gespeichert und übertragen wurden und nichts bedeuteten.

Sichtbar wurde das wieder von unten: `flask_infusion` ist der letzte fehlende Eintrag des
Rote-Zimmer-Sockels. Allein gebaut wäre es ein Trank gewesen, der eine Zahl erhöht, die
niemand liest.

| Teil | im Original | im Port vor Runde 244 |
|---|---|---|
| die Felder | `HbmPlayerProps` | **da** |
| `getEffectiveMaxShield` | `HbmPlayerProps:179` | fehlte |
| Regeneration | `EntityEffectHandler:89` | fehlte |
| Schadensabzug | `ModEventHandler:700` | fehlte |
| Anzeige | `RenderScreenOverlay:246` | fehlte |
| `ItemModShield` (`australium_iii`) | `+25` | fehlte |
| `ItemFlask` (`flask_infusion`) | `+5`, gedeckelt auf 100 | fehlte |

#### Die Regeneration beschleunigt sich, und das ist so gewollt geblieben

Das Original addiert nicht `0.005` je Takt, sondern `0.005 * (Takte seit Beginn der Ruhe)`.
Wer eine Minute nicht getroffen wird, lädt am Ende gut sechzigmal so schnell wie am Anfang.
Das sieht nach einem Versehen aus -- gemeint war vermutlich eine gleichmäßige Rate --,
aber es ist die Kurve, die das Spiel hat. Sie zu begradigen wäre eine andere Waffe gewesen.
Die sechzig Takte Wartezeit vor dem ersten Nachladen stehen ebenfalls unverändert.

#### Drei Stellen, an denen 1.21 einen anderen Weg verlangt

* **Der Aufsatz wird jedes Mal neu gefragt.** `getEffectiveMaxShield` sieht bei jedem Aufruf
  in den Kevlar-Steckplatz der Brustplatte; wer sie ablegt, verliert den Aufschlag im
  selben Takt. Ein gemerkter Wert müsste beim An- und Ablegen nachgezogen werden -- eine
  Buchhaltung, die nur schiefgehen kann. Das Original macht es genauso.
* **Die Leiste verdrängt nichts mehr.** Das Original hängt sich an
  `GuiIngameForge.left_height` und schiebt den Zähler um zehn hoch, damit Hunger und Luft
  darunter rutschen. Diesen Zähler gibt es in 1.21 nicht; die Schildleiste zeichnet sich
  jetzt über der Lebensanzeige und lässt die übrigen, wo sie sind. Den Zähler nachzubauen
  hieße, in jede fremde Leiste einzugreifen.
* **Der Anhang synchronisiert sich selbst.** Das Original schickt nach jeder Änderung ein
  `ExtPropPacket` hinterher; der Port hat `.sync(STREAM_CODEC)` am `AttachmentType`, und
  der Client bekommt den Wert ohne Zutun.

#### Eine Sorte, nicht drei

Die CE-Abspaltung liefert drei Flaschentexturen (`empty`, `shield`, `radpot`). `EnumInfusion`
des 1.7.10-Standes kennt genau **eine** Sorte: `SHIELD`. Der Port meldet nur die an, statt
zwei leere Plätze dazuzuerfinden.

**Der Sockel des Roten Zimmers ist damit vollständig: 25 von 25.** Vier Runden haben ihn
geschlossen -- 233 legte ihn mit dreizehn an und zählte die zwölf fehlenden im Klassenkopf
namentlich auf, 241 reichte die zehn Rüstungsaufsätze nach, 243 das Sternmetallschwert,
244 die Infusionsflasche. Der Kopf zählt jetzt nichts mehr auf, weil nichts mehr fehlt.

### Runde 245: der Tau-Bolzen wird sichtbar — und die Begründung war falsch

Seit Runde 236 schießt die Kybernetische Krabbe einen Bolzen, den `EmptyEntityRenderer`
zeichnet: also gar nicht. Die Aufgabenliste hielt dazu fest, das Original zeichne ihn über
`ResourceManager.projectiles`, Teil `"BulletRifle"`, und diese OBJ-Datei liege nicht im
Port. **Beides stimmt, und trotzdem ist der Schluss falsch.**

`RenderBullet` zeichnet so -- aber das ist das alte Geschosssystem, das im Original selbst
mit `@Deprecated // the entire old bullet system should finally fucking die i hate it`
markiert ist. `EntityBullet`, das die Krabbe wirklich wirft, meldet `ClientProxy:615` bei
**`RenderRocket`** an, und das zeichnet `ModelBullet`: **einen einzigen Kasten**, zwei mal
eins mal eins, auf einer Bildfläche von acht mal vier. Kein OBJ nötig.

Auch die Textur war nicht die erwartete. `RenderRocket` wählt `tau.png` nur bei
`getIsCritical()`; gesetzt wird das ausschließlich im `isTau`-Konstruktor
(`EntityBullet:191`), und die Krabbe nimmt einen anderen (`:82`). Ihr Bolzen trägt
`bullet.png`.

Zwischenzeitlich lagen `projectiles.obj` (132 KB) und `bullet_rifle.png` schon im Baum und
waren im `ResourceManager` angemeldet -- beides zurückgenommen, sobald die Messung zeigte,
dass sie niemand braucht. Eine Datei, die nichts zeichnet, ist derselbe Fehler wie eine
Klasse, die niemand anmeldet.

`ModelBullet` bringt dieselbe Falle mit wie die Krabbe in Runde 236: ein `mirror = true`
**hinter** dem `addBox`. 1.7.10 liest das Feld zum Zeitpunkt des Aufrufs, also ist es
wirkungslos, und der Kasten wird nicht gespiegelt.

### Runde 246: Ballistit — und die Granate hatte die falsche Zahl

Ballistit fehlte dem Port. Runde 240 hatte nachgemessen, dass es **kein Blocker** ist: die
240-mm-Granaten und die DGK-Patrone ließen sich über Cordit bauen, und Cordit entsteht in
der Chemiefabrik. Das stimmt -- und trotzdem fehlte mehr als ein Gegenstand.

**Das Original legt jede 240-mm-Granate dreimal an** (`WeaponRecipes:223` bis `:234`):

| Treibladung | Ausbeute |
|---|---|
| Schießpulver | 4 |
| Ballistit | 4 |
| Cordit | **6** |

Der Port hatte davon einen Zweig: **Cordit mit vier**. Also die Zutat des besseren Pulvers
und der Lohn des schlechteren -- wer den Weg über die Chemiefabrik ging, bekam nichts
dafür. Jetzt stehen alle drei Zweige, jeder mit seiner Zahl.

Die DGK-Patrone hat im Original zwei Zweige (`:250`, `:251`), beide zu einem Stück: dort
lohnt das bessere Pulver tatsächlich nicht, und das bleibt so.

Ballistit selbst kommt aus `PowderRecipes:28` -- Schießpulver, Kaliumnitrat, Zucker,
formlos, drei Stück -- und trägt in der Gefahrenliste `EXPLOSIVE 1F` gegen die `2F` des
Cordits.

**Was diese Runde zeigt:** „kein Blocker" und „nicht nötig" sind zwei verschiedene Dinge.
Die Messung von Runde 240 war richtig und hat trotzdem eine falsche Zahl stehen lassen,
weil sie nur gefragt hat, ob sich etwas *bauen* lässt -- nicht, ob es sich so baut wie im
Original.

### Runde 247: die Erfolge — 61 im Original, einer im Port, und der hieß „test"

Der Port hatte genau eine Erfolgsdatei: `data/hbmsntm/advancement/root.json`,
handgeschrieben, mit `"title": {"translate": "test"}` und leerer Beschreibung. Ein
Erfolgsbaum, der aus einer leeren Wurzel besteht, ist derselbe Fehler wie eine Klasse, die
niemand anmeldet -- er steht da und tut nichts.

Das Original hat **61 Erfolge**, angemeldet als eine `AchievementPage` namens
„Nuclear Tech". Die Trennlinie zwischen ihnen lässt sich messen:

| Auslöser | Anzahl | Weg auf 1.21 |
|---|---|---|
| `triggerAchievement(...)` im Quelltext | 32 | braucht je eine Stelle im Port, die feuert |
| Forges Bau- und Aufnahmeerkennung | 29 | `InventoryChangeTrigger` |

**Diese Runde bringt die zweite Gruppe** -- die Fortschrittskette, vom Brennerpresse bis
zur Fusion. Wer den Symbolgegenstand im Inventar hat, bekommt den Erfolg, genau wie im
Original.

Die 32 getriggerten fehlen noch, und das ist kein Versehen: jeder braucht eine Stelle, die
ihn feuert -- den Tod durch Strahlung, das Betreten des roten Zimmers, den Start einer
Sojus. Sie kommen mit ihren Auslösern, nicht vorher.

**Gitterplätze gibt es in 1.21 nicht mehr.** Das Original setzt jeden Erfolg auf eine
Koordinate (`achBlastFurnace` auf 1,3, `achFusion` auf 13,-7); 1.21 legt den Baum selbst
aus der Vorgängerkette. Die Kette ist übernommen, die Koordinaten entfallen ersatzlos.
Aus `.setSpecial()` wird `AdvancementType.CHALLENGE` -- derselbe gezackte Rahmen.

Zwei Erfolge der Kette fehlen mit benanntem Grund: `achSILEX` (den Block `machine_silex`
gibt es im Port nicht) und `achChicagoPile` (sein Symbol `pile_rod_plutonium` ist im Port
die Metadatensorte `PU239` von `PILE_ROD`, und ein Erfolg auf eine Metadatensorte braucht
ein `DataComponentPredicate`).

Titel und Beschreibungen sind wörtlich die des Originals, bis hin zum ó in „Fólkvangr".

### Runde 248: die ersten fünf getriggerten Erfolge — und ein Trigger, den 1.7.10 nicht braucht

Runde 247 hat die Erfolge gebracht, die über den Besitz eines Gegenstands fallen. Bleiben
die **32 getriggerten**: im Original steht dort schlicht
`player.triggerAchievement(MainRegistry.achX)`, an 32 Stellen im Quelltext.

**Auf 1.21 gibt es dazu kein Gegenstück.** Ein Erfolg wird nicht verliehen, sondern sein
Kriterium wird erfüllt, und Kriterien müssen angemeldet sein. `NtmCriteria.MARKE` ist
dieser Ersatz: **ein** Trigger für alle, mit einer Kennung als Unterscheidung. Die
Alternative wäre ein eigener Trigger je Erfolg gewesen -- 32 Klassen, die sich nur im
Namen unterscheiden.

Fünf der 32 haben im Port bereits eine Stelle, die feuern kann -- gemessen, nicht geraten:

| Erfolg | Stelle im Original | Stelle im Port |
|---|---|---|
| `red_room` | `BlockKeyhole:69`, `BlockRedBrickKeyhole:91` | `KeyholeBlock`, `RedBrickKeyholeBlock` |
| `rad_poison` | `EntityEffectHandler:265` | derselbe Handler, ab 200 RAD |
| `rad_death` | `EntityEffectHandler:241` | derselbe Handler, ab 1000 RAD |
| `go_fish` | `ItemBoltgun:66` | `BoltgunItem` |
| `no9` | `ItemCigarette:55` | `CigaretteItem` |

**Der Bolzen-Erfolg fällt beim Getroffenen, nicht beim Schützen** -- das steht so im
Original und ist leicht zu übersehen: wer von einem Bolzen erschlagen wird, hat ihn sich
verdient.

`go_fish` zeigt im Original ein `achievement_icon` mit der Metadatensorte `GOFISH` -- einen
Symbolgegenstand, den es nur für Erfolge gibt und den der Port nicht hat. Statt ihn
nachzubauen zeigt der Erfolg die Waffe, die ihn verleiht.

Die übrigen 27 warten auf ihre Auslöser: vier Bossmünzen hängen an Bossen, die es im Port
nicht gibt, fünf Digamma-Stufen am Digamma-System, zwei Horizons-Erfolge an den
Gerald-Satelliten. Sie kommen mit ihnen, nicht vorher.

#### CI-Fix zu Runde 247: `save` nimmt eine ResourceLocation

Zwei Fehler, beide dieselbe Zeile in zwei Ausfertigungen:

```
NtmAdvancementProvider.java:70: error: incompatible types: String cannot be converted to ResourceLocation
                .save(speichern, NuclearTechMod.withDefaultNamespace("root").toString(), helper);
```

**Vanilla und NeoForge haben beide ein `save`, und sie nehmen Verschiedenes.** Vanillas
`Advancement.Builder.save(Consumer, String)` nimmt einen String; NeoForges Erweiterung
`save(Consumer, ResourceLocation, ExistingFileHelper)` nimmt eine ResourceLocation. Wer
den `ExistingFileHelper` mitgibt, meint die zweite -- und darf dann nicht noch
`.toString()` schreiben.

Derselbe Befund wie beim Datentag in Runde 242: **kein Tor kann das sehen.** Die Tore
laufen ohne Minecraft auf der Platte, sie kennen keine Vanilla-Signaturen. Was es gibt,
ist die CI -- und die hat es in einem Durchgang gefunden.

### Runde 249: vier Katastrophen-Erfolge — und die Frage, wo sie hängen

Runde 248 hat die fünf getriggerten Erfolge gebracht, deren Auslöser im Port schon standen.
Diese Runde geht dieselbe Frage systematisch an: **welche Datei feuert im Original welchen
Erfolg, und gibt es die Datei im Port?** Gemessen über die 28 Dateien, die
`triggerAchievement` aufrufen:

| Auslöser im Original | im Port | Erfolg |
|---|---|---|
| `TileEntityRBMKBase` | `RBMKBaseBlockEntity` | `rbmk_boom` |
| `TileEntityWatz` | `WatzBlockEntity` | `watz_boom` |
| `EntityCreeperNuclear` | `CreeperNuclear` | `boss_creeper` |
| `EntityNukeExplosionMK3`/`MK5` | beide da | `manhattan` |
| `EntitySpear`, `GenericFluidBlock`, `ItemModKnife`, `TileEntityReactorZirnox`, `TileEntityPADetector` | **fehlen** | — |

Vier Erfolge kommen damit nach. Drei Anmerkungen, die beim Bauen sichtbar wurden:

* **`manhattan` fällt bei jedem Spieler in der Welt**, nicht bei denen in der Nähe -- wer
  die Mod spielt, hat den Knall gehört. Die anderen drei ziehen eine Kugel von fünfzig
  Blöcken; dafür gibt es jetzt `NtmCriteria.markeImUmkreis`, in zwei Fassungen (um einen
  Block, um eine Entität).
* **MK5 hat keinen `did`-Merker** wie MK3. Der Port setzt den Erfolg stattdessen an
  `explosion == null` -- genau derselbe Zeitpunkt, der erste Takt, in dem es losgeht.
* **Drei von vier Symbolgegenständen fehlen dem Port.** `bucket_mud` wird zum Watz-Pellet,
  `coin_creeper` zu Vanillas Creeperkopf (die vier Bossmünzen fehlen alle), und `nuke_boy`
  heißt im Port `nuke_little_boy` -- derselbe Block unter dem vollen Namen, wieder die
  Namensfalle.

Der Erfolgsbaum steht damit bei **29 von 61**. Was noch fehlt, wartet auf Teilsysteme:
die vier Bosse, das Digamma-System, die Gerald-Satelliten, der Sojus-Start, der
ZIRNOX-Reaktor, der Speer und das Messer.

#### CI-Fix zu Runde 249: die Hilfsmethode kam nie an

Drei Fehler, alle derselbe: `cannot find symbol: method markeImUmkreis`. Die Methode war
zuerst versehentlich in `RBMKBaseBlockEntity` gelandet, sollte nach `NtmCriteria` wandern
-- und **das Skript, das sie dorthin verschieben sollte, ist auf halbem Weg abgebrochen**.
Der Teil, der sie aus der Blockentität entfernte, lief; der Teil, der sie in `NtmCriteria`
eintrug, nicht.

Gesehen hätte man das mit einem Blick: ich habe nach dem Verschieben die *Aufrufstellen*
geprüft und nicht das *Ziel*. Die Aufrufe standen alle da und sahen richtig aus -- nur gab
es die Methode nirgends mehr.

**Kein Tor kann das fangen**, obwohl es diesmal nicht an Minecraft liegt: `import-check`
prüft, ob jeder benutzte **Typ** erreichbar ist, nicht jede benutzte **Methode**. Ein Tor
für Methodenaufrufe müsste den ganzen Baum typauflösen -- das ist ein Übersetzer, kein
Tor. Was bleibt, ist die Regel: nach einem Skript, das abbricht, prüfen was davon lief,
und nicht nur, was man sehen wollte.

## Standortbestimmung nach Runde 249: was noch fehlt, hängt an einem Fundament

Die Punkte, die seit Runden als offen geführt wurden, sind bis auf drei abgearbeitet. Diese
drei hängen **alle am selben fehlenden Fundament** -- und das ist eine Messung, kein
Eindruck:

### Das NBT-Bauwerkssystem

Das Original hat in `world/gen/nbt/` elf Dateien mit zusammen **1975 Zeilen**:
`NBTStructure`, `JigsawPiece`, `JigsawPool`, `SpawnCondition`, `INBTBlockTransformable`
(allein 233 Zeilen Drehlogik für jeden Blocktyp), `INBTTileEntityTransformable` und fünf
Selektoren. Dazu **79 `.nbt`-Dateien** mit den Bauwerken selbst.

**Der Port hat davon nichts, und auch keinen Ersatz.** Kein `StructureTemplate`, kein
`StructurePiece`, kein `JigsawPlacement`; in `world/gen/` stehen nur die drei
Feature-Provider, die Erze, Öl, Landminen und Bombenkrater setzen. Null `.nbt`-Dateien.

### Was daran hängt

1. **Das Meteoritenverlies.** `meteor_spawner` und `dungeon_spawner` setzt niemand, weil
   es keine Struktur gibt, die sie enthält.
2. **Die vier Bauzauberstäbe.** Eine frühere Notiz hielt fest, sie hingen *nicht* am
   Bauwerkssystem. Nachgemessen: technisch richtig, sachlich irreführend.
   `BlockWandLogic` und `BlockWandLoot` haben je **genau einen** Bezug -- die Methode
   `transformTE`; die dreißig bzw. neununddreißig Treffer auf `nbt.` waren gewöhnliches
   `NBTTagCompound`. Aber ihr **Zweck** ist das System: der Beutestab trägt den Hinweistext
   *„Define loot crates/piles in .nbt structures"* und ersetzt sich, wenn eine Struktur ihn
   platziert. Ohne Strukturen ist er ein Block, den nie etwas auslöst.
3. **Ein Teil der 32 offenen Erfolge**, über `LogicBlock`: `LogicBlockConditions` stellt
   den Sockel auf (*„Find a great ancient weapon"*) und ist ein Logikblock **in** einer
   Struktur, der auf Redstone reagiert.

### Warum das kein reines Portieren ist

1.7.10 hat kein Bauwerkssystem, also hat das Original sich eines gebaut. **1.21 hat eines**
-- `StructureTemplate`, `Structure`, `StructurePool`, Jigsaw, alles über Datenpakete und
Datagen. Die 1975 Zeilen Zeile für Zeile zu übertragen wäre falsch; richtig ist, die
Bauwerke des Originals in Vanillas System zu überführen. Das ist Neukonstruktion mit dem
Original als Vorlage -- und der größte verbliebene Brocken des ganzen Ports.

Erster Schritt beim Bau: prüfen, welche der 79 `.nbt`-Dateien 1.21 unverändert laden kann.
Das Format hat sich geändert -- Palette statt Blockkennziffern.

> **Nachgeholt in den Runden 250 und 251.** Antwort auf die Frage des letzten Absatzes:
> **keine einzige**. Der Umsetzer steht jetzt, das Meteoritenverlies wird gebaut, und von
> den vier Bauzauberstäben sind zwei erledigt. Siehe *Runde 250/251* am Ende.

## Runde 250/251 — Das Meteoritenverlies steht in der Welt

Die Standortbestimmung nach Runde 249 nannte drei offene Punkte und ein gemeinsames
Fundament. Das Fundament ist gebaut.

### Runde 250: zwei Blöcke, die Runde 91 übersehen hat

`meteor_brick_mossy` und `meteor_brick_cracked`. Im Kreativreiter fallen sie kaum auf — sie
sehen aus wie der glatte Meteoritenziegel mit Moos beziehungsweise Rissen. An jeder Wand des
Verlieses stehen sie trotzdem: dessen Blockwähler ersetzt jeden gesetzten `meteor_brick` zu
vier Zehnteln durch sich selbst, zu drei durch den bemoosten und zu drei durch den rissigen.
Ohne die beiden wäre das ganze Verlies gleichförmig glatt.

Danach meldete `tools/structure-gap.py` nur noch **vier** echte Lücken — und alle vier waren
Bauzauberstäbe.

### Runde 251: der Umsetzer, die Vorlagen, die Struktur

**Der Umsetzer** (`tools/nbt2structure.py`, dazu `tools/extract-structures.sh`). Die
`.nbt`-Dateien des Originals sehen aus wie Strukturblock-Dateien von 1.21 — gleiche
Schlüssel, gleiches gzip-NBT — sind es aber nicht: ihre Palette nennt Blöcke im Namensschema
von vor der Flattening-Umstellung, mit der Metadaten-Zahl als Eigenschaft, und das Feld
`DataVersion` fehlt ganz. Jeder Block wird einzeln übersetzt.

**Der Umsetzer rät nicht.** Ein `(Name, meta)`-Paar, das nicht in seiner Tabelle steht,
bricht den Lauf ab. Das ist der Unterschied zum Original, das einen unbekannten Blocknamen
stillschweigend zu Luft macht (`BlockDefinition`: *„if(block == null) block = Blocks.air"*) —
genau das passiert dort heute mit `hbm:tile.ladder_tungsten`, die es nicht mehr gibt: **das
Leiterzimmer des Meteoritenverlieses hat im Original keine Leiter mehr.** Der Port setzt die
Stahlleiter, die dasselbe ist und die es noch gibt.

Was der Umsetzer außerdem nachrechnet, weil 1.7.10 es beim Zeichnen tat und nicht in den
Daten hatte: die **Eckenform jeder Treppe** und die **vier Verbindungen jedes Zaunfelds**.

**38 Bauwerksdateien** liegen jetzt unter `data/hbmsntm/structure/meteor/` — 9729 Blöcke.

**Die Zauberstäbe.** Von den vieren sind nach dieser Runde noch zwei offen:

| Stab | im Port |
|---|---|
| `wand_jigsaw` | **braucht keinen Block** — in 1.21 ist das `minecraft:jigsaw`. Richtung, Pool, Ziel, Ersatzblock und die beiden Prioritäten gehen eins zu eins über |
| `wand_loot` | **portiert** — Block, Blockentität, Selbstersatz beim ersten Servertick |
| `wand_logic` | offen; gehört zu den Logikblöcken der Verliese |
| `wand_tandem` | offen; verzögertes Nachsetzen von Bauwerksteilen |

**Die Blockwähler** des Originals (`Component.MeteorBricks` und drei Geschwister) sind
Prozessorlisten geworden. Deren Regeln werden der Reihe nach geprüft und die erste
zutreffende gewinnt — die Zahlen sind deshalb **bedingte** Wahrscheinlichkeiten: drei Zehntel
für den bemoosten, dann drei Siebtel des Rests für den rissigen, was wieder drei Zehntel
ergibt.

**Die Spitze**, die aus dem Boden ragt, trägt im Original `conformToTerrain` mit
`heightOffset = -3`: jede einzelne Spalte wird auf die Geländehöhe gesetzt und dann drei
Blöcke abgesenkt. Der Schwerkraftprozessor von 1.21 tut genau das.

### Drei Stellen, an denen 1.21 nicht dasselbe kann

1. **Größe.** Das Original zählt *Stücke* und hört bei 128 auf. 1.21 zählt *Tiefe* und lässt
   höchstens 20 zu. Im Port stehen 7 — wie bei Vanillas Dörfern, die damit auf rund hundert
   Stücke kommen.
2. **Reichweite.** Original 128 Blöcke vom Mittelpunkt, der benutzte Erbauer von 1.21 setzt
   80 fest.
3. **Biome.** Das Original fragt `biome.rootHeight >= 0` ab. Die Zahl gibt es nicht mehr; an
   ihrer Stelle steht eine Liste aller Oberflächenbiome außer Ozeanen, Flüssen und Sümpfen,
   dazu die drei Höhlenbiome. Genau diese drei Gruppen liegen in 1.7.10 unter null.

### Wie selten das Verlies steht — nachgerechnet, nicht geschätzt

Das Original legt **ein** Raster von zwölf Chunks über die Welt und verlost in jeder Zelle
*ein* Bauwerk unter allen, die im dortigen Biom stehen dürfen. Für die Ebene summieren sich
die Gewichte auf **422**; das Verlies zieht mit Gewicht 1 mit. Es steht also in einer von 422
Zellen.

1.21 verlost nicht — jedes Bauwerk bekommt sein eigenes Raster, und die Dichte steckt allein
im Rasterabstand. Gleiche Dichte heißt: Abstand mal Abstand gleich 144 mal 422, also **246
Chunks**, Zwischenraum 82. Das ist rund ein Verlies auf 3900 mal 3900 Blöcke — so selten wie
im Original.

### Was an der Beute fehlt, und warum

Kein Eintrag verschwindet stillschweigend; alles steht an seiner Stelle im Quelltext:

- **Der Tresor** (`hbm:tile.safe`) ist im Port eine Vanilla-Truhe mit demselben Vorrat. Die
  Beute ist erreichbar, das Schloss fehlt.
- **Der Vorrat des Tresors** besteht im Original aus dem Buch und acht Stempelbüchern. Die
  Stempelbücher gibt es im Port nicht — es bleibt der eine Eintrag.
- **Die Schatztruhe** verliert vier von zwanzig Einträgen: `pill_herbal`, `heart_piece`,
  `egg_glyphid`, `blueprint_folder`.
- **`LOOT_METEOR`** — das Beuterezept des MKU-Stücks — braucht das MKU-Rätsel des Originals:
  ein je Welt neu gewürfeltes Rezept samt Buch, das darauf hinweist. Weder das Rätsel noch
  seine Zutaten noch das Lorebuch gibt es im Port. Der Sockel bleibt dort leer; er wird
  **nicht** mit etwas anderem gefüllt.
- **Die Statue** im Stück `meteor-3-statue` ist im Original `statue_elb_f`, registriert unter
  dem Namen `#undef` — ein versteckter, unzerstörbarer, selbstleuchtender Block mit eigenem
  Modell. Der Port hat die Statuenfamilie nicht; an ihrer Stelle steht der Sockelstein.

### Gemessener Stand danach

`tools/structure-gap.py`: von 185 Blocknamen der 79 Bauwerke sind **157** angelegt, **11**
über Familien abgedeckt, **15** brauchen keinen Block. **Echt fehlend: 2** — `wand_logic`
und `wand_tandem`.

**Bauwerke in der Welt: 1 von 79.** Der Umsetzer gilt ab jetzt für alle; was den übrigen
achtundsiebzig fehlt, ist je eine eigene Platzierung.

## Runde 252 — Elf weitere Erfolge, acht davon mit neuem Auslöser

Der Erfolgsbaum stand nach Runde 249 bei 29 von 61. Der Grund war nie die Datenerzeugung,
sondern die Regel, die seit Runde 247 gilt: **ein getriggerter Erfolg kommt mit seinem
Auslöser, nicht vorher.** Ein Erfolg ohne die Stelle, die ihn feuert, ist ein Eintrag im
Fortschrittsblatt, den niemand jemals bekommt.

Diese Runde verdrahtet acht Auslöser, die im Port längst standen — und in vier Fällen sogar
als `// todo` im Quelltext:

| Erfolg | Auslöser im Port |
|---|---|
| `horizons_start` | `SatelliteHorizons.onOrbit` — der Satellit erreicht die Umlaufbahn |
| `horizons_end` | `SatelliteHorizons.theHorizons` — Tom fällt vom Himmel |
| `zirnox_boom` | `ReactorZirnoxBlockEntity.meltdown` — hundert Blöcke Umkreis, wie im Original |
| `omega12` | `MachinePADetectorBlockEntity` — ein Digamma-Teilchen im Detektor |
| `hidden` | verseuchter Creeper, erschlagen von einem fallenden Güterwagen |
| `stratum` | der erste Gneis, mit den fünfhundert Erfahrungspunkten des Originals |
| `slimeball` | ein aufgehobener Schleimball |

Beim Gneis prüft das Original eigens, ob der Erfolg schon steht — sonst wäre ein Gneisbruch
eine Erfahrungsmühle. Auf 1.21 steht dieselbe Auskunft im Fortschrittsblatt des Spielers, und
die Prüfung ist übernommen.

Dazu drei, die keinen Auslöser brauchen, weil das Original sie ebenfalls ohne einen anmeldet:
`horizons_bonus`, `sacrifice` und `impossible`. Der unmögliche Erfolg zeigt `nothing` — einen
Gegenstand, den niemand bekommt. Das ist der Witz, und er bleibt.

**Zwei Symbole gibt es im Port nicht**, und beide bekommen eines, das dasselbe meint: das
`achievement_icon` mit Spielart ACID wird zum Schleimball selbst, das mit QUESTIONMARK zum
Güterwagen-Sprengkopf.

### Stand: 40 von 61

Von den 32 getriggerten stehen **17**. Die fehlenden 15 warten auf Inhalte, die der Port noch
nicht hat: die Sojus-Rakete (`soyuz`, `space`), vier der fünf Bosse, die vierteilige
Digamma-Kette samt `HbmLivingProps`, die Schimmerwaffen (`fiend`, `fiend2`), das Injektormesser
(`someWounds`), der Radiumkaffee (`radium`), der Speer (`digammaKauaiMoho`) und ein
Säure-Flüssigkeitsblock (`sulfuric`).

Von den 29 nicht getriggerten fehlen sechs: `tasteofblood` und `c20_5` (Symbolgegenstände, die
der Port nicht hat), `potato` (Kartoffelbatterie), `SILEX` (`machine_silex`), `chicagoPile`
(sein Symbol ist im Port eine Metadatensorte und braucht ein `DataComponentPredicate`) und
`digammaUpOnTop` (hängt an der Digamma-Kette).

`inferno` ist messbar nah: Güterwagen, Bomblet und beide Maschinen gibt es, aber der Port
reicht bei einer Explosion den Verursacher nicht an die Maschine weiter. Das ist eine eigene
Runde wert, keine Zeile.

## Runde 253 — Der Tandemstab war nie gebraucht, und der Rest ist gezählt

Die vier Bauzauberstäbe waren einer der drei offenen Punkte. Nachgemessen, in welcher der
79 Bauwerksdateien jeder von ihnen überhaupt vorkommt:

| Stab | Dateien | Folge |
|---|---:|---|
| `wand_jigsaw` | 42 | in 1.21 `minecraft:jigsaw`; kein eigener Block |
| `wand_loot` | 34 | seit Runde 251 portiert |
| `wand_logic` | 4 | `crane`, `crane_mod`, `factory`, `tower_base` |
| `wand_tandem` | **1** | nur `test-tandem-core.nbt` |

**Der Tandemstab steht in genau einer Probedatei des Urhebers**, die kein `SpawnCondition`
benutzt. Kein Bauwerk, das der Mod setzt, braucht ihn. Seine Aufgabe — ein Anschlussstück erst
setzen, wenn die Chunks davor geladen sind — beschreibt er selbst als Umgehung eines Fehlers im
Vanilla-Bauwerkssystem von 1.7.10, und in 1.21 setzt dieses System seine Stücke ohnehin
chunkweise. `tools/structure-gap.py` führt ihn jetzt mit dieser Begründung unter „kein Block
nötig".

**Damit bleibt von den vieren einer:** `wand_logic`. Er wird zum `logic_block` — dem Kopf eines
eigenen Teilsystems von rund tausend Zeilen (`LogicBlockActions` allein 554), das Fallen,
Wellen von Gegnern und einstürzende Decken in vier Bauwerken auslöst, die der Port noch nicht
baut. Ihn jetzt anzulegen hieße, einen Block zu haben, den nichts setzt und nichts auslöst.

### Zwei Verbesserungen am Umsetzer

**Er liest jetzt alle 79 Dateien.** Mindestens eine (`crane.nbt`) hat hinter dem gzip-Strom noch
Datenmüll stehen; Pythons `gzip.decompress` bricht darauf ab, Java liest den Strom bis zum Ende
des NBT und schaut nicht weiter. `zlib.decompressobj` tut dasselbe. Nachgeprüft: die 38 bereits
umgesetzten Dateien kommen danach Byte für Byte gleich heraus.

**Luft mit Metadaten ist Luft.** In etlichen Dateien steht `minecraft:air` mit einer
Metadaten-Zahl ungleich null — Reste davon, was vor dem Abspeichern an der Stelle stand.

### Was noch vor dem Umsetzer liegt — gezählt, nicht geschätzt

`tools/nbt2structure.py --fehlliste` zählt auf, welche `(Name, meta)`-Paare der Tabelle
fehlen. Über alle 79 Dateien:

| | |
|---|---:|
| offene Paare gesamt | **603** |
| davon Vanilla | 168 auf 56 Blocknamen |
| davon `hbm:` | 435 auf 161 Blocknamen |

Für die 33 Bauwerke, die ein `SpawnCondition` wirklich baut (ohne das Verlies), sind es 502
Paare. Von deren `hbm:`-Blocknamen sind **82 ohne Metadaten und im Port unter demselben Namen
vorhanden** — die sind eine Zeile pro Stück. Die 73 Namen mit Metadaten sind die eigentliche
Arbeit: Treppen und Stufen, die ganze Rohrfamilie, die sechzehn Betonfarben, Türen, Lampen.

Das ist der nächste Brocken, und er ist jetzt jederzeit nachzählbar statt zu schätzen.

## Runde 254 — Die Flattening-Tabelle für Vanilla ist vollständig

Von den 603 offenen `(Name, meta)`-Paaren des Umsetzers waren 168 Vanilla-Blöcke auf 56
Namen. Sie sind jetzt alle übersetzt — **null offene Vanilla-Paare über alle 79 Dateien**.

Die Tabelle ist nicht abgeschrieben, sondern aus den Regeln gebaut, die vor dem Flattening
galten: die Farbliste in ihrer damaligen Reihenfolge, die sechs Holzarten, die zwei unteren
Bit einer Treppe als Richtung und das dritte als Deckenanbau, die unteren drei Bit einer
Halbstufe als Werkstoff und das vierte als obere Hälfte. Zwei Zeilen Code decken damit jeweils
ein Dutzend Paare ab.

### Vier Dinge, die in den Daten gar nicht stehen

1. **Die Eckenform jeder Treppe** und **die Verbindungen jedes Zauns** rechnete 1.7.10 beim
   Zeichnen aus den Nachbarn aus. Beides holt der Umsetzer seit Runde 251 nach.
2. **Die obere Hälfte einer Tür** trägt in 1.7.10 nur das Scharnier, die untere nur die
   Richtung. 1.21 will beides in beiden, sonst steht die Tür verdreht.
3. **Die obere Hälfte einer doppelhohen Pflanze** trägt keine Sorte — welche Pflanze es ist,
   steht nur unten.
4. **Die Drehung eines Schädels** und **der Inhalt eines Blumentopfs** standen in der
   Blockentität; seit 1.13 sind sie Teil des Blockzustands beziehungsweise ein eigener Block.

Alle vier laufen als Nachlauf über das fertige Gitter, bevor die Palette gebaut wird.

### Stand

| | |
|---|---:|
| offene Paare über alle 79 Dateien | **435** |
| davon Vanilla | **0** |
| davon `hbm:` | 435 auf 161 Blocknamen |

Die 38 Dateien des Meteoritenverlieses kommen nach der Änderung Byte für Byte gleich heraus —
nachgeprüft mit `diff -r`.

## Runde 255 — Die Umsetzungstabelle ist bis auf einen Block fertig

Nach Runde 254 waren noch 435 `(Name, meta)`-Paare auf 161 `hbm:`-Blocknamen offen. Jetzt
sind es **7 Paare auf 2 Namen** — und beide sind Bauzauberstäbe.

Der Weg dahin, in vier Schritten:

1. **69 Namen eins zu eins.** Blöcke, die im Port genauso heißen und in den Bauwerken nur mit
   Metadatum null vorkommen. Ihr Standardzustand ist die richtige Übersetzung.
2. **Die Familien.** Dreizehn Treppen, drei Stufenblöcke samt ihren Doppelstufen, die
   vierundzwanzig Rohre, die sechzehn Betonfarben, die acht Töne des erweiterten Betons, die
   Türen. Die Reihenfolgen sind nicht geraten: sie stehen in `ModBlocks.java`, wo die
   Werkstoffe dem Erbauer eines `BlockMultiSlab` in genau dieser Folge übergeben werden.
3. **Die Einzelstücke.** Scheinwerfer (Bit 0 heißt zerschossen, die oberen drei sind die
   Seite), Gitter (das Metadatum ist die Höhe in Achteln), Panzerbeton (der Verfall), toter
   Bewuchs, Leuchtstein, Amboss, Falltür, Schmalspurgleis, Wackelkopf.
4. **Die Mehrblockmaschinen.** In 1.7.10 sagt ein Metadatum dreierlei zugleich, und der
   Kopfkommentar von `BlockDummyable` erklärt es: 0–5 ein Platzhalter, 6–11 ein Platzhalter
   mit Merker, 12–15 der Kern — und die Richtung ist die Zahl minus 0, 6 beziehungsweise 10.
   Der Port hat dafür zwei Eigenschaften statt einer Zahl, `facing` und `type`.

### Ein Tor, das an einer Stelle nicht hingesehen hat

`tools/structure-gap.py` las nur die **Palette** jeder Bauwerksdatei. Ein Beutestab nennt den
Block, zu dem er wird, aber in **seiner Blockentität** — in der Palette steht nur der Stab.
Damit war der Aktenschrank (`filing_cabinet`) in vier Bauwerken unsichtbar und hat in keiner
Zählung gefehlt.

Das Tor liest jetzt beides. Ergebnis: **drei echte Lücken** statt einer.

| fehlt | wo |
|---|---|
| `filing_cabinet` | `oil_rig`, `radio_house`, `laboratory`, `factory`, `crane_mod` |
| `safe` | `meteor-3-book` (seit Runde 251 durch eine Truhe vertreten) |
| `wand_logic` | `crane`, `crane_mod`, `factory`, `tower_base` |

Dabei fiel noch eine Namensverschiebung auf: der graue RTG heißt im Original im Feld
`machine_rtg`, angemeldet aber als `machine_rtg_grey`. Der Port nimmt den Feldnamen.

### Zwei Zahlen, die niemand mehr auflösen kann

Zwei Beutestäbe nennen ihren Ersatzblock als **Zahl** — 557 und 683. 1.7.10 löst einen
Blocknamen, der eine Zahl ist, über die Blockkennziffer auf, und die galt nur in der Welt des
Urhebers. Sie sind nicht auflösbar und bleiben ein Fehler des Umsetzers, kein stiller Ersatz.
Die dritte solche Zahl, 54, ist die Vanilla-Truhe und damit eindeutig.

## Runde 256 — Der Tresor steht selbst da

Runde 251 hat im Meteoritenverlies eine Vanilla-Truhe an die Stelle des Tresors gesetzt, weil
es den Block im Port nicht gab, und das dort als Abweichung vermerkt. Jetzt gibt es ihn.

**Er war nie eine eigene Klasse.** Im Original ist `safe` dasselbe `BlockStorageCrate` wie die
Vorratskisten — nur mit fünfzehn Fächern statt sechsunddreißig und mit dem Bild auf der
**Vorderseite** statt auf dem Deckel. Genau dafür hat `BlockStorageCrate.getIcon` einen eigenen
Zweig: `side == metadata` statt der sonstigen Deckelprüfung.

Im Port heißt das: eine Art mehr in `CrateBlock.Type`, eine Blockentität von fünfzehn Fächern
in drei Reihen zu fünf (eingerückt um zwei Fachbreiten, wie in `ContainerSafe`), und ein
Modell, das sein Bild vorne trägt. Das Schloss kommt mit — jede Kiste des Ports stammt von
`LockableBaseBlockEntity` ab.

Seine Sprengfestigkeit von **10000** ist die des Originals: er soll eine Kernwaffe überstehen.

Der Umsetzer setzt jetzt den Tresor statt der Truhe; genau eine der 38 Vorlagendateien ändert
sich dadurch (`meteor-3-book`).

`tools/structure-gap.py`: **zwei echte Lücken** statt drei — `filing_cabinet` und `wand_logic`.

## Runde 257 — Das Tor, das den falsch geschriebenen Zustand findet, und sein eigener blinder Fleck

Der Umsetzer schreibt Blockzustände als Zeichenketten in die `.nbt`-Datei: `"facing"`,
`"axis"`, `"layer"`. Ob der Block diese Eigenschaft überhaupt hat, prüft dort niemand — und
Minecraft prüft es auch nicht. `NbtUtils.readBlockState` geht vom **Standardzustand** aus und
setzt nur, was es kennt; eine unbekannte Eigenschaft wird stillschweigend überlesen. Aus einer
Treppe mit `"facng"` wird eine Treppe nach Norden, aus einer Säule mit falscher Achse eine
stehende. Kein Absturz, kein Protokolleintrag. Das Bauwerk steht nur schief, und niemand weiß
warum.

`tools/structure-state-check.sh` (43. Tor) hält deshalb jede Eigenschaft, die der Umsetzer
erzeugt, gegen die Blockklasse des Ports: Blockname → Klasse aus `NtmBlocks.java`,
Eigenschaften aus dem `createBlockStateDefinition` der Klasse samt ihrer Oberklassen. Vanilla-
Blöcke bleiben außen vor — deren Klassen liegen nicht auf der Platte, die Tore laufen ohne
Minecraft.

**Nachgemessen:** 876 der 1367 Tabelleneinträge zeigen auf einen Block des Ports, zusammen 228
verschiedene.

**Zwei echte Funde:**

- `turret_sentry_damaged` stand in der Mehrblock-Tabelle und bekam damit `facing` und `type`.
  `TurretSentryBlock` erbt von `BaseEntityBlock` und hat **keine** Eigenschaft. Nachgemessen
  über alle 79 Rohdateien: der Block kommt **genau einmal** vor, mit Meta 0, in `crane_mod.nbt`
  — und war über `EINS_ZU_EINS` längst abgedeckt.
- `door_bunker_bulkhead` stand in der Türliste. Diese Tür gibt es im Port nicht (kein Treffer
  unter `src/`), und in keiner der 79 Rohdateien kommt sie vor.

### Der blinde Fleck des Tors war das Tor selbst

Der Gegenversuch — eine Eigenschaft absichtlich falsch schreiben, messen, zurückkopieren —
ergab beim **zweiten** Lauf wieder einen Fund, obwohl die Datei wiederhergestellt war. Der
Grund lag nicht im Umsetzer, sondern im Lader: das Tor holte den Umsetzer über
`importlib.util.spec_from_file_location`, und Python hält ein `.pyc` für gültig, wenn Größe und
Änderungszeit **auf die Sekunde genau** passen. `"axsi"` ist genauso lang wie `"axis"`, das
Zurückkopieren fiel in dieselbe Sekunde — also maß das Tor eine Datei, die es auf der Platte
nicht mehr gab.

Ein Tor, das den Quelltext neben sich liegen lässt und stattdessen einen Zwischenspeicher
misst, ist schlimmer als keins: es meldet grün für etwas, das nie gelaufen ist, oder rot für
etwas, das nie geschrieben wurde. Beide Lader — dieses Tor und `tools/structure-check.sh` —
lesen jetzt den Quelltext und übersetzen ihn selbst; `sys.dont_write_bytecode = True` sorgt
dafür, dass gar kein `.pyc` mehr entsteht.

**Nachgemessen nach dem Umbau:** Gegenversuch rot mit genauer Zeile
(`dungeon_chain (ClimbableChainBlock): "axsi"`), Wiederherstellung **mit aufgeprägter
Änderungszeit der verfälschten Fassung** wieder grün. Genau der Fall, der das Tor vorher
getäuscht hat. Die 38 Meteorvorlagen setzt der Umsetzer weiterhin byte-identisch um.

## Runde 258 — Sieben Erfolge, und drei, die im Original selbst niemand bekommt

39 von 61 standen nach Runde 252 (die Runde selbst schrieb 40 -- sie zählte die eigene Wurzel
mit, die das Original nicht hat). Diese Runde bringt sieben, und sie trennt dabei zwei Dinge,
die bis hierher vermischt waren: was der Port **noch nicht kann**, und was das Original
**selbst nicht tut**.

### Vier mit neuem Auslöser

**Die Digamma-Kette — sehen, fühlen, wissen.** Das Original hängt sie an drei Schwellen
desselben Wertes: über null, ab zwei, ab zehn Drx. Der Port liest diesen Wert ohnehin jeden
Tick, in `EntityEffectHandler.handleDigamma`. Ein Detail entscheidet dort über einen der drei:
die Methode springt bei `digamma < 0.1F` zurück, der Schweißtropfen fängt erst dort an — die
Erfolgsprüfung steht deshalb **vor** dem Rücksprung, sonst bekäme niemand den ersten der drei,
und niemand merkte es.

**Inferno** hängt am Flüssigkeitstank: gefüllt mit etwas Brennbarem, zerlegt von einem
Zeta-Bomblet, verliehen an jeden Spieler im Umkreis von hundert Blöcken. Das Original hat
dieselbe Stelle ein zweites Mal in der Raffinerie; im Port hat deren Block kein
`onBlockExploded` — sie zerlegt sich nur selbst, von außen ist sie nicht sprengbar. Diese
zweite Stelle kommt mit dem Verhalten, nicht vorher.

### Einer, dessen Begründung nicht trug

**Chicago Pile** stand seit Runde 247 als zurückgestellt vermerkt, weil sein Symbol
`pile_rod_plutonium` im Port eine Metadatensorte von `PILE_ROD` ist. Das war ein Grund gegen
das Symbol, nicht gegen den Erfolg: sein Auslöser ist das Bauen von `billet_pu_mix`, und genau
den Gegenstand gibt es im Port. Er zeigt jetzt den Gegenstand, der ihn verleiht.

### Zwei, die das Original nie vergibt

Nachgemessen über den ganzen Quelltext des Originals: **`tasteofblood`, `c20_5` und
`digammaUpOnTop` stehen in keinem `triggerAchievement`-Aufruf und in keiner Zeile des
`AchievementHandler`.** Sie stehen auf der Erfolgsseite und sind dort genauso unerreichbar wie
`impossible`. Das ist kein Loch im Port — es ist der Zustand der Vorlage.

Die ersten beiden stehen jetzt da, mit demselben Kriterium wie `impossible`: dem Gegenstand
`NOTHING`, den niemand bekommt. Eine Marke mit einer Kennung, die nirgends gefeuert wird, wäre
dasselbe in umständlich — und sie sähe aus wie eine vergessene Verdrahtung. `digammaUpOnTop`
fehlt weiter, aber aus einem anderen Grund: er hängt an `digammaKauaiMoho`, und den gibt es im
Port noch nicht. Er kommt mit seinem Vorgänger.

### Der Stand

**46 von 61**, in 47 Knoten — der eigenen Wurzel, die 1.21 verlangt und die das Original nicht
hat.

*Berichtigung zur Zählung:* Runde 252 schrieb „40 von 61" und zählte diese Wurzel als einen der
61 mit. Gemessen waren es 39. Die Zahl hier ist gegen die 61 Namen aus `MainRegistry` geprüft,
Name für Name.

Von den 32 getriggerten des Originals stehen **20**. Die fehlenden 15 warten nicht auf Arbeit am
Erfolgssystem, sondern auf Dinge, die es im Port noch nicht gibt:

| fehlt | wartet auf |
|---|---|
| `soyuz`, `space` | die Sojus-Rakete |
| `bossMeltdown`, `bossMaskman`, `bossWorm`, `bossUFO` | vier der fünf Bosse |
| `fiend`, `fiend2` | die Schimmerwaffen |
| `someWounds` | das Injektormesser |
| `radium` | den Radiumkaffee |
| `sulfuric` | einen Säure-Flüssigkeitsblock |
| `digammaKauaiMoho` | den Speer |
| `digammaUpOnTop` | seinen Vorgänger `digammaKauaiMoho` |
| `potato` | die Kartoffelbatterie |
| `SILEX` | `machine_silex` |

## Runde 259 — Der Aktenschrank, und was an ihm hing

`tools/structure-gap.py` hatte seit Runde 256 noch zwei echte Lücken: `filing_cabinet` und
`wand_logic`. Diese Runde schließt die erste — und der Grund, sie zu schließen, ist messbar.

**Nachgemessen, Datei für Datei:** von den 79 `.nbt`-Dateien des Originals setzte der Umsetzer
**69** um. Die zehn übrigen scheiterten an genau drei Ursachen:

| Ursache | Dateien |
|---|---|
| `wand_loot` zeigt auf `filing_cabinet` | aircraft_carrier, laboratory, oil_rig, radio_house |
| `wand_logic` fehlt | crane, crane_mod, factory, tower_base |
| unauflösbare Zahlen-ID / `wand_tandem` | test-rot, test-tandem-core (beides Testdateien) |

Der Aktenschrank stand damit vier Bauwerken im Weg, die sonst vollständig umsetzbar sind. Nach
dieser Runde sind es **73 von 79**.

### Was er ist

Im Original ein `BlockDecoContainer` mit `TileEntityFileCabinet` — und diese Blockentität stammt
von `TileEntityCrateBase` ab. Er ist also eine Kiste, mit acht Fächern, Schloss und Spinnen.
Im Port erbt seine Blockentität deshalb von `CrateBaseBlockEntity`, und das Schloss kommt mit.

**Zwei Schubladen, nacheinander.** Die untere fährt sofort heraus, die obere erst zehn Ticks
später — das ist der ganze Reiz des Stücks. Die Geräusche kommen aus dem Herausfahren, nicht aus
dem Öffnen des Fensters; darum zählen `startOpen` und `stopOpen` hier nur die Benutzer, statt
wie an jeder anderen Kiste einen Ton zu spielen.

**Die Fachreihen stehen 36 Pixel auseinander**, nicht 18: jede Reihe ist eine Schublade, und im
Fenster steht dazwischen die Front. Dafür hat `CrateBaseBlockEntity` jetzt `getRowPitch()` und
`MenuBase.addSlots` eine Überladung mit getrennten Abständen. Alle anderen Kisten lassen beides
bei 18 — die Vorgabe ändert nichts an ihnen.

**Zwei Sorten, eine Blockentität.** Grün und stahlgrau sind im Original zwei Metadatenwerte
desselben Blocks; im Port sind es zwei Blockanmeldungen, aber dieselbe Blockentitätsart. Welche
Textur gilt, entscheidet darum der Block unter der Entität, nicht der Darsteller — die Schleife
in `ClientProxy` legt je Blockentitätsart genau einen Darsteller an, und zwei Texturen in einem
Darsteller gingen nur so.

Nur die Stahlsorte hat ein Rezept; im Original ist das genauso (`CraftingManager`, Zeile 933).
Die grüne steht ausschließlich in den Bauwerken.

### Die Drehung ist ausgerechnet, nicht geraten

`RenderFileCabinet` dreht im Original nach `getBlockMetadata() >> 2`: 0→180°, 1→0°, 2→270°,
3→90°. Was die vier Werte bedeuten, sagt `BlockDecoModel.onBlockPlacedBy` in seinen eigenen
Kommentaren: 0=Nord, 1=Süd, 2=West, 3=Ost. Daraus wird `NORD 180, OST 90, SUED 0, WEST 270` —
und genau so steht der Eintrag jetzt in `tools/facing-list.txt`, wo das Blickrichtungs-Tor ihn
gegen den Port hält.

### Ein Fund des Beutetors

`loot-check.sh` wurde rot: beide neuen Blöcke hatten `requiresCorrectToolForDrops()`, aber
keinen `mineable`-Tag — sie wären mit keinem Werkzeug gefallen, und ihre Beutetabelle wäre tote
Ladung gewesen. Nachgetragen, Tor wieder grün.

### Ein Fund im Tor selbst

`inventory-check.sh` wurde ebenfalls rot -- und der Fehler lag nicht am neuen Darsteller,
sondern in der Vergleichsliste. Dort stand `filingcabinet 0,0,0 1`, also der Standardwert, den
die Liste einträgt, wenn das Original **keinen** Aufruf hat. Das Original hat aber einen:
`glTranslated(-1D, 0.5D, -1D)`, `glRotatef(180F)`, `glScalef(4F, 4F, 4F)`.

**Warum die Liste ihn übersah, ist gemessen:** `RenderFileCabinet` ist der **einzige** der 225
Darsteller des Originals, dessen `renderInventory` `glScalef` benutzt statt der sonst üblichen
Form `double scale = X; glScaled(scale, scale, scale)`. Der Erzeuger der Liste kennt nur die
zweite Form und fiel auf den Standardwert zurück. Kein anderer Eintrag der Liste trägt diesen
Standardwert -- der Fehler ist dieser eine, und er lag still, solange der Aktenschrank nicht
portiert war: das Tor vergleicht nur Paare, und ohne Gegenstück im Port gibt es kein Paar.

Eintrag berichtigt, Darsteller auf die Zahlen des Originals gesetzt, Tor grün.

### Was offen bleibt

`wand_logic` — der Einstieg in ein rund tausendzeiliges Fallensystem, das vier Bauwerke
benutzen. Das ist kein Block, den man nachreicht, sondern ein Teilsystem; es bekommt eine eigene
Runde.

## Runde 260 — Die ersten sechs Einzelbauwerke stehen in der Welt

Das Meteoritenverlies war seit Runde 251 das einzige Bauwerk, das der Port tatsächlich in die
Welt setzt. Die übrigen ließen sich zwar umsetzen, lagen aber nur als Datei im Jar. Diese Runde
bringt die erste Gruppe: die sechs, die das Original an **eine einzige Bedingung** hängt,
`BiomeDictionary.isBiomeOfType(biome, Type.SANDY)` — Vertibird, sein Wrack, die drei
Wüstenhütten und die tote Satellitenschüssel.

### Die Höhe ist ein Versatz, keine Höhe

Das Original gibt jedem `JigsawPiece` einen `heightOffset` — drittes Argument seines Erbauers —
und setzt das Stück so viele Blöcke **unter** die Geländeoberkante: der Vertibird drei, sein
Wrack zehn, die Hütten fünf bis sieben. In 1.21 heißt dasselbe: `projectStartToHeightmap` setzen
und die Starthöhe als Versatz lesen. Ohne das Heightmap-Argument wäre die Zahl eine **absolute**
Höhe, und die Hütten stünden auf Y = −7 im Grundgestein. Deshalb braucht es hier den
sechsstelligen Erbauer von `JigsawStructure`, nicht den fünfstelligen des Verlieses.

### Der Rasterabstand ist gerechnet, nicht gewählt

Nach derselben Formel wie beim Verlies: Abstand = 12 · √(Gesamtgewicht / eigenem Gewicht),
Zwischenraum ein Drittel davon.

Das Gesamtgewicht der Wüste ist **ausgezählt, nicht geschätzt**: dort ziehen Spire (2), Features
(50), Bunker (6), Vertibird (6), Wrack (10), Waldchemie (30), Waldposten (30), Fabrik (40), Kran
(20), die zwei Flugzeugwracks (je 25), die drei Hütten (18, 20, 22), die tote Schüssel (15) und
das Verlies (1) — zusammen **320**. Nicht dabei sind die Ruinen (sie verlangen
`canSpawnLightningBolt()`, und in der Wüste regnet es nicht), alles mit `isFlatBiome` (die Wüste
ist nicht SPARSE), der Turmsockel (er schließt SANDY ausdrücklich aus) und alles, was Ozean,
Strand oder Ebene verlangt.

| Bauwerk | Gewicht | Abstand | Zwischenraum |
|---|---|---|---|
| vertibird | 6 | 88 | 29 |
| crashed_vertibird | 10 | 68 | 23 |
| dead_dish_small | 15 | 55 | 18 |
| desert_shack_1 | 18 | 51 | 17 |
| desert_shack_2 | 20 | 48 | 16 |
| desert_shack_3 | 22 | 46 | 15 |

Jedes bekommt einen eigenen Streuwert. Das ist kein Schmuck: zwei Bauwerke mit demselben
Streuwert **und** demselben Abstand landeten in jeder Rasterzelle auf demselben Feld und stünden
ineinander.

### Was an dieser Runde nicht gemessen ist -- und warum nicht

Die Biomliste. Welche Biome Forge in 1.7.10 mit `SANDY` versieht, steht in Forges eigener
`registerVanillaBiomes` — nicht im Quelltext des Originals und nicht in diesem Verzeichnisbaum.
Nachlesbar ist nur, was der Typ bedeutet: Boden aus Sand oder Sandstein.

Die Liste ist deshalb **bewusst eng**: Wüste und Mesa-Familie, wo der Sandboden außer Frage
steht. Strände und Savannen stehen nicht drin — für den Strand hat das Original einen eigenen
Typ (`BEACH`, den der Leuchtturm getrennt abfragt), und die Savanne hat Grasboden. Eine zu enge
Liste lässt ein Bauwerk seltener stehen; eine zu weite setzt es an Orte, an denen es im Original
nie stand. Im Zweifel ist das erste der kleinere Schaden, und es steht so im Quelltext
vermerkt.

### Ein Loch im Bauwerkstor, sofort gestopft

`structure-check.sh` liest die benutzten Vorlagenpfade als `add("...")` aus `NtmTemplatePools`.
Die sechs neuen Pools entstehen über eine Hilfe `einzeln(...)`, und die sah das Tor nicht: es
meldete alle sechs Dateien als „ohne Benutzer", obwohl sie richtig im Pool lagen. Die Hilfsform
steht jetzt mit in der Erkennung.

**Stand: 7 von 34 Bauwerken in der Welt** (das Verlies und diese sechs), 73 von 79 Rohdateien
umsetzbar.

## Runde 261 — Die zehn Ruinen, und ein Streuwert, der zehnmal derselbe gewesen wäre

Die zweite Gruppe: `NTMRuinsA` bis `NTMRuinsJ`. Sie hängen alle an derselben Bedingung —
`!isWaterBiome(biome) && biome.canSpawnLightningBolt()` — und tragen alle `conformToTerrain`.

### Gelände folgen heißt Schwerkraftprozessor

Alle zehn haben im Original `conformToTerrain = true` **und** `heightOffset = -1`. Das ist
dieselbe Paarung wie bei der Meteorspitze, nur mit einem Block statt dreien: jede einzelne
Säule wird auf die Geländehöhe gesetzt und dann abgesenkt. In 1.21 ist das ein
`GravityProcessor(WORLD_SURFACE_WG, -1)` im Pool.

**Der Versatz gehört dabei zum Geländefolgen und wird nicht noch einmal auf das ganze Bauwerk
gerechnet.** Die Struktur startet deshalb mit Versatz null — zweimal gerechnet stünde jede
Ruine einen Block zu tief.

### Ein Fund an der eigenen Arbeit

Runde 260 bildete den Streuwert als `996996996 + Abstand`. Das ging gut, solange nur die sechs
der Wüste dastanden: ihre Abstände sind alle verschieden. Bei den Ruinen fällt dieselbe Formel
in genau die Falle, vor der der Kommentar daneben warnt — **neun der zehn haben den Abstand
71**, bekämen also denselben Streuwert und stünden in jeder Rasterzelle auf demselben Feld,
also ineinander.

Der Streuwert wird jetzt durchgezählt. Gemessen: alle sechzehn Werte verschieden.

### Zwei Zahlen, die sich widersprechen — und was damit geschieht

Das Gesamtgewicht der Ebene ist bei den Ruinen der Nenner. Runde 251 hat dafür **422**
ausgezählt; eine zweite Auszählung in dieser Runde kam auf **491**. Der Unterschied liegt an
zwei Angaben, die sich in diesem Verzeichnisbaum nicht nachschlagen lassen:

- ob Forge der Ebene den Typ `SPARSE` gibt — davon hängen Labor, Funkhaus und Sendeturm ab,
  zusammen 75;
- wie das Leergewicht (`plainsNullWeight`, 4) mitzählt.

Hier steht die ältere Zahl, damit Ruinen und Verlies auf derselben Skala liegen. Das ist eine
Entscheidung, keine Messung, und sie steht so im Quelltext: wer die beiden Angaben einmal
misst, rechnet **beide** Orte um, nicht nur einen.

Die Biomliste ist aus `canSpawnLightningBolt()` hergeleitet — Vanilla 1.7.10: „es regnet, und
es schneit nicht". Damit fallen Wüste und Mesa (kein Regen), alles Verschneite und, über
`isWaterBiome`, Ozeane und Flüsse weg. Auch diese Liste ist eine Anwendung der Regel, keine
Messung an Vanillas Tabelle; sie steht mit derselben Warnung im Quelltext wie die SANDY-Liste
aus Runde 260.

### Noch ein Loch im Bauwerkstor

Die Ruinenpools gehen durch eine zweite Fassung der Hilfe — die mit Prozessoren —, und das
Muster aus Runde 260 verlangte eine schließende Klammer direkt hinter dem Pfad. Alle zehn
Dateien wurden wieder als „ohne Benutzer" gemeldet. Das Muster endet jetzt am Pfad.

**Stand: 17 von 34 Bauwerken in der Welt.**

## Runde 262 — Sechs auf flachem Land

Die dritte Gruppe: Spire, Waldchemie, Waldposten, die beiden Flugzeugwracks und die
Wasserpumpe. Sie haben alle **keine Höhenschranke** — deshalb gehen sie mit demselben
Verfahren wie die Wüstengruppe, nur mit anderen Biomlisten.

### Drei Bedingungen statt eines Typs

Das Original unterscheidet fünf der sechs nicht über einen `BiomeDictionary`-Typ, sondern über
die **Geländerauheit** `biome.heightVariation`:

| Bedingung | wer |
|---|---|
| `≤ 0.05 && !isWaterBiome` | Spire |
| `≤ 0.3 && !isWaterBiome` | Waldchemie, Waldposten, beide Flugzeugwracks |
| `Type.PLAINS \|\| Type.SWAMP` | Wasserpumpe |

Die Schwelle 0,3 lässt in 1.7.10 fast jedes Oberflächenbiom durch; hart ausgeschlossen sind
die Extreme Hills und ihre Abkömmlinge (0,5). Die Liste im Quelltext ist entsprechend weit.

Auch diese drei Listen sind **hergeleitet, nicht nachgemessen** — welche `heightVariation` ein
Biom von 1.7.10 trägt, steht in Vanilla 1.7.10 und nicht in diesem Verzeichnisbaum. Sie tragen
denselben Vermerk wie die SANDY-Liste aus Runde 260.

### Der Spire ist das seltenste Bauwerk des Originals

Gewicht 2 gegen ein Gesamtgewicht von 422 — Abstand **174 Chunks**, also rund einer auf 2800
mal 2800 Blöcke. Das Verlies (Gewicht 1) ist noch seltener.

### Ein Fund beim Einbau

Die beiden Flugzeugwracks bekommen denselben Abstand (49) und die Waldstücke ebenfalls (45).
Seit Runde 261 wird der Streuwert durchgezählt statt aus dem Abstand gebildet — sonst wäre hier
genau derselbe Fehler noch einmal entstanden, diesmal doppelt. Gemessen: alle 22 Streuwerte
verschieden.

**Stand: 23 von 34 Bauwerken in der Welt.** Offen bleiben die vier mit Höhenschranke
(Strandpatrouille, Flugzeugträger, Ölplattform, Leuchtturm — 1.21 kennt dafür keinen fertigen
Bauwerkstyp), die Schüssel (auch Höhenschranke), die drei mit `isFlatBiome` (Labor, Funkhaus,
Sendeturm), die vier, die `wand_logic` brauchen, und die beiden Nicht-NBT-Bauwerke (Features,
Bunker), die im Original gar keine Vorlagendatei haben.

## Runde 263 — Funkhaus und Sendeturm, und eine Zahl, die dreimal anders hieß

Die vierte Gruppe wäre `isFlatBiome` — `heightVariation ≤ 0.2 && !isWaterBiome && Type.SPARSE`
—, und sie hat drei Mitglieder. Zwei davon stehen jetzt: Funkhaus und Sendeturm. Das Labor
fehlt weiter, denn es trägt zusätzlich eine **Höhenschranke** (53 bis 65), und dafür hat 1.21
keinen fertigen Bauwerkstyp.

`SPARSE` heißt bei Forge „schütter bewachsen" — offenes Land ohne geschlossenen Wald. Zusammen
mit der Rauheitsschranke bleibt übrig: Ebene, Savanne, Wiese und die lichten Formen. Wald,
Dschungel und Taiga sind draußen, Gebirge ohnehin.

### Die Zahl, die dreimal anders hieß

Beim Schreiben des Kommentars zu `SPARSE` fiel auf, dass die Bemerkung eine **dritte**
Gesamtzahl für die Ebene einführte (497) neben den schon vorhandenen 422 und 491. Nachgerechnet
geht keine der Verbindungen auf: 422 + 75 (Labor, Funkhaus, Sendeturm) + 4 (Leergewicht) wären
501, nicht 491.

Damit ist die Erklärung aus Runde 261 — „der Unterschied liegt an zwei Angaben" — **zu
einfach**: die ältere Auszählung zählt noch anderes anders. Beide Notizen stehen jetzt so da:
die Zahlen sind 422 und 491, sie lassen sich nicht sauber gegeneinander aufrechnen, und welche
stimmt, entscheidet eine Tabelle, die nicht in diesem Verzeichnisbaum liegt.

Es bleibt bei 422 für alle Einträge — **eine gemeinsame Skala, die um ein Sechstel daneben
liegt, ist harmloser als zwei richtige Skalen nebeneinander.** Wer Forges Tabelle einmal misst,
rechnet alle Einträge um, nicht einzelne.

**Stand: 25 von 34 Bauwerken in der Welt.**

## Runde 264 — Die Höhenschranke war nie eine Schranke

Sechs Bauwerke trugen `minHeight`/`maxHeight`, und die letzten beiden Runden haben sie
deswegen zurückgestellt: „1.21 kennt dafür keinen fertigen Bauwerkstyp." **Das war ein
Lesefehler.**

`NBTStructure.java:921`:

```java
int y = isFlatWorld ? averageHeight : MathHelper.clamp_int(averageHeight, minHeight, maxHeight);
```

`minHeight` und `maxHeight` **verwerfen den Ort nicht, sie klemmen die Höhe.** Das Bauwerk wird
immer gesetzt, nur nie höher als `maxHeight` und nie tiefer als `minHeight`. Wer das als Filter
liest, sucht in 1.21 nach einem Bauwerkstyp, den es nicht gibt; wer es als Klemme liest, muss
nur noch entscheiden, ob die Klemme am gegebenen Ort greift.

| | Fenster | Versatz | greift die Klemme? |
|---|---|---|---|
| Ölplattform | 11–12 | −20 | **immer** — Meeresboden minus 20 liegt weit darunter |
| Leuchtturm | 28–29 | −40 | **immer** — dasselbe, nur tiefer |
| Strandpatrouille | 58–67 | −5 | **meistens** — Strand liegt um 63, macht 58 |
| Flugzeugträger | ≤ 42 | −6 | selten |
| Schüssel, Labor | 53–65 | −10 | selten |

Die ersten drei bekommen deshalb eine **feste Höhe ohne Höhenkarte** — genau die Form, die das
Meteoritenverlies schon hat, wo `minHeight` und `maxHeight` beide 32 sind. Die letzten drei
folgen dem Gelände wie alle anderen.

**Das ist die einzige bewusste Abweichung dieser Gruppe:** an ungewöhnlich hohem oder tiefem
Gelände steht Träger, Schüssel oder Labor dort, wo das Original sie festgehalten hätte. Das
steht so im Quelltext.

### Der Ozean hat einen eigenen Nenner

Die drei Ozeanbauwerke rechnen nicht gegen die 422 der Ebene, sondern gegen **27**: im Wasser
ziehen nur Träger (3), Ölplattform (5), Leuchtturm (4) und das Leergewicht des Ozeans (15).
Alles andere verlangt `!isWaterBiome`. Deshalb stehen sie **enger** als die Landbauwerke,
obwohl ihre Gewichte viel kleiner sind — sie teilen sich den Ozean fast allein.

**Stand: 31 von 34 Bauwerken in der Welt.** Offen bleiben genau **drei**: Kran, Fabrik und
Turmsockel — alle drei warten auf `wand_logic`.

*Beim Nachzählen aufgefallen:* der Kran benutzt nicht `crane.nbt`, sondern `crane_mod.nbt`
(`StructureManager.java:90` — der Feldname ist `crane`, die Datei heißt anders). `crane.nbt`
ist im Original eine Leiche: keine Zeile verweist darauf. Das sind also drei Bauwerke aus vier
Dateien, nicht vier Bauwerke.

Dazu kommen die beiden, die im Original gar keine Vorlagendatei haben und deshalb nicht in
diesen 34 stecken: Features und Bunker sind dort handgeschriebene Bauwerkskomponenten
(`MapGenNTMFeatures`, `BunkerStart`) und gehören zu einer eigenen Aufgabe.

## Runde 265 — Der Logikstab: die Falle als Block

Der letzte der vier Zauberstäbe. Er ist die **Falle** in einem Bauwerk: in ihm stehen zwei
Namen — eine Bedingung und eine Aktion —, und er führt sie aus, solange er steht.

### Zwei Zähler sind der ganze Zustand

`phase` zählt hoch, sooft die Bedingung zutrifft; `timer` zählt die Ticks seit dem letzten
Zutreffen. Jede Aktion liest beide und entscheidet daran, was sie tut — deshalb brauchen
Aktion und Bedingung kein eigenes Gedächtnis.

**Erst die Aktion, dann die Bedingung.** Das Original macht es so, und es ist sichtbar: eine
Aktion sieht in Phase 0 die Welt, *bevor* die Bedingung sie auf 1 stellt.

### Ein unbekannter Name löscht den Block — und das ist die Vorlage

`LogicBlock.java:113`: findet das Original Aktion oder Bedingung nicht in seiner Tabelle, setzt
es an die Stelle des Stabes Luft. Das passiert dort auch im Spiel — der Turmsockel nennt
`DEAD_GUY_BASE_TOWER`, und diese Aktion ist im Original **nirgends angemeldet**: die einzige
`put()`-Zeile dafür ist auskommentiert **und** anders geschrieben (`DEAD_GUY_TOWER_BASE`,
`LogicBlockActions.java:537`).

Das macht einen Teil-Port sauber: ein Stab, dessen Aktion der Port noch nicht kennt, verhält
sich genau wie im Original — er verschwindet. Kein stilles Loch, sondern dasselbe Verhalten.

### Drei von neun Aktionen, und warum genau diese

Nachgezählt über die drei Bauwerke mit Logikstäben nennen sie zehn Aktionen, von denen das
Original neun auflösen kann. Diese Runde bringt die drei, die **ohne neue Hilfsklassen**
auskommen:

| Aktion | was sie tut |
|---|---|
| `COLLAPSE_ROOF_RAD_5` | die Decke fällt herunter |
| `POWER_LOCK` | der Tresor nebenan schließt sich zu |
| `DEAD_GUY_CRANE` | aus dem Stab wird ein Skeletthalter mit einer Waffe |

Die übrigen sechs warten auf Teile, die der Port noch nicht hat: `MobUtil` samt
Ausrüstungspools (fünf Mob-Aktionen), das KI-Ziel `EntityAIFireGun` (zwei davon zusätzlich) und
die C4-Ladung mit Zeitzünder (`BOMB_CRANE`).

**Die Kugel ist keine Kugel.** `COLLAPSE_ROOF_RAD_5` läuft von −4 bis unter +4 und vergleicht
das Abstandsquadrat mit `r*r/2`, also 8 statt 16 — die Reichweite ist damit rund 2,8 statt 4,
und die Schleife ist um einen halben Block versetzt. Beides übernommen: das Ergebnis ist genau
die Deckenform, die man im Spiel sieht.

**Fallende Blöcke brauchen nichts Neues.** Das Original hat dafür eine eigene Entität, weil
Vanilla 1.7.10 nur Sand und Kies fallen lässt. In 1.21 nimmt `FallingBlockEntity.fall` jeden
Zustand mit.

### Der Umsetzer ist damit fertig

**77 von 79 Rohdateien** — vorher 73. Die beiden übrigen sind Testdateien mit absichtlich
unauflösbarem Inhalt: `test-rot` nennt eine Blocknummer, die nur in der Welt ihres Urhebers
galt, und `test-tandem-core` den Tandemstab, den sonst keine Datei benutzt.

**Jede Datei, die das Original tatsächlich benutzt, lässt sich jetzt umsetzen.**

`unlock()` an `LockableBaseBlockEntity` ist neu — das Gegenstück zu `lock()`, im Original
genauso benannt und an genau einer Stelle gebraucht: die Wechselwirkung `POWER_LOCK` sperrt den
Tresor wieder auf, wenn nebenan mehr als 500 kHE stehen.

## Runde 266 — 34 von 34

Kran, Fabrik und Turmsockel — die letzten drei. Sie waren nicht wegen ihrer Bedingungen offen
(die sind gewöhnlich), sondern weil ihnen der Logikstab fehlte. Mit Runde 265 gibt es ihn.

| Bauwerk | Bedingung | Gewicht | Abstand |
|---|---|---|---|
| Kran | Rauheit ≤ 0,2, kein Wasser | 20 | 55 |
| Fabrik | Rauheit ≤ 0,2, kein Wasser | 40 | 39 |
| Turmsockel | Rauheit ≤ 0,3, kein Wasser, **kein Sandboden** | 30 | 45 |

Der Turmsockel ist das **einzige** Bauwerk des Originals, das einen Biomtyp *ausschließt*
statt ihn zu verlangen: `!BiomeDictionary.isBiomeOfType(biome, Type.SANDY)`. Er steht nicht in
der Wüste.

Zwei neue Biomlisten: `mittelflach` (Rauheit ≤ 0,2) ist die von `flach` ohne die Pilzinsel —
der Unterschied zwischen 0,2 und 0,3 ist in 1.7.10 genau diese eine —, und `flachOhneSand` ist
`flach` ohne alles, was in `sandbiome` steht, plus ohne die Strände.

Der Kran benutzt `crane_mod.nbt`, nicht `crane.nbt` (siehe Runde 264); im Port heißt die Datei
trotzdem `crane.nbt`, weil der Name des Bauwerks zählt und nicht der der Vorlage im Original.

**Damit stehen alle 34 NBT-Bauwerke des Originals in der Welt.** Offen bleiben aus dieser
Gruppe nur noch die beiden, die im Original gar keine Vorlagendatei haben — Features und Bunker
sind dort handgeschriebene Bauwerkskomponenten (`MapGenNTMFeatures`, `BunkerStart`).

## Runde 267 — Die Ausrüstungslisten, und eine Liste, die es nie gab

Drei der sechs offenen Logikstab-Aktionen brauchten nur eines: `MobUtil`, die gewichteten
Listen, aus denen ein Zombie seinen Helm und ein Skelett seine Waffe zieht.

**Vier Listen, nicht acht.** Das Original führt acht Pools; vier gehören zu den Ruß-Mobs und
dem Gob-Block, die der Port nicht hat. Übernommen sind nur die, die die Aktionen benutzen:
gewöhnlich, fortgeschritten, Fernkampf (ohne Hand — die füllt die Waffenliste) und drei
Waffenlisten.

### Eine Liste, die im Original nie gefüllt wird

`SKELETON_GUN_TIER_1` ruft `assignItemsToEntity(mob, MobUtil.slotPoolMasks, ...)`. Nachgemessen
über den ganzen Quelltext hat `slotPoolMasks` **genau zwei Vorkommen**: die Erklärung und
diesen Aufruf. Keine einzige `put()`-Zeile füllt sie. Der Aufruf läuft also über eine leere
Abbildung und tut nichts — im Port steht er darum gar nicht erst.

Das ist nach `DEAD_GUY_BASE_TOWER` (Runde 265) der zweite Fund dieser Art im selben
Teilsystem: etwas, das dasteht und nie wirkt.

### Neun Einträge entfallen, und warum kein Ersatz

Von 78 Gegenständen der übernommenen Listen gibt es 69 im Port. Die neun fehlenden sind die
drei Schilder (`chernobylsign`, `sopsign`, `stopsign`), die beiden Jacken (`jackt`, `jackt2`),
`mask_of_infamy`, `reer_graar` und die beiden Schraubenschlüssel (`wrench`,
`wrench_flipped`).

Ihre Gewichte fallen **ersatzlos** weg. Damit bleiben die Verhältnisse der übrigen Einträge
zueinander die des Originals, nur die Summe ist kleiner. Ein Ersatzgegenstand wäre geraten; ein
Weglassen ist nachlesbar.

**Ein Name geht auseinander:** was im Original `ModItems.hat` heißt, ist dort als `nossy_hat`
angemeldet (`ModItemsArmor.java:33` — das Feld heißt anders als der Name). Gefunden hat das
nicht das Auge, sondern `registry-check`, das `NtmItems.HAT` als unbekanntes Feld meldete.

### Drei Fallen mehr

`ZOMBIE_TIER_1`, `ZOMBIE_TIER_2` und `SKELETON_GUN_TIER_1` sind damit live — sechs der neun
auflösbaren Aktionen. Alle drei setzen **drei Mobs auf denselben Punkt** und löschen danach den
Stab; dass die drei übereinander stehen, löst Vanilla selbst auf, sobald sie ticken. So steht
es im Original.

Offen bleiben `SKELETON_GUN_TIER_2/3` (sie brauchen das KI-Ziel `EntityAIFireGun` — ohne es
stünde ein Skelett mit einer Waffe da, die es nie abfeuert, und das wäre schlimmer als keines)
und `BOMB_CRANE` (die C4-Ladung mit Zeitzünder).

## Runde 268 — Das Schuss-Ziel, und eine Bedingung, die immer wahr ist

`SKELETON_GUN_TIER_2` und `SKELETON_GUN_TIER_3` waren die letzten beiden auflösbaren Aktionen
des Logikstabs. Beide hängen am KI-Ziel `EntityAIFireGun`: die Waffen des Mods werden über
Tastendrücke bedient, nicht über den Bogen-Angriff der Vanilla-KI, und ohne ein Ziel, das
diese Tasten drückt, stünde dort ein Skelett mit einer Waffe, die es nie abfeuert.

Portiert als `com.hbm.entity.ai.FireGunGoal`. Die Waffen-API des Ports gibt jeden Aufruf des
Originals her — `inventoryTick` statt `onUpdate`, sonst dieselben Namen —, und
`MagazineBelt.getAmount` behandelt einen `null`-Behälter bereits, weil das Original dort
denselben Sonderfall für genau diese KI hat.

### Der Schalter ohne `break`

`updateState` sieht im Original so aus (`EntityAIFireGun.java:98-110`):

```java
switch(state) {
case FIRING: updateKeybind(gun, stack, EnumKeybind.GUN_PRIMARY);
case RELOADING: updateKeybind(gun, stack, EnumKeybind.RELOAD);
default: clearKeybinds(gun, stack); break;
}
```

Kein Zweig hat ein `break`, jeder endet also in `clearKeybinds`. Druck und Loslassen derselben
Taste landen damit in **einem** Tick.

Wirkungslos ist das nicht — das Press-Lambda einer Waffe feuert unmittelbar
(`Lego.LAMBDA_STANDARD_FIRE` ruft `doStandardFire` direkt auf) —, es hält die Taste nur nicht.
Dauerfeuer kommt deshalb nicht aus dem gehaltenen Druck, sondern daraus, dass `tick()` während
`FIRING` jeden Tick erneut drückt. Ein nachgetragenes `break` würde das Verhalten ändern;
deshalb steht im Port derselbe Schalter, mit einem Kommentar, warum er so bleibt.

### Die Bedingung, die immer wahr ist

Der eigentliche Fund der Runde steht nicht im KI-Ziel, sondern in `MobUtil.assignItemsToEntity`
(`MobUtil.java:253-256`):

```java
//Give skeleton AI if it has a gun
if (slot == 0 && entity instanceof EntitySkeleton && pool == slotPools.get(0)) {
	addFireTask((EntityLiving) entity);
}
```

`slotPools` ist der **Parameter** der Methode, nicht ein statisches Feld, und `pool` ist
`entry.getValue()` der laufenden Schleife. Bei `slot == 0` sind beide dasselbe Objekt — der
dritte Teil der Bedingung ist immer wahr und prüft nichts.

Damit bekommt **jedes** Skelett, dem eine der Listen etwas in die Hand legt, das Schuss-Ziel,
nicht nur die beiden oberen Stufen. `SKELETON_GUN_TIER_1` schoss im Original also die ganze
Zeit — nur mit den weiten Standardwerten (20 Blöcke Reichweite, 30 Grad Streuung) statt mit den
scharf eingestellten der Stufen 2 und 3. Der Port hatte das bis hierher nicht; jetzt hat er es.

Der Kommentar daneben stimmt außerdem nicht: er sagt "if it has a gun", die Bedingung fragt
aber nur nach dem Platz, nicht nach dem Gegenstand. Ein Skelett mit einer Schaufel bekommt das
Ziel ebenfalls. Schaden tut das nichts, denn `canUse()` verlangt eine Waffe und lässt das Ziel
sonst schlafen — übernommen, wie es dasteht.

### Warum die Doppelprüfung bleibt

`MobUtil.schussZiel` hängt höchstens ein Ziel an. Im Original steht über der entsprechenden
Methode der Kommentar, die Ziele würden sich sonst übereinanderstapeln — und genau das ist der
Grund: die Stufen 2 und 3 hängen ihr eigenes Ziel **vor** dem Ausrüsten an, das Ausrüsten würde
danach ein zweites, schwächeres nachlegen. Die Prüfung hat also auch im Port eine Ursache; sie
steht nicht vorsorglich da.

### Stand

Acht der neun auflösbaren Aktionen sind live. Offen bleibt `BOMB_CRANE` (die C4-Ladung mit
Zeitzünder — `BlockChargeC4` plus `TileEntityCharge`; der Port hat mit `ExplosiveChargeBlock`
den Block, aber keine Block-Entität mit Zeitzünder). Die zehnte, `DEAD_GUY_BASE_TOWER`, kommt
nie — ihre Anmeldezeile ist im Original auskommentiert und zudem anders geschrieben.

Das ist nach `DEAD_GUY_BASE_TOWER` (Runde 265) und `slotPoolMasks` (Runde 267) der dritte Fund
dieser Art im selben Teilsystem — mit dem Unterschied, dass dieser hier nicht nichts tut,
sondern mehr, als der Kommentar daneben behauptet.

## Runde 269 — Die Haftladungen, und eine Stange, die nie angemeldet war

`BOMB_CRANE` war die letzte offene Aktion des Logikstabs. Sie setzt eine C4-Haftladung über
sich und stellt ihr eine Uhr — und die ganze Familie dieser Ladungen fehlte dem Port.

### Was gefehlt hat

Vier Blöcke (`BlockChargeBase` mit `BlockChargeC4`, `BlockChargeDynamite`, `BlockChargeMiner`,
`BlockChargeSemtex`) und ihre Blockentität `TileEntityCharge`. Sie sind etwas anderes als die
schon portierten `det_*`-Blöcke: die kleben an der Fläche, auf die man sie setzt, tragen eine
Schaltuhr und lassen sich **nur mit dem Entschärfer** wieder abnehmen — wer sie zerschlägt,
zündet sie.

Portiert als `ChargeBaseBlock` samt den vier Ableitungen und `ChargeBlockEntity`.

Im Original malt ein Blockdarsteller (ISBRH) den Körper und ein Blockentitäten-Darsteller
(TESR) die Restzeit darauf. 1.21 kennt kein ISBRH mehr; der Block ist hier unsichtbar und
`RenderExplosiveCharge` macht beides. Zwei Formen reichen für vier Ladungen — im Original steht
das als `getRenderType()`, der die `renderID` der jeweils anderen Klasse zurückgibt.

### Die Stange, die es nie gab

Beim Nachbauen der Rezepte fiel auf: `stick_c4` ist im Port **nicht angemeldet**. Keine Zutat
fehlte — `ingot_c4`, `det_cord`, Papier, `safety_fuse` sind alle da —, nur die Stange selbst
war übersprungen worden. Damit fehlten still zwei Rezepte: das der Stange und das des
**C-4-Blocks**, der seit seiner Portierung unbaubar im Spiel stand.

Nachgereicht samt Textur, Gefahreneintrag (`EXPLOSIVE, 2.5F`, wie im Original), Kreativ-Reiter
und beiden Rezepten. Dazu die vier Rezepte der Ladungen: drei Stangen plus Klebeband, und die
Bergbauladung als Dynamitladung mit vier Feuersteinen darum.

### Nachgemessen: neun Aktionen, nicht zehn

Beim Anschluss von `BOMB_CRANE` habe ich die Zahlen im Kommentar von `LogicActions`
nachgerechnet, indem ich die drei Bauwerksdateien mit dem Lader des Umsetzers eingelesen und
die Felder `actionID`/`conditionID`/`interactionID` **aller 31 Stäbe** gezogen habe. Ergebnis:

| | genannt | auflösbar | jetzt live |
|---|---|---|---|
| Aktionen | 9 | 8 | 8 |
| Bedingungen | 5 | 5 | 5 |
| Wechselwirkungen | 1 | 1 | 1 |

Der alte Kommentar sagte „zehn Aktionen, neun auflösbar". Beide Zahlen waren um eins zu hoch.

Zwei Dinge fallen dabei auf:

**`SKELETON_GUN_TIER_3` kommt in keinem Bauwerk vor.** Runde 268 hat sie eingeschaltet, ohne
dass ein Stab sie nennt. Angemeldet ist sie im Original trotzdem, und darum bleibt sie auch
hier — wer einen Stab von Hand setzt, soll sie benutzen können.

**`BOMB_CRANE` ist zugleich eine Bedingung.** Derselbe Name steht im Original in beiden
Tabellen, und der Kran benutzt beide an ein und demselben Stab: `actionID` und `conditionID`
stehen dort beide auf `BOMB_CRANE`. Dem Port fehlte die Bedingung — `LogicConditions` kannte
nur vier Namen, dieser Stab hätte sich also gelöscht. Jetzt kennt er fünf.

Beide setzen dieselbe Ladung, mit verschiedenen Zeiten: die Aktion 1200 Ticks, die Bedingung
200. Da jeder Tick **erst die Aktion und dann die Bedingung** ruft
(`LogicBlock.java:117-118`), überschreibt die Bedingung die Minute sofort mit zehn Sekunden —
die Minute ist gesetzt und nie zu sehen. Übernommen, wie es dasteht.

### Stand

Der Logikstab ist damit vollständig: acht von acht auflösbaren Aktionen, fünf von fünf
Bedingungen, die eine Wechselwirkung. Die neunte Aktion, `DEAD_GUY_BASE_TOWER`, kommt nie —
ihre Anmeldezeile ist im Original auskommentiert und zudem anders geschrieben.

## Runde 270 — Standortbestimmung: die drei offenen Punkte sind zu

Die Standortbestimmung nach Runde 249 nannte drei offene Punkte: das **Meteoritenverlies**,
die **vier Bauzauberstäbe** und das **Erfolgssystem**. Diese Runde misst alle drei nach,
statt sie für erledigt zu halten.

### Der vierte Bauzauberstab kommt nicht, und das ist messbar

Von den vier Stäben sind drei erledigt: `wand_jigsaw` (wird zum Jigsaw-Block von 1.21),
`wand_loot` (Runde 254) und `wand_logic` (Runden 265 bis 269). Offen blieb `wand_tandem`.

Ich habe alle 79 Rohdateien mit dem Lader des Umsetzers eingelesen und gezählt, welche
Stabsorte wo vorkommt:

| Stab | Vorkommen | Dateien |
|---|---|---|
| `wand_jigsaw` | 72 | 40 Dateien |
| `wand_loot` | 67 | 27 Dateien |
| `wand_logic` | 11 | crane, crane_mod, factory, tower_base |
| `wand_tandem` | 3 | **nur** `test-tandem-core.nbt` |

Und diese eine Datei wird im Original **nur in einer auskommentierten Zeile** angemeldet —
`StructureManager.java:102-103`, daneben `:98` für `test-rot.nbt`. Beide Testdateien kommen
also auch im Original nie in die Welt.

Damit ist der Tandemstab kein Rückstand, sondern ein Werkzeug des Urhebers, das er selbst
abgeschaltet hat. Ihn zu portieren hieße, eine Mechanik zu bauen, die nichts benutzt — und
zwar eine, deren Zweck 1.21 gar nicht mehr hat: der Stab ist ausdrücklich ein Umweg um die
Schwäche des 1.7.10-Systems, Bauwerksteile vor der Geländeerzeugung zusammenzusetzen
(„*NBTStructures have the inherent flaws of the vanilla structure system*", schreibt der
Kommentar dort selbst). Das Jigsaw-System von 1.21 hat diese Schwäche nicht.

**Die Deckung des Umsetzers liest sich damit anders:** nicht 77 von 79, sondern **77 von 77**
Dateien, die das Original tatsächlich benutzt. Der Hinweis steht jetzt auch im Kopf von
`tools/nbt2structure.py`.

### Das Erfolgssystem: nachgemessen, unverändert blockiert

46 von 61. Die 15 übrigen hängen an Inhalt, der dem Port fehlt. Ich habe die sieben
Symbolgegenstände, an denen sie hängen, einzeln gegen `NtmItems` und `NtmBlocks` geprüft:
`battery_potatos`, `shimmer_sledge`, `shimmer_axe`, `injector_knife`, `coffee_radium`,
`achievement_icon` und `machine_silex` — **keiner davon ist angemeldet**. Dazu die Sojus, die
vier Bosse und der Speer.

> **Berichtigt in Runde 271:** Diese Messung war bei einem der sieben falsch. `coffee_radium`
> **gibt es** im Port — als `DrinkType.COFFEE_RADIUM`, ein Meta-Gegenstand ohne eigenen
> Registriereintrag. Die Suche ging nur über `NtmItems` und `NtmBlocks` und konnte ihn dort
> nicht finden. Siehe *Runde 271*.

### Stand

| Punkt | Stand |
|---|---|
| Meteoritenverlies | steht in der Welt (Runden 250–256, 266) |
| Bauzauberstäbe | 3 von 3 erreichbaren; der vierte ist im Original abgeschaltet |
| Erfolgssystem | 46 von 61; die 15 übrigen messbar durch fehlenden Inhalt blockiert (eine Messung davon in Runde 271 berichtigt) |

## Runde 271 — Der Radiumkaffee war die ganze Zeit da

Der Stop-Haken hat die Standortbestimmung aus Runde 270 zu Recht nicht gelten lassen: der
Punkt „Erfolgssystem" war nicht fertig, sondern nur als blockiert erklärt. Beim ersten Griff
nach dem günstigsten Blocker fiel auf, dass diese Erklärung bei einem von sieben **falsch**
war.

### Der Messfehler

Runde 270 suchte die sieben Symbolgegenstände über `NtmItems` und `NtmBlocks` — also über die
**Registriernamen**. `coffee_radium` hat keinen. Er existiert im Port als
`DrinkItem.DrinkType.COFFEE_RADIUM`, ein Eintrag in einem Meta-Gegenstand, und war der Suche
deshalb unsichtbar. Die zweite Suche, über den ganzen Quelltext statt über die
Registrierlisten, hat ihn sofort gefunden.

Die Lehre ist eine alte in diesem Port: **wer nur dort nachsieht, wo er etwas erwartet, misst
die eigene Erwartung.** Die Meta-Gegenstände sind genau die Stellen, an denen die Namen des
Originals keine Registriernamen mehr sind.

### Der Erfolg hängt jetzt

`achRadium` ist damit nicht blockiert, sondern war nur nie angeschlossen. Sein Auslöser steht
im Original in `ItemEnergy.java:182`, unmittelbar nach der Strahlendosis von 500 — im Port
also in `LAMBDA_COFFEE_RADIUM`. Er hängt, wie im Original, an der Zentrifuge als Vorgänger und
trägt den Herausforderungs-Rahmen (`setSpecial()`).

Dafür brauchte der Erfolgsgeber eine zweite Fassung von `erfolg(...)`, die einen **Stapel**
statt eines `ItemLike` als Sinnbild nimmt: ein Meta-Gegenstand lässt sich anders nicht
bezeichnen.

**47 von 61.** Die Zählung im Kopf von `NtmAdvancementProvider` ist entsprechend berichtigt.

### Was dabei noch auffiel

Die **Schimmerwaffen** haben im Port bereits Modelle und Texturen — `shimmer_sledge.obj`,
`shimmer_axe.obj` und beide Texturen liegen da, und `ResourceManager` hält Felder dafür. Die
Gegenstände fehlen trotzdem. Dabei ist eine Schiefheit entstanden: `shimmer_axe` wird geladen,
`shimmer_sledge` steht daneben **auskommentiert** — ein Modell im Speicher, das niemand
zeichnet, und ein Feld, das immer `null` ist. Das gehört in die Runde, die die beiden Waffen
nachreicht, und ist hier nur vermerkt.

## Runde 272 — Das Messer, und ein Herzcontainer, der nie etwas gab

Nächster Blocker aus der Liste: `injector_knife`, Auslöser von `achSomeWounds`. Er ist ein
**Rüstungsmodul**, und das Modulsystem steht seit Runde 137 mit achtzehn Einträgen — gefehlt
hat nur die Klasse.

### Der Fund: ein Summand, den niemand liest

`ItemModHealth` (Herzcontainer, schwarzer Diamant) legt seit seiner Portierung
`Attributes.MAX_HEALTH` in die Eigenschaftskarte, die `ArmorModHandler.updateMods` jeden Tick
zusammenrechnet. Angewandt wird die Karte danach aber nur über `TRACKED_ATTRIBUTES` — und
darin standen **nur Tempo und Rückstoß**.

Beide Module gaben also **gar keine Lebensenergie**. Zwanzig Punkte der eine, vierzig der
andere, und beide wirkungslos, seit sie im Port stehen. Aufgefallen ist das erst, weil das
Messer denselben Weg benutzt — nur mit umgekehrtem Vorzeichen.

`MAX_HEALTH` steht jetzt in der Liste, mit `ADD_VALUE`: im Original hängt der Wert als
absolute Attributänderung am Rüstungsteil (`ItemModHealth.getModifiers`), nicht anteilig.

### Das Messer

Alle 50 Ticks zwei Punkte höchster Lebensenergie, bis zwei übrig sind; dann hört es auf, und
genau in dem Augenblick fällt der Erfolg.

Zwei Stellen weichen bewusst ab:

**Wo der Abzug steht.** Das Original hängt eine Attributänderung mit fester Kennung an den
Träger und ersetzt sie bei jedem Schnitt durch eine größere — die Summe steht also im Träger.
In 1.21 rechnet `ArmorModHandler` die Summe jeden Tick neu aus den Modulen zusammen, darum
steht der Stand am Rüstungsteil, an derselben Stelle wie die Module selbst. Das überlebt
Abnehmen und Wiederanlegen, wie im Original.

**Wann der Erfolg fällt.** Das Original liest die Gesundheit *nach* dem Setzen der Änderung.
Im Port läuft `addAttributes` erst nach `modUpdate`, die neue Grenze steht also noch nicht —
sie wird vorgerechnet. Es ist derselbe Schnitt.

Nicht übernommen: der `bloodvomit`-Partikel und der Bildschirmruckler `properJolt`. Beides
gibt es im Port nicht. Der Klang (`entity.slicer`) bleibt.

**Ohne Rezept, und warum.** Im Original baut sich das Messer aus `injector_5htp` und einem
Eisenschwert. Der 5-HTP-Injektor fehlt dem Port — und mit ihm der Stabilitätseffekt, an dem
seine ganze Wirkung hängt (er zieht fünf Digamma ab und gibt Stabilität). Ein halber Injektor
wäre ein Gegenstand, der nichts tut. Das Rezept kommt darum mit ihm.

**48 von 61.**

## Runde 273 — Die Schwefelsäure, und wie man einen Schleimball auflöst

Nächster Blocker: `achSulfuric`. Beim Nachlesen zeigt sich, dass der Erfolg etwas anderes
verlangt, als sein Name nahelegt.

### Der Auslöser ist kein Trinken und kein Bauen

Er steht in `GenericFluidBlock.onEntityCollidedWithBlock`: fällt ein **Gegenstand** in eine
Flüssigkeit mit Schadensquelle, friert seine Bewegung ein und er nimmt jede Sekunde ein
Zehntel des Schadens. Stirbt er dabei und war er ein **Schleimball**, bekommen alle Spieler im
Umkreis von zehn Blöcken den Erfolg.

Und diese Stelle gilt nur für einen einzigen Block. Nachgemessen über alle
`new GenericFluidBlock`-Anmeldungen des Originals: **genau eine** setzt eine Schadensquelle,
nämlich `sulfuric_acid_block` (`ModBlocks.java:2345`). Der Zweig steht darum im Port nicht in
einer gemeinsamen Oberklasse, sondern in `SulfuricAcidLiquidBlock`.

### Was dazukam

Ein Fluidtyp mit den Zahlen des Originals (Dichte 1840, Zähigkeit 1000, Temperatur 273), die
beiden Fluide, der Block mit Sprengfestigkeit 500, beide Texturen aus der CE-Abspaltung.

Die Wirkung auf Lebewesen ist die des Originals: fünf Schaden je Tick, und wer schneller als
0,2 hineinfällt, wird auf halbe Geschwindigkeit gebremst. Das Zischen alle fünf Ticks bleibt;
die Wolkenpartikel entfallen, weil das Original sie auf dem Server erzeugt und das auf 1.21
ein Paketthema für sich wäre.

**Das Sinnbild ist ersetzt, der Auslöser nicht.** Das Original nimmt `achievement_icon` mit
dem Merkmal `BALLS` — einen Meta-Gegenstand, den der Port nicht hat. Dort steht jetzt der
Schleimball selbst, was ohnehin näher an dem liegt, was man tun muss. Vorgänger ist wie im
Original `slimeball`.

**49 von 61.**

## Runde 274 — Die Kartoffel, und wieder ein Meta-Gegenstand

`achPotato` war der letzte Blocker, den ich für „die ganze Batteriefamilie fehlt" gehalten
hatte. Auch das war falsch gemessen — zum zweiten Mal aus demselben Grund.

### Dieselbe Falle, dasselbe Muster

Die gewöhnlichen Batterien **gibt es** im Port: `battery_redstone` bis `battery_quantum` und
sechs Kondensatoren, alle als `BatteryPackItem.BatteryPackType`. Ein Meta-Gegenstand, also
wieder kein Registriername — und wieder an der Suche vorbei.

Nach Runde 271 ist das der zweite Fall. Die Regel, die daraus folgt und die jetzt hier steht:
**bei einem Namen aus dem Original, den man im Port nicht findet, ist die erste Frage nicht
„fehlt er?", sondern „ist er ein Eintrag in einer Aufzählung?"** Die Meta-Gegenstände sind
genau die Stellen, an denen 1.7.10-Namen keine Registriernamen mehr sind.

Gefehlt haben also nur die beiden Kartoffeln.

### Warum sie am Ende der Liste stehen

Der Platz in dieser Aufzählung **ist** das Metadatum am Stapel. Ein Eintrag in der Mitte würde
jeden dahinter verschieben — und damit jede Batterie in jeder gespeicherten Welt. Angehängt
wird nichts verschoben.

Das hatte eine Nebenwirkung: `isCapacitor()` las bis hierher die Reihenfolge ab
(`ordinal() > BATTERY_QUANTUM.ordinal()`), und die beiden Kartoffeln hinter den Kondensatoren
wären dadurch selbst zu Kondensatoren geworden — sichtbar am Modell, das der Sockel dafür
zeichnet. Jetzt trägt jeder Eintrag die Antwort als eigenes Feld.

### PotatOS spricht

Die große Kartoffelbatterie meldet sich alle 200 bis 300 Ticks zu Wort, solange sie Ladung hat
und in der Hand liegt — acht Ansagen, eine davon zufällig, und die **Tonhöhe hängt am
Ladestand**: leer klingt sie tief, voll hoch. So steht es in `ItemPotatos.onUpdate`.

Der Wartezähler liegt am Stapel, nicht im Gegenstand: ein Gegenstand ist ein Singleton, und
zwei Kartoffeln im selben Rucksack sollen nicht im Gleichtakt reden. Das Original legt ihn aus
demselben Grund ins NBT des Stapels.

Beide entstehen **voll** — aufladen lassen sie sich nicht, ihr Ladetempo ist null.

**50 von 61.**

## Runde 275 — Die Schimmerausrüstung: vier Teile für zwei Erfolge

`fiend` und `fiend2` verlangen etwas, das keine der bisherigen Runden verlangt hat: **zwei
Dinge gleichzeitig**. `ArmorUtil.checkForFiend` prüft die Jacke am Leib **und** die passende
Waffe in der Hand. Eine Waffe allein reicht nicht, eine Jacke allein auch nicht.

Portiert sind deshalb sieben Gegenstände: die drei Bauteile (`shimmer_handle`, `shimmer_head`,
`shimmer_axe_head`), die beiden Waffen und die beiden Jacken — mit allen sieben Rezepten des
Originals.

### Was die beiden Waffen tun

Der **Hammer** schleudert sein Ziel in Blickrichtung des Angreifers davon, fünffach genommen.
Auf einen Block angewandt schlägt er ihn heraus und wirft ihn als Trümmerstück hinterher —
`Rubble` gibt es im Port längst, samt Darsteller.

Die **Axt** halbiert die *aktuelle* Lebensenergie des Ziels (nicht die höchste — das tut der
Diamanthammer) und schlägt auf einen Block eine Spalte aus dreien heraus.

Beide respektieren die Grenze 6000: alles mit dieser Sprengfestigkeit oder mehr bleibt stehen.

Dafür brauchte `SpecialSwordItem` zwei Erweiterungen: einen `useOn`-Haken, den es bisher nicht
hatte, und eine zweite Fassung von `setHurtEnemy`, die auch den **Angreifer** durchreicht —
der Hammer stößt in *dessen* Blickrichtung, nicht in die des Ziels.

### Der Schadenswert, nachgerechnet

Das Original legt für beide ein eigenes Material an: `addToolMaterial("SHIMMERSLEDGE", 1, 0,
25.0F, 26F, 200)`. Haltbarkeit **null** heißt dort unzerstörbar; der Port hat das schon beim
Desh- und Wismutwerkzeug so.

Der Schaden geht in 1.7.10 aus `4 + 26` des Materials hervor, also 30. In 1.21 setzt sich
derselbe Wert anders zusammen: 26 Schadensbonus des Tiers + 3 der Schwertformel + 1
Grundschlag des Spielers. Dieselbe 30, auf einem anderen Weg.

### Ein zerbrochener Satz

Beim Eintragen fiel auf, dass der Kopfkommentar von `NtmAdvancementProvider` seit Runde 272
einen halben Satz enthielt: dort wurde „das Messer (someWounds)," aus einer Aufzählung
entfernt, ohne den Rest zu lesen, und zurück blieb „die vier Bosse, das / der Speer". Jetzt
steht dort die gemessene Liste der neun übrigen, mit den Klassennamen, an denen sie hängen.

**52 von 61.**

## Runde 276 — Der Digamma-Speer: zwei Erfolge aus einer Kernschmelze

Der nächste günstigste Blocker war der Speer — er entsperrt zwei Erfolge auf einmal,
`digamma_kauai_moho` und den unerreichbaren `digamma_up_on_top`, der auf ihm aufbaut.

`EntitySpear` entsteht, wenn eine RBMK-Kernschmelze einen DRX-Stab erwischt: hundert Blöcke
über der Anlage, in der Mitte ihres Umrisses. Von dort sinkt er mit 0,2 je Tick herab und sät
dabei Digamma — jeden Tick eine Explosion vom Radius 7,5 an einer gaußverteilten Stelle im
Umkreis. Liegt die Stelle näher als zwanzig Blöcke, entsteht ein Schachbrett aus Digamma-Schutt,
sonst Asche. Jeder Spieler der Welt bekommt dabei 0,05 Digamma — und den Erfolg.

Trifft er auf Grund, wartet er hundert Ticks und verseucht dann **alles**, was in der Welt
lebt, nicht nur seine Umgebung. Dazu spielt `weapon.dFlash`.

### Zwei Namen, ein Block

Der Gittermustersetzer des Originals verlangt `pribris_digamma`. Den gibt es im Port nicht —
dachte ich. Gemessen: `ModBlocks.java:2144` ist die einzige Stelle, die `RBMKDebrisDigamma`
anlegt, und der Port führt genau diese Klasse als `rbmk_debris_digamma`. Es ist derselbe Block,
nur der Registrierungsname weicht ab. Zu portieren war also nur die **Asche** — und deren
Textur `ash_digamma.png` lag schon seit einer früheren Runde verwaist im Port, ohne Block, der
sie benutzt hätte.

Der neue Block fiel prompt durch `tab-check`: ohne Kreativreiter ist er weder im Kreativbau
noch in JEI auffindbar. Er steht jetzt neben `rbmk_debris_digamma`, wo er hingehört.

### DIGAMMA2 gibt es hier nicht

Das Original unterscheidet beim Verseuchen zwischen `DIGAMMA` und `DIGAMMA2`. Der Port kennt
nur `DIGAMMA`, und seine `contaminate()` schaut bei Digamma ohnehin nicht auf die Art
(`ContaminationUtil.java:176`). Beide Aufrufe des Originals gehen deshalb über dieselbe Art —
ohne Verhaltensunterschied.

### Der Darsteller

`lance.obj` und `lance.png` kommen aus der CE-Abspaltung; der Darsteller verschiebt um +15,
dreht 180° um X und skaliert zweifach — der Speer hängt also mit der Spitze nach unten,
fünfzehn Blöcke über seinem eigenen Mittelpunkt.

**54 von 61.**

## Runde 277 — Die Sojus stand zu Unrecht auf der Blockerliste

Nach dem Speer blieben sieben Erfolge. Zwei davon, `soyuz` und `space`, führte die Liste unter
„die Sojus" — als wäre die Rakete nicht portiert.

Sie ist es. `com.hbm.entity.missile.Soyuz` steht seit einer früheren Runde im Port, samt
Startrampe (`SoyuzLauncherBlockEntity`), Satellitenregister (`XSatelliteRegistry`),
Abgaspartikeln und dem `alarm.soyuzed`-Schrei. Gefehlt haben **zwei Auslöserzeilen** und eine
Schadensart.

Das ist das **dritte Mal**, dass die Messung aus Runde 270 danebenlag — nach dem Radiumkaffee
(Runde 271) und der Kartoffelbatterie (Runde 274). Alle drei Male hat dieselbe Messung nur
nach Registriernamen gesucht und übersehen, dass die Sache unter einem anderen Namen oder in
einer anderen Klasse längst da war.

### Was gefehlt hat

**Die Abgasfahne hatte keinen eigenen Schaden.** Im Port stand dort `damageSources().magic()`
mit einem `todo`. Das Original nimmt `ModDamageSource.exhaust` mit `setDamageIsAbsolute()` und
`setDamageBypassesArmor()`: wer beim Start unter der Rakete steht, verbrennt, ganz gleich, was
er trägt. Jetzt gibt es `NtmDamageTypes.EXHAUST` mit den drei Tags, die dieses Paar in 1.21
ausmachen — `BYPASSES_ARMOR`, `BYPASSES_EFFECTS`, `BYPASSES_RESISTANCE`.

**Der erste Erfolg** fällt genau dort: Spieler in der Abgasfahne, jeden Tick.

**Der zweite Erfolg** hing an einem Zweig, den der Port gar nicht hatte. Liegt beim Start ein
`flame_pony` in Nutzlastschacht 0, tut die Rakete im Orbit nichts weiter, als fünfundzwanzig
Leuchtspuren zu versprengen — und gibt **jedem Spieler der Welt** den Erfolg, nicht nur dem,
der gestartet hat. Neunzig Millionen Dollar für ein Plüschpony.

### Was noch offen bleibt

Fünf Erfolge: die vier Bosse (`EntityRADBeast`, `EntityMaskMan`, `EntityBOTPrimeHead`,
`EntityUFO` — keine davon portiert) und `SILEX`. Für die SILEX gilt die Begründung geprüft:
ihre `TileEntitySILEX` hat ein Feld `hasLaser` und vergleicht `recipe.laserStrength` mit ihrem
eigenen `mode` — sie braucht den FEL wirklich, das ist keine Vermutung.

Offen geblieben ist auch das zweite `todo` der Sojus: die Landekapsel (`EntitySoyuzCapsule`
samt `soyuz_capsule`-Block). Sie hängt an keinem Erfolg.

**56 von 61.**

## Runde 278 — Die Landekapsel, und eine Fracht, die ins Leere ging

Das zweite `todo` der Sojus: `EntitySoyuzCapsule`. Die Rakete kennt zwei Modi — Modus 0 bringt
einen Satelliten in den Orbit, Modus 1 ist der **Rückflug**: statt zu steigen, setzt sie ihre
Fracht in sechshundert Blöcken Höhe über einem Zielpunkt ab. Von dort kommt die Kapsel am
Fallschirm herunter, schlägt ein und steht danach als Block in der Welt, mit der Fracht darin
— und der Rakete, mit der sie gekommen ist, im neunzehnten Fach.

Portiert sind fünf Klassen: die fallende Entität, ihr Darsteller, der Block, seine
Blockentität und deren Darsteller. Alle Modelle und Texturen lagen schon im Port —
`soyuz_lander.obj` war sogar bereits in einen VBO geladen, aber nur der Fallschirm daraus
wurde benutzt (von der Fallschirmkiste). Die Kapsel selbst zeichnete niemand.

### Eine Fracht, die ins Leere ging

Beim Anschließen fiel ein echter Portierungsfehler auf. Das Original baut in der Startrampe
eine **leere** Liste und hängt die achtzehn Frachtfächer an; sie liegen danach auf 0 bis 17,
und genau dort liest die Kapsel sie wieder aus. Der Port hatte stattdessen eine Liste der
Länge 27 **vorbelegt** und trotzdem angehängt — die Fracht landete auf 27 bis 44 und war weg.

Das war folgenlos, solange es die Kapsel nicht gab: `deployPayload()` hatte für Modus 1 nur
ein `todo`. Jetzt nicht mehr.

### Ein Fach außerhalb des Rasters

Neunzehn Fächer passen in kein Rechteck. Die Kistenbasis des Ports kennt bisher nur Raster;
sie hat jetzt einen Haken für Fächer daneben — die Kapsel setzt ihr neunzehntes links neben
das Raster, genau wie das Original.

Der Haken gibt **Zahlen** zurück, kein Menü. Gemessen: nimmt er das Menü als Parameter, meldet
der javac-Lauf des Torwächters (der ohne Minecraft-API läuft) im Menü einen rekursiven
Konstruktoraufruf. Mit einem Zahlenfeld bleibt er still.

### Was offen bleibt

Das Original streut verrostete Kapseln an Stränden, mit einer Schallplatte darin
(`record_glass`, die einzige des Mods, `setCreativeTab(null)` — nur dort zu finden). Beides
fehlt noch: der Port hat keine Jukebox-Unterstützung, und eine Schallplatte auf 1.21 braucht
einen `JukeboxSong`-Datenpackeintrag. Sound, Textur und Modell dafür liegen in der
CE-Abspaltung bereit.

## Runde 279 — Die Platte im Sand, und ein Tor, das fünfzehn Minuten spart

Die Kapsel aus Runde 278 stand in der Welt, aber niemand konnte eine finden: das Original
streut sie an Stränden, und das fehlte noch. Dazu gehört die einzige Schallplatte des Mods.

### Die Schallplatte

`record_glass` trägt im Original `setCreativeTab(null)` — sie steht in keinem Reiter, in
keinem Rezept, in keiner Beutetabelle. Der **einzige** Weg zu ihr führt über eine verrostete
Landekapsel, vier Blöcke tief im Strandsand.

Auf 1.7.10 war eine Platte ein Gegenstand, der seinen Klangnamen selbst kannte. Auf 1.21 ist
sie ein Datenpackeintrag — Klang, Anzeigename, Spieldauer und die Zahl, die ein Komparator
neben dem Plattenspieler ausgibt; der Gegenstand zeigt nur noch darauf. Der Port hatte
bisher gar keine Jukebox-Unterstützung; `NtmJukeboxSongs` ist die erste.

Die Spieldauer ist nachgemessen, nicht geschätzt: 2 986 977 Abtastwerte bei 48 kHz, also
62,23 Sekunden. Der Anzeigename ist der des Originals — `item.record.glass.desc=? ? ?`, mehr
verrät der Mod über diese Platte nicht.

### Vier Blöcke tief

Das Original setzt die Kapsel auf `getHeightValue - 4` und prüft, ob drei Blöcke über ihr noch
fester Grund steht. Sie liegt also **begraben**; was man am Strand sieht, ist Sand. Häufigkeit
und Biom sind die des Originals: jeder hundertste Chunk (`WorldConfig.capsuleStructure`), nur
am Strand.

### Das 44. Tor

Runde 278 ist in der CI durchgefallen — nach vier Minuten Bauzeit, an einer Zeile, die in
Sekunden zu prüfen gewesen wäre:

```
particleOnlyBlock(SOYUZ_CAPSULE, withDefaultNamespace("textures/models/.../soyuz_lander.png"))
```

`ModelBuilder.texture` hängt `textures/` und `.png` **selbst** an. Richtig ist
`modLoc("models/soyuz_capsule/soyuz_lander")`. Der Fehler stand im Quelltext; kein Tor hat
hingeschaut. `model-resolve-check` läuft erst hinter `runData` — und `runData` war genau daran
gescheitert.

`tools/texture-ref-check.sh` sammelt jetzt jedes ausgeschriebene `modLoc("…")` aus den
Datenerzeugern und fragt, ob es auf eine Textur, ein Modell von Hand oder ein erzeugtes Modell
zeigt. 467 Literale, null Funde — und in der Gegenprobe, mit der falschen Zeile wieder
eingesetzt, genau einen. Was es nicht sieht, steht in seinem Kopf: zusammengesetzte Pfade wie
`modLoc("block/" + name)`.

**44 Tore.**

## Runde 280 — Der Maskenmann, und Munition, die kein Spieler abfeuert

Von den fünf übrigen Erfolgen hängen vier an Bossentitäten. Der Maskenmann ist mit 122 Zeilen
der kleinste — aber nicht der einfachste: er brachte ein ganzes Teilsystem mit, das der Port
bisher gar nicht hatte.

### Munition ohne Waffe

Die drei KI-Klassen des Maskenmanns verschießen Geschosse aus `BulletConfigSyncingUtil` — dem
**alten** Geschosssystem des Originals (`BulletConfiguration`, `EntityBulletBaseNT`), das eine
Zahlentabelle führt, damit Server und Klient dieselbe Einstellung meinen. Der Port kennt nur
das neue (`BulletConfig`, `BulletBaseMK4`), und dort bringt jedes Geschoss seine Einstellung
selbst mit; die Tabelle entfällt ersatzlos.

`XFactoryNPC` ist die Übersetzung: fünf Geschosse, die kein Spieler abfeuert. Vorbild war
`XFactoryTurret` — die Munition der Geschütztürme, die im Port denselben Weg schon gegangen
ist.

Der Munitionsgegenstand bleibt bei allen fünf die Maskenmann-Münze. Nicht weil sie verschossen
würde, sondern weil das alte System ohne Munitionsangabe nicht auskam; eine andere Angabe wäre
eine Änderung ohne Anlass.

### Drei Angriffe, und einer davon feuert selbst

Der Maskenmann hält von sich aus Abstand: seine Annäherung zielt nicht auf den Spieler, sondern
auf einen Punkt **zehn Blöcke davor**. Unter zehn Blöcken feuert die Minigun, darüber der Laser
mit drei Angriffen im Wechsel — Kugel, Rakete, Salve.

Die **Kugel** ist die interessanteste: sie fliegt langsam, hält eine Minute und schickt alle
zehn Ticks einen Bolzen auf *jeden* Spieler im Umkreis von fünfzig Blöcken. Die **Salve** wirft
fünf Leuchtspuren, und wo eine aufschlägt, fällt ein Meteor aus dreißig Blöcken Höhe.

Der Wechsel zwischen den Angriffen ist nicht reihum: das Original zählt die Ordnungszahl um
null oder eins weiter und nimmt den Rest bei drei — nie zweimal derselbe hintereinander.

### Tausend Lebenspunkte, und ein Ei

Er nimmt Feuer und Magie gar nicht, Geschosse und Sprengungen halb, und alles über fünfzig wird
darüber hinaus halbiert: ein Schlag von tausend kommt als 525 an. Bei halber Lebensenergie
sprengt er einmal über sich, und ab da trägt er statt eines Kopfes einen Schädel — mit einem
Zettel daneben: IOU.

Ein geworfenes **Ei** tötet ihn mit einem Zehntel Wahrscheinlichkeit auf der Stelle, ohne
Erfahrung. Das ist die Hintertür des Originals und steht hier, wie sie dort steht.

### Die Wolkenflasche, halb

Eines seiner Beutestücke ist `bottled_cloud` — im Original kein einfacher Gegenstand, sondern
ein **Rüstungsmodul** für die Brustplatte: ein Achtel mehr Tempo, und drei waagerechte Sprünge.
Der Temposchub steht; die Sprünge nicht. Der Port hat die Taste dafür (`HbmKeybinds.DASH`), aber
niemanden, der sie ausliest — das ganze Dash-System steht im Original in
`EntityEffectHandler:716` und ist nicht übernommen. Das steht so in der Klasse, statt ein
Verhalten zu erfinden, das es nicht gibt.

**57 von 61.** Offen: drei Bosse und die SILEX.

## Runde 281 — Das Strahlenbiest: eine Lohe, die nicht brennt

Der zweite Boss. `EntityRADBeast` sieht aus wie eine Lohe, verhält sich wie eine — und schießt
doch keine Feuerbälle: bis dreißig Blöcke bestrahlt es sein Ziel **unmittelbar**, sechzehn
Punkte, und lädt dabei den Chunk unter sich mit hundert Einheiten Strahlung auf. Was man fliegen
sieht, ist kein Geschoss, sondern der grüne Strahl des Darstellers.

### Zwei Größen, ein Erfolg

Das gewöhnliche Biest hat 120 Lebenspunkte, der Anführer 360. Das Original unterscheidet beide
nirgends durch ein Feld, sondern **an der Höchstenergie** (`getMaxHealth() > 150`) — und daran
hängt dreierlei: ob die Münze fällt, welche Teilchen es versprüht, und ob sein Tod den Erfolg
auslöst. Der Port führt dieselbe Prüfung an derselben Stelle.

### Was es sonst ausmacht

Es steigt auf, wenn sein Ziel über ihm ist — um eine Höhe, die es sich alle hundert Ticks neu
auswürfelt, gaußverteilt um einen halben Block mit einer Streuung von drei. Es fällt nur
gebremst und nimmt keinen Fallschaden. Wasser dagegen tut ihm jede Runde weh.

Seine Beute sind ein bis drei abgebrannte ZIRNOX-Stäbe — und wenn es im Wasser stirbt,
stattdessen doppelt so viel Abfall.

### Zwei Dinge blieben liegen

Die **M65-Maske**: das Original legt der Lohe ein eigenes Kopfmodell auf, ein Techne-Modell aus
zehn Formen. Auf 1.21 wäre das eine eigene LayerDefinition. Das Biest steht ohne sie — die Maske
ist Schmuck, kein Verhalten.

Die **Geiger-Stimme**: das Original nimmt als Lebenslaut `item.geiger1` bis `6`. Die Klänge hat
der Port, aber keinen Mob, der sie als Stimme führt; das wäre eine Klangtabelle für sich.

**58 von 61.** Offen: zwei Bosse und die SILEX.

## Runde 282 — Balls-O-Tron Prime: ein Wesen aus fünfundsiebzig Teilen

Der dritte Boss, und der einzige, der kein einzelnes Wesen ist: ein Kopf und
**vierundsiebzig Glieder**, die er beim Erscheinen selbst anlegt. Alle tragen dieselbe
Kopfkennung und finden sich daran wieder; jedes Glied folgt seinem Vordermann auf dreieinhalb
Blöcke Abstand.

### Er läuft nicht, er schwimmt durch die Welt

Keines der Teile nutzt die Wegsuche. Jedes setzt seine Bewegung unmittelbar aus dem Abstand zu
seinem Wegpunkt, und Blöcke halten ihn nicht auf. Nur die Reibung unterscheidet sich: in der
Luft 0,995, im Boden 0,98 — und ein Glied nimmt davon noch neun Zehntel, damit die Kette hinten
nachhängt.

Der Kopf hat zwei Zustände. Ohne Ziel kreist er um seinen Erscheinungsort, hundert Blöcke breit
und sechzig hoch. Mit Ziel taucht er erst auf zehn Blöcke Höhe herunter; erst wenn er einmal
unter fünfzehn war, geht er gerade auf sein Ziel zu — und vergisst das mit einer
Wahrscheinlichkeit von eins zu achtzig wieder.

### Schaden geht immer an den Kopf

Nur der Kopf führt Lebensenergie. Trifft etwas ein Glied, reicht es den Schlag an seinen
Vordermann weiter, bis er beim Kopf ankommt. Ertrinken und Ersticken prallen ab, und der Wurm
kann sich nicht selbst verletzen: das Original prüft dafür die **Kopfkennung des Angreifers** —
dieselbe Kennung heißt dieselbe Kette.

Der Schlag eines Glieds nimmt **drei Viertel der aktuellen** Lebensenergie, nicht der höchsten.
Wer mit einem Prozent hineinläuft, verliert drei Viertel davon.

### Wie ein Glied stirbt

Findet es niemanden mehr vor sich, zieht es sich 1999 Punkte ab — im Original der Weg, ein
Glied zu entfernen, ohne `setDead` zu rufen. Und hat es seinen Kopf verloren, zerplatzt es mit
einer Wahrscheinlichkeit von eins zu sechzig je Tick.

### Zwei Laser mehr

`XFactoryNPC` ist um `worm_laser` und `worm_bolt` gewachsen. Der Kopf schießt fünf Strahlen auf
einmal, jeden mit wachsender Streuung; ein Glied einen einzigen, schwächeren. Beide sehen durch
Glas und Laub — die Sichtprüfung des Originals fragt nur nach festen Blöcken.

**59 von 61.** Offen: der UFO und die SILEX.

## Runde 283 — Das UFO, und eine Explosion, die zweimal dastand

Der letzte der vier Bosse. Zwanzigtausend Lebenspunkte, fünfzehn Blöcke breit, und er schlägt
auf drei Arten zu.

**Der Fangstrahl** geht senkrecht nach unten bis zum ersten festen Block. Wer darin steht, nimmt
tausend Punkte, fängt fünf Sekunden Feuer und bekommt fünf Einheiten Strahlung. Er geht an,
sobald das Ziel waagerecht näher als fünfundzwanzig Blöcke ist.

**Der Laser** kommt nicht aus der Mitte, sondern aus einem Drehpunkt **zehn Blöcke neben** dem
UFO, in einem Winkel von minus achtzig bis plus achtzig Grad zur Zielrichtung — deshalb wirkt
es, als schösse der Rand der Scheibe.

**Die Raketen** sind gelenkt, richten aber keinen Blockschaden an und sprengen nicht. Was tötet,
ist der Aufschlag.

Welche Waffe dran ist, entscheidet **die Uhr, nicht die Lage**: in den ersten zweihundert Ticks
jedes Dreihunderterblocks der Laser, in den letzten hundert die Raketen.

### Der Flug ist ein Überschießen

Es steuert einen Punkt an, der fünfunddreißig Blöcke **hinter** seinem Ziel liegt, und das
meistens aus einer zufällig gedrehten Richtung — daraus entsteht das Kreisen. Bewegt wird nur,
solange der Kurszähler läuft; sonst steht es still in der Luft.

### Eine Explosion, die zweimal dastand

Sein Tod ist eine kleine Atomexplosion. Die gab es im Port noch nicht als Klasse — aber
**Zeile für Zeile** in `FissureBombBlock.explodeEntity`: dieselben fünf Schritte, dieselben
Zahlen (Druckwelle 20, Todeskreis 55, Strahlungsstufe 3), also genau `PARAMS_MEDIUM` des
Originals.

Jetzt steht sie einmal, in `ExplosionNukeSmall`, und die Spaltbombe ruft dieselbe Stelle wie das
UFO. Das ist keine neue Mechanik, sondern eine, die schon zweimal dastand.

**60 von 61.** Offen bleibt allein `SILEX` — und dafür braucht es den FEL.

## Runde 284 — Die fünf Laserkristalle, und eine Behauptung, die nicht stimmte

Der letzte offene Erfolg heißt `SILEX`. Vor der Maschine kommt, was sie steuert: die fünf
Laserkristalle des FEL.

Jeder gibt dem Laser eine **Wellenlänge**, und die entscheidet, welche Rezepte die SILEX
darunter fahren kann — von Kohlendioxid in Desh (infrarot) über BiSmUTh (sichtbar), CMB-
Schrabidat (ultraviolett) und Dineutronium-Funken (Gamma) bis zum Digamma-Kristall, dessen
Beschreibung das Original mit dem Verschleierungsformat schreibt: man liest sie nie, sie
flackert.

### Einer war schon da — und vier Tore haben es gemerkt

Der BiSmUTh-Kristall steckt in der Wismut-Brustplatte und kam deshalb früher als die anderen.
Beim Nachtragen der übrigen vier fiel er doppelt an, und **vier Tore meldeten es gleichzeitig**:
`dupreg-check` (zweimal derselbe Anmeldename), `location-check` (dieselbe ResourceLocation),
`lang-check` (derselbe Übersetzungsschlüssel) und `syntax-check` (dieselbe Konstante). Jedes
davon hätte den Start des Spiels abgebrochen.

Sein Kommentar sagte: die vier anderen gehören zum FEL, „den der Port nicht hat", und die
Wellenlängen-Anzeige bleibe darum draußen. Beides ist jetzt überholt; der Kommentar sagt das.

### Eine Behauptung, die nicht stimmte

In Runde 277 stand hier und im Kopf von `NtmAdvancementProvider`, die SILEX prüfe `hasLaser`.
Nachgemessen mit `git grep` über das ganze Original: **das Feld wird nirgends gesetzt.**
`TileEntitySILEX.java:39` ist die einzige Fundstelle im ganzen Mod.

Was sie wirklich prüft, ist ihr Feld `mode` — und das setzt ihr der FEL von außen
(`TileEntityFEL.java:84 ff.`, über den Kristall in seinem Schacht). Ohne FEL bleibt der Modus
auf `NULL`, und nur Rezepte ohne Wellenlängenanforderung laufen.

Und noch etwas ist nachgemessen: der Erfolg hängt am **Bau** der Maschine
(`AchievementHandler.craftingAchievements`), nicht an ihrem Betrieb.

**60 von 61** — unverändert, aber der Weg zum letzten ist jetzt vermessen statt vermutet.

## Runde 285 — Die SILEX-Rezepte: 295 Einträge, und keiner davon ist Zufall

Vor der Maschine kommen ihre Rezepte. `SILEXRecipes` bringt **91 Rezeptzeilen** in den Port —
51 davon stehen in der Fünferschleife der Brennstoffpellets, macht **295 Einträge** in der
Karte und **391 gewichtete Ausgaben**, genau so viele wie im Original.

### Drei Zahlen machen ein Rezept

Die erste sagt, wie viel Lösung ein Stück des Eingangs ergibt — ein Barren 900, ein Pellet 600,
Balefire nur 400. Die zweite, was ein einzelner Ausgang davon kostet; bei 900 und 100 sind das
die neun Nuggets, die man erwartet. Die dritte ist die **Wellenlänge**: der Strahl des FEL muss
mindestens so weit oben liegen, und liegt er höher, läuft die Maschine schneller. Deshalb ist
ein Digamma-Kristall auch dann etwas wert, wenn man nur Uran trennt.

### Die Gewichte sind Zusagen, keine Chancen

Das Original würfelt nicht. Es zählt mit einem festen Primzahlschritt durch die Gewichtsleiter —
wer eine Tonne unangereichertes Uran durchlässt, bekommt am Ende exakt die 86:10:2:2, die im
Rezept stehen. Das Durchzählen gehört der Maschine; hier stehen nur die Anteile.

### Der Metawert des Abfalls ist nicht die Ordnungszahl

Das Original führt zwei getrennte Aufzählungen — `ItemWasteLong.WasteClass` mit fünf Klassen,
`ItemWasteShort.WasteClass` mit acht. Der Port hat sie in **eine** Aufzählung mit neun Klassen
zusammengelegt, und die Reihenfolge der beiden Listen steht als `SHORT` und `LONG` daneben.

Wer hier `WasteClass.THORIUM.ordinal()` einsetzte, bekäme stillschweigend den falschen Abfall:
Thorium ist die siebte Klasse der Gesamtliste, aber die **vierte** des langlebigen Abfalls. Der
Port schlägt den Platz deshalb am Gegenstand selbst nach und wirft, wenn er ihn nicht findet.

### Zwei Gegenstände, die es noch nicht gab

**`dust_tiny`** fällt an, wenn abgeklungener Abfall zerlegt wird — neun davon ergeben einen
ganzen Staubhaufen, wie im Original (`MineralRecipes.add1To9Pair`); das Paar ist mitgeliefert.

**`powder_ash_fullerene`**, die sechste Aschesorte, entsteht nur an einer Stelle: sichtbares
Licht auf Fullerenlösung. Im Kristallisator stand seit Runden ein Kommentar, der genau das
festhielt — das Rezept zum CFT-Barren fehle, weil seine einzige Quelle der nicht portierte
SILEX sei. Der Kommentar ist jetzt ein Rezept.

### Ein Übertragungsfehler, der auffiel

Beim Zirkonium-Brüter zahlen Wismut und Plutonium-241 in der xenonvergifteten Reihe drei
Zirkoniumnuggets für drei Xenonpulver — 147 statt 150. Beim Americium-Brüter **nicht**: dort
stehen in beiden Reihen 150. Die erste Fassung hier glich das an und erfand damit eine Wirkung
ohne Ursache; nachgemessen und zurückgenommen, mit einem Satz im Kommentar, damit es beim
nächsten Lesen nicht wieder „korrigiert" wird.

**44 Tore grün.** Was fehlt, ist die Maschine selbst — und mit ihr der letzte Erfolg, denn der
hängt an ihrem **Bau**, nicht an ihrem Betrieb.

## Runde 286 — Die SILEX: 61 von 61

Die Maschine, auf die der letzte Erfolg wartete. Fünf Blöcke lang, drei breit, zwei hoch, mit
zwei Anschlüssen quer über dem Kern — dort, wo im Modell die Rohre sitzen.

### Sie kann von sich aus nichts

Ihr Feld `mode` wird **an jedem Tickende auf NULL zurückgesetzt**. Nur ein FEL über ihr setzt es
neu, und der kommt erst in der nächsten Runde. Bis dahin steht die Maschine da, nimmt Peroxid
an, löst ihren Eingang darin — und wartet auf Licht. Das ist kein halber Port, sondern genau
das Verhalten des Originals: ohne Strahl laufen nur Rezepte ohne Wellenlängenanforderung, und
davon gibt es keines.

### Die Ausgabe wird nicht gewürfelt

Ein Zähler springt nach jedem Ausgang um **137** weiter — eine Primzahl — und wird auf die
Summe der Gewichte umgebrochen. Über viele Durchgänge trifft er damit genau die Anteile des
Rezepts. Deshalb sind die Zahlen aus Runde 285 Zusagen und keine Hoffnungen.

### Eine Tabelle, die nichts tat

Das Original führt neben den Rezepten eine zweite Tabelle mit drei Flüssigkeiten — UF6, PUF6,
Todeslösung — und fragt sie, ehe es nach einem Rezept sucht. Nachgemessen: sie bildet jede der
drei auf **ihr eigenes Sinnbild** ab, also auf genau das, was der zweite Zweig ohnehin bildet,
und für alle drei findet sich ein Rezept (die Hexafluoride über die Übersetzung auf ihren
Barren, die Todeslösung unmittelbar). Die Tabelle ändert nichts; im Port steht sie nicht, und
im Kommentar steht, warum.

### Die Welle

Quer über das Fenster läuft eine Sinuskurve, deren Frequenz sich mit jeder Stufe der
Wellenlänge verdoppelt: Infrarot schwingt träge, Digamma flimmert. Sichtbares Licht hat keine
feste Farbe, sondern schillert durch den Farbkreis. Ohne Kristall steht gar keine Welle da —
und das ist die ehrlichste Anzeige, die diese Maschine haben kann.

### 61 von 61

`achSILEX` hängt im Original am **Bau** der Maschine, nicht an ihrem Betrieb
(`AchievementHandler.craftingAchievements`). Mit dem Block ist er fällig. Die Erfolgsliste ist
damit geschlossen; Aufgabe #150 ist erledigt.

**44 Tore grün.** Offen für die nächste Runde: der FEL, der ihr den Strahl gibt, und die
JEI-Ansicht ihrer 295 Rezepte.

## Runde 287 — Der FEL gibt der SILEX ihr Licht

Neun Blöcke lang, drei breit, und der Strom kommt hinten herein. Der Freie-Elektronen-Laser tut
selbst nichts Nützliches: er schießt einen Strahl geradeaus, und was in dieser Bahn steht, hat
ein Problem. Sein Zweck ist die Maschine aus Runde 286.

### Der Kristall bestimmt alles

Ohne Kristall im Schacht ist der Modus NULL, der Strahl bleibt aus und die Maschine zieht keinen
Strom. Mit Kristall kostet ein Tick **1250 · 3^Stufe**: infrarot 3750, Digamma gut 300.000. Die
Bahn ist 24 Blöcke lang und läuft einen Block über dem Kern; das erste Undurchsichtige hält sie
auf und fängt mit einem Fünftel Wahrscheinlichkeit zu brennen an, sofern es nicht sprengfester
als 75 ist.

Wer in der Bahn steht: sichtbares Licht blendet **und** zündet (das Original schreibt hier einen
absichtlichen Durchfall im `switch`), Infrarot und UV zünden, Gamma verstrahlt, Digamma tut das,
was Digamma tut.

### Zwei Funde beim Messen

**Die ERR.-Anzeige kam nie zurück.** Das Original setzt `missingValidSilex` genau einmal auf
`false` und nie wieder auf `true`. Nach dem ersten Treffer zeigt das Fenster also für immer
LIVE, auch wenn die SILEX längst abgebaut ist. Hier wird die Marke vor jedem Durchlauf
zurückgesetzt — damit zeigt das Fenster das, was sein Name sagt.

**Ein Zweig, den niemand erreicht.** Das Original lässt Flüssigkeiten in der Bahn verdampfen.
Der Zweig ist tot: eine Flüssigkeit ist nicht undurchsichtig, der Strahl ist im Zweig darüber
schon durch sie hindurch. Er steht im Port nicht, und der Kommentar sagt, warum.

### Zwei Tore haben mitgelesen

`dupreg-check` fand, dass `block.fel` **längst angemeldet war** — als `FEL_LOOP`, samt
Klangdefinition und `.ogg`, seit einer Runde, die den Klang schon vorbereitet hatte. Meine
zweite Anmeldung hätte das Hochfahren geworfen.

`offscreen-check` fand, dass `RenderFEL` ohne `shouldRenderOffScreen` dasteht. Ein großer
Sichtkasten reicht dafür nicht — er kann nur zusätzlich wegschneiden. Der Strahl wäre
verschwunden, sobald die Maschine selbst aus dem Bild fällt.

### Und die Ansicht der 295 Rezepte

Die SILEX hat jetzt ihre JEI-Kategorie: ein Eingang, bis zu sieben Ausgänge, unter jedem sein
Anteil in Prozent. Das Original zeigt höchstens sechs und lässt deshalb bei den drei
Schrabidiumpellets das Xenon weg; die Rezepte bleiben, wie sie sind, aber diese Ansicht kann
sieben zeigen und zeigt sie auch.

**44 Tore grün.** Der SILEX-Zweig ist damit vollständig: Rezepte, Maschine, Laser, Kristalle,
Erfolg und Ansicht.

## Runde 288 — Die Ente, die zu lange in der Strahlung stand

Drei auskommentierte Stellen im Port haben auf diese Runde gewartet, und alle drei gehören
zusammen.

### Ab 200 Rad ist es keine Ente mehr

`EntityEffectHandler` verwandelt bestrahlte Tiere: die Kuh wird ab 50 Rad zum Pilzkuh, der
Dorfbewohner ab 500 zum Zombie. Der dritte Fall fehlte — **die Ente wird ab 200 Rad zur
Quackos**, fünfundzwanzigmal so groß, unverwundbar, reitbar und selbst gegen Strahlung immun.

Der Vergleich prüft die Klasse **genau**, nicht `instanceof`: sonst würde die Quackos in jedem
Tick wieder zur Quackos. Das Original schreibt es an dieser Stelle ebenso.

Sie lässt sich nicht loswerden. Ihre drei Methoden sind im Original mit „prank'd" kommentiert:
der Tod wird auf dem Server verweigert, die Gesundheit springt bei jedem Setzen auf das Maximum
zurück, und fiele sie doch unter die Welt, setzt sie sich selbst wieder auf Höhe 256.

Der einzige Ausweg sind **Erbsen**. Ein Rechtsklick lässt jede Quackos im Umkreis von fünfzig
Blöcken in einer Wolke aus hundertfünfzig Teilchen verschwinden; sie hinterlässt drei goldene
Eier. Warum ausgerechnet Erbsen, sagt das Original nicht.

In `ContaminationUtil` stand die Zeile `immuneEntities.add(EntityQuackos.class)` seit Runden
auskommentiert da. Jetzt steht sie.

### Zwei Rufgegenstände, die es längst hätten geben können

`EntitySpawnerItem` trug drei auskommentierte Zweige: Kopter, Wurm, UFO. Der Wurm steht seit
Runde 282, das UFO seit Runde 283 — **beide Rufgegenstände sind jetzt da**, samt dem
Hinweistext des Wurms, der ebenfalls als Kommentar dastand:

> Without a player in survival mode to target, he struggles around a lot.
> He's doing his best so please show him some consideration.

Der Kopterruf bleibt aus, und der Grund steht jetzt im Kommentar statt in einem
auskommentierten Block: den Jagdkopter gibt es im Port nicht.

Das UFO braucht beim Rufen seine hundert Ticks Anlauf — es kommt fünfunddreißig Blöcke hoch
herein und soll erst einmal ankommen. Dafür hat `Ufo` jetzt `anlaufZeit(int)`; das Feld selbst
bleibt privat.

**44 Tore grün.**

## CI-Fix 479 — Ein Wort zu wenig, und das 45. Tor

`Quackos.mobInteract` stand als `protected` da, weil `Mob` es so deklariert. `Animal` hebt es
auf `public`, und damit ist die Überschreibung unzulässig:

> error: attempting to assign weaker access privileges; was public

**Kein Tor konnte das sehen** — `syntax-check` übersetzt ohne Minecraft und kennt die
Oberklasse nicht.

Das 45. Tor, `access-check`, prüft dieselbe Frage nach dem Verfahren von `signature-check`:
die richtige Sichtbarkeit steht vielfach im Projekt selbst, und wer als einziger enger schreibt
als die Mehrheit, ist es, der falsch ist.

**Diesen einen Fall fängt es nicht, und das ist gemessen:** `mobInteract` steht im ganzen Port
genau einmal. Gegen eine Mehrheit von eins lässt sich nichts prüfen — dieselbe Grenze steht
seit Runde 181 im Kopf von `signature-check`. Was es fängt, ist die nächste Stelle dieser Art
bei einem häufigen Namen; gegengeprobt: setzt man das `render` einer einzigen Oberfläche auf
`protected`, meldet es genau diese Zeile gegen 149 andere, und sonst nichts.

Die erste Fassung meldete vier Stellen, die **alle richtig waren**: eine Schnittstelle hat
keine Sichtbarkeitswörter, und ein wiederholter Regex fing nur das letzte Wort (`public final`
wurde zu `final`). Beides berichtigt, danach null Funde über den ganzen Baum.

## Runde 289 — Was die Bosse in die Welt bringt

Vier Bosse stehen seit den Runden 280 bis 283 im Port bereit — und **nichts brachte sie in die
Welt**. Man kam nur mit Befehlen oder den Rufgegenständen aus Runde 288 an sie heran. Der Teil
des Originals, der sie von selbst schickt, fehlte ganz.

### Drei Regeln, und jede hat eine Bedingung, die der Spieler selbst herstellt

**Der Maskenmann** kommt nach zwanzig Minuten, in denen der Spieler ununterbrochen drei Blöcke
unter der Oberfläche war, mindestens fünfzig Rad im Blut hatte und schon einmal einen
Kristallisator gebaut oder gesetzt hat. Eine Minute vorher kommt eine Warnung; reißt eine der
drei Bedingungen ab, fängt die Uhr von vorn an.

**Die Strahlenbiester** kommen nach einer Kernschmelze. ZIRNOX und Forschungsreaktor setzen
jedem Spieler im Umkreis von hundert Blöcken eine Marke; alle neunzig Minuten wird gewürfelt,
und wer die Marke trägt, bekommt zehn von ihnen vor die Tür gesetzt — das erste ist der
Anführer. Danach ist die Marke verbraucht.

**Das Gespenst** kommt zu dem, der Digamma im Blut hat: alle zwanzig Takte eine Chance von eins
zu fünf, fünfundsiebzig Blöcke entfernt. Es tut nichts, man kann ihm nichts anhaben — und es
verschwindet, sobald irgendein Spieler näher als fünfzig Blöcke kommt. Man sieht es nur von
weitem stehen und findet nichts mehr, wenn man hingeht.

### Eine Einstellung, die nie gelesen wird

Das Original führt `elementalAttackDistance` und macht sie konfigurierbar — liest sie aber
**nie**: der Strahlenbiest-Zweig nimmt `raidAttackDistance`, die Einstellung der FBI-Razzia.
Beide stehen auf 32, darum fällt es nie auf. Der Port führt nur die eine Zahl, die wirklich
zählt.

### Nicht übernommen

Die FBI-Razzia. Sie braucht `EntityFBI` und `EntityFBIDrone`, und die gibt es im Port nicht;
mit ihnen kommt sie nach. Das steht jetzt als Grund im Kopf des Systems statt gar nicht.

**45 Tore grün.**

## CI-Fix 480 — Zwei Fehler, ein neues Tor

Lauf 480 blieb an zwei Stellen aus Runde 289 hängen, und beide gehören zu Klassen, die kein
Tor sah.

**Erstens der Zeichner des Gespenstes.** Er setzte den durchscheinenden Zeichentyp so, wie man
es auf 1.7.10 täte: durch Überschreiben von `Model.renderType`.

> error: renderType(ResourceLocation) in DurchscheinendesModell cannot override
> renderType(ResourceLocation) in Model — overridden method is final

Auf 1.21 ist diese Methode endgültig; der Zeichentyp wird entweder im Konstruktor des Modells
übergeben oder — wie jetzt hier — am Zeichner über `getRenderType` bestimmt. Dass es
überhaupt einen durchscheinenden Typ braucht, ist nachgemessen und nicht geraten: `ghost.png`
hat 88 Bildpunkte mit Alpha 112 neben 1372 undurchsichtigen. Ein Ausschnitt-Typ würde genau
diese 88 hart machen, und das Original blendet sie weich ein.

**Zweitens ein Name aus dem falschen Jahrzehnt.** `MobSpawnSystem` fragte die Strahlung mit
`ContaminationUtil.getRads(player)` ab — so heißt sie im Original von 1.7.10. Im Port las bis
jetzt nur `HbmLivingAttachments.getRadiation` diesen Wert, `getRads` gab es nicht. Statt die
Aufrufstelle umzubiegen, ist jetzt die Methode selbst nachgereicht: sie steht im Original in
`ContaminationUtil` und bündelt dort zwei Dinge, die im Port getrennt liegen — das Lesen des
Wertes und die Prüfung auf Strahlenimmunität. Getrennt gemessen: `EntityEffectHandler` fragt
`isRadImmune` an zwei Stellen von Hand ab, neben dem Lesen. Wer `getRads` ruft, bekommt beides
in einem, so wie im Original.

### Das 46. Tor: `projmeth-check`

Der zweite Fehler ist der lehrreichere, denn **Empfänger und Methode liegen beide im Port**.
Was hier fehlt, fehlt wirklich — dafür braucht es keinen Minecraft-Klassenpfad, nur den
eigenen Quelltext. Genau das prüft das neue Tor: jeder Aufruf `Projektklasse.methode(...)`
muss eine Methode nennen, die die Klasse oder eine ihrer **Projekt**-Oberklassen erklärt.

Drei Bedingungen halten es sauber, und die erste war teuer erkauft: über den nackten Namen
gemessen meldete es 62 Stellen, an denen der Empfänger gar nicht aus dem Port kam
(`Item.getId`, `Pair.of` — Namensgleichheit mit Minecraft). Erst als der Empfänger über die
Einfuhrzeilen der Datei aufgelöst wurde, blieb null übrig.

**Die Lücke ist benannt:** Klassen, die von einer Fremdklasse erben, bleiben außen vor — was
sie erben, steht in der Bibliothek. Gemessen sind das 176 der 11733 Aufrufstellen; die übrigen
11557 werden geprüft. Gegenprobe: nimmt man `getRads` wieder heraus, meldet das Tor genau
`MobSpawnSystem.java:119` und sonst nichts.

**46 Tore grün.**

## Runde 290 — Der Müll, den man nicht los wird

Im Original gibt es eine winzige Entität, `EntityItemWaste`, und sie tut genau **eine** Sache:
sie lässt sich nicht zerstören. Zwei Überschreibungen, beide mit fester Antwort — mehr steht
dort nicht. Der Port hatte sie nicht, und der Kommentar in `NuclearWasteItem` behauptete, sie
**leuchte und verstrahle ihre Umgebung**. Das steht nirgends im Original. Nachgemessen, im
Kommentar berichtigt und die Entität nachgereicht.

### Was dabei auffiel: eine Unterscheidung, die der Port eingeebnet hatte

Das Original hat drei Klassen, wo der Port eine hat:

| Original | erbt von | Folge |
|---|---|---|
| `ItemNuclearWaste` | `Item` | verfällt nie, unzerstörbar am Boden |
| `ItemWasteLong` | `ItemNuclearWaste` | ebenso |
| `ItemWasteShort` | **`Item`** | verfällt nach fünf Minuten wie alles andere |

Der Port gab **allen** Abfallsorten die unendliche Lebensdauer, auch dem kurzlebigen. Das ist
jetzt berichtigt: `NuclearWasteItem` trägt einen Wahrheitswert `persistent`, und die vier
kurzlebigen Gegenstände stehen auf `false`.

Drei weitere Gegenstände lagen ebenfalls falsch herum: `nuclear_waste`, `nuclear_waste_tiny`
und `nuclear_waste_vitrified` waren im Port **gewöhnliche Items** — im Original sind es
`ItemNuclearWaste`. Sie hatten also nicht einmal die unendliche Lebensdauer. Auch das steht
jetzt richtig.

`DepletedFuelItem` und `RBMKPelletItem` tragen die Regel nun ebenfalls; bei beiden stand seit
den Runden 32 und 135 ein „noch nicht portiert" im Kopf, das jetzt weg kann.

### Wo die Unzerstörbarkeit wirklich herkommt

Nicht aus der Entität allein. Die Entität fängt den Schaden ab, aber **dass der Müll liegen
bleibt, kommt von `getEntityLifespan` des Gegenstandes** — auf 1.21 genau wie auf 1.7.10. Das
ist im Kopf beider Klassen festgehalten, damit es niemand an der falschen Stelle sucht.

Nicht übernommen: `trinitite` und `nuclear_waste_vitrified_tiny` gibt es im Port noch gar
nicht; sie gehören zur offenen Gegenstandsliste, nicht hierher.

**46 Tore grün.**

## Runde 291 — Die Razzia

Seit Runde 289 steht im Kopf von `MobSpawnSystem`, dass die FBI-Razzia fehlt, weil ihre beiden
Entitäten nicht portiert sind. Eine davon ist jetzt da.

### Der Beamte, der nie schießt

`EntityFBI` trägt einen Revolver oder eine Spas-12, und `attackEntityWithRangedAttack` ist im
Original **leer** — kein einziger Befehl im Rumpf. Die Fernkampf-Aufgabe läuft trotzdem mit
Priorität 2, also **über** dem Nahkampf: sie lässt ihn auf fünfzehn Blöcke herangehen,
stehenbleiben und alle zwanzig bis fünfundzwanzig Takte nichts tun.

Der Port hat mit `FireGunGoal` durchaus eine Aufgabe, die eine Waffe abfeuert. Sie hier
einzusetzen wäre bequem — und wäre eine **Erfindung**. Die leere Methode steht darum leer im
Port, samt der Aufgabe, die sie ruft, und der Kopf der Klasse sagt warum.

Gefährlich ist er trotzdem, nur anders als man denkt: alle vierzig Takte schießt er einen
Strahl in eine zufällige Richtung, und trifft der eine Maschine aus seiner Liste — Presse,
Chemiefabrik, Kristallisator, Turbine, Zyklotron, RTG, Kisten, Türen —, ist sie weg. Und alles,
was in anderthalb Blöcken Umkreis am Boden liegt, brennt zehn Sekunden.

### Die Grabe-Aufgabe

`EntityAIBreaking` ist die zweite Hälfte: wer keinen Weg zum Spieler findet, gräbt sich einen.
Die Rechnung des Originals steht unverändert im Port — Härte durch drei, fünf Hundertstel
Fortschritt je Takt, alle fünf Takte Klopfen und Sprungbild. Dass Stein dabei **sofort**
fällt, ist keine Nachlässigkeit des Ports: die Ganzzahldivision macht aus 1,5 eine Null, und
eine Division durch null ergibt im Original Unendlich. Ausgeschrieben statt erschlichen, aber
dasselbe Ergebnis.

Das Abtasten der Umrisse hat der Port vereinfacht: das Original wandert mit einem Zähler über
Breite mal Breite mal Höhe Punkte, um bei großen Wesen alle Ecken zu erwischen, und hält im
eigenen Kommentar fest, dass Zweibeiner nur zwei davon brauchen. Der Port tastet Augen- und
Fußhöhe ab — dieselben zwei.

### Eine Marke, die nie gesetzt wird

Das Original prüft vor jeder Razzia eine Marke `fbiMark`, die eine Schonfrist von zwanzig
Minuten setzen soll. Geschrieben wird sie einzig in `markFBI` — und **`markFBI` wird im ganzen
Original nie gerufen**. Die Prüfung ist immer wahr. Der Port führt weder Marke noch Prüfung;
was er stattdessen hat, ist diese Zeile hier.

### Was noch fehlt

Die fünf Quadrokopter. `EntityFBIDrone` erbt von `EntityUFOBase`, einer Flugsteuerung mit
Sichtkegel und Höhenwahl, und der Port führt sein Ufo ohne Grundklasse — die müsste erst
herausgelöst werden. Deshalb steht in den Einstellungen auch **kein** `raidDrones`: eine Zahl,
die niemand liest, ist schlimmer als keine.

**46 Tore grün.**

## Runde 292 — Die Quadrokopter, und eine Namensfalle

Die Razzia war seit Runde 291 halb da: fünfzehn Beamte, aber keine Drohnen. Die fehlten, weil
`EntityFBIDrone` von `EntityUFOBase` erbt — einer Flugsteuerung, die der Port nicht hatte.

### Warum der Port sein UFO nicht darauf umstellt

Das Original hat `EntityUFO extends EntityUFOBase`. Der Port hat `Ufo` als eigenständige
Klasse, und das war keine Nachlässigkeit: die Werte, mit denen das UFO fliegt, sind **andere**
als die der Grundklasse — fünfunddreißig Blöcke Überschießen statt zehn bis zwanzig, die
Drehung nur in zwei von drei Fällen, Höhe plus zwanzig statt plus zwei. `UfoBase` kommt darum
als das dazu, was sie im Original ist: die Steuerung für **alle anderen** Scheiben. Das UFO
nachträglich darauf zu setzen wäre eine eigene Runde mit eigener Messung, keine Nebensache.

### Die Falle, die ein Port sich hier stellt

Im Original heißen die drei Lesestellen des Wegpunkts `getX()`, `getY()` und `getZ()`. Auf
1.7.10 ging das, denn die Position hieß `posX`. **Auf 1.21 sind `getX`/`getY`/`getZ` die
Position der Entität selbst.** Wer die Klasse Zeile für Zeile überträgt, bekommt Code, der
anstandslos übersetzt und etwas völlig anderes rechnet: `getX() - posX` wird zu
`getX() - getX()`, also null. Der Wegpunkt heißt im Port darum `getWegpunkt()`, und der Kopf
der Klasse warnt davor.

### Der Kopter

Fünfunddreißig Lebenspunkte, sieht hundert Blöcke weit, hält sich sieben bis zehn Blöcke über
seinem Ziel. Steht er genau darüber — weniger als fünf Blöcke in beiden waagerechten
Richtungen, mehr als drei Blöcke höher —, **lässt er eine Splittergranate fallen**: keine
geworfene, eine fallende. Das Original setzt sie auf seine eigene Stelle und gibt ihr keinen
Schwung. Danach sechzig Takte Pause.

Sein Aussehen ist ein OBJ-Modell mit einer Drehung, die **keine Zufallsdrehung ist**: der
Generator wird aus der Nummer der Entität gesät und einmal gezogen. Dieselbe Drohne steht
damit in jedem Bild gleich, zwei Drohnen aber verschieden. Ein gewöhnlicher Zufall ließe sie
flackern.

### Die Einstellung, die jetzt gelesen wird

`raidDrones` stand in Runde 291 bewusst **nicht** in der Konfiguration — eine Zahl, die niemand
liest, ist schlimmer als keine. Jetzt liest sie jemand, und jetzt steht sie da.

**46 Tore grün.**

## Runde 293 — Fischen mit Sprengstoff

`stick_dynamite_fishing` fehlte im Port, und mit ihm `EntityItemBuoyant` — eine Klasse von
einunddreißig Zeilen, die genau eine Sache kann: **im Wasser nicht sinken**.

### Der treibende Gegenstand

Steht unter ihm Wasser, bekommt er jeden Takt einen Schubs von 0,045 nach oben. Das Original
schaut dabei einen Sechzehntelblock unter sich und verlangt Wasser mit einem Metadatenwert
**unter acht** — acht und darüber ist auf 1.7.10 der senkrechte Strahl unter einer Quelle.
Auf 1.21 heißt dieselbe Unterscheidung `FALLING`.

### Der Knall, der schwächer ist als der der Stange

Der Fischerdynamit sprengt mit Stärke **drei**, einen Viertelblock über seiner Stelle, ohne
Feuer und **ohne Blockschaden** — die gewöhnliche Stange nimmt fünf und reißt Löcher. Danach
werden fünfzehn Punkte in einem Würfel von fünfzehn Blöcken Kante gezogen; wo Wasser steht,
treibt ein Fisch auf, mit einem Aufwärtsschwung von eins.

### Eine Abweichung, die aus einer Zahl folgt

Das Original zieht seine Beute mit `FishingHooks.getRandomFishable(rand, chance, 0, 100)`. Die
**hundert** ist die Anzahl Ticks, die eine Angel gebraucht hätte, und sie drückt in Forges
eigener Rechnung Schrott und Schätze auf nahezu null: es kommt praktisch nur Fisch heraus.
Diese Rechnung gibt es auf 1.21 nicht mehr, und die Tabelle `gameplay/fishing` würfelt Schrott
und Schätze mit. Der Port nimmt darum gleich `gameplay/fishing/fish` — die Untertabelle, die
genau das enthält, was das Original praktisch ausgibt. Das Werkzeug ist ein anderes, das
Ergebnis dasselbe.

### Keine Gefahrenmeldung

`stick_dynamite` steht im Gefahrensystem als Sprengstoff, der Fischerdynamit **nicht** —
nachgemessen: im Original steht dort nur die eine Zeile. Der Port reicht keine zweite nach.

**46 Tore grün.**

## Runde 294 — Ein Dünger, der nichts tat

`powder_fertilizer` steht seit Runde 56 im Port — als **gewöhnlicher Gegenstand ohne jede
Wirkung**. Im Original ist er `ItemFertilizer`, und ein Klick düngt den ganzen Würfel von drei
mal drei mal drei Blöcken um die angeklickte Stelle.

Der angeklickte Block wird dabei **erzwungen**: bei ihm entfällt der Wurf, der sonst
entscheidet, ob Knochenmehl anschlägt. Die sechsundzwanzig Nachbarn müssen würfeln wie immer.
Verbraucht wird höchstens einer, egal wie viele Pflanzen angeschlagen haben.

### Zwei Dinge, die der Port nicht mitnimmt

`useFertillizer` — die zweite öffentliche Methode — bedient im Original den Werfer. **Der Port
hat kein Werferverhalten**, keine einzige Stelle registriert eines; eine Methode ohne Aufrufer
wäre toter Code. Kommt das Werferverhalten, kommt sie mit.

Das `BonemealEvent` fragt im Original andere Mods, ob sie das Düngen übernehmen wollen. Auf
1.21 trägt NeoForge dieses Ereignis an anderer Stelle, und der Port würde hier entweder doppelt
fragen oder eine fremde Rechnung nachbauen. Was wachsen darf, entscheidet die Pflanze selbst.

### Und einer, den er ändern muss

Das Original gibt aus `onItemUse` **immer false** zurück, auch wenn etwas gewachsen ist — auf
1.7.10 hat das nur zur Folge, dass die Hand nicht ausschlägt. Auf 1.21 entscheidet der
Rückgabewert über Handschlag **und** darüber, ob der Klick als erledigt gilt. Der Port gibt
darum `sidedSuccess` zurück, wenn etwas angeschlagen hat.

### Was offen bleibt, und warum

**Herstellbar ist er noch nicht.** Das Original kennt zwei Rezepte, beide vier Stück:
Calciumstaub oder irgendeine Asche, dazu roter Phosphor, Salpeter und Schwefel. Alle vier
Stäube kommen dort aus dem Mats-System (`CA.dust()`, `P_RED.dust()`, …). Im Port stehen die
Materialien in `Mats` mit `setAutogen(DUST)` bereit, aber **keine einzige Stelle außerhalb von
`Mats` greift auf einen dieser Stäube zu** — nachgemessen. Das Rezept gehört damit in die
Runde, die das Autogen-System erschließt, nicht hierher; eine Zutat zu erfinden, die es so
nicht gibt, wäre schlimmer als ein Gegenstand, den man vorerst nur im Schöpfermodus bekommt.

Nebenbei ist damit die erste Hälfte dessen da, was die Taube braucht: sie düngt im Flug, wenn
sie fett ist, und ruft dafür genau diese Methode.

**46 Tore grün.**

## Runde 295 — Die Taube

Sechs Klassen für einen Vogel: die Taube selbst, die Schnittstelle `IFlyingCreature` und vier
Aufgaben. Sie steht seit Runde 228 auf der Liste, und sie war der einzige Grund, warum diese
Schnittstelle fehlte.

### Zwei Zustände, und ein Zufall für jeden

Abgehoben wird bei Angriff, bei Feuer oder aus Laune — eins zu sechshundert je Takt, im Mittel
alle dreißig Sekunden. Gelandet wird **allein** aus Laune, eins zu zweihundert. Im Flug steigt
sie mit einem Rauschen um 0,04 pro Takt, bis sie zehn Blöcke über dem Boden ist, läuft mit
anderthalb vorwärts und dreht alle zwanzig Takte ein Stück.

Drei Aufgaben — Schwimmen, Umherziehen, Brotfressen — laufen **nur am Boden**. Das Original
hängt dafür ein `Predicate` an eigens abgeschriebene Fassungen von `EntityAIWander` und
`EntityAISwimming`; auf 1.21 genügt es, `RandomStrollGoal` und `FloatGoal` zu erben und die
Frage zu erweitern. Der Wurf ist derselbe: eins zu hundertzwanzig, und genau das ist auch die
Voreinstellung von `RandomStrollGoal`.

### Brot macht fett, und fett macht Dünger

Sie sucht Brot am Boden, zehn Blöcke weit, läuft hin und frisst. Näher als einen Block würfelt
sie eins zu drei; trifft der Wurf, ist der Gegenstand weg und der Rest des Stapels bleibt
liegen. **Fett wird sie bei jedem Versuch**, auch wenn der Wurf danebengeht — so steht es im
Original.

Und fett bleibt sie, bis sie im Flug düngt: eins zu fünfzig je Takt sucht sie sich
fünfundzwanzig Blöcke unter sich die erste Pflanze, die Knochenmehl annimmt, und lässt etwas
fallen. Das ist **erzwungenes** Düngen, ohne den üblichen Wurf — und es ruft genau die Methode,
die Runde 294 dafür freigelegt hat. Danach wird sie mit eins zu zehn wieder dünn.

### Zwei Merkwürdigkeiten, unverändert übernommen

Der Ton des Fressens hat im Original die Lautstärke `0.5F + 0.5F * rand.nextInt(2)` — eine
**ganzzahlige** Wahl zwischen halb und ganz, wo man eine gleitende erwarten würde.

Und der Flügelschlag ist keiner: das Modell setzt `rotateAngleZ = ageInTicks`, also den
fortlaufenden Zähler selbst. Die Flügel **wirbeln durch**, statt zu schwingen. Der Zähler kommt
dabei nicht von der Lebenszeit, sondern aus `handleRotationFloat`, das die Taube mit ihrem
eigenen `fallTime` überschreibt — er steigt, solange sie in der Luft ist, und steht still, wenn
sie sitzt.

### Ein Modell mit zwei Rümpfen

Es gibt den Rumpf zweimal: schlank und um einen Bildpunkt aufgeblasen. Im Original hängen die
**gleichen zwei Flügel** an beiden — derselbe `ModelRenderer` wird zweimal als Kind
eingehängt. Auf 1.21 gehört ein gebackenes Teil zu genau einem Elternteil; hier gibt es deshalb
zwei Flügelpaare, und gezeichnet wird das Paar des Rumpfes, der gerade dran ist. Sichtbar ist
kein Unterschied, weil das Original ohnehin nie beide Rümpfe zeichnet.

### Zwei Schläge lassen sie platzen

Wer ihr auf einmal doppelt so viel Schaden zufügt, wie sie Leben hat, bekommt keine Leiche,
sondern **zehn Federn in alle Richtungen**.

**46 Tore grün.**

## Runde 296 — Die Puppe und die Blockspinne

Zwei kleine Wesen, die im Original nur über das Spawn-Ei in die Welt kommen und trotzdem
fehlten.

### Die Puppe

Sie hat keine Aufgaben, läuft nicht weg und schlägt nicht zurück. Sie steht da und **sagt an,
wieviel Leben sie noch hat**: ihr Name ist `Leben / Höchstleben`, auf ein Zehntel gerundet,
und er wird immer angezeigt. Ein Rechtsklick mit einem Rüstungsteil legt es ihr an — das
Original rechnet dafür `4 - armorType`, auf 1.21 nennt das Teil seinen Platz selbst.

Fallen lässt sie **nichts**, auch nicht die Rüstung, die sie trägt: das Original überschreibt
`dropEquipment` leer, und eine Beutetabelle hat sie nie gehabt.

### Die Blockspinne

Ein Block auf acht Beinen. Was sie trägt, entscheidet, wie zäh sie ist: ihre Lebensenergie ist
der **Sprengwiderstand** des Blocks, mindestens aber eins. Obsidian macht sie also zu einem
Brocken, Wolle zu nichts.

Das Original führt Blocknummer und Metawert in zwei DataWatcher-Feldern. Auf 1.21 gibt es
weder das eine noch das andere — ein Blockzustand trägt beides zusammen, und `Block.getId` /
`Block.stateById` bilden ihn auf eine Zahl ab, die sich übertragen lässt.

Ihre acht Beine laufen **gegenläufig**: die ungeraden schwenken in die eine Richtung und heben
sich dabei, die geraden in die andere und senken sich — fünf Tausendstel je Grad. Darüber
zeichnet der Zeichner den getragenen Block; das Original nimmt dafür den Gegenstandszeichner,
auf 1.21 tut es der Blockzeichner unmittelbar.

Eine Kleinigkeit am Rande: das Original ruft `getExplosionResistance(null)` — mit einer
Entität, die es nicht gibt. Auf 1.21 steht der Wert am Block selbst und braucht kein
Gegenüber.

**46 Tore grün.**

## Runde 297 — Die Made

Neunundvierzig Zeilen, und zwei davon sind auf 1.21 nicht mehr übersetzbar.

Die Made hat acht Leben, doppeltes Lauftempo und zwei Schaden, und sie greift jeden Spieler
an, den sie sechzehn Blöcke weit sieht — **bei jedem Licht**. Sie kommt nicht von selbst in
die Welt; im Original kriecht sie aus dem Glyphid, und der ist noch nicht portiert.

### Zwei Stellen, die es nicht mehr gibt

`getCreatureAttribute` sagte im Original, dass sie ein **Gliederfüßer** ist — davon hängt ab,
ob das Mal der Gliederfüßer gegen sie wirkt. Auf 1.21 gibt es diese Methode nicht mehr; die
Frage entscheidet der Tag `sensitive_to_bane_of_arthropods`, und der steht jetzt im
Tag-Erzeuger.

`isValidLightLevel` gab im Original **immer wahr** zurück, damit sie auch im Hellen erscheinen
kann. Sie gehört zu `getCanSpawnHere` — und die Made erscheint nirgends von selbst. Eine Regel
für eine Erscheinung, die es nicht gibt, wäre toter Code; der Port lässt sie weg und sagt
warum.

### Und eine, die der Port dazutun muss

Ein `MeleeAttackGoal`. Das Original sucht sein Ziel in `findPlayerToAttack` und läuft in
`EntityMob.attackEntity` von selbst hin; in 1.21 gibt es beides nicht mehr, und ohne diese
Aufgabe stünde die Made nur da — dieselbe Stelle wie beim Untoten Soldaten in Runde 232.

Beim Sterben kippt sie ganz um: `getDeathMaxRotation` gibt im Original hundertachtzig Grad
statt der üblichen neunzig.

**46 Tore grün.**

## CI-Fix 486 — Ein Punkt zu viel

Ein einziger Fehler, und es war genau die Stelle, die ich beim Schreiben als riskant notiert
hatte:

> EatBreadGoal.java:100: error: cannot find symbol — method value(), location: variable
> GENERIC_EAT of type SoundEvent

`SoundEvents.GENERIC_EAT` ist auf 1.21.1 ein **`SoundEvent`**, kein `Holder`. Andere Felder
derselben Klasse sind sehr wohl Holder — die über `registerForHolder` angelegten —, und genau
deshalb steht im `api-check` jetzt **nur dieser eine Name** und keine Regel über die ganze
Klasse: was gemessen ist, ist dieser eine Fall.

Gegenprobe: mit wieder eingesetztem `.value()` meldet das Tor genau diese Zeile, ohne es
schweigt es.

**Der Rest der beiden Runden ist durchgelaufen** — die Taube mit ihren vier Aufgaben, das
Modell mit den zwei Rümpfen, die Puppe und die Blockspinne standen alle im selben Lauf und
hatten keinen einzigen Fehler.

## Runde 298 — Die Glyphiden, erster Teil: die Grundform

Die größte offene Entitätengruppe des Ports: zehn Klassen, rund zweitausend Zeilen. Diese
Runde bringt das Fundament — die Grundform, ihre Wertetabelle, den Merkpunkt und das Graben.

### Ein Untier mit Aufgaben

Ein Glyphid ist kein Zombie. Er hat ein **Zuhause**, das er sich beim ersten Takt merkt, eine
**Aufgabe** aus sieben möglichen, und einen **Draht zu seinen Artgenossen**: was er tut, gibt
er per `communicate` an alle im Umkreis weiter. Aus einzelnen Untieren wird so ein Zug.

Der Merkpunkt ist dabei der Zettel, auf dem die Aufgabe steht — eine unsichtbare Entität, die
zwei Minuten lebt. Wer sie erreicht, bekommt ihre Aufgabe, und sie verschwindet. Sie kann
**einen zweiten nach sich ziehen**: so entsteht „geh nach Hause, hol Verstärkung, komm
zurück".

### Fünf Bits Panzer

Die Panzerung ist ein Bitmuster: fünf Bits, fünf Stücke. Jeder Treffer kann eines absprengen —
die Wahrscheinlichkeit ist (Schaden mal 0,6) **zum Quadrat** in Prozent, bei zehn Schaden also
sechsunddreißig. Was noch dran ist, zählt für Schadensschwelle und Widerstand, beide gewichtet
mit Stücken durch fünf. Und man **sieht** es: der Zeichner lässt jedes abgeschlagene Stück weg.

Welcher Schaden durchkommt, ist fein abgestuft und Zahl für Zahl übernommen — Atomsprengungen
zerreißen ihn, Teilchenstrahlen kümmern ihn kaum.

### Nur eine Werte-Tabelle, und das ist gemessen

Das Original führt zwei Tabellen, `GlyphidStats70K` und `GlyphidStatsNT`, und stellt beide in
statische Felder. Aber `getStats()` gibt **immer** die NT-Tabelle zurück, und keine andere
Stelle im ganzen Original nennt die 70K-Tabelle je wieder. Der Port führt nur die eine, die
etwas entscheidet. Ebenso fehlen `divisor` und `damageThreshold`: beide sind im Original mit
`@Deprecated` gekennzeichnet, und die NT-Tabelle liest sie nicht.

### Der Graber

Steht etwas im Weg, sprengt er es weg — mit einer eigenen Zuteilung, die anders rechnet als
die gewöhnliche: die verbraucht Kraft an jedem Block und bleibt stehen, wenn sie alle ist;
der Graber läuft eine **feste Strecke** und bricht nur ab, wenn ein Block härter ist als
seine Obergrenze. Größere Glyphiden reißen größere Löcher.

### Was nicht mitkommt, und warum

* **Der eigene Wegfinder** des Originals (`PathFinderUtils`, der Teilwege annimmt) fehlt im
  Port; hier läuft die gewöhnliche Navigation. Der Unterschied fällt dort auf, wo kein
  vollständiger Weg existiert — und genau dann greift das Graben.
* **Das Zerschlagen der Laternen**, wenn er geblendet ist: den Block `lantern` gibt es im Port
  nicht. Was bleibt, ist die Flucht.
* **Die Sonderabfrage auf den Glyphidenbau** im Graber: auch diesen Block gibt es noch nicht.
* **Der zweite Zeichendurchgang** für die verseuchte Haut. Auf 1.21 wäre das eine eigene Lage,
  und die braucht einen Modelltyp, den ein OBJ-Zeichner nicht hat. Der Port zeichnet die
  verseuchte Haut stattdessen **als** Haut.

### Eine Zahl, die man nicht glauben mag

Das Tempo des Glyphiden steht in der Tabelle als **eins**. Ein Spieler hat 0,1, ein Zombie
0,23 — der Glyphid ist damit um ein Vielfaches schneller als alles Gewohnte. Beim Schreiben
hatte ich das für einen Maßstabsunterschied zwischen 1.7.10 und 1.21 gehalten und
stillschweigend geviertelt. **Ist es nicht:** beide Fassungen rechnen auf derselben Skala, und
die Blockspinne aus Runde 296 trägt dieselbe Eins. Zurückgenommen — wer hier teilt, macht ein
anderes Spiel daraus.

### Eine Berichtigung nebenbei

`glyphid_meat` stand im Port mit fünf Sättigungspunkten und Faktor null. Weder das Original
noch die CE-Abspaltung geben das her: **beide sagen drei Punkte und Faktor ein halb.**
Berichtigt. Dazu kommt das gebratene Stück, das bisher ganz fehlte — acht Punkte und Stärke II
für neun Sekunden, und der Ofen macht es aus dem rohen.

**46 Tore grün.**

---

## Runde 299 — Die Glyphiden, zweiter Teil: die Werfer

Drei Untiere und ein Wurfkörper. Der **Brawler** springt, der **Bombardier** wirft, der
**Blaster** wirft mehr — und die Säurebombe ist das, was dabei fliegt.

### Ballistik statt Zielhilfe

Der Bombardier rät nicht, er rechnet. Alle zwanzig Takte merkt er sich, wo sein Ziel stand;
die Differenz zum Jetzt ist seine Schätzung der Zielgeschwindigkeit. Damit sagt er voraus, wo
das Ziel in zwanzig (nahes Ziel) oder sechzig Takten (fernes) sein wird, und löst dann die
Wurfparabel nach dem Abwurfwinkel auf. Die Wurzel hat zwei Lösungen: die **flache Bahn** für
nahe Ziele, die **steile** für ferne — daher der Vorzeichenschalter. Ist die Wurzel negativ,
liegt das Ziel außer Reichweite, und er wirft gar nicht erst.

Die Schwerkraft in dieser Rechnung ist 0,04. Genau dieselbe Zahl trägt die Säurebombe als
`getGravityVelocity`, und ihr Luftwiderstand ist 1,0 statt der üblichen 0,99 — sie wird also
überhaupt nicht gebremst. **Die drei Zahlen müssen zusammenpassen**, sonst geht jeder Wurf
daneben; das ist der Grund, warum die Bombe eine eigene Klasse und nicht eine Granate mit
anderem Zünder ist.

Aus einer Salve wird ein Fächer, weil die i-te Bombe die Streuung `i × getSpreadMult`
bekommt: die erste fliegt genau, die letzte am weitesten daneben. Der Bombardier wirft fünf
mit Faktor 1, der Blaster zehn mit Faktor 0,5 — enger gestreut, aber dreimal so viel Schaden
je Klecks.

Dabei ist die Streuung selbst gebaut und nicht von `Projectile.shoot` geliehen: Vanille streut
**dreieckig mit 0,0172275**, das Original **gaußisch mit 0,0075**. Über eine Zehnerserie mit
wachsendem Faktor wäre das mehr als der doppelte Fächer.

### Der Glyphid als Geschoss

Der Brawler benutzt dieselbe Rechnung für sich selbst — das Original sagt dazu trocken „yeag
this is now a motherfucking projectile". Nur sind die Zahlen andere: v0 ist 1,5 statt 1, die
Schwerkraft in der Rechnung 0,01 statt 0,04, und der gefundene Winkel wird **durch 3,5
geteilt**, bevor er die Richtung dreht. Das ist keine saubere Ballistik mehr: die Rechnung
liefert den Winkel für einen hohen Bogen, und ein Siebtel Neigung daraus macht einen flachen
Satz nach vorn.

Damit ihm der eigene Sprung nichts tut, nimmt er **Fallschaden bis zehn** nicht an — ein Sturz
aus großer Höhe aber schon. Nicht Unverwundbarkeit, eine Grenze.

### Toter Code, der nicht mitkommt

Der Brawler des Originals führt dieselbe Vorhersage wie der Bombardier — und sie ist
**wirkungslos**, gleich zweifach: `lastX/lastY/lastZ` werden in jedem Takt auf den Ort des
Ziels gesetzt und im selben Takt gelesen, die Differenz ist also immer null; und das Feld
`lastTarget` wird nie belegt, weshalb die Rechnung sie ohnehin verwerfen würde. Nachgemessen,
nicht vermutet. Der Port baut die Felder deshalb nicht nach und schreibt stattdessen hin,
warum. Beim Bombardier, der nur alle zwanzig Takte misst, ist dieselbe Vorhersage echt.

### Zwei Löcher aus Runde 298 gestopft

Der Glyphid ist im Original ein **Gliederfüßer** (`getCreatureAttribute`). Auf 1.21 trägt das
ein Entity-Type-Tag, und in Runde 298 war es übersehen worden — das Schwert der Gliederfüßer
tat ihm nichts. Jetzt trägt es die ganze Familie.

Und die Wesen hatten keine Namen. Gemessen: von 104 angemeldeten Entitätsarten trugen **16**
einen Namen. Nachgereicht sind die zwölf, für die das Original einen hat (Glyphid und seine
drei neuen Verwandten, FBI-Agent und -Drohne, Made, Taube, Quackos, Kugel, Rakete, Schrabnel),
dazu drei ohne Vorlage (Blockspinne, Attrappe, Geist), die sonst ihren rohen Schlüssel im
Todesbildschirm zeigen würden.

Dabei **sieben Berichtigungen**: der Port hatte sich „Cybercrab", „Teslacrab", „Taintcrab",
„Maskman", „Gold Creeper" zurechtgelegt, nannte das UFO schlicht „UFO" und das Rumpfteil des
Balls-O-Tron wie seinen Kopf. Original und CE-Abspaltung sagen übereinstimmend „Cyber Crab",
„Tesla Crab", „Taint Crab", „Mask Man", „Golden Creeper", **„Martian Invasion Ship"** und
**„Balls-O-Tron Segment"**.

### Was die Familie noch braucht

Behemoth und Brenda hängen an `glyphid_gland`, der Digger an `Library.getBlockPosInPath` und
einem Metawert von `Rubble`, Scout und Nuclear am Bau und am `glyphid_spawner`-Block. Alle
vier Abhängigkeiten sind gemessen und fehlen im Port.

**46 Tore grün.**

---

## Runde 300 — CI-Fix 489, der Digger, und ein Tor mehr

### Ein Name, der auf 1.21 nicht mehr frei ist

CI 489 war rot, drei Übersetzungsfehler, alle derselbe:

```
error: getScale() in Glyphid cannot override getScale() in LivingEntity
```

`LivingEntity.getScale()` liefert ein `float`. Der Glyphid brachte aus dem Original ein
`getScale()` mit, das ein `double` liefert — das ist kein Überschreiben, sondern ein Fehler.
Umbenannt in `getGlyphidScale()`.

**Warum Tor 34 das nicht sah:** `vanilla-name-check.sh` kennt eine Liste fest belegter
Vanilla-Methodennamen, und die enthielt genau einen Eintrag — `Entity.getType`, den Fall aus
Runde 188. Jetzt enthält sie auch `LivingEntity.getScale`. Dafür musste das Tor umgebaut
werden: eine Klasse steht unter **mehreren** Wurzeln (ein Monster ist ein `LivingEntity`
*und* ein `Entity`), und bisher blieb die Suche bei der ersten stehen — welche das war, hing
von der Reihenfolge im Wörterbuch ab. Gegenprobe: benennt man zurück, meldet das Tor alle
vier betroffenen Zeilen und sonst nichts.

### Der Digger — und vier Abhängigkeiten, die es längst gibt

Die Notiz aus Runde 299 sagte, der Digger hänge an `Library.getBlockPosInPath` und einem
Metawert von `Rubble`. **Nachgemessen stimmt davon nur die Hälfte:** `getBlockPosInPath` ist
sechs Zeilen und jetzt portiert; `setMetaBasedOnBlock` braucht der Port gar nicht, weil er
statt Block-plus-Metawert einen `Block` speichert (`Rubble.setBlock`). `DummyableBlock` und
`NtmBlocks.CONCRETE` waren ohnehin da. Der Digger war also nie blockiert.

Er schlägt alle sechs Sekunden auf den Boden: ein waagerechter Strahl von sechs Blöcken in
Blickrichtung, dann acht weitere, je ein Sechzehntel Bogenmaß weitergedreht und einen Block
tiefer. Alles darauf, dessen Sprengfestigkeit unter der von Beton liegt, wird herausgerissen
und dem Ziel entgegengeworfen — fünfzehn Schaden je Trümmerstück.

Auch bei ihm ist die Zielvorhersage des Originals **wirkungslos**, aus denselben zwei Gründen
wie beim Brawler. Nachgemessen, nicht nachgebaut.

### Die Streuung an einer Stelle

Drei Klassen hatten inzwischen dieselbe zehn Zeilen: Richtung normieren, gaußisch mit 0,0075
streuen, strecken. Jetzt steht das einmal als `BobMathUtil.throwableHeading`. Das ist kein
Schönheitsgriff: `ProjectileNT.getMovementToShoot` sieht genauso aus, **normiert aber nicht
und streut mit dem Pfeilwert 0,0172275**. Wer im Vorbeigehen das eine für das andere hält,
baut einen doppelt so breiten Fächer und eine um ein Fünftel zu schnelle Bombe.

### Ein vertauschtes `super`, und das 47. Tor

Beim Nachmessen der Digger-Abhängigkeiten fiel in `Rubble` auf:

```java
protected void addAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);   // <- LESEN im Speicherzweig
```

Das übersetzt sauber und fällt in keinem der 46 Tore auf. Es ist trotzdem ein Fehler mit
Wirkung: der Aufruf liest aus dem noch **leeren** Etikett, schreibt also nichts aus der
Oberklasse weg und setzt deren Felder obendrein zurück — bei einem `Projectile` ist das der
Werfer, der damit beim Speichern verlorengeht.

Die beiden Methoden sehen sich zum Verwechseln ähnlich, nehmen dasselbe `CompoundTag` und
stehen in jeder Entitätsklasse direkt untereinander. `tools/nbtsuper-check.sh` grenzt jeden
Rumpf über die Klammerbilanz ab und sucht darin den `super`-Aufruf der jeweils anderen
Methode. Gemessen: **96 Rümpfe, ein Fund** — genau dieser. Gegenprobe meldet ihn wieder.

**47 Tore grün.**

---

## Runde 301 — Die Drüse, die Kanne, und die beiden Schweren

Die Notiz sagte, Behemoth und Brenda hingen an `glyphid_gland`, und das an einem
unportierten Teilsystem. Nachgemessen ist das Teilsystem **76 Zeilen groß** — also portiert.

### Ein Behälter, den man wirft

`ItemDisperser` ist im Original eine Klasse für **zwei** Gegenstände: die Verteilerkanne,
die jede versprühbare Flüssigkeit nimmt und hergestellt wird, und die Glyphidendrüse, die
nur Schwefelsäure oder Pheromon enthält und erbeutet wird. Beide tragen ihre Flüssigkeit im
Metawert, beide fliegen beim Rechtsklick davon, beide platzen beim ersten Anstoßen und
lassen eine Wolke zurück — zehn Blöcke breit, fünf hoch, vier Sekunden. Was die Wolke tut,
steht nicht in der Kanne, sondern in den Merkmalen der Flüssigkeit.

Die Drüse fasst mit **viertausend Millibar doppelt so viel** wie die Kanne, und leer lässt
sie sich zu **zweitausend Millibar Biogas** verflüssigen — mit Abstand die ergiebigste Zeile
dieser Rezeptliste.

Eine Kleinigkeit, die das Original bewusst so hält: bei der Drüse steht der Flüssigkeitsname
**vorne** („Sulfuric Acid Gland"), bei der Kanne hinten („Disperser Canister: Sulfuric
Acid"). Über zwei Übersetzungsschlüssel übernommen.

### Der Behemoth speit sechs Sekunden am Stück

Alle sechs Sekunden holt er Luft, dann speit er sechs Sekunden lang — in **jedem** dieser
hundertzwanzig Takte eine Säurewolke. Währenddessen legt er sich selbst Langsamkeit VI auf
und hält seine Blickrichtung auf dem Wert des vorigen Takts fest. Ein Strahl, der sich nicht
mitdreht, ist ein Strahl, dem man ausweichen kann; das ist der Sinn der Zeile
`rotationYaw = prevRotationYaw`.

Dabei ein Fallstrick: der Port hat für die Chemikalienwolke einen Bauweg mit Düsenversatz
und Geschwindigkeit 1 — für den Chemiewerfer. Das Original gibt dem Behemoth
`EntityChemical(world, this, 0, 0, 0)`, und dieser Bauweg **wirft die drei Versätze weg** und
reicht nur an `EntityThrowable(world, thrower)` weiter: Augenhöhe, Geschwindigkeit **1,5**,
Streuung 1. Nachgelesen, nicht angenommen.

Sein Tod ist eine Säurewolke, seine Drüse fällt **immer** (das Original legt die Zeile vor
den Aufruf der Oberklasse, ohne `byPlayer`-Abfrage), und sein Panzer ist der zäheste der
Familie: Faktor 0,15 statt 0,6.

### Brenda stirbt nicht allein

Vierzehn Blöcke Pheromon, und **zwölf Glyphiden**, die im selben Augenblick schlüpfen und
auseinanderstieben. Wer sie im Nahkampf erlegt, steht in der Mitte. Ihr Panzer ist mit 0,12
noch zäher als der des Behemoth, und sie ist feuerfest — im Original setzt das ihr Bauweg,
auf 1.21 steht es am `EntityType`.

### Noch ein Loch aus Runde 298

Der Glyphid des Originals überschreibt `swingDuration()` auf **15** Takte. Vanille nimmt 6.
Runde 298 hatte das übersehen, der Kieferbiss lief also zweieinhalbmal zu schnell. Auf 1.21
heißt die Stelle `getCurrentSwingDuration()`; der Behemoth setzt sie auf 100 — er holt weit
aus.

### Was ein Tor abgefangen hat

`model-check` meldete zwei fehlende Texturen: `disperser_canister_empty.png` und
`glyphid_gland_empty.png`. Die gibt es nicht, und zwar **mit Absicht** — das Original gibt
dem leeren und dem vollen Behälter dieselbe Grundtextur (`setTextureName` steht bei beiden
auf demselben Namen). Statt Dateien zu erfinden, teilen sich die Modelle die Textur.

**47 Tore grün.**

---

## Runde 302 — Big Man Johnson

Auch hier stimmte die Notiz nicht: der Nuclear sollte an `volcanic_lava_block` hängen. Den
gibt es im Port seit langem als `NtmBlocks.VOLCANIC_LAVA`. **Er war nie blockiert.**

Er greift nicht an, er wird gebracht. Sein Tod ist eine Kernexplosion vom Radius
fünfundzwanzig, und davor liegen fünf Sekunden Piepsen: ab dem Tod zählt `deathTicks`, alle
zehn Takte ein Ping, bei neunzig bekommen seine Artgenossen Widerstand und Feuerschutz, bei
hundert geht er hoch.

Was zurückbleibt, hängt an seiner Unterart. Der gewöhnliche und der radioaktive reißen einen
Krater und lassen vulkanische Lava stehen; der **verseuchte** sprengt stattdessen fünfzehn
bis zwanzig Maden heraus und lässt die Blöcke in Ruhe.

### Ein Fehler des Originals, bewusst übernommen

Die Stelle, die seine Artgenossen schützen soll, zählt die Nachbarn im Umkreis von acht
Blöcken — und legt Widerstand und Feuerschutz dann **auf sich selbst**. Das
`addPotionEffect` im Original hat keinen Empfänger. Die Schleife entscheidet also nur, wie
oft er sich selbst stärkt, nicht wen er schützt.

Nicht geradegezogen, und das mit Absicht: wer das repariert, lässt die ganze Schar seinen
Knall überleben statt nur ihn selbst — der ohnehin stirbt. Das wäre ein anderes Spiel.
Vermerkt steht es an der Stelle.

### Er spricht nur mit Spähern

Die Grundform gibt ihre Aufgabe an jeden Artgenossen weiter, der Merkpunkte annimmt; er nur
an die, die spähen. Beim Sterben schickt er ihnen als erstes den Rückzugsbefehl — wer neben
einer Bombe steht, soll laufen. Im Port steht dieselbe Frage als `istSpaeher()` statt als
`instanceof EntityGlyphidScout`, was den Späher als Abhängigkeit erspart.

Und drei Aufgaben behandelt er eigen: am Ziel angekommen geht er in den Leerlauf, beim Bauen
ohne Angreifer bekommt er Eile IV, und beim **Umformen der Landschaft setzt er seine
Lebenspunkte auf null** — das ist bei ihm das Umformen.

**47 Tore grün.**

---

## Runde 303 — Der Bau, der Späher, und der Ruß als Regler

Die letzte offene Notiz: Späher und Nuclear hingen am `glyphid_base`-Block und an
`GlyphidHive`. Diesmal stimmte sie — beides fehlte wirklich. Jetzt ist es da, und damit ist
die Glyphiden-Familie vollständig: **neun von neun Klassen portiert.**

### Der Ruß ist der Regler

Das Gelege bringt alle zwei Minuten einen Schwarm hervor. Wie **groß** er ist und **was**
darin steckt, hängt beides am Ruß in der Luft:

```
Schwarmgröße        = Grundgröße × max(Faktor × Ruß/Rußschritt, 1), höchstens zehn
Chance je Art       = Grundwert + (Aufschlag − Aufschlag / max((Ruß+1)/3, 1))
```

Die zweite Formel liest sich sperrig, tut aber etwas Einfaches: bei null Ruß bleibt der
Grundwert stehen, mit steigendem Ruß wächst der Aufschlag auf seinen vollen Wert zu. Arten
mit **negativem** Grundwert kommen darum überhaupt erst ab einer gewissen Verschmutzung vor —
Brenda ab zwanzig, Big Man Johnson ab fünfzig. Wer sauber wirtschaftet, sieht nur gewöhnliche
Glyphiden; wer die Gegend zurußt, bekommt zehnköpfige Schwärme mit Kernwaffen darin.

Zwei Bremsen halten das im Rahmen: über fünfzig Glyphiden in der Welt, und es kommt nichts
mehr; stehen schon mehr als drei neben dem Gelege, wartet es — außer es ist ein radioaktives,
das kennt diese Bremse nicht.

### Der Späher baut den nächsten Bau — und überlebt es nicht

Er ist der Grund, warum ein Befall sich ausbreitet statt an einer Stelle zu bleiben. In fünf
Schritten: eine Stelle im Umkreis von fünfundvierzig Blöcken würfeln, prüfen (fester Boden,
noch kein Baufleisch, weit genug weg), Merkpunkt setzen und rufen, am Ziel acht Richtungen
absuchen — und dann **sprengt er sich selbst und hinterlässt den Bau.**

Auf Basalt baut er groß, wird dafür schneller, und der Merkpunkt bekommt Vorrang. Steht ein
Big Man Johnson neben ihm, schaltet er auf Umformen um: weiter weg suchen, mehr Abstand
halten.

Sein Biss vergiftet, und das ist Absicht — das Original nennt es wörtlich *"extreme measures
for anti-scout bullying"*. Wer den Späher jagt, soll es merken.

**Nicht übernommen:** der Rampant-Zweig, der ihn gezielt auf den Spielerstützpunkt zulaufen
lässt. Er hängt an `PollutionHandler.targetCoords`, das der Port nicht hat; ohne diese
Koordinate fällt das Original selbst in den einfachen Zweig zurück, und genau der steht hier.

### Zwei Eigenheiten, die man gerade ziehen möchte

Die Suche nach Nachbarbauten dreht um **360/16** Grad je Schritt und läuft acht Schritte —
also nur einen **halben** Kreis. Übernommen, wie es dasteht; wer auf 360/8 geht, prüft andere
Richtungen als das Original.

Und das Bauschema wird von **oben nach unten** gelesen (`schematicSmall[4 - j]`). Wer die
Schichten umdreht, baut ein anderes Nest.

### Was CI 491 gefunden hat

`LivingEntity.getCurrentSwingDuration()` ist auf 1.21.1 **privat** und lässt sich gar nicht
überschreiben. Runde 301 wollte darüber die 15 Takte des Originals setzen — zwei
Übersetzungsfehler, und diesmal ohne jeden Hinweis auf einen Rückgabetyp. Jetzt steht dort,
was das Original auch tut: `updateSwingTime` selbst neu geschrieben, Zeile für Zeile wie die
Vorlage, nur mit der eigenen Dauer.

Das Tor bekam dafür eine **zweite Liste**: Namen, die in der Wurzel privat sind und deshalb
mit *jedem* Rückgabetyp ein Fehler wären. Gegenprobe meldet genau die Stelle.

### Und was zwei Tore abgefangen haben

`claim-check` meldete einen Satz in `BlockAllocatorGlyphidDig`, der behauptete, es gebe
`glyphid_spawner` im Port nicht — seit dieser Runde gibt es ihn. Mit dem Block kam die
Sonderabfrage des Originals: **vor dem Gelege bricht der Grabstrahl immer ab**, wie hart es
auch sei. Sonst sprengte ein grabender Glyphid seine eigene Brut weg.

`tab-check` meldete sieben neue Einträge ohne Kreativreiter. Eingetragen.

**47 Tore grün.**

---

## Runde 304 — Der Bau in die Welt gesetzt

Runde 303 hat den Bau gebaut, aber nirgends hingestellt. **Ohne diese Runde wäre die ganze
Familie im Überlebensspiel unerreichbar gewesen:** der Späher baut neue Nester, aber nur
ausgehend von einem, das schon steht. Das erste muss die Welt mitbringen.

Im Original steht das als zwölf Zeilen mitten in `HbmWorldGen` (Z. 191–202). Auf 1.21 sind
daraus vier Teile geworden — Feature, konfiguriertes Feature, platziertes Feature und
Biom-Änderer —, aber die Zahlen sind dieselben: **ein Bau je 256 Chunks im Mittel**, jeder
**zehnte verseucht**, und Beute gibt es immer.

Der Unterschied zu den Nestern, die der Späher hinterlässt: dort steht `beute = false`. Das
hat einen Grund — ein Nest, das gerade erst entstanden ist, hatte noch niemanden, der es
hätte füllen können.

Die Höhensuche ist Schleife für Schleife übernommen: vom Oberflächenwert drei Blöcke hoch
und einen tief, von oben nach unten, bis der Block darunter fest ist. Findet sich keiner,
entsteht nichts.

Eine Entscheidung, die das Original nicht treffen musste: der Bau steht an der **Oberfläche**,
kommt also in `SURFACE_STRUCTURES` und nicht in `UNDERGROUND_DECORATION` wie die Landmine.

Damit ist **die Glyphiden-Familie vollständig**: neun Klassen, der Bau, das Gelege, der
Schwarm, die Beute — und ein Weg, auf dem all das in einer neuen Welt zu finden ist.

**47 Tore grün.**

---

## Runde 305 — CI-Fix 493: ein Fehler ohne Fehlermeldung, und das 48. Tor

CI 493 war rot, und das Protokoll enthielt **keine einzige Ausnahme**. Der Server blieb
zwanzig Minuten lang bei

```
[worldgen/INFO] Preparing spawn area: 34%
```

stehen und lief in die Zeitgrenze. Kein Absturz, kein Hinweis, nichts, woran man sich
festhalten könnte.

### Was da passiert ist

Ein Feature läuft auf dem **worldgen-Faden** und bekommt einen `WorldGenLevel` — der ist
absichtlich eingeschränkt und schreibt nur in die Chunks, die gerade entstehen. Mein
`GlyphidHiveFeature` holte sich davon mit `getLevel()` den **echten ServerLevel** und
schrieb darauf. Das löst Chunk-Ladungen aus, die ihrerseits auf den worldgen-Faden warten.
Ein Deadlock, der sich als Stillstand tarnt.

Der Beweis ist sauber: Runde 303 (CI 492) war **ohne** die Weltgenerierung grün, Runde 304
**mit** ihr hängt. Dazwischen liegt genau diese Datei.

`GlyphidHive.generateSmall` nimmt jetzt einen `LevelAccessor` statt eines `Level` — das
deckt beide Fälle ab, den Weltgenerator und den Späher zur Laufzeit. `LootGenerator.applyLoot`
ebenso; es brauchte ohnehin nur `getBlockEntity` und `getRandom`.

Dazu eine zweite, kleinere Stelle derselben Art: der Schädel in der Kammer wurde mit
**Blockflagge 3** gesetzt, wie im Original. Die 3 benachrichtigt die Nachbarn, und das kann
während der Weltgenerierung ebenfalls Chunks nachladen. Jetzt 2 — der Blockinhalt entsteht
auch damit.

### Das 48. Tor

Ein Fehler, der zwanzig Minuten CI kostet und **keine Zeile Fehlermeldung** hinterlässt,
gehört gefangen, bevor er in CI kommt. `tools/worldgen-check.sh` durchsucht jede Klasse
unter `world/`, die von `Feature` erbt, nach `getLevel()` — außerhalb von Kommentaren, damit
die Warnung nicht selbst anschlägt.

Gemessen: **0 Funde bei 8 geprüften Features.** Setzt man den Aufruf aus CI 493 wieder ein,
meldet das Tor genau `GlyphidHiveFeature.java`.

Bemerkenswert daran: kein anderes der 47 Tore konnte das sehen. Es übersetzt sauber, es ist
kein Namensproblem, kein fehlender Verweis. Nur der Server-Test fällt darüber — und der
braucht zwanzig Minuten, um es zu merken.

**48 Tore grün.**

---

## Runde 306 — der Client startet nicht: rbmk_element.obj, und das 49. Tor

Ein echter Spielstart (ATLauncher, NeoForge 21.1.238) brach beim Laden ab:

```
ModelFormatException: Error parsing entry ('f 136/305/19 189/306/19 187/307/19', line 738)
in file 'hbmsntm:models/obj/machines/rbmk_element.obj'
- Invalid number of points for face (expected 4, found 3)
  at com.hbm.main.ResourceManager.init(ResourceManager.java:1100)
```

Die Modelle werden in `onClientSetup` geladen — eine verzögerte Aufgabe; wirft sie, startet
das Spiel gar nicht. **CI hat das nie gesehen**, weil der Rauchtest einen dedizierten Server
startet, und der lädt kein einziges Modell.

### Ursache

Die Datei ist byte-gleich mit dem Original. Ihre Gruppe `Cap` mischt Vier- und
**24 Dreiecke**. Das Original lädt sie darum mit `mixedMode = true`; der Port hatte das
zweite Argument weggelassen, und der Lader lehnte das erste Dreieck ab.

Das Flag allein hätte nicht gereicht: im Mischbetrieb bleibt der Modus der Gruppe ungesetzt,
und beide Hochladewege (`asVBO()` und `getRenderer()`) übergaben ihn ungeprüft an
`Tesselator.begin`. Das Original zeichnete solche Modelle Ecke für Ecke; ein Puffer kennt nur
eine Primitivart. Der neue gemeinsame Helfer `HFRWavefrontObject.baueGruppe` legt eine
gemischte Gruppe als Dreiecke ab und teilt jedes Viereck entlang der Diagonale 0–2. Reine
Gruppen bleiben unverändert. Die beiden bisher doppelt geschriebenen Hochladeschleifen
laufen jetzt durch denselben Helfer.

### Das 49. Tor

`tools/obj-check.sh` bildet die Regeln des Laders für **jeden** aktiven
`new HFRWavefrontObject("...")`-Aufruf nach: Zeilenmuster (etwa `1e-05`, das der Lader
ablehnt), Indizes, Drei- und Vierecke ohne `mixedMode`, und bei hochgeladenen Modellen
Flächen ohne Normale und Gruppen ohne Fläche. Auskommentierte Aufrufe zählen nicht.

Gemessen: **0 Funde bei 312 Ladeaufrufen**; nur eine Datei im ganzen Bestand mischt überhaupt.
Gegenproben: ohne das `true` meldet das Tor als ersten Fund **Zeile 738** — dieselbe Zeile
wie das Absturzprotokoll; der auskommentierte `shimmer_sledge`-Aufruf eingesetzt meldet eine
leere Gruppe; eine Ecke `1e-05`, eine Fläche ohne Normale und ein Index 99999 in `sphere.obj`
werden alle drei gemeldet.

Damit prüft CI zum ersten Mal etwas, das nur auf dem Client geschieht.

### Dieselbe Protokolldatei, eine zweite Zeile: das 50. Tor

```
Texture hbmsntm:block/geiger with size 55x55 limits mip level from 4 to 0
```

Kein Absturz, aber eine Wirkung auf **jeden Block im Spiel**: alles unter `textures/block/`
und `textures/item/` landet in einem gemeinsamen Atlas, dessen Mipmap-Stufe sich nach der
schlechtesten Textur richtet. Eine ungerade Seitenlänge setzt sie auf 0 — entfernte Blöcke
flimmern. Die Texturen stammen unverändert aus dem Original; 1.7.10 hatte Mipmaps
standardmäßig aus, darum fiel es dort nie auf.

Gemessen: sieben Texturen senkten die Stufe — `geiger` 55×55, `deco_computer` 66×66,
`deco_pole_top` 20×20, `rtg` 84×84, `deco_tape_recorder` 56×56, `nuke_little_boy` 184×112,
`ingot_nikonium` 200×200. Jede ist jetzt **ganzzahlig** vergrößert (Faktor 16, 8, 4, 4, 2, 2, 2),
jedes Pixel zu einem k×k-Block; ein Prüflauf hat jedes Zielpixel gegen sein Quellpixel
verglichen. Die Modelle rechnen UV-Koordinaten relativ zum Sprite (`GeometryBakeUtil`,
`lerp` zwischen `getU0`/`getU1`), das Bild bleibt also identisch.

`tools/mip-check.sh` prüft das künftig: **0 Funde bei 2879 Texturen**; mit dem alten
`geiger.png` meldet es genau diese Datei.

**50 Tore grün.**

---

## Runde 307 — der zweite Client-Absturz, und endlich ein Client in CI

Mit der neuen Jar kam der Spielstart an `rbmk_element.obj` vorbei und fiel an der nächsten
Stelle:

```
IllegalArgumentException: No model for layer hbmsntm:pole_satellite_receiver#main
  at EntityModelSet.bakeLayer
  at RenderPoleSatelliteReceiver.getRenderer(RenderPoleSatelliteReceiver.java:85)
  at ClientProxy.registerClientExtensions
```

`getRenderer()` läuft in `onClientSetup`. Dort hat `EntityModelSet` seine Schichten aber noch
**nicht** — die kommen erst mit dem Ressourcen-Neuladen danach. Der Kommentar an der Stelle
behauptete das Gegenteil. Jetzt wird die Schicht erst beim ersten Zeichnen gebacken, genau
wie es `GasMaskItem` schon immer tat. Alle anderen `bakeLayer`-Aufrufe außerhalb eines
Renderer-Kontexts sind gemessen: sie laufen erst beim Zeichnen.

### Der eigentliche Fehler: CI sah den Client nie

Zwei Abstürze hintereinander, beide nur beim Spieler zu sehen — und jeder weitere käme genauso,
einer pro Spielstart. Der Rauchtest in CI startete bisher nur einen **Server**, der kein
Modell, keine Textur und keinen Renderer lädt.

Neu in `build.yml`: **„Run client (startup smoke test)“**. Ein echter Client startet unter Xvfb
mit Mesa-Software-OpenGL und läuft über das Ressourcen-Neuladen hinaus. Geurteilt wird über das
Protokoll, mit genau den Zeilen, die die beiden echten Abstürze geschrieben haben
(`encountered an error in a deferred task`, `Crash report saved`, `broken mod state`, …).

Vorab gemessen, indem der Schritt lokal mit einem Ersatzprozess lief, der ein Protokoll Zeile
für Zeile ausgibt:

| Eingabe | Ergebnis |
|---|---|
| Protokoll des OBJ-Absturzes | rot |
| Protokoll des Schicht-Absturzes | rot |
| dasselbe ohne die Absturzzeilen | grün |
| dasselbe ohne Ende des Neuladens | rot (Zeitgrenze) |

Ob Xvfb und Software-OpenGL auf dem CI-Rechner tragen, zeigt erst der erste echte Lauf.

### Erster echter Lauf (CI 496): rot — aber nicht wegen unserer Mod

Der neue Schritt lief und scheiterte im Absturzbericht selbst:
`NoClassDefFoundError: net/caffeinemc/mods/sodium/api/vertex/serializer/VertexSerializer`
aus einem Iris-Mixin. `build.gradle` hatte Iris als `implementation` — damit lag Iris im
Entwicklungs-Client, ohne das Sodium, das es voraussetzt. Gemessen: der Code nutzt **keine**
Iris-Klasse, nur `ModList.isLoaded("iris")`. Die Abhängigkeit hatte also genau eine Wirkung,
und die war dieser Absturz. Sie ist entfernt.

---

## Runde 308 — schwarze Steuerstäbe: Licht aus dem falschen Block

Erster Blick ins Spiel (Bildschirmfoto): die Deckel der Steuerstäbe sind schwarz, die
Brennstoffkanäle von oben kaum zu sehen.

Ursache, im Kommentar des Steuerstab-Renderers sogar als „ABWEICHUNG“ festgehalten: gezeichnet
wurde mit dem Licht, das 1.21 dem Renderer mitgibt — dem des **Kernblocks**. Der ist
undurchsichtig; in ihm ist das Licht immer 0. Das Original liest die Helligkeit dagegen von Hand
**über dem Säulenkopf** (`yCoord + offset + 1`).

`RenderRBMKControlRod` und `RenderRBMKFuelChannel` überschreiben jetzt `getPacketLight` und
nehmen das Licht über dem Kopf — dasselbe Muster, das `RenderWatz` schon benutzt. Beim
Brennstoffkanal gilt es für Kappe, Innenrohr und Stabbündel gleichermaßen: sichtbar ist davon
nur, was man von oben durch den Kopf sieht.

Offen: ob „der Brennstoffkanal ist unsichtbar“ allein daher kam (eine Kappe mit Licht 0 über
einem Rohr ohne Deckfläche) oder eine zweite Ursache hat, zeigt erst der nächste Blick ins Spiel.

---

## Runde 309 — der Brennstoffkanal verschwindet beim Drehen, die Konsole ist verschwommen

Zwei neue Bildschirmfotos. Der Steuerstab-Deckel ist jetzt richtig beleuchtet (Runde 308 wirkt).
Zwei andere Fehler bleiben:

### Brennstoffkanal: je nach Blickwinkel unsichtbar

Blickwinkelabhängig heißt: der Renderer wird gar nicht erst aufgerufen. Minecraft 1.21 sammelt
Blockentitäten nur aus **sichtbaren Chunk-Abschnitten** ein. Beim RBMK sitzt die Blockentität im
Kernblock ganz unten in der Säule, mitten im undurchsichtigen Reaktor; gezeichnet wird bis zu
sechzehn Blöcke höher. Fällt der Abschnitt des Kernblocks aus der Sichtbarkeit, verschwindet
der ganze Kopf. Genau das hatte die Leviathan-Turbine in Runde 153/160 schon einmal.

`RenderRBMKFuelChannel` und `RenderRBMKControlRod` setzen jetzt `shouldRenderOffScreen`. Das
Offscreen-Tor kannte bisher nur Renderer, deren Vorlage `INFINITE_EXTENT_AABB` nimmt — beide
RBMK-Renderer tun das im Original nicht. **Zweite Regel:** ein Sichtkasten über mehr als 16
Blöcke liegt immer zum Teil in einem anderen Abschnitt und braucht die Methode ebenfalls.
Gemessen: genau diese zwei Renderer (je `y + 17`); ohne die Methode meldet das Tor genau sie.

### RBMK-Konsole: das ganze Menü verschwommen

`RBMKConsoleScreen.render` rief `renderBackground`, zeichnete die Konsole und rief dann
`super.render` — das in 1.21 **noch einmal** `renderBackground` aufruft, samt Weichzeichner, über
der fertigen Konsole. Jetzt überschreibt der Bildschirm `renderBackground` (abgedunkelt wie in
1.7.10, ohne Weichzeichner, darauf die Konsole) und ruft nur noch `super.render`.

Dasselbe Muster gemessen in allen 149 `render`-Methoden: drei Funde — die Konsole, die
Tontafel (ebenfalls ein `Screen`, dort lag der Hintergrund über der Tafel) und der Ziegelofen
(ein Container-Bildschirm; dort nur doppelt abgedunkelt). Alle drei behoben;
`tools/screen-check.sh` hält es fest (0 Funde, mit dem alten Aufruf genau die Konsole).
