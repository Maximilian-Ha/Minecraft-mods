#!/usr/bin/env bash
# Prueft, ob im Java-Code referenzierte Assets tatsaechlich vorhanden sind.
#
# WARUM: `gradlew build` uebersetzt nur. Ein Tippfehler in einem Texturpfad oder eine
# vergessene Datei faellt erst im laufenden Spiel auf -- als fehlende Textur oder als
# Absturz beim Laden eines OBJ-Modells. Dieses Skript findet so etwas vorher.
#
# Geprueft werden:
#   NuclearTechMod.withDefaultNamespace("textures/...")   -> assets/hbmsntm/...
#   new HFRWavefrontObject("models/obj/...")              -> assets/hbmsntm/...
#   modLoc("block/xyz")  in den Datengeneratoren          -> assets/hbmsntm/textures/block/xyz.png
#
# Exit-Code 0 = alle Referenzen aufloesbar, 1 = mindestens eine fehlt.

set -uo pipefail
cd "$(dirname "$0")/.."

ASSETS="src/main/resources/assets/hbmsntm"
MISSING=0
CHECKED=0

check() { # $1 = erwarteter Pfad, $2 = Fundstelle
  CHECKED=$((CHECKED + 1))
  if [ ! -f "$1" ]; then
    echo "  FEHLT: $1"
    echo "         referenziert in $2"
    MISSING=$((MISSING + 1))
  fi
}

echo "Pruefe Asset-Referenzen ..."

# withDefaultNamespace("...") mit Dateiendung -> direkter Pfad unter assets/hbmsntm/
while IFS= read -r line; do
  file="${line%%:*}"
  ref=$(echo "$line" | grep -oP 'withDefaultNamespace\("\K[^"]+\.(png|ogg|json)' | head -1)
  [ -z "$ref" ] && continue
  check "$ASSETS/$ref" "$file"
# resources.put(...) legt eine zur Laufzeit erzeugte Ressource an -- keine Datei auf der Platte.
done < <(grep -rn 'withDefaultNamespace("[^"]*\.\(png\|ogg\|json\)"' src/main/java --include='*.java' | grep -v 'resources.put(')

# HFRWavefrontObject("models/obj/...")
while IFS= read -r line; do
  file="${line%%:*}"
  ref=$(echo "$line" | grep -oP 'HFRWavefrontObject\("\K[^"]+' | head -1)
  [ -z "$ref" ] && continue
  check "$ASSETS/$ref" "$file"
done < <(grep -rn 'HFRWavefrontObject("' src/main/java --include='*.java')

# modLoc("block/...") in den Datengeneratoren -> Blocktextur.
# Zeilen mit UncheckedModelFile sind ausgenommen: dort bezeichnet modLoc ein ELTERN-MODELL
# (models/block/*.json), das der Datengenerator selbst erzeugt -- etwa die von layeringBlock
# erstellten Stufen leaves_layer_1 .. _8. Das waeren sonst reine Fehlalarme.
while IFS= read -r line; do
  file="${line%%:*}"
  for ref in $(echo "$line" | grep -oP 'modLoc\("block/\K[^"]+'); do
    check "$ASSETS/textures/block/$ref.png" "$file"
  done
done < <(grep -rn 'modLoc("block/' src/main/java/com/hbm/datagen --include='*.java' | grep -v 'UncheckedModelFile')

# Handgeschriebene Modelldateien unter models/block und models/item verweisen auf Texturen
# ("hbmsntm:block/xyz") und auf ein Elternmodell ("hbmsntm:block/abc"). Der Datengenerator
# erzeugt seine Modelle selbst und prueft dabei nichts nach; diese Dateien schreibt niemand
# ausser uns, und ein Tippfehler darin faellt sonst erst im Spiel auf.
while IFS= read -r line; do
  ref="${line%%|*}"
  file="${line#*|}"
  check "$ASSETS/$ref" "$file"
done < <(python3 - "$ASSETS" <<'PYEOF'
import json, os, sys

wurzel = sys.argv[1]

def melde(pfad, quelle):
    print("%s|%s" % (pfad, quelle))

for unterbau in ("models/block", "models/item"):
    ordner = os.path.join(wurzel, unterbau)
    if not os.path.isdir(ordner): continue
    for name in sorted(os.listdir(ordner)):
        if not name.endswith(".json"): continue
        quelle = os.path.join(ordner, name)
        try:
            daten = json.load(open(quelle))
        except Exception as fehler:
            print("KAPUTT: %s (%s)" % (quelle, fehler), file=sys.stderr)
            sys.exit(2)

        eltern = daten.get("parent")
        # Ohne Namensraum meint das Vanille (block/block, item/generated) -- nicht unsere Sache.
        if isinstance(eltern, str) and eltern.startswith("hbmsntm:"):
            melde("models/%s.json" % eltern.split(":", 1)[1], quelle)

        for wert in (daten.get("textures") or {}).values():
            # "#seite" verweist auf einen anderen Eintrag derselben Tabelle, nicht auf eine Datei.
            if not isinstance(wert, str) or wert.startswith("#"): continue
            if not wert.startswith("hbmsntm:"): continue
            melde("textures/%s.png" % wert.split(":", 1)[1], quelle)
PYEOF
)

echo "  geprueft: $CHECKED Referenzen"
if [ "$MISSING" -gt 0 ]; then
  echo "  FEHLEND : $MISSING"
  exit 1
fi
echo "OK - alle Referenzen aufloesbar."
exit 0
