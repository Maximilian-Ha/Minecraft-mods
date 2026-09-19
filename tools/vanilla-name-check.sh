#!/usr/bin/env bash
#
# Prueft, dass keine Klasse des Ports eine Methode erklaert, deren NAME zu einer festen
# Vanilla-Methode ihrer Oberklasse gehoert, aber einen anderen Rueckgabetyp hat.
#
# Hintergrund: In Runde 188 kostete das einen CI-Lauf. Die Chemikalienwolke hiess im Original
# EntityChemical und hatte ein getType(), das den Fluidtyp zurueckgab. In 1.21 hat aber schon
# net.minecraft.world.entity.Entity ein getType(), und das gibt EntityType zurueck:
#
#   error: getType() in Chemical cannot override getType() in Entity
#
# WARUM DIE UEBRIGEN TORE DAS NICHT SEHEN: tools/signature-check.sh vergleicht nur Methoden
# MIT @Override gegen die Mehrheit im Projekt -- hier stand kein @Override, und es sollte auch
# keines stehen. tools/syntax-check.sh uebersetzt ohne Minecraft-Klassenpfad und kennt die
# Oberklasse gar nicht. Der Fehler ist erst in der Abnahme aufgefallen.
#
# WIE ES OHNE KLASSENPFAD ENTSCHEIDBAR WIRD: die Vererbungskette steht im Baum selbst. Jede
# Klasse mit "extends" wird verfolgt, bis eine bekannte Vanilla-Wurzel erreicht ist; fuer
# jede Wurzel steht unten, welche Methodennamen dort fest belegt sind und was sie liefern.
#
# DIE LISTE IST BEWUSST KURZ. Sie enthaelt nur, was nachweislich schon schiefging, und waechst
# mit jedem weiteren Fall. Ein Tor, das raet, meldet Unsinn; dieses hier meldet nur, was
# gemessen ist.
#
# NACHGEMESSEN (Runde 188): ueber den ganzen Baum null Funde. Benennt man Chemical.getFluidType
# zurueck in getType, meldet die Pruefung genau diese Zeile -- und sonst nichts.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

JAVA = 'src/main/java'

# Wurzel -> { Methodenname: erlaubter Rueckgabetyp }
FESTE_NAMEN = {
    'Entity': {'getType': 'EntityType'},
}

# Was im Baum als diese Wurzel zaehlt (Vanilla-Klassen, die selbst davon erben).
VANILLA_ERBEN = {
    'Entity': {
        'Entity', 'Projectile', 'LivingEntity', 'Mob', 'PathfinderMob', 'Monster',
        'AbstractArrow', 'ThrowableProjectile', 'AbstractHurtingProjectile', 'ItemEntity',
        'ExperienceOrb', 'AbstractMinecart', 'Boat', 'Display', 'PartEntity',
    },
}

klassen = {}   # Klassenname -> (Oberklasse oder None, Pfad)
methoden = {}  # Klassenname -> Liste (Name, Rueckgabetyp, Zeile)

KLASSE = re.compile(r'\bclass\s+(\w+)(?:<[^>]*>)?\s*(?:extends\s+([\w.]+)(?:<[^>]*>)?)?')
METHODE = re.compile(r'^\s*(?:public|protected|private)\s+(?:static\s+|final\s+|synchronized\s+)*'
                     r'([\w.<>\[\], ?]+?)\s+(\w+)\s*\(')

for wurzel, _dirs, namen in os.walk(JAVA):
    for fn in namen:
        if not fn.endswith('.java'): continue
        pfad = os.path.join(wurzel, fn)
        quelle = open(pfad, encoding='utf-8', errors='replace').read()

        m = KLASSE.search(quelle)
        if not m: continue
        kls, ober = m.group(1), m.group(2)
        if ober: ober = ober.split('.')[-1]
        klassen[kls] = (ober, pfad)

        eigene = []
        for i, zeile in enumerate(quelle.split('\n'), 1):
            mm = METHODE.match(zeile)
            if not mm: continue
            rueck, name = mm.group(1).strip(), mm.group(2)
            if rueck in ('return', 'new', 'else', 'case'): continue
            eigene.append((name, rueck.split('<')[0].split('.')[-1], i))
        methoden[kls] = eigene


def wurzel_von(kls):
    """Die Vanilla-Wurzel dieser Klasse, oder None."""
    gesehen = set()
    aktuell = kls
    while aktuell and aktuell not in gesehen:
        gesehen.add(aktuell)
        eintrag = klassen.get(aktuell)
        ober = eintrag[0] if eintrag else aktuell
        if ober is None: return None
        for name, erben in VANILLA_ERBEN.items():
            if ober in erben: return name
        if ober not in klassen: return None
        aktuell = ober
    return None


probleme = []
geprueft = 0

for kls, eigene in methoden.items():
    wurzel = wurzel_von(kls)
    if wurzel is None: continue
    geprueft += 1

    for name, rueck, zeile in eigene:
        erlaubt = FESTE_NAMEN[wurzel].get(name)
        if erlaubt is None: continue
        if rueck != erlaubt:
            probleme.append('%s:%d: %s.%s() liefert %s -- in %s liefert %s() aber %s, und der '
                            'Name ist dort fest belegt'
                            % (klassen[kls][1], zeile, kls, name, rueck, wurzel, name, erlaubt))

print('Pruefe Namenskollisionen mit festen Vanilla-Methoden ... %d Klassen unter bekannten Wurzeln'
      % geprueft)

if probleme:
    print('  AUFFAELLIG: %d' % len(probleme))
    for p in probleme: print('  ' + p)
    sys.exit(1)

print('OK - keine Methode kollidiert mit einer fest belegten Vanilla-Methode.')
PYEOF
