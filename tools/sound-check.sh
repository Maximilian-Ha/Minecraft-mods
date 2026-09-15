#!/usr/bin/env bash
# Ton-Gate fuer den Port.
#
# WARUM DAS EXISTIERT
# -------------------
# Ein Tonereignis wird an ZWEI Stellen erklaert: als SoundEvent in NtmSoundEvents und als
# Eintrag in assets/hbmsntm/sounds.json, der auf eine .ogg-Datei zeigt. Fehlt die zweite
# Haelfte, ist das Ereignis anstandslos registriert, spielbar -- und STUMM. Kein Fehler, keine
# Meldung, kein Absturz; es passiert einfach nichts.
#
# Aufgefallen ist das in Runde 127 beim Bau der Sirene: alarm.hatch, alarm.soyuzed und
# alarm.chime standen seit Langem als Ereignis da, ihre .ogg-Dateien lagen im Ordner, und in der
# sounds.json stand kein Wort davon. Die Messung ergab damals 110 solcher Faelle von 157
# Ereignissen -- die halbe Tonkulisse des Ports, Waffen, Tueren und Tuerme eingeschlossen.
#
# Geprueft wird dreierlei:
#   1. Jedes registrierte Ereignis hat einen Eintrag in der sounds.json.
#   2. Jeder Eintrag zeigt auf eine Datei, die es gibt.
#   3. Jeder Eintrag gehoert zu einem registrierten Ereignis.
#
# BASELINE
# --------
# Die 107 stummen Ereignisse, die vor Runde 127 schon stumm waren, stehen in
# tools/sound-baseline.txt. Sie sind kein Freibrief, sondern eine Liste offener Arbeit: die
# Tondateien liegen alle im Baum, es fehlen nur die Eintraege. Wer einen nachtraegt, streicht
# ihn hier. NEUE stumme Ereignisse schlagen sofort an.
#
# Exit-Code 0 = sauber, 1 = neue stumme Ereignisse oder fehlende Dateien.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - "$@" <<'PYEOF'
import json, re, pathlib, sys

QUELLE   = 'src/main/java/com/hbm/registry/NtmSoundEvents.java'
LISTE    = 'src/main/resources/assets/hbmsntm/sounds.json'
ORDNER   = pathlib.Path('src/main/resources/assets/hbmsntm/sounds')
BASELINE = pathlib.Path('tools/sound-baseline.txt')

registriert = re.findall(r'reg\("([^"]+)"\)', open(QUELLE, encoding='utf-8').read())
liste = json.load(open(LISTE, encoding='utf-8'))

print(f"Pruefe Tonereignisse ... {len(registriert)} registriert, {len(liste)} Eintraege")

baseline = set()
if BASELINE.exists():
    for zeile in BASELINE.read_text(encoding='utf-8').splitlines():
        zeile = zeile.strip()
        if zeile and not zeile.startswith('#'):
            baseline.add(zeile)

stumm    = [n for n in registriert if n not in liste]
verwaist = [k for k in liste if k not in registriert]

fehlt_datei = []
for schluessel, wert in liste.items():
    for eintrag in wert.get('sounds', []):
        name = eintrag if isinstance(eintrag, str) else eintrag.get('name', '')
        if not name.startswith('hbmsntm:'):
            continue
        pfad = ORDNER / (name.split(':', 1)[1] + '.ogg')
        if not pfad.exists():
            fehlt_datei.append(f"{schluessel} -> {name}")

neu_stumm = sorted(set(stumm) - baseline)
behoben   = sorted(baseline - set(stumm))

print(f"  stumme Ereignisse insgesamt: {len(stumm)}")
print(f"  davon in Baseline bekannt  : {len(stumm) - len(neu_stumm)}")
print(f"  NEUE stumme Ereignisse     : {len(neu_stumm)}")
print(f"  Eintrag ohne Tondatei      : {len(fehlt_datei)}")
print(f"  Eintrag ohne Registrierung : {len(verwaist)}")

if behoben:
    print()
    print("Seit der Baseline behoben -- bitte dort streichen:")
    for n in behoben: print("   ", n)

fehler = 0

if neu_stumm:
    fehler = 1
    print()
    print("NEUE STUMME EREIGNISSE -- registriert, aber ohne Eintrag in der sounds.json:")
    for n in neu_stumm: print("   ", n)

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

if fehler:
    sys.exit(1)

print("OK - kein neues stummes Tonereignis.")
PYEOF
