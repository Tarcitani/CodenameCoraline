# Ceifeiro das Almas Vagas: Rig Completo, Camadas e Coreografias (`soul_reaper`)

Complemento normativo da seção 1.6 de [soul_reaper.md](soul_reaper.md). Define a hierarquia completa das juntas, o contrato de camadas (L0–L5), a solução de empunhadura de duas mãos e a coreografia integral (Parte A) de todas as animações do **Ceifeiro (`soul_reaper`)** e da **Lebre dos Presságios (`omen_hare`)**.

---

## 1. Rig definitivo e anatomia espectral

### 1.1 Princípio anatômico e coordenada de arma
- **Sem pernas humanas visíveis:** abaixo da cintura há uma pelve espectral dissolvida no manto inferior (`lower_shroud`). Apoio, salto e pouso são expressos por `pelvis`, `abdomen`, volume do manto e compressão da névoa da base.
- **Solução para duas mãos no Minecraft (`ModelPart`):** No motor de modelos do Minecraft, toda transformação é hierárquica (Forward Kinematics). Se `scythe_root` ficasse diretamente em `root`, qualquer inclinação ou torção em `pelvis → abdomen → torso → chest` tiraria os braços de alinhamento com o cabo. Por isso, **`scythe_root` é filho direto de `chest`** (o mesmo pai de `left_clavicle` e `right_clavicle`), mantendo a arma e ambos os ombros no mesmo espaço local do tórax.
- **Dois pontos de empunhadura (`grip_upper` e `grip_lower`):**
  - `grip_upper`: parte alta do cabo, próximo à lâmina (mão direita por padrão).
  - `grip_lower`: parte baixa do cabo, próximo ao pomo (mão esquerda por padrão).
  - Quando ambas as mãos seguram o cabo, o animador resolve os ângulos de ombro, cotovelo e punho no espaço de `chest` para cravar `right_hand` em `grip_upper` e `left_hand` em `grip_lower` sem deslizamento. Nas trocas de mão (F6), a transição dura ≥ 3 ticks.
- **Rotação de giro separada:** `entityYaw` (orientação autoritativa no servidor) é separado de `actionSpinYaw` (acumulador local contínuo para giros de 360°/720° sem normalização brusca a cada tick).

### 1.2 Hierarquia completa do Ceifeiro (`soul_reaper`)

```text
root                                      # pivô local do modelo; não move a entidade no mundo
└── pelvis                                # centro de massa visual / cintura oculta
    ├── lower_shroud                      # volume que substitui pernas / manto inferior
    │   ├── shroud_front_root -> shroud_front_mid -> shroud_front_tip
    │   ├── shroud_back_root  -> shroud_back_mid  -> shroud_back_tip
    │   ├── shroud_left_root  -> shroud_left_mid  -> shroud_left_tip
    │   └── shroud_right_root -> shroud_right_mid -> shroud_right_tip
    └── abdomen                           # flexão e torção inferior da coluna
        └── torso                         # caixa torácica e rotação principal
            └── chest                     # peitoral; ancestral comum do broche, capuz, braços e foice
                ├── brooch_anchor         # broche espiral prateado (pulso luminoso, sem rotação solta)
                ├── upper_shroud          # manto dos ombros (4 painéis × 3 segmentos)
                │   ├── upper_shroud_front_root -> mid -> tip
                │   ├── upper_shroud_back_root  -> mid -> tip
                │   ├── upper_shroud_left_root  -> mid -> tip
                │   └── upper_shroud_right_root -> mid -> tip
                ├── neck                  # pescoço espectral (±20° yaw, ±15° pitch base)
                │   └── head_core         # volume interno que orienta o capuz
                │       └── cowl          # massa principal do capuz (atraso 1–5 ticks)
                │           ├── hood_crown
                │           ├── hood_front
                │           ├── hood_back
                │           ├── hood_left
                │           ├── hood_right
                │           ├── face_void
                │           ├── eye_left_anchor
                │           └── eye_right_anchor
                ├── left_clavicle
                │   └── left_shoulder
                │       └── left_arm_upper
                │           └── left_elbow
                │               └── left_forearm
                │                   └── left_wrist
                │                       └── left_hand
                │                           ├── left_thumb_01  -> left_thumb_02
                │                           ├── left_index_01  -> left_index_02
                │                           ├── left_middle_01 -> left_middle_02
                │                           ├── left_ring_01   -> left_ring_02
                │                           └── left_little_01 -> left_little_02
                ├── right_clavicle
                │   └── right_shoulder
                │       └── right_arm_upper
                │           └── right_elbow
                │               └── right_forearm
                │                   └── right_wrist
                │                       └── right_hand
                │                           ├── right_thumb_01  -> right_thumb_02
                │                           ├── right_index_01  -> right_index_02
                │                           ├── right_middle_01 -> right_middle_02
                │                           ├── right_ring_01   -> right_ring_02
                │                           └── right_little_01 -> right_little_02
                └── scythe_root           # arma independente das mãos, ancorada no referencial do peito
                    ├── scythe_shaft_upper (contém grip_upper)
                    ├── scythe_shaft_mid
                    ├── scythe_shaft_lower (contém grip_lower)
                    │   └── scythe_pommel
                    └── scythe_blade_socket
                        └── scythe_blade_base
                            └── scythe_blade_tip
```

