# Dragão Bebê (`baby_dragon`)

Companheiro original para Minecraft 26.1.2 / NeoForge 26.1.2.124. Sem bibliotecas novas, GeckoLib, reprodução ou crescimento para um dragão adulto. A aparência é a de um filhote permanente, com quatro patas; a categoria é `CREATURE`, inclusive no Pacífico.

## Aparência e recursos

Cabeça larga, focinho curto, mandíbula articulada, olhos amarelos com pupila escura, dois chifres em segmentos curvos, barbatanas laterais, peito e barriga creme. As quatro patas terminam em pés claros. Cada asa tem braço, articulação externa, dedos e membrana escalonada; a cauda tem três articulações e ponta alaranjada. Paleta: `#A83232`, `#E34B36`, `#FF8A3D`, `#F5D6A0`, `#FFD966`.

Modelo nativo `EntityModel`/`ModelPart`. Atlas de 256 × 128 com células de UV separadas por peça; peças simétricas reutilizam intencionalmente o mesmo material. Escamas, sombras, nervuras, narinas e brilho dos olhos são pintados por Pillow com semente fixa. Escala normal 0,85; no ombro 0,58. Hitbox independente: 0,45 × 0,65, olhos a 0,48.

`BabyDragonAnimations` contém 12 `AnimationDefinition`s, todas assadas em `KeyframeAnimation`, com canais Catmull–Rom: idle, walk, takeoff, flying, hover, landing, shoulder_idle, fireball_charge, fireball_shoot, hurt, sleep e happy. A entidade contém os `AnimationState`s; o render state copia os relógios. Eventos do servidor iniciam decolagem, pouso, preparação, disparo, dano e felicidade. O voo mistura progressivamente a postura, com asas externas defasadas, patas recolhidas e cauda compensatória. Há piscadas, olhar, respiração e inclinação curiosa da cabeça.

Geração e inspeção offline:

```powershell
python tools/generate_baby_dragon_assets.py
.\gradlew.bat -I tools/baby-dragon-sketches.gradle exportBabyDragonSketchModel
python tools/generate_baby_dragon_sketches.py
```

O exportador percorre a geometria real assada após `setupAnim`, incluindo UVs, em 144 amostras de animação. Saídas em `build/baby-dragon-sketches/`: `model.json`, `anatomy.png` (três vistas), `review.png` (12 poses) e `animations.gif`. As três gravuras do Bestiário derivam desses mesmos dados. Compilar/jogar não exige executar os geradores.

## Atributos

| Propriedade | Valor |
|---|---|
| Vida | 20 HP |
| Armadura | 2 |
| Velocidade terrestre | 0,22 |
| Velocidade aérea | 0,34, atributo vanilla `flying_speed` alterável por `/attribute` |
| Detecção/acompanhamento | 16 blocos |
| Alcance de defesa/disparo | 12 blocos |
| XP | 2 |
| Fogo e queda | Imune |
| Drops | Nenhum |

## Interações

| Ação | Resultado |
|---|---|
| Salmão cru no selvagem | Consome um, 1/3 de chance de domesticar; respeita o evento NeoForge de domesticação |
| Salmão cru pelo dono | Consome um, cura 4 HP, felicidade e corações |
| Mão vazia pelo dono | Alterna ficar/acompanhar e informa a ordem na barra de ação |
| Agachar + mão vazia no dragão | Solicita aproximação e pouso no ombro; se já estiver pousado, libera |
| Agachar + graveto no dragão | Alterna a defesa e informa o estado na barra de ação |
| Segurar agachamento por 20 ticks no ombro | Libera o dragão depois da tolerância inicial de 40 ticks |

Outros jogadores não mudam o dono nem comandam ou alimentam o companheiro. Nomear com etiqueta continua vanilla. UUID do dono, domesticação, vida, nome e ordem de sentar são persistidos pela superclasse `TamableAnimal`; defesa, flag do ombro e recarga são dados adicionais. Não há tela própria.

## Máquina de estados e movimento

`DragonBrain` é o único árbitro de deslocamento e combate; não há objetivos concorrentes de seguir, orbitar e atacar. Somente flutuação de emergência e olhar usam goals vanilla.

| Estado | Entrada e saída |
|---|---|
| `WILD_IDLE` | Selvagem ou dono ausente; passeios e voos curtos, com espera de 140–299 ticks |
| `FOLLOWING` | Dono disponível e sem outra prioridade; caminha quando além de 4 blocos e para abaixo de 2,5 |
| `FLYING` | Distância acima de 8 blocos ou dono em movimento rápido; mantém voo pelo menos 60 ticks |
| `ORBITING` | Dono próximo após 80 ticks de espera; voa por 140–239 ticks e respeita intervalo ambiental de 300 |
| `RETURNING` | Pedido de ombro, saída, fuga com vida ≤ 5 ou escape da água |
| `SHOULDER` | Dono autoriza, distância permite e posição do ombro está livre; interrompe navegação |
| `SITTING` | Ordem de ficar; primeiro pousa, depois interrompe deslocamento; à noite fecha os olhos |
| `DEFENDING` | Agressão válida recente ao dono/dragão ou alvo válido atacado pelo dono; termina com morte, alcance excedido, ordem, defesa desligada ou 240 ticks |

Prioridade: escape de líquido, ordem de ficar, recuo com pouca vida, defesa reativa, pedido de ombro, acompanhamento e comportamento ambiental. A seleção de ameaças usa timestamps a cada cinco ticks; não busca o hostil mais próximo. Navegação é refeita no máximo a cada oito ticks.

