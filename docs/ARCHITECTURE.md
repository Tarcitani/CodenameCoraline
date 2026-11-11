# Arquitetura

Estado real do código em `src/`. Atualize este arquivo junto com qualquer mudança de estrutura.

`verification/ModelGameTestRegistration` mantém as suítes de geometria do Quebra-Copas e Carmesim fora do carregamento normal do servidor dedicado. Somente as propriedades opt-in dessas suítes carregam as classes que referenciam renderização; isso evita que a reflexão do barramento resolva classes gráficas ausentes no servidor.

## Estrutura de diretórios

```
CodenameCoraline/
├── build.gradle            # plugins, toolchain Java 25, run configs NeoForge, GameTests, geração de metadados
├── settings.gradle         # plugin foojay (provisiona JDK) e repositórios do plugin
├── gradle.properties       # versões (Minecraft, NeoForge) e propriedades do mod
├── gradlew / gradlew.bat   # Gradle Wrapper
├── gradle/wrapper/         # distribuição e propriedades do wrapper
├── tools/                  # scripts Python de geração de texturas, UVs, poses e boss bars
├── docs/                   # arquitetura, decisões (ADRs), roadmap, estado atual e design por criatura
│   ├── CURRENT_STATE.md    # resumo de 1 página para inicialização rápida de contexto
│   ├── ARCHITECTURE.md     # estrutura de pacotes, registros e fluxo de inicialização
│   ├── DECISIONS.md        # decisões arquiteturais (D-001 a D-037)
│   ├── DEVELOPMENT.md      # comandos de build, GameTests e solução de problemas
│   ├── ROADMAP.md          # histórico técnico detalhado de entregas
│   ├── PROMPT_NOVO_MOB.md  # prompt mestre para criação de mobs/bosses
│   └── design/             # especificações detalhadas e validações de cada mob/boss
├── src/main/java/com/codenamecoraline/
│   ├── CodenameCoraline.java   # ponto de entrada (@Mod) e orquestrador de eventos
│   ├── registry/               # registros centralizados via DeferredRegister
│   │   ├── ModEntities.java    # registro de tipos de entidade e atributos
│   │   ├── ModItems.java       # registro de itens, armas, armaduras e ovos
│   │   ├── ModSounds.java      # registro de SoundEvents
│   │   ├── ModCreativeTabs.java# aba criativa do mod
│   │   ├── ModEffects.java     # efeitos de status do mod
│   │   ├── ModPotions.java     # poções vanilla customizadas
│   │   └── ModDragonSystems.java# sistemas e atributos estendidos do dragão
│   ├── entity/                 # lógica de gameplay, IA, atributos e estados (SERVIDOR)
│   │   ├── redcap/             # Redcap (variantes de armas, idade, mira de pedras)
│   │   ├── basilisk/           # Basilisco da Praga (sopro, cegueira, decomposição)
│   │   ├── lithophage/         # Litófago (7 estados de IA, estilhaço de âmbar)
│   │   ├── collector/          # Cobrador da Última Dádiva (boss, trenó, rituais)
│   │   ├── canopy/             # Quebra-Copas (boss arbóreo, golpes de gorila, salto)
│   │   ├── macaque/            # Micos de Casca (lacaios arbóreos do Quebra-Copas)
│   │   ├── crimson/            # Comandante Carmesim (boss duelo, combos, guarda/riposta)
│   │   ├── ten_threads_witch/  # Bruxa dos Dez Fios (Matriarca + 10 marionetes, AttackRunner)
│   │   ├── babydragon/         # Dragão Companheiro (3 anatomias, montaria, inventário, totem)
│   │   ├── raven/              # Corvo Feérico (companheiro místico, sentinela, coleta, ombro)
│   │   ├── salamander/         # Salamandra de Enxofre (predador de magma, cuspe balístico e alcatrão)
│   │   ├── architect/          # O Arquiteto (boss de seis braços: estátua, ataques A–F, aparo, postura)
│   │   ├── spirit/             # Espíritos residuais de chefes derrotados (BossSpirit)
│   │   └── PlayerSummon.java   # interface comum para invocações aliadas do jogador
│   ├── client/                 # modelos, renderizadores, animações e telas (CLIENTE)
│   │   ├── bossbar/            # ThemedBossBars e estilos temáticos de barra de vida
│   │   ├── bestiary/           # telas, layouts de página e renderizadores do Bestiário
│   │   ├── redcap/, basilisk/, lithophage/, collector/, canopy/,
│   │   ├── macaque/, crimson/, ten_threads_witch/, babydragon/, raven/, salamander/, architect/ # modelos e animações
│   │   └── ...                 # camadas emissivas (EyesLayer) e EntityRenderState
│   ├── bestiary/               # modelo de dados do Bestiário (Bestiary, BestiaryEntry, BestiaryPage)
│   ├── inventory/              # menus de container do jogador (DragonMenu)
│   ├── item/                   # classes de itens especiais (DeadrootClubItem, TenThreadsStaffItem, etc.)
│   ├── util/                   # utilitários matemáticos, físicos e de rede
│   └── verification/           # suítes de testes de integração automatizados (GameTests)
│       ├── BabyDragonGameTests.java, BasiliskGameTests.java, BrimstoneSalamanderGameTests.java,
│       ├── CanopyBreakerGameTests.java, CrimsonCommanderGameTests.java, DeadrootClubGameTests.java,
│       ├── DragonEvolutionGameTests.java, DragonModelGameTests.java, FeyRavenGameTests.java,
│       ├── LastGiftCollectorGameTests.java, LithophageGameTests.java, MatriarchAnimationGameTests.java,
│       ├── TenThreadsWitchGameTests.java,
│       └── ModelGameTestRegistration.java # controle de carregamento dedicado de testes de modelo
├── src/main/resources/
│   ├── assets/codenamecoraline/# recursos visuais e sonoros do cliente
│   │   ├── textures/           # atlas de entidades, itens, partículas e gui/boss_bar/
│   │   ├── models/ & items/    # definições de modelos de blocos e itens 1.21.x
│   │   ├── lang/               # localizações completas (pt_br.json e en_us.json)
│   │   └── sounds.json         # mapeamento de áudios vanilla e pitches
│   └── data/codenamecoraline/  # dados de servidor e mecânicas vanilla
│       ├── advancement/        # árvores de conquistas temáticas por boss/mob
│       ├── loot_table/         # tabelas de saque de entidades e rituais
│       ├── recipe/             # receitas de bancada e suporte de poções
│       ├── tags/               # tags de entidades, blocos e itens
│       └── neoforge/           # biome modifiers para spawn de entidades
└── src/main/templates/META-INF/
    └── neoforge.mods.toml      # template de metadados expandido pelo Gradle
```

