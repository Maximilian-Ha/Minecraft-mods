#!/usr/bin/env python3
"""Setzt die .nbt-Bauwerke des Originals (1.7.10) in Strukturvorlagen fuer 1.21 um.

WARUM EIN UMSETZER: die 79 Dateien unter assets/hbm/structures/ sehen aus wie die
Strukturblock-Dateien von 1.21 -- gleiche Schluessel (size, palette, blocks, entities),
gleiches gzip-NBT. Sie sind es aber nicht. Ihr Inhalt stammt von vor der
Flattening-Umstellung:

    {'Name': 'hbm:tile.meteor_brick', 'Properties': {'meta': '0'}}

Ein Name plus eine Metadaten-Zahl, wo 1.21 einen Blockzustand mit benannten
Eigenschaften erwartet. Dazu fehlt der Datei das Feld DataVersion, ohne das 1.21 sie gar
nicht erst liest. Jeder Block muss also einzeln uebersetzt werden.

WAS DER UMSETZER NICHT TUT: raten. Ein (Name, meta)-Paar, das nicht in der Tabelle steht,
bricht den Lauf ab. Ein stillschweigend zu Luft gewordener Block waere im fertigen Bauwerk
ein Loch, das niemand bemerkt -- und genau das macht das Original heute mit
hbm:tile.ladder_tungsten, dessen Block es nicht mehr gibt (siehe LEITER unten).

Aufruf:
    tools/nbt2structure.py <quellverzeichnis> <zielverzeichnis>
    tools/nbt2structure.py --pruefe <quellverzeichnis>     nur Tabelle pruefen, nichts schreiben
    tools/nbt2structure.py --fehlliste <quellverzeichnis>  zaehlt auf, was der Tabelle fehlt

Die Quelldateien liest man mit
    git show hbm-upstream/master:src/main/resources/assets/hbm/structures/<pfad>
heraus; tools/extract-structures.sh nimmt einem das ab.
"""

import gzip
import io
import os
import struct
import sys
import zlib

# 1.21.1
DATA_VERSION = 3955

MODID = "hbmsntm"


# ---------------------------------------------------------------- NBT lesen

def _lese(d, i, typ):
    if typ == 1: return d[i] - 256 if d[i] > 127 else d[i], i + 1
    if typ == 2: return struct.unpack_from('>h', d, i)[0], i + 2
    if typ == 3: return struct.unpack_from('>i', d, i)[0], i + 4
    if typ == 4: return struct.unpack_from('>q', d, i)[0], i + 8
    if typ == 5: return struct.unpack_from('>f', d, i)[0], i + 4
    if typ == 6: return struct.unpack_from('>d', d, i)[0], i + 8
    if typ == 7:
        n = struct.unpack_from('>i', d, i)[0]; i += 4
        return list(d[i:i + n]), i + n
    if typ == 8:
        n = struct.unpack_from('>H', d, i)[0]; i += 2
        return d[i:i + n].decode('utf-8', 'replace'), i + n
    if typ == 9:
        t = d[i]; i += 1
        n = struct.unpack_from('>i', d, i)[0]; i += 4
        werte = []
        for _ in range(n):
            x, i = _lese(d, i, t)
            werte.append(x)
        return Liste(t, werte), i
    if typ == 10:
        werte = {}
        while True:
            t = d[i]; i += 1
            if t == 0: break
            ln = struct.unpack_from('>H', d, i)[0]; i += 2
            name = d[i:i + ln].decode('utf-8', 'replace'); i += ln
            x, i = _lese(d, i, t)
            werte[name] = x
        return werte, i
    if typ == 11:
        n = struct.unpack_from('>i', d, i)[0]; i += 4
        return list(struct.unpack_from('>%di' % n, d, i)), i + 4 * n
    if typ == 12:
        n = struct.unpack_from('>i', d, i)[0]; i += 4
        return list(struct.unpack_from('>%dq' % n, d, i)), i + 8 * n
    raise ValueError('unbekannter NBT-Typ %d' % typ)


class Liste(list):
    """Eine NBT-Liste, die ihren Elementtyp behaelt -- sonst geht er beim Schreiben verloren."""

    def __init__(self, elementtyp, werte):
        super().__init__(werte)
        self.elementtyp = elementtyp


def laden(pfad):
    d = open(pfad, 'rb').read()
    if d[:2] == b'\x1f\x8b':
        # NICHT gzip.decompress: mindestens eine der 79 Dateien des Originals hat hinter dem
        # gzip-Strom noch Datenmuell stehen, und darauf bricht gzip.decompress ab. Java liest
        # den Strom bis zum Ende des NBT und schaut nicht weiter -- decompressobj tut dasselbe.
        d = zlib.decompressobj(31).decompress(d)
    i = 0
    t = d[i]; i += 1
    ln = struct.unpack_from('>H', d, i)[0]; i += 2
    i += ln
    wert, _ = _lese(d, i, t)
    return wert


# ---------------------------------------------------------------- NBT schreiben

class Int(int): pass
class Byte(int): pass
class Short(int): pass
class Float(float): pass


def _typ_von(wert):
    if isinstance(wert, Byte): return 1
    if isinstance(wert, Short): return 2
    if isinstance(wert, Int): return 3
    if isinstance(wert, Float): return 5
    if isinstance(wert, bool): return 1
    if isinstance(wert, int): return 3
    if isinstance(wert, float): return 6
    if isinstance(wert, str): return 8
    if isinstance(wert, Liste): return 9
    if isinstance(wert, list): return 9
    if isinstance(wert, dict): return 10
    raise ValueError('kein NBT-Typ fuer %r' % type(wert))


def _schreibe(buf, typ, wert):
    if typ == 1: buf.write(struct.pack('>b', int(wert)))
    elif typ == 2: buf.write(struct.pack('>h', int(wert)))
    elif typ == 3: buf.write(struct.pack('>i', int(wert)))
    elif typ == 4: buf.write(struct.pack('>q', int(wert)))
    elif typ == 5: buf.write(struct.pack('>f', float(wert)))
    elif typ == 6: buf.write(struct.pack('>d', float(wert)))
    elif typ == 8:
        roh = wert.encode('utf-8')
        buf.write(struct.pack('>H', len(roh))); buf.write(roh)
    elif typ == 9:
        if isinstance(wert, Liste):
            et = wert.elementtyp
        elif wert:
            et = _typ_von(wert[0])
        else:
            et = 0
        buf.write(struct.pack('>b', et)); buf.write(struct.pack('>i', len(wert)))
        for x in wert:
            _schreibe(buf, et, x)
    elif typ == 10:
        for name, x in wert.items():
            t = _typ_von(x)
            buf.write(struct.pack('>b', t))
            roh = name.encode('utf-8')
            buf.write(struct.pack('>H', len(roh))); buf.write(roh)
            _schreibe(buf, t, x)
        buf.write(b'\x00')
    else:
        raise ValueError('kann NBT-Typ %d nicht schreiben' % typ)


def speichern(pfad, wurzel):
    buf = io.BytesIO()
    buf.write(struct.pack('>b', 10))
    buf.write(struct.pack('>H', 0))
    _schreibe(buf, 10, wurzel)
    with gzip.GzipFile(pfad, 'wb', mtime=0) as f:
        f.write(buf.getvalue())


# ---------------------------------------------------------------- Richtungen

# Die Seitenzahlen von 1.7.10 (ForgeDirection): 0 unten, 1 oben, dann die vier Himmelsrichtungen.
SEITE = {0: 'down', 1: 'up', 2: 'north', 3: 'south', 4: 'west', 5: 'east'}

GEGEN = {'north': 'south', 'south': 'north', 'west': 'east', 'east': 'west',
         'up': 'down', 'down': 'up'}

# Versatz je Richtung, fuer die Nachbarsuche.
VERSATZ = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0),
           'up': (0, 1, 0), 'down': (0, -1, 0)}

LINKS = {'north': 'west', 'west': 'south', 'south': 'east', 'east': 'north'}
RECHTS = {v: k for k, v in LINKS.items()}


# ---------------------------------------------------------------- Blocktabelle

def zustand(name, **eigenschaften):
    """Ein Blockzustand als (Name, Eigenschaften)-Paar."""
    return (name, {k: str(v).lower() if isinstance(v, bool) else str(v)
                   for k, v in eigenschaften.items()})


def _einfach(*namen):
    return {(n, 0): zustand(n if ':' in n else MODID + ':' + n) for n in namen}


# Bloecke, die im Port unter demselben Namen ohne Eigenschaften stehen.
GLEICH = [
    'meteor_brick', 'meteor_polished', 'meteor_brick_chiseled', 'meteor_battery',
    'meteor_spawner', 'block_meteor', 'block_meteor_cobble', 'block_starmetal',
    'ntm_dirt', 'crate', 'crate_red', 'deco_lead', 'tesla',
]

