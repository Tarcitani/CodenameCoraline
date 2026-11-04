# Comandante Carmesim (`crimson_commander`)

Boss opcional (semi-boss) das câmaras profundas. Um general juramentado a um trono esquecido, que continua guardando o salão subterrâneo depois da ruína do reino. Duelista disciplinado: pune ataques apressados com guarda e riposta, persegue quem foge com investidas e esmaga o chão com saltos pesados. Estética de cavaleiro carmesim, original (nada de nomes, logos ou textos protegidos).

Estado: implementado e coberto por GameTests (ver "Testes"). **Visual, animações em movimento, sons e sensação de combate não foram verificados no jogo** (`runClient` pendente).

## Ficha

| Item | Valor |
|---|---|
| Nomes | Comandante Carmesim / Crimson Commander |
| Caixa | 1,1 × 3,5 blocos, olhos a 3,05 (modelo desenhado em tamanho real, sem escala no renderizador) |
| Atributos | 220 PV, armadura 14, resistência de armadura 6, velocidade 0,34 (0,38 no estágio 2, 0,42 no 3: um cavaleiro ágil), dano base 12, resistência a repulsão 0,75, visão 40, 250 XP |
| Imunidades | fogo e lava (`fireImmune`), congelamento (`canFreeze` falso e tag `freeze_immune_entity_types`), anda sobre neve em pó (`powder_snow_walkable_mobs`), afogamento (`canBreatheUnderwater`), 80% menos dano de queda, não queima ao sol, projéteis frontais devolvidos durante a guarda |
| Alvos | jogadores e golens de ferro; ignora animais passivos; nunca ataca outro Comandante nem as próprias Sentinelas |
| Spawn natural | raro: peso 2, grupo de 1, na tag `codenamecoraline:crimson_commander_habitat` (`#minecraft:is_mountain`, `deep_dark`, `dripstone_caves`); só em Y ≤ 0, luz ≤ 7, chão firme, 4 blocos livres, sem outro Comandante a 48 blocos e 1 chance em 4 a cada tentativa natural |
| Invocação | Insígnia do Juramento Manchada em tijolos de ardósia ou ardósia polida, com 5×5×5 blocos livres acima e nenhum Comandante a 48 blocos. Falha não consome e explica no overlay (`wrong_block`, `no_space`, `already_active`) |

## Ataques

Tempos em ticks (20 por segundo). Todo preparo ganha de 0 a 3 ticks extras sorteados (o último quadro segura a telegrafia), exceto o da Guarda (reação imediata) e o do Golpe Pesado Aéreo, cujo salto tem ritmo fixo. Recargas valem para o estágio 1; a partir do estágio 2 caem 20%.

