# O Arquiteto — Kandiaru (`architect`)

> **Status:** especificação de design e produção (contrato de modelo, rig, animação e combate). **Nada implementado.** Nenhum item visual foi validado no cliente (`runClient` não executado). Versão canônica criada em 2026-11-08 a partir da ficha externa `arquiteto_kandiaru_especificacao_minecraft (1).md` (1204 linhas), do dossiê `dossi_de_combate_sung_jin_woo_vs_arquiteto.md`, dos prompts de especificação (colado e `prompt_final_arquiteto_kandiaru_detalhado.md`) e da prancha conceitual `a_dark_dramatic_character_design_sheet_in_a_minec.png`. Não havia ficha anterior do Arquiteto em `docs/design/`; nada foi sobrescrito.

## Índice

0. Resumo executivo · 1. Classificação das fontes e conflitos · 2. Identidade e pilares · 3. Convenção de coordenadas e tabela única de dimensões · 4. Anatomia parte por parte · 5. Árvore do rig e tabela de pivôs · 6. Sockets e âncoras · 7. Disfarce, tábua e timeline mestre da revelação · 8. Direção de arte, paleta, pixel art e UV · 9. As seis cimitarras · 10. Postura, movimentação e navegação · 11. Defesa, resistência e vulnerabilidades · 12. IA e fases · 13. Catálogo de ataques · 14. Variações e função das fileiras de braços · 15. Catálogo de animações e canais · 16. Poderes, partículas, VFX e áudio · 17. Hitboxes e justiça · 18. Compatibilidade técnica com o projeto · 19. Itens obrigatórios de boss do projeto · 20. Plano de produção e critérios de aceitação · 21. Registro de decisões, dúvidas e testes pendentes · 22. Relatório de alterações

Legenda usada em todo o documento: **[REF]** base da referência (dossiê/visuais) · **[ADP]** adaptação de design · **[OPC]** opcional / fase posterior · **[HIP]** hipótese a validar no blockout ou no cliente.

---

## 0. Resumo executivo

**Decisões finais**

| # | Decisão | Tipo |
|---|---|---|
| 1 | ID `architect`, pacote `architect` (padrão do repo: IDs descritivos sem artigo — `crimson_commander`, `canopy_breaker`, `last_gift_collector`). Nome exibido "O Arquiteto" / en_us "The Architect"; subtítulo "Kandiaru". | [ADP] |
| 2 | Uma entidade só. Seis braços, seis cimitarras, duas asas, máscara e tábua são `ModelPart`s do mesmo `EntityModel<ArchitectRenderState>`. | [ADP] |
| 3 | Escala: 54 px de altura (3,375 bl; meta 3,35 arredondada para pixel inteiro), colisão `1.55 × 3.35` bl, asas abertas 84 px (5,25 bl). | [ADP] |
| 4 | Rig com cadeia real de 7 níveis por braço (ombro → braço → cotovelo → antebraço → punho → mão → arma), `teeth_upper` na cabeça e `teeth_lower` na `jaw`, máscara bipartida com pivôs nas bordas externas, tábua presa a `tablet_anchor` filho de `torso`. | [ADP] (corrige P0/P1) |
| 5 | Revelação em **uma** timeline mestre `architect_awaken` de 110 ticks; nenhum outro clip toca o modelo durante ela. | [ADP] |
| 6 | Atlas único 256×256 (armas em região própria do mesmo atlas, não atlas separado de 128) + textura emissiva com o mesmo UV desenhada por `EyesLayer` (padrão já usado em `BasiliskEyesLayer` e `DragonGlowLayer`). | [ADP] |
| 7 | Animações: `AnimationDefinition` vanilla. Composição em dois níveis (clip principal + overlay com conjunto de partes disjunto), padrão do `CrimsonCommanderModel`; escalar para compositor por canal (padrão `PoseMixer`, D-035) só se o blockout provar necessário. | [ADP] |
| 8 | Partículas da v1 são **nomes lógicos** mapeados para partículas vanilla (`DustParticleOptions` coloridas, partículas de bloco). Tipos de partícula próprios ficam [OPC] e exigiriam registro novo + entrada em `DECISIONS.md`. Sons: eventos do mod apontando para áudios vanilla com pitch (D-010). | [ADP] |
| 9 | Fases por vida: F1 100–70 %, F2 70–35 %, F3 < 35 %. Protocolo de Domínio (J) é [OPC] e desligado por padrão na v1. | [ADP] |
| 10 | Dentes **planos** (referência visual), não "afiados" (texto do dossiê). | [ADP] resolve conflito C-01 |
| 11 | Faixa de eventos de rede `160–179` (140–159 ocupadas por corvo e salamandra). | [ADP] |

**Pendências principais:** relatório `validacao_alteracoes_arquiteto_kandiaru.md` não foi encontrado na máquina (procurado em Downloads, Desktop, Documents e `C:\MinecraftDev`); as correções P0/P1 foram aplicadas a partir do texto do prompt da tarefa. Todos os ângulos, pivôs e tempos são [HIP] até o blockout no Blockbench e o teste em `runClient`. Arena (Estátua de Deus, nós rúnicos, estátuas de cenário) não tem estrutura projetada — fica em §21.

---

## 1. Classificação das fontes e conflitos

### 1.1 Hierarquia

1. Dossiê → fatos **[REF]** (disfarce, tábua, seis braços/cimitarras, asas, sorriso, olhos, veios de mana, autômatos, feixes térmicos, contagem de 3:00, constituição pétrea, mandíbula quebrada pela joelhada, morte pela face partida).
2. Ficha externa (1) → base a consolidar; suas decisões são **[ADP]** e foram preservadas salvo conflito abaixo.
3. Correções P0/P1 (prompt) → obrigatórias, aplicadas em §5 e §7.
4. Prancha conceitual → somente silhueta e diagramação. **Não** define geometria, paleta (a prancha usa vermelho/magenta nos poderes e runas vermelhas no torso — descartado), UV nem dentes (a prancha mostra caveira sorridente em "Sorriso Maníaco" — descartado).

### 1.2 Conflitos registrados

| ID | Conflito | Impacto | Resolução |
|---|---|---|---|
| C-01 | Dossiê: "dentes afiados talhados em pedra". Ficha/visuais: dentes planos, retangulares, cerrados. | Leitura facial inteira. | Dentes planos (visual prevalece sobre prosa; proibição explícita de presas no prompt). [ADP] |
| C-02 | Dossiê: corpo "imune a sangramentos e armas comuns". | Jogabilidade. | Sem imunidade a dano físico; imune só a efeitos de sangramento/veneno inexistentes em pedra (ver §11.1). [ADP] |
| C-03 | Dossiê: "dezenas de estátuas guardiãs" avançam. | Escopo (novos mobs proibidos). | Comando dos Autômatos usa blocos/estruturas da arena e placas temporárias, sem entidade com IA. [ADP] |
| C-04 | Ficha §2.2 máscara energizada `#E8F6FF`; ficha §4.3 `#DDF4FF`. | Paleta. | `#DDF4FF` (tabela de paleta é a fonte). |
| C-05 | Ficha §2.2 losango `#E6D6FF / #B6FFF0`; paleta `#D8C8FF`. | Mistura lilás com ciano de mana. | Losango só lilás (`#D8C8FF/#7E64C4/#F7F2FF`). Ciano é exclusivo da mana. |
| C-06 | Ficha §2.2 sombra do nariz `#36343D` (fora da paleta). | Paleta. | Usar `#434852` (sombra da pedra cinza-azulada, material do rosto). |
| C-07 | Ficha: `teeth` única filha de `head` com `jaw` articulada. | Dentes inferiores ficariam parados ao abrir a boca (P0). | `teeth_upper` em `head`, `teeth_lower` em `jaw` (§5). |
| C-08 | Ficha: braço com 6 filhos irmãos. | Rotação do cotovelo não moveria antebraço/mão/arma (P0). | Cadeia aninhada (§5). |
| C-09 | Ficha: três clips de revelação (`awaken_cracks`, `reveal_arms`, `reveal_wings`) sem regra de prioridade. | Dois clips no mesmo pivô (P1). | Clip único `architect_awaken`, 110 ticks (§7.4). Os nomes antigos viram rótulos de trechos. |
| C-10 | Ficha: armas em atlas 128×128 separado. | Um `EntityModel` desenha com uma textura por passe; atlas separado exigiria camada de render extra. | Região de armas no atlas 256 (§8.6). |
| C-11 | Ficha: `mandament_table` filha de `root`. | Tábua não acompanharia inclinação do torso; mãos desalinhadas (P1). | `tablet_anchor` filho de `torso`, alinhado aos sockets de mão (§7.2). |
| C-12 | Prompt sugere `the_architect`. | Padrão de IDs. | `architect` (§0 dec. 1). |
| C-13 | Altura 3,35 bl = 53,6 px. | Pixel fracionário. | 54 px (3,375 bl) no modelo; colisão mantém 3,35. Diferença 0,4 px, invisível. |
| C-14 | Dossiê: mão "dedos terminados em pontas agudas"; ficha: "garras" de rocha talhada. | Nenhum. | Mantido: pontas cônicas facetadas, sem garras orgânicas. |
| C-15 | Dossiê: asas "com texturas de penas rígidas"; prompt: "não penas macias". | Leitura do material. | Placas de pedra **em forma de pena esculpida**, rígidas, com recortes de sombra. Compatível. |
| C-16 | Prompt colado: timeline `architect_awaken`; prompt detalhado: `architect_awaken_cracks`. | Nome do clip. | `architect_awaken`; `architect_awaken_cracks` vira rótulo do trecho 1–5 da timeline. |
| C-17 | Ficha: caminhada sem duração; prompt detalhado: loop 24 t, corrida 16–20 t, strafe 16 t. | Locomoção. | Valores do prompt detalhado (corrida 18 t). |

---

## 2. Identidade e pilares de design

O Arquiteto é uma **falsa divindade esculpida em pedra**. Em repouso, um monumento religioso encapuzado segurando a Tábua dos Mandamentos [REF]. Desperto, revela anatomia impossível e funcional: torso arquitetônico, seis braços independentes, seis cimitarras de basalto, asas de gárgula em placas, túnica mineral e um rosto de estátua clássica corrompido — fendas de marionete, esclera negra, pupila coral, losango na testa e um sorriso largo demais de dentes planos cerrados [REF].

Pilares (preservados da ficha):
1. **Falsa divindade** — a forma inicial é imóvel, serena e crível como decoração.
2. **Uncanny valley, não caveira** — feições humanas clássicas (nariz afilado, maçãs, queixo firme, dentes humanos) distorcidas. Proibido: caveira, órbitas redondas grandes, presas, sorriso de tubarão, demônio genérico.
3. **Anatomia impossível, mas funcional** — cada braço tem ombro, cotovelo, punho e trajetória próprios.
4. **Duelista mecânico** — mede distância, alterna alturas, protege ângulos.
5. **Pedra viva administrada por magia** — veios de mana ciano, selos lilás, rachaduras que reagem.
6. **Chefe legível** — todo golpe tem preparo visual e sonoro; complexidade vem da combinação, não de golpes imprevisíveis.

Silhueta em três distâncias: **longe** — capuz pontudo escuro, duas asas recortadas, coroa lateral de seis lâminas, dois pontos coral e um losango lilás; **média** — rosto de pedra emergindo do vão negro, sorriso claro cerrado, três fileiras de braços por lado, veios no peito; **perto** — fendas faciais, nariz, duas fileiras de dentes, dedos segmentados, runas nas lâminas, placas sobrepostas.

---

## 3. Convenção de coordenadas e tabela única de dimensões

### 3.1 Convenção (usar em todo o documento)

- **Unidade:** pixel de modelo. **16 px = 1 bloco.**
- **Coordenadas absolutas "BB"** (as que se digitam no Blockbench, formato *Modded Entity / Java*): origem no chão, no centro da colisão; **+Y para cima**; **frente do personagem = −Z** (norte do Blockbench); **+X = lado ESQUERDO do personagem** (padrão vanilla: `leftArm` em x = +5). Sufixo `_left` = +X, `_right` = −X.
- **Conversão para `PartPose.offset` em Java** (eixo Y do código cresce para baixo, chão em y = 24 na raiz vanilla): para filho direto de `root`, `offset = (X, 24 − Y, Z)`; para qualquer outro filho, `offset = (X − Xpai, −(Y − Ypai), Z − Zpai)`. As tabelas abaixo dão **pivôs absolutos BB**; o implementador deriva os offsets.
- **Rotações** em graus, como em `KeyframeAnimations.degreeVec`, convenção vanilla de `ModelPart` (ordem Z·Y·X, X aplicado primeiro — mesma nota do `CrimsonCommanderAnimations`):
  - `xRot` negativo levanta para a frente um membro pendente (braço à frente horizontal = −90); `xRot` positivo leva para trás uma parte abaixo do pivô (flexão de joelho = +; abrir mandíbula = +; cabeça olhando para baixo = +).
  - Abdução lateral: lado esquerdo (+X) abre com `zRot` **negativo**; lado direito abre com `zRot` **positivo** (mesma convenção do balanço de braço vanilla).
  - `yRot`: descrito em termos anatômicos ("medial/para dentro", "lateral/para fora"). **Sinal [HIP]: confirmar no blockout** antes de gravar keyframes.
- **Pose neutra (bind pose)** = pose em que a geometria é modelada: em pé, braços pendentes levemente abertos, cotovelos retos, armas apontando para baixo-frente, asas fechadas, boca fechada, máscara presente. Todas as poses de animação são deltas sobre ela.

### 3.2 Tabela única de dimensões finais

| Medida | Meta do prompt | Valor final (px) | Valor final (bl) | Observação |
|---|---|---:|---:|---|
| Altura até o topo do capuz | 3,35 | 54 | 3,375 | C-13 |
| Colisão (largura × altura) | 1,55 × 3,35 | — | 1,55 × 3,35 | `EntityType.Builder.sized(1.55F, 3.35F)`; altura dos olhos 2,95 bl (Y 47,2) |
| Largura tórax/ombros (sem braços) | 1,45 | 23 | 1,44 | ribcage X −11,5…+11,5 |
| Largura visual em guarda | 3,1–3,6 | 52–56 | 3,25–3,5 | lâminas médias abertas |
| Envergadura asas abertas | 5,0–5,4 | 84 | 5,25 | raiz X ±4 + 38 px por asa |
| Cimitarra (pomo à ponta) | 1,05–1,25 | 17–20 | 1,06–1,25 | §9 |
| Alcance braço armado desde o ombro | 2,0–2,3 | 33–36 | 2,06–2,25 | 8 + 8 + 2 (mão até a pegada) + 15–18 de lâmina além do punho |
| Altura ombros sup / méd / inf | 2,55 / 2,20 / 1,85 | 41 / 35 / 30 | 2,56 / 2,19 / 1,88 | inferior subiu 0,03 para não colidir com `abdomen` |
| Altura pelve (pivô) | — | 22 | 1,375 | pernas 10 + 10 + 2 |
| Pivô do pescoço / cabeça | — | 43 / 45 | 2,69 / 2,81 | |

Nenhum outro número de escala deve aparecer fora desta tabela; seções posteriores a referenciam.

---

## 4. Anatomia parte por parte

Formato de cada entrada: **forma · proporção · material · geometria sugerida (cubos, tamanhos em px L×A×P) · textura · articulação/pivô · função de animação.** Pivôs absolutos estão em §5.3.

### 4.1 Cabeça, capuz e rosto

- **`head`** — bloco do crânio, levemente vertical. Cubo 7×8×7 (X −3,5…3,5; Y 45…53; Z −3,5…3,5). Material: pedra cinza-azulada (§8.3). A face frontal (−Z) é a **face verdadeira** pintada; volume do nariz, sobrancelhas e mandíbula vem de cubos próprios. Pivô na base do crânio (Y 45, Z 0). Anima: olhar o alvo, inclinação arrogante, recuo de gargalhada.
- **`hood`** (capuz externo) — alto, estreito no topo, pesado nas laterais. 4 cubos: `hood_shell_back` 9×9×4 atrás (Z +1…+5); `hood_side_left`/`hood_side_right` 1,5×10×8 (X ±3,75…±5,25; Y 44…54; Z −4,5…+3,5), bordas frontais com lascas; `hood_crown` cunha 6×2×7 (Y 52…54) com rotação X −8° para formar a ponta. Pedra cinza-azulada externa, preto `#0B0B10` nas faces internas. Filho de `head`, sem pivô animado próprio (opcional: balanço de ±2° no `hood_crown` em impactos pesados).
- **`hood_shadow_inner`** (P1 — cavidade real) — **não** é uma placa preta sobre o rosto. São 3 placas recuadas que formam uma moldura em "U" invertido **atrás da linha da face e ao redor dela**:
  - `hood_cavity_top`: 8×0,5×3 em Y 53…53,5, Z −4,5…−1,5 (teto da cavidade; visível de frente como faixa negra acima da testa).
  - `hood_cavity_left` / `hood_cavity_right`: 0,5×9×3 em X ±3,75, Z −4,5…−1,5 (paredes; visíveis de frente como faixas negras de 0,5 px ladeando a face e, de lado, como o interior do capuz).
  - Faces pintadas de `#0B0B10`: as voltadas para o centro (para dentro) e para −Z. Faces voltadas para fora: invisíveis (cobertas pelas laterais do capuz) — pintar com pedra para evitar buraco se aparecerem.
  - A face frontal do `head` fica em Z −3,5; as paredes da cavidade começam em Z −4,5: **1 px de recuo** cria a sombra por geometria sem nenhuma superfície coplanar com o rosto. Olhos, nariz, sorriso e mandíbula ficam livres no vão central de 7 px.
  - Antes da revelação, o `head` tem `xRot` +18° (olhando para baixo): o teto da cavidade cobre os olhos visualmente e a face fica "quase oculta" sem placa extra.
