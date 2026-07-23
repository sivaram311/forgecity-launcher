# SIGN-OFF — v0.10.0-filament-house-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 Filament house Wave 4 |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip | `36a23b8e0fab2bc19d5134604892d7523ff298ab` |
| Tag | `v0.10.0-filament-house-dev` (prerelease) |
| versionCode | **22** |
| APK | `forgecity-0.10.0-filament-house-dev-debug.apk` |
| SHA-256 | `3D958C94EA50A82C85A0EF4F01BA6B7AF2C1BB6D5ADCB13BD0C5C6371293D9C2` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] SceneView 4.15.0 + Filament house GLB assets
- [x] `HouseFilamentSurface` wired; procedural + CityCanvas fallbacks preserved
- [x] Day/night lighting (`FilamentHouseLighting` / Grok consult)
- [x] Characters + assistant speech pulse; hotspot cubes tappable
- [x] Kotlin 2.3.21 + KSP 2.3.10 (SceneView 4.25 blocked on Kotlin 2.4)
- [x] Unit tests + `compileDebugKotlin` + `assembleDebug` PASS
- [x] Docs #12 (README/ROADMAP/HANDOFF/OPS/VERIFICATION/parallel-exec + Grok notes)
- [x] No secrets; no port/DB/CSS
- [ ] #16 Realme E2E — **PENDING** (prerelease waiver)
- [x] Reviewer GO before push/tag

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.10.0-filament-house-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `36a23b8e0fab2bc19d5134604892d7523ff298ab` (“Ship Filament SceneView house HOME (0.10.0).”) matches claimed SHA; HEAD on `main` (1 ahead of `origin/main`). Staged follow-up: this SIGN-OFF only (no second tip SHA yet).
- Scope: SceneView `4.15.0`; assets `filament/house_shell.glb` + `char_idle.glb`; `HouseFilamentSurface` / `FilamentHouseLighting` / `HouseWorld`; `useFilamentHouse=true` with procedural `HouseHomeSurface` + `CityCanvas` fallbacks; versionCode **22** / `0.10.0-filament-house-dev`; Kotlin **2.3.21** · KSP **2.3.10** (4.25 deferred for Kotlin 2.4).
- Local `app/build/outputs/apk/debug/app-debug.apk` SHA-256 matches claimed `3D958C94EA50A82C85A0EF4F01BA6B7AF2C1BB6D5ADCB13BD0C5C6371293D9C2`.
- Tip diff: no secrets/keystore/env; no port/DB/CSS; `.gitignore` adds `.tmp-sceneview/` only.
- #16 Realme physical soak PENDING with explicit prerelease waiver — consistent with prior forgecity debug tags.

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- Commit this SIGN-OFF before push/tag if not already on tip.
- ACTIVITY-LOG entry required after push/publish.