| Ataque | Gatilho | Preparo | Execução | Recuperação | Dano e efeitos | Telegrafia | Contra-jogada | Recarga |
|---|---|---|---|---|---|---|---|---|
| Corte Cruzado Carmesim (`CRIMSON_SLASH`) | alvo a ≤ 3,8 com linha de visão, mesmo nível | 12 (tronco gira, espada puxada para trás) | 6 (varredura de 140°, dano no tick 3) | 14 | 12 + empurrão 0,6 | brilho rubi no visor; som de corte grave | sair do arco frontal e punir os 14 ticks | 30 |
| Investida Perfurante (`DASH_THRUST`) | alvo a 4 a 11, ou recuando a 3 a 11, com caminho e chão contínuo até ele | 16 (inclina, aponta a espada, rastro no chão) | até 9 blocos até a posição prevista do alvo (para no contato, numa parede ou ao fim da distância) | 18 se acertou, 24 se errou (animação de frenagem própria) | 15 + Lentidão I por 40 ticks | trilha carmesim no chão marcando o vetor; a mira congela 3 ticks antes do avanço e a linha desenhada é a que ele corre | esquivar de lado no fim do preparo e atacar pelas costas na frenagem | 80 |
| Golpe Pesado Aéreo (`HEAVY_SLAM`) | estágio 2+: alvo a 2,5 a 7,5; qualquer estágio: alvo 1 a 4 blocos acima (pilar) até 8,5; estágio 2+: alvo colado (menos de 2,5) até 10 ticks depois de uma recuperação | 22 (duas mãos sobre a cabeça; salto de 2 blocos no tick 14) | 10 (desce cravando a lâmina; impacto ao tocar o chão, no máximo no tick 6) | 26 (puxa a espada do chão) | 20 a até 2 blocos do impacto, 12 de 2 a 4 (uma parede protege do anel externo); joga para o alto (+0,5) e desativa escudos na mão por 100 ticks | anel dourado (raio 4) e carmesim (raio 2) no chão em volta do ponto onde a lâmina vai cair; bigorna e explosão | sair do anel ou saltar a borda; punir os 26 ticks | 120 |
| Guarda Defensiva (`DEFENSIVE_GUARD`) | 3 golpes recebidos em 40 ticks (golpes com atacante ou origem; veneno, sufocamento e afins não contam), ou todos os ataques em recarga e alvo a ≤ 6 (espera em média 0,4 s) | 6 (firma as pernas, cruza a espada) | 30 de guarda ativa, girando no máximo 12° por tick | 10 | durante a guarda ativa: 75% menos dano frontal (arco de 140°); projéteis frontais devolvidos; golpe corpo a corpo frontal vira Riposta | faíscas douradas; guarda travada (escudo) | não atacar de frente; contornar (ele gira devagar) ou curar | 160 |
| Riposta (`RIPOSTE`) | golpe corpo a corpo frontal (atacante a ≤ 4,5) durante a guarda ativa | 0 | 6 (dano no tick 2) | 8 | 14 num cone de 120° e 3,2 blocos, empurrão 1,4 e elevação 0,45 | corte imediato | não bater de frente na guarda | (gasta a recarga da guarda) |
| Onda de Lâmina Sanguínea (`BLOOD_WAVE`) | estágio 2+: alvo a 8 a 20; qualquer estágio: alvo inalcançável (sem caminho) a 3 a 20, alto demais (mais de 4 acima) ou fugindo a 8 a 20 | 18 (lâmina carregada de rubi e ouro) | 8 (crescente liberado no tick 3) | 16 | 13 por alvo, uma vez; viaja 22 blocos a 1,5 por tick e para em blocos sólidos com explosão | lâmina brilhante; feitiço cortante | esquiva lateral ou cobertura | 140 |

Regras comuns:

- **Nunca bate no ar.** Durante o preparo ele segue mirando e (no Corte) avançando até 3 ticks antes do golpe; ao comprometer, se o alvo saiu do alcance ou do nível, ele cancela (`ATTACK_CANCEL`) com recarga curta (20). O salto do Golpe Pesado também é cancelado se o alvo já fugiu no tick do salto. A Investida nunca é escolhida nem lançada sobre um buraco, lava ou parede: a linha é conferida na escolha e de novo no comprometimento (`groundAlong`).
- **Escolha ponderada** sem repetir o ataque anterior quando há outra opção. Pesos: Corte 6, Investida 5 (9 contra quem recua), Golpe Pesado 12 contra pilar, 8 contra quem gruda após a recuperação, 4 normal; Onda 10 contra inalcançável, alto ou fugitivo, 4 normal.
- **Mira à frente** com a velocidade do alvo medida no servidor (média das últimas posições), nunca a do cliente.
- **Lançamentos:** dano primeiro, depois a velocidade (o empurrão do próprio golpe apagaria o lançamento).

## Estágios

