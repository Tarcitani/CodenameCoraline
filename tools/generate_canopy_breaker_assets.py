#!/usr/bin/env python3
"""
Single source of truth for the Canopy Breaker's model boxes and textures.

Defines every cube of the model (part, offset, size, material), shelf-packs their UV footprints into a 256x256 atlas
(guaranteeing there is no overlap), paints the atlas, and writes:

  * assets/codenamecoraline/textures/entity/canopy_breaker.png          (256x256 colour)
  * assets/codenamecoraline/textures/entity/canopy_breaker_eyes.png     (256x256 emissive eyes)
  * src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerParts.java  (generated cube factory used by the model)
  * item icons: hollow_trunk_idol, deadroot_club, canopy_breaker_spawn_egg, canopy_breaker_portrait, canopy_resin,
    heartwood_splinter (16x16)
  * the 3D held model of the Deadroot Club (models/item/deadroot_club_3d.json + swatch texture)

Run:  python tools/generate_canopy_breaker_assets.py
"""
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
ENTITY_DIR = ASSETS / "textures/entity"
ITEM_DIR = ASSETS / "textures/item"
MODEL_DIR = ASSETS / "models/item"
JAVA_OUT = ROOT / "src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerParts.java"
ATLAS = 256

# ---------------------------------------------------------------------------------------------------------------
# Palette
FUR = (124, 74, 48, 255)
FUR_D = (84, 48, 34, 255)
FUR_L = (162, 100, 62, 255)
FUR_LL = (190, 126, 82, 255)
SKIN = (66, 48, 46, 255)
SKIN_D = (44, 32, 32, 255)
SKIN_L = (96, 70, 64, 255)
SCAR = (150, 112, 96, 255)
MOSS = (72, 100, 54, 255)
MOSS_D = (46, 70, 38, 255)
MOSS_L = (104, 138, 74, 255)
BARK = (92, 72, 58, 255)
BARK_D = (58, 44, 36, 255)
WOOD = (104, 70, 44, 255)
WOOD_D = (64, 42, 28, 255)
WOOD_L = (146, 102, 64, 255)
WOOD_DD = (38, 24, 18, 255)
BONE = (214, 200, 166, 255)
EYE = (255, 196, 64, 255)
EYE_H = (255, 236, 150, 255)
OUT = (22, 12, 14, 255)


def noise(x, y, seed=0):
    n = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)
    return (n & 0x7FFFFFFF) % 100


