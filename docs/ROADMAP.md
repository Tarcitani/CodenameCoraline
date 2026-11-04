# Roadmap técnico

Estado da fundação, verificações realizadas e pendências. Não contém escopo criativo do mod: ele ainda não foi definido.

## Estado inicial (fundação)

- [x] Diretório `C:\MinecraftDev\CodenameCoraline` criado; estava vazio, sem repositório Git.
- [x] Projeto Gradle com NeoForge 26.1.2 (ver `docs/DECISIONS.md`, D-001).
- [x] Gradle Wrapper copiado do template oficial `NeoForgeMDKs/MDK-26.1.2-ModDevGradle`.
- [x] Ponto de entrada `com.codenamecoraline.CodenameCoraline` e metadados `neoforge.mods.toml`.
- [x] Documentação: `README.md`, `CLAUDE.md`, `docs/ARCHITECTURE.md`, `docs/DEVELOPMENT.md`, `docs/DECISIONS.md`, `docs/ROADMAP.md`.
- [x] Repositório Git local inicializado (branch `main`).
- [x] Repositório remoto privado `https://github.com/Tarcitani/CodenameCoraline` criado; `origin/main` em `658bfe9`.
- [x] Compilação `./gradlew build` executada com sucesso em 2026-10-27 — ver verificações abaixo.

## Verificações realizadas

| Data | Verificação | Resultado |
|---|---|---|
| 2026-10-27 | Java do sistema | Temurin 21.0.12 (`JAVA_HOME` definido). Java 25 ausente. |
| 2026-10-27 | Git | 2.55.0 disponível |
| 2026-10-27 | GitHub CLI | 2.103.0, autenticado como `Tarcitani` (escopos `repo`, `gist`, `read:org`) |
| 2026-10-27 | Nome `CodenameCoraline` na conta `Tarcitani` | Não existe (consulta via `gh repo view`) |
| 2026-10-27 | Conectividade com Maven do NeoForge, Gradle e GitHub API | Respondendo (HTTP 200) |
| 2026-10-27 | Versões NeoForge 26.1.2.124 e plugin ModDevGradle 2.0.148 | Presentes nos metadados Maven |
| 2026-10-27 | `gradlew build` via Git Bash | Falhou: `Unable to establish loopback connection` |
| 2026-10-27 | `gradlew.bat build` via PowerShell/cmd | Falhou com a mesma causa |
| 2026-10-27 | `gradlew compileJava --stacktrace` com `JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true` | Falhou; causa raiz: `Pipe` do JVM (`Invalid argument: connect`) |
| 2026-10-27 | `java.nio.channels.Pipe.open()` em programa Java simples (JDK 21, mesmo shell) | Funciona |
| 2026-10-27 | `./gradlew build` no terminal do usuário (fora do sandbox do agente) | **Sucesso**: `BUILD SUCCESSFUL` em `build.log`; jar gerado às 15:47; classe em Java 25; JDK 25 provisionado em `%USERPROFILE%\.gradle\jdks` |
| 2026-10-27 | `./gradlew runClient` (executado pelo usuário) | **Sucesso**: janela abriu; `codenamecoraline loaded`; encerramento limpo |
| 2026-10-27 | `./gradlew runServer` (executado pelo usuário) | **Sucesso**: `codenamecoraline loaded`; `Done (2.315s)` |
| 2026-10-27 | Auditoria Git (`git add -n`, varredura por padrões de segredo) | Nenhum segredo encontrado; `.omc/` ignorado; `gradle-wrapper.jar` versionável |

## Pendências

