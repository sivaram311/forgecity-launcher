# ForgeCity Roadmap

**Goal:** Transform the Realme P2 Pro home screen into a deeply immersive,
game-like living city with rich backgrounds, fluid animations, progression, and
story — while staying performant on Snapdragon 7s Gen 2 + Adreno 710 + 120 Hz.

**Repo:** https://github.com/sivaram311/forgecity-launcher  
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`  
**Last updated:** 2026-07-23 (`0.7.0-assistant-clarity-dev` · Assistant Clarity implementing)

---

## 1. Vision

ForgeCity turns the phone into a personal cyber-fantasy metropolis. Apps are
buildings. Daily habits rebuild districts, unlock chapters, and shape a
branching narrative.

**Core pillars**

| Pillar | Intent |
|--------|--------|
| Living world backgrounds | Day/night, weather, parallax, ambient life |
| Rich animations | Camera fly-ins, level-ups, particles, smooth transitions |
| Progression & story | Usage XP → city evolution + chapter quests |
| Multi-agent AI layer | In-city companions for quests, layout, focus |
| Realme P2 Pro first | Battery-aware, 120 Hz smooth, low thermal impact |

**Target feel:** Stardew Valley city-building + cyber night-city atmosphere,
running as the actual HOME launcher.

---

## 2. Current state — MVP 0.1.0 “Embers”

| Included | Gaps |
|----------|------|
| Compose isometric Canvas city | Static background |
| Pan / zoom / tap-to-launch | Minimal animations |
| District classification + Room | No real UsageStats XP loop |
| Chapter 1 briefing + resource strip | No weather / ambient life |
| HOME registration + search | No in-city AI agents |

**Ship status:** Source + debug APK published as public prerelease
`v0.1.0-mvp`. Physical Realme P2 Pro E2E is still **pending** (documented
waiver in `docs/VERIFICATION.md`) — not yet “live on device” as a signed-off
gate.

---

## 3. Target experience

### 3.1 Backgrounds & world

- Parallax sky/ground (foreground grid + mid-ground + animated sky)
- Real-time day/night; building lights at night
- Weather particles (rain/fog/clear) from device time (+ optional light API)
- Low-cost ambient life (traffic, flicker, occasional particles)

### 3.2 Animations & polish

- Camera with inertia + bounds; double-tap recenter; tap → fly-in before launch
- Building level-up growth, construction shimmer, idle glow
- Lightweight particles (level-up, XP orbs, rain, focus aura)
- Resource counter animation; search re-layout
- Optional haptics later

### 3.3 Gamification & story

- UsageStats → Power / Focus / Gold; Scrap from organization
- Visual building upgrades tied to Room levels
- Chapters with map markers, rewards, happiness/decay feedback
- In-city agents: Mayor, Story Weaver, Architect, Guardian, Chronicler

### 3.4 Realme P2 Pro

- Prefer `graphicsLayer` / `Animatable`; low overdraw
- Pause heavy loops in Power Save / screen-off
- Portrait-first, gesture-nav friendly; ColorOS quirks documented in OPS

---

## 4. Feature matrix

| Category | Feature | Priority | Phase | Tech notes |
|----------|---------|----------|-------|------------|
| Background | Parallax + day/night | High | 2 | Compose Canvas layers |
| Background | Weather particles | Medium | 3 | Lightweight emitter |
| Animations | Camera fly-in on tap | High | 2 | Compose transform |
| Animations | Building level-up + particles | High | 2 | Canvas + emitter |
| Animations | Ambient traffic/lights | Medium | 3 | Low-frequency timers |
| Progression | UsageStats XP → resources | High | 2 | WorkManager + UsageStats |
| Progression | Visual building upgrades | High | 2 | Room level + redraw |
| Story | Chapters 1–3 branching | High | 3 | JSON + later AI |
| AI agents | 5 in-city agents | Medium | 4 | Hybrid local/VPS |
| Polish | Haptics + optional sound | Low | 4 | HapticFeedback |
| Performance | Battery/thermal modes | High | 2 | PowerManager |
| Performance | Overdraw / profiler | High | Ongoing | On-device Profiler |

---

## 5. Phased roadmap

### Phase 1 — MVP City Shell — v0.1.0 ✅

- Branch: `feature/mvp-city-shell`
- Deliverable: Working isometric launcher with basic visuals
- Release: https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.1.0-mvp

### Phase 2 — Living City & Progression — v0.2.0 “Awakening” (merged to main · device gate open)

**Branch:** merged via [PR #1](https://github.com/sivaram311/forgecity-launcher/pull/1) → `main` (`d1f8b09`)  
**versionName:** `0.2.0-awakening-dev` · **Prerelease:** `v0.2.0-awakening-dev`  
**Parallel plan:** [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md)  
**Merge note:** User-directed waiver of #16 E2E for merge only — annotated `v0.2.0` still requires device GO.

| Wave 1 item | Status |
|-------------|--------|
| Day/night parallax sky + night glows | Landed (dev) |
| Camera fly-in + double-tap recenter | Landed (dev) |
| AnimationBudget (power-save gate) | Landed (dev) |
| UsageStats XP → resources + WorkManager | Landed (dev) |
| Building levels from launches | Landed (dev) |
| Realme P2 Pro E2E | **PENDING** — blocks `v0.2.0` tag |

| Wave 2 item | Status |
|-------------|--------|
| Level-up particle burst | Landed (dev) |
| Animated resource counters | Landed (dev) |
| Harvest debounce (1h gate) | Landed (dev) |
| Room-backed building levels (`building_stats`, DB v2) | Landed (dev) |
| Seed-reset bug fix (idempotent seeding) | Landed (dev) |
| Chapter 2–3 quest stubs | Landed (dev) |

- Deliverable: v0.2.0 “Awakening” — City feels alive and responsive to habits
- **Gate:** Realme P2 Pro E2E + crew SIGN-OFF

### Experimental dev train — v0.3.x

- `v0.3.0-forge-assistant-dev`: favorites, accurate hit testing, notification assistant/TTS
- `v0.3.1-forge-assistant-dev`: editable quiet hours + exact notification actions
- `v0.3.2-background-video-dev`: Media3 background layer, toggle/opacity,
  lifecycle and power gating, missing-asset fallback
- **Pending:** final H.264 MP4, seamless-loop visual test, Realme decoder,
  thermal, 120Hz, and battery evidence

### Phase 3 — Story & World Depth — v0.3.0 (~4–6 weeks)

- Chapters 1–3 branching + visual set pieces
- Weather + ambient life
- Quest markers on map
- Story Weaver (basic)
- **Gate:** device E2E + SIGN-OFF

### Phase 3.5 — Smart speech cascade — v0.4.6 “Gemini native audio” (in progress)

**Spec:** [GEMINI-SPEECH-CASCADE-SPEC.md](GEMINI-SPEECH-CASCADE-SPEC.md) · **Parallel:** streams G1–G4 in [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md)

| Item | Status |
|------|--------|
| `SMART_CASCADE`: Gemini **native audio** → Agent Portal → device TTS | ✅ source |
| Editable prompt template (`{appLabel}`, `{title}`, `{text}`, `{maxChars}`) | ✅ |
| Gemini API key + TTS model / voice / `ta-IN` in City Assistant | ✅ |
| `GEMINI_TAMIL` = fail-closed native audio | ✅ |
| TEST TTS runs full cascade | ✅ |
| In-app COPY LOG diagnostics (`0.6.1`) | ✅ |
| Realme E2E for cascade | ⏳ #16 |

Gemini audio is **opt-in** (notification text sent to Google when key is set).

### Grok consult — config panel + roadmap (2026-07-23)

**Source:** [design/GROK-LAUNCHER-CONFIG-ROADMAP.md](design/GROK-LAUNCHER-CONFIG-ROADMAP.md)  
**Model:** requested `grok-4-1-fast-reasoning` (API reported `grok-4.3`)

**Headline recommendations**
- Mode-gated settings cards (hide Gemini vs Portal fields by speech mode)
- Prompt validation / presets so rewrite prompts cannot be saved for GEMINI AUDIO
- Pin TEST TTS; collapse diagnostics
- Next train theme **0.7 Assistant Clarity**; defer Kongu rewrite→audio two-step to **0.8**

### Phase 3.6 — Assistant Clarity — v0.7.0 (landed)

**Theme:** Stable speech · versionCode **19** · `0.7.0-assistant-clarity-dev` — published

### Phase 3.7 — Realistic 3D House HOME — v0.8.0 (Wave 0/1 landed)

**Consult:** [design/GROK-3D-HOUSE-LAUNCHER-PLAN.md](design/GROK-3D-HOUSE-LAUNCHER-PLAN.md)  
**Parallel exec:** [design/3D-HOUSE-PARALLEL-EXEC.md](design/3D-HOUSE-PARALLEL-EXEC.md)  
**versionCode 20** · `0.8.0-3d-house-dev`

| Wave | Focus | Status |
|------|--------|--------|
| Wave 0 | FeatureFlag + video default off + versionCode 20 | ✅ |
| Wave 1 | Rooms/placement + HouseHomeSurface + AnimationBudget tiers | ✅ procedural Compose |
| Wave 2 | Filament/glTF + characters | pending |
| Wave 3 | Device perf / #16 | pending |

**Stack:** Wave 1 = procedural Compose house; Filament/SceneView remains long-term per Grok.

### Phase 4 — AI Agents & Polish — v0.4.0 “Symphony” (~6–8 weeks)

- Five in-city agents
- Haptics / subtle audio
- Icon pack / themes; ColorOS-friendly badges
- Performance + thermal pass
- **Gate:** device E2E + SIGN-OFF

### Phase 5 — Advanced & Release — v1.0.0 (~8–12 weeks)

- Optional Filament path (only if profiling allows)
- Backup / optional cloud sync
- Public beta / Play or sideload focus
- Custom districts
- **Gate:** promote evidence per machine CONSCIOUS rules

### Checkpoints

- Every phase ends with Realme P2 Pro E2E + development-crew SIGN-OFF
- Prefer tagged weekly builds when device lab is available
- Re-check performance budget after every major animation addition

---

## 6. Development crew

See `agents/crew/CREW.md` and `agents/roles/`.

| Role | Owns |
|------|------|
| Vision & Product | Roadmap priority, fun vs battery |
| UI / Animation | Camera, particles, transitions |
| Android Systems | UsageStats, PowerManager, ColorOS |
| Narrative Designer | Story bible, quest templates |
| AI Integration | In-city agents + VPS bridge |
| Data & Persistence | Room, migrations, backup |
| QA & Optimization | Battery/thermal/a11y gates |

**Suggested next crew task:** Realme P2 Pro E2E (#16) against
`v0.3.3-background-video-asset-dev` covering loop, thermal, and battery.

---

## 7. Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Adreno 710 jank | Low particle counts, pooling, early device profile |
| Battery drain | Animation budget + PowerManager gating |
| ColorOS quirks | Document in OPS; test gestures / recents / badges |
| Scope creep | Strict phase gates — polish before new features |

---

## 8. Success metrics

- Smooth 120 Hz with &lt; 5% frame drops in normal use
- Battery impact &lt; 3–5% extra vs stock launcher over 8-hour test
- Player feels “the city reacts to how I use my phone”
- ≥ 3 chapters with meaningful visual progression
- Positive feedback on atmosphere and animations

---

## 9. Immediate next actions

1. **P0 — Assistant Clarity (`0.7.0`):** finish mode-gated UI + masked keys wire;
   green unit/lint/assemble; fill APK SHA; tag prerelease.
2. **P0 device lab:** Realme P2 Pro E2E for chrome, video, DIRECT, Portal Tamil,
   Gemini cascade, and Clarity validation (CONSCIOUS #16).
3. **Tag:** annotated production tags only after Realme device GO.
4. **0.8 / later:** Kongu rewrite→audio two-step; weather / quest markers /
   in-city agents.

**Rejected for launcher:** Cloudflare Workers AI / GLM remote Worker integration.

**Shipped:** Portal Tamil (`0.4.0`–`0.4.2`), Gemini audio cascade (`0.4.3`–`0.5.1`),
UI polish (`0.5.0`), city 3D (`0.6.0`), diagnostics log (`0.6.1`).
**In flight:** Assistant Clarity (`0.7.0`).

---

## Related docs

| Doc | Purpose |
|-----|---------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Technical stack |
| [STORY-BIBLE.md](STORY-BIBLE.md) | Campaign outline |
| [OPS.md](OPS.md) | Build / device ops |
| [VERIFICATION.md](VERIFICATION.md) | Evidence record |
| [../agents/crew/CREW.md](../agents/crew/CREW.md) | Crew operating model |
