"""Render Last Gift Collector bestiary ink plates from the exported 3D Minecraft model (Pillow only).

First run: gradlew.bat -I tools/last-gift-collector-sketches.gradle exportLastGiftCollectorSketchModel
Then run:  python tools/generate_last_gift_collector_sketches.py
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline/textures"
OUT = ASSETS / "gui/bestiary"
POSES = json.loads((ROOT / "build/last-gift-collector-sketches/model.json").read_text())
SKIN = Image.open(ASSETS / "entity/last_gift_collector.png").convert("RGBA")
EYES = Image.open(ASSETS / "entity/last_gift_collector_eyes.png").convert("RGBA")
SS = 3
INK = (77, 52, 29)
FROST_INK = (56, 118, 156)


def project(vertex, yaw=0.52, pitch=0.14):
    x, y, z = vertex[:3]
    sideways = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sideways, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def draw_winter_backdrop(bg: Image.Image, width: int, height: int) -> None:
    d = ImageDraw.Draw(bg)
    w, h = width * SS, height * SS
    # Snowy taiga pine silhouettes and winter moon/sleigh trail in sepia & frost ink
    for tx, th in [(14, 28), (24, 20), (88, 22), (98, 30)]:
        px = tx * SS
        by = (height - 6) * SS
        d.polygon([(px - 5 * SS, by), (px + 5 * SS, by), (px, by - th * SS)],
                  fill=(*INK, 34), outline=(*INK, 72))
    # Crescent moon and sleigh arc in upper sky
    d.arc((12 * SS, 4 * SS, 98 * SS, 36 * SS), 195, 345, fill=(*FROST_INK, 90), width=SS)
    # Packed ice altar ground line
    d.line((6 * SS, (height - 6) * SS, (width - 6) * SS, (height - 6) * SS), fill=(*INK, 85), width=SS)


def render_plate(pose: str, width: int, height: int, combat_fx: bool = False) -> Image.Image:
    faces = POSES[pose]
    yaw = -0.48 if combat_fx else 0.52
    projected = [[project(v, yaw=yaw, pitch=0.14) for v in face["vertices"]] for face in faces]
    xs = [v[0] for face in projected for v in face]
    ys = [v[1] for face in projected for v in face]
    span_x = max(xs) - min(xs)
    span_y = max(ys) - min(ys)
    scale = min((width - 20) / span_x, (height - 10) / span_y) * SS
    left = (width * SS - span_x * scale) / 2
    top = (height * SS - span_y * scale) / 2
    w, h = width * SS, height * SS

    backdrop = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw_winter_backdrop(backdrop, width, height)

    zbuf = [-math.inf] * (w * h)
    owners = [-1] * (w * h)
    colors = [(0, 0, 0, 0)] * (w * h)

    for index, (face, vertices) in enumerate(zip(faces, projected)):
        points = [(left + (x - min(xs)) * scale, top + (y - min(ys)) * scale, z)
                  for x, y, z in vertices]
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
                    uv = [sum(weight * face["vertices"][i][axis]
                              for weight, i in zip([u, v, t], ids)) for axis in [3, 4]]
                    tx = min(255, max(0, int(uv[0] * 256)))
                    ty = min(255, max(0, int(uv[1] * 256)))
                    sample = SKIN.getpixel((tx, ty))
                    glow = EYES.getpixel((tx, ty))
                    if sample[3] == 0:
                        continue
                    zbuf[pixel], owners[pixel] = depth, index
                    if glow[3] > 0 or "scythe" in face["part"] or "cage_bell" in face["part"]:
                        color = (*FROST_INK, 215)
                    else:
                        shade = int(sum(sample[:3]) / 3)
                        color = (*INK, 70 + min(85, shade))
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

    if combat_fx:
        fx_layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        d = ImageDraw.Draw(fx_layer)
        d.arc((14 * SS, 8 * SS, 96 * SS, 64 * SS), 190, 330, fill=(*FROST_INK, 175), width=SS)
        d.arc((18 * SS, 12 * SS, 92 * SS, 60 * SS), 200, 320, fill=(*INK, 145), width=SS)
        drawing = Image.alpha_composite(fx_layer, drawing)

    combined = Image.alpha_composite(backdrop, drawing)
    result = combined.resize((width, height), Image.Resampling.LANCZOS)
    out_name = "last_gift_collector_combat.png" if combat_fx else "last_gift_collector_anatomy.png"
    result.save(OUT / out_name)
    return result


def render_spoils() -> Image.Image:
    image = Image.new("RGBA", (112, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    items = [
        (4, "renegade_greataxe", "I"),
        (32, "cracked_bell", "II"),
        (60, "frostbound_thread", "III"),
        (88, "solstice_seal", "IV"),
    ]
    for x, name, numeral in items:
        sprite = Image.open(ASSETS / f"item/{name}.png").convert("RGBA")
        for y in range(16):
            for sx in range(16):
                r, g, b, alpha = sprite.getpixel((sx, y))
                if alpha:
                    is_frost = b > 170 and g > 140
                    color = (*FROST_INK, 225) if is_frost else (*INK, 190)
                    sprite.putpixel((sx, y), color)
        image.alpha_composite(sprite.resize((24, 24), Image.Resampling.NEAREST), (x, 12))
        draw.line((x + 2, 43, x + 22, 43), fill=(*INK, 115))
        draw.text((x + 10 - len(numeral) * 2, 46), numeral, fill=(*INK, 220))
    image.save(OUT / "last_gift_collector_spoils.png")
    return image


OUT.mkdir(parents=True, exist_ok=True)
plates = [
    render_plate("anatomy", 112, 80, False),
    render_plate("combat", 112, 72, True),
    render_spoils(),
]
review = Image.new("RGBA", (360, 110), (224, 204, 163, 255))
for idx, img in enumerate(plates):
    review.alpha_composite(img, (idx * 120 + 4, (110 - img.height) // 2))
review_out = ROOT / "build/last-gift-collector-sketches/review.png"
review_out.parent.mkdir(parents=True, exist_ok=True)
review.resize((1080, 330), Image.Resampling.NEAREST).save(review_out)
print("Generated 3 Last Gift Collector bestiary plates (anatomy, combat, spoils).")

