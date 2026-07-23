# ForgeCity - Grok Filament SceneView lighting / materials

**Requested model:** `grok-4-1-fast-reasoning` (Grok 4.1 Fast reasoning)
**API reported model:** `grok-4.3`
**Generated:** 2026-07-23
**Source:** xAI Chat Completions API (`XAI_API_KEY`)
**Scope:** Day/night light intensities, sky colors, directional euler, exposure, speech-pulse scale for Adreno 710 mid-range
**Usage:** prompt=303 · completion=122 · reasoning=885

---

## Numbers-only tuning table

| Mode | SunIntensity | IndirectLightIntensity | SkyColorR | SkyColorG | SkyColorB | DirectionalLightEulerX | DirectionalLightEulerY | DirectionalLightEulerZ | Exposure | SpeechPulseScale |
|------|-------------:|-----------------------:|----------:|----------:|----------:|-----------------------:|-----------------------:|-----------------------:|---------:|-----------------:|
| Day | 100000 | 40000 | 0.65 | 0.8 | 1.0 | -50 | 110 | 0 | 13 | 1.2 |
| Night | 800 | 8000 | 0.08 | 0.08 | 0.25 | -35 | 110 | 0 | 9 | 0.9 |

## MUST-SHIP rules

- Lock IBL to 256², disable specular reflections
- Disable shadows, SSAO, bloom on Adreno 710
- Use single static directional light only
- Clamp exposure 8–14, no auto-exposure
- Pre-bake all materials, metallic ≤0.4
