# Salamandra de Enxofre (`brimstone_salamander`)

> Documento vivo de especificação e estado técnico da Salamandra de Enxofre.
> Concebido para a dimensão do Nether no ecossistema de folclore feérico tradicional do CodenameCoraline.

## 1. Ficha

### 1.1 Identificação e Lore
- **ID / nome interno:** `brimstone_salamander`; **Nome pt_br:** `Salamandra de Enxofre`; **Nome en_us:** `Brimstone Salamander`.
- **Perfil:** `hostil` (ver tabela de escopo e testes em `docs/PROCESSO_MOB.md`).
- **Nome do pacote (`pkg`):** `salamander` (`com.codenamecoraline.entity.salamander` e `client.salamander`).
- **Mob de referência (estrutura de código):** `Lithophage` (para estados de combate terrestre, projétil balístico, recargas anti-whiff e telegrafias visuais).
- **Lore e Origem Mítica (4–8 linhas):**
  Nos bestiários alquímicos e lendas da Corte Sombria (Unseelie), a salamandra é a criatura primordial nascida das chamas puras, capaz de se banquetear onde mortais viram cinzas. No Nether de Coraline, as Salamandras de Enxofre rastejam pelas margens de basalto e pelos lagos incandescentes, agindo como predadores territoriais que condensam o calor da dimensão em seus ventres. Seu papo inflável secreta um alcatrão sulfuroso viscoso que não só incendeia, mas sufoca o movimento e queima com a fúria das forjas feéricas ancestrais.
- **Assinatura de Silhueta e Proporções (OBRIGATÓRIO):**
  Rastejador quadrúpede longo e de perfil baixo ("sprawled posture"), com ventre próximo ao solo e patas projetadas para os lados com garras curvas de basalto. A cabeça é triangular, achatada e larga, equipada com uma papada/garganta de fole inflável emissiva. O dorso apresenta uma crista segmentada assimétrica de placas de rocha vulcânica pontiaguda, rompendo a simetria clássica de répteis. A cauda é longa, espessa e comprimida verticalmente, conferindo silhueta de anfíbio nadador de lava.
- **Referências visuais:**
  Salamandras de fogo reais (*Salamandra salamandra*), salamandras alquímicas medievais, blocos de basalto polido e netherrack. Paleta: carapaça em basalto carvão (`#1c1d21`) com placas de cinza vulcânico (`#383a42`), veios de pó de enxofre amarelo-ácido (`#e3c434` / `#f2e06b`), papo e fendas de lava em gradiente incandescente alaranjado e rubro (`#ff5722` / `#ff9800`).
- **Dimensões:** altura `0.85` blocos, largura da colisão `1.3` blocos, comprimento da colisão `2.2` blocos, altura dos olhos `0.55` blocos.
- **Resolução do atlas / texel:** `128×128` px (densidade uniforme de 16 px/bloco; 26 caixas mapeadas sem sobreposição de UV).

### 1.2 Atributos e Ecologia Básica
- **Atributos:** vida `44.0`, armadura `8.0` (+ resistência de armadura `2.0`), velocidade `0.23` (terrestre) / `0.38` (em lava), dano base `6.0`, resistência a repulsão `0.65`, alcance de visão `24.0`, XP `10`.
- **Imunidades e afinidades:** imunidade total a fogo e lava (`fireImmune`), navega e não se afoga em lava (`canBreatheUnderwater` / fluidos). Vulnerabilidade feérica: sofre 200% de dano por congelamento/frio extremo; água no Overworld causa choque térmico e 4.0 de dano por segundo. Redução de 60% no dano de queda.
- **Onde aparece:** Deltas de Basalto (`Basalt Deltas`), Ermos do Nether (`Nether Wastes`) e margens de lava da Floresta Carmesim (`Crimson Forest`); gerado sobre blocos de `basalt`, `blackstone`, `netherrack` ou adjacente a fontes de lava entre as camadas Y 24 e Y 48; raridade peso 25, bandos pequenos de 1 a 3 indivíduos.
- **Limite de spawn:** máximo de 3 por chunk; **dificuldade pacífica:** não spawna.
- **Tags de alvo:** `minecraft:player`, `minecraft:iron_golem`, pequenos striders ou magma cubes quando em modo caça.

