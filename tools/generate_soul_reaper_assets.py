#!/usr/bin/env python3
"""Generate textures for Soul Reaper, Omen Hare, and Wandering Soul Wisp with UV non-overlap verification."""

from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
TEX_DIR = ROOT / "src" / "main" / "resources" / "assets" / "codenamecoraline" / "textures"
ITEM_TEX_DIR = TEX_DIR / "item"
ENTITY_TEX_DIR = TEX_DIR / "entity"


def get_box_uv_rects(u: int, v: int, dx: int, dy: int, dz: int) -> dict[str, tuple[int, int, int, int]]:
    """Return standard Minecraft box UV face rectangles (x0, y0, x1_exclusive, y1_exclusive)."""
    return {
        "top": (u + dz, v, u + dz + dx, v + dz),
        "bottom": (u + dz + dx, v, u + dz + 2 * dx, v + dz),
        "right": (u, v + dz, u + dz, v + dz + dy),
        "front": (u + dz, v + dz, u + dz + dx, v + dz + dy),
        "left": (u + dz + dx, v + dz, u + 2 * dz + dx, v + dz + dy),
        "back": (u + 2 * dz + dx, v + dz, u + 2 * dz + 2 * dx, v + dz + dy),
    }


def verify_uv_non_overlap(tex_w: int, tex_h: int, boxes: list[tuple[str, int, int, int, int, int]]) -> None:
    """Assert that no two box faces overlap and all faces fit within (tex_w, tex_h)."""
    owner: dict[tuple[int, int], str] = {}
    for name, u, v, dx, dy, dz in boxes:
        rects = get_box_uv_rects(u, v, dx, dy, dz)
        for face_name, (x0, y0, x1, y1) in rects.items():
            if x0 < 0 or y0 < 0 or x1 > tex_w or y1 > tex_h:
                raise ValueError(f"UV out of bounds for {name}.{face_name}: {(x0, y0, x1, y1)} in {tex_w}x{tex_h}")
            for py in range(y0, y1):
                for px in range(x0, x1):
                    prev = owner.get((px, py))
                    if prev is not None:
                        raise ValueError(f"UV overlap at ({px}, {py}) between {prev} and {name}.{face_name}")
                    owner[(px, py)] = f"{name}.{face_name}"


def paint_box(
    img: Image.Image,
    u: int,
    v: int,
    dx: int,
    dy: int,
    dz: int,
    base: tuple[int, int, int, int],
    trim: tuple[int, int, int, int] | None = None,
    accent: tuple[int, int, int, int] | None = None,
    tattered_bottom: bool = False,
) -> None:
    """Paint all 6 faces of a Minecraft box with directional shading, border trim, and optional tattered hem."""
    rects = get_box_uv_rects(u, v, dx, dy, dz)
    face_mult = {
        "top": 1.14,
        "front": 1.04,
        "left": 0.92,
        "right": 0.88,
        "back": 0.84,
        "bottom": 0.72,
    }
    for face_name, (x0, y0, x1, y1) in rects.items():
        mult = face_mult[face_name]
        w = x1 - x0
        h = y1 - y0
        for ry in range(h):
            for rx in range(w):
                px, py = x0 + rx, y0 + ry
                if tattered_bottom and face_name in ("front", "back", "left", "right") and ry == h - 1:
                    if (rx + u) % 2 == 0:
                        continue
                grain = ((px * 13 + py * 29) % 9 - 4) * 2
                r = max(0, min(255, int(base[0] * mult) + grain))
                g = max(0, min(255, int(base[1] * mult) + grain))
                b = max(0, min(255, int(base[2] * mult) + grain))
                a = base[3]
                if trim is not None and (rx == 0 or rx == w - 1 or ry == 0 or ry == h - 1):
                    r = (r + trim[0]) // 2
                    g = (g + trim[1]) // 2
                    b = (b + trim[2]) // 2
                if accent is not None and face_name == "front" and 1 <= rx < w - 1 and ry == h // 2:
                    r, g, b = accent[0], accent[1], accent[2]
                img.putpixel((px, py), (r, g, b, a))