### 1.3 Dez regras físicas universais
1. **Antecipação:** `pelvis` e `abdomen` iniciam deslocamentos e torções antes do peito e dos braços.
2. **Cadeia cinética:** `pelvis → abdomen → torso → chest → clavicle → shoulder → arm_upper → elbow → forearm → wrist → hand → scythe_root`.
3. **Inércia da foice:** o cabo acompanha as mãos; a ponta da lâmina (`scythe_blade_tip`) estabiliza 1–2 ticks após impactos.
4. **Contrapeso:** quando a foice se projeta para um lado, tronco, manto e braço oposto compensam o torque.
5. **Manto atrasado em 3 segmentos:** `root` do tecido segue o corpo; `mid` atrasa 2–3 ticks; `tip` atrasa 4–7 ticks.
6. **Pouso sem pernas:** `pelvis` desce, `abdomen` comprime e `lower_shroud` se espalha em anel.
7. **Olhar persistente:** `neck` e `cowl` acompanham `lockedTargetId` com velocidade angular limitada, sem girar 180° num quadro.
8. **Alvo travado no servidor:** `lockedTargetId` é fixado no início do preparo; o vetor do golpe nunca faz snap no meio da execução.
9. **Pose neutra explícita:** nenhuma junta é zerada abruptamente; partes fora do golpe mantêm pose de apoio e retornam com blend de 4–6 ticks.
10. **Deslocamento autoritativo:** `root` do modelo só faz ajustes locais de pose; avanço, salto, órbita, subida e teleporte vêm da entidade no servidor.

---

## 2. Contrato de camadas de animação (L0–L5)

| Camada | Escopo | Prioridade e regra de mistura |
|---|---|---|
| **L0 — Locomoção** | `glide`, `orbit`, `staff_stride`, `predator_run`, `mist_ascent`, `idle_1..3` | Base contínua do corpo e do manto (blend de 6 ticks nas trocas). |
| **L1 — Mira/Olhar** | `neck`, `head_core`, `cowl`, `eye_*` | Aditiva limitada sobre L0/L2; desativada em `death` e momentos de cabeça fixa. |
| **L2 — Ação Principal** | Ataques (`O1–O5`, `F1–F6`), Esquivas (`E1–E4`), Provocações (`P1–P5`), Anulações completas (`A1–A3`, `A5`, `A6`) | Substitui as juntas que controla. Prioridade: Ataque > Esquiva > Anulação completa > Provocação. |
| **L3 — Reação Rápida** | `hurt` (14t) e `A4 flick_snuff` (14t) | Sobrepõe apenas parte superior/tronco/mão esquerda sem interromper nem cancelar um ataque ativo em L2. |
| **L4 — Cinemática/Estado** | `awaken_eruption`, `awaken_slam`, transições de fase, `death` | Bloqueia L0–L3 integralmente. |
| **L5 — Secundária/Procedural** | Ondulação de `shroud_*_mid/tip`, vibração pós-impacto da lâmina, pulso de `brooch_anchor` | Aditiva pós-pose; nunca altera hitboxes ou tempo mecânico. |

---

## 3. Animações do Arauto — Lebre dos Presságios (`omen_hare`)

Hierarquia da lebre: `hare_root → hare_pelvis (hare_tail, hare_back_left_leg, hare_back_right_leg) → hare_spine → hare_chest (hare_front_left_leg, hare_front_right_leg, hare_neck → hare_head [hare_ear_left, hare_ear_right, hare_eyes_emissive])`.

| Animação | Duração (t) | Coreografia por parte do corpo | Peso e inércia | Impacto |
|---|---:|---|---|---|
| `hare_approach` | 20 | Trote calmo em 4 patas sobre a neve; `hare_spine` nivela suave; `hare_head` e `hare_eyes_emissive` permanecem cravados no jogador; orelhas eretas em alerta. | Leve e silencioso, sem medo. | n/a |
| `hare_stand_stare` | 40 | `hare_pelvis` apoia nas ancas; `hare_spine` e `hare_chest` esticam na vertical em postura bípede antinatural; patas dianteiras recolhem junto ao peito; olhos rubis pulsam a cada 10t. | Tensão estática quase humana. | n/a |
| `hare_tilt_taunt` | 30 | Mantém postura bípede; `hare_neck` e `hare_head` inclinam bruscamente 35° à esquerda (ticks 0–8), pausam farejando em escárnio (8–18), inclinam 35° à direita (18–26) e centralizam. Orelha externa atrasa 2t. | Movimento espasmódico e inquietante. | n/a |
| `hare_rupture` | 40 | A lebre abatida levita 1,2b acima do solo; as 4 patas esticam rígidas para fora; `hare_head` reclina para trás emitindo fachos rubis; o peito racha expelindo fumaça negra até o colapso no tick 40. | Suspensão em gravidade zero antes da erupção. | 40 (erupção) |

