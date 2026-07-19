# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. Chapter 1 (**Embers**) ships as
the MVP city shell.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)  
**Branch:** `feature/phase-2-awakening` · version `0.2.0-awakening-dev` (Phase 2 Waves 1–2)  
**PR:** https://github.com/sivaram311/forgecity-launcher/pull/1  
**Stable MVP tag:** `v0.1.0-mvp` on `main`

## Download

Latest Phase 2 debug prerelease (sideload for testing on Realme P2 Pro / Android 8.0+):

- APK: https://github.com/sivaram311/forgecity-launcher/releases/download/v0.2.0-awakening-dev/forgecity-0.2.0-awakening-dev-debug.apk
- SHA-256: https://github.com/sivaram311/forgecity-launcher/releases/download/v0.2.0-awakening-dev/forgecity-0.2.0-awakening-dev-debug.apk.sha256
- Releases page: https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.2.0-awakening-dev

This is a **debug-signed prerelease** for testing only. On-device Realme E2E (#16) is
still pending, and no production (upload-key) signing has been applied.

Verify the hash before installing:

```powershell
curl.exe -L -o forgecity-0.2.0-awakening-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.2.0-awakening-dev/forgecity-0.2.0-awakening-dev-debug.apk
Get-FileHash .\forgecity-0.2.0-awakening-dev-debug.apk -Algorithm SHA256
# expect 885182F9A12671BE1E68E3DF6819518FE20A308A3D6748FCAF2C440345E89B11
adb install -r .\forgecity-0.2.0-awakening-dev-debug.apk
```

Older MVP debug build remains at tag [`v0.1.0-mvp`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.1.0-mvp).

## What works in 0.2.0-awakening-dev (Phase 2 Waves 1–2)

- Everything from 0.1.0-mvp, plus:
- Real-clock day/night sky + night building glows + stars (battery-gated)
- Camera fly-in on tap before launch; double-tap recenter
- UsageStats → Power / Focus / Gold / Scrap (needs Usage Access grant)
- Periodic WorkManager harvest (6h) with 1h debounce
- Building levels from launches (Room `building_stats`)
- Level-up particle burst + animated resource counters
- Chapter 2–3 quest stubs (seeded locked)
- Parallel execution plan: [docs/PARALLEL-EXECUTION.md](docs/PARALLEL-EXECUTION.md)

## What worked in 0.1.0-mvp

- Registers as Android `HOME` + `DEFAULT` (+ app-drawer `LAUNCHER` entry).
- Jetpack Compose isometric city canvas with pan/zoom.
- Installed launchable apps mapped to district-colored buildings.
- Tap a building to launch the app.
- Search filter over building labels.
- Room schema for city meta, buildings, and story progress (seeded Chapter 1).
- Story briefing + resource strip (scrap / power / focus / gold dust).
- Package install/remove refresh.

## Explicitly not yet

Living AI agents, weather API, Filament, widgets, icon packs, notification
badges, cloud sync, and `QUERY_ALL_PACKAGES`. App discovery uses launcher-intent
`<queries>` only. UsageStats XP is in **0.2.0-awakening-dev** (needs Usage Access).

## Next (blocks annotated `v0.2.0`)

1. Realme P2 Pro device E2E — checklist in [docs/OPS.md](docs/OPS.md)
2. Proper Room `Migration(1,2)` (replace destructive fallback before non-debug)
3. Merge [PR #1](https://github.com/sivaram311/forgecity-launcher/pull/1) after E2E GO

## Requirements

- Android Studio + SDK Platform 35
- JDK 17+
- Device API 26+ (portrait-first; Realme P2 Pro primary)

## Build

```powershell
.\gradlew.bat test lint assembleDebug
```

APK: `app\build\outputs\apk\debug\app-debug.apk`

## Install

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

Accept the home-role prompt, or Settings → Default apps → Home app.

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Technical architecture |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Phased roadmap (vision → v1.0) |
| [docs/PARALLEL-EXECUTION.md](docs/PARALLEL-EXECUTION.md) | Parallel streams / waves (CONSCIOUS) |
| [docs/STORY-BIBLE.md](docs/STORY-BIBLE.md) | Campaign outline |
| [docs/OPS.md](docs/OPS.md) | Build / device ops |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | Evidence record |
| [agents/crew/CREW.md](agents/crew/CREW.md) | Development crew |

Device SoT: `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`

Related prototype: `E:\MyWorkspace\sandbox\my-realme-launcher` (Views-based Phase 0).
