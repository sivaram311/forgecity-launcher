# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.6.0-city-3d-dev`
**Latest prerelease:** [`v0.6.0-city-3d-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.0-city-3d-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.6.0-city-3d-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.6.0-city-3d-dev/forgecity-0.6.0-city-3d-dev-debug.apk
Get-FileHash .\forgecity-0.6.0-city-3d-dev-debug.apk -Algorithm SHA256
# expect F637ECF048AF7DFBC921F6C074F6EABD6A3CC72C7D046BDD5578B766D9105A2A
adb install -r .\forgecity-0.6.0-city-3d-dev-debug.apk
```

## What works in 0.6.0-city-3d-dev

- **Premium low-poly 3D city pass:** gradient wall shading, roof rim light,
  softer contact shadows, depth-shaded ground plane
- **Emissive windows:** deterministic per-pane night glow with subtle flicker;
  new continuous `nightFactor` + `timeSeconds` lighting model (backward compatible)
- Roof silhouettes gain night emissive accents — shapes, hit testing, and district
  theme unchanged
- Plus **0.5.1** Gemini native audio fix and **0.5.0** UI polish
- Speech modes: OFF → DIRECT → PORTAL → GEMINI AUDIO → CASCADE

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/HANDOFF.md](docs/HANDOFF.md) | Current tip + next actions |
| [docs/OPS.md](docs/OPS.md) | Install, grants, diagnostics |
| [docs/GEMINI-SPEECH-CASCADE-SPEC.md](docs/GEMINI-SPEECH-CASCADE-SPEC.md) | Speech cascade |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | Build evidence |

## Build

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Debug-signed prerelease. Realme device E2E (#16) still **PENDING** — not production.