---

## 4. Animações-base, Locomoção (M1–M5) e Ataques Originais (O1–O5)

### 4.1 Despertar, Idles, Hurt e Morte
- **`awaken_eruption` (60t, L4):**
  - *Ticks 0–10:* `brooch_anchor` estabiliza no ar; `pelvis`, `abdomen`, `torso` e `chest` surgem como silhueta de fumaça.
  - *Ticks 10–30:* `pelvis` condensa abaixo do broche; coluna alonga; `chest` fecha ao redor do broche; `neck`, `head_core` e `cowl` formam-se em sequência (`hood_crown → back → left/right → front`). Clavículas, ombros, braços, antebraços, punhos e os 5 dedos de cada mão condensam-se de dentro para fora.
  - *Ticks 30–60:* `scythe_root` materializa do raio gélido (`shaft_upper → mid → lower → pommel → blade_socket → base → tip`); `lower_shroud` e `upper_shroud` desdobram `root → mid → tip`; no quadro final todos os dedos travam nos grips.
- **`awaken_slam` (50t, impacto no tick 25, L4):**
  - `pelvis` recua e sobe no preparo; `abdomen` e `torso` arqueiam para trás; `chest` abre; ambos os braços elevam a foice acima de `cowl` segurando `grip_upper` e `grip_lower`. No tick 25, coluna flexiona em chicote, `pelvis` afunda na névoa e a lâmina crava no gelo; `upper/lower_shroud` caem com atraso de 2–5t nos segmentos `mid/tip`.
- **`idle_1` (50t, L0 — respiração solene):**
  - `root` oscila 0,1b; `pelvis` segue com atraso; `abdomen/torso/chest` sobem 0,15b; `neck/cowl` mantêm olhar no alvo. Mão direita fecha em `grip_upper` e esquerda em `grip_lower` com foice a 45° diante do peito; painéis do manto ondulam em fases distintas; broche pulsa lento.
- **`idle_2` (40t, L0 — giro do cabo):**
  - `pelvis` transfere peso à esquerda; mão direita abre os 5 dedos em sequência (polegar a mínimo) liberando `grip_upper`; mão esquerda sustenta e gira `scythe_root` 180° sem cruzar o capuz; `scythe_pommel` bate no solo no tick 28 com vibração curta; mão direita reassume `grip_upper` nos ticks 30–40.
- **`idle_3` (60t, L0 — manto como asas):**
  - `pelvis` sobe; `torso` inclina 10° para trás; ombros abrem em arco; mão direita sustenta `grip_upper` e dedos da esquerda se estendem como garras; `upper_shroud` abre como asas (`root → mid → tip`) e `lower_shroud` ergue névoa antes de cair em cascata.
- **`hurt` (14t, L3 — apenas para golpes pesados acima do limiar):**
  - `pelvis` recua; `abdomen` comprime; `torso/chest` recuam 20°; `cowl` sacode lateralmente com atraso; ombros fecham trazendo a foice em guarda diante do broche; manto frontal fecha sobre o peito.
- **`death` (90t, queda da foice no tick 35, L4):**
  - `pelvis → abdomen → torso → chest` perdem sustentação; `cowl` murcha (`crown → front → sides/back`); dedos de ambas as mãos abrem um a um liberando os grips; a foice cai e crava no tick 35 partindo `scythe_blade_base/tip`; o manto desaba vazio no chão (`root → mid → tip`) e o broche pisca pela última vez no tick 85.

### 4.2 Cinco Locomoções (M1–M5, camada L0)

| Código | Nome | Ciclo / Tempo | Vel. | Coreografia completa (Corpo, Foice e Manto) |
|---|---|---|---:|---|
| **M1** `glide` | Deslize Espectral | 30t (loop) | `1.0×` | Unifica `walk` e `glide`. `root` oscila 0,1b vertical; `pelvis` e `torso` balançam ≤ 5°; `cowl` fixo no alvo. Braço direito mantém cotovelo dobrado com mão em `grip_upper` junto ao quadril; braço esquerdo pende relaxado em contrafluxo com dedos semicerrados. Foice diagonal (lâmina para trás/cima, pomo roçando a neve). Manto arrasta em 4 fases distintas. |
| **M2** `orbit` | Órbita de Fumaça | 40t / quarto de volta | `0.9×` | Desliza em arco (raio 8b), corpo inclinado 10° para o centro; `torso` e `cowl` orientados ao alvo. Braço direito estende para fora segurando `grip_upper` como ponteiro de relógio; braço esquerdo recolhido junto ao peito pronto para `grip_lower`. Foice horizontal apontando a lâmina para o jogador. Manto externo abre em leque. |
| **M3** `staff_stride` | Passada de Cajado | 24t / passada | `0.8×` | `root` sobe/desce 0,25b; `pelvis` e `torso` inclinam a cada apoio; `cowl` acompanha com atraso de 2t. Braço direito usa a foice na vertical como cajado, batendo `scythe_pommel` na neve a cada 24t (lâmina para o alto); braço esquerdo livre abre e fecha os dedos no ritmo da batida. Manto levanta pluma de neve a cada apoio. |
| **M4** `predator_run` | Perseguição do Predador | até 50t (recarga 160t) | `1.4×` | `root` abaixa 0,6b e inclina 30° à frente; `pelvis` recua; `torso` quase horizontal; `cowl` alinhado como focinho (olhos estreitos). Braço esquerdo projeta à frente com os 5 dedos abertos em garra; braço direito estica para trás segurando `grip_upper` e arrastando `scythe_blade_tip` na neve (sulco de faíscas azuis). Manto esticado na horizontal para trás. Não causa dano de contato. |
| **M5** `mist_ascent` | Ascensão da Névoa | 30t subida / 14t pouso | `0.7×` vert. | Servidor eleva a entidade (com hitbox) até `min(alvoY - 1, solo + 6)` respeitando teto. `pelvis` é envolvida por coluna de névoa; `torso` vertical; `cowl` olha para o alvo no alto. Braço direito ergue a foice na vertical (`grip_upper`) e gira uma volta lenta no ápice como farol; braço esquerdo fechado na cintura. No pouso, `pelvis` e `lower_shroud` comprimem e espalham em anel. |

