#!/usr/bin/env python3
"""
Single source of truth for the models and textures of the Witch of Ten Threads and her ten puppets.

Every one of the 11 models is a tree of parts, each a list of boxes. They are written here in "height above the ground"
terms (model space: 16 px per block, ground at y = 24, front is -z) and converted to part-relative cube offsets. For each
model the boxes are shelf-packed into a 256x256 atlas (no overlapping UV footprints), painted by material, and the script
writes:

  * assets/codenamecoraline/textures/entity/<id>.png and <id>_eyes.png  (colour and emissive: button eyes and spools)
  * src/main/java/com/codenamecoraline/client/ten_threads_witch/TenThreadsParts.java  (generated model layers)
  * item icons (16x16): cursed_spindle, puppet_remnant, matriarch_thimble, ten_threads_staff, portrait, spawn egg
  * the 3D held model of the Staff of the Ten Threads (models/item/ten_threads_staff_3d.json + swatch texture)
  * build/ten-threads-witch-assets/atlas_<id>.png (atlas x3 with box names) for review

Run:  python tools/generate_ten_threads_witch_assets.py
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
ENTITY_DIR = ASSETS / "textures/entity"
ITEM_DIR = ASSETS / "textures/item"
MODEL_DIR = ASSETS / "models/item"
JAVA_OUT = ROOT / "src/main/java/com/codenamecoraline/client/ten_threads_witch/TenThreadsParts.java"
PREVIEW_DIR = ROOT / "build/ten-threads-witch-assets"
ATLAS = 256

# ---------------------------------------------------------------------------------------------------------------
# Palette (the sheet's hex colours plus derived shades)
WOOD = (43, 29, 20, 255)
WOOD_D = (23, 14, 9, 255)
WOOD_L = (74, 52, 38, 255)
BURLAP = (140, 122, 107, 255)
BURLAP_D = (110, 93, 79, 255)
BURLAP_L = (166, 148, 130, 255)
LACE = (194, 185, 167, 255)
LACE_D = (150, 142, 126, 255)
PORC = (234, 230, 223, 255)
PORC_D = (200, 194, 186, 255)
CRACK = (58, 53, 50, 255)
BLUE = (59, 89, 120, 255)
BONE = (214, 206, 191, 255)
BONE_D = (158, 148, 130, 255)
BRONZE = (74, 59, 44, 255)
BRONZE_L = (104, 84, 62, 255)
PATINA = (62, 122, 107, 255)
THREAD = (216, 226, 232, 255)
THREAD_D = (160, 176, 186, 255)
EYE = (255, 176, 46, 255)
EYE_H = (255, 226, 150, 255)
RUBY = (224, 17, 95, 255)
RUBY_H = (255, 120, 170, 255)
CRIMSON = (255, 30, 66, 255)
BARK = (58, 42, 30, 255)
BARK_L = (86, 64, 44, 255)
MOSS = (70, 96, 52, 255)
MOSS_D = (44, 66, 36, 255)
CLOTH = (58, 47, 42, 255)
CLOTH_D = (36, 28, 26, 255)
CLOTH_L = (84, 68, 60, 255)
TENDON = (90, 31, 42, 255)
SKIN = (184, 160, 144, 255)
SKIN_D = (140, 118, 104, 255)
OUT = (20, 12, 10, 255)
CLEAR = (0, 0, 0, 0)


def noise(x, y, seed=0):
    n = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)
    return (n & 0x7FFFFFFF) % 100


# ---------------------------------------------------------------------------------------------------------------
# Model description helpers. A box is (name, cx, bottom height, cz, w, h, d, material): centre x, height of the underside
# above the ground, centre z, size. A part is (name, parent, pivot x, pivot height, pivot z, [boxes]).
class Model:
    def __init__(self, ident):
        self.id = ident
        self.parts = []
        self.counter = 0

    def part(self, name, parent, px, ph, pz, boxes=(), rot=(0, 0, 0)):
        """rot: the rest rotation in degrees (x, y, z), e.g. the curl of a relaxed finger."""
        self.parts.append({"name": name, "parent": parent, "pivot": (px, ph, pz), "boxes": [], "rot": rot})
        for b in boxes:
            self.box(name, *b)
        return name

    def box(self, part, cx, hb, cz, w, h, d, mat):
        p = next(p for p in self.parts if p["name"] == part)
        self.counter += 1
        p["boxes"].append((f"{part}_{self.counter}", cx, hb, cz, w, h, d, mat))

    def all_boxes(self):
        return [b for p in self.parts for b in p["boxes"]]


def lr(m, name, parent, px, ph, pz, boxes_fn):
    """Adds a left and a right part (suffix _l at +x, _r at -x); boxes_fn(sign) returns the boxes of one side."""
    for sign, suffix in ((1, "_l"), (-1, "_r")):
        m.part(name + suffix, parent, px * sign, ph, pz, boxes_fn(sign))


def spool_boxes(cx, cy, cz, axis):
    """A thread spool: a red core between two brass flanges, its axis along x, y or z (all dimensions whole pixels)."""
    if axis == "x":
        return [(cx, cy - 1, cz, 3, 2, 2, "spool"), (cx - 2, cy - 1.5, cz, 1, 3, 3, "brass"), (cx + 2, cy - 1.5, cz, 1, 3, 3, "brass")]
    if axis == "y":
        return [(cx, cy - 1, cz, 2, 3, 2, "spool"), (cx, cy - 2.5, cz, 3, 1, 3, "brass"), (cx, cy + 1.5, cz, 3, 1, 3, "brass")]
    return [(cx, cy - 1, cz, 2, 2, 3, "spool"), (cx, cy - 1.5, cz - 2, 3, 3, 1, "brass"), (cx, cy - 1.5, cz + 2, 3, 3, 1, "brass")]


def build_models():
    models = {}

    # ---- the Matriarch: a tiny, frail old woman in a mossy hooded cloak, lace spectacles, the staff with its ten spools
    # The rig of the Matriarch's animation system (docs/design/matriarch_animation.md): the hip and the chest bend apart,
    # the arms have elbows and wrists, each hand has a thumb and four fingers, the face has brows and a mouth with corners,
    # the hair has two locks of two segments, and the cloak is four panels (back, hem, left, right) plus the hood's point.
    # The silhouette and the materials are those of the previous model; only the joints were added.
    m = Model("ten_threads_witch")
    m.part("body", None, 0, 7, 0, [(0, 4.5, 0, 9, 3, 6, "dress")])
    m.part("chest", "body", 0, 7, 0, [(0, 7, 0, 8, 6, 5, "dress"), (0, 11.5, -2.6, 4, 1, 1, "silver")])
    for s, sfx in ((1, "_l"), (-1, "_r")):
        m.part("leg" + sfx, "body", 2 * s, 7, 0, [(2 * s, 2, 0, 3, 5, 3, "dress")])
        m.part("foot" + sfx, "leg" + sfx, 2 * s, 2, 0, [(2 * s, 0, -1, 3, 2, 5, "boot")])
    m.part("mantle", "chest", 0, 13, 0, [(0, 11.5, 0, 12, 3, 8, "cloak"), (0, 10, -3.5, 4, 2, 1, "cloak")])
    m.part("skirt", "chest", 0, 12, 3.5, [(0, 7, 3.5, 11, 5, 2, "cloak")])
    m.part("skirt_hem", "skirt", 0, 7, 3.5, [(0, 3, 3.5, 11, 4, 2, "cloak")])
    for s, sfx in ((1, "_l"), (-1, "_r")):
        m.part("cloak" + sfx, "chest", 5.5 * s, 12, 0.5, [(5.5 * s, 4, 0.5, 1, 8, 6, "cloak")])
    m.part("head", "chest", 0, 13, -0.5, [(0, 12.5, -0.5, 6, 6, 5, "face_old"), (0, 14, -3.5, 1, 2, 1, "skin_old"),
                                        (3.4, 12, -0.5, 1, 2, 1, "silver"), (-3.4, 12, -0.5, 1, 2, 1, "silver")])
    m.part("hair_back", "head", 0, 18.5, 2.5, [(0, 13.5, 2.5, 5, 5, 2, "hair_grey")])
    for s, sfx in ((1, "_l"), (-1, "_r")):
        m.part("hair_lock" + sfx, "head", 3.5 * s, 17.5, -2.2, [(3.5 * s, 15, -2.2, 1, 2.5, 1, "hair_grey")])
        m.part("hair_lock" + sfx + "_tip", "hair_lock" + sfx, 3.5 * s, 15, -2.2, [(3.5 * s, 13.2, -2.2, 0.8, 1.8, 0.8, "hair_grey")])
    m.part("hood", "head", 0, 19, 0, [(0, 18.5, -0.5, 8, 2, 7, "cloak"), (0, 11.5, 3.5, 8, 8, 2, "cloak"), (4.5, 12, -0.5, 1, 7, 6, "cloak"), (-4.5, 12, -0.5, 1, 7, 6, "cloak"),
                                      (0, 18, -4, 7, 1, 1, "cloak")])
    m.part("hood_tip", "hood", 0, 20.5, 1, [(0, 20.5, 1, 4, 2, 4, "cloak")])
    m.part("blindfold", "head", 0, 15.5, -0.5, [(0, 14.5, -0.5, 7, 2, 6, "lace"), (1.7, 14.5, -3.6, 2, 2, 1, "lens"), (-1.7, 14.5, -3.6, 2, 2, 1, "lens")])
    m.part("eyes", "head", 0, 15.1, -3.0)
    for s, sfx in ((1, "_l"), (-1, "_r")):
        m.part("eye" + sfx, "eyes", 1.6 * s, 15.1, -3.0, [(1.6 * s, 14.6, -3.0, 1, 1, 1, "eye")])
        m.part("brow" + sfx, "head", 1.7 * s, 16.85, -3.15, [(1.7 * s, 16.6, -3.15, 2, 0.5, 0.3, "brow")])
    m.part("mouth", "head", 0, 13.85, -3.1, [(0, 13.6, -3.1, 1.6, 0.5, 0.2, "mouth")])
    for s, sfx in ((1, "_l"), (-1, "_r")):
        m.part("mouth" + sfx, "head", 0.8 * s, 13.85, -3.11, [(1.1 * s, 13.6, -3.11, 0.6, 0.5, 0.2, "mouth")])
    # arms: shoulder, elbow, wrist; the left palm faces the body, the right hand closes round the staff in front of it
    for s, sfx in ((1, "_l"), (-1, "_r")):
        m.part("arm" + sfx, "chest", 4.6 * s, 12, 0, [(4.6 * s, 9.5, 0, 3, 3, 3, "dress")])
        m.part("forearm" + sfx, "arm" + sfx, 4.6 * s, 9.5, 0, [(4.6 * s, 6.5, 0, 2.6, 3, 2.6, "dress")])
        m.part("hand" + sfx, "forearm" + sfx, 4.6 * s, 6.5, 0, [(4.6 * s, 5.3, 0, 2, 1.2, 1.6, "skin_old")])
    for i, z in enumerate((-0.6, -0.2, 0.2, 0.6)):  # left: index to little finger, front to back
        m.part(f"finger_l{i}", "hand_l", 4.3, 5.3, z, [(4.3, 3.9, z, 0.4, 1.4, 0.35, "skin_old")], rot=(-6 + 4 * i, 0, 16))
    m.part("thumb_l", "hand_l", 4.0, 5.9, -0.85, [(4.0, 4.9, -0.85, 0.45, 1.0, 0.4, "skin_old")], rot=(-20, 0, 24))
    for i, x in enumerate((-4.05, -4.45, -4.85, -5.25)):  # right: wrapped round the staff
        m.part(f"finger_r{i}", "hand_r", x, 5.3, -0.6, [(x, 3.9, -0.6, 0.35, 1.4, 0.4, "skin_old")], rot=(-72, 0, 0))
    m.part("thumb_r", "hand_r", -5.5, 5.9, -0.4, [(-5.5, 4.9, -0.4, 0.4, 1.0, 0.45, "skin_old")], rot=(-40, 0, -20))
    m.part("staff", "forearm_r", -5.2, 6, -2, [(-5.2, 0, -2, 2, 29, 2, "wood_dark")])
    wcx, wcy = -5.2, 30.0
    wheel = [(wcx, wcy - 1.5, -2, 3, 3, 3, "wood_dark"), (wcx, wcy - 0.5, -2, 12, 1, 1, "wood"), (wcx, wcy - 6, -2, 1, 12, 1, "wood"),
             (wcx, wcy + 5.5, -2, 5, 1, 2, "wood"), (wcx, wcy - 6.5, -2, 5, 1, 2, "wood"), (wcx + 6, wcy - 2.5, -2, 1, 5, 2, "wood"), (wcx - 6, wcy - 2.5, -2, 1, 5, 2, "wood"),
             (wcx + 4.2, wcy + 3.7, -2, 2, 2, 2, "wood"), (wcx - 4.2, wcy + 3.7, -2, 2, 2, 2, "wood"), (wcx + 4.2, wcy - 5.7, -2, 2, 2, 2, "wood"), (wcx - 4.2, wcy - 5.7, -2, 2, 2, 2, "wood")]
    m.part("wheel", "staff", wcx, wcy, -2, wheel)
    for i in range(10):
        a = i * math.pi * 2 / 10 + math.pi / 2
        sx, sy = wcx + math.cos(a) * 8.0, wcy + math.sin(a) * 8.0
        axis = "y" if abs(math.cos(a)) > 0.7 else "x"
        m.part(f"spool_{i}", "wheel", sx, sy, -2, spool_boxes(sx, sy, -2, axis))
    models[m.id] = m

    # ---- 01 the Sewing Arachnid: a cracked porcelain doll rising from a thorax of spools, on eight needle-pointed bone legs
    m = Model("sewing_arachnid")
    m.part("body", None, 0, 9, 0, [(0, 6, 0, 10, 3, 10, "wood"), (0, 9, 0, 8, 1, 8, "brass")])
    m.part("torso", "body", 0, 14, 0, [(0, 10, 0, 6, 5, 4, "porc_doll"), (0, 10.5, -2.2, 4, 3, 1, "stitch_chest")])
    m.part("deco", "body", 0, 10, 0, [*spool_boxes(-5.5, 9.5, 1, "y"), *spool_boxes(5.5, 9.5, -2, "y"), *spool_boxes(0, 11, 4.5, "x")])
    m.part("needles", "body", 0, 14, 3, [(-3, 10, 3, 1, 10, 1, "steel"), (3, 10, 4, 1, 9, 1, "steel"), (0, 10, 5, 1, 12, 1, "steel"), (-3, 19, 3, 1, 1, 1, "brass")])
    m.part("head", "body", 0, 15, 0, [(0, 15, 0, 6, 6, 6, "doll_head"), (0, 21, 0, 6, 1, 6, "hair"), (0, 15.5, 3, 6, 5, 1, "hair"), (0, 20, -3, 6, 1, 1, "hair"),
                                    (3, 17, 0, 1, 3, 4, "hair"), (-3, 17, 0, 1, 3, 4, "hair")])
    lr(m, "arm", "body", 3.5, 15, 0, lambda s: [(4.5 * s, 9, 0, 2, 6, 2, "porc_doll"), (4.5 * s, 6, 0, 2, 3, 2, "porc_doll")])
    m.part("abdomen", "body", 0, 8, 4, [(0, 3, 8, 8, 7, 8, "yarn"), (0, 10, 8, 2, 2, 2, "brass")])
    reach = (12, 15, 15, 12)
    for i, z in enumerate((-5.0, -1.7, 1.7, 5.0)):
        lr(m, f"leg{i}", "body", 4, 9, z, lambda s, z=z, r=reach[i]: [((4 + r) / 2 * s, 8.5, z, r - 3, 1.5, 1.5, "bone"), (r * s, 3, z, 1.5, 6, 1.5, "bone"), (r * s, 0, z, 1, 4, 1, "steel")])
    models[m.id] = m

    # ---- 02 the Porcelain Hound: a china dog painted with blue flowers, a gap in its flank, brass-gold teeth
    m = Model("porcelain_hound")
    m.part("body", None, 0, 11, 0, [(0, 8, 1, 7, 6, 11, "porc_floral"), (0, 8.5, -5, 7, 7, 4, "porc_floral"), (0, 8, 6, 6, 6, 3, "porc_floral"),
                                   (3.6, 8.5, 1, 1, 4, 5, "wound"), (0, 14, -2, 1, 1, 8, "porc_seam")])
    m.part("neck", "body", 0, 13, -6, [(0, 12, -7, 4, 6, 4, "porc_floral")])
    m.part("head", "body", 0, 17, -8, [(0, 14.5, -11, 6, 5, 6, "porc_floral"), (0, 14.5, -15.5, 4, 3, 5, "porc_floral"), (0, 16.5, -18.4, 2, 2, 1, "nose"),
                                      (2.2, 17, -14.2, 1, 1, 1, "eye"), (-2.2, 17, -14.2, 1, 1, 1, "eye"), (2, 19.5, -9.5, 2, 4, 1, "porc_floral"), (-2, 19.5, -9.5, 2, 4, 1, "porc_floral"),
                                      (1.5, 13.5, -16.5, 1, 1, 2, "gold"), (-1.5, 13.5, -16.5, 1, 1, 2, "gold")])
    m.part("jaw", "head", 0, 14.5, -12, [(0, 12.5, -15, 4, 2, 6, "porc_floral"), (1.5, 14.5, -16.5, 1, 1, 2, "gold"), (-1.5, 14.5, -16.5, 1, 1, 2, "gold"), (0, 13.5, -14, 3, 1, 3, "wound")])
    for name, x, z in (("leg_fl", 2.3, -5), ("leg_fr", -2.3, -5), ("leg_bl", 2.3, 5.5), ("leg_br", -2.3, 5.5)):
        m.part(name, "body", x, 8, z, [(x, 5, z, 3, 3, 3, "porc_floral"), (x, 1, z, 2, 4, 2, "porc_floral"), (x, 0, z - 1, 3, 1, 4, "porc_floral")])
    m.part("tail", "body", 0, 14, 7, [(0, 13, 8, 2, 2, 4, "porc_floral"), (0, 14, 11, 1, 3, 2, "porc_floral"), (0, 15.5, 12.5, 1, 1, 1, "gold")])
    models[m.id] = m

    # ---- 03 the Hollow Bell: a verdigris bronze bell with ornate relief, a stitched skeleton hanging in it as a clapper
    m = Model("hollow_bell")
    tiers = [(14, 3, 22, "bell_rim"), (17, 3, 20, "bell_flare"), (20, 5, 18, "bell_panel"), (25, 5, 16, "bell_panel"), (30, 3, 13, "bell_shoulder"),
             (33, 2, 9, "bell_rim"), (35, 2, 6, "bell_handle"), (37, 2, 3, "bell_handle")]
    m.part("body", None, 0, 26, 0, [(0, hb, 0, wd, ht, wd, mt) for hb, ht, wd, mt in tiers] + [(0, 13.5, 0, 18, 1, 18, "bell_inside")])
    m.part("clapper", "body", 0, 17, 0, [(0, 14, 0, 1, 3, 1, "thread"), (0, 9, 0, 5, 5, 5, "skull_doll"), (0, 4, 0, 4, 5, 3, "ribs"), (3, 4.5, 0, 1, 4, 1, "bone"),
                                         (-3, 4.5, 0, 1, 4, 1, "bone"), (1, 0, 0, 1, 4, 1, "bone"), (-1, 0, 0, 1, 4, 1, "bone"), (0, 12, -2.6, 3, 1, 1, "stitch_chest")])
    models[m.id] = m

    # ---- 04 the Patchwork Stag: a stag with a deer skull and bone antlers, a sewn patchwork body and thin wooden stilt legs
    m = Model("patchwork_stag")
    m.part("body", None, 0, 28, 0, [(0, 22, 0, 9, 10, 17, "patchwork"), (0, 32, 0, 2, 1, 15, "bone"), (0, 23, 7, 8, 8, 3, "patchwork"), (4.6, 25, 0, 1, 5, 5, "gear"),
                                   (0, 21.5, -4, 7, 1, 7, "leather_dark")])
    m.part("neck", "body", 0, 31, -8, [(0, 31, -9, 5, 11, 5, "patchwork"), (0, 40, -9, 6, 2, 6, "leather_dark")])
    m.part("head", "neck", 0, 43, -9, [(0, 43, -11, 6, 6, 7, "skull"), (0, 41.5, -17, 4, 4, 6, "skull"), (3, 45, -13.8, 1, 2, 1, "eye"), (-3, 45, -13.8, 1, 2, 1, "eye"),
                                      (0, 41, -20.2, 2, 1, 1, "iron")])
    m.part("jaw", "head", 0, 41.5, -14, [(0, 40, -17, 3, 1, 6, "skull")])
    lr(m, "antler", "head", 2.5, 47, -10.5, lambda s: [(2.5 * s, 47, -10.5, 2, 9, 2, "antler"), (5.5 * s, 52, -10.5, 4, 2, 2, "antler"), (7.5 * s, 53, -10.5, 2, 6, 2, "antler"),
                                                      (3.5 * s, 56, -10.5, 1, 4, 1, "antler"), (3 * s, 51, -13.5, 1, 1, 4, "antler"), (3.5 * s, 50, -7.5, 1, 1, 3, "antler")])
    for name, x, z in (("leg_fl", 3.5, -6.5), ("leg_fr", -3.5, -6.5), ("leg_bl", 3.5, 6.5), ("leg_br", -3.5, 6.5)):
        m.part(name, "body", x, 28, z, [(x, 18, z, 3, 10, 3, "leather_wrap"), (x, 17, z, 4, 2, 4, "patchwork"), (x, 3, z, 2, 14, 2, "wood_stilt"), (x, 0, z, 3, 3, 3, "iron")])
    m.part("tail", "body", 0, 30, 9, [(0, 26, 10, 2, 5, 2, "patchwork"), (0, 24, 10, 3, 2, 3, "hair_dark")])
    models[m.id] = m

    # ---- 05 the Stitched Rook: a huge crow with a bird skull for a head, a wicker ribcage and tattered folded wings
    m = Model("stitched_rook")
    m.part("body", None, 0, 19, 0, [(0, 11, 1, 11, 11, 13, "feather"), (0, 11, -6.5, 8, 8, 2, "wicker_rib"), (0, 22, 1, 9, 2, 11, "feather_light"), (0, 14, 8, 8, 6, 3, "feather")])
    m.part("neck", "body", 0, 22, -5, [(0, 20, -6, 5, 6, 5, "feather")])
    m.part("head", "body", 0, 26, -7, [(0, 24, -8.5, 7, 7, 7, "skull_bird"), (4, 27, -9.5, 1, 2, 1, "eye"), (-4, 27, -9.5, 1, 2, 1, "eye"), (0, 31, -8.5, 4, 1, 4, "feather")])
    m.part("beak_top", "head", 0, 27, -12, [(0, 26.5, -16.5, 3, 2, 9, "beak"), (0, 25.5, -21, 2, 2, 2, "beak")])
    m.part("beak_bottom", "head", 0, 26, -12, [(0, 24.5, -16, 2, 1, 8, "beak")])
    for sign, suffix in ((1, "_l"), (-1, "_r")):
        m.part("wing" + suffix, "body", 6 * sign, 22, 0, [(8 * sign, 20, -3, 3, 3, 7, "bone_dark"), (8 * sign, 12, 1, 3, 10, 12, "wing_cloth"), (10 * sign, 11, 2, 1, 10, 10, "feather")])
        m.part("wing_tip" + suffix, "wing" + suffix, 8 * sign, 12, 1, [(8.5 * sign, 3, 2, 3, 9, 10, "wing_cloth"), (10.5 * sign, 2, 3, 1, 9, 8, "feather_light"), (8 * sign, 0, 4, 2, 4, 5, "feather")])
    m.part("tail", "body", 0, 16, 7, [(0, 8, 10, 6, 2, 10, "feather"), (3, 9, 11, 2, 1, 8, "feather_light"), (-3, 9, 11, 2, 1, 8, "feather_light")])
    lr(m, "leg", "body", 2.5, 11, 1, lambda s: [(2.5 * s, 4, 1, 2, 7, 2, "bone_dark"), (2.5 * s, 0, -2, 3, 1, 6, "iron"), (4 * s, 0, -3.5, 1, 1, 3, "iron"), (1 * s, 0, -3.5, 1, 1, 3, "iron")])
    models[m.id] = m

    # ---- 06 the Hollow Bride: a tall veiled doll in a torn gown, a bleeding hollow in her chest, eight thin arms with needles
    m = Model("hollow_bride")
    m.part("body", None, 0, 16, 0, [(0, 16, 0, 7, 12, 4, "gown"), (0, 26, 0, 9, 2, 5, "gown"), (0, 17, -2.4, 3, 8, 1, "wound"), (0, 28, 0, 2, 2, 2, "arm_porc")])
    m.part("skirt", "body", 0, 16, 0, [(0, 13, 0, 9, 3, 7, "gown"), (0, 10, 0, 11, 3, 9, "gown"), (0, 7, 0, 13, 3, 11, "gown"), (0, 4, 0, 16, 3, 14, "gown"), (0, 2, 0, 18, 2, 16, "gown"),
                                      (0, 0, 0, 21, 2, 19, "gown_hem")])
    m.part("head", "body", 0, 30, 0, [(0, 30, 0, 5, 7, 5, "blank_face"), (0, 37, 0, 5, 1, 5, "lace"), (0, 36, -2.8, 5, 1, 1, "lace")])
    m.part("veil", "head", 0, 37, 3, [(0, 12, 3, 8, 25, 1, "lace"), (4, 20, 0, 1, 17, 4, "lace"), (-4, 20, 0, 1, 17, 4, "lace"), (0, 6, 4, 10, 6, 1, "lace")])
    reach = (6, 8, 10, 12)
    lift = (7, 5, 3, 1)
    for i in range(8):
        s = 1 if i % 2 == 0 else -1
        k = i // 2
        lvl = (27, 24, 21, 18)[k]
        r, lf = reach[k], lift[k]
        m.part(f"arm_{i}", "body", 3.5 * s, lvl, 0, [((3.5 + r) / 2 * s, lvl - 1, 0, r - 3, 1.5, 1.5, "arm_porc"), (r * s, lvl - 1, 0, 1.5, lf + 1, 1.5, "arm_porc"),
                                                    (r * s, lvl + lf, 0, 2, 2, 2, "arm_porc")] + ([(r * s + 1 * s, lvl + lf - 3, -1.5, 1, 9, 1, "steel")] if i in (1, 2, 5, 6) else []))
    models[m.id] = m

    # ---- 07 the Skirted Centipede: three porcelain masks on a dress form, then skirted dress forms in a row, a red spool at the tail
    m = Model("skirted_centipede")
    m.part("body", None, 0, 9, -34, [(0, 0, -34, 15, 5, 12, "skirt_a"), (0, 5, -34, 7, 1, 5, "dress_form"), (0, 6, -34, 9, 7, 6, "dress_form"), (0, 13, -35, 4, 2, 3, "dress_form"),
                                    (0, 15, -36, 7, 7, 5, "mask_neutral"), (-6, 14, -35, 6, 7, 5, "mask_frown"), (6, 14, -35, 6, 7, 5, "mask_smile"),
                                    (8, 0, -36, 2, 2, 2, "skin_doll"), (-8, 0, -36, 2, 2, 2, "skin_doll"), (8, 0, -31, 2, 2, 2, "skin_doll"), (-8, 0, -31, 2, 2, 2, "skin_doll")])
    prev = "body"
    for i in range(1, 7):
        z = -24 + 10 * (i - 1)
        skirt = ("skirt_b", "skirt_c", "skirt_a")[i % 3]
        boxes = [(0, 0, z, 14, 5, 10, skirt), (0, 5, z, 6, 1, 5, "dress_form"), (0, 6, z, 8, 7, 5, "dress_form"), (0, 13, z, 3, 3, 3, "dress_form"),
                 (7.5, 0, z - 2, 2, 2, 2, "skin_doll"), (-7.5, 0, z - 2, 2, 2, 2, "skin_doll"), (7.5, 0, z + 2, 2, 2, 2, "skin_doll"), (-7.5, 0, z + 2, 2, 2, 2, "skin_doll")]
        if i == 6:
            boxes += [(0, 5, z + 7, 1, 11, 1, "thread"), *spool_boxes(0, 20, z + 7, "y"), (0, 19, z + 7, 5, 5, 5, "spool")]
        prev = m.part(f"seg_{i}", prev, 0, 9, z, boxes)
    models[m.id] = m

    # ---- 08 the Root Mother: a tree-bodied mother with a carved wooden face, huge mossy arms, a cage holding two dolls, a skirt of roots
    m = Model("root_mother")
    m.part("body", None, 0, 26, 0, [(0, 16, 0, 12, 8, 9, "trunk"), (0, 24, 0, 16, 14, 11, "trunk"), (0, 36, 0, 18, 3, 12, "moss"), (0, 22, -5.4, 10, 13, 1, "cavity")])
    m.part("head", "body", 0, 39, 0, [(0, 39, -0.5, 10, 9, 9, "wood_face"), (0, 48, 0, 10, 2, 9, "moss"), (3, 48, 0, 1, 4, 1, "bark_dark"), (-3, 49, 1, 1, 3, 1, "bark_dark")])
    m.part("cage", "body", 0, 34, -6.5, [(5, 21, -7.4, 1, 14, 1, "wicker"), (-5, 21, -7.4, 1, 14, 1, "wicker"), (0, 21, -7.4, 11, 1, 1, "wicker"), (0, 27, -7.4, 11, 1, 1, "wicker"),
                                       (0, 33, -7.4, 11, 1, 1, "wicker"), (0, 28, -7.4, 1, 6, 1, "wicker"), (-2.5, 23, -5.6, 3, 5, 2, "doll_red"), (-2.5, 28, -5.6, 3, 3, 2, "porc_doll"),
                                       (2.5, 23, -5.6, 3, 5, 2, "doll_blue"), (2.5, 28, -5.6, 3, 3, 2, "porc_doll")])
    lr(m, "arm", "body", 10, 37, 0, lambda s: [(10.5 * s, 27, 0, 6, 10, 6, "bark_arm"), (10.5 * s, 35, 0, 9, 3, 9, "moss")])
    lr(m, "forearm", "arm_l", 10.5, 27, 0, lambda s: [(10.5 * s, 14, 0, 5, 13, 5, "bark_arm"), (10.5 * s, 9, 0, 7, 5, 7, "moss"), (10.5 * s, 10, -3.6, 5, 4, 1, "bark_dark")])
    m.parts[-1]["parent"] = "arm_r"  # the right forearm hangs from the right arm
    m.parts[-2]["parent"] = "arm_l"
    for i, (x, z) in enumerate(((5, 3), (-5, 3), (5, -3), (-5, -3))):
        m.part(f"leg_{i}", "body", x, 16, z, [(x, 0, z, 5, 16, 5, "bark")])
    m.part("skirt", "body", 0, 14, 0, [(0, 0, 0, 30, 3, 6, "root"), (0, 0, 0, 6, 3, 30, "root"), (0, 3, 0, 24, 3, 5, "root"), (0, 3, 0, 5, 3, 24, "root"), (0, 6, 0, 19, 5, 19, "root"),
                                     (0, 11, 0, 15, 5, 14, "root")])
    for i, (x, z, ln, alongx) in enumerate(((13, 11, 9, True), (-13, 11, 9, True), (12, -11, 8, False), (-12, -11, 8, False))):
        m.part(f"root_{i}", "body", x, 8, z, [(x + (4 if x > 0 else -4) * (1 if alongx else 0), 3, z, ln if alongx else 3, 3, 3 if alongx else ln, "root")])
    models[m.id] = m

    # ---- 09 the Patchwork Executioner: a sack head, riveted patches, a chain belt and one colossal wooden and iron right arm
    m = Model("patchwork_executioner")
    m.part("body", None, 0, 30, 0, [(0, 26, 0, 13, 4, 9, "leather_dark"), (0, 30, 0, 17, 18, 11, "leather_patch"), (0, 27, -4.8, 14, 2, 1, "chain"), (0, 36, -5.8, 11, 9, 1, "iron_plate"),
                                   (0, 44, 0, 15, 2, 12, "iron_plate")])
    m.part("neck", "body", 0, 48, 0, [(0, 46, 0, 8, 3, 8, "stitch_neck")])
    m.part("head", "body", 0, 49, -1, [(0, 49, -1, 10, 10, 10, "sack"), (0, 59, -1, 4, 3, 4, "sack"), (0, 48.5, -1, 11, 1, 11, "rope")])
    m.part("arm_l", "body", -10.5, 46, 0, [(-10.5, 44, 0, 8, 3, 9, "iron_plate"), (-10.5, 28, 0, 5, 17, 5, "leather"), (-10.5, 23, 0, 6, 5, 6, "skin_old")])
    m.part("arm_r", "body", 11, 46, 0, [(11.5, 45, 0, 14, 4, 13, "iron_plate"), (11.5, 32, 0, 10, 13, 10, "leather_patch")])
    m.part("forearm_r", "arm_r", 11.5, 32, 0, [(11.5, 17, 0, 12, 15, 11, "wood_slab"), (11.5, 27, 0, 13, 3, 12, "iron_plate"), (11.5, 8, 0, 14, 9, 12, "iron_gauntlet"),
                                             (6.5, 20, -3, 2, 8, 2, "bone"), (6.5, 22, 2, 2, 6, 2, "bone"), (11.5, 17, -6, 6, 10, 1, "iron_plate")])
    lr(m, "leg", "body", 4.5, 28, 0, lambda s: [(4.5 * s, 16, 0, 7, 12, 7, "leather_dark"), (4.5 * s, 16, -3.6, 5, 4, 1, "iron"), (4.5 * s, 4, 0, 7, 12, 7, "iron_plate"), (4.5 * s, 0, -1, 8, 4, 10, "boot")])
    models[m.id] = m

    # ---- 10 the Cradle Giant: a thin skeleton in rags with a wicker cage for a head, a glow inside, six needle arms
    m = Model("cradle_giant")
    body = [(0, 32, 0, 8, 3, 5, "bone"), (0, 35, 1.5, 2, 18, 2, "bone"), (0, 46, 0, 14, 2, 5, "bone"), (0, 36, -3, 1, 9, 1, "bone"), (7.5, 40, 1, 1, 9, 6, "rag"), (-7.5, 40, 1, 1, 9, 6, "rag"),
            (0, 38, 3, 14, 8, 1, "rag")]
    for k in range(5):
        hb = 36 + 2 * k
        body += [(0, hb, -2.5, 9, 1, 1, "bone"), (4.5, hb, 0, 1, 1, 5, "bone"), (-4.5, hb, 0, 1, 1, 5, "bone")]
    m.part("body", None, 0, 34, 0, body)
    m.part("skirt", "body", 0, 33, 0, [(0, 14, -3, 10, 18, 1, "rag"), (0, 14, 3, 10, 18, 1, "rag"), (5, 16, 0, 1, 16, 5, "rag"), (-5, 16, 0, 1, 16, 5, "rag"), (2, 12, -3.5, 3, 4, 1, "rag")])
    m.part("neck", "body", 0, 48, 0, [(0, 48, 0, 2, 3, 2, "bone")])
    cradle = [(0, 51, 0, 12, 1, 12, "wicker"), (0, 62, 0, 12, 1, 12, "wicker"), (0, 57, -5.5, 12, 1, 1, "wicker"), (0, 57, 5.5, 12, 1, 1, "wicker"), (5.5, 57, 0, 1, 1, 10, "wicker"),
              (-5.5, 57, 0, 1, 1, 10, "wicker"), (0, 53, 0, 7, 7, 7, "glow_core"), (0, 63, 0, 4, 2, 4, "wicker")]
    for px_, pz_ in ((5.5, 5.5), (-5.5, 5.5), (5.5, -5.5), (-5.5, -5.5)):
        cradle.append((px_, 52, pz_, 1, 10, 1, "wicker"))
    cradle += [(0, 52, -5.5, 10, 10, 1, "wicker_mesh"), (0, 52, 5.5, 10, 10, 1, "wicker_mesh"), (5.5, 52, 0, 1, 10, 10, "wicker_mesh"), (-5.5, 52, 0, 1, 10, 10, "wicker_mesh")]
    m.part("cradle", "body", 0, 51, 0, cradle)
    lr(m, "arm_u", "body", 8, 46, 0, lambda s: [(8 * s, 32, 0, 3, 14, 3, "bone"), (8 * s, 18, 0, 2, 14, 2, "bone"), (8 * s, 15, 0, 3, 3, 3, "bone"),
                                                (8 * s, 3, -1.5, 1, 12, 1, "steel")] + ([(9 * s, 4, 0.5, 1, 8, 1, "steel"), (7 * s, 4, 0.5, 1, 8, 1, "steel")] if s < 0 else []))
    m.parts[-2]["name"], m.parts[-1]["name"] = "arm_ul", "arm_ur"
    lr(m, "arm_m", "body", 10.5, 42, 0, lambda s: [(10.5 * s, 26, 0, 2, 16, 2, "bone"), (10.5 * s, 12, 0, 1, 14, 1, "steel")])
    m.parts[-2]["name"], m.parts[-1]["name"] = "arm_ml", "arm_mr"
    lr(m, "arm_l", "body", 13, 38, 0, lambda s: [(13 * s, 24, 0, 2, 14, 2, "bone"), (13 * s, 10, 0, 1, 14, 1, "steel")])
    m.parts[-2]["name"], m.parts[-1]["name"] = "arm_ll", "arm_lr"
    lr(m, "leg", "body", 4, 32, 0, lambda s: [(4 * s, 18, 0, 3, 14, 3, "bone"), (4 * s, 17, 0, 4, 2, 4, "bone"), (4 * s, 3, 0, 2, 14, 2, "bone"), (4 * s, 0, -1.5, 4, 2, 6, "bone")])
    models[m.id] = m
    return models


# ---------------------------------------------------------------------------------------------------------------
def ceil(v):
    return int(-(-v // 1))


def footprint(w, h, d):
    return 2 * (ceil(d) + ceil(w)), ceil(d) + ceil(h)


def pack(boxes):
    items = sorted(boxes, key=lambda b: (-footprint(b[4], b[5], b[6])[1], -footprint(b[4], b[5], b[6])[0]))
    layout = {}
    x = y = row_h = 0
    for name, cx, hb, cz, w, h, d, mat in items:
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


def faces_of(u, v, w, h, d):
    w, h, d = ceil(w), ceil(h), ceil(d)
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def region_pixels(u, v, w, h, d):
    for name, (fx0, fy0, fw, fh) in faces_of(u, v, w, h, d).items():
        for j in range(fh):
            for i in range(fw):
                yield name, fx0 + i, fy0 + j, i, j, fw, fh


# ---------------------------------------------------------------------------------------------------------------
# Materials. Each painter gets a context and returns a colour, or (colour, glow colour) for an emissive pixel, or CLEAR.
GOLD = (222, 176, 72, 255)
GOLD_D = (150, 108, 40, 255)
GOLD_L = (250, 224, 140, 255)
STEEL = (196, 202, 212, 255)
STEEL_D = (128, 136, 150, 255)
STEEL_DD = (82, 88, 100, 255)
RED_IN = (104, 18, 26, 255)
RED_IN_D = (52, 8, 14, 255)
FLOWER = (44, 80, 150, 255)
FLOWER_D = (28, 52, 110, 255)
FLOWER_L = (112, 152, 206, 255)
VERD = (62, 132, 110, 255)
VERD_L = (112, 180, 150, 255)
VERD_D = (34, 86, 72, 255)
BRONZE_B = (146, 100, 54, 255)
BONE_T = (222, 204, 170, 255)
BONE_TD = (172, 150, 114, 255)
BONE_TDD = (118, 98, 72, 255)
LEATHER = (116, 84, 58, 255)
LEATHER_D = (78, 56, 40, 255)
LEATHER_L = (146, 110, 78, 255)
CANVAS = (160, 138, 104, 255)
GREY_C = (108, 104, 100, 255)
RED_C = (122, 52, 46, 255)
OLIVE_C = (96, 104, 70, 255)
STITCH = (226, 206, 160, 255)
WICKER = (158, 114, 62, 255)
WICKER_D = (112, 78, 40, 255)
WICKER_DD = (70, 46, 24, 255)
FEATHER = (30, 30, 40, 255)
FEATHER_L = (62, 68, 96, 255)
FEATHER_LL = (96, 104, 134, 255)
GOWN = (226, 214, 190, 255)
GOWN_D = (186, 170, 142, 255)
GOWN_STAIN = (150, 100, 88, 255)
SACK = (176, 150, 112, 255)
SACK_D = (136, 112, 80, 255)
IRON = (110, 110, 118, 255)
IRON_D = (66, 66, 76, 255)
IRON_L = (150, 150, 160, 255)
RUST = (126, 72, 44, 255)
CLOAK = (70, 88, 56, 255)
CLOAK_D = (44, 58, 38, 255)
CLOAK_L = (104, 130, 72, 255)
DRESS = (104, 86, 74, 255)
DRESS_D = (72, 58, 50, 255)
SKIN_OLD = (200, 172, 150, 255)
SKIN_OLD_D = (160, 130, 112, 255)
HAIR_G = (178, 178, 172, 255)
HAIR_GD = (128, 128, 124, 255)
HAIR_B = (96, 62, 42, 255)
HAIR_BD = (62, 40, 28, 255)
DOLL_PORC = (236, 226, 216, 255)
DOLL_PORC_D = (206, 194, 184, 255)
BLUSH = (226, 150, 140, 255)
DARK_EYE = (28, 22, 24, 255)
SPOOL_R = (216, 28, 48, 255)
SPOOL_RD = (150, 18, 34, 255)
SPOOL_RL = (255, 110, 120, 255)

MATS = {}


def mat(*names):
    def deco(fn):
        for n in names:
            MATS[n] = fn
        return fn
    return deco


def eye_cols(w):
    return (1, 2, w - 3, w - 2) if w >= 8 else (1, w - 2)


class Ctx:
    __slots__ = ("face", "x", "y", "i", "j", "fw", "fh", "n", "seed", "w", "h", "d")

    def __init__(self, face, x, y, i, j, fw, fh, n, seed, w, h, d):
        self.face, self.x, self.y, self.i, self.j, self.fw, self.fh, self.n, self.seed, self.w, self.h, self.d = face, x, y, i, j, fw, fh, n, seed, w, h, d

    @property
    def side(self):
        return self.face in ("front", "back", "left", "right")

    @property
    def edge(self):
        return self.i == 0 or self.j == 0 or self.i == self.fw - 1 or self.j == self.fh - 1


def pick(n, *cols):
    """A colour from the list by a 0..99 noise value (earlier entries are the most common)."""
    return cols[min(len(cols) - 1, n * len(cols) // 100)]


def blend(a, b, t):
    return tuple(int(a[k] + (b[k] - a[k]) * t) for k in range(3)) + (255,)


def dark(c, f):
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), c[3])


@mat("wood", "wood_dark")
def _wood(c):
    base = WOOD if c.w and True else WOOD
    col = WOOD_L if c.n > 86 else WOOD_D if c.n < 22 else WOOD
    if c.side and (c.i * 2 + c.j) % 7 == 0:
        col = WOOD_D
    return col


@mat("burlap")
def _burlap(c):
    weave = (c.i + c.j) % 2 == 0
    return BURLAP_L if (weave and c.n > 70) else BURLAP if weave else BURLAP_D


@mat("lace")
def _lace(c):
    col = LACE if (c.i + c.j) % 3 else LACE_D
    if c.side and c.n < 14:
        return LACE_D if c.fh <= 8 or c.j <= c.fh // 2 else CLEAR
    return col


@mat("thread")
def _thread(c):
    return THREAD if (c.i + c.j) % 2 == 0 else THREAD_D


@mat("eye")
def _eye(c):
    if c.fw >= 2 and c.i == c.fw // 2 and c.j == c.fh // 2:
        return OUT
    col = EYE if (c.i + c.j) % 2 == 0 else EYE_H
    return col, EYE


@mat("spool")
def _spool(c):
    wind = (c.i + c.j) % 3
    col = SPOOL_R if wind else SPOOL_RD
    if c.n > 88:
        col = SPOOL_RL
    return col, col


@mat("brass", "gold")
def _brass(c):
    col = GOLD_L if c.n > 82 else GOLD if c.n > 20 else GOLD_D
    if c.edge:
        col = GOLD_D
    return col


@mat("silver")
def _silver(c):
    return STEEL if c.n > 40 else STEEL_D


@mat("lens")
def _lens(c):
    return (STEEL_D if c.edge else (200, 214, 226, 255)) if c.face == "front" else STEEL_D


@mat("steel")
def _steel(c):
    if c.side and c.i == 0:
        return STEEL_D
    return STEEL if (c.i + c.j) % 5 else STEEL_D


@mat("iron")
def _iron(c):
    col = IRON_D if c.n < 30 else IRON
    if c.n > 92:
        col = RUST
    return col


@mat("iron_plate")
def _iron_plate(c):
    col = IRON_L if c.n > 86 else IRON_D if c.n < 18 else IRON
    if c.n > 96:
        col = RUST
    if c.edge:
        col = IRON_D
    if c.side and c.i % 6 == 2 and c.j % 6 == 2 and c.fw > 5 and c.fh > 5:
        col = STEEL  # rivets
    return col


@mat("iron_gauntlet")
def _gauntlet(c):
    col = _iron_plate(c)
    if c.side and c.j == c.fh * 2 // 3:
        col = IRON_D
    if c.face == "front" and c.j > c.fh * 2 // 3 and c.i % 4 == 1:
        col = IRON_D  # knuckles
    return col


@mat("chain")
def _chain(c):
    return IRON_L if (c.i + c.j) % 2 == 0 else IRON_D


@mat("bone")
def _bone(c):
    col = BONE_T if c.n > 28 else BONE_TD
    if c.side and (c.j == 0 or c.j == c.fh - 1) and c.fh > 4:
        col = BONE_TD  # the joint
    if c.i == 0 and c.side:
        col = BONE_TD
    return col


@mat("bone_dark")
def _bone_dark(c):
    return BONE_TD if c.n > 30 else BONE_TDD


@mat("bone_foot")
def _bone_foot(c):
    return _bone(c)


@mat("porc_doll", "arm_porc")
def _porc_doll(c):
    col = DOLL_PORC if c.n > 20 else DOLL_PORC_D
    if c.side and c.j == c.fh // 2 and c.fh > 4:
        col = DOLL_PORC_D  # the jointed seam
    if c.n < 4:
        col = CRACK
    return col


@mat("stitch_chest")
def _stitch_chest(c):
    if c.i == c.j or c.i + c.j == c.fw - 1:
        return CRACK
    return (200, 176, 140, 255) if c.n > 30 else (170, 146, 112, 255)


@mat("doll_head")
def _doll_head(c):
    col = DOLL_PORC if c.n > 20 else DOLL_PORC_D
    if c.face == "front":
        w, h = c.fw, c.fh
        ey = h * 2 // 5
        if c.j in (ey, ey + 1) and c.i in eye_cols(w):
            col = DARK_EYE  # big hollow eyes
            if c.j == ey and c.i in ((1, w - 3) if w >= 8 else (1, w - 2)):
                col = (250, 250, 250, 255)
        elif c.j == ey + 2 and (c.i == 0 or c.i == w - 1):
            col = BLUSH
        elif c.j == h - 2 and w // 2 - 1 <= c.i <= w // 2:
            col = (170, 40, 50, 255)  # the little mouth
        elif (c.i + c.j) == w // 2 + 1 and c.j < ey:
            col = CRACK  # a crack from the brow
    elif c.n < 5:
        col = CRACK
    return col


@mat("hair", "hair_dark")
def _hair(c):
    col = HAIR_B if (c.i + c.j // 2) % 3 else HAIR_BD
    return HAIR_BD if c.n > 90 else col


@mat("hair_grey")
def _hair_grey(c):
    return HAIR_G if (c.i + c.j // 2) % 3 else HAIR_GD


@mat("skin_doll")
def _skin_doll(c):
    return DOLL_PORC if c.n > 25 else DOLL_PORC_D


@mat("skin", "skin_old")
def _skin(c):
    return SKIN_OLD if c.n > 25 else SKIN_OLD_D


@mat("face_old")
def _face_old(c):
    col = SKIN_OLD if c.n > 25 else SKIN_OLD_D
    if c.face == "front":
        # the brows and the mouth are parts of their own (they move with her expressions); only the skin is painted here
        h = c.fh
        if c.j == 1 and c.n > 70:
            col = SKIN_OLD_D  # the lines of the brow
        elif c.j >= h // 2 and (c.i == 1 or c.i == c.fw - 2) and c.n > 40:
            col = SKIN_OLD_D  # cheek hollows
        elif c.j == h // 2 and c.n > 70:
            col = SKIN_OLD_D  # wrinkles
    elif c.face == "top":
        col = HAIR_G
    return col


@mat("brow")
def _brow(c):
    return HAIR_GD if c.n > 30 else (98, 98, 94, 255)


@mat("mouth")
def _mouth(c):
    return (112, 58, 56, 255) if c.face in ("front", "top") else (70, 34, 34, 255)


@mat("dress")
def _dress(c):
    col = DRESS if c.n > 20 else DRESS_D
    if c.side and c.i % 3 == 0:
        col = DRESS_D
    return col


@mat("boot")
def _boot(c):
    return LEATHER_D if c.n > 25 else (48, 34, 26, 255)


@mat("cloak")
def _cloak(c):
    col = CLOAK if c.n > 18 else CLOAK_D
    if noise(c.x // 3, c.y // 3, c.seed) > 66:
        col = CLOAK_L if c.n > 40 else (88, 116, 60, 255)  # moss on the wool
    if c.side and c.j == c.fh - 1 and c.n < 50:
        col = CLEAR if c.fh > 6 else CLOAK_D  # a ragged hem
    return col


@mat("porc_floral", "porcelain")
def _porc_floral(c):
    col = PORC if c.n > 22 else PORC_D
    a, b = (c.i + (c.j // 4) * 2) % 4, c.j % 4
    if (a, b) == (1, 1):
        col = FLOWER_D
    elif (a, b) in ((0, 1), (2, 1), (1, 0), (1, 2)):
        col = FLOWER
    elif (a, b) == (3, 3) and c.n < 70:
        col = FLOWER_L  # a leaf
    elif (a + b) % 4 == 3 and c.n < 35:
        col = FLOWER_L  # vines
    if c.edge and c.side:
        col = PORC_D
    if c.n < 3:
        col = CRACK
    return col


@mat("porc_seam")
def _porc_seam(c):
    return PORC_D if c.n > 30 else CRACK


@mat("nose")
def _nose(c):
    return (36, 30, 34, 255) if c.n > 25 else (70, 60, 64, 255)


@mat("wound")
def _wound(c):
    if c.edge:
        return PORC_D if c.n > 40 else CRACK
    col = RED_IN if c.n > 30 else RED_IN_D
    if c.side and c.j % 3 == 1 and 0 < c.i < c.fw - 1:
        col = BONE_T if c.n > 40 else BONE_TD  # ribs showing in the gap
    return col


@mat("yarn")
def _yarn(c):
    d = (c.i + c.j) % 4
    col = (210, 30, 52, 255) if d < 2 else (150, 18, 36, 255) if d == 2 else (244, 90, 100, 255)
    return col


# ---- the bell: verdigris bronze with raised bands and panels
@mat("bell_rim")
def _bell_rim(c):
    col = VERD if c.n > 25 else VERD_D
    if c.side:
        if c.j == c.fh - 1:
            col = BRONZE_B
        elif c.fh >= 3 and c.j == c.fh - 2:
            col = VERD_L
        elif c.fh >= 3 and c.i % 4 == 2 and c.j == 1:
            col = VERD_D  # studs
    return col


@mat("bell_flare")
def _bell_flare(c):
    col = VERD if c.n > 22 else VERD_D
    if c.side:
        if c.i % 5 == 2:
            col = VERD_D
        elif c.i % 5 == 3 and c.n > 40:
            col = VERD_L
        if c.j == c.fh - 1 and c.fh >= 3:
            col = BRONZE_B
    return col


@mat("bell_panel", "bell_shoulder")
def _bell_panel(c):
    col = VERD if c.n > 20 else VERD_D
    if c.n > 92:
        col = BRONZE_B
    if c.side:
        w, h = c.fw, c.fh
        if c.i in (0, w - 1) or c.j in (0, h - 1):
            return BRONZE_B if c.n > 30 else VERD_D
        if c.i in (1, w - 2) or c.j in (1, h - 2):
            return VERD_L
        cx, cy = (w - 1) / 2.0, (h - 1) / 2.0
        dx, dy = (c.i - cx) / max(1.0, w / 2.5), (c.j - cy) / max(1.0, h / 2.5)
        r = dx * dx + dy * dy
        if 0.55 < r < 0.85:
            col = VERD_L  # an oval relief
        elif r <= 0.55 and (c.i + c.j) % 3 == 0:
            col = VERD_D
    return col


@mat("bell_handle")
def _bell_handle(c):
    return BRONZE_B if c.n > 45 else VERD_D if c.n > 15 else VERD


@mat("bell_inside")
def _bell_inside(c):
    return (26, 38, 34, 255) if c.n > 30 else (14, 22, 20, 255)


@mat("skull_doll", "skull_bird")
def _skull(c):
    col = BONE_T if c.n > 28 else BONE_TD
    if c.face == "front":
        w, h = c.fw, c.fh
        ey = h * 2 // 5
        if c.j in (ey, ey + 1) and c.i in eye_cols(w):
            col = DARK_EYE
        elif c.j == ey + 2 and c.i == w // 2:
            col = BONE_TDD  # the nose
        elif c.j == h - 1 and c.i % 2 == 0:
            col = BONE_TDD  # teeth
    return col


@mat("ribs")
def _ribs(c):
    if c.side and c.j % 2 == 1:
        return BONE_TDD
    return BONE_T if c.n > 28 else BONE_TD


# ---- patchwork and leather
@mat("patchwork", "leather_patch")
def _patchwork(c):
    cell = noise(c.x // 5, c.y // 5, c.seed + 3)
    base = (LEATHER, CANVAS, GREY_C, RED_C, OLIVE_C, LEATHER_L)[cell * 6 // 100]
    col = blend(base, dark(base, 0.8), 0.5) if c.n < 28 else base
    if c.side and ((c.i % 5 == 0 and c.j % 2 == 0) or (c.j % 5 == 0 and c.i % 2 == 0)):
        col = STITCH
    if c.edge and c.side:
        col = dark(col, 0.85)
    return col


@mat("leather", "leather_dark", "leather_wrap")
def _leather(c):
    col = LEATHER if c.n > 22 else LEATHER_D
    if c.side and c.j % 5 == 4:
        col = LEATHER_D
    if c.side and c.i % 4 == 0 and c.j % 3 == 0:
        col = STITCH
    return col


@mat("gear")
def _gear(c):
    cx, cy = (c.fw - 1) / 2.0, (c.fh - 1) / 2.0
    dx, dy = c.i - cx, c.j - cy
    r = math.sqrt(dx * dx + dy * dy)
    if c.face in ("left", "right") or c.face == "front":
        if r < 0.8:
            return IRON_L
        if 1.4 < r < 2.2:
            return WOOD_L if c.n > 30 else WOOD
        if r >= 2.2:
            return LEATHER_D
        return WOOD_D
    return LEATHER_D


@mat("skull")
def _deer_skull(c):
    col = (232, 224, 206, 255) if c.n > 30 else (200, 190, 168, 255)
    if c.face == "front":
        w, h = c.fw, c.fh
        if c.j == 1 and (c.i in (0, 1) or c.i in (w - 2, w - 1)) and w >= 5:
            return (24, 18, 18, 255), None
    if c.n < 5:
        col = (120, 100, 80, 255)
    return col


@mat("antler")
def _antler(c):
    return (92, 70, 52, 255) if c.n > 40 else (62, 46, 34, 255) if c.n > 12 else (140, 116, 88, 255)


@mat("wood_stilt")
def _stilt(c):
    col = WOOD_L if c.n > 80 else WOOD if c.n > 20 else WOOD_D
    if c.side and c.j % 6 == 5:
        col = LEATHER_D  # a wrapped band
    return col


# ---- the rook
@mat("feather", "feather_light")
def _feather(c):
    row = (c.j // 2) % 2
    pos = (c.i + row * 2) % 4
    col = FEATHER if pos else FEATHER_L if c.n > 40 else FEATHER
    if pos == 0 and c.n > 60:
        col = FEATHER_LL
    if c.n > 96:
        col = (126, 110, 90, 255)  # a worn patch
    return col


@mat("wicker_rib")
def _wicker_rib(c):
    if c.face == "front":
        if c.j % 3 == 0:
            return (28, 18, 14, 255)
        return WICKER if c.n > 30 else WICKER_D
    return FEATHER if c.n > 30 else FEATHER_L


@mat("beak")
def _beak(c):
    col = (212, 194, 150, 255) if c.n > 30 else (176, 156, 116, 255)
    if c.side and c.j == c.fh - 1:
        col = BONE_TDD
    return col


@mat("wing_cloth")
def _wing_cloth(c):
    col = (146, 120, 90, 255) if c.n > 30 else (112, 90, 66, 255)
    if c.side or c.face in ("top", "bottom"):
        if c.i % 5 == 0:
            col = (84, 66, 48, 255)  # the feather bones
        if c.i % 5 == 2 and c.j % 3 == 0:
            col = STITCH
    if c.n < 12 and (c.j == c.fh - 1 or c.i == c.fw - 1):
        return CLEAR
    return col


# ---- the bride
@mat("gown")
def _gown(c):
    col = GOWN if c.n > 18 else GOWN_D
    if c.side and c.i % 4 == 0:
        col = GOWN_D  # folds
    if c.n < 5:
        col = GOWN_STAIN
    return col


@mat("gown_hem")
def _gown_hem(c):
    col = _gown(c)
    if c.side and c.j == c.fh - 1 and c.n < 55:
        return CLEAR
    if c.side and c.j == c.fh - 2 and c.n < 25:
        return GOWN_STAIN
    return col


@mat("blank_face")
def _blank_face(c):
    col = DOLL_PORC if c.n > 15 else DOLL_PORC_D
    if c.face == "front":
        h = c.fh
        if c.j == h * 2 // 5 and (c.i == 1 or c.i == c.fw - 2):
            col = DARK_EYE  # two thin dark slits, nothing more
        elif c.j == h * 2 // 5 + 1 and (c.i == 1 or c.i == c.fw - 2):
            col = DOLL_PORC_D
        elif c.j == h - 2 and c.i == c.fw // 2:
            col = (190, 150, 150, 255)
    if c.n < 3:
        col = CRACK
    return col


# ---- the centipede
@mat("dress_form")
def _dress_form(c):
    col = (188, 170, 140, 255) if c.n > 25 else (160, 142, 114, 255)
    if c.side and c.j % 5 == 2:
        col = (140, 122, 96, 255)  # seam
    if c.side and c.i == c.fw // 2 and c.j > 0:
        col = (140, 122, 96, 255)
    if c.n > 96:
        col = STEEL  # a pin
    return col


def _skirt(c, tones):
    row = (c.j // 3) % len(tones)
    col = tones[row]
    if c.side and c.j % 3 == 2:
        col = dark(col, 0.78)  # the ruffle's shadow
    if c.side and c.n > 90 and c.j % 3 == 1:
        col = GOWN  # a floral dot
    if c.side and c.j == c.fh - 1 and c.n < 35:
        return CLEAR
    return col


@mat("skirt_a")
def _skirt_a(c):
    return _skirt(c, [(120, 128, 86, 255), (214, 198, 160, 255), (160, 100, 112, 255)])


@mat("skirt_b")
def _skirt_b(c):
    return _skirt(c, [(150, 110, 130, 255), (226, 214, 190, 255), (96, 104, 70, 255)])


@mat("skirt_c")
def _skirt_c(c):
    return _skirt(c, [(190, 150, 82, 255), (86, 120, 116, 255), (226, 214, 190, 255)])


def _mask(c, mouth):
    """A pale porcelain mask: tall hollow eyes, a small dark mouth; the three differ in the set of the mouth and in the cracks."""
    col = DOLL_PORC if c.n > 15 else DOLL_PORC_D
    if c.face == "front":
        w, h = c.fw, c.fh
        ey = h * 2 // 5
        if ey - 1 <= c.j <= ey + 1 and c.i in eye_cols(w):
            col = DARK_EYE  # hollow eyes
        elif c.j == h - 2 and w // 2 - 1 <= c.i <= w // 2:
            col = (74, 34, 38, 255)  # a small dark mouth
        elif mouth == "frown" and c.j == h - 1 and c.i in (w // 2 - 1, w // 2):
            col = DOLL_PORC_D
        elif mouth == "smile" and c.j == h - 3 and c.i in (w // 2 - 1, w // 2):
            col = DOLL_PORC_D
        elif c.j == ey + 2 and c.i == w // 2:
            col = DOLL_PORC_D  # the shadow of the nose
        elif mouth == "neutral" and c.i == c.j + 1 and c.j < ey:
            col = CRACK  # a crack across the brow
    elif c.n < 4:
        col = CRACK
    return col


@mat("mask_neutral")
def _mask_n(c):
    return _mask(c, "neutral")


@mat("mask_frown")
def _mask_f(c):
    return _mask(c, "frown")


@mat("mask_smile")
def _mask_s(c):
    return _mask(c, "smile")


# ---- the root mother
@mat("trunk", "bark")
def _trunk(c):
    col = BARK_L if c.n > 80 else BARK
    if c.side and (c.i + c.j // 4) % 4 == 0:
        col = (36, 26, 18, 255)  # furrows
    if noise(c.x // 3, c.y // 3, c.seed) > 74:
        col = MOSS if c.n > 40 else MOSS_D
    if c.face == "top":
        col = MOSS if c.n > 30 else MOSS_D
    return col


@mat("bark_arm")
def _bark_arm(c):
    col = _trunk(c)
    if noise(c.x // 2, c.y // 2, c.seed + 9) > 60 and c.j < c.fh // 2:
        col = MOSS if c.n > 40 else (110, 140, 70, 255)
    return col


@mat("bark_dark")
def _bark_dark(c):
    return (52, 36, 26, 255) if c.n > 30 else (36, 26, 18, 255)


@mat("moss")
def _moss(c):
    col = MOSS if c.n > 30 else MOSS_D
    if c.n > 88:
        col = (120, 150, 80, 255)
    return col


@mat("root")
def _root(c):
    col = (92, 66, 44, 255) if c.n > 30 else (62, 44, 30, 255)
    if c.side and c.n > 84:
        col = (130, 100, 70, 255)
    if c.side and (c.i + c.j) % 7 == 0:
        col = (46, 32, 22, 255)
    if c.face == "top" and c.n > 60:
        col = MOSS_D
    return col


@mat("wood_face")
def _wood_face(c):
    col = _trunk(c)
    if c.face == "front":
        w, h = c.fw, c.fh
        ey = h * 2 // 5
        col = (120, 92, 64, 255) if c.n > 30 else (96, 72, 48, 255)
        if c.j == ey and (2 <= c.i <= 3 or w - 4 <= c.i <= w - 3):
            col = (30, 20, 14, 255)  # closed, calm eyes
        elif c.j == ey - 1 and (2 <= c.i <= 3 or w - 4 <= c.i <= w - 3):
            col = (70, 50, 34, 255)
        elif c.j in (ey + 1, ey + 2) and c.i == w // 2:
            col = (80, 58, 40, 255)  # the nose ridge
        elif c.j == h - 3 and w // 2 - 1 <= c.i <= w // 2 + 1:
            col = (50, 30, 22, 255)
        elif c.j == 0:
            col = MOSS if c.n > 40 else MOSS_D  # mossy hair line
    return col


@mat("cavity")
def _cavity(c):
    return (26, 18, 14, 255) if c.n > 30 else (14, 10, 8, 255)


@mat("wicker")
def _wicker(c):
    weave = ((c.i // 1 + c.j // 1) % 2 == 0)
    col = WICKER if weave else WICKER_D
    if c.n > 88:
        col = (190, 146, 84, 255)
    return col


@mat("wicker_mesh")
def _wicker_mesh(c):
    if c.edge:
        return WICKER if c.n > 30 else WICKER_D
    if c.i % 4 in (1, 2) and c.j % 4 in (1, 2):
        return CLEAR  # holes: the light shows through the wicker
    return WICKER if (c.i + c.j) % 2 == 0 else WICKER_DD


@mat("glow_core")
def _glow_core(c):
    col = (255, 168, 54, 255) if c.n > 25 else (255, 214, 120, 255)
    return col, col


@mat("doll_red")
def _doll_red(c):
    return (170, 50, 44, 255) if (c.i + c.j) % 3 else (130, 36, 34, 255)


@mat("doll_blue")
def _doll_blue(c):
    return (60, 98, 156, 255) if (c.i + c.j) % 3 else (42, 70, 120, 255)


# ---- the executioner
@mat("sack")
def _sack(c):
    col = SACK if (c.i + c.j) % 2 == 0 else SACK_D
    if c.n > 85:
        col = (196, 170, 130, 255)
    if c.face == "front":
        w, h = c.fw, c.fh
        if abs(c.i - (w * 2 // 3)) <= 1 and abs(c.j - h * 2 // 5) <= 1:
            return (20, 12, 10, 255)  # the single eye hole
        if c.i == w // 3 and c.j > 1:
            col = STITCH if c.j % 2 == 0 else (80, 60, 44, 255)  # a stitched seam
    if c.face == "top" and c.n > 50:
        col = SACK_D
    return col


@mat("rope")
def _rope(c):
    return (170, 140, 96, 255) if (c.i + c.j) % 3 else (120, 96, 64, 255)


@mat("stitch_neck")
def _stitch_neck(c):
    if c.side and c.j == c.fh // 2:
        return STITCH if c.i % 2 == 0 else (60, 40, 34, 255)
    return (156, 100, 96, 255) if c.n > 30 else (118, 70, 70, 255)


@mat("wood_slab")
def _wood_slab(c):
    col = WOOD_L if c.n > 84 else WOOD if c.n > 18 else WOOD_D
    if c.side and c.j % 5 == 0:
        col = WOOD_D  # plank joints
    if c.side and c.j % 5 == 2 and c.i % 6 == 1:
        col = STEEL_D  # nails
    return col


@mat("tendon")
def _tendon(c):
    col = TENDON if (c.i + c.j) % 3 else (130, 48, 60, 255)
    return col, (CRIMSON if (c.i + c.j) % 5 == 0 else None)


@mat("cloth")
def _cloth(c):
    col = CLOTH_L if c.n > 85 else CLOTH_D if c.n < 25 else CLOTH
    return col


@mat("rag")
def _rag(c):
    col = (46, 40, 38, 255) if c.n > 25 else (28, 24, 24, 255)
    if c.side and (c.i + c.j) % 7 == 0:
        col = (74, 64, 58, 255)
    if c.side and c.j >= c.fh - 3 and c.n < 45:
        return CLEAR  # torn strips
    if c.side and c.i % 3 == 2 and c.j > c.fh - 8 and c.n < 70:
        return CLEAR
    return col


@mat("patch")
def _patch_old(c):
    return _patchwork(c)


@mat("bronze")
def _bronze(c):
    col = BRONZE_L if c.n > 80 else BRONZE
    if c.n < 9:
        col = PATINA
    return col


def _finish(col, c):
    """Edge darkening and a soft vertical gradient (the game's own lighting does the rest)."""
    if col is CLEAR or col[3] == 0:
        return col
    f = 1.0
    if c.side:
        f *= 1.06 - 0.16 * (c.j / max(1, c.fh - 1))
        if c.edge:
            f *= 0.9
    return dark(col, f) if f != 1.0 else col


def paint_box(img, eyes, box, u, v):
    name, cx, hb, cz, w, h, d, mat_name = box
    px = img.load()
    ex = eyes.load()
    seed = sum(ord(ch) for ch in name)
    painter = MATS[mat_name]
    for face, x, y, i, j, fw, fh in region_pixels(u, v, w, h, d):
        c = Ctx(face, x, y, i, j, fw, fh, noise(x, y, seed), seed, w, h, d)
        res = painter(c)
        glow = None
        if isinstance(res, tuple) and len(res) == 2 and isinstance(res[0], tuple):
            res, glow = res
        if res is None:
            res = (255, 0, 255, 255)
        px[x, y] = _finish(res, c)
        if glow is not None:
            ex[x, y] = glow


def build_images(model, layout):
    img = Image.new("RGBA", (ATLAS, ATLAS), CLEAR)
    eyes = Image.new("RGBA", (ATLAS, ATLAS), CLEAR)
    for box in model.all_boxes():
        u, v = layout[box[0]]
        paint_box(img, eyes, box, u, v)
    return img, eyes


# ---------------------------------------------------------------------------------------------------------------
def fnum(v):
    s = f"{v:.3f}".rstrip("0").rstrip(".")
    return (s if s not in ("", "-0") else "0") + "F"


def java_name(name):
    return "p_" + name


def write_java(models, layouts):
    lines = [
        "package com.codenamecoraline.client.ten_threads_witch;",
        "",
        "import java.util.List;",
        "import net.minecraft.client.model.geom.PartPose;",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "import net.minecraft.client.model.geom.builders.LayerDefinition;",
        "import net.minecraft.client.model.geom.builders.MeshDefinition;",
        "import net.minecraft.client.model.geom.builders.PartDefinition;",
        "",
        "/**",
        " * GENERATED by tools/generate_ten_threads_witch_assets.py: do not edit by hand.",
        " * The model layers of the Witch of Ten Threads and of her ten puppets (cubes with their UV origin from the",
        " * shelf-packed atlas of each model; no overlapping footprints), the part names of each and the height of its",
        " * body pivot above the ground (in model pixels).",
        " */",
        "public final class TenThreadsParts {",
        f"    public static final int ATLAS = {ATLAS};",
        "",
        "    private TenThreadsParts() {}",
        "",
        "    public static LayerDefinition layer(String id) {",
        "        return switch (id) {",
    ]
    for ident in models:
        lines.append(f'            case "{ident}" -> {ident}();')
    lines += ['            default -> throw new IllegalArgumentException("Unknown model " + id);', "        };", "    }", "",
              "    public static List<String> partNames(String id) {", "        return switch (id) {"]
    for ident, m in models.items():
        names = ", ".join(f'"{p["name"]}"' for p in m.parts)
        lines.append(f'            case "{ident}" -> List.of({names});')
    lines += ['            default -> throw new IllegalArgumentException("Unknown model " + id);', "        };", "    }", "",
              "    /** Height of the body pivot above the ground, in model pixels. */", "    public static float pivotHeight(String id) {", "        return switch (id) {"]
    for ident, m in models.items():
        lines.append(f'            case "{ident}" -> {fnum(m.parts[0]["pivot"][1])};')
    lines += ["            default -> 0.0F;", "        };", "    }", ""]
    for ident, m in models.items():
        layout = layouts[ident]
        lines += [f"    private static LayerDefinition {ident}() {{", "        MeshDefinition mesh = new MeshDefinition();", "        PartDefinition root = mesh.getRoot();"]
        absolute = {p["name"]: (p["pivot"][0], 24 - p["pivot"][1], p["pivot"][2]) for p in m.parts}
        for p in m.parts:
            ax, ay, az = absolute[p["name"]]
            if p["parent"] is None:
                rx, ry, rz = ax, ay, az
                parent = "root"
            else:
                bx, by, bz = absolute[p["parent"]]
                rx, ry, rz = ax - bx, ay - by, az - bz
                parent = java_name(p["parent"])
            cube = ["CubeListBuilder.create()"]
            for name, cx, hb, cz, w, h, d, mat in p["boxes"]:
                u, v = layout[name]
                x0, y0, z0 = cx - w / 2 - ax, 24 - (hb + h) - ay, cz - d / 2 - az
                cube.append(f".texOffs({u}, {v}).addBox({fnum(x0)}, {fnum(y0)}, {fnum(z0)}, {fnum(w)}, {fnum(h)}, {fnum(d)})")
            ex, ey, ez = (math.radians(a) for a in p.get("rot", (0, 0, 0)))
            if ex or ey or ez:
                pose = f"PartPose.offsetAndRotation({fnum(rx)}, {fnum(ry)}, {fnum(rz)}, {fnum(ex)}, {fnum(ey)}, {fnum(ez)})"
            else:
                pose = f"PartPose.offset({fnum(rx)}, {fnum(ry)}, {fnum(rz)})"
            lines.append(f'        PartDefinition {java_name(p["name"])} = {parent}.addOrReplaceChild("{p["name"]}", {"".join(cube)}, {pose});')
        lines += ["        return LayerDefinition.create(mesh, ATLAS, ATLAS);", "    }", ""]
    lines.append("}")
    JAVA_OUT.parent.mkdir(parents=True, exist_ok=True)
    JAVA_OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


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


def icon_spindle():
    cells = {}
    for i in range(12):  # a carved spindle: a long shaft with a fat whorl and silver thread wound on it
        x, y = 2 + i, 13 - i
        cells[(x, y)] = WOOD_L if i % 2 else WOOD
        cells[(x + 1, y)] = WOOD_D
    for (x, y) in ((7, 8), (8, 7), (6, 8), (8, 8), (7, 7), (9, 7), (7, 9), (8, 6), (6, 9)):
        cells[(x, y)] = THREAD if (x + y) % 2 else THREAD_D
    cells[(13, 1)] = CRACK
    cells[(14, 1)] = CRIMSON
    cells[(1, 14)] = BONE
    cells[(2, 15)] = THREAD
    return outline(cells)


def icon_remnant():
    cells = {}
    for y in range(3, 13):
        for x in range(3, 13):
            n = noise(x, y, 5)
            cells[(x, y)] = BURLAP_L if n > 75 else BURLAP_D if n < 20 else BURLAP
    for x in range(3, 13):
        cells[(x, 3 + (x % 3))] = CLEAR if x % 4 == 0 else BURLAP_D  # a torn edge
    for k in range(3, 13, 2):
        cells[(k, 8)] = THREAD  # stitches
    for (x, y) in ((7, 6), (8, 6), (7, 7), (8, 7)):
        cells[(x, y)] = EYE if (x + y) % 2 else EYE_H  # a button
    return outline({k: v for k, v in cells.items() if v != CLEAR})


def icon_thimble():
    cells = {}
    for y in range(4, 13):
        half = 3 if y < 6 else 4
        for x in range(8 - half, 8 + half):
            n = noise(x, y, 11)
            cells[(x, y)] = THREAD_D if n < 25 else THREAD if n > 70 else (190, 200, 208, 255)
            if (x + y) % 3 == 0 and 5 < y < 11:
                cells[(x, y)] = (110, 120, 130, 255)  # the dimples
    for x in range(4, 12):
        cells[(x, 12)] = BRONZE_L
        cells[(x, 13)] = BRONZE
    cells[(7, 3)] = THREAD
    cells[(8, 3)] = THREAD
    cells[(7, 8)] = RUBY
    return outline(cells)


def icon_staff():
    cells = {}
    for i in range(13):  # a long staff with a spinning wheel at the top
        cells[(2 + i, 13 - i)] = WOOD_L if i % 2 else WOOD
        cells[(3 + i, 13 - i)] = WOOD_D
    cx, cy = 11, 4
    for k in range(10):
        a = k * math.pi / 5
        cells[(round(cx + math.cos(a) * 3), round(cy + math.sin(a) * 3))] = RUBY if k % 2 == 0 else THREAD
    cells[(cx, cy)] = WOOD_L
    cells[(1, 14)] = BONE
    return outline(cells)


def icon_portrait():
    cells = {}
    for y in range(3, 15):  # an old face under a lace blindfold, a tight bun
        for x in range(4, 12):
            dx, dy = x - 7.5, y - 8.5
            if dx * dx / 18 + dy * dy / 30 <= 1:
                n = noise(x, y, 13)
                cells[(x, y)] = SKIN if n > 30 else SKIN_D
    for x in range(4, 12):
        cells[(x, 7)] = LACE
        cells[(x, 8)] = LACE_D
    for (x, y) in ((6, 11), (7, 11), (8, 11), (9, 11)):
        cells[(x, y)] = CLOTH_D
    for (x, y) in ((7, 2), (8, 2), (7, 3), (8, 3)):
        cells[(x, y)] = BURLAP
    for k in range(6):
        cells[(12 + k // 3, 2 + k)] = THREAD if k % 2 == 0 else CRIMSON  # threads rising from her
    return outline(cells)


def icon_egg():
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 8.3
            if (dx / 6.2) ** 2 + (dy / 7.4) ** 2 <= 1:
                n = noise(x, y, 17)
                px[x, y] = BURLAP_D if n < 22 else BURLAP_L if n > 78 else BURLAP
                if (x + y) % 5 == 0 and 4 < y < 12:
                    px[x, y] = THREAD
    for c in ((6, 6), (9, 9), (7, 11)):
        px[c] = RUBY
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3] > 0 and px[nx, ny] != OUT:
                        px[x, y] = OUT
                        break
    return img


SW = {"wood": (0, 0), "wood_l": (4, 0), "wood_d": (8, 0), "thread": (12, 0), "spool": (0, 4), "bone": (4, 4)}


def staff_swatches():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    for name, (u0, v0) in SW.items():
        base, light, dark = {
            "wood": (WOOD_L, (110, 80, 58, 255), WOOD), "wood_l": ((110, 80, 58, 255), (140, 104, 76, 255), WOOD_L),
            "wood_d": (WOOD, WOOD_L, WOOD_D), "thread": (THREAD, (240, 246, 250, 255), THREAD_D),
            "spool": (RUBY, RUBY_H, (160, 10, 70, 255)), "bone": (BONE, (236, 230, 216, 255), BONE_D),
        }[name]
        for j in range(4):
            for i in range(4):
                n = (i * 7 + j * 13 + u0 + v0) % 5
                px[u0 + i, v0 + j] = dark if n == 0 else light if n == 4 else base
    return img


def staff_model():
    parts = [
        ([7.2, -12, 7.2], [8.8, 22, 8.8], "wood"),            # the staff
        ([6.6, -13, 6.6], [9.4, -12, 9.4], "bone"),           # foot cap
        ([6.4, 20, 7.4], [9.6, 21.6, 8.6], "wood_d"),         # hub collar
        ([3.0, 23, 7.5], [13.0, 24.2, 8.5], "wood_l"),        # wheel, horizontal bar
        ([7.4, 18, 7.5], [8.6, 29, 8.5], "wood_l"),           # wheel, vertical bar
        ([4.2, 18.4, 7.5], [11.8, 19.4, 8.5], "wood"),        # wheel, lower rim
        ([4.2, 28.6, 7.5], [11.8, 29.6, 8.5], "wood"),        # wheel, upper rim
        ([2.2, 22.6, 7.2], [4.6, 25.0, 8.8], "spool"),        # spools around the wheel
        ([11.4, 22.6, 7.2], [13.8, 25.0, 8.8], "spool"),
        ([6.8, 29.4, 7.2], [9.2, 31.8, 8.8], "spool"),
        ([6.8, 16.0, 7.2], [9.2, 18.4, 8.8], "spool"),
        ([4.0, 27.0, 7.2], [6.0, 29.0, 8.8], "thread"),       # silver thread wound on the rim
        ([10.0, 18.0, 7.2], [12.0, 20.0, 8.8], "thread"),
    ]
    elements = []
    for a, b, mat in parts:
        u, v = SW[mat]
        face = {"uv": [u, v, u + 4, v + 4], "texture": "#0"}
        elements.append({"from": a, "to": b, "rotation": {"angle": -45, "axis": "z", "origin": [8, 8, 8]},
                         "faces": {k: dict(face) for k in ("north", "south", "east", "west", "up", "down")}})
    big = lambda s: [s, s, s]
    return {
        "textures": {"0": "codenamecoraline:item/ten_threads_staff_3d", "particle": "codenamecoraline:item/ten_threads_staff_3d"},
        "elements": elements,
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": big(0.95)},
            "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": big(0.95)},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": big(0.8)},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": big(0.8)},
            "gui": {"rotation": [30, 225, 0], "scale": big(0.42)},
            "ground": {"translation": [0, 3, 0], "scale": big(0.45)},
            "fixed": {"rotation": [0, 90, 0], "scale": big(0.5)},
        },
    }


