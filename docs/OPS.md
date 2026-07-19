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

Latest Background Video asset build:

```powershell
curl.exe -L -o forgecity-0.3.3-background-video-asset-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.3.3-background-video-asset-dev/forgecity-0.3.3-background-video-asset-dev-debug.apk
Get-FileHash .\forgecity-0.3.3-background-video-asset-dev-debug.apk -Algorithm SHA256
# expect B0B9EBC58D2AFB0AD47626790CBEBA98DD0335C0C87D7E7D7AF0E70D6018B7D4
adb install -r .\forgecity-0.3.3-background-video-asset-dev-debug.apk
```

Also grant: Home role, Usage Access, Notification Access (allowlist apps before TTS).

Older: `v0.3.2-background-video-dev` (SHA `5D0F8430…`, no bundled MP4).

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
- [ ] City canvas pans/zooms at 120 Hz without thermal throttling in 5 minutes
- [ ] Tap building → fly-in → launches correct app; Home returns to city
- [ ] Double-tap recenters camera
- [ ] Search filters buildings
- [ ] Package install/remove refreshes buildings
- [ ] Portrait cutout/safe-area chrome stays clear

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
`v0.3.3-background-video-asset-dev` with bundled procedural MP4).
**Annotated production tags** require: final MP4 + Realme device E2E GO +
Reviewer #17.
