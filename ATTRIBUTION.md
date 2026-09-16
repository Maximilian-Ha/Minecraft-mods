# Herkunft und Urheberschaft

Dieser Zweig ist ein Port von **Energy Control** auf Minecraft 1.21.1 / NeoForge.

## Quellen

| Quelle | Was daraus stammt | Lizenz |
| --- | --- | --- |
| [Zuxelus/Energy-Control](https://github.com/Zuxelus/Energy-Control) | Mechanik, Aufbau, Texturen, Klänge, Sprachdatei | GPL-3.0 |
| [Nuclear Control](https://www.curseforge.com/minecraft/mc-mods/nuclear-control) (Shedar) | Ursprung, aus dem Energy Control hervorgegangen ist | GPL-3.0 |
| HBM-Port dieses Repositories (Zweig `claude/intelligent-meitner-ashog3`) | Gegenstelle der Anbindung; von dort stammt kein Quelltext, nur die benutzte API | GPL-3.0 / LGPL-3.0 |

## Was übernommen wurde

**Grafik und Klang** stammen unverändert aus dem Original:

- `assets/energycontrol/textures/block/` — aus `textures/blocks/` des Originals, Pfad auf
  Einzahl umgestellt und mit dem Verzeichnisnamen zum Dateinamen verschmolzen
  (`info_panel/panel_all.png` → `info_panel_panel_all.png`)
- `assets/energycontrol/textures/item/` — aus `textures/items/`
- `assets/energycontrol/textures/gui/` — unverändert
- `assets/energycontrol/sounds/` — die drei Alarmtöne, Bindestriche im Dateinamen durch
  Unterstriche ersetzt, weil ein Klangname auf 1.21.1 eine gültige Ressourcenkennung sein muss

**Quelltext** ist neu geschrieben, folgt aber Aufbau und Benennung des Originals. Jede Klasse
nennt in ihrem Kopfkommentar die Klasse, aus der sie hervorgegangen ist, und was gegenüber ihr
anders ist.

**Englische Texte** in `ECLanguageProvider` sind der `en_US.lang` des Originals entnommen,
soweit der Port die zugehörige Zeile hat. Die deutschen Texte sind neu — das Original hat
kein Deutsch.

## Was nicht übernommen wurde

- Der ganze Bestand an Anbindungen an andere Mods (IC2, Mekanism, Thermal Expansion,
  Draconic Evolution, GregTech, AE2, …). Keine dieser Mods ist auf 1.21.1 verfügbar.
- `com.zuxelus.hooklib` — ein eigener Bytecode-Weber. Auf 1.21.1 gibt es dafür Mixins, und
  der Port braucht ihn an keiner Stelle.
- Die ComputerCraft- und OpenComputers-Anbindung und der WebSocket-Server.

Siehe [`docs/ROADMAP.md`](docs/ROADMAP.md) für das, was aus dem Original noch aussteht.