TABELLE = {}
for _n in GLEICH:
    TABELLE[('hbm:tile.' + _n, 0)] = zustand(MODID + ':' + _n)

# LUFT MIT METADATEN. In etlichen Dateien steht Luft mit einer Metadaten-Zahl ungleich null --
# Reste davon, was vor dem Abspeichern an der Stelle stand. Luft hat keine Spielarten; jede
# davon ist Luft.
for _m in range(16):
    TABELLE[('minecraft:air', _m)] = zustand('minecraft:air')

TABELLE.update({
    ('minecraft:glowstone', 0): zustand('minecraft:glowstone'),
    # 1.7.10 zaehlt die Wollfarben umgekehrt: 15 ist schwarz.
    ('minecraft:wool', 15): zustand('minecraft:black_wool'),

    # Der Block heisst im Original tnt, registriert ist er als tnt_ntm; der Port nennt
    # ihn wieder tnt.
    ('hbm:tile.tnt_ntm', 0): zustand(MODID + ':tnt'),

    # BALEFIRE ist im Port ein Feuerblock mit Alter; meta 0 heisst frisch gelegt.
    ('hbm:tile.balefire', 0): zustand(MODID + ':balefire', age=0),

    # DER BUNTE BETON war in 1.7.10 ein Block mit sechzehn Metadaten-Werten, die ueber
    # ItemDye.field_150921_b[~meta & 15] auf den Farbnamen zeigen. meta 5 ergibt lime.
    ('hbm:tile.concrete_colored', 5): zustand(MODID + ':concrete_lime'),

    # DER STATUENBLOCK. Registriert ist er im Original unter dem Namen "#undef"
    # (ModBlocks.java: statue_elb_f = new DecoBlockAlt(...).setBlockName("#undef")) -- ein
    # versteckter Block, unzerstoerbar und selbstleuchtend, gezeichnet von RenderDecoBlockAlt
    # mit einem eigenen Modell. Der Port hat die Statuenfamilie noch nicht; bis dahin steht
    # hier der Sockelstein, auf dem sie im Original steht.
    ('hbm:tile.#undef', 5): zustand(MODID + ':meteor_polished'),
})

# DER GIFTBLOCK ist eine Fluessigkeit; meta ist ihre Hoehe.
for _m in (0, 1):
    TABELLE[('hbm:tile.toxic_block', _m)] = zustand(MODID + ':toxic_block', level=_m)

# DIE SAEULE: 1.7.10 legt die Achse in die oberen zwei Bit (0 = y, 4 = x, 8 = z).
for _m, _a in ((0, 'y'), (4, 'x'), (8, 'z')):
    TABELLE[('hbm:tile.meteor_pillar', _m)] = zustand(MODID + ':meteor_pillar', axis=_a)

# STAHLWAND UND STAHLECKE tragen die Seitenzahl als Metadatum.
for _m in (2, 3, 4, 5):
    TABELLE[('hbm:tile.steel_wall', _m)] = zustand(MODID + ':steel_wall', facing=SEITE[_m])
    TABELLE[('hbm:tile.steel_corner', _m)] = zustand(MODID + ':steel_corner', facing=SEITE[_m])

# DIE LEITER. hbm:tile.ladder_tungsten gibt es im Original nicht mehr -- sie steht in
# MainRegistry.ignoreMappings, und NBTStructure macht aus einem unbekannten Namen Luft
# (BlockDefinition: "if(block == null) block = Blocks.air"). Das Leiterzimmer des
# Meteoritenverlieses hat heute also keine Leiter. Der Port setzt die Stahlleiter, die
# dasselbe ist und die es noch gibt.
for _m in (2, 3, 4, 5):
    TABELLE[('hbm:tile.ladder_tungsten', _m)] = zustand(MODID + ':ladder_steel', facing=SEITE[_m])

# DIE TREPPE. 1.7.10: die unteren zwei Bit sind die Richtung (0 Ost, 1 West, 2 Sued,
# 3 Nord), Bit 2 hebt sie an die Decke. Die Form (gerade oder Ecke) stand in 1.7.10 nicht
# in den Daten -- sie wurde beim Zeichnen aus den Nachbarn bestimmt. Genau das rechnet
# forme_treppen() weiter unten nach.
_TREPPENRICHTUNG = {0: 'east', 1: 'west', 2: 'south', 3: 'north'}
for _m in range(8):
    TABELLE[('hbm:tile.brick_obsidian_stairs', _m)] = zustand(
        MODID + ':brick_obsidian_stairs',
        facing=_TREPPENRICHTUNG[_m & 3],
        half='top' if _m & 4 else 'bottom',
        shape='straight', waterlogged=False)

# DER MASCHENDRAHTZAUN. Die Verbindungen rechnet forme_zaeune() nach.
TABELLE[('hbm:tile.fence_metal', 0)] = zustand(
    MODID + ':fence_metal', north=False, east=False, south=False, west=False, waterlogged=False)

# DIE REDSTONEFACKEL. 1.7.10: 1 Ost, 2 West, 3 Sued, 4 Nord (an der Wand), 5 auf dem Boden.
_FACKEL = {1: 'east', 2: 'west', 3: 'south', 4: 'north'}
for _m, _r in _FACKEL.items():
    TABELLE[('minecraft:redstone_torch', _m)] = zustand('minecraft:redstone_wall_torch', facing=_r, lit=True)
    TABELLE[('minecraft:unlit_redstone_torch', _m)] = zustand('minecraft:redstone_wall_torch', facing=_r, lit=False)
TABELLE[('minecraft:redstone_torch', 5)] = zustand('minecraft:redstone_torch', lit=True)
TABELLE[('minecraft:unlit_redstone_torch', 5)] = zustand('minecraft:redstone_torch', lit=False)

# DIE FALLENKISTE. 1.7.10 legt die Blickrichtung in meta 2..5 ab.
for _m in (2, 3, 4, 5):
    TABELLE[('minecraft:trapped_chest', _m)] = zustand(
        'minecraft:trapped_chest', facing=SEITE[_m], type='single', waterlogged=False)




# ---------------------------------------------------------------- hbm, eins zu eins

# Bloecke, die es im Port unter genau demselben Namen gibt und die in den Bauwerken nur mit
# Metadatum null vorkommen. Ihr Standardzustand ist die richtige Uebersetzung -- was sonst
# noch an Eigenschaften an ihnen haengt, stand in 1.7.10 nicht in der Datei.
EINS_ZU_EINS = [
    'barrel_corroded', 'barrel_plastic', 'block_aluminium', 'block_copper',
    'block_electrical_scrap', 'block_red_copper', 'block_scrap', 'block_slag', 'brick_asbestos',
    'brick_compound', 'brick_concrete', 'brick_concrete_broken', 'brick_concrete_cracked',
    'brick_concrete_mossy', 'brick_light', 'concrete', 'concrete_asbestos', 'concrete_rebar',
    'concrete_smooth', 'concrete_super_broken', 'crate_ammo', 'crate_can', 'crate_lead',
    'crate_metal', 'crate_supply', 'crate_weapon', 'deco_aluminium', 'deco_beryllium',
    'deco_loot', 'deco_red_copper', 'deco_rusty_steel', 'deco_steel', 'deco_titanium',
    'deco_tungsten', 'det_charge', 'dirt_dead', 'dungeon_spawner', 'gas_asbestos',
    'gravel_obsidian', 'hev_battery', 'leaves_layer', 'machine_funnel', 'mine_ap', 'mine_he',
    'mine_naval', 'mush', 'oil_spill', 'ore_oil_sand', 'pole_top', 'red_cable',
    'red_wire_coated', 'reinforced_brick', 'reinforced_glass', 'reinforced_glass_pane',
    'reinforced_light', 'reinforced_sand', 'reinforced_stone', 'sellafield_slaked', 'spikes',
    'spotlight_beam', 'spotlight_fluoro', 'tile_lab', 'tile_lab_broken', 'tile_lab_cracked',
    'turret_sentry_damaged', 'vitrified_barrel', 'waste_leaves', 'yellow_barrel',
]

for _n in EINS_ZU_EINS:
    TABELLE[('hbm:tile.' + _n, 0)] = zustand(MODID + ':' + _n)

# DREI FAESSER STEHEN IM PORT ANDERSHERUM: dort heisst zuerst das Fass, dann die Sorte.
for _alt, _neu in (('lox_barrel', 'barrel_lox'), ('pink_barrel', 'barrel_pink'),
                   ('red_barrel', 'barrel_red')):
    TABELLE[('hbm:tile.' + _alt, 0)] = zustand(MODID + ':' + _neu)