| Estágio | Vida | Ao entrar (uma única vez) | Efeitos |
|---|---|---|---|
| 1 — Vigília de Ferro | 100% a 60% | — | Corte, Investida e Guarda; Onda e Golpe Pesado só contra pilar, fuga ou alvo inalcançável. Barra carmesim profundo (`RED`) |
| 2 — Juramento Desperto | abaixo de 60% | 30 ticks de `AWAKEN_TRANSITION` (ergue a espada, pulso carmesim), invulnerável; no tick 18 chama até 2 Sentinelas | velocidade 0,32, recargas 20% menores, Golpe Pesado e Onda na rotação. Barra rubi incandescente (`PINK`) |
| 3 — Fúria do Último Comandante | abaixo de 25% | 40 ticks de `FURY_TRANSITION` (crava a espada, o visor se acende), invulnerável | velocidade 0,35, todo dano +25%, partículas constantes no visor e na lâmina, combo: Investida que acerta emenda direto no Golpe Pesado, sem recuperação. Barra ouro-sangue (`YELLOW`), pulsante |

Um golpe que leve direto do estágio 1 ao 3 aplica o 3 e também chama as Sentinelas (o juramento desperta uma vez só). Curar nunca repete nem desfaz uma transição.

## Visual (referência: prancha "Crimson Knight")

Elmo todo carmesim com viseira escura em T, crista alta e barbatanas laterais; um penacho longo sobe da crista e se curva para trás. Capa esfarrapada carmesim. A espada larga tem lâmina negra e larga com fio dourado fino, guarda e pomo de aço escuro com nós e empunhadura vermelha enrolada, sem runas. Ouro só nos detalhes de ombreiras, cintos e insígnia.

## Entrada, agilidade e autocura

- **Intro (100 ticks, cinemática no cliente como os outros bosses):** ajoelhado sobre a espada cravada (câmera baixa), levanta-se aos 40 ticks arrancando a lâmina (tremor), saúda aos 62 e o título "O COMANDANTE CARMESIM / O General Sem Rei" aparece aos 66, com tarjas, FOV fechado e a tecla de pular para pular (a mesma do Cobrador).
- **Agilidade:** velocidade maior, corrida ×1,45 acima de 9 blocos, circulando a ×1,3. Corte em salto (avança e pula no golpe) seguido de salto mortal para trás na recuperação; Investida com um pulo na largada; Golpe Pesado com salto de 2,6 blocos e alcance até 9.
- **Knockback maior:** Corte 1,4 (+0,35 de subida), Investida 1,7 (+0,4), Golpe Pesado 1,2 (+0,75), Riposta 2,3 (+0,6) e a Onda empurra 1,0 (+0,35).
- **Autocura (estágio 2 em diante, abaixo de 70% de vida, recarga de 30 s):** ele recua até 10 blocos correndo, chama as Sentinelas que faltam e, ajoelhado sobre a espada, recupera 15% da vida em 3 s com uma aura vermelha em anéis subindo. Perder 8% da vida enquanto canaliza interrompe a cura (recarga pela metade). As Sentinelas lutam por ele nesse tempo.
- **Investida com mais efeitos:** onda sônica e poeira na largada, chamas de alma, faíscas e poeira do chão ao longo do avanço e explosão com brilho e partículas no acerto.

## Movimento e IA

- **Alvo:** quem o feriu primeiro enquanto válido e a até 40 blocos; senão o jogador ou golem mais próximo. Histerese: só troca de alvo se o novo estiver pelo menos 4 blocos mais perto. Reavaliação uma vez por segundo.
- **Perseguição:** caminho até a posição prevista do alvo (`createPath` + `canReach`), recalculado a cada 4 ticks; corre (×1,25) acima de 9 blocos. Com algum ataque pronto, aproxima até 2,4 blocos. Com tudo em recarga, circula a 4,5 blocos do alvo, trocando de lado a cada 2,5 s, só por pontos de chão seguro (sem lava, fogo, magma nem queda maior que 3).
- **Relevo:** sobe degraus de 1 bloco (step 1,1). Contra alvo em pilar ou saliência usa o salto do Golpe Pesado (pousa em cima). Sem caminho ou sem progresso por 2 s (e alvo a até 14 blocos), salta até perto do alvo (`STEP_JUMP`, recarga de 5 s). Preso em blocos por 10 ticks, abre caminho até o ponto livre mais próximo a até 3 blocos (sem teletransporte longo). Contra alvo do outro lado de um buraco, usa a Onda.
- **Saltos:** arco calculado (parábola com 2 blocos acima do ponto mais alto) seguido tick a tick por velocidade, com colisão real.

