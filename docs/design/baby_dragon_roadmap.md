# BABY DRAGON — DEVELOPMENT ROADMAP & TECHNICAL ARCHITECTURE
**Projeto:** Codename Coraline (`codenamecoraline`)  
**Plataforma:** Minecraft `26.1.2` | NeoForge `26.1.2.124` | Java `25`  
**Escopo:** Diagnóstico técnico do código-fonte, arquitetura de evolução, roadmap oficial em 12 etapas e registro vivo de implementação e validação do sistema **Baby Dragon**.

---

## Classificação Metodológica (Legenda de Evidências)

Todas as análises deste documento distinguem explicitamente:
- **[COMPROVADO NO CÓDIGO]**: Comportamento ou defeito verificado diretamente por inspeção estática das classes atuais (com referência exata de arquivo, método e linha).
- **[HIPÓTESE PARA TESTE]**: Comportamento inferido da interação com sistemas vanilla/NeoForge que deve ser validado via GameTest (`runBabyDragonTest`) ou reprodução controlada.
- **[REQUISITO DEFINIDO]**: Especificação funcional ou restrição arquitetural estabelecida pelo Prompt Oficial de Execução.
- **[SUGESTÃO ARQUITETURAL]**: Proposta técnica de evolução desenhada para reutilizar a estrutura existente sem criar sistemas paralelos.

---

## Quadro Geral de Status das Etapas

| Etapa | Título | Complexidade | Status Atual |
| :--- | :--- | :--- | :--- |
| **Etapa 1** | Combate térmico, ignição e preservação de drops | Baixa/Média | **Testado** |
| **Etapa 2** | Atributos individuais e persistência | Média | **Testado** |
| **Etapa 3** | Nomeação após domesticação | Baixa/Média | **Testado** |
| **Etapa 4** | Variações visuais (6 por cor/estágio) | Média | **Testado** |
| **Etapa 5** | Inteligência artificial, combate e comandos | Média/Alta | **Testado** |
| **Etapa 6** | Física de voo (autônomo e montado) | Alta | **Testado** |
| **Etapa 7** | Combate avançado | Média/Alta | **Testado** |
| **Etapa 8** | Equipamentos e mochila | Média | **Testado** |
| **Etapa 9** | Totem da Imortalidade | Média/Alta | **Testado** |
| **Etapa 10** | Caça terrestre | Alta | **Testado** |
| **Etapa 11** | Caça aquática | Alta | **Testado** |
| **Etapa 12** | Interações sociais | Média | **Testado** |
| **Futuro** | Ressurreição *(Fora do escopo de implementação atual)* | — | **Pendente (Futuro)** |

---

## 1. Diagnóstico da Arquitetura Atual

### 1.1. Visão Geral das Classes Atuais
| Camada | Classe / Arquivo | Responsabilidade Atual |
| :--- | :--- | :--- |
| **Entidade Central** | [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java) | Subclasse de `TamableAnimal` e `PlayerSummon`. Gerencia `SynchedEntityData` (`STATE`, `AIRBORNE`, `DEFENSIVE`, `SLEEPING`, `STAGE`, `DRAGON_XP`, `EVOLUTION`, `BREATH`, `CHARGING`, `SHOT`), progressão `BABY -> YOUNG -> ADULT`, combate (melee, `DragonEmber`, sopro contínuo), voo montado, pouso no ombro e persistência (`ValueOutput` / `ValueInput`). |
| **IA e Máquina de Estados** | [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java) | Árbitro de movimentação e combate chamado em `BabyDragon.customServerAiStep()`. Controla aquisição de alvos reativos (`selectReactiveTarget`), órbita de defesa (`defend`), acompanhamento terrestre/aéreo, pouso no ombro e comportamento selvagem (`wild`). |
| **Estados, Estágios e Comandos** | [`DragonState.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonState.java), [`DragonStage.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonStage.java), [`DragonCommands.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonCommands.java), [`DragonEvents.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonEvents.java) | Estados (`WILD_IDLE`, `FOLLOWING`, `FLYING`, `ORBITING`, `SHOULDER`, `SITTING`, `DEFENDING`, `RETURNING`), estágios (`BABY`, `YOUNG`, `ADULT`), comandos administrativos `/dragon` e eventos de servidor (`DragonMagmaTrail.tick` e crédito de abate em `LivingDeathEvent`). |
| **Controle de Voo** | [`DragonFlightControl.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonFlightControl.java) | Estende `MoveControl`. Interpola linearmente `deltaMovement` em direção ao ponto alvo (`lerp(..., 0.14)`) e atualiza `yRot`/`xRot`. |
| **Projéteis e Efeitos** | [`DragonEmber.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonEmber.java), [`DragonMagmaTrail.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonMagmaTrail.java), [`ModDragonSystems.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/registry/ModDragonSystems.java) | `DragonEmber` (`ThrowableItemProjectile`) escala tamanho/alcance/dano por estágio; no estágio `ADULT` gera trilhas temporárias de magma (`TEMPORARY_MAGMA`) persistidas em `SavedData` (`DragonMagmaTrail`). |
| **Anatomia e Rig Compartilhado** | [`DragonAnatomy.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonAnatomy.java), [`DragonRig.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonRig.java) | Cinemática determinística compartilhada entre servidor e cliente para pescoço/cabeça/mandíbula, origem do fogo (`getMouthPosition()`) e direção do sopro (`getFireDirection()`). |
| **Inventário e Rede** | [`DragonMenu.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/inventory/DragonMenu.java), [`DragonRiderPayload.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonRiderPayload.java) | Container `SimpleContainer(18)` escalonado por estágio (`0` / `9` / `18` slots) e pacote C2S para controles de montaria. |
| **Renderização e Animações (Java Nativo)** | [`BabyDragonRenderer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderer.java), [`DragonModel.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonModel.java), [`DragonAnimations.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonAnimations.java), [`DragonGeometry.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonGeometry.java) | Renderizador com troca dinâmica de modelo por `DragonStage` (`BabyDragonModel`, `YoungDragonModel`, `AdultDragonModel`), animações nativas `AnimationDefinition` / `KeyframeAnimation` e camada emissiva (`RenderTypes.eyes`). |