Pacotes existem somente quando há requisito funcional real: monólito modular onde cada mob possui subpastas dedicadas em `entity/` e `client/`, mantendo lógica de gameplay completamente desacoplada do cliente gráfico.

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

## Anatomia de um mob (mapa de arquivos)

Tomando o Litófago como exemplo (`<id>` = `lithophage`, `<pkg>` = `lithophage`). `<id>` nomeia arquivos, scripts e registros; `<pkg>` nomeia as pastas Java e pode ser mais curto (`canopy_breaker` → `canopy`, `last_gift_collector` → `collector`, `crimson_commander` → `crimson`, `baby_dragon` → `babydragon`). Todo mob novo toca os mesmos pontos; use o mob de referência da ficha para copiar o formato.

| Camada | Arquivo / local | Papel |
|---|---|---|
| Entidade (servidor) | `entity/<pkg>/<Mob>.java` | Atributos (`createAttributes`), `enum State`, goals, ataques, `checkSpawnRules`. Sem código gráfico |
| Tipo e atributos | `registry/ModEntities.java` | `EntityType` (`ENTITY_TYPES.register`), `EntityAttributeCreationEvent`, `RegisterSpawnPlacementsEvent` |
| Itens | `registry/ModItems.java` | Ovo de spawn (`SpawnEggItem`) e itens do mob; `assets/.../items/<id>_spawn_egg.json`, `models/item/`, `textures/item/` |
| Aba criativa | `registry/ModCreativeTabs.java` | Ovo e itens |
| Sons | `registry/ModSounds.java` + `assets/.../sounds.json` | Eventos mapeados para áudios vanilla (D-010); legendas em `lang/` |
| Modelo (cliente) | `client/<pkg>/<Mob>Model.java` | `EntityModel`, hierarquia de `ModelPart` e `createBodyLayer` |
| Animação (cliente) | `client/<pkg>/<Mob>Animations.java` | `AnimationDefinition` por keyframe; acionadas por evento de entidade ou `SynchedEntityData` |
| Render | `client/<pkg>/<Mob>RenderState.java`, `<Mob>Renderer.java`, `<Mob>EyesLayer.java` | Estado de render, renderizador, camada emissiva |
| Registro do cliente | `client/<pkg>/<Mob>Client.java` | `@EventBusSubscriber(value = Dist.CLIENT)`: camada do modelo e renderizador |
| Barra de boss | `client/bossbar/<Mob>BossBar.java` + `ThemedBossBars` | Estilo temático registrado pela chave de tradução do nome da entidade; textura em `textures/gui/boss_bar/<id>.png` |
| Cinemática de boss | `client/<pkg>/<Mob>CinematicDirector.java` (ex.: `CollectorCinematicDirector`) | Câmera, FOV, letterbox, título; tecla `key.codenamecoraline.skip_cinematic` |
| Bestiário | `bestiary/Bestiary.java` | Capítulo (6+ páginas) e gravuras `textures/gui/bestiary/<id>_{anatomy,combat,spoils}.png` |
| Texturas | `assets/.../textures/entity/<id>.png`, `<id>_eyes.png` | Geradas por `tools/generate_<id>_assets.py` |
| Dados | `data/.../loot_table/entities/<id>.json`, `neoforge/biome_modifier/*`, `tags/`, `recipe/`, `advancement/bosses/` | Drops, spawn por bioma, tags, receitas, conquistas |
| Textos | `assets/.../lang/en_us.json`, `pt_br.json` | Nome, ovo, legendas, conquistas, Bestiário |
| Testes | `verification/<Mob>GameTests.java` | `@EventBusSubscriber` + `RegisterGameTestsEvent`, ativado por `Boolean.getBoolean("coraline.<id>Tests")` |
| Execução de teste | `build.gradle` (`runs { <id>Test { ... } }`) | Gera a tarefa `run<Mob>Test` |
| Ferramentas | `tools/generate_<id>_assets.py`, `Export<Mob>Model.java`, `generate_<id>_sketches.py`, `<id>-sketches.gradle` | Textura, geometria exportada, gravuras e poses |

Subsistemas compartilhados: `entity/PlayerSummon` (invocações do jogador), `entity/spirit/BossSpirit` (espírito pós-boss), `util/`, `inventory/`, `item/`. Fluxo de uma entidade nova: registrar tipo → atributos/spawn → cliente (modelo, animação, render) → dados → textos → testes.

