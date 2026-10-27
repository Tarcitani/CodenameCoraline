# Desenvolvimento

Procedimentos de compilação, execução, testes e diagnóstico. Cada comando tem um status. **Só marque "validado" após executar o comando com sucesso e registrar a data.**

## Compilação

| Comando | Função | Status |
|---|---|---|
| `./gradlew build` (PowerShell: `.\gradlew.bat build`) | Compila, executa verificações e gera `build/libs/codenamecoraline-<versão>.jar` | VALIDADO em 2026-10-27: `BUILD SUCCESSFUL` em `build.log`; jar `codenamecoraline-0.1.0-SNAPSHOT.jar` gerado com `neoforge.mods.toml` expandido e classe compilada em Java 25 |
| `./gradlew compileJava` | Compila apenas o código Java | VALIDADO em 2026-10-27 (incluído no `build`) |

Observação: o shell do agente de IA não consegue executar o Gradle (`Unable to establish loopback connection`). A validação acima foi feita fora dele, no terminal do usuário.

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

- Não há testes automatizados (JUnit ou gametests) no projeto.
- A verificação atual é a compilação (`build`) e a carga do mod nas execuções de desenvolvimento.
- Quando houver lógica testável, decida o framework em `docs/DECISIONS.md` antes de adicioná-lo.

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
