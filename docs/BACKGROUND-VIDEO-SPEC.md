# ForgeCity Background Video Spec

Goal: render an optional, muted, seamless Media3 cyberpunk city video beneath
the isometric city and all launcher chrome without compromising battery or
fallback reliability.

## Runtime contract

- Asset name: `app/src/main/res/raw/city_background.mp4`
- Recommended H.264 MP4: vertical, at most 1080×1920, 30fps, 10 seconds,
  under 20MB, visually seamless.
- The asset is looked up dynamically. If it is absent or fails to decode,
  ForgeCity keeps the existing day/night gradient and never crashes.
- ExoPlayer is muted, uses `REPEAT_MODE_ALL`, and has no controls.
- Playback requires all of: user setting enabled, foreground/resumed lifecycle,
  interactive screen, and `AnimationBudget` approval.
- Power Save / idle / screen-off and lifecycle pause stop playback.
- Opacity is persisted and clamped to 0.4–1.0 (default 0.80).

## Settings

The City Assistant card includes an Atmosphere section:

- Background Video toggle
- Video Opacity slider

Preferences are local SharedPreferences. No media or user content is uploaded.

## Release limitation

The repository intentionally does not ship a generated MP4 in this wave.
Builds exercise the missing-asset fallback. Real video loop quality, hardware
decode, thermal behavior, and battery impact require the final MP4 plus a
physical Realme P2 Pro E2E run.
