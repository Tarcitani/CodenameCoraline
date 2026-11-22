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
    # Asymmetric Drogon-style flight clip (24 ticks = 1.2s base cycle, used by preview/exporter and as reference;
    # live flight in FireDragonAnimController layers smooth speed/glide/climb/dive/bank dynamics).
    # 62% powerful downstroke (0..15), 38% quick folded recovery upstroke (15..24), delayed torso heave (Image 1-3).
    wings={}
    for side,sign in [('left',1),('right',-1)]:
        wings[f'{side}_wing_upper']=[
            [0, -8, sign*58, -sign*46],
            [6, -12, sign*68, -sign*10],
            [12, 8, sign*76, sign*48],
            [15, 10, sign*70, sign*54],
            [19, -4, sign*50, sign*4],
            [24, -8, sign*58, -sign*46],
        ]
        wings[f'{side}_wing_forearm']=[
            [0, 0, -sign*12, sign*16],
            [6, 0, -sign*22, sign*6],
            [12, 0, -sign*18, -sign*18],
            [15, 0, -sign*10, -sign*26],
            [19, 0, sign*24, -sign*44],
            [24, 0, -sign*12, sign*16],
        ]
        wings[f'{side}_wing_tip']=[
            [0, 0, -sign*14, sign*22],
            [6, 0, -sign*20, sign*12],
            [12, 0, -sign*18, -sign*10],
            [15, 0, -sign*12, -sign*24],
            [19, 0, sign*16, -sign*36],
            [24, 0, -sign*14, sign*22],
        ]
        # Front legs tucked neatly under chest, hind legs swept cleanly back with subtle inertia
        wings[f'{side}_front_upper']=[[0,-48,0,0],[12,-54,0,0],[18,-44,0,0],[24,-48,0,0]]
        wings[f'{side}_front_lower']=[[0,72,0,0],[12,78,0,0],[18,66,0,0],[24,72,0,0]]
        wings[f'{side}_front_foot']=[[0,-24,0,0],[12,-28,0,0],[24,-24,0,0]]
        wings[f'{side}_hind_upper']=[[0,42,0,0],[12,50,0,0],[18,36,0,0],[24,42,0,0]]
        wings[f'{side}_hind_lower']=[[0,28,0,0],[12,20,0,0],[18,34,0,0],[24,28,0,0]]
        wings[f'{side}_hind_foot']=[[0,24,0,0],[12,30,0,0],[24,24,0,0]]
    # Delayed body heave: High Point right after downstroke (15..17), Low Point right after upstroke (2..4)
    wings['torso']=[[0,4,0,0],[6,2,0,0],[12,-6,0,0],[16,-4,0,0],[20,3,0,0],[24,4,0,0]]
    wings['neck_base']=[[0,-5,0,0],[6,-3,0,0],[12,6,0,0],[16,4,0,0],[20,-3,0,0],[24,-5,0,0]]
    wings['neck_mid']=[[0,2,0,0],[12,-3,0,0],[24,2,0,0]]
    wings['head']=[[0,2,0,0],[12,-2,0,0],[24,2,0,0]]
    wings['tail_1']=[[0,-6,0,0],[6,5,0,0],[12,8,0,0],[18,-5,0,0],[24,-6,0,0]]
    wings['tail_2']=[[0,-10,0,0],[8,2,0,0],[15,12,0,0],[21,-4,0,0],[24,-10,0,0]]
    wings['tail_3']=[[0,-8,0,0],[10,-4,0,0],[18,14,0,0],[24,-8,0,0]]
    wings['tail_4']=[[0,-4,0,0],[12,-8,0,0],[20,16,0,0],[24,-4,0,0]]
    clip('fly',24,True,wings,{'torso':[[0,0,-1.0,0],[6,0,-.4,0],[12,0,1.2,0],[16,0,1.8,0],[20,0,.2,0],[24,0,-1.0,0]]})
    # Takeoff (16t): crouch & lower head -> unfold wings base-to-tip -> power downstroke + hind-leg launch -> tuck legs
    tk={
        'torso':[[0,0,0,0],[4,10,0,0],[8,-4,0,0],[12,-18,0,0],[16,-10,0,0]],
        'neck_base':[[0,0,0,0],[4,14,0,0],[8,-6,0,0],[12,-12,0,0],[16,-6,0,0]],
        'neck_mid':[[0,0,0,0],[4,6,0,0],[12,-6,0,0],[16,0,0,0]],
        'head':[[0,0,0,0],[4,-8,0,0],[12,10,0,0],[16,4,0,0]],
        'tail_1':[[0,0,0,0],[4,-8,0,0],[12,18,0,0],[16,10,0,0]],
        'tail_2':[[0,0,0,0],[6,-10,0,0],[14,16,0,0],[16,8,0,0]],
        'tail_3':[[0,0,0,0],[8,-8,0,0],[16,12,0,0]],
    }
    for side,sign in [('left',1),('right',-1)]:
        tk[f'{side}_wing_upper']=[[0,0,0,0],[4,0,sign*35,-sign*20],[8,-10,sign*68,-sign*48],[12,8,sign*74,sign*52],[16,-6,sign*60,-sign*32]]
        tk[f'{side}_wing_forearm']=[[0,0,0,0],[4,0,sign*18,sign*10],[8,0,-sign*18,sign*12],[12,0,-sign*18,-sign*20],[16,0,-sign*12,sign*8]]
        tk[f'{side}_wing_tip']=[[0,0,0,0],[6,0,sign*10,sign*12],[10,0,-sign*18,sign*10],[13,0,-sign*16,-sign*22],[16,0,-sign*12,sign*12]]
        tk[f'{side}_front_upper']=[[0,0,0,0],[4,-32,0,0],[9,-10,0,0],[16,-48,0,0]]
        tk[f'{side}_front_lower']=[[0,0,0,0],[4,48,0,0],[9,18,0,0],[16,72,0,0]]
        tk[f'{side}_hind_upper']=[[0,0,0,0],[4,-38,0,0],[9,36,0,0],[16,42,0,0]]
        tk[f'{side}_hind_lower']=[[0,0,0,0],[4,58,0,0],[9,-12,0,0],[16,28,0,0]]
    clip('takeoff',16,rot=tk,pos={'torso':[[0,0,0,0],[4,0,-1.8,0],[9,0,1.2,0],[16,0,2.2,0]]})
    # Land (16t): upright braking flare (Image 4/5 Rokunin!) -> wide wing brake -> legs reach down -> impact crouch -> fold wings
    ld={
        'torso':[[0,-14,0,0],[4,-24,0,0],[8,-10,0,0],[11,10,0,0],[16,0,0,0]],
        'neck_base':[[0,-8,0,0],[4,16,0,0],[8,8,0,0],[11,12,0,0],[16,0,0,0]],
        'neck_mid':[[0,0,0,0],[4,8,0,0],[11,-4,0,0],[16,0,0,0]],
        'head':[[0,6,0,0],[4,-12,0,0],[11,-6,0,0],[16,0,0,0]],
        'tail_1':[[0,10,0,0],[4,22,0,0],[9,12,0,0],[12,-6,0,0],[16,0,0,0]],
        'tail_2':[[0,8,0,0],[5,18,0,0],[12,-8,0,0],[16,0,0,0]],
        'tail_3':[[0,6,0,0],[6,14,0,0],[13,-6,0,0],[16,0,0,0]],
    }
    for side,sign in [('left',1),('right',-1)]:
        ld[f'{side}_wing_upper']=[[0,-6,sign*65,-sign*30],[4,18,sign*74,-sign*18],[8,12,sign*62,sign*26],[12,4,sign*28,-sign*8],[16,0,0,0]]
        ld[f'{side}_wing_forearm']=[[0,0,-sign*16,0],[4,0,-sign*22,-sign*10],[8,0,sign*12,-sign*16],[12,0,sign*16,-sign*8],[16,0,0,0]]
        ld[f'{side}_wing_tip']=[[0,0,-sign*14,0],[5,0,-sign*18,-sign*12],[10,0,sign*10,-sign*12],[16,0,0,0]]
        ld[f'{side}_front_upper']=[[0,-46,0,0],[5,-28,0,0],[9,-18,0,0],[12,-24,0,0],[16,0,0,0]]
        ld[f'{side}_front_lower']=[[0,68,0,0],[5,20,0,0],[9,14,0,0],[12,42,0,0],[16,0,0,0]]
        ld[f'{side}_hind_upper']=[[0,36,0,0],[5,-36,0,0],[9,-20,0,0],[12,-30,0,0],[16,0,0,0]]
        ld[f'{side}_hind_lower']=[[0,24,0,0],[5,26,0,0],[9,18,0,0],[12,48,0,0],[16,0,0,0]]
    clip('land',16,rot=ld,pos={'torso':[[0,0,1.4,0],[5,0,.8,0],[9,0,-.4,0],[12,0,-1.8,0],[16,0,0,0]]})
    prep={'torso':(-7,0,0),'neck_base':(-12,0,0),'neck_mid':(8,0,0),'head':(-8,0,0),'jaw':(24,0,0),'tail_1':(12,0,0),'left_hind_upper':(10,0,0),'right_hind_upper':(10,0,0)}
    hit={'torso':(5,0,0),'neck_base':(10,0,0),'neck_mid':(-10,0,0),'head':(-5,0,0),'jaw':(32,0,0),'tail_1':(-6,0,0),'left_hind_upper':(-4,0,0),'right_hind_upper':(-4,0,0)}
    clip('fire_windup',12,rot={k:[[0,0,0,0],[12,*v]] for k,v in prep.items()},pos={'torso':[[0,0,0,0],[12,0,-.3,.7]]})
    clip('fire_release',1,rot={k:[[0,*prep[k]],[1,*v]] for k,v in hit.items()},pos={'torso':[[0,0,-.3,.7],[1,0,0,-.5]]})
    clip('fire_recovery',15,rot={k:[[0,*v],[15,0,0,0]] for k,v in hit.items()},pos={'torso':[[0,0,0,-.5],[15,0,0,0]]})
    clip('hurt',8,rot={'torso':frames((0,0,-8),8,at=.25),'neck_base':frames((-12,0,0),8,at=.25),'head':frames((10,0,0),8,at=.25)})
    clip('stun',20,rot={'neck_base':frames((15,0,0),20),'head':frames((12,0,8),20)})
    clip('death',20,rot={'torso':[[0,0,0,0],[12,0,0,70],[20,0,0,85]],'neck_base':[[0,0,0,0],[20,20,0,0]],'tail_2':[[0,0,0,0],[20,0,-15,0]]},pos={'torso':[[0,0,0,0],[20,0,-2,0]]})
    sit={'torso':(-12,0,0),'left_hind_upper':(-52,0,0),'right_hind_upper':(-52,0,0),'left_hind_lower':(74,0,0),'right_hind_lower':(74,0,0),'left_hind_foot':(-22,0,0),'right_hind_foot':(-22,0,0),'left_front_upper':(10,0,0),'right_front_upper':(10,0,0),'neck_base':(12,0,0),'neck_mid':(-4,0,0),'tail_1':(10,20,0),'tail_2':(4,24,0),'tail_3':(0,22,0),'tail_4':(-2,16,0)}
    clip('sit',12,rot={k:[[0,0,0,0],[12,*v]] for k,v in sit.items()},pos={'torso':[[0,0,0,0],[12,0,-1.2,0]]})
    clip('stand',12,rot={k:[[0,*v],[12,0,0,0]] for k,v in sit.items()},pos={'torso':[[0,0,-1.2,0],[12,0,0,0]]})
    # Left shoulder: tuck the feet, fold the wings and coil the tail toward the player's neck.
    # The renderer mirrors central Y/Z channels for the right shoulder.
    shoulder={'torso':(6,-10,-8),'neck_base':(12,-18,6),'neck_mid':(6,-12,0),
        'head':(12,-12,8),'tail_1':(4,42,-8),'tail_2':(-2,48,0),
        'tail_3':(-4,45,0),'tail_4':(-6,38,0),
        'left_wing_upper':(5,-20,12),'right_wing_upper':(5,20,-12),
        'left_wing_forearm':(0,55,-28),'right_wing_forearm':(0,-55,28),
        'left_wing_tip':(0,25,10),'right_wing_tip':(0,-25,-10)}
    for side in ['left','right']:
        for leg in ['front','hind']:
            shoulder[f'{side}_{leg}_upper']=(-65,0,0)
            shoulder[f'{side}_{leg}_lower']=(95,0,0)
            shoulder[f'{side}_{leg}_foot']=(-30,0,0)
    shoulder_tracks={k:[[0,*v],[40,*v]] for k,v in shoulder.items()}
    shoulder_tracks['torso']=[[0,6,-10,-8],[20,7,-10,-8],[40,6,-10,-8]]
    shoulder_tracks['neck_mid']=[[0,6,-12,0],[20,7,-12,0],[40,6,-12,0]]
    shoulder_tracks['tail_4']=[[0,-6,38,0],[20,-6,40,0],[40,-6,38,0]]
    clip('shoulder',40,True,rot=shoulder_tracks,pos={'torso':[[0,0,1,0],[20,0,1.15,0],[40,0,1,0]]})
    clip('eat',24,rot={'neck_base':frames((20,0,0),24),'head':frames((12,0,0),24),'jaw':[[0,0,0,0],[6,18,0,0],[10,0,0,0],[14,18,0,0],[18,0,0,0],[24,0,0,0]]})
    clip('play_chase',100,True,rot={'torso':[[0,-5,-8,0],[25,-5,8,0],[50,-5,-8,0],[75,-5,8,0],[100,-5,-8,0]],'tail_1':[[0,0,15,0],[50,0,-15,0],[100,0,15,0]],'head':frames((-10,12,0),100)})
    clip('greet',30,rot={'neck_base':frames((-5,12,0),30),'head':frames((0,15,12),30),'tail_2':frames((0,12,0),30)})
    clip('imitate_wings',40,rot={'left_wing_upper':frames((0,45,-25),40,at=.35),'right_wing_upper':frames((0,-45,25),40,at=.65),'head':frames((-5,0,8),40)})
    rest=dict(sit); rest.update({'neck_base':(25,0,0),'head':(15,0,-10),'tail_1':(0,25,0),'tail_2':(0,25,0),'tail_3':(0,20,0)})
    clip('rest_together',100,rot={k:[[0,0,0,0],[30,*v],[100,*v]] for k,v in rest.items()},pos={'torso':[[0,0,0,0],[30,0,-1.3,0],[100,0,-1.3,0]]})
    # Young dragon combat and territorial clips (shared joint names).
    bite_w={'torso':(-6,0,0),'neck_base':(-18,0,0),'neck_mid':(-10,0,0),'head':(-10,0,0),'jaw':(36,0,0),'tail_1':(10,0,0)}
    bite_s={'torso':(8,0,0),'neck_base':(20,0,0),'neck_mid':(14,0,0),'head':(6,0,0),'jaw':(2,0,0),'tail_1':(-8,0,0)}
    clip('bite_windup',8,rot={k:[[0,0,0,0],[8,*v]] for k,v in bite_w.items()},pos={'torso':[[0,0,0,0],[8,0,0,.9]]})
    clip('bite_strike',4,rot={k:[[0,*bite_w[k]],[4,*v]] for k,v in bite_s.items()},pos={'torso':[[0,0,0,.9],[4,0,-.4,-1.4]]})
    clip('bite_recovery',8,rot={k:[[0,*v],[8,0,0,0]] for k,v in bite_s.items()},pos={'torso':[[0,0,-.4,-1.4],[8,0,0,0]]})
    br_w={'torso':(-8,0,0),'neck_base':(-14,0,0),'neck_mid':(10,0,0),'head':(-6,0,0),'jaw':(28,0,0),'left_wing_upper':(0,45,-20),'right_wing_upper':(0,-45,20),'tail_1':(10,0,0)}
    clip('breath_windup',10,rot={k:[[0,0,0,0],[10,*v]] for k,v in br_w.items()},pos={'torso':[[0,0,0,0],[10,0,-.4,.8]]})
    clip('breath_active',24,rot={
        'torso':[[0,-4,0,0],[12,-4,0,0],[24,-4,0,0]],
        'neck_base':[[0,6,-14,0],[12,6,14,0],[24,6,-10,0]],
        'neck_mid':[[0,-4,-10,0],[12,-4,10,0],[24,-4,-6,0]],
        'head':[[0,-4,-12,0],[12,-4,12,0],[24,-4,-8,0]],
        'jaw':[[0,34,0,0],[12,32,0,0],[24,30,0,0]],
        'left_wing_upper':[[0,0,50,-22],[12,0,55,-18],[24,0,50,-22]],
        'right_wing_upper':[[0,0,-50,22],[12,0,-55,18],[24,0,-50,22]],
        'tail_1':[[0,0,12,0],[12,0,-12,0],[24,0,10,0]],
    },pos={'torso':[[0,0,-.2,-.3],[24,0,-.2,-.3]]})
    br_end={'torso':(-4,0,0),'neck_base':(6,-10,0),'neck_mid':(-4,-6,0),'head':(-4,-8,0),'jaw':(30,0,0),'left_wing_upper':(0,50,-22),'right_wing_upper':(0,-50,22),'tail_1':(0,10,0)}
    clip('breath_recovery',10,rot={k:[[0,*v],[10,0,0,0]] for k,v in br_end.items()},pos={'torso':[[0,0,-.2,-.3],[10,0,0,0]]})
    clip('growl',24,rot={
        'torso':frames((8,0,0),24),
        'neck_base':frames((14,0,0),24),
        'head':[[0,0,0,0],[6,-10,8,0],[14,-10,-8,0],[24,0,0,0]],
        'jaw':[[0,0,0,0],[6,26,0,0],[18,22,0,0],[24,0,0,0]],
        'left_wing_upper':frames((0,38,-22),24),
        'right_wing_upper':frames((0,-38,22),24),
        'tail_1':[[0,0,0,0],[12,0,14,0],[24,0,0,0]],
    },pos={'torso':frames((0,-.8,0),24)})

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
        '                    AnimationChannel.Interpolations.CATMULLROM);',
        '        }',
        '        return new AnimationChannel(rotation ? AnimationChannel.Targets.ROTATION : AnimationChannel.Targets.POSITION, keys);',
        '    }','}']
    (ROOT/'src/main/java/com/codenamecoraline/client/firedragon/FireDragonAnimations.java').write_text('\n'.join(java)+'\n',encoding='utf-8')
    doc += ['', '## Ombro: referência enrolada', '',
        'Clipe de 40 ticks: patas recolhidas, asas fechadas sobre o dorso, cabeça acomodada e cauda curvada para dentro. Pequena respiração no tronco/pescoço e ajuste da ponta da cauda. No ombro direito, o renderizador espelha Y/Z das juntas centrais (tronco, pescoço, cabeça e cauda), incluindo a rotação de bind. Sem espelhar UVs ou escalar negativamente a malha. Pose e altura ainda aguardam teste visual do usuário.']
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
