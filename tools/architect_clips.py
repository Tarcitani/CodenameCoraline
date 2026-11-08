"""Animation clips of the Architect (spec docs/design/architect.md §7.4, §10, §11, §13, §15), built from the poses of
tools/architect_poses.py. Every clip is stored as per-part TRACKS: {key: [(tick, value)]}, absolute values (degrees,
model pixels or scale), so each event keeps its own ticks. tools/generate_architect_assets.py turns them into the
generated ArchitectAnimations.java; tools/preview_architect_poses.py samples them offline.

Attack frames sit on the spec §13 ticks so the damage windows of slice 3 match the picture.
"""
from architect_poses import BASE, STATUE, WING_FOLDED, WING_HALF, WING_OPEN, mirror

CLIPS = {}  # name -> (ticks, loops, smooth, tracks)


def P(*overrides, sym=True, base=None):
    """BASE (or another pose) with overrides; left-side entries are mirrored to the right unless sym=False."""
    out = dict(BASE if base is None else base)
    for o in overrides:
        out.update(mirror(o) if sym else o)
    return out


def arms(tier, side, arm, elbow=(0, 0, 0), wrist=(0, 0, 0)):
    n = f"{tier}_{side}"
    return {f"arm_{n}": arm, f"elbow_{n}": elbow, f"wrist_{n}": wrist}


def lr(tier, left_arm, elbow=(0, 0, 0), wrist=(0, 0, 0)):
    """The same arm state on both sides (the right one mirrored)."""
    return mirror(arms(tier, "left", left_arm, elbow, wrist))


def default_of(key):
    return (0, 0, 0) if key.startswith("@") else (1, 1, 1) if key.startswith("%") else BASE.get(key, (0, 0, 0))


def tracks_of(frames):
    """Whole-pose frames -> tracks, keeping only keys that move or differ from BASE."""
    keys = set()
    for _, pose in frames:
        keys |= set(pose)
    out = {}
    for k in sorted(keys):
        d = default_of(k)
        values = [(t, tuple(pose.get(k, d))) for t, pose in frames]
        if all(v == d for _, v in values):
            continue
        out[k] = values
    return out


def clip(name, ticks, loops, frames, smooth=True):
    CLIPS[name] = (ticks, loops, smooth, tracks_of(frames))


GUARD = P({
    "torso": (10, 0, 0), "knee_left": (25, 0, 0), "@pelvis": (0, 2, 0), "leg_left": (-14, 0, -8),
    # top blades crossed high in front of the face, mid blades level over the flanks, low blades crossed on the belly
    "arm_top_left": (-150, 0, -25), "elbow_top_left": (-40, 0, 0), "wrist_top_left": (0, 0, 35),
    "arm_mid_left": (-20, -90, -75), "elbow_mid_left": (-60, 0, 0), "wrist_mid_left": (-30, 0, 0),
    "arm_low_left": (-55, 30, -6), "elbow_low_left": (-60, 0, 0), "wrist_low_left": (0, 0, 30),
    "wing_root_left": (0, -28, 0),
})


# ------------------------------------------------------------------------------------------------ presentation
def presentation():
    clip("DORMANT_STATUE", 200, True, [(0, STATUE), (200, STATUE)], smooth=False)
    idle_b = P({"torso": (7, 0, 0), "arm_top_left": (-15, -90, -100), "arm_mid_left": (-18, -90, -73),
                "wing_root_left": (0, -44, 0), "head": (-2, 0, 6)})
    idle_b["arm_top_right"] = (-12, 90, 108)  # the dominant upper right arm adjusts on its own (spec §10.1)
    clip("BATTLE_IDLE", 60, True, [(0, BASE), (30, idle_b), (60, BASE)])
    awaken()
    laugh_open = P({"head": (-14, 0, 4), "@neck": (0, -0.5, 0), "jaw": (38, 0, 0), "brow_left": (0, 0, 6), "torso": (2, 0, 0)})
    laugh_mid = P({"head": (-10, 0, 4), "@neck": (0, -0.5, 0), "jaw": (30, 0, 0), "brow_left": (0, 0, 6), "torso": (6, 0, 0)})
    clip("MANIACAL_LAUGH", 36, False, [(0, BASE), (8, laugh_open), (12, laugh_mid), (16, laugh_open), (20, laugh_mid),
                                       (24, laugh_open), (28, laugh_mid), (36, BASE)])


