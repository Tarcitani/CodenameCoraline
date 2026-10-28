# Desenvolvimento

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
