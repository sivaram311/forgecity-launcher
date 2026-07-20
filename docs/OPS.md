# ForgeCity ops

## Source

| Item | Value |
|------|-------|
| GitHub | https://github.com/sivaram311/forgecity-launcher |
| Default working branch | `main` |
| Merged PR | https://github.com/sivaram311/forgecity-launcher/pull/1 (MERGED) |
| Local path | `E:\MyWorkspace\sandbox\forgecity-launcher` |

```powershell
git clone https://github.com/sivaram311/forgecity-launcher.git
cd forgecity-launcher
git checkout main
```

## Download (prerelease debug APK)

Latest Gemini native audio build:

```powershell
curl.exe -L -o forgecity-0.4.6-gemini-native-audio-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.4.6-gemini-native-audio-dev/forgecity-0.4.6-gemini-native-audio-dev-debug.apk
Get-FileHash .\forgecity-0.4.6-gemini-native-audio-dev-debug.apk -Algorithm SHA256
adb install -r .\forgecity-0.4.6-gemini-native-audio-dev-debug.apk
```

Also grant: Home role, Usage Access, Notification Access (allowlist apps before speech).

### Gemini native audio (0.4.6)

1. Save Gemini API key in City Assistant.
2. Confirm model `gemini-3.1-flash-tts-preview`, voice `Kore`, language `ta-IN`.
3. Cycle speech mode to **GEMINI AUDIO** (fail-closed) or **CASCADE** (audio → Portal → device).
4. Edit the audio prompt template if needed; TEST TTS exercises the active mode.
5. Logcat: `adb logcat -s ForgeCityTTS` — look for `gemini_audio_*` / `pcm_play_started`.

### ASSIST chip (0.4.4)

Persistent top-right **`ASSIST +` / `ASSIST −`** (below `UI +/−`) toggles City Assistant
panel, search bar, and favorites dock independently of full chrome. City canvas
and chapter/resources stay under the main `UI` chip.

### Tamil Agent Portal rewrite (0.4.0)

1. Portal **0.1.14+** has rewrite enabled on DEV / PREPROD / PROD (keys in each
   env `.env` only — never commit).
2. Tap the persistent `UI +` chip. In City Assistant, cycle Speech mode to
   `PORTAL தமிழ்`; set HTTPS endpoint to one of:
   - **PROD:** `https://agent-portal.delena.buzz/api/integrations/forgecity/tamil-rewrite`
   - **Staging:** `https://agent-portal-staging.delena.buzz/api/integrations/forgecity/tamil-rewrite`
   - **DEV:** `https://delena.buzz/api/integrations/forgecity/tamil-rewrite`
   Save the matching env’s `FORGECITY_REWRITE_API_KEY` (encrypted on device).
3. Allowlist at least one messaging app; send a test notification.
4. Expect Tamil TTS only; portal down / bad key / missing Tamil voice → silent.

### Built-in speech test + terminal diagnostics (0.4.1)

1. Select `DIRECT` or `PORTAL தமிழ்`, then tap **TEST TTS**.
2. DIRECT speaks a fixed local English test line. PORTAL sends a fixed synthetic
   test sentence (never notification content), validates the Tamil response, and
   speaks it using `ta-IN` / `ta`.
3. The status line reports rewrite/TTS success or failure. `OFF` explains that a
   speech mode must be selected.
4. Saved API keys remain Android-Keystore encrypted at rest but are intentionally
   visible in the config field for setup. Do not share screenshots.

Terminal diagnosis:

```powershell
adb logcat -c
adb logcat -s ForgeCityTTS
```

Logs include listener gates, selected route, endpoint host/HTTP status, elapsed
time, and TTS result. They never include API keys, notification title/body, or
Tamil response text.

For local-only speech, select `DIRECT`: ForgeCity uses the device default
locale and makes no Agent Portal request. `OFF` is the fresh-install default.
The launcher chrome is also hidden by default; `UI +` / `UI −` remains visible
with a 48 dp touch target and persists the choice.

