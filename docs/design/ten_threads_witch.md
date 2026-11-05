# A Bruxa dos Dez Fios (`ten_threads_witch`)

Boss composto e sequencial de floresta antiga. **Ester, a Matriarca Costureira**, uma velha frágil de 1,15 bloco, recusou a própria morte e dividiu medos, dores e memórias em dez marionetes monstruosas de pano, porcelana, bronze e raiz, presas ao seu Cajado do Tear Vivo por dez fios de éter prateado. Só uma marionete luta de cada vez; a Matriarca não pode ser ferida enquanto restar uma. Quando a décima cai, ela arranca a venda e luta sozinha (estágio 11). Original: nada de nomes, logos ou textos protegidos.

Estado: **implementado e coberto por GameTests** (ver "Testes"). **Visual, animações em movimento, sons, sensação de combate, barra de vida e gravuras no livro não foram verificados no jogo** (`runClient` pendente).

## Ficha

| Item | Valor |
|---|---|
| Nomes | A Bruxa dos Dez Fios / The Witch of Ten Threads; Ester, a Matriarca Costureira |
| Caixa | 0,6 × 1,15, olhos a 0,95 (modelo em tamanho real) |
| Atributos | 150 PV, armadura 0, velocidade 0,28 (0,40 no estágio 11), dano 10, sem repulsão extra |
| XP | 700 (a Matriarca) e 50 por marionete |
| Imunidades | queda, congelamento (tag `freeze_immune_entity_types`), neve em pó andável (tag `powder_snow_walkable_mobs`); nunca some por distância |
| Estados | `EncounterPhase`: `DORMANT, INTRO, STAGE_01..STAGE_10, STAGE_11_TRANSITION, STAGE_11_FURY, DEATH_CINEMATIC` |
| Ajudantes | `thread_hitbox` (trecho cortável do fio) e `spool_vital_orb` (orbe de recuperação) |

## Paleta (hex, usada pelos geradores)

| Uso | Cor |
|---|---|
| Madeira escura / clara do tear e do cajado | `#2B1D14` / `#4A3426` |
| Juta (corpo, véu) | `#8C7A6B` (sombra `#6E5D4F`, luz `#A69482`) |
| Renda | `#C2B9A7` (sombra `#968E7E`) |
| Porcelana | `#EAE6DF` (sombra `#C8C2BA`), rachadura `#3A3532` |
| Azul-cobalto (pintura do cão) | `#3B5978` |
| Osso | `#D6CEBF` (sombra `#9E9482`) |
| Bronze (sino) | `#4A3B2C` (luz `#68543E`), pátina `#3E7A6B` |
| Fio de éter | `#D8E2E8` (sombra `#A0B0BA`) |
| Olhos e brasas | âmbar `#FFB02E` (realce `#FFE296`) |
| Carretéis rubi | `#E0115F` (realce `#FF78AA`) |
| Fio carmesim (alerta) | `#FF1E42` |
| Casca, musgo | `#3A2A1E`, `#465F34` |
| Pano escuro, tendões, pele | `#3A2F2A`, `#5A1F2A`, `#B8A090` |
| Contorno | `#140C0A` |

## Atributos das marionetes

Tabela de `PuppetKind` (única fonte; o código e os testes leem dela). O estágio é a posição na lista.

| # | Marionete (`id`) | Caixa (L × A, olhos) | PV | Armadura | Resist. a repulsão | Velocidade | Dano | Voa | Altura de degrau |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Aracnídea de Costura (`sewing_arachnid`) | 1,3 × 1,1, 0,65 | 80 | 2 | 0,2 | 0,34 | 6 | não | 1,0 |
| 2 | Cão de Porcelana (`porcelain_hound`) | 0,9 × 1,4, 1,1 | 95 | 10 (0 depois de rachar) | 0,3 | 0,41 | 7 | não | 1,0 |
| 3 | Sino Oco (`hollow_bell`) | 1,4 × 2,5, 2,1 | 110 | 8 | 0,8 | 0,26 | 8 | sim (paira) | 1,0 |
| 4 | Cervo Retalhado (`patchwork_stag`) | 1,4 × 3,1, 2,6 | 125 | 4 | 0,5 | 0,38 | 9 | não | 1,5 |
| 5 | Gralha Costurada (`stitched_rook`) | 1,5 × 2,2, 1,8 | 115 | 3 | 0,2 | 0,42 | 8 | sim | 1,0 |
| 6 | Noiva Oca (`hollow_bride`) | 0,8 × 2,7, 2,3 | 135 | 5 | 0,4 | 0,33 | 7 | não | 1,2 |
| 7 | Centopeia de Saias (`skirted_centipede`) | 1,0 × 0,9, 0,7 | 145 | 6 | 0,7 | 0,39 | 8 | não | 1,0 |
| 8 | Mãe das Raízes (`root_mother`) | 2,0 × 3,0, 2,4 | 160 | 10 | 1,0 | 0,22 | 12 | não | 1,0 |
| 9 | Carrasco de Retalhos (`patchwork_executioner`) | 1,8 × 3,8, 3,2 | 185 | 12 | 0,9 | 0,25 | 13 | não | 1,0 |
| 10 | Gigante do Berço (`cradle_giant`) | 1,6 × 4,2, 3,8 | 220 | 8 | 0,9 | 0,30 | 12 | não | 1,4 |

