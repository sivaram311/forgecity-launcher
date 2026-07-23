# SIGN-OFF — forgecity-launcher v0.14.0-ibl-fresnel-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 gap #8 IBL + fresnel stand-ins |
| Reviewer agent id | Release / Push Reviewer (readonly) · Composer subagent |
| Provider | cursor |
| Tip SHA | `3fe2c2639b83eb0d10400ec4280023310e797dea` |
| Branch / tag | `main` (ahead 1 of `origin/main`) · claimed prerelease `v0.14.0-ibl-fresnel-dev` (**not present locally yet**) |
| versionCode | **32** |
| versionName | `0.14.0-ibl-fresnel-dev` |
| APK | `forgecity-0.14.0-ibl-fresnel-dev-debug.apk` (~44.2 MB) |
| SHA-256 | `534386BC68F1A77381E8ACFE17B8BF8EB98D154F4FDDD8FFCEBD28C2C276C4D1` |
| When (UTC+5:30) | 2026-07-23 22:03+ IST tip; review ~22:05 IST |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12): README, HANDOFF, `GROK-0.14-IBL-FRESNEL.md`, GAP backlog #8 **0.14.0 LANDED**
- [x] No secrets in commit (diff scan clean; no keystore/env/token blobs)
- [x] Fleet splits OK — N/A (single forgecity Android app; no classic/css-next split)
- [ ] DEV E2E green if this push includes a release tag (#16) — **waived**: `adb` unavailable / no Realme device (same as prior forgecity debug tags)
- [x] Login E2E (#18) — N/A (no CSS DEV host login surface for this APK ship)
- [x] Tag ≠ live understood — prerelease `-dev` debug only; matrix not falsely bumped
- [x] versionCode **32** / versionName match `app/build.gradle.kts` + docs
- [x] APK SHA-256 matches claimed (local hash verified)
- [x] Unit tests exist for IBL caps (`FilamentHouseIblTest` + lighting `iblIntensity` = 1800); `:app:testDebugUnitTest` for those classes **PASS**
- [x] Tip SHA is 0.14.0 commit (`Ship Adreno-safe HDR IBL and fresnel stand-ins (0.14.0).`)
- [x] `.tmp-aar` **not** committed (untracked `?? .tmp-aar/` only)

## Verdict

**GO**

### Findings

- Tip `3fe2c26` is the sole unpushed commit on `main`; message and file set match gap #8 scope.
- Scope verified in tip:
  - `FilamentHouseIbl.kt` + asset `house_ibl_256.hdr` (~128 KB / blob 131121 bytes)
  - `HouseFilamentSurface`: HDR env via `createHDREnvironment`, grazing rim light, lit glass/rims with reflectance
  - `HouseHumanoidNode`: skin/hair/cloth reflectance from `FilamentHouseIbl`
  - `FilamentHouseLighting`: day IBL **1800** / night **900**
  - Tests: `FilamentHouseIblTest` (asset path, day/night caps, rim/reflectance) + `FilamentHouseLightingTest` asserts day `iblIntensity == 1800`
- Docs same-turn: README (vc32 + SHA expect line), HANDOFF, `docs/design/GROK-0.14-IBL-FRESNEL.md`, `GAP-VS-PRODUCTION-HOUSE.md` row 8 **LANDED**.
- Local APK SHA-256 **matches** claimed `534386BC68F1A77381E8ACFE17B8BF8EB98D154F4FDDD8FFCEBD28C2C276C4D1`.
- No secrets in commit; `.tmp-aar/` remains untracked (do not `git add`).
- Local tag `v0.14.0-ibl-fresnel-dev` **absent** — expected until Lead annotates after this GO.
- Non-blocking: README/HANDOFF already link GitHub release URL for the tag (doc-ahead-of-publish drift until push/tag/release).
- #16 Realme soak **PENDING** — prerelease waiver OK per job brief / prior forgecity debug tags (`adb` not on PATH).

### Conditions

- Prerelease debug only; do **not** promote to production while #16 PENDING.
- After GO: Lead may push `main`, create annotated tag `v0.14.0-ibl-fresnel-dev` at `3fe2c26`, publish GitHub prerelease + APK asset.
- Do **not** commit `.tmp-aar/`.
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push, tag, or commit (SIGN-OFF write only).

### Blockers

None for prerelease push/tag under documented #16 waiver.