Spec: [TAMIL-REWRITE-SPEC.md](TAMIL-REWRITE-SPEC.md).

Older: `v0.3.3-background-video-asset-dev` (SHA `B0B9EBC5…`).

Debug-signed, prerelease. Device E2E (#16) pending; do not treat as production.

## Build

1. Install Android SDK Platform 35 + Build-Tools (this host uses `C:\Android\Sdk`).
2. Create `local.properties` (gitignored):

   ```properties
   sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
   ```

3. Run:

   ```powershell
   .\gradlew.bat test lint assembleDebug
   ```

## Realme P2 Pro checklist (CONSCIOUS #16 — **current gate**)

Honor `E:\MyAgent\workflow\devices\REALME-P2-PRO.md` (360Ã—780 logical, curved
sides, center punch-hole, ≥44 dp targets).

Use build **`v0.3.3-background-video-asset-dev`** (or local `assembleDebug` matching tip).

### Baseline launcher

- [ ] `adb devices` sees the phone
- [ ] Install debug APK (hash matches SHA above)
- [ ] Accept home role / set default home
- [ ] Fresh install starts with only city/background + `UI +`; buildings still launch
- [ ] Toggle chip clears status/cutout at 360×780, is reachable by TalkBack, and is at least 48 dp
- [ ] `UI +` fades chrome in; `UI −` hides chapter/resources/settings/search/dock/hints/bubble
- [ ] Chrome visibility survives process restart
- [ ] City canvas pans/zooms at 120 Hz without thermal throttling in 5 minutes
- [ ] Tap building → fly-in → launches correct app; Home returns to city
- [ ] Double-tap recenters camera
- [ ] Search filters buildings
- [ ] Package install/remove refreshes buildings
- [ ] Portrait cutout/safe-area chrome stays clear

### Speech modes

- [ ] Fresh install mode is `OFF`
- [ ] Legacy TTS+rewrite migrates to Portal Tamil; TTS-only migrates to Direct
- [ ] `DIRECT` speaks local filtered text in the device default locale with no network request
- [ ] `PORTAL தமிழ்` uses only the configured no-store Portal endpoint and Tamil voice
- [ ] Portal timeout/auth/malformed response/missing Tamil voice stays silent
- [ ] Allowlist, quiet hours, budget, dedupe, and body non-persistence hold in both speech modes

### Phase 2 Awakening

- [ ] Day/night sky matches device clock; night glows/stars when not in power-save
- [ ] Power-save: ambient stars/glows gated off
- [ ] Grant Usage Access; Power / Focus / Gold update after harvest (1h debounce)
- [ ] Launch apps until a building levels up; particle burst + taller prism
- [ ] Force-stop + reopen: resources / quest progress / building levels survive
- [ ] Chapter briefing still shows; Chapter 2–3 quests remain locked stubs

### Background video

- [x] Place final H.264 MP4 at `app/src/main/res/raw/city_background.mp4` (procedural 0.3.3)
- [ ] Video renders below city/UI and loops without a visible seam
- [ ] Toggle persists; opacity slider remains between 40–100%
- [ ] Home/background lifecycle pauses/resumes playback
- [ ] Power Save, idle, and screen-off stop video and ambient animation
- [ ] Missing/invalid MP4 falls back to day/night gradient without a crash
- [ ] Five-minute playback has no decoder stutter or thermal warning
- [ ] Eight-hour comparison stays below the <5% extra battery target

Record results in `docs/VERIFICATION.md` (PASS / FAIL / notes). Device GO is
required before any annotated production tag.

## Reset home app

```powershell
adb shell am start -a android.settings.HOME_SETTINGS
```

## Release boundary

Sandbox DEV only. No host ports / Postgres / CSS for this APK.
**Debug prereleases** are published for sideload (latest:
`v0.4.1-tts-diagnostics-dev`; Realme E2E remains pending).
**Annotated production tags** require: Realme device E2E GO + Reviewer #17.