- **`brow_left` / `brow_right`** — placas 3×1×1 em Y 50,5…51,5, Z −4,0…−3,5, `zRot` ±10° (franzidas para o centro). Sombra embaixo na textura. Animáveis em ±8° para expressões.
- **`forehead_diamond_seal`** — cubo 1×2,5×0,5 em X −0,5…0,5, Y 50,5…53, Z −4,0…−3,5 (frente meio pixel à frente da testa; a face traseira coincide com a testa, mas fica oculta por ela — nenhuma face visível coplanar). Pintura: losango lilás (§8.3). Emissivo na camada de brilho. Anima: escala 1,0→1,15 em pulsos de autoridade.
- **`eye_left` / `eye_right`** — **pintados na face** do `head` (sem cubo): fenda amendoada inclinada de 3×1 px (canto externo 1 px mais alto), esclera `#0B0B10`, íris anelar de 1 px `#FF604B` com centro `#FFD09A` na camada emissiva. Âncoras `eye_socket_left/right` (§6) marcam a origem do brilho e das partículas dos olhos (os feixes térmicos vêm da arena, §13.I).
- **`nose_bridge`** — cubo 1×3×1 em X −0,5…0,5, Y 47…50, Z −4,5…−3,5; aresta frontal clara, laterais sombra `#434852`. Fixo.
- **Fendas zigomáticas** — pintadas: de cada canto inferior do olho, 1 px de largura `#121217`, descendo em diagonal 2 px e dobrando 2 px para a lateral (ângulo obtuso), com borda de luz `#9BA3B0` do lado de cima. Continuam por 1 px na face lateral do `head` (UV contínuo).
- **Fendas mandibulares** — pintadas: dos cantos da boca descendo obliquamente até a borda inferior da `jaw`. **Coincidem com as bordas laterais do cubo `jaw`**: a fenda é a junta real da peça móvel.
- **`mouth_interior`** — cubo 6×2×1 em X −3…3, Y 45,5…47,5, Z −3,0…−2,0, preto `#0B0B10` (aparece quando a boca abre). Recuado 0,5 px em relação aos dentes.
- **`teeth_upper`** — cubo 6×1×0,5 em X −3…3, Y 46,5…47,5, Z −3,6…−3,1 (recuado 0,1 da face, nunca coplanar). Filho de `head`. Pintura: com densidade 2× na face (§8.2), 6 px de cubo = 12 px de UV → 4 dentes de 2 px + divisórias `#24232B` de 1 px (12 = 4×2 + 4×1, incluindo a borda), dentes `#C7BFB0`. Se a densidade 2× for descartada no blockout, cair para 3 dentes de 1 px + divisórias de 1 px (§8.2).
- **`jaw`** — cubo 6×2,5×6,5 em X −3…3, Y 44…46,5, Z −3,5…+3,0. Queixo firme e estreito: a frente tem um sub-cubo `jaw_chin` 3×1×0,5 em Z −4,0. **Pivô** em (0; 46,5; +2,0): atrás dos cantos da boca, na linha das "articulações temporo-mandibulares" escondidas sob o capuz. Arco: 0° (fechada, oclusão perfeita) → +22° (fala) → +38° (gargalhada) → +55° (quebra na morte, [OPC]). Limites seguros 0…+40° em combate.
- **`teeth_lower`** — filho de `jaw`; cubo 6×1×0,5 em X −3…3, Y 45,5…46,5, Z −3,6…−3,1. Com `jaw` a 0°, a borda superior de `teeth_lower` (Y 46,5) toca a borda inferior de `teeth_upper` (Y 46,5): oclusão sem sobreposição nem fresta. Divisórias dos dentes inferiores defasadas meio dente das superiores.
- **`face_mask_left` / `face_mask_right`** — ver §7.3 (geometria, pivôs, trajetórias).

### 4.2 Pescoço (`neck`)

Curto e grosso: 3 anéis `neck_ring_1..3` empilhados (5×1×5, 5,5×1×5,5, 6×1×6; Y 43…46 menos a parte coberta pelo capuz). Pedra cinza-azulada escurecida; 1 veio ciano vertical na frente (pintado), ligando a mandíbula ao selo do esterno. Pivô Y 43. Anima: `xRot` −15…+25 (compressão em golpes descendentes = `head` desce 1 px via POSITION; extensão em gargalhada = `head` sobe 0,5 px).

### 4.3 Tórax, esterno e abdômen

- **`torso`** — parte-mãe (pivô na cintura, Y 29). Contém:
  - `ribcage` cubo 16×14×9 (X −8…8; Y 29…43; Z −4…5) + duas **colunas de encaixe** `shoulder_mount_left/right` 3,5×14×7 (X ±8…±11,5) — a "estrutura arquitetônica" que sustenta os três ombros por lado. Largura total 23 px (§3.2).
  - Placas de costela: 3 sulcos horizontais pintados nas laterais, discretos (sem aparência de esqueleto: nada de vãos abertos).
  - Os três encaixes por lado são discos 4×4×1 pintados/levemente salientes nas colunas, em Y 41, 35, 30.
- **`sternum_seal`** — cubo 4×6×0,5 em X −2…2, Y 34…40, Z −4,5…−4,0. Três losangos encaixados num arco quebrado + linhas de circuito. Repouso: brilho quase nulo (1 nível acima da base); comando: linhas acendem do centro para fora (troca de intensidade na camada emissiva via estado — §18). Fixo; ponto vulnerável durante canalizações (§11).
- **Veios de mana do tórax** — pintados: do selo para cada um dos seis encaixes, linhas de 1 px com cotovelos de 90° (circuito), não curvas.
- **`abdomen`** — 4 placas horizontais sobrepostas `abdomen_plate_1..4` (14×2×8 → 12×2×7, cada uma 0,5 px recuada da de cima), Y 22…29. Duas fendas luminosas laterais pintadas. Filho de `torso` com pivô próprio em Y 29: gira para trás/baixo independente do tronco (`yRot` ±25°, `xRot` ±15°), dando a sensação de torção em degraus — metade da torção em `abdomen`, metade em `torso` (§10).

### 4.4 Seis braços

**Distribuição** (ver pivôs §5.3):

| Braço | Ombro (X, Y, Z) BB | Fileira | Leitura |
|---|---|---|---|
| `arm_top_left` / `arm_top_right` | (±10,5; 41; −1,0) | Superior | mais alto e à frente; verticais, estocadas, guarda alta |
| `arm_mid_left` / `arm_mid_right` | (±11,5; 35; +0,5) | Médio | lateral, um pouco atrás; horizontais, fintas, aparos |
| `arm_low_left` / `arm_low_right` | (±10,0; 30; +1,5) | Inferior | junto às costelas baixas e recuado; varreduras, tesouras, retornos |

**Segmentos (iguais nos seis, com assimetrias abaixo):**

| Segmento | Geometria (px) | Detalhe |
|---|---|---|
| encaixe/ombro (`arm_*`) | disco 4×4×4 + placa angular 5×2×5 em cima (ombreira natural, não medieval) | pivô do ombro no centro do disco |
| `upper_arm_*` | 3,5×8×3,5, afunilando (sub-cubo 3×3×3 perto do cotovelo) | 2 placas longitudinais + 1 linha gravada |
| `elbow_*` | anel 4×1,5×4 | pivô do cotovelo; pixels ciano entre placas |
| `forearm_*` | 4×8×4 + crista externa 1×6×1,5 pontuda | mais espesso que o braço; arranhões diagonais |
| `wrist_*` | anel 3,5×1,5×3,5 (3 placas pintadas) | pivô do punho |
| `hand_*` | palma 3×3×2; 4 dedos 0,75×2,5×0,75 (2 segmentos pintados, ponta cônica clara); polegar 1×2×1 cruzando o cabo | pose fechada padrão; dedos sem animação individual na v1 |
| `sword_*` | §9 | socket `grip_*` no centro da palma |

Comprimento ombro→pegada = 8 + 1,5 + 8 + 1,5 + 1,5 = 20,5 px; com 15–18 px de lâmina além da mão → alcance 33–36 px (§3.2).

**Assimetrias visuais [REF ficha, preservadas]** — só geometria/textura, **sem atributos distintos na v1**:
- `top_right`: placas do braço 0,5 px maiores, lâmina mais longa (dominante).
- `top_left`: placa do antebraço lascada com linha ciano na rachadura.
- `mid_right`: antebraço 4,5 px de largura (bloqueios).
- `mid_left`: dedos 3 px (fintas, troca de pegada).
- `low_right`: ponta mineral 1×1×2 no cotovelo, apontando para trás.
- `low_left`: fratura antiga pintada no punho.

### 4.5 Asas pétreas (gárgula)

Cada asa (grupos WING_L / WING_R):
- **`wing_root_*`** — encaixe escapular 4×5×3 atrás do tórax alto, pivô em (±4; 40; +5). Gira para abrir/fechar.
- **`wing_arm_*`** — haste principal (úmero) 2×2×14, saindo para trás-fora; pivô no root.
- **`wing_forearm_*`** — haste secundária (antebraço) 1,5×1,5×14, pivô na ponta do `wing_arm_*` (cotovelo da asa). Borda de ataque superior; ponta externa mais longa (`wing_tip_*` 1×1×6 cônica).
- **`wing_finger_1..4_*`** — 4 dedos pétreos 1×1×(10, 12, 11, 8), pivôs em leque na ponta do `wing_forearm_*`; cada um com 2–3 **placas de cobertura** (`wing_plate_*`) 0,5×4×5 rígidas, em forma de pena esculpida, sobrepostas com 0,5 px de vão para recorte de sombra (C-15). Não há membrana.
- **Placas internas** `wing_inner_plate_*` 0,5×5×4 densas junto ao root, escondem a articulação quando fechada.

Estados (ângulos [HIP], ver §5.4):

| Estado | Root | Arm | Forearm | Dedos | Silhueta |
|---|---|---|---|---|---|
| Fechada | dobrada para trás e para baixo, quase colada ao manto | — | dobrada sobre o arm | fechados em feixe | pontas superiores 2 px acima dos ombros; base na altura do quadril (Y ~22) |
| Semiaberta (combate) | 35° lateral | 20° | 40° | abertos 50 % | contrapeso visível, não amplia leitura de largura além de ~3,5 bl |
| Aberta | 80° lateral, 15° para cima | 10° | 5° | leque total | 84 px de envergadura, borda em coroa quebrada |

Comportamento por movimento em §10. **Sem voo permanente** — só impulso, planeio curto e mergulho.

### 4.6 Quadril, túnica, pernas e pés

- **`pelvis`** — placa pélvica 10×4×7 em Y 20…24, pivô (0; 22; 0). Filha de `root`. Torção `yRot` ±20°. Cintura arquitetônica: 3 placas pintadas.
- **Túnica de placas minerais** (filhas de `pelvis`, cada uma com pivô na borda superior para balançar):
  - `robe_front` — 3 placas verticais (pregas) 3×16×1 lado a lado em X −4,5…4,5, de Y 22 até Y 6 (não chega ao chão: **as canelas aparecem abaixo de Y 6**); pontas inferiores irregulares (recortes de 1–2 px na geometria). Pivô (0; 22; −4). Abertura no centro de 1 px entre pregas para não esconder a leitura das pernas em passada.
  - `robe_back_upper` — mais longa: 10×19×1 de Y 22 até Y 3, pivô (0; 22; +4), dividida em `robe_back_upper` e `robe_back_lower` (pivô Y 12) para balanço em duas etapas.
  - `robe_left` / `robe_right` — placas diagonais 1×14×6 (X ±5…±6), pivô (±5,5; 22; 0).
  - Folgas: `robe_left`/`robe_right` ficam a 1 px de distância da trajetória do `forearm_low_*` em guarda (ver §5.5).
- **Pernas** (filhas de `pelvis`):
  - `leg_*` (coxa) 4,5×10×5, pivô do quadril (±3; 22; 0), placa frontal em escudo alongado.
  - `knee_*` placa angular 4×2×2 na frente; **pivô do joelho** (±3; 12; −0,5).
  - `shin_*` (canela) 3,5×10×4, levemente inclinada para trás (bind `xRot` +4°), crista frontal clara, 2 placas-tendão atrás.
  - `ankle_*` pivô (±3; 2; +0,5); `foot_*` 5×2×7 (mais largo que o tornozelo), 3 dedos em cunha 1,5×1,5×2 na frente (Z −5…−3), calcanhar 3×2×1,5 destacado, sola plana em Y 0.
- **Anti-interseção:** coxas parcialmente cobertas por `robe_front`/`robe_left`/`robe_right`; em flexão de quadril > 50°, `robe_front` recebe `xRot` −0,6 × (flexão) por regra procedural ou keyframe acompanhante (as pregas "abrem" à frente do joelho). Braços inferiores em guarda passam **à frente** de `robe_left`/`robe_right` (Z ≤ −4) nunca através dela.

---

## 5. Árvore do rig e tabela de pivôs

### 5.1 Árvore completa (sem placeholders)

```text
root                                   # chão, (0,0,0) BB
├── pelvis
│   ├── robe_front
│   ├── robe_back_upper
│   │   └── robe_back_lower
│   ├── robe_left
│   ├── robe_right
│   ├── leg_left
│   │   └── knee_left
│   │       └── shin_left
│   │           └── ankle_left
│   │               └── foot_left
│   ├── leg_right
│   │   └── knee_right
│   │       └── shin_right
│   │           └── ankle_right
│   │               └── foot_right
│   └── torso
│       ├── abdomen
│       ├── ribcage
│       ├── sternum_seal
│       ├── socket_sternum                       (âncora)
│       ├── tablet_anchor
│       │   └── mandament_tablet
│       │       ├── tablet_half_left
│       │       └── tablet_half_right
│       ├── neck
│       │   └── head
│       │       ├── hood
│       │       │   ├── hood_shell_back
│       │       │   ├── hood_side_left
│       │       │   ├── hood_side_right
│       │       │   └── hood_crown
│       │       ├── hood_shadow_inner
│       │       │   ├── hood_cavity_top
│       │       │   ├── hood_cavity_left
│       │       │   └── hood_cavity_right
│       │       ├── forehead_diamond_seal
│       │       ├── brow_left
│       │       ├── brow_right
│       │       ├── nose_bridge
│       │       ├── eye_socket_left                (âncora)
│       │       ├── eye_socket_right               (âncora)
│       │       ├── mouth_interior
│       │       ├── teeth_upper
│       │       ├── jaw
│       │       │   └── teeth_lower
│       │       ├── face_mask_left
│       │       └── face_mask_right
│       ├── arm_top_left
│       │   └── upper_arm_top_left
│       │       └── elbow_top_left
│       │           └── forearm_top_left
│       │               └── wrist_top_left
│       │                   └── hand_top_left
│       │                       ├── tablet_grip_left        (âncora)
│       │                       └── sword_top_left
│       │                           └── blade_tip_top_left  (âncora)
│       ├── arm_top_right
│       │   └── upper_arm_top_right
│       │       └── elbow_top_right
│       │           └── forearm_top_right
│       │               └── wrist_top_right
│       │                   └── hand_top_right
│       │                       ├── tablet_grip_right       (âncora)
│       │                       └── sword_top_right
│       │                           └── blade_tip_top_right (âncora)
│       ├── arm_mid_left
│       │   └── upper_arm_mid_left
│       │       └── elbow_mid_left
│       │           └── forearm_mid_left
│       │               └── wrist_mid_left
│       │                   └── hand_mid_left
│       │                       └── sword_mid_left
│       │                           └── blade_tip_mid_left
│       ├── arm_mid_right
│       │   └── upper_arm_mid_right
│       │       └── elbow_mid_right
│       │           └── forearm_mid_right
│       │               └── wrist_mid_right
│       │                   └── hand_mid_right
│       │                       └── sword_mid_right
│       │                           └── blade_tip_mid_right
│       ├── arm_low_left
│       │   └── upper_arm_low_left
│       │       └── elbow_low_left
│       │           └── forearm_low_left
│       │               └── wrist_low_left
│       │                   └── hand_low_left
│       │                       └── sword_low_left
│       │                           └── blade_tip_low_left
│       ├── arm_low_right
│       │   └── upper_arm_low_right
│       │       └── elbow_low_right
│       │           └── forearm_low_right
│       │               └── wrist_low_right
│       │                   └── hand_low_right
│       │                       └── sword_low_right
│       │                           └── blade_tip_low_right
│       ├── wing_root_left
│       │   └── wing_arm_left
│       │       └── wing_forearm_left
│       │           ├── wing_tip_left
│       │           ├── wing_finger_1_left
│       │           ├── wing_finger_2_left
│       │           ├── wing_finger_3_left
│       │           └── wing_finger_4_left
│       └── wing_root_right
│           └── wing_arm_right
│               └── wing_forearm_right
│                   ├── wing_tip_right
│                   ├── wing_finger_1_right
│                   ├── wing_finger_2_right
│                   ├── wing_finger_3_right
│                   └── wing_finger_4_right
└── (nenhum outro filho de root)
```

Notas:
- `torso` é filho de `pelvis` (não de `root`, como na ficha) para que pulos/agachamentos movendo a pelve levem o corpo todo, e a torção do tronco seja relativa ao quadril. `abdomen` é geometria do torso com pivô próprio para torção parcial.
- Placas de cobertura das asas (`wing_plate_*`, `wing_inner_plate_*`) são **cubos** dentro dos dedos/root, não partes animadas.
- Os anéis do pescoço são cubos de `neck`.
- `elbow_*`, `wrist_*`, `knee_*`, `ankle_*` são **partes-pivô** com a geometria do anel; o segmento seguinte é filho delas. Assim, rotacionar o cotovelo move antebraço, punho, mão e arma sem correção manual (P0).
- Âncoras (`socket_*`, `eye_socket_*`, `tablet_grip_*`, `blade_tip_*`) são partes **sem cubos**, usadas só para ler a posição no mundo (§6).
- Contagem: 117 partes nomeadas (inclui âncoras). `AGENTS.md`/D-009: nomes únicos — todos acima são únicos.

### 5.2 Peças com pais/funções (resumo de geometria)

A geometria de cada peça está em §4. Tabela de rig com pivôs, eixos e limites a seguir.

### 5.3 Tabela de pivôs (coordenadas absolutas BB, pose neutra)

Limites são **seguros** (fora deles há interseção ou quebra de silhueta) [HIP até o blockout]. "Controladores" usa as siglas de clips do §15: APR (apresentação/revelação), LOC (locomoção), DEF (defesa/dano), ATK (ataques A–G), POW (poderes), IDL (idle/procedural), DTH (morte).

**Tronco, cabeça e rosto**

