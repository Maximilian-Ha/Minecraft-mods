# Gap Analysis — Nuclear Reactors Subsystem

**Original:** `hbm-1710` (Minecraft 1.7.10 / Forge) — `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710`
**Port:** `hbm-neo` (Minecraft 1.21.1 / NeoForge) — `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo`
**Scope:** RBMK, PWR, ZIRNOX, research reactor, reactor control, WATZ, fusion (ITER/tokamak), ICF, nuclear pile (CP-1), breeding reactor, and the shared neutron-flux framework.

> Verification method: exhaustive `grep -ril` over both trees for every reactor keyword (`rbmk|zirnox|watz|pwr|icf|fusion|tokamak|torus|klystron|plasma|reactor|pile|graphite|neutron|breeder|breeding|moderator|absorber|outgasser|crane|meltdown`), not filename matching. The NEO rename convention (`TileEntityFoo` → `FooBlockEntity`, `com/hbm/tileentity` → `com/hbm/blockentity`) was accounted for. `hbm-neo` and `hbm-neo-full` were confirmed to contain an **identical** set of `.java` files (same HEAD `4320fb9 "the zirnox"`), so no reactor code is hiding in the other checkout.

---

## 1. Headline numbers

| Metric | Original (1.7.10) | NEO (1.21.1) |
|---|---|---|
| Java files in subsystem | **269** | **13** |
| Lines of Java in subsystem | **36,095** | **~1,252** |
| Reactor asset files (models/textures/sounds/GUI) | **559** | **42** |
| `.obj` models for reactors | 42 | 2 (`zirnox.obj`, `zirnox_destroyed.obj`) |
| Distinct reactor "machines" (user-facing) | 9 families | 1 family (ZIRNOX) |
| Registered blocks in subsystem | ~90 (`rbmk_*` 33, `pwr_*` 11, `fusion_*` 12, `icf*` 5, `watz*` 5, `pile*` 3 + graphite variants, `reactor_*` 2, debris 4) | 3 (`machine_zirnox`, `zirnox_destroyed`, `pwr_controller` — the last is a bare `new Block()` placeholder) |
| Registered block entities in subsystem | ~70 | 2 |
| RBMK fuel-rod item instances | 33 `ItemRBMKRod` + 64 `ItemRBMKPellet` entries | 0 |

For scale, the whole mod is 3,398 Java files in 1.7.10 vs 937 in NEO; reactors are ~8% of the original mod's file count and one of its two flagship feature sets.

### Per-family code volume in the original (some overlap between rows, e.g. fusion breeder)

| Family | Files | LOC |
|---|---|---|
| RBMK (incl. crane, debris, dials, neutron handler, GUIs, renderers, items) | 122 | 16,476 |
| Fusion / ITER / tokamak | 39 | 5,636 |
| Nuclear pile (CP-1) | 27 | 3,333 |
| ICF | 19 | 2,285 |
| PWR | 10 | 1,909 |
| Research reactor + reactor control | 11 | 1,728 |
| ZIRNOX | 10 | 1,674 |
| WATZ | 12 | 1,516 |
| Breeding (solid + fluid breeder, fusion breeder) | 14 | 1,394 |
| Neutron framework (shared) | 7 | 882 |

---

## 2. What NEO actually has (complete list — 13 files)

| NEO file | LOC | Notes |
|---|---|---|
| `src/main/java/com/hbm/blockentity/machine/ReactorZirnoxBlockEntity.java` | 387 | Faithful port of the core loop |
| `src/main/java/com/hbm/blockentity/machine/ZirnoxDestroyedBlockEntity.java` | 57 | **Radiation emission dropped** (see §3.1) |
| `src/main/java/com/hbm/blocks/machine/ReactorZirnoxBlock.java` | 85 | `DummyableBlock` + `MultiblockHandlerXR` |
| `src/main/java/com/hbm/blocks/machine/ZirnoxDestroyedBlock.java` | 91 | |
| `src/main/java/com/hbm/inventory/menus/ReactorZirnoxMenu.java` | 54 | |
| `src/main/java/com/hbm/inventory/screens/ReactorZirnoxScreen.java` | 116 | Gauges/tooltips ported |
| `src/main/java/com/hbm/render/blockentity/RenderZirnox.java` | 76 | BER + BEWLR item renderer (better than the 1.7.10 TESR) |
| `src/main/java/com/hbm/render/blockentity/RenderZirnoxDestroyed.java` | 44 | |
| `src/main/java/com/hbm/items/machine/ZirnoxRodItem.java` | 80 | Enum + NBT life; **tooltips dropped**, `getBarWidth` is buggy |
| `src/main/java/com/hbm/items/machine/BreedingRodItem.java` | 33 | Enum-only shell; **no machine consumes it** |
| `src/main/java/com/hbm/items/tools/DyatlovItem.java` | 40 | Zirnox-only debug tool (1.7.10 version also poked RBMK) |
| `src/main/java/com/hbm/particle/RBMKFlameParticle.java` | 107 | Used only by `ZirnoxDestroyedBlockEntity` |
| `src/main/java/com/hbm/particle/RBMKMushParticle.java` | 81 | Registered but nothing spawns it |
| `src/main/java/com/hbm/inventory/fluid/trait/FT_PWRModerator.java` | 41 | Trait only; `addInfo`/`addInfoHidden` are `//todo` stubs; **no PWR reads it** |

Plus assets: `models/obj/zirnox.obj`, `zirnox_destroyed.obj`, `textures/models/zirnox*.png`, 20 `rod_zirnox*` item textures, `textures/gui/reactors/gui_zirnox.png`, 5 `particle_no_sheet/rbmk_*.png`, `sounds/block/rbmk_explosion.ogg`, `rbmk_az5_cover.ogg`, `textures/block/pwr_casing_blank.png`, `pwr_controller.png`, `textures/models/fusion/plasma*.png` (textures with no code behind them).

