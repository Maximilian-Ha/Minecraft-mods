# Gap Analysis — Subsystem: Machines & their BlockEntities/TileEntities

- **Original:** `hbm-1710` (Minecraft 1.7.10, Forge) — `src/main/java/com/hbm/tileentity/machine`, `src/main/java/com/hbm/blocks/machine`
- **Port:** `hbm-neo` (Minecraft 1.21.1, NeoForge) — `src/main/java/com/hbm/blockentity/machine`, `src/main/java/com/hbm/blocks/machine`
- Analysis date: 2026-09-13. Read-only; no files in either tree were modified.

---

## 1. Hard counts

### 1.1 Original (1.7.10)

| Path | `.java` files | LOC |
|---|---:|---:|
| `com/hbm/tileentity/machine` (root only) | 159 | 44,201 |
| `com/hbm/tileentity/machine/albion` | 8 | 1,851 |
| `com/hbm/tileentity/machine/fusion` | 10 | 2,965 |
| `com/hbm/tileentity/machine/oil` | 16 | 4,424 |
| `com/hbm/tileentity/machine/pile` | 11 | 1,688 |
| `com/hbm/tileentity/machine/rbmk` | 33 | 7,936 |
| `com/hbm/tileentity/machine/storage` | 21 | 3,977 |
| **`tileentity/machine` total** | **258** | **67,042** |
| `com/hbm/blocks/machine` (root only) | 216 | 22,189 |
| `com/hbm/blocks/machine/albion` | 6 | 357 |
| `com/hbm/blocks/machine/fusion` | 9 | 875 |
| `com/hbm/blocks/machine/pile` | 13 | 1,357 |
| `com/hbm/blocks/machine/rbmk` | 34 | 1,947 |
| **`blocks/machine` total** | **278** | **26,725** |
| **SUBSYSTEM TOTAL** | **536** | **93,767** |

Class-kind split: `tileentity/machine` = 5 interfaces + 18 abstract + **235 concrete** TEs.
`blocks/machine` = 8 abstract + **270 concrete** blocks.

> Note: the task hint said "159 files" for `tileentity/machine`. That is the *root package only*; the recursive count including `albion/fusion/oil/pile/rbmk/storage` is **258**.

### 1.2 NEO port (1.21.1)

| Path | `.java` files | LOC |
|---|---:|---:|
| `com/hbm/blockentity/machine` (root only) | 22 | — |
| `com/hbm/blockentity/machine/boiler` | 3 | — |
| `com/hbm/blockentity/machine/heater` | 6 | — |
| `com/hbm/blockentity/machine/oil` | 5 | — |
| `com/hbm/blockentity/machine/storage` | 11 | — |
| **`blockentity/machine` total** | **47** | **10,096** |
| `com/hbm/blocks/machine` (root only) | 28 | — |
| `com/hbm/blocks/machine/heater` | 6 | — |
| **`blocks/machine` total** | **34** | **3,856** |
| **SUBSYSTEM TOTAL** | **81** | **13,952** |

Class-kind split: `blockentity/machine` = 8 abstract + **39 concrete** BEs. `blocks/machine` = 1 abstract + **33 concrete** blocks.

Two machine blocks belonging to this subsystem live outside `blocks/machine` in NEO and were counted separately (not in the 34):
`com/hbm/blocks/network/MachineBatterySocketBlock.java`, `com/hbm/blocks/network/MachineBatteryREDDBlock.java`.

### 1.3 Headline ratios

| Metric | Original | NEO | Coverage |
|---|---:|---:|---:|
| Subsystem `.java` files | 536 | 81 | 15.1% |
| Subsystem LOC | 93,767 | 13,952 | 14.9% |
| Concrete TE/BE classes | 235 | 39 | 16.6% |
| Concrete block classes | 270 | 33 (+2 elsewhere) | 13.0% |
| Machine OBJ models in resources (whole mod) | 514 | 111 | 21.6% |
| TESR / BER classes (whole mod) | 249 | 47 | 18.9% |
| Recipe-system classes | 67 | 18 | 26.9% |
| NEI/JEI machine handlers | ~57 NEI | 13 JEI categories | ~23% |

LOC-weighted coverage of the *specific machines that were ported* (summing the original LOC of every 1.7.10 class that has a NEO counterpart) is ≈ **15,300 / 93,767 ≈ 16.3%** — consistent with the file-count ratio.

---

## 2. Machine-by-machine status

The NEO port registers its block entities in
`hbm-neo/src/main/java/com/hbm/blockentity/NtmBlockEntityTypes.java` (271 lines) and its blocks in
`hbm-neo/src/main/java/com/hbm/blocks/NtmBlocks.java` (437 lines, 296 block registrations for the whole mod).
Everything below was checked against those two files *and* by opening the classes.

