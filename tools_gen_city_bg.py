#!/usr/bin/env python3
"""
Original procedural cyberpunk night-city loop generator.
100% generated this session — no third-party footage or stock assets.
Outputs raw RGB24 frames to stdout for ffmpeg pipe encoding.
"""
from __future__ import annotations

import math
import struct
import sys
import zlib
from typing import List, Tuple

import numpy as np

# Render at half resolution / 10fps; FFmpeg performs the final high-quality
# 1080x1920 upscale and 30fps interpolation. This keeps generation practical.
W, H = 540, 960
FPS = 10
DURATION = 10.0
N_FRAMES = int(FPS * DURATION)  # 300
PERIOD = DURATION  # seamless period

# Quiet bands: top 22%, bottom 18%
TOP_QUIET = int(H * 0.22)
BOT_QUIET = int(H * 0.18)
CONTENT_TOP = TOP_QUIET
CONTENT_BOT = H - BOT_QUIET

def hash01(i: int, j: int = 0) -> float:
    """Deterministic pseudo-random in [0,1)."""
    n = (i * 374761393 + j * 668265263) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return (n & 0xFFFFFF) / 0x1000000


def smooth_pulse(t: float, phase: float = 0.0, amp: float = 0.12, base: float = 1.0) -> float:
    """Gentle non-flashing luminance modulation (one full cycle per period)."""
    return base + amp * math.sin(2.0 * math.pi * (t / PERIOD) + phase)


def make_buildings() -> List[dict]:
    """Isometric-ish elevated skyline: deep ink towers with neon accents."""
    buildings = []
    # Layers: far (small), mid, near (larger)
    layers = [
        # (y_base_frac of content, depth_scale, count, min_h, max_h, min_w, max_w)
        (0.38, 0.35, 28, 80, 220, 28, 55),
        (0.52, 0.55, 22, 120, 340, 40, 80),
        (0.68, 0.75, 16, 160, 480, 55, 110),
        (0.82, 0.95, 12, 200, 560, 70, 140),
    ]
    bid = 0
    for yf, depth, count, minh, maxh, minw, maxw in layers:
        y_base = CONTENT_TOP + int((CONTENT_BOT - CONTENT_TOP) * yf)
        xs = np.linspace(-80, W + 80, count)
        for xi, x in enumerate(xs):
            bid += 1
            h = minh + hash01(bid, 1) * (maxh - minh)
            w = minw + hash01(bid, 2) * (maxw - minw)
            # slight horizontal jitter
            xj = x + (hash01(bid, 3) - 0.5) * 40
            # isometric skew
            skew = int(18 * depth)
            tone = 6 + int(12 * depth)  # near buildings slightly lighter blacks
            neon_hue = hash01(bid, 4)  # 0..1 maps to palette
            neon_side = 0 if hash01(bid, 5) < 0.5 else 1
            win_rows = max(3, int(h / (14 + 6 * (1 - depth))))
            win_cols = max(2, int(w / (10 + 4 * (1 - depth))))
            buildings.append(
                {
                    "id": bid,
                    "x": float(xj),
                    "y_base": y_base,
                    "h": float(h),
                    "w": float(w),
                    "skew": skew,
                    "depth": depth,
                    "tone": tone,
                    "neon_hue": neon_hue,
                    "neon_side": neon_side,
                    "win_rows": win_rows,
                    "win_cols": win_cols,
                    "has_antenna": hash01(bid, 6) > 0.72,
                    "has_sign": hash01(bid, 7) > 0.45,
                    "sign_h": 8 + int(hash01(bid, 8) * 18),
                    "pulse_phase": hash01(bid, 9) * math.pi * 2,
                    "window_seed": bid * 97,
                }
            )
    return buildings


