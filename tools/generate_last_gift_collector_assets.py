#!/usr/bin/env python3
"""
Procedural pixel-art texture generator for the Collector of the Last Gift (`last_gift_collector`):
- Entity texture (`256x256`): `assets/codenamecoraline/textures/entity/last_gift_collector.png`
- Emissive layer (`256x256`): `assets/codenamecoraline/textures/entity/last_gift_collector_eyes.png`
- Sky sleigh texture (`128x128`): `assets/codenamecoraline/textures/entity/collector_sleigh.png`
- Item sprites (`16x16`):
  - `last_gift_collector_spawn_egg.png`
  - `silent_bell.png`
  - `cracked_bell.png`
  - `frostbound_thread.png`
  - `solstice_seal.png`
"""

from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ENTITY_DIR = ROOT / "src/main/resources/assets/codenamecoraline/textures/entity"
ITEM_DIR = ROOT / "src/main/resources/assets/codenamecoraline/textures/item"

# Palette from Section 4.2
WINE_LIGHT = (98, 40, 54, 255)
WINE_MAIN = (78, 31, 42, 255)       # #4E1F2A
WINE_DARK = (54, 21, 29, 255)       # #36151D
WINE_DEEP = (38, 14, 20, 255)

WOOD_LIGHT = (62, 50, 46, 255)
WOOD_MAIN = (43, 35, 32, 255)       # #2B2320
WOOD_DARK = (29, 24, 23, 255)       # #1D1817

FUR_HIGHLIGHT = (232, 238, 242, 255)
FUR_LIGHT = (215, 220, 224, 255)    # #D7DCE0
FUR_MID = (168, 178, 186, 255)      # #A8B2BA
FUR_SHADOW = (128, 138, 148, 255)

PORCELAIN_LIGHT = (228, 232, 236, 255)
PORCELAIN_MID = (196, 202, 210, 255)
PORCELAIN_DARK = (146, 154, 164, 255)

ICE_PALE = (191, 231, 250, 255)     # #BFE7FA
ICE_MID = (142, 200, 232, 255)      # #8EC8E8
ICE_DEEP = (86, 148, 188, 255)

BRONZE_LIGHT = (142, 127, 94, 255)  # #8E7F5E
BRONZE_MID = (111, 98, 72, 255)     # #6F6248
BRONZE_DARK = (76, 66, 48, 255)

IRON_LIGHT = (122, 132, 142, 255)
IRON_MID = (94, 102, 110, 255)      # #5E666E
IRON_DARK = (58, 64, 72, 255)

GLOW_BRIGHT = (139, 224, 255, 255)  # #8BE0FF
GLOW_CORE = (83, 184, 232, 255)     # #53B8E8
GLOW_DIM = (42, 116, 164, 255)


def hash_noise(x: int, y: int, seed: int = 0) -> int:
    n = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)
    return (n & 0x7FFFFFFF) % 100


def fill_box_faces(
    img: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    base_color: tuple[int, int, int, int],
    dark_color: tuple[int, int, int, int],
    light_color: tuple[int, int, int, int],
    seed: int = 1,
    vertical_stripes: bool = False,
) -> None:
    """Paint all 6 standard Minecraft box UV faces at (u, v) for dimensions (w, h, d)."""
    px = img.load()
    total_w = 2 * (d + w)
    total_h = d + h
    for dy in range(total_h):
        for dx in range(total_w):
            x = u + dx
            y = v + dy
            if x < 0 or y < 0 or x >= img.width or y >= img.height:
                continue
            # Top / bottom strips only span [d .. d + 2*w)
            if dy < d and (dx < d or dx >= d + 2 * w):
                continue
            n = hash_noise(x, y, seed)
            c = base_color
            if dy < d:
                # Top or bottom face
                c = light_color if dx < d + w else dark_color
            else:
                rel_y = dy - d
                if rel_y < 2:
                    c = light_color
                elif rel_y >= h - 2:
                    c = dark_color
                elif vertical_stripes and (dx % 4 == 0):
                    c = dark_color
                elif n < 22:
                    c = dark_color
                elif n > 82:
                    c = light_color
            px[x, y] = c


