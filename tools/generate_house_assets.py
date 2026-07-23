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
DOOR_H = 2.1
STRIPE_W = 0.2
BAND_BASE_TOP = FLOOR_T + 0.9
BAND_SHADOW_TOP = FLOOR_T + 2.1
BASEBOARD_H = 0.12
BASEBOARD_D = 0.02
CHAIR_RAIL_H = 0.08
CHAIR_RAIL_Y = FLOOR_T + 0.9
PICTURE_RAIL_H = 0.06
PICTURE_RAIL_Y = FLOOR_T + 2.1
WINDOW_FRAME_W = 0.08
DOOR_CASING_W = 0.10
EXTERIOR_TRIM = 0xFF3A3630


@dataclass(frozen=True)
class RoomSpec:
    name: str
    min_x: float
    min_z: float
    max_x: float
    max_z: float
    floor: int
    base: int
    shadow: int
    trim: int
    accent: int


# Matches buzz.delena.forgecity.house.HouseRoom bounds (meters, SW origin).
# Grok wall palettes: base / shadow / trim / accent.
ROOMS: tuple[RoomSpec, ...] = (
    RoomSpec("kitchen", 0, 0, 3, 3, 0xFFC9A66B, 0xFFE8DFD0, 0xFFC9B89A, 0xFF3D2B1F, 0xFF8B5E3C),
    RoomSpec("living", 3, 0, 7, 3, 0xFFB8956A, 0xFFD4C9B9, 0xFFA89B85, 0xFF2F2A24, 0xFF5C4033),
    RoomSpec("hallway", 0, 3, 3, 6, 0xFFD4C4A8, 0xFFCFC8BC, 0xFFA99D8C, 0xFF3A3630, 0xFF6B5B4F),
    RoomSpec("office", 3, 3, 7, 6, 0xFFA8885C, 0xFFD8D0C4, 0xFFB3A68F, 0xFF2C2823, 0xFF4A3F35),
    RoomSpec("bedroom", 0, 6, 3, 9, 0xFFC4A882, 0xFFE2D9CC, 0xFFBFAE94, 0xFF3F2E26, 0xFF7A5C4A),
    RoomSpec("workshop", 3, 6, 7, 9, 0xFFB07A4A, 0xFFC8BFAF, 0xFF958A75, 0xFF2A2722, 0xFF5C5245),
    RoomSpec("vault", 7, 3, 9, 6, 0xFF8A7340, 0xFFB8B0A3, 0xFF7D7668, 0xFF1F1C18, 0xFF3D3630),
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


def room_wall_colors(room: RoomSpec) -> tuple[
    tuple[float, float, float, float],
    tuple[float, float, float, float],
    tuple[float, float, float, float],
]:
    return rgba(room.base), rgba(room.shadow), rgba(room.trim)


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

    def add_banded_box(
        self,
        x0: float,
        y0: float,
        z0: float,
        x1: float,
        y1: float,
        z1: float,
        base_c: tuple[float, float, float, float],
        shadow_c: tuple[float, float, float, float],
        trim_c: tuple[float, float, float, float],
        material: int = MAT_VERTEX,
    ) -> None:
        if y1 <= y0:
            return
        bands = (
            (y0, min(y1, BAND_BASE_TOP), base_c),
            (max(y0, BAND_BASE_TOP), min(y1, BAND_SHADOW_TOP), shadow_c),
            (max(y0, BAND_SHADOW_TOP), y1, trim_c),
        )
        for by0, by1, color in bands:
            if by1 - by0 > 1e-6:
                self.add_box(x0, by0, z0, x1, by1, z1, color, material=material)

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
        room: RoomSpec,
        door_center: float | None = None,
    ) -> None:
        base_c, shadow_c, trim_c = room_wall_colors(room)
        y0, y1 = FLOOR_T, WALL_H
        if door_center is None:
            self.add_banded_box(x - WALL_T / 2, y0, z0, x + WALL_T / 2, y1, z1, base_c, shadow_c, trim_c)
            return
        half = DOOR_W / 2
        if door_center - half > z0:
            self.add_banded_box(
                x - WALL_T / 2, y0, z0, x + WALL_T / 2, y1, door_center - half,
                base_c, shadow_c, trim_c,
            )
        if door_center + half < z1:
            self.add_banded_box(
                x - WALL_T / 2, y0, door_center + half, x + WALL_T / 2, y1, z1,
                base_c, shadow_c, trim_c,
            )

    def add_wall_z(
        self,
        z: float,
        x0: float,
        x1: float,
        room: RoomSpec,
        door_center: float | None = None,
    ) -> None:
        base_c, shadow_c, trim_c = room_wall_colors(room)
        y0, y1 = FLOOR_T, WALL_H
        if door_center is None:
            self.add_banded_box(x0, y0, z - WALL_T / 2, x1, y1, z + WALL_T / 2, base_c, shadow_c, trim_c)
            return
        half = DOOR_W / 2
        if door_center - half > x0:
            self.add_banded_box(
                x0, y0, z - WALL_T / 2, door_center - half, y1, z + WALL_T / 2,
                base_c, shadow_c, trim_c,
            )
        if door_center + half < x1:
            self.add_banded_box(
                door_center + half, y0, z - WALL_T / 2, x1, y1, z + WALL_T / 2,
                base_c, shadow_c, trim_c,
            )

    def add_exterior_wall_x(self, x: float, z0: float, z1: float) -> None:
        base_c, shadow_c, trim_c = rgba(0xFFCFC8BC), rgba(0xFFA99D8C), rgba(EXTERIOR_TRIM)
        self.add_banded_box(x - WALL_T / 2, FLOOR_T, z0, x + WALL_T / 2, WALL_H, z1, base_c, shadow_c, trim_c)

    def add_exterior_wall_z(self, z: float, x0: float, x1: float) -> None:
        base_c, shadow_c, trim_c = rgba(0xFFCFC8BC), rgba(0xFFA99D8C), rgba(EXTERIOR_TRIM)
        self.add_banded_box(x0, FLOOR_T, z - WALL_T / 2, x1, WALL_H, z + WALL_T / 2, base_c, shadow_c, trim_c)

    def add_molding_z(
        self,
        z: float,
        x0: float,
        x1: float,
        inward: float,
        trim_c: tuple[float, float, float, float],
        accent_c: tuple[float, float, float, float],
    ) -> None:
        z0, z1 = (z, z + BASEBOARD_D) if inward > 0 else (z - BASEBOARD_D, z)
        self.add_box(x0, FLOOR_T, z0, x1, FLOOR_T + BASEBOARD_H, z1, trim_c)
        self.add_box(x0, CHAIR_RAIL_Y, z0, x1, CHAIR_RAIL_Y + CHAIR_RAIL_H, z1, accent_c)
        self.add_box(x0, PICTURE_RAIL_Y, z0, x1, PICTURE_RAIL_Y + PICTURE_RAIL_H, z1, trim_c)

    def add_molding_x(
        self,
        x: float,
        z0: float,
        z1: float,
        inward: float,
        trim_c: tuple[float, float, float, float],
        accent_c: tuple[float, float, float, float],
    ) -> None:
        x0, x1 = (x, x + BASEBOARD_D) if inward > 0 else (x - BASEBOARD_D, x)
        self.add_box(x0, FLOOR_T, z0, x1, FLOOR_T + BASEBOARD_H, z1, trim_c)
        self.add_box(x0, CHAIR_RAIL_Y, z0, x1, CHAIR_RAIL_Y + CHAIR_RAIL_H, z1, accent_c)
        self.add_box(x0, PICTURE_RAIL_Y, z0, x1, PICTURE_RAIL_Y + PICTURE_RAIL_H, z1, trim_c)

    def add_door_casing_z(
        self,
        z: float,
        door_center: float,
        trim_c: tuple[float, float, float, float],
        inward: float = 1.0,
    ) -> None:
        half = DOOR_W / 2
        cw = DOOR_CASING_W
        y0, y1 = FLOOR_T, FLOOR_T + DOOR_H
        z0, z1 = (z, z + cw) if inward > 0 else (z - cw, z)
        self.add_box(door_center - half - cw, y0, z0, door_center - half, y1, z1, trim_c)
        self.add_box(door_center + half, y0, z0, door_center + half + cw, y1, z1, trim_c)
        self.add_box(door_center - half - cw, y1, z0, door_center + half + cw, y1 + cw, z1, trim_c)

    def add_door_casing_x(
        self,
        x: float,
        door_center: float,
        trim_c: tuple[float, float, float, float],
        inward: float = 1.0,
    ) -> None:
        half = DOOR_W / 2
        cw = DOOR_CASING_W
        y0, y1 = FLOOR_T, FLOOR_T + DOOR_H
        x0, x1 = (x, x + cw) if inward > 0 else (x - cw, x)
        self.add_box(x0, y0, door_center - half - cw, x1, y1, door_center - half, trim_c)
        self.add_box(x0, y0, door_center + half, x1, y1, door_center + half + cw, trim_c)
        self.add_box(x0, y1, door_center - half - cw, x1, y1 + cw, door_center + half + cw, trim_c)

    def add_window_frame_x(
        self,
        x: float,
        z_center: float,
        z_half: float,
        y0: float,
        y1: float,
        outward: float,
        frame_c: tuple[float, float, float, float],
    ) -> None:
        fw = WINDOW_FRAME_W
        wx = x + outward * 0.04
        depth = fw if outward >= 0 else -fw
        z_lo, z_hi = z_center - z_half, z_center + z_half
        self.add_box(wx, y0 - fw, z_lo - fw, wx + depth, y0, z_hi + fw, frame_c)
        self.add_box(wx, y1, z_lo - fw, wx + depth, y1 + fw, z_hi + fw, frame_c)
        self.add_box(wx, y0, z_lo - fw, wx + depth, y1, z_lo, frame_c)
        self.add_box(wx, y0, z_hi, wx + depth, y1, z_hi + fw, frame_c)

    def add_window_frame_z(
        self,
        z: float,
        x_center: float,
        x_half: float,
        y0: float,
        y1: float,
        outward: float,
        frame_c: tuple[float, float, float, float],
    ) -> None:
        fw = WINDOW_FRAME_W
        wz = z + outward * 0.04
        depth = fw if outward >= 0 else -fw
        x_lo, x_hi = x_center - x_half, x_center + x_half
        self.add_box(x_lo - fw, y0 - fw, wz, x_hi + fw, y0, wz + depth, frame_c)
        self.add_box(x_lo - fw, y1, wz, x_hi + fw, y1 + fw, wz + depth, frame_c)
        self.add_box(x_lo - fw, y0, wz, x_lo, y1, wz + depth, frame_c)
        self.add_box(x_hi, y0, wz, x_hi + fw, y1, wz + depth, frame_c)

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