`NtmBlocks.PWR_CONTROLLER` exists purely so the machine creative tab has an icon:
```java
// PWR
public static final DeferredBlock<Block> PWR_CONTROLLER = register("pwr_controller", () -> new Block(BlockBehaviour.Properties.of()));
```
It has no block entity, no model, no logic.

---

## 3. Per-family breakdown

### 3.1 ZIRNOX — **ported, ~85%** (the only family with any port)

Original: `tileentity/machine/TileEntityReactorZirnox.java` (693), `TileEntityZirnoxDestroyed.java` (106), `blocks/machine/ReactorZirnox.java`, `ZirnoxDestroyed.java`, `items/machine/ItemZirnoxRod.java` (130), `inventory/container/ContainerReactorZirnox.java`, `inventory/gui/GUIReactorZirnox.java` (123), `render/tileentity/RenderZirnox.java`, `RenderZirnoxDestroyed.java`, `entity/projectile/EntityZirnoxDebris.java` (160).

| Mechanic | 1.7.10 | NEO | Verdict |
|---|---|---|---|
| 24-slot rod grid + `getNeighbouringSlots` adjacency map | yes | identical | ported |
| Per-rod `decay()` with neighbour-count multiplier, `ZirnoxType` heat/maxLife, depleted-rod swap via `fuelMap` | yes | identical | ported |
| Water / CO₂ / superheated-steam tanks, `loadTank` from fluid-ID slots | yes | identical | ported |
| Pressure = `CO2fill*2 + heat*(CO2fill/max)`, steam curve `(heat-10256)/maxHeat * min(CO2/14000,1) * 25 * 7.5` | yes | identical | ported |
| Meltdown → replace with `zirnox_destroyed`, `MultiblockHandlerXR.fillSpace`, explosion r=12, `rbmk_explosion` sound | yes | yes | ported |
| **Meltdown debris** (`spawnDebris`, `zirnoxDebris()`, `EntityZirnoxDebris` with 5 `DebrisType` OBJ models: blank/concrete/element/exchanger/shrapnel) | yes | **absent** | **missing** |
| **`ExplosionNukeGeneric.waste(world, x, y, z, 35)`** — the 35-block fallout/corium field after meltdown | yes | **absent** | **missing** |
| **Destroyed-block radiation** — `radiate()` ray-cast, 500 000 RAD while burning / 75 000 after, fire damage within 5 blocks | yes | **absent** (only fire particles + sound remain) | **missing** |
| Achievement `achZIRNOXBoom` + `radMark` elemental flag on nearby players | yes | absent | missing (cosmetic/Elementals) |
| OpenComputers `SimpleComponent` (10 callbacks: getTemp/getPressure/getWater/getSteam/getCarbonDioxide/ventCarbonDioxide/getFuel/isActive/setActive/getInfo) | yes | absent | missing (OC does not exist on 1.21) |
| Redstone-over-Radio (`IRORValueProvider`, `IRORInteractive`) | yes | absent (API package exists in NEO, unused here) | missing |
| `IInfoProviderEC` (Energy Control display) | yes | absent | missing (dead mod) |
| Fuel-rod tooltips: depletion %, heat, `desc.item.zirnoxRod`/`zirnoxBreedingRod` | yes | **absent** | missing |
| Durability bar | `getDurabilityForDisplay` (double) | `getBarWidth` returns `getLifeTime(stack) / num.maxLife` — **integer division, always 0** until life ≥ maxLife | **bug** |
| GUI gauges, tank tooltips, on/off + vent buttons | yes | yes | ported |
| Tilt / `checkTilt(TiltType.CONFIG)` | yes | yes | ported |

Concrete bug to fix: `hbm-neo/src/main/java/com/hbm/items/machine/ZirnoxRodItem.java`
```java
@Override
public int getBarWidth(ItemStack stack) {
    ZirnoxType num = EnumUtil.grabEnumSafely(theEnum, MetaHelper.getMeta(stack));
    return getLifeTime(stack) / num.maxLife;   // int/int -> 0 for all realistic values; should be Math.round(13F * life / maxLife)
}
```
Same integer-division problem in `getBarColor`.

Also note `ZirnoxDestroyedBlockEntity` writes NBT key `"onFire"` but reads `"fire"` — the flame state does not survive a world reload. This is inherited verbatim from the 1.7.10 original (same asymmetry), so it is a faithful bug port rather than a regression, but it should be fixed while porting.

### 3.2 RBMK — **0% ported** (the largest single gap: 122 files, 16,476 LOC)

Nothing exists in NEO except two particle classes and two sound files. Missing in full:

**Tile entities (33 files, 7,936 LOC)** — `tileentity/machine/rbmk/`:
`TileEntityRBMKBase` (610), `TileEntityRBMKConsole` (830), `TileEntityRBMKRod` (580), `TileEntityCraneConsole` (481), `RBMKDials` (376), `TileEntityRBMKBoiler` (370), `TileEntityRBMKGraph` (328), `TileEntityRBMKGauge` (314), `TileEntityRBMKKeyPad` (308), `TileEntityRBMKOutgasser` (307), `TileEntityRBMKNumitron` (304), `TileEntityRBMKTerminal` (295), `TileEntityRBMKAutoloader` (292), `TileEntityRBMKLever` (284), `TileEntityRBMKIndicator` (282), `TileEntityRBMKHeater` (281), `TileEntityRBMKControl` (243), `TileEntityRBMKControlManual` (231), `TileEntityRBMKControlAuto` (188), `TileEntityRBMKCooler` (187), `TileEntityRBMKSlottedBase` (175), `TileEntityRBMKDisplay` (148), `TileEntityRBMKStorage` (103), `TileEntityRBMKOutlet` (91), `TileEntityRBMKInlet` (84), `TileEntityRBMKRodReaSim` (56), `IRBMKLoadable` (33), `TileEntityRBMKAbsorber`/`Moderator`/`Reflector` (30 each), `TileEntityRBMKBlank` (24), `TileEntityRBMKActiveBase` (21), `IRBMKFluxReceiver` (20).

