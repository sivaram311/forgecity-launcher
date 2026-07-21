# UI Polish Implementation Brief — ForgeCity 0.5.x

**Goal:** Implement full UI/video polish (Slices A–D) in one pass. Functionality (speech, launch, favorites, video gates) must stay intact.

**Local repo:** `E:\MyWorkspace\sandbox\forgecity-launcher` · branch `main`  
**Device SoT:** Realme P2 Pro (portrait, punch-hole, 120 Hz) — keep battery gates.

## Non-negotiables

1. Do **not** break speech cascade, ASSIST settings data, Keystore keys, Portal contract, or AnimationBudget video/ambient gates.
2. Stay on **Compose Canvas** — no Filament/3D engine.
3. Background video still muted, lifecycle/power-gated, optional toggle + opacity.
4. Run `.\gradlew.bat testDebugUnitTest lintDebug assembleDebug` before finishing.
5. Bump `versionName` to `0.5.0-ui-polish-dev` and increment `versionCode` by 1 in `app/build.gradle.kts`.
6. Update `docs/HANDOFF.md` + short entry in `docs/VERIFICATION.md` + `docs/OPS.md` chrome notes if needed.
7. Commit on `main` with a clear message. **Do not `git push`** unless already clean policy allows — prefer local commit only; if you push, only after green gradle.
8. Follow existing package style under `buzz.delena.forgecity`.

---

## Slice A — City first (chrome)

**Files:** `ui/ForgeCityHomeScreen.kt`, `ui/CityAssistantOverlay.kt` (settings card), `ui/FavoritesDock.kt`, new files under `ui/theme/` if useful.

1. **Remove full-screen video mud scrim** currently applied when video is on (`Box` with heavy `0xA60A0A12` gradients over the whole screen). Replace with **local scrims only** under top chrome and bottom dock (gradient strips / behind cards), so video + city remain visible.
2. **Assistant settings out of permanent home scroll:** default home must NOT show the full `AssistantSettingsCard`. Open it from a **modal bottom sheet / full-screen dialog** when user taps ASSIST (or gear). Keep all existing settings fields and callbacks.
3. **Chapter card → compact pill** by default (title + chapter one line); tap expands briefing. Persist expand state only in memory.
4. **Replace four text ChromeChips** (UI/ASSIST/SEARCH/DOCK) with a cleaner control: either one overflow menu FAB **or** small icon-like chips (still accessible). Keep ability to hide chrome, assistant panel, search, dock.
5. **Default chrome:** dock on, search off or compact, assistant settings panel off, chapter collapsed. Existing SharedPreferences toggles may stay; set sensible defaults for first run if already stored — do not wipe user prefs aggressively; if defaults are already true for assistant, change default initial values only when key absent.
6. Hide permanent dock help caption ("Long-press building…") behind first-use or remove; optional short toast on first long-press is fine.
7. Resource strip: more compact (smaller padding / single row density).

---

## Slice B — Building craft (canvas)

**Files:** `ui/CityCanvas.kt`, optionally `city/IsoLayout.kt`, new `ui/cityrender/` helpers if file gets large.

1. **District silhouettes:** vary roof/top shape and accent by `District` (Forge spire, Vault flatter gold top, Nexus teal antenna, Arena angular, Garden softer, Archive stepped, Custom default). Keep hit geometry compatible with `BuildingHitGeometry` (same base footprint / height formula unless you carefully update hit tests + unit tests).
2. **Facade polish:** left/right wall shading, window dots (level-dependent), soft contact shadow under base, cleaner night glow (not a huge orange blob).
3. **Icon treatment:** draw app icons in a circular/rounded badge on roof or upper facade with subtle shadow; when zoomed out (`scale < ~0.9`) use smaller badge or color plate only (LOD).
4. **Ground plane:** simple isometric tile/road under the city (subtle grid pads), less pure empty void.
5. **Favorites:** gold ring/banner pin, not only orange fill.
6. Keep pan/zoom, fly-in, double-tap recenter, level-up growth + burst (improve burst slightly if cheap).

---

## Slice C — Atmosphere video

**Files:** `ui/background/*`, `ForgeCityHomeScreen.kt` scrim, `tools_gen_city_bg.py`, `docs/BACKGROUND-VIDEO-SPEC.md`, optionally regenerate `res/raw/city_background.mp4`.

1. After local-scrim change, optionally **mask video to upper ~45–55%** of screen (gradient fade into city mid) so iso city owns lower half — implement if it looks better; otherwise full-bleed with light local scrims only.
2. Improve procedural generator if regenerating: cleaner neon, quieter top/bottom bands, less harsh flicker, seamless 10s loop. Target ≤ ~4–6 MB H.264 1080×1920 portrait, no audio.
3. If ffmpeg/python unavailable or regen fails, **keep existing MP4** but still fix scrim + optional upper mask in Compose.
4. Poster/still: when video disabled or budget off, rely on day/night sky gradient (already present); optional soft vignette OK.
5. Update BACKGROUND-VIDEO-SPEC if asset hash/size changes.

---

## Slice D — Motion & dock polish

1. **Pan inertia** after transform gesture ends (decay velocity) — keep bounds.
2. **Search-to-focus:** when query matches a single building (or user submits), camera fly toward that building.
3. **Haptics** on favorite pin/unpin if easy (`HapticFeedback` / `LocalView`).
4. Dock: slightly glassier material, clearer empty slots ("pin" affordance).

---

## Tests

- Update/add unit tests if hit geometry or pure math helpers change.
- Existing tests must pass.
- Lint clean for new code.

## Deliverable checklist

- [ ] All slices A–D implemented
- [ ] versionName/versionCode bumped
- [ ] gradle test + lint + assembleDebug PASS
- [ ] HANDOFF + VERIFICATION updated
- [ ] Local git commit (message like `Polish home UI: city-first chrome, buildings, video atmosphere`)
- [ ] Short summary of files changed and how to verify on device

Do not invent Realme E2E #16 PASS. Leave E2E PENDING.
