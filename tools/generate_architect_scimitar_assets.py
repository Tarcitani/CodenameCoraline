r"""Generates every asset of the Architect's Scimitar and the Architect portrait (spec docs/design/architect.md §26.5).

Run: python tools/generate_architect_scimitar_assets.py
Out: textures/item/architect_scimitar{,_charged,_3d}.png, textures/item/architect_portrait.png,
     models/item/architect_scimitar{,_charged,_3d,_3d_raised,_3d_charged,_3d_dim}.json, models/item/architect_portrait.json,
     items/architect_scimitar.json, items/architect_portrait.json

This script is the single source of the scimitar's geometry: the hand model is the five cubes of §9.1 (pommel, grip,
guard, blade base, blade tip bent 14 degrees like sword_top_right) plus the mana groove, the edge and the rune.
The whole sword is laid on the item diagonal (-45 degrees about Z, like the Deadroot Club) so the vanilla handheld
display transforms hold it as they hold a sword.
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


# ============================================================ 32x32 atlas of the hand model
# swatches in texture pixels (x0, y0, x1, y1); UVs in the model are pixels / 2
SW = {
    "blade": (0, 0, 16, 16),
    "groove": (16, 0, 18, 14),
    "groove_bright": (18, 0, 20, 14),
    "groove_dim": (20, 0, 22, 14),
    "edge": (22, 0, 24, 16),
    "grip": (24, 0, 26, 8),
    "stone": (26, 0, 32, 6),
    "rune": (26, 8, 28, 10),
    "rune_lit": (28, 8, 30, 10),
}


def atlas() -> Image.Image:
    img = Image.new("RGBA", (32, 32), CLEAR)
    px = img.load()

    def fill(name, fn):
        x0, y0, x1, y1 = SW[name]
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = fn(x - x0, y - y0)

    # basalt in stepped bands, a crack line and lighter facets (no gradients)
    fill("blade", lambda x, y: SHADOW if (x + 2 * y) % 11 == 0 else LIGHT if (x * 3 + y) % 13 == 0 else BASE)
    fill("groove", lambda x, y: SPARK if y % 5 == 2 else GROOVE)
    fill("groove_bright", lambda x, y: SPARK if y % 3 else (0xE6, 0xFF, 0xFA, 255))
    fill("groove_dim", lambda x, y: GROOVE_DIM)
    fill("edge", lambda x, y: EDGE if y % 4 == 1 else LIGHT)
    fill("grip", lambda x, y: GRIP_LIGHT if (y + x) % 3 == 0 else GRIP_DARK)
    fill("stone", lambda x, y: EDGE if x == y else LIGHT if (x + y) % 2 == 0 else BASE)
    fill("rune", lambda x, y: RUNE)
    fill("rune_lit", lambda x, y: (0xF0, 0xE8, 0xFF, 255))
    return img


def uv(name):
    x0, y0, x1, y1 = SW[name]
    return [x0 / 2, y0 / 2, x1 / 2, y1 / 2]


def faces(name, only=None):
    out = {}
    for d in ("north", "south", "east", "west", "up", "down"):
        if only is None or d in only:
            out[d] = {"uv": uv(name), "texture": "#0"}
    return out


# Geometry (model units, vertical: pommel at the bottom, blade up +Y, flat in X, thin in Z, edge on +X)
C = (8.0, 8.0)  # the whole sword turns -45 degrees about Z around this point to lie on the item diagonal
TILT = -45.0
CURVE = 14.0  # sword_top_right (§9.2)
TIP_PIVOT = (8.0, 11.5)  # where blade_tip leaves blade_base


def rot(p, deg, o):
    a = math.radians(deg)
    x, y = p[0] - o[0], p[1] - o[1]
    return (o[0] + x * math.cos(a) - y * math.sin(a), o[1] + x * math.sin(a) + y * math.cos(a))


def element(name, frm, to, tex, *, tip=False, emission=0, only=None):
    """A cube on the diagonal; tip parts also carry the 14 degree bend (one rotation: -45 + 14 about the moved pivot)."""
    if tip:
        pivot = rot(TIP_PIVOT, TILT, C)
        d = (pivot[0] - TIP_PIVOT[0], pivot[1] - TIP_PIVOT[1])
        frm = [frm[0] + d[0], frm[1] + d[1], frm[2]]
        to = [to[0] + d[0], to[1] + d[1], to[2]]
        rotation = {"origin": [round(pivot[0], 4), round(pivot[1], 4), 8], "z": TILT + CURVE}
    else:
        rotation = {"origin": [C[0], C[1], 8], "z": TILT}
    e = {"name": name, "from": [round(v, 4) for v in frm], "to": [round(v, 4) for v in to],
         "rotation": rotation, "faces": faces(tex, only)}
    if emission:
        e["light_emission"] = emission
    return e


def hand_model(groove_tex, groove_light, rune_tex, rune_light):
    els = [
        element("pommel", [7.25, -3.5, 7.25], [8.75, -2.0, 8.75], "stone"),
        element("grip", [7.5, -2.0, 7.5], [8.5, 2.0, 8.5], "grip"),
        element("guard", [6.5, 2.0, 7.25], [9.5, 3.0, 8.75], "stone"),
        element("blade_base", [6.5, 3.0, 7.5], [9.5, 11.5, 8.5], "blade"),
        element("edge_base", [9.5, 3.0, 7.75], [10.0, 11.5, 8.25], "edge"),
        element("blade_tip", [7.0, 11.5, 7.5], [9.0, 18.0, 8.5], "blade", tip=True),
        element("edge_tip", [9.0, 11.5, 7.75], [9.4, 17.4, 8.25], "edge", tip=True),
        # the mana groove: ~60 % of the blade, on both flats, glowing
        element("groove_front", [7.6, 4.2, 8.5], [8.3, 11.5, 8.6], groove_tex, emission=groove_light, only=("south", "east", "west", "up", "down")),
        element("groove_back", [7.6, 4.2, 7.4], [8.3, 11.5, 7.5], groove_tex, emission=groove_light, only=("north", "east", "west", "up", "down")),
        element("groove_tip_front", [7.6, 11.5, 8.5], [8.3, 13.0, 8.6], groove_tex, tip=True, emission=groove_light, only=("south", "east", "west", "up")),
        element("groove_tip_back", [7.6, 11.5, 7.4], [8.3, 13.0, 7.5], groove_tex, tip=True, emission=groove_light, only=("north", "east", "west", "up")),
        # the complete rune by the guard (§9.2)
        element("rune_front", [7.4, 3.2, 8.5], [8.5, 3.9, 8.65], rune_tex, emission=rune_light, only=("south",)),
        element("rune_back", [7.4, 3.2, 7.35], [8.5, 3.9, 7.5], rune_tex, emission=rune_light, only=("north",)),
    ]
    return {
        "textures": {"0": "codenamecoraline:item/architect_scimitar_3d", "particle": "codenamecoraline:item/architect_scimitar_3d"},
        "elements": els,
        # vanilla handheld values [ADP]: adjust in runClient
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
            "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
            "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
        },
    }


def model(name):
    return {"type": "minecraft:model", "model": f"codenamecoraline:item/{name}"}


def charged_flag(on_true, on_false):
    return {"type": "minecraft:condition", "property": "minecraft:custom_model_data", "index": 0,
            "on_true": on_true, "on_false": on_false}


def item_definition():
    in_hand = {
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
        "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"],
                   "model": charged_flag(model("architect_scimitar_charged"), model("architect_scimitar"))}],
        "fallback": in_hand,
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
    print("Generated the Architect's Scimitar (2 icons, 32x32 atlas, 6 models, item definition) and the portrait.")


if __name__ == "__main__":
    main()