**Blocks (34 files, 1,947 LOC)** — `blocks/machine/rbmk/`: `RBMKBase`, `RBMKPipedBase`, `RBMKMiniPanelBase`, `RBMKRod`, `RBMKRodReaSim`, `RBMKControl`, `RBMKControlAuto`, `RBMKBoiler`, `RBMKHeater`, `RBMKCooler`, `RBMKModerator`, `RBMKReflector`, `RBMKAbsorber`, `RBMKBlank`, `RBMKOutgasser`, `RBMKStorage`, `RBMKInlet`, `RBMKOutlet`, `RBMKLoader`, `RBMKAutoloader`, `RBMKConsole`, `RBMKCraneConsole`, `RBMKTerminal`, `RBMKGauge`, `RBMKGraph`, `RBMKIndicator`, `RBMKNumitron`, `RBMKLever`, `RBMKKeyPad`, `RBMKDisplay`, `RBMKDebris`, `RBMKDebrisBurning`, `RBMKDebrisRadiating`, `RBMKDebrisDigamma`.

**Mechanics that must be reimplemented, not just transcribed:**
- **Flux graph.** `handler/neutron/` (882 LOC): `NeutronNodeWorld` holds a per-world `HashMap<BlockPos, NeutronNode>` plus a stream queue; `RBMKNeutronHandler` (429 LOC) walks rays column-to-column applying moderator/absorber/reflector/control-rod attenuation and `NType` (SLOW/FAST/FUSION) conversion. `PileNeutronHandler` (119 LOC) reuses the same graph for the pile.
- **`RBMKDials`** — 24 tunable dials stored as **world game rules** (`dialPassiveCooling`, `dialColumnHeight`, `dialFluxRange`, `dialReasimBoilers`, `dialDisableMeltdowns`, `dialDisableXenon`, `dialModeratorEfficiency`, …) created at world load with `world.getGameRules().addGameRule(...)` and re-read each tick.
- **Fuel rods** — `ItemRBMKRod` (603 LOC): `EnumBurnFunc` (LOG_TEN, PLATEU, ARCH, SIGMOID, SQUARE_ROOT, LINEAR, QUADRATIC, EXPERIMENTAL), `EnumDepleteFunc` (LINEAR, RAISING_SLOPE, BOOSTED_SLOPE, GENTLE_SLOPE, STATIC), xenon-135 poison build-up/burn-off, separate core and hull heat, diffusion, per-stack NBT `yield`/`poison`/`coreHeat`/`hullHeat`. 33 rod instances + 64 pellet entries.
- **Meltdown** — `TileEntityRBMKBase.meltdown()` (137 LOC) does a flood-fill over `columns`, converts them to debris blocks, spawns `EntityRBMKDebris` (178 LOC, 6 debris types incl. `DIGAMMA`), emits `ParticleRBMKMush`, and produces a persistent radiating/burning debris field plus `HazardModifierRBMKHot` / `HazardModifierRBMKRadiation` item hazards.
- **Lid system** — `ItemRBMKLid` + `RBMKBase.hasOwnLid()` / `LID_NONE|LID_STANDARD|LID_GLASS` static render flags and `shouldSideBeRendered` overrides.
- **Control-computer UI** — 11 screen-only (container-less) GUIs totalling 2,243 LOC: `GUIRBMKConsole`, `GUIScreenRBMKTerminal`, `GUIScreenRBMKGraph`, `GUIScreenRBMKGauge`, `GUIScreenRBMKDisplay`, `GUIScreenRBMKIndicator`, `GUIScreenRBMKKeyPad`, `GUIScreenRBMKLever`, `GUIScreenPager` + container GUIs `GUIRBMKRod`, `GUIRBMKControl`, `GUIRBMKControlAuto`, `GUIRBMKHeater`, `GUIRBMKBoiler`, `GUIRBMKOutgasser`, `GUIRBMKStorage`, `GUIRBMKAutoloader`.
- **Crane** — `TileEntityCraneConsole` (481 LOC) drives a rendered gantry over the reactor grid with its own OBJ model (`crane.obj`, `crane_buffer.obj`, `crane_console.obj`) and inventory routing.
- **Renderers** — 17 files, 1,712 LOC across `render/tileentity/RenderRBMK*` and the `ISimpleBlockRenderingHandler`-style `render/block/RenderRBMKRod`, `RenderRBMKControl`, `RenderRBMKReflector`, `RenderPribris`.
- **NEI handlers** — `RBMKRodDisassemblyHandler`, `RBMKWasteDecayHandler`, `OutgasserHandler`, `FuelPoolHandler`.
- **Recipes** — `OutgasserRecipes`, `FuelPoolRecipes`, `crafting/handlers/RBMKFuelCraftingHandler` (rod re-fill/disassembly as an `IRecipe`), `RodRecipes`.
- **Config/diagnostics** — `TileEntityRBMKBase.diagnosticPrintHook` HUD overlay, `ILookOverlay`, `IToolable` screwdriver actions.

### 3.3 PWR — **0% ported** (10 files, 1,909 LOC)

`TileEntityPWRController` (708 LOC) scans a `BlockDummyable` shell for `pwr_fuel`, `pwr_heatex`, `pwr_channel`, `pwr_heatsink`, `pwr_neutron_source`, `pwr_reflector`, `pwr_port`, `pwr_control`, `pwr_casing`, `pwr_block` (11 registered blocks, only `pwr_controller` exists in NEO as an empty `Block`), then runs a flux/rod-level/core-heat/hull-heat model driven by `FT_PWRModerator` (present in NEO as a data holder only) and `FT_Heatable` (present in NEO) to boil coolant. Items `ItemPWRFuel` (`EnumPWRFuel`) and `ItemPWRPrinter` (a slice-printer with `GUIScreenSlicePrinter`) are absent; `ContainerPWR`, `GUIPWR`, `handler/nei/PWRRecipeHandler` absent.

The NEO tree does ship `textures/block/pwr_casing_blank.png` and `pwr_controller.png` — assets landed, code did not.