| Parte | Pai | Função | Pivô (X; Y; Z) | Eixos | Limites seguros | Controladores | Risco de colisão |
|---|---|---|---|---|---|---|---|
| `root` | — | origem | (0; 0; 0) | — | — | (posição só por entidade) | — |
| `pelvis` | root | quadril | (0; 22; 0) | X, Y, Z + POS | X ±20, Y ±20, Z ±10; POS Y −6…+2 | LOC, ATK, DEF, DTH | túnica × coxas |
| `torso` | pelvis | tronco | (0; 29; 0) | X, Y, Z | X −15…+35, Y ±40, Z ±15 | todos | braços inferiores × túnica |
| `abdomen` | torso | torção parcial | (0; 29; 0) | X, Y | X ±15, Y ±25 | ATK, LOC | placas × braços inferiores |
| `ribcage` | torso | geometria (estático) | (0; 29; 0) | — | — | — | — |
| `sternum_seal` | torso | selo | (0; 37; −4,25) | SCALE | 1,0–1,15 | POW, DEF | — |
| `neck` | torso | pescoço | (0; 43; 0) | X, Y, Z + POS | X −15…+25, Y ±35, Z ±10 | APR, IDL, ATK | capuz × asas fechadas |
| `head` | neck | crânio | (0; 45; 0) | X, Y, Z + POS | X −25…+25, Y ±45 (soma com neck ≤ 70), Z ±15 | APR, IDL (olhar), todos | capuz × ombros superiores |
| `hood` e filhos | head | capuz | (0; 45; 0) | (estático) / `hood_crown` X ±2 | — | DTH | asas fechadas |
| `hood_shadow_inner` e filhos | head | cavidade | (0; 45; 0) | estático | — | — | — |
| `forehead_diamond_seal` | head | selo frontal | (0; 51,75; −3,75) | SCALE | 1,0–1,15 | APR, POW | — |
| `brow_left` / `brow_right` | head | sobrancelha | (±1,5; 51; −3,75) | Z | bind ±10, ±8 a mais | IDL, APR | máscara (máscara cobre na fase 0) |
| `nose_bridge` | head | nariz | (0; 48,5; −4) | — | — | — | máscara (máscara tem recorte no nariz, §7.3) |
| `eye_socket_left` / `_right` | head | âncora olhos | (±1,75; 49,5; −3,6) | — | — | — | — |
| `mouth_interior` | head | boca | (0; 46,5; −2,5) | — | — | — | — |
| `teeth_upper` | head | dentes superiores | (0; 47; −3,35) | — | — | — | máscara |
| `jaw` | head | mandíbula | (0; 46,5; +2,0) | X | 0…+40 (morte +55) | APR, IDL, ATK (gritos), DTH | `neck_ring_3` ao abrir > 40 |
| `teeth_lower` | jaw | dentes inferiores | (0; 46; −3,35) | — | — | (herda jaw) | `teeth_upper` (oclusão em 0°) |
| `face_mask_left` | head | meia-máscara | (+3,5; 48,5; −4,25) | X, Y, Z + POS + SCALE | ver §7.3 | APR somente | `hood_side_left` durante a saída |
| `face_mask_right` | head | meia-máscara | (−3,5; 48,5; −4,25) | X, Y, Z + POS + SCALE | ver §7.3 | APR somente | `hood_side_right` |

**Braços** (lado esquerdo; o direito espelha X e inverte o sinal de `zRot`/`yRot`). Pose neutra: braço pendente com abdução de 15° (top), 25° (mid), 10° (low); cotovelo 0°; lâmina para baixo-frente.

| Parte | Pai | Pivô esquerdo (X; Y; Z) | Pivô direito | Eixos | Limites seguros | Controladores | Risco |
|---|---|---|---|---|---|---|---|
| `arm_top_*` (ombro) | torso | (+10,5; 41; −1,0) | (−10,5; 41; −1,0) | X, Y, Z | X −180…+60; Z abdução 0…120 (lado esq. negativo); Y ±60 | APR, LOC, DEF, ATK, POW | capuz/asa raiz em X < −150 com abdução < 20 |
| `upper_arm_top_*` | arm_top | = ombro | = ombro | (rolagem Y) | Y ±45 | ATK | — |
| `elbow_top_*` | upper_arm_top | (+10,5; 32,5; −1,0) | (−10,5; 32,5; −1,0) | X | 0…−145 (flexão) | ATK, DEF, APR | antebraço × tórax em flexão > 120 com adução |
| `forearm_top_*` | elbow_top | = cotovelo | = cotovelo | (rolagem Y) | Y ±90 | ATK | — |
| `wrist_top_*` | forearm_top | (+10,5; 23; −1,0) | (−10,5; 23; −1,0) | X, Y, Z | X ±80, Z ±45, Y ±90 | ATK, DEF | — |
| `hand_top_*` | wrist_top | (+10,5; 21,5; −1,0) | (−10,5; 21,5; −1,0) | — | — | — | — |
| `sword_top_*` | hand_top | grip (+10,5; 20; −1,0) | (−10,5; 20; −1,0) | X (ajuste de pegada) | ±20 | APR (desembainhar) | túnica/coxa na pose neutra (ver §5.5) |
| `arm_mid_*` | torso | (+11,5; 35; +0,5) | (−11,5; 35; +0,5) | X, Y, Z | X −160…+70; Z 0…110; Y ±70 | idem | asa semiaberta em X > +40 |
| `elbow_mid_*` | upper_arm_mid | (+11,5; 26,5; +0,5) | (−11,5; 26,5; +0,5) | X | 0…−145 | | |
| `wrist_mid_*` | forearm_mid | (+11,5; 17; +0,5) | (−11,5; 17; +0,5) | X, Y, Z | X ±80, Z ±45, Y ±90 | | |
| `hand_mid_*` | wrist_mid | (+11,5; 15,5; +0,5) | (−11,5; 15,5; +0,5) | — | | | |
| `sword_mid_*` | hand_mid | (+11,5; 14; +0,5) | (−11,5; 14; +0,5) | X | ±20 | | coxas em pose neutra |
| `arm_low_*` | torso | (+10,0; 30; +1,5) | (−10,0; 30; +1,5) | X, Y, Z | X −130…+50; Z 5…90; Y ±60 | idem | `robe_left`/`robe_right`, coxa, `sword_mid_*` |
| `elbow_low_*` | upper_arm_low | (+10,0; 21,5; +1,5) | (−10,0; 21,5; +1,5) | X | 0…−140 | | |
| `wrist_low_*` | forearm_low | (+10,0; 12; +1,5) | (−10,0; 12; +1,5) | X, Y, Z | X ±80, Z ±45, Y ±90 | | |
| `hand_low_*` | wrist_low | (+10,0; 10,5; +1,5) | (−10,0; 10,5; +1,5) | — | | | |
| `sword_low_*` | hand_low | (+10,0; 9; +1,5) | (−10,0; 9; +1,5) | X | ±20 | | **chão** em pose neutra — ver nota |

Nota de pose neutra x chão: com braços pendentes, a ponta da `sword_low_*` (17 px abaixo do punho = Y −8) atravessaria o chão. Por isso a **bind pose real** tem `elbow_low_*` em −70° e `wrist_low_*` em −30°: a lâmina inferior fica horizontal à frente da coxa em Y ~12. Da mesma forma `elbow_mid_*` −45° e `elbow_top_*` −20°. A tabela dá pivôs com o braço reto apenas para montar a geometria; **a pose neutra de exportação é a da §5.4 (idle)**.

**Asas**

| Parte | Pai | Pivô esq. | Pivô dir. | Eixos | Limites | Controladores | Risco |
|---|---|---|---|---|---|---|---|
| `wing_root_*` | torso | (+4; 40; +5) | (−4; 40; +5) | X, Y, Z | Y fechada→aberta 0…85 lateral; Z 0…25 (subir); X ±20 | APR, LOC, ATK, DEF | ombro superior em abdução > 100 com asa fechada |
| `wing_arm_*` | wing_root | = root | = root | Y, Z | Y ±30 | idem | `hood_shell_back` |
| `wing_forearm_*` | wing_arm | (+4; 40; +19) em bind (ponta do arm) | (−4; 40; +19) | Y | 0…160 (fechar dobrando) | idem | `robe_back_upper` quando fechada |
| `wing_tip_*` | wing_forearm | ponta (+4; 40; +33) | (−4; 40; +33) | — | — | — | — |
| `wing_finger_1..4_*` | wing_forearm | (+4; 40; +19) leque | espelhado | Y, Z | abertura ±35 entre dedos | idem | dedos vizinhos |

Bind das asas = **fechadas**, apontando para trás (+Z) e dobradas para baixo; o hipotético "aberto" é alcançado por rotação. Sinal de `yRot` das asas: [HIP] confirmar no blockout.

**Pernas e túnica**

| Parte | Pai | Pivô esq. | Pivô dir. | Eixos | Limites | Controladores | Risco |
|---|---|---|---|---|---|---|---|
| `leg_*` | pelvis | (+3; 22; 0) | (−3; 22; 0) | X, Z | X −80…+40; Z ±20 | LOC, ATK, APR | `robe_front` |
| `knee_*` | leg | (+3; 12; −0,5) | (−3; 12; −0,5) | X | 0…+120 | LOC | — |
| `shin_*` | knee | = joelho | = joelho | — | — | — | — |
| `ankle_*` | shin | (+3; 2; +0,5) | (−3; 2; +0,5) | X, Z | X ±40; Z ±15 | LOC | chão |
| `foot_*` | ankle | = tornozelo | = tornozelo | — | — | — | — |
| `robe_front` | pelvis | (0; 22; −4) | — | X, Z | X −45…+10; Z ±8 | LOC, ATK, IDL | coxas, braços inferiores |
| `robe_back_upper` | pelvis | (0; 22; +4) | — | X | −10…+40 | LOC, ATK | asas fechadas |
| `robe_back_lower` | robe_back_upper | (0; 12; +4,5) | — | X | −10…+30 | LOC | — |
| `robe_left` / `robe_right` | pelvis | (+5,5; 22; 0) | (−5,5; 22; 0) | X, Z | X ±25; Z abertura 0…20 | LOC, ATK | `forearm_low_*` |

**Tábua**

| Parte | Pai | Pivô | Eixos | Limites | Controladores |
|---|---|---|---|---|---|
| `tablet_anchor` | torso | (0; 32; −8) | X, Y, Z + POS | livre durante APR | APR |
| `mandament_tablet` | tablet_anchor | (0; 32; −8) | — | — | APR |
| `tablet_half_left` / `_right` | mandament_tablet | (±0,25; 22; −8) (base, centro inferior) | X, Y, Z + POS + SCALE | livre | APR |

### 5.4 Poses de teste (ângulos-chave, lado esquerdo; direito espelhado salvo indicação)

Valores [HIP] para o blockout; servem como primeiro chute verificável.

| Pose | Tronco / cabeça | Top | Mid | Low | Asas | Pernas |
|---|---|---|---|---|---|---|
| **Estátua** (código) | torso 0; neck +10; head +18 (olha para baixo) | top: braço (8, 48, −16), cotovelo −95, punho (90, 0, −15) — resolvido numericamente para `tablet_grip_*` cair em (±5,5; 30; −8) | mid: braço (22, 0, 14), cotovelo −10, punho 70 (pendente atrás do corpo, lâmina para trás) | low: braço (28, 0, 16), cotovelo −10, punho 88 | fechadas, pontas acima dos ombros | retas |
| **Idle de combate** (código = BASE) | torso X +6; neck −4; head (−2, 0, 4) | braço (−15, −90, −105), cotovelo −70, punho −10 → braço aberto, antebraço para cima, lâmina alta para fora (coroa) | braço (−20, −90, −70), cotovelo −40, punho −20 → lâmina nivelada para fora e à frente | braço (−30, 0, −32), cotovelo −55, punho 15 → lâmina baixa à frente | semiabertas | joelhos +14, quadril (−8, 0, −7), pelve 1 px abaixo |
| **Guarda total** (código) | torso X +10 | braço (−150, 0, −25), cotovelo −40, punho (0, 0, 35) | braço (−20, −90, −75), cotovelo −60, punho −30 | braço (−55, 30, −6), cotovelo −60, punho (0, 0, 30) | root Y −28 | joelhos +25 |
| **Ataque amplo** (pico do giro C) | torso Y −35, abdomen Y −10 | X −90, Z −80 | X −90, Z −90 | X −70, Z −75 | abertas | quadril Z ±5 |
| **Salto (ápice)** | torso X +25, pelvis POS Y +2 | X −160, Z −20 (lâminas acima da cabeça) | X −60, Z −70 | X −30, Z −40 | abertas, Z 20 | joelhos +90, coxas −60 |
| **Revelação (fim)** | ver §7.4 tick 110 = idle de combate + jaw +30 | | | | abertas | |

### 5.5 Matriz de interferência a validar no blockout

| Par | Pose crítica | Regra |
|---|---|---|
| `sword_low_*` × coxas/`robe_front` | idle, Crescente (D) | lâmina inferior sempre a Z ≤ −6 (à frente) ou abaixo de Y 8 com abdução ≥ 15 |
| `forearm_low_*` × `robe_left`/`robe_right` | guarda total | `robe_left`/`robe_right` recebe Z +10 (abre) acompanhando |
| `sword_mid_*` × `sword_low_*` | idle | planos diferentes: mid horizontal externo, low diagonal interno; distância mínima 2 px |
| `arm_top_*` × `wing_root_*` | estocada E, protocolo | ombro superior com X < −150 exige asa ≥ semiaberta |
| `sword_top_*` × `hood_crown` | salto, Mandamento Cruzado | lâminas cruzam **à frente** (Z ≤ −10) da cabeça, nunca sobre o capuz |
| asas × `robe_back_upper` | fechadas | asas fechadas a ≥ 1 px de `robe_back_upper` |
| `jaw` × `neck_ring_3` | gargalhada | limite +40 |
| máscara × capuz | revelação | trajetórias §7.3 saem para a frente antes de girar lateralmente |

---

## 6. Sockets e âncoras

Partes sem geometria. Uso: no cliente, ler a matriz acumulada da parte (mesmo caminho de transformação usado para desenhar) para emitir partículas no ponto real; no servidor, as hitboxes **não** dependem delas (§17).

| Âncora | Pai | Posição BB (neutra) | Uso |
|---|---|---|---|
| `grip_*` (implícito = pivô de `sword_*`) | `hand_*` | ver §5.3 | prender arma |
| `blade_tip_top_left` … `blade_tip_low_right` (6) | `sword_*` | 17–19 px ao longo da lâmina | rastro, faísca de impacto, convergência de carga |
| `tablet_grip_left` / `tablet_grip_right` | `hand_top_*` | centro da palma, 1 px para dentro | alinhamento da tábua (§7.2) |
| `wing_root_*` (pivô) | `torso` | (±4; 40; +5) | pó ao abrir |
| `wing_tip_*` | `wing_forearm_*` | ponta | rastro de mergulho |
| `socket_sternum` | `torso` | (0; 37; −4,6) | feixes do protocolo, pulso de mana, hitbox de ponto fraco (só referência visual) |
| `forehead_diamond_seal` (pivô) | `head` | (0; 51,75; −4,0) | pulso de autoridade |
| `eye_socket_left` / `_right` | `head` | (±1,75; 49,5; −3,6) | brilho dos olhos; **não** emite os feixes térmicos (vêm da arena, ataque I) |
| `foot_*` (sola) | `ankle_*` | (±3; 0; −1) | poeira de passo |
| `hand_*` (palma) | `wrist_*` | pivô de `hand_*` | faísca de aparo próxima à mão |

Precedente no projeto: `PuppetThreadRenderLayer` (D-034) desenha os fios a partir de um ponto de ancoragem calculado no servidor (`getThreadAnchor`). Para partículas presas à animação, a âncora precisa da matriz acumulada da parte no cliente. [HIP] confirmar na implementação o caminho exato para ler essa transformação em 26.1.2; se não houver acesso limpo, calcular a posição da âncora no servidor a partir do tick do clip (mesmo princípio do D-034).


---

## 7. Disfarce, tábua e timeline mestre da revelação

### 7.1 Estátua dos Mandamentos (fase 0) — `architect_dormant_statue`

Não é outra criatura: é uma pose do mesmo modelo [ADP]. Corpo vertical, sem respiração; `neck` +10°, `head` +18° (olhando para baixo: o teto da cavidade do capuz esconde a face); máscara inteira e inerte (`#A19B91`); olhos e losango apagados (textura glow `_glow_dormant`, §8.5); asas fechadas; `arm_top_*` segurando a tábua; `arm_mid_*` e `arm_low_*` dobrados atrás das placas laterais da túnica e das asas (pose "Estátua", §5.4) — de frente só dois braços são visíveis [REF]. As armas dos braços ocultos ficam ocultas junto; as duas superiores ficam **atrás da tábua**, lâminas para baixo. Clip: loop de 200 ticks, todos os canais constantes; microvariação ambiental opcional = nenhuma no modelo (só partículas de poeira ambiente [OPC]). A entidade fica sobre um pedestal que é bloco da arena, não parte do modelo.

### 7.2 Tábua dos Mandamentos e pegada (P1)

- **Geometria:** `mandament_tablet` = `tablet_half_left` + `tablet_half_right`, cada metade 5×14×1,5 com sub-cubo 5,5×4×1,5 no topo (a tábua fica 1 px mais larga em cima), bordas quebradas e chanfros pintados. Linha de fratura central em zigue-zague pintada nas faces internas. Três linhas de glifos fictícios (adoração, louvor, prova da fé) [REF], traço de 1 px, sem texto real.
- **Transformação estável:** `tablet_anchor` é filho de `torso`, pivô (0; 32; −8). A tábua ocupa X −5,5…5,5, Y 22…38, Z −8,75…−7,25 — cobre o abdômen [REF]. Como é filha do torso, acompanha qualquer inclinação do corpo sem desalinhar das mãos.
- **Sockets `tablet_grip_left` / `tablet_grip_right`:** âncoras no centro da palma de `hand_top_*`. Na pose de estátua, devem cair em (±5,5; 30; −8) — as bordas laterais da tábua, a meia altura — com tolerância de ±0,25 px. A pose "Estátua" da §5.4 (ombro superior X −55, Z −8; cotovelo −65; punho Y medial 70 para a palma encarar a borda) é o ponto de partida; ajustar no blockout até a âncora coincidir. Orientação: palma voltada para o centro (±X), dedos fechados por cima da borda frontal, polegar por trás.
- **Regra:** a tábua **não** é filha das mãos; mãos e tábua alinham-se pela pose. Assim a tábua pode cair sozinha.
- **Revelação:** solta-se da pose (POSITION/ROTATION em `tablet_anchor` e nas metades), cai, quebra, e no fim recebe SCALE 0 (os restos viram partículas `architect_tablet_shard`). Nunca volta, nunca é escudo ou arma [REF ficha].

### 7.3 Máscara bipartida (P0)

