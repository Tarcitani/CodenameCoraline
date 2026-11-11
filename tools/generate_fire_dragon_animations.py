"""Authoritative animation tracks; generates Java catalog and matching angle documentation."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
CLIPS=[]

def frames(peak, duration, start=(0,0,0), end=(0,0,0), at=.5):
    return [[0,*start],[round(duration*at,3),*peak],[duration,*end]]

def clip(name,ticks,loop=False,rot=None,pos=None):
    CLIPS.append(dict(name=name,ticks=ticks,loop=loop,rot=rot or {},pos=pos or {}))

def pair(peak,sign=1): return peak

def catalog():
    clip('none',1)
    clip('breathe',40,True,{'torso':frames((1.5,0,0),40),'neck_mid':frames((-2,0,0),40),'tail_2':frames((0,3,0),40)})
    clip('idle_sniff',30,rot={'neck_base':frames((-12,0,0),30),'head':[[0,0,0,0],[9,-8,18,5],[20,-5,-15,-5],[30,0,0,0]],'tail_1':frames((0,-8,0),30)})
    clip('idle_wings',36,rot={'left_wing_upper':frames((0,30,-18),36,at=.33),'right_wing_upper':frames((0,-30,18),36,at=.67),'left_hind_upper':frames((5,0,0),36),'right_hind_upper':frames((-5,0,0),36)})
    clip('idle_tail',40,rot={'neck_base':frames((0,20,0),40),'head':frames((5,30,12),40),'tail_1':frames((0,-20,0),40),'tail_2':frames((0,-18,0),40),'jaw':frames((12,0,0),40)})
    walk={}
    for side,phase in [('left',1),('right',-1)]:
        for leg,opposite in [('front',1),('hind',-1)]:
            swing=phase*opposite*22
            walk[f'{side}_{leg}_upper']=[[0,swing,0,0],[10,-swing,0,0],[20,swing,0,0]]
            walk[f'{side}_{leg}_lower']=[[0,-swing*.4,0,0],[10,swing*.4,0,0],[20,-swing*.4,0,0]]
    walk['tail_1']=[[0,0,-6,0],[10,0,6,0],[20,0,-6,0]]
    clip('walk',20,True,walk,{'torso':[[0,0,0,0],[5,0,.3,0],[10,0,0,0],[15,0,.3,0],[20,0,0,0]]})
    wings={}
    for side,sign in [('left',1),('right',-1)]:
        wings[f'{side}_wing_upper']=[[0,0,sign*65,-sign*35],[4,0,sign*65,sign*55],[8,0,sign*65,-sign*35],[12,0,sign*65,sign*55],[16,0,sign*65,-sign*35]]
        wings[f'{side}_wing_forearm']=[[0,0,-sign*18,0],[4,0,-sign*18,-sign*20],[8,0,-sign*18,0],[12,0,-sign*18,-sign*20],[16,0,-sign*18,0]]
        wings[f'{side}_wing_tip']=frames((0,-sign*18,sign*12),16,start=(0,-sign*18,0),end=(0,-sign*18,0))
        for leg in ['front','hind']:
            wings[f'{side}_{leg}_upper']=[[0,-35,0,0],[16,-35,0,0]]
            wings[f'{side}_{leg}_lower']=[[0,55,0,0],[16,55,0,0]]
    clip('fly',16,True,wings,{'torso':frames((0,1,0),16)})
    clip('takeoff',16,rot={'torso':frames((-10,0,0),16,end=(-10,0,0)),'left_wing_upper':frames((0,65,-35),16,end=(0,65,-35)),'right_wing_upper':frames((0,-65,35),16,end=(0,-65,35))},pos={'torso':[[0,0,0,0],[6,0,-1,0],[16,0,2,0]]})
    clip('land',16,rot={'torso':frames((8,0,0),16),'left_wing_upper':frames((0,65,-25),16),'right_wing_upper':frames((0,-65,25),16)},pos={'torso':frames((0,-1.2,0),16)})
    prep={'torso':(-7,0,0),'neck_base':(-12,0,0),'neck_mid':(8,0,0),'head':(-8,0,0),'jaw':(24,0,0),'tail_1':(12,0,0),'left_hind_upper':(10,0,0),'right_hind_upper':(10,0,0)}
    hit={'torso':(5,0,0),'neck_base':(10,0,0),'neck_mid':(-10,0,0),'head':(-5,0,0),'jaw':(32,0,0),'tail_1':(-6,0,0),'left_hind_upper':(-4,0,0),'right_hind_upper':(-4,0,0)}
    clip('fire_windup',12,rot={k:[[0,0,0,0],[12,*v]] for k,v in prep.items()},pos={'torso':[[0,0,0,0],[12,0,-.3,.7]]})
    clip('fire_release',1,rot={k:[[0,*prep[k]],[1,*v]] for k,v in hit.items()},pos={'torso':[[0,0,-.3,.7],[1,0,0,-.5]]})
    clip('fire_recovery',15,rot={k:[[0,*v],[15,0,0,0]] for k,v in hit.items()},pos={'torso':[[0,0,0,-.5],[15,0,0,0]]})
    clip('hurt',8,rot={'torso':frames((0,0,-8),8,at=.25),'neck_base':frames((-12,0,0),8,at=.25),'head':frames((10,0,0),8,at=.25)})
    clip('stun',20,rot={'neck_base':frames((15,0,0),20),'head':frames((12,0,8),20)})
    clip('death',20,rot={'torso':[[0,0,0,0],[12,0,0,70],[20,0,0,85]],'neck_base':[[0,0,0,0],[20,20,0,0]],'tail_2':[[0,0,0,0],[20,0,-15,0]]},pos={'torso':[[0,0,0,0],[20,0,-2,0]]})
    sit={'torso':(-12,0,0),'left_hind_upper':(-50,0,0),'right_hind_upper':(-50,0,0),'left_hind_lower':(70,0,0),'right_hind_lower':(70,0,0),'neck_base':(12,0,0),'tail_1':(12,0,0)}
    clip('sit',12,rot={k:[[0,0,0,0],[12,*v]] for k,v in sit.items()},pos={'torso':[[0,0,0,0],[12,0,-1,0]]})
    clip('stand',12,rot={k:[[0,*v],[12,0,0,0]] for k,v in sit.items()},pos={'torso':[[0,0,-1,0],[12,0,0,0]]})
    shoulder=dict(sit)
    shoulder.update({'left_wing_upper':(0,-12,8),'right_wing_upper':(0,12,-8),'tail_1':(18,0,0),'tail_2':(20,0,0),'tail_3':(15,0,0)})
    clip('shoulder',40,True,rot={k:[[0,*v],[40,*v]] for k,v in shoulder.items()},pos={'torso':[[0,0,-1,0],[40,0,-1,0]]})
    clip('eat',24,rot={'neck_base':frames((20,0,0),24),'head':frames((12,0,0),24),'jaw':[[0,0,0,0],[6,18,0,0],[10,0,0,0],[14,18,0,0],[18,0,0,0],[24,0,0,0]]})
    clip('play_chase',100,True,rot={'torso':[[0,-5,-8,0],[25,-5,8,0],[50,-5,-8,0],[75,-5,8,0],[100,-5,-8,0]],'tail_1':[[0,0,15,0],[50,0,-15,0],[100,0,15,0]],'head':frames((-10,12,0),100)})
    clip('greet',30,rot={'neck_base':frames((-5,12,0),30),'head':frames((0,15,12),30),'tail_2':frames((0,12,0),30)})
    clip('imitate_wings',40,rot={'left_wing_upper':frames((0,45,-25),40,at=.35),'right_wing_upper':frames((0,-45,25),40,at=.65),'head':frames((-5,0,8),40)})
    rest=dict(sit); rest.update({'neck_base':(25,0,0),'head':(15,0,-10),'tail_1':(0,25,0),'tail_2':(0,25,0),'tail_3':(0,20,0)})
    clip('rest_together',100,rot={k:[[0,0,0,0],[30,*v],[100,*v]] for k,v in rest.items()},pos={'torso':[[0,0,0,0],[30,0,-1.3,0],[100,0,-1.3,0]]})

def generate():
    catalog()
    enum=['package com.codenamecoraline.entity.firedragon;','',
        '/** Visual-only clip metadata; generated by tools/generate_fire_dragon_animations.py. */',
        'public enum FireDragonAnimation {']
    enum += ['    '+c['name'].upper()+f'({c["ticks"]}, {str(c["loop"]).lower()})'+(';' if i==len(CLIPS)-1 else ',') for i,c in enumerate(CLIPS)]
    enum+=['    public final int ticks;','    public final boolean looping;',
        '    FireDragonAnimation(int ticks, boolean looping) { this.ticks = ticks; this.looping = looping; }',
        '    public static FireDragonAnimation fromName(String name) {',
        '        for (FireDragonAnimation clip : values()) if (clip.name().equalsIgnoreCase(name)) return clip;',
        '        return NONE;','    }','}']
    (ROOT/'src/main/java/com/codenamecoraline/entity/firedragon/FireDragonAnimation.java').write_text('\n'.join(enum)+'\n',encoding='utf-8')
    java=['package com.codenamecoraline.client.firedragon;','',
        'import net.minecraft.client.animation.*;','',
        '/** Generated tracks, angles in degrees; Java keyframes convert them to radians. */',
        'public final class FireDragonAnimations {','    private FireDragonAnimations() {}',
        '    public static final AnimationDefinition[] CLIPS = {']
    doc=['# Dragão de Fogo — animações do bebê', '',
        'Gerado por `tools/generate_fire_dragon_animations.py`. Parte B da ficha. Todas as linhas estão **pendentes de revisão visual humana**.',
        'Rotações: deltas x/y/z em graus em relação ao bind pose; `KeyframeAnimations.degreeVec` converte para radianos. `ModelPart.translateAndRotate` aplica `Quaternionf.rotationZYX(zRot,yRot,xRot)`, confirmado nas fontes 26.1.2.124. Posição: unidades do modelo (1/16 bloco), convertidas por `posVec` (Y invertido). Curva LINEAR em todos os trechos; os pontos intermediários definem a aceleração e o amortecimento.', '',
        '| Clipe | Ticks | Junta / canal | Keyframes: tick → x,y,z | Estado |','|---|---|---|---|---|']
    for c in CLIPS:
        line=f'        AnimationDefinition.Builder.withLength({c["ticks"]/20:.3f}F)'+('.looping()' if c['loop'] else '')
        java.append(line)
        for kind in ['rot','pos']:
            for joint,frameset in c[kind].items():
                vals=', '.join(f'{float(v/20 if i==0 else v):.4f}F' for f in frameset for i,v in enumerate(f))
                java.append(f'            .addAnimation("{joint}", {kind}({vals}))')
                desc='; '.join(f'{f[0]} → '+','.join(str(v) for v in f[1:]) for f in frameset)
                doc.append(f'| {c["name"]} | {c["ticks"]} | {joint} / {kind} | {desc} | pendente de revisão visual |')
        java.append('            .build(),')
    java+=['    };',
        '    private static AnimationChannel rot(float... frames) { return channel(true, frames); }',
        '    private static AnimationChannel pos(float... frames) { return channel(false, frames); }',
        '    private static AnimationChannel channel(boolean rotation, float[] frames) {',
        '        Keyframe[] keys = new Keyframe[frames.length / 4];',
        '        for (int i = 0; i < keys.length; i++) { int j = i * 4;',
        '            keys[i] = new Keyframe(frames[j], rotation',
        '                    ? KeyframeAnimations.degreeVec(frames[j+1], frames[j+2], frames[j+3])',
        '                    : KeyframeAnimations.posVec(frames[j+1], frames[j+2], frames[j+3]),',
        '                    AnimationChannel.Interpolations.LINEAR);',
        '        }',
        '        return new AnimationChannel(rotation ? AnimationChannel.Targets.ROTATION : AnimationChannel.Targets.POSITION, keys);',
        '    }','}']
    (ROOT/'src/main/java/com/codenamecoraline/client/firedragon/FireDragonAnimations.java').write_text('\n'.join(java)+'\n',encoding='utf-8')
    from generate_fire_dragon_assets import geometry, PARTS
    geometry()
    doc += ['', '## Bind pose e pivôs', '',
        'Unidades: 1/16 bloco para offsets; graus para a rotação inicial. Juntas sem geometria continuam sendo pivôs reais. A camada ventral usa CubeDeformation 0,25 unidade do modelo, coerente com os mobs de referência; não são 0,25 bloco de expansão.', '',
        '| Junta | Pai | Pivô x,y,z | Rotação base x,y,z |', '|---|---|---|---|']
    for p in PARTS:
        doc.append(f"| {p['name']} | {p['parent']} | {p['pivot']} | {p['rotation']} |")
    (ROOT/'docs/design/fire_dragon_animation.md').write_text('\n'.join(doc)+'\n',encoding='utf-8')
    target=ROOT/'build/fire-dragon-sketches'; target.mkdir(parents=True,exist_ok=True)
    (target/'animations.json').write_text(json.dumps(CLIPS),encoding='utf-8')
    print(f'Generated {len(CLIPS)-1} animations and matching angle documentation.')

if __name__=='__main__': generate()
