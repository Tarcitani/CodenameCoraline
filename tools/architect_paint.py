"""Pixel-art painters of the Architect's atlas (reform): resolution independent, so every box can be drawn at the
density its importance deserves (the face at 3 texels per model pixel, the hood, coat and torso at 2, limbs at 1.5).

Everything here works in texels of one UV island face: a painter receives the face name, the position (i, j) inside
the face and its size (fw, fh) and returns an RGBA tuple (or None to leave the texel clear). Light comes from the top
left, so top and left edges catch light and bottom and right edges fall into shadow (stepped, 4 tones per material).
Glow layers are written through the `glow(key, colour)` callback. Pixel art, not realism: clusters, hard steps, no gradients.
"""
import math

CLEAR = (0, 0, 0, 0)
FACE = ((67, 72, 82, 255), (110, 117, 130, 255), (155, 163, 176, 255), (195, 201, 210, 255))
GRANITE = ((73, 71, 79, 255), (119, 116, 122, 255), (170, 164, 160, 255), (201, 195, 188, 255))
BASALT = ((21, 21, 29, 255), (40, 40, 49, 255), (85, 81, 91, 255), (110, 106, 116, 255))
SLATE = ((30, 31, 40, 255), (52, 54, 66, 255), (92, 96, 110, 255), (128, 133, 148, 255))  # the coat: a blue-black stone
TABLET = ((110, 106, 103, 255), (161, 155, 145, 255), (199, 191, 176, 255), (214, 208, 196, 255))
CRACK = (30, 29, 37, 255)
DEEP = (16, 16, 21, 255)
VOID = (11, 11, 16, 255)
CORAL = (255, 96, 75, 255)
CORAL_D = (169, 31, 50, 255)
CORAL_HOT = (255, 208, 154, 255)
LILAC = (216, 200, 255, 255)
LILAC_D = (126, 100, 196, 255)
LILAC_L = (247, 242, 255, 255)
MASK_GLOW = (221, 244, 255, 255)
MANA = (56, 189, 186, 255)
MANA_D = (23, 106, 112, 255)
MANA_L = (182, 255, 240, 255)


# ------------------------------------------------------------------------------------------------ noise
def h2(x, y, seed):
    n = (x * 374761393 + y * 668265263 + seed * 2147483647 + 1442695040888963407) & 0xFFFFFFFFFFFFFFFF
    n = ((n ^ (n >> 29)) * 0xBF58476D1CE4E5B9) & 0xFFFFFFFFFFFFFFFF
    n = ((n ^ (n >> 32)) * 0x94D049BB133111EB) & 0xFFFFFFFFFFFFFFFF
    return ((n ^ (n >> 31)) & 0xFFFFFF) / float(0xFFFFFF)