# ---------------------------------------------------------------------------------------------------------------
# Boxes: (name, part, x, y, z, w, h, d, material)
# Units are model pixels; ground is y=24 in model space (renderer scales the whole model by SCALE, see the model class).
BOXES = [
    ("pelvis", "pelvis", -6, -3, -4, 12, 6, 8, "fur"),
    ("chest", "chest", -12, -15, -7, 24, 15, 14, "fur"),
    ("mantle", "chest", -14, -17, -8, 28, 5, 16, "mantle"),
    ("belly", "chest", -7, -9, -7.5, 14, 8, 1, "skin_chest"),
    ("moss_back", "chest", -9, -16, 6, 8, 3, 6, "moss"),
    ("moss_shoulder", "chest", 5, -18, -3, 7, 3, 7, "moss"),
    ("bark_back", "chest", 1, -12, 7, 8, 7, 2, "bark"),
    ("head", "head", -5.5, -9, -5.5, 11, 9, 11, "head"),
    ("brow", "head", -6, -6, -8, 12, 3, 3, "fur_dark"),
    ("snout", "head", -3.5, -4.5, -9, 7, 4.5, 4, "snout"),
    ("ear_l", "head", 5.5, -6.5, -1, 2, 3, 3, "skin"),
    ("ear_r", "head", -7.5, -6.5, -1, 2, 3, 3, "skin"),
    ("crest", "head", -2, -11, -4, 4, 2, 9, "fur_dark"),
    ("jaw", "jaw", -3.5, 0, -6.5, 7, 3, 7, "jaw"),
    ("l_upper", "left_upper_arm", -3.5, 0, -3.5, 7, 11, 7, "fur"),
    ("l_fore", "left_forearm", -3, 0, -3, 6, 11, 6, "fur"),
    ("l_hand", "left_hand", -3.5, 0, -3.5, 7, 5, 7, "skin"),
    ("l_fingers", "left_hand", -3, 5, -3.5, 6, 3, 7, "fingers"),
    ("r_upper", "right_upper_arm", -3.5, 0, -3.5, 7, 11, 7, "fur"),
    ("r_fore", "right_forearm", -3, 0, -3, 6, 11, 6, "fur"),
    ("r_hand", "right_hand", -3.5, 0, -3.5, 7, 5, 7, "skin"),
    ("r_fingers", "right_hand", -3, 5, -3.5, 6, 3, 7, "fingers"),
    ("l_thigh", "left_thigh", -3.5, 0, -3.5, 7, 8, 7, "fur"),
    ("l_shin", "left_shin", -3, 0, -3, 6, 6, 6, "fur"),
    ("l_foot", "left_foot", -3.5, 0, -7, 7, 2, 10, "skin_foot"),
    ("r_thigh", "right_thigh", -3.5, 0, -3.5, 7, 8, 7, "fur"),
    ("r_shin", "right_shin", -3, 0, -3, 6, 6, 6, "fur"),
    ("r_foot", "right_foot", -3.5, 0, -7, 7, 2, 10, "skin_foot"),
    # club in the hand (shaft + head + knots) and stowed on the back
    # The club points along -y from the hand (a pose's club angle is the direction of its head). The hand grips the butt
    # end of the handle (wrapped in bark); the long handle and the head extend AWAY from the body, as a gorilla holds a branch.
    ("club_grip", "club_hand", -2.5, -3, -2.5, 5, 6, 5, "bark"),
    ("club_shaft", "club_hand", -2, -12, -2, 4, 16, 4, "club"),
    ("club_head", "club_hand", -4.5, -26, -4.5, 9, 14, 9, "club_head"),
    ("club_knot_a", "club_hand", -6.5, -23, -1.5, 2, 3, 3, "club"),
    ("club_knot_b", "club_hand", 4.5, -18, -2, 2, 3, 4, "club"),
    ("club_root_a", "club_hand", -1, -30, -1, 2, 4, 2, "club"),
    ("club_root_b", "club_hand", -4.5, -28, 2, 2, 3, 2, "club"),
    # the same club stowed on the back
    ("back_grip", "club_back", -2.5, -3, -2.5, 5, 6, 5, "bark"),
    ("back_shaft", "club_back", -2, -12, -2, 4, 16, 4, "club"),
    ("back_head", "club_back", -4.5, -26, -4.5, 9, 14, 9, "club_head"),
    ("back_knot", "club_back", -6.5, -23, -1.5, 2, 3, 3, "club"),
]


