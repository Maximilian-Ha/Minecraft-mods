#!/usr/bin/env bash
#
# Prueft die HBM-Anbindung gegen die Quellen des HBM-Ports.
#
# WARUM DAS EXISTIERT
# -------------------
# tools/syntax-check.sh uebersetzt ohne Minecraft-Klassenpfad. Dabei zerfaellt jede
# Typinformation, und javac kann nicht mehr sagen, ob ein Feld wie RBMKBaseBlockEntity.heat
# oder eine Methode wie CompatExternal.getCoreFromPos wirklich existiert -- alles landet in
# der Sammelmeldung "cannot find symbol", die dort gefiltert wird.
#
# Genau diese Zugriffe sind aber die empfindlichste Stelle des ganzen Mods: sie zeigen in
# eine fremde, sich bewegende Quelle. Benennt der HBM-Port ein Feld um, faellt das sonst
# erst im vollen Bau auf -- oder gar nicht, wenn gerade keine HBM-JAR zur Hand ist.
#
# Dieses Skript liest deshalb die Quellen des HBM-Ports selbst und prueft:
#   1. Jeder Import aus com.hbm / api.hbm zeigt auf eine vorhandene Datei.
#   2. Jedes Feld und jede Methode, die die Anbindung auf einem solchen Typ anspricht,
#      steht in dessen Datei oder in einer ihrer Oberklassen/Schnittstellen.
#
# Es ersetzt keinen Bau -- Signaturen und Typen prueft es nicht. Es faengt aber die
# Fehlerklasse ab, die beim Mitwachsen mit dem HBM-Port entsteht: umbenannt, verschoben,
# entfernt.
#
# Quelle des HBM-Ports, in dieser Reihenfolge:
#   $HBM_SRC              (Pfad auf ein src/main/java)
#   hbm/src/main/java     (Nachbar-Auscheckung des HBM-Zweigs)
# Findet sich keine, endet das Skript mit Hinweis und Erfolg -- ohne Quelle laesst sich
# nichts pruefen, und das darf den uebrigen Lauf nicht aufhalten.

set -uo pipefail
cd "$(dirname "$0")/.."

SRC="${HBM_SRC:-}"
if [ -z "$SRC" ] && [ -d "hbm/src/main/java" ]; then SRC="hbm/src/main/java"; fi

if [ -z "$SRC" ] || [ ! -d "$SRC" ]; then
  echo "HBM-Quellen nicht gefunden (HBM_SRC oder hbm/src/main/java) -- Pruefung uebersprungen."
  exit 0
fi

HBM_SRC="$SRC" python3 tools/hbm_api_check.py