# Nicholas-the-Renegade palette: burgundy coat, cream/olive fur, blue-grey skin, cream beard, burlap sack.
RED_MAIN = (150, 26, 40, 255)
RED_DARK = (104, 16, 30, 255)
RED_LIGHT = (188, 48, 58, 255)
TRIM_MAIN = (226, 222, 196, 255)
TRIM_DARK = (166, 164, 132, 255)
TRIM_LIGHT = (246, 244, 226, 255)
SKIN_MAIN = (140, 164, 192, 255)
SKIN_DARK = (100, 122, 152, 255)
SKIN_LIGHT = (176, 198, 220, 255)
BEARD_MAIN = (216, 204, 164, 255)
BEARD_DARK = (158, 150, 112, 255)
BEARD_LIGHT = (240, 232, 198, 255)
LEATHER = (26, 22, 26, 255)
LEATHER_LIGHT = (52, 44, 46, 255)
GOLD = (214, 174, 60, 255)
BURLAP_MAIN = (150, 120, 78, 255)
BURLAP_DARK = (104, 80, 50, 255)
BURLAP_LIGHT = (186, 154, 104, 255)
EYE_GLOW = (255, 204, 70, 255)
EYE_HOT = (255, 120, 40, 255)
STEEL_MAIN = (150, 160, 170, 255)
STEEL_DARK = (84, 92, 104, 255)
STEEL_LIGHT = (214, 224, 232, 255)


def band(img, u, v, w, h, d, y0, y1, base, dark, light, seed=1):
    """Repaint rows y0..y1 (relative to the side faces) of a box with a fur/trim colour."""
    px = img.load()
    for dy in range(d + y0, d + y1 + 1):
        for dx in range(2 * (d + w)):
            x, y = u + dx, v + dy
            n = hash_noise(x, y, seed)
            px[x, y] = dark if n < 22 else (light if n > 78 else base)


