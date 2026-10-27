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
