# ForgeCity Launcher

**Latest:** [`v0.17.0-assistant-character-rigged-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.17.0-assistant-character-rigged-dev) · vc **37**

## Download
```powershell
curl.exe -L -o forgecity-0.17.0-assistant-character-rigged-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.17.0-assistant-character-rigged-dev/forgecity-0.17.0-assistant-character-rigged-dev-debug.apk
Get-FileHash .\forgecity-0.17.0-assistant-character-rigged-dev-debug.apk -Algorithm SHA256
# expect 6966693C31D32EAC160AB4E2E59127BAE9A0E9EE4CD209B3BB346013F32C00C3
```

## 0.17.0 (Assistant character: real rigged/animated glTF)
- User feedback: the Assistant character looked "not realistic" (raw Kotlin-puppeteered capsule primitives, no real animation clips)
- New `tools/generate_rigged_character.py`: a real glTF rig (7-node hierarchy: body/torso/head/armL/armR/legL/legR, well under the documented 18-joint Adreno budget) with 3 baked animation clips (Idle/Talk/Wave), PBR skin/hair/cloth materials, and the existing face photo embedded as a real texture — motion ported 1:1 from `HouseHumanoidPose.compute()` so it looks/moves the same, just via a real animation system instead of per-frame Kotlin math
- `AssistantCharacterScreen` now plays this via SceneView's `ModelNode`/`Animator` (`animationName`/`animationLoop` driven by the current action)
- **Safe fallback:** if the rigged asset fails to load, silently falls back to the previous procedural character rather than a blank/white screen — matches this app's established fallback pattern elsewhere
- Not yet device-confirmed — this touches this app's historically most bug-prone area (Filament/glTF loading); please report whether you see the new rigged character or the fallback

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