### Dragão de Fogo (bebê): fundação e visual

`entity/firedragon/FireDragon` estende `TamableAnimal`, seguindo a base do `FeyRaven`. Sem goals concorrentes: `FireDragonCompanion` coordena prioridades no servidor por estados, com consultas escalonadas, nutrição persistente na entidade e proteções de dono/equipe. Código da fatia 3 aguardando testes do usuário. `ModEntities` registra tipo, atributos e restrição de spawn natural; `ModItems` e abas expõem o ovo criativo (distinto do ovo incubável ainda pendente). Cores e início do clipe visual são sincronizados por `SynchedEntityData`; cores persistem no save, e o relógio usa tempo do mundo para funcionar quando o cliente começa a rastrear uma entidade já existente. `FireDragonAnimation` mantém metadados comuns sem importar classes do cliente.

`client/firedragon/` contém Model, Geometry, Animations, RenderState, Renderer, EyesLayer e Client. Model reutiliza os mesmos clipes no renderizador e no exportador; a fonte de geometria/UV é `tools/generate_fire_dragon_assets.py`, que gera `FireDragonGeometry.java`, 125 texturas 128×128 e o ícone 16×16. `tools/generate_fire_dragon_animations.py` gera o enum comum, keyframes Java e documentação de ângulos; editar esses scripts e regenerar, sem alterar os arquivos gerados diretamente. `VisualPreview` em NBT é uma ferramenta de diagnóstico de pose, sem efeito em gameplay. Não há evento customizado de rede.

`FireDragonGameTests` é habilitado por `coraline.fireDragonTests` (`runFireDragonTest`): registro, spawn, dano, aparência persistente e idles do servidor. `tools/fire-dragon-sketches.gradle` exporta o modelo com todas as animações para revisão; `DragonFireball` é um projétil de item sem explosão/fogo em terreno e com ignição anterior ao dano. Cliente registra seu ThrownItemRenderer. Ombro preserva a mesma entidade no mundo em vez de recriá-la no NBT do jogador. Vínculo, voo, fogo e ombro têm código novo ainda não compilado/testado nesta etapa; jovem/adulto/ninhos seguem pendentes.

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
- **Névoa:** `WitherMistCloud` estende `AreaEffectCloud`, verifica distância e oclusão e aplica Decomposição III e Cegueira vanilla. Eleva instâncias mais fracas para III e não renova instâncias III ou superiores, preservando o relógio de dano e evitando acúmulo por sobreposição. O ID antigo da entidade é preservado para carregar nuvens salvas.
- **Efeito e poção:** `registry/ModEffects` registra o efeito `wither_resistance`; `registry/ModPotions` registra os tipos de poção e as misturas sequenciais no suporte vanilla, com dois intermediários e versões finais de três/oito minutos. Ambos usam `DeferredRegister` no ponto de entrada. `WitherResistanceEffect` remove Decomposição e Cegueira existentes e o evento `MobEffectEvent.Applicable` impede novas aplicações durante a proteção. As misturas usam `RegisterBrewingRecipesEvent` e `PotionBrewing.Builder.addMix`. O registro nativo também alimenta nomes, conversões arremessável/persistente e abas do criativo, sem inclusão manual duplicada.
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

## O Quebra-Copas (`canopy_breaker`)

- **Servidor:** `entity/canopy/CanopyBreaker` (92 PV, armadura 5, velocidade 0,29, 1,8 × 3,1; 1,0 × 2,2 ao escalar) concentra a máquina de estados (`Activity`), os seis ataques (`Attack`, fases preparar/executar/recuperar), os três estágios, a introdução, a morte e as rotas de árvore. O estado visual é escolhido no servidor e sincronizado (`DATA_ANIM`, `DATA_OVERLAY`, `DATA_CLUB`, `DATA_STAGE`, `DATA_ACTIVITY`). `entity/canopy/CanopyAnim` lista as 88 animações com estado ativador, evento, interrupção e próxima (fonte da Animation Bible).
- **Árvores:** `entity/canopy/TreeNav` detecta troncos, pontos de aproximação, poleiros e pousos e planeja saltos balísticos (arrasto horizontal 0,91, vertical 0,98, gravidade do atributo; velocidade horizontal até 1,15, vertical de 0,3 a 1,1, voo de 8 a 44 ticks, trajetória conferida em meios ticks com a caixa real). Subir usa `noPhysics` com cada passo validado contra blocos sólidos; falhas caem em `FALL_RECOVERY` ou descem pelo tronco, sem teleporte.
- **Item de invocação:** `item/HollowTrunkIdolItem` (`hollow_trunk_idol`) avalia seis condições sem consumir o item na falha.
- **Cliente:** `client/canopy/` contém `CanopyBreakerModel` (hierarquia pélvis → peito → cabeça e braços, pernas e porrete guardado), `CanopyBreakerParts` (gerado por `tools/generate_canopy_breaker_assets.py`), `CanopyBreakerAnimations` (poses absolutas convertidas para deslocamentos da postura de repouso), `CanopyBreakerRenderState`, `CanopyBreakerRenderer` (escala 1,2), `CanopyBreakerEyesLayer` e `CanopyBreakerClient`.
- **Recursos, Conquistas e Bestiário:** receita `recipe/hollow_trunk_idol.json`, saque `loot_table/entities/canopy_breaker.json`, conquistas `advancement/bosses/canopy_{idol,summon,defeat,club}.json`, sons vanilla mapeados em `sounds.json` (`ModSounds.CANOPY_*`), entrada `Bestiary.CANOPY_BREAKER` (6 páginas) e gravuras em `textures/gui/bestiary/canopy_breaker_*.png`.
- **Verificação:** `verification/CanopyBreakerGameTests` registra 17 testes na execução `runCanopyBreakerTest` (`coraline.canopyTests=true`) em `build/canopy-gametest/`. Detalhes em [design/canopy_breaker.md](design/canopy_breaker.md).