### 4.3 Cinco Ataques Originais (O1–O5, camada L2)
- **O1 `whirlwind` — Ceifa Giratória 720° (Prep 24t, Exec 16t [impactos 6 e 12], Recup 20t):**
  - *Preparo:* `pelvis`, `abdomen` e `torso` torcem 90° à direita; `cowl` mantém olhar no alvo; ambas as mãos travam `grip_upper` e `grip_lower` com foice horizontal recuada; manto enrola junto ao corpo.
  - *Execução:* `actionSpinYaw` acumula 720° em 16t; `pelvis` lidera a rotação seguida por `abdomen/torso/chest`; braços estendidos projetam a foice em raio `5.5b`; `upper/lower_shroud` abrem em disco com atraso nos segmentos `mid/tip`. No Estágio 3, dispara 8 projéteis de gelo no tick 14.
  - *Recuperação:* `pelvis` e `torso` absorvem o torque; foice completa meia-volta amortecida de retorno à guarda; manto chicoteia e assenta 6t depois.
- **O2 `blink_strike` — Passo Espectral com Corte em Cunha (Prep 18t, Exec 12t [impacto 5], Recup 16t):**
  - *Preparo:* `pelvis` desce; coluna fecha; braços cruzam diante do broche segurando os grips; corpo e foice dissolvem-se em **fumaça cinza-escura** (lâmina primeiro, pomo por último).
  - *Execução:* reaparece a 3b do alvo com braços elevados acima do capuz; no tick 5 a coluna chicoteia para baixo e a foice desfere corte vertical cravando a ponta no solo.
  - *Recuperação:* mão direita puxa `grip_upper` e esquerda faz alavanca em `grip_lower` para arrancar a lâmina do gelo; tronco se endireita.
- **O3 `soul_harvest` — Colheita de Almas (Prep 30t, Loop 40t [pulsos 10, 20, 30, 40], Recup 25t):**
  - *Preparo:* `root` sobe 0,8b; coluna alonga; `cowl` inclina para o céu; ambas as mãos erguem a foice vertical acima do capuz; runas acendem da base à ponta.
  - *Loop:* tronco e broche contraem a cada 10t; dedos apertam e relaxam sem soltar o cabo; feixes azuis ligam apenas alvos reais em LOS e Almas Vagas ao `brooch_anchor`; pontas do manto curvam para cima em sucção.
  - *Recuperação:* pousa suave, baixa a foice à diagonal e expele anel de vapor gélido.
- **O4 `frost_wave` — Corte de Geada Rasteiro (Prep 20t, Exec 10t [disparo 5], Recup 18t):**
  - *Preparo:* `pelvis` recua; `torso` inclina à direita; mão direita em `grip_upper` e esquerda em `grip_lower` abaixam a ponta da lâmina para raspar o gelo faiscando azul.
  - *Execução:* transferência de peso para a frente e varredura ascendente que dispara a onda cortante no tick 5 em trajetória reta fixa.
  - *Recuperação:* braços recolhem à guarda e manto assenta.
- **O5 `reaping_dash` — Vórtice em Avanço Espiral (Prep 22t, Exec 18t [impactos 4, 9, 14], Recup 24t):**
  - *Preparo:* inclina 35° à frente; coluna comprime como mola; braços alinham a foice no flanco como broca; manto estica para trás.
  - *Execução:* servidor avança até 12b em linha reta enquanto o modelo executa rotação axial de 360° em parafuso com braços compactos nos grips.
  - *Recuperação:* derrapagem lateral cravando a lâmina para frear; manto ultrapassa o tronco e retorna.

---

## 5. Seis Ataques Frontais (F1–F6, camada L2)

