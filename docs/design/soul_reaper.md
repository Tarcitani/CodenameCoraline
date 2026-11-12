# Ceifeiro das Almas Vagas (`soul_reaper`)

> **Arquivos complementares obrigatórios:**
> - [soul_reaper_animation.md](soul_reaper_animation.md) — Hierarquia completa do rig, contrato de camadas (L0–L5), física de tecido/duas mãos e coreografias integrais (Lebre, despertar, M1–M5, O1–O5, F1–F6, E1–E4, P1–P5, A1–A6).
> - [soul_reaper_systems.md](soul_reaper_systems.md) — Escala multiplayer (`N_peak`), regras anti-instakill pós-armadura, anulação por procedência (A1–A6), memória de hábitos, arena e diálogos.

## 1. Ficha

### 1.1 Identificação e Lore
- **ID / nome interno:** `soul_reaper` (arauto: `omen_hare`, lacaio: `wandering_soul_wisp`); **Nome pt_br:** Ceifeiro das Almas Vagas; **Nome en_us:** Reaper of Wandering Souls.
- **Perfil:** `boss` (cinemática de metamorfose, barra temática gélida/espectral, 3 estágios monotônicos, 11 ataques, 4 esquivas, 5 provocações, 6 anulações, 5 locomoções e GameTests dedicados).
- **Nome do pacote (`pkg`):** `reaper` (localizado em `entity/reaper/` e `client/reaper/`).
- **Mob de referência (estrutura de código):** `LastGiftCollector` (boss colossal com foice, cinemática e barra temática) + conceitos de camadas/mixer de `MatriarchAnimator`.
- **Lore e Origem Mítica (4–8 linhas):**
  Nos picos congelados e nas taigas sepultadas sob nevascas perpétuas, os povos antigos temiam a figura do Ceifeiro das Almas Vagas. Não se trata de um espectro vingativo vulgar, mas do psicopompo primordial do inverno feérico: uma manifestação viva do repouso final que colhe espíritos perdidos no frio antes que apodreçam na matéria. Onde passa, a vegetação definha instantaneamente em pó de geada e cinzas secas. Disfarçado na tempestade como uma solitária lebre albina de olhos rubis, ele testa a índole dos viajantes; aqueles que erguem a mão para abater a criatura despertam a encarnação da morte em toda a sua estatura colossal.
- **Assinatura de Silhueta e Proporções (OBRIGATÓRIO):**
  Colossal, com **5,4 blocos de altura** (três vezes a estatura de um jogador). Não possui pernas humanas visíveis: abaixo da cintura há uma pelve espectral dissolvida num manto negro-cinzento esfarrapado que se espalha em névoa sólida no chão. O rosto é um vácuo escuro sob um capuz profundo segmentado, com dois brilhos oculares azuis-frios e um grande broche espiral prateado ancorado no peito. Braços esguios e longos (com clavícula, ombro, cotovelo, antebraço, punho e 5 dedos articulados por mão) empunham com duas mãos uma foice curva de 6 blocos de comprimento total.
- **Referências visuais:**
  Arte conceitual folclórica monocromática: manto fluido com dobras góticas, capuz pontiagudo, broche espiral no peito, foice com cabo curvo de madeira gélida ancestral e lâmina longa de ferro meteórico, acompanhado da lebre branca aos seus pés. Paleta: ardósia, preto carvão, cinza espectral, prata fosca, brilho azul-alma nas runas e rubi nos olhos da lebre.
- **Dimensões:**
  - `soul_reaper`: altura `5.4` blocos, largura da colisão `2.2` blocos, altura dos olhos `4.8` blocos.
  - `omen_hare`: altura `0.6` blocos, largura `0.5` blocos, altura dos olhos `0.45` blocos.
  - `wandering_soul_wisp`: altura `0.8` blocos, largura `0.6` blocos, altura dos olhos `0.4` blocos.
