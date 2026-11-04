#!/usr/bin/env python3
"""
Single source of truth for the Crimson Commander's model boxes and textures (and his Crimson Ash Vanguards, who share
the same armour model in ash-grey).

Defines every cube of the model (part, offset, size, material), shelf-packs their UV footprints into a 256x256 atlas
(guaranteeing there is no overlap), paints the atlas, and writes:

  * assets/codenamecoraline/textures/entity/crimson_commander.png            (256x256 colour)
  * assets/codenamecoraline/textures/entity/crimson_commander_eyes.png       (256x256 emissive: visor slit, blade runes)
  * assets/codenamecoraline/textures/entity/crimson_ash_vanguard.png         (same layout, ash armour)
  * assets/codenamecoraline/textures/entity/crimson_ash_vanguard_eyes.png    (emissive: visor slit and crimson seams)
  * src/main/java/com/codenamecoraline/client/crimson/CrimsonCommanderParts.java  (generated cube factory)
  * item icons (16x16): crimson_plate_shard, stained_oath_crest, crimson_oath_greatsword, crimson_commander_portrait,
    crimson_commander_spawn_egg, crimson_ash_vanguard_spawn_egg
  * the 3D held model of the Crimson Oath Greatsword (models/item/crimson_oath_greatsword_3d.json + swatch texture)
  * build/crimson-commander-assets/atlas_preview.png (atlas x3 with box names, for review)

Model space: pixels, ground at y=24, front is -z; the model is drawn at real size (no renderer scale): 3.5 blocks tall.

Run:  python tools/generate_crimson_commander_assets.py
"""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
ENTITY_DIR = ASSETS / "textures/entity"
ITEM_DIR = ASSETS / "textures/item"
MODEL_DIR = ASSETS / "models/item"
JAVA_OUT = ROOT / "src/main/java/com/codenamecoraline/client/crimson/CrimsonCommanderParts.java"
PREVIEW = ROOT / "build/crimson-commander-assets/atlas_preview.png"
ATLAS = 256

# ---------------------------------------------------------------------------------------------------------------
# Palette (the sheet's hex colours, plus derived shades)
CRIMSON = (139, 17, 30, 255)        # #8B111E deep crimson
CRIMSON_L = (184, 29, 42, 255)      # #B81D2A blood-red highlight
CRIMSON_LL = (214, 64, 70, 255)
CRIMSON_M = (110, 12, 24, 255)
CRIMSON_D = (74, 7, 14, 255)        # #4A070E bordeaux recesses
MAIL = (58, 61, 69, 255)            # #3A3D45 iron chain mail
MAIL_L = (90, 94, 104, 255)
MAIL_D = (34, 36, 42, 255)
OBSIDIAN = (24, 22, 28, 255)        # #18161C obsidian steel
OBSIDIAN_L = (46, 42, 54, 255)
GOLD = (212, 175, 55, 255)          # #D4AF37 old gold
GOLD_L = (243, 207, 85, 255)        # #F3CF55
GOLD_D = (150, 112, 30, 255)
GOLD_DD = (96, 68, 18, 255)
RUBY = (255, 42, 60, 255)           # #FF2A3C emissive ruby
RUBY_H = (255, 150, 150, 255)
LEATHER = (54, 30, 26, 255)
LEATHER_D = (34, 18, 16, 255)
PLUME = (168, 20, 34, 255)
PLUME_L = (212, 46, 56, 255)
PLUME_D = (98, 10, 20, 255)
CAPE = (112, 14, 26, 255)
CAPE_L = (140, 22, 34, 255)
CAPE_D = (66, 8, 16, 255)
OUT = (20, 8, 10, 255)
CLEAR = (0, 0, 0, 0)

# Ash recolour for the vanguards: armour of grey ash, the crimson only in the glowing seams.
ASH_MAP = {
    CRIMSON: (80, 78, 82, 255), CRIMSON_L: (112, 110, 114, 255), CRIMSON_LL: (138, 136, 140, 255),
    CRIMSON_M: (64, 62, 66, 255), CRIMSON_D: (40, 38, 42, 255),
    GOLD: (104, 92, 80, 255), GOLD_L: (130, 118, 104, 255), GOLD_D: (78, 68, 60, 255), GOLD_DD: (54, 48, 44, 255),
    PLUME: (90, 86, 88, 255), PLUME_L: (116, 112, 114, 255), PLUME_D: (58, 54, 56, 255),
    CAPE: (66, 62, 64, 255), CAPE_L: (86, 82, 84, 255), CAPE_D: (40, 38, 40, 255),
}