### 3.4 WATZ — **0% ported** (12 files, 1,516 LOC)

`TileEntityWatz` (674 LOC) is a vertically-segmented multiblock (segments every 3 blocks, discovered by walking down from the top) with shared coolant/hot-coolant/WATZ-fluid tanks, per-slot item locks, passive+reaction flux (`fluxLastBase`, `fluxLastReaction`), and a `watz_pump` redstone gate. `TileEntityWatzStruct`, `blocks/machine/Watz.java`, `WatzPump.java`, `BlockWatzStruct.java`, `ItemWatzPellet`, `ContainerWatz`, `GUIWatz`, `RenderWatz`, `RenderWatzMultiblock`, `RenderWatzPump`, `handler/nei/WatzRecipeHandler` — all absent. The `WATZ` **fluid** is registered in NEO (`Fluids.java:286`) and `textures/gui/fluids/watz.png` exists, but nothing produces or consumes it.

### 3.5 Research reactor + reactor control — **0% ported** (11 files, 1,728 LOC)

`TileEntityReactorResearch` (495 LOC): 12 fuel-plate slots, per-slot `slotFlux[12]`, `totalFlux`, control-rod `level`/`targetLevel` with `rodControl()`, water coolant byte, `ItemPlateFuel` → `waste_plate_*` conversion, meltdown at 50 000 heat. `TileEntityReactorControl` + `ItemReactorSensor` provide the remote gauge/control panel. `RenderSmallReactor` uses `reactor_small_base.obj` + `reactor_small_rods.obj`. Containers/GUIs `ContainerReactorResearch`, `GUIReactorResearch`, `ContainerReactorControl`, `GUIReactorControl` absent.

### 3.6 Breeding reactor — **item shell only, ~5%**

`TileEntityMachineReactorBreeding` (262 LOC) sits adjacent to a research reactor, sums its `totalFlux`, and advances `BreederRecipes` at `0.0025 * (flux / recipe.flux)` per tick. NEO has `BreedingRodItem` with the full 18-value `BreedingRodType` enum and registers `ROD`, `ROD_DUAL`, `ROD_QUAD` — but there is **no breeding reactor block entity, no `BreederRecipes`, no `FluidBreederRecipes`, no `ContainerMachineReactorBreeding`/`GUIMachineReactorBreeding`, no `RenderBreeder`**. The items are currently inert.

### 3.7 Fusion / ITER / tokamak — **0% ported** (39 files, 5,636 LOC)

`tileentity/machine/fusion/` (10 files, 2,965 LOC): `TileEntityFusionTorus` (629), `TileEntityFusionPlasmaForge`, `TileEntityFusionKlystron`, `TileEntityFusionKlystronCreative`, `TileEntityFusionMHDT`, `TileEntityFusionCollector`, `TileEntityFusionCoupler`, `TileEntityFusionBoiler`, `TileEntityFusionBreeder`, `IFusionPowerReceiver`. Plus `TileEntityFusionTorusStruct`, `blocks/machine/fusion/` (9 files, 875 LOC), `BlockFusionComponent`, `BlockFusionTorusStruct`, `FusionHatch`, `ContainerFusionTorus/Klystron/Breeder`, `GUIFusionTorus/Klystron/Breeder`, and 11 renderers (`RenderFusionTorus`, `RenderFusionTorusMultiblock`, `RenderFusionPlasmaForge`, …) driving 8 OBJ models. `PlasmaForgeRecipes` and `OutgasserRecipes` feed it. NEO ships `textures/models/fusion/plasma.png`, `plasma_glow.png`, `plasma_sparkle.png` — again assets without code.

### 3.8 ICF (inertial confinement fusion) — **0% ported** (19 files, 2,285 LOC)

`TileEntityICF` (387), `TileEntityICFController`, `TileEntityICFPress`, `TileEntityICFStruct`; blocks `MachineICF`, `MachineICFController`, `MachineICFPress`, `BlockICF`, `BlockICFComponent`, `BlockICFLaserComponent`, `BlockICFStruct`; `ItemICFPellet`; `ContainerICF`, `ContainerICFPress`, `GUIICF`, `GUIICFPress`; `RenderICF`, `RenderICFController`, `RenderICFMultiblock` + `icf.obj`.

### 3.9 Nuclear pile (CP-1) — **0% ported** (27 files, 3,333 LOC)

`tileentity/machine/pile/` (11 files, 1,688 LOC) — `TileEntityPileCore` (657) with `PileChannel` fuel/ventilation/control channel lists, player-driven `drillChannel()` boring, `runSimulation()`, `handleVentilation()`, `handleMeltdown()`, `recalculateSegments()`; plus `TileEntityPileBase`, `TileEntityPileBaseMK2`, `TileEntityPileFuel`, `TileEntityPileBreedingFuel`, `TileEntityPileControl`, `TileEntityPileSource`, `TileEntityPileVent`, `TileEntityPileLoader`, `TileEntityPileNeutronDetector`, `TileEntityPileDeviceBase`. Blocks: `blocks/machine/pile/` (13 files, 1,357 LOC — `BlockGraphite`, `BlockGraphiteDrilled`, `BlockGraphiteDrilledBase`, `BlockGraphiteDrilledTE`, `BlockGraphiteFuel`, `BlockGraphiteBreedingFuel`, `BlockGraphiteBreedingProduct`, `BlockGraphiteRod`, `BlockGraphiteSource`, `BlockGraphiteNeutronDetector`, `BlockPile`, `BlockPileBrick`, `BlockPileDevice`). Items `ItemPileRod`, `ItemPileRodMK2`. Renderers `RenderPileControl`, `RenderPileLoader`, `RenderPileVent` + 3 OBJs. Depends on `PileNeutronHandler`.

### 3.10 Shared neutron framework — **0% ported** (7 files, 882 LOC)