- **Resolução do atlas / texel:** textura `256x256` px para o Ceifeiro (`soul_reaper.png` + `soul_reaper_eyes.png`), `64x64` px para a Lebre (`omen_hare.png` + `omen_hare_eyes.png`) e `32x32` px para a Alma Vaga (`wandering_soul_wisp.png`), geradas e validadas sem sobreposição de UV por `tools/generate_soul_reaper_assets.py`.

### 1.2 Atributos e Ecologia Básica
- **Atributos (`soul_reaper`, base N=1):**
  - Vida: `350` pontos (escala até `2240` em N=10; ver `soul_reaper_systems.md`).
  - Armadura: `12` pontos (+ resistência de armadura `4`).
  - Velocidade de movimento base: `0.26` (`0.32` no Estágio 3; modulada por M1–M5).
  - Dano base de ataque: `14` pontos (cada golpe usa seu valor nominal da tabela 1.4).
  - Resistência a repulsão: `0.9`.
  - Alcance de visão (`followRange`): `48` blocos.
  - XP concedida na morte: `150` pontos.
- **Imunidades e afinidades:**
  - Imune a congelamento (`freeze`), fogo após 2s (extingue chamas com geada), afogamento, queda, veneno, decomposição (`wither`), regeneração e lentidão.
  - Afinidade: fora de combate em nevasca ativa, regenera +1 PV a cada 40 ticks.
- **Efeito Passivo Ecológico — Aura de Desolação Gélida:**
  - Raio: `6` blocos esféricos ao redor do boss, avaliado a cada `10` ticks.
  - Regras seguras: afeta apenas blocos naturais de vegetação (`#minecraft:leaves`, `#minecraft:flowers`, `#minecraft:saplings`, grama alta, samambaias e vinhas), respeitando a gamerule `mobGriefing` e ignorando blocos protegidos/colocados em estruturas para não destruir construções de jogadores. Quebra em partículas de folhas secas acinzentadas e geada sem dropar itens.
- **Onde aparece / Invocação Orgânica (A Lebre dos Presságios):**
  - **Spawn Natural:** Durante tempestades em biomas de neve (`level.isRaining() && level.isThundering()` em `#minecraft:is_snowy_plains`, `#minecraft:is_snowy_taiga`, *Ice Spikes*, *Snowy Slopes*, *Grove*), surge naturalmente uma única **Lebre dos Presságios** (`omen_hare`, 10 PV).
  - **Limite estrito:** Máximo `1` lebre ativa num raio de 128 blocos por jogador e nenhum `soul_reaper` ativo na dimensão.
  - **Provocação e Gatilho:** A lebre aproxima-se até 3–4 blocos do jogador sem fugir, ergue-se nas patas traseiras (`hare_stand_stare`) e inclina a cabeça em escárnio (`hare_tilt_taunt`) com olhos rubis emissivos. Quando um jogador causa dano letal à lebre, a morte comum é interceptada e inicia-se a **Cinemática de Metamorfose** (200 ticks). Se a tempestade acabar sem abate, a lebre se desfaz em neve.
- **Limite de spawn:** `1` Ceifeiro ativo por dimensão; **dificuldade pacífica:** não spawna (despawn imediato em Pacífico).
- **Tags de alvo:** `player` (prioritário), `iron_golem`, `snow_golem` e criaturas atingidas no raio da Colheita de Almas.

### 1.3 Matriz de Comportamento e Interações
- **Relação com a própria espécie:** Solitário absoluto.
- **Mortos-vivos:** Indiferente fora de combate; ceifa qualquer morto-vivo que entre no arco dos golpes.
- **Aldeões e golems:** Hostil a golems de ferro e neve; ignora aldeões.
- **Animais pacíficos:** Indiferente, exceto durante a *Colheita de Almas* (drenados caso estejam no raio de 10 blocos).
- **Outros mobs do mod:** Neutro para Matriarca e Quebra-Copas; hostil contra Basilisco da Praga e Litófagos.
- **Luz solar vs escuridão:** Não queima ao sol; sob sol aberto seu alcance de percepção fora de combate cai para 32 blocos.
- **Água e chuva:** Congela água superficial sob sua base em Gelo Fosco; converte chuva num raio de 16 blocos em nevasca visual.
- **Fora do bioma:** Se levado a biomas quentes, perde 4 de armadura e retorna ao ponto de invocação via Passo Espectral ao atingir o leash (48 blocos).
- **Rotina fora de combate:** Locomoção base unificada em **`glide`** (Deslize Espectral; `walk` é apenas alias legado de compatibilidade).
- **Idles sorteados:** **Idle 1 (`idle_1`, 50t):** respiração solene e pulso do broche; **Idle 2 (`idle_2`, 40t):** giro de meia-volta do cabo na mão esquerda e batida do pomo no solo; **Idle 3 (`idle_3`, 60t):** abertura do manto superior como asas negras.

