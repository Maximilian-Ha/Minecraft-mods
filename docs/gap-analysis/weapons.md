# Gap Analysis — Weapons subsystem (guns / ammo / weapon mods / turrets)

**Original:** `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-1710` (MC 1.7.10, Forge)
**Port:** `/tmp/claude-0/-home-user-Minecraft-mods/579c4c53-6990-5b28-a626-53b037820eb9/scratchpad/hbm-neo` (MC 1.21.1, NeoForge)
**Scope:** SEDNA gun framework + all gun/ammo items, weapon modifications, legacy (pre-SEDNA) weapon items, grenades, and the whole turret family (blocks, block entities, GUIs, renderers).
**Method:** file enumeration with `find`/`wc`, then side-by-side content reads of every ported file. Every "missing" claim below was checked with keyword greps across the entire NEO tree (not filename matching), accounting for the `TileEntityFoo → FooBlockEntity` / `com.hbm.tileentity → com.hbm.blockentity` renames.

---

## 1. Headline numbers

| Metric | Original 1.7.10 | NEO 1.21.1 | Coverage |
|---|---|---|---|
| `.java` files in subsystem paths (see §2 for the exact path set) | **242** | **31** | 12.8 % |
| Lines of code (same paths, approx.) | ~24 500 | ~4 100 | 17 % |
| Registered guns (`gun_*` items) | **70** | **4** | 5.7 % |
| Registered `BulletConfig` ammo definitions | **156** | **18** | 11.5 % |
| SEDNA `XFactory*` caliber/weapon families | **24** | **3** (2 with guns) | 12.5 % |
| Gun first-person renderers (`render/item/weapon/sedna`) | **52** + base | **4** + base | 7.7 % |
| Weapon-mod implementations (`sedna/mods/WeaponMod*`) | **33** | **0** | 0 % |
| Magazine implementations (`sedna/mags`) | **8** + iface | **3** + iface | 37 % |
| Orchestras (server-side anim companions) | **52** | **4** | 7.7 % |
| Gun sound events registered | **73** | **16** | 22 % |
| Turret blocks / block entities | **13 / 15** | **0 / 0** | 0 % |
| Turret GUIs + container | **12 + 1** | **0** | 0 % |
| Turret TESRs | **13** | **0** | 0 % |
| Grenade items | **9** | **0** | 0 % |
| Legacy `handler/guncfg` (deprecated NPC bullet system) | **5** | **0** | 0 % |

**Honest functional coverage: ~15 %.** The *framework* is in good shape (~75–85 % of the SEDNA engine core is faithfully ported); the *content* built on it is barely started, and turrets — an entire second half of this subsystem — have not been begun at all.

---

## 2. Exact path set used for the counts

Original (242 `.java`):

| Path | Files |
|---|---|
| `com/hbm/items/weapon/**` (incl. `sedna/**`, `grenade/**`) | 111 |
| `com/hbm/render/item/weapon/**` (incl. `sedna/**`) | 61 |
| `com/hbm/blocks/turret/` | 15 |
| `com/hbm/tileentity/turret/` | 15 |
| `com/hbm/render/tileentity/RenderTurret*.java` | 13 |
| `com/hbm/inventory/gui/GUITurret*.java` + `GUIWeaponTable.java` | 13 |
| `com/hbm/handler/guncfg/` | 5 |
| `com/hbm/inventory/container/ContainerTurretBase.java`, `ContainerWeaponTable.java` | 2 |
| `com/hbm/handler/{BulletConfiguration,BulletConfigSyncingUtil,CasingEjector}.java` | 3 |
| `com/hbm/items/machine/{ItemTurretBiometry,ItemTurretChip}.java` | 2 |
| `com/hbm/items/ItemAmmoEnums.java` | 1 |
| `com/hbm/crafting/WeaponRecipes.java` | 1 |

NEO (31 `.java`): `com/hbm/items/weapon/**` = 26, `com/hbm/render/item/weapon/sedna/` = 5. **Everything else in the table above is 0.**

---

## 3. SEDNA engine core — ported and near-complete

These are genuine, faithful ports (API-adapted, logic preserved). Verified by whitespace-stripped diffs.