def ceil(v):
    return int(-(-v // 1))


def footprint(w, h, d):
    return 2 * (ceil(d) + ceil(w)), ceil(d) + ceil(h)


def pack():
    """Shelf packing, tallest first; returns {name: (u, v)}."""
    items = sorted(BOXES, key=lambda b: -footprint(b[5], b[6], b[7])[1])
    layout = {}
    x = y = row_h = 0
    for name, part, bx, by, bz, w, h, d, mat in items:
        fw, fh = footprint(w, h, d)
        if x + fw > ATLAS:
            x = 0
            y += row_h
            row_h = 0
        if y + fh > ATLAS:
            raise SystemExit(f"atlas overflow at {name}")
        layout[name] = (x, y)
        x += fw
        row_h = max(row_h, fh)
    return layout


# ---------------------------------------------------------------------------------------------------------------
def region_pixels(u, v, w, h, d):
    """Yield (face, x, y, fx, fy, fw, fh): atlas pixel and the face-local position for every pixel of a box footprint."""
    w, h, d = ceil(w), ceil(h), ceil(d)
    faces = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }
    for name, (fx0, fy0, fw, fh) in faces.items():
        for j in range(fh):
            for i in range(fw):
                yield name, fx0 + i, fy0 + j, i, j, fw, fh


def j_in_face(face, i, j, fw, fh):
    return j / max(1, fh - 1) if face not in ('top', 'bottom') else 0.4


def paint_box(img, eyes, box, u, v):
    name, part, bx, by, bz, w, h, d, mat = box
    px = img.load()
    seed = sum(ord(c) for c in name)
    for face, x, y, i, j, fw, fh in region_pixels(u, v, w, h, d):
        n = noise(x, y, seed)
        c = None
        if mat in ("fur", "fur_dark", "mantle", "skin_chest", "fingers", "head", "snout"):
            tones = [FUR_LL, FUR_L, FUR, FUR_D, (58, 34, 26, 255)]
            shift = 0
            if mat == "fur_dark":
                shift = 2
            if mat == "mantle":
                shift = -1
            if mat in ("skin_chest", "fingers", "snout"):
                tones = [(120, 92, 80, 255), SKIN_L, SKIN, SKIN_D, (30, 22, 22, 255)]
                shift = 0
            t = j_in_face(face, i, j, fw, fh)
            idx = 0 if t < 0.22 else 1 if t < 0.45 else 2 if t < 0.78 else 3
            idx += shift
            if n < 11:
                idx += 1
            elif n > 90:
                idx -= 1
            if mat in ("fur", "mantle") and (i * 5 + j // 2) % 6 == 0:
                idx += 1  # vertical fur strands
            if face == "bottom":
                idx = 3
            if face == "top":
                idx = 0 if mat in ("fur", "mantle") else idx
            c = tones[max(0, min(4, idx))]
        elif mat in ("skin", "skin_foot", "jaw"):
            c = SKIN_D if n < 25 else SKIN_L if n > 80 else SKIN
            if mat == "skin_foot" and face == "bottom":
                c = SKIN_D
        elif mat == "moss":
            c = MOSS_D if n < 30 else MOSS_L if n > 70 else MOSS
        elif mat == "bark":
            c = BARK_D if (n < 35 or (i + j) % 5 == 0) else BARK
        elif mat in ("club", "club_head"):
            c = WOOD_D if n < 28 else WOOD_L if n > 78 else WOOD
            if face in ("front", "back", "left", "right") and (i * 2 + j) % 9 == 0:
                c = WOOD_D  # grain lines
            if mat == "club_head":
                if j == 0 or j == fh - 1:
                    c = WOOD_D
        px[x, y] = c

    # ---- per-box details ------------------------------------------------------------------------------------
    def put(face_name, fi, fj, color, ecolor=None):
        for face, x, y, i, j, fw, fh in region_pixels(u, v, w, h, d):
            if face == face_name and i == fi and j == fj:
                px[x, y] = color
                if ecolor and eyes is not None:
                    eyes.load()[x, y] = ecolor
                return

    wi, hi, di = ceil(w), ceil(h), ceil(d)
    if name == "head":
        # face: heavy brow shadow, glowing amber eyes, flared nostril line on the front face
        for i in range(wi):
            put("front", i, 1, SKIN_D)
            put("front", i, 2, SKIN_D)
        for ex in (2, wi - 4):
            for dx in range(2):
                put("front", ex + dx, 3, EYE, EYE)
                put("front", ex + dx, 4, EYE_H, EYE_H)
        for i in range(3, wi - 3):
            put("front", i, hi - 2, SKIN_L)
    if name == "snout":
        for i in range(wi):
            put("front", i, 0, SKIN_L)
            put("front", i, 1, SKIN)
        put("front", 1, 2, SKIN_D)
        put("front", wi - 2, 2, SKIN_D)
    if name == "jaw":
        for i in range(1, wi - 1, 2):
            put("front", i, 0, BONE)
    if name in ("chest", "l_upper", "r_fore"):
        # old scars
        for k in range(4):
            put("front", 3 + k, 3 + k, SCAR)
            put("front", 8 + k, 4 + k, SCAR)
    if name in ("l_fingers", "r_fingers"):
        for i in range(0, wi, 2):
            put("front", i, 0, SKIN_D)
            put("front", i, 2, SKIN_L)
    if name == "club_head":
        # impact marks and a crack
        for k in range(5):
            put("front", 2 + k, 3 + k, WOOD_DD)
            put("left", 2 + (k % 3), 8 + k // 2, WOOD_DD)
        for k in range(4):
            put("back", 4, 2 + k * 2, WOOD_DD)


def build_images(layout):
    img = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
    for box in BOXES:
        u, v = layout[box[0]]
        paint_box(img, eyes, box, u, v)
    return img, eyes


# ---------------------------------------------------------------------------------------------------------------
def write_java(layout):
    parts = {}
    for name, part, x, y, z, w, h, d, mat in BOXES:
        parts.setdefault(part, []).append((name, x, y, z, w, h, d))
    lines = [
        "package com.codenamecoraline.client.canopy;",
        "",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "",
        "/**",
        " * GENERATED by tools/generate_canopy_breaker_assets.py: do not edit by hand.",
        " * Every cube of the Canopy Breaker with its UV origin from the shelf-packed atlas (no overlapping footprints).",
        " */",
        "public final class CanopyBreakerParts {",
        f"    public static final int ATLAS = {ATLAS};",
        "",
        "    private CanopyBreakerParts() {}",
        "",
        "    /** Cubes of one model part. */",
        "    public static CubeListBuilder cubes(String part) {",
        "        CubeListBuilder b = CubeListBuilder.create();",
        "        switch (part) {",
    ]
    for part, boxes in parts.items():
        lines.append(f'            case "{part}" -> {{')
        for name, x, y, z, w, h, d in boxes:
            u, v = layout[name]
            lines.append(f"                b.texOffs({u}, {v}).addBox({x}F, {y}F, {z}F, {w}F, {h}F, {d}F); // {name}")
        lines.append("            }")
    lines += [
        '            default -> throw new IllegalArgumentException("Unknown part " + part);',
        "        }",
        "        return b;",
        "    }",
        "}",
        "",
    ]
    JAVA_OUT.parent.mkdir(parents=True, exist_ok=True)
    JAVA_OUT.write_text("\n".join(lines), encoding="utf-8")


# ---------------------------------------------------------------------------------------------------------------
# Items
def outline(cells, size=16):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    for (x, y), c in cells.items():
        if 0 <= x < size and 0 <= y < size:
            px[x, y] = c
    for y in range(size):
        for x in range(size):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if (x + dx, y + dy) in cells:
                        px[x, y] = OUT
                        break
    return img


def icon_club():
    cells = {}
    # diagonal root club: thick knobbed head upper right, thin handle lower left
    for i in range(0, 11):
        x, y = 2 + i, 13 - i
        c = WOOD_L if i % 2 == 0 else WOOD
        cells[(x, y)] = c
        cells[(x + 1, y)] = WOOD_D
    for (x, y) in ((9, 1), (10, 1), (11, 1), (8, 2), (9, 2), (10, 2), (11, 2), (12, 2), (7, 3), (8, 3), (9, 3), (10, 3), (11, 3),
                   (12, 3), (13, 3), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4), (12, 4), (13, 4), (8, 5), (9, 5), (10, 5), (11, 5),
                   (12, 5), (13, 5), (9, 6), (10, 6), (11, 6), (12, 6)):
        n = noise(x, y, 3)
        cells[(x, y)] = WOOD_D if n < 28 else WOOD_L if n > 70 else WOOD
    for c in ((12, 0), (13, 1), (14, 2), (6, 4), (6, 5), (14, 5)):
        cells[c] = WOOD_D
    for c in ((10, 3), (10, 4), (11, 4), (9, 5)):
        cells[c] = WOOD_DD
    cells[(0, 15)] = BONE
    cells[(1, 14)] = BONE
    cells[(1, 15)] = WOOD_D
    return outline(cells)


def icon_idol():
    cells = {}
    # a small hollow trunk with a carved ape face
    for y in range(2, 15):
        for x in range(4, 12):
            n = noise(x, y, 9)
            cells[(x, y)] = BARK_D if n < 30 else (130, 100, 76, 255) if n > 75 else BARK
    for x in range(4, 12):
        cells[(x, 2)] = MOSS
        cells[(x, 3)] = MOSS_D if x % 2 else MOSS_L
    for x in (4, 5, 10, 11):
        cells[(x, 4)] = MOSS
    for (x, y) in ((6, 7), (7, 7), (9, 7), (10, 7)):
        cells[(x, y)] = SKIN_D
    cells[(7, 7)] = EYE
    cells[(9, 7)] = EYE
    for x in range(6, 11):
        cells[(x, 10)] = SKIN_D
    for x in range(6, 11, 2):
        cells[(x, 11)] = BONE
    for y in (13, 14):
        for x in (3, 4, 11, 12):
            cells[(x, y)] = BARK
    return outline(cells)


def icon_resin():
    cells = {}
    for y in range(4, 13):
        for x in range(4, 12):
            dx, dy = x - 7.5, y - 8
            if dx * dx / 14 + dy * dy / 18 <= 1:
                n = noise(x, y, 5)
                cells[(x, y)] = (255, 190, 70, 255) if n < 30 else (224, 140, 40, 255) if n < 75 else (255, 224, 130, 255)
    for c in ((6, 6), (6, 7)):
        cells[c] = (255, 246, 190, 255)
    return outline(cells)


def icon_splinter():
    cells = {}
    for i in range(0, 12):
        x, y = 2 + i, 13 - i
        cells[(x, y)] = WOOD_L
        cells[(x + 1, y)] = WOOD
        if i > 6:
            cells[(x, y - 1)] = WOOD_D
    for c in ((13, 2), (14, 1), (12, 1)):
        cells[c] = MOSS_L
    return outline(cells)


def icon_portrait():
    cells = {}
    for y in range(2, 15):
        for x in range(3, 13):
            dx, dy = x - 7.5, y - 8.5
            if dx * dx / 24 + dy * dy / 38 <= 1:
                n = noise(x, y, 11)
                cells[(x, y)] = FUR_D if n < 25 else FUR_L if n > 75 else FUR
    for y in range(7, 12):
        for x in range(5, 11):
            cells[(x, y)] = SKIN if y < 10 else SKIN_L
    for x in range(4, 12):
        cells[(x, 6)] = SKIN_D
    cells[(6, 8)] = EYE
    cells[(9, 8)] = EYE
    for x in range(6, 10, 2):
        cells[(x, 11)] = BONE
    cells[(7, 10)] = SKIN_D
    cells[(8, 10)] = SKIN_D
    for c in ((4, 3), (3, 4), (12, 3), (13, 4)):
        cells[c] = FUR_D
    for c in ((5, 2), (10, 2)):
        cells[c] = MOSS
    return outline(cells)


def icon_egg():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 8.3
            if (dx / 6.2) ** 2 + (dy / 7.4) ** 2 <= 1:
                n = noise(x, y, 17)
                px[x, y] = FUR_D if n < 22 else FUR_L if n > 78 else FUR
                if (x + y) % 6 == 0 and 5 < y < 12:
                    px[x, y] = MOSS
                if y < 6:
                    px[x, y] = BARK_D if n < 40 else BARK
    for c in ((6, 9), (9, 11), (8, 7)):
        px[c] = MOSS_L
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3] > 0 and px[nx, ny] != OUT:
                        px[x, y] = OUT
                        break
    return img


# 3D held model of the club (rotated 45 degrees like a tool sprite so the stock handheld transforms apply)
SW = {"wood": (0, 0), "wood_l": (4, 0), "wood_d": (8, 0), "knot": (12, 0)}


def club_swatches():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    for name, (u0, v0) in SW.items():
        base, light, dark = {
            "wood": (WOOD, WOOD_L, WOOD_D), "wood_l": (WOOD_L, (190, 140, 90, 255), WOOD),
            "wood_d": (WOOD_D, WOOD, WOOD_DD), "knot": (WOOD_DD, WOOD_D, (20, 12, 10, 255)),
        }[name]
        for j in range(4):
            for i in range(4):
                n = (i * 7 + j * 13 + u0 + v0) % 5
                px[u0 + i, v0 + j] = dark if n == 0 else light if n == 4 else base
    return img


def club_model():
    parts = [
        ([7.2, -10, 7.2], [8.8, 16, 8.8], "wood"),         # handle
        ([6.5, -12.5, 6.5], [9.5, -9.5, 9.5], "wood_d"),    # pommel knob
        ([6.2, 12, 6.2], [9.8, 15, 9.8], "wood_d"),         # binding
        ([4.5, 15, 4.8], [11.5, 28.4, 11.2], "wood_l"),     # club head
        ([3.6, 18, 6.2], [4.5, 21, 9.8], "wood"),           # knot left
        ([11.5, 21, 6.4], [12.4, 24, 9.6], "wood"),         # knot right
        ([6.8, 28.4, 6.8], [9.2, 31.9, 9.2], "wood_d"),     # root tip
        ([5.4, 26, 11.2], [8.2, 28.4, 12.4], "wood_d"),     # root spike back
        ([6.2, 19, 4.2], [9.4, 22, 4.8], "knot"),           # impact scar
    ]
    elements = []
    for a, b, mat in parts:
        u, v = SW[mat]
        face = {"uv": [u, v, u + 4, v + 4], "texture": "#0"}
        elements.append({
            "from": a, "to": b,
            "rotation": {"angle": -45, "axis": "z", "origin": [8, 8, 8]},
            "faces": {k: dict(face) for k in ("north", "south", "east", "west", "up", "down")},
        })
    big = lambda s: [s, s, s]
    return {
        "textures": {"0": "codenamecoraline:item/deadroot_club_3d", "particle": "codenamecoraline:item/deadroot_club_3d"},
        "elements": elements,
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": big(1.2)},
            "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": big(1.2)},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": big(0.95)},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": big(0.95)},
            "gui": {"rotation": [30, 225, 0], "scale": big(0.5)},
            "ground": {"translation": [0, 3, 0], "scale": big(0.55)},
            "fixed": {"rotation": [0, 90, 0], "scale": big(0.6)},
        },
    }


