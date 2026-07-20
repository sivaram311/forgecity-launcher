# SIGN-OFF — v0.4.6 Gemini native audio

| Field | Value |
|-------|-------|
| Scope | `0.4.6-gemini-native-audio-dev` / versionCode 13 |
| Reviewer | GO (source + unit/lint/assemble) |
| Date | 2026-07-20 |

## Checklist

- [x] Gemini TTS client (`responseModalities: AUDIO`) + PCM `AudioTrack` playback
- [x] CASCADE: Gemini audio → Portal → DIRECT; GEMINI mode fail-closed audio
- [x] Settings: TTS model / voice / `ta-IN` + audio prompt template
- [x] Text-model prefs migrate to `gemini-3.1-flash-tts-preview`
- [x] `testDebugUnitTest` / `lintDebug` / `assembleDebug` green
- [ ] Realme P2 Pro E2E (#16) — still pending for production tags

## Notes

Debug prerelease only. Notification bodies / PCM / API keys must not appear in Logcat.
