# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.5.1-gemini-audio-fix-dev`
**Latest prerelease:** [`v0.5.1-gemini-audio-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.5.1-gemini-audio-fix-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.5.1-gemini-audio-fix-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.5.1-gemini-audio-fix-dev/forgecity-0.5.1-gemini-audio-fix-dev-debug.apk
Get-FileHash .\forgecity-0.5.1-gemini-audio-fix-dev-debug.apk -Algorithm SHA256
# expect 05D21575B597856A01989F8B15E2BD1804497294A4ECE296C188F8AFC1D52365
adb install -r .\forgecity-0.5.1-gemini-audio-fix-dev-debug.apk
```

## What works in 0.5.1-gemini-audio-fix-dev

- **Fix:** Gemini native audio request matches official TTS `generateContent` shape
  (voice-only `speechConfig`; language steered via prompt — invalid `languageCode` removed)
- Retry on empty/server audio; larger response budget; AudioTrack prime-write
- Plus **0.5.0** UI polish (city-first chrome, buildings, video scrims, inertia/dock)
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