### 2.1 PORTED — functionally complete or near-complete (mechanics verified)

These were compared method-by-method against the 1.7.10 originals. Differences are almost entirely 1.7.10→1.21.1 API renames (`readFromNBT`→`loadAdditional`, `provideGUI`→`createMenu`, `getAccessibleSlotsFromSide`→`getSlotsForFace`, `isItemValidForSlot`→`canPlaceItem`, `invalidate`→`setRemoved`, `onChunkUnload`→`onChunkUnloaded`).

| Machine | 1.7.10 class (LOC) | NEO class (LOC) | Notes |
|---|---|---|---|
| Centrifuge | `TileEntityMachineCentrifuge` (394) | `machine/MachineCentrifugeBlockEntity` (380) | Upgrades, audio loop, power, JEI, BER all present |
| Chemical plant | `TileEntityMachineChemicalPlant` (356) | `machine/MachineChemicalPlantBlockEntity` (345) | Uses ported `ModuleMachineChemicalPlant` |
| Assembly machine | `TileEntityMachineAssemblyMachine` (518) | `machine/MachineAssemblyMachineBlockEntity` (480) | Uses ported `ModuleMachineAssembler`; JEI transfer handler present |
| Press | `TileEntityMachinePress` (261) | `machine/MachinePressBlockEntity` (232) | |
| Shredder | `TileEntityMachineShredder` (434) | `machine/MachineShredderBlockEntity` (348) | |
| Soldering station | `TileEntityMachineSolderingStation` (404) | `machine/MachineSolderingStationBlockEntity` (409) | |
| Arc welder | `TileEntityMachineArcWelder` (407) | `machine/MachineArcWelderBlockEntity` (317) | |
| Blast furnace | `TileEntityMachineBlastFurnace` (330) | `machine/MachineBlastFurnaceBlockEntity` (281) | |
| Combination furnace | `TileEntityFurnaceCombination` (286) | `machine/MachineFurnaceCombinationBlockEntity` (269) | |
| Wood burner | `TileEntityMachineWoodBurner` (331) | `machine/MachineWoodBurnerBlockEntity` (368) | |
| Heat boiler | `TileEntityHeatBoiler` (382) | `machine/boiler/MachineHeatBoilerBlockEntity` (46) + `AbstractBoilerBlockEntity` (324) | Refactored into shared base |
| Industrial boiler | `TileEntityHeatBoilerIndustrial` (339) | `machine/boiler/MachineIndustrialBoilerBlockEntity` (50) + base | |
| Heater: firebox | `TileEntityHeaterFirebox` (107) | `machine/heater/HeaterFireboxBlockEntity` (219) | |
| Heater: oven | `TileEntityHeaterOven` (133) | `machine/heater/HeaterOvenBlockEntity` (70) + `AbstractHeaterMachineBlockEntity` (128) | |
| Heater: oil burner | `TileEntityHeaterOilburner` (276) | `machine/heater/HeaterOilburnerBlockEntity` (209) | |
| Heater: electric | `TileEntityHeaterElectric` (232) | `machine/heater/HeaterElectricBlockEntity` (213) | |
| Heater: heat exchanger | `TileEntityHeaterHeatex` (288) | `machine/heater/HeaterHeatexBlockEntity` (228) | |
| Oil derrick | `oil/TileEntityMachineOilWell` (190) | `machine/oil/MachineOilWellBlockEntity` (137) + `OilDrillBaseBlockEntity` (318) | `suckRec`→`suckRecursive` present |
| Pumpjack | `oil/TileEntityMachinePumpjack` (228) | `machine/oil/MachinePumpjackBlockEntity` (182) | |
| Fracking tower | `oil/TileEntityMachineFrackingTower` (233) | `machine/oil/MachineFrackingTowerBlockEntity` (189) | |
| Fluid tank | `storage/TileEntityMachineFluidTank` (590) | `machine/storage/MachineFluidTankBlockEntity` (418) | Only 1.21 user of `IRepairable` |
| Fluid barrel | `storage/TileEntityBarrel` (420) | `machine/storage/BarrelBlockEntity` (336) | |
| Crates (iron/steel/tungsten/desh/template) | `storage/TileEntityCrateBase` (230) + 4 subclasses | `machine/storage/CrateBaseBlockEntity` (330) + 5 subclasses | |
| Battery REDD | `storage/TileEntityBatteryREDD` (280) | `machine/storage/BatteryREDDBlockEntity` (255) | |
| Chungus turbine | `TileEntityChungus` (273) | `machine/ChungusBlockEntity` (164) + `TurbineBaseBlockEntity` (173) | |
| Zirnox (destroyed shell) | `TileEntityZirnoxDestroyed` (105) | `machine/ZirnoxDestroyedBlockEntity` (57) | |
| Sat linker | `TileEntityMachineSatLinker` (60) | `machine/MachineSatLinkerBlockEntity` (53) | |
| Decontaminator | `TileEntityDecon` (48) | `machine/DecontaminatorBlockEntity` (55) | |
| Geiger block | `TileEntityGeiger` (115) | `machine/GeigerBlockEntity` (79) | |
| Soyuz launcher | `TileEntitySoyuzLauncher` (464) | `machine/SoyuzLauncherBlockEntity` (416) | `// todo make sats` at line 342 |
| NTM anvil | `NTMAnvil` | `blocks/machine/NTMAnvilBlock` + `inventory/recipes/anvil/*` | Anvil recipe system + JEI ported |

