# O Quebra-Copas (The Canopy Breaker)

Implementação para Minecraft 26.1.2 / NeoForge 26.1.2.124. Boss opcional de floresta antiga: uma fera simiesca arborícola de 3,1 blocos que carrega o **Porrete de Raiz-Morta**, caminha no chão, escala troncos, atravessa copas com saltos balísticos reais e despenca sobre quem estiver embaixo. A prioridade do projeto foi a animação: cada estado de jogo tem uma pose e um movimento legíveis, e a mobilidade nas árvores é executada pelo servidor, sem teletransporte.

> **Estado da verificação.** Compilação e 18 GameTests aprovados (10 execuções seguidas) pelo GameTestServer direto (ver "Verificação"). **Não verificado no jogo:** `gradlew build`/`runCanopyBreakerTest` pelo Gradle, `runClient`, a leitura das animações em movimento, os sons e a aparência da barra de vida. A qualidade visual só foi inspecionada em renderizações offline do modelo real (poses estáticas) e das imagens geradas.

## Identificadores

| Elemento | ID |
|---|---|
| Boss | `codenamecoraline:canopy_breaker` (`/summon codenamecoraline:canopy_breaker`) |
| Ovo | `codenamecoraline:canopy_breaker_spawn_egg` |
| Item de invocação | `codenamecoraline:hollow_trunk_idol` (**Ídolo do Tronco Oco**) |
| Arma rara | `codenamecoraline:deadroot_club` (**Porrete de Raiz-Morta**) |
| Materiais | `canopy_resin` (**Resina da Copa**), `heartwood_splinter` (**Estilhaço de Cerne**) |
| Ícone das conquistas | `canopy_breaker_portrait` (só para conquistas, não obtível no modo normal) |
| Entrada do Bestiário | `Bestiary.CANOPY_BREAKER`, seis páginas |

- Receita do ídolo (`recipe/hollow_trunk_idol.json`): carvalho-escuro, bloco de osso e bloco de musgo em formato de ídolo (` L / LBL / ` + ` R `).
- Saque (`loot_table/entities/canopy_breaker.json`), concedido **uma única vez** na morte: 2 a 4 Resina da Copa; 50% de Estilhaço de Cerne; 30% de Porrete de Raiz-Morta. XP do mob: 60.
- Conquistas (`advancement/bosses/`): `canopy_idol` (obter o ídolo), `canopy_summon` (usá-lo em um tronco), `canopy_defeat` (derrotar o boss, moldura `challenge`) e `canopy_club` (obter o porrete, moldura `goal`). Todas usam o retrato como ícone.

## Invocação (`HollowTrunkIdolItem`)

O ídolo é usado sobre um **tronco** (`TreeNav.isLog`). Consome uma unidade apenas quando **todas** as condições valem; uma falha não gasta o item, toca um golpe de madeira e explica o motivo na barra de ação:

1. o bloco clicado é um tronco (`WRONG_BLOCK`);
2. bioma: taiga de pinheiros antiga, taiga de abetos antiga ou floresta sombria (`WRONG_BIOME`);
3. noite, `12500..23500` ticks (`NOT_NIGHT`);
4. ao menos 14 troncos num raio de 8 blocos, de -2 a +14 de altura (`FEW_TREES`);
5. clareira livre e apoiada perto do tronco para a chegada (`NO_CLEARING`);
6. nenhum outro Quebra-Copas vivo num raio de 96 blocos (`ALREADY_ACTIVE`).

O boss chega com uma introdução curta de 44 ticks (rugido, invulnerável), depois a barra de vida aparece.

## Visual e modelo

