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

def write_geometry():
    lines=['package com.codenamecoraline.client.firedragon;','',
        'import net.minecraft.client.model.geom.PartPose;',
        'import net.minecraft.client.model.geom.builders.*;','',
        '/** Generated by tools/generate_fire_dragon_assets.py; edit geometry there. */',
        'public final class FireDragonGeometry {',
        '    private FireDragonGeometry() {}',
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
    lines+=['        return LayerDefinition.create(mesh, 128, 128);','    }','}']
    target=ROOT/'src/main/java/com/codenamecoraline/client/firedragon/FireDragonGeometry.java'
    target.parent.mkdir(parents=True,exist_ok=True); target.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    out=ROOT/'build/fire-dragon-sketches'; out.mkdir(parents=True,exist_ok=True)
    (out/'geometry.json').write_text(json.dumps(dict(parts=PARTS,boxes=BOXES)),encoding='utf-8')

def main():
    geometry(); pack(); write_geometry()
    folder=ASSETS/'entity/fire_dragon'; folder.mkdir(parents=True,exist_ok=True)
    for primary in range(5):
        for secondary in range(5):
            for accent in range(5):
                base,glow=skin(primary,secondary,accent)
                base.save(folder/f'{primary}_{secondary}_{accent}.png')
    _,glow=skin(2,4,0); glow.save(folder/'eyes.png')
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
    print('Generated 125 color combinations, transparent emissive eyes and spawn egg.')

if __name__=='__main__': main()
