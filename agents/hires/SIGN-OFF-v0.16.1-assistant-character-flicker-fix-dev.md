# SIGN-OFF — forgecity-launcher v0.16.1-assistant-character-flicker-fix-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-25 Assistant character hotfix |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `1a9f85e` |
| Scope | versionCode **36** · `0.16.1-assistant-character-flicker-fix-dev` |
| Tag | `v0.16.1-assistant-character-flicker-fix-dev` prerelease |
| When (IST) | 2026-07-25 |

## Checklist

- [x] User-reported bug (multiple avatars + flickering in Assistant mode on real device) addressed with a targeted, single-cause fix
- [x] Fix scoped to `AssistantCharacterScreen.kt` only — `isOpaque = false` → `true` on both `createEnvironment` and `SceneView`, backdrop moved from Compose blend-through to Filament skybox color; no other file touched except version bump + docs
- [x] No logic/behavior change to greeting, streak, tap-reaction, or mode-switching code from 0.16.0 — this is a rendering-config-only fix
- [x] `testDebugUnitTest` / `lintDebug` / `assembleDebug` all PASS (unchanged test count — this bug class isn't unit-testable, it's a device GPU compositing issue)
- [x] APK SHA-256 `E39AF6AEC4CB05545BF357EF89E765A895936C4B403A6391C0EAA07CD89D969D`
- [x] No secrets in diff
- [x] Docs updated same-turn: `README.md`, `docs/VERIFICATION.md`, `docs/HANDOFF.md`
- [ ] Fix is **diagnosed, not device-confirmed** by the build host — no ADB device attached here. Root-cause reasoning: `isOpaque=false` diverges from the only proven pattern in this codebase (`HouseFilamentSurface` always uses `isOpaque=true`) and matches this repo's known Adreno TextureSurface compositing bug history (`docs/design/GROK-WHITE-SCREEN.md`, 0.10.1, 0.10.4). User must confirm on the real Realme P2 Pro.

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.16.1-assistant-character-flicker-fix-dev` + GitHub debug APK asset — **conditional** on the user re-testing before this is considered closed.

### Findings (#17)

- Tip `1a9f85e` matches claimed scope exactly: `isOpaque` flip in `AssistantCharacterScreen.kt` (2 sites) + skybox-color `SideEffect` replacing the Compose gradient blend-through; `app/build.gradle.kts` version bump; docs.
- Local `dist/forgecity-0.16.1-assistant-character-flicker-fix-dev-debug.apk` SHA-256 matches claimed hash.
- This is the repo's fastest possible honest turnaround on a real device bug report: single hypothesis, single targeted change, no speculative extra edits bundled in.

### Conditions

- **Not** closed until the user confirms the flicker/duplicate-avatar symptom is gone on the Realme P2 Pro. If it persists, the next hypothesis is the `cameraManipulator`'s `orbitHomePosition = cameraNode.worldPosition` argument or the `WAVE`-triggered pose transition, not yet ruled out.
- Character visual realism ("not realistic" user feedback) is explicitly **out of scope** for this hotfix — tracked separately, not yet actioned.
- ACTIVITY-LOG entry required after push/publish.
