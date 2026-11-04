"""Generate three native dragon anatomies and UV atlases from the original hatchling palette.

The palette snapshot is captured from the existing texture once. No external art or dependencies.
Every cuboid owns a packed UV rectangle; left/right sides intentionally do not share UVs.
"""
from pathlib import Path
import hashlib
import json
import math
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/codenamecoraline'
ASSETS = ROOT / 'src/main/resources/assets/codenamecoraline/textures/entity'
OUT = ROOT / 'build/dragon-remodel'
PALETTE = ROOT / 'tools/dragon_original_palette.json'
SIZE = 512

# Distinct proportions in model pixels, before the existing stage render scales.
RIGS = [
    dict(id='baby', bodyY=19.5, neckY=-1.2, neckZ=-3, midY=-.1, midZ=-1.0,
         headY=0, headZ=-.5, mouthY=1.0, mouthZ=-6.8, jawLever=3.5,
         neckPitch=-.10, midPitch=.06, width=.45, height=.72, eye=.53,
         saddleY=.30, saddleZ=.02, bodyW=6, bodyH=5.6, bodyD=7.5,
         headW=7, headH=5.8, headD=6, leg=2.5, wing=1, tail=1),
    dict(id='young', bodyY=17.7, neckY=-1.7, neckZ=-4.2, midY=-.3, midZ=-2.5,
         headY=-.3, headZ=-2.1, mouthY=1.1, mouthZ=-9.0, jawLever=4.5,
         neckPitch=-.32, midPitch=.14, width=1.15, height=1.85, eye=1.40,
         saddleY=.83, saddleZ=.10, bodyW=8, bodyH=6.6, bodyD=10.5,
         headW=7.5, headH=5.5, headD=7.8, leg=4.3, wing=1.6, tail=1.6),
    dict(id='adult', bodyY=16.5, neckY=-3.0, neckZ=-6.3, midY=-.5, midZ=-4.3,
         headY=-.2, headZ=-4.1, mouthY=1.25, mouthZ=-11.7, jawLever=5.7,
         neckPitch=-.62, midPitch=.28, width=2.65, height=3.90, eye=2.85,
         saddleY=2.16, saddleZ=.20, bodyW=12, bodyH=9.5, bodyD=15,
         headW=8, headH=6.2, headD=10, leg=5.5, wing=2.8, tail=2.5),
]