Todas as marionetes são a mesma classe (`PuppetEntity`); o que as diferencia é essa tabela, o modelo e os sons. A Mãe das Raízes cura 2 PV por segundo dentro d'água. O Cervo quebra folhas por onde investe.

## Estágios e sequência

1. A Matriarca se revela (`INTRO`, 60 ticks): os dez corpos pendem a 11 blocos do centro, a 4 de altura, presos por fios (partículas) ao dossel; ela fala a fala de abertura.
2. Estágio *n* (1 a 10): a marionete *n* é puxada até um ponto a 7 blocos do centro, na direção do jogador, e acorda (20 ticks, invulnerável). Só ela luta; a Matriarca fica **intocável** (`hurtServer` devolve falso) e as outras pendem invulneráveis.
3. Quando a marionete cai: o carretel correspondente estoura (bit em `getBrokenSpoolsBitmask`), caem dois **Orbes de Carretel Vital** e um **Retalho de Marionete**; 20 ticks de reação da Matriarca e 25 de invocação (`REACT_TICKS + SUMMON_TICKS`), e o estágio seguinte começa. Nenhum estágio é pulado; se a marionete some (recarga do mundo, comando), a mesma é invocada de novo.
4. Depois da décima: `STAGE_11_TRANSITION` (80 ticks de colapso, intocável) e `STAGE_11_FURY`: 150 PV cheios, velocidade 0,40, vulnerável, barra vermelha.
5. Morte (`DEATH_CINEMATIC`, 80 ticks): fio se desfaz em pó prateado; ao fim nasce **uma** caixa de costura (baú) com a recompensa; todas as marionetes somem com ela.

Falas (chave `dialogue.codenamecoraline.ten_threads_witch.<id>`, mostradas na barra de ação a até 48 blocos, com som de voz e gancho `WitchDialogueManager.setVoiceHook` para uma voz sintetizada opcional): `intro`, `stage_1` a `stage_10`, `thread_cut` (no máximo uma a cada 60 ticks), `fury`, `death` — 14 linhas em pt_br e en_us.

## O Fio Carmesim e o estado ENROSCADA

Antes de cada ataque **especial** (o terceiro de cada marionete, `special`), o fio que a prende ao cajado fica **carmesim** e zumbe (animação `WINDUP_CRIMSON`, partículas e som de tensão). Nessa janela existe um `thread_hitbox` entre o cajado e a costa da marionete:

- Um golpe de jogador ou uma flecha nele **corta o fio**: o ataque é cancelado, a marionete perde 15% da vida máxima (dano mágico, ignora armadura), cai **ENROSCADA** por 70 ticks (3,5 s, imóvel, sem atacar) e recebe **×1,75** de dano; o jogador ganha a conquista *Corte na Costura* e a Matriarca reclama.
- Marionetes com `Cut.RECOVERY` (Centopeia, Gigante) expõem o fio na recuperação do especial, não no preparo.
- **Desviar** também pune: um especial que não acerta ninguém (`missTangles`) deixa a marionete enroscada pelo tempo `tangleTicks` do ataque. Uma investida contra bloco ou escudo é um *choque* e também a enrosca.
- O fio só se corta com o hitbox ativo; fora dele `cutThread` devolve falso e nada muda. O hitbox só é cortado por jogador ou projétil (marionetes e golems não).

## Ataques

Tempos em ticks (20 por segundo): preparo / execução / recuperação. Dano em PV (corações = ÷2). *Alcance* é a faixa em que o ataque pode ser escolhido. Todos os ataques seguem as regras comuns abaixo. Fonte: `PuppetKind`.

