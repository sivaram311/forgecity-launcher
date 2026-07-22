# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md`

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.6.1-tts-error-log-dev` · versionCode 18 |
| Latest release | [`v0.6.1-tts-error-log-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.1-tts-error-log-dev) |
| APK SHA-256 | `BE2F45E5EF46F7CD11F4B3CBB0A03A3CD0DA49E8889E7AA0A054699600568383` |
| Prior tip | [`v0.6.0-city-3d-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.6.0-city-3d-dev) |

## Speech diagnostics log (0.6.1)

- Assistant settings → **Speech diagnostics log** textbox appends safe `ForgeCityTTS` events
- **COPY LOG** → paste into agent chat; **CLEAR** resets the ring buffer (max 200 lines)
- Still never logs API keys, notification title/body, or rewrite/TTS text
- Logcat unchanged: `adb logcat -s ForgeCityTTS`

## Now → next

| Now | Next |
|-----|------|
| 0.6.1 diagnostics log published | Device: TEST TTS → COPY LOG → paste if Gemini fails |
| Realme E2E (#16) PENDING | Blocks production tags |

Session: 2026-07-22.
