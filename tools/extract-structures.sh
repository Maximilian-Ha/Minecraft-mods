#!/usr/bin/env bash
# Holt die .nbt-Bauwerke des Originals aus dem Fernzweig hbm-upstream/master in ein
# Arbeitsverzeichnis. Sie liegen NICHT im Port -- sie sind das Eingangsmaterial fuer
# tools/nbt2structure.py, und das Ergebnis der Umsetzung ist das, was eingecheckt wird.
#
# Aufruf: tools/extract-structures.sh <zielverzeichnis> [praefix]
#   praefix schraenkt auf einen Unterbaum ein, z. B. "meteor/".
set -euo pipefail

ZIEL="${1:?Zielverzeichnis fehlt}"
PRAEFIX="${2:-}"
QUELLE="src/main/resources/assets/hbm/structures"

mkdir -p "$ZIEL"

anzahl=0
while read -r pfad; do
    rel="${pfad#$QUELLE/}"
    [ -n "$PRAEFIX" ] && case "$rel" in "$PRAEFIX"*) ;; *) continue ;; esac
    mkdir -p "$ZIEL/$(dirname "$rel")"
    git show "hbm-upstream/master:$pfad" > "$ZIEL/$rel"
    anzahl=$((anzahl + 1))
done < <(git ls-tree -r --name-only hbm-upstream/master -- "$QUELLE" | grep '\.nbt$')

echo "$anzahl Bauwerke nach $ZIEL geholt"