- Modelo nativo (`client/canopy/CanopyBreakerModel`), sem biblioteca de animação. Hierarquia: `pelvis → chest → head → jaw`; braços `upper_arm → forearm → hand` (a mão direita carrega `club_hand`); pernas `thigh → shin → foot`; porrete guardado `chest → club_back`, alternado pelo estado `clubDrawn`. Escala de renderização 1,2, atlas 256×256 com 37 caixas sem sobreposição de UV (verificado por teste) e máscara emissiva dos olhos âmbar.
- As caixas e a pintura da textura vêm de `tools/generate_canopy_breaker_assets.py`, que também escreve `CanopyBreakerParts.java`, os ícones dos itens e o modelo 3D do porrete na mão.
- Hitbox 1,8 × 3,1; ao escalar passa a 1,0 × 2,2 (`Pose.CROUCHING`), para abraçar o tronco.
- O estado visual é decidido **no servidor** e sincronizado (`DATA_ANIM`, `DATA_OVERLAY` para o dano, `DATA_CLUB`, `DATA_STAGE`, `DATA_ACTIVITY`); o cliente apenas toca a animação que o servidor escolheu. Assim o que se vê nunca diverge da mecânica.

## Atributos e estágios

| Atributo | Valor |
|---|---|
| Vida | 200 |
| Armadura | 8 |
| Velocidade em terra | 0,33 (×1,04 no estágio 2, ×1,12 no estágio 3) |
| Tamanho | 1,8 × 3,1 blocos (1,0 × 2,2 ao escalar) |
| Dano de queda | nenhum (`causeFallDamage` desligado) |
| Despawn por distância | nunca |

- **Estágio 1 (Guardião da Mata).** Luta no chão, sobe só quando o alvo está alto demais ou fora de alcance.
- **Estágio 2 (60% de vida, Caçador da Copa).** Uma única transição de 50 ticks (rugido, invulnerável, partículas de folhas). Libera a **Queda da Copa** e passa a flanquear pelas árvores.
- **Estágio 3 (25% de vida, Fúria da Madeira).** Uma única transição de 60 ticks. Mais rápido, pausas menores, `FURY_IDLE`, barra vermelha pulsante. Curar o boss nunca repete uma transição nem volta o estágio.

## Combate

Todo golpe tem **preparação** (telegráfica), **execução** e **recuperação**. O dano só é aplicado na janela de execução e apenas a alvos dentro da zona do golpe; **nunca há golpe no ar**: se o alvo sai da zona útil durante a preparação, o ataque é cancelado (`attackStillUseful`) e o boss toca `attack_interrupted`. Um golpe pesado (>= 7% da vida máxima) durante a recuperação, fora do intervalo de 140 ticks, faz o boss cambalear (`STAGGER`).

| Ataque | Dano | Preparo | Execução | Recuperação | Zona e regra |
|---|---|---|---|---|---|
| Pancada de Tronco (`TRUNK_SLAM`) | 7 | 20 | 10 (golpe no tick 6) | 18 | círculo de raio 2,6 à frente; alvo atrás não é atingido |
| Varredura da Copa (`CANOPY_SWEEP`, esquerda/direita) | 5 | 16 | 10 (golpe no tick 7) | 18 | arco frontal de alcance 4,6, lado escolhido pela posição do alvo |
| Bote Bruto (`BRUTE_LUNGE`) | 6 | 10 | 12 | 18 | avanço a 0,85 b/tick, de 4,5 a 10 blocos, exige linha de visão |
| Revés (`BACKHAND`) | 4 | 10 | 6 (golpe no tick 3) | 10 | alcance 3,2, para quem chega perto demais |
| Queda da Copa (`CANOPY_DROP`) | 8 | 14 | salto balístico | 26 | só após descida real, círculo de raio 3,4 marcado no chão; uma vez por subida |
| Rugido Intimidador (`INTIMIDATE`) | 0 | 30 | - | - | rugido e partículas; **não retira o controle do jogador** |

- Escolha ponderada (`pickAttack`): respeita alcance, altura, linha de visão, recargas (50, 44, 90, 60, 200 e 480 ticks) e evita repetir o ataque anterior. Pesos: Revés 5 (7 na fúria), Pancada 6, Varredura 5, Bote 5, Rugido 1.
- Alvo (`chooseTarget`): o último jogador que o feriu (por 100 ticks); senão o mais próximo; mantém o alvo atual com histerese de 4 blocos.
- O porrete é sacado (`DRAW_CLUB_*`, 32 ticks) quando o alvo está a menos de 24 blocos ou quando ele é ferido; o porrete só troca de mão no meio da animação.

