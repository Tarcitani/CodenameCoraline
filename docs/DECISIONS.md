# Decisões arquiteturais

Registro apenas de decisões relevantes: contexto, decisão e consequências. Não é diário de mudanças (isso é o Git).

## D-037 — Verificação de modelos opt-in e câmera de montaria anatômica

**Contexto.** A inspeção no cliente mostrou que a câmera frontal padrão entrava na cabeça longa do Adulto. O primeiro servidor dedicado real também detectou que a reflexão do registro automático das suítes de geometria tentava resolver `PoseStack.Pose`, ausente na distribuição dedicada, mesmo com os testes desabilitados.

**Decisão.** Usar o atributo vanilla sincronizado `CAMERA_DISTANCE` com distância baseada na largura do rig. Um assinante comum, `ModelGameTestRegistration`, delega às suítes de geometria do dragão, Quebra-Copas e Carmesim apenas quando suas propriedades de teste estão habilitadas; as classes que contêm assinaturas gráficas deixam de ser assinantes automáticos.

**Consequências.** A câmera continua usando a proteção vanilla contra paredes. Servidores normais não carregam classes de renderização dos testes, e os testes continuam exercitando os modelos reais no GameTestServer de desenvolvimento. Build, 81 testes do dragão, 35 do Quebra-Copas e 34 do Carmesim aprovados; servidor dedicado iniciado em loopback. Sem alteração dos ataques desses bosses.

## D-036 — Anatomias próprias com rig de ataque comum entre cliente e servidor

**Contexto.** A evolução anterior ampliava a mesma geometria. A nova ficha exige silhuetas, proporções, asas e caudas diferentes, preservando identidade cromática, gameplay e entidade.

**Decisão.** Três modelos nativos e atlas próprios, com hierarquia de nomes comum e 17 animações por estágio. Geometria é reproduzível por Python/Pillow, com paleta extraída da textura Bebê e snapshot de origem com hash. Torso chanfrado por seções, asas com braço/cotovelo/punho/dedos e membranas recortadas, pescoço hierárquico, garras e caudas de 3/4/6 segmentos. O gerador produz também `DragonRig`, única fonte dos pivôs usados por `DragonAnatomy`; as mesmas rotações posicionam a cabeça no modelo e calculam boca/eixo do fogo no servidor. O renderer escolhe a instância por estado no momento de submissão, preservando o desenho de múltiplas idades no mesmo quadro. Máscaras emissivas e uma brasa ocasional complementam o Adulto sem cobrir a pele.

**Consequências.** Sem nova entidade, biblioteca ou alteração de balanceamento. Dimensões físicas e assento passam a seguir anatomias reais, além da escala; preparos e disparo mantêm os tempos de gameplay. A mira usa o pivô articulado, e um teste compara o ponto e o eixo com a matriz da cabeça real. A comparação com a imagem de referência permanece impossível enquanto ela não estiver disponível: o anexo recebido contém só texto. As versões de corpo/UV anteriores são substituídas pelas novas, mas registros, identidade, vínculo e persistência permanecem.

## D-033 — Evolução do companheiro existente, montaria de servidor e inventário vanilla

**Contexto.** Expandir `baby_dragon` para três idades preservando mundos, UUID, dono, modelo, domesticação e comandos anteriores, sem bibliotecas de runtime.

**Decisão.** Estágios e XP próprios sincronizados/persistentes na mesma entidade. Crescimento 1/1,8/3 afeta dimensões, olhos, boca, modelo e assento; a evolução dura 60 ticks e espera espaço livre. Crédito de XP usa dano efetivo contra hostis, teto por vítima e bônus único de participação na morte. Inventário nativo com 18 posições persistentes por índice, ativadas por idade; apenas o dono acessa; morte derruba itens uma vez e downgrade administrativo recusa conteúdo em posições que seriam ocultadas. Montaria usa pacote de intenção validado e física no servidor, sem aceitar coordenadas do cliente; X desce, G desmonta, R dispara e B sopra (V já era usado pelas cinemáticas). Graveto normal preserva ficar/seguir em qualquer idade; agachar com livro abre a tela sem substituir o ombro Bebê.

**Consequências.** O registro e os recursos existentes continuam; não se cria outra entidade de dragão, reprodução ou ressurreição. Transferência automática entre dimensões continua fora do sistema. O ledger guarda até 1024 vítimas, mais que o necessário para alcançar o teto de 2000 XP; após isso recusa novos créditos. Aparência em movimento, sensação de voo e latência exigem validação humana.

## D-034 — Magma temporário sem substituição de terreno

**Contexto.** A bola colossal precisa deixar magma que expire mesmo com chunks descarregados, sem apagar construções ou alterações posteriores.

**Decisão.** Bloco próprio sem item nem loot, aparência do magma vanilla e sem o dano indiscriminado de `MagmaBlock`. Colocação exclusivamente em ar sobre lista curta de terreno natural, atrás do projétil. Diário `SavedData` por dimensão com posição, prazo em tempo de jogo e UUID do dragão. Não forçar chunks; ao voltar, restaurar o ar somente se ainda houver o bloco temporário. Dano usa o filtro de aliados do mestre; se ele não está disponível, não há dano.

**Consequências.** Nenhum bloco existente é substituído. Pausar o servidor pausa o tempo de expiração; chunks descarregados conservam o prazo e são limpos ao carregar. O rastro tem cap de 4096 posições por dimensão. Quebrar ou substituir uma posição invalida sua restauração, preservando mudanças posteriores.

## D-035 — JUnit apenas para os testes unitários existentes

**Contexto.** O build completo falhava em `compileTestJava`: `ThreadSafeLruCacheTest` já importava Jupiter, mas não havia dependência declarada.

**Decisão.** Com autorização explícita do usuário em 2026-11-04, declarar BOM JUnit 5.13.4, Jupiter e launcher somente nas configurações `testImplementation/testRuntimeOnly`, com `useJUnitPlatform`. Os GameTests do mod continuam nativos.

**Consequências.** Os 15 testes unitários passam a executar no `build`; nada do JUnit integra o jar distribuído ou vira dependência de runtime do mod. Nenhum teste existente é desativado ou enfraquecido.

**Correção da fixture.** A execução real expôs colisão entre as chaves `put` (0–15999) e `getOrCompute` (10000–25999) no teste concorrente. Operações legítimas podiam produzir valores diferentes para a mesma chave, contrariando a asserção. A faixa calculada agora começa após todas as chaves de `put`; quantidade de threads/operações, capacidade e asserções foram mantidas.

## D-001 — Plataforma: NeoForge com Minecraft 26.1.2