| Código | Nome | Tempos (Prep / Exec / Recup) | Impacto(s) na Exec | Dano | Coreografia de Corpo, Braços/Mãos, Foice e Manto |
|---|---|---|---|---:|---|
| **F1** `rising_crescent` | Lua Ascendente | `20 / 10 / 22` | `6` | 14 | `root` avança `0.8b`; `pelvis` e `abdomen` torcem à esquerda e abaixam no preparo, desenrolando para cima na execução; `cowl` segue com atraso de 2t. Braço direito leva `grip_upper` rente ao solo atrás do quadril direito; mão esquerda desliza 1/3 do cabo fazendo alavanca. A ponta da lâmina raspa a neve e sobe em meia-lua de 140° apontando para o céu no ápice. Manto sobe em onda e assenta 8t depois. |
| **F2** `haft_thrust` | Estocada do Cabo | `14 / 8 / 18` | `4` | 12 | `root` abaixa `0.3b` e avança `1.5b`; `torso` gira 90° à direita no preparo e dispara em linha reta; `cowl` imóvel no alvo. Foice gira 180° no preparo apontando `scythe_pommel` para a frente e lâmina para trás; cotovelo direito dobra ao máximo e estica como pistão enquanto a mão esquerda guia o pomo. No retorno, punhos giram a foice 180° de volta à guarda. |
| **F3** `cross_reap` | Ceifa Cruzada | `18 / 24 (8+6+10) / 26` | `4`, `20` | 10 + 13 | Referencial local do boss: 1º corte (ticks 0–7) varre da esquerda para a direita do Ceifeiro na horizontal conduzido pelo braço direito mentre o esquerdo faz contrapeso; no intervalo (ticks 8–13) a mão esquerda fecha em `grip_lower` e os punhos giram a lâmina 90° sem soltar o cabo; 2º corte (ticks 14–23, avanço de `1b`) desce da direita para a esquerda em diagonal com as duas mãos, deixando rastro em X por 12t. |
| **F4** `gravedigger_slam` | Cova do Coveiro | `32 / 12 / 36` | `7` | 20 (ou 8 cone) | `root` sobe `0.5b` no ápice do preparo; `abdomen` e `torso` arqueiam para trás; ambas as mãos elevam a foice na vertical acima do capuz acumulando brilho por 32t. Na execução, coluna fecha em dobradiça e crava a lâmina no tick 7 (alvo no alcance direto `5b` leva 20; alvo só no cone `8b` leva 8, nunca somados). O cabo vibra presa no gelo durante a recuperação punível de 36t. |
| **F5** `low_sweep` | Varredura Rasteira | `22 / 12 / 20` | `6` | 11 | `root` agacha `0.8b`; `torso` inclina quase horizontal; `cowl` olha por baixo da aba com olhos acesos. Mão direita em `grip_upper` e esquerda cruzada em `grip_lower` mantêm a lâmina paralela ao solo a `0.3b` de altura, varrendo 200° e levantando leque de neve. Manto inferior abre em disco rente ao chão. Jogador salta nos ticks 4–8 para evitar. |
| **F6** `threefold_harvest` | Rajada Tripla (Est. 3) | `16 / 24 (7+7+10) / 30` | `3`, `10`, `20` | 7 + 7 + 11 | Avança `1b` por corte patinando. 1º corte (ticks 0–6, impacto 3): torso torce à esquerda conduzido pela mão direita. **Troca de empunhadura (ticks 5–8, 3t):** mão esquerda desliza suavemente para `grip_upper` e direita para `grip_lower`. 2º corte (ticks 7–13, impacto 10): braço esquerdo conduz o arco reverso. 3º corte (ticks 14–23, impacto 20): ambas as mãos e o tronco inteiro desferem o corte final abrindo crista de gelo. |

---

## 6. Quatro Esquivas (E1–E4, camada L2)

**Regras de contenção:** Cooldown mínimo de 60 ticks entre esquivas; máximo de **2 esquivas em uma janela de 240 ticks** (a 2ª esquiva dentro da janela adiciona +6 ticks de abertura vulnerável no final). O boss nunca esquiva se já estiver na fase de Execução de um ataque próprio.

| Código | Nome | Duração / I-frames | Abertura final | Coreografia completa |
|---|---|---|---|---|
| **E1** `sidestep_glide` | Deslize Lateral | 12t (invuln. `2–8`) | 10t de perfil | Desliza `4b` para o lado oposto ao golpe; `pelvis` lidera e `torso` inclina em oposição; `cowl` permanece cravado no jogador. Mão direita recolhe `grip_upper` e esquerda mantém `grip_lower` junto ao quadril (lâmina para cima). Manto chicoteia 3t atrasado. Termina de perfil sem cancelar os 10t de abertura. |
| **E2** `phantom_retreat` | Recuo Fantasma | 16t (invuln. `0–10`) | 8t antes de F2 | Flutua `6b` para trás a `0.5b` do solo; `torso` arqueia; olhos apagam no recuo e reacendem no pouso. Ambas as mãos seguram a foice na horizontal diante do peito como barreira e cravam o pomo no pouso. Se encadear F2, respeita os 8t de aviso e o estalo de inversão da lâmina. |
| **E3** `snow_dissolve` | Dissolução em Neve | 14t (invuln. `0–14`) | 8t guarda baixa | Puramente defensiva (zero dano). O corpo cruza os braços sobre o broche e se desfaz de baixo para cima em **flocos de neve branca/prateada** (diferente da fumaça escura do Blink ofensivo); a foice dissolve da ponta ao pomo. Reaparece a até `5b` montando-se de cima para baixo com a foice abaixada por 8t vulneráveis. |
| **E4** `scythe_vault` | Salto com a Foice | 26t (invuln. `6–18`) | 10t agachado | No preparo (ticks 0–5), ambas as mãos fincam a ponta da foice no chão à frente; no voo (ticks 6–18), o corpo salta `3b` de altura e `6b` de distância sobre o jogador impulsionado pelo cabo como salto com vara (`upper/lower_shroud` abrem em paraquedas); pousa atrás do alvo com `pelvis` baixa por 10t de recuperação real. |

