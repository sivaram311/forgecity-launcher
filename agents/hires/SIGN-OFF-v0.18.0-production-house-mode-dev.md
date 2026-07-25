# SIGN-OFF — forgecity-launcher v0.18.0-production-house-mode-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-25 Production House HomeMode |
| Reviewer | **PENDING #17** (hire readonly Reviewer before push/tag) |
| Tip SHA | *(fill after commit)* |
| Scope | versionCode **38** · `0.18.0-production-house-mode-dev` |
| Tag | `v0.18.0-production-house-mode-dev` prerelease (after GO) |
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
- [ ] **Zero on-device verification** — WebView + dock toggle await Realme user confirm
- [ ] Reviewer #17 GO before `git push` / annotated tag / GitHub release asset

## Verdict

**Ready for #17 Reviewer hire** — do **not** push or tag until GO.

### Findings (author)

- Fast path deliberately uses live WebView of Production House PROD rather than a native Filament film-lot port.
- Requires network; offline shows “Lot offline — check network”.
- Native Filament lot / bundled static PH assets explicitly out of scope for 0.18.0.

### Conditions

- Not closed until user reports: lot loads in WebView, favorites launch, Apps chip hides/shows dock only.
- ACTIVITY-LOG entry required after push/publish.
