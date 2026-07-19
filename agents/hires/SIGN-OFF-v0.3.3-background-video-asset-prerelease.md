# SIGN-OFF — forgecity-launcher v0.3.3-background-video-asset-dev

| Field | Value |
|-------|-------|
| Session | CONSCIOUS #17 background-video asset prerelease |
| Role | Reviewer |
| Repo | `sivaram311/forgecity-launcher` |
| Branch / tag | `feature/background-video-asset` → PR to `main`; tag `v0.3.3-background-video-asset-dev` |
| Version | versionCode 6 · versionName `0.3.3-background-video-asset-dev` |
| APK | `dist/forgecity-0.3.3-background-video-asset-dev-debug.apk` |
| APK SHA-256 | `B0B9EBC58D2AFB0AD47626790CBEBA98DD0335C0C87D7E7D7AF0E70D6018B7D4` |
| Video SHA-256 | `1AC2A4AB2B18F16B201C1F6A59C45CC87C355DEB1D402F46B385C781ED6FA798` |

## Checklist

- [x] Original procedural MP4 at `app/src/main/res/raw/city_background.mp4`
- [x] Media probe: H.264 / yuv420p / 1080×1920 / 30fps / 10.000s / no audio / <20MB
- [x] Generator provenance documented (`tools_gen_city_bg.py`; no third-party footage)
- [x] `testDebugUnitTest` + `lintDebug` + `assembleDebug` PASS
- [x] Docs/hashes match; no secrets; APK stays gitignored under `dist/`
- [x] Realme E2E remains PENDING (waived for debug prerelease only)

## Verdict

**GO** — branch push, PR merge to `main`, and debug prerelease
`v0.3.3-background-video-asset-dev` (prerelease=true). Production tags remain
BLOCKED pending Realme #16.