def generate_collector_entity_textures() -> None:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    px = img.load()
    epx = eyes.load()

    for y in range(256):
        for x in range(256):
            n = hash_noise(x, y, 7)
            px[x, y] = RED_DARK if n < 40 else (LEATHER if n < 75 else RED_MAIN)

    # Pelvis (0, 0, 14, 8, 10): dark leather
    fill_box_faces(img, 0, 0, 14, 8, 10, LEATHER, LEATHER, LEATHER_LIGHT, seed=10)

    # Torso: burgundy coat, cream fur centre strip with buttons, fur hem
    fill_box_faces(img, 0, 20, 20, 16, 12, RED_MAIN, RED_DARK, RED_LIGHT, seed=20, vertical_stripes=True)
    band(img, 0, 20, 20, 16, 12, 13, 15, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=22)
    for yy in range(32, 45):
        for xx in range(20, 24):
            n = hash_noise(xx, yy, 23)
            px[xx, yy] = TRIM_DARK if n < 25 else (TRIM_LIGHT if n > 75 else TRIM_MAIN)
    for yy in (35, 39, 43):
        px[21, yy] = LEATHER
        px[22, yy] = LEATHER
    # Waist belt (68, 20, 16, 6, 10) with a gold buckle on the front face
    fill_box_faces(img, 68, 20, 16, 6, 10, LEATHER, LEATHER, LEATHER_LIGHT, seed=21)
    for yy in range(31, 35):
        for xx in range(84, 88):
            px[xx, yy] = GOLD if (xx in (84, 87) or yy in (31, 34)) else LEATHER

    # Head: long hair skull (sides/back cream, top burgundy) + blue-grey face
    fill_box_faces(img, 0, 56, 10, 12, 10, BEARD_MAIN, BEARD_DARK, RED_MAIN, seed=30, vertical_stripes=True)
    fill_box_faces(img, 44, 56, 10, 11, 2, SKIN_MAIN, SKIN_DARK, SKIN_LIGHT, seed=31)
    fx, fy = 46, 58  # front face origin

    def fp(x, y, c, e=None):
        px[fx + x, fy + y] = c
        if e:
            epx[fx + x, fy + y] = e

    for (x, y) in ((1, 2), (2, 2), (3, 3), (4, 3), (8, 2), (7, 2), (6, 3), (5, 3)):
        fp(x, y, (58, 42, 58, 255))
    for x in (2, 3, 6, 7):
        fp(x, 4, EYE_GLOW, EYE_GLOW)
        fp(x, 5, SKIN_DARK)
    fp(3, 4, EYE_HOT, EYE_HOT)
    fp(6, 4, EYE_HOT, EYE_HOT)
    for y in (5, 6):
        fp(4, y, SKIN_LIGHT)
        fp(5, y, SKIN_DARK)
    for x in range(10):
        fp(x, 6 if 3 <= x <= 6 else 7, BEARD_LIGHT if x % 2 else BEARD_MAIN)
    for x in (0, 1, 8, 9):
        fp(x, 8, BEARD_MAIN)
    for x in range(2, 8):
        fp(x, 8, (62, 12, 22, 255))
    for x in range(3, 7):
        fp(x, 8, (236, 230, 212, 255))
    for x in range(2, 8):
        fp(x, 9, (62, 12, 22, 255))
    fp(4, 9, (176, 56, 74, 255))
    fp(5, 9, (176, 56, 74, 255))
    for x in range(1, 9):
        fp(x, 10, BEARD_MAIN)

    # Santa hat: body (72,56), fur brim (198,56), drooping tip (112,56), pompom (112,70)
    fill_box_faces(img, 72, 56, 10, 7, 10, RED_MAIN, RED_DARK, RED_LIGHT, seed=32)
    fill_box_faces(img, 198, 56, 13, 3, 13, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=33)
    band(img, 198, 56, 13, 3, 13, 0, 2, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=34)
    fill_box_faces(img, 112, 56, 5, 8, 5, RED_MAIN, RED_DARK, RED_LIGHT, seed=35)
    fill_box_faces(img, 112, 70, 5, 5, 5, TRIM_LIGHT, TRIM_DARK, TRIM_MAIN, seed=36)

    # Huge beard: main (134,56,12,18,6) and tapering tail (172,56,8,12,4)
    fill_box_faces(img, 134, 56, 12, 18, 6, BEARD_MAIN, BEARD_DARK, BEARD_LIGHT, seed=37, vertical_stripes=True)
    fill_box_faces(img, 172, 56, 8, 12, 4, BEARD_MAIN, BEARD_DARK, BEARD_LIGHT, seed=38, vertical_stripes=True)

    # Long nose (176,73,3,4,5): blue-grey with a cold red tip on the front and underside
    fill_box_faces(img, 176, 73, 3, 4, 5, SKIN_MAIN, SKIN_DARK, SKIN_LIGHT, seed=39)
    for yy in (80, 81):
        for xx in range(181, 184):
            px[xx, yy] = (200, 96, 116, 255)
    for yy in range(73, 78):
        for xx in range(184, 187):
            px[xx, yy] = (200, 96, 116, 255)

    # Mantle: fur collar + burgundy back coat + side drapes with fur hems
    fill_box_faces(img, 0, 84, 40, 7, 14, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=40)
    fill_box_faces(img, 112, 84, 24, 28, 4, RED_MAIN, RED_DARK, RED_LIGHT, seed=41, vertical_stripes=True)
    band(img, 112, 84, 24, 28, 4, 24, 27, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=44)
    fill_box_faces(img, 172, 84, 4, 24, 10, RED_MAIN, RED_DARK, RED_LIGHT, seed=42, vertical_stripes=True)
    fill_box_faces(img, 204, 84, 4, 24, 10, RED_MAIN, RED_DARK, RED_LIGHT, seed=43, vertical_stripes=True)
    band(img, 172, 84, 4, 24, 10, 20, 23, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=45)
    band(img, 204, 84, 4, 24, 10, 20, 23, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=46)

    # The sack on his back (0,124,16,22,12) with a skull stitched on its outer face; flap (60,124,14,18,2)
    fill_box_faces(img, 0, 124, 16, 22, 12, BURLAP_MAIN, BURLAP_DARK, BURLAP_LIGHT, seed=50)
    fill_box_faces(img, 60, 124, 14, 18, 2, BURLAP_DARK, LEATHER, BURLAP_MAIN, seed=51)
    sx, sy = 44, 140
    for yy in range(0, 9):
        for xx in range(0, 8):
            edge = xx in (0, 7) or yy in (0, 8)
            if edge and not (xx in (0, 7) and yy in (0, 8)):
                px[sx + xx, sy + yy] = LEATHER_LIGHT
            else:
                px[sx + xx, sy + yy] = (214, 208, 190, 255)
    for (xx, yy) in ((2, 3), (3, 3), (4, 3), (5, 3)):
        px[sx + xx, sy + yy] = LEATHER
    epx[sx + 2, sy + 3] = EYE_GLOW
    epx[sx + 5, sy + 3] = EYE_GLOW
    for xx in range(2, 6):
        px[sx + xx, sy + 7] = LEATHER

    # Cracked bell core kept inside the sack (96,124,8,10,8) and clapper (132,124,4,5,4)
    fill_box_faces(img, 96, 124, 8, 10, 8, BRONZE_MID, BRONZE_DARK, BRONZE_LIGHT, seed=52)
    fill_box_faces(img, 132, 124, 4, 5, 4, EYE_GLOW, EYE_HOT, EYE_GLOW, seed=53)
    for by in range(128, 133):
        for bx in range(136, 144):
            epx[bx, by] = EYE_HOT

    # Coat lapels (156,124,18,12,4): burgundy with gold buttons
    fill_box_faces(img, 156, 124, 18, 12, 4, RED_DARK, RED_DARK, RED_MAIN, seed=54)
    for bx in range(160, 190, 4):
        px[bx, 144] = GOLD

    # Arms: burgundy sleeve with fur cuff; forearm red -> fur cuff -> blue-grey hand
    for (u, seed) in ((0, 60), (60, 62)):
        fill_box_faces(img, u, 164, 7, 14, 7, RED_MAIN, RED_DARK, TRIM_MAIN, seed=seed)
        band(img, u, 164, 7, 14, 7, 11, 13, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=seed + 5)
    for (u, seed) in ((32, 61), (92, 63)):
        fill_box_faces(img, u, 164, 6, 14, 6, RED_MAIN, RED_DARK, RED_LIGHT, seed=seed)
        band(img, u, 164, 6, 14, 6, 7, 9, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=seed + 5)
        band(img, u, 164, 6, 14, 6, 10, 13, SKIN_MAIN, SKIN_DARK, SKIN_LIGHT, seed=seed + 7)

    # The Renegade's Greataxe (same materials as the item): haft, wrap, pommel, collar, blade, edge, toe, horn, spike, runes
    fill_box_faces(img, 124, 164, 3, 54, 3, (110, 74, 44, 255), (72, 46, 30, 255), (150, 106, 64, 255), seed=70)
    fill_box_faces(img, 138, 164, 4, 11, 4, LEATHER, LEATHER, LEATHER_LIGHT, seed=71)
    fill_box_faces(img, 138, 180, 5, 5, 5, GOLD, (150, 110, 34, 255), (255, 226, 120, 255), seed=72)
    fill_box_faces(img, 138, 192, 5, 9, 5, GOLD, (150, 110, 34, 255), (255, 226, 120, 255), seed=73)
    fill_box_faces(img, 158, 164, 3, 20, 12, STEEL_MAIN, STEEL_DARK, STEEL_LIGHT, seed=74)
    fill_box_faces(img, 190, 164, 2, 16, 5, STEEL_LIGHT, STEEL_MAIN, (255, 255, 255, 255), seed=75)
    fill_box_faces(img, 206, 164, 2, 6, 7, STEEL_DARK, LEATHER, STEEL_MAIN, seed=76)
    fill_box_faces(img, 226, 164, 2, 4, 7, STEEL_DARK, LEATHER, STEEL_MAIN, seed=77)
    fill_box_faces(img, 158, 204, 2, 8, 8, STEEL_MAIN, STEEL_DARK, STEEL_LIGHT, seed=78)
    fill_box_faces(img, 180, 204, 2, 4, 4, STEEL_DARK, LEATHER, STEEL_MAIN, seed=79)
    for (ru, rv) in ((190, 190), (206, 190)):
        fill_box_faces(img, ru, rv, 1, 4, 4, (90, 190, 240, 255), (60, 140, 200, 255), (190, 240, 255, 255), seed=80)
        for yy in range(rv, rv + 8):
            for xx in range(ru, ru + 10):
                epx[xx, yy] = (120, 210, 255, 255)

    # Legs: burgundy trousers; boots black with fur cuffs
    for (u, seed) in ((0, 80), (60, 82)):
        fill_box_faces(img, u, 200, 6, 12, 6, RED_DARK, RED_DARK, RED_MAIN, seed=seed)
    for (u, seed) in ((28, 81), (88, 83)):
        fill_box_faces(img, u, 200, 7, 12, 8, LEATHER, LEATHER, LEATHER_LIGHT, seed=seed)
        band(img, u, 200, 7, 12, 8, 0, 3, TRIM_MAIN, TRIM_DARK, TRIM_LIGHT, seed=seed + 5)

    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    img.save(ENTITY_DIR / "last_gift_collector.png")
    eyes.save(ENTITY_DIR / "last_gift_collector_eyes.png")


