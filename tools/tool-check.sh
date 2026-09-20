#!/usr/bin/env bash
#
# Prueft, dass jede Werkzeugsorte aus IToolable.ToolType beide Seiten hat:
# einen Gegenstand, der sie traegt, UND mindestens einen Block, der nach ihr fragt.
#
# Hintergrund: ToolType ist die einzige Verbindung zwischen einem Einstellwerkzeug und den
# Maschinen, die sich damit einstellen lassen. Beide Seiten stehen weit auseinander -- die
# Gegenstaende in NtmItems, die Abfragen in den onScrew-Methoden von zwei Dutzend Bloecken --
# und nichts im Bau verbindet sie. Faellt eine Seite weg, meldet weder javac noch das Spiel
# etwas: der Zweig wird einfach nie erreicht.
#
# GENAU DAS LAG IM PORT DREIFACH VOR, gefunden in Runde 195:
#   * HAND_DRILL: sechs Bloecke fragten danach (beide Heizer, beide Reaktorstapel-Bloecke,
#     die elektrische Presse, der Giessereiauslass) -- und KEIN Gegenstand trug die Sorte.
#     Sechs Zweige, die nichts erreichen konnte.
#   * DEFUSER: der Entschaerfer war angemeldet -- und KEIN Block fragte nach ihm. Ein
#     Werkzeug, das nichts tun konnte.
#   * BOLT: zwei Umwandlungen im ToolConversionBlock warten darauf, kein Gegenstand traegt es.
#
# Schon Runde 99 hatte dieselbe Fehlerklasse an derselben Stelle: die Schraubenzieher waren
# gewoehnliche Gegenstaende statt ToolingItem, und fuenfzehn Maschinen waren dadurch in
# Wahrheit nicht einstellbar. Auch das fiel nur beim Lesen auf, nicht im Bau.
#
# DIE REGEL. Fuer jede Sorte in ToolType gilt: entweder hat sie BEIDE Seiten, oder KEINE.
# Eine Sorte ganz ohne Traeger und ohne Abfrager ist kein Fehler -- sie ist nur eine
# Aufzaehlungsstelle, die noch niemand benutzt. Eine Sorte mit nur einer Seite ist einer.
#
# TRAEGER ist ein "new ToolingItem(ToolType.X", ein "super(ToolType.X" in einer Unterklasse
# davon, oder ein ausdrueckliches "ToolType.X.register(".
# ABFRAGER ist ein "ToolType.X" in einer Datei, die onScrew erklaert oder aufruft.
#
# DIE AUSNAHMEN stehen unten mit Begruendung und sind gemessen, nicht gegriffen.
#
set -u
rc=0

python3 - <<'PYEOF' || rc=1
import os, re, sys

ROOT = 'src/main/java'
IFACE = os.path.join(ROOT, 'api/hbm/block/IToolable.java')

quelle = open(IFACE, encoding='utf-8').read()
m = re.search(r'enum ToolType \{(.*?);', quelle, re.S)
sorten = [t.strip() for t in m.group(1).replace('\n', ' ').split(',') if t.strip()]

traeger  = {s: [] for s in sorten}
abfrager = {s: [] for s in sorten}

