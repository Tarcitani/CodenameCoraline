"""Generate 5 subtle visual texture variants (1..5) per dragon stage alongside variant 0.

Each variant belongs to the same crimson dragon color family while varying:
- Scale tones (red, neck, snout, leg, dark_red, tail)
- Belly plates (cream)
- Wing bones and membranes (wing_bone, membrane, wing_highlight)
- Horns, claws and dorsal spines (horn, horn_tip, claw, orange)
- Subtle scars / markings and scale details on specific anatomical parts
"""
import math
import random
from PIL import Image
from generate_dragon_remodel import RIGS, SIZE, ASSETS, palette, make, pack

VARIANTS = {
    1: {
        # Ruby Ember: deeper ruby scales, golden-parchment belly, copper wing membranes, weathered horn bases, snout/shoulder scar
        'name': 'ruby_ember',
        'mul': {
            'red': (0.93, 0.86, 0.90), 'dark_red': (0.90, 0.82, 0.88), 'neck': (0.94, 0.88, 0.90),
            'snout': (0.95, 0.90, 0.88), 'leg': (0.92, 0.86, 0.88), 'tail': (0.93, 0.87, 0.90),
            'cream': (1.02, 0.97, 0.88), 'horn': (0.92, 0.88, 0.82), 'horn_tip': (0.82, 0.76, 0.70),
            'claw': (0.88, 0.84, 0.78), 'orange': (1.00, 0.92, 0.85),
            'wing_bone': (0.90, 0.84, 0.86), 'membrane': (0.98, 0.89, 0.82), 'wing_highlight': (1.00, 0.94, 0.86),
        },
        'scar_parts': {'head', 'muzzle_0', 'left_front_leg'},
        'scar_tint': (0.68, 0.56, 0.58),
        'fleck_parts': {'body', 'tail_base', 'right_cheek', 'left_cheek'},
        'fleck_tint': (1.14, 1.08, 0.92),
    },
    2: {
        # Volcanic Ash: dusky smoky-crimson scales, pale ash-cream belly, soot-veined wings, obsidian-tipped horns, dorsal ash markings
        'name': 'volcanic_ash',
        'mul': {
            'red': (0.88, 0.85, 0.88), 'dark_red': (0.84, 0.80, 0.84), 'neck': (0.89, 0.85, 0.87),
            'snout': (0.88, 0.84, 0.85), 'leg': (0.86, 0.82, 0.85), 'tail': (0.87, 0.84, 0.86),
            'cream': (0.95, 0.95, 0.96), 'horn': (0.86, 0.84, 0.84), 'horn_tip': (0.58, 0.54, 0.56),
            'claw': (0.68, 0.64, 0.66), 'orange': (0.92, 0.84, 0.78),
            'wing_bone': (0.82, 0.78, 0.80), 'membrane': (0.90, 0.84, 0.80), 'wing_highlight': (0.94, 0.88, 0.84),
        },
        'scar_parts': {'body', 'right_wing_elbow', 'tail_base'},
        'scar_tint': (0.62, 0.58, 0.60),
        'fleck_parts': {'forehead', 'neck', 'tail_middle'},
        'fleck_tint': (0.76, 0.72, 0.74),
    },
    3: {
        # Sunlit Copper: warm vermilion-copper scales, sunlit cream belly, amber wing membranes, gilded horns, warm cheek/tail freckles
        'name': 'sunlit_copper',
        'mul': {
            'red': (1.04, 1.08, 0.94), 'dark_red': (1.04, 1.05, 0.92), 'neck': (1.05, 1.08, 0.95),
            'snout': (1.03, 1.06, 0.94), 'leg': (1.04, 1.06, 0.94), 'tail': (1.05, 1.08, 0.94),
            'cream': (1.03, 1.02, 0.94), 'horn': (1.02, 0.98, 0.88), 'horn_tip': (1.00, 0.96, 0.84),
            'claw': (0.98, 0.94, 0.84), 'orange': (1.02, 1.06, 0.92),
            'wing_bone': (1.02, 1.02, 0.90), 'membrane': (1.00, 1.06, 0.90), 'wing_highlight': (1.02, 1.08, 0.94),
        },
        'scar_parts': {'right_cheek', 'right_front_leg'},
        'scar_tint': (0.78, 0.68, 0.62),
        'fleck_parts': {'body', 'left_cheek', 'right_cheek', 'tail_base', 'tail_middle'},
        'fleck_tint': (1.16, 1.18, 0.98),
    },
    4: {
        # Ancient Garnet: deep wine-garnet scales, aged ivory belly, dark umber wing bones, ringed horns, twin torso/wing battle scars
        'name': 'ancient_garnet',
        'mul': {
            'red': (0.86, 0.78, 0.84), 'dark_red': (0.82, 0.74, 0.80), 'neck': (0.87, 0.79, 0.84),
            'snout': (0.89, 0.82, 0.84), 'leg': (0.85, 0.76, 0.82), 'tail': (0.86, 0.78, 0.83),
            'cream': (0.94, 0.90, 0.82), 'horn': (0.88, 0.82, 0.74), 'horn_tip': (0.74, 0.68, 0.62),
            'claw': (0.80, 0.74, 0.68), 'orange': (0.94, 0.86, 0.80),
            'wing_bone': (0.80, 0.72, 0.76), 'membrane': (0.92, 0.82, 0.78), 'wing_highlight': (0.96, 0.88, 0.82),
        },
        'scar_parts': {'body', 'head', 'left_wing_elbow', 'right_wing_elbow'},
        'scar_tint': (0.64, 0.52, 0.56),
        'fleck_parts': {'neck', 'forehead', 'tail_tip'},
        'fleck_tint': (1.12, 0.96, 0.88),
    },
    5: {
        # Crowned Flame: vivid carmine scales with crisp scale contrast, ochre-cream belly, bright ember-veined wings, pale ivory horns
        'name': 'crowned_flame',
        'mul': {
            'red': (1.02, 0.94, 0.88), 'dark_red': (0.96, 0.86, 0.82), 'neck': (1.00, 0.92, 0.86),
            'snout': (1.02, 0.96, 0.88), 'leg': (0.96, 0.88, 0.84), 'tail': (1.00, 0.92, 0.86),
            'cream': (1.00, 0.94, 0.84), 'horn': (1.03, 1.02, 0.98), 'horn_tip': (0.76, 0.68, 0.62),
            'claw': (0.96, 0.94, 0.90), 'orange': (1.02, 1.02, 0.88),
            'wing_bone': (0.94, 0.84, 0.82), 'membrane': (1.00, 0.98, 0.86), 'wing_highlight': (1.04, 1.05, 0.92),
        },
        'scar_parts': {'muzzle_1', 'tail_base'},
        'scar_tint': (0.72, 0.60, 0.58),
        'fleck_parts': {'forehead', 'right_brow', 'left_brow', 'body', 'tail_base', 'tail_middle'},
        'fleck_tint': (1.18, 1.12, 0.90),
    },
}


