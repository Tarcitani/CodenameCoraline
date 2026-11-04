#!/usr/bin/env python3
"""
Themed boss-bar frame for the Crimson Commander (`codenamecoraline:crimson_commander`).

Writes `assets/codenamecoraline/textures/gui/boss_bar/crimson_commander.png` (288x44): the rail is his greatsword lying
flat, a blade of obsidian steel with gold edges whose fuller is the health slot; the hilt (crimson grip wrapped in gold,
a crossguard and a pommel with a ruby) closes the left end and the point of the blade the right end. His closed helm
(gold brow band, the T visor glowing ruby, horns and the crest) sits over the middle with the long red plume flowing to
the right, and torn crimson pennants hang under the rail. Gold notches above the rail mark the 60% and 25% stages.
The health slot is TRANSPARENT: the game draws the dark back and the fill behind this frame, so the helm overlaps the
fill. The name is split in two and set under the rail, either side of the helm.

Drawn at 8x with soft shapes, downsampled, snapped to a fixed palette and outlined (hand-painted pixel look).
Geometry constants must match `CrimsonCommanderBossBar` in `client/bossbar`.
"""
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/crimson_commander.png"

W, H = 288, 44
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 46, 18, 196, 6
MARK_60 = SLOT_X + round(SLOT_W * 0.60)
MARK_25 = SLOT_X + round(SLOT_W * 0.25)
S = 8

