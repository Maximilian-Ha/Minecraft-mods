#!/usr/bin/env bash
# Syntax-Gate fuer den Port.
#
# WARUM DAS EXISTIERT
# -------------------
# Ein echter Gradle-Build braucht maven.neoforged.net, libraries.minecraft.net und
# repo1.maven.org. Wo diese Hosts nicht erreichbar sind, laesst sich der Mod nicht
# kompilieren. javac kann die Quellen aber trotzdem PARSEN und typisieren -- nur
# scheitert dann jede Referenz auf Minecraft-/NeoForge-Klassen.
#
# Dieses Skript trennt daher:
#   * erwartete Meldungen  -> Folge der fehlenden Minecraft-/NeoForge-API
#   * echte Syntaxfehler   -> Fehler in UNSEREM Code
#
# Es ersetzt keinen Build. Es faengt aber zuverlaessig die haeufigste Fehlerklasse
# beim Portieren ab: kaputte Edits (fehlende Klammern, halbe Methoden, unvollstaendige
# Merges, verrutschte Bloecke).
#
# BASELINE
# --------
# javac erzeugt bei fehlender API vereinzelt Folgefehler, die wie echte Fehler
# aussehen (z. B. "recursive constructor invocation", wenn der Typ des
# Konstruktorarguments unbekannt ist und die Ueberladungsaufloesung deshalb auf den
# eigenen Konstruktor faellt). Solche nachweislich unschaedlichen Meldungen stehen in
# tools/syntax-baseline.txt. Das Skript schlaegt nur bei Meldungen an, die dort NICHT
# stehen -- so bleibt es aussagekraeftig, statt dauerhaft rot zu sein.
#
# Neue Baseline schreiben (nur nach manueller Pruefung jeder Zeile!):
#   tools/syntax-check.sh --update-baseline
#
# Verwendung:
#   tools/syntax-check.sh                          # gesamtes src/main/java
#   tools/syntax-check.sh path/to/Foo.java [...]   # einzelne Dateien
#
# ACHTUNG bei der Einzeldatei-Pruefung: fehlt der Kontext der uebrigen Quellen, meldet
# javac zusaetzliche Folgefehler, die im Gesamtlauf nicht auftreten (etwa "recursive
# constructor invocation" bei delegierenden Konstruktoren). Massgeblich ist immer der
# Lauf ohne Argumente ueber den gesamten Baum.
#
# Exit-Code 0 = sauber, 1 = neue, ungeklaerte Fehler.

set -uo pipefail
cd "$(dirname "$0")/.."

BASELINE="tools/syntax-baseline.txt"
UPDATE=0
if [ "${1:-}" = "--update-baseline" ]; then UPDATE=1; shift; fi

OUT=$(mktemp -d)
# javac legt bei sehr langen Kommandozeilen eine Argumentdatei im Arbeitsverzeichnis
# an; die raeumen wir zusammen mit dem Temp-Verzeichnis wieder weg.
trap 'rm -rf "$OUT"; rm -f javac.*.args' EXIT

if [ "$#" -gt 0 ]; then
  FILES=("$@")
else
  mapfile -t FILES < <(find src/main/java -name '*.java' | sort)
fi

if [ "${#FILES[@]}" -eq 0 ]; then
  echo "Keine Java-Dateien gefunden."
  exit 0
fi

echo "Pruefe ${#FILES[@]} Java-Dateien ..."

# ---------------------------------------------------------------------------
# Durchgang 1: doppelt erklaerte Konstanten.
#
# WARUM DAS EIN EIGENER DURCHGANG IST
# Runde 120 lief mit einem zweimal erklaerten MACHINE_RADAR_LARGE in die CI: fuenf
# Torwaechter gruen, der Bau rot. Kein javac-Torwaechter kann das je finden. Der Grund
# steht in javacs Check.checkUnique: die Doppelt-Pruefung beginnt mit einem vorzeitigen
# Ruecksprung, sobald der TYP des Feldes fehlerhaft ist. Gemessen an einem Minimalbeispiel:
#
#   public static final String            BAR = ...;  (zweimal)  -> gemeldet
#   public static final some.missing.Type FOO = ...;  (zweimal)  -> stumm
#
# Jedes Registrierungsfeld dieses Ports hat einen Typ aus der fehlenden API
# (DeferredBlock, DeferredItem, ...). Fuer genau die Felder, bei denen ein Duplikat
# weh tut, schweigt javac also immer. Deshalb hier ein Textdurchgang.
#
# Erfasst werden Konstanten im Rumpf einer aeusseren Klasse (genau vier Leerzeichen
# Einzug). Verschachtelte Klassen bleiben aussen vor: dort darf derselbe Name in zwei
# Geltungsbereichen stehen, das waere ein Fehlalarm.
DUPES="$OUT/dupes.txt"
printf '%s\n' "${FILES[@]}" | python3 -c '
import re, sys, collections
DECL = re.compile(r"^(\s*)(?:public|protected|private)\s+static\s+final\s+"
                  r"[A-Za-z0-9_$.]+(?:\s*<[^;=]*>)?(?:\s*\[\s*\])*\s+"
                  r"([A-Za-z_$][A-Za-z0-9_$]*)\s*=")
