# SIGN-OFF — v0.10.1-filament-fix-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 Filament fullscreen + function fix |
| Reviewer | **PENDING** (#17) |
| Tip | *(fill after commit)* |
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
- [ ] Reviewer GO

## Verdict

**PENDING** #17.
