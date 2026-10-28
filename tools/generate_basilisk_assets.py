"""Rebuild the code-native Basilisk pixel textures (requires Pillow). No game assets are copied."""
from pathlib import Path
from random import Random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
rng = Random(1729)


def save(image, relative):
    path = ASSETS / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def patch(image, box, base, variation=10):
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            delta = rng.randrange(-variation, variation + 1)
            image.putpixel((x, y), tuple(max(0, min(255, c + delta)) for c in base) + (255,))


skin = Image.new("RGBA", (128, 128), (44, 51, 43, 255))
patch(skin, (0, 0, 64, 96), (53, 62, 49), 13)
d = ImageDraw.Draw(skin)
for _ in range(210):
    x, y = rng.randrange(62), rng.randrange(94)
    d.line((x, y, x + 1, y), fill=(31, 38, 32, 255))
patch(skin, (40, 24, 64, 46), (113, 111, 70), 12)
patch(skin, (0, 62, 12, 68), (166, 156, 109), 8)
patch(skin, (64, 0, 94, 24), (167, 150, 76), 12)
# The false eyes have a cadaverous golden sclera with fine green-grey veins.
for _ in range(30):
    x, y = rng.randrange(64, 92), rng.randrange(22)
    d.line((x, y, x + 2, y + 1), fill=(103, 110, 72, 255))
# Five-pixel pupil plate: an eclipsed iris; this UV is used only on the fronts.
for y in range(5):
    for x in range(5):
        distance = (x - 2) ** 2 + (y - 2) ** 2
        skin.putpixel((96 + x, y), (29, 32, 25, 255) if distance <= 2 else (212, 175, 55, 255))
skin.putpixel((97, 1), (235, 218, 138, 255))
patch(skin, (110, 16, 114, 19), (207, 180, 71), 4)
# Thin membrane UVs. Irregular transparent pixels make the edges ragged.
for box in [(40, 48, 54, 54), (48, 48, 62, 60), (48, 60, 64, 72)]:
    patch(skin, box, (92, 104, 64), 15)
    for x in range(box[0], box[2]):
        for y in range(box[1], box[3]):
            if (x + 2 * y) % 7 == 0:
                skin.putpixel((x, y), (38, 47, 36, 255))
            if y == box[1] and x % 3 == 0:
                skin.putpixel((x, y), (0, 0, 0, 0))
save(skin, "textures/entity/basilisk.png")
eyes = Image.new("RGBA", skin.size)
for x in range(5):
    for y in range(5):
        color = skin.getpixel((96 + x, y))
        if sum(color[:3]) > 200:
            eyes.putpixel((96 + x, y), tuple(int(v * 0.65) for v in color[:3]) + (255,))
for x in range(110, 114):
    for y in range(16, 19):
        eyes.putpixel((x, y), (120, 100, 28, 255))
save(eyes, "textures/entity/basilisk_eyes.png")


def item(name, draw):
    image = Image.new("RGBA", (16, 16))
    draw(ImageDraw.Draw(image))
    save(image, f"textures/item/{name}.png")


def egg(d):
    d.polygon([(6, 1), (9, 1), (12, 4), (14, 9), (13, 13), (10, 15), (5, 15), (2, 13), (1, 9), (3, 4)], fill="#151B18")
    d.polygon([(6, 2), (9, 2), (11, 5), (13, 9), (12, 12), (10, 14), (5, 14), (3, 12), (2, 9), (4, 5)], fill="#262B28")
    for box in [(4, 4, 7, 7), (9, 7, 12, 10), (4, 10, 7, 13), (8, 2, 9, 3)]:
        d.ellipse(box, fill="#C59B27")
    d.point((5, 5), fill="#EDCF69")
    d.line((3, 8, 3, 10), fill="#455146")


def lily(d):
    d.line((7, 14, 8, 7), fill="#526A4E", width=2)
    d.polygon([(8, 11), (3, 8), (4, 12), (7, 13)], fill="#7D956A")
    d.polygon([(8, 8), (3, 5), (4, 2), (7, 5), (8, 1), (10, 4), (13, 3), (12, 7)], fill="#9CA796")
    d.polygon([(5, 5), (4, 3), (8, 6), (8, 2), (10, 6), (12, 4), (11, 7), (8, 8)], fill="#DBDFC3")
    d.line((7, 6, 10, 6), fill="#C6AE56")


def butterfly(d):
    d.polygon([(7, 6), (2, 2), (1, 5), (3, 9), (2, 12), (5, 13), (7, 10)], fill="#603B29")
    d.polygon([(9, 6), (14, 2), (15, 5), (12, 9), (13, 12), (10, 13), (9, 10)], fill="#603B29")
    d.polygon([(6, 6), (3, 4), (3, 7), (6, 9), (4, 10), (5, 11), (7, 9)], fill="#BE6C37")
    d.polygon([(10, 6), (13, 4), (13, 7), (10, 9), (12, 10), (11, 11), (9, 9)], fill="#BE6C37")
    d.line((8, 5, 8, 12), fill="#272C23", width=2)
    d.line((7, 5, 6, 3), fill="#C1B35E")
    d.line((9, 5, 10, 3), fill="#C1B35E")
    for p in [(4, 6), (12, 6), (5, 10), (11, 10)]:
        d.point(p, fill="#D7BC67")


