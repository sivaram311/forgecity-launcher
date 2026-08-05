# SIGN-OFF — forgecity-launcher v0.18.1-foundry-rename-dev

| Field | Value |
|-------|-------|
| Session | claude-code, this session |
| Reviewer agent id | `cursor-agent -p --model auto --mode ask --trust` (readonly) |
| Provider | cursor |
| Tip SHA | `81005a8` |
| Branch / tag | `main` + prerelease tag `v0.18.1-foundry-rename-dev` |
| When (IST) | 2026-08-05 |

## Commits covered

| SHA | Summary |
|-----|---------|
| `a49fcec` | Rename display name ForgeCity → Foundry (strings, chapter chip, TEST TTS copy, docs note) |
| `81005a8` | Ship v0.18.1-foundry-rename-dev (versionCode 39) + doc pointers/SHA |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12) — README, `docs/HANDOFF.md`, `docs/VERIFICATION.md` all updated in these two commits
- [x] No secrets in commit — confirmed by readonly reviewer against `origin/main..HEAD`; only public URLs and SHA-256 digests present
- [x] Fleet splits OK — N/A, single sandbox Android app, no shared infra touched
- [x] DEV E2E green if this push includes a release tag (#16) — **N/A/waived**: string-only display-name rename does not touch rendering/Filament/network code; same established waiver pattern this repo has used since v0.1.0 (documented PENDING in VERIFICATION.md, not a silent skip)
- [x] Login E2E used DEV public domain when host exists (#18) — N/A, no login surface touched
- [x] Tag ≠ live understood — N/A, debug prerelease only, matches established pattern
- [x] `applicationId buzz.delena.forgecity`, repo name, package/class structure confirmed unchanged
- [x] `versionCode`/`versionName` bumped to **39** / `0.18.1-foundry-rename-dev` in `app/build.gradle.kts`
- [x] `testDebugUnitTest` / `lintDebug` / `assembleDebug` PASS
- [x] APK badging verified via `aapt dump badging`: `package buzz.delena.forgecity versionCode 39`, `application-label:'Foundry'`
- [x] APK SHA-256 `3D4F0082A92205A1D9E932C56700CBD9CB5313FFF5C3FE7C36A1ECE53F6B7802` · `dist/forgecity-0.18.1-foundry-rename-dev-debug.apk`
- [x] `.tmp-aar/` untracked — not staged for push

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.18.1-foundry-rename-dev` + GitHub debug APK asset.

### Findings

- Independent `cursor-agent --mode ask` review of the full `origin/main..HEAD` diff (both commits, not just the tip) found no secrets, no scope creep beyond the claimed display-name rename + version ship, and confirmed `applicationId`/repo/package identifiers untouched.
- Diff is exactly: `app_name`/`set_as_home` strings, one UI chip string, TEST TTS copy (4 occurrences total across `AssistantSpeechMode.kt` + `SpeechModeTestRunner.kt`), `versionCode`/`versionName`, and matching doc updates. No logic, manifest, dependency, or networking changes.
- Deliberately left alone and confirmed present unchanged: log tag `ForgeCityTTS`, HTTP header `X-ForgeCity-Key` (live Agent Portal server contract).
- Same Realme #16 on-device waiver pattern this repo has used for every prerelease since v0.1.0 — documented as PENDING, not silently skipped; a string-only rename doesn't exercise the historically bug-prone Filament/glTF/WebView paths, so this is lower-risk than most prior ships in this history.

### Conditions

- None blocking. ACTIVITY-LOG entry required after push/publish (CONSCIOUS #12).
