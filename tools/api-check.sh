#!/usr/bin/env bash
#
# Prueft den Quelltext gegen eine Liste bekannter Fallen der Minecraft-/NeoForge-API auf 1.21.
#
# Hintergrund: die anderen drei Pruefungen koennen das nicht. syntax-check.sh uebersetzt ohne
# Minecraft-Klassenpfad und filtert "cannot find symbol" heraus, weil die Meldung dabei
# zehntausendfach als Folgefehler entsteht; import-check.sh kennt nur Projekttypen, keine
# Methodennamen fremder Klassen. Ein falsch erinnerter Methodenname auf einer Minecraft-Klasse
# faellt deshalb erst beim Uebersetzen in der CI auf, und das kostet jedes Mal einen Durchlauf.
#
# Diese Liste macht daraus einen einmaligen Preis: was die CI einmal beanstandet hat, steht
# danach hier und wird nie wieder gepusht. Sie waechst mit jedem Fehlschlag; vollstaendig kann
# sie nicht sein, denn der Maven-Server von NeoForge ist in dieser Umgebung durch eine
# Organisationsrichtlinie gesperrt (403), die echte API liegt also nicht vor.
#
# Jeder Eintrag: Suchmuster (Python-Regex), kurze Erklaerung, richtige Schreibweise.

set -uo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PYEOF'
import os, re, sys

ROOT = 'src/main/java'