PARTITION_Z = (3.0, 6.0)
PARTITION_X = (3.0, 7.0)


def _spans_office_vault_wall(room: RoomSpec) -> bool:
    return room.min_z < 6.0 and room.max_z > 3.0


def _skip_wall_z(room: RoomSpec, z: float) -> bool:
    if z not in PARTITION_Z:
        return False
    return abs(z - room.min_z) < 1e-6 or abs(z - room.max_z) < 1e-6


def _skip_wall_x(room: RoomSpec, x: float) -> bool:
    if x not in PARTITION_X or not _spans_office_vault_wall(room):
        return False
    return abs(x - room.min_x) < 1e-6 or abs(x - room.max_x) < 1e-6


def _add_room_moldings(mb: MeshBuilder, room: RoomSpec) -> None:
    trim_c, accent_c = rgba(room.trim), rgba(room.accent)
    inset = 0.02
    if room.min_z > 0:
        mb.add_molding_z(room.min_z, room.min_x + inset, room.max_x - inset, inward=1, trim_c=trim_c, accent_c=accent_c)
    if room.max_z < 9:
        mb.add_molding_z(room.max_z, room.min_x + inset, room.max_x - inset, inward=-1, trim_c=trim_c, accent_c=accent_c)
    if room.min_x > 0:
        mb.add_molding_x(room.min_x, room.min_z + inset, room.max_z - inset, inward=1, trim_c=trim_c, accent_c=accent_c)
    if room.max_x < 9:
        mb.add_molding_x(room.max_x, room.min_z + inset, room.max_z - inset, inward=-1, trim_c=trim_c, accent_c=accent_c)


