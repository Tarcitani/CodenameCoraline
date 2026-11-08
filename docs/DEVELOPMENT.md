# Desenvolvimento

## Remodelagem anatômica do dragão — 2026-11-04

Verificação complementar: cliente real nos três estágios, evolução, inventário e persistência; montaria, câmera, asas/cauda, bola colossal/magma, sopro e deslocamento em servidor dedicado local. A câmera agora usa `CAMERA_DISTANCE` conforme o rig. O registro gráfico dos testes é delegado por `ModelGameTestRegistration` somente quando solicitado (D-037); build e suítes 81/35/34 aprovados em `%TEMP%/coraline-dragon-server-compatibility-tests.log`. Servidor dedicado iniciou em `127.0.0.1`. Os dois perfis conectaram, mas a alternância de janelas provocou duas falhas nativas de GLFW; inspeção simultânea e latência não aprovadas. Evidências e comandos em [design/dragon_remodel.md](design/dragon_remodel.md).

`build` com os 15 unitários aprovado. `runBabyDragonTest`: **81 testes obrigatórios, seis execuções consecutivas aprovadas**, logs `%TEMP%/coraline-dragon-remodel-isolated-1..6.log`. As fixtures de evolução/mecânica usam `setNoAi(true)` explicitamente: `spawnWithNoFreeWill` remove goals/behaviors vanilla, mas não impede `customServerAiStep` e o cérebro próprio. Montaria real e testes anteriores de perseguição/voo continuam com movimento habilitado. Nenhuma asserção foi removida ou afrouxada.

Regressões: Basilisco 17/17, Litófago 11/11, Cobrador 18/18 (na repetição), Quebra-Copas 35/35, Porrete 9/9 e Carmesim 34/34. O Cobrador teve falha inicial em `collector_late_stage_attacks_deal_damage` (Giro); passou sem alteração na repetição. Logs `%TEMP%/coraline-dragon-remodel-regression-*.log`.

```powershell
python tools/generate_dragon_remodel.py
.\gradlew.bat -I tools/baby-dragon-sketches.gradle exportBabyDragonSketchModel
python tools/generate_baby_dragon_sketches.py
.\gradlew.bat --no-parallel --max-workers=1 build runBabyDragonTest
```

Gerador, exportador e rasterizador executados. Paleta de referência em `tools/dragon_original_palette.json`; geometrias e `DragonRig` gerados juntos; atlas 512×512 por idade e máscaras emissivas. O gerador antigo de assets chama o novo para não restaurar um atlas incompatível com os modelos atuais.

Prévias: `build/baby-dragon-sketches/{baby_views,young_views,adult_views,anatomy_clay,evolution,adult_attacks}.png` e `animations.gif`. `runClient` de inspeção usa `-I tools/dragon-evolution-preview.gradle` e só o mundo descartável em `build/dragon-evolution-preview`. Evidências/limites em [design/dragon_remodel.md](design/dragon_remodel.md). Comparação com a imagem ausente e avaliação humana de movimento/combate continuam pendentes.

## Evolução do dragão — 2026-11-04

- `build runBabyDragonTest --rerun-tasks`, com `--no-parallel --max-workers=1`: **VALIDADO em seis rodadas consecutivas**, 15 unitários e 71 GameTests por rodada. Logs `%TEMP%/coraline-dragon-evolution-verified-1.log` a `-6.log`. O JUnit 5.13.4 foi autorizado pelo usuário e é exclusivo de testes; o jar do mod não o incorpora.
- Regressões executadas isoladamente em seis rodadas: Basilisco 17, Litófago 11, Cobrador 18, Quebra-Copas 35, Porrete 9 e Carmesim 34 testes por suíte. Basilisco e Cobrador tiveram uma falha isolada cada e passaram numa sétima tentativa, acumulando seis aprovações; ver relatório para nomes das falhas. Logs `%TEMP%/coraline-dragon-evolution-regression-run<Suite>Test-<rodada>.log`.
- Exportação e rasterização: **VALIDADAS**, 612 amostras (3 idades × 17 poses × 12 quadros), PNGs de comparação/ataques e GIF em `build/baby-dragon-sketches/`. Comandos abaixo.
- Cliente de inspeção: `-I tools/dragon-evolution-preview.gradle runClient`, diretório isolado `build/dragon-evolution-preview`, cópia do mundo descartável anterior. Abertura da tela, movimentação de pilhas, montagem, assento e tecla G observados. Screenshots em `build/dragon-evolution-preview/screenshots/`. **Qualidade final de animações/combate/som permanece não verificada pelo usuário.** Nenhum mundo em `run/saves` foi usado.

```powershell
.\gradlew.bat --no-parallel --max-workers=1 build runBabyDragonTest
.\gradlew.bat -I tools/baby-dragon-sketches.gradle exportBabyDragonSketchModel
python tools/generate_baby_dragon_sketches.py
.\gradlew.bat runClient
```

Comandos no jogo: `/dragon <UUID ou seletor de uma entidade> stage baby|young|adult`, `xp add <1..2000>` e `info`. Exemplo: `/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage adult`. Exigem operador e mesma dimensão. `stage` posiciona o XP no início da idade, sem animação; `xp add` percorre a evolução normal. Reduções recusam slots ocupados que seriam escondidos.

Controle: salmão doma/cura; graveto normal ficar/seguir; agachar com graveto alterna defesa. Agachar com livro abre GUI em qualquer idade; agachar vazio abre GUI no Jovem/Adulto e mantém ombro no Bebê. Clique vazio no Adulto monta. W/S movem, A/D viram, Espaço sobe, X desce/pousa, G desmonta, R dispara e B sopra. Teclas novas remapeáveis; Shift mantém desmontagem vanilla e V mantém cinemáticas.

Balanceamento e matriz de verificação em [design/dragon_evolution.md](design/dragon_evolution.md) e [design/dragon_evolution_validation.md](design/dragon_evolution_validation.md).

Procedimentos de compilação, execução, testes e diagnóstico. Cada comando tem um status. **Só marque "validado" após executar o comando com sucesso e registrar a data.**

## Compilação

