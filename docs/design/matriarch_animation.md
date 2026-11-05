# Matriarca — sistema de animação

Especificação técnica e matriz das animações da Matriarca (Bruxa dos Dez Fios). Os valores numéricos de cada keyframe
ficam no código, que é a fonte única (`client/ten_threads_witch/matriarch/*Clips.java`, um arquivo por categoria, com um
comentário por clipe descrevendo o gesto). Este documento descreve finalidade, camada, prioridade, duração, sequência,
articulações, interpolação, estágios, compatibilidade, interrupção e eventos de cada ID. Quando um valor ainda precisa de
ajuste no jogo, ele aparece em **Parâmetros a calibrar**.

> Estado: implementado e testado por código (GameTests). **Nada foi validado visualmente no jogo** (`runClient` não foi
> executado). Renders offline do modelo real em poses-chave foram usados só para achar erros grosseiros de geometria.

## 1. Diagnóstico inicial (auditoria)

- **Encontrado**: a Matriarca usava o mesmo `PuppetModel` das marionetes, com **uma** animação por vez (`PuppetAnim`, 13
  valores, `DATA_ANIM`) gerada por papel de parte (`PuppetAnimations`). Não havia camadas, prioridade por articulação nem
  cross-fade. O rig tinha 23 partes: braço inteiriço com mão 2×2 (**sem cotovelo, pulso nem dedos**), cabeça sem boca nem
  sobrancelhas (pintadas na textura), cabelo como uma caixa escondida pelo capuz, capa e saia em uma peça só, e quadril e
  tronco na mesma parte (`body`). Ela ficava parada nos estágios 1–10 (`navigation.stop()` a cada tick) e só andava na fúria.
- **Reaproveitado**: `KeyframeAnimation`/`AnimationDefinition`/`Keyframe`/`AnimationChannel` nativos (dados e
  interpolação), `EncounterPhase` e toda a IA (`AttackRunner`, `PuppetAttack`, as fases do encontro), `TenThreadsEyesLayer`
  (generalizada), o gerador de modelos e texturas, o padrão de sincronização por `SynchedEntityData` e o padrão de GameTests.
- **Limitações confirmadas da API 26.1**: `KeyframeAnimation.apply` escreve direto nas partes, somando, com um único peso para
  a animação inteira; não há peso por articulação nem prioridade. Por isso o sistema amostra as `AnimationDefinition` com o
  mesmo algoritmo do vanilla (busca binária do keyframe + interpolação do keyframe final), mas num buffer, e compõe depois.
- **Limitações que continuam**: modelo de caixas (sem deformação): dedos, lábios e sobrancelhas são caixas de 0,2–0,5 px;
  pernas têm uma só articulação (sem joelho) — ajoelhar é aproximado girando a perna inteira e baixando o quadril; os olhos
  ficam atrás da venda de renda até a fúria.

## 2. O rig (alteração mínima)

Gerado por `tools/generate_ten_threads_witch_assets.py` (silhueta e materiais mantidos; só articulações acrescentadas):

| Região | Articulações |
|---|---|
| Tronco | `body` (quadril, raiz) → `chest` (tronco; pais de cabeça, braços, manto e capa) |
| Pernas | `leg_l/r` → `foot_l/r` |
| Braços | `arm_l/r` (ombro) → `forearm_l/r` (cotovelo) → `hand_l/r` (pulso) → `finger_l0..3`, `thumb_l` (idem `_r`) |
| Cajado | `staff` (filho do antebraço direito; o pulso o mantém em pé, ver §3.6) → `wheel` → `spool_0..9` |
| Cabeça | `head` → `hood` → `hood_tip`; `blindfold`; `eyes` → `eye_l/r`; `brow_l/r`; `mouth`, `mouth_l/r` (cantos) |
| Cabelo | `hair_back`; `hair_lock_l` → `hair_lock_l_tip` (idem `_r`) |
| Capa | `mantle`; `skirt` (painel de trás) → `skirt_hem`; `cloak_l/r` (painéis laterais) |

Cinco dedos por mão (antes nenhum). A mão esquerda pende com a palma para o corpo; a direita fica fechada em volta do
cajado (curvatura de repouso no gerador). Convenções de eixo (lado esquerdo; o direito espelha y e z): braços e pernas vão
para a frente com x negativo e abrem com z negativo; tronco e cabeça inclinam para a frente com x positivo; sobrancelha
com z negativo = raiva, positivo = preocupação; canto da boca com z negativo = sorriso.

Articulações virtuais (sem geometria, animadas como as outras): `fx_thread_l/r` (x alcance, y tensão, z rompimento dos
fios de cada mão), `fx_attack` (x alcance, y quantidade, z chicote dos fios de ataque), `fx_dissolve` (x desintegração da
morte, y ocultação da entrada), `fx_glow` (x brilho dos olhos, y tom carmesim dos fios, z liberdade do cajado).

## 3. Arquitetura

### 3.1 Servidor (`entity/ten_threads_witch/matriarch/`)

- `MatAnim`: o catálogo — um valor por ID com código, camada, prioridade, duração, flags e eventos.
- `MatriarchAnimDirector`: recebe da IA o que aconteceu (ganchos `on…`), escolhe o clipe de cada camada, aplica as regras de
  prioridade e interrupção, encadeia saídas, agenda o comportamento ocioso, aponta o olhar e dispara cada evento uma vez.
  Não escolhe alvo nem ataque.
- `MatriarchPresence`: o mínimo de movimento para haver locomoção e flutuação de verdade (andar devagar perto do centro,
  recuar de quem chega a menos de 3,5 blocos, flutuar a 1 bloco do chão a partir do estágio 6, subir na fúria atrás de um
  alvo mais de 3,5 blocos acima).
- `MatriarchMoveset`: os ataques da última fase, o clipe de cada fase de cada ataque e a tabela de combos.
- `MatriarchEffects`: partículas e sons dos eventos. `MatriarchMood`: os três estágios de comportamento.
- `MatriarchAnimSync` / `MatClipRef`: os relógios do cliente e o valor empacotado de cada camada sincronizada.

### 3.2 Camadas (canais)

| Camada | Sincronizada | Conteúdo | Rank (empate) |
|---|---|---|---|
| `BASE` | não (humor) | respiração MAT-IDLE-01 | 0–1 |
| `GAIT` | estilo e flag de flutuação | locomoção e flutuação (exceto a arrancada) | 10–12 |
| `FACE` | sim | expressões | 20 |
| `HAND_R` / `HAND_L` | sim | fios de cada mão; gestos de duas mãos ocupam as duas | 30 / 31 |
| `BODY` | sim | ações de corpo inteiro: ociosos, provocações, ataques, defesas, atordoamento, quedas, transições, cinemáticas, morte | 40 |
| olhar | alvo + modo | MAT-IDLE-04/05/06 (procedural) | 50 |
| `REACT` | sim | reações a dano, sempre somadas | 60 |
| depois da composição | — | dedos (MAT-IDLE-07), tecido e cabelo (MAT-CLOTH/HAIR) | — |

