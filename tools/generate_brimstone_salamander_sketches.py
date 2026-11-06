r"""Render Brimstone Salamander offline pose previews and Bestiary ink plates from the exported model (Pillow).

First run: .\gradlew.bat -I tools/brimstone-salamander-sketches.gradle exportBrimstoneSalamanderSketchModel
Then run:  python tools/generate_brimstone_salamander_sketches.py
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline/textures"
BESTIARY_OUT = ASSETS / "gui/bestiary"
SKETCH_DIR = ROOT / "build/brimstone-salamander-sketches"

POSES = json.loads((SKETCH_DIR / "model.json").read_text())
SKIN = Image.open(ASSETS / "entity/brimstone_salamander.png").convert("RGBA")
EYES = Image.open(ASSETS / "entity/brimstone_salamander_eyes.png").convert("RGBA")

SS = 3
INK = (68, 48, 32)
SULPHUR_INK = (212, 138, 36)


def project(vertex, yaw=0.68, pitch=0.26):
    x, y, z = vertex[:3]
    sideways = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sideways, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def render_pose_image(pose: str, width: int, height: int, yaw: float = 0.68, pitch: float = 0.26,
                      sepia_mode: bool = False) -> Image.Image:
    faces = POSES[pose]
    projected = [[project(v, yaw=yaw, pitch=pitch) for v in face["vertices"]] for face in faces]
    xs = [v[0] for face in projected for v in face]
    ys = [v[1] for face in projected for v in face]
    span_x = max(1e-4, max(xs) - min(xs))
    span_y = max(1e-4, max(ys) - min(ys))
    scale = min((width - 20) / span_x, (height - 14) / span_y) * SS
    left = (width * SS - span_x * scale) / 2
    top = (height * SS - span_y * scale) / 2
    w, h = width * SS, height * SS

    zbuf = [-math.inf] * (w * h)
    owners = [-1] * (w * h)
    colors = [(0, 0, 0, 0)] * (w * h)

    for index, (face, vertices) in enumerate(zip(faces, projected)):
        points = [(left + (x - min(xs)) * scale, top + (y - min(ys)) * scale, z)
                  for x, y, z in vertices]
        raw = face["vertices"]
        ax, ay, az = points[1][0] - points[0][0], points[1][1] - points[0][1], points[1][2] - points[0][2]
        bx, by, bz = points[2][0] - points[0][0], points[2][1] - points[0][1], points[2][2] - points[0][2]
        nx, ny, nz = ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx
        nlen = max(1e-6, math.sqrt(nx * nx + ny * ny + nz * nz))
        shade = 0.80 + 0.28 * max(0.0, (-0.35 * nx - 0.55 * ny + 0.75 * nz) / nlen)

        for ids in [(0, 1, 2), (0, 2, 3)]:
            a, b, c = [points[i] for i in ids]
            ra, rb, rc = [raw[i] for i in ids]
            denom = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
            if abs(denom) < 1e-8:
                continue
            for py in range(max(0, int(min(a[1], b[1], c[1]))), min(h, int(max(a[1], b[1], c[1])) + 1)):
                for px in range(max(0, int(min(a[0], b[0], c[0]))), min(w, int(max(a[0], b[0], c[0])) + 1)):
                    x, y = px + 0.5, py + 0.5
                    u = ((b[1] - c[1]) * (x - c[0]) + (c[0] - b[0]) * (y - c[1])) / denom
                    v = ((c[1] - a[1]) * (x - c[0]) + (a[0] - c[0]) * (y - c[1])) / denom
                    t = 1.0 - u - v
                    if min(u, v, t) < -1e-5:
                        continue
                    depth = u * a[2] + v * b[2] + t * c[2]
                    pixel = py * w + px
                    if depth < zbuf[pixel]:
                        continue
                    tu = int((u * ra[3] + v * rb[3] + t * rc[3]) * SKIN.width) % SKIN.width
                    tv = int((u * ra[4] + v * rb[4] + t * rc[4]) * SKIN.height) % SKIN.height
                    sr, sg, sb, sa = SKIN.getpixel((tu, tv))
                    er, eg, eb, ea = EYES.getpixel((tu, tv))
                    if sa == 0 and ea == 0:
                        continue
                    zbuf[pixel] = depth
                    owners[pixel] = index
                    if ea > 0:
                        colors[pixel] = (er, eg, eb, 255)
                    elif sepia_mode:
                        lum = (0.299 * sr + 0.587 * sg + 0.114 * sb) / 255.0
                        ink_alpha = int(min(245, max(85, (1.0 - lum * 0.55) * 215)))
                        colors[pixel] = (*INK, ink_alpha)
                    else:
                        colors[pixel] = (
                            min(255, int(sr * shade)),
                            min(255, int(sg * shade)),
                            min(255, int(sb * shade)),
                            255,
                        )

    canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0) if sepia_mode else (238, 232, 218, 255))
    if not sepia_mode:
        draw = ImageDraw.Draw(canvas)
        draw.rectangle((2, 2, w - 3, h - 3), outline=(120, 105, 84, 255), width=2)

    for py in range(1, h - 1):
        for px in range(1, w - 1):
            idx = py * w + px
            if owners[idx] < 0:
                continue
            edge = any(owners[py * w + px + dx + dy * w] != owners[idx]
                       for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)])
            if edge:
                canvas.putpixel((px, py), (*INK, 235) if sepia_mode else (14, 15, 20, 255))
            else:
                canvas.putpixel((px, py), colors[idx])

    return canvas.resize((width, height), Image.Resampling.LANCZOS)


def main() -> None:
    SKETCH_DIR.mkdir(parents=True, exist_ok=True)
    BESTIARY_OUT.mkdir(parents=True, exist_ok=True)

    review_poses = [
        ("idle", 0.68, 0.24),
        ("idle_throat_puff", 0.64, 0.20),
        ("walk", 0.72, 0.26),
        ("swim_lava", 0.58, 0.28),
        ("snap_bite_prep", 0.75, 0.22),
        ("snap_bite_exec", 0.75, 0.22),
        ("sulfur_spit_prep", 0.65, 0.20),
        ("sulfur_spit_exec", 0.65, 0.20),
        ("tail_whip_exec", -0.75, 0.26),
    ]

    sheet = Image.new("RGBA", (200 * 3, 150 * 3), (238, 232, 218, 255))
    for i, (pose_name, yaw, pitch) in enumerate(review_poses):
        img = render_pose_image(pose_name, 200, 150, yaw=yaw, pitch=pitch, sepia_mode=False)
        out_path = SKETCH_DIR / f"pose_{pose_name}.png"
        img.save(out_path)
        sheet.paste(img, ((i % 3) * 200, (i // 3) * 150))
        print(f"Saved pose preview: {out_path}")

    sheet_path = SKETCH_DIR / "brimstone_salamander_pose_sheet.png"
    sheet.save(sheet_path)
    print(f"Saved composite pose sheet: {sheet_path}")

    # Bestiary plates (112x80 anatomy, 112x72 combat, 112x64 spoils)
    anatomy = render_pose_image("anatomy", 112, 80, yaw=0.68, pitch=0.24, sepia_mode=True)
    combat = render_pose_image("combat", 112, 72, yaw=-0.62, pitch=0.22, sepia_mode=True)
    spoils = Image.new("RGBA", (112, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(spoils)
    # Left: Brimstone Scale study; Right: Magma cream & sulphur coal study
    d.polygon([(18, 36), (30, 16), (48, 20), (44, 42), (26, 48)], fill=(*INK, 95), outline=(*INK, 230))
    d.line((26, 26, 40, 36), fill=(*SULPHUR_INK, 240), width=2)
    d.ellipse((66, 20, 90, 44), fill=(*INK, 85), outline=(*INK, 225))
    d.ellipse((72, 26, 84, 38), fill=(*SULPHUR_INK, 220))

    anatomy.save(BESTIARY_OUT / "brimstone_salamander_anatomy.png")
    combat.save(BESTIARY_OUT / "brimstone_salamander_combat.png")
    spoils.save(BESTIARY_OUT / "brimstone_salamander_spoils.png")
    print(f"Saved Bestiary plates: anatomy, combat, and spoils in {BESTIARY_OUT}")


if __name__ == "__main__":
    main()