- **Geometria:** cada metade é uma placa 3,5×6×0,5 cobrindo Y 45,5…51,5 (sobrancelhas, olhos, nariz e boca); `face_mask_left` em X 0…+3,5, `face_mask_right` em X −3,5…0. Profundidade **Z −4,5…−4,0**: a face traseira encosta na frente das sobrancelhas e do losango (Z −4,0), 0,5 px à frente da face do `head` (Z −3,5) e dos dentes (Z −3,6) — nenhuma superfície visível coplanar. O `nose_bridge` (frente Z −4,5) passa por um recorte de 0,5×3 px na borda central de cada metade (1×3 no total): a ponta do nariz fica rente à máscara, mas em faces perpendiculares — sem z-fighting.
- **Face verdadeira** modelada atrás desde o início; nunca há troca de textura.
- **Pivôs:** borda lateral externa, meia altura: `face_mask_left` (+3,5; 48,5; −4,25), `face_mask_right` (−3,5; 48,5; −4,25).
- **Ordem:** direita primeiro [REF ficha], esquerda depois.
- **Trajetória da direita** (ticks da timeline §7.4):
  - 34–36: vibração (`yRot` ±2 alternado a cada tick).
  - 36–40: "porta abrindo": `yRot` leva a borda central 35° para a frente/fora (sentido [HIP]); a borda sai do rosto antes de qualquer queda, sem atravessar o capuz.
  - 40–52: desprende: POSITION (−3; −10; −6) relativo ao bind, `zRot` +60, `xRot` +40.
  - 52–58: queda: POSITION (−6; −44; −10) (≈ chão à frente do pé direito), `zRot` +110.
  - 58: impacto → SCALE 1→0 em 2 ticks, evento de 6 `architect_tablet_shard` claros no ponto. A peça **cai inteira** e se fragmenta só por partícula.
- **Trajetória da esquerda:** espelhada, mais rápida ("estala" sob tensão): 44–46 vibração; 46–49 abre 35°; 49–58 POSITION (+3; −10; −6), `zRot` −70; 58–64 queda até (+6; −44; −10), `zRot` −120; 64 impacto, SCALE 0, 6 fragmentos.
- **Quadro icônico meio-a-meio** [REF]: ticks 40–46, metade esquerda ainda no rosto e energizada (glow `#DDF4FF`), metade direita já revelando olho negro com pupila coral.
- A máscara não volta. Na morte é a `jaw` que se parte (§15.3).

### 7.4 Timeline mestre `architect_awaken` (110 ticks = 5,5 s)

Clip **único**, sem loop, dono exclusivo de **todas** as partes enquanto toca (prioridade 1, §15.4); nenhum overlay roda durante ele. O nome `architect_awaken_cracks` (prompt detalhado) e os antigos `architect_reveal_arms`/`architect_reveal_wings` são **rótulos de trechos** desta timeline, não clips paralelos (C-09, C-16). Gatilho: jogador a ≤ 10 bl com linha de visão do pedestal, ou dano direto na estátua (servidor). Invulnerável durante a revelação; colisão já ativa. Partículas e sons disparados pelo servidor nos ticks indicados (eventos de rede §19).

| # | Ticks | Partes | Chaves (posição/rotação) | Partículas | Áudio | Estado resultante |
|---|---|---|---|---|---|---|
| 1 | 0–14 | `forehead_diamond_seal`, glifos da tábua | sem movimento; glow troca `_glow_dormant` → `_glow` no tick 6 (só losango e glifos acesos nessa textura intermediária [HIP]) | 2 `architect_mana_spark` lilás no losango | zumbido grave baixo | brilho fraco |
| 2 | 10–24 | `wrist_top_*`, tábua | microtremor `wrist_top_*` X ±1,5 a cada 2 t | `architect_stone_crack` nos dedos, 4 `architect_stone_dust` na tábua | 3 estalos secos (t12, t17, t22) | rachaduras |
| 3 | 22–34 | `neck`, `head` | `neck` X +10→−8; `head` X +18→−4; `head` POS Y +0,5 | pó na nuca | rangido longo | rosto sai da sombra; olhos da máscara acendem t30 |
| 4 | 34–58 | `face_mask_right` | §7.3 | 6 shards t58 | estalo agudo t36; tilintar t58 | metade direita caída; olho coral direito aceso t40 |
| 5 | 44–64 | `face_mask_left` | §7.3 | 6 shards t64 | estalo t46; impacto t64 | face verdadeira completa t49 |
| 6 | 50–62 | `torso`, `sternum_seal` | `torso` X 0→−6 (peito estufa) e POS Y +0,5; `sternum_seal` SCALE 1→1,15→1 | `architect_mana_pulse` no esterno t56 | baque grave t56 | anéis ciano dos 6 encaixes acesos (glow `_glow`) |
| 7 | 58–84 | `arm_mid_*`, `arm_low_*` e filhos | desdobramento **sequencial**: `arm_mid_right` 58–68, `arm_mid_left` 62–72, `arm_low_right` 68–78, `arm_low_left` 74–84; cada um vai da pose Estátua à Idle passando por *overshoot* de abdução +15° e volta nos últimos 3 t | 4 `architect_stone_chip` por ombro no início de cada um | 4 estalos de articulação (pitch 0,80/0,85/0,90/0,95) | quatro braços em guarda |
| 8 | 70–90 | asas (`wing_root/arm/forearm/finger_*`) | fechada → aberta total 70–82 (esquerda 2 t antes); raízes primeiro, hastes +2 t, dedos +4 t em leque; aberta → semiaberta 82–90 | 6 `architect_stone_dust` por asa nas juntas | atrito grave de placas + estalo t82 | asas semiabertas |
| 9 | 60–99 | `arm_top_*`, `tablet_anchor`, `tablet_half_*` | 60–66: mãos superiores soltam (`arm_top_*` Z −25); 60–72: tábua cai (POS Y −16, aceleração, `xRot` +25 tombando para frente); 72: impacto (base em Y 0); 72–80: metades separam (POS X ∓3, `zRot` ±25); 96–99: SCALE 1→0 | t72 12 shards + 10 dust em anel; t96 8 shards | queda pesada com eco t72 | tábua some |
| 10 | 86–110 | `jaw`, `arm_top_*`, seis `sword_*`, `head` | `jaw` 0→+30 (86–96) e mantém; `arm_top_*` 72–100 sobem à pose Idle; as seis lâminas assumem o idle até 106; `head` Z +6 (inclinação arrogante) | `architect_mana_trace` em cada ranhura, 2 t de defasagem entre lâminas (96–106) | gargalhada mineral 92–108 | **pose final = `architect_battle_idle` frame 0 com `jaw` +30**; o clip libera e o idle assume em 4 t; `jaw` volta a 0 em 6 t |

Variante curta (revelação no meio de combate) [OPC]: 60 ticks, eventos 1–2 pulados e defasagens do evento 7 reduzidas a 2 t.

---

## 8. Direção de arte, paleta, pixel art e UV

### 8.1 Princípios

Textura feita para Minecraft: clusters de pixels definidos, sombreamento em degraus com **quatro níveis por material** (sombra, base, luz, brilho), sem gradiente suave, ruído fotográfico ou PBR. Preto profundo só em cavidade do capuz, esclera, interior da boca e fundo das fendas — sem contorno preto geral. Rachaduras seguem o plano da face do cubo e continuam na face vizinha quando atravessam uma aresta.

### 8.2 Densidade

Padrão: 1 px de textura = 1 px de modelo. Exceção desejada: frente do `head`, dentes, máscaras e losango com **densidade 2×** para caberem duas fileiras de dentes com divisórias e as fendas. [HIP] o `CubeListBuilder` vanilla mapeia UV na proporção do cubo; densidade 2× exige modelar essas superfícies como cubos finos com o dobro das dimensões e escala 0,5 na parte (`PartPose` com escala, se disponível na 26.1.2 — confirmar) ou aceitar 1×. **Decisão:** testar 2× no blockout; se não houver caminho limpo, ficar em 1× com 3 dentes por fileira (1 px de dente + 1 px de divisória) e registrar a escolha em `DECISIONS.md`.

Padrão de pixels por material: pedra do rosto em clusters 2×2 com variação de 1 nível; granito do corpo em clusters irregulares de 2–4 px; basalto das armas quase liso (clusters grandes), contrastando com o corpo; túnica em faixas verticais de 2 px (pregas).

### 8.3 Paleta funcional (fonte única)

| Material | Sombra | Base | Luz | Brilho |
|---|---|---|---|---|
| Pedra cinza-azulada (face, capuz) | `#434852` | `#6E7582` | `#9BA3B0` | `#C3C9D2` |
| Granito do corpo / túnica | `#49474F` | `#77747A` | `#AAA4A0` | `#C9C3BC` |
| Fendas profundas | `#121217` | `#24232B` | `#49474F` (borda) | — |
| Vão do capuz, esclera, boca | `#050508` | `#0B0B10` | `#1C1C24` | — |
| Máscara inerte / dentes | `#6E6A67` | `#A19B91` | `#C7BFB0` | — |
| Máscara energizada (glow) | `#89C4E8` | `#DDF4FF` | `#FFFFFF` | — |
| Losango frontal (glow) | `#7E64C4` | `#D8C8FF` | `#F7F2FF` | — |
| Basalto das armas | `#15151D` | `#282831` | `#55515B` | — |
| Veios de mana (glow) | `#176A70` | `#38BDBA` | `#B6FFF0` | — |
| Pupila/íris (glow) | `#A91F32` | `#FF604B` | `#FFD09A` | — |
| Feixe térmico (VFX) | `#9E102B` | `#F02B42` | núcleo `#FFF0D9` | borda `#FF8A3D` |
| Poeira mineral (VFX) | `#625D5C` | `#9A9289` | `#C9BDA9` | — |
| Erro administrativo (VFX pontual) | — | `#FF2E3E` | — | — |

Acréscimos desta versão: brilhos `#C3C9D2` e `#C9C3BC` (4º nível pedido pelo prompt) e as cores da borda laranja e do erro, citadas sem valor nas fontes. **Separação obrigatória:** mana = ciano; autoridade = lilás; olhos = coral; calor = carmesim com núcleo quente; erro = vermelho só em pixels pontuais. A prancha conceitual (runas vermelhas no corpo, poderes magenta) não sobrescreve isto.

### 8.4 Aplicação por peça

| Peça | Aplicação |
|---|---|
| Capuz externo | pedra cinza-azulada; lascas em sombra nas bordas frontais; luz no topo da cunha |
| Interior do capuz | `#0B0B10` chapado, sem ruído |
| Face verdadeira | base `#6E7582`; 2 linhas de sombra `#434852` no topo (sombra do capuz) e sob as maçãs; linhas de expressão na bochecha e no queixo |
| Olhos | fenda `#0B0B10`; íris 1 px `#A91F32` na base e `#FF604B`/`#FFD09A` no glow |
| Fendas | `#121217` 1 px + luz `#9BA3B0` do lado oposto ao corte |
| Dentes | faces `#C7BFB0`, bordas `#A19B91`, divisórias e linha de oclusão `#24232B`; 2–3 linhas escuras de tensão abaixo do lábio inferior |
| Máscara | inerte `#A19B91/#C7BFB0` lisa, olhos esculpidos fechados, meio-sorriso calmo; energizada no glow `#DDF4FF` |
| Losango | base `#7E64C4`; glow `#D8C8FF` |
| Esterno | placa `#AAA4A0`, losangos gravados `#49474F`, circuitos ciano no glow |
| Encaixes dos braços | disco `#49474F` + anel ciano 1 px (glow) |
| Braços | placas externas 1 nível mais claras que as internas; cotovelo anel `#24232B` + 2 px ciano; antebraço com arranhões diagonais; dedos em 3 valores (base escura, face clara, ponta `#C9C3BC`); assimetrias §4.4 |
| Asas | placas alternando granito e basalto; borda de ataque 1 px `#9BA3B0`; pontas `#15151D`; fissuras curtas nas junções, sem membrana |
| Túnica | faixas de 2 px alternando base/sombra; pontas desgastadas; fraturas revelando `#24232B`; nada de costura ou tecido |
| Pernas | granito; crista frontal da canela em luz; joelho facetado |
| Tábua | `#AAA4A0`, glifos `#49474F`, 2–3 pontos `#625D5C` nas quinas; glifos lilás fracos no glow |
| Armas | §9.3 |

### 8.5 Emissivo

Pipeline real do projeto: `EyesLayer` + `RenderTypes.eyes(textura)` (`BasiliskEyesLayer`, `DragonGlowLayer` — este escolhe o `RenderType` por estágio). Plano: texturas glow com o mesmo UV, preto/transparente fora das regiões emissivas:

| Textura | Conteúdo | Quando |
|---|---|---|
| `architect_glow_dormant.png` | vazia | fase 0, recuperação/stagger (olhos "apagam") |
| `architect_glow_waking.png` | losango, glifos da tábua, olhos da máscara | revelação t6–t49 |
| `architect_glow.png` | íris, losango, anéis dos encaixes, ranhuras das lâminas | combate |
| `architect_glow_command.png` | o anterior + selo do esterno e veios completos | canalizações H/I/J, fase 2+ ao comandar |

Escolha por estado sincronizado (padrão `DragonGlowLayer`). "Luz correndo" em lâminas/veios é feita com partículas, não com textura animada.

### 8.6 Atlas e UV (256×256, único)

```text
  0        64        128       192       256
0 ┌─────────┬─────────┬───────────────────┐
  │ HEAD    │ HOOD    │ TORSO / RIBCAGE   │  head (face 2× se viável), jaw, dentes,
  │ face    │ shell + │ shoulder mounts   │  nariz, máscara L e R em ilhas
  │ máscaras│ cavity  │ esterno           │  separadas (sem espelho)
64├─────────┼─────────┼─────────┬─────────┤
  │ ARM TOP │ ARM TOP │ ARM MID │ ARM MID │  cada braço: ombro, upper, cotovelo,
  │ LEFT    │ RIGHT   │ LEFT    │ RIGHT   │  antebraço, punho, mão — ilha própria
128├─────────┼─────────┼─────────┴─────────┤
  │ ARM LOW │ ARM LOW │ WINGS L + R       │
  │ LEFT    │ RIGHT   │ dedos e placas    │
176├─────────┴─────────┼─────────┬─────────┤
  │ PELVIS, ROBE ×5,  │ LEGS L/R│ TABLET  │
  │ ABDOMEN           │ + FEET  │ 2 metades│
216├───────────────────┴─────────┼─────────┤
  │ SWORDS ×6 (base comum, ilhas │ NECK,   │
  │ próprias: lascas/runas)      │ reserva │
256└──────────────────────────────┴─────────┘
```

Regras: margem mínima de 2 px entre ilhas; face, máscaras e braços nunca espelhados; armas com geometria-base comum e ilhas próprias. Atlas de armas separado (128×128) foi descartado (C-10): exigiria passe de render extra. Gerar geometria e atlas pelo mesmo script em `tools/`, como nos outros mobs, mantém as duas coisas coerentes [HIP: decidir na fatia de modelo].

---

## 9. As seis cimitarras

### 9.1 Modelo-base

Basalto esculpido: lâmina curva larga na base, afinando na ponta; dorso espesso irregular com 1–2 quebras angulares; fio mais claro; ranhura de mana em ~60 % do comprimento; cabo curto escuro; pomo facetado; guarda mínima de duas pontas de pedra, sem cruzeta. Cinco cubos: `pommel` 1,5×1,5×1,5; `grip` 1×4×1 (dentro da mão; o polegar cruza por fora); `guard` 3×1×1,5; `blade_base` 1×7×3; `blade_tip` 1×(L−13)×2 inclinado pelo ângulo de curvatura (a curva é feita por dois cubos inclinados). Orientação no bind: lâmina ao longo de −Y local (pendente da mão), fio para −Z. Pega: pivô `sword_*` no centro do `grip` = centro da palma.

### 9.2 Variações

| Arma | Comprimento (px) | Curvatura | Largura base | Lascas / marcas | Papel |
|---|---:|---:|---:|---|---|
| `sword_top_right` | 20 | 14° | 3 | runa central completa, fio mais claro | estocadas, aberturas (dominante) |
| `sword_top_left` | 19 | 20° (mais fechada) | 3 | fratura pequena na base | descendentes, contra-ataques |
| `sword_mid_right` | 18 | 14° | 3,5 | dorso grosso | bloqueios, horizontais, pressão |
| `sword_mid_left` | 18 | 10° | 2,5 | ponta alongada | fintas, precisão |
| `sword_low_right` | 17 | 26° (pronunciada) | 3 | ponta curta | varreduras baixas |
| `sword_low_left` | 17 | 18° | 3 | lasca no fio | retornos, tesoura |

### 9.3 Textura e mana

Basalto `#282831/#15151D/#55515B`; fio 1 px `#55515B` com pontos `#9BA3B0`; ranhura `#15151D` com 1–2 px ciano no glow. Preparação: a luz "corre" da guarda à ponta por partículas `architect_mana_trace` ao longo da lâmina [ADP].

### 9.4 Mãos

Pose fechada (padrão, geometria fixa). Pose aberta de guarda [OPC]: exigiria dedos como partes; na v1 a "mão aberta" é só o punho girado. A arma nunca atravessa a palma porque o `grip` ocupa o vão entre palma e dedos na geometria.

---

## 10. Postura, movimentação e navegação

Regras gerais: **cabeça vira primeiro (0 t), torso +3 t, pelve/pés +6 t**; giro máximo do corpo 12°/tick em combate; braços nunca balançam como humanos — mantêm guarda com micro-correções de fase própria; o deslocamento no jogo coincide com o tick de contato do pé (velocidade aplicada só nos ticks de apoio, como pede o critério de completude). Valores são [ADP]/[HIP].

### 10.1 Idle de combate — `architect_battle_idle` (loop 60 t)

| Ticks | Ação |
|---|---|
| 0–10 | pose estável (§5.4 Idle); `head` Z +4 inclinado; seis lâminas em três planos |
| 10–20 | `arm_top_right` (dominante) ajusta X −3°; `arm_mid_*` oscilam ±3° defasados 5 t; pulso discreto no esterno (partícula 1× `architect_mana_spark`) |
| 20–30 | `pelvis` Z +3 (peso na perna esquerda), `knee_right` +5; `wing_root_right` +4° compensa; `robe_*` atrasam 3 t |
| 30–40 | `head`/`neck` seguem o alvo (procedural, atraso 2 t); `jaw` 0 (sorriso fechado) |
| 40–50 | `elbow_low_*` corrigem −4° e voltam; runas de 2 lâminas piscam (partícula) |
| 50–60 | retorno suave à pose base |

Máquina mineral quase imóvel: amplitudes ≤ 4°, sem "respiração" de peito.

### 10.2 Caminhada — `architect_walk` (loop 24 t; 0,16 bl/t ≈ 1,9 bl por ciclo)

| Ticks | Pernas | Corpo | Braços | Asas / túnica | Evento |
|---|---|---|---|---|---|
| 0–3 | `leg_left` X 0→−25, `knee_left` +30 | `pelvis` Y +4 (transfere peso) | guarda fixa | `robe_left` X −10 | — |
| 3–6 | `leg_left` −25→−10, `knee_left` +30→+5: sola plana no chão no t6 | `torso` Y +3 (atraso) | `arm_low_right` corrige −4° (estabiliza guarda baixa) | asas inclinam −3° contra o deslocamento | **t6 passo**: `architect_heavy_footstep` + som |
| 6–9 | `leg_right` passa: 0→−25, `knee_right` +30 | `pelvis` Y +4→−4 | idem espelhado | `robe_right` −10; `robe_back_lower` 3 t atrás | — |
| 9–12 | `leg_right` assenta | estabiliza | — | — | — |
| 12–24 | espelhado (passo direito no t18) | | | | **t18 passo** |

