# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md` (Reviewer GO before push, Realme E2E before production tags, activity log)

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.5.0-ui-polish-dev` · versionCode 15 |
| Prior tip | `0.4.7-pcm-playback-fix-dev` (speech/PCM still current) |
| Latest speech release | [`v0.4.7-pcm-playback-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.7-pcm-playback-fix-dev) |
| UI polish | **0.5.0 local** — city-first chrome, building craft, video scrims, motion/dock |

## Read first

| Doc | Why |
|-----|-----|
| [UI-POLISH-IMPL-BRIEF.md](./UI-POLISH-IMPL-BRIEF.md) | Slices A–D scope for 0.5.0 |
| [ROADMAP.md](./ROADMAP.md) | Phases |
| [BACKGROUND-VIDEO-SPEC.md](./BACKGROUND-VIDEO-SPEC.md) | Video contract |
| [OPS.md](./OPS.md) | Device setup |
| [VERIFICATION.md](./VERIFICATION.md) | Evidence |

## UI architecture (0.5.0)

**Home surfaces**

- **HOME:** day/night sky + optional video (upper fade) + isometric city + chapter pill + compact resources + favorites dock
- **LAB:** Assistant settings in **modal sheet** (menu → Assistant settings), not permanent scroll
- **Chrome menu:** single overflow (UI / ASSIST / SEARCH / DOCK) instead of four text chips

**City render**

- `ui/cityrender/CityRender.kt` — ground plane, shaded prisms, windows, district roofs (`DistrictSilhouette` / `RoofStyle`), icon badges with LOD, gold favorite pin, level-up burst
- `CityCanvas` — pan/zoom + **inertia**, search **focus fly-in**, haptics on pin, uses CityRender

**Video**

- No full-screen mud scrim; gradient fade mid→bottom + local top/bottom chrome scrims + soft vignette
- Existing procedural `res/raw/city_background.mp4` kept (no regen required)

**Defaults (key absent)**

- Launcher chrome **on**, dock **on**, search **off**, assistant settings sheet **off**

## Speech (unchanged from 0.4.7)

Modes: OFF → DIRECT → PORTAL → GEMINI AUDIO → CASCADE. PCM MODE_STREAM + MediaPlayer fallback.

## Now → next

| Now | Next |
|-----|------|
| 0.5.0 UI polish implemented | On-device visual GO + Realme E2E #16 still PENDING |
| Speech still 0.4.7 tip on GitHub until 0.5.0 published | Package APK + prerelease after Reviewer GO |

## Build

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Session handoff date: 2026-07-21.
