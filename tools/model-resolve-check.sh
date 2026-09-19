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
  # canned_asbestos ist die Namenszeile eines METAWERTS von canned_conserve, kein eigener
  # Gegenstand. Sie steht hier, damit die Probe auch den Fehlalarm abfaengt, den der erste
  # Entwurf siebenundzwanzigfach erzeugt hat.
  cat > "$T/assets/hbmsntm/lang/en_us.json" <<'JSON'
{
  "block.hbmsntm.door_metal": "Metal Door",
  "block.hbmsntm.pwr_block": "PWR Block",
  "item.hbmsntm.pwr_fuel_hot": "Hot PWR Fuel",
  "item.hbmsntm.ingot_lead": "Lead Ingot",
  "item.hbmsntm.canned_asbestos": "Canned Asbestos",
  "block.hbmsntm.fence_metal": "Metal Fence"
}
JSON
  OUT=$("$0" "$T"); RC=$?
  ELTERN=$(printf '%s' "$OUT" | sed -n 's/.*Elternmodell fehlt *: \([0-9]*\).*/\1/p')
  OHNE=$(printf   '%s' "$OUT" | sed -n 's/.*Gegenstand ohne Modell *: \([0-9]*\).*/\1/p')
  if [ "$RC" = "1" ] && [ "$ELTERN" = "1" ] && [ "$OHNE" = "3" ]; then
    echo "OK - Selbstprobe: 1 fehlendes Elternmodell, 3 Gegenstaende ohne Modell, Metawert nicht gemeldet."
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
# Die erzeugte Sprachdatei nennt jeden Gegenstand -- aber nicht nur die. Metagegenstaende
# geben je Wert eine eigene Namenszeile aus, obwohl nur EIN Gegenstand registriert ist:
# canned_conserve etwa liefert siebenundzwanzig Zeilen item.hbmsntm.canned_asbestos,
# canned_spam und so weiter. Der erste Entwurf dieser Pruefung hielt sie fuer Gegenstaende und
# meldete siebenundzwanzig Fehlalarme.
#
# Deshalb wird die Sprachdatei gegen die tatsaechlichen Registrierungsnamen gesiebt: jeder
# Name, der im Quelltext als register("name", ...) auftaucht. Das erfasst NtmItems, die
# register-Helfer von NtmBlocks und auch das, was GunFactory zur Laufzeit anmeldet -- ammo_debug
# kam genau von dort und war der Grund, warum model-check.sh ihn nicht sah.
#
# Die Liste enthaelt auch Fluide, Toene und Menues; das schadet nicht, denn nur item.- und
# block.-Zeilen werden ueberhaupt gegen sie gehalten. Umgekehrt gilt: was der Ausdruck nicht
# faengt, bleibt unbemerkt -- eine blinde Stelle, kein Fehlalarm.
# Elf Bloecke bekommen absichtlich KEINEN Gegenstand: sie stehen mit dem blanken
# BLOCKS.register im Quelltext statt mit einem der register-Helfer, die sonst jedem Block
# einen BlockItem mitgeben. Fluessigkeiten, Feuer, Wrapper -- nichts davon soll in der Hand
# liegen. Eine Namenszeile haben sie trotzdem, deshalb muessen sie hier ausgenommen werden.
# Kommt einer hinzu, faellt er auf und gehoert mit Begruendung in diese Liste.
OHNE_GEGENSTAND = {
    'balefire', 'barricade', 'corium', 'fire_digamma', 'icf_block',
    'mud', 'pile_block', 'rad_lava', 'spotlight_beam', 'toxic_block', 'volcanic_lava',
    # Der Lichtfleck des Flutlichts: wie spotlight_beam nur Licht, nichts zum Anfassen.
    'floodlight_beam',
    # Die Schlackenpfuetze: entsteht nur unter dem Schlackenabstich und gibt beim Abbauen
    # einen Schrottklumpen her, nicht sich selbst. Im Original ebenso ohne Kreativreiter.
    'slag',
}

def registry_namen(quelle='src/main/java'):
    import re as _re
    namen = set()
    REG = _re.compile(r'\bregister\w*\(\s*"([a-z0-9_]+)"\s*,')
    for dirpath, _, names in os.walk(quelle):
        for fn in names:
            if not fn.endswith('.java'):
                continue
            t = open(os.path.join(dirpath, fn), encoding='utf-8', errors='replace').read()
            t = _re.sub(r'//[^\n]*', '', _re.sub(r'/\*.*?\*/', '', t, flags=_re.S))
            namen |= set(REG.findall(t))
    return namen

ohne_modell = []
lang = None
for root in roots:
    cand = os.path.join(root, 'assets', MOD, 'lang', 'en_us.json')
    if os.path.isfile(cand):
        lang = cand

registriert = registry_namen() if os.path.isdir('src/main/java') else None

if lang and registriert:
    keys = json.load(open(lang, encoding='utf-8'))
    for k in sorted(keys):
        teile = k.split('.')
        if len(teile) != 3: continue
        art, ns, name = teile
        if ns != MOD or art not in ('item', 'block'): continue
        if name not in registriert: continue      # Namenszeile eines Metawerts, kein Gegenstand
        if art == 'block' and name in OHNE_GEGENSTAND: continue
        if ('item/' + name) not in models:
            ohne_modell.append((art, name))