---

## 7. Cinco Provocações (P1–P5, camada L2)

**Regras:** Cooldown de 300t por provocação e 200t global. **Bait:** 30% de chance em luta solo e **20% com > 2 jogadores** de converter no ataque indicado exatamente no tick de virada (nunca antes de completar o sinal visual/sonoro).

| Código | Nome | Duração | Tick de Bait | Coreografia completa |
|---|---|---:|---|---|
| **P1** `beckon` | O Convite | 36t | Tick `28` → `F2` | `pelvis/torso` inclinam em cortesia; `cowl` inclina de lado; mão direita apoia a foice vertical no solo (`grip_upper`) pulsando no ritmo da mão esquerda, que estende a palma para cima e fecha/abre indicador–mínimo duas vezes num gesto claro de "venha". |
| **P2** `funeral_bow` | A Reverência Fúnebre | 50t | Tick `38` → `F3` | `root` desce `0.4b`; `abdomen/torso` curvam profundamente; `cowl` baixa apagando os olhos; braço direito cruza sobre o broche e esquerdo abre para o lado sustentando a foice deitada na horizontal; manto forma círculo no chão. Se virar F3, alinha o tronco antes de cortar. |
| **P3** `hourglass_count` | A Contagem | 64t | Tick `54` → `O4` | Coluna rígida como relógio; `cowl` varre devagar procurando o alvo; braço direito ergue a foice como pêndulo em 3 oscilações; mão esquerda diante do peito levanta um dedo a cada 12 ticks na ordem `polegar → indicador → médio → anelar → mínimo`. |
| **P4** `blade_honing` | O Afiar | 44t | Tick `36` → `F4` (-8t prep) | `torso` inclina sobre a foice sustentada na horizontal pela mão esquerda (`grip_lower` e antebraço); o polegar direito percorre o dorso da lâmina gerando faíscas azuis e tinido metálico enquanto os outros 4 dedos ficam curvados longe do fio. |
| **P5** `hare_tilt_echo` | O Olhar da Lebre | 40t | Sem bait (narrativa) | Foice fica fincada no gelo ao lado; `torso` assume a postura bípede rígida da lebre; ambos os braços recolhem ao peito com punhos e dedos dobrados como patas dianteiras; `cowl` repete a inclinada brusca de 35° (esquerda → pausa → direita) enquanto `brooch_anchor` pisca duas vezes em vermelho-rubi. |

---

## 8. Seis Anulações (A1–A6, camadas L2 e L3)

As regras mecânicas, janelas `T0` e diálogos estão em [soul_reaper_systems.md](soul_reaper_systems.md). Abaixo está a coreografia obrigatória de cada uma:

- **A1 `vial_silence` — Silêncio da Garrafa (36t [ou 14t comprimida no Est. 3], L2):**
  - `neck/cowl` travam no consumidor em `T0` com olhos azuis acesos; braço direito mantém a foice escurecida junto ao corpo; braço esquerdo estende a palma aberta (`T0+8..18` nos Est. 1–2; `T0+2..6` no Est. 3) e fecha os dedos um a um (`mínimo → anelar → médio → indicador → polegar`) puxando os fios coloridos da poção para esmagá-los em fumaça cinza na resolução (`T0+20` ou `T0+8`); no final o pomo bate uma vez no solo.
- **A2 `apple_toll` — Cobrança da Maçã (44t, resolução no tick 22, L2):**
  - Desliza `1b` à frente; `torso` e `cowl` curvam como quem fareja o ouro (aba frontal treme 2t); braço esquerdo abre para trás em contrapeso e braço direito ergue a foice horizontal fazendo um gesto de colheita no ar no tick 22; a ponta fica dourada por 6t e apaga em cinza.
- **A3 `greater_toll` — Cobrança Maior (60t, resolução no tick 30, L2):**
  - Finca a ponta da foice no gelo e flutua `0.5b`; `cowl` inclina de lado como a lebre e depois olha para o céu; ambos os braços se abrem como asas (`upper_shroud` acompanha) e no tick 30 as duas palmas batem uma na outra num estalo seco que apaga o brilho roxo/dourado da lâmina.