# ZWEI NAMEN KENNT DAS ORIGINAL SELBST NICHT MEHR; beide stehen dort in ignoreMappings.
# ore_coal_oil traegt das Original in seine eigene Ersatztabelle ein (NBTStructure:89) und
# macht Kohleerz daraus; hier steht dasselbe. barrel_iron hat keinen Nachfolger -- im
# Original wird daraus Luft, und dabei bleibt es.
TABELLE[('hbm:tile.ore_coal_oil', 0)] = zustand('minecraft:coal_ore')
TABELLE[('hbm:tile.barrel_iron', 0)] = zustand('minecraft:air')

# Der Waffentisch heisst im Port ohne das machine_ davor.
TABELLE[('hbm:tile.machine_weapon_table', 0)] = zustand(MODID + ':weapon_table')

# ---------------------------------------------------------------- Vanilla vor dem Flattening

# 1.7.10 legt Farbe, Holzart und Gesteinsart als Metadaten-Zahl ab; seit 1.13 ist jede davon
# ein eigener Block. Die Reihenfolgen unten sind die von damals und stehen fest.
FARBEN = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
          'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
HOLZ = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak']

# Treppen: die unteren zwei Bit sind die Richtung, Bit 2 haengt sie an die Decke.
TREPPENRICHTUNG = {0: 'east', 1: 'west', 2: 'south', 3: 'north'}


def _treppen(alt, neu):
    for meta in range(8):
        TABELLE[('minecraft:' + alt, meta)] = zustand(
            'minecraft:' + neu,
            facing=TREPPENRICHTUNG[meta & 3],
            half='top' if meta & 4 else 'bottom',
            shape='straight', waterlogged=False)


def _stufen(alt, neu_je_meta):
    """Halbstufen: meta & 7 waehlt den Werkstoff, Bit 3 hebt sie nach oben."""
    for meta, neu in neu_je_meta.items():
        TABELLE[('minecraft:' + alt, meta)] = zustand('minecraft:' + neu, type='bottom', waterlogged=False)
        TABELLE[('minecraft:' + alt, meta | 8)] = zustand('minecraft:' + neu, type='top', waterlogged=False)


def _schlicht(*paare):
    for alt, neu in paare:
        TABELLE[('minecraft:' + alt, 0)] = zustand('minecraft:' + neu)


_schlicht(
    ('bookshelf', 'bookshelf'), ('brick_block', 'bricks'), ('clay', 'clay'),
    ('coal_block', 'coal_block'), ('cobblestone', 'cobblestone'),
    ('crafting_table', 'crafting_table'), ('dirt', 'dirt'), ('grass', 'grass_block'),
    ('gravel', 'gravel'), ('sandstone', 'sandstone'), ('sponge', 'sponge'),
    ('stone', 'stone'), ('waterlily', 'lily_pad'), ('web', 'cobweb'),
)

TABELLE[('minecraft:fence', 0)] = zustand('minecraft:oak_fence',
        north=False, east=False, south=False, west=False, waterlogged=False)
TABELLE[('minecraft:cobblestone_wall', 0)] = zustand('minecraft:cobblestone_wall',
        north='none', east='none', south='none', west='none', up=True, waterlogged=False)
TABELLE[('minecraft:iron_bars', 0)] = zustand('minecraft:iron_bars',
        north=False, east=False, south=False, west=False, waterlogged=False)
TABELLE[('minecraft:glass_pane', 0)] = zustand('minecraft:glass_pane',
        north=False, east=False, south=False, west=False, waterlogged=False)
TABELLE[('minecraft:redstone_lamp', 0)] = zustand('minecraft:redstone_lamp', lit=False)
TABELLE[('minecraft:wooden_pressure_plate', 0)] = zustand('minecraft:oak_pressure_plate', powered=False)
TABELLE[('minecraft:flower_pot', 0)] = zustand('minecraft:flower_pot')
TABELLE[('minecraft:sand', 0)] = zustand('minecraft:sand')
TABELLE[('minecraft:sand', 1)] = zustand('minecraft:red_sand')
TABELLE[('minecraft:stonebrick', 0)] = zustand('minecraft:stone_bricks')
TABELLE[('minecraft:stonebrick', 1)] = zustand('minecraft:mossy_stone_bricks')
TABELLE[('minecraft:stonebrick', 2)] = zustand('minecraft:cracked_stone_bricks')
TABELLE[('minecraft:stonebrick', 3)] = zustand('minecraft:chiseled_stone_bricks')
# In 1.20.3 umbenannt: aus grass wurde short_grass, weil grass schon der Erdblock war.
TABELLE[('minecraft:tallgrass', 0)] = zustand('minecraft:dead_bush')
TABELLE[('minecraft:tallgrass', 1)] = zustand('minecraft:short_grass')
TABELLE[('minecraft:tallgrass', 2)] = zustand('minecraft:fern')

for _i, _f in enumerate(FARBEN):
    TABELLE[('minecraft:wool', _i)] = zustand('minecraft:%s_wool' % _f)
    TABELLE[('minecraft:stained_glass', _i)] = zustand('minecraft:%s_stained_glass' % _f)
    TABELLE[('minecraft:stained_hardened_clay', _i)] = zustand('minecraft:%s_terracotta' % _f)
    TABELLE[('minecraft:stained_glass_pane', _i)] = zustand('minecraft:%s_stained_glass_pane' % _f,
            north=False, east=False, south=False, west=False, waterlogged=False)

for _i, _h in enumerate(HOLZ):
    TABELLE[('minecraft:planks', _i)] = zustand('minecraft:%s_planks' % _h)

# Stamm: die unteren zwei Bit waehlen die Holzart, die oberen die Achse. 12 heisst rundum
# Rinde -- in 1.21 der wood-Block.
for _i, _h in enumerate(HOLZ[:4]):
    for _a, _achse in ((0, 'y'), (4, 'x'), (8, 'z')):
        TABELLE[('minecraft:log', _i + _a)] = zustand('minecraft:%s_log' % _h, axis=_achse)
    TABELLE[('minecraft:log', _i + 12)] = zustand('minecraft:%s_wood' % _h, axis='y')

# Laub: Bit 2 heisst "nicht verfallen", Bit 3 "auf Verfall pruefen".
for _i, _h in enumerate(HOLZ[:4]):
    for _z in (0, 4, 8, 12):
        TABELLE[('minecraft:leaves', _i + _z)] = zustand('minecraft:%s_leaves' % _h,
                persistent=bool(_z & 4), distance=7, waterlogged=False)

_treppen('brick_stairs', 'brick_stairs')
_treppen('oak_stairs', 'oak_stairs')
_treppen('spruce_stairs', 'spruce_stairs')
_treppen('dark_oak_stairs', 'dark_oak_stairs')
_treppen('sandstone_stairs', 'sandstone_stairs')
_treppen('stone_brick_stairs', 'stone_brick_stairs')
# stone_stairs hiess schon in 1.7.10 so, gemeint war immer die Bruchsteintreppe.
_treppen('stone_stairs', 'cobblestone_stairs')

_stufen('stone_slab', {0: 'smooth_stone_slab', 1: 'sandstone_slab', 2: 'petrified_oak_slab',
                       3: 'cobblestone_slab', 4: 'brick_slab', 5: 'stone_brick_slab',
                       6: 'nether_brick_slab', 7: 'quartz_slab'})
_stufen('wooden_slab', {_i: '%s_slab' % _h for _i, _h in enumerate(HOLZ)})

# Doppelstufen sind in 1.21 dieselbe Stufe mit type=double.
for _m, _n in ((0, 'smooth_stone_slab'), (1, 'sandstone_slab'), (2, 'petrified_oak_slab'),
               (3, 'cobblestone_slab'), (4, 'brick_slab'), (5, 'stone_brick_slab'),
               (6, 'nether_brick_slab'), (7, 'quartz_slab')):
    TABELLE[('minecraft:double_stone_slab', _m)] = zustand('minecraft:' + _n, type='double', waterlogged=False)
for _i, _h in enumerate(HOLZ):
    TABELLE[('minecraft:double_wooden_slab', _i)] = zustand('minecraft:%s_slab' % _h, type='double', waterlogged=False)

# Fluessigkeiten: die Zahl ist die Fuellhoehe, ab 8 faellt sie.
for _m in range(16):
    TABELLE[('minecraft:water', _m)] = zustand('minecraft:water', level=_m)
    TABELLE[('minecraft:lava', _m)] = zustand('minecraft:lava', level=_m)

# Fackeln: 1 bis 4 haengen an der Wand, 5 steht auf dem Boden.
_FACKELSEITE = {1: 'east', 2: 'west', 3: 'south', 4: 'north'}
for _m, _r in _FACKELSEITE.items():
    TABELLE[('minecraft:torch', _m)] = zustand('minecraft:wall_torch', facing=_r)