| Class (orig → NEO) | Orig LoC | NEO LoC | Verdict |
|---|---|---|---|
| `sedna/ItemGunBaseNT` → `sedna/GunBaseNTItem` | 486 | 492 | **Full.** Whole state machine present: `GunState`, per-receiver timers, jam/wear, draw/inspect/reload, keybind dispatch (`IKeybindReceiver`), `inventoryTick` (was `onUpdate`) + new `tickClient`, recoil statics, smoke nodes, `renderHUD`. NBT moved to `DataComponents` custom data via `TagsUtil`. |
| `sedna/GunConfig` | 166 | 165 | **Full** — byte-for-byte equivalent after import/format normalisation. |
| `sedna/Receiver` | 168 | 169 | **Full** minus `setupLockonFire()` (commented out; depends on `LAMBDA_LOCKON_CAN_FIRE`). `fireSound` is now `Holder<SoundEvent>`. |
| `sedna/BulletConfig` | 311 | 262 | **~90 %.** All stat fields, builder chain, `LAMBDA_STANDARD_RICOCHET`, `LAMBDA_STANDARD_ENTITY_HIT` present. Ammo/casing items became `Supplier`s (deferred-registry safe) with `getAmmo()`/`getCasingItem()`. **Missing:** `selfDamageDelay`, `LAMBDA_BEAM_HIT` / `LAMBDA_STANDARD_BEAM_HIT`. Damage sources replaced by data-driven `NtmDamageTypes` (so `DamageSourceSednaWithAttacker/NoAttacker` are legitimately obsolete, not a gap). |
| `sedna/Crosshair` | 36 | 36 | **Full.** |
| `sedna/factory/GunStateDecider` | 160 | 159 | **Full.** |
| `sedna/factory/Lego` | 343 | 295 | **~85 %.** `doStandardFire`, reload lambda, click dispatch, smoke handling, wear/spread/damage math, `standardExplode` all ported. **Missing:** `LAMBDA_STANDARD_CLICK_SECONDARY`, `LAMBDA_LOCKON_CAN_FIRE`, `LAMBDA_DEBUG_CAN_FIRE`, `tinyExplode()`. |
| `sedna/mags/IMagazine`, `MagazineSingleTypeBase`, `MagazineFullReload`, `MagazineSingleReload` | 74/246/… | 68/244/… | **~85 %** — see §6 for the ammo-bag/casing-bag holes. |
| `sedna/hud/*` (3 files) | — | — | **Full** (ammo counter, durability bar, interface). |
| `render/item/weapon/sedna/ItemRenderWeaponBase` | 478 | 309 | **~65 %.** `PoseStack`/`MultiBufferSource` rewrite done; sway/turn, aiming transform, smoke nodes, muzzle flash, gap flash all ported. **Missing:** FOV/zoom modifier (`getFOVModifier`, `getBaseFOV`), akimbo third-person setup, `setupModTable`/`renderModTable` (weapon-bench preview), `renderLaserFlash`. |
| `render/anim/*` (BusAnimation etc.) | 6 files | 6 files | **Full** — animation bus framework 1:1. |
| Recoil plumbing (`NuclearTechModClient` tick) | — | — | **Full.** |
| `MuzzleFlashPacket`, `HbmAnimation` payloads | — | — | **Full** (converted to `CustomPacketPayload` + `StreamCodec`). |

**Net: the engine works.** A modder can add a new gun to NEO today with roughly the same code shape as 1.7.10.

---

## 4. Guns — per-factory content table

70 guns exist in 1.7.10 across 24 `XFactory*` files. NEO ships 10 factory files, only 3 of which are `XFactory*`, and only 2 of those register guns.