def awaken():
    """The 110-tick master timeline (spec §7.4), written as tracks so every event keeps its own ticks."""
    tracks = {}

    def tr(key, *points):
        tracks[key] = list(points)

    S, B = STATUE, BASE
    tr("head", (0, S["head"]), (22, S["head"]), (34, (-4, 0, 0)), (100, (-4, 0, 0)), (110, (-2, 0, 10)))
    tr("neck", (0, S["neck"]), (22, S["neck"]), (34, (-8, 0, 0)), (100, B["neck"]), (110, B["neck"]))
    tr("@head", (0, (0, 0, 0)), (22, (0, 0, 0)), (34, (0, -0.5, 0)), (100, (0, -0.5, 0)), (110, (0, 0, 0)))
    tr("jaw", (0, (0, 0, 0)), (86, (0, 0, 0)), (96, (30, 0, 0)), (110, (30, 0, 0)))
    w = S["wrist_top_left"]
    shake = [(0, w), (10, w)] + [(t, (w[0] + (1.5 if t % 4 == 0 else -1.5), w[1], w[2])) for t in range(12, 22, 2)]
    shake += [(24, w), (60, w), (100, B["wrist_top_left"]), (110, B["wrist_top_left"])]
    tr("wrist_top_left", *shake)
    tr("wrist_top_right", *[(t, (v[0], -v[1], -v[2])) for t, v in shake[:-2]],
       (100, B["wrist_top_right"]), (110, B["wrist_top_right"]))
    # the mask: right half first (opens like a door, comes off, falls, breaks), the left half later and faster
    tr("face_mask_right", (0, (0, 0, 0)), (34, (0, 0, 0)), (35, (0, 2, 0)), (36, (0, -2, 0)), (40, (0, 35, 0)),
       (52, (40, 35, 60)), (58, (40, 35, 110)), (110, (40, 35, 110)))
    tr("@face_mask_right", (0, (0, 0, 0)), (40, (0, 0, 0)), (52, (-3, 10, -6)), (58, (-6, 44, -10)), (110, (-6, 44, -10)))
    tr("%face_mask_right", (0, (1, 1, 1)), (58, (1, 1, 1)), (60, (0, 0, 0)), (110, (0, 0, 0)))
    tr("face_mask_left", (0, (0, 0, 0)), (44, (0, 0, 0)), (45, (0, -2, 0)), (46, (0, 2, 0)), (49, (0, -35, 0)),
       (58, (40, -35, -70)), (64, (40, -35, -120)), (110, (40, -35, -120)))
    tr("@face_mask_left", (0, (0, 0, 0)), (49, (0, 0, 0)), (58, (3, 10, -6)), (64, (6, 44, -10)), (110, (6, 44, -10)))
    tr("%face_mask_left", (0, (1, 1, 1)), (64, (1, 1, 1)), (66, (0, 0, 0)), (110, (0, 0, 0)))
    # the chest swells, the seal pulses
    tr("torso", (0, S["torso"]), (50, S["torso"]), (56, (-6, 0, 0)), (62, (0, 0, 0)), (100, B["torso"]), (110, B["torso"]))
    tr("@torso", (0, (0, 0, 0)), (50, (0, 0, 0)), (56, (0, -0.5, 0)), (62, (0, 0, 0)), (110, (0, 0, 0)))
    tr("%sternum_seal", (0, (1, 1, 1)), (50, (1, 1, 1)), (56, (1.15, 1.15, 1.15)), (62, (1, 1, 1)), (110, (1, 1, 1)))
    # the four hidden arms unfold one after another, each with a small overshoot of abduction
    for n, t0, t1 in (("mid_right", 58, 68), ("mid_left", 62, 72), ("low_right", 68, 78), ("low_left", 74, 84)):
        s = 1 if n.endswith("left") else -1
        for seg in ("arm", "elbow", "wrist"):
            k = f"{seg}_{n}"
            a, b = S[k], B[k]
            over = (b[0], b[1], b[2] - 15 * s) if seg == "arm" else b
            tr(k, (0, a), (t0, a), (t1 - 3, over), (t1, b), (110, b))
    # the upper arms let go of the tablet and rise
    for side, s in (("left", 1), ("right", -1)):
        k = f"arm_top_{side}"
        a, b = S[k], B[k]
        tr(k, (0, a), (60, a), (66, (a[0], a[1], a[2] - 25 * s)), (72, (a[0], a[1], a[2] - 25 * s)), (100, b), (110, b))
        k = f"elbow_top_{side}"
        tr(k, (0, S[k]), (66, S[k]), (100, B[k]), (110, B[k]))
    # wings: folded -> fully open (left 2 ticks earlier) -> half open
    open_m = mirror(WING_OPEN)
    for side, t0 in (("left", 70), ("right", 72)):
        for key in WING_FOLDED:
            k = key.replace("_left", "_" + side)
            tr(k, (0, S[k]), (t0, S[k]), (t0 + 12, open_m[k]), (90, B[k]), (110, B[k]))
    # the tablet falls (its base hits the ground at 72), splits, lies there and crumbles away
    tr("@tablet_anchor", (0, (0, 0, 0)), (60, (0, 0, 0)), (66, (0, 6, -1)), (72, (0, 22, -2)), (110, (0, 22, -2)))
    tr("tablet_anchor", (0, (0, 0, 0)), (60, (0, 0, 0)), (72, (8, 0, 0)), (110, (8, 0, 0)))
    tr("tablet_half_left", (0, (0, 0, 0)), (72, (0, 0, 0)), (80, (0, 0, -24)), (110, (0, 0, -24)))
    tr("tablet_half_right", (0, (0, 0, 0)), (72, (0, 0, 0)), (80, (0, 0, 24)), (110, (0, 0, 24)))
    tr("@tablet_half_left", (0, (0, 0, 0)), (72, (0, 0, 0)), (80, (3, 0, 0)), (110, (3, 0, 0)))
    tr("@tablet_half_right", (0, (0, 0, 0)), (72, (0, 0, 0)), (80, (-3, 0, 0)), (110, (-3, 0, 0)))
    tr("%tablet_anchor", (0, (1, 1, 1)), (96, (1, 1, 1)), (99, (0, 0, 0)), (110, (0, 0, 0)))
    # the legs settle into the stance
    for k in ("leg_left", "leg_right", "knee_left", "knee_right", "ankle_left", "ankle_right"):
        tr(k, (0, S[k]), (60, S[k]), (100, B[k]), (110, B[k]))
    tr("@pelvis", (0, (0, 0, 0)), (60, (0, 0, 0)), (100, B["@pelvis"]), (110, B["@pelvis"]))
    CLIPS["AWAKEN"] = (110, False, False, tracks)


