# HBM NTM Port Gap Analysis — Fluid System

**Subsystem:** Fluid system (FluidType registry, fluid traits, tanks, pipes, pressure, fluid networks)
**Original:** `hbm-1710` (Minecraft 1.7.10 / Forge) — 3477 `.java` files total
**Port:** `hbm-neo` (Minecraft 1.21.1 / NeoForge) — 981 `.java` files total
**Method:** file-by-file enumeration with `find`/`wc`, plus whitespace-insensitive `diff -ubwB` of every counterpart file. Class renames (`TileEntityFoo` → `FooBlockEntity`, `ItemFluidIcon` → `FluidIconItem`, `FluidDuctStandard` → `FluidDuctStandardBlock`) were resolved by keyword search, not filename matching.

---

## 1. Scope and raw counts

### Scope definition used for the headline counts

| Area | Original paths | NEO paths |
|---|---|---|
| Legacy fluid API (MK1) | `api/hbm/fluid/` | *(absent — intentionally dropped)* |
| Fluid network API (MK2) | `api/hbm/fluidmk2/` | `api/hbm/fluidmk2/` |
| Registry / traits / tanks | `com/hbm/inventory/fluid/{,tank,trait}/` | same |
| World fluid blocks | `com/hbm/blocks/fluid/` | `com/hbm/blocks/fluids/` + `com/hbm/fluids/` |
| Node network core | `com/hbm/uninos/{,networkproviders}/` | same |
| Container registry | `com/hbm/inventory/FluidContainer{,Registry}.java` | same |
| Ducts / pipes (blocks) | `com/hbm/blocks/network/FluidDuct*`, `FluidPipeAnchor`, `FluidPump`, `IBlockFluidDuct` | `com/hbm/blocks/network/FluidDuct*Block`, `IBlockFluidDuct` |
| Ducts / pipes (TE/BE) | `com/hbm/tileentity/network/TileEntityPipe*`, `TileEntityPipelineBase` | `com/hbm/blockentity/network/PipeBaseBlockEntity` |
| Fluid items | `com/hbm/items/machine/ItemFluid*`, `ItemCanister`, `ItemGasTank`, `ItemInfiniteFluid`, `IItemFluidIdentifier`, `com/hbm/items/tool/ItemPipette` | `com/hbm/items/machine/Fluid*Item`, `InfiniteFluidItem`, `IItemFluidIdentifier` |
| Fluid storage machines | `TileEntityMachineFluidTank`, `TileEntityBarrel`, `TileEntityMachineBigAssTank`, `TileEntityMachinePuF6Tank`, `TileEntityMachineUF6Tank`, `TileEntityStorageDrum` | `MachineFluidTankBlockEntity`, `BarrelBlockEntity` |
| Fluid/pipe renderers | `RenderPipe`, `RenderBoxDuct`, `RenderTestPipe`, `RenderPipeAnchor`, `RenderFluidTank`, `RenderFluidBarrel`, `RenderBigAssTank`, `RenderPuF6Tank`, `RenderUF6Tank`, `RenderBarrel` | `RenderFluidTank`, `RenderPipeItem`, `RenderBarrelItem`, `PipeNeoBakedModel` |

**Total `.java` in scope: ORIGINAL = 115, NEO = 61.**

### Sub-package counts (exact)

| Package | ORIG | NEO |
|---|---:|---:|
| `api/hbm/fluid` (MK1 API) | 5 | 0 |
| `api/hbm/fluidmk2` | 14 | 14 |
| `com/hbm/inventory/fluid` (+ `tank`, `trait`) | 20 | 19 |
| `com/hbm/blocks/fluid` → `blocks/fluids` + `fluids` | 18 | 4 |
| `com/hbm/uninos` (+ `networkproviders`) | 16 | 6 |
| Duct/pipe blocks + BEs | 14 | 5 |

Cross-cutting reach, as a proxy for how much of the mod is wired into fluids:

| Metric | ORIG | NEO |
|---|---:|---:|
| Files referencing `FluidTank` | 175 | 56 |
| Files referencing `FluidType` | 175 | 67 |
| Non-API files implementing an `IFluid*MK2` interface | 52 | 21 |
| Non-API hits on `getPressure`/`setPressure`/`HIGHEST_VALID_PRESSURE` | 39 | 6 |

---

## 2. Per-feature status

### 2.1 Fluid network API — `api/hbm/fluidmk2` (FULLY PORTED)

All 14 files present, and diffs are pure API-translation (`World`→`Level`, `ForgeDirection`→`Direction`, `int x,y,z`→`BlockPos`, `PacketDispatcher.wrapper.sendToAllAround`→`PacketDistributor.sendToPlayersNear`, tabs→spaces). Algorithms are byte-for-byte equivalent.