def main():
    layout = pack()
    img, eyes = build_images(layout)
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    img.save(ENTITY_DIR / "canopy_breaker.png")
    eyes.save(ENTITY_DIR / "canopy_breaker_eyes.png")
    write_java(layout)
    icon_club().save(ITEM_DIR / "deadroot_club.png")
    icon_idol().save(ITEM_DIR / "hollow_trunk_idol.png")
    icon_resin().save(ITEM_DIR / "canopy_resin.png")
    icon_splinter().save(ITEM_DIR / "heartwood_splinter.png")
    icon_portrait().save(ITEM_DIR / "canopy_breaker_portrait.png")
    icon_egg().save(ITEM_DIR / "canopy_breaker_spawn_egg.png")
    club_swatches().save(ITEM_DIR / "deadroot_club_3d.png")
    with open(MODEL_DIR / "deadroot_club_3d.json", "w", encoding="utf-8", newline="\n") as fh:
        json.dump(club_model(), fh, indent=2)
        fh.write("\n")
    used = max(v + footprint(b[5], b[6], b[7])[1] for b in BOXES for (u, v) in [layout[b[0]]])
    print(f"Generated Canopy Breaker atlas ({len(BOXES)} boxes, {used}/{ATLAS} rows used), parts class and item art.")


if __name__ == "__main__":
    main()