SOUL_REAPER_BOXES: list[tuple[str, int, int, int, int, int]] = [
    # Row 0 (v=0)
    ("upper_shroud", 0, 0, 16, 10, 10),
    ("chest", 56, 0, 14, 11, 8),
    ("head", 104, 0, 8, 8, 8),
    ("pelvis", 140, 0, 10, 5, 6),
    ("lower_shroud_ring", 176, 0, 12, 6, 8),
    ("lower_spine", 220, 0, 6, 7, 4),
    # Row 1 (v=24)
    ("scythe_blade_base", 0, 24, 16, 6, 2),
    ("scythe_blade_tip", 40, 24, 14, 5, 1),
    ("scythe_collar", 74, 24, 5, 5, 5),
    ("scythe_pommel", 98, 24, 4, 5, 4),
    ("scythe_shaft_upper", 118, 24, 2, 24, 2),
    ("scythe_shaft_lower", 130, 24, 2, 18, 2),
    ("grip_upper", 142, 24, 3, 4, 3),
    ("grip_lower", 158, 24, 3, 4, 3),
    ("neck", 174, 24, 4, 4, 4),
    ("jaw", 194, 24, 6, 3, 4),
    ("cowl_rim", 218, 24, 10, 3, 4),
    # Row 2 (v=54)
    ("cowl_left_fold", 0, 54, 2, 10, 7),
    ("cowl_right_fold", 22, 54, 2, 10, 7),
    ("cowl_tail_1", 44, 54, 6, 9, 2),
    ("cowl_tail_2", 64, 54, 4, 9, 2),
    ("rib_flaps_left", 80, 54, 6, 8, 2),
    ("rib_flaps_right", 100, 54, 6, 8, 2),
    ("left_clavicle", 120, 54, 5, 4, 6),
    ("right_clavicle", 146, 54, 5, 4, 6),
    ("left_upper_arm", 172, 54, 3, 12, 4),
    ("right_upper_arm", 190, 54, 3, 12, 4),
    ("left_forearm", 208, 54, 3, 11, 3),
    ("right_forearm", 224, 54, 3, 11, 3),
    # Row 3 (v=76)
    ("left_Sleeve_drape_1", 0, 76, 2, 9, 5),
    ("left_sleeve_drape_2", 18, 76, 2, 8, 4),
    ("right_sleeve_drape_1", 34, 76, 2, 9, 5),
    ("right_sleeve_drape_2", 52, 76, 2, 8, 4),
    ("left_hand", 68, 76, 3, 4, 2),
    ("left_thumb", 82, 76, 1, 3, 1),
    ("left_index_group", 90, 76, 2, 4, 1),
    ("left_middle_ring_group", 100, 76, 2, 4, 1),
    ("left_pinky_claw", 110, 76, 1, 4, 1),
    ("right_hand", 118, 76, 3, 4, 2),
    ("right_thumb", 132, 76, 1, 3, 1),
    ("right_index_group", 140, 76, 2, 4, 1),
    ("right_middle_ring_group", 150, 76, 2, 4, 1),
    ("right_pinky_claw", 160, 76, 1, 4, 1),
    ("waist_chains", 168, 76, 10, 6, 2),
    # Row 4 (v=96)
    ("front_left_strip_1", 0, 96, 4, 8, 1),
    ("front_left_strip_2", 14, 96, 4, 8, 1),
    ("front_left_strip_3", 28, 96, 3, 7, 1),
    ("front_right_strip_1", 40, 96, 4, 8, 1),
    ("front_right_strip_2", 54, 96, 4, 8, 1),
    ("front_right_strip_3", 68, 96, 3, 7, 1),
    ("side_left_strip_1", 80, 96, 1, 8, 6),
    ("side_left_strip_2", 98, 96, 1, 8, 6),
    ("side_left_strip_3", 116, 96, 1, 7, 5),
    ("side_right_strip_1", 132, 96, 1, 8, 6),
    ("side_right_strip_2", 150, 96, 1, 8, 6),
    ("side_right_strip_3", 168, 96, 1, 7, 5),
    # Row 5 (v=116)
    ("back_center_strip_1", 0, 116, 8, 9, 1),
    ("back_center_strip_2", 22, 116, 7, 9, 1),
    ("back_center_strip_3", 42, 116, 6, 8, 1),
    ("spectral_tail_1", 60, 116, 5, 11, 4),
    ("spectral_tail_2", 82, 116, 3, 11, 3),
]