# ------------------------------------------------------------------------------------------------ locomotion
def locomotion():
    def walk_frame(front, back, high):
        o = dict(BASE)
        o.update({f"leg_{front}": (-28, 0, BASE[f"leg_{front}"][2]), f"knee_{front}": (22, 0, 0),
                  f"leg_{back}": (14, 0, BASE[f"leg_{back}"][2]), f"knee_{back}": (18, 0, 0),
                  "@pelvis": (0, 1 if high else 2.5, 0), "torso": (6, 4 if front == "left" else -4, 0),
                  "wing_root_left": (0, -44 if front == "left" else -36, 0),
                  "wing_root_right": (0, 36 if front == "left" else 44, 0)})
        return o
    clip("WALK", 24, True, [(0, walk_frame("left", "right", True)), (6, walk_frame("left", "right", False)),
                            (12, walk_frame("right", "left", True)), (18, walk_frame("right", "left", False)),
                            (24, walk_frame("left", "right", True))])
    run_base = P({"torso": (22, 0, 0), "head": (-18, 0, 0), "@pelvis": (0, 2, 0), "wing_root_left": (0, -30, 0),
                  "robe_back_upper": (25, 0, 0)},
                 lr("top", (40, 0, -20), (-40, 0, 0)), lr("mid", (-10, -90, -40), (-30, 0, 0)),
                 lr("low", (-40, 0, -20), (-40, 0, 0)))

    def run_frame(front, back, low):
        o = dict(run_base)
        o.update({f"leg_{front}": (-50, 0, 0), f"knee_{front}": (30, 0, 0), f"leg_{back}": (30, 0, 0),
                  f"knee_{back}": (40, 0, 0), "@pelvis": (0, 4 if low else 1, 0)})
        return o
    clip("RUN", 18, True, [(0, run_frame("left", "right", False)), (4, run_frame("left", "right", True)),
                           (9, run_frame("right", "left", False)), (13, run_frame("right", "left", True)),
                           (18, run_frame("left", "right", False))])


