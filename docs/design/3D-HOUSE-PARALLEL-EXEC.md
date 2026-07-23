# Parallel agent execution — 3D House Wave 0/1 → 2/3

**Plan SoT:** [GROK-3D-HOUSE-LAUNCHER-PLAN.md](GROK-3D-HOUSE-LAUNCHER-PLAN.md)  
**Session:** 2026-07-23 · auto-model subagents only  
**version target:** `0.8.0-3d-house-dev` · versionCode **20** (no bump in Wave 2/3)

## Adaptations from Grok (repo truth)

| Grok name | Actual repo |
|-----------|-------------|
| `Media3CityLoop.kt` / `CityVideoPlayer.kt` | `ui/background/CityBackgroundVideo.kt` + Media3 deps in `app/build.gradle.kts` |
| `res/raw/city_loop.mp4` | check `app/src/main/res/raw/` |
| `HouseActivity` | Keep `MainActivity` as HOME; do not split unless needed |
| Missing `.glb` assets | **Procedural Compose geometry first**; Filament/glTF deferred |
| Package `render` / `placement` / `character` | Create under `buzz.delena.forgecity.house.*` / `ui/house/` |
| Agent-5 `perf` / Agent-7 instrumented | `power/AnimationBudget.kt` + `power/HousePerfBudget.kt`; checklist in `docs/DEVICE-E2E-HOUSE-CHECKLIST.md` |

## Wave 0/1 — landed (tag `v0.8.0-3d-house-dev`)

| Slice | Status | Notes |
|-------|--------|-------|
| A0 FeatureFlags + video off + version 20 | Done | `HouseFeatureFlags`, SettingsStore video default off |
| A1 rooms / `AppPlacementEngine` | Done | District→room + unit tests |
| A2 `HouseHomeSurface` procedural | Done | Compose floor-plan; no Filament |
| A3 `AnimationBudget` quality tiers | Done | HIGH/MEDIUM/LOW + policy tests |

## Wave 2 — in progress (2026-07-23)

| Slice | Owner | Status | Notes |
|-------|-------|--------|-------|
| Characters + richer rooms + Vault cell | W2-A | **In progress** | Compose silhouettes; `maxCharacters` / soft-glow gates from budget; **no Filament** |
| Integration overlays / dock over house | W2-B (if spawned) | **In progress** | Preserve assistant chrome; CityCanvas fallback via flag |

**Done-when (Wave 2):** Vault visible; idle characters capped by budget; furniture silhouettes; soft lamp glow gated; unit tests green. Filament still **out of scope**.

## Wave 3 — in progress (2026-07-23)

| Slice | Owner | Status | Notes |
|-------|-------|--------|-------|
| `HousePerfBudget` + FPS stub | W3 | **Landed (code)** | `power/HousePerfBudget.kt`; records tier decisions; `FpsEstimator` / `StubFpsEstimator`; policy tests |
| Realme #16 house checklist | W3 | **Landed (doc)** | [`DEVICE-E2E-HOUSE-CHECKLIST.md`](../DEVICE-E2E-HOUSE-CHECKLIST.md) — physical device required for GO |
| Minimal `@Ignore` androidTest | W3 | **Landed** | `HouseInstrumentedSmokeTest` ignored; does not fail CI unit tests |
| Device soak evidence | Lab / F | **Pending** | Needs USB Realme P2 Pro |

**Done-when (Wave 3):** Policy + checklist filed; instrumented smoke ignored/skipped in CI; #16 remains PENDING until physical run.

## Wave agents (ownership reminder)

See parent orchestration; each agent forbidden from editing other agents' owned paths.  
**Forbidden across Wave 2/3 agents:** UI redesign drive-by, version bump, Filament deps.
