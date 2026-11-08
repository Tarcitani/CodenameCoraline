r"""Generates every asset of the Architect's Scimitar and the Architect portrait (spec docs/design/architect.md §26.5).

Run: python tools/generate_architect_scimitar_assets.py
Out: textures/item/architect_scimitar{,_charged,_3d}.png (the 3D atlas is 64x64 since the reform), textures/item/architect_portrait.png,
     models/item/architect_scimitar{,_charged,_3d,_3d_raised,_3d_charged,_3d_dim}.json, models/item/architect_portrait.json,
     items/architect_scimitar.json, items/architect_portrait.json

This script is the single source of the scimitar's geometry. Reform: the 3D model is a curved scimitar of four chained
blade segments (each bent more than the last), a thick uneven spine, a lit edge, a recessed groove with the glowing mana,
a faceted pommel, a ringed grip and a stone guard with upturned tips. The whole sword is laid on the item diagonal
(-45 degrees about Z) so the vanilla handheld display transforms hold it as they hold a sword. The item definition uses
the 2D icon only in the inventory (gui); hands, the ground, frames and shelves all use the 3D model.
"""
import json
import math
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/codenamecoraline"
TEX = ASSETS / "textures/item"
MODELS = ASSETS / "models/item"
ITEMS = ASSETS / "items"

# Palette §9.3 / §26.5
SHADOW = (0x15, 0x15, 0x1D, 255)
BASE = (0x28, 0x28, 0x31, 255)
LIGHT = (0x55, 0x51, 0x5B, 255)
EDGE = (0x9B, 0xA3, 0xB0, 255)
GROOVE = (0x38, 0xBD, 0xBA, 255)
SPARK = (0xB6, 0xFF, 0xF0, 255)
GROOVE_DIM = (0x24, 0x23, 0x2B, 255)
RUNE = (0xD8, 0xC8, 0xFF, 255)
GRIP_DARK = (0x1E, 0x1A, 0x1F, 255)
GRIP_LIGHT = (0x3B, 0x35, 0x40, 255)
CLEAR = (0, 0, 0, 0)


# ============================================================ 16x16 inventory icon
ICON = [
    "..............LE",
    ".............SBE",
    "............SBLE",
    "...........SBGE.",
    "..........SBGLE.",
    ".........SBGBE..",
    "........SBPBE...",
    ".......SBGBE....",
    "......SBGLE.....",
    "...L.SRBBE......",
    "....LSBEE.......",
    ".....HLL........",
    "....DH.L........",
    "...HD...........",
    "..LS............",
    "..SS............",
]
ICON_COLORS = {"S": SHADOW, "B": BASE, "L": LIGHT, "E": EDGE, "G": GROOVE, "P": SPARK, "R": RUNE,
               "D": GRIP_DARK, "H": GRIP_LIGHT}


