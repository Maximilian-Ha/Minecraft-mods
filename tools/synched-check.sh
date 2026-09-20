#!/usr/bin/env bash
# ---------------------------------------------------------------------------------------
# Tor 40: jede Entitaetsklasse, die man wirklich erzeugen kann, muss defineSynchedData
# haben -- es sei denn, eine ihrer Vorfahrinnen im Projekt hat es schon.
#
# Warum: Entity erklaert defineSynchedData(SynchedEntityData.Builder) abstrakt, und
# weder Projectile noch ThrowableProjectile fuellen es aus. Wer von einer dieser
# Klassen abstammt und es vergisst, ist nicht uebersetzbar -- aber das merkt man erst
# in der CI, eine halbe Stunde spaeter. Genau das ist der TauShot aus Runde 236
# gewesen: der einzige der acht ProjectileNT-Erben ohne diese Methode.
#
# WORAUF DAS TOR SCHAUT und worauf nicht: es kennt nur den Quelltext des Projekts.
# Ob eine Vanilla-Klasse die Methode mitbringt, kann es nicht nachsehen -- darum steht
# unten eine kurze, benannte Liste der Vanilla-Klassen, die es NICHT tun. Eine Kette,
# die bei einer anderen Vanilla-Klasse endet (Monster, Creeper, ThrowableItemProjectile
# und so fort), laesst das Tor in Ruhe: dort bringt Vanilla die Methode mit.
# Wird eine weitere solche Vanilla-Basis gebraucht, muss ihr Name hier dazu -- geraten
# wird nichts.
#
# NACHGEMESSEN (Runde 236): 45 Ketten enden bei einer der drei Klassen unten. Mit dem
# TauShot, wie die CI ihn abgelehnt hat, meldet das Tor genau ihn; mit der Berichtigung
# null Funde. Nimmt man Sawblade die Methode weg, meldet es genau Sawblade.
# ---------------------------------------------------------------------------------------
set -u
cd "$(dirname "$0")/.."

echo -n "Pruefe Datenanbindung der Entitaeten ... "

funde=$(python3 - <<'PY'
import os, re

# Vanilla-Klassen, die defineSynchedData NICHT ausfuellen. Entity erklaert es abstrakt,
# Projectile und ThrowableProjectile reichen es unveraendert weiter.
VANILLA_OHNE = {'Entity', 'Projectile', 'ThrowableProjectile'}

klassen = {}   # Name -> (Pfad, Elternname, abstrakt?, hat defineSynchedData?)

for wurzel, _, dateien in os.walk('src/main/java'):
    for d in dateien:
        if not d.endswith('.java'): continue
        pfad = os.path.join(wurzel, d)
        text = open(pfad, encoding='utf-8', errors='replace').read()
        sauber = re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', text, flags=re.S))

        m = re.search(r'\n(?:public\s+)?(abstract\s+)?(?:final\s+)?class\s+(\w+)([^{]*)\{', sauber)
        if not m: continue

        abstrakt = m.group(1) is not None
        name = m.group(2)

        # Den Kopf von seinen Typparametern befreien: "class X<T extends Entity> extends Y"
        # nennt sonst Entity als Elternklasse.
        kopf = m.group(3)
        vorher = None
        while vorher != kopf:
            vorher = kopf
            kopf = re.sub(r'<[^<>]*>', '', kopf)

        e = re.search(r'\bextends\s+([\w.]+)', kopf)
        if not e: continue
        eltern = e.group(1).split('.')[-1]

        # Die Methode heisst ueberall gleich; wie der Parametertyp geschrieben ist
        # (SynchedEntityData.Builder oder nur Builder), ist von Datei zu Datei verschieden.
        hat = re.search(r'\bdefineSynchedData\s*\(', sauber) is not None
        klassen[name] = (pfad, eltern, abstrakt, hat)

ketten = 0
funde = []

for name, (pfad, eltern, abstrakt, hat) in sorted(klassen.items()):
    if abstrakt: continue

    # Die Kette im Projekt hinauflaufen.
    hat_irgendwo = hat
    aktuell = eltern
    gesehen = {name}
    while aktuell in klassen and aktuell not in gesehen:
        gesehen.add(aktuell)
        _, naechster, _, hat_dort = klassen[aktuell]
        hat_irgendwo = hat_irgendwo or hat_dort
        aktuell = naechster

    # aktuell ist jetzt der erste Name ausserhalb des Projekts.
    if aktuell not in VANILLA_OHNE: continue

    ketten += 1
    if not hat_irgendwo:
        funde.append('%s: %s hat kein defineSynchedData, und keine ihrer Vorfahrinnen auch.' % (pfad, name))

print('KETTEN %d' % ketten)
for f in funde: print('FUND ' + f)
PY
)

ketten=$(echo "$funde" | grep '^KETTEN ' | awk '{print $2}')
treffer=$(echo "$funde" | grep -c '^FUND ' || true)

echo "$ketten Ketten enden bei Entity, Projectile oder ThrowableProjectile"
echo "  ohne defineSynchedData : $treffer"

if [ "$treffer" -gt 0 ]; then
    echo
    echo "NICHT UEBERSETZBAR -- Entity erklaert defineSynchedData abstrakt:"
    echo "$funde" | grep '^FUND ' | sed 's/^FUND /  /'
    exit 1
fi

echo "OK - jede erzeugbare Entitaet fuellt defineSynchedData aus."
