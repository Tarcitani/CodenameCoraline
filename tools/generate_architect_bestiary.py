r"""Renders the Architect's bestiary ink plates from the REAL exported Java model (Pillow only).

First run: .\gradlew.bat -I tools/architect-sketches.gradle exportArchitectSketchModel
           python tools/generate_architect_scimitar_assets.py   (the spoils plate inks the scimitar icon)
Then run:  python tools/generate_architect_bestiary.py
Out:       textures/gui/bestiary/architect_{anatomy,combat,spoils}.png (112x80, 112x72, 112x64)
           build/architect-sketches/bestiary_review.png
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline/textures"
OUT = ASSETS / "gui/bestiary"
POSES = json.loads((ROOT / "build/architect-sketches/model.json").read_text())
SKIN = Image.open(ASSETS / "entity/architect.png").convert("RGBA")
GLOW = Image.open(ASSETS / "entity/architect_glow.png").convert("RGBA")
SS = 3
INK = (60, 48, 42)
MANA_INK = (38, 128, 126)
CORAL_INK = (178, 72, 52)
RUNE_INK = (112, 92, 160)


def project(v, yaw, pitch=0.12):
    x, y, z = (c * 16 for c in v[:3])
    sx = math.cos(yaw) * x - math.sin(yaw) * z
    along = math.sin(yaw) * x + math.cos(yaw) * z
    return sx, math.cos(pitch) * y - math.sin(pitch) * along, -math.sin(pitch) * y - math.cos(pitch) * along


def temple_backdrop(width, height):
    bg = Image.new("RGBA", (width * SS, height * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(bg)
    ground = (height - 6) * SS
    # two temple pillars, an arch and a stepped floor, all faint
    for px in (5, width - 12):
        d.rectangle((px * SS, 6 * SS, (px + 6) * SS, ground), outline=(*INK, 70), fill=(*INK, 18))
        for by in range(10, height - 8, 9):
            d.line((px * SS, by * SS, (px + 6) * SS, by * SS), fill=(*INK, 45), width=1)
    d.arc((11 * SS, -18 * SS, (width - 11) * SS, 34 * SS), 200, 340, fill=(*INK, 60), width=SS)
    d.line((4 * SS, ground, (width - 4) * SS, ground), fill=(*INK, 100), width=SS)
    d.line((10 * SS, ground + 3 * SS, (width - 10) * SS, ground + 3 * SS), fill=(*INK, 55), width=SS)
    return bg


def render_plate(pose, width, height, yaw, out_name, fx=None):
    faces = POSES[pose]
    proj = [[project(v, yaw) for v in f["vertices"]] for f in faces]
    xs = [p[0] for f in proj for p in f]
    ys = [p[1] for f in proj for p in f]
    scale = min((width - 14) / (max(xs) - min(xs)), (height - 8) / (max(ys) - min(ys))) * SS
    left = (width * SS - (max(xs) - min(xs)) * scale) / 2
    top = (height * SS - (max(ys) - min(ys)) * scale) / 2 - SS
    w, h = width * SS, height * SS
    zbuf = [-math.inf] * (w * h)
    owner = [-1] * (w * h)
    color = [None] * (w * h)
    for idx, (face, pts3) in enumerate(zip(faces, proj)):
        raw = face["vertices"]
        pts = [(left + (x - min(xs)) * scale, top + (y - min(ys)) * scale, z) for x, y, z in pts3]
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
                    if g[3]:
                        # coral eyes stay coral, the rest of the glow (seal, grooves) is mana ink
                        color[i] = (*(CORAL_INK if g[0] > g[2] + 40 else MANA_INK), 235)
                    else:
                        shade = sum(s[:3]) / 3
                        color[i] = (*INK, 70 + min(130, int(shade * 1.1)))
    drawing = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    out = drawing.load()
    for yy in range(1, h - 1):
        for xx in range(1, w - 1):
            i = yy * w + xx
            if owner[i] < 0:
                continue
            edge = any(owner[i + d] != owner[i] for d in (-1, 1, -w, w))
            c = color[i]
            if edge:
                c = (*INK, 235)
            elif c[:3] == INK and (xx + yy * 2) % 15 < 2:
                c = (*INK, max(150, c[3]))  # hatching
            out[xx, yy] = c
    if fx:
        layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        fx(ImageDraw.Draw(layer), width, height)
        drawing = Image.alpha_composite(drawing, layer)
    result = Image.alpha_composite(temple_backdrop(width, height), drawing).resize((width, height), Image.Resampling.LANCZOS)
    result.save(OUT / out_name)
    return result


def six_cuts(d, width, height):
    """The Sixfold Edict: six crescent cuts around him in mana ink."""
    cx, cy = width / 2 * SS, (height / 2 - 2) * SS
    for i in range(6):
        r = (22 + (i % 3) * 5) * SS
        start = -150 + i * 55
        d.arc((cx - r, cy - r * 0.7, cx + r, cy + r * 0.7), start, start + 38, fill=(*MANA_INK, 190), width=SS)


def render_spoils():
    image = Image.new("RGBA", (112, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    items = [(10, "architect_scimitar", "I"), (44, "commandment_tablet", "II"), (78, "architect_portrait", "III")]
    for x, name, numeral in items:
        sprite = Image.open(ASSETS / f"item/{name}.png").convert("RGBA")
        for y in range(16):
            for sx in range(16):
                r, g, b, a = sprite.getpixel((sx, y))
                if not a:
                    continue
                if g > r + 40 and b > r + 40:
                    ink = (*MANA_INK, 230)
                elif r > g + 60:
                    ink = (*CORAL_INK, 220)
                elif b > g + 20 and r > g:
                    ink = (*RUNE_INK, 220)
                else:
                    ink = (*INK, 110 + min(110, (r + g + b) // 5))
                sprite.putpixel((sx, y), ink)
        image.alpha_composite(sprite.resize((24, 24), Image.Resampling.NEAREST), (x, 12))
        draw.line((x + 2, 43, x + 22, 43), fill=(*INK, 115))
        draw.text((x + 10 - len(numeral) * 2, 46), numeral, fill=(*INK, 220))
    image.save(OUT / "architect_spoils.png")
    return image


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    plates = [
        render_plate("idle", 112, 80, -0.55, "architect_anatomy.png"),
        render_plate("a_cut3", 112, 72, -0.35, "architect_combat.png", fx=six_cuts),
        render_spoils(),
    ]
    review = Image.new("RGBA", (360, 110), (224, 204, 163, 255))
    for i, img in enumerate(plates):
        review.alpha_composite(img, (i * 120 + 4, (110 - img.height) // 2))
    out = ROOT / "build/architect-sketches/bestiary_review.png"
    review.resize((1080, 330), Image.Resampling.NEAREST).save(out)
    print("Generated 3 Architect bestiary plates (anatomy, combat, spoils).")


if __name__ == "__main__":
    main()
