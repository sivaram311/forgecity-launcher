# ForgeCity Launcher

**Latest:** [`v0.16.1-assistant-character-flicker-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.16.1-assistant-character-flicker-fix-dev) · vc **36**

## Download
```powershell
curl.exe -L -o forgecity-0.16.1-assistant-character-flicker-fix-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.16.1-assistant-character-flicker-fix-dev/forgecity-0.16.1-assistant-character-flicker-fix-dev-debug.apk
Get-FileHash .\forgecity-0.16.1-assistant-character-flicker-fix-dev-debug.apk -Algorithm SHA256
# expect E39AF6AEC4CB05545BF357EF89E765A895936C4B403A6391C0EAA07CD89D969D
```

## 0.16.1 (Assistant character flicker/duplicate-avatar hotfix)
- Reported on-device: multiple ghost copies of the character + flickering in Assistant mode
- Root cause: `AssistantCharacterScreen`'s Filament surface was `isOpaque = false` (to blend a Compose gradient behind it) — transparent `TextureSurface` doesn't reliably clear the previous frame on some Adreno GPUs, which reads as ghosted duplicate frames of the animated character
- Fix: surface is `isOpaque = true` (matches the proven `HouseFilamentSurface` pattern); the backdrop is now a Filament skybox color instead of a Compose blend-through

## 0.16.0 (Assistant character home mode)
- New third **Home mode** (Settings → tap "Home mode" to cycle City → House → Assistant → City)
- Assistant mode: one full-screen animated character (jointed capsule humanoid, reused from the house work)
- Greets you with the **plain device TTS** (`AssistantTtsEngine.speakDirect`, never the Gemini/Portal cascade) when you return to the home screen, debounced ~20s
- Tap the character for a short reaction line + wave gesture
- Daily-open streak with escalating callouts at 3/7/30 consecutive days
- Realme #16 physical E2E still **PENDING** — see `docs/VERIFICATION.md`

## 0.15.1 (face front fix)
- Face card moved to **front of head** (−Z + 180° yaw); was on the back in 0.15.0

## 0.15.0 (shared face card)
- All house humanoids show **`faces/siva.png`** (256×256) as a face card

## 0.14.0 (gap #8 — IBL + fresnel stand-ins)
- Soft **256×128 HDR IBL** · reflectance + rim light