### 2.2 PORTED PARTIALLY — real mechanics missing

| Machine | What's missing (verified by reading both files) |
|---|---|
| **ZIRNOX reactor** (`TileEntityReactorZirnox` 693 → `ReactorZirnoxBlockEntity` 387) | Meltdown is gutted. NEO `meltdown()` (lines 309–321) does: clear inventory, `fillSpace` with `zirnox_destroyed`, play sound, vanilla `level.explode(…, 12F)`. **Missing from the original:** `spawnDebris(DebrisType)` / `zirnoxDebris()` — 2 EXCHANGER + 20 CONCRETE + 20 BLANK + 10 ELEMENT + 10 GRAPHITE + 10 SHRAPNEL `EntityZirnoxDebris` projectiles; `ExplosionNukeGeneric.waste(world, x, y, z, 35)` fallout; the 100-block achievement trigger; the `radMark` elemental-mob flag. Also missing: `getGaugeScaled`, OpenComputers component (10 `@Callback` methods), ROR value/function providers, `provideExtraInfo`. |
| **Battery socket** (`TileEntityBatterySocket` 445 → `BatterySocketBlockEntity` 180) | The entire supercapacitor arc-discharge mechanic is absent: `discharge` (BulletConfig), `explodeDischarge`, `fluctuate`, `pickNewSCTarget`, `hasSCLoaded`, plus `getEnergyInfo`/`IInfoProviderEC`, `provideExtraInfo`, OC and ROR. What remains is a plain battery-item charger with a 20-tick delta log. |
| **Refinery** (`oil/TileEntityMachineRefinery` 476 → `MachineRefineryBlockEntity` 456) | `onFire` state is ported (set at line 430, saved/synced), but `isDamaged()` / `getRepairMaterials()` / `repair()` / `tryExtinguish()` are not — the refinery cannot be repaired or extinguished. `IRepairable` exists in NEO but is implemented only by `MachineFluidTankBlockEntity`. `getFloorCount`/`getFloorPosFromIndex` also gone. |
| **Radar** (`TileEntityMachineRadarNT` 734 → `MachineRadarBlockEntity` 423) | Scan/allocate/redstone logic ported faithfully. Missing: whole OC component surface (11 `@Callback` methods), `IConfigurableMachine` JSON tuning (`getConfigName`/`readIfPresent`/`writeConfig`). |
| **Large radar** | `NtmBlocks.MACHINE_RADAR_LARGE` (NtmBlocks.java:378) is registered as a plain `MachineRadarBlock`, which returns a `MachineRadarBlockEntity`, but `NtmBlockEntityTypes.MACHINE_RADAR` (line 259) only lists `NtmBlocks.MACHINE_RADAR` as a valid block. The large radar's BE is therefore invalid for its block. The 1.7.10 `TileEntityMachineRadarLarge` (separate class, larger range, `BlockDummyable` multiblock) is not ported at all — the NEO block is a single non-multiblock cube. |
| **Cooling tower (small)** | `CondenserBaseBlockEntity` + `TowerSmallBlockEntity` exist, but `NtmBlockEntityTypes.TOWER_SMALL` (line 236) is registered against `NtmBlocks.FLUID_DUCT_NEO` and **there is no tower block at all**. Dead/unreachable code. `TileEntityCondenser`, `TileEntityCondenserPowered`, `TileEntityTowerLarge` are not ported. |
| **Chungus turbine** | `getType`/`setType` (turbine tier/variant selection) and `IConfigurableMachine` JSON config not ported; OC and ROR gone. |
| **Everything with upgrades** | `UpgradeManagerNT` and `IUpgradeInfoProvider` exist, but only **10** NEO block entities implement `IUpgradeInfoProvider` vs **31** in 1.7.10. |

### 2.3 NOT PORTED AT ALL

Verified by keyword scan of every 1.7.10 class name against the whole NEO tree (`grep -rlI`), then manually de-duplicating false positives (`Core`, `Dummy`, `FF`, `Safe` are English words that matched unrelated code; `Watz`, `Microwave`, `ICF`, `SoyuzCapsule`, `MachineBattery` matched only enum names, damage types, fluid traits, or comments).

