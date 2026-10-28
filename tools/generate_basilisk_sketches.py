"""Render bestiary ink plates from the exported, animated Minecraft model (Pillow only).

First run: gradlew.bat -I tools/basilisk-sketches.gradle exportBasiliskSketchModel
Then run:  python tools/generate_basilisk_sketches.py
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline/textures"
OUT = ASSETS / "gui/bestiary"
POSES = json.loads((ROOT / "build/basilisk-sketches/model.json").read_text())
SKIN = Image.open(ASSETS / "entity/basilisk.png").convert("RGBA")
SS = 3
INK = (77, 52, 29)


def project(vertex, yaw=0.72, pitch=0.30):
    x, y, z = vertex[:3]
    sideways = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sideways, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def plate(pose, width, height, breathing=False):
    faces = POSES[pose]
    projected = [[project(v) for v in face["vertices"]] for face in faces]
    xs = [v[0] for face in projected for v in face]
    ys = [v[1] for face in projected for v in face]
    max_width = width - (31 if breathing else 8)
    scale = min(max_width / (max(xs) - min(xs)), (height - 10) / (max(ys) - min(ys))) * SS
    left = 4 * SS
    top = (height * SS - (max(ys) - min(ys)) * scale) / 2
    w, h = width * SS, height * SS
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
                    sample = SKIN.getpixel((min(127, max(0, int(uv[0] * 128))), min(127, max(0, int(uv[1] * 128)))))
                    if sample[3] == 0:
                        continue
                    zbuf[pixel], owners[pixel] = depth, index
                    if "false_eye" in face["part"]:
                        color = (124, 93, 28, 150) if max(sample[:3]) > 95 else (*INK, 245)
                    else:
                        shade = int(sum(sample[:3]) / 3)
                        color = (*INK, 48 + min(60, shade // 2))
                    colors[pixel] = color
    drawing = Image.new("RGBA", (w, h))
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
                color = (*INK, 220)
            elif (x + y * 2) % 18 < 2:
                color = (*INK, max(130, color[3]))
            pixels[x, y] = color
    if breathing:
        mouth = project((0, 1.25, -0.8))
        mx = left + (mouth[0] - min(xs)) * scale
        my = top + (mouth[1] - min(ys)) * scale
        mist = Image.new("RGBA", (w, h))
        d = ImageDraw.Draw(mist)
        for n in range(10):
            cx = mx + (n * 3 + 3) * SS
            cy = my + math.sin(n * 1.8) * (n + 2) * 0.6 * SS
            radius = (2 + n * 0.65) * SS
            d.arc((cx - radius, cy - radius, cx + radius, cy + radius), 50 + n * 23, 300 + n * 23,
                  fill=(*INK, 145 - n * 7), width=SS)
        mist.alpha_composite(drawing)
        drawing = mist
    result = drawing.resize((width, height), Image.Resampling.LANCZOS)
    result.save(OUT / f"basilisk_{'breath' if breathing else 'anatomy'}.png")
    return result


def parts():
    image = Image.new("RGBA", (112, 64))
    draw = ImageDraw.Draw(image)
    for x, name, numeral in [(2, "leg", "I"), (40, "eye", "II"), (78, "tail", "III")]:
        sprite = Image.open(ASSETS / f"item/basilisk_{name}.png").convert("RGBA")
        # Keep the recognisable item shapes, drawn in the same sepia ink as the model plates.
        for y in range(16):
            for sx in range(16):
                r, g, b, alpha = sprite.getpixel((sx, y))
                if alpha:
                    color = (125, 92, 30, 220) if r > 100 and g > 90 and b < 100 else (*INK, 175)
                    sprite.putpixel((sx, y), color)
        image.alpha_composite(sprite.resize((32, 32), Image.Resampling.NEAREST), (x, 9))
        draw.line((x + 3, 46, x + 27, 46), fill=(*INK, 100))
        draw.text((x + 12 - len(numeral), 49), numeral, fill=(*INK, 220))
    image.save(OUT / "basilisk_parts.png")
    return image


OUT.mkdir(parents=True, exist_ok=True)
images = [plate("anatomy", 112, 80), plate("breath", 112, 72, True), parts()]
review = Image.new("RGBA", (360, 110), (224, 204, 163, 255))
for index, image in enumerate(images):
    review.alpha_composite(image, (index * 120 + 4, (110 - image.height) // 2))
review.resize((1080, 330), Image.Resampling.NEAREST).save(ROOT / "build/basilisk-sketches/review.png")
print("Generated 3 bestiary drawings from the Basilisk model and item sprites.")