1. ~~**Loopback no sandbox do agente.**~~ Falha histórica do ambiente anterior; `build`, `runBasiliskTest` e cliente de inspeção executados pelo agente no workspace atual em 2026-10-27. Causa original não identificada.
2. **Autorização para o JDK 25.** O plugin foojay baixa o JDK 25 para `%USERPROFILE%\.gradle\jdks`. Isso depende de acesso a `api.adoptium.net`/`foojay` e foi autorizado pelo usuário.
3. ~~**Execução em jogo.**~~ Concluída em 2026-10-27: `runClient` e `runServer` executados com o mod carregado (ver verificações).
4. **Licença.** Definir a licença real antes de publicação aberta (provisório: All Rights Reserved).
5. ~~**Group ID e nome do pacote.**~~ Mantidos como `com.codenamecoraline` (D-008).
6. **Escopo criativo.** Tema inspirador definido: folclore feérico tradicional. Primeiro candidato: Redcap, proposta em `docs/design/redcap.md`. Primeira fatia escolhida: só a entidade.
   - Escrito: entidade com atributos e IA de perseguição, modelo do corpo seguindo o dossiê, camada dos olhos emissivos, lang en_us/pt_br.
   - Verificado em 2026-10-27: as 7 classes Java compilam com `javac` do JDK 25 contra o Minecraft/NeoForge 26.1.2 em cache (corrigido o import de `EntityRenderersEvent`, que fica em `net.neoforged.neoforge.client.event`). Depois disso, `gradlew build` e `runClient` passaram (abaixo).
   - Aparência refeita em 2026-10-27 a partir de uma arte de referência do usuário: corpo encurvado, cabeça projetada à frente, orelhas grandes com argolas de ouro, capuz carmesim com ponta caída para trás, barba grisalha, presas, braços longos com garras e braçadeiras de couro, pernas dobradas com botas de ferro enferrujado, tanga esfarrapada e pique com lâmina larga ensanguentada na mão esquerda. `RedcapModel` passou a ser um `EntityModel` próprio (o `HumanoidModel` reposicionava os braços durante o ataque), com renderizador `MobRenderer`; `Redcap.getMainArm()` retorna `LEFT`, então o golpe usa o braço do pique. Texturas 128x64 (ver `docs/DEVELOPMENT.md`).
   - Diferenças em relação à arte de referência, para seguir o dossiê: pique em vez de faca; botas de ferro em vez de pés enfaixados.
   - Validado em 2026-10-27 pelo usuário: `.\gradlew.bat build` e `runClient`; `/summon codenamecoraline:redcap` funciona. Prints em jogo mostram modelo e texturas carregados (sem textura ausente), olhos emissivos, pique na mão esquerda e o Redcap acompanhando o jogador com a cabeça. Log sem erros ou avisos do mod e sem crash report.
   - Feedback do usuário (2026-10-27): ataque "com as mãos", idle simples demais e sem variações, caminhada genérica, sem som. Refeito no mesmo dia:
     - estocada com o pique em duas mãos e dano sincronizado ao golpe (5 ticks de antecipação);
     - caminhada própria;
     - respiração, orelhas tremendo e piscar;
     - quatro variações de idle sorteadas: farejar, olhar em volta, afiar a lâmina e conferir o gorro;
     - oito sons com legendas (`docs/DECISIONS.md` D-009 e D-010).
   - Verificado fora do jogo: compilação com `javac` e quadros das animações renderizados com o `setupAnim` real (posição do pique e da mão conferidas numericamente). Todos os 29 áudios referenciados existem no índice de assets do 26.1.2.
   - Segundo ajuste (2026-10-27): o pique agora herda o ângulo da mão e do antebraço (sem rotação própria na pose base; antes era contra-rotacionado para ficar vertical), e os keyframes da estocada e de afiar foram resolvidos de novo para essa pose. O golpe errava porque o dano só valia se o alvo ainda estivesse no alcance curto de melee 5 ticks depois do início; agora o Redcap para e mira durante a antecipação e o golpe acerta se o alvo estiver a até 2,2 blocos da hitbox dele (`PIKE_REACH`).
   - Terceiro ajuste (2026-10-27): pique inclinado para cima e para a frente (a lâmina apontava para trás); golpes que nunca acertavam corrigidos (causa em `docs/DECISIONS.md` D-011); ataque com uma mão só; três variantes (lança, adaga, garras com pedras) e ovos de geração com aba própria no criativo. Compilação com `javac`, UVs sem conflito, alcance das armas medido (ponta da lança 2,1 blocos, adaga 1,6, garras 1,0 do centro).
   - Quarto ajuste (2026-10-27): comportamento em grupo (alerta, cerco, apoio das garras; D-012); a pedra virou uma pedrinha animada (item `redcap_pebble`, 8 quadros) com mira balística e sem fogo amigo (o solver foi testado contra a física do projétil: erro de altura 0 em 63 casos, mas a antecipação do alvo em movimento só se prova no jogo); um único ovo de geração que cria qualquer variante.
   - Quinto ajuste (2026-10-27): idades por cor (jovem, adulto, velho), drops (unha, dente, adaga, pique, touca; chances e desgaste em `docs/ARCHITECTURE.md`, decisão D-013) e a touca de Barrete Vermelho (amizade, ajuda ao portador, fúria ao tirar). Compila com `javac`; texturas e ícones conferidos fora do jogo; ícones dos itens e a textura de uso da touca nunca foram vistos no jogo.
   - Sexto ajuste (2026-10-27): a touca virou um modelo 3D de capuz na cabeça (D-014) e o Bestiário foi criado (D-015), com o capítulo do Redcap em 10 páginas (folclore, sangue, adaga, garras, ataques, bando, touca, onde encontrar, despojos), desenhos em tinta feitos a partir do modelo e receita no survival. O texto do livro admite que a geração natural ainda não existe.
   - Ainda não verificado em jogo: o modelo da touca na cabeça, o Bestiário (layout, quebra de linhas, botões, desenhos), os drops, a touca (amizade, ajuda, fúria), as cores por idade, o comportamento em grupo, a mira das pedrinhas, as novas animações, os sons e o golpe atrasado (testar em modo sobrevivência).
   - Pendente, opcional: voz de goblin original em `.ogg` no lugar dos áudios do jogo base (exige encoder OGG).
   - Não incluído na fatia: emboscada (escalar ruínas e arremessar pedras), mecânica do gorro de sangue, banimento sagrado, spawn e spawn egg, drops.
