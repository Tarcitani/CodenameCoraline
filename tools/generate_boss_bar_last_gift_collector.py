#!/usr/bin/env python3
"""
Themed boss-bar frame for Nicolas Noel, the Renegade (`codenamecoraline:last_gift_collector`).

Writes `assets/codenamecoraline/textures/gui/boss_bar/last_gift_collector.png` (288x44) in the style of ornate boss bars:
palmate antler racks on both ends, a thin frost-fringed rail whose health slot is TRANSPARENT (the game draws the dark
back and the fill behind this frame), crossed greataxes behind his face (hat, glowing amber eyes, long nose, moustache)
and a huge beard whose wisps trail along the rail. Name text goes under the rail on both sides of the beard.

The art is drawn at 8x with soft shapes and shading, downsampled, snapped to a fixed palette and outlined, which gives
the hand-painted pixel look. Geometry constants must match `LastGiftCollectorBossBar` in `client/bossbar`.
"""
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/last_gift_collector.png"

W, H = 288, 44
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 46, 18, 196, 6   # health slot (transparent in the texture)
S = 8                                              # supersampling

# ---- palette (every pixel is snapped to one of these) -----------------------------------------------------------
P = {
    "outline": (22, 10, 16),
    "bone_l": (250, 242, 220), "bone": (222, 206, 170), "bone_m": (186, 166, 128), "bone_d": (128, 108, 82),
    "gold_l": (255, 232, 130), "gold": (222, 176, 62), "gold_d": (150, 108, 34),
    "wine": (92, 18, 34), "wine_d": (52, 10, 22), "wine_l": (138, 30, 50),
    "red_l": (222, 70, 72), "red": (180, 30, 46), "red_d": (118, 16, 32),
    "skin_l": (190, 210, 230), "skin": (140, 164, 192), "skin_d": (96, 118, 148),
    "nose": (214, 108, 126),
    "beard_l": (250, 244, 226), "beard": (226, 214, 176), "beard_m": (186, 174, 138), "beard_d": (140, 130, 100),
    "steel_l": (236, 244, 250), "steel": (166, 180, 194), "steel_d": (104, 118, 136),
    "wood": (112, 76, 46), "wood_d": (66, 42, 28),
    "ice_l": (222, 246, 255), "ice": (160, 214, 244), "ice_d": (100, 160, 206),
    "eye_l": (255, 250, 190), "eye": (255, 200, 60), "eye_d": (235, 130, 30),
    "holly": (46, 130, 78), "holly_d": (28, 84, 52), "berry": (210, 34, 50),
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
        """Tapered round-capped stroke along a smooth curve through pts (Catmull-Rom)."""
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


def antler(cv: Canvas, flip: bool):
    f = mir if flip else (lambda p: p)
    beam = f([(48, 21), (40, 19.5), (32, 15), (22, 12.5), (12, 8), (4, 2.5)])
    tines_up = [
        f([(36, 17), (35.5, 10), (33, 4), (32, 0.8)]),
        f([(28, 13.8), (28.5, 7), (26, 2.5), (25, 0.6)]),
        f([(20, 12), (19, 6.5), (15, 2.5), (14, 0.5)]),
        f([(11, 8.5), (7, 6), (3.5, 6.5), (1.2, 8)]),
    ]
    brow = [
        f([(42, 20.5), (40, 28), (36, 33), (33.5, 38)]),
        f([(34, 17.5), (30, 25), (25, 30), (21, 33)]),
    ]
    for pts, r0, r1 in [(beam, 3.2, 1.0)] + [(t, 1.7, 0.45) for t in tines_up] + [(b, 1.9, 0.5) for b in brow]:
        cv.stroke(pts, r0 * 1.0, r1, "bone_d", dx=0.5 * (-1 if flip else 1), dy=0.7)       # shadow side
        cv.stroke(pts, r0 * 0.92, r1 * 0.9, "bone_m")
        cv.stroke(pts, r0 * 0.78, r1 * 0.8, "bone", dx=-0.25 * (-1 if flip else 1), dy=-0.3)
        cv.stroke(pts, r0 * 0.34, r1 * 0.5, "bone_l", dx=-0.55 * (-1 if flip else 1), dy=-0.7)  # highlight
    # holly + bell at the antler root
    hx = W - 47 if flip else 47
    for ang, col in ((-35, "holly"), (30, "holly_d"), (-80, "holly")):
        a = math.radians(ang + (180 if flip else 0))
        cv.poly([(hx, 25), (hx + math.cos(a - 0.35) * 7, 25 + math.sin(a - 0.35) * 7),
                 (hx + math.cos(a) * 9, 25 + math.sin(a) * 9), (hx + math.cos(a + 0.35) * 7, 25 + math.sin(a + 0.35) * 7)], col)
    for bx, by in ((hx - 1.6, 25.6), (hx + 1.4, 26.4), (hx - 0.2, 27.4)):
        cv.ellipse(bx, by, 1.5, 1.5, "berry")
    cv.ellipse(hx + (4 if flip else -4), 31, 2.4, 2.8, "gold")
    cv.ellipse(hx + (4 if flip else -4) - 0.7, 30.2, 0.9, 1.1, "gold_l")
    cv.ellipse(hx + (4 if flip else -4), 33.6, 0.8, 0.8, "wood_d")


def rail(cv: Canvas):
    body = [(36, 20.5), (44, 14.5), (244, 14.5), (252, 20.5), (244, 26.5), (44, 26.5)]
    cv.poly(body, "wine_d")
    cv.poly([(38.5, 20.5), (45, 15.7), (243, 15.7), (249.5, 20.5), (243, 25.3), (45, 25.3)], "wine")
    cv.line([(44, 14.6), (244, 14.6)], 0.9, "gold_l")
    cv.line([(44, 26.2), (244, 26.2)], 0.9, "gold_d")
    cv.line([(36.5, 20.5), (44, 14.7)], 0.9, "gold")
    cv.line([(36.5, 20.5), (44, 26.2)], 0.9, "gold_d")
    cv.line([(251.5, 20.5), (244, 14.7)], 0.9, "gold")
    cv.line([(251.5, 20.5), (244, 26.2)], 0.9, "gold_d")
    # fur tufts along the top edge
    x = 52
    while x < 238:
        if not (112 < x < 176):
            h = 1.6 + ((x * 7) % 5) * 0.45
            cv.poly([(x, 14.8), (x + 2.2, 14.8), (x + 1.1, 14.8 - h)], "beard" if (x // 3) % 2 else "beard_l")
        x += 3.1
    # icicles under the rail
    for xi, ln in ((54, 3.5), (63, 5), (73, 3), (84, 4.5), (96, 3.2), (192, 3.2), (204, 4.5), (215, 3), (225, 5), (234, 3.5)):
        cv.poly([(xi, 26.0), (xi + 2.6, 26.0), (xi + 1.3, 26.0 + ln)], "ice")
        cv.poly([(xi, 26.0), (xi + 1.2, 26.0), (xi + 0.9, 26.0 + ln * 0.7)], "ice_l")
    # gold rivets at the rail ends
    for rx in (50.5, 237.5):
        cv.ellipse(rx, 20.5, 1.7, 1.7, "gold")
        cv.ellipse(rx - 0.4, 20.1, 0.7, 0.7, "gold_l")


def axe(cv: Canvas, flip: bool):
    f = mir if flip else (lambda p: p)
    haft = f([(125, 37), (133, 30), (142, 20), (154, 11), (165, 4)])
    cv.stroke(haft, 1.9, 1.9, "wood_d", dx=0.3, dy=0.4)
    cv.stroke(haft, 1.5, 1.5, "wood")
    cv.stroke(haft, 0.5, 0.5, "gold_d", dx=-0.4, dy=-0.4)
    cv.ellipse(*(f([(125, 37.5)])[0]), 2.0, 2.0, "gold")
    # axe head (blade facing outwards, up)
    head = f([(160.5, 6.5), (166, 0.5), (176, 1.2), (180, 7), (173, 13), (168, 9.5)])
    cv.poly(head, "steel_d")
    cv.poly(f([(161.5, 6.8), (166.5, 1.5), (175, 2), (177.5, 6.5), (172, 11), (168, 8.6)]), "steel")
    cv.poly(f([(163, 6.5), (167, 2.6), (174, 3), (172, 5.5), (166, 7.8)]), "steel_l")
    cv.line(f([(176.5, 3), (179, 7), (173, 12.4)]), 0.9, "ice_l")
    cv.ellipse(*(f([(166, 9.2)])[0]), 1.7, 1.7, "gold")


def face(cv: Canvas):
    cx = 144
    # beard (huge, behind the face) with strands
    cv.poly([(127, 24), (161, 24), (162, 32), (153, 40), (144, 44), (135, 40), (126, 32)], "beard_d")
    cv.poly([(128, 24), (160, 24), (160.8, 31.5), (152.5, 39), (144, 43), (135.5, 39), (127.2, 31.5)], "beard_m")
    cv.poly([(130, 24), (158, 24), (159, 30.5), (151.5, 37.5), (144, 41.5), (136.5, 37.5), (129, 30.5)], "beard")
    cv.poly([(133, 26), (143, 26), (141, 36), (136, 36)], "beard_l")
    for sx in (134, 139, 144, 149, 154):
        cv.line([(sx, 30), (sx + (-1 if sx < 144 else 1), 38)], 0.5, "beard_m")
    # wisps that trail along the rail
    for flip in (False, True):
        pf = mir if flip else (lambda p: p)
        for pts, w in (([(128, 29), (118, 30.5), (108, 29), (98, 30.8), (88, 29.5)], 1.5),
                       ):
            cv.stroke(pf(pts), w, 0.25, "beard_d", dy=0.5)
            cv.stroke(pf(pts), w * 0.75, 0.2, "beard")
            cv.stroke(pf(pts), w * 0.3, 0.1, "beard_l", dy=-0.35)
    # face
    cv.ellipse(cx, 21.5, 14.5, 9.2, "skin_d")
    cv.ellipse(cx, 21, 14, 8.8, "skin")
    cv.ellipse(cx - 1.5, 19.6, 11, 5.8, "skin_l")
    cv.poly([(130, 22), (158, 22), (155, 28), (133, 28)], "skin_d")
    # brows and glowing eyes
    cv.poly([(132, 17.4), (140.5, 19.6), (140.5, 21), (132, 19.6)], "outline")
    cv.poly([(156, 17.4), (147.5, 19.6), (147.5, 21), (156, 19.6)], "outline")
    for ex in (137, 151):
        cv.ellipse(ex, 22, 4.2, 2.6, "eye_d")
        cv.ellipse(ex, 21.9, 3.3, 1.9, "eye")
        cv.ellipse(ex - 0.5, 21.5, 1.6, 0.9, "eye_l")
    # long cold nose
    cv.poly([(142.6, 20), (145.4, 20), (146.4, 30), (141.6, 30)], "skin_l")
    cv.poly([(144.6, 20.5), (145.4, 20.5), (146.4, 30), (144.8, 30)], "skin_d")
    cv.ellipse(cx, 30.6, 2.5, 2.0, "nose")
    cv.ellipse(cx - 0.6, 30.0, 0.9, 0.6, "red_l")
    # drooping moustache
    for sgn in (-1, 1):
        cv.poly([(cx + sgn * 0.8, 28), (cx + sgn * 6, 27), (cx + sgn * 11.5, 29.6), (cx + sgn * 9, 33), (cx + sgn * 3.5, 32)], "beard")
        cv.poly([(cx + sgn * 1.5, 28.4), (cx + sgn * 6, 27.6), (cx + sgn * 8.5, 29), (cx + sgn * 4, 29.8)], "beard_l")
    # hat: body, drooping tip, fur brim and pompom
    cv.poly([(128.5, 13.5), (159.5, 13.5), (153, 5), (144, 1.5), (135, 5)], "red_d")
    cv.poly([(131, 13.5), (156, 13.5), (150.5, 6), (144, 3), (137, 6)], "red")
    cv.poly([(133, 12.5), (146, 12.5), (141, 6.5), (137, 7.5)], "red_l")
    cv.poly([(151, 4.5), (158.5, 9), (165, 18), (160.4, 19), (155, 11)], "red_d")
    cv.poly([(151, 5.5), (157, 9.5), (162, 17), (159.5, 17.4), (154.5, 11)], "red")
    cv.ellipse(163, 19.5, 3.6, 3.6, "beard_m")
    cv.ellipse(162.6, 19, 3, 3, "beard")
    cv.ellipse(162, 18.2, 1.3, 1.3, "beard_l")
    cv.poly([(126, 12), (162, 12), (164, 16.4), (124, 16.4)], "beard_m")
    cv.poly([(126.5, 12.2), (161.5, 12.2), (163, 15.6), (125, 15.6)], "beard")
    for tx in range(127, 162, 4):
        cv.ellipse(tx, 12.4, 1.6, 1.2, "beard_l")
        cv.ellipse(tx + 2, 16, 1.5, 1.1, "beard")


def snap(canvas: Canvas) -> Image.Image:
    small = canvas.img.resize((W, H), Image.BOX)
    px = small.load()
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    opx = out.load()
    for y in range(H):
        for x in range(W):
            r, g, b, a = px[x, y]
            if a < 128:
                continue
            best = min(PAL, key=lambda q: (q[0] - r) ** 2 + (q[1] - g) ** 2 + (q[2] - b) ** 2)
            opx[x, y] = best + (255,)
    return out


def build() -> Image.Image:
    frame = Canvas()
    antler(frame, False)
    antler(frame, True)
    rail(frame)
    emblem = Canvas()
    axe(emblem, False)
    axe(emblem, True)
    face(emblem)
    base = snap(frame)
    top = snap(emblem)
    tpx = top.load()
    bpx = base.load()
    # the health slot is transparent (the game draws its dark back and the fill behind this frame), except under the emblem
    for y in range(SLOT_Y, SLOT_Y + SLOT_H):
        for x in range(SLOT_X, SLOT_X + SLOT_W):
            if tpx[x, y][3] == 0:
                bpx[x, y] = (0, 0, 0, 0)
    out = base
    out.alpha_composite(top)
    opx = out.load()
    slot_open = lambda x, y: (SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H and opx[x, y][3] == 0)
    res = out.copy()
    rpx = res.load()
    for y in range(H):
        for x in range(W):
            if opx[x, y][3] == 0 and not slot_open(x, y):
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < W and 0 <= ny < H and opx[nx, ny][3] > 0:
                        rpx[x, y] = c("outline")
                        break
    for x in range(SLOT_X, SLOT_X + SLOT_W):
        for y in (SLOT_Y - 1, SLOT_Y + SLOT_H):
            if opx[x, y][3] == 0 or tpx[x, y][3] == 0:
                rpx[x, y] = c("outline")
    return res


if __name__ == "__main__":
    OUT.parent.mkdir(parents=True, exist_ok=True)
    build().save(OUT)
    print("Generated", OUT)
