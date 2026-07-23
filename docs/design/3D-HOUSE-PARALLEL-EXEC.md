# Parallel agent execution — 3D House Wave 0/1 → 2/3 → 4 (Filament)

**Plan SoT:** [GROK-3D-HOUSE-LAUNCHER-PLAN.md](GROK-3D-HOUSE-LAUNCHER-PLAN.md)  
**Session:** 2026-07-23 · auto-model subagents only  
**version target:** `0.10.0-filament-house-dev` · versionCode **22**

## Adaptations from Grok (repo truth)

| Grok name | Actual repo |
|-----------|-------------|
| `Media3CityLoop.kt` / `CityVideoPlayer.kt` | `ui/background/CityBackgroundVideo.kt` + Media3 deps in `app/build.gradle.kts` |
| `res/raw/city_loop.mp4` | check `app/src/main/res/raw/` |
| `HouseActivity` | Keep `MainActivity` as HOME; do not split unless needed |
| `.glb` assets | `app/src/main/assets/filament/house_shell.glb`, `char_idle.glb` — `tools/generate_house_assets.py` |
| Package `render` / `placement` / `character` | `buzz.delena.forgecity.house.*` / `ui/house/` incl. `HouseFilamentSurface.kt`, `FilamentHouseLighting.kt`, `HouseWorld.kt` |
| Agent-5 `perf` / Agent-7 instrumented | `power/AnimationBudget.kt` + `power/HousePerfBudget.kt`; checklist in `docs/DEVICE-E2E-HOUSE-CHECKLIST.md` |

## Wave 0/1 — landed (tag `v0.8.0-3d-house-dev`)

| Slice | Status | Notes |
|-------|--------|-------|
| A0 FeatureFlags + video off + version 20 | Done | `HouseFeatureFlags`, SettingsStore video default off |
| A1 rooms / `AppPlacementEngine` | Done | District→room + unit tests |
| A2 `HouseHomeSurface` procedural | Done | Compose floor-plan; Filament fallback path kept |
| A3 `AnimationBudget` quality tiers | Done | HIGH/MEDIUM/LOW + policy tests |

## Wave 2 — landed (`v0.9.0-3d-house-characters-dev`)

| Slice | Status | Notes |
|-------|--------|-------|
| Characters + richer rooms + Vault cell | Done | Compose silhouettes; `maxCharacters` / soft-glow gates from budget |
| Integration overlays / dock over house | Done | Assistant chrome preserved; CityCanvas fallback via house toggle |

**Done-when (Wave 2):** Vault visible; idle characters capped by budget; furniture silhouettes; soft lamp glow gated; unit tests green.

## Wave 3 — landed (code/docs; #16 pending)

| Slice | Status | Notes |
|-------|--------|-------|
| `HousePerfBudget` + FPS stub | Done | `power/HousePerfBudget.kt`; records tier decisions; `FpsEstimator` / `StubFpsEstimator`; policy tests |
| Realme #16 house checklist | Done (doc) | [`DEVICE-E2E-HOUSE-CHECKLIST.md`](../DEVICE-E2E-HOUSE-CHECKLIST.md) — physical device required for GO |
| Minimal `@Ignore` androidTest | Done | `HouseInstrumentedSmokeTest` ignored; does not fail CI unit tests |
| Device soak evidence | **Pending** | Needs USB Realme P2 Pro |

**Done-when (Wave 3):** Policy + checklist filed; instrumented smoke ignored/skipped in CI; #16 remains PENDING until physical run.

## Wave 4 — Filament landed (`0.10.0-filament-house-dev`)

| Slice | Status | Notes |
|-------|--------|-------|
| SceneView `4.15.0` + Kotlin/KSP bump | Done | Kotlin `2.3.21` · KSP `2.3.10` |
| glTF assets | Done | `house_shell.glb`, `char_idle.glb` under `assets/filament/` |
| `HouseFilamentSurface` + lighting | Done | `FilamentHouseLighting.kt`, `HouseWorld.kt` |
| `useFilamentHouse=true` | Done | procedural `HouseHomeSurface` fallback if false |
| Grok ship/lighting docs | Done | [GROK-FILAMENT-SHIP.md](GROK-FILAMENT-SHIP.md), [GROK-FILAMENT-LIGHTING.md](GROK-FILAMENT-LIGHTING.md) |
| Tag + APK SHA | **Pending** | `v0.10.0-filament-house-dev` not created yet |
| Realme #16 Filament soak | **Pending** | prerelease waiver OK |

**Done-when (Wave 4):** Filament default path compiles; unit/lint/assemble PASS; device #16 evidence filed.

## Wave agents (ownership reminder)

See parent orchestration; each agent forbidden from editing other agents' owned paths.  
**Forbidden across Wave 2/3/4 agents:** UI redesign drive-by, unplanned version bump.
