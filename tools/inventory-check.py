"""Teil von tools/inventory-check.sh -- vergleicht die renderInventory-Werte mit dem Original."""
import os
import re
import sys

ORDNER = 'src/main/java/com/hbm/render/blockentity'
LISTE = 'tools/inventory-list.txt'
BLOECKE = 'src/main/java/com/hbm/blocks/NtmBlocks.java'

# Bloecke, die im Port anders heissen als im Original. Nur Umbenennungen, keine Ausnahmen.
UMBENANNT = {
    'machinezirnox': 'reactorzirnox',
}

# SCHULDENLISTE: hier weicht der Port ab, die Zahl des Originals ist aber noch nicht
# uebernommen. Jede Zeile nennt den gemessenen Ist- und Sollwert. Wer eine berichtigt,
# streicht die Zeile. Das Tor schlaegt an, wenn eine Zeile ueberfluessig wird.
SCHULD = {}


def ohne(t):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', t, flags=re.S))


def block(t, ab):
    d = 0
    for i in range(ab, len(t)):
        if t[i] == '{':
            d += 1
        elif t[i] == '}':
            d -= 1
            if d == 0:
                return t[ab + 1:i]
    return ''


def werte(rumpf):
    tr = re.search(r'translate\(\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*\)', rumpf)
    sc = re.search(r'scale\(\s*(-?[\d.]+)F?', rumpf)
    t = '%g,%g,%g' % (float(tr.group(1)), float(tr.group(2)), float(tr.group(3))) if tr else '0,0,0'
    return t, ('%g' % float(sc.group(1))) if sc else '1'


def werte_je_block(rumpf):
    """Ein Darsteller kann mehrere Bloecke bedienen und dann je Block andere Zahlen setzen.

    Erkannt wird die Form
        if(stack.is(NtmBlocks.X.asItem()) || stack.is(NtmBlocks.Y.asItem())) { ... }
    samt else-Zweig. Ohne Verzweigung gilt ein Wert fuer alle.
    """
    if 'stack.is(' not in rumpf:
        return None
    je = {}
    rest = rumpf
    while True:
        m = re.search(r'\bif\s*\((.*?)\)\s*\{', rest, re.S)
        if not m:
            break
        felder = re.findall(r'NtmBlocks\.(\w+)\.asItem\(\)', m.group(1))
        zweig = block(rest, m.end() - 1)
        for f in felder:
            je[f] = werte(zweig)
        nach = rest[m.end() + len(zweig):]
        sonst = re.match(r'\s*\}\s*else\s*\{', nach)
        if sonst:
            je['*'] = werte(block(nach, sonst.end() - 1))
        rest = rest[m.end() + len(zweig):]
    return je or None


def main():
    soll = {}
    for zeile in open(LISTE, encoding='utf-8'):
        zeile = zeile.strip()
        if not zeile or zeile.startswith('#'):
            continue
        n, t, s = zeile.split()
        soll[n] = (t, s)

    bl = ohne(open(BLOECKE, encoding='utf-8').read())
    name = {m.group(1): m.group(2) for m in
            re.finditer(r'DeferredBlock<[^>]*>\s+(\w+)\s*=\s*\w+\(\s*"([a-z0-9_]+)"', bl)}

    verglichen = 0
    abweichend = []
    erledigt = []

    for f in sorted(os.listdir(ORDNER)):
        if not f.endswith('.java'):
            continue
        quelle = ohne(open(os.path.join(ORDNER, f), encoding='utf-8').read())
        ri = re.search(r'public void renderInventory\([^)]*\)\s*\{', quelle)
        if not ri:
            continue
        rumpf = block(quelle, ri.end() - 1)
        je_block = werte_je_block(rumpf)
        standard = werte(rumpf)
        for feld in sorted(set(re.findall(r'NtmBlocks\.(\w+)\.asItem\(\)', quelle))):
            reg = name.get(feld)
            if not reg:
                continue
            schluessel = reg.replace('_', '')
            schluessel = UMBENANNT.get(schluessel, schluessel)
            if schluessel not in soll:
                continue
            ist = standard
            if je_block is not None:
                ist = je_block.get(feld, je_block.get('*', standard))
            verglichen += 1
            if ist == soll[schluessel]:
                if feld in SCHULD:
                    erledigt.append(feld)
            elif feld not in SCHULD:
                abweichend.append((f[:-5], feld, ist, soll[schluessel]))

    print('Pruefe Inventarbilder ... %d Eintraege im Original, %d Paare vergleichbar'
          % (len(soll), verglichen))
    print('  abweichend und nicht erklaert : %d' % len(abweichend))
    print('  Schuldenliste erledigt        : %d' % len(erledigt))

    if not abweichend and not erledigt:
        print('OK - jedes Inventarbild traegt die Zahl des Originals.')
        return 0

    if abweichend:
        print()
        print('ABWEICHENDE INVENTARBILDER -- zu klein, zu gross oder verschoben:')
        for datei, feld, ist, s in abweichend:
            hinweis = ''
            if float(ist[1]) < float(s[1]) / 3:
                hinweis = '  <-- winziges Puenktchen'
            elif float(ist[1]) > float(s[1]) * 3:
                hinweis = '  <-- laeuft aus dem Rahmen'
            print('   %-26s %-24s ist translate %s scale %s | soll translate %s scale %s%s'
                  % (datei, feld, ist[0], ist[1], s[0], s[1], hinweis))
    if erledigt:
        print()
        print('SCHULDENLISTE ERLEDIGT -- diese Zeilen koennen raus:')
        for feld in erledigt:
            print('   %s' % feld)
    return 1


sys.exit(main())
