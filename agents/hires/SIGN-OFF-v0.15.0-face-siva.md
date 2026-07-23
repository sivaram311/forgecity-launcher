# SIGN-OFF — forgecity-launcher v0.15.0-face-siva-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 shared siva.png face card (0.15.0) |
| Reviewer agent id | Release / Push Reviewer (readonly) · Composer subagent |
| Provider | cursor |
| Tip SHA | `8395622ed44408f36de652266d2eea5755157301` |
| Branch / tag | `main` (ahead 1 of `origin/main`) · claimed prerelease `v0.15.0-face-siva-dev` (**not present locally yet**) |
| versionCode | **33** |
| versionName | `0.15.0-face-siva-dev` |
| APK | `forgecity-0.15.0-face-siva-dev-debug.apk` (~44.2 MB / 46370974 bytes) |
| SHA-256 | `E207352E29F1B8048A086F0ACEF2D051E7415BC41277DAC71216EC63FA28F244` |
| When (UTC+5:30) | 2026-07-23 23:29+ IST tip; review ~23:29 IST |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12): README, HANDOFF
- [x] No secrets in commit (diff scan clean; no keystore/env/token blobs)
- [x] Fleet splits OK — N/A (single forgecity Android app; no classic/css-next split)
- [ ] DEV E2E green if this push includes a release tag (#16) — **waived**: `adb` unavailable / no Realme device (same as prior forgecity debug tags)
- [x] Login E2E (#18) — N/A (no CSS DEV host login surface for this APK ship)
- [x] Tag ≠ live understood — prerelease `-dev` debug only; matrix not falsely bumped
- [x] versionCode **33** / versionName match `app/build.gradle.kts` + docs
- [x] APK SHA-256 matches claimed (local hash verified)
- [x] Unit test `HouseFaceAssetsTest` present (SHARED_FACE path assert)
- [x] Tip SHA is 0.15.0 commit (`Ship shared siva.png face card on all humanoids (0.15.0).`)
- [x] `.tmp-aar` **not** committed (untracked `?? .tmp-aar/` only; `.gitignore` line 23)

## Verdict

**GO**

### Findings

- Tip `8395622` is the sole unpushed commit on `main`; message and file set match face-card scope.
- Scope verified in tip:
  - `app/src/main/assets/faces/siva.png` — **256×256**, 67086 bytes
  - `HouseFaceAssets.SHARED_FACE` = `"faces/siva.png"`
  - `HouseFilamentSurface`: one shared `MaterialInstance` (`sharedFaceMat`) from `ImageTexture` + passed as `faceMaterial`
  - `HouseHumanoidNode`: face card `CubeNode` (0.20×0.20×0.012) on head; fallback local texture if shared mat null
  - Docs: README (vc33 + SHA expect line), HANDOFF
  - Test: `HouseFaceAssetsTest`
- Local APK SHA-256 **matches** claimed `E207352E29F1B8048A086F0ACEF2D051E7415BC41277DAC71216EC63FA28F244`.
- No secrets in commit; `.tmp-aar/` remains untracked (do not `git add`).
- Local tag `v0.15.0-face-siva-dev` **absent** — expected until Lead annotates after this GO.
- Non-blocking: README/HANDOFF already link GitHub release URL for the tag (doc-ahead-of-publish drift until push/tag/release).
- #16 Realme soak **PENDING** — prerelease waiver OK per job brief / prior forgecity debug tags (`adb` not on PATH).

### Conditions

- Prerelease debug only; do **not** promote to production while #16 PENDING.
- After GO: Lead may push `main`, create annotated tag `v0.15.0-face-siva-dev` at `8395622`, publish GitHub prerelease + APK asset.
- Do **not** commit `.tmp-aar/`.
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push, tag, or commit (SIGN-OFF write only).

### Blockers

None for prerelease push/tag under documented #16 waiver.
