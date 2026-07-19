# ForgeCity Roadmap

**Goal:** Transform the Realme P2 Pro home screen into a deeply immersive,
game-like living city with rich backgrounds, fluid animations, progression, and
story — while staying performant on Snapdragon 7s Gen 2 + Adreno 710 + 120 Hz.

**Repo:** https://github.com/sivaram311/forgecity-launcher  
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`  
**Last updated:** 2026-07-19 (Phase 2 Wave 1 landed on `feature/phase-2-awakening`)

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

### Phase 2 — Living City & Progression — v0.2.0 “Awakening” (in progress)

**Branch:** `feature/phase-2-awakening` · versionName `0.2.0-awakening-dev`  
**Parallel plan:** [PARALLEL-EXECUTION.md](PARALLEL-EXECUTION.md) (Wave 1)

| Wave 1 item | Status |
|-------------|--------|
| Day/night parallax sky + night glows | Landed (dev) |
| Camera fly-in + double-tap recenter | Landed (dev) |
| AnimationBudget (power-save gate) | Landed (dev) |
| UsageStats XP → resources + WorkManager | Landed (dev) |
| Building levels from launches | Landed (dev) |
| Realme P2 Pro E2E | **PENDING** — blocks `v0.2.0` tag |

- Deliverable: v0.2.0 “Awakening” — City feels alive and responsive to habits
- **Gate:** Realme P2 Pro E2E + crew SIGN-OFF

### Phase 3 — Story & World Depth — v0.3.0 (~4–6 weeks)

- Chapters 1–3 branching + visual set pieces
- Weather + ambient life
- Quest markers on map
- Story Weaver (basic)
- **Gate:** device E2E + SIGN-OFF

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

**Suggested next crew task:** Implement Phase 2 camera fly-in + building
level-up animation system with battery awareness.

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

1. Keep `docs/ROADMAP.md` as SoT (this file) and link from README.
2. Merge `feature/mvp-city-shell` → `main` after Reviewer GO (polish complete).
3. Kick Phase 2 with UI/Animation + Android Systems roles.
4. Prototype parallax background on a Phase 2 branch.
5. Establish on-device Canvas performance baseline on Realme P2 Pro.
6. Implement first UsageStats → resource generation path.

---

## Related docs

| Doc | Purpose |
|-----|---------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Technical stack |
| [STORY-BIBLE.md](STORY-BIBLE.md) | Campaign outline |
| [OPS.md](OPS.md) | Build / device ops |
| [VERIFICATION.md](VERIFICATION.md) | Evidence record |
| [../agents/crew/CREW.md](../agents/crew/CREW.md) | Crew operating model |
