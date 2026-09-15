# Herkunft und Urheberschaft

Dieses Repository ist ein abgeleitetes Werk. Es enthält Code aus zwei Upstream-Projekten,
beide unter GPL-3.0 / LGPL-3.0.

## 1. HBM's Nuclear Tech Mod (Original, Minecraft 1.7.10)

- Repository: <https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT>
- Autor: **HbmMods / The Bobcat** und Mitwirkende
- Referenz-Commit: `7c60926916275e49c434537ad4c5492f3d55e09b` (2026-09-12)
- Umfang: 3.477 Java-Dateien, ca. 464.000 Zeilen Java, 5.978 Texturen, 514 OBJ-Modelle
- Rolle hier: **Referenzimplementierung**. Alle zu portierenden Mechaniken stammen
  inhaltlich aus diesem Code.
- Als Git-Remote eingebunden: `hbm-upstream`

## 2. HBM's NTM: Neo Edition (Port auf 1.21.1 NeoForge)

- Repository: <https://github.com/ohiomannnn/HBMsNTM-NEO-EDITION>
- Autoren: **ohiomannnn** (210 Commits), **RS8-2** (9 Commits)
- Übernommener Commit: `4320fb9153754a8b09cc208124b88a16a3e6c27a` (2026-08-15), Version 198A
- Umfang: 981 Java-Dateien, ca. 108.000 Zeilen Java
- Rolle hier: **Codebasis dieses Ports**. Die komplette Git-Historie (219 Commits) wurde
  per `git merge --allow-unrelated-histories` übernommen, sodass die Autorschaft jedes
  einzelnen Commits erhalten bleibt.
- Als Git-Remote eingebunden: `neo`

## Upstream-Updates übernehmen

```bash
git fetch neo && git merge neo/master          # Fortschritt der Neo Edition übernehmen
git fetch hbm-upstream                          # 1.7.10-Original als Referenz aktualisieren
```

## Lizenz

Da beide Quellen unter GPL-3.0 / LGPL-3.0 stehen, steht auch dieses Repository unter
LGPL-3.0. Siehe `LICENSE` und `LICENSE.LESSER`. Eine Weitergabe — auch modifiziert —
ist erlaubt, solange sie unter derselben Lizenz erfolgt und die Urheberschaft erhalten bleibt.
