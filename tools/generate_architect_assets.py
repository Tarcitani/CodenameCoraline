"""Generates every asset of the Architect, Kandiaru (spec: docs/design/architect.md).

Writes:
  * src/main/java/.../client/architect/ArchitectParts.java  (GENERATED: rig hierarchy, pivots and cubes with UVs)
  * textures/entity/architect.png               256x256 body atlas (pixel art, 4 tones per material)
  * textures/entity/architect_glow.png          emissive layer in combat (coral irises, lilac seal, cyan veins, grooves)
  * textures/entity/architect_glow_command.png  emissive layer while commanding (full sternum circuit and veins)
  * textures/entity/architect_glow_waking.png   emissive layer of the awakening (seal, tablet glyphs, the mask's eyes)
  * textures/item/architect_spawn_egg.png, textures/item/commandment_tablet.png
  * src/main/java/.../client/architect/ArchitectAnimations.java (GENERATED from tools/architect_clips.py)
  * build/architect-assets/atlas_preview.png    the atlas x3 with box names, for review

Coordinates (spec §3.1): geometry is written in absolute Blockbench coordinates, pixels, origin on the ground,
+Y up, front -Z, +X = the character's LEFT. The script converts to model space (y_model = 24 - Y) and to offsets
relative to each part's pivot. Boxes marked with a density of 2 are drawn with twice the texels (declared at double
size and shrunk back with a negative CubeDeformation): the face, the teeth, the mask and the forehead seal.

Run:  python tools/generate_architect_assets.py
"""
import sys
from pathlib import Path
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
ENTITY_DIR = ASSETS / "textures/entity"
ITEM_DIR = ASSETS / "textures/item"
JAVA_OUT = ROOT / "src/main/java/com/codenamecoraline/client/architect/ArchitectParts.java"
PREVIEW = ROOT / "build/architect-assets/atlas_preview.png"
ATLAS = 256
GAP = 1  # empty texels between UV islands (anti-bleeding)

CLEAR = (0, 0, 0, 0)
# ---- palette (spec §8.3): (shadow, base, light, highlight)
FACE = ((67, 72, 82, 255), (110, 117, 130, 255), (155, 163, 176, 255), (195, 201, 210, 255))
GRANITE = ((73, 71, 79, 255), (119, 116, 122, 255), (170, 164, 160, 255), (201, 195, 188, 255))
BASALT = ((21, 21, 29, 255), (40, 40, 49, 255), (85, 81, 91, 255), (110, 106, 116, 255))
TABLET = ((110, 106, 103, 255), (161, 155, 145, 255), (199, 191, 176, 255), (214, 208, 196, 255))
CRACK = (36, 35, 43, 255)
DEEP = (18, 18, 23, 255)
VOID = (11, 11, 16, 255)
CORAL = (255, 96, 75, 255)
CORAL_D = (169, 31, 50, 255)
CORAL_HOT = (255, 208, 154, 255)
LILAC = (216, 200, 255, 255)
LILAC_D = (126, 100, 196, 255)
LILAC_L = (247, 242, 255, 255)
MASK_GLOW = (221, 244, 255, 255)
MANA = (56, 189, 186, 255)
MANA_D = (23, 106, 112, 255)
MANA_L = (182, 255, 240, 255)

# ---------------------------------------------------------------------------------------------------------------
# Rig: name -> (parent, pivot X, pivot Y, pivot Z) in absolute Blockbench coordinates. Order = parents first.
PARTS = {}
BOXES = []  # (name, part, x0, y0, z0, x1, y1, z1, material, density)


def part(name, parent, x, y, z):
    assert name not in PARTS, name
    assert parent is None or parent in PARTS, (name, parent)
    PARTS[name] = (parent, x, y, z)


def box(name, owner, x0, y0, z0, x1, y1, z1, mat, density=1):
    assert owner in PARTS, owner
    BOXES.append((name, owner, x0, y0, z0, x1, y1, z1, mat, density))


