#!/usr/bin/env bash
#
# Prueft, dass kein Anmeldename zweimal vergeben ist.
#
# WARUM DAS EXISTIERT
# -------------------
# Ein DeferredRegister nimmt jeden Namen genau einmal. Ein zweites register() mit demselben
# Namen wirft beim Hochfahren des Mods eine IllegalArgumentException -- und zwar aus dem
# statischen Anfangsblock der Registerklasse heraus, also noch bevor irgendetwas laeuft. Der
# Mod startet dann gar nicht.
#
# Fuer javac ist das unsichtbar: zwei Felder mit verschiedenen Bezeichnern, die zufaellig
# dieselbe Zeichenkette weiterreichen, sind voellig gueltiges Java.
#
# GENAU DAS LAG IN RUNDE 203 VOR: NtmSoundEvents hatte "entity.ufo_blast" zweimal --
# einmal als GUN_TESLA_BLAST (seit der Teslakanone), einmal als frisch angelegtes UFO_BLAST
# fuer die ueberladene Tau-Kanone. Alle 38 damaligen Tore waren gruen, syntax-check war gruen,
# die Uebersetzung lief durch; erst runData brach ab, vier Minuten spaeter in CI:
#   Caused by: java.lang.IllegalArgumentException: Duplicate registration entity.ufo_blast
# Das Ton-Tor sah es nicht, weil es Feldname -> Name in eine Abbildung schreibt: zwei Felder
# auf denselben Namen fallen darin lautlos zusammen.
#
# WIE GEMESSEN WIRD
# -----------------
# ANMELDER ist ein DeferredRegister-Feld (ITEMS, BLOCKS, SOUND_EVENTS, ...).
# Namen erreichen ihn auf zwei Wegen, und beide werden gelesen:
#   1. unmittelbar -- ANMELDER.register("name", ...)
#   2. ueber einen WEITERREICHER -- eine Methode, deren erster Parameter ein String ist und
#      die genau diesen Parameter an ANMELDER.register(...) weitergibt (so NtmBlocks.register,
#      NtmSoundEvents.reg, NtmItems.registerPickaxe). Gezaehlt werden dann die Zeichenketten
#      an DEREN Aufrufstellen.
# Ein Weiterreicher wird bei seiner erklaerenden Datei gefuehrt, nicht bei seinem blossen
# Namen: sonst zieht ein "register(" irgendwo im Baum Namen in ein Register, mit dem es nichts
# zu tun hat. Aufrufe zaehlen daher aus derselben Datei oder ausdruecklich als Klasse.method().
#
# NtmBlocks.register meldet in ZWEI Register an -- den Block und seinen BlockItem. Beide
# Seiten werden gefuehrt, damit ein Blockname, der mit einem Gegenstandsnamen zusammenfaellt,
# auch auffaellt.
#
# WAS DAS TOR NICHT SIEHT, sagt es am Ende selbst an: jede Anmeldung, deren Name nicht als
# Zeichenkette dasteht (NtmFluidBridge meldet in einer Schleife ueber Fluids.metaOrder an).
# Ein Tor, das dort schweigt, wo es nicht hinsieht, waere schlimmer als keines.
#
# GEMESSEN: 16 Anmelder, 12 Weiterreicher, 3346 Anmeldenamen, null Funde. Zwei Stellen sind
# unlesbar (die Schleife in NtmFluidBridge), und das Tor sagt sie an. Mit dem wieder
# eingesetzten zweiten "entity.ufo_blast" genau ein Fund, an den beiden richtigen Zeilen.
#
set -uo pipefail
cd "$(dirname "$0")/.."

python3 - "$@" <<'PYEOF'
import os, re, sys, collections

ROOT = 'src/main/java'

dat = {}
for dp, _, fs in os.walk(ROOT):
    for fn in fs:
        if not fn.endswith('.java'): continue
        p = os.path.join(dp, fn)
        s = open(p, encoding='utf-8', errors='replace').read()
        # Kommentare weg: ein auskommentiertes register() ist keine Anmeldung. Ihre
        # ZEILENUMBRUECHE bleiben stehen -- sonst verschiebt sich jede gemeldete Zeilennummer
        # um die Laenge aller Kommentare davor, und der Fund zeigt auf die falsche Stelle.
        s = re.sub(r'/\*.*?\*/', lambda m: '\n' * m.group(0).count('\n'), s, flags=re.S)
        s = re.sub(r'//[^\n]*', '', s)
        dat[p] = s

def zeile(s, pos): return s[:pos].count('\n') + 1

# ---------------------------------------------------------------- Anmelder
anmelder = set()
for s in dat.values():
    for m in re.finditer(r'DeferredRegister[\w.<>,\s?\[\]]*\s+([A-Z][A-Z0-9_]+)\s*=', s):
        anmelder.add(m.group(1))

