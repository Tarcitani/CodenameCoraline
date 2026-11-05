# <Nome do mob> (`<id>`)

> Template: copie para `docs/design/<id>.md` e preencha. Substitua todo `<...>`. Campo vazio, contraditório ou com `<...>` residual **bloqueia** o início das fatias (ver `docs/MOB_CONTRACT.md`, regra 4). Sem número, o campo não está pronto.

## 1. Ficha

### 1.1 Identificação e Lore
- **ID / nome interno:** `<id>`; **Nome pt_br:** `<nome>`; **Nome en_us:** `<name>`; **Tipo:** mob comum | companheiro | mini-boss | **[BOSS]**.
- **Nome do pacote (`pkg`):** `<nome curto da pasta em entity/ e client/, ex.: canopy, collector, crimson, babydragon>`. O ID do mob (`<id>`) é usado em arquivos, scripts e registros; o `pkg` só nos pacotes Java. Confira os pacotes existentes antes de escolher.
- **Mob de referência (estrutura de código):** `<Lithophage | LastGiftCollector | CanopyBreaker | CrimsonCommander | BabyDragon | outro>`.
- **Lore e Origem Mítica (4–8 linhas):** `<origem mitológica/feérica, papel ecológico ou espiritual, lenda que os povos contam, motivação e ameaça/propósito>`.
- **Assinatura de Silhueta e Proporções (OBRIGATÓRIO):** `<foco visual e distorção anatômica marcante que quebra a simetria clássica: membros desproporcionais, postura curvada, assimetria intencional, pescoço/cauda segmentada>`.
- **Referências visuais:** `<imagens/links; paleta de cores, materiais, o que incorporar e o que evitar>`.
- **Dimensões:** altura `<blocos>`, largura da colisão `<blocos>`, altura dos olhos `<blocos>`.
- **Resolução do atlas / texel:** `<16 px/bloco padrão | atlas customizado declarado e justificado>`.

### 1.2 Atributos e Ecologia Básica
- **Atributos:** vida `<n>`, armadura `<n>` (+ resistência `<n>`), velocidade `<n>`, dano base `<n>`, resistência a repulsão `<n>`, alcance de visão `<n>`, XP `<n>`.
- **Imunidades e afinidades:** `<fogo, congelamento, afogamento, queda, luz do sol, etc.>`.
- **Onde aparece:** `<biomas, estruturas, camada Y, luz/horário, raridade, tamanho do bando>` | invocação `<condições de espaço/bioma, item consumido, falha não gasta item>`.
- **Limite de spawn:** `<máximo por chunk ou área>`; **dificuldade pacífica:** `<não spawna | spawna>`.
- **Tags de alvo:** `<undead, villager, golem, animal, ...>`.

### 1.3 Matriz de Comportamento e Interações
- **Relação com a própria espécie:** `<cooperativo, alfa, neutro, territorialista>`.
- **Mortos-vivos:** `<hostil | neutro | aliado | evita>`. **Aldeões e golems:** `<ignora | ataca | teme>`. **Animais pacíficos:** `<caça | indiferente | protetor>`. **Outros mobs do mod:** `<reação e imunidade mútua>`.
- **Luz solar vs escuridão:** `<queima | debilita | enfurece | indiferente>`. **Água e chuva:** `<nada | afunda | dano | foge>`. **Fora do bioma:** `<mantém | passivo | volta | frenesi>`.
- **Rotina fora de combate:** `<patrulha, sono, vigília, alimentação>`.
- **Idle 1 (sorteado):** `<inspeção/percepção>`. **Idle 2:** `<reajuste postural>`. **Idle 3:** `<demonstração de identidade/ameaça>`.

### 1.4 Combate, Fases e Multiplayer
- **[BOSS] Cinemática:** duração `<ticks>`; ação cênica `<descida, rugido, ritual...>`; efeitos no cliente `<câmera, letterbox, tremor, título>`.
- **Tabela de ataques** (uma linha por ataque):

