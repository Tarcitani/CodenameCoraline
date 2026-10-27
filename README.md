# Codename Coraline

Mod de Minecraft Java para NeoForge. Nome provisório. Atualmente contém apenas a fundação técnica do projeto: build, metadados e ponto de entrada. **Não há funcionalidades de gameplay implementadas.**

## Versões

| Componente | Versão | Observação |
|---|---|---|
| Minecraft | 26.1.2 | `minecraft_version` em `gradle.properties` |
| NeoForge | 26.1.2.124 | `neo_version` em `gradle.properties` |
| Java (toolchain) | 25 | Exigido pelo Minecraft 26.1.2 |
| Gradle Wrapper | 9.2.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Plugin ModDevGradle | `net.neoforged.moddev` 2.0.148 | `build.gradle` |

Decisões e motivos: [docs/DECISIONS.md](docs/DECISIONS.md).

## Pré-requisitos

- Windows 10/11 (ambiente de referência).
- Git.
- Java 25 **ou** conexão com a internet para o Gradle provisionar o JDK 25 automaticamente (plugin `foojay-resolver`).
- Conexão com a internet no primeiro build (Minecraft, NeoForge e dependências são baixados).
- Não é necessário instalar o Gradle: use o wrapper (`gradlew`).

## Preparação do ambiente

```bash
git clone <url-do-repositório>
cd CodenameCoraline
```

Não há configuração global exigida. Caches do Gradle ficam em `%USERPROFILE%\.gradle`.

## Comandos básicos

```bash
./gradlew build        # compila e gera o jar em build/libs
./gradlew compileJava  # compila o código-fonte
./gradlew runClient    # instância de desenvolvimento do cliente
./gradlew runServer    # servidor de desenvolvimento
```

No PowerShell use `.\gradlew.bat build`. Detalhes, testes e diagnóstico de erros: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Estrutura

Resumo em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Instruções para sessões de IA: [CLAUDE.md](CLAUDE.md).

## Estado atual

- Build configurado com NeoForge 26.1.2 e Java 25.
- Ponto de entrada `com.codenamecoraline.CodenameCoraline` (`@Mod`).
- Metadados `neoforge.mods.toml` gerados a partir de `gradle.properties`.
- Testes automatizados: não existem ainda.
- Status de validação por comando: ver [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) e [docs/ROADMAP.md](docs/ROADMAP.md).

## Licença

Todos os direitos reservados (provisório; ver pendências em [docs/ROADMAP.md](docs/ROADMAP.md)).
