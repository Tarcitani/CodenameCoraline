# Arquitetura

Estado real do código em `src/`. Atualize este arquivo junto com qualquer mudança de estrutura.

## Estrutura de diretórios

```
CodenameCoraline/
├── build.gradle            # plugins, toolchain Java 25, run configs NeoForge, geração de metadados
├── settings.gradle         # plugin foojay (provisiona JDK) e repositórios do plugin
├── gradle.properties       # versões (Minecraft, NeoForge) e propriedades do mod
├── gradlew / gradlew.bat   # Gradle Wrapper
├── gradle/wrapper/         # distribuição e propriedades do wrapper
├── src/main/java/com/codenamecoraline/
│   ├── CodenameCoraline.java           # ponto de entrada
│   ├── registry/ModEntities.java       # DeferredRegister de entidades e atributos
│   ├── registry/ModSounds.java         # DeferredRegister de eventos de som
│   ├── registry/ModItems.java          # ovo, pedrinha, unha, dente, adaga, pique, touca
│   ├── registry/ModCreativeTabs.java   # aba do criativo só com os ovos
│   ├── entity/redcap/Redcap.java       # entidade Redcap (variante, atributos, IA, ataques, sons, idle no cliente)
│   ├── entity/redcap/RedcapVariant.java # lança, adaga, garras: números de combate
│   ├── entity/redcap/RedcapAge.java    # jovem, adulto, velho: só a cor
│   ├── entity/redcap/RedcapRock.java   # pedrinha arremessada pelas garras
│   ├── entity/redcap/RockAim.java      # mira balística com antecipação do alvo
│   └── client/redcap/                  # apenas cliente: modelo, animações, render state, renderizador, olhos, registro
├── src/main/resources/assets/codenamecoraline/
│   ├── items/, models/item/, textures/item/ # ovo de geração e pedrinha animada
│   ├── sounds.json                     # áudios de cada evento de som (referências a áudios do jogo base)
│   ├── lang/en_us.json, pt_br.json     # nomes de entidades e legendas de som
│   └── textures/entity/                # redcap.png, redcap_eyes.png (ver docs/DEVELOPMENT.md)
├── src/main/templates/META-INF/
│   └── neoforge.mods.toml      # template de metadados (expandido pelo Gradle)
└── docs/                   # esta documentação
```

Pacotes existem somente quando há conteúdo real: `registry`, `entity` e `client` foram criados para o Redcap (ver `docs/design/redcap.md`).

## Ponto de entrada

- `com.codenamecoraline.CodenameCoraline`, anotada com `@Mod(CodenameCoraline.MOD_ID)`.
- Construtor recebe o `IEventBus` do mod e delega o registro a `ModSounds.register(...)` e `ModEntities.register(...)`.
- `MOD_ID = "codenamecoraline"` deve ser igual a `mod_id` em `gradle.properties` e ao `modId` em `neoforge.mods.toml`.

## Fluxo de inicialização

1. O NeoForge lê `META-INF/neoforge.mods.toml` no jar (gerado a partir de `src/main/templates`).
2. O NeoForge instancia a classe anotada com `@Mod`, passando o `IEventBus` do mod.
3. `ModEntities.register` registra o `DeferredRegister` no barramento e ouve `EntityAttributeCreationEvent` para os atributos do Redcap.
4. No cliente, `client/redcap/RedcapClient` (anotado com `@EventBusSubscriber(value = Dist.CLIENT)`) registra a definição de camada do modelo e o renderizador.

## Mecanismos de registro

- Entidades: `DeferredRegister.Entities` em `registry/ModEntities.java`, criado com o `MOD_ID`.
- Atributos de entidade: `EntityAttributeCreationEvent`, no barramento do mod.
- Sons: `DeferredRegister<SoundEvent>` em `registry/ModSounds.java`; os áudios de cada evento ficam em `assets/codenamecoraline/sounds.json` e as legendas nos arquivos `lang`.
- Modelo e renderização: somente no cliente, em `client/redcap`. `RedcapModel` estende `EntityModel<RedcapRenderState>` (não `HumanoidModel`); `RedcapRenderer` é um `MobRenderer`.

## Animação do Redcap

