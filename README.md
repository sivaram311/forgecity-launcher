# ForgeCity Launcher

Story-driven HOME for Realme P2 / P2 Pro: apps live in a warm interior **house**
(with idle characters) or classic isometric city. Assistant can read notifications aloud.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.10.1-filament-fix-dev`
**Latest prerelease:** [`v0.10.1-filament-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.10.1-filament-fix-dev)

## Download

Tip (**0.10.1** Filament fix) — published prerelease:

```powershell
curl.exe -L -o forgecity-0.10.1-filament-fix-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.10.1-filament-fix-dev/forgecity-0.10.1-filament-fix-dev-debug.apk
Get-FileHash .\forgecity-0.10.1-filament-fix-dev-debug.apk -Algorithm SHA256
# expect 1CEB353D9B6F4D0F6B21390A25436CA97568F1D36D286C909896544A7C541116
```

## What works in 0.10.1

- Immersive fullscreen HOME; SceneView **TextureSurface** (fixes blank 3D behind Compose)
- Filament house + orbit + app marker chips; Compose/CityCanvas fallbacks
- Grok ship/lighting notes: [GROK-FILAMENT-SHIP.md](docs/design/GROK-FILAMENT-SHIP.md), [GROK-FILAMENT-LIGHTING.md](docs/design/GROK-FILAMENT-LIGHTING.md)
- Device checklist: [docs/DEVICE-E2E-HOUSE-CHECKLIST.md](docs/DEVICE-E2E-HOUSE-CHECKLIST.md) (#16 still pending)

## Docs

| Doc | Purpose |
|-----|---------|
| [GROK-3D-HOUSE-LAUNCHER-PLAN.md](docs/design/GROK-3D-HOUSE-LAUNCHER-PLAN.md) | Full Grok plan |
| [HANDOFF.md](docs/HANDOFF.md) | Tip + next |
| [OPS.md](docs/OPS.md) | Install / grants |