### 3.3 Composição e prioridade por articulação

Cada camada envia, por canal (articulação × rotação/posição/escala), valor, peso, prioridade e modo. `PoseMixer` ordena as
contribuições de cada canal da prioridade mais fraca para a mais forte (empates pelo rank fixo da camada — nunca pela ordem
de execução do código) e aplica: **substituição** (`lerp` até o valor pelo peso) ou **soma** (valor × peso). Assim uma
camada mais forte domina só as articulações que move. A prioridade é por trilha: o balanço dos braços ao andar tem 9,5,
mais fraco que os fios (9), então andar nunca tira a mão do boneco; o olhar é aditivo com 6,5.

Prioridades (especificação §8): 1 morte; 2 cinemáticas; 3 transições; 4 quedas; 5 atordoamento; 6 reações (aditivas);
7 ataques/defesas/esquivas e ataques de fio; 8 locomoção; 9 manipulação de fios; 10 secundários, expressões, ociosos.

Canais dominados por uma substituição de prioridade ≤ 5 amortecem dedos, tecido e cabelo procedurais (`PoseMixer.lock`).

### 3.4 Interrupção e transição

- Um pedido substitui o clipe da camada quando: o atual acabou; o atual é ocioso (flag `INT`); ou o pedido tem prioridade
  igual ou mais forte. Clipes `LOCK` (queda, transição, cinemática, morte) só cedem a prioridade **estritamente** mais forte.
- Pedido de fio para uma mão que o clipe de corpo reivindica (`HL`/`HR`) **espera** e começa quando o braço é liberado.
- Fim de um clipe: fila da camada → saída fixa (`MatAnim.next`, ex.: queda → impacto; derrota → desaparecimento) → laço de
  controle da mão → nada. `HOLD` segura o último quadro (preparações, chão).
- Toda troca faz cross-fade **dentro** da camada (o clipe anterior continua no seu relógio enquanto some); onde só um dos
  dois mexe num canal, ele entra/sai sozinho. Tempos: reações 1,5 t; ataques 2–2,5; atordoamento/queda 2; transições 4;
  cinemáticas 3; fios 4 (ataque 2); ociosos 7; rosto 6; saída 5–8. Nunca há troca direta para o repouso: o repouso é a
  camada `BASE`, sempre presente por baixo.

### 3.5 Eventos, servidor e multiplayer

Eventos (`MatEvent`) ficam no catálogo com o tick do clipe; o diretor os dispara **no servidor**, uma vez, ao cruzar o tick
(também com velocidade variável). Partículas e sons enviados pelo servidor chegam a todos os clientes; o cliente só desenha
(nunca dispara efeito), então nada se duplica. A morte só remove a entidade depois de `CIN_05 + CIN_06` (144 t).

### 3.6 Cliente (`client/ten_threads_witch/matriarch/`)

`MatriarchAnimator` (um por entidade, lê só entradas simples — o mesmo código roda nos GameTests), `PoseMixer`, `MatClip`
(amostra a `AnimationDefinition` vanilla num buffer), `ClipBuilder` (linguagem de autoria: ticks, graus, pixels; dedos por
flexão), `Ease` (curvas como `AnimationChannel.Interpolation`: SMOOTH, IN, OUT, BACK, SNAP, STEP + LINEAR/CATMULLROM
vanilla), `GaitController`, `GazeController`, `FingerController`, `SecondaryMotion`, `MatriarchModel` (aplica a pose com os
offsets vanilla de parte; dissolve/revela com `skipDraw`, que preserva os filhos; o pulso mantém o cajado em pé exceto em
golpes de cajado e quedas), `MatriarchThreadLayer` (fios saindo das pontas dos dedos animados) e `MatriarchRenderer`.

### 3.7 Desempenho

Sem alocação por quadro nos laços quentes (busca binária sem lambda, buffers reutilizados, ordenação por inserção de no
máximo 12 entradas por canal). Exceções conscientes: um `float[72]` por quadro na camada de fios (a geometria é desenhada
depois, por coleta) e alguns `Vec3` na extração do estado. Molas a 60 Hz fixos e ciclos de passo por distância: o resultado
independe da taxa de quadros.

## 4. Estágios de comportamento

`MatriarchMood.unravel` (0 a 1): marionetes 1–5 → **composta** (< 0,3); 6–10 → **irritada** (0,3–0,6); colapso e fúria →
**desfeita** (0,6 → 1 conforme perde vida). Efeitos: (a) respiração própria de cada estágio; (b) clipes com flag `MOOD`
ganham amplitude (até ×1,45); (c) a partir do estágio irritado o tempo desses clipes é "engasgado" em células de 3 ticks
(segura e salta) — monotônico, termina no tempo certo; (d) o diretor escolhe ociosos, rostos, defesas, esquivas, quedas e
recuperações conforme o estágio; (e) olhar com mais overshoot e inclinações antinaturais; dedos com tremor; tecido e cabelo
com vontade própria. A identidade (rig, figurino, gestos-base) é a mesma nos três.

## 5. Matriz

Colunas: **T** tipo (L laço, A ação, Tr transição, R reação, P procedural); **Cam·Pri·t** camada, prioridade e duração em
ticks; **Ativa → sai**; **Articulações**; **Sequência**; **Int.** interpolação dominante; **Est.** comportamento por estágio;
**Compat.** coexiste com (✓) / incompatível com (✗); **Ev.** eventos (tick). Interrupção segue §3.4 salvo nota.

### 5.1 Repouso e comportamento independente

