# Die 79 Bauwerke des Originals — was ihnen im Port fehlt

Erhoben in dieser Sitzung aus den 79 `.nbt`-Dateien unter `assets/hbm/structures/` des
Originals, durch Auslesen ihrer Blockpaletten und Abgleich gegen die im Port
registrierten Blöcke (`NtmBlocks`).

## Die Zahlen

| | |
|---|---:|
| Bauwerksdateien | 79 |
| Format | eigenes Format des Originals, `version: 1` — **nicht** das der Strukturblöcke von 1.21 |
| verschiedene (Blockname, Metadaten)-Paare | 664 |
| davon Vanilla | 183 Paare auf 61 Namen |
| davon `hbm:` | 481 Paare auf 185 Namen |
| `hbm:`-Blöcke, die der Port hat | 59 |
| davon nur unter anderem Namen | 4 |
| **`hbm:`-Blöcke, die dem Port wirklich fehlen** | **122** |
| Bauwerke, die sich heute vollständig bauen ließen | **0 von 79** |

Vier Namen aus der Fehlliste sind keine echten Lücken, sondern Wortumstellungen — der Port
benennt Stufen und Fässer anders herum als das Original:

```
concrete_brick_slab  ->  brick_concrete_slab
lox_barrel           ->  barrel_lox
pink_barrel          ->  barrel_pink
red_barrel           ->  barrel_red
```

Sie brauchen keinen neuen Block, nur einen Eintrag in der Umbenennungstabelle. An der letzten
Zeile der Tabelle ändert das nichts: auch mit diesen vieren bleibt kein Bauwerk übrig, das sich
vollständig bauen ließe.

Drei weitere Einträge sind in 1.21 überhaupt kein eigener Block mehr, sondern eine Eigenschaft:

```
brick_double_slab            \
concrete_brick_double_slab    >  1.21: die zugehörige Stufe mit type=double
concrete_double_slab         /
```

In 1.7.10 war die Doppelstufe ein eigener Block neben der halben; in 1.21 ist sie derselbe
`SlabBlock` mit `type=double`. Auch sie sind Sache der Umsetzungstabelle, nicht neuer Blöcke —
sofern die zugehörige halbe Stufe existiert. `brick_concrete_slab` gibt es im Port, `concrete_slab`
und `brick_slab` nicht; die beiden müssen also doch angelegt werden.

**Echter Bedarf an neuen Blöcken: 115.**

> **Berichtigung (nach Runde 92).** Hier stand zuerst **119**. Die Zahl war zu hoch: sie
> enthielt `#undef`, das gar kein Block ist, und drei Namen, die das Original *selbst* nicht
> mehr kennt — `barrel_iron`, `ladder_tungsten` und `ore_coal_oil` stehen dort in
> `ignoreMappings`, der Liste der Altnamen, die beim Laden alter Welten stillschweigend
> wegfallen. Auch die Fortschrittszahlen der Runden 89 bis 92 („24 der 119", „40", „50", „56")
> waren falsch: sie zählten mal angelegte Blöcke des Ports, mal Namen des Originals. Beides ist
> nicht dasselbe, denn aus einem Metadaten-Block des Originals werden im Port mehrere.
>
> Von Hand ist diese Zahl offenbar nicht zuverlässig zu führen. Sie wird deshalb ab jetzt
> gemessen: [`tools/structure-gap.py`](../../tools/structure-gap.py) liest die 79 Bauwerke aus
> `hbm-upstream/master`, zieht die in `NtmBlocks.java` angelegten Blöcke ab und rechnet die
> beiden Ausnahmearten heraus. Der Aufruf mit `--list` nennt die fehlenden Namen.


## Die Mehrfachstufen: wie ihre Metadaten zu lesen sind

Vier der Einträge sind im Original keine einzelnen Blöcke, sondern `BlockMultiSlab` — *eine*
Stufe, deren Metadaten-Zahl das Material auswählt. Die unteren drei Bit wählen den Werkstoff,
Bit 3 (Wert 8) sagt „obere Hälfte":

```
concrete_slab        0 concrete_smooth   1 concrete        2 concrete_asbestos
                     3 ducrete_smooth    4 ducrete         5 asphalt
concrete_brick_slab  0 brick_concrete    1 …_mossy         2 …_cracked
                     3 …_broken          4 brick_ducrete
brick_slab           0 reinforced_stone  1 reinforced_brick 2 brick_obsidian
                     3 brick_light       4 brick_compound  5 brick_asbestos   6 brick_fire
```

In 1.21 wird daraus je Werkstoff ein eigener `SlabBlock`; „obere Hälfte" ist dort die Eigenschaft
`type=top`, „Doppelstufe" ist `type=double`.

**Gebraucht werden aber nur die Werkstoffe, die in den 79 Bauwerken wirklich vorkommen** — das
sind längst nicht alle:

| Mehrfachstufe | benutzte Metadaten | daraus folgende Stufen |
|---|---|---|
| `concrete_slab` | 0, 1, 2, 8, 9, 10 | `concrete_smooth_slab`, `concrete_slab`, `concrete_asbestos_slab` |
| `concrete_brick_slab` | 0, 2 | `brick_concrete_slab` ✓, `brick_concrete_cracked_slab` ✓ |
| `brick_slab` | 0, 1, 3, 4, 8, 9, 11, 12 | `reinforced_stone_slab`, `reinforced_brick_slab`, `brick_light_slab`, `brick_compound_slab` |

Zwei davon hat der Port schon. Neu anzulegen sind also **sieben Stufenblöcke**, nicht achtzehn.

Das letzte Feld ist das entscheidende. Selbst das einfachste Stück — ein 3×3-Beutestück
aus dem Meteoritenfeld — braucht eine Blockart, die es im Port nicht gibt. Es gibt kein
Bauwerk, mit dem sich anfangen ließe, ohne vorher Blöcke nachzuziehen.