TABELLE[('minecraft:torch', 5)] = zustand('minecraft:torch')
TABELLE[('minecraft:torch', 0)] = zustand('minecraft:torch')

# Knopf und Hebel tragen ihre Seite in denselben Zahlen; Bit 3 heisst gedrueckt.
_KNOPF = {0: ('ceiling', 'north'), 1: ('wall', 'east'), 2: ('wall', 'west'),
          3: ('wall', 'south'), 4: ('wall', 'north'), 5: ('floor', 'north')}
for _m, (_seite, _r) in _KNOPF.items():
    for _p in (0, 8):
        TABELLE[('minecraft:stone_button', _m + _p)] = zustand('minecraft:stone_button',
                face=_seite, facing=_r, powered=bool(_p))

_HEBEL = {0: ('ceiling', 'west'), 1: ('wall', 'east'), 2: ('wall', 'west'), 3: ('wall', 'south'),
          4: ('wall', 'north'), 5: ('floor', 'north'), 6: ('floor', 'west'), 7: ('ceiling', 'north')}
for _m, (_seite, _r) in _HEBEL.items():
    for _p in (0, 8):
        TABELLE[('minecraft:lever', _m + _p)] = zustand('minecraft:lever',
                face=_seite, facing=_r, powered=bool(_p))

# Wandschild: die Zahl ist die Seite, in die es schaut.
for _m in (2, 3, 4, 5):
    TABELLE[('minecraft:wall_sign', _m)] = zustand('minecraft:oak_wall_sign', facing=SEITE[_m], waterlogged=False)

# Der Schaedel liegt in allen Dateien auf dem Boden (meta 1) und ist vom Typ 0, also ein
# Skelettschaedel; seine Drehung steht in der Blockentitaet und wandert in den Zustand.
for _m in range(16):
    TABELLE[('minecraft:skull', _m)] = zustand('minecraft:skeleton_skull', rotation=0)

# Ranken: 1 Sued, 2 West, 4 Nord, 8 Ost.
for _m in range(16):
    TABELLE[('minecraft:vine', _m)] = zustand('minecraft:vine',
            south=bool(_m & 1), west=bool(_m & 2), north=bool(_m & 4), east=bool(_m & 8), up=False)

# Doppelhohe Pflanzen: Bit 3 ist die obere Haelfte, und die obere traegt KEINE Sorte --
# welche Pflanze es ist, steht nur unten. forme_pflanzen() holt sie von dort.
_DOPPELPFLANZE = {0: 'sunflower', 1: 'lilac', 2: 'tall_grass', 3: 'large_fern',
                  4: 'rose_bush', 5: 'peony'}
for _m, _p in _DOPPELPFLANZE.items():
    TABELLE[('minecraft:double_plant', _m)] = zustand('minecraft:' + _p, half='lower')
for _m in range(8, 16):
    TABELLE[('minecraft:double_plant', _m)] = zustand('minecraft:tall_grass', half='upper')

# Verstaerker und Vergleicher: untere zwei Bit die Richtung, obere die Verzoegerung
# beziehungsweise die Betriebsart.
_DIODE = {0: 'south', 1: 'west', 2: 'north', 3: 'east'}
for _m in range(16):
    TABELLE[('minecraft:unpowered_repeater', _m)] = zustand('minecraft:repeater',
            facing=_DIODE[_m & 3], delay=(_m >> 2) + 1, locked=False, powered=False)
    TABELLE[('minecraft:powered_repeater', _m)] = zustand('minecraft:repeater',
            facing=_DIODE[_m & 3], delay=(_m >> 2) + 1, locked=False, powered=True)
    TABELLE[('minecraft:unpowered_comparator', _m)] = zustand('minecraft:comparator',
            facing=_DIODE[_m & 3], mode='subtract' if _m & 4 else 'compare', powered=False)
    TABELLE[('minecraft:powered_comparator', _m)] = zustand('minecraft:comparator',
            facing=_DIODE[_m & 3], mode='subtract' if _m & 4 else 'compare', powered=True)

# Falltuer: die Reihenfolge ist die des Setzens in 1.7.10 (Seite 2 bis 5 auf 0 bis 3).
_FALLTUER = {0: 'north', 1: 'south', 2: 'west', 3: 'east'}
for _m in range(16):
    TABELLE[('minecraft:trapdoor', _m)] = zustand('minecraft:oak_trapdoor',
            facing=_FALLTUER[_m & 3], half='top' if _m & 8 else 'bottom',
            open=bool(_m & 4), powered=False, waterlogged=False)

# Tuer und Bett: die untere Haelfte traegt die Richtung, die obere nur das Scharnier
# beziehungsweise nichts. forme_tueren() und forme_betten() holen sich das Fehlende.
_TUERRICHTUNG = {0: 'east', 1: 'south', 2: 'west', 3: 'north'}
for _m in range(8):
    TABELLE[('minecraft:wooden_door', _m)] = zustand('minecraft:oak_door',
            facing=_TUERRICHTUNG[_m & 3], half='lower', hinge='left',
            open=bool(_m & 4), powered=False)
for _m in range(8, 16):
    TABELLE[('minecraft:wooden_door', _m)] = zustand('minecraft:oak_door',
            facing='north', half='upper', hinge='right' if _m & 1 else 'left',
            open=False, powered=False)

_BETTRICHTUNG = {0: 'south', 1: 'west', 2: 'north', 3: 'east'}
for _m in range(16):
    TABELLE[('minecraft:bed', _m)] = zustand('minecraft:red_bed',
            facing=_BETTRICHTUNG[_m & 3], part='head' if _m & 8 else 'foot',
            occupied=bool(_m & 4))



# ---------------------------------------------------------------- hbm, Familien

def _hbm_treppen(name, ziel=None):
    """Treppen zaehlen im Port wie in Vanilla; nur der Name wechselt gelegentlich."""
    for meta in range(8):
        TABELLE[('hbm:tile.' + name, meta)] = zustand(
            MODID + ':' + (ziel or name),
            facing=TREPPENRICHTUNG[meta & 3],
            half='top' if meta & 4 else 'bottom',
            shape='straight', waterlogged=False)


for _t in ('brick_compound_stairs', 'brick_concrete_broken_stairs', 'brick_concrete_cracked_stairs',
           'brick_concrete_mossy_stairs', 'brick_concrete_stairs', 'brick_light_stairs',
           'concrete_asbestos_stairs', 'concrete_smooth_stairs', 'concrete_stairs',
           'lightstone_bricks_stairs', 'reinforced_brick_stairs', 'reinforced_stone_stairs',
           'brick_obsidian_stairs'):
    _hbm_treppen(_t)

# DIE STUFENBLOECKE. Im Original ist jeder von ihnen EIN Block, dessen untere drei Bit den
# Werkstoff waehlen und dessen viertes ihn nach oben hebt (BlockMultiSlab). Im Port ist jeder
# Werkstoff eine eigene Stufe. Die Reihenfolgen stehen in ModBlocks.java: die Werkstoffe
# werden dem Erbauer in genau dieser Folge uebergeben.
MULTISTUFEN = {
    # ModBlocks.java:1530
    'brick_slab': ['reinforced_stone', 'reinforced_brick', 'brick_obsidian', 'brick_light',
                   'brick_compound', 'brick_asbestos', 'brick_fire'],
    # ModBlocks.java:1528
    'concrete_brick_slab': ['brick_concrete', 'brick_concrete_mossy', 'brick_concrete_cracked',
                            'brick_concrete_broken', 'brick_ducrete'],
    # ModBlocks.java:1526
    'concrete_slab': ['concrete_smooth', 'concrete', 'concrete_asbestos', 'ducrete_smooth',
                      'ducrete', 'asphalt'],
}

# Werkstoffe, die der Port nicht als Stufe hat. Sie kommen in keinem der 79 Bauwerke vor --
# nachgeprueft mit --fehlliste --, stehen hier aber, damit die Tabelle nicht stillschweigend
# etwas Falsches liefert, falls sie doch einmal auftauchen.
STUFEN_OHNE_PORT = {'brick_obsidian', 'brick_asbestos', 'brick_fire', 'brick_ducrete',
                    'ducrete_smooth', 'ducrete', 'asphalt'}

for _name, _werkstoffe in MULTISTUFEN.items():
    for _i, _w in enumerate(_werkstoffe):
        if _w in STUFEN_OHNE_PORT:
            continue
        TABELLE[('hbm:tile.' + _name, _i)] = zustand(MODID + ':%s_slab' % _w, type='bottom', waterlogged=False)
        TABELLE[('hbm:tile.' + _name, _i + 8)] = zustand(MODID + ':%s_slab' % _w, type='top', waterlogged=False)
    # Die Doppelstufe ist ein eigener Block, dessen Metadatum nur den Werkstoff nennt.
    _doppel = _name.replace('_slab', '_double_slab')
    for _i, _w in enumerate(_werkstoffe):
        if _w in STUFEN_OHNE_PORT:
            continue
        TABELLE[('hbm:tile.' + _doppel, _i)] = zustand(MODID + ':%s_slab' % _w, type='double', waterlogged=False)

