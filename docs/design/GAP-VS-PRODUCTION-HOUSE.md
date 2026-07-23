# ForgeCity vs Production House — deep realism gap

**Baseline:** ForgeCity `v0.10.5-wall-character-dev` (vc27)  
**Reference:** Production House web R3F tip (Grok 0.1 design + 0.2 upgrade)  
**Question:** What must improve so *characters* and *finishing* feel like Production House?  
**Generated:** 2026-07-23 (code inspection + prior Grok plans)

---

## 0. Honest framing

Production House does **not** use photoreal glTF humans either. It uses **procedural capsule/sphere humanoids** with **jointed animation**, **MeshStandardMaterial** (roughness per layer), **day-cycle lighting**, **fresnel/emissive facade finishing**, **tube cables**, and **Points dust**.

ForgeCity 0.10.5 is closer on “colored boxes in a room,” but still far on **silhouette language, motion, material response, and scene finishing density**.

| Layer | Production House (today) | ForgeCity 0.10.5 (today) |
|-------|--------------------------|---------------------------|
| Characters | Capsule limbs + sphere head/hair; skin/hair/top/bottom materials | Box stacks; 2–3 vertex colors; static GLB |
| Motion | Joint graph: walk / talk / point / clap / camera / boom / phone | Whole-body Y bob only |
| Materials | `MeshStandardMaterial` roughness 0.7–0.9; emissive pulse | Vertex color + occasional emissive glass |
| Lighting | Day clock + sun color lerp + hemisphere + fills | Dual directional + fog + fixed exposure |
| Finishing | Fresnel edge strips, window pulse, tube cables, C-stands, apple boxes, sandbags | Wall bands + rails + frames; thin box cables; cube dust |
| Life | Take timeline drives actions | Idle only; speech = slight lift |

---

## 1. Characters — what to improve (priority order)

### P0 — Body language (biggest “not PH” gap)
Production House `Humanoid.tsx` has **separate transforms** for body, head, L/R arms, L/R legs and an **action state machine**.

ForgeCity needs the same idea on Filament (SceneView nodes or code-driven transforms), not a single rigid GLB:

| Missing | PH has | Why it reads “real” |
|---------|--------|---------------------|
| Arm/leg swing | walk / idle micro-motion | Humans aren’t statues |
| Head look | idle yaw + talk nod | Focus / attention |
| Role actions | talk, point, phone, walk | Purpose in the room |
| Gender/scale | female scale 0.92 | Variety |
| Skin vs clothing | separate materials | Soft vs fabric |

**Improve to:** Filament node hierarchy `root → hips → torso → head / armL / armR / legL / legR` with Kotlin `onFrame` action enum mirroring PH (`idle`, `walk`, `talk`, `wave`). Keep low poly capsules (generate via Python or SceneView Capsule/Sphere nodes).

### P1 — Silhouette fidelity
PH uses **capsules + spheres** (rounded). ForgeCity uses **boxes** (toy).

**Improve to:** regenerate characters as capsule/sphere stack (or SceneView geometry nodes): head sphere, hair cap, torso capsule, limb capsules, hand spheres — with PH-like proportions (`hipY≈0.92`, head radius ≈0.13).

### P2 — Costume layers & roster
PH crew: director / AD / DP / etc. with **skin + hair + top + bottom** hex from tokens.

ForgeCity: mayor/assist/npc two-tone boxes.

**Improve to:**
- Explicit materials: skin `#D4A882`, hair `#1A120C`, top/bottom from role
- Shoes darker block; optional collar/vest as separate mesh (already started)
- Expand roster only after motion works (don’t add more statues)

### P3 — Spatial storytelling
PH talent **walks a path** on the take timeline; crew occupy marks.

ForgeCity characters stand in room cells forever.

**Improve to:** short room patrols / sit-at-desk / face-window loops tied to `AssistantHouseBridge` and time-of-day.

---

## 2. Finishing — what to improve (priority order)

### P0 — Material response (walls/floors/props)
PH: roughness + metalness + emissive intensity animation.  
FC: flat vertex color bands (better than before, still “painted foam”).

**Improve to (Adreno-safe):**
- Per-material roughness in glTF PBR (floor 0.85–0.95 wood, trim 0.4–0.55, glass 0.2)
- Soft **emissive window pulse** (PH: 4s sine, rim cool `#7ec8e3` / warm `#e8b86d`)
- Optional **edge rim strips** on outer wall corners (PH fresnel stand-in without custom shaders)