| File | ORIG loc | NEO loc | Status | Notes |
|---|---:|---:|---|---|
| `FluidNetMK2.java` | 152 | 152 | **Full** | Pressure-banded provider/receiver setup, weighted priority distribution, `notAccountedFor` scapegoat loop (100 iterations), `cleanUp()` — all identical |
| `FluidNode.java` | 19 | 19 | **Full** | |
| `IFluidUserMK2.java` | 15 | 13 | **Full** | `HIGHEST_VALID_PRESSURE = 5`, `DEFAULT_PRESSURE_RANGE {0,0}` preserved |
| `IFluidProviderMK2.java` | 12 | 11 | **Full** | |
| `IFluidReceiverMK2.java` | 68 | 64 | **Full** | `trySubscribe` + particle debug ported; return type changed from `EnumTransferAction` to `void` (see 2.9) |
| `IFluidStandardSenderMK2.java` | 136 | 131 | **Full** | `tryProvide`, `getFluidAvailable`, `useUpFluid` round-robin drain — identical |
| `IFluidStandardReceiverMK2.java` | 68 | 69 | **Full** | |
| `IFluidStandardTransceiverMK2.java` | 5 | 2 | **Full** | |
| `IFluidConnectorMK2.java` | 17 | 14 | **Full** | |
| `IFluidConnectorBlockMK2.java` | 12 | 11 | **Full** | |
| `IFluidPipeMK2.java` | 27 | 25 | **Full** | `createNode` with 6 `DirPos` connections preserved |
| `IFillableItem.java` | 21 | 19 | **Full** | |
| `IFluidRegisterListener.java` | 9 | 8 | **Full** | |
| `package-info.java` | 23 | 8 | Partial | Doc comment trimmed; cosmetic |

`BlockEntityAccessCache` (NEO) replaces `TileAccessCache` (ORIG) and is present.

### 2.2 Legacy MK1 fluid API — `api/hbm/fluid` (NOT PORTED, probably correct)

`IFluidConnector`, `IFluidConnectorBlock`, `IFluidStandardReceiver`, `IFluidStandardSender`, `IFluidStandardTransceiver` — 5 files, ~118 non-API references in the 1.7.10 tree. The NEO tree has **zero** references to `api.hbm.fluid.*`. This is a deliberate simplification: everything the port ported was moved onto MK2. Downstream machines that still used MK1 in 1.7.10 will need MK2 conversion as they are ported, but the MK1 classes themselves do not need to exist.

### 2.3 `FluidType` (FULLY PORTED, two small deletions)