OMEN_HARE_BOXES: list[tuple[str, int, int, int, int, int]] = [
    ("body", 0, 0, 5, 5, 8),
    ("head", 28, 0, 4, 4, 5),
    ("chest_tuft", 48, 0, 4, 3, 2),
    ("left_ear", 0, 16, 2, 6, 1),
    ("right_ear", 8, 16, 2, 6, 1),
    ("tail", 16, 16, 3, 3, 2),
    ("nose", 28, 16, 2, 1, 1),
    ("left_Front_leg", 0, 26, 2, 3, 2),
    ("right_front_leg", 10, 26, 2, 3, 2),
    ("left_hind_leg", 20, 26, 2, 4, 3),
    ("right_hind_leg", 32, 26, 2, 4, 3),
]

WISP_BOXES: list[tuple[str, int, int, int, int, int]] = [
    ("core", 0, 0, 6, 6, 6),
    ("halo_ring", 0, 12, 8, 4, 8),
    ("tail_wisp", 0, 24, 4, 4, 4),
]


def generate_soul_reaper_textures() -> None:
    verify_uv_non_overlap(256, 256, SOUL_REAPER_BOXES)
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (256, 256), (0, 0, 0, 0))

    shroud_dark = (28, 36, 50, 255)
    shroud_mid = (36, 46, 64, 255)
    frost_trim = (146, 182, 212, 255)
    bone_pale = (196, 206, 218, 255)
    bone_dark = (118, 132, 150, 255)
    ash_wood = (48, 38, 44, 255)
    leather_grip = (72, 46, 52, 255)
    blade_steel = (176, 214, 236, 255)
    blade_edge = (224, 248, 255, 255)
    cyan_soul = (72, 216, 238, 255)
    silver_chain = (158, 174, 194, 255)

    for name, u, v, dx, dy, dz in SOUL_REAPER_BOXES:
        if "blade" in name:
            paint_box(img, u, v, dx, dy, dz, blade_steel, trim=blade_edge, accent=cyan_soul)
            # Glowing rune edge on emissive layer
            front = get_box_uv_rects(u, v, dx, dy, dz)["front"]
            for px in range(front[0] + 1, front[2] - 1, 2):
                eyes.putpixel((px, front[3] - 1), (110, 238, 255, 255))
        elif "shaft" in name:
            paint_box(img, u, v, dx, dy, dz, ash_wood, trim=bone_dark)
        elif "grip" in name:
            paint_box(img, u, v, dx, dy, dz, leather_grip, trim=frost_trim)
        elif name in ("scythe_collar", "scythe_pommel"):
            paint_box(img, u, v, dx, dy, dz, bone_pale, trim=silver_chain, accent=cyan_soul)
        elif name in ("lower_spine", "neck", "jaw", "rib_flaps_left", "rib_flaps_right") or "hand" in name or "thumb" in name or "group" in name or "claw" in name:
            paint_box(img, u, v, dx, dy, dz, bone_pale, trim=bone_dark)
        elif "arm" in name or "forearm" in name:
            paint_box(img, u, v, dx, dy, dz, bone_dark, trim=frost_trim)
        elif "spectral_tail" in name:
            paint_box(img, u, v, dx, dy, dz, (44, 118, 148, 235), trim=cyan_soul, tattered_bottom=True)
        elif name == "waist_chains":
            paint_box(img, u, v, dx, dy, dz, silver_chain, trim=frost_trim, tattered_bottom=True)
        elif name == "head":
            paint_box(img, u, v, dx, dy, dz, (12, 14, 22, 255), trim=shroud_dark)
            # Glowing abyssal crimson eyes on front of head beneath the cowl rim
            front = get_box_uv_rects(u, v, dx, dy, dz)["front"]
            fx0, fy0, _, _ = front
            for ex in (fx0 + 1, fx0 + 2, fx0 + 5, fx0 + 6):
                img.putpixel((ex, fy0 + 4), (255, 42, 74, 255))
                img.putpixel((ex, fy0 + 5), (210, 22, 52, 255))
                eyes.putpixel((ex, fy0 + 4), (255, 48, 80, 255))
                eyes.putpixel((ex, fy0 + 5), (220, 24, 56, 255))
        elif "strip_3" in name or "drape_2" in name or "tail_2" in name:
            paint_box(img, u, v, dx, dy, dz, shroud_dark, trim=frost_trim, tattered_bottom=True)
        elif name == "upper_shroud":
            paint_box(img, u, v, dx, dy, dz, shroud_mid, trim=frost_trim, accent=cyan_soul)
            # Central spiral brooch on upper_shroud front
            front = get_box_uv_rects(u, v, dx, dy, dz)["front"]
            cx = (front[0] + front[2]) // 2
            cy = front[1] + 3
            for ox in range(-2, 3):
                for oy in range(-2, 3):
                    if abs(ox) + abs(oy) <= 3:
                        img.putpixel((cx + ox, cy + oy), silver_chain if abs(ox) + abs(oy) == 3 else cyan_soul)
                        if abs(ox) + abs(oy) <= 1:
                            eyes.putpixel((cx + ox, cy + oy), (120, 244, 255, 255))
        else:
            paint_box(img, u, v, dx, dy, dz, shroud_mid, trim=frost_trim)

    img.save(ENTITY_TEX_DIR / "soul_reaper.png")
    eyes.save(ENTITY_TEX_DIR / "soul_reaper_eyes.png")


