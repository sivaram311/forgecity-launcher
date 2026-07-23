# ForgeCity Launcher

Story-driven HOME for Realme P2 / P2 Pro: apps live in a warm interior **house**
(with idle characters) or classic isometric city. Assistant can read notifications aloud.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.9.0-3d-house-characters-dev`
**Latest prerelease:** [`v0.9.0-3d-house-characters-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.9.0-3d-house-characters-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.9.0-3d-house-characters-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.9.0-3d-house-characters-dev/forgecity-0.9.0-3d-house-characters-dev-debug.apk
Get-FileHash .\forgecity-0.9.0-3d-house-characters-dev-debug.apk -Algorithm SHA256
# expect D8E66EA9442B9C7F7747FCCA9DCBE3FFE454FA3F886648EEEDE842D461154A7F
adb install -r .\forgecity-0.9.0-3d-house-characters-dev-debug.apk
```

## What works in 0.9.0

- House HOME: Vault annex, furniture silhouettes, mayor/assistant/NPC idle characters
- AnimationBudget caps characters + soft shadows; battery saver reduces cast
- Settings: **3D House home** toggle (CityCanvas fallback)
- Assistant speech pulses the assistant character
- City video default off; Assistant Clarity (0.7) unchanged
- Device checklist: [docs/DEVICE-E2E-HOUSE-CHECKLIST.md](docs/DEVICE-E2E-HOUSE-CHECKLIST.md)

## Docs

| Doc | Purpose |
|-----|---------|
| [GROK-3D-HOUSE-LAUNCHER-PLAN.md](docs/design/GROK-3D-HOUSE-LAUNCHER-PLAN.md) | Full Grok plan |
| [HANDOFF.md](docs/HANDOFF.md) | Tip + next |
| [OPS.md](docs/OPS.md) | Install / grants |