## Was fehlt, nach Art

### sonstige (57)

```
#undef                              anvil_lead                          barrel_iron                         block_electrical_scrap
block_starmetal                     charger                             door_bunker                         door_metal
door_office                         dungeon_chain                       dungeon_spawner                     fence_metal
floodlight                          hev_battery                         ladder_steel                        ladder_tungsten
lightstone                          lightstone_bricks_stairs            lox_barrel                          meteor_battery
meteor_brick                        meteor_brick_chiseled               meteor_pillar                       meteor_polished
meteor_spawner                      mush                                ntm_dirt                            pink_barrel
plant_dead                          pole_satellite_receiver             pole_top                            radio_telex
radiorec                            rail_narrow                         red_barrel                          skeleton_holder
spotlight_beam                      spotlight_fluoro                    spotlight_halogen                   spotlight_incandescent
spotlight_incandescent_off          tape_recorder                       tesla                               tile_lab
tile_lab_broken                     tile_lab_cracked                    tnt_ntm                             toxic_block
trapdoor_steel                      vitrified_barrel                    wand_jigsaw                         wand_logic
wand_loot                           wand_tandem                         wood_barrier                        wood_structure
yellow_barrel
```

### Deko (20)

```
deco_computer                       deco_crt                            deco_loot                           deco_pipe
deco_pipe_framed                    deco_pipe_framed_green_rusted       deco_pipe_framed_red                deco_pipe_framed_rusted
deco_pipe_marked                    deco_pipe_quad                      deco_pipe_quad_marked               deco_pipe_quad_red
deco_pipe_quad_rusted               deco_pipe_red                       deco_pipe_rim                       deco_pipe_rim_green
deco_pipe_rim_marked                deco_pipe_rim_rusted                deco_pipe_rusted                    deco_toaster
```

### Beton (13)

```
concrete_asbestos_stairs            concrete_brick_double_slab          concrete_brick_slab                 concrete_colored
concrete_colored_ext                concrete_double_slab                concrete_pillar                     concrete_rebar
concrete_slab                       concrete_smooth_stairs              concrete_stairs                     concrete_super
concrete_super_broken
```

### Gelaende und Erz (10)

```
ore_coal_oil                        reinforced_brick                    reinforced_brick_stairs             reinforced_glass
reinforced_glass_pane               reinforced_lamp_off                 reinforced_light                    reinforced_sand
reinforced_stone                    reinforced_stone_stairs
```

### Kisten (8)

```
crate                               crate_ammo                          crate_can                           crate_lead
crate_metal                         crate_red                           crate_supply                        crate_weapon
```

### Maschinen (7)

```
fluid_duct_gauge                    machine_boiler_off                  machine_electric_furnace_off        machine_fluidtank
machine_funnel                      machine_microwave                   machine_weapon_table
```

### Ziegel (6)

```
brick_compound                      brick_compound_stairs               brick_double_slab                   brick_light_stairs
brick_obsidian_stairs               brick_slab
```

### Metall (5)

```
steel_corner                        steel_grate_wide                    steel_poles                         steel_roof
steel_wall
```

## Was der Port schon hat (59)

```
balefire                            barrel_corroded                     barrel_plastic                      block_aluminium
block_copper                        block_meteor                        block_meteor_cobble                 block_red_copper
block_scrap                         block_slag                          bobblehead                          brick_asbestos
brick_concrete                      brick_concrete_broken               brick_concrete_broken_stairs        brick_concrete_cracked
brick_concrete_cracked_stairs       brick_concrete_mossy                brick_concrete_mossy_stairs         brick_concrete_stairs
brick_light                         concrete                            concrete_asbestos                   concrete_smooth
crate_iron                          deco_aluminium                      deco_beryllium                      deco_lead
deco_red_copper                     deco_rusty_steel                    deco_steel                          deco_titanium
deco_tungsten                       det_charge                          dirt_dead                           fluid_duct_neo
gas_asbestos                        geiger                              gravel_obsidian                     leaves_layer
machine_battery                     machine_diesel                      machine_rotary_furnace              mine_ap
mine_he                             mine_naval                          oil_spill                           ore_oil_sand
red_cable                           red_connector                       red_wire_coated                     sellafield_slaked
spikes                              steel_beam                          steel_grate                         steel_scaffold
turret_howard_damaged               turret_sentry_damaged               waste_leaves
```

## Was daraus folgt

Die Reihenfolge für Stufe 5 steht damit fest, und sie beginnt **nicht** bei der
Weltgenerierung:

1. **Die 115 fehlenden Blöcke.** Der weitaus größte Teil ist Bauwerk und Zierat — Beton in
   allen Spielarten, Ziegel, Treppen, Stufen, Kisten, Rohre, Schilder. Das ist viel Arbeit,
   aber mechanische: die meisten sind einfache Blöcke ohne Verhalten. Sie sind im Port
   bisher übersprungen worden, weil sie nichts *tun* — genau deshalb stehen sie jetzt im Weg.
2. **Der Umsetzer für das Dateiformat.** Er braucht die Flattening-Tabelle für die 183
   Vanilla-Paare und die Umbenennungstabelle für die 481 `hbm:`-Paare. Einmalige Arbeit,
   danach gilt sie für alle 79 Bauwerke.
3. **Die 79 Bauwerke** in einem Zug.
4. Erst dann die handgeschriebenen Strukturklassen, Verliese, Biome und Erzverteilung.

Wer diese Reihenfolge umdreht, schreibt zuerst die aufwendigste Arbeit und hat am Ende
immer noch kein Gebäude in der Welt.