| Factory | Orig guns | NEO | Status |
|---|---|---|---|
| `GunFactory` | `gun_debug` | `gun_debug` | **Ported** (also registers `ammo_debug`, `ammo_standard`, `ammo_secret`). |
| `XFactory12ga` (683→189 LoC) | `gun_maresleg`, `gun_maresleg_akimbo`, `gun_maresleg_broken`, `gun_liberator`, `gun_spas12`, `gun_autoshotgun`, `gun_autoshotgun_shredder`, `gun_autoshotgun_sexy` | `gun_maresleg`, `gun_spas12` | **Partial** — 2/8 guns, 9/24 bullet configs. Whole **shredder submunition system** (`makeShredderConfig`, `makeShredderSubmunition`, `g12_shredder_*`, `g12_sub_*`) and the equestrian easter-egg rounds are absent. |
| `XFactory44` (280→100 LoC) | `gun_henry`, `gun_henry_lincoln`, `gun_heavy_revolver`, `gun_heavy_revolver_lilmac`, `gun_heavy_revolver_protege`, `gun_hangman` | `gun_hangman` | **Partial** — 1/6 guns, 6/8 bullet configs. Missing `m44_equestrian_pip`/`_mn7` (boxcar + torpedo impact lambdas), `scope_lilmac` scope texture. |
| `XFactoryCatapult` (225→35 LoC) | `gun_fatman` + `LAMBDA_NUKE_STANDARD/DEMO/HIGH/TOTS/HIVE/BALEFIRE`, mortar/hook rounds | *(bullet cfg `cluster_submunition` only)* | **Stub** — no gun, no nuke rounds. |
| `XFactory357` | `gun_light_revolver`, `_atlas`, `_dani` | — | **Missing** |
| `XFactory9mm` | `gun_greasegun`, `gun_lag`, `gun_uzi`, `gun_uzi_akimbo` | — | **Missing** |
| `XFactory22lr` | `gun_am180`, `gun_star_f`, `gun_star_f_akimbo` | — | **Missing** |
| `XFactory556mm` | `gun_g3`, `gun_g3_zebra`, `gun_stg77` | — | **Missing** (incl. scope overlay guns) |
| `XFactory762mm` | `gun_carbine`, `gun_minigun`, `gun_minigun_lacunae`, `gun_minigun_dual`, `gun_mas36` | — | **Missing** (minigun spin-up, belt mags) |
| `XFactory50` | `gun_amat`, `_subtlety`, `_penance`, `gun_m2` | — | **Missing** (thermal sights, scoped) |
| `XFactory10ga` | `gun_double_barrel`, `_sacred_dragon`, `gun_autoshotgun_heretic` | — | **Missing** |
| `XFactory40mm` | `gun_flaregun`, `gun_congolake`, `gun_mk108` | — | **Missing** (flares, grenade rounds) |
| `XFactory45` | *(ammo only: 5 configs)* | — | **Missing** |
| `XFactory75Bolt` | `gun_bolter` | — | **Missing** |
| `XFactory35800` | `gun_aberrator`, `gun_aberrator_eott` | — | **Missing** |
| `XFactoryBlackPowder` | `gun_pepperbox` | — | **Missing** |
| `XFactoryAccelerator` | `gun_tau`, `gun_coilgun`, `gun_n_i_4_n_i` | — | **Missing** (beam/charge weapons) |
| `XFactoryEnergy` | `gun_tesla_cannon`, `gun_laser_pistol`, `_pew_pew`, `_morning_glory`, `gun_lasrifle` | — | **Missing** (beam weapons, `MagazineElectricEngine`) |
| `XFactoryFlamer` | `gun_flamer`, `_topaz`, `_daybreaker`, `gun_chemthrower` | — | **Missing** (fluid magazines) |
| `XFactoryRocket` | `gun_panzerschreck`, `gun_stinger`, `gun_quadro`, `gun_missile_launcher` | — | **Missing** (lock-on targeting) |
| `XFactoryFolly` | `gun_folly` | — | **Missing** |
| `XFactoryDrill` | `gun_drill` | — | **Missing** |
| `XFactoryTool` | `gun_fireext`, `gun_charge_thrower` | — | **Missing** |
| `XFactoryPA` | `gun_pa_melee`, `gun_pa_ranged` | — | **Missing** (power-armour weapons) |
| `XFactoryTurret` | *(ammo only: DGK + 240 mm shells)* | — | **Missing** |

### Specialised gun item subclasses — `sedna/impl/` (5 files, 0 ported)
`ItemGunChargeThrower`, `ItemGunChemthrower`, `ItemGunDrill`, `ItemGunNI4NI`, `ItemGunStinger` — all absent. These override `GunBaseNTItem` for bespoke behaviour (fluid consumption, block breaking, missile lock-on).

---

## 5. Weapon modifications — effectively 0 %

| Item | Orig | NEO |
|---|---|---|
| `sedna/mods/XWeaponModManager` | 447 LoC | **81 LoC skeleton** |
| `sedna/mods/IWeaponMod` | iface | **Ported** |
| `WeaponMod*` implementations | **33 classes** | **0** |
| `weapon_mod_test/generic/special/caliber` items | registered | **not registered** (enums `ModTest/ModGeneric/ModSpecial/ModCaliber` exist in `GunFactory` but no `ItemEnumMulti` registration) |
| `GUIWeaponTable` + `ContainerWeaponTable` + `machine_weapon_table` block | present | **absent** (`GunBaseNTItem:224` has the mod-list tooltip block commented out referencing `GUIWeaponTable`) |

What survives in NEO's `XWeaponModManager`: the `idToMod` / `stackToMod` / `modToStack` maps, `eval()` (which the whole `GunConfig`/`Receiver` DNA-getter chain calls, so it is load-bearing and works), and `WeaponModDefinition`. What is gone: `init()`, `install()`, `uninstall()`, `isApplicable()`, `hasUpgrade()`, `modFromStack()`, `onInstallStack()`, `onUninstallStack()`, `saveMagState()`/`restoreMagState()`/`changedMagState()` (caliber conversion preserving loaded rounds), and the `Comparator` that enforces install priority.

Missing implementations (complete list):
`WeaponModBase`, `WeaponModCaliber`, `WeaponModCanisters`, `WeaponModCarbineBayonet`, `WeaponModChoke`, `WeaponModDrill`, `WeaponModDrillFortune`, `WeaponModEngine`, `WeaponModGenericDamage`, `WeaponModGenericDurability`, `WeaponModGreasegun`, `WeapnModG3SawedOff` *(sic)*, `WeaponModLasAuto`, `WeaponModLasCapacitor`, `WeaponModLasShotgun`, `WeaponModLiberatorSpeedloader`, `WeaponModMASBayonet`, `WeaponModMinigunSpeedup`, `WeaponModNickel`, `WeaponModOverride`, `WeaponModPanzerschreckSawedOff`, `WeaponModPolymerFurniture`, `WeaponModSawedOff`, `WeaponModScope`, `WeaponModShredderSpeedup`, `WeaponModSilencer`, `WeaponModSlowdown`, `WeaponModStackMag`, `WeaponModTestDamage`, `WeaponModTestFirerate`, `WeaponModTestMulti`, `WeaponModUziSaturnite`.

