# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `feature/background-video-asset` · version `0.3.3-background-video-asset-dev`
**Latest prerelease:** [`v0.3.3-background-video-asset-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.3.3-background-video-asset-dev)

## Download

Latest debug prerelease (sideload):

- Releases: https://github.com/sivaram311/forgecity-launcher/releases
- Tag target: `v0.3.3-background-video-asset-dev`

This is **debug-signed**. Realme P2 Pro E2E (#16) is still pending for any annotated production tag.

```powershell
curl.exe -L -o forgecity-0.3.3-background-video-asset-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.3.3-background-video-asset-dev/forgecity-0.3.3-background-video-asset-dev-debug.apk
Get-FileHash .\forgecity-0.3.3-background-video-asset-dev-debug.apk -Algorithm SHA256
# expect B0B9EBC58D2AFB0AD47626790CBEBA98DD0335C0C87D7E7D7AF0E70D6018B7D4
adb install -r .\forgecity-0.3.3-background-video-asset-dev-debug.apk
```

## What works in 0.3.3-background-video-asset-dev

- Bundled original procedural `city_background.mp4` (1080×1920 H.264, 10s, muted, ~3.0 MB)
- Media3 ExoPlayer full-screen looping video layer beneath `CityCanvas`
- Persisted Background Video toggle and 40–100% opacity slider
- Lifecycle pause/resume, mute, local-media low buffer, power-save/screen-off gating
- Runtime lookup still falls back to day/night gradient if the raw asset is removed
- Sparse isometric city + depth-sorted AABB hit testing + press glow
- Favorites dock (long-press pin/unpin, max 6, Room `building_stats.isFavorite`)
- Neon assistant bubble + NotificationListenerService
- Privacy-first TTS (off by default, empty allowlist, editable quiet hours, never persists bodies)
- Assistant bubble invokes the notification's exact `contentIntent`, with app-launch fallback
- Dusk purple→orange sky, power-grid lines, chapter card, level growth animation
- Phase 2: UsageStats XP, day/night, fly-in, harvest debounce, Migration(1,2)+`(2,3)`

## Spec

See [docs/IMPLEMENTATION-SPEC.md](docs/IMPLEMENTATION-SPEC.md) and
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