def blob(i, j, seed, s):
    s = max(1, int(s))
    return h2(i // s, j // s, seed)


def level_of(i, j, seed, size=3, bias=0.0):
    """0..3 stone level from three octaves of blocky noise: shadow, base, light, chip highlight."""
    n = 0.52 * blob(i, j, seed, size) + 0.30 * blob(i + 2, j + 1, seed + 11, max(1, size - 1)) \
        + 0.18 * blob(i, j + 3, seed + 23, size * 2) + bias
    return 0 if n < 0.24 else 1 if n < 0.72 else 2 if n < 0.90 else 3


def bevel(level, i, j, fw, fh, depth=1):
    """Top and left edges catch the light, bottom and right edges fall into shadow."""
    if j < depth or i < depth and fw > 3:
        level += 1
    if j >= fh - depth or i >= fw - depth and fw > 3:
        level -= 1
    return max(0, min(3, level))


def worm(seed, fw, fh, start=None, steps=8, drift=(0, 1)):
    """A crack: a wandering line of texels, mostly following the drift direction."""
    x = int(h2(seed, 1, 5) * fw) if start is None else start[0]
    y = int(h2(seed, 2, 5) * (fh * 0.5)) if start is None else start[1]
    out = {(x, y)}
    for k in range(steps):
        r = h2(seed, k, 9)
        if r < 0.55:
            x += drift[0]
            y += drift[1]
        elif r < 0.78:
            x += 1
            y += drift[1]
        else:
            x -= 1
            y += drift[1]
        out.add((x, y))
    return out


class Face:
    """One face of a box: its size, a seed, and the cracks that cross it (precomputed so neighbours agree)."""

    def __init__(self, name, face, fw, fh, seed):
        self.name, self.face, self.fw, self.fh = name, face, fw, fh
        self.seed = seed
        self.side = face in ("front", "back", "left", "right")
        n = 1 + (1 if fw * fh > 120 else 0) + (1 if fw * fh > 600 else 0)
        self.cracks = set()
        for k in range(n):
            if h2(seed, k, 77) < 0.55:
                self.cracks |= worm(seed * 7 + k, fw, fh, steps=max(4, fh // 3))
        self.worn = {(int(h2(seed, k, 31) * fw), int(h2(seed, k, 32) * fh)) for k in range(max(1, fw * fh // 90))}


def stone(pal, f, i, j, size=3, bias=0.0, bev=1, crack=True, worn=True):
    """The base material: stepped noise, a bevelled edge, rare cracks and worn pits."""
    lv = level_of(i, j, f.seed, size, bias)
    if f.side:
        lv = bevel(lv, i, j, f.fw, f.fh, bev)
    elif f.face == "top":
        lv = max(lv, 2)
    else:
        lv = min(lv, 1)
    if crack and (i, j) in f.cracks and f.fw > 3:
        return CRACK
    if worn and (i, j) in f.worn and f.fw > 4:
        lv = max(0, lv - 1)
    return pal[lv]


def plates(pal, f, i, j, ph, pw=None, seed_shift=0, size=3, bias=0.0):
    """Stacked plates: horizontal seams every `ph` texels (a thin dark line), each plate a different tone."""
    row = j // ph
    tone = h2(row, f.seed, 41 + seed_shift)
    pbias = -0.12 if tone < 0.3 else 0.10 if tone > 0.72 else 0.0
    if j % ph == ph - 1 and f.side:
        return pal[0]
    if pw and i % pw == pw - 1 and f.side and (row % 2 == 0 or i % (pw * 2) == pw - 1):
        return pal[0]
    c = stone(pal, f, i, j, size, bias + pbias, 0)
    if j % ph == 0 and f.side:
        c = pal[min(3, pal.index(c) + 1)] if c in pal else c
    return c


# ------------------------------------------------------------------------------------------------ the face
def eye_hit(u, v, cx, cy, half_w, half_h, outward, tilt):
    dx = (u - cx) / half_w
    if abs(dx) > 1.0:
        return False
    dy = (v - cy) + tilt * dx * outward
    return abs(dy) <= half_h * (1.0 - abs(dx) ** 1.7)


def paint_face(f, i, j, glow):
    """The true face, normalised: hood shadow, black almond eyes with coral irises, hollow cheeks, a mask-like wide smile."""
    if f.face != "front":
        return stone(FACE, f, i, j, 2)
    fw, fh = f.fw, f.fh
    u, v = (i + 0.5) / fw, (j + 0.5) / fh
    base = stone(FACE, f, i, j, 2, bev=0, crack=False, worn=False)
    # shadow under the hood
    if v < 0.09:
        return VOID
    if v < 0.17:
        return FACE[0] if (i + j) % 2 == 0 or v < 0.13 else DEEP
    if v < 0.21 and (i + j) % 3 == 0:
        return FACE[0]
    eyes = ((0.265, 0.445, -1.0), (0.735, 0.445, 1.0))
    for cx, cy, side in eyes:
        outward = side
        if eye_hit(u, v, cx, cy, 0.168, 0.075, outward, 0.045):
            r = math.hypot((u - cx) / 0.060, (v - cy) / 0.068)
            if r <= 0.45:
                glow("glow", CORAL_HOT)
                glow("command", CORAL_HOT)
                return CORAL_HOT
            if r <= 1.0:
                glow("glow", CORAL)
                glow("command", CORAL)
                return CORAL if r <= 0.78 else CORAL_D
            return VOID  # the black sclera
        if eye_hit(u, v, cx, cy, 0.196, 0.108, outward, 0.045):
            return DEEP if v < cy else FACE[0]  # lids and the socket
        # the brow ridge: a heavy dark bar with a lit upper edge
        bx = (u - cx) * outward
        if -0.15 <= bx <= 0.20 and cy - 0.150 <= v + 0.04 * bx <= cy - 0.100:
            return FACE[0] if v + 0.04 * bx > cy - 0.138 else FACE[3]
    # the nose bridge sides (the nose itself is a box)
    if 0.435 <= u <= 0.470 and 0.40 <= v <= 0.66:
        return FACE[0]
    if 0.530 <= u <= 0.565 and 0.40 <= v <= 0.66:
        return FACE[0] if v < 0.62 else DEEP
    if 0.455 <= u <= 0.545 and 0.64 <= v <= 0.69:
        return DEEP  # the nostrils
    # cheekbones catch the light, the hollow below them sinks
    for cx in (0.155, 0.845):
        if abs(u - cx) <= 0.085 and 0.575 <= v <= 0.615:
            return FACE[3]
        if abs(u - cx) <= 0.105 and 0.625 <= v <= 0.700:
            return FACE[0]
    # smile creases: from beside the nose, around the cheek, up to the corners of the wide mouth
    for sgn in (-1, 1):
        t = (u - 0.5) * sgn
        if 0.115 <= t <= 0.43:
            arc = 0.68 + 0.115 * (1.0 - ((t - 0.43) / 0.315) ** 2) if t >= 0.115 else 0.0
            yy = 0.86 - (0.86 - 0.70) * ((0.43 - t) / 0.315) ** 0.8 if t < 0.43 else 0.7
            if abs(v - (0.70 + (0.43 - t) * 0.62)) <= 0.030:
                return CRACK
    # the corners of the mouth, pulled up
    for sgn in (-1, 1):
        t = (u - 0.5) * sgn
        if t >= 0.405 and 0.77 <= v <= 0.95 and abs(v - (0.94 - (t - 0.405) * 1.7)) <= 0.04:
            return CRACK
    if (i, j) in f.cracks and v > 0.22:
        return CRACK
    if (i, j) in f.worn:
        return FACE[0]
    return base


def paint_jaw(f, i, j, glow):
    """The front of the jaw: the dark mouth line, the marionette slits that make it a hinged piece, a lit chin edge."""
    if f.face != "front":
        return stone(FACE, f, i, j, 2)
    fw, fh = f.fw, f.fh
    u, v = (i + 0.5) / fw, (j + 0.5) / fh
    if v < 0.14:
        return VOID
    if v < 0.26 and 0.08 < u < 0.92:
        return CRACK  # the lower lip's shadow
    for cx in (0.115, 0.885):
        if abs(u - cx) <= 0.034 and v > 0.14:
            return DEEP  # marionette slits
        if abs(u - cx + (0.034 if cx < 0.5 else -0.034)) <= 0.020 and v > 0.3:
            return FACE[2]  # the lit rim beside each slit
    if 0.46 <= u <= 0.54 and v > 0.55:
        return FACE[0]  # chin cleft
    if v > 0.86:
        return FACE[2]
    c = stone(FACE, f, i, j, 2, bev=0, crack=True)
    return c


def paint_teeth(mat, f, i, j, glow):
    """Two rows of flat, rectangular teeth (never fangs): 3-texel teeth with 1-texel dark gaps; the lower row is offset."""
    if f.face != "front":
        return VOID
    shift = 0 if mat == "teeth_up" else 2
    if (i + shift) % 4 == 3:
        return CRACK
    top = mat == "teeth_up"
    lit = j == (0 if top else f.fh - 1)
    wear = h2(i // 4, 3 if top else 4, f.seed) < 0.25
    pal = TABLET
    if lit:
        return pal[2]
    return pal[0] if wear and j >= f.fh - 2 else pal[1]


def paint_seal(f, i, j, glow):
    """The forehead diamond: a lilac diamond in a stone frame."""
    if f.face != "front":
        return LILAC_D
    u, v = (i + 0.5) / f.fw, (j + 0.5) / f.fh
    inside = abs(u - 0.5) / 0.5 + abs(v - 0.5) / 0.5 <= 1.0
    if not inside:
        return stone(FACE, f, i, j, 2)
    glow("glow", LILAC)
    glow("command", LILAC)
    glow("waking", LILAC)
    return LILAC_D if abs(u - 0.5) + abs(v - 0.5) > 0.22 else LILAC_L


def paint_mask(mat, f, i, j, glow):
    """The serene half masks (inert stone): closed eye, calm half smile, a jagged break down the middle."""
    c = stone(TABLET, f, i, j, 2, bev=1)
    if f.face != "front":
        return TABLET[0]
    left = mat == "mask_left"
    centre = (f.fw - 1 - i) if left else i
    inner = f.fw - 1 if left else 0
    u, v = (i + 0.5) / f.fw, (j + 0.5) / f.fh
    cx = abs((centre + 0.5) / f.fw)
    if i == inner and (j // 2) % 3 == 1:
        c = CRACK
    if 0.19 <= v <= 0.24 and 0.2 <= cx <= 0.78:
        c = TABLET[0]  # the closed lid
    if 0.24 < v <= 0.28 and 0.2 <= cx <= 0.78:
        c = TABLET[2]
    if 0.72 <= v <= 0.78 and cx <= 0.62 and cx >= 0.05:
        c = TABLET[0]  # the calm half smile
    if 0.30 <= v <= 0.62 and cx <= 0.08:
        c = TABLET[2]  # the nose ridge
    glow("waking", MASK_GLOW if c != CRACK else LILAC)
    return c


# ------------------------------------------------------------------------------------------------ the body
def paint_ribcage(f, i, j, glow):
    """Rib plates, a lit sternum channel and mana veins running from the sternum to the six shoulder sockets."""
    base = stone(GRANITE, f, i, j, 3, bias=-0.04)
    if f.face == "front":
        fw, fh = f.fw, f.fh
        u, v = (i + 0.5) / fw, (j + 0.5) / fh
        # horizontal rib grooves on the sides of the chest
        if (u < 0.28 or u > 0.72) and (j % max(3, fh // 10)) == max(3, fh // 10) - 1:
            base = GRANITE[0]
        # the veins: right-angled circuit traces to the top, middle and low sockets
        for row, w in ((0.20, 1), (0.60, 1), (0.88, 1)):
            rj = int(row * fh)
            if abs(j - rj) < w and (u < 0.40 or u > 0.60) and 0.03 <= u <= 0.97:
                glow("command", MANA)
                if row == 0.60:
                    glow("glow", MANA)
                return MANA_D
        for ux in (0.40, 0.60):
            if abs(u - ux) * fw < 0.9 and 0.20 <= v <= 0.88:
                glow("command", MANA)
                return MANA_D
        # inscriptions: tiny glyph blocks in two columns, discreet
        if 0.06 <= u <= 0.30 and (j % 5 in (1, 2)) and h2(i // 2, j // 5, f.seed) > 0.55 and 0.1 < v < 0.9:
            return GRANITE[0]
        if 0.70 <= u <= 0.94 and (j % 5 in (1, 2)) and h2(i // 2, j // 5, f.seed + 4) > 0.55 and 0.1 < v < 0.9:
            return GRANITE[0]
    return base


def paint_abdomen(f, i, j, glow):
    c = stone(GRANITE, f, i, j, 3, bev=1)
    if f.face == "front" and f.fw > 4 and i in (1, f.fw - 2) and 1 < j < f.fh - 1:
        glow("glow", MANA)
        glow("command", MANA)
        return MANA_D
    return c


def paint_mount(mat, f, i, j, glow):
    if mat == "mount_disc" and f.face in ("left", "right", "front"):
        cx, cy = (f.fw - 1) / 2.0, (f.fh - 1) / 2.0
        r = max(abs(i - cx) / max(1.0, cx), abs(j - cy) / max(1.0, cy))
        if 0.62 <= r < 0.82:
            glow("glow", MANA)
            glow("command", MANA)
            return MANA_D
        if r < 0.62:
            return DEEP
    c = stone(GRANITE, f, i, j, 3, bias=-0.02)
    if mat == "mount" and f.side and j % 5 == 0:
        return GRANITE[0]
    return c


def paint_sternum(f, i, j, glow):
    """The sternum seal: three stacked lilac diamonds (circuit nodes) in a granite frame."""
    if f.face != "front":
        return GRANITE[1]
    u, v = (i + 0.5) / f.fw, (j + 0.5) / f.fh
    for cy, strong in ((0.2, False), (0.5, True), (0.8, False)):
        d = abs(u - 0.5) / 0.38 + abs(v - cy) / 0.14
        if d <= 1.0:
            glow("command", LILAC)
            if strong:
                glow("glow", LILAC_D)
            return LILAC if d <= 0.45 else LILAC_D
    if abs(u - 0.5) < 0.05 and 0.1 < v < 0.9:
        glow("command", MANA)
        return MANA_D
    if i in (0, f.fw - 1) or j in (0, f.fh - 1):
        return GRANITE[0]
    return stone(GRANITE, f, i, j, 2, bev=0)


def paint_limb(mat, f, i, j, glow, name):
    """Arms, hands, legs and feet: granite with scratches, chips, a lit crest; every part its own seed."""
    if mat == "elbow" or mat == "wrist_cracked":
        c = stone(GRANITE, f, i, j, 2, bias=-0.10)
        if f.side and j in (0, f.fh - 1):
            return CRACK
        if f.side and f.face == "front" and j == 1 and i % 6 == 2:
            glow("command", MANA)
            return MANA_D
        if mat == "wrist_cracked" and f.side and i == f.fw // 2:
            return DEEP
        return c
    if mat in ("forearm", "forearm_chipped"):
        c = stone(GRANITE, f, i, j, 3)
        if f.side and (i + 2 * j) % 7 == 0 and h2(i, j, f.seed) > 0.4:
            c = GRANITE[0]  # diagonal scratches
        if mat == "forearm_chipped" and f.face == "front" and 2 <= j <= 7 and i == f.fw // 2:
            glow("glow", MANA)
            glow("command", MANA)
            return MANA_D
        return c
    if mat == "hand":
        return GRANITE[1] if h2(i // 2, j // 2, f.seed) > 0.3 else GRANITE[0]
    if mat == "fingers":
        return GRANITE[0] if i % 2 == 1 else (GRANITE[3] if j == f.fh - 1 else GRANITE[2])
    if mat == "granite_light":
        c = stone(GRANITE, f, i, j, 3, bias=0.16)
        return GRANITE[3] if f.side and j < 1 else c
    return stone(GRANITE, f, i, j, 3)


def paint_blade(mat, f, i, j, glow):
    """Black basalt blades: a lighter cutting edge on the -Z side, a central mana groove, a thick irregular spine."""
    if mat == "grip":
        return BASALT[0] if (j // 1) % 2 else BASALT[1]
    if mat == "basalt_light":
        return stone(BASALT, f, i, j, 2, bias=0.10)
    c = stone(BASALT, f, i, j, 2, bias=-0.05)
    if mat == "blade_tip":
        c = BASALT[2] if f.face == "top" else c
        if f.face in ("left", "right") and i == 0:
            return BASALT[3]
        return c
    if f.face in ("left", "right"):
        if i == 0:
            return BASALT[3]  # the edge
        if i == f.fw // 2 and 1 <= j <= f.fh - 2:
            if j % 3 != 2:
                glow("glow", MANA)
            return MANA_D if j % 3 != 2 else BASALT[0]  # the mana groove
        if i == f.fw - 1:
            return BASALT[0]  # the spine
    return c


def paint_wing(mat, f, i, j, glow):
    if mat == "wing_bone":
        c = stone(GRANITE, f, i, j, 3, bias=0.02)
        return FACE[2] if f.face == "top" else c
    if mat == "wing_plate":
        c = plates(BASALT, f, i, j, 5, 6, 3, 2, 0.12)
        if j >= f.fh - 2:
            return BASALT[0]
        if (i + j) % 9 == 0 and h2(i, j, f.seed) > 0.5:
            return CRACK
        return c
    if mat == "wing_tip":
        return BASALT[0] if (i + j) % 5 else BASALT[1]
    return stone(GRANITE, f, i, j, 3)


def paint_tablet(name, f, i, j, glow):
    if f.face == "front" and not name.startswith("tablet_top"):
        if (j % 6) in (2, 3) and 1 <= i <= f.fw - 2 and h2(i // 2, j // 6, f.seed) > 0.35:
            glow("waking", LILAC)
            return TABLET[0]  # three lines of glyphs
        inner = f.fw - 1 if name.endswith("left") else 0
        if i == inner and j % 2 == 0:
            return CRACK
    c = stone(TABLET, f, i, j, 3, bev=1)
    if (i in (0, f.fw - 1) or j in (0, f.fh - 1)) and h2(i, j, f.seed) < 0.3:
        return (98, 93, 92, 255)  # mineral moss on the corners
    return c


# ------------------------------------------------------------------------------------------------ the coat
def glyph(f, i, j, cell_w, cell_h, thr, seed_shift=0):
    """Discreet engraved inscriptions: a grid of cells, each a hashed 3x5 glyph, drawn as a slightly darker mark."""
    ci, cj = i // cell_w, j // cell_h
    if h2(ci, cj, f.seed + seed_shift) < thr:
        return False
    gi, gj = i % cell_w, j % cell_h
    if gi == 0 or gj == 0 or gi >= cell_w - 1 or gj >= cell_h - 1:
        return False
    return h2(ci * 7 + gi, cj * 11 + gj, f.seed + 3 + seed_shift) > 0.45


def paint_coat(mat, name, f, i, j, glow):
    """The long stone coat: dark slate panels, a raised lit rim, recessed centres with engraved inscriptions, chipped hems."""
    fw, fh = f.fw, f.fh
    if not f.side:
        return SLATE[2] if f.face == "top" else SLATE[0]
    c = stone(SLATE, f, i, j, 3, bias=-0.04, bev=0)
    rim = 2
    on_rim = i < rim or i >= fw - rim or j < rim or j >= fh - rim
    if mat in ("coat_trim", "collar"):
        c = stone(GRANITE, f, i, j, 2, bias=-0.06, bev=1)
        if on_rim and fw > 6 and fh > 6:
            return GRANITE[2] if (i < rim or j < rim) else GRANITE[0]
        return c
    if on_rim and fw > 8 and fh > 8:
        return SLATE[3] if (i < rim or j < rim) else SLATE[0]
    if f.face in ("front", "back") and fw > 10 and fh > 10:
        if glyph(f, i - rim, j - rim, 5, 7, 0.55):
            c = SLATE[0]
            if h2(i, j, f.seed + 9) > 0.93:
                glow("glow", LILAC_D)
                return LILAC_D
        # a vertical fold line down the middle of the long panels
        if i == fw // 2 and 3 < j < fh - 3 and h2(i, j // 4, f.seed) > 0.3:
            return SLATE[0]
    if mat == "coat_hem" and j >= fh - 2:
        return SLATE[0]
    return c
