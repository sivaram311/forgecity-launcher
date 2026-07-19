# ForgeCity ops

## Source

| Item | Value |
|------|-------|
| GitHub | https://github.com/sivaram311/forgecity-launcher |
| Default working branch | `feature/mvp-city-shell` |
| Local path | `E:\MyWorkspace\sandbox\forgecity-launcher` |

```powershell
git clone https://github.com/sivaram311/forgecity-launcher.git
cd forgecity-launcher
git checkout feature/mvp-city-shell
```

## Download (prerelease debug APK)

```powershell
curl.exe -L -o forgecity-0.1.0-mvp-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.1.0-mvp/forgecity-0.1.0-mvp-debug.apk
Get-FileHash .\forgecity-0.1.0-mvp-debug.apk -Algorithm SHA256   # expect 073C4959...86C2209A
adb install -r .\forgecity-0.1.0-mvp-debug.apk
```

Debug-signed, prerelease. Device E2E pending; do not treat as production.

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

## Realme P2 Pro checklist

Honor `E:\MyAgent\workflow\devices\REALME-P2-PRO.md` (360×780 logical, curved
sides, center punch-hole, ≥44 dp targets).

- [ ] `adb devices` sees the phone
- [ ] Install debug APK
- [ ] Accept home role / set default home
- [ ] City canvas pans/zooms at 120 Hz without thermal throttling in 5 minutes
- [ ] Tap building launches correct app; Home returns to city
- [ ] Search filters buildings
- [ ] Package install/remove refreshes buildings
- [ ] Portrait cutout/safe-area chrome stays clear

## Reset home app

```powershell
adb shell am start -a android.settings.HOME_SETTINGS
```

## Release boundary

Sandbox DEV only. No port/DB/CSS. Signed release + device E2E + Reviewer GO
required before any public distribution.
