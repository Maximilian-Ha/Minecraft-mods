#!/usr/bin/env bash
#
# Prueft die Mekanism-Anbindung gegen die Quellen von Mekanism.
#
# Gleiche Begruendung wie bei tools/hbm-api-check.sh: tools/syntax-check.sh uebersetzt
# ohne Klassenpfad und kann deshalb nicht sagen, ob ein Feld wie
# FissionReactorMultiblockData.lastBurnRate oder eine Methode wie
# TurbineMultiblockData.getProductionRate wirklich existiert. Genau diese Zugriffe zeigen
# aber in eine fremde, sich bewegende Quelle.
#
# Quelle von Mekanism, in dieser Reihenfolge:
#   $MEKANISM_SRC   (Pfad auf die Auscheckung, also das Verzeichnis mit src/api/java)
#   mek             (Nachbar-Auscheckung, wie sie CI anlegt)
# Findet sich keine, endet das Skript mit Hinweis und Erfolg -- ohne Quelle laesst sich
# nichts pruefen, und das darf den uebrigen Lauf nicht aufhalten.
#
# Geprueft werden die drei Quelltextsaetze von Mekanism: die API (mekanism.api), die
# Hauptmod (mekanism.common) und die Generatoren (mekanism.generators).

set -uo pipefail
cd "$(dirname "$0")/.."

ROOT="${MEKANISM_SRC:-}"
if [ -z "$ROOT" ] && [ -d "mek/src/api/java" ]; then ROOT="mek"; fi

if [ -z "$ROOT" ] || [ ! -d "$ROOT/src/api/java" ]; then
  echo "Mekanism-Quellen nicht gefunden (MEKANISM_SRC oder mek/) -- Pruefung uebersprungen."
  exit 0
fi

API_CHECK_NAME="Mekanism" \
API_CHECK_DIR="src/main/java/com/zuxelus/energycontrol/crossmod/mekanism" \
API_CHECK_SRC="$ROOT/src/api/java:$ROOT/src/main/java:$ROOT/src/generators/java" \
API_CHECK_PKGS="mekanism.api,mekanism.common,mekanism.generators" \
python3 tools/api_check.py
