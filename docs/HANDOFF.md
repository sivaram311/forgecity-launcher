# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.11.0-humanoid-daycycle-dev` · versionCode **28** |
| Latest release | [`v0.11.0-humanoid-daycycle-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.11.0-humanoid-daycycle-dev) |
| APK SHA-256 | `E09408908541924FA99B0A1A2D1452795F41377E3DDDE67AFFBAB3D080FBE1A6` |
| Prior tip | [`v0.10.5-wall-character-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.10.5-wall-character-dev) · SHA `66BE9AAA…` |

## Waves complete (0.8 → 0.11)

| Wave | Landed |
|------|--------|
| 0 | Flags, video default off, version train |
| 1 | Procedural house + placement engine |
| 2 | Vault annex, furniture, idle characters, house/city toggle, budget caps, assistant speech pulse |
| 3 | `HousePerfBudget`, DEVICE-E2E-HOUSE-CHECKLIST (#16 still needs Realme) |
| 4 | **Filament** SceneView `4.15.0` · glTF assets · lighting/dust/walls |
| 5 | **0.11** jointed capsule humanoids + day-cycle + window/rim finishing |

**Fallbacks:** procedural `HouseHomeSurface` when `useFilamentHouse=false`; CityCanvas when house toggle off.

**Grok / gap:** [design/GAP-VS-PRODUCTION-HOUSE.md](design/GAP-VS-PRODUCTION-HOUSE.md) · [design/GROK-0.11-HUMANOID.md](design/GROK-0.11-HUMANOID.md)

## Now → next

| Now | Next |
|-----|------|
| 0.11.0 humanoid + day-cycle | 0.11.1 droop cables + hero props + softer dust |
| Compile + unit PASS | Device #16 using `docs/DEVICE-E2E-HOUSE-CHECKLIST.md` (prerelease waiver OK) |

Session: 2026-07-23.