`handler/neutron/{NeutronHandler, NeutronNode, NeutronNodeWorld, NeutronStream, RBMKNeutronHandler, PileNeutronHandler, package-info}`. This is a hard prerequisite for RBMK **and** the pile, and the design assumptions (static per-world maps keyed by `BlockPos`, invalidation on `invalidate()`/`onChunkUnload()`) must be rebuilt against 1.21's `ServerLevel`/`SavedData`/chunk lifecycle.

---

## 4. Summary table

| Family | Original files / LOC | NEO status | Coverage |
|---|---|---|---|
| ZIRNOX | 10 / 1,674 | ported, mechanics trimmed | **~85%** |
| Breeding reactor | 14 / 1,394 | item enum only | **~5%** |
| PWR | 10 / 1,909 | placeholder block + unused fluid trait | **~2%** |
| WATZ | 12 / 1,516 | fluid registered only | **~1%** |
| RBMK | 122 / 16,476 | 2 particle classes, 2 sounds | **~1%** |
| Research reactor + control | 11 / 1,728 | none | **0%** |
| Fusion / ITER | 39 / 5,636 | 3 plasma textures | **0%** |
| ICF | 19 / 2,285 | none | **0%** |
| Nuclear pile | 27 / 3,333 | none | **0%** |
| Neutron framework | 7 / 882 | none | **0%** |
| **Total** | **269 / 36,095** | **13 files / ~1,252 LOC** | **~5%** |

**Honest functional coverage: 5%.** Weighting by LOC gives 3.9% (ZIRNOX is 4.6% of subsystem code and ~85% done); weighting by "number of reactor families a player can build" gives 11% (1 of 9) but flatters the result badly, because ZIRNOX is by a wide margin the simplest reactor in the mod — no flux graph, no multiblock scan, no control computer, no config dials. 5% is the defensible figure.

---

## 5. Architectural blockers for finishing the port

### 5.1 Blockers that are already solved in NEO (good news — reuse these)

| Need | NEO equivalent |
|---|---|
| `BlockDummyable` multiblock cores/dummies | `com/hbm/blocks/DummyableBlock` (`FACING` + `TYPE` blockstate properties, `findCore`) |
| `MultiblockHandlerXR.checkSpace/fillSpace` | `com/hbm/handler/MultiblockHandlerXR` (ported) |
| Fluid tanks + fluid net | `com/hbm/inventory/fluid/tank/FluidTank`, `api/hbm/fluidmk2/*` (`IFluidStandardTransceiverMK2`, `FluidNetMK2`) |
| Power net | `api/hbm/energymk2/*` (`PowerNetMK2`, `Nodespace`, `IEnergyProviderMK2`) |
| `FT_Heatable` steam chain | ported (134 LOC) |
| Byte-buf TE sync (`networkPackNT`, `IBufPacketReceiver`) | ported, now over `RegistryFriendlyByteBuf` |
| Container-less GUIs | `com/hbm/blockentity/IScreenProvider` |
| Radiation | `handler/radiation/ChunkRadiationManager`, `util/ContaminationUtil` |
| Nuke/fallout | `explosion/ExplosionNukeGeneric` etc. |
| Looping machine sound | `com/hbm/sound/AudioWrapper` |
| OBJ models + custom render state | `render/loader/HFRWavefrontObject`, `render/material/MaterialRenderState`, `BlockEntityRendererNT`, `IBEWLRProvider` |
| Damage-as-subtype items | `com/hbm/items/EnumMultiItem` + `com/hbm/inventory/MetaHelper` |
| Look overlay / screwdriver | `blocks/ILookOverlay`, `api/hbm/block/IToolable` |
| Redstone-over-Radio | `api/hbm/redstoneoverradio/*` (present, unused by reactors) |

### 5.2 Genuine blockers

1. **Neutron-flux framework has no 1.21 home.** `NeutronNodeWorld` keeps static per-`World` `HashMap`s of nodes and a stream queue mutated from `updateEntity`, `invalidate()`, and `onChunkUnload()`. On 1.21 these need to live on the `ServerLevel` (an attachment or `SavedData`), respect `ChunkEvent.Unload`/`setRemoved()`/`clearRemoved()` semantics, and be safe against the chunk-loading differences. This is a rewrite, not a translation, and it gates both RBMK and the pile.

2. **`RBMKDials` is built on 1.7.10 world game rules.** `world.getGameRules().addGameRule(key, value)` was free-form string rules; 1.21 requires `GameRules.register(name, category, GameRules.IntegerValue.create(...))` at mod-init with statically-typed `GameRules.Key` objects, and there is no "add rule at runtime" path. All 24 dials (many of them `double`, which vanilla game rules do **not** support — only boolean and int) must be re-homed, most likely onto a NeoForge config or a `SavedData`, with the `/gamerule`-style UX replaced. The `RBMKDials.getGameRule(world, key, isIteration)` call sites are threaded through nearly every RBMK tick method.

3. **Metadata blocks → blockstates, for ~90 blocks.** RBMK columns encode lid type and rotation partly through metadata and *static mutable render flags* (`RBMKBase.renderLid`, `overrideOnlyRenderSides`, `dropLids`, `digamma` are `public static` fields toggled during rendering). `shouldSideBeRendered(IBlockAccess, x,y,z, side)` is gone; on 1.21 lid/glass variants must become real blockstate properties (or a model with `BlockStateProperties`) and `skipRendering`/`BlockBehaviour.Properties`. The graphite pile blocks (`BlockGraphiteDrilled*`) likewise use metadata for drill direction.

4. **`IIcon` → texture atlas + JSON models.** Every RBMK/PWR/pile block registers `IIcon` fields in `registerBlockIcons(IIconRegister)` and picks per-side/per-meta icons in `getIcon(side, meta)`. On 1.21 that becomes blockstate JSON + model JSON (generated via the existing `NtmBlockStateProvider`/`NtmItemModelProvider` datagen), which means ~500 asset files to author or generate, not just copy. NEO's existing datagen providers make this tractable but it is real work.