- **Contexto:** o projeto é um mod Java para Minecraft. Não havia versão ou loader definidos. O usuário escolheu, entre as opções oferecidas (1.21.1, 1.21.11 e 26.1.2), a linha 26.1.2 com NeoForge.
- **Decisão:** Minecraft 26.1.2 + NeoForge 26.1.2.124 (última build verificada no Maven do NeoForge), usando o template oficial `NeoForgeMDKs/MDK-26.1.2-ModDevGradle` como referência.
- **Consequências:**
  - Exige Java 25 (toolchain do build). O Java 21 do sistema não basta.
  - O ecossistema de mods de terceiros para 26.1.2 é menor que o da linha 1.21.1. Dependências externas precisam ser verificadas antes de adoção.
  - Atualizações de versão exigem alinhar `minecraft_version`, `minecraft_version_range` e `neo_version`.

## D-002 — Java 25 com toolchain provisionada pelo Gradle

- **Contexto:** o sistema tem apenas Java 21 (Temurin). O Minecraft 26.1.2 é distribuído com Java 25.
- **Decisão:** `java.toolchain.languageVersion = 25` em `build.gradle`, com o plugin `org.gradle.toolchains.foojay-resolver-convention` em `settings.gradle` para baixar o JDK 25 quando ausente. Não se altera `JAVA_HOME` nem o Java padrão do sistema.
- **Consequências:** o primeiro build baixa o JDK 25 para `%USERPROFILE%\.gradle\jdks`. O JDK 21 continua disponível para outros projetos.

## D-003 — Gradle Wrapper e plugin ModDevGradle

- **Contexto:** o template oficial usa Gradle 9.2.1 e o plugin `net.neoforged.moddev` 2.0.148 (última versão publicada no Maven do NeoForged na verificação).
- **Decisão:** manter essas versões, obtidas do mirror oficial do template. Não se usa Gradle instalado globalmente.
- **Consequências:** atualizações do plugin ou do Gradle devem ser verificadas contra a documentação do ModDevGradle.

## D-004 — Monólito com um único módulo Gradle

- **Contexto:** o projeto começa sem requisitos de gameplay.
- **Decisão:** um módulo Gradle, um pacote raiz `com.codenamecoraline`, apenas o ponto de entrada. Sem subprojetos, sem frameworks internos, sem pacotes vazios.
- **Consequências:** a estrutura cresce conforme os requisitos. Criar um subprojeto exige nova decisão aqui.

## D-005 — Sem framework de testes por enquanto

- **Contexto:** não há lógica testável. Testes de jogo (gametests) e JUnit exigem decisões de dependência.
- **Decisão:** nenhuma dependência de teste é adicionada nesta etapa. A verificação é `build` e execução de desenvolvimento.
- **Consequências:** a primeira lógica testável deve trazer sua decisão de framework aqui.

## D-006 — Isolamento do projeto e Gradle

- **Contexto:** o usuário mantém outro mod (`MeuPrimeiroMod`) no mesmo computador, com o Gradle usando `%USERPROFILE%\.gradle`.
- **Decisão:** nenhum arquivo do outro projeto é lido para copiar ou reutilizar. Caches do Gradle permanecem no diretório padrão, por escolha do usuário. Versões de artefatos são fixadas por projeto.
- **Consequências:** caches são compartilhados fisicamente entre os dois projetos, mas cada um resolve suas próprias versões. Se for necessário isolamento total, redefinir `GRADLE_USER_HOME` por comando (decisão futura).

## D-007 — Licença provisória

- **Contexto:** a licença do mod não foi definida.
- **Decisão:** `mod_license=All Rights Reserved`, sem arquivo de licença no repositório.
- **Consequências:** definir a licença real antes de qualquer publicação aberta; ver `docs/ROADMAP.md`.

## D-008 — Group ID e pacote raiz mantidos

- **Contexto:** `com.codenamecoraline` foi escolhido como provisório. O usuário decidiu mantê-lo.
- **Decisão:** `mod_group_id=com.codenamecoraline` e pacote `com.codenamecoraline` ficam como estão.
- **Consequências:** trocar o pacote no futuro exige renomear as classes e o `mod_id` continua independente. Não há publicação externa que dependa do group ID.

## D-009 — Animações: keyframes do jogo + procedural, sem biblioteca

- **Contexto:** o Redcap precisava de estocada com o pique, caminhada própria e variações de idle. Existem bibliotecas de animação (ex.: GeckoLib), mas seriam uma dependência nova.
- **Decisão:** usar o sistema de keyframes do próprio Minecraft (`AnimationDefinition`, `KeyframeAnimation`, `AnimationState`, o mesmo do Warden) para ações pontuais, e código procedural em `setupAnim` para movimentos contínuos (caminhada, respiração, movimento secundário). Os keyframes ficam em Java (`RedcapAnimations`), no mesmo formato que o Blockbench exporta.
- **Consequências:** nenhuma dependência nova. Os nomes de partes do modelo precisam ser únicos. A estocada atrasa o dano em 5 ticks (`PikeThrustGoal`) para coincidir com o golpe visual; se o alvo sair do alcance nesse intervalo, o golpe erra.

## D-010 — Sons do Redcap montados com áudios do jogo base

- **Contexto:** não há encoder OGG Vorbis instalado (sem ffmpeg, oggenc, sox nem bibliotecas Python de áudio), e o jogo só aceita `.ogg`. Instalar ferramentas exige autorização.
- **Decisão:** os eventos de som são do mod (`ModSounds`, legendas próprias), mas `sounds.json` aponta para áudios do jogo base com pitch alterado (bruxa e illagers para a voz, golem de ferro para as botas, raposa para farejar, rebolo para afiar). Nenhum áudio do jogo é copiado para o repositório.
- **Consequências:** funciona sem arquivos de áudio novos. Para uma voz original basta adicionar `.ogg` em `assets/codenamecoraline/sounds/` e trocar as entradas de `sounds.json`; o código não muda.

## D-011 — Variantes do Redcap por arma

- **Contexto:** o usuário quer Redcaps que carregam armas diferentes: lança, adaga, e um sem arma que ataca com garras e arremessa pedras. Também queria só uma mão atacando por golpe.
- **Decisão:** uma única entidade (`codenamecoraline:redcap`) com a variante como dado sincronizado, em vez de três tipos de entidade. Um único ovo de geração (`redcap_spawn_egg`) cria Redcaps de qualquer variante, sorteada ao nascer; `/summon codenamecoraline:redcap {Variant:"dagger"}` fixa a variante (o NBT `Variant` tem prioridade sobre o sorteio). Os números de cada variante ficam em `RedcapVariant`.
- **Consequências:** um só modelo, renderizador e conjunto de sons. Variantes novas exigem um valor no enum, uma parte de arma no modelo e uma animação de ataque. O `MeleeAttackGoal` precisa de `followingTargetEvenIfNotSeen = true`: com `false` o objetivo termina quando o caminho acaba e cancela o golpe que ainda está na antecipação (causa dos golpes que nunca acertavam).

