r"""Offline preview of the Architect's Scimitar item models in every display context (reform §6).

Run: python tools/preview_architect_scimitar.py
Out: build/architect-scimitar-preview/scimitar_contexts.png and a text report of where the grip and the tip land.

It reads the generated models/item/architect_scimitar_3d*.json (elements with free rotation, per-face UVs, display
transforms) and the 64x64 atlas, applies the same transform chain as the game (centre the model, scale, rotate X after Y
after Z as JOML's rotationXYZ does, translate) and draws it flat-shaded from the front and the side. It is a check of
orientation, proportion and texture mapping, NOT a substitute for runClient: lighting, the hand pose of the player and
the item entity's spin are the game's.
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
OUT = ROOT / "build/architect-scimitar-preview"
CONTEXTS = ["firstperson_righthand", "thirdperson_righthand", "ground", "fixed"]


def rot_matrix(rx, ry, rz):
    """JOML Quaternionf.rotationXYZ: v' = Rx(Ry(Rz(v))), angles in degrees, right-handed."""
    def mat(axis, deg):
        a = math.radians(deg)
        c, s = math.cos(a), math.sin(a)
        if axis == "x":
            return [[1, 0, 0], [0, c, -s], [0, s, c]]
        if axis == "y":
            return [[c, 0, s], [0, 1, 0], [-s, 0, c]]
        return [[c, -s, 0], [s, c, 0], [0, 0, 1]]
    def mul(a, b):
        return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mul(mat("x", rx), mul(mat("y", ry), mat("z", rz)))


def apply(m, v):
    return tuple(sum(m[i][j] * v[j] for j in range(3)) for i in range(3))


def element_faces(el):
    """The six quads of an element (corner order matching the UV order), after the element rotation."""
    (x0, y0, z0), (x1, y1, z1) = el["from"], el["to"]
    rotation = el.get("rotation")
    if rotation:
        o = rotation["origin"]
        m = rot_matrix(rotation.get("x", 0.0), rotation.get("y", 0.0), rotation.get("z", 0.0))
        # JOML rotationZYX(z, y, x) for the element: Rz(Ry(Rx(v))): reverse the order of the item transform
        def mul(a, b):
            return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
        mz = rot_matrix(0, 0, rotation.get("z", 0.0))
        my = rot_matrix(0, rotation.get("y", 0.0), 0)
        mx = rot_matrix(rotation.get("x", 0.0), 0, 0)
        m = mul(mz, mul(my, mx))
    else:
        o, m = (8, 8, 8), None

    def tr(p):
        if m is None:
            return p
        d = (p[0] - o[0], p[1] - o[1], p[2] - o[2])
        r = apply(m, d)
        return (o[0] + r[0], o[1] + r[1], o[2] + r[2])
    # each face: corners in the order top-left, bottom-left, bottom-right, top-right as seen from outside
    quads = {
        "south": [(x0, y1, z1), (x0, y0, z1), (x1, y0, z1), (x1, y1, z1)],
        "north": [(x1, y1, z0), (x1, y0, z0), (x0, y0, z0), (x0, y1, z0)],
        "east": [(x1, y1, z1), (x1, y0, z1), (x1, y0, z0), (x1, y1, z0)],
        "west": [(x0, y1, z0), (x0, y0, z0), (x0, y0, z1), (x0, y1, z1)],
        "up": [(x0, y1, z0), (x0, y1, z1), (x1, y1, z1), (x1, y1, z0)],
        "down": [(x0, y0, z1), (x0, y0, z0), (x1, y0, z0), (x1, y0, z1)],
    }
    out = []
    for name, face in el["faces"].items():
        out.append((name, [tr(p) for p in quads[name]], face["uv"], el.get("light_emission", 0)))
    return out


def transform_context(model, ctx):
    d = model["display"][ctx]
    r, t, sc = d["rotation"], d["translation"], d["scale"]
    m = rot_matrix(*r)

    def f(p):
        v = tuple((p[i] / 16.0 - 0.5) * sc[i] for i in range(3))
        v = apply(m, v)
        return tuple(v[i] + t[i] / 16.0 for i in range(3))
    return f