# ------------------------------------------------------------------------------------------------ defence and damage
def defence():
    clip("GUARD_FULL", 20, True, [(0, GUARD), (10, P({"torso": (11, 0, 0)}, base=GUARD)), (20, GUARD)])
    stagger = P({"@pelvis": (0, 3, 0), "knee_left": (40, 0, 0), "torso": (-8, 0, 0), "head": (14, 0, 0),
                 "wing_root_left": (0, -20, -10)},
                lr("top", (-40, -90, -140), (-20, 0, 0)), lr("mid", (-10, -90, -110), (-10, 0, 0)),
                lr("low", (-10, 0, -60), (-10, 0, 0)))
    clip("STAGGER", 30, False, [(0, BASE), (5, stagger), (14, P({"torso": (-4, 0, 0)}, base=stagger)), (22, stagger),
                                (30, BASE)])
    kneel = P({"@pelvis": (0, 9, 0), "leg_left": (-70, 0, -10), "knee_left": (110, 0, 0), "ankle_left": (-30, 0, 0),
               "torso": (20, 0, 0), "head": (20, 0, 0)},
              lr("top", (-20, 0, -40), (-40, 0, 0), (30, 0, 0)), lr("mid", (0, 0, -40), (-20, 0, 0), (40, 0, 0)),
              lr("low", (10, 0, -30), (-10, 0, 0), (60, 0, 0)), WING_FOLDED)
    collapse = P({"@pelvis": (0, 12, 0), "torso": (55, 0, 0), "head": (30, 0, 0), "jaw": (55, 0, 0)},
                 lr("top", (-10, 0, -70)), lr("mid", (10, 0, -70)), lr("low", (20, 0, -60)), base=kneel)
    clip("DEATH", 80, False, [(0, BASE), (10, P({"elbow_top_left": (-40, 0, 0), "elbow_mid_left": (-20, 0, 0)})),
                              (25, kneel), (35, P({"jaw": (55, 0, 0), "head": (24, 0, 0)}, base=kneel)),
                              (60, collapse), (80, collapse)], smooth=False)
    block = P({"arm_mid_left": (-85, 30, 0), "elbow_mid_left": (-40, 0, 0), "wrist_mid_left": (-50, 0, 0),
               "arm_top_left": (-100, -20, 0), "elbow_top_left": (-30, 0, 0), "torso": (6, -12, 0)}, sym=False)
    rip_wind = P({"arm_top_right": (-160, 20, 30), "elbow_top_right": (-30, 0, 0)}, sym=False, base=block)
    rip = P({"arm_top_right": (-50, -40, 0), "elbow_top_right": (-10, 0, 0), "torso": (10, 14, 0)}, sym=False, base=block)
    clip("PARRY_LEFT", 24, False, [(0, BASE), (4, block), (9, block), (12, rip_wind), (14, rip), (16, rip), (24, BASE)],
         smooth=False)
    # the right parry is the left one mirrored: the right mid arm blocks, the upper LEFT arm answers (spec §11.3)
    block_r = P({"arm_mid_right": (-85, -30, 0), "elbow_mid_right": (-40, 0, 0), "wrist_mid_right": (-50, 0, 0),
                 "arm_top_right": (-100, 20, 0), "elbow_top_right": (-30, 0, 0), "torso": (6, 12, 0)}, sym=False)
    rip_wind_r = P({"arm_top_left": (-160, -20, -30), "elbow_top_left": (-30, 0, 0)}, sym=False, base=block_r)
    rip_r = P({"arm_top_left": (-50, 40, 0), "elbow_top_left": (-10, 0, 0), "torso": (10, -14, 0)}, sym=False,
              base=block_r)
    clip("PARRY_RIGHT", 24, False, [(0, BASE), (4, block_r), (9, block_r), (12, rip_wind_r), (14, rip_r), (16, rip_r),
                                    (24, BASE)], smooth=False)
    # projectile deflection (14): the mid arms cross in front of the chest, one wing leans in (spec §11.4)
    cross = P({"torso": (8, 0, 0), "wing_root_left": (0, -20, -25)}, lr("mid", (-80, 40, 0), (-30, 0, 0)))
    clip("PROJECTILE_DEFLECT", 14, False, [(0, BASE), (3, cross), (8, cross), (14, BASE)], smooth=False)
    # backstep (16): 1.5 blocks back, two heavy steps at 8 and 11 (spec §10.5)
    back_a = P({"leg_left": (18, 0, -7), "knee_left": (20, 0, 0), "leg_right": (-18, 0, 7), "torso": (12, 0, 0),
                "@pelvis": (0, 2, 0)}, sym=False)
    back_b = P({"leg_right": (18, 0, 7), "knee_right": (20, 0, 0), "leg_left": (-14, 0, -7), "torso": (10, 0, 0),
                "@pelvis": (0, 2, 0)}, sym=False)
    clip("BACKSTEP", 16, False, [(0, BASE), (6, back_a), (8, back_a), (11, back_b), (16, BASE)])


