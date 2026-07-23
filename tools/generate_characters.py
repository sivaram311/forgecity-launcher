#!/usr/bin/env python3
"""Generate vertex-colored character GLBs for ForgeCity Phase 0.10.6."""

from __future__ import annotations

import shutil
from dataclasses import dataclass
from pathlib import Path

from generate_house_assets import OUT_DIR, MeshBuilder, rgba

SKIN = 0xFFE8C4A8
SHOE = 0xFF2E241C


@dataclass(frozen=True)
class CharSpec:
    node_name: str
    filename: str
    head_body_ratio: float
    primary: int
    secondary: int
    trim: int
    total_h: float = 1.65
    leg_frac: float = 0.47
    torso_w: float = 0.24
    torso_d: float = 0.15


MAYOR = CharSpec(
    "char_mayor",
    "char_mayor.glb",
    3.8,
    0xFF2F2A24,
    0xFF5C4033,
    0xFF8B5E3C,
    total_h=1.67,
    torso_w=0.28,
    torso_d=0.17,
)
ASSIST = CharSpec(
    "char_assist",
    "char_assist.glb",
    4.1,
    0xFF3A3630,
    0xFF6B5B4F,
    0xFFA89B85,
    total_h=1.64,
    leg_frac=0.48,
    torso_w=0.20,
    torso_d=0.13,
)
NPC = CharSpec(
    "char_npc",
    "char_npc.glb",
    3.6,
    0xFF2C2823,
    0xFF4A3F35,
    0xFF958A75,
    total_h=1.66,
    leg_frac=0.45,
    torso_w=0.30,
    torso_d=0.18,
)

ROSTER: tuple[CharSpec, ...] = (MAYOR, ASSIST, NPC)


def _upper_dims(spec: CharSpec) -> tuple[float, float, float]:
    leg_h = spec.total_h * spec.leg_frac
    upper = spec.total_h - leg_h
    head_h = upper / (1.0 + spec.head_body_ratio)
    torso_h = upper - head_h
    return leg_h, torso_h, head_h


def _add_legs(
    mb: MeshBuilder,
    spec: CharSpec,
    leg_h: float,
    leg_w: float,
    leg_d: float,
    gap: float,
) -> None:
    pants = rgba(spec.primary)
    shoe = rgba(SHOE)
    half_gap = gap / 2
    mb.add_box(-leg_w - half_gap, 0, -leg_d / 2, -half_gap, leg_h, leg_d / 2, pants)
    mb.add_box(half_gap, 0, -leg_d / 2, leg_w + half_gap, leg_h, leg_d / 2, pants)
    mb.add_box(-leg_w - half_gap, 0, -leg_d / 2, -half_gap, 0.05, leg_d / 2, shoe)
    mb.add_box(half_gap, 0, -leg_d / 2, leg_w + half_gap, 0.05, leg_d / 2, shoe)


def _add_arms(
    mb: MeshBuilder,
    spec: CharSpec,
    body_y0: float,
    body_h: float,
    arm_w: float,
    arm_h: float,
    arm_d: float,
    sleeve_color: tuple[float, float, float, float],
    hand_color: tuple[float, float, float, float],
    inset: float,
) -> None:
    arm_y = body_y0 + body_h - arm_h - 0.02
    torso_half = spec.torso_w / 2
    mb.add_box(-torso_half - arm_w, arm_y, -arm_d / 2, -torso_half, arm_y + arm_h, arm_d / 2, sleeve_color)
    mb.add_box(torso_half, arm_y, -arm_d / 2, torso_half + arm_w, arm_y + arm_h, arm_d / 2, sleeve_color)
    mb.add_box(-torso_half - arm_w - inset, arm_y, -arm_d / 2, -torso_half - arm_w, arm_y + arm_h, arm_d / 2, hand_color)
    mb.add_box(torso_half + arm_w, arm_y, -arm_d / 2, torso_half + arm_w + inset, arm_y + arm_h, arm_d / 2, hand_color)


def build_mayor(spec: CharSpec) -> MeshBuilder:
    mb = MeshBuilder()
    leg_h, torso_h, head_h = _upper_dims(spec)
    leg_w, leg_d, gap = 0.13, 0.17, 0.07
    _add_legs(mb, spec, leg_h, leg_w, leg_d, gap)

    body_y0 = leg_h
    coat = rgba(spec.secondary)
    trim = rgba(spec.trim)
    accent = rgba(spec.primary)
    skin = rgba(SKIN)
    half_w, half_d = spec.torso_w / 2, spec.torso_d / 2

    mb.add_box(-half_w, body_y0, -half_d, half_w, body_y0 + torso_h, half_d, coat)
    coat_tail = 0.08
    mb.add_box(-half_w + 0.02, body_y0 - coat_tail, -half_d + 0.01, half_w - 0.02, body_y0, half_d - 0.01, accent)

    head_y0 = body_y0 + torso_h
    head_w = head_h * 0.95
    mb.add_box(-head_w / 2, head_y0, -head_w / 2, head_w / 2, head_y0 + head_h, head_w / 2, skin)

    collar_h = 0.05
    collar_y = head_y0 - 0.01
    mb.add_box(-half_w + 0.02, collar_y, half_d, half_w - 0.02, collar_y + collar_h, half_d + 0.03, trim)
    mb.add_box(-half_w * 0.55, collar_y + 0.01, half_d + 0.01, half_w * 0.55, collar_y + collar_h - 0.01, half_d + 0.03, accent)

    _add_arms(mb, spec, body_y0, torso_h, 0.10, 0.52, 0.12, coat, skin, 0.02)
    return mb


