# Portierungs-Leitfaden: 1.7.10 → 1.21.1 (NeoForge)

Dieser Leitfaden hält das Vorgehen fest, das beim Portieren des Elektroofens und des
Diesel-Generators entstanden ist. Beide sind über die CI gegen echtes
NeoForge 21.1.228 / Minecraft 1.21.1 gebaut worden; das Muster ist also erprobt.

**Grundregel:** Nichts erfinden. Für jede API-Frage gibt es im Port bereits eine
Maschine, die es vormacht. Erst ein strukturell ähnliches Vorbild suchen, dann dessen
API-Verwendung übernehmen. Das ist der einzige verlässliche Weg, ohne ständigen Build
korrekten Code zu schreiben.

## Vorbilder nach Maschinentyp

| Gesuchter Typ | Vorbild im Port |
| --- | --- |
| Einzelblock, Strom + Fortschritt + Upgrades | `MachineCentrifugeBlockEntity` |
| Einzelblock, Block-Klasse mit Blickrichtung | `MachineShredderBlock` |
| Multiblock | `MachineSolderingStationBlock` (`DummyableBlock`) |
| Stromerzeuger mit Fluid und Ein/Aus-Knopf | `MachineWoodBurnerBlockEntity` |
| Stromerzeuger mit Abgasen | `MachineDieselBlockEntity` |
| OBJ-Renderer + Item-Darstellung | `RenderArcWelder` |
| Menü mit Slots | `MachineCentrifugeMenu` |
| Oberfläche mit Tank, Energiebalken, Knopf | `MachineWoodBurnerScreen`, `MachineDieselScreen` |

## Die Berührungspunkte einer Maschine

Eine Maschine ist erst vollständig, wenn **alle** zutreffenden Punkte erledigt sind.
Diese Liste wurde durch Nachzählen an der Zentrifuge ermittelt und zweimal angewandt.

### Neue Klassen

| Datei | Zweck |
| --- | --- |
| `blockentity/machine/XBlockEntity.java` | Logik. Erbt von `MachineBaseBlockEntity` (oder `MachinePollutingBlockEntity`, wenn die Maschine Abgase erzeugt). |
| `blocks/machine/XBlock.java` | Block. `BaseEntityBlock` für Einzelblöcke, `DummyableBlock` für Multiblöcke. |
| `inventory/menus/XMenu.java` | Slots. Erbt von `MenuBase<XBlockEntity>`. |
| `inventory/screens/XScreen.java` | Oberfläche. Erbt von `InfoScreen<XMenu>`. |
| `render/blockentity/RenderX.java` | Nur wenn die Maschine ein OBJ-Modell hat. |

### Registrierungen

| Datei | Eintrag |
| --- | --- |
| `blocks/NtmBlocks.java` | `DeferredBlock<Block> X = register("x", () -> new XBlock(...))` |
| `blockentity/NtmBlockEntityTypes.java` | `BlockEntityType.Builder.of(XBlockEntity::new, NtmBlocks.X.get())` |
| `inventory/NtmMenuTypes.java` | `reg("x", XMenu::new)` |
| `main/CommonEvents.java` | `event.register(NtmMenuTypes.X.get(), XScreen::new)` in `registerScreens` |
| `main/ClientProxy.java` | `BlockEntityRenderers.register(...)` — nur bei eigenem Renderer |
| `inventory/NtmCreativeTabs.java` | `output.accept(NtmBlocks.X)` |
| `main/ResourceManager.java` | Texturkonstante, `IModelCustom`-Feld, `HFRWavefrontObject(...)` — nur bei OBJ |

### Datengeneratoren

| Datei | Eintrag |
| --- | --- |
| `datagen/NtmBlockStateProvider.java` | Sichtbarer Block: eigene `registerX()`-Methode. OBJ-gerenderter Block: `particleOnlyBlock(...)` (erzeugt zugleich das `builtin/entity`-Itemmodell). |
| `datagen/NtmBlockLootTableProvider.java` | `dropSelf(NtmBlocks.X.get())` |
| `datagen/NtmBlockTagProvider.java` | In die Spitzhacken-Liste aufnehmen, sonst lässt sich der Block nicht abbauen |
| `datagen/NtmLanguageProvider.java` | Blockname **und** `container.x` für den Fenstertitel |
| `datagen/NtmRecipeProvider.java` | Werkbank-Rezept |
| `inventory/recipes/AssemblyMachineRecipes.java` | Assembler-Rezept (`GenericRecipe`) — viele Maschinen werden im Original so gebaut, nicht an der Werkbank |

### Assets

Texturen, GUI-Grafiken und OBJ-Modelle aus dem 1.7.10-Original übernehmen. Pfade:

