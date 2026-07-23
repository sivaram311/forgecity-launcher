# ForgeCity - Grok white-screen diagnosis

**Requested:** grok-4-1-fast-reasoning · **API:** grok-4.3 · **2026-07-23**

---

1. Ranked root causes (white + orbit works)
   1. Overbright: combined sun (14k) + IBL (12k) + exposure (1.15) saturates to white before tonemap.
   2. Skybox/environment intensity or color tint forcing white clear.
   3. Bloom threshold too low + high intensity â full-screen glow.
   4. Emissive materials in procedural GLB with no exposure compensation.
   5. Fog + high ambient scattering producing uniform white.
   6. Missing or identity tonemapping + linear output to sRGB surface.

2. Most likely
   Overbright from sun + IBL + exposure. Adreno 710 + Filament default tonemap clips hard.

3. Exact fix (SceneView 4.15)
   ```kotlin
   sceneView.scene.skybox = null // or low-intensity
   directionalLight.intensity = 5000f
   ibl.indirectLight.intensity = 3000f
   sceneView.camera.exposure = 1.5f   // higher = darker
   bloomOptions.threshold = 2f
   bloomOptions.strength = 0.3f
   ```
   Rebuild environment with `iblPrefilter` at lower intensity.

4. Safe day numbers
   Sun 4000â6000 lux, IBL 2000â4000 lux, exposure 1.6â2.0. Night: sun 500, IBL 800, exposure 2.2.

5. Debug checklist
   - Disable all lights â still white? (skybox/tonemap)
   - Set `sceneView.camera.exposure = 10f` â if dark appears, exposure/lights.
   - `sceneView.renderer.clearOptions.clearColor = float4(0.2,0,0,1)` â red confirms overbright.
   - Log `engine.createRenderableManager().getPrimitiveCount` and material emissive values.
