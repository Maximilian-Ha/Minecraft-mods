#!/usr/bin/env bash
# Ton-Gate fuer den Port.
#
# WARUM DAS EXISTIERT
# -------------------
# Ein Tonereignis wird an ZWEI Stellen erklaert: als SoundEvent in NtmSoundEvents und als
# Eintrag in der sounds.json, der auf eine .ogg-Datei zeigt. Fehlt die zweite Haelfte, ist das
# Ereignis anstandslos registriert, spielbar -- und STUMM. Kein Fehler, keine Meldung, kein
# Absturz; es passiert einfach nichts.
#
# Aufgefallen ist das in Runde 127 beim Bau der Sirene: alarm.hatch, alarm.soyuzed und
# alarm.chime standen seit Langem als Ereignis da, ihre .ogg-Dateien lagen im Ordner, und in der
# sounds.json stand kein Wort davon.
#
# WAS SICH GEAENDERT HAT
# ----------------------
# Die sounds.json wird erzeugt, nicht geschrieben: NtmSoundDefinitionsProvider schreibt sie beim
# runData-Lauf. Daneben lag bis zuletzt eine handgeschriebene Fassung im selben Pfad -- zwei
# Dateien auf einem Ziel, von denen beim Bauen eine gewinnt. Dieses Tor hat deshalb lange die
# falsche Datei gemessen und 107 Ereignisse als stumm gefuehrt, die der Erzeuger laengst
# abdeckte. Die Handdatei ist fort, ihre 19 Sirenen stehen jetzt im Erzeuger, und hier wird
# gemessen, was der Erzeuger tatsaechlich schreibt. Eine Baseline braucht es nicht mehr: alle
# 179 Ereignisse haben einen Eintrag.
#
# Geprueft wird dreierlei:
#   1. Jedes registrierte Ereignis hat einen Eintrag im Erzeuger.
#   2. Jeder Eintrag zeigt auf eine Datei, die es gibt.
#   3. Jeder Eintrag gehoert zu einem registrierten Ereignis.
#
# GEMESSEN: 179 Ereignisse, 179 Eintraege, null Funde. Mit auskommentiertem ALARM_KLAXON-Eintrag
# genau ein Fund, mit verstellter Datei genau einer.
#
# Exit-Code 0 = sauber, 1 = stumme Ereignisse, fehlende Dateien oder verwaiste Eintraege.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - "$@" <<'PYEOF'
import re, pathlib, sys

QUELLE  = 'src/main/java/com/hbm/registry/NtmSoundEvents.java'
ERZEUGER= 'src/main/java/com/hbm/datagen/NtmSoundDefinitionsProvider.java'
ORDNER  = pathlib.Path('src/main/resources/assets/hbmsntm/sounds')

quelle = open(QUELLE, encoding='utf-8').read()
# Feldname -> Registriername, damit der Erzeuger auf Ereignisse abgebildet werden kann.
felder = dict(re.findall(r'\b([A-Z][A-Z0-9_]*)\s*=\s*reg\(\s*"([^"]+)"\s*\)', quelle))
registriert = re.findall(r'reg\("([^"]+)"\)', quelle)

# Kommentare fallen weg: ein auskommentierter Eintrag ist gueltiges Java und sieht fuer eine
# Regel wie ein vorhandener aus. Genau so hat sich FT_Toxin drei Runden lang versteckt.
erzeuger = re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', open(ERZEUGER, encoding='utf-8').read(), flags=re.S))

eintraege = {}
unbekannt = []
for teil in re.split(r'this\.add\(NtmSoundEvents\.', erzeuger)[1:]:
    feld = re.match(r'([A-Z0-9_]+)', teil).group(1)
    name = felder.get(feld)
    if name is None:
        unbekannt.append(feld); continue
    # Beide Schreibweisen: sound("hbmsntm:pfad") und sound(withDefaultNamespace("pfad")).
    dateien = re.findall(r'sound\(\s*(?:NuclearTechMod\.withDefaultNamespace\(\s*)?"([^"]+)"', teil)
    eintraege[name] = [d.split(':')[-1] for d in dateien]

print(f"Pruefe Tonereignisse ... {len(registriert)} registriert, {len(eintraege)} Eintraege im Erzeuger")

stumm    = [n for n in registriert if n not in eintraege]
verwaist = [k for k in eintraege if k not in registriert]

fehlt_datei = []
for schluessel, dateien in eintraege.items():
    if not dateien:
        fehlt_datei.append(f"{schluessel} -> Eintrag ohne jede Tondatei")
    for d in dateien:
        if not (ORDNER / (d + '.ogg')).exists():
            fehlt_datei.append(f"{schluessel} -> {d}.ogg")

print(f"  stumme Ereignisse          : {len(stumm)}")
print(f"  Eintrag ohne Tondatei      : {len(fehlt_datei)}")
print(f"  Eintrag ohne Registrierung : {len(verwaist)}")
print(f"  Feld nicht aufloesbar      : {len(unbekannt)}")

fehler = 0

if stumm:
    fehler = 1
    print()
    print("STUMME EREIGNISSE -- registriert, aber ohne Eintrag im Erzeuger:")
    for n in stumm: print("   ", n)

if fehlt_datei:
    fehler = 1
    print()
    print("EINTRAEGE OHNE TONDATEI:")
    for n in fehlt_datei: print("   ", n)

if verwaist:
    fehler = 1
    print()
    print("EINTRAEGE OHNE REGISTRIERTES EREIGNIS:")
    for n in verwaist: print("   ", n)

if unbekannt:
    fehler = 1
    print()
    print("FELDER, DIE NICHT AUF EIN EREIGNIS ZEIGEN -- hier ist das Tor blind:")
    for n in unbekannt: print("   ", n)

if fehler:
    sys.exit(1)

print("OK - jedes Tonereignis hat einen Eintrag und eine Datei.")
PYEOF