| Comando | Função | Status |
|---|---|---|
| `./gradlew build` (PowerShell: `.\gradlew.bat build`) | Compila, executa verificações e gera `build/libs/codenamecoraline-<versão>.jar` | VALIDADO em 2026-10-27: `BUILD SUCCESSFUL` em `build.log`; jar `codenamecoraline-0.1.0-SNAPSHOT.jar` gerado com `neoforge.mods.toml` expandido e classe compilada em Java 25 |
| `./gradlew compileJava` | Compila apenas o código Java | VALIDADO em 2026-10-27 (incluído no `build`) |

Houve uma falha histórica de loopback no ambiente anterior do agente (`Unable to establish loopback connection`). Em 2026-10-27, no workspace `C:\Users\Venti\CodenameCoraline`, `build`, `runBasiliskTest` e o cliente de inspeção executaram pelo shell do agente com sucesso. O diagnóstico histórico abaixo é mantido para referência.

Primeira execução: baixa a distribuição do Gradle 9.2.1, o JDK 25 (se ausente), o Minecraft 26.1.2 e o NeoForge. Pode levar vários minutos e vários GB.

## Execução da instância de desenvolvimento

| Comando | Função | Status |
|---|---|---|
| `./gradlew runClient` | Abre o Minecraft cliente com o mod | VALIDADO em 2026-10-27: janela do jogo abriu; log com `codenamecoraline loaded`; encerramento limpo (`Stopping!`, sem crash) |
| `./gradlew runServer` | Inicia servidor de desenvolvimento com `--nogui` | VALIDADO em 2026-10-27: log com `codenamecoraline loaded` e `Done (2.315s)` |

Diretórios de execução: `run/` (ignorado pelo Git). Esses comandos são caros; use-os somente quando a mudança exigir verificação em jogo.

## Texturas geradas (Redcap)

`src/main/resources/assets/codenamecoraline/textures/entity/redcap.png` e `redcap_eyes.png` (128x64, mesmo tamanho declarado em `RedcapModel.createBodyLayer`) foram pintados em 2026-10-27 por uma ferramenta Java descartável fora do projeto (scratchpad da sessão de IA). A ferramenta lê a geometria real de `RedcapModel` (`ModelPart.visit`), pinta cada face por material e renderiza um preview com culling de faces traseiras. A ferramenta não é versionada: os PNGs são a fonte. Para repintar, edite os PNGs (ex.: Blockbench).

Verificado pela ferramenta: nenhuma sobreposição de UV entre cubos diferentes; `redcap_eyes.png` com 4 pixels opacos (`#FF1E00` e `#FF6A2A`) na face frontal da cabeça. Verificado em preview fora do jogo: silhueta, cores e posição das peças. Verificado em jogo (2026-10-27): as texturas carregam e o visual corresponde ao preview. Não verificado em jogo: animações de caminhada e ataque.

## Testes

- `.\gradlew.bat runBasiliskTest` executa os Game Tests do Basilisco em `build/basilisk-gametest/`, sem abrir os mundos do usuário. Não exige um framework externo (D-017).
- `.\gradlew.bat runDeadrootClubTest` executa os 7 testes da habilidade do Porrete de Raiz-Morta (carga, dano por altura, invocação com cap, aliados) em `build/deadroot-gametest/` (propriedade `coraline.deadrootTests=true`).
- `.\gradlew.bat runCanopyBreakerTest` executa os 17 testes do Quebra-Copas em `build/canopy-gametest/` (propriedade `coraline.canopyTests=true`). O terreno é construído por cada teste a Y relativo 100 e todos os chunks dele são forçados.
- `.\gradlew.bat runCrimsonCommanderTest` executa os 32 testes do Comandante Carmesim em `build/crimson-gametest/` (propriedade `coraline.crimsonTests=true`). Status: **não executado pelo agente** (o Gradle não abre a conexão de loopback no shell dele); a suíte foi validada pelo lançador direto do GameTestServer (ver a seção de 2026-11-03).
- `.\gradlew.bat runTenThreadsWitchTest` executa os 30 testes da Bruxa dos Dez Fios em `build/ten-threads-gametest/` (propriedade `coraline.tenThreadsTests=true`). Status: **não executado pelo agente** (Gradle indisponível no shell dele); a suíte foi validada pelo lançador direto do GameTestServer (ver a seção de 2026-11-05).
- `.\gradlew.bat runFeyRavenTest` executa os 8 testes do Corvo Feérico em `build/fey-raven-gametest/` (propriedade `coraline.feyRavenTests=true`). Status: **VALIDADO em 2026-11-05** em 4 rodadas consecutivas via Gradle (ver seção abaixo).
- `.\gradlew.bat runArchitectTest` executa os 34 testes do Arquiteto (fundação, revelação, IA e combate, conteúdo e Cimitarra) em `build/architect-gametest/` (propriedade `coraline.architectTests=true`). Status: **VALIDADO em 2026-11-08** (6 rodadas seguidas na fatia 4, "All 35 required tests passed"; `build` completo verde), rodado pelo agente no terminal do app: no shell com sandbox o Gradle falha com `Unable to establish loopback connection`, mas no painel de terminal (fora da sandbox) funciona.
- Os testes só são registrados com `coraline.basiliskTests=true`, configurado nessa execução Gradle. Não são ativados por `runClient` ou `runServer` comuns.
- A compilação (`build`) verifica o código e gera o JAR; os Game Tests verificam o comportamento no servidor. A aparência, o HUD e a paginação do Bestiário precisam também de revisão no cliente.

## Depuração

- Logs: `run/logs/latest.log` (cliente e servidor).
- Procure a linha `codenamecoraline loaded` para confirmar que o mod carregou.
- Para depurar no IDE: importe o projeto como Gradle, aguarde a sincronização e use as run configurations geradas pelo plugin.
- VS Code: `.vscode/settings.json` (local, ignorado pelo Git) aponta o Java language server e a importação do Gradle para o JDK 25 em `%USERPROFILE%\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2`. Status: **NÃO VALIDADO**; confirme na IDE que o Java de projeto é 25 e que não há erros falsos. O `java` do PATH é 21 e não deve ser alterado.

## Diagnóstico de erros comuns

