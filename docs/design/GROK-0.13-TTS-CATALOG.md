# ForgeCity — Grok 0.13 TTS catalog

**API:** grok-4.3 · **2026-07-23**

Ship: `0.13.0-tts-catalog`

- Curated model/voice catalogs (no network fetch on launch)
- Random / Random F / Random M resolve at speak time
- Named template JSON library + active id
- Persist selections across restarts
- No device E2E; no PII in diagnostics

Implemented as plan: GeminiTtsCatalog, GeminiVoiceResolver, PromptTemplateLibraryCodec, AssistantSettingsStore library CRUD, CityAssistantOverlay dropdowns.
