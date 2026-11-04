"""Render the Crimson Commander bestiary ink plates from the exported 3D Minecraft model (Pillow only).

First run: gradlew.bat -I tools/crimson-commander-sketches.gradle exportCrimsonCommanderSketchModel
Then run:  python tools/generate_crimson_commander_sketches.py

Plates (sizes fixed by the Bestiary): anatomy 112x80 (Combat Idle Stance, the sword on his shoulder), combat 112x72
(the Dash Thrust with speed lines and the crimson trail) and spoils 112x64 (greatsword, plate shard, crest).
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline/textures"
OUT = ASSETS / "gui/bestiary"
POSES = json.loads((ROOT / "build/crimson-commander-sketches/model.json").read_text())
SKIN = Image.open(ASSETS / "entity/crimson_commander.png").convert("RGBA")
EYES = Image.open(ASSETS / "entity/crimson_commander_eyes.png").convert("RGBA")
SS = 3
INK = (77, 52, 29)
RED_INK = (150, 34, 30)
GOLD_INK = (150, 112, 40)


def project(vertex, yaw, pitch=0.12):
    x, y, z = vertex[:3]
    sideways = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sideways, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def draw_hall_backdrop(bg: Image.Image, width: int, height: int) -> None:
    d = ImageDraw.Draw(bg)
    ground = (height - 6) * SS
    # two deepslate pillars with an arch between them, a hanging banner and the tiled floor of the hall
    for tx, tw in [(5, 7), (width - 12, 7)]:
        d.rectangle((tx * SS, 3 * SS, (tx + tw) * SS, ground), fill=(*INK, 28), outline=(*INK, 80))
        for by in range(8, height - 8, 6):
            d.line((tx * SS, by * SS, (tx + tw) * SS, by * SS), fill=(*INK, 55), width=1)
    d.arc((12 * SS, -18 * SS, (width - 12) * SS, 30 * SS), 200, 340, fill=(*INK, 90), width=SS)
    d.polygon([(width * 0.78 * SS, 4 * SS), (width * 0.86 * SS, 4 * SS), (width * 0.86 * SS, 22 * SS),
               (width * 0.82 * SS, 18 * SS), (width * 0.78 * SS, 22 * SS)], fill=(*RED_INK, 70), outline=(*RED_INK, 120))
    d.line((4 * SS, ground, (width - 4) * SS, ground), fill=(*INK, 100), width=SS)
    for rx in range(10, width - 6, 12):
        d.line((rx * SS, ground, (rx - 4) * SS, (height - 2) * SS), fill=(*INK, 60), width=SS)


def render_plate(pose: str, width: int, height: int, yaw: float, out_name: str, thrust: bool = False) -> Image.Image:
    faces = POSES[pose]
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
    draw_hall_backdrop(backdrop, width, height)

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
                    tx = min(255, max(0, int(uv[0] * 256)))
                    ty = min(255, max(0, int(uv[1] * 256)))
                    sample = SKIN.getpixel((tx, ty))
                    glow = EYES.getpixel((tx, ty))
                    if sample[3] == 0:
                        continue
                    zbuf[pixel], owners[pixel] = depth, index
                    if glow[3] > 0:
                        color = (*RED_INK, 235)
                    elif face["part"] == "sword":
                        gold = sample[0] > 140 and sample[1] > 100
                        color = (*GOLD_INK, 210) if gold else (*INK, 225)
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

    if thrust:
        fx = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        d = ImageDraw.Draw(fx)
        # speed lines behind him and the crimson trail of the dash along the floor
        for k, yy in enumerate((24, 31, 38, 45, 52)):
            d.line((6 * SS, yy * SS, (30 - k * 3) * SS, yy * SS), fill=(*INK, 150), width=SS)
        ground = (height - 7) * SS
        for k in range(9):
            d.ellipse(((10 + k * 6) * SS, ground - SS, (12 + k * 6) * SS, ground + SS), fill=(*RED_INK, 120 + k * 10))
        drawing = Image.alpha_composite(fx, drawing)

    combined = Image.alpha_composite(backdrop, drawing)
    result = combined.resize((width, height), Image.Resampling.LANCZOS)
    result.save(OUT / out_name)
    return result


def render_spoils() -> Image.Image:
    image = Image.new("RGBA", (112, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    items = [(10, "crimson_oath_greatsword", "I"), (44, "crimson_plate_shard", "II"), (78, "stained_oath_crest", "III")]
    for x, name, numeral in items:
        sprite = Image.open(ASSETS / f"item/{name}.png").convert("RGBA")
        for y in range(16):
            for sx in range(16):
                r, g, b, alpha = sprite.getpixel((sx, y))
                if alpha:
                    red = r > g + 60 and r > b + 40
                    gold = r > 150 and g > 110 and b < 110
                    sprite.putpixel((sx, y), (*RED_INK, 225) if red else (*GOLD_INK, 215) if gold else (*INK, 195))
        image.alpha_composite(sprite.resize((24, 24), Image.Resampling.NEAREST), (x, 12))
        draw.line((x + 2, 43, x + 22, 43), fill=(*INK, 115))
        draw.text((x + 10 - len(numeral) * 2, 46), numeral, fill=(*INK, 220))
    image.save(OUT / "crimson_commander_spoils.png")
    return image


OUT.mkdir(parents=True, exist_ok=True)
plates = [
    render_plate("anatomy", 112, 80, 0.55, "crimson_commander_anatomy.png"),
    render_plate("combat", 112, 72, 1.25, "crimson_commander_combat.png", thrust=True),
    render_spoils(),
]
review = Image.new("RGBA", (360, 110), (224, 204, 163, 255))
for idx, img in enumerate(plates):
    review.alpha_composite(img, (idx * 120 + 4, (110 - img.height) // 2))
review_out = ROOT / "build/crimson-commander-sketches/review.png"
review_out.parent.mkdir(parents=True, exist_ok=True)
review.resize((1080, 330), Image.Resampling.NEAREST).save(review_out)
print("Generated 3 Crimson Commander bestiary plates (anatomy, combat, spoils).")
