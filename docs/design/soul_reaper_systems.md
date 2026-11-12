# Ceifeiro das Almas Vagas: Sistemas de Dificuldade, Anti-Instakill, Anulação e Voz (`soul_reaper`)

Complemento normativo de [soul_reaper.md](soul_reaper.md) e [soul_reaper_animation.md](soul_reaper_animation.md). Define a matemática multiplayer, as 7 regras anti-instakill, o rastreamento de procedência das anulações (A1–A6), a inteligência tática e o catálogo de diálogos.

---

## 1. Dificuldade extrema, escala multiplayer e inteligência tática

### 1.1 Contagem de jogadores efetivos (`N` e `N_peak`)
- **`N` atual:** jogadores vivos, em modo sobrevivência, num raio de 48 blocos do boss, recalculado a cada 20 ticks (espectadores, criativos e mortos não contam).
- **`N_peak` monotônico:** o boss guarda `N_peak = max(N_peak, N)` durante toda a luta. A escala **sobe** quando novos jogadores entram no combate, mas **nunca reduz** se jogadores morrerem ou fugirem.
- **Preservação de percentual de vida:** quando `N_peak` aumenta no meio da luta, a vida máxima sobe pela fórmula abaixo e a vida atual é reescalada mantendo exatamente o mesmo percentual (`healthRatio`), sem curar nem regredir estágios.

### 1.2 Fórmulas de escalonamento (usando `N_peak`, limite `1..10`)

| Atributo | Fórmula | Limite (N_peak = 10) |
|---|---|---|
| Vida máxima | `350 × (1 + 0,6 × (N_peak − 1))` | `2240` PV |
| Dano nominal por golpe | `base × (1 + 0,03 × (N_peak − 1))` | `+27%` |
| Recuperação dos ataques | `round(base × (1 − 0,03 × (N_peak − 1)))` | mín. `70%` do base (sem remover janelas puníveis) |
| Teto de cura da Colheita (`O3`) por uso | `24 + 6 × (N_peak − 1)` | máx. `60` PV (respeitando teto da fase: 65% no Est. 2, 30% no Est. 3) |
| Almas Vagas invocadas no Estágio 2 | `4 + N_peak` | máx. `12` (`6 PV` cada) |

### 1.3 Proteção contra burst e Enrage em etapas
- **Teto anti-burst (dano recebido pelo boss):** nenhum golpe isolado pode retirar mais de **4% da vida máxima atual** do boss (`LivingIncomingDamageEvent`).
- **Enrage progressivo (após 7200 ticks / 6 min de combate ativo):**
  1. *Etapa 1 (6 min):* aumenta a frequência de ataques do Estágio 3 e reduz o intervalo entre escolhas de IA em 15%, mantendo todas as telegrafias intactas.
  2. *Etapa 2 (8 min):* +10% na velocidade de movimento e +10% no dano nominal (ainda sujeito às regras anti-instakill da seção 2).
  3. *Etapa 3 (10 min+):* preparação dos ataques reduzida em mais 10% (piso de 75% da duração original para continuar legível). Reinicia apenas se o boss ficar 600 ticks fora de combate sem alvos na arena.

### 1.4 Comportamento multiplayer e memória de hábitos
- **Placar de ameaça e foco travado (`lockedTargetId`):** mantém os 3 jogadores de maior ameaça (dano nos últimos 100t + proximidade). Alterna o alvo principal a cada 200t entre o top 3, mas **trava `lockedTargetId` no início do preparo de cada ataque** (nunca troca de alvo no meio da execução).
- **Memória curta de hábitos (janela de 80 ticks por jogador):**
  - *Hábito de bloqueio (≥ 3 bloqueios em 80t):* habilita **A5 (`shield_tithe`)** no Estágio 2+.
  - *Hábito de disparo (≥ 3 projéteis em 80t):* habilita **A6 (`arrow_reversal`)** ou **E3 (`snow_dissolve`)**.
  - *Hábito de distância (> 12b constante):* prioriza **M4 (`predator_run`)**, **O2 (`blink_strike`)** ou **P1 (`beckon`)**.
