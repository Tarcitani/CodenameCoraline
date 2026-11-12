# Desenvolvimento

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

- `.\gradlew.bat runFireDragonTest` executa os 3 testes próprios da fundação do Dragão de Fogo (registro/atributos/recursos, spawn e dano), além do teste do framework, em `build/fire-dragon-gametest/` (`coraline.fireDragonTests=true`). **VALIDADO em 2026-11-11** pelo agente: `compileJava`, `build` e quatro rodadas consecutivas de `.\gradlew.bat runFireDragonTest runFeyRavenTest runBrimstoneSalamanderTest` aprovadas (totais do servidor: 4/4, 9/9, 23/23 por rodada). Logs em `build/logs/fire_dragon_fatia1_round_1.log` até `fire_dragon_fatia1_round_4.log`. Na fundação ainda não havia renderizador; a inspeção visual foi feita depois, na fatia 2 (seção abaixo). Ícone: `python tools/generate_fire_dragon_assets.py`, executado com sucesso.

- `.\gradlew.bat runBasiliskTest` executa os Game Tests do Basilisco em `build/basilisk-gametest/`, sem abrir os mundos do usuário. Não exige um framework externo (D-017).
- `.\gradlew.bat runDeadrootClubTest` executa os 7 testes da habilidade do Porrete de Raiz-Morta (carga, dano por altura, invocação com cap, aliados) em `build/deadroot-gametest/` (propriedade `coraline.deadrootTests=true`).
- `.\gradlew.bat runCanopyBreakerTest` executa os 17 testes do Quebra-Copas em `build/canopy-gametest/` (propriedade `coraline.canopyTests=true`). O terreno é construído por cada teste a Y relativo 100 e todos os chunks dele são forçados.
- `.\gradlew.bat runCrimsonCommanderTest` executa os 32 testes do Comandante Carmesim em `build/crimson-gametest/` (propriedade `coraline.crimsonTests=true`). Status: **não executado pelo agente** (o Gradle não abre a conexão de loopback no shell dele); a suíte foi validada pelo lançador direto do GameTestServer (ver a seção de 2026-11-03).
- `.\gradlew.bat runTenThreadsWitchTest` executa os 30 testes da Bruxa dos Dez Fios em `build/ten-threads-gametest/` (propriedade `coraline.tenThreadsTests=true`). Status: **não executado pelo agente** (Gradle indisponível no shell dele); a suíte foi validada pelo lançador direto do GameTestServer (ver a seção de 2026-11-05).
- `.\gradlew.bat runFeyRavenTest` executa os 8 testes do Corvo Feérico em `build/fey-raven-gametest/` (propriedade `coraline.feyRavenTests=true`). Status: **VALIDADO em 2026-11-05** em 4 rodadas consecutivas via Gradle (ver seção abaixo).
- `.\gradlew.bat runArchitectTest` executa os 53 testes do Arquiteto (fundação, revelação, IA e combate, conteúdo e Cimitarra, reforma: movimento, olhar, esquivas, escudo, alvo e balanceamento; as linhas `ARCHITECT_METRIC` do log trazem as medidas) em `build/architect-gametest/` (propriedade `coraline.architectTests=true`). Status: **VALIDADO em 2026-11-08** (fatia 4: 6 rodadas, 35 testes; reforma: `build runArchitectTest` com "All 54 required tests passed" em 1 execução final, mais ~8 execuções durante o ajuste), rodado pelo agente no terminal do app: no shell com sandbox o Gradle falha com `Unable to establish loopback connection`, mas no painel de terminal (fora da sandbox) funciona.
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

## O Arquiteto (`architect`) — Correção de orientação e corrida frontal — 2026-11-08