## Porrete de Raiz-Morta: Golpe Descendente em Salto (`deadroot_club`)

- **Item:** `item/DeadrootClubItem` (espada de `DEADROOT`, raridade rara). Segurar o clique direito no chão carrega o salto (até 1,5 s, `MAX_CHARGE_TICKS`); soltar salta com altura e distância proporcionais à carga (`releaseUsing`). Shift + clique direito invoca Micos de Casca (`summon`), como o Machado do Renegado. Salto gasta 4 de durabilidade, invocação 120; a recarga de 12 s (salto) e 20 s (invocação) é compartilhada pelo item.
- **Habilidade:** `item/DeadrootLeapSlam` guarda o estado do salto por jogador (mapa no servidor, limpo ao sair). O salto é vertical com leve impulso para a frente; ao passar do ápice o jogador mergulha para baixo (`DIVE_SPEED`) e, ao tocar o solo, o porrete bate: dano em área de raio 3,5 (100% a até 1,5 bloco do impacto, 60% no anel externo, só em `Enemy`), com O voo é no estilo de um martelo arremessado: o porrete puxa o jogador na direção exata do olhar (velocidade de 0,9 a 2,0 conforme a carga, mais um leve impulso de 0,22 para cima) e mantém o empuxo por 3 a 10 ticks; depois o jogador plana no embalo. Segurar Shift no ar faz o mergulho. Uma parede interrompe o empuxo. O impacto toca `MACE_SMASH_GROUND_HEAVY`, solta poeira, pedra e dois anéis de partículas. Dano base 6 mais 1,5 por bloco de queda (máximo +16 blocos, ou seja, até 30). Sem dano de queda para o jogador.
- **Eventos:** `PlayerTickEvent.Post` avança o salto; `PlayerLoggedOutEvent` descarta o estado.
- **Verificação:** `verification/DeadrootClubGameTests` (execução `runDeadrootClubTest`, `coraline.deadrootTests=true`).

## Micos de Casca (`bark_macaque`)

- **Servidor:** `entity/macaque/BarkMacaque` (Monster; 14 PV, armadura 2, velocidade 0,32) com máquina de estados `Activity` (`ARRIVE, GROUND, TRUNK_APPROACH, TRUNK_CLIMB, LEAP_PREPARE, AIRBORNE, LAND, RETREAT, DYING`), alvo apenas jogador, salto balístico com mordida única, rota de tronco com `TreeNav` e `WallClimberNavigation`. `MacaqueAnim` lista as 12 animações (sincronizadas por `DATA_ANIM`).
- **Boss:** `CanopyBreaker` guarda os chamados pendentes, aplica o limite de 4 lacaios, chama 2 na abertura e 1 em cada transição de estágio e os dispensa na morte/remoção (`callMinions`, `getMinions`, `dismissMinions`).
- **Cliente:** `client/macaque/` com `BarkMacaqueModel`, `BarkMacaqueAnimations`, `BarkMacaqueRenderState`, `BarkMacaqueRenderer`, `BarkMacaqueEyesLayer`, `BarkMacaqueClient` e o `BarkMacaqueParts` gerado por `tools/generate_bark_macaque_assets.py`.
- **Recursos:** ovo `bark_macaque_spawn_egg`, sons vanilla mapeados em `sounds.json` (`ModSounds.MACAQUE_*`), traduções pt/en. Testes em `CanopyBreakerGameTests`.

## Barras de vida de bosses (`client/bossbar`)

Regra do projeto (ver `CLAUDE.md`): cada boss desenha a própria barra. `ThemedBossBars` cancela a barra padrão (`CustomizeGuiOverlayEvent.BossEventProgress`) para bosses com estilo registrado e delega a um `Style`. Nicolas Noel, o Renegado usa `LastGiftCollectorBossBar`: moldura 288×44 de galhadas palmadas, trilha com pelo e pingentes de gelo, machados cruzados e o rosto dele (gorro, olhos âmbar, nariz longo, barba enorme), com o slot de vida transparente (fundo e preenchimento são desenhados antes da moldura); o nome é dividido em dois e fica sob a trilha, de cada lado da barba (`tools/generate_boss_bar_last_gift_collector.py`); o preenchimento muda de cor por estágio (vermelho, vinho, gelo, ouro e, na Fúria, fogo pulsante) e a trilha marca onde cada estágio começa.

O Quebra-Copas usa `CanopyBreakerBossBar` (moldura `textures/gui/boss_bar/canopy_breaker.png`, gerada por `tools/generate_boss_bar_canopy_breaker.py`): casca rachada com marcas de garra, raízes com agulhas e musgo, o rosto da fera sobre o meio, entalhes nas marcas de 60% e 25%, nome centralizado sob a barra e cor do preenchimento por estágio (verde-musgo, âmbar, brasa pulsante).