| Marionete | Ataque (`id`) | Estilo | Alcance | Tempos | Dano | Observações |
|---|---|---|---|---|---|---|
| Aracnídea | `ground_stitch` | leque 30° | 4 a 12 | 16/10/14 | 4 | Lentidão III por 2 s; recarga 40 |
| | `needle_jab` | 2 estocadas | 0 a 3,2 | 12/10/12 | 6 + 6 | recarga 30 |
| | `canopy_plunge` (**especial**) | salto | 3 a 10 | 24/14/20 | 11 | pousa onde o jogador *estava* ao travar a mira; fio cortável no preparo; errar a enrosca 70; recarga 140 |
| Cão | `hinge_bite` | cone | 0 a 3 | 10/8/12 | 7 | Definhamento 2 s; recarga 30 |
| | `shard_shake` | anel | 0 a 4 | 16/12/16 | 5 | recarga 80 |
| | `porcelain_lunge` (**especial**) | investida | 4 a 14 | 22/16/18 | 12 | empurrão 1,4; contra escudo ou parede **racha** (armadura 10 → 0) e enrosca; recarga 120 |
| Sino | `repulse_toll` | anel | 0 a 9 | 20/12/18 | 8 | repulsão 1,0; só no chão; recarga 60 |
| | `bronze_slam` | anel | 0 a 4 | 18/10/20 | 10 | recarga 60 |
| | `funeral_resonance` (**especial**, não enrosca) | ondas (3 anéis) | 0 a 10 | 40/24/24 | 13 | Náusea 3 s; fio cortável no preparo; recarga 160 |
| Cervo | `antler_sweep` | cone 180° | 0 a 4 | 16/10/16 | 9 | repulsão 0,8; recarga 40 |
| | `stilt_stomp` | cone 130° | 0 a 3,5 | 18/10/16 | 11 | recarga 50 |
| | `impaling_stampede` (**especial**) | investida | 5 a 16 | 26/20/20 | 14 | atravessa folhas; um tronco à frente o enrosca; recarga 150 |
| Gralha | `tattered_gust` | cone 100° | 0 a 6 | 14/12/14 | 4 | repulsão 1,6; recarga 40 |
| | `quill_barrage` | leque 50° | 5 a 15 | 18/10/16 | 6 | recarga 70 |
| | `shear_dive` (**especial**) | investida em mergulho | 6 a 16 | 28/16/18 | 13 | recarga 150 |
| Noiva | `eight_arm_flurry` | 4 golpes | 0 a 3,2 | 14/16/16 | 3,5 × 4 | Veneno 2 s; recarga 50 |
| | `thread_sidestep` (reativo) | passo lateral | — | 4/8/6 | 0 | dispara depois de 3 golpes em 40 ticks |
| | `bridal_veil_grapple` (**especial**) | agarrar | 4 a 9 | 22/14/18 | 13 | puxa o alvo (1,5); fio cortável no preparo; recarga 140 |
| Centopeia | `procession_coil` | anel | 3 a 7 | 14/30/14 | 7 | só no chão; recarga 60 |
| | `pin_spit` | leque 70° | 4 a 11 | 16/10/14 | 8 | recarga 50 |
| | `burrow_ambush` (**especial**) | escavar | 3 a 16 | 30/14/36 | 14 | some sob o chão e emerge no ponto travado; fio cortável **na recuperação**; recarga 160 |
| Mãe | `maternal_sweep` | cone 220° | 0 a 4,5 | 20/12/18 | 12 | repulsão 1,6; recarga 50 |
| | `bramble_snare` | armadilha | 4 a 12 | 20/10/16 | 5 | Enraizamento (Lentidão VII) 2,5 s; recarga 90 |
| | `deep_rooting` (**especial**, não enrosca) | ondas | 0 a 9 | 60/60/20 | 10 | canaliza; fio cortável no preparo (por trás); recarga 180 |
| Carrasco | `dragging_cleave` | anel | 0 a 5 | 22/14/20 | 13 | só no chão; recarga 60 |
| | `tectonic_hop` | salto | 5 a 12 | 20/14/18 | 11 | recarga 90 |
| | `guillotine_smash` (**especial**) | cone 100° | 0 a 6 | 28/10/90 | 18 | quebra escudo; braço preso na recuperação (nuca ×2,5); recarga 150 |
| Gigante | `cross_loom_slash` | cone 140° | 0 a 4,8 | 16/10/14 | 12 | recarga 50 |
| | `forest_wire_trap` | arame | 5 a 14 | 20/16/16 | 10 | só acerta quem está no chão (um pulo o vence); recarga 90 |
| | `six_limb_barrage` (**especial**) | 6 golpes | 0 a 6 | 18/36/40 | 7 × 5 + 14 | fio cortável **na recuperação**; recarga 160 |
| Matriarca (estágio 11) | `last_needle_thrust` | investida | 3 a 10 | 14/10/14 | 10 | repulsão 1,2; recarga 50 |
| | `phantom_puppet_echo` | cone 130° | 2 a 8 | 16/12/14 | 11 | recarga 60 |
| | `unraveled_storm` (**especial**, sem corte) | tempestade | 0 a 12 | 20/60/20 | 9 por golpe | ao acabar a deixa exausta no chão (ENROSCADA); recarga 140 |

