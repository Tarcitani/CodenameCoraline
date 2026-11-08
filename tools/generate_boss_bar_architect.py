#!/usr/bin/env python3
"""
Themed boss-bar frame for the Architect (`codenamecoraline:architect`), spec docs/design/architect.md §19.

Writes `assets/codenamecoraline/textures/gui/boss_bar/architect.png` (288x44): the rail is a carved stone lintel shaped
like the Tablet of the Commandments, blue-grey stone with chipped corners and fictitious glyphs; a gargoyle wing of stone
plates closes each end and the hooded head sits over the middle (black hood cavity, two coral pupils and the lilac
diamond seal on the brow). Lilac notches above the rail mark where phase two (70%) and phase three (35%) begin. The
health slot is TRANSPARENT: the game draws the dark back and the fill (lilac, cyan or coral by phase) behind the frame.

Drawn at 8x with flat shapes, downsampled, snapped to the spec palette and outlined.
Geometry constants must match `ArchitectBossBar` in `client/bossbar`.
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/architect.png"

W, H = 288, 44
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 46, 18, 196, 6
MARK_70 = SLOT_X + round(SLOT_W * 0.70)
MARK_35 = SLOT_X + round(SLOT_W * 0.35)
S = 8

P = {
    "outline": (11, 11, 16),
    "stone_l": (155, 163, 176), "stone": (110, 117, 130), "stone_d": (67, 72, 82),
    "granite_l": (170, 164, 160), "granite": (119, 116, 122), "granite_d": (73, 71, 79),
    "crack": (36, 35, 43), "void": (11, 11, 16),
    "coral": (255, 96, 75), "coral_hot": (255, 208, 154),
    "lilac": (216, 200, 255), "lilac_d": (126, 100, 196),
    "mana": (56, 189, 186), "basalt": (40, 40, 49),
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


def draw(cv):
    # ---- the lintel (tablet laid flat): stone rail around the slot, chipped corners
    cv.poly([(38, 14), (250, 14), (252, 16), (252, 26), (250, 28), (38, 28), (36, 26), (36, 16)], "stone_d")
    cv.rect(38, 15, 250, 17, "stone_l")
    cv.rect(38, 25, 250, 27, "stone")
    for x in range(52, 238, 9):  # fictitious glyphs carved along the top and bottom bands
        cv.rect(x, 15.5, x + 3, 16.5, "granite_d")
        cv.rect(x + 4, 25.5, x + 6, 26.5, "granite_d")
    for x in (80, 150, 210):  # cracks of the old stone
        cv.poly([(x, 14), (x + 1, 14), (x + 2, 17), (x + 1, 17)], "crack")
    # ---- a stone gargoyle wing at each end: a bone along the top and plate feathers hanging from it
    for side in (-1, 1):
        x0 = 36 if side < 0 else 252
        bone = [(x0, 15), (x0 + 34 * side, 4), (x0 + 36 * side, 6), (x0 + 3 * side, 19)]
        cv.poly(bone, "granite")
        for k in range(5):
            bx = x0 + (6 + k * 6) * side
            by = 13 - k * 1.8
            cv.poly([(bx, by), (bx + 5 * side, by - 1), (bx + 4 * side, by + 14 - k), (bx + 1 * side, by + 12 - k)],
                    "granite_d" if k % 2 else "granite_l")
            cv.poly([(bx + 1 * side, by + 12 - k), (bx + 4 * side, by + 14 - k), (bx + 2.5 * side, by + 17 - k)], "basalt")
    # ---- lilac notches above the rail: where phases two (70%) and three (35%) begin
    for mx in (MARK_70, MARK_35):
        cv.poly([(mx - 2, 11), (mx + 3, 11), (mx + 0.5, 15)], "lilac_d")
        cv.rect(mx, 12, mx + 1, 14, "lilac")
    # ---- the hooded head over the middle
    cx = W / 2
    cv.poly([(cx - 9, 30), (cx - 10, 10), (cx - 5, 1), (cx + 5, 1), (cx + 10, 10), (cx + 9, 30)], "stone_d")  # hood
    cv.poly([(cx - 8, 29), (cx - 9, 11), (cx - 4, 3), (cx + 4, 3), (cx + 9, 11), (cx + 8, 29)], "stone")
    cv.poly([(cx - 6, 28), (cx - 6, 10), (cx - 3, 6), (cx + 3, 6), (cx + 6, 10), (cx + 6, 28)], "void")    # cavity
    cv.poly([(cx - 4.5, 27), (cx - 4.5, 12), (cx + 4.5, 12), (cx + 4.5, 27)], "stone_d")                    # face
    cv.rect(cx - 4.5, 12, cx + 4.5, 13, "void")
    cv.poly([(cx - 0.6, 8), (cx + 0.6, 8), (cx + 1.4, 10), (cx + 0.6, 12), (cx - 0.6, 12), (cx - 1.4, 10)], "lilac")  # seal
    cv.rect(cx - 4, 15, cx - 1, 16.2, "void")   # eyes
    cv.rect(cx + 1, 15, cx + 4, 16.2, "void")
    cv.rect(cx - 2.8, 15, cx - 1.8, 16.2, "coral")
    cv.rect(cx + 1.8, 15, cx + 2.8, 16.2, "coral")
    cv.rect(cx - 0.5, 17, cx + 0.5, 20, "stone_l")  # nose
    cv.rect(cx - 4, 22, cx + 4, 25, "granite_l")    # the wide flat-toothed smile
    for tx in range(-3, 4, 2):
        cv.rect(cx + tx, 22, cx + tx + 0.5, 25, "crack")
    cv.rect(cx - 4, 23.3, cx + 4, 23.8, "crack")
    cv.poly([(cx - 4.5, 21), (cx - 3, 22), (cx - 4.5, 23)], "crack")  # smile corners and cheek slits
    cv.poly([(cx + 4.5, 21), (cx + 3, 22), (cx + 4.5, 23)], "crack")
    cv.poly([(cx - 4.5, 17), (cx - 3.5, 17), (cx - 4.5, 19)], "crack")
    cv.poly([(cx + 4.5, 17), (cx + 3.5, 17), (cx + 4.5, 19)], "crack")
    # ---- the health slot stays transparent (the fill is drawn behind)
    cv.d.rectangle([SLOT_X * S, SLOT_Y * S, (SLOT_X + SLOT_W) * S - 1, (SLOT_Y + SLOT_H) * S - 1], fill=(0, 0, 0, 0))
    cv.d.rectangle([(cx - 6) * S, SLOT_Y * S, (cx + 6) * S - 1, (SLOT_Y + SLOT_H) * S - 1], fill=c("stone_d"))


def snap(img):
    small = img.resize((W, H), Image.Resampling.BOX)
    px = small.load()
    for y in range(H):
        for x in range(W):
            r, g, b, a = px[x, y]
            if a < 110:
                px[x, y] = (0, 0, 0, 0)
                continue
            best = min(PAL, key=lambda q: (q[0] - r) ** 2 + (q[1] - g) ** 2 + (q[2] - b) ** 2)
            px[x, y] = best + (255,)
    out = small.copy()
    o = out.load()
    for y in range(H):
        for x in range(W):
            if px[x, y][3] == 0 and not (SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H):
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < W and 0 <= ny < H and px[nx, ny][3]:
                        o[x, y] = c("outline")
                        break
    return out


def main():
    cv = Canvas()
    draw(cv)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    snap(cv.img).save(OUT)
    print(f"Generated {OUT.relative_to(ROOT)} ({W}x{H}, slot {SLOT_X},{SLOT_Y} {SLOT_W}x{SLOT_H}, marks {MARK_70},{MARK_35})")


if __name__ == "__main__":
    main()