### 1.3 Matriz de Comportamento e Interações
- **Relação com a própria espécie:** territorialista mas não canibal; não se atacam, mantêm distância mínima de 4 blocos entre si durante repouso.
- **Mortos-vivos:** indiferente a esqueletos murchos e zumbis piglins a menos que atacada diretamente.
- **Aldeões e golems:** hostil contra golens de ferro que se aproximem de margens de lava.
- **Animais pacíficos:** caça filhotes de Striders que entrem em seu território de margem.
- **Outros mobs do mod:** neutro em relação a Basilisco e Litófago; ignora o Corvo Feérico.
- **Luz solar vs escuridão:** indiferente (dimensão do Nether).
- **Água e chuva:** entra em pânico se exposta à chuva no Overworld; contato com água causa petrificação de escamas e dano contínuo de choque térmico.
- **Fora do bioma:** se levada para fora do Nether, busca ativamente blocos incandescentes (fogueiras, tochas, magma, lava) para manter seu calor corporal.
- **Rotina fora de combate:** rastejar pelas margens, mergulhar na lava para recarregar o calor do papo, repousar sobre rochas aquecidas.
- **Variações de Repouso (Idle - sorteadas a cada 120–240 ticks):**
  - *Idle 1:* Inflar o papo emissivo soltando um silvo abafado com baforadas de fumaça preta e fagulhas de lava pelas narinas.
  - *Idle 2:* Esticar as patas dianteiras contra o solo, baixar o ventre e ondular a cauda achatada lateralmente.
  - *Idle 3:* Girar a cabeça em 45° farejando o calor ambiente com sua língua incandescente bífida.

### 1.4 Combate, Fases e Dinâmica Multiplayer (`Perfil: hostil`)
- **Foco de combate:** persegue o alvo mais próximo com linha de visão; se atacada à distância por projéteis, retalia imediatamente com o cuspe sulfuroso.
- **Tabela de ataques:**

| Ataque | Alcance (blocos) | Preparo (ticks) | Execução (ticks) | Recuperação (ticks) | Dano | Efeitos/projéteis | Telegrafia | Contra-jogada | Recarga (ticks) |
|---|---|---|---|---|---|---|---|---|---|
| Mordida de Fricção (`SNAP_BITE`) | 0.0–2.2 | 8 | 4 | 12 | 7.0 | Incendeia alvo por 40 ticks | Pescoço recua em 'S', mandíbula abre 40° com brilho âmbar interno e estalo seco | Passo para trás de 1 bloco ou bloqueio com escudo (bloqueio atordoa por 10 ticks) | 35 |
| Cuspe Sulfuroso (`SULFUR_SPIT`) | 4.5–16.0 | 16 | 6 | 18 | 5.0 | Dispara projétil `BrimstoneSpit`; no impacto cria Poça de Alcatrão Ardente (2×2 blocos, 100 ticks) com Lentidão II e fogo | Papo infla ao máximo emitindo pulso luminoso amarelo e sibilo de vapor comprimido | Corrida lateral contínua para esquivar da parábola do projétil | 90 |
| Chicotada de Cauda (`TAIL_WHIP`) | 0.0–2.5 (arco traseiro/lateral) | 10 | 5 | 14 | 6.0 | Empurrão horizontal de 1.2 blocos e dano de impacto | Finca patas dianteiras, ergue e racha a cauda em veios de fogo vivo | Pular no momento do impacto ou posicionar-se frontalmente | 60 |

- **Regras de combate anti-whiff e anti-pillaring:**
  - *Anti-whiff:* durante os 8 ticks de preparo da Mordida, se o jogador recuar além de 2.6 blocos, a salamandra cancela o golpe no tick 7 e entra em recarga curta (15 ticks) sem travar no ar.
  - *Anti-pillaring:* se o alvo estiver 2 a 5 blocos acima do plano da salamandra (subindo pilares ou em saliências de basalto), ela desativa ataques corpo a corpo e dispara o `SULFUR_SPIT` em ângulo parabólico elevado mirando na base ou nos pés do jogador para cobrir a superfície com alcatrão e chamas.
  - *Anti-cerco:* se mais de um jogador cercar a criatura a menos de 2 blocos, a prioridade da Chicotada de Cauda sobe para repelir flanqueadores.

### 1.5 Drops, Sons e Textos
- **Drops:**
  - Comuns: 1–2 `minecraft:magma_cream` (85%), 1–3 `minecraft:coal` (65%).
  - Especial (novo item): 1 `codenamecoraline:brimstone_scale` (**Escama de Enxofre**, 35% de chance) — placa rochosa resistente usada para infusões alquímicas de proteção térmica feérica e reforço de ferramentas.
