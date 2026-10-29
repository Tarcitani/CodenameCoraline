# Cobrador da Última Dádiva (The Collector of the Last Gift)

Implementação para Minecraft 26.1.2 / NeoForge 26.1.2.124. Boss colossal, cinematográfico e opcional de folclore invernal, inspirado na encenação e atmosfera de chegada aérea de *Nicholas The Renegade* (*Sword Art Online*), reinterpretado com identidade autoral própria.

## Entidades, Itens e Identificadores

- **ID do Boss:** `codenamecoraline:last_gift_collector` (`/summon codenamecoraline:last_gift_collector`).
- **ID do Trenó Cinematográfico:** `codenamecoraline:collector_sleigh` (ator temporário não-combatente, sem persistência em disco).
- **Ovo de Geração:** `codenamecoraline:last_gift_collector_spawn_egg` (**Ovo do Cobrador da Última Dádiva** / *Collector of the Last Gift Spawn Egg*), textura 16×16.
- **Item de Invocação:** `codenamecoraline:silent_bell` (**Sino Mudo** / *Silent Bell*), textura 16×16.
  - Receita (`data/codenamecoraline/recipe/silent_bell.json`): `1x minecraft:bell`, `4x minecraft:packed_ice`, `1x minecraft:soul_lantern`.
- **Itens de Saque:**
  - `codenamecoraline:cracked_bell` (**Sino Trincado** / *Cracked Bell*) — 100% garantido (1 unidade).
  - `codenamecoraline:frostbound_thread` (**Fio de Geada** / *Frostbound Thread*) — comum (2 a 4 unidades).
  - `codenamecoraline:solstice_seal` (**Lacre do Solstício** / *Solstice Seal*) — raro (20% de chance).
- **Conquista Opcional:** `codenamecoraline:bosses/the_final_delivery` (**A Última Entrega** / *The Final Delivery*), moldura `challenge`, ícone `codenamecoraline:cracked_bell`, 100 XP.

## Condições de Invocação (`SilentBellItem`)

O `SilentBellItem` valida cinco condições antes de consumir 1 unidade e iniciar o encontro:
1. Usado diretamente sobre um bloco de `minecraft:packed_ice`.
2. Bioma local é `minecraft:snowy_taiga` ou `minecraft:grove`.
3. Período noturno (`12500..23500` ticks no ciclo do relógio padrão do mundo).
4. Acesso vertical desobstruído ao céu (`Heightmap.Types.MOTION_BLOCKING` e 16 blocos verticais livres) e volume de aterrissagem livre (`3×5×3` acima do altar).
5. Nenhum outro `LastGiftCollector` ativo em um raio de 64 blocos.

Caso qualquer condição falhe, o item **não é consumido** e exibe mensagem clara na barra de ação informando o motivo.

## Cinemática de Abertura (9,5 s / 190 ticks) e Diretor de Câmera

Durante `BossState.INTRO_CINEMATIC`, o boss permanece invulnerável a ataques convencionais, não causa dano de combate e oculta a boss bar até o início real da luta:

1. **`INTRO_PRELUDE` (0,0–1,2 s / ticks 0–24):** vento invernal, neve ascendente e badaladas distantes no céu.
2. **`INTRO_LIGHTNING` (1,2–2,0 s / ticks 24–40):** clarão azul-frio silencioso no céu (cosmético: sem fogo, sem dano, sem alterar o clima global) revelando o `CollectorSleigh` (`codenamecoraline:collector_sleigh`) puxado por cervos espectrais de inverno.
3. **`INTRO_SLEIGH_PASS` (2,0–3,5 s / ticks 40–70):** o trenó de ~8 blocos cruza o céu noturno acima da clareira deixando rastro de geada e lanternas frias.
4. **`INTRO_JUMP` (3,5–4,5 s / ticks 70–90):** o Cobrador agacha no convés e salta do trenó em movimento com a Foice do Inverno erguida.
5. **`INTRO_FALL` (4,5–6,2 s / ticks 90–124):** queda acelerada do céu rumo ao altar de gelo compactado.
6. **`INTRO_LANDING` (6,2–7,3 s / ticks 124–146):** aterrissagem única (`landingCount == 1`) com onda visual de neve e gelo estilhaçado (sem destruir blocos) e tremor de câmera.
7. **`INTRO_REVEAL` (7,3–9,5 s / ticks 146–190):** enquadramento contra-plongée (ângulo baixo), abertura do manto, badalada da gaiola-relicário e exibição do título **"O COBRADOR DA ÚLTIMA DÁDIVA"** (*"THE COLLECTOR OF THE LAST GIFT"*).
8. **`INTRO_COMPLETE` (tick 190):** descarta o trenó temporário, restaura câmera/HUD, exibe a `ServerBossEvent` e inicia `BossState.PHASE_ONE`.

### Controle de Câmera e Tecla de Pulo (`CollectorCinematicDirector`)

