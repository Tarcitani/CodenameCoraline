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

## Testes

- Não há testes automatizados (JUnit ou gametests) no projeto.
- A verificação atual é a compilação (`build`) e a carga do mod nas execuções de desenvolvimento.
- Quando houver lógica testável, decida o framework em `docs/DECISIONS.md` antes de adicioná-lo.

## Depuração

- Logs: `run/logs/latest.log` (cliente e servidor).
- Procure a linha `codenamecoraline loaded` para confirmar que o mod carregou.
- Para depurar no IDE: importe o projeto como Gradle, aguarde a sincronização e use as run configurations geradas pelo plugin.

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
