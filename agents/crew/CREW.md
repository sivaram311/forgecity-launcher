# ForgeCity development crew

Multi-agent build crew for ForgeCity. Run locally (Cursor/Antigravity) or on
VPS. Product code stays under `E:\MyWorkspace\sandbox\forgecity-launcher`.

## Roles

| Role | Owns | Hire first for |
|------|------|----------------|
| Vision & Product | MVP scope, fun vs battery | Phase prioritization |
| Narrative Designer | Story bible, quests, tone | Chapter text |
| UI / Animation | Iso canvas, camera, particles | Compose polish |
| Android Systems | Manifest, PackageManager, ColorOS, perf | Device quirks |
| AI Integration | In-city agents + VPS bridge | Phase 4 |
| Data & Persistence | Room schema/migrations/backup | Save integrity |
| QA & Optimization | Battery/thermal/a11y on P2 Pro | Device gates |

Detailed prompts: `agents/roles/`.

## First pipeline (MVP shell — done)

1. Android Systems — HOME manifest + app catalog  
2. UI/Animation — isometric canvas + gestures  
3. Data — Room seed + story briefing  
4. Narrative — Chapter 1 quest stubs  
5. QA — OPS checklist once SDK/device available  

## Phase 2 pipeline (Awakening)

SoT: `docs/ROADMAP.md` + `docs/PARALLEL-EXECUTION.md`.  
Branch: merged to `main` via PR #1 · Prerelease: `v0.2.0-awakening-dev`

| Wave | Status | Notes |
|------|--------|-------|
| Wave 1 | ✅ on `main` | day/night, fly-in, UsageStats/WorkManager, launch levels |
| Wave 2 | ✅ on `main` | particles, animated counters, Room levels, quest stubs, debounce |
| Room Migration(1,2) | ✅ on `main` | destructive fallback removed |
| PR #1 merge | ✅ MERGED | user E2E waiver for merge; not E2E PASS |
| Device E2E (#16) | ⏳ **NEXT** | blocks annotated `v0.2.0` |

**Hire next:** QA & Optimization (P2 Pro lab) when device is available.

## In-launcher living agents (Phase 4+)

Mayor · Story Weaver · Architect · Scout · Guardian · Chronicler — see
`docs/ARCHITECTURE.md` and `docs/ROADMAP.md`. Not implemented yet.