def generate_omen_hare_textures() -> None:
    verify_uv_non_overlap(64, 64, OMEN_HARE_BOXES)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    albino_white = (242, 246, 252, 255)
    frost_shadow = (204, 216, 232, 255)
    crimson_eye = (232, 28, 56, 255)
    pink_inner = (234, 188, 202, 255)

    for name, u, v, dx, dy, dz in OMEN_HARE_BOXES:
        if "ear" in name:
            paint_box(img, u, v, dx, dy, dz, albino_white, trim=frost_shadow, accent=pink_inner)
        elif name == "nose":
            paint_box(img, u, v, dx, dy, dz, (238, 128, 148, 255))
        else:
            paint_box(img, u, v, dx, dy, dz, albino_white, trim=frost_shadow)

        if name == "head":
            front = get_box_uv_rects(u, v, dx, dy, dz)["front"]
            left = get_box_uv_rects(u, v, dx, dy, dz)["left"]
            right = get_box_uv_rects(u, v, dx, dy, dz)["right"]
            # Intense ruby eyes on front and sides of head
            for px, py in [
                (front[0], front[1] + 1),
                (front[2] - 1, front[1] + 1),
                (left[0] + 1, left[1] + 1),
                (right[2] - 2, right[1] + 1),
            ]:
                img.putpixel((px, py), crimson_eye)
                eyes.putpixel((px, py), (255, 48, 76, 255))

    img.save(ENTITY_TEX_DIR / "omen_hare.png")
    eyes.save(ENTITY_TEX_DIR / "omen_hare_eyes.png")


