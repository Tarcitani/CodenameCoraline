"""Reproducible baby geometry, unique UV packing, five-color skins and emissive eyes."""
from pathlib import Path
import json
import math
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/codenamecoraline/textures'
SIZE = 128
PARTS = []
BOXES = []

def part(name, parent, pivot, rotation=(0,0,0)):
    PARTS.append(dict(name=name,parent=parent,pivot=pivot,rotation=rotation))

def box(name, joint, origin, size, material='primary', inflate=0):
    BOXES.append(dict(name=name,joint=joint,origin=origin,size=size,material=material,inflate=inflate))

def geometry():
    part('torso','root',(0,19,0))
    box('torso','torso',(-3,-3,-3.5),(6,6,7))
    part('belly','torso',(0,0,0))
    box('belly','belly',(-2,-1.5,-3.65),(4,4,1),'secondary',.25)
    part('neck_base','torso',(0,-1,-3))
    box('neck_base','neck_base',(-2,-2,-1),(4,3,3))
    part('neck_mid','neck_base',(0,-1,-1))
    box('neck_mid','neck_mid',(-1.5,-2,-2),(3,3,3))
    part('neck_top','neck_mid',(0,-1,-1))
    box('neck_top','neck_top',(-1,-1,-1),(2,2,2))
    part('head','neck_top',(0,0,-1))
    box('head','head',(-3.5,-2.5,-4),(7,5,6))
    box('snout','head',(-3,-.25,-6),(6,2,3))
    part('jaw','head',(0,1.65,-1.5))
    box('jaw','jaw',(-2.5,0,-4.25),(5,1,4),'secondary')
    for side, sign in [('left',1),('right',-1)]:
        part(f'{side}_eye','head',(sign*3.45,-.7,-2.3))
        box(f'{side}_cheek',f'{side}_eye',(-.5,-1.5,-1.5),(1,3,3),'primary')
        box(f'{side}_eye_front','head',(sign*1.65-1,-2.3,-4.2),(2,2,.5),'eye')
        part(f'{side}_horn','head',(sign*2.4,-2,0),(-25,0,sign*18))
        box(f'{side}_horn',f'{side}_horn',(-.75,-2.5,-.75),(1.5,3,1.5),'accent')
        part(f'{side}_horn_tip',f'{side}_horn',(0,-2.5,0),(-22,0,sign*15))
        box(f'{side}_horn_tip',f'{side}_horn_tip',(-.5,-1.5,-.5),(1,2,1),'accent')
        box(f'{side}_fang','head',(sign*2-.5,1.4,-4.4),(1,1,1),'ivory')
        part(f'{side}_wing_upper','torso',(sign*2.8,-2,1),(0,-sign*65,-sign*40))
        x=0 if sign==1 else -3
        box(f'{side}_wing_upper',f'{side}_wing_upper',(x,-.5,-.5),(3,1,1),'accent')
        for step,depth in enumerate([3,2.5,2]):
            xx=step if sign==1 else -step-1
            box(f'{side}_membrane_inner_{step}',f'{side}_wing_upper',(xx,0,.25),(1,.5,depth),'secondary')
        part(f'{side}_wing_forearm',f'{side}_wing_upper',(sign*3,0,0),(0,sign*18,sign*8))
        box(f'{side}_wing_forearm',f'{side}_wing_forearm',(x,-.5,-.5),(3,1,1),'primary')
        for step,depth in enumerate([4,3,2]):
            xx=step if sign==1 else -step-1
            box(f'{side}_membrane_outer_{step}',f'{side}_wing_forearm',(xx,0,0),(1,.5,depth),'secondary')
        part(f'{side}_wing_tip',f'{side}_wing_forearm',(sign*3,0,0),(0,sign*18,0))
        box(f'{side}_wing_tip',f'{side}_wing_tip',(0 if sign==1 else -2,-.25,0),(2,.5,2),'secondary')
        for which,z in [('front',-2.1),('hind',2.2)]:
            name=f'{side}_{which}'
            hind=which=='hind'
            part(f'{name}_upper','torso',(sign*2.5,1,z))
            box(f'{name}_upper',f'{name}_upper',(-1,-.5,-1),(2,2,2) if not hind else (2,3,3))
            part(f'{name}_lower',f'{name}_upper',(0,1.5,0))
            box(f'{name}_lower',f'{name}_lower',(-.75,0,-.75),(1.5,1.5,1.5))
            part(f'{name}_foot',f'{name}_lower',(0,1.5,-.5))
            box(f'{name}_foot',f'{name}_foot',(-1,0,-1.3),(2,1,2.5),'secondary')
            box(f'{name}_claw',f'{name}_foot',(-.75,.35,-1.7),(1.5,.5,1),'accent')
    for i in range(1,5):
        parent='torso' if i==1 else f'tail_{i-1}'
        part(f'tail_{i}',parent,(0,0,3 if i==1 else 2.5),(-8 if i==1 else -4,7 if i==3 else 0,0))
        width=3 if i==1 else 2 if i==2 else 1.5 if i==3 else 1
        box(f'tail_{i}',f'tail_{i}',(-width/2,-width/2,0),(width,width,3))
        box(f'tail_crest_{i}',f'tail_{i}',(-.5,-width/2-1,1),(1,1.5,1.5),'accent')
    for i in range(3):
        part(f'crest_{i}','torso',(0,-3, i*2-1))
        box(f'crest_{i}',f'crest_{i}',(-.5,-1,0),(1,1.5,1.5),'accent')


def pack():
    x=y=row=0
    occupied=set()
    for b in BOXES:
        w,h,d=[math.ceil(v) for v in b['size']]
        rw,rh=2*(w+d),h+d
        if x+rw>SIZE:
            x=0; y+=row+1; row=0
        assert y+rh<=SIZE, ('Atlas overflow',b['name'],y,rh)
        b['uv']=[x,y]
        for yy in range(y,y+rh):
            for xx in range(x,x+rw):
                assert (xx,yy) not in occupied, b['name']
                occupied.add((xx,yy))
        x+=rw+1; row=max(row,rh)
    print(f'UV OK: {len(BOXES)} unique cube rectangles, no overlap, atlas {SIZE}x{SIZE}')

PALETTES=[(40,43,48),(226,221,204),(176,38,28),(225,100,25),(219,169,53)]