---

## 6. Ammo & magazines

### Magazines (`sedna/mags`)
| Class | NEO |
|---|---|
| `IMagazine` | ported (with 2 stubbed statics, below) |
| `MagazineSingleTypeBase` | ported, **ammo-bag branch commented out** |
| `MagazineFullReload` | ported |
| `MagazineSingleReload` | ported |
| `MagazineBelt` (162 LoC) | **missing** — blocks minigun/M2/autoshotgun |
| `MagazineInfinite` | **missing** |
| `MagazineFluid` | **missing** — blocks flamer/chemthrower |
| `MagazineLiquidEngine` | **missing** — blocks fuel-powered tools |
| `MagazineElectricEngine` | **missing** — blocks energy weapons/drill |

Stubbed in `IMagazine` (NEO):
- `handleAmmoBag(...)` — body fully commented out ⇒ **casing bag never collects brass**.
- `shouldUseUpTrenchie(...)` — `trenchie`/`aos` hard-coded `false` ⇒ **Trenchmaster armour ammo-saving perk dead**.
- `MagazineSingleTypeBase.standardReload` — the ~45-line `ammo_bag` / `ammo_bag_infinite` reload branch is commented out ⇒ **reloading from ammo bags does not work**.

### Ammo items
| Item | Orig | NEO |
|---|---|---|
| `ammo_standard` (`EnumAmmo`, 94 variants) | ✓ | ✓ registered; enum copied verbatim incl. `ORDER` |
| `ammo_secret` (`EnumAmmoSecret`, 8) | ✓ | ✓ registered |
| `ammo_debug` | ✓ | ✓ |
| `casing` (`EnumCasingType`) | ✓ | ✓ (`NtmItems.CASING`) |
| `ItemAmmo` (174 LoC, legacy multi-ammo) | ✓ | **missing** |
| `ItemAmmoArty` (377 LoC) / `ammo_arty` | ✓ | **missing** |
| `ItemAmmoHIMARS` (225 LoC) / `ammo_himars` | ✓ | **missing** |
| `ItemAmmoEnums` (3 enums incl. `Ammo240Shell`) | ✓ | **missing** |
| `ItemClip`, `WeaponizedCell`, `GunB92Cell` | ✓ | **missing** |
| `ammo_dgk`, `ammo_shell` (turret feed) | ✓ | **missing** |
| Ammo press (`MachineAmmoPress` + recipes + GUI) | ✓ | **missing** |
| `ItemAmmoBag` / `ItemAmmoContainer` / `BlockAmmoCrate` | ✓ | **missing** |
| `ItemCasingBag` + `GUICasingBag` + `ContainerCasingBag` | ✓ | **missing** |

Note: NEO's `EnumAmmo` was renamed to `GunFactory.Ammo` but the **ordinal order is preserved exactly**, which is the right call — the ordinals are the on-disk item subtype.

---

## 7. Legacy (pre-SEDNA) weapon items — 1/16 ported

`com/hbm/items/weapon/` root, 1.7.10:

| File | LoC | NEO |
|---|---|---|
| `ItemMissile` | 109 | → `MissileItem` (96) **ported** |
| `GunB92` | 290 | missing |
| `GunB92Cell` | 73 | missing |
| `ItemAmmo` | 174 | missing |
| `ItemAmmoArty` | 377 | missing |
| `ItemAmmoHIMARS` | 225 | missing |
| `ItemClip` | 32 | missing |
| `ItemCrucible` | 191 | missing |
| `ItemCustomMissile` | 115 | missing |
| `ItemCustomMissilePart` | 368 | missing |
| `ItemDisperser` | 75 | missing |
| `ItemGenericGrenade` | 42 | missing |
| `ItemGrenade` | 17 | missing |
| `ItemGrenadeDynamite` | 25 | missing |
| `ItemGrenadeFishing` | 60 | missing |
| `WeaponizedCell` | 64 | missing |

`com/hbm/items/weapon/grenade/` (5 files, **0 ported**): `ItemGrenadeExtra`, `ItemGrenadeFilling` (318 LoC — the modular grenade payload table), `ItemGrenadeFuze`, `ItemGrenadeShell`, `ItemGrenadeUniversal` (235 LoC). The whole modular-grenade crafting system is absent, and `ItemGrenadeFilling` is also referenced by `GunFactoryClient` in 1.7.10 for 40 mm rounds.