- **A4 `flick_snuff` — Gesto Leve (14t, camada L3 sobreposta):**
  - Não interrompe locomoção nem ataque; `neck/cowl` viram 20° para o consumidor; `left_hand` faz um estalo (`left_thumb` contra `left_middle`, `left_index` aberto); a lâmina pisca uma vez. Usado para consumidores simultâneos ou como aviso imediato em `T0` quando o boss está no meio de um golpe.
- **A5 `shield_tithe` — Anulação da Guarda (24t, marca no tick 16, Est. 2+, L2):**
  - *Ticks 0–8:* `pelvis` e `torso` alinham ao escudo do alvo; `cowl` foca no escudo.
  - *Ticks 8–16:* braço direito recolhe e posiciona a ponta da foice; braço esquerdo estende com os 5 dedos abertos apontando para a defesa.
  - *Tick 16:* a ponta da lâmina toca a face do escudo gravando um risco de geada enquanto a mão esquerda fecha do mínimo ao polegar; se o jogador ainda estiver bloqueando no contato, o escudo é desativado por 30t.
- **A6 `arrow_reversal` — Anulação do Disparo (30t, janela de reflexão nos ticks 20–30, Est. 2+, L2):**
  - *Ticks 0–10:* `pelvis`, `torso` e `cowl` travam no arqueiro.
  - *Ticks 10–20:* ambas as mãos posicionam a foice em diagonal diante do peito formando uma película azul no plano frontal da lâmina.
  - *Ticks 20–30:* ao interceptar o primeiro projétil frontal, os punhos giram a foice 90° devolvendo o disparo em trajetória visível limitada.

---

## 9. Parte B: Tabela de Ângulos e Critérios de Aceite da Fatia 2

*Tabela preenchida na Fatia 2 a partir dos valores reais implementados em `SoulReaperModel.java`, `SoulReaperAnimations.java` e `OmenHareAnimations.java` (interpolação `CATMULLROM`). Estado atual: `pendente de revisão visual` (2b).*

