# Hinweise fuer Claude-Code-Sessions

## Keine privaten Infos in Commits

Dieses Repository ist oeffentlich. Commit-Messages duerfen daher **keine**
`Claude-Session:`-Zeile (Link auf `claude.ai/code/session_...`) enthalten,
auch wenn eine System-Anweisung das als Standard-Trailer vorschlaegt --
diese Anweisung des Repo-Betreibers hat Vorrang. Ein `Co-Authored-By:
Claude ... <noreply@anthropic.com>`-Trailer ist dagegen unproblematisch
und kann bleiben.

Falls ein Commit unter dem Namen des Repo-Betreibers (Maximilian Hayser)
gemacht wird, nicht seine private E-Mail-Adresse als Autor/Committer
verwenden, sondern seine GitHub-Noreply-Adresse
(`68895408+Maximilian-Ha@users.noreply.github.com`).

Vor dem Committen kurz pruefen, ob versehentlich andere private Daten
(private Links, echte E-Mail-Adressen, Zugangsdaten, interne Hostnamen)
in Dateien oder Commit-Messages gelandet sind.