O Comandante Carmesim usa `CrimsonCommanderBossBar` (moldura `textures/gui/boss_bar/crimson_commander.png`, gerada por `tools/generate_boss_bar_crimson_commander.py`): a espada larga deitada, com o sulco da lâmina como slot de vida, o elmo com visor em T sobre o meio, entalhes dourados em 60% e 25%, nome dividido dos dois lados do elmo e preenchimento carmesim, rubi incandescente e ouro-sangue pulsante conforme o estágio (`RED`, `PINK`, `YELLOW`).

## Comandante Carmesim (`crimson_commander`)

- **Servidor:** `entity/crimson/CrimsonCommander` (220 PV, armadura 14 + 6 de resistência, 1,1 × 3,5) com máquina de estados `Activity` (`INTRO, GROUND, ATTACK, TRANSITION, DYING, DEAD, HEALING`) e ataques aninhados (`Attack`: corte, investida, golpe pesado aéreo, guarda, riposta, onda; fases preparar/executar/recuperar). As ondas de sangue são simuladas pelo próprio boss (`Wave`: posição, direção, alvos atingidos), sem entidade de projétil. O estado visual é escolhido no servidor e sincronizado (`DATA_ANIM`, `DATA_OVERLAY`, `DATA_ACTIVITY`, `DATA_STAGE`); `entity/crimson/CrimsonAnim` lista as 35 animações (fonte da tabela de `docs/design/crimson_commander.md`). Persistência em NBT: estágio, transições, Sentinelas chamadas, espada entregue, primeiro agressor.
- **Lacaios:** `entity/crimson/CrimsonAshVanguard` (28 PV, 1,95 de altura), ligado ao Comandante por UUID, limite de 2, vida de 600 ticks, sem saque; desfaz-se quando o mestre cai (`CrimsonCommander.summonVanguards`, `getVanguards`, `dismissVanguards`).
- **Itens:** `item/StainedOathCrestItem` (ritual em ardósia, três condições, sem consumir na falha) e `item/CrimsonOathGreatswordItem` (Investida Carmesim; estado por jogador num mapa do servidor, avançado em `PlayerTickEvent.Post` e limpo ao sair). Fragmento, retrato e dois ovos são itens simples em `ModItems`.
- **Spawn:** `CrimsonCommander.checkSpawnRules` registrado em `ModEntities.onSpawnPlacements`; biome modifier `neoforge/biome_modifier/crimson_commander_spawns.json` sobre a tag `tags/worldgen/biome/crimson_commander_habitat.json`.
- **Cliente:** `client/crimson/` contém `CrimsonCommanderModel` (modelo em tamanho real, também usado pelas Sentinelas, que escondem penacho, capa e espinhos), `CrimsonCommanderParts` (gerado por `tools/generate_crimson_commander_assets.py`), `CrimsonCommanderAnimations` (poses absolutas convertidas em deslocamentos da postura de repouso), `CrimsonCommanderRenderState`, `CrimsonEyesLayer` (visor, runas e fendas emissivas), `CrimsonCommanderRenderer`, `CrimsonAshVanguardRenderer` (escala 0,557), `CrimsonCommanderSpiritRenderer` e `CrimsonCinematicDirector` (câmera, FOV, tarjas e título da intro, como os outros bosses) e `CrimsonCommanderClient`.
- **Recursos, Conquistas e Bestiário:** receita `recipe/crimson_oath_greatsword.json`, saque `loot_table/entities/crimson_commander.json`, conquistas `advancement/bosses/{oath_discovered,commander_summoned,commander_defeated,crimson_greatsword_obtained}.json`, sons `ModSounds.CRIMSON_*` e `VANGUARD_*` mapeados para eventos vanilla, entrada `Bestiary.CRIMSON_COMMANDER` (6 páginas) e gravuras `textures/gui/bestiary/crimson_commander_*.png`.
- **Verificação:** `verification/CrimsonCommanderGameTests` registra 34 testes na execução `runCrimsonCommanderTest` (`coraline.crimsonTests=true`) em `build/crimson-gametest/`. Detalhes em [design/crimson_commander.md](design/crimson_commander.md).

## A Bruxa dos Dez Fios (`ten_threads_witch`)