| Sintoma | Causa provável | Ação |
|---|---|---|
| `Unable to establish loopback connection` | Gradle 9.2.1 (JVM 21 do sistema) falha em `java.nio.channels.Pipe` com `SocketException: Invalid argument: connect` (`PipeImpl$Initializer$LoopbackConnector`). | Causa **não identificada**. Testado em 2026-10-27: Git Bash, PowerShell (`gradlew.bat`) e `JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true`: todos falharam. Um programa Java simples que chama `Pipe.open()` funciona no mesmo JDK, então o problema está no contexto do Gradle, não no JDK. Próximo passo: rodar `gradlew build --stacktrace --info` fora do sandbox do agente, ou verificar firewall/antivírus/VPN e adaptadores virtuais (há interfaces 192.168.56.x e IPv6 ativos). |
| `Toolchain ... 25 not found` / falha de provisionamento | JDK 25 ausente e download bloqueado | Instale Temurin 25 ou verifique acesso ao `api.adoptium.net` e ao `foojay` |
| `Could not resolve net.neoforged...` | Versão de NeoForge inexistente ou repositório indisponível | Confira `neo_version` no Maven `maven.neoforged.net` |
| `Failed to load properties from file: server.properties` (`NoSuchFileException`) na primeira execução do `runServer` | Esperado: o arquivo não existia e o servidor o cria em seguida. Se o `Done (...)` aparecer depois, é só aviso. | Nenhuma |
| `Missing property ... mod_id` (ou outra propriedade) na tarefa `generateModMetadata` | Propriedade removida de `gradle.properties` | Restaure a propriedade ou ajuste `build.gradle` |
| `modId mismatch` / mod não carrega | `MOD_ID` diferente de `mod_id` | Alinhe `CodenameCoraline.MOD_ID`, `mod_id` e o `modId` do toml |
| Build lento ou sem espaço | Caches do Gradle em `%USERPROFILE%\.gradle` | Limpe apenas caches do Gradle com autorização; não apague pastas de outros projetos |

## Procedimento de validação

1. `git status` deve mostrar somente os arquivos esperados.
2. `./gradlew build` sem erros.
3. Opcional, sob demanda: `./gradlew runServer` e confirmar a linha de carregamento do mod no log.
4. Atualize o status na tabela acima com a data e o resultado real.

## Como acrescentar um mob ao Bestiário

1. Anexe um `BestiaryEntry` em `bestiary/Bestiary.java` (e à lista `ENTRIES`), com as páginas desejadas.
2. Escreva as chaves `bestiary.codenamecoraline.<id>.*` em `lang/pt_br.json` e `lang/en_us.json`: `name`, e `title`/`body` por página (`caption` nas páginas com desenho; `body` pode ser `""`). O texto de uma página só com texto cabe em cerca de 15 linhas de 21 caracteres; passando disso ele continua numa página extra.
3. Desenhos: PNG de 112 de largura em `textures/gui/bestiary/`. Os do Redcap foram gerados a partir do próprio modelo e das texturas por uma ferramenta descartável (render em perfil, contornos e hachuras em tinta sépia, capuz em tinta vermelha) que não é versionada: os PNGs são a fonte.
4. Escreva só o que o jogo faz de fato. O capítulo do Redcap diz que a geração natural ainda não existe ("por ora, só se invoca com o ovo"); atualize esse trecho quando as regras de geração forem implementadas.

## Verificação do Basilisco — 2026-10-27

- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, 12 testes obrigatórios aprovados (11 do mod e um do framework). Repetido após as correções da revisão visual, novamente com sucesso.
- Cobertura dos testes do mod: pulsos e decadência da barra; imunidades e multiplicadores de dano; luz das almas nas duas mãos e provocação; névoa sem dano direto; sobreposição sem multiplicar acúmulo; 100 de dano atravessando armadura; consumo e sobrevivência com totem; quatro consumíveis de cura; amuleto; sequência real de saltar/inspirar/expelir; cura e resistência do golem pelo lírio.
- Os logs de servidor foram revisados além do resultado dos testes. Foi corrigida uma receita que referenciava `minecraft:chain`; o identificador usado no 26.1.2 é `minecraft:iron_chain`.
- Inspeção em cliente: mundo descartável copiado dos Game Tests para `build/basilisk-preview/saves/BasiliskInspection`, sem modificar `run/saves/`. A execução recebeu um init script temporário do Gradle para usar esse diretório.
- Bestiário em português: índice, botão do Basilisco e todas as sete páginas (13–19 nesta tradução) conferidos no jogo, sem corte de títulos, texto ou rodapés. Foram corrigidos os acentos que haviam virado `?` nos novos textos.
- Ícones dos seis novos itens (incluindo o ovo) conferidos na barra rápida, sem textura ausente. Modelo conferido de frente e em três quartos no jogo; as seções dos olhos e as placas das pupilas receberam espessuras distintas para remover faces coplanares, e o cliente foi reaberto para conferir a correção.
- HUD conferido em sobrevivência: barra preta/dourada legível, valores de 15, 30 e 45 sincronizados durante exposição e desaparecimento após a decadência. O cenário visual usou Resistência V para permitir observação sem morte; a letalidade e o totem foram verificados separadamente pelos Game Tests.
- Conferidos no cliente o recuo ao equipar tocha das almas na mão secundária e a retomada dos saltos/sopro após removê-la. A névoa gerada pelo ataque apresentou partículas de fumaça, tinta e fagulhas douradas, sem erro de renderização.
- Evidências locais: `build/basilisk-preview/screenshots/2026-10-27_19.43.32.png` (Bestiário), `2026-10-27_19.45.19.png` (modelo corrigido) e `2026-10-27_19.46.02.png` (HUD). São arquivos da execução descartável, ignorados pelo Git.
- As texturas do Basilisco são reproduzíveis com `python tools/generate_basilisk_assets.py` (Pillow). Esse comando não é necessário para compilar ou jogar.

Os testes de servidor não medem frequência de geração em uma campanha de sobrevivência, nem substituem avaliação de balanceamento pelo jogador.

## Revisão do Basilisco — 2026-10-28

- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, `BUILD SUCCESSFUL`; todos os 12 testes obrigatórios aprovados (11 próprios e um do framework). A primeira compilação apontou uma assinatura incorreta no teste de drops; corrigida antes da execução bem-sucedida.
- Cobertura atual substitui os testes da praga: aplicação de Decomposição I e dano gradual real enquanto permanece na névoa; ausência de dano instantâneo; duração da névoa/efeito; sobreposição sem amplificação ou reinício do relógio de dano; bloqueio por parede; receita de perna/olho; consumo devolvendo frasco; cura e proteção contra Decomposição, inclusive amplificador maior; expiração e remoção por leite; drops exclusivamente de perna/olho; imunidades, fraquezas, luz das almas e sequência da IA.
- Log da execução: `%TEMP%/coraline-wither-validation.log`. Avisos de API obsoleta e de terminal sem recursos avançados; nenhum erro de registro, receita ou teste. O PowerShell classificou a nota de API obsoleta emitida no stderr como `NativeCommandError`, apesar do Gradle concluir com sucesso.
- Novos ícones gerados pelo script existente e inspecionados em `build/basilisk-item-review.png`. A geração não alterou as texturas existentes do corpo ou do ovo.
- Ainda não conferidos no cliente: novos itens e ícone de efeito, apresentação dos corações pretos e paginação dos textos atualizados. A revisão visual de 2026-10-27 documenta o estado anterior.