## D-012 — Comportamento coletivo sem módulo próprio

- **Contexto:** o usuário quer um senso de ataque coletivo entre Redcaps. O dossiê original descreve o Redcap como solitário.
- **Decisão:** a coordenação fica dentro de `Redcap` (alerta de alvo, `FlankGoal`, `StayBackGoal`), usando só consultas de entidades próximas em raio de 24 blocos; não há gerenciador de bando nem estado compartilhado. A pedra ignora Redcaps (`canHitEntity`).
- **Consequências:** funciona sem estrutura nova e sobrevive a descarregamento de chunks, mas cada Redcap decide só pelo que vê em volta. Se o raio de alerta ou o cerco se mostrarem caros com muitos Redcaps, a consulta de vizinhos é o ponto a otimizar. A regra de geração solitária do dossiê continua pendente.

## D-013 — Drops feitos no código, itens de combate reaproveitam os do jogo

- **Contexto:** os drops têm chance por utilidade, a arma é a da variante e sai com desgaste aleatório, e a touca muda o comportamento da mob.
- **Decisão:** `Redcap.dropCustomDeathLoot` sorteia os drops (a tabela de saque do tipo fica vazia). A adaga usa `Item.Properties.sword` e o pique `Item.Properties.spear` do jogo, com um material próprio de ferro enferrujado (durabilidade 140), em vez de itens com lógica de ataque própria. A touca usa `humanoidArmor` e a amizade vem de checar o item na cabeça, sem componente nem efeito.
- **Consequências:** looting não aumenta os drops (a tabela de saque padrão não é usada). Mudar chances é editar as constantes `DROP_*`. Por não haver NBT de dono, a "amizade" não é persistente: depende de o jogador estar com a touca na cabeça.

## D-014 — Touca como modelo de item na cabeça, não como armadura

- **Contexto:** a touca como armadura (camada plana de 64x32 sobre o modelo humanoide) ficou esquisita: uma caixa vermelha sem a forma de capuz nem a ponta caída.
- **Decisão:** a touca é um item `Equippable` de cabeça sem asset de armadura, e o jogo desenha o modelo 3D do próprio item na cabeça (`display_context: head`). O modelo usa só caixas alinhadas aos eixos (calota em degraus, abas, nuca, ponta em degraus), sem rotação de elementos, e foi verificado fora do jogo com a mesma cadeia de transformações da camada de cabeça (`CustomHeadLayer`).
- **Consequências:** não há defesa nem durabilidade. A forma é de voxels, não de curvas. Se uma versão futura do jogo mudar a camada de cabeça, é este modelo que precisa ser reajustado.

## D-015 — Bestiário próprio em vez de livro escrito

- **Contexto:** o usuário quer um livro que explique cada mob (lado místico, ataque, onde encontrar) com rascunhos desenhados, e que cresça a cada mob.
- **Decisão:** uma tela própria (`BestiaryScreen`) alimentada por dados (`Bestiary.ENTRIES` e chaves de tradução), em vez de um livro escrito do jogo, porque este só mostra texto. O índice e as páginas são montados a partir da lista de capítulos.
- **Consequências:** acrescentar um mob é acrescentar dados e textos, sem mexer na tela. Todo o texto fica nos arquivos `lang`. A paginação depende da fonte do jogo, então o ajuste fino da quebra de linha só se confirma no jogo.

## D-016 — Praga do Basilisco controlada por vítima no servidor (substituída por D-018)

- **Contexto:** a referência fornecida pede nuvens de cinco segundos, uma barra de 0–100 e dano letal ao completar o acúmulo. Várias nuvens podem se sobrepor e o HUD precisa acompanhar o servidor.
- **Decisão:** usar uma subclasse de `AreaEffectCloud` para detecção/partículas, um temporizador por vítima e attachments NeoForge para dados. O valor persistente é sincronizado somente com o jogador afetado; temporizadores e referências ao autor da névoa são transitórios. Um tipo de dano próprio ignora armadura/escudo, conservando o comportamento do totem.
- **Consequências:** sobreposição não multiplica a taxa, paredes impedem exposição e não há mapas globais retendo jogadores. A barra pode continuar após logout, mas é limpa na morte. O amuleto é segurado em uma mão, sem dependência de acessórios. As raízes usam partículas e imobilização, sem alterar terreno.

## D-017 — Game Tests nativos para integração do Basilisco