def _add_door_casings(mb: MeshBuilder) -> None:
    hallway = _room_by_name("hallway")
    kitchen = _room_by_name("kitchen")
    living = _room_by_name("living")
    office = _room_by_name("office")
    bedroom = _room_by_name("bedroom")
    workshop = _room_by_name("workshop")
    vault = _room_by_name("vault")

    mb.add_door_casing_z(3.0, 1.5, rgba(hallway.trim), inward=1)
    mb.add_door_casing_z(3.0, 1.5, rgba(kitchen.trim), inward=-1)
    mb.add_door_casing_z(3.0, 5.0, rgba(office.trim), inward=1)
    mb.add_door_casing_z(3.0, 5.0, rgba(living.trim), inward=-1)
    mb.add_door_casing_x(3.0, 4.5, rgba(office.trim), inward=1)
    mb.add_door_casing_x(3.0, 4.5, rgba(hallway.trim), inward=-1)
    mb.add_door_casing_z(6.0, 1.5, rgba(bedroom.trim), inward=1)
    mb.add_door_casing_z(6.0, 1.5, rgba(hallway.trim), inward=-1)
    mb.add_door_casing_z(6.0, 5.0, rgba(workshop.trim), inward=1)
    mb.add_door_casing_z(6.0, 5.0, rgba(office.trim), inward=-1)
    mb.add_door_casing_x(7.0, 4.5, rgba(vault.trim), inward=1)
    mb.add_door_casing_x(7.0, 4.5, rgba(office.trim), inward=-1)


