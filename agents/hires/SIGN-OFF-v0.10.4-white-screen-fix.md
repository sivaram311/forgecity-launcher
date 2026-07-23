# SIGN-OFF — v0.10.4-white-screen-fix-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 Filament white-screen exposure fix |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip | `59b494ba3f172444ee21dc62e4d4c9c230eb31f2` |
| Tag | `v0.10.4-white-screen-fix-dev` (prerelease) |
| versionCode | **26** |
| APK | `forgecity-0.10.4-white-screen-fix-dev-debug.apk` |
| SHA-256 | `AF48EEA7D44FD3838724D45C68D65FC8ECBC719D7411577C36B73F87F55E7224` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] Photographic `setExposure(aperture, shutter, iso)` (not bare EV float)
- [x] Lower sun/IBL; day bloom off
- [x] Grok white-screen note
- [x] assemble PASS (trusted VERIFICATION + matching local APK)
- [ ] #16 Realme E2E — no device attached (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.10.4-white-screen-fix-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `59b494ba3f172444ee21dc62e4d4c9c230eb31f2` (“fix: Filament white screen from EV exposure misuse (0.10.4).”) matches claimed scope; `main` 1 ahead of `origin/main`; no push/tag by #17.
- Scope verified: `HouseFilamentSurface` calls `setExposure(lighting.aperture, lighting.shutterSpeed, lighting.iso)`; day sun **14k→5k** / IBL **12k→3k**; `day.bloomEnabled = false`; versionCode **26** / `0.10.4-white-screen-fix-dev`; `docs/design/GROK-WHITE-SCREEN.md`.
- Local `forgecity-0.10.4-white-screen-fix-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `AF48EEA7D44FD3838724D45C68D65FC8ECBC719D7411577C36B73F87F55E7224`.
- Tip diff: no secrets/keystore/env.
- #16 Realme physical soak PENDING (no ADB) — prerelease waiver OK, consistent with prior forgecity debug tags.

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- Commit this SIGN-OFF before push/tag (was PENDING stub on tip).
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push or tag.
