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

MAT_VERTEX = 0
MAT_WINDOW = 1

WALL_H = 2.5
FLOOR_T = 0.05
WALL_T = 0.1
DOOR_W = 0.9
STRIPE_W = 0.2
BASEBOARD_H = 0.08
BASEBOARD_D = 0.04
TRIM_CREAM = 0xFFF5E6D3
TRIM_WOOD = 0xFFC4A070


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


def rgba(argb: int) -> tuple[float, float, float, float]:
    return (
        ((argb >> 16) & 0xFF) / 255.0,
        ((argb >> 8) & 0xFF) / 255.0,
        (argb & 0xFF) / 255.0,
        1.0,
    )


def shade(argb: int, delta: int) -> tuple[float, float, float, float]:
    r = max(0, min(255, ((argb >> 16) & 0xFF) + delta))
    g = max(0, min(255, ((argb >> 8) & 0xFF) + delta))
    b = max(0, min(255, (argb & 0xFF) + delta))
    return (r / 255.0, g / 255.0, b / 255.0, 1.0)


def floor_stripe_colors(base: int) -> tuple[tuple[float, float, float, float], tuple[float, float, float, float]]:
    return shade(base, -18), shade(base, 14)


class MeshBuilder:
    def __init__(self) -> None:
        self._verts: list[tuple[float, float, float]] = []
        self._colors: list[tuple[float, float, float, float]] = []
        self._indices: list[int] = []
        self._cur_mat = MAT_VERTEX
        self._idx_start = 0
        self._segments: list[tuple[int, int, int]] = []

    def _set_material(self, material: int) -> None:
        if material == self._cur_mat:
            return
        if self._indices:
            self._segments.append((self._cur_mat, self._idx_start, len(self._indices)))
            self._idx_start = len(self._indices)
        self._cur_mat = material

    def _finalize_segments(self) -> list[tuple[int, int, int]]:
        if not self._indices:
            return []
        if not self._segments or self._segments[-1][2] < len(self._indices):
            self._segments.append((self._cur_mat, self._idx_start, len(self._indices)))
        return self._segments

    def add_box(
        self,
        x0: float,
        y0: float,
        z0: float,
        x1: float,
        y1: float,
        z1: float,
        color: tuple[float, float, float, float],
        material: int = MAT_VERTEX,
    ) -> None:
        if x1 < x0:
            x0, x1 = x1, x0
        if y1 < y0:
            y0, y1 = y1, y0
        if z1 < z0:
            z0, z1 = z1, z0
        if x1 - x0 < 1e-6 or y1 - y0 < 1e-6 or z1 - z0 < 1e-6:
            return

        self._set_material(material)
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

    def add_striped_floor(self, room: RoomSpec) -> None:
        c0, c1 = floor_stripe_colors(room.floor)
        x0, z0, x1, z1 = room.min_x, room.min_z, room.max_x, room.max_z
        z = z0
        stripe = 0
        while z < z1 - 1e-6:
            z_end = min(z + STRIPE_W, z1)
            self.add_box(x0, 0, z, x1, FLOOR_T, z_end, c0 if stripe % 2 == 0 else c1)
            z = z_end
            stripe += 1

    def add_wall_x(
        self,
        x: float,
        z0: float,
        z1: float,
        wall_argb: int,
        door_center: float | None = None,
        tint: int = 0,
    ) -> None:
        wc = shade(wall_argb, tint)
        if door_center is None:
            self.add_box(x - WALL_T / 2, FLOOR_T, z0, x + WALL_T / 2, WALL_H, z1, wc)
            return
        half = DOOR_W / 2
        if door_center - half > z0:
            self.add_box(x - WALL_T / 2, FLOOR_T, z0, x + WALL_T / 2, WALL_H, door_center - half, wc)
        if door_center + half < z1:
            self.add_box(x - WALL_T / 2, FLOOR_T, door_center + half, x + WALL_T / 2, WALL_H, z1, wc)

    def add_wall_z(
        self,
        z: float,
        x0: float,
        x1: float,
        wall_argb: int,
        door_center: float | None = None,
        tint: int = 0,
    ) -> None:
        wc = shade(wall_argb, tint)
        if door_center is None:
            self.add_box(x0, FLOOR_T, z - WALL_T / 2, x1, WALL_H, z + WALL_T / 2, wc)
            return
        half = DOOR_W / 2
        if door_center - half > x0:
            self.add_box(x0, FLOOR_T, z - WALL_T / 2, door_center - half, WALL_H, z + WALL_T / 2, wc)
        if door_center + half < x1:
            self.add_box(door_center + half, FLOOR_T, z - WALL_T / 2, x1, WALL_H, z + WALL_T / 2, wc)

    def add_baseboard_z(self, z: float, x0: float, x1: float, inward: float) -> None:
        cream = rgba(TRIM_CREAM)
        if inward > 0:
            self.add_box(x0, FLOOR_T, z, x1, FLOOR_T + BASEBOARD_H, z + BASEBOARD_D, cream)
        else:
            self.add_box(x0, FLOOR_T, z - BASEBOARD_D, x1, FLOOR_T + BASEBOARD_H, z, cream)

    def add_baseboard_x(self, x: float, z0: float, z1: float, inward: float) -> None:
        wood = rgba(TRIM_WOOD)
        if inward > 0:
            self.add_box(x, FLOOR_T, z0, x + BASEBOARD_D, FLOOR_T + BASEBOARD_H, z1, wood)
        else:
            self.add_box(x - BASEBOARD_D, FLOOR_T, z0, x, FLOOR_T + BASEBOARD_H, z1, wood)

    def add_window_x(
        self,
        x: float,
        z_center: float,
        z_half: float,
        y0: float,
        y1: float,
        outward: float,
    ) -> None:
        glow = rgba(0xFFFFE8B8)
        wx = x + outward * 0.04
        self.add_box(
            wx - 0.01, y0, z_center - z_half,
            wx + 0.01, y1, z_center + z_half,
            glow, material=MAT_WINDOW,
        )

    def add_window_z(
        self,
        z: float,
        x_center: float,
        x_half: float,
        y0: float,
        y1: float,
        outward: float,
    ) -> None:
        glow = rgba(0xFFFFD890)
        wz = z + outward * 0.04
        self.add_box(
            x_center - x_half, y0, wz - 0.01,
            x_center + x_half, y1, wz + 0.01,
            glow, material=MAT_WINDOW,
        )

    def add_floor_lamp(self, cx: float, cz: float) -> None:
        pole_h = 1.45
        pole_r = 0.018
        shade = rgba(0xFFFFB040)
        metal = rgba(0xFF4A4E54)
        self.add_box(
            cx - pole_r, FLOOR_T, cz - pole_r,
            cx + pole_r, FLOOR_T + pole_h, cz + pole_r,
            metal,
        )
        y0 = FLOOR_T + pole_h
        self.add_box(
            cx - 0.1, y0, cz - 0.1,
            cx + 0.1, y0 + 0.12, cz + 0.1,
            shade, material=MAT_WINDOW,
        )

    def add_table_chairs(
        self,
        cx: float,
        cz: float,
        table_w: float,
        table_d: float,
        wood: tuple[float, float, float, float],
        seat: tuple[float, float, float, float],
    ) -> None:
        th = 0.75
        ch, cw, cd = 0.45, 0.38, 0.38
        self.add_box(cx - table_w / 2, FLOOR_T, cz - table_d / 2, cx + table_w / 2, FLOOR_T + th, cz + table_d / 2, wood)
        leg = 0.06
        for sx, sz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            lx0 = cx + sx * (table_w / 2 - leg)
            lz0 = cz + sz * (table_d / 2 - leg)
            lx1 = cx + sx * table_w / 2
            lz1 = cz + sz * table_d / 2
            self.add_box(lx0, FLOOR_T, lz0, lx1, FLOOR_T + th - 0.04, lz1, rgba(0xFF5A3D28))
        chair_off = max(table_w, table_d) * 0.55
        for ox, oz in ((0, chair_off), (0, -chair_off)):
            self.add_box(cx + ox - cw / 2, FLOOR_T, cz + oz - cd / 2, cx + ox + cw / 2, FLOOR_T + ch, cz + oz + cd / 2, seat)
            self.add_box(cx + ox - cw / 2, FLOOR_T + ch, cz + oz - cd / 2, cx + ox + cw / 2, FLOOR_T + ch + 0.35, cz + oz + cd / 2, wood)

    def to_glb(self, path: Path, node_name: str) -> int:
        if not self._verts:
            raise ValueError("empty mesh")

        segments = self._finalize_segments()
        positions = np.array(self._verts, dtype=np.float32)
        colors = np.array(self._colors, dtype=np.float32)
        all_indices = np.array(self._indices, dtype=np.uint16)

        pos_bytes = positions.tobytes()
        col_bytes = colors.tobytes()
        pos_pad = (4 - len(pos_bytes) % 4) % 4
        col_pad = (4 - len(col_bytes) % 4) % 4
        blob = pos_bytes + b"\x00" * pos_pad + col_bytes + b"\x00" * col_pad
        col_off = len(pos_bytes) + pos_pad
        cursor = col_off + len(col_bytes) + col_pad

        buffer_views = [
            BufferView(buffer=0, byteOffset=0, byteLength=len(pos_bytes), target=34962),
            BufferView(buffer=0, byteOffset=col_off, byteLength=len(col_bytes), target=34962),
        ]
        accessors = [
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
        ]
        primitives: list[Primitive] = []

        for mat, start, end in segments:
            if end <= start:
                continue
            slice_idx = all_indices[start:end]
            slice_bytes = slice_idx.tobytes()
            blob += slice_bytes
            buffer_views.append(
                BufferView(buffer=0, byteOffset=cursor, byteLength=len(slice_bytes), target=34963),
            )
            accessors.append(Accessor(
                bufferView=len(buffer_views) - 1,
                componentType=5123,
                count=len(slice_idx),
                type="SCALAR",
            ))
            primitives.append(Primitive(
                attributes={"POSITION": 0, "COLOR_0": 1},
                indices=len(accessors) - 1,
                material=mat,
            ))
            cursor += len(slice_bytes)

        mat_defs = {
            MAT_VERTEX: Material(
                name="VertexColor",
                pbrMetallicRoughness=PbrMetallicRoughness(
                    baseColorFactor=[1.0, 1.0, 1.0, 1.0],
                    metallicFactor=0.0,
                    roughnessFactor=0.92,
                ),
                doubleSided=True,
            ),
            MAT_WINDOW: Material(
                name="WindowGlow",
                pbrMetallicRoughness=PbrMetallicRoughness(
                    baseColorFactor=[1.0, 1.0, 1.0, 1.0],
                    metallicFactor=0.0,
                    roughnessFactor=0.35,
                ),
                emissiveFactor=[0.95, 0.80, 0.50],
                doubleSided=True,
            ),
        }
        used = sorted({mat for mat, _, _ in segments})
        mat_remap = {old: new for new, old in enumerate(used)}
        materials = [mat_defs[m] for m in used]
        for prim in primitives:
            prim.material = mat_remap[prim.material]

        gltf = GLTF2(
            asset=Asset(version="2.0", generator="forgecity generate_house_assets.py"),
            scene=0,
            scenes=[Scene(nodes=[0])],
            nodes=[Node(mesh=0, name=node_name)],
            meshes=[Mesh(primitives=primitives)],
            materials=materials,
            buffers=[Buffer(byteLength=len(blob))],
            bufferViews=buffer_views,
            accessors=accessors,
        )

        path.parent.mkdir(parents=True, exist_ok=True)
        gltf.set_binary_blob(blob)
        gltf.save(str(path))
        return path.stat().st_size