def noise(x, y, seed=0):
    n = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)
    return (n & 0x7FFFFFFF) % 100


# ---------------------------------------------------------------------------------------------------------------
# Boxes: (name, part, x, y, z, w, h, d, material). Offsets are relative to the part's pivot (see CrimsonCommanderModel).
BOXES = [
    # pelvis: hips, belt with the heraldic insignia, a ring of skirt plates (faulds)
    ("hips", "pelvis", -7, -3, -4, 14, 6, 8, "plate_dark"),
    ("belt", "pelvis", -7.3, -3.6, -4.3, 14.6, 1.6, 8.6, "gold_dark"),
    ("faulds", "pelvis", -7.5, 1, -4.5, 15, 4, 9, "plate"),
    ("insignia", "pelvis", -2, -3, -5.4, 4, 4, 1, "insignia"),
    # chest: torso, breastplate, layered abdomen plates, gorget
    ("torso", "chest", -8, -19, -5, 16, 19, 10, "plate"),
    ("breast", "chest", -8.5, -18, -6, 17, 10, 4, "plate_light"),
    ("abdomen", "chest", -7, -8, -5.5, 14, 7.5, 3, "abdomen"),
    ("gorget", "chest", -5, -21, -4.5, 10, 3, 9, "plate_light"),
    # head: closed helm with a shadowed T visor, a sharp crest; horns and the plume hang off it
    ("helmet", "head", -4.5, -10, -5, 9, 10, 9, "helmet"),
    ("crest_front", "head", -1.5, -14, -5, 3, 5, 5, "plate_light"),
    ("crest_back", "head", -1, -12, -1, 2, 3, 6, "plate"),
    ("horn_l", "horn_l", -0.75, -5, -0.75, 1.5, 5, 1.5, "plate"),
    ("horn_r", "horn_r", -0.75, -5, -0.75, 1.5, 5, 1.5, "plate"),
    ("plume", "plume", -1.5, -13, -1.5, 3, 13, 3, "plume"),
    ("plume_tail", "plume_tail", -1.5, 0, -1.5, 3, 15, 3, "plume"),
    # right arm: spiked pauldron, mail sleeve, gauntleted forearm, fist
    ("r_pauldron", "right_arm", -5.5, -3.5, -5, 8, 7, 10, "pauldron"),
    ("r_rim", "right_arm", -6, 1.5, -5.5, 9, 2.5, 11, "gold"),
    ("r_upper", "right_arm", -2.5, 0, -2.5, 5, 10, 5, "mail"),
    ("r_spike_a", "r_spike_a", -1, -6, -1, 2, 6, 2, "spike"),
    ("r_spike_b", "r_spike_b", -1, -5, -1, 2, 5, 2, "spike"),
    ("r_spike_c", "r_spike_c", -1, -4, -1, 2, 4, 2, "spike"),
    ("r_couter", "right_forearm", -3.4, -0.5, -3.4, 6.8, 3, 6.8, "plate_dark"),
    ("r_gauntlet", "right_forearm", -3, 0, -3, 6, 9, 6, "plate"),
    ("r_cuff", "right_forearm", -3.5, 6.5, -3.5, 7, 2.5, 7, "gold_dark"),
    ("r_fist", "right_hand", -2.5, 0, -2.5, 5, 4, 5, "fist"),
    # left arm (mirror)
    ("l_pauldron", "left_arm", -2.5, -3.5, -5, 8, 7, 10, "pauldron"),
    ("l_rim", "left_arm", -3, 1.5, -5.5, 9, 2.5, 11, "gold"),
    ("l_upper", "left_arm", -2.5, 0, -2.5, 5, 10, 5, "mail"),
    ("l_spike_a", "l_spike_a", -1, -6, -1, 2, 6, 2, "spike"),
    ("l_spike_b", "l_spike_b", -1, -5, -1, 2, 5, 2, "spike"),
    ("l_spike_c", "l_spike_c", -1, -4, -1, 2, 4, 2, "spike"),
    ("l_couter", "left_forearm", -3.4, -0.5, -3.4, 6.8, 3, 6.8, "plate_dark"),
    ("l_gauntlet", "left_forearm", -3, 0, -3, 6, 9, 6, "plate"),
    ("l_cuff", "left_forearm", -3.5, 6.5, -3.5, 7, 2.5, 7, "gold_dark"),
    ("l_fist", "left_hand", -2.5, 0, -2.5, 5, 4, 5, "fist"),
    # the greatsword in the right fist: it points along -z from the grip (a pose's sword angle is the blade's direction)
    ("pommel", "sword", -2, -2, 3.5, 4, 4, 3, "steel_guard"),
    ("grip", "sword", -1, -1, -3, 2, 2, 7, "grip"),
    ("guard", "sword", -1.5, -7, -5, 3, 14, 2, "steel_guard"),
    ("guard_knob_a", "sword", -2, -9, -5.5, 4, 3, 3, "steel_guard"),
    ("guard_knob_b", "sword", -2, 6, -5.5, 4, 3, 3, "steel_guard"),
    ("blade", "sword", -0.8, -4, -33, 1.6, 8, 28, "blade"),
    ("blade_tip", "sword", -0.7, -3, -38, 1.4, 6, 5, "blade"),
    ("blade_point", "sword", -0.6, -1.5, -41, 1.2, 3, 3, "blade"),
    # the tattered royal cape
    ("cape", "cape", -7, 0, 0, 14, 38, 1, "cape"),
    # legs: mail thighs with cuisses, knee cops, greaves, sabatons
    ("r_thigh", "right_thigh", -3, 0, -3, 6, 12, 6, "mail"),
    ("r_cuisse", "right_thigh", -3.4, 0.5, -3.6, 6.8, 9, 4, "plate"),
    ("r_knee", "right_shin", -3.2, -1.5, -3.8, 6.4, 3.5, 3.4, "gold_dark"),
    ("r_greave", "right_shin", -3, 0, -3, 6, 9, 6, "plate"),
    ("r_sabaton", "right_foot", -3.2, -1, -5.5, 6.4, 3, 8.5, "plate_dark"),
    ("l_thigh", "left_thigh", -3, 0, -3, 6, 12, 6, "mail"),
    ("l_cuisse", "left_thigh", -3.4, 0.5, -3.6, 6.8, 9, 4, "plate"),
    ("l_knee", "left_shin", -3.2, -1.5, -3.8, 6.4, 3.5, 3.4, "gold_dark"),
    ("l_greave", "left_shin", -3, 0, -3, 6, 9, 6, "plate"),
    ("l_sabaton", "left_foot", -3.2, -1, -5.5, 6.4, 3, 8.5, "plate_dark"),
]


