"""Generate the Brimstone Salamander entity texture, emissive magma/sulphur layer, and verify UV non-overlap (Pillow)."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"

# 5-tone volcanic basalt & fey alchemy sulphur-magma palette from docs/design/brimstone_salamander.md
EDGE_CHARCOAL = (18, 19, 23, 255)      # #121317 deep basalt crevice / scale outline
SHADOW_BASALT = (28, 29, 33, 255)      # #1C1D21 dark basalt belly & lower faces
BASE_SLATE = (44, 46, 54, 255)         # #2C2E36 main volcanic rock hide
MID_SLATE = (56, 58, 66, 255)          # #383A42 carapace scutes
HIGHLIGHT_ASH = (76, 79, 90, 255)      # #4C4F5A top ridge highlight

SULPHUR_GOLD = (227, 196, 52, 255)     # #E3C434 alchemy brimstone spot base
SULPHUR_BRIGHT = (242, 224, 107, 255)  # #F2E06B bright sulphur crystal fleck
MAGMA_RED = (216, 67, 21, 255)         # #D84315 deep ember fissure
MAGMA_ORANGE = (255, 87, 34, 255)      # #FF5722 glowing lava vein
MAGMA_CORE = (255, 167, 38, 255)       # #FFA726 white-hot bellows core

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


def paint_basalt_box(skin: Image.Image, eyes: Image.Image, u: int, v: int, w: int, h: int, d: int, name: str) -> None:
    rect_w = 2 * (d + w)
    rect_h = d + h
    for py in range(v, v + rect_h):
        rel_y = py - v
        for px in range(u, u + rect_w):
            rel_x = px - u
            if rel_y < d:
                tone = HIGHLIGHT_ASH if rel_x < d + w else SHADOW_BASALT
            elif rel_y == rect_h - 1 or rel_x in (0, d, d + w, rect_w - 1):
                tone = EDGE_CHARCOAL
            elif (rel_x + rel_y) % 5 == 0:
                tone = HIGHLIGHT_ASH
            elif (rel_x * 2 + rel_y) % 3 == 0:
                tone = MID_SLATE
            else:
                tone = BASE_SLATE
            skin.putpixel((px, py), tone)

    # Specialized details per anatomical group
    if name == "throat_sac":
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                dist = abs((px - u) - rect_w // 2) + abs((py - v) - rect_h // 2)
                c = MAGMA_CORE if dist < 4 else (MAGMA_ORANGE if dist < 8 else SULPHUR_GOLD)
                skin.putpixel((px, py), c)
                eyes.putpixel((px, py), c)
    elif name == "forked_tongue":
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                c = SULPHUR_BRIGHT if (px + py) % 2 == 0 else MAGMA_ORANGE
                skin.putpixel((px, py), c)
                eyes.putpixel((px, py), c)
    elif name.startswith("dorsal_plate_"):
        for py in range(v + d, v + rect_h):
            for px in range(u, u + rect_w):
                if py == v + d:
                    skin.putpixel((px, py), SULPHUR_BRIGHT)
                    eyes.putpixel((px, py), SULPHUR_GOLD)
                elif (px + py) % 3 == 0:
                    skin.putpixel((px, py), MAGMA_ORANGE)
                    eyes.putpixel((px, py), MAGMA_RED)
    elif name in ("body", "body_carapace", "neck", "tail_base", "tail_mid", "tail_tip"):
        for py in range(v, v + rect_h):
            for px in range(u, u + rect_w):
                # Classic salamander sulphur blotches & glowing magma cracks
                pattern = (px * 7 + py * 13) % 29
                if pattern in (0, 1):
                    skin.putpixel((px, py), SULPHUR_GOLD)
                    if pattern == 0:
                        eyes.putpixel((px, py), SULPHUR_BRIGHT)
                elif pattern == 14:
                    skin.putpixel((px, py), MAGMA_ORANGE)
                    eyes.putpixel((px, py), MAGMA_ORANGE)


def main() -> None:
    verify_uv_non_overlap(128, 128)

    skin = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (128, 128), (0, 0, 0, 0))

    for u, v, w, h, d, name in UV_BOXES:
        paint_basalt_box(skin, eyes, u, v, w, h, d, name)

    # Lateral glowing sulphur-amber eyes on head_cranium (0, 34, 9, 4, 6)
    # Right side face: x in [0..6], y in [40..44]; Left side face: x in [15..21], y in [40..44]; Front face: x in [6..15], y in [40..44]
    for ex, ey in [(2, 41), (3, 41), (17, 41), (18, 41), (7, 41), (13, 41)]:
        skin.putpixel((ex, ey), SULPHUR_BRIGHT)
        eyes.putpixel((ex, ey), SULPHUR_BRIGHT)
    for ex, ey in [(2, 42), (18, 42), (8, 41), (12, 41)]:
        skin.putpixel((ex, ey), MAGMA_ORANGE)
        eyes.putpixel((ex, ey), MAGMA_ORANGE)

    # Glowing nostrils on upper_jaw (58, 34, 7, 2, 5) front face [63..70, 39..41]
    for nx, ny in [(64, 39), (68, 39)]:
        skin.putpixel((nx, ny), MAGMA_CORE)
        eyes.putpixel((nx, ny), MAGMA_CORE)

    out_dir = ASSETS / "textures/entity"
    out_dir.mkdir(parents=True, exist_ok=True)
    skin.save(out_dir / "brimstone_salamander.png")
    eyes.save(out_dir / "brimstone_salamander_eyes.png")

    item_dir = ASSETS / "textures/item"
    item_dir.mkdir(parents=True, exist_ok=True)
    spit = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(3, 13):
        for x in range(3, 13):
            dist = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if dist <= 4.6:
                if dist < 1.8:
                    spit.putpixel((x, y), SULPHUR_BRIGHT)
                elif dist < 2.8:
                    spit.putpixel((x, y), MAGMA_CORE if (x + y) % 2 == 0 else SULPHUR_GOLD)
                elif dist < 3.8:
                    spit.putpixel((x, y), MAGMA_ORANGE if (x * y) % 3 == 0 else SHADOW_BASALT)
                else:
                    spit.putpixel((x, y), EDGE_CHARCOAL)
    spit.save(item_dir / "brimstone_spit.png")
    print(f"Generated {out_dir / 'brimstone_salamander.png'}, {out_dir / 'brimstone_salamander_eyes.png'}, and {item_dir / 'brimstone_spit.png'}")


if __name__ == "__main__":
    main()