- **Ataque de costas ao jogador (desincronização de `lockedYaw`):** Durante ataques, esquivas, aparos e recuo (`locksFacing() == true`), `tick()` forçava `this.face(this.lockedYaw)` tanto no servidor quanto no cliente. Como `lockedYaw` era um campo Java simples sem sincronização via rede (`SynchedEntityData`), no cliente ele tinha sempre o valor padrão `0.0F` (Sul). Se o jogador estivesse a Norte, Leste ou Oeste, o chefe virava as costas no cliente durante a animação de ataque. Solução: criação do `DATA_LOCKED_YAW` (`SynchedEntityData.defineId(Architect.class, EntityDataSerializers.FLOAT)`), atualização em `setLockedYaw(...)` e leitura no cliente em `getLockedYaw()`. No método `face(yaw)`, eliminação do cálculo com `lead` residual em poses travadas, alinhando pontualmente `yRot`, `yRotO`, `yBodyRot`, `yBodyRotO`, `yHeadRot` e `yHeadRotO`.
- **Corrida de lado em combate (`Tactic.CIRCLE` vs `Tactic.ADVANCE`):** A tática `position()` selecionava `Tactic.CIRCLE` (strafe lateral com velocidade frontal de apenas `0.05` e lateral de `0.17`) sempre que a distância estivesse abaixo de 5,08 blocos. Além disso, o ataque de investida (`RUSHING_REBUKE`) travava o facing no `0.0F` desincronizado enquanto corria. Solução: quando fora de alcance corpo a corpo imediato (`dist > CLOSE_RANGE + 0.8D` ~2,9 m) ou quando pronto para engajar (`globalCooldown <= 0`), a tática agora é obrigatoriamente `Tactic.ADVANCE`, usando `RUN_SPEED` (1.1) para avançar de frente direto ao jogador; `Tactic.CIRCLE` é restrito à recuperação e proximidade imediata. A rotação de corpo durante perseguição aumentou de 12° para 20°/tick (`TURN_DEGREES_PER_TICK`).
- **Substituição imediata de alvo morto:** Quando o alvo atual morre enquanto o chefe executa um ataque longo (28–60 ticks), a IA adiava a seleção de novo alvo até o retorno a `COMBAT_IDLE`. Em `tick()`, adicionada verificação ativa: se `getTarget() != null && !isFoeInLeash(getTarget())`, `selectTarget(level)` é invocado imediatamente, selecionando o próximo inimigo vivo e mantendo a estabilidade de alvo sem atrasos.
- **Validação:** 4 rodadas consecutivas de `runArchitectTest` (54/54 testes passando com 0 flakes) e `build` verde.

## O Arquiteto (`architect`) — Desativação temporária do mob e drops — 2026-11-08

- **Decisão do usuário:** Mob e drops reprovados no teste de jogo pelo usuário e desativados temporariamente aguardando redesenho.
- **Isolamento de gameplay:**
  - Removidos de todas as abas criativas modded e vanilla (`ModCreativeTabs`).
  - Removido do Bestiário (`Bestiary.ENTRIES`).
  - Receita da Tábua dos Mandamentos bloqueada (`commandment_tablet.json` com `neoforge:false`).
  - Invocação manual inerte em ambiente normal (`CommandmentTabletItem.ENABLED = false`).
- **Status dos testes:** O código e assets continuam íntegros no repositório; `.\gradlew.bat build` verde e `runArchitectTest` (54/54) mantido operacional com ativação de testes (`coraline.architectTests=true`).

## Dragão de Fogo — visual em revisão (2026-11-11)

Executados pelo agente com sucesso:

1. `python tools/generate_fire_dragon_assets.py`: modelo Java, ícone e 125 skins; 63 retângulos UV sem sobreposição, atlas 128×128 e emissivo alinhado/transparente.
2. `python tools/generate_fire_dragon_animations.py`: 22 clipes, enum comum e tabela de ângulos/pivôs em `docs/design/fire_dragon_animation.md`.
3. `.\gradlew.bat compileJava -I tools/fire-dragon-sketches.gradle exportFireDragonSketchModel`: modelo real; validação de juntas em todos os ticks e exportação de 75 poses. `python tools/generate_fire_dragon_sketches.py`: três pranchas, 23 poses individuais e quatro GIFs, em `build/fire-dragon-sketches/`.
4. `build` e quatro rodadas de `.\gradlew.bat runFireDragonTest runFeyRavenTest runBrimstoneSalamanderTest`: 6/6, 9/9, 23/23 por rodada. Agora são 5 testes próprios do dragão mais 1 do framework. Logs `build/logs/fire_dragon_fatia2_round_1.log` a `_4.log`.
5. `.\gradlew.bat runClient`: inspecionados vermelho/dourado e preto/dourado, voo em prévia, preparo de fogo e escala junto ao jogador no mundo novo “Fire Dragon Visual Review”; encerramento normal, sem crash. Log `build/logs/fire_dragon_fatia2_client_final.log`. Aprovação visual humana ainda pendente; os comportamentos não foram implementados nesta fatia.

