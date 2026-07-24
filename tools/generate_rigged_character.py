#!/usr/bin/env python3
"""Generate a rigged, animated glTF Assistant character.

Replaces Kotlin-side per-frame pose puppeteering (HouseHumanoidPose.compute,
called every frame in AssistantCharacterScreen) with a real glTF node
hierarchy + baked Idle/Talk/Wave Animation clips, played back via SceneView's
ModelNode.playAnimation(...). Body parts are rigid (no continuous deforming
surface), so this uses plain animated Nodes, not vertex skinning.

Geometry, joint offsets, and motion formulas are ported 1:1 from:
  - app/src/main/java/buzz/delena/forgecity/ui/house/HouseHumanoidNode.kt
  - app/src/main/java/buzz/delena/forgecity/house/character/HouseHumanoidPose.kt
so the rig looks/moves like the existing procedural character (which stays
in place as the runtime fallback if this asset fails to load).
"""

from __future__ import annotations

import math
from pathlib import Path

from pygltflib import (
    GLTF2,
    Accessor,
    Animation,
    AnimationChannel,
    AnimationChannelTarget,
    AnimationSampler,
    Asset,
    Buffer,
    BufferView,
    Image,
    Material,
    Mesh,
    Node,
    PbrMetallicRoughness,
    Primitive,
    Sampler,
    Scene,
    Texture,
    TextureInfo,
)

from generate_house_assets import OUT_DIR, rgba

FACE_PNG = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "faces" / "siva.png"

# HouseHumanoidPose.HIP_Y
HIP_Y = 0.92

# HouseHumanoidPose.lookFor(CharacterRole.ASSISTANT) colors.
SKIN = rgba(0xFFF0D5C0)
HAIR = rgba(0xFF1A2430)
CLOTH_TOP = rgba(0xFF6B9BD1)
CLOTH_BOTTOM = rgba(0xFF2C3A4A)

MAT_SKIN, MAT_HAIR, MAT_CLOTH_TOP, MAT_CLOTH_BOTTOM, MAT_FACE = range(5)


# ---------------------------------------------------------------- geometry --

def build_ellipsoid(
    rx: float, ry: float, rz: float, rings: int = 6, segments: int = 8,
) -> tuple[list[tuple[float, float, float]], list[int]]:
    """UV-sphere scaled per axis (stands in for sphere/capsule), centered at
    the local origin. rings/segments low by design — Adreno-safe low-poly,
    matches this repo's existing low-poly budget."""
    positions: list[tuple[float, float, float]] = []
    for ring in range(rings + 1):
        theta = math.pi * ring / rings
        y = math.cos(theta)
        r = math.sin(theta)
        for seg in range(segments):
            phi = 2.0 * math.pi * seg / segments
            x = r * math.cos(phi)
            z = r * math.sin(phi)
            positions.append((x * rx, y * ry, z * rz))
    indices: list[int] = []
    for ring in range(rings):
        for seg in range(segments):
            a = ring * segments + seg
            b = ring * segments + (seg + 1) % segments
            c = (ring + 1) * segments + seg
            d = (ring + 1) * segments + (seg + 1) % segments
            indices.extend((a, c, b))
            indices.extend((b, c, d))
    return positions, indices


def build_face_quad(size: float = 0.20) -> tuple[list[tuple[float, float, float]], list[tuple[float, float]], list[int]]:
    """Flat textured quad facing -Z, matching HouseHumanoidNode's face card."""
    h = size / 2.0
    positions = [(-h, -h, 0.0), (h, -h, 0.0), (h, h, 0.0), (-h, h, 0.0)]
    uvs = [(0.0, 1.0), (1.0, 1.0), (1.0, 0.0), (0.0, 0.0)]
    indices = [0, 1, 2, 0, 2, 3]
    return positions, uvs, indices


def offset(positions: list[tuple[float, float, float]], dx: float, dy: float, dz: float) -> list[tuple[float, float, float]]:
    return [(x + dx, y + dy, z + dz) for x, y, z in positions]


# ---------------------------------------------------------------- pose math --
# Ported from HouseHumanoidPose.compute() — see that file for the Kotlin
# source of truth. Values are radians (no radToDeg — glTF wants quaternions).

