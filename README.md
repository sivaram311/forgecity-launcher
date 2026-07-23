# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.6.1-tts-error-log-dev`
**Latest prerelease:** [`v0.6.1-tts-error-log-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.1-tts-error-log-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.6.1-tts-error-log-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.6.1-tts-error-log-dev/forgecity-0.6.1-tts-error-log-dev-debug.apk
Get-FileHash .\forgecity-0.6.1-tts-error-log-dev-debug.apk -Algorithm SHA256
# expect BE2F45E5EF46F7CD11F4B3CBB0A03A3CD0DA49E8889E7AA0A054699600568383
adb install -r .\forgecity-0.6.1-tts-error-log-dev-debug.apk
```

## What works in 0.6.1-tts-error-log-dev

- **In-app speech diagnostics log:** append-only safe events in Assistant settings;
  **COPY LOG** → paste into chat with the agent (no keys / notification bodies)
- Plus **0.6.0** premium low-poly 3D city pass
- Plus **0.5.1** Gemini native audio fix and **0.5.0** UI polish
- Speech modes: OFF → DIRECT → PORTAL → GEMINI AUDIO → CASCADE

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/design/GROK-LAUNCHER-CONFIG-ROADMAP.md](docs/design/GROK-LAUNCHER-CONFIG-ROADMAP.md) | Grok 4.1 config panel + roadmap consult |
| [docs/HANDOFF.md](docs/HANDOFF.md) | Current tip + next actions |
| [docs/OPS.md](docs/OPS.md) | Install, grants, diagnostics |
| [docs/GEMINI-SPEECH-CASCADE-SPEC.md](docs/GEMINI-SPEECH-CASCADE-SPEC.md) | Speech cascade |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | Build evidence |

## Build

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```
