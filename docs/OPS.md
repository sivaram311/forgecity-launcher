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

Tip (**0.10.0** Filament house) — tag `v0.10.0-filament-house-dev` not published yet:

```powershell
.\gradlew.bat assembleDebug
# rename dist artifact to forgecity-0.10.0-filament-house-dev-debug.apk
Get-FileHash .\forgecity-0.10.0-filament-house-dev-debug.apk -Algorithm SHA256
# expect 3D958C94EA50A82C85A0EF4F01BA6B7AF2C1BB6D5ADCB13BD0C5C6371293D9C2
adb install -r .\forgecity-0.10.0-filament-house-dev-debug.apk
```

Published prior (**0.9.0** procedural house):

```powershell
curl.exe -L -o forgecity-0.9.0-3d-house-characters-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.9.0-3d-house-characters-dev/forgecity-0.9.0-3d-house-characters-dev-debug.apk
Get-FileHash .\forgecity-0.9.0-3d-house-characters-dev-debug.apk -Algorithm SHA256
# expect D8E66EA9442B9C7F7747FCCA9DCBE3FFE454FA3F886648EEEDE842D461154A7F
adb install -r .\forgecity-0.9.0-3d-house-characters-dev-debug.apk
```

Older tip (**0.8.0** house Wave 1): SHA
`C14D5E2CCE7F5C29387CB1BC88BD15E5228BADC0219F88D4936B8D6F7F0AAF3E`.

Also grant: Home role, Usage Access, Notification Access (allowlist apps before speech).

### Assistant Clarity (0.7.0)

Neon sheet, mode-first — only the fields that match the active speech path.

1. **Mode-gated fields:** Gemini key / model / voice / audio prompt appear for
   **GEMINI AUDIO** and **CASCADE**. Portal endpoint + rewrite key appear for
   **PORTAL** (and cascade fallthrough). DIRECT / OFF stay lean.
2. **`PromptModeValidator`:** rewrite-style prompts (e.g. “spoken script”,
   “translate the notification”) are rejected for GEMINI AUDIO / CASCADE.
   Need a speak-aloud cue (“Synthesize speech”, “Read aloud”, …).
3. **Presets:** chips load **Tamil clear**, **Kongu friend**, or **English brief**
   (`AudioPromptPresets`) — all validator-safe for native audio.
4. **Masked keys:** API key fields show dots by default; reveal on tap. Keystore
   stays encrypted at rest; viewing does not re-wrap the secret.
5. **TEST TTS:** disabled when the current mode’s audio prompt fails validation
   (or mode is OFF). Valid DIRECT / PORTAL still run as before.
6. **Diagnostics:** **COPY LOG** / **CLEAR** unchanged — no keys, titles, bodies,
   or rewrite text in the ring buffer.

### City-first chrome (0.5.0)

1. Top-right **overflow menu** (hamburger) toggles: Launcher UI, Assistant settings,
   Search, Favorites dock. Defaults: chrome + dock on; search + assistant sheet off.
2. **Chapter pill** — tap to expand briefing.
3. **Assistant settings** open as a **modal sheet** (menu → Assistant settings), not
   a permanent home scroll. Background video toggle + opacity live there.
4. Long-press a building to pin/unpin favorites (haptic). Empty dock slots show **pin**.
5. Search (when enabled): typing a unique app name flies the camera to that building.

### Gemini native audio (0.5.1+)

1. Open Assistant settings sheet → save Gemini API key.
2. Confirm model `gemini-3.1-flash-tts-preview`, voice `Kore`. Language field is a
   **prompt hint** only (API auto-detects language from text; do not expect a
   separate `languageCode` in the JSON body).
3. Cycle speech mode to **GEMINI AUDIO** (fail-closed) or **CASCADE**.
4. Prefer a template that starts with “Synthesize speech only…” (default does).
5. TEST TTS; logcat: `adb logcat -s ForgeCityTTS` → `gemini_audio_ok` then
   `pcm_play_started backend=audiotrack|mediaplayer`.

**0.5.1 fix:** invalid `speechConfig.languageCode` removed (was causing Gemini fail).

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

### Built-in speech test + diagnostics (0.4.1 → 0.7.0)

1. Select a speech mode, then tap **TEST TTS** (enabled only when
   `PromptModeValidator.canRunTest` passes — see Assistant Clarity above).
2. DIRECT speaks a fixed local English test line. PORTAL sends a fixed synthetic
   test sentence (never notification content), validates the Tamil response, and
   speaks it using `ta-IN` / `ta`. GEMINI AUDIO / CASCADE use native audio.
3. The status line reports rewrite/TTS success or failure. `OFF` explains that a
   speech mode must be selected.
4. **0.6.1+:** open the **Speech diagnostics log** box under TEST TTS. It appends
   safe events (`I`/`W` + timestamp). Tap **COPY LOG** and paste into agent chat.
   **CLEAR** resets the ring buffer (max 200 lines). Same events also go to logcat.
5. **0.7.0:** keys are masked until reveal; still Keystore-encrypted at rest.
   Do not share screenshots of revealed key fields.

Terminal diagnosis (optional):

```powershell
adb logcat -c
adb logcat -s ForgeCityTTS
```

Logs include listener gates, selected route, endpoint host/HTTP status, elapsed
time, and TTS result. They never include API keys, notification title/body, or
Tamil response text.

For local-only speech, select `DIRECT`: ForgeCity uses the device default
locale and makes no Agent Portal request. `OFF` is the fresh-install default for
speech. **0.5.0** shows launcher chrome + dock by default; use the overflow menu
to hide panels.

Spec: [TAMIL-REWRITE-SPEC.md](TAMIL-REWRITE-SPEC.md).

Older speech tip: `v0.4.7-pcm-playback-fix-dev` (SHA `C98727E5…`).  
Older video tip: `v0.3.3-background-video-asset-dev` (SHA `B0B9EBC5…`).

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
