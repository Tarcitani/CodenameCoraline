"""Generate the Lithophage entity textures and item sprites (requires Pillow)."""
from pathlib import Path
from random import Random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
rng = Random(20261029)


def save(image: Image.Image, relative: str) -> None:
    path = ASSETS / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def sedimentary_patch(image: Image.Image, box: tuple[int, int, int, int], base: tuple[int, int, int],
                      variation: int = 8, strata: bool = True) -> None:
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        band = -5 if (strata and y % 4 == 0) else (4 if (strata and y % 5 == 2) else 0)
        for x in range(x0, x1):
            delta = band + rng.randrange(-variation, variation + 1)
            color = tuple(max(0, min(255, c + delta)) for c in base) + (255,)
            image.putpixel((x, y), color)


# 1. Entity base texture: dark hardened clay (#34383A), sedimentary bands, cracks and amber crystals (#D4A64B).
skin = Image.new("RGBA", (128, 128), (52, 56, 58, 255))
sedimentary_patch(skin, (0, 0, 76, 108), (52, 56, 58), variation=7, strata=True)
draw = ImageDraw.Draw(skin)

# Darker slate-clay shading on lower torso, joints and lower shins.
sedimentary_patch(skin, (0, 16, 30, 27), (43, 47, 49), variation=6)
sedimentary_patch(skin, (20, 76, 40, 102), (40, 44, 46), variation=6)

# Asymmetric right shoulder shelf and forearm mineral ridge: tougher slate crust.
sedimentary_patch(skin, (0, 44, 23, 55), (62, 67, 70), variation=7)
sedimentary_patch(skin, (62, 44, 72, 54), (168, 128, 56), variation=10, strata=False)

# Cracks and fissures across torso, shoulders and arms.
for cx, cy, length in [
    (6, 4, 5), (14, 6, 6), (22, 5, 4), (8, 19, 4),
    (5, 47, 4), (28, 48, 5), (46, 47, 6), (24, 63, 5),
    (42, 63, 5), (6, 79, 4), (24, 80, 4), (6, 93, 4),
]:
    for step in range(length):
        px = cx + (step % 2)
        py = cy + step
        skin.putpixel((px, py), (26, 29, 31, 255))

# Recessed chest amber fissure plate at texOffs(40, 0), size (6, 4, 1).
sedimentary_patch(skin, (40, 0, 54, 5), (36, 39, 41), variation=4, strata=False)
for x in range(42, 46):
    skin.putpixel((x, 2), (212, 166, 75, 255))
    skin.putpixel((x, 3), (164, 118, 42, 255))
skin.putpixel((43, 2), (240, 202, 112, 255))

# Weathered clay mask head: skull at (0, 28) and carved mask front plate at (28, 28).
sedimentary_patch(skin, (0, 28, 27, 43), (48, 52, 54), variation=6)
sedimentary_patch(skin, (28, 28, 46, 36), (64, 68, 70), variation=5, strata=False)
# Mask border bevel and weathered vertical fracture (no human mouth or nose).
draw.rectangle((29, 29, 36, 35), outline=(38, 42, 44, 255))
draw.line((32, 29, 32, 35), fill=(30, 33, 35, 255))
draw.line((30, 34, 35, 34), fill=(34, 37, 39, 255))
# Narrow recessed eye fissures with discreet amber mineral glow.
for fx in [30, 31, 34, 35]:
    skin.putpixel((fx, 31), (212, 166, 75, 255))
    skin.putpixel((fx, 32), (96, 70, 28, 255))
skin.putpixel((31, 31), (238, 198, 104, 255))
skin.putpixel((34, 31), (238, 198, 104, 255))

# Crown mineral ridges at (48, 28) and (64, 28).
sedimentary_patch(skin, (48, 28, 78, 35), (72, 76, 78), variation=7, strata=False)
for px, py in [(51, 30), (54, 31), (67, 30)]:
    skin.putpixel((px, py), (212, 166, 75, 255))

# Mineral crystal groups: right shoulder (80, 0), left shoulder (80, 10), dorsal (80, 20), held shard (100, 0).
for box in [(80, 0, 98, 9), (80, 10, 90, 17), (80, 20, 112, 30)]:
    sedimentary_patch(skin, box, (176, 134, 58), variation=14, strata=False)
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            if (x + y) % 5 == 0:
                skin.putpixel((x, y), (230, 188, 92, 255))
            elif (x * 3 + y) % 7 == 0:
                skin.putpixel((x, y), (112, 82, 34, 255))

sedimentary_patch(skin, (100, 0, 114, 9), (212, 166, 75), variation=12, strata=False)
for y in range(1, 7):
    for x in range(102, 110):
        if (x + y) % 3 == 0:
            skin.putpixel((x, y), (245, 212, 122, 255))

save(skin, "textures/entity/lithophage.png")

