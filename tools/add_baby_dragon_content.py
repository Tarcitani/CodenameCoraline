"""One-time content authoring helper; preserved for translation and sound reproducibility."""
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
ASSETS=ROOT/'assets/codenamecoraline'
DATA=ROOT/'data/codenamecoraline'


def write(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')


translations={
 'pt_br':{
  'entity.codenamecoraline.baby_dragon':'Dragão Bebê',
  'entity.codenamecoraline.dragon_ember':'Centelha de Dragão',
  'item.codenamecoraline.baby_dragon_spawn_egg':'Ovo Gerador de Dragão Bebê',
  'item.codenamecoraline.dragon_ember':'Centelha de Dragão',
  'message.codenamecoraline.dragon.defense_on':'Dragão: defesa ativada',
  'message.codenamecoraline.dragon.defense_off':'Dragão: defesa desativada',
  'message.codenamecoraline.dragon.stay':'Dragão: descansar',
  'message.codenamecoraline.dragon.follow':'Dragão: acompanhar',
  'bestiary.codenamecoraline.baby_dragon.name':'Dragão Bebê',
  'intro':('Uma Pequena Chama','Há quem diga que estes filhotes nascem do calor guardado pelas montanhas. Quatro patinhas, bochechas largas, asas de membrana e uma cauda inquieta denunciam um dragão ainda pequeno. Suas escamas vermelhas guardam uma centelha que nunca queima seu próprio corpo.'),
  'habitat':('Entre Pedra e Copa','Procure em florestas, taigas, bosques e montanhas do Mundo Superior. São incomuns e aparecem sozinhos ou em pares, sobre chão firme, com luz e céu aberto. Sua vida vale vinte pontos, com dois de armadura; fogo e quedas não os ferem.'),
  'behavior':('Um Olhar Curioso','O filhote selvagem passeia, observa viajantes e faz voos curtos. Não caça animais nem procura briga. Deixe espaço para que abra as asas. Um companheiro mandado descansar fecha os olhos à noite; alimentá-lo desperta um alegre balanço da cauda.'),
  'taming':('Conquistar Confiança','Ofereça salmão cru: cada tentativa consome um e tem uma chance em três de conquistar sua confiança. Corações confirmam o vínculo. Depois, só seu dono pode alimentá-lo, curando quatro pontos por salmão. Clique com a mão vazia para alternar entre descansar e acompanhar.'),
  'companion':('Ao Seu Lado','Ele caminha próximo, voa para acompanhar corridas e faz voltas irregulares quando você para. Agache e clique nele com a mão vazia para chamá-lo ao ombro. Após o pouso, solte e segure o agachamento por um segundo para liberá-lo. Água, montarias, voo de élitro, perigo ou combate também o fazem sair. Muito distante, ele busca chão seguro perto do dono.'),
  'defense':('Uma Centelha Amiga','O filhote reage a agressões e aos combates do dono. Prepara a boca por doze ticks e lança uma centelha de três pontos de dano; recarrega por 1,5 segundo antes de preparar outro disparo. Não causa explosões nem incendeia o cenário. Poupa dono, aliados e companheiros. Agache e clique com um graveto para alternar a defesa. A ordem de descansar tem prioridade.'),
 },
 'en_us':{
  'entity.codenamecoraline.baby_dragon':'Baby Dragon',
  'entity.codenamecoraline.dragon_ember':'Dragon Ember',
  'item.codenamecoraline.baby_dragon_spawn_egg':'Baby Dragon Spawn Egg',
  'item.codenamecoraline.dragon_ember':'Dragon Ember',
  'message.codenamecoraline.dragon.defense_on':'Dragon: defense enabled',
  'message.codenamecoraline.dragon.defense_off':'Dragon: defense disabled',
  'message.codenamecoraline.dragon.stay':'Dragon: rest',
  'message.codenamecoraline.dragon.follow':'Dragon: follow',
  'bestiary.codenamecoraline.baby_dragon.name':'Baby Dragon',
  'intro':('A Little Flame','Some say these hatchlings come from warmth held deep within mountains. Four tiny paws, broad cheeks, membrane wings and a restless tail mark a dragon still small. Its red scales shelter a spark that never burns its own body.'),
  'habitat':('Stone and Canopy','Search Overworld forests, taigas, groves and mountains. Uncommon, they appear alone or in pairs on solid ground with daylight and open sky. They have twenty health points and two armor; fire and falls cannot hurt them.'),
  'behavior':('A Curious Gaze','Wild hatchlings wander, watch travellers and make short flights. They do not hunt animals or seek fights. Give them room to spread their wings. A companion ordered to rest closes its eyes at night; feeding brings a happy wag of the tail.'),
  'taming':('Earning Trust','Offer raw salmon: each attempt consumes one and has a one-in-three chance of earning trust. Hearts confirm the bond. Afterwards only its owner may feed it, healing four points per salmon. Right-click with an empty hand to switch between resting and following.'),
  'companion':('By Your Side','It walks nearby, flies to keep up with running and makes irregular loops when you stop. Sneak and right-click it empty-handed to call it to your shoulder. After landing, release sneak, then hold it for one second to release the dragon. Water, vehicles, elytra flight, danger or combat also make it leave. Far away, it looks for safe ground near its owner.'),
  'defense':('A Friendly Spark','The hatchling reacts to attacks and its owner’s fights. It charges for twelve ticks and fires an ember dealing three damage, with a 1.5-second cooldown before charging again. It never explodes or ignites scenery. Owner, allies and companions are spared. Sneak and right-click with a stick to toggle defense. The rest command takes priority.'),
 }
}
for lang,added in translations.items():
    path=ASSETS/f'lang/{lang}.json'; existing=json.loads(path.read_text(encoding='utf-8'))
    for key,value in added.items():
        if isinstance(value,tuple):
            title,body=value
            existing[f'bestiary.codenamecoraline.baby_dragon.{key}.title']=title
            existing[f'bestiary.codenamecoraline.baby_dragon.{key}.body']=body
            if key in ('intro','companion','defense'): existing[f'bestiary.codenamecoraline.baby_dragon.{key}.caption']=title
        else: existing[key]=value
    labels=['murmura','cantarola','geme','se apaga','bate as asas','pousa','inspira fogo','lança uma centelha'] if lang=='pt_br' else ['murmurs','chirps happily','whimpers','fades','flaps','lands','draws fire','shoots an ember']
    for event,label in zip(['ambient','happy','hurt','death','flap','land','charge','shoot'],labels): existing[f'subtitles.codenamecoraline.baby_dragon.{event}']=('Dragão '+label) if lang=='pt_br' else ('Dragon '+label)
    write(path,existing)
path=ASSETS/'sounds.json'; sounds=json.loads(path.read_text(encoding='utf-8'))
for event,vanilla,volume,pitch in [('ambient','entity.axolotl.idle_air',.7,1.2),('happy','entity.allay.ambient_with_item',.55,1.2),('hurt','entity.axolotl.hurt',.7,1.1),('death','entity.axolotl.death',.7,1.1),('flap','entity.parrot.fly',.6,1.15),('land','entity.parrot.step',.6,1.2),('charge','item.firecharge.use',.3,1.3),('shoot','entity.blaze.shoot',.45,1.4)]:
    sounds[f'entity.baby_dragon.{event}']={'subtitle':f'subtitles.codenamecoraline.baby_dragon.{event}','sounds':[{'name':'minecraft:'+vanilla,'type':'event','volume':volume,'pitch':pitch}]}
write(path,sounds)
write(DATA/'tags/worldgen/biome/baby_dragon_habitat.json',{'replace':False,'values':['#minecraft:is_forest','#minecraft:is_mountain','#minecraft:is_taiga','minecraft:grove']})
write(DATA/'neoforge/biome_modifier/baby_dragon_spawns.json',{'type':'neoforge:add_spawns','biomes':'#codenamecoraline:baby_dragon_habitat','spawners':[{'type':'codenamecoraline:baby_dragon','weight':6,'minCount':1,'maxCount':2}]})
write(DATA/'loot_table/entities/baby_dragon.json',{'type':'minecraft:entity','pools':[]})
