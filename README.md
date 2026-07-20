# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.4.6-gemini-native-audio-dev`
**Latest prerelease:** build locally / publish as `v0.4.6-gemini-native-audio-dev`

## Download

Latest debug prerelease (sideload):

- Releases: https://github.com/sivaram311/forgecity-launcher/releases
- Tag target: `v0.4.6-gemini-native-audio-dev`

This is **debug-signed**. Realme P2 Pro E2E (#16) is still pending for any annotated production tag.

```powershell
curl.exe -L -o forgecity-0.4.6-gemini-native-audio-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.4.6-gemini-native-audio-dev/forgecity-0.4.6-gemini-native-audio-dev-debug.apk
Get-FileHash .\forgecity-0.4.6-gemini-native-audio-dev-debug.apk -Algorithm SHA256
# expect 1644ED69CC47074932E327170F998D9593ED73A1CEE0AD0FB7B34A2F9C92BC6A
adb install -r .\forgecity-0.4.6-gemini-native-audio-dev-debug.apk
```

## What works in 0.4.6-gemini-native-audio-dev

- **Gemini native audio TTS** (`gemini-3.1-flash-tts-preview`) — template → PCM → `AudioTrack`
- Modes: OFF → DIRECT → PORTAL → **GEMINI AUDIO** → **CASCADE** (Audio→Portal→device)
- Configurable TTS model, voice (`Kore`), language (`ta-IN`)
- Editable audio prompt template with `{appLabel}` `{title}` `{text}` `{maxChars}`
- Separate chips: **`ASSIST`**, **`SEARCH`**, **`DOCK`** (+ `UI`)
- Custom editable **TEST TTS** text field
- Portal Tamil rewrite remains cascade tier 2
- Saved API key visible in config for device setup (encrypted at rest)
- Safe terminal diagnostics via `adb logcat -s ForgeCityTTS`; keys and message
  content are never logged
- Immersive city-first launcher: chrome hidden by default; 48 dp `UI +` / `UI −` chip
- Persisted speech mode with legacy preference migration