| ID | Nome | T | Cam·Pri·t | Ativa → sai | Articulações | Sequência | Int. | Est. | Compat. | Ev. |
|---|---|---|---|---|---|---|---|---|---|---|
| IDLE-01a/b/c | Idle principal | L | BASE·10·64/48/56 | sempre; troca por humor com fade de 20 t | quadril, tronco (escala), manto, cabeça, braços, antebraços, mãos | a: 1 respiração em 3,2 s, peso ±0,8°; b: mais curta, ombros erguidos, espasmo de cabeça aos 24; c: torta, respiração que "engasga" (10–14, 30–34) | Catmull; SNAP no espasmo | três versões | ✓ tudo (é a base) | — |
| IDLE-02 | Alternativo elegante | A | BODY·10·70 | agendador (composta 40 %) → fim | cabeça, ombro, antebraço/mão/dedos E, quadril, pernas, tronco | 0–14 inclina a cabeça; 12–20 ajusta o ombro; 18 vira a palma; 18–42 dedos refinados; 20–50 peso à direita (pernas compensam); 50–66 retorno | SMOOTH | mais raro depois | ✓ fios na mão D, olhar (0,6) ✗ ataques | — |
| IDLE-03 | Idle perturbador | A | BODY·10·84 MOOD | agendador (irritada 35 %, desfeita 60 %) | cabeça, dedos E/D, tronco, ombro E | inclinações súbitas da cabeça 10–13, 30–33, 50–52; dedos isolados 16, 40, 60; contrações do tronco 21, 59; ombro 45; retorno assimétrico 78 | SNAP/STEP | amplitude e engasgo crescem | ✓ fios ✗ ações | — |
| IDLE-04 | Observar o jogador | P | olhar·6,5 | alvo/jogador mais próximo (24 b) | olhos (pos), cabeça (≤50° giro, ≤30° pitch), tronco (≤22°) | mola amortecida; olhos lideram | mola | overshoot e inclinação antinatural crescem | ✓ braços sempre livres; peso = olhar do clipe de corpo | — |
| IDLE-05 | Observar marionete | A+P | BODY·10·44 + olhar modo PUPPET | marionete desperta/ferida/enroscada/derrotada | tronco, cabeça, antebraço E, quadril | 0–10 inclina; 8–36 acenos da cabeça; 12–32 mão sobe; 44 volta | SMOOTH | rosto muda (§5.10) | ✓ fios | — |
| IDLE-06 | Observar os fios | A | BODY·10·60 | agendador; olhar modo THREADS (para a mão) | cabeça, braço/antebraço/mão/dedos E, fx_thread_l | 10 cabeça baixa e mão sobe; 13–34 belisca os fios (tensão 0,9 aos 18); 60 volta | SMOOTH | — | ✗ fios na mão E (prioridade) | THREAD_TENSION 18 |
| IDLE-07 | Dedos independentes | P | após composição | sempre | 10 dedos | deriva própria por dedo (2 ondas) + figuras: dobrar um, abrir, ondular, tensionar, espasmo; mãos nunca na mesma figura | — | deriva 3→10°, tremor > 0,3, espasmos no fim | ✓ tudo; ×0,4 em gesto preciso; amortecido por prio ≤ 5 | — |
| IDLE-08a | Gesto de manipulação (1 mão) | A | HAND·9·40 MOOD | floreio do agendador sem boneco | antebraço, pulso (giro ±40°), dedos | 10 reposiciona; 10–26 gira o pulso e dedos um a um; 34 volta | SMOOTH | amplitude | ✓ locomoção, mão oposta | — |
| IDLE-08b | Gestos simultâneos (2 mãos) | A | HAND(L+R)·9·48 MOOD | idem | antebraço/mão/dedos E e D | E fecha e abre (6–30); D tira indicador e médio do cajado (14–34) | SMOOTH | amplitude | ✓ locomoção | — |
| IDLE-09 | Mudança de postura | A | BODY·10·56 | agendador | quadril, pernas, pé E, tronco, cabeça, braços | 12 peso à direita; 14 pé E dá passo; 30 acomoda; 50–56 estável (tecido atrasa pela mola) | SMOOTH | — | ✓ fios | — |
| IDLE-10a | Provocação elegante | A | BODY·10·50 | início de estágio (composta) | cabeça, tronco, braço/antebraço/mão/dedos E | 10 queixo para cima e de lado; 8–16 gesto de desprezo (OUT); 44 volta | SMOOTH/OUT | só composta | ✗ não interrompe ações (INT) | — |
| IDLE-10b | Aplauso lento | A | BODY·10·92 HL HR | marionete acerta o jogador (recarga 360+ t) | braços, antebraços, dedos E, cabeça | 14 mãos sobem; palma E bate na mão D 24, 44, 64; 80–92 volta | SMOOTH | — | ✗ fios (esperam) | CLAP 26, 46, 66 |
| IDLE-10c1..5 | Imitação | A | BODY·10·56/44/50/56/40 | jogador agachado / pulando / bloqueando / comendo-bebendo / atacando (16 b, recarga 420+) | conforme a ação | c1 reverência exagerada; c2 pulinho de braços erguidos; c3 encolhe atrás das mãos e espia; c4 gole com o dedinho erguido; c5 golpe desajeitado de espada invisível | SMOOTH/BACK | rosto EXP-03 (deboche) | ✗ só com o corpo livre | CACKLE; WHOOSH (c5) |

### 5.2 Locomoção (`GaitController`, ciclo por distância percorrida)

| ID | Nome | T | Cam·Pri·t | Ativa → sai | Articulações | Sequência | Int. | Compat. |
|---|---|---|---|---|---|---|---|---|
| MOVE-01 | Caminhada | L | GAIT·8·20 (0,9 b/ciclo) | velocidade > 0,012 b/t, estilo normal | pernas, pés, quadril (pos, rot), tronco, braços (9,5), cabeça (+) | contato do calcanhar 0/10; elevação no meio do apoio 5/15; torção quadril×tronco ±3° | Catmull | ✓ olhar, fios, rosto |
| MOVE-02 | Lenta e ameaçadora | L | GAIT·8·32 (1,0) | estilo MENACE (passeio nos estágios) | idem | pausa no contato (0–6, 16–22), tronco firme +4° | Catmull | ✓ |
| MOVE-03 | Agressiva | L | GAIT·8·16 (1,05) | estilo AGGRESSIVE (alvo < 3 b) | idem + cotovelos a −35° | passadas ±24°, quadril +6° à frente | Catmull | ✓ |
| MOVE-04 | Aceleração | Tr | GAIT·8·14 | parada → movimento | quadril, tronco, braço E | inclina 6→9→3°, braços bombeiam; a cadência sobe sozinha (ciclo por distância) | SMOOTH | ✓ |
| MOVE-05 | Desaceleração | Tr | GAIT·8·14 | movimento ≥ 10 t → parada | pernas, pé D, quadril, tronco, braço | último passo curto, correção para trás aos 6, pés juntos aos 14 | SMOOTH | ✓ |
| MOVE-06 | Corrida | L | GAIT·8·12 (1,7) | > 0,11 b/t | pernas ±38°, quadril +12°, braços ±35° com cotovelo −60° | fase aérea 3/9 | Catmull | ✓ |
| MOVE-07 | Recuo | L | GAIT·8·24 (0,7) | movimento contra o olhar (< −0,3) | pernas, pés (ponta primeiro), quadril −3°, mãos em guarda | sem cruzar pernas (z ±2°) | Catmull | ✓ olhar no alvo |
| MOVE-08 | Aproximação | L | GAIT·8·20 (0,95) | estilo APPROACH (alvo a 3–9 b) | idem + mão E meio erguida (9,5) | passos que encurtam com a velocidade | Catmull | ✓ fios na mão E |
| MOVE-09 | Mudança de direção | Tr | GAIT·8·12 | rumo muda > 60° em < 6 t | cabeça (+), tronco, quadril, pernas | cabeça antecipa 25°, quadril pivota, passo de ajuste | SMOOTH | ✓ |
| MOVE-10 | Giro do corpo | Tr | GAIT·8·16 (espelhado pelo lado) | parada, guinada > 35° em 6 t | quadril, tronco, pernas | cabeça → tronco → quadril → pés, um após o outro | OUT | ✓ |

