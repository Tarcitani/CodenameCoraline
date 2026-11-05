"""Generate the Fey Raven entity texture, emissive eyes/runes layer, and verify UV non-overlap (Pillow)."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"

# 5-tone intentional pixel-art palette from docs/design/fey_raven.md
SHADOW = (18, 19, 24, 255)        # #121318 deep crease & under-wing shadow
BASE = (30, 33, 43, 255)          # #1E212B obsidian raven plumage
MID_SLATE = (39, 44, 58, 255)     # #272C3A layered covert feather tone
HIGHLIGHT = (47, 54, 71, 255)     # #2F3647 indigo-slate sheen on upper edges
EDGE_CONTRAST = (12, 13, 17, 255) # #0C0D11 crisp feather barb edge
BEAK_BASE = (38, 41, 48, 255)     # #262930 horn beak base
BEAK_SHINE = (68, 74, 86, 255)    # #444A56 culmen ridge highlight
SILVER_RUNE = (216, 220, 230, 255)# #D8DCE6 silver-white etched runes
CYAN_GLOW = (124, 232, 245, 255)  # #7CE8F5 mystic cyan third-eye & rune glow
CYAN_CORE = (210, 252, 255, 255)  # #D2FCFF bright pupil core
CYAN_DIM = (58, 148, 168, 255)    # #3A94A8 soft outer rune aura
PARCHMENT = (216, 199, 166, 255)  # #D8C7A6 aged messenger scroll
PARCHMENT_DARK = (178, 158, 124, 255)
LEATHER = (138, 90, 50, 255)      # #8A5A32 leather binding strap

# Exact UV table matching FeyRavenModel.createBodyLayer() (u, v, w, h, d, name)
UV_BOXES = [
    (0, 0, 5, 7, 5, "body"),
    (20, 0, 5, 4, 4, "breast_ruff"),
    (38, 0, 4, 3, 4, "neck"),
    (0, 12, 4, 4, 5, "head"),
    (18, 12, 2, 2, 1, "third_eye_crest"),
    (32, 12, 2, 1, 4, "upper_beak"),
    (44, 12, 1, 1, 1, "upper_beak_tip"),
    (48, 12, 2, 1, 3, "lower_beak"),
    (48, 16, 2, 2, 2, "beak_berries"),
    (0, 21, 1, 4, 6, "left_wing_inner"),
    (14, 21, 1, 3, 5, "left_wing_outer"),
    (28, 21, 1, 4, 6, "right_wing_inner"),
    (42, 21, 1, 3, 5, "right_wing_outer"),
    (0, 32, 4, 1, 4, "tail_base"),
    (16, 32, 5, 1, 4, "tail_mid"),
    (34, 32, 6, 1, 4, "tail_tip"),
    (0, 38, 1, 3, 2, "left_thigh"),
    (6, 38, 1, 3, 1, "left_tarsus"),
    (10, 38, 2, 1, 3, "left_talons"),
    (20, 38, 1, 3, 2, "right_thigh"),
    (26, 38, 1, 3, 1, "right_tarsus"),
    (30, 38, 2, 1, 3, "right_talons"),
    (40, 38, 2, 2, 4, "messenger_scroll"),
]


def verify_uv_non_overlap(atlas_w: int = 64, atlas_h: int = 64) -> None:
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


def paint_feathered_box(img: Image.Image, u: int, v: int, w: int, h: int, d: int) -> None:
    rect_w = 2 * (d + w)
    rect_h = d + h
    for py in range(v, v + rect_h):
        rel_y = py - v
        for px in range(u, u + rect_w):
            rel_x = px - u
            # Top face highlight, bottom face shadow, layered feather barbs on sides
            if rel_y < d:
                tone = HIGHLIGHT if rel_x < d + w else SHADOW
            elif (rel_x + rel_y) % 4 == 0:
                tone = HIGHLIGHT
            elif rel_y == rect_h - 1 or rel_x in (0, d, d + w, rect_w - 1):
                tone = EDGE_CONTRAST
            elif (rel_x + rel_y * 2) % 3 == 0:
                tone = MID_SLATE
            else:
                tone = BASE
            img.putpixel((px, py), tone)


def main() -> None:
    verify_uv_non_overlap(64, 64)

    skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    for u, v, w, h, d, name in UV_BOXES:
        paint_feathered_box(skin, u, v, w, h, d)

    # 1. Breast ruff (20, 0, 5, 4, 4): V-shaped silver & cyan fey runes on front face
    # Front face of (5,4,4) at (20,0) is [24..29, 4..8]
    for rx, ry in [(24, 5), (25, 6), (26, 7), (27, 6), (28, 5), (26, 5)]:
        skin.putpixel((rx, ry), SILVER_RUNE)
        eyes.putpixel((rx, ry), CYAN_DIM)
    eyes.putpixel((26, 6), CYAN_GLOW)

    # 2. Head (0, 12, 4, 4, 5): front face [5..9, 17..21], right [0..5, 17..21], left [9..14, 17..21]
    # Lateral eyes on right face (x=2, y=19) and left face (x=11, y=19)
    for ex, ey in [(2, 19), (11, 19)]:
        skin.putpixel((ex, ey), CYAN_GLOW)
        eyes.putpixel((ex, ey), CYAN_GLOW)
    for ex, ey in [(1, 19), (12, 19)]:
        skin.putpixel((ex, ey), SILVER_RUNE)

    # Vertical third-eye gem centered on third_eye_crest (18, 12, 2, 2, 1) front face [19..21, 13..15]
    skin.putpixel((19, 13), CYAN_CORE)
    skin.putpixel((20, 14), CYAN_GLOW)
    skin.putpixel((19, 14), SILVER_RUNE)
    skin.putpixel((20, 13), SILVER_RUNE)
    eyes.putpixel((19, 13), CYAN_CORE)
    eyes.putpixel((20, 14), CYAN_GLOW)

    # 3. Beaks (upper 32,12; tip 44,12; lower 48,12) and glow berries (48,16)
    for u, v, w, h, d in [(32, 12, 2, 1, 4), (44, 12, 1, 1, 1), (48, 12, 2, 1, 3)]:
        for py in range(v, v + d + h):
            for px in range(u, u + 2 * (d + w)):
                skin.putpixel((px, py), BEAK_SHINE if py == v else BEAK_BASE)
    for py in range(16, 20):
        for px in range(48, 56):
            is_berry = (px + py) % 2 == 0
            skin.putpixel((px, py), CYAN_GLOW if is_berry else (46, 94, 62, 255))
            if is_berry:
                eyes.putpixel((px, py), CYAN_GLOW)

    # 4. Wings (0,21), (14,21), (28,21), (42,21): silver covert arcs & glowing rune sigils
    for base_u in (0, 28):
        for step in range(5):
            rx = base_u + 1 + step
            ry = 27 + (step % 3)
            skin.putpixel((rx, ry), SILVER_RUNE)
            eyes.putpixel((rx, ry), CYAN_DIM if step % 2 == 0 else CYAN_GLOW)
            skin.putpixel((rx + 7, ry), SILVER_RUNE)
            eyes.putpixel((rx + 7, ry), CYAN_DIM)

    # 5. Legs & talons: dark horny scales
    for u, v, w, h, d in [
        (0, 38, 1, 3, 2), (6, 38, 1, 3, 1), (10, 38, 2, 1, 3),
        (20, 38, 1, 3, 2), (26, 38, 1, 3, 1), (30, 38, 2, 1, 3),
    ]:
        for py in range(v, v + d + h):
            for px in range(u, u + 2 * (d + w)):
                skin.putpixel((px, py), SHADOW if (px + py) % 2 == 0 else BEAK_BASE)

    # 6. Messenger scroll (40, 38, 2, 2, 4): parchment roll with leather tie & cyan wax seal
    for py in range(38, 44):
        for px in range(40, 52):
            if px in (45, 46) or py == 41:
                skin.putpixel((px, py), LEATHER)
            elif (px + py) % 3 == 0:
                skin.putpixel((px, py), PARCHMENT_DARK)
            else:
                skin.putpixel((px, py), PARCHMENT)
    skin.putpixel((45, 42), CYAN_GLOW)
    eyes.putpixel((45, 42), CYAN_GLOW)

    out_dir = ASSETS / "textures/entity"
    out_dir.mkdir(parents=True, exist_ok=True)
    skin.save(out_dir / "fey_raven.png")
    eyes.save(out_dir / "fey_raven_eyes.png")
    print(f"Generated {out_dir / 'fey_raven.png'} and {out_dir / 'fey_raven_eyes.png'}")


if __name__ == "__main__":
    main()
