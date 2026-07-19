# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.4.0-tamil-agent-dev`
**Latest prerelease:** [`v0.4.0-tamil-agent-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.0-tamil-agent-dev) (PR #6 merged)

## Download

Latest debug prerelease (sideload):

- Releases: https://github.com/sivaram311/forgecity-launcher/releases
- Tag target: `v0.4.0-tamil-agent-dev`

This is **debug-signed**. Realme P2 Pro E2E (#16) is still pending for any annotated production tag.

```powershell
curl.exe -L -o forgecity-0.4.0-tamil-agent-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.4.0-tamil-agent-dev/forgecity-0.4.0-tamil-agent-dev-debug.apk
Get-FileHash .\forgecity-0.4.0-tamil-agent-dev-debug.apk -Algorithm SHA256
# expect E4C3E161D464D2AC15994AE91F5880FB160B7D99F775743A98A81F9224497AD8
adb install -r .\forgecity-0.4.0-tamil-agent-dev-debug.apk
```

## What works in 0.4.0-tamil-agent-dev

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
| [docs/GLM-IMPLEMENTATION-PLAN.md](docs/GLM-IMPLEMENTATION-PLAN.md) | Older plan — Cloudflare Workers AI path **rejected** for launcher |
