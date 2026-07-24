# SIGN-OFF — forgecity-launcher v0.16.0-assistant-character-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-25 Assistant character home mode |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `08cec4a` |
| Scope | versionCode **35** · `0.16.0-assistant-character-dev` |
| Tag | `v0.16.0-assistant-character-dev` prerelease |
| When (IST) | 2026-07-25 |

## Checklist

- [x] `HomeMode` enum + `AssistantSettingsStore.homeMode` replaces the old `houseHomeEnabled` boolean end-to-end (store, ViewModel, MainActivity, ForgeCityHomeScreen, settings sheet) — no leftover references (`grep houseHomeEnabled` clean except a doc comment)
- [x] `AssistantCharacterScreen` is standalone (own engine/camera/light); does not reuse `HouseFilamentSurface`'s room/orbit/hotspot code
- [x] Resume greeting (`ForgeCityViewModel.onHomeResumed`) calls `AssistantTtsEngine.speakDirect` only — confirmed no `CascadeSpeechOrchestrator`/Gemini/Portal path is reachable from Assistant mode
- [x] New `HumanoidAction.WAVE` is additive to `HouseHumanoidPose.compute`; no existing `when` branch (IDLE/TALK/WALK/SIT) changed; no exhaustive-match test broke
- [x] Streak logic (`AssistantGreetings.computeStreak`/`streakSuffix`) is pure and unit-tested; `AssistantSettingsStore.recordDailyOpen` just persists its result
- [x] test+lint+assemble green: `testDebugUnitTest`, `lintDebug`, `assembleDebug` all PASS, incl. new `HomeModeTest` + `AssistantGreetingsTest`
- [x] APK SHA-256 `7A7E8ADC721C41F50F42A2F0F20140D47AFBE9683837A18057F376F6BF76E113`
- [x] No secrets — diff-scanned changed files for key/token/password patterns; only pre-existing Keystore-backed `apiKey`/`SecretKey` plumbing (untouched, passed through unchanged param lists), no literal credentials
- [x] Docs updated same-turn: `README.md`, `docs/VERIFICATION.md`, `docs/ROADMAP.md`, `docs/HANDOFF.md`
- [ ] Realme #16 physical soak PENDING (waiver for prerelease — user is sideloading and testing directly this time, closing the gap every prior ForgeCity release left open)

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.16.0-assistant-character-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `08cec4a` matches claimed scope: `HomeMode.kt`, `AssistantGreetings.kt`, `ui/assistant/AssistantCharacterScreen.kt` new; `AssistantSettingsStore`, `ForgeCityViewModel`, `MainActivity`, `ForgeCityHomeScreen`, `CityAssistantOverlay`, `HouseHumanoidPose` modified; two new test files. No Gradle dependency changes beyond versionCode/versionName bump.
- Local `dist/forgecity-0.16.0-assistant-character-dev-debug.apk` SHA-256 matches claimed hash.
- `AssistantCharacterTts` lifecycle: lazily constructed via nullable-backed getter (not `by lazy`, which doesn't support `isInitialized` on member properties), shut down in `onCleared()` only if actually constructed — no engine leak, no forced construction for users who never open Assistant mode.
- City and House modes' own render paths are unchanged (`houseMode`/`assistantMode` are mutually exclusive booleans derived from the same `homeMode` enum); City-mode and House-mode behavior is identical to pre-change.

### Conditions

- Prerelease only; Realme #16 physical soak still open — user has the APK and is testing directly, which is the point of this ship.
- ACTIVITY-LOG entry required after push/publish.