- **Ecos da Foice (`N_peak ≥ 4`):** a cada 400 ticks, cria `min(N_peak − 2, 6)` ecos translúcidos em jogadores espaçados que não estejam sob outro ataque de área. O círculo azul no solo pulsa 2 vezes (24 ticks de aviso) antes do eco repetir o último golpe pesado com 50% do dano.

---

## 2. Sete regras anti-instakill (obrigatórias e verificadas por GameTest)

O boss é extremamente punitivo a erros acumulados, mas **nenhum golpe ou sequência isolada pode matar instantaneamente um jogador saudável**.

1. **Teto pós-armadura de 65% por golpe (`LivingDamageEvent.Pre`):**
   - O dano final efetivo (já descontadas armadura, resistência e encantamentos) de qualquer impacto único do boss, eco ou projétil é limitado a **`0.65 × maxHealth`** do alvo (ex.: máx. `13.0` PV perdidos num jogador de 20 PV, esteja ele sem armadura ou de Netherite).
   - *Por que pós-armadura:* evita instakill em jogadores com armadura leve e ao mesmo tempo preserva a diferença real entre golpes pesados (`F4 = 20`, `O1 = 18`) e rápidos (`F2 = 12`, `F5 = 11`) contra jogadores de armadura completa.
2. **Janela de misericórdia de 6 ticks consciente de multi-hit:**
   - Após receber um impacto do boss, o alvo fica imune por **6 ticks** a impactos duplicados do mesmo pulso ou de fontes paralelas (ex.: boss + Eco da Foice no mesmo instante).
   - Golpes multi-hit intencionais (`O1`, `O5`, `F3`, `F6`) identificam cada acerto por `(attackSequenceId, pulseIndex)`: pulsos distintos da mesma sequência (espaçados em ≥ 7 ticks, como `3, 10, 20` em `F6` e `4, 20` em `F3`) nunca são cancelados indevidamente pela janela de misericórdia.
3. **Regra do Fôlego (proteção de sequência a ≥ 80% PV):**
   - Se o jogador tinha **`health ≥ 0.80 × maxHealth`** no tick em que o boss iniciou o preparo de uma sequência (`attackSequenceId`), nenhum impacto daquela sequência pode reduzir a vida desse jogador abaixo de **`1.0` PV**. Uma vez ativada, a proteção só se renova quando o jogador voltar a ≥ 80% de vida.
4. **Teto da Colheita de Almas (`O3`):**
   - O dreno contínuo causa no máximo `60% da vida máxima` do alvo ao longo dos 4 pulsos de uma conjuração e nunca reduz o alvo abaixo de `1.0` PV no último pulso se ele iniciou a canalização acima de 50% de vida.
5. **Dano exclusivo em ataques compostos (`F4`):**
   - Em `F4` (`gravedigger_slam`), o alvo atingido na zona direta (`5.0b`, 20 dano) não recebe o dano do cone (`8.0b`, 8 dano) no mesmo golpe.
6. **Repulsão segura contra perigos ambientais:**
   - A componente vertical de repulsão é limitada a `1.2` blocos; se o vetor horizontal empurrar o alvo para lava ou queda livre > 8 blocos num raio de 2 blocos, a repulsão horizontal é zerada (mantendo o dano).
7. **Sem morte por cobrança e Totem intacto:**
   - A cobrança de cura instantânea (seção 3) nunca reduz o jogador abaixo de **`2.0` PV**, e o **Totem da Imortalidade** funciona normalmente sem jamais ser anulado.

---

## 3. Sistema de Anulações (A1–A6) e Rastreamento de Procedência