7. **Testes automatizados.** Game Tests nativos adotados para o Basilisco (D-017). Executar `.\gradlew.bat runBasiliskTest`.
8. ~~**Push do commit `54252a1`.**~~ Incluído no histórico de `origin/main`.

## Basilisco da Praga — 2026-10-27

- Implementado a partir do Markdown fornecido: modelo, texturas, olhos emissivos, animações procedurais, sons, IA de saltos e sopro, repulsão por itens das almas, acúmulo/decadência/cura de praga, HUD, dano com suporte ao totem, geração subterrânea e drops.
- Implementados os usos das recompensas: lírio para golem/visão espiritual, flechas de podridão, frasco arremessável e amuleto segurado em uma mão. Capítulo adicionado ao Bestiário em ambos os idiomas.
- Adaptações e limites: [design/basilisk.md](design/basilisk.md).
- Build e Game Tests passaram: 11 testes próprios e um teste do framework, 12 obrigatórios aprovados. Cobertura e revisão visual em [DEVELOPMENT.md](DEVELOPMENT.md).
- Revisão no cliente identificou e corrigiu acentos substituídos por `?` nos novos textos em português e faces coplanares nos olhos. A correção foi compilada e a suíte de servidor passou novamente.
- Revisão visual concluída: modelo corrigido, ícones, sete páginas do capítulo em português, barra de praga sincronizada, decadência, salto/sopro e repulsão pela tocha das almas. Evidências e limites da verificação em `docs/DEVELOPMENT.md`.

## Revisão do Basilisco — 2026-10-28

