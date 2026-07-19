# SIGN-OFF — v0.4.2 rewrite contract fix debug prerelease

| Field | Value |
|-------|-------|
| Session | CONSCIOUS #17 |
| Scope | `v0.4.2-rewrite-contract-fix-dev` / versionCode 9 |
| Date | 2026-07-20 |

## Root cause

`RewriteRequest.toJson()` sent 6 fields including `"store": false`. The server
contract (`ForgeCityRewriteContract.java`) requires exactly 5 fields and rejects
extras with HTTP 400 `invalid_request`; the client mapped 400 to `Unavailable`,
so both the TEST TTS button and live notification rewrites failed. Confirmed by
curl against PROD: with `store` → 400; without `store` → 200 + Tamil.

## Checks

- [x] Fix removes `store` field; request emits exactly `schemaVersion, appLabel, title, text, maxChars`
- [x] `no-store` still enforced via `Cache-Control` request header
- [x] Regression test asserts exact contract and absence of `store`
- [x] Unit tests, lint, and debug build pass
- [x] No secrets committed
- [x] HTTPS-only endpoint validation unchanged
- [x] Realme physical E2E remains PENDING; debug prerelease only

## Verdict

**GO** — commit, push, and publish the debug prerelease.
