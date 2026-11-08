"""Fast offline pose preview of the Architect, straight from the rig and pose data (no Gradle).

It applies the same transform as ModelPart (translate by the pivot, then rotation Z*Y*X, then scale) to the boxes of
tools/generate_architect_assets.py and draws them textured with the generated atlas (nearest texel of each face
centre region), from the front, the left side, the back and three-quarters. It is a review aid: the authoritative
render is the Gradle export of the real Java model (tools/architect-sketches.gradle).

Run:  python tools/preview_architect_poses.py [CLIP:tick ...]     (default: statue, idle, guard, walk)
Out:  build/architect-sketches/preview_<clip>_<tick>.png
"""
import math
import sys
from pathlib import Path
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
import generate_architect_assets as gen  # noqa: E402
import architect_poses as poses  # noqa: E402
import architect_clips as clips  # noqa: E402

OUT = Path(__file__).resolve().parent.parent / "build/architect-sketches"
VIEWS = (("front", 0.0), ("left", -math.pi / 2), ("back", math.pi), ("3/4", -0.6))


def mat_mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(4)) for j in range(4)] for i in range(4)]


def rot_x(t):
    c, s = math.cos(t), math.sin(t)
    return [[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]]


def rot_y(t):
    c, s = math.cos(t), math.sin(t)
    return [[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]]


