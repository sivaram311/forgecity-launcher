# Gemini speech cascade — product spec

**Status:** implementing (`0.4.3-gemini-cascade-dev`)  
**Priority:** **P1** (after P0 Tamil Portal + device diagnostics)  
**Date:** 2026-07-20

## Goal

Add a **smart cascade** speech path:

```text
Eligible notification / TEST TTS
  → apply user-editable pre-template (placeholders)
  → 1) Google Gemini API rewrite (first preference, if configured + reachable)
  → 2) Agent Portal Tamil rewrite (second, if configured + reachable)
  → 3) Device default-locale TTS (last resort, local DIRECT line)
```

Gemini performs **text rewrite**; Android **Tamil TTS** speaks Gemini/Portal output.
Gemini native audio TTS is **out of scope** for v1.

## Speech modes (unchanged manual paths + new cascade)

| Mode | Behaviour |
|------|-----------|
| `OFF` | No speech |
| `DIRECT_TTS` | Local filtered line → device locale TTS (no network) |
| `AGENT_PORTAL_TAMIL` | Portal only → Tamil TTS; fail-closed silent |
| `SMART_CASCADE` | Gemini → Portal → DIRECT (this spec) |

## Pre-template

User-editable template with placeholders:

| Placeholder | Source |
|-------------|--------|
| `{appLabel}` | Notification app label |
| `{title}` | Notification title |
| `{text}` | Notification body |
| `{maxChars}` | Output char budget (220) |

**Gemini:** formatted template is the `generateContent` user prompt.  
**Portal:** unchanged server contract (`appLabel`, `title`, `text`, `maxChars`) — server rewrites; template is not injected into Portal v1.  
**DIRECT fallback:** `NotificationSpeechFilter.spokenLine` (no template).

Template is editable in City Assistant, persisted in prefs, and applied on every TEST TTS / live notification before Gemini is called.

## Gemini configuration (launcher)

| Setting | Storage | Default |
|---------|---------|---------|
| API key | Android Keystore encrypted | empty |
| Model | plain pref | `gemini-2.0-flash` |
| Pre-template | plain pref | see `PromptTemplateDefaults` |

HTTPS only: `generativelanguage.googleapis.com`. Key never logged.

## Privacy

| Path | Data leaves device? |
|------|---------------------|
| DIRECT | No |
| Portal | Yes — to your Agent Portal host (no-store) |
| Gemini | Yes — to Google Generative Language API (user opt-in via key) |
| Cascade | Gemini attempt first; Portal only if Gemini fails |

Notification bodies are **not persisted** on device. Diagnostics log route/HTTP/TTS enums only.

## Parallel workstreams (this train)

| Stream | Owner | Deliverable |
|--------|-------|-------------|
| **G1** | ai-integration | `GeminiRewriteClient`, `PromptTemplateFormatter`, `CascadeSpeechOrchestrator` |
| **G2** | android-systems | `AssistantSettingsStore`, `AssistantSpeechMode`, listener + test runner routing |
| **G3** | ui-animation | City Assistant fields: Gemini key/model, template editor, CASCADE label |
| **G4** | qa-optimization | Unit tests, VERIFICATION, OPS, ROADMAP |

## Out of scope (v1)

- Gemini audio output / WaveNet
- Cloudflare Workers AI
- Portal server-side template injection
- Annotated production tag (Realme E2E still pending)