def _room_by_name(name: str) -> RoomSpec:
    return next(r for r in ROOMS if r.name == name)


def _add_room_baseboards(mb: MeshBuilder, room: RoomSpec) -> None:
    mb.add_baseboard_z(room.min_z, room.min_x + 0.02, room.max_x - 0.02, inward=1)
    mb.add_baseboard_z(room.max_z, room.min_x + 0.02, room.max_x - 0.02, inward=-1)
    mb.add_baseboard_x(room.min_x, room.min_z + 0.02, room.max_z - 0.02, inward=1)
    mb.add_baseboard_x(room.max_x, room.min_z + 0.02, room.max_z - 0.02, inward=-1)


def _add_exterior_windows(mb: MeshBuilder) -> None:
    wy0, wy1 = 0.9, 2.0
    mb.add_window_z(0.0, 1.5, 0.55, wy0, wy1, outward=-1)
    mb.add_window_x(0.0, 1.5, 0.55, wy0, wy1, outward=-1)
    mb.add_window_z(0.0, 5.0, 0.7, wy0, wy1, outward=-1)
    mb.add_window_z(0.0, 3.8, 0.5, wy0, wy1, outward=-1)
    mb.add_window_x(0.0, 7.5, 0.55, wy0, wy1, outward=-1)
    mb.add_window_z(9.0, 1.5, 0.55, wy0, wy1, outward=1)
    mb.add_window_z(9.0, 5.0, 0.7, wy0, wy1, outward=1)
    mb.add_window_x(9.0, 4.5, 0.45, wy0, wy1, outward=1)