def generate_wisp_texture() -> None:
    verify_uv_non_overlap(32, 32, WISP_BOXES)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    for name, u, v, dx, dy, dz in WISP_BOXES:
        if name == "core":
            paint_box(img, u, v, dx, dy, dz, (170, 248, 255, 245), trim=(84, 214, 242, 255))
        elif name == "halo_ring":
            paint_box(img, u, v, dx, dy, dz, (76, 198, 232, 190), trim=(190, 250, 255, 230))
        else:
            paint_box(img, u, v, dx, dy, dz, (54, 164, 208, 210), trim=(120, 230, 250, 220), tattered_bottom=True)
    img.save(ENTITY_TEX_DIR / "wandering_soul_wisp.png")


def make_egg(
    primary: tuple[int, int, int],
    secondary: tuple[int, int, int],
    accent: tuple[int, int, int],
) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rows = [
        (1, 6, 9),
        (2, 5, 10),
        (3, 4, 11),
        (4, 3, 12),
        (5, 3, 12),
        (6, 2, 13),
        (7, 2, 13),
        (8, 2, 13),
        (9, 2, 13),
        (10, 2, 13),
        (11, 3, 12),
        (12, 3, 12),
        (13, 4, 11),
        (14, 5, 10),
    ]
    spots = {(5, 4), (9, 5), (4, 8), (10, 9), (6, 11), (11, 12)}
    eyes_px = {(6, 7), (9, 7)}
    for y, x0, x1 in rows:
        for x in range(x0, x1 + 1):
            is_border = x == x0 or x == x1 or y in (1, 14)
            if (x, y) in eyes_px:
                color = accent
            elif (x, y) in spots:
                color = secondary
            elif is_border:
                color = tuple(max(0, int(c * 0.68)) for c in primary)
            else:
                shade = 1.08 if (x + y) < 13 else (0.92 if (x + y) > 17 else 1.0)
                color = tuple(min(255, int(c * shade)) for c in primary)
            img.putpixel((x, y), (*color, 255))
    return img


