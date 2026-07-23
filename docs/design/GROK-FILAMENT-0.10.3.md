# ForgeCity - Grok 0.10.3 next realism slice

**Requested:** grok-4-1-fast-reasoning · **API:** grok-4.3 · **2026-07-23**

---

1) Shadows: In SceneView 4.15, gate via `if (sceneView.animationBudget == AnimationBudget.HIGH) { lightManager.setShadowOptions(light, ShadowOptions(2048, 4, 0.001f, 0.1f, true)); view.setShadowingEnabled(true); }` else disable. Adreno 710 safe at 30fps.

2) Dust motes: 64 instanced GLB nodes (low-poly spheres, vertex color only). Animate via TransformManager translation + small random rotation per frame. No custom shaders; reuse existing unlit material.

3) Atmosphere: Add 8 emissive window planes pulsing 0.6-1.0 intensity at 0.8Hz sine; 12 thin cable cylinders with high roughness + low specular highlight for glint.

4) DO-NOT: No SSR, no custom materials, no dynamic GI, no >128 nodes, no per-frame shader recompiles.

5) version name: 0.10.3-adreno-dust