## Mobilidade nas árvores

O corpo tem uma máquina de estados (`Activity`): `INTRO, GROUND, DRAW_CLUB, ATTACK, ROAR, CLIMB_APPROACH, CLING, CLIMB_UP, CLIMB_DOWN, BRANCH, JUMP_PREPARATION, AIRBORNE, LANDING, LAND_RECOVERY, FALL_RECOVERY, STAGGER, TRANSITION, DYING, DEAD`.

1. **Decisão (`considerRoute`).** Alvo elevado (>= 3,5 blocos acima) ou boss preso por 60 ticks: rota de reposicionamento até a copa perto do alvo. No estágio 2+, com a Queda da Copa pronta e alvo entre 6 e 20 blocos no mesmo nível, um sorteio por tick inicia um flanco.
2. **Planejamento completo antes de se comprometer (`planRoute`).** Tronco utilizável (`TreeNav.bestTrunk`, até 12 blocos), ponto de aproximação alcançável a pé, poleiro no topo da copa, pouso livre e **salto fisicamente possível** (`TreeNav.planJump`). Se qualquer elo falha, não há rota e o boss continua no chão.
3. **Aproximação e agarre.** Corre até o tronco (`CLIMB_APPROACH`), agarra a casca (`CLING`, 14 ticks) e sobe a 0,2 b/tick com pausas de reposicionamento a cada 34 ticks. Durante a subida o corpo usa `noPhysics`, mas **cada passo é validado**: passa por folhas, nunca por troncos nem blocos sólidos (`climbStep`).
4. **Aderência.** A cada tick o tronco ao lado precisa existir (`hasGrip`). Sem ele (tronco quebrado, bloqueio) o boss **perde o apoio**: solta, grita, cai com física normal e fica em `FALL_RECOVERY` até pousar, sem teleporte.
5. **Poleiro e preparação do salto.** No topo, `BRANCH` equilibra, atualiza o pouso conforme o alvo se move e segue para `JUMP_PREPARATION` (agachamento).
6. **Salto balístico.** O plano (`planJump`) procura o menor tempo de voo (8 a 44 ticks) cuja velocidade horizontal (até 1,15) e vertical (0,3 a 1,1) são permitidas e cuja trajetória inteira, em meios ticks e com a caixa real do corpo, está livre de blocos sólidos. A cada tick de preparação o plano é recalculado; **se o destino ficou inválido, o salto é cancelado** e o boss desce pelo tronco. Em voo a velocidade horizontal é corrigida suavemente rumo ao pouso.
7. **Pouso.** `LANDING` decide por distância e altura: leve (copa), pesado (chão) ou impacto da Queda da Copa (dano em área uma única vez). Um salto que não saiu do poleiro conta como falho: uma nova tentativa e, na segunda falha, desce pelo tronco.
8. **Retorno ao chão.** Cancelar a rota no alto reagarra o tronco e usa `CLIMB_DOWN` (`abortRoute`); nunca há reaparição em outro lugar.

Limites medidos: saltos práticos de até cerca de 10 a 12 blocos horizontais. Em florestas com copas muito densas ao redor do pouso o plano pode não achar trajetória livre; nesse caso o boss luta no chão.

## Lacaios: Micos de Casca (`bark_macaque`)

Extensão da inteligência do boss, não mobs soltos: controlam o terreno, distraem o jogador e criam aberturas para os golpes. Ver D-029.

| Atributo | Valor |
|---|---|
| ID / ovo | `codenamecoraline:bark_macaque` / `bark_macaque_spawn_egg` (**Mico de Casca**) |
| Vida / armadura / velocidade | 14 / 2 / 0,32 |
| Tamanho | 0,7 × 0,85 |
| Mordida | 3 de dano, uma por salto |
| Dano de queda | nenhum |