# DIE ROHRE. Im Port sind sie Saeulen; das Metadatum ist die Achse, wie bei jedem
# Saeulenblock von 1.7.10 (0 y, 4 x, 8 z).
ROHRE = [
    'deco_pipe', 'deco_pipe_rusted', 'deco_pipe_green', 'deco_pipe_green_rusted',
    'deco_pipe_red', 'deco_pipe_marked',
    'deco_pipe_rim', 'deco_pipe_rim_rusted', 'deco_pipe_rim_green', 'deco_pipe_rim_green_rusted',
    'deco_pipe_rim_red', 'deco_pipe_rim_marked',
    'deco_pipe_quad', 'deco_pipe_quad_rusted', 'deco_pipe_quad_green', 'deco_pipe_quad_green_rusted',
    'deco_pipe_quad_red', 'deco_pipe_quad_marked',
    'deco_pipe_framed', 'deco_pipe_framed_rusted', 'deco_pipe_framed_green',
    'deco_pipe_framed_green_rusted', 'deco_pipe_framed_red', 'deco_pipe_framed_marked',
]

for _r in ROHRE:
    for _m, _a in ((0, 'y'), (4, 'x'), (8, 'z')):
        TABELLE[('hbm:tile.' + _r, _m)] = zustand(MODID + ':' + _r, axis=_a)

# Zwei weitere Saeulen desselben Zuschnitts.
for _saeule in ('concrete_pillar', 'meteor_pillar'):
    for _m, _a in ((0, 'y'), (4, 'x'), (8, 'z')):
        TABELLE[('hbm:tile.' + _saeule, _m)] = zustand(MODID + ':' + _saeule, axis=_a)

# DER BUNTE BETON. Die Farbe steht als ~meta & 15 in ItemDye.field_150921_b -- dieselbe
# verkehrte Zaehlung wie bei der Wolle, nur dass hbm sie noch einmal umdreht.
BETONFARBEN = ['black', 'red', 'green', 'brown', 'blue', 'purple', 'cyan', 'light_gray',
               'gray', 'pink', 'lime', 'yellow', 'light_blue', 'magenta', 'orange', 'white']
for _m in range(16):
    TABELLE[('hbm:tile.concrete_colored', _m)] = zustand(MODID + ':concrete_' + BETONFARBEN[(~_m) & 15])

# Der erweiterte Beton zaehlt geradeaus, in der Reihenfolge von EnumConcreteType.
BETON_EXT = ['machine', 'machine_stripe', 'indigo', 'purple', 'pink', 'hazard', 'sand', 'bronze']
for _i, _n in enumerate(BETON_EXT):
    TABELLE[('hbm:tile.concrete_colored_ext', _i)] = zustand(MODID + ':concrete_ext_' + _n)

# DIE TUEREN zaehlen wie die Vanilla-Tuer; forme_tueren() holt der oberen Haelfte nach, was
# nur die untere weiss.
for _tuer in ('door_metal', 'door_office', 'door_bunker', 'door_red'):
    for _m in range(8):
        TABELLE[('hbm:tile.' + _tuer, _m)] = zustand(MODID + ':' + _tuer,
                facing=_TUERRICHTUNG[_m & 3], half='lower', hinge='left',
                open=bool(_m & 4), powered=False)
    for _m in range(8, 16):
        TABELLE[('hbm:tile.' + _tuer, _m)] = zustand(MODID + ':' + _tuer,
                facing='north', half='upper', hinge='right' if _m & 1 else 'left',
                open=False, powered=False)



# ---------------------------------------------------------------- hbm, Einzelstuecke

def _seitenblock(name, ziel=None, eigenschaft='facing'):
    """Bloecke, deren Metadatum die Seitenzahl ist (2 Nord bis 5 Ost)."""
    for meta in (2, 3, 4, 5):
        TABELLE[('hbm:tile.' + name, meta)] = zustand(MODID + ':' + (ziel or name), **{eigenschaft: SEITE[meta]})


for _b in ('charger', 'geiger', 'machine_boiler_off', 'radiorec', 'skeleton_holder',
           'steel_poles', 'tape_recorder', 'wood_barrier', 'ladder_steel', 'floodlight',
           'machine_diesel', 'machine_battery', 'pole_satellite_receiver'):
    _seitenblock(_b)

# DIE KETTE ist im Port eine Achse, im Original eine Seite: unten und oben sind dieselbe
# Achse, Nord und Sued auch.
for _m, _a in ((0, 'y'), (1, 'y'), (2, 'z'), (3, 'z'), (4, 'x'), (5, 'x')):
    TABELLE[('hbm:tile.dungeon_chain', _m)] = zustand(MODID + ':dungeon_chain', axis=_a, waterlogged=False)

# DAS GITTER: das Metadatum ist die Hoehe, in Achteln. Neun heisst einen Achtel unter dem
# Boden (BlockGrate.getY).
for _m in range(10):
    TABELLE[('hbm:tile.steel_grate', _m)] = zustand(MODID + ':steel_grate', layer=_m)
    TABELLE[('hbm:tile.steel_grate_wide', _m)] = zustand(MODID + ':steel_grate_wide', layer=_m)

# DER PANZERBETON zaehlt seinen Verfall von null bis fuenfzehn.
for _m in range(16):
    TABELLE[('hbm:tile.concrete_super', _m)] = zustand(MODID + ':concrete_super', decay=_m)

# DER MASCHENDRAHTZAUN: null ist das Feld, alles andere der Pfosten (BlockMetalFence.getIcon).
for _m in range(1, 16):
    TABELLE[('hbm:tile.fence_metal', _m)] = zustand(MODID + ':fence_metal_post',
            north=False, east=False, south=False, west=False, waterlogged=False)

# Die dunkle Lampe ist im Port dieselbe mit lit=false.
TABELLE[('hbm:tile.reinforced_lamp_off', 0)] = zustand(MODID + ':reinforced_lamp', lit=False)

# DREI BLOECKE HABEN IM PORT KEINE DREHUNG. Im Original liegt in ihrem Metadatum eine
# Ausrichtung; der Port hat sie als schlichte Bloecke angelegt (Runde 74). Alle Spielarten
# werden derselbe Block -- das ist ein Verlust an Vielfalt, aber kein falscher Block.
for _b in ('steel_beam', 'steel_roof', 'steel_scaffold'):
    for _m in range(16):
        TABELLE[('hbm:tile.' + _b, _m)] = zustand(MODID + ':' + _b)

# Die Vorratskiste kennt im Port keine Drehung.
for _m in range(16):
    TABELLE[('hbm:tile.crate_iron', _m)] = zustand(MODID + ':crate_iron')

# DIE TOTEN PFLANZEN: das Metadatum ist die Nummer in EnumDeadPlantType.
TOTE_PFLANZEN = ['generic', 'grass', 'flower', 'bigflower', 'fern']
for _i, _p in enumerate(TOTE_PFLANZEN):
    TABELLE[('hbm:tile.plant_dead', _i)] = zustand(MODID + ':plant_dead_' + _p)

# DER LEUCHTSTEIN: das Metadatum ist die Nummer in LightstoneType.
LEUCHTSTEIN = ['lightstone', 'lightstone_tile', 'lightstone_bricks',
               'lightstone_bricks_chiseled', 'lightstone_chiseled']
for _i, _l in enumerate(LEUCHTSTEIN):
    TABELLE[('hbm:tile.lightstone', _i)] = zustand(MODID + ':' + _l)

# DER AMBOSS: im Port ein Block mit Spielart. Der Bleiamboss ist Nummer eins.
for _m in range(16):
    TABELLE[('hbm:tile.anvil_lead', _m)] = zustand(MODID + ':anvil', subtype=1, facing='north')

# DIE STAHLFALLTUER zaehlt wie die Vanilla-Falltuer.
for _m in range(16):
    TABELLE[('hbm:tile.trapdoor_steel', _m)] = zustand(MODID + ':trapdoor_steel',
            facing=_FALLTUER[_m & 3], half='top' if _m & 8 else 'bottom',
            open=bool(_m & 4), powered=False, waterlogged=False)

# DAS SCHMALSPURGLEIS: dieselben Formen wie das Vanilla-Gleis.
GLEISFORM = {0: 'north_south', 1: 'east_west', 2: 'ascending_east', 3: 'ascending_west',
             4: 'ascending_north', 5: 'ascending_south', 6: 'south_east', 7: 'south_west',
             8: 'north_west', 9: 'north_east'}