| Original (1.7.10) | Ziel (1.21.1) |
| --- | --- |
| `assets/hbm/textures/blocks/` | `assets/hbmsntm/textures/block/` (Einzahl!) |
| `assets/hbm/textures/gui/` | `assets/hbmsntm/textures/gui/` |
| `assets/hbm/models/machines/x.obj` | `assets/hbmsntm/models/obj/machines/x.obj` |
| `assets/hbm/textures/models/machines/` | `assets/hbmsntm/textures/models/machines/` |

## API-Umstellungen 1.7.10 → 1.21.1

| 1.7.10 | 1.21.1 im Port |
| --- | --- |
| `TileEntity` | `BlockEntity`; Paket `com.hbm.tileentity` → `com.hbm.blockentity` |
| Klassenname `TileEntityMachineX` | `MachineXBlockEntity` |
| `updateEntity()` | `updateEntity()` aus `ITickable`, angestoßen über `getTicker` im Block |
| `readFromNBT` / `writeToNBT` | `loadAdditional` / `saveAdditional`, je mit `HolderLookup.Provider` |
| `serialize(ByteBuf)` | `serialize(RegistryFriendlyByteBuf)` |
| `ItemStack[] slots`, `null` als leer | `NonNullList<ItemStack> slots`, `ItemStack.EMPTY` |
| `stack.stackSize` | `stack.getCount()`, `grow()`, `shrink()` |
| `itemStack.isItemEqual(other)` | `ItemStack.isSameItemSameComponents(a, b)` |
| Metadaten am Block | Blockstate-Eigenschaften (`FACING`, `LIT`, …) |
| Zwei Blöcke für an/aus | **ein** Block mit `BlockStateProperties.LIT` |
| `IIcon` / `registerBlockIcons` | Texturatlas + JSON-Modelle über die Datengeneratoren |
| `TileEntitySpecialRenderer` | `BlockEntityRenderer`, hier `BlockEntityRendererNT` |
| `Tessellator` / GL11 | `PoseStack` / `VertexConsumer`, gekapselt in `RenderContext` |
| Item-Darstellung im Inventar | `BlockEntityWithoutLevelRenderer` über `IBEWLRProvider` |
| `Container` / `GuiContainer` | `AbstractContainerMenu` / `AbstractContainerScreen` |
| `IGUIProvider` | `MenuProvider.createMenu` |
| Eigene `IMessage`-Pakete | `CustomPacketPayload` + `StreamCodec`; für Knöpfe `CompoundTagControl` |
| `worldObj.isRemote` | `level.isClientSide` |
| `xCoord/yCoord/zCoord` | `getBlockPos()` |
| `ForgeDirection` | `Direction` |
| `worldObj.getTotalWorldTime()` | `level.getGameTime()` |
| `isBlockIndirectlyGettingPowered` | `level.hasNeighborSignal(pos)` |
| `FurnaceRecipes.smelting()` | `level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)` + `SingleRecipeInput` |
| Ore Dictionary | Tags, bzw. `ComparableStack` in den `GenericRecipe`-Systemen |
| Schadenswert als Untertyp | Data Components |
| NEI | JEI (`com/hbm/handler/jei`) |

## Fallstricke

- **Slot-Bereiche sind inklusiv.** `UpgradeManagerNT.checkSlots(slots, start, end)` nutzt
  intern `subList(start, end + 1)`. Für einen einzelnen Upgrade-Slot an Index 3 also
  `checkSlots(slots, 3, 3)`.
- **Blockausrichtung.** `getHorizontalDirection()` liefert die Blickrichtung des Spielers.
  Soll die Vorderseite zum Spieler zeigen, `.getOpposite()` verwenden.
- **Registrierungen sind leicht zu vergessen.** Fehlt der Block-Tag-Eintrag, lässt sich die
  Maschine nicht abbauen; fehlt `container.x`, steht ein roher Übersetzungsschlüssel im
  Fenstertitel. Nach dem Portieren jeden Punkt der Liste oben per `grep` gegenprüfen.
- **Noch fehlende Infrastruktur.** `IConfigurableMachine` (JSON-Konfiguration, im Original
  28 Maschinen) und die OpenComputers-Anbindung (57 Maschinen) gibt es im Port nicht.
  Kennwerte fest im Quelltext lassen und mit `// todo config` markieren — so hält es der
  übrige Port auch.
- **Eine Basisklasse, zwei Fassungen.** Portieren mehrere Durchgänge parallel, kann dieselbe
  neue Basisklasse doppelt entstehen — in Runde 5 lieferten zwei Gruppen ein
  `PylonBaseBlockEntity`, einmal als `CableBaseBlockEntity`-Ableitung mit
  `List<int[]> connected`, einmal als `LoadedBaseBlockEntity`-Ableitung mit
  `List<BlockPos> connected`. Beide Fassungen übersetzen für sich; gemischt fällt der Build
  mit neun Fehlern um. Bei doppelt gelieferten Dateien reicht es **nicht**, die öffentliche
  API grob abzugleichen: Oberklasse, Feldtypen und Rückgabetypen jeder abstrakten Methode
  einzeln vergleichen und die abhängigen Klassen entsprechend nachziehen.
