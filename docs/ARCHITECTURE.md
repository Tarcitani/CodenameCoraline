# Arquitetura

Estado real do código em `src/`. Atualize este arquivo junto com qualquer mudança de estrutura.

## Estrutura de diretórios

```
CodenameCoraline/
├── build.gradle            # plugins, toolchain Java 25, run configs NeoForge, geração de metadados
├── settings.gradle         # plugin foojay (provisiona JDK) e repositórios do plugin
├── gradle.properties       # versões (Minecraft, NeoForge) e propriedades do mod
├── gradlew / gradlew.bat   # Gradle Wrapper
├── gradle/wrapper/         # distribuição e propriedades do wrapper
├── src/main/java/com/codenamecoraline/
│   └── CodenameCoraline.java   # ponto de entrada
├── src/main/templates/META-INF/
│   └── neoforge.mods.toml      # template de metadados (expandido pelo Gradle)
└── docs/                   # esta documentação
```

Não existem outros pacotes: eles serão criados quando um requisito real exigir (ver `docs/DECISIONS.md`).

## Ponto de entrada

- `com.codenamecoraline.CodenameCoraline`, anotada com `@Mod(CodenameCoraline.MOD_ID)`.
- Construtor sem argumentos. Hoje apenas registra uma mensagem de log.
- `MOD_ID = "codenamecoraline"` deve ser igual a `mod_id` em `gradle.properties` e ao `modId` em `neoforge.mods.toml`.

## Fluxo de inicialização

1. O NeoForge lê `META-INF/neoforge.mods.toml` no jar (gerado a partir de `src/main/templates`).
2. O NeoForge instancia a classe anotada com `@Mod`.
3. O construtor do mod executa; nenhum evento de ciclo de vida (`FMLCommonSetupEvent` etc.) é registrado ainda.

## Mecanismos de registro

Nenhum registro de conteúdo existe ainda. Quando houver itens, blocos ou entidades, a regra é:
- Usar `DeferredRegister` do NeoForge, criado com o `MOD_ID`.
- Registrar no event bus do mod dentro do construtor do ponto de entrada (ou de uma classe de registro dedicada, se o volume justificar).

## Metadados

`src/main/templates/META-INF/neoforge.mods.toml` usa placeholders `${...}` preenchidos pela tarefa `generateModMetadata` a partir de `gradle.properties`. O resultado vai para `build/generated/sources/modMetadata`, que entra em `sourceSets.main.resources`. **Não edite o arquivo gerado em `build/`.**

Dependências declaradas: `neoforge` (faixa a partir de `neo_version`) e `minecraft` (faixa `minecraft_version_range`).

## Dependências importantes

| Dependência | Função | Origem |
|---|---|---|
| NeoForge 26.1.2.124 | Loader e APIs | `neoForge { version = neo_version }` |
| Minecraft 26.1.2 | Jogo | Declarado pelo plugin ModDevGradle |
| `net.neoforged.moddev` 2.0.148 | Ambiente de desenvolvimento e run configs | `build.gradle` |
| `foojay-resolver-convention` 1.0.0 | Provisiona JDK 25 se ausente | `settings.gradle` |
| Log (SLF4J via `com.mojang.logging`) | Logs | Fornecido pelo Minecraft |

Nenhuma biblioteca adicional foi incluída.

## Regras que futuras alterações precisam preservar

- `MOD_ID`, `mod_id` e `modId` devem permanecer idênticos.
- Não editar `build/generated/**`; alterações vão em `src/main/templates` ou `gradle.properties`.
- Versões de Minecraft e NeoForge mudam juntas, sempre verificadas (ver `docs/DECISIONS.md`).
- Java 25 é exigido pelo Minecraft 26.1.2; não reduza a toolchain sem nova decisão.
- Não adicionar lógica de gameplay em classes de renderização ou no ponto de entrada além de carregamento e registro.
- Não criar subprojetos Gradle sem justificativa registrada em `docs/DECISIONS.md`.