def icon(charged: bool) -> Image.Image:
    """Pommel bottom-left, tip top-right, the edge (light) on the convex lower-right side; groove and rune glow."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y, row in enumerate(ICON):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            c = ICON_COLORS[ch]
            if charged and ch in "GP":
                c = SPARK if ch == "G" else (0xE6, 0xFF, 0xFA, 255)
            if charged and ch == "R":
                c = (0xF0, 0xE8, 0xFF, 255)
            px[x, y] = c
    return img


# ============================================================ 16x16 portrait (advancement icon)
def portrait() -> Image.Image:
    rows = [
        "....SSSSSSSS....",
        "...SBBBBBBBBS...",
        "..SBBLLLLLLBBS..",
        "..SBLKKKKKKLBS..",
        ".SBLK......KLBS.",
        ".SBK.FFFFFF.KBS.",
        ".SBK.FWCFCWF.KS.",
        ".SBK.FFFDFFF.KS.",
        ".SBK.FFFFFFF.KS.",
        ".SBK.FTTTTTF.KS.",
        ".SBK.FMMMMMF.KS.",
        ".SBBK.FFFFF.KBS.",
        "..SBBK.....KBS..",
        "...SBBBBBBBBS...",
        "....SSSSSSSS....",
        "................",
    ]
    colors = {
        "S": SHADOW, "B": BASE, "L": LIGHT, "K": (0x0B, 0x0B, 0x10, 255), ".": (0x0B, 0x0B, 0x10, 255),
        "F": (0x6E, 0x75, 0x82, 255), "W": (0x0B, 0x0B, 0x10, 255), "C": (0xFF, 0x60, 0x4B, 255),
        "D": RUNE, "T": (0xD9, 0xD2, 0xC4, 255), "M": (0x24, 0x23, 0x2B, 255),
    }
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "." and (x < 2 or x > 13 or y > 13 or y < 1):
                continue  # transparent outside the hood
            px[x, y] = colors[ch]
    # keep the outer corners clear
    for x, y in [(0, 4), (15, 4), (0, 11), (15, 11), (1, 12), (14, 12), (2, 13), (13, 13), (3, 14), (12, 14)]:
        if px[x, y] == colors["."]:
            px[x, y] = CLEAR
    return img


# ============================================================ 64x64 atlas of the 3D model (reform)
# The item model is built in an UPRIGHT frame (pommel at the bottom, blade up +Y, the edge on +X, the blade a flat plate in
# the XY plane, 1 unit thick in Z) and laid on the item diagonal (-45 degrees about Z) like the vanilla sword icons, so the
# vanilla handheld transforms hold it as they hold a sword. 4 texels per model unit (64 px = 16 units).
PX = 4
SW = {  # name: (x, y, w, h) in texels
    "blade0": (0, 0, 14, 14), "blade1": (0, 14, 14, 14), "blade2": (0, 28, 14, 14), "blade3": (0, 42, 14, 12),
    "edge": (14, 0, 4, 14), "spine": (18, 0, 4, 14), "groove": (22, 0, 2, 14), "groove_bright": (24, 0, 2, 14),
    "groove_dim": (26, 0, 2, 14), "stone": (28, 0, 12, 12), "grip": (40, 0, 6, 12), "rune": (46, 0, 4, 4),
    "rune_lit": (50, 0, 4, 4), "cap": (54, 0, 4, 4), "stone_dark": (28, 12, 12, 12), "ring": (40, 12, 8, 4),
}


def blade_tile(w, h, seed, tip):
    """A side of the blade, drawn: black basalt in stepped bands, a lit cutting edge on the right, a thick darker
    spine on the left, a recessed channel down the middle for the mana groove, a crack or two."""
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            n = ((x * 73 + y * 151 + seed * 37) % 97) / 97.0
            c = BASE
            if n > 0.86:
                c = LIGHT
            elif n < 0.16:
                c = SHADOW
            if y % 5 == 4 and n < 0.5:  # horizontal strata
                c = SHADOW
            if x >= w - 1:
                c = EDGE  # the sharpened edge
            elif x == w - 2:
                c = (0x70, 0x76, 0x84, 255)
            elif x == w - 3:
                c = LIGHT
            if x <= 1:
                c = SHADOW if x == 0 else (0x1D, 0x1D, 0x26, 255)  # the spine
            if w // 2 - 1 <= x <= w // 2:
                c = (0x10, 0x10, 0x16, 255)  # the channel the groove sits in
            px[x, y] = c
    # a crack across the lower third and a chipped notch on the edge
    for k in range(5):
        cx, cy = 3 + k, h - 5 + (k % 2)
        if cx < w // 2 - 1:
            px[cx, cy] = SHADOW
    if tip:
        for y in range(h):
            for x in range(w):  # the very tip narrows: clear the corner outside the point
                if x > (w - 1) - (y * 0.5):
                    pass
    return img


def atlas() -> Image.Image:
    img = Image.new("RGBA", (64, 64), CLEAR)

    def put(name, tile):
        x0, y0, _, _ = SW[name]
        img.paste(tile, (x0, y0))

    for i in range(4):
        w, h = SW[f"blade{i}"][2:]
        put(f"blade{i}", blade_tile(w, h, i + 1, i == 3))

    def fill(name, fn):
        x0, y0, w, h = SW[name]
        for y in range(h):
            for x in range(w):
                img.putpixel((x0 + x, y0 + y), fn(x, y, w, h))
    fill("edge", lambda x, y, w, h: EDGE if x >= w - 2 else (0x70, 0x76, 0x84, 255) if x == w - 3 else LIGHT)
    fill("spine", lambda x, y, w, h: SHADOW if x < 2 else (0x1D, 0x1D, 0x26, 255) if (x + y // 3) % 3 else BASE)
    fill("groove", lambda x, y, w, h: SPARK if y % 5 == 2 else GROOVE)
    fill("groove_bright", lambda x, y, w, h: (0xE6, 0xFF, 0xFA, 255) if y % 3 == 0 else SPARK)
    fill("groove_dim", lambda x, y, w, h: GROOVE_DIM)
    # carved stone for the guard and the pommel: facets, a lit top-left, a shadowed bottom-right, mineral flecks
    fill("stone", lambda x, y, w, h: EDGE if (x < 2 and y < 8) or y == 0 else SHADOW if x > w - 3 or y > h - 3
         else LIGHT if (x * 5 + y * 3) % 11 == 0 else BASE)
    fill("stone_dark", lambda x, y, w, h: SHADOW if x > w - 3 or y > h - 3 else BASE if (x + y) % 4 else (0x1D, 0x1D, 0x26, 255))
    fill("grip", lambda x, y, w, h: GRIP_LIGHT if (y + x) % 4 == 0 else GRIP_DARK)
    fill("ring", lambda x, y, w, h: EDGE if y == 0 else LIGHT if y == 1 else BASE)
    fill("rune", lambda x, y, w, h: RUNE)
    fill("rune_lit", lambda x, y, w, h: (0xF0, 0xE8, 0xFF, 255))
    fill("cap", lambda x, y, w, h: BASE)
    return img


def uv(name, w_units=None, h_units=None, pad=0.0):
    """UV rectangle (model units 0..16) of a swatch, cropped to a face of w x h units at 4 texels per unit."""
    x0, y0, w, h = SW[name]
    cw = w if w_units is None else min(w, max(1, round(w_units * PX)))
    ch = h if h_units is None else min(h, max(1, round(h_units * PX)))
    return [x0 / PX, y0 / PX, (x0 + cw) / PX, (y0 + ch) / PX]


# Geometry --------------------------------------------------------------------------------------------------------------
C = (8.0, 8.0)  # the whole sword turns -45 degrees about Z around this point to lie on the item diagonal
TILT = -45.0
SHIFT = 2.0  # the sword is 20 units long: this centres it on the diagonal (pommel 0.8 .. tip 15.2 on both axes)
Z0, Z1 = 7.5, 8.5  # the plate: 1 unit thick


def rot(p, deg, o):
    a = math.radians(deg)
    x, y = p[0] - o[0], p[1] - o[1]
    return (o[0] + x * math.cos(a) - y * math.sin(a), o[1] + x * math.sin(a) + y * math.cos(a))


def element(name, frm, to, tex, *, pivot=None, theta=0.0, emission=0, faces=None, y_rot=None):
    """A cube of the upright frame, optionally bent by `theta` degrees about `pivot` (x, y), then laid on the diagonal.
    One rotation about Z: -45 + theta about the moved pivot (compose two planar rotations)."""
    pv = pivot if pivot is not None else (8.0, (frm[1] + to[1]) / 2.0)
    pv = (pv[0], pv[1] + SHIFT)
    frm = [frm[0], frm[1] + SHIFT, frm[2]]
    to = [to[0], to[1] + SHIFT, to[2]]
    moved = rot(pv, TILT, C)
    d = (moved[0] - pv[0], moved[1] - pv[1])
    frm = [frm[0] + d[0], frm[1] + d[1], frm[2]]
    to = [to[0] + d[0], to[1] + d[1], to[2]]
    rotation = {"origin": [round(moved[0], 4), round(moved[1], 4), 8], "z": round(TILT + theta, 4)}
    if y_rot is not None:  # a faceted piece: a 45 degree twist about its own vertical axis (applied before the tilt)
        rotation = {"origin": [round(moved[0], 4), round(moved[1], 4), 8], "x": 0.0, "y": y_rot, "z": round(TILT + theta, 4)}
    e = {"name": name, "from": [round(v, 4) for v in frm], "to": [round(v, 4) for v in to], "rotation": rotation,
         "faces": faces}
    if emission:
        e["light_emission"] = emission
    return e


def box_faces(name, w, h, *, only=None, top=None):
    """Faces of a w x h unit plate: the sides show the swatch at 4 texels per unit, the thin ends a flat tone."""
    out = {}
    whole = name.startswith("blade")
    for d in ("north", "south"):
        if only is None or d in only:
            rect = uv(name) if whole else uv(name, w, h)
            if d == "north":
                rect = [rect[2], rect[1], rect[0], rect[3]]  # seen from -Z the texture's right is -X: mirror it
            out[d] = {"uv": rect, "texture": "#0"}
    for d in ("east", "west", "up", "down"):
        if only is None or d in only:
            out[d] = {"uv": uv(top or "stone", 1, 1), "texture": "#0"}
    return out


# blade: four chained segments, each bent a little more than the last (a scimitar's curve), widths taper to the point
SEGMENTS = [  # (length, half width, bend of this segment in degrees, spine thickness)
    (3.4, 1.6, 0.0, 1.6), (3.3, 1.5, 4.0, 1.4), (3.2, 1.35, 8.0, 1.6), (3.0, 1.1, 12.0, 1.2),
]  # 24 degrees of curve in all by the point (a first try with 47 read as a crescent moon, not a scimitar)
OVERLAP = 0.25  # each segment runs this much into the next so the bend leaves no gap on the convex side
BLADE_Y0 = 3.4


def hand_model(groove_tex, groove_light, rune_tex, rune_light):
    els = []
    # pommel: a faceted stone gem (a cube twisted 45 degrees), a cap below, and a lit ring above the grip
    els.append(element("pommel", [7.1, -3.6, 7.1], [8.9, -1.8, 8.9], "stone", faces=box_faces("stone", 1.8, 1.8), y_rot=45.0))
    els.append(element("pommel_cap", [7.5, -4.3, 7.5], [8.5, -3.6, 8.5], "cap", faces=box_faces("stone_dark", 1.0, 0.7)))
    els.append(element("grip_lower", [7.4, -1.8, 7.4], [8.6, 0.3, 8.6], "grip",
                       faces=box_faces("grip", 1.2, 2.1, top="grip")))
    els.append(element("grip_ring", [7.1, 0.3, 7.1], [8.9, 0.8, 8.9], "ring", faces=box_faces("ring", 1.8, 0.5)))
    els.append(element("grip_upper", [7.4, 0.8, 7.4], [8.6, 2.6, 8.6], "grip",
                       faces=box_faces("grip", 1.2, 1.8, top="grip")))
    # the guard: a stone bar with a centre boss and an upturned stone tip at each end
    els.append(element("guard", [5.8, 2.6, 7.2], [10.2, 3.5, 8.8], "stone", faces=box_faces("stone_dark", 4.4, 0.9)))
    els.append(element("guard_boss", [7.2, 2.4, 6.9], [8.8, 3.8, 9.1], "stone", faces=box_faces("stone", 1.6, 1.4)))
    for side, x0, x1, th in (("left", 5.3, 6.1, 28.0), ("right", 9.9, 10.7, -28.0)):
        els.append(element(f"guard_tip_{side}", [x0, 3.1, 7.3], [x1, 4.5, 8.7], "stone",
                           pivot=((x0 + x1) / 2.0, 3.1), theta=th, faces=box_faces("stone", 0.8, 1.4)))
    # the blade
    bx, by, ang = 8.0, BLADE_Y0, 0.0
    for i, (length, hw, bend, spine) in enumerate(SEGMENTS):
        ang += bend
        sw = f"blade{i}"
        pv = (bx, by)
        els.append(element(f"blade_{i}", [bx - hw, by, Z0], [bx + hw, by + length + OVERLAP, Z1], sw, pivot=pv, theta=ang,
                           faces=box_faces(sw, 2 * hw, length)))
        # the thick, uneven spine along the back (-X): thicker than the plate
        els.append(element(f"spine_{i}", [bx - hw - 0.35, by, 8.0 - spine / 2.0], [bx - hw + 0.45, by + length, 8.0 + spine / 2.0],
                           "spine", pivot=pv, theta=ang, faces=box_faces("spine", 0.8, length, top="stone_dark")))
        # the lit edge along the front (+X)
        els.append(element(f"edge_{i}", [bx + hw - 0.1, by, 7.8], [bx + hw + 0.4, by + length, 8.2], "edge", pivot=pv, theta=ang,
                           faces=box_faces("edge", 0.5, length, top="edge")))
        # the mana groove, glowing, on both flats
        gl = {"south": {"uv": uv(groove_tex, 0.5, length), "texture": "#0"}, "north": {"uv": uv(groove_tex, 0.5, length), "texture": "#0"}}
        els.append(element(f"groove_front_{i}", [bx - 0.25, by + 0.1, Z1], [bx + 0.25, by + length - 0.1, Z1 + 0.1], groove_tex,
                           pivot=pv, theta=ang, emission=groove_light, faces={"south": gl["south"], "up": gl["south"]}))
        els.append(element(f"groove_back_{i}", [bx - 0.25, by + 0.1, Z0 - 0.1], [bx + 0.25, by + length - 0.1, Z0], groove_tex,
                           pivot=pv, theta=ang, emission=groove_light, faces={"north": gl["north"], "up": gl["north"]}))
        rad = math.radians(ang)
        bx, by = bx - length * math.sin(rad), by + length * math.cos(rad)
    # the point: a final small wedge ending the last segment
    rad = math.radians(ang)
    els.append(element("point", [bx - 0.9, by - 0.05, 7.6], [bx + 0.9, by + 1.4, 8.4], "blade3", pivot=(bx, by), theta=ang + 8.0,
                       faces=box_faces("blade3", 1.8, 1.4)))
    # the rune by the guard, complete (§9.2)
    els.append(element("rune_front", [7.3, 3.9, Z1], [8.7, 4.8, Z1 + 0.15], rune_tex, emission=rune_light,
                       faces={"south": {"uv": uv(rune_tex, 1.0, 1.0), "texture": "#0"}}))
    els.append(element("rune_back", [7.3, 3.9, Z0 - 0.15], [8.7, 4.8, Z0], rune_tex, emission=rune_light,
                       faces={"north": {"uv": uv(rune_tex, 1.0, 1.0), "texture": "#0"}}))
    return {
        "textures": {"0": "codenamecoraline:item/architect_scimitar_3d", "particle": "codenamecoraline:item/architect_scimitar_3d"},
        "elements": els,
        "display": DISPLAY,
    }


# Display transforms per context [ADP until runClient]. The sword lies on the item diagonal like a vanilla sword sprite
# and is about as long (20 units), so the vanilla handheld values hold it the same way. The ground and frame values are
# new: the drop is a 3D sword floating a little above the floor and spinning (the item entity turns it about Y), the
# frame shows it on its diagonal, the shelf and the head slot get quiet values.
DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2.5, 0], "scale": [0.62, 0.62, 0.62]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.78, 0.78, 0.78]},
    "on_shelf": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.78, 0.78, 0.78]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
    "gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.95, 0.95, 0.95]},
}


def model(name):
    return {"type": "minecraft:model", "model": f"codenamecoraline:item/{name}"}


def charged_flag(on_true, on_false):
    return {"type": "minecraft:condition", "property": "minecraft:custom_model_data", "index": 0,
            "on_true": on_true, "on_false": on_false}


def item_definition():
    """The inventory icon stays the crisp 2D sprite; every other context (both hands, the ground, frames, shelves) is the
    3D sword, so a dropped scimitar is the real model and not the flat icon."""
    in_world = {
        "type": "minecraft:condition", "property": "minecraft:using_item",
        "on_true": model("architect_scimitar_3d_raised"),
        "on_false": charged_flag(
            model("architect_scimitar_3d_charged"),
            {"type": "minecraft:range_dispatch", "property": "minecraft:cooldown",
             "entries": [{"threshold": 0.0001, "model": model("architect_scimitar_3d_dim")}],
             "fallback": model("architect_scimitar_3d")}),
    }
    return {"model": {
        "type": "minecraft:select", "property": "minecraft:display_context",
        "cases": [{"when": ["gui"], "model": charged_flag(model("architect_scimitar_charged"), model("architect_scimitar"))}],
        "fallback": in_world,
    }}


def write_json(path: Path, data):
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def main():
    icon(False).save(TEX / "architect_scimitar.png")
    icon(True).save(TEX / "architect_scimitar_charged.png")
    atlas().save(TEX / "architect_scimitar_3d.png")
    portrait().save(TEX / "architect_portrait.png")
    for name in ("architect_scimitar", "architect_scimitar_charged"):
        write_json(MODELS / f"{name}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"codenamecoraline:item/{name}"}})
    write_json(MODELS / "architect_scimitar_3d.json", hand_model("groove", 8, "rune", 8))
    write_json(MODELS / "architect_scimitar_3d_raised.json", hand_model("groove", 15, "rune", 8))
    write_json(MODELS / "architect_scimitar_3d_charged.json", hand_model("groove_bright", 15, "rune_lit", 15))
    write_json(MODELS / "architect_scimitar_3d_dim.json", hand_model("groove_dim", 0, "rune", 0))
    write_json(ITEMS / "architect_scimitar.json", item_definition())
    write_json(MODELS / "architect_portrait.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "codenamecoraline:item/architect_portrait"}})
    write_json(ITEMS / "architect_portrait.json", {"model": model("architect_portrait")})
    print("Generated the Architect's Scimitar (2 icons, 64x64 atlas, 6 models, item definition) and the portrait.")


if __name__ == "__main__":
    main()
