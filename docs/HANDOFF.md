# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.8.0-3d-house-dev` · versionCode 20 |
| Latest release | [`v0.8.0-3d-house-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.8.0-3d-house-dev) |
| APK SHA-256 | `C14D5E2CCE7F5C29387CB1BC88BD15E5228BADC0219F88D4936B8D6F7F0AAF3E` |
| Prior tip | [`v0.7.0-assistant-clarity-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.7.0-assistant-clarity-dev) |

## 3D House Wave 0/1 (0.8.0)

- **House HOME on:** procedural Compose floor-plan (`HouseHomeSurface`); `HouseFeatureFlags.use3dHouse=true`
- **City video off:** `useCityVideo=false`; new installs default background video off
- **Placement:** `AppPlacementEngine` + district→room map; apps as tappable room markers
- **Budget:** `AnimationBudget` quality tiers (HIGH/MEDIUM/LOW) for later Filament/characters
- **Fallback:** set `use3dHouse=false` to restore CityCanvas
- **Plan:** [design/GROK-3D-HOUSE-LAUNCHER-PLAN.md](design/GROK-3D-HOUSE-LAUNCHER-PLAN.md)

Still pending vs Grok full vision: Filament/glTF, characters, Vault room cell, #16 device E2E.

## Now → next

| Now | Next |
|-----|------|
| 0.8.0 Wave 1 house published | Device: tap app markers in rooms; flip flag if need city |
| Filament + characters | Wave 2/3 per Grok plan |
| Realme E2E (#16) PENDING | Blocks production tags |

Session: 2026-07-23.