Comandos `/summon` e `VisualPreview` ficam na ficha. Não use `--quickPlaySingleplayer` como opção direta do Gradle: não é uma opção da tarefa; a inspeção usou o menu normal do jogo. O exportador Java deve estar em UTF-8 sem BOM (`javac` rejeita U+FEFF no início do arquivo).

## Dragão de Fogo — testes exclusivos do usuário (2026-11-11)

Por instrução explícita do usuário, TODOS os testes são feitos por ele, incluindo compileJava/build, GameTests e runClient. Não executar esses comandos como agente. Evidências anteriores permanecem históricas. Fatia 3 escrita sem compilação ou testes: executar primeiro `.\gradlew.bat compileJava`, depois `.\gradlew.bat runFireDragonTest` e `.\gradlew.bat runClient`. Suíte ampliada de 5 para 9 testes próprios, ainda não executada. Roteiro manual, limitações e regressões em `design/fire_dragon.md`, seção 3. Não marcar aceito antes dos resultados humanos. Ombro usa a entidade no mundo; incluir save/load, logout, dois dragões, papagaios/corvo e teto baixo nos testes.

### Ajustes de ombro/combate após relato do usuário

Causas no código: faltava excluir o bebê no ombro dos empurrões de `LivingEntity`; a seleção do mesmo agressor podia zerar novamente o preparo; aquisição dependia apenas de timestamps e a mira apontava para olhos sem antecipação. Alterações: bloqueios condicionais de empurrão/colisão, callback após dano aceito, manutenção do preparo, recuo em recarga, mira ao torso com previsão limitada e nova pose de ombro pelo gerador. Gerador de animações executado para atualizar os arquivos; **compileJava, GameTests e runClient não executados pelo agente**. Três regressões adicionais escritas (12 testes próprios no total), pendentes. Roteiro detalhado em `design/fire_dragon.md`, seção 4. Só marcar resolvido após resultados do usuário.

### Acompanhamento do ombro — ajuste visual pendente de teste

Usuário aprovou defesa, mira e pose do ombro; relatou atraso do dragão ao andar. O renderer agora compensa a posição interpolada atrasada da entidade com a posição interpolada atual do dono, por `getRenderOffset`, e acompanha a rotação corporal do jogador. A posição/colisão e a autoridade do servidor permanecem na entidade. Offset zerado fora do ombro para não afetar voo ou desembarque. Nenhuma compilação ou teste executado pelo agente. Usuário deve conferir andar/correr, girar e desembarcar, nos dois lados e com outro jogador em multiplayer. Nível recomendado: Medium. Sem commit.

### Visibilidade em primeira pessoa — aguardando usuário

Renderer reduz para 60% o tamanho visual do dragão no ombro apenas quando a câmera em primeira pessoa pertence ao dono. Terceira pessoa, câmera de outro jogador, tamanho real/colisão e comportamento permanecem iguais. A escala é recalculada a cada extração de render state para restaurar o tamanho ao alternar câmera ou desembarcar. Sem compilação, testes ou runClient pelo agente. Usuário deve conferir os dois ombros em primeira pessoa e alternar F5, além de observar outro jogador. Nível recomendado: Medium. Sem commit.

### Altura da miniatura em primeira pessoa — aguardando teste humano

Usuário relatou que a redução deixou o bebê baixo demais. A escala visual continua em 60%, mas o renderer compensa a altura usando uma âncora de 10/16 bloco junto ao pescoço: deslocamento vertical de (1 − escala) × altura da âncora × escala base, ou +0,25 bloco na escala base 1. Assim a miniatura encolhe em torno da região do pescoço, em vez dos pés. Compensação exclusiva da primeira pessoa do dono; posição real e terceira pessoa não mudam. Sem compilação ou testes pelo agente. Conferir altura e campo de visão ao andar/girar e alternar F5.

### Estabilização do ombro ao caminhar — teste humano pendente

