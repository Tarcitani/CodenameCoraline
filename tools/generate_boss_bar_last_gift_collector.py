#!/usr/bin/env python3
"""
Themed boss-bar frame for Nicolas Noel, the Renegade (`codenamecoraline:last_gift_collector`).

Writes `assets/codenamecoraline/textures/gui/boss_bar/last_gift_collector.png` (256x48): antler ends, a gold rail with a
dark inset slot for the health fill (drawn by code), icicles, sleigh bells and a medallion with his face (hat, long nose,
beard). The geometry constants below must match `LastGiftCollectorBossBar` in `client/bossbar`.
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/last_gift_collector.png"

W, H = 256, 48
SLOT_X, SLOT_Y, SLOT_W, SLOT_H = 38, 19, 180, 6  # health fill area (kept in sync with the Java style)

OUTLINE = (26, 14, 18, 255)
BONE, BONE_D, BONE_L = (218, 204, 170, 255), (150, 130, 100, 255), (246, 238, 214, 255)
GOLD, GOLD_D, GOLD_L = (214, 170, 60, 255), (150, 110, 34, 255), (255, 226, 120, 255)
WINE, WINE_D = (96, 18, 34, 255), (58, 10, 22, 255)
RED, RED_D = (176, 28, 44, 255), (120, 16, 30, 255)
SKIN, SKIN_D, SKIN_L = (140, 164, 192, 255), (100, 122, 152, 255), (176, 198, 220, 255)
NOSE = (206, 104, 124, 255)
BEARD, BEARD_D = (226, 214, 176, 255), (166, 156, 118, 255)
ICE, ICE_D = (196, 234, 252, 255), (130, 190, 224, 255)
SLOT = (34, 14, 20, 255)
EYE = (255, 204, 70, 255)


def mirror_x(x: int) -> int:
    return W - 1 - x


def build() -> Image.Image:
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # ---- antlers (left drawn, right mirrored) -------------------------------------------------------------
    def antlers(draw: ImageDraw.ImageDraw, flip: bool) -> None:
        def p(pts):
            return [(mirror_x(x) if flip else x, y) for x, y in pts]
        beam = [(40, 21), (31, 22), (25, 17), (17, 14), (9, 9), (3, 3)]
        draw.line(p(beam), fill=BONE, width=3)
        draw.line(p([(x, y - 1) for x, y in beam]), fill=BONE_L, width=1)
        for tine in ([(25, 17), (23, 9), (22, 3)], [(17, 14), (13, 7), (12, 2)], [(9, 9), (5, 11), (2, 10)],
                     [(31, 22), (27, 30), (23, 35)], [(22, 24), (17, 30), (14, 36)]):
            draw.line(p(tine), fill=BONE, width=2)
            draw.line(p([tine[0], tine[1]]), fill=BONE_D, width=1)
    antlers(d, False)
    antlers(d, True)

    # ---- rail with the recessed health slot ---------------------------------------------------------------
    d.rectangle([SLOT_X - 3, SLOT_Y - 3, SLOT_X + SLOT_W + 2, SLOT_Y + SLOT_H + 2], fill=GOLD_D)
    d.rectangle([SLOT_X - 2, SLOT_Y - 2, SLOT_X + SLOT_W + 1, SLOT_Y + SLOT_H + 1], fill=GOLD)
    d.line([(SLOT_X - 2, SLOT_Y - 2), (SLOT_X + SLOT_W + 1, SLOT_Y - 2)], fill=GOLD_L)
    d.rectangle([SLOT_X - 1, SLOT_Y - 1, SLOT_X + SLOT_W, SLOT_Y + SLOT_H], fill=OUTLINE)
    d.rectangle([SLOT_X, SLOT_Y, SLOT_X + SLOT_W - 1, SLOT_Y + SLOT_H - 1], fill=SLOT)
    # fur trim along the rail: cream tufts on the top edge
    for x in range(SLOT_X - 2, SLOT_X + SLOT_W + 2, 3):
        d.point((x, SLOT_Y - 3), fill=BONE_L)
        d.point((x + 1, SLOT_Y - 4 if (x // 3) % 2 else SLOT_Y - 3), fill=BONE)
    # gold studs on the rail
    for x in (SLOT_X + 12, SLOT_X + 36, SLOT_X + SLOT_W - 36, SLOT_X + SLOT_W - 12):
        d.rectangle([x, SLOT_Y + SLOT_H + 1, x + 1, SLOT_Y + SLOT_H + 2], fill=GOLD_L)

    # ---- icicles -------------------------------------------------------------------------------------------
    for x, length in ((52, 6), (63, 9), (79, 5), (92, 7), (164, 7), (177, 5), (193, 9), (204, 6)):
        d.polygon([(x, SLOT_Y + SLOT_H + 3), (x + 3, SLOT_Y + SLOT_H + 3), (x + 1, SLOT_Y + SLOT_H + 3 + length)], fill=ICE)
        d.line([(x + 1, SLOT_Y + SLOT_H + 3), (x + 1, SLOT_Y + SLOT_H + 3 + length - 2)], fill=ICE_D)

    # ---- sleigh bells ----------------------------------------------------------------------------------------
    for bx in (98, 152):
        d.ellipse([bx, 27, bx + 5, 33], fill=GOLD)
        d.line([(bx + 1, 28), (bx + 1, 31)], fill=GOLD_L)
        d.line([(bx + 3, 33), (bx + 5, 33)], fill=GOLD_D)
        d.point((bx + 3, 34), fill=OUTLINE)

    # ---- medallion --------------------------------------------------------------------------------------------
    cx, cy, r = 128, 24, 19
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=GOLD_D)
    d.ellipse([cx - r + 1, cy - r + 1, cx + r - 1, cy + r - 1], fill=GOLD)
    d.ellipse([cx - r + 3, cy - r + 3, cx + r - 3, cy + r - 3], fill=WINE)
    d.ellipse([cx - r + 5, cy - r + 5, cx + r - 5, cy + r - 5], fill=WINE_D)
    # snowflake dots on the plate
    for px, py in ((113, 30), (143, 30), (115, 15), (141, 15)):
        d.point((px, py), fill=BONE_L)

    # beard (behind the face)
    d.polygon([(113, 26), (143, 26), (141, 37), (128, 47), (115, 37)], fill=BEARD)
    d.polygon([(128, 30), (141, 37), (128, 47)], fill=BEARD_D)
    for yy in range(31, 44, 3):
        d.line([(124, yy), (126, yy + 2)], fill=BEARD_D)
    # face
    d.rectangle([116, 16, 140, 27], fill=SKIN)
    d.rectangle([116, 24, 140, 27], fill=SKIN_D)
    d.line([(116, 16), (140, 16)], fill=SKIN_L)
    # angry brows + glowing amber eyes
    d.line([(119, 18), (123, 19)], fill=OUTLINE, width=1)
    d.line([(137, 18), (133, 19)], fill=OUTLINE, width=1)
    d.rectangle([120, 20, 122, 21], fill=EYE)
    d.rectangle([134, 20, 136, 21], fill=EYE)
    # long cold nose
    d.rectangle([127, 20, 129, 28], fill=SKIN_L)
    d.rectangle([127, 27, 129, 29], fill=NOSE)
    # drooping moustache over the beard
    d.polygon([(116, 26), (127, 25), (128, 29), (122, 31), (116, 29)], fill=BEARD)
    d.polygon([(140, 26), (129, 25), (128, 29), (134, 31), (140, 29)], fill=BEARD)
    # Santa hat with a fur brim and a pompom drooping to the right
    d.polygon([(113, 15), (143, 15), (136, 4), (126, 1)], fill=RED)
    d.polygon([(126, 1), (136, 4), (143, 15), (132, 15)], fill=RED_D)
    d.rectangle([111, 13, 145, 17], fill=BEARD)
    for xx in range(112, 145, 3):
        d.point((xx, 17), fill=BEARD_D)
    d.ellipse([139, 2, 146, 9], fill=BONE_L)
    d.point((141, 4), fill=(255, 255, 255, 255))

    # ---- crisp outline around everything -----------------------------------------------------------------------
    px = img.load()
    out = img.copy()
    opx = out.load()
    for y in range(H):
        for x in range(W):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < W and 0 <= ny < H and px[nx, ny][3] > 0:
                        opx[x, y] = OUTLINE
                        break
    return out


if __name__ == "__main__":
    OUT.parent.mkdir(parents=True, exist_ok=True)
    build().save(OUT)
    print("Generated", OUT)