### 3.1 Rastreamento obrigatório de procedência (A1–A3)
Para cumprir a regra de **remover apenas os efeitos concedidos pelo item recém-consumido** (preservando buffs preexistentes de beacon, armaduras/itens do mod ou outras fontes):
1. Em `LivingEntityUseItemEvent.Start` (ou snapshot pré-consumo), o servidor grava o mapa `preExistingEffects` do jogador (`MobEffect → {amplifier, duration}`).
2. Em `LivingEntityUseItemEvent.Finish` (para `PotionItem` bebido, `GoldenAppleItem` e `EnchantedGoldenAppleItem` a até 48 blocos do boss), o servidor compara os efeitos atuais com `preExistingEffects` e registra apenas o delta em `PendingNullification(playerId, T0, addedOrUpgradedEffects, instantHealAmount)`.
3. Na resolução (`T0 + 20` nos Estágios 1–2; `T0 + 8` no Estágio 3):
   - Para cada efeito em `addedOrUpgradedEffects`: se o efeito não existia em `preExistingEffects`, é removido; se já existia com amplificador/duração menor (ex.: Regeneração I de um beacon), é **restaurado para a duração/amplificador anterior** em vez de ser apagado por completo.
   - Se o jogador ganhou vida instantânea (`instantHealAmount > 0`), o boss aplica a **Cobrança**: dano mágico não letal igual a `instantHealAmount`, respeitando o piso absoluto de **`2.0` PV**.
4. Poções arremessáveis/persistentes permanecem fora do escopo de A1 (permitindo jogo cooperativo de suporte com menor potência).

### 3.2 Sincronização estrita entre aviso visual (`T0`) e resolução mecânica
- **Regra de ouro:** nenhum efeito é removido e nenhuma cobrança é aplicada sem telegrafia prévia visível a partir de `T0`.
- Se o boss está livre (em L0 ou provocação), inicia imediatamente a animação completa em L2 (`A1 vial_silence`, `A2 apple_toll` ou `A3 greater_toll`, respeitando o cooldown global de 200t para animações completas).
- Se o boss está executando um ataque/esquiva em L2 ou se o cooldown de animação completa está ativo, ele dispara **imediatamente em `T0` a reação sobreposta `A4 flick_snuff` na camada L3** (virada de capuz + estalo de dedos da mão esquerda + feixe visual no jogador), garantindo que o jogador sempre veja o aviso 20 ticks (ou 8 ticks no Estágio 3) antes da resolução!

### 3.3 Anulações táticas de combate (Estágio 2+)
- **A5 `shield_tithe` — Anulação da Guarda (24t, marca no tick 16, cooldown 240t por jogador):**
  - *Gatilho:* mesmo jogador bloqueia ≥ 3 golpes do boss em 80 ticks.
  - *Efeito:* no tick 16, se o jogador **continuar com o escudo erguido** no alcance frontal (`4.5b`), aplica `disableShield` por 30 ticks sem causar dano nem consumir durabilidade extra.
  - *Contra-jogada:* baixar o escudo e dar passo lateral durante os 16 ticks de preparo faz o boss errar a marca.
- **A6 `arrow_reversal` — Anulação do Disparo (30t, janela nos ticks 20–30, cooldown 300t):**
  - *Gatilho:* jogador dispara ≥ 3 projéteis contra o boss em 80 ticks (quando E3 está em recarga).
  - *Efeito:* abre barreira frontal por 10 ticks (ticks 20–30); o **primeiro projétil** que atingir o arco frontal de 120° é refletido de volta na direção do atirador com velocidade `1.2×` e dano limitado pela regra anti-instakill.
  - *Contra-jogada:* cessar fogo durante o brilho azul da lâmina (ticks 0–20) ou atacar pelos flancos.

---

## 4. Dinâmica de Arena e Almas Vagas

- **Almas Vagas (`wandering_soul_wisp`, Estágio 2+):**
  - Possuem **6 PV** e recebem no máximo 1 acerto de dano em área a cada 10 ticks (evitando que morram instantaneamente para um único sweep acidental em grupo, exigindo foco deliberado).
  - Cada alma mantém um fio azul tênue ligado ao `brooch_anchor` do Ceifeiro. Destruir uma alma corta o fio com um estalo vítreo e reduz em 6 PV o potencial de cura da próxima Colheita de Almas.