- **Multiblock oder nicht?** Im Original ist `BlockDummyable` die Multiblock-Basis. Erbt der
  Originalblock davon nicht (die Anschlusskästen erben von `PylonBase extends BlockContainer`),
  darf der Port ihn auch nicht von `DummyableBlock` ableiten — sonst fehlen
  `getDimensions()`/`getOffset()`, und `findCore` sucht einen Kern, den es nie gibt.
- **Dummy-Blöcke haben im Original eine TileEntity.** `createNewTileEntity` staffelt dort nach
  Metadaten: `>= 12` der Kern, `>= 6` ein `TileEntityProxyCombo` mit den nötigen Fähigkeiten,
  darunter — für die reinen Dummy-Felder — meist immer noch ein
  `TileEntityProxyCombo().inventory()`. Der Port bildet `DUMMY` bisher durchweg auf `null` ab
  und verliert damit die Andockpunkte für Trichter und Rohre an diesen Feldern. Beim
  Portieren die dritte Zeile des Originals mitlesen, auch wenn sie im Port derzeit nicht
  umgesetzt wird (siehe Stufe 2 der Roadmap).
- **Eine Notiz ist kein Beweis.** Weggelassen-Meldungen aus einem Portierdurchgang landen
  schnell ungeprüft in der Roadmap und werden dort zur vermeintlichen Tatsache. Sechs solcher
  Notizen haben sich inzwischen als falsch erwiesen — von „`AnvilRecipes` fehlt" (die Klasse
  lag längst da) bis „der Port kennt keinen Feindraht" (`WIRE_RED_COPPER` **ist** der
  Feindraht). Vor jeder Entscheidung, die auf so einer Notiz aufbaut, die betreffende Stelle
  im Original **und** im Port noch einmal selbst aufschlagen.
- **Methodennamen, die auf 1.21 schon belegt sind.** Die Vorlage aus 1.7.10 benutzt einen
  Namen, den die Minecraft-Oberklasse inzwischen selbst führt — `EntityMist.getType()` etwa
  liefert dort den Fluidtyp, während `Entity.getType()` auf 1.21 den `EntityType` liefert. Der
  Build bricht dann mit „cannot override" ab. `override-check.sh` kennt dafür seit Runde 14
  eine kleine Liste der belegten Namen in `Entity`, `BlockEntity`, `Item` und `Block`; wo eine
  Vererbungskette bei einer Minecraft-Klasse endet, ist das die einzige Handhabe, denn die
  Minecraft-API steht dem Skript nicht zur Verfügung.

## Prüfen

```bash
tools/syntax-check.sh          # lokal, findet kaputte Edits
tools/asset-check.sh           # lokal, findet Verweise auf fehlende Texturen und Modelle
tools/override-check.sh        # lokal, findet Ableitungen, die nicht zur Oberklasse passen
tools/import-check.sh          # lokal, findet vergessene Importe auf eigene Klassen
git push                       # die CI baut gegen echtes NeoForge -- das ist der Beweis
```

**Was die lokalen Prüfungen nicht können.** `syntax-check.sh` übersetzt ohne Minecraft-
Klassenpfad; Meldungen wie `cannot find symbol` oder `incompatible types` entstehen dabei zu
Zehntausenden als Folgefehler und werden weggefiltert. Echte Typfehler, die dieselbe Form
haben, gehen darin unter — in Runde 5 meldete das Gate grün, während neun echte Fehler im
Baum standen. `override-check.sh` schließt einen Teil dieser Lücke (nicht implementierte
abstrakte Methoden und abweichende Rückgabetypen zwischen Projektklassen, in der Gegenprobe
5 der 9 Fehler), findet aber weder Sichtbarkeitskonflikte beim Überschreiben noch `@Override`
an Methoden ohne Supertyp noch falsche Argumenttypen. `import-check.sh` schließt eine weitere:
ein vergessener Import auf eine **eigene** Klasse erzeugt ebenfalls `cannot find symbol` und ging
darin unter — in Runde 8 kostete genau das einen CI-Fehlschlag, weil die Registrierungszeile für
`CrystallizerRecipes` eingetragen, der Import aber vergessen war. Geprüft werden nur
`com.hbm`/`api.hbm`-Typen an eindeutigen Stellen (`new X(`, `extends`, `implements`,
deklarierter Variablentyp); Minecraft-Typen kann das Werkzeug ohne Klassenpfad nicht beurteilen. **Grün heißt hier: keine bekannte
Abweichung — nicht: übersetzt.** Der Beweis bleibt die CI.

Details zum Syntax-Gate und warum es nötig ist: [`BUILDING.md`](BUILDING.md).