def cyst(d):
    d.polygon([(5, 2), (10, 2), (13, 5), (14, 10), (11, 14), (4, 13), (2, 9), (2, 5)], fill="#3B3C36")
    d.ellipse((3, 3, 12, 12), fill="#8D8956")
    d.ellipse((4, 4, 11, 11), fill="#D4AF37")
    d.ellipse((6, 5, 10, 10), fill="#1E241B")
    d.point((5, 5), fill="#F0DB98")
    d.line((4, 13, 3, 15), fill="#6D7350")
    d.line((11, 13, 12, 15), fill="#6D7350")


def flask(d):
    d.rectangle((6, 1, 9, 3), fill="#806332")
    d.rectangle((6, 4, 9, 6), fill="#81968A")
    d.polygon([(6, 6), (3, 9), (3, 13), (5, 15), (11, 15), (13, 13), (13, 9), (9, 6)], fill="#859B88")
    d.polygon([(6, 8), (4, 10), (4, 13), (6, 14), (10, 14), (12, 12), (11, 9)], fill="#202920")
    d.line((4, 10, 4, 12), fill="#C9D1B3")
    for p in [(7, 10), (10, 12), (6, 13), (9, 9)]:
        d.point(p, fill="#C59B27")


def ward(d):
    d.arc((3, 0, 12, 11), 175, 365, fill="#9A813E", width=2)
    d.line((3, 5, 5, 10), fill="#C9AC58")
    d.line((12, 5, 10, 10), fill="#C9AC58")
    d.polygon([(8, 7), (12, 10), (12, 13), (8, 15), (4, 13), (4, 10)], fill="#3B4838")
    d.ellipse((5, 9, 11, 14), fill="#BDA24A")
    d.ellipse((7, 10, 10, 13), fill="#262C23")
    d.point((6, 10), fill="#E3CF7D")


def leg(d):
    d.polygon([(3, 1), (7, 1), (10, 4), (10, 7), (7, 10), (10, 12), (13, 12),
               (15, 14), (12, 15), (8, 14), (4, 11), (4, 8), (6, 5), (2, 4)], fill="#222B23")
    d.polygon([(4, 2), (7, 2), (9, 4), (9, 6), (6, 9), (6, 11), (10, 13),
               (12, 13), (12, 14), (8, 13), (5, 10), (5, 8), (7, 5), (3, 4)], fill="#687955")
    d.line((5, 3, 7, 4, 7, 6, 5, 9), fill="#93A17A")
    d.rectangle((3, 1, 5, 2), fill="#AA7771")
    d.point((4, 1), fill="#E0CFAB")
    for point in [(11, 14), (13, 14), (14, 13)]:
        d.point(point, fill="#D9CEA3")


def tail(d):
    d.polygon([(1, 2), (5, 1), (9, 3), (11, 6), (11, 9), (9, 12), (13, 12), (15, 10),
               (15, 13), (12, 15), (7, 14), (5, 11), (7, 8), (7, 6), (4, 5), (1, 5)], fill="#222B23")
    d.polygon([(2, 2), (5, 2), (8, 4), (10, 6), (10, 9), (8, 12), (9, 13), (12, 13),
               (14, 12), (12, 14), (8, 13), (6, 11), (8, 8), (8, 5), (5, 4), (2, 4)], fill="#687955")
    d.line((4, 2, 7, 4, 9, 6, 9, 9, 7, 11), fill="#93A17A")
    d.polygon([(8, 3), (11, 2), (10, 5), (13, 5), (11, 8), (13, 9), (10, 11)], fill="#9AA16C")
    d.line((1, 2, 1, 4), fill="#AA7771")
    d.point((1, 3), fill="#E0CFAB")


for name, draw in [("basilisk_spawn_egg", egg), ("catacomb_lily", lily), ("rotten_butterfly", butterfly),
                   ("basilisk_eye_cyst", cyst), ("mist_flask", flask), ("basilisk_ward", ward),
                   ("basilisk_leg", leg), ("basilisk_eye", cyst), ("basilisk_tail", tail)]:
    item(name, draw)

icon = Image.new("RGBA", (18, 18))
d = ImageDraw.Draw(icon)
d.polygon([(3, 2), (9, 0), (15, 2), (15, 10), (12, 14), (9, 17), (6, 14), (3, 10)], fill="#344132")
d.polygon([(5, 3), (9, 2), (13, 3), (13, 10), (9, 14), (5, 10)], fill="#A5B66D")
d.line((6, 8, 8, 10, 12, 5), fill="#263127", width=2)
save(icon, "textures/mob_effect/wither_resistance.png")

print("Generated 2 entity textures, 9 item sprites and 1 effect icon.")