def hue_to_rgb(hue: float, sat: float = 0.85, val: float = 1.0) -> Tuple[float, float, float]:
    """Map 0..1 hue through cyberpunk palette: purple/blue/magenta/warm-orange."""
    # Remap uniform hue into discrete cyberpunk ranges
    # 0.00-0.35 purple/blue, 0.35-0.65 magenta, 0.65-1.0 warm orange
    if hue < 0.35:
        # purple -> blue (270-210 deg-ish)
        t = hue / 0.35
        r, g, b = 0.45 + 0.15 * (1 - t), 0.15 + 0.25 * t, 0.95
    elif hue < 0.65:
        t = (hue - 0.35) / 0.30
        r, g, b = 0.85 + 0.15 * t, 0.12, 0.75 + 0.15 * (1 - t)
    else:
        t = (hue - 0.65) / 0.35
        r, g, b = 1.0, 0.35 + 0.35 * t, 0.08 + 0.12 * (1 - t)
    r = r * sat * val + (1 - sat) * val * 0.15
    g = g * sat * val + (1 - sat) * val * 0.15
    b = b * sat * val + (1 - sat) * val * 0.15
    return r, g, b


def make_vehicles(n: int = 48) -> List[dict]:
    vehs = []
    for i in range(n):
        lane_yf = 0.55 + hash01(i, 20) * 0.28  # within content mid-lower
        y = CONTENT_TOP + int((CONTENT_BOT - CONTENT_TOP) * lane_yf)
        direction = 1 if hash01(i, 22) < 0.5 else -1
        laps = (1 + int(hash01(i, 21) * 3)) * direction
        color_h = hash01(i, 23)
        vehs.append(
            {
                "y": y,
                "x0": hash01(i, 24) * W,
                "laps": laps,
                "color": hue_to_rgb(color_h, 0.9, 0.9),
                "size": 2 + int(hash01(i, 25) * 3),
                "trail": 8 + int(hash01(i, 26) * 18),
            }
        )
    return vehs


def make_rain(n: int = 420) -> List[dict]:
    drops = []
    for i in range(n):
        drops.append(
            {
                "x": hash01(i, 30) * W,
                "y0": hash01(i, 31) * H,
                "len": 6 + hash01(i, 32) * 14,
                # Integer full-height laps guarantee t=0 and t=PERIOD match.
                "speed": (1 + int(hash01(i, 33) * 3)) * (H + 20) / PERIOD,
                "alpha": 0.08 + hash01(i, 34) * 0.18,
            }
        )
    return drops


def soft_disc(img: np.ndarray, cx: float, cy: float, radius: float, color: Tuple[float, float, float], strength: float):
    """Additive soft glow disc (vectorized local patch)."""
    r = int(math.ceil(radius * 2.2))
    x0 = max(0, int(cx) - r)
    x1 = min(W, int(cx) + r + 1)
    y0 = max(0, int(cy) - r)
    y1 = min(H, int(cy) + r + 1)
    if x0 >= x1 or y0 >= y1:
        return
    yy, xx = np.ogrid[y0:y1, x0:x1]
    dist = np.sqrt((xx - cx) ** 2 + (yy - cy) ** 2)
    fall = np.clip(1.0 - dist / (radius * 1.8), 0, 1) ** 2
    a = fall * strength
    for c in range(3):
        img[y0:y1, x0:x1, c] = np.clip(img[y0:y1, x0:x1, c] + a * color[c], 0, 1)


