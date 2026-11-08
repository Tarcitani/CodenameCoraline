"""Generate the Brimstone Salamander entity textures (3 variants: Brimstone, Soulflame, Crimson),
emissive layers, and verify UV non-overlap (Pillow)."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"

BRIMSTONE_PALETTE = {
    "edge_charcoal": (18, 19, 23, 255),      # #121317 deep basalt crevice
    "shadow_basalt": (28, 29, 33, 255),      # #1C1D21 dark basalt belly & lower faces
    "base_slate": (44, 46, 54, 255),         # #2C2E36 main volcanic rock hide
    "mid_slate": (56, 58, 66, 255),          # #383A42 carapace scutes
    "highlight_ash": (76, 79, 90, 255),      # #4C4F5A top ridge highlight
    "accent_base": (227, 196, 52, 255),      # #E3C434 alchemy brimstone spot base
    "accent_bright": (242, 224, 107, 255),   # #F2E06B bright sulphur crystal fleck
    "glow_dark": (216, 67, 21, 255),         # #D84315 deep ember fissure
    "glow_mid": (255, 87, 34, 255),          # #FF5722 glowing lava vein
    "glow_core": (255, 167, 38, 255),        # #FFA726 white-hot bellows core
}

SOULFLAME_PALETTE = {
    "edge_charcoal": (14, 18, 24, 255),      # #0E1218 deep soul-soil basalt
    "shadow_basalt": (22, 30, 38, 255),      # #161E26 dark soul-shale belly
    "base_slate": (36, 46, 56, 255),         # #242E38 cold slate hide
    "mid_slate": (48, 62, 74, 255),          # #303E4A blue scute plates
    "highlight_ash": (150, 168, 178, 255),   # #96A8B2 calcined soul-bone scute ridge
    "accent_base": (0, 180, 216, 255),       # #00B4D8 spectral cyan crystal spot
    "accent_bright": (144, 224, 239, 255),   # #90E0EF bright soul spark
    "glow_dark": (2, 62, 138, 255),          # #023E8A deep abyssal soul fissure
    "glow_mid": (0, 229, 255, 255),          # #00E5FF soul fire turquoise
    "glow_core": (224, 247, 250, 255),       # #E0F7FA white-hot soul core
}

CRIMSON_PALETTE = {
    "edge_charcoal": (24, 12, 16, 255),      # #180C10 deep crimson crevice
    "shadow_basalt": (42, 18, 26, 255),      # #2A121A vulcanized netherrack belly
    "base_slate": (62, 26, 36, 255),         # #3E1A24 dark crimson rock hide
    "mid_slate": (82, 36, 48, 255),          # #522430 crimson scutes
    "highlight_ash": (138, 38, 60, 255),     # #8A263C fungal spore scute crest
    "accent_base": (216, 27, 96, 255),       # #D81B60 crimson spore fleck
    "accent_bright": (255, 64, 129, 255),    # #FF4081 vibrant spore bloom
    "glow_dark": (183, 28, 28, 255),         # #B71C1C deep blood ember fissure
    "glow_mid": (255, 23, 68, 255),          # #FF1744 glowing ruby vein
    "glow_core": (255, 138, 128, 255),       # #FF8A80 incandescent ruby core
}

# Exact 22-box UV table matching BrimstoneSalamanderModel.createBodyLayer() (u, v, w, h, d, name)
UV_BOXES = [
    (0, 0, 10, 6, 14, "body"),
    (50, 0, 10, 4, 12, "body_carapace"),
    (96, 0, 6, 3, 6, "throat_sac"),
    (0, 22, 2, 3, 3, "dorsal_plate_1"),
    (12, 22, 2, 5, 4, "dorsal_plate_2"),
    (26, 22, 3, 4, 4, "dorsal_plate_3"),
    (42, 22, 2, 4, 3, "dorsal_plate_4"),
    (54, 22, 2, 3, 3, "dorsal_plate_5"),
    (66, 22, 7, 5, 5, "neck"),
    (0, 34, 9, 4, 6, "head_cranium"),
    (32, 34, 2, 2, 4, "head_brow_left"),
    (46, 34, 2, 1, 3, "head_brow_right"),
    (58, 34, 7, 2, 5, "upper_jaw"),
    (84, 34, 7, 2, 5, "lower_jaw_bone"),
    (110, 34, 3, 1, 4, "forked_tongue"),
    (0, 46, 5, 3, 3, "left_front_leg"),
    (18, 46, 4, 4, 4, "left_front_paw"),
    (36, 46, 5, 3, 3, "right_front_leg"),
    (54, 46, 4, 4, 4, "right_front_paw"),
    (72, 46, 5, 3, 4, "left_hind_leg"),
    (92, 46, 4, 4, 5, "left_hind_paw"),
    (0, 58, 5, 3, 4, "right_hind_leg"),
    (20, 58, 4, 4, 5, "right_hind_paw"),
    (40, 58, 6, 5, 7, "tail_base"),
    (68, 58, 4, 6, 8, "tail_mid"),
    (94, 58, 2, 7, 8, "tail_tip"),
]


def verify_uv_non_overlap(atlas_w: int = 128, atlas_h: int = 128) -> None:
    ownership: dict[tuple[int, int], str] = {}
    overlaps: list[str] = []
    for u, v, w, h, d, name in UV_BOXES:
        rect_w = 2 * (d + w)
        rect_h = d + h
        if u + rect_w > atlas_w or v + rect_h > atlas_h:
            raise AssertionError(f"Box {name} exceeds {atlas_w}x{atlas_h} atlas: ({u},{v})+({rect_w},{rect_h})")
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                prev = ownership.get((px, py))
                if prev is not None and prev != name:
                    overlaps.append(f"Pixel ({px},{py}) shared by {prev} and {name}")
                ownership[(px, py)] = name
    if overlaps:
        raise AssertionError("UV overlap detected:\n" + "\n".join(overlaps[:10]))
    print(f"[UV CHECK PASSED] {len(UV_BOXES)} boxes verified on {atlas_w}x{atlas_h} atlas with 0 overlaps.")


def paint_basalt_box(skin: Image.Image, eyes: Image.Image, u: int, v: int, w: int, h: int, d: int, name: str, palette: dict, is_baby: bool = False) -> None:
    rect_w = 2 * (d + w)
    rect_h = d + h
    for py in range(v, v + rect_h):
        rel_y = py - v
        for px in range(u, u + rect_w):
            rel_x = px - u
            if rel_y < d:
                tone = palette["highlight_ash"] if rel_x < d + w else palette["shadow_basalt"]
            elif rel_y == rect_h - 1 or rel_x in (0, d, d + w, rect_w - 1):
                tone = palette["edge_charcoal"]
            elif is_baby:
                # Smoother juvenile texture with fewer jagged scutes
                if (rel_x + rel_y) % 6 == 0:
                    tone = palette["mid_slate"]
                else:
                    tone = palette["base_slate"]
            elif (rel_x + rel_y) % 5 == 0:
                tone = palette["highlight_ash"]
            elif (rel_x * 2 + rel_y) % 3 == 0:
                tone = palette["mid_slate"]
            else:
                tone = palette["base_slate"]
            skin.putpixel((px, py), tone)

    # Specialized details per anatomical group
    if name == "throat_sac":
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                dist = abs((px - u) - rect_w // 2) + abs((py - v) - rect_h // 2)
                if is_baby:
                    # Smaller, tender bellows with gentle warm glow
                    c = palette["glow_mid"] if dist < 2 else (palette["accent_base"] if dist < 5 else palette["shadow_basalt"])
                    skin.putpixel((px, py), c)
                    if dist < 2:
                        eyes.putpixel((px, py), c)
                else:
                    c = palette["glow_core"] if dist < 4 else (palette["glow_mid"] if dist < 8 else palette["accent_base"])
                    skin.putpixel((px, py), c)
                    eyes.putpixel((px, py), c)
    elif name == "forked_tongue":
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                c = palette["accent_bright"] if (px + py) % 2 == 0 else palette["glow_mid"]
                skin.putpixel((px, py), c)
                eyes.putpixel((px, py), c)
    elif name.startswith("dorsal_plate_"):
        for py in range(v + d, v + rect_h):
            for px in range(u, u + rect_w):
                if is_baby:
                    # Softer juvenile dorsal scutes
                    if py == v + d:
                        skin.putpixel((px, py), palette["accent_base"])
                    elif (px + py) % 4 == 0:
                        skin.putpixel((px, py), palette["glow_dark"])
                else:
                    if py == v + d:
                        skin.putpixel((px, py), palette["accent_bright"])
                        eyes.putpixel((px, py), palette["accent_base"])
                    elif (px + py) % 3 == 0:
                        skin.putpixel((px, py), palette["glow_mid"])
                        eyes.putpixel((px, py), palette["glow_dark"])
    elif name in ("body", "body_carapace", "neck", "tail_base", "tail_mid", "tail_tip"):
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                if is_baby:
                    # Fine, subtle juvenile fissure lines
                    pattern = (px * 11 + py * 7) % 37
                    if pattern == 0:
                        skin.putpixel((px, py), palette["accent_base"])
                        eyes.putpixel((px, py), palette["accent_base"])
                    elif pattern == 18:
                        skin.putpixel((px, py), palette["glow_dark"])
                else:
                    pattern = (px * 7 + py * 13) % 29
                    if pattern in (0, 1):
                        skin.putpixel((px, py), palette["accent_base"])
                        if pattern == 0:
                            eyes.putpixel((px, py), palette["accent_bright"])
                    elif pattern == 14:
                        skin.putpixel((px, py), palette["glow_mid"])
                        eyes.putpixel((px, py), palette["glow_mid"])


def generate_variant(palette: dict, out_skin_path: Path, out_eyes_path: Path) -> None:
    skin = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (128, 128), (0, 0, 0, 0))

    for u, v, w, h, d, name in UV_BOXES:
        paint_basalt_box(skin, eyes, u, v, w, h, d, name, palette, is_baby=False)

    # Lateral glowing eyes on head_cranium (0, 34, 9, 4, 6)
    for ex, ey in [(2, 41), (3, 41), (17, 41), (18, 41), (7, 41), (13, 41)]:
        skin.putpixel((ex, ey), palette["accent_bright"])
        eyes.putpixel((ex, ey), palette["accent_bright"])
    for ex, ey in [(2, 42), (18, 42), (8, 41), (12, 41)]:
        skin.putpixel((ex, ey), palette["glow_mid"])
        eyes.putpixel((ex, ey), palette["glow_mid"])

    # Glowing nostrils on upper_jaw (58, 34, 7, 2, 5) front face [63..70, 39..41]
    for nx, ny in [(64, 39), (68, 39)]:
        skin.putpixel((nx, ny), palette["glow_core"])
        eyes.putpixel((nx, ny), palette["glow_core"])

    out_skin_path.parent.mkdir(parents=True, exist_ok=True)
    skin.save(out_skin_path)
    eyes.save(out_eyes_path)
    print(f"Generated {out_skin_path.name} and {out_eyes_path.name}")


def generate_baby_variant(palette: dict, out_skin_path: Path, out_eyes_path: Path) -> None:
    skin = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (128, 128), (0, 0, 0, 0))

    for u, v, w, h, d, name in UV_BOXES:
        paint_basalt_box(skin, eyes, u, v, w, h, d, name, palette, is_baby=True)

    # Expressive, rounder lateral eyes on head_cranium (0, 34, 9, 4, 6)
    # Left eye cluster (px 2..4, py 41..42)
    for ex, ey in [(2, 41), (2, 42), (4, 41), (4, 42)]:
        skin.putpixel((ex, ey), palette["glow_mid"])
        eyes.putpixel((ex, ey), palette["glow_mid"])
    for ex, ey in [(3, 41), (3, 42)]:
        skin.putpixel((ex, ey), palette["glow_core"])
        eyes.putpixel((ex, ey), palette["accent_bright"])

    # Right eye cluster (px 16..18, py 41..42)
    for ex, ey in [(16, 41), (16, 42), (18, 41), (18, 42)]:
        skin.putpixel((ex, ey), palette["glow_mid"])
        eyes.putpixel((ex, ey), palette["glow_mid"])
    for ex, ey in [(17, 41), (17, 42)]:
        skin.putpixel((ex, ey), palette["glow_core"])
        eyes.putpixel((ex, ey), palette["accent_bright"])

    # Forward curiosity sparkles (px 7, 8, 12, 13, py 41)
    for ex, ey in [(7, 41), (13, 41)]:
        skin.putpixel((ex, ey), palette["accent_bright"])
        eyes.putpixel((ex, ey), palette["accent_bright"])

    # Subtle glowing nostrils on upper_jaw (58, 34, 7, 2, 5)
    for nx, ny in [(64, 39), (68, 39)]:
        skin.putpixel((nx, ny), palette["glow_mid"])
        eyes.putpixel((nx, ny), palette["glow_mid"])

    out_skin_path.parent.mkdir(parents=True, exist_ok=True)
    skin.save(out_skin_path)
    eyes.save(out_eyes_path)
    print(f"Generated {out_skin_path.name} and {out_eyes_path.name}")


def main() -> None:
    verify_uv_non_overlap(128, 128)

    entity_dir = ASSETS / "textures/entity"
    generate_variant(BRIMSTONE_PALETTE, entity_dir / "brimstone_salamander.png", entity_dir / "brimstone_salamander_eyes.png")
    generate_variant(SOULFLAME_PALETTE, entity_dir / "brimstone_salamander_soulflame.png", entity_dir / "brimstone_salamander_soulflame_eyes.png")
    generate_variant(CRIMSON_PALETTE, entity_dir / "brimstone_salamander_crimson.png", entity_dir / "brimstone_salamander_crimson_eyes.png")

    generate_baby_variant(BRIMSTONE_PALETTE, entity_dir / "brimstone_salamander_baby.png", entity_dir / "brimstone_salamander_baby_eyes.png")
    generate_baby_variant(SOULFLAME_PALETTE, entity_dir / "brimstone_salamander_soulflame_baby.png", entity_dir / "brimstone_salamander_soulflame_baby_eyes.png")
    generate_baby_variant(CRIMSON_PALETTE, entity_dir / "brimstone_salamander_crimson_baby.png", entity_dir / "brimstone_salamander_crimson_baby_eyes.png")

    item_dir = ASSETS / "textures/item"
    item_dir.mkdir(parents=True, exist_ok=True)
    spit = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(3, 13):
        for x in range(3, 13):
            dist = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if dist <= 4.6:
                if dist < 1.8:
                    spit.putpixel((x, y), BRIMSTONE_PALETTE["accent_bright"])
                elif dist < 2.8:
                    spit.putpixel((x, y), BRIMSTONE_PALETTE["glow_core"] if (x + y) % 2 == 0 else BRIMSTONE_PALETTE["accent_base"])
                elif dist < 3.8:
                    spit.putpixel((x, y), BRIMSTONE_PALETTE["glow_mid"] if (x * y) % 3 == 0 else BRIMSTONE_PALETTE["shadow_basalt"])
                else:
                    spit.putpixel((x, y), BRIMSTONE_PALETTE["edge_charcoal"])
    spit.save(item_dir / "brimstone_spit.png")

    scale = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(2, 15):
        half_w = 4.5 * (1.0 - (y - 2) / 14.0) if y > 7 else 1.5 + (y - 2) * 0.6
        min_x = int(7.5 - half_w)
        max_x = int(7.5 + half_w)
        for x in range(min_x, max_x + 1):
            if x in (min_x, max_x) or y in (2, 14):
                scale.putpixel((x, y), BRIMSTONE_PALETTE["edge_charcoal"])
            elif (x + y) % 4 == 0:
                scale.putpixel((x, y), BRIMSTONE_PALETTE["accent_bright"])
            elif (x * 2 + y) % 5 == 0:
                scale.putpixel((x, y), BRIMSTONE_PALETTE["glow_mid"])
            elif (x + y * 2) % 3 == 0:
                scale.putpixel((x, y), BRIMSTONE_PALETTE["highlight_ash"])
            else:
                scale.putpixel((x, y), BRIMSTONE_PALETTE["base_slate"])
    scale.save(item_dir / "brimstone_scale.png")
    print(f"Generated item textures brimstone_spit.png and brimstone_scale.png")


if __name__ == "__main__":
    main()
