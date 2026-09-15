# HBM's Nuclear Tech Mod — Port auf Minecraft 1.21.1 (NeoForge)

Ziel dieses Repositories ist ein auf **Minecraft 1.21.1 / NeoForge** lauffähiger Port von
[HBM's Nuclear Tech Mod](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) (Original: Minecraft 1.7.10),
der die Maschinen und Mechaniken des Originals übernimmt.

> **Status: in Arbeit.** Der Port ist noch nicht vollständig. Gegenüber der Ausgangsbasis sind
> ergänzt:
>
> - **Stromerzeugung:** Diesel-Generator, Dampfmaschine, Dampfturbine, Stirlingmotor,
>   Verbrennungsmotor mit vier Kolbensätzen, Gasturbine, Turbofan, RTG mit zehn Pelletsorten,
>   Solarkessel mit Heliostatspiegeln.
> - **Stromverteilung:** das komplette Fernleitungsnetz — Strommasten in drei Größen,
>   Umspannwerk, beide Anschlusskästen, Stromschalter, Kabeldiode, Kabeldetektor,
>   Kabelanzeige, ummantelter Draht und die Kabelspule. Dazu fünf Batteriestufen und beide
>   Kondensatoren.
> - **Verarbeitung:** Elektroofen, Verdichter (samt kompakter Bauform), Mischer, Steinmühle,
>   Eisen- und Stahlofen, Doppelofen samt Aufsatz, Kristallisator, Drescher und Sägewerk.
> - **Verschmutzung:** Aschegrube, Ziegel- und Industrieschornstein. Der Rauch der
>   Verbrennungsmaschinen landet damit nicht mehr ungefiltert in der Umwelt, sondern wird
>   im Schornstein gemindert und als Flugasche und Feinruß wiedergewonnen.
>
> Der aktuelle Abdeckungsgrad gegenüber dem 1.7.10-Original ist in
> [`docs/GAP-ANALYSIS.md`](docs/GAP-ANALYSIS.md) pro Subsystem dokumentiert, der Arbeitsstand
> in [`docs/ROADMAP.md`](docs/ROADMAP.md). Nicht für ernsthafte Survival-Welten benutzen.
>
> **Zum Bauen:** `./gradlew build` übersetzt nur. Blockmodelle, Sprachdateien, Rezepte und
> Loot-Tabellen entstehen erst durch `./gradlew runData` — das gehört einmal vor dem Packen
> ausgeführt, sonst fehlen sie in der JAR. Siehe [`docs/BUILDING.md`](docs/BUILDING.md).

## Herkunft

Dieses Repository ist **kein Neuanfang**, sondern baut direkt auf zwei Vorarbeiten auf.
Die vollständige Git-Historie beider Quellen ist in dieses Repo gemergt, damit Urheberschaft
erhalten bleibt und Upstream-Updates weiter übernommen werden können:

| Quelle | Rolle | Lizenz |
| --- | --- | --- |
| [HbmMods/Hbm-s-Nuclear-Tech-GIT](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) | Original (1.7.10), Referenz für alle Mechaniken | GPL-3.0 / LGPL-3.0 |
| [ohiomannnn/HBMsNTM-NEO-EDITION](https://github.com/ohiomannnn/HBMsNTM-NEO-EDITION) | Basis dieses Ports (1.21.1 NeoForge) | GPL-3.0 / LGPL-3.0 |

Details und die exakten Upstream-Commits: [`ATTRIBUTION.md`](ATTRIBUTION.md).

## Build

```bash
./gradlew build
```

Voraussetzungen: **JDK 21**, Gradle 8.14.x (der Wrapper lädt die passende Version selbst).
Der Build braucht Netzzugriff auf `maven.neoforged.net`, `repo1.maven.org`,
`libraries.minecraft.net`, `maven.parchmentmc.org` und `services.gradle.org`.

Details, inkl. Vorgehen bei eingeschränktem Netz: [`docs/BUILDING.md`](docs/BUILDING.md).

## Dokumentation

| Dokument | Inhalt |
| --- | --- |
| [`docs/GAP-ANALYSIS.md`](docs/GAP-ANALYSIS.md) | Was ist portiert, was fehlt — pro Subsystem, mit Aufwandsschätzung |
| [`docs/PORTING-GUIDE.md`](docs/PORTING-GUIDE.md) | 1.7.10 → 1.21.1: API-Mapping und Konventionen dieses Projekts |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Priorisierte Reihenfolge der weiteren Portierung |
| [`docs/BUILDING.md`](docs/BUILDING.md) | Build, Toolchain, Offline-Hinweise |

## Lizenz

GNU Lesser General Public License v3 — wie das Original. Siehe [`LICENSE`](LICENSE) und
[`LICENSE.LESSER`](LICENSE.LESSER).
