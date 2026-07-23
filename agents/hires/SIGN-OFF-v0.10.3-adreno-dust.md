# SIGN-OFF — v0.10.3-adreno-dust-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 Grok Adreno dust / HIGH shadows |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip | `cbd44d941a6974b7be7fa05212169ea0cd4e66ed` |
| Tag | `v0.10.3-adreno-dust-dev` (prerelease) |
| versionCode | **25** |
| APK | `forgecity-0.10.3-adreno-dust-dev-debug.apk` |
| SHA-256 | `870C19820A8BF6EA9ABCE179EB24E4F80B7D7282DBE4C7B9E0779827CF1CC7C7` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] DustMoteCloud 32/64 + window-fill pulse + HIGH shadows via `setShadowingEnabled`
- [x] GLB floor lamps; Grok 0.10.3 note
- [x] compile/test/assemble PASS (trusted VERIFICATION + matching local APK)
- [ ] #16 Realme E2E — no device attached (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.10.3-adreno-dust-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `cbd44d941a6974b7be7fa05212169ea0cd4e66ed` (“Ship Adreno dust motes and HIGH-tier shadows (0.10.3).”) matches claimed scope; `main` 1 ahead of `origin/main`; no `v0.10.3*` tag yet (correct pre-push).
- Scope verified in tip: `DustMoteCloud` countFor **64**/HIGH (`allowsSoftShadows`) · **32**/else; `windowPulse` 0.6..1.0 @ ~0.8 Hz drives fill intensity; `HouseFilamentSurface` `view.setShadowingEnabled(allowsSoftShadows)` + `isShadowCaster`; `allowsSoftShadows` gated to `AnimationBudget` HIGH; `house_shell.glb` **67896** bytes with `add_floor_lamp` ×2; `docs/design/GROK-FILAMENT-0.10.3.md`; versionCode **25** / `0.10.3-adreno-dust-dev`.
- Local `forgecity-0.10.3-adreno-dust-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `870C19820A8BF6EA9ABCE179EB24E4F80B7D7282DBE4C7B9E0779827CF1CC7C7`.
- Tip diff: no secrets/keystore/env.
- #16 Realme physical soak PENDING (no ADB) — prerelease waiver OK, consistent with prior forgecity debug tags.
- Non-blocking: Grok 64 GLB sphere instances / explicit `ShadowOptions(2048…)` / emissive window planes scaled to CubeNode dust + `setShadowingEnabled` + fill-light pulse (Adreno-safe); HANDOFF “Now” still mentions 0.10.1; README/HANDOFF release URLs pre-point at unpublished tag.

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- Commit this SIGN-OFF before push/tag (tip currently has PENDING stub).
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push or tag.