for _m, _f in GLEISFORM.items():
    TABELLE[('hbm:tile.rail_narrow', _m)] = zustand(MODID + ':rail_narrow', shape=_f, waterlogged=False)



# DIE SCHEINWERFER: Bit 0 heisst zerschossen, die oberen drei sind die Seitenzahl
# (Spotlight.getDirection: metadata >> 1; isBroken: metadata & 1).
for _b in ('spotlight_halogen', 'spotlight_incandescent', 'spotlight_fluoro'):
    for _seite in range(6):
        for _kaputt in (0, 1):
            TABELLE[('hbm:tile.' + _b, (_seite << 1) | _kaputt)] = zustand(
                    MODID + ':' + _b, facing=SEITE[_seite], lit=True, broken=bool(_kaputt))
    # Die dunkle Fassung ist im Port dieselbe mit lit=false.
    for _seite in range(6):
        for _kaputt in (0, 1):
            TABELLE[('hbm:tile.' + _b + '_off', (_seite << 1) | _kaputt)] = zustand(
                    MODID + ':' + _b, facing=SEITE[_seite], lit=False, broken=bool(_kaputt))

# Der dunkle Ofen ebenso.
for _m in (2, 3, 4, 5):
    TABELLE[('hbm:tile.machine_electric_furnace_off', _m)] = zustand(
            MODID + ':machine_electric_furnace', facing=SEITE[_m], lit=False)

# DIE DEKO-MODELLE: das Metadatum ist (Drehung << 2) | Spielart, und die Drehung zaehlt
# 0 Nord, 1 Sued, 2 West, 3 Ost (BlockDecoModel.onBlockPlacedBy, dort auch der Kommentar).
_DEKODREHUNG = {0: 'north', 1: 'south', 2: 'west', 3: 'east'}


def _dekomodell(name, spielarten):
    for meta in range(16):
        spielart = meta & 3
        if spielart >= len(spielarten):
            continue
        TABELLE[('hbm:tile.' + name, meta)] = zustand(
                MODID + ':' + spielarten[spielart], facing=_DEKODREHUNG[meta >> 2])


_dekomodell('deco_computer', ['deco_computer'])

# CRT und Toaster zaehlen andersherum: bei ihnen ist die Spielart oben und die Drehung unten
# (BlockDecoCRT.damageDropped: (meta % 16) / 4).
def _dekovariante(name, spielarten):
    for meta in range(16):
        spielart = (meta % 16) // 4
        if spielart >= len(spielarten):
            continue
        TABELLE[('hbm:tile.' + name, meta)] = zustand(
                MODID + ':' + spielarten[spielart], facing=_DEKODREHUNG[meta % 4])


_dekovariante('deco_crt', ['deco_crt_clean', 'deco_crt_broken', 'deco_crt_blinking', 'deco_crt_bsod'])
_dekovariante('deco_toaster', ['deco_toaster_iron', 'deco_toaster_steel', 'deco_toaster_wood'])

# DER HOLZBAU: das Metadatum ist die Nummer in EnumWoodStructure.
for _i, _w in enumerate(['roof', 'scaffold', 'ceiling']):
    TABELLE[('hbm:tile.wood_structure', _i)] = zustand(MODID + ':wood_structure_' + _w)

# DER WACKELKOPF steht auf sechzehn Drehungen; WELCHER Kopf es ist, stand in 1.7.10 in der
# Blockentitaet und steht im Port in der Eigenschaft meta. Die Bauwerke setzen keinen
# bestimmten -- ohne Blockentitaet bleibt es der erste.
for _m in range(16):
    TABELLE[('hbm:tile.bobblehead', _m)] = zustand(MODID + ':bobblehead', direction=_m, meta=0)



# DIE MEHRBLOCKMASCHINEN. In 1.7.10 besteht so eine Maschine aus einem Kern und einer Wolke
# von Platzhaltern; das Metadatum sagt, was der Block ist und wohin die Maschine schaut
# (BlockDummyable, Kopfkommentar):
#
#   0 bis  5   Platzhalter, Richtung = Metadatum
#   6 bis 11   Platzhalter mit Merker ("extra"), Richtung = Metadatum minus 6
#  12 bis 15   der Kern, Richtung = Metadatum minus 10 (also nur die vier waagerechten)
#
# Der Port hat dafuer zwei Eigenschaften statt einer Zahl: facing und type.
MEHRBLOCK = {
    'machine_fluidtank': 'machine_fluid_tank',
    'machine_rotary_furnace': 'machine_rotary_furnace',
    'radio_telex': 'radio_telex',
    'turret_howard_damaged': 'turret_howard_damaged',
    # NICHT DABEI: turret_sentry_damaged. Er sieht aus wie sein Geschwister, ist im Port aber
    # keine DummyableBlock-Maschine, sondern ein schlichter Block ohne Eigenschaften -- und in
    # den Bauwerken steht er ohnehin nur mit Metadatum null. Das Zustands-Tor hat den Fehler
    # gemeldet, bevor ein Bauwerk damit gebaut wurde.
}

for _alt, _neu in MEHRBLOCK.items():
    for _m in range(6):
        TABELLE[('hbm:tile.' + _alt, _m)] = zustand(MODID + ':' + _neu, facing=SEITE[_m], type='dummy')
    for _m in range(6, 12):
        TABELLE[('hbm:tile.' + _alt, _m)] = zustand(MODID + ':' + _neu, facing=SEITE[_m - 6], type='extra')
    for _m in range(12, 16):
        TABELLE[('hbm:tile.' + _alt, _m)] = zustand(MODID + ':' + _neu, facing=SEITE[_m - 10], type='core')

# Die Mikrowelle hat im Port keine Drehung -- im Original steht in ihrem Metadatum eine.
for _m in range(16):
    TABELLE[('hbm:tile.machine_microwave', _m)] = zustand(MODID + ':machine_microwave')

# Der Rotdraht-Anschluss traegt die Seite, in die er zeigt.
for _m in range(6):
    TABELLE[('hbm:tile.red_connector', _m)] = zustand(MODID + ':red_connector', facing=SEITE[_m])

# DIE FLUESSIGKEITSLEITUNGEN. Das Messrohr hat im Port keine Eigenschaften -- was es misst,
# steht in seiner Blockentitaet. Die Standardleitung traegt ihre sechs Anschluesse und die
# Fluessigkeitsnummer; die Anschluesse rechnet 1.21 beim Setzen selbst aus, die Nummer
# stammt aus dem Metadatum.
for _m in range(16):
    TABELLE[('hbm:tile.fluid_duct_gauge', _m)] = zustand(MODID + ':fluid_duct_gauge')
    TABELLE[('hbm:tile.fluid_duct_neo', _m)] = zustand(MODID + ':fluid_duct_neo', meta=_m,
            north=False, south=False, east=False, west=False, up=False, down=False)


# Gegenstaende, die in Truheninhalten vorkommen. Wert None heisst: im Port nicht vorhanden,
# der Stapel faellt weg -- das ist eine Entscheidung, keine Luecke.
GEGENSTAENDE = {
    'minecraft:redstone_torch': 'minecraft:redstone_torch',
    'hbm:item.fragment_lanthanium': MODID + ':fragment_lanthanium',
    'hbm:item.fragment_boron': MODID + ':fragment_boron',
    # Der Port hat nur den ganzen Quecksilberklumpen, nicht den winzigen.
    'hbm:item.nugget_mercury_tiny': None,
    # ItemAutogen mit Werkstoffnummer; 2600 ist Eisen (Mats.MAT_IRON).
    ('hbm:item.pipe', 2600): MODID + ':pipe_iron',
}


# ---------------------------------------------------------------- Zauberstaebe

# Die Pools des Meteoritenverlieses heissen im Original schlicht "default", "spike", ...;
# in 1.21 ist ein Pool ein Datenobjekt mit vollem Namen.
def pool_name(roh, praefix):
    return '%s:%s/%s' % (MODID, praefix, roh)


