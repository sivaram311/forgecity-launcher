# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.4.5-split-chips-gemini-mode-dev`
**Latest prerelease:** [`v0.4.5-split-chips-gemini-mode-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.5-split-chips-gemini-mode-dev)

## Download

Latest debug prerelease (sideload):

- Releases: https://github.com/sivaram311/forgecity-launcher/releases
- Tag target: `v0.4.5-split-chips-gemini-mode-dev`

This is **debug-signed**. Realme P2 Pro E2E (#16) is still pending for any annotated production tag.

```powershell
curl.exe -L -o forgecity-0.4.5-split-chips-gemini-mode-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.4.5-split-chips-gemini-mode-dev/forgecity-0.4.5-split-chips-gemini-mode-dev-debug.apk
Get-FileHash .\forgecity-0.4.5-split-chips-gemini-mode-dev-debug.apk -Algorithm SHA256
# expect 86E1CEDA64908B69771E614B210F743070AFDAC0D0E2A7D3D705A98D3FECC917
adb install -r .\forgecity-0.4.5-split-chips-gemini-mode-dev-debug.apk
```

## What works in 0.4.5-split-chips-gemini-mode-dev

- Separate chips: **`ASSIST`**, **`SEARCH`**, **`DOCK`** (+ existing `UI`)
- Dedicated **`GEMINI தமிழ்`** mode (fail-closed) alongside **CASCADE**
- Custom editable **TEST TTS** text field
- Prior Gemini 2.5-flash default + Portal Tamil + diagnostics
- Saved API key is visible in config for device setup (still encrypted at rest)
- Safe terminal diagnostics via `adb logcat -s ForgeCityTTS`; keys and message
  content are never logged
- Immersive city-first launcher: chrome is hidden by default and a persistent
  48 dp safe-area `UI +` / `UI −` chip restores or hides controls
- Persisted speech mode: `OFF` (default), device-locale `DIRECT_TTS`, or
  fail-closed `AGENT_PORTAL_TAMIL`, including legacy preference migration
- **P0:** Eligible notifications → Agent Portal no-store Tamil rewrite → Tamil TTS
- Opt-in Portal mode + encrypted `X-ForgeCity-Key` + HTTPS endpoint
- Bounded RAM queue, coalesce by notification key, silent fail-closed (no English TTS fallback)
- Tamil locale TTS (`ta-IN` / `ta`); no default-locale speech for rewrite path
- **No Cloudflare Workers AI** in the launcher
- All prior `0.3.3` features (video background, assistant bubble, favorites, UsageStats XP)

## Spec

See [docs/TAMIL-REWRITE-SPEC.md](docs/TAMIL-REWRITE-SPEC.md),
[docs/IMPLEMENTATION-SPEC.md](docs/IMPLEMENTATION-SPEC.md), and
[docs/BACKGROUND-VIDEO-SPEC.md](docs/BACKGROUND-VIDEO-SPEC.md).

## Build

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Technical architecture |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Phased roadmap |
| [docs/OPS.md](docs/OPS.md) | Build / device ops |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | Evidence record |
| [docs/IMPLEMENTATION-SPEC.md](docs/IMPLEMENTATION-SPEC.md) | Assistant upgrade spec |
| [docs/BACKGROUND-VIDEO-SPEC.md](docs/BACKGROUND-VIDEO-SPEC.md) | Media3 background contract |
| [docs/TAMIL-REWRITE-SPEC.md](docs/TAMIL-REWRITE-SPEC.md) | **P0** Agent Portal Tamil rewrite + TTS |
| [docs/GEMINI-SPEECH-CASCADE-SPEC.md](docs/GEMINI-SPEECH-CASCADE-SPEC.md) | **P1** Gemini → Portal → device TTS cascade |
| [docs/GLM-IMPLEMENTATION-PLAN.md](docs/GLM-IMPLEMENTATION-PLAN.md) | Older plan — Cloudflare Workers AI path **rejected** for launcher |
