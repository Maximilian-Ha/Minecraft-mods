#!/usr/bin/env bash
#
# Prueft, ob jeder benutzte PROJEKTTYP auch erreichbar ist -- also importiert, im selben
# Paket, von einem Wildcard-Import gedeckt oder voll qualifiziert geschrieben.
#
# Hintergrund: tools/syntax-check.sh kann das nicht. Es uebersetzt ohne Minecraft-Klassenpfad,
# und "cannot find symbol" entsteht dabei zu Zehntausenden als Folgefehler -- die Meldung steht
# deshalb in der Filterliste. Ein vergessener Import auf eine EIGENE Klasse erzeugt aber genau
# diese Meldung und geht darin unter. In Runde 8 kostete das einen CI-Fehlschlag: die Zeile
# recipeHandlers.add(new CrystallizerRecipes()) stand in SerializableRecipe, der Import fehlte.
#
# Geprueft werden nur com.zuxelus-Typen; Minecraft-, NeoForge- und HBM-Typen kann dieses
# Werkzeug ohne Klassenpfad nicht beurteilen. Die HBM-Typen prueft tools/hbm-api-check.sh.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

ROOT = 'src/main/java'

def strip(src):
    """
    Entfernt Zeichenketten, Zeichenliterale und Kommentare -- in EINEM Durchgang von links
    nach rechts.

    Runde 55: vorher liefen vier getrennte regulaere Ausdruecke nacheinander. Das geht schief,
    sobald eines der Zeichen im Geltungsbereich eines anderen steht: ein Apostroph in einem
    Kommentar oeffnet ein Zeichenliteral, das bis zum naechsten Apostroph alles verschluckt,
    ein /* in einem Zeilenkommentar oeffnet einen Blockkommentar. In GenericRecipes.java hat
    das die Deklaration von IOutput unsichtbar gemacht -- das Gate hat dort jahrelang mit
    einem loechrigen Text gearbeitet, ohne dass es auffiel.

    Ein Zustandsautomat kennt diese Fallen nicht: was in einer Zeichenkette steht, ist
    Zeichenkette, und was in einem Kommentar steht, ist Kommentar.

    NACHGEMESSEN (Runde 55): der Schaden war ueberschaubar, aber real. Von 1.374 Dateien waren
    fuenf betroffen -- in ihnen hat die alte Aufbereitung Typdeklarationen verschluckt, die
    Gates haben sie also nicht gekannt:

        api/hbm/energymk2/PowerNetMK2.java          PowerNetMK2
        com/hbm/util/Tuple.java                     Tuple
        com/hbm/interfaces/IBomb.java               BombReturnCode
        com/hbm/uninos/UniNodespace.java            UniNodeWorld
        com/hbm/inventory/recipes/loader/...        IOutput, ChanceOutput

    Ein vergessener Import auf einen dieser Typen waere durchgerutscht. Nach der Reparatur
    meldet das Gate keinen -- der Port ist an diesen Stellen sauber.
    """
    out = []
    i = 0
    n = len(src)

    while i < n:
        c = src[i]

        if c == '"' or c == "'":
            quote = c
            i += 1
            while i < n:
                if src[i] == '\\': i += 2; continue
                if src[i] == quote: i += 1; break
                if src[i] == '\n' and quote == "'": break   # unbalanciert, nicht ueber Zeilen laufen
                i += 1
            out.append('""' if quote == '"' else "''")
            continue

        if c == '/' and i + 1 < n and src[i + 1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i + 1] == '/'): i += 1
            i += 2
            out.append(' ')
            continue

        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n': i += 1
            continue

        out.append(c)
        i += 1

    return ''.join(out)

# 1) Nur TOP-LEVEL-Projekttypen einsammeln (Typ heisst wie die Datei). Genau die brauchen
#    einen Import; verschachtelte Typen erreicht man ueber ihre aeussere Klasse oder einen
#    eigenen Import, und ihre Namen (Builder, Type, Entry ...) kollidieren zu oft mit
#    Minecraft-Typen, um sie ohne Klassenpfad beurteilen zu koennen.
types = {}
files = []
for dirpath, _, names in os.walk(ROOT):
    for fn in names:
        if not fn.endswith('.java'): continue
        path = os.path.join(dirpath, fn)
        raw = open(path, encoding='utf-8', errors='replace').read()
        m = re.search(r'^\s*package\s+([\w.]+)\s*;', raw, re.M)
        pkg = m.group(1) if m else ''
        files.append((path, pkg, raw))
        if not (pkg.startswith('com.zuxelus')): continue
        types.setdefault(fn[:-5], set()).add(pkg)


