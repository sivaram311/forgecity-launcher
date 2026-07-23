# ForgeCity Launcher

**Latest:** [`v0.14.0-ibl-fresnel-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.14.0-ibl-fresnel-dev) · vc **32**

## Download
```powershell
curl.exe -L -o forgecity-0.14.0-ibl-fresnel-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.14.0-ibl-fresnel-dev/forgecity-0.14.0-ibl-fresnel-dev-debug.apk
Get-FileHash .\forgecity-0.14.0-ibl-fresnel-dev-debug.apk -Algorithm SHA256
# expect 534386BC68F1A77381E8ACFE17B8BF8EB98D154F4FDDD8FFCEBD28C2C276C4D1
```

## 0.14.0 (gap #8 — IBL + fresnel stand-ins)
- Soft **256×128 HDR IBL** (`house_ibl_256.hdr`) · day ~1800 / night ~900
- Skin/cloth/glass **reflectance** + grazing **rim** directional (no custom filamat)
- Grok caps: `docs/design/GROK-0.14-IBL-FRESNEL.md`

## 0.13.0 (TTS catalog)
- Gemini **model** + **voice** dropdowns (30 prebuilt voices)
- **Random / Random female / Random male** (resolved each speak)
- Named **prompt template library** (save as / pick / delete); builtins seeded

## 0.12.0 (open roof + patrols)
- Open-roof dollhouse (ceilings unsealed) + patrol/sit loops

## 0.11.2 (set dressing)
- Droop cables, hero props, sphere dust, pictures + AO