### 5.3 Flutuação

| ID | Nome | T | Cam·Pri·t | Ativa → sai | Sequência | Compat. |
|---|---|---|---|---|---|---|
| FLOAT-01 | Entrada | Tr | GAIT·8·24 | flag de flutuação liga (fim do PHASE-01; fúria com alvo alto) | passada para, tronco junta (8) e sobe (16), pernas se recolhem e os pés apontam, braços abrem | ✓ fios |
| FLOAT-02 | Estável | L | GAIT·8·60 | flutuando parada | ondulação vertical ±0,6 px, equilíbrio ±1,5° | ✓ |
| FLOAT-03 | Deslizamento elegante | L | GAIT·8·40 | flutuando em movimento, composta | inclina 8° no sentido, pernas arrastam 22°, braços para trás | ✓ |
| FLOAT-04 | Deslocamento perturbador | L | GAIT·8·48 MOOD | flutuando em movimento, irritada+ (o servidor também varia a velocidade: paradas e arrancadas) | reorientações súbitas 11/23/37, pausa 19–30, pernas desiguais | ✓ |
| FLOAT-05 | Arrancada agressiva | A | BODY·7·38 HL | ataque `last_needle_thrust` | 0–14 agacha e recolhe; 14–24 decola (+1,5 px) e dispara inclinada 25°, pernas atrás; 24–38 endireita e pousa | ✗ fios E · DODGE 12, WHOOSH 22 |
| FLOAT-06 | Ascendente | L | GAIT·8·30 | vy > 0,03 | pernas retas, pés apontados, braços sobem, rosto para cima | ✓ |
| FLOAT-07 | Descendente | L | GAIT·8·30 | vy < −0,03 | tronco inclina, pernas vão à frente, braços equilibram | ✓ |
| FLOAT-08 | Suspensão imóvel | L | GAIT·8·80 | parada flutuando > 60 t | quase nada (0,15 px, 1°) — nunca congelada | ✓ |
| FLOAT-09 | Retorno ao solo | Tr | GAIT·8·20 | flag desliga | pés alcançam (8), toque e absorção (−1 px aos 10), recupera (20) | ✓ |

### 5.4 Manipulação dos fios (mão esquerda; espelhada quando a direita toca; duas mãos quando `BOTH`)

Regra de controle: nenhum dedo pertence a uma marionete; qualquer combinação comanda. Todos os clipes de fio usam a pose
de titereiro (braço −30/0/−8, antebraço −55, pulso 0/90/0, dedos 20/25/25/30/15) como referência de entrada e saída.

| ID | Nome | T | Pri·t | Ativa | Sequência (ticks) | Ev. |
|---|---|---|---|---|---|---|
| THREAD-01 | Manifestação | A BOTH | 9·30 | início da fúria (os fios passam às mãos dela); na intro é parte de CIN-03 | 0–10 mãos sobem e juntam os dedos; 18 abrem de golpe; 10–26 fios aparecem (fx 0→1) e assentam | MANIFEST 12 |
| THREAD-02 | Abertura dos dedos | A | 9·16 | sequência de invocação | indicador 4, médio 7, anelar 10, mínimo 13, polegar 13 | — |
| THREAD-03 | Separação individual | A | 9·20 | idem | configurações alternadas 6/12/20; pulso ±6° | — |
| THREAD-04 | Extensão dos braços | A BOTH HOLD | 9·24 | idem | os dois braços se estendem até o alcance (−75°), tronco +6° | — |
| THREAD-05 | Elevação das mãos | A BOTH HOLD | 9·20 | idem | mãos à altura dos ombros; ombros sobem 0,4 px | — |
| THREAD-06 | Cruzamento dos dedos | A BOTH HOLD | 9·24 | antes de especial (sigilo) | indicador/anelar estendidos, médio/mínimo cruzados por baixo (8–16); fios ficam carmesim | TENSION 16 |
| THREAD-07 | Formação de conexões | A BOTH | 9·30 | invocação | cada dedo estala para a frente (9, 12, 15, 18, 21) e um fio salta dele (fx em degraus) | FORM 10, 20 |
| THREAD-08 | Tensionamento | A HOLD | 9·20 | fim da invocação | fecha (8), puxa para cima e para trás, segura; tensão → 1 | TENSION 10 |
| THREAD-09 | Controle de uma marionete | L MOOD | 9·48 | marionete ativa, parada | microcorreções; comandos de um dedo 8/24/32; tensão varia | — |
| THREAD-10 | Controle de várias | L MOOD | 9·64 | mão D com marionetes penduradas | polirritmia lenta dos dedos; fios até as penduradas | — |
| THREAD-11 | Duas mãos simultâneas | L BOTH MOOD | 9·40 | laço de controle do estágio 10 (a última marionete, com as duas mãos) | uma mão sobe enquanto a outra desce, comandos diferentes | — |
| THREAD-12 | Uma mão só | L MOOD | 9·32 | enquanto ela anda nos estágios | mão baixa junto ao quadril; a outra fica livre e balança | — |
| THREAD-13 | Comando preciso | A | 9·14 | floreio periódico sobre o laço | só dedos e pulso (indicador 3, médio 8) | — |
| THREAD-14 | Puxar os fios | A | 9·18 | anti-buraco (içar marionete) | alcança 6, agarra 8, puxa 12 (tronco acompanha) | TENSION 8 |
| THREAD-15 | Soltar os fios | A | 9·20 | marionete derrotada | dedos abrem em sequência, tensão cai, fios somem | RELEASE 8 |
| THREAD-16 | Alterar a tensão | A | 9·16 | floreio; ataque da marionete errou | flexão/soltura do pulso e dedos | — |
| THREAD-17 | Transferir o controle | A BOTH (espelhável) | 9·28 | a cada 600–900 t nos estágios | mãos se encontram (12), fios passam de uma à outra (14–22) | FORM 14 |
| THREAD-18 | Interromper | A | 9·14 | reset (todos saíram) | dedos relaxam, mão desce, fios afrouxam | RELEASE 6 |
| THREAD-19 | Recuperar conexão | A | 9·24 | marionete sai do enrosco | alcança aberta (8), agarra (12), fio refaz (16) | FORM 16 |
| THREAD-20 | Romper conexões | A | 9·18 | fio cortado / marionete derrotada | mão puxada para trás, dedos voam, fio estala (z = 1) e cai | SNAP 4 |
| THREAD-21 | Preparação de ataque | A HOLD MOOD | 7·16 | wind-up de marionete ou da Matriarca | mão ao ombro, dedos juntam, fios esticam, fios de ataque nascem (0,15) | TENSION 8 |
| THREAD-22 | Lançamento | A MOOD | 7·14 | golpes FAN/WIRE; `tripwire_cast` | arremesso (3), fio dispara (alcance 1 aos 5) | RELEASE 5 |
| THREAD-23 | Chicoteamento | A MOOD | 7·16 | golpes CONE/MULTI; `phantom_puppet_echo` | ergue largo (4), estala diagonal (7, BACK), onda do fio (8) | WHOOSH 6, RELEASE 8 |
| THREAD-24 | Estocada | A MOOD | 7·12 | CHARGE/LEAP/BURROW; `thread_lance`, `barrier_lance` | mira, impulso reto com indicador apontado (3) | RELEASE 4 |
| THREAD-25 | Enrolamento | A BOTH MOOD | 7·24 | TRAP; `cocoon_snare` | mãos circulam uma em volta da outra e fecham (16) | FORM 6, TENSION 16 |
| THREAD-26 | Puxão ofensivo | A MOOD | 7·20 | GRAPPLE; anti-pilar (mão D); `binding_coil` | agarra (6), puxão com o corpo (10, IN) | TENSION 9 |
| THREAD-27 | Retração | A | 7·14 | recuperação de ataques de fio | fios recolhem, mão volta à pose de titereiro | RELEASE 4 |
| THREAD-28 | Múltiplos fios | A BOTH MOOD | 7·30 | RING/WAVES/STORM; `thread_barrage` | três rajadas alternando mãos (8, 15, 22); fios por rajada 1→1+4×estranheza (máx. 7) | RELEASE 8, 15, 22 |