- Praga substituída por Decomposição I vanilla no sopro; removidos barra, acúmulo, raízes, attachments e dano próprio. Antecipação, área, duração da névoa, movimento e demais regras do mob preservados.
- Drops atuais: perna (50%) e olho (25%), independentes do bioma. Receita sem forma com ambos e frasco de vidro produz poção de resistência por três minutos, curando Decomposição existente e bloqueando novas aplicações.
- Receitas e entradas do criativo das recompensas anteriores removidas; registros antigos preservados para inventários existentes. Bestiário atualizado em português e inglês, com novos ícones de itens e efeito.
- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, 11 testes do mod e um do framework aprovados (12 obrigatórios). Receita, consumo, cura/proteção, expiração, leite, dano vanilla, sobreposição, paredes, drops e IA exercitados no servidor.
- Pendente: revisão dos novos ícones, efeito de corações pretos e textos do Bestiário no cliente, além de avaliação de balanceamento pelo jogador. Ícones inspecionados fora do jogo.

## Decomposição III e preparo vanilla — 2026-10-28

- Sopro elevado para Decomposição III (amplificador 2) por cinco segundos; eleva efeitos mais fracos e preserva os mais fortes, sem renovar continuamente o relógio de dano.
- Rabo adicionado com chance de 35%, independente de perna (50%) e olho (25%). Ícone próprio, registro, tradução e entrada em Ingredientes.
- Receita de bancada substituída por suporte vanilla: água + fungo do Nether → estranha → perna → olho → rabo → resistência por três minutos. Redstone estende para oito; pólvora e bafo de dragão usam conversões vanilla.
- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, 15 testes obrigatórios aprovados (14 do mod e um do framework). O suporte real foi exercitado, incluindo ausência de combustível, vinte segundos por etapa, consumo de ingrediente e até três frascos, preparo completo, consumo e extensão. Também verificados ordem das misturas, conversões de recipiente, força III, efeitos mais fracos/fortes e os três drops.
- Bestiário e documentação atualizados. Ainda pendente de inspeção visual no cliente: apresentação dos novos itens, nomes de poções e paginação dos textos; testes de servidor não substituem revisão visual ou avaliação de balanceamento.

## Bestiário ilustrado e spawn comum — 2026-10-28

- Três pranchas em sépia adicionadas ao capítulo do Basilisco: anatomia, sopro e partes. As duas primeiras usam geometria e poses do modelo real; a terceira usa os ícones dos drops. Ferramentas opcionais de geração versionadas em `tools/`.
- Spawn natural ampliado a pântanos/manguezais em qualquer luz e cavernas escuras em todo o Overworld; removido limite de Y < 0. Peso 100, grupos 4–8 e limite de agrupamento oito. Mantida categoria hostil `MONSTER` e seus limites vanilla.
- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, 17 testes obrigatórios aprovados (16 próprios e um do framework). Novos testes exercitam tabelas reais de seis biomas, peso/grupos, pântanos de dia, solo válido, subterrâneo sob planícies, exclusão do Nether e de cavernas iluminadas.
- Cliente de inspeção em `build/basilisk-preview`: três imagens e todas as dez páginas do capítulo em português (13–22) conferidas no jogo. Título de preparo encurtado e texto compactado após revisão, com recursos recarregados e conferidos novamente. Nenhum mundo de `run/saves/` foi usado.
- Pendente: avaliação da densidade em campanha de sobrevivência; os testes confirmam regras e tabelas, sem prometer quantidade fixa por área. A versão inglesa recebeu traduções/captions, mas não foi inspecionada visualmente nesta revisão.

## Litófago (Lithophage) — 2026-10-29

- Implementado o mob completo `codenamecoraline:lithophage` e o projétil `codenamecoraline:amber_shard`: modelo articulado (~2,1 blocos) com máscara de argila desgastada, ombros assimétricos e cristais de âmbar; texturas 128×128 (base + camada emissiva); 12 animações por keyframe + procedural; 7 estados de IA (`DORMANT`, `AWAKENING`, `VIGILANT`, `PURSUING`, `COMBAT`, `RECOVERY`, `DYING`); 3 ataques sincronizados (Golpe de Escória, Varredura Tectônica e Estilhaço de Âmbar); sons, partículas, ovo de geração (`lithophage_spawn_egg`), drops (`ancient_clay_fragment` e `mineral_amber_core`) e capítulo ilustrado no Bestiário (`Bestiary.LITHOPHAGE`).
- `.\gradlew.bat build runLithophageTest`: **VALIDADO** em 2026-10-29 (11 testes obrigatórios aprovados, 10 próprios incluindo ressonância sísmica em grupo e 1 do framework). `.\gradlew.bat runBasiliskTest`: **VALIDADO** em 2026-10-29 (17 testes obrigatórios aprovados).