### 10.3 Corrida/aproximação — `architect_run` (loop 18 t; 0,28 bl/t)

Entrada 6 t (joelhos +35, `torso` X +22, `pelvis` POS Y −2). Ciclo: uma passada por perna (9 t), contato pesado nos t4 e t13 (`architect_heavy_footstep` maior). `arm_top_*` X +30 recolhendo lâminas para trás; `arm_mid_*` colados aos flancos (Z −15); `arm_low_*` X −40 à frente sem cruzar as coxas (Z ≤ −6). Asas semiabertas 60 %. `robe_back_*` X +25. Saída: 8 t (pé trava, tronco estabiliza) — **nunca** emenda direto num golpe: depois da trava começa o telegráfico do ataque.

### 10.4 Strafe — `architect_strafe_left` / `_right` (loop 16 t; 0,12 bl/t)

0–3 pé do lado do movimento desloca (`leg` Z ∓15), cabeça fixa no jogador; 3–6 o outro pé acompanha sem cruzar; 6–10 `torso` Y ±8 para o alvo, lâminas do lado oposto avançam (X −10) cobrindo a abertura; 10–16 estabiliza. Contatos nos t3 e t6. A versão direita espelha trajetória, não UV.

### 10.5 Recuo — `architect_backstep` (ação 16 t; 1,5 bl)

0–4 `arm_top_*` cruzam parcialmente (Y medial 30), asas fecham 30 %; 4–8 pé traseiro recua (deslocamento aplicado 5–8), quadril desloca; 8–11 pé dianteiro acompanha (deslocamento 9–11), peito voltado ao jogador; 11–16 braços voltam à guarda. Sem deslize: velocidade zero fora dos ticks de apoio.

### 10.6 Pivô / meia-volta — `architect_pivot_left` / `_right` (12 t para 90°, 20 t para 180°)

1. 0–2: cabeça gira até 45° e fixa o alvo. 2. 2–5: ombros do lado da rotação abrem (Z ±20), os opostos recuam (X +20) para não atravessar o corpo. 3. 3–8: torso gira até 60° à frente da pelve. 4. 6–12: pelve e pé de apoio pivotam. 5. 8–14: pé livre reposiciona (contato no fim). 6. Asas: a externa abre +20° como contrapeso; as seis armas reassumem três alturas no fim. Proibido girar 180° instantaneamente; se o jogador está atrás, pivô completo antes de qualquer combo.

### 10.7 Bote curto — `architect_short_lunge` (22 t; 2–3 bl)

| Fase | Ticks | Descrição |
|---|---|---|
| Preparação | 0–8 | joelhos +40, `pelvis` POS Y −2, braços recuam em alturas distintas (top X +20, mid Z −30, low X +15), asas comprimem (root −10°) |
| Avanço | 8–14 | pé traseiro impulsiona; deslocamento 2–3 bl distribuído nos 6 t (sem teleporte); `torso` X +25; asas abrem 30 %; **sem dano** |
| Trava | 14–16 | pé dianteiro toca (contato t14); torso continua 2 t por inércia (X +30→+15) |
| Recuperação | 16–22 | volta à guarda; o golpe seguinte começa **depois**, com telegráfico próprio |

### 10.8 Salto — `architect_jump` (24 t até o ápice)

0–8 compressão (`knee_*` +90, `leg_*` −60, `pelvis` POS Y −4); top erguem lâminas (variante ofensiva) ou recolhem (variante de reposicionamento); asas abrem em impulso 6–10; 10–12 pés deixam o chão (direito 1 t depois do esquerdo), impulso vertical até 3 bl ou diagonal até 5 bl; 12–24 subida com asas totalmente abertas, `torso` X +25; 20–24 asas recolhem 50 % e lâminas alinham para a queda. Sem voo sustentado.

### 10.9 Queda/mergulho — `architect_fall` (loop até tocar o chão)

`torso` aponta para a trajetória (X +35), `knee_*` +60 recolhidos, seis lâminas convergem para baixo-frente (sem se cruzar), asas fechadas 50 %, `robe_*` atrasados para cima. Partículas: fragmentos pequenos e `architect_mana_trace` nas `wing_tip_*`.

### 10.10 Aterrissagem — `architect_land` (12 t)

t0 solas tocam primeiro; 0–4 `knee_*` +70, `pelvis` POS Y −3, `torso` X +20; 2–6 asas batem **uma** vez (abrem 20° e fecham); 2–8 `arm_low_*` abrem Z ±15 para equilíbrio; 6–12 recupera para idle. `architect_heavy_footstep` ×2 + anel de poeira rente ao chão; tremor de câmera [OPC, configurável]. Nenhum ataque antes do fim.

### 10.11 Navegação

Colisão 1,55 × 3,35 passa em vãos de 2×4 bl; asas e armas fora da colisão. Sem espaço lateral para abrir asas (parede a < 3 bl dos lados) → variantes com asas a 50 % e IA não escolhe Roda (C). Teto livre < 6 bl → sem Queda da Gárgula (F) nem salto alto. Sem voo; gravidade normal; salto balístico. Se o pathfinding não alcança o jogador: reposicionamento a pé, poder ambiental ou F (se houver espaço); **nunca teleporte**.

---

## 11. Defesa, resistência e vulnerabilidades

### 11.1 Constituição pétrea (parâmetros, não imunidade)

- Imune: afogamento, Veneno, Wither, queda < 6 bl (asas amortecem) [ADP de C-02]. **Não** imune a dano físico, fogo, explosões ou projéteis.
- Resistência a repulsão alta (parâmetro, ex.: 0,9).
- Golpes abaixo do limiar de postura não cancelam ataques; geram só `hurt_small` como overlay de tronco/braço.
- Feedback de impacto: veio ciano mais próximo pulsa (partícula), lascas saem do ponto atingido. Rachaduras acumuladas por textura alternativa na fase 3 [OPC].

### 11.2 Guardas

Entrada na guarda total: top sobem primeiro, mid abrem +3 t, low fecham +6 t (transição 10 t).

| Guarda | Pose (lado esq.; dir. espelhado) | Orientação do fio | Cobre | Deixa vulnerável | Uso |
|---|---|---|---|---|---|
| `architect_guard_full` | top X −110, Z −20, cotovelo −95, punho Y medial 40 (diagonais altas); mid X −80, Z −60, cotovelo −60, punho Y lateral 20 (horizontais à frente dos flancos); low X −50, Z −10, cotovelo −90 (cruzadas no abdômen, esq. sobre dir.) | fios para fora (−Z/lateral) | cone frontal 120°, três alturas | costas, laterais > 60°, área acima da cabeça | padrão defensivo |
| `architect_guard_high` | top em V invertido (X −130, Z −35); mid mantêm horizontais; low não passam do abdômen (X ≥ −40) | fios para cima/fora | cabeça, peito alto | pernas e abdômen | contra golpes altos e saltos |
| `architect_guard_mid` | mid em duas diagonais horizontais afastadas 3 px do torso; top cobrem a cabeça sem cruzar; low prontos à frente (X −30) | fios laterais | flancos | ataques baixos, costas | contra quem circula |
| `architect_guard_low` | low cruzados à frente de abdômen e quadril (X −60); mid abertos; top protegem a face | fios para baixo/fora | abdômen, quadril | golpes altos | contra aproximação baixa |

Nenhuma guarda cobre 360°. Redução de dano frontal só com guarda ativa (parâmetro, ex.: −50 %).

### 11.3 Aparo — `architect_parry_left` / `_right` (ação 24 t)

| Ticks | Ação |
|---|---|
| 0–4 | `arm_mid_*` do lado atacado leva a lâmina à frente (X −80, Z −20); `arm_top_*` do mesmo lado aponta a lâmina ao atacante |
| **5–9** | **janela de aparo** (5 t): um golpe corpo a corpo frontal recebido nela é anulado |
| contato | 2–3 faíscas ciano + microfragmentos no `blade_tip_mid_*`, anel de pressão; som seco pedra-pedra |
| 10–16 | se aparou: réplica — `arm_top_*` do lado **oposto** faz diagonal curta descendente; hitbox ativa 13–14 (arco 2,5 bl, cone 70°) |
| 17–24 | recuperação; volta à guarda |

Condições: só sai de idle/guarda/locomoção (nunca de ataque ou recuperação); ataque frontal (cone ±60°); cooldown 100 t (F1), 80 t (F2), 120 t (F3). Falha (jogador não atacou na janela): pula a réplica e volta à guarda no t14 — nunca espera indefinidamente. Após aparo bem-sucedido, a réplica tem recuperação de 8 t sem guarda: vulnerável. Ignoram/quebram o aparo: costas e laterais > 60°, explosões, área, projéteis, dano ambiental, e golpe acima do limiar de postura (quebra + stagger). Máximo **2 aparos seguidos**; o 3º ataque frontal em 60 t não é aparado.

### 11.4 Deflexão de projéteis — `architect_projectile_deflect` (ação 14 t)

0–3 `arm_mid_*` cruzam à frente do torso; `wing_root_*` do lado exposto inclina 25°; 3–8 janela de deflexão; projétil leve (flecha comum, bola de neve, ovo) é desviado para o lado do braço de cima com faíscas no ponto real de contato; 8–14 volta à guarda. Cooldown 40 t. Projéteis fortes (tridente, flecha de besta perfurante, projétil explosivo, bola de fogo) **não** são anulados: causam dano (reduzido pela guarda, se ativa) e somam postura em dobro.

### 11.5 Dano localizado e stagger

- `architect_hurt_small` (overlay 8 t): braço/ombro mais próximo do golpe recua 6°; não interrompe ataque.
- `architect_hurt_heavy` (overlay 14 t): arma do braço atingido desvia 10°, cotovelo recua, `torso` 4°, veio pisca; não interrompe ataque, mas soma postura.
- `architect_stagger` (ação 30 t, prioridade 3): joelhos +30, asas abrem em reflexo, braços se afastam (Z ±40), esterno exposto, glow → `_glow_dormant`. Janela de punição real.
- `architect_arm_disrupted` [OPC] (overlay 12 t): um braço perde alinhamento e volta.
- **Postura:** golpes ≥ limiar (parâmetro, ex.: 8 de dano) somam 100 %; menores, 25 %; projéteis fortes e acertos no esterno em canalização, 200 %. Postura cheia → stagger. Após stagger, imune a novo stagger por 200 t. Decai 10 %/s sem receber dano. Dano fraco nunca causa stagger sozinho.
- O jogador percebe a guarda quebrada por: som de rachadura distinto, faíscas `architect_mana_error` no esterno, braços abertos e olhos apagados.

### 11.6 Pontos de vulnerabilidade

| Situação | Sinal | Efeito |
|---|---|---|
| Recuperação de ataque | braços abertos, glow apagado 6 t | dano normal, sem aparo |
| Costas com asas totalmente abertas (C, F) | asas abertas | dano +25 % (parâmetro) |
| Esterno em canalização (H, I, J) | selo pulsando forte | acerto frontal no torso (altura 2,1–2,5 bl) dano +50 % e postura 200 % |
| Braço/arma atingido em ataque | braço desvia | só visual na v1 |
| Cabeça | — | **não** é ponto fraco permanente (evita ignorar o combate) |

---

## 12. IA, fases e lógica de combate

### 12.1 Princípios

Duelista mecânico e administrador: mede distância, protege ângulos, reconhece flancos, alterna alturas. Seleção ponderada pela situação (§12.3) com penalidade de repetição (o mesmo ataque não sai 2× seguidas; peso −50 % se usado nos últimos 3). Um ataque físico por vez. Um poder ambiental pode coexistir com locomoção, mas **nunca** golpe físico e zona ambiental cobrindo o mesmo espaço, nem duas zonas ambientais inevitáveis sobrepostas. Cooldown global entre ataques: 10 t (F1), 8 t (F2), 6 t (F3), somado à recuperação do próprio ataque. Considera: espaço livre (asas/teto), posição dos emissores, linha de visão e se há rota real de esquiva.

### 12.2 Fases

| Fase | Faixa | Personalidade | Ataques | Mudanças |
|---|---|---|---|---|
| 0 — Monumento | não iniciado | imóvel, sem perseguir | — | gatilho → `architect_awaken` |
| 1 — Duelista de Seis Lâminas | 100–70 % | cerimonial; ensina as três alturas | A, B, C, D, E, G; deflexão | asas semiabertas; aparos ocasionais |
| 2 — Administrador do Templo | 70–35 % | combina lâmina e arena | F1 + F, H, I | transição `architect_phase_enrage` (40 t, invulnerável, empurrão leve); glow `_command` ao comandar; asas abertas com mais frequência |
| 3 — Administrador em colapso | < 35 % | desesperado, arrogância ferida | todos + combos encadeados (dois físicos com pausa de 8 t antes do último golpe); J se habilitado | transição com gargalhada; `jaw` +12 em repouso; `torso` +8 curvado; veios piscam irregulares; recuperações +30 % |

Percentuais são parâmetros [ADP]. Transição de fase espera o ataque atual terminar. Primeira entrega (§20) inclui A–F e G; H e I dependem da arena.

### 12.3 Matriz de decisão por posição

| Situação | Prioritárias | Proibidas |
|---|---|---|
| Frontal curta (≤ 3 bl, cone ±60°) | A, B, G (se o jogador ataca), D | F, I, E |
| Colado ao quadril (≤ 1,5 bl) | D, backstep + A, guarda baixa | B, E |
| Média (3–7 bl) | bote + A, E, C (se ≥ 2 jogadores perto) | G |
| Recuo em linha reta | E, corrida + A, H (F2+) | C |
| Flanco (60–120°) | strafe + pivô, guarda média, C | E, B |
| Costas (> 120°) | pivô com asa cobrindo, depois combo | qualquer golpe instantâneo para trás |
| No ar (> 1,5 bl acima do chão) | D, salto curto, F (anunciada) | C |
| Muito distante (> 12 bl) | I, H (F2+), corrida | A–E |
| Pillaring (parado ≥ 3 bl acima, sem rota) | F se houver espaço, I | — |

Foco: agressor recente; troca para o mais próximo se o atual sair do leash (§19). Multiplayer: um ataque por vez; com ≥ 2 jogadores a ≤ 4 bl, C ganha peso.

---

## 13. Catálogo de ataques

### 13.0 Convenções

- 20 ticks = 1 s. **Preparação** = leitura do jogador; **ativo** = ticks em que o volume de dano existe; **recuperação** = janela de punição.
- Braços: `sup-L/R` = `arm_top_*`, `med-L/R` = `arm_mid_*`, `inf-L/R` = `arm_low_*`.
- Direção, alvo e sentido são **fixados no início do telegráfico** e não mudam depois (sem correção de mira após o disparo começar).
- Hitboxes são volumes do servidor relativos à posição/orientação da entidade no início do golpe (§17). Formas: **ARCO** (setor de anel com altura mín./máx.), **CORREDOR** (caixa orientada), **VOLUME**, **ANEL**, **FEIXE**.
- Dano: parâmetro relativo ao "dano base" (DB) do chefe, a balancear na fatia de combate. Nenhum número final.
- Alturas do volume em blocos acima do pé do chefe: baixa 0–1,1; média 1,0–2,2; alta 1,8–3,4.
- Cada clip de animação usa **exatamente** os ticks desta seção (§15).

### 13.A Decreto das Seis Lâminas — `architect_attack_sixfold_edict` (38 t)

1. **Função:** combo principal corpo a corpo; ensina as três alturas. 2. **Fases:** 1–3.
3. **Telegráfico:** t0–5 as seis armas recuam a alturas diferentes (pose de "leque recolhido"); seis cliques curtos ascendentes; ranhuras acendem em sequência (trace).
4–7. **Sequência, trajetórias e hitboxes:**

| Ticks | Corpo | Braço / trajetória | Ativo | Hitbox |
|---:|---|---|---|---|
| 0–5 | pés firmam; `torso` X −4; cabeça fixa | todas recuam: top X +20, mid Z −40, low X +25 | — | — |
| 6–10 | ombro alto esq. avança; `torso` Y −10 | `sup-L` diagonal descendente: de alto-esquerda (X −150, Z −40) a baixo-direita (X −30, Z −5, Y medial 30) | **8–9** | ARCO alto→médio, raio 3,0, setor −20°…+50° (do lado esq. para o centro), altura 1,2–3,4 |
| 10–14 | `torso` gira para a direita (Y +15) | `med-R` horizontal na altura do peito, de fora para dentro (Y de lateral 70 a medial 20) | **12–13** | ARCO médio, raio 3,0, setor −60°…+30°, altura 1,0–2,2 |
| 14–18 | `pelvis` POS Y −2, joelhos +30 | `inf-L` varredura baixa, de trás-esq. para frente-dir. | **16–17** | ARCO baixo, raio 2,6, setor −30°…+60°, altura 0–1,1 |
| 18–22 | `torso` gira para a esquerda (Y −15) | `sup-R` diagonal ascendente, de baixo-esq. a alto-dir. | **20–21** | ARCO médio→alto, raio 3,0, setor −50°…+20°, altura 1,2–3,4 |
| 22–26 | asas corrigem (root ±8) | `med-L` horizontal de retorno, de dentro para fora | **24–25** | ARCO médio, raio 3,0, setor −30°…+60° |
| 26–30 | joelhos +35 | `inf-R` corte de tesoura convergente (de fora para o centro), encontrando a posição final do `inf-L` | **28–29** | ARCO baixo, raio 2,6, setor −40°…+40° |
| 31–38 | braços abrem e voltam à guarda | — | — | — |

8. **Dano:** cada corte 0,5 DB; um alvo pode ser atingido por no máximo 4 dos 6 (imunidade de 3 t entre acertos do mesmo combo).
9. **Recuperação:** 31–38 (8 t, 11 t na F3); sem aparo.
10. **Contra-jogo:** recuar 3+ bl antes do t8; saltar o corte 3 e abaixar-se não existe em Minecraft — ler a altura e sair lateralmente para o lado do braço que acabou de atacar; punir no 31–38 pelas costas/flanco.
11. **Variação "colado"** (jogador ≤ 1,5 bl no t0): após o corte 2, `pelvis` recua 1 bl (t14–16) e o corte 3 vira varredura **bilateral** com `inf-L` e `inf-R` juntos (ativo 17–18, ARCO baixo, setor frontal −70°…+70°); cortes 4–6 inalterados. Sinal: na preparação os dois inferiores recuam juntos (em vez de só o esquerdo).
12. **VFX/áudio:** cada corte = rastro de 4 segmentos angulares ciano + whoosh grave curto; contato = faísca ciano, lascas, flash branco no ponto de impacto.
13. **Conflitos:** `inf-L`/`inf-R` × coxas e `robe_front` (lâminas a Z ≤ −6 no arco baixo); `sup-R` ascendente × `hood` (passar à frente, Z ≤ −10).

