"""Texture-aware offline rasterizer for three real anatomies and their seventeen animations.

Run the Gradle exporter first. No hand-drawn substitute geometry is used.
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'build/baby-dragon-sketches'
ASSETS=ROOT/'src/main/resources/assets/codenamecoraline'
POSES=json.loads((OUT/'model.json').read_text())
SKINS={stage:Image.open(ASSETS/f'textures/entity/{stage}_dragon.png').convert('RGBA') for stage in ['baby','young','adult']}
NAMES=['idle','walk','takeoff','flying','hover','landing','shoulder_idle','fireball_charge','fireball_shoot','hurt','sleep','happy','breath_prepare','breath_loop','evolve','death','melee']


def project(v,yaw=.62,pitch=.16):
    x,y,z=v[:3];x,z=math.cos(yaw)*x-math.sin(yaw)*z,math.sin(yaw)*x+math.cos(yaw)*z
    return x,math.cos(pitch)*y-math.sin(pitch)*z,-math.sin(pitch)*y-math.cos(pitch)*z


def render(key,w,h,yaw=.62,sepia=False,common_scale=None,clay=False):
    skin=SKINS['adult' if key.startswith('adult_') else 'young' if key.startswith('young_') else 'baby']
    faces=POSES[key]
    projected=[[project(v,yaw) for v in f['vertices']] for f in faces]
    xs=[v[0] for f in projected for v in f];ys=[v[1] for f in projected for v in f]
    minx,maxx,miny,maxy=min(xs),max(xs),min(ys),max(ys)
    scale=common_scale or min((w-14)/(maxx-minx),(h-14)/(maxy-miny))
    left=(w-(maxx-minx)*scale)/2;top=(h-(maxy-miny)*scale)/2
    image=Image.new('RGBA',(w,h));pixels=image.load();zbuf=[-math.inf]*(w*h);owners=[-1]*(w*h)
    for face_index,(face,verts) in enumerate(zip(faces,projected)):
        points=[(left+(x-minx)*scale,top+(y-miny)*scale,z)for x,y,z in verts]
        a,b,c=verts[:3]
        u=[b[i]-a[i]for i in range(3)];v=[c[i]-a[i]for i in range(3)]
        n=[u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]]
        norm=math.sqrt(sum(x*x for x in n)) or 1
        shade=.73+.25*abs(n[1]/norm)+.08*abs(n[2]/norm)
        for ids in [(0,1,2),(0,2,3)]:
            a,b,c=[points[i] for i in ids]
            denom=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
            if abs(denom)<1e-8:continue
            for py in range(max(0,int(min(a[1],b[1],c[1]))),min(h,int(max(a[1],b[1],c[1]))+1)):
                for px in range(max(0,int(min(a[0],b[0],c[0]))),min(w,int(max(a[0],b[0],c[0]))+1)):
                    x,y=px+.5,py+.5
                    u=((b[1]-c[1])*(x-c[0])+(c[0]-b[0])*(y-c[1]))/denom
                    v=((c[1]-a[1])*(x-c[0])+(a[0]-c[0])*(y-c[1]))/denom;t=1-u-v
                    if min(u,v,t)<-1e-6:continue
                    depth=u*a[2]+v*b[2]+t*c[2];index=py*w+px
                    if depth<zbuf[index]:continue
                    uv=[sum(weight*face['vertices'][i][axis]for weight,i in zip([u,v,t],ids))for axis in [3,4]]
                    sample=skin.getpixel((min(skin.width-1,max(0,int(uv[0]*skin.width))),min(skin.height-1,max(0,int(uv[1]*skin.height)))))
                    if not sample[3]:continue
                    if clay:sample=(207,210,207,255)
                    color=tuple(min(255,int(c*shade))for c in sample[:3])+(255,)
                    if sepia:color=(86,57,33,110+int(sum(sample[:3])/6))
                    pixels[px,py]=color;zbuf[index]=depth;owners[index]=face_index
    if sepia:
        for y in range(1,h-1):
            for x in range(1,w-1):
                i=y*w+x
                if owners[i]>=0 and any(owners[n]!=owners[i] for n in [i-1,i+1,i-w,i+w]):pixels[x,y]=(74,46,27,225)
    return image


def main():
    board=Image.new('RGB',(896,1080),'#17242C');d=ImageDraw.Draw(board)
    for i,name in enumerate(NAMES):
        x,y=(i%4)*224,(i//4)*216
        tile=render(name+'_5',448,360).resize((224,180),Image.Resampling.LANCZOS)
        board.paste(tile,(x,y+24),tile);d.text((x+12,y+8),name.upper(),fill='#F5D6A0')
    board.save(OUT/'review.png')
    views_to_render=[('FRONT','idle_3',0),('SIDE','idle_3',1.57),('REAR','idle_3',3.14),('THREE QUARTERS','idle_3',.65),
                     ('WINGS OPEN','hover_3',.65),('WINGS FOLDED','idle_3',.65),('FLIGHT','flying_3',.65),('ATTACK','fireball_charge_7',.65)]
    for stage in ['baby','young','adult']:
        prefix='' if stage=='baby' else stage+'_'
        sheet=Image.new('RGB',(1280,600),'#17242C');d=ImageDraw.Draw(sheet)
        for i,(label,key,yaw) in enumerate(views_to_render):
            if stage=='adult' and label=='ATTACK':key='breath_loop_5'
            tile=render(prefix+key,320,264,yaw)
            x,y=(i%4)*320,(i//4)*300
            sheet.paste(tile,(x,y+28),tile);d.text((x+12,y+9),stage.upper()+' / '+label,fill='#F5D6A0')
        sheet.save(OUT/f'{stage}_views.png')
    clay=Image.new('RGB',(1080,360),'#17242C');d=ImageDraw.Draw(clay)
    for i,prefix in enumerate(['','young_','adult_']):
        tile=render(prefix+'idle_3',360,320,.65,clay=True);clay.paste(tile,(i*360,30),tile)
        d.text((i*360+15,10),['BABY','YOUNG','ADULT'][i]+' / UNSKINNED ANATOMY',fill='#F5D6A0')
    clay.save(OUT/'anatomy_clay.png')
    views=Image.new('RGB',(768,272),'#17242C');d=ImageDraw.Draw(views)
    for i,(yaw,label) in enumerate([(0,'FRONT'),(.65,'THREE QUARTERS'),(1.57,'SIDE')]):
        tile=render('idle_3',512,480,yaw).resize((256,240),Image.Resampling.LANCZOS);views.paste(tile,(i*256,25),tile);d.text((i*256+12,9),label,fill='#F5D6A0')
    views.save(OUT/'anatomy.png')
    frames=[]
    for frame in range(12):
        board=Image.new('RGB',(512,670),'#17242C');d=ImageDraw.Draw(board)
        for i,name in enumerate(NAMES):
            x,y=(i%4)*128,(i//4)*134;tile=render(f'{name}_{frame}',128,112)
            board.paste(tile,(x,y+20),tile);d.text((x+4,y+4),name.replace('fireball_',''),fill='#F5D6A0')
        frames.append(board)
    frames[0].save(OUT/'animations.gif',save_all=True,append_images=frames[1:],duration=90,loop=0)
    # Same pixels per block across ages: geometry comes from the real model, renderer scale is explicit.
    growth=Image.new('RGB',(1050,420),'#17242C');d=ImageDraw.Draw(growth)
    for i,(stage,scale) in enumerate([('',1),('young_',1.8),('adult_',3)]):
        key=stage+'idle_3'
        original=POSES[key]
        POSES[key]=[{**face,'vertices':[[v[0]*scale*.85,1.5+(v[1]-1.5)*scale*.85,v[2]*scale*.85,*v[3:]] for v in face['vertices']]}for face in original]
        tile=render(key,350,350,common_scale=36)
        growth.paste(tile,(i*350,40),tile);d.text((i*350+20,15),f'{stage or "baby_"} {scale}x',fill='#F5D6A0')
        POSES[key]=original
    growth.save(OUT/'evolution.png')
    combat=Image.new('RGB',(1000,600),'#17242C');d=ImageDraw.Draw(combat)
    for i,key in enumerate(['adult_fireball_charge_10','adult_breath_prepare_10','adult_breath_loop_5','adult_melee_5']):
        for j,yaw in enumerate([1.57,.65]):
            tile=render(key,250,260,yaw);combat.paste(tile,(i*250,j*300+25),tile)
            d.text((i*250+4,j*300+5),key.replace('adult_','')+(' SIDE'if j==0 else ' 3/4'),fill='#F5D6A0')
    combat.save(OUT/'adult_attacks.png')
    bestiary=ASSETS/'textures/gui/bestiary';bestiary.mkdir(parents=True,exist_ok=True)
    for key,pose,height in [('anatomy','idle_4',80),('flight','flying_4',72),('defense','fireball_charge_9',72)]:
        plate=render(pose,336,height*3,sepia=True).resize((112,height),Image.Resampling.LANCZOS)
        plate.save(bestiary/f'baby_dragon_{key}.png')
    print('Rendered 17 poses, eight views per anatomy, unskinned comparison, scale comparison, adult attacks, animation GIF and bestiary engravings.')


if __name__=='__main__':main()
