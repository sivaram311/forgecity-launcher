# Verification

## 2026-07-19 — ForgeCity 0.1.0-mvp bootstrap

| Check | Result | Notes |
|------|--------|-------|
| Project scaffold + docs | PASS | Compose MVP + architecture/story/crew |
| XML / source present | PASS | Manifest HOME filters, Room, Compose UI |
| Reviewer SIGN-OFF (#17) | GO | `agents/hires/SIGN-OFF-mvp-city-shell-push.md` for tip `9bd418b` |
| GitHub publish | PASS | https://github.com/sivaram311/forgecity-launcher · `feature/mvp-city-shell` |
| Realme device E2E | PENDING | No physical P2 Pro attached to build host |

## 2026-07-19 — 0.1.0-mvp build + APK publish

Android SDK installed on the build host (`C:\Android\Sdk`: cmdline-tools,
platform 35, build-tools 35.0.0, platform-tools).

| Check | Result | Notes |
|------|--------|-------|
| `testDebugUnitTest` | PASS | `IsoMathTest` + `DistrictClassifierTest` green |
| `assembleDebug` | PASS | `app-debug.apk` produced |
| APK badging | PASS | pkg `buzz.delena.forgecity` · v `0.1.0-mvp` · label ForgeCity · HOME launchable |
| Release asset | PUBLISHED | `forgecity-0.1.0-mvp-debug.apk` (10.9 MB) + `.sha256` on tag `v0.1.0-mvp` (prerelease) |
| Download round-trip | PASS | `gh release download` re-fetch SHA-256 matches build hash exactly |
| Public accessibility | PUBLIC | Repo visibility set public 2026-07-19; anonymous APK HEAD returns 200 |
| Realme device E2E (#14–#16) | PENDING | Debug prerelease; device run required before any non-debug release |

Build command:

```powershell
$env:ANDROID_HOME="C:\Android\Sdk"
.\gradlew.bat testDebugUnitTest assembleDebug
```

APK SHA-256: `073C495949BD52BB1FD9AD09ACBF1A65339F80F6F150B2B3F282960B86C2209A`

### Caveat

Build is **debug-signed** and marked **prerelease**. It has not run on a
physical Realme P2 Pro, so #16 device E2E is a documented waiver (no device on
the build host), not a pass. No production upload key applied.
