# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.7.0-assistant-clarity-dev` · versionCode 19 |
| Latest release | [`v0.7.0-assistant-clarity-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.7.0-assistant-clarity-dev) |
| APK SHA-256 | `CA5EE2B60FF8DBF75F63A40BDA55672D799874689CA8B346FDD201F579A408FC` |
| Prior tip | [`v0.6.1-tts-error-log-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.1-tts-error-log-dev) |

## Assistant Clarity (0.7.0)

- Mode-gated sheet: Gemini fields vs Portal fields by speech mode
- `PromptModeValidator` rejects rewrite-style prompts for **GEMINI AUDIO** / **CASCADE**
- Audio prompt presets: **Tamil clear** / **Kongu friend** / **English brief**
- API keys masked by default; reveal on tap (Keystore untouched on view)
- **TEST TTS** disabled while audio prompt invalid for current mode
- Diagnostics still **COPY LOG** / **CLEAR** (privacy invariants unchanged)

## Now → next

| Now | Next |
|-----|------|
| 0.7.0 Assistant Clarity published | Device: pick **Kongu friend** preset → TEST TTS → COPY LOG if fail |
| Grok consult filed | Kongu rewrite→audio two-step deferred to **0.8** |
| Realme E2E (#16) PENDING | Blocks production tags |

Session: 2026-07-23.