def generate_item_textures() -> None:
    # 1. ancient_ice_shard.png (16x16 crystalline glacial shard)
    shard = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    c_border = (36, 74, 114, 255)
    c_dark = (68, 128, 178, 255)
    c_mid = (118, 192, 232, 255)
    c_bright = (188, 238, 255, 255)
    c_glint = (242, 254, 255, 255)
    shard_rows = [
        (2, 10, 11),
        (3, 9, 12),
        (4, 8, 12),
        (5, 7, 11),
        (6, 6, 11),
        (7, 5, 10),
        (8, 5, 9),
        (9, 4, 9),
        (10, 4, 8),
        (11, 3, 7),
        (12, 3, 6),
        (13, 3, 5),
    ]
    for y, x0, x1 in shard_rows:
        for x in range(x0, x1 + 1):
            if x == x0 or x == x1 or y in (2, 13):
                col = c_border
            elif (x + y) % 5 == 0:
                col = c_glint
            elif x <= (x0 + x1) // 2:
                col = c_bright
            else:
                col = c_mid if (x + y) % 2 == 0 else c_dark
            shard.putpixel((x, y), col)
    shard.save(ITEM_TEX_DIR / "ancient_ice_shard.png")

    # 2. frozen_soul_essence.png (16x16 swirling soul orb in frost cage)
    essence = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(2, 14):
        for x in range(2, 14):
            dx = x - 7.5
            dy = y - 7.5
            dist = (dx * dx + dy * dy) ** 0.5
            if dist <= 5.6:
                if dist > 4.6:
                    col = (28, 56, 86, 255)
                elif dist > 3.3:
                    col = (58, 154, 196, 255) if (x + y) % 2 == 0 else (84, 198, 232, 255)
                elif dist > 1.8:
                    col = (146, 238, 252, 255)
                else:
                    col = (232, 254, 255, 255)
                essence.putpixel((x, y), col)
    essence.save(ITEM_TEX_DIR / "frozen_soul_essence.png")

    # 3. frost_spiral_brooch.png (16x16 silver spiral brooch with cyan gem)
    brooch = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    silver_rim = (82, 92, 112, 255)
    silver_mid = (156, 168, 188, 255)
    silver_hi = (218, 228, 242, 255)
    cyan_gem = (84, 224, 246, 255)
    for y in range(2, 14):
        for x in range(2, 14):
            dx = x - 7.5
            dy = y - 7.5
            dist = (dx * dx + dy * dy) ** 0.5
            if dist <= 5.5:
                if dist > 4.4:
                    col = silver_rim
                elif dist > 2.2:
                    spiral = ((x - 7) * 2 + (y - 7)) % 3 == 0
                    col = silver_hi if spiral else silver_mid
                else:
                    col = (220, 252, 255, 255) if dist < 1.1 else cyan_gem
                brooch.putpixel((x, y), col)
    brooch.save(ITEM_TEX_DIR / "frost_spiral_brooch.png")

    # 4. soul_reaper_scythe.png (16x16 diagonal haft + curved meteoric blade)
    scythe = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    wood_dark = (34, 42, 58, 255)
    wood_mid = (58, 70, 92, 255)
    steel_dark = (74, 86, 108, 255)
    steel_light = (176, 198, 224, 255)
    rune_cyan = (104, 234, 252, 255)
    # Haft from bottom-left (2,14) to top-right (12,4)
    for i in range(12):
        hx = 2 + i
        hy = 14 - i
        scythe.putpixel((hx, hy), wood_mid)
        if hx + 1 < 16:
            scythe.putpixel((hx + 1, hy), wood_dark)
    # Curved crescent blade sweeping from (11,3) across top-left to (3,7)
    blade_pts = [
        (12, 3), (11, 2), (10, 2), (9, 2), (8, 2), (7, 3), (6, 3), (5, 4), (4, 5), (3, 6), (3, 7),
        (11, 3), (10, 3), (9, 3), (8, 3), (7, 4), (6, 4), (5, 5), (4, 6),
        (10, 4), (9, 4), (8, 4), (7, 5),
    ]
    for idx, (bx, by) in enumerate(blade_pts):
        if by == 2 or bx <= 4:
            scythe.putpixel((bx, by), steel_light)
        elif idx % 4 == 0:
            scythe.putpixel((bx, by), rune_cyan)
        else:
            scythe.putpixel((bx, by), steel_dark)
    scythe.save(ITEM_TEX_DIR / "soul_reaper_scythe.png")

    # 5. soul_reaper_portrait.png (16x16 advancement icon: dark cowl + glowing cyan eyes)
    portrait = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(1, 15):
        half_w = min(6, 2 + y // 2)
        for x in range(8 - half_w, 8 + half_w):
            is_edge = x in (8 - half_w, 8 + half_w - 1) or y in (1, 14)
            inside_void = 5 <= x <= 10 and 5 <= y <= 11
            if (x, y) in {(6, 7), (9, 7)}:
                col = (112, 244, 255, 255)
            elif (x, y) in {(5, 7), (10, 7)}:
                col = (52, 168, 212, 255)
            elif inside_void:
                col = (8, 10, 16, 255)
            elif is_edge:
                col = (16, 20, 30, 255)
            else:
                col = (34, 42, 58, 255)
            portrait.putpixel((x, y), col)
    portrait.save(ITEM_TEX_DIR / "soul_reaper_portrait.png")


def main() -> None:
    ITEM_TEX_DIR.mkdir(parents=True, exist_ok=True)
    ENTITY_TEX_DIR.mkdir(parents=True, exist_ok=True)

    generate_soul_reaper_textures()
    generate_omen_hare_textures()
    generate_wisp_texture()
    generate_item_textures()

    reaper_egg = make_egg((24, 30, 44), (96, 214, 232), (218, 36, 68))
    reaper_egg.save(ITEM_TEX_DIR / "soul_reaper_spawn_egg.png")

    hare_egg = make_egg((238, 242, 248), (192, 206, 224), (214, 24, 52))
    hare_egg.save(ITEM_TEX_DIR / "omen_hare_spawn_egg.png")

    print("Generated and verified all Soul Reaper, Omen Hare, Wandering Soul Wisp, and drop item textures.")


if __name__ == "__main__":
    main()