### 5.5 Preparações e ataques corporais (corpo, prioridade 7; a execução inclui a recuperação)

| ID | Nome | t | Usado por | Sequência | Ev. |
|---|---|---|---|---|---|
| ATK-01 | Preparação discreta | 8 HOLD | `thimble_flick`, `seam_rip`, finais de combo, contra-ataques | tronco enrola 8°, mão E ao peito, dedos em garra | — |
| ATK-02 | Preparação pesada | 22 HOLD | `staff_crush`, `unmaking_embrace` | cajado acima da cabeça com as duas mãos (14–22), costas arqueadas, base aberta | TENSION 12 |
| ATK-03 | Preparação especial | 30 HOLD MOOD | `unraveled_storm` | sobe nas pontas, braços abertos com palmas para cima, dedos estremecem (22–26), olhos acendem | MANIFEST 6, SURGE 22 |
| ATK-04 | Preparação enganosa | 16 HOLD | `hem_cleave` (ramifica em `feint_flick` se o alvo chega a ≤ 2,2 b aos prep−3) | braços a meia altura, tronco torce para um lado e para o outro | — |
| ATK-05 | Cancelamento | 12 | alvo saiu antes do golpe (o runner cancela) | braços descem, inclinação de desdém | RELEASE 3 |
| ATK-06 | Rápido | 14 | `thimble_flick`, `feint_flick`, `parry_riposte` | arranhão curto em garra (2) e volta | WHOOSH 2 |
| ATK-07 | Pesado | 26 | `staff_crush`, `guard_crush` | cajado desce (3, IN), corpo dobra, impacto, levanta devagar; cajado acompanha o golpe | WHOOSH 3, IMPACT 6 |
| ATK-08 | Uma mão | 18 | `seam_rip`, `dodge_counter` | mão E sobe e rasga em diagonal (6, BACK); mão D livre para fio | WHOOSH 3 |
| ATK-09 | Duas mãos | 26 | `hem_cleave` (2 acertos) | braços cruzam e varrem para fora, duas vezes (6, 14) | WHOOSH 2, 8 |
| ATK-10 | Sobrenatural | 24 MOOD | `unmaking_embrace` | coluna dobra ao contrário, braços giram para trás (4, SNAP), dobra sobre o alvo (8) | WHOOSH 4, CACKLE 6 |
| ATK-11 | Grande alcance | 20 MOOD | `needle_volley` | arremesso lateral com passo, fios de ataque alcançam 1 aos 4 | RELEASE 4 |
| ATK-12 | Especial | 80 MOOD | `unraveled_storm` | sobe 2,5 px e gira duas vezes (0–60) com braços abertos e espasmos de cabeça; cai dobrada (60–80) | POWER_BURST 2, 30, 58 |
| ATK-13 | Finalização de combo | 28 MOOD | `combo_finale` | giro de revés com o cajado (4) e pisão (6), recuperação própria | WHOOSH 4, IMPACT 6 |

Os ataques corporais que movem só o braço esquerdo (ATK-01, 06, 08, 11) liberam a mão direita para fios.

### 5.6 Combos (`MatriarchMoveset.COMBOS`)

Cada ramo: pré-condição = o ataque `from` acabou; janela = ticks; condição de ramificação = condições do alvo; animação
executada = preparação do ataque `to` (a transição parte da cauda de recuperação); cancelamento = alvo morto/fora de vista,
queda, atordoamento ou golpe forte; recuperação = a recuperação de cada ataque. A IA escolhe o ramo; o sistema de animação só
compõe.

| De | Janela | Condição | Para |
|---|---|---|---|
| thimble_flick | 8 | alvo bloqueando com escudo | staff_crush (quebra a guarda) |
| thimble_flick | 8 | perto (≤ 2,8) | seam_rip |
| thimble_flick | 8 | meio (2,8–7) e recuando | phantom_puppet_echo |
| seam_rip | 8 | perto | combo_finale (ATK-13) |
| seam_rip | 8 | longe (> 7) | thread_lance |
| staff_crush | 10 | meio | binding_coil |
| thread_lance | 8 | perto / longe | seam_rip / needle_volley |
| binding_coil | 10 | perto | combo_finale |
| tripwire_cast | 8 | no ar | thread_lance |
| needle_volley | 8 | meio | cocoon_snare |