- **Base:** a cada quadro o modelo volta à pose definida em `createBodyLayer` (inclinações fixas ficam em partes filhas). Nomes de partes são únicos, com prefixo de lado (`right_forearm`, `left_boot`...), porque as animações por keyframe buscam ossos pelo nome.
- **Procedural** (`RedcapModel.setupAnim`): olhar, caminhada (passos curtos, joelhos altos, corpo balançando e curvando mais ao correr, braço livre solto, pique como cajado), respiração, garras flexionando, orelhas tremendo e ponta do gorro com movimento secundário.
- **Keyframes** (`RedcapAnimations`, sistema `AnimationDefinition`/`KeyframeAnimation` do jogo, somado ao procedural): estocada com o pique e quatro variações de idle (farejar, olhar em volta, afiar a lâmina, conferir o gorro).
- **Estados:** `Redcap` guarda `AnimationState`s, copiados para `RedcapRenderState` em `RedcapRenderer.extractRenderState`. A estocada começa no servidor, que envia o evento de entidade `EVENT_PIKE_THRUST` (4); as variações de idle são sorteadas só no cliente, quando o Redcap está parado e sem alvo.
- **Variantes:** `RedcapVariant` (`SPEAR`, `DAGGER`, `CLAWS`) é um dado sincronizado da entidade (`DATA_VARIANT`) e salvo em NBT (`Variant`). Define dano, tempo de antecipação, intervalo e alcances. Sorteada em `finalizeSpawn`, ou fixada pelo ovo (o componente `ENTITY_DATA` do item carrega `Variant`). O modelo mostra só a arma da variante (`pike`, `dagger`; `rock` só durante o arremesso).
- **Ataque:** `Redcap.WeaponAttackGoal` (um `MeleeAttackGoal` com `followingTargetEvenIfNotSeen = true`) inicia o golpe, envia o evento de entidade (101 melee, 102 garra esquerda, 103 arremesso) e aplica o dano `windupTicks` depois, no quadro do golpe, se o alvo estiver a até `strikeReach` da hitbox. Uma mão por golpe: lança e adaga usam o braço esquerdo, as garras alternam os braços.
- **Pedrinha:** `Redcap.RockThrowGoal` (só `CLAWS`, alvo entre 5 e 16 blocos com linha de visão) para, faz a animação e lança `RedcapRock` em `ROCK_RELEASE_TICKS`. A mira (`RockAim`) simula o movimento do projétil (gravidade, inércia 0,99, deslocamento) para achar o ângulo que passa pelo centro do alvo e adianta o alvo pelo tempo de voo. A pedrinha é o item `redcap_pebble` com textura animada (8 quadros girando), desenhada pela `ThrownItemRenderer` na metade do tamanho; causa 8 de dano, repulsão e Lentidão II por 2 s e atravessa outros Redcaps (sem fogo amigo).
- **Piscar:** `RedcapEyesLayer` deixa de desenhar os olhos brilhantes por 3 ticks a cada 110.
- Itens e abas criativas são registrados em `ModItems` e `ModCreativeTabs`; o livro usa `Bestiary.ENTRIES`.

## Metadados

`src/main/templates/META-INF/neoforge.mods.toml` usa placeholders `${...}` preenchidos pela tarefa `generateModMetadata` a partir de `gradle.properties`. O resultado vai para `build/generated/sources/modMetadata`, que entra em `sourceSets.main.resources`. **Não edite o arquivo gerado em `build/`.**

Dependências declaradas: `neoforge` (faixa a partir de `neo_version`) e `minecraft` (faixa `minecraft_version_range`).

## Dependências importantes

| Dependência | Função | Origem |
|---|---|---|
| NeoForge 26.1.2.124 | Loader e APIs | `neoForge { version = neo_version }` |
| Minecraft 26.1.2 | Jogo | Declarado pelo plugin ModDevGradle |
| `net.neoforged.moddev` 2.0.148 | Ambiente de desenvolvimento e run configs | `build.gradle` |
| `foojay-resolver-convention` 1.0.0 | Provisiona JDK 25 se ausente | `settings.gradle` |
| Log (SLF4J via `com.mojang.logging`) | Logs | Fornecido pelo Minecraft |

Nenhuma biblioteca adicional foi incluída.

## Regras que futuras alterações precisam preservar

- `MOD_ID`, `mod_id` e `modId` devem permanecer idênticos.
- Não editar `build/generated/**`; alterações vão em `src/main/templates` ou `gradle.properties`.
- Versões de Minecraft e NeoForge mudam juntas, sempre verificadas (ver `docs/DECISIONS.md`).
- Java 25 é exigido pelo Minecraft 26.1.2; não reduza a toolchain sem nova decisão.
- Não adicionar lógica de gameplay em classes de renderização ou no ponto de entrada além de carregamento e registro.
- Não criar subprojetos Gradle sem justificativa registrada em `docs/DECISIONS.md`.

