#!/usr/bin/env python3
"""
Themed boss-bar frame for the Witch of Ten Threads (`codenamecoraline:ten_threads_witch`).

Writes `assets/codenamecoraline/textures/gui/boss_bar/ten_threads_witch.png` (288x44): the rail is the beam of her loom,
dark wood with bone-and-brass trims; a spool of crimson thread wound on a bone core closes the left end and a long bone
needle with its eye closes the right end. A round patched-cloth emblem with cross-stitching sits over the middle, a taut
silver thread runs from spool to needle under the beam, and torn cloth patches hang below the ends.
The health slot is TRANSPARENT: the game draws the dark back and the fill behind this frame. The ten spool marks (one per
puppet) are drawn by the game above the slot, lit for the stages already reached.

Drawn at 8x with soft shapes, downsampled, snapped to a fixed palette and outlined (hand-painted pixel look).
Geometry constants must match `TenThreadsWitchBossBar` in `client/bossbar`.
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/ten_threads_witch.png"

W, H = 288, 44
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 46, 18, 196, 6
S = 8

P = {
    "outline": (22, 12, 16),
    "wood_l": (128, 90, 62), "wood": (88, 58, 42), "wood_d": (56, 36, 30), "wood_dd": (32, 20, 20),
    "bone_l": (244, 234, 212), "bone": (216, 202, 174), "bone_d": (160, 144, 120),
    "brass_l": (236, 196, 96), "brass": (196, 150, 58), "brass_d": (130, 94, 36),
    "thread_l": (255, 96, 106), "thread": (204, 32, 52), "thread_d": (124, 16, 34),
    "silver_l": (232, 238, 248), "silver": (172, 184, 208), "silver_d": (100, 112, 138),
    "patch": (108, 78, 122), "patch_d": (64, 42, 78), "patch_l": (150, 116, 164),
    "stitch": (242, 222, 172), "rag": (150, 60, 66), "rag_d": (92, 34, 44),
}
PAL = list(P.values())


def c(name):
    return P[name] + (255,)


class Canvas:
    def __init__(self):
        self.img = Image.new("RGBA", (W * S, H * S), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.img)

    def poly(self, pts, color):
        self.d.polygon([(x * S, y * S) for x, y in pts], fill=c(color))

    def rect(self, x0, y0, x1, y1, color):
        self.d.rectangle([x0 * S, y0 * S, x1 * S - 1, y1 * S - 1], fill=c(color))

    def ellipse(self, cx, cy, rx, ry, color):
        self.d.ellipse([(cx - rx) * S, (cy - ry) * S, (cx + rx) * S, (cy + ry) * S], fill=c(color))

    def line(self, pts, width, color):
        self.d.line([(x * S, y * S) for x, y in pts], fill=c(color), width=max(1, int(width * S)))


def beam(cv: Canvas):
    # the loom beam: dark wood with a lit top edge, brass bands at the ends of the slot
    cv.poly([(34, 13.5), (254, 13.5), (254, 28.5), (34, 28.5)], "wood_dd")
    cv.poly([(34, 14.4), (254, 14.4), (254, 27.6), (34, 27.6)], "wood_d")
    cv.poly([(34, 15.2), (254, 15.2), (254, 26.8), (34, 26.8)], "wood")
    cv.line([(35, 14.8), (253, 14.8)], 0.7, "wood_l")
    # wood grain
    for x in range(40, 250, 17):
        cv.line([(x, 16.2), (x + 6, 16.6)], 0.4, "wood_d")
        cv.line([(x + 7, 25.6), (x + 12, 25.2)], 0.4, "wood_d")
    # the recess around the slot
    cv.rect(SLOT_X - 2, SLOT_Y - 1.5, SLOT_X + SLOT_W + 2, SLOT_Y + SLOT_H + 1.5, "wood_dd")
    # brass bands
    for x in (SLOT_X - 6, SLOT_X + SLOT_W + 3):
        cv.rect(x, 13.5, x + 3, 28.5, "brass_d")
        cv.rect(x + 0.4, 14.2, x + 2.6, 27.8, "brass")
        cv.line([(x + 0.8, 14.6), (x + 0.8, 27)], 0.5, "brass_l")
    # taut silver thread running under the beam from the spool to the needle
    cv.line([(30, 30.5), (150, 32.5), (258, 30.5)], 0.7, "silver_d")
    cv.line([(30, 30.1), (150, 32.1), (258, 30.1)], 0.4, "silver_l")


def spool(cv: Canvas):
    # a spool on the left: bone core, two flanges, crimson thread wound between them
    cv.poly([(12, 8), (30, 8), (30, 34), (12, 34)], "bone_d")
    cv.rect(10, 6.5, 32, 10, "bone_d")
    cv.rect(10.6, 7.1, 31.4, 9.4, "bone")
    cv.rect(10, 32, 32, 35.5, "bone_d")
    cv.rect(10.6, 32.6, 31.4, 34.9, "bone")
    cv.rect(13, 10, 29, 32, "thread_d")
    cv.rect(13.6, 10, 28.4, 32, "thread")
    for y in range(11, 32, 3):
        cv.line([(13.6, y), (28.4, y + 1.2)], 0.6, "thread_d")
        cv.line([(13.6, y + 1.4), (28.4, y + 2.4)], 0.4, "thread_l")
    cv.line([(11.4, 7.7), (30, 7.7)], 0.5, "bone_l")
    cv.line([(11.4, 33.1), (30, 33.1)], 0.5, "bone_l")
    # loose thread trailing to the beam
    cv.line([(29, 21), (33, 21.6)], 0.8, "thread")


def needle(cv: Canvas):
    # a long bone needle on the right with a lit eye and a thread through it
    cv.poly([(250, 17), (276, 20.3), (284, 21.6), (276, 22.9), (250, 26)], "bone_d")
    cv.poly([(250, 17.8), (275, 20.7), (281.5, 21.6), (275, 22.5), (250, 25.2)], "bone")
    cv.line([(251, 18.6), (276, 21)], 0.5, "bone_l")
    cv.ellipse(256, 21.5, 2.2, 2.9, "bone_d")
    cv.ellipse(256, 21.5, 1.3, 2.0, "wood_dd")
    cv.line([(255, 21.5), (242, 29), (230, 31)], 0.6, "thread")
    cv.ellipse(256, 21.5, 0.5, 1.0, "thread_l")


def patches(cv: Canvas):
    # torn cloth patches hanging under the ends, clear of the name
    for x0, ln, col in ((50, 11, "rag"), (228, 11, "rag")):
        top = 27.6
        cv.poly([(x0, top), (x0 + 9, top), (x0 + 9, top + ln), (x0 + 6.5, top + ln - 3), (x0 + 4.5, top + ln + 0.5),
                 (x0 + 2.2, top + ln - 2.6), (x0, top + ln)], "rag_d")
        cv.poly([(x0 + 0.8, top + 0.6), (x0 + 8.2, top + 0.6), (x0 + 8.2, top + ln - 1), (x0 + 6.5, top + ln - 3.6),
                 (x0 + 4.5, top + ln - 0.6), (x0 + 2.2, top + ln - 3.4), (x0 + 0.8, top + ln - 1.2)], col)
        for k in range(3):
            cv.line([(x0 + 1.6 + k * 2.6, top + 1.8), (x0 + 2.6 + k * 2.6, top + 2.8)], 0.4, "stitch")


def emblem(cv: Canvas):
    cx, cy = W / 2, 12
    # a round patched-cloth emblem with cross-stitching, bordered in brass
    cv.ellipse(cx, cy, 11.5, 11.5, "brass_d")
    cv.ellipse(cx, cy, 10.5, 10.5, "brass")
    cv.ellipse(cx - 0.4, cy - 0.4, 9.2, 9.2, "patch_d")
    cv.ellipse(cx, cy, 8.8, 8.8, "patch")
    cv.ellipse(cx - 2, cy - 3, 4.2, 3.2, "patch_l")
    # stitched X and ring of stitches
    cv.line([(cx - 4.5, cy - 4.5), (cx + 4.5, cy + 4.5)], 0.8, "stitch")
    cv.line([(cx + 4.5, cy - 4.5), (cx - 4.5, cy + 4.5)], 0.8, "stitch")
    for k in range(12):
        import math
        a = k * math.pi / 6
        cv.line([(cx + math.cos(a) * 7.4, cy + math.sin(a) * 7.4), (cx + math.cos(a) * 8.4, cy + math.sin(a) * 8.4)], 0.5, "stitch")
    # a crimson button in the middle
    cv.ellipse(cx, cy, 2.2, 2.2, "thread_d")
    cv.ellipse(cx, cy, 1.6, 1.6, "thread")
    cv.ellipse(cx - 0.5, cy - 0.5, 0.6, 0.6, "thread_l")
    # two bone needles crossed behind
    cv.line([(cx - 16, cy + 8), (cx + 16, cy - 8)], 0.9, "bone_d")
    cv.line([(cx + 16, cy + 8), (cx - 16, cy - 8)], 0.9, "bone_d")


def snap(img: Image.Image) -> Image.Image:
    small = img.resize((W, H), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px, src = out.load(), small.load()
    for y in range(H):
        for x in range(W):
            r, g, b, a = src[x, y]
            if a < 120:
                continue
            best = min(PAL, key=lambda p: (p[0] - r) ** 2 + (p[1] - g) ** 2 + (p[2] - b) ** 2)
            px[x, y] = best + (255,)
    return out


def finish(body: Image.Image, face: Image.Image) -> Image.Image:
    out = snap(body)
    px = out.load()
    out.alpha_composite(snap(face))
    px = out.load()
    for y in range(SLOT_Y - 1, SLOT_Y + SLOT_H + 1):
        for x in range(SLOT_X - 1, SLOT_X + SLOT_W + 1):
            inside = SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H
            px[x, y] = (0, 0, 0, 0) if inside else P["outline"] + (255,)
    ring = []
    for y in range(H):
        for x in range(W):
            if px[x, y][3] == 0 and any(0 <= x + dx < W and 0 <= y + dy < H and px[x + dx, y + dy][3] == 255
                                        and not (SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H)
                                        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                ring.append((x, y))
    for x, y in ring:
        px[x, y] = P["outline"] + (255,)
    return out


def main():
    body = Canvas()
    patches(body)
    beam(body)
    spool(body)
    needle(body)
    face = Canvas()
    emblem(face)
    frame = finish(body.img, face.img)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    frame.save(OUT)
    sheet = Image.new("RGBA", (W, H * 2 + 4), (30, 28, 34, 255))
    for i, color in enumerate(((168, 186, 224), (255, 60, 90))):
        tile = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        t = tile.load()
        for y in range(SLOT_Y, SLOT_Y + SLOT_H):
            for x in range(SLOT_X, SLOT_X + int(SLOT_W * (1.0 - 0.3 * i))):
                t[x, y] = color + (255,)
        tile.alpha_composite(frame)
        sheet.alpha_composite(tile, (0, i * (H + 4)))
    review = ROOT / "build/ten-threads-bossbar/review.png"
    review.parent.mkdir(parents=True, exist_ok=True)
    sheet.resize((W * 4, (H * 2 + 4) * 4), Image.Resampling.NEAREST).save(review)
    print("Wrote", OUT)


if __name__ == "__main__":
    main()