- **Comportamento (`entity/macaque/BarkMacaque`).** Só tem jogadores como alvo (nunca o boss, outros Micos ou outros mobs). Flanqueia por um ponto ao lado do alvo, circula a 4,2 a 5,5 blocos e só ataca quando o alvo olha para outro lado (ou após 60 ticks de espera). O ataque é um salto balístico real: agacha (8 ticks), recebe velocidade uma única vez, morde ao encostar na caixa do alvo e salta para trás (24 ticks de recuo; recarga de 70 ticks). Sem acerto, aterrissa e recupera.
- **Subir em árvores.** Alvo 3 ou mais blocos acima: rota de tronco (`TreeNav`: tronco, ponto de aproximação alcançável a pé, poleiro na copa); a subida valida cada passo contra blocos sólidos (folhas passam, troncos não) e exige o tronco ao lado a cada tick; perdendo o apoio, ele solta e cai com física normal. Sem árvore, usa a navegação de escalada de paredes do jogo (`WallClimberNavigation`). Sem teletransporte.
- **Vínculo.** O Mico chamado guarda o UUID do boss. Quando o boss morre ou é removido, os Micos somem com uma nuvem de folhas; se o boss desaparece sem passar por esses caminhos, a regra de órfão remove o Mico em até 10 ticks após os 40 primeiros ticks de vida. O boss nunca os atinge (a Varredura atravessa os lacaios) e eles nunca ferem uns aos outros.
- **Chamados (`CanopyBreaker.callMinions`).** Abertura: o boss ruge e dois Micos descem das copas (a introdução do ídolo ou, sem ídolo, o primeiro alvo avistado, com a animação de rugido). Estágio 2 (60%): um reforço. Estágio 3 (25%): um último reforço. Limite de **quatro lacaios simultâneos**, contando os vivos e os já a caminho; com a arena cheia o chamado não faz nada. Cada chamado é anunciado por folhas no ponto de chegada, grito e rugido antes de o Mico entrar. Curar o boss não repete chamados e não há reaparecimento contínuo: são no máximo quatro Micos por luta.
- **Visual.** Modelo próprio (`client/macaque`, 25 caixas, atlas 64×64, olhos emissivos), gerado por `tools/generate_bark_macaque_assets.py`; 12 animações por keyframe (`MacaqueAnim`): idle, caminhar, correr, escalar, agachar para o salto, no ar, aterrissar, mordida, recuo, grito, dano e morte, escolhidas pelo servidor e sincronizadas. Poses inspecionadas apenas em renderizações offline estáticas.
- **Não implementado ainda:** o **Javali de Raiz** da proposta (24 PV, armadura 3, velocidade 0,28, investidas curtas). Fica como segunda camada tática se os testes de jogo mostrarem que a luta precisa de mais pressão terrestre; no estágio 2 o reforço atual é um Mico, não um Javali.
- **Não verificado no jogo:** a leitura das animações em movimento, o balanceamento (14 PV, 3 de dano, limite de quatro) e os sons.

## Barra de vida (`client/bossbar/CanopyBreakerBossBar`)

Registrada em `ThemedBossBars` para `entity.codenamecoraline.canopy_breaker`. Moldura de 288×44 (`tools/generate_boss_bar_canopy_breaker.py`): casca escura rachada com três marcas de garra em cada lado, raízes retorcidas com agulhas de pinheiro e musgo nas pontas, a cabeça da fera (sobrancelha pesada, olhos âmbar, presas) sobre o meio, e entalhes de osso nas marcas de 60% e 25%. A fenda de vida é transparente: a moldura é desenhada por cima do preenchimento, de modo que raízes e rosto o cobrem. A cor do preenchimento segue o estágio (verde-musgo, âmbar e brasa pulsante) e o servidor a define por estágio. Prévia: `build/canopy-breaker-bossbar/review.png`.

## Bestiário

Seis páginas (`Bestiary.CANOPY_BREAKER`): anatomia (gravura), tradição e invocação, atributos e estágios, estratégia, o porrete em movimento (gravura) e despojos (gravura), em português e inglês. As gravuras de 112×80, 112×72 e 112×64 saem de `tools/ExportCanopyBreakerModel.java` (modelo e poses reais), `tools/canopy-breaker-sketches.gradle` e `tools/generate_canopy_breaker_sketches.py`.