## Sentinelas de Cinza Carmesim (`crimson_ash_vanguard`)

1,95 × 0,7 blocos, 28 PV, 6 de dano, armadura 4, velocidade 0,3, imunes a fogo e congelamento. Mesmo modelo do Comandante em escala 0,557, armadura de cinza com fendas carmesim emissivas, sem penacho, capa e espinhos. Ligadas ao Comandante por UUID: atacam o alvo dele (senão o jogador ou golem mais próximo), nunca o mestre nem outra Sentinela; os golpes do mestre e de outras Sentinelas não as ferem. No máximo 2 vivas por boss. Duram 600 ticks; desfazem-se em cinza quando o mestre morre ou some. Sem saque e sem XP. O corte delas usa o tempo e as animações do Corte do mestre (preparo 12, dano no tick 3, recuperação 14, recarga 16) e também nunca bate no ar. Uma criada pelo ovo não tem mestre nem limite de tempo.

## Morte e recompensas

`die` concede as recompensas uma vez (`deathAwardCount`): 250 XP (quando morto por jogador), 3 a 6 Fragmentos de Placa Carmesim (100%) e 25% de uma Insígnia do Juramento Manchada (loot table). Cinemática de 90 ticks: `DEATH_KNEEL` (cai sobre um joelho e crava a espada; no tick 20 a lâmina morde o chão), `DEATH_FINAL` no tick 30 com o espírito translúcido e azul subindo (`crimson_commander_spirit`, padrão dos outros bosses), e no tick 50 a **Espada Larga do Juramento Carmesim** é deixada onde ele se ajoelhou, uma única vez (`deliverGreatsword` ignora chamadas repetidas). As Sentinelas se desfazem na hora.

## Espada Larga do Juramento Carmesim (`crimson_oath_greatsword`)

Espada de material próprio (`CRIMSON_OATH`: 2100 de durabilidade, nível netherite): 10 de dano no total, velocidade -3,0, épica, resistente a fogo. Clique direito: **Investida Carmesim**, avanço de 6 blocos em 6 ticks fixos na direção horizontal do olhar, 8 de dano uma vez em cada criatura hostil da linha (nunca em jogadores, pets, montarias, aldeões nem nas invocações do jogador), rastro vermelho, 8 de durabilidade, recarga de 120 ticks; para numa parede e não funciona montado. Receita sem forma: 4 Fragmentos de Placa, 1 Insígnia e 1 espada de netherite. Modelo 3D na mão (`models/item/crimson_oath_greatsword_3d.json`, caixas giradas 45° como sprite de ferramenta), ícone 2D em inventário, chão e moldura.

## Modelo, poses de referência e animações

Modelo em tamanho real (3,5 blocos, chão em y = 24 do modelo, frente em -z), 53 caixas e 23 partes em `CrimsonCommanderParts` (gerado), atlas de 256×256 com 99 linhas usadas e nenhuma sobreposição de UV (testado). Hierarquia: `pelvis` → `chest` → `head` (`horn_l`, `horn_r`, `plume` → `plume_tail`), `cape`, braços `*_arm` (com três espinhos rotacionados cada) → `*_forearm` → `*_hand` → `sword` (só na direita); pernas `*_thigh` → `*_shin` → `*_foot`.

A lâmina aponta para -z a partir do punho. Com só rotações em x, o ângulo da lâmina S é a soma das rotações em x de peito, braço, antebraço e mão: S = 0 aponta para a frente, -90 para cima, +90 para baixo, -180 para trás. Mão com x = +90 põe a lâmina na linha do antebraço. A ordem do `ModelPart` é Z·Y·X (x primeiro), então um y na mão gira a lâmina em torno do antebraço. Poses de referência (absolutas, em graus; conferidas em renders offline e pelo teste `crimson_reference_poses_hold_the_sword_right`):