def render(model, tex, ctx, view, size=300, label=""):
    """view: 'front' looks along -Z (x right, y up), 'side' looks along -X (z right... depth x), '34' a three-quarter view."""
    f = transform_context(model, ctx)
    quads = []
    for el in model["elements"]:
        for name, pts, uv, emission in element_faces(el):
            quads.append((name, [f(p) for p in pts], uv, emission))
    yaw = {"front": 0.0, "side": -90.0, "34": -35.0}[view]
    pitch = {"front": 0.0, "side": 0.0, "34": 18.0}[view]
    a, b = math.radians(yaw), math.radians(pitch)

    def cam(p):
        x = math.cos(a) * p[0] + math.sin(a) * p[2]
        z = -math.sin(a) * p[0] + math.cos(a) * p[2]
        y = math.cos(b) * p[1] - math.sin(b) * z
        z2 = math.sin(b) * p[1] + math.cos(b) * z
        return x, y, z2
    pts_all = [cam(p) for _, q, _, _ in quads for p in q]
    minx, maxx = min(p[0] for p in pts_all), max(p[0] for p in pts_all)
    miny, maxy = min(p[1] for p in pts_all), max(p[1] for p in pts_all)
    span = max(maxx - minx, maxy - miny, 1e-6)
    k = (size - 20) / span
    ss = 2
    w = h = size * ss
    zbuf = [-1e9] * (w * h)
    img = Image.new("RGB", (w, h), (226, 222, 212))
    px = img.load()
    tw, th = tex.size
    tpx = tex.load()
    for name, q, uv, emission in quads:
        cq = [cam(p) for p in q]
        scr = [(((c[0] - minx) * k + 10) * ss + (size - (maxx - minx) * k - 20) * ss / 2, (size - ((c[1] - miny) * k + 10) - (size - (maxy - miny) * k - 20) / 2) * ss, c[2]) for c in cq]
        u0, v0, u1, v1 = uv
        corners_uv = [(u0, v0), (u0, v1), (u1, v1), (u1, v0)]
        nx = (cq[1][1] - cq[0][1]) * (cq[2][2] - cq[0][2]) - (cq[1][2] - cq[0][2]) * (cq[2][1] - cq[0][1])
        ny = (cq[1][2] - cq[0][2]) * (cq[2][0] - cq[0][0]) - (cq[1][0] - cq[0][0]) * (cq[2][2] - cq[0][2])
        nz = (cq[1][0] - cq[0][0]) * (cq[2][1] - cq[0][1]) - (cq[1][1] - cq[0][1]) * (cq[2][0] - cq[0][0])
        ln = math.sqrt(nx * nx + ny * ny + nz * nz) or 1.0
        light = 0.62 + 0.38 * max(0.0, (-nx * 0.3 + ny * 0.5 + nz * 0.81) / ln)
        for tri in ((0, 1, 2), (0, 2, 3)):
            A, B, C = (scr[i] for i in tri)
            ua, ub, uc = (corners_uv[i] for i in tri)
            den = (B[1] - C[1]) * (A[0] - C[0]) + (C[0] - B[0]) * (A[1] - C[1])
            if abs(den) < 1e-9:
                continue
            for y in range(max(0, int(min(A[1], B[1], C[1]))), min(h, int(max(A[1], B[1], C[1])) + 2)):
                for x in range(max(0, int(min(A[0], B[0], C[0]))), min(w, int(max(A[0], B[0], C[0])) + 2)):
                    l1 = ((B[1] - C[1]) * (x + 0.5 - C[0]) + (C[0] - B[0]) * (y + 0.5 - C[1])) / den
                    l2 = ((C[1] - A[1]) * (x + 0.5 - C[0]) + (A[0] - C[0]) * (y + 0.5 - C[1])) / den
                    l3 = 1 - l1 - l2
                    if min(l1, l2, l3) < -1e-3:
                        continue
                    z = l1 * A[2] + l2 * B[2] + l3 * C[2]
                    if z < zbuf[y * w + x]:
                        continue
                    u = l1 * ua[0] + l2 * ub[0] + l3 * uc[0]
                    v = l1 * ua[1] + l2 * ub[1] + l3 * uc[1]
                    c = tpx[min(tw - 1, max(0, int(u / 16.0 * tw))), min(th - 1, max(0, int(v / 16.0 * th)))]
                    if c[3] == 0:
                        continue
                    zbuf[y * w + x] = z
                    lt = 1.0 if emission else light
                    px[x, y] = (int(c[0] * lt), int(c[1] * lt), int(c[2] * lt))
    img = img.resize((size, size), Image.Resampling.LANCZOS)
    ImageDraw.Draw(img).text((6, 4), label or f"{ctx} {view}", fill=(50, 40, 30))
    return img


def report(model):
    """Where the grip centre and the tip land after each hand transform, next to a vanilla sword sprite's handle (3,3) and tip (15,15)."""
    lines = []
    grip = next(e for e in model["elements"] if e["name"] == "grip_ring")
    tip = next(e for e in model["elements"] if e["name"] == "point")

    def centre(e):
        pts = [p for _, q, _, _ in element_faces(e) for p in q]
        return tuple(sum(p[i] for p in pts) / len(pts) for i in range(3))
    for ctx in ("thirdperson_righthand", "firstperson_righthand", "ground"):
        f = transform_context(model, ctx)
        g, t = f(centre(grip)), f(centre(tip))
        vg, vt = f((3.5, 3.5, 8.0)), f((15.0, 15.0, 8.0))
        lines.append(f"{ctx}: grip {tuple(round(c * 16, 2) for c in g)} (vanilla handle {tuple(round(c * 16, 2) for c in vg)}), "
                     f"tip {tuple(round(c * 16, 2) for c in t)} (vanilla tip {tuple(round(c * 16, 2) for c in vt)})")
    return lines


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    model = json.loads((ASSETS / "models/item/architect_scimitar_3d.json").read_text())
    tex = Image.open(ASSETS / "textures/item/architect_scimitar_3d.png").convert("RGBA")
    tiles = []
    for ctx in CONTEXTS:
        for view in ("front", "side", "34"):
            tiles.append(render(model, tex, ctx, view, 260, f"{ctx}: {view}"))
    icon = Image.open(ASSETS / "textures/item/architect_scimitar.png").convert("RGBA").resize((260, 260), Image.Resampling.NEAREST)
    bg = Image.new("RGB", (260, 260), (226, 222, 212))
    bg.paste(icon, (0, 0), icon)
    ImageDraw.Draw(bg).text((6, 4), "gui (2D icon)", fill=(50, 40, 30))
    tiles.append(bg)
    cols = 3
    rows = (len(tiles) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * 260, rows * 260), (226, 222, 212))
    for i, t in enumerate(tiles):
        sheet.paste(t, ((i % cols) * 260, (i // cols) * 260))
    sheet.save(OUT / "scimitar_contexts.png")
    text = "\n".join(report(model))
    (OUT / "report.txt").write_text(text + "\n", encoding="utf-8")
    print(text)
    print(f"Wrote {OUT / 'scimitar_contexts.png'}")


if __name__ == "__main__":
    main()