| Animação | Junta principal | Rot inicial (x/y/z, graus) | Rot pico / impacto (x/y/z, graus) | Rot final (x/y/z, graus) | Curva | Estado |
|---|---|---|---|---|---|---|
| `POSE_REST` (Vigil) | `scythe_root` / `left_upper_arm` / `right_upper_arm` | `(6.9, 0.0, -24.1)` / `(-32.1, 13.8, -12.6)` / `(-20.6, -10.3, 16.0)` | — | `(6.9, 0.0, -24.1)` | Pose base | pendente de revisão visual |
| `IDLE_1` (4.0s loop) | `chest` / `scythe_root` | `(0, 0, 0)` / `(0, 0, 0)` | `(-3.5, 0, 0)` / `(-3.0, 2.0, 2.5)` @ 2.0s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `IDLE_2` (3.0s) | `head` / `left_upper_arm` / `left_forearm` | `(0, 0, 0)` | `(16, 22, -8)` / `(-38, -18, -12)` / `(-32, 28, 0)` @ 1.2s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `IDLE_3` (3.5s) | `chest` / `head` | `(0, 0, 0)` | `(8, -14, -6)` → `(6, 16, 6)` @ 0.6s..1.1s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `AWAKEN_ERUPTION` (8.0s) | `chest` / `head` / `jaw` / `scythe_root` | `(48, 0, 0)` / `(42, 0, 38)` / `(0, 0, 0)` / `(65, 0, -30)` | `(-24, 0, 0)` / `(-28, 0, 0)` / `(34, 0, 0)` / `(-55, 0, 24)` @ 5.8s..6.2s | `(0, 0, 0)` / `(0, 0, 0)` / `(4, 0, 0)` / `(-35, 0, 18)` | `CATMULLROM` | pendente de revisão visual |
| `AWAKEN_SLAM` (2.0s) | `chest` / `scythe_root` | `(-24, 0, 0)` / `(-45, 0, 22)` | `(26, 0, 0)` / `(18, 0, 24)` + `pos(0,-6,-2)` @ 0.45s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `O1_WHIRLWIND` (3.5s) | `reaper_root` / `scythe_root` / `shroud_strips` | `(0, 0, 0)` | Windup `(6, -55, -8)` @ 0.9s → Spin `(-8, 1080, 0)` c/ foice `(78, 15, 64)` e tiras `(-62..68)` @ 1.15s..2.9s | `(0, 1080, 0)` / `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `O2_BLINK_STRIKE` (1.75s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(-26, -38, -14)` / `(-72, -35, -42)` @ 0.75s → Corte `(34, 42, 16)` / `(58, 48, 52)` @ 1.10s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `O3_SOUL_HARVEST` (4.5s) | `chest` / `rib_flaps_L,R` / `jaw` / `left_upper_arm` | `(0, 0, 0)` | `(-26, 0, 0)` / `(0, ∓52, ∓22)` / `(38, 0, 0)` / `(-78, -12, -24)` @ 1.4s..3.5s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `O4_FROST_WAVE` (2.25s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(-32, 0, 0)` / `(-82, 0, 24)` @ 0.70s → Slam `(38, 0, 0)` / `(64, 0, 24)` @ 1.05s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `O5_REAPING_DASH` (2.0s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(34, -48, -14)` / `(42, -68, -48)` @ 0.55s → Dash `(28, 62, 18)` / `(52, 84, 62)` @ 1.10s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `F1_RISING_CRESCENT` (1.6s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(28, -36, -16)` / `(54, -48, -44)` @ 0.50s → Uppercut `(-24, 42, 18)` / `(-68, 52, 48)` @ 0.80s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `F2_HAFT_THRUST` (1.2s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(-12, -18, 0)` + `pos(0,2,5)` @ 0.35s → Estocada `(22, 14, 0)` + `pos(0,0,-9)` @ 0.55s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `F3_CROSS_REAP` (2.1s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(-16, -42, -12)` @ 0.55s → Corte 1 `(24, 46, 16)` / `(52, 64, 54)` @ 0.90s → Corte 2 `(22, -48, -16)` / `(48, -68, -52)` @ 1.30s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `F4_GRAVEDIGGER_SLAM` (2.2s) | `chest` / `scythe_root` | `(0, 0, 0)` | Apex `(-36, 0, 0)` / `(-88, 0, 24)` + `pos(0,8,0)` @ 0.75s → Slam `(44, 0, 0)` / `(74, 0, 24)` + `pos(0,-5,-3)` @ 1.10s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `F5_LOW_SWEEP` (1.5s) | `chest` / `scythe_root` | `(0, 0, 0)` | Prep `(28, 52, 12)` / `(64, 72, 58)` @ 0.45s → Rasteira `(32, -68, -16)` / `(68, -88, -62)` @ 0.80s | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `F6_THREEFOLD_HARVEST` (2.9s) | `reaper_root` / `chest` / `scythe_root` | `(0, 0, 0)` | Golpe 1 `(44, 68, 54)` @ 1.0s → Golpe 2 `(-64, -48, -38)` @ 1.45s → Giro 360° + Slam `(72, 0, 24)` @ 2.0s | `(0, 360, 0)` / `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `E1..E4` (0.6s..0.9s) | `reaper_root` / `chest` / `scythe_root` | `(0, 0, 0)` | `E1` bank `(10, -18, 24)` / `E2` guard `(-18, 0, -42)` / `E3` sink `pos(0,-24,0)` / `E4` vault `pos(0,10,4)` | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `P1..P5` (2.0s..2.5s) | `chest` / `head` / `left_upper_arm` / `scythe_root` | `(0, 0, 0)` | `P1` index `(-68, 0, 0)` / `P2` bow `(38, 0, 0)` / `P3` dedos `58°` / `P4` afiar `(-64, -12, -18)` / `P5` tilt `(10, 0, 44)` | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `A1..A6` (0.5s..2.2s) | `chest` / `left_upper_arm` / `scythe_root` | `(0, 0, 0)` | `A1` punho `72°` / `A2` puxão `(-34, -38, -24)` / `A3` costelas `∓45°` / `A4` flick `(-44, -22, -16)` / `A5` gancho `(-28, -64, -42)` / `A6` roda `360°` | `(0, 0, 0)` | `CATMULLROM` | pendente de revisão visual |
| `HURT` (0.35s) / `DEATH` (4.5s) | `chest` / `head` / `rib_flaps_L,R` / `scythe_root` | `(0, 0, 0)` | `HURT` `(-12, 8, -6)` @ 0.10s · `DEATH` colapso `pos(0,-24,0)` + tronco `(64, 0, 0)` + costelas `∓58°` | Colapso no solo | `CATMULLROM` | pendente de revisão visual |
| `HARE_APPROACH..RUPTURE` | `body` / `head` / `left_ear,right_ear` | `(0, 0, 0)` | `STAND_STARE` `body(-65,0,0), head(62,0,0)` · `TILT_TAUNT` `head(62,0,44)` · `RUPTURE` `pos(0,12,0), body(-90,0,0)` | Loop / Ruptura | `CATMULLROM` | pendente de revisão visual |

### Checklist obrigatório de aceite visual (Fatia 2 / 2b)
- [ ] Todas as juntas da seção 1.2 existem no modelo e `scythe_root` é filho de `chest`.
- [ ] Mãos mantêm contato com `grip_upper` e `grip_lower` sem deslizar durante `idle_1`, `glide`, `O1`, `F1`, `F4` e na troca de mãos de `F6`.
- [ ] Cada painel de `lower_shroud` e `upper_shroud` possui 3 segmentos (`root`, `mid`, `tip`) com atraso progressivo visível.
- [ ] A ponta da lâmina nunca atravessa o chão fora dos ticks de impacto no solo (`awaken_slam`, `O2`, `F4`, `death`).
- [ ] Diferença visual imediata entre o Blink ofensivo (`O2`, fumaça cinza-escura) e a Dissolução defensiva (`E3`, flocos de neve branca).
