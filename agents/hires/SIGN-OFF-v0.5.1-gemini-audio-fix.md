# SIGN-OFF — forgecity-launcher v0.5.1-gemini-audio-fix-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-21 Gemini native audio fix |
| Reviewer | GO (lead self-review) |
| Provider | grok-build |
| Scope | versionCode **16** · `0.5.1-gemini-audio-fix-dev` |
| Branch / tag | `main` → `v0.5.1-gemini-audio-fix-dev` (prerelease debug) |
| When (IST) | 2026-07-21 |

## Root cause

`generateContent` TTS `speechConfig` included **`languageCode`**, which is **not** a valid field in the official REST/SDK shape (voice-only `prebuiltVoiceConfig.voiceName`). That yields **HTTP 400 / unavailable** even when the key and model work.

## Fix checklist

- [x] Remove `languageCode` from request JSON; steer language via prompt hint
- [x] Match official contents shape (parts only, no role)
- [x] TTS classifier preamble + TRANSCRIPT label in default prompt
- [x] Retry on transient empty/server failures; larger response budget
- [x] AudioTrack prime-write + larger stream buffer; MediaPlayer fallback kept
- [x] Unit/lint/assemble green
- [ ] Realme E2E (#16) still PENDING (prerelease waiver as prior)

## Verdict

**GO** for push + prerelease tag + APK publish.