### 13.B Mandamento Cruzado — `architect_attack_crossed_mandate` (30 t)

1. Punir jogador em linha reta à frente. 2. Fases 1–3.
3. **Telegráfico:** t0–8 superiores se afastam e elevam (X −160, Z −50) formando um "V" aberto; médios recuam; peito abre; **losango pulsa**; som de impacto seco ascendente no t4.

| Ticks | Ação | Ativo | Hitbox |
|---:|---|---|---|
| 0–8 | preparação acima | — | — |
| 9–13 | `sup-L` e `sup-R` descem cruzando em X à frente do peito (cada uma diagonal, cruzamento em Z −1,5 bl, Y 2,4 bl) | **11–13** | dois CORREDORES finos diagonais (0,6 × 3,0 × 2,6 bl) cruzados ±40° no plano frontal, centrados 1,6 bl à frente, altura 1,4–3,2 |
| 14–18 | `med-L` e `med-R` completam X inferior com atraso | **16–18** | dois CORREDORES ±35°, centrados 1,6 bl à frente, altura 0,6–2,0 |
| 19–22 | asas dão impulso curto (root −15 → +5) | — | — |
| 23–30 | lâminas se separam, guarda | — | — |

8. Dano: cada X 0,9 DB (alto); acerto pelos dois X no máximo 1,4 DB. 9. Recuperação 23–30 (8 t) — costas abertas. 10. **Contra-jogo:** sair lateralmente ≥ 1,5 bl durante a preparação (os corredores são estreitos); atacar pelas costas na recuperação. 11. Variação F3: o X inferior chega 2 t mais cedo (14–16 ativo), telegráfico igual. 12. VFX: dois rastros em X (5 segmentos); som de "tesoura" pedra-pedra no cruzamento. 13. Conflito: lâminas superiores × capuz (cruzam à frente), médias × superiores (planos separados 0,5 bl em Z).

### 13.C Roda de Cartenon — `architect_attack_cartenon_wheel` (44 t)

1. Controle de espaço ao redor (anti-cerco). 2. Fases 1–3 (exige ≥ 3 bl livres de cada lado).
3. **Telegráfico:** t0–10 seis braços abrem em três alturas, lâminas tangenciais ao corpo; t11–15 asas abrem, pé direito ancora; crescendo curto de rotação. O **sentido** (horário/anti-horário) alterna a cada uso e é visível na preparação: o tronco faz um contra-giro de 20° para o lado oposto ao do giro.

| Ticks | Ação | Ativo | Hitbox |
|---:|---|---|---|
| 0–10 | abertura | — | — |
| 11–15 | ancoragem | — | — |
| 16–20 | giro começa (torso+pelve 360° em 14 t no total); lâminas superiores varrem | **17–19** | ANEL alto: raio interno 0,8, externo 3,2, altura 1,8–3,4, 360° |
| 21–25 | médias varrem | **22–24** | ANEL médio: 0,8–3,4, altura 1,0–2,2 |
| 26–29 | inferiores fecham o arco | **27–28** | ANEL baixo: 0,8–3,0, altura 0–1,1 |
| 30–34 | desacelera, lâminas afastam | — | — |
| 35–44 | inclina para o lado do giro, recupera | — | — |

Entre os três ativos há janelas seguras de 2 t (20–21, 25–26). 8. Dano 0,6 DB por anel; máx. 2 anéis por alvo. 9. Recuperação 35–44 (10 t) com costas expostas (asas abertas, +25 %). 10. **Contra-jogo:** sair para > 3,4 bl na preparação; quem fica perto deve saltar o anel baixo e estar fora antes do médio — na prática, recuar. 11. Variação: só o sentido alterna (decidido antes do t0). 12. VFX: rastros curtos em três alturas; poeira em anel no pé ancorado; vento cortante contínuo interrompido no t30. 13. Conflitos: braços inferiores × túnica durante o giro (túnica abre `robe_left`/`robe_right` Z +20).

### 13.D Crescente Partidor — `architect_attack_splitter_crescent` (32 t)

1. Punir jogador colado ou que salta. 2. Fases 1–3.
3. **Telegráfico:** t0–7 centro de gravidade baixa (`pelvis` POS Y −3), inferiores recolhem as lâminas para trás; raspar de pedra.

| Ticks | Ação | Ativo | Hitbox |
|---:|---|---|---|
| 0–7 | preparação | — | — |
| 8–13 | `inf-L` e `inf-R` varredura baixa convergente | **10–12** | ARCO baixo, raio 2,6, setor −70°…+70°, altura 0–1,0 |
| 14–20 | `med-L` e `med-R` cortes de baixo para cima (dois crescentes laterais) | **16–19** | dois ARCOS verticais, um por lado (setores −70°…−10° e +10°…+70°), raio 2,8, altura 0,5–3,6 (pega quem saltou) |
| 21–24 | superiores: golpe descendente curto ao centro | **22–24** | CORREDOR frontal 1,2 × 2,4 (prof.), altura 0–3,2 |
| 25–32 | asas recolhem, guarda | — | — |

8. Dano: baixa 0,5, crescentes 0,6, descendente 0,7 DB; máx. 2 acertos por alvo. 9. Recuperação 8 t. 10. **Contra-jogo:** pular a varredura **não** basta — os crescentes pegam no ar; sair lateralmente (o centro fica livre nos 16–19 e as laterais livres nos 22–24) ou recuar > 2,8 bl na preparação. Nunca ficar sobre a cabeça do chefe. 11. Variação "jogador no ar" (t0): pula os 8–13 (sem varredura) e começa nos crescentes no t8, mantendo o telegráfico de agachar. 12. VFX: crescentes com rastro curvo de 5 segmentos; lascas do chão na varredura. 13. Conflito: varredura baixa × pés do próprio chefe (lâminas a Z ≤ −5).

### 13.E Sentença Perfurante — `architect_attack_piercing_sentence` (26 t)

1. Punir recuo em linha reta. 2. Fases 1–3 (a partir de 85 % na F1).
3. **Telegráfico:** t0–8 superiores alinham as lâminas ao alvo (X −90, pontas juntas), médios protegem flancos, inferiores recuam, cabeça trava a direção; pulso grave.

| Ticks | Ação | Ativo | Hitbox |
|---:|---|---|---|
| 0–8 | alinhamento; **direção travada no t8** | — | — |
| 9–12 | tronco avança 1,5 bl (deslocamento real), asas fecham parcialmente, pé traseiro impulsiona | — | — |
| 13–14 | estocada dupla `sup-L` + `sup-R` | **13–14** | CORREDOR 1,0 (larg.) × 4,5 (prof.) bl à frente, altura 0,8–2,8 |
| 15–16 | `med-R` (terceira lâmina) chega atrasada | **15–16** | CORREDOR 0,8 × 3,5, mesma linha |
| 17–19 | postura travada, braços estendidos | — | — |
| 20–26 | armas recolhem, torso recua, vulnerável | — | — |

8. Dano 1,0 DB (estocada), 0,5 DB (terceira); máx. 1,3. 9. Recuperação 20–26 (7 t, 9 t na F3), frente exposta. 10. **Contra-jogo:** passo lateral de 1 bl durante 9–12; bloquear com escudo funciona (dano de escudo normal), mas a terceira lâmina pode acertar se o jogador abaixar o escudo cedo. 11. Variação: nenhuma de direção; F3 encurta 9–12 para 9–11 com o mesmo alinhamento. 12. VFX: convergência de partículas nas pontas (carregado) 4–8, liberação no t13; whoosh perfurante. 13. Conflito: superiores × capuz no alinhamento (lâminas a Y ≤ 2,6 bl).

### 13.F Queda da Gárgula — `architect_attack_gargoyle_fall` (56 t)

1. Ataque aéreo, impacto e reposicionamento; resposta a pillaring e a distância média. 2. Fases 2–3 (exige teto livre ≥ 6 bl e ponto de pouso sólido).
3. **Telegráfico:** t0–8 joelhos dobram, asas abrem, batida de asa + estalo de impulso. **Marca no chão:** círculo de runas lilás (`architect_mana_rune`) no ponto-alvo de t9 a t32, raio = raio do impacto central.

| Ticks | Ação | Ativo | Hitbox |
|---:|---|---|---|
| 0–8 | compressão; top erguem lâminas; mid abrem; low protegem o centro | — | — |
| 9–16 | impulso vertical/diagonal (até 5 bl de alcance horizontal); **ponto de pouso fixado no t9** (posição do alvo naquele tick) | — | — |
| 17–23 | pausa no ápice; torso aponta ao alvo; armas convergem sem se atravessar | — | — |
| 24–31 | mergulho diagonal ao ponto marcado; asas fecham 50 % | — | — |
| 32–35 | impacto | **32–33** central / **34–35** anel | VOLUME central: cilindro raio 1,5, altura 0–2,0; ANEL externo: raio 1,5–3,5, altura 0–0,8 (onda rasteira) |
| 36–43 | pose de impacto, lâminas recolhem | — | — |
| 44–56 | uma asa bate para estabilizar; guarda | — | — |

8. Dano: central 1,2 DB + repulsão; anel 0,5 DB. 9. Recuperação 36–56 (21 t), costas expostas no 36–43 (asas abertas). 10. **Contra-jogo:** sair da marca durante a subida e **não voltar** antes do t36; saltar no t34 evita o anel rasteiro. 11. Variação: salto vertical (alvo ≤ 2 bl) ou diagonal (2–5 bl). 12. VFX: lascas dos pés na decolagem, rastro de `wing_tip_*`, anel de poeira e fragmentos no impacto, tremor [OPC]. 13. Conflito: lâminas convergentes × cabeça (formam um cone à frente, Z ≤ −8).

### 13.G Aparo e Réplica — `architect_attack_parry_reprise` (24 t)

Regras e ticks completos em §11.3 (janela 5–9, réplica ativa 13–14, ARCO 2,5 bl / cone 70°, altura 1,0–3,0, dano 0,6 DB). Telegráfico da réplica: o braço oposto recua 3 t antes (10–12) com som curto. É o único "ataque" disparado por evento do jogador; cooldowns e limite de 2 seguidos em §11.3.

### 13.H Comando dos Autômatos — `architect_command_automata` (64 t) [F2+]

**Fantasia:** o templo obedece. **Escopo:** sem mobs; usa estruturas da arena ou placas temporárias.

| Ticks | Arquiteto | Mundo | Áudio |
|---:|---|---|---|
| 0–8 | para; cabeça gira para a 1ª estrutura; losango acende; esterno acende do centro para fora (glow `_command`) | — | pulso grave |
| 8–16 | `sup-R` aponta para a 1ª zona; `med-L` aponta para a 2ª; `inf-*` recuam em selo | linhas ciano retas esterno→zonas (partículas) | — |
| 12–40 | mantém a pose (canalização; esterno vulnerável, §11.6); pode girar o tronco ±20° mas **não anda** | runas lilás nas zonas: zona 1 de t12 a t36, zona 2 de t20 a t44 (≥ 24 t = 1,2 s de aviso cada) | tique-taque de pedra crescente |
| 36–38 | — | **zona 1**: estrutura golpeia ou placa emerge | impacto |
| 44–46 | — | **zona 2** idem | impacto |
| 46–64 | volta à guarda | estruturas retornam ou desfazem-se em partículas | — |

- **Zonas:** máximo 2 por execução (3 na F3), círculos de raio 1,5 bl, ≥ 4 bl entre si, escolhidas com o jogador **mas nunca** exatamente sob ele no t12: a 1ª no ponto previsto do jogador (posição + 1 s de velocidade), a 2ª cortando a rota de fuga mais provável.
- **Hitbox:** VOLUME cilíndrico raio 1,5, altura 0–2,5, ativo 2 t. Dano 0,8 DB + repulsão.
- **Estruturas:** blocos marcados da arena (ex.: estátuas decorativas como blocos/displays) — **depende da arena (pendente §21)**. **Fallback:** placa de pedra temporária emerge do chão (bloco temporário ou partícula de bloco + dano em área; sem entidade com IA, sem loot, sem navegação) e desaparece em 20 t.
- **Cancelamento:** stagger durante 0–35 cancela as zonas ainda não disparadas (runas quebram em `architect_mana_error`); perder linha de visão da zona não cancela (as runas já avisaram).
- **Cooldown:** 300 t.
- **Contra-jogo:** sair das runas; punir o esterno durante a canalização.

### 13.I Olhos da Estátua de Deus — `architect_command_beams` (60 t) + `architect_beam_recoil` (16 t) [F2+]

**Emissor:** olhos da Estátua de Deus da arena (dois pontos fixos altos). **Fallback:** dois focos rúnicos fixos no cenário (blocos), nunca entidade, nunca os olhos do próprio Arquiteto.

| Ticks | Arquiteto | Feixes | Áudio |
|---:|---|---|---|
| 0–12 | interrompe ataques; cabeça vira ao alvo; `sup-L/R` erguem e apontam; `med-*` protegem o torso; `inf-*` recuam; esterno pulsa | — | — |
| 13–24 | mantém (pode girar o tronco, não anda) | emissores acendem; **duas linhas-guia finas** carmesim no piso marcando as faixas de varredura; runas nas extremidades | carregamento crescente |
| 25–30 | — | feixe se forma: núcleo branco, faixa carmesim, borda laranja | — |
| 31–48 | — | **disparo contínuo**: cada feixe varre ao longo da sua linha-guia, de uma ponta à outra, a 0,4 bl/t (≈ 7 bl em 18 t) | zumbido contínuo |
| 49–60 | `architect_beam_recoil` começa no 49 (ombros caem, torso recua, asas compensam, cabeça volta) | núcleo some (49–51), borda some (51–54), chão incandescente até t100 | desligamento abrupto |

- **Geometria:** dois feixes **paralelos** (não convergentes), separados ≥ 4 bl, cada um varrendo uma faixa reta definida no t13 a partir da posição do jogador (uma faixa passa pelo jogador, a outra 4–5 bl ao lado, cortando a fuga mais provável). Distância máxima 32 bl.
- **Hitbox:** FEIXE = volume de 0,8 bl de largura (visual 1,2 bl: núcleo 0,2, faixa 0,6, borda 0,4) do emissor até o chão, ativo 31–48; dano 0,25 DB a cada 5 t enquanto dentro + fogo 2 s. Chão incandescente (49–100): só visual na v1 (sem dano) [ADP].
- **Rastreamento:** nenhum — a varredura segue a linha-guia fixada no t13.
- **Zonas seguras:** tudo fora das duas faixas; cobertura atrás de blocos sólidos (raycast do emissor).
- **Interrupção:** stagger do Arquiteto antes do t25 cancela; depois do t25 o feixe completa (o emissor é da arena).
- **Cooldown:** 400 t. Não é usado se houver zona de H ativa sobre as mesmas faixas.

### 13.J Protocolo de Domínio — `architect_protocol_start` / `architect_protocol_interrupt` [OPC, F3, desligado na v1]

Adaptação do contador de 03:00 [REF] para ritual de arena, **sem** morte inevitável, possessão ou perda de controle.

| Etapa | Ticks | Descrição |
|---|---|---|
| Início | 0–40 (`protocol_start`) | `sup-L/R` elevados (X −170); `med-*` e `inf-*` em pose de selo (mãos convergindo à frente do esterno); asas abertas; cabeça ergue; `jaw` +25; losango e esterno no máximo; círculo de runas raio 6 bl no chão; 4 nós externos acendem um a cada 8 t |
| Canalização | 40 → 3640 (180 s) | barra temática exibe "PROTOCOLO" e o tempo restante (o texto da bossbar é do servidor, §19); feixes finos esterno→nós; o Arquiteto **volta a lutar** com ataques A–E (não fica parado 3 min); a cada 30 s fica 60 t em pose de selo com **esterno exposto** (janela de interrupção) |
| Interromper | — | (a) acertar o esterno 3× numa janela de selo; ou (b) desativar os 4 nós (cada nó = bloco da arena que se desliga com 1 golpe) |
| Interrompido (`protocol_interrupt`, 30 t) | — | nós apagam em ordem inversa; linhas viram pixels vermelhos/cianos; esterno pisca; torso recua; stagger de 60 t; perde H e I por 600 t |
| Concluído | t3640 | 3 s de aviso (linhas convergem ao centro, som subindo); onda baixa: ANEL raio 0–12 bl, altura 0–1,5, dano 2,0 DB + repulsão forte; **saltar** no momento certo ou estar atrás de cobertura evita; nada além disso |
| Jogador sai da arena | — | contador pausa; ao voltar, continua |
| Nó inacessível | — | a interrupção por esterno continua possível; nós são opcionais |
| Cooldown | — | 1 vez por encontro |

### 13.K Privilégios revogados — reação narrativa [OPC]

Sem console literal: quando ocorre a transição para a F3 (ou na interrupção do protocolo), runas quebram em `architect_mana_error`, glow do selo cai para `_glow` por 40 t e a postura muda de arrogante (cabeça erguida) para curvada. Sem sistema global novo.

---

## 14. Variações e função das fileiras de braços

| Fileira | Identidade | Golpes | Papel defensivo |
|---|---|---|---|
| Superior | amplitude, verticais, estocadas, X alto | A1, A4, B (X alto), D final, E, F | cabeça, pescoço, peito alto |
| Média | horizontais, fintas, aparos, réplicas | A2, A5, B (X baixo), D crescentes, E 3ª lâmina, G | flancos, interceptação frontal |
| Inferior | baixas, curtas, súbitas | A3, A6, D varredura, C anel baixo | abdômen, punição de aproximação |

Regras: nunca os seis iniciam juntos (defasagem 3–5 t; exceção: preparação de F e J, que são poses de risco alto); pelo menos uma lâmina em guarda durante A, B, D e E (em A, os 2 braços que ainda não atacaram ficam em guarda); torso gira para o braço condutor; asas reagem ao centro de massa; inferiores nunca atravessam coxas, túnica ou chão; nunca seis hitboxes permanentes.

