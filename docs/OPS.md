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

## Build

1. Install Android SDK Platform 35 + Build-Tools.
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