def jigsaw(meta, te, praefix):
    """Aus einem wand_jigsaw wird der Jigsaw-Block von 1.21.

    Beide bedeuten dasselbe: eine Anschlussstelle, die nach einer Richtung zeigt, ein Stueck
    aus einem Pool zieht und danach zu dem Block wird, der in ihr steht. 1.21 legt die
    Richtung in orientation ab -- vorne_oben --, wobei fuer die vier waagerechten Richtungen
    oben immer up ist und fuer oben/unten eine beliebige Himmelsrichtung steht (die Fuge ist
    rollend, die Drehung also gleichgueltig).
    """
    vorne = SEITE[meta]
    if vorne == 'up':
        orientierung = 'up_north'
    elif vorne == 'down':
        orientierung = 'down_south'
    else:
        orientierung = vorne + '_up'

    ersatz = te.get('block', 'minecraft:air')
    ersatz = uebersetze_ersatzblock(ersatz, te.get('meta', 0))

    daten = {
        'id': 'minecraft:jigsaw',
        'name': '%s:%s' % (MODID, te.get('name', 'default')),
        'target': '%s:%s' % (MODID, te.get('target', 'default')),
        'pool': pool_name(te.get('pool', 'default'), praefix),
        'final_state': ersatz,
        'joint': 'rollable' if te.get('roll', 1) else 'aligned',
        'placement_priority': Int(te.get('placement', 0)),
        'selection_priority': Int(te.get('selection', 0)),
    }
    return zustand('minecraft:jigsaw', orientation=orientierung), daten


def uebersetze_ersatzblock(name, meta):
    if name in ('minecraft:air', '', None):
        return 'minecraft:air'
    eintrag = TABELLE.get((name, meta))
    if eintrag is None:
        raise KeyError('Ersatzblock eines Zauberstabs nicht in der Tabelle: %s meta %s' % (name, meta))
    blockname, eigenschaften = eintrag
    if not eigenschaften:
        return blockname
    return '%s[%s]' % (blockname, ','.join('%s=%s' % kv for kv in sorted(eigenschaften.items())))


# Welcher Block aus einem wand_loot wird.
ERSATZ_BEUTE = {
    'hbm:tile.deco_loot': MODID + ':deco_loot',
    'minecraft:chest': 'minecraft:chest',
    'hbm:tile.crate_steel': MODID + ':crate_steel',
    'hbm:tile.crate_iron': MODID + ':crate_iron',
    'hbm:tile.safe': MODID + ':safe',
    # 1.7.10 loest einen Blocknamen, der eine Zahl ist, ueber die Blockkennziffer auf
    # (Block.getBlockFromName). 54 ist die Truhe. Die beiden anderen Zahlen, die in den
    # Dateien stehen -- 557 und 683 --, zeigen auf Bloecke, deren Kennziffer nur in der
    # Welt des Urhebers galt; sie sind nicht aufloesbar und bleiben ein Fehler.
    '54': 'minecraft:chest',
    # Der Feldname im Original ist machine_rtg, der angemeldete Name machine_rtg_grey --
    # wieder ein Fall, in dem beide auseinandergehen. Der Port nimmt den Feldnamen.
    'hbm:tile.machine_rtg_grey': MODID + ':machine_rtg',
}


def beutestab(meta, te):
    """Aus einem wand_loot wird der Beutestab des Ports -- derselbe Block, dieselben Felder.

    Er ersetzt sich beim ersten Serverticken selbst, genau wie im Original
    (BlockWandLoot.TileEntityWandLoot.replace).
    """
    roh = te.get('block', 'hbm:tile.deco_loot')
    if roh not in ERSATZ_BEUTE:
        raise KeyError('wand_loot zeigt auf unbekannten Block: %s' % roh)

    daten = {
        'id': MODID + ':wand_loot',
        'block': ERSATZ_BEUTE[roh],
        'pool': te.get('pool', ''),
        'min': Int(te.get('min', 0)),
        'max': Int(te.get('max', 0)),
        'rot': Float(te.get('rot', 0.0)),
    }
    return zustand(MODID + ':wand_loot'), daten


# ---------------------------------------------------------------- Inhalte

def uebersetze_inhalt(te, gegenstandspalette):
    """Truheninhalte: 1.7.10 speichert Zahlen-IDs, dazu eine Palette Zahl -> Name."""
    posten = []
    for eintrag in te.get('Items', []):
        name = gegenstandspalette.get(eintrag.get('id'))
        if name is None:
            raise KeyError('Gegenstands-ID %s steht in keiner Palette' % eintrag.get('id'))
        schaden = eintrag.get('Damage', 0)
        ziel = GEGENSTAENDE.get((name, schaden), GEGENSTAENDE.get(name, KeyError))
        if ziel is KeyError:
            raise KeyError('Gegenstand nicht in der Tabelle: %s (Damage %s)' % (name, schaden))
        if ziel is None:
            continue
        posten.append({
            'slot': Byte(eintrag.get('Slot', 0)),
            'item': {'id': ziel, 'count': Int(eintrag.get('Count', 1))},
        })
    if not posten:
        return None
    return {'id': 'minecraft:trapped_chest', 'Items': Liste(10, posten)}


# Was in einem Blumentopf steht, war in 1.7.10 die Zahl des Gegenstands IN DER BLOCKENTITAET;
# seit 1.13 ist jeder gefuellte Topf ein eigener Block. Null heisst leer.
BLUMENTOPF = {
    0: 'minecraft:flower_pot',
    6: 'minecraft:potted_oak_sapling',
    31: 'minecraft:potted_fern',
    32: 'minecraft:potted_dead_bush',
    37: 'minecraft:potted_dandelion',
    38: 'minecraft:potted_poppy',
    39: 'minecraft:potted_brown_mushroom',
    40: 'minecraft:potted_red_mushroom',
    81: 'minecraft:potted_cactus',
}


# ---------------------------------------------------------------- Formen nachrechnen

def _ist_treppe(zust):
    return zust is not None and zust[0].endswith('_stairs')


def forme_treppen(gitter):
    """Setzt die shape-Eigenschaft jeder Treppe so, wie 1.21 sie aus den Nachbarn ableitet.

    1.7.10 kannte die Eigenschaft nicht und hat die Ecke beim Zeichnen bestimmt; die Regel
    unten ist die von Vanilla (StairBlock.makeStairShape), damit im Port dasselbe steht, was
    im Original zu sehen war.
    """
    for pos, zust in list(gitter.items()):
        if not _ist_treppe(zust):
            continue
        name, eig = zust
        vorne = eig['facing']
        halb = eig['half']

        form = 'straight'

        nachbar = _treppe_bei(gitter, pos, vorne)
        if nachbar and nachbar[1]['half'] == halb:
            richtung = nachbar[1]['facing']
            if richtung != vorne and richtung != GEGEN[vorne]:
                form = 'outer_left' if richtung == LINKS[vorne] else 'outer_right'

        if form == 'straight':
            nachbar = _treppe_bei(gitter, pos, GEGEN[vorne])
            if nachbar and nachbar[1]['half'] == halb:
                richtung = nachbar[1]['facing']
                if richtung != vorne and richtung != GEGEN[vorne]:
                    form = 'inner_left' if richtung == LINKS[vorne] else 'inner_right'

        eig['shape'] = form


def _treppe_bei(gitter, pos, richtung):
    dx, dy, dz = VERSATZ[richtung]
    nachbar = gitter.get((pos[0] + dx, pos[1] + dy, pos[2] + dz))
    return nachbar if _ist_treppe(nachbar) else None


# Bloecke, an die ein Zaun andockt: alles, was in diesen Bauwerken eine volle Wand ist.
KEINE_ZAUNVERBINDUNG = {'minecraft:air', MODID + ':fence_metal', MODID + ':fence_metal_post',
                        MODID + ':toxic_block',
                        MODID + ':balefire', 'minecraft:redstone_torch',
                        'minecraft:redstone_wall_torch', 'minecraft:jigsaw',
                        MODID + ':wand_loot'}


def forme_tueren(gitter):
    """Holt der oberen Tuerhaelfte, was nur die untere weiss -- Richtung und Zustand.

    1.7.10 legt die Richtung allein in die untere Haelfte und das Scharnier allein in die
    obere. 1.21 will beides in beiden, sonst zeichnet die Tuer sich verdreht.
    """
    for pos, zust in list(gitter.items()):
        if zust is None or not zust[0].endswith('_door'):
            continue
        if zust[1].get('half') != 'upper':
            continue

        unten = gitter.get((pos[0], pos[1] - 1, pos[2]))
        if unten is None or unten[0] != zust[0] or unten[1].get('half') != 'lower':
            continue

        zust[1]['facing'] = unten[1]['facing']
        zust[1]['open'] = unten[1]['open']
        unten[1]['hinge'] = zust[1]['hinge']


# Die sechs doppelhohen Pflanzen. Die obere Haelfte traegt in 1.7.10 keine Sorte.
DOPPELPFLANZEN = {'sunflower', 'lilac', 'tall_grass', 'large_fern', 'rose_bush', 'peony'}


def forme_pflanzen(gitter):
    """Gibt der oberen Haelfte einer doppelhohen Pflanze die Sorte der unteren."""
    for pos, zust in list(gitter.items()):
        if zust is None or zust[1].get('half') != 'upper':
            continue
        if zust[0].removeprefix('minecraft:') not in DOPPELPFLANZEN:
            continue

        unten = gitter.get((pos[0], pos[1] - 1, pos[2]))
        if unten is None or unten[1].get('half') != 'lower':
            continue
        if unten[0].removeprefix('minecraft:') not in DOPPELPFLANZEN:
            continue

        gitter[pos] = (unten[0], {'half': 'upper'})


