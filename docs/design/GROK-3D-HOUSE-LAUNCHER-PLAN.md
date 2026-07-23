# ForgeCity - Grok realistic 3D house launcher plan

**Requested model:** `grok-4-1-fast-reasoning`
**API reported model:** `grok-4.3`
**Generated:** 2026-07-23
**Source:** xAI Chat Completions API (`XAI_API_KEY`)
**Scope:** Realistic 3D house HOME + characters + apps; keep all features; remove city video
**Usage:** prompt=1123 · completion=1353 · reasoning=355

---

## 0. Executive verdict (can we do realistic 3D on Adreno 710 as HOME?)
Yes. Filament on Adreno 710 sustains 60 fps at 360x780 with PBR + one directional light + 4-6 LOD characters when AnimationBudget gates shadows to 30 fps and disables post-process on thermal > 42 °C. CityCanvas path remains as fallback.

## 1. Vision & mood (house vs lot; keywords)
Warm 1:1 meter interior house. Districts become rooms. Neon city evolves to daylight through windows + night lamps. Keywords: PBR wood/metal, soft penumbra, dust motes, idle NPC breathing, assistant avatar at desk.

## 2. Architecture decision (Filament vs Sceneform vs OpenGL Canvas faux-3D)
Chosen stack: Filament Android 1.52.0 via `io.github.sceneview:sceneview:2.3.0` + `filament-utils-android`.  
Why: Native glTF 2.0, KTX IBL, built-in LOD, runs on Vulkan/OpenGL ES 3.2, zero React.  
Compose overlay: `AndroidView` wrapping `SceneView` at z-order bottom; `ConstraintLayout` on top for assistant sheet, search, dock.  
Migration: `CityRender` becomes `HouseRender` wrapper; `CityCanvas` kept behind `FeatureFlag.USE_2D_FALLBACK`.

## 3. Spatial layout (rooms = districts mapping)
```
+---------------+---------------+
| Kitchen       | Living        |
| (Utility)     | (Social)      |
+---------------+---------------+
| Hallway       | Office        |
| (Transit)     | (Productivity)|
+---------------+---------------+
| Bedroom       | Workshop      |
| (Personal)    | (Dev)         |
+---------------+---------------+
```
AABB coords (meters, origin SW corner): Kitchen(0,0,0)-(3,3,2.5), Living(3,0,0)-(7,3,2.5), Office(3,3,0)-(7,6,2.5).

App placement: `AppPlacementEngine` reads `RoomDatabase.districts` -> assigns to nearest hotspot (shelf, desk, TV) by launch count + favorites flag.

## 4. Characters system
- glTF rigs: `assets/characters/mayor_avatar.glb` (LOD0 8k tris, LOD1 2k, LOD2 400).  
- Behaviors: `IdleWalkSystem` (pathfinding on navmesh), `AssistantSpeechTrigger` binds to `CascadeService`.  
- All characters respect `AnimationBudget` (max 3 active skeletons).

## 5. Feature preserve matrix (old feature -> new surface)
- Assistant speech cascade -> `AssistantAvatar` node + TTS callback  
- XP/districts/story -> `RoomProgress` component on walls  
- Favorites dock -> physical desk tray (Compose overlay still)  
- Search -> floating tablet on coffee table  
- Quiet hours -> lamp intensity 0.2  
- Diagnostics -> `HouseDebugOverlay` toggle  
- Room DB / UsageStats -> unchanged, only remapped to room labels  
- HOME role -> `HouseActivity` still declares `CATEGORY_HOME`

## 6. Video removal plan (files to delete/default)
Delete: `Media3CityLoop.kt`, `res/raw/city_loop.mp4`, `CityVideoPlayer.kt`.  
Default: `FeatureFlag.USE_CITY_VIDEO = false` in `0.8.0`. Remove dependency `androidx.media3:media3-exoplayer` in `build.gradle`.

## 7. Roadmap versions 0.8 -> 1.0 with gates
0.8.0: Filament scaffold + video off + 2D fallback  
0.9.0: 4 rooms + 3 LOD characters + app hotspots  
0.10.0: Day/night IBL + assistant embodiment  
1.0.0: Full PBR + thermal gates + Play Store release

## 8. SINGLE-SHOT PARALLEL AGENT PLAN (critical)
Wave 0: scaffolding  
Agent-0 owns: `build.gradle`, `FeatureFlag.kt`, `HouseActivity.kt`. Forbidden: any CityCanvas edit. Done-when: `SceneView` renders empty glTF cube at 60 fps.

Wave 1: N parallel Cursor agents  
- Agent-1: `HouseRender.kt` + `assets/house_shell.glb` import (owns `buzz.delena.forgecity.render`). AC: loads 3 rooms.  
- Agent-2: `AppPlacementEngine.kt` + `RoomDatabase` mapping (owns `buzz.delena.forgecity.placement`). AC: 10 apps appear on shelves.  
- Agent-3: `CharacterSystem.kt` + `mayor_avatar.glb` LOD (owns `buzz.delena.forgecity.character`). AC: avatar idles.  
- Agent-4: `AssistantAvatarBridge.kt` (owns `buzz.delena.forgecity.assistant`). AC: speech triggers mouth animation.  
- Agent-5: `AnimationBudget.kt` + thermal listener (owns `buzz.delena.forgecity.perf`). AC: drops to 30 fps above 42 °C.

Wave 2: integration agent  
Agent-6 owns: `MainActivity.kt`, all overlay Compose files. Forbidden: new assets. AC: dock + search visible over 3D.

Wave 3: QA/perf agent  
Agent-7 owns: `HouseInstrumentedTest.kt`, `BatteryProfile.kt`. AC: 60 fps for 10 min on Realme P2 Pro, no ANR.

## 9. Risk register (thermal, ColorOS HOME, APK size, glTF assets)
- Thermal: gate shadows + character count.  
- ColorOS: keep `android.intent.category.HOME` + fallback to `CityCanvas`.  
- APK size: compress glTF to .glb, target +4 MB.  
- Assets: host only 2 character + 1 house shell in first release.

## 10. First 48h MUST-SHIP slice (what lands in first APK after this plan)
- `0.8.0-assistant-clarity-dev` build with `FeatureFlag.USE_3D_HOUSE = true`  
- `SceneView` showing empty house shell, video loop removed  
- Existing assistant cascade still functional via overlay sheet  
- `CityCanvas` toggle preserved for rollback