# Prompt: Fatia 3 — IA e combate

Modelo sugerido: Opus ou Sonnet.

=== INÍCIO ===

MISSÃO: executar a **Fatia 3 (IA e combate)** do mob `<id>`.

Leia, nesta ordem e nada além disso:
1. `AGENTS.md` (Memória do projeto) e `docs/MOB_CONTRACT.md` §1, §3.2, §4, §5, §6, §7.
2. `docs/design/<id>.md`: seções 1.2 a 1.4, 1.6 (Parte A: ticks e tick do impacto) e Estado de implementação.
3. `entity/<ref_pkg>/<Ref>.java` (mob de referência; use o `pkg` da ficha para os pacotes novos) e `verification/<Ref>GameTests.java`.

Pré-condição: "2b. Revisão visual" aprovada. Se não estiver, pare e avise.

Faça:
- Plano curto: estados (`enum State`), goals e tabela de ataques mapeada para estados. Aguarde "ok".
- Máquina de estados explícita; cada ataque com as 4 fases (preparo, validação anti-whiff, impacto, recuperação) e ticks vindos da tabela da ficha, em constantes nomeadas.
- Servidor autoritativo: nenhuma lógica de dano no cliente. Sincronia visual por `broadcastEntityEvent` na faixa reservada na ficha (≥ 100) e `SynchedEntityData`.
- Predicados de alvo conforme as tags da ficha; histerese de ameaça; anti-cerco e anti-pillaring; rotina ociosa; idles sorteados a cada 100–200 ticks.
- Lacaios via `PlayerSummon`; boss: estágios, cinemática (`client/<pkg>/<Mob>CinematicDirector`, tecla `key.codenamecoraline.skip_cinematic`), imunidade temporária e barra temática (`ThemedBossBars` + `tools/generate_boss_bar_<id>.py`).
- Performance: sem alocação por tick no loop de IA, sem streams, buscas espaciais escalonadas (contrato §6).
- GameTests de IA e de cada ataque (acerto, fuga/anti-whiff, estágios).

Comando de aceite:
```bash
./gradlew compileJava > build/logs/fatia3_compile.log 2>&1
./gradlew run<Mob>Test > build/logs/fatia3_test.log 2>&1
```

Ao final: atualize Estado de implementação (data, comando, resultado real), liste o que ficou sem teste, sugira `feat(<id>): fatia 3 - ia e combate` e PARE. Não commit sem autorização.

=== FIM ===
