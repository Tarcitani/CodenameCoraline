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