`com/hbm/inventory/fluid/FluidType.java` — 292 → 280 loc. Everything ported: numeric ID + forced-ID safeguard (Bob's dementia exception verbatim), string ID, color, GUI tint, poison/flammability/reactivity, `EnumSymbol`, `temperature`/`ROOM_TEMPERATURE`, container map, trait map, custom-fluid and foreign-fluid constructors, `onTankBroken`/`onTankUpdate`/`onFluidRelease`, `addInfo` shift-to-expand tooltip, `NETWORK_PROVIDER`, all deprecated compat delegates.

Deletions:
- **`getDict(int quantity)` removed** — this produced the `containerNmB<fluid>` / `ntmcontainerN<fluid>` Ore Dictionary keys. Dropped because OreDictionary no longer exists; no tag-based replacement was written (see 2.7).
- **`getConditionalName()` removed**; `getLocalizedName()`→`getName(): Component`, `getName()`→`getInternalName()`. Sensible 1.21 signature change, but every call site had to be fixed and some string-formatted tooltips lost formatting fidelity.

### 2.4 `Fluids` registry (FULLY PORTED, 1 fluid + toxin wiring missing)

`com/hbm/inventory/fluid/Fluids.java` — 1050 → 1041 loc. 157 → 156 declared `FluidType` fields; every numeric ID and every `addTraits(...)`/`addContainers(...)` chain is preserved, including the JSON trait config (`hbmFluidTraits.json`) serialize/deserialize and the fluid-rename/migration machinery.

Missing / changed:

| Item | Status |
|---|---|
| `ACID` (deprecated alias for `PEROXIDE`, used by JAOPCA) | **Removed** |
| `renameMapping` HashBiMap + `renameMapping.put("ACID", PEROXIDE)` | **Removed** — old-world fluid renames will not migrate |
| `FT_Toxin` trait application to `CHLORINE`, `PHOSGENE`, `MUSTARDGAS`, `ESTRADIOL`, `REDMUD` | **Removed** (trait class absent) |
| `FLUE` soot release multiplier | Changed `SOOT_GAS * 5` → `SOOT_GAS * 25` (deliberate rebalance, not a gap) |
| `metaOrder` build | Refactored into `addMetaOrderIfAbsent` + `syncMetaOrderWithMappings` — functionally equivalent or better |

### 2.5 Fluid traits — `com/hbm/inventory/fluid/trait` (19 of 20, 2 stubs)

| Trait | ORIG loc | NEO loc | Status | What is missing |
|---|---:|---:|---|---|
| `FluidTrait` (base + registry) | 75 | 70 | **Partial** | **`FluidTrait.onRelease(world,x,y,z,type,tank,release,mB)` static dispatcher deleted.** This is the single entry point every machine used to fire *all* traits on a spill/burn. NEO only has `FT_Polluting.pollute(...)`, which fires pollution only — `FT_VentRadiation` and any future release-reacting trait never fire. Also `registerTrait("toxin", …)` is commented out, and `heatable`/`coolable` are registered twice (harmless duplicate, `traitList` gets duplicate entries → tooltips can print heat info twice). |
| `FluidTraitSimple` (10 tag traits) | 68 | 68 | **Full** | `FT_Gaseous`, `FT_Gaseous_ART`, `FT_Liquid`, `FT_Viscous`, `FT_Plasma`, `FT_Amat`, `FT_LeadContainer`, `FT_Delicious`, `FT_NoID`, `FT_NoContainer`, `FT_Unsiphonable` all present |
| `FT_Combustible` | 74 | 75 | **Full** | `FuelGrade` enum + energy preserved |
| `FT_Flammable` | 47 | 43 | **Full** | |
| `FT_Heatable` | 129 | 134 | **Full** | Multi-step heating, per-`HeatingType` efficiency (BOILER/HEATEXCHANGER/PWR/ICF/PA), JSON round-trip all intact; `hasSteps()` added |
| `FT_Coolable` | 95 | 81 | **Partial** | `addInfoHidden` is `// todo` — thermal-capacity and per-`CoolingType` efficiency tooltip gone; `CoolingType.getLocalizedName()` deleted. Mechanics intact. |
| `FT_PWRModerator` | 45 | 41 | **Partial** | `addInfo`/`addInfoHidden` are both `//todo` — the flux-multiplier tooltip is gone. Mechanic (`multiplier`) intact. |
| `FT_Corrosive` | 50 | 48 | **Full** | (cosmetic: "highly corrosive" now GOLD instead of YELLOW/GOLD split) |
| `FT_Poison` | 46 | 45 | **Full** | |
| `FT_Polluting` | 94 | 108 | **Full+** | Gains static `pollute(level,pos,type,release,mB)` helper |
| `FT_VentRadiation` | 48 | 49 | **Full** | but see `FluidTrait.onRelease` above — nothing calls it any more |
| `FT_Pheromone` | 44 | 44 | **Full** | |
| **`FT_Toxin`** | **212** | **absent** | **MISSING** | Entire toxin framework: `ToxinDirectDamage`, `ToxinEffects`, `HazardClass` gating (`GAS_LUNG`, `GAS_BLISTERING`, `PARTICLE_FINE`), gas-mask/armor interaction, JSON ser/deser. Chlorine, phosgene, mustard gas, estradiol and red mud are now inert. |

### 2.6 Fluid tanks — `com/hbm/inventory/fluid/tank` (FULLY PORTED)

| File | ORIG | NEO | Status |
|---|---:|---:|---|
| `FluidTank.java` | 292 | 289 | **Full** — fill/max/type/pressure, NBT read/write with legacy name-compat path, byte-buf serialize/deserialize, `loadTank`/`unloadTank`/`setType` (now `NonNullList<ItemStack>` + `Level`), `renderTank` reimplemented on `Tesselator`/`BufferBuilder`/`RenderSystem` |
| `FluidLoadingHandler.java` | 9 | 10 | **Full** |
| `FluidLoaderStandard.java` | 101 | 102 | **Full** — damage-as-subtype replaced by `MetaHelper.getMeta`, display name by `DataComponents.CUSTOM_NAME` |
| `FluidLoaderInfinite.java` | 46 | 39 | **Full** |
| `FluidLoaderFillableItem.java` | 74 | 71 | **Full** — armor-mod pry-through preserved |

Regressions inside `FluidTank`:
- `implements Cloneable` dropped.
- `renderTankInfo` → `renderTankTooltip` now calls `guiGraphics.renderComponentTooltip` instead of `GUIElements.drawHoveringTextFluid`. The custom fluid tooltip (hazard symbol / `EnumSymbol` rendering, fluid-colored frame) is gone.

### 2.7 Fluid container registry (HEAVILY REDUCED)

`com/hbm/inventory/FluidContainerRegistry.java` — 190 → 108 loc.

Only three container families are still registered, in a single loop: `fluid_tank_lead_full`, `fluid_tank_full`, `fluid_barrel_full`.

**Removed registrations** (every one of these is a gameplay item that can no longer be filled/emptied):

- Vanilla: water bucket, potion bottle (250 mB), lava bucket, experience bottle
- Mod buckets: `bucket_mud` (WATZ), `bucket_schrabidic_acid`, `bucket_sulfuric_acid`
- Barrel blocks as containers: `red_barrel`(DIESEL 10k), `pink_barrel`(KEROSENE 10k), `lox_barrel`(OXYGEN 10k)
- Ore blocks as fluid sources: `ore_oil` (OIL 250), `ore_gneiss_gas` (PETROLEUM 50/250 by config)
- Cells: `cell_deuterium`, `cell_tritium`, `cell_uf6`, `cell_puf6`, `cell_antimatter`, `cell_anti_schrabidium`, `cell_sas3`
- `bottle_mercury`, `ingot_mercury` (125 mB)
- `rod_zirnox_tritium` (2000 mB)
- Particle containers: `particle_hydrogen`, `particle_amat`, `particle_aschrab`
- IV bags: `iv_blood`, `iv_xp`
- `can_mug`
- **`CD_Canister` loop** — `canister_full`/`canister_empty` per fluid
- **`CD_Gastank` loop** — `gas_full`/`gas_empty` per fluid
- **Dispersable loop** — `disperser_canister` (2000 mB), `glyphid_gland` (4000 mB)
- `Compat.registerCompatFluidContainers()`

**Removed API methods:** `getContainers(FluidType)`, `getContainer(FluidType, ItemStack)`, `getFluidType(ItemStack)`.
**Removed:** `OreDictionary.registerOre(con.type.getDict(...), con.fullContainer)` — the whole `containerNmB<fluid>` ore-dict contract that recipes and other mods consumed. No `TagKey` equivalent was written.

`FluidContainer.java` (25 → 21 loc) is fine (null → `ItemStack.EMPTY`).

**Also missing: any NeoForge `IFluidHandler` / `FluidStack` capability bridge.** NEO's `com.hbm.inventory.FluidStack` is HBM's own class, not `net.neoforged.neoforge.fluids.FluidStack`. There is not a single reference to `IFluidHandler`, `Capabilities.FluidHandler`, or NeoForge `FluidStack` anywhere in the port — so NTM fluids cannot interoperate with any other mod's tanks or pipes, and vanilla buckets can no longer be used at all (see the container list above).

### 2.8 Node network core — `com/hbm/uninos` (CORE FULL, one real regression, 5 providers missing)

| File | Status | Notes |
|---|---|---|
| `UniNodespace.java` | **Partial** | Ported to `Level`/`BlockPos`/`MinecraftServer.getAllLevels()`. **The network reaper is deleted**: `reapTimer`, `updateReapTimer()`, the 5-minute `links.removeIf(expired)` sweep and the `activeNodeNets.removeIf(links.isEmpty())` empty-network purge are all gone. Expired nodes and orphaned nets are never collected → unbounded growth over a long session. |
| `NodeNet.java` | **Full** | `ILoadedTile`→`ILoadedBE`, `isInvalid()`→`isRemoved()`, `Random`→`RandomSource` |
| `GenNode.java` | **Full** | |
| `INetworkProvider.java` | **Full** | |
| `networkproviders/FluidNetProvider.java` | **Full** | import reorder only |
| `networkproviders/PowerNetProvider.java` | **Full** | |
| `FoundryNetwork` / `FoundryNetworkProvider` | **MISSING** | molten-metal foundry transport network |
| `KlystronNetwork` / `KlystronNetworkProvider` | **MISSING** | |
| `PlasmaNetwork` / `PlasmaNetworkProvider` | **MISSING** | |
| `PneumaticNetwork` / `PneumaticNetworkProvider` | **MISSING** | |
| `RebarNetwork` / `RebarNetworkProvider` | **MISSING** | |

The foundry network in particular is a fluid-adjacent subsystem (molten metal channels, basins, molds, outlets, slagtaps, `IRenderFoundry`, `ParticleFoundry`) and is **entirely absent** — 13 original classes, 0 ported.

### 2.9 Pipes and ducts (1 of 7 variants)

| Original | NEO counterpart | Status |
|---|---|---|
| `FluidDuctBase` (146) | `FluidDuctBaseBlock` (87) | **Partial** — fluid-ID right-click retyping and `changeTypeRecursively` (64-deep flood) ported. Lost: metadata-driven duct tiers/rendering hooks, `IBlockMulti` sub-item handling now moved to `FluidDuctConnectingBlock` |
| `FluidDuctStandard` (254) | `FluidDuctStandardBlock` (131) + `FluidDuctConnectingBlock` (111) | **Ported** — connection state now `BlockStateProperties.{NORTH..DOWN}` + custom `META` int property; all 6 collision/outline shape cases reproduced; `ILookOverlay` printHook ported |
| `FluidDuctBox` (326) | — | **MISSING** — clad/boxed duct, `IBlockMulti`, its own `RenderBoxDuct` (292 loc) |
| `FluidDuctBoxExhaust` (86) | — | **MISSING** |
| `FluidDuctGauge` (194, incl. `TileEntityPipeGauge`) | — | **MISSING** — inline fill gauge, `IBlockMultiPass`, `INBTBlockTransformable`, OpenComputers component, RTTY value provider |
| `FluidDuctPaintable` (233, incl. `TileEntityPipePaintable`) | — | **MISSING** — paintable ducts, `IToolable`, `ICopiable` |
| `FluidDuctPaintableBlockExhaust` (221) | — | **MISSING** |
| `FluidPipeAnchor` (121) + `TileEntityPipeAnchor` (44) + `RenderPipeAnchor` | — | **MISSING** |
| `FluidPump` (blocks/network, incl. `TileEntityFluidPump`) | — | **MISSING** — in-line pump, `IControlReceiver` GUI, buffered capacity |
| `TileEntityPipeBaseNT` (165) | `PipeBaseBlockEntity` (153) | **Full** — node create/destroy, `canConnect`, NBT + `RegistryFriendlyByteBuf` sync, `IFluidCopiable` paste-settings with TOOL_CTRL recursive retype |
| `TileEntityPipeExhaust` (56) | — | **MISSING** — exhaust-only pipe variant |
| `TileEntityPipelineBase` (197) | — | **MISSING** — long-distance pipeline node with arbitrary `connected` list (used by buried pipelines / oil transport) |
| `RenderPipe` (128) / `RenderTestPipe` | `PipeNeoBakedModel` + `RenderPipeItem` | **Ported (different approach)** — TESR replaced with a `LevelAwareWavefrontBakedModel` with a 64-entry connection-mask quad cache and a base+overlay sprite. Fluid-color tinting path exists via the overlay sprite. |
| `ItemFluidDuct` (134) | — | **MISSING** — the duct item that carries the fluid-type subtype for placement |

NEO registers exactly one duct block: `NtmBlocks.FLUID_DUCT_NEO` (`"fluid_duct_neo"`).

### 2.10 Pressure subsystem (API PRESENT, ALL MACHINERY MISSING)

The pressure dimension survives end-to-end in the API (`FluidNetMK2` bands transfers across pressures 0–5, `FluidTank.getPressure()`, `getProvidingPressureRange`/`getReceivingPressureRange`, `FluidIconItem` pressure NBT + "Pressurized, use compressor!" tooltip). What is gone is everything that *changes* pressure:

| Original | Status |
|---|---|
| `MachineCompressor` + `TileEntityMachineCompressor` | **MISSING** |
| `MachineCompressorCompact` + `TileEntityMachineCompressorCompact` | **MISSING** |
| `TileEntityMachineCompressorBase` | **MISSING** |
| `ContainerCompressor`, `GUICompressor`, `CompressorRecipes`, NEI `CompressorHandler` | **MISSING** |
| `RenderCompressor`, `RenderCompressorCompact` | **MISSING** |
| `MachinePump` + `TileEntityMachinePumpBase`/`PumpElectric`/`PumpSteam`, `GUIPump`, `RenderPump` | **MISSING** |
| `WatzPump` + `RenderWatzPump` | **MISSING** |
| `MachinePumpjack` / `TileEntityMachinePumpjack` | **PORTED** (`MachinePumpjackBlock`, `MachinePumpjackBlockEntity`, `RenderPumpjack`) |

Non-API pressure references: 39 (ORIG) → 6 (NEO). In practice a player can see pressure on tanks and items but has no way to produce or relieve it.

### 2.11 World fluid blocks (2 of 9 families)

| Original family | Files | NEO | Status |
|---|---|---|---|
| `VolcanicFluid` + `VolcanicBlock` (162) | 2 | `NtmFluids.VOLCANIC_LAVA{,_FLOWING}`, `NtmFluidTypes.VOLCANIC_LAVA_TYPE`, `VolcanicLiquidBlock` (121) | **Ported** — reimplemented on `BaseFlowingFluid` + `LiquidBlock` + NeoForge `FluidType` with custom `move()` |
| `RadFluid` + `RadBlock` (81) | 2 | `NtmFluids.RAD_LAVA{,_FLOWING}`, `RAD_LAVA_TYPE`, `RadLiquidBlock` (64) | **Ported** |
| `AcidFluid` + `AcidBlock` (113) | 2 | — | **MISSING** |
| `CoriumFluid` + `CoriumBlock` (111) + `CoriumFinite` (91) | 3 | — | **MISSING** — corium spread/decay, the core meltdown fluid |
| `MudFluid` + `MudBlock` (190) | 2 | — | **MISSING** |
| `SchrabidicFluid` + `SchrabidicBlock` (135) | 2 | — | **MISSING** |
| `ToxicFluid` + `ToxicBlock` (115) | 2 | — | **MISSING** |
| `GenericFluid` (31) / `GenericFluidBlock` (104) / `GenericFiniteFluid` (52) | 3 | — | **MISSING** — the shared base infra that all of the above used |

(`OilSpillBlock` exists in NEO under `blocks/generic`-ish registration and is not part of `blocks/fluid` in either tree.)

### 2.12 Fluid items

| Original | ORIG loc | NEO | NEO loc | Status |
|---|---:|---|---:|---|
| `IItemFluidIdentifier` | — | `IItemFluidIdentifier` | — | **Full** (signature `getType(Level, BlockPos, ItemStack)`) |
| `ItemFluidIcon` | 111 | `FluidIconItem` | 83 | **Partial** — fill/pressure NBT (now `TagsUtil` custom data), name, `addInfo` ported. Lost: `getSubItems` creative enumeration in nice order, `getColorFromItemStack` fluid tinting (needs an `ItemColor`/tint-index model), two-pass overlay icon |
| `ItemFluidIDMulti` | 176 | `FluidIDMultiItem` | 121 | **Partial** |
| `ItemFluidTank` | 88 | `FluidTankItem` | 19 | **Heavily stubbed** — only `getName` remains. Lost: creative sub-item enumeration honouring `hasNoContainer()`/`needsLeadContainer()`, the two-pass overlay icon (`fluid_tank_overlay`, `fluid_tank_lead_overlay`, `fluid_barrel_overlay`, `fluid_pack_overlay`) and its per-fluid colour tint — full/lead/barrel/pack items now render untinted |
| `ItemInfiniteFluid` | 28 | `InfiniteFluidItem` | 28 | **Full** |
| `ItemCanister` | 88 | — | — | **MISSING** |
| `ItemGasTank` | 93 | — | — | **MISSING** |
| `ItemFluidSiphon` | 99 | — | — | **MISSING** |
| `ItemFluidDuct` | 134 | — | — | **MISSING** |
| `ItemPipette` | 210 | — | — | **MISSING** (also an `IFillableItem`) |
| `MagazineFluid`, `WeaponModCanisters` | — | — | **MISSING** (weapon subsystem) |

`IFillableItem` implementors: 7 in ORIG (`ItemGunChemthrower`, `ItemGunDrill`, `ItemPipette`, `ItemToolAbilityFueled`, `ItemBlowtorch`, `JetpackFueledBase`, `ArmorFSBFueled`) → **1** in NEO (`BlowtorchItem`).

### 2.13 Fluid storage machines

| Original | NEO | Status |
|---|---|---|
| `TileEntityMachineFluidTank` (590) | `MachineFluidTankBlockEntity` (418) | **Partial** — networking, node management, 4 modes, comparator output, `IPersistentNBT`, `IOverpressurable`, `tryExtinguish` (WATER/FOAM/CO2) all ported. **`updateLeak(int)` is gutted**: the antimatter `ExplosionVNT` branch, the flammable branch (entity ignite + `ParticleUtil.spawnGasFlame` + `FluidTrait.onRelease(..., BURN, amount*5)`) and the gaseous branch (the "tower" `AuxParticlePacketNT` plume + `FluidTrait.onRelease(..., SPILL, amount*5)`) are all deleted — a ruptured tank now silently drains with no fire, no gas cloud, no pollution and no radiation. Also dropped: the legacy `getBlockMetadata() < 12` multiblock migration path. |
| `TileEntityBarrel` (420) | `BarrelBlockEntity` (336) | **Partial** — modes, buffer-mode node behaviour, tilting, `checkFluidInteraction` ported. **Missing:** the `barrel_corroded` slow-leak branch (1 mB drip + `FluidTrait.onRelease(SPILL)` + random destruction), the Tom's-firestorm/water-barrel explosion branch, and the `barrel_antimatter` exemption (NEO destroys *any* barrel holding antimatter — `barrel_antimatter` is not registered in `NtmBlocks`, so the exemption was dropped rather than ported). |
| `TileEntityMachineBigAssTank` + `RenderBigAssTank` | — | **MISSING** |
| `TileEntityMachinePuF6Tank` + `RenderPuF6Tank` | — | **MISSING** |
| `TileEntityMachineUF6Tank` + `RenderUF6Tank` | — | **MISSING** |
| `TileEntityStorageDrum` | — | **MISSING** |
| `RenderFluidTank` | `RenderFluidTank` | Ported |
| `RenderFluidBarrel`, `RenderBarrel` | `RenderBarrelItem` | Partial |

Registered barrels in NEO: `BARREL_RED`, `BARREL_PINK`, `BARREL_LOX`, `BARREL_TAINT`, `BARREL_PLASTIC`, `BARREL_STEEL`, `BARREL_CORRODED`, `BARREL_TCALLOY`. Missing vs ORIG: `barrel_antimatter`, `yellow_barrel`, `vitrified_barrel`.

### 2.14 Other fluid-touching infrastructure not ported

| Feature | Files | Status |
|---|---|---|
| Foundry network (molten metal) — basin, casting base, channel, mold, outlet, slagtap, tank, `TileEntityFoundryBase`, `IRenderFoundry`, 5 renderers, `ParticleFoundry`, `CanneryFoundryChannel` | ~20 | **MISSING** |
| `MachineDrain` / `TileEntityMachineDrain` / `RenderDrain` | 3 | **MISSING** |
| `MachineGasFlare` / `TileEntityMachineGasFlare` / `GUIMachineGasFlare` / `ContainerMachineGasFlare` / `RenderGasFlare` | 5 | **MISSING** |
| `CompatFluidRegistry` (`setupForeign` path for other mods' fluids) | 1 | **MISSING** — `FluidType.setupForeign` and `Fluids.foreignFluids` exist but nothing populates them |
| `TileEntityProxyConductor` (fluid proxy for multiblocks) | 1 | **MISSING** |
| NEI fluid handlers (72 NEI handler files) → JEI | — | JEI plugin exists (`NtmJeiPlugin` + ~10 handlers) and does render fluids via HBM's own `FluidStack`; no JEI fluid-ingredient type registered |

---

## 3. Architectural blockers for finishing the port

1. **`FluidTrait.onRelease` dispatcher is gone.** Restoring it is the single highest-leverage fix: it is the hook that connects the fluid system to pollution, chunk radiation and (once ported) toxin effects. Every ported and future machine that spills/burns fluid needs it back. Currently `FT_VentRadiation.onFluidRelease` is dead code.
2. **No NeoForge `IFluidHandler` bridge.** NTM's fluid system is entirely self-contained (`long`-based, pressure-banded, `FluidType`-keyed) and does not map cleanly onto `FluidStack`/`IFluidHandler`. A bidirectional adapter is needed for: vanilla buckets, other mods, and the `BlockCapability`/`ItemCapability` registration in `RegisterCapabilitiesEvent`. This is a design decision, not just a port — a 1:1 `FluidType`↔`Fluid` mapping for the 156 fluids plus flowing/source pairs and bucket items is a lot of registry surface.
3. **Metadata-as-subtype → data components.** 1.7.10 encoded the fluid ID in `ItemStack` damage (`new ItemStack(ModItems.fluid_tank_full, 1, id)`), used by `ItemFluidTank`, `ItemFluidIcon`, `ItemCanister`, `ItemGasTank`, `ItemFluidDuct` and the whole container registry. The port uses a shim (`MetaHelper.getMeta/setMeta` over custom data), which works but means creative-tab enumeration, JEI subtype interpreters (`MetaSubtypeInterpreter` exists) and recipe matching all need per-item wiring that mostly has not been written.
4. **Ore Dictionary → tags.** `FluidType.getDict(quantity)` produced `container1000diesel`-style ore-dict keys consumed by recipes and third-party mods. There is no `TagKey<Item>` replacement, so the "any 1000 mB container of X" recipe idiom has no expression in the port.
5. **`IIcon` multi-pass item rendering → item models + tint indices.** `ItemFluidTank`/`ItemFluidIcon` used `requiresMultipleRenderPasses()` + `getIconFromDamageForRenderPass` + `getColorFromItemStack` to draw a grey container with a fluid-coloured overlay. 1.21 needs a two-layer item model with `tintindex` and a registered `ItemColor`, per item. This is why `FluidTankItem` is 19 lines.
6. **TESR → BlockEntityRenderer / baked models.** `RenderPipe`, `RenderBoxDuct`, `RenderPipeAnchor`, `RenderBigAssTank`, `RenderUF6Tank`, `RenderPuF6Tank`, `RenderFoundry*` are all `Tessellator`-based TESRs. The port's chosen answer for ducts is a custom `BakedModel` (`PipeNeoBakedModel`) with a 64-state connection cache — good, but each remaining duct/tank variant needs its own model or BER, plus `ModelEvent.RegisterAdditional` / `RegisterGeometryLoaders` wiring.
7. **Metadata blocks → blockstates.** `FluidDuctBox`, `FluidDuctGauge`, `FluidDuctPaintable` are `IBlockMulti`/`IBlockMultiPass` with metadata tiers and multi-pass rendering. The port's `FluidDuctConnectingBlock` shows the pattern (6 boolean connection properties + a custom `META` `IntegerProperty` + `IMultiBlock`), but each variant needs its own blockstate JSON, model set, loot table and `getStateForPlacement`.
8. **`ForgeDirection.UNKNOWN` has no `Direction` equivalent.** `TileEntityPipelineBase` builds its node with `DirPos(x,y,z, ForgeDirection.UNKNOWN)` for omnidirectional pipeline links. NEO's `canConnect` uses `dir != null` as the stand-in, but the pipeline class itself is unported and this idiom needs a deliberate answer.
9. **Custom packets → NeoForge payloads.** The particle-debug path and the gas-plume/leak effects used `PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(...))`. The port has `AuxParticle` + `PacketDistributor.sendToPlayersNear` and it works in the API classes — but the machine-side leak effects that used it were deleted rather than converted.
10. **Nodespace reaper regression.** `UniNodespace.updateNodespace` lost the periodic sweep. Any long-running world will accumulate expired `GenNode`s and empty `NodeNet`s. Fixing it is cheap and should be done before more machines are ported onto the network.
11. **Duplicate trait registration.** `FluidTrait` static block registers `heatable`/`coolable` twice, so `traitList` contains duplicates and tooltips iterate them twice. Cheap fix, but it will silently double-print once the `//todo` tooltips are filled in.
12. **Missing sibling networks.** `FoundryNetwork`, `PlasmaNetwork`, `PneumaticNetwork`, `KlystronNetwork`, `RebarNetwork` all extend the same `NodeNet`/`INetworkProvider` core that *is* ported. They are unblocked work, not blocked work — but they are a large chunk of the remaining fluid-adjacent surface.
13. **`FT_Toxin` depends on the armor/hazard subsystem.** `HazardClass` (`GAS_LUNG`, `GAS_BLISTERING`, `PARTICLE_FINE`), `ArmorRegistry`, gas-mask filters and `HbmPotion.death` must exist before the toxin trait can be restored meaningfully.

---

## 4. Honest coverage assessment

| Area | Weight (rough) | Coverage |
|---|---:|---:|
| MK2 network API (`api/hbm/fluidmk2`) | 12% | ~100% |
| `FluidType` + `Fluids` registry (156/157 fluids, JSON config) | 18% | ~95% |
| Fluid traits | 12% | ~80% (FT_Toxin absent, 2 tooltip stubs, `onRelease` dispatcher gone) |
| `FluidTank` + loaders | 10% | ~95% |
| Nodespace / `NodeNet` core | 6% | ~85% (reaper regression) |
| Pipes / ducts | 14% | ~30% (1 of 7 block variants, no pipeline/anchor/exhaust/gauge/paintable/box/pump) |
| Pressure machinery | 8% | ~5% (API only, no compressor, no pump) |
| Container registry + fluid items | 10% | ~30% |
| World fluid blocks | 6% | ~22% (2 of 9 families) |
| Fluid storage machines | 4% | ~35% (2 of 6, both with gutted leak/interaction logic) |

**Weighted functional coverage: ~45%.**

The core is in excellent shape — the registry, trait framework, tank, and the pressure-banded network solver are essentially line-for-line faithful and would pass a behavioural diff. The *periphery* is where the port is thin: the things a player actually touches (ducts other than the plain one, compressors, pumps, canisters, gas tanks, pipettes, siphons, buckets, corium/acid/mud/schrabidic/toxic world fluids, tank rupture effects) are largely absent, and the two safety-net integrations (trait release dispatch, nodespace reaping) have silent regressions.

---

## 5. Effort estimate

For one experienced Minecraft modder already fluent in both 1.7.10 Forge and 1.21.1 NeoForge, working on this subsystem alone:

| Work item | Days |
|---|---:|
| Restore `FluidTrait.onRelease` dispatcher + fix duplicate trait registration + fill the two `//todo` tooltips | 1 |
| Restore `UniNodespace` reaper | 0.5 |
| Port `FT_Toxin` (212 loc) + `HazardClass`/armor gating hookup | 3 |
| Complete `FluidContainerRegistry` (all removed registrations) + tag-based replacement for `getDict` | 3 |
| `ItemCanister`, `ItemGasTank`, `ItemFluidSiphon`, `ItemPipette`, `ItemFluidDuct` + two-layer tinted item models for the tank/icon/barrel items | 5 |
| NeoForge `IFluidHandler`/`FluidStack` bidirectional capability bridge (design + 156-fluid mapping + bucket items) | 6 |
| Duct variants: box, box-exhaust, gauge, paintable, paintable-exhaust — blocks + BEs + blockstates + baked models | 8 |
| Pipe anchor, exhaust pipe, `TileEntityPipelineBase` + in-line `FluidPump` | 4 |
| Compressor + compact compressor + recipes + GUI/menu/screen + JEI handler + renderers | 6 |
| Pumps (electric/steam/Watz) + GUIs + renderers | 4 |
| World fluid blocks: generic base infra on `BaseFlowingFluid`/`LiquidBlock`, then acid, corium (+finite), mud, schrabidic, toxic | 7 |
| Remaining tank machines: BigAssTank, UF6, PuF6, storage drum + their BERs | 5 |
| Tank/barrel leak-fire-gas-plume effects, corroded-barrel drip, antimatter exemption, firestorm interaction | 2 |
| Testing, world-load migration checks, fluid-ID stability verification | 3 |

**Total: ~48 person-days.** Call it **45** as a planning figure if the `IFluidHandler` bridge is descoped to "NTM-internal only".

Explicitly *not* costed here (belongs to other subsystems but blocks full fluid parity): the foundry network (~20 files, +8 days), gas flare, drain, `MachineGasFlare` GUI, and the ~31 fluid-using machines that exist in 1.7.10 but not in the port.

---

## 6. Appendix — key file paths

**Original:**
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/api/hbm/fluidmk2/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/api/hbm/fluid/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/inventory/fluid/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/inventory/fluid/trait/FT_Toxin.java`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/blocks/fluid/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/blocks/network/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/tileentity/network/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/uninos/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710/src/main/java/com/hbm/inventory/FluidContainerRegistry.java`

**Port:**
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/api/hbm/fluidmk2/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/inventory/fluid/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/inventory/fluid/trait/FluidTrait.java`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/fluids/NtmFluids.java`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/blocks/fluids/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/blocks/network/`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/blockentity/network/PipeBaseBlockEntity.java`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/uninos/UniNodespace.java`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/inventory/FluidContainerRegistry.java`
- `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo/src/main/java/com/hbm/render/model/PipeNeoBakedModel.java`
