#!/usr/bin/env bash
# Registrier-Gate fuer den Port.
#
# WARUM DAS EXISTIERT
# -------------------
# Fast jede Runde schreibt NtmItems.IRGENDWAS.get() oder NtmBlocks.IRGENDWAS. Steht das Feld
# nicht da, ist das ein glatter Uebersetzungsfehler -- und der Syntax-Torwaechter sieht ihn NICHT.
#
# Gemessen in Runde 130: ein absichtlich eingefuegtes NtmItems.WIRE_GIBTESNICHT.get() lief durch
# alle sechs Tore, ohne dass eines anschlug. Der Grund ist derselbe wie bei den doppelten
# Konstanten aus Runde 121: javac kennt die Klasse NtmItems, aber der Feldtyp DeferredItem<Item>
# ist ohne NeoForge-Klassenpfad ein Fehlertyp. Sobald der Typ fehlerhaft ist, bricht javac die
# Attributierung des Feldzugriffs ab und meldet gar nichts mehr. Offline ist der Zugriff also
# unsichtbar, und der Fehler faellt erst in der CI auf -- sechs Minuten spaeter.
#
# Geprueft wird deshalb TEXTUELL, ohne javac: jeder Zugriff der Form Registrierklasse.NAME wird
# gegen die Felder nachgeschlagen, die die Klasse wirklich erklaert.
#
# WAS NICHT GEPRUEFT WIRD
# -----------------------
# Methodenaufrufe (Registrierklasse.name(...)) -- die sieht der Uebersetzer, weil ihr Rueckgabetyp
# an der Signatur haengt und nicht an einem Fehlertyp.
#
# Exit-Code 0 = sauber, 1 = mindestens ein Zugriff auf ein Feld, das es nicht gibt.

set -uo pipefail

cd "$(dirname "$0")/.."

python3 - <<'PY'
import os, re, sys

SRC = "src/main/java"

# Die Klassen, deren Felder nur ueber Fehlertypen erreichbar sind -- genau die, die javac
# durchwinkt. Der Name muss eindeutig sein, sonst faende der Zugriff die falsche Klasse.
REGISTRIES = {}

# NICHT NUR final: ein Teil der Waffenfelder ist "public static" ohne final und wird erst
# spaeter zugewiesen. Und nicht nur "=": manche stehen als blosse Erklaerung mit Semikolon.
decl = re.compile(r'^\s*public\s+static\s+(?:final\s+)?\S.*?\b(?P<name>[A-Z][A-Z0-9_]*)\s*[=;]', re.M)

def collect(path, cls):
    with open(path, encoding="utf-8") as f:
        body = f.read()
    REGISTRIES[cls] = set(m.group("name") for m in decl.finditer(body))

WANTED = [
    ("com/hbm/items/NtmItems.java",                  "NtmItems"),
    ("com/hbm/blocks/NtmBlocks.java",                "NtmBlocks"),
    ("com/hbm/blockentity/NtmBlockEntityTypes.java", "NtmBlockEntityTypes"),
    ("com/hbm/inventory/NtmMenuTypes.java",          "NtmMenuTypes"),
    ("com/hbm/entity/NtmEntityTypes.java",           "NtmEntityTypes"),
    ("com/hbm/registry/NtmSoundEvents.java",         "NtmSoundEvents"),
]

for rel, cls in WANTED:
    path = os.path.join(SRC, rel)
    if not os.path.exists(path):
        print("  FEHLT: %s -- die Regel kennt eine Klasse, die es nicht mehr gibt" % rel)
        sys.exit(1)
    collect(path, cls)

known_total = sum(len(v) for v in REGISTRIES.values())

# Zugriff: Klasse.NAME, aber NICHT Klasse.name( -- Methoden sieht javac selbst.
use = re.compile(r'\b(' + "|".join(REGISTRIES) + r')\s*\.\s*([A-Z][A-Za-z0-9_]*)\b(\s*\()?')

checked = 0
bad = []

for root, _, files in os.walk(SRC):
    for name in files:
        if not name.endswith(".java"):
            continue
        path = os.path.join(root, name)
        with open(path, encoding="utf-8") as f:
            lines = f.readlines()
        for no, line in enumerate(lines, 1):
            stripped = line.lstrip()
            if stripped.startswith("//") or stripped.startswith("*"):
                continue
            for cls, field, call in use.findall(line):
                if call:          # Methodenaufruf
                    continue
                checked += 1
                if field not in REGISTRIES[cls]:
                    bad.append((path, no, cls, field))

print("Pruefe Registrier-Zugriffe ... %d bekannte Felder in %d Klassen, %d Zugriffe"
      % (known_total, len(REGISTRIES), checked))

if bad:
    for path, no, cls, field in bad:
        print("  UNBEKANNT: %s.%s" % (cls, field))
        print("             %s:%d" % (path, no))
    print("  FEHLEND : %d" % len(bad))
    sys.exit(1)

print("OK - jeder Zugriff trifft ein erklaertes Feld.")
PY