## Comportamento em grupo

- **Alerta:** quando um Redcap escolhe um alvo, ri e todos os Redcaps num raio de 24 blocos sem alvo assumem o mesmo, em silêncio (`Redcap.setTarget`). Ferir um deles também alerta os outros (`HurtByTargetGoal`).
- **Cerco** (`FlankGoal`, lança e adaga): com outros meio-corpo-a-corpo no mesmo alvo, cada um vai para um ponto de um círculo de 3,5 blocos em volta do alvo, espaçados igualmente (dois fazem uma pinça), e espera até 25 ticks pelos outros antes de avançar juntos.
- **Apoio** (`StayBackGoal`, garras): com lança/adaga no mesmo alvo, mantém a distância de arremesso (recua se perto de 7 blocos, aproxima se longe de 13) e joga pedrinhas em vez de correr para o corpo a corpo. Sozinho, ataca de perto como antes.

## Idade (cor)

`RedcapAge` (`YOUNG`, `ADULT`, `OLD`) é um dado sincronizado separado da variante, sorteado em `finalizeSpawn` (metade adultos, um quarto de cada outro) e salvo em NBT (`RedcapAge`). Só escolhe a textura (`redcap_young.png`, `redcap.png`, `redcap_old.png`, mesma geometria e mesmos UVs): o jovem é um pouco mais verde, liso e de barba escura; o velho, mais cinza, com mais rugas e barba esbranquiçada. Não altera atributos. `/summon codenamecoraline:redcap {RedcapAge:"old"}` fixa a idade.

## Drops

Só quando um jogador mata. Quanto mais útil o item, menor a chance (`Redcap.dropCustomDeathLoot`):

| Item | Chance | Para quê |
|---|---|---|
| Unha de Barrete Vermelho (1 a 2) | 55% | ingrediente de poção (ainda não planejado) |
| Dente de Barrete Vermelho (1 a 2) | 30% | ingrediente de poção (ainda não planejado) |
| Adaga enferrujada | 14% (só variante adaga) | arma rápida |
| Pique enferrujado | 10% (só variante lança) | lança do jogo, com alcance maior |
| Touca de Barrete Vermelho | 3% | usada na cabeça: os Redcaps veem o portador como um deles |

As armas saem com desgaste aleatório uniforme, de 1 ponto até um ponto antes de quebrar. A variante de garras não tem arma. A tabela de saque do tipo (`data/codenamecoraline/loot_table/entities/redcap.json`) está vazia de propósito: os drops são feitos no código para sortear o desgaste.

## A touca

`ModItems.REDCAP_CAP` é um item de cabeça (`Equippable` HEAD **sem** asset de armadura): sem asset, o jogo desenha o modelo do próprio item na cabeça (contexto de exibição `head`, escolhido em `items/redcap_cap.json`). O modelo `models/item/redcap_cap_worn.json` é um capuz 3D de caixas alinhadas aos eixos: calota em degraus, abas laterais, nuca e uma ponta que cai para trás. Sem defesa nem durabilidade. Com ela na cabeça (`Redcap.wearsCap`):
- **Amigo:** o Redcap não escolhe o jogador como alvo (`isFriendTo`) e abandona o alvo se ele colocar a touca.
- **Ajuda, como um lobo:** `HelpWearerGoal` ataca o que feriu o portador ou o que ele feriu nos últimos 5 s (nunca outro Redcap nem outro portador).
- **Traição:** o Redcap lembra por 20 s do portador que tomou por amigo. Se esse jogador aparecer à vista sem a touca, ele e o bando ao redor ficam enfurecidos por 30 s: atacam todos (touca ou não) e ganham Velocidade I. Ferir um Redcap usando a touca também enfurece. Em criativo ou espectador não há traição.

## Bestiário

Item `codenamecoraline:bestiary` (`item/BestiaryItem`): usar abre `client/bestiary/BestiaryScreen`, um livro de duas páginas. A primeira abertura é o índice (um botão por mob); cada capítulo começa numa página esquerda. O texto é quebrado na largura da página e continua em páginas extras quando é longo (`(continuação)`).