def ceil(v):
    return int(-(-v // 1))


def footprint(w, h, d):
    return 2 * (ceil(d) + ceil(w)), ceil(d) + ceil(h)


def pack():
    """Shelf packing, tallest first; returns {name: (u, v)}."""
    items = sorted(BOXES, key=lambda b: (-footprint(b[5], b[6], b[7])[1], -footprint(b[5], b[6], b[7])[0]))
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
def faces_of(u, v, w, h, d):
    w, h, d = ceil(w), ceil(h), ceil(d)
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }


def region_pixels(u, v, w, h, d):
    """Yield (face, x, y, i, j, fw, fh): atlas pixel and its face-local position for every pixel of a box footprint."""
    for name, (fx0, fy0, fw, fh) in faces_of(u, v, w, h, d).items():
        for j in range(fh):
            for i in range(fw):
                yield name, fx0 + i, fy0 + j, i, j, fw, fh


def shade3(t, light, base, dark):
    return light if t < 0.22 else base if t < 0.78 else dark


def paint_box(img, eyes, box, u, v):
    name, part, bx, by, bz, w, h, d, mat = box
    px = img.load()
    ex = eyes.load()
    seed = sum(ord(c) for c in name)
    for face, x, y, i, j, fw, fh in region_pixels(u, v, w, h, d):
        n = noise(x, y, seed)
        t = j / max(1, fh - 1)
        side = face in ("front", "back", "left", "right")
        rim = side and (i == 0 or i == fw - 1)
        c = None
        if mat in ("plate", "plate_light", "pauldron", "helmet"):
            c = shade3(t, CRIMSON_L, CRIMSON, CRIMSON_M) if side else (CRIMSON_L if face == "top" else CRIMSON_D)
            if mat == "plate_light" and side and t < 0.35:
                c = CRIMSON_LL if n > 60 else CRIMSON_L
            if rim:
                c = CRIMSON_D
            if side and j == fh - 1:
                c = CRIMSON_D
            if n < 5:
                c = CRIMSON_LL  # scratches catching the light
            if mat == "plate_light" and side and (j == 0 or j == fh - 1) and fh > 3:
                c = GOLD if (i % 3) else GOLD_L  # gold trim along the top and bottom edges
            if mat == "pauldron" and side and j in (2, 4):
                c = CRIMSON_D  # overlapping lames
        elif mat == "plate_dark":
            c = shade3(t, CRIMSON_M, CRIMSON_D, OUT) if side else CRIMSON_D
            if n < 8:
                c = CRIMSON
        elif mat == "abdomen":
            band = j % 3
            c = CRIMSON_L if band == 0 else CRIMSON if band == 1 else CRIMSON_D  # layered lames
            if rim:
                c = CRIMSON_D
        elif mat == "fist":
            c = CRIMSON_M if n > 30 else CRIMSON_D
            if side and j == 0:
                c = CRIMSON
            if face == "front" and j in (1, 3):
                c = CRIMSON_D  # finger plates
        elif mat == "mail":
            dot = (i + (j % 2)) % 2 == 0
            c = MAIL_L if dot and n > 40 else MAIL if dot else MAIL_D
            if face == "bottom":
                c = MAIL_D
        elif mat in ("gold", "gold_dark"):
            c = shade3(t, GOLD_L, GOLD, GOLD_D) if side else (GOLD_L if face == "top" else GOLD_D)
            if mat == "gold_dark":
                c = GOLD if c == GOLD_L else GOLD_D if c == GOLD else GOLD_DD
            if side and (i + j * 2) % 7 == 0:
                c = GOLD_D if mat == "gold" else GOLD_DD  # engraved scrollwork
        elif mat == "insignia":
            c = GOLD if n > 25 else GOLD_D
            if face == "front":
                cx, cy = (fw - 1) / 2, (fh - 1) / 2
                if abs(i - cx) <= 0.6 and abs(j - cy) <= 0.6 or (abs(i - cx) + abs(j - cy) <= 1.0):
                    c = RUBY
                    ex[x, y] = RUBY
                elif i in (0, fw - 1) or j in (0, fh - 1):
                    c = GOLD_L
        elif mat == "spike":
            c = CRIMSON_D if i % 2 else CRIMSON_M
            if side and i == 0:
                c = CRIMSON_L  # lit ridge
            if side and j <= 1:
                c = OBSIDIAN  # blackened point
        elif mat == "horn":
            c = OBSIDIAN if t < 0.7 else CRIMSON_D
            if side and i == 0:
                c = OBSIDIAN_L
            if side and j >= fh - 2:
                c = GOLD_D
        elif mat == "plume":
            stripe = (i * 3 + (x % 2)) % 4
            c = PLUME_L if stripe == 0 else PLUME if stripe in (1, 2) else PLUME_D
            if n < 12:
                c = PLUME_D
            if face == "bottom" or (side and j == fh - 1):
                c = PLUME_D
        elif mat == "cape":
            fold = i % 4
            c = CAPE_L if fold == 0 else CAPE if fold in (1, 2) else CAPE_D
            if j in (1, 2):
                c = GOLD if j == 1 else GOLD_D  # royal hem under the collar
            if n < 6:
                c = CAPE_D  # stains
            # tattered bottom: ragged strips and holes in the last rows
            if side and fh > 20:
                tear = 3 + (noise(i, 7, 41) % 7)
                if j >= fh - tear:
                    c = CLEAR
                if 18 < j < fh - 8 and noise(i, j, 77) < 3:
                    c = CLEAR
        elif mat == "steel_guard":
            c = shade3(t, MAIL_L, MAIL, MAIL_D) if side else (MAIL_L if face == "top" else MAIL_D)
            if n < 8:
                c = OBSIDIAN_L
            if rim:
                c = OBSIDIAN
        elif mat == "grip":
            c = CRIMSON_D if (i + j) % 3 else CRIMSON_M
            if (i + j) % 6 == 0:
                c = MAIL  # dark wire wrapping
        elif mat == "blade":
            c = OBSIDIAN if n > 18 else OBSIDIAN_L
            if face in ("top", "bottom"):
                c = GOLD_L if (j + i) % 3 else GOLD  # the gold cutting edges
            elif face in ("left", "right") and fh >= 4:
                if j == 0 or j == fh - 1:
                    c = GOLD if i % 2 else GOLD_L
                elif j == 1 or j == fh - 2:
                    c = OBSIDIAN_L
        if c is None:
            c = (255, 0, 255, 255)
        px[x, y] = c

    # ---- per-box details ------------------------------------------------------------------------------------
    f = faces_of(u, v, w, h, d)

    def put(face_name, fi, fj, color, glow=None):
        fx0, fy0, fw, fh = f[face_name]
        if 0 <= fi < fw and 0 <= fj < fh:
            px[fx0 + fi, fy0 + fj] = color
            if glow is not None:
                ex[fx0 + fi, fy0 + fj] = glow

    wi, hi, di = ceil(w), ceil(h), ceil(d)
    if name == "helmet":
        # closed helm: dark T visor (eye slit across, breath slit down the middle) with the ruby glow inside
        for i in range(1, wi - 1):
            put("front", i, 3, CRIMSON_D)
            put("front", i, 4, OBSIDIAN, RUBY if 1 < i < wi - 2 else None)
            put("front", i, 5, CRIMSON_D)
        for jj in range(5, 9):
            put("front", wi // 2, jj, OBSIDIAN, RUBY if jj < 8 else None)
            put("front", wi // 2 - 1, jj, CRIMSON_D)
            put("front", wi // 2 + 1, jj, CRIMSON_D)
        for i in range(wi):
            put("front", i, 0, CRIMSON_D)  # a dark brow ridge
        for i in (1, 2, wi - 3, wi - 2):
            put("front", i, 7, CRIMSON_M)  # breathing holes
    if name == "breast":
        # a gold chevron and the oath's sword in relief on the breastplate
        cx = wi // 2
        for k in range(5):
            put("front", cx - k - 1, 2 + k, GOLD_D)
            put("front", cx + k, 2 + k, GOLD_D)
        for jj in range(2, 9):
            put("front", cx, jj, GOLD_L if jj > 2 else GOLD)
            put("front", cx - 1, jj, GOLD_D if jj > 2 else GOLD)
    if name == "torso":
        for jj in range(4, hi - 2):
            put("back", wi // 2, jj, CRIMSON_D)  # spine ridge of the backplate
    if name == "faulds":
        for side in ("front", "back", "left", "right"):
            fw = f[side][2]
            for i in range(0, fw, 3):
                for jj in range(hi):
                    put(side, i, jj, CRIMSON_D)  # separate plates of the skirt
    if name in ("r_greave", "l_greave", "r_gauntlet", "l_gauntlet"):
        put("front", wi // 2, 2, GOLD)
        put("front", wi // 2, 3, GOLD_D)
    if name == "cape":
        # the stained crest of the oath stitched on the back of the cape
        cx = wi // 2
        for jj in range(6, 14):
            for i in range(cx - 3, cx + 3):
                inside = abs(i - cx + 0.5) + max(0, jj - 10) * 0.8 <= 3
                if inside:
                    put("back", i, jj, GOLD_D if (i + jj) % 3 else GOLD)
        for jj in range(7, 13):
            put("back", cx - 1, jj, OBSIDIAN)
            put("back", cx, jj, OBSIDIAN)


def build_images(layout):
    img = Image.new("RGBA", (ATLAS, ATLAS), CLEAR)
    eyes = Image.new("RGBA", (ATLAS, ATLAS), CLEAR)
    for box in BOXES:
        u, v = layout[box[0]]
        paint_box(img, eyes, box, u, v)
    return img, eyes


def ash_variant(img, eyes, layout):
    """The vanguards wear the same armour in grey ash; crimson survives only in glowing seams and the visor."""
    ash = img.copy()
    px = ash.load()
    for y in range(ATLAS):
        for x in range(ATLAS):
            c = px[x, y]
            if c[3] and c in ASH_MAP:
                px[x, y] = ASH_MAP[c]
    glow = Image.new("RGBA", (ATLAS, ATLAS), CLEAR)
    gx = glow.load()
    src = eyes.load()
    for y in range(ATLAS):
        for x in range(ATLAS):
            if src[x, y][3]:
                gx[x, y] = (230, 30, 40, 255)
    # crimson cracks across the chest and the gauntlets
    for name, path in (("breast", [(2, 1), (3, 2), (3, 3), (4, 4), (5, 4), (6, 5), (9, 3), (10, 4), (11, 5), (12, 6)]),
                       ("torso", [(4, 12), (5, 13), (5, 14), (6, 15), (10, 11), (11, 12), (11, 13)]),
                       ("r_gauntlet", [(2, 3), (3, 4), (3, 5)]), ("l_gauntlet", [(3, 2), (2, 3), (2, 4)])):
        box = next(b for b in BOXES if b[0] == name)
        u, v = layout[name]
        fx0, fy0, fw, fh = faces_of(u, v, box[5], box[6], box[7])["front"]
        for i, j in path:
            if i < fw and j < fh:
                px[fx0 + i, fy0 + j] = (40, 6, 10, 255)
                gx[fx0 + i, fy0 + j] = (230, 30, 40, 255)
    return ash, glow


# ---------------------------------------------------------------------------------------------------------------
def write_java(layout):
    parts = {}
    for name, part, x, y, z, w, h, d, mat in BOXES:
        parts.setdefault(part, []).append((name, x, y, z, w, h, d))
    lines = [
        "package com.codenamecoraline.client.crimson;",
        "",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "",
        "/**",
        " * GENERATED by tools/generate_crimson_commander_assets.py: do not edit by hand.",
        " * Every cube of the Crimson Commander (and his vanguards) with its UV origin from the shelf-packed atlas.",
        " */",
        "public final class CrimsonCommanderParts {",
        f"    public static final int ATLAS = {ATLAS};",
        "",
        "    private CrimsonCommanderParts() {}",
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
        "            default -> {",
        "            }",
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
    img = Image.new("RGBA", (size, size), CLEAR)
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


def icon_greatsword():
    cells = {}
    # pommel and grip lower left, crossguard, then the long black blade to the upper right with gold edges and runes
    cells[(1, 14)] = GOLD
    cells[(2, 14)] = GOLD_D
    cells[(1, 13)] = GOLD_D
    for k in range(2):
        cells[(2 + k, 13 - k)] = LEATHER if k % 2 else LEATHER_D
    for (x, y) in ((2, 10), (3, 10), (3, 11), (4, 11), (5, 12), (5, 13), (6, 13), (4, 12)):
        cells[(x, y)] = MAIL
    cells[(4, 10)] = MAIL_L
    cells[(6, 12)] = MAIL_L
    cells[(4, 11)] = MAIL_D
    # the blade: a two-pixel black core with a single gold cutting edge along its upper side and ruby runes in the fuller
    for k in range(10):
        x, y = 5 + k, 11 - k
        cells[(x, y)] = OBSIDIAN
        cells[(x + 1, y)] = OBSIDIAN_L if k % 3 else OBSIDIAN
        cells[(x, y - 1)] = GOLD_L if k % 2 else GOLD
    cells[(15, 1)] = GOLD_L
    cells[(15, 0)] = GOLD_L
    return outline(cells)


def icon_shard():
    cells = {}
    poly = [(3, 4), (11, 2), (13, 7), (12, 12), (6, 13), (2, 9)]
    for y in range(16):
        for x in range(16):
            inside = False
            for k in range(len(poly)):  # point in polygon (even-odd)
                (x1, y1), (x2, y2) = poly[k], poly[(k + 1) % len(poly)]
                if (y1 > y + 0.5) != (y2 > y + 0.5) and x + 0.5 < (x2 - x1) * (y + 0.5 - y1) / (y2 - y1) + x1:
                    inside = not inside
            if inside:
                n = noise(x, y, 19)
                cells[(x, y)] = CRIMSON_L if y < 6 and n > 30 else CRIMSON_D if n < 18 else CRIMSON
    for x in range(4, 11):
        cells[(x, 4 - (x - 3) // 4)] = GOLD
    for (x, y) in ((6, 7), (7, 8), (9, 6), (5, 10)):
        cells[(x, y)] = CRIMSON_LL
    cells[(8, 10)] = CRIMSON_D
    cells[(9, 11)] = CRIMSON_D
    return outline(cells)


def icon_crest():
    cells = {}
    # a heron-shaped heraldic shield: gold border, crimson field, the black oath sword, old dark stains
    for y in range(2, 15):
        half = 6 if y < 9 else max(0, 6 - (y - 8))
        for x in range(8 - half, 8 + half):
            border = x in (8 - half, 8 + half - 1) or y == 2
            cells[(x, y)] = GOLD if border else CRIMSON
    for y in range(4, 13):
        cells[(7, y)] = OBSIDIAN
        cells[(8, y)] = OBSIDIAN_L if y < 11 else OBSIDIAN
    for x in range(5, 11):
        cells[(x, 6)] = GOLD_L
    cells[(7, 4)] = GOLD_L
    cells[(8, 4)] = GOLD_L
    for (x, y) in ((4, 9), (5, 10), (10, 4), (11, 5), (9, 11), (4, 5)):
        cells[(x, y)] = CRIMSON_D  # stains
    cells[(3, 3)] = GOLD_D
    cells[(12, 3)] = GOLD_D
    return outline(cells)


def icon_portrait():
    cells = {}
    # the closed helm, front: crimson, gold brow band, T visor glowing ruby, crest and the red plume behind
    for y in range(4, 15):
        for x in range(4, 12):
            n = noise(x, y, 23)
            cells[(x, y)] = CRIMSON_L if x < 6 else CRIMSON_D if x > 10 else CRIMSON if n > 15 else CRIMSON_M
    for x in range(4, 12):
        cells[(x, 5)] = CRIMSON_D
    for x in range(5, 11):
        cells[(x, 8)] = OBSIDIAN
    for y in range(9, 13):
        cells[(7, y)] = OBSIDIAN
        cells[(8, y)] = OBSIDIAN
    for y in range(1, 4):
        cells[(7, y)] = CRIMSON_L
        cells[(8, y)] = CRIMSON
    for (x, y) in ((12, 3), (13, 4), (13, 5), (14, 6), (13, 7), (14, 8), (12, 2)):
        cells[(x, y)] = PLUME_L if y % 2 else PLUME
    for (x, y) in ((3, 6), (2, 5), (12, 6)):
        cells[(x, y)] = CRIMSON_D  # cheek fins
    return outline(cells)


def icon_egg(base, mid, dark, spot, spot2):
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 8.3
            if (dx / 6.2) ** 2 + (dy / 7.4) ** 2 <= 1:
                n = noise(x, y, 31)
                px[x, y] = dark if n < 22 else mid if n > 78 else base
    for c in ((6, 5), (9, 7), (5, 10), (10, 11), (8, 13)):
        px[c] = spot
    for c in ((7, 9), (10, 4)):
        px[c] = spot2
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3] > 0 and px[nx, ny] != OUT:
                        px[x, y] = OUT
                        break
    return img


# 3D held model of the greatsword (rotated 45 degrees like a tool sprite so the stock handheld transforms apply)
SW = {"blade": (0, 0), "gold": (4, 0), "gold_l": (8, 0), "grip": (12, 0), "rune": (0, 4), "steel": (4, 4)}


def sword_swatches():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    for name, (u0, v0) in SW.items():
        base, light, dark = {
            "blade": (OBSIDIAN, OBSIDIAN_L, (14, 12, 18, 255)), "gold": (GOLD, GOLD_L, GOLD_D),
            "gold_l": (GOLD_L, (255, 232, 140, 255), GOLD), "grip": (LEATHER, (80, 46, 38, 255), LEATHER_D),
            "rune": (RUBY, RUBY_H, (200, 20, 40, 255)), "steel": (MAIL, MAIL_L, MAIL_D),
        }[name]
        for j in range(4):
            for i in range(4):
                n = (i * 7 + j * 13 + u0 + v0) % 5
                px[u0 + i, v0 + j] = dark if n == 0 else light if n == 4 else base
    return img


def sword_model():
    parts = [
        ([6.4, -13.5, 6.4], [9.6, -11, 9.6], "steel"),       # pommel
        ([7.2, -11, 7.2], [8.8, -3, 8.8], "grip"),           # grip
        ([2.6, -3, 6.6], [13.4, -1.2, 9.4], "steel"),        # broad crossguard
        ([2.2, -3.6, 6.4], [3.6, -0.6, 9.6], "steel"),       # guard knob
        ([12.4, -3.6, 6.4], [13.8, -0.6, 9.6], "steel"),     # guard knob
        ([5.4, -1.2, 7.3], [10.6, 24, 8.7], "blade"),        # wide black blade
        ([5.0, -1.2, 7.4], [5.5, 24, 8.6], "gold_l"),        # left gold edge
        ([10.5, -1.2, 7.4], [11.0, 24, 8.6], "gold_l"),      # right gold edge
        ([6.2, 24, 7.4], [9.8, 28.5, 8.6], "blade"),         # tip
        ([7.4, 28.5, 7.6], [8.6, 30.5, 8.4], "gold_l"),      # point
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
        "textures": {"0": "codenamecoraline:item/crimson_oath_greatsword_3d",
                     "particle": "codenamecoraline:item/crimson_oath_greatsword_3d"},
        "elements": elements,
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": big(1.15)},
            "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": big(1.15)},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": big(0.9)},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": big(0.9)},
            "gui": {"rotation": [30, 225, 0], "scale": big(0.5)},
            "ground": {"translation": [0, 3, 0], "scale": big(0.5)},
            "fixed": {"rotation": [0, 90, 0], "scale": big(0.55)},
        },
    }


def preview(img, layout):
    big = img.resize((ATLAS * 3, ATLAS * 3), Image.NEAREST)
    bg = Image.new("RGBA", big.size, (60, 70, 90, 255))
    bg.alpha_composite(big)
    d = ImageDraw.Draw(bg)
    for box in BOXES:
        u, v = layout[box[0]]
        fw, fh = footprint(box[5], box[6], box[7])
        d.rectangle((u * 3, v * 3, (u + fw) * 3 - 1, (v + fh) * 3 - 1), outline=(255, 255, 255, 90))
        d.text((u * 3 + 2, v * 3 + 1), box[0], fill=(255, 255, 255, 255))
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    bg.save(PREVIEW)


def main():
    layout = pack()
    img, eyes = build_images(layout)
    ash, ash_eyes = ash_variant(img, eyes, layout)
    for p in (ENTITY_DIR, ITEM_DIR, MODEL_DIR):
        p.mkdir(parents=True, exist_ok=True)
    img.save(ENTITY_DIR / "crimson_commander.png")
    eyes.save(ENTITY_DIR / "crimson_commander_eyes.png")
    ash.save(ENTITY_DIR / "crimson_ash_vanguard.png")
    ash_eyes.save(ENTITY_DIR / "crimson_ash_vanguard_eyes.png")
    write_java(layout)
    icon_greatsword().save(ITEM_DIR / "crimson_oath_greatsword.png")
    icon_shard().save(ITEM_DIR / "crimson_plate_shard.png")
    icon_crest().save(ITEM_DIR / "stained_oath_crest.png")
    icon_portrait().save(ITEM_DIR / "crimson_commander_portrait.png")
    icon_egg(CRIMSON, CRIMSON_L, CRIMSON_D, GOLD, OBSIDIAN).save(ITEM_DIR / "crimson_commander_spawn_egg.png")
    icon_egg((84, 82, 86, 255), (118, 116, 120, 255), (48, 46, 50, 255), RUBY, CRIMSON_D).save(
        ITEM_DIR / "crimson_ash_vanguard_spawn_egg.png")
    sword_swatches().save(ITEM_DIR / "crimson_oath_greatsword_3d.png")
    with open(MODEL_DIR / "crimson_oath_greatsword_3d.json", "w", encoding="utf-8", newline="\n") as fh:
        json.dump(sword_model(), fh, indent=2)
        fh.write("\n")
    preview(img, layout)
    used = max(v + footprint(b[5], b[6], b[7])[1] for b in BOXES for (u, v) in [layout[b[0]]])
    print(f"Generated Crimson Commander atlas ({len(BOXES)} boxes, {used}/{ATLAS} rows used), vanguard atlas, "
          f"parts class and item art.")


if __name__ == "__main__":
    main()
