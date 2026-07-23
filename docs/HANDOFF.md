# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.10.3-adreno-dust-dev` · versionCode **25** |
| Latest release | [`v0.10.3-adreno-dust-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.10.3-adreno-dust-dev) |
| APK SHA-256 | `870C19820A8BF6EA9ABCE179EB24E4F80B7D7282DBE4C7B9E0779827CF1CC7C7` |
| Prior tip | [`v0.10.0-filament-house-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.10.0-filament-house-dev) · SHA `3D958C94…` |

## Waves complete (0.8 → 0.10)

| Wave | Landed |
|------|--------|
| 0 | Flags, video default off, version train |
| 1 | Procedural house + placement engine |
| 2 | Vault annex, furniture, idle characters, house/city toggle, budget caps, assistant speech pulse |
| 3 | `HousePerfBudget`, DEVICE-E2E-HOUSE-CHECKLIST (#16 still needs Realme) |
| 4 | **Filament** SceneView `4.15.0` · glTF assets · `HouseFilamentSurface` / `FilamentHouseLighting` / `HouseWorld` · `useFilamentHouse=true` |

**Fallbacks:** procedural `HouseHomeSurface` when `useFilamentHouse=false`; CityCanvas when house toggle off.

**Grok:** [design/GROK-FILAMENT-SHIP.md](design/GROK-FILAMENT-SHIP.md) · [design/GROK-FILAMENT-LIGHTING.md](design/GROK-FILAMENT-LIGHTING.md)

## Now → next

| Now | Next |
|-----|------|
| 0.10.1 fullscreen + TextureSurface fix | Device #16 using `docs/DEVICE-E2E-HOUSE-CHECKLIST.md` (no ADB device this session) |
| Compile + unit PASS | Device #16 using `docs/DEVICE-E2E-HOUSE-CHECKLIST.md` (prerelease waiver OK) |

Session: 2026-07-23.