def generate_sleigh_texture() -> None:
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    px = img.load()

    for y in range(128):
        for x in range(128):
            n = hash_noise(x, y, 99)
            px[x, y] = WOOD_MAIN if n < 50 else (WOOD_DARK if n < 85 else BRONZE_MID)

    # Timber deck & side walls
    fill_box_faces(img, 0, 0, 3, 12, 42, WOOD_MAIN, WOOD_DARK, BRONZE_MID, seed=101)
    fill_box_faces(img, 60, 0, 28, 22, 6, WINE_MAIN, WINE_DARK, BRONZE_LIGHT, seed=102)
    fill_box_faces(img, 60, 28, 26, 12, 4, WOOD_MAIN, WOOD_DARK, BRONZE_MID, seed=103)

    # Cold-blue lanterns (64, 48, 5, 8, 5)
    fill_box_faces(img, 64, 48, 5, 8, 5, GLOW_CORE, IRON_DARK, GLOW_BRIGHT, seed=104)

    # Spectral winter stags (64, 64, 8, 9, 20), (64, 94, 6, 12, 7), (92, 94, 12, 10, 4)
    fill_box_faces(img, 64, 64, 8, 9, 20, ICE_MID, ICE_DEEP, ICE_PALE, seed=105)
    fill_box_faces(img, 64, 94, 6, 12, 7, ICE_MID, ICE_DEEP, ICE_PALE, seed=106)
    fill_box_faces(img, 92, 94, 12, 10, 4, ICE_PALE, ICE_MID, GLOW_BRIGHT, seed=107)

    img.save(ENTITY_DIR / "collector_sleigh.png")