## Decomposição III e suporte vanilla — 2026-10-28

- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, `BUILD SUCCESSFUL`; 15 testes obrigatórios aprovados (14 próprios e um do framework). Log: `%TEMP%/coraline-brewing-validation.log`.
- O teste do bloco real percorre água → estranha → infusão de perna → infusão ocular → resistência → versão estendida. Confere que sem pó de blaze nada é preparado, o combustível é consumido, cada mistura aguarda os 400 ticks vanilla e um ingrediente transforma três frascos. A poção produzida cura Decomposição III e concede proteção por 3600 ticks; redstone produz 9600 ticks.
- Testes adicionais verificam rejeição de ordem incorreta; conversões de pólvora/bafo de dragão; extensão mantendo recipiente; amplificador 2 e dano real da névoa; promoção de efeito fraco e preservação de efeito mais forte; drop de rabo e ausência dos drops antigos. Mantidas verificações de paredes, sobreposição, proteção, expiração, leite, atributos, luz das almas e IA.
- Ícone de rabo reproduzível pelo gerador existente. Textos atualizados em português e inglês, com nomes dos tipos nativos e variantes. A apresentação no cliente ainda não foi inspecionada nesta revisão.

## Bestiário ilustrado e spawn comum — 2026-10-28

- `.\gradlew.bat build runBasiliskTest`: **VALIDADO**, `BUILD SUCCESSFUL`; todos os 17 testes obrigatórios passaram (16 próprios e um do framework). Log: `%TEMP%/coraline-bestiary-spawns-validation.log`.
- Cobertura nova: tabelas de spawn reais de pântano, manguezal, planícies, cavernas verdejantes, cavernas de espeleotemas e escuridão profunda; peso 100, grupos 4–8 e categoria hostil. Um cenário real verifica superfície de pântanos em horário diurno, rejeição de superfície comum e solo inválido, câmara subterrânea escura, exclusão do Nether e rejeição após iluminação com tocha.
- Cliente executado com init script temporário, diretório `build/basilisk-preview` e quick play do mundo descartável `BasiliskInspection`. As três imagens carregaram; todas as páginas 13–22 do capítulo em português foram inspecionadas. Título de preparo foi encurtado e o texto compactado; `processResources` e recarga com F3+T permitiram conferir o resultado final. Recursos não apresentaram erros de carregamento.
- Evidências no diretório de screenshots da inspeção: `2026-10-28_17.46.03.png` (anatomia), `2026-10-28_17.46.26.png` (habitat/sopro), `2026-10-28_17.46.41.png` (partes), `2026-10-28_17.48.53.png` (preparo após a correção de texto).
- Ao carregar o mundo de inspeção antigo, NeoForge registrou e descartou um attachment salvo `death_blight_buildup` do sistema de praga removido. A execução continuou normalmente e o cliente encerrou com `BUILD SUCCESSFUL`; não houve erro de carregamento das novas imagens.
- A regra subterrânea usa altura sem folhas e cobertura de pelo menos quatro blocos, sem exigir Y negativo. A frequência prática depende de terreno, limite e competição de monstros; não foi medida em campanha. Traduções em inglês foram atualizadas, sem revisão visual dessa língua.

### Regenerar as pranchas do Basilisco

1. `.\gradlew.bat -I tools/basilisk-sketches.gradle exportBasiliskSketchModel` — **VALIDADO**. Compila a ferramenta opcional e exporta o modelo e poses reais para `build/basilisk-sketches/model.json`, usando o toolchain Java 25 e as dependências existentes.
2. `python tools/generate_basilisk_sketches.py` — **VALIDADO**. Requer Pillow, já usado pelo gerador de texturas, e escreve três PNGs transparentes de 112 pixels de largura em `textures/gui/bestiary/`. Anatomia tem altura 80, sopro 72 e partes 64. Prancha de revisão: `build/basilisk-sketches/review.png`.
3. Os arquivos de geração são opcionais: builds normais usam os PNGs versionados, sem executar essas ferramentas. Se os sprites de partes forem alterados, execute antes `python tools/generate_basilisk_assets.py`.

## Litófago (Lithophage) — 2026-10-29

- `.\gradlew.bat build runLithophageTest`: **VALIDADO** em 2026-10-29, `BUILD SUCCESSFUL`; todos os 10 testes obrigatórios passaram (9 testes do Litófago e 1 do framework).
- `.\gradlew.bat runBasiliskTest`: **VALIDADO** em 2026-10-29, `BUILD SUCCESSFUL`; todos os 17 testes obrigatórios do Basilisco continuam passando sem regressões.
- Cobertura de `LithophageGameTests`: atributos e bake das 12 animações no modelo real; ciclo `DORMANT -> AWAKENING -> VIGILANT -> PURSUING` e retorno a `DORMANT`; limite territorial de 20 blocos; Golpe de Escória (5 HP no tick 12 e janela `RECOVERY`); Varredura Tectônica (4 HP restrita ao arco frontal); Estilhaço de Âmbar (4 HP, bloqueio por parede e ausência de fogo amigo); cancelamento de golpe ao morrer e drops da tabela de saque (`ancient_clay_fragment` e `mineral_amber_core`); tabelas de bioma (`lush_caves` e `dripstone_caves`) e regras de altura (`Y = -20` a `20`), luz e colisão; ovo `lithophage_spawn_egg` e capítulo `Bestiary.LITHOPHAGE`.
- Geração reprodutível de recursos:
  1. `python tools/generate_lithophage_assets.py` — gera `lithophage.png`, `lithophage_eyes.png` e os 4 ícones de itens (`lithophage_spawn_egg`, `ancient_clay_fragment`, `mineral_amber_core`, `amber_shard`).
  2. `.\gradlew.bat -I tools/lithophage-sketches.gradle exportLithophageSketchModel` e `python tools/generate_lithophage_sketches.py` — exportam a geometria/poses reais e geram as 3 pranchas do Bestiário (`lithophage_anatomy.png`, `lithophage_combat.png`, `lithophage_spoils.png`).