- **Dados:** `bestiary/Bestiary.ENTRIES` lista os capítulos (`BestiaryEntry`: id e páginas); cada `BestiaryPage` é só texto ou tem um desenho. Todos os textos são chaves de tradução `bestiary.codenamecoraline.<mob>.<página>.title|caption|body` (`.name` do capítulo) nos arquivos `lang`.
- **Arte:** fundo `textures/gui/bestiary_book.png` (300x190) e desenhos em `textures/gui/bestiary/*.png`.
- **Obtenção:** aba Ferramentas do criativo e receita sem forma: livro + corante vermelho + pena (`data/codenamecoraline/recipe/bestiary.json`).

## Basilisco

- **Servidor:** `entity/basilisk/Basilisk` contém atributos, imunidades, fraquezas, geração, drops de perna/olho/rabo, memória do agressor e objetivos de combate, repulsão e espreita. A navegação anfíbia escolhe os caminhos; `HopMoveControl` executa os saltos.
- **Névoa:** `WitherMistCloud` estende `AreaEffectCloud`, verifica distância e oclusão e aplica Decomposição III vanilla. Eleva instâncias mais fracas para III e não renova instâncias III ou superiores, preservando o relógio de dano e evitando acúmulo por sobreposição. O ID antigo da entidade é preservado para carregar nuvens salvas.
- **Efeito e poção:** `registry/ModEffects` registra o efeito `wither_resistance`; `registry/ModPotions` registra os tipos de poção e as misturas sequenciais no suporte vanilla, com dois intermediários e versões finais de três/oito minutos. Ambos usam `DeferredRegister` no ponto de entrada. `WitherResistanceEffect` remove Decomposição existente e o evento `MobEffectEvent.Applicable` impede novas aplicações durante a proteção. As misturas usam `RegisterBrewingRecipesEvent` e `PotionBrewing.Builder.addMix`. O registro nativo também alimenta nomes, conversões arremessável/persistente e abas do criativo, sem inclusão manual duplicada.
- **Cliente:** `client/basilisk/` contém modelo, renderizador, estado de renderização e olhos emissivos. O estado e a inflação do papo são sincronizados; não há HUD ou attachments de praga.
- **Compatibilidade:** itens antigos permanecem registrados, mas sem drops, receitas ou entradas no criativo. `CatacombLilyItem` conserva o uso de exemplares existentes em golens; `MistFlask` passa a criar a nova névoa.
- **Recursos:** modificador de spawn da tag `minecraft:is_overworld` (peso 100, grupos 4–8), com filtro de habitat na entidade: pântanos em qualquer luz ou cavernas escuras sob o terreno. Misturas nativas e ícones de perna, olho, rabo e efeito. Os drops usam código e tabela de saque vazia. O capítulo do Bestiário está em `Bestiary.BASILISK` e nos dois arquivos de idioma, com três desenhos em `textures/gui/bestiary/basilisk_*.png`. `tools/ExportBasiliskModel.java` exporta as poses reais; o script Python de pranchas gera os PNGs em sépia. Os textos longos continuam pela paginação existente.
- **Verificação:** `verification/BasiliskGameTests` registra testes apenas com `coraline.basiliskTests=true`. A execução Gradle `basiliskTest` usa `build/basilisk-gametest/`.

Valores e receitas: [design/basilisk.md](design/basilisk.md).

## Litófago (Lithophage)

- **Servidor:** `entity/lithophage/Lithophage` implementa atributos (36 HP, 6 de armadura, velocidade 0,22, resistência a empurrão 0,55, alcance de detecção 12 e limite territorial de perseguição de 20 blocos a partir de `homePos`), regras de geração subterrânea (`checkSpawnRules`) e máquina de 7 estados (`DORMANT`, `AWAKENING`, `VIGILANT`, `PURSUING`, `COMBAT`, `RECOVERY`, `DYING`) com 3 ataques (`SLAM`, `SWEEP`, `SHARD`).
- **Projétil:** `entity/lithophage/AmberShard` estende `ThrowableItemProjectile`, causa 4 HP de dano com alcance máximo de 15 blocos (40 ticks), colide com blocos e entidades e ignora outros Litófagos.
- **Cliente:** `client/lithophage/` contém `LithophageModel`, `LithophageAnimations` (12 animações por keyframe somadas ao controle procedural), `LithophageRenderState`, `LithophageEyesLayer` (brilho âmbar nas fendas da máscara e cristais quando desperto) e `LithophageRenderer`.
- **Recursos e Bestiário:** modificadores de bioma `lithophage_lush_spawns.json` (Lush Caves, peso 28, grupos 1–3) e `lithophage_dripstone_spawns.json` (Dripstone Caves, peso 16, grupos 1–2); tabela de saque `loot_table/entities/lithophage.json` (`ancient_clay_fragment` e `mineral_amber_core`); ovo `lithophage_spawn_egg`; capítulo `Bestiary.LITHOPHAGE` com três pranchas em `textures/gui/bestiary/lithophage_*.png`.
- **Verificação:** `verification/LithophageGameTests` registra testes na execução `runLithophageTest` (`coraline.lithophageTests=true`) em `build/lithophage-gametest/`. Detalhes em [design/lithophage.md](design/lithophage.md).

