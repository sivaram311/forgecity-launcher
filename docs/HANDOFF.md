# Handoff — ForgeCity Launcher

**Repo:** https://github.com/sivaram311/forgecity-launcher (public)
**Local:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`
**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`
**Standing rules:** `E:\MyAgent\workflow\CONSCIOUS.md` (Reviewer GO before push, Realme E2E before production tags, activity log)

## Current tip

| Field | Value |
|-------|-------|
| versionName | `0.4.7-pcm-playback-fix-dev` · versionCode 14 |
| main HEAD | `7eaa387` |
| Latest release | [`v0.4.7-pcm-playback-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.7-pcm-playback-fix-dev) (prerelease, debug) |
| APK SHA-256 | `C98727E5F1169E486193D6E3E1ADBF9D21AA646E76231EE841A85F5756B9B377` |

## Read first

| Doc | Why |
|-----|-----|
| [ROADMAP.md](./ROADMAP.md) | Phases; P1 = Gemini native audio → Portal → DIRECT |
| [GEMINI-SPEECH-CASCADE-SPEC.md](./GEMINI-SPEECH-CASCADE-SPEC.md) | Speech cascade product spec |
| [OPS.md](./OPS.md) | Download, grants, on-device setup, diagnostics |
| [VERIFICATION.md](./VERIFICATION.md) | Per-version test/lint/assemble evidence |
| `agents/hires/SIGN-OFF-*.md` | Reviewer GO per ship |

## Speech architecture (0.4.7)

Modes cycle: `OFF` → `DIRECT` → `PORTAL தமிழ்` → **`GEMINI AUDIO`** (fail-closed) → **`CASCADE`** (Gemini audio → Portal → device TTS).

- **Gemini native audio TTS:** `gemini-3.1-flash-tts-preview`, `responseModalities:["AUDIO"]`, `speechConfig` voice (`Kore`) + `ta-IN`. Returns L16 PCM @ 24 kHz.
- **Playback:** `AssistantTtsEngine.playPcm` → streamed `AudioTrack` (MODE_STREAM); falls back to `MediaPlayer` + temp WAV. `PcmAudioNormalizer` handles raw L16 or RIFF/WAV.
- **Portal tier 2:** unchanged Tamil rewrite → device Tamil TTS.
- **DIRECT tier 3:** `NotificationSpeechFilter.spokenLine` → device locale TTS.

Key files: `assistant/gemini/GeminiAudioTtsClient.kt`, `assistant/gemini/GeminiRewriteClient.kt` (legacy text), `PromptTemplateFormatter.kt`, `CascadeSpeechOrchestrator.kt`, `AssistantTtsEngine.kt` (+ `PcmAudioNormalizer`), `SpeechModeTestRunner.kt`, `AssistantSpeechMode.kt`, `AssistantSettingsStore.kt`, `ui/CityAssistantOverlay.kt`, `ui/ForgeCityViewModel.kt`.

Config UI: Gemini key (Keystore), TTS model / voice / language, editable prompt template, Portal endpoint/key (visible), custom TEST TTS text + button. Chips: `UI` / `ASSIST` / `SEARCH` / `DOCK`.

## Agent Portal (companion, tier 2)

- PROD endpoint: `https://agent-portal.delena.buzz/api/integrations/forgecity/tamil-rewrite`
- Header `X-ForgeCity-Key`; body exactly 5 fields (`schemaVersion`, `appLabel`, `title`, `text`, `maxChars`); `Cache-Control: no-store`.
- Key lives in `G:\apps\agent-portal\.env` as `FORGECITY_REWRITE_API_KEY` — never commit.

## Recent fixes

1. Portal 400 (0.4.2): dropped invalid 6th JSON field `"store":false`.
2. Gemini unavailable (0.4.4): dead `gemini-2.0-flash` → default `gemini-2.5-flash`; header auth.
3. Native audio (0.4.6): text rewrite → Gemini audio + `AudioTrack`.
4. Playback UNAVAILABLE (0.4.7): MODE_STATIC → MODE_STREAM + MediaPlayer WAV fallback.

## Now → next

| Now | Next |
|-----|------|
| 0.4.7 published; on-device Gemini audio playback fix awaiting user confirm | Confirm `pcm_play_started backend=...` in logcat on device |
| Realme P2 Pro E2E (#16) still PENDING | Blocks annotated production tags |

## Build / ship

```powershell
cd E:\MyWorkspace\sandbox\forgecity-launcher
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
# package -> dist\, write .sha256, gh release create --prerelease, upload apk + .sha256
```

Diagnostics on device: `adb logcat -s ForgeCityTTS` (never logs keys/bodies/Tamil text).

Session handoff date: 2026-07-21.