| Ataque | Alcance (blocos) | Preparo (ticks) | Execução (ticks) | Recuperação (ticks) | Dano | Efeitos/projéteis | Telegrafia | Contra-jogada |
|---|---|---|---|---|---|---|---|---|
| `<nome>` | `<n>` | `<n>` | `<n>` | `<n>` | `<n>` | `<...>` | `<...>` | `<...>` |

- **[BOSS] Estágios:** limiares de vida `<ex.: 60%, 25%>`; mudanças `<novos ataques, recargas, velocidade>`; fúria final `<...>`.
- **[BOSS] Persistência e reaparecimento:** cooldown `<ticks/dias/nunca>`; estado salvo `<sim | não (noSave)>`.
- **Escala multiplayer:** `<não | sim: fórmula e limite>`. **Leash:** `<distância>`; ao sair `<retorna | cura | desiste>`.
- **Foco:** `<agressor recente | mais próximo | maior dano>`. **Anti-cerco:** `<ataque 360° | repulsão | salto>`. **Anti-pillaring:** `<ataque à distância | quebra de apoio | recuo>`.
- **Lacaios/invocações:** `<conceito; regras via PlayerSummon (dono/UUID); limite populacional>`.

### 1.5 Drops, Sons e Textos
- **Drops:** comuns `<item, quantidade, chance>`; especial/arma `<propriedade, durabilidade, recarga>`; receitas `<...>`.
- **Sons:** por evento (ambiente, hurt, morte, passos, cada ataque) descreva o **caráter do som** (`<grave e rouco, estalo seco, sino metálico>`). Não escreva IDs de som: a fatia 4 escolhe os eventos vanilla que existem no 26.1.2 (decisão D-010) e verifica antes de registrar.
- **Textos:** Bestiário `<capítulo de 6+ páginas>`; conquistas em cadeia `<título/descrição/ícone>`.
- **Faixa de eventos de rede:** `<ex.: 120–139>` (IDs ≥ 100; checar `docs/design/*` para não colidir).

### 1.6 Animação (OBRIGATÓRIA, em duas partes)

**Parte A: intenção e tempo (preenchida na ficha, aceita rascunho do chat).** Uma linha por animação (idle 1–3, andar, cada ataque dividido em preparo/execução/recuperação, hurt, morte). Descreva a pose-chave em palavras usando partes do corpo nomeadas, e dê a duração em ticks. Sem ângulos.

| Animação | Duração (ticks) | Pose-chave e movimento (em palavras) | Peso e inércia | Tick do impacto |
|---|---|---|---|---|
| `<ataque_1_preparo>` | `<n>` | `<eleva a arma acima da cabeça, torso inclina para trás, perna de trás apoia>` | `<recua o peso>` | `<n ou n/a>` |

- **Juntas do modelo (hierarquia):** `<tronco > cabeça > pescoço 1..n; tronco > braço_sup > braço_inf > mão; ...>`, com o ponto físico de dobra de cada uma.

**Parte B: ângulos (preenchida pela fatia 2, não pelo rascunho).** O agente da fatia 2 calcula os ângulos contra o mob de referência e o modelo real, valida nas poses renderizadas e grava aqui o que o código faz. A ficha passa a refletir o código, não o contrário.

| Animação | Junta | Rot inicial (x/y/z, graus) | Rot final (x/y/z, graus) | Curva | Estado |
|---|---|---|---|---|---|
| `<ataque_1_preparo>` | `<braço_dir>` | | | | pendente |

## 2. Estado de implementação

> Atualizado ao final de cada sessão. É o único meio de passar contexto entre sessões.

| Fatia | Estado | Data | Comando de aceite e resultado |
|---|---|---|---|
| 0. Ficha revisada | pendente | | |
| 1. Fundação | pendente | | |
| 2. Visual e rigging | pendente | | |
| 2b. Revisão visual (humano) | pendente | | |
| 3. IA e combate | pendente | | |
| 4. Polimento e docs | pendente | | |
| 5. Relatório | pendente | | |

- **Próximo passo:** `<o que a próxima sessão deve fazer primeiro>`
- **Decisões tomadas e motivos:** `<lista curta; decisões grandes também em DECISIONS.md>`
- **Pendências e riscos:** `<...>`