# Runde 67: alle im Projekt erklaerten Typnamen, verschachtelte eingeschlossen. Die Tabelle
# "types" oben kennt nur Dateinamen; MissileMicro etwa steht als innere Klasse in
# MissileTier0.java und fehlt dort.
ALL_DECLARED = set()
for _path, _pkg, _raw in files:
    for _m in re.finditer(r'\b(?:class|interface|enum|record|@interface)\s+(\w+)', strip(_raw)):
        ALL_DECLARED.add(_m.group(1))

# Alles aus java.lang steht ohne Import da.
JAVA_LANG = {
    'Appendable','ArithmeticException','ArrayIndexOutOfBoundsException','ArrayStoreException',
    'AutoCloseable','Boolean','Byte','Character','CharSequence','Class','ClassCastException',
    'ClassLoader','ClassNotFoundException','CloneNotSupportedException','Cloneable','Comparable',
    'Deprecated','Double','Enum','Error','Exception','Float','FunctionalInterface',
    'IllegalAccessException','IllegalArgumentException','IllegalStateException',
    'IndexOutOfBoundsException','InheritableThreadLocal','Integer','InterruptedException',
    'Iterable','Long','Math','NegativeArraySizeException','NoSuchFieldException',
    'NoSuchMethodException','NullPointerException','Number','NumberFormatException','Object',
    'OutOfMemoryError','Override','Package','Process','ProcessBuilder','Readable','Record',
    'Runnable','Runtime','RuntimeException','SafeVarargs','SecurityException','Short',
    'StackTraceElement','StrictMath','String','StringBuffer','StringBuilder',
    'StringIndexOutOfBoundsException','SuppressWarnings','System','Thread','ThreadGroup',
    'ThreadLocal','Throwable','UnsupportedOperationException','Void','AssertionError',
    'StackOverflowError','LinkageError','NoClassDefFoundError','CloneNotSupportedException',
}

# Verschachtelte Typen FREMDER Oberklassen. Sie stehen ohne Qualifizierung da, weil die Klasse
# von ihnen erbt -- Item.Properties in jedem Item, SavedData.Factory in jedem Speicherobjekt.
# Ohne Minecraft-Klassenpfad laesst sich das nicht aufloesen, also stehen sie hier namentlich.
# Die Liste waechst nur, wenn ein neuer solcher Name wirklich auftaucht; geraten wird nichts.
INHERITED_NESTED = {
    'Properties',   # Item.Properties
    'Factory',      # SavedData.Factory
    'Type',         # CustomPacketPayload.Type -- in jedem Paket
    'TooltipContext',  # Item.TooltipContext -- in jeder appendHoverText-Signatur
}

problems = []

