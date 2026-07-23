# ForgeCity - Grok config panel + roadmap consult

**Requested model:** `grok-4-1-fast-reasoning` (Grok 4.1 Fast reasoning)
**API reported model:** `grok-4.3`
**Generated:** 2026-07-23
**Source:** xAI Chat Completions API (`XAI_API_KEY`)
**Scope:** City Assistant configuration panel UX + launcher improvements roadmap
**Usage:** prompt=945 · completion=1156 · reasoning=478

---

## 1. Executive verdict (5-8 lines)
Current assistant sheet mixes four speech paths into one scroll, causing prompt mismatches and visual noise. Redesign splits into mode-first segmented control with conditional sections, moving keys and diagnostics behind disclosure. This cuts invalid Gemini AUDIO calls and respects 360px width on Realme P2 Pro. P0 fixes target prompt validation and TEST placement. Roadmap keeps city visuals primary; settings remain secondary neon panels. Two-step rewrite->audio deferred to 0.8. All changes use Material3 ExposedDropdown and Card in Compose without new network calls.

## 2. Config panel IA redesign
- Information architecture: Mode selector first, then three collapsible cards (Speech Engine, Credentials, Atmosphere). Diagnostics and TEST always last. Progressive disclosure: Credentials hidden until mode requires network.
- Wireframe-like ASCII layout for phone sheet (360w):
```
+-----------------------------------+
| City Assistant                    |
| [X]                               |
| Speech Mode: [DIRECT|PORTAL|G-AUD]|
|                                   |
| --- Speech Engine ----------------|
| Prompt template (G-AUDIO only)    |
| [multiline 4 lines]               |
| Presets: [Speak|Narrate|...]      |
|                                   |
| --- Credentials ------------------|
| (shown only if PORTAL/G-AUDIO)    |
| Gemini key: [••••••] [edit]       |
| Model: [gemini-1.5-flash]         |
|                                   |
| --- Atmosphere -------------------|
| Video bg [toggle] Opacity [slider]|
| Quiet hours [ - 23:00 + ]         |
|                                   |
| TEST: [text field] [Speak] status |
| Diagnostics [v] 3 events [Copy]   |
+-----------------------------------+
```
- Specific Compose UI recommendations: Use `ModalBottomSheet` + `LazyColumn`. Top `SingleChoiceSegmentedButtonRow` for modes. Each section is `ElevatedCard` with `ExpansionTile`-style `AnimatedVisibility`. `TextField` for prompt uses `SupportingText` showing required placeholders per mode. `DropdownMenu` for Gemini model/voice.
- Prompt UX: On mode change to GEMINI AUDIO, clear or warn if template contains rewrite tokens. Provide three presets via `FilterChip` row that load validated strings. Add `onValueChange` validation: reject if "{title}" present in GEMINI AUDIO mode, show inline error "Use speak-aloud prompt only".
- Diagnostics / TEST TTS placement: Fixed bottom bar with `OutlinedButton` for TEST and trailing `IconButton` to expand log list. Log never auto-scrolls; manual "Show recent 10".

## 3. Must-ship config improvements (P0/P1) with acceptance criteria
P0:
- Mode change clears incompatible prompt fields and shows one-line explanation chip. AC: No HTTP 400 on first Gemini test after switch.
- API key fields use `visualTransformation = PasswordVisualTransformation()` by default; edit reveals only after tap. AC: Keystore never touched on view.
- TEST TTS button disabled until valid prompt for current mode. AC: Button state reflects mode + template check.

P1:
- Allowlist apps moved to separate `NavigationDrawer` destination. AC: Settings sheet < 70% screen height.
- Quiet hours use `TimePicker` dialog instead of +/- buttons. AC: Persists across ColorOS restarts.

## 4. Two-step Kongu rewrite -> Gemini audio plan (if recommended)
Not recommended for 0.7. Current CASCADE already chains; adding explicit two-step UI increases settings density without proven user need. Defer to 0.8 after 0.7 telemetry on CASCADE usage.

## 5. Launcher app roadmap 0.7 -> 1.0
- 0.7 (next): Assistant settings split + prompt validation. Theme: "Stable speech".
- 0.8: Living background video performance on SD 7s Gen 2 + Usage XP building unlock. Theme: "City motion".
- 0.9: Multi-agent selection with per-building assignment. Theme: "Agents".
- 1.0: Full story progression and export. Theme: "Forge release".

Priority matrix (Impact x Effort):
- High impact / low effort: Mode-segmented sheet, prompt validation.
- High impact / med effort: Separate allowlist screen, Realme E2E battery profile.
- Med impact / low effort: Preset chips, TEST bottom bar.
- Low priority: New animation system, custom TTS engine.

What NOT to do yet: New 3D building models, notification body logging, Gemini function calling, or any background service rewrite.

## 6. Risks (privacy, battery, ColorOS, Gemini quota/TTS)
- Privacy: Never surface notification text in diagnostics; only event types and timestamps.
- Battery: Video background toggle defaults off on ColorOS low-power detection.
- ColorOS: Test `WindowManager.LayoutParams` flags for sheet on Realme P2 Pro; avoid FLAG_BLUR_BEHIND.
- Gemini quota/TTS: Add client-side 60s cooldown after TEST; show remaining chars in prompt field.

## 7. Suggested next 2 implementation slices for Cursor agents
Slice 1: `ui/assistant/AssistantSheet.kt` - replace current Column with SegmentedButtonRow + three AnimatedVisibility Cards; add `AssistantMode` enum and `validatePromptForMode` function.
Slice 2: `ui/assistant/TestTtsBar.kt` + `data/AssistantPrefs.kt` - implement bottom TEST row with mode-aware enabled state and DataStore update for prompt presets.