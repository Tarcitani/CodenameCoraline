"""Render the Witch of Ten Threads bestiary ink plates from the exported 3D Minecraft models (Pillow only).

First run: export build/ten-threads-sketches/model.json with `ExportTenThreadsWitchModel` (tools/ten-threads-witch-sketches.gradle,
task exportTenThreadsSketchModel, or javac against the dev classpath).
Then run:  python -I tools/generate_ten_threads_witch_sketches.py

Plates (sizes fixed by the Bestiary): anatomy 112x80 (the Matriarch at rest, ten spools on her wheel), combat 112x72 (the
Cradle Giant winding up while the crimson thread tightens to the staff) and spoils 112x64 (staff, remnant, thimble).
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline/textures"
OUT = ASSETS / "gui/bestiary"
POSES = json.loads((ROOT / "build/ten-threads-sketches/model.json").read_text())
SS = 3
INK = (77, 52, 29)
RED_INK = (150, 34, 30)
SILVER_INK = (92, 96, 112)
SKINS = {}
EYES = {}


def skin(key):
    i = key.split("@")[0]
    if i not in SKINS:
        SKINS[i] = Image.open(ASSETS / f"entity/{i}.png").convert("RGBA")
        eyes = ASSETS / f"entity/{i}_eyes.png"
        EYES[i] = Image.open(eyes).convert("RGBA") if eyes.exists() else Image.new("RGBA", SKINS[i].size, (0, 0, 0, 0))
    return SKINS[i], EYES[i]


def project(vertex, yaw, pitch=0.12):
    x, y, z = vertex[:3]
    sideways = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sideways, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def draw_forest_backdrop(bg: Image.Image, width: int, height: int) -> None:
    d = ImageDraw.Draw(bg)
    ground = (height - 6) * SS
    # two old trunks, hanging threads from the canopy and the mossy floor of the theatre
    for tx, tw in [(4, 6), (width - 10, 6)]:
        d.rectangle((tx * SS, 2 * SS, (tx + tw) * SS, ground), fill=(*INK, 26), outline=(*INK, 80))
        for by in range(6, height - 8, 7):
            d.line((tx * SS, by * SS, (tx + tw - 1) * SS, (by + 2) * SS), fill=(*INK, 50), width=1)
    for k, hx in enumerate((22, 40, 62, 84)):
        d.line((hx * SS, 0, hx * SS, (6 + (k % 2) * 6) * SS), fill=(*SILVER_INK, 110), width=1)
    d.line((4 * SS, ground, (width - 4) * SS, ground), fill=(*INK, 100), width=SS)
    for rx in range(10, width - 6, 12):
        d.line((rx * SS, ground, (rx - 4) * SS, (height - 2) * SS), fill=(*INK, 60), width=SS)


def render_plate(key: str, width: int, height: int, yaw: float, out_name: str, thread: bool = False) -> Image.Image:
    faces = POSES[key]
    sk, glow_tex = skin(key)
    projected = [[project(v, yaw) for v in face["vertices"]] for face in faces]
    xs = [v[0] for face in projected for v in face]
    ys = [v[1] for face in projected for v in face]
    span_x = max(xs) - min(xs)
    span_y = max(ys) - min(ys)
    scale = min((width - 16) / span_x, (height - 8) / span_y) * SS
    left = (width * SS - span_x * scale) / 2
    top = (height * SS - span_y * scale) / 2 - 2 * SS
    w, h = width * SS, height * SS

    backdrop = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw_forest_backdrop(backdrop, width, height)

    zbuf = [-math.inf] * (w * h)
    owners = [-1] * (w * h)
    colors = [(0, 0, 0, 0)] * (w * h)
    for index, (face, vertices) in enumerate(zip(faces, projected)):
        points = [(left + (x - min(xs)) * scale, top + (y - min(ys)) * scale, z) for x, y, z in vertices]
        for ids in [(0, 1, 2), (0, 2, 3)]:
            a, b, c = [points[i] for i in ids]
            denom = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
            if abs(denom) < 1e-8:
                continue
            for py in range(max(0, int(min(a[1], b[1], c[1]))), min(h, int(max(a[1], b[1], c[1])) + 1)):
                for px in range(max(0, int(min(a[0], b[0], c[0]))), min(w, int(max(a[0], b[0], c[0])) + 1)):
                    x, y = px + 0.5, py + 0.5
                    u = ((b[1] - c[1]) * (x - c[0]) + (c[0] - b[0]) * (y - c[1])) / denom
                    v = ((c[1] - a[1]) * (x - c[0]) + (a[0] - c[0]) * (y - c[1])) / denom
                    t = 1 - u - v
                    if min(u, v, t) < -1e-6:
                        continue
                    depth = u * a[2] + v * b[2] + t * c[2]
                    pixel = py * w + px
                    if depth < zbuf[pixel]:
                        continue
                    uv = [sum(weight * face["vertices"][i][axis] for weight, i in zip([u, v, t], ids)) for axis in [3, 4]]
                    tx = min(sk.width - 1, max(0, int(uv[0] * sk.width)))
                    ty = min(sk.height - 1, max(0, int(uv[1] * sk.height)))
                    sample = sk.getpixel((tx, ty))
                    glow = glow_tex.getpixel((tx, ty))
                    if sample[3] == 0:
                        continue
                    zbuf[pixel], owners[pixel] = depth, index
                    if glow[3] > 0:
                        color = (*RED_INK, 235)
                    else:
                        shade = int(sum(sample[:3]) / 3)
                        color = (*INK, 95 + min(125, int(shade * 1.1)))
                    colors[pixel] = color

    drawing = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    pixels = drawing.load()
    for y in range(h):
        for x in range(w):
            i = y * w + x
            if owners[i] < 0:
                continue
            edge = any(owners[ny * w + nx] != owners[i]
                       for nx, ny in [(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)]
                       if 0 <= nx < w and 0 <= ny < h)
            color = colors[i]
            if edge:
                color = (*INK, 235)
            elif (x + y * 2) % 16 < 2:
                color = (*INK, max(140, color[3]))
            pixels[x, y] = color

    if thread:
        fx = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        d = ImageDraw.Draw(fx)
        # the crimson thread tightening from the puppet's head to the staff beyond the canopy, with tension ticks
        hx = left + (max(xs) - min(xs)) * scale * 0.52
        d.line((hx, top + SS, (width - 6) * SS, 0), fill=(*RED_INK, 230), width=SS)
        for k in range(1, 5):
            tx = hx + ((width - 6) * SS - hx) * k / 5
            ty = top + SS + (0 - top - SS) * k / 5
            d.line((tx - SS, ty - 2 * SS, tx + SS, ty + 2 * SS), fill=(*RED_INK, 190), width=1)
        for k, yy in enumerate((30, 37, 44)):  # speed lines: the wind-up
            d.line((5 * SS, yy * SS, (22 - k * 3) * SS, yy * SS), fill=(*INK, 140), width=SS)
        drawing = Image.alpha_composite(fx, drawing)

    combined = Image.alpha_composite(backdrop, drawing)
    result = combined.resize((width, height), Image.Resampling.LANCZOS)
    result.save(OUT / out_name)
    return result


def render_spoils() -> Image.Image:
    image = Image.new("RGBA", (112, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    items = [(10, "ten_threads_staff", "I"), (44, "puppet_remnant", "II"), (78, "matriarch_thimble", "III")]
    for x, name, numeral in items:
        sprite = Image.open(ASSETS / f"item/{name}.png").convert("RGBA")
        for y in range(sprite.height):
            for sx in range(sprite.width):
                r, g, b, alpha = sprite.getpixel((sx, y))
                if alpha:
                    red = r > g + 60 and r > b + 40
                    sprite.putpixel((sx, y), (*RED_INK, 225) if red else (*INK, 195))
        image.alpha_composite(sprite.resize((24, 24), Image.Resampling.NEAREST), (x, 12))
        draw.line((x + 2, 43, x + 22, 43), fill=(*INK, 115))
        draw.text((x + 10 - len(numeral) * 2, 46), numeral, fill=(*INK, 220))
    image.save(OUT / "ten_threads_witch_spoils.png")
    return image


OUT.mkdir(parents=True, exist_ok=True)
plates = [
    render_plate("ten_threads_witch@idle@1.0", 112, 80, 0.55, "ten_threads_witch_anatomy.png"),
    render_plate("cradle_giant@windup_crimson@1.0", 112, 72, 0.35, "ten_threads_witch_combat.png", thread=True),
    render_spoils(),
]
review = Image.new("RGBA", (360, 110), (224, 204, 163, 255))
for idx, img in enumerate(plates):
    review.alpha_composite(img, (idx * 120 + 4, (110 - img.height) // 2))
review_out = ROOT / "build/ten-threads-sketches/review.png"
review.resize((1080, 330), Image.Resampling.NEAREST).save(review_out)
print("Generated 3 Witch of Ten Threads bestiary plates (anatomy, combat, spoils).")