def clamp_rgb(rgb):
    return tuple(max(0, min(255, int(round(c)))) for c in rgb)


def generate_variant_texture(a, base_colors, variant_id, cfg):
    age = RIGS.index(a.rig)
    rng = random.Random(71041 + age * 100 + variant_id * 17)
    image = Image.new('RGBA', (SIZE, SIZE))
    mul = cfg['mul']
    for b in a.boxes:
        x, y, w, h = b['uv']
        name = b['material']
        part = b['part']
        base = base_colors[name]
        if name in mul:
            m = mul[name]
            base = [base[0] * m[0], base[1] * m[1], base[2] * m[2]]
        for py in range(h):
            for px in range(w):
                stripe = ((px + (py // 3) * 2) % 5 == 0 and py % 3 == 2)
                shade = 0.84 + 0.18 * (1 - py / max(1, h - 1)) + rng.uniform(-0.035, 0.035)
                if stripe and name in ('red', 'neck', 'dark_red', 'tail', 'leg'):
                    shade -= 0.11 + age * 0.025 + (0.02 if variant_id in (4, 5) else 0.0)
                if name == 'cream':
                    # Subtle belly plate segmentation and edge shading
                    if py % 4 == 0:
                        shade -= 0.045 + 0.008 * variant_id
                    elif py % 4 == 2:
                        shade += 0.025
                if name in ('membrane', 'wing_highlight'):
                    vein = math.sin(px * (0.50 + 0.04 * variant_id) + py * 0.20)
                    shade += 0.065 * vein - (0.11 if (px + variant_id) % 7 == 0 else 0.0)
                if name in ('horn', 'horn_tip', 'claw'):
                    ring_mod = 3 if variant_id in (2, 4) else 4
                    shade -= 0.10 if (py + variant_id) % ring_mod == 0 else 0.0
                rgb = [c * shade for c in base]

                # Subtle battle scars / natural markings on designated parts
                if part in cfg['scar_parts'] and name in ('red', 'dark_red', 'snout', 'leg', 'tail'):
                    diag = (px - py * 2 + variant_id * 3)
                    if 2 <= (diag % 19) <= 3 and 1 <= py < h - 1:
                        st = cfg['scar_tint']
                        rgb = [rgb[0] * st[0], rgb[1] * st[1], rgb[2] * st[2]]

                # Subtle scale freckles / ember flecks on designated parts
                if part in cfg['fleck_parts'] and name in ('red', 'neck', 'tail', 'dark_red'):
                    if ((px * 7 + py * 13 + variant_id * 5) % 23) == 0:
                        ft = cfg['fleck_tint']
                        rgb = [rgb[0] * ft[0], rgb[1] * ft[1], rgb[2] * ft[2]]

                color = clamp_rgb(rgb) + (255,)
                if name == 'eye':
                    color = clamp_rgb(base_colors['eye']) + (255,)
                    if px % 6 in (2, 3):
                        color = clamp_rgb(base_colors['ink']) + (255,)
                    if px % 6 == 1 and py % 5 == 1:
                        color = clamp_rgb(base_colors['horn_tip']) + (255,)
                image.putpixel((x + px, y + py), color)

    out_path = ASSETS / f"{a.rig['id']}_dragon_{variant_id}.png"
    image.save(out_path)
    return out_path


def main():
    base_colors = palette()
    anatomies = [make(r) for r in RIGS]
    for a in anatomies:
        pack(a)
        for variant_id, cfg in VARIANTS.items():
            path = generate_variant_texture(a, base_colors, variant_id, cfg)
            print(f"Generated {path.name} ({cfg['name']})")


if __name__ == '__main__':
    main()

