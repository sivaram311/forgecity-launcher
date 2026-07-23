# Device E2E — 3D House HOME (Realme P2 Pro · CONSCIOUS #16)

**Device SoT:** `E:\MyAgent\workflow\devices\REALME-P2-PRO.md`  
**Build:** tip `0.8.0-3d-house-dev` (versionCode 20) or matching local `assembleDebug`  
**Related:** [OPS.md](OPS.md) (city/video #16), [VERIFICATION.md](VERIFICATION.md), [design/3D-HOUSE-PARALLEL-EXEC.md](design/3D-HOUSE-PARALLEL-EXEC.md)

> **Physical device required** for every unchecked item below unless marked *emulatable*.  
> Do **not** claim #16 PASS without a Realme P2 Pro run recorded in `VERIFICATION.md`.

## Legend

| Mark | Meaning |
|------|---------|
| **DEVICE** | Needs USB Realme P2 Pro (or equivalent ColorOS mid-range) |
| **EMULATABLE** | Can smoke on emulator / unit CI; still re-check on device before GO |
| **LOG** | Write PASS/FAIL + notes into `VERIFICATION.md` |

---

## 1. Install + HOME role — **DEVICE**

- [ ] `adb devices` lists the Realme (`LOG`)
- [ ] Install debug APK (`adb install -r …`); hash matches published tip when using release asset (`LOG`)
- [ ] Accept home role / set ForgeCity as default HOME (`adb shell am start -a android.settings.HOME_SETTINGS`)
- [ ] Cold start lands on **house** floor-plan (not city) when `HouseFeatureFlags.use3dHouse=true`
- [ ] Portrait cutout / curved sides: chrome and room labels remain reachable (≥44 dp)

## 2. Tap apps in rooms — **DEVICE**

- [ ] Room markers appear in Kitchen / Living / Hall / Office / Bedroom / Workshop (and Vault if Wave 2 landed)
- [ ] Tap a marker launches the mapped app; system Home returns to house
- [ ] Search (if shown) still filters / finds apps without crashing house surface
- [ ] Package install/remove refreshes markers after resume

## 3. Long-press favorite — **DEVICE**

- [ ] Long-press a favorite on the dock (or house path that pins favorites) adds/toggles favorite without ANR
- [ ] Favorites survive process death / force-stop + reopen

## 4. Assistant TEST TTS — **DEVICE**

- [ ] Open assistant settings; speech mode not silently stuck on a broken path
- [ ] Use **TEST TTS** (or diagnostics “test speak”) — audible output on device speakers (`LOG`: mode + locale)
- [ ] Quiet hours / allowlist / OFF mode still suppress unwanted speech
- [ ] Body text is never persisted (spot-check storage / logs)

## 5. Thermal / power-save → characters drop — **DEVICE**

Depends on Wave 2 characters + `AnimationBudget` / `HousePerfBudget` wiring.

- [ ] With battery saver **off** and cool device: quality tier **HIGH** (soft glow / up to 3 characters if implemented)
- [ ] Toggle **Battery saver ON** → characters drop to ≤1 (MEDIUM) or freeze (LOW); soft shadows off (`LOG`: observe UI + optional `HousePerfBudget.recentDecisions()`)
- [ ] Toggle Battery saver **OFF** → characters / glow recover without restart
- [ ] Optional stress: 5–10 min house idle; if thermal moderate/severe trips, tier drops toward MEDIUM/LOW (`LOG`)

## 6. House / city toggle — **DEVICE** (+ **EMULATABLE** compile flag)

- [ ] With `HouseFeatureFlags.use3dHouse=false` (rebuild): CityCanvas isometric home is restored (`EMULATABLE` compile check; **DEVICE** for feel)
- [ ] With `use3dHouse=true`: house is default HOME surface again
- [ ] City video remains default-off; enabling video does not crash when house is on

## 7. 10-minute soak — **DEVICE** · **LOG**

Leave house HOME foreground, screen on, mild interaction every ~2 min (pan/tap empty floor ok).

| Minute | Note (jank / ANR / thermal banner / battery % / tier) |
|--------|------------------------------------------------------|
| 0 | |
| 2 | |
| 5 | |
| 10 | |

- [ ] No ANR / fatal crash
- [ ] No sustained UI freeze >1 s
- [ ] Optional: estimate FPS subjectively vs `targetFpsHint` (60 HIGH / 30 MEDIUM·LOW); stub `FpsEstimator` need not match lab meters
- [ ] Record battery delta if measuring overnight drain separately (do not invent &lt;5% claims)

## 8. Sign-off gate

- [ ] All **DEVICE** rows PASS or explicitly waived with owner + reason
- [ ] Evidence pasted into `docs/VERIFICATION.md` (#16 house HOME section)
- [ ] Reviewer #17 only after device GO for production tags

---

## Quick commands

```powershell
adb devices
adb install -r .\forgecity-0.8.0-3d-house-dev-debug.apk
adb shell am start -a android.settings.HOME_SETTINGS
# Battery saver (API varies by ColorOS; Settings UI is authoritative)
adb shell settings put global low_power 1
adb shell settings put global low_power 0
```
