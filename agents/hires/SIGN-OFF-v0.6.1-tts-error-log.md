# SIGN-OFF — forgecity-launcher v0.6.1-tts-error-log-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-22 in-app TTS diagnostics / error log |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `74874c1ab27eabb0957d5ee32b2d7860ca803a08` |
| Provider | cursor |
| Scope | versionCode **18** · `0.6.1-tts-error-log-dev` |
| Branch / tag | `main` → `v0.6.1-tts-error-log-dev` (prerelease debug) |
| When (IST) | 2026-07-22 |

## Checklist

- [x] Append-only in-app diagnostics ring buffer (`ForgeCityTtsDiagnostics`)
- [x] Assistant settings UI: monospace log + COPY LOG + CLEAR
- [x] Privacy: no keys / notification bodies / rewrite text in log lines
- [x] Unit tests for ring buffer + uiStatus truncate
- [x] Docs updated same turn (#12): README, HANDOFF, OPS, VERIFICATION, GEMINI-SPEECH-CASCADE-SPEC
- [x] versionCode 17→18, versionName `0.6.1-tts-error-log-dev`
- [x] `testDebugUnitTest` + `lintDebug` + `assembleDebug` green
- [x] APK local SHA-256 `BE2F45E5EF46F7CD11F4B3CBB0A03A3CD0DA49E8889E7AA0A054699600568383`
- [ ] Realme P2 Pro DEV E2E (#16) — **PENDING** (prerelease waiver; same as 0.4.x/0.5.x/0.6.0; not production)
- [x] #18 N/A (no CSS login surface)
- [x] Fleet splits N/A (Android launcher only)
- [x] No secrets/keys/tokens in tip diff; no tracked apk/aab/jks/keystore/env/local.properties

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.6.1-tts-error-log-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `74874c1` scoped to diagnostics ring buffer + settings UI wiring + version bump + docs/tests; matches stated feature.
- Privacy invariants hold: `ForgeCityTtsDiagnostics` documents no keys/bodies/titles/rewrite text; call sites log safe metadata (mode, package, host, result enums); `uiStatus` truncates to 160 chars and callers pass scrubbed status strings only.
- Docs (#12) updated: README, HANDOFF, OPS, VERIFICATION, GEMINI-SPEECH-CASCADE-SPEC reflect 0.6.1 + SHA-256.
- #16 Realme E2E PENDING with explicit prerelease waiver — consistent with prior forgecity debug tags (0.4.x–0.6.0).
- #18 N/A; fleet splits N/A.

### Conditions

- Prerelease only (`--prerelease`); debug-signed DEV build.
- Do not promote to production tag while #16 is PENDING.
- ACTIVITY-LOG entry required after push/publish.