Regras comuns (`AttackRunner`, uma só implementação para as 11 criaturas):

- **Nunca bate no ar.** O preparo continua mirando (a mira trava 6 ticks antes do golpe); ao comprometer, se o alvo saiu do alcance (+1,6) ou do nível, o ataque é cancelado, sem dano e com recarga curta. Os **especiais** disparam na linha travada de propósito.
- **Escolha ponderada** (5 para os comuns, 4 para o especial), sem repetir o último quando há outra opção, só com linha de visão e alvo a até ~3 blocos de diferença de altura; os ataques têm faixas de alcance e recargas próprias.
- **Mira à frente** com a velocidade do alvo medida no servidor.
- **Aliados nunca se ferem**: `AttackRunner.isFoe` só aceita jogadores (não espectadores) e golems de ferro; qualquer `TenThreadsEncounterMember` fica de fora, assim como outros monstros.
- Dano primeiro, depois a velocidade (o empurrão do golpe apagaria o lançamento).

## Pontos fracos e contrajogadas por marionete

| Marionete | Como derrotá-la | Regra no código |
|---|---|---|
| Aracnídea | Desviar sem pular do Salto do Tear deixa-a cravada e enroscada | `missTangles` |
| Cão | Pôr um escudo ou um tronco no caminho da investida racha a porcelana (armadura 10 → 0) | `crash()` |
| Sino | Flechas vindas de cima ricocheteiam; só o badalo embaixo sofre (×2); cortar o fio durante a Ressonância o enrosca | `damageMultiplier`: projétil com origem ≥ 1,4 acima = 0; abaixo = ×2; `Cut.WINDUP` |
| Cervo | Pôr-se diante de um tronco e desviar a galopada o enrosca | `crashed` |
| Gralha | Cortar o fio no mergulho ou acertá-la quando paira | `Cut.WINDUP` |
| Noiva | Depois de três golpes ela dá um passo de lado (reativo, sem enrosco); cortar o fio do Véu a enrosca | reativo + `Cut.WINDUP` |
| Centopeia | O fio só aparece quando emerge (recuperação): cortá-lo então a enrosca | `Cut.RECOVERY` |
| Mãe | Dar a volta por trás enquanto canaliza e cortar o fio traseiro | `Cut.WINDUP`, sem repulsão |
| Carrasco | Desviar da Guilhotina e golpear a costura do pescoço de cima (×2,5, também com o braço preso) | `damageMultiplier` |
| Gigante | Cortar o fio depois do sexto golpe: ajoelha, e o berço leva ×2,5 (pernas em pé: ×0,5) | `damageMultiplier` |

Enroscada, qualquer marionete leva ×1,75 (acumula com os multiplicadores acima, exceto o berço do Gigante, que já vale ×2,5).

## Arena: o Teatro de Fios

- **Âncoras** (`ArenaAnchorManager`): ao começar, procura num raio de 20, entre 4 e 14 blocos acima dela, até 8 troncos (um por oitante); um oitante sem árvore recebe um **Poste de Costura**, uma coluna de fio de 6 blocos (só partículas; nenhum bloco é colocado ou destruído).
- **Círculo de 28 blocos**: quem esteve dentro e sai é empurrado de volta e sofre 2 de dano mágico (no máximo uma vez por segundo). Quem nunca entrou é ignorado (testes lado a lado e jogadores a 60 blocos não são puxados).
- **Anti-pilar**: alvo mais de 4,5 acima da marionete e inalcançável por 60 ticks é **puxado para baixo** por um fio roxo (com Queda Lenta de 25 ticks, para a puxada não matar); recarga de 100 ticks. Esse caso tem preferência sobre o içar.
- **Anti-buraco**: marionete sem conseguir chegar ao alvo por 50 ticks é **içada** pelos fios até um ponto livre ao lado dele.
- **Reset**: nenhum jogador válido a menos de 48 blocos por 200 ticks (10 s) → a marionete ativa recupera toda a vida, perde ataques e armadilhas e os fios soltos somem; o estágio é mantido.
- **Só um boss por vez** a 64 blocos (o Fuso recusa uma segunda invocação).

## Invocação (Fuso Amaldiçoado)

