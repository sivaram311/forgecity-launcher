# ForgeCity Launcher

A story-driven home screen for Realme P2 / P2 Pro (ColorOS). Apps live in a
warm interior house (Wave 1) or classic isometric city (fallback). Habits rebuild
districts. A neon city assistant can read notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.8.0-3d-house-dev`
**Latest prerelease:** [`v0.8.0-3d-house-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.8.0-3d-house-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.8.0-3d-house-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.8.0-3d-house-dev/forgecity-0.8.0-3d-house-dev-debug.apk
Get-FileHash .\forgecity-0.8.0-3d-house-dev-debug.apk -Algorithm SHA256
# expect C14D5E2CCE7F5C29387CB1BC88BD15E5228BADC0219F88D4936B8D6F7F0AAF3E
adb install -r .\forgecity-0.8.0-3d-house-dev-debug.apk
```

## What works in 0.8.0-3d-house-dev

- **House HOME (Wave 1):** procedural multi-room floor plan; apps as tappable markers
- **Placement engine:** districts → rooms; favorites prefer desk/TV hotspots
- **City video off** by default (`HouseFeatureFlags.useCityVideo=false`)
- **CityCanvas fallback:** set `HouseFeatureFlags.use3dHouse=false`
- Plus **0.7** Assistant Clarity (mode-gated Gemini/Portal, prompt validation, COPY LOG)

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/design/GROK-3D-HOUSE-LAUNCHER-PLAN.md](docs/design/GROK-3D-HOUSE-LAUNCHER-PLAN.md) | Grok realistic 3D house plan |
| [docs/HANDOFF.md](docs/HANDOFF.md) | Current tip + next actions |
| [docs/OPS.md](docs/OPS.md) | Install, grants, diagnostics |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Phased roadmap |

## Build

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```