def build_rig():
    part("root", None, 0, 24, 0)
    # ---- pelvis, robe, legs
    part("pelvis", "root", 0, 22, 0)
    box("pelvis_plate", "pelvis", -5, 20, -3.5, 5, 24, 3.5, "granite")
    part("robe_front", "pelvis", 0, 22, -4)
    for i, x in enumerate((-4.5, -1.5, 1.5)):
        box(f"robe_front_{i}", "robe_front", x, 6 + (i % 2) * 2, -4.6, x + 3, 22, -3.6, "robe")
    part("robe_back_upper", "pelvis", 0, 22, 4)
    box("robe_back_upper", "robe_back_upper", -5, 12, 3.6, 5, 22, 4.6, "robe")
    part("robe_back_lower", "robe_back_upper", 0, 12, 4.1)
    box("robe_back_lower", "robe_back_lower", -5, 3, 3.6, 5, 12, 4.6, "robe")
    for side, s in (("left", 1), ("right", -1)):
        part(f"robe_{side}", "pelvis", 5.5 * s, 22, 0)
        box(f"robe_{side}", f"robe_{side}", min(5 * s, 6 * s), 8, -3, max(5 * s, 6 * s), 22, 3, "robe")
    for side, s in (("left", 1), ("right", -1)):
        x0, x1 = (3 * s - 2.25, 3 * s + 2.25)
        part(f"leg_{side}", "pelvis", 3 * s, 22, 0)
        box(f"thigh_{side}", f"leg_{side}", x0, 12, -2.5, x1, 22, 2.5, "granite")
        box(f"thigh_shield_{side}", f"leg_{side}", 3 * s - 1.5, 13, -3, 3 * s + 1.5, 20, -2.5, "granite_light")
        part(f"knee_{side}", f"leg_{side}", 3 * s, 12, -0.5)
        box(f"knee_{side}", f"knee_{side}", 3 * s - 2, 11, -3, 3 * s + 2, 13, -1, "granite_light")
        part(f"shin_{side}", f"knee_{side}", 3 * s, 12, -0.5)
        box(f"shin_{side}", f"shin_{side}", 3 * s - 1.75, 2, -2, 3 * s + 1.75, 12, 2, "granite")
        box(f"shin_crest_{side}", f"shin_{side}", 3 * s - 0.5, 3, -2.5, 3 * s + 0.5, 11, -2, "granite_light")
        part(f"ankle_{side}", f"shin_{side}", 3 * s, 2, 0.5)
        part(f"foot_{side}", f"ankle_{side}", 3 * s, 2, 0.5)
        box(f"foot_{side}", f"foot_{side}", 3 * s - 2.5, 0, -3, 3 * s + 2.5, 2, 2.5, "granite")
        box(f"heel_{side}", f"foot_{side}", 3 * s - 1.5, 0, 2.5, 3 * s + 1.5, 2, 4, "granite")
        for k, dx in enumerate((-1.75, 0, 1.75)):
            box(f"toe_{k}_{side}", f"foot_{side}", 3 * s + dx - 0.75, 0, -5, 3 * s + dx + 0.75, 1.5, -3, "granite_light")
    # ---- torso
    part("torso", "pelvis", 0, 29, 0)
    box("ribcage", "torso", -8, 29, -4, 8, 43, 5, "ribcage")
    for side, s in (("left", 1), ("right", -1)):
        box(f"mount_{side}", "torso", min(8 * s, 11.5 * s), 28, -3.5, max(8 * s, 11.5 * s), 43, 3.5, "mount")
    part("abdomen", "torso", 0, 29, 0)
    for i in range(4):
        w, d = 7 - i * 0.5, 4 - i * 0.25
        box(f"abdomen_{i}", "abdomen", -w, 29 - (i + 1) * 1.75, -d, w, 29 - i * 1.75, d, "abdomen")
    part("sternum_seal", "torso", 0, 37, -4.25)
    box("sternum_seal", "sternum_seal", -2, 34, -4.5, 2, 40, -4, "sternum")
    part("tablet_anchor", "torso", 0, 32, -8)
    part("mandament_tablet", "tablet_anchor", 0, 32, -8)
    for side, s in (("left", 1), ("right", -1)):
        part(f"tablet_half_{side}", "mandament_tablet", 0.25 * s, 22, -8)
        box(f"tablet_{side}", f"tablet_half_{side}", min(0, 5 * s), 22, -8.75, max(0, 5 * s), 34, -7.25, "tablet")
        box(f"tablet_top_{side}", f"tablet_half_{side}", min(0, 5.5 * s), 34, -8.75, max(0, 5.5 * s), 38, -7.25, "tablet")
    # ---- neck and head
    part("neck", "torso", 0, 43, 0)
    for i, (w, y) in enumerate(((3, 43), (2.75, 44), (2.5, 45))):
        box(f"neck_ring_{i}", "neck", -w, y, -w, w, y + 1, w, "face_stone")
    part("head", "neck", 0, 46, 0)
    box("cranium", "head", -3.5, 46.5, -3.5, 3.5, 53, 3.5, "face_stone")
    box("face", "head", -3.5, 46.5, -3.6, 3.5, 53, -3.5, "face", 2)
    part("hood", "head", 0, 46, 0)
    box("hood_shell_back", "hood", -4.5, 44, 1, 4.5, 53.5, 5, "hood")
    for side, s in (("left", 1), ("right", -1)):
        box(f"hood_side_{side}", "hood", min(3.75 * s, 5.25 * s), 44, -4.5, max(3.75 * s, 5.25 * s), 54, 3.5, "hood")
    box("hood_crown", "hood", -3.5, 53, -4.5, 3.5, 54.5, 3.5, "hood")
    part("hood_shadow_inner", "head", 0, 46, 0)
    box("hood_cavity_top", "hood_shadow_inner", -3.75, 53, -4.5, 3.75, 53.4, -3.6, "void")
    for side, s in (("left", 1), ("right", -1)):
        box(f"hood_cavity_{side}", "hood_shadow_inner", min(3.5 * s, 3.75 * s), 44.5, -4.5, max(3.5 * s, 3.75 * s), 53, -3.6, "void")
    part("forehead_diamond_seal", "head", 0, 51.75, -3.75)
    box("forehead_seal", "forehead_diamond_seal", -0.5, 50.5, -3.9, 0.5, 53, -3.6, "seal", 2)
    for side, s in (("left", 1), ("right", -1)):
        part(f"brow_{side}", "head", 1.6 * s, 50.6, -3.75)
        box(f"brow_{side}", f"brow_{side}", 1.6 * s - 1.4, 50.2, -3.95, 1.6 * s + 1.4, 51, -3.6, "face_stone")
        part(f"eye_socket_{side}", "head", 1.75 * s, 49.5, -3.6)
    part("nose_bridge", "head", 0, 48.5, -4)
    box("nose", "nose_bridge", -0.5, 47.5, -4.5, 0.5, 50, -3.6, "face_stone")
    part("mouth_interior", "head", 0, 46.5, -2.5)
    box("mouth_interior", "mouth_interior", -2.75, 45, -3.2, 2.75, 46.5, -0.5, "void")
    part("teeth_upper", "head", 0, 47, -3.65)
    box("teeth_upper", "teeth_upper", -3, 46.5, -3.75, 3, 47.5, -3.6, "teeth_up", 2)
    part("jaw", "head", 0, 46.5, 2)
    box("jaw", "jaw", -3, 44, -3.5, 3, 46.5, 3, "jaw_stone")
    box("jaw_front", "jaw", -3, 44, -3.6, 3, 46.5, -3.5, "jaw_face", 2)
    box("chin", "jaw", -1.5, 43.5, -3.9, 1.5, 44.5, -3.4, "face_stone")
    part("teeth_lower", "jaw", 0, 46, -3.65)
    box("teeth_lower", "teeth_lower", -3, 45.5, -3.75, 3, 46.5, -3.6, "teeth_low", 2)
    for side, s in (("left", 1), ("right", -1)):
        part(f"face_mask_{side}", "head", 3.5 * s, 48.5, -4.6)
        box(f"face_mask_{side}", f"face_mask_{side}", min(0, 3.5 * s), 44.5, -4.7, max(0, 3.5 * s), 52, -4.55,
            f"mask_{side}", 2)
    # ---- six arms (spec §4.4): hanging chains, the idle pose comes from the animations' BASE
    for tier, (sx, sy, sz) in (("top", (10.5, 41, -1)), ("mid", (11.5, 35, 0.5)), ("low", (10, 30, 1.5))):
        for side, s in (("left", 1), ("right", -1)):
            arm(tier, side, s, sx * s, sy, sz)
    # ---- wings (spec §4.5): built open along +-X, folded by the animations
    for side, s in (("left", 1), ("right", -1)):
        wing(side, s)


