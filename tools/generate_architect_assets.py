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

Coordinates (spec §3.1): geometry is written in absolute Blockbench coordinates of the ORIGINAL 3.35-block body,
pixels, origin on the ground, +Y up, front -Z, +X = the character's LEFT. The reform scales every coordinate by
SCALE (1.4: the body is 4.7 blocks tall) when it is registered, then converts to model space (y_model = 24 - Y) and to
offsets relative to each part's pivot. Every box is drawn at the texel density of its material (DENS: the face 3
texels per model pixel, hood, torso and coat 2, limbs 1.5): the box is declared in whole texels and shrunk back to its
real size with a negative CubeDeformation. The painters live in tools/architect_paint.py.

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
ATLAS = 512
GAP = 1  # empty texels between UV islands (anti-bleeding)

from architect_paint import *  # noqa: E402,F401,F403  (palettes and the painters)
from architect_poses import RIG_SCALE as SCALE  # noqa: E402

# ---------------------------------------------------------------------------------------------------------------
# Rig: name -> (parent, pivot X, pivot Y, pivot Z) in absolute Blockbench coordinates. Order = parents first.
PARTS = {}
BOXES = []  # (name, part, x0, y0, z0, x1, y1, z1, material, density)


# texel density of each material (texels per model pixel); the box is declared in whole texels and shrunk back
DENS = {"face": 3, "jaw_face": 3, "teeth_up": 3, "teeth_low": 3, "seal": 3, "mask_left": 3, "mask_right": 3, "sternum": 3,
        "face_stone": 2, "jaw_stone": 2, "hood": 2, "void": 1, "ribcage": 2, "abdomen": 2, "mount": 1.5, "mount_disc": 2,
        "coat": 2, "coat_trim": 2, "collar": 2, "coat_hem": 2, "granite": 1.5, "granite_light": 2, "elbow": 2,
        "forearm": 2, "forearm_chipped": 2, "wrist_cracked": 2, "hand": 2, "fingers": 2, "basalt_light": 2, "grip": 2,
        "blade": 2, "blade_tip": 2, "wing_bone": 1.5, "wing_plate": 2, "wing_tip": 1.5, "tablet": 2}


def part(name, parent, x, y, z):
    assert name not in PARTS, name
    assert parent is None or parent in PARTS, (name, parent)
    if name != "root":
        x, y, z = x * SCALE, y * SCALE, z * SCALE
    PARTS[name] = (parent, x, y, z)


def box(name, owner, x0, y0, z0, x1, y1, z1, mat, density=None):
    assert owner in PARTS, owner
    BOXES.append((name, owner, x0 * SCALE, y0 * SCALE, z0 * SCALE, x1 * SCALE, y1 * SCALE, z1 * SCALE, mat,
                  DENS.get(mat, 1.5)))


def span(a, b, s):
    """X range of a box on the side s (+1 left, -1 right) from magnitudes a < b."""
    return (a * s, b * s) if s > 0 else (b * s, a * s)


def build_rig():
    part("root", None, 0, 24, 0)
    # ---- pelvis, robe, legs
    part("pelvis", "root", 0, 22, 0)
    box("pelvis_plate", "pelvis", -5, 20, -3.5, 5, 24, 3.5, "granite")
    build_coat_skirt()
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
    build_coat_upper()
    # ---- six arms (spec §4.4): hanging chains, the idle pose comes from the animations' BASE
    for tier, (sx, sy, sz) in (("top", (10.5, 41, -1)), ("mid", (11.5, 35, 0.5)), ("low", (10, 30, 1.5))):
        for side, s in (("left", 1), ("right", -1)):
            arm(tier, side, s, sx * s, sy, sz)
    # ---- wings (spec §4.5): built open along +-X, folded by the animations
    for side, s in (("left", 1), ("right", -1)):
        wing(side, s)