for path, pkg, raw in files:
    if not (pkg.startswith('com.zuxelus')): continue
    body_all = strip(raw)

    explicit = set()          # einfache Namen mit explizitem Import
    wildcards = set()         # Pakete mit Wildcard-Import
    for im in re.finditer(r'^\s*import\s+(?:static\s+)?([\w.]+?)(?:\.(\*))?\s*;', body_all, re.M):
        full, star = im.group(1), im.group(2)
        if star: wildcards.add(full)
        else: explicit.add(full.rsplit('.', 1)[-1])

    # Importblock aus dem Rumpf entfernen, damit Importe nicht als Nutzung zaehlen
    body = re.sub(r'^\s*import\s+[^;]+;', '', body_all, flags=re.M)
    # voll qualifizierte Nutzungen ausblenden (com.hbm.x.Y / api.hbm.x.Y)
    body = re.sub(r'\b(?:com|api)\.hbm[\w.]*', '', body)

    # eigene Typen dieser Datei
    own = {tm.group(1) for tm in re.finditer(r'\b(?:class|interface|enum|record)\s+(\w+)', body_all)}

    # Nur eindeutige Typstellen: Konstruktoraufruf, Vererbung, deklarierter Variablentyp.
    # Ein blosses Vorkommen des Namens genuegt nicht -- es koennte ein Minecraft-Typ oder ein
    # verschachtelter Typ hinter einem Punkt sein.
    used = set()
    used |= set(re.findall(r'\bnew\s+([A-Z]\w*)\s*[(<]', body))
    used |= set(re.findall(r'\bextends\s+([A-Z]\w*)', body))
    used |= set(re.findall(r'\bimplements\s+([A-Z]\w*)', body))
    used |= set(re.findall(r'(?<![.\w])([A-Z]\w*)\s+\w+\s*[=;)]', body))
    # Runde 45: die beiden Formen, die bis dahin durchrutschten und einen CI-Lauf kosteten --
    # der Typ als Generikum-Argument (BlockEntityType<Foo>) und als Methodenreferenz (Foo::new).
    used |= set(re.findall(r'[<,]\s*([A-Z]\w*)\s*[>,]', body))
    used |= set(re.findall(r'(?<![.\w])([A-Z]\w*)\s*::', body))
    # Runde 128: der STATISCHE ZUGRIFF, die Form, die bis dahin durchrutschte und einen
    # CI-Lauf kostete -- CassetteItem.TrackType.fromMeta(...) nennt CassetteItem nur als
    # Praefix. Der Name steht in keiner der Formen oben, ist aber sehr wohl ein Typ, der
    # importiert sein muss.
    used |= set(re.findall(r'(?<![.\w])([A-Z]\w*)\.[A-Za-z_]', body))

    for name in sorted(used):
        if name in own or name in explicit: continue
        if name not in types: continue                       # kein Projekttyp -> nicht beurteilbar
        pkgs = types[name]
        if pkg in pkgs: continue                             # selbes Paket
        if any(p in wildcards for p in pkgs): continue       # Wildcard deckt es ab
        problems.append('%s: %s ist nicht erreichbar (liegt in %s)'
                        % (path, name, ', '.join(sorted(pkgs))))

    # ---------------------------------------------------------------------------------------
    # Runde 67: die Gegenprobe. Bis hierher wurden NUR Projekttypen geprueft; jeder andere Name
    # galt als "nicht beurteilbar" und rutschte durch. Das kostete in Runde 66 einen CI-Lauf:
    # FusionTorusBlockEntity benutzte IFusionPowerReceiver, eine Klasse, die ich im selben Paket
    # erwartet, aber nie angelegt hatte. Kein Import zeigte ins Leere, also sah das Gate nichts.
    #
    # Dabei ist der Fall entscheidbar: gibt es einen Namen im GANZEN Projekt nirgends, steht er
    # nicht in java.lang und ist er auch nicht importiert, dann kann er sich nur noch hinter
    # einem projektfremden Wildcard verbergen. Hat die Datei keinen solchen, gibt es ihn nicht.
    #
    # Geprueft werden nur Stellen, an denen ein Name zweifelsfrei ein Typ ist: instanceof,
    # new, extends, implements. Alles andere -- Aufzaehlungswerte in switch-Zweigen, Konstanten,
    # Methodenreferenzen -- wuerde falsch anschlagen.
    # ---------------------------------------------------------------------------------------
    if not any(not (w.startswith('com.zuxelus')) for w in wildcards):

        certain = set()
        certain |= set(re.findall(r'\binstanceof\s+([A-Z]\w*)', body))
        certain |= set(re.findall(r'\bnew\s+([A-Z]\w*)\s*[(<]', body))
        certain |= set(re.findall(r'\bextends\s+([A-Z]\w*)', body))
        certain |= set(re.findall(r'\bimplements\s+([A-Z]\w*)', body))

        for name in sorted(certain):
            if name in own or name in explicit: continue
            if name in ALL_DECLARED or name in JAVA_LANG or name in INHERITED_NESTED: continue
            if re.fullmatch(r'[A-Z0-9_]+', name): continue   # Konstante, kein Typ
            problems.append('%s: %s ist nirgends erklaert -- weder in der Datei, noch importiert, '
                            'noch irgendwo im Projekt' % (path, name))

