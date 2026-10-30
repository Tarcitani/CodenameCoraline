"""Reproducible original hatchling atlas and sprites. Pillow only, no downloaded art."""
from pathlib import Path
import json
import math
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/codenamecoraline'


def generate():
    rng = random.Random(34851)
    atlas = Image.new('RGBA', (256, 128), (0, 0, 0, 0))
    # Each cuboid receives its own 32x16 cell, matching BabyDragonModel.cube().
    colors = [(227,75,54),(245,214,160),(203,64,48),(227,75,54),(239,112,64),
              (245,214,160),(255,217,102),(255,217,102),(245,214,160),(255,230,181),
              (255,138,61),(190,58,48),(168,50,50),(245,214,160),(168,50,50),
              (255,138,61),(227,75,54),(255,155,79),(255,155,79),(255,155,79),
              (168,50,50),(203,64,48),(218,72,50),(234,92,51),(255,176,58),(255,138,61)]
    d = ImageDraw.Draw(atlas)
    for slot, base in enumerate(colors):
        ox, oy = slot % 8 * 32, slot // 8 * 16
        for y in range(16):
            for x in range(32):
                scale = .86 + .17 * (1-y/16) + rng.uniform(-.035,.035)
                atlas.putpixel((ox+x,oy+y),tuple(min(255,int(c*scale)) for c in base)+(255,))
        if slot not in (6,7,8,9,13):
            for y in range(3,15,4):
                for x in range(2+(y%8)//2,30,5):
                    ink=tuple(int(c*.78) for c in base)+(255,)
                    d.line([(ox+x,oy+y),(ox+x+1,oy+y+1),(ox+x+3,oy+y)],fill=ink)
        if slot in (15,17,18,19):
            for x in range(2,31,6): d.line((ox+x,oy+1,ox+x-2,oy+14),fill=(197,82,49,255))
        if slot == 4:
            # Snout front UV is u=depth, v=depth: tiny nostrils.
            for x in (4,6): d.point((ox+x,oy+4),fill=(82,35,34,255))
        if slot == 13:
            for x in (3,4): d.line((ox+x,oy+3,ox+x,oy+4),fill=(255,240,199,255))
        if slot in (6,7):
            # Side eyes: large dark pupils with cream glints; all UV faces agree.
            for x in range(32):
                for y in range(16):
                    if x%5 in (1,2): atlas.putpixel((ox+x,oy+y),(63,34,37,255))
            d.point((ox+4,oy+3),fill=(255,255,239,255))
            d.point((ox+2,oy+3),fill=(255,255,239,255))
    target=ASSETS/'textures/entity/baby_dragon.png';target.parent.mkdir(parents=True,exist_ok=True);atlas.save(target)
    egg=Image.new('RGBA',(16,16));d=ImageDraw.Draw(egg)
    d.ellipse((3,1,12,14),fill='#A83232',outline='#562D2E')
    d.ellipse((4,3,10,12),fill='#E34B36')
    for box in [(5,4,7,6),(9,8,11,10),(5,11,7,12)]:d.rectangle(box,fill='#FF8A3D')
    d.point((6,2),fill='#F5D6A0')
    ember=Image.new('RGBA',(16,16));d=ImageDraw.Draw(ember)
    d.polygon([(4,12),(2,8),(5,5),(6,1),(9,5),(11,3),(13,8),(11,13),(7,15)],fill='#E34B36')
    d.ellipse((4,6,11,13),fill='#FF8A3D');d.ellipse((6,8,10,12),fill='#FFD966');d.point((8,10),fill='#FFF4CB')
    for name,sprite in [('baby_dragon_spawn_egg',egg),('dragon_ember',ember)]:
        (ASSETS/'textures/item').mkdir(parents=True,exist_ok=True);sprite.save(ASSETS/f'textures/item/{name}.png')
        (ASSETS/f'items/{name}.json').write_text(json.dumps({'model':{'type':'minecraft:model','model':f'codenamecoraline:item/{name}'}},indent=2)+'\n')
        (ASSETS/f'models/item/{name}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'codenamecoraline:item/{name}'}},indent=2)+'\n')
    print('Generated hatchling atlas, egg and ember sprites.')


if __name__ == '__main__':
    generate()
