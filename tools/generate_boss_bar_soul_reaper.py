#!/usr/bin/env python3
"""
Themed boss-bar frame for the Reaper of Wandering Souls (`codenamecoraline:soul_reaper`).

Writes `assets/codenamecoraline/textures/gui/boss_bar/soul_reaper.png` (288x44): the rail is the Great Scythe lying
horizontally — dark ash-wood and bone shaft with its lantern pommel on the left, sweeping into the frost-rimmed crescent
blade on the right whose fuller is the transparent health slot. Over the center sits the pointed abyssal cowl with two
glowing crimson eyes above the silver spiral brooch. Frost-silver notches mark the 65% and 30% stage thresholds.
Geometry constants match `SoulReaperBossBar` in `client/bossbar`.
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/soul_reaper.png"

W, H = 288, 44
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 46, 18, 196, 6
MARK_65 = SLOT_X + round(SLOT_W * 0.65)
MARK_30 = SLOT_X + round(SLOT_W * 0.30)
S = 8

P = {
    "outline": (10, 14, 22),
    "slate_l": (76, 94, 120), "slate": (44, 56, 76), "slate_d": (26, 34, 48), "slate_dd": (14, 18, 28),
    "silver_l": (228, 244, 255), "silver": (176, 206, 230), "silver_d": (118, 146, 174), "silver_dd": (72, 92, 116),
    "bone_l": (220, 226, 234), "bone": (182, 192, 206), "bone_d": (124, 136, 152),
    "cyan_l": (176, 250, 255), "cyan": (76, 214, 238), "cyan_d": (36, 138, 168),
    "crimson_l": (255, 110, 134), "crimson": (232, 32, 64), "crimson_d": (148, 14, 38),
    "ash": (52, 40, 46),
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


def scythe_rail(cv: Canvas):
    # Scythe blade rail from collar (x 40) to curved crescent reaper tip (x 276)
    cv.poly([(40, 13.5), (252, 13.5), (276, 19.5), (262, 31.0), (246, 28.5), (40, 28.5)], "silver_d")
    cv.poly([(40, 14.5), (251, 14.5), (272, 19.8), (260, 29.2), (246, 27.4), (40, 27.4)], "silver")
    cv.poly([(40, 15.6), (249, 15.6), (266, 20.5), (248, 26.4), (40, 26.4)], "slate_d")
    cv.poly([(40, 16.2), (247, 16.2), (262, 20.8), (247, 25.8), (40, 25.8)], "slate")
    cv.line([(41, 14.2), (250, 14.2)], 0.7, "silver_l")
    cv.line([(41, 16.6), (246, 16.6)], 0.6, "slate_l")
    # Recessed fuller around the health slot
    cv.rect(SLOT_X - 2, SLOT_Y - 1.5, SLOT_X + SLOT_W + 2, SLOT_Y + SLOT_H + 1.5, "slate_dd")
    # Glowing cyan soul runes near the scythe tip
    for x in (250, 255, 260):
        cv.rect(x, 19.8, x + 1.4, 22.0, "cyan")
    # Frost-silver notches above the rail at 65% and 30% stage marks
    for mx in (MARK_65, MARK_30):
        cv.poly([(mx - 2.2, 13.6), (mx + 2.2, 13.6), (mx + 0.8, 9.2), (mx - 0.8, 9.2)], "silver")
        cv.poly([(mx - 0.2, 13.6), (mx + 2.2, 13.6), (mx + 0.8, 9.2), (mx + 0.2, 9.2)], "silver_d")
        cv.line([(mx - 1.2, 13.2), (mx - 0.4, 9.8)], 0.5, "silver_l")


def haft_and_collar(cv: Canvas):
    # Bone & silver scythe collar at x=34..42
    cv.poly([(35, 7), (42, 9), (42, 33), (35, 35), (33, 31), (34.5, 21), (33, 11)], "bone_d")
    cv.poly([(35.8, 8.2), (41, 9.8), (41, 32.2), (35.8, 33.8), (34.2, 30.5), (35.6, 21), (34.2, 11.5)], "bone")
    cv.line([(36.2, 9), (36.8, 20)], 0.8, "bone_l")
    cv.ellipse(38, 21, 2.2, 2.5, "cyan_d")
    cv.ellipse(38, 21, 1.5, 1.8, "cyan")
    cv.ellipse(37.5, 20.4, 0.6, 0.7, "cyan_l")
    # Dark ash shaft wrapped in frost-silver bands
    cv.rect(12, 18, 34, 24, "slate_d")
    cv.rect(12, 18.7, 34, 23.3, "ash")
    for x in range(14, 34, 4):
        cv.line([(x, 18.2), (x + 1.8, 23.8)], 0.7, "silver_d")
    # Skull/lantern counterweight pommel on the left end
    cv.ellipse(8, 21, 5.4, 5.6, "bone_d")
    cv.ellipse(8, 21, 4.3, 4.5, "bone")
    cv.ellipse(7.2, 19.8, 1.5, 1.3, "bone_l")
    cv.ellipse(8.3, 21.2, 2.0, 2.1, "cyan_d")
    cv.ellipse(8.1, 21.0, 1.3, 1.4, "cyan")


def shroud_tassels(cv: Canvas):
    # Tattered frost-trimmed shroud strips hanging at left and right ends
    for x0, ln in ((48, 13), (230, 13)):
        top = 27.6
        cv.poly([
            (x0, top), (x0 + 9, top), (x0 + 9, top + ln),
            (x0 + 6.5, top + ln - 3.2), (x0 + 4.5, top + ln + 0.6),
            (x0 + 2.2, top + ln - 2.8), (x0, top + ln)
        ], "slate_d")
        cv.poly([
            (x0 + 0.8, top + 0.6), (x0 + 8.2, top + 0.6), (x0 + 8.2, top + ln - 1),
            (x0 + 6.5, top + ln - 3.8), (x0 + 4.5, top + ln - 0.5),
            (x0 + 2.2, top + ln - 3.4), (x0 + 0.8, top + ln - 1.2)
        ], "slate")
        cv.line([(x0 + 0.6, top + 1.2), (x0 + 8.4, top + 1.2)], 0.9, "silver")
        cv.line([(x0 + 2.2, top + 2.5), (x0 + 2.2, top + ln - 4)], 0.5, "slate_l")


def cowl_and_brooch(cv: Canvas):
    cx = W / 2
    # Pointed abyssal cowl hood with frost-silver rim sitting over the middle
    cv.poly([
        (cx - 11.5, 30), (cx - 11, 11), (cx - 6.5, 3.5), (cx, 0.8),
        (cx + 6.5, 3.5), (cx + 11, 11), (cx + 11.5, 30),
        (cx + 6.5, 33), (cx - 6.5, 33)
    ], "slate_d")
    cv.poly([
        (cx - 10.4, 29.2), (cx - 10, 11.5), (cx - 5.8, 4.6), (cx, 2.0),
        (cx + 5.8, 4.6), (cx + 10, 11.5), (cx + 10.4, 29.2),
        (cx + 5.8, 32.0), (cx - 5.8, 32.0)
    ], "slate")
    cv.line([(cx - 9.8, 11.5), (cx - 5.4, 4.8), (cx, 2.2), (cx + 5.4, 4.8), (cx + 9.8, 11.5)], 0.9, "silver")
    # Deep shadow cavity inside the hood
    cv.poly([
        (cx - 7.6, 22), (cx - 7.2, 12), (cx, 7.5), (cx + 7.2, 12), (cx + 7.6, 22), (cx, 24.5)
    ], "slate_dd")
    # Glowing crimson eyes inside the abyssal hood
    for sx in (-1, 1):
        cv.ellipse(cx + sx * 3.6, 15.6, 2.1, 1.3, "crimson_d")
        cv.ellipse(cx + sx * 3.6, 15.6, 1.4, 0.8, "crimson")
        cv.ellipse(cx + sx * 3.3, 15.4, 0.6, 0.4, "crimson_l")
    # Ancient silver spiral brooch at the base of the hood (y=26.5)
    cv.ellipse(cx, 26.8, 5.2, 5.2, "silver_d")
    cv.ellipse(cx, 26.8, 4.1, 4.1, "silver")
    cv.ellipse(cx, 26.8, 2.5, 2.5, "cyan_d")
    cv.ellipse(cx, 26.8, 1.5, 1.5, "cyan")
    cv.ellipse(cx - 0.5, 26.3, 0.6, 0.6, "cyan_l")


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
    for y in range(SLOT_Y - 1, SLOT_Y + SLOT_H + 1):
        for x in range(SLOT_X - 1, SLOT_X + SLOT_W + 1):
            inside = SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H
            px[x, y] = (0, 0, 0, 0) if inside else P["outline"] + (255,)
    out.alpha_composite(snap(face))
    px = out.load()
    ring = []
    for y in range(H):
        for x in range(W):
            if px[x, y][3] == 0 and any(
                0 <= x + dx < W
                and 0 <= y + dy < H
                and px[x + dx, y + dy][3] == 255
                and not (SLOT_X <= x < SLOT_X + SLOT_W and SLOT_Y <= y < SLOT_Y + SLOT_H)
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
            ):
                ring.append((x, y))
    for x, y in ring:
        px[x, y] = P["outline"] + (255,)
    return out


def main():
    cv = Canvas()
    shroud_tassels(cv)
    scythe_rail(cv)
    haft_and_collar(cv)
    face = Canvas()
    cowl_and_brooch(face)
    frame = finish(cv.img, face.img)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    frame.save(OUT)
    print("Wrote", OUT)


if __name__ == "__main__":
    main()