## Cobrador da Última Dádiva (Collector of the Last Gift) — 2026-10-29

- `.\gradlew.bat runLastGiftCollectorTest`: **VALIDADO** em 2026-10-29, `BUILD SUCCESSFUL`; todos os 9 testes obrigatórios passaram (8 testes do Cobrador da Última Dádiva e 1 do framework).
- `.\gradlew.bat build runLithophageTest runBasiliskTest`: **VALIDADO** em 2026-10-29, `BUILD SUCCESSFUL`; todos os testes do Litófago (11/11) e do Basilisco (17/17) continuam passando sem regressões.
- Cobertura de `LastGiftCollectorGameTests`: registro de entidades/itens, atributos (128 HP, 8 armadura, 0,23/0,27 velocidade, 4,5 blocos de altura) e bake dos modelos (`LastGiftCollectorModel`, `CollectorSleighModel`) e das 18 animações; validação das 5 condições do `SilentBellItem` sem consumo em falha; cinemática completa de 190 ticks com `CollectorSleigh`, aterrissagem única (`landingCount == 1`) e desbloqueio da Fase I; Foice do Inverno restrita ao arco frontal e janela `RECOVERY`; Badalada Oca com 3 raios e fendas seguras; Procissão dos Penhores com 1,0 s de aviso antes da erupção; transição única para a Fase II aos 50% de vida e Salto do Solstício sem dano aéreo; cinemática de morte de 3,5 s cancelando ataques ativos, drops (`cracked_bell`, `frostbound_thread`), conquista `the_final_delivery` e capítulo `Bestiary.LAST_GIFT_COLLECTOR`.
- Geração reprodutível de recursos:
  1. `python tools/generate_last_gift_collector_assets.py` — gera `last_gift_collector.png` (256×256), `last_gift_collector_eyes.png` (256×256), `collector_sleigh.png` (128×128) e os 5 ícones de itens 16×16 (`last_gift_collector_spawn_egg`, `silent_bell`, `cracked_bell`, `frostbound_thread`, `solstice_seal`).
  2. `.\gradlew.bat -I tools/last-gift-collector-sketches.gradle exportLastGiftCollectorSketchModel` e `python tools/generate_last_gift_collector_sketches.py` — exportam a geometria 3D real e geram as 3 pranchas do Bestiário (`last_gift_collector_anatomy.png`, `last_gift_collector_combat.png`, `last_gift_collector_spoils.png`).

## Quebra-Copas — 2026-10-29

- GameTestServer direto (mesmo carregador e classpath do Gradle, classes compiladas com `javac`, recursos copiados e `neoforge.mods.toml` expandido à mão; o Gradle não consegue criar a JVM de build no shell do agente): **VALIDADO**, 18 testes obrigatórios aprovados (17 do mod e um do framework); suíte repetida 10 vezes seguidas. Regressão: Basilisco 17/17, Litófago 11/11 e Cobrador 18/18 aprovados pelo mesmo lançador.
- `.\gradlew.bat build` e `.\gradlew.bat runCanopyBreakerTest`: **não executados pelo agente**; rode no terminal. `runClient` também pendente: aparência, animações em movimento, sons e barra de vida não foram vistos no jogo.
- Ferramentas de ativos: `python tools/generate_canopy_breaker_assets.py` (modelo, textura, ícones), `.\gradlew.bat -I tools/canopy-breaker-sketches.gradle exportCanopyBreakerSketchModel` seguido de `python tools/generate_canopy_breaker_sketches.py` (gravuras), `python tools/generate_boss_bar_canopy_breaker.py` (barra) e `python tools/generate_canopy_animation_bible.py` (tabela da Animation Bible em `docs/design/canopy_breaker.md`).

## Micos de Casca (lacaios do Quebra-Copas) — 2026-10-29

- GameTestServer direto: `CanopyBreakerGameTests` agora com 26 testes obrigatórios (17 do boss, 8 dos lacaios, 1 do framework), **VALIDADO** em 12 execuções seguidas. Regressão: Basilisco 17/17, Litófago 11/11, Cobrador 18/18, Porrete 4/4.
- Gradle (`build`, `runCanopyBreakerTest`) e `runClient` não executados pelo agente. Ferramenta: `python tools/generate_bark_macaque_assets.py` (modelo, textura, ovo).
- Instabilidades de teste corrigidas pela raiz: chunks do terreno (e vizinhos do ídolo) precisam estar forçados para que os mobs processem IA; o jogador de servidor mock é invulnerável.

## Invocações, voo do porrete, intro e espíritos — 2026-10-29

- GameTestServer direto: Quebra-Copas 27/27 em 8 execuções seguidas (inclui intro com batida no peito e espírito na morte); Porrete 9/9 em 3 (voo na direção do olhar, empuxo sustentado, invocações que não se atacam); Cobrador 18/18 em 3 (espírito na morte); Basilisco 17/17; Litófago 11/11.
- Gradle e `runClient` não executados pelo agente: câmera/título da intro, transparência do espírito e sensação do voo pendentes de inspeção no jogo.

## Dragão Bebê — 2026-10-29