def forme_zaeune(gitter):
    """Setzt die vier Verbindungen jedes Zaunfelds. 1.7.10 hat auch sie beim Zeichnen bestimmt."""
    for pos, zust in list(gitter.items()):
        if zust is None or zust[0] not in (MODID + ':fence_metal', MODID + ':fence_metal_post'):
            continue
        for richtung in ('north', 'east', 'south', 'west'):
            dx, dy, dz = VERSATZ[richtung]
            nachbar = gitter.get((pos[0] + dx, pos[1] + dy, pos[2] + dz))
            verbunden = nachbar is not None and nachbar[0] not in KEINE_ZAUNVERBINDUNG
            if nachbar is not None and nachbar[0] in (MODID + ':fence_metal', MODID + ':fence_metal_post'):
                verbunden = True
            zust[1][richtung] = 'true' if verbunden else 'false'


# ---------------------------------------------------------------- Umsetzen

def umsetzen(quelle, praefix):
    roh = laden(quelle)

    groesse = list(roh['size'])
    gegenstandspalette = {p['ID']: p['Name'] for p in roh.get('itemPalette', [])}
    palette = roh['palette']

    # Zustand und Blockentitaet je Position. Die Zustaende sind veraenderlich, weil Treppen
    # und Zaeune ihre Form erst kennen, wenn alle Nachbarn stehen.
    gitter = {}
    entitaeten = {}

    for block in roh['blocks']:
        pos = tuple(block['pos'])
        eintrag = palette[block['state']]
        name = eintrag['Name']
        meta = int(eintrag.get('Properties', {}).get('meta', '0'))
        te = block.get('nbt')

        if name == 'hbm:tile.wand_jigsaw':
            if te is None:
                raise ValueError('%s: wand_jigsaw ohne Blockentitaet bei %s' % (quelle, list(pos)))
            zust, daten = jigsaw(meta, te, praefix)
        elif name == 'hbm:tile.wand_loot':
            if te is None:
                raise ValueError('%s: wand_loot ohne Blockentitaet bei %s' % (quelle, list(pos)))
            zust, daten = beutestab(meta, te)
        elif name == 'minecraft:skull':
            # Der Schaedel: die Drehung steht in der Blockentitaet, in 1.21 im Zustand.
            # SkullType 0 ist der Skelettschaedel -- alle Vorkommen der 79 Dateien sind das.
            if te is not None and te.get('SkullType', 0) != 0:
                raise KeyError('%s: Schaedelsorte %s ist nicht uebersetzt' % (quelle, te.get('SkullType')))
            zust = ('minecraft:skeleton_skull', {'rotation': str(te.get('Rot', 0) if te else 0)})
            daten = None
        elif name == 'minecraft:flower_pot':
            # Der Blumentopf: was darin steht, stand in 1.7.10 in der Blockentitaet.
            inhalt = te.get('Item', 0) if te else 0
            if inhalt not in BLUMENTOPF:
                raise KeyError('%s: Blumentopfinhalt %s ist nicht uebersetzt' % (quelle, inhalt))
            zust = (BLUMENTOPF[inhalt], {})
            daten = None
        else:
            gefunden = TABELLE.get((name, meta))
            if gefunden is None:
                raise KeyError('%s: kein Eintrag fuer %s meta %d' % (quelle, name, meta))
            zust = (gefunden[0], dict(gefunden[1]))
            daten = uebersetze_inhalt(te, gegenstandspalette) if te else None

        gitter[pos] = zust
        if daten is not None:
            entitaeten[pos] = daten

    forme_treppen(gitter)
    forme_zaeune(gitter)
    forme_tueren(gitter)
    forme_pflanzen(gitter)

    # Palette einsammeln und Bloecke schreiben.
    paletteneu = []
    index = {}
    bloecke = []
    for pos in sorted(gitter, key=lambda p: (p[1], p[2], p[0])):
        name, eig = gitter[pos]
        schluessel = (name, tuple(sorted(eig.items())))
        if schluessel not in index:
            index[schluessel] = len(paletteneu)
            eintrag = {'Name': name}
            if eig:
                eintrag['Properties'] = dict(sorted(eig.items()))
            paletteneu.append(eintrag)
        eintrag = {'pos': Liste(3, [Int(pos[0]), Int(pos[1]), Int(pos[2])]),
                   'state': Int(index[schluessel])}
        if pos in entitaeten:
            eintrag['nbt'] = entitaeten[pos]
        bloecke.append(eintrag)

    return {
        'DataVersion': Int(DATA_VERSION),
        'size': Liste(3, [Int(groesse[0]), Int(groesse[1]), Int(groesse[2])]),
        'palette': Liste(10, paletteneu),
        'blocks': Liste(10, bloecke),
        'entities': Liste(10, []),
    }


def fehlliste(quellverzeichnis):
    """Zaehlt auf, welche (Name, meta)-Paare der Tabelle noch fehlen.

    Der Umsetzer selbst bricht beim ersten unbekannten Paar ab -- richtig so, beim Umsetzen.
    Zum Planen braucht man aber die ganze Liste auf einmal: wie viel Arbeit liegt noch vor
    einem, und in welchen Familien.
    """
    offen = {}
    dateien = {}
    for verzeichnis, _, namen in os.walk(quellverzeichnis):
        for name in sorted(namen):
            if not name.endswith('.nbt'):
                continue
            voll = os.path.join(verzeichnis, name)
            rel = os.path.relpath(voll, quellverzeichnis)
            wurzel = laden(voll)
            for eintrag in wurzel['palette']:
                blockname = eintrag['Name']
                if blockname in ('hbm:tile.wand_jigsaw', 'hbm:tile.wand_loot'):
                    continue
                meta = int(eintrag.get('Properties', {}).get('meta', '0'))
                if (blockname, meta) in TABELLE:
                    continue
                offen[(blockname, meta)] = offen.get((blockname, meta), 0) + 1
                dateien.setdefault((blockname, meta), set()).add(rel)

    vanilla = sorted(k for k in offen if k[0].startswith('minecraft:'))
    eigen = sorted(k for k in offen if not k[0].startswith('minecraft:'))

    print('Fehlliste der Umsetzungstabelle')
    print('  offene Paare gesamt : %d' % len(offen))
    print('  davon Vanilla       : %d  auf %d Blocknamen' % (len(vanilla), len({k[0] for k in vanilla})))
    print('  davon hbm           : %d  auf %d Blocknamen' % (len(eigen), len({k[0] for k in eigen})))
    print()
    for gruppe, titel in ((vanilla, 'VANILLA'), (eigen, 'HBM')):
        if not gruppe:
            continue
        print('--- %s ---' % titel)
        letzter = None
        for blockname, meta in gruppe:
            if blockname != letzter:
                print('  %s' % blockname)
                letzter = blockname
            print('      meta %-3d  %4dx  in %d Dateien' % (meta, offen[(blockname, meta)], len(dateien[(blockname, meta)])))
    return 0


def main(argv):
    if '--fehlliste' in argv:
        argv = [a for a in argv if a != '--fehlliste']
        if len(argv) < 2:
            print(__doc__)
            return 2
        return fehlliste(argv[1])

    nurpruefen = '--pruefe' in argv
    argv = [a for a in argv if a != '--pruefe']

    if len(argv) < 2:
        print(__doc__)
        return 2

    quellverzeichnis = argv[1]
    zielverzeichnis = argv[2] if len(argv) > 2 else None
    praefix = argv[3] if len(argv) > 3 else 'meteor'

    dateien = []
    for verzeichnis, _, namen in os.walk(quellverzeichnis):
        for name in namen:
            if name.endswith('.nbt'):
                voll = os.path.join(verzeichnis, name)
                dateien.append(os.path.relpath(voll, quellverzeichnis))
    dateien.sort()

    if not dateien:
        print('FEHLER: keine .nbt-Dateien in %s' % quellverzeichnis)
        return 1

    bloecke = 0
    for datei in dateien:
        wurzel = umsetzen(os.path.join(quellverzeichnis, datei), praefix)
        bloecke += len(wurzel['blocks'])
        if zielverzeichnis and not nurpruefen:
            ziel = os.path.join(zielverzeichnis, datei)
            os.makedirs(os.path.dirname(ziel), exist_ok=True)
            speichern(ziel, wurzel)

    print('%d Dateien umgesetzt, %d Bloecke, %s' % (
        len(dateien), bloecke, 'nur geprueft' if nurpruefen else 'geschrieben nach ' + str(zielverzeichnis)))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
