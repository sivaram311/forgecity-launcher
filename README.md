# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. Chapter 1 (**Embers**) ships as
the MVP city shell.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)  
**Branch:** `feature/mvp-city-shell` · version `0.1.0-mvp`

## Download

Prerelease debug build (sideload for testing on Realme P2 Pro / Android 8.0+):

- APK: https://github.com/sivaram311/forgecity-launcher/releases/download/v0.1.0-mvp/forgecity-0.1.0-mvp-debug.apk
- SHA-256: https://github.com/sivaram311/forgecity-launcher/releases/download/v0.1.0-mvp/forgecity-0.1.0-mvp-debug.apk.sha256
- Releases page: https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.1.0-mvp

This is a **debug-signed prerelease** for testing only. On-device Realme E2E is
still pending, and no production (upload-key) signing has been applied.

Verify the hash before installing:

```powershell
curl.exe -L -o forgecity-0.1.0-mvp-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.1.0-mvp/forgecity-0.1.0-mvp-debug.apk
Get-FileHash .\forgecity-0.1.0-mvp-debug.apk -Algorithm SHA256
# expect 073C495949BD52BB1FD9AD09ACBF1A65339F80F6F150B2B3F282960B86C2209A
adb install -r .\forgecity-0.1.0-mvp-debug.apk
```

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