- `.\gradlew.bat --no-parallel --max-workers=1 build runBabyDragonTest`: **VALIDADO**. A suíte final tem **31 testes obrigatórios** (30 próprios e um do framework) e passou em **três execuções consecutivas**. Durante a implementação, a versão de 28 passou cinco vezes e a de 30 passou três vezes.
- Regressões isoladas: Basilisco **17/17**, Litófago **11/11**, Cobrador **18/18**, Quebra-Copas **27/27** e Porrete **9/9**, cada suíte com pelo menos **duas execuções aprovadas**. Houve falhas intermitentes anteriores em iluminação do Basilisco, ativação de chunk do Quebra-Copas e medição de dano do Porrete; testes antigos não foram alterados. Detalhes e limites em [design/baby_dragon_validation.md](design/baby_dragon_validation.md).
- A tarefa `runBabyDragonTest` usa `build/baby-dragon-gametest/` e só registra os testes com `coraline.babyDragonTests=true`. Executar as suítes separadamente evita sobrepor seis servidores pesados, sem alterar as configurações globais ou do projeto.
- `python tools/generate_baby_dragon_assets.py`, `.\gradlew.bat -I tools/baby-dragon-sketches.gradle exportBabyDragonSketchModel` e `python tools/generate_baby_dragon_sketches.py`: **VALIDADOS**. Geram atlas, sprites, geometria/UV reais, 144 amostras de animação, três gravuras e prévias PNG/GIF em `build/baby-dragon-sketches/`.
- `runClient`: **EXECUTADO**, duas sessões no diretório descartável `build/baby-dragon-preview`, com cópia do mundo dos testes. Modelo, textura, voo, felicidade/domesticação, ombro e persistência ao reabrir observados; primeira pessoa e índice corrigidos após inspeção. Capítulo completo nos dois idiomas conferido, com as três gravuras. Screenshots em `build/baby-dragon-preview/screenshots/`. Nenhum mundo de `run/saves/` usado.
- **Não validados integralmente:** áudio, combate prolongado/balanceamento e sessão multijogador com dois clientes reais. A expansão discreta do peito na preparação foi adicionada após a sessão de cliente, validada pelo bake e prévias offline. Todas essas distinções constam no relatório.

## Quebra-Copas: golpes de gorila e pegada do porrete — 2026-10-31

- GameTestServer direto: Quebra-Copas 34/34 em 7 execuções seguidas (inclui geometria do porrete, guarda quebrada, agarrar e arremessar, soco no chão e salto de chão, também até um pilar); Porrete 9/9, Cobrador 18/18, Basilisco 17/17, Litófago 11/11.
- Gradle e `runClient` não executados pelo agente; a pegada, as animações novas e a sensação do arremesso foram vistas apenas em renderizações estáticas do modelo real.

## Quebra-Copas: lançamentos — 2026-10-31

- Revés lança o jogador para trás (empuxo 1,0) e para cima (elevação 0,6), no sentido do golpe; Varredura e Pancada também elevam (0,35 e 0,3). Agarrar e Arremessar sobe mais (elevação 1,1); Soco no Chão joga para o alto (0,9).
- GameTestServer direto: Quebra-Copas 35/35 em 3 execuções seguidas (novo teste: o revés realmente sobe e voa para trás, medido por deslocamento, não por velocidade instantânea, que o atrito do chão corta em um tick).
- Não verificado no jogo: a altura e a distância reais dos lançamentos com o `runClient`.

## Comandante Carmesim — 2026-11-03

- GameTestServer direto (classes compiladas com `javac`, recursos copiados, `neoforge.mods.toml` expandido à mão; o Gradle falhou de novo com `Unable to establish loopback connection` em `compileJava`): **VALIDADO**, `CrimsonCommanderGameTests` com 32 testes do mod mais 1 do framework aprovados em 8 execuções seguidas com o código final.
- Regressão pelo mesmo lançador: Quebra-Copas 35/35, Porrete 9/9, Cobrador 18/18, Basilisco 17/17, Litófago 11/11 e Dragão Bebê 38/38. Numa das rodadas o servidor do Porrete caiu com o mesmo NPE de agendamento aninhado (falha antiga dos testes do Porrete, que não foram alterados); repetido duas vezes, 9/9 nas duas.
- Instabilidades encontradas e corrigidas pela raiz: (1) agendar esperas dentro de um `runAfterDelay` modificava o mapa de tarefas do GameTest enquanto ele era percorrido e derrubava o servidor (NPE no fastutil): os testes agora registram uma única bomba por tick no início e as esperas entram numa lista dela; (2) entidades criadas antes de o chunk do lote entrar em ticking ficavam paradas e invisíveis às buscas: cada teste espera o lote ficar ativo (`plot`); (3) lotes maiores que a célula de 19 blocos de uma arena invadiam a vizinha (uma investida acertou o golem de outro teste; uma Sentinela atacou o alvo do vizinho): tudo ficou dentro de x e z de 0 a 18, ou num lote distante, e todo Comandante recebe um alvo próprio e próximo.
- Revisão de código independente (leitura): sem defeitos críticos; corrigidos os médios (anéis do golpe pesado no ponto da lâmina, guarda que girava instantaneamente, mira da investida congelada e linha reconferida no comprometimento, espada que acertava montaria, pets e aldeões, animação inicial que não começava no cliente) e os leves de maior impacto (ondas na transição, morte cancelada por outro mod, salvamento no meio da transição ou da morte, UUID inválido, recarga antes do desgaste, investida após a morte, sentinela que podia golpear o ar).
- `.\gradlew.bat build` e `.\gradlew.bat runCrimsonCommanderTest`: **não executados pelo agente**; rode no terminal. `runClient` pendente: modelo, animações em movimento, partículas, sons, barra de vida e gravuras não foram vistos no jogo.
- Ferramentas de ativos: `python tools/generate_crimson_commander_assets.py` (modelo, texturas do Comandante e das Sentinelas, ícones, espada 3D; prévia do atlas em `build/crimson-commander-assets/atlas_preview.png`), `python tools/generate_boss_bar_crimson_commander.py` (barra; prévia em `build/crimson-commander-bossbar/review.png`), `.\gradlew.bat -I tools/crimson-commander-sketches.gradle exportCrimsonCommanderSketchModel` seguido de `python tools/generate_crimson_commander_sketches.py` (gravuras; nesta sessão o exportador foi compilado e executado direto com `javac`/`java`, sem Gradle) e `python tools/generate_crimson_animation_table.py` (tabela de animações em `docs/design/crimson_commander.md`).

## A Bruxa dos Dez Fios — 2026-11-05