Contra-ataques ligados à defesa: DEF-02 → `parry_riposte`; DEF-01 → `guard_crush`; DEF-03 → `barrier_lance`;
DEF-08 → `dodge_counter`.

### 5.7 Defesa e esquivas (corpo, prioridade 7)

| ID | Nome | t | Ativa | Efeito de jogo | Sequência | Ev. |
|---|---|---|---|---|---|---|
| DEF-01 | Braços | 18 | fúria: golpe corpo a corpo de jogador pela frente ≥ 7 (chance 18 % + 14 % × estranheza, recarga 100); estágios: golpe no casulo (irritada) | fúria: metade do dano | antebraços cruzados diante do rosto (3), encolhe, recua um passo; → DEF-09 | BLOCK 4 |
| DEF-02 | Bloqueio com a mão | 12 | idem com golpe < 7; casulo (composta) | fúria: anula | palma aberta sobe de estalo (2), queixo erguido | BLOCK 3 |
| DEF-03 | Barreira de fios | 22 | projétil de jogador (50 %); casulo com projétil | anula | mãos sobem e abrem, dedos espalmados, teia diante dela (fx_attack 0,35) | BARRIER 5 |
| DEF-04 | Defesa com marionete | 26 | casulo, marionete ativa a < 7 b (25 %) | visual + evento abstrato | estende a mão (6), puxa os fios atravessando (10) | PUPPET_GUARD 8 |
| DEF-05 | Esquiva lateral | 14 (espelhada) | projétil (50 %, estranheza ≤ 0,75) | anula e desloca 0,55 de lado | inclina e dá passo para o lado; → DEF-09 | DODGE 2 |
| DEF-06 | Esquiva para trás | 16 | fúria: jogador a < 1,6 b (1/24 por tick, recarga 70) | salto para trás 0,6 | pulinho para trás com o tronco afastado | DODGE 2 |
| DEF-07 | Esquiva sobrenatural | 18 MOOD | projétil com estranheza > 0,75 | anula e desloca | dobra de lado além do possível (2, SNAP) com a cabeça nivelada e volta (13) | DODGE 3 |
| DEF-08 | Esquiva agressiva | 18 | como DEF-06 com estranheza > 0,5 (50 %) | salto + contra-ataque | recua, investe numa estocada baixa com a garra pronta | DODGE 2 |
| DEF-09 | Recuperação defensiva | 14 | depois de DEF-01/02/03/05/06/07 | — | volta à base, compostura | — |

### 5.8 Reações a dano (camada REACT, sempre somadas, prioridade 6; só na fúria — nos estágios ela é intocável)

| ID | Nome | t | Escolha | Articulações / sequência |
|---|---|---|---|---|
| HIT-01 | Leve | 8 | dano < 4 | tronco −5°, cabeça −6° (2) |
| HIT-02 | Moderado | 12 | 4–8 | tronco, ombros, cabeça viram (2) |
| HIT-03 | Pesado | 20 | ≥ 8 sem atordoar | empurrada para trás 1 px, braços abertos, passo para se apoiar |
| HIT-04 | Pelas costas | 14 | golpe vindo de trás | joga para a frente, cabeça chicoteia para trás, braços atrás |
| HIT-05 | Durante manipulação | 10 | uma mão com fio ativo | **só tronco e cabeça** — braços e fios intactos |
| HIT-06 | Durante ataque | 8 | ataque em execução/recuperação | tremor do tronco; o ataque segue |
| HIT-07 | Desequilíbrio | 18 | flutuando ou em movimento | quadril rola ±10°, pernas e braços corrigem |
| HIT-08 | Perda de coordenação | 26 MOOD | 3 golpes em 40 t | cabeça, braços e dedos fora de sincronia (degraus) |

Intensidade cresce com o dano e o estágio pela escolha acima e pelo rosto (EXP-05 → EXP-08).

### 5.9 Atordoamento (corpo, prioridade 5; incapacita a IA)

| ID | Nome | t | Ativa → sai | Sequência |
|---|---|---|---|---|
| STUN-01 | Interrupção breve | 10 | golpe ≥ 6 na preparação (cancela o ataque; recarga de atordoamento 80 t) → fim | congela e cambaleia, dedos abrem |
| STUN-02 | Perda de equilíbrio | 18 | golpe ≥ 9 fora da execução, estranheza ≤ 0,7 (mesma recarga de 80 t) → STUN-05 | balança para um lado e outro, braços giram, pés procuram chão |
| STUN-03 | Espasmos | 24 MOOD | idem, estranheza > 0,7 → STUN-05 | trancos seguros por um instante em direções sem sentido (STEP) |
| STUN-04 | Prolongado | L 40 MOOD | tempestade erra (80 t de enrosco) | de joelhos, curvada, balançando, espasmos 20–30 |
| STUN-05 | Recuperação | 22 | fim de STUN-02/03/04 (fade de entrada mais longo) | controle volta junta a junta, pequena sobrecorreção, fios a firmam |

### 5.10 Queda e recuperação (corpo, prioridade 4, LOCK; duas poses de chão: de costas e de bruços)

Regra de derrubada (nova, não havia no sistema): golpe único ≥ 12 + 4 × estranheza na fúria, no máximo uma derrubada a cada 240 t (dentro da recarga um golpe pesado só a faz cambalear); de frente → FALL-02, de trás → FALL-01,
estranheza > 0,8 → FALL-03. Variantes: ≥ 20 rápida (×1,35), < 14 lenta (×0,75). No chão 26 t (FALL-04 segura), depois a
recuperação: 30 % FALL-09 (estranheza < 0,9), senão FALL-05 (< 0,35), FALL-06 (< 0,7) ou FALL-07. Golpe nos primeiros 60 %
da recuperação → FALL-08 (uma vez) → chão → recuperação de novo.

