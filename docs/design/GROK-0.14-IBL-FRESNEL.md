# Grok / Lead — 0.14 HDR IBL + fresnel stand-ins (gap #8)

**Ship:** `0.14.0-ibl-fresnel-dev` · vc **32**  
**Device target:** Realme P2 Pro · Adreno 710

## Caps (do not exceed)

| Knob | Cap | Why |
|------|-----|-----|
| HDR | 256×128 Radiance `house_ibl_256.hdr` (~128 KB) | No 4K IBL on Adreno |
| Day IBL intensity | ~1800 (1200–2000) | Washed scene / thermal |
| Night IBL | ~900 | Mood without crush |
| Rim DIRECTIONAL | ~1200 lux | Fresnel stand-in |
| Exposure | `setExposure(aperture, shutter, iso)` only | Bare EV → white screen |
| Custom filamat | **No** | Ship-fast; reflectance instead |

## What landed

1. Asset `assets/filament/house_ibl_256.hdr` loaded via `EnvironmentLoader.createHDREnvironment` (fallback to neutral IBL).
2. `FilamentHouseIbl` intensity + reflectance constants.
3. Humanoid skin/cloth/hair reflectance; lit glass + corner rims (was unlit).
4. Grazing rim directional light.

## Not in this ship

Custom `.filamat` fresnel shaders, Mixamo, 2K textures, SSR, #16 Realme soak.