- GameTestServer direto (classes compiladas com `javac`, recursos copiados, `neoforge.mods.toml` expandido à mão; o Gradle não roda no shell do agente): **VALIDADO**, `TenThreadsWitchGameTests` com 30 testes do mod aprovados em 6 execuções seguidas com o código final, mais 2 execuções de confirmação depois dos últimos ajustes de texto (cada execução leva cerca de 40 s).
- Regressão pelo mesmo lançador: Comandante Carmesim 34/34, Quebra-Copas 35/35, Porrete 9/9, Cobrador 18/18, Basilisco 17/17, Litófago 11/11, Dragão Bebê 81/81 (execuções: Comandante Carmesim 5 execução(ões), Quebra-Copas 1 execução(ões), Porrete 1 execução(ões), Cobrador 1 execução(ões), Basilisco 1 execução(ões), Litófago 1 execução(ões), Dragão Bebê 1 execução(ões)).
- Instabilidades encontradas e corrigidas pela raiz durante o desenvolvimento: (1) o hitbox do fio era criado na origem do mundo, possivelmente descarregada, e só depois movido: agora é posicionado antes de entrar no nível; (2) a limpeza da Matriarca removia **qualquer** hitbox a 80 blocos (inclusive os de outros encontros): passou a remover só os dos seus fios; (3) a arena empurrava qualquer jogador entre 28 e 68 blocos: agora só quem entrou; (4) o salto das marionetes dependia do atrito e quase nunca caía onde mirava: agora é guiado até o ponto travado e cai no fim do voo; (5) as ondas do Sino deixavam um vão de 0,14 bloco entre anéis; (6) ataques de longo alcance dos testes atingiam os alvos de outros testes na célula vizinha: os testes de ataque rodam em lotes distantes.
- `.\gradlew.bat build` e `.\gradlew.bat runTenThreadsWitchTest` (tarefa nova, `build/ten-threads-gametest/`, `-Dcoraline.tenThreadsTests=true`): **não executados pelo agente**; rode no terminal. `runClient` pendente: modelos, animações em movimento, partículas, sons, barra de vida e gravuras não foram vistos no jogo.
- Ferramentas de ativos: `python tools/generate_ten_threads_witch_assets.py` (modelos, atlas, camadas emissivas, `TenThreadsParts.java`, ícones, cajado 3D), `python tools/generate_boss_bar_ten_threads_witch.py` (barra; prévia em `build/ten-threads-bossbar/review.png`), `.\gradlew.bat -I tools/ten-threads-witch-sketches.gradle exportTenThreadsSketchModel` seguido de `python tools/generate_ten_threads_witch_sketches.py` (gravuras; nesta sessão o exportador foi compilado e executado direto com `javac`/`java`, sem Gradle; prévia em `build/ten-threads-sketches/review.png`).

### Remodelagem dos modelos da Bruxa dos Dez Fios — 2026-11-05

- `python tools/generate_ten_threads_witch_assets.py` agora contém as caixas novas e os pintores de material (`MATS`); atlas de 256 × 256 com folga (no máximo 95 de 256 linhas usadas); `TenThreadsParts.java` é regenerado. Depois de regenerar, recompile e rode a suíte; para as gravuras do Bestiário, exporte as poses (`ExportTenThreadsWitchModel`) e rode `python tools/generate_ten_threads_witch_sketches.py`.
- Validação: `TenThreadsWitchGameTests` 30/30 no GameTestServer direto com os modelos novos; `validate_assets.py` sem problemas. Renders offline (`build/ten-threads-sketches/models_final_*.png`) mostram repouso, preparo do fio carmesim e enroscada das 11 criaturas.

### Animação da Matriarca — 2026-11-05

- `.\gradlew.bat runMatriarchAnimationTest` executa os 20 testes do sistema de animação em `build/matriarch-animation-gametest/` (propriedade `coraline.matriarchAnimTests=true`). Status: **não executado via Gradle pelo agente**; a suíte foi executada pelo lançador direto do GameTestServer (18/18 seis vezes; depois do ajuste da fúria, 20/20 três vezes), assim como a da Bruxa (30/30).
- Os clipes ficam em `client/ten_threads_witch/matriarch/*Clips.java` (ticks, graus, pixels; dedos por flexão). Depois de mudar o rig no gerador, rode `python tools/generate_ten_threads_witch_assets.py` e a suíte de animação (ela confere que todo clipe só usa articulações que existem).
- Para calibrar no jogo: `/tick rate 5` deixa as transições lentas o bastante para ver saltos de pose; `/summon codenamecoraline:ten_threads_witch` com um jogador perto começa a entrada.

## Corvo Feérico (`fey_raven`) — 2026-11-05

- `.\gradlew.bat build`: **VALIDADO em 2026-11-05** (`BUILD SUCCESSFUL`, log `build/logs/fatia4_build.log`).
- `.\gradlew.bat runFeyRavenTest`: **VALIDADO em 2026-11-05** em **4 execuções consecutivas** (`All 9 required tests passed :)` — 8 testes em `FeyRavenGameTests` mais 1 do framework; logs `build/logs/fatia4_round1..4.log`).
- Regressão: `.\gradlew.bat runDeadrootClubTest` **VALIDADO em 2026-11-05** (`All 9 required tests passed :)`, log `build/logs/fatia4_deadroot_regression.log`), confirmando que a integração de `PlayerSummon` não afetou as invocações existentes.
- Cobertura de `FeyRavenGameTests`: (1) registro de entidade/ovo, atributos (18 PV, armadura 2, velocidade terrestre 0,25, voo 0,55, ataque 3,0, hitbox 0,6×0,8) e bake do modelo/animações; (2) máquina de estados e regras de spawn; (3) domesticação com `glow_berries`, cura (+4 PV) e alternância `FOLLOW`/`STAY`; (4) defesa do dono (`PECK_ATTACK` contra agressores e alvos do dono) e proteção mútua contra fogo amigo (`PlayerSummon`); (5) coleta automática de itens brilhantes (`FETCH_ITEM` para o slot do pergaminho) e empoleiramento no ombro (`PERCH_SHOULDER`) com ejeção ao entrar na água; (6) Sentinela Mística revelando hostis invisíveis com `GLOWING` (`SENTINEL_ALERT`) e persistência NBT do inventário/dono/comando; (7) retorno em emergência com vida baixa (`<= 30%`) e espera pacífica em `SITTING` com dono ausente; (8) capítulo `Bestiary.FEY_RAVEN` (6 páginas), item `fey_feather`, sons `ModSounds.FEY_RAVEN_*`, 3 conquistas e validação das texturas/gravuras PNG empacotadas.
- Detalhe técnico resolvido: `LivingEntity.addEffect(MobEffects.INVISIBILITY)` só atualiza a flag `isInvisible()` quando `LivingEntity.tick()` roda `updateInvisibilityStatus()`. Em `FeyRaven.tickMysticSentinel()`, checar `threat.isInvisible() || threat.hasEffect(MobEffects.INVISIBILITY)` garante detecção imediata mesmo no mesmo tick em que o efeito é aplicado.
- Geração reprodutível de recursos:
  1. `python tools/generate_fey_raven_assets.py` — valida 0 sobreposições de UV entre as 23 caixas e gera `fey_raven.png` (128×128), `fey_raven_eyes.png` (128×128), `fey_raven_spawn_egg.png` (16×16) e `fey_feather.png` (16×16).
  2. `.\gradlew.bat -I tools/fey-raven-sketches.gradle exportFeyRavenSketchModel` e `python tools/generate_fey_raven_sketches.py` — exportam a geometria 3D real em 8 poses e geram `build/fey-raven-sketches/pose_*.png` e as 3 gravuras `textures/gui/bestiary/fey_raven_{anatomy,combat,spoils}.png`.
