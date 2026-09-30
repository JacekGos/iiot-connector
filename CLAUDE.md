# iiot-connector

Kotlin service responsible for all industrial protocol connectivity (OPC-UA, MQTT). Publishes raw data to Kafka; 
consumes desired connector config from Kafka; has no direct dependency on core-platform except a one-time startup REST fetch.

## Build & test

- Build: `./gradlew build`
- Full check (tests + ktlint + Detekt): `./gradlew check`
- Run locally: needs the Docker Compose stack up first (Redpanda, Mosquitto) — see README
- Integration tests against OPC-UA use the Prosys OPC UA Simulation Server; MQTT integration tests use the local Mosquitto container

## Architecture essentials

- Each configured connector (OPC-UA or MQTT) runs in its own isolated `CoroutineScope` — a failure in one must never affect others
- `ConnectorManager` reconciles desired vs. active connections on every `config-changes` Kafka event (add/update/remove), guarded by a per-connector `Mutex`
- Reconnection uses exponential backoff (1s → 2s → 4s → 8s → max 60s for OPC-UA; similar pattern for MQTT)
- Kafka topics: produces `raw-events` and `connector-status`; consumes `config-changes`
- On startup: fetches current config via `GET /api/internal/connector-config` on core-platform, then switches to Kafka-driven updates

Full architecture rationale and ADRs (why Redpanda over Kafka, why Kotlin, etc.) live in the "Industrial IOT platform" Claude project, not here — check there before re-deriving a decision that's already been made.

## Code style

- ktlint + Detekt enforced via `./gradlew check`, CI fails on violations
- Kotlin Coroutines + Flow idioms throughout — avoid blocking calls in connector code paths

## Working with GitHub issues

Issues/milestones for the WHOLE project (this repo, `iiot-core-platform`, and `iiot-core-web`) are tracked centrally in **`JacekGos/iiot-core-platform`** — not split per repo. Always target that repo with `gh issue` commands, even when working here.

- Labels: type is `bug` or `enhancement`; repo is `repo:connector`, `repo:core-platform`, or `repo:core-web` (a cross-cutting issue can carry more than one repo label). That's the full label set — don't invent new ones.
- Milestones = epics, named `EPIC-<n>`.
- Issue titles are `<epic>.<n> — <short title>` (e.g. `4.6 — OPC-UA connector implementation`) — no `STORY-` prefix, so it doubles as a branch-name prefix: `<epic>-<n>-<slug>` (dot becomes dash), e.g. branch `4-6-opc-ua-connector`.
- Before implementing a story, pull its acceptance criteria with `gh issue view <n> --repo JacekGos/iiot-core-platform` — treat the AC as the definition of done, verify against it before opening a PR.
- Reference the issue in commits/PRs. Since the PR lives in this repo but the issue lives in `iiot-core-platform`, use the full cross-repo form: `Closes JacekGos/iiot-core-platform#<n>` in the PR description (not bare `#<n>`, which only resolves within the same repo).
- A bug noticed during work should become its own issue (`gh issue create --repo JacekGos/iiot-core-platform --label bug,repo:connector`), not silently expand the scope of the current one.
- planning.md is retired as a source for new work — new stories/epics are created directly on GitHub.