def compute_pose(action: str, t: float) -> dict[str, float]:
    body_y = math.sin(t * 1.2) * 0.006
    body_yaw = math.sin(t * 1.4) * 0.08 if action == "TALK" else math.sin(t * 0.5) * 0.03
    head_pitch = 0.06 + math.sin(t * 2.0) * 0.04 if action == "TALK" else 0.04
    head_yaw = math.sin(t * 0.7) * (0.08 if action == "IDLE" else 0.12)

    if action == "TALK":
        arm_rx = -0.4 + math.sin(t * 2.5) * 0.25
        arm_rz = 0.15
    elif action == "WAVE":
        arm_rx = -2.1 + math.sin(t * 9.0) * 0.35
        arm_rz = 0.5
    else:
        arm_rx = math.sin(t * 1.1) * 0.08
        arm_rz = 0.12
    arm_lx = math.sin(t * 1.1 + 1.0) * 0.08
    arm_lz = -0.12

    leg = 0.02
    return {
        "body_y": body_y, "body_yaw": body_yaw,
        "head_pitch": head_pitch, "head_yaw": head_yaw,
        "arm_rx": arm_rx, "arm_rz": arm_rz, "arm_lx": arm_lx, "arm_lz": arm_lz,
        "leg_r": leg, "leg_l": leg,
    }


def quat_axis(axis: str, angle: float) -> tuple[float, float, float, float]:
    if abs(angle) < 1e-9:
        return (0.0, 0.0, 0.0, 1.0)
    half = angle / 2.0
    s = math.sin(half)
    x = s if axis == "x" else 0.0
    y = s if axis == "y" else 0.0
    z = s if axis == "z" else 0.0
    return (x, y, z, math.cos(half))


def quat_mul(a: tuple[float, float, float, float], b: tuple[float, float, float, float]) -> tuple[float, float, float, float]:
    ax, ay, az, aw = a
    bx, by, bz, bw = b
    return (
        aw * bx + ax * bw + ay * bz - az * by,
        aw * by - ax * bz + ay * bw + az * bx,
        aw * bz + ax * by - ay * bx + az * bw,
        aw * bw - ax * bx - ay * by - az * bz,
    )


CLIPS = {
    # name: (action, duration_seconds, sample_count, loop)
    "Idle": ("IDLE", 6.0, 24, True),
    "Talk": ("TALK", 3.0, 24, True),
    "Wave": ("WAVE", 1.5, 16, False),
}


# --------------------------------------------------------------- blob/gltf --

class BlobBuilder:
    def __init__(self) -> None:
        self.chunks = bytearray()
        self.buffer_views: list[BufferView] = []

    def add(self, data: bytes, target: int | None = None) -> int:
        pad = (4 - len(data) % 4) % 4
        offset_ = len(self.chunks)
        self.chunks.extend(data)
        self.chunks.extend(b"\x00" * pad)
        self.buffer_views.append(BufferView(buffer=0, byteOffset=offset_, byteLength=len(data), target=target))
        return len(self.buffer_views) - 1


def _f32(values: list[float]) -> bytes:
    import struct
    return struct.pack(f"<{len(values)}f", *values)


def _vec3_bytes(vecs: list[tuple[float, float, float]]) -> bytes:
    flat = [c for v in vecs for c in v]
    return _f32(flat)


def _vec4_bytes(vecs: list[tuple[float, float, float, float]]) -> bytes:
    flat = [c for v in vecs for c in v]
    return _f32(flat)


def _vec2_bytes(vecs: list[tuple[float, float]]) -> bytes:
    flat = [c for v in vecs for c in v]
    return _f32(flat)


def _u16_bytes(values: list[int]) -> bytes:
    import struct
    return struct.pack(f"<{len(values)}H", *values)


def _minmax3(vecs: list[tuple[float, float, float]]) -> tuple[list[float], list[float]]:
    xs, ys, zs = zip(*vecs)
    return [min(xs), min(ys), min(zs)], [max(xs), max(ys), max(zs)]


