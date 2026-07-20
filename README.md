# ForgeCity Launcher

A story-driven isometric city home screen for Realme P2 / P2 Pro (ColorOS).
Apps become buildings. Habits rebuild districts. A neon city assistant can read
notifications aloud when you opt in.

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Branch:** `main` · version `0.4.7-pcm-playback-fix-dev`
**Latest prerelease:** [`v0.4.7-pcm-playback-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.7-pcm-playback-fix-dev)

## Download

```powershell
curl.exe -L -o forgecity-0.4.7-pcm-playback-fix-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.4.7-pcm-playback-fix-dev/forgecity-0.4.7-pcm-playback-fix-dev-debug.apk
Get-FileHash .\forgecity-0.4.7-pcm-playback-fix-dev-debug.apk -Algorithm SHA256
# expect C98727E5F1169E486193D6E3E1ADBF9D21AA646E76231EE841A85F5756B9B377
adb install -r .\forgecity-0.4.7-pcm-playback-fix-dev-debug.apk
```

## What works in 0.4.7-pcm-playback-fix-dev

- Fix: Gemini native audio **playback** (streamed AudioTrack + MediaPlayer WAV fallback)
- Gemini TTS (`gemini-3.1-flash-tts-preview`) — template → PCM → play
- Modes: OFF → DIRECT → PORTAL → GEMINI AUDIO → CASCADE
- Voice / language / prompt template config
- Separate ASSIST / SEARCH / DOCK / UI chips
- Diagnostics: `adb logcat -s ForgeCityTTS`