for dirpath, _, files in os.walk(ROOT):
    for fn in files:
        if not fn.endswith('.java'): continue
        path = os.path.join(dirpath, fn)
        if os.path.abspath(path) == os.path.abspath(IFACE): continue
        src = open(path, encoding='utf-8', errors='replace').read()
        # Kommentare weg, damit ein erklaerender Satz nicht als Abfrage zaehlt
        src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
        src = re.sub(r'//[^\n]*', '', src)

        fragt = 'onScrew' in src

        for s in sorten:
            # Eine Datei kann mehrere Traeger enthalten -- NtmItems hat zwei Schraubenzieher
            # und zwei Handbohrer in derselben Datei. Gezaehlt werden Fundstellen, nicht
            # Dateien; sonst zaehlt ein Tor ungenau und sagt es nicht.
            # Drei Wege, die Sorte zu tragen: eine Erzeugung, die die Sorte unmittelbar
            # mitgibt, eine Unterklasse, die die Sorte an super() weiterreicht (so die
            # Bolzenpistole), oder ein ausdrueckliches register().
            #
            # DIE ERZEUGUNG FRAGT NICHT NACH DEM KLASSENNAMEN, und zwar seit Runde 242.
            # Vorher stand hier 'new ToolingItem\(' -- damit war das Tor blind fuer jede
            # Unterklasse, die die Sorte durchreicht statt sie festzuschreiben. Genau das
            # tut DefuserItem, und das Tor meldete daraufhin den Entschaerfer als Sorte
            # ohne Traeger, obwohl eine Zeile darueber einer angemeldet wurde. Gemessen:
            # im ganzen Baum gibt es fuenf Stellen der Form 'new X(ToolType.Y', und alle
            # fuenf sind Gegenstandsanmeldungen -- das breitere Muster faengt nichts
            # Falsches ein.
            n = len(re.findall(r'new \w+\(\s*ToolType\.' + s + r'\b', src)) \
                + len(re.findall(r'super\(\s*ToolType\.' + s + r'\b', src)) \
                + len(re.findall(r'ToolType\.' + s + r'\.register\(', src))
            if n:
                traeger[s].extend([path] * n)
            elif fragt and re.search(r'ToolType\.' + s + r'\b', src):
                abfrager[s].append(path)

# --------------------------------------------------------------------------------------
# AUSNAHMEN. Jede mit dem Grund, warum die fehlende Seite heute nicht zu haben ist.
# --------------------------------------------------------------------------------------
AUSNAHMEN = {
    # Keine. Bis Runde 198 stand hier BOLT -- angeblich fehlten bolt_spike, der Klang
    # RIVET_GUN und die Schnittstelle IAnimatedItem. Zwei davon waren falsch: bolt_spike
    # verschiesst das Original gar nicht (die Zeile ist dort auskommentiert), und der Klang
    # heisst tool.boltgun und stand seit jeher im Port. Die Schnittstelle fehlte wirklich
    # und ist nachgereicht.
}

probleme = []
paare = 0

for s in sorten:
    hat_t = len(traeger[s]) > 0
    hat_a = len(abfrager[s]) > 0

    if hat_t and hat_a:
        paare += 1
        if s in AUSNAHMEN:
            probleme.append('%s steht in der Ausnahmeliste, hat aber beide Seiten -- die Zeile ist veraltet' % s)
        continue

    if not hat_t and not hat_a:
        continue                       # unbenutzte Aufzaehlungsstelle, kein Fehler

    if s in AUSNAHMEN: continue

    if hat_a:
        probleme.append('%s: %d Abfrager, aber kein Gegenstand traegt die Sorte -- diese Zweige erreicht nichts:\n     %s'
                        % (s, len(abfrager[s]), '\n     '.join(sorted(abfrager[s]))))
    else:
        probleme.append('%s: Gegenstand vorhanden (%s), aber kein Block fragt danach -- das Werkzeug kann nichts tun'
                        % (s, ', '.join(sorted(set(traeger[s])))))

print('Pruefe Werkzeugsorten ... %d Sorten, %d davon mit beiden Seiten' % (len(sorten), paare))
for s in sorten:
    print('  %-12s Traeger %d, Abfrager %d%s' % (s, len(traeger[s]), len(abfrager[s]),
          '   [Ausnahme]' if s in AUSNAHMEN else ''))

if probleme:
    print('  AUFFAELLIG: %d' % len(probleme))
    for p in probleme: print('   ' + p)
    sys.exit(1)

print('OK - jede benutzte Werkzeugsorte hat Traeger und Abfrager.')
PYEOF

exit $rc