# ---------------------------------------------------------------- Weiterreicher
# (Datei, Methodenname) -> Menge der Anmelder, in die sie ihren ersten Parameter weitergibt
# Jede Methodenerklaerung sieht von aussen wie ein Aufruf aus. Ihre Stellen werden gemerkt,
# damit "register(String name, ..." nicht als Anmeldung ohne lesbaren Namen zaehlt.
ERKLAERSTELLE = collections.defaultdict(set)
for p, s in dat.items():
    for m in re.finditer(r'(?:private|public|protected|static|final|abstract|default)\s[\w<>,\[\].?\s]*?\b(\w+)\s*\(', s):
        ERKLAERSTELLE[p].add(m.end() - 1)

METH = re.compile(r'(?:static|private|public|protected|final|<[^>]*>|\s)+[\w<>,\[\].?\s]+?\s(\w+)\s*\(\s*(?:final\s+)?String\s+(\w+)\s*[,)]')
weiter = collections.defaultdict(set)
durchgereicht = collections.defaultdict(set)   # Datei -> Parameternamen, die weitergereicht werden
for p, s in dat.items():
    for m in METH.finditer(s):
        name, par = m.group(1), m.group(2)
        k = s.find('{', m.end())
        if k < 0: continue
        tiefe, i = 0, k
        while i < len(s):
            if s[i] == '{': tiefe += 1
            elif s[i] == '}':
                tiefe -= 1
                if tiefe == 0: break
            i += 1
        rumpf = s[k:i]
        for a in anmelder:
            if re.search(r'\b' + a + r'\s*\.\s*register\w*\s*\(\s*' + par + r'\s*[,)]', rumpf):
                weiter[(p, name)].add(a)
                durchgereicht[p].add(par)

# Klassenname -> Datei, damit ein NtmBlocks.register("x") aus einer anderen Datei ankommt
klasse = {os.path.splitext(os.path.basename(p))[0]: p for p in dat}

# ---------------------------------------------------------------- Namen einsammeln
namen = collections.defaultdict(list)     # (Anmelder, Name) -> [Fundstelle]
blind = collections.Counter()             # Anmeldungen ohne lesbaren Namen

for p, s in dat.items():
    # 1. unmittelbar
    for m in re.finditer(r'\b([A-Z][A-Z0-9_]{2,})\s*\.\s*register\w*\s*\(\s*(?:"([^"]*)"|([a-zA-Z_]\w*))\s*([,)])', s):
        a = m.group(1)
        if a not in anmelder: continue
        if m.group(2) is not None:
            namen[(a, m.group(2))].append('%s:%d' % (p, zeile(s, m.start())))
        elif m.group(4) == ')':
            # Einstellig: das ist die Anmeldung des Anmelders am Ereignisbus
            # (ITEMS.register(eventBus)), keine Anmeldung eines Namens.
            pass
        elif m.group(3) not in durchgereicht[p]:
            # Ein Bezeichner statt einer Zeichenkette, und er ist keiner, den ein Weiterreicher
            # dieser Datei durchreicht: hier sieht das Tor den Namen nicht.
            blind['%s:%d' % (p, zeile(s, m.start()))] += 1

    # 2. ueber Weiterreicher
    for (wp, wname), ziele in weiter.items():
        if wp == p:
            muster = r'(?<![\w.])' + wname + r'\s*\('
        elif klasse.get(os.path.splitext(os.path.basename(wp))[0]) == wp:
            muster = r'(?<![\w.])' + os.path.splitext(os.path.basename(wp))[0] + r'\s*\.\s*' + wname + r'\s*\('
        else:
            continue
        for m in re.finditer(muster, s):
            if m.end() - 1 in ERKLAERSTELLE[p]: continue
            rest = s[m.end():m.end() + 200].lstrip()
            if re.match(r'[a-zA-Z_]\w*\s*\)', rest): continue   # Bus-Anmeldung, kein Name
            lit = re.match(r'"([^"]*)"\s*[,)]', rest)
            if lit:
                for a in ziele:
                    namen[(a, lit.group(1))].append('%s:%d' % (p, zeile(s, m.start())))
            else:
                arg = re.match(r'([a-zA-Z_]\w*)\s*[,)]', rest)
                # Reicht ein Weiterreicher nur seinen eigenen Parameter an den naechsten
                # weiter (registerPickaxe -> registerPickaxe), steht der Name an DESSEN
                # Aufrufstelle und ist dort schon gezaehlt.
                if arg and arg.group(1) in durchgereicht[p]: continue
                blind['%s:%d' % (p, zeile(s, m.start()))] += 1

doppel = {k: v for k, v in namen.items() if len(v) > 1}

print('Pruefe Anmeldenamen ... %d Anmelder, %d Weiterreicher, %d Namen'
      % (len(anmelder), len(weiter), len(namen)))
if blind:
    print('  Ohne lesbaren Namen (nicht geprueft): %d Stellen' % len(blind))
    for f in sorted(blind)[:10]:
        print('     %s' % f)

if doppel:
    print('  AUFFAELLIG: %d Name(n) zweimal vergeben -- das wirft beim Hochfahren' % len(doppel))
    for (a, n), stellen in sorted(doppel.items()):
        print('   %s "%s":' % (a, n))
        for st in stellen: print('        %s' % st)
    sys.exit(1)

print('OK - jeder Anmeldename ist genau einmal vergeben.')
PYEOF
