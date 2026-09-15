# Gap Analysis — Energy Subsystem (HE power, UNINOS, energymk2, batteries, cables, transformers, RTGs, generators)

**Original:** `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710` (MC 1.7.10, Forge)
**Port:** `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo` (MC 1.21.1, NeoForge)
All paths below are relative to `src/main/java/` in the respective tree.

---

## 0. Headline numbers

| Metric | Original (1.7.10) | NEO (1.21.1) | Ratio |
|---|---|---|---|
| `.java` files in subsystem | **183** | **46** | 25.1 % |
| LOC in subsystem | **23 219** | **4 748** | 20.4 % |
| Whole-mod `.java` files (context) | 3 477 | 981 | 28.2 % |
| `IEnergyProviderMK2` implementors (generators/storage) | 22 | 4 | 18 % |
| `IEnergyReceiverMK2` implementors (consumers) | 74 | 15 | 20 % |
| `IEnergyConductorMK2` implementors (cables/batteries) | 4 | 3 | 75 % |
| `IEnergyConnectorBlock` implementors (TE-less connect) | 4 | **0** | 0 % |
| `IBatteryItem` implementors (battery items) | 10 | 3 | 30 % |
| UNINOS network providers | 12 files (6 net types) | 2 files (2 net types) | 17 % |

**Note on the hints:** `api/hbm/energy` (Nodespace MK1) **does not exist** in the 1.7.10 tree — `api/hbm/energymk2` is the whole public energy API in both trees. `com/hbm/tileentity/network` in the original is a *mixed* package (power + fluid ducts + cranes + conveyors + drones + radio torches); only the power-relevant members are counted here.

---

## 1. Exact file inventory

### 1.1 Group A — Core energy API (`api/hbm/energymk2/`) — 10 files / 559 LOC orig

| File | orig LOC | NEO LOC | Status |
|---|---|---|---|
| `IBatteryItem.java` | 43 | 46 | **Full** (NBT → `TagsUtil.putCustomData` data component; tag renamed `charge` → `Charge`) |
| `IEnergyConductorMK2.java` | 23 | 25 | **Full** |
| `IEnergyConnectorBlock.java` | 24 | 19 | **Full** (interface only — no implementors exist in NEO) |
| `IEnergyConnectorMK2.java` | 16 | 13 | **Full** (`ForgeDirection.UNKNOWN` → `null`) |
| `IEnergyHandlerMK2.java` | 29 | 20 | **Partial** — `provideInfoForECMK2()` (Energy Control compat) dropped |
| `IEnergyProviderMK2.java` | 76 | 67 | **Full** — logic 1:1; `EnumTransferAction` return value dropped (now `void`) |
| `IEnergyReceiverMK2.java` | 98 | 96 | **Full** — logic 1:1; `EnumTransferAction` return dropped |
| `Nodespace.java` | 62 | 64 | **Full** |
| `PowerNetMK2.java` | 156 | 154 | **Full** — line-for-line port incl. priority buckets, weighted distribution, rounding-error scapegoat loop, `sendPowerDiode()` |
| `package-info.java` | 32 | 32 | Full |

This is the strongest part of the port. `PowerNetMK2.update()` is a literal transliteration: provider polling with 3 s timeout, `isBadLink` culling, 5-tier `ConnectionPriority` demand buckets, weighted allocation, and the random-provider surplus reclaim loop are all present and identical.

Supporting infra outside the subsystem file list but required and **ported**: `api/hbm/blockentity/ILoadedBE.java` contains `BlockEntityAccessCache` (port of `api/hbm/tile/ILoadedTile.TileAccessCache`), `com/hbm/lib/Library.canConnect()`, `Library.chargeItemsFromTE()`, `Library.chargeTEFromItems()`, `com/hbm/util/fauxpointtwelve/DirPos`.

### 1.2 Group B — UNINOS (`com/hbm/uninos/`) — 16 files / 747 LOC orig, 6 files in NEO

| File | orig | NEO | Status |
|---|---|---|---|
| `GenNode.java` | 55 | 43 | **Partial** — `setStandardConnections(x,y,z)` helper dropped |
| `INetworkProvider.java` | 10 | 5 | Full |
| `NodeNet.java` | 92 | 89 | **Full** — `joinNetworks`/`joinLink`/`forceJoinLink`/`leaveLink`/`destroy`/`isBadLink` all 1:1 |
| `UniNodespace.java` | 158 | 148 | **Partial — see §3.1** (reaper removed) |
| `networkproviders/PowerNetProvider.java` | 13 | 11 | Full |
| `networkproviders/FluidNetProvider.java` | 20 | 18 | Full (fluid subsystem, listed for completeness) |
| `networkproviders/PneumaticNetwork.java` (296) + `PneumaticNetworkProvider.java` | 309 | — | **Missing** |
| `networkproviders/FoundryNetwork.java` + `FoundryNetworkProvider.java` | 24 | — | **Missing** |
| `networkproviders/KlystronNetwork.java` + `KlystronNetworkProvider.java` | 22 | — | **Missing** |
| `networkproviders/PlasmaNetwork.java` + `PlasmaNetworkProvider.java` | 22 | — | **Missing** |
| `networkproviders/RebarNetwork.java` + `RebarNetworkProvider.java` | 22 | — | **Missing** |