| Pose | Ângulos principais | Resultado |
|---|---|---|
| Combat Idle Stance (ombro) | peito 6, braço -25 (z 14), antebraço -95, mão -31 (S = -145) | lâmina apoiada na ombreira direita, subindo para trás |
| Dash Thrust / Lunge | peito 28 (y -10), braço -80, antebraço -8, mão 72 (S = 12) | lâmina nivelada com o alvo, bem à frente; pernas em afundo |
| Heavy Overhead Slam (armar) | peito -12 a -16, braços -165 com z ±26 (mãos juntas), mão -13 (S ≈ -205) | duas mãos sobre a cabeça, lâmina pendendo atrás |
| Impacto | peito 34, braços -70 com y ∓28, mão 112 (S = 70) | ponta cravada no chão à frente |
| Defensive Guard | peito 12, braço -50 (y -20), antebraço -52 (S = -90), mão y -40 | lâmina cruzando o tronco, ponta para cima e para a esquerda |
| Corte horizontal | braço x -90 girado pelo y de +55 (armar) a -20 (fim), peito y +40 a -40, mão 85 | braço e lâmina formam uma alavanca que varre da direita para a esquerda |

Renders de validação (gerados de `CrimsonCommanderModel` real): `build/crimson-commander-assets/sheet_attacks_side.png`, `sheet_attacks_34.png` e `sheet_life.png` (ferramenta de poses do scratchpad, mesma usada nos outros bosses), atlas com nomes em `build/crimson-commander-assets/atlas_preview.png`.

<!-- ANIMATION-TABLE:START -->

36 animações (`CrimsonAnim`), todas com keyframes em `CrimsonCommanderAnimations`.


**A. stance and movement**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `combat_idle` | 3.00 s | sim | GROUND, standing: the greatsword rests on the right shoulder | none | any action | walk, attacks |
| `idle_look` | 3.00 s | não | GROUND, no target: scans the hall | none | any action | combat_idle |
| `walk` | 1.20 s | sim | GROUND, marching with the sword on the shoulder | step sounds | any action | run, combat_idle |
| `run` | 0.70 s | sim | GROUND, chasing a far target, sword trailing low | step sounds | any action | walk, attacks |
| `step_jump` | 0.80 s | não | GROUND, jumping onto a ledge or out of a pit | none | landing | walk |

**B. crimson_slash**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `slash_prepare` | 0.60 s | não | ATTACK prepare: Crimson Slash (torso twists, the sword is drawn back) | ruby glow at the visor | target escapes (cancel) | slash_execute |
| `slash_execute` | 0.30 s | não | ATTACK execute: Crimson Slash (140 degree horizontal sweep) | DAMAGE at the sweep tick, crimson arc | none | slash_recovery |
| `slash_recovery` | 0.70 s | não | ATTACK recover: Crimson Slash (open) | none | none | combat_idle |

**C. dash_thrust**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `thrust_prepare` | 0.80 s | não | ATTACK prepare: Dash Thrust (leans in, points the sword) | crimson trail on the ground along the vector | target escapes (cancel) | thrust_execute |
| `thrust_execute` | 0.40 s | não | ATTACK execute: Dash Thrust (fast advance up to 9 blocks) | DAMAGE on contact, Slowness I | wall stops the advance | thrust_recovery, thrust_recovery_miss, slam_prepare (fury combo) |
| `thrust_recovery` | 0.90 s | não | ATTACK recover: Dash Thrust after a hit | none | none | combat_idle |
| `thrust_recovery_miss` | 1.20 s | não | ATTACK recover: Dash Thrust that missed (braking, long) | none | none | combat_idle |

**D. heavy_overhead_slam**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `slam_prepare` | 1.10 s | não | ATTACK prepare: Heavy Overhead Slam (two hands over the head, jump of 2 blocks) | gold and crimson ring on the ground | target escapes before the jump (cancel) | slam_execute |
| `slam_execute` | 0.50 s | não | ATTACK execute: Heavy Overhead Slam (the blade is driven into the ground) | DAMAGE in two rings, shockwave, shields disabled | none | slam_recovery |
| `slam_recovery` | 1.30 s | não | ATTACK recover: Heavy Overhead Slam (pulls the sword out of the ground) | none | none | combat_idle |

