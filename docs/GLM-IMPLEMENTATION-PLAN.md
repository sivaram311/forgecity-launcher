# ForgeCity — GLM Implementation Plan

| Field | Value |
|-------|-------|
| Requested planner | `@cf/zai-org/glm-5.2` (Cloudflare Workers AI) |
| GLM-5.2 status | **Unavailable on Workers Free** — API error **5035** (requires **Workers Paid**) |
| Free-tier substitute tried | `@cf/zai-org/glm-4.7-flash` timed out; `@cf/meta/llama-3.3-70b-instruct-fp8-fast` too generic for SoT |
| This document | Authored **via Grok CLI** from live repo SoT on `main` |
| Repo tip | `0.3.3-background-video-asset-dev` · **versionCode 6** · PR **#5** merged |
| Device gate | Realme P2 Pro physical E2E (**#16**) — **PENDING**; blocks annotated production tags |
| Date | 2026-07-19 |
| Constraint | Planning doc only; does not authorize code/git/build changes by itself |

> **Note:** Re-plan with true GLM-5.2 only after Workers Paid is enabled on the Cloudflare account. Until then, treat this file as the authoritative next-wave plan, aligned with [ROADMAP.md](ROADMAP.md), [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md), [ARCHITECTURE.md](ARCHITECTURE.md), [VERIFICATION.md](VERIFICATION.md), and [BACKGROUND-VIDEO-SPEC.md](BACKGROUND-VIDEO-SPEC.md).

> **2026-07-19 product override:** Cloudflare Workers AI / GLM remote Worker integration for the **launcher is REJECTED**. P0 is Agent Portal Tamil notification rewrite + Tamil TTS — see [TAMIL-REWRITE-SPEC.md](TAMIL-REWRITE-SPEC.md). Section 5 below is historical only.

---

## 1) Maturity verdict

ForgeCity is a **strong debug-sideload launcher MVP+**, not a production home screen.

**Shipped on `main` (0.3.3):** Compose isometric `CityCanvas` (pan/zoom, depth AABB hit, fly-in, double-tap recenter); districts; Room **v3** + migrations **1→2→3**; favorites dock (max 6); UsageStats → resources + WorkManager; `AnimationBudget` (`interactive && !powerSave && !idle`); day/night sky; Forge Assistant (`NotificationListenerService` + privacy-first TTS: off by default, empty allowlist, quiet hours, **never** persists bodies); Media3 muted looping background with toggle + opacity + lifecycle/power gates; bundled original procedural `res/raw/city_background.mp4` (1080×1920 H.264, 10s, ~3 MB) + `tools_gen_city_bg.py`.

**Not production-ready because:** physical Realme P2 Pro E2E is still **PENDING** — decoder, 120 Hz smoothness, thermal, ColorOS HOME quirks, and **&lt;5% extra battery** claims are unverified. Story is catalog stubs (chapters 1–3 seeded, little branching UI). In-city agents, weather/ambient life, widgets/shortcuts, icon packs, ColorOS badges, and optional Filament are **not started**.

**Product maturity band:** **Late Phase 2 / experimental 0.3.x train** — feature density is high for a debug APK; **trust density** (device evidence) is low. Next highest-value move is the device gate, with narrative/weather/agent-stub scaffolds in parallel (no hardware required).

---

## 2) Clarifying questions (max 6, prioritized)

1. **P0 — Device lab window:** When can a Realme P2 Pro be on USB with debugging for a half-day OPS lab (install `v0.3.3-background-video-asset-dev`, HOME role, video + Awakening + battery sample)?  
2. **P0 — Phase order after GO:** Prefer **Phase 3 story/weather/markers** before **Phase 4 agents**, or agents-as-local-stubs first for “city feels inhabited”? (Plan default: Phase 3 UI + **E0 stubs in parallel**.)  
3. **P1 — Workers Paid / remote AI:** Approve Cloudflare **Workers Paid** so later agent inference can use `@cf/zai-org/glm-5.2` (or a paid model) via a **server-side Worker only**?  
4. **P1 — Remote payload consent:** Confirm optional cloud is OK if the Worker receives **only** non-PII city summaries (`chapterId`, resource ints, district counts, `hourOfDay`) — **never** notification title/body/package of the spoken notif?  
5. **P2 — Distribution target:** Stay **sideload/debug prereleases** for the 90-day window, or schedule a **release-signed** path after E2E?  
6. **P2 — Video art direction:** Keep procedural `city_background.mp4` under the [BACKGROUND-VIDEO-SPEC.md](BACKGROUND-VIDEO-SPEC.md) contract, or budget a later art pass (still local raw, same gates)?

---

## 3) 30 / 60 / 90 day plan

Streams (owners): **A** UI/Anim · **B** Android Systems · **C** Data/Room · **D** Narrative · **E** AI Agents · **F** QA/device.

Package root: `app/src/main/java/buzz/delena/forgecity/`.

**Parallelism rule:** Phase **release tags** still gate on device GO; **implementation** of D/E0/A scaffolds may land as `*-dev` without claiming performance PASS.

### Days 0–30 — Gate + zero-device scaffolds

Prefer the **smallest high-value next wave**: close **#16**, while D/E0/A design work needs no device.

| Wave ID | Streams | Deliverable | Exact packages / files (touch list) | Acceptance criteria | Risks |
|---------|---------|-------------|-------------------------------------|---------------------|-------|
| **W-F0** | **F** (+ lead) | Realme P2 Pro E2E lab against tip prerelease | `docs/OPS.md` (checklist run), `docs/VERIFICATION.md` (evidence rows), optional `agents/hires/SIGN-OFF-*` | Install `v0.3.3-background-video-asset-dev`; set as HOME; complete Awakening + **background-video** OPS paths; log decoder OK/FAIL, 120 Hz feel, thermal, power-save pauses video+ambient, quiet hours/TTS defaults; PASS or explicit FAIL with repro | No device → stays PENDING; do **not** invent battery numbers |
| **W-D0** | **D** | Chapter JSON pack **schema** + samples (assets or kotlin packs) | `story/StoryCatalog.kt`, new e.g. `story/QuestPack.kt` / `assets/story/*.json` (if chosen), `docs/STORY-BIBLE.md`, unit tests under `app/src/test/.../story/` | Chapters 1–3 expressed as data (ids, goals, branch keys Vault vs Spire); unique quest ids; `StoryCatalogTest` (or successor) green; **no** requirement for map UI yet | Schema churn before C migration — keep fields additive |
| **W-A0** | **A** (+ **B** contract) | Weather / ambient **scaffold** behind budget | `ui/CityCanvas.kt`, `city/DayNightCycle.kt`, new e.g. `city/WeatherState.kt` or `ui/WeatherEmitter.kt`, read-only use of `power/AnimationBudget.kt` | Emitter API + optional gated prototype; **zero** particles when `!AnimationBudget.allowsAmbient`; unit-testable pure math preferred | Overdraw/jank on Adreno 710 if shipped ungated |
| **W-E0** | **E** | **Local-only** agent interface stubs | **New** `agents/` package: e.g. `Agent.kt`, `LocalAgentRuntime.kt`, `MayorAgent.kt`, `StoryWeaverAgent.kt`, `ArchitectAgent.kt`, `ScoutAgent.kt`, `GuardianAgent.kt`, `ChroniclerAgent.kt`; wire optional preview text via `ui/ForgeCityViewModel.kt` / `ui/CityAssistantOverlay.kt` | Interfaces + deterministic briefings from Room summary only; **no** `HttpURLConnection`/OkHttp/Ktor; no CF token; offline always works | Scope creep into remote before privacy review |
| **W-C0** | **C** | Schema **request** only (docs) for story progress fields | `docs/` note → later `data/Entities.kt`, `data/ForgeCityMigrations.kt` | Written field list for branch path, quest marker anchors; **no** migration merge until D0 schema frozen | Premature DB v4 without consumers |

**Ship policy (0–30):**  
- Evidence-only docs → version may stay `0.3.3` or bump `0.3.4-e2e-evidence-dev` if VERIFICATION/OPS materially change with a tagged debug APK.  
- Code scaffolds → `0.3.4-story-scaffold-dev` or `0.3.5-agent-stubs-dev` as needed; still debug prereleases.  
- **Annotated production tags blocked** until W-F0 GO.

**Risks (period):** Device delay freezes trust metrics; parallel scaffolds must not weaken battery gates or assistant privacy.

---

### Days 31–60 — Phase 3 foundations (“living map + story depth”)

Assumes W-F0 at least **attempted**; if FAIL, fix blockers first (video decode, drain, HOME). Scaffolds from 0–30 become product features.

| Wave ID | Streams | Deliverable | Exact packages / files | Acceptance criteria | Risks |
|---------|---------|-------------|------------------------|---------------------|-------|
| **W3-D1** | **D** + **C** | Branching chapter progress persisted | `story/*`, `data/Entities.kt` (`StoryProgressEntity`, maybe `CityMetaEntity` path flags), `data/CityDao.kt`, `data/CityRepository.kt`, `data/ForgeCityDatabase.kt`, `data/ForgeCityMigrations.kt` (**v3→v4** if needed), `ui/ForgeCityViewModel.kt` | Unlock rules for ch2/ch3 stubs; Vault vs Tech Spire branch flags durable across restart; migration tested (`ForgeCityMigrationsTest`) | Migration bugs wiping favorites/resources — test IGNORE/seed patterns already used in v2/v3 |
| **W3-A2** | **A** + **D** | Quest markers on isometric map | `ui/CityCanvas.kt`, `city/BuildingHitGeometry.kt` / `city/IsoMath.kt` / `city/IsoLayout.kt`, ViewModel quest → marker model | Depth-sorted markers; tap opens quest sheet (reuse chapter card patterns in `ForgeCityHomeScreen.kt` / overlay); marker count capped (e.g. ≤5 active) | Hit-test regressions vs buildings/favorites |
| **W3-A1** | **A** + **B** | Weather particles (rain/fog/clear) | `ui/CityCanvas.kt`, weather helper, `power/AnimationBudget.kt` (no rewrite of gate semantics) | Weather driven by local clock (+ optional later API **not** required); fully paused in power-save/screen-off/idle; no crash if ambient off | Thermal + battery regression — re-run F checklist |
| **W3-B1** | **B** | Ambient + video regression on gates | `ui/background/BackgroundVideoPlayer.kt`, `ui/background/CityBackgroundVideo.kt`, `power/AnimationBudget.kt`, assistant unchanged | Confirm video still requires: user toggle + resumed + interactive + budget; document any ColorOS quirks in OPS | ColorOS aggressive kill of NLS / WorkManager |
| **W3-F1** | **F** | Regression E2E on tip APK | `docs/VERIFICATION.md`, `docs/OPS.md` | Full checklist green on Realme for story+weather build; battery sample if weather on | Lab time scarcity |

**Version target:** `0.4.0-story-foundations-dev` (debug prerelease).  
**Release gate for any undecoated `v0.4.0`:** W3-F1 GO + Reviewer SIGN-OFF (#17).

---

### Days 61–90 — Symphony lite (local agents + optional Worker)

| Wave ID | Streams | Deliverable | Exact packages / files | Acceptance criteria | Risks |
|---------|---------|-------------|------------------------|---------------------|-------|
| **W4-E1** | **E** + **D** | Local Mayor + Story Weaver (+ stub others) | `agents/*`, `story/*`, `ui/CityAssistantOverlay.kt`, `ui/ForgeCityViewModel.kt` | Daily/local briefing from `CityMetaEntity` + quest list + district counts; Scout/Guardian/Chronicler/Architect return template strings; unit tests for pure formatters | Copy spam — rate-limit speech-like UI (mirror `assistant/SpeechBudget.kt` patterns if needed) |
| **W4-E2** | **E** + **B** | Optional Cloudflare **Worker proxy** (feature-flag, default **OFF**) | App: `agents/remote/AgentRemoteClient.kt` (or similar); **never** secrets in `app/`; Worker repo/script **outside** APK (self-hosted Worker); settings in SharedPreferences near `assistant/AssistantSettingsStore.kt` or new `agents/AgentSettingsStore.kt` | Worker holds Workers AI token; app POSTs **allowlisted JSON only** (see §5); timeout/HTTP fail → **local stub**; remote disabled without user opt-in | Accidental PII; free-tier 5035 on glm-5.2 — pick paid model or keep stubs |
| **W4-A1** | **A** | Haptics on pin / level-up | `ui/FavoritesDock.kt`, `ui/CityCanvas.kt` / ViewModel events | Subtle; no haptics when ambient budget false if tied to celebration FX | Annoyance — keep short |
| **W4-B2** | **B** | Widgets / shortcuts **spike** (optional if time) | new widget provider under `app/src/main` + manifest; `launcher/AppCatalog.kt` only if needed | Pin favorite app or city scrap counter; no `QUERY_ALL_PACKAGES` | ColorOS widget tray quirks |
| **W4-F2** | **F** | 8h battery compare vs stock launcher | VERIFICATION evidence | **&lt;5% extra drain** claim only if measured; power-save path re-verified | Lab protocol variance |

**Version target:** `0.5.0-symphony-stubs-dev`.  
**Do not** embed `CLOUDFLARE_WORKERS_AI_TOKEN` or any API token in the APK.

---

### Suggested “now” parallel hire board (no serial wait)

```text
Hire F:  W-F0 Realme E2E (#16)                    [blocks production tags]
Hire D:  W-D0 chapter packs + STORY-BIBLE align   [no device]
Hire E:  W-E0 agents/ local stubs                 [no device, no network]
Hire A:  W-A0 weather scaffold + AnimationBudget  [no device]
Hire C:  W-C0 migration field proposal only       [docs first]
```

Conflict rules (unchanged from [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md)): UI never writes Room directly; Systems never draw Canvas; migrations owned by **C**; no host ports/Postgres/CSS for this APK.

---

## 4) Version roadmap (0.3.3 → v1.0, `-dev` prereleases)

Naming: **`versionName` = `X.Y.Z-<theme>-dev`** for debug prereleases; GitHub prerelease tags **`v` + versionName**. Annotated **production** tags (no `-dev`, or store-ready) only after Realme **#16 GO** + Reviewer **#17**.

| versionName (proposed) | versionCode* | Theme | Streams | Gate |
|------------------------|--------------|-------|---------|------|
| `0.3.3-background-video-asset-dev` | **6** (current) | Media3 + procedural MP4 | A B F | **Shipped** debug prerelease; E2E pending |
| `0.3.4-e2e-evidence-dev` | 7 | Docs/evidence or hotfix from lab | F | Device lab executed (PASS or FAIL recorded) |
| `0.3.5-story-scaffold-dev` | 8 | Quest packs / catalog data only | D C | Unit tests; no production tag |
| `0.3.6-agent-stubs-dev` | 9 | `agents/` local interfaces | E | No network; privacy checklist |
| `0.4.0-story-foundations-dev` | 10 | Weather + markers + branching progress | A D C B F | Realme smoke on this tip |
| `0.4.1-story-foundations` (optional undecoated) | 11 | Same after GO | F + #17 | **Requires #16 GO** |
| `0.5.0-symphony-stubs-dev` | 12 | Local agents + optional Worker client | E B F | Remote default off; fail closed |
| `0.6.0-polish-dev` | 13 | Haptics/audio, icon packs, ColorOS badges, widgets | A B F | ColorOS checklist |
| `0.7.0-beta-dev` | 14 | Public beta polish, thermal pass | all | Multi-day battery/thermal evidence |
| `1.0.0` | 20+ | Sideload-stable “city home” | all | Full E2E + SIGN-OFF; optional Filament **only** if profiler proves need; optional backup/sync |

\*versionCode values are **planning slots** — bump monotonically in `app/build.gradle.kts` at ship time; do not skip evidence for pretty numbers.

**Historical line (already published, do not renumber):**  
`0.1.0-mvp` → `0.2.0-awakening-dev` → `0.3.0/0.3.1-forge-assistant-dev` → `0.3.2-background-video-dev` → **`0.3.3-background-video-asset-dev`**.

**Roadmap doc phases vs train:** ROADMAP “Phase 3 v0.3.0” narrative ships **after** the experimental 0.3.x assistant/video train — product versioning above uses **0.4.0-story-*** for that narrative drop to avoid colliding with already-shipped 0.3.x assistant tags.

---

## 5) AI-agent architecture sketch (Cloudflare Workers AI, no secrets in APK)

### 5.1 Hybrid model

```text
┌─────────────────────────────────────────────────────────────┐
│ ForgeCity APK (buzz.delena.forgecity)                         │
│  agents/LocalAgentRuntime                                     │
│    ├─ Mayor, Story Weaver, Architect, Scout, Guardian,        │
│    │  Chronicler — deterministic templates first              │
│    ├─ inputs: CityRepository snapshot (Room)                  │
│    │         + hourOfDay + AnimationBudget/user flags         │
│    └─ optional: AgentRemoteClient  ──HTTPS──►  CF Worker      │
│         feature flag default OFF · timeout · fail closed      │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ POST /v1/city-briefing
                              │ Authorization: app-issued session
                              │   or signed app attestation later
                              │ Body: allowlisted JSON only
                              ▼
┌─────────────────────────────────────────────────────────────┐
│ Self-hosted Cloudflare Worker (server-side only)              │
│  secret: WORKERS_AI_TOKEN / AI binding                        │
│  env: model id e.g. @cf/zai-org/glm-5.2 (Paid) or fallback    │
│  validates schema · strips unknown keys · rate limits         │
│  calls Workers AI · returns { "briefing": "...", "agent": } │
└─────────────────────────────────────────────────────────────┘
```

**Why hybrid:** On-device stubs always work offline, preserve privacy, and match CONSCIOUS “no tokens in client.” Remote is **flavor text upgrade**, not gameplay authority.

**GLM-5.2 note:** `@cf/zai-org/glm-5.2` requires **Workers Paid** (Free returns **5035**). Until Paid is enabled, Worker should call a free/paid available model **or** the app should stay local-only. This plan was produced via **Grok CLI** for that reason.

### 5.2 App ↔ Worker payload **allowlist** (request)

Only these fields (names illustrative; keep stable once shipped):

| Field | Type | Source in app | Allowed |
|-------|------|---------------|---------|
| `schemaVersion` | int | constant | yes |
| `chapterId` | int | `CityMetaEntity.chapterId` | yes |
| `scrap` | int | meta | yes |
| `power` | int | meta | yes |
| `focus` | int | meta | yes |
| `goldDust` | int | meta | yes |
| `districtCounts` | map&lt;string,int&gt; | aggregated from `buildings` / classifier | yes |
| `activeQuestIds` | string[] | `story_progress` where status=active (ids only) | yes |
| `hourOfDay` | int 0–23 | local clock | yes |
| `locale` | string (optional) | `Locale.getDefault().language` | yes |
| `agentRole` | enum string | Mayor / StoryWeaver / … | yes |

**Explicitly forbidden in any request or log shipped to Worker:**

- Notification title, text, bigText, ticker, categories tied to message bodies  
- Raw `PackageManager` app labels lists beyond **counts per district**  
- Contacts, SMS, exact usage event timelines, precise lat/long  
- Assistant allowlist package names (optional later only with separate consent)  
- Any API tokens

### 5.3 Response contract

```json
{
  "schemaVersion": 1,
  "agentRole": "Mayor",
  "briefing": "Short plain text ≤ 500 chars",
  "suggestedQuestId": null
}
```

Client validates length; on any parse/HTTP/AI error → **local stub**.

### 5.4 Offline / fail-closed

| Condition | Behavior |
|-----------|----------|
| Remote flag off (default) | LocalAgentRuntime only |
| No network / DNS fail | Local stub |
| HTTP 4xx/5xx / 5035 model unavailable | Local stub |
| Timeout (recommend 3–5s) | Local stub |
| Response fails schema / too long | Local stub |
| Power-save (optional policy) | Skip remote; local one-liner only |

### 5.5 Secrets & CONSCIOUS

- Token lives in Worker secrets / server env — **never** `local.properties`, never `BuildConfig` from a checked-in secret, never APK string resources.  
- Dev workshop may keep tokens under machine secret paths **outside** this APK tree; do not copy into `E:\MyWorkspace\sandbox\forgecity-launcher\app`.  
- No Cloudflare DNS API tokens in the project for this client.  
- Assistant path remains separate: `assistant/*` continues to **never** persist notification bodies; agents **must not** read NLS buffers for remote prompts.

### 5.6 Agent roles (product mapping)

| Agent | Local stub behavior (0–90d) | Later remote flavor |
|-------|----------------------------|---------------------|
| **Mayor** | City status from resources + chapter | Pep talk / daily decree |
| **Story Weaver** | Next quest blurb from `StoryCatalog` | Tone-adapted chapter copy |
| **Architect** | Layout tips (favorites count, empty districts) | District balance advice |
| **Scout** | “What’s new” from install/launch counts | Exploration prompts |
| **Guardian** | Focus/Arena tension from resource mix | Focus-mode coaching |
| **Chronicler** | Session recap from harvest deltas | Diary-style summary |

Package home: **`buzz.delena.forgecity.agents`** (new), consumed by `ui` / ViewModel; **no** Canvas draws from E; **no** Room writes except via `data/CityRepository`.

---

## 6) Explicit do-not-do list

1. **Do not** claim Realme / Adreno / 120 Hz / thermal / **&lt;5% battery** PASS without **#16** evidence in [VERIFICATION.md](VERIFICATION.md).  
2. **Do not** cut annotated **production** tags (or store uploads) while device E2E is PENDING.  
3. **Do not** embed Cloudflare / Workers AI / DNS / any API **tokens** in the APK, VCS, or `BuildConfig` for release.  
4. **Do not** persist **notification bodies** (or full notification payloads) in Room, files, or analytics.  
5. **Do not** turn TTS on by default or pre-fill a broad package allowlist — deny-by-default stays.  
6. **Do not** add **`QUERY_ALL_PACKAGES`** without explicit user/product OK (Play-restricted; sideload-only discussion).  
7. **Do not** run ambient particles or **background video** when `AnimationBudget` disallows ambient (power-save, non-interactive, idle) or when lifecycle is paused.  
8. **Do not** send notification text, private messages, or raw usage event streams to any Worker/LLM.  
9. **Do not** block offline use on remote AI — fail closed to local stubs.  
10. **Do not** start **Filament** / 3D engine work before Compose profiling on the Realme shows a hard ceiling.  
11. **Do not** open host ports, run Postgres, or add a CSS/web companion **for this APK** unless product explicitly requests a separate console.  
12. **Do not** `git push` without Reviewer **SIGN-OFF** (#17) and CONSCIOUS activity/docs updates; keep work on **E:** sandbox only.  
13. **Do not** treat merge waivers as E2E PASS (PR merges without device lab are not device GO).  
14. **Do not** expand favorites dock past policy (**max 6**) without a deliberate UX pass (`city/FavoritePolicy.kt`).  
15. **Do not** replace `city_background.mp4` with huge assets that break the ~3 MB / 1080×1920 / muted loop contract without updating [BACKGROUND-VIDEO-SPEC.md](BACKGROUND-VIDEO-SPEC.md) and re-running device lab.

---

## Immediate recommended actions

1. Answer clarifying questions **#1–#3** (device date, Phase 3 vs agents, Workers Paid).  
2. Run **W-F0** Realme lab on `v0.3.3-background-video-asset-dev` as soon as hardware is available.  
3. In parallel (no device): **W-D0** chapter packs, **W-E0** `agents/` stubs, **W-A0** weather scaffold behind `AnimationBudget`.  
4. Only after Paid plan: re-invoke `@cf/zai-org/glm-5.2` if a second opinion plan is desired; until then this Grok-authored plan is SoT for implementation sequencing.

---

## Related docs

| Doc | Role |
|-----|------|
| [ROADMAP.md](ROADMAP.md) | Phased product roadmap |
| [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md) | Streams A–F + wave IDs |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Stack + package map |
| [VERIFICATION.md](VERIFICATION.md) | Evidence / #16 status |
| [BACKGROUND-VIDEO-SPEC.md](BACKGROUND-VIDEO-SPEC.md) | Media3 contract |
| [STORY-BIBLE.md](STORY-BIBLE.md) | Campaign outline |
| [OPS.md](OPS.md) | Device lab commands |