### 1.4 Combate, Fases e Tabela Unificada de Ações

- **[BOSS] Cinemática de Metamorfose e Entrada (200 ticks / 10s, boss invulnerável):**
  - **Ticks 0–40 (`hare_rupture`):** A lebre abatida levita a 1,2m; olhos emitem fachos rubis; fendas de fumaça negra abrem-se no peito.
  - **Ticks 40–90:** Explosão ciclônica de névoa escura; a Aura de Desolação limpa vegetação natural em 12 blocos; a lebre cristaliza-se no broche espiral prateado.
  - **Ticks 90–150 (`awaken_eruption`):** A partir do broche ancorado, condensam-se em sequência pelve, coluna, tórax, capuz segmentado, braços/dedos, manto e a foice de 6m trazida por um raio azul-gélido.
  - **Ticks 150–200 (`awaken_slam`, impacto no tick 175 / 25 da pose):** O Ceifeiro ergue a foice com ambas as mãos e crava o cabo no gelo, gerando onda de choque que empurra jogadores a até 6 blocos em 4 blocos (sem dano), ativando a barra temática.

- **Tabela Unificada de Ataques (11 ataques: 5 Originais `O1–O5` + 6 Frontais `F1–F6`):**
  - *Regras:* Todos os ticks de impacto são contados a partir do **tick 0 da fase de Execução**. Ataque que erra todos os alvos no(s) tick(s) de impacto recebe **+6 ticks na Recuperação (anti-whiff)**. O alvo (`lockedTargetId`) é travado no início do Preparo. As hitboxes são calculadas no servidor por arcos, cones e cápsulas (nunca pela caixa de colisão corporal de 2,2b).