`com/hbm/handler/guncfg/` (5 files, **0 ported**) — the `@Deprecated` pre-SEDNA `BulletConfiguration` system, still live in 1.7.10 for NPC/mob weapons (`EntityAIMaskmanLasergun`, `EntityBOTPrimeBase`, `EntityUFO`, `EntityMissileCustom`) and for `EntityBulletBaseNT`. Also missing: `handler/BulletConfiguration`, `handler/BulletConfigSyncingUtil`, `handler/CasingEjector`.

---

## 8. Turrets — 0 % ported

Nothing turret-related exists in NEO. The only two hits for "turret" across the entire NEO tree are a comment in `NtmCreativeTabs.java:999` (`// turrets, weapons, ammo`) and a mention inside `BulletBaseMK4`. No block, block entity, GUI, menu, renderer, item, sound or asset.

### What has to be built

| Component | Orig files | Orig LoC | Notes |
|---|---|---|---|
| `tileentity/turret/TileEntityTurretBaseNT` | 1 | **1 107** | The engine: radian-based yaw/pitch tracking with client interpolation, target acquisition + filters (players/animals/mobs/machines), whitelist via `ItemTurretBiometry`, ammo inventory + `BulletConfig` matching, energy draw (`IEnergyReceiverMK2`), casing ejection, StatTrak counter, `IControlReceiver` GUI control packets, OpenComputers `SimpleComponent` + `IRORInteractive` (Redstone-over-Radio), `IRadarDetectableNT`. |
| `TileEntityTurretBaseArtillery` | 1 | 126 | Indirect-fire base. |
| Concrete turret BEs | 13 | ~2 880 | Arty (528), HIMARS (385), Maxwell (299), Sentry (260), Fritz (226), Howard (221), Tauon (177), Richard (179), Chekhov (176), Jeremy (160), HowardDamaged (113), SentryDamaged (106), Friendly (53). |
| `blocks/turret/*` | 15 | ~640 | `TurretBase`/`TurretBaseNT` extend `BlockDummyable` (multiblock dummy-block pattern). |
| `render/tileentity/RenderTurret*` | 13 | 843 | TESRs. |
| `inventory/gui/GUITurret*` + `ContainerTurretBase` | 13 | 740 | `GUITurretBase` alone is 322 LoC (target-filter toggles, biometry name list add/remove). |
| `items/machine/ItemTurretBiometry`, `ItemTurretChip` | 2 | — | |
| `XFactoryTurret` ammo (`ammo_dgk`, 240 mm shell family) | 1 | 55 | Depends on `ItemAmmoEnums.Ammo240Shell` and `ammo_shell`, neither ported. |
| `ItemDesignatorArtyRange` | 1 | — | Artillery targeting designator. |

Turret blocks: `turret_arty`, `turret_chekhov`, `turret_friendly`, `turret_fritz`, `turret_himars`, `turret_howard`, `turret_howard_damaged`, `turret_jeremy`, `turret_maxwell`, `turret_richard`, `turret_sentry`, `turret_sentry_damaged`, `turret_tauon`.

**Good news for the porter:** NEO already has the dependencies turrets need — `api/hbm/energymk2/IEnergyReceiverMK2`, `api/hbm/entity/IRadarDetectableNT`, `api/hbm/redstoneoverradio`, `com/hbm/blockentity/MachineBaseBlockEntity`, `com/hbm/blocks/DummyableBlock` (the `BlockDummyable` port), `SpentCasing` + `CasingCreator` particles, `BulletBaseMK4`, `ExplosionVNT`. **Missing prerequisites:** `CasingEjector`, an `IGUIProvider` equivalent (NEO uses vanilla `MenuProvider`, so each turret GUI must be re-architected as `AbstractContainerMenu` + `Screen`), and the artillery shell / DGK ammo items.

---

## 9. Rendering & assets

| Item | Orig | NEO |
|---|---|---|
| Gun first-person renderers | 52 + `ItemRenderWeaponBase` | 4 (`ItemRenderDebug`, `ItemRenderMaresleg`, `ItemRenderSPAS12`, `ItemRenderHangman`) + base |
| Non-SEDNA weapon renderers (`ItemRenderBigSword`, `ItemRenderChainsaw`, `ItemRenderCrucible`, `ItemRenderFireExt`, `ItemRenderGavel`, `ItemRenderGunAnim`, `ItemRenderRedstoneSword`, `ItemRenderShim`) | 8 | 0 |
| `LegoClient` bullet/beam renderers | 703 LoC, ~40 `RENDER_*` lambdas incl. 11 beam renderers, flares, wire/hook, nuke, RPZB, ML, QD | 160 LoC, **12** bullet lambdas only — all beam renderers, flares, `renderWire`, `renderFlare(Sprite)`, `renderStandardLaser`, `drawLineSegment` absent |
| Scope overlay | `ModEventHandlerClient:367` → `RenderScreenOverlay.renderScope()` | **absent** — `GunConfig.scopeTexture_DNA` is stored and `getScopeTexture()` exists, but nothing ever reads it. `thermalSights` likewise unread. |
| Crosshair overlay | ✓ | ✓ `RenderScreenOverlay.renderCustomCrosshairs` |
| Gun sounds | 73 `GUN_*` | 16 |
| Orchestras | 52 | 4 (`DEBUG`, `HANGMAN`, `MARESLEG`, `SPAS`) |