def _add_exterior_windows(mb: MeshBuilder) -> None:
    wy0, wy1 = 0.9, 2.0
    frame = rgba(EXTERIOR_TRIM)
    openings = (
        ("z", 0.0, 1.5, 0.55, wy0, wy1, -1),
        ("x", 0.0, 1.5, 0.55, wy0, wy1, -1),
        ("z", 0.0, 5.0, 0.7, wy0, wy1, -1),
        ("z", 0.0, 3.8, 0.5, wy0, wy1, -1),
        ("x", 0.0, 7.5, 0.55, wy0, wy1, -1),
        ("z", 9.0, 1.5, 0.55, wy0, wy1, 1),
        ("z", 9.0, 5.0, 0.7, wy0, wy1, 1),
        ("x", 9.0, 4.5, 0.45, wy0, wy1, 1),
    )
    for kind, plane, center, half, y0, y1, outward in openings:
        if kind == "z":
            mb.add_window_frame_z(plane, center, half, y0, y1, outward, frame)
            mb.add_window_z(plane, center, half, y0, y1, outward)
        else:
            mb.add_window_frame_x(plane, center, half, y0, y1, outward, frame)
            mb.add_window_x(plane, center, half, y0, y1, outward)


def _droop_span(
    a: tuple[float, float, float],
    b: tuple[float, float, float],
    sag: float,
    samples: int = 10,
) -> list[tuple[float, float, float]]:
    """Parabolic mid-span droop (Production House CableRun gravity feel)."""
    pts: list[tuple[float, float, float]] = []
    for i in range(samples + 1):
        t = i / samples
        x = a[0] + (b[0] - a[0]) * t
        y = a[1] + (b[1] - a[1]) * t - sag * 4.0 * t * (1.0 - t)
        z = a[2] + (b[2] - a[2]) * t
        pts.append((x, y, z))
    return pts


def _add_cable_chain(
    mb: MeshBuilder,
    waypoints: list[tuple[float, float, float]],
    sag: float = 0.10,
    radius: float = 0.016,
) -> None:
    cable = rgba(0xFF1A1816)
    samples: list[tuple[float, float, float]] = []
    for i in range(len(waypoints) - 1):
        span = _droop_span(waypoints[i], waypoints[i + 1], sag=sag, samples=8)
        if i > 0:
            span = span[1:]
        samples.extend(span)
    for x, y, z in samples:
        mb.add_box(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius, cable)


def _add_cable_runs(mb: MeshBuilder) -> None:
    # Hallway → office → workshop droops (replaces axis-aligned plank cables).
    _add_cable_chain(
        mb,
        [(0.25, 0.08, 3.25), (1.6, 0.06, 4.5), (2.85, 0.07, 5.7)],
        sag=0.11,
    )
    _add_cable_chain(
        mb,
        [(2.95, 0.08, 3.4), (4.5, 0.05, 4.5), (6.4, 0.07, 5.6)],
        sag=0.12,
    )
    _add_cable_chain(
        mb,
        [(3.2, 0.08, 6.15), (4.8, 0.04, 7.4), (6.3, 0.07, 8.6)],
        sag=0.10,
    )


