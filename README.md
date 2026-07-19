# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. Chapter 1 (**Embers**) ships as
the MVP city shell.

## What works in 0.1.0-mvp

- Registers as Android `HOME` + `DEFAULT` (+ app-drawer `LAUNCHER` entry).
- Jetpack Compose isometric city canvas with pan/zoom.
- Installed launchable apps mapped to district-colored buildings.
- Tap a building to launch the app.
- Search filter over building labels.
- Room schema for city meta, buildings, and story progress (seeded Chapter 1).
- Story briefing + resource strip (scrap / power / focus / gold dust).
- Package install/remove refresh.

## Explicitly not in MVP

Living AI agents, UsageStats XP loop, weather API, Filament, widgets, icon
packs, notification badges, cloud sync, and `QUERY_ALL_PACKAGES`. App discovery
uses launcher-intent `<queries>` only.

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
| [docs/STORY-BIBLE.md](docs/STORY-BIBLE.md) | Campaign outline |
| [docs/OPS.md](docs/OPS.md) | Build / device ops |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | Evidence record |
| [agents/crew/CREW.md](agents/crew/CREW.md) | Development crew |

Device SoT: `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`

Related prototype: `E:\MyWorkspace\sandbox\my-realme-launcher` (Views-based Phase 0).