---

## 2. Problemas Confirmados por Inspeção do Código

### 2.1. Sistema de Fogo, Ignição e Drops (Resolvidos na Etapa 1)
1. **[COMPROVADO NO CÓDIGO — CORRIGIDO NA ETAPA 1] `DragonEmber` nunca incendiava alvos nem assava carne:**
   - **Arquivo:** [`DragonEmber.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonEmber.java#L46-L66)
   - **Causa Original:** Tanto `onHitEntity` quanto o dano de splash em `onHit` invocavam apenas `BabyDragon.damageEnemy(...)`, sem chamar `target.igniteForSeconds(...)`. Como `damageEnemy` aplica `damageSources().mobAttack(this)`, animais abatidos por `DragonEmber` morriam com `isOnFire() == false`, falhando na condição `minecraft:furnace_smelt` das loot tables vanilla.
2. **[COMPROVADO NO CÓDIGO — CORRIGIDO NA ETAPA 1] Ordem invertida de dano e ignição no sopro (`applyBreathDamage`):**
   - **Arquivo:** [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java#L570)
   - **Causa Original:** `if (this.damageEnemy(target, 5)) target.igniteForSeconds(3);` executava `target.hurtServer(...)` **antes** de `target.igniteForSeconds(3)`. Se o pulso fosse letal, `die()` e `dropAllDeathLoot()` rodavam dentro de `hurtServer()` enquanto `target.isOnFire()` ainda era `false`, gerando carne crua.
3. **[COMPROVADO NO CÓDIGO — CORRIGIDO NA ETAPA 1] `DragonMagmaTrail` causava dano físico sem ignição e podia colocar bloco sobre `ItemEntity`:**
   - **Arquivo:** [`DragonMagmaTrail.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonMagmaTrail.java#L43-L75)
   - **Causa Original:** `tick` chamava `dragon.damageEnemy(target, 2)` sem ignição, e `place` verificava apenas `LivingEntity.class` antes de colocar o bloco sólido `TEMPORARY_MAGMA`.

### 2.2. Problemas no Sistema de Combate e IA (`DragonBrain.java`)
1. **[COMPROVADO NO CÓDIGO] Desistência arbitrária de alvo após 240 ticks (12 segundos):**
   - **Arquivo:** [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java#L32-L35) (`this.combatTicks > 240` limpa `setTarget(null)` mesmo em combate ativo).
2. **[COMPROVADO NO CÓDIGO] Órbita circular cega de raio fixo (5 blocos):**
   - **Arquivo:** [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java#L118-L120) (`defend` orbita a 5m fixos sem adaptar ao estágio ou validar obstáculos antes de calcular a órbita).
3. **[COMPROVADO NO CÓDIGO] Indefensabilidade total quando sentado (`isOrderedToSit()`):**
   - **Arquivo:** [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java#L32) e [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java#L87) (desativa completamente `selectReactiveTarget` quando em descanso/`STAY`).
4. **[COMPROVADO NO CÓDIGO] Fuga imediata de qualquer líquido (`isInLiquid()`):**
   - **Arquivo:** [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java#L41-L44) e [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java#L420-L423) (trata água limpa igual a lava, impedindo caça aquática).

### 2.3. Problemas de Atributos, Variantes, Nomeação e Equipamentos
1. **[COMPROVADO NO CÓDIGO — CORRIGIDO NA ETAPA 2] Atributos fixos por estágio:** [`BabyDragon.applyStageAttributes`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java#L184-L194) aplicava constantes fixas de `DragonStage` sem atributos individuais.
2. **[COMPROVADO NO CÓDIGO — CORRIGIDO NA ETAPA 4] Textura única por estágio:** [`BabyDragonRenderer.getTextureLocation`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderer.java#L25-L27) retornava apenas 1 textura por estágio.
3. **[COMPROVADO NO CÓDIGO — CORRIGIDO NA ETAPA 3] Ausência de fluxo de nomeação pós-domesticação:** [`BabyDragon.mobInteract`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java#L318-L331) domesticava com salmão sem abrir tela de nomeação.

---

## 3. Diretrizes e Correções Técnicas Obrigatórias Incorporadas

1. **Totem da Imortalidade (Etapa 9):**
   - **Descartada** a abordagem de cancelar `LivingDeathEvent` e recriar manualmente os efeitos vanilla.
   - A implementação na Etapa 9 inspecionará o fluxo real de proteção de morte por Totem do Minecraft/NeoForge (`LivingEntity.checkTotemDeathProtection` / hooks NeoForge) para que o Totem equipado no dragão participe autenticamente do mecanismo de dano fatal, preservando estatísticas, eventos, efeitos e pacote `EntityEvent.TALISMAN_ACTIVATE` (ID 35) no jogador.
2. **Proteção de Drops (Etapa 1):**
   - **Descartado** o uso genérico de `ItemEntity.setInvulnerable(true)` ou alteração global de resistência ao fogo.
   - Correção baseada exclusivamente na causa comprovada: ignição prévia ao dano letal em `damageEnemyWithFire`, seletores estritos em `LivingEntity` e prevenção de colocação de `TEMPORARY_MAGMA` sobre `ItemEntity`.
3. **Inventário e Mochila (Etapa 8):**
   - **Proibido** assumir que expandir `SimpleContainer` de 18 para 20 slots preserva índices automaticamente.
   - `BabyDragon` atualmente salva `DragonInventory` via `ContainerHelper.saveAllItems(output.child("DragonInventory"), this.inventory.getItems())` (slots `0..17`). Qualquer adição de slots de equipamento na Etapa 8 usará armazenamento separado ou migração explícita, idempotente e testada, garantindo perda zero em mundos existentes.
   - **Regra inegociável:** O jogador continuará podendo montar o dragão adulto **sem sela**.
4. **Atributos Individuais (Etapa 2):**
   - Valores de faixas e multiplicadores são tratados como propostas experimentais; antes de fixar os valores finais na Etapa 2, será avaliado o balanceamento em relação aos valores base de `DragonStage`.
5. **Regeneração Passiva:**
   - **Removida** do escopo. A recuperação de vida do dragão ocorre por alimentação direta pelo dono (`Items.SALMON` em `mobInteract`) e, a partir das Etapas 10/11, pelo consumo de presas abatidas quando o dragão estiver ferido.
6. **Ressurreição:**
   - Mantida apenas como registro conceitual futuro. **Não** será criado `DragonBondSavedData` nem infraestrutura antecipada.

---

## 4. Roadmap Oficial de Execução (12 Etapas)

### ETAPA 1 — Combate térmico, ignição e preservação de drops
- **Status:** Testado
- **Implementação Realizada:**
  - Criado o método `BabyDragon.damageEnemyWithFire(LivingEntity target, float damage, int igniteSeconds)` que valida proteção (`isProtected`), salva `previousFire = target.getRemainingFireTicks()`, aplica `target.igniteForSeconds(igniteSeconds)` **antes** de `this.damageEnemy(target, damage)` caso `!target.fireImmune()`, e restaura `previousFire` caso `hurtServer` seja rejeitado (`!hurt`).
  - Atualizado `BabyDragon.applyBreathDamage()` para utilizar `this.damageEnemyWithFire(target, 5, 3)`, corrigindo a inversão de ordem em que alvos mortos no primeiro pulso do sopro morriam antes de pegar fogo.
  - Atualizado `DragonEmber.onHitEntity` (`3 + stage.ordinal()` segundos) e `DragonEmber.onHit` splash (`2 + stage.ordinal()` segundos) para utilizar `damageEnemyWithFire`, além de reforçar `!(entity instanceof ItemEntity)` em `canHitEntity`.
  - Atualizado `DragonMagmaTrail.tick` para utilizar `dragon.damageEnemyWithFire(target, 2, 2)` e `DragonMagmaTrail.place` para recusar colocação do bloco sólido `TEMPORARY_MAGMA` sobre `ItemEntity`.
- **Arquivos Criados:** Nenhum.
- **Arquivos Modificados:**
  - [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java)
  - [`DragonEmber.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonEmber.java)
  - [`DragonMagmaTrail.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonMagmaTrail.java)
  - [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java)
- **Decisões Técnicas:**
  - Manter `damageSources().mobAttack(this)` dentro de `damageEnemy` combinado com ignição prévia em `damageEnemyWithFire`: garante que as loot tables vanilla/NeoForge reconheçam `is_on_fire: true` na entidade abatida (convertendo carnes cruas em assadas via `SmeltItemFunction`), preserva 100% da atribuição de dano/XP/abate ao dragão e permite que o impacto físico continue atingindo inimigos imunes a fogo (ex.: `Blaze`) sem incendiá-los.
  - Preservação de drops sem `ItemEntity.setInvulnerable(true)` global: assegurada pelo filtro estrito de `LivingEntity` nas áreas de dano, exclusão explícita em `DragonEmber.canHitEntity` e bloqueio de colocação de `TEMPORARY_MAGMA` sobre `ItemEntity` em `DragonMagmaTrail.place`.
- **Testes Executados:**
  - `.\gradlew.bat test runBabyDragonTest --no-daemon`
  - Novos GameTests adicionados e executados em `BabyDragonGameTests`:
    - `dragon_fire_cooks_meat_all_stages` (valida `COOKED_CHICKEN`, `COOKED_PORKCHOP`, `COOKED_BEEF`, ignição de splash e ausência de carnes cruas nos 3 estágios).
    - `dragon_breath_and_magma_cook_and_preserve_drops` (valida abate letal no primeiro pulso do sopro gerando carne assada, sobrevivência dos drops a pulsos subsequentes de sopro e projéteis, bloqueio de `DragonMagmaTrail.place` sobre `ItemEntity` e abate térmico por magma gerando `COOKED_MUTTON`).
    - `dragon_fire_respects_immune_and_invulnerable` (valida que `Blaze` sofre dano físico sem pegar fogo, alvo invulnerável não pega fogo e lobo aliado permanece intacto).
- **Resultados:**
  - Compilação limpa e **84 de 84 GameTests aprovados** (`All 84 required tests passed :)` em 3.652s), além da suíte JUnit `:test`.
- **Problemas Conhecidos:** Nenhum.
- **Próxima Etapa:** Etapa 2 — Atributos individuais e persistência (iniciando pela proposta de distribuição e balanceamento dos atributos antes de fixar os multiplicadores finais).

### ETAPA 2 — Atributos individuais e persistência
- **Status:** Testado
- **Implementação Realizada:**
  - Adicionados os 4 atributos individuais sincronizados (`SynchedEntityData`) em [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java): `INTELLIGENCE`, `VITALITY`, `STRENGTH` e `AGILITY` na faixa `[0.85F, 1.25F]` (padrão `1.00F` antes da inicialização ou para migração de saves legados), além da flag persistente `traitsInitialized`.
  - Implementada geração determinística única (`initializeTraitsOnce()`) no nascimento/spawn (`finalizeSpawn` e fallback server-side no primeiro `tick` caso criado diretamente em código), utilizando distribuição triangular (`0.85F + (r1 + r2) * 0.20F`).
  - Integrado o escalonamento individual aos valores base de cada estágio (`DragonStage`) em `applyStageAttributes()`:
    - `Vitalidade`: `MAX_HEALTH = stage.health * vitality`, `ARMOR = stage.armor * vitality`, `ARMOR_TOUGHNESS = stage.toughness * vitality`, `KNOCKBACK_RESISTANCE = min(1.0, stage.knockbackResistance * vitality)`.
    - `Força`: `ATTACK_DAMAGE = stage.melee * strength`, `getFireballDamage() = stage.fireball * strength`, `getBreathDamage() = stage.breath * strength`.
    - `Agilidade`: `MOVEMENT_SPEED = stage.groundSpeed * agility`, `FLYING_SPEED = stage.flightSpeed * agility`, além de taxa de curva angular (`maxTurn = 8F * agility`) e aceleração/inércia (`lerp = clamp(0.18 * agility, 0.10, 0.35)`) em [`DragonFlightControl.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonFlightControl.java).
    - `Inteligência`: `FOLLOW_RANGE = (stage == ADULT ? 48.0 : 32.0) * intelligence`, predição de mira (`lead = clamp(dist * 0.35 * intelligence, 0, 8)`), raio de percepção de ameaças (`12.0 * intelligence`) e limiar de recuo tático (`health <= 5.0F * intelligence`) em [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java).
  - Persistência NBT completa (`TraitsInitialized`, `TraitIntelligence`, `TraitVitality`, `TraitStrength`, `TraitAgility`) em `addAdditionalSaveData` e `readAdditionalSaveData`, com migração limpa para saves antigos (`1.00F` fixo quando a tag `TraitsInitialized` está ausente, preservando os valores exatos do mundo anterior sem re-sorteio) e restauração de `Health` pós-aplicação de atributos para evitar truncamento da vida de dragões `YOUNG`/`ADULT` ou de alta vitalidade ao carregar o mundo.
- **Arquivos Criados:** Nenhum.
- **Arquivos Modificados:**
  - [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java)
  - [`DragonEmber.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonEmber.java)
  - [`DragonFlightControl.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonFlightControl.java)
  - [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java)
  - [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java)
  - [`DragonEvolutionGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/DragonEvolutionGameTests.java)
- **Decisões Técnicas:**
  - Preservação da progressão `BABY < YOUNG < ADULT`: mesmo com variação máxima/mínima (`1.25F` vs `0.85F`), um `BABY` com `1.25F` (`37.5` HP, `5.0` melee) permanece abaixo de um `YOUNG` com `0.85F` (`51.0` HP, `5.95` melee), e um `YOUNG` com `1.25F` (`75.0` HP, `8.75` melee) permanece abaixo de um `ADULT` com `0.85F` (`102.0` HP, `10.2` melee).
  - Migração determinística (`1.00F`) para saves legados: ao detectar `DragonStage` ou `DragonXp` sem `TraitsInitialized`, o dragão recebe `1.00F` nos 4 atributos e `traitsInitialized = true`, mantendo 100% de compatibilidade com mundos existentes.
- **Testes Executados:**
  - `.\gradlew.bat test runBabyDragonTest --no-daemon`
  - Novos GameTests adicionados em `BabyDragonGameTests`:
    - `dragon_individual_traits_generation_and_persistence` (valida faixa `[0.85, 1.25]`, variação individual em amostra de dragões e imutabilidade após domesticação, evolução `BABY -> YOUNG -> ADULT` e reload NBT).
    - `dragon_individual_traits_legacy_save_migration` (valida migração de save antigo sem tags de traits para `1.00F` fixo e preservação de `Health` acima do limite base de `BABY`).
    - `dragon_individual_traits_behavioral_impact` (valida impacto real em `MAX_HEALTH`, `ATTACK_DAMAGE`, `FLYING_SPEED`, `FOLLOW_RANGE`, dano de `DragonEmber`, dano de sopro e percepção/recuo tático no `DragonBrain`).
- **Resultados:**
  - Compilação limpa e **87 de 87 GameTests aprovados** (`All 87 required tests passed :)` em 4.281s), além da suíte JUnit `:test`.
- **Problemas Conhecidos:** Nenhum.
- **Próxima Etapa:** Etapa 3 — Nomeação após domesticação.

### ETAPA 3 — Nomeação após domesticação
- **Status:** Testado
- **Implementação Realizada:**
  - Criados os payloads server-authoritative `DragonNamingPayloads.Prompt` (S2C) e `DragonNamingPayloads.Submit` (C2S) em [`DragonNamingPayloads.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonNamingPayloads.java), registrados em `RegisterPayloadHandlersEvent` (servidor/comum) e `RegisterClientPayloadHandlersEvent` em [`BabyDragonClient.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonClient.java) (cliente), garantindo isolamento total de classes de GUI no servidor dedicado.
  - Criada a tela [`DragonNameScreen.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonNameScreen.java) com campo `EditBox` (limite de 32 caracteres) e botões de **Confirmar** (ou tecla Enter) e **Pular** (mantendo o nome padrão do estágio).
  - Atualizado [`BabyDragon.mobInteract`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java) para disparar `DragonNamingPayloads.Prompt` ao domesticar com sucesso um dragão ainda sem nome customizado (`!this.hasCustomName()`).
  - Implementado [`BabyDragon.acceptOwnerName(Player sender, String rawName)`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java) com validação server-side estrita: propriedade (`isOwnedByEntity`), entidade viva e domesticada, distância (`<= 8` blocos), rejeição de caracteres de controle/formatação (`StringUtil.isAllowedChatCharacter`, bloqueando `\n`, `\0`, `§`), limite de comprimento (`MAX_CUSTOM_NAME_LENGTH = 32`) e suporte a pular (string vazia/em branco).
  - Adicionadas chaves de localização em [`en_us.json`](file:///c:/MinecraftDev/CodenameCoraline/src/main/resources/assets/codenamecoraline/lang/en_us.json) e [`pt_br.json`](file:///c:/MinecraftDev/CodenameCoraline/src/main/resources/assets/codenamecoraline/lang/pt_br.json).
- **Arquivos Criados:**
  - [`DragonNamingPayloads.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonNamingPayloads.java)
  - [`DragonNameScreen.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonNameScreen.java)
- **Arquivos Modificados:**
  - [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java)
  - [`BabyDragonClient.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonClient.java)
  - [`en_us.json`](file:///c:/MinecraftDev/CodenameCoraline/src/main/resources/assets/codenamecoraline/lang/en_us.json)
  - [`pt_br.json`](file:///c:/MinecraftDev/CodenameCoraline/src/main/resources/assets/codenamecoraline/lang/pt_br.json)
  - [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java)
- **Decisões Técnicas:**
  - Verificação `serverPlayer.connection.hasChannel(DragonNamingPayloads.Prompt.TYPE)` antes do envio S2C: evita exceções em conexões embarcadas de testes (`makeMockServerPlayerInLevel`) mantendo envio real para clientes conectados.
  - Validação caractere a caractere via `StringUtil.isAllowedChatCharacter` antes do `trim()`: impede injeção de códigos de formatação (`§`) ou caracteres de controle em pacotes forjados.
- **Testes Executados:**
  - `.\gradlew.bat test runBabyDragonTest --no-daemon`
  - Novos GameTests adicionados em `BabyDragonGameTests`:
    - `dragon_post_tame_naming_and_skip` (valida solicitação de nome após domesticação, opção de pular mantendo nome padrão, confirmação com trim, preservação na evolução `BABY -> YOUNG -> ADULT` e persistência NBT).
    - `dragon_naming_security_validation` (valida rejeição de dragão selvagem, jogador não-dono, dono fora de alcance, nome acima de 32 caracteres, caracteres de controle e `§`).
- **Resultados:**
  - Compilação limpa e **89 de 89 GameTests aprovados** (`All 89 required tests passed :)` em 4.244s), além da suíte JUnit `:test`.
- **Problemas Conhecidos:** Nenhum.
- **Próxima Etapa:** Etapa 4 — Variações visuais.

### ETAPA 4 — Variações visuais
- **Status:** Testado
- **Implementação Realizada:**
  - Adicionado o accessor `VARIANT` (`SynchedEntityData`, faixa `0..5`, `VARIANT_COUNT = 6`) em [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java), inicializado deterministicamente junto aos atributos individuais em `initializeTraitsOnce()` e persistido em NBT (`DragonVariant`), com fallback `0` para saves antigos (preservando a textura original dos dragões existentes).
  - Geradas 5 variações sutis adicionais (`1..5`) por estágio (`baby`, `young`, `adult`) via [`generate_dragon_variants.py`](file:///c:/MinecraftDev/CodenameCoraline/tools/generate_dragon_variants.py), respeitando o mapeamento UV exato de `DragonGeometry` e preservando `0` como a textura clássica original (`{stage}_dragon.png`):
    - `0` (`classic_crimson`): Carmesim clássico original.
    - `1` (`ruby_ember`): Tons de escama rubi mais profundos, barriga pergaminho-dourado, membranas acobreadas, base de chifres envelhecida e cicatriz sutil no focinho/ombro.
    - `2` (`volcanic_ash`): Escamas carmesim-defumadas, barriga creme-cinza, membranas com veios de fuligem, pontas de chifre obsidiana e marcas dorsais de cinza.
    - `3` (`sunlit_copper`): Escamas vermelhão-cobre aquecidas, barriga creme solar, membranas âmbar, chifres dourados e sardas/pintinhas sutis nas bochechas e cauda.
    - `4` (`ancient_garnet`): Escamas granada-vinho, barriga marfim envelhecido, ossos de asa escuros, chifres anelados e cicatrizes duplas no torso/cotovelo da asa.
    - `5` (`crowned_flame`): Escamas carmim vivo com bordas contrastadas, barriga ocre, membranas iluminadas, chifres marfim-claro e manchas de brasa na testa/crista/cauda.
  - Propagado `state.variant` em [`BabyDragonRenderState.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderState.java) e atualizado [`BabyDragonRenderer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderer.java) (`textureFor(DragonStage, int)` e `getTextureLocation`).
- **Arquivos Criados:**
  - [`generate_dragon_variants.py`](file:///c:/MinecraftDev/CodenameCoraline/tools/generate_dragon_variants.py)
  - `src/main/resources/assets/codenamecoraline/textures/entity/baby_dragon_1.png` .. `baby_dragon_5.png`
  - `src/main/resources/assets/codenamecoraline/textures/entity/young_dragon_1.png` .. `young_dragon_5.png`
  - `src/main/resources/assets/codenamecoraline/textures/entity/adult_dragon_1.png` .. `adult_dragon_5.png`
- **Arquivos Modificados:**
  - [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java)
  - [`BabyDragonRenderState.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderState.java)
  - [`BabyDragonRenderer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderer.java)
  - [`DragonModelGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/DragonModelGameTests.java)
  - [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java)
- **Decisões Técnicas:**
  - Manter a variante `0` apontando para `{stage}_dragon.png`: preserva 100% de compatibilidade retroativa com recursos existentes e garante que saves antigos sem a tag `DragonVariant` mantenham sua aparência original.
- **Testes Executados:**
  - `.\gradlew.bat test runBabyDragonTest --no-daemon`
  - `dragon_model_stage_assets` em `DragonModelGameTests` (valida empacotamento e unicidade SHA-256 de todas as 18 texturas base/variantes + 3 texturas glow).
  - `dragon_texture_variants_persistence` em `BabyDragonGameTests` (valida geração na faixa `0..5`, diversidade na população e persistência após domesticação, evolução `BABY -> YOUNG -> ADULT` e reload NBT).
- **Resultados:**
  - Compilação limpa e **90 de 90 GameTests aprovados** (`All 90 required tests passed :)` em 5.238s), além da suíte JUnit `:test`.
- **Problemas Conhecidos:** Nenhum.
- **Próxima Etapa:** Etapa 5 — Inteligência artificial, combate e comandos.

### ETAPA 5 — Inteligência artificial, combate e comandos
- **Status:** Testado
- **Implementação Realizada:**
  - Adicionado o enum `BabyDragon.Command` (`FOLLOW`, `STAY`, `PROTECT`, `RETURN`), a âncora persistente `stayAnchor`, a flag `returnRequested` e o método server-authoritative `issueCommand(Command, Player)` em [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java), além do suporte via comando `/dragon <dragon> command <follow|stay|protect|return>` em [`DragonCommands.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonCommands.java) e `clickMenuButton` em [`DragonMenu.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/inventory/DragonMenu.java).
  - Evoluído [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java) com a hierarquia estrita de 8 níveis de prioridade:
    1. Emergências da entidade (`isLiquidEmergency()`: lava, oxigênio baixo `< 60` ou imersão não intencional).
    2. Proteção e resgate do dono (`tryRescueFallingOwner`: dragão `ADULT` intercepta e monta automaticamente o dono em queda livre com `fallDistance >= 5.0F`, zerando `fallDistance`).
    3. Comandos explícitos (`RETURN` voa diretamente ao dono com disparo oportunista `engageInTransit` sem desviar da rota; `STAY` defende contra ameaças imediatas em até `10` blocos da `stayAnchor` e retorna à âncora ao cessar o perigo).
    4. Combate e recuo tático de baixa vida (removido o corte arbitrário `combatTicks > 240`; alvos são mantidos enquanto válidos, dentro do leash e com janela de perda de visão escalada por `Inteligência`).
    5. Caça autônoma (`tickHunting`).
    6. Seguir o dono.
    7. Interações sociais (`tickSocial`).
    8. Ociosidade / voo orbital ambiente.
- **ArquivosModificados:**
  - [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java)
  - [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java)
  - [`DragonCommands.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonCommands.java)
  - [`DragonMenu.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/inventory/DragonMenu.java)
  - [`en_us.json`](file:///c:/MinecraftDev/CodenameCoraline/src/main/resources/assets/codenamecoraline/lang/en_us.json), [`pt_br.json`](file:///c:/MinecraftDev/CodenameCoraline/src/main/resources/assets/codenamecoraline/lang/pt_br.json)
  - [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java)
- **Testes Executados:**
  - `dragon_combat_retains_target_beyond_240_ticks`, `dragon_stay_self_defense_and_return_to_anchor`, `dragon_commands_return_protect_follow_and_rescue` (**93/93 GameTests aprovados**).

### ETAPA 6 — Física de voo
- **Status:** Testado
- **Implementação Realizada:**
  - Adicionados `flightMomentum`, `flightRoll`, `flightPitch`, `glideFactor` e `updateFlightAttitude(float yawDelta, float targetPitch)` em [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java).
  - Evoluídos o voo autônomo em [`DragonFlightControl.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonFlightControl.java) e o voo montado em [`BabyDragon.travelMounted`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java) com ganho de momento em mergulho, conversão de velocidade em sustentação/subida, inércia proporcional ao momento e escalonamento por `Agilidade`.
  - Propagados `flightRoll`, `flightPitch` e `glideFactor` para [`BabyDragonRenderState.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderState.java), [`BabyDragonRenderer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderer.java) e [`DragonModel.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonModel.java) (inclinação lateral *banking*, arfagem *pitch* e redução de batimento de asas durante planeio/mergulho).
- **Testes Executados:**
  - `dragon_flight_physics_dive_climb_banking` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java) + regressão `dragon_evolution_riding_flight`.

### ETAPA 7 — Combate avançado
- **Status:** Testado
- **Implementação Realizada:**
  - Implementado `evaluateThreatPriority(@Nullable LivingEntity owner, LivingEntity candidate)` em [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java) priorizando agressores diretos do dono, ameaças próximas ao dono, alvos do dono, agressores do próprio dragão e inimigos com maior poder ofensivo.
  - Aprimorado `selectCombatPosition(LivingEntity target)` com espaçamento tático por estágio (incluindo recuo evasivo para `BABY` quando o inimigo fecha distância `< 4.0` blocos), validação de linha de tiro (`hasClearSightFrom`) e prevenção de posicionamento sobre lava/fogo/magma (`!isHazardousBelow`).
- **Testes Executados:**
  - `dragon_advanced_combat_threat_and_positioning` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java) (**95/95 GameTests aprovados**).

### ETAPA 8 — Equipamentos e mochila
- **Status:** Testado
- **Implementação Realizada:**
  - Adicionado `SimpleContainer equipment = new SimpleContainer(2)` (`EQUIP_SLOT_SADDLE = 0`, `EQUIP_SLOT_TOTEM = 1`) e sincronização via `SynchedEntityData` (`SADDLED`, `HAS_TOTEM`) em [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java), separado do `inventory` de 18 slots (`"DragonInventory"`) para garantir zero deslocamento de índices ou perda de itens em mundos existentes.
  - Preservada integralmente a regra de que **montar o dragão adulto NÃO exige sela** (`mountOwner` e `canAddPassenger` permanecem independentes de `isSaddled()`).
  - Equipar uma sela (`Items.SADDLE`) libera +9 slots de carga da mochila (`getActiveCargoSlots()`: `BABY` passa de `0 -> 9`, `YOUNG` de `9 -> 18`, `ADULT` mantém `18`) e desbloqueia o slot dedicado ao Totem da Imortalidade (`MENU_SLOT_TOTEM = 55`).
  - Bloqueada a remoção da sela (`canUnequipSaddle()`) enquanto o slot de Totem ou os slots bônus da mochila contiverem itens.
  - Adicionada camada de renderização 3D [`DragonGearLayer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonGearLayer.java) acoplada ao modelo nativo sem alterar a contagem de cubos ou o atlas UV de [`DragonGeometry.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonGeometry.java).
- **Arquivos Criados/Modificados:**
  - [`DragonGearLayer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonGearLayer.java)
  - [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java), [`DragonMenu.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/inventory/DragonMenu.java), [`DragonScreen.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonScreen.java), [`BabyDragonRenderState.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderState.java), [`BabyDragonRenderer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/BabyDragonRenderer.java)
- **Testes Executados:**
  - `dragon_saddle_backpack_and_legacy_inventory` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java) + regressão de `dragon_evolution_inventory_access` e `dragon_evolution_inventory_transfer`.

### ETAPA 9 — Totem da Imortalidade
- **Status:** Testado
- **Implementação Realizada:**
  - Integrada a ativação do Totem equipado na sela do dragão ao fluxo autêntico de dano fatal em [`DragonEvents.onLivingDamagePre`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonEvents.java) (`EventPriority.LOWEST`) e [`BabyDragon.tryActivateTotemForOwner`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java).
  - Respeita `DamageTypeTags.BYPASSES_INVULNERABILITY`, prioriza Totem já segurado nas mãos do próprio jogador, verifica distância máxima de `32` blocos na mesma dimensão, valida `CommonHooks.onLivingUseTotem`, consome atomicamente 1 unidade do slot `EQUIP_SLOT_TOTEM`, registra estatísticas/`CriteriaTriggers.USED_TOTEM`, aplica `DeathProtection.applyEffects` (`Regeneration II`, `Absorption II`, `Fire Resistance I`), emite o evento `(byte) 35` e zera o dano fatal sem permitir que `die()` seja invocado.
- **Testes Executados:**
  - `dragon_totem_of_undying_owner_protection` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java).

### ETAPA 10 — Caça terrestre
- **Status:** Testado
- **Implementação Realizada:**
  - Implementado o ciclo completo de caça terrestre em [`DragonBrain.tickHunting`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java): seleção de presas terrestres válidas (`Chicken`, `Rabbit`, `Pig`, `Sheep`, `Cow` selvagens, adultas, sem nome, desprotegidas e com `maxHealth <= 20`), perseguição e abate térmico, coleta restrita ao drop recém-gerado pela presa abatida (`tryCollectPreyDrop`), transporte visual na mandíbula (`CARRIED_ITEM`, [`DragonModel.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonModel.java) e [`DragonGearLayer.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/client/babydragon/DragonGearLayer.java)), autocura se o dragão estiver ferido e entrega ao inventário/pés do dono quando com vida cheia.
- **Testes Executados:**
  - `dragon_land_hunting_delivery_and_self_heal` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java).

### ETAPA 11 — Caça aquática
- **Status:** Testado
- **Implementação Realizada:**
  - Implementada a caça aquática controlada em [`DragonBrain.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java) (`isValidAquaticPrey`, `isIntentionallyInWater`, `waterHunting`, `resurfacingFromHunt`) e [`BabyDragon.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/BabyDragon.java) (supressão condicional de `FloatGoal` durante mergulho intencional): o dragão identifica peixes (`Cod`, `Salmon`, `TropicalFish`) na água quando possui fôlego seguro (`airSupply >= 120`), mergulha até o peixe, captura o drop na mandíbula, retorna à superfície (`findWaterSurfaceAbove`) e se alimenta ou entrega o peixe ao dono, mantendo lava e falta real de oxigênio (`airSupply < 60`) como emergências imediatas de Prioridade 1.
- **Testes Executados:**
  - `dragon_aquatic_hunting_dive_and_resurface` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java).

### ETAPA 12 — Interações sociais
- **Status:** Testado
- **Implementação Realizada:**
  - Implementado [`DragonBrain.tickSocial`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/entity/babydragon/DragonBrain.java) (Prioridade 7): dois dragões domesticados do mesmo dono, em solo, seguros e ociosos próximos ao dono iniciam interação social sincronizada (`socialPartner`, `socialTicks = 60`), olhando um para o outro, aproximando-se, executando `happy()` (`Pose.HAPPY`) e pequenos saltos laterais sincronizados, com interrupção imediata (`clearSocialState()`) no exato tick em que qualquer ameaça, dano, caça ou comando (`STAY`, `RETURN`) ocorrer.
- **Testes Executados:**
  - `dragon_social_interaction_and_interruption` em [`BabyDragonGameTests.java`](file:///c:/MinecraftDev/CodenameCoraline/src/main/java/com/codenamecoraline/verification/BabyDragonGameTests.java).
- **Resultado Global da Suíte (`.\gradlew.bat test runBabyDragonTest --no-daemon`):**
  - Compilação limpa, suíte JUnit `:test` aprovada e **100 de 100 GameTests aprovados** (`All 100 required tests passed :)`).

### FUNCIONALIDADE FUTURA — Ressurreição
- **Status:** Pendente (Futuro — Não implementar)
- **Nota:** Registrada apenas como direção futura. Nenhuma classe ou `SavedData` (`DragonBondSavedData`) será criada nesta fase.