def build_coat_skirt():
    """The long stone coat below the waist, in independent pieces so it can lag behind the body (reform 4.2): the front
    panels end near the knees (two hinged segments and a jagged hem each), the back is longer (to the ankles), the
    sides are thin flaps. All in original pixels; part() and box() apply the scale."""
    for side, s in (("left", 1), ("right", -1)):
        part(f"coat_front_{side}_upper", "pelvis", 3.9 * s, 24.4, -4.9)
        a, b = span(1.3, 7.2, s)
        box(f"coat_front_{side}_upper", f"coat_front_{side}_upper", a, 13.0, -5.6, b, 24.6, -4.2, "coat")
        # an engraved trim band across the waist
        box(f"coat_belt_{side}", f"coat_front_{side}_upper", a - 0.2 * s, 22.4, -5.8, b + 0.2 * s, 24.0, -4.0, "coat_trim")
        part(f"coat_front_{side}_lower", f"coat_front_{side}_upper", 3.9 * s, 13.0, -4.9)
        a, b = span(1.3, 7.4, s)
        box(f"coat_front_{side}_lower", f"coat_front_{side}_lower", a, 8.6, -5.9, b, 13.2, -4.4, "coat")
        # the hem breaks into three teeth of different lengths
        for k, (x0, x1, y0) in enumerate(((1.3, 3.5, 6.4), (3.5, 5.6, 7.4), (5.6, 7.4, 6.9))):
            a2, b2 = span(x0, x1, s)
            box(f"coat_hem_front_{side}_{k}", f"coat_front_{side}_lower", a2, y0, -5.9, b2, 8.8, -4.4, "coat_hem")
        part(f"coat_side_{side}", "pelvis", 5.2 * s, 24.4, 0)
        a, b = span(5.0, 6.8, s)
        box(f"coat_side_{side}", f"coat_side_{side}", a, 9.2, -3.6, b, 24.6, 3.8, "coat")
        a, b = span(5.0, 6.6, s)
        box(f"coat_side_hem_{side}", f"coat_side_{side}", a, 6.4, -2.4, b, 9.4, 2.8, "coat_hem")
    part("coat_back_upper", "pelvis", 0, 24.4, 4.3)
    box("coat_back_upper", "coat_back_upper", -5.7, 11.5, 3.7, 5.7, 24.6, 5.3, "coat")
    box("coat_back_trim", "coat_back_upper", -5.9, 22.4, 3.5, 5.9, 24.0, 5.5, "coat_trim")
    part("coat_back_lower", "coat_back_upper", 0, 11.5, 4.7)
    box("coat_back_lower", "coat_back_lower", -5.9, 3.4, 4.2, 5.9, 11.7, 5.8, "coat")
    for k, (x0, x1, y0) in enumerate(((-5.9, -3.0, 0.8), (-3.0, 0.0, 1.9), (0.0, 3.0, 0.4), (3.0, 5.9, 1.5))):
        box(f"coat_hem_back_{k}", "coat_back_lower", x0, y0, 4.2, x1, 3.6, 5.8, "coat_hem")


def build_coat_upper():
    """The coat above the waist: a high stiff collar, panelled lapels on the chest and broad shoulder plates; static
    with the torso (they follow it), the long pieces below are what moves."""
    part("coat_collar", "torso", 0, 43, 2)
    box("collar_back", "coat_collar", -6.6, 41.5, 5.1, 6.6, 49.6, 6.7, "collar")
    for side, s in (("left", 1), ("right", -1)):
        a, b = span(5.6, 7.3, s)
        box(f"collar_{side}", "coat_collar", a, 41.0, -3.5, b, 48.4, 5.3, "collar")
        a, b = span(3.8, 6.4, s)
        box(f"collar_tip_{side}", "coat_collar", a, 43.0, -4.7, b, 49.2, -3.4, "collar")
        part(f"coat_lapel_{side}", "torso", 5.2 * s, 43, -4.6)
        a, b = span(2.6, 8.0, s)
        box(f"lapel_{side}", f"coat_lapel_{side}", a, 33.0, -5.5, b, 44.0, -4.1, "coat")
        a, b = span(2.1, 2.9, s)
        box(f"lapel_edge_{side}", f"coat_lapel_{side}", a, 32.0, -5.9, b, 44.6, -4.3, "coat_trim")
        a, b = span(6.6, 8.9, s)
        box(f"lapel_peak_{side}", f"coat_lapel_{side}", a, 43.0, -5.4, b, 46.2, -3.6, "coat_trim")
        part(f"coat_shoulder_{side}", "torso", 10.5 * s, 44, 0)
        a, b = span(7.2, 13.9, s)
        box(f"coat_shoulder_{side}", f"coat_shoulder_{side}", a, 43.4, -4.9, b, 46.0, 4.9, "coat_trim")
        a, b = span(12.7, 14.5, s)
        box(f"coat_shoulder_drop_{side}", f"coat_shoulder_{side}", a, 36.2, -4.3, b, 43.6, 4.3, "coat")


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
    """Declared (texel) size of a box: whole texels at the material's density; the CubeDeformation shrinks it back."""
    _, _, x0, y0, z0, x1, y1, z1, _, k = b
    return (max(1, ceil((x1 - x0) * k - 1e-6)), max(1, ceil((y1 - y0) * k - 1e-6)), max(1, ceil((z1 - z0) * k - 1e-6)))


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
    seed = sum((n + 1) * ord(c) for n, c in enumerate(name)) % 9973
    for face, (fx0, fy0, fw, fh) in faces_of(u, v, w, h, d).items():
        f = Face(name, face, fw, fh, seed * 31 + sum(ord(c) for c in face))
        for j in range(fh):
            for i in range(fw):
                x, y = fx0 + i, fy0 + j

                def glow(key, color, x=x, y=y):
                    gl[key][x, y] = color
                c = texel(mat, name, f, i, j, glow)
                if c is not None:
                    px[x, y] = c