def rot_z(t):
    c, s = math.cos(t), math.sin(t)
    return [[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]


def translate(x, y, z):
    return [[1, 0, 0, x], [0, 1, 0, y], [0, 0, 1, z], [0, 0, 0, 1]]


def scale(s):
    return [[s[0], 0, 0, 0], [0, s[1], 0, 0], [0, 0, s[2], 0], [0, 0, 0, 1]]


def apply(m, p):
    return tuple(m[i][0] * p[0] + m[i][1] * p[1] + m[i][2] * p[2] + m[i][3] for i in range(3))


def sample(clip, tick):
    """Absolute pose at a tick: linear between each track's keys (good enough for a preview)."""
    ticks, loops, smooth, tracks = clips.CLIPS[clip]
    out = {}
    for key, points in tracks.items():
        value = points[-1][1]
        for (t0, v0), (t1, v1) in zip(points, points[1:]):
            if t0 <= tick <= t1:
                a = 0 if t1 == t0 else (tick - t0) / (t1 - t0)
                value = tuple(v0[i] + (v1[i] - v0[i]) * a for i in range(3))
                break
        out[key] = value
    return out


def world_matrices(pose):
    mats = {}
    for name, (parent, x, y, z) in gen.PARTS.items():
        if parent is None:
            mats[name] = translate(0, 0, 0)
            continue
        _, px, py, pz = gen.PARTS[parent]
        ox, oy, oz = x - px, py - y, z - pz
        dx, dy, dz = pose.get("@" + name, (0, 0, 0))
        r = pose.get(name, poses.BASE.get(name, (0, 0, 0)))
        m = mat_mul(mats[parent], translate(ox + dx, oy + dy, oz + dz))
        m = mat_mul(m, mat_mul(rot_z(math.radians(r[2])), mat_mul(rot_y(math.radians(r[1])), rot_x(math.radians(r[0])))))
        if "%" + name in pose:
            m = mat_mul(m, scale(pose["%" + name]))
        mats[name] = m
    return mats


def box_faces(b, m):
    name, owner, x0, y0, z0, x1, y1, z1, mat, k = b
    _, px, py, pz = gen.PARTS[owner]
    lo = (x0 - px, py - y1, z0 - pz)
    hi = (x1 - px, py - y0, z1 - pz)
    corners = {}
    for ix, x in enumerate((lo[0], hi[0])):
        for iy, y in enumerate((lo[1], hi[1])):
            for iz, z in enumerate((lo[2], hi[2])):
                corners[(ix, iy, iz)] = apply(m, (x, y, z))
    quads = {"front": [(0, 0, 0), (1, 0, 0), (1, 1, 0), (0, 1, 0)], "back": [(1, 0, 1), (0, 0, 1), (0, 1, 1), (1, 1, 1)],
             "left": [(1, 0, 0), (1, 0, 1), (1, 1, 1), (1, 1, 0)], "right": [(0, 0, 1), (0, 0, 0), (0, 1, 0), (0, 1, 1)],
             "top": [(0, 0, 1), (1, 0, 1), (1, 0, 0), (0, 0, 0)], "bottom": [(0, 1, 0), (1, 1, 0), (1, 1, 1), (0, 1, 1)]}
    return {f: [corners[c] for c in q] for f, q in quads.items()}


def render(pose, atlas, layout, size=340, hidden=()):
    mats = world_matrices(pose)
    sheet = Image.new("RGBA", (size * len(VIEWS), size + 16), (236, 231, 220, 255))
    draw = ImageDraw.Draw(sheet)
    for vi, (label, yaw) in enumerate(VIEWS):
        polys = []
        for b in gen.BOXES:
            if not visible(b[1], pose) or any(b[1].startswith(h) for h in hidden):
                continue
            u, v = layout[b[0]]
            rects = gen.faces_of(u, v, *gen.dims(b))
            for face, pts in box_faces(b, mats[b[1]]).items():
                rx, ry, rw, rh = rects[face]
                color = atlas.getpixel((min(255, rx + rw // 2), min(255, ry + rh // 2)))
                if color[3] == 0:
                    continue
                proj = []
                for (x, y, z) in pts:
                    # model space: +x the character's left, +y down, -z front; we look at the front from -z
                    c, s = math.cos(yaw), math.sin(yaw)
                    vx, vz = x * c - z * s, x * s + z * c
                    proj.append((vx, y, vz))
                depth = sum(p[2] for p in proj) / 4
                n = normal(proj)
                shade = 0.75 + 0.25 * max(0.0, -n[2] * 0.7 - n[1] * 0.5)
                polys.append((depth, proj, tuple(int(ch * shade) for ch in color[:3])))
        polys.sort(key=lambda p: -p[0])
        ox, oy, k = vi * size + size / 2, 12 + size * 0.92, size / 100
        for _, proj, col in polys:
            draw.polygon([(ox + p[0] * k, oy + (p[1] - 24) * k) for p in proj], fill=col, outline=(20, 20, 26))
        draw.text((vi * size + 6, 2), label, fill=(60, 50, 40))
        draw.line((vi * size, oy, vi * size + size, oy), fill=(150, 140, 120))
    return sheet


def normal(p):
    ax, ay, az = (p[1][i] - p[0][i] for i in range(3))
    bx, by, bz = (p[2][i] - p[0][i] for i in range(3))
    nx, ny, nz = ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx
    length = math.sqrt(nx * nx + ny * ny + nz * nz) or 1
    return nx / length, ny / length, nz / length


def visible(part, pose):
    s = pose.get("%" + part)
    return s is None or s[0] > 0.01


def main():
    gen.build_rig()
    layout = gen.pack()
    atlas = Image.open(gen.ENTITY_DIR / "architect.png").convert("RGBA")
    specs = sys.argv[1:] or ["DORMANT_STATUE:0", "BATTLE_IDLE:0", "GUARD_FULL:0", "WALK:0"]
    OUT.mkdir(parents=True, exist_ok=True)
    for spec in specs:
        clip, tick = spec.split(":")
        props = ("face_mask", "mandament_tablet", "tablet_half")
        hidden = () if clip in ("DORMANT_STATUE", "AWAKEN") else props
        img = render(sample(clip, float(tick)), atlas, layout, hidden=hidden)
        path = OUT / f"preview_{clip.lower()}_{tick}.png"
        img.save(path)
        print(path)


if __name__ == "__main__":
    main()
