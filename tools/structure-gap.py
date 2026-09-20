#!/usr/bin/env python3
"""Misst, wie viele Bloecke den 79 Bauwerken des Originals im Port noch fehlen.

WARUM: die Zahl ist von Hand nicht zuverlaessig zu fuehren. Aus einem Block des Originals
werden im Port oft mehrere (aus einem Metadaten-Block wird je Spielart einer), und einige
Namen in den Bauwerken sind gar keine Bloecke mehr. Beides zusammen fuehrt beim Zaehlen
schnell in die Irre -- genau das ist in den Runden 89 bis 92 passiert. Dieses Skript rechnet
die Zahl stattdessen jedes Mal neu aus.

VORGEHEN:
  1. Die 79 .nbt-Dateien werden aus dem Fernzweig hbm-upstream/master gelesen. Sie sind
     gzip-NBT im eigenen Format des Originals; ihre Palette nennt je Block einen Namen
     (hbm:tile.xyz) und eine Metadaten-Zahl.
  2. Die im Port angelegten Bloecke werden aus NtmBlocks.java gelesen.
  3. Die Differenz ist die Fehlliste. Davon abgezogen werden zwei Arten von Eintraegen,
     die keinen neuen Block brauchen -- siehe KEIN_BLOCK und FAMILIEN unten.

Aufruf: tools/structure-gap.py [--list]
"""

import io
import os
import re
import struct
import subprocess
import sys
import zlib

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UPSTREAM = "hbm-upstream/master"
STRUCTURES = "src/main/resources/assets/hbm/structures"
NTM_BLOCKS = "src/main/java/com/hbm/blocks/NtmBlocks.java"

# Namen, die in den Bauwerken stehen, aber keinen Block im Port brauchen.
KEIN_BLOCK = {
    # Kein Block, sondern eine Markierung des Originals.
    "#undef",
    # In 1.21 kein eigener Block mehr, sondern die zugehoerige Stufe mit type=double.
    "brick_double_slab", "concrete_brick_double_slab", "concrete_double_slab",
    # Der Port benennt sie anders herum; sie sind da.
    "concrete_brick_slab", "lox_barrel", "pink_barrel", "red_barrel",
    # Im Port heisst der Waffentisch weapon_table, ohne das machine_ davor,
    # und das TNT schlicht tnt.
    "machine_weapon_table", "tnt_ntm",
    # Der Port schreibt den Tank mit Unterstrich: machine_fluid_tank.
    "machine_fluidtank",
    # Der Feldname im Original ist machine_rtg, der angemeldete machine_rtg_grey; der Port
    # nimmt den Feldnamen. Wieder ein Fall, in dem beide auseinandergehen.
    "machine_rtg_grey",
    # Das Original selbst kennt sie nicht mehr: sie stehen dort in ignoreMappings,
    # der Liste der Altnamen, die beim Laden alter Welten stillschweigend wegfallen.
    # ladder_tungsten setzt tools/nbt2structure.py auf die Stahlleiter um -- im Original
    # wird sie zu Luft, weil BlockDefinition einen unbekannten Namen so aufloest.
    "barrel_iron", "ladder_tungsten", "ore_coal_oil",
    # Der Jigsaw-Stab ist in 1.21 der Jigsaw-Block von Vanilla. Er markiert die
    # Anschlussstelle zweier Bauwerksstuecke; beides tut dasselbe, und der Umsetzer
    # schreibt minecraft:jigsaw an seine Stelle.
    "wand_jigsaw",
    # Der Tandemstab steht in genau EINER der 79 Dateien: test-tandem-core.nbt. Das ist
    # eine Probedatei des Urhebers, die kein SpawnCondition benutzt -- kein Bauwerk, das
    # der Mod setzt, braucht ihn. Seine Aufgabe (ein Anschlussstueck erst setzen, wenn die
    # Chunks davor geladen sind) hat in 1.21 ohnehin das Bauwerkssystem selbst.
    "wand_tandem",
}

# Metadaten-Bloecke des Originals, aus denen im Port mehrere Bloecke geworden sind.
# Der Name links kommt in keinem Blocknamen des Ports mehr vor, die Sache ist aber erledigt;
# die Umsetzungstabelle bildet Name samt Metadaten-Zahl auf den jeweiligen Block ab.
FAMILIEN = {
    "brick_slab":           "reinforced_stone_slab, reinforced_brick_slab, brick_light_slab, brick_compound_slab",
    "concrete_colored":     "concrete_white ... concrete_black (sechzehn Farben)",
    "concrete_colored_ext": "concrete_ext_machine ... concrete_ext_bronze (acht Toene)",
    "reinforced_lamp_off":  "reinforced_lamp mit lit=false",
    "wood_structure":       "wood_structure_roof, wood_structure_scaffold, wood_structure_ceiling",
    "plant_dead":           "plant_dead_generic, _grass, _flower, _bigflower, _fern",
    "spotlight_incandescent_off": "spotlight_incandescent mit lit=false",
    "deco_crt":             "deco_crt_clean, _broken, _blinking, _bsod",
    "deco_toaster":         "deco_toaster_iron, _steel, _wood",
    "anvil_lead":           "anvil mit subtype=LEAD (NTMAnvilBlock.Variant)",
    "machine_electric_furnace_off": "machine_electric_furnace mit lit=false",
}


