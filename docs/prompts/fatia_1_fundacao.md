# Prompt: Fatia 1 — Fundação

Modelo sugerido: Sonnet ou Haiku. Substitua `<id>` e cole a seção entre as linhas.

=== INÍCIO ===

MISSÃO: executar a **Fatia 1 (Fundação)** do mob `<id>`.

Leia, nesta ordem e nada além disso:
1. `AGENTS.md` (seção Memória do projeto).
2. `docs/MOB_CONTRACT.md` (regras 1 a 6 e §8).
3. `docs/design/<id>.md` (ficha e Estado de implementação).
4. `docs/ARCHITECTURE.md`, seção "Anatomia de um mob". Os arquivos do mob de referência indicado na ficha (`entity/<ref_pkg>/<Ref>.java` e o registro dele em `registry/ModEntities.java`). Use o `pkg` da ficha para os pacotes novos (não o `<id>`).

Pré-condição: "0. Ficha revisada" está concluída. Se não estiver, pare e avise. Se a ficha tiver `<...>`, pare e pergunte.

Faça:
- Plano de 10 linhas dos arquivos que vai criar/alterar. Aguarde "ok" do usuário.
- `EntityType` e atributos em `registry/ModEntities.java` (incluindo `EntityAttributeCreationEvent` e `RegisterSpawnPlacementsEvent`).
- Classe da entidade `entity/<pkg>/<Mob>.java` com `enum State` e esqueleto, sem comportamento de combate.
- Ovo de spawn (`ModItems`, item JSON, modelo e textura de item), aba criativa (`ModCreativeTabs`), `biome_modifier` e tags, se a ficha pedir.
- Textos em `lang/en_us.json` e `lang/pt_br.json` (nome, ovo).
- Run config de GameTest em `build.gradle` (`<id>Test`, `coraline.<id>Tests`) e `verification/<Mob>GameTests.java` com teste de registro e de spawn, protegido por `Boolean.getBoolean("coraline.<id>Tests")`.

Comando de aceite:
```bash
mkdir -p build/logs
./gradlew compileJava > build/logs/fatia1_compile.log 2>&1
./gradlew run<Mob>Test > build/logs/fatia1_test.log 2>&1
```
Leia só o trecho de erro dos logs. Se o Gradle falhar com `Unable to establish loopback connection`, **pare e peça ao usuário para rodar o comando** no terminal dele; não improvise lançador manual sem pedido (ver `docs/DEVELOPMENT.md`, 2026-11-03).

Ao final: marque "1. Fundação" como concluída em Estado de implementação (data, comando e resultado reais; se foi o usuário que rodou, diga isso), preencha "Próximo passo", sugira a mensagem de commit `feat(<id>): fatia 1 - fundacao` e PARE. Não commit sem autorização, não inicie a fatia 2.

=== FIM ===
