# SIGN-OFF — v0.4.4 Gemini fix + ASSIST chip

| Field | Value |
|-------|-------|
| Session | CONSCIOUS #17 |
| Scope | `0.4.4-gemini-fix-assist-chip-dev` / versionCode 11 |
| Date | 2026-07-20 |

## Root cause (Gemini unavailable)

1. Default model `gemini-2.0-flash` was **shut down 2026-06-01**; valid keys still hit a dead model → non-OK HTTP → mapped to Unavailable.
2. HTTP 400 `API_KEY_INVALID` was mapped to Unavailable instead of Unauthorized.
3. Malformed (no Tamil) was also shown as "unavailable".

## Fix

- Default + auto-migrate to `gemini-2.5-flash`
- `x-goog-api-key` header; clearer status strings; ModelUnavailable / Malformed / Unauthorized distinct
- ASSIST +/− chip hides City Assistant + search + favorites dock

## Verdict

**GO** — commit, push, publish debug prerelease.
