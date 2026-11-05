# Codename Coraline

Mod de Minecraft Java para NeoForge. Nome provisório. Contém o Redcap, o Basilisco, o Litófago, os bosses opcionais Cobrador da Última Dádiva, Quebra-Copas e Comandante Carmesim e um Bestiário com capítulos em português e inglês.

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

**Dragões em desenvolvimento:** os três estágios ainda não estão finalizados e precisam de muitas alterações e refinamentos, incluindo aparência, animações, combate, voo e validação em multiplayer. O estado e as pendências estão em [docs/ROADMAP.md](docs/ROADMAP.md) e [docs/design/dragon_remodel.md](docs/design/dragon_remodel.md).

- Build configurado com NeoForge 26.1.2 e Java 25.
- Redcap com variantes, coordenação em grupo, itens e touca de disfarce.
- Basilisco hostil e comum em pântanos e cavernas, com grupos de 4–8, saltos, sopro antecipado, névoa de Decomposição III, repulsão por luz das almas e drops de perna/olho/rabo usados no suporte vanilla para preparar resistência ao efeito. Detalhes em [docs/design/basilisk.md](docs/design/basilisk.md).
- Dragão companheiro domesticável com salmão: mesma entidade `baby_dragon`, evolui Bebê → Jovem → Adulto com XP próprio (500 e mais 1500), bolas progressivas, magma temporário seguro, Sopro Infernal, montaria e inventário vanilla (0/9/18 slots). Três anatomias próprias com pescoços, asas articuladas e caudas diferentes, mesma paleta original, texturas e emissão por idade e 17 animações próprias por estágio. Preserva acompanhamento, defesa, ombro do Bebê e geração natural. Controles em [docs/design/dragon_evolution.md](docs/design/dragon_evolution.md), remodelagem/prévias em [docs/design/dragon_remodel.md](docs/design/dragon_remodel.md); testes: `.\gradlew.bat runBabyDragonTest`.
- Litófago (Lithophage) hostil e incomum em Lush Caves e Dripstone Caves (`Y = -20` a `20`), com 7 estados de IA, 3 ataques sincronizados (Golpe de Escória, Varredura Tectônica, Estilhaço de Âmbar), 13 animações, ressonância sísmica em grupo, ovo personalizado e drops minerais. Detalhes em [docs/design/lithophage.md](docs/design/lithophage.md).
- Cobrador da Última Dádiva (Collector of the Last Gift) — boss colossal opcional de folclore invernal (~4,5 blocos), invocado à noite com o Sino Mudo (`silent_bell`) sobre gelo compactado em Taiga Nevada ou Bosque Nevado, com cinemática de chegada em trenó pelo céu (9,5 s), diretor de câmera e HUD no cliente com tecla de pulo, 2 fases, 4 ataques coreografados, 18 animações, conquista *A Última Entrega* e saque exclusivo. Detalhes em [docs/design/last_gift_collector.md](docs/design/last_gift_collector.md).
- O Quebra-Copas (The Canopy Breaker) — boss opcional de floresta antiga: fera simiesca arborícola de 3,1 blocos com o Porrete de Raiz-Morta, invocada à noite com o Ídolo do Tronco Oco em taiga antiga ou floresta sombria. Anda no chão, escala troncos, atravessa copas com saltos balísticos reais e despenca sobre o jogador (Queda da Copa). 200 PV, três estágios (60% e 25%), 6 ataques, 88 animações por keyframe, barra de vida temática, Bestiário de 6 páginas, quatro conquistas e saque exclusivo. Detalhes em [docs/design/canopy_breaker.md](docs/design/canopy_breaker.md).
- Comandante Carmesim (Crimson Commander) — boss opcional das câmaras profundas: cavaleiro carmesim de 3,5 blocos com espada larga, raro abaixo da camada 0 no escuro ou invocado com a Insígnia do Juramento Manchada sobre tijolos de ardósia. 220 PV, três estágios (60% e 25%), 5 ataques mais guarda com riposta, duas Sentinelas de Cinza no segundo estágio, 35 animações, barra de vida temática, Bestiário de 6 páginas, quatro conquistas e a Espada Larga do Juramento Carmesim (Investida Carmesim). Detalhes em [docs/design/crimson_commander.md](docs/design/crimson_commander.md); testes: `.\gradlew.bat runCrimsonCommanderTest`.
- A Bruxa dos Dez Fios (The Witch of Ten Threads) — boss composto de floresta antiga: Ester, a Matriarca Costureira (150 PV, 1,15 bloco), invocada com o Fuso Amaldiçoado em floresta escura, jardim pálido, taiga antiga ou floresta densa à noite. Dez marionetes (Aracnídea, Cão de Porcelana, Sino Oco, Cervo, Gralha, Noiva Oca, Centopeia, Mãe das Raízes, Carrasco, Gigante do Berço) lutam **uma por vez** enquanto ela é intocável; antes de cada especial o fio fica carmesim e pode ser cortado (marionete enroscada, ×1,75). Depois da décima, a Matriarca luta sozinha. Arena de 28 blocos com anti-pilar e anti-buraco, 13 animações por criatura, barra de vida temática, Bestiário de 8 páginas, cinco conquistas e o Cajado dos Dez Fios (três posturas). Detalhes em [docs/design/ten_threads_witch.md](docs/design/ten_threads_witch.md); testes: `.\gradlew.bat runTenThreadsWitchTest`.
- Bestiário ilustrado com informações de combate, habitat e recompensas das criaturas.
- Metadados `neoforge.mods.toml` gerados a partir de `gradle.properties`.
- Testes de integração: `.\gradlew.bat runBasiliskTest`, `.\gradlew.bat runLithophageTest` e `.\gradlew.bat runLastGiftCollectorTest`, em mundos descartáveis dentro de `build/`.
- Status de validação por comando: ver [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) e [docs/ROADMAP.md](docs/ROADMAP.md).

## Licença

Todos os direitos reservados (provisório; ver pendências em [docs/ROADMAP.md](docs/ROADMAP.md)).