# (Regex, was daran falsch ist, was stattdessen zu schreiben ist)
PITFALLS = [
    (r'\)\s*(\.\s*or\s*\()\s*(?:Block\.box|Shapes\.)',
     'VoxelShape hat kein or(); Kaesten werden nicht verkettet vereinigt',
     'Shapes.or(a, b, c, ...)'),

    # AABB(BlockPos) gibt es, AABB(BlockPos, BlockPos) nicht -- die zweistellige Form nimmt Vec3.
    (r'new\s+AABB\s*\(\s*(?:this\s*\.\s*)?(?:worldPosition|blockPos|corePos|pos)\s*(?:\.\s*\w+\s*\([^()]*\)\s*)*,\s*(?:this\s*\.\s*)?(?:worldPosition|blockPos|corePos|pos)\s*(?:\.\s*\w+\s*\([^()]*\)\s*)*\)',
     'AABB hat keinen Konstruktor aus zwei BlockPos; die zweistellige Form nimmt Vec3',
     'new AABB(x, y, z, x + 1, y + 1, z + 1) oder AABB.encapsulatingFullBlocks(a, b)'),

    (r'ItemInteractionResult\s*\.\s*(?!SUCCESS|CONSUME_PARTIAL|CONSUME|PASS_TO_DEFAULT_BLOCK_INTERACTION|SKIP_DEFAULT_BLOCK_INTERACTION|FAIL|sidedSuccess|consumesAction|indicateItemUse|values|valueOf)([A-Za-z_]\w*)',
     'ItemInteractionResult kennt diesen Wert nicht -- es gibt kein PASS',
     'SUCCESS, CONSUME, CONSUME_PARTIAL, PASS_TO_DEFAULT_BLOCK_INTERACTION, SKIP_DEFAULT_BLOCK_INTERACTION, FAIL'),

    # Runde 42 kostete genau das einen CI-Durchlauf: die variable Entitaetsgroesse haengt in
    # dieser Fassung an getDimensions(Pose). getDefaultDimensions gibt es hier noch nicht, und
    # mit @Override davor ist es ein Uebersetzungsfehler statt einer stillen Nichtwirkung.
    (r'\b(getDefaultDimensions)\s*\(\s*Pose\b',
     'getDefaultDimensions(Pose) gibt es in dieser Minecraft-Fassung nicht',
     'getDimensions(Pose) ueberschreiben, wie es Mist tut'),

    # Runde 59 kostete genau das einen CI-Durchlauf. EnumUtil.grabEnumSafely leitet seinen
    # Rueckgabetyp aus dem ZIEL ab -- steht kein Ziel da, bleibt nur die Schranke Enum<?>, und
    # jeder Feld- oder Methodenzugriff darauf ist ein Uebersetzungsfehler. Als Argument einer
    # Methode mit erklaertem Parametertyp geht es dagegen; nur der Zugriff auf dem Ergebnis
    # selbst ist die Falle.
    (r'grabEnumSafely\s*\((?:[^()]|\([^()]*\))*\)\s*(\.\s*\w)',
     'grabEnumSafely liefert ohne Zuweisungsziel nur Enum<?> -- ein Zugriff darauf uebersetzt nicht',
     'erst einer Variablen des Aufzaehlungstyps zuweisen, dann darauf zugreifen'),

    # Runde 116 kostete genau das einen CI-Durchlauf, gleich fuenfmal in einer Datei.
    # DeferredItem<Item> ist ZUGLEICH Holder<Item> und ItemLike, und ItemStack hat
    # Konstruktoren fuer beides. Ein DeferredItem direkt hineinzugeben ist deshalb nicht
    # etwa bequem, sondern zweideutig: "reference to ItemStack is ambiguous".
    #
    # DeferredBlock ist NICHT betroffen -- der ist Holder<Block>, und dafuer gibt es keinen
    # Konstruktor; dort bleibt ItemLike als einzige Moeglichkeit stehen. Deshalb trifft die
    # Regel nur NtmItems, nicht NtmBlocks.
    #
    # Die Regel erkennt die uebliche Schreibweise NtmItems.NAME. Eine Variable vom Typ
    # DeferredItem faellt ihr durch; das ist bewusst, denn sie soll keine Attrappe werden.
    (r'new\s+ItemStack\s*\(\s*NtmItems\s*\.\s*[A-Z_0-9]+\s*(?=[,)])',
     'DeferredItem ist zugleich Holder<Item> und ItemLike -- der ItemStack-Aufruf ist zweideutig',
     'new ItemStack(NtmItems.NAME.get(), ...)'),
]

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
    """
    out = []
    i = 0
    n = len(src)

    while i < n:
        c = src[i]

        if c == '"' or c == "'":
            quote = c
            start = i
            i += 1
            while i < n:
                if src[i] == '\\': i += 2; continue
                if src[i] == quote: i += 1; break
                if src[i] == '\n' and quote == "'": break   # unbalanciert, nicht ueber Zeilen laufen
                i += 1
            out.append(('""' if quote == '"' else "''") + '\n' * src.count('\n', start, min(i, n)))
            continue

        if c == '/' and i + 1 < n and src[i + 1] == '*':
            start = i
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i + 1] == '/'): i += 1
            i += 2
            # Die Zeilenumbrueche des Kommentars bleiben stehen, sonst stimmen alle
            # Zeilennummern dahinter nicht mehr. Runde 61: das Gate meldete GenericRecipes:158
            # fuer eine Stelle, die in Zeile 168 steht.
            out.append('\n' * src.count('\n', start, min(i, n)))
            continue

        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n': i += 1
            continue

        out.append(c)
        i += 1

    return ''.join(out)

# Zweite, BERECHNETE Pruefung: Component ist unveraenderlich, MutableComponent nicht.
# Wer auf einem Component withStyle oder append aufruft, bekommt "cannot find symbol".
# Welche Projektklassen ein unveraenderliches getName() haben, steht im Quelltext -- also
# lesen wir es dort ab, statt eine Liste zu pflegen. In Runde 20 kostete genau das einen
# CI-Durchlauf: FluidType.getName() liefert Component, NTMMaterial.getName() dagegen
# MutableComponent, und beide heissen gleich.
CLASS_RE = re.compile(r'\b(?:class|interface|enum|record)\s+(\w+)')
IMMUTABLE_NAME_RE = re.compile(r'public\s+Component\s+getName\s*\(\s*\)')
ACCESSOR_RE = re.compile(r'public\s+(\w+)\s+(\w+)\s*\(\s*\)')

def collect_immutable_accessors(root):
    """Liefert die Namen parameterloser Methoden, deren Rueckgabetyp ein unveraenderliches
    getName() hat -- also die Stellen, an denen .getName().withStyle(...) scheitern wuerde."""
    immutable = set()
    accessors = {}

    for dirpath, _, names in os.walk(root):
        for fn in names:
            if not fn.endswith('.java'):
                continue
            src = strip(open(os.path.join(dirpath, fn), encoding='utf-8', errors='replace').read())
            cls = CLASS_RE.search(src)
            if cls and IMMUTABLE_NAME_RE.search(src):
                immutable.add(cls.group(1))
            for m in ACCESSOR_RE.finditer(src):
                accessors.setdefault(m.group(1), set()).add(m.group(2))

    names = set()
    for cls in immutable:
        names |= accessors.get(cls, set())
    return sorted(names)

# Dritte, BERECHNETE Pruefung: getRenderBoundingBox() ohne Parameter stammt aus der
# NeoForge-Erweiterung der Block-Entitaet und gilt dem Compiler nicht als ueberschriebene
# Methode -- @Override ist dort ein Fehler. Erbt die Klasse die Methode dagegen von einer
# Projektklasse, ist @Override richtig (so bei MachineCompressorCompactBlockEntity). Welcher
# Fall vorliegt, laesst sich aus dem Quelltext ableiten, also wird es abgeleitet statt gelistet.
EXTENDS_RE = re.compile(r'\b(?:class)\s+(\w+)(?:\s*<[^{]*?>)?\s+extends\s+([\w.]+)')
RENDER_BOX_RE = re.compile(r'public\s+AABB\s+getRenderBoundingBox\s*\(\s*\)')
RENDER_BOX_OVERRIDE_RE = re.compile(r'@Override\s+public\s+AABB\s+getRenderBoundingBox\s*\(\s*\)')

def collect_render_box_hierarchy(root):
    """Liefert (Oberklasse je Klasse, Klassen mit eigenem getRenderBoundingBox(), Fundstellen
    mit @Override davor)."""
    parents = {}
    declares = set()
    marked = []

    for dirpath, _, names in os.walk(root):
        for fn in names:
            if not fn.endswith('.java'):
                continue
            path = os.path.join(dirpath, fn)
            src = strip(open(path, encoding='utf-8', errors='replace').read())

            cls = CLASS_RE.search(src)
            if not cls:
                continue
            name = cls.group(1)

            ext = EXTENDS_RE.search(src)
            if ext and ext.group(1) == name:
                parents[name] = ext.group(2).split('.')[-1]

            if RENDER_BOX_RE.search(src):
                declares.add(name)

            for m in RENDER_BOX_OVERRIDE_RE.finditer(src):
                marked.append((path, src[:m.start()].count('\n') + 1, name))

    return parents, declares, marked

parents, declares, marked = collect_render_box_hierarchy(ROOT)

render_box_findings = []
for path, line, cls in marked:
    parent = parents.get(cls)
    seen = {cls}
    inherited = False
    while parent and parent not in seen:
        if parent in declares:
            inherited = True
            break
        seen.add(parent)
        parent = parents.get(parent)
    if not inherited:
        render_box_findings.append((path, line,
            'getRenderBoundingBox() stammt aus der NeoForge-Erweiterung und gilt dem Compiler '
            'nicht als ueberschrieben',
            'die Methode ohne @Override schreiben'))

# Vierte, BERECHNETE Pruefung: Block.codec() gibt in Minecraft MapCodec<? extends Block> zurueck.
# Wer stattdessen den eigenen Typ ohne Platzhalter schreibt, legt ihn fuer alle Ableitungen fest --
# MapCodec ist invariant, eine Unterklasse kann ihn dann nicht mehr verengen. Solange die Klasse
# niemand beerbt, faellt das nicht auf; in Runde 27 kostete genau das einen CI-Durchlauf, als
# RBMKRodReaSimBlock und RBMKControlAutoBlock dazukamen. Welche Klassen beerbt werden, steht im
# Quelltext, also wird es dort abgelesen.
CODEC_RE = re.compile(r'MapCodec\s*<\s*(\w+)\s*>\s*codec\s*\(\s*\)')

def collect_narrow_codecs(root, parents):
    """Findet codec()-Methoden mit festem eigenen Typ in Klassen, die beerbt werden."""
    extended = set(parents.values())
    findings = []

    for dirpath, _, names in os.walk(root):
        for fn in sorted(names):
            if not fn.endswith('.java'):
                continue
            path = os.path.join(dirpath, fn)
            src = strip(open(path, encoding='utf-8', errors='replace').read())

            cls = CLASS_RE.search(src)
            if not cls:
                continue
            name = cls.group(1)

            if name not in extended:
                continue

            for m in CODEC_RE.finditer(src):
                if m.group(1) != name:
                    continue
                line = src.count('\n', 0, m.start()) + 1
                findings.append((path, line,
                    'codec() legt hier den eigenen Typ ohne Platzhalter fest, die Klasse wird aber beerbt -- '
                    'MapCodec ist invariant, die Ableitung kann ihn nicht verengen',
                    'MapCodec<? extends ' + name + '> schreiben'))

    return findings

COLLECTION_DECL_RE = re.compile(
    r'\b(?:List|ArrayList|LinkedList|Set|HashSet|LinkedHashSet|TreeSet|Map|HashMap|'
    r'LinkedHashMap|Collection|Deque|ArrayDeque|Queue|NonNullList)\s*<[^;=()]*?>\s+(\w+)\s*[;=,)]')

ARRAY_DECL_RE = re.compile(r'\b[\w.<>\[\], ?]*?\[\]\s+(\w+)\s*[;=,)]')

def collect_length_on_collections(root):
    """
    .length auf einer Sammlung statt .size().

    Der Port baut an vielen Stellen ItemStack[] auf NonNullList/List um; bleibt dabei ein
    .length stehen, uebersetzt es nicht. Offline ist das UNSICHTBAR: javac laesst den Rumpf
    einer Klasse, deren Oberklasse fehlt, ungeprueft -- und das sind fast alle Klassen dieses
    Ports. Runde 60 kostete genau das einen CI-Durchlauf.

    Gesucht wird rein textuell und deshalb vorsichtig: gemeldet wird ein Name nur dann, wenn
    er IM GANZEN PROJEKT ausschliesslich als Sammlung erklaert ist. Sobald irgendwo auch ein
    Feld gleichen Namens als Array steht -- vertices, pollution und blueprintPools gibt es in
    diesem Baum in beiden Formen --, laesst sich ohne Typaufloesung nicht entscheiden, welches
    gemeint ist, und der Name bleibt aussen vor.
    """
    collections = set()
    arrays = set()
    sources = {}

    for dirpath, _, names in os.walk(root):
        for fn in sorted(names):
            if not fn.endswith('.java'): continue
            path = os.path.join(dirpath, fn)
            src = strip(open(path, encoding='utf-8', errors='replace').read())
            sources[path] = src
            collections.update(COLLECTION_DECL_RE.findall(src))
            arrays.update(ARRAY_DECL_RE.findall(src))

    unambiguous = collections - arrays
    findings = []

    for path, src in sources.items():
        for name in unambiguous:
            for m in re.finditer(r'\b' + re.escape(name) + r'\s*\.\s*length\b', src):
                line = src.count('\n', 0, m.start()) + 1
                findings.append((path, line,
                    name + ' ist im Projekt ausschliesslich als Sammlung erklaert, .length gibt es darauf nicht',
                    '.size() benutzen'))

    return findings

length_findings = collect_length_on_collections(ROOT)

codec_findings = collect_narrow_codecs(ROOT, parents)

findings = []
checked = 0

for dirpath, _, names in os.walk(ROOT):
    for fn in sorted(names):
        if not fn.endswith('.java'):
            continue

        path = os.path.join(dirpath, fn)
        checked += 1
        src = strip(open(path, encoding='utf-8', errors='replace').read())

        for pattern, why, fix in PITFALLS:
            for match in re.finditer(pattern, src):
                # Zeigt auf die Gruppe, wenn das Muster eine hat -- sonst auf den Anfang
                pos = match.start(1) if match.lastindex else match.start()
                line = src.count('\n', 0, pos) + 1
                findings.append((path, line, why, fix))

accessors = collect_immutable_accessors(ROOT)

if accessors:
    pattern = re.compile(r'(?:%s)\s*\(\s*\)\s*\.\s*getName\s*\(\s*\)\s*\.\s*(withStyle|append)\s*\('
                         % '|'.join(re.escape(a) for a in accessors))

    for dirpath, _, names in os.walk(ROOT):
        for fn in sorted(names):
            if not fn.endswith('.java'):
                continue
            path = os.path.join(dirpath, fn)
            src = strip(open(path, encoding='utf-8', errors='replace').read())
            for match in pattern.finditer(src):
                line = src.count('\n', 0, match.start()) + 1
                findings.append((path, line,
                                 'getName() liefert hier Component, nicht MutableComponent',
                                 'erst .copy() aufrufen'))

findings.extend(render_box_findings)
findings.extend(codec_findings)
findings.extend(length_findings)

print('Pruefe bekannte API-Fallen ... %d Dateien, %d Muster + %d Component-Zugriffe + %d Zeichenfenster + %d Codecs'
      % (checked, len(PITFALLS), len(accessors), len(marked), len(parents)))

if findings:
    print('  AUFFAELLIG: %d' % len(findings))
    for path, line, why, fix in findings:
        print('  %s:%d: %s -- richtig ist: %s' % (path, line, why, fix))
    sys.exit(1)

print('OK - keine bekannte API-Falle im Quelltext.')
PYEOF