| Situação | Quem inicia | Quem cobre | 2º golpe | Sinal da variação | Hitbox |
|---|---|---|---|---|---|
| Jogador colado | `inf-L`+`inf-R` (D ou A-colado) | `sup-*` em guarda alta | crescentes médios | dois inferiores recuam juntos | conforme D/A |
| Recuando | `sup-*` (E) ou bote | `med-*` flancos | 3ª lâmina média | lâminas superiores alinhadas | corredor |
| Bloqueando com escudo repetidamente (≥ 3 bloqueios em 100 t) | alterna E (estocada) com D (baixo) | top/mid | — | o telegráfico do ataque escolhido, sem troca no meio | conforme |
| Atacando na recuperação | G só se cooldown permitir; senão aceita o dano | — | réplica | braço médio sai | arco |
| Flanco | pivô; depois A começando pelo braço do lado do jogador | lâminas opostas fecham | — | cabeça vira primeiro | — |
| Atrás | pivô 180° (20 t) com asa do lado do jogador fechando | — | combo só depois | asa fecha | — |
| No ar | D (variação no ar) ou F | — | — | agachar | conforme |
| Distante | H ou I (F2+), senão corrida | — | — | esterno acende | conforme |

---

## 15. Catálogo de animações, canais e composição

### 15.1 Grupos de canais

| Grupo | Partes |
|---|---|
| FACE | `jaw`, `brow_*`, `forehead_diamond_seal` (escala), `face_mask_*` |
| HEAD | `neck`, `head` |
| TRUNK | `torso`, `abdomen`, `sternum_seal` |
| ARM_TL / ARM_TR / ARM_ML / ARM_MR / ARM_LL / ARM_LR | cadeia completa de cada braço até `sword_*` |
| WING_L / WING_R | cadeia de cada asa |
| HIP_LEGS | `pelvis`, `leg_*`, `knee_*`, `ankle_*` |
| ROBE | `robe_*` |
| TABLET | `tablet_anchor`, `tablet_half_*` |

### 15.2 Tabela mestre

Prioridade: 1 morte/transição · 2 ataque/poder · 3 stagger/dano pesado/aparo · 4 salto/queda/aterrissagem · 5 locomoção · 6 idle/expressão. Camada: **P** = clip principal; **O** = overlay (só grupos indicados).

| ID | Cat. | Loop/ação | Dur. (t) | Grupos | Inicial → final | Prior. | Camada / blend | Eventos |
|---|---|---|---:|---|---|---:|---|---|
| `architect_dormant_statue` | apresent. | loop | 200 | todos | estátua → estátua | 1 | P exclusivo | — |
| `architect_awaken` | apresent. | ação | 110 | todos | estátua → idle (+jaw 30) | 1 | P exclusivo, sem overlay | §7.4 |
| `architect_maniacal_laugh` | apresent. | ação | 36 | FACE, HEAD, TRUNK | qualquer → igual | 6 | O (sobre idle/locomoção) | som t4, t16 |
| `architect_battle_idle` | apresent. | loop | 60 | todos | idle | 6 | P | §10.1 |
| `architect_walk` | loc. | loop | 24 | HIP_LEGS, ROBE, TRUNK, WING_*, braços (micro) | — | 5 | P; quando há ataque, só HIP_LEGS+ROBE continuam (§15.4) | passos t6, t18 |
| `architect_run` | loc. | loop | 18 | idem | — | 5 | P | passos t4, t13 |
| `architect_strafe_left/right` | loc. | loop | 16 | HIP_LEGS, ROBE, TRUNK, braços | — | 5 | P | passos t3, t6 |
| `architect_backstep` | loc. | ação | 16 | todos | idle → idle | 5 | P | passos t8, t11 |
| `architect_pivot_left/right` | loc. | ação | 12 / 20 | todos | idle → idle | 5 | P | passo no fim |
| `architect_short_lunge` | loc. | ação | 22 | todos | idle → idle | 5 (2 se encadeado a ataque) | P | t8 impulso, t14 trava |
| `architect_jump` | loc. | ação | 24 | todos | idle → ápice | 4 | P | t10 decolagem |
| `architect_fall` | loc. | loop | 10 | todos | ápice → queda | 4 | P | — |
| `architect_land` | loc. | ação | 12 | todos | queda → idle | 4 | P | t0 impacto |
| `architect_guard_full` | def. | pose (loop 20) | 20 | braços, WING_*, TRUNK | — | 5 | P (substitui o idle dos braços) | — |
| `architect_guard_high` | def. | pose | 20 | braços | — | 5 | P | — |
| `architect_guard_mid` | def. | pose | 20 | braços | — | 5 | P | — |
| `architect_guard_low` | def. | pose | 20 | braços | — | 5 | P | — |
| `architect_parry_left/right` | def. | ação | 24 | ARM_M*, ARM_T*, TRUNK | guarda → guarda | 3 | P (pernas mantêm a pose anterior) | t5–9 janela, t13 réplica |
| `architect_projectile_deflect` | def. | ação | 14 | ARM_ML, ARM_MR, WING do lado | — | 3 | O | faísca no contato |
| `architect_hurt_small` | dano | ação | 8 | TRUNK + 1 braço | — | 6 | O aditivo | — |
| `architect_hurt_heavy` | dano | ação | 14 | TRUNK + 1 braço | — | 3 | O aditivo | pulso de veio |
| `architect_stagger` | dano | ação | 30 | todos | qualquer → idle | 3 | P (cancela ataque) | t0 rachadura |
| `architect_arm_disrupted` [OPC] | dano | ação | 12 | 1 braço | — | 3 | O | — |
| `architect_death` | dano | ação | 80 | todos | qualquer → caído | 1 | P exclusivo | §15.3 |
| `architect_attack_sixfold_edict` | atk | ação | 38 | todos | idle → idle | 2 | P | §13.A |
| `architect_attack_crossed_mandate` | atk | ação | 30 | todos | | 2 | P | §13.B |
| `architect_attack_cartenon_wheel` (+ `_ccw`) | atk | ação | 44 | todos | | 2 | P | §13.C |
| `architect_attack_splitter_crescent` (+ `_air`) | atk | ação | 32 / 24 | todos | | 2 | P | §13.D |
| `architect_attack_piercing_sentence` | atk | ação | 26 | todos | | 2 | P | §13.E |
| `architect_attack_gargoyle_fall` | atk | ação | 56 | todos | | 2 | P | §13.F |
| `architect_attack_parry_reprise` | atk | = parry com réplica | 24 | | | 3 | P | §11.3 |
| `architect_command_automata` | poder | ação | 64 | todos menos HIP_LEGS | | 2 | P (pernas: idle) | §13.H |
| `architect_command_beams` | poder | ação | 48 | idem | | 2 | P | §13.I |
| `architect_beam_recoil` | poder | ação | 16 | TRUNK, braços sup., WING_*, HEAD | | 2 | P | — |
| `architect_protocol_start` | poder | ação | 40 | todos | | 2 | P | §13.J |
| `architect_protocol_interrupt` | poder | ação | 30 | todos | | 2 | P | — |
| `architect_phase_enrage` | transição | ação | 40 | todos | qualquer → idle | 1 | P exclusivo | §15.3 |

### 15.3 Timelines dos clips não detalhados antes

- **`architect_maniacal_laugh` (36 t):** 0–4 `head` X −10, `neck` POS Y +0,5; 4–8 `jaw` 0→+38 (`teeth_lower` acompanha); 8–28 `jaw` oscila +30↔+38 a cada 4 t, `torso` X −4↔0; `brow_*` Z +6 (sobem); 28–36 fecha `jaw` → 0, cabeça volta. Som: rangido mineral em 3 pulsos (t8, t16, t24).
- **`architect_guard_*` (20 t):** entrada 10 t com defasagem top → mid (+3) → low (+6); poses §11.2; 10–20 loop com ±1° de tremor.
- **`architect_hurt_small` (8 t):** braço mais próximo do golpe −6° na direção do impacto (0–2), volta (2–8); `torso` 2°.
- **`architect_hurt_heavy` (14 t):** braço atingido: cotovelo +15, punho 10°; `torso` 4° oposto; 0–3 ida, 3–14 volta.
- **`architect_stagger` (30 t):** 0–5 `knee_*` +30, `pelvis` POS Y −2, braços Z ±40 (abrem), asas abrem 60 % em reflexo, `head` X +10; 5–22 mantém (tremor ±2); 22–30 volta à guarda.
- **`architect_death` (80 t):** 0–10 braços perdem tensão (cotovelos +30, lâminas desalinham); 10–25 joelhos cedem (+60), `pelvis` POS Y −6; 20–35 asas caem (root fecha e desce 20°); 30–45 `jaw` +55 (quebra) e `head` X +20; 40–60 `torso` X +40 cai para a frente sobre os joelhos; 60–80 braços tocam o chão; ao fim, partículas `architect_stone_chip` + `architect_tablet_shard` (face partindo) e a entidade é removida no padrão de morte do projeto. Não some abruptamente. Som em etapas: estalo interno t0, quebra da face t32, colapso t45 e t62.
- **`architect_phase_enrage` (40 t):** 0–8 `torso` X −10 (ergue-se), asas abrem 100 %; 8–20 `jaw` +35, braços abrem Z ±30 em sequência (top, mid, low, 2 t entre pares); 20–30 `sternum_seal` e losango SCALE 1,15 e glow `_command`; 30–40 volta ao idle da nova fase. Partícula: `architect_mana_pulse` t20; F3 adiciona `architect_mana_error` no esterno.
- **`architect_command_automata` (64 t) / `architect_command_beams` (48 t + recoil 16 t) / `architect_protocol_*`:** poses e ticks nas tabelas de §13.H–J.

### 15.4 Composição e regras

Implementação de referência: padrão do `CrimsonCommanderModel` — um clip **principal** (`anim`) e um **overlay** opcional, cada um com seu `AnimationState`, aplicados em sequência sobre a pose de bind. Regras para garantir que dois clips **nunca** escrevam no mesmo pivô sem regra:

1. O overlay só pode conter canais dos grupos declarados na tabela (coluna Grupos). Overlays permitidos: `maniacal_laugh` (FACE/HEAD/TRUNK), `hurt_small`, `hurt_heavy`, `arm_disrupted`, `projectile_deflect`. Overlay de dano é **aditivo** (o `KeyframeAnimation.apply` vanilla soma); por isso seus valores são deltas pequenos.
2. Locomoção durante ataque: o clip principal é o ataque. As pernas usam uma das duas soluções, a decidir no blockout: (a) a IA trava o movimento horizontal durante ataques (exceto deslocamentos embutidos — E, bote, F), então as pernas do ataque bastam; ou (b) um overlay de pernas `architect_walk_legs` (só HIP_LEGS+ROBE) e o ataque **não** tem canais nesses grupos. **Recomendação:** (a) na v1; é a forma mais simples de cumprir "locomoção não sobrescreve ataques".
3. Olhar procedural (`head`/`neck` seguindo o alvo) é aplicado **depois** dos clips só quando o principal é idle, locomoção ou guarda (prioridade 5–6). Ataques e poderes que travam a cabeça têm canais de HEAD.
4. Cross-fade: troca de principal com transição de 4 t (amostrar o último quadro do anterior e o primeiro do novo; se o padrão do projeto não tiver cross-fade, aceitar corte seco no blockout e registrar). Reset: todo clip parte da pose de bind; clips de ação terminam na pose de idle.
5. Cancelamento: um clip só é cancelado por outro de prioridade mais alta (número menor). Exceção explícita: stagger (3) cancela ataques e poderes (2) **antes** do primeiro tick ativo; depois disso, espera o fim do ativo atual. Morte (1) cancela tudo.
6. Se o blockout mostrar necessidade de prioridade por canal (vários overlays simultâneos), adotar o compositor por canal da Matriarca (`PoseMixer`, D-035) — registrar como decisão nova.

---

## 16. Poderes, partículas, VFX e áudio

### 16.1 Linguagem visual

Arquitetura mágica sendo executada: partículas quadradas, lascas angulares, linhas retas, runas geométricas, pulsos em ângulos definidos, trilhas curtas. Sem fumaça genérica, sem caracteres de texto, sem excesso que esconda o chão.

### 16.2 Implementação das partículas

O projeto **não registra tipos de partícula próprios** (busca em `src/main/java`: só partículas vanilla e `DustParticleOptions`). Os nomes `architect_*` são **lógicos**: na v1 cada um é uma receita de emissão com partículas vanilla. Tipos próprios (sprites quadrados/runas) ficam [OPC] e exigem registro novo e entrada em `DECISIONS.md`.

| Nome lógico | Mapeamento v1 [HIP] | Cor | Tamanho | Velocidade / gravidade | Vida | Qtde | Origem | Evento |
|---|---|---|---|---|---|---|---|---|
| `architect_stone_dust` | `DustParticleOptions` | `#9A9289` / `#625D5C` | 0,6–1,0 | 0,02 radial, sem gravidade | 10–16 t | 4–10 | junta/pé/impacto | passos, asas, revelação |
| `architect_stone_chip` | partícula de bloco (pedra) | do bloco | vanilla | 0,15–0,3 oposto ao impacto, com gravidade | 20 t | 3–6 | ponto de impacto | golpes, desdobramento |
| `architect_stone_crack` | `DustParticleOptions` em linha (3–4 pontos alinhados) | `#24232B` | 0,5 | 0 | 12 t | 3–4 linhas | superfície | revelação, stagger |
| `architect_heavy_footstep` | dust rente + 2 de bloco | `#9A9289` | 0,8 | horizontal 0,05 | 12 t | 6 + 2 | `foot_*` | cada passo |
| `architect_tablet_shard` | partícula de bloco (pedra polida/diorito) | clara | vanilla | 0,2, gravidade | 30 t | 6–12 | máscara/tábua | §7.4 |
| `architect_mana_spark` | dust | `#38BDBA`/`#B6FFF0` | 0,4 | 0,05 | 6 t | 1–3 | selo/veios | pulsos |
| `architect_mana_trace` | dust em sequência ao longo da lâmina (1 ponto por tick) | `#38BDBA` | 0,4 | 0 | 4 t | 1/tick × 6 t | `sword_*` guarda→`blade_tip_*` | preparo de golpes |
| `architect_mana_rune` | dust em contorno de losango/círculo | `#D8C8FF` (autoridade) | 0,6 | 0 | reemitida a cada 4 t | 12–24 por runa | chão | marcas F, H, J |
| `architect_mana_pulse` | anel de dust (16 pontos) expandindo | `#38BDBA` | 0,5 | radial 0,15 | 8 t | 16 | `socket_sternum` | despertar, enrage |
| `architect_mana_error` | dust | `#FF2E3E` | 0,4 | 0,1 aleatória | 5 t | 3–5 | esterno/runas | stagger, cancelamento, revogação |
| rastro de corte | dust em 3–5 pontos ao longo do arco do golpe, emitidos no tick ativo | `#38BDBA` + `#55515B` | 0,5 | 0 | 4 t | 3–5 | arco da hitbox | cada golpe |
| faísca de impacto/aparo | `ParticleTypes` de faísca vanilla equivalente + dust branco | ciano/branco | — | 0,2 | 4 t | 2–3 | ponto de contato | contato |
| calor do feixe | dust carmesim/laranja + fumaça leve | `#F02B42`, `#FF8A3D` | 0,8 | subida 0,03 | 10 t | 2/tick por bloco atingido | linha do feixe | §13.I |
| núcleo do feixe | linha de dust densa (a cada 0,25 bl) ou camada de render em linhas (padrão D-034) [HIP] | `#FFF0D9` / `#F02B42` | — | 0 | 1 t (reemitida) | — | emissor → chão | §13.I |

Linhas esterno→nó/zona (H, J): dust ciano a cada 0,3 bl, reemitida a cada 2 t; ou camada de linhas como o fio da Matriarca (D-034) se o custo de partículas for alto.

### 16.3 Por elemento

- **Lâminas:** preparação = trace guarda→ponta; corte = rastro angular 3–5 segmentos; impacto = faísca ciano + lascas + flash branco; aparo = 2–3 faíscas + anel de pressão (8 pontos dust); carregado (E) = partículas convergem à ponta 4–8 e saem no t13. Tudo na posição real (âncoras, §6, ou posição calculada da hitbox).
- **Asas:** abertura = pó nas juntas; batida = poeira em leque no chão, sem dano; mergulho = fragmentos + trace nas `wing_tip_*`; fechamento defensivo = estalo + pó nas junções.
- **Feixes:** núcleo branco, faixa carmesim, borda laranja, calor, cinza e pedra incandescente; desligamento em camadas (núcleo → borda → chão).
- **Protocolo:** runas circulares e linhas retas; nós em sequência; feixes finos esterno→nó; convergência e onda baixa ao concluir; apagamento inverso e fragmentação em pixels na interrupção.

### 16.4 Áudio (eventos do mod → áudios vanilla com pitch, D-010)

| Evento | Caráter | Preparo ≠ impacto? |
|---|---|---|
| Estátua (idle) | silêncio; ronco grave distante raro | — |
| Rachadura | estalos secos com eco longo | — |
| Desdobramento dos braços | 4 estalos de articulação distintos (pitches 0,80–0,95), mais os 2 superiores ao soltar a tábua | — |
| Abertura das asas | atrito grave de placas + fragmentos | — |
| Passos | impacto pesado curto abafado, cauda de reverberação | — |
| Preparo de corte (A) | seis cliques curtos ascendentes | sim |
| Corte | whoosh grave curto + estalo mineral | — |
| Aparo | pedra contra pedra agudo + ressonância breve | sim (sem preparo; o som é o contato) |
| Mandamento Cruzado | impacto seco ascendente (preparo) / "tesoura" (cruzamento) | sim |
| Roda | crescendo de rotação (preparo) / vento cortante (giro) | sim |
| Crescente | raspar de pedra (preparo) | sim |
| Sentença | pulso grave (preparo) / whoosh perfurante | sim |
| Gárgula | batida de asa + estalo de impulso / baque pesado no impacto | sim |
| Autômatos | pulso grave + tique-taque de pedra / impacto | sim |
| Feixe | carregamento crescente / zumbido contínuo / desligamento abrupto | sim |
| Protocolo | pulsos rítmicos graves | sim |
| Gargalhada | pedra rangendo em pulsos; não humana | — |
| Stagger | rachadura alta + tilintar de erro | — |
| Morte | estalo interno, quebra da face, colapso em etapas | — |

---

## 17. Hitboxes e justiça

Quatro sistemas separados: (1) geometria visual; (2) colisão da entidade `1.55 × 3.35`, fixa — não cresce com asas nem armas; (3) volumes temporários de golpe, criados pela lógica do servidor nos ticks ativos de §13, com forma, posição e rotação relativas à orientação **travada** no início do golpe; (4) partículas, puramente visuais.

