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

Die Quelldateien liest man mit
    git show hbm-upstream/master:src/main/resources/assets/hbm/structures/<pfad>
heraus; tools/extract-structures.sh nimmt einem das ab.
"""

import gzip
import io
import os
import struct
import sys

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
        d = gzip.decompress(d)
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

TABELLE.update({
    ('minecraft:air', 0): zustand('minecraft:air'),
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


# Welcher Block aus einem wand_loot wird. Der Tresor fehlt dem Port; die Truhe tut dasselbe
# und haelt denselben Vorrat.
ERSATZ_BEUTE = {
    'hbm:tile.deco_loot': MODID + ':deco_loot',
    'minecraft:chest': 'minecraft:chest',
    'hbm:tile.safe': 'minecraft:chest',
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
KEINE_ZAUNVERBINDUNG = {'minecraft:air', MODID + ':fence_metal', MODID + ':toxic_block',
                        MODID + ':balefire', 'minecraft:redstone_torch',
                        'minecraft:redstone_wall_torch', 'minecraft:jigsaw',
                        MODID + ':wand_loot'}


def forme_zaeune(gitter):
    """Setzt die vier Verbindungen jedes Zaunfelds. 1.7.10 hat auch sie beim Zeichnen bestimmt."""
    for pos, zust in list(gitter.items()):
        if zust is None or zust[0] != MODID + ':fence_metal':
            continue
        for richtung in ('north', 'east', 'south', 'west'):
            dx, dy, dz = VERSATZ[richtung]
            nachbar = gitter.get((pos[0] + dx, pos[1] + dy, pos[2] + dz))
            verbunden = nachbar is not None and nachbar[0] not in KEINE_ZAUNVERBINDUNG
            if nachbar is not None and nachbar[0] == MODID + ':fence_metal':
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


def main(argv):
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
