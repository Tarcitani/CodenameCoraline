# Corvo Feérico (`fey_raven`)

> Documento vivo de especificação e estado técnico do Corvo Feérico.
> Baseado na referência visual botânica em `docs/design/fey_raven_reference.jpg`.

## 1. Ficha

### 1.1 Identificação e Lore
- **ID / nome interno:** `fey_raven`; **Nome pt_br:** `Corvo Feérico`; **Nome en_us:** `Fey Raven`.
- **Perfil:** `aliado` (ver tabela de escopo e testes em `docs/PROCESSO_MOB.md`).
- **Nome do pacote (`pkg`):** `raven` (`com.codenamecoraline.entity.raven` e `client.raven`).
- **Mob de referência (estrutura de código):** `BabyDragon` (para `TamableAnimal`, vínculo com dono, comandos e proteção) e `Parrot` vanilla (para asas, voo e ombro).
- **Lore e Origem Mítica (4–8 linhas):**
  Pássaro místico de plumagem corvina negra azulada e inteligência sobrenatural, associado aos mensageiros das cortes feéricas crepusculares. Possui um terceiro olho místico no centro da testa e runas antigas entalhadas em suas penas que pulsam em luminescência cerúlea. Quando domesticado, viaja como sentinela de seu mestre, alertando sobre perigos ocultos com grasnados ressonantes e carregando mensagens e pequenos tesouros em um estojo de pergaminho preso à sua perna.
- **Assinatura de Silhueta e Proporções (OBRIGATÓRIO):**
  Corpo esguio de corvo com bico curvo e robusto de ave de rapina, garras afiadas em galho, cauda longa escalonada em cunha e penugem volumosa na garganta. Destaque marcante para o terceiro olho posicionado verticalmente no topo do crânio e pergaminho cilíndrico rúnico atado à pata esquerda com fitas soltas.
- **Referências visuais:**
  `docs/design/fey_raven_reference.jpg`. Paleta: penas preto ônix profundo (`#141419`) com reflexos grafite/azulado (`#252b36`), bico e pernas cinza chumbo (`#2f3136`), runas e terceiro olho em brilho ciano mágico (`#5ff5e0` / `#2bc7b4`), pergaminho pergaminho amarelado envelhecido (`#d8c69f`).
- **Dimensões:** altura `0.65` blocos, largura da colisão `0.5` blocos, altura dos olhos `0.55` blocos.
- **Resolução do atlas / texel:** `64×64` px (densidade uniforme de 16 px/bloco para tamanho de ave pequena).

### 1.2 Atributos e Ecologia Básica
- **Atributos:** vida `14.0`, armadura `0.0`, velocidade `0.28` (terrestre) / `0.40` (voo), dano base `2.0`, resistência a repulsão `0.1`, alcance de visão `24.0`, XP `3`.
- **Imunidades e afinidades:** imune a dano de queda (planar com asas), imune a efeito de Cegueira/Darkness (visão feérica do terceiro olho).
- **Onde aparece:** Florestas escuras (`Dark Forest`), Bosques ancestrais (`Old Growth Taiga`), Bosques pálidos e biomas de pântano; pousado em galhos de árvores acima de Y 64; raridade peso 15, bandos pequenos de 1 a 3 indivíduos.
- **Limite de spawn:** máximo de 3 por chunk; **dificuldade pacífica:** spawna normalmente.
- **Tags de alvo:** `arthropod` (caça pequenas aranhas de caverna/traças), evita monstros mortos-vivos no estado selvagem.

