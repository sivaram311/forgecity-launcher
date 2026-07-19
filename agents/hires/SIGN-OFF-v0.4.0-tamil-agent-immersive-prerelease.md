# SIGN-OFF — forgecity-launcher feature/tamil-agent-immersive-speech

| Field | Value |
|-------|-------|
| Session | CONSCIOUS #17 · Tamil Agent Portal immersive speech prerelease |
| Reviewer agent id | readonly Reviewer (generalPurpose subagent) |
| Provider | cursor |
| Tip SHA | f86423fcefb2d4ddb84d4894e8beb4aa75c9cefa |
| Branch / tag | feature/tamil-agent-immersive-speech (branch push; debug prerelease `v0.4.0-tamil-agent-dev`) |
| When (UTC+5:30) | 2026-07-20 00:14 |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12) — tip commit `f86423f` bundles source + tests + `docs/{TAMIL-REWRITE-SPEC,VERIFICATION,OPS,ARCHITECTURE,ROADMAP,GLM-IMPLEMENTATION-PLAN}.md` + README
- [x] No secrets in commit — full-tree scan for api_key/secret/password/token/bearer/PEM/`ghp_`/`sk-`/`AKIA` found only doc references and encrypted-keystore plumbing; `git ls-files` has no `.apk/.aab/.jks/.keystore/.p12/.env/.pem` or `local.properties`
- [x] Fleet splits OK — sandbox DEV-only launcher; no host ports / Postgres / CSS involved
- [x] DEV E2E green if this push includes a release tag (#16) — N/A for branch + debug prerelease; Realme device E2E correctly still PENDING (see below)
- [x] Login E2E (#18) — N/A (no login host in this APK)
- [x] Tag ≠ live understood — `main` remains `0.3.3`; matrix not falsely bumped; `0.4.0` is local/prerelease only

## Verdict

**GO**

Scope: branch push of `feature/tamil-agent-immersive-speech` and/or debug-signed prerelease of `0.4.0-tamil-agent-dev`. NOT authorized for an annotated production tag.

### Findings

**Secrets / supply chain — PASS**
- No hardcoded keys/tokens/endpoints. API key is user-entered, encrypted via Android Keystore AES/GCM (`AssistantSettingsStore`), never displayed after save, never logged. `X-ForgeCity-Key` header is populated only from the decrypted user value.
- `android:allowBackup="false"` prevents key/pref exfiltration via ADB backup. INTERNET permission added (required for Portal POST); `<queries>` stays scoped — no `QUERY_ALL_PACKAGES`.
- No tracked APK/keystore/env; `.gitignore` tracked.

**Privacy defaults — PASS**
- Speech mode default **OFF** (`speechMode` migrates only from legacy flags that default false → OFF).
- Package allowlist **empty** by default; `NotificationSpeechFilter` denies when allowlist empty (unit-tested `denyByDefaultEmptyAllowlist`).
- Endpoint + API key default empty; `isRemoteRewriteConfigured` requires both.
- **No body persistence:** notification title/body/Tamil never written to prefs/Room/logs. `RewriteRequest` sends `store:false`; `notificationKey` is not serialized into the JSON (unit-tested). Bubble `PendingIntent`/text stay in-memory.

**SpeechMode exclusivity — PASS**
- `AssistantSpeechMode` enum {OFF, DIRECT_TTS, AGENT_PORTAL_TAMIL} is single source of truth; `NotificationSpeechRoute.resolve` maps OFF→NONE, DIRECT_TTS→DIRECT, AGENT_PORTAL_TAMIL→PORTAL only when configured else NONE. DIRECT path never touches the rewrite client; PORTAL path never uses default-locale TTS. Legacy migration and routing unit-tested (`AssistantSpeechModeTest`).

**Immersive chrome — PASS**
- `LauncherChromeDefaults.VISIBLE = false`; all chapter/resource/settings/search/dock/hint/bubble/allowlist UI is gated behind `AnimatedVisibility(launcherChromeVisible)`. The `UI +/UI −` chip is rendered outside that gate (always present), 48 dp, TopEnd, status-bar-padded, with `contentDescription`/`stateDescription` for TalkBack. Visibility persisted. Unit-tested `launcherChromeDefaultsHidden`.

**Cloudflare rejection — PASS**
- No Cloudflare/Workers AI/desktop-CLI code path in the APK; client is a single `HttpsURLConnection` (HTTPS-only, no redirects, no userInfo/query, bounded 16 KB response, strict JSON parse, Tamil-charset + forbidden-meta validation, fail-closed). Rejection documented in README, OPS, TAMIL-REWRITE-SPEC (non-goals), and GLM-IMPLEMENTATION-PLAN.

**Realme E2E — CORRECTLY PENDING**
- `docs/VERIFICATION.md` (0.4.0 section) marks "Realme 360×780 immersive + speech E2E | PENDING — No physical run claimed." OPS release boundary requires final device GO + Reviewer #17 before any annotated production tag. Debug prerelease is explicitly allowed with the standing device-absent waiver. No false PASS claimed.

**Non-blocking notes**
- Request JSON carries an extra `store:false` field not shown in the spec's request example (spec lists schemaVersion/appLabel/title/text/maxChars). Privacy-positive; consider adding to spec for completeness.
- SIGN-OFF filename says `immersive-prerelease` while `versionName` is `0.4.0-tamil-agent-dev`; naming variance only, no functional impact.
- `assistantEnabled` defaults true (assistant bubble visible) while speech defaults OFF — consistent with "speech off" privacy requirement.