# ---------------------------------------------------------------------------------------
# Runde 55: die Gegenrichtung. Bis hierher wurde geprueft, ob jeder BENUTZTE Projekttyp
# erreichbar ist. Der umgekehrte Fall -- ein Import, der ins Leere zeigt -- rutschte durch
# und kostete einen CI-Lauf:
#
#     import com.hbm.inventory.recipes.BlastFurnaceRecipes.BlastFurnaceRecipe;
#
# BlastFurnaceRecipe ist eine eigene Datei, keine verschachtelte Klasse von
# BlastFurnaceRecipes. javac meldet dafuer "cannot find symbol" -- dieselbe Meldung, die das
# Syntax-Gate zu Zehntausenden herausfiltern muss, weil die Minecraft-API fehlt.
#
# Hier laesst sich das sauber pruefen: ein Import aus com.hbm/api.hbm MUSS auf eine Datei
# oder eine darin deklarierte Klasse zeigen. Minecraft-Importe bleiben wie ueberall aussen vor.
# ---------------------------------------------------------------------------------------

def resolves(full, toplevel, declared, allow_member):
    """
    Loest einen Importpfad auf: der laengste Anfang muss eine Projektdatei sein, jedes weitere
    Stueck ein darin deklarierter Typ. Bei einem statischen Import darf das letzte Stueck ein
    Feld oder eine Methode sein und wird deshalb nicht geprueft.
    """
    parts = full.split('.')

    for cut in range(len(parts), 0, -1):
        head = '.'.join(parts[:cut])
        if head not in toplevel: continue

        rest = parts[cut:]
        if not rest: return True

        if allow_member: rest = rest[:-1]

        return all(part in declared[head] for part in rest)

    return False


# Voll qualifizierte Namen aller Projektdateien, und je Datei die darin deklarierten Typen.
toplevel = {}
declared = {}
packages = set()
for path, pkg, raw in files:
    if not (pkg.startswith('com.zuxelus')): continue
    name = os.path.basename(path)[:-5]
    fqn = pkg + '.' + name
    toplevel[fqn] = path
    packages.add(pkg)
    declared[fqn] = {m.group(1) for m in re.finditer(r'\b(?:class|interface|enum|record|@interface)\s+(\w+)', strip(raw))}

for path, pkg, raw in files:
    if not (pkg.startswith('com.zuxelus')): continue

    for im in re.finditer(r'^\s*import\s+(static\s+)?([\w.]+?)(\.\*)?\s*;', strip(raw), re.M):
        static, full, star = im.group(1), im.group(2), im.group(3)

        # Nur eigene Typen: die Klassen des HBM-Ports liegen nicht in diesem Baum,
        # sie prueft tools/hbm-api-check.sh gegen dessen Quellen.
        if not full.startswith('com.zuxelus'): continue

        if star:
            # Ein Wildcard darf auf ein Paket zeigen oder auf einen Typ (dann meint er dessen
            # Mitglieder). Beides ist gueltig; nur ins Leere darf er nicht zeigen.
            if full in packages or resolves(full, toplevel, declared, allow_member=False): continue
            problems.append('%s: Wildcard-Import zeigt ins Leere: %s' % (path, full))
            continue

        # Ein statischer Import endet auf einem Feld oder einer Methode, ein gewoehnlicher
        # auf einem Typ.
        if resolves(full, toplevel, declared, allow_member=bool(static)): continue

        problems.append('%s: Import zeigt ins Leere: %s' % (path, full))

