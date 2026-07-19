# SIGN-OFF — v0.4.1 TTS diagnostics debug prerelease

| Field | Value |
|-------|-------|
| Session | CONSCIOUS #17 |
| Reviewer | `00ad38e9-9c9c-4101-bf68-152dbe838531` |
| Scope | `v0.4.1-tts-diagnostics-dev` / versionCode 8 |
| Date | 2026-07-20 |

## Checks

- [x] OFF / DIRECT / PORTAL Tamil manual TTS test paths
- [x] Portal test sends fixed synthetic text only
- [x] API key visible by explicit product request; Android-Keystore encryption at rest preserved
- [x] `ForgeCityTTS` diagnostics omit key, notification title/body, and Tamil response text
- [x] HTTPS-only endpoint validation preserved
- [x] No embedded secrets
- [x] Unit tests, lint, and debug build pass
- [x] APK hash matches docs and sidecar
- [x] Realme physical E2E remains PENDING; debug prerelease only

## Verdict

**GO** — commit, push, and publish the debug prerelease. No production release claim.