## Animation Bible

Uma animação por estado de jogo, nunca por sorteio visual. Uma pose de referência do porrete (`CARRY`, `WIND`, `MID`, `IMPACT`, `GROUND`) define a trajetória de todos os golpes; o ângulo do porrete é a soma dos ângulos de tronco, ombro, antebraço e punho. As poses são definidas em valores absolutos (`Pose.of("parte rx ry rz")`) e convertidas para deslocamentos relativos à postura de repouso.

<!-- ANIMATION-BIBLE:START -->

Gerada por `tools/generate_canopy_animation_bible.py` a partir de `CanopyAnim` e `CanopyBreakerAnimations`; não edite à mão. As colunas de estado, evento, interrupção e próxima seguem o texto do enum (inglês).


#### A. personality / idle

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `idle_breathing` | sim | 3.20 | GROUND, standing still | none | any action | idle_watch, walk |
| `idle_watch` | sim | 4.00 | GROUND, standing, player in sight | none | any action | idle_breathing |
| `idle_sniff` | não | 2.00 | GROUND, standing, random | none | any action | idle_breathing |
| `idle_scratch` | não | 2.40 | GROUND, standing, random | none | any action | idle_breathing |
| `idle_club_drag` | não | 2.00 | GROUND, club drawn, random | scrape sound | any action | idle_breathing |
| `idle_roar_short` | não | 1.40 | GROUND, standing, random, long idle | short grunt sound | any action | idle_breathing |

#### B. ground locomotion

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `walk` | sim | 1.20 | GROUND, moving slowly | step sounds | any action | run_four_limb, stop_moving |
| `run_four_limb` | sim | 0.70 | GROUND, chasing far target | step sounds | any action | run_bipedal, stop_moving |
| `run_bipedal` | sim | 0.60 | GROUND, short run before an attack or jump | step sounds | any action | attack, jump_prepare |
| `start_moving` | não | 0.40 | GROUND, first ticks of movement | none | any action | walk |
| `stop_moving` | não | 0.50 | GROUND, speed drops to zero | foot plant | any action | idle_breathing |
| `turn_left` | não | 0.60 | GROUND, large turn to the left | none | any action | walk, idle |
| `turn_right` | não | 0.60 | GROUND, large turn to the right | none | any action | walk, idle |
| `step_up` | não | 0.50 | GROUND, climbing a low obstacle | none | any action | walk |
| `fall` | sim | 0.60 | AIRBORNE without a planned jump (lost support) | none | landing | fall_recovery |

#### C. tree climbing

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `climb_approach` | sim | 0.60 | CLIMB_APPROACH, running to the trunk | step sounds | target lost, hurt stagger | climb_grab |
| `climb_grab` | não | 0.50 | CLIMB_APPROACH reached the trunk | hands catch the bark (wood hit) | support lost | climb_up |
| `climb_up` | sim | 0.90 | CLIMBING_UP | bark scrapes (wood hit) | support lost, hurt stagger | cling_idle, branch_launch |
| `climb_down` | sim | 1.00 | CLIMBING_DOWN | bark scrapes | support lost, hurt stagger | cling_idle, walk |
| `cling_idle` | sim | 3.00 | CLINGING | none | support lost, route change | cling_reposition, climb_up |
| `cling_reposition` | não | 1.00 | CLINGING, adjusting grip | wood hit | support lost | cling_idle |
| `branch_traverse_left` | sim | 0.90 | BRANCH_TRAVERSE, moving left along the canopy | leaf steps | support lost | branch_balance |
| `branch_traverse_right` | sim | 0.90 | BRANCH_TRAVERSE, moving right along the canopy | leaf steps | support lost | branch_balance |
| `branch_balance` | sim | 2.00 | BRANCH_TRAVERSE, standing on the canopy | none | jump plan | branch_crouch |
| `branch_crouch` | não | 0.50 | JUMP_PREPARATION on the canopy | wood creak | destination invalid | branch_launch, jump_abort |
| `branch_launch` | não | 0.40 | JUMP_PREPARATION to AIRBORNE | leaf burst, jump sound | none | jump_airborne |
| `branch_grab` | não | 0.50 | LANDING on a trunk or branch | wood hit | none | land_recovery |
| `climb_failed` | não | 0.80 | support lost while climbing | scream | landing | fall, fall_recovery |

