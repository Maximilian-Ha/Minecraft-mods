"""Prueft eine Anbindung an eine fremde Mod gegen deren Quellen.

Aufruf ueber tools/hbm-api-check.sh oder tools/mekanism-api-check.sh; dort steht auch,
warum es das gibt. Welche Anbindung geprueft wird, sagen vier Umgebungsvariablen:

    API_CHECK_NAME  Name fuer die Ausgabe, etwa "HBM"
    API_CHECK_DIR   unser Paket, etwa src/.../crossmod/hbm
    API_CHECK_SRC   Quellverzeichnisse der fremden Mod, durch : getrennt
    API_CHECK_PKGS  Paketvorsaetze, deren Importe geprueft werden, durch , getrennt
"""

import os
import re
import sys

NAME = os.environ["API_CHECK_NAME"]
OUR_DIR = os.environ["API_CHECK_DIR"]
SRC_ROOTS = [p for p in os.environ["API_CHECK_SRC"].split(":") if p]
PACKAGES = [p for p in os.environ["API_CHECK_PKGS"].split(",") if p]

COMMENT = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*.*?\*/', re.S)


def strip(text):
    """Zeichenketten und Kommentare raus, Zeilenzahl bleibt erhalten."""
    def repl(match):
        piece = match.group(0)
        return "\n" * piece.count("\n")
    return COMMENT.sub(repl, text)


def index_sources():
    """Einfacher Klassenname -> Dateipfad, fuer alle Quellen der fremden Mod."""
    index = {}
    for src in SRC_ROOTS:
        for root, _dirs, files in os.walk(src):
            for name in files:
                if name.endswith(".java"):
                    index.setdefault(name[:-5], os.path.join(root, name))
    return index


IMPORT = re.compile(r"^import\s+((?:%s)[\w.]*)\s*;"
                    % "|".join(re.escape(p) for p in PACKAGES), re.M)


def source_of(full):
    """Die Datei, in der dieser voll benannte Typ steht -- oder None."""
    for src in SRC_ROOTS:
        path = os.path.join(src, *full.split(".")) + ".java"
        if os.path.isfile(path):
            return path
    return None


CLASS_DECL = re.compile(
    r"\b(?:class|interface|enum|record)\s+(\w+)[^{]*?"
    r"(?:extends\s+([\w.<>,\s]+?))?"
    r"(?:implements\s+([\w.<>,\s]+?))?\s*\{", re.S)


def supertypes(path):
    """Die Namen aller direkten Ober-Typen in einer Datei."""
    text = strip(open(path, encoding="utf-8").read())
    names = []
    for match in CLASS_DECL.finditer(text):
        for group in (match.group(2), match.group(3)):
            if not group:
                continue
            for part in group.split(","):
                part = re.sub(r"<.*", "", part).strip()
                part = part.split(".")[-1]
                if part:
                    names.append(part)
    return names


MEMBER = re.compile(r"\b(\w+)\s*(\(|;|,|\)|\.|\s)")


def members_of(path):
    """Alle Namen, die in dieser Datei erklaert werden -- Felder, Methoden, Konstanten."""
    text = strip(open(path, encoding="utf-8").read())
    found = set()
    # Methoden und Felder mit Sichtbarkeit oder als Aufzaehlungswert.
    for match in re.finditer(r"\b(\w+)\s*\(", text):
        found.add(match.group(1))
    for match in re.finditer(r"[\w>\]]\s+(\w+)\s*(?:=|;|,)", text):
        found.add(match.group(1))
    # Aufzaehlungswerte stehen ohne Typ davor.
    for match in re.finditer(r"^\s*([A-Z][A-Z0-9_]*)\s*(?:,|;|\()", text, re.M):
        found.add(match.group(1))
    return found


def all_members(type_name, index, seen=None):
    """Namen des Typs und aller erreichbaren Ober-Typen."""
    if seen is None:
        seen = set()
    if type_name in seen or type_name not in index:
        return set()
    seen.add(type_name)
    path = index[type_name]
    names = members_of(path)
    for parent in supertypes(path):
        names |= all_members(parent, index, seen)
    return names


