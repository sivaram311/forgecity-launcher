# SIGN-OFF — v0.4.7 PCM playback fix

| Field | Value |
|-------|-------|
| Scope | `0.4.7-pcm-playback-fix-dev` / versionCode 14 |
| Reviewer | GO (source + unit/lint/assemble) |
| Date | 2026-07-20 |

## Checklist

- [x] Streamed `AudioTrack` (MODE_STREAM) instead of fragile MODE_STATIC
- [x] MediaPlayer + temp WAV fallback
- [x] USAGE_MEDIA + richer `pcm_*` diagnostics (no bodies/keys)
- [x] Unit tests for PCM/WAV normalizer
- [x] `testDebugUnitTest` / `lintDebug` / `assembleDebug` green
- [ ] Realme P2 Pro E2E (#16) — pending

## Notes

Fixes “Gemini audio OK; playback UNAVAILABLE” when API succeeds but static AudioTrack init/write fails on device.
