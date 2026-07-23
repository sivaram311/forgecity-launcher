# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.7.0-assistant-clarity-dev`
**Latest prerelease:** [`v0.7.0-assistant-clarity-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.7.0-assistant-clarity-dev) · prior [`v0.6.1-tts-error-log-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.1-tts-error-log-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.7.0-assistant-clarity-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.7.0-assistant-clarity-dev/forgecity-0.7.0-assistant-clarity-dev-debug.apk
Get-FileHash .\forgecity-0.7.0-assistant-clarity-dev-debug.apk -Algorithm SHA256
# expect CA5EE2B60FF8DBF75F63A40BDA55672D799874689CA8B346FDD201F579A408FC
adb install -r .\forgecity-0.7.0-assistant-clarity-dev-debug.apk
```

## What works in 0.7.0-assistant-clarity-dev

- **Assistant Clarity:** mode-gated Gemini vs Portal settings; speak-aloud prompt
  validation (`PromptModeValidator`); presets Tamil clear / Kongu friend / English brief
- Masked API keys with reveal; **TEST TTS** gated off when audio prompt invalid
- Speech diagnostics: **COPY LOG** / **CLEAR** (same privacy rules as 0.6.1)
- Plus **0.6.x** city 3D + diagnostics, **0.5.x** Gemini audio + UI polish
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
