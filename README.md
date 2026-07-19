# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)  
**Branch:** `feature/assistant-handoff-gaps` · version `0.3.1-forge-assistant-dev`
**Stable on main (pre-assistant):** Phase 2 Awakening merged · tag `v0.2.0-awakening-dev`

## Download

Latest debug prerelease (sideload):

- Releases: https://github.com/sivaram311/forgecity-launcher/releases
- Tag target: `v0.3.1-forge-assistant-dev`

This is **debug-signed**. Realme P2 Pro E2E (#16) is still pending for any annotated production tag.

```powershell
curl.exe -L -o forgecity-0.3.1-forge-assistant-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.3.1-forge-assistant-dev/forgecity-0.3.1-forge-assistant-dev-debug.apk
Get-FileHash .\forgecity-0.3.1-forge-assistant-dev-debug.apk -Algorithm SHA256
# expect F1FF71110BD2DC4BABF1D6E724EDDC7DA00075D0B6FAEE8E6CEE873F62920171
adb install -r .\forgecity-0.3.1-forge-assistant-dev-debug.apk
```

## What works in 0.3.1-forge-assistant-dev

- Sparse isometric city + depth-sorted AABB hit testing + press glow
- Favorites dock (long-press pin/unpin, max 6, Room `building_stats.isFavorite`)
- Neon assistant bubble + NotificationListenerService
- Privacy-first TTS (off by default, empty allowlist, editable quiet hours, never persists bodies)
- Assistant bubble invokes the notification's exact `contentIntent`, with app-launch fallback
- Dusk purple→orange sky, power-grid lines, chapter card, level growth animation
- Phase 2: UsageStats XP, day/night, fly-in, harvest debounce, Migration(1,2)+`(2,3)`

## Spec

See [docs/IMPLEMENTATION-SPEC.md](docs/IMPLEMENTATION-SPEC.md).

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