| Ataque | Forma | Altura (bl) | Alcance | Ticks ativos | Alvos | Cancelamento |
|---|---|---|---|---|---|---|
| A | 6 ARCOS | 0–3,4 por golpe | 2,6–3,0 | 8–9, 12–13, 16–17, 20–21, 24–25, 28–29 | jogadores e mobs hostis ao chefe | stagger antes do t8 |
| B | 4 CORREDORES (2 X) | 1,4–3,2 / 0,6–2,0 | 3,0 | 11–13, 16–18 | idem | stagger antes do t11 |
| C | 3 ANÉIS | alto / médio / baixo | 3,0–3,4 | 17–19, 22–24, 27–28 | idem | stagger antes do t17 |
| D | ARCO + 2 ARCOS + CORREDOR | 0–1,0 / 0,5–3,6 / 0–3,2 | 2,4–2,8 | 10–12, 16–19, 22–24 | idem | stagger antes do t10 |
| E | 2 CORREDORES | 0,8–2,8 | 4,5 / 3,5 | 13–14, 15–16 | idem | stagger antes do t13 |
| F | VOLUME + ANEL | 0–2,0 / 0–0,8 | r 1,5 / 1,5–3,5 | 32–33, 34–35 | idem | stagger na subida aborta (cai sem dano) |
| G | ARCO | 1,0–3,0 | 2,5 | 13–14 | só o atacante aparado | — |
| H | 2–3 VOLUMES | 0–2,5 | r 1,5 | 2 t por zona | idem | stagger antes do disparo da zona |
| I | 2 FEIXES | coluna | 0,8 de largura, até 32 | 31–48, a cada 5 t | idem | stagger antes do t25 |
| J | ANEL | 0–1,5 | 12 | 1 t na conclusão | idem | interrupção |

Regras: nenhum volume existe antes de a lâmina começar a se mover (todo ativo começa ≥ 2 t depois do início do movimento do braço no clip); o volume acompanha a lâmina com atraso de 0–1 t, nunca adiantado; asas não causam dano (nenhum ataque declara dano de asa); partículas nunca causam dano; nunca dois volumes inevitáveis no mesmo espaço (IA, §12.1); sempre existe saída (lateral em A/B/D/E, distância em C, sair da marca em F, fora das faixas em I). Telegráficos mínimos estão no item 3 de cada ataque.

---

## 18. Compatibilidade técnica com o projeto

Verificado no código em 2026-11-08 (sem alterações):

| Tema | Encontrado no repositório | Consequência para o Arquiteto |
|---|---|---|
| Modelo | `CrimsonCommanderModel extends EntityModel<CrimsonCommanderRenderState>`; `LayerDefinition.create(mesh, ATLAS, ATLAS)` com `CrimsonCommanderParts.ATLAS = 256` | `ArchitectModel extends EntityModel<ArchitectRenderState>`, atlas 256 |
| Animação | `AnimationDefinition`/`KeyframeAnimation` assados por clip (`baked` em `EnumMap`), principal + overlay (`state.anim`, `state.overlay`) | mesmo padrão; enum `ArchitectAnim` |
| Canais | `AnimationChannel.Targets.POSITION`, `ROTATION` e `SCALE` em uso | máscara e tábua usam SCALE para sumir |
| Composição por canal | `PoseMixer` + `MatriarchAnimDirector` (D-035), específico da Matriarca | reserva se §15.4 regra 6 for acionada |
| Emissivo | `EyesLayer` em `BasiliskEyesLayer`, `DragonGlowLayer` (escolhe `RenderType` por estágio) | §8.5 |
| Linhas em render | `PuppetThreadRenderLayer` (D-034) | opção para feixes/linhas |
| Partículas | só vanilla e `DustParticleOptions` | §16.2 |
| Sons | eventos do mod com áudio vanilla (D-010) | §16.4 |
| Animação sem biblioteca | D-009 | sem GeckoLib |
| Barra de boss | `client/bossbar/ThemedBossBars` | §19 |
| Eventos de rede | IDs 100–120 e 140–159 em uso | Arquiteto: 160–179 |

Não confirmado (verificar na implementação, sem inventar): leitura da matriz acumulada de uma parte para âncoras; escala de parte para UV 2×; cross-fade entre clips.

---

## 19. Itens obrigatórios de boss do projeto (AGENTS.md)

- **Barra temática:** `client/bossbar/ArchitectBossBar` implementando `ThemedBossBars.Style`, registrada pela chave de tradução do nome; textura `textures/gui/boss_bar/architect.png` gerada por script em `tools/`. Tema: moldura de capuz/arco de templo em pedra cinza-azulada, preenchimento por fase (F1 lilás `#D8C8FF`, F2 ciano `#38BDBA`, F3 coral `#FF604B`), marcas na trilha em 70 % e 35 %; no protocolo, o nome da barra mostra o contador.
- **Conquistas de história**, **Bestiário** (capítulo de 6+ páginas: estátua, despertar, seis lâminas, templo, protocolo, queda) e **GameTests** (`runArchitectTest`, a criar): revelação 110 t, fases nos limiares, aparo com cooldown e limite de 2, imunidade a stagger por 200 t, hitboxes ausentes fora dos ticks ativos, protocolo nunca mata sozinho se desligado/interrompido.
- **Eventos de rede (`broadcastEntityEvent`):** 160 início da revelação; 161 queda da máscara direita; 162 esquerda; 163 tábua; 164–169 cortes A1–A6; 170 aparo; 171 stagger; 172 impacto F; 173 comando H; 174 feixes I; 175 protocolo início; 176 protocolo interrompido; 177 enrage; 178 morte; 179 reserva.
- **Leash e persistência:** a definir com a arena (§21).

---

## 20. Plano de produção e critérios de aceitação

| Marco | Conteúdo | Depende de | Saída verificável |
|---|---|---|---|
| M1 Blockout sem textura | proporções §3.2, cabeça/capuz/cavidade, seis cadeias de braço, seis armas, asas, túnica, pernas, tábua | — | `.bbmodel` com a árvore §5.1 (entregável separado; **não existe ainda**) |
| M2 Validação do rig | mandíbula + dentes, máscaras, cadeias, asas, sockets da tábua | M1 | capturas das poses de teste |
| M3 Poses de teste | frente, costas, lados, topo, perspectiva; estátua, idle, guarda, ataque amplo, salto, revelação | M2 | matriz §5.5 sem interseção |
| M4 Textura | atlas 256, glow ×4, paleta §8.3 | M3 | PNGs |
| M5 Animações 1 | apresentação, locomoção, defesa | M4 | clips Java |
| M6 Animações 2 | A–G, enrage, morte | M5 | clips Java |
| M7 Integração e combate v1 | entidade, IA, hitboxes, partículas, sons, barra, GameTests | M6 | `build` + `runArchitectTest` verdes |
| M8 Cliente | revisão visual em `runClient` | M7 | **única etapa que valida visual** |
| M9 Arena + H, I | estrutura do templo, emissores | arena projetada | — |
| M10 [OPC] | J, integridade por braço, desarme, desmembramento | M9 | — |

H e I **não** podem ser marcados prontos sem a arena (M9).

**Checklist de blockout/visual** (marcar só após M8): [ ] seis braços separados e articuláveis · [ ] seis armas presas às mãos · [ ] sem interseção evidente lâmina/torso/asa/túnica/perna · [ ] capuz com cavidade real · [ ] rosto humanoide, sem caveira · [ ] olhos negros com pupila coral e losango · [ ] fendas de marionete · [ ] duas fileiras de dentes planos · [ ] `teeth_lower` acompanha `jaw` · [ ] duas metades de máscara com trajetórias próprias · [ ] asas segmentadas de pedra · [ ] tábua alinhada aos dois sockets · [ ] textura legível em luz e sombra · [ ] paleta de poderes preservada.

**Checklist de animação/combate:** [ ] trajetórias independentes por braço · [ ] preparo, ativo e recuperação em todos os ataques · [ ] locomoção não sobrescreve ataque · [ ] defesa não cobre 360° · [ ] janelas reais de punição · [ ] feixes e zonas anunciados · [ ] sem stun-lock · [ ] F3 mais agressiva e legível · [ ] ticks de animação = ticks de hitbox.

---

## 21. Registro de decisões, dúvidas e testes pendentes

| ID | Item | Tipo | Como resolver |
|---|---|---|---|
| Q-01 | Relatório `validacao_alteracoes_arquiteto_kandiaru.md` ausente na máquina | dúvida | entregar o arquivo; conferir se há P0/P1 além dos 5 aplicados (mandíbula/dentes, cadeias, máscaras, cavidade, tábua) |
| Q-02 | Sinal de `yRot` | **resolvido na fatia 2** pelo export do modelo Java real: braço esquerdo com Y −90 faz o cotovelo dobrar para cima; asa esquerda fecha para trás com Y −80; máscara direita abre com Y +, esquerda com Y − | — |
| Q-03 | Densidade 2× na face | **resolvido na fatia 2**: cubo declarado com o dobro de texels e encolhido por `CubeDeformation` negativa (rosto, dentes, máscara, losango); 4 dentes de 2 texels por fileira | — |
| Q-04 | Leitura de matriz acumulada para âncoras | [HIP] | fatia de render |
| Q-05 | Cross-fade entre clips | [HIP] | fatia de animação |
| Q-06 | Arena: Estátua de Deus, estruturas comandáveis, nós, pedestal, leash, persistência | pendente; o encontro já pode começar pela Tábua dos Mandamentos (§23) | ficha de arena/estrutura própria |
| Q-07 | Atributos numéricos | **resolvido em 2026-11-08** (escolha do usuário, conjunto "mais duro"; ver §23) | — |
| Q-08 | Drops, conquistas, textos do Bestiário | pendente | fatia de conteúdo |
| Q-09 | Ângulos da §5.4 e limites da §5.3 | [HIP] | M2/M3 |
| Q-10 | Todos os tempos de §10 e §13 | [ADP] | M8 (`runClient`) e balanceamento |
| Q-11 | Chão incandescente com dano | [OPC] | após M9 |
| Q-12 | Tipos de partícula próprios | [OPC] | se a v1 com vanilla não ler bem |

Nada desta ficha foi testado no jogo. Nenhum comando de build foi necessário (só documentação).

---

## 22. Relatório de alterações

- **Criado:** `docs/design/architect.md` (este arquivo). Nenhum arquivo de código, recurso, registro ou outra doc foi alterado.
- **Corrigido (P0/P1):** dentes divididos em `teeth_upper`/`teeth_lower` com pivô da `jaw` e oclusão; cadeia aninhada de 7 níveis repetida para os seis braços com pivôs e limites; máscara bipartida com pivôs, profundidade, ordem, vetores, fragmentação e ticks; cavidade do capuz por placas recuadas; tábua em `tablet_anchor` com sockets `tablet_grip_*`; revelação unificada em 110 t.
- **Preservado da ficha (1):** pilares, silhueta em três distâncias, anatomia, assimetrias dos braços e armas, paleta, regras de textura, catálogo A–J com os tempos originais (38/30/44/32/26/56/22–28/60 t), funções das fileiras, nomes de animações e partículas, tabela de áudio, telegráficos mínimos, regras de legibilidade, escopo mínimo e restrições.
- **Ajustado:** ID `architect`; altura 54 px; atlas único; partículas como nomes lógicos sobre vanilla; durações de locomoção do prompt detalhado (caminhada 24 t, corrida 18 t, strafe 16 t); conflitos C-01…C-16.
- **Em aberto:** §21.
- **Não testado:** tudo (sem `runClient`, sem blockout).

---

## 23. Atributos, invocação e perfil (fatia 1)

- **Perfil:** `boss`. **Pacote (`pkg`):** `architect`. **Mob de referência:** `CrimsonCommander` (boss com fases) e `BrimstoneSalamander` (forma da fatia 1).
- **Atributos** (escolha do usuário em 2026-11-08, conjunto "mais duro"): vida 480, armadura 16, resistência de armadura 8, dano base 12, velocidade 0,30, resistência a repulsão 0,95, alcance de visão 48, XP 500, altura de passo 1,1. Em `Architect.createAttributes()`.
- **Imunidades** (§11.1): Veneno e Wither, afogamento, congelamento, quedas < 6 blocos. **Não** é imune a fogo nem a dano físico. A estátua dormente ignora dano que não venha de jogador (e `/kill`), não pode ser empurrada e não sofre repulsão.
- **Spawn:** sem spawn natural. Ovo gerador (criativo) e **Tábua dos Mandamentos** (`commandment_tablet`, raridade épica, 1 por pilha): usada no topo de um bloco sólido, ergue o Arquiteto dormente virado para o jogador. Precisa de um mundo fora do pacífico, espaço livre para 1,55 × 3,35 blocos e nenhum outro Arquiteto vivo a 96 blocos. Uso que falha não gasta a tábua e mostra o motivo. **Receita/obtenção em sobrevivência: pendente para a fatia 4** (decisão de design ainda não tomada). Quando a arena existir, ela vira o caminho principal (Q-06).
- **Estados (`Architect.State`):** DORMANT, AWAKENING, COMBAT_IDLE, REPOSITION, ATTACKING, CHANNELING, GUARDING, PARRYING, STAGGERED, PHASE_TRANSITION, DYING. **Fases (`Architect.Phase`):** MONUMENT (dormente), DUELIST (≥ 70 %), ADMINISTRATOR (≥ 35 %), COLLAPSE (< 35 %), via `phaseFor`.
- **Eventos de rede:** constantes 160–178 já declaradas na classe (§19); disparo a partir da fatia 3.

## Estado de implementação

| Fatia | Estado | Data | Evidência |
|---|---|---|---|
| 0. Ficha revisada | concluída | 2026-11-08 | este documento; atributos resolvidos com o usuário (§23) |
| 1. Fundação | **concluída** | 2026-11-08 | `.\gradlew.bat compileJava` → BUILD SUCCESSFUL; `.\gradlew.bat runArchitectTest` → "All 5 required tests passed" (os 4 do Arquiteto + 1 de outra suíte sempre registrada), 1 rodada. Rodados pelo agente no terminal do app (fora da sandbox, onde o Gradle falha com loopback). |
| 2. Visual e rigging | **concluída** | 2026-11-08 | `python tools/generate_architect_assets.py` (110 partes, 181 cubos, sem sobreposição de UV, 3 texturas glow, 16 clipes); `.\gradlew.bat compileJava` OK; `.\gradlew.bat runArchitectTest` → 5 testes do Arquiteto + 1 de outra suíte aprovados; `.\gradlew.bat -I tools/architect-sketches.gradle exportArchitectSketchModel` OK; `python tools/generate_architect_sketches.py` → `build/architect-sketches/architect_pose_sheet.png`. **O `runClient` ainda falta.** |
| 2b. Revisão visual (humano) | **concluída** | 2026-11-08 | Poses da folha e barra temática aprovadas pelo usuário (fora do jogo); visual no jogo pendente de `runClient` |
| 3. IA e combate | pendente | — | — |
| 4. Polimento e docs | pendente | — | — |

Arquivos da fatia 1: `entity/architect/Architect.java`, `item/CommandmentTabletItem.java`, `verification/ArchitectGameTests.java`, registros em `ModEntities`/`ModItems`/`ModCreativeTabs`, `architectTest` em `build.gradle`, textos en_us/pt_br, `items/` e `models/item/` do ovo e da tábua, `tools/generate_architect_assets.py` (gera os ícones).

GameTests (`coraline.architectTests=true`): `architect_attributes_and_registration`, `architect_phase_thresholds`, `architect_dormant_statue_constitution`, `architect_tablet_summon_rules`.

**Próximo passo:** `runClient` pelo usuário (conferir brilho, escala ao lado do jogador e revelação); fatia 3 (`docs/prompts/fatia_3_ia_combate.md`), já liberada pela aprovação 2b.

## 24. Fatia 2 — o que o código faz (Parte B)

- **Fonte única do visual:** `tools/generate_architect_assets.py` monta o rig (pivôs e cubos da §5 em coordenadas BB) e gera `client/architect/ArchitectParts.java`, o atlas e as texturas glow; `tools/architect_poses.py` (BASE, estátua, estados das asas) e `tools/architect_clips.py` (16 clipes como trilhas por parte) geram `client/architect/ArchitectAnimations.java`. **Ângulos se editam nos `.py` e se regeneram**; nunca à mão no Java gerado. Os valores atuais da §5.4 foram atualizados para os do código.
- **Clipes implementados** (`ArchitectAnim`): DORMANT_STATUE 200 (loop), AWAKEN 110, BATTLE_IDLE 60 (loop), MANIACAL_LAUGH 36, WALK 24 (loop), RUN 18 (loop), GUARD_FULL 20 (loop), STAGGER 30, DEATH 80, SIXFOLD_EDICT 38, CROSSED_MANDATE 30, CARTENON_WHEEL 44, SPLITTER_CRESCENT 32, PIERCING_SENTENCE 26, GARGOYLE_FALL 56, PARRY_LEFT 24. Ataques com interpolação linear e quadros nos ticks da §13. **Ainda não existem:** strafe, backstep, pivô, bote, salto/queda/aterrissagem separados, guardas alta/média/baixa, PARRY_RIGHT, deflexão, hurt, poderes H–J e enrage (§15.2) — entram quando a fatia 3 precisar deles.
- **Composição:** um clipe por vez (sem overlay ainda); olhar procedural da cabeça/pescoço só em idle, andar, correr e guarda. Máscara e tábua visíveis só em DORMANT_STATUE e AWAKEN.
- **Brilho:** `ArchitectGlowLayer` escolhe `architect_glow` (combate), `_command` (estado CHANNELING), `_waking` (AWAKEN) ou nada (estátua, stagger, morte).
- **Servidor (mínimo para revisar):** `DATA_ANIM` sincronizado; golpe de jogador na estátua → AWAKENING (110 ticks invulnerável) → COMBAT_IDLE (idle/andar pelo movimento). Barra temática visível fora da estátua, cor por fase (PURPLE/BLUE/RED). Sem IA de combate.
- **Barra:** `tools/generate_boss_bar_architect.py` → `textures/gui/boss_bar/architect.png` (288×44, slot 46,18 196×6, marcas em 70 % e 35 %); `client/bossbar/ArchitectBossBar` registrado em `ThemedBossBars`.
- **Ferramentas de revisão:** `tools/preview_architect_poses.py` (prévia rápida em Python, sem Gradle) e `tools/ExportArchitectModel.java` + `tools/architect-sketches.gradle` + `tools/generate_architect_sketches.py` (modelo Java real, texturizado).
- **Atlas:** o layout é por empacotamento automático (prateleiras), não o diagrama lógico da §8.6; a separação por ilhas e a margem de 1 texel valem.
- **Pendente de revisão visual** (todas as linhas acima): `runClient` com `/summon codenamecoraline:architect ~ ~ ~` (estátua; bata nela para despertar) ou a Tábua dos Mandamentos.