def git_show(path):
    return subprocess.run(["git", "-C", REPO, "show", "%s:%s" % (UPSTREAM, path)],
                          capture_output=True, check=True).stdout


def read_nbt(f):
    """Liest eine NBT-Wurzel. Reicht fuer das Format des Originals (version 1)."""

    def u1(): return struct.unpack(">B", f.read(1))[0]
    def u2(): return struct.unpack(">H", f.read(2))[0]
    def i4(): return struct.unpack(">i", f.read(4))[0]
    def s():
        n = u2()
        return f.read(n).decode("utf-8", "replace")

    def payload(t):
        if t == 1: return struct.unpack(">b", f.read(1))[0]
        if t == 2: return struct.unpack(">h", f.read(2))[0]
        if t == 3: return i4()
        if t == 4: return struct.unpack(">q", f.read(8))[0]
        if t == 5: return struct.unpack(">f", f.read(4))[0]
        if t == 6: return struct.unpack(">d", f.read(8))[0]
        if t == 7: return f.read(i4())
        if t == 8: return s()
        if t == 9:
            et = u1(); n = i4()
            return [payload(et) for _ in range(n)]
        if t == 10:
            d = {}
            while True:
                tt = u1()
                if tt == 0: return d
                # Name zuerst in eine Variable: in "d[s()] = payload(tt)" wertet Python die
                # rechte Seite zuerst aus, der Datenstrom liefe damit aus dem Takt.
                name = s()
                d[name] = payload(tt)
        if t == 11: return [i4() for _ in range(i4())]
        if t == 12: return [struct.unpack(">q", f.read(8))[0] for _ in range(i4())]
        raise ValueError("unbekannter NBT-Typ %d" % t)

    t = u1()
    if t == 0: return None
    s()
    return payload(t)


def structure_names():
    """Alle hbm-Blocknamen, die in den 79 Bauwerken vorkommen."""

    # -r, weil unter structures/ noch ein Unterverzeichnis (meteor) liegt.
    listing = [line for line in subprocess.run(
        ["git", "-C", REPO, "ls-tree", "-r", "--name-only", "%s:%s" % (UPSTREAM, STRUCTURES)],
        capture_output=True, check=True, text=True).stdout.split() if line.endswith(".nbt")]
    names = set()

    for entry in listing:
        raw = git_show("%s/%s" % (STRUCTURES, entry))
        if raw[:2] == b"\x1f\x8b":
            # Hinter dem gzip-Strom steht in manchen Dateien noch etwas; decompressobj
            # hoert am Stromende auf, gzip.decompress wuerde daran scheitern.
            raw = zlib.decompressobj(16 + zlib.MAX_WBITS).decompress(raw)
        root = read_nbt(io.BytesIO(raw))

        for block in root.get("palette", []):
            name = block.get("Name", "")
            if name.startswith("hbm:tile."):
                names.add(name[len("hbm:tile."):])

        # DIE PALETTE IST NICHT ALLES. Ein Beutestab nennt den Block, zu dem er wird, in
        # SEINER BLOCKENTITAET -- in der Palette steht nur der Stab. Wer nur die Palette
        # liest, uebersieht sie: der Aktenschrank (filing_cabinet) steht in vier Bauwerken
        # und hat bis Runde 255 in keiner Zaehlung gefehlt, weil niemand dort nachgesehen
        # hat. Ein Tor, das an einer Stelle nicht hinsieht, ist schlimmer als keines.
        for block in root.get("blocks", []):
            entity = block.get("nbt")
            if not entity:
                continue
            name = entity.get("block", "")
            if isinstance(name, str) and name.startswith("hbm:tile."):
                names.add(name[len("hbm:tile."):])

    return names, len(listing)


def port_blocks():
    """Alle im Port angelegten Blocknamen."""

    source = open(os.path.join(REPO, NTM_BLOCKS), encoding="utf-8").read()
    return set(re.findall(r'register(?:New|BlastInfoBlock|Pipe)?\(\s*"([a-z0-9_]+)"', source))


def main():
    wanted, files = structure_names()
    have = port_blocks()

    missing = wanted - have
    families = missing & set(FAMILIEN)
    real = sorted(missing - KEIN_BLOCK - families)

    print("Bauwerke des Originals         : %d" % files)
    print("darin benutzte hbm-Blocknamen  : %d" % len(wanted))
    print("davon im Port angelegt         : %d" % len(wanted & have))
    print("davon als Familie abgedeckt    : %d" % len(families))
    print("davon kein Block noetig        : %d" % len(missing & KEIN_BLOCK))
    print("ECHT FEHLEND                   : %d" % len(real))

    if "--list" in sys.argv:
        print()
        for name in real:
            print("  " + name)

    return 0


if __name__ == "__main__":
    sys.exit(main())
