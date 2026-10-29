# Decisões arquiteturais

Registro apenas de decisões relevantes: contexto, decisão e consequências. Não é diário de mudanças (isso é o Git).

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