- **Servidor** (`entity/ten_threads_witch/`): `MatriarchWitchEntity` (150 PV, 0,6 × 1,15) conduz o encontro inteiro com a máquina `EncounterPhase` (`DORMANT, INTRO, STAGE_01..STAGE_10, STAGE_11_TRANSITION, STAGE_11_FURY, DEATH_CINEMATIC`): intro, uma marionete por estágio, reação e invocação entre estágios, colapso, fúria e morte com uma só caixa de recompensa. Ela é intocável enquanto qualquer fio resistir (`hurtServer`). `PuppetEntity` é **a única classe das dez marionetes**; o que as diferencia é a tabela `PuppetKind` (atributos, caixa, três ataques com dados em `PuppetAttack`), o modelo e o som. O estado da marionete (`PuppetState`) e a animação (`PuppetAnim`, 13) são decididos no servidor e sincronizados (`DATA_STATE`, `DATA_ANIM`, `DATA_CRIMSON`, `DATA_CRACKED`, `DATA_OWNER_ID`).
- **Ataques** (`AttackRunner`): **uma** implementação das fases preparar → executar → recuperar para as 11 criaturas (estilos `CONE, FAN, MULTI, RING, CHARGE, LEAP, WAVES, STORM, GRAPPLE, SIDESTEP, BURROW, TRAP, WIRE`). O hospedeiro (`AttackRunner.Host`) recebe `phaseChanged`, `attackEnded`, `tangle` e `crashed`. Regras comuns (nunca bate no ar, escolha ponderada, mira à frente, aliados nunca se ferem) estão ali, não nas marionetes.
- **Fio carmesim**: `ThreadHitboxEntity` (hitbox cortável, não salvo, ligado à marionete por UUID, dono = a Matriarca da marionete) e `PuppetEntity.cutThread`/`enterTangled` (15% de dano, 70 ticks, ×1,75). `SpoolVitalOrbEntity` é o orbe de recuperação. A limpeza da Matriarca (morte, reinício) só mexe nos hitboxes dos **seus** fios.
- **Arena e anticheese**: `ArenaAnchorManager` (âncoras em troncos ou Postes de Costura de partículas), círculo de 28 blocos que só prende quem entrou, anti-pilar (`pullDown`) e anti-buraco (`PuppetEntity.lift`) conduzidos pela Matriarca em `tickAntiCheese`, reset por ausência (`tickReset`). Nenhum bloco é colocado ou destruído (a Matriarca só quebra folhas por onde o Cervo investe).
- **Texto e falas**: `WitchDialogueManager` (`Line`, `say`, gancho de voz opcional); chaves `dialogue.codenamecoraline.ten_threads_witch.*`.
- **Itens**: `item/CursedSpindleItem` (ritual com cinco resultados, sem consumir na falha) e `item/TenThreadsStaffItem` (três posturas no `CUSTOM_DATA`, recarga antes do desgaste). Retalho, dedal, retrato e ovo são itens simples em `ModItems` (+ tag `puppet_remnants`); sons `ModSounds.WITCH_*`, `PUPPET_*` e `puppetAction(kind)` mapeados para eventos vanilla.
- **Cliente** (`client/ten_threads_witch/`): `PuppetModel` (um só modelo parametrizado pelo id, também o da Matriarca), `TenThreadsParts` (gerado por `tools/generate_ten_threads_witch_assets.py`; não editar à mão), `PuppetAnimations` (uma animação por **papel** da parte), `PuppetRenderState`, `TenThreadsEyesLayer` (emissivo), `PuppetThreadRenderLayer` (o fio até o cajado em linhas, sem biblioteca), `PuppetRenderer`, `MatriarchRenderer` e `TenThreadsWitchClient` (registro das 11 camadas; hitbox e orbe usam `NoopRenderer`, são só partículas). A barra é `client/bossbar/TenThreadsWitchBossBar`.
- **Recursos, Conquistas e Bestiário**: receita `recipe/cursed_spindle.json`, saque `loot_table/entities/ten_threads_witch.json`, conquistas `advancement/bosses/{craft_cursed_spindle,summon_ten_threads_witch,sever_crimson_thread,defeat_ten_threads_witch,obtain_ten_threads_staff}.json`, entrada `Bestiary.TEN_THREADS_WITCH` (8 páginas) e gravuras `textures/gui/bestiary/ten_threads_witch_*.png`.
- **Animação da Matriarca** (desde 2026-11-05; ver [design/matriarch_animation.md](design/matriarch_animation.md)): servidor em `entity/ten_threads_witch/matriarch/` — `MatAnim` (catálogo de 142 entradas com camada, prioridade, duração, flags e eventos), `MatriarchAnimDirector` (ganchos da IA → clipes por camada, regras de prioridade/interrupção, eventos disparados uma vez), `MatriarchPresence` (passeio, recuo e flutuação), `MatriarchMoveset` (ataques da fúria, clipes por fase e combos), `MatriarchEffects`, `MatriarchMood`, `MatriarchAnimSync`/`MatClipRef` (cinco camadas sincronizadas: corpo, reação, mão esquerda, mão direita, rosto; mais estilo de passo, olhar e foco). Cliente em `client/ten_threads_witch/matriarch/` — `MatriarchAnimator` (composição por quadro), `PoseMixer` (prioridade por articulação), `MatClip`/`ClipBuilder`/`Ease` (amostragem de `AnimationDefinition` vanilla num buffer), controladores de passo, olhar, dedos e tecido/cabelo, `MatriarchModel`, `MatriarchThreadLayer` e os clipes por categoria (`IdleClips`, `LocomotionClips`, `ThreadClips`, `CombatClips`, `ReactionClips`, `FaceClips`, `CinematicClips`). A Matriarca deixou de usar `PuppetModel`/`PuppetAnimations` em jogo (a ferramenta de gravuras ainda os usa para a pose de repouso).
- **Verificação**: `verification/TenThreadsWitchGameTests` registra 30 testes na execução `runTenThreadsWitchTest` (`coraline.tenThreadsTests=true`) em `build/ten-threads-gametest/`; `verification/MatriarchAnimationGameTests` registra 20 na execução `runMatriarchAnimationTest` (`coraline.matriarchAnimTests=true`) em `build/matriarch-animation-gametest/`. Detalhes em [design/ten_threads_witch.md](design/ten_threads_witch.md).

## Invocações do jogador (`entity/PlayerSummon`)

Interface comum às criaturas invocadas por armas (Presentes Famintos aliados do Machado do Renegado, Micos de Casca aliados do Porrete de Raiz-Morta). Uma invocação de jogador nunca mira nem fere outra (de qualquer arma), e o golpe em área do porrete as poupa. Os lacaios do boss da mesma espécie respondem `false` e continuam inimigos.

## Espíritos dos bosses (`entity/spirit/BossSpirit`)