def texel(mat, name, f, i, j, glow):
    if mat == "face":
        return paint_face(f, i, j, glow)
    if mat == "jaw_face":
        return paint_jaw(f, i, j, glow)
    if mat in ("teeth_up", "teeth_low"):
        return paint_teeth(mat, f, i, j, glow)
    if mat == "seal":
        return paint_seal(f, i, j, glow)
    if mat in ("mask_left", "mask_right"):
        return paint_mask(mat, f, i, j, glow)
    if mat == "sternum":
        return paint_sternum(f, i, j, glow)
    if mat == "ribcage":
        return paint_ribcage(f, i, j, glow)
    if mat == "abdomen":
        return paint_abdomen(f, i, j, glow)
    if mat in ("mount", "mount_disc"):
        return paint_mount(mat, f, i, j, glow)
    if mat in ("granite", "granite_light", "elbow", "wrist_cracked", "forearm", "forearm_chipped", "hand", "fingers"):
        return paint_limb(mat, f, i, j, glow, name)
    if mat in ("blade", "blade_tip", "grip", "basalt_light"):
        return paint_blade(mat, f, i, j, glow)
    if mat in ("wing_bone", "wing_plate", "wing_tip"):
        return paint_wing(mat, f, i, j, glow)
    if mat == "tablet":
        return paint_tablet(name, f, i, j, glow)
    if mat in ("coat", "coat_trim", "collar", "coat_hem"):
        return paint_coat(mat, name, f, i, j, glow)
    if mat == "void":
        return VOID
    if mat == "hood":
        # the inside of the hood is black; the outside is stacked plates of cold stone
        inner = {"hood_side_left": "right", "hood_side_right": "left", "hood_shell_back": "front", "hood_crown": "front"}
        if f.face == "bottom" or f.face == inner.get(name) or (name.startswith("hood_side") and f.face == "back"):
            return VOID
        return plates(FACE, f, i, j, 6, 7, 5, 3, -0.02)
    if mat == "jaw_stone":
        return VOID if f.face == "top" else stone(FACE, f, i, j, 2)
    if mat == "face_stone":
        if name == "cranium" and f.face == "bottom":
            return VOID
        c = stone(FACE, f, i, j, 2)
        if name == "nose" and f.face == "front" and f.side:  # a lit ridge down the front of the statue's nose
            return FACE[3] if i == f.fw // 2 else c
        return c
    return stone(GRANITE, f, i, j, 3)


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
    big = img.resize((ATLAS * 2, ATLAS * 2), Image.Resampling.NEAREST).convert("RGBA")
    draw = ImageDraw.Draw(big)
    for b in BOXES:
        u, v = layout[b[0]]
        fw, fh = footprint(*dims(b))
        draw.rectangle((u * 2, v * 2, (u + fw) * 2 - 1, (v + fh) * 2 - 1), outline=(255, 0, 255, 120))
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
