# Roadmap técnico

Estado da fundação, verificações realizadas e pendências. Não contém escopo criativo do mod: ele ainda não foi definido.

## Estado inicial (fundação)

- [x] Diretório `C:\MinecraftDev\CodenameCoraline` criado; estava vazio, sem repositório Git.
- [x] Projeto Gradle com NeoForge 26.1.2 (ver `docs/DECISIONS.md`, D-001).
- [x] Gradle Wrapper copiado do template oficial `NeoForgeMDKs/MDK-26.1.2-ModDevGradle`.
- [x] Ponto de entrada `com.codenamecoraline.CodenameCoraline` e metadados `neoforge.mods.toml`.
- [x] Documentação: `README.md`, `CLAUDE.md`, `docs/ARCHITECTURE.md`, `docs/DEVELOPMENT.md`, `docs/DECISIONS.md`, `docs/ROADMAP.md`.
- [x] Repositório Git local inicializado (branch `main`), sem commit.
- [x] Repositório remoto privado criado e vazio: `https://github.com/Tarcitani/CodenameCoraline` (`origin` configurado; nada enviado).
- [ ] Compilação `./gradlew build` executada com sucesso — ver verificações abaixo.

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
| 2026-10-27 | Compilação do projeto | **Não validada.** Nenhum código foi compilado com sucesso. |
| 2026-10-27 | Auditoria Git (`git add -n`, varredura por padrões de segredo) | Nenhum segredo encontrado; `.omc/` ignorado; `gradle-wrapper.jar` versionável |

## Pendências

1. **Compilação não validada.** O Gradle não executa neste ambiente (loopback, ver `docs/DEVELOPMENT.md`). Validar com `gradlew build` fora do sandbox ou após resolver a causa.
2. **Autorização para o JDK 25.** O plugin foojay baixa o JDK 25 para `%USERPROFILE%\.gradle\jdks`. Isso depende de acesso a `api.adoptium.net`/`foojay` e foi autorizado pelo usuário.
3. **Execução em jogo.** `runClient` e `runServer` não foram executados.
4. **Licença.** Definir a licença real antes de publicação aberta (provisório: All Rights Reserved).
5. **Group ID e nome do pacote.** `com.codenamecoraline` é provisório. Confirmar se deve usar outro domínio.
6. **Escopo criativo.** Não definido. Nenhum sistema de gameplay foi iniciado.
7. **Testes automatizados.** Nenhum framework escolhido (ver D-005).
8. **Commit inicial e publicação no GitHub.** Aguardam confirmação explícita do usuário.
