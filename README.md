# ForgeCity Launcher

Story-driven HOME for Realme P2 / P2 Pro: apps live in a warm interior **house**
(with idle characters) or classic isometric city. Assistant can read notifications aloud.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.10.0-filament-house-dev`
**Latest prerelease:** [`v0.9.0-3d-house-characters-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.9.0-3d-house-characters-dev) · tip tag `v0.10.0-filament-house-dev` pending

## Download

Tip (**0.10.0** Filament house) — build locally or wait for tag:

```powershell
.\gradlew.bat assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
# rename to forgecity-0.10.0-filament-house-dev-debug.apk after tag publish
Get-FileHash .\forgecity-0.10.0-filament-house-dev-debug.apk -Algorithm SHA256
# expect 3D958C94EA50A82C85A0EF4F01BA6B7AF2C1BB6D5ADCB13BD0C5C6371293D9C2
```

Prior (**0.9.0** procedural house):

```powershell
curl.exe -L -o forgecity-0.9.0-3d-house-characters-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.9.0-3d-house-characters-dev/forgecity-0.9.0-3d-house-characters-dev-debug.apk
Get-FileHash .\forgecity-0.9.0-3d-house-characters-dev-debug.apk -Algorithm SHA256
# expect D8E66EA9442B9C7F7747FCCA9DCBE3FFE454FA3F886648EEEDE842D461154A7F
```

## What works in 0.10.0

- **Filament house HOME** (SceneView `4.15.0`): `house_shell.glb` + `char_idle.glb`; `HouseFeatureFlags.useFilamentHouse=true`
- Procedural `HouseHomeSurface` fallback when flag off; **CityCanvas** when house toggle off
- Vault annex, furniture, idle characters, AnimationBudget caps, assistant speech pulse (0.9 carry-over)
- Grok ship/lighting notes: [GROK-FILAMENT-SHIP.md](docs/design/GROK-FILAMENT-SHIP.md), [GROK-FILAMENT-LIGHTING.md](docs/design/GROK-FILAMENT-LIGHTING.md)
- Device checklist: [docs/DEVICE-E2E-HOUSE-CHECKLIST.md](docs/DEVICE-E2E-HOUSE-CHECKLIST.md) (#16 still pending)

## Docs

| Doc | Purpose |
|-----|---------|
| [GROK-3D-HOUSE-LAUNCHER-PLAN.md](docs/design/GROK-3D-HOUSE-LAUNCHER-PLAN.md) | Full Grok plan |
| [HANDOFF.md](docs/HANDOFF.md) | Tip + next |
| [OPS.md](docs/OPS.md) | Install / grants |