P = {
    "outline": (20, 8, 10),
    "steel_l": (70, 64, 80), "steel": (40, 36, 48), "steel_d": (24, 22, 28), "steel_dd": (14, 12, 18),
    "gold_l": (243, 207, 85), "gold": (212, 175, 55), "gold_d": (150, 112, 30), "gold_dd": (96, 68, 18),
    "crimson_l": (184, 29, 42), "crimson": (139, 17, 30), "crimson_m": (110, 12, 24), "crimson_d": (74, 7, 14),
    "ruby_l": (255, 150, 150), "ruby": (255, 42, 60), "ruby_d": (190, 20, 40),
    "plume_l": (212, 46, 56), "plume": (168, 20, 34), "plume_d": (98, 10, 20),
    "leather": (54, 30, 26),
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

    def stroke(self, pts, r0, r1, color, samples=40):
        curve = catmull(pts, samples)
        n = len(curve)
        for i, (x, y) in enumerate(curve):
            r = r0 + (r1 - r0) * i / max(1, n - 1)
            self.ellipse(x, y, r, r, color)


def catmull(pts, samples):
    pts = [pts[0]] + list(pts) + [pts[-1]]
    out = []
    segs = len(pts) - 3
    per = max(2, samples // segs)
    for i in range(segs):
        p0, p1, p2, p3 = pts[i], pts[i + 1], pts[i + 2], pts[i + 3]
        for k in range(per):
            t = k / per
            t2, t3 = t * t, t * t * t
            out.append(tuple(0.5 * ((2 * p1[j]) + (-p0[j] + p2[j]) * t + (2 * p0[j] - 5 * p1[j] + 4 * p2[j] - p3[j]) * t2
                                    + (-p0[j] + 3 * p1[j] - 3 * p2[j] + p3[j]) * t3) for j in range(2)))
    out.append(pts[-2])
    return out


def blade(cv: Canvas):
    # the flat of the blade: from the crossguard (x 40) to the point (x 268), gold edges top and bottom
    cv.poly([(40, 13.5), (250, 13.5), (270, 21), (250, 28.5), (40, 28.5)], "gold_d")
    cv.poly([(40, 14.6), (249, 14.6), (267, 21), (249, 27.4), (40, 27.4)], "gold")
    cv.poly([(40, 15.6), (248, 15.6), (264, 21), (248, 26.4), (40, 26.4)], "steel_d")
    cv.poly([(40, 16.2), (247, 16.2), (261, 21), (247, 25.8), (40, 25.8)], "steel")
    cv.line([(41, 14.2), (249, 14.2)], 0.7, "gold_l")          # lit upper edge
    cv.line([(41, 16.6), (246, 16.6)], 0.6, "steel_l")
    # the fuller around the health slot (darker groove)
    cv.rect(SLOT_X - 2, SLOT_Y - 1.5, SLOT_X + SLOT_W + 2, SLOT_Y + SLOT_H + 1.5, "steel_dd")
    # ruby runes etched along the blade beyond the slot's ends
    for x in (252, 256):
        cv.rect(x, 20, x + 1.4, 22, "ruby")
    # gold notches above the edge at the stage marks
    for mx in (MARK_60, MARK_25):
        cv.poly([(mx - 2.2, 13.6), (mx + 2.2, 13.6), (mx + 0.8, 9.6), (mx - 0.8, 9.6)], "gold")
        cv.poly([(mx - 0.2, 13.6), (mx + 2.2, 13.6), (mx + 0.8, 9.6), (mx + 0.2, 9.6)], "gold_d")
        cv.line([(mx - 1.3, 13.2), (mx - 0.5, 10.2)], 0.5, "gold_l")


def hilt(cv: Canvas):
    # crossguard: a tall gold bar with flared tips, a ruby set in the middle
    cv.poly([(36, 6), (42, 8), (42, 34), (36, 36), (33.5, 33), (35, 21), (33.5, 9)], "gold_d")
    cv.poly([(36.5, 7.2), (41, 8.8), (41, 33.2), (36.5, 34.8), (34.8, 32.5), (36.2, 21), (34.8, 9.5)], "gold")
    cv.line([(36.6, 8), (37.2, 20)], 0.8, "gold_l")
    cv.ellipse(38, 21, 2.2, 2.6, "ruby_d")
    cv.ellipse(38, 21, 1.5, 1.9, "ruby")
    cv.ellipse(37.5, 20.3, 0.6, 0.7, "ruby_l")
    # grip wrapped in crimson leather and gold wire
    cv.rect(12, 18, 34, 24, "crimson_d")
    cv.rect(12, 18.7, 34, 23.3, "crimson")
    for x in range(13, 34, 3):
        cv.line([(x, 18.2), (x + 2, 23.8)], 0.7, "gold_d")
    cv.line([(12.5, 19.2), (33.5, 19.2)], 0.5, "crimson_l")
    # pommel: a gold disc with a ruby
    cv.ellipse(8, 21, 5.4, 5.8, "gold_d")
    cv.ellipse(8, 21, 4.4, 4.8, "gold")
    cv.ellipse(7.2, 19.8, 1.6, 1.4, "gold_l")
    cv.ellipse(8.4, 21.4, 2.0, 2.2, "ruby_d")
    cv.ellipse(8.2, 21.2, 1.3, 1.5, "ruby")


def pennants(cv: Canvas):
    # torn crimson pennants hanging from the blade, gold-hemmed
    for x0, ln in ((48, 13), (230, 13)):  # at the ends, clear of the name set under the rail
        top = 27.6
        cv.poly([(x0, top), (x0 + 9, top), (x0 + 9, top + ln), (x0 + 6.5, top + ln - 3), (x0 + 4.5, top + ln + 0.5),
                 (x0 + 2.2, top + ln - 2.6), (x0, top + ln)], "crimson_d")
        cv.poly([(x0 + 0.8, top + 0.6), (x0 + 8.2, top + 0.6), (x0 + 8.2, top + ln - 1), (x0 + 6.5, top + ln - 3.6),
                 (x0 + 4.5, top + ln - 0.6), (x0 + 2.2, top + ln - 3.4), (x0 + 0.8, top + ln - 1.2)], "crimson")
        cv.line([(x0 + 0.6, top + 1.2), (x0 + 8.4, top + 1.2)], 0.9, "gold")
        cv.line([(x0 + 2.2, top + 2.5), (x0 + 2.2, top + ln - 4)], 0.5, "crimson_l")


def helm(cv: Canvas):
    cx = W / 2
    # plume flowing back to the right from the crest
    cv.stroke([(cx + 1, 4), (cx + 3, -3), (cx + 12, -1.5), (cx + 20, 4.0), (cx + 25, 10.0), (cx + 29, 14.5)], 2.6, 0.9, "plume_d")
    cv.stroke([(cx + 2, 1.8), (cx + 10, 0.6), (cx + 18, 3.4), (cx + 24, 8.2), (cx + 28.5, 11.8)], 1.8, 0.6, "plume")
    cv.stroke([(cx + 4, 1.0), (cx + 11, 0.2), (cx + 17, 2.6)], 0.8, 0.3, "plume_l")
    # horns sweeping up and out
    for sx in (-1, 1):
        cv.poly([(cx + sx * 9, 12), (cx + sx * 13.5, 9.5), (cx + sx * 12, 14.5), (cx + sx * 9.5, 15)], "crimson_d")
        cv.line([(cx + sx * 9.8, 12.4), (cx + sx * 12.6, 10.6)], 0.5, "crimson_l")
    # the helm: crimson, rounded top, a little wider at the cheeks
    cv.poly([(cx - 10.5, 30), (cx - 11, 10), (cx - 8, 4.5), (cx + 8, 4.5), (cx + 11, 10), (cx + 10.5, 30), (cx + 6, 32.5),
             (cx - 6, 32.5)], "crimson_d")
    cv.poly([(cx - 9.6, 29.4), (cx - 10, 10.5), (cx - 7.4, 5.6), (cx + 7.4, 5.6), (cx + 10, 10.5), (cx + 9.6, 29.4),
             (cx + 5.6, 31.6), (cx - 5.6, 31.6)], "crimson")
    cv.poly([(cx - 9.6, 29.4), (cx - 10, 10.5), (cx - 7.4, 5.6), (cx - 4, 5.6), (cx - 6.4, 11), (cx - 6.6, 29)], "crimson_l")
    cv.poly([(cx + 6.6, 29), (cx + 6.4, 11), (cx + 4, 5.6), (cx + 7.4, 5.6), (cx + 10, 10.5), (cx + 9.6, 29.4)], "crimson_m")
    # crest: a sharp gold fin over the crown
    cv.poly([(cx - 1.8, 6.5), (cx - 1.4, 1.4), (cx, -0.5), (cx + 1.4, 1.4), (cx + 1.8, 6.5)], "crimson_d")
    cv.poly([(cx - 1.0, 6), (cx - 0.7, 1.6), (cx, 0.4), (cx + 0.4, 1.6), (cx + 0.7, 6)], "crimson_l")
    # gold brow band
    cv.poly([(cx - 10.2, 11), (cx + 10.2, 11), (cx + 10.2, 13.0), (cx - 10.2, 13.0)], "crimson_d")
    # the T visor: a dark slit across and down, glowing ruby inside
    cv.poly([(cx - 8.2, 15), (cx + 8.2, 15), (cx + 8.2, 18.4), (cx + 2.2, 18.4), (cx + 2.2, 27), (cx - 2.2, 27), (cx - 2.2, 18.4),
             (cx - 8.2, 18.4)], "steel_dd")
    cv.poly([(cx - 7, 16.1), (cx + 7, 16.1), (cx + 7, 17.3), (cx - 7, 17.3)], "ruby")
    cv.poly([(cx - 0.9, 17.3), (cx + 0.9, 17.3), (cx + 0.9, 25.5), (cx - 0.9, 25.5)], "ruby_d")
    cv.poly([(cx - 4.5, 16.3), (cx - 1.5, 16.3), (cx - 1.5, 16.9), (cx - 4.5, 16.9)], "ruby_l")
    # breathing holes and a gold rim at the chin
    for hx in (-6, -4.4, 4.4, 6):
        cv.ellipse(cx + hx, 24.5, 0.55, 0.55, "crimson_d")
    cv.line([(cx - 6, 31.4), (cx + 6, 31.4)], 0.8, "crimson_d")


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
    """Body (blade, hilt, pennants) and helm are snapped separately so the helm can overlap the transparent slot."""
    out = snap(body)
    px = out.load()
    for y in range(SLOT_Y - 1, SLOT_Y + SLOT_H + 1):
        for x in range(SLOT_X - 1, SLOT_X + SLOT_W + 1):
            inside = SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H
            px[x, y] = (0, 0, 0, 0) if inside else P["outline"] + (255,)
    out.alpha_composite(snap(face))
    px = out.load()
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
    cv = Canvas()
    pennants(cv)
    blade(cv)
    hilt(cv)
    face = Canvas()
    helm(face)
    frame = finish(cv.img, face.img)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    frame.save(OUT)
    # review sheet: frame over a mock fill in the three stage colours
    sheet = Image.new("RGBA", (W, H * 3 + 8), (30, 28, 34, 255))
    fills = [(176, 24, 42), (255, 60, 90), (232, 180, 60)]
    for i, color in enumerate(fills):
        tile = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        t = tile.load()
        for y in range(SLOT_Y, SLOT_Y + SLOT_H):
            for x in range(SLOT_X, SLOT_X + int(SLOT_W * (1.0 - 0.3 * i))):
                t[x, y] = color + (255,)
        tile.alpha_composite(frame)
        sheet.alpha_composite(tile, (0, i * (H + 4)))
    review = ROOT / "build/crimson-commander-bossbar/review.png"
    review.parent.mkdir(parents=True, exist_ok=True)
    sheet.resize((W * 4, (H * 3 + 8) * 4), Image.Resampling.NEAREST).save(review)
    print("Wrote", OUT)


if __name__ == "__main__":
    main()