Em terra usa `GroundPathNavigation`, evitando lava, fogo e vizinhos do fogo. No ar troca para `FlyingPathNavigation`; `DragonFlightControl` interpola a velocidade em 14% por tick, limita giro a nove graus e desacelera ao chegar. `Entity.move` resolve colisões: não há noclip. A órbita varia raio (aproximadamente 2,15–3,45 blocos), altura (1,4–2,4) e fase; o dono continua sendo o centro móvel. Ao parar, procura chão firme abaixo antes de fechar as asas. O sistema de pathfinding pode escolher rotas alternativas quando um ponto fica bloqueado.

Acima de 24 blocos, tenta teletransportar uma vez por segundo. Busca apenas chunks carregados, borda do mundo válida, chão sólido, espaço sem colisão de blocos/entidades e sem fluidos, magma ou fogueiras. Se não houver destino, não teletransporta. Sentado, preso por laço, passageiro ou no ombro não usa esse recurso.

## Ombro e identidade

A API real do papagaio foi examinada: `ShoulderRidingEntity.setEntityOnShoulder` grava NBT no jogador e descarta a entidade; a saída instancia outra entidade e o renderer vanilla só conhece papagaios. Aqui a decisão é manter **a entidade viva no mundo**, usando uma âncora lateral acompanhando a rotação do corpo e o agachamento do dono. O próprio renderer desenha o dragão em escala reduzida. Não há camada substituta, passageiro invisível, descarte nem cópia de NBT durante o pouso.

O servidor valida a âncora; o cliente acompanha sua posição para reduzir atraso visual. A cada tick, verifica espaço e situação do dono. Apenas um dragão pode ocupar esse ombro. Libera em água, montaria, élitro, espectador, morte/ausência do dono, dano de pelo menos 4 HP, dano ao dragão, agachamento prolongado ou combate defensivo. A saída procura chão/ar livre; a criatura continua com o mesmo UUID e dados. Ao carregar uma flag de ombro sem dono disponível, ela sai para o mundo; nunca se cria um segundo dragão. Não implementa transferência automática entre dimensões.

## Defesa e centelhas

Pode reagir a um hostil que feriu o dono, a um agressor próprio ou a um alvo válido atacado pelo dono nos últimos cinco segundos. Criaturas neutras só entram quando envolvidas nesse combate. Animais pacíficos e aldeões podem ser alvos quando o dono os ataca; sem envolvimento, são ignorados. Aliados de equipe, companheiros do mesmo dono, invocações de jogador e o dono continuam protegidos. Jogadores respeitam `Player.canHarmPlayer` (incluindo regras de PvP do `ServerPlayer`). `PlayerSummon` integra a proteção recíproca já existente com Micos e Presentes aliados.

Durante a defesa mantém movimento orbital a cerca de cinco blocos do alvo. Com linha de visão e alvo a até 12 blocos, prepara durante 12 ticks, dispara e entra em recarga de 30 ticks. Perda de alvo/visão cancela a preparação. A centelha nasce na boca, velocidade 1,2 (`DragonEmber.SPEED`), gravidade zero, percurso de até 12 blocos e vida limitada. Usa `ThrowableItemProjectile` com sprite de 16 × 16 e renderer reduzido com luz própria. Dano direto: 3 HP antes da armadura. Partículas de chama pequena no carregamento, rastro e impacto. Sem dano periódico de fogo.

O projétil ignora aliados na colisão e confere a proteção novamente no impacto. Colide com blocos. Não usa as rotinas de explosão, ignição, transformação de blocos ou `SmallFireball`; portanto não causa incêndios no cenário nem depende de `mobGriefing`.

## Spawn, sons e Bestiário

Tag de biomas `baby_dragon_habitat`: florestas, montanhas, taigas e bosque (`grove`); modificador único com peso 6, grupos 1–2. Spawn em chão sólido, céu aberto, luz ≥ 9, sem fluido e sem bloco ocupando a hitbox. Ovo na aba do mod e na aba vanilla de ovos. Não despawna à distância.

Oito eventos sonoros próprios referenciam apenas eventos vanilla (axolote, allay, papagaio, carga ígnea e blaze), com volume/pitch de filhote; nenhum `.ogg` novo. Capítulo `Bestiary.BABY_DRAGON`: introdução, habitat, comportamento, domesticação, companheirismo e defesa, traduzidos em pt-BR/en-US e com três gravuras. A paginação existente continua textos longos.

## Verificação

`BabyDragonGameTests` só é registrado por `coraline.babyDragonTests=true`, na tarefa `runBabyDragonTest`, com mundo separado em `build/baby-dragon-gametest/`. A suíte cobre os 23 tópicos pedidos e cenários adicionais de parede, alternância de defesa e liberação do ombro bloqueado. A execução inclui um teste do framework.

Resultados, número de execuções e limites da inspeção visual estão registrados em [baby_dragon_validation.md](baby_dragon_validation.md). Prévias offline não substituem a avaliação do movimento, sons e posicionamento em cliente real.


A resposta aos golpes do dono verifica novos eventos a cada tick e reconhece alvos a ate 24 blocos do dragao para permitir aproximacao. O alcance de disparo continua em 12 blocos. A mira antecipa a velocidade do alvo no instante do disparo, compensando o arrasto da centelha; nao ha perseguicao automatica do projetil.