for path in (l.strip() for l in sys.stdin if l.strip()):
    seen = collections.defaultdict(list)
    try:
        lines = open(path, encoding="utf-8").read().splitlines()
    except OSError:
        continue
    for n, line in enumerate(lines, 1):
        m = DECL.match(line)
        if m and len(m.group(1)) == 4:
            seen[m.group(2)].append(n)
    for name, at in sorted(seen.items()):
        if len(at) > 1:
            print("%s: %s ist %dx erklaert -- Zeilen %s"
                  % (path, name, len(at), ", ".join(map(str, at))))
' > "$DUPES" 2>/dev/null || true
DUPCOUNT=$(grep -c . "$DUPES" || true)
echo "  doppelt erklaerte Konstanten: $DUPCOUNT"

# ---------------------------------------------------------------------------
# Durchgang 2: javac
RAW="$OUT/raw.txt"
# -sourcepath: projekteigene Typen sollen sich gegenseitig aufloesen, damit nur noch
#              die echten Fremd-APIs (Minecraft/NeoForge) fehlen.
# -Xmaxerrs:   javac bricht sonst bei 100 Fehlern ab und verdeckt spaetere Syntaxfehler.
javac -nowarn -proc:none -implicit:none -Xmaxerrs 1000000 -Xmaxwarns 1 \
      -sourcepath src/main/java -d "$OUT/classes" "${FILES[@]}" > "$RAW" 2>&1

# Meldungen, die ausschliesslich daran liegen, dass die Minecraft-/NeoForge-API fehlt.
#
# NICHT IN DIESER LISTE, seit Runde 121: "variable X is already defined in class Y".
# Diese Meldung kann nie an einer fehlenden Fremd-API liegen -- ein doppelt erklaertes Feld ist
# immer ein Fehler im eigenen Quelltext. Sie stand hier trotzdem und wurde damit verschluckt.
# Das allein reicht aber NICHT, siehe die Doppelerklaerungs-Pruefung weiter unten.
RESOLUTION='package [A-Za-z0-9_.]+ does not exist|cannot find symbol|cannot access|bad class file|does not override or implement a method from a supertype|no suitable (method|constructor) found|incompatible types|unreported exception|is not abstract and does not override abstract method|cannot be applied to given types|static import only from classes and interfaces|cannot be accessed from outside package|no interface expected here|does not take parameters|cannot inherit from final|an enclosing instance that contains|non-static (variable|method|class|type variable) .* cannot be referenced|array required, but|bad operand types?|operator .* cannot be applied|inconvertible types|is abstract; cannot be instantiated|has (private|protected) access|is not public in|is ambiguous|incompatible thrown types|invalid method declaration; return type required|method does not override|unexpected type|not a statement after|cannot assign a value to final variable|missing return statement|variable .* might not have been initialized|unreachable statement'

# Zeilennummern werden fuer den Vergleich entfernt: sonst gilt jede Meldung nach einer
# Einfuegung weiter oben faelschlich als neu. Datei + Meldungstext genuegen zur Identifikation.
grep -E '^[^ ].*:[0-9]+: error:|^error:' "$RAW" | grep -Ev "$RESOLUTION" \
  | sed -E 's/^([^ ]+):[0-9]+: error:/\1: error:/' | sort -u > "$OUT/flagged.txt" || true

TOTAL=$(grep -cE ': error:|^error:' "$RAW" || true)
FLAGGED=$(grep -c . "$OUT/flagged.txt" || true)

echo "  javac-Meldungen insgesamt : $TOTAL  (erwartet: fehlende Minecraft-/NeoForge-API)"
echo "  auffaellige Meldungen     : $FLAGGED"

if [ "$UPDATE" = "1" ]; then
  {
    echo "# Baseline fuer tools/syntax-check.sh"
    echo "# Nachweislich unschaedliche javac-Folgefehler aus der fehlenden Minecraft-/NeoForge-API."
    echo "# JEDE Zeile hier wurde manuell geprueft. Nichts ungeprueft hinzufuegen."
    cat "$OUT/flagged.txt"
  } > "$BASELINE"
  echo "Baseline geschrieben nach $BASELINE ($FLAGGED Eintraege)."
  exit 0
fi

if [ -f "$BASELINE" ]; then
  grep -v '^#' "$BASELINE" | grep -v '^$' | sort -u > "$OUT/base.txt"
else
  : > "$OUT/base.txt"
fi

comm -23 "$OUT/flagged.txt" "$OUT/base.txt" > "$OUT/new.txt"
NEW=$(grep -c . "$OUT/new.txt" || true)
echo "  davon in Baseline geklaert: $((FLAGGED - NEW))"
echo "  NEUE, ungeklaerte Fehler  : $NEW"

if [ "$NEW" -gt 0 ]; then
  echo
  echo "NEUE FEHLER (nicht in der Baseline) -- bitte pruefen:"
  sed 's/^/  /' "$OUT/new.txt"
  echo
  echo "Fundstellen mit Zeilennummer:"
  while IFS= read -r n; do
    f="${n%%:*}"
    grep -E "^${f}:[0-9]+: error:" "$RAW" | grep -Ev "$RESOLUTION" | sed 's/^/  /'
  done < "$OUT/new.txt" | sort -u
  echo
  echo "Wenn eine Meldung nachweislich nur an der fehlenden API liegt, mit"
  echo "Begruendung in $BASELINE eintragen."
  exit 1
fi

if [ "$DUPCOUNT" -gt 0 ]; then
  echo
  echo "DOPPELT ERKLAERTE KONSTANTEN -- javac kann diese nicht melden, siehe Kommentar oben:"
  sed 's/^/  /' "$DUPES"
  exit 1
fi

echo "OK - keine neuen Syntaxfehler."
exit 0