Nodespace tick hook is wired: `com/hbm/main/NtmEventHandler.onServerTick(ServerTickEvent.Pre)` → `UniNodespace.updateNodespace(event.getServer())`, mirroring `ModEventHandler.onServerTick` Phase.START in the original.

### 1.3 Group C — Power transmission blocks & tiles — 37 files / 3 930 LOC orig

| Original file | LOC | NEO counterpart | Status |
|---|---|---|---|
| `com/hbm/tileentity/network/TileEntityCableBaseNT.java` | 51 | `com/hbm/blockentity/network/CableBaseBlockEntity.java` (53) | **Full** — node create/reuse/destroy 1:1 |
| `com/hbm/blocks/network/BlockCable.java` | 90 | `com/hbm/blocks/network/CableBlock.java` (107) | **Full** — metadata/AABB → 6 `BooleanProperty` blockstate + `VoxelShape`; `CableBakedModel` + `RenderCableItem` for visuals |
| `com/hbm/blocks/network/BlockCablePaintable.java` | 214 | — | **Missing** (paintable cable, 16 colours) |
| `com/hbm/blocks/network/BlockCableGauge.java` | 189 | — | **Missing** (in-line power gauge w/ overlay readout) |
| `com/hbm/blocks/network/BlockOpenComputersCablePaintable.java` | 360 | — | **Missing** (OC-mod integration; arguably N/A on 1.21) |
| `com/hbm/blocks/network/PowerCableBox.java` | 270 | — | **Missing** (full-block cable "box" variant) |
| `com/hbm/blocks/network/WireCoated.java` | 46 | — | **Missing** (coated red wire) |
| `red_cable_classic` (rendered by `RenderCableClassic`) | — | — | **Missing** (only `RED_CABLE` is registered in `NtmBlocks`) |
| `com/hbm/blocks/network/CableSwitch.java` + `TileEntityCableSwitch.java` | 87 | — | **Missing** (redstone-gated network break) |
| `com/hbm/blocks/network/CableDetector.java` | 67 | — | **Missing** (comparator output of net load) |
| `com/hbm/blocks/network/CableDiode.java` (incl. inner `TileEntityDiode`) | 227 | — | **Missing** — throughput limiter + one-way diode + `ConnectionPriority` selector + GUI; this is the mod's "transformer" in gameplay terms |
| `com/hbm/inventory/gui/GUIDiode.java` | — | — | **Missing** |
| `com/hbm/tileentity/network/TileEntityConnector.java` | 43 | — | **Missing** |
| `com/hbm/tileentity/network/TileEntityConnectorSuper.java` | 18 | — | **Missing** |
| `com/hbm/blocks/network/ConnectorRedWire.java` | 67 | — | **Missing** |
| `com/hbm/blocks/network/ConnectorRedWireSuper.java` | 62 | — | **Missing** |
| `com/hbm/tileentity/network/TileEntityPylonBase.java` | 199 | — | **Missing** — long-range wire linking, per-node dye colour, `canConnect(first,second)` length/type validation |
| `com/hbm/tileentity/network/TileEntityPylon.java` | 58 | — | **Missing** |
| `com/hbm/tileentity/network/TileEntityPylonMedium.java` | 59 | — | **Missing** |
| `com/hbm/tileentity/network/TileEntityPylonLarge.java` | 40 | — | **Missing** |
| `com/hbm/blocks/network/PylonBase.java` / `PylonMedium.java` / `PylonLarge.java` / `PylonRedWire.java` | 282 | — | **Missing** (7 registered pylon blocks incl. the two "transformer" medium variants) |
| `com/hbm/tileentity/network/TileEntitySubstation.java` + `blocks/network/Substation.java` | 159 | — | **Missing** |
| `com/hbm/tileentity/network/TileEntityConverterHeRf.java` | 125 | — | **Missing** (HE → RF) |
| `com/hbm/tileentity/network/TileEntityConverterRfHe.java` | 106 | — | **Missing** (RF → HE) |
| `com/hbm/blocks/machine/BlockConverterHeRf.java` / `BlockConverterRfHe.java` | ~90 | — | **Missing** |
| `com/hbm/blocks/machine/MachineTransformer.java` | 37 | `NtmBlocks.TRANSFORMER` (plain `Block`) | **Full-equivalent** (it is decoration-only in both trees; NEO drops the custom `IIcon` top/side split) |
| `com/hbm/blocks/generic/BlockCableConnect.java` | — | — | **Missing** |
| `com/hbm/blocks/network/MachineBatteryREDD.java` | — | `com/hbm/blocks/network/MachineBatteryREDDBlock.java` | **Full** |
| `com/hbm/blocks/network/MachineBatterySocket.java` | — | `com/hbm/blocks/network/MachineBatterySocketBlock.java` | **Full** |
| `com/hbm/tileentity/TileEntityProxyCombo.java` | 569 | `com/hbm/blockentity/ProxyComboBlockEntity.java` (383) | **Partial — see §3.3** |
| `com/hbm/tileentity/TileEntityProxyEnergy.java` | 68 | — | **Missing** (power-only proxy; combo proxy covers most cases) |
| `com/hbm/items/tool/ItemPowerNetTool.java` | 94 | — | **Missing** (debug tool: dumps node/net membership) |
| `com/hbm/commands/CommandReapNetworks.java` | 43 | — | **Missing** (`/reapnetworks` admin command) |