def tones(rgb):
    return [tuple(max(0,min(255,round(c*m))) for c in rgb)+(255,) for m in (.47,.72,1,1.17)]

def skin(primary,secondary,accent):
    image=Image.new('RGBA',(SIZE,SIZE),(0,0,0,0)); glow=Image.new('RGBA',(SIZE,SIZE),(0,0,0,0))
    for b in BOXES:
        u,v=b['uv']; w,h,d=[math.ceil(n) for n in b['size']]
        mat=b['material']
        rgb=PALETTES[primary if mat=='primary' else secondary if mat=='secondary' else accent]
        if mat=='dark': rgb=(24,19,18)
        if mat=='ivory': rgb=(244,224,173)
        if mat=='eye': rgb=(242,174,45)
        if mat=='leather': rgb=(112,58,28)
        if mat=='leather_gold': rgb=(208,158,46)
        palette=tones(rgb)
        for yy in range(h+d):
            for xx in range(2*(w+d)):
                tone=3 if yy<d else 2
                if yy==h+d-1 or xx==0: tone=0
                elif yy%3==0 and (xx+yy//3)%4==0: tone=1
                image.putpixel((u+xx,v+yy),palette[tone])
        if b['name']=='snout':
            for xx in [1,4]: image.putpixel((u+d+xx,v+d),(38,20,15,255))
        if b['name']=='jaw':
            for xx in range(w): image.putpixel((u+d+xx,v+d),(65,35,19,255))
        if mat=='eye':
            # All faces receive the same broad dark pupil; no reversed/missing eye in side views.
            for xx in range(2*(w+d)):
                for yy in range(d,d+h):
                    color=(35,22,15,255) if yy==d+1 and xx%3==1 else (255,189,57,255)
                    if yy==d and xx%3==1: color=(255,247,211,255)
                    image.putpixel((u+xx,v+yy),color)
                    # Only highlights glow; eyes remain readable by daylight.
                    if color==(255,247,211,255): glow.putpixel((u+xx,v+yy),color)
    return image,glow

def young_geometry():
    PARTS.clear(); BOXES.clear()
    # Sleek adolescent dragon: higher stance (torso y=14), longer neck/snout, bigger wings, athletic legs, whip tail.
    part('torso', 'root', (0, 14, 0))
    box('torso', 'torso', (-3.5, -3, -5.5), (7, 6, 11))
    part('belly', 'torso', (0, 0, 0))
    box('belly', 'belly', (-2.5, -1.2, -5.6), (5, 4, 10), 'secondary', .25)
    part('neck_base', 'torso', (0, -1.5, -5.0))
    box('neck_base', 'neck_base', (-2, -2, -2.5), (4, 4, 4))
    part('neck_mid', 'neck_base', (0, -1.5, -2.5))
    box('neck_mid', 'neck_mid', (-1.75, -2, -3.5), (3.5, 3.5, 4.5))
    part('neck_top', 'neck_mid', (0, -1.2, -3.2))
    box('neck_top', 'neck_top', (-1.5, -1.5, -3.0), (3, 3, 4))
    part('head', 'neck_top', (0, -.5, -2.8))
    box('head', 'head', (-3, -2.5, -4.5), (6, 4.5, 6))
    box('snout', 'head', (-2.25, -.5, -8.5), (4.5, 2.5, 5))
    part('jaw', 'head', (0, 1.5, -2.0))
    box('jaw', 'jaw', (-2, 0, -6.2), (4, 1.5, 5.5), 'secondary')
    for side, sign in [('left', 1), ('right', -1)]:
        part(f'{side}_eye', 'head', (sign * 2.95, -.8, -2.8))
        box(f'{side}_cheek', f'{side}_eye', (-.5, -1.5, -1.5), (1, 3, 3), 'primary')
        box(f'{side}_eye_front', 'head', (sign * 1.45 - 1, -2.0, -4.7), (2, 1.5, .5), 'eye')
        part(f'{side}_horn', 'head', (sign * 2.2, -2.2, -.5), (-32, 0, sign * 20))
        box(f'{side}_horn', f'{side}_horn', (-.75, -4, -.75), (1.5, 4.5, 1.5), 'accent')
        part(f'{side}_horn_tip', f'{side}_horn', (0, -4, 0), (-28, 0, sign * 16))
        box(f'{side}_horn_tip', f'{side}_horn_tip', (-.5, -3.2, -.5), (1, 3.5, 1), 'accent')
        box(f'{side}_fang', 'head', (sign * 1.6 - .4, 1.4, -7.8), (.8, 1.2, .8), 'ivory')
        part(f'{side}_wing_upper', 'torso', (sign * 3.2, -2.2, -.5), (0, -sign * 65, -sign * 40))
        x = 0 if sign == 1 else -5
        box(f'{side}_wing_upper', f'{side}_wing_upper', (x, -.6, -.6), (5, 1.2, 1.2), 'accent')
        for step, depth in enumerate([5, 4.5, 4, 3.5]):
            xx = step * 1.25 if sign == 1 else -(step + 1) * 1.25
            box(f'{side}_membrane_inner_{step}', f'{side}_wing_upper', (xx, 0, .3), (1.25, .5, depth), 'secondary')
        part(f'{side}_wing_forearm', f'{side}_wing_upper', (sign * 5, 0, 0), (0, sign * 18, sign * 8))
        xf = 0 if sign == 1 else -6
        box(f'{side}_wing_forearm', f'{side}_wing_forearm', (xf, -.5, -.5), (6, 1, 1), 'primary')
        box(f'{side}_wing_claw', f'{side}_wing_forearm', (0 if sign == 1 else -1, -.8, -1.6), (1, 1, 1.5), 'accent')
        for step, depth in enumerate([6.5, 5.5, 4.5, 3.5]):
            xx = step * 1.5 if sign == 1 else -(step + 1) * 1.5
            box(f'{side}_membrane_outer_{step}', f'{side}_wing_forearm', (xx, 0, 0), (1.5, .5, depth), 'secondary')
        part(f'{side}_wing_tip', f'{side}_wing_forearm', (sign * 6, 0, 0), (0, sign * 18, 0))
        box(f'{side}_wing_tip', f'{side}_wing_tip', (0 if sign == 1 else -4, -.25, 0), (4, .5, 4), 'secondary')
        for which, z in [('front', -3.6), ('hind', 3.6)]:
            name = f'{side}_{which}'
            hind = which == 'hind'
            part(f'{name}_upper', 'torso', (sign * 3.1, 2, z))
            box(f'{name}_upper', f'{name}_upper', (-1.25, -.5, -1.25), (2.5, 4, 2.5) if not hind else (3, 4.5, 3.5))
            part(f'{name}_lower', f'{name}_upper', (0, 3.5, 0))
            box(f'{name}_lower', f'{name}_lower', (-1, 0, -1), (2, 3.5, 2))
            part(f'{name}_foot', f'{name}_lower', (0, 3.5, -.6))
            box(f'{name}_foot', f'{name}_foot', (-1.25, 0, -1.8), (2.5, 1, 3.5), 'secondary')
            box(f'{name}_claw', f'{name}_foot', (-1, .3, -2.4), (2, .6, 1.2), 'accent')
    for i in range(1, 5):
        parent = 'torso' if i == 1 else f'tail_{i-1}'
        part(f'tail_{i}', parent, (0, -.2 if i == 1 else 0, 5.2 if i == 1 else 4.2), (-6 if i == 1 else -3, 5 if i == 3 else 0, 0))
        width = 3.5 if i == 1 else 2.8 if i == 2 else 2.0 if i == 3 else 1.5
        length = 5 if i == 1 else 4.5
        box(f'tail_{i}', f'tail_{i}', (-width / 2, -width / 2, 0), (width, width, length))
        box(f'tail_crest_{i}', f'tail_{i}', (-.5, -width / 2 - 1.3, 1.2), (1, 1.8, 2), 'accent')
    box('tail_spade', 'tail_4', (-1.75, -.4, 3.5), (3.5, .8, 3.5), 'accent')
    for i in range(3):
        part(f'crest_{i}', 'torso', (0, -3, i * 3 - 3))
        box(f'crest_{i}', f'crest_{i}', (-.5, -1.5, 0), (1, 2, 2), 'accent')

def adult_geometry():
    PARTS.clear(); BOXES.clear()
    # Colossal adult Fire Dragon sculpted from the turnaround & detail reference sheet:
    # - Broad muscular chest tapering to athletic hindquarters, protected by overlapping throat/breast/belly scutes.
    # - Proud S-curved neck (-26 deg base, -14 deg mid, +16 deg top, +24 deg head -> 0 deg net predatory gaze).
    # - Fierce angular skull with heavy brow ridges, stepped classic snout, nasal horn, sagittal crest horns,
    #   long swept primary crown horns, secondary outer horns, 3-tiered cheek spines, and strong fanged jaw.
    # - Continuous fiery dorsal spine ridge from nape to tail tip, ending in flared lateral flame blades.
    # - Massive bat-like wings with defined arm/forearm/finger bone struts, hooked elbow claws, and wide membranes.
    # - Powerful reptilian legs with outer armor plates, heel spurs, and 3 distinct curved forward talons (ground y=24).
    part('torso', 'root', (0, 8.0, 0))
    box('torso_chest', 'torso', (-5.0, -4.4, -7.8), (10.0, 8.6, 8.4), 'primary')
    box('torso_mid', 'torso', (-4.4, -4.0, 0.4), (8.8, 7.8, 4.2), 'primary')
    box('torso_pelvis', 'torso', (-4.0, -3.6, 4.4), (8.0, 7.2, 3.8), 'primary')
    box('dorsal_plate_front', 'torso', (-3.4, -4.9, -7.4), (6.8, 1.0, 7.8), 'armor_plate')
    box('dorsal_plate_rear', 'torso', (-2.8, -4.5, 0.4), (5.6, 1.0, 7.4), 'armor_plate')
    box('torso_spine_a', 'torso', (-0.55, -7.2, -6.8), (1.1, 2.8, 1.8), 'ember_spine')
    box('torso_spine_b', 'torso', (-0.55, -6.9, 2.4), (1.1, 2.6, 2.0), 'ember_spine')
    box('torso_spine_c', 'torso', (-0.5, -6.4, 5.4), (1.0, 2.4, 1.8), 'ember_spine')

    # Overlapping breast & belly armor scutes (animates with breathing)
    part('belly', 'torso', (0, 0, 0))
    box('breast_scute_top', 'belly', (-3.6, -2.6, -8.5), (7.2, 3.2, 1.6), 'scute')
    box('breast_scute_low', 'belly', (-4.0, 0.4, -8.3), (8.0, 3.8, 2.4), 'scute')
    box('belly_scute_1', 'belly', (-3.8, 2.3, -6.2), (7.6, 2.4, 5.2), 'scute', .15)
    box('belly_scute_2', 'belly', (-3.4, 2.0, -1.2), (6.8, 2.2, 5.0), 'scute', .10)
    box('belly_scute_3', 'belly', (-2.9, 1.7, 3.6), (5.8, 2.0, 4.2), 'scute')

    # Sculpted 3D Dragon-Rider Saddle (toggled visible only when saddle is equipped in slot 0)
    part('saddle', 'torso', (0, -5.0, -2.0))
    box('saddle_pad', 'saddle', (-5.3, -0.4, -4.3), (10.6, 1.2, 8.6), 'leather', .10)
    box('saddle_seat', 'saddle', (-3.9, -1.6, -3.5), (7.8, 1.4, 7.0), 'leather', .12)
    box('saddle_pommel', 'saddle', (-3.0, -3.8, -4.1), (6.0, 2.6, 1.6), 'leather_gold')
    box('saddle_cantle', 'saddle', (-3.2, -3.4, 2.5), (6.4, 2.2, 1.6), 'leather_gold')
    box('saddle_strap_l', 'saddle', (5.05, 0.2, -2.0), (.7, 6.2, 4.0), 'leather')
    box('saddle_strap_r', 'saddle', (-5.75, 0.2, -2.0), (.7, 6.2, 4.0), 'leather')
    box('saddle_buckle_l', 'saddle', (5.15, 2.4, -1.1), (.8, 1.6, 2.2), 'leather_gold')
    box('saddle_buckle_r', 'saddle', (-5.95, 2.4, -1.1), (.8, 1.6, 2.2), 'leather_gold')
    box('saddle_stirrup_l', 'saddle', (5.2, 6.0, -0.8), (.6, 1.8, 1.6), 'leather_gold')
    box('saddle_stirrup_r', 'saddle', (-5.8, 6.0, -0.8), (.6, 1.8, 1.6), 'leather_gold')

    # Proud S-curved muscular neck with overlapping throat scutes & twin fiery nape spines per joint
    part('neck_base', 'torso', (0, -2.2, -7.2), (-26, 0, 0))
    box('neck_base', 'neck_base', (-3.0, -2.6, -4.8), (6.0, 5.2, 5.4), 'primary')
    box('neck_base_armor', 'neck_base', (-2.2, -3.1, -4.6), (4.4, 1.0, 4.8), 'armor_plate')
    box('neck_base_scute_1', 'neck_base', (-2.4, 0.6, -5.2), (4.8, 2.3, 2.6), 'scute')
    box('neck_base_scute_2', 'neck_base', (-2.6, 1.1, -2.6), (5.2, 2.2, 2.6), 'scute')
    box('neck_base_spine_1', 'neck_base', (-0.55, -5.4, -4.3), (1.1, 2.6, 1.8), 'ember_spine')
    box('neck_base_spine_2', 'neck_base', (-0.6, -5.2, -2.1), (1.2, 2.5, 1.8), 'ember_spine')

    part('neck_mid', 'neck_base', (0, -1.6, -4.2), (-14, 0, 0))
    box('neck_mid', 'neck_mid', (-2.5, -2.3, -5.0), (5.0, 4.6, 5.4), 'primary')
    box('neck_mid_armor', 'neck_mid', (-1.9, -2.8, -4.8), (3.8, 0.9, 4.8), 'armor_plate')
    box('neck_mid_scute_1', 'neck_mid', (-2.0, 0.6, -5.3), (4.0, 2.0, 2.6), 'scute')
    box('neck_mid_scute_2', 'neck_mid', (-2.2, 0.9, -2.7), (4.4, 2.0, 2.6), 'scute')
    box('neck_mid_spine_1', 'neck_mid', (-0.5, -5.0, -4.4), (1.0, 2.5, 1.8), 'ember_spine')
    box('neck_mid_spine_2', 'neck_mid', (-0.5, -4.8, -2.2), (1.0, 2.4, 1.8), 'ember_spine')

    part('neck_top', 'neck_mid', (0, -1.4, -4.4), (16, 0, 0))
    box('neck_top', 'neck_top', (-2.1, -2.0, -4.4), (4.2, 4.0, 4.8), 'primary')
    box('neck_top_armor', 'neck_top', (-1.6, -2.4, -4.2), (3.2, 0.8, 4.2), 'armor_plate')
    box('neck_top_scute', 'neck_top', (-1.7, 0.6, -4.6), (3.4, 1.8, 4.4), 'scute')
    box('neck_top_spine_1', 'neck_top', (-0.45, -4.4, -3.9), (0.9, 2.2, 1.6), 'ember_spine')
    box('neck_top_spine_2', 'neck_top', (-0.45, -4.2, -1.9), (0.9, 2.1, 1.6), 'ember_spine')

    # Fierce angular skull with heavy brow ridges, classic snout, central sagittal crest, swept horns & cheek spines
    part('head', 'neck_top', (0, -0.5, -3.8), (24, 0, 0))
    box('head', 'head', (-3.6, -3.0, -5.4), (7.2, 5.0, 6.2), 'primary')
    box('head_crown_plate', 'head', (-2.8, -3.5, -5.0), (5.6, 1.0, 5.6), 'armor_plate')
    box('snout', 'head', (-2.6, -1.6, -10.2), (5.2, 3.0, 5.2), 'primary')
    box('snout_tip', 'head', (-2.0, -1.2, -12.6), (4.0, 2.6, 2.6), 'primary')
    box('snout_ridge', 'head', (-1.4, -2.1, -11.4), (2.8, 0.8, 6.4), 'armor_plate')
    box('nasal_horn', 'head', (-0.5, -3.3, -12.2), (1.0, 2.2, 1.4), 'ember_horn')
    box('central_crest_front', 'head', (-0.5, -5.2, -4.2), (1.0, 2.2, 2.4), 'ember_spine')
    box('central_crest_main', 'head', (-0.6, -6.8, -1.8), (1.2, 3.8, 2.6), 'ember_spine')

    part('jaw', 'head', (0, 1.6, -2.4))
    box('jaw', 'jaw', (-2.4, 0.0, -7.2), (4.8, 1.8, 6.6), 'secondary')
    box('jaw_tip', 'jaw', (-1.7, 0.0, -9.8), (3.4, 1.5, 2.8), 'secondary')
    box('jaw_chin_scute', 'jaw', (-1.8, 1.4, -8.4), (3.6, 0.8, 6.8), 'scute')

    for side, sign in [('left', 1), ('right', -1)]:
        part(f'{side}_eye', 'head', (sign * 3.4, -1.1, -3.8))
        box(f'{side}_cheek', f'{side}_eye', (-0.7, -1.4, -2.0), (1.4, 3.2, 4.0), 'primary')
        # Angled menacing brow ridge & glowing supernatural eye socket (visible from front and profile)
        box(f'{side}_brow_ridge', 'head', (sign * 2.2 - (0 if sign == 1 else 1.8), -2.9, -6.8), (1.8, 0.9, 3.8), 'armor_plate')
        box(f'{side}_eye_front', 'head', (sign * 1.95 - (0 if sign == 1 else 1.6), -2.1, -6.4), (1.6, 1.4, 2.2), 'eye')
        # 3-tiered lateral cheek & occipital spines
        box(f'{side}_frill_top', 'head', (sign * 3.3 - (0 if sign == 1 else 2.2), -1.8, -1.2), (2.2, 1.0, 3.6), 'ember_spine')
        box(f'{side}_frill_mid', 'head', (sign * 3.1 - (0 if sign == 1 else 2.4), -0.4, -0.6), (2.4, 1.0, 4.0), 'ember_spine')
        box(f'{side}_frill_low', 'head', (sign * 2.7 - (0 if sign == 1 else 2.0), 1.0, -1.0), (2.0, 1.0, 3.4), 'ember_spine')
        # Primary long swept-back crown horns + secondary outer temporal horns
        part(f'{side}_horn', 'head', (sign * 2.5, -2.8, -0.6), (-42, 0, sign * 22))
        box(f'{side}_horn', f'{side}_horn', (-1.0, -6.2, -1.0), (2.0, 6.8, 2.0), 'ember_horn')
        box(f'{side}_sub_horn', f'{side}_horn', (sign * 0.9 - (0 if sign == 1 else 1.3), -4.2, 0.8), (1.3, 5.2, 1.4), 'ember_horn')
        part(f'{side}_horn_tip', f'{side}_horn', (0, -6.0, 0), (-26, 0, sign * 14))
        box(f'{side}_horn_tip', f'{side}_horn_tip', (-0.7, -6.4, -0.7), (1.4, 6.8, 1.4), 'ember_spine')
        box(f'{side}_sub_horn_tip', f'{side}_horn_tip', (sign * 0.8 - (0 if sign == 1 else 0.9), -3.6, 1.1), (0.9, 4.6, 1.0), 'ember_spine')
        # Upper & lower predatory fangs
        box(f'{side}_fang_front', 'head', (sign * 1.45 - .4, 1.3, -11.8), (.8, 1.5, .8), 'ivory')
        box(f'{side}_fang_mid', 'head', (sign * 1.95 - .45, 1.3, -8.8), (.9, 1.6, .9), 'ivory')
        box(f'{side}_fang_low', 'jaw', (sign * 1.35 - .35, -1.1, -9.0), (.7, 1.2, .7), 'ivory')

        # Massive bat-like wings with defined bone struts, curved elbow claws & fiery-rimmed membranes
        part(f'{side}_wing_upper', 'torso', (sign * 4.6, -3.4, -2.2), (0, -sign * 65, -sign * 40))
        x = 0 if sign == 1 else -9.0
        box(f'{side}_wing_upper', f'{side}_wing_upper', (x, -1.0, -1.0), (9.0, 2.0, 2.0), 'armor_plate')
        box(f'{side}_wing_shoulder_spur', f'{side}_wing_upper', (0 if sign == 1 else -2.2, -1.3, -1.4), (2.2, 1.6, 2.2), 'ember_horn')
        for step, depth in enumerate([11.0, 10.2, 9.4, 8.6]):
            xx = step * 2.25 if sign == 1 else -(step + 1) * 2.25
            box(f'{side}_membrane_inner_{step}', f'{side}_wing_upper', (xx, 0, .4), (2.25, .5, depth), 'wing_membrane')

        part(f'{side}_wing_forearm', f'{side}_wing_upper', (sign * 9.0, 0, 0), (0, sign * 18, sign * 8))
        xf = 0 if sign == 1 else -12.0
        box(f'{side}_wing_forearm', f'{side}_wing_forearm', (xf, -0.85, -0.85), (12.0, 1.7, 1.7), 'armor_plate')
        box(f'{side}_wing_claw', f'{side}_wing_forearm', (0 if sign == 1 else -1.6, -1.3, -3.2), (1.6, 1.6, 2.8), 'ember_horn')
        box(f'{side}_wing_claw_hook', f'{side}_wing_forearm', (0.2 if sign == 1 else -1.3, -1.0, -4.8), (1.1, 1.1, 1.8), 'ember_spine')
        box(f'{side}_wing_strut_a', f'{side}_wing_forearm', (sign * 3.5 - (0 if sign == 1 else 1.0), -0.45, 0.5), (1.0, 0.9, 10.8), 'wing_bone')
        box(f'{side}_wing_strut_b', f'{side}_wing_forearm', (sign * 8.0 - (0 if sign == 1 else 1.0), -0.45, 0.5), (1.0, 0.9, 9.2), 'wing_bone')
        for step, depth in enumerate([12.5, 11.2, 9.8, 8.4]):
            xx = step * 3.0 if sign == 1 else -(step + 1) * 3.0
            box(f'{side}_membrane_outer_{step}', f'{side}_wing_forearm', (xx, 0, 0), (3.0, .5, depth), 'wing_membrane')

        part(f'{side}_wing_tip', f'{side}_wing_forearm', (sign * 12.0, 0, 0), (0, sign * 18, 0))
        box(f'{side}_wing_tip_bone', f'{side}_wing_tip', (0 if sign == 1 else -8.5, -0.6, -0.6), (8.5, 1.2, 1.2), 'wing_bone')
        box(f'{side}_wing_strut_c', f'{side}_wing_tip', (sign * 3.5 - (0 if sign == 1 else 0.9), -0.4, 0.4), (0.9, 0.8, 6.8), 'wing_bone')
        for step, depth in enumerate([7.6, 5.4]):
            xx = step * 4.25 if sign == 1 else -(step + 1) * 4.25
            box(f'{side}_wing_tip_{step}', f'{side}_wing_tip', (xx, -0.25, 0), (4.25, .5, depth), 'wing_membrane')

        # Powerful reptilian legs with outer scale plates, heel spurs & 3 individual curved talons (y=8+2.8+5.2+6.0+2.0=24.0)
        for which, z in [('front', -5.2), ('hind', 4.8)]:
            name = f'{side}_{which}'
            hind = which == 'hind'
            part(f'{name}_upper', 'torso', (sign * 4.5, 2.8, z))
            box(f'{name}_upper', f'{name}_upper', (-2.0, -1.2, -2.0), (4.0, 6.4, 4.2) if not hind else (4.4, 6.8, 5.2), 'primary')
            box(f'{name}_plate', f'{name}_upper', (sign * 1.4 - (0 if sign == 1 else 1.0), -0.8, -1.6), (1.0, 5.2, 3.4), 'armor_plate')
            part(f'{name}_lower', f'{name}_upper', (0, 5.2, 0.2))
            box(f'{name}_lower', f'{name}_lower', (-1.5, 0, -1.5), (3.0, 6.0, 3.2), 'primary')
            box(f'{name}_shin_scute', f'{name}_lower', (-1.2, 0.4, -1.8), (2.4, 5.0, 0.8), 'armor_plate')
            box(f'{name}_heel_spur', f'{name}_lower', (-0.5, 1.2, 1.4), (1.0, 1.8, 1.6), 'ember_spine')
            part(f'{name}_foot', f'{name}_lower', (0, 6.0, -0.8))
            box(f'{name}_foot', f'{name}_foot', (-2.0, 0, -2.6), (4.0, 2.0, 4.8), 'armor_plate')
            box(f'{name}_claw_in', f'{name}_foot', (-1.85, .4, -4.6), (1.0, 1.6, 2.4), 'ember_horn')
            box(f'{name}_claw_mid', f'{name}_foot', (-0.5, .3, -5.0), (1.0, 1.7, 2.8), 'ember_horn')
            box(f'{name}_claw_out', f'{name}_foot', (0.85, .4, -4.6), (1.0, 1.6, 2.4), 'ember_horn')

    # Long muscular flexible tail with ventral scutes, twin dorsal spines per segment & flaming tip blades
    for i in range(1, 5):
        parent = 'torso' if i == 1 else f'tail_{i-1}'
        part(f'tail_{i}', parent, (0, -.3 if i == 1 else 0, 7.6 if i == 1 else 6.8), (-6 if i == 1 else -3, 4 if i == 3 else 0, 0))
        width = 4.8 if i == 1 else 3.8 if i == 2 else 2.8 if i == 3 else 2.0
        length = 7.6 if i == 1 else 7.4 if i == 2 else 7.2 if i == 3 else 7.0
        box(f'tail_{i}', f'tail_{i}', (-width / 2, -width / 2, 0), (width, width, length), 'primary')
        box(f'tail_scute_{i}', f'tail_{i}', (-width * 0.38, width * 0.25, 0.2), (width * 0.76, 0.9, length - 0.4), 'scute')
        box(f'tail_crest_a_{i}', f'tail_{i}', (-.6, -width / 2 - 2.2, 1.0), (1.2, 2.6, 2.2), 'ember_spine')
        box(f'tail_crest_b_{i}', f'tail_{i}', (-.55, -width / 2 - 2.0, 4.0), (1.1, 2.4, 2.2), 'ember_spine')
    box('tail_3_blade_l', 'tail_3', (1.2, -0.4, 2.2), (2.2, 0.8, 4.4), 'ember_spine')
    box('tail_3_blade_r', 'tail_3', (-3.4, -0.4, 2.2), (2.2, 0.8, 4.4), 'ember_spine')
    box('tail_4_blade_l', 'tail_4', (0.8, -0.45, 1.0), (3.0, 0.9, 5.6), 'ember_spine')
    box('tail_4_blade_r', 'tail_4', (-3.8, -0.45, 1.0), (3.0, 0.9, 5.6), 'ember_spine')
    box('tail_spade', 'tail_4', (-1.4, -0.5, 6.2), (2.8, 1.0, 4.4), 'ember_spine')
    for i in range(3):
        z_crest = -6.0 if i == 0 else (3.6 if i == 1 else 6.0)
        part(f'crest_{i}', 'torso', (0, -4.6, z_crest), (-18 - i * 2, 0, 0))
        box(f'crest_{i}', f'crest_{i}', (-.6, -3.2 + i * 0.2, -0.8), (1.2, 3.6 - i * 0.2, 2.2), 'ember_spine')


def lerp_rgb(c0, c1, t):
    t = max(0.0, min(1.0, t))
    return (
        int(round(c0[0] + (c1[0] - c0[0]) * t)),
        int(round(c0[1] + (c1[1] - c0[1]) * t)),
        int(round(c0[2] + (c1[2] - c0[2]) * t)),
        255,
    )


def scale_rgb(rgb, mult):
    return tuple(max(0, min(255, int(round(c * mult)))) for c in rgb[:3]) + (255,)


def adult_skin(primary, secondary, accent):
    """Uses the exact 5-color Baby/Young palette (Black, White, Red, Orange, Gold) with detailed scale, scute & membrane shading."""
    image = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    glow = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    p_base = PALETTES[primary]
    s_base = PALETTES[secondary]
    a_base = PALETTES[accent]
    a_low = scale_rgb(a_base, 0.58)[:3]
    a_mid = a_base
    a_hot = scale_rgb(a_base, 1.28)[:3]

    for b in BOXES:
        u, v = b['uv']
        w, h, d = [math.ceil(n) for n in b['size']]
        mat = b['material']
        bname = b['name']

        for yy in range(h + d):
            for xx in range(2 * (w + d)):
                is_top = yy < d
                rel_y = (yy / max(1, d - 1)) if is_top else ((yy - d) / max(1, h - 1))

                if mat in ('primary', 'armor_plate'):
                    # Interlocking reptile scales in exact PrimaryColor with 3D bevel & subtle accent seams
                    base = scale_rgb(p_base, 1.10 if mat == 'armor_plate' else 1.0)[:3]
                    sx = (xx + (yy // 2) * 2) % 4
                    sy = yy % 2
                    grain = (((xx * 13 + yy * 7) % 11) - 5) * 0.015
                    if sy == 1 and sx in (0, 3):
                        col = scale_rgb(base, 0.50 + grain)
                    elif sy == 0 and sx in (1, 2):
                        col = scale_rgb(base, (1.20 if is_top else 1.12) + grain)
                    else:
                        col = scale_rgb(base, (1.04 if is_top else 0.92 - rel_y * 0.14) + grain)
                    image.putpixel((u + xx, v + yy), col)

                elif mat in ('scute', 'secondary'):
                    # Overlapping horizontal chest, throat & belly armor plates in exact SecondaryColor
                    band = yy % 3
                    chevron = abs((xx % max(2, w)) - (w / 2.0)) / max(1.0, w / 2.0)
                    if band == 0:
                        col = scale_rgb(s_base, 0.54)
                    elif band == 1:
                        col = scale_rgb(s_base, 1.18 - chevron * 0.08)
                    else:
                        col = scale_rgb(s_base, 0.96 - chevron * 0.12)
                    image.putpixel((u + xx, v + yy), col)

                elif mat == 'wing_membrane':
                    # Wing membrane in SecondaryColor shaded darker near the bone and brighter with veins toward the edge
                    local_z = (yy % max(1, d)) / max(1, d - 1) if is_top else rel_y
                    vein_wave = abs(math.sin(xx * 0.85 + yy * 0.55) * math.cos(xx * 0.35 - yy * 0.7))
                    col = lerp_rgb(scale_rgb(s_base, 0.56)[:3], scale_rgb(s_base, 1.08)[:3], local_z)
                    if vein_wave > 0.78 and local_z > 0.18:
                        col = lerp_rgb(col[:3], a_hot, 0.35)
                    image.putpixel((u + xx, v + yy), col)

                elif mat == 'wing_bone':
                    edge = xx == 0 or yy == 0 or yy == h + d - 1
                    col = scale_rgb(p_base, 0.58 if edge else 0.92)
                    image.putpixel((u + xx, v + yy), col)

                elif mat in ('ember_spine', 'ember_horn'):
                    # Horns, cheek frills, dorsal spines & tail blades in exact AccentColor (ribbed dark base -> bright tip)
                    tip_factor = (1.0 - rel_y) if not is_top else 0.85
                    if 'blade' in bname or 'spade' in bname or 'frill' in bname:
                        tip_factor = max(tip_factor, (xx % max(2, d)) / max(1, d - 1))
                    rib = 0.84 if (yy % 2 == 0 and mat == 'ember_horn') else 1.0
                    col = lerp_rgb(a_low, a_mid if tip_factor < 0.65 else a_hot, tip_factor)
                    col = scale_rgb(col[:3], rib)
                    image.putpixel((u + xx, v + yy), col)

                elif mat == 'ivory':
                    col = lerp_rgb((184, 160, 126), (248, 236, 202), 1.0 - rel_y * 0.6)
                    image.putpixel((u + xx, v + yy), col)

                elif mat == 'leather':
                    # Rich stitched dark saddle leather
                    stitch = (xx % 4 == 0 and (yy == d + 1 or yy == h + d - 2))
                    col = (164, 114, 62, 255) if stitch else scale_rgb((116, 56, 26), 0.72 if (xx == 0 or yy == h + d - 1) else 1.08 if is_top else 0.92)
                    image.putpixel((u + xx, v + yy), col)

                elif mat == 'leather_gold':
                    # Polished gold trim, pommel, cantle, buckles & stirrups
                    col = scale_rgb((226, 174, 48), 0.74 if (xx == 0 or yy == h + d - 1) else 1.18 if is_top else 0.98)
                    image.putpixel((u + xx, v + yy), col)

                elif mat == 'eye':
                    # Bright glowing ember eyes matching Baby/Young readability
                    for_pupil = not is_top and (xx % 3 == 1) and (d <= yy < d + h)
                    if for_pupil:
                        col = (255, 247, 211, 255) if yy == d else (35, 22, 15, 255)
                    else:
                        col = (255, 189, 57, 255)
                    image.putpixel((u + xx, v + yy), col)
                    if col == (255, 247, 211, 255) or col == (255, 189, 57, 255):
                        glow.putpixel((u + xx, v + yy), col)

        if bname == 'snout_tip':
            for xx in [1, max(1, w - 2)]:
                if d + 1 < h + d:
                    image.putpixel((u + d + xx, v + d + 1), (32, 18, 15, 255))
    return image, glow


def write_geometry(class_name='FireDragonGeometry', atlas_size=128, write_sketch_json=True):
    lines=['package com.codenamecoraline.client.firedragon;','',
        'import net.minecraft.client.model.geom.PartPose;',
        'import net.minecraft.client.model.geom.builders.*;','',
        '/** Generated by tools/generate_fire_dragon_assets.py; edit geometry there. */',
        f'public final class {class_name} {{',
        f'    private {class_name}() {{}}',
        '    public static LayerDefinition createBodyLayer() {',
        '        MeshDefinition mesh = new MeshDefinition();',
        '        PartDefinition root = mesh.getRoot();']
    for p in PARTS:
        cubes=[b for b in BOXES if b['joint']==p['name']]
        builder='CubeListBuilder.create()'
        for b in cubes:
            vals=', '.join(f'{float(v)}F' for v in b['origin']+b['size'])
            builder+=f'.texOffs({b["uv"][0]}, {b["uv"][1]}).addBox({vals}, new CubeDeformation({float(b["inflate"])}F))'
        pivot=', '.join(f'{float(v)}F' for v in p['pivot'])
        rot=', '.join(f'{math.radians(v):.8f}F' for v in p['rotation'])
        lines.append(f'        PartDefinition {p["name"]} = {p["parent"]}.addOrReplaceChild("{p["name"]}", {builder}, PartPose.offsetAndRotation({pivot}, {rot}));')
    lines+=[f'        return LayerDefinition.create(mesh, {atlas_size}, {atlas_size});','    }','}']
    target=ROOT/f'src/main/java/com/codenamecoraline/client/firedragon/{class_name}.java'
    target.parent.mkdir(parents=True,exist_ok=True); target.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    if write_sketch_json:
        out=ROOT/'build/fire-dragon-sketches'; out.mkdir(parents=True,exist_ok=True)
        (out/'geometry.json').write_text(json.dumps(dict(parts=PARTS,boxes=BOXES)),encoding='utf-8')

def write_fireball_textures(folder):
    base = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    glow = Image.new('RGBA', (64, 64), (0, 0, 0, 0))

    def put(x, y, col, gcol=None):
        if 0 <= x < 64 and 0 <= y < 64:
            base.putpixel((x, y), col)
            if gcol is not None:
                glow.putpixel((x, y), gcol)

    def fire_color(t, alpha=255):
        # t in [0..1]: 0=white-hot core, 0.35=bright yellow, 0.65=fiery orange, 1.0=deep ember red
        stops = [
            (0.00, (255, 254, 232)),
            (0.28, (255, 232, 84)),
            (0.58, (255, 148, 22)),
            (0.82, (232, 62, 12)),
            (1.00, (148, 22, 8)),
        ]
        t = max(0.0, min(1.0, t))
        for i in range(len(stops) - 1):
            t0, c0 = stops[i]
            t1, c1 = stops[i + 1]
            if t <= t1:
                f = (t - t0) / max(1e-6, t1 - t0)
                return (
                    int(c0[0] + (c1[0] - c0[0]) * f),
                    int(c0[1] + (c1[1] - c0[1]) * f),
                    int(c0[2] + (c1[2] - c0[2]) * f),
                    alpha,
                )
        return (148, 22, 8, alpha)

    # 1. Core (0,0) 16x8: white-hot incandescent
    for y in range(0, 8):
        for x in range(0, 16):
            n = ((x * 11 + y * 7) % 13) / 13.0
            put(x, y, fire_color(0.05 + n * 0.22, 255), fire_color(0.02 + n * 0.12, 220))

    # 2. Inner shell (0,10) 20x10: bright yellow-gold swirling flame
    for y in range(10, 20):
        for x in range(0, 20):
            n = ((x * 7 + y * 13) % 17) / 17.0
            a = 235 if n > 0.18 else 140
            put(x, y, fire_color(0.22 + n * 0.35, a), fire_color(0.18 + n * 0.25, 160))

    # 3. Outer shell (24,0) 24x12: turbulent orange flame with cutout windows
    for y in range(0, 12):
        for x in range(24, 48):
            wave = math.sin(x * 0.75 + y * 0.55) * 0.5 + 0.5
            n = ((x * 5 + y * 9) % 11) / 11.0
            if wave * 0.6 + n * 0.4 < 0.30:
                continue
            t = 0.42 + (1.0 - wave) * 0.42
            a = int(170 + 80 * wave)
            put(x, y, fire_color(t, a), fire_color(t * 0.85, int(a * 0.55)))

    # 4. Crown flame petals (0,22) 14x7: bright base -> fiery wispy tips
    for y in range(22, 29):
        for x in range(0, 14):
            prog = (y - 22) / 6.0
            n = ((x * 9 + y * 5) % 11) / 11.0
            if prog > 0.72 and n < 0.45:
                continue
            a = int(245 * (1.0 - prog * 0.45))
            put(x, y, fire_color(0.28 + prog * 0.62, a), fire_color(0.22 + prog * 0.5, int(a * 0.6)))

    # 5. Tail segments: base (24,14 20x10), mid (24,26 18x9), tip (24,37 14x7)
    for ox, oy, w, h, t0, t1, a0 in [
        (24, 14, 20, 10, 0.30, 0.68, 225),
        (24, 26, 18, 9, 0.50, 0.88, 195),
        (24, 37, 14, 7, 0.68, 1.00, 165),
    ]:
        for dy in range(h):
            for dx in range(w):
                prog = dy / max(1, h - 1)
                n = ((dx * 7 + dy * 11) % 13) / 13.0
                if prog > 0.75 and n < 0.35:
                    continue
                t = t0 + (t1 - t0) * prog + (n - 0.5) * 0.12
                a = int(a0 * (1.0 - prog * 0.38))
                put(ox + dx, oy + dy, fire_color(t, a), fire_color(t * 0.9, int(a * 0.5)))

    # 6. Crossed comet flame fins (0,46) 52x16: wide glowing head tapering into ragged comet tail
    for dy in range(16):
        prog = dy / 15.0  # 0=front near core, 1=tail tip
        for dx in range(52):
            # Map local U within each 10px face strip so both top/bottom faces taper symmetrically
            local_u = (dx % 10) / 9.0
            center_dist = abs(local_u - 0.5) * 2.0
            width_limit = max(0.12, (1.0 - prog * 0.88))
            flutter = 0.14 * math.sin(dy * 1.1 + dx * 0.9)
            if center_dist > width_limit + flutter:
                continue
            edge = center_dist / max(0.05, width_limit + flutter)
            t = min(1.0, 0.18 + prog * 0.72 + edge * 0.25)
            a = int(max(40, min(245, 240 * (1.0 - prog * 0.62) * (1.0 - edge * 0.45))))
            put(dx, 46 + dy, fire_color(t, a), fire_color(t * 0.85, int(a * 0.65)))

    base.save(folder / 'fireball.png')
    glow.save(folder / 'fireball_glow.png')

def main():
    global SIZE
    SIZE = 128
    PARTS.clear(); BOXES.clear()
    geometry(); pack(); write_geometry('FireDragonGeometry', 128, True)
    folder=ASSETS/'entity/fire_dragon'; folder.mkdir(parents=True,exist_ok=True)
    for primary in range(5):
        for secondary in range(5):
            for accent in range(5):
                base,glow=skin(primary,secondary,accent)
                base.save(folder/f'{primary}_{secondary}_{accent}.png')
    _,glow=skin(2,4,0); glow.save(folder/'eyes.png')
    write_fireball_textures(folder)
    # Dedicated adolescent Young geometry & 125 color skins (256x256 atlas for 16 px/block density).
    SIZE = 256
    young_geometry(); pack(); write_geometry('FireDragonYoungGeometry', 256, False)
    young_folder = folder / 'young'; young_folder.mkdir(parents=True, exist_ok=True)
    for primary in range(5):
        for secondary in range(5):
            for accent in range(5):
                base, glow = skin(primary, secondary, accent)
                base.save(young_folder / f'{primary}_{secondary}_{accent}.png')
    _, glow = skin(2, 4, 0); glow.save(young_folder / 'eyes.png')
    # Colossal Adult geometry, 3D saddle & 125 color skins (256x256 atlas).
    SIZE = 256
    adult_geometry(); pack(); write_geometry('FireDragonAdultGeometry', 256, False)
    adult_folder = folder / 'adult'; adult_folder.mkdir(parents=True, exist_ok=True)
    for primary in range(5):
        for secondary in range(5):
            for accent in range(5):
                base, glow = adult_skin(primary, secondary, accent)
                base.save(adult_folder / f'{primary}_{secondary}_{accent}.png')
    _, glow = adult_skin(0, 1, 0); glow.save(adult_folder / 'eyes.png')
    # Keep the foundation item reproducible.
    egg=Image.new('RGBA',(16,16),(0,0,0,0))
    palette=tones(PALETTES[2])
    for y in range(2,15):
        for x in range(2,14):
            if ((x-7.5)/(4.2+y*.08))**2+((y-8.5)/6.5)**2<=1:
                color=(247,163,43,255) if (x*3+y*5)%13<3 else palette[2 if x<8 and y<10 else 1]
                egg.putpixel((x,y),color)
    (ASSETS/'item').mkdir(parents=True,exist_ok=True)
    egg.save(ASSETS/'item/fire_dragon_spawn_egg.png')
    print('Generated 125 baby + 125 young + 125 adult skins, emissive eyes, 3D fireball textures and spawn egg.')

if __name__=='__main__': main()