5. **TESR → `BlockEntityRenderer`, Tessellator → `VertexConsumer`/`PoseStack`.** 32 reactor renderers use `Tessellator.instance`, `GL11` immediate mode, `glRotatef`/`glTranslatef`, and `ISimpleBlockRenderingHandler` (`RenderRBMKRod`, `RenderRBMKControl`, `RenderRBMKReflector`, `RenderPribris`) which has **no equivalent at all** on 1.21 — those must become baked models or full BERs. NEO's `RenderContext`/`BlockEntityRendererNT`/`MaterialRenderState` layer already exists, so the pattern is established (see `RenderZirnox`), but each of the 32 needs individual work, and the fluid/plasma effects (`RenderFusionTorus` plasma ring, `RenderICFMultiblock` laser beams) need shader/render-type equivalents.

6. **Container-less GUI network flow.** `IGUIProvider.provideGUI` returning a raw `GuiScreen` with no `Container` was legal on 1.7.10. On 1.21 an open-screen request must be a client-side reaction to a custom payload; the 11 RBMK screen GUIs (console, terminal, graph, gauge, indicator, keypad, lever, display, pager) each need a payload + client handler. NEO's `IScreenProvider` establishes the pattern but currently has few users.

7. **Custom packets → NeoForge payloads.** Reactors use `PacketDispatcher`/`AuxParticlePacketNT`/`PacketThreading.createAllAroundThreadedPacket`, plus `NBTControlPacket` for every button. Each becomes a `CustomPacketPayload` with a `StreamCodec` registered on `RegisterPayloadHandlersEvent`. NEO has `network/toserver/CompoundTagControl` and `particle/vanilla/NbtParticleOptions` as the replacements — so this is mechanical, but it touches every GUI and every particle emission.

8. **Item damage-as-subtype and NBT → data components.** `ItemRBMKRod` stores `yield`, `poison`, `coreHeat`, `hullHeat` in `stack.stackTagCompound`; `ItemRBMKPellet` uses 64 damage values; `ItemZirnoxRod`/`ItemPWRFuel`/`ItemWatzPellet`/`ItemICFPellet`/`ItemBreedingRod` are `ItemEnumMulti` damage-subtype items. NEO's `EnumMultiItem` + `MetaHelper` + `TagsUtil.getCustomData` shim keeps this working, but the RBMK rods' four floating-point stack fields really want `DataComponentType<Double>` registrations, and `RBMKFuelCraftingHandler` (a custom `IRecipe` that merges rod NBT during crafting) has to become a `CraftingRecipe` with a serializer.

9. **NEI → JEI.** Four reactor NEI handlers (`RBMKRodDisassemblyHandler`, `RBMKWasteDecayHandler`, `OutgasserHandler`, `FuelPoolHandler`) plus `PWRRecipeHandler`, `WatzRecipeHandler`, `ConstructionHandler` must be rewritten as `IRecipeCategory` implementations. NEO has a working `handler/jei/` with 15 categories to copy from, so the pattern is solved — but `RBMKWasteDecayHandler` renders a *decay chain over time*, which needs a bespoke category.

10. **Ore dictionary → tags.** `OreDictManager` entries for RBMK/WATZ fuels must become `TagKey<Item>` + datagen tag providers.

11. **`IFluidHandler` / fluid-slot conventions.** The reactors use HBM's own `FluidTank.loadTank(slots, in, out)` with fluid-identifier items rather than Forge's `IFluidHandler`. NEO already ported this (`FluidTank.loadTank(level, 24, 26, slots)` in `ReactorZirnoxBlockEntity`), so this is a non-blocker — but any place the original leaned on `net.minecraftforge.fluids.FluidStack` directly needs the `neoforge` capability API (`Capabilities.FluidHandler.BLOCK`) instead.

12. **Dead integrations.** OpenComputers (`li.cil.oc`, `@Optional.Interface`, `SimpleComponent`, `CompatHandler.OCComponent`), Energy Control (`IInfoProviderEC`), and microblocks (`MicroBlocksCompatHandler`) do not exist on 1.21. Every reactor implements 6–15 OC callbacks; those should be dropped or re-expressed through the existing `api/hbm/redstoneoverradio` interfaces. This *removes* work but changes behaviour, and the `@Optional.InterfaceList` annotations must be stripped from every class.

13. **Achievements → advancements.** `player.triggerAchievement(MainRegistry.achZIRNOXBoom)` and the RBMK achievements in `util/AchievementHandler` need JSON advancements via datagen.

14. **Explosion/world-edit APIs.** `ExplosionNukeGeneric.waste`, `EntityRBMKDebris`/`EntityZirnoxDebris` (falling-block-like projectiles with OBJ renders), and the debris block spread all use `world.setBlock(x,y,z,block,meta,flag)` and `EntityFallingBlock` semantics that changed. NEO's `entity/item/FallingBlockEntityNT` and `entity/projectile/DebrisBase`/`Rubble` are the hooks to build the debris entities on.

---

## 6. Effort estimate

Assumes one experienced modder who already knows both the 1.7.10 HBM codebase and 1.21.1 NeoForge, working inside this port where the fluid/energy/multiblock/render/particle scaffolding already exists.