SWORD_LENGTH = {("top", "right"): 20, ("top", "left"): 19, ("mid", "right"): 18, ("mid", "left"): 18,
                ("low", "right"): 17, ("low", "left"): 17}
SWORD_WIDTH = {("mid", "right"): 3.5, ("mid", "left"): 2.5}


def arm(tier, side, s, x, y, z):
    n = f"{tier}_{side}"
    part(f"arm_{n}", "torso", x, y, z)
    box(f"shoulder_{n}", f"arm_{n}", x - 2, y - 2, z - 2, x + 2, y + 2, z + 2, "mount_disc")
    plate = 0.5 if n == "top_right" else 0
    box(f"pauldron_{n}", f"arm_{n}", x - 2.5 - plate, y + 1.5, z - 2.5 - plate, x + 2.5 + plate, y + 3, z + 2.5 + plate, "granite_light")
    part(f"upper_arm_{n}", f"arm_{n}", x, y, z)
    box(f"upper_{n}", f"upper_arm_{n}", x - 1.75, y - 8.5, z - 1.75, x + 1.75, y - 1, z + 1.75, "granite")
    part(f"elbow_{n}", f"upper_arm_{n}", x, y - 8.5, z)
    box(f"elbow_{n}", f"elbow_{n}", x - 2, y - 9.25, z - 2, x + 2, y - 7.75, z + 2, "elbow")
    if n == "low_right":
        box("elbow_spur_low_right", "elbow_low_right", x - 0.5, y - 9, z + 2, x + 0.5, y - 8, z + 4, "granite_light")
    part(f"forearm_{n}", f"elbow_{n}", x, y - 8.5, z)
    half = 2.25 if n == "mid_right" else 2
    box(f"forearm_{n}", f"forearm_{n}", x - half, y - 17, z - half, x + half, y - 9.25, z + half,
        "forearm_chipped" if n == "top_left" else "forearm")
    cx0, cx1 = sorted((x + (half - 0.5) * s, x + (half + 1) * s))  # outer ridge of the forearm
    box(f"crest_{n}", f"forearm_{n}", cx0, y - 16, z - 0.75, cx1, y - 10, z + 0.75, "granite_light")
    part(f"wrist_{n}", f"forearm_{n}", x, y - 17.5, z)
    box(f"wrist_{n}", f"wrist_{n}", x - 1.75, y - 18.25, z - 1.75, x + 1.75, y - 17, z + 1.75,
        "wrist_cracked" if n == "low_left" else "elbow")
    part(f"hand_{n}", f"wrist_{n}", x, y - 19, z)
    # a closed fist around the grip (the grip runs vertically through the hand centre at y - 20)
    finger = 3 if n == "mid_left" else 2.5
    box(f"palm_{n}", f"hand_{n}", x + 0.6 * s - 1.2, y - 21.5, z - 1.25, x + 0.6 * s + 1.2, y - 18.25, z + 1.25, "hand")
    box(f"fingers_{n}", f"hand_{n}", x - 1.4, y - 18.25 - finger - 0.5, z - 1.9, x + 1.4, y - 18.75, z - 0.6, "fingers")
    box(f"thumb_{n}", f"hand_{n}", x - 1, y - 20, z + 0.6, x + 1, y - 19, z + 1.6, "hand")
    part(f"tablet_grip_{side}" if tier == "top" else f"palm_anchor_{n}", f"hand_{n}", x, y - 20, z)
    sword(n, tier, side, x, y - 20, z)