def _add_cable_runs(mb: MeshBuilder) -> None:
    cable = rgba(0xFF2A2420)
    h, w, d = 0.025, 0.06, 0.04
    mb.add_box(2.88, FLOOR_T, 3.05, 2.88 + w, FLOOR_T + h, 5.95, cable)
    mb.add_box(0.05, FLOOR_T, 3.05, 2.88, FLOOR_T + h, 3.05 + d, cable)
    mb.add_box(3.05, FLOOR_T, 3.05, 3.05 + d, FLOOR_T + h, 5.95, cable)
    mb.add_box(3.05, FLOOR_T, 6.0, 3.05 + d, FLOOR_T + h, 8.85, cable)


def _add_furniture(mb: MeshBuilder) -> None:
    wood = rgba(0xFF5A3D28)
    wood_light = rgba(0xFF8B6344)
    linen = rgba(0xFFD8C8B0)
    metal = rgba(0xFF6A6E74)
    gold = rgba(0xFFC9A227)
    seat = rgba(0xFF6B4A38)

    mb.add_box(0.35, FLOOR_T, 2.15, 2.65, FLOOR_T + 0.9, 2.75, wood_light)
    mb.add_box(0.35, FLOOR_T + 0.9, 2.15, 2.65, FLOOR_T + 0.95, 2.75, rgba(0xFF9A9088))

    mb.add_table_chairs(5.0, 1.6, 1.1, 0.7, wood, seat)
    mb.add_floor_lamp(3.55, 0.75)
    mb.add_table_chairs(5.0, 4.5, 1.0, 0.55, wood, seat)
    mb.add_floor_lamp(6.35, 3.55)

    mb.add_box(0.4, FLOOR_T, 7.0, 2.5, FLOOR_T + 0.45, 8.2, wood)
    mb.add_box(0.5, FLOOR_T + 0.45, 7.1, 2.4, FLOOR_T + 0.55, 8.1, linen)
    mb.add_box(0.6, FLOOR_T + 0.55, 7.15, 1.2, FLOOR_T + 0.72, 7.55, rgba(0xFFEDE4D4))

    mb.add_box(4.5, FLOOR_T, 7.4, 6.5, FLOOR_T + 0.85, 8.0, wood)
    mb.add_box(4.55, FLOOR_T + 0.85, 7.45, 6.45, FLOOR_T + 0.92, 7.95, metal)

    mb.add_box(7.55, FLOOR_T, 4.2, 8.45, FLOOR_T + 0.55, 5.1, rgba(0xFF3A3F48))
    mb.add_box(7.6, FLOOR_T + 0.55, 4.25, 8.4, FLOOR_T + 0.62, 5.05, gold)