**E. defensive_guard and riposte**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `guard_prepare` | 0.30 s | não | GUARD prepare: plants the legs, crosses the sword in front of the torso | locked-guard sound | none | guard_hold |
| `guard_hold` | 1.00 s | sim | GUARD active (75% less frontal damage, projectiles deflected) | gold sparks on every block | a frontal melee hit (riposte) | guard_recovery, riposte |
| `guard_block` | 0.25 s | não | GUARD, a hit is blocked (overlay) | gold sparks, shield sound | none | guard_hold |
| `guard_recovery` | 0.50 s | não | GUARD recover | none | none | combat_idle |
| `riposte` | 0.50 s | não | RIPOSTE: the guard breaks into an instant counter-cut | DAMAGE in a 3.2 block cone, strong knock-back | none | combat_idle |

**F. blood_crescent_wave**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `wave_prepare` | 0.90 s | não | ATTACK prepare: Blood Crescent Wave (the blade is charged with ruby and gold) | spell sound, glowing blade | target escapes (cancel) | wave_execute |
| `wave_execute` | 0.40 s | não | ATTACK execute: Blood Crescent Wave (rising cut that releases the wave) | the wave travels 22 blocks | none | wave_recovery |
| `wave_recovery` | 0.80 s | não | ATTACK recover: Blood Crescent Wave | none | none | combat_idle |

**G. reactions**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `hurt_front` | 0.40 s | não | damage from the front (overlay) | hurt sound | none | previous animation |
| `hurt_back` | 0.40 s | não | damage from behind (overlay) | hurt sound | none | previous animation |
| `attack_cancel` | 0.50 s | não | an attack was cancelled because the target escaped | none | none | combat_idle |

**H. stages**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `awaken_transition` | 1.50 s | não | TRANSITION to stage 2 (60% health): raises the sword, crimson pulse | invulnerable, calls the vanguards | death | combat_idle |
| `fury_transition` | 2.00 s | não | TRANSITION to stage 3 (25% health): drives the sword down, the visor flares | invulnerable | death | fury_idle |
| `heal_channel` | 1.00 s | sim | HEALING: crouched over the planted sword, a red aura rising around him | he recovers 15% of his health over 3 s | damage of 8% of his health breaks it | combat_idle |
| `fury_idle` | 1.60 s | sim | stage 3 standing, the sword held low and ready | constant particles at the visor and blade | any action | attacks |

**I. arrival and death**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `intro_kneel` | 2.00 s | sim | INTRO: kneeling, both hands on the planted sword | crimson motes | none | intro_rise |
| `intro_rise` | 2.00 s | não | INTRO: rises, frees the sword and salutes with it | steel ring | none | combat_idle |
| `death_kneel` | 1.50 s | não | DYING: falls to one knee and plants the greatsword | heavy armour fall | none | death_final |
| `death_final` | 2.00 s | sim | DYING / DEAD: head bowed over the planted sword | his spirit rises | none | removed |

**J. the vanguards**

| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |
|---|---|---|---|---|---|---|
| `vanguard_rise` | 1.00 s | não | a Crimson Ash Vanguard rises from the ash | ash burst | none | combat_idle |
| `vanguard_crumble` | 1.00 s | não | a vanguard falls apart into ash (death, expiry, master fallen) | ash burst | none | removed |

<!-- ANIMATION-TABLE:END -->

## Arte e recursos

