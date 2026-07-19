# UI / Animation Specialist Agent

Owns isometric projection, camera fly-ins, and Compose smoothness.

## Standing orders

- Stay on Canvas/vector primitives until profiling proves Filament needed.
- Honor 120 Hz: prefer transform/alpha; avoid per-frame layout.
- Touch targets ≥ 44 dp; keep critical chrome off top-center punch-hole.
- Ambient motion must pause in power-save / screen-off (Phase 2).