**Whole families — 0 files ported:**

| Family | Original files | Original LOC | Contents |
|---|---:|---:|---|
| **RBMK** (`rbmk/`) | 33 TE + 34 blocks | 9,883 | `RBMKBase`, `RBMKRod`, `RBMKRodReaSim`, `RBMKBoiler`, `RBMKHeater`, `RBMKOutgasser`, `RBMKControl`/`Auto`/`Manual`, `RBMKAbsorber`, `RBMKModerator`, `RBMKReflector`, `RBMKCooler`, `RBMKBlank`, `RBMKStorage`, `RBMKConsole`, `RBMKCraneConsole`, `RBMKAutoloader`, `RBMKInlet`/`Outlet`, `RBMKGauge`, `RBMKGraph`, `RBMKIndicator`, `RBMKKeyPad`, `RBMKLever`, `RBMKNumitron`, `RBMKDisplay`, `RBMKTerminal`, `RBMKDials`, `IRBMKFluxReceiver`, `IRBMKLoadable`, debris blocks (`RBMKDebris`, `…Burning`, `…Digamma`, `…Radiating`) |
| **Fusion reactor** (`fusion/`) | 10 TE + 9 blocks | 3,840 | `TileEntityFusionTorus`, `FusionBoiler`, `FusionBreeder`, `FusionCollector`, `FusionCoupler`, `FusionKlystron`(+Creative), `FusionMHDT`, `FusionPlasmaForge`, `IFusionPowerReceiver`, `BlockFusionTorusStruct`, `FusionHatch` |
| **Particle accelerator / Albion** (`albion/`) | 8 TE + 6 blocks | 2,208 | `TileEntityPABeamline`, `PADetector`, `PADipole`, `PAQuadrupole`, `PARFC`, `PASource`, `TileEntityCooledBase`, `IParticleUser`, `BlockBeamBase`, `BlockHadronCoil` |
| **Nuclear pile** (`pile/`) | 11 TE + 13 blocks | 3,045 | `TileEntityPileBase`/`MK2`, `PileCore`, `PileControl`, `PileFuel`, `PileBreedingFuel`, `PileSource`, `PileNeutronDetector`, `PileVent`, `PileLoader`, `PileDeviceBase`, all `BlockGraphite*` |
| **ICF (inertial confinement fusion)** | 4 TE + 4 blocks | ~1,200 | `TileEntityICF`, `ICFController`, `ICFPress`, `ICFStruct`, `BlockICF*`, `BlockICFLaserComponent` |
| **Foundry / casting** | 8 TE + 8 blocks | ~1,900 | `TileEntityFoundryBase`, `FoundryBasin`, `FoundryCastingBase`, `FoundryChannel`, `FoundryMold`, `FoundryOutlet`, `FoundrySlagtap`, `FoundryTank`, `TileEntityCrucible`, `IRenderFoundry` |
| **PWR / Watz / research reactors** | 8 TE + 8 blocks | ~2,300 | `TileEntityPWRController`, `BlockPWR`, `BlockPillarPWR`, `TileEntityWatz`, `TileEntityWatzStruct`, `WatzPump`, `TileEntityReactorResearch`, `TileEntityReactorControl`, `TileEntityMachineReactorBreeding` |
| **Anti-matter / Core (CM)** | 6 TE + 7 blocks | ~1,600 | `TileEntityCore`, `CoreEmitter`, `CoreInjector`, `CoreReceiver`, `CoreStabilizer`, `BlockCM`, `BlockCMAnchor`, `BlockCMFlux`, `BlockCMGlass`, `BlockCMHeat`, `BlockCMPort`, `CoreCore`, `CoreComponent` |

**Individual machines — a non-exhaustive but representative list of what is missing (all 0 hits in NEO):**

*Oil / chemical processing (11 of 16 `oil/` TEs missing):*
`TileEntityMachineCatalyticCracker`, `CatalyticReformer`, `Coker`, `FractionTower`, `GasFlare`, `Hydrotreater`, `Liquefactor`, `PyroOven`, `Solidifier`, `VacuumDistill`, `TileEntitySpacer` (+ `FractionSpacer` block).

*Power generation:*
`TileEntityMachineRTG`, `MachineRadGen`, `MachineDiesel`, `MachineCombustionEngine`, `MachineTurbofan`, `MachineLargeTurbine`, `MachineIndustrialTurbine`, `MachineTurbine`, `MachineTurbineGas`, `TileEntitySteamEngine`, `TileEntityStirling`, `TileEntitySolarBoiler`, `TileEntitySolarMirror`, `MachineIGenerator`, `TileEntityTesla`, `TileEntityRtgFurnace`, `TileEntityDiFurnaceRTG`, `MachineConverterHeRf` / `ConverterRfHe`.