### P1 — Lighting finishing (mood, not just brightness)
PH `LightsRig`: **continuous day cycle**, sun color lerp, **hemisphere** sky/ground, moving sun azimuth.

FC: static day/night presets.

**Improve to:**
- Slow `tDay` 0…1 loop (e.g. 120s) driving sun color/intensity (reuse PH stop table scaled for interior)
- Hemisphere / ambient ground bounce tint `#3a3530`
- Keep photographic exposure; never bare EV float

### P2 — Cable & prop language
PH: **CatmullRom tube cables** with droop; C-stands; apple boxes; sandbags; boom; slate.

FC: axis-aligned thin boxes as “cables.”

**Improve to:**
- Tube / multi-segment drooping cable paths along hallway→office
- 2–3 recognizable prop types per room (not more generic boxes): lamp (done), plant, framed picture, laptop on desk, kettle in kitchen

### P3 — Dust / air
PH: `THREE.Points` 120–200 particles, wind, life cycle, blue-hour boost.  
FC: 32–64 CubeNodes — visible but “chunky snow.”

**Improve to:** true point/sprite dust if Filament supports cheap particles; else smaller unlit spheres + higher count only on HIGH budget.

### P4 — Architectural micro-finishing
Still weak vs a finished interior (even after 0.10.5 bands):

| Item | Status | Need |
|------|--------|------|
| Wall bands + rails | Landed | Keep; vary accent per room more |
| Window frame | Landed | Add sill + mullion cross |
| Door casing | Landed | Add panel inset / door leaf thickness |
| Floor–wall shoe molding | Partial | Continuous cove / quarter-round |
| Ceiling | Missing | Thin ceiling plane + light tray |
| Pictures / switches | Missing | Flat framed quads + outlet plates |
| Scuffs / AO | Missing | Vertex darkening in corners (fake AO) |

### P5 — Audio / haptics (PH finishing feel)
PH has clap SFX + mute. Optional later for ForgeCity (door soft click, assistant cue) — not visual-critical.

---

## 3. Ranked improvement backlog (ship order)

| # | Workstream | Target feel | Effort | Suggested version |
|---|------------|-------------|--------|-------------------|
| 1 | **Jointed humanoid + actions** (capsule limbs, idle/talk/walk) | Characters like PH | L | **0.11.0 LANDED** |
| 2 | **Day-cycle lights + hemisphere bounce** | Living air like PH lot | M | **0.11.0 LANDED** |
| 3 | **Window emissive pulse + corner rim strips** | Facade finishing | S | **0.11.0 LANDED** |
| 4 | **Droop cables + 1 hero prop set per room** | Set dressing | M | 0.11.1 |
| 5 | **Points/sphere dust** (replace cube motes) | Air | S | 0.11.1 |
| 6 | Ceiling + picture frames + corner AO | Interior finish | M | 0.11.2 |
| 7 | Room patrols / sit loops | Life | M | 0.12 |
| 8 | Custom Filament fresnel / HDR IBL | Photoreal ceiling | L / defer | 0.12+ |

**Do not prioritize:** Mixamo/skinned meshes, 2K textures, SSR, many dynamic lights — Adreno 710 + APK size.

---

## 4. Success criteria (“feels like Production House”)

User should be able to say yes to:

1. Characters have **round limbs** and **moving arms/head**, not rigid boxes.  
2. At least **talk / idle / walk** actions are readable from the orbit camera.  
3. Windows **breathe** (emissive pulse); corners catch a cool/warm rim.  
4. Light **changes over a minute** (not a hard day/night flip only).  
5. Cables read as **curves on the floor**, not Lego planks.  
6. Dust is **soft motes**, not bouncing cubes.  
7. Still **≥30 fps** on Realme P2 Pro with AnimationBudget MEDIUM.

---

## 5. Bottom line

**Characters:** the missing piece is not “more colors” — it is **PH’s jointed Humanoid + action graph**.  
**Finishing:** the missing piece is not “more wall bands” — it is **material response (roughness/emissive), day-cycle light, curved cables, and soft dust**.

0.10.5 closed the “empty painted room” gap. Closing the “Production House” gap starts at **0.11 jointed characters + living light**.
