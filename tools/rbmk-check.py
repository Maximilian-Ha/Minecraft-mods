"""Teil von tools/rbmk-check.sh -- prueft die Aufbauten auf den RBMK-Saeulen."""
import os
import re
import sys

LISTE = 'tools/rbmk-list.txt'
BLOECKE = 'src/main/java/com/hbm/blocks/NtmBlocks.java'
KLASSEN = 'src/main/java/com/hbm/blocks/machine/rbmk'
TEXTUREN = 'src/main/resources/assets/hbmsntm/textures/block'
ZUSTAENDE = 'src/main/java/com/hbm/datagen/NtmBlockStateProvider.java'


def ohne(t):
    return re.sub(r'//[^\n]*', '', re.sub(r'/\*.*?\*/', '', t, flags=re.S))


def main():
    soll = set()
    for zeile in open(LISTE, encoding='utf-8'):
        zeile = zeile.strip()
        if zeile and not zeile.startswith('#'):
            soll.add(zeile)

    bl = ohne(open(BLOECKE, encoding='utf-8').read())
    # Blockname -> Klasse, mit der er angelegt wird
    klasse_von = {}
    for m in re.finditer(r'DeferredBlock<[^>]*>\s+\w+\s*=\s*\w+\(\s*"([a-z0-9_]+)"\s*,\s*\(\)\s*->\s*new\s+(\w+)', bl):
        klasse_von[m.group(1)] = m.group(2)

    # Welche Klassen sagen hasPipes() == true? Vererbung ueber eine Ebene mitgenommen.
    sagt_rohre = set()
    erbt_von = {}
    for f in os.listdir(KLASSEN):
        if not f.endswith('.java'):
            continue
        quelle = ohne(open(os.path.join(KLASSEN, f), encoding='utf-8').read())
        name = f[:-5]
        m = re.search(r'class\s+%s\s+extends\s+(\w+)' % re.escape(name), quelle)
        if m:
            erbt_von[name] = m.group(1)
        if re.search(r'boolean\s+hasPipes\(\)\s*\{\s*return\s+true', quelle):
            sagt_rohre.add(name)

    def hat_rohre(k, tiefe=0):
        if tiefe > 8:
            return False
        if k in sagt_rohre:
            return True
        eltern = erbt_von.get(k)
        return hat_rohre(eltern, tiefe + 1) if eltern else False

    # Der Port benennt manche Saeulen anders als das Original.
    def norm(n):
        return n.replace('_', '')
    port_nach_norm = {norm(n): n for n in klasse_von}

    fehlt_rohre = []
    fehlt_textur = []
    ueberzaehlig = []
    for original in sorted(soll):
        n = port_nach_norm.get(norm(original))
        if n is None:
            continue
        if not hat_rohre(klasse_von[n]):
            fehlt_rohre.append(n)

    # Jede Saeule, die im Port Rohre meldet, muss auch im Original eine haben.
    for n, k in sorted(klasse_von.items()):
        if not n.startswith('rbmk_'):
            continue
        if hat_rohre(k) and norm(n) not in {norm(x) for x in soll}:
            ueberzaehlig.append(n)

    # Und jede gemeldete Rohrsaeule braucht ihre beiden Bilder.
    zust = ohne(open(ZUSTAENDE, encoding='utf-8').read())
    for m in re.finditer(r'rbmkColumn\(NtmBlocks\.(\w+)\.get\(\)\s*,\s*"([a-z0-9_]+)"', zust):
        feld, textur = m.group(1), m.group(2)
        reg = None
        for name, kl in klasse_von.items():
            if re.search(r'\b%s\s*=\s*\w+\(\s*"%s"' % (re.escape(feld), re.escape(name)), bl):
                reg = name
                break
        if reg is None or not hat_rohre(klasse_von.get(reg, '')):
            continue
        for seite in ('pipe_side', 'pipe_top'):
            if not os.path.exists(os.path.join(TEXTUREN, '%s_%s.png' % (textur, seite))):
                fehlt_textur.append('%s_%s.png' % (textur, seite))

    # Brennstoffkanaele zeichnen Kappe und Innenrohr aus rbmk_element; ihr Blockmodell hat
    # dafuer keine Deckflaeche. Ohne das _inner-Bild saehe man in den Block hinein.
    fehlt_innen = []
    for f in sorted(os.listdir(KLASSEN)):
        if not f.endswith('.java'):
            continue
        quelle = ohne(open(os.path.join(KLASSEN, f), encoding='utf-8').read())
        for m in re.finditer(r'return\s+this\.moderated\s*\?\s*"([a-z0-9_]+)"\s*:\s*"([a-z0-9_]+)"', quelle):
            for basis in m.groups():
                for teil in ('top', 'inner'):
                    bild = '%s_%s.png' % (basis, teil)
                    if not os.path.exists(os.path.join(TEXTUREN, bild)):
                        fehlt_innen.append(bild)

    # Die ReaSim-Steuerstaebe zeigen unten ihren Stromanschluss statt der Deckflaeche.
    fehlt_boden = []
    zust_quelle = ohne(open(ZUSTAENDE, encoding='utf-8').read())
    eigener_boden = set()
    for f in sorted(os.listdir(KLASSEN)):
        if not f.endswith('.java'):
            continue
        quelle = ohne(open(os.path.join(KLASSEN, f), encoding='utf-8').read())
        if re.search(r'boolean\s+hasOwnBottom\(\)\s*\{\s*return\s+this\.powered', quelle):
            eigener_boden.add(f[:-5])
    if eigener_boden:
        for m in re.finditer(r'rbmkColumn\(NtmBlocks\.(\w+)\.get\(\)\s*,\s*"([a-z0-9_]+)"', zust_quelle):
            if 'reasim' not in m.group(2) or 'control' not in m.group(2):
                continue
            bild = '%s_bottom.png' % m.group(2)
            if not os.path.exists(os.path.join(TEXTUREN, bild)):
                fehlt_boden.append(bild)

    print('Pruefe RBMK-Saeulen ... %d Rohrsaeulen im Original' % len(soll))
    print('  ohne hasPipes() im Port : %d' % len(fehlt_rohre))
    print('  mit hasPipes() zu viel  : %d' % len(ueberzaehlig))
    print('  ohne Rohrbild           : %d' % len(fehlt_textur))
    print('  Kanal ohne Kappenbild   : %d' % len(fehlt_innen))
    print('  ReaSim ohne Bodenbild   : %d' % len(fehlt_boden))

    if not fehlt_rohre and not ueberzaehlig and not fehlt_textur and not fehlt_innen and not fehlt_boden:
        print('OK - jede Rohrsaeule des Originals traegt auch hier ihre Stutzen.')
        return 0

    for titel, liste in (('OHNE ROHRSTUTZEN -- oben bleibt eine glatte Flaeche', fehlt_rohre),
                         ('ROHRSTUTZEN, DIE DAS ORIGINAL NICHT HAT', ueberzaehlig),
                         ('FEHLENDE ROHRBILDER', fehlt_textur),
                         ('FEHLENDE BILDER FUER KAPPE ODER INNENROHR', fehlt_innen),
                         ('FEHLENDE BODENBILDER DER REASIM-STEUERSTAEBE', fehlt_boden)):
        if not liste:
            continue
        print()
        print('%s:' % titel)
        for x in liste:
            print('   %s' % x)
    return 1


sys.exit(main())