- Utiliza `ViewportEvent.ComputeCameraAngles`, `ViewportEvent.ComputeFov`, `MovementInputUpdateEvent` e `RenderGuiEvent.Post` para interpolar suavemente o olhar do jogador, aplicar barras letterbox e exibir o título.
- **Tecla configurável (`key.codenamecoraline.skip_cinematic`, padrão `V`):** libera imediatamente a câmera, o FOV, o HUD e o movimento do jogador local sem quebrar o estado do servidor nem afetar outros jogadores em multiplayer. O bloqueio também é liberado automaticamente se o jogador sofrer dano, morrer, mudar de dimensão ou desconectar.

## Atributos, Fases e Ataques Coreografados

| Atributo | Valor |
|---|---|
| Altura / Largura de Colisão | 4,5 blocos / 1,85 blocos (largura visual de ombros ~2,5 blocos) |
| Vida Máxima | 128 HP (64 corações) |
| Armadura | 8 |
| Velocidade (Fase I / Fase II) | 0,23 / 0,27 |
| Resistência a Repulsão | 0,85 (85%) |
| Alcance de Seguimento | 40 blocos |
| Experiência | 100 XP |

### 4 Ataques Coreografados (`AttackType`)

1. **Foice do Inverno (`WINTER_SCYTHE`):**
   - Preparação de 1,0 s (20 ticks) girando o tronco e recuando a foice longa.
   - Corte horizontal em arco frontal (`SCYTHE_ARC_MIN_DOT = 0.15`, nunca 360°) causando **7,0 HP** de dano e repulsão lateral. Na Fase II, executa um segundo corte de retorno.
   - Recuperação vulnerável de 1,1 s (Fase I) ou 0,8 s (Fase II).
2. **Badalada Oca (`HOLLOW_BELL`):**
   - Preparação de 1,4 s (28 ticks) erguendo a haste da foice enquanto o sino dorsal dobra.
   - Impacto no solo lançando **3 ondas lineares de geada** na Fase I (ou **5 ondas** na Fase II) até 12 blocos, causando **5,0 HP** de dano com brechas seguras entre os raios, zero destruição de blocos e zero nuvens persistentes.
   - Recuperação de 1,2 s (24 ticks) e recarga de 5,0 s.
3. **Procissão dos Penhores (`PAWN_PROCESSION`):**
   - Marca **3 zonas circulares de geada** no chão por **1,0 s (20 ticks)** de aviso visual/sonoro claro antes da erupção.
   - Raízes congeladas irrompem causando **4,0 HP** de dano apenas a quem permanecer dentro das zonas marcadas.
   - Recuperação de 1,0 s (20 ticks) e recarga de 5,5 s.
4. **Salto do Solstício (`SOLSTICE_JUMP` — exclusivo da Fase II):**
   - Preparação de 1,3 s (26 ticks) flexionando os joelhos e mirando a posição do alvo.
   - Salto parabólico controlado de 1,0 s (20 ticks) por até 14 blocos, causando **0 dano durante o voo**.
   - Impacto de aterrissagem causando **8,0 HP** no centro (`3,2` blocos) e **4,0 HP** na onda circular (`5,4` blocos), seguido de **1,5 s (30 ticks)** de recuperação vulnerável.

### Transição para a Fase II (`PHASE_TRANSITION`) e Morte (`DEATH_CINEMATIC`)

- Ao atingir `<= 50%` de vida (`64 HP`), entra uma única vez em `PHASE_TRANSITION` por **3,0 s (60 ticks)**: abre a porta da gaiola-relicário dorsal (`cage_door`), faz o sino trincado vibrar intensamente, muda a cor da boss bar e eleva a velocidade base para `0,27`.
- Ao receber dano letal, cancela imediatamente qualquer ataque ativo e entra em `DEATH_CINEMATIC` por **3,5 s (70 ticks)**: cai de joelhos (`animation.last_gift_collector.death`), apoia e solta a Foice do Inverno, apaga o brilho emissivo dos olhos e concede loot/XP/conquista exatamente uma vez (`deathAwardCount == 1`).


## Eventos de entidade

Os eventos servidor→cliente do Cobrador usam os IDs 100–116 (`LastGiftCollector.EVENT_*`). A faixa 60–76 foi abandonada porque o ID 60 é o "poof" de morte do vanilla, que `tickDeath()` transmite ao final da cinemática: o cliente o interpretava como `EVENT_INTRO_START` e reiniciava a animação de aparição sobre o cadáver. O teste `collector_registration_attributes_and_animations` protege essa faixa.

## Segunda revisão (D-024)

Resumo: seis estágios (a Fúria é o sexto, abaixo de 10% de vida), 12 ataques entre machado, magia e invocação, IA com alvo, antecipação e anti-kiting, Presentes Famintos no lugar dos redcaps, morte com presente de Natal e baú, e história contada pelas conquistas `bosses/*`. Detalhes e motivos em `docs/DECISIONS.md` (D-024). Eventos de entidade: 100–120.
