# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.5.1-gemini-audio-fix-dev` · versionCode 16 |
| Latest release | [`v0.5.1-gemini-audio-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.5.1-gemini-audio-fix-dev) |
| APK SHA-256 | `05D21575B597856A01989F8B15E2BD1804497294A4ECE296C188F8AFC1D52365` |
| Prior UI tip | [`v0.5.0-ui-polish-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.5.0-ui-polish-dev) |

## Gemini audio fix (0.5.1)

- **Root cause:** `speechConfig.languageCode` is not valid on `generateContent` TTS → 400 / unavailable
- **Fix:** voice-only `prebuiltVoiceConfig.voiceName`; language via prompt hint + default template preamble
- **Also:** retries, 12 MB response cap, AudioTrack prime-write + larger buffer
- Logcat: `adb logcat -s ForgeCityTTS` → `gemini_audio_ok` then `pcm_play_started backend=…`

## Now → next

| Now | Next |
|-----|------|
| 0.5.1 published | Device confirm GEMINI AUDIO TEST TTS |
| Realme E2E (#16) PENDING | Blocks production tags |

Session: 2026-07-21.
