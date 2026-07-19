# Parallel roadmap execution plan

**Status:** Active · CONSCIOUS-aligned  
**Session:** `forgecity-mvp-2026-07-19`  
**SoT roadmap:** [ROADMAP.md](ROADMAP.md)  
**Rule:** phases still gate **release tags**; implementation workstreams run **in parallel**.

---

## Principles (CONSCIOUS)

| Rule | How we apply it here |
|------|----------------------|
| #5 Drive purposes | All work on E: sandbox only |
| #6 Ports | No host ports (Android APK) |
| #8 CSS | N/A until a companion web console exists |
| #10 Activity log | Every crew wave logged |
| #12 Docs after action | ROADMAP / VERIFICATION / OPS same turn |
| #14–#16 E2E | Device lab on Realme P2 Pro before non-debug tags |
| #17 Reviewer | SIGN-OFF before every `git push` |

---

## Parallel workstreams (all phases)

Workstreams are **independent files / packages** so agents can land PRs without serial blocking.
Merge order still respects phase **product** gates (no v0.3 story ship until v0.2 living city is green on device).

```text
                    ┌─────────────────────────────┐
                    │   Vision & Product (lead)   │
                    │  priority · battery budget  │
                    └──────────────┬──────────────┘
           ┌───────────┬───────────┼───────────┬───────────┐
           ▼           ▼           ▼           ▼           ▼
      Stream A    Stream B    Stream C    Stream D    Stream E
      UI/Anim     Android     Data        Narrative   AI/Agents
      Canvas      Systems     Room/XP     Story       Phase 4+
```

| Stream | Owner role | Owns packages | Can start now? |
|--------|------------|---------------|----------------|
| **A — UI/Animation** | ui-animation | `ui/`, city render | ✅ Phase 2 |
| **B — Android Systems** | android-systems | `usage/`, `power/`, WorkManager | ✅ Phase 2 |
| **C — Data** | data-persistence | `data/`, migrations | ✅ Phase 2 |
| **D — Narrative** | narrative-designer | `story/`, STORY-BIBLE | ✅ design; code Phase 3 |
| **E — AI Agents** | ai-integration | `agents/` runtime (future) | ⏸ stubs only until Phase 4 |
| **F — QA** | qa-optimization | e2e checklist, VERIFICATION | ✅ continuous |

---

## Wave plan (maximize parallelism)

### Wave 0 — Planning (done)

- [x] ROADMAP.md
- [x] This PARALLEL-EXECUTION.md
- [x] Crew roles under `agents/roles/`

### Wave 1 — Phase 2 foundations (done)

Run **A + B + C** in the same branch tip:

| ID | Stream | Deliverable | Done when |
|----|--------|-------------|-----------|
| W1-A1 | A | Day/night parallax sky + night building glows | Visual change with real clock |
| W1-A2 | A | Camera fly-in before app launch | Tap → animate → launch |
| W1-A3 | A | Double-tap recenter + animation budget hook | Power-save pauses ambient |
| W1-B1 | B | `AnimationBudget` (PowerManager) | Ambient off in power-save |
| W1-B2 | B | UsageStats harvest → resources | WorkManager periodic + grant UX |
| W1-C1 | C | Resource apply + launch-count building levels | Room meta updates; levels redraw |
| W1-F1 | F | Unit tests for XP + day/night math | `./gradlew testDebugUnitTest` green |

### Wave 2 — Phase 2 polish (landed on dev)

| ID | Stream | Deliverable | Status |
|----|--------|-------------|--------|
| W2-A1 | A | Level-up particle burst (capped, 12 particles) | ✅ landed (dev) |
| W2-A2 | A | Animated resource counters (`animateIntAsState`) | ✅ landed (dev) |
| W2-B1 | B | Usage access deep-link + harvest debounce (1h gate) | ✅ landed (dev) |
| W2-C1 | C | Room-backed building levels (`building_stats`, DB v2) + idempotent seeding | ✅ landed (dev) |
| W2-D1 | D | Chapter 2–3 quest stubs seeded (pulled forward) | ✅ landed (dev) |
| W2-F1 | F | Realme P2 Pro baseline (120 Hz / battery) — **blocks v0.2.0 tag** | ⏳ pending device |

### Wave 2.5 — Background video framework (done on `main`)

| ID | Stream | Deliverable | Status |
|----|--------|-------------|--------|
| W25-A1 | A | Media3 ExoPlayer layer under CityCanvas | ✅ `v0.3.2` |
| W25-B1 | B | Lifecycle + AnimationBudget + power-save receiver | ✅ |
| W25-F1 | F | Missing-asset fallback + debug prerelease | ✅ PR #4 |
| W25-A2 | A | Final `city_background.mp4` + seamless loop | ✅ `v0.3.3` procedural |
| W25-F2 | F | Realme decoder/thermal/battery E2E | ⏳ pending device |

### Wave 3 — Phase 3 (parallel design + scaffold)

| ID | Stream | Deliverable |
|----|--------|-------------|
| W3-D1 | D | Chapter 1–3 JSON quest packs |
| W3-A1 | A | Weather particle emitter (gated) |
| W3-A2 | A | Quest map markers |
| W3-E0 | E | Agent interface stubs (no network) |

### Wave 4 — Phase 4+ (after Wave 2 device GO)

| ID | Stream | Deliverable |
|----|--------|-------------|
| W4-E1 | E | Mayor / Story Weaver / Architect / Guardian / Chronicler |
| W4-A1 | A | Haptics + optional audio |
| W4-B1 | B | ColorOS badge / QS exploration |

---

## Conflict rules (keep streams parallel)

1. **UI never writes Room directly** — call repository / ViewModel APIs.
2. **Systems never draw Canvas** — expose `AnimationBudget` + XP results only.
3. **Migrations are owned by Stream C** — other streams request schema fields in docs first.
4. **No `QUERY_ALL_PACKAGES`** without explicit user confirmation.
5. **No host ports / CSS client** in this app until companion UI is requested.
6. **Tag `v0.2.0` only after** Wave 2 device E2E GO (#16).

---

## Suggested simultaneous agent hires

```text
Hire A: UI/Animation — W1-A1..A3 on feature/phase-2-awakening
Hire B: Android Systems — W1-B1..B2 same branch (non-overlapping files)
Hire C: Data — W1-C1 same branch
Hire F: QA — write tests + VERIFICATION updates after A/B/C land
```

Lead merges when unit tests + Reviewer #17 GO; device E2E is a separate hire with Playwright slot N/A (native adb lab).

---

## Tracking

| Phase | Version | Parallel streams | Release gate |
|-------|---------|------------------|--------------|
| 1 Embers | `v0.1.0-mvp` | — | ✅ shipped (debug prerelease) |
| 2 Awakening | `v0.2.0-awakening-dev` | A+B+C+D+F | ✅ Waves 1–2 + debug prerelease; **device E2E** for annotated `v0.2.0` |
| 2.5 Video | `v0.3.3-background-video-asset-dev` | A+B+F | ✅ framework + procedural MP4 on `main`; Realme E2E open |
| 3 Narrative | `v0.3.0` | A+D(+E0)+F | Device E2E required |
| 4 Symphony | `v0.4.0` | E+A+B+F | Device E2E required |
| 5 Stable | `v1.0.0` | all | Promote evidence if distributed beyond sideload |

**Current hire focus:** Stream F (Realme P2 Pro video/decoder/thermal/battery E2E).
