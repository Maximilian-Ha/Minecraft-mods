"""Teil von tools/syntax-check.sh, Durchgang 3.

Sucht im ResourceManager Zuweisungen, die einen HFRWavefrontObject direkt in ein Feld
vom Typ IModelCustom oder IObjRenderer schreiben. Der Lader ist keines von beidem --
erst asVBO() liefert ein IModelCustom, getRenderer() einen IObjRenderer. Warum das
nicht javac erledigt, steht im Kommentar an der Aufrufstelle.
"""
import re
import sys

PFAD = "src/main/java/com/hbm/main/ResourceManager.java"
NOETIG = {"IModelCustom": "asVBO()", "IObjRenderer": "getRenderer()"}

try:
    zeilen = open(PFAD, encoding="utf-8").read().splitlines()
except OSError:
    sys.exit(0)

typen = {}
for zeile in zeilen:
    m = re.match(r"\s*public\s+static\s+(IModelCustom|IObjRenderer)\s+"
                 r"([A-Za-z_$][A-Za-z0-9_$]*)\s*;", zeile)
    if m:
        typen[m.group(2)] = m.group(1)

for nr, zeile in enumerate(zeilen, 1):
    m = re.match(r"\s*([A-Za-z_$][A-Za-z0-9_$]*)\s*=\s*new\s+HFRWavefrontObject\s*\(", zeile)
    if not m:
        continue
    typ = typen.get(m.group(1))
    if typ is None:
        continue
    if NOETIG[typ] not in zeile:
        print("%s:%d: %s ist %s, die Zuweisung endet aber nicht auf .%s"
              % (PFAD, nr, m.group(1), typ, NOETIG[typ]))