Entidade visual (LivingEntity sem colisão, sem dano, sem salvar) que sobe a 0,045 bloco por tick por 90 ticks a partir do corpo do boss. Cada boss tem seu tipo (`canopy_breaker_spirit`, `last_gift_collector_spirit`, `crimson_commander_spirit`) para o cliente desenhar com o modelo dele: `CanopyBreakerSpiritRenderer` e `LastGiftCollectorSpiritRenderer` usam `RenderTypes.entityTranslucent`, cor azul-pálida com alfa que entra e some (`BossSpirit.visibility`) e luz cheia. O Quebra-Copas o libera no impacto da queda da morte (tick 48), o Cobrador quando os olhos apagam (tick 45).

## Intro do Quebra-Copas (`client/canopy/CanopyCinematicDirector`)

No padrão do Cobrador: enquanto o boss está em `INTRO` (120 ticks), a câmera do jogador próximo vira para ele (primeiro a copa, depois o peito, depois o rosto), o FOV fecha, tarjas de cinema entram, cada golpe no peito e o rugido tremem a câmera, e o título aparece com o rugido. A mesma tecla de pular (V) ou levar dano libera a câmera. O roteiro do servidor (`tickIntro`): olhar em volta (0 a 24), `INTRO_CHEST_BEAT` com um tambor por golpe (24 a 72), `ROAR_AGGRO` e a chegada dos dois Micos (72 a 104).

## Corvo Feérico (`fey_raven`)

- **Servidor:** `entity/raven/FeyRaven` (TamableAnimal implementando `PlayerSummon`; 14 PV, armadura 0, velocidade terrestre 0,28, velocidade de voo 0,40, ataque 2,0, hitbox 0,5 × 0,65, altura dos olhos 0,55) implementa a máquina de estados sincronizada `RavenState` (`IDLE_GROUND`, `WALK`, `FLY_ORBIT`, `PERCH_SHOULDER`, `FETCH_ITEM`, `SENTINEL_ALERT`, `PECK_ATTACK`, `SITTING`, `HURT`, `DYING`), domesticação com `minecraft:glow_berries` (33% de chance por baga) ou cura (+4 PV quando domesticado), ciclo de comandos `CommandMode` (`FOLLOW` → `SIT` → `SENTINEL` → `FOLLOW`, mão vazia), empoleiramento no ombro (`Shift + Clique direito` de mãos vazias preservando a entidade via `startRiding`), inventário de 1 slot no pergaminho da perna (`SimpleContainer(1)` persistido em NBT), coleta automática de itens brilhantes em raio de 12 blocos (`FETCH_ITEM`), Sentinela Mística que aplica `MobEffects.GLOWING` por 100 ticks (5 s) a hostis invisíveis em raio de 16 blocos (`SENTINEL_ALERT`, recarga de 600 ticks = 30 s), e voo de retorno em emergência quando a vida cai abaixo de 30% (`<= 5,4 PV`).
- **Spawn:** `FeyRaven.checkSpawnRules` registrado em `ModEntities.onSpawnPlacements`; biome modifier `neoforge/biome_modifier/fey_raven_spawns.json` sobre a tag `tags/worldgen/biome/fey_raven_habitat.json` (`dark_forest`, `old_growth_pine_taiga`, `old_growth_spruce_taiga`, `taiga`, `forest`).
- **Cliente:** `client/raven/` contém `FeyRavenModel` (21 juntas articuladas e 23 caixas 3D sem interseção de UV em atlas 64×64, incluindo terceiro olho na testa, colar de penas, asas duplas, cauda tripla, bagas no bico e pergaminho na canela esquerda), `FeyRavenAnimations` (10 animações por keyframe), `FeyRavenRenderState`, `FeyRavenEyesLayer` (camada emissiva ciano para os três olhos, runas das asas/peito e bagas), `FeyRavenRenderer` e `FeyRavenClient`.
- **Recursos, Conquistas e Bestiário:** item `codenamecoraline:fey_feather` (`ModItems.FEY_FEATHER`), ovo `fey_raven_spawn_egg`, tabela de saque `loot_table/entities/fey_raven.json`, 5 eventos sonoros em `ModSounds` (`FEY_RAVEN_AMBIENT`, `FEY_RAVEN_ALERT`, `FEY_RAVEN_FLAP`, `FEY_RAVEN_HURT`, `FEY_RAVEN_DEATH`) mapeados para eventos vanilla em `sounds.json`, 3 conquistas (`advancement/bosses/{find_fey_raven,tame_fey_raven,fey_messenger}.json`), capítulo `Bestiary.FEY_RAVEN` (6 páginas) e gravuras `textures/gui/bestiary/fey_raven_{anatomy,combat,spoils}.png`.
- **Verificação:** `verification/FeyRavenGameTests` registra 8 testes na execução `runFeyRavenTest` (`coraline.feyRavenTests=true`) em `build/fey-raven-gametest/`. Detalhes em [design/fey_raven.md](design/fey_raven.md).

## Salamandra de Enxofre (`brimstone_salamander`)

