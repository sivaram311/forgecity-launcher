# SIGN-OFF — forgecity-launcher v0.17.0-assistant-character-rigged-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-25 Assistant character rig |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `3008709` |
| Scope | versionCode **37** · `0.17.0-assistant-character-rigged-dev` |
| Tag | `v0.17.0-assistant-character-rigged-dev` prerelease |
| When (IST) | 2026-07-25 |

## Checklist

- [x] New asset-authoring tool (`tools/generate_rigged_character.py`) is additive — does not modify `generate_house_assets.py`/`generate_characters.py`; City/House modes' character rendering (`HouseHumanoidNode`/`HouseHumanoidPose`) is untouched and still used as the fallback
- [x] Joint budget: 7 nodes, well under the `GROK-0.11-HUMANOID.md`-documented ≤18-bone Adreno-710 limit
- [x] Rig offsets and motion formulas ported 1:1 from `HouseHumanoidPose.HIP_Y`/`compute()` — verified by direct comparison against the Kotlin source, not re-derived from scratch
- [x] Generated asset independently structurally validated beyond the script's own self-check: every accessor's byte range checked against its bufferView/blob bounds, every mesh primitive's indices checked against its vertex count, every animation channel's node reference checked valid, every baked rotation quaternion's norm checked ≈ 1.0 at every keyframe of all 3 clips — all passed
- [x] `SceneScope.ModelNode` composable's parameter names (`animationName`, `animationLoop`, `animationSpeed`, `apply`) were not assumed — decompiled from the actual `sceneview-4.15.0.aar` classes (no sources jar exists for this dependency) via `javap -v`, reading the Compose source-information string embedded in the bytecode, before writing Kotlin against it
- [x] Kotlin integration compiles clean (`compileDebugKotlin` run standalone first, before the full suite, specifically to catch API-mismatch risk early)
- [x] Fallback path: `riggedInstance == null` → renders the unchanged, already-working procedural character — verified by reading the diff, not just assumed
- [x] `testDebugUnitTest` / `lintDebug` / `assembleDebug` all PASS
- [x] New `.glb` confirmed actually present in the built APK via `unzip -l app-debug.apk` (not just "the script ran")
- [x] APK SHA-256 `6966693C31D32EAC160AB4E2E59127BAE9A0E9EE4CD209B3BB346013F32C00C3`
- [x] No secrets in diff
- [x] Docs updated same-turn: `README.md`, `docs/VERIFICATION.md`, `docs/ROADMAP.md`, `docs/HANDOFF.md`
- [ ] **Zero on-device or GPU-rendered verification of any kind.** No ADB device, no emulator, no OpenGL/Filament renderer available to this build host. Every check above is static (bytecode/structural/compile-time) — none of it proves the character actually renders, animates smoothly, or performs acceptably on the Realme P2 Pro.

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.17.0-assistant-character-rigged-dev` + GitHub debug APK asset — **strong condition** below.

### Findings (#17)

- Tip `3008709` matches claimed scope exactly: new `tools/generate_rigged_character.py`, new `app/src/main/assets/filament/char_assistant_rigged.glb`, `AssistantCharacterScreen.kt` render-path swap with null-fallback, version bump, docs. No changes to `HouseHumanoidPose.kt`/`HouseHumanoidNode.kt`/City or House mode rendering.
- This is the highest-risk ship in the Assistant-mode line so far by construction (new Filament/glTF asset, this app's worst historical bug category — white-screen, blank-scene, the flicker bug fixed last ship) — and simultaneously the *least* device-verified, since this build host has no way to render anything. The static-verification chain (structural glTF validation + bytecode-confirmed API + clean compile) is real and substantive, but it is not a substitute for seeing it render.
- The fallback path is the single most load-bearing piece of this ship: if the rig is subtly wrong in a way static validation can't catch (e.g. a Filament gltfio parser quirk not exercised by pygltflib's own loader), the user should see the *old* character, not a blank screen — this was verified by reading the actual `if (instance != null) ... else ...` branch, not assumed.

### Conditions

- **Not closed** until the user reports which character they actually saw (rigged vs. fallback) and whether Idle/Talk/Wave read correctly.
- If the rigged asset fails to load or renders incorrectly, next debugging step is to check `ForgeCityTtsDiagnostics`-style logcat output for a Filament/gltfio parse error, not to re-guess at the binary layout blind.
- ACTIVITY-LOG entry required after push/publish.
