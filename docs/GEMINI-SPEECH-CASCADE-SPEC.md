# Gemini speech cascade — product spec

**Status:** implementing (`0.4.6-gemini-native-audio-dev`)  
**Priority:** **P1**  
**Date:** 2026-07-20

## Goal

Smart cascade speech path with **Gemini native audio TTS** as tier 1:

```text
Eligible notification / TEST TTS
  → apply user-editable prompt template (placeholders)
  → 1) Google Gemini TTS model (`responseModalities: AUDIO`) → PCM playback
  → 2) Agent Portal Tamil rewrite → device Tamil TTS
  → 3) Device default-locale TTS (last resort)
```

Gemini returns **audio bytes** (typically 24 kHz L16 PCM). Android plays via `AudioTrack`.
Device Tamil TTS is only used for Portal / DIRECT tiers.

## Speech modes

| Mode | Behaviour |
|------|-----------|
| `OFF` | No speech |
| `DIRECT_TTS` | Local filtered line → device locale TTS (no network) |
| `AGENT_PORTAL_TAMIL` | Portal only → Tamil TTS; fail-closed silent |
| `GEMINI_TAMIL` (UI: **GEMINI AUDIO**) | Native audio only; fail-closed |
| `SMART_CASCADE` | Gemini audio → Portal → DIRECT |

## Prompt template

User-editable template with placeholders:

| Placeholder | Source |
|-------------|--------|
| `{appLabel}` | Notification app label |
| `{title}` | Notification title |
| `{text}` | Notification body |
| `{maxChars}` | Spoken content budget (220) |

**Gemini audio:** formatted template is the `generateContent` user prompt (style + content).  
**Portal:** unchanged server contract — template is not injected into Portal.  
**DIRECT fallback:** `NotificationSpeechFilter.spokenLine` (no template).

## Gemini configuration (launcher)

| Setting | Storage | Default |
|---------|---------|---------|
| API key | Android Keystore encrypted | empty |
| TTS model | plain pref | `gemini-3.1-flash-tts-preview` |
| Voice | plain pref | `Kore` |
| Language | plain pref | `ta-IN` (prompt hint only — **not** sent as speechConfig.languageCode) |
| Prompt template | plain pref | see `PromptTemplateDefaults` |

Prior text-only models (`gemini-2.5-flash`, etc.) are auto-migrated to the TTS default.

**Request shape (0.5.1+):** `generationConfig.responseModalities=["AUDIO"]` +
`speechConfig.voiceConfig.prebuiltVoiceConfig.voiceName` only. Language is
steered in the user prompt (official generateContent TTS docs).

HTTPS only: `generativelanguage.googleapis.com`. Key never logged. Bodies / PCM never logged.

## Privacy

| Path | Data leaves device? |
|------|---------------------|
| DIRECT | No |
| Portal | Yes — Agent Portal host (no-store) |
| Gemini audio | Yes — Google Generative Language API (opt-in via key) |
| Cascade | Gemini audio first; Portal only if Gemini fails |

## Out of scope (this train)

- Multi-speaker dialogue
- Streaming SSE playback
- Cloudflare Workers AI
- Annotated production tag (Realme E2E still pending)