| ID / Código | Nome | Estágio mín. | Alcance / Geometria | Preparo (t) | Execução (t) | Recup. (t) | Tick(s) de impacto | Dano nominal | Cooldown (t) | Papel e Contra-jogada |
|---|---|---|---|---:|---:|---:|---|---:|---:|---|
| **O1** `whirlwind` | Ceifa Giratória 720° | 1 | Raio `5.5` (360°) | 24 | 16 | 20 | `6`, `12` | 18 | 100 | Anti-cerco (2 voltas completas), quebra escudo. Saltar/sair do raio. |
| **O2** `blink_strike` | Passo Espectral em Cunha | 1 | Teleporte `14.0` → golpe `3.5` | 18 | 12 | 16 | `5` | 16 | 120 | Fecha distância com fumaça cinza-escura. Esquivar para o lado após o som. |
| **O3** `soul_harvest` | Colheita de Almas | 2 | Esfera `10.0` (LOS) | 30 | 40 | 25 | `10, 20, 30, 40` | 3/pulso | 400 | Drena até `min(N,4)` alvos + Almas Vagas; cura boss (teto `24+6(N-1)`). Quebrar LOS ou causar 40+ dano. |
| **O4** `frost_wave` | Corte de Geada Rasteiro | 1 | Linha `20.0` (larg. `1.8`) | 20 | 10 | 18 | `5` (disparo) | 12 | 90 | Pressão à distância e follow-up anti-pillaring (trajetória reta fixa). Saltar ou sair da linha. |
| **O5** `reaping_dash` | Vórtice em Avanço Espiral | 2 | Corredor `12.0` (larg. `2.5`) | 22 | 18 | 24 | `4, 9, 14` | 15 | 140 | Avanço em parafuso horizontal. Sair da reta ou bloquear em ângulo. |
| **F1** `rising_crescent` | Lua Ascendente | 1 | `6.0` (arco frontal 140°) | 20 | 10 | 22 | `6` | 14 | 60 | Corte diagonal ascendente, lança `1.2b` vertical e quebra guarda. Recuar 2b ou rolar à direita do boss. |
| **F2** `haft_thrust` | Estocada do Cabo | 1 | `7.0` (linha `1.5`) | 14 | 8 | 18 | `4` | 12 | 50 | Golpe rápido com pomo, repulsão `3.0b`, desativa escudo 40t no contato. Passo lateral. |
| **F3** `cross_reap` | Ceifa Cruzada | 2 | `5.5` (arco frontal 160°) | 18 | 24 (`8+6+10`) | 26 | `4`, `20` | 10 + 13 | 110 | Dois cortes em X (esq→dir depois dir→esq do boss). Entrar por baixo ou esquivar na diagonal. |
| **F4** `gravedigger_slam` | Cova do Coveiro | 2 | Direto `5.0` / Cone `8.0` | 32 | 12 | 36 | `7` | 20 (ou 8 no cone) | 160 | Martelada vertical lenta (dano direto e cone não somam no mesmo alvo). Sair do cone e punir nos 36t. |
| **F5** `low_sweep` | Varredura Rasteira | 1 | `6.5` (arco 200°, alt. `0.6`) | 22 | 12 | 20 | `6` | 11 | 70 | Corte rente ao solo + Lentidão I (40t) em quem estiver no chão. Saltar entre os ticks 4–8. |
| **F6** `threefold_harvest` | Rajada Tripla | 3 | `5.5` (+`1.0b` avanço/corte) | 16 | 24 (`7+7+10`) | 30 | `3`, `10`, `20` | 7 + 7 + 11 | 180 | Três cortes contínuos com troca real de mão no 2º corte. Esquivar em diagonal ou bloquear o 3º. |

- **Seleção de IA (Anti-repetição):** O boss guarda os últimos 3 ataques executados; o último ataque fica bloqueado na escolha imediata e os dois anteriores recebem peso `0.25x`, garantindo rotação constante entre famílias de golpe.

- **[BOSS] Estágios (Progressão Monotônica — nunca regride):**
  - **Estágio 1 (100% – 65% PV) — O Vigia das Neves:** Usa `O1`, `O2`, `O4`, `F1`, `F2`, `F5`; esquivas `E1–E4`; provocações `P1–P5`; anulações `A1–A4`.
  - **Estágio 2 (65% – 30% PV) — A Dança das Almas Perdidas:** Desbloqueia `O3`, `O5`, `F3`, `F4` e as anulações táticas `A5` (Guarda) e `A6` (Disparo). Invoca `4 + N` (máx. 12) **Almas Vagas** (`wandering_soul_wisp`, **6 PV** cada, ligadas visualmente ao broche) a cada ciclo de Colheita. Teto de cura travado em 65% da vida máxima.
  - **Estágio 3 (30% – 0% PV) — Fúria da Nevasca Eterna:** Velocidade base sobe para `0.32`; desbloqueia `F6`; preparo de F1–F6 reduzido em 15%; `O1` dispara 8 fragmentos de gelo ao final do giro; `A1` resolve em 8 ticks (com aviso instantâneo em `T0`). Teto de cura travado em 30% da vida máxima.

