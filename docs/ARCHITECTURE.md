# ForgeCity architecture

**Status:** Forge Assistant + Background Video asset (`0.3.3-background-video-asset-dev`)
**Primary device:** Realme P2 Pro 5G â€” Snapdragon 7s Gen 2 / Adreno 710 / 120 Hz
**Auth / ports / DB env:** none (on-device Android app; no CSS, no host port)

## Stack

| Layer | Choice |
|-------|--------|
| UI | Kotlin + Jetpack Compose |
| City render | Custom `Canvas` isometric projection |
| Video background | Media3 ExoPlayer + `PlayerView`, local raw resource |
| App discovery | `PackageManager` launcher intents + `<queries>` |
| Persistence | Room v3 + migrations 1â†’2â†’3; SharedPreferences for assistant/atmosphere |
| Background | WorkManager + UsageStats (Phase 2) |
| Assistant | NotificationListenerService + local TTS; hybrid AI remains future |

## Package map

```text
buzz.delena.forgecity
â”œâ”€â”€ MainActivity / ForgeCityApp
â”œâ”€â”€ city/          Districts, IsoMath, DayNightCycle, classifier, state
â”œâ”€â”€ launcher/      AppCatalog (query + launch + grid placement)
â”œâ”€â”€ data/          Room entities, DAO, DB, Migrations(1â†’2), repository
â”œâ”€â”€ usage/         UsageStats harvest, XP math, LaunchTracker, WorkManager
â”œâ”€â”€ power/         AnimationBudget (PowerManager gate)
â”œâ”€â”€ assistant/     Notification listener, TTS, settings, quiet hours
â”œâ”€â”€ story/         Chapter briefings + starter quests
â””â”€â”€ ui/            Compose home screen, city canvas, ViewModel
    â””â”€â”€ background/ Media3 player + lifecycle-aware video composable
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
  repeat-all, max target source 1080Ã—1920/30fps.
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

See **[ROADMAP.md](ROADMAP.md)** for the full phased plan (v0.1 â†’ v1.0),
feature matrix, risks, and success metrics.

1. **MVP City Shell** âœ… (`v0.1.0-mvp`)
2. Living map + UsageStats resources (`v0.2.0 Awakening`) âœ… code; device E2E pending
2.5. Media3 background video (`v0.3.2`) âœ… framework; final MP4 + device E2E pending
3. Story engine branching (`v0.3.0`)
4. In-city AI agents (`v0.4.0 Symphony`)
5. Realme polish + optional Filament / sync (`v1.0.0`)

## Promote / env

DEV-only sandbox project. No F:/G: deploy, no nginx host, no CSS client until a
companion web console is explicitly requested.

Remote: https://github.com/sivaram311/forgecity-launcher (public). Branch pushes
require Reviewer SIGN-OFF (CONSCIOUS #17). APK tags also need DEV device E2E
(#14â€“#16) once a Realme P2 Pro is available for physical smoke.
