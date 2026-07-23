# SIGN-OFF — v0.11.0-humanoid-daycycle-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 PH gap backlog 1–3 (humanoid + day-cycle + facade) |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip (HEAD) | `946c1dbd8f9ae889d0e07a2424a2e937584ba5a4` (prior 0.10.5 SIGN-OFF; **0.11 ship still uncommitted**) |
| Tag | `v0.11.0-humanoid-daycycle-dev` (claimed prerelease; **not present locally yet**) |
| versionCode | **28** |
| APK | `forgecity-0.11.0-humanoid-daycycle-dev-debug.apk` |
| SHA-256 | `E09408908541924FA99B0A1A2D1452795F41377E3DDDE67AFFBAB3D080FBE1A6` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] Jointed capsule humanoids idle/talk/walk (`HouseHumanoidPose` + `HouseHumanoidNode` in `HouseFilamentSurface`)
- [x] `FilamentDayCycle` ~120s sun/fill + sky bounce
- [x] `HouseFacadeFinishing` window panes + corner rims
- [x] Docs: README, HANDOFF, `GAP-VS-PRODUCTION-HOUSE`, `GROK-0.11-HUMANOID.md`
- [x] Unit tests (`HouseHumanoidPoseTest`, `FilamentDayCycleTest` / facade); tip/working tree no secrets
- [x] versionCode **28** / `0.11.0-humanoid-daycycle-dev`; APK SHA match
- [ ] #16 Realme E2E — no device / no `adb` (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for commit of working-tree 0.11 scope + this SIGN-OFF, then push of `main` + annotated prerelease tag `v0.11.0-humanoid-daycycle-dev` + GitHub debug APK asset.

### Findings (#17)

- Working tree (not yet on tip) implements claimed scope vs HEAD `946c1db` (“docs: #17 GO for v0.10.5…”). `main` clean vs `origin/main` at tip; dirty WT has 0.11 sources. No push/tag by #17; no local `v0.11*` tag.
- Scope verified: `HouseHumanoidPose` IDLE/TALK/WALK + role looks; `HouseHumanoidNode` capsule/sphere joints; wired in `HouseFilamentSurface` (replaces GLB char nodes for roster). `FilamentDayCycle.PERIOD_SEC=120`, sun/fill lux caps, sky RGB + hemi bounce applied to lights/env. `HouseFacadeFinishing` 6 window panes + 6 corner rims with ~4s emissive pulse. GAP backlog rows 1–3 marked **0.11.0 LANDED**.
- `app/build.gradle.kts`: versionCode **28**, versionName `0.11.0-humanoid-daycycle-dev`.
- Local `forgecity-0.11.0-humanoid-daycycle-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `E09408908541924FA99B0A1A2D1452795F41377E3DDDE67AFFBAB3D080FBE1A6`.
- Docs updated/present: README + HANDOFF (vc28 / SHA / 0.11 wave); `docs/design/GAP-VS-PRODUCTION-HOUSE.md`; `docs/design/GROK-0.11-HUMANOID.md`.
- Unit tests present; `:app:testDebugUnitTest` for `buzz.delena.forgecity.house.*` **PASS**. No secrets/keystore/env in reviewed 0.11 Kotlin/docs.
- #16 Realme physical soak PENDING (`adb` unavailable) — prerelease waiver OK, consistent with prior forgecity debug tags.

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- **Commit** 0.11 ship + this SIGN-OFF before push/tag (scope currently only in working tree; tip still 0.10.5 SIGN-OFF).
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push or tag.
