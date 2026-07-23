# SIGN-OFF — forgecity-launcher v0.9.0-3d-house-characters-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 Wave 2/3 complete |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `a128af0c1c8aef45073f9c0482c2ba13b2bd6704` |
| Scope | versionCode **21** · `0.9.0-3d-house-characters-dev` |
| Tag | `v0.9.0-3d-house-characters-dev` prerelease |
| When (IST) | 2026-07-23 |

## Checklist

- [x] Wave 2: Vault, furniture, characters, toggle, budget wiring, speech pulse
- [x] Wave 3: HousePerfBudget + DEVICE-E2E-HOUSE-CHECKLIST
- [x] Assistant/chrome preserved; CityCanvas via toggle
- [x] test+lint+assemble green
- [x] APK SHA-256 `D8E66EA9442B9C7F7747FCCA9DCBE3FFE454FA3F886648EEEDE842D461154A7F`
- [ ] Filament (explicitly out of scope for 0.9)
- [ ] Realme #16 physical soak PENDING (waiver for prerelease)
- [x] No secrets

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.9.0-3d-house-characters-dev` + GitHub debug APK asset.

### Findings (#17)

- Tip `a128af0` matches claimed SHA; message/scope cover Waves 2+3: Vault annex, furniture silhouettes, idle characters (`maxCharacters` / soft-glow from `AnimationBudget`), settings `house_home_enabled` toggle, `AssistantHouseBridge` speech pulse, `HousePerfBudget` + policy tests, `DEVICE-E2E-HOUSE-CHECKLIST.md`; versionCode **21** / `0.9.0-3d-house-characters-dev`; no Filament/glTF deps in Gradle or app sources.
- Local `dist/forgecity-0.9.0-3d-house-characters-dev-debug.apk` SHA-256 matches claimed `D8E66EA9442B9C7F7747FCCA9DCBE3FFE454FA3F886648EEEDE842D461154A7F`; no secrets/keystore/env in tip.
- Runtime budget caps still flow via `AnimationBudget` in `ForgeCityViewModel`; `HousePerfBudget` is landed (policy + history + tests) per Wave 3 done-when — not yet swapped as sole ViewModel source (non-blocking for this prerelease).
- #16 Realme physical soak PENDING with explicit prerelease waiver — consistent with prior forgecity debug tags and checklist gate.

### Conditions

- Prerelease only; procedural Compose house ≠ full Filament realism.
- Do not promote to production while #16 PENDING.
- #16 checklist ready for device when available.
- ACTIVITY-LOG entry required after push/publish.
