# ForgeCity – Implementation Spec (Cursor AI)

**Goal:** Upgrade the current ForgeCity launcher (isometric city home screen) based on the approved prototype style. Focus on usability, favorites, game-like feel, and a notification-reading AI assistant.

**Branch:** `feature/assistant-handoff-gaps` · **Version target:** `0.3.1-forge-assistant-dev`

## Defaults (product decisions)

- TTS **off** by default; app allowlist **empty** until user opts in (privacy-first).
- Screenshots requested via Android emulator; tooling/image installed, but this host has
  virtualization disabled and cannot boot x86_64 AVDs. Realme is also absent.
- Never persist notification title/body; SharedPreferences holds toggles/packages/quiet hours only.

## Features

1. Sparse buildings + depth-sorted AABB hit testing + press feedback
2. Persistent favorites dock (Room `isFavorite` on `building_stats`, max 6)
3. Neon assistant + NotificationListenerService + TextToSpeech
4. Dusk cyber atmosphere, power grid, chapter card, level growth polish

## Success criteria

- Reliable tap targeting; favorites one-tap; assistant reads only allowed apps when enabled
- Smooth on Realme P2 Pro targets; power-save / screen-off gates speech + ambient
- Debug prerelease published; annotated production tag blocked until physical Realme E2E