def build_house_shell() -> MeshBuilder:
    mb = MeshBuilder()

    for room in ROOMS:
        mb.add_striped_floor(room)
        tint_n = 6 if room.name in ("kitchen", "bedroom") else -4
        tint_s = -5 if room.name in ("living", "workshop") else 3
        mb.add_wall_z(room.min_z, room.min_x, room.max_x, room.wall, tint=tint_n)
        mb.add_wall_z(room.max_z, room.min_x, room.max_x, room.wall, tint=tint_s)
        mb.add_wall_x(room.min_x, room.min_z, room.max_z, room.wall, tint=-3)
        mb.add_wall_x(room.max_x, room.min_z, room.max_z, room.wall, tint=5)
        _add_room_baseboards(mb, room)

    mb.add_wall_z(3.0, 0, 3, ROOMS[2].wall, door_center=1.5, tint=4)
    mb.add_wall_z(3.0, 3, 7, ROOMS[3].wall, door_center=5.0, tint=-3)
    mb.add_wall_x(3.0, 3, 6, ROOMS[2].wall, door_center=4.5, tint=2)
    mb.add_wall_z(6.0, 0, 3, ROOMS[2].wall, door_center=1.5, tint=-2)
    mb.add_wall_z(6.0, 3, 7, ROOMS[3].wall, door_center=5.0, tint=3)
    mb.add_wall_x(7.0, 3, 6, ROOMS[6].wall, door_center=4.5, tint=-4)

    mb.add_wall_x(0.0, 0, 9, TRIM_CREAM, tint=-8)
    mb.add_wall_x(9.0, 0, 9, TRIM_CREAM, tint=6)
    mb.add_wall_z(0.0, 0, 9, TRIM_CREAM, tint=-5)
    mb.add_wall_z(9.0, 0, 9, TRIM_CREAM, tint=4)

    _add_exterior_windows(mb)
    _add_cable_runs(mb)
    _add_furniture(mb)

    return mb