---

## 10. Confirmed defects in the already-ported code

1. **`com/hbm/entity/projectile/BulletBeamBase.java` (NEO), `tick()` is infinitely recursive.**
   ```java
   public void tick() {
       ...
       this.tick();            // ← should be super.tick()
       ...
   }
   ```
   The 1.7.10 original calls `super.onUpdate()`. Currently latent only because no beam weapon is registered; the moment any `ProjectileType.BEAM` config is added this is an instant `StackOverflowError`.

2. **`BulletBeamBase` lost the hitscan.** `EntityBulletBeamBase` (339 LoC) contains `performHitscan()` (~170 LoC), `performHitscanExternal(double range)`, `setRotationsFromVector(Vec3)` and the angular-inaccuracy spawn constructor. NEO's 115-LoC version has **none** of them, and `onImpact` is never called from anywhere. Beam weapons (laser pistol/rifle, tau, tesla, folly, NI4NI, coilgun) cannot function until this is rewritten.

3. **`GunConfig.scopeTexture` / `hasThermalSights` are dead ends** — stored, evaluated through the mod system, never consumed by any renderer.

4. **`XWeaponModManager` has no `init()`**, so `idToMod` is permanently empty. `eval()` therefore always returns the base value — correct by accident, but it means every `_DNA` getter is currently a no-op pass-through.

---

## 11. Architectural blockers for the remaining work

1. **Item damage-as-subtype → data components.** `ammo_standard`/`ammo_secret`/`casing`/`weapon_mod_*` are metadata multi-items in 1.7.10 (`new ItemStack(item, 1, ordinal)`). NEO invented `EnumMultiItem` + `MetaHelper` + `ComparableStack(item, count, enum)` to bridge this, and `BulletConfig` now stores `Supplier<ComparableStack>` so ammo identity resolves after deferred registration. **This pattern is established and works** — but every one of the ~140 unported `BulletConfig`s has to be rewritten into it, and `ItemAmmoArty`/`ItemAmmoHIMARS`/`ItemGrenadeFilling`, which encode *stats* in NBT/meta, need a full data-component redesign.

2. **`IIcon` + `ItemRenderer` → BEWLR + client extensions.** 1.7.10 used `MinecraftForgeClient.registerItemRenderer(item, IItemRenderer)`. NEO uses `RegisterClientExtensionsEvent` + `IClientItemExtensions.getCustomRenderer()` returning a `BlockEntityWithoutLevelRenderer`, plus `applyForgeHandTransform` for the first-person pose. The shim is written (`GunFactoryClient.registerGunItemRenderer`) but every one of the 48 remaining renderers must be converted from immediate-mode `Tessellator`/`GL11` to `PoseStack` + `VertexConsumer` + `RenderType`.

3. **`Tessellator`/fixed-function GL → `VertexConsumer`/`RenderType`.** This is the single biggest mechanical cost. `LegoClient` alone is 703 lines of raw `Tessellator` bullet/beam drawing plus `GL11.glBlendFunc`/`glDepthMask` state juggling; each beam renderer needs a bespoke `RenderType` (the port already does this for `SMOKE` and a memoized `FLASH`). Additive-blend beams, flare sprites and the hook wire all need new render types with correct depth/sort behaviour.

4. **Metadata blocks → blockstates, and `BlockDummyable` → `DummyableBlock`.** All 13 turret blocks are `BlockDummyable` multiblocks that encode facing + dummy offset in metadata. NEO's `DummyableBlock` exists; each turret still needs a blockstate JSON, a model, and a datagen entry (NEO has `com/hbm/datagen`).

5. **TESR → `BlockEntityRenderer`.** 13 turret TESRs to convert, each with two-axis animated rotation driven by interpolated BE fields.

6. **`IGUIProvider` → `MenuProvider` / `AbstractContainerMenu` / `Screen`.** 1.7.10's `IGUIProvider` + numeric GUI IDs + `GuiHandler` is gone; NEO has no `IGUIProvider` at all. All 13 turret GUIs and the weapon-mod table GUI need new menu types registered in a `DeferredRegister<MenuType<?>>`, plus `MenuScreens.register`.

7. **Custom packets → NeoForge payloads.** `IControlReceiver.receiveControl(player, NBTTagCompound)` drove all turret GUI buttons. NEO has `toserver/CompoundTagControl` and `CompoundTagItemControl`, so the pattern exists — the turret side just isn't written. Turret sync used `IBufPacketReceiver`/`BufPacket`, which **is** ported.