def _add_ceilings(mb: MeshBuilder) -> None:
    """
    Open-roof dollhouse finish (0.12): perimeter coves + light trays only.
    Full ceiling slabs sealed the orbit view — rooms must stay readable from above.
    """
    cove = rgba(0xFFE0D8CC)
    tray = rgba(0xFFD4C8B4)
    band = 0.14
    y0 = WALL_H - 0.05
    y1 = WALL_H
    for room in ROOMS:
        x0, x1 = room.min_x + 0.04, room.max_x - 0.04
        z0, z1 = room.min_z + 0.04, room.max_z - 0.04
        # Four perimeter cove strips (open center)
        mb.add_box(x0, y0, z0, x1, y1, z0 + band, cove)
        mb.add_box(x0, y0, z1 - band, x1, y1, z1, cove)
        mb.add_box(x0, y0, z0 + band, x0 + band, y1, z1 - band, cove)
        mb.add_box(x1 - band, y0, z0 + band, x1, y1, z1 - band, cove)
        # Light tray along longer axis (does not seal)
        cx = (room.min_x + room.max_x) * 0.5
        cz = (room.min_z + room.max_z) * 0.5
        if room.max_x - room.min_x >= room.max_z - room.min_z:
            half = (room.max_x - room.min_x) * 0.22
            mb.add_box(cx - half, y0 - 0.04, cz - 0.06, cx + half, y0, cz + 0.06, tray)
        else:
            half = (room.max_z - room.min_z) * 0.22
            mb.add_box(cx - 0.06, y0 - 0.04, cz - half, cx + 0.06, y0, cz + half, tray)


def _add_picture_frame(
    mb: MeshBuilder,
    x0: float,
    y0: float,
    z0: float,
    x1: float,
    y1: float,
    z1: float,
    art: tuple[float, float, float, float],
) -> None:
    frame = rgba(0xFF3A3228)
    # Outer frame
    mb.add_box(x0, y0, z0, x1, y1, z1, frame)
    # Inset art
    inset = 0.03
    ax0, ay0, az0 = x0 + inset, y0 + inset, z0
    ax1, ay1, az1 = x1 - inset, y1 - inset, z1
    if abs(x1 - x0) < abs(z1 - z0):
        # Frame on X wall — push art along X
        mid = (x0 + x1) * 0.5
        mb.add_box(mid - 0.005, ay0, z0 + inset, mid + 0.005, ay1, z1 - inset, art)
    else:
        mid = (z0 + z1) * 0.5
        mb.add_box(x0 + inset, ay0, mid - 0.005, x1 - inset, ay1, mid + 0.005, art)


def _add_pictures(mb: MeshBuilder) -> None:
    # Living south wall
    _add_picture_frame(mb, 4.6, 1.15, 0.08, 5.6, 1.85, 0.12, rgba(0xFF6B8F71))
    # Office west wall
    _add_picture_frame(mb, 3.08, 1.2, 4.1, 3.12, 1.9, 5.0, rgba(0xFF4A6FA5))
    # Bedroom east wall
    _add_picture_frame(mb, 2.88, 1.15, 6.8, 2.92, 1.8, 7.6, rgba(0xFFB07060))
    # Hallway
    _add_picture_frame(mb, 0.08, 1.25, 4.2, 0.12, 1.85, 4.9, rgba(0xFF8A7A5A))


def _add_corner_ao(mb: MeshBuilder) -> None:
    """Fake floor-corner AO wedges (dark, short)."""
    ao = rgba(0xFF2A241E)
    h = 0.35
    t = 0.07
    for room in ROOMS:
        corners = (
            (room.min_x, room.min_z),
            (room.max_x, room.min_z),
            (room.min_x, room.max_z),
            (room.max_x, room.max_z),
        )
        for cx, cz in corners:
            mb.add_box(cx - t * 0.5, FLOOR_T, cz - t * 0.5, cx + t * 0.5, FLOOR_T + h, cz + t * 0.5, ao)


