"""Greataxe art: 16x16 inventory icon (auto-outlined pixel art) + 3D item model (JSON elements + swatch texture)."""
import json, math
from PIL import Image

OUT_ITEM = r'C:\MinecraftDev\CodenameCoraline\src\main\resources\assets\codenamecoraline\textures\item'
OUT_MODEL = r'C:\MinecraftDev\CodenameCoraline\src\main\resources\assets\codenamecoraline\models\item'

OUTLINE = (28, 22, 30, 255)
WOOD, WOOD_L, WOOD_D = (110, 74, 44, 255), (150, 106, 64, 255), (72, 46, 30, 255)
STEEL, STEEL_L, STEEL_D, STEEL_DD = (150, 164, 178, 255), (226, 236, 244, 255), (96, 108, 124, 255), (64, 74, 90, 255)
GOLD, GOLD_L, GOLD_D = (214, 170, 60, 255), (255, 226, 120, 255), (150, 110, 34, 255)
GLOW, GLOW_L = (90, 190, 240, 255), (190, 240, 255, 255)


def icon():
    cells = {}
    # haft: diagonal, two pixels wide
    for i in range(0, 12):
        x, y = 2 + i, 14 - i
        cells[(x, y)] = WOOD_L if i % 2 == 0 else WOOD
        cells[(x + 1, y)] = WOOD_D if i % 3 else WOOD
    # leather wrap near the grip
    for i in (2, 3):
        x, y = 2 + i, 14 - i
        cells[(x, y)] = GOLD_D
        cells[(x + 1, y)] = GOLD_D
    # pommel
    for c in ((1, 15), (2, 15), (1, 14), (2, 14)):
        cells[c] = GOLD
    cells[(1, 15)] = GOLD_D
    # blade: a big crescent to the upper left of the haft top
    for y in range(0, 10):
        for x in range(1, 12):
            inside = ((x - 6.6) / 5.3) ** 2 + ((y - 4.0) / 4.2) ** 2 <= 1.0
            bite = (x - 10.2) ** 2 + (y - 7.4) ** 2 < 3.1 ** 2
            if inside and not bite:
                d = (x - 6.6) + (y - 4.0) * 0.7
                cells[(x, y)] = STEEL_L if d < -3.6 else STEEL if d < 0.6 else STEEL_D if d < 2.8 else STEEL_DD
    # frost runes
    for c in ((5, 3), (6, 4), (5, 5)):
        if c in cells:
            cells[c] = GLOW
    cells[(6, 4)] = GLOW_L
    # collar where blade meets haft
    for c in ((10, 5), (11, 5), (10, 6), (11, 6), (12, 5), (12, 4)):
        cells[c] = GOLD if c[0] != 12 else GOLD_L
    cells[(10, 6)] = GOLD_D
    # back spike
    for c in ((13, 3), (14, 2), (13, 4), (12, 3), (14, 3)):
        cells[c] = STEEL if c[1] >= 3 else STEEL_L
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()
    for (x, y), c in cells.items():
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = c
    # outline: any empty cell touching a filled cell (4-neighbourhood)
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) in cells:
                        px[x, y] = OUTLINE
                        break
    return img


# ---------- 3D model ----------
SW = {  # name -> (u0, v0) of a 4x4 swatch in the 16x16 palette texture
    'wood': (0, 0), 'wood_l': (4, 0), 'steel': (8, 0), 'steel_d': (12, 0),
    'gold': (0, 4), 'glow': (4, 4), 'leather': (8, 4), 'edge': (12, 4),
}


def swatch_texture():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
    px = img.load()

    def fill(name, base, light, dark):
        u0, v0 = SW[name]
        for j in range(4):
            for i in range(4):
                n = (i * 7 + j * 13 + u0 + v0) % 5
                px[u0 + i, v0 + j] = dark if n == 0 else light if n == 4 else base
    fill('wood', WOOD, WOOD_L, WOOD_D)
    fill('wood_l', WOOD_L, (190, 140, 90, 255), WOOD)
    fill('steel', STEEL, STEEL_L, STEEL_D)
    fill('steel_d', STEEL_D, STEEL, STEEL_DD)
    fill('gold', GOLD, GOLD_L, GOLD_D)
    fill('glow', GLOW, GLOW_L, (60, 140, 200, 255))
    fill('leather', (48, 38, 40, 255), (84, 66, 62, 255), (30, 24, 28, 255))
    fill('edge', STEEL_L, (255, 255, 255, 255), STEEL)
    return img


def model3d():
    # Upright axe (haft along y), then every element is rotated 45 degrees about z through (8,8,8): the same diagonal
    # as a vanilla tool sprite, so the stock handheld display transforms hold it correctly. Blade faces -x.
    parts = [
        ([7, -6, 7], [9, 24, 9], 'wood'),            # haft
        ([6.6, -7.5, 6.6], [9.4, -5, 9.4], 'gold'),  # pommel cap
        ([6.7, -2, 6.7], [9.3, 4, 9.3], 'leather'),  # grip wrap
        ([6.4, 19.5, 6.4], [9.6, 24.5, 9.6], 'gold'),  # collar
        ([0.2, 16, 7.2], [7, 27, 8.8], 'steel'),     # blade body
        ([-2.6, 17.2, 7.5], [0.2, 26, 8.5], 'edge'),   # bevelled cutting edge
        ([0.8, 13, 7.4], [4.2, 16, 8.6], 'steel_d'),   # lower toe
        ([1.6, 27, 7.4], [5.2, 29, 8.6], 'steel_d'),   # upper horn
        ([9, 19.5, 7.3], [13.5, 23.5, 8.7], 'steel'),  # back spike
        ([13.5, 20.5, 7.5], [15.5, 22.5, 8.5], 'steel_d'),  # spike tip
        ([2.2, 20.5, 8.7], [4.2, 22.5, 8.9], 'glow'),  # frost rune (front)
        ([2.2, 20.5, 7.1], [4.2, 22.5, 7.3], 'glow'),  # frost rune (back)
    ]
    elements = []
    for (a, b, mat) in parts:
        u, v = SW[mat]
        face = {"uv": [u, v, u + 4, v + 4], "texture": "#0"}
        elements.append({
            "from": a, "to": b,
            "rotation": {"angle": -45, "axis": "z", "origin": [8, 8, 8]},
            "faces": {k: dict(face) for k in ("north", "south", "east", "west", "up", "down")},
        })
    big = lambda s: [s, s, s]
    return {
        "textures": {"0": "codenamecoraline:item/renegade_greataxe_3d", "particle": "codenamecoraline:item/renegade_greataxe_3d"},
        "elements": elements,
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": big(1.0)},
            "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": big(1.0)},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": big(0.8)},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": big(0.8)},
            "gui": {"rotation": [30, 225, 0], "scale": big(0.5)},
            "ground": {"translation": [0, 3, 0], "scale": big(0.55)},
            "fixed": {"rotation": [0, 90, 0], "scale": big(0.6)},
        },
    }


if __name__ == '__main__':
    icon().save(OUT_ITEM + r'\renegade_greataxe.png')
    swatch_texture().save(OUT_ITEM + r'\renegade_greataxe_3d.png')
    with open(OUT_MODEL + r'\renegade_greataxe_3d.json', 'w', encoding='utf-8', newline='\n') as fh:
        json.dump(model3d(), fh, indent=2)
        fh.write('\n')
    print('ok')