- **Contexto:** compilação não comprova o dano, a sobrevivência com totem, o acúmulo de nuvens ou a sequência de IA. O projeto já possui o framework de Game Tests do Minecraft/NeoForge no ambiente de desenvolvimento.
- **Decisão:** registrar testes somente na execução optativa `runBasiliskTest`, com `coraline.basiliskTests=true`, em um mundo separado dentro de `build/`. Não adicionar JUnit ou outra biblioteca.
- **Consequências:** os testes exercitam entidades e eventos reais do servidor e podem ser repetidos sem os mundos do usuário. Renderização, HUD, sons e paginação continuam exigindo revisão no cliente. Referências de API: [Game Tests](https://docs.neoforged.net/docs/misc/gametest/) e [Data Attachments](https://docs.neoforged.net/docs/datastorage/attachments/).

## D-018 — Decomposição vanilla e poção de resistência do Basilisco

- **Contexto:** o usuário removeu a praga e pediu o efeito de corações pretos do Wither no sopro, com perna e olho como drops para fabricar uma poção de resistência.
- **Decisão:** manter a forma e a antecipação do sopro, aplicar `MobEffects.WITHER` e remover barra, attachments, raízes e dano próprio. Registrar somente o efeito benéfico de resistência; a receita gera uma poção vanilla com componentes. A resistência limpa Decomposição atual e impede novas aplicações por três minutos.
- **Consequências:** a névoa não renova efeitos ativos, pois isso reiniciaria o relógio de dano vanilla. Sobreposição não amplifica dano. Drops antigos e suas receitas são substituídos; registros de itens e o ID da nuvem permanecem para compatibilidade com mundos existentes. Sem bibliotecas novas.

## D-019 — Poções do Basilisco no suporte vanilla

- **Contexto:** o usuário elevou o sopro para Decomposição III e pediu fabricação como no Minecraft vanilla, acrescentando rabo aos ingredientes.
- **Decisão:** registrar tipos nativos de poção e misturas sequenciais: estranha → perna → olho → rabo → resistência por três minutos. Redstone estende para oito minutos. A receita anterior de bancada é removida; a parte de preparo da D-018 fica substituída.
- **Consequências:** suporte, combustível, tempo por etapa e processamento de até três frascos são vanilla. Os tipos registrados participam das conversões de recipiente e das abas do criativo automaticamente. Intermediários não concedem efeitos; misturas rejeitam etapas fora de ordem. Testes exercitam o bloco real, além do registro de misturas. Nenhuma dependência adicional.

## D-020 — Basilisco comum em pântanos e cavernas

- **Contexto:** o usuário quer o Basilisco como criatura hostil comum, encontrada com frequência nos pântanos e no subterrâneo, e imagens no Bestiário.
- **Decisão:** adicionar spawn à tag de biomas do Overworld com peso 100 e grupos 4–8; filtrar na entidade para pântanos/manguezais em qualquer luz ou cavernas sem céu, com luz ≤ 7 e cobertura de pelo menos quatro blocos. A categoria continua `MONSTER`, com agrupamento máximo oito. Removidas restrições antigas de três biomas e Y < 0.
- **Consequências:** participa do spawn natural e dos limites de monstros. Peso alto e grupos maiores tornam o mob comum sem um spawner paralelo; não garantem uma contagem fixa por área. Folhas são ignoradas na altura de cobertura. Ilustrações em sépia usam o modelo/poses reais, preservando o estilo do Bestiário, com ferramentas opcionais que não participam dos builds normais.

## D-021 — Litófago: máquina de estados, keyframes nativos e spawn seletivo por caverna

- **Contexto:** implementação do Litófago (`codenamecoraline:lithophage`), criatura mineral subterrânea com despertar progressivo, três ataques (vertical, varredura em arco frontal e projétil de âmbar), 12 animações, ovo próprio, loot e capítulo no Bestiário.
- **Decisão:** manter a arquitetura sem dependências externas (D-009/D-010): modelo articulado em `LithophageModel`, 12 `AnimationDefinition`s em `LithophageAnimations` sincronizadas por eventos de entidade e `SynchedEntityData`, projétil `AmberShard` via `ThrowableItemProjectile`, dois modificadores de bioma separados (`Lush Caves` com peso 28 e `Dripstone Caves` com peso 16) combinados ao filtro `Lithophage.checkSpawnRules` (`Y` entre `-20` e `20`, luz ≤ 7, sem céu e espaço livre de 2,1 blocos), tabela de saque JSON nativa e suíte `LithophageGameTests` (`runLithophageTest`).
- **Consequências:** nenhuma biblioteca externa adicionada; todas as 12 animações, os 7 estados e os 3 ataques funcionam de forma determinística entre servidor e cliente; o Bestiário incorpora o terceiro capítulo mantendo as entradas existentes intactas.

## D-022 — Cobrador da Última Dádiva: ator de trenó temporário e diretor de câmera local no cliente

- **Contexto:** implementação do boss opcional `codenamecoraline:last_gift_collector` com entrada cinematográfica de 9,5 segundos pelo céu em um trenó (`collector_sleigh`), salto para a arena, duas fases de combate, quatro ataques coreografados, conquista e capítulo no Bestiário.
- **Decisão:** separar o trenó aéreo em uma entidade leve não-combatente (`CollectorSleigh`, `noSave()`, descartada após a passagem) sincronizada com os subestados de `LastGiftCollector` (`IntroStage`), e implementar o controle de câmera/HUD no cliente via eventos nativos do NeoForge (`ViewportEvent.ComputeCameraAngles`, `ViewportEvent.ComputeFov`, `MovementInputUpdateEvent`, `RenderGuiEvent.Post`, `RegisterKeyMappingsEvent`) em `CollectorCinematicDirector`. A tecla de pulo (`key.codenamecoraline.skip_cinematic`) atua localmente no cliente, liberando câmera e movimento sem alterar o estado do servidor nem interromper outros jogadores em multiplayer.
- **Consequências:** zero dependências externas; proteção completa contra travamento de câmera (em dano, morte, troca de dimensão, desconexão ou skip); validação automatizada de ritual, cinemática, ataques, transição de fase, loot e conquista em `LastGiftCollectorGameTests` (`runLastGiftCollectorTest`).

## D-023 — Cobrador renegado: cinco estágios, ataques imprevisíveis e visual de Papai Noel renegado

**Contexto.** O Cobrador original tinha duas fases, quatro ataques de ritmo fixo e visual de "mantel vinho com máscara e foice"; o retorno foi: previsível, pouco espalhafatoso, fácil e fora da referência (Nicholas the Renegade).

**Decisão.** (1) Cinco estágios por vida (80/60/40/20%): cada limiar abre uma transição de 60 ticks e uma "presença assustadora" (fraqueza + escuridão); quedas que cruzam vários limiares vão direto ao estágio correspondente. (2) Cinco ataques novos: Pisada (anel), Giro do Machado (estágio 2+), Olhar Hipnótico (2+, só atinge quem olha), Convocação Feérica (3+, até 4 redcaps) e Cone de Gelo (4+, congela); estágio 5 soma 4 de dano a cada golpe e encurta recuperações. (3) Escolha ponderada sem repetir o ataque anterior, preparo com ±jitter, combos que pulam a recuperação (chance cresce por estágio), investida "inquieta" a longa distância e pisada de reação contra quem fica colado durante a recuperação. (4) Vida 420, armadura 10 (+4 de resistência), danos ~50% maiores. (5) Modelo reaproveita os nomes de peças (animações intactas): gorro com pompom, rosto azul-acinzentado gritando, barba enorme, casaco bordô com pelo, saco com caveira e machado enorme. Eventos de entidade passam a 100–116 (o 60 colidia com o "poof" de morte do vanilla).

**Consequências.** Os testes foram atualizados para os novos números (420 PV, armadura 10, dano 11) e há três testes novos de estágios, Pisada, Cone e Giro. O Olhar Hipnótico e a escalada de dano do estágio 5 não têm teste automático. A sensação de dificuldade e o visual em jogo precisam de validação manual (`runClient`).

## D-024 — Cobrador: machado de verdade, magia com rastro, Presentes Famintos, Fúria e presente final

**Contexto.** Segundo retorno: animações de ataque não respeitavam o machado, redcaps como lacaios eram incoerentes (e se voltavam contra o chefe), faltava nariz longo, animações fracas, ele afundava e congelava na neve em pó, IA fraca, feitiços sem rastro, e faltavam um estágio de fúria, uma morte temática e história nas conquistas.

**Decisão.** (1) Animações reescritas a partir de poses de referência validadas por renderização offline (ver `LastGiftCollectorAnimations`): carregar sobre o ombro, golpe vertical, corte diagonal, giro, pisada com o machado plantado, conjuração com o machado erguido, recuperação apoiado no cabo, rugido. (2) Lacaios: **Presente Faminto** (`gift_mimic`), caixa de presente com pernas e tampa-mandíbula; só ataca jogadores, ignora o Cobrador, tem fusível e estoura. Substitui os redcaps. (3) Magia com rastro via "bolts" no servidor: onda crescente (machado), cometas teleguiados e presentes que caem sobre áreas marcadas; todos desenham a trajetória com partículas contínuas. (4) IA: escolha de alvo (quem o feriu, senão o mais próximo, com histerese), mira com antecipação do movimento, detecção de kiting (responde com magia), de pilar/altura, de travamento (blink de gelo), espaçamento e circulação entre ataques. (5) Estágio 6, **Fúria** (≤10% de vida): velocidade, armadura e dano amplificados (x1,5), cooldowns em dobro de velocidade, preparos e recuperações menores. (6) Imune a congelamento e a neve em pó (tags de entidade + `canFreeze`), e desenterra-se se ficar preso em blocos. (7) Morte: o corpo é "embrulhado" e cai um presente de Natal (`collector_gift`) que se abre sobre um baú com todo o saque, sempre incluindo o **Machado Enorme do Renegado**. (8) Seis conquistas contam a história (`bosses/silent_bell` → `the_summoning` → `the_final_delivery` → `the_cracked_bell` / `the_lost_axe` → `the_solstice_seal`), todas com o rosto do chefe (`collector_portrait`) como ícone.

**Consequências.** O estágio 3 invoca Presentes Famintos já na transição. Há 15 GameTests do Cobrador. O visual em jogo (animações, partículas, o presente se abrindo) e a sensação da IA ainda precisam de validação manual com `runClient`.

## D-025 — Machado Enorme do Renegado: ícone novo, modelo 3D na mão e invocação de aliados

**Contexto.** O ícone do machado estava fraco, o modelo na mão era o sprite chato de sempre, e o machado não tinha identidade além do dano.

**Decisão.** (1) Ícone 16×16 novo, desenhado por código com contorno automático (`tools/generate_renegade_greataxe.py`). (2) Modelo 3D de caixas (`models/item/renegade_greataxe_3d.json`, textura de paleta 16×16) com a mesma anatomia do machado do chefe: cabo, empunhadura, colar dourado, lâmina com gume chanfrado, runas de gelo e espigão. É girado 45° como um sprite de ferramenta, para que os `display` padrão de `handheld` sirvam; ocupa cerca do dobro do tamanho de uma ferramenta. O item escolhe por `display_context`: ícone no inventário, no chão e em molduras; 3D na mão (mesmo padrão da lança). (3) Botão direito chama 3 **Presentes Famintos aliados** (`RenegadeGreataxeItem`): gasta 250 de durabilidade, recarga de 25 s, no máximo 4 aliados por jogador, e o machado nunca quebra ao chamar (recusa quando gasto demais). Os aliados seguem o dono, atacam mobs hostis e o Cobrador, nunca jogadores, não se ferem com o dono, estouram após 30 s ou ao fim do fusível causando 9 de dano só a inimigos (com crédito de morte para o dono).

**Consequências.** `GiftMimic` ganhou dono (UUID, salvo em NBT) e lado (aliado/hostil). As regras de quem ataca quem estão em `canAttack`. A orientação do modelo 3D na mão foi conferida só por uma projeção matemática; precisa de `runClient` para ajustar `display` (rotação, translação e escala) se parecer torto. O caminho `use` do item não tem teste automático (sem jogador simulado); as regras de aliança e o estouro têm (`collector_greataxe_allied_mimics`).

## D-026 — Nome na história e o machado nas mãos do chefe

**Decisão.** Na história o chefe se chama **Nicolas Noel, o Renegado** (epíteto: "o Cobrador da Última Dádiva"). Nome de exibição, barra de chefe, título da cinemática, ovo, trenó, presente, legendas, Bestiário e conquistas foram atualizados em pt_br e en_us; os IDs (`last_gift_collector` etc.) não mudam. O machado que ele empunha agora é o **mesmo Machado Enorme do Renegado** do item: o modelo do chefe usa as mesmas peças e materiais (cabo, empunhadura, pomo, colar dourado, lâmina, gume, ponta, chifre, espigão, runas de gelo emissivas), em escala 1,8×. Ao invocar Presentes Famintos ele também usa o poder do machado: as runas dele faíscam no momento do chamado.

**Consequências.** A arte do machado (`tools/generate_last_gift_collector_assets.py`) e a do item (`tools/generate_renegade_greataxe.py`) devem ser mantidas em sintonia. O visual em jogo ainda precisa de `runClient`.

## D-027 — Barras de vida temáticas para todo boss

**Decisão.** Bosses não usam a barra padrão. O evento `CustomizeGuiOverlayEvent.BossEventProgress` é cancelado para bosses com estilo registrado em `ThemedBossBars`, identificados pela chave de tradução do nome (o servidor já a envia no evento do boss). O estilo de Nicolas Noel é uma moldura de 256×48 (galhadas, trilha dourada, medalhão com o rosto) com preenchimento desenhado por código, cor por estágio (a cor da barra enviada pelo servidor), brilho animado, marcas de estágio e brilho pulsante na Fúria. Virou regra em `CLAUDE.md`.

**Consequências.** Um boss com nome personalizado (etiqueta) perde o estilo e volta à barra padrão, porque o nome deixa de ser a chave de tradução. O visual só pode ser validado com `runClient`; a prévia foi feita offline.

**Ajuste D-025 (aliados ajudam o dono contra qualquer coisa).** Os Presentes Famintos aliados só atacavam mobs hostis, então ignoravam animais mesmo quando o dono os atacava. Agora, a cada 8 ticks, eles adotam como alvo quem feriu o dono e, senão, o que o dono acertou por último (até 8 s atrás, qualquer criatura, animais incluídos), além de continuarem atacando mobs hostis por iniciativa própria. Continuam fora de alvo: o dono, qualquer jogador, outros aliados e os pets domesticados do dono. O estouro final fere hostis e o alvo atual/foco do dono. Teste: `collector_allies_help_owner_against_anything`.

## D-028 — O Quebra-Copas: mobilidade arbórea executada pelo servidor e animação decidida no servidor

**Contexto.** O boss precisava escalar, atravessar copas e atacar de cima sem parecer um mob que "voa" ou se teletransporta, e com animações que sempre correspondam ao que a mecânica faz.

**Decisão.** (1) A animação é escolhida no servidor e sincronizada (`DATA_ANIM`); o cliente só a toca. Cada estado de jogo tem animação própria (88 no total, catalogadas em `CanopyAnim` com estado, evento, interrupção e próxima). (2) A escalada usa `noPhysics` com hitbox reduzida (`Pose.CROUCHING`, 1,0 × 2,2) e **cada passo é validado** contra blocos sólidos (folhas passam, troncos não); a cada tick o tronco ao lado precisa existir, senão o boss perde o apoio, cai e se recupera. (3) Os saltos são balísticos: `TreeNav.planJump` procura o menor tempo de voo cuja velocidade é permitida e cuja trajetória, em meios ticks e com a caixa real, está livre; o plano é refeito durante a preparação e um destino inválido cancela o salto e faz o boss descer pelo tronco. Em voo a velocidade horizontal é corrigida rumo ao pouso. (4) Não usamos biblioteca de animação nem sons externos (`.ogg`): os sons são eventos vanilla mapeados em `sounds.json`. (5) A barra de vida temática, as gravuras do Bestiário e os ícones são gerados por scripts versionados em `tools/`.

**Consequências.** Saltos práticos de até cerca de 10 a 12 blocos; copas muito densas ao redor do pouso podem impedir o plano (o boss luta no chão). Os GameTests constroem os próprios terrenos e precisam forçar o carregamento de todos os chunks do terreno: mobs só executam IA em chunks que processam entidades, e uma arena maior que os 12 × 12 do modelo deixava o boss parado em chunks vizinhos. A aparência e a leitura das animações em movimento só podem ser julgadas com `runClient`.


**Ajuste D-029 (Quebra-Copas mais duro e mais movel; porrete 3D).** O chefe era facil e parado demais. Vida 92 -> 200 (acima dos 128 de Noel), armadura 5 -> 8, resistencia a repulsao 0,75, velocidade 0,29 -> 0,33, recargas de ataque menores e movimento continuo no chao: galope em campo aberto, arco largo de 55 graus ao redor do alvo entre golpes (lado alterna a cada ~2,5 s) e pressao constante quando o golpe esta pronto mas fora da janela. O modelo 3D do porrete nao carregava ('to' specifier exceeds the allowed boundaries: a caixa chegava a y = 36,5, e o limite do Minecraft e [-16, 32]); agora cabe nos limites e a escala de exibicao em terceira pessoa e 1,2, igual a escala do modelo do chefe, para ter o mesmo tamanho.

## D-029 — Lacaios do Quebra-Copas: Micos de Casca como extensão do boss

**Contexto.** O boss ganha aliados que controlam o terreno e criam aberturas, sem virar uma luta de vários bosses nem deixar mobs agressivos órfãos.

**Decisão.** (1) Primeiro só o **Mico de Casca**, por reforçar a mobilidade entre árvores; o Javali de Raiz fica como segunda camada, adiada até haver evidência de jogo de que a luta precisa de pressão terrestre. (2) O lacaio é um mob comum com vínculo explícito (UUID do boss), só tem jogadores como alvo e é imune aos golpes do boss e dos irmãos; o boss exclui lacaios dos seus golpes. (3) O boss decide os chamados: dois na abertura, um por transição de estágio, limite duro de quatro simultâneos contando os vivos e os pendentes, sem reaparecimento. (4) Cada chamado é anunciado (folhas, grito, rugido) antes de entrar. (5) Os lacaios saem com a morte ou a remoção do boss; uma regra de órfão cobre os demais casos. (6) Mobilidade física: salto balístico de velocidade única, rota de tronco com passos validados e escalada de paredes do jogo; sem teletransporte.

**Consequências.** O reforço do estágio 2 é hoje um Mico, não um Javali. O limite de quatro, a vida (14), a mordida (3) e as recargas são valores iniciais a calibrar jogando. Os GameTests precisam de um jogador mock danificável (`makeMockPlayer(GameType.SURVIVAL)`), porque o jogador de servidor mock é invulnerável até o cliente carregar.

## D-030 — Invocações que não se atacam, voo do porrete, intro do Quebra-Copas e espírito na morte

**Contexto.** Micos e Presentes aliados do mesmo jogador brigavam entre si (os dois são `Enemy`). O porrete não avançava: o mergulho automático cortava 75% do embalo assim que o jogador começava a cair. O Quebra-Copas não tinha intro no padrão do Cobrador, e as mortes dos bosses não tinham um fecho visual.

**Decisão.** (1) Interface `PlayerSummon`: invocações de jogador formam um lado só; nunca se miram, nunca se ferem, e o golpe do porrete as poupa. (2) O porrete puxa na direção exata do olhar com empuxo sustentado (estilo martelo arremessado); o mergulho passou a ser opcional, com Shift no ar. (3) Intro de 6 s com batida no peito, rugido e diretor de câmera próprio, reaproveitando a tecla e o formato do Cobrador. (4) A morte libera um espírito: o modelo do boss translúcido e azul subindo e se apagando, como entidade visual com renderizador próprio por boss.

**Consequências.** O espírito é desenhado com `entityTranslucent` e só pode ser julgado no `runClient`, assim como a câmera e os tremores da intro. Os GameTests da intro do Quebra-Copas e dos lacaios aguardam o fim da intro de 120 ticks.

## D-031 — Dragão companheiro com ombro sem substituição da entidade

**Contexto.** O Dragão Bebê precisa andar, voar, orbitar e pousar no ombro preservando a mesma criatura. Na API 26.1.2, `ShoulderRidingEntity` salva NBT no jogador e descarta o papagaio; a renderização vanilla do ombro é específica para papagaios. O requisito do dragão exige preservar a entidade ativa, além dos dados.

**Decisão.** Usar `TamableAnimal` para o vínculo UUID e comandos persistentes, um único `DragonBrain` para arbitrar movimentos/defesa e navegações terrestre/aérea nativas com controle de velocidade gradual. No ombro, a entidade continua salva e rastreada no mundo, presa a uma âncora validada junto ao dono; o mesmo renderer usa escala menor. Não transformar em NBT do jogador, passageiro ou entidade substituta. Ocultar apenas o próprio dragão no ombro em primeira pessoa, preservando a visão do dono. A saída valida espaço e conserva identidade. O projétil é próprio, derivado de `ThrowableItemProjectile`, com dano direto e sem rotinas de incêndio/explosão.

**Consequências.** Não há duplicação por reconstrução de snapshots nem necessidade de uma camada especial de jogador. A entidade deixa o ombro quando o dono deixa de estar disponível no mesmo mundo; não implementa transferência automática entre dimensões. `PlayerSummon` reaproveita a regra de aliados dos Micos e Presentes. Arte usa modelo nativo, 12 animações por keyframe, atlas procedural reproduzível e gravuras exportadas da geometria real. Nenhuma dependência adicional.

## D-031 — Golpes de gorila, guarda quebrada e salto de chão do Quebra-Copas

**Contexto.** Feedback de jogo: o porrete estava inclinado para o próprio corpo, um escudo bastava para vencê-lo, ele só batia com o porrete, não subia em árvores e quase não pulava. Uma sessão anterior marcou tudo como concluído com mudanças rasas (ver a correção em `docs/CANOPY_BREAKER_PROGRESS.md`).

**Decisão.** (1) A geometria do porrete foi corrigida na origem: a mão segura a ponta do cabo e a cabeça fica na outra extremidade, apontando para fora; as poses não precisam mais de remendos. (2) Todo golpe do boss quebra a guarda de quem bloqueia (recarga de 5 s no escudo, jogador abaixado); o Agarrão não é bloqueável e é o golpe preferido contra quem se defende. (3) Agarrar e Arremessar é um agarrão real: a vítima vira passageira dele, é erguida e lançada para frente; Soco no Chão é uma onda de choque que joga todos para o alto; ambos têm animações próprias. (4) Acrobacia: salto balístico de chão (mesmo planejador das árvores), também contra alvos em pilares fora de alcance, no lugar de um pulinho periódico; flanco por árvores desde o estágio 1. (5) O antigo `SHIELD_BREAK`, que nunca era usado, foi removido.

**Consequências.** O boss ficou mais letal para quem se esconde atrás de escudo ou de um pilar; os valores (dano 3+8 do agarrão, 9 do soco, recargas 110 e 90, raio 4) são pontos de partida a calibrar jogando. Nada disso foi visto em jogo: animações em movimento, sensação do arremesso e da subida em florestas reais dependem do `runClient`.

## D-032 — Comandante Carmesim: duelista de servidor, modelo em tamanho real e Sentinelas no mesmo modelo

**Contexto.** Ficha de um boss humanoide de 3,5 blocos com espada larga, guarda com riposta, investidas, golpe aéreo e uma onda à distância, três estágios, lacaios e uma arma de recompensa com habilidade, seguindo as regras de qualidade de IA, animação e testes dos bosses anteriores.

**Decisão.** (1) Tudo que tem efeito de jogo é decidido no servidor (`CrimsonCommander`), com estados (`Activity`) e ataques em fases (`Attack`/`Phase`); o cliente só toca a animação sincronizada. (2) A Onda de Lâmina Sanguínea é simulada pelo próprio boss (lista de ondas com posição, direção e alvos já atingidos, avançada a cada tick em subpassos com colisão de blocos), não por uma entidade de projétil: não precisa de renderizador, não é salva e o rastro é interpolado por partículas. (3) A guarda usa o gancho vanilla de deflexão (`deflection` → `ProjectileDeflection.REVERSE`) para projéteis e redução de 75% em `hurtServer` para o resto; golpe corpo a corpo frontal durante a guarda vira Riposta. (4) O modelo é desenhado em tamanho real (sem escala no renderizador), com a espada apontando para -z a partir do punho; as poses de referência são documentadas com seus ângulos e conferidas por um teste que mede a geometria posada. (5) As Sentinelas de Cinza usam o mesmo modelo e o mesmo atlas (gerados juntos, paletas diferentes), escalados e sem penacho, capa e espinhos, e reaproveitam as animações do Corte. (6) A Investida só é lançada se houver chão contínuo até o alvo; sem caminho, ele usa a onda ou salta. (7) O combo da Fúria é determinístico (Investida que acerta emenda no Golpe Pesado), para ser testável e legível. (8) A Espada Larga é entregue uma vez na cinemática da morte, fora da loot table, que fica com fragmentos e a insígnia.

**Consequências.** O boss não depende de entidades auxiliares além das Sentinelas e do espírito. Valores de dano e recarga são os da ficha e podem precisar de calibração jogando. A primeira Insígnia só sai de um Comandante natural (raro) ou do modo criativo. Visual, partículas e sons dependem do `runClient` para validação.


## D-033 — Bruxa dos Dez Fios: boss composto sequencial numa só classe de marionete

**Contexto.** A ficha pede um boss que reúne onze criaturas (a Matriarca e dez marionetes com atributos, tamanhos, voo e três ataques cada), lutadas uma por vez, com um fio cortável antes de cada ataque especial, arena própria, anti-cheese e uma arma de recompensa com três posturas. Onze classes de entidade com onze IAs, onze renderizadores e onze animadores seriam cerca de dez vezes o código a testar e a manter.

**Decisão.** (1) Existe **uma** classe `PuppetEntity` para as dez marionetes (dez tipos de entidade registrados sobre ela, `PuppetKind` como tabela) e **um** `AttackRunner` que implementa as fases e os 13 estilos de ataque para as 11 criaturas; um ataque novo é uma linha de dados (`PuppetAttack`), não uma classe. (2) A **Matriarca é o diretor**: só ela decide estágio, quem acorda, invulnerabilidade, arena, puxada e reset; as marionetes só lutam e avisam (`onPuppetDefeated`). (3) Só uma marionete luta por vez e a Matriarca é intocável (`hurtServer` falso) até o estágio 11: o jogador sempre enfrenta uma coisa só, e não há combinação de estados a testar. (4) Aliados nunca se ferem: `TenThreadsEncounterMember` marca todos os membros e `AttackRunner.isFoe` os exclui; os alvos são jogadores (não espectadores) e golems de ferro, o mesmo critério dos bosses anteriores. (5) O fio carmesim e o estado ENROSCADA são a única mecânica de contrajogada comum; os pontos fracos por marionete são multiplicadores num só método (`damageMultiplier`). (6) Os dez corpos pendem dos fios desde o `INTRO` (`suspend`) e são puxados na vez (`awaken`); se uma marionete some, a mesma é recriada, sem pular estágio. (7) A arena só prende quem entrou nela e a limpeza da Matriarca só remove os hitboxes dos **seus** fios: dois encontros (ou dois testes) a menos de 80 blocos não interferem. (8) O anti-pilar tem preferência sobre o anti-buraco quando o alvo está alto e inalcançável (içar a marionete até o pilar anularia a puxada).

**Consequências.** Todas as criaturas compartilham comportamento, bugs e correções; um ajuste no `AttackRunner` vale para as onze (por isso o salto passou a ser guiado até o ponto travado). Mais marionetes custam uma entrada em `PuppetKind`, um modelo e um som. O balanceamento é feito pela tabela. A IA é genérica por estilo: nenhuma marionete tem comportamento único fora de `damageMultiplier` e de poucos casos em `PuppetEntity` (rachadura do cão, cura da Mãe na água, folhas do Cervo, enraizamento).

## D-034 — O fio da marionete é uma camada de render em linhas, sem biblioteca de animação

**Contexto.** O fio que liga cada marionete ao cajado da Matriarca precisa existir no cliente, mudar de cor (prata → carmesim tenso) e acompanhar o corpo animado, sem entidade extra por fio. Um mod de animação (GeckoLib e similares) resolveria ossos e fios, mas traria uma dependência nova.

**Decisão.** O fio é desenhado por `PuppetThreadRenderLayer`, uma `RenderLayer` que, a partir do estado de render (`threadAnchor` da Matriarca e se o fio está carmesim), submete linhas (`RenderTypes.lines()`) ligando as costas da marionete ao ponto de onde os fios saem do cajado. As animações dos modelos usam o `KeyframeAnimation` do próprio jogo, amostrado de uma função por papel da parte (`PuppetAnimations`). Nenhuma dependência nova (regra do projeto: nada sem finalidade concreta registrada aqui).

**Consequências.** O fio é uma reta (ou levemente curva, pela amostragem), não uma corda com física; em contrapartida não custa entidades, não é salvo e sempre sabe a cor do estado. O ponto de partida do fio é o do servidor (`getThreadAnchor`), então o fio do cliente e o hitbox cortável coincidem. A aparência só se confirma no `runClient`.

## D-035 — Animação da Matriarca em camadas compostas por articulação, sobre a animação nativa

**Contexto.** A especificação de animação da Matriarca (2026-11-05) exige animações simultâneas (andar e manipular, flutuar e mexer os dedos), prioridade por articulação, cross-fade, interrupções que liberam só as articulações necessárias, eventos sincronizados e sem duplicação em multiplayer, sem GeckoLib e sem bibliotecas novas. A API 26.1 (`KeyframeAnimation.apply`) só aplica uma animação inteira, somando, com um peso único.

**Decisão.** Os clipes continuam sendo `AnimationDefinition` vanilla (keyframes e interpolações nativas, mais curvas próprias implementadas como `AnimationChannel.Interpolation`). Eles são amostrados com o mesmo algoritmo do vanilla, mas num buffer por camada, e um compositor (`PoseMixer`) resolve cada canal por prioridade (empate pelo rank fixo da camada): substituição ou soma. Cinco camadas são decididas no servidor (`MatriarchAnimDirector`) e sincronizadas por `SynchedEntityData`; locomoção, respiração e as camadas procedurais (olhar, dedos, tecido e cabelo) são derivadas no cliente do que ele já sabe (movimento, estágio, vida). Os eventos ficam no catálogo e só o servidor os dispara.

**Consequências.** Qualquer combinação compatível coexiste sem regra especial; a ordem do código não muda o resultado (testado). O custo é um passo de composição por quadro (≈ 60 articulações × poucas camadas). O sistema é específico da Matriarca; as marionetes continuam em `PuppetAnimations`.

## D-036 — Rig da Matriarca ampliado (dedos, rosto, cabelo, capa em painéis)

**Contexto.** O rig anterior não tinha cotovelo, pulso, dedos, boca, sobrancelhas, mechas nem painéis separados, e quadril e tronco eram a mesma parte. A especificação pede animações dessas articulações e manda propor a alteração mínima quando faltarem.

**Decisão.** No gerador, acrescentar articulações sem mudar silhueta nem materiais: `chest` separado do quadril; braço em ombro, cotovelo e pulso; cinco dedos por mão (caixas de 0,35–0,45 px, curvatura de repouso por rotação de parte); pés; sobrancelhas, boca e cantos da boca como partes (tirados da pintura do rosto); duas mechas de dois segmentos; capa em painel de trás, barra e laterais; ponta do capuz. O cajado continua no antebraço direito e o modelo o mantém em pé (compensação do pulso).

**Consequências.** 56 partes (antes 23); o atlas usa 51/256 linhas. Detalhes de 0,2–0,5 px podem ler pouco à distância (a calibrar no jogo).

## D-037 — A última fase ganha um repertório de golpes e uma regra de derrubada

**Contexto.** O catálogo pede ataques rápido, pesado, de uma e duas mãos, sobrenatural, de alcance, especial, finalização de combo, ataques de fio, defesas, esquivas e quedas; a fúria tinha três ataques e nenhuma defesa ou derrubada. Uma animação sem gatilho no jogo nunca seria vista.

**Decisão.** Sem mexer no `AttackRunner`, a lista `FURY_ATTACKS` passou a ter 20 ataques (`MatriarchMoveset`, seis só por combo ou contra-ataque) e uma tabela de combos com condições do alvo. Na fúria: defesa contra jogador (aparar anula, braços cruzados reduzem à metade, barreira ou esquiva contra projétil; 18 % + 14 % × estranheza, recarga 100 t); salto para trás quando a encurralam; golpe ≥ 12 + 4 × estranheza a derruba (26 t no chão; no máximo uma derrubada a cada 240 t); golpe ≥ 6 na preparação interrompe e ≥ 9 a faz cambalear (recarga comum de 80 t, sem trava de atordoamento); depois de cada sequência de golpes ela respira de 46 t (vida cheia) a 32 t (perto da morte). Nos estágios ela continua intocável; as defesas são só visuais.

**Consequências.** Medido contra um alvo parado: sem a pausa o dano por segundo dobrava (4,1 contra 2,2 da fúria original); com ela fica em 1,1× com vida cheia e 1,35× perto da morte, com mais variedade de golpes (ver `design/matriarch_animation.md` §9). A sensação contra um jogador que se move ainda precisa ser avaliada em jogo. Os testes da Bruxa (30) continuam passando.

## D-038 — Poses de braço próprias por extensão de enum (`enumextensions.json`)

**Contexto.** O aparo da Cimitarra do Arquiteto precisa de uma pose de braço em terceira pessoa que siga a linha do tempo do uso (erguer, janela com tremor, baixar) e de uma pose curta de contragolpe. `HumanoidModel.ArmPose` é um enum vanilla; nenhuma das poses existentes (bloqueio, lança, arco) tem esse desenho, e o projeto não usa biblioteca de animação (D-009).

**Decisão.** Usar o mecanismo oficial do NeoForge: `ArmPose` implementa `IExtensibleEnum`, então `META-INF/enumextensions.json` (declarado em `neoforge.mods.toml` com `enumExtensions`) acrescenta `CODENAMECORALINE_SCIMITAR_PARRY` e `CODENAMECORALINE_SCIMITAR_COUNTER`, cada uma com um `IArmPoseTransformer` (`client/architect/ScimitarArmPoses`). A primeira pessoa usa `IClientItemExtensions.applyForgeHandTransform`. O cliente reconhece o aparo pela flag de `CUSTOM_MODEL_DATA` que o servidor grava no item, sem pacote de rede novo.

**Consequências.** É a primeira extensão de enum do mod: novas poses de item seguem o mesmo arquivo. Os nomes precisam do prefixo do mod id (o carregador recusa o contrário). As classes referenciadas são só do cliente e só carregam lá. A aparência das poses só é validada no `runClient`.