def sword(n, tier, side, x, gy, z):
    length = SWORD_LENGTH[(tier, side)]
    width = SWORD_WIDTH.get((tier, side), 3)
    part(f"sword_{n}", f"hand_{n}", x, gy, z)
    box(f"pommel_{n}", f"sword_{n}", x - 0.75, gy + 2, z - 0.75, x + 0.75, gy + 3.5, z + 0.75, "basalt_light")
    box(f"grip_{n}", f"sword_{n}", x - 0.5, gy - 2, z - 0.5, x + 0.5, gy + 2, z + 0.5, "grip")
    box(f"guard_{n}", f"sword_{n}", x - 0.75, gy - 3, z - 1.5, x + 0.75, gy - 2, z + 1.5, "basalt_light")
    # the edge faces -Z, the thick spine +Z
    box(f"blade_base_{n}", f"sword_{n}", x - 0.5, gy - 10, z - width + 0.5, x + 0.5, gy - 3, z + 0.5, "blade")
    tip = length - 13.5
    tip_y = gy - 10
    part(f"blade_tip_{n}", f"sword_{n}", x, tip_y, z)
    box(f"blade_tip_{n}", f"blade_tip_{n}", x - 0.5, tip_y - tip, z - 1.5, x + 0.5, tip_y + 0.25, z + 0.5, "blade_tip")


def wing(side, s):
    n = side
    part(f"wing_root_{n}", "torso", 4 * s, 40, 5)
    box(f"wing_root_{n}", f"wing_root_{n}", min(2 * s, 6 * s), 37.5, 5, max(2 * s, 6 * s), 42.5, 8, "granite")
    part(f"wing_arm_{n}", f"wing_root_{n}", 4 * s, 40, 5)
    box(f"wing_arm_{n}", f"wing_arm_{n}", min(5 * s, 18 * s), 40, 5.5, max(5 * s, 18 * s), 42, 7.5, "wing_bone")
    box(f"wing_inner_plate_{n}", f"wing_arm_{n}", min(5 * s, 15 * s), 33, 6.25, max(5 * s, 15 * s), 40, 6.75, "wing_plate")
    part(f"wing_forearm_{n}", f"wing_arm_{n}", 18 * s, 41, 6.5)
    box(f"wing_forearm_{n}", f"wing_forearm_{n}", min(18 * s, 32 * s), 41, 5.75, max(18 * s, 32 * s), 42.5, 7.25, "wing_bone")
    part(f"wing_tip_{n}", f"wing_forearm_{n}", 32 * s, 41.75, 6.5)
    box(f"wing_tip_{n}", f"wing_tip_{n}", min(32 * s, 38 * s), 41.25, 6, max(32 * s, 38 * s), 42.25, 7, "wing_tip")
    for k, (fx, length) in enumerate(((19, 10), (23, 12), (27, 11), (31, 8))):
        f = f"wing_finger_{k + 1}_{n}"
        part(f, f"wing_forearm_{n}", fx * s, 41, 6.5)
        box(f, f, fx * s - 0.5, 41 - length, 6, fx * s + 0.5, 41, 7, "wing_bone")
        box(f"{f}_plate", f, min(fx * s, (fx - 4) * s), 41 - length + 1.5, 6.25, max(fx * s, (fx - 4) * s), 40.5, 6.75, "wing_plate")


