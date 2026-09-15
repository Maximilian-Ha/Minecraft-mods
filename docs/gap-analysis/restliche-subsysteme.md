# Die zehn nachgezogenen Subsysteme

Stand: Original `hbm-upstream/master`, Port `claude/intelligent-meitner-ashog3` nach Runde 43.

Die ursprüngliche Gap-Analyse hatte fünf der fünfzehn Subsysteme untersucht und die übrigen
zehn ausdrücklich offengelassen. Dieses Dokument holt sie nach.

## Wie gemessen wurde — und was das wert ist

Anders als die ersten fünf Berichte beruht dieser **nicht** auf Einschätzungen von
Analyse-Agenten, sondern auf Zählungen über beide Quellbäume. Das hat einen Vorteil und einen
Nachteil, und beide sollte man kennen, bevor man die Zahlen benutzt:

**Vorteil:** die Zahlen sind reproduzierbar. Jede Zeile dieses Dokuments lässt sich mit einem
`git ls-tree` und einem `grep` nachrechnen.

**Nachteil:** gezählte Dateien sagen nichts über Funktion. Das zeigt sich hier an einem
konkreten Fall: bei den **Partikeln** hat der Port *mehr* Dateien und *mehr* Zeilen als das
Original (61 gegen 55 Dateien, 6.901 gegen 5.443 Zeilen) — und trotzdem sind nur 13 von 38
Partikelklassen portiert. Der Grund ist, dass 1.21 für jeden Partikel zusätzlich eine
Provider- und eine Options-Klasse verlangt, wo 1.7.10 mit einer einzigen `EntityFX`-Ableitung
auskam. Wer hier Dateien zählt, misst die API-Umstellung, nicht den Fortschritt.

Deshalb steht in der Tabelle unten jeweils die **zählbare Einheit** des Subsystems — also
registrierte Items, registrierte Blöcke, Rezepthandler, Renderer —, nicht die Dateizahl.

## Übersicht

| Subsystem | Einheit | Original | Port | Abdeckung |
| --- | --- | ---: | ---: | ---: |
| Items | registrierte Items | 1.690 | 936 | 55 % |
| Blöcke (generisch) | registrierte Blöcke | 918 | 388 | 42 % |
| Rezepte | Rezepthandler | 48 | 20 | 42 % |
| GUIs | Container/Menüs | 178 | 67 | 38 % |
| | Oberflächen | 218 | 82 | 38 % |
| Rendering | Block-Renderer | 249 | 89 | 36 % |
| | Entity-Renderer | 141 | 145¹ | — |
| Strahlung/Gefahren | Gefahrentypen | 9 | 9 | **100 %** |
| | Modifikatoren | 5 | 5 | **100 %** |
| | Registry-Einträge | 340 | 195 | 57 % |
| | Chunk-Strahlung | 7 Dateien | 3 | 43 % |
| Weltgenerierung | Dateien | 86 | 13 | **15 %** |
| Entities | registrierte Typen | 122 | 57 | 47 % |
| Partikel | Partikelklassen | 38 | 13 | 34 % |
| Infrastruktur | Handler | 137 | 46 | 34 % |
| | Netzwerkpakete | 29 | 16 | 55 % |
| | Befehle | 13 | 5 | 38 % |
| | Speicherdaten | 18 | 13 | 72 % |

¹ Die Zahlen sind nicht vergleichbar: der Port meldet je Entitätstyp an, das Original je
Klasse, und mehrere Typen teilen sich einen Renderer (allein die Raketen fünfmal denselben).
Der Entity-Renderer-Stand folgt dem Entity-Stand, also rund 47 %.

## Die beiden Ausreißer

### Strahlung und Gefahren — praktisch fertig

Das einzige Subsystem, dessen **Gerüst vollständig** portiert ist: alle neun Gefahrentypen,
alle fünf Modifikatoren, das Attachment-System an den Lebewesen. Was fehlt, sind Einträge in
der Liste — und die kommen ohnehin mit den Gegenständen, zu denen sie gehören. Von den 145
fehlenden Einträgen entfallen die meisten auf Gegenstände, die es im Port noch gar nicht gibt.

Das ist kein Zufall: die Runden 39 und 41 haben hier gezielt nachgezogen.

### Weltgenerierung — der wunde Punkt

**15 % ist der mit Abstand schlechteste Wert im ganzen Projekt.** Im Original stehen unter
`world/`:

| Paket | Dateien | was drin ist |
| --- | ---: | --- |
| `gen` | 28 | Strukturen |
| `generator` | 22 | Erzverteilung, Ölfelder, Sellafield |
| `dungeon` | 13 | Bunker, Silos, Labore |
| `feature` | 14 | Einzelelemente |
| `biome` | 3 | eigene Biome |

Im Port stehen dem 13 Dateien gegenüber: sechs Features (Ölflecken, Ölblasen, Bodenschätze,
Landminen, abgestürzte Bombe), die Meteoritenstruktur samt Einschlagsystem und die drei
Registrierungsklassen für die 1.21-Weltgenerierung.

**Es gibt im Port kein einziges Gebäude.** Keinen Bunker, kein Silo, kein Labor, keine
Forschungsstation — nichts von dem, was das Original in der Welt verteilt. Wer den Mod heute
spielt, findet Öl und Erze, aber nichts zu erkunden.

Dazu kommt, dass 1.21 die Weltgenerierung vollständig auf datengetriebene JSON-Strukturen und
Jigsaw-Pools umgestellt hat. Die 28 Strukturklassen des Originals sind handgeschriebener
Java-Code, der Block für Block setzt. Das ist **keine Portierung, sondern eine Neuentwicklung
in einem anderen Paradigma** — die Zeilenzahl des Originals ist hier ein besonders schlechter
Schätzer für den Aufwand.

## Was das für den Gesamtumfang heißt

Die fünf zuerst analysierten Subsysteme kamen auf rund 770 Personentage. Für die zehn hier
nachgezogenen lässt sich das **nicht seriös in Personentagen ausdrücken** — dafür müsste man
Mechanik für Mechanik vergleichen, so wie es die ersten fünf Berichte getan haben, und nicht
zählen.

Was sich sagen lässt: gemessen an den Einheiten liegt die Abdeckung dieser zehn Subsysteme
zwischen 15 % (Weltgenerierung) und 100 % (Gefahrengerüst), im Mittel bei rund **45 %** — also
deutlich höher als bei den fünf zuerst analysierten (5–45 %). Das ist plausibel: die zehn hier
sind überwiegend Grundlagen, die beim Portieren der Maschinen ohnehin mitgezogen wurden.

Die Ausnahme ist die Weltgenerierung, und sie ist die einzige der zehn, die eine **eigene
Stufe** verdient statt nebenbei mitzulaufen.