def put_pattern(img: Image.Image, pattern: list[str], palette: dict[str, tuple[int, int, int, int]]) -> None:
    px = img.load()
    for y, row in enumerate(pattern):
        for x, ch in enumerate(row):
            if ch != "." and ch in palette:
                px[x, y] = palette[ch]


def generate_item_textures() -> None:
    ITEM_DIR.mkdir(parents=True, exist_ok=True)

    # 1. last_gift_collector_spawn_egg.png (16x16)
    egg = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    egg_pattern = [
        "......DDDD......",
        "....DDFFFFDD....",
        "...DFFffffFFD...",
        "..DFffWWWWffFD..",
        "..DWwWBBBBWwWD..",
        ".DWWwBGGGGbwWWD.",
        ".DWWwBGGGGbwWWD.",
        ".DWWWwBBBBwWWWD.",
        ".DWWWWwwwwWWWWD.",
        ".DWWwBBBBBBwWWD.",
        ".DWWbGGGGGGbWWD.",
        "..DWwBBBBBBwWD..",
        "..DWWwwwwwwWWD..",
        "...DDWWWWWWDD...",
        "....DDDDDDDD....",
        "................",
    ]
    put_pattern(egg, egg_pattern, {
        "D": WINE_DEEP,
        "W": WINE_MAIN,
        "w": WINE_DARK,
        "F": FUR_MID,
        "f": FUR_LIGHT,
        "B": BRONZE_MID,
        "b": BRONZE_LIGHT,
        "G": GLOW_BRIGHT,
    })
    egg.save(ITEM_DIR / "last_gift_collector_spawn_egg.png")

    # 2. silent_bell.png (16x16)
    sbell = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    sbell_pattern = [
        "......IIII......",
        ".....IIiiII.....",
        "......IiiI......",
        "....DDBBBBDD....",
        "...DBBbbbbBBD...",
        "..DBbbIIIIbbBD..",
        "..DBbIiiiiIbBD..",
        "..DBbIiggiIbBD..",
        ".DDBbIiggiIbBDD.",
        ".DBbbIIIIIIbbBD.",
        ".DBbbbbbbbbbbBD.",
        "DDBBBBBBBBBBBBDD",
        "DIIIIIIIIIIIIIID",
        "....DDDGGDDD....",
        "......GGGG......",
        "................",
    ]
    put_pattern(sbell, sbell_pattern, {
        "D": WOOD_DARK,
        "B": BRONZE_DARK,
        "b": BRONZE_MID,
        "I": ICE_MID,
        "i": ICE_PALE,
        "G": GLOW_CORE,
        "g": GLOW_BRIGHT,
    })
    sbell.save(ITEM_DIR / "silent_bell.png")

    # 3. cracked_bell.png (16x16)
    cbell = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    cbell_pattern = [
        "......DDDD......",
        ".....DBbbBD.....",
        "......DBBD......",
        "....DDBbbBDD....",
        "...DBBbbbbBBD...",
        "..DBbbbGGbbbBD..",
        "..DBbbGggBbbBD..",
        "..DBbBGgBBbbBD..",
        ".DDBbGGbBBBBBDD.",
        ".DBbGggBBBBbbBD.",
        ".DBbGgBBBBbbbBD.",
        "DDBBGBBBBBBBBBDD",
        "DDDDGGDDDDDDDDDD",
        ".....GGGG.......",
        "......GG........",
        "................",
    ]
    put_pattern(cbell, cbell_pattern, {
        "D": WOOD_DARK,
        "B": BRONZE_MID,
        "b": BRONZE_LIGHT,
        "G": GLOW_CORE,
        "g": GLOW_BRIGHT,
    })
    cbell.save(ITEM_DIR / "cracked_bell.png")

    # 4. frostbound_thread.png (16x16)
    thread = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    thread_pattern = [
        ".....DDDDDD.....",
        "....DWWWWWWd....",
        "....DDDDDDDD....",
        "...DIIffffIID...",
        "..DIifWWWWfiID..",
        "..DifWWggWWfiD..",
        "..DifWgGGgWfiD..",
        "..DifWWggWWfiD..",
        "..DIifWWWWfiID..",
        "...DIIffffIID...",
        "....DDDDDDDD....",
        "....DWWWWWWd....",
        ".....DDDDDDii...",
        "...........iGi..",
        "............ii..",
        "................",
    ]
    put_pattern(thread, thread_pattern, {
        "D": WOOD_DARK,
        "d": WOOD_MAIN,
        "W": WINE_MAIN,
        "f": FUR_LIGHT,
        "I": ICE_MID,
        "i": ICE_PALE,
        "G": GLOW_BRIGHT,
        "g": GLOW_CORE,
    })
    thread.save(ITEM_DIR / "frostbound_thread.png")

    # 5. solstice_seal.png (16x16)
    seal = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    seal_pattern = [
        "....DDDDDDDD....",
        "..DDBBBBBBBBDD..",
        ".DBBWWWWWWWWBBD.",
        ".DBWWIIwwIIWWBD.",
        "DBWWIiiwwiiIWWBD",
        "DBWWiGGGGGGiWWBD",
        "DBWWwGggggGwWWBD",
        "DBWWwGggggGwWWBD",
        "DBWWiGGGGGGiWWBD",
        "DBWWIiiwwiiIWWBD",
        ".DBWWIIwwIIWWBD.",
        ".DBBWWWWWWWWBBD.",
        "..DDBBBBBBBBDD..",
        "....DDWWWWDD....",
        ".....DWWWWd.....",
        "................",
    ]
    put_pattern(seal, seal_pattern, {
        "D": WOOD_DARK,
        "d": WINE_DEEP,
        "B": BRONZE_LIGHT,
        "W": WINE_MAIN,
        "w": WINE_DARK,
        "I": ICE_MID,
        "i": ICE_PALE,
        "G": GLOW_CORE,
        "g": GLOW_BRIGHT,
    })
    seal.save(ITEM_DIR / "solstice_seal.png")


