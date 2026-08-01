# AI-DLC Inception Baseline - forgecity-launcher

**Captured:** 2026-08-01 (as-is snapshot, not a target design)

## Purpose

ForgeCity Launcher is an on-device Android **HOME launcher** (package `buzz.delena.forgecity`) that turns the phone home screen into a game-like living city: installed apps appear as buildings on an isometric map, with progression, story chapters, and an optional notification-speech assistant. Primary target device documented in-repo is the Realme P2 Pro 5G. Distribution is via public GitHub prerelease debug APKs (latest tip `0.18.0-production-house-mode-dev`, versionCode **38**).

## Tech stack

| Layer | As stated in repo |
|-------|-------------------|
| Language / UI | Kotlin + Jetpack Compose (Material3); JVM target 17 |
| Build | Android Gradle Plugin **8.11.1**; Kotlin **2.3.21**; KSP **2.3.10**; single module `:app` |
| SDK | `compileSdk` / `targetSdk` **35**; `minSdk` **26** |
| Compose BOM | `androidx.compose:compose-bom:2025.07.00` |
| Persistence | Room **2.7.2** (DB version **3**, migrations 1→2→3); SharedPreferences for assistant/atmosphere |
| Background work | WorkManager **2.10.2** + UsageStats |
| Video | Media3 ExoPlayer **1.4.1** |
| 3D / house | SceneView / Filament via `io.github.sceneview:sceneview:4.15.0` |
| City render | Custom Compose `Canvas` isometric projection (not a separate web server) |
| Tests | JUnit **4.13.2** unit tests; AndroidX instrumentation runner declared |
| App id / version | `buzz.delena.forgecity` · `versionName` `0.18.0-production-house-mode-dev` · `versionCode` **38** |

No `package.json`, `pom.xml`, or Docker/Compose files exist in this repo.

## Current features (as-built)

- **HOME + LAUNCHER registration** — `MainActivity` with `HOME`/`DEFAULT` and `LAUNCHER` intent filters; portrait, singleTask.
- **Four Home modes** (Settings cycles): **City → House → Assistant → Production House → City** (`HomeMode`).
  - **City** — isometric Canvas city; pan/zoom; tap building to launch app; day/night ambient; optional Media3 background video; districts (Forge, Vault, Nexus, Archive, Arena, Garden, Custom); search fly-to; long-press pin favorites.
  - **House** — Filament/SceneView 3D house surface (with Compose fallback path via feature flags); room placement of apps; humanoids, IBL/lighting, day cycle, dust, cables, face card assets.
  - **Assistant** — full-screen animated character (`AssistantCharacterScreen`); rigged glTF with procedural fallback; resume greeting via device TTS; tap reaction + wave; daily-open streak callouts.
  - **Production House** — full-screen WebView loading live `https://production-house.delena.buzz`; favorites dock retained; always-reachable **Apps** chip toggles dock hide/show only.
- **Launcher chrome** — overflow menu for Launcher UI / Assistant settings / Search / Favorites dock; chapter pill; resource counters; persistent UI toggle chip.
- **App discovery / launch** — `PackageManager` + `<queries>` launcher intents; favorites dock pin/unpin/launch.
- **Progression / story** — Room-backed building stats/XP from launches; UsageStats harvest + WorkManager; chapter briefings and quest stubs in `StoryCatalog`.
- **Forge Assistant / speech** — notification listener service; modes OFF / DIRECT_TTS / AGENT_PORTAL_TAMIL / GEMINI_TAMIL / SMART_CASCADE; Gemini audio TTS catalog (models/voices/templates); Agent Portal Tamil rewrite HTTPS client; diagnostics ring buffer; quiet hours / allowlist patterns documented in OPS.
- **Power gating** — animation / house perf budgets tied to power-save style policies.
- **Unit tests** — substantial `app/src/test` coverage (city, data migrations, house, assistant, Production House URL, HomeMode, etc.); one instrumented smoke test under `androidTest`.

## Deploy topology (known facts below - cross-check against what you find in-repo, note any discrepancy explicitly rather than silently picking one)

**External note to verify:** “No web port reserved in the machine ports registry. Described elsewhere as a public Docker-Compose-based isometric-style launcher.”

**In-repo facts:**

- This project is an **Android APK**, not a hosted web service. `docs/OPS.md` states: “Sandbox DEV only. No host ports / Postgres / CSS for this APK.” `docs/ARCHITECTURE.md` states: “DEV-only sandbox project. No F:/G: deploy, no nginx host, no CSS client until a companion web console is explicitly requested.”
- **No** `docker-compose` / `Dockerfile` present. Isometric city rendering is **on-device Canvas Compose**, not a Compose (Docker) stack.
- Ship path: GitHub public repo → prerelease **debug-signed** APK assets on version tags; sideload via `adb install` / download URL (see README / OPS).
- Optional outbound HTTPS only: Agent Portal Tamil rewrite endpoints and Gemini APIs (user-configured keys); Production House mode embeds remote URL `https://production-house.delena.buzz` in a WebView.

**Discrepancy:** The “Docker-Compose-based … launcher” description does **not** match this repository. Agree with the ports-registry note: there is **no** local web listen port for this app. The isometric launcher is the Android HOME app; Docker Compose is not the delivery mechanism here.

## Known debt / gaps (as-is, factual)

- **Realme #16 physical E2E still PENDING** across tip releases through `0.18.0` — documented repeatedly in `docs/VERIFICATION.md`, README, HANDOFF, ROADMAP (no ADB device on build host; prerelease waivers).
- **0.18.0 Production House** — WebView load + dock toggle **not yet device-confirmed** (HANDOFF / VERIFICATION / README).
- **0.17.0 rigged Assistant character** — Filament/glTF path least-verified ship; user must confirm rigged asset vs procedural fallback on device.
- **Story chapter 2–3 quests** — seeded as locked stubs (`StoryCatalog`); fuller progression rules called out as later waves in ARCHITECTURE/ROADMAP.
- **Background video** — framework landed; VERIFICATION historically notes final MP4 / device decoder soak as pending when asset absent.
- **Annotated non-debug / production tags** — blocked while #16 E2E remains pending (VERIFICATION pattern).
- **House FPS estimator** — `HousePerfBudget` documents an always-unknown stub `FpsEstimator` (no real sampler wired).
- Roadmap still lists aspirational phases (weather, in-city multi-agent layer, etc.) beyond what is shipped; those are **not** claimed as as-built here.

## Sources consulted

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/OPS.md`
- `docs/HANDOFF.md`
- `docs/VERIFICATION.md` (including 0.16–0.18 sections)
- `docs/ROADMAP.md` (current-state / tip sections)
- `build.gradle.kts`
- `app/build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/buzz/delena/forgecity/HomeMode.kt`
- `app/src/main/java/buzz/delena/forgecity/assistant/AssistantSpeechMode.kt`
- `app/src/main/java/buzz/delena/forgecity/data/ForgeCityDatabase.kt`
- `app/src/main/java/buzz/delena/forgecity/ui/ForgeCityHomeScreen.kt` (imports / structure)
- `app/src/main/java/buzz/delena/forgecity/ui/lot/ProductionHouseWebSurface.kt`
- `app/src/main/java/buzz/delena/forgecity/power/HousePerfBudget.kt` (stub note via search)
- `app/src/main/java/buzz/delena/forgecity/story/StoryCatalog.kt` (stub note via search)
- Glob checks: no `package.json`, no `docker-compose*` / `Dockerfile*`; `app/src/test` and `app/src/androidTest` inventory