## Cobrador da Última Dádiva (Collector of the Last Gift) — 2026-10-29

- Implementado o boss opcional `codenamecoraline:last_gift_collector` e o ator de trenó `codenamecoraline:collector_sleigh`: modelo colossal de 4,5 blocos (`LastGiftCollectorModel`, atlas 256×256 + camada emissiva `LastGiftCollectorEyesLayer`), modelo do trenó de ~8 blocos (`CollectorSleighModel`, atlas 128×128), 18 animações por keyframe (`LastGiftCollectorAnimations`), diretor de câmera e HUD no cliente (`CollectorCinematicDirector`) com tecla de pulo configurável (`key.codenamecoraline.skip_cinematic`), item de invocação `silent_bell` com receita e validação de 5 condições rituais, 2 fases de combate, 4 ataques coreografados (`WINTER_SCYTHE`, `HOLLOW_BELL`, `PAWN_PROCESSION`, `SOLSTICE_JUMP`), saque (`cracked_bell`, `frostbound_thread`, `solstice_seal`), conquista `bosses/the_final_delivery` e capítulo ilustrado de 6 páginas no Bestiário (`Bestiary.LAST_GIFT_COLLECTOR`).
- Segunda revisão (D-024): animações de machado, Presente Faminto, magia com rastro, IA, Fúria, presente final com baú e Machado Enorme, conquistas de história. GameTestServer direto: Cobrador 15/15, Basilisco 17/17, Litófago 11/11 em 2026-10-29; Gradle e `runClient` pendentes.
- Revisão pós-feedback (D-023): 5 estágios, 5 novos ataques, imprevisibilidade e visual renegado. Validado sem Gradle (GameTestServer direto): Cobrador 12/12, Basilisco 17/17, Litófago 11/11 em 2026-10-29; `gradlew build`/`runXTest` ainda a rodar no terminal do usuário; qualidade visual/cinemática pendente de `runClient`.
- `.\gradlew.bat runLastGiftCollectorTest` (versão anterior à D-023): **VALIDADO** em 2026-10-29 (9 testes obrigatórios aprovados, 8 próprios e 1 do framework). `.\gradlew.bat build runLithophageTest runBasiliskTest`: **VALIDADO** em 2026-10-29 sem regressões.

## O Quebra-Copas (The Canopy Breaker) — 2026-10-29

- Implementado o boss opcional `codenamecoraline:canopy_breaker`: modelo e textura próprios (37 caixas, atlas 256×256, olhos emissivos), 88 animações por keyframe sincronizadas pelo servidor, três estágios (60% e 25%, uma vez cada), seis ataques com preparo, execução e recuperação, mobilidade arbórea real (escalada com apoio verificado, saltos balísticos entre copas, Queda da Copa, perda de apoio com recuperação, cancelamento de saltos inválidos, volta ao chão), Ídolo do Tronco Oco (6 condições), ovo, Porrete de Raiz-Morta, Resina da Copa e Estilhaço de Cerne, barra de vida temática, Bestiário de 6 páginas com três gravuras, quatro conquistas e documentação (`docs/design/canopy_breaker.md`, D-028).
- GameTestServer direto: Quebra-Copas 18/18 aprovados em 10 execuções seguidas em 2026-10-29. Regressão no mesmo dia: Basilisco 17/17, Litófago 11/11, Cobrador 18/18. Pendentes, rodar no terminal do usuário: `.\gradlew.bat build`, `.\gradlew.bat runCanopyBreakerTest`, as demais tarefas de teste e `.\gradlew.bat runClient`.
- **Não verificado no jogo:** leitura das animações em movimento, sons, barra de vida e gravuras no Bestiário, desempenho do planejamento de saltos em florestas reais e balanceamento da luta. As poses foram inspecionadas apenas em renderizações offline do modelo real.

