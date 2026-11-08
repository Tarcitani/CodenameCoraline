"""Poses and animation clips of the Architect (spec docs/design/architect.md §5.4, §7.4, §10, §13, §15).

Single source of truth: tools/generate_architect_assets.py turns these clips into the generated
client/architect/ArchitectAnimations.java, and tools/preview_architect_poses.py draws them offline.

A pose maps a part to ABSOLUTE degrees (x, y, z) in ModelPart order (Z * Y * X, x applied first); "@part" keys are
position offsets in model pixels (y DOWN, +x = the character's left, -z = front) and "%part" keys are uniform scales.
Parts left out keep BASE (the battle idle). Sign conventions (spec §3.1): a negative x lifts a hanging limb forward;
the left side (+x) opens with a negative z, the right side with a positive z. Right-side values are written once for
the left side and mirrored with mirror() as (x, -y, -z).
"""

TIERS = ("top", "mid", "low")
SIDES = ("left", "right")


def mirror(pose):
    """Copies every *_left entry to its *_right twin with (x, -y, -z); explicit right entries win."""
    out = dict(pose)
    for key, value in pose.items():
        bare = key.lstrip("@%")
        if bare.endswith("_left"):
            twin = key[: len(key) - len("left")] + "right"
            if twin in pose:
                continue
            if key.startswith("@"):
                out[twin] = (-value[0], value[1], value[2])
            elif key.startswith("%"):
                out[twin] = value
            else:
                out[twin] = (value[0], -value[1], -value[2])
    return out


def merge(*poses):
    out = {}
    for p in poses:
        out.update(p)
    return out


# ---- blade curvature (spec §9.2): constant, lives in BASE
CURVE = {"top_right": 14, "top_left": 20, "mid_right": 14, "mid_left": 10, "low_right": 26, "low_left": 18}

# ---- wing states (left; mirrored)
WING_FOLDED = {"wing_root_left": (-8, -80, 0), "wing_arm_left": (0, 0, -22), "wing_forearm_left": (0, 168, 30),
               "wing_finger_1_left": (0, 0, 10), "wing_finger_2_left": (0, 0, 5), "wing_finger_3_left": (0, 0, 0),
               "wing_finger_4_left": (0, 0, -5)}
WING_HALF = {"wing_root_left": (0, -40, 0), "wing_arm_left": (0, 0, -12), "wing_forearm_left": (0, 55, 0),
             "wing_finger_1_left": (0, 0, -8), "wing_finger_2_left": (0, 0, -18), "wing_finger_3_left": (0, 0, -28),
             "wing_finger_4_left": (0, 0, -38)}
WING_OPEN = {"wing_root_left": (0, -14, 0), "wing_arm_left": (0, 0, -16), "wing_forearm_left": (0, 8, -6),
             "wing_finger_1_left": (0, 0, -6), "wing_finger_2_left": (0, 0, -20), "wing_finger_3_left": (0, 0, -34),
             "wing_finger_4_left": (0, 0, -50)}

# ---- legs
STANCE = {"@pelvis": (0, 1, 0), "leg_left": (-8, 0, -7), "knee_left": (14, 0, 0), "ankle_left": (-6, 0, 7)}

# ---- the six arms in the battle idle: three planes of threat (spec §5.4)
ARMS_IDLE = {
    # top: raised out to the side, forearm up, blade high and outward (guards the head, crown silhouette)
    "arm_top_left": (-15, -90, -105), "elbow_top_left": (-70, 0, 0), "wrist_top_left": (-10, 0, 0),
    # mid: out to the flank, blade level and pointing forward-out
    "arm_mid_left": (-20, -90, -70), "elbow_mid_left": (-40, 0, 0), "wrist_mid_left": (-20, 0, 0),
    # low: close to the hips, blade low and forward, ready to sweep
    "arm_low_left": (-30, 0, -32), "elbow_low_left": (-55, 0, 0), "wrist_low_left": (15, 0, 0),
}

BASE = mirror(merge(
    {f"blade_tip_{k}": (v, 0, 0) for k, v in CURVE.items()},
    STANCE, ARMS_IDLE, WING_HALF,
    {"torso": (6, 0, 0), "neck": (-4, 0, 0), "head": (-2, 0, 4)},
))
# blade curvature is not mirrored: each blade has its own value
for _k, _v in CURVE.items():
    BASE[f"blade_tip_{_k}"] = (_v, 0, 0)

# ---- the statue (spec §7.1, §7.2): two arms hold the tablet, four arms folded behind the robe and the wings
STATUE = mirror(merge(
    WING_FOLDED,
    {"@pelvis": (0, 0, 0), "leg_left": (0, 0, -2), "knee_left": (0, 0, 0), "ankle_left": (0, 0, 2),
     "torso": (0, 0, 0), "neck": (10, 0, 0), "head": (18, 0, 0),
     # solved so tablet_grip_* lands on the tablet's edges (+-5.5; 30; -8) BB: the forearms point in, the blades hang
     "arm_top_left": (8, 48, -16), "elbow_top_left": (-95, 0, 0), "wrist_top_left": (90, 0, -15),
     # the other four arms hang behind the body, blades swept back under the folded wings
     "arm_mid_left": (22, 0, 14), "elbow_mid_left": (-10, 0, 0), "wrist_mid_left": (70, 0, 0),
     "arm_low_left": (28, 0, 16), "elbow_low_left": (-10, 0, 0), "wrist_low_left": (88, 0, 0)},
))