| ID | Nome | t | Sequência | Ev. |
|---|---|---|---|---|
| FALL-01 | Para a frente | 16 → 04 | tropeça (4), braços estendem para amparar, de bruços (16) | FALL_IMPACT 13 |
| FALL-02 | Para trás | 16 → 04 | braços sobem, pés chutam, de costas (16) | FALL_IMPACT 13 |
| FALL-03 | Sobrenatural | 22 → 04 (costas/bruços) | tomba rígida como marionete cortada, gira errado no ar (3–8, SNAP), bate chapada (14) | FALL_IMPACT 18 |
| FALL-04 | Impacto | 10 HOLD (2 versões) | achata (2), quica (5), cabeça levanta, assenta | — |
| FALL-05 | Recuperação elegante | 30 (2) | senta/empurra (8), ajoelha (16), levanta e ajeita o capuz com a mão (22–28) | — |
| FALL-06 | Parcialmente sobrenatural | 32 MOOD (2) | começa humana (0–12) e é içada pelo peito (14, SNAP), membros pendurados, posta de pé (28) | CACKLE 16 |
| FALL-07 | Antinatural | 28 MOOD (2) | arqueia sobre cabeça e calcanhares, dobra ao contrário, juntas estalam no lugar uma a uma | — |
| FALL-08 | Interrompida | 16 → 04 (2) | meio de pé, é golpeada e cai de novo | FALL_IMPACT 10 |
| FALL-09 | Retomando os fios | 32 (2) | mãos lançam fios para cima (6), agarram (10), é puxada rígida até ficar de pé (14–24) | MANIFEST 6, TENSION 14 |

### 5.11 Expressões (camada FACE; laços com vida própria)

Partes: sobrancelhas (altura, inclinação), boca (largura, abertura), cantos da boca, olhos (tamanho; brilho). Com a venda
(estágios 1–10) os olhos não aparecem e a expressão vem de sobrancelhas e boca.

| ID | Nome | t | Uso padrão | Composição |
|---|---|---|---|---|
| EXP-01 | Sorriso elegante | 60 | intro, provocação elegante, especial composta | cantos −18°, boca ×1,1, olhos 0,85 |
| EXP-02 | Sorriso arrogante | 60 | padrão composta; aplauso | sorriso torto (−28/6), uma sobrancelha erguida, pálpebras pesadas |
| EXP-03 | Desprezo | 48 | defesa, imitação, derrota de marionete (composta) | sobrancelhas erguidas, cantos para baixo, olhos estreitos |
| EXP-04 | Curiosidade | 56 | fio cortado (composta), reset, início do estágio 6+ | sobrancelhas desiguais, boca em "o", olhos grandes |
| EXP-05 | Irritação contida | 50 | padrão estágios 6–7; erro de marionete | sobrancelhas baixas no centro, lábios apertados, tique aos 25 |
| EXP-06 | Irritação crescente | 40 | padrão 8–10; PHASE-01; derrota de marionete (irritada) | mais dura, cantos para baixo, boca meio aberta, tiques |
| EXP-07 | Fúria | 30 | padrão fúria com alvo (estranheza < 0,85); ataques | rosnado aberto, sobrancelhas no fundo, olhos grandes, tremor |
| EXP-08 | Perturbadora | 64 MOOD | padrão fúria sem alvo / fim | sorriso largo demais que cresce, sobrancelhas em desacordo, olhos desiguais |
| EXP-09 | Perda de controle | 36 MOOD | colapso, quedas, atordoamento | troca de expressão a cada 9 t (preocupação, aperto, sorriso) |
| EXP-10 | Sobrenatural | 44 MOOD | especial, início da fúria, sigilo (irritada+) | sobrancelhas altas e paradas, boca esticada aberta, olhos acesos |

### 5.12 Vestido e cabelos (`SecondaryMotion`, molas de 2 eixos a 60 Hz)

| ID | Modo | Comportamento |
|---|---|---|
| CLOTH-01 / HAIR-01 | natural | molas em repouso seguem respiração e pequenos movimentos com atraso |
| CLOTH-02 / HAIR-02 | caminhada | arrasto proporcional à velocidade (até 45°), balanço lateral e por giro; a barra segue o painel e a ponta da mecha segue a mecha (atraso encadeado) |
| CLOTH-03 / HAIR-03 | flutuação | o tecido sobe e ondula devagar; vertical (subir/descer) levanta ou abaixa |
| CLOTH-04 / HAIR-04 | ataques | impulso dos trancos do tronco e do quadril (barra e pontas ×1,4) |
| CLOTH-05 / HAIR-06 | sobrenatural | a partir do estágio irritado, cada peça ganha movimento próprio fora de fase até contorcer (amplitude ∝ estranheza) |
| HAIR-05 | flutuação independente | flutuando e irritada+, as mechas sobem e se afastam |

Clipes de prioridade forte (transições, cinemáticas) escrevem o tecido diretamente e amortecem as molas.

### 5.13 Transições de estágio (corpo, prioridade 3, LOCK; a IA espera)

| ID | Nome | t | Quando | Sequência | Ev. |
|---|---|---|---|---|---|
| PHASE-01 | Para o intermediário | 64 | entre a 5ª e a 6ª marionete (o intervalo cresce 64 t) | olha o cajado; fio estala (20) e ela recua; ergue-se mais alta, ombros sobem, tecido agita; surto abre os braços (38); mãos voltam aos fios numa pose mais larga e dura (50–64); depois começa a flutuar | SNAP 20, SURGE 38, MANIFEST 50 |
| PHASE-02 | Para o avançado | 50 | colapso (fim da 10ª) | todos os fios estalam (4), cai de joelhos (14), arranha o rosto com a cabeça tremendo (18–28), surto: cabeça para cima, costas arqueadas, braços abertos, tecido em leque (30–50) | SNAP 4, FALL_IMPACT 14, SURGE 30 |
| PHASE-03 | Recuperação sobrenatural | 28 | após PHASE-02 | içada pelo peito, membros pendurados, girada, posta de pé; a cabeça estala por último | FORM 6 |
| PHASE-05 | Intensificação corporal e facial | 26 | após PHASE-03 | estica torta, dedos dobram um a um, braços flutuam à frente com pulsos moles; tranco de ombros | CACKLE 12 |
| PHASE-04 | Manifestação ampliada dos fios | 30 | fecha o colapso | braços abertos para cima, fios por toda parte (8), varre para baixo até a guarda da fúria | POWER_BURST 8, MANIFEST 20 |

Colapso total = 50 + 28 + 26 + 30 = 134 t (`COLLAPSE_TICKS`).

### 5.14 Cinemáticas (prioridade 2; morte prioridade 1)