*Ore & material processing:*
`MachineArcFurnaceLarge`, `MachineRotaryFurnace`, `MachineCrystallizer`, `MachineMixer`, `MachineGasCent`, `MachinePUREX`, `MachineRadiolysis`, `MachineExposureChamber`, `MachineElectrolyser`, `MachineCompressor`(+`Base`,`Compact`), `MachineEPress`, `MachineAmmoPress`, `MachineRockMill`, `MachineOreSlopper`, `MachineThresher`, `TileEntitySawmill`, `MachineAutosaw`, `MachineStrandCaster`, `MachineHephaestus`, `TileEntitySILEX`, `MachineExcavator`, `MachineMiningLaser`, `TileEntityMachineDrain`, `TileEntityMachineIntake`, `MachinePumpBase`/`PumpElectric`/`PumpSteam`, `TileEntityMicrowave`, `TileEntityDeuteriumExtractor`/`DeuteriumTower`, `TileEntityDiFurnace`, `TileEntityFurnaceBrick`/`FurnaceIron`/`FurnaceSteel`, `MachineElectricFurnace`, `TileEntityAshpit`, `TileEntityChimneyBase`/`ChimneyBrick`/`ChimneyIndustrial`.

*Assembly / automation:*
`MachineAssemblyFactory`, `MachineChemicalFactory`, `MachinePrecAss`, `MachineMissileAssembly`, `MachineAutocrafter`, `MachineFunnel`, `TileEntityConveyorPress`, `PistonInserter`, `TileEntityCustomMachine` (+ `BlockCustomMachine`, `CustomMachineRecipes`, `CustomMachineConfigJSON`).

*Logic / control / misc:*
`MachineSuperComputer`, `MachineTapeDrive`, `MachineDetector`, `PowerDetector`, `MachineSiren`, `TileEntityRadiobox`, `TileEntityRadioRec`, `TileEntityBroadcaster`, `PinkCloudBroadcaster`, `MachineSatDock`, `MachineSatLink`, `MachineTeleporter`, `MachineTeleanchor`, `MachineKeyForge`, `MachineLPW2`, `TileEntityCharger`, `TileEntityForceField` / `BlockFF` / `MachineFieldDisturber`, `TileEntityCyberCrab`, `TileEntityDemonLamp`, `TileEntityBlastDoor`, `TileEntityHatch`, `TileEntityCargoElevator`, `TileEntityChlorineSeal` / `BlockSeal`, `TileEntityRefueler`, `TileEntityLaunchpadLambda`, `TileEntityLaunchpadSoyuz`, `MachineTransformer`, `MachineCapacitor`/`CapacitorBus`, `BlockArmorTable`, `BlockWeaponTable`, `BlockVendingMachine`, `Floodlight`(+Beam), `Spotlight`(+Beam/Modular), `RailBooster`/`RailGeneric`/`RailHighspeed`.

*Storage (11 of 21 `storage/` TEs missing):*
`TileEntityMachineBigAssTank`, `MachineBAT9000`, `MachineFENSU`, `MachineOrbus`, `MachinePuF6Tank`, `MachineUF6Tank`, `TileEntityMassStorage` (+ `BlockMassStorage`), `TileEntitySafe`, `TileEntityFileCabinet`, `TileEntitySoyuzCapsule`, `TileEntityStorageDrum`, `TileEntityWasteDrum`.
(The 1.7.10 tiered batteries `machine_battery*`, `machine_fensu`, `capacitor_*`, `machine_bat9000` are `@Deprecated` in `ModBlocks.java` — skipping them is a legitimate choice, not a gap. The *live* battery blocks, socket + REDD, **are** ported.)

---

## 3. Infrastructure: what the port already has vs. what is still missing

### 3.1 Present and working (this materially lowers remaining effort)

