#!/usr/bin/env python3
"""
Single source of truth for the Bark Macaque's model boxes and textures.

Defines every cube of the model (part, offset, size, material), shelf-packs their UV footprints into a 64x64 atlas
(no overlapping footprints), paints it, and writes:

  * assets/codenamecoraline/textures/entity/bark_macaque.png         (64x64 colour)
  * assets/codenamecoraline/textures/entity/bark_macaque_eyes.png    (64x64 emissive eyes)
  * assets/codenamecoraline/textures/item/bark_macaque_spawn_egg.png (16x16)
  * src/main/java/com/codenamecoraline/client/macaque/BarkMacaqueParts.java (generated cube factory)

All box sizes are integers so the UV footprints match the model exactly.
Run:  python tools/generate_bark_macaque_assets.py
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
JAVA_OUT = ROOT / "src/main/java/com/codenamecoraline/client/macaque/BarkMacaqueParts.java"
ATLAS = 64

FUR = (140, 76, 52, 255)
FUR_D = (92, 48, 36, 255)
FUR_L = (176, 100, 66, 255)
BARK = (104, 72, 54, 255)
BARK_D = (66, 46, 36, 255)
BARK_L = (136, 98, 70, 255)
SKIN = (78, 54, 50, 255)
SKIN_D = (50, 34, 34, 255)
SKIN_L = (112, 82, 72, 255)
MOSS = (76, 104, 56, 255)
MOSS_D = (48, 72, 40, 255)
MOSS_L = (108, 140, 78, 255)
BONE = (214, 200, 166, 255)
EYE = (255, 196, 64, 255)
EYE_H = (255, 236, 150, 255)


def noise(x, y, seed=0):
    n = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)
    return (n & 0x7FFFFFFF) % 100


# (name, part, x, y, z, w, h, d, material); the model faces -z, y points down, the ground is y=24
BOXES = [
    ("body", "body", -3, -3, -5, 6, 6, 10, "fur"),
    ("bark_plate", "body", -3, -4, -4, 6, 1, 6, "bark"),
    ("moss_tuft", "body", -2, -5, 0, 3, 1, 3, "moss"),
    ("belly", "body", -2, 3, -4, 4, 1, 7, "skin"),
    ("head", "head", -3, -3, -5, 6, 6, 5, "head"),
    ("snout", "head", -2, 0, -7, 4, 3, 2, "snout"),
    ("ear_l", "head", 3, -3, -3, 2, 2, 1, "skin"),
    ("ear_r", "head", -5, -3, -3, 2, 2, 1, "skin"),
    ("root_crest", "head", -1, -5, -4, 2, 2, 3, "bark"),
    ("jaw", "jaw", -2, 0, -4, 4, 1, 4, "skin"),
    ("l_arm", "left_arm", -1, 0, -1, 2, 4, 2, "fur"),
    ("l_forearm", "left_forearm", -1, 0, -1, 2, 4, 2, "fur"),
    ("l_hand", "left_hand", -1, 0, -2, 2, 1, 3, "skin"),
    ("r_arm", "right_arm", -1, 0, -1, 2, 4, 2, "fur"),
    ("r_forearm", "right_forearm", -1, 0, -1, 2, 4, 2, "fur"),
    ("r_hand", "right_hand", -1, 0, -2, 2, 1, 3, "skin"),
    ("l_thigh", "left_thigh", -1, 0, -1, 2, 4, 2, "fur"),
    ("l_shin", "left_shin", -1, 0, -1, 2, 4, 2, "fur"),
    ("l_foot", "left_foot", -1, 0, -3, 2, 1, 4, "skin"),
    ("r_thigh", "right_thigh", -1, 0, -1, 2, 4, 2, "fur"),
    ("r_shin", "right_shin", -1, 0, -1, 2, 4, 2, "fur"),
    ("r_foot", "right_foot", -1, 0, -3, 2, 1, 4, "skin"),
    ("tail_a", "tail_a", -1, -1, 0, 2, 2, 5, "fur"),
    ("tail_b", "tail_b", -1, -1, 0, 2, 2, 5, "fur"),
    ("tail_c", "tail_c", -1, -1, 0, 2, 2, 4, "root_tail"),
]


def footprint(w, h, d):
    return 2 * (d + w), d + h


def pack():
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


def region_pixels(u, v, w, h, d):
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


def paint_box(img, eyes, box, u, v):
    name, part, bx, by, bz, w, h, d, mat = box
    px = img.load()
    seed = sum(ord(c) for c in name)
    for face, x, y, i, j, fw, fh in region_pixels(u, v, w, h, d):
        n = noise(x, y, seed)
        t = j / max(1, fh - 1)
        if mat in ("fur", "head"):
            tones = [FUR_L, FUR, FUR, FUR_D]
            idx = 0 if t < 0.2 else 1 if t < 0.5 else 2 if t < 0.8 else 3
            if n < 14:
                idx = min(3, idx + 1)
            elif n > 88:
                idx = max(0, idx - 1)
            if face == "top":
                idx = 0
            if face == "bottom":
                idx = 3
            c = tones[idx]
        elif mat == "bark":
            c = BARK_D if (n < 30 or (i + j) % 4 == 0) else BARK_L if n > 85 else BARK
        elif mat == "root_tail":
            c = BARK_D if n < 40 else BARK
        elif mat in ("skin", "snout"):
            c = SKIN_D if n < 25 else SKIN_L if n > 80 else SKIN
        elif mat == "moss":
            c = MOSS_D if n < 30 else MOSS_L if n > 70 else MOSS
        else:
            c = FUR
        px[x, y] = c

    def put(face_name, fi, fj, color, ecolor=None):
        for face, x, y, i, j, fw, fh in region_pixels(u, v, w, h, d):
            if face == face_name and i == fi and j == fj:
                px[x, y] = color
                if ecolor and eyes is not None:
                    eyes.load()[x, y] = ecolor
                return

    if name == "head":
        for i in range(w):
            put("front", i, 1, SKIN_D)
        for ex in (1, w - 3):
            put("front", ex, 2, EYE, EYE)
            put("front", ex + 1, 2, EYE_H, EYE_H)
    if name == "snout":
        put("front", 1, 1, SKIN_D)
        put("front", 2, 1, SKIN_D)
    if name == "jaw":
        for i in range(0, w, 2):
            put("front", i, 0, BONE)
    if name == "body":  # root scars and a moss streak
        for k in range(3):
            put("left", 3 + k, 1 + k, BARK_L)
            put("right", 4 + k, 1 + k, BARK_L)


def build_images(layout):
    img = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
    for box in BOXES:
        u, v = layout[box[0]]
        paint_box(img, eyes, box, u, v)
    return img, eyes


def write_java(layout):
    parts = {}
    for name, part, x, y, z, w, h, d, mat in BOXES:
        parts.setdefault(part, []).append((name, x, y, z, w, h, d))
    lines = [
        "package com.codenamecoraline.client.macaque;",
        "",
        "import net.minecraft.client.model.geom.builders.CubeListBuilder;",
        "",
        "/**",
        " * GENERATED by tools/generate_bark_macaque_assets.py: do not edit by hand.",
        " * Every cube of the Bark Macaque with its UV origin from the shelf-packed atlas (no overlapping footprints).",
        " */",
        "public final class BarkMacaqueParts {",
        f"    public static final int ATLAS = {ATLAS};",
        "",
        "    private BarkMacaqueParts() {}",
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


def icon_egg():
    """Spawn egg: bark-brown egg with moss spots, sparing the pixel outline."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    rows = {1: (6, 9), 2: (5, 10), 3: (4, 11), 4: (3, 12), 5: (3, 12), 6: (3, 12), 7: (3, 12), 8: (3, 12),
            9: (3, 12), 10: (3, 12), 11: (4, 11), 12: (4, 11), 13: (5, 10), 14: (6, 9)}
    for y, (x0, x1) in rows.items():
        for x in range(x0, x1 + 1):
            edge = x in (x0, x1) or y in (1, 14)
            px[x, y] = (40, 24, 20, 255) if edge else (FUR_L if (x + y) % 5 == 0 else FUR)
    for x, y in ((5, 4), (6, 4), (9, 6), (10, 6), (10, 7), (6, 9), (7, 9), (7, 10)):
        px[x, y] = MOSS_D
    for x, y in ((5, 4), (10, 6), (6, 9)):
        px[x, y] = MOSS_L
    for x, y in ((7, 6), (8, 6)):
        px[x, y] = EYE
    return im


def main():
    layout = pack()
    img, eyes = build_images(layout)
    (ASSETS / "textures/entity").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/entity/bark_macaque.png")
    eyes.save(ASSETS / "textures/entity/bark_macaque_eyes.png")
    icon_egg().save(ASSETS / "textures/item/bark_macaque_spawn_egg.png")
    write_java(layout)
    used = max(layout[b[0]][1] + footprint(b[5], b[6], b[7])[1] for b in BOXES)
    print(f"Bark Macaque assets written: {len(BOXES)} boxes, {used} atlas rows used of {ATLAS}")


if __name__ == "__main__":
    main()
