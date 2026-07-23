# SIGN-OFF — v0.12.0-patrol-openroof-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 PH gap backlog #7 (patrol/sit) + open-roof fix |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip (HEAD) | `01afac251cf58da8414749b5bc68a5d50ca5b5c4` (0.11.2 ship; **0.12.0 still uncommitted**) |
| Tag | `v0.12.0-patrol-openroof-dev` (claimed prerelease; **not present locally yet**) |
| versionCode | **30** |
| APK | `forgecity-0.12.0-patrol-openroof-dev-debug.apk` |
| SHA-256 | `4DDFFF4F12CDF0FE30948A4A861392612290E06F9E7C86B5EED8063FF4AD0E54` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] Open-roof: `_add_ceilings` perimeter cove + light trays only (no full sealed slabs)
- [x] Camera home raised (`Position` y **11.5**)
- [x] `HouseCharacterMotion` PATROL/SIT + `HumanoidAction.SIT` pose; wired in `HouseFilamentSurface`
- [x] Docs: README, HANDOFF, `GAP-VS-PRODUCTION-HOUSE` backlog **#7 0.12.0 LANDED**; vc **30**
- [x] Unit tests (`HouseCharacterMotionTest`, `HouseHumanoidPoseTest`); tip/working tree no secrets
- [x] versionCode **30** / `0.12.0-patrol-openroof-dev`; APK SHA match
- [ ] #16 Realme E2E — no device / no `adb` (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for commit of working-tree 0.12.0 scope + this SIGN-OFF, then push of `main` + annotated prerelease tag `v0.12.0-patrol-openroof-dev` + GitHub debug APK asset.

### Findings (#17)

- Working tree (not yet on tip) implements claimed 0.12.0 vs HEAD `01afac2` (“Ship set dressing: droop cables, hero props, soft dust (0.11.2).”). `main` tracks `origin/main` at tip; dirty WT has 0.12.0 sources. No push/tag/commit by #17; no local `v0.12.0*` tag (latest local tag `v0.11.2-set-dressing-dev`).
- Scope verified: `generate_house_assets.py` `_add_ceilings` docstring + four perimeter cove strips (`band=0.14`) + thin light tray — open center (fixes 0.11.2 sealed-slab orbit blindness). `HouseFilamentSurface` camera home `y = 11.5f`. New `HouseCharacterMotion` (mayor/workshop PATROL; assistant/kitchen SIT; talk override when speaking); `HouseHumanoidPose` SIT folds legs; surface samples motion → pose → `HouseHumanoidNode`. GAP backlog row **#7** marked **0.12.0 LANDED**; row 6 notes open-roof cove. `house_shell.glb` **154380** bytes.
- `app/build.gradle.kts`: versionCode **30**, versionName `0.12.0-patrol-openroof-dev`.
- Local `forgecity-0.12.0-patrol-openroof-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `4DDFFF4F12CDF0FE30948A4A861392612290E06F9E7C86B5EED8063FF4AD0E54`.
- Docs updated: README + HANDOFF (vc30 / SHA / 0.12.0 wave); `docs/design/GAP-VS-PRODUCTION-HOUSE.md` #7 LANDED.
- Unit tests present; `:app:testDebugUnitTest` for `HouseCharacterMotionTest` + `HouseHumanoidPoseTest` **PASS**. No secrets/keystore/env in reviewed 0.12.0 Kotlin/docs/generator.
- #16 Realme physical soak PENDING (`adb` unavailable) — prerelease waiver OK, consistent with prior forgecity debug tags.
- Non-blocking: GAP §P4 micro-finishing table still lists Ceiling / Pictures / AO as **Missing** while ranked backlog 4–6/7 say LANDED — doc drift only (carried from 0.11.2).

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- **Commit** 0.12.0 ship + this SIGN-OFF before push/tag (scope currently only in working tree; tip still 0.11.2).
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push, tag, or commit.