- Receita sem forma: 4 fios (`string`), 2 ossos, 1 crânio de esqueleto do Wither, 1 lã vermelha, 1 fragmento de ametista.
- Clique direito num bloco sólido (face de cima firme) com 5 × 5 × 5 blocos livres acima do bloco clicado e **nenhuma outra Matriarca a 64 blocos**, em **floresta escura, jardim pálido, taiga antiga de abetos ou pinheiros**, ou em qualquer floresta densa (14 troncos num raio de 8, até 12 acima) à noite (12500 a 23500). Uma falha mostra a razão, toca um som e **não consome o Fuso**; o sucesso consome um e inicia o `INTRO`.
- Conquistas em cadeia (retrato como ícone): `craft_cursed_spindle` → `summon_ten_threads_witch` → `sever_crimson_thread` → `defeat_ten_threads_witch` → `obtain_ten_threads_staff`.

## Recompensas e o Cajado dos Dez Fios

- Cada marionete: 1 Retalho de Marionete Amaldiçoada (ingrediente de reparo do cajado, tag `puppet_remnants`) e 2 Orbes de Carretel Vital (cura 4 PV, 3 de fome, 15 de durabilidade reparada em cada mão).
- A Matriarca deixa **uma** Caixa de Costura (baú) com o **Cajado dos Dez Fios** (100%) e 1 a 2 **Dedais da Matriarca**; 700 XP.
- **Cajado** (1561 de durabilidade, dano 7 como arma, épico), três posturas trocadas com Shift + clique direito (guardadas no `CUSTOM_DATA` do item), cada uma com recarga e desgaste próprios (a recarga é colocada antes do desgaste):

| Postura | Efeito | Recarga | Desgaste |
|---|---|---|---|
| I, Fios de Caça | puxa o jogador até o bloco ou criatura visto a até 22 blocos, com Queda Lenta 3 s; sem âncora, não gasta nada | 60 | 1 |
| II, Fios de Controle | badalada em 8 blocos: 8 de dano a cada inimigo, enraíza 4 s (Lentidão VII) e o lança | 140 | 2 |
| III, Fios de Ruína | braços esmagam 4 × 4 blocos a 3 à frente: 22 de dano e lançamento | 200 | 4 |

## Visual e modelo

Silhuetas, materiais e telegrafias em que o jogador lê o ataque antes de ele acontecer: o fio carmesim é sempre a pista do especial.

| Criatura | Silhueta e materiais (referência do usuário, imagens de 2026-11-05) |
|---|---|
| Matriarca | velha encurvada de capa de lã com musgo e capuz, óculos/venda de renda, brincos de prata, botas; cajado de madeira escura com uma **roda de dez carretéis** (núcleo vermelho entre flanges de latão) que estouram um a um; na fúria, venda arrancada e olhos âmbar |
| Aracnídea | torso e cabeça de boneca de porcelana rachada (cabelo castanho, olhos ocos) sobre um tórax de madeira com latão e carretéis; oito pernas de osso com ponta de agulha de aço; agulhas nas costas e um novelo vermelho atrás |
| Cão | porcelana branca com flores azul-cobalto, uma fenda aberta no flanco (vermelho escuro com costelas), dentes dourados, juntas e rabo segmentados |
| Sino | sino de bronze oxidado (verdigris) em camadas com painéis ovais em relevo, alça no topo, interior escuro; esqueleto remendado pendurado por um fio como badalo |
| Cervo | crânio de cervo com órbitas âmbar, galhada de osso escuro ramificada, corpo de retalhos costurados (couro, lona, pano vermelho e oliva) com uma engrenagem de madeira no flanco, pernas de pau com cascos de ferro |
| Gralha | corvo enorme de penas azul-escuras, crânio de ave com bico longo de osso e olhos âmbar, ossatura de vime exposta no peito, asas de pano rasgado dobradas ao longo do corpo (abrem ao atacar), garras de ferro |
| Noiva | gola alta e vestido de renda creme rasgado e manchado, véu longo, rosto de porcelana liso com fendas escuras no lugar dos olhos, **buraco sangrento** no peito e oito braços finos em leque, quatro com agulhas |
| Centopeia | três máscaras de porcelana (olhos ocos) sobre um manequim de costura e seis manequins com saias babadas de tecidos diferentes, mãozinhas e pezinhos por baixo; um carretel vermelho brilhante na cauda |
| Mãe das Raízes | tronco com rosto de madeira entalhado de olhos fechados e musgo, braços de casca enormes cobertos de musgo, uma gaiola de vime no peito com duas bonecas (vermelha e azul), saia de raízes entrelaçadas |
| Carrasco | cabeça de saco de aniagem com um único buraco de olho e costura, torso de couro remendado com placa de ferro rebitada, cinto de correntes, um braço esquerdo normal e um colossal direito de tábuas, ferro e espinhos de osso, botas de placas |
| Gigante | esqueleto magro em trapos negros, cabeça de **gaiola de vime** com um núcleo âmbar brilhante visível pelas frestas, braço superior com mãos e quatro braços finos de agulha de aço |