def build_char_idle() -> MeshBuilder:
    mb = MeshBuilder()
    skin = rgba(0xFFE8C4A8)
    shirt = rgba(0xFF6B7FA3)
    pants = rgba(0xFF4A3828)
    shoe = rgba(0xFF2E241C)

    leg_w, leg_h, leg_d = 0.13, 0.78, 0.17
    leg_gap = 0.07
    mb.add_box(-leg_w - leg_gap / 2, 0, -leg_d / 2, -leg_gap / 2, leg_h, leg_d / 2, pants)
    mb.add_box(leg_gap / 2, 0, -leg_d / 2, leg_w + leg_gap / 2, leg_h, leg_d / 2, pants)
    mb.add_box(-leg_w - leg_gap / 2, 0, -leg_d / 2, -leg_gap / 2, 0.05, leg_d / 2, shoe)
    mb.add_box(leg_gap / 2, 0, -leg_d / 2, leg_w + leg_gap / 2, 0.05, leg_d / 2, shoe)

    body_y0 = leg_h
    body_h = 0.62
    mb.add_box(-0.24, body_y0, -0.15, 0.24, body_y0 + body_h, 0.15, shirt)

    head_size = 0.28
    head_y0 = body_y0 + body_h
    mb.add_box(-head_size / 2, head_y0, -head_size / 2, head_size / 2, head_y0 + head_size, head_size / 2, skin)

    arm_w, arm_h, arm_d = 0.09, 0.50, 0.11
    arm_y = body_y0 + body_h - arm_h - 0.02
    mb.add_box(-0.34, arm_y, -arm_d / 2, -0.24, arm_y + arm_h, arm_d / 2, shirt)
    mb.add_box(0.24, arm_y, -arm_d / 2, 0.34, arm_y + arm_h, arm_d / 2, shirt)
    mb.add_box(-0.36, arm_y, -arm_d / 2, -0.34, arm_y + arm_h, arm_d / 2, skin)
    mb.add_box(0.34, arm_y, -arm_d / 2, 0.36, arm_y + arm_h, arm_d / 2, skin)

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
