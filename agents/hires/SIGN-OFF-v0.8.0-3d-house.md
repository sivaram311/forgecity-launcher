# SIGN-OFF — forgecity-launcher v0.8.0-3d-house-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 3D House Wave 0/1 |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `54ecbff395437f5517f33b274f5bd3435b41f892` |
| Provider | cursor + auto-model subagents A0–A3 |
| Scope | versionCode **20** · `0.8.0-3d-house-dev` |
| Branch / tag | `main` → `v0.8.0-3d-house-dev` (prerelease debug) |
| When (IST) | 2026-07-23 |

## Checklist

- [x] Grok plan filed (`GROK-3D-HOUSE-LAUNCHER-PLAN.md`)
- [x] HouseFeatureFlags + video default off
- [x] AppPlacementEngine + DistrictRoomMap + unit tests
- [x] HouseHomeSurface procedural Compose + HomeScreen wiring
- [x] AnimationBudget quality tiers
- [x] Assistant/chrome preserved; CityCanvas fallback via flag
- [x] testDebugUnitTest + lintDebug + assembleDebug green
- [x] APK SHA-256 `C14D5E2CCE7F5C29387CB1BC88BD15E5228BADC0219F88D4936B8D6F7F0AAF3E`
- [ ] Filament/glTF + characters (Wave 2 — not this tag)
- [ ] Realme E2E (#16) PENDING (prerelease waiver)
- [x] #18 N/A; no secrets in tip

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.8.0-3d-house-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `54ecbff` message/scope match Wave 0/1: procedural Compose `HouseHomeSurface`, `AppPlacementEngine` + `DistrictRoomMap`, `HouseFeatureFlags` (`use3dHouse=true`, `useCityVideo=false`), `AnimationBudget` HIGH/MEDIUM/LOW tiers, versionCode **20** / `0.8.0-3d-house-dev`; no Filament/glTF deps.
- Local `dist/` APK SHA-256 matches claimed `C14D5E2CCE7F5C29387CB1BC88BD15E5228BADC0219F88D4936B8D6F7F0AAF3E`; `dist/` + `*.apk` gitignored; no secrets/keystore/env in tip.
- Video default-off: compile flag + `AssistantSettingsStore.backgroundVideoEnabled` default `false`; house mode forces `videoOn=false`. CityCanvas fallback intact when `use3dHouse=false`.
- Assistant/chrome overlays, favorites dock, settings sheet remain; only home surface swaps.
- #16 Realme E2E PENDING with explicit prerelease waiver — consistent with prior forgecity debug tags.
- Non-blocking: `HouseHomeSurface` KDoc still says “not AppPlacementEngine” while `demoHouseMarkers` uses the engine; house markers are tap-only (no long-press); Vault UI shares Office cell.

### Conditions

- Prerelease debug only (`--prerelease`); Wave 1 is procedural house, not full Filament realism.
- Do not promote to production while #16 PENDING.
- ACTIVITY-LOG entry required after push/publish.
