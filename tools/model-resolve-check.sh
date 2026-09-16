#!/usr/bin/env bash
# Prueft die ERZEUGTEN Modelle: laesst sich jedes Elternmodell aufloesen, und hat jeder
# Gegenstand ueberhaupt eines?
#
# WARUM DAS NICHT BEI DEN UEBRIGEN TOREN STEHT
# Die fuenfzehn Tore in tools/ arbeiten auf dem Quelltext und auf src/main/resources. Der
# groesste Teil der Modelle entsteht aber erst in runData, und runData braucht den
# Minecraft-Klassenpfad -- der ist in der Entwicklungsumgebung durch eine Organisations-
# richtlinie gesperrt. Diese Pruefung laeuft deshalb in der CI, direkt hinter runData.
#
# WAS SIE FINDET
# Das Spielprotokoll vom 16.09. zeigte neun Gegenstaende mit dem fehlenden-Modell-Wuerfel:
#   * drei Tueren -- doorBlockWithRenderType erzeugt kein Gegenstandsmodell
#   * zwei Zaeune -- fenceBlock erzeugt kein _inventory, das Gegenstandsmodell zeigte darauf
#   * pwr_block   -- Blockmodell da, blockItem vergessen
#   * zwei EnumMultiItem ohne eigene Textur je Wert -- registerItemModel schrieb gar nichts
#   * ammo_debug  -- nie im Erzeuger genannt
# Keines davon konnte ein Tor auf dem Quelltext sehen: die ersten sechs haengen daran, was der
# Erzeuger TUT, nicht daran, was im Quelltext STEHT.
#
# Verwendung:
#   tools/model-resolve-check.sh                 # src/main/resources + src/generated/resources
#   tools/model-resolve-check.sh DIR [DIR ...]   # eigene Baeume, fuer die Eigenprobe
#
# Exit-Code 0 = sauber, 1 = Fundstellen, 2 = nichts zu pruefen.

set -uo pipefail
cd "$(dirname "$0")/.."

# --selbstprobe baut eine Nachbildung der neun Fundstellen vom 16.09. und prueft, dass die
# Regel sie sieht. Ein Tor, dessen Empfindlichkeit niemand misst, ist eine Attrappe.
if [ "${1:-}" = "--selbstprobe" ]; then
  T=$(mktemp -d); trap 'rm -rf "$T"' EXIT
  mkdir -p "$T/assets/hbmsntm/models/item" "$T/assets/hbmsntm/lang"
  printf '{ "parent": "hbmsntm:block/fence_metal_inventory" }\n' > "$T/assets/hbmsntm/models/item/fence_metal.json"
  printf '{ "parent": "item/generated" }\n'                      > "$T/assets/hbmsntm/models/item/ingot_lead.json"
  cat > "$T/assets/hbmsntm/lang/en_us.json" <<'JSON'
{
  "block.hbmsntm.door_metal": "Metal Door",
  "block.hbmsntm.pwr_block": "PWR Block",
  "item.hbmsntm.pwr_fuel_hot": "Hot PWR Fuel",
  "item.hbmsntm.ingot_lead": "Lead Ingot",
  "block.hbmsntm.fence_metal": "Metal Fence"
}
JSON
  OUT=$("$0" "$T"); RC=$?
  ELTERN=$(printf '%s' "$OUT" | sed -n 's/.*Elternmodell fehlt *: \([0-9]*\).*/\1/p')
  OHNE=$(printf   '%s' "$OUT" | sed -n 's/.*Gegenstand ohne Modell *: \([0-9]*\).*/\1/p')
  if [ "$RC" = "1" ] && [ "$ELTERN" = "1" ] && [ "$OHNE" = "3" ]; then
    echo "OK - Selbstprobe: 1 fehlendes Elternmodell und 3 Gegenstaende ohne Modell erkannt."
    exit 0
  fi
  echo "FEHLER - die Selbstprobe erkennt die eingebauten Fehler nicht mehr:"
  printf '%s\n' "$OUT"
  exit 1
fi

if [ "$#" -gt 0 ]; then ROOTS=("$@"); else ROOTS=(src/main/resources src/generated/resources); fi

python3 - "${ROOTS[@]}" <<'PYEOF'
import json, os, sys

