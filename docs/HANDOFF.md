# Handoff — Foundry (formerly ForgeCity Launcher)

**Naming:** display name changed to **Foundry** (`app_name` string + in-app chapter chip + TEST TTS
copy) on 2026-08-05, shipped as `0.18.1-foundry-rename-dev`. Scope was deliberately display-name-only:
`applicationId buzz.delena.forgecity`, the GitHub repo name, package structure, and the
`forgecity-<version>-debug.apk` release-filename convention are all unchanged. Internal identifiers
kept as-is: log tag `ForgeCityTTS`, HTTP header `X-ForgeCity-Key` (live Agent Portal contract — do not
rename without a coordinated server-side change).

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.18.1-foundry-rename-dev` · versionCode **39** |
| Latest release | [`v0.18.1-foundry-rename-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.18.1-foundry-rename-dev) |
| APK SHA-256 | `3D4F0082A92205A1D9E932C56700CBD9CB5313FFF5C3FE7C36A1ECE53F6B7802` |
| Prior tip | [`v0.18.0-production-house-mode-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.18.0-production-house-mode-dev) |

## Now → next

| Now | Next |
|-----|------|
| 0.18.1 display-name rename to **Foundry** — app label + in-app chip + TEST TTS copy only; `testDebugUnitTest`/`lintDebug`/`assembleDebug` green; **not device-confirmed** (string-only change, doesn't re-trigger #16) | Still outstanding from 0.18.0: user confirms on Realme P2 Pro that Production House WebView loads the live lot, favorites launch correctly, **Apps** chip hides/shows dock only |

Session: 2026-08-05.