Usuário aprovou o tamanho atual, mas ainda observou glitches durante a caminhada. A primeira pessoa agora ancora posição e yaw na própria câmera (`Camera.position()`/`yRot()`), em vez da interpolação do dono e do yaw corporal que varia ao andar/strafe. A rotação relativa residual da cabeça é zerada nessa visão. Escala 60% e compensação vertical anteriores preservadas; terceira pessoa continua presa ao corpo do dono. Nenhuma compilação ou teste executado pelo agente. Conferir caminhada frontal, corrida, movimento lateral e giro nos dois ombros, alternando F5; relatar se a oscilação persiste. Nível recomendado: High. Sem commit.
## Ceifeiro das Almas Vagas (`soul_reaper`) — 2026-11-08

- `.\gradlew.bat runSoulReaperTest`: **VALIDADO em 2026-11-08** em **6 execuções consecutivas** (`All 13 required tests passed :)` — 12 testes em `SoulReaperGameTests` mais 1 do framework; logs em `build/logs/fatia4_round1..6.log`) + `.\gradlew.bat build` verde (`build/logs/fatia4_build.log`) e regressão `.\gradlew.bat runLastGiftCollectorTest` verde (`build/logs/fatia4_regression_collector.log`).
- Cobertura de `SoulReaperGameTests` (12 testes):
  1. `soul_reaper_registration_and_attributes`
  2. `soul_reaper_immunities_and_anti_burst`
  3. `omen_hare_spawn_rules_and_wisp_binding`
  4. `soul_reaper_models_and_animations_baking`
  5. `omen_hare_metamorphosis_and_awakening`
  6. `soul_reaper_desolation_aura_and_frost`
  7. `soul_reaper_stages_healing_and_scaling`
  8. `soul_reaper_original_attacks_and_anti_whiff`
  9. `soul_reaper_frontal_attacks_and_exclusive_f4`
  10. `soul_reaper_seven_anti_instakill_rules`
  11. `soul_reaper_provenance_nullification_and_taunt`
  12. `soul_reaper_items_scythe_brooch_and_spirit`
- Problemas técnicos resolvidos durante a implementação:
  - **Relógio interno em testes diretos (`serverTicks` vs `Entity.tickCount`):** Em Minecraft, `Entity.tickCount` é incrementado por `ServerLevel.tickNonPassenger` fora de `entity.tick()`. Testes que invocam `reaper.tick()` diretamente em loop síncrono não avançavam `tickCount`, impedindo a resolução agendada de anulações (`T0 + 20t`). Solução: contador interno `serverTicks` incrementado no início de `SoulReaper.tick()`.
  - **Sobreposição de `EXCLUSION_RADIUS = 128` entre arenas paralelas do GameTestRunner:** O `GameTestRunner` instancia todas as arenas do batch lado a lado (~30 blocos de distância). Entidades não descartadas ao final de um teste bloqueavam `OmenHare.checkSpawnRules` em arenas vizinhas. Solução: descarte explícito (`.discard()`) ao final de cada teste e limpeza inicial na arena de teste de spawn.
- Geração reprodutível de recursos:
  1. `python tools/generate_soul_reaper_assets.py` — verifica 0 colisões de UV em `256×256` (`soul_reaper`), `64×64` (`omen_hare`) e `32×32` (`wandering_soul_wisp`) e gera texturas de entidades, ovos e itens (`soul_reaper_scythe`, `frost_spiral_brooch`, `ancient_ice_shard`, `frozen_soul_essence`, `soul_reaper_portrait`).
  2. `python tools/generate_boss_bar_soul_reaper.py` — gera `textures/gui/boss_bar/soul_reaper.png` (`256×64`).
  3. `.\gradlew.bat -I tools/soul-reaper-sketches.gradle exportSoulReaperSketchModel` e `python tools/generate_soul_reaper_sketches.py` — exportam 12 poses 3D e geram `build/soul-reaper-sketches/soul_reaper_pose_sheet.png`, `omen_hare_pose_sheet.png` e `textures/gui/bestiary/soul_reaper_{anatomy,combat,spoils}.png`.
- **Pendente:** validação visual e acústica interativa em sessão de `runClient`.