# ------------------------------------------------------------------------------------------------ idle variants
def idles():
    """The three out-of-combat idles drawn every 100-200 ticks (MOB_CONTRACT §3.2): the laugh (presentation), the
    blade inspection and the wing adjustment."""
    inspect = P({"head": (16, 0, 14), "neck": (4, 0, 0), "arm_mid_left": (-70, -60, -10), "elbow_mid_left": (-60, 0, 0),
                 "wrist_mid_left": (0, 0, 40)}, sym=False)
    turn = P({"wrist_mid_left": (0, 0, -30), "head": (14, 0, 18)}, sym=False, base=inspect)
    clip("IDLE_BLADE_INSPECT", 30, False, [(0, BASE), (8, inspect), (16, turn), (22, inspect), (30, BASE)])
    stretch = P(WING_OPEN, {"torso": (0, 0, 0), "head": (-8, 0, 4)})
    clip("IDLE_WING_ADJUST", 24, False, [(0, BASE), (8, stretch), (12, stretch), (18, P(WING_FOLDED)), (24, BASE)])


# ------------------------------------------------------------------------------------------------ phase transition
def transition():
    """Phase enrage (40, spec §15.3): rises, wings fully open, jaw +35, arms open in pairs, the seal swells."""
    rise = P({"torso": (-10, 0, 0), "head": (-8, 0, 0)}, WING_OPEN)
    jaw = P({"jaw": (35, 0, 0)}, base=rise)
    top = P(lr("top", (-20, -90, -135)), base=jaw)
    mid = P(lr("mid", (-20, -90, -100)), base=top)
    low = P(lr("low", (-30, 0, -62)), base=mid)
    swell = P({"%sternum_seal": (1.15, 1.15, 1.15), "%forehead_diamond_seal": (1.15, 1.15, 1.15)}, base=low)
    clip("PHASE_ENRAGE", 40, False, [(0, BASE), (8, rise), (12, jaw), (14, top), (16, mid), (18, low), (20, low),
                                     (25, swell), (30, low), (40, BASE)])