#### D. jumps and landings

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `jump_prepare` | não | 0.60 | JUMP_PREPARATION on the ground | grunt | destination invalid | jump_takeoff, jump_abort |
| `jump_takeoff` | não | 0.35 | start of the ballistic trajectory | jump sound, leaf burst | none | jump_airborne |
| `jump_airborne` | sim | 0.90 | AIRBORNE (traversal jump) | none | landing | jump_land_light, jump_land_heavy |
| `jump_steer` | sim | 0.80 | AIRBORNE, trajectory corrected | none | landing | jump_land_* |
| `jump_land_light` | não | 0.60 | LANDING on a canopy or ledge | soft landing, leaves | none | land_recovery |
| `jump_land_heavy` | não | 0.80 | LANDING on the ground | thud, dust and leaves | none | land_recovery |
| `land_recovery` | não | 1.10 | LANDING_RECOVERY | pant | stagger | idle, attack |
| `jump_abort` | não | 0.40 | JUMP_PREPARATION cancelled | none | none | walk, idle |

#### E. club

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `draw_club_prepare` | não | 0.40 | DRAW_CLUB step 1 | none | hurt stagger | draw_club |
| `draw_club` | não | 0.60 | DRAW_CLUB step 2: the club swaps from the back to the hand | wood hit | hurt stagger | draw_club_finish |
| `draw_club_finish` | não | 0.60 | DRAW_CLUB step 3 | none | none | club_guard_idle |
| `club_guard_idle` | sim | 2.40 | GROUND, club drawn, standing | none | any action | club_guard_walk |
| `club_guard_walk` | sim | 1.20 | GROUND, club drawn, moving | step sounds | any action | club_guard_idle |
| `club_drag` | sim | 1.60 | GROUND, club drawn, dragging the club | scrape | any action | club_guard_idle |
| `club_recover_grip` | não | 0.70 | after a heavy swing | none | any action | club_guard_idle |
| `club_stow` | não | 0.80 | optional: club back to the back | wood hit | none | idle_breathing |

#### F. attacks

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `attack_trunk_slam_prepare` | não | 1.00 | ATTACK prepare: Trunk Slam | roar_attack | target lost (cancel) | attack_trunk_slam_execute |
| `attack_trunk_slam_prepare_alt` | não | 1.10 | ATTACK prepare: Trunk Slam, stepping variant | roar_attack | target lost (cancel) | attack_trunk_slam_execute |
| `attack_trunk_slam_execute` | não | 0.50 | ATTACK execute: Trunk Slam | DAMAGE at impact tick | hurt does not cancel | attack_trunk_slam_recovery |
| `attack_trunk_slam_recovery` | não | 1.00 | ATTACK recover: Trunk Slam | none | none | club_recover_grip |
| `attack_canopy_sweep_prepare_left` | não | 0.80 | ATTACK prepare: Canopy Sweep (arc to the left) | swing sound | target lost (cancel) | attack_canopy_sweep_execute_left |
| `attack_canopy_sweep_prepare_right` | não | 0.80 | ATTACK prepare: Canopy Sweep (arc to the right) | swing sound | target lost (cancel) | attack_canopy_sweep_execute_right |
| `attack_canopy_sweep_execute_left` | não | 0.50 | ATTACK execute: Canopy Sweep left | DAMAGE at mid-arc tick | hurt does not cancel | attack_canopy_sweep_recovery |
| `attack_canopy_sweep_execute_right` | não | 0.50 | ATTACK execute: Canopy Sweep right | DAMAGE at mid-arc tick | hurt does not cancel | attack_canopy_sweep_recovery |
| `attack_canopy_sweep_recovery` | não | 0.90 | ATTACK recover: Canopy Sweep | none | none | club_recover_grip |
| `attack_brute_lunge_prepare` | não | 0.50 | ATTACK prepare: Brute Lunge | roar_attack | target lost (cancel) | attack_brute_lunge_execute |
| `attack_brute_lunge_execute` | não | 0.60 | ATTACK execute: Brute Lunge (committed advance) | DAMAGE at the end of the advance | hurt does not cancel | attack_brute_lunge_recovery |
| `attack_brute_lunge_recovery` | não | 0.90 | ATTACK recover: Brute Lunge | pant | none | club_recover_grip |
| `canopy_drop_prepare` | não | 0.70 | JUMP_PREPARATION for the Canopy Drop (on the canopy) | roar_attack, ground marker | destination invalid (cancel) | canopy_drop_airborne |
| `canopy_drop_airborne` | sim | 0.80 | AIRBORNE, Canopy Drop trajectory | none | landing | canopy_drop_impact |
| `canopy_drop_impact` | não | 0.60 | LANDING from the Canopy Drop | DAMAGE once, dust and leaves | none | canopy_drop_recovery |
| `canopy_drop_recovery` | não | 1.30 | LANDING_RECOVERY after the drop (vulnerable) | pant | stagger | club_guard_idle |
| `attack_backhand_prepare` | não | 0.50 | ATTACK prepare: Backhand | grunt | target lost (cancel) | attack_backhand_execute |
| `attack_backhand_execute` | não | 0.30 | ATTACK execute: Backhand | DAMAGE at contact tick | hurt does not cancel | attack_backhand_recovery |
| `attack_backhand_recovery` | não | 0.50 | ATTACK recover: Backhand | none | none | club_guard_idle |