def main():
    index = index_sources()
    problems = []
    checked_types = 0
    checked_members = 0

    for name in sorted(os.listdir(OUR_DIR)):
        if not name.endswith(".java"):
            continue
        path = os.path.join(OUR_DIR, name)
        raw = open(path, encoding="utf-8").read()
        text = strip(raw)

        # 1. Importe aufloesen.
        imports = {}
        for match in IMPORT.finditer(text):
            full = match.group(1)
            simple = full.split(".")[-1]
            expected = source_of(full)
            if expected is None:
                problems.append("%s: Import zeigt ins Leere: %s" % (path, full))
                continue
            imports[simple] = expected
            checked_types += 1

        if not imports:
            continue

        # Importzeilen raus: dort steht ein Punkt zwischen Paketteilen, kein Zugriff.
        body = re.sub(r"^import[^;]*;", "", text, flags=re.M)

        # 2. Variablen, deren Typ aus der fremden Mod stammt: "instanceof X y" und "X y =".
        #    Mehrfach benannte Variablen sammeln alle ihre Typen; taucht derselbe Name mit
        #    zwei Typen auf, laesst sich hier nicht entscheiden, welcher an welcher Stelle
        #    gilt -- der Name faellt dann aus der Pruefung. Lieber eine Luecke als eine
        #    Falschmeldung.
        seen_types = {}
        arrays = set()

        def note(name, type_name):
            seen_types.setdefault(name, set()).add(type_name)

        for match in re.finditer(r"\binstanceof\s+(\w+)\s+(\w+)\b", body):
            if match.group(1) in imports:
                note(match.group(2), match.group(1))
        for match in re.finditer(r"\b(\w+)(\[\])?\s+(\w+)\s*=", body):
            if match.group(1) in imports:
                note(match.group(3), match.group(1))
                if match.group(2):
                    arrays.add(match.group(3))
        for match in re.finditer(r"\bfor\s*\(\s*(\w+)\s+(\w+)\s*:", body):
            if match.group(1) in imports:
                note(match.group(2), match.group(1))

        variables = {name: next(iter(types)) for name, types in seen_types.items()
                     if len(types) == 1}

        # 3. Zugriffe pruefen -- auf Variablen wie auf die Typen selbst (statisch).
        targets = dict(variables)
        for simple in imports:
            targets[simple] = simple

        cache = {}
        for match in re.finditer(r"\b(\w+)\.(\w+)\b", body):
            owner, member = match.group(1), match.group(2)
            if owner not in targets:
                continue
            # X.class ist das Klassenobjekt, kein Zugriff auf ein Glied.
            if member == "class":
                continue
            # Ein Feld-Array kennt nur length; seine Elemente werden ueber den Index
            # angesprochen und tauchen hier nicht als owner auf.
            if owner in arrays:
                if member != "length":
                    problems.append("%s: %s.%s -- %s ist ein Feld, kennt nur length"
                                    % (path, owner, member, owner))
                continue
            type_name = targets[owner]
            if type_name not in cache:
                cache[type_name] = all_members(type_name, index)
            checked_members += 1
            if member not in cache[type_name]:
                line = body[:match.start()].count("\n") + 1
                problems.append("%s:%d: %s.%s -- %s kennt kein %s"
                                % (path, line, owner, member, type_name, member))

    print("Pruefe %s-Anbindung gegen %s" % (NAME, ", ".join(SRC_ROOTS)))
    print("  geprueft: %d Importe, %d Zugriffe" % (checked_types, checked_members))

    if problems:
        print("  AUFFAELLIG: %d" % len(problems))
        for problem in problems:
            print("  " + problem)
        return 1

    print("OK - jeder Import und jeder Zugriff findet sein Ziel in den Quellen von %s." % NAME)
    return 0


if __name__ == "__main__":
    sys.exit(main())