### 1.3 Matriz de Comportamento e Interações
- **Relação com a própria espécie:** cooperativo e curioso; empoleiram-se próximos em copas de árvores.
- **Mortos-vivos:** evita a uma distância segura de 8 blocos; se domesticado, grasna alto para alertar o dono sobre mortos-vivos invisíveis ou emboscados a até 16 blocos.
- **Aldeões e golems:** indiferente; pousa em telhados de vilas.
- **Animais pacíficos:** indiferente a animais de rebanho; ataca e caça baratas/traças (`silverfish`).
- **Outros mobs do mod:** neutro em relação ao Basilisco e Litófago; respeita e evita o Quebra-Copas e a Bruxa.
- **Luz solar vs escuridão:** ativo de dia e de noite; seus olhos e runas brilham no escuro absoluto.
- **Água e chuva:** sacode as asas e busca abrigo sob folhagens de árvores durante temporais.
- **Fora do bioma:** mantém curiosidade e busca o ponto mais alto do terreno para empoleirar.
- **Rotina fora de combate:** saltitar em galhos, planar de árvore em árvore, limpar penugem com o bico, inspecionar o chão à procura de bagas.
- **Variações de Repouso (Idle - sorteadas a cada 100–200 ticks):**
  - *Idle 1:* Limpar e ajeitar as penas do peito com o bico, eriçando a penugem da garganta.
  - *Idle 2:* Abrir o terceiro olho com um pulso emissivo suave enquanto vira a cabeça em 180° com curiosidade de corvo.
  - *Idle 3:* Grasnar inclinando o corpo para a frente e sacudindo a cauda em leque.

### 1.4 Regras de Aliado / Companheiro (`Perfil: aliado`)
- **Domesticação e Cura:**
  - *Domesticação:* Bagas Brilhantes (`minecraft:glow_berries`), com chance de 33% por baga, gerando partículas de corações feéricos turquesa.
  - *Cura:* Bagas Brilhantes e Bagas Doces restauram 4.0 de vida por consumo.
- **Comportamento com o Dono:**
  - *Modos de Comando:* Ciclo via clique direito com a mão vazia: **Seguir** (voa e acompanha o dono a 3–8 blocos) $\to$ **Sentar / Pousar** (fica fixo no bloco) $\to$ **Sentinela** (orbita o local em círculos).
  - *Teletransporte de Segurança:* Se a distância do dono ultrapassar 20 blocos no modo seguir, teletransporta-se para o ar ou bloco seguro acima do dono.
- **Recursos Especiais (Pergaminho de Mensageiro & Ombro):**
  - *Pergaminho de Mensageiro (Inventário 1 slot):* O jogador pode abrir o inventário do corvo (Shift + clique direito) e colocar 1 item (livro com pena, papel, mapa ou gema) no estojo da pata. O corvo dropa o item intacto se for abatido.
  - *Pouso no Ombro:* Se o dono pular em direção ao corvo em modo seguir, o corvo pode pousar no ombro esquerdo (não obstrutivo em primeira pessoa).
  - *Sentinela Mística:* Se qualquer mob hostil invisível, camuflado ou se aproximando por trás estiver a menos de 16 blocos do dono, o corvo emite um grasnado de alerta e ilumina o alvo com o efeito de Brilho (`Glowing`) por 5 segundos (cooldown de 30 segundos).
- **Proteção e Fogo Amigo:**
  - Imunidade mútua: o dono não machuca o corvo com golpes acidentais de espada ou arcos; o corvo nunca ataca o dono nem aliados domesticados pelo dono (ex.: lobos, dragões).
  - Se o dono for atacado, o corvo voa alto e ataca com bicadas de distração rasantes, recuando imediatamente.
  - Se o dono morrer, o corvo pousa no local e permanece em vigília guardando o corpo até o dono retornar.

### 1.5 Drops, Sons e Textos
- **Drops:** comuns: 1–2 `minecraft:feather`; se domesticado com pergaminho equipado, dropa o item guardado; drop especial raro (10%): `codenamecoraline:fey_feather` (pena luminescente usada em poções ou receitas feéricas).
- **Sons (Mapeamento Vanilla D-010):**
  - *Ambiente / Grasnar:* evento `entity.parrot.ambient` com pitch alterado para `0.65` (som rouco e grave de corvo).
  - *Grasnado de Alerta Místico:* evento `entity.witch.celebrate` / `block.amethyst_block.resonate` pitch `1.4`.
  - *Bater de Asas:* evento `entity.parrot.fly` pitch `0.85`.
  - *Dor:* evento `entity.parrot.hurt` pitch `0.70`.
  - *Morte:* evento `entity.parrot.death` pitch `0.65`.
