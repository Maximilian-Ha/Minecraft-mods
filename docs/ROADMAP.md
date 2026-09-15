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
- [x] `steel_beam` und `stone_gneiss` nachgereicht — beide sind im Original schlichte Blöcke.
      Damit hat der Heliostatspiegel wieder sein Originalrezept (ihm fehlte bis jetzt eines),
      und die Steinmühle mahlt wieder Gneis (`rock.schist`).
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