- **Sons (caráter sonoro):**
  - *Ambiente:* respiração pesada e silvo borbulhante de forja abafada e magma em ebulição.
  - *Hurt:* estalo rascante de pedra vulcânica trincando sob choque térmico e sibilo agudo.
  - *Morte:* estertor grave e prolongado de vapor esvaindo de um fole com estrondo de carapaça rochosa colapsando.
  - *Passos:* unhas duras raspando basalto e som abafado de ventre escamoso arrastando sobre pedra.
  - *Ataque Mordida:* estalo ósseo seco e contundente de mandíbulas minerais se chocando.
  - *Ataque Cuspe:* golfada sob forte pressão gasosa e silvo de alcatrão queimando no ar.
  - *Ataque Cauda:* zunido pesado rasgando o ar seguido por estalo de chicote quente.
- **Textos:**
  - Bestiário: capítulo de 6 páginas (`Bestiary.BRIMSTONE_SALAMANDER`) detalhando as fábulas alquímicas da salamandra, seu papel na ecologia de magma do Nether e ilustrações anatômicas.
  - Conquistas em cadeia:
    1. *bosses/find_brimstone_salamander*: "Espírito das Forjas Primordiais" (Encontre uma Salamandra de Enxofre viva no Nether).
    2. *bosses/brimstone_scale*: "Couro que não Arde" (Obtenha uma Escama de Enxofre intacta).
- **Faixa de eventos de rede (`broadcastEntityEvent`):** `150–159` (150 = papo inflando, 151 = disparo de cuspe sulfuroso, 152 = chicotada de cauda, 153 = estalo de mandíbula).

### 1.6 Animação (Parte A: Intenção e Tempo)

| Animação | Duração (ticks) | Pose-chave e movimento (em palavras) | Peso e inércia | Tick do impacto |
|---|---|---|---|---|
| `idle_breathe` | 40 | O corpo repousa rente ao solo; o papo na garganta expande e contrai suavemente em pulso térmico; a ponta da cauda oscila 3° para os lados | Respiração pesada e lenta | n/a |
| `idle_throat_puff` | 30 | Eleva a cabeça 15°, o papo estufa ao dobro do volume, solta uma baforada de vapor e volta ao normal | Pulso elástico de fole | n/a |
| `idle_tail_sway` | 35 | Firma as patas dianteiras e ondula a cauda em 'S' completo do início à ponta sobre o chão | Ondulação de réptil | n/a |
| `idle_sniff` | 25 | Gira a cabeça 35° para a esquerda, projeta a língua bífida para fora por 8 ticks e recolhe bruscamente | Movimento rápido e seco | n/a |
| `walk` | 16 | Passo quadrupedal cruzado e rastejante: pata dianteira esquerda avança com traseira direita; tronco ondula lateralmente acompanhando o ciclo | Marcha pesada e sinuosa | n/a |
| `swim_lava` | 14 | Patas recolhidas contra os flancos do corpo; ondulação contínua e fluida do tronco e da cauda impulsionando a criatura pela lava | Hidrodinâmica e ágil | n/a |
| `snap_bite_prep` | 8 | Puxa o pescoço e a cabeça para trás em 'S', finca as quatro patas no chão e abre a mandíbula inferior em 40° revelando o interior incandescente | Tensão concentrada no pescoço | n/a |
| `snap_bite_exec` | 4 | Projeta a cabeça e o pescoço bruscamente para a frente em linha reta, fechando as mandíbulas no fim do percurso com impacto | Avanço balístico seco | 2 |
| `snap_bite_recovery` | 12 | Cabeça recua suavemente para a postura neutra enquanto o tronco reajusta o centro de gravidade sobre as quatro patas | Desaceleração controlada | n/a |
| `sulfur_spit_prep` | 16 | Cabeça se ergue 25°, o papo infla progressivamente até o limite emitindo luz intensa, patas dianteiras travam | Compressão de fole sob pressão | n/a |
| `sulfur_spit_exec` | 6 | Espasmo no pescoço impulsionando a cabeça para a frente em arco, a boca abre e o glóbulo de alcatrão é expelido sob pressão | Coice visível no tronco | 2 |
| `sulfur_spit_recovery` | 18 | O papo murcha completamente soltando filetes de fumaça, a cabeça baixa e o corpo volta à altura padrão | Exaustão e alívio | n/a |
| `tail_whip_prep` | 10 | Torção de 30° no tronco para a direita, patas esquerdas fincam no chão e cauda ergue-se do solo flexionando em chicote | Mola carregada | n/a |
| `tail_whip_exec` | 5 | Giro súbito da cauda varrendo um arco horizontal de 160° em direção ao lado oposto na altura do solo | Chicote rápido de alta inércia | 3 |
| `tail_whip_recovery` | 14 | A cauda freia o movimento arrastando a ponta no solo e retorna à posição central alinhada ao tronco | Fricção e recuperação de postura | n/a |
| `hurt` | 6 | Tranco para trás, cabeça sobe em espasmo e o corpo se contrai recolhendo as patas | Espasmo defensivo | n/a |
| `death` | 24 | Perda total de sustentação nas patas, o corpo despenca no chão, a cauda relaxa esticada e a boca abre lentamente liberando o último brilho térmico | Colapso inerte | n/a |

