r"""Renders the Architect pose review sheet from the REAL exported Java model (textured, with the glow layer).

First run: .\gradlew.bat -I tools/architect-sketches.gradle exportArchitectSketchModel
Then run:  python tools/generate_architect_sketches.py
Out:       build/architect-sketches/pose_<name>.png (front 3/4 and side) and architect_pose_sheet.png (all poses)
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src/main/resources/assets/codenamecoraline/textures/entity"
SKETCH_DIR = ROOT / "build/architect-sketches"
SKIN = Image.open(TEX / "architect.png").convert("RGBA")
GLOW = Image.open(TEX / "architect_glow.png").convert("RGBA")
SS = 2
W, H = int(__import__('os').environ.get('ARCHITECT_TILE_W', 260)), int(__import__('os').environ.get('ARCHITECT_TILE_H', 320))
VIEWS = ((-0.55, 0.12), (math.pi / 2 * -1, 0.05))  # three-quarters from his left front, and his left side
_front = __import__('os').environ.get('ARCHITECT_FRONT')  # "1": the second view is straight from the front instead of the side
if _front:
    VIEWS = ((-0.55, 0.12), (0.0, 0.03))


def project(v, yaw, pitch):
    x, y, z = (c * 16 for c in v[:3])  # the export is in blocks; work in model pixels
    sx = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sx, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def render(faces, yaw, pitch, bounds):
    w, h = W * SS, H * SS
    minx, maxx, miny, maxy = bounds
    k = min((w - 20) / (maxx - minx), (h - 30) / (maxy - miny))
    zbuf = [-math.inf] * (w * h)
    owner = [-1] * (w * h)
    color = [None] * (w * h)
    for idx, face in enumerate(faces):
        raw = face["vertices"]
        pts = [project(v, yaw, pitch) for v in raw]
        pts = [((p[0] - minx) * k + 10, (p[1] - miny) * k + 20, p[2]) for p in pts]
        ax, ay = pts[1][0] - pts[0][0], pts[1][1] - pts[0][1]
        bx, by = pts[2][0] - pts[0][0], pts[2][1] - pts[0][1]
        shade = 0.78 + 0.22 * (1 if ax * by - ay * bx > 0 else 0.6)
        for tri in ((0, 1, 2), (0, 2, 3)):
            a, b, c = (pts[i] for i in tri)
            ra, rb, rc = (raw[i] for i in tri)
            den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
            if abs(den) < 1e-9:
                continue
            for py in range(max(0, int(min(a[1], b[1], c[1]))), min(h, int(max(a[1], b[1], c[1])) + 1)):
                for px in range(max(0, int(min(a[0], b[0], c[0]))), min(w, int(max(a[0], b[0], c[0])) + 1)):
                    x, y = px + 0.5, py + 0.5
                    u = ((b[1] - c[1]) * (x - c[0]) + (c[0] - b[0]) * (y - c[1])) / den
                    v = ((c[1] - a[1]) * (x - c[0]) + (a[0] - c[0]) * (y - c[1])) / den
                    t = 1 - u - v
                    if min(u, v, t) < -1e-4:
                        continue
                    depth = u * a[2] + v * b[2] + t * c[2]
                    i = py * w + px
                    if depth < zbuf[i]:
                        continue
                    tu = min(SKIN.width - 1, max(0, int((u * ra[3] + v * rb[3] + t * rc[3]) * SKIN.width)))
                    tv = min(SKIN.height - 1, max(0, int((u * ra[4] + v * rb[4] + t * rc[4]) * SKIN.height)))
                    s = SKIN.getpixel((tu, tv))
                    g = GLOW.getpixel((tu, tv))
                    if s[3] == 0 and g[3] == 0:
                        continue
                    zbuf[i] = depth
                    owner[i] = idx
                    color[i] = g[:3] if g[3] else tuple(int(ch * shade) for ch in s[:3])
    img = Image.new("RGB", (w, h), (232, 226, 214))
    px = img.load()
    for yy in range(1, h - 1):
        for xx in range(1, w - 1):
            i = yy * w + xx
            if owner[i] < 0:
                continue
            edge = any(owner[i + d] != owner[i] for d in (-1, 1, -w, w))
            px[xx, yy] = (16, 16, 22) if edge else color[i]
    gy = (24 * math.cos(pitch) - miny) * k + 20  # model y = 24 px is the ground
    ImageDraw.Draw(img).line((0, gy, w, gy), fill=(150, 140, 120), width=1)
    return img.resize((W, H), Image.Resampling.LANCZOS)


def main():
    poses = json.loads((SKETCH_DIR / "model.json").read_text())
    only = set(__import__('sys').argv[1:])  # optional: render only these poses
    tiles = []
    for name, faces in poses.items():
        if only and name not in only:
            continue
        views = []
        for yaw, pitch in VIEWS:
            pts = [project(v, yaw, pitch) for f in faces for v in f["vertices"]]
            bounds = (-68, 68, min(-36, min(p[1] for p in pts)) - 2, 27)
            zoom = __import__('os').environ.get('ARCHITECT_ZOOM')  # "minx,maxx,miny,maxy" in model pixels (y up is negative)
            if zoom:
                bounds = tuple(float(v) for v in zoom.split(','))
            views.append(render(faces, yaw, pitch, bounds))
        tile = Image.new("RGB", (W * 2, H + 14), (232, 226, 214))
        tile.paste(views[0], (0, 14))
        tile.paste(views[1], (W, 14))
        ImageDraw.Draw(tile).text((4, 1), name, fill=(60, 40, 30))
        tile.save(SKETCH_DIR / f"pose_{name}.png")
        tiles.append(tile)
        print(f"pose_{name}.png")
    cols = 3
    rows = (len(tiles) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * W * 2, rows * (H + 14)), (232, 226, 214))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % cols) * W * 2, (i // cols) * (H + 14)))
    sheet.save(SKETCH_DIR / "architect_pose_sheet.png")
    print("architect_pose_sheet.png")


if __name__ == "__main__":
    main()