### 1.4 Group D — Generators — 39 files / 8 512 LOC orig

| Generator | Orig TE / Block | NEO | Status |
|---|---|---|---|
| Industrial Turbine ("Chungus") | `TileEntityMachineIndustrialTurbine` (332) + `MachineIndustrialTurbine` | `ChungusBlockEntity` (164) + `MachineChungusBlock` + `RenderChungus` | **Partial — see §3.2** |
| Turbine base class | `TileEntityTurbineBase` (185) | `TurbineBaseBlockEntity` (173) | **Full** (steam-tier lever cycle, `FT_Coolable` conversion, dual tanks, power buffer, NBT, packet all 1:1; EC/ROR info dropped) |
| Wood Burner | `TileEntityMachineWoodBurner` (331) + `MachineWoodBurner` | `MachineWoodBurnerBlockEntity` (368) + `MachineWoodBurnerBlock` + menu/screen/BER | **Full** (solid + wood-oil burn, ash placement, smoke, battery slot charging, control receiver) |
| Steam Turbine (small) | `TileEntityMachineTurbine` + `MachineTurbine` | — | **Missing** |
| Large Turbine | `TileEntityMachineLargeTurbine` + `MachineLargeTurbine` | — | **Missing** |
| Gas Turbine | `TileEntityMachineTurbineGas` + `MachineTurbineGas` | — | **Missing** |
| Turbofan | `TileEntityMachineTurbofan` + `MachineTurbofan` | — | **Missing** |
| Diesel Generator | `TileEntityMachineDiesel` + `MachineDiesel` | — | **Missing** |
| Combustion Engine | `TileEntityMachineCombustionEngine` + `MachineCombustionEngine` | — | **Missing** |
| RTG (`machine_rtg`) | `TileEntityMachineRTG` + `MachineRTG` | — | **Missing** |
| Radiothermal Generator | `TileEntityMachineRadGen` + `MachineRadGen` | — | **Missing** |
| Radiolysis Generator | `TileEntityMachineRadiolysis` + `MachineRadiolysis` (+ `RadiolysisRecipes`, NEI handler) | — | **Missing** |
| Gas Flare | `TileEntityMachineGasFlare` + `MachineGasFlare` | — | **Missing** |
| Steam Engine | `TileEntitySteamEngine` + `MachineSteamEngine` | — | **Missing** |
| Stirling Engine (3 blocks) | `TileEntityStirling` + `MachineStirling` | — | **Missing** |
| Solar Boiler / Solar Mirror | `TileEntitySolarBoiler`, `TileEntitySolarMirror` + blocks | — | **Missing** |
| RTG Furnace / RTG Blast Furnace | `TileEntityRtgFurnace`, `TileEntityDiFurnaceRTG` + blocks | — | **Missing** (deprecated in orig, but registered) |
| Charger | `TileEntityCharger` (146) + `Charger` | — | **Missing** (wireless item charging pad) |
| Fusion MHD Tap | `TileEntityFusionMHDT` | — | **Missing** (fusion subsystem) |
| Core Receiver | `TileEntityCoreReceiver` | — | **Missing** (fusion subsystem) |
| Custom Machine (scripted) | `TileEntityCustomMachine` | — | **Missing** |

**2 of 17** stand-alone HE generators exist in NEO, one of them incomplete.

### 1.5 Group E — Energy storage — 7 files / 2 179 LOC orig

| Original | LOC | NEO | Status |
|---|---|---|---|
| `TileEntityBatteryBase.java` | 274 | `BatteryBaseBlockEntity.java` (213) | **Partial** — mode matrix (`mode_input/buffer/output/none`), `redLow`/`redHigh` redstone modes, `ConnectionPriority` cycling, comparator output, node lifecycle all 1:1. **`getSettings()` returns `null` (bug, §3.4)**; OpenComputers callbacks dropped. |
| `TileEntityBatterySocket.java` | 445 | `BatterySocketBlockEntity.java` (180) | **Partial — see §3.5** (SC-battery arcing/fluctuation mechanic missing; extraction predicate broken) |
| `TileEntityBatteryREDD.java` (FEnSU) | 280 | `BatteryREDDBlockEntity.java` (255) | **Full** — `BigInteger` power, `usePower`/`transferPower` overrides, `IPersistentNBT`, chunk-unload node teardown, delta log (OC dropped) |
| `TileEntityMachineBattery.java` | 441 | — | **Missing** — the classic 5-tier HE battery block (`machine_battery`, `_lithium`, `_schrabidium`, `_dineutronium`, `_potato`) with power graph GUI, ROR, EC info, `IPersistentNBT` |
| `com/hbm/blocks/machine/MachineBattery.java` | — | — | **Missing** |
| `com/hbm/blocks/machine/MachineCapacitor.java` | 393 | — | **Missing** — 5 capacitor blocks implementing `IEnergyConnectorBlock` + `IEnergyReceiverMK2` **at the Block level with no TE**; the only user of `IEnergyConnectorBlock` in the power system |
| `com/hbm/blocks/machine/MachineCapacitorBus.java` | 58 | — | **Missing** |

### 1.6 Group F — Battery / RTG items — 9 files / 862 LOC orig

