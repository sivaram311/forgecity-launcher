# SIGN-OFF — v0.11.2-set-dressing-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 PH gap backlog 4–6 (set dressing) |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip (HEAD) | `4ec6a9c5ab1be33071c97590eed62e9e30f9b2c8` (0.11.0 ship; **0.11.2 still uncommitted**) |
| Tag | `v0.11.2-set-dressing-dev` (claimed prerelease; **not present locally yet**) |
| versionCode | **29** |
| APK | `forgecity-0.11.2-set-dressing-dev-debug.apk` |
| SHA-256 | `79342FD2A70D9EB47BA85956E4465F61E0E554662D51D3CA226BABF20C88B9AF` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] Droop cables: `generate_house_assets.py` `_add_cable_chain` / parabolic mid-span + `HouseCableRuns.kt`
- [x] Hero props per room (`_add_hero_props`)
- [x] Sphere dust: `DustMoteCloud` + `HouseFilamentSurface` `SphereNode` (not `CubeNode` for motes)
- [x] Ceilings, pictures, corner AO in generator; `house_shell.glb` regenerated (**148164** bytes ≈148KB)
- [x] Docs: README, HANDOFF, `GAP-VS-PRODUCTION-HOUSE` backlog 4–6 **0.11.2 LANDED**; vc **29**
- [x] Unit tests (`HouseCableRunsTest`, `DustMoteCloudTest`); tip/working tree no secrets
- [x] versionCode **29** / `0.11.2-set-dressing-dev`; APK SHA match
- [ ] #16 Realme E2E — no device / no `adb` (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for commit of working-tree 0.11.2 scope + this SIGN-OFF, then push of `main` + annotated prerelease tag `v0.11.2-set-dressing-dev` + GitHub debug APK asset.

### Findings (#17)

- Working tree (not yet on tip) implements claimed gap 4–6 vs HEAD `4ec6a9c` (“Ship jointed humanoids and day-cycle lighting (0.11.0).”). `main` tracks `origin/main` at tip; dirty WT has 0.11.2 sources. No push/tag by #17; no local `v0.11.2*` tag (only `v0.11.0-humanoid-daycycle-dev`).
- Scope verified: `_droop_span` / `_add_cable_chain` parabolic `y -= sag*4t(1-t)` hallway→office→workshop; `HouseCableRuns.droopSpan` + `defaultRuns` match GLB waypoints. `_add_hero_props` kettle/plant/laptop/slate/toolbox/vault bar (+ bedroom lamp). `DustMoteCloud` 72/40 sphere radii; `HouseFilamentSurface` dust loop uses `SphereNode` (windows/rims still cubes — OK). Generator `_add_ceilings` / `_add_pictures` / `_add_corner_ao` wired in `build_house_shell`. GAP backlog rows 4–6 marked **0.11.2 LANDED**.
- `app/build.gradle.kts`: versionCode **29**, versionName `0.11.2-set-dressing-dev`.
- Local `forgecity-0.11.2-set-dressing-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `79342FD2A70D9EB47BA85956E4465F61E0E554662D51D3CA226BABF20C88B9AF`.
- Docs updated: README + HANDOFF (vc29 / SHA / 0.11.2 wave); `docs/design/GAP-VS-PRODUCTION-HOUSE.md` backlog 4–6 LANDED.
- Unit tests present; `:app:testDebugUnitTest` for `HouseCableRunsTest` + `DustMoteCloudTest` **PASS**. No secrets/keystore/env in reviewed 0.11.2 Kotlin/docs/generator.
- #16 Realme physical soak PENDING (`adb` unavailable) — prerelease waiver OK, consistent with prior forgecity debug tags.
- Non-blocking: GAP §P4 micro-finishing table still lists Ceiling / Pictures / AO as **Missing** while ranked backlog 4–6 say LANDED — doc drift only.

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- **Commit** 0.11.2 ship + this SIGN-OFF before push/tag (scope currently only in working tree; tip still 0.11.0).
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push or tag.