# ------------------------------------------------------------------------------------------------ attacks
def attacks():
    # ---- A. Decree of the Six Blades (38): six cuts, one arm at a time, never together
    st = P({"torso": (2, 0, 0)}, lr("top", (-165, 0, -25), (-20, 0, 0)), lr("mid", (-90, -110, 0), (-15, 0, 0)),
           lr("low", (-30, -70, -10), (-20, 0, 0)))
    frames = [(0, BASE), (5, dict(st))]
    cuts = [  # (tick the cut ends, arm, end value, torso yaw, extra)
        (10, "arm_top_left", (-40, 40, 0), -15, {}),
        (14, "arm_mid_right", (-90, -50, 0), 15, {}),
        (18, "arm_low_left", (-50, 60, 0), 0, {"@pelvis": (0, 3, 0), "knee_left": (30, 0, 0), "knee_right": (30, 0, 0), "leg_left": (-22, 0, -7), "leg_right": (-22, 0, 7)}),
        (22, "arm_top_right", (-165, -10, 30), -15, {"@pelvis": (0, 1, 0)}),
        (26, "arm_mid_left", (-90, -100, 0), 10, {"wing_root_left": (0, -30, 0), "wing_root_right": (0, 52, 0)}),
        (30, "arm_low_right", (-50, -45, 0), 0, {"knee_left": (34, 0, 0), "knee_right": (34, 0, 0), "@pelvis": (0, 3, 0), "leg_left": (-24, 0, -7), "leg_right": (-24, 0, 7)}),
    ]
    for t1, key, end, yaw, extra in cuts:
        frames.append((t1 - 4, dict(st)))  # every other arm holds still until its own cut starts
        st = dict(st)
        st["torso"] = (8, yaw, 0)
        st[key] = end
        st.update(extra)
        frames.append((t1, dict(st)))
    frames = sorted({t: p for t, p in frames}.items())
    frames += [(31, dict(st)), (38, BASE)]
    clip("SIXFOLD_EDICT", 38, False, frames, smooth=False)

    # ---- B. Crossed Mandate (30): a high X, then a low X slightly late
    wind = P({"torso": (-6, 0, 0)}, lr("top", (-160, 0, -45), (-10, 0, 0)), lr("mid", (-90, -115, 0), (-10, 0, 0)))
    hi_x = P({"torso": (14, 0, 0)}, lr("top", (-70, 35, 10), (-10, 0, 0)), lr("mid", (-90, -115, 0), (-10, 0, 0)))
    lo_x = P({"torso": (18, 0, 0), "@pelvis": (0, 2, 0)}, lr("top", (-60, 35, 10), (-10, 0, 0)),
             lr("mid", (-60, 40, 0), (-10, 0, 0)))
    flap = P({"wing_root_left": (0, -20, -15)}, base=lo_x)
    clip("CROSSED_MANDATE", 30, False, [(0, BASE), (8, wind), (11, wind), (13, hi_x), (16, hi_x), (18, lo_x), (20, flap),
                                         (22, lo_x), (30, BASE)], smooth=False)

    # ---- C. Cartenon Wheel (44): arms open in three heights, the whole body spins once
    open_ = P(lr("top", (-20, 0, -120)), lr("mid", (0, 0, -90)), lr("low", (20, 0, -60)), WING_OPEN)

    def spin(yaw):
        return P({"pelvis": (0, yaw, 0)}, base=open_)
    clip("CARTENON_WHEEL", 44, False, [(0, BASE), (10, open_), (15, open_), (16, spin(0)), (20, spin(-110)),
                                       (25, spin(-230)), (29, spin(-320)), (34, spin(-360)),
                                       (44, P({"pelvis": (0, -360, 0), "torso": (10, 0, 12)}))], smooth=False)

    # ---- D. Splitter Crescent (32): low sweep, rising crescents, a short overhead chop
    crouch = P({"@pelvis": (0, 3, 0), "knee_left": (35, 0, 0), "leg_left": (-24, 0, -7), "torso": (14, 0, 0)}, lr("low", (30, -60, -20), (-10, 0, 0)))
    sweep = P(lr("low", (-50, 50, 0), (-10, 0, 0)), base=crouch)
    low_mid = P(lr("mid", (-20, 0, -30)), base=sweep)
    rise = P({"@pelvis": (0, 1, 0), "torso": (2, 0, 0)}, lr("mid", (-170, 0, -40), (-10, 0, 0)), base=sweep)
    chop_wind = P(lr("top", (-170, 0, -20), (-30, 0, 0)), base=rise)
    chop = P({"torso": (20, 0, 0)}, lr("top", (-60, 10, 0), (-10, 0, 0)), base=rise)
    clip("SPLITTER_CRESCENT", 32, False, [(0, BASE), (7, crouch), (8, crouch), (13, sweep), (14, low_mid), (20, rise),
                                          (21, chop_wind), (24, chop), (32, BASE)], smooth=False)

    # ---- E. Piercing Sentence (26): the upper blades aligned, a thrust in a narrow corridor
    aim = P({"torso": (4, 0, 0), "head": (0, 0, 0)}, lr("top", (-90, 15, 0), (-60, 0, 0), (60, 0, 0)),
            lr("mid", (-20, -90, -60), (-40, 0, 0)), lr("low", (20, 0, -20), (-30, 0, 0)))
    lean = P({"torso": (24, 0, 0), "@pelvis": (0, 2, -1), "leg_left": (-35, 0, -6), "knee_left": (30, 0, 0),
              "wing_root_left": (0, -60, 0)}, base=aim)
    thrust = P(lr("top", (-95, 8, 0), (0, 0, 0), (0, 0, 0)), base=lean)
    third = P({"arm_mid_right": (-90, 10, 0), "elbow_mid_right": (0, 0, 0), "wrist_mid_right": (0, 0, 0)}, sym=False,
              base=thrust)
    clip("PIERCING_SENTENCE", 26, False, [(0, BASE), (8, aim), (12, lean), (14, thrust), (16, third), (19, third),
                                          (26, BASE)], smooth=False)

    # ---- F. Gargoyle Fall (56): crouch, leap, apex, dive, impact, recover
    load = P({"@pelvis": (0, 5, 0), "knee_left": (80, 0, 0), "leg_left": (-50, 0, -8), "ankle_left": (-30, 0, 0),
              "torso": (20, 0, 0)}, lr("top", (-170, 0, -20), (-20, 0, 0)), lr("mid", (-20, -90, -100), (-20, 0, 0)),
             lr("low", (-40, 30, -10), (-40, 0, 0)), WING_OPEN)
    leap = P({"@pelvis": (0, 0, 0), "knee_left": (10, 0, 0), "leg_left": (10, 0, -4), "ankle_left": (20, 0, 0),
              "torso": (-5, 0, 0)}, base=load)
    apex = P({"torso": (30, 0, 0), "knee_left": (60, 0, 0), "leg_left": (-40, 0, -4)},
             lr("top", (-120, 15, 0), (-10, 0, 0)), lr("mid", (-90, 20, 0), (-10, 0, 0)), lr("low", (-60, 20, 0), (-10, 0, 0)),
             base=leap)
    dive = P({"torso": (40, 0, 0)}, WING_HALF, base=apex)
    impact = P({"@pelvis": (0, 7, 0), "knee_left": (90, 0, 0), "leg_left": (-60, 0, -10), "ankle_left": (-30, 0, 0),
                "torso": (35, 0, 0)}, lr("top", (-70, 15, 0), (0, 0, 0), (40, 0, 0)),
               lr("mid", (-60, 20, -10), (0, 0, 0), (40, 0, 0)), lr("low", (-40, 20, -10), (0, 0, 0), (40, 0, 0)), base=dive)
    flap = P({"wing_root_left": (0, -10, -20)}, sym=False, base=impact)
    clip("GARGOYLE_FALL", 56, False, [(0, BASE), (8, load), (16, leap), (17, apex), (23, apex), (31, dive), (33, impact),
                                      (43, impact), (48, flap), (56, BASE)], smooth=False)


presentation()
locomotion()
defence()
attacks()
idles()
transition()
