# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md` (Reviewer GO before push, Realme E2E before production tags, activity log)

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.5.0-ui-polish-dev` · versionCode 15 |
| Latest release | [`v0.5.0-ui-polish-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.5.0-ui-polish-dev) (prerelease, debug) |
| APK SHA-256 | `BB8FECCF655928DC5EC5D28665890CE3FC63F7422028F9E3A6327D2C062C3CFA` |
| Prior speech tip | [`v0.4.7-pcm-playback-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.7-pcm-playback-fix-dev) |

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
| 0.5.0 published (debug prerelease) | Confirm UI + video + speech on Realme P2 Pro |
| Realme E2E (#16) still PENDING | Blocks annotated **production** tags |

## Build

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Session handoff date: 2026-07-21.