8. **NBT on `ItemStack` → `DataComponents`.** Already solved for guns via `TagsUtil.getCustomData/putCustomData`, but note this is a read-modify-**write** per field (`setValueInt` re-puts the whole `CompoundTag`), which is measurably heavier than 1.7.10's direct tag mutation — the gun tick writes many fields per tick per receiver. Worth a batched-write refactor before adding 66 more guns.

9. **Damage sources → `DamageType` registry.** Done correctly (`NtmDamageTypes` + `level.damageSources().source(...)`), but each new `DamageClass` needs a datapack JSON.

10. **Ore dictionary → tags, NEI → JEI.** Weapon/ammo crafting (`WeaponRecipes`, 324 LoC; `AmmoPressRecipes`) is entirely unported; NEO's recipe layer is JSON/datagen-driven (`com/hbm/datagen`, `com/hbm/inventory/recipes`), so these are rewrites rather than translations.

11. **`IFluidHandler` rewrite.** `MagazineFluid`/`MagazineLiquidEngine` back the flamer/chemthrower off `FluidTank`s stored on the item. NEO has `api/hbm/fluidmk2` and NeoForge `IFluidHandlerItem`, but the item-side fluid capability must be attached via `RegisterCapabilitiesEvent` — a different model from 1.7.10.

12. **OpenComputers / Redstone-over-Radio integration.** `TileEntityTurretBaseNT` implements `SimpleComponent` + `CompatHandler.OCComponent` behind `@Optional.Interface`. NeoForge has no `@Optional.Interface`; OC has no 1.21.1 release. This integration should simply be dropped (NEO keeps `api/hbm/redstoneoverradio`, so RoR can stay).

13. **Deferred registration ordering.** 1.7.10 built `BulletConfig`s at item-construction time referencing live `Item` instances. NEO must defer: the port uses `Supplier`/`Holder` and passes `DeferredRegister.Items` into each factory `init(...)`. Every new factory must follow this or NPE at class-load.

---

## 12. Effort estimate

Assumes one experienced Minecraft modder already fluent in both 1.7.10 HBM and 1.21.1 NeoForge, working on an otherwise-healthy port where the surrounding subsystems (entities, explosions, particles, energy, blocks) land in parallel.

| Work package | Person-days |
|---|---|
| Finish SEDNA core: mod manager `init`/install/uninstall/caliber-swap, 33 `WeaponMod*` classes, weapon-table block + menu + screen + BEWLR preview | 12 |
| Remaining 5 magazine types (belt, infinite, fluid, liquid-engine, electric-engine) + ammo-bag / casing-bag / Trenchmaster re-enable + ammo bag & casing bag items and menus | 7 |
| Fix `BulletBeamBase` (recursion + port `performHitscan`) and port all beam renderers in `LegoClient` | 5 |
| 21 remaining `XFactory*` files: ~66 guns, ~138 bullet configs, 5 `sedna/impl` subclasses, lock-on, charge, drill and flamer behaviours | 30 |
| 48 gun renderers + models/textures wiring + scope overlay + thermal sights + akimbo third-person + FOV/zoom | 28 |
| 48 remaining Orchestras + 57 remaining gun sound events + sounds.json | 8 |
| Legacy weapon items: 9 grenades (incl. modular filling/fuze/shell system), `ItemCrucible`, `ItemCustomMissile(Part)`, `GunB92`, `WeaponizedCell`, `ItemDisperser`, `ItemAmmo*`, `ItemAmmoEnums` | 12 |
| `handler/guncfg` legacy NPC bullet system (`BulletConfiguration`, `BulletConfigSyncingUtil`, `GunNPCFactory`, `GunEnergy/Rocket/DGK`) + `CasingEjector` | 5 |
| Turrets: base engine + artillery base + 13 concrete BEs | 16 |
| Turrets: 15 blocks (blockstates/models/datagen) + 13 BERs + 13 menus/screens + biometry & chip items + arty designator | 16 |
| Ammo press machine + ammo crate + ammo/casing containers + `WeaponRecipes`/`AmmoPressRecipes` as datagen | 7 |
| Integration, balance parity checks, JEI subtypes for ammo/mods, creative tab ordering, bugfixing | 9 |
| **Total** | **155** |

Rounded: **~155 person-days** (about 7–8 calendar months for one person, ~4 months for two). Turrets alone are ~32 days; guns+renderers are ~58; weapon mods are ~12.

---

## 13. Dependency order

The weapons subsystem cannot be finished before these land:

1. **Projectiles/entities** — `BulletBaseMK4` is ported but `BulletBeamBase` is broken and `EntityBulletBaseNT`/`EntityBullet` are absent; `EntityBoxcar`, `EntityTorpedo`, `EntityArtilleryShell`, `EntityMissile*` are referenced by bullet impact lambdas and by HIMARS/arty turrets.
2. **Items/materials & metadata framework** — `ItemAmmoEnums`, `ammo_shell`, `ammo_dgk`, casing types, and the mod-item multi-items.
3. **Explosions** — `ExplosionVNT` is ported; the nuke/balefire/hive variants used by catapult and folly rounds are not all verified present.
4. **GUI/menu framework** — a project-wide `MenuType` registry pattern is needed before 14 weapon/turret screens can be written.
5. **Machines/energy** — turrets need `IEnergyReceiverMK2` consumers wired the same way as other NEO machines (`MachineBaseBlockEntity` is available).
6. **Armor** — Trenchmaster (`ArmorTrenchmaster`) gates the ammo-saving perk stubbed in `IMagazine`.
7. **Recipes/datagen** — `WeaponRecipes` and `AmmoPressRecipes` need the assembly-machine/anvil recipe loaders (partially present in NEO).

---

## 14. Concrete file lists

### NEO — everything that exists today (31 files)
```
com/hbm/items/weapon/MissileItem.java
com/hbm/items/weapon/sedna/BulletConfig.java
com/hbm/items/weapon/sedna/Crosshair.java
com/hbm/items/weapon/sedna/GunBaseNTItem.java
com/hbm/items/weapon/sedna/GunConfig.java
com/hbm/items/weapon/sedna/Receiver.java
com/hbm/items/weapon/sedna/package-info.java
com/hbm/items/weapon/sedna/factory/ConfettiUtil.java
com/hbm/items/weapon/sedna/factory/GunFactory.java
com/hbm/items/weapon/sedna/factory/GunFactoryClient.java
com/hbm/items/weapon/sedna/factory/GunStateDecider.java
com/hbm/items/weapon/sedna/factory/Lego.java
com/hbm/items/weapon/sedna/factory/LegoClient.java
com/hbm/items/weapon/sedna/factory/Orchestras.java
com/hbm/items/weapon/sedna/factory/XFactory12ga.java
com/hbm/items/weapon/sedna/factory/XFactory44.java
com/hbm/items/weapon/sedna/factory/XFactoryCatapult.java
com/hbm/items/weapon/sedna/hud/HUDComponentAmmoCounter.java
com/hbm/items/weapon/sedna/hud/HUDComponentDurabilityBar.java
com/hbm/items/weapon/sedna/hud/IHUDComponent.java
com/hbm/items/weapon/sedna/mags/IMagazine.java
com/hbm/items/weapon/sedna/mags/MagazineFullReload.java
com/hbm/items/weapon/sedna/mags/MagazineSingleReload.java
com/hbm/items/weapon/sedna/mags/MagazineSingleTypeBase.java
com/hbm/items/weapon/sedna/mods/IWeaponMod.java
com/hbm/items/weapon/sedna/mods/XWeaponModManager.java
com/hbm/render/item/weapon/sedna/ItemRenderDebug.java
com/hbm/render/item/weapon/sedna/ItemRenderHangman.java
com/hbm/render/item/weapon/sedna/ItemRenderMaresleg.java
com/hbm/render/item/weapon/sedna/ItemRenderSPAS12.java
com/hbm/render/item/weapon/sedna/ItemRenderWeaponBase.java
```
Supporting (counted under other subsystems but load-bearing here): `com/hbm/entity/projectile/BulletBaseMK4.java`, `BulletBeamBase.java`, `com/hbm/render/entity/projectile/RenderBulletMK4.java`, `com/hbm/particle/SpentCasing*.java`, `com/hbm/particle/helper/CasingCreator.java`, `com/hbm/render/anim/*`, `com/hbm/network/toclient/{MuzzleFlashPacket,HbmAnimation}.java`, `com/hbm/handler/{HbmKeybinds,HbmKeybindsServer}.java`.

### Original — the 21 unported `XFactory*` + 5 `sedna/impl` + 33 `sedna/mods` + 5 `sedna/mags`
See §4, §5, §6. Paths are all under
`hbm-1710/src/main/java/com/hbm/items/weapon/sedna/{factory,impl,mods,mags}/`.

### Original — turret files (all 0 % ported, 71 files)
`hbm-1710/src/main/java/com/hbm/blocks/turret/*.java` (15)
`hbm-1710/src/main/java/com/hbm/tileentity/turret/*.java` (15)
`hbm-1710/src/main/java/com/hbm/render/tileentity/RenderTurret*.java` (13)
`hbm-1710/src/main/java/com/hbm/inventory/gui/GUITurret*.java` (12)
`hbm-1710/src/main/java/com/hbm/inventory/container/ContainerTurretBase.java`
`hbm-1710/src/main/java/com/hbm/items/machine/{ItemTurretBiometry,ItemTurretChip}.java`
`hbm-1710/src/main/java/com/hbm/items/tool/ItemDesignatorArtyRange.java`
`hbm-1710/src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryTurret.java`
`hbm-1710/src/main/java/com/hbm/entity/projectile/EntityArtilleryShell.java`
