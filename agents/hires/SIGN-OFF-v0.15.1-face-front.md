# SIGN-OFF — forgecity-launcher v0.15.1-face-front-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 face card front-of-head hotfix (0.15.1) |
| Reviewer agent id | CONSCIOUS #17 Push Reviewer (readonly) · Composer subagent |
| Provider | cursor |
| Tip SHA | `531245de6e7dd824a98f5f73ef27de2a51c45e85` |
| Branch / tag | `main` (ahead 1 of `origin/main`) · claimed prerelease `v0.15.1-face-front-dev` (**not present locally / on GitHub yet**) |
| versionCode | **34** |
| versionName | `0.15.1-face-front-dev` |
| APK | `forgecity-0.15.1-face-front-dev-debug.apk` (~44.2 MB / 46370974 bytes) |
| SHA-256 | `977CCF820F37CC74F77282764A2FCBB849D9CBA02161A08D89E0D7B5BD96AF0E` |
| When (UTC+5:30) | 2026-07-23 23:38+ IST tip; review ~23:39 IST |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12): README, HANDOFF
- [x] No secrets in commit (diff scan clean; no keystore/env/token blobs)
- [x] Fleet splits OK — N/A (single forgecity Android app; no classic/css-next split)
- [ ] DEV E2E green if this push includes a release tag (#16) — **waived**: `adb` unavailable / no Realme device (same as prior forgecity debug tags)
- [x] Login E2E (#18) — N/A (no CSS DEV host login surface for this APK ship)
- [x] Tag ≠ live understood — prerelease `-dev` debug only; matrix not falsely bumped
- [x] versionCode **34** / versionName match `app/build.gradle.kts` + docs
- [x] APK SHA-256 matches claimed (local hash verified)
- [x] Face card transform: `z = -0.125f` + `Rotation(y = 180f)` (was `z = +0.125f`)
- [x] Tip SHA is 0.15.1 commit (`Fix face card on front of head (0.15.1).`)
- [x] Working tree clean for tracked files; only untracked `?? .tmp-aar/` (do not commit)
- [x] `.tmp-aar` **not** committed (untracked only; `.gitignore` covers)

## Verdict

**GO**

### Findings

- Tip `531245d` is the sole unpushed commit on `main`; message and file set match face-front hotfix scope (4 files: `HouseHumanoidNode.kt`, `app/build.gradle.kts`, README, HANDOFF).
- Scope verified in tip:
  - `HouseHumanoidNode`: face `CubeNode` `position.z` **+0.125 → −0.125**; added `rotation = Rotation(y = 180f)`
  - Comment updated: portrait on face (−Z local; +Z was back of head)
  - `versionCode` **34** / `versionName` `0.15.1-face-front-dev`
  - Docs: README (vc34 + SHA expect line), HANDOFF
- Local APK SHA-256 **matches** claimed `977CCF820F37CC74F77282764A2FCBB849D9CBA02161A08D89E0D7B5BD96AF0E`.
- No secrets in commit; `.tmp-aar/` remains untracked (do not `git add`).
- Local / GitHub tag `v0.15.1-face-front-dev` **absent** — expected until Lead annotates after this GO.
- Non-blocking: README/HANDOFF already link GitHub release URL for the tag (doc-ahead-of-publish drift until push/tag/release).
- #16 Realme soak **PENDING** — prerelease waiver OK per job brief / prior forgecity debug tags (`adb` not on PATH).

### Conditions

- Prerelease debug only; do **not** promote to production while #16 PENDING.
- After GO: Lead may push `main`, create annotated tag `v0.15.1-face-front-dev` at `531245d`, publish GitHub prerelease + APK asset.
- Do **not** commit `.tmp-aar/`.
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push, tag, or commit (SIGN-OFF write only).

### Blockers

None for prerelease push/tag under documented #16 waiver.