- **[BOSS] Persistência, Leash, Anti-cerco e Anti-pillaring:**
  - **Persistência:** Não despawna em combate; se não houver jogadores válidos em 64 blocos por 600 ticks, afunda em névoa e encerra o encontro.
  - **Leash:** 48 blocos do ponto de despertar; retorna com Passo Espectral ao centro se for atraído além do limite.
  - **Anti-cerco em 2 níveis:**
    1. *Nível 1 (2 jogadores a < 4 blocos):* eleva prioridade de `O1` (`whirlwind`) ou `E4` (`scythe_vault`), respeitando cooldown.
    2. *Nível 2 (3+ jogadores a < 6 blocos por ≥ 10 ticks):* anuncia punição e prioriza `F4` (cone) ou `O1`.
  - **Anti-pillaring ordenado (sem respostas simultâneas):**
    1. *Primário:* se o alvo está ≥ 3 blocos acima do solo por ≥ 30 ticks, usa **M5 (`mist_ascent`)**, subindo com aviso de 30 ticks até a altura do jogador (máx. +6 blocos sobre o solo, respeitando teto e colisão).
    2. *Secundário:* se o alvo permanecer elevado à distância, dispara **`O4` (`frost_wave`)** em trajetória fixa.
    3. *Terciário:* Passo Espectral apenas se o caminho terrestre/aéreo for inválido, fora de janela de dano.

### 1.5 Drops, Sons, Textos e Rede
- **Drops:**
  - Comuns: Fragmento de Gelo Antigo (`ancient_ice_shard`, 6–12, 100%), Essência de Alma Congelada (`frozen_soul_essence`, 2–4, 100%).
  - Arma lendária: **Foice das Almas Vagas** (`soul_reaper_scythe`, 100%) — dano 10, vel. 1.2; ataque carregado em arco de 360°; habilidade ativa *Segada Espiritual* (shift + botão direito, recarga 300t: avanço de 3b com roubo de 2 PV por inimigo atingido, máx. 6 PV).
  - Troféu: **Broche da Espiral Frígida** (`frost_spiral_brooch`, 100%) — no inventário/mão secundária concede imunidade a congelamento em neve fofa e +20% de velocidade sobre gelo/neve.
- **Sons (caráter, mapeados para eventos vanilla em `ModSounds` / D-010):**
  - Ambiente: vento gélido grave com crepitar de tecido; Passos (`staff_stride`): batida oca de madeira congelada; Blink ofensivo: chiado de pressão e colapso seco de ar; Dissolução E3: sopro suave de flocos; Giros/Cortes: zunido metálico pesado; Colheita: ressonância espectral e sinos distantes; Anulações: estalo vítreo seco; Hurt: ranger de geleira; Morte: fratura de gelo ancestral seguida de suspiro.
- **Textos:** Capítulo do Bestiário (6+ páginas com gravuras geradas por script) e 4 conquistas em `advancement/bosses/`.
- **Arquitetura de Sincronização de Rede (Corrigida):**
  - **Estado autoritativo (`SynchedEntityData`):** `DATA_STAGE` (int), `DATA_LOCOMOTION` (byte: M1–M5), `DATA_ACTION_ID` (int: ID da ação atual de L2/L4), `DATA_ACTION_TICK` (int), `DATA_REACT_ID` (byte: L3 `hurt` / `A4`), `DATA_LOCKED_TARGET_ID` (int). Garante que qualquer cliente receba a pose exata sem depender de pacotes perdidos e sem sofrer com o limite `byte` com sinal.
  - **Eventos pontuais (`broadcastEntityEvent`, faixa `(byte) 180` a `(byte) 199`):** usados apenas para gatilhos instantâneos de partículas, som e tremor de câmera (180 = início da metamorfose, 181 = fumaça de blink, 182 = impacto de abertura, 183 = pulso da Colheita, 184 = onda de geada, 185 = neve da dissolução E3, 186 = feixe de anulação A1–A3, 187 = estalo A4, 188 = marca de escudo A5, 189 = reflexo A6, 190 = transição Fase 2, 191 = transição Fase 3, 192 = impacto pesado F4, 193 = quebra da foice na morte).