roots = [r for r in sys.argv[1:] if os.path.isdir(r)]

if not roots:
    print("Keiner der angegebenen Baeume ist vorhanden -- nichts zu pruefen.")
    print("In der CI steht diese Pruefung hinter runData; ohne die erzeugten Dateien")
    print("kann sie nicht aussagen.")
    sys.exit(2)

MOD = 'hbmsntm'

# ------------------------------------------------------------------ Modelle einsammeln
# pfad -> Datei, ueber alle Baeume; der spaetere Baum gewinnt (wie beim Packen)
models = {}
for root in roots:
    base = os.path.join(root, 'assets', MOD, 'models')
    for dirpath, _, names in os.walk(base):
        for fn in names:
            if not fn.endswith('.json'):
                continue
            full = os.path.join(dirpath, fn)
            key = os.path.relpath(full, base).replace(os.sep, '/')[:-len('.json')]
            models[key] = full

# ------------------------------------------------------------------ Eltern aufloesen
unresolved = []
for key in sorted(models):
    try:
        data = json.load(open(models[key], encoding='utf-8'))
    except (OSError, ValueError) as e:
        unresolved.append((key, 'unlesbar: %s' % e))
        continue

    parent = data.get('parent')
    if not isinstance(parent, str):
        continue
    if ':' not in parent:
        continue                      # vanilla, etwa "item/generated"
    ns, path = parent.split(':', 1)
    if ns != MOD:
        continue                      # anderer Mod oder minecraft -- nicht unsere Sache
    if path not in models:
        unresolved.append((key, 'Elternmodell %s fehlt' % parent))

# ------------------------------------------------------------------ Gegenstand ohne Modell
# Die erzeugte Sprachdatei ist die einzige vollstaendige Liste der Registrierungsnamen, die
# nach runData vorliegt. lang-check.sh stellt sicher, dass jeder Eintrag darin eine Namenszeile
# hat -- damit ist sie hier als Verzeichnis brauchbar.
# Neun Bloecke bekommen absichtlich KEINEN Gegenstand: sie stehen mit dem blanken
# BLOCKS.register im Quelltext statt mit einem der register-Helfer, die sonst jedem Block
# einen BlockItem mitgeben. Fluessigkeiten, Feuer, Wrapper -- nichts davon soll in der Hand
# liegen. Eine Namenszeile haben sie trotzdem, deshalb muessen sie hier ausgenommen werden.
# Kommt einer hinzu, faellt er auf und gehoert mit Begruendung in diese Liste.
OHNE_GEGENSTAND = {
    'balefire', 'barricade', 'corium', 'fire_digamma', 'icf_block',
    'mud', 'pile_block', 'rad_lava', 'volcanic_lava',
}

ohne_modell = []
lang = None
for root in roots:
    cand = os.path.join(root, 'assets', MOD, 'lang', 'en_us.json')
    if os.path.isfile(cand):
        lang = cand

if lang:
    keys = json.load(open(lang, encoding='utf-8'))
    for k in sorted(keys):
        teile = k.split('.')
        if len(teile) != 3: continue
        art, ns, name = teile
        if ns != MOD or art not in ('item', 'block'): continue
        if art == 'block' and name in OHNE_GEGENSTAND: continue
        if ('item/' + name) not in models:
            ohne_modell.append((art, name))

# ------------------------------------------------------------------ Bericht
print("Pruefe erzeugte Modelle ... %d Modelle in %d Baeumen%s"
      % (len(models), len(roots), ", Verzeichnis aus %s" % lang if lang else ", ohne Sprachdatei"))
print("  Elternmodell fehlt      : %d" % len(unresolved))
print("  Gegenstand ohne Modell  : %d" % len(ohne_modell))

if unresolved or ohne_modell:
    if unresolved:
        print("\nELTERNMODELL FEHLT:")
        for key, why in unresolved:
            print("  %s: %s" % (key, why))
    if ohne_modell:
        print("\nOHNE MODELL (zeigt im Spiel den fehlenden-Modell-Wuerfel):")
        for art, name in ohne_modell:
            print("  %s %s -- models/item/%s.json fehlt" % (art, name, name))
    sys.exit(1)

print("OK - jedes Elternmodell loest auf, und jeder Gegenstand hat ein Modell.")
PYEOF
