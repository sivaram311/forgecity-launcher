# SIGN-OFF — v0.13.0-tts-catalog-dev

| Field | Value |
|-------|-------|
| Session | 2026-07-23 TTS catalog (models/voices/templates) |
| Reviewer | **GO** (CONSCIOUS #17 readonly) |
| Tip (HEAD) | `e6ea72f3d68f8e784546d74fc66086577cda72a1` (0.12.0 ship; **0.13.0 still uncommitted**) |
| Tag | `v0.13.0-tts-catalog-dev` (claimed prerelease; **not present locally yet**) |
| versionCode | **31** |
| APK | `forgecity-0.13.0-tts-catalog-dev-debug.apk` |
| SHA-256 | `ADA35CC0C54CC77C15C362675D8BE6D4FCD333F091F4A6471E4EAD699447DC78` |
| When (IST) | 2026-07-23 |

## Checklist

- [x] `GeminiTtsCatalog` **30** voices + **3** models; `GeminiVoiceResolver` random sentinels (`__RANDOM__` / `__RANDOM_F__` / `__RANDOM_M__`)
- [x] `AssistantSettingsStore` template library JSON + CRUD; `CascadeSpeechOrchestrator` resolves voice before `synthesize`
- [x] `CityAssistantOverlay` `CatalogDropdown` for model/voice/templates + Save as…/Delete
- [x] Docs: README, HANDOFF, `GROK-0.13-TTS-CATALOG.md`; vc **31**
- [x] Unit tests (`GeminiTtsCatalogTest`, `GeminiVoiceResolverTest`, `PromptTemplateLibraryCodecTest`); tip/working tree no secrets
- [x] versionCode **31** / `0.13.0-tts-catalog-dev`; APK SHA match
- [ ] #16 Realme E2E — no device / no `adb` (PENDING waiver)
- [x] Reviewer GO

## Verdict

**GO** for commit of working-tree 0.13.0 scope + this SIGN-OFF, then push of `main` + annotated prerelease tag `v0.13.0-tts-catalog-dev` + GitHub debug APK asset.

### Findings (#17)

- Working tree (not yet on tip) implements claimed 0.13.0 vs HEAD `e6ea72f` (“Ship open-roof dollhouse and room patrols (0.12.0).”). `main` tracks `origin/main` at tip; dirty WT has 0.13.0 sources. No push/tag/commit by #17; no local `v0.13*` tag (latest local tag `v0.12.0-patrol-openroof-dev`).
- Scope verified: `GeminiTtsCatalog` — **3** `ModelOption`s + **30** `VoiceOption`s (both genders). `GeminiVoiceSelection` / `GeminiVoiceResolver` encode/decode FIXED + random sentinels; resolve picks from full / female / male pools at speak time. `PromptTemplateLibraryCodec` JSON encode/decode + preset seed; `AssistantSettingsStore` `KEY_PROMPT_TEMPLATES_JSON` + list/select/update/saveAs/delete CRUD. `CascadeSpeechOrchestrator.tryGeminiAudio` calls `GeminiVoiceResolver.resolve` then `geminiAudioClient.synthesize(..., voice = resolvedVoice)`. Overlay: template/model/voice `CatalogDropdown` + Save as…/Delete wired via `ForgeCityViewModel`.
- `app/build.gradle.kts`: versionCode **31**, versionName `0.13.0-tts-catalog-dev`.
- Local `forgecity-0.13.0-tts-catalog-dev-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk` SHA-256 both match claimed `ADA35CC0C54CC77C15C362675D8BE6D4FCD333F091F4A6471E4EAD699447DC78`.
- Docs updated: README + HANDOFF (vc31 / SHA / 0.13.0 wave); `docs/design/GROK-0.13-TTS-CATALOG.md`.
- Unit tests present; `:app:testDebugUnitTest` for `buzz.delena.forgecity.assistant.gemini.*` **PASS** (catalog 30 voices, resolver sentinels, codec round-trip/seed). No secrets/keystore/env in reviewed 0.13.0 Kotlin/docs (API keys remain Android Keystore–encrypted prefs only).
- #16 Realme physical soak PENDING (`adb` unavailable) — prerelease waiver OK, consistent with prior forgecity debug tags.
- Non-blocking: HANDOFF/README already link GitHub release/tag `v0.13.0-tts-catalog-dev` while local tip is still 0.12.0 and tag is absent — doc-ahead-of-ship drift until commit/push/tag.

### Conditions

- Prerelease debug only; do not promote to production while #16 PENDING.
- **Commit** 0.13.0 ship + this SIGN-OFF before push/tag (scope currently only in working tree; tip still 0.12.0).
- ACTIVITY-LOG entry required after push/publish.
- #17 did **not** push, tag, or commit.
