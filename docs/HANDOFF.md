# Handoff — Foundry (formerly ForgeCity Launcher)

**Naming:** display name changed to **Foundry** (`app_name` string + in-app chapter chip + TEST TTS
copy) on 2026-08-05. Scope was deliberately display-name-only: `applicationId buzz.delena.forgecity`,
the GitHub repo name, package structure, and release/APK filenames are unchanged. Internal identifiers
kept as-is: log tag `ForgeCityTTS`, HTTP header `X-ForgeCity-Key` (live Agent Portal contract — do not
rename without a coordinated server-side change).

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.18.0-production-house-mode-dev` · versionCode **38** |
| Latest release | [`v0.18.0-production-house-mode-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.18.0-production-house-mode-dev) |
| APK SHA-256 | `A7C99C2FDABE66D6C3E31A00AA40AC4B57F05C16490BA1C08A413C5032D094C1` |
| Prior tip | [`v0.17.0-assistant-character-rigged-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.17.0-assistant-character-rigged-dev) |

## Now → next

| Now | Next |
|-----|------|
| 0.18.0 fourth Home mode **Production House** — WebView loads live Production House PROD v0.1.0 (`https://production-house.delena.buzz`); favorites dock unchanged; **Apps** chip toggles dock-only hide/show — **unverified on-device**, no ADB on this build host | User confirms on Realme P2 Pro: WebView loads the live lot, favorites launch correctly, **Apps** chip hides/shows dock only (WebView stays visible) |

Session: 2026-07-25.