- **Pendente:** inspeção em sessão real de `runClient` (voo, empoleiramento no ombro em 1ª/3ª pessoa, áudio e paginação do Bestiário em jogo).

## Salamandra de Enxofre (`brimstone_salamander`) — 2026-11-06

- `.\gradlew.bat runBrimstoneSalamanderTest`: **VALIDADO em 2026-11-06** em **4 execuções consecutivas** (`All 6 required tests passed :)` — 5 testes em `BrimstoneSalamanderGameTests` mais 1 do framework; logs `build/logs/fatia4_round1..4.log`).
- Cobertura de `BrimstoneSalamanderGameTests`:
  1. `attributesAndRegistration`: registro de entidade, ovo, projétil `brimstone_spit`, item `brimstone_scale`, atributos (44 PV, 8 armadura, 2 tenacidade, velocidade 0,23, dano 6,0, repulsão 0,65, alcance 24), sensibilidade à água, imunidade a fogo/lava, bake do modelo (18 juntas, 26 caixas) e existência das 17 animações, capítulo `Bestiary.BRIMSTONE_SALAMANDER` (6 páginas) e 7 sons de `ModSounds`.
  2. `spawnRulesAndImmunities`: redução de 60% em dano de queda, rejeição de spawn fora de Y 24–48, e aceitação de spawn natural sobre basalto na faixa de camada.
  3. `snapBiteAndAntiWhiff`: impacto da mordida aos 8+2 ticks aplicando 7,0 de dano e chamas, transição para recuperação (12 ticks), e validação anti-whiff aos 7 ticks de preparo (cancela e entra em recarga curta de 15 ticks quando o alvo recua além de 2,6 blocos).
  4. `sulfurSpitAndTarPool`: detecção de alvo em pilares (anti-pillaring a 2–5 blocos acima), disparo balístico de `BrimstoneSpit` e transformação no impacto em Poça de Alcatrão Ardente (2×2 blocos por 100 ticks) aplicando Lentidão II e fogo a intrusos.
  5. `tailWhipAndTargetMatrix`: acionamento prioritário por anti-cerco (2+ inimigos a menos de 2 blocos) causando dano e repulsão horizontal em arco amplo; validação da matriz de alvos (ataca Iron Golems e filhotes de Strider, ignora Undead/Basilisco/Litófago/Corvo Feérico em neutro, e retalia contra Undead sob ataque direto).
- Geração reprodutível de recursos:
  1. `python tools/generate_brimstone_salamander_assets.py` — valida 0 sobreposições de UV no atlas 128×128 (26 caixas) e gera `brimstone_salamander.png`, `brimstone_salamander_eyes.png`, `brimstone_spit.png` e `brimstone_scale.png`.
  2. `.\gradlew.bat -I tools/brimstone-salamander-sketches.gradle exportBrimstoneSalamanderSketchModel` e `python tools/generate_brimstone_salamander_sketches.py` — exportam geometria 3D e geram 9 poses e as 3 gravuras `textures/gui/bestiary/brimstone_salamander_{anatomy,combat,spoils}.png`.
- **Pendente:** inspeção em sessão real de `runClient` (natação na lava, partículas de alcatrão, sons e leitura do Bestiário em jogo).

## O Arquiteto (`architect`) — fatia 3 — 2026-11-08

- **`spawnWithNoFreeWill` não desliga o cérebro:** ele só remove os goals; `customServerAiStep` continua rodando. O Arquiteto decide no `customServerAiStep`, então os testes determinísticos usam `setNoAi(true)`; as linhas do tempo dos ataques ficam em `tick()` e rodam mesmo assim. Sintoma antes da correção: o chefe "sem vontade" atacava o golem de teste por conta própria.
- **Teto de barreira nas arenas de GameTest:** com `TestData(..., required)` o framework fecha a arena com barreiras, inclusive um teto na altura da estrutura (6 na `basilisk_test_arena`). A Queda da Gárgula exige 4 blocos livres acima dele e era cancelada. Os testes do Arquiteto usam o construtor completo com `skyAccess = true`.
- **Arena encostada na parede:** um golem de 1,4 de largura no bloco z = 11 invadia a parede de barreira e sufocava (1 de dano a cada poucos ticks). Mantenha as vítimas a um bloco da borda.
- **Pouso e espaço só contra blocos:** `level.noCollision(entity, box)` também testa entidades; a checagem de pouso usa `noBlockCollision` para não falhar sobre o próprio alvo.

## O Arquiteto (`architect`) — fatia 4 — 2026-11-08

- **Jogador mock para testar dano:** `GameTestHelper.makeMockServerPlayerInLevel()` devolve um `ServerPlayer` com `gameMode()` fixo em CRIATIVO e sem cliente carregado, e `ServerPlayer.isInvulnerableTo` recusa todo dano enquanto `!connection.hasClientLoaded()`. Por isso o `LivingIncomingDamageEvent` nunca dispara (sintoma: nenhum golpe acertava o jogador, nem pelas costas). Para dano real use `makeMockPlayer(GameType.SURVIVAL)` (um `Player` comum; a dificuldade do servidor de teste é NORMAL, então dano de mob não é escalado).
- **Estado de item por jogador em testes:** a cimitarra avança no `ServerTickEvent.Post` (não no tick do jogador), porque jogadores mock não são tickados.
- **Assets da cimitarra e gravuras:** `python tools/generate_architect_scimitar_assets.py` e depois `python tools/generate_architect_bestiary.py` (este lê `build/architect-sketches/model.json`, gerado por `.\gradlew.bat -I tools/architect-sketches.gradle exportArchitectSketchModel`). Status: **executados em 2026-11-08**; a aparência no jogo só se valida no `runClient`.
