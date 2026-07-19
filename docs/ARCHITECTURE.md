# ForgeCity architecture

**Status:** Forge Assistant + Background Video (`0.3.2-background-video-dev`)
**Primary device:** Realme P2 Pro 5G — Snapdragon 7s Gen 2 / Adreno 710 / 120 Hz  
**Auth / ports / DB env:** none (on-device Android app; no CSS, no host port)

## Stack

| Layer | Choice |
|-------|--------|
| UI | Kotlin + Jetpack Compose |
| City render | Custom `Canvas` isometric projection |
| Video background | Media3 ExoPlayer + `PlayerView`, local raw resource |
| App discovery | `PackageManager` launcher intents + `<queries>` |
| Persistence | Room v3 + migrations 1→2→3; SharedPreferences for assistant/atmosphere |
| Background | WorkManager + UsageStats (Phase 2) |
| Assistant | NotificationListenerService + local TTS; hybrid AI remains future |

## Package map

```text
buzz.delena.forgecity
├── MainActivity / ForgeCityApp
├── city/          Districts, IsoMath, DayNightCycle, classifier, state
├── launcher/      AppCatalog (query + launch + grid placement)
├── data/          Room entities, DAO, DB, Migrations(1→2), repository
├── usage/         UsageStats harvest, XP math, LaunchTracker, WorkManager
├── power/         AnimationBudget (PowerManager gate)
├── assistant/     Notification listener, TTS, settings, quiet hours
├── story/         Chapter briefings + starter quests
└── ui/            Compose home screen, city canvas, ViewModel
    └── background/ Media3 player + lifecycle-aware video composable
```

Parallel streams: [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md).

## Districts

| District | Theme | Unlock |
|----------|-------|--------|
| Forge | Productivity / coding | Chapter 1 |
| Vault | Finance / trading | Chapter 2 |
| Nexus | Social / comms | Chapter 2 |
| Archive | Files / gallery | Chapter 2 |
| Arena | Entertainment | Chapter 3 |
| Garden | Health / mindfulness | Chapter 3 |
| Custom | User-defined | Chapter 5 |

MVP classifier uses package/label heuristics; user overrides come later.

## Performance budget (Adreno 710)

- Keep building primitives to simple extruded diamonds (no heavy meshes).
- Pause ambient animation when `PowerManager.isPowerSaveMode` or screen off (Phase 2).
- Video exists only while enabled/resumed; muted local decode, low local buffer,
  repeat-all, max target source 1080×1920/30fps.
- Missing/failed video resource falls back to the day/night gradient.
- Prefer `graphicsLayer` / transform gestures over layout thrash.
- Cap simultaneous particle emitters; profile on device before Filament upgrade.

## Permissions policy

| Permission | MVP | Notes |
|------------|-----|-------|
| Launcher `<queries>` | Yes | Sufficient for home-screen app listing |
| `PACKAGE_USAGE_STATS` | Declared | Used in Phase 2 XP; requires Settings grant |
| `QUERY_ALL_PACKAGES` | No | Sideload-only option later; Play-restricted |
| Notification listener | Yes | User-granted; deny-by-default allowlist, TTS off by default |

## Phase plan

See **[ROADMAP.md](ROADMAP.md)** for the full phased plan (v0.1 → v1.0),
feature matrix, risks, and success metrics.

1. **MVP City Shell** ✅ (`v0.1.0-mvp`)  
2. Living map + UsageStats resources (`v0.2.0 Awakening`) ← **in progress** (device E2E next)  
3. Story engine branching (`v0.3.0`)  
4. In-city AI agents (`v0.4.0 Symphony`)  
5. Realme polish + optional Filament / sync (`v1.0.0`)

## Promote / env

DEV-only sandbox project. No F:/G: deploy, no nginx host, no CSS client until a
companion web console is explicitly requested.

Remote: https://github.com/sivaram311/forgecity-launcher (public). Branch pushes
require Reviewer SIGN-OFF (CONSCIOUS #17). APK tags also need DEV device E2E
(#14–#16) once a Realme P2 Pro is available for physical smoke.