# 2. Emissive texture: discreet amber glow on mask fissures, chest core, and crystal veins.
eyes = Image.new("RGBA", skin.size, (0, 0, 0, 0))
for fx, fy, col in [
    (30, 31, (190, 142, 54, 255)),
    (31, 31, (232, 188, 88, 255)),
    (34, 31, (232, 188, 88, 255)),
    (35, 31, (190, 142, 54, 255)),
    (42, 2, (165, 120, 44, 255)),
    (43, 2, (212, 166, 75, 255)),
    (44, 2, (212, 166, 75, 255)),
    (45, 2, (165, 120, 44, 255)),
]:
    eyes.putpixel((fx, fy), col)

for box in [(80, 0, 98, 9), (80, 10, 90, 17), (80, 20, 112, 30), (100, 0, 114, 9)]:
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            if (x + y * 2) % 6 == 0:
                eyes.putpixel((x, y), (185, 140, 56, 255))

save(eyes, "textures/entity/lithophage_eyes.png")


# 3. Item sprites (16x16 pixel art).
def make_item(name: str, painter) -> None:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    painter(img, ImageDraw.Draw(img))
    save(img, f"textures/item/{name}.png")


def paint_spawn_egg(img: Image.Image, d: ImageDraw.Draw) -> None:
    # Ovo de Argila Ancestral: slate-grey base (#34383A) with mineral amber veins/spots (#D4A64B).
    d.polygon([(6, 1), (9, 1), (12, 4), (14, 9), (13, 13), (10, 15), (5, 15), (2, 13), (1, 9), (3, 4)], fill="#232628")
    d.polygon([(6, 2), (9, 2), (11, 5), (13, 9), (12, 12), (10, 14), (5, 14), (3, 12), (2, 9), (4, 5)], fill="#34383A")
    d.polygon([(6, 2), (8, 2), (10, 4), (9, 7), (5, 7), (4, 5)], fill="#454A4D")
    # Dark clay fracture lines
    d.line((5, 4, 7, 8), fill="#26292B")
    d.line((7, 8, 6, 12), fill="#26292B")
    d.line((9, 6, 11, 10), fill="#26292B")
    # Mineral amber spots (#D4A64B) with bright glint (#F0C96B) and deep border (#9A7026)
    for sx, sy in [(4, 5), (9, 4), (7, 8), (4, 11), (10, 10)]:
        d.rectangle((sx, sy, sx + 1, sy + 1), fill="#D4A64B")
        img.putpixel((sx, sy), (240, 201, 107, 255))
        if sy + 2 < 15:
            img.putpixel((sx + 1, sy + 1), (154, 112, 38, 255))


def paint_ancient_clay_fragment(img: Image.Image, d: ImageDraw.Draw) -> None:
    d.polygon([(4, 2), (11, 2), (14, 6), (13, 12), (9, 14), (3, 13), (2, 7)], fill="#232628")
    d.polygon([(5, 3), (10, 3), (13, 6), (12, 11), (9, 13), (4, 12), (3, 7)], fill="#34383A")
    d.polygon([(5, 3), (10, 3), (11, 6), (7, 7), (4, 6)], fill="#484E52")
    # Sedimentary crack and small amber trace
    d.line((5, 6, 8, 8, 10, 11), fill="#1E2022")
    d.line((6, 9, 8, 10), fill="#D4A64B")
    img.putpixel((7, 9), (240, 201, 107, 255))


def paint_mineral_amber_core(img: Image.Image, d: ImageDraw.Draw) -> None:
    # Dark clay matrix surrounding a bright faceted amber core
    d.polygon([(5, 1), (10, 1), (14, 5), (14, 11), (10, 14), (5, 14), (2, 11), (2, 5)], fill="#25282A")
    d.polygon([(6, 2), (9, 2), (13, 5), (13, 10), (9, 13), (6, 13), (3, 10), (3, 5)], fill="#34383A")
    d.polygon([(6, 3), (10, 3), (12, 6), (11, 11), (8, 12), (4, 10), (4, 6)], fill="#9E7024")
    d.polygon([(6, 4), (9, 4), (11, 6), (10, 10), (8, 11), (5, 9), (5, 6)], fill="#D4A64B")
    d.polygon([(6, 5), (8, 5), (9, 7), (7, 8), (6, 7)], fill="#F3D27A")
    img.putpixel((6, 5), (255, 240, 184, 255))


def paint_amber_shard(img: Image.Image, d: ImageDraw.Draw) -> None:
    d.polygon([(8, 1), (12, 5), (11, 11), (7, 14), (4, 10), (5, 4)], fill="#8E631E")
    d.polygon([(8, 2), (11, 5), (10, 10), (7, 13), (5, 9), (6, 4)], fill="#D4A64B")
    d.polygon([(8, 3), (10, 5), (8, 9), (6, 6)], fill="#F5DA85")
    img.putpixel((8, 4), (255, 244, 196, 255))


make_item("lithophage_spawn_egg", paint_spawn_egg)
make_item("ancient_clay_fragment", paint_ancient_clay_fragment)
make_item("mineral_amber_core", paint_mineral_amber_core)
make_item("amber_shard", paint_amber_shard)

print("Generated Lithophage entity textures (128x128) and 4 item sprites (16x16).")