def draw_building(img: np.ndarray, b: dict, pan: float, t: float):
    x = b["x"] + pan * (0.15 + 0.85 * b["depth"])
    # wrap for seamless horizontal drift
    while x < -200:
        x += W + 400
    while x > W + 200:
        x -= W + 400

    w = b["w"]
    h = b["h"]
    yb = b["y_base"]
    yt = yb - h
    skew = b["skew"]
    tone = b["tone"] / 255.0

    # Main body rectangle with slight isometric top
    x0 = int(x - w / 2)
    x1 = int(x + w / 2)
    y0 = int(yt)
    y1 = int(yb)
    x0c, x1c = max(0, x0), min(W, x1)
    y0c, y1c = max(0, y0), min(H, y1)
    if x0c < x1c and y0c < y1c:
        base = tone * (0.7 + 0.3 * b["depth"])
        img[y0c:y1c, x0c:x1c, :] = np.maximum(img[y0c:y1c, x0c:x1c, :], base * 0.35)
        # fill solid dark tower
        img[y0c:y1c, x0c:x1c, 0] = base * 0.9
        img[y0c:y1c, x0c:x1c, 1] = base * 0.95
        img[y0c:y1c, x0c:x1c, 2] = base * 1.15  # slight cool tint

    # Isometric roof edge
    if 0 <= y0 < H:
        for dx in range(skew):
            xx0 = x0 + dx
            xx1 = x1 + dx
            yy = y0 - dx // 2
            if 0 <= yy < H:
                xa, xb = max(0, xx0), min(W, xx1)
                if xa < xb:
                    img[yy, xa:xb, :] = np.clip(img[yy, xa:xb, :] + 0.02, 0, 1)

    pulse = smooth_pulse(t, b["pulse_phase"], amp=0.10, base=0.92)

    # Windows
    rows, cols = b["win_rows"], b["win_cols"]
    if x0c < x1c and y0c < y1c and rows > 0 and cols > 0:
        for r in range(rows):
            for c in range(cols):
                lit = hash01(b["window_seed"] + r * 31 + c, 40) > 0.55
                if not lit:
                    continue
                wx = x0 + int((c + 0.5) * w / cols)
                wy = y0 + int((r + 0.6) * h / rows)
                if not (0 <= wx < W and 0 <= wy < H):
                    continue
                # occasional warm vs cool windows
                wh = hash01(b["window_seed"] + r * 7 + c, 41)
                if wh < 0.55:
                    col = (0.55 * pulse, 0.75 * pulse, 1.0 * pulse)  # cool
                elif wh < 0.85:
                    col = (0.95 * pulse, 0.45 * pulse, 0.85 * pulse)  # magenta
                else:
                    col = (1.0 * pulse, 0.55 * pulse, 0.2 * pulse)  # warm
                ww, wht = 2, 3
                xa, xb = max(0, wx), min(W, wx + ww)
                ya, yb2 = max(0, wy), min(H, wy + wht)
                if xa < xb and ya < yb2:
                    img[ya:yb2, xa:xb, 0] = col[0]
                    img[ya:yb2, xa:xb, 1] = col[1]
                    img[ya:yb2, xa:xb, 2] = col[2]
                # tiny glow
                if hash01(b["window_seed"] + r + c, 42) > 0.82:
                    soft_disc(img, wx + 1, wy + 1, 6, col, 0.08 * pulse)

    # Vertical neon edge strip
    nr, ng, nb = hue_to_rgb(b["neon_hue"], 0.95, pulse)
    nx = x1 - 3 if b["neon_side"] else x0 + 1
    if 0 <= nx < W - 1 and y0c < y1c:
        strip_h0, strip_h1 = y0c, y1c
        for yy in range(strip_h0, strip_h1):
            fade = 0.35 + 0.65 * (0.5 + 0.5 * math.sin(yy * 0.08 + b["pulse_phase"]))
            img[yy, nx, 0] = min(1.0, nr * fade)
            img[yy, nx, 1] = min(1.0, ng * fade)
            img[yy, nx, 2] = min(1.0, nb * fade)
            if nx + 1 < W:
                img[yy, nx + 1, 0] = min(1.0, nr * fade * 0.5)
                img[yy, nx + 1, 1] = min(1.0, ng * fade * 0.5)
                img[yy, nx + 1, 2] = min(1.0, nb * fade * 0.5)
        soft_disc(img, nx, (y0c + y1c) / 2, 18 + 20 * b["depth"], (nr, ng, nb), 0.06 * pulse)

    # Horizontal neon sign
    if b["has_sign"]:
        sh = b["sign_h"]
        sy = int(y0 + h * (0.25 + 0.4 * hash01(b["id"], 50)))
        sx0 = max(0, x0 + 4)
        sx1 = min(W, x1 - 4)
        if sx0 < sx1 and 0 <= sy < H - sh:
            nr, ng, nb = hue_to_rgb((b["neon_hue"] + 0.2) % 1.0, 0.95, pulse)
            img[sy : sy + max(2, sh // 3), sx0:sx1, 0] = nr
            img[sy : sy + max(2, sh // 3), sx0:sx1, 1] = ng
            img[sy : sy + max(2, sh // 3), sx0:sx1, 2] = nb
            soft_disc(img, (sx0 + sx1) / 2, sy, 22, (nr, ng, nb), 0.12 * pulse)

    # Antenna
    if b["has_antenna"] and 0 <= int(x) < W and y0 - 40 < H:
        ax = int(x)
        for ay in range(max(0, y0 - 40), max(0, y0)):
            if 0 <= ax < W:
                img[ay, ax, :] = np.clip(img[ay, ax, :] + 0.15, 0, 1)
        soft_disc(img, ax, y0 - 38, 10, hue_to_rgb(b["neon_hue"], 1.0, 1.0), 0.25 * pulse)


def render_frame(t: float, buildings, vehicles, rain) -> np.ndarray:
    # Start near black with cool gradient
    img = np.zeros((H, W, 3), dtype=np.float32)
    yy = np.linspace(0, 1, H, dtype=np.float32)[:, None]
    # sky gradient: deep ink purple-black
    img[:, :, 0] = 0.02 + 0.03 * (1 - yy)
    img[:, :, 1] = 0.01 + 0.02 * (1 - yy)
    img[:, :, 2] = 0.04 + 0.06 * (1 - yy)

    # distant horizon glow band (centered content)
    mid = (CONTENT_TOP + CONTENT_BOT) / 2
    for k, col, rad, strength in [
        (0.0, (0.25, 0.1, 0.45), 280, 0.18),
        (0.15, (0.15, 0.2, 0.55), 200, 0.12),
        (-0.1, (0.45, 0.15, 0.2), 160, 0.10),
    ]:
        soft_disc(img, W * (0.5 + k), mid + 80, rad, col, strength)

    # subtle stars / distant city sparkle (static + gentle pulse)
    pulse_g = smooth_pulse(t, 0.0, 0.08, 1.0)
    for i in range(90):
        sx = int(hash01(i, 60) * W)
        sy = int(hash01(i, 61) * CONTENT_TOP * 0.9)
        br = 0.15 + 0.35 * hash01(i, 62)
        if 0 <= sx < W and 0 <= sy < H:
            img[sy, sx, :] = np.clip(img[sy, sx, :] + br * 0.25 * pulse_g, 0, 1)

    # horizontal pan for parallax (seamless: full cycle returns)
    pan = 48.0 * math.sin(2.0 * math.pi * t / PERIOD)

    # Sort buildings far to near
    for b in sorted(buildings, key=lambda z: z["depth"]):
        draw_building(img, b, pan, t)

    # Street glow floor across mid-lower content
    street_y = CONTENT_TOP + int((CONTENT_BOT - CONTENT_TOP) * 0.78)
    for x in range(0, W, 4):
        g = 0.04 + 0.03 * math.sin(x * 0.02 + 2.0 * math.pi * t / PERIOD)
        y0 = max(0, street_y - 6)
        y1 = min(H, street_y + 40)
        img[y0:y1, x, 0] = np.clip(img[y0:y1, x, 0] + g * 0.5, 0, 1)
        img[y0:y1, x, 1] = np.clip(img[y0:y1, x, 1] + g * 0.2, 0, 1)
        img[y0:y1, x, 2] = np.clip(img[y0:y1, x, 2] + g * 0.35, 0, 1)

    # Vehicle lights (seamless wrap)
    for v in vehicles:
        # position advances and wraps over period
        x = (
            v["x0"] +
            v["laps"] * (W + 40) * (t / PERIOD) +
            pan * 0.2
        ) % (W + 40) - 20
        y = v["y"]
        col = v["color"]
        soft_disc(img, x, y, 5 + v["size"], col, 0.55)
        # short trail
        trail_dir = -1 if v["laps"] > 0 else 1
        for k in range(v["trail"]):
            tx = x + trail_dir * k * 1.4
            soft_disc(img, tx, y, 2.5, col, 0.08 * (1 - k / v["trail"]))

    # Fog layers (soft horizontal noise bands) — animated slowly, loops
    fog_phase = 2.0 * math.pi * t / PERIOD
    for band in range(5):
        by = CONTENT_TOP + int((CONTENT_BOT - CONTENT_TOP) * (0.25 + band * 0.12))
        thickness = 40 + band * 12
        y0 = max(0, by - thickness)
        y1 = min(H, by + thickness)
        if y0 >= y1:
            continue
        rows = y1 - y0
        # soft vertical falloff
        vy = np.linspace(-1, 1, rows, dtype=np.float32)
        fall = np.exp(-vy * vy * 2.5)[:, None]
        # horizontal drift
        xs = np.arange(W, dtype=np.float32)
        noise = 0.5 + 0.5 * np.sin(xs * 0.01 + fog_phase * (1 + band * 0.2) + band)
        fog_col = np.array([0.12, 0.08, 0.18], dtype=np.float32) * (0.04 + 0.02 * band)
        layer = fall * noise[None, :] * 0.35
        for c in range(3):
            img[y0:y1, :, c] = np.clip(img[y0:y1, :, c] + layer * fog_col[c], 0, 1)

    # Rain (seamless: modular positions)
    for d in rain:
        y = (d["y0"] + d["speed"] * t) % (H + 20) - 10
        x = (d["x"] + pan * 0.05 + 8 * math.sin(fog_phase + d["x"] * 0.01)) % W
        length = d["len"]
        a = d["alpha"]
        xi = int(x)
        yi = int(y)
        if 0 <= xi < W:
            for dy in range(int(length)):
                yy2 = yi + dy
                if 0 <= yy2 < H:
                    img[yy2, xi, :] = np.clip(img[yy2, xi, :] + a * 0.55, 0, 1)

    # Atmospheric vignette / quiet bands
    # Top quiet 22%: darken strongly
    top_fade = np.linspace(1.0, 0.0, TOP_QUIET, dtype=np.float32) ** 1.4
    for i in range(TOP_QUIET):
        m = 1.0 - 0.88 * top_fade[i]
        img[i, :, :] *= m
        # keep near black
        img[i, :, :] = img[i, :, :] * 0.35 + np.array([0.01, 0.01, 0.02]) * (1 - 0.35)

    # Bottom quiet 18%
    bot_fade = np.linspace(0.0, 1.0, BOT_QUIET, dtype=np.float32) ** 1.4
    for i in range(BOT_QUIET):
        row = H - BOT_QUIET + i
        m = 1.0 - 0.92 * bot_fade[i]
        img[row, :, :] *= m
        img[row, :, :] = img[row, :, :] * 0.25 + np.array([0.005, 0.005, 0.01]) * (1 - 0.25)

    # Soft side vignette
    xv = np.linspace(-1, 1, W, dtype=np.float32)
    side = 1.0 - 0.22 * (np.abs(xv) ** 2)
    img *= side[None, :, None]

    # Film grain (subtle, temporal for life, periodic)
    # reseed style: use deterministic grain from frame index for loop seamlessness at t=0 and t=T
    # Actually for seamless loop, grain must match at t=0 and t=T. Use time-periodic grain.
    fi = int(round(t * FPS)) % N_FRAMES
    g = np.zeros((H, W), dtype=np.float32)
    # cheap procedural grain
    ys = np.arange(H, dtype=np.float32)[:, None]
    xs = np.arange(W, dtype=np.float32)[None, :]
    g = 0.012 * np.sin(xs * 12.9898 + ys * 78.233 + fi * 0.17) * np.cos(xs * 0.5 + fi * 0.31)
    img = np.clip(img + g[:, :, None], 0, 1)

    # Ensure first and conceptual last frame continuity via continuous functions only (done)

    out = (img * 255.0).astype(np.uint8)
    return out


def main():
    buildings = make_buildings()
    vehicles = make_vehicles()
    rain = make_rain()
    # Stream raw RGB frames to stdout
    # Two wrapped look-ahead frames let FFmpeg interpolate through the loop
    # boundary while the final encode is trimmed to exactly 300 frames.
    render_frames = N_FRAMES + 2
    for fi in range(render_frames):
        t = fi / FPS
        frame = render_frame(t, buildings, vehicles, rain)
        sys.stdout.buffer.write(frame.tobytes())
        if fi % 30 == 0:
            sys.stderr.write(f"frame {fi}/{render_frames}\n")
            sys.stderr.flush()
    sys.stderr.write(f"done {render_frames} frames\n")


if __name__ == "__main__":
    main()