- **Hierarquia de Juntas do Modelo (`BrimstoneSalamanderModel`):**
  - `root` (base no chão, centrado entre as patas)
    - `body` (tronco principal rebaixado, pivô central)
      - `dorsal_ridge` (crista assimétrica com 5 placas pontiagudas de basalto)
      - `throat_sac` (papo de fole emissivo na parte inferior do pescoço/peito)
      - `neck` (articulação do pescoço, flexão horizontal e vertical)
        - `head` (crânio triangular achatado)
          - `upper_jaw` (focinho e narinas)
          - `lower_jaw` (mandíbula inferior móvel articulada na base do crânio)
      - `left_front_leg` > `left_front_paw` (articulação em cotovelo aberto lateral)
      - `right_front_leg` > `right_front_paw`
      - `left_hind_leg` > `left_hind_paw` (articulação em joelho voltado para trás)
      - `right_hind_leg` > `right_hind_paw`
      - `tail_base` (base muscular da cauda)
        - `tail_mid` (segmento intermediário flexível)
          - `tail_tip` (ponta afilada e achatada verticalmente)

### 1.6b Animação (Parte B: Ângulos e Curvas Reais de `BrimstoneSalamanderAnimations.java`)

| Animação | Junta | Eixo / Canal | Inicial → Pico → Final (graus / px / escala) | Curva | Estado |
|---|---|---|---|---|---|
| `idle_breathe` (2.00s loop) | `body`, `throat_sac`, `tail_base`, `tail_tip` | Pos Y / Scale / Rot X/Y | `body` Pos Y `0 → -0.3 → 0`; `throat_sac` Scale `(1,1,1) → (1.18,1.28,1.15) → (1,1,1)`, Rot X `0° → +8° → 0°`; `tail_base` Y `±2.5°`; `tail_tip` Y `∓3.5°` | `CATMULLROM` | aprovado |
| `idle_throat_puff` (1.50s) | `neck`, `head`, `throat_sac`, `lower_jaw` | Scale / Rot X | `neck` X `0° → -15° → -12° → 0°`; `head` X `0° → -10° → -6° → 0°`; `throat_sac` Scale `(1,1,1) → (1.52,1.95,1.45) → (0.92,0.85,0.92) → (1,1,1)`, Rot X `0° → +18° → -4° → 0°`; `lower_jaw` X `0° → +18° → 0°` | `CATMULLROM` / `LINEAR` | aprovado |
| `idle_tail_sway` (1.75s) | `body`, `tail_base`, `tail_mid`, `tail_tip` | Rot Y | `body` Y `±6°`; `tail_base` Y `+24° → -24° → 0°`; `tail_mid` Y `-16° → +28° → -28° → 0°`; `tail_tip` Y `-24° → +32° → -32° → 0°` | `CATMULLROM` | aprovado |
| `idle_sniff` (1.25s) | `neck`, `head`, `lower_jaw` | Rot X/Y/Z | `neck` `(0,0,0) → (-6°,+18°,0°) → (-2°,-8°,0°) → (0,0,0)`; `head` `(0,0,0) → (-4°,+21°,-6°) → (-2°,-10°,+4°) → (0,0,0)`; `lower_jaw` X `0° → +24° (8 ticks) → 0°` | `LINEAR` | aprovado |
| `walk` (0.80s loop) | `body`, `neck`, `legs`, `tail` | Rot X/Y/Z | `body` Y `±8°`, Z `∓2°`; `neck` Y `∓6°`; `left_front`/`right_hind` Y `-18° ↔ +24°`, Z `-14°`; `right_front`/`left_hind` Y `+18° ↔ -24°`, Z `+14°`; `tail_base/mid/tip` Y `∓16° / ∓22° / ∓26°` | `CATMULLROM` / `LINEAR` | aprovado |
| `swim_lava` (0.70s loop) | `body`, `neck`, `legs`, `tail` | Rot Y/Z | `legs` recolhidas nos flancos (`yRot ∓48°/∓52°, zRot ±12°`); onda senoidal `body` Y `±12°`, `neck` Y `∓10°`, `tail_base` Y `∓24°`, `tail_mid` Y `∓32°`, `tail_tip` Y `∓38°` | `CATMULLROM` | aprovado |
| `snap_bite_prep` (0.40s) | `body`, `neck`, `head`, `lower_jaw`, `front_legs` | Pos Z / Rot X/Y | `body` Pos Z `0 → +1.8`, Rot X `0° → -8°`; `neck` X `0° → -22°`; `head` X `0° → +14°`; `lower_jaw` X `0° → +40°`; `front_legs` Y `0° → ±18°` | `LINEAR` | aprovado |
| `snap_bite_exec` (0.20s, hit 0.10s) | `body`, `neck`, `head`, `lower_jaw`, `front_legs` | Pos Z / Rot X/Y | `body` Pos Z `+1.8 → -2.8 → -2.4`, Rot X `-8° → +10° → +8°`; `neck` X `-22° → +18° → +14°`; `head` X `+14° → -8° → -4°`; `lower_jaw` X `+40° → 0°` | `LINEAR` | aprovado |
| `snap_bite_recovery` (0.60s) | `body`, `neck`, `head`, `front_legs` | Pos Z / Rot X/Y | `body` Pos Z `-2.4 → 0`, Rot X `+8° → 0°`; `neck` X `+14° → 0°`; `head` X `-4° → 0°`; `front_legs` Y `∓20° → 0°` | `CATMULLROM` | aprovado |
| `sulfur_spit_prep` (0.80s) | `body`, `neck`, `head`, `throat_sac`, `lower_jaw` | Scale / Rot X | `body` X `0° → -10°`; `neck` X `0° → -18°`; `head` X `0° → -12°`; `throat_sac` Scale `(1,1,1) → (1.65,2.10,1.55)`, Rot X `0° → +22°`; `lower_jaw` X `0° → +10°` | `CATMULLROM` / `LINEAR` | aprovado |
| `sulfur_spit_exec` (0.30s, hit 0.10s) | `body`, `neck`, `head`, `throat_sac`, `lower_jaw` | Pos Z / Scale / Rot X | `body` Pos Z `0 → +1.4 → +0.8`, Rot X `-10° → +8° → +4°`; `neck` X `-18° → +20° → +10°`; `head` X `-12° → +14° → +6°`; `throat_sac` Scale `(1.65,2.10,1.55) → (0.85,0.75,0.85) → (0.90,0.80,0.90)`; `lower_jaw` X `+10° → +42° → +26°` | `LINEAR` | aprovado |
| `sulfur_spit_recovery` (0.90s) | `body`, `neck`, `head`, `throat_sac`, `lower_jaw` | Pos Z / Scale / Rot X | `body` Pos Z `+0.8 → 0`, Rot X `+4° → 0°`; `neck` X `+10° → +14° → 0°`; `head` X `+6° → +10° → 0°`; `throat_sac` Scale `(0.90,0.80,0.90) → (1,1,1)`; `lower_jaw` X `+26° → +8° → 0°` | `CATMULLROM` | aprovado |
| `tail_whip_prep` (0.50s) | `body`, `neck`, `tail_base`, `tail_mid`, `tail_tip` | Rot X/Y | `body` Y `0° → -28°`; `neck` Y `0° → +18°`; `tail_base` `(0,0,0) → (+12°,-38°,0°)`; `tail_mid` `(0,0,0) → (+10°,-42°,0°)`; `tail_tip` `(0,0,0) → (+8°,-36°,0°)` | `LINEAR` | aprovado |
| `tail_whip_exec` (0.25s, hit 0.15s) | `body`, `neck`, `tail_base`, `tail_mid`, `tail_tip` | Rot X/Y | `body` Y `-28° → +32° → +28°`; `neck` Y `+18° → -20° → -16°`; `tail_base` `(+12°,-38°,0°) → (-4°,+58°,0°) → (-2°,+52°,0°)`; `tail_mid` `(+10°,-42°,0°) → (-4°,+64°,0°) → (-2°,+56°,0°)`; `tail_tip` `(+8°,-36°,0°) → (-2°,+56°,0°) → (0°,+48°,0°)` | `LINEAR` | aprovado |
| `tail_whip_recovery` (0.70s) | `body`, `neck`, `tail_base`, `tail_mid`, `tail_tip` | Rot X/Y | `body` Y `+28° → 0°`; `neck` Y `-16° → 0°`; `tail_base` `(-2°,+52°,0°) → (0,0,0)`; `tail_mid` `(-2°,+56°,0°) → (0,0,0)`; `tail_tip` `(0°,+48°,0°) → (0,0,0)` | `CATMULLROM` | aprovado |
| `hurt` (0.30s) | `body`, `head`, `lower_jaw` | Pos Z / Rot X/Y/Z | `body` Pos Z `0 → +1.2 → 0`, Rot `(0,0,0) → (-14°,0°,+6°) → (0,0,0)`; `head` `(0,0,0) → (-18°,+8°,+10°) → (0,0,0)`; `lower_jaw` X `0° → +24° → 0°` | `LINEAR` | aprovado |
| `death` (1.20s) | `body`, `neck`, `head`, `lower_jaw`, `throat_sac`, `legs`, `tail` | Pos Y / Scale / Rot X/Y/Z | `body` Pos Y `0 → -2.2`, Rot `(0,0,0) → (+6°,-10°,+22°)`; `neck` `(+18°,+16°,0°)`; `head` `(+16°,+14°,-16°)`; `lower_jaw` X `+32°`; `legs` Y `∓28°..∓32°`, Z `∓32°`; `tail` X `-8°..-4°`, Y `+18°..+22°` | `LINEAR` | aprovado |