def build_assist(spec: CharSpec) -> MeshBuilder:
    mb = MeshBuilder()
    leg_h, torso_h, head_h = _upper_dims(spec)
    leg_w, leg_d, gap = 0.11, 0.15, 0.08
    _add_legs(mb, spec, leg_h, leg_w, leg_d, gap)

    body_y0 = leg_h
    shirt = rgba(spec.secondary)
    vest = rgba(spec.trim)
    pants = rgba(spec.primary)
    skin = rgba(SKIN)
    half_w, half_d = spec.torso_w / 2, spec.torso_d / 2

    mb.add_box(-half_w, body_y0, -half_d, half_w, body_y0 + torso_h, half_d, shirt)
    vest_w = half_w * 0.55
    mb.add_box(-vest_w, body_y0 + 0.04, -half_d + 0.01, vest_w, body_y0 + torso_h - 0.06, half_d - 0.01, vest)
    mb.add_box(-half_w, body_y0, -half_d, -vest_w, body_y0 + torso_h * 0.35, half_d, pants)
    mb.add_box(vest_w, body_y0, -half_d, half_w, body_y0 + torso_h * 0.35, half_d, pants)

    head_y0 = body_y0 + torso_h
    head_w = head_h * 0.92
    mb.add_box(-head_w / 2, head_y0, -head_w / 2, head_w / 2, head_y0 + head_h, head_w / 2, skin)

    _add_arms(mb, spec, body_y0, torso_h, 0.07, 0.48, 0.10, shirt, skin, 0.015)
    return mb


def build_npc(spec: CharSpec) -> MeshBuilder:
    mb = MeshBuilder()
    leg_h, torso_h, head_h = _upper_dims(spec)
    leg_w, leg_d, gap = 0.14, 0.18, 0.06
    _add_legs(mb, spec, leg_h, leg_w, leg_d, gap)

    body_y0 = leg_h
    shirt = rgba(spec.secondary)
    trim = rgba(spec.trim)
    dark = rgba(spec.primary)
    skin = rgba(SKIN)
    half_w, half_d = spec.torso_w / 2, spec.torso_d / 2

    mb.add_box(-half_w, body_y0, -half_d, half_w, body_y0 + torso_h, half_d, shirt)
    mb.add_box(-half_w, body_y0 + torso_h * 0.55, -half_d, half_w, body_y0 + torso_h * 0.62, half_d + 0.01, dark)

    head_y0 = body_y0 + torso_h
    head_w = head_h * 1.0
    mb.add_box(-head_w / 2, head_y0, -head_w / 2, head_w / 2, head_y0 + head_h, head_w / 2, skin)

    arm_w, arm_h, arm_d = 0.11, 0.46, 0.13
    arm_y = body_y0 + torso_h - arm_h - 0.02
    cuff_h = 0.07
    mb.add_box(-half_w - arm_w, arm_y, -arm_d / 2, -half_w, arm_y + arm_h, arm_d / 2, shirt)
    mb.add_box(half_w, arm_y, -arm_d / 2, half_w + arm_w, arm_y + arm_h, arm_d / 2, shirt)
    mb.add_box(-half_w - arm_w, arm_y, -arm_d / 2, -half_w, arm_y + cuff_h, arm_d / 2, trim)
    mb.add_box(half_w, arm_y, -arm_d / 2, half_w + arm_w, arm_y + cuff_h, arm_d / 2, trim)
    mb.add_box(-half_w - arm_w - 0.02, arm_y, -arm_d / 2, -half_w - arm_w, arm_y + arm_h, arm_d / 2, skin)
    mb.add_box(half_w + arm_w, arm_y, -arm_d / 2, half_w + arm_w + 0.02, arm_y + arm_h, arm_d / 2, skin)
    return mb


BUILDERS = {
    "char_mayor": build_mayor,
    "char_assist": build_assist,
    "char_npc": build_npc,
}


def build_character(spec: CharSpec) -> MeshBuilder:
    return BUILDERS[spec.node_name](spec)


def generate_all(out_dir: Path | None = None) -> list[tuple[Path, int]]:
    target = out_dir or OUT_DIR
    target.mkdir(parents=True, exist_ok=True)
    results: list[tuple[Path, int]] = []

    for spec in ROSTER:
        path = target / spec.filename
        size = build_character(spec).to_glb(path, spec.node_name)
        results.append((path, size))

    idle_path = target / "char_idle.glb"
    mayor_path = target / MAYOR.filename
    shutil.copy2(mayor_path, idle_path)
    results.append((idle_path, idle_path.stat().st_size))
    return results


def main() -> None:
    for path, size in generate_all():
        print(f"Wrote {path} ({size:,} bytes)")


if __name__ == "__main__":
    main()