## Cobrador da Última Dádiva (`last_gift_collector`)

- **Servidor:** `entity/collector/LastGiftCollector` implementa o boss colossal (~4,5 blocos de altura, 128 HP, 8 de armadura, velocidade 0,23 na Fase I e 0,27 na Fase II, resistência a repulsão 0,85, `ServerBossEvent`), a cinemática de abertura em 7 subestados (9,5 s / 190 ticks), a máquina de estados (`INTRO_CINEMATIC`, `PHASE_ONE`, `PHASE_TRANSITION`, `PHASE_TWO`, `ATTACK_PREPARATION`, `ATTACK_EXECUTION`, `RECOVERY`, `DEATH_CINEMATIC`, `DEAD`) e os 4 ataques coreografados (`WINTER_SCYTHE`, `HOLLOW_BELL`, `PAWN_PROCESSION`, `SOLSTICE_JUMP`).
- **Trenó Cinematográfico:** `entity/collector/CollectorSleigh` é o ator visual temporário que cruza o céu durante a introdução (`shouldBeSaved() = false`, sem IA de combate e descartado automaticamente ao fim da introdução).
- **Item de Invocação:** `item/SilentBellItem` (`codenamecoraline:silent_bell`) valida cinco condições (bloco `packed_ice`, bioma `snowy_taiga` ou `grove`, noite, acesso livre ao céu/clareira e ausência de outro Cobrador ativo em 64 blocos) sem consumir o item em caso de falha.
- **Cliente:** `client/collector/` contém `LastGiftCollectorModel`, `LastGiftCollectorAnimations` (18 animações por keyframe), `LastGiftCollectorRenderState`, `LastGiftCollectorEyesLayer`, `LastGiftCollectorRenderer`, `CollectorSleighModel`, `CollectorSleighRenderState`, `CollectorSleighRenderer`, `CollectorCinematicDirector` (enquadramento de câmera, FOV, letterbox, título e tecla configurável `key.codenamecoraline.skip_cinematic`) e `LastGiftCollectorClient`.
- **Recursos, Conquista e Bestiário:** receita `recipe/silent_bell.json`, tabela de saque `loot_table/entities/last_gift_collector.json` (`cracked_bell`, `frostbound_thread`, `solstice_seal`), conquista `advancement/bosses/the_final_delivery.json` (*A Última Entrega* / *The Final Delivery*, 100 XP) e capítulo `Bestiary.LAST_GIFT_COLLECTOR` com três pranchas geradas a partir do modelo 3D real (`last_gift_collector_anatomy.png`, `last_gift_collector_combat.png`, `last_gift_collector_spoils.png`).
- **Verificação:** `verification/LastGiftCollectorGameTests` registra testes na execução `runLastGiftCollectorTest` (`coraline.collectorTests=true`) em `build/collector-gametest/`. Detalhes em [design/last_gift_collector.md](design/last_gift_collector.md).

## Barras de vida de bosses (`client/bossbar`)

Regra do projeto (ver `CLAUDE.md`): cada boss desenha a própria barra. `ThemedBossBars` cancela a barra padrão (`CustomizeGuiOverlayEvent.BossEventProgress`) para bosses com estilo registrado e delega a um `Style`. Nicolas Noel, o Renegado usa `LastGiftCollectorBossBar`: moldura 288×44 de galhadas palmadas, trilha com pelo e pingentes de gelo, machados cruzados e o rosto dele (gorro, olhos âmbar, nariz longo, barba enorme), com o slot de vida transparente (fundo e preenchimento são desenhados antes da moldura); o nome é dividido em dois e fica sob a trilha, de cada lado da barba (`tools/generate_boss_bar_last_gift_collector.py`); o preenchimento muda de cor por estágio (vermelho, vinho, gelo, ouro e, na Fúria, fogo pulsante) e a trilha marca onde cada estágio começa.