- **Servidor:** `entity/salamander/BrimstoneSalamander` (Monster; 44 PV, 8 de armadura, 2 de tenacidade, velocidade terrestre 0,23, velocidade em lava 0,38, dano base 6,0, repulsão 0,65, alcance 24, hitbox 1,3 × 0,85, altura dos olhos 0,55) implementa a máquina de estados `State` (`IDLE`, `STALKING`, `PURSUING`, `SNAP_BITE`, `SULFUR_SPIT`, `TAIL_WHIP`, `RECOVERY`, `DYING`), ataques com 4 fases de execução, anti-whiff com cancelamento aos 7 ticks da mordida e recarga curta de 15 ticks, atordoamento adicional de +10 ticks na mordida bloqueada por escudo, anti-pillaring para alvos 2 a 5 blocos acima e anti-cerco para 2+ inimigos a até 2 blocos. Fora do Nether, busca ativamente blocos incandescentes quando em repouso.
- **Projétil:** `entity/salamander/BrimstoneSpit` estende `ThrowableItemProjectile`, causa 5,0 de dano e transforma-se no impacto em uma Poça de Alcatrão Ardente (2×2 blocos por 100 ticks) que aplica Lentidão II e incendeia entidades vivas que não sejam salamandras.
- **Spawn:** `BrimstoneSalamander.checkSpawnRules` em `ModEntities.onSpawnPlacements`; bioma configurado via `neoforge/biome_modifier/brimstone_salamander_spawns.json` sobre a tag `tags/worldgen/biome/brimstone_salamander_habitat.json` (Basalt Deltas, Nether Wastes, Crimson Forest) entre Y = 24 e Y = 48 em basalto, pedra-negra, rocha do nether ou adjacente a lava.
- **Cliente:** `client/salamander/` contém `BrimstoneSalamanderModel` (18 juntas e 26 caixas em atlas 128×128 sem sobreposição de UVs), `BrimstoneSalamanderAnimations` (17 animações por keyframe), `BrimstoneSalamanderRenderState`, `BrimstoneSalamanderEyesLayer` (camada emissiva de enxofre/magma para olhos, papo de fole, narinas e placas dorsais), `BrimstoneSalamanderRenderer` e `BrimstoneSalamanderClient` (registra renderizador da salamandra e renderizador com sprite do projétil `brimstone_spit`).
- **Recursos, Conquistas e Bestiário:** item `codenamecoraline:brimstone_scale` (`ModItems.BRIMSTONE_SCALE`), item de projétil `codenamecoraline:brimstone_spit`, ovo `brimstone_salamander_spawn_egg`, tabela de saque `loot_table/entities/brimstone_salamander.json` (creme de magma, carvão e escama de enxofre com bônus de pilhagem), 7 eventos sonoros em `ModSounds` (`BRIMSTONE_SALAMANDER_AMBIENT`, `HURT`, `DEATH`, `STEP`, `BITE`, `SPIT`, `TAIL_WHIP`) mapeados em `sounds.json`, 2 conquistas (`find_brimstone_salamander`, `brimstone_scale`), capítulo `Bestiary.BRIMSTONE_SALAMANDER` (6 páginas) e gravuras `textures/gui/bestiary/brimstone_salamander_{anatomy,combat,spoils}.png`.
- **Verificação:** `verification/BrimstoneSalamanderGameTests` registra 5 métodos de teste na execução `runBrimstoneSalamanderTest` (`coraline.brimstoneSalamanderTests=true`) cobrindo atributos, regras de spawn, imunidades, snap bite, anti-whiff, cuspe balístico, poça de alcatrão, tail whip e matriz de alvos/ecologia. Detalhes em [design/brimstone_salamander.md](design/brimstone_salamander.md).

## O Arquiteto — Kandiaru (`architect`)

- **Entidade:** `entity/architect/Architect` (boss, 480 PV; estados em `Architect.State`, fases em `Architect.Phase`). A linha do tempo de cada ataque roda em `tick()`; as decisões ficam em `customServerAiStep`. Tabela de ataques em `ArchitectAttack`, volumes de golpe (ARCO, ANEL, CORREDOR) em `ArchitectStrikes`, proteção dos jogadores na revelação em `ArchitectRevealProtection` (`LivingIncomingDamageEvent`). Eventos de rede 160–178.
- **Cliente:** `client/architect/` — modelo de 110 partes e animações geradas por `tools/generate_architect_assets.py` (fonte única dos clipes: `tools/architect_clips.py`), `ArchitectGlowLayer` (brilho por estado), `ArchitectCinematicDirector` (cinemática da revelação), barra `client/bossbar/ArchitectBossBar`.
- **Itens:** `item/CommandmentTabletItem` (ergue a estátua; receita `recipe/commandment_tablet.json`) e `item/ArchitectScimitarItem` (drop: Aparo e Réplica; estado por jogador num mapa do servidor avançado em `ServerTickEvent.Post`; aparo corpo a corpo em `LivingIncomingDamageEvent`, de projétil em `ProjectileImpactEvent`). O brilho da cimitarra carregada vem da flag 0 de `CUSTOM_MODEL_DATA`, lida pelo modelo do item (`items/architect_scimitar.json`: `using_item`, `custom_model_data`, `cooldown`).
- **Poses do jogador com a cimitarra:** `client/architect/ArchitectScimitarClient` (`IClientItemExtensions`: pose de braço e mão em primeira pessoa) e `ScimitarArmPoses`, duas entradas novas de `HumanoidModel.ArmPose` declaradas em `META-INF/enumextensions.json` (D-038).
- **Recursos, conquistas e Bestiário:** saque `loot_table/entities/architect.json`, conquistas `advancement/bosses/architect_{tablet,statue,defeated,scimitar}.json`, sons `ModSounds.ARCHITECT_*` e `SCIMITAR_*` mapeados para eventos vanilla (D-010), entrada `Bestiary.ARCHITECT` (8 páginas) e gravuras `textures/gui/bestiary/architect_*.png` (`tools/generate_architect_bestiary.py`). Assets da cimitarra e do retrato: `tools/generate_architect_scimitar_assets.py`.
- **Verificação:** `verification/ArchitectGameTests` (execução `runArchitectTest`, `coraline.architectTests=true`). Ficha: `docs/design/architect.md`.