## Porrete de Raiz-Morta: Golpe Descendente em Salto — 2026-10-29

- Implementada a habilidade do `deadroot_club`: salto no chão (clique direito), mergulho e golpe no solo com dano em área (100% sob o impacto, 60% no anel externo) e dano crescente com a altura da queda. Recarga de 12 s e durabilidade por uso. Traduções pt/en e descrição no item.
- Revisão em 2026-10-29: carga do salto (segurar o clique direito), dano que cresce com a altura da queda (1,5 por bloco, até 16 blocos) e invocação de Micos de Casca com Shift + clique direito (cap de 4, custo de durabilidade, recarga). Salto mais horizontal (apex de cerca de 1 a 4 blocos, alcance de cerca de 4 a 11) para servir de locomoção; impacto no chão com martelada pesada, poeira, pedra, e dois anéis de onda de choque.
- GameTestServer direto: `DeadrootClubGameTests` 7/7 de habilidade aprovados em 3 execuções seguidas. Pendentes: `.\gradlew.bat runDeadrootClubTest` e `runClient`.
- **Não verificado no jogo:** a curva do salto, a sensação do mergulho, os sons e as partículas do impacto, e o alcance da área frente ao terreno real.

## Lacaios do Quebra-Copas: Micos de Casca — 2026-10-29

- Implementado `codenamecoraline:bark_macaque` (modelo, textura, 12 animações, salto balístico com mordida, subida de tronco, escalada de paredes), ovo, sons e traduções, e a integração no boss: chamado de abertura (com e sem ídolo), um reforço por estágio, limite de quatro, vínculo, saída com a morte do boss e remoção de órfãos (D-029).
- GameTestServer direto: 26/26 em 12 execuções seguidas; regressões sem falhas. Pendentes: Gradle, `runClient`.
- **Não verificado no jogo:** animações em movimento, sons, balanceamento. **Não implementado:** Javali de Raiz (adiado por decisão, D-029).

## Invocações, voo do porrete, intro e espíritos — 2026-10-29

- Invocações de jogador (Micos e Presentes aliados) não se atacam nem se ferem; o golpe do porrete as poupa.
- Porrete: voo na direção do olhar com empuxo sustentado (como um martelo arremessado), mergulho com Shift no ar.
- Quebra-Copas: intro de 6 s (olhar, bater no peito com tambor, rugido, título) com câmera, tarjas e tremor no padrão do Cobrador.
- Morte do Quebra-Copas e do Cobrador: espírito translúcido e azul subindo do corpo.
- GameTestServer direto: ver `docs/DEVELOPMENT.md`. **Não verificado no jogo:** câmera e título da intro, transparência e cor do espírito, sensação do voo.

## Cegueira do Basilisco — 2026-10-29

- Névoa aplica Cegueira por cinco segundos junto de Decomposição III. A poção existente cura e bloqueia ambos durante os mesmos três/oito minutos; nomes e Bestiário atualizados em português e inglês.
- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, todos os testes obrigatórios aprovados, incluindo Cegueira na névoa, paredes, cura, proteção, expiração e leite.

## Dragão Bebê Companheiro — 2026-10-29

