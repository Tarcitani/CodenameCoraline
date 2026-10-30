#!/usr/bin/env python3
"""
Themed boss-bar frame for The Canopy Breaker (`codenamecoraline:canopy_breaker`).

Writes `assets/codenamecoraline/textures/gui/boss_bar/canopy_breaker.png` (288x44): a rail of dark, cracked bark with three
pale claw gouges on each side, gnarled roots curling up from both ends with spruce needles and moss, the brute's head
(heavy brow, amber eyes, tusks) set over the middle, and notches at the 60% and 25% stage marks. The health slot is
TRANSPARENT: the game draws the dark back and the fill behind this frame, so the roots and the head overlap the fill.
The name goes under the rail, centred.

Drawn at 8x with soft shapes, downsampled, snapped to a fixed palette and outlined (hand-painted pixel look).
Geometry constants must match `CanopyBreakerBossBar` in `client/bossbar`.
"""
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/canopy_breaker.png"

W, H = 288, 44
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 46, 18, 196, 6
MARK_60 = SLOT_X + round(SLOT_W * 0.60)
MARK_25 = SLOT_X + round(SLOT_W * 0.25)
S = 8

P = {
    "outline": (18, 11, 8),
    "bark_l": (122, 86, 54), "bark": (84, 58, 38), "bark_m": (62, 42, 28), "bark_d": (38, 25, 17),
    "gouge_l": (238, 208, 150), "gouge": (206, 168, 104), "gouge_d": (150, 112, 66),
    "moss_l": (128, 168, 70), "moss": (84, 122, 52), "moss_d": (50, 80, 38),
    "needle": (40, 96, 58), "needle_d": (24, 62, 40), "needle_l": (70, 130, 78),
    "fur_l": (112, 84, 58), "fur": (78, 56, 38), "fur_d": (48, 33, 24),
    "skin": (126, 92, 66), "skin_d": (92, 64, 46),
    "bone_l": (246, 238, 214), "bone": (214, 198, 160),
    "eye_l": (255, 246, 170), "eye": (255, 190, 50), "eye_d": (220, 110, 24),
    "gold": (214, 168, 70), "gold_d": (140, 100, 36),
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

    def ellipse(self, cx, cy, rx, ry, color):
        self.d.ellipse([(cx - rx) * S, (cy - ry) * S, (cx + rx) * S, (cy + ry) * S], fill=c(color))

    def line(self, pts, width, color):
        self.d.line([(x * S, y * S) for x, y in pts], fill=c(color), width=max(1, int(width * S)))

    def stroke(self, pts, r0, r1, color, dx=0.0, dy=0.0, samples=40):
        curve = catmull(pts, samples)
        n = len(curve)
        for i, (x, y) in enumerate(curve):
            r = r0 + (r1 - r0) * i / max(1, n - 1)
            self.ellipse(x + dx, y + dy, r, r, color)


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


def mir(pts):
    return [(W - x, y) for x, y in pts]


def rail(cv: Canvas):
    body = [(36, 20.5), (44, 14), (244, 14), (252, 20.5), (244, 27), (44, 27)]
    cv.poly(body, "bark_d")
    cv.poly([(38.5, 20.5), (45, 15.2), (243, 15.2), (249.5, 20.5), (243, 25.8), (45, 25.8)], "bark_m")
    cv.poly([(40, 20.5), (46, 16.2), (242, 16.2), (248, 20.5), (242, 24.8), (46, 24.8)], "bark")
    cv.line([(46, 15.5), (242, 15.5)], 1.0, "bark_l")           # lit top edge
    cv.line([(46, 26.4), (242, 26.4)], 1.0, "bark_d")           # shaded underside
    # vertical bark furrows
    x = 52
    while x < 238:
        h = 3 + (x * 7) % 4
        y0 = 15.8 if (x // 5) % 2 else 22 - h / 2
        cv.line([(x, y0), (x + 0.6, y0 + h)], 0.5, "bark_d")
        x += 5.3
    # cracks
    for pts in ([(70, 15.5), (72, 18), (70.5, 20), (73, 23)], [(188, 25.5), (186, 22), (188.5, 20), (187, 17)],
                [(126, 25.5), (128, 23), (126.5, 21.5)]):
        cv.line(pts, 0.6, "outline")
    # claw gouges (three parallel slashes) on each side, pale heartwood showing through
    for base in (60, 210):
        for k in range(3):
            x0 = base + k * 4.2
            cv.line([(x0 - 2.2, 15.0), (x0 + 2.4, 26.6)], 1.5, "gouge_d")
            cv.line([(x0 - 2.0, 15.0), (x0 + 2.2, 26.4)], 1.0, "gouge")
            cv.line([(x0 - 1.7, 15.4), (x0 + 0.4, 21.0)], 0.45, "gouge_l")
    # moss patches
    for mx, my, r in ((100, 14.6, 4.0), (176, 14.4, 5.0), (150, 26.8, 3.4)):
        cv.ellipse(mx, my, r, r * 0.55, "moss_d")
        cv.ellipse(mx - 0.4, my - 0.5, r * 0.8, r * 0.42, "moss")
        cv.ellipse(mx - 0.9, my - 0.9, r * 0.4, r * 0.2, "moss_l")
    # stage notches above the rail: bone chips at 60% and 25%
    for mx in (MARK_60, MARK_25):
        cv.poly([(mx - 2.2, 14.4), (mx + 2.2, 14.4), (mx + 0.8, 10.2), (mx - 0.8, 10.2)], "bone")
        cv.poly([(mx - 0.4, 14.4), (mx + 2.2, 14.4), (mx + 0.8, 10.2), (mx + 0.2, 10.2)], "gouge_d")
        cv.line([(mx - 1.4, 14.0), (mx - 0.5, 10.8)], 0.5, "bone_l")


def root(cv: Canvas, flip: bool):
    f = mir if flip else (lambda p: p)
    sx = -1 if flip else 1
    main = f([(46, 21), (38, 22), (29, 17), (20, 19), (12, 12), (5, 3)])
    branches = [
        f([(33, 19.5), (30, 12), (31, 5), (28, 1)]),
        f([(22, 18.2), (17, 11), (19, 5), (15, 1.5)]),
        f([(40, 22.5), (36, 30), (31, 35), (27, 41)]),
        f([(26, 19), (22, 27), (24, 33), (20, 38)]),
        f([(12, 12.5), (7, 13), (3, 10)]),
    ]
    for pts, r0, r1 in [(main, 3.3, 0.9)] + [(b, 1.7, 0.4) for b in branches]:
        cv.stroke(pts, r0 * 1.02, r1, "bark_d", dx=0.5 * sx, dy=0.7)
        cv.stroke(pts, r0 * 0.92, r1 * 0.9, "bark_m")
        cv.stroke(pts, r0 * 0.74, r1 * 0.8, "bark", dx=-0.25 * sx, dy=-0.3)
        cv.stroke(pts, r0 * 0.3, r1 * 0.5, "bark_l", dx=-0.5 * sx, dy=-0.6)
    # spruce needle sprays on the upper branches, moss on the root crown
    for (px, py), ang in (((28, 2), -100), ((15, 2), -110), ((31, 6), -60), ((19, 6), -70), ((3, 9), 150), ((5, 4), -150)):
        for off, col in ((-26, "needle_d"), (0, "needle"), (26, "needle_l")):
            a = math.radians(ang + off + (180 - 2 * (ang % 360 if False else 0)) * 0)
            x1 = (W - px if flip else px)
            dx = math.cos(a) * 5.4 * (-1 if flip else 1)
            dy = math.sin(a) * 5.4
            cv.line([(x1, py + 1.6), (x1 + dx, py + 1.6 + dy)], 1.1, col)
    mx = W - 40 if flip else 40
    cv.ellipse(mx, 19.6, 5.2, 2.6, "moss_d")
    cv.ellipse(mx - 0.5 * sx, 18.9, 4.1, 2.0, "moss")
    cv.ellipse(mx - 1.0 * sx, 18.3, 2.0, 0.9, "moss_l")


def head(cv: Canvas):
    cx = W / 2
    # ears and fur mass
    cv.ellipse(cx - 19.5, 13.5, 3.2, 4.4, "fur_d")
    cv.ellipse(cx + 19.5, 13.5, 3.2, 4.4, "fur_d")
    cv.ellipse(cx - 19.5, 13.5, 1.7, 2.6, "skin_d")
    cv.ellipse(cx + 19.5, 13.5, 1.7, 2.6, "skin_d")
    cv.poly([(cx - 21, 15), (cx - 17, 3.5), (cx - 8, 0.4), (cx + 8, 0.4), (cx + 17, 3.5), (cx + 21, 15),
             (cx + 15, 25), (cx - 15, 25)], "fur_d")
    cv.poly([(cx - 19, 14.5), (cx - 15.5, 4.6), (cx - 7.5, 1.4), (cx + 7.5, 1.4), (cx + 15.5, 4.6), (cx + 19, 14.5),
             (cx + 13.5, 24), (cx - 13.5, 24)], "fur")
    cv.poly([(cx - 14, 4.8), (cx - 7, 2.2), (cx + 7, 2.2), (cx + 14, 4.8), (cx + 8, 6.2), (cx - 8, 6.2)], "fur_l")  # crest
    # moss tuft on the crown
    cv.ellipse(cx - 6, 2.4, 4.4, 1.6, "moss_d")
    cv.ellipse(cx - 6.6, 1.9, 3.2, 1.2, "moss")
    # heavy brow ridge
    cv.poly([(cx - 17, 10.6), (cx - 6, 9.0), (cx, 11.4), (cx + 6, 9.0), (cx + 17, 10.6), (cx + 15, 14.6), (cx, 15.4), (cx - 15, 14.6)], "bark_d")
    cv.poly([(cx - 15.4, 10.9), (cx - 6, 9.6), (cx, 11.8), (cx + 6, 9.6), (cx + 15.4, 10.9), (cx + 14.4, 12.2), (cx - 14.4, 12.2)], "fur_d")
    # amber eyes under the brow, with a glow halo
    for ex in (cx - 8.2, cx + 8.2):
        cv.ellipse(ex, 14.2, 4.0, 2.7, "eye_d")
        cv.ellipse(ex, 14.2, 3.0, 1.9, "eye")
        cv.ellipse(ex - 0.5, 13.7, 1.5, 0.9, "eye_l")
        cv.ellipse(ex + 0.4, 14.3, 0.55, 1.2, "outline")
    # muzzle, nostrils, mouth, tusks
    cv.ellipse(cx, 20.4, 11.5, 5.8, "skin_d")
    cv.ellipse(cx, 19.8, 10.2, 4.8, "skin")
    cv.ellipse(cx - 2.6, 18.3, 1.1, 0.8, "outline")
    cv.ellipse(cx + 2.6, 18.3, 1.1, 0.8, "outline")
    cv.line([(cx - 7, 22.2), (cx - 2.5, 23.4), (cx + 2.5, 23.4), (cx + 7, 22.2)], 0.8, "outline")
    cv.poly([(cx - 7.6, 21.4), (cx - 5.4, 21.8), (cx - 6.6, 26.2)], "bone")
    cv.poly([(cx + 7.6, 21.4), (cx + 5.4, 21.8), (cx + 6.6, 26.2)], "bone")
    cv.poly([(cx - 7.0, 21.8), (cx - 5.9, 22.0), (cx - 6.5, 24.4)], "bone_l")
    cv.poly([(cx + 7.0, 21.8), (cx + 5.9, 22.0), (cx + 6.5, 24.4)], "bone_l")


def hanging_vines(cv: Canvas):
    for vx, ln in ((92, 5.0), (112, 3.2), (172, 3.4), (196, 5.2), (64, 3.0), (222, 3.0)):
        cv.line([(vx, 26.6), (vx + 0.8, 26.6 + ln * 0.5), (vx - 0.4, 26.6 + ln)], 0.7, "moss_d")
        cv.ellipse(vx - 0.4, 26.6 + ln, 0.9, 1.1, "moss")


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
    """Body (rail, roots) and face are snapped separately so the face can overlap the transparent health slot."""
    out = snap(body)
    px = out.load()
    # the health slot stays transparent, framed by a dark inner lip
    for y in range(SLOT_Y - 1, SLOT_Y + SLOT_H + 1):
        for x in range(SLOT_X - 1, SLOT_X + SLOT_W + 1):
            inside = SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H
            px[x, y] = (0, 0, 0, 0) if inside else P["outline"] + (255,)
    # the face covers the middle of the slot and the rail
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
    root(cv, False)
    root(cv, True)
    rail(cv)
    hanging_vines(cv)
    face = Canvas()
    head(face)
    frame = finish(cv.img, face.img)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    frame.save(OUT)
    # review sheet: frame over a mock fill in the three stage colours
    sheet = Image.new("RGBA", (W, H * 3 + 8), (30, 28, 34, 255))
    fills = [(88, 140, 58), (214, 168, 46), (226, 70, 24)]
    for i, color in enumerate(fills):
        tile = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        t = tile.load()
        for y in range(SLOT_Y, SLOT_Y + SLOT_H):
            for x in range(SLOT_X, SLOT_X + SLOT_W):
                t[x, y] = color + (255,)
        tile.alpha_composite(frame)
        sheet.alpha_composite(tile, (0, i * (H + 4)))
    review = ROOT / "build/canopy-breaker-bossbar/review.png"
    review.parent.mkdir(parents=True, exist_ok=True)
    sheet.resize((W * 4, (H * 3 + 8) * 4), Image.Resampling.NEAREST).save(review)
    print("Wrote", OUT)


if __name__ == "__main__":
    main()
