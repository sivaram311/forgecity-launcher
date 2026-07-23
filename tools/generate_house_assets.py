#!/usr/bin/env python3
"""Generate minimal vertex-colored GLB assets for ForgeCity Filament house HOME."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

import numpy as np
from pygltflib import (
    GLTF2,
    Accessor,
    Asset,
    Buffer,
    BufferView,
    Material,
    Mesh,
    Node,
    PbrMetallicRoughness,
    Primitive,
    Scene,
)

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "app" / "src" / "main" / "assets" / "filament"


@dataclass(frozen=True)
class RoomSpec:
    name: str
    min_x: float
    min_z: float
    max_x: float
    max_z: float
    floor: int
    wall: int


# Matches buzz.delena.forgecity.house.HouseRoom bounds (meters, SW origin).
ROOMS: tuple[RoomSpec, ...] = (
    RoomSpec("kitchen", 0, 0, 3, 3, 0xFFC9A66B, 0xFF8F6A3E),
    RoomSpec("living", 3, 0, 7, 3, 0xFFB8956A, 0xFF7A5138),
    RoomSpec("hallway", 0, 3, 3, 6, 0xFFD4C4A8, 0xFF9A8060),
    RoomSpec("office", 3, 3, 7, 6, 0xFFA8885C, 0xFF6E4C32),
    RoomSpec("bedroom", 0, 6, 3, 9, 0xFFC4A882, 0xFF8B6848),
    RoomSpec("workshop", 3, 6, 7, 9, 0xFFB07A4A, 0xFF6B4226),
    RoomSpec("vault", 7, 3, 9, 6, 0xFF8A7340, 0xFFC9A227),
)

WALL_H = 2.5
FLOOR_T = 0.05
WALL_T = 0.1
DOOR_W = 0.9


def rgba(argb: int) -> tuple[float, float, float, float]:
    return (
        ((argb >> 16) & 0xFF) / 255.0,
        ((argb >> 8) & 0xFF) / 255.0,
        (argb & 0xFF) / 255.0,
        1.0,
    )


class MeshBuilder:
    def __init__(self) -> None:
        self._verts: list[tuple[float, float, float]] = []
        self._colors: list[tuple[float, float, float, float]] = []
        self._indices: list[int] = []

    def add_box(
        self,
        x0: float,
        y0: float,
        z0: float,
        x1: float,
        y1: float,
        z1: float,
        color: tuple[float, float, float, float],
    ) -> None:
        if x1 < x0:
            x0, x1 = x1, x0
        if y1 < y0:
            y0, y1 = y1, y0
        if z1 < z0:
            z0, z1 = z1, z0
        if x1 - x0 < 1e-6 or y1 - y0 < 1e-6 or z1 - z0 < 1e-6:
            return

        base = len(self._verts)
        corners = (
            (x0, y0, z0),
            (x1, y0, z0),
            (x1, y0, z1),
            (x0, y0, z1),
            (x0, y1, z0),
            (x1, y1, z0),
            (x1, y1, z1),
            (x0, y1, z1),
        )
        self._verts.extend(corners)
        self._colors.extend([color] * 8)
        faces = (
            (0, 2, 1),
            (0, 3, 2),
            (4, 5, 6),
            (4, 6, 7),
            (0, 1, 5),
            (0, 5, 4),
            (1, 2, 6),
            (1, 6, 5),
            (2, 3, 7),
            (2, 7, 6),
            (3, 0, 4),
            (3, 4, 7),
        )
        for a, b, c in faces:
            self._indices.extend((base + a, base + b, base + c))

    def add_wall_x(
        self,
        x: float,
        z0: float,
        z1: float,
        color: tuple[float, float, float, float],
        door_center: float | None = None,
    ) -> None:
        if door_center is None:
            self.add_box(x - WALL_T / 2, FLOOR_T, z0, x + WALL_T / 2, WALL_H, z1, color)
            return
        half = DOOR_W / 2
        if door_center - half > z0:
            self.add_box(x - WALL_T / 2, FLOOR_T, z0, x + WALL_T / 2, WALL_H, door_center - half, color)
        if door_center + half < z1:
            self.add_box(x - WALL_T / 2, FLOOR_T, door_center + half, x + WALL_T / 2, WALL_H, z1, color)

    def add_wall_z(
        self,
        z: float,
        x0: float,
        x1: float,
        color: tuple[float, float, float, float],
        door_center: float | None = None,
    ) -> None:
        if door_center is None:
            self.add_box(x0, FLOOR_T, z - WALL_T / 2, x1, WALL_H, z + WALL_T / 2, color)
            return
        half = DOOR_W / 2
        if door_center - half > x0:
            self.add_box(x0, FLOOR_T, z - WALL_T / 2, door_center - half, WALL_H, z + WALL_T / 2, color)
        if door_center + half < x1:
            self.add_box(door_center + half, FLOOR_T, z - WALL_T / 2, x1, WALL_H, z + WALL_T / 2, color)

    def to_glb(self, path: Path, node_name: str) -> int:
        if not self._verts:
            raise ValueError("empty mesh")

        positions = np.array(self._verts, dtype=np.float32)
        colors = np.array(self._colors, dtype=np.float32)
        indices = np.array(self._indices, dtype=np.uint16)

        pos_bytes = positions.tobytes()
        col_bytes = colors.tobytes()
        idx_bytes = indices.tobytes()
        pos_pad = (4 - len(pos_bytes) % 4) % 4
        col_pad = (4 - len(col_bytes) % 4) % 4
        blob = pos_bytes + b"\x00" * pos_pad + col_bytes + b"\x00" * col_pad + idx_bytes

        pos_len = len(pos_bytes)
        col_off = pos_len + pos_pad
        col_len = len(col_bytes)
        idx_off = col_off + col_len + col_pad
        idx_len = len(idx_bytes)

        gltf = GLTF2(
            asset=Asset(version="2.0", generator="forgecity generate_house_assets.py"),
            scene=0,
            scenes=[Scene(nodes=[0])],
            nodes=[Node(mesh=0, name=node_name)],
            meshes=[Mesh(primitives=[Primitive(
                attributes={"POSITION": 0, "COLOR_0": 1},
                indices=2,
                material=0,
            )])],
            materials=[Material(
                name="VertexColor",
                pbrMetallicRoughness=PbrMetallicRoughness(
                    baseColorFactor=[1.0, 1.0, 1.0, 1.0],
                    metallicFactor=0.0,
                    roughnessFactor=0.92,
                ),
                doubleSided=True,
            )],
            buffers=[Buffer(byteLength=len(blob))],
            bufferViews=[
                BufferView(buffer=0, byteOffset=0, byteLength=pos_len, target=34962),
                BufferView(buffer=0, byteOffset=col_off, byteLength=col_len, target=34962),
                BufferView(buffer=0, byteOffset=idx_off, byteLength=idx_len, target=34963),
            ],
            accessors=[
                Accessor(
                    bufferView=0,
                    componentType=5126,
                    count=len(positions),
                    type="VEC3",
                    min=positions.min(axis=0).tolist(),
                    max=positions.max(axis=0).tolist(),
                ),
                Accessor(
                    bufferView=1,
                    componentType=5126,
                    count=len(colors),
                    type="VEC4",
                ),
                Accessor(
                    bufferView=2,
                    componentType=5123,
                    count=len(indices),
                    type="SCALAR",
                ),
            ],
        )

        path.parent.mkdir(parents=True, exist_ok=True)
        gltf.set_binary_blob(blob)
        gltf.save(str(path))
        return path.stat().st_size


def build_house_shell() -> MeshBuilder:
    mb = MeshBuilder()
    cream = rgba(0xFFF5E6D3)

    for room in ROOMS:
        fc = rgba(room.floor)
        wc = rgba(room.wall)
        mb.add_box(room.min_x, 0, room.min_z, room.max_x, FLOOR_T, room.max_z, fc)
        # Per-room perimeter walls (north/south/east/west edges).
        mb.add_wall_z(room.min_z, room.min_x, room.max_x, wc)
        mb.add_wall_z(room.max_z, room.min_x, room.max_x, wc)
        mb.add_wall_x(room.min_x, room.min_z, room.max_z, wc)
        mb.add_wall_x(room.max_x, room.min_z, room.max_z, wc)

    # Shared internal walls with doorways aligned to Compose floor-plan.
    hall = rgba(ROOMS[2].wall)
    off = rgba(ROOMS[3].wall)
    vault = rgba(ROOMS[6].wall)

    mb.add_wall_z(3.0, 0, 3, hall, door_center=1.5)       # kitchen ↔ hallway
    mb.add_wall_z(3.0, 3, 7, off, door_center=5.0)        # living ↔ office
    mb.add_wall_x(3.0, 3, 6, hall, door_center=4.5)       # hallway ↔ office
    mb.add_wall_z(6.0, 0, 3, hall, door_center=1.5)       # hallway ↔ bedroom
    mb.add_wall_z(6.0, 3, 7, off, door_center=5.0)        # office ↔ workshop
    mb.add_wall_x(7.0, 3, 6, vault, door_center=4.5)      # office ↔ vault annex

    # Outer shell (9×9 m footprint incl. vault annex).
    mb.add_wall_x(0.0, 0, 9, cream)
    mb.add_wall_x(9.0, 0, 9, cream)
    mb.add_wall_z(0.0, 0, 9, cream)
    mb.add_wall_z(9.0, 0, 9, cream)

    return mb


def build_char_idle() -> MeshBuilder:
    mb = MeshBuilder()
    skin = rgba(0xFFE8C4A8)
    shirt = rgba(0xFF6B7FA3)
    pants = rgba(0xFF4A3828)
    shoe = rgba(0xFF2E241C)

    # ~1.6 m tall block humanoid, origin at feet center.
    leg_w, leg_h, leg_d = 0.14, 0.55, 0.18
    leg_gap = 0.08
    mb.add_box(-leg_w - leg_gap / 2, 0, -leg_d / 2, -leg_gap / 2, leg_h, leg_d / 2, pants)
    mb.add_box(leg_gap / 2, 0, -leg_d / 2, leg_w + leg_gap / 2, leg_h, leg_d / 2, pants)
    mb.add_box(-leg_w - leg_gap / 2, 0, -leg_d / 2, -leg_gap / 2, 0.06, leg_d / 2, shoe)
    mb.add_box(leg_gap / 2, 0, -leg_d / 2, leg_w + leg_gap / 2, 0.06, leg_d / 2, shoe)

    body_y0 = leg_h
    body_h = 0.70
    mb.add_box(-0.22, body_y0, -0.14, 0.22, body_y0 + body_h, 0.14, shirt)

    head_size = 0.32
    head_y0 = body_y0 + body_h
    mb.add_box(-head_size / 2, head_y0, -head_size / 2, head_size / 2, head_y0 + head_size, head_size / 2, skin)

    # Simple arms.
    arm_w, arm_h, arm_d = 0.10, 0.52, 0.12
    arm_y = body_y0 + body_h - arm_h
    mb.add_box(-0.32, arm_y, -arm_d / 2, -0.22, arm_y + arm_h, arm_d / 2, shirt)
    mb.add_box(0.22, arm_y, -arm_d / 2, 0.32, arm_y + arm_h, arm_d / 2, shirt)
    mb.add_box(-0.34, arm_y, -arm_d / 2, -0.32, arm_y + 0.12, arm_d / 2, skin)
    mb.add_box(0.32, arm_y, -arm_d / 2, 0.34, arm_y + 0.12, arm_d / 2, skin)

    return mb


def main() -> None:
    house_path = OUT_DIR / "house_shell.glb"
    char_path = OUT_DIR / "char_idle.glb"

    house_size = build_house_shell().to_glb(house_path, "house_shell")
    char_size = build_char_idle().to_glb(char_path, "char_idle")

    print(f"Wrote {house_path} ({house_size:,} bytes)")
    print(f"Wrote {char_path} ({char_size:,} bytes)")


if __name__ == "__main__":
    main()
