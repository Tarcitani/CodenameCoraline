# Litófago (Lithophage)

Implementação para Minecraft 26.1.2 / NeoForge 26.1.2.124. Criatura mineral hostil original que habita cavernas, composta de argila endurecida, sedimentos escuros e incrustações de âmbar mineral.

## Entidade e visual

- ID da entidade: `codenamecoraline:lithophage` (`/summon codenamecoraline:lithophage`).
- ID do projétil: `codenamecoraline:amber_shard`.
- Ovo de geração: `codenamecoraline:lithophage_spawn_egg` (**Ovo do Litófago** / *Lithophage Spawn Egg*), disponível na aba de ovos do mod e na aba vanilla de Ovos de Geração. Paleta em cinza-ardósia escuro (`#34383A`) com veios e manchas de âmbar mineral (`#D4A64B`).
- Modelo articulado nativo em `client/lithophage/LithophageModel.java` (~2,1 blocos de altura; caixa de colisão `0,85 × 2,1`, altura dos olhos `1,8`):
  - Tronco largo e robusto (`body`), ombros assimétricos (`right_arm` elevado com crosta de ardósia e cristais; `left_arm` inclinado), braços longos e pesados (`right_forearm`, `left_forearm`), pernas curtas e firmes (`right_leg`, `right_shin`, `left_leg`, `left_shin`), cabeça em formato de máscara de argila desgastada (`head`) com fendas estreitas de brilho âmbar e grupos de cristais minerais (`dorsal_crystals`, `right_shoulder_crystals`, `left_shoulder_crystals`, `held_shard`).
- Textura base `textures/entity/lithophage.png` (128×128) e camada emissiva `textures/entity/lithophage_eyes.png` (128×128) renderizada por `LithophageEyesLayer` apenas quando a criatura está despertando ou desperta.

## Animações

Definidas em `client/lithophage/LithophageAnimations.java` e conectadas aos 12 `AnimationState`s da entidade:

1. `animation.lithophage.idle` (`IDLE`)
2. `animation.lithophage.idle_alternate` (`IDLE_ALTERNATE`)
3. `animation.lithophage.awakening` (`AWAKENING`)
4. `animation.lithophage.alert` (`ALERT`)
5. `animation.lithophage.walk` (`WALK`)
6. `animation.lithophage.turn` (`TURN`)
7. `animation.lithophage.attack_slam` (`ATTACK_SLAM`)
8. `animation.lithophage.attack_sweep` (`ATTACK_SWEEP`)
9. `animation.lithophage.attack_shard` (`ATTACK_SHARD`)
10. `animation.lithophage.attack_recovery` (`ATTACK_RECOVERY`)
11. `animation.lithophage.hurt` (`HURT`)
12. `animation.lithophage.death` (`DEATH`)
13. `animation.lithophage.fall_asleep` (`FALL_ASLEEP`)

## Atributos, estados de IA e combate

| Atributo | Valor |
|---|---|
| Vida máxima | 36 HP (18 corações) |
| Armadura | 6 |
| Velocidade de movimento | 0,22 |
| Resistência a empurrão | 0,55 (55%) |
| Alcance de detecção | 12 blocos |
| Raio de ressonância sísmica | 8 blocos (acorda aliados dormentes em bando) |
| Perseguição máxima | 20 blocos da posição inicial (`homePos`) |
| Experiência | 12 XP |

### Máquina de estados (`Lithophage.State`)

- **A — Adormecido (`DORMANT`):** permanece imóvel como formação rochosa, sem brilho nas fendas e com poeira ocasional de argila. Ao detectar ameaça válida a até 12 blocos (ou sofrer dano), inicia o despertar.
- **B — Despertar (`AWAKENING`):** 30 ticks (1,5 s), executando a elevação do tronco e acendendo o brilho âmbar sem reiniciar nem entrar em loop. **Ressonância Sísmica:** o rugido de despertar propaga tremores pelo leito de rocha (`RESONANCE_RADIUS = 8.0`), acordando todos os outros Litófagos dormentes ou em processo de adormecimento no raio e compartilhando o alvo inicial. Além disso, ao sofrer dano, alerta Litófagos aliados próximos via `HurtByTargetGoal.setAlertOthers()`.
- **C — Vigilância (`VIGILANT`):** orienta cabeça e corpo para o alvo, avalia distância e linha de visão, investiga a última posição conhecida quando aplicável e inicia o adormecimento após 60 ticks (3,0 s) sem ameaça.
- **D — Adormecimento (`FALLING_ASLEEP`):** 28 ticks (1,4 s), executando a animação `FALL_ASLEEP`, ruído sísmico de acomodação (`LITHOPHAGE_AMBIENT`), poeira de argila nos pés, transição suave de postura e atenuação gradual do brilho âmbar das fendas até retornar a `DORMANT`. Pode ser interrompido imediatamente se o jogador se aproximar ou causar dano.
- **E — Perseguição (`PURSUING`):** navegação terrestre pesada, respeitando o limite territorial de 20 blocos a partir de `homePos`.
- **F — Combate (`COMBAT`):**
  - **Golpe de Escória (`SLAM`):** pancada vertical com 0,6 s (12 ticks) de preparação, causando **5 HP** de dano e empurrão moderado no alcance frontal curto, seguido de 0,8 s (16 ticks) de recuperação.
  - **Varredura Tectônica (`SWEEP`):** giro lateral com 0,8 s (16 ticks) de preparação, causando **4 HP** de dano apenas a alvos dentro do arco frontal configurado, seguido de 0,9 s (18 ticks) de recuperação.
  - **Estilhaço de Âmbar (`SHARD`):** preparação de 0,75 s (15 ticks) concentrando partículas âmbar na mão e disparo do projétil `AmberShard` (**4 HP** de dano, recarga de 3–5 s, bloqueado por paredes e sem fogo amigo contra outros Litófagos), seguido de 0,6 s (12 ticks) de recuperação.
- **F — Recuperação (`RECOVERY`):** janela real de vulnerabilidade pós-ataque na qual o Litófago permanece parado e não inicia novos golpes.
- **G — Morte (`DYING`):** interrompe imediatamente qualquer ataque em andamento, executa a animação de colapso (`DEATH`), emite detritos minerais e aplica a tabela de saque uma única vez.

## Biomas, geração natural e loot

- **Lush Caves (principal):** modificador `lithophage_lush_spawns.json` (peso 28, grupos de 1–3).
- **Dripstone Caves (secundário):** modificador `lithophage_dripstone_spawns.json` (peso 16, grupos de 1–2).
- **Regras (`Lithophage.checkSpawnRules`):** altura entre `Y = -20` e `Y = 20`, subterrâneo sem céu visível, luz ≤ 7, solo firme válido, espaço livre para os 2,1 blocos de altura e dificuldade diferente de Pacífico.
- **Tabela de saque (`data/codenamecoraline/loot_table/entities/lithophage.json`):**
  - `codenamecoraline:ancient_clay_fragment` (**Fragmento de Argila Ancestral**): 1–2 unidades (85% de chance).
  - `codenamecoraline:mineral_amber_core` (**Núcleo de Âmbar Mineral**): 1 unidade (30% de chance).