- **Trilhas de Geada e Setores de Nevasca:**
  - `O4` (`frost_wave`) e `O5` (`reaping_dash`) deixam uma trilha visual de geada no chão por 80 ticks que aplica apenas Lentidão I a quem pisar nela.
  - No Estágio 3, a nevasca pulsa em rajadas direcionais mantendo a silhueta de 5,4m, o broche e o brilho azul da lâmina sempre visíveis a até 24 blocos.

---

## 5. Direção de Voz e Catálogo de Diálogos

- **Regras anti-spam:** Cooldown global mínimo de **100 ticks** entre linhas de chat; cooldown de **600 ticks** para a mesma categoria; em consumo simultâneo por vários jogadores, envia uma única linha coletiva. Chaves no formato `entity.codenamecoraline.soul_reaper.line.{categoria}.{n}` (`pt_br` e `en_us`).

| Categoria | Gatilho | Linhas em `pt_br` | Linhas em `en_us` |
|---|---|---|---|
| `awaken` | Fim da cinemática de entrada | 1. "A neve já sabe o seu nome." / 2. "Você feriu a única coisa pura deste inverno." / 3. "Não há pressa. O frio sempre espera." | 1. "The snow already knows your name." / 2. "You struck the only pure thing in this winter." / 3. "There is no haste. The cold always waits." |
| `potion` (A1) | Anulação de poção | 1. "Frasco vazio, esperança vazia." / 2. "Engula o que quiser. O inverno desfaz tudo." / 3. "Essa cor não combina com a neve." | 1. "Empty vial, empty hope." / 2. "Swallow what you wish. Winter undoes it all." / 3. "That color does not suit the snow." |
| `apple` (A2) | Anulação de maçã dourada | 1. "Ouro não compra mais um inverno." / 2. "Doce... e inútil." / 3. "Mastigue devagar. É a última." | 1. "Gold cannot buy another winter." / 2. "Sweet... and useless." / 3. "Chew slowly. It is your last." |
| `enchanted_apple` (A3) | Anulação de maçã encantada | 1. "Até o brilho encantado se curva ao frio." / 2. "Isso sim era um presente. Vou guardar." / 3. "Quanta luz para tão pouca vida." | 1. "Even enchanted gleam bows to the frost." / 2. "Now that was a gift. I shall keep it." / 3. "So much light for so little life." |
| `multi_consume` | ≥ 2 jogadores anulados juntos | 1. "Quantos de vocês bebem de uma vez? Eu conto todos." | 1. "How many of you drink at once? I count every one." |
| `toll_spared` | Cobrança trava em 2 PV | 1. "Ainda não. Você ainda não me deve o suficiente." | 1. "Not yet. You do not owe me enough yet." |
| `shield` (A5) | Anulação da Guarda | 1. "Você se esconde atrás de uma promessa de ferro." / 2. "Erga-o outra vez. Quero ouvir quando partir." / 3. "A guarda também se cansa." | 1. "You hide behind a promise of iron." / 2. "Raise it again. I want to hear it shatter." / 3. "Even a guard grows weary." |
| `arrow` (A6) | Anulação do Disparo | 1. "A distância não é abrigo." / 2. "Toda sentença retorna ao seu autor." / 3. "Não dispare contra o inverno." | 1. "Distance is no shelter." / 2. "Every sentence returns to its author." / 3. "Do not loose arrows against the winter." |
| `dodge` (E1–E4) | Esquiva bem-sucedida (rara) | 1. "Tão perto. Tão lento." / 2. "Você golpeia onde eu estava." / 3. "A neve não guarda forma." | 1. "So close. So slow." / 2. "You strike where I used to be." / 3. "Snow holds no shape." |
| `phase` | Entrada nos Estágios 2 e 3 | Est. 2: "Elas ainda se lembram de vocês." / Est. 3: "Chega de cerimônia. A última neve não derrete." | Stage 2: "They still remember you." / Stage 3: "Enough ceremony. The final snow never melts." |