#### G. roars

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `roar_idle` | não | 1.20 | GROUND, distant short roar | roar sound | any action | idle_breathing |
| `roar_aggro` | não | 1.60 | target sighted / intro | roar sound (cooldown) | death | club_guard_idle |
| `roar_attack` | não | 0.50 | start of a heavy attack | roar sound (cooldown) | death | attack prepare |
| `roar_phase_two` | não | 2.00 | stage 2 transition | roar sound | death | club_guard_idle |
| `roar_hurt` | não | 0.50 | heavy damage taken | roar sound (cooldown) | death | club_guard_idle |
| `roar_stagger` | não | 0.60 | STAGGER | roar sound | death | stumble |
| `roar_death` | não | 1.00 | DYING, first beat | roar sound | none | death_fall |
| `pant_recovery` | sim | 1.20 | LAND_RECOVERY or ATTACK recovery, tired | pant | any action | idle |
| `intimidate_roar` | não | 1.50 | ATTACK: Intimidating Roar | roar sound, knock-back of nothing | death | club_guard_idle |

#### H. reactions

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `hurt_front` | não | 0.35 | damage from the front (overlay) | hurt sound | none | previous animation |
| `hurt_side` | não | 0.35 | damage from the side (overlay) | hurt sound | none | previous animation |
| `hurt_back` | não | 0.35 | damage from behind (overlay) | hurt sound | none | previous animation |
| `stagger` | não | 0.80 | STAGGER: heavy hit during recovery or after a failed jump | grunt | death | stumble, knockback_recover |
| `stumble` | não | 0.70 | after stagger | none | death | knockback_recover |
| `knockback_recover` | não | 0.80 | after strong knock-back | none | death | club_guard_idle |
| `attack_interrupted` | não | 0.60 | an attack was cancelled (target left) | none | death | club_guard_idle |
| `target_lost` | não | 1.60 | no target for a while | sniff | any action | idle_breathing |
| `taunt` | não | 1.40 | pressure beat between attacks, stage 2+ | roar sound (cooldown) | any action | club_guard_idle |

#### I. stages and death

| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `phase_two_transition` | não | 2.50 | TRANSITION to stage 2 (60% health) | roar, invulnerable | death | club_guard_idle |
| `fury_transition` | não | 3.00 | TRANSITION to stage 3 (25% health) | roar, club slam, invulnerable | death | fury_idle |
| `fury_idle` | sim | 2.00 | stage 3 standing | none | any action | fury movement |
| `death_prepare` | não | 0.90 | DYING 1 | none | none | death_fall |
| `death_fall` | não | 0.80 | DYING 2 | none | none | death_impact |
| `death_impact` | não | 0.50 | DYING 3 | heavy thud, leaves | none | death_final |
| `death_final` | não | 1.00 | DYING 4 / DEAD | none | none | removed |

Total: **88 animações**, todas com keyframes definidos.

<!-- ANIMATION-BIBLE:END -->

### Poses de validação

Renderizações offline do modelo real (poses estáticas, sem movimento) usadas para validar a legibilidade: parado, observando, porrete nas costas, saque, guarda, preparo e impacto da Pancada de Tronco, preparo e impacto da Varredura, bote, salto, agarre, subida, travessia, pouso, Queda da Copa no ar e no chão, rugido de fúria, dano e morte. As imagens foram geradas em `build/` e inspecionadas durante o desenvolvimento; **elas não substituem a leitura das animações em movimento, que depende de `runClient`.**

## Verificação

`CanopyBreakerGameTests` (26 testes obrigatórios, 25 do mod e um do framework; os 17 do boss e os 8 dos lacaios), habilitados por `-Dcoraline.canopyTests=true` (tarefa `runCanopyBreakerTest`). Cada teste monta o próprio terreno em altitude relativa Y=100 (chão de pedra e abetos com folhas persistentes) para que as arenas paralelas não interfiram:

| Teste | O que prova |
|---|---|
| `canopy_registration_and_attributes` | registro, 200 PV, armadura 8, velocidade 0,33, hitbox, itens, ovo |
| `canopy_animation_catalogue_and_uv_layout` | todas as animações definidas e assadas, IDs únicos, UV sem sobreposição |
| `canopy_idol_conditions` | cada condição de invocação, item não consumido na falha, um chefe só |
| `canopy_chases_and_draws_club` | persegue de longe e saca o porrete |
| `canopy_attack_trunk_slam` | sem dano no preparo, dano à frente, nada atrás, fases |
| `canopy_attack_sweep_and_backhand` | arco frontal, nada atrás, revés a curta distância |
| `canopy_attack_lunge_and_cancel` | bote atinge a 8 blocos; preparo cancelado quando o alvo some |
| `canopy_attack_animation_sync` | preparo, execução e recuperação tocam em ordem |
| `canopy_tree_detection` | tronco, aproximação, poleiro, ocupação, salto impossível rejeitado |
| `canopy_climbs_and_jumps_between_trees` | subida real, salto e pouso na outra copa sem teleporte |
| `canopy_drop_attack_lands_once` | Queda da Copa pousa e causa dano uma única vez |
| `canopy_invalid_jump_is_cancelled` | parede no caminho cancela o salto e ele desce pelo tronco |
| `canopy_lost_grip_falls_and_recovers` | tronco cortado: cai, recupera e volta ao chão |
| `canopy_no_trees_falls_back_to_ground` | sem árvores não há rota; persegue no chão |
| `canopy_stages_transition_once` | 60% e 25% uma vez cada, cura não repete |
| `canopy_stagger_and_hurt_overlay` | golpe pesado na recuperação cambaleia; sobreposição de dano |
| `canopy_death_loot_and_advancements` | morte uma vez, saque, quatro batidas da morte, remoção, Bestiário, conquistas, gravuras, barra |

Os 8 testes dos lacaios (`macaque_*` e `minions_*`) cobrem registro e animações, salto e mordida única sem teletransporte, subida de tronco até um alvo alto, vínculo (boss e lacaios nunca se ferem), chamado de abertura com e sem ídolo, reforços por estágio com o limite de quatro, saída dos lacaios com a morte do boss e remoção de órfãos.

Como o Gradle não executa no shell do agente (ver `docs/DEVELOPMENT.md`), os testes foram rodados pelo GameTestServer direto. Rode no terminal: `.\gradlew.bat build`, `.\gradlew.bat runCanopyBreakerTest` e as demais tarefas de teste.