| Original | NEO | Status |
|---|---|---|
| `ItemBatteryPack.java` (176, 12 subtypes) | `BatteryPackItem.java` (194) | **Full** — all 6 batteries + 6 capacitors, charge/discharge rates, armour-slot use, `RenderBatteryPackItem` |
| `ItemBatterySC.java` (67, 10 isotopes) | `BatterySCItem.java` (63) | **Full** — meta → `MetaHelper` data-component shim |
| `ItemBatteryCreative.java` (18) | `BatteryCreativeItem.java` (22) | **Full** |
| `ItemBattery.java` (146) — generic tiered HE battery item | — | **Missing** — `cube_power`, `battery_potato`, `memory` all instantiate it |
| `ItemPotatos.java` (`battery_potatos`) | — | **Missing** |
| `ItemModBattery.java` (armor_battery_mk2/mk3) | — | **Missing** (armour power-draw modifier) |
| `ItemRTGPellet.java` (184) | `NtmItems.PELLET_RTG` = plain `Item` | **Missing in substance** — heat value, decay-over-time, lifespan, decay product, 11 pellet types all absent |
| `ItemRTGPelletDepleted.java` (`DepletedRTGMaterial`) | — | **Missing** |
| `util/RTGUtil.java`, `tileentity/IRTGUser.java` | — | **Missing** — no RTG heat aggregation / decay handling anywhere in NEO |
| JEI/NEI battery subtypes | `handler/jei/subtypes/BatterySubtypeInterpreter.java` | **Full-equivalent** (NEI → JEI) |

### 1.7 Group G — GUIs, containers, renderers, recipe handlers — 65 files / 6 430 LOC orig

Ported (14 NEO files): `BatteryREDDMenu`/`Screen`, `BatterySocketMenu`/`Screen`, `MachineWoodBurnerMenu`/`Screen`, `RenderBatteryREDD`, `RenderBatterySocket`, `RenderChungus`, `RenderWoodBurner`, `RenderBatteryPackItem`, `RenderCableItem`, `CableBakedModel`, `BatterySubtypeInterpreter`.
`GUIBatterySocket` → `BatterySocketScreen` is a faithful 1:1 port (delta readout, redLow/redHigh mode icons, priority tooltip + click cycling).

Missing (≈51 files): every container/GUI for RTG, RadGen, Diesel, Combustion Engine, Turbine, TurbineGas, Turbofan, Large Turbine, Gas Flare, Radiolysis, RTG Furnace, DiFurnace RTG, **`GUIMachineBattery` (the power-graph battery GUI)**, `GUIDiode`; and every TESR for Pylon/PylonMedium/PylonLarge/PylonBase (incl. the catenary wire rendering), Substation, Charger, RTG, RadGen, Radiolysis, Diesel, Combustion Engine, Steam Engine, Stirling, Turbofan, TurbineGas, BigTurbine, Solar Boiler, Gas Flare; plus `RenderCable`, `RenderCableClassic`, `RenderCapacitor`, `RenderBattery`, `RenderRTGBlock`, `ItemRenderTransformer`; plus the NEI handlers `RTGRecipeHandler`, `RadiolysisRecipeHandler` and `RadiolysisRecipes`.

---

## 2. Feature summary table