| Concern | NEO implementation |
|---|---|
| Machine base class | `com/hbm/blockentity/MachineBaseBlockEntity.java` (`WorldlyContainer` + `MenuProvider` + `ITickable`), `LoadedBaseBlockEntity`, `BlockEntityNT` |
| Energy net | `api/hbm/energymk2/*` — `IEnergyReceiverMK2`, `IEnergyProviderMK2`, `IEnergyConductorMK2`, `IBatteryItem` |
| Fluid net | `api/hbm/fluidmk2/*` — `IFluidStandardTransceiverMK2`, `com/hbm/inventory/fluid/tank/FluidTank`, fluid traits (`FT_Heatable`, `FT_PWRModerator`, …) |
| Heat | `api/hbm/tile/IHeatSource` |
| Node/network graph | `com/hbm/uninos` (6 of 16 original classes: `NodeNet`, `UniNodespace`, `GenNode`, `FluidNetProvider`, `PowerNetProvider`, `INetworkProvider`) |
| Multiblock | `com/hbm/blocks/DummyableBlock.java` (BlockDummyable equivalent, 25 users), `MultiBlock`, `IMultiBlock`, `handler/MultiblockHandlerXR`, `ProxyBaseBlockEntity`/`ProxyComboBlockEntity` |
| BE→client sync | `com/hbm/network/toclient/BufPacket.java` + `IBufPacketReceiver` + `networkPackNT(range)` |
| Control packets | `network/toserver/CompoundTagControl.java`, `IControlReceiver`-style `receiveControl(CompoundTag)` |
| GUI | `com/hbm/inventory/menus` (37 menus), `com/hbm/inventory/screens` (48 screens) |
| Rendering | `com/hbm/render/blockentity` (47 classes) with `BlockEntityRendererNT` + `IBEWLRProvider` (item-form rendering) |
| Upgrades | `UpgradeManagerNT`, `IUpgradeInfoProvider` (10 users) |
| Persistent NBT (machine-in-item) | `IPersistentNBT` (9 users, matching the original's 9) |
| JEI | `com/hbm/handler/jei` — 13 categories + 2 transfer handlers + subtype interpreters |
| Machine logic modules | `com/hbm/module` — `ModuleMachineBase`, `ModuleMachineAssembler`, `ModuleMachineChemicalPlant`, `ModuleBurnTime` (4 of the original 15) |
| Repair/blowtorch | `com/hbm/blockentity/IRepairable.java` |
| Look overlay | `com/hbm/blocks/ILookOverlay.java` |

### 3.2 Missing infrastructure (blocks many remaining machines)

| Missing | Original | Impact |
|---|---|---|
| **JSON machine config** | `config/MachineConfig.java`, `MachineDynConfig.java`, `CustomMachineConfigJSON.java`, `IConfigurableMachine` (28 machine TEs) | 0 NEO classes implement it. Every ported machine has its tuning constants hardcoded (e.g. `// todo config` in `MachineOilWellBlockEntity:30`, `MachinePumpjackBlockEntity:32`, `MachineFrackingTowerBlockEntity:27`, `CondenserBaseBlockEntity:24`). The whole `hbmMachines.json` user-tuning feature is gone. |
| **OpenComputers integration** | `li.cil.oc` `SimpleComponent` on **57** machine TEs | 0 in NEO. OC does not exist on 1.21.1; a decision is needed (drop, or reimplement against CC:Tweaked / a custom API). Every affected machine needs its `@Callback` surface re-specified. |
| **Redstone-over-Radio** | `IRORValueProvider`/`IRORInteractive` on **40** machine TEs | Only 5 NEO files touch it. |
| **Energy Control / info providers** | `api/hbm/tile/IInfoProviderEC`, `EnumTransferAction`, `ILoadedTile` | `api/hbm/tile` in NEO contains only `IHeatSource`; `ILoadedTile`→`ILoadedBE` exists, the other two do not. |
| **Remaining logic modules** | `ModuleMachineRockMill`, `ModuleMachinePUREX`, `ModuleMachinePlasma`, `ModuleMachineSuperComputer`, `ModuleMachinePrecAss`, `ModuleMachineFusion`, `ModulePatternMatcher`, `NumberDisplay`, `IParse`/`ParseMSES1*` | 11 of 15 missing — each gates its machines. |
| **Recipe systems** | 67 recipe classes | 18 in NEO. ~49 recipe types missing (`CrucibleRecipes`, `SILEXRecipes`, `CyclotronRecipes`, `FusionRecipes`, `CrackingRecipes`, `ReformingRecipes`, `CokerRecipes`, `PyroOvenRecipes`, `FractionRecipes`, `VacuumRefineryRecipes`, `HydrotreatingRecipes`, `SolidificationRecipes`, `LiquefactionRecipes`, `CompressorRecipes`, `ElectrolyserFluidRecipes`/`MetalRecipes`, `GasCentrifugeRecipes`, `PUREXRecipe(s)`, `RadiolysisRecipes`, `ExposureChamberRecipes`, `CrystallizerRecipes`, `MixerRecipes`, `RockMillRecipes`, `ArcFurnaceRecipes`, `RotaryFurnaceRecipes`, `PrecAssRecipes`, `AmmoPressRecipes`, `MissileAssembly`, `BreederRecipes`, `FluidBreederRecipes`, `FuelPoolRecipes`, `OutgasserRecipes`, `AnnihilatorRecipes`, `SuperComputerRecipes`, `CustomMachineRecipes`, …). |
| **Assets** | 514 OBJ models | 111 in NEO. ~400 machine models (plus their textures, blockstates, item models, and the 1.21 datagen entries in `com/hbm/datagen`) still to migrate. Only 2 hand-written blockstate JSONs exist; the rest is datagen. |

---

## 4. Architectural blockers for the remaining ~85%

1. **Metadata-as-subtype → blockstates + multiple blocks.** 124 of 278 original machine blocks extend `BlockDummyable`, which packs *facing + dummy-offset* into the 4-bit block metadata (`getBlockMetadata() - BlockDummyable.offset`). NEO's `DummyableBlock` re-expresses this as `FACING` + a `DummyBlockType` enum property (`com/hbm/blocks/DummyBlockType.java`, `blocks/states/`). Every remaining multiblock has to be re-derived by hand: core position, `findCore`, fill-space dimensions, and the collision/outline shapes that used to be computed from meta. Two unresolved `// todo` comments already sit in `DummyableBlock.java:137` and `:164`.
2. **`IIcon` → texture atlas + JSON models.** 68 original machine blocks use `IIcon` with `registerBlockIcons`/`getIcon(side, meta)`. 1.21 has no per-side icon API; each needs a blockstate/model JSON (or datagen) and the sided-texture logic re-expressed as model variants.
3. **TESR → `BlockEntityRenderer` + `Tessellator` → `VertexConsumer`/`PoseStack`.** 249 original TESRs use `Tessellator.instance`, `GL11` fixed-function calls, `glPushMatrix`, and the WavefrontObject loader. NEO has 47 BERs on a new `BlockEntityRendererNT` + `IBEWLRProvider` base (item-form rendering now needs a `BlockEntityWithoutLevelRenderer`, which did not exist in 1.7.10). ~200 renderers remain, each also needing its OBJ re-exported/re-baked and a RenderType chosen.
4. **Custom packets → `CustomPacketPayload` + `StreamCodec`.** The original uses 29 `IMessage`/`SimpleNetworkWrapper` packets with raw `ByteBuf`. NEO has 15 payloads and a working `BufPacket` BE-sync path, but every remaining machine that syncs bespoke state (RBMK console graphs, radar screens, PA beam state, foundry mold contents) needs its serialize/deserialize re-typed against `RegistryFriendlyByteBuf`, and `NtmNetwork` registration.
5. **`IInventory`/`ISidedInventory` → `WorldlyContainer` + `NonNullList<ItemStack>`; `ItemStack == null` → `ItemStack.EMPTY`.** Mechanical but touches every one of the ~196 unported TEs. The original also frequently mutates `slots[i] = null`.
6. **Item damage-as-subtype → data components.** `ItemBatterySC.EnumBatterySC`, RBMK fuel rods (`ItemRBMKRod` stores heat/yield/xenon in NBT), `MetaHelper`/`MetaSubtypeInterpreter` in NEO shows the shim in use. All RBMK rod state and every `meta`-keyed machine item must move to `DataComponentType`s in `items/component/NtmDataComponents.java`.
7. **Capabilities rewrite.** 1.7.10 HBM does *not* use Forge capabilities at all — it uses its own `IEnergyReceiverMK2` / `IFluidStandardTransceiver` interfaces discovered by `world.getTileEntity(...) instanceof`. NEO keeps those interfaces but must additionally register `Capabilities.ItemHandler.BLOCK` / `FluidHandler.BLOCK` per `BlockEntityType` for interop with other 1.21 mods. Nothing in the current port registers those.
8. **GUI rewrite.** 179 `Container` + 224 `GuiContainer` classes → `AbstractContainerMenu` + `AbstractContainerScreen` with `MenuType` registration (`NtmMenuTypes`) and a `StreamCodec` for the extra data. 37/48 exist; ~140 machine GUIs remain, and each `drawTexturedModalRect` call has to become `GuiGraphics.blit`.
9. **NEI → JEI.** ~57 NEI handlers → 13 JEI categories. JEI's recipe-transfer and focus API is materially different; each category also needs a `RecipeType`, a drawable background, and (for fluids) a `IPlatformFluidHelper` adapter.
10. **Ore dictionary → tags.** `OreDictionary.getOres(...)` is used pervasively by machine recipes; NEO has `registry/tags` and `data/c/tags` but the migration is only partially done (18/67 recipe classes).
11. **Recipes as code → data-driven `SerializableRecipe`.** NEO has moved to `SerializableRecipe`/`GenericRecipe` with `data/hbmsntm/recipe` JSON. Every remaining recipe class must be re-expressed in that shape plus a datagen provider — this is a *design* change, not a translation, and is the single biggest hidden cost.
12. **Achievements/advancements, `ExplosionNukeGeneric.waste`, `EntityZirnoxDebris`, `EntityRBMKDebris`.** Reactor failure modes depend on entity + fallout subsystems outside this package; they gate the RBMK/Zirnox/Watz/fusion work.
13. **OpenComputers has no 1.21 equivalent.** 57 machines expose an OC component. Either drop the feature (a visible functional regression) or write a new automation API.

### Known defects already present in the port

- `NtmBlockEntityTypes.java:236` — `TOWER_SMALL` is registered against `NtmBlocks.FLUID_DUCT_NEO`; there is no cooling-tower block, so `TowerSmallBlockEntity`/`CondenserBaseBlockEntity` are unreachable.
- `NtmBlocks.java:378` — `MACHINE_RADAR_LARGE` uses `MachineRadarBlock` whose BE type (`MACHINE_RADAR`) does not list it as valid.
- `NtmBlocks.java:310` — `PRESS_PREHEATER` is registered as a plain `Block` (no BE), i.e. decorative only.

---

## 5. Honest coverage estimate

| Basis | Coverage |
|---|---:|
| Files | 15.1% |
| LOC | 14.9% |
| Concrete machine classes | 16.6% |
| LOC-weighted, counting only machines with a NEO counterpart | 16.3% |
| **Functional coverage (adjusted down for partial ports and defects)** | **≈ 16%** |

Adjustments applied: the ~33 ported machines are genuinely faithful (not stubs) and the shared infrastructure is real, which argues *up*; but Zirnox meltdown, battery-socket discharge, refinery repair, large radar, and the cooling tower are incomplete or broken, and the JSON machine-config, OC, and ROR layers are absent across the board, which argues *down*. The two roughly cancel, leaving the file/LOC ratio as the fair number. **16%.**

---

## 6. Effort estimate

Remaining: ~196 concrete TEs + ~239 concrete blocks ≈ **78,000 LOC** of original logic, plus ~400 OBJ models/renderers, ~140 GUIs, ~49 recipe systems.

| Work package | Person-days |
|---|---:|
| ~120 simple/medium standalone machines (logic + block + menu/screen + BER + model/blockstate + JEI where needed) @ ~1.2 d | 145 |
| RBMK complete (33 TEs, 34 blocks, console/crane/graph/terminal GUIs, rod data components, debris, ReaSim) | 30 |
| Fusion torus + ICF | 18 |
| Particle accelerator (albion) | 10 |
| Nuclear pile + graphite blocks | 8 |
| PWR + Watz + research/breeding reactors | 18 |
| Anti-matter Core (CM) | 8 |
| Foundry / crucible / casting chain | 12 |
| Remaining oil & chemical chain (11 machines + their fluid traits) | 16 |
| Power generation family (RTG, turbines ×5, engines, solar, tesla, converters) | 22 |
| Remaining storage (BigAssTank, FENSU, BAT9000, Orbus, UF6/PuF6, mass storage, safe, drums, capsule) | 12 |
| Automation/logistics (autocrafter, funnel, conveyor press, inserter, slopper, excavator, mining laser, autosaw, …) | 24 |
| Infrastructure: `IConfigurableMachine`/JSON config, ROR completion, `IInfoProviderEC`, remaining 11 logic modules, OC replacement decision | 22 |
| ~49 recipe systems → `SerializableRecipe` + datagen + ~44 JEI categories | 32 |
| Assets: ~400 OBJ models, textures, blockstate/item-model datagen | 30 |
| Fix existing defects + capability registration + integration testing | 15 |
| **Total** | **≈ 322** |

Call it **300–360 person-days** for one experienced NeoForge modder who already knows the HBM codebase. Difficulty: **extreme** — this is the largest subsystem in the mod and the two hardest parts (RBMK and the recipe-system redesign) are both still entirely ahead.

---

## 7. Depends on (must land before/with this subsystem)

- **Fluids** — `FluidTank`, fluid traits (`FT_Heatable`, `FT_PWRModerator`, `FT_Combustible`), `NtmFluids`/`NtmFluidTypes`. Mostly present; the remaining traits gate the oil/chemical chain.
- **Energy** — `api/hbm/energymk2` + `uninos` power net. Present; 10 of 16 `uninos` classes still missing.
- **Items** — battery items, RBMK rods, upgrades, templates, blueprints, all as data components. Gates the reactor and assembler families.
- **Recipes / JEI** — the `SerializableRecipe` design and the missing 49 recipe types gate most processing machines.
- **GUI / menus** — `MenuType` + screen framework. Present but only ~25% populated.
- **Rendering** — `BlockEntityRendererNT`, `IBEWLRProvider`, OBJ loader, shaders. Present but only ~19% populated.
- **Explosions & radiation** — `ExplosionNukeGeneric.waste`, fallout, `hazard`/`radiation` handlers. Gates RBMK/Zirnox/Watz/fusion failure modes.
- **Entities** — `EntityZirnoxDebris`, `EntityRBMKDebris`, rubble/shrapnel. Gates reactor meltdowns.