Os modelos foram refeitos em 2026-11-05 a partir das imagens de referência do usuário (as onze criaturas identificadas uma a uma); nenhuma imagem foi copiada, só a leitura das silhuetas e materiais. Cada material tem um pintor próprio (porcelana com flores, verdigris com relevo, retalhos costurados, vime com frestas, penas, couro rebitado…) com escurecimento de bordas, e as faces das cabeças são pintadas à mão (olhos, bocas, rachaduras). Modelos (`tools/generate_ten_threads_witch_assets.py` gera as caixas, os atlas com UVs sem sobreposição, a camada emissiva `<id>_eyes.png` e `TenThreadsParts.java`) são árvores de partes nomeadas; `PuppetAnimations` descreve **uma** animação por papel da parte (corpo, cabeça, braço, perna, asa, segmento, badalo, roda…) e a amostra em quadros-chave sobre a pose de repouso, de modo que as 11 criaturas compartilham a descrição e ainda assim se movem de forma diferente. O fio de cada marionete até o cajado é desenhado por `PuppetThreadRenderLayer` (linhas no cliente, **sem biblioteca de animação nova**; ver D-034).

### Animações (13 por criatura)

| Animação (`PuppetAnim`) | Duração (s) | Uso |
|---|---|---|
| `SLEEP` | 3,0 | pendurada, balança no fio |
| `AWAKEN` | 0,8 | puxada ao chão |
| `IDLE` | 2,4 | repouso de combate |
| `WALK` | 0,9 | perseguição |
| `WINDUP` | 0,6 | preparo de ataque comum |
| `WINDUP_CRIMSON` | 0,5 | preparo do especial, fio carmesim |
| `ATTACK_A/B/C` | 0,5 | execução do 1º/2º/3º ataque |
| `RECOVER` | 0,8 | recuperação |
| `TANGLED` | 1,2 | enroscada (e a Matriarca colapsada) |
| `DEATH` | 1,2 | morte |
| `TAUNT` | 0,8 | a Matriarca puxa o fio / ri |

Fúria: a Matriarca reaproveita as animações com `fury` (venda arrancada, olhos acesos) e sem os carretéis.

## Barra de vida

`client/bossbar/TenThreadsWitchBossBar` (textura `textures/gui/boss_bar/ten_threads_witch.png`, 288 × 44, gerada por `tools/generate_boss_bar_ten_threads_witch.py`): a viga de um tear com um carretel carmesim à esquerda, uma agulha de osso à direita e um emblema de pano remendado ao centro; o slot de vida é transparente (o jogo desenha o fundo e o preenchimento atrás da moldura). O preenchimento mostra a vida da marionete atual, fio de prata mais avermelhado a cada estágio, e **rubi pulsante** no estágio 11. Dez marcas de carretel acima do slot, uma por marionete: acesas as já vencidas, branca a atual, apagadas as futuras. O nome vem do servidor: `bossbar.codenamecoraline.ten_threads_witch.stage` (`[Fio %s/10] %s`) e `.fury`. O estilo está registrado em `ThemedBossBars` sob as duas chaves e sob o nome da entidade.

## Sons (eventos vanilla em `sounds.json`; nenhum `.ogg` novo)

