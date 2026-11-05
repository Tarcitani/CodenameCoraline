# Prompt: Fatia 3 — IA de Companheiro / Aliado

Modelo sugerido: Opus ou Sonnet.
Substitua `<id>` e use para criaturas com Perfil `aliado` (ex.: companheiros domesticáveis).

=== INÍCIO ===

MISSÃO: executar a **Fatia 3 (IA de Aliado / Companheiro)** do mob `<id>`.

Leia, nesta ordem e nada além disso:
1. `AGENTS.md` (Memória do projeto) e `docs/MOB_CONTRACT.md` §1, §4, §6.
2. `docs/design/<id>.md`: seções 1.2 a 1.4b, 1.6 e Estado de implementação.
3. Referência no código: `entity/babydragon/BabyDragon.java` (domesticação, dono, inventário, seguir/sentar, proteção) e `verification/BabyDragonGameTests.java`.

Pré-condição: "2b. Revisão visual" aprovada. Se não, pare e avise.

Faça:
- Plano curto de IA e estados (domesticação, metas de companheiro, comandos, persistência de dono). Aguarde "ok".
- Domesticação e vínculo por UUID (`TamableAnimal`, salvando NBT do dono `Owner` de forma segura).
- Modos de comando com sincronia cliente-servidor:
  - Seguir o dono (`FollowOwnerGoal` com teletransporte seguro sem asfixia);
  - Sentar / esperar (`SitWhenOrderedToGoal`);
  - Defesa mútua (atacar agressores do dono e alvos que o dono ataca, sem ferir outros animais pacíficos sem envolvimento).
- Prevenção de dano amigo estrita: o dono não machuca o aliado sem querer; o aliado nunca machuca o dono nem aliados vinculados.
- Inventário próprio (se a ficha pedir, como no `DragonMenu`) ou montaria/ombro, salvando e dropando itens na morte.
- GameTests dedicados em `verification/<Mob>GameTests.java`:
  - Teste de domesticação por item;
  - Teste de comando seguir e esperar;
  - Teste de lealdade e proteção;
  - Teste de persistência de UUID de dono após reload NBT.

Comando de aceite:
```bash
./gradlew compileJava > build/logs/fatia3_compile.log 2>&1
./gradlew run<Mob>Test > build/logs/fatia3_test.log 2>&1
```

Ao final: atualize Estado de implementação (data, comando, resultado real), sugira `feat(<id>): fatia 3 - ia de companheiro` e PARE. Não commit sem autorização.

=== FIM ===

