# SIGN-OFF — forgecity-launcher v0.18.0-production-house-mode-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-25 Production House HomeMode |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip SHA | `011409f` |
| Scope | versionCode **38** · `0.18.0-production-house-mode-dev` |
| Tag | `v0.18.0-production-house-mode-dev` prerelease |
| When (IST) | 2026-07-25 |

## Checklist

- [x] `HomeMode.PRODUCTION_HOUSE` added; cycle CITY → HOUSE → ASSISTANT → PRODUCTION_HOUSE → CITY
- [x] Settings label shows **LOT** for the new mode
- [x] `ProductionHouseWebSurface` loads `https://production-house.delena.buzz` (PROD v0.1.0); JS + DOM storage on; offline/error fallback UI
- [x] Favorites dock remains available; dock visibility no longer ANDed with launcher chrome (Apps chip can restore dock alone)
- [x] Always-reachable **Apps** chip toggles `dockPanelVisible` (dock-only hide)
- [x] City video / Filament house / Assistant character paths unchanged when not in LOT mode
- [x] `HomeModeTest` + `ProductionHouseUrlTest` updated
- [x] versionCode **38** / `0.18.0-production-house-mode-dev`
- [x] `testDebugUnitTest` / `lintDebug` / `assembleDebug` PASS
- [x] APK SHA-256 `A7C99C2FDABE66D6C3E31A00AA40AC4B57F05C16490BA1C08A413C5032D094C1` · `dist/forgecity-0.18.0-production-house-mode-dev-debug.apk`
- [x] Docs tip updated: README, HANDOFF, ROADMAP, VERIFICATION, OPS
- [x] No secrets in diff
- [x] `.tmp-aar/` untracked — not staged for push
- [ ] **Zero on-device verification** — WebView + dock toggle await Realme user confirm

## Verdict

**GO** for push of `main` + annotated prerelease tag `v0.18.0-production-house-mode-dev` + GitHub debug APK asset — **strong condition** below.

### Findings (#17)

- Tip `011409f` matches claimed scope exactly: `HomeMode.PRODUCTION_HOUSE`, new `ProductionHouseWebSurface` → `https://production-house.delena.buzz`, `AppsDockChip` always reachable, dock scrim/favorites `AnimatedVisibility` decoupled from `launcherChromeVisible` (dock-only hide), version bump 38, docs tip, unit tests. City/House/Assistant render branches untouched — new mode is an additive `else if (productionHouseMode)` branch; city video explicitly gated off for LOT mode.
- APK SHA-256 verified locally via `Get-FileHash`: `A7C99C2FDABE66D6C3E31A00AA40AC4B57F05C16490BA1C08A413C5032D094C1` matches claimed hash exactly.
- Diff scan clean: no literal API keys, tokens, or keystore material; only public PROD URL and existing Keystore plumbing passed through unchanged.
- Working tree: `.tmp-aar/` is untracked (`??`) and nothing staged — safe for push.
- Same prerelease waiver as 0.17: zero on-device WebView/GPU verification on this build host is a **condition**, not a blocker.

### Conditions

- **Not closed** until the user reports: lot loads in WebView, favorites launch, **Apps** chip hides/shows dock only (WebView stays visible).
- ACTIVITY-LOG entry required after push/publish.