| Work package | Person-days |
|---|---|
| Neutron framework rebuilt on `ServerLevel` + chunk lifecycle | 7 |
| `RBMKDials` re-homed off game rules (24 dials, doubles) + config UI | 2 |
| RBMK columns: 20 block entities + 34 blocks + blockstates/models + lids + heat/steam/flux loop | 24 |
| RBMK control-computer UIs: 17 GUIs (11 container-less) + payloads | 14 |
| RBMK fuel-rod item system: 33 rods, burn/deplete curves, xenon, data components, crafting handler | 9 |
| RBMK renderers (17 files incl. 4 `ISimpleBlockRenderingHandler` rewrites) + crane | 8 |
| RBMK meltdown: debris blocks, `EntityRBMKDebris`, particles, hazard modifiers, HUD diagnostics | 5 |
| PWR: controller sim + 11 blocks + fuel/printer items + GUI + JEI | 9 |
| WATZ: segmented multiblock + pump + pellets + 3 renderers + GUI | 6 |
| Research reactor + reactor control + sensor + breeding reactor + `BreederRecipes`/`FluidBreederRecipes` | 7 |
| Fusion/ITER: 10 BEs, 9 blocks, torus multiblock, plasma render, 3 GUIs, `PlasmaForgeRecipes` | 16 |
| ICF: 4 BEs, 7 blocks, laser components, press, pellets, 3 renderers, 2 GUIs | 8 |
| Nuclear pile: 11 BEs, 13 blocks, channel drilling, core sim, MK2 rods, 3 renderers | 11 |
| ZIRNOX completion: debris entities, waste field, destroyed-block radiation, RoR, tooltips, bar-width bug | 3 |
| Assets: ~520 files — blockstate/model/lang/loot datagen, OBJ + texture migration | 12 |
| Recipes + tags + JEI categories (`OutgasserRecipes`, `FuelPoolRecipes`, `RodRecipes`, 7 categories) | 6 |
| Integration, balance verification against 1.7.10, playtesting, bug-fixing | 13 |
| **Total** | **≈160** |

**Estimate: 160 person-days** (~8 months of one full-time developer, or ~4 months for two). RBMK alone is ~62 days of that. A useful intermediate milestone is "neutron framework + RBMK + ZIRNOX completion" at ~72 days, which restores the mod's signature feature.

---

## 7. Recommended porting order

1. **Neutron framework** — unblocks RBMK and the pile; nothing else can start without it.
2. **ZIRNOX completion** (3 days) — cheap, closes the one family that is nearly done, and forces the debris-entity + waste-field infrastructure that RBMK will reuse.
3. **Research reactor → reactor control → breeding reactor** — small, self-contained, exercises the flux-sum pattern and `BreederRecipes`.
4. **PWR** — medium multiblock, reuses `FT_PWRModerator`/`FT_Heatable` already in the tree.
5. **RBMK** — the long pole. Suggested internal order: dials → `RBMKBase`/blank/moderator/reflector/absorber → rod + fuel items → control rods → boiler/heater/cooler/inlet/outlet → storage/outgasser/autoloader/loader → console/terminal/gauge/graph/indicator/numitron/lever/keypad/display → crane → meltdown/debris.
6. **Nuclear pile** — reuses the neutron graph; independent of RBMK otherwise.
7. **WATZ**, then **ICF**, then **fusion/ITER** — the late-game tiers, largest renderer burden, least blocking.

---

## 8. Appendix — full original file list (269 files)

### `tileentity/machine/rbmk/` (33)
`IRBMKFluxReceiver`, `IRBMKLoadable`, `RBMKDials`, `TileEntityCraneConsole`, `TileEntityRBMKAbsorber`, `TileEntityRBMKActiveBase`, `TileEntityRBMKAutoloader`, `TileEntityRBMKBase`, `TileEntityRBMKBlank`, `TileEntityRBMKBoiler`, `TileEntityRBMKConsole`, `TileEntityRBMKControl`, `TileEntityRBMKControlAuto`, `TileEntityRBMKControlManual`, `TileEntityRBMKCooler`, `TileEntityRBMKDisplay`, `TileEntityRBMKGauge`, `TileEntityRBMKGraph`, `TileEntityRBMKHeater`, `TileEntityRBMKIndicator`, `TileEntityRBMKInlet`, `TileEntityRBMKKeyPad`, `TileEntityRBMKLever`, `TileEntityRBMKModerator`, `TileEntityRBMKNumitron`, `TileEntityRBMKOutgasser`, `TileEntityRBMKOutlet`, `TileEntityRBMKReflector`, `TileEntityRBMKRod`, `TileEntityRBMKRodReaSim`, `TileEntityRBMKSlottedBase`, `TileEntityRBMKStorage`, `TileEntityRBMKTerminal`

### `tileentity/machine/pile/` (11)
`TileEntityPileBase`, `TileEntityPileBaseMK2`, `TileEntityPileBreedingFuel`, `TileEntityPileControl`, `TileEntityPileCore`, `TileEntityPileDeviceBase`, `TileEntityPileFuel`, `TileEntityPileLoader`, `TileEntityPileNeutronDetector`, `TileEntityPileSource`, `TileEntityPileVent`

### `tileentity/machine/fusion/` (10)
`IFusionPowerReceiver`, `TileEntityFusionBoiler`, `TileEntityFusionBreeder`, `TileEntityFusionCollector`, `TileEntityFusionCoupler`, `TileEntityFusionKlystron`, `TileEntityFusionKlystronCreative`, `TileEntityFusionMHDT`, `TileEntityFusionPlasmaForge`, `TileEntityFusionTorus`

### `tileentity/machine/` (13)
`TileEntityFusionTorusStruct`, `TileEntityICF`, `TileEntityICFController`, `TileEntityICFPress`, `TileEntityICFStruct`, `TileEntityMachineReactorBreeding`, `TileEntityPWRController`, `TileEntityReactorControl`, `TileEntityReactorResearch`, `TileEntityReactorZirnox`, `TileEntityWatz`, `TileEntityWatzStruct`, `TileEntityZirnoxDestroyed`

### `blocks/machine/rbmk/` (34)
`RBMKAbsorber`, `RBMKAutoloader`, `RBMKBase`, `RBMKBlank`, `RBMKBoiler`, `RBMKConsole`, `RBMKControl`, `RBMKControlAuto`, `RBMKCooler`, `RBMKCraneConsole`, `RBMKDebris`, `RBMKDebrisBurning`, `RBMKDebrisDigamma`, `RBMKDebrisRadiating`, `RBMKDisplay`, `RBMKGauge`, `RBMKGraph`, `RBMKHeater`, `RBMKIndicator`, `RBMKInlet`, `RBMKKeyPad`, `RBMKLever`, `RBMKLoader`, `RBMKMiniPanelBase`, `RBMKModerator`, `RBMKNumitron`, `RBMKOutgasser`, `RBMKOutlet`, `RBMKPipedBase`, `RBMKReflector`, `RBMKRod`, `RBMKRodReaSim`, `RBMKStorage`, `RBMKTerminal`