# ------------------------------------------------------------------ Liste gegen Quelltext
# Dieser Teil laeuft AUCH OHNE erzeugte Modelle und damit auch auf dem Entwicklungsrechner.
# Er gleicht ab, welche Bloecke im Quelltext mit blankem BLOCKS.register angelegt sind -- also
# ohne Gegenstand -- und haelt das gegen OHNE_GEGENSTAND. Ohne ihn faellt ein neuer solcher
# Block erst in der CI auf, wo allein "runData" die Modelle erzeugt; genau das ist beim
# Giftblock passiert und hat einen Lauf gekostet.
import re as _re2

blank = set()
for dirpath, _, names in os.walk('src/main/java'):
    for fn in names:
        if not fn.endswith('.java'): continue
        text = open(os.path.join(dirpath, fn), encoding='utf-8').read()
        blank |= set(_re2.findall(r'\bBLOCKS\.register\(\s*"([a-z0-9_]+)"\s*,', text))

fehlt_in_liste = sorted(blank - OHNE_GEGENSTAND)
zuviel_in_liste = sorted(OHNE_GEGENSTAND - blank)

# ------------------------------------------------ Handgeschriebener Zustand ohne Gegenstand
# Runde 182: die Pruefung "Gegenstand ohne Modell" oben braucht die erzeugte Sprachdatei, um
# zu wissen, welche Gegenstaende es ueberhaupt gibt. Die entsteht erst in runData -- lokal
# steht sie nicht zur Verfuegung, und die Pruefung wird uebersprungen. Genau das hat den
# Beutesockel durchgelassen: sein models/item/deco_loot.json fehlte, alle Tore waren gruen,
# und CI hat es gefunden.
#
# EIN TEIL DAVON IST OHNE SPRACHDATEI ENTSCHEIDBAR. Wer seinen Blockzustand VON HAND in
# src/main/resources schreibt, bekommt vom Datenerzeuger auch kein Gegenstandsmodell -- er
# laeuft ja gar nicht ueber ihn. Fuer diese Bloecke muss das Gegenstandsmodell ebenfalls von
# Hand dastehen. Das sind wenige (elf zur Zeit), aber es ist genau die Gruppe, in der der
# Fehler entsteht.
#
# NACHGEMESSEN (Runde 182): elf handgeschriebene Zustaende, zehn mit eigenem Gegenstandsmodell,
# einer (slag) absichtlich ohne Gegenstand und deshalb in OHNE_GEGENSTAND. Null Funde. Nimmt
# man models/item/deco_loot.json wieder heraus, meldet die Pruefung genau ihn.

HAND_BS = 'src/main/resources/assets/hbmsntm/blockstates'
HAND_IM = 'src/main/resources/assets/hbmsntm/models/item'

zustand_ohne_item = []
if os.path.isdir(HAND_BS) and os.path.isdir(HAND_IM):
    hand_item = {f[:-5] for f in os.listdir(HAND_IM) if f.endswith('.json')}
    for f in sorted(os.listdir(HAND_BS)):
        if not f.endswith('.json'): continue
        name = f[:-5]
        if name in hand_item or name in OHNE_GEGENSTAND: continue
        zustand_ohne_item.append(name)

# ------------------------------------------------------------------ Bericht
print("Pruefe erzeugte Modelle ... %d Modelle in %d Baeumen%s%s"
      % (len(models), len(roots),
         ", Verzeichnis aus %s" % lang if lang else ", ohne Sprachdatei",
         ", %d Registrierungsnamen" % len(registriert) if registriert else ""))
print("  Elternmodell fehlt      : %d" % len(unresolved))
print("  Gegenstand ohne Modell  : %d" % len(ohne_modell))
print("  Bloecke ohne Gegenstand : %d im Quelltext, %d nicht in der Liste, %d ueberfluessig"
      % (len(blank), len(fehlt_in_liste), len(zuviel_in_liste)))
print("  Handzustand ohne Bild   : %d" % len(zustand_ohne_item))

if fehlt_in_liste or zuviel_in_liste:
    if fehlt_in_liste:
        print("\nMIT BLANKEM BLOCKS.register ANGELEGT, ABER NICHT IN OHNE_GEGENSTAND:")
        for name in fehlt_in_liste:
            print("  %s -- entweder einen Gegenstand geben oder mit Begruendung eintragen" % name)
    if zuviel_in_liste:
        print("\nIN OHNE_GEGENSTAND, ABER NICHT MEHR SO ANGELEGT:")
        for name in zuviel_in_liste:
            print("  %s -- Eintrag streichen" % name)
    sys.exit(1)

if zustand_ohne_item:
    print("\nHANDGESCHRIEBENER BLOCKZUSTAND OHNE HANDGESCHRIEBENES GEGENSTANDSMODELL:")
    for name in zustand_ohne_item:
        print("  %s -- models/item/%s.json fehlt; der Datenerzeuger legt es nicht an, denn der "
              "Blockzustand kommt nicht von ihm" % (name, name))
    sys.exit(1)

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
