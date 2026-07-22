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
| Public accessibility | PUBLIC | Repo public; anonymous APK HEAD returns 200 |
| Product direction / roadmap | ADDED | `docs/ROADMAP.md` (v0.1 → v1.0 phases) |
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

## 2026-07-19 — Phase 2 Wave 1 (Awakening foundations)

Branch: `feature/phase-2-awakening` · versionName `0.2.0-awakening-dev`  
Parallel plan: `docs/PARALLEL-EXECUTION.md`

| Check | Result | Notes |
|------|--------|-------|
| Day/night + stars + night glows | LANDED | `DayNightCycle` + Canvas ambient (power-gated) |
| Camera fly-in / double-tap recenter | LANDED | Compose `Animatable` |
| UsageStats harvest + WorkManager | LANDED | Grant UX + 6h periodic worker |
| Building levels from launches | LANDED | `LaunchTracker` + XP level map |
| Unit tests (XP + day/night) | PASS | `testDebugUnitTest` green on Wave 1 |
| `assembleDebug` | PASS | `0.2.0-awakening-dev` APK built |
| Device E2E (#16) | PENDING | Blocks annotated `v0.2.0` tag |

## 2026-07-19 — Phase 2 Wave 2 (Awakening depth)

Branch: `feature/phase-2-awakening` · versionName `0.2.0-awakening-dev`  
Parallel plan: `docs/PARALLEL-EXECUTION.md`

| Track | Check | Result | Notes |
|-------|-------|--------|-------|
| A (UI) | Level-up particle burst | LANDED | Canvas radial burst on level increase |
| A (UI) | Animated resource counters | LANDED | `animateIntAsState` on resource chips |
| B (Systems) | Harvest debounce | LANDED | `lastHarvestEpoch` gate (1h) in ViewModel + worker |
| C (Data) | Room-backed building levels | LANDED | `building_stats` table = level SoT (DB v2) |
| C (Data) | Seed-reset bug fixed | FIXED | `insertMetaIfAbsent`/`insertQuestIfAbsent` (IGNORE) stop resource/quest reset on restart |
| D (Story) | Chapter 2–3 quest stubs | LANDED | `StoryCatalog.chapterTwo/ThreeQuests` seeded locked |
| Tests | `testDebugUnitTest` | PASS | + `StoryCatalogTest` (unique ids, chapter coverage) |
| Build | `assembleDebug` | PASS | Green after `getValue` import fix |
| Device E2E (#16) | PENDING | Still blocks annotated `v0.2.0` tag |

## 2026-07-19 — PR + v0.2.0-awakening-dev prerelease APK

| Check | Result | Notes |
|------|--------|-------|
| PR opened | PASS | https://github.com/sivaram311/forgecity-launcher/pull/1 (`feature/phase-2-awakening` → `main`) |
| Reviewer #17 (prerelease tag) | GO | `agents/hires/SIGN-OFF-v0.2.0-awakening-dev-prerelease.md` for tip `26b9f82` |
| Tag `v0.2.0-awakening-dev` | PUBLISHED | Prerelease, target `26b9f82` |
| APK asset | PUBLISHED | `forgecity-0.2.0-awakening-dev-debug.apk` (11.0 MB) |
| APK SHA-256 | `885182F9A12671BE1E68E3DF6819518FE20A308A3D6748FCAF2C440345E89B11` | |
| Device E2E (#16) | PENDING | Blocks annotated non-debug `v0.2.0` |

## Status snapshot — 2026-07-19 (post-prerelease)

| Item | State |
|------|--------|
| Code waves 1–2 | Landed on `feature/phase-2-awakening` |
| Unit tests / debug assemble | PASS |
| PR #1 → `main` | **MERGED** 2026-07-19 (`d1f8b09`) — user-directed E2E waiver for merge only |
| Tag `v0.2.0-awakening-dev` | PUBLISHED (prerelease, debug-signed) |
| Realme P2 Pro E2E (#16) | **PENDING — next gate** (no adb device) |
| Room Migration(1,2) | ✅ on `main` |
| Annotated `v0.2.0` | BLOCKED until E2E + Reviewer GO |

Device lab checklist: `docs/OPS.md` → “Realme P2 Pro checklist”.

## 2026-07-19 — Room Migration(1,2)

| Check | Result | Notes |
|------|--------|-------|
| `ForgeCityMigrations.MIGRATION_1_2` | LANDED | Adds `lastHarvestEpoch` + `building_stats` |
| Destructive fallback removed | LANDED | `addMigrations` only |
| Unit tests | PASS | `ForgeCityMigrationsTest` + full `testDebugUnitTest` |
| `assembleDebug` | PASS | Tip after migration |
| `adb devices` | EMPTY | No Realme attached — #16 still PENDING |

## 2026-07-19 — PR #1 merged to main

| Check | Result | Notes |
|------|--------|-------|
| Reviewer #17 merge | GO | `agents/hires/SIGN-OFF-merge-pr1-main.md` |
| Merge commit | `d1f8b09` | merge commit (history preserved) |
| User E2E waiver | RECORDED | Merge authorized without device lab; **not** E2E PASS |
| `feature/mvp-city-shell` | N/A | Already equal to pre-merge `main` — nothing unique |
| Annotated `v0.2.0` | STILL BLOCKED | Needs real device GO |

## 2026-07-19 — Forge Assistant 0.3.0-dev (feature/forge-assistant)

| Check | Result | Notes |
|------|--------|-------|
| Spec saved | PASS | `docs/IMPLEMENTATION-SPEC.md` |
| Sparse city + AABB hit + press glow | LANDED | `BuildingHitGeometry` + `IsoLayout` 120×60 |
| Favorites dock (max 6) | LANDED | Room `building_stats.isFavorite` + `MIGRATION_2_3` |
| NotificationListener + TTS | LANDED | Privacy: TTS off, empty allowlist, no body persistence |
| Atmosphere polish | LANDED | Dusk sky, power grid, chapter card, height growth |
| `testDebugUnitTest` | PASS | hit/favorite/quiet/filter/dedupe/migration 2→3 |
| `assembleDebug` | PASS | `0.3.0-forge-assistant-dev` |
| APK SHA-256 | `073E26F33AA48FC3E210FBF8650FB09F99EB6EC066CE7B8C1411FC5084001D13` | |
| Emulator screenshots | DEFERRED | At 0.3.0 build time; follow-up installed tooling but virtualization blocks boot |
| Device E2E (#16) | PENDING | Blocks annotated production tags |

## 2026-07-19 — Forge Assistant 0.3.1 handoff closure

| Check | Result | Notes |
|------|--------|-------|
| Editable quiet hours | LANDED | 30-minute start/end controls; persisted locally |
| Exact notification action | LANDED | Bubble sends ephemeral `contentIntent`; app fallback |
| Privacy boundary | PASS | `PendingIntent`/content remain in-memory; no notification persistence |
| `testDebugUnitTest` | PASS | Full JVM suite |
| `assembleDebug` | PASS | `0.3.1-forge-assistant-dev` (versionCode 4) |
| APK SHA-256 | `F1FF71110BD2DC4BABF1D6E724EDDC7DA00075D0B6FAEE8E6CEE873F62920171` | |
| Emulator tooling/image | INSTALLED | API 35 Google APIs x86_64 |
| Emulator boot/screenshots | BLOCKED | Host virtualization extension unavailable |
| Realme E2E (#16) | PENDING | Physical device absent |

## 2026-07-19 — Media3 Background Video 0.3.2-dev

| Check | Result | Notes |
|------|--------|-------|
| Media3 dependencies | PASS | exoplayer/ui/common 1.4.1 |
| ExoPlayer lifecycle | LANDED | muted, repeat-all, low local buffer, release on dispose |
| Home layer ordering | LANDED | video → contrast scrim → CityCanvas → UI |
| Toggle / opacity | LANDED | persisted; default ON / 0.80; opacity clamped 0.4–1.0 |
| Power gating | LANDED | AnimationBudget + live power-save receiver |
| Missing asset fallback | PASS | build has no MP4; runtime lookup returns gradient fallback safely |
| `testDebugUnitTest` | PASS | Existing JVM suite |
| `lintDebug` | PASS | Media3 unstable APIs explicitly opted in |
| `assembleDebug` | PASS | `0.3.2-background-video-dev` (versionCode 5) |
| APK SHA-256 | `5D0F84306085B4DDAF6CB57E59FE1009439F8F6CA71E9D011079A412C1D1CD2F` | |
| Final MP4 loop/decoder test | PENDING | `res/raw/city_background.mp4` not supplied |
| Realme performance/battery | PENDING | Physical device absent; <5% target unverified |

## 2026-07-19 — PR #4 merged + v0.3.2-background-video-dev published

| Check | Result | Notes |
|------|--------|-------|
| PR #4 → `main` | MERGED | merge commit `2bd8868` |
| Reviewer GO | PASS | `agents/hires/SIGN-OFF-v0.3.2-background-video-prerelease.md` |
| Debug prerelease | PASS | https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.3.2-background-video-dev |
| Release assets | PASS | APK + `.sha256`; download round-trip hash matched |
| Final MP4 shipped | NO | intentional; gradient fallback only in this APK |
| Realme E2E (#16) | PENDING | Blocks annotated production tags |

## 2026-07-19 — Background Video asset 0.3.3-dev

| Check | Result | Notes |
|------|--------|-------|
| Procedural MP4 generated | PASS | `tools_gen_city_bg.py` + FFmpeg; no third-party footage |
| Media probe | PASS | H.264 / yuv420p / 1080×1920 / 30fps / 10.000s / no audio / 3164693 bytes |
| Video SHA-256 | `1AC2A4AB2B18F16B201C1F6A59C45CC87C355DEB1D402F46B385C781ED6FA798` | |
| Loop seam (first vs last) | MEASURED | SSIM ≈ 0.915, PSNR ≈ 31.4 dB (periodic motion; not bit-identical) |
| `testDebugUnitTest` | PASS | |
| `lintDebug` | PASS | |
| `assembleDebug` | PASS | `0.3.3-background-video-asset-dev` (versionCode 6) |
| APK SHA-256 | `B0B9EBC58D2AFB0AD47626790CBEBA98DD0335C0C87D7E7D7AF0E70D6018B7D4` | |
| Realme decoder/thermal/battery | PENDING | Physical device absent |

## 2026-07-19 — PR #5 merged + v0.3.3-background-video-asset-dev published

| Check | Result | Notes |
|------|--------|-------|
| PR #5 → `main` | MERGED | merge commit `23d3245` |
| Reviewer GO | PASS | `agents/hires/SIGN-OFF-v0.3.3-background-video-asset-prerelease.md` |
| Debug prerelease | PASS | https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.3.3-background-video-asset-dev |
| Release assets | PASS | APK + `.sha256`; download round-trip hash matched |
| Bundled MP4 | PASS | procedural `city_background.mp4` in `res/raw` |
| Realme E2E (#16) | PENDING | Blocks annotated production tags |


## 2026-07-19 — Tamil Agent Portal rewrite 0.4.0-dev (local)

| Check | Result | Notes |
|------|--------|-------|
| Grok architecture | PASS | Phone→HTTPS Agent Portal; no Cloudflare; no desktop CLI on device |
| Portal endpoint | PASS | `POST /api/integrations/forgecity/tamil-rewrite`; dedicated `X-ForgeCity-Key`; no ChatMessage persistence |
| Android pipeline | PASS | RAM queue, encrypted key, Tamil TTS `ta-IN`/`ta`, silent fail-closed |
| Contract alignment | PASS | `schemaVersion` / `tamil` (not `tamilText`) |
| Immersive chrome | SOURCE PASS | Hidden default; persistent 48 dp safe-area chip; canvas remains composed |
| Speech migration/routing | UNIT PASS | OFF / DIRECT / PORTAL enum; legacy mapping; direct path has no client call |
| `testDebugUnitTest` | PASS | Migration, routing, hidden default, parser, queue and existing suites |
| `lintDebug` | PASS | |
| `assembleDebug` | PASS | `0.4.0-tamil-agent-dev` / versionCode 7 |
| APK SHA-256 | `E4C3E161D464D2AC15994AE91F5880FB160B7D99F775743A98A81F9224497AD8` | local debug artifact before release publish |
| Cloudflare Workers AI | REJECTED | product ban for launcher |
| Realme 360×780 immersive + speech E2E | PENDING | No physical run claimed; needs device + enabled portal env |

## 2026-07-20 — PR #6 merged + v0.4.0-tamil-agent-dev published

| Check | Result | Notes |
|------|--------|-------|
| PR #6 → `main` | MERGED | merge commit `351e8a9` |
| Agent Portal companion | MERGED | portal PR #2 @ `69e0bc7` (`feature/forgecity-tamil-rewrite`) |
| Reviewer GO | PASS | `agents/hires/SIGN-OFF-v0.4.0-tamil-agent-immersive-prerelease.md` |
| Debug prerelease | PASS | https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.0-tamil-agent-dev |
| APK SHA-256 | `E4C3E161D464D2AC15994AE91F5880FB160B7D99F775743A98A81F9224497AD8` | |
| Realme E2E (#16) | PENDING | Blocks annotated production tags |

## 2026-07-20 — Agent Portal DEV live rewrite smoke

| Check | Result | Notes |
|------|--------|-------|
| Portal jar rebuild | PASS | Stale DEV jar (2026-07-18) rebuilt with ForgeCity classes |
| `FORGECITY_REWRITE_ENABLED` | PASS | Local `.env` only (not committed); DEV restarted on `:8080` |
| Wrong key | PASS | HTTP 401 |
| Loopback rewrite | PASS | `schemaVersion:1` `status:ok` Tamil present; `Cache-Control: no-store` |
| Public HTTPS rewrite | PASS | `https://delena.buzz/api/integrations/forgecity/tamil-rewrite` → 200 + Tamil |
| Realme device E2E (#16) | PENDING | `adb devices` empty — USB Realme P2 Pro required |

## 2026-07-20 — 0.4.1 TTS test + diagnostics

| Check | Result | Notes |
|------|--------|-------|
| Version | PASS | `0.4.1-tts-diagnostics-dev` / versionCode 8 |
| Mode-aware test | SOURCE PASS | OFF diagnosis; DIRECT fixed local line; PORTAL fixed synthetic rewrite → Tamil TTS |
| API key visibility | PASS | decrypted into config field on demand; remains Android-Keystore encrypted at rest |
| Safe Logcat | SOURCE PASS | tag `ForgeCityTTS`; route/HTTP/TTS diagnostics; no keys, notification bodies, or rewrite text |
| `testDebugUnitTest` | PASS | |
| `lintDebug` | PASS | |
| `assembleDebug` | PASS | |
| APK SHA-256 | `2CBFABC5BB4942719EAAC04A60BAAA9E0DC7A4F67413FE8EB9696C992855FAAF` | `dist/forgecity-0.4.1-tts-diagnostics-dev-debug.apk` |
| Physical TEST TTS | PENDING | No adb device attached; debug prerelease only |

## 2026-07-20 — 0.4.2 rewrite contract fix

| Check | Result | Notes |
|------|--------|-------|
| Root cause | CONFIRMED | App sent 6-field body incl. `store:false`; server (`ForgeCityRewriteContract`) requires exactly 5 → HTTP 400 `invalid_request` → app `Unavailable` |
| curl repro | PASS | PROD: with `store` → 400; without `store` → 200 + Tamil (same key) |
| Fix | PASS | `RewriteRequest.toJson()` emits exactly `schemaVersion,appLabel,title,text,maxChars`; `no-store` stays a header |
| Regression test | PASS | `sendsExactlyTheServerContractFields` asserts key set and no `store` |
| `testDebugUnitTest` / `lintDebug` / `assembleDebug` | PASS | versionCode 9 / `0.4.2-rewrite-contract-fix-dev` |
| APK SHA-256 | `476089BE96B3BCBAA7793D45AB865C8D7347772776FB5CDFD45F8DFD647C8F91` | `dist/forgecity-0.4.2-rewrite-contract-fix-dev-debug.apk` |
| Physical TEST TTS | PENDING | Awaiting Realme device |

## 2026-07-20 — 0.4.3 Gemini cascade (source)

| Check | Result | Notes |
|------|--------|-------|
| `SMART_CASCADE` mode | SOURCE PASS | Gemini → Portal → DIRECT routing |
| Pre-template editor | SOURCE PASS | persisted; placeholders substituted |
| Gemini client | SOURCE PASS | HTTPS `generativelanguage.googleapis.com`; Tamil validation |
| `CascadeSpeechOrchestrator` | SOURCE PASS | tier fallthrough + diagnostics |
| `testDebugUnitTest` / `lintDebug` / `assembleDebug` | PASS | versionCode 10 |
| APK SHA-256 | `AFFEEAA381CC9AB2BDE81F4737BE03F499FD021704A94A11B2D429AC21A9B985` | `dist/forgecity-0.4.3-gemini-cascade-dev-debug.apk` |
| Physical cascade E2E | PENDING | No adb device |

## 2026-07-20 — v0.4.3-gemini-cascade-dev published

| Check | Result | Notes |
|------|--------|-------|
| Debug prerelease | PASS | https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.4.3-gemini-cascade-dev |
| Tip | `82d92c7` | Gemini cascade source |
| APK SHA-256 | `AFFEEAA381CC9AB2BDE81F4737BE03F499FD021704A94A11B2D429AC21A9B985` | download round-trip at publish |
| Realme E2E (#16) | PENDING | Blocks annotated production tags |

## 2026-07-20 — 0.4.4 Gemini unavailable root-cause + ASSIST chip

| Check | Result | Notes |
|------|--------|-------|
| Root cause | CONFIRMED | Default `gemini-2.0-flash` shut down 2026-06-01; 400 API_KEY_INVALID / Malformed mislabeled as Unavailable |
| Fix | PASS | Default + migrate → `gemini-2.5-flash`; header auth; distinct status strings |
| ASSIST chip | PASS | Hides assistant + search + favorites; 48 dp; persisted |
| `testDebugUnitTest` / `lintDebug` / `assembleDebug` | PASS | versionCode 11 |
| APK SHA-256 | `AE44CEA9E688D2115460809A02E1EDC08190BD60DE6F3F2EC5EB679A1402C491` | |
| Realme E2E (#16) | PENDING | |

## 2026-07-20 — 0.4.5 split chips + GEMINI mode + custom TEST text

| Check | Result | Notes |
|------|--------|-------|
| ASSIST / SEARCH / DOCK chips | PASS | Independent visibility prefs |
| `GEMINI_TAMIL` mode | PASS | Fail-closed Gemini-only; CASCADE unchanged |
| Custom TEST TTS text | PASS | Persisted; used by all modes |
| Unit/lint/assemble | PASS | versionCode 12 |
| APK SHA-256 | `86E1CEDA64908B69771E614B210F743070AFDAC0D0E2A7D3D705A98D3FECC917` | |

## 2026-07-20 — 0.4.6 Gemini native audio TTS

| Check | Result | Notes |
|------|--------|-------|
| Gemini AUDIO path | PASS | `responseModalities:AUDIO` → PCM `AudioTrack` |
| Text-model migration | PASS | `gemini-2.5-flash` → `gemini-3.1-flash-tts-preview` |
| Voice / language prefs | PASS | default `Kore` / `ta-IN` |
| CASCADE order | PASS | Gemini audio → Portal → DIRECT |
| `GEMINI_TAMIL` UI | PASS | Labelled GEMINI AUDIO; fail-closed |
| Unit/lint/assemble | PASS | versionCode 13 |
| APK SHA-256 | `1644ED69CC47074932E327170F998D9593ED73A1CEE0AD0FB7B34A2F9C92BC6A` | |
| Realme E2E (#16) | PENDING | |

## 2026-07-20 — 0.4.7 PCM playback fix

| Check | Result | Notes |
|------|--------|-------|
| MODE_STREAM AudioTrack | PASS | replaces MODE_STATIC |
| MediaPlayer WAV fallback | PASS | cacheDir temp wav |
| Diagnostics | PASS | `pcm_play_attempt` / backend tags |
| Unit/lint/assemble | PASS | versionCode 14 |
| APK SHA-256 | `C98727E5F1169E486193D6E3E1ADBF9D21AA646E76231EE841A85F5756B9B377` | |
| Realme E2E (#16) | PENDING | |

## 2026-07-21 — 0.5.0 UI polish (A–D)

| Check | Result | Notes |
|------|--------|-------|
| City-first chrome | LANDED | Chapter pill, overflow menu, ASSIST modal sheet |
| Local video scrims | LANDED | No full-screen mud; upper fade + chrome strips |
| Building craft | LANDED | `CityRender` + `DistrictSilhouette`, LOD badges |
| Motion / dock | LANDED | Pan inertia, search focus, haptics, glass dock |
| Unit/lint/assemble | PASS | versionCode 15 · 52 tests |
| APK SHA-256 | `BB8FECCF655928DC5EC5D28665890CE3FC63F7422028F9E3A6327D2C062C3CFA` | local dist |
| Realme E2E (#16) | PENDING | |

## 2026-07-21 — 0.5.1 Gemini audio fix

| Check | Result | Notes |
|------|--------|-------|
| Root cause | FIXED | removed invalid speechConfig.languageCode |
| Request body unit test | PASS | voice-only; no role/languageCode |
| Unit/lint/assemble | PASS | versionCode 16 |
| APK SHA-256 | `05D21575B597856A01989F8B15E2BD1804497294A4ECE296C188F8AFC1D52365` | |
| Realme E2E (#16) | PENDING | |

## 2026-07-21 — 0.6.0 CityRender 3D visual upgrade

| Check | Result | Notes |
|------|--------|-------|
| Lighting model | LANDED | continuous `nightFactor`/`timeSeconds`/`activityPulse`, backward-compatible defaults |
| Building shading | LANDED | gradient walls, roof rim light, softer contact shadow |
| Windows | LANDED | emissive night panes + deterministic flicker |
| Roof silhouettes | LANDED | night emissive accents; shapes unchanged |
| Ground plane | LANDED | depth gradient + night grid response |
| Hit geometry / prism | UNCHANGED | `BuildingHitGeometry`, footprint, `DistrictSilhouette.of`, `RoofStyle` intact |
| Unit/lint/assemble | PASS | versionCode 17 |
| APK SHA-256 | `F637ECF048AF7DFBC921F6C074F6EABD6A3CC72C7D046BDD5578B766D9105A2A` | local dist (16.84 MB) |
| Realme E2E (#16) | PENDING | debug prerelease; not production |

## 2026-07-22 — 0.6.1 in-app TTS error / diagnostics log

| Check | Result | Notes |
|------|--------|-------|
| Ring buffer | LANDED | `ForgeCityTtsDiagnostics` append-only, max 200 lines, `StateFlow` snapshot |
| UI | LANDED | Assistant settings: monospace log + **COPY LOG** / **CLEAR** |
| Privacy | PASS | still no keys / notification bodies / rewrite text |
| Unit tests | PASS | `ForgeCityTtsDiagnosticsTest` + full `testDebugUnitTest` |
| lint / assemble | PASS | versionCode 18 |
| APK SHA-256 | `BE2F45E5EF46F7CD11F4B3CBB0A03A3CD0DA49E8889E7AA0A054699600568383` | local dist (~16.9 MB) |
| Realme E2E (#16) | PENDING | debug prerelease; not production |
