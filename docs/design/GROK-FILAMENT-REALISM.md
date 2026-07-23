# ForgeCity - Grok Filament realism vs Production House

**Requested model:** `grok-4-1-fast-reasoning`
**API reported model:** `grok-4.3`
**Generated:** 2026-07-23
**Source:** xAI Chat Completions API (`XAI_API_KEY`)
**Scope:** Close realism gap toward Production House mood on Filament/Adreno 710
**Usage:** prompt=520 · completion=596 · reasoning=508

---

1) Gap analysis (ranked)
1. No atmospheric fog / depth haze
2. Flat IBL + single directional lacks blue-hour color temp and interior bounce
3. Materials are pure vertex color, zero roughness variation or micro-detail
4. No window-driven interior key light or emissive planes
5. Missing subtle post-processing (tonemap, slight bloom, color grading)
6. No small geometric props (cables, trim, floor boards)
7. Exposure locked; scene feels uniformly lit vs filmic contrast
8. Skybox is solid color instead of low-res HDR gradient

2) MUST-SHIP 0.10.2 (<1 day)
- Add Filament `Fog` component (linear, density 0.015) to SceneView.
- Replace solid sky with 4Ã4 px procedural gradient texture as IBL source.
- Add second low-intensity directional light (warm fill) from window direction.
- Switch render quality to `QUALITY` and enable `COLOR_GRADE` + mild bloom (threshold 2.0).
- In Python generator: output per-face roughness in vertex color alpha; add 8 thin box âcablesâ + 3 window planes with emissive factor 0.6.
- Set `Camera.exposure` dynamically; ship with two presets (day/night).

3) Exact numbers (Adreno 710 safe)
Day: sun intensity 80k lux, 5800 K, IBL 25k, exposure 11.0, fog density 0.012 linear, sky top #4a6fa5 bottom #1e2a44.
Night: sun 2k lux, 4500 K, IBL 3k, exposure 14.0, fog 0.008, sky top #0f1a2e bottom #05080f.
Interior warm: add 3000 K fill light intensity 12k from window normal.

4) Asset generation plan (Python glTF)
- Add vertex color alpha channel for roughness (0.3 floor, 0.7 walls, 0.85 cables).
- Generate 3 thin rectangular boxes as cables with dark vertex color.
- Create 2â3 window quads with baseColor black + emissive 0.6 warm.
- Fake wood grain: alternate two brown vertex colors on floor faces in 0.2 m stripes.
- Add 4 low-poly props (chair, table, lamp) using only boxes + cylinders, vertex-colored.
- Output one extra UV set for future lightmap if needed; keep total <30 KB.

5) Explicitly DEFER
- Real-time shadows (cost on 710)
- Volumetric dust / particles
- Screen-space reflections
- Any custom Filament materials beyond KTX IBL + vertex color
- Dynamic GI or multiple bounce lights

6) Version name
0.10.2 BlueHourInterior
