# ForgeCity - Grok wall/character realism action plan

**Requested:** `grok-4-1-fast-reasoning`
**API reported:** `grok-4.3`
**Generated:** 2026-07-23
**Baseline tip:** `v0.10.4-white-screen-fix-dev` (vc26)
**Usage:** prompt=718 · completion=1051

---

## A. Current gaps (ranked 1-10)
1. Flat single-color walls lack base/trim/accent hierarchy  
2. No baseboards, chair rails or picture rails  
3. Windows are emissive quads without frames or reveals  
4. Doors are boxes without panels or casings  
5. Character is single-color block with no clothing layers  
6. No wall variation between rooms  
7. Lighting has no bounce or wall-specific tint  
8. Floors stop at walls with no transition molding  
9. No outlet/switch plates or cable drops on walls  
10. Scale feels toy-like due to missing architectural modules

## B. Design codes (copy-paste)
- Kitchen: base #E8DFD0, shadow #C9B89A, trim #3D2B1F, accent #8B5E3C  
- Living: base #D4C9B9, shadow #A89B85, trim #2F2A24, accent #5C4033  
- Hallway: base #CFC8BC, shadow #A99D8C, trim #3A3630, accent #6B5B4F  
- Office: base #D8D0C4, shadow #B3A68F, trim #2C2823, accent #4A3F35  
- Bedroom: base #E2D9CC, shadow #BFAE94, trim #3F2E26, accent #7A5C4A  
- Workshop: base #C8BFAF, shadow #958A75, trim #2A2722, accent #5C5245  
- Vault: base #B8B0A3, shadow #7D7668, trim #1F1C18, accent #3D3630  

Character roster:  
- Mayor: head:body 1:3.8, clothing #2F2A24 / #5C4033 / #8B5E3C, silhouette keywords: coat, collar, straight  
- Assistant: head:body 1:4.1, clothing #3A3630 / #6B5B4F / #A89B85, silhouette keywords: vest, sleeves, slim  
- NPC: head:body 1:3.6, clothing #2C2823 / #4A3F35 / #958A75, silhouette keywords: shirt, cuffs, stocky  

Molding/window/door modules: baseboard 0.12h Ã 0.02d, chair rail 0.08h at 0.9m, picture rail 0.06h at 2.1m, window frame 0.08w, door casing 0.10w, door panel 0.9Ã2.1m

## C. Action plan phases
Phase 0.10.5 Wall Architecture (MUST-SHIP)  
Python glTF generator tasks: add per-room vertex-colored wall strips (base 0â0.9m, shadow 0.9â2.1m, trim 2.1â2.4m), extrude 0.12m baseboard + 0.08m chair rail as separate meshes, add 0.06m picture rail, generate 0.08m window reveals and 0.10m door casings as boxes with 4-vertex bevels, output house_walls_0.10.5.glb. Kotlin: load new GLB, replace wall nodes, keep existing vertex colors for Adreno.

Phase 0.10.6 Character Fidelity  
Python glTF: split char_idle into torso/legs/head, add 2-color clothing layers (body #2F2A24, trim #5C4033), simple collar extrusion 0.03m, output char_mayor_0.10.6.glb, char_assist_0.10.6.glb, char_npc_0.10.6.glb. Kotlin: load per-character GLB, replace scale-pulse with 0.02m vertical bob at 1.2Hz on torso only.

Phase 0.11 Atmosphere polish (defer)  
Add subtle per-wall AO vertex color pass, 16 dust motes per room, outlet plates 0.08Ã0.12m, cable drops as 2-segment lines, blue-hour directional tint shift.

## D. DO-NOT list for Adreno 710
No normal maps, no PBR textures >512, no dynamic lights >2, no skinned meshes, no alpha on walls, no >4k vertices per room, no post-process bloom, no real-time shadows on characters.

## E. Success criteria
Walls show three distinct horizontal bands + 0.12m baseboard per room; doors have 0.10m casings; characters display two-tone clothing with 0.03m collar; bob animation visible on speech; total GLB size <120KB; 60fps on Realme P2 Pro.