GIFT_RED = (190, 36, 52, 255)
GIFT_RED_DARK = (132, 20, 36, 255)
GIFT_RED_LIGHT = (226, 74, 84, 255)
GIFT_GREEN = (36, 122, 74, 255)
GIFT_GOLD = (230, 182, 40, 255)
GIFT_GOLD_DARK = (170, 128, 26, 255)
GIFT_GOLD_LIGHT = (255, 226, 110, 255)
INK = (24, 18, 22, 255)


def hline_band(img, u, v, w, h, d, x0, x1, base, dark, light, seed=1):
    """Paint a vertical ribbon (columns x0..x1 of every side face) through a box."""
    px = img.load()
    for dy in range(d, d + h):
        for dx in range(2 * (d + w)):
            if x0 <= (dx % (d + w)) <= x1:
                x, y = u + dx, v + dy
                n = hash_noise(x, y, seed)
                px[x, y] = dark if n < 20 else (light if n > 80 else base)


def generate_gift_mimic_texture() -> None:
    """64x64: body (0,0,12,9,12), lid (0,22,14,4,14), bow (0,42,6,3,6), legs (40,40,4,5,4)."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.load()
    fill_box_faces(img, 0, 0, 12, 9, 12, GIFT_RED, GIFT_RED_DARK, GIFT_RED_LIGHT, seed=201)
    hline_band(img, 0, 0, 12, 9, 12, 5, 6, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=202)
    # top face of the body (inside the "mouth"): dark red gullet with a row of teeth around it
    for y in range(0, 12):
        for x in range(12, 24):
            px[x, y] = (70, 8, 18, 255)
    for x in range(12, 24, 2):
        px[x, 0] = (240, 236, 220, 255)
        px[x, 11] = (240, 236, 220, 255)
    for y in range(1, 11, 2):
        px[12, y] = (240, 236, 220, 255)
        px[23, y] = (240, 236, 220, 255)
    # a lolling tongue on the front face (front = x 12..23, y 12..20)
    for x in range(16, 20):
        for y in range(15, 20):
            px[x, y] = (200, 70, 90, 255)
    fill_box_faces(img, 0, 22, 14, 4, 14, GIFT_GREEN, (20, 80, 48, 255), (70, 160, 104, 255), seed=203)
    hline_band(img, 0, 22, 14, 4, 14, 6, 7, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=204)
    # lid underside (bottom face) carries the upper teeth: bottom face is x 28..41, y 22..35
    for y in range(22, 36):
        for x in range(28, 42):
            px[x, y] = (70, 8, 18, 255)
    for x in range(28, 42, 2):
        px[x, 22] = (240, 236, 220, 255)
        px[x, 35] = (240, 236, 220, 255)
    # angry yellow eyes on the lid front (front face x 14..27, y 36..39)
    for (x0, x1) in ((16, 18), (23, 25)):
        for x in range(x0, x1 + 1):
            px[x, 37] = (255, 214, 70, 255)
            px[x, 38] = (255, 140, 40, 255)
    for x in (16, 17, 24, 25):
        px[x, 36] = INK
    fill_box_faces(img, 0, 42, 6, 3, 6, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=205)
    fill_box_faces(img, 40, 40, 4, 5, 4, GIFT_GREEN, (20, 80, 48, 255), (70, 160, 104, 255), seed=206)
    img.save(ENTITY_DIR / "gift_mimic.png")


def generate_collector_gift_texture() -> None:
    """128x128: base (0,0,20,14,20), lid (0,36,22,4,22), bow (0,66,12,4,6)."""
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    fill_box_faces(img, 0, 0, 20, 14, 20, GIFT_RED, GIFT_RED_DARK, GIFT_RED_LIGHT, seed=211)
    hline_band(img, 0, 0, 20, 14, 20, 8, 11, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=212)
    # snowflake dots on the paper
    px = img.load()
    for yy in range(22, 32, 5):
        for xx in range(2, 78, 7):
            if (xx // 7 + yy // 5) % 2 == 0:
                px[xx, yy] = (246, 244, 226, 255)
    fill_box_faces(img, 0, 36, 22, 4, 22, GIFT_GREEN, (20, 80, 48, 255), (70, 160, 104, 255), seed=213)
    hline_band(img, 0, 36, 22, 4, 22, 9, 12, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=214)
    fill_box_faces(img, 0, 66, 12, 4, 6, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=215)
    fill_box_faces(img, 40, 66, 6, 6, 6, GIFT_GOLD, GIFT_GOLD_DARK, GIFT_GOLD_LIGHT, seed=216)
    img.save(ENTITY_DIR / "collector_gift.png")


def generate_greataxe_and_portrait() -> None:
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    face = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    face_pattern = [
        "......RRRR.W....",
        "....RRRRRRRW....",
        "...RRRRRRRRR....",
        "..FFFFFFFFFFF...",
        "..FFFFFFFFFFF...",
        "..cKKKkkKKKc....",
        "..cKYKKKKYKc....",
        "..cKKKKNNKKc....",
        "..cKKKKNnKKc....",
        "..ccMMMMMMcc....",
        "...cMMMMMMc.....",
        "...cBMMMMBc.....",
        "....cBBBBc......",
        ".....cBBc.......",
        "......cc........",
        "................",
    ]
    put_pattern(face, face_pattern, {
        "R": GIFT_RED,
        "W": TRIM_LIGHT,
        "F": TRIM_MAIN,
        "c": BEARD_DARK,
        "K": SKIN_MAIN,
        "k": INK,
        "Y": EYE_GLOW,
        "N": SKIN_LIGHT,
        "n": SKIN_DARK,
        "M": BEARD_MAIN,
        "B": BEARD_LIGHT,
    })
    face.save(ITEM_DIR / "collector_portrait.png")


if __name__ == "__main__":
    generate_collector_entity_textures()
    generate_sleigh_texture()
    generate_item_textures()
    generate_gift_mimic_texture()
    generate_collector_gift_texture()
    generate_greataxe_and_portrait()
    print("Generated Last Gift Collector entity, sleigh, and item textures.")