# ---------------------------------------------------------------------------------------
# Runde 99: FREMDTYPEN mit bekanntem Paket. Bis hierher galt jeder Nicht-Projekttyp als
# "nicht beurteilbar" -- ohne Minecraft-Klassenpfad weiss das Gate ja nicht, wo eine
# Minecraft-Klasse wohnt. Das kostete in Runde 98 einen CI-Lauf: NtmBlocks benutzte
# BlockSetType.IRON, ohne BlockSetType zu importieren. Die Datei hat einen Wildcard auf
# net.minecraft.world.level.block, BlockSetType liegt aber in ...block.state.properties --
# der Wildcard deckt ihn nicht.
#
# ENTSCHEIDBAR IST DAS SEHR WOHL, und zwar aus dem Projekt selbst heraus: wo eine fremde
# Klasse wohnt, steht in den Tausenden expliziten Importen, die der Port ohnehin hat. Wird
# BlockSetType irgendwo explizit importiert, ist sein Paket damit bekannt -- und eine
# zweite Datei, die den Namen ohne Import und ohne deckenden Wildcard benutzt, ist falsch.
#
# DREI FILTER halten die Fehlalarme heraus, und sie sind noetig:
#   * Namen, die das Projekt selbst erklaert, bleiben aussen vor (FluidType, Fluids -- der
#     Port hat eigene, Minecraft hat gleichnamige).
#   * Namen, die IRGENDWO im Projekt hinter einem Punkt stehen, sind verschachtelte Typen
#     (Item.Properties, CustomPacketPayload.Type). Die koennen geerbt im Geltungsbereich
#     stehen, ohne importiert zu sein.
#   * Namen, fuer die zwei verschiedene Pakete bekannt sind, sind mehrdeutig.
#
# NACHGEMESSEN (Runde 99): ueber den ganzen Baum meldet die Regel null Funde. Nimmt man den
# Import aus Runde 98 wieder heraus, meldet sie genau ihn -- und sonst nichts.
# ---------------------------------------------------------------------------------------

FOREIGN = {}        # einfacher Name -> Pakete, in denen er im Projekt explizit importiert wird
NESTED = set()      # jeder Name, der irgendwo hinter einem Typ steht

for _path, _pkg, _raw in files:
    _body = strip(_raw)
    for _im in re.finditer(r'^\s*import\s+([\w.]+)\s*;', _body, re.M):
        _full = _im.group(1)
        if _full.startswith('com.hbm') or _full.startswith('api.hbm'): continue
        _head, _, _simple = _full.rpartition('.')
        if not _simple or not _simple[0].isupper(): continue
        FOREIGN.setdefault(_simple, set()).add(_head)
    for _m in re.finditer(r'\b[A-Z]\w*\.([A-Z]\w*)\b', _body):
        NESTED.add(_m.group(1))

for path, pkg, raw in files:
    if not (pkg.startswith('com.zuxelus')): continue

    body_all = strip(raw)

    explicit = set()
    wildcards = set()
    for im in re.finditer(r'^\s*import\s+(?:static\s+)?([\w.]+?)(?:\.(\*))?\s*;', body_all, re.M):
        full, star = im.group(1), im.group(2)
        if star: wildcards.add(full)
        else: explicit.add(full.rsplit('.', 1)[-1])

    body = re.sub(r'^\s*import\s+[^;]+;', '', body_all, flags=re.M)
    # voll qualifizierte Nutzungen ausblenden -- die brauchen keinen Import
    body = re.sub(r'\b(?:com|api|net|java|javax|org|io|it|mezz|foundry)\.[\w.]*', '', body)

    own = {tm.group(1) for tm in re.finditer(r'\b(?:class|interface|enum|record)\s+(\w+)', body_all)}

    used = set(re.findall(r'(?<![.\w])([A-Z]\w*)\s*\.', body))    # statischer Empfaenger
    used |= set(re.findall(r'\bnew\s+([A-Z]\w*)\s*[(<]', body))
    used |= set(re.findall(r'\bextends\s+([A-Z]\w*)', body))
    used |= set(re.findall(r'\bimplements\s+([A-Z]\w*)', body))
    used |= set(re.findall(r'\binstanceof\s+([A-Z]\w*)', body))

    for name in sorted(used):
        if name in own or name in explicit: continue
        if name in ALL_DECLARED or name in NESTED: continue
        if name not in FOREIGN or len(FOREIGN[name]) != 1: continue
        if re.fullmatch(r'[A-Z0-9_]+', name): continue           # Konstante, kein Typ
        home = next(iter(FOREIGN[name]))
        if home in wildcards: continue
        problems.append('%s: %s ist nicht erreichbar -- weder importiert noch von einem '
                        'Wildcard gedeckt (liegt in %s)' % (path, name, home))

print('Pruefe Projekt-Importe ... %d Dateien, %d bekannte Typen, %d Importziele' % (len(files), len(types), len(toplevel)))
if problems:
    print('  AUFFAELLIG: %d' % len(problems))
    for p in problems: print('  ' + p)
    sys.exit(1)
print('OK - jeder benutzte Projekttyp ist erreichbar.')
PYEOF
