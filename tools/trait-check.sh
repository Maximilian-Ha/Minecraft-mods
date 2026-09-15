#!/usr/bin/env bash
#
# trait-check.sh -- prueft, ob jeder Fluid-Trait, den ein Fluid traegt, in FluidTrait
# registriert ist.
#
# WARUM ES DIESES TOR GIBT: FT_Toxin war seit Runde 14 portiert, aber seine Registrierung
# stand auskommentiert da. Vier Fluide tragen ihn (Chlor, Phosgen, Senfgas, Rotschlamm).
# Fluids.writeDefaultTraits schlaegt jeden Trait in traitNameMap nach; fuer einen nicht
# eingetragenen kam null zurueck, und der JsonWriter warf "NullPointerException: name == null".
# Das riss den ganzen Mod beim ERSTSTART mit -- also bei jeder frischen Installation.
#
# Kein Compiler findet das: die auskommentierte Zeile ist gueltiges Java, und der Trait wird
# ueberall sonst korrekt benutzt. Erst der Programmablauf faellt darueber.
#
# GEMESSEN: ueber den ganzen Baum null Funde. Mit der Registrierung von FT_Toxin wieder
# auskommentiert genau ein Fund, der die Klasse und die vier betroffenen Fluide benennt.

set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TRAITFILE="$ROOT/src/main/java/com/hbm/inventory/fluid/trait/FluidTrait.java"
FLUIDFILE="$ROOT/src/main/java/com/hbm/inventory/fluid/Fluids.java"

for f in "$TRAITFILE" "$FLUIDFILE"; do
    if [ ! -f "$f" ]; then echo "FEHLER: $f nicht gefunden."; exit 2; fi
done

python3 - "$TRAITFILE" "$FLUIDFILE" <<'PY'
import re, sys

traitfile, fluidfile = sys.argv[1], sys.argv[2]
trait = open(traitfile, encoding='utf-8').read()
fluid = open(fluidfile, encoding='utf-8').read()

# Registriert gilt nur, was NICHT auskommentiert ist: eine Zeile, deren erstes
# nichtleeres Zeichen bereits registerTrait ist.
registered = set(re.findall(r'^[ \t]*registerTrait\("([^"]+)"\s*,\s*(\w+)\.class\)', trait, re.M))
reg_classes = {c for _, c in registered}

# Auskommentierte Registrierungen getrennt sammeln -- sie sind der haeufigste Fall
# und verdienen eine eigene Meldung.
commented = set(re.findall(r'^[ \t]*//[ \t]*registerTrait\("([^"]+)"\s*,\s*(\w+)\.class\)', trait, re.M))

# Benutzt: jede per new erzeugte FT_-Klasse in Fluids.java, plus die Konstanten.
used = {}
for m in re.finditer(r'new (FT_\w+)\s*\(', fluid):
    used.setdefault(m.group(1), set())

# Welches Fluid traegt welchen Trait? Eine Fluidzeile beginnt mit NAME = new FluidType("NAME"
for line in fluid.split('\n'):
    fm = re.search(r'new FluidType\("([A-Z0-9_]+)"', line)
    if not fm: continue
    for cm in re.finditer(r'new (FT_\w+)\s*\(', line):
        used.setdefault(cm.group(1), set()).add(fm.group(1))

# Auch die Konstanten (GASEOUS = new FT_Gaseous() usw.) zaehlen als benutzt.
for cls in re.findall(r'public static final (FT_\w+)\s+[A-Z_0-9]+\s*=', fluid):
    used.setdefault(cls, set())

missing = sorted(c for c in used if c not in reg_classes)

print("Pruefe Fluid-Traits ... %d registriert, %d benutzt" % (len(reg_classes), len(used)))

if not missing:
    print("OK - jeder benutzte Trait ist registriert.")
    sys.exit(0)

print("  AUFFAELLIG: %d" % len(missing))
for cls in missing:
    carriers = sorted(used[cls])
    note = ""
    for name, c in commented:
        if c == cls:
            note = ' -- die Registrierung als "%s" steht auskommentiert da' % name
    where = (", getragen von: " + ", ".join(carriers)) if carriers else ""
    print("  %s ist in FluidTrait nicht registriert%s%s" % (cls, note, where))
print()
print("Ein nicht registrierter Trait laesst Fluids.writeDefaultTraits beim ERSTSTART")
print("mit einer NullPointerException abbrechen. Bitte registerTrait(...) ergaenzen.")
sys.exit(1)
PY