### 1.6 Resumo do Rig e Animação
- **Solução estrutural do rig (55+ juntas):**
  - `root` → `pelvis` (`lower_shroud` com 4 painéis × 3 segmentos) → `abdomen` → `torso` → `chest` (`brooch_anchor`, `upper_shroud` com 4 painéis × 3 segmentos, `neck` → `head_core` → `cowl` com 5 abas + olhos, `left/right_clavicle` → `shoulder` → `arm_upper` → `elbow` → `forearm` → `wrist` → `hand` com 5 dedos × 2 falanges, e **`scythe_root`**).
  - **Por que `scythe_root` fica sob `chest` (com solver IK de 2 mãos):** Sendo filho de `chest` (o mesmo pai das clavículas), a foice herda automaticamente toda inclinação, respiração e torção da coluna (`pelvis → abdomen → torso → chest`), mantendo `grip_upper` e `grip_lower` sempre no mesmo espaço coordenado dos braços e eliminando qualquer deslizamento das mãos.
- **Tabelas completas (Parte A e regras da Parte B):** ver [soul_reaper_animation.md](soul_reaper_animation.md).

---

## 2. Estado de implementação

| Fatia | Estado | Data | Comando de aceite e resultado |
|---|---|---|---|
| 0. Ficha revisada | concluída | 2026-11-08 | Ficha principal, catálogo de animações/rig e sistemas revisados e unificados sem contradições. |
| 1. Fundação | concluída | 2026-11-08 | `.\gradlew.bat compileJava` (OK) e `.\gradlew.bat runSoulReaperTest` (3/3 testes de `SoulReaperGameTests` + 1 base = 4/4 passed em 13s, executado pelo agente). |
| 2. Visual e rigging | concluída | 2026-11-08 | `python tools/generate_soul_reaper_assets.py` (OK), `python tools/generate_boss_bar_soul_reaper.py` (OK), `.\gradlew.bat -I tools/soul-reaper-sketches.gradle exportSoulReaperSketchModel` (OK), `python tools/generate_soul_reaper_sketches.py` (OK) e `.\gradlew.bat runSoulReaperTest` (4/4 testes de `SoulReaperGameTests` + 1 base = 5/5 passed). |
| 2b. Revisão visual (humano) | concluída | 2026-11-08 | Aprovada pelo usuário ("ok") após inspeção das pranchas de poses, barra temática e gravuras. |
| 3. IA e combate | concluída | 2026-11-08 | `.\gradlew.bat compileJava` (OK) e `.\gradlew.bat runSoulReaperTest` (11/11 testes de `SoulReaperGameTests` + 1 base = 12/12 passed em 12s, executado pelo agente). |
| 4. Polimento e docs | concluída | 2026-11-08 | `.\gradlew.bat build` (OK), 6 rodadas consecutivas de `.\gradlew.bat runSoulReaperTest` (12/12 testes de `SoulReaperGameTests` + 1 base = 13/13 passed em todas as 6 rodadas) e regressão `.\gradlew.bat runLastGiftCollectorTest` (18/18 passed). |
| 5. Relatório | pendente | | |

- **Próximo passo:** Executar a **Fatia 5 (Relatório final de verificação)** conforme `docs/prompts/relatorio.md` ou validar em sessão interativa de `.\gradlew.bat runClient`.
- **Decisões tomadas e motivos:**
  - Unificação dos três documentos (`soul_reaper.md`, `soul_reaper_animation.md`, `soul_reaper_systems.md`) com tabela única de 11 ataques, 4 esquivas, 5 provocações, 6 anulações e 5 locomoções.
  - `scythe_root` ancorado em `chest` com dois pontos de empunhadura (`grip_upper`, `grip_lower`) e solver analítico para manter as duas mãos presas ao cabo em toda torção de tronco.
  - Sincronização de ações via `SynchedEntityData` e eventos de rede restritos a `(byte) 180–199` para efeitos pontuais.
  - Teto anti-instakill de 65% aplicado **pós-armadura** (`LivingDamageEvent.Pre`), preservando a letalidade contra jogadores endgame sem permitir instakill em jogadores sem armadura.
- **Pendências e riscos:**
  - Calibrar os ângulos (Parte B) na Fatia 2 primeiro em 5 poses-piloto (`idle_1`, `glide`, `F1`, `F4`, `death`) antes de gerar os demais clipes.
