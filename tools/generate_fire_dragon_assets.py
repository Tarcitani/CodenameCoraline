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
    # Colossal adult dragon inspired by references: crowned horns, cheek frill spikes, nasal horn,
    # muscular plated chest/neck, huge wings with elbow claws, heavy talons, whip tail spade, and toggleable 3D saddle.
    # Ground check: torso y=8 + leg upper y=3 + lower y=5 + foot y=6 + foot height 2 = 24 exact ground.
    part('torso', 'root', (0, 8, 0))
    box('torso', 'torso', (-4.5, -4, -7.5), (9, 8, 15))
    part('belly', 'torso', (0, 0, 0))
    box('belly', 'belly', (-3.5, -1.5, -7.7), (7, 5, 14), 'secondary', .25)
    # 3D Dragon Saddle (toggled visible only when saddled)
    part('saddle', 'torso', (0, -4.1, -2.0))
    box('saddle_seat', 'saddle', (-3.6, -1.0, -3.5), (7.2, 1.4, 7.0), 'leather', .15)
    box('saddle_pommel', 'saddle', (-2.4, -2.4, -3.8), (4.8, 1.8, 1.4), 'leather_gold')
    box('saddle_cantle', 'saddle', (-2.6, -2.2, 2.4), (5.2, 1.6, 1.4), 'leather_gold')
    box('saddle_strap_l', 'saddle', (4.3, 0.0, -1.8), (.6, 5.2, 3.6), 'leather')
    box('saddle_strap_r', 'saddle', (-4.9, 0.0, -1.8), (.6, 5.2, 3.6), 'leather')
    # Muscular S-curved neck
    part('neck_base', 'torso', (0, -2.0, -7.0))
    box('neck_base', 'neck_base', (-2.8, -2.6, -3.6), (5.6, 5.2, 5.4))
    box('neck_base_plate', 'neck_base', (-2.0, 1.2, -3.5), (4.0, 1.8, 4.8), 'secondary')
    box('neck_base_crest', 'neck_base', (-.5, -4.4, -2.4), (1.0, 2.2, 2.4), 'accent')
    part('neck_mid', 'neck_base', (0, -2.0, -3.6))
    box('neck_mid', 'neck_mid', (-2.3, -2.4, -4.6), (4.6, 4.6, 5.6))
    box('neck_mid_plate', 'neck_mid', (-1.7, 1.1, -4.5), (3.4, 1.6, 5.0), 'secondary')
    box('neck_mid_crest', 'neck_mid', (-.5, -4.2, -3.2), (1.0, 2.2, 2.4), 'accent')
    part('neck_top', 'neck_mid', (0, -1.5, -4.4))
    box('neck_top', 'neck_top', (-2.0, -2.0, -3.8), (4.0, 4.0, 4.8))
    box('neck_top_crest', 'neck_top', (-.5, -3.6, -2.6), (1.0, 2.0, 2.2), 'accent')
    # Crowned angular skull with nasal horn, cheek frills & heavy fangs
    part('head', 'neck_top', (0, -.6, -3.6))
    box('head', 'head', (-3.5, -3.0, -5.2), (7.0, 5.2, 6.8))
    box('snout', 'head', (-2.5, -.8, -10.5), (5.0, 2.8, 6.0))
    box('nasal_horn', 'head', (-.6, -2.8, -10.2), (1.2, 2.4, 1.4), 'accent')
    part('jaw', 'head', (0, 1.8, -2.2))
    box('jaw', 'jaw', (-2.3, 0, -7.8), (4.6, 1.8, 6.8), 'secondary')
    for side, sign in [('left', 1), ('right', -1)]:
        part(f'{side}_eye', 'head', (sign * 3.4, -.9, -3.4))
        box(f'{side}_cheek', f'{side}_eye', (-.6, -1.6, -1.8), (1.2, 3.2, 3.6), 'primary')
        # Fan-like cheek/jaw frill spikes (references #2, #6, #7)
        box(f'{side}_frill_top', 'head', (sign * 3.2 - (0 if sign == 1 else 2.2), -1.6, -1.0), (2.2, 1.0, 3.2), 'accent')
        box(f'{side}_frill_low', 'head', (sign * 2.8 - (0 if sign == 1 else 2.0), 0.6, -1.2), (2.0, 1.0, 3.0), 'accent')
        box(f'{side}_eye_front', 'head', (sign * 1.75 - 1.0, -2.2, -5.4), (2.0, 1.6, .6), 'eye')
        # Grand sweeping crown horns
        part(f'{side}_horn', 'head', (sign * 2.5, -2.6, -.6), (-36, 0, sign * 22))
        box(f'{side}_horn', f'{side}_horn', (-.9, -5.4, -.9), (1.8, 6.0, 1.8), 'accent')
        part(f'{side}_horn_tip', f'{side}_horn', (0, -5.4, 0), (-30, 0, sign * 16))
        box(f'{side}_horn_tip', f'{side}_horn_tip', (-.65, -4.6, -.65), (1.3, 5.0, 1.3), 'accent')
        box(f'{side}_fang', 'head', (sign * 1.9 - .45, 1.6, -9.6), (.9, 1.6, .9), 'ivory')
        # Colossal articulated wings with elbow claw
        part(f'{side}_wing_upper', 'torso', (sign * 4.2, -3.0, -1.2), (0, -sign * 65, -sign * 40))
        x = 0 if sign == 1 else -7.5
        box(f'{side}_wing_upper', f'{side}_wing_upper', (x, -.8, -.8), (7.5, 1.6, 1.6), 'accent')
        for step, depth in enumerate([7.5, 6.8, 6.0, 5.2]):
            xx = step * 1.85 if sign == 1 else -(step + 1) * 1.85
            box(f'{side}_membrane_inner_{step}', f'{side}_wing_upper', (xx, 0, .4), (1.85, .5, depth), 'secondary')
        part(f'{side}_wing_forearm', f'{side}_wing_upper', (sign * 7.5, 0, 0), (0, sign * 18, sign * 8))
        xf = 0 if sign == 1 else -9.0
        box(f'{side}_wing_forearm', f'{side}_wing_forearm', (xf, -.7, -.7), (9.0, 1.4, 1.4), 'primary')
        box(f'{side}_wing_claw', f'{side}_wing_forearm', (0 if sign == 1 else -1.4, -1.2, -2.8), (1.4, 1.4, 2.6), 'accent')
        for step, depth in enumerate([9.5, 8.2, 6.8, 5.5]):
            xx = step * 2.25 if sign == 1 else -(step + 1) * 2.25
            box(f'{side}_membrane_outer_{step}', f'{side}_wing_forearm', (xx, 0, 0), (2.25, .5, depth), 'secondary')
        part(f'{side}_wing_tip', f'{side}_wing_forearm', (sign * 9.0, 0, 0), (0, sign * 18, 0))
        box(f'{side}_wing_tip', f'{side}_wing_tip', (0 if sign == 1 else -6.0, -.3, 0), (6.0, .6, 6.0), 'secondary')
        # Muscular legs + heavy talons (torso y=8 + 3 + 5 + 6 + 2 = 24)
        for which, z in [('front', -5.0), ('hind', 5.0)]:
            name = f'{side}_{which}'
            hind = which == 'hind'
            part(f'{name}_upper', 'torso', (sign * 4.2, 3.0, z))
            box(f'{name}_upper', f'{name}_upper', (-1.8, -.8, -1.8), (3.6, 5.6, 3.6) if not hind else (4.2, 6.2, 4.8))
            part(f'{name}_lower', f'{name}_upper', (0, 5.0, 0))
            box(f'{name}_lower', f'{name}_lower', (-1.4, 0, -1.4), (2.8, 6.0, 2.8))
            part(f'{name}_foot', f'{name}_lower', (0, 6.0, -.8))
            box(f'{name}_foot', f'{name}_foot', (-1.7, 0, -2.4), (3.4, 2.0, 4.6), 'secondary')
            box(f'{name}_claw', f'{name}_foot', (-1.4, .6, -3.6), (2.8, 1.1, 1.8), 'accent')
    # Long whip tail with tall dorsal crests & barbed tail spade
    for i in range(1, 5):
        parent = 'torso' if i == 1 else f'tail_{i-1}'
        part(f'tail_{i}', parent, (0, -.3 if i == 1 else 0, 7.2 if i == 1 else 6.0), (-5 if i == 1 else -3, 4 if i == 3 else 0, 0))
        width = 4.6 if i == 1 else 3.6 if i == 2 else 2.6 if i == 3 else 1.8
        length = 6.8 if i == 1 else 6.4
        box(f'tail_{i}', f'tail_{i}', (-width / 2, -width / 2, 0), (width, width, length))
        box(f'tail_crest_{i}', f'tail_{i}', (-.6, -width / 2 - 2.0, 1.6), (1.2, 2.6, 2.6), 'accent')
    box('tail_spade', 'tail_4', (-2.6, -.5, 4.8), (5.2, 1.0, 5.2), 'accent')
    for i in range(3):
        part(f'crest_{i}', 'torso', (0, -4.0, i * 4.2 - 4.2))
        box(f'crest_{i}', f'crest_{i}', (-.6, -2.4, 0), (1.2, 3.0, 2.8), 'accent')


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
                base, glow = skin(primary, secondary, accent)
                base.save(adult_folder / f'{primary}_{secondary}_{accent}.png')
    _, glow = skin(2, 4, 0); glow.save(adult_folder / 'eyes.png')
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