| Evento (`entity.…`) | Eventos vanilla (volume / tom) |
|---|---|
| `ten_threads_witch.ambient` | `entity.witch.ambient` (0,9 / 0,72) + `block.amethyst_block.chime` (0,4 / 0,6) |
| `.hurt` | `entity.witch.hurt` (1,0 / 0,75) + `block.wool.hit` |
| `.death` | `entity.witch.death` (1,0 / 0,65) + `entity.item.break` (1,0 / 0,5) |
| `.thread_tension` | `item.crossbow.loading_middle` (1,2 / 1,5) + `block.note_block.bass` (1,0 / 1,8) |
| `.thread_snap` | `item.lead.break` (1,5 / 1,3) + `block.amethyst_cluster.break` |
| `.voice` | `entity.witch.ambient` (1,0 / 0,6) + `entity.illusioner.ambient` (0,8 / 0,7) |
| `ten_threads_puppet.hurt / death / awaken / tangled / crash / crack` | lã, quebra de item, ametista e corda, madeira, vaso decorado |
| `sewing_arachnid.action` | `entity.spider.step` + `entity.sheep.shear` |
| `porcelain_hound.action` | `block.decorated_pot.shatter` + `entity.baby_wolf.growl` + `block.glass.step` |
| `hollow_bell.action` | `block.bell.use` (1,6 / 0,55) + `block.bell.resonate` |
| `patchwork_stag.action` | `entity.camel.dash` + `entity.skeleton.step` + `block.bamboo_wood.step` |
| `stitched_rook.action` | `entity.parrot.imitate.phantom` + `entity.ender_dragon.flap` + `entity.sheep.shear` |
| `hollow_bride.action` | `entity.allay.ambient_without_item` + `entity.player.attack.sweep` |
| `skirted_centipede.action` | `entity.silverfish.step` + `block.rooted_dirt.break` |
| `root_mother.action` | `entity.warden.dig` + `block.cherry_wood.step` |
| `patchwork_executioner.action` | `entity.iron_golem.attack` + `block.anvil.land` (1,4 / 0,55) |
| `cradle_giant.action` | `block.wooden_trapdoor.open` + `entity.wither.shoot` |

Desvio da ficha: os eventos `entity.leash_knot.break` e `entity.wolf.growl` não existem nesta versão; usam-se `item.lead.break` e `entity.baby_wolf.growl` (o teste de conteúdo confere que todo evento citado existe no registro vanilla). Cada definição tem legenda em pt_br e en_us.

## Arte e recursos

- `python tools/generate_ten_threads_witch_assets.py`: modelos, atlas (com prévia), camadas emissivas, `TenThreadsParts.java`, ícones (fuso, retalho, dedal, cajado, retrato, ovo) e o modelo 3D do cajado (`ten_threads_staff_3d`), usado na mão (a definição do item escolhe o 3D fora de inventário, chão e vitrine).
- `python tools/generate_boss_bar_ten_threads_witch.py`: a moldura da barra (prévia em `build/ten-threads-bossbar/review.png`).
- `ExportTenThreadsWitchModel` (`tools/ten-threads-witch-sketches.gradle`, tarefa `exportTenThreadsSketchModel`; nesta sessão compilado e executado direto com `javac`/`java`) seguido de `python tools/generate_ten_threads_witch_sketches.py`: três gravuras do Bestiário a partir do modelo real (anatomia 112 × 80 com a roda de dez carretéis, combate 112 × 72 com o Gigante armando o fio carmesim, espólios 112 × 64).

## Conteúdo

- **Bestiário** (`Bestiary.TEN_THREADS_WITCH`, 8 páginas em pt_br e en_us): A Costureira (história), O Casulo de Fios (gravura 112 × 80), O Fio Carmesim (112 × 72), Marionetes I a III, IV a VI, VII a IX, X e a Verdadeira Face, Espólios e o Cajado (112 × 64).
- **Conquistas** (em cadeia, ícone = retrato): *Fio da Meada*, *Teatro na Floresta Escura*, *Corte na Costura* (critério `trigger`, concedida por `PuppetEntity.awardAdvancement` ao cortar um fio), *Sem Nenhum Fio Restante*, *O Novo Marionetista*.
- Saque `entities/ten_threads_witch.json` (cajado 100%, dedal 1 a 2), receita `cursed_spindle.json`, tag de itens `puppet_remnants`, tags de congelamento e neve em pó.

## Testes (`TenThreadsWitchGameTests`, `coraline.tenThreadsTests=true`, 30 testes)

Tarefa Gradle `runTenThreadsWitchTest` (`build/ten-threads-gametest/`). Cada teste monta o próprio lote alto (piso de pedra) e usa **golems de ferro** como alvo (um jogador de servidor seria perseguido por toda marionete a 48 blocos). Os de ataques, o Fuso, a borda da arena e o reset rodam em lotes distantes para que círculos, leques e investidas não alcancem os alvos de outros testes.

