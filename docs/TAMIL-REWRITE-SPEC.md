# ForgeCity Tamil Agent Portal Rewrite

**Status:** implemented in source (`0.4.0-tamil-agent-dev`) · not yet published  
**Priority:** **P0** — first product priority over Cloudflare / GLM Workers AI  
**Date:** 2026-07-19

## Product rule

When the explicit speech mode is `AGENT_PORTAL_TAMIL`, every eligible
notification must:

1. Trigger a **fresh / isolated Agent Portal rewrite conversation** on the host.
2. Receive a **clear Tamil rewrite** (no invented facts, no metadata).
3. Speak that Tamil text with Android **Tamil TTS**.

**No Cloudflare Workers AI** is used by the launcher. Desktop CLI paths are never invoked from the phone.

## Privacy defaults (unchanged)

| Setting | Default |
|---------|---------|
| Speech mode | **OFF** |
| Package allowlist | **empty** |
| Endpoint / API key | **empty** |
| Launcher chrome | **hidden** (persistent 48 dp Show UI / Hide UI chip remains) |
| Notification bodies / Tamil text | **never persisted** on device |
| Logs | enums / lengths only — never title/body/tamil |

Fail closed: timeout, auth failure, portal down, malformed rewrite, or missing Tamil voice → **silent** (UI bubble may still show; no English TTS fallback).

Legacy migration runs once when no explicit mode is stored:

- `ttsEnabled && remoteRewriteEnabled` → `AGENT_PORTAL_TAMIL`
- `ttsEnabled` only → `DIRECT_TTS`
- otherwise → `OFF`

`DIRECT_TTS` never calls Agent Portal. It speaks the local
`NotificationSpeechFilter.spokenLine(appLabel, title, text)` using the device
default locale.

## Data flow

```text
NotificationListenerService
  → local gates (assistant, SpeechBudget, quiet hours, allowlist, dedupe)
  → ephemeral UI bubble
  → DIRECT_TTS: local spokenLine → device-default-locale TTS
  OR
  → RAM queue (max 3 pending + 1 active; coalesce by key)
  → HTTPS POST /api/integrations/forgecity/tamil-rewrite
       Header: X-ForgeCity-Key
  → validate {schemaVersion:1,status:ok,tamil}
  → AssistantTtsEngine speak with Locale ta-IN / ta
```

## Request / response contract

### Request

```json
{
  "schemaVersion": 1,
  "appLabel": "WhatsApp",
  "title": "…",
  "text": "…",
  "maxChars": 220
}
```

### Success response

```json
{
  "schemaVersion": 1,
  "status": "ok",
  "tamil": "…"
}
```

Errors return status-only JSON (e.g. `{"status":"unauthorized"}`) with no echoed body.

## Agent Portal host config

Disabled by default. Enable only with env:

| Env | Purpose |
|-----|---------|
| `FORGECITY_REWRITE_ENABLED=true` | Master enable |
| `FORGECITY_REWRITE_API_KEY` | Dedicated key (not the portal global API key) |
| `FORGECITY_REWRITE_TIMEOUT_MS` | Hard timeout (default 30000) |
| `FORGECITY_REWRITE_MAX_CHARS` | Server ceiling (default 500; phone sends ≤220) |
| `FORGECITY_REWRITE_MAX_CONCURRENT` | Default 1 |
| `FORGECITY_REWRITE_WORKSPACE` | Ephemeral ACP cwd |

Server creates a **non-persisted** Cursor ACP session per request, rejects tools/permissions, suppresses payload logs, and validates `TAMIL:` output.

## Phone settings

Show UI chip → City Assistant section:

- Compact speech-mode cycle: OFF → DIRECT → PORTAL தமிழ்
- HTTPS endpoint URL (e.g. `https://delena.buzz/api/integrations/forgecity/tamil-rewrite`)
- API key (stored encrypted via Android Keystore AES/GCM; never shown after save)

## Explicit non-goals

- Cloudflare Workers AI / GLM remote Worker from the APK
- Persisting bodies in Agent Portal ChatMessage / Room / SharedPreferences
- Shelling out to `grok` / `agent` / `agy` from Android
- English TTS fallback when Tamil rewrite fails
