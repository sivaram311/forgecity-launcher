# SIGN-OFF — v0.10.1-filament-fix-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 Filament fullscreen + function fix |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip | `5023e5e947eb322854651ba77db2d2cb10a06a49` |
| Tag | `v0.10.1-filament-fix-dev` (prerelease) |
| versionCode | **23** |
| APK | `forgecity-0.10.1-filament-fix-dev-debug.apk` |
| SHA-256 | `1CEB353D9B6F4D0F6B21390A25436CA97568F1D36D286C909896544A7C541116` |
| When (IST) | 2026-07-23 |

## Fixes

- [x] Immersive fullscreen (edge-to-edge + hide system bars)
- [x] SceneView `TextureSurface` (was SurfaceView behind opaque Compose → blank)
- [x] Remove full-screen vignette hit-stealer; draw vignette via `drawWithContent`
- [x] Marker chips for reliable app launch; orbit manipulator enabled
- [x] `autoCenterContent=false` so camera framing sticks
- [x] Unit/compile/assemble PASS
- [ ] #16 Realme E2E — no device attached (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.10.1-filament-fix-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `5023e5e947eb322854651ba77db2d2cb10a06a49` (“fix: Filament fullscreen and TextureSurface house (0.10.1).”) matches claimed SHA; `main` 1 ahead of `origin/main`.
- Scope verified in tip: `MainActivity` `enableEdgeToEdge` + immersive system bars; `HouseFilamentSurface` `SurfaceType.TextureSurface` + `rememberCameraManipulator` + `HouseMarkerChips` + `autoCenterContent=false`; vignette moved to `drawWithContent` (no full-screen hit Box); versionCode **23** / `0.10.1-filament-fix-dev`.
- Local `forgecity-0.10.1-filament-fix-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `1CEB353D9B6F4D0F6B21390A25436CA97568F1D36D286C909896544A7C541116`.
- Tip diff: no secrets/keystore/env; no port/DB/CSS.
- #16 Realme physical soak PENDING (no ADB) — prerelease waiver OK, consistent with prior forgecity debug tags.
- Non-blocking: unused `Stroke` import in `ForgeCityHomeScreen.kt`; dirty unstaged `docs/HANDOFF.md` Now/Next line (not on tip).

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- Commit this SIGN-OFF (and optional dirty HANDOFF) before push/tag if not already on tip.
- ACTIVITY-LOG entry required after push/publish.