def _add_hero_props(mb: MeshBuilder) -> None:
    """One readable prop per room (0.11.1 set dressing)."""
    metal = rgba(0xFF6A6E74)
    copper = rgba(0xFFB87333)
    pot = rgba(0xFF4A3A2A)
    leaf = rgba(0xFF3D6B3A)
    screen = rgba(0xFF1A2430)
    plastic = rgba(0xFF2C333C)
    slate = rgba(0xFF1E1E1E)
    chalk = rgba(0xFFE8E4D8)

    # Kitchen — kettle on counter
    mb.add_box(1.35, 0.95, 2.35, 1.55, 1.12, 2.55, copper)
    mb.add_box(1.38, 1.12, 2.38, 1.52, 1.18, 2.52, metal)
    mb.add_box(1.48, 1.05, 2.52, 1.62, 1.08, 2.58, copper)

    # Living — potted plant
    mb.add_box(6.15, FLOOR_T, 0.45, 6.45, FLOOR_T + 0.22, 0.75, pot)
    mb.add_box(6.22, FLOOR_T + 0.22, 0.52, 6.38, FLOOR_T + 0.55, 0.68, leaf)
    mb.add_box(6.18, FLOOR_T + 0.50, 0.48, 6.42, FLOOR_T + 0.72, 0.72, leaf)

    # Office — laptop on desk
    mb.add_box(4.7, 0.76, 4.25, 5.3, 0.80, 4.65, plastic)
    mb.add_box(4.75, 0.80, 4.30, 5.25, 1.05, 4.35, screen)

    # Bedroom — switch plate + small dresser box already; add kettle-like lamp base done via pillow
    mb.add_box(2.35, 0.55, 7.2, 2.55, 0.72, 7.4, rgba(0xFFEDE4D4))
    mb.add_box(2.40, 0.72, 7.25, 2.50, 0.78, 7.35, rgba(0xFFFFC978), material=MAT_WINDOW)

    # Workshop — toolbox
    mb.add_box(3.6, FLOOR_T, 7.15, 4.35, FLOOR_T + 0.28, 7.55, rgba(0xFFC45C26))
    mb.add_box(3.7, FLOOR_T + 0.28, 7.25, 4.25, FLOOR_T + 0.35, 7.45, metal)

    # Hallway — clap slate / clapper (PH nod)
    mb.add_box(1.15, FLOOR_T, 4.55, 1.55, FLOOR_T + 0.04, 4.85, slate)
    mb.add_box(1.18, FLOOR_T + 0.04, 4.58, 1.52, FLOOR_T + 0.06, 4.82, chalk)

    # Vault — lock bar across chest
    mb.add_box(7.7, 0.58, 4.55, 8.3, 0.66, 4.75, metal)


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
    hallway = _room_by_name("hallway")
    office = _room_by_name("office")
    vault = _room_by_name("vault")

    for room in ROOMS:
        mb.add_striped_floor(room)
        if not _skip_wall_z(room, room.min_z):
            mb.add_wall_z(room.min_z, room.min_x, room.max_x, room)
        if not _skip_wall_z(room, room.max_z):
            mb.add_wall_z(room.max_z, room.min_x, room.max_x, room)
        if not _skip_wall_x(room, room.min_x):
            mb.add_wall_x(room.min_x, room.min_z, room.max_z, room)
        if not _skip_wall_x(room, room.max_x):
            mb.add_wall_x(room.max_x, room.min_z, room.max_z, room)
        _add_room_moldings(mb, room)

    mb.add_wall_z(3.0, 0, 3, hallway, door_center=1.5)
    mb.add_wall_z(3.0, 3, 7, office, door_center=5.0)
    mb.add_wall_x(3.0, 3, 6, hallway, door_center=4.5)
    mb.add_wall_z(6.0, 0, 3, hallway, door_center=1.5)
    mb.add_wall_z(6.0, 3, 7, office, door_center=5.0)
    mb.add_wall_x(7.0, 3, 6, vault, door_center=4.5)

    mb.add_exterior_wall_x(0.0, 0, 9)
    mb.add_exterior_wall_x(9.0, 0, 9)
    mb.add_exterior_wall_z(0.0, 0, 9)
    mb.add_exterior_wall_z(9.0, 0, 9)

    _add_door_casings(mb)
    _add_exterior_windows(mb)
    _add_cable_runs(mb)
    _add_furniture(mb)
    _add_hero_props(mb)
    _add_pictures(mb)
    _add_corner_ao(mb)
    _add_ceilings(mb)

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
    from generate_characters import generate_all

    house_path = OUT_DIR / "house_shell.glb"
    house_size = build_house_shell().to_glb(house_path, "house_shell")
    print(f"Wrote {house_path} ({house_size:,} bytes)")

    for path, size in generate_all(OUT_DIR):
        print(f"Wrote {path} ({size:,} bytes)")


if __name__ == "__main__":
    main()