| ID | Nome | t | Sequência | Ev. |
|---|---|---|---|---|
| CIN-01 | Entrada | 60 | fios sobem na arena (1); ela surge do casulo, enrolada, braços cruzados, flutuando: silhueta primeiro, depois cabelo e vestido, mãos por último (ocultação 1 → 0 entre 8 e 56); desenrola | INTRO_THREADS 1, INTRO_REVEAL 16, 36 |
| CIN-02 | Apresentação | 50 | ergue a cabeça e olha o jogador (10–22); descruza e abre os braços com a palma para cima (18–30); dedos desabrocham um a um (24–34); fios brotam (30–40); pequena reverência (40–50) | MANIFEST 30 |
| CIN-03 | Primeira manifestação de poder | 44 | mão sobe alta e se abre (10), fecha lançando fios às dez marionetes penduradas (14), puxão (20), cajado ergue e a roda gira (20–34), pose final exclusiva (36–44) | FORM 14, POWER_BURST 24, TENSION 30 |
| CIN-04 | Apresentação de mudança de estágio | — | não é clipe: o servidor toca PHASE-01 ou a cadeia PHASE-02→03→05→04 conforme o estágio que começa | — |
| CIN-05 | Sequência de derrota | 116 HOLD | ver §6 | SNAP 28, 36, 56; RELEASE 48, 64; VANISH dos fios 72; DISINTEGRATE 78, 88, 98, 108 |
| CIN-06 | Desaparecimento final | 28 HOLD | resto se desfaz: cabelo e capuz, cabeça, olhos; o sorriso por último (dissolve 0,8 → 1) | DISINTEGRATE 6, 16; VANISH 26 |

Entrada inteira = 154 t (`INTRO_TICKS`); a IA de combate e o 1º estágio só começam depois; ela é intocável durante.

## 6. Morte

`die()` → fase `DEATH_CINEMATIC`, todas as camadas param (o clipe dono do corpo e do rosto é CIN-05), marionetes somem.

| Fase | Ticks | O que acontece |
|---|---|---|
| 1 Resistência | 0–28 | tenta manter a postura; braços tremem em degraus; dedos perdem a precisão; cabeça faz "não"; rosto de incredulidade (olhos ×1,4, sobrancelhas altas, cabeça recua aos 6) |
| 2 Perda de controle | 28–56 | fios estalam (28 mão E, 36 mão D, 56), outros afrouxam; mãos erram comandos; pernas fraquejam (44) |
| 3 Desprendimento | 48–76 | fios se soltam e somem (72); braços relaxam; dedos saem da pose de manipulação |
| 4 Desintegração | 76–116 | dedos e mãos (0,08–0,18), antebraços, braços, cajado, pés e pernas, capa, quadril, tronco (até 0,8); o corpo afunda conforme as pernas somem; partículas a cada 10 t |
| 5 Expressão final | 0–116 | incredulidade (0–30) → desespero (30–70: sobrancelhas franzidas para cima, cantos para baixo, olhos que fogem) → sorriso prolongado (70 →) |
| 6 Desaparecimento | 116–144 | cabelo, capuz, cabeça, olhos; o sorriso some por último (0,985); `VANISH`; a entidade só é removida aos 144 t e então a caixa de recompensa aparece |

A cor vermelha de dano é suprimida durante a cinemática.

## 7. Testes (`verification/MatriarchAnimationGameTests`, 20 testes)

| Teste | Cobre |
|---|---|
| ma_catalogue_complete | todo ID de §5 tem clipe com trilhas, duração e laço iguais ao catálogo, espelho, versão de bruços nas quedas; eventos dentro do clipe; contagem de IDs por família (10/10/9/28/13/9/8/5/9/10/5/6/5/6); todo ataque com clipes; combos só para ataques dela |
| ma_rig_has_the_joints | as articulações novas existem; modelo assa; venda sai |
| ma_composition_order_independent | resultado igual em qualquer ordem; prioridade e soma corretas |
| ma_t01 … ma_t12 | os 12 cenários de aceitação (§13 da especificação), descritos no relatório; os do cliente compõem poses com o `MatriarchAnimator` real, os do servidor rodam o diretor numa entidade real |
| ma_events_fire_once | três palmas, cada uma disparada uma vez |
| ma_priority_and_waiting | ocioso cede; queda não cede a ação mais fraca; cede a mais forte; fio espera o ataque de duas mãos e começa depois |
| ma_combo_branches | ramos por distância e por alvo recuando |
| ma_fury_breathes_between_sequences | depois de uma sequência de golpes ela respira e não ataca; a pausa encurta perto da morte |
| ma_no_knockdown_or_stun_lock | segunda derrubada logo depois vira cambaleio; terceiro golpe seguido é só reação |

## 8. Parâmetros a calibrar no jogo (`runClient`)

Método: ver a cena no jogo a 1×, em câmera lenta (`/tick rate 5`) e de perto; ajustar o número no clipe e repetir.

- Escala visível dos dedos, sobrancelhas e boca (caixas de 0,2–0,5 px): podem precisar de amplitude maior para ler à distância.
- Passadas por ciclo (`GaitController.stride`) contra a velocidade real: pés escorregando indicam passada errada.
- Altura de flutuação (`MatriarchPresence.HOVER = 1,0`) e a ondulação de FLOAT-02.
- Forças das molas de tecido e cabelo (`SecondaryMotion.STIFF/DAMP`, impulso 900).
- Altura do quadril nas poses de chão e ajoelhada (−5, −5,5, −3,5 px): pode atravessar o chão ou flutuar.
- Compensação do cajado (0,9) e os ticks de golpe de cajado (fx_glow z).
- Fios: espessura, curvatura (catenária) e quantidade nos ataques.
- Ordem da desintegração e da revelação (`MatRig.dissolveOrder/revealOrder`).
- Chances e recargas de defesa e esquiva (18 % + 14 % × estranheza; 100 t), o limiar de derrubada (12 + 4 × estranheza, recarga 240 t), a recarga de atordoamento (80 t) e a pausa entre sequências da fúria (46 → 32 t).

## 9. Ritmo da fúria (medido)

Depois de cada sequência de golpes (um ataque ou um combo inteiro), ela respira antes de escolher o próximo:
`FURY_REST_CALM` 46 t com vida cheia, caindo até `FURY_REST_WILD` 32 t perto da morte (pela estranheza). Contra-ataques de
defesa e esquiva não esperam a pausa. Medição com uma sonda temporária (removida) no GameTestServer: a Matriarca contra
um golem parado e sem IA, 60 s, golem recolocado no lugar a cada segundo.

| Versão | Dano/s | Golpes/min | Amostras |
|---|---|---|---|
| Fúria original (3 ataques, sem pausa) | 2,2 | 14 | 4 |
| Nova, sem pausa | 4,1 | 29 | 4 |
| Nova, vida cheia (pausa ≈ 40 t) | 2,5 (1,1×) | 17 | 12 |
| Nova, perto da morte (pausa 32 t) | 3,0 (1,35×) | 21 | 8 |

Limites: alvo parado, sem jogador (defesas, esquivas e derrubadas só reagem a jogadores e não entram na conta). O que a
luta parece para um jogador que se move continua a ser avaliado no jogo.
