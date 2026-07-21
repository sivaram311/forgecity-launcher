# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.5.0-ui-polish-dev`
**Latest prerelease:** [`v0.5.0-ui-polish-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.5.0-ui-polish-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.5.0-ui-polish-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.5.0-ui-polish-dev/forgecity-0.5.0-ui-polish-dev-debug.apk
Get-FileHash .\forgecity-0.5.0-ui-polish-dev-debug.apk -Algorithm SHA256
# expect BB8FECCF655928DC5EC5D28665890CE3FC63F7422028F9E3A6327D2C062C3CFA
adb install -r .\forgecity-0.5.0-ui-polish-dev-debug.apk
```

## What works in 0.5.0-ui-polish-dev

- **City-first home:** chapter pill, compact resources, overflow menu (not four text chips)
- **Assistant settings** in a modal sheet (not permanent home scroll)
- **Building craft:** district roof silhouettes, ground plane, facade windows, LOD icon badges, gold favorite pin
- **Video atmosphere:** local chrome scrims + mid fade (no full-screen mud); existing looping MP4
- **Motion:** pan inertia, search fly-to match, long-press haptics, glassier dock
- Speech stack from **0.4.7**: Gemini PCM MODE_STREAM + MediaPlayer fallback, CASCADE modes

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/HANDOFF.md](docs/HANDOFF.md) | Current tip + next actions |
| [docs/OPS.md](docs/OPS.md) | Install, grants, diagnostics |
| [docs/UI-POLISH-IMPL-BRIEF.md](docs/UI-POLISH-IMPL-BRIEF.md) | 0.5.0 slice scope |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | Build evidence |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Product phases |

## Build

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Debug-signed prerelease. Realme device E2E (#16) still **PENDING** — not production.