# ---------------------------------------------------------------------------------------------------------------
def ceil(v):
    return int(-(-v // 1))


def dims(b):
    """Declared (texel) size of a box: whole texels, doubled on 2x boxes; the CubeDeformation shrinks it to the real size."""
    _, _, x0, y0, z0, x1, y1, z1, _, k = b
    return max(1, ceil((x1 - x0) * k - 1e-6)), max(1, ceil((y1 - y0) * k - 1e-6)), max(1, ceil((z1 - z0) - 1e-6))


def footprint(w, h, d):
    return 2 * (ceil(d) + ceil(w)), ceil(d) + ceil(h)


def pack():
    """Shelf packing, tallest first, with a gap around every island; returns {name: (u, v)}."""
    items = sorted(BOXES, key=lambda b: (-footprint(*dims(b))[1], -footprint(*dims(b))[0]))
    layout = {}
    x = y = row_h = 0
    for b in items:
        fw, fh = footprint(*dims(b))
        if x + fw > ATLAS:
            x, y, row_h = 0, y + row_h + GAP, 0
        if y + fh > ATLAS:
            raise SystemExit(f"atlas overflow at {b[0]}")
        layout[b[0]] = (x, y)
        x += fw + GAP
        row_h = max(row_h, fh)
    return layout


def check_overlap(layout):
    taken = {}
    for b in BOXES:
        u, v = layout[b[0]]
        fw, fh = footprint(*dims(b))
        for yy in range(v, v + fh):
            for xx in range(u, u + fw):
                if (xx, yy) in taken:
                    raise SystemExit(f"UV overlap: {b[0]} and {taken[(xx, yy)]}")
                taken[(xx, yy)] = b[0]
    return len(taken)


def faces_of(u, v, w, h, d):
    w, h, d = ceil(w), ceil(h), ceil(d)
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


def noise(x, y, seed=0):
    return (x * 73856093 ^ y * 19349663 ^ seed * 83492791) % 100


def tone(pal, t, n):
    """Stepped shading: light at the top of a face, shadow at the bottom, clustered noise in between."""
    if t < 0.18:
        return pal[2]
    if t > 0.82:
        return pal[0]
    return pal[2] if n > 86 else pal[0] if n < 12 else pal[1]


# ---------------------------------------------------------------------------------------------------------------
def paint(img, glows, b, u, v):
    name, owner, *_, mat, k = b
    w, h, d = dims(b)
    px = img.load()
    gl = {key: g.load() for key, g in glows.items()}
    seed = sum(ord(c) for c in name)

    def glow(x, y, color, which=("glow", "command")):
        for key in which:
            gl[key][x, y] = color

    for face, (fx0, fy0, fw, fh) in faces_of(u, v, w, h, d).items():
        side = face in ("front", "back", "left", "right")
        for j in range(fh):
            for i in range(fw):
                x, y = fx0 + i, fy0 + j
                n = noise(x // 2, y // 2, seed)
                t = j / max(1, fh - 1)
                pal = {"face_stone": FACE, "jaw_stone": FACE, "hood": FACE, "granite_light": GRANITE,
                       "basalt_light": BASALT, "blade": BASALT, "blade_tip": BASALT, "grip": BASALT,
                       "wing_tip": BASALT, "tablet": TABLET}.get(mat, GRANITE)
                c = tone(pal, t, n) if side else (pal[2] if face == "top" else pal[0])
                if mat == "granite_light" and side:
                    c = pal[3] if t < 0.2 else pal[2] if n > 40 else pal[1]
                if side and fw > 2 and (i == 0 or i == fw - 1) and mat not in ("face", "teeth_up", "teeth_low", "seal",
                                                                                 "mask_left", "mask_right", "jaw_face"):
                    c = pal[0]
                if mat == "void" or (mat == "hood" and face in ("back",) and name.startswith("hood_side")):
                    c = VOID
                elif mat == "hood" and face == "bottom":
                    c = VOID
                elif mat == "robe" and side:
                    c = (GRANITE[2] if (i // 2) % 2 == 0 else GRANITE[1]) if t < 0.85 else GRANITE[0]
                    if j == fh - 1 and (i + seed) % 3 == 0:
                        c = CLEAR  # broken lower tips
                    if n < 6:
                        c = CRACK
                elif mat == "abdomen" and side:
                    c = GRANITE[2] if j == 0 else GRANITE[0] if j == fh - 1 else GRANITE[1]
                    if face == "front" and i in (0, fw - 1) and 0 < j < fh - 1:
                        c = MANA_D
                        glow(x, y, MANA)
                elif mat == "ribcage":
                    c = paint_ribcage(face, i, j, fw, fh, n, c, x, y, glow)
                elif mat in ("mount", "mount_disc"):
                    if face in ("left", "right", "front") and mat == "mount_disc":
                        cx, cy = (fw - 1) / 2, (fh - 1) / 2
                        r = max(abs(i - cx), abs(j - cy))
                        if 1.0 <= r < 1.6:
                            c = MANA_D
                            glow(x, y, MANA)
                        elif r < 1.0:
                            c = DEEP
                    elif mat == "mount" and side and j % 4 == 0:
                        c = GRANITE[0]
                elif mat == "sternum":
                    c = paint_sternum(face, i, j, fw, fh, c, x, y, glow, gl)
                elif mat in ("elbow", "wrist_cracked"):
                    c = CRACK if side and j in (0, fh - 1) else GRANITE[1]
                    if side and j == 1 and i % 6 == 2 and face == "front":
                        c = MANA_D
                        glow(x, y, MANA, ("command",))
                    if mat == "wrist_cracked" and side and i == fw // 2:
                        c = DEEP
                elif mat in ("forearm", "forearm_chipped"):
                    if side and (i + j) % 5 == 0 and n > 50:
                        c = GRANITE[0]  # diagonal scratches
                    if mat == "forearm_chipped" and face == "front" and 2 <= j <= 5 and i == fw // 2:
                        c = MANA_D
                        glow(x, y, MANA)
                elif mat == "hand":
                    c = GRANITE[1] if n > 30 else GRANITE[0]
                elif mat == "fingers":
                    c = GRANITE[0] if i % 2 == 1 else (GRANITE[3] if j == fh - 1 else GRANITE[2])
                elif mat == "grip":
                    c = BASALT[0] if j % 2 else BASALT[1]
                elif mat == "blade":
                    c = BASALT[1] if n > 20 else BASALT[0]
                    if face in ("left", "right") and i == 0:
                        c = BASALT[3]  # the lighter edge (-Z)
                    if face in ("left", "right") and i == fw // 2 and 1 <= j <= fh - 2:
                        c = BASALT[0]
                        if j % 3 == 0:
                            glow(x, y, MANA)  # mana groove
                elif mat == "blade_tip":
                    c = BASALT[1] if face != "top" else BASALT[2]
                    if face in ("left", "right") and i == 0:
                        c = BASALT[3]
                elif mat == "wing_bone":
                    c = tone(GRANITE, t, n) if side else GRANITE[2]
                    if face == "top":
                        c = FACE[2]  # a cold light along the leading edge
                elif mat == "wing_plate":
                    c = GRANITE[1] if (i // 2 + j // 3) % 2 == 0 else BASALT[2]
                    if j >= fh - 2:
                        c = BASALT[0]  # almost black tips
                    if (i + j) % 7 == 0:
                        c = CRACK
                elif mat == "wing_tip":
                    c = BASALT[0]
                elif mat == "tablet":
                    c = paint_tablet(name, face, i, j, fw, fh, n, c, x, y, gl)
                elif mat == "face":
                    c = paint_face(face, i, j, fw, fh, c, x, y, glow)
                elif mat == "jaw_face":
                    c = paint_jaw(face, i, j, fw, fh, c)
                elif mat in ("teeth_up", "teeth_low"):
                    c = paint_teeth(mat, face, i, j, fw, fh)
                elif mat == "seal":
                    c = LILAC_D
                    if face == "front":
                        cx, cy = (fw - 1) / 2, (fh - 1) / 2
                        diamond = abs(i - cx) / (fw / 2) + abs(j - cy) / (fh / 2) <= 1.0
                        c = LILAC_D if diamond else FACE[1]
                        if diamond:
                            glow(x, y, LILAC, ("glow", "command", "waking"))
                elif mat in ("mask_left", "mask_right"):
                    c = paint_mask(mat, face, i, j, fw, fh, n, x, y, gl)
                elif mat == "jaw_stone" and face == "top":
                    c = VOID
                elif mat == "face_stone" and name == "cranium" and face == "bottom":
                    c = VOID
                if c is not None:
                    px[x, y] = c


def paint_face(face, i, j, fw, fh, c, x, y, glow):
    """The true face, 14x13 texels (2x): eyes, cheek slits, smile corners. Rows from the top (y=53) down."""
    if face != "front":
        return FACE[1]
    c = FACE[1]
    if j <= 1:
        return FACE[0]  # the hood's shadow on the forehead
    if j == 2 and (i < 2 or i > fw - 3):
        return FACE[0]
    # eyes: narrow almonds tilted up at the outer corner, sclera black, a coral ring iris
    for ex0 in (1, 8):
        outer_up = ex0 == 1
        for di in range(5):
            row = 6 if (di == (0 if outer_up else 4)) else 7
            if j == row and i == ex0 + di:
                return VOID
            if j == 7 and i == ex0 + di and 0 < di < 4:
                if di == 2:
                    glow(x, y, CORAL_HOT)
                    return CORAL_D
                if di in (1, 3):
                    glow(x, y, CORAL)
                    return CORAL_D
                return VOID
        if j == 8 and ex0 < i < ex0 + 4:
            return FACE[0]  # lower lid shadow
    if j == 5 and 1 <= i <= 12 and i not in (6, 7):
        return FACE[0]  # brow shadow
    # cheek slits (zygomatic): from below the outer eye corner, diagonally down then out to the side
    for (ci, cj) in ((1, 9), (0, 10), (12, 9), (13, 10)):
        if (i, j) == (ci, cj):
            return DEEP
    if (j == 9 and i in (2, 11)) or (j == 10 and i in (1, 12)):
        return FACE[2]  # light rim on the other side of the cut
    # cheekbones catch the light
    if j == 8 and i in (1, 12):
        return FACE[2]
    # smile corners pulled up, lips retracted (the teeth plate covers the mouth centre)
    if j == 10 and (i in (2, 3, 10, 11)):
        return CRACK
    if j in (11, 12) and i in (0, 1, 12, 13):
        return CRACK if j == 11 else FACE[0]
    if j == 9 and 4 <= i <= 9:
        return FACE[2] if i in (6, 7) else FACE[1]
    return c


def paint_jaw(face, i, j, fw, fh, c):
    if face != "front":
        return FACE[1]
    if j == 0:
        return VOID  # inside the open mouth line
    if j == 1 and 1 <= i <= fw - 2:
        return CRACK  # lower lip shadow
    if i in (0, fw - 1) or (j >= 2 and i in (1, fw - 2) and j % 2 == 0):
        return DEEP  # mandibular slits: the real joint of the jaw piece
    if j in (3, 4) and 3 <= i <= fw - 4 and (i + j) % 3 == 0:
        return FACE[0]  # chin tension lines
    return FACE[2] if j == fh - 1 else FACE[1]


def paint_teeth(mat, face, i, j, fw, fh):
    """Two flat rows of human teeth: 2-texel teeth with 1-texel dark gaps, no points."""
    if face != "front":
        return VOID
    gap = (i % 3 == 2) if mat == "teeth_up" else ((i + 1) % 3 == 2)  # the lower row is offset half a tooth
    if gap:
        return CRACK
    if mat == "teeth_up":
        return TABLET[2] if j == 0 else TABLET[1]
    return TABLET[1] if j == fh - 1 else TABLET[2]


def paint_mask(mat, face, i, j, fw, fh, n, x, y, gl):
    """Serene angelic half-mask (inert stone); the waking glow lights it white-blue. Mirrored eye positions on purpose."""
    c = TABLET[1] if n > 15 else TABLET[0]
    if face != "front":
        return TABLET[0]
    left = mat == "mask_left"
    inner = 0 if not left else fw - 1  # the column at the centre line of the face (cut edge)
    centre = (fw - 1 - i) if left else i
    if i == inner and j % 3 == 1:
        c = CRACK  # jagged break along the vertical split
    if j < 2:
        c = TABLET[2]
    if j == 6 and 2 <= centre <= 5:
        c = TABLET[0]  # carved closed eye
    if j == 5 and 2 <= centre <= 5:
        c = TABLET[2]
    if j in (11, 12) and centre <= 3 and centre >= 0 and (j == 12 or centre == 3):
        c = TABLET[0]  # calm half-smile
    if j >= 8 and j <= 10 and centre == 0:
        c = TABLET[2]  # nose ridge
    gl["waking"][x, y] = MASK_GLOW if c != CRACK else LILAC
    return c


def paint_sternum(face, i, j, fw, fh, c, x, y, glow, gl):
    if face != "front":
        return GRANITE[1]
    c = GRANITE[2]
    cx = (fw - 1) / 2
    for cy in (1.5, 3, 4.5):
        if abs(i - cx) + abs(j - cy) * 1.2 <= 1.1:
            gl["command"][x, y] = LILAC
            if cy == 3:
                gl["glow"][x, y] = LILAC_D
            return LILAC_D
    if j in (0, fh - 1) or i in (0, fw - 1):
        return GRANITE[0]
    return c


def paint_ribcage(face, i, j, fw, fh, n, c, x, y, glow):
    if face == "front":
        # rib plates without a skeleton look: horizontal grooves on the sides of the chest
        if i < 4 or i > fw - 5:
            if j % 3 == 2:
                c = GRANITE[0]
        # mana veins from the sternum (centre) to the six shoulder sockets, right-angled like a circuit
        cx = fw // 2
        for row in (3, 9, 13):  # top, mid, low socket heights (from the top of the ribcage)
            if j == row and (i <= cx - 3 or i >= cx + 2) and (i < 2 or i > fw - 3 or True):
                if 1 <= i <= fw - 2:
                    glow(x, y, MANA, ("command",))
                    if row == 9:
                        glow(x, y, MANA, ("glow",))
                    return MANA_D
        if (i in (cx - 3, cx + 2)) and 3 <= j <= 13:
            glow(x, y, MANA, ("command",))
            return MANA_D
    return c


def paint_tablet(name, face, i, j, fw, fh, n, c, x, y, gl):
    if face == "front" and not name.startswith("tablet_top"):
        if j in (2, 5, 8) and 1 <= i <= fw - 2 and (i + j) % 3 != 0:
            gl["waking"][x, y] = LILAC
            return TABLET[0]  # three lines of glyphs: adoration, praise, proof of faith
        inner = fw - 1 if name.endswith("left") else 0
        if i == inner and j % 2 == 0:
            return CRACK
    if (i in (0, fw - 1) or j in (0, fh - 1)) and n < 30:
        return (98, 93, 92, 255)  # mineral moss on the corners
    return c


# ---------------------------------------------------------------------------------------------------------------
def build_images(layout):
    img = Image.new("RGBA", (ATLAS, ATLAS), CLEAR)
    glows = {key: Image.new("RGBA", (ATLAS, ATLAS), CLEAR) for key in ("glow", "command", "waking")}
    for b in BOXES:
        u, v = layout[b[0]]
        paint(img, glows, b, u, v)
    # the irises and the seal glow in every lit layer
    return img, glows


def preview(img, layout):
    big = img.resize((ATLAS * 3, ATLAS * 3), Image.Resampling.NEAREST).convert("RGBA")
    draw = ImageDraw.Draw(big)
    for b in BOXES:
        u, v = layout[b[0]]
        fw, fh = footprint(*dims(b))
        draw.rectangle((u * 3, v * 3, (u + fw) * 3 - 1, (v + fh) * 3 - 1), outline=(255, 0, 255, 120))
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    big.save(PREVIEW)


def fmt(v):
    return f"{round(v, 4):g}F"


def write_java(layout):
    lines = [
        "package com.codenamecoraline.client.architect;",
        "",
        "import net.minecraft.client.model.geom.builders.CubeDeformation;",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "",
        "/**",
        " * GENERATED by tools/generate_architect_assets.py: do not edit by hand.",
        " * The Architect's rig (spec docs/design/architect.md §5): every part with its parent and its pivot offset from the",
        " * parent in model pixels, and every cube with its UV origin in the 256x256 atlas. Parents come before children.",
        " */",
        "public final class ArchitectParts {",
        f"    public static final int ATLAS = {ATLAS};",
        "",
        "    /** One rig part: name, parent (null for the root's children), offset from the parent's pivot. */",
        "    public record Part(String name, String parent, float x, float y, float z) {}",
        "",
        "    public static final Part[] PARTS = {",
    ]
    for name, (parent, x, y, z) in PARTS.items():
        if parent is None:
            continue
        _, ppx, ppy, ppz = PARTS[parent]
        jparent = "null" if parent == "root" else f'"{parent}"'
        lines.append(f'            new Part("{name}", {jparent}, {fmt(x - ppx)}, {fmt(ppy - y)}, {fmt(z - ppz)}),')
    lines += ["    };", "", "    private ArchitectParts() {}", "",
              "    /** Cubes of one model part. */",
              "    public static CubeListBuilder cubes(String part) {",
              "        CubeListBuilder b = CubeListBuilder.create();",
              "        switch (part) {"]
    by_part = {}
    for b in BOXES:
        by_part.setdefault(b[1], []).append(b)
    for owner, boxes in by_part.items():
        _, px_, py_, pz_ = PARTS[owner]
        lines.append(f'            case "{owner}" -> {{')
        for b in boxes:
            name, _, x0, y0, z0, x1, y1, z1, mat, k = b
            u, v = layout[name]
            w, h, d = x1 - x0, y1 - y0, z1 - z0
            W, H, D = dims(b)
            # model-space min corner relative to the pivot; the cube is declared in whole texels and shrunk back
            # by a negative deformation g: it spans [x - g, x + W + g], so declare x = min + g
            mx, my, mz = x0 - px_, py_ - y1, z0 - pz_
            gx, gy, gz = (w - W) / 2, (h - H) / 2, (d - D) / 2
            if abs(gx) + abs(gy) + abs(gz) < 1e-6:
                lines.append(f"                b.texOffs({u}, {v}).addBox({fmt(mx)}, {fmt(my)}, {fmt(mz)}, "
                             f"{fmt(W)}, {fmt(H)}, {fmt(D)}); // {name}")
            else:
                lines.append(f"                b.texOffs({u}, {v}).addBox({fmt(mx + gx)}, {fmt(my + gy)}, {fmt(mz + gz)}, "
                             f"{fmt(W)}, {fmt(H)}, {fmt(D)}, new CubeDeformation({fmt(gx)}, {fmt(gy)}, {fmt(gz)})); "
                             f"// {name}{' (2x texels)' if k == 2 else ''}")
        lines.append("            }")
    lines += ["            default -> {", "            }", "        }", "        return b;", "    }", "}", ""]
    JAVA_OUT.parent.mkdir(parents=True, exist_ok=True)
    JAVA_OUT.write_text("\n".join(lines), encoding="utf-8", newline="\n")


# ---------------------------------------------------------------------------------------------------------------
# Items
OUT = VOID


def outlined(px, size=16):
    for y in range(size):
        for x in range(size):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < size and 0 <= ny < size and px[nx, ny][3] > 0 and px[nx, ny] != OUT:
                        px[x, y] = OUT
                        break


def icon_egg():
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 8.3
            if (dx / 6.2) ** 2 + (dy / 7.4) ** 2 <= 1:
                n = noise(x, y, 41)
                px[x, y] = FACE[0] if n < 22 or x > 10 else FACE[2] if n > 80 or x < 5 else FACE[1]
    for x in range(5, 11):
        for y in range(5, 9):
            px[x, y] = OUT
    px[6, 7] = CORAL
    px[9, 7] = CORAL
    for c in ((7, 3), (8, 3), (7, 4)):
        px[c] = LILAC
    for c in ((5, 12), (6, 12), (10, 11), (10, 12), (8, 14)):
        px[c] = MANA
    outlined(px)
    return img


def icon_tablet():
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(1, 15):
        half = 5 if y < 4 else 4
        for x in range(8 - half, 8 + half):
            n = noise(x, y, 7)
            px[x, y] = TABLET[0] if n < 15 or x == 8 + half - 1 or y == 14 else TABLET[2] if n > 85 or y == 1 else TABLET[1]
    for row in (4, 7, 10):
        for x in range(5, 11):
            if (x + row) % 3 != 0:
                px[x, row] = GRANITE[0]
    for (x, y) in ((8, 2), (7, 4), (8, 6), (7, 8), (8, 10), (7, 12), (8, 13)):
        px[x, y] = CRACK
    px[7, 2] = LILAC
    outlined(px)
    return img


def main():
    build_rig()
    layout = pack()
    used = check_overlap(layout)
    img, glows = build_images(layout)
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    img.save(ENTITY_DIR / "architect.png")
    glows["glow"].save(ENTITY_DIR / "architect_glow.png")
    glows["command"].save(ENTITY_DIR / "architect_glow_command.png")
    glows["waking"].save(ENTITY_DIR / "architect_glow_waking.png")
    write_java(layout)
    import architect_java_animations  # after build_rig: the clips only need the pose data
    clip_count = architect_java_animations.write()
    preview(img, layout)
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    icon_egg().save(ITEM_DIR / "architect_spawn_egg.png")
    icon_tablet().save(ITEM_DIR / "commandment_tablet.png")
    print(f"Generated Architect rig ({len(PARTS)} parts, {len(BOXES)} boxes), atlas ({used} texels used, no UV overlap), "
          f"3 glow layers, {clip_count} animation clips and item art.")


if __name__ == "__main__":
    main()
