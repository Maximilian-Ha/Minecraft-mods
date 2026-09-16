# Energy Control — Port auf Minecraft 1.21.1 (NeoForge)

Ziel dieses Zweiges ist ein auf **Minecraft 1.21.1 / NeoForge** lauffähiger Port von
[Energy Control](https://github.com/Zuxelus/Energy-Control) (Original: Minecraft 1.12.2),
mit besonderem Augenmerk auf die Zusammenarbeit mit dem
[HBM-Port im Zweig `claude/intelligent-meitner-ashog3`](../../tree/claude/intelligent-meitner-ashog3)
dieses Repositories.

Energy Control zeigt Messwerte anderer Maschinen auf Informationstafeln an. Die Werte holen
**Sensorkarten**, die mit einem **Bausatz** auf einen Block eingemessen werden.

> **Status: erster Bauabschnitt.** Enthalten sind:
>
> - **Informationstafel** mit Kartenfach, drei Aufwertungsfächern und Textanzeige auf der Schauseite
> - **Wärmemelder** — sucht einen HBM-Reaktor in der Nachbarschaft und gibt ab einer einstellbaren
>   Temperatur ein Redstone-Signal
> - **Bereichsmelder** — gibt ein Redstone-Signal, wenn der Messwert einer Karte einen Bereich verlässt
> - **Heulalarm** und **Warnleuchte**
> - **Karten**: Strom (mit Durchsatz), Flüssigkeit, Inventar, Redstone, Vanilla, Zeit, Text und **HBM**
> - **Bausätze** zu allen Karten mit Ziel, dazu Reichweiten-, Farb- und Berührungsaufwertung
>
> Die Tafeln brauchen **Strom** und nehmen ihn über Forge Energy — also aus jeder Energie-Mod
> dieser Fassung; in der Konfiguration abschaltbar. Die offenen Schritte sind als Stufenplan in
> [`docs/ROADMAP.md`](docs/ROADMAP.md) beschrieben, was gegenüber dem Original bewusst anders
> ist, in [`docs/ENTSCHEIDUNGEN.md`](docs/ENTSCHEIDUNGEN.md).
>
> **Zum Bauen:** `./gradlew build` übersetzt nur. Blockmodelle, Sprachdateien, Rezepte und
> Loot-Tabellen entstehen erst durch `./gradlew runData` — das gehört einmal vor dem Packen
> ausgeführt, sonst fehlen sie in der JAR. Siehe [`docs/BUILDING.md`](docs/BUILDING.md).

## Zusammenspiel mit dem HBM-Port

Die HBM-Anbindung ist der Schwerpunkt dieses Ports und in
[`docs/HBM-KOMPATIBILITAET.md`](docs/HBM-KOMPATIBILITAET.md) im Einzelnen beschrieben. Kurz:

| Was | Woher |
| --- | --- |
| Strom aller HBM-Maschinen | `IEnergyHandlerMK2` (`getPower` / `getMaxPower`) |
| Tanks aller HBM-Maschinen | `IFluidUserMK2.getAllTanks()` |
| Mehrblockmaschinen | `CompatExternal.getCoreFromPos` — HBMs eigener Weg vom Platzhalter zum Kern |
| RBMK-Säulen | Säulentemperatur, Schmelzpunkt und alles, was die Säule an die Reaktorkonsole meldet |
| ZIRNOX, Forschungsreaktor, Watz | Temperatur, Druck, Fluss, Ein/Aus |
| Batterieblock | Lade- und Entladeleistung aus `delta` |
| Strahlung | `ChunkRadiationManager` |
| Funkwerte | jede Maschine, die `IRORValueProvider` anbietet |

Die Anbindung ist **weich**: fehlt der HBM-Port, lädt der Mod unverändert, nur die HBM-Karte
findet dann kein Ziel. Der Kern kennt keine einzige HBM-Klasse — `CrossModLoader` lädt die
Brücke über ihren Namen.

## Herkunft

Dieser Zweig ist ein Port, kein Neuanfang.

| Quelle | Rolle | Lizenz |
| --- | --- | --- |
| [Zuxelus/Energy-Control](https://github.com/Zuxelus/Energy-Control) | Original (1.12.2), Vorlage für alle Mechaniken, Texturen und Klänge | GPL-3.0 |
| [Nuclear Control](https://www.curseforge.com/minecraft/mc-mods/nuclear-control) (Shedar) | Ursprung von Energy Control | GPL-3.0 |
| HBM-Port dieses Repositories | Gegenstelle der Anbindung | GPL-3.0 / LGPL-3.0 |

Details: [`ATTRIBUTION.md`](ATTRIBUTION.md).

## Build

```bash
./gradlew build
```

Voraussetzungen: **JDK 21**, Gradle 8.14.x (der Wrapper lädt die passende Version selbst).

Die HBM-Anbindung wird nur übersetzt, wenn eine JAR des HBM-Ports vorliegt:

```bash
./gradlew build -Phbm_jar=/pfad/zu/hbmsntm-198A.jar
```

Ohne sie bleibt das Paket `crossmod/hbm` aus dem Quelltextsatz; alles andere baut unverändert.
Siehe [`docs/BUILDING.md`](docs/BUILDING.md).

## Prüfwerkzeuge

Ein voller Gradle-Bau braucht Netzzugriff auf `maven.neoforged.net` und Freunde. Wo der fehlt,
prüfen die Werkzeuge unter `tools/` den Quelltext trotzdem:

| Werkzeug | Was es prüft |
| --- | --- |
| `tools/syntax-check.sh` | Syntaxfehler im eigenen Code, trotz fehlender Minecraft-API |
| `tools/import-check.sh` | jeder benutzte Projekttyp ist erreichbar |
| `tools/asset-check.sh` | jede angesprochene Textur ist da, keine liegt ungenutzt herum |
| `tools/lang-check.sh` | jeder Übersetzungsschlüssel ist übersetzt |
| `tools/hbm-api-check.sh` | jeder Zugriff der Anbindung findet sein Ziel im HBM-Port |

Alle fünf laufen in der CI, dazu der volle Bau gegen den echten HBM-Port.

## Lizenz

GPL-3.0, wie das Original. Siehe [`LICENSE`](LICENSE).