| Teste | O que prova |
|---|---|
| `tt_registration_attributes_and_content` | atributos, caixas, olhos, XP, voo e especial de cada marionete; Matriarca; tags de imunidade; itens, ovo, cajado (1561, épico, dano 7), reparo por retalhos; sons; Bestiário (8 páginas, 3 gravuras), 5 conquistas, receita, saque; texturas e olhos das 11 criaturas; todas as chaves de texto em pt_br e en_us; 22 definições de som só com eventos vanilla existentes e legendadas |
| `tt_models_uv_and_animations` | as 11 criaturas assam, UVs sem sobreposição, 13 animações com a duração certa e vértices finitos, ataques e morte diferem do repouso, 10 carretéis que somem conforme estouram |
| `tt_matriarch_invulnerable_until_stage_11` | intocável nos estágios 1 a 10 (inclusive as marionetes penduradas), vulnerável e com os dez carretéis estourados no 11 |
| `tt_sequence_stages_1_to_3` | uma marionete por vez, na ordem, carretel que estoura na hora, dois orbes e um retalho por morte, barra cheia |
| `tt_sequence_9_to_fury_death_and_casket` | 9 → 10 → colapso → fúria (150 PV, 0,40) → morte contada uma vez → uma única caixa com o cajado e 1 a 2 dedais, nenhuma marionete dela sobrevive |
| `tt_thread_cut_cancels_and_tangles` | hitbox único e ligado à marionete, só jogadores o cortam, 15% de dano, ENROSCADA por ~3,5 s, ×1,75 comparado a uma marionete livre, ataque cancelado, fio não se corta duas vezes |
| `tt_missed_special_tangles` | especial que erra (alvo se desloca depois da mira travada) enrosca |
| `tt_hound_cracks_against_a_wall` | rachar leva a armadura de 10 a 0 (uma só vez); investida real contra parede racha, enrosca e poupa o golem atrás |
| `tt_weak_points_and_shells` | sino (flecha de cima 0, badalo ×2), carrasco (nuca ×2,5), enroscada ×1,75, gigante (pernas ×0,5, ajoelhado ×2,5) |
| `tt_never_swings_at_air_and_chases` | alvo sai do alcance: 0 golpes, 1 cancelado, sem dano; persegue um alvo distante |
| `tt_alliances` | só jogadores e golems são alvos; nenhum dano entre marionetes e Matriarca em nenhum sentido |
| `tt_attacks_<id>` (10 testes) | cada ataque da marionete fere o golem em alcance e **nunca** a marionete vizinha |
| `tt_staff_postures_cooldown_and_wear` | ciclo de posturas guardado no item, Shift não gasta, controle (8 de dano, enraíza, poupa quem está fora), ruína (22), caça (puxa, sem âncora não gasta), recarga e desgaste |
| `tt_orb_rewards` | cura 4, fome 3, repara 15 sem passar de zero |
| `tt_dialogue_hook` | gancho de voz, fala do estágio, queixa do fio retida por 60 ticks e depois dita |
| `tt_anti_pillar_pulls_the_target_down` | alvo sobre pilar de 8 blocos é puxado com Queda Lenta |
| `tt_anti_hole_lifts_the_puppet` | marionete selada num poço é içada para junto do alvo |
| `tt_spindle_conditions` | sem bloco sólido, bioma errado (dia e noite), floresta densa à noite aceita, 5 × 5 × 5 apertado, falha não consome, sucesso consome um e cria uma Matriarca em `INTRO` com dez marionetes, segunda invocação recusada |
| `tt_arena_edge_and_advancement` | empurrão e dano na borda (só para quem entrou), jogador distante intacto, conquista do corte concedida a um jogador de servidor |
| `tt_reset_when_everyone_leaves` | sem jogador por 10 s a marionete ativa recupera a vida e o estágio é mantido |

## Limites conhecidos

- **Fidelidade à referência:** os modelos são caixas com textura de 1 pixel por unidade (como os outros bosses), então a leitura é de silhueta e material, não do detalhe das imagens (a flor da porcelana, o relevo do sino, as bonecas na gaiola ficam em poucos pixels). Pontos onde o resultado ainda se afasta da referência: o sino é escalonado (camadas) e não curvo; os manequins da Centopeia são baixos e roliços; a Noiva tem saia em degraus; as pernas da Aracnídea são retas em vez de articuladas. Conferido só em renders offline; a aparência no jogo (iluminação, escala, animação em movimento) está **não verificada no jogo**.

- Valores de dano, recarga e alcance são os da ficha e precisam de calibração jogando.
- O jogador de teste do servidor é invulnerável até o cliente carregar; por isso o dano da borda é verificado pelo contador `getArenaHits()`, não pela vida.
- Cinemática de entrada própria (câmera) não foi feita: o `INTRO` é a Matriarca falando e os dez corpos pendendo, sem diretor de câmera.
- Voz sintetizada: apenas o gancho (`WitchDialogueManager.setVoiceHook`); nenhuma voz é distribuída.

## Animação da Matriarca

O sistema de animação da Matriarca (camadas, prioridades, catálogo completo, entrada, transições, morte) está em
[matriarch_animation.md](matriarch_animation.md). Mudou também o jogo da última fase: ataques, combos, defesas, esquivas e
derrubada (ver D-037), e as durações da entrada (154 t), do colapso (134 t) e da morte (144 t).