- **Textos:**
  - Bestiário: capítulo completo de 6 páginas (`Bestiary.FEY_RAVEN`) com textos de ecologia feérica e 3 gravuras (Anatomia botânica, Voo místico, Mensageiro e pergaminho).
  - Conquistas: cadeia de 3 avanços (`bosses/find_fey_raven`, `bosses/tame_fey_raven`, `bosses/fey_messenger`).
- **Faixa de eventos de rede (`broadcastEntityEvent`):** `140–149` (reservada exclusivamente para o corvo feérico: 140 = piscar terceiro olho, 141 = alerta místico, 142 = abrir pergaminho).

### 1.6 Animação (Parte A: Intenção e Tempo)

| Animação | Duração (ticks) | Pose-chave e movimento (em palavras) | Peso e inércia | Tick do impacto |
|---|---|---|---|---|
| `idle_base` | 40 | Respiração suave em poleiro: peito expande levemente, cauda balança 2° no eixo Z, asas coladas ao flanco | Leve e orgânico | n/a |
| `idle_clean_feathers` | 30 | Vira o bico para as penas da asa esquerda, passa o bico 2 vezes e sacode a cabeça | Rápido com trancos de pássaro | n/a |
| `idle_third_eye` | 35 | Ergue a cabeça para cima, o terceiro olho abre com brilho intenso, bico levemente aberto | Místico e estático | n/a |
| `idle_caw` | 25 | Inclina o corpo 30° para a frente, abre o bico emitindo grasnado e abre a cauda em cunha | Impulso frontal | n/a |
| `fly_flap` | 8 | Asas abrem na horizontal (extensão máxima), descem com força arqueadas e sobem dobradas | Ciclo rápido de batida de asas | n/a |
| `fly_glide` | 20 | Asas semi-estendidas rígidas com pontas ligeiramente curvadas para cima, cauda aberta | Sustentação no ar | n/a |
| `peck_attack` | 15 | Mergulho rasante com bico projetado à frente em golpe direto de estocada | Impacto veloz | 8 |
| `hurt` | 6 | Tranco para trás com asas semi-abertas e penas eriçadas | Reação rápida | n/a |
| `death` | 20 | Asas perdem sustentação, corpo tomba de lado e se recolhe | Queda suave | n/a |

- **Hierarquia de Juntas do Modelo:**
  - `root` (posicionado na base dos pés)
    - `body` (torso inclinado a 45°, ponto de articulação nos quadris)
      - `neck` & `head` (base no topo do peito, com `beak` e `third_eye_layer`)
      - `left_wing` & `right_wing` (ombros no topo lateral do torso, com sub-articulação de penas primárias)
      - `tail` (base na garupa, com 3 leques de penas sobrepostas)
      - `left_leg` & `right_leg` (coxas finas saindo do ventre, com garras articuladas e caixa do `scroll` na pata esquerda)

---

## 2. Estado de implementação

| Fatia | Estado | Data | Comando de aceite e resultado |
|---|---|---|---|
| 0. Ficha revisada | **concluída** | 2026-11-05 | Ficha completa sem `<...>`, validada contra `docs/design/fey_raven_reference.jpg` |
| 1. Fundação | **concluída** | 2026-11-05 | `.\gradlew.bat compileJava` (OK) + `.\gradlew.bat runFeyRavenTest` (All 3 required tests passed, 2 testes de `fey_raven_test_environment`, BUILD SUCCESSFUL in 13s) |
| 2. Visual e rigging | pendente | | |
| 2b. Revisão visual (humano) | pendente | | |
| 3. IA de Companheiro | pendente | | |
| 4. Polimento e docs | pendente | | |
| 5. Relatório de entrega | pendente | | |

- **Próximo passo:** Iniciar a **Fatia 2 (Visual e rigging)** numa nova sessão (ou após `/clear`) executando o prompt [docs/prompts/fatia_2_visual.md](file:///c:/MinecraftDev/CodenameCoraline/docs/prompts/fatia_2_visual.md).