---

## 2. Estado de implementação

| Fatia | Estado | Data | Comando de aceite e resultado |
|---|---|---|---|
| 0. Ficha revisada | **concluída** | 2026-11-06 | Ficha completa sem `<...>`, valores calibrados contra a escala vanilla e contrato de perfil `hostil` |
| 1. Fundação | **concluída** | 2026-11-06 | `.\gradlew.bat compileJava` (OK) + `.\gradlew.bat runBrimstoneSalamanderTest` (All 3 required tests passed, 2 testes de `brimstone_salamander_test_environment`, BUILD SUCCESSFUL in 20s) |
| 2. Visual e rigging | **concluída** | 2026-11-06 | `.\gradlew.bat compileJava` (OK) + `python tools/generate_brimstone_salamander_assets.py` (26 boxes, 0 UV overlaps on 128x128) + `.\gradlew.bat -I tools/brimstone-salamander-sketches.gradle exportBrimstoneSalamanderSketchModel` (12 poses) + `python tools/generate_brimstone_salamander_sketches.py` (9 poses + pranchas do Bestiário) |
| 2b. Revisão visual (humano) | **concluída** | 2026-11-06 | Poses e texturas aprovadas pelo usuário |
| 3. IA e combate | **concluída** | 2026-11-06 | `.\gradlew.bat compileJava` (OK) + `.\gradlew.bat runBrimstoneSalamanderTest` (All 6 required tests passed in 699.7 ms, 5 testes em `brimstone_salamander_test_environment`, BUILD SUCCESSFUL in 20s) |
| 4. Polimento e docs | **concluída** | 2026-11-06 | `.\gradlew.bat build` (OK) + 4 rodadas de `.\gradlew.bat runBrimstoneSalamanderTest` (All 6 required tests passed em todas as 4 rodadas; 5 testes em `brimstone_salamander_test_environment`); Bestiário de 6 páginas com gravuras, item `brimstone_scale`, 7 sons em `ModSounds`, 2 conquistas e docs atualizados |
| 5. Relatório | **concluída** | 2026-11-06 | Relatório executivo completo gerado; 100% de conformidade com a ficha, 0 pendências de compilação, 4 rodadas consecutivas de GameTests aprovadas |

- **Próximo passo:** Inspeção interativa em `runClient` para validação de áudio, partículas de alcatrão e navegação em lava no jogo real.
- **Decisões tomadas e motivos:**
  - Perfil `hostil`: predador territorial que habita as margens de lava e biomas quentes do Nether (*Basalt Deltas*, *Nether Wastes*, *Crimson Forest*).
  - Vulnerabilidade feérica: suscetibilidade severa a dano de frio/congelamento (200%), enfatizando o contraste mítico entre o fogo primordial feérico e o gelo ancestral.
  - Mob de referência: `Lithophage` para a estrutura de máquina de estados e projétil.
- **Pendências e riscos:**
  - Garantir navegação limpa do pathfinder anfíbio (transição fluida e natural entre solo sólido de basalto e nadar na superfície da lava).

