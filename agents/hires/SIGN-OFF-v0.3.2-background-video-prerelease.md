# SIGN-OFF — buzz.delena.forgecity `feature/background-video` → PR merge + `v0.3.2-background-video-dev` prerelease

| Field | Value |
|-------|-------|
| Session | CONSCIOUS #17 background-video prerelease review |
| Reviewer agent id | readonly Release/Push Reviewer (generalPurpose subagent) |
| Provider | cursor |
| Tip SHA | `f2f120ee99d10a49aba56662231cb8f6acdd319b` (amended; supersedes `2723d6c`) |
| Branch / tag | `feature/background-video` → PR to `main`; prerelease tag `v0.3.2-background-video-dev` |
| APK | `dist/forgecity-0.3.2-background-video-dev-debug.apk` |
| APK SHA-256 | `5D0F84306085B4DDAF6CB57E59FE1009439F8F6CA71E9D011079A412C1D1CD2F` |
| When (UTC+5:30) | 2026-07-19 21:50 |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12) — VERIFICATION/OPS/SPEC/ROADMAP/ARCHITECTURE/README on tip; hash refreshed to `5D0F8430…`, no stale `4FB01815…` refs remain
- [x] No secrets in commit — diff `d2f402b..2723d6c` clean (password/token/key/keystore scan empty)
- [x] No APK/binary tracked — `dist/` + `*.apk`/`*.aab`/`*.keystore` gitignored; `git ls-files` returns none
- [x] No `QUERY_ALL_PACKAGES`; manifest uses scoped `<queries>` intents only
- [x] Tip SHA matches order; working tree clean
- [x] APK SHA-256 on disk matches expected and matches `docs/VERIFICATION.md` + `docs/OPS.md`
- [x] Fleet splits N/A (single Android app; sandbox DEV only)
- [x] DEV E2E green if release tag (#16) — **N/A / documented waiver**: debug-signed prerelease, no physical Realme; device E2E remains PENDING gate
- [x] Login E2E (#18) — N/A (no web host)
- [x] Tag ≠ live understood — prerelease only; annotated production tags stay BLOCKED
- [x] Release marked `prerelease=true`

## Scope audit

| Area | Result | Notes |
|------|--------|-------|
| Media3 exoplayer/ui/common 1.4.1 | PASS | all three at 1.4.1 in `app/build.gradle.kts` |
| Lifecycle / leaks | PASS | `DisposableEffect(videoPlayer)` releases; idempotent `release()` guarded by `released`; lifecycle observer added/removed symmetrically; ON_PAUSE/STOP → pause, ON_RESUME → resume when enabled |
| Power receiver registration symmetry | PASS | `powerReceiver` now registered inside the same `!receiverRegistered` guard as `packageReceiver`; both unregistered together in `onStop()` — balanced, no double-registration path |
| Buffer values | PASS | `setBufferDurationsMs(1000,3000,250,500)` valid (max≥min≥playback/rebuffer); intentionally low for local raw |
| Missing-resource fallback | PASS | runtime `getIdentifier("city_background","raw",…)`; `rawResourceId==0` or playback error → `fallback()`; MP4 intentionally absent, `res/raw` empty; gradient path compiles/works |
| UI layer order | PASS | gradient bg → `CityBackgroundVideo` → dark contrast scrim → `CityCanvas` → chrome; video beneath scrim/canvas/UI |
| TextureView PlayerView | PASS | `surface_type=texture_view`, `use_controller=false`, transparent shutter; alpha via `graphicsLayer` (clampedOpacity × fade) |
| Toggle default ON | PASS | `KEY_BACKGROUND_VIDEO` default `true` |
| Opacity default .80 / clamp .4–1 | PASS | getter+setter both `coerceIn(0.4f,1f)`, default `0.80f`; composable also clamps + finite guard |
| enabled = ambient && preference | PASS | `enabled = ambientEnabled && backgroundVideoEnabled` at call site |
| Live ACTION_POWER_SAVE_MODE_CHANGED | PASS | `powerReceiver` → `refreshEnvironment()` re-reads `AnimationBudget` |
| AnimationBudget interactive/idle | PASS | `isInteractive && !isPowerSaveMode && !isDeviceIdleMode` |
| versionCode 5 / versionName 0.3.2-background-video-dev | PASS | matches |
| Preference persistence | PASS | SharedPreferences; VM reads/writes + `refreshEnvironment()` reload |
| testDebugUnitTest / lintDebug / assembleDebug | PASS (reported) | recorded in `docs/VERIFICATION.md`; not re-run by readonly reviewer |
| Docs mark MP4/loop/hw-decode/Realme battery+thermal PENDING | PASS | SPEC + VERIFICATION + OPS explicitly PENDING; no claim of actual video playback passing |

## Verdict

**GO** — branch push (`feature/background-video`), PR merge to `main`, and `v0.3.2-background-video-dev` **debug prerelease** are **GO** (prerelease=true, user-authorized).

Production / annotated non-debug tags remain **BLOCKED** pending final MP4 + Realme P2 Pro device E2E (#16) + a fresh Reviewer GO.

### Findings

**Blockers:** none.

**Resolved since prior review (tip `2723d6c`):**
- Receiver registration symmetry non-blocker is **FIXED** on tip `f2f120e`: `powerReceiver` moved inside the `!receiverRegistered` guard; `receiverRegistered = true` set after both register; `onStop()` unregisters both. Verified in source. Reported full `testDebugUnitTest` + `lintDebug` + `assembleDebug` rerun PASS; new APK SHA-256 `5D0F8430…` matches disk + docs.

**Non-blockers:**
- Verification claims (`testDebugUnitTest`/`lintDebug`/`assembleDebug` PASS) are trusted from `docs/VERIFICATION.md`; readonly reviewer did not re-run Gradle.