class GltfBuilder:
    """Minimal multi-mesh / multi-node / animation glTF assembler.

    Deliberately separate from generate_house_assets.MeshBuilder (which only
    ever emits a single static node+mesh) — this is additive new tooling, not
    a modification of the proven house/city asset path.
    """

    def __init__(self) -> None:
        self.blob = BlobBuilder()
        self.accessors: list[Accessor] = []
        self.materials: list[Material] = []
        self.meshes: list[Mesh] = []
        self.nodes: list[Node] = []
        self.images: list[Image] = []
        self.textures: list[Texture] = []
        self.samplers: list[Sampler] = []
        self.animations: list[Animation] = []
        self.root_node = 0

    def add_accessor(self, **kwargs) -> int:
        self.accessors.append(Accessor(**kwargs))
        return len(self.accessors) - 1

    def add_material(self, mat: Material) -> int:
        self.materials.append(mat)
        return len(self.materials) - 1

    def add_position_color_primitive(
        self,
        positions: list[tuple[float, float, float]],
        indices: list[int],
        material: int,
        color: tuple[float, float, float, float],
    ) -> Primitive:
        pos_bv = self.blob.add(_vec3_bytes(positions), target=34962)
        mn, mx = _minmax3(positions)
        pos_acc = self.add_accessor(bufferView=pos_bv, componentType=5126, count=len(positions), type="VEC3", min=mn, max=mx)
        colors = [color] * len(positions)
        col_bv = self.blob.add(_vec4_bytes(colors), target=34962)
        col_acc = self.add_accessor(bufferView=col_bv, componentType=5126, count=len(colors), type="VEC4")
        idx_bv = self.blob.add(_u16_bytes(indices), target=34963)
        idx_acc = self.add_accessor(bufferView=idx_bv, componentType=5123, count=len(indices), type="SCALAR")
        return Primitive(attributes={"POSITION": pos_acc, "COLOR_0": col_acc}, indices=idx_acc, material=material)

    def add_face_primitive(
        self,
        positions: list[tuple[float, float, float]],
        uvs: list[tuple[float, float]],
        indices: list[int],
        material: int,
    ) -> Primitive:
        pos_bv = self.blob.add(_vec3_bytes(positions), target=34962)
        mn, mx = _minmax3(positions)
        pos_acc = self.add_accessor(bufferView=pos_bv, componentType=5126, count=len(positions), type="VEC3", min=mn, max=mx)
        uv_bv = self.blob.add(_vec2_bytes(uvs), target=34962)
        uv_acc = self.add_accessor(bufferView=uv_bv, componentType=5126, count=len(uvs), type="VEC2")
        idx_bv = self.blob.add(_u16_bytes(indices), target=34963)
        idx_acc = self.add_accessor(bufferView=idx_bv, componentType=5123, count=len(indices), type="SCALAR")
        return Primitive(attributes={"POSITION": pos_acc, "TEXCOORD_0": uv_acc}, indices=idx_acc, material=material)

    def add_mesh(self, primitives: list[Primitive]) -> int:
        self.meshes.append(Mesh(primitives=primitives))
        return len(self.meshes) - 1

    def add_node(self, name: str, translation: tuple[float, float, float], mesh: int | None = None, children: list[int] | None = None) -> int:
        self.nodes.append(Node(name=name, translation=list(translation), mesh=mesh, children=children or []))
        return len(self.nodes) - 1

    def add_image_texture(self, png_bytes: bytes) -> int:
        img_bv = self.blob.add(png_bytes, target=None)
        self.images.append(Image(bufferView=img_bv, mimeType="image/png"))
        self.samplers.append(Sampler(magFilter=9729, minFilter=9729, wrapS=33071, wrapT=33071))
        self.textures.append(Texture(source=len(self.images) - 1, sampler=len(self.samplers) - 1))
        return len(self.textures) - 1

    def add_translation_track(self, times: list[float], values: list[tuple[float, float, float]]) -> AnimationSampler:
        in_bv = self.blob.add(_f32(times), target=None)
        in_acc = self.add_accessor(bufferView=in_bv, componentType=5126, count=len(times), type="SCALAR", min=[min(times)], max=[max(times)])
        out_bv = self.blob.add(_vec3_bytes(values), target=None)
        out_acc = self.add_accessor(bufferView=out_bv, componentType=5126, count=len(values), type="VEC3")
        return AnimationSampler(input=in_acc, output=out_acc, interpolation="LINEAR")

    def add_rotation_track(self, times: list[float], values: list[tuple[float, float, float, float]]) -> AnimationSampler:
        in_bv = self.blob.add(_f32(times), target=None)
        in_acc = self.add_accessor(bufferView=in_bv, componentType=5126, count=len(times), type="SCALAR", min=[min(times)], max=[max(times)])
        out_bv = self.blob.add(_vec4_bytes(values), target=None)
        out_acc = self.add_accessor(bufferView=out_bv, componentType=5126, count=len(values), type="VEC4")
        return AnimationSampler(input=in_acc, output=out_acc, interpolation="LINEAR")

    def save(self, path: Path) -> int:
        gltf = GLTF2(
            asset=Asset(version="2.0", generator="forgecity generate_rigged_character.py"),
            scene=0,
            scenes=[Scene(nodes=[self.root_node])],
            nodes=self.nodes,
            meshes=self.meshes,
            materials=self.materials,
            images=self.images,
            textures=self.textures,
            samplers=self.samplers,
            animations=self.animations,
            buffers=[Buffer(byteLength=len(self.blob.chunks))],
            bufferViews=self.blob.buffer_views,
            accessors=self.accessors,
        )
        path.parent.mkdir(parents=True, exist_ok=True)
        gltf.set_binary_blob(bytes(self.blob.chunks))
        gltf.save(str(path))
        return path.stat().st_size