def palette():
    if not PALETTE.exists():
        source = ASSETS / 'baby_dragon.png'
        im = Image.open(source).convert('RGBA')
        samples = {'red': 0, 'cream': 1, 'neck': 2, 'snout': 4, 'eye': 6,
                   'horn': 8, 'horn_tip': 9, 'orange': 10, 'leg': 11,
                   'dark_red': 12, 'claw': 13, 'wing_bone': 14, 'membrane': 15,
                   'wing_highlight': 17, 'tail': 21, 'flame': 24}
        colors = {}
        for name, slot in samples.items():
            # Bright, non-ink texels retain the original material hue rather than averaging pupils/shadows.
            pixels = [im.getpixel((slot % 8 * 32 + x, slot // 8 * 16 + y))[:3]
                      for x in range(32) for y in range(16)]
            colors[name] = list(sorted(pixels, key=sum)[int(len(pixels) * .82)])
        colors['ink'] = list(im.getpixel((6 * 32 + 1, 3))[:3])
        PALETTE.write_text(json.dumps({'source': 'original baby_dragon.png before remodel',
            'sha256': hashlib.sha256(source.read_bytes()).hexdigest(), 'colors': colors}, indent=2) + '\n')
    return json.loads(PALETTE.read_text())['colors']


class Anatomy:
    def __init__(self, rig):
        self.rig = rig
        self.parts = []
        self.boxes = []
        self.by_name = {}

    def part(self, name, parent, pivot=(0, 0, 0), rot=(0, 0, 0)):
        item = dict(name=name, parent=parent, pivot=pivot, rot=rot, boxes=[])
        assert name not in self.by_name, name
        self.parts.append(item)
        self.by_name[name] = item
        return name

    def box(self, part, material, xyz, dims, emissive=False):
        assert min(dims) > 0
        box = dict(part=part, material=material, xyz=xyz, dims=dims, emissive=emissive)
        self.boxes.append(box)
        self.by_name[part]['boxes'].append(box)

    def detail(self, name, parent, material, pivot, dims, rot=(0, 0, 0), emissive=False):
        self.part(name, parent, pivot, rot)
        self.box(name, material, (-dims[0] / 2, -dims[1] / 2, -dims[2] / 2), dims, emissive)


def make(r):
    a = Anatomy(r); age = RIGS.index(r); w, h, depth = r['bodyW'], r['bodyH'], r['bodyD']
    a.part('body', 'root', (0, r['bodyY'], 0))
    # Five horizontal tiers and three longitudinal masses create a bevelled muscular torso.
    for section,(center,length,girth) in enumerate([(-depth*.31,depth*.40,1.0),(0,depth*.28,.89),(depth*.31,depth*.38,.94)]):
        for tier,contour in enumerate([.64,.88,1,.91,.67]):
            tw=w*girth*contour; th=h/5
            a.box('body','red' if section<2 else 'dark_red',(-tw/2,-h/2+tier*th,center-length/2),(tw,th,length))
    belly_count = 3 + age * 2
    for i in range(belly_count):
        z = -depth*.4 + i*depth*.8/max(1, belly_count-1)
        a.detail(f'belly_plate_{i}', 'body', 'cream', (0, h*.38, z), (w*.66, h*.35, depth/belly_count*.88), (-.06, 0, 0))
    a.part('neck', 'body', (0, r['neckY'], r['neckZ']), (r['neckPitch'], 0, 0))
    neck_w = [3.7, 4.3, 5.5][age]
    neck_l = abs(r['midZ']) + 1.2
    a.box('neck', 'neck', (-neck_w/2, -neck_w*.5, -neck_l), (neck_w, neck_w, neck_l+1))
    a.part('neck_middle', 'neck', (0, r['midY'], r['midZ']), (r['midPitch'], 0, 0))
    a.box('neck_middle', 'neck', (-neck_w*.43, -neck_w*.45, r['headZ']-.4), (neck_w*.86, neck_w*.9, abs(r['headZ'])+1.5))
    for i in range(2+age*2):
        z = -.5 - i*(neck_l-.5)/(2+age*2)
        a.detail(f'neck_throat_{i}', 'neck', 'cream', (0, neck_w*.42, z), (neck_w*.7, .35+age*.12, .8+age*.3), (.08, 0, 0))
    a.part('head', 'neck_middle', (0, r['headY'], r['headZ']),(-r['neckPitch']-r['midPitch'],0,0))
    hw, hh, hd = r['headW'], r['headH'], r['headD']
    a.box('head', 'red', (-hw/2, -hh*.64, -hd*.67), (hw, hh, hd))
    a.detail('forehead', 'head', 'red', (0, -hh*.5, -hd*.35), (hw*.84, hh*.42, hd*.92), (-.08, 0, 0))
    # The adult muzzle narrows through three staggered volumes instead of a square snout.
    front = abs(r['mouthZ'])-.9
    steps = 2+age
    for i in range(steps):
        t = i/max(1, steps-1)
        length = (front-hd*.45)/steps*1.2
        z = -hd*.45-i*(front-hd*.45)/steps-length/2
        a.detail(f'muzzle_{i}', 'head', 'snout', (0, -.25+.18*t, z),
                 (hw*(.77-.21*t), hh*(.40-.09*t), length), (-.035, 0, 0))
    a.part('jaw', 'head', (0, hh*.25+.25, -hd*.33))
    jaw_len = front-hd*.33-.1
    a.box('jaw', 'cream', (-hw*.31, 0, -jaw_len), (hw*.62, .65+age*.2, jaw_len+.7))
    for side in (-1, 1):
        p = 'left' if side == 1 else 'right'
        a.detail(p+'_cheek', 'head', 'red', (side*hw*.4, .1, -hd*.2), (hw*.28, hh*.62, hd*.6), (0, side*-.10, side*.08))
        a.part(p+'_eye', 'head', (side*(hw*.552), -hh*.24, -hd*.48))
        a.box(p+'_eye', 'eye', (-.10, -(.9 if age == 0 else .65), -1.15), (.20, 1.8 if age == 0 else 1.3, 2.3+age*.25), age > 0)
        a.detail(p+'_brow', 'head', 'dark_red', (side*hw*.55, -hh*.42, -hd*.47), (.8+age*.25, .6+age*.18, 2.6+age*.6), (.10*age, side*-.08, side*-.12))
        a.detail(p+'_nostril', 'head', 'ink', (side*hw*.19, -.35, -front-.35), (.45+age*.12, .30, .12), (0, 0, 0))
        horn = a.part(p+'_horn', 'head', (side*hw*.35, -hh*.59, hd*.16), (-.36, 0, side*.20))
        horn_count = 2+age
        for i in range(horn_count):
            segment = p+'_horn' if i == 0 else a.part(f'{p}_horn_{i}', horn, (0, -(1.2+age*.35), .4+age*.2), (-.34, 0, side*.07))
            taper = 1-i/(horn_count+1)
            a.box(segment, 'horn' if i < horn_count-1 else 'horn_tip', (-.65*taper, -(1.8+age*.7), -.25), (1.3*taper, 2.0+age*.7, 1.2*taper))
            horn = segment
        a.detail(p+'_ear', 'head', 'orange', (side*hw*.53, -.2, hd*.21), (.55, 2.3+age, 1.8+age*.65), (.25, side*-.4, side*-.7))
        for i in range(age+1):
            a.detail(f'{p}_face_spike_{i}', 'head', 'horn', (side*(hw*.48+.4*i), .7-i*.7, hd*.15+i*.6),
                     (.65, 1.3+age*.7, .65), (-.7, side*.3, side*-.65))
        for i in range(age*2+1):
            z = -hd*.5-i*(front-hd*.5)/max(1, age*2)
            a.detail(f'{p}_tooth_{i}', 'head', 'horn_tip', (side*hw*.25, hh*.22+.1, z), (.35+age*.07, .6+age*.3, .35+age*.07), (0, 0, side*.08))
        for front_leg in (False, True):
            name = p+('_front_leg' if front_leg else '_back_leg')
            leg_w = 1.8+age*.65
            z = -depth*.31 if front_leg else depth*.32
            a.part(name, 'body', (side*(w*.42), h*.25, z), (.05 if front_leg else -.12, 0, side*.04))
            a.box(name, 'leg', (-leg_w/2, -.6, -leg_w/2), (leg_w, r['leg']*.62+1, leg_w*(1.1 if front_leg else 1.45)))
            shin = a.part(name+'_shin', name, (0, r['leg']*.52, .25 if front_leg else .65), (-.10 if front_leg else .18, 0, 0))
            a.box(shin, 'red', (-leg_w*.36, -.2, -leg_w*.4), (leg_w*.72, r['leg']*.48, leg_w*.8))
            foot_y = 24-r['bodyY']-h*.25-r['leg']*.52
            paw = a.part(name+'_paw', shin, (0, foot_y-.8, -.3))
            a.box(paw, 'cream', (-leg_w*.58, 0, -leg_w*.8), (leg_w*1.16, .8, leg_w*1.35))
            for toe in range(3):
                tx = (toe-1)*leg_w*.34
                a.detail(f'{name}_toe_{toe}', paw, 'red', (tx, .25, -leg_w*.86), (leg_w*.30, .55, .8+age*.3))
                a.detail(f'{name}_claw_{toe}', paw, 'horn' if age < 2 else 'ink', (tx, .3, -leg_w*.98-.4-age*.12),
                         (.27+age*.13, .35+age*.12, .8+age*.55), (-.12, 0, 0))
        make_wing(a, p, side, age, r)
    # Progressive dorsal armour. Leave a clear saddle pocket on the adult's upper back.
    for i in range(4+age*2):
        z = -depth*.45+i*depth*.95/(3+age*2)
        if age == 2 and -1 < z < 4: continue
        a.detail(f'dorsal_{i}', 'body', 'orange', (0, -h*.55, z), (.8+age*.45, 1.3+age*.9, 1.1+age*.55), (-.35, 0, 0))
    if age > 0:
        for side in (-1, 1):
            for i in range(3+age):
                z = -depth*.35+i*depth*.7/(2+age)
                a.detail(f'armor_{side}_{i}', 'body', 'dark_red', (side*w*.48, -h*.08, z), (.65, h*.48, 1.8+age*.6), (0, side*.12, side*-.08))
                if age == 2:
                    a.detail(f'heat_seam_{side}_{i}', 'body', 'orange', (side*w*.511, -h*.15, z-.8), (.12, .18, 1.3), (0, side*.12, side*-.08), True)
    tail_names = ['tail_base', 'tail_middle', 'tail_tip'] + (['tail_3'] if age > 0 else []) + (['tail_4', 'tail_5'] if age == 2 else [])
    parent = 'body'; last_length = 0
    for i, name in enumerate(tail_names):
        length = (3.4+age*.9)*(1-i*.075)
        width = (2.4+age*.65)*(1-i/(len(tail_names)+.4))
        a.part(name, parent, (0, .4, depth*.46) if i == 0 else (0, 0, last_length*.86), (.06 if i == 0 else -.025, 0, 0))
        a.box(name, 'tail', (-width/2, -width*.38, 0), (width, width*.76, length))
        a.detail(name+'_spine', name, 'orange', (0, -width*.50, length*.3), (.5+age*.25, .8+age*.5, .8+age*.35), (-.4, 0, 0))
        if age == 2:
            for side in (-1, 1): a.detail(name+f'_side_fin_{side}', name, 'dark_red', (side*width*.6, 0, length*.6), (.7, .5, 1.7), (0, side*-.5, 0))
        parent = name; last_length = length
    a.part('tail_flame', parent, (0, 0, last_length*.8), (0, .1, 0))
    for i in range(2+age*2):
        a.detail(f'flame_blade_{i}', 'tail_flame', 'flame' if i%2 else 'orange',
                 ((i%2-.5)*(.6+age*.4), -.2-(i%3)*.3, .5+i*.38), (.7+age*.25, .25+age*.12, 1.8+age*.5),
                 ((i%3-1)*.22, (i%2-.5)*.6, (i%2-.5)*.6), age == 2)
    if age==2:
        for i in range(3):
            a.detail(f'flame_core_{i}','tail_flame','flame',(0,-.35-i*.35,1.2+i*.7),(1.7-i*.5,.9-i*.25,3.5+i*.4),(-.14-i*.06,0,0),True)
    return a


def make_wing(a, p, side, age, r):
    size = r['wing']; root = p+'_wing'
    a.part(root, 'body', (side*r['bodyW']*.43, -r['bodyH']*.31, -r['bodyD']*.18), (0, side*-1.05, side*-.18))
    upper = 3.6*size
    a.box(root, 'wing_bone', (0 if side == 1 else -upper, -.45, -.45), (upper, .9+age*.13, 1.0+age*.13))
    membrane(a,root,side,age,[(0,.2),(upper,.3),(upper,5.0*size),(0,3.3*size)])
    elbow = a.part(p+'_wing_elbow', root, (side*upper, 0, .4), (0, side*-.20, 0))
    a.box(elbow, 'red', (-.65, -.65, -.65), (1.3+age*.12, 1.3, 1.3))
    lower = 2.6*size
    a.box(elbow, 'wing_bone', (0 if side == 1 else -lower, -.32, -.35), (lower, .65, .75))
    membrane(a,elbow,side,age,[(0,.1),(lower,.2),(lower,4.4*size),(0,5.0*size)])
    wrist = a.part(p+'_wing_tip', elbow, (side*lower, 0, .2), (0, side*-.30, 0))
    span = 5.0*size
    points = [(0, 0), (span, .2), (span*.86, span*.43), (span*.59, span*.84), (span*.18, span*1.02), (-span*.16, span*.72)]
    membrane(a,wrist,side,age,points)
    for i,(end_x,end_z) in enumerate(points[1:]):
        length = math.hypot(end_x,end_z)
        finger = a.part(f'{p}_finger_{i}', wrist, (0, -.05, 0), (0, -side*math.atan2(end_z,end_x), 0))
        for segment in range(3):
            width = (.32+age*.07)*(1-segment*.2)
            start = length*segment/3
            a.box(finger, 'wing_bone', (start if side == 1 else -start-length/3, -width/2, -width/2), (length/3+.05, width, width))
        a.detail(f'{p}_finger_claw_{i}', finger, 'horn', (side*length, 0, 0), (.65+age*.25, .35, .4), (0, 0, side*-.15))


def membrane(a,parent,side,age,points):
    # Scanline strips form a tapering, scalloped fan rather than a pair of rectangular planes.
    step = .8 if age == 0 else 1.15 if age == 1 else 1.4
    x = min(p[0] for p in points)
    max_x=max(p[0] for p in points)
    n = 0
    while x < max_x:
        center = x+step/2
        intersections = []
        for (x1,z1),(x2,z2) in zip(points, points[1:]+points[:1]):
            if min(x1,x2) <= center < max(x1,x2) and x1 != x2:
                intersections.append(z1+(center-x1)/(x2-x1)*(z2-z1))
        if len(intersections) >= 2:
            low, high = min(intersections), max(intersections)
            high -= (.08+age*.12)*abs(math.sin(center*1.8))
            if high > low+.1:
                a.box(parent, 'membrane' if n%3 else 'wing_highlight', (side*x if side == 1 else -x-step, .035*math.sin(center*.4), low), (step, .11, high-low), age == 2 and n%6 == 0)
                n += 1
        x += step


def pack(a):
    x=y=2; row_h=0
    for b in a.boxes:
        w,h,d=b['dims']; tw=math.ceil(2*(w+d)); th=math.ceil(h+d)
        assert tw+4<SIZE and th+4<SIZE
        if x+tw+2>SIZE: x=2; y+=row_h+2; row_h=0
        assert y+th+2<=SIZE, (a.rig['id'], y, th)
        b['uv']=(x,y,tw,th); x+=tw+2; row_h=max(row_h,th)
    occupied=set()
    for b in a.boxes:
        x,y,w,h=b['uv']
        for py in range(y,y+h):
            for px in range(x,x+w):
                assert (px,py) not in occupied
                occupied.add((px,py))
    return y+row_h+2


def texture(a, colors):
    rng=random.Random(71041+RIGS.index(a.rig)); age=RIGS.index(a.rig)
    image=Image.new('RGBA',(SIZE,SIZE)); glow=Image.new('RGBA',(SIZE,SIZE)); draw=ImageDraw.Draw(image)
    for b in a.boxes:
        x,y,w,h=b['uv']; name=b['material']; base=colors[name]
        for py in range(h):
            for px in range(w):
                # Keep the sampled material hue; local relief and scale edges add depth, not a new palette.
                stripe=((px+(py//3)*2)%5==0 and py%3==2)
                shade=.84+.18*(1-py/max(1,h-1))+rng.uniform(-.035,.035)
                if stripe and name in ('red','neck','dark_red','tail','leg'): shade-=.11+age*.025
                if name in ('membrane','wing_highlight'):
                    shade+=.06*math.sin(px*.55+py*.18)-(.10 if px%7==0 else 0)
                if name in ('horn','horn_tip','claw'): shade-=.09 if py%4==3 else 0
                color=tuple(max(0,min(255,int(c*shade))) for c in base)+(255,)
                if name=='eye':
                    color=tuple(base)+(255,)
                    if px%6 in (2,3): color=tuple(colors['ink'])+(255,)
                    if px%6==1 and py%5==1: color=tuple(colors['horn_tip'])+(255,)
                image.putpixel((x+px,y+py),color)
                if (b['emissive'] and (name=='eye' or name=='flame' or (px+py)%5<2)):
                    glow.putpixel((x+px,y+py),color)
    image.save(ASSETS/f"{a.rig['id']}_dragon.png")
    glow.save(ASSETS/f"{a.rig['id']}_dragon_glow.png")
    return image


def f(value):
    return f'{float(value):.5f}F'


def emit(anatomies):
    lines=['package com.codenamecoraline.client.babydragon;', '',
        'import com.codenamecoraline.entity.babydragon.DragonStage;',
        'import net.minecraft.client.model.geom.PartPose;', 'import net.minecraft.client.model.geom.builders.*;', '',
        '/** Generated by tools/generate_dragon_remodel.py. Each age has its own anatomy and UV allocation. */',
        'public final class DragonGeometry {', '    public static LayerDefinition layer(DragonStage stage) {',
        '        return switch(stage) { case BABY -> baby(); case YOUNG -> young(); case ADULT -> adult(); };', '    }']
    for a in anatomies:
        lines += [f"    private static LayerDefinition {a.rig['id']}() {{", '        MeshDefinition mesh = new MeshDefinition();', '        PartDefinition root = mesh.getRoot();']
        for p in a.parts:
            builder='CubeListBuilder.create()'
            for b in p['boxes']:
                x,y,_,_=b['uv']; builder+=f'.texOffs({x}, {y}).addBox('+', '.join(f(v) for v in (*b['xyz'],*b['dims']))+')'
            pose='PartPose.offsetAndRotation('+', '.join(f(v) for v in (*p['pivot'],*p['rot']))+')'
            variable = p['name'].replace('-', 'n')
            parent = p['parent'].replace('-', 'n')
            lines.append(f"        PartDefinition {variable} = {parent}.addOrReplaceChild(\"{p['name']}\", {builder}, {pose});")
        lines += [f'        return LayerDefinition.create(mesh, {SIZE}, {SIZE});', '    }']
    lines += ['    private DragonGeometry() {}', '}']
    (JAVA/'client/babydragon/DragonGeometry.java').write_text('\n'.join(lines)+'\n')
    for index,r in enumerate(RIGS):
        r['tailRootZ']=r['bodyD']*.46
        r['tailSegments']=[3,4,6][index]
        r['tailLength']=3.4+index*.9
    keys=['bodyY','neckY','neckZ','midY','midZ','headY','headZ','mouthY','mouthZ','jawLever','neckPitch','midPitch','width','height','eye','saddleY','saddleZ','tailRootZ','tailSegments','tailLength']
    lines=['package com.codenamecoraline.entity.babydragon;', '', '/** Generated alongside the real model; coordinates are model pixels except physical dimensions and saddle. */',
        'public record DragonRig('+', '.join('float '+k for k in keys)+') {',
        '    public static DragonRig of(DragonStage stage) { return switch(stage) {']
    for r in RIGS: lines.append(f"        case {r['id'].upper()} -> new DragonRig("+', '.join(f(r[k]) for k in keys)+');')
    lines += ['    }; }', '}']
    (JAVA/'entity/babydragon/DragonRig.java').write_text('\n'.join(lines)+'\n')


def main():
    OUT.mkdir(parents=True,exist_ok=True); colors=palette(); anatomies=[make(r) for r in RIGS]
    preview=Image.new('RGB',(SIZE*3,SIZE+28),'#17242c'); d=ImageDraw.Draw(preview)
    report={}
    for index,a in enumerate(anatomies):
        rows=pack(a); im=texture(a,colors); preview.paste(im,(SIZE*index,28),im)
        d.text((SIZE*index+8,8),a.rig['id'].upper()+f' {len(a.parts)} parts / {len(a.boxes)} cubes',fill='#f5d6a0')
        report[a.rig['id']]=dict(parts=len(a.parts),cubes=len(a.boxes),atlas_used_rows=rows,uvs=[b['uv'] for b in a.boxes],rig=a.rig)
        print(a.rig['id'],len(a.parts),'parts',len(a.boxes),'cubes',rows,'atlas rows; UV overlap: 0')
    emit(anatomies)
    (OUT/'geometry.json').write_text(json.dumps(report,indent=2)+'\n')
    preview.save(OUT/'atlases.png')


if __name__=='__main__': main()
