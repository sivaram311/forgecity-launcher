# ForgeCity Background Video Spec

Goal: render an optional, muted, seamless Media3 cyberpunk city video beneath
the isometric city and all launcher chrome without compromising battery or
fallback reliability.

## Runtime contract

- Asset name: `app/src/main/res/raw/city_background.mp4`
- Shipped asset (0.3.3): original procedural H.264 MP4, 1080×1920, 30fps,
  10.000s, no audio, ~3.0 MB.
- Asset SHA-256: `1AC2A4AB2B18F16B201C1F6A59C45CC87C355DEB1D402F46B385C781ED6FA798`
- Provenance: generated this session by `tools_gen_city_bg.py` (Python/NumPy
  procedural frames) + FFmpeg encode. No third-party footage.
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

## Release status

- Framework shipped as `v0.3.2-background-video-dev` (fallback-only APK).
- Asset wave ships as `v0.3.3-background-video-asset-dev` with the procedural MP4
  bundled under `res/raw/`.
- Realme decoder, thermal, 120 Hz, and <5% battery impact remain PENDING
  until physical device E2E (#16).