| Feature | Ported |
|---|---|
| `energymk2` API (provider/receiver/conductor/connector/battery-item interfaces) | ✅ Full |
| `PowerNetMK2` distribution algorithm (priority buckets, weighted split, surplus reclaim, diode send) | ✅ Full |
| UNINOS node graph (`GenNode`, `NodeNet`, `UniNodespace`, node join/split/merge) | ⚠️ Partial (no reaper) |
| UNINOS power provider | ✅ Full |
| UNINOS pneumatic/foundry/klystron/plasma/rebar providers | ❌ Missing |
| Basic red cable (blockstate + baked model + item render) | ✅ Full |
| Cable variants: classic, paintable, gauge, box, coated wire, OC cable | ❌ Missing |
| Cable switch / detector / diode (transformer) | ❌ Missing |
| Connectors (red wire, super) | ❌ Missing |
| Pylons (small/steel/medium ×4/large) + wire linking + dye colour | ❌ Missing |
| Substation | ❌ Missing |
| HE ↔ RF/FE converters and any FE interop | ❌ Missing |
| Battery base logic (modes, priority, comparator, node) | ⚠️ Partial (`getSettings` bug) |
| Battery socket (portable battery dock) | ⚠️ Partial (SC arcing/fluctuation missing, extraction predicate broken) |
| FEnSU / `machine_battery_redd` (BigInteger storage) | ✅ Full |
| `machine_battery` ×5 tiers + power-graph GUI | ❌ Missing |
| Capacitors ×5 + capacitor bus (`IEnergyConnectorBlock` path) | ❌ Missing |
| Chungus industrial turbine | ⚠️ Partial (flywheel spool-up absent) |
| Turbine base (steam-tier cycling, `FT_Coolable`) | ✅ Full |
| Wood burner | ✅ Full |
| 15 other generators (RTG, RadGen, Diesel, Combustion, Turbine, TurbineGas, Turbofan, LargeTurbine, SteamEngine, Stirling, Radiolysis, GasFlare, SolarBoiler/Mirror, RTG furnaces, Charger) | ❌ Missing |
| Battery items: pack, SC, creative | ✅ Full |
| Battery items: generic `ItemBattery`, potato, cube_power, memory, armour battery mods | ❌ Missing |
| RTG pellet system (heat, decay, lifespan, depleted products, `IRTGUser`) | ❌ Missing |
| Energy proxy (`ProxyCombo` power/conductor delegation) | ⚠️ Partial (`allowDirectProvision` not delegated) |
| `ItemPowerNetTool`, `/reapnetworks` | ❌ Missing |
| `IConfigurableMachine` JSON balance config (34 machines in orig) | ❌ Missing (0 implementors in NEO) |
| OpenComputers / Energy Control / Redstone-over-Radio hooks on energy tiles | ❌ Missing (ROR interfaces exist but energy tiles don't implement them) |

---

## 3. Concrete defects found in the ported code

### 3.1 `UniNodespace` reaper removed (memory leak + stale-net accumulation)
`com/hbm/uninos/UniNodespace.java` (1.7.10) ran a 5-minute `reapTimer`:

```java
if(reapTimer <= 0) {
    activeNodeNets.forEach((net) -> { net.links.removeIf((link) -> ((GenNode) link).expired); });
    activeNodeNets.removeIf((net) -> net.links.size() <= 0); // reap empty networks
}
```

The NEO `updateNetworks()` is only:

```java
for (NodeNet net : activeNodeNets) net.resetTrackers();
for (NodeNet net : activeNodeNets) net.update();
```

Consequences: expired nodes are never purged from `net.links`, and empty `NodeNet`s are never removed from the static `activeNodeNets` set, so the per-tick iteration cost grows monotonically over a session. The companion `/reapnetworks` command (`com/hbm/commands/CommandReapNetworks.java`), which was the manual escape hatch and also cleared `UniNodespace.worlds`, was not ported either. Nothing in NEO clears `UniNodespace.levels` on level unload.

### 3.2 Chungus turbine: flywheel mechanic not ported
Original `TileEntityMachineIndustrialTurbine` overrides `generatePower` and `onServerTick`:

```java
public void generatePower(long power, int steamConsumed) {
    FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);
    double eff = trait.getEfficiency(CoolingType.TURBINE) * getEfficiency();
    int maxOps = (int) Math.ceil((tanks[0].getMaxFill() * consumptionPercent()) / trait.amountReq);
    this.maxPower = (long) (maxOps * trait.heatEnergy * eff);
    this.flywheel_energy += power;
}
public void onServerTick() {
    this.spin = (double) flywheel_energy / FLYWHEEL_MAX_ENERGY;
    this.lastPowerTarget = Math.min((long)(Math.max(this.spin, 0.05) * maxPower), this.flywheel_energy);
    this.flywheel_energy -= this.lastPowerTarget;
    this.powerBuffer = this.lastPowerTarget;
}
```

`ChungusBlockEntity.onServerTick()` is only `turnTimer--; if(operational) turnTimer = 25;`. The fields `spin`, `flywheel_energy`, `maxPower`, `lastPowerTarget`, the constant `FLYWHEEL_MAX_ENERGY`, their NBT persistence, and the `spin` value in `serialize`/`deserialize` are all absent — the turbine outputs full theoretical power the instant steam arrives instead of spooling up. Also missing on the NEO class: `doesResizeCompressor() { return true; }` (so the lever-driven steam-tier cycle in `TurbineBaseBlockEntity.onLeverPull()` will not resize the tanks), `canConnect(FluidType, Direction)` side restriction, and `IConfigurableMachine` (the NEO file carries `//Configurable values todo`). `canConnect(Direction)` also differs: orig allows only the side opposite the facing, NEO allows any horizontal side.

### 3.3 `ProxyComboBlockEntity` does not delegate `allowDirectProvision()`
Original:

```java
public boolean allowDirectProvision() {
    if(!power) return false;
    if(getCoreObject() instanceof IEnergyReceiverMK2) return ((IEnergyReceiverMK2)getCoreObject()).allowDirectProvision();
    return true;
}
```

`ProxyComboBlockEntity` has no such override, so it inherits `IEnergyReceiverMK2`'s `default → true`. `BatteryBaseBlockEntity` returns `false` specifically to force battery I/O through the network; a proxy in front of one would let providers push power straight in, bypassing the battery's mode/priority logic. `canConnect(FluidType, Direction)` and `ICrucibleAcceptor` (the `moltenMetal()` builder flag) are also not implemented, leaving `moltenMetal()` inert.

### 3.4 `BatteryBaseBlockEntity.getSettings()` builds a tag then returns `null`

```java
public CompoundTag getSettings(Level level, BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    tag.putShort("redLow", redLow);
    tag.putShort("redHigh", redHigh);
    tag.putByte("priority", (byte) this.priority.ordinal());
    return null;      // <-- should be `tag`
}
```

The blueprint/settings-copy tool (`ICopiable`) therefore copies nothing from any battery; `pasteSettings` is implemented correctly, so the feature is half-broken rather than absent.

### 3.5 `BatterySocketBlockEntity.canTakeItemThroughFace()` compares slot index to a mode constant
Original `canExtractItem(int slot, ItemStack stack, int side)`:

```java
int mode = this.getRelevantMode(false);
if(mode == mode_output && charge == 0) return true;
if(mode == mode_input  && charge == maxCharge) return true;
```

NEO:

```java
public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
    if(stack.getItem() instanceof IBatteryItem batteryItem) {
        if(index == mode_input  && batteryItem.getCharge(stack) == 0) return true;
        if(index == mode_output && batteryItem.getCharge(stack) == batteryItem.getMaxCharge(stack)) return true;
    }
    return false;
}
```

`index` is the inventory slot (always 0 here), not the redstone mode — and the two conditions are additionally swapped relative to the original. Hopper/conveyor auto-swap of charged/empty batteries is broken.

Also missing from the socket: the superconductor-battery subsystem — `scPowerMult` (which multiplies `getPower()`), `hasSCLoaded()`, `fluctuate()`, `pickNewSCTarget()`, `discharge()` (the arcing `EntityBulletBeamBase` electric discharge at 1–3 minute intervals), `explodeDischarge()`, and the `BulletConfig discharge` static. The NEO class keeps the `BATTERY_SC` item working as a pure power source with no hazard.

### 3.6 `EnumTransferAction` return values dropped
`IEnergyProviderMK2.tryProvide` and `IEnergyReceiverMK2.trySubscribe` returned `EnumTransferAction` (`NOTHING` / `CONNECT_NET` / `PROVIDE_DIRECT`) in 1.7.10; NEO returns `void`. Nothing in the current NEO tree consumes it, but any machine ported later that used the result for connection feedback will need the enum restored.

---

## 4. Architectural blockers for finishing the port

1. **`ForgeDirection.UNKNOWN` has no 1.21 equivalent, and UNINOS depends on it.**
   Pylons, substations and connectors register their node connections as `new DirPos(x, y, z, ForgeDirection.UNKNOWN)` — a direction-agnostic link used for long-range wires. NEO's `DirPos` holds a `net.minecraft.core.Direction`, and `UniNodespace.checkConnection` unconditionally calls `revCon.getDir().getStepX()`, while `checkNodeConnection` calls `con.getDir().getOpposite()`. Passing `null` NPEs. Before any pylon/substation/connector can be ported, `DirPos` needs a null-safe sentinel (or a synthetic `UNKNOWN`) threaded through `UniNodespace.checkConnection`, `checkNodeConnection`, `IEnergyReceiverMK2.trySubscribe` and `IEnergyProviderMK2.tryProvide`. **This is the single highest-leverage blocker in the subsystem.**

2. **Metadata-as-subtype for blocks.** `machine_battery` (5 tiers), capacitors (5 tiers), pylons (7 variants) and cables (6 variants) are distinct `Block` instances in 1.7.10 selected by `ModBlocks` fields but frequently sharing one class that branches on `this == ModBlocks.x` (see `BlockCable.getRenderType()`, `MachineTransformer.registerBlockIcons()`). On 1.21 each needs either separate `DeferredBlock` registrations with per-instance `Properties`, or a blockstate property. The NEO tree already picked "one registration per variant" (`NtmBlocks`), so this is mechanical but voluminous.

3. **TE-less `IEnergyConnectorBlock` receivers.** `MachineCapacitor` (393 LOC) implements `IEnergyReceiverMK2` *on the Block object*, storing charge in block metadata rather than a TE. There is no analogue on 1.21 — `Block` instances are singletons and `BlockState` cannot hold a `long`. Capacitors must be re-architected as block entities (or as blockstate-encoded discrete charge levels), which changes their save format and their interaction with `Library.canConnect`. `IEnergyConnectorBlock` currently has zero implementors in NEO, so the whole code path is untested.

4. **TESR → `BlockEntityRenderer` and the pylon catenary.** ~20 energy TESRs are unported. Pylon wire rendering in particular draws dynamic multi-segment catenaries between arbitrary linked positions with per-node dye colour — `Tessellator` immediate-mode with `GL11` state changes must become `VertexConsumer` + `RenderType` + `PoseStack`, and the wire geometry has to be rebuilt as a buffered mesh or a `RenderType.lines()` batch. NEO's `BlockEntityRendererNT` + wavefront/OBJ baked-model infra (`SimpleWavefrontBakedModel`, `LevelAwareWavefrontBakedModel`) exists and is a good foundation, but no wire/catenary primitive is present.

5. **`IIcon` → texture atlas / models for cables.** Done for the base cable via `CableBakedModel` + blockstate booleans; the paintable variants additionally need per-colour tinting (`BlockColor`/`ItemColor` or 16 model variants), and the gauge cable needs a dynamic overlay quad driven by network load.

6. **No RF/FE bridge.** The original bundles `cofh/api/energy/**` and the two converter TEs. On NeoForge the equivalent is `Capabilities.EnergyStorage.BLOCK` + `IEnergyStorage`, registered via `RegisterCapabilitiesEvent`. Nothing in the NEO tree references `IEnergyStorage` at all — this is a from-scratch design task, not a transliteration, and it also determines whether HE machines should expose FE directly instead of needing converter blocks.

7. **Item damage-as-subtype for battery/RTG items.** Partially solved: NEO has `MetaHelper.getMeta(stack)` / `MetaHelper.metaStack(...)` backing an `EnumMultiItem`, and `IBatteryItem` charge now lives in a custom-data component via `TagsUtil.putCustomData`. `ItemRTGPellet`'s per-instance `heat`/`lifespan`/`decayItem` fields plus its NBT lifespan counter will map onto the same shim, but `getDurabilityForDisplay`/`getContainerItem`-driven decay needs rethinking against 1.21's `DataComponents.DAMAGE` and crafting remainders.

8. **Networking.** `IBufPacketReceiver` + `networkPackNT(range)` is already ported onto `RegistryFriendlyByteBuf` + `PacketDistributor` (see `BatteryBaseBlockEntity.serialize/deserialize`), and `AuxParticlePacketNT` → `com/hbm/network/toclient/AuxParticle`. Remaining generators can reuse this; not a blocker, just work. Note the original's `PacketThreading` off-thread packet builder is gone — every ported tile serialises on the server thread now.

9. **NEI → JEI.** `RTGRecipeHandler` and `RadiolysisRecipeHandler` need JEI category reimplementation; `BatterySubtypeInterpreter` shows the pattern is already established.

10. **No JSON machine-config system.** `IConfigurableMachine` (34 implementors in 1.7.10, including the industrial turbine, both converters, diesel, combustion engine and the turbines) has **zero** implementors in NEO. Either port it or accept hardcoded balance; every generator ported from here on inherits this decision.

11. **Multiblock/dummy-block offsets.** Pylons, substation, turbines and the battery socket are `BlockDummyable` multiblocks keyed off `getBlockMetadata() - BlockDummyable.offset`. NEO has `DummyableBlock` with a `FACING` blockstate property and `getDimensions()/getOffset()`, and the ported Chungus/socket use it correctly — so the pattern is proven, but every remaining multiblock generator needs its dimensions/offsets re-derived by hand.

12. **Level/dimension keying of the node map.** `UniNodespace.levels` is a `HashMap<Level, UniNodeWorld>` holding strong references to `Level` objects with no unload hook. On a dedicated server with dynamic dimensions this leaks levels; keying by `ResourceKey<Level>` plus a `LevelEvent.Unload` handler is the correct fix and should land alongside the reaper.

---

## 5. Honest functional coverage

**~22 %.**

Reasoning: the *substrate* — the `energymk2` API and `PowerNetMK2`/UNINOS — is ~95 % complete and is a faithful transliteration, which means "generator → cable → machine" works end-to-end today. But that substrate is only 1 306 of 23 219 subsystem LOC. Above it:

- Transmission: 1 of ~18 registered cable/connector/pylon/substation/diode blocks ≈ **10 %**
- Generation: 2 of 17 generators, one of them missing its defining mechanic ≈ **9 %**
- Storage: 2 of 4 storage tiles (one partial), 0 of 6 capacitor blocks, 0 of 5 battery tiers ≈ **35 %**
- Items: 3 of 7 battery item classes, RTG pellet system ≈ 0 % ≈ **35 %**
- GUI/render: 14 of 65 files, all matching ported content ≈ **20 %**
- Interop (RF/FE, OC, Energy Control, ROR, JSON config): ≈ **0 %**

LOC ratio is 20.4 %; file ratio 25.1 %. Weighting the completed API substrate slightly above its LOC share, and discounting for the five concrete defects in the code that *is* present, **22 %** is the defensible figure.

---

## 6. Effort estimate

**≈ 85 person-days** for an experienced 1.21.1/NeoForge modder already familiar with this codebase.

| Work package | Days |
|---|---|
| Fix `DirPos`/UNINOS direction-agnostic links; restore reaper + level-unload cleanup; `/reapnetworks`; `ItemPowerNetTool` | 5 |
| Fix the five defects in §3 (proxy `allowDirectProvision`, `getSettings`, socket extraction, Chungus flywheel + compressor resize, `EnumTransferAction`) | 4 |
| Cable variants (classic, paintable ×16 colours, gauge, box, coated wire) incl. baked models, blockstates, datagen, item renders | 9 |
| Cable switch, detector, diode/transformer (+ GUI, priority selector), connectors ×2 | 7 |
| Pylons ×7 + substation: node linking, dye colour, wire catenary `BlockEntityRenderer`, link validation, OBJ models | 14 |
| `machine_battery` ×5 tiers + power-graph GUI + capacitors ×6 (re-architected onto block entities) | 10 |
| Generators — fluid/steam family (Turbine, LargeTurbine, TurbineGas, Turbofan, SteamEngine, GasFlare): BEs + menus + screens + BERs + models | 15 |
| Generators — fuel/nuclear family (Diesel, Combustion Engine, RTG, RadGen, Radiolysis + recipes, Stirling, SolarBoiler/Mirror, RTG furnaces, Charger) | 16 |
| RTG pellet system (`ItemRTGPellet` + depleted + `RTGUtil` + `IRTGUser`, decay on data components), `ItemBattery`, potato batteries, armour battery mods | 6 |
| FE/`IEnergyStorage` capability bridge + HE↔RF converter blocks | 4 |
| `IConfigurableMachine` JSON config for energy machines | 3 |
| JEI categories (RTG, Radiolysis), lang/tags/recipes, integration testing | 6 |
| **Total** | **99 → 85 after overlap/reuse** |

Sanity check: 18 500 LOC of remaining 1.7.10 source at a realistic sustained rate of ~220 net LOC/day of *ported, asset-complete, tested* content lands at ~84 days. The two estimates agree.

Risk: the pylon wire rendering and the capacitor re-architecture are the two items most likely to overrun; treat their 14 + 10 days as the widest error bars.

---

## 7. Dependencies — what must land before/with this subsystem

- **Fluids / fluid networks** — `FluidTank`, `FluidType`, `FT_Coolable`, `IFluidStandardTransceiverMK2` are already ported and in use by `TurbineBaseBlockEntity`; the remaining steam generators need no further fluid work, but the diesel/combustion/gas-flare family needs the oil fluid chain complete.
- **Machines framework** — `MachineBaseBlockEntity`, `MenuBase`, `DummyableBlock`/multiblock, `ICopiable`, `IControlReceiver`, `IPersistentNBT`: all present and proven; no blocker.
- **Rendering** — wavefront/OBJ loader and `BlockEntityRendererNT` exist; a line/catenary render primitive does not and is required for pylons.
- **Items / meta shim** — `MetaHelper`, `EnumMultiItem`, `TagsUtil` custom-data are present; RTG pellet decay needs the damage/remainder story settled.
- **Radiation & hazard** — `HazardRegistry` exists but `HazardModifierRTGRadiation`, `HalfLifeType` and `VersatileConfig.rtgDecay()/scaleRTGPower()` are not ported; RTG pellets depend on them.
- **Redstone-over-Radio** — `RTTYSystem` and `IRORValueProvider`/`IRORInteractive` exist in NEO, but no energy tile implements them yet (batteries, socket, turbines all did in 1.7.10).
- **Config** — the `IConfigurableMachine` JSON layer does not exist in NEO at all.

---

## 8. Appendix — full NEO subsystem file list (46 files)

```
api/hbm/energymk2/IBatteryItem.java
api/hbm/energymk2/IEnergyConductorMK2.java
api/hbm/energymk2/IEnergyConnectorBlock.java
api/hbm/energymk2/IEnergyConnectorMK2.java
api/hbm/energymk2/IEnergyHandlerMK2.java
api/hbm/energymk2/IEnergyProviderMK2.java
api/hbm/energymk2/IEnergyReceiverMK2.java
api/hbm/energymk2/Nodespace.java
api/hbm/energymk2/PowerNetMK2.java
api/hbm/energymk2/package-info.java
com/hbm/uninos/GenNode.java
com/hbm/uninos/INetworkProvider.java
com/hbm/uninos/NodeNet.java
com/hbm/uninos/UniNodespace.java
com/hbm/uninos/networkproviders/FluidNetProvider.java
com/hbm/uninos/networkproviders/PowerNetProvider.java
com/hbm/blockentity/network/CableBaseBlockEntity.java
com/hbm/blocks/network/CableBlock.java
com/hbm/blocks/network/MachineBatteryREDDBlock.java
com/hbm/blocks/network/MachineBatterySocketBlock.java
com/hbm/blockentity/ProxyComboBlockEntity.java
com/hbm/blockentity/machine/TurbineBaseBlockEntity.java
com/hbm/blockentity/machine/ChungusBlockEntity.java
com/hbm/blockentity/machine/MachineWoodBurnerBlockEntity.java
com/hbm/blocks/machine/MachineChungusBlock.java
com/hbm/blocks/machine/MachineWoodBurnerBlock.java
com/hbm/blockentity/machine/storage/BatteryBaseBlockEntity.java
com/hbm/blockentity/machine/storage/BatteryREDDBlockEntity.java
com/hbm/blockentity/machine/storage/BatterySocketBlockEntity.java
com/hbm/items/machine/BatteryCreativeItem.java
com/hbm/items/machine/BatteryPackItem.java
com/hbm/items/machine/BatterySCItem.java
com/hbm/inventory/menus/BatteryREDDMenu.java
com/hbm/inventory/menus/BatterySocketMenu.java
com/hbm/inventory/menus/MachineWoodBurnerMenu.java
com/hbm/inventory/screens/BatteryREDDScreen.java
com/hbm/inventory/screens/BatterySocketScreen.java
com/hbm/inventory/screens/MachineWoodBurnerScreen.java
com/hbm/render/blockentity/RenderBatteryREDD.java
com/hbm/render/blockentity/RenderBatterySocket.java
com/hbm/render/blockentity/RenderChungus.java
com/hbm/render/blockentity/RenderWoodBurner.java
com/hbm/render/item/RenderBatteryPackItem.java
com/hbm/render/item/RenderCableItem.java
com/hbm/render/model/CableBakedModel.java
com/hbm/handler/jei/subtypes/BatterySubtypeInterpreter.java
```

Registered energy blocks in `com/hbm/blocks/NtmBlocks.java`: `RED_CABLE`, `MACHINE_BATTERY_SOCKET`, `MACHINE_BATTERY_REDD`, `MACHINE_CHUNGUS`, `MACHINE_WOOD_BURNER`, `TRANSFORMER` (decoration). Registered energy items in `com/hbm/items/NtmItems.java`: `BATTERY_PACK`, `BATTERY_SC`, `BATTERY_CREATIVE`, `BATTERY_SPARK`/`BATTERY_TRIXITE` (inert components), `PELLET_RTG` (inert).