- `python tools/generate_crimson_commander_assets.py`: caixas do modelo, atlas do Comandante e das Sentinelas (mesmo layout, paletas diferentes), camadas emissivas (T do visor, runas da lâmina, insígnia; fendas das Sentinelas), `CrimsonCommanderParts.java`, ícones 16×16 com contorno automático (espada, fragmento, insígnia, retrato, dois ovos) e o modelo 3D da espada. Paleta da ficha: carmesim `#8B111E`, realce `#B81D2A`, bordô `#4A070E`, malha `#3A3D45`, obsidiana `#18161C`, ouro `#D4AF37`/`#F3CF55`, rubi `#FF2A3C`.
- `python tools/generate_boss_bar_crimson_commander.py`: moldura 288×44 da barra (a espada deitada; o sulco da lâmina é o slot de vida transparente, cabo com rubi à esquerda, ponta à direita, elmo com visor em T e penacho sobre o meio, flâmulas nas pontas, entalhes dourados em 60% e 25%); prévia em `build/crimson-commander-bossbar/review.png`. `CrimsonCommanderBossBar` divide o nome dos dois lados do elmo.
- `.\gradlew.bat -I tools/crimson-commander-sketches.gradle exportCrimsonCommanderSketchModel` e `python tools/generate_crimson_commander_sketches.py`: gravuras do Bestiário a partir do modelo real (anatomia 112×80 na Combat Idle Stance, combate 112×72 na Investida, espólios 112×64), prévia em `build/crimson-commander-sketches/review.png`.
- `python tools/generate_crimson_animation_table.py`: reescreve a tabela de animações acima a partir do código.

## Sons (eventos vanilla em `sounds.json`)

| Evento | Som vanilla | Volume / tom |
|---|---|---|
| `ambient` | `item.armor.equip_netherite` | 0,6 / 0,65 |
| `step` | `entity.iron_golem.step` | 0,8 / 0,75 |
| `hurt` | `entity.iron_golem.hurt` | 0,9 / 0,7 |
| `death` | `entity.wither_skeleton.death` + `block.beacon.deactivate` | 1,2 / 0,6 |
| `crimson_slash` | `entity.player.attack.sweep` | 1,1 / 0,7 |
| `dash_thrust` | `entity.breeze.wind_burst` | 1,2 / 0,8 |
| `heavy_overhead_slam` | `block.anvil.land` + `entity.generic.explode` | 1,2 / 0,65 |
| `defensive_guard` | `item.shield.block` | 1,1 / 1,2 |
| `blood_crescent_wave` | `entity.evoker.cast_spell` | 1,1 / 0,55 |
| `awaken` (transições) | `block.beacon.activate` + `entity.evoker.prepare_summon` | 1,2 / 0,6 |
| Sentinela `ambient`, `hurt`, `death`, `step` | esqueleto (graves) e `block.soul_sand.break` na morte | — |

## Conteúdo

- Bestiário (`Bestiary.CRIMSON_COMMANDER`, 6 páginas em pt_br e en_us): O General Sem Rei, Anatomia da Armadura Carmesim (gravura 112×80), Mestre da Espada Larga (112×72), Disciplina de Duelo, O Juramento Desperto, Espólios do Salão Manchado (112×64).
- Conquistas em cadeia, todas com `crimson_commander_portrait`: `bosses/oath_discovered` (Eco de um Juramento), `bosses/commander_summoned` (Audiência com o General, só dispara num uso bem-sucedido), `bosses/commander_defeated` (Repouso da Lâmina Vermelha, desafio), `bosses/crimson_greatsword_obtained` (Herdeiro do Juramento).
- Loot table `entities/crimson_commander.json`, receita `crimson_oath_greatsword.json`, biome modifier `crimson_commander_spawns.json`, tags de congelamento e neve em pó.

## Testes (`CrimsonCommanderGameTests`, `coraline.crimsonTests=true`, 32 testes)

