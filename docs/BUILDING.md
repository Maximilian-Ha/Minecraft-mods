# Build

## Voraussetzungen

- **JDK 21** (`java.toolchain.languageVersion = 21`)
- Gradle wird vom Wrapper geladen (`./gradlew`), benötigt wird 8.14.x
- Netzzugriff auf:

  | Host | wofür |
  | --- | --- |
  | `services.gradle.org` | Gradle-Distribution für den Wrapper |
  | `plugins.gradle.org` | Gradle-Plugin `net.neoforged.moddev` |
  | `maven.neoforged.net` | NeoForge 21.1.228 |
  | `libraries.minecraft.net`, `piston-data.mojang.com` | Minecraft 1.21.1 und Bibliotheken |
  | `maven.parchmentmc.org` | Parchment-Mappings 2024.11.17 |
  | `repo1.maven.org` | übrige Abhängigkeiten |

## Bauen

```bash
./gradlew build          # erzeugt build/libs/energycontrol-<version>.jar
./gradlew runClient      # Entwicklungs-Client
./gradlew runServer      # Entwicklungs-Server
./gradlew runData        # Datengeneratoren -> src/generated/resources
```

Der erste Build dauert lange: NeoForge dekompiliert und remapped Minecraft.
Rechne mit 4–8 GB RAM (`org.gradle.jvmargs=-Xmx4G` in `gradle.properties`).

### `runData` gehört vor jedes Packen

`build` hängt **nicht** von `runData` ab, und `src/generated/resources` ist nicht versioniert.
Das heißt: die JAR aus einem reinen `./gradlew build` enthält **keine** Blockmodelle,
Blockstates, Sprachdateien, Rezepte und Loot-Tabellen — alle Blöcke erscheinen im Spiel als
fehlendes Modell, ohne Namen, ohne Rezept und ohne Drop.

Vor dem Packen einer benutzbaren JAR also einmal:

```bash
./gradlew runData && ./gradlew build
```

## Die HBM-Anbindung mitbauen

Das Paket `com/zuxelus/energycontrol/crossmod/hbm` wird gegen die Klassen des HBM-Ports
übersetzt. Ohne sie bleibt es aus dem Quelltextsatz — der Rest baut und läuft unverändert,
nur die HBM-Karte findet dann nichts.

Die JAR wird in dieser Reihenfolge gesucht:

1. `-Phbm_jar=/pfad/zur/hbmsntm-198A.jar`
2. `libs/*.jar`
3. `hbm/build/libs/hbmsntm-*.jar`

Der dritte Weg ist der bequemste: den HBM-Zweig daneben auschecken und dort einmal bauen.

```bash
git worktree add hbm claude/intelligent-meitner-ashog3
(cd hbm && ./gradlew build)
./gradlew build
```

Beim Konfigurieren sagt Gradle, welcher Weg gegriffen hat:

```
Energy Control: HBM-Anbindung wird gegen /.../hbm/build/libs/hbmsntm-198A.jar uebersetzt.
```

oder

```
Energy Control: keine HBM-JAR gefunden, die Anbindung crossmod/hbm wird nicht uebersetzt.
```

## Bei eingeschränktem Netz

Wo `maven.neoforged.net` nicht erreichbar ist, lässt sich der Mod nicht übersetzen. Die
Werkzeuge unter `tools/` prüfen den Quelltext trotzdem — was sie können und was nicht, steht
in ihrem Kopfkommentar. Der Reihe nach:

```bash
./tools/syntax-check.sh
./tools/import-check.sh
./tools/asset-check.sh
./tools/lang-check.sh
HBM_SRC=/pfad/zum/hbm/src/main/java ./tools/hbm-api-check.sh
```

Keines davon ersetzt den Bau. Der läuft in der CI, gegen echtes NeoForge und gegen den
echten HBM-Port.
