# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.11.2-set-dressing-dev` · versionCode **29** |
| Latest release | [`v0.11.2-set-dressing-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.11.2-set-dressing-dev) |
| APK SHA-256 | `79342FD2A70D9EB47BA85956E4465F61E0E554662D51D3CA226BABF20C88B9AF` |
| Prior tip | [`v0.11.0-humanoid-daycycle-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.11.0-humanoid-daycycle-dev) · SHA `E0940890…` |

## Waves complete (0.8 → 0.11)

| Wave | Landed |
|------|--------|
| 0 | Flags, video default off, version train |
| 1 | Procedural house + placement engine |
| 2 | Vault annex, furniture, idle characters, house/city toggle, budget caps, assistant speech pulse |
| 3 | `HousePerfBudget`, DEVICE-E2E-HOUSE-CHECKLIST (#16 still needs Realme) |
| 4 | **Filament** SceneView `4.15.0` · glTF assets · lighting/dust/walls |
| 5 | **0.11** jointed capsule humanoids + day-cycle + window/rim finishing |
| 6 | **0.11.2** droop cables, hero props, sphere dust, ceilings/pictures/AO |

**Fallbacks:** procedural `HouseHomeSurface` when `useFilamentHouse=false`; CityCanvas when house toggle off.

**Grok / gap:** [design/GAP-VS-PRODUCTION-HOUSE.md](design/GAP-VS-PRODUCTION-HOUSE.md) · [design/GROK-0.11-HUMANOID.md](design/GROK-0.11-HUMANOID.md)

## Now → next

| Now | Next |
|-----|------|
| 0.11.2 set dressing (gap 4–6) | 0.12 room patrols / sit loops |
| Compile + unit PASS | Device #16 using `docs/DEVICE-E2E-HOUSE-CHECKLIST.md` (prerelease waiver OK) |

Session: 2026-07-23.