| Teste | O que prova |
|---|---|
| `crimson_registration_and_immunities` | atributos da ficha, caixa, olhos, XP, imunidades (fogo, congelamento, tags, afogamento), queda com 1/5 do dano, ovos, 10 de dano da espada |
| `crimson_animation_catalogue_and_uv` | toda animação com keyframes, duração e bake; preparos com a duração das fases; nenhuma sobreposição de UV |
| `crimson_reference_poses_hold_the_sword_right` | a espada no ombro, nivelada na investida, atrás da cabeça ao armar, cravada à frente no impacto e cruzando o tronco na guarda |
| `crimson_crest_conditions` | bloco errado, salão estreito, ardósia polida aceita, sucesso consome um, chega em `INTRO`, segundo a menos de 48 recusado, falha não consome |
| `crimson_intro_then_duel` | ajoelhado, invulnerável, ergue-se e saúda, depois luta |
| `crimson_chases_far_target` | fecha distância de um alvo a 28 blocos |
| `crimson_slash_hits_front_spares_back` | 12 à frente, 0 atrás, recuperação com animação própria |
| `crimson_never_swings_at_air` | alvo sai durante o preparo: cancela, sem dano, recarga curta |
| `crimson_dash_thrust_hits_and_slows` | 15, Lentidão I, avanço real, recuperação de 18 |
| `crimson_dash_thrust_miss_recovers_longer` | alvo esquiva no início: erro registrado, frenagem própria, recuperação de 24 |
| `crimson_slam_two_rings_and_launch` | salto real, anéis de aviso desenhados onde a lâmina cai, 20 no epicentro, 12 na borda, 0 fora, alvos lançados para cima, recuperação longa |
| `crimson_slam_disables_shields` | escudo na mão desativado por 100 ticks |
| `crimson_slam_reaches_a_pillar` | salta para cima de um pilar de 3 blocos e acerta quem está lá, sem teletransporte |
| `crimson_guard_reduces_frontal_damage` | dano frontal muito menor que sem guarda, sem riposta para golpe distante, de costas sem redução |
| `crimson_guard_deflects_arrows` | flecha frontal devolvida, sem dano |
| `crimson_guard_riposte` | golpe corpo a corpo frontal vira riposta: 14 e arremesso para trás |
| `crimson_guard_after_three_hits` | três golpes em dois segundos levantam a guarda; dano sem atacante (magia) não conta |
| `crimson_guard_turns_slowly` | na guarda ele gira no máximo 12° por tick: quem passa para as costas não é bloqueado nem leva riposta |
| `crimson_wave_travels_hits_and_stops_at_walls` | 13 no alvo a 14 blocos, parede detém a onda |
| `crimson_wave_against_unreachable_target` | do outro lado de um buraco ele usa a onda, nunca corte nem investida |
| `crimson_stages_transition_once` | 60% e 25% uma vez cada, invulnerável na transição, velocidades, cores da barra, duas Sentinelas, recargas -20%, dano +25% |
| `crimson_fury_combo_dash_into_slam` | na fúria, investida que acerta emenda no golpe pesado sem recuperação |
| `crimson_vanguards_cap_and_alliance` | limite 2, atributos, UUID, alianças, imunes aos golpes do mestre, sem saque |
| `crimson_vanguards_expire_and_fall_with_master` | somem aos 600 ticks e quando o mestre morre |
| `crimson_death_rewards_once` | fragmentos 3 a 6, espada só na cinemática e uma vez, espírito sobe, removido ao fim |
| `crimson_content_bestiary_advancements_lang` | 6 páginas e gravuras nos tamanhos certos, 4 conquistas, receita, texturas, loot table e chaves de texto nos dois idiomas |
| `crimson_target_priority_and_hysteresis` | mais próximo por padrão, quem feriu primeiro tem prioridade, histerese de 4 blocos |
| `crimson_frees_himself_from_blocks` | emparedado, sai para o ponto livre mais próximo |
| `crimson_leaps_out_of_a_dead_end` | sem caminho até uma saliência de 2 blocos, salta e pousa nela |
| `crimson_natural_spawn_rules` | biomas do modificador (peso 2, nunca em planícies), câmara escura abaixo de 0 aceita, acima de 0, com luz ou perto de outro Comandante recusa |
| `crimson_greatsword_dash` | Investida Carmesim: 6 blocos, 8 de dano uma vez no hostil, nada na vaca, 8 de durabilidade, recarga |
| `crimson_alliances_and_passive_animals` | golens e jogadores sim, vacas e outro Comandante não |

Não coberto por teste automático: o caminho completo "golpe do boss encontra um jogador real" (o jogador de teste não entra nas buscas de entidades do nível), a renderização (modelo, barra, partículas, sons) e o comportamento em cavernas reais.
