# Parallel agent execution — 3D House Wave 0/1

**Plan SoT:** [GROK-3D-HOUSE-LAUNCHER-PLAN.md](GROK-3D-HOUSE-LAUNCHER-PLAN.md)  
**Session:** 2026-07-23 · auto-model subagents only  
**version target:** `0.8.0-3d-house-dev` · versionCode **20**

## Adaptations from Grok (repo truth)

| Grok name | Actual repo |
|-----------|-------------|
| `Media3CityLoop.kt` / `CityVideoPlayer.kt` | `ui/background/CityBackgroundVideo.kt` + Media3 deps in `app/build.gradle.kts` |
| `res/raw/city_loop.mp4` | check `app/src/main/res/raw/` |
| `HouseActivity` | Keep `MainActivity` as HOME; do not split unless needed |
| Missing `.glb` assets | **Procedural Filament/Compose geometry first**; no binary glTF required in Wave 1 |
| Package `render` / `placement` / `character` | Create under `buzz.delena.forgecity.house.*` |

## Wave agents

See parent orchestration; each agent forbidden from editing other agents' owned paths.