def build() -> GltfBuilder:
    gb = GltfBuilder()

    mat_skin = gb.add_material(Material(
        name="Skin", doubleSided=True,
        pbrMetallicRoughness=PbrMetallicRoughness(baseColorFactor=list(SKIN), metallicFactor=0.02, roughnessFactor=0.55),
    ))
    mat_hair = gb.add_material(Material(
        name="Hair", doubleSided=True,
        pbrMetallicRoughness=PbrMetallicRoughness(baseColorFactor=list(HAIR), metallicFactor=0.0, roughnessFactor=0.88),
    ))
    mat_top = gb.add_material(Material(
        name="ClothTop", doubleSided=True,
        pbrMetallicRoughness=PbrMetallicRoughness(baseColorFactor=list(CLOTH_TOP), metallicFactor=0.05, roughnessFactor=0.68),
    ))
    mat_bottom = gb.add_material(Material(
        name="ClothBottom", doubleSided=True,
        pbrMetallicRoughness=PbrMetallicRoughness(baseColorFactor=list(CLOTH_BOTTOM), metallicFactor=0.02, roughnessFactor=0.78),
    ))
    face_texture = gb.add_image_texture(FACE_PNG.read_bytes())
    mat_face = gb.add_material(Material(
        name="Face", doubleSided=True, alphaMode="BLEND",
        pbrMetallicRoughness=PbrMetallicRoughness(
            baseColorTexture=TextureInfo(index=face_texture),
            metallicFactor=0.0, roughnessFactor=0.55,
        ),
    ))

    # --- torso (static relative to body/hips) ---
    torso_pos, torso_idx = build_ellipsoid(0.18, 0.21, 0.18, rings=6, segments=8)
    torso_mesh = gb.add_mesh([gb.add_position_color_primitive(torso_pos, torso_idx, mat_top, CLOTH_TOP)])

    # --- head: skin + hair spheres + textured face card ---
    skin_pos, skin_idx = build_ellipsoid(0.13, 0.13, 0.13, rings=6, segments=8)
    hair_pos, hair_idx = build_ellipsoid(0.135, 0.135, 0.135, rings=6, segments=8)
    hair_pos = offset(hair_pos, 0.0, 0.10, 0.0)
    face_pos, face_uv, face_idx = build_face_quad(0.20)
    face_pos = offset(face_pos, 0.0, 0.01, -0.125)
    head_mesh = gb.add_mesh([
        gb.add_position_color_primitive(skin_pos, skin_idx, mat_skin, SKIN),
        gb.add_position_color_primitive(hair_pos, hair_idx, mat_hair, HAIR),
        gb.add_face_primitive(face_pos, face_uv, face_idx, mat_face),
    ])

    # --- arms: upper-arm capsule-ish + hand sphere ---
    def build_arm_mesh() -> int:
        upper_pos, upper_idx = build_ellipsoid(0.05, 0.17, 0.05, rings=5, segments=6)
        upper_pos = offset(upper_pos, 0.0, -0.22, 0.0)
        hand_pos, hand_idx = build_ellipsoid(0.045, 0.045, 0.045, rings=5, segments=6)
        hand_pos = offset(hand_pos, 0.0, -0.48, 0.0)
        return gb.add_mesh([
            gb.add_position_color_primitive(upper_pos, upper_idx, mat_top, CLOTH_TOP),
            gb.add_position_color_primitive(hand_pos, hand_idx, mat_skin, SKIN),
        ])

    arm_r_mesh = build_arm_mesh()
    arm_l_mesh = build_arm_mesh()

    # --- legs ---
    def build_leg_mesh() -> int:
        leg_pos, leg_idx = build_ellipsoid(0.07, 0.275, 0.07, rings=5, segments=6)
        leg_pos = offset(leg_pos, 0.0, -0.4, 0.0)
        return gb.add_mesh([gb.add_position_color_primitive(leg_pos, leg_idx, mat_bottom, CLOTH_BOTTOM)])

    leg_r_mesh = build_leg_mesh()
    leg_l_mesh = build_leg_mesh()

    # --- node hierarchy (root == "body"/hips pivot; Kotlin's ModelNode
    # worldPosition places the whole rig, matching the outer wrapper Node in
    # HouseHumanoidNode.kt) ---
    torso_node = gb.add_node("torso", (0.0, HIP_Y + 0.35, 0.0), mesh=torso_mesh)
    head_node = gb.add_node("head", (0.0, HIP_Y + 0.72, 0.0), mesh=head_mesh)
    arm_r_node = gb.add_node("armR", (0.28, HIP_Y + 0.5, 0.0), mesh=arm_r_mesh)
    arm_l_node = gb.add_node("armL", (-0.28, HIP_Y + 0.5, 0.0), mesh=arm_l_mesh)
    leg_r_node = gb.add_node("legR", (0.1, HIP_Y, 0.0), mesh=leg_r_mesh)
    leg_l_node = gb.add_node("legL", (-0.1, HIP_Y, 0.0), mesh=leg_l_mesh)
    body_node = gb.add_node(
        "body", (0.0, 0.0, 0.0),
        children=[torso_node, head_node, arm_r_node, arm_l_node, leg_r_node, leg_l_node],
    )
    gb.root_node = body_node

    # --- animations ---
    animations: list[Animation] = []
    for clip_name, (action, duration, samples, _loop) in CLIPS.items():
        times = [duration * i / (samples - 1) for i in range(samples)]
        body_t: list[tuple[float, float, float]] = []
        body_r: list[tuple[float, float, float, float]] = []
        head_r: list[tuple[float, float, float, float]] = []
        arm_r_r: list[tuple[float, float, float, float]] = []
        arm_l_r: list[tuple[float, float, float, float]] = []
        leg_r_r: list[tuple[float, float, float, float]] = []
        leg_l_r: list[tuple[float, float, float, float]] = []
        for t in times:
            p = compute_pose(action, t)
            body_t.append((0.0, p["body_y"], 0.0))
            body_r.append(quat_axis("y", p["body_yaw"]))
            head_r.append(quat_mul(quat_axis("y", p["head_yaw"]), quat_axis("x", p["head_pitch"])))
            arm_r_r.append(quat_mul(quat_axis("z", p["arm_rz"]), quat_axis("x", p["arm_rx"])))
            arm_l_r.append(quat_mul(quat_axis("z", p["arm_lz"]), quat_axis("x", p["arm_lx"])))
            leg_r_r.append(quat_axis("x", p["leg_r"]))
            leg_l_r.append(quat_axis("x", p["leg_l"]))

        samplers: list[AnimationSampler] = []
        channels: list[AnimationChannel] = []

        def add_channel(node: int, path: str, sampler: AnimationSampler) -> None:
            samplers.append(sampler)
            channels.append(AnimationChannel(sampler=len(samplers) - 1, target=AnimationChannelTarget(node=node, path=path)))

        add_channel(body_node, "translation", gb.add_translation_track(times, body_t))
        add_channel(body_node, "rotation", gb.add_rotation_track(times, body_r))
        add_channel(head_node, "rotation", gb.add_rotation_track(times, head_r))
        add_channel(arm_r_node, "rotation", gb.add_rotation_track(times, arm_r_r))
        add_channel(arm_l_node, "rotation", gb.add_rotation_track(times, arm_l_r))
        add_channel(leg_r_node, "rotation", gb.add_rotation_track(times, leg_r_r))
        add_channel(leg_l_node, "rotation", gb.add_rotation_track(times, leg_l_r))

        animations.append(Animation(name=clip_name, samplers=samplers, channels=channels))

    gb.animations = animations
    return gb


def _sanity_check(path: Path) -> None:
    from pygltflib import GLTF2 as _GLTF2

    doc = _GLTF2().load(str(path))
    joint_count = len(doc.nodes)
    anim_count = len(doc.animations)
    anim_names = sorted(a.name for a in doc.animations)
    assert joint_count <= 18, f"joint budget exceeded: {joint_count} > 18"
    assert anim_count == 3, f"expected 3 animation clips, got {anim_count}"
    assert anim_names == ["Idle", "Talk", "Wave"], anim_names
    assert doc.images and doc.images[0].mimeType == "image/png", "face texture missing"
    print(f"  sanity OK: {joint_count} nodes, {anim_count} clips ({', '.join(anim_names)}), face texture embedded")


def main() -> None:
    out_path = OUT_DIR / "char_assistant_rigged.glb"
    gb = build()
    size = gb.save(out_path)
    print(f"Wrote {out_path} ({size:,} bytes)")
    _sanity_check(out_path)


if __name__ == "__main__":
    main()
