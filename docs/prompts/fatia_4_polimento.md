# Prompt: Fatia 4 — Polimento e documentação

Modelo sugerido: Sonnet ou Haiku (trabalho de dados e texto).

=== INÍCIO ===

MISSÃO: executar a **Fatia 4 (Polimento e docs)** do mob `<id>`.

Leia, nesta ordem e nada além disso:
1. `AGENTS.md` (Memória do projeto) e `docs/MOB_CONTRACT.md` §1, §6.4 e §8.
2. `docs/design/<id>.md`: seção 1.5 e Estado de implementação.
3. Para copiar o formato: o mob de referência em `bestiary/Bestiary.java`, `registry/ModSounds.java`, `loot_table/entities/<ref>.json`, `advancement/` e `docs/ARCHITECTURE.md` (seção do mob de referência).

Pré-condição: "3. IA e combate" concluída. Se não, pare e avise.

Faça:
- Tabela de saque `data/codenamecoraline/loot_table/entities/<id>.json` e itens/receitas especiais da ficha.
- Sons: eventos em `ModSounds` e `sounds.json` reutilizando áudios vanilla (D-010); legendas em `lang/`. Use só eventos de som que já aparecem em `sounds.json`/`ModSounds` de outro mob ou que você confirmou existir na API do 26.1.2. A ficha traz apenas o caráter do som; **não use IDs inventados**.
- Bestiário: capítulo de 6+ páginas em `Bestiary.java`; gravuras 112×80, 112×72 e 112×64 geradas por `tools/generate_<id>_sketches.py`.
- Conquistas de história (boss: em `advancement/bosses/`), textos em `en_us.json` e `pt_br.json`.
- Documentação: `docs/ARCHITECTURE.md` (seção do mob), `docs/DECISIONS.md` (decisões grandes), `docs/ROADMAP.md`, `docs/CURRENT_STATE.md` (resumo), `docs/DEVELOPMENT.md` (comandos validados).
- Validação final: N rodadas consecutivas, com N do Perfil da ficha (`boss` 6; `hostil`/`neutro`/`aliado` 4; `invocação` 3).
Bash:
```bash
mkdir -p build/logs
./gradlew build > build/logs/fatia4_build.log 2>&1
for i in $(seq 1 N); do ./gradlew run<Mob>Test > build/logs/fatia4_round$i.log 2>&1 || { echo "FALHOU rodada $i"; break; }; done
```
PowerShell (o terminal padrão no Windows):
```powershell
New-Item -ItemType Directory -Force build/logs | Out-Null
.\gradlew.bat build *> build/logs/fatia4_build.log
foreach ($i in 1..N) { .\gradlew.bat run<Mob>Test *> "build/logs/fatia4_round$i.log"; if ($LASTEXITCODE -ne 0) { "FALHOU rodada $i"; break } }
```
Rode também as suítes de regressão dos mobs que dividem código (ex.: `PlayerSummon`, `BossSpirit`, `ThemedBossBars`). Falhou uma rodada: reinicie a contagem depois de corrigir a causa raiz.

Se o Gradle falhar com `Unable to establish loopback connection`, peça ao usuário para rodar `build` e as N rodadas no terminal dele e colar o resultado; não declare a fatia concluída sem esse resultado.

Ao final: atualize Estado de implementação, sugira `feat(<id>): fatia 4 - polimento e docs` e PARE. Não commit sem autorização.

=== FIM ===