### `blocks/machine/pile/` (13)
`BlockGraphite`, `BlockGraphiteBreedingFuel`, `BlockGraphiteBreedingProduct`, `BlockGraphiteDrilled`, `BlockGraphiteDrilledBase`, `BlockGraphiteDrilledTE`, `BlockGraphiteFuel`, `BlockGraphiteNeutronDetector`, `BlockGraphiteRod`, `BlockGraphiteSource`, `BlockPile`, `BlockPileBrick`, `BlockPileDevice`

### `blocks/machine/fusion/` (9)
`MachineFusionBoiler`, `MachineFusionBreeder`, `MachineFusionCollector`, `MachineFusionCoupler`, `MachineFusionKlystron`, `MachineFusionKlystronCreative`, `MachineFusionMHDT`, `MachineFusionPlasmaForge`, `MachineFusionTorus`

### `blocks/machine/` (21)
`BlockFusionComponent`, `BlockFusionTorusStruct`, `BlockICF`, `BlockICFComponent`, `BlockICFLaserComponent`, `BlockICFStruct`, `BlockPWR`, `BlockPillarPWR`, `BlockWatzStruct`, `FusionHatch`, `MachineICF`, `MachineICFController`, `MachineICFPress`, `MachinePWRController`, `MachineReactorBreeding`, `MachineReactorControl`, `ReactorResearch`, `ReactorZirnox`, `Watz`, `WatzPump`, `ZirnoxDestroyed`

### `handler/neutron/` (7)
`NeutronHandler`, `NeutronNode`, `NeutronNodeWorld`, `NeutronStream`, `PileNeutronHandler`, `RBMKNeutronHandler`, `package-info`

### `inventory/container/` (19)
`ContainerFusionBreeder`, `ContainerFusionKlystron`, `ContainerFusionTorus`, `ContainerICF`, `ContainerICFPress`, `ContainerMachineReactorBreeding`, `ContainerPWR`, `ContainerRBMKAutoloader`, `ContainerRBMKControl`, `ContainerRBMKControlAuto`, `ContainerRBMKGeneric`, `ContainerRBMKHeater`, `ContainerRBMKOutgasser`, `ContainerRBMKRod`, `ContainerRBMKStorage`, `ContainerReactorControl`, `ContainerReactorResearch`, `ContainerReactorZirnox`, `ContainerWatz`

### `inventory/gui/` (26)
`GUIFusionBreeder`, `GUIFusionKlystron`, `GUIFusionTorus`, `GUIICF`, `GUIICFPress`, `GUIMachineReactorBreeding`, `GUIPWR`, `GUIRBMKAutoloader`, `GUIRBMKBoiler`, `GUIRBMKConsole`, `GUIRBMKControl`, `GUIRBMKControlAuto`, `GUIRBMKHeater`, `GUIRBMKOutgasser`, `GUIRBMKRod`, `GUIRBMKStorage`, `GUIReactorControl`, `GUIReactorResearch`, `GUIReactorZirnox`, `GUIScreenRBMKDisplay`, `GUIScreenRBMKGauge`, `GUIScreenRBMKGraph`, `GUIScreenRBMKIndicator`, `GUIScreenRBMKKeyPad`, `GUIScreenRBMKLever`, `GUIScreenRBMKTerminal`, `GUIWatz`

### `render/` (32)
`block/RenderPribris`, `block/RenderRBMKControl`, `block/RenderRBMKReflector`, `block/RenderRBMKRod`, `tileentity/RenderBreeder`, `tileentity/RenderCraneConsole`, `tileentity/RenderFusionBoiler`, `RenderFusionBreeder`, `RenderFusionCollector`, `RenderFusionCoupler`, `RenderFusionKlystron`, `RenderFusionKlystronCreative`, `RenderFusionMHDT`, `RenderFusionPlasmaForge`, `RenderFusionTorus`, `RenderFusionTorusMultiblock`, `RenderICF`, `RenderICFController`, `RenderICFMultiblock`, `RenderPileControl`, `RenderPileLoader`, `RenderPileVent`, `RenderRBMKAutoloader`, `RenderRBMKConsole`, `RenderRBMKControlRod`, `RenderRBMKDisplay`, `RenderRBMKFuelChannel`, `RenderRBMKGauge`, `RenderRBMKGraph`, `RenderRBMKIndicator`, `RenderRBMKKeyPad`, `RenderRBMKLever`, `RenderRBMKNumitron`, `RenderRBMKTerminal`, `RenderSmallReactor`, `RenderWatz`, `RenderWatzMultiblock`, `RenderWatzPump`, `RenderZirnox`, `RenderZirnoxDestroyed`

### Items (15)
`items/machine/ItemBreedingRod`, `ItemFuelRod`, `ItemICFPellet`, `ItemPWRFuel`, `ItemPWRPrinter`, `ItemPileRod`, `ItemPileRodMK2`, `ItemRBMKLid`, `ItemRBMKPellet`, `ItemRBMKRod`, `ItemReactorSensor`, `ItemWatzPellet`, `ItemZirnoxRod`; `items/tool/ItemRBMKTool`, `ItemDyatlov`

### Support (12)
`crafting/RodRecipes`, `crafting/handlers/RBMKFuelCraftingHandler`, `entity/projectile/EntityRBMKDebris`, `EntityZirnoxDebris`, `handler/nei/FuelPoolHandler`, `OutgasserHandler`, `PWRRecipeHandler`, `RBMKRodDisassemblyHandler`, `RBMKWasteDecayHandler`, `WatzRecipeHandler`, `hazard/modifier/HazardModifierRBMKHot`, `HazardModifierRBMKRadiation`, `inventory/recipes/FuelPoolRecipes`, `inventory/fluid/trait/FT_PWRModerator`, `particle/ParticleRBMKFlame`, `ParticleRBMKMush`, `ParticleRBMKSteam`
