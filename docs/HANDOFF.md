# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.6.0-city-3d-dev` · versionCode 17 |
| Latest release | [`v0.6.0-city-3d-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.0-city-3d-dev) |
| APK SHA-256 | `F637ECF048AF7DFBC921F6C074F6EABD6A3CC72C7D046BDD5578B766D9105A2A` |
| Prior audio tip | [`v0.5.1-gemini-audio-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.5.1-gemini-audio-fix-dev) |

## Gemini audio fix (0.5.1)

- **Root cause:** `speechConfig.languageCode` is not valid on `generateContent` TTS → 400 / unavailable
- **Fix:** voice-only `prebuiltVoiceConfig.voiceName`; language via prompt hint + default template preamble
- **Also:** retries, 12 MB response cap, AudioTrack prime-write + larger buffer
- Logcat: `adb logcat -s ForgeCityTTS` → `gemini_audio_ok` then `pcm_play_started backend=…`

## Now → next

| Now | Next |
|-----|------|
| 0.6.0 city-3d published | Device visual check of 3D pass + GEMINI AUDIO TTS |
| Realme E2E (#16) PENDING | Blocks production tags |

## CityRender 3D visual upgrade (0.6.0)

Premium low-poly pass in `ui/cityrender/CityRender.kt` + `ui/CityCanvas.kt`.
Continuous lighting model: new **optional** params `nightFactor`, `timeSeconds`,
`activityPulse` on `drawCityBuilding` (defaults keep old callers working);
`drawGroundPlane(ambientEnabled, nightFactor=0f)`.
Wall gradient shading, roof rim light, softer contact shadow, emissive/flicker
windows, roof night emissive, depth-shaded ground.
Unchanged: prism footprint, `BuildingHitGeometry`, district colors,
`DistrictSilhouette.of`, `RoofStyle` shapes, hit testing, pressed/favorite/night gate.
`testDebugUnitTest` + `lintDebug` + `assembleDebug` green.

Session: 2026-07-21.
