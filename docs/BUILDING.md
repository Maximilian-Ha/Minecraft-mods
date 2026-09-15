# Build

## Voraussetzungen

- **JDK 21** (`java.toolchain.languageVersion = 21`)
- Gradle wird vom Wrapper geladen (`./gradlew`), benoetigt wird 8.14.x
- Netzzugriff auf:
  | Host | wofuer |
  | --- | --- |
  | `services.gradle.org` | Gradle-Distribution fuer den Wrapper |
  | `plugins.gradle.org` | Gradle-Plugin `net.neoforged.moddev` |
  | `maven.neoforged.net` | NeoForge 21.1.228 |
  | `libraries.minecraft.net`, `piston-data.mojang.com` | Minecraft 1.21.1 und Bibliotheken |
  | `maven.parchmentmc.org` | Parchment-Mappings 2024.11.17 |
  | `repo1.maven.org` | uebrige Abhaengigkeiten |
  | `maven.blamejared.com` | JEI 19.25.0.325 |
  | `maven.caffeinemc.net` | Sodium-API |
  | `maven.ryanhcode.dev` | Sable / Sable-Companion |
  | `api.modrinth.com` | Iris |

## Bauen

```bash
./gradlew build          # erzeugt build/libs/hbmsntm-<version>.jar
./gradlew runClient      # Entwicklungs-Client
./gradlew runServer      # Entwicklungs-Server
./gradlew runData        # Datengeneratoren -> src/generated/resources
```

Der erste Build dauert lange: NeoForge dekompiliert und remapped Minecraft.
Rechne mit 4–8 GB RAM (`org.gradle.jvmargs=-Xmx4G` in `gradle.properties`).

### `runData` gehoert vor jedes Packen

`build` haengt **nicht** von `runData` ab, und `src/generated/resources` ist nicht
versioniert (auch nicht in der NEO-Ausgangsbasis). Das heisst: die JAR aus einem reinen
`./gradlew build` enthaelt **keine** Blockmodelle, Blockstates, Sprachdateien, Rezepte
und Loot-Tabellen — alle Bloecke erscheinen im Spiel als fehlendes Modell, ohne Namen,
ohne Rezept und ohne Drop. Genau so baut auch die CI, deren gruener Haken deshalb nur
bedeutet: **es uebersetzt**, nicht: es ist spielbar.

Vor dem Packen einer benutzbaren JAR also einmal:

```bash
./gradlew runData && ./gradlew build
```

Zwei Dateien liegen doppelt vor — handgeschrieben unter `src/main/resources` und zugleich
von einem Datengenerator erzeugt: `assets/hbmsntm/sounds.json` sowie Blockstate und Modelle
von `machine_shredder` und `ore_bedrock_oil`. Das stammt aus der Ausgangsbasis. Solange
`src/generated/resources` fehlt, faellt es nicht auf; sobald `runData` gelaufen ist, liefern
beide Quellverzeichnisse denselben Pfad. Beim Aufraeumen sind die handgeschriebenen Fassungen
die ueberholten: der Datengenerator deckt sie vollstaendig ab (106 Sound-Eintraege gegenueber
47).

## Wenn kein Netzzugriff auf die Maven-Repos besteht

In abgeschotteten Umgebungen (CI-Sandboxes, Firmen-Proxys mit Allowlist) sind die oben
genannten Hosts oft gesperrt. Dann ist **kein** Gradle-Build moeglich — Minecraft und
NeoForge lassen sich nicht beschaffen, und ohne sie gibt es keinen Compile-Classpath.

Fuer diesen Fall gibt es ein reduziertes Pruefwerkzeug:

```bash
tools/syntax-check.sh                       # gesamtes src/main/java
tools/syntax-check.sh src/main/java/...java # einzelne Dateien
```

Es laesst `javac` die Quellen parsen und typisieren und trennt dabei

- **erwartete Meldungen** — jede Referenz auf Minecraft/NeoForge schlaegt fehl, das sind
  hier rund 24.000 Meldungen — von
- **echten Syntaxfehlern** in unserem Code.

Nachweislich unschaedliche javac-Folgefehler stehen mit Begruendung in
`tools/syntax-baseline.txt`; das Skript meldet nur Abweichungen davon.

### Asset-Referenzen

```bash
tools/asset-check.sh
```

`gradlew build` uebersetzt nur Java. Ein Tippfehler in einem Texturpfad oder eine vergessene
Datei faellt erst im laufenden Spiel auf -- als fehlende Textur oder als Absturz beim Laden
eines OBJ-Modells. Dieses Skript prueft alle im Code referenzierten Assets
(`withDefaultNamespace("textures/...")`, `HFRWavefrontObject("models/obj/...")` sowie die
Blocktexturen aus den Datengeneratoren) gegen den tatsaechlichen Dateibestand.

Ausgenommen sind zur Laufzeit erzeugte Ressourcen (`resources.put`) und `modLoc`-Angaben in
`UncheckedModelFile`, weil diese Eltern-Modelle bezeichnen, die der Datengenerator selbst
erstellt -- sonst waeren es Fehlalarme.

**Beide Skripte ersetzen keinen Build.** Sie finden keine Typfehler, keine falschen
Methodenaufrufe und keine Laufzeitprobleme. Es faengt aber die haeufigste Fehlerklasse beim Portieren ab:
kaputte Edits — fehlende Klammern, halbe Methoden, verrutschte Bloecke, unvollstaendige
Merges. Vor jedem Commit ausfuehren; ein echter `./gradlew build` muss trotzdem folgen,
sobald die Repos erreichbar sind.

## CI

`.github/workflows/build.yml` baut bei jedem Push und Pull Request mit JDK 21 und legt
das Jar als Artefakt ab. Dort sind die Maven-Hosts erreichbar, der Build ist also die
verbindliche Pruefung.