def preview(ident, img, layout, boxes):
    big = img.resize((ATLAS * 3, ATLAS * 3), Image.NEAREST)
    bg = Image.new("RGBA", big.size, (60, 70, 90, 255))
    bg.alpha_composite(big)
    d = ImageDraw.Draw(bg)
    for box in boxes:
        u, v = layout[box[0]]
        fw, fh = footprint(box[4], box[5], box[6])
        d.rectangle((u * 3, v * 3, (u + fw) * 3 - 1, (v + fh) * 3 - 1), outline=(255, 255, 255, 70))
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    bg.save(PREVIEW_DIR / f"atlas_{ident}.png")


def main():
    models = build_models()
    for p in (ENTITY_DIR, ITEM_DIR, MODEL_DIR):
        p.mkdir(parents=True, exist_ok=True)
    layouts = {}
    summary = []
    for ident, m in models.items():
        boxes = m.all_boxes()
        layout = pack(boxes)
        layouts[ident] = layout
        img, eyes = build_images(m, layout)
        img.save(ENTITY_DIR / f"{ident}.png")
        eyes.save(ENTITY_DIR / f"{ident}_eyes.png")
        preview(ident, img, layout, boxes)
        used = max(v + footprint(b[4], b[5], b[6])[1] for b in boxes for (u, v) in [layout[b[0]]])
        summary.append(f"{ident}: {len(boxes)} boxes, {used}/{ATLAS} rows")
    write_java(models, layouts)
    icon_spindle().save(ITEM_DIR / "cursed_spindle.png")
    icon_remnant().save(ITEM_DIR / "puppet_remnant.png")
    icon_thimble().save(ITEM_DIR / "matriarch_thimble.png")
    icon_staff().save(ITEM_DIR / "ten_threads_staff.png")
    icon_portrait().save(ITEM_DIR / "ten_threads_witch_portrait.png")
    icon_egg().save(ITEM_DIR / "ten_threads_witch_spawn_egg.png")
    staff_swatches().save(ITEM_DIR / "ten_threads_staff_3d.png")
    with open(MODEL_DIR / "ten_threads_staff_3d.json", "w", encoding="utf-8", newline="\n") as fh:
        json.dump(staff_model(), fh, indent=2)
        fh.write("\n")
    print("Generated", len(models), "models:", "; ".join(summary))


if __name__ == "__main__":
    main()