- Implementados `baby_dragon`, domesticação/cura por salmão, comandos de ficar/seguir/defesa, navegação terrestre/aérea, órbita variável, ombro preservando a entidade original, saída segura, persistência e centelhas defensivas sem fogo amigo, incêndios ou destruição. Spawn raro em florestas/montanhas/taigas/bosques e ovo nas abas criativas.
- Modelo original com quatro patas, mandíbula, olhos, chifres, asas membranosas articuladas, cauda e atlas detalhado. Doze animações nativas, oito sons com eventos vanilla, capítulo de seis páginas-base em pt-BR/en-US e três gravuras exportadas da geometria real. Índice do Bestiário ajustado para o sexto capítulo.
- Gradle `build` e `runBabyDragonTest`: **VALIDADOS**, suíte final **31/31 em três execuções consecutivas**. Regressões: Basilisco 17/17, Litófago 11/11, Cobrador 18/18, Quebra-Copas 27/27 e Porrete 9/9, com pelo menos duas aprovações de cada suíte. Falhas intermitentes anteriores registradas no relatório; testes antigos preservados.
- Duas sessões reais de `runClient` em mundo descartável: modelo/textura, voo, domesticação, felicidade, ombro e permanência ao reabrir, primeira pessoa desobstruída e capítulo completo nos dois idiomas conferidos. Prévias estáticas e animadas geradas. **Ainda dependem de avaliação:** áudio, combate prolongado/balanceamento, terreno florestal denso e multijogador sob latência. A expansão do peito no carregamento foi validada offline após a inspeção.
- Especificação: [design/baby_dragon.md](design/baby_dragon.md). Matriz de conformidade, testes, decisões, arquivos e evidências: [design/baby_dragon_validation.md](design/baby_dragon_validation.md).


- Correcao posterior do Dragao Bebe: removido impulso lateral no ombro e conflito do disparo com o evento vanilla do Sniffer; dois testes reproduziram os defeitos e a versao corrigida passou **33/33 GameTests e build**. Detalhes em [relatorio](design/baby_dragon_validation.md).

- Dragao Bebe agora auxilia contra mobs pacificos atingidos pelo dono, incluindo galinhas, vacas e porcos, preservando aliados e animais sem envolvimento. Build e **38/38 GameTests aprovados**.

## Quebra-Copas: golpes de gorila, guarda quebrada e salto de chão — 2026-10-31

- Porrete segurado pela ponta do cabo, cabeça para fora (geometria, não só pose); golpes quebram a guarda do escudo; Agarrar e Arremessar e Soco no Chão com animações próprias; salto de chão (inclusive contra alvos em pilares) e flanco por árvores desde o estágio 1. Ver D-031.
- GameTestServer direto: ver `docs/DEVELOPMENT.md`. **Não verificado no jogo:** animações em movimento, sensação do arremesso, comportamento em florestas reais, o caminho completo golpe -> escudo de jogador real.

## Comandante Carmesim — 2026-11-03

- Novo boss `crimson_commander`: cavaleiro carmesim de 3,5 blocos com espada larga; Corte Cruzado, Investida Perfurante, Golpe Pesado Aéreo, Guarda Defensiva com Riposta e Onda de Lâmina Sanguínea; três estágios (60% e 25%) com Sentinelas de Cinza Carmesim no segundo e combo de fúria no terceiro; spawn natural raro abaixo de Y 0 no escuro e invocação com a Insígnia do Juramento Manchada sobre tijolos de ardósia; morte com espírito e a Espada Larga do Juramento Carmesim (Investida Carmesim) entregue uma vez; barra temática, Bestiário de 6 páginas com 3 gravuras do modelo real, 4 conquistas, receita, loot e sons. Ver D-032 e [design/crimson_commander.md](design/crimson_commander.md).
- Evidência: GameTestServer direto, 32/32 em 8 execuções seguidas com o código final; regressão das outras suítes aprovada (ver `docs/DEVELOPMENT.md`). Poses de referência conferidas em renders offline do modelo real e por teste de geometria.
- **Pendente / não verificado no jogo:** `.\gradlew.bat build` e `runCrimsonCommanderTest` (o Gradle não roda no shell do agente), `runClient` (aparência, animações em movimento, partículas, sons, barra, gravuras no livro), balanceamento real, comportamento em cavernas reais e contra um jogador real (o jogador de teste não entra nas buscas de entidades). A primeira Insígnia só vem de um Comandante natural (raro) ou do criativo.
