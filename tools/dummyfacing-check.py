"""Teil von tools/dummyfacing-check.sh -- FACING nur vom Kern lesen."""
import os
import re
import sys

WURZEL = 'src/main/java/com/hbm/blocks'
BENUTZUNG = re.compile(r'(protected \w+ use\w*\(.*?\n\s*\}\n)', re.S)
AUS_ZUSTAND = re.compile(r'\bstate\.getValue\(\s*(?:DummyableBlock\.)?FACING\s*\)')


def ohne(t):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', t, flags=re.S))


def main():
    geprueft = 0
    treffer = []
    for wurzel, _, namen in os.walk(WURZEL):
        for n in sorted(namen):
            if not n.endswith('.java'):
                continue
            pfad = os.path.join(wurzel, n)
            quelle = ohne(open(pfad, encoding='utf-8').read())
            if 'extends DummyableBlock' not in quelle:
                continue
            geprueft += 1
            for m in BENUTZUNG.finditer(quelle):
                if AUS_ZUSTAND.search(m.group(1)):
                    treffer.append(pfad)
                    break

    print('Pruefe Ausrichtung beim Anklicken ... %d Vielblock-Bloecke' % geprueft)
    print('  lesen FACING aus dem angeklickten Zustand: %d' % len(treffer))

    if not treffer:
        print('OK - jeder holt die Aufstellrichtung beim Kern.')
        return 0

    print()
    print('FACING AUS DEM ANGEKLICKTEN BLOCK -- das ist beim Vielblock fast immer falsch:')
    for p in treffer:
        print('   %s' % p)
    print()
    print('Die Hilfsbloecke tragen in FACING die Richtung ZUM KERN (MultiblockHandlerXR.fillSpace),')
    print('nicht die Aufstellrichtung. Richtig ist die Aufstellrichtung vom Kern:')
    print('    Direction dir = be.getBlockState().getValue(FACING);')
    return 1


sys.exit(main())
