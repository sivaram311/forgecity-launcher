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

Latest Forge Assistant build:

```powershell
curl.exe -L -o forgecity-0.3.1-forge-assistant-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.3.1-forge-assistant-dev/forgecity-0.3.1-forge-assistant-dev-debug.apk
Get-FileHash .\forgecity-0.3.1-forge-assistant-dev-debug.apk -Algorithm SHA256
# expect F1FF71110BD2DC4BABF1D6E724EDDC7DA00075D0B6FAEE8E6CEE873F62920171
adb install -r .\forgecity-0.3.1-forge-assistant-dev-debug.apk
```

Also grant: Home role, Usage Access, Notification Access (allowlist apps before TTS).

Older: `v0.3.0-forge-assistant-dev` (SHA `073E26F3…`).

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

Honor `E:\MyAgent\workflow\devices\REALME-P2-PRO.md` (360×780 logical, curved
sides, center punch-hole, ≥44 dp targets).

Use build **`v0.2.0-awakening-dev`** (or local `assembleDebug` matching tip).

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

Record results in `docs/VERIFICATION.md` (PASS / FAIL / notes). Device GO is
required before merging PR #1 as annotated `v0.2.0`.

## Reset home app

```powershell
adb shell am start -a android.settings.HOME_SETTINGS
```

## Release boundary

Sandbox DEV only. No host ports / Postgres / CSS for this APK.  
**Debug prerelease** (`v0.2.0-awakening-dev`) is published for sideload.  
**Annotated `v0.2.0` / non-debug** requires: device E2E GO + Reviewer #17.  
Room `Migration(1,2)` is in tip (destructive fallback removed).
