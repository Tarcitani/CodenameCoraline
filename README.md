# Codename Coraline

Mod de Minecraft Java para NeoForge. Nome provisório. Contém o Redcap, o Basilisco, o Litófago, os bosses opcionais Cobrador da Última Dádiva e Quebra-Copas e um Bestiário com capítulos em português e inglês.

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
- Redcap com variantes, coordenação em grupo, itens e touca de disfarce.
- Basilisco hostil e comum em pântanos e cavernas, com grupos de 4–8, saltos, sopro antecipado, névoa de Decomposição III, repulsão por luz das almas e drops de perna/olho/rabo usados no suporte vanilla para preparar resistência ao efeito. Detalhes em [docs/design/basilisk.md](docs/design/basilisk.md).
- Litófago (Lithophage) hostil e incomum em Lush Caves e Dripstone Caves (`Y = -20` a `20`), com 7 estados de IA, 3 ataques sincronizados (Golpe de Escória, Varredura Tectônica, Estilhaço de Âmbar), 13 animações, ressonância sísmica em grupo, ovo personalizado e drops minerais. Detalhes em [docs/design/lithophage.md](docs/design/lithophage.md).
- Cobrador da Última Dádiva (Collector of the Last Gift) — boss colossal opcional de folclore invernal (~4,5 blocos), invocado à noite com o Sino Mudo (`silent_bell`) sobre gelo compactado em Taiga Nevada ou Bosque Nevado, com cinemática de chegada em trenó pelo céu (9,5 s), diretor de câmera e HUD no cliente com tecla de pulo, 2 fases, 4 ataques coreografados, 18 animações, conquista *A Última Entrega* e saque exclusivo. Detalhes em [docs/design/last_gift_collector.md](docs/design/last_gift_collector.md).
- O Quebra-Copas (The Canopy Breaker) — boss opcional de floresta antiga: fera simiesca arborícola de 3,1 blocos com o Porrete de Raiz-Morta, invocada à noite com o Ídolo do Tronco Oco em taiga antiga ou floresta sombria. Anda no chão, escala troncos, atravessa copas com saltos balísticos reais e despenca sobre o jogador (Queda da Copa). 200 PV, três estágios (60% e 25%), 6 ataques, 88 animações por keyframe, barra de vida temática, Bestiário de 6 páginas, quatro conquistas e saque exclusivo. Detalhes em [docs/design/canopy_breaker.md](docs/design/canopy_breaker.md).
- Bestiário ilustrado com informações de combate, habitat e recompensas das quatro criaturas.
- Metadados `neoforge.mods.toml` gerados a partir de `gradle.properties`.
- Testes de integração: `.\gradlew.bat runBasiliskTest`, `.\gradlew.bat runLithophageTest` e `.\gradlew.bat runLastGiftCollectorTest`, em mundos descartáveis dentro de `build/`.
- Status de validação por comando: ver [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) e [docs/ROADMAP.md](docs/ROADMAP.md).

## Licença

Todos os direitos reservados (provisório; ver pendências em [docs/ROADMAP.md](docs/ROADMAP.md)).
