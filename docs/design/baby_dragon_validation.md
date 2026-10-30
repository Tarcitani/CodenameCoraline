# Entrega e validação do Dragão Bebê — 2026-10-29

## A. Resumo executivo

Implementados: entidade `baby_dragon`, vínculo persistente com o dono, salmão para domesticação/cura, comandos sem GUI, acompanhamento terrestre/aéreo, órbita variável, pouso e saída do ombro com a entidade original, defesa reativa, centelha sem incêndios e com proteção de aliados, geração natural, ovo, sons vanilla, modelo original, textura detalhada, 12 animações nativas e capítulo do Bestiário em dois idiomas.

O build produz `build/libs/codenamecoraline-0.1.0-SNAPSHOT.jar`. Não houve commit ou push. As alterações anteriores do Basilisco e o diretório `bin/` preexistente foram preservados.

## B. Matriz de conformidade

| Requisito | Estado | Evidência |
|---|---|---|
| 1. Registro da entidade | PASSOU | `dragon_registration`: entidade, categoria e ovo reais |
| 2. Atributos iniciais | PASSOU | `dragon_attributes`: vida, armadura, velocidade, atributo de voo |
| 3. Spawn natural | PASSOU | `dragon_natural_spawn`: tabelas reais, grupos e recusa de espaço ocupado |
| 4. Domesticação | PASSOU | `dragon_taming_and_feeding`; interação com salmão também observada no cliente |
| 5. Persistência do dono | PASSOU | `dragon_owner_persistence`: round-trip NBT com UUID |
| 6. Acompanhamento terrestre | PASSOU | `dragon_ground_follow`: deslocamento efetivo em ticks reais |
| 7. Acompanhamento aéreo | PASSOU | `dragon_air_follow`: decolagem, ganho de altura e aproximação |
| 8. Voo orbital | PASSOU | `dragon_orbit_moves`: atividade e mudança efetiva de posição |
| 9. Pouso no ombro | PASSOU | `dragon_shoulder_approach`: pedido, aproximação e âncora |
| 10. Saída do ombro | PASSOU | `dragon_shoulder_exit` e `dragon_shoulder_unsafe_exit` |
| 11. Persistência após pouso | PASSOU | `dragon_shoulder_preserves_data`; ombro preservado ao reabrir o cliente |
| 12. Ausência de duplicação | PASSOU | `dragon_no_duplicate`: oito ciclos com o UUID original |
| 13. Ataque defensivo | PASSOU | `dragon_reactive_defense`, autodefesa selvagem e combate com neutros envolvidos |
| 14. Dano do projétil | PASSOU | `dragon_ember_damage`: impacto real de 3 HP, sem fogo posterior |
| 15. Proteção do dono | PASSOU | `dragon_owner_safe`: filtro de aliança e alvo; mesmo filtro na colisão/impacto |
| 16. Proteção de aliados | PASSOU | `dragon_allies_safe`: projétil real atravessa lobo do dono sem dano |
| 17. Recarga | PASSOU | `dragon_fire_cooldown`: preparação de 12 e intervalo de 60 ticks |
| 18. Fogo/queda | PASSOU | `dragon_fire_and_fall_immunity` |
| 19. Teletransporte seguro | PASSOU | `dragon_safe_teleport`: destino livre/chão firme e recusa de lava |
| 20. Ordem de ficar | PASSOU | `dragon_sit_command`: permanece no lugar; prioridade sobre defesa |
| 21. Salvamento/carregamento | PASSOU | `dragon_save_load`: entidade carregada no mundo mantendo dados e recarga |
| 22. Bestiário | PASSOU | Registro, seis páginas-base, gravuras e revisão de todas as páginas em pt-BR/en-US |
| 23. Animações/recursos | PASSOU | Bake das 12 animações no esqueleto real e presença dos PNGs; exportação de 144 amostras |
| Colisão e cenário | PASSOU | Centelha para em madeira sem quebrar/queimar; voo físico não atravessa parede |
| Alcance da centelha | PASSOU | `dragon_ember_range`: percurso limitado a 12 blocos |
| Modelo/texturas/ombro no cliente | PASSOU | Duas sessões de `runClient`, screenshots e correções verificadas |
| Leitura audiovisual completa/balanceamento | PARCIAL | Sem avaliação auditiva dos sons ou luta prolongada em terreno natural |
| Multijogador com dois clientes reais | PARCIAL | APIs de dono, equipes e PvP usadas; não houve sessão com dois clientes |

Não há requisito de código marcado como FALHOU ou BLOQUEADO. As linhas PARCIAL descrevem o limite da validação realizada, não uma declaração de validação total da experiência.

## C. Decisões técnicas

1. `TamableAnimal` mantém o vínculo e a persistência vanilla. A aparência de filhote é permanente; não há reprodução.
2. `DragonBrain` é o único árbitro de movimento e combate. Os oito estados têm prioridades, intervalos e histerese. Eventos antigos de combate não iniciam novas reações após cinco segundos.
3. Navegação nativa no chão/ar e aceleração gradual; todas as colisões de voo continuam físicas.
4. Ombro usa âncora da mesma entidade no mundo. A API real do papagaio, que descarta a entidade para gravá-la no jogador, foi analisada e descartada para esse requisito (D-031). Apenas a representação do próprio ocupante é ocultada em primeira pessoa.
5. `DragonEmber` causa dano direto, sem rotinas de ignição/explosão, e valida aliados na trajetória e no impacto. Vida e origem do projétil também são salvas.
6. Sem dependências novas. A API efetiva foi consultada no source jar local do Minecraft/NeoForge; registro e spawn seguem os mecanismos descritos também na [documentação oficial do NeoForge](https://docs.neoforged.net/docs/entities/livingentity/).

## D. Testes e repetições

- `build`: aprovado em múltiplas etapas, inclusive com a suíte ampliada. O exporter também compilou e executou com Java 25.
- Suíte inicial de 28 testes obrigatórios: cinco execuções aprovadas durante a implementação.
- Suíte intermediária de 30: três execuções consecutivas aprovadas.
- Suíte final de 31 (30 próprios e um do framework): **três execuções consecutivas aprovadas**, todas com `build` aprovado; detalhes também em `docs/DEVELOPMENT.md`.

Regressões executadas sem modificar testes antigos para obter aprovação:

| Suíte | Testes obrigatórios | Resultado das repetições isoladas |
|---|---:|---|
| Basilisco | 17 | Duas execuções aprovadas |
| Litófago | 11 | Duas execuções aprovadas |
| Cobrador | 18 | Duas execuções aprovadas |
| Quebra-Copas | 27 | Duas execuções aprovadas |
| Porrete | 9 | Duas execuções aprovadas |

A tentativa inicial de executar todas juntas, com o paralelismo do projeto ativo, falhou em `swamp_and_underground_spawn_rules` (iluminação) e `canopy_idol_conditions` (chunk distante). Na primeira rodada isolada, `deadroot_slam_damage_grows_with_height` também falhou (dano medido 0 versus 8,856). Todas essas suítes passaram nas repetições seguintes, sem alterações em seus testes. Isso é evidência de instabilidade nas execuções, não prova de uma causa específica. Logs completos foram mantidos no diretório temporário do usuário, com prefixo `coraline-dragon-`.

Comando reproduzível, em execuções separadas para reduzir interferência de carga:

```powershell
.\gradlew.bat --no-parallel --max-workers=1 build runBabyDragonTest
.\gradlew.bat --no-parallel --max-workers=1 runBasiliskTest
.\gradlew.bat --no-parallel --max-workers=1 runLithophageTest
.\gradlew.bat --no-parallel --max-workers=1 runLastGiftCollectorTest
.\gradlew.bat --no-parallel --max-workers=1 runCanopyBreakerTest
.\gradlew.bat --no-parallel --max-workers=1 runDeadrootClubTest
```

## E. Prévias visuais

- [Três vistas do modelo real](../../build/baby-dragon-sketches/anatomy.png).
- [Prancha das 12 poses](../../build/baby-dragon-sketches/review.png).
- [Prévia animada das 12 animações](../../build/baby-dragon-sketches/animations.gif).
- [Captura inicial no Minecraft](../../build/baby-dragon-preview/screenshots/2026-10-29_23.11.03.png).
- [Ombro em terceira pessoa](../../build/baby-dragon-preview/screenshots/2026-10-29_23.16.28.png).
- [Índice com seis capítulos corrigido](../../build/baby-dragon-preview/screenshots/2026-10-29_23.19.26.png).

Os artefatos em `build/` são locais e ignorados pelo Git. Os scripts de exportação/geração e as três gravuras finais do Bestiário ficam em `tools/` e `src/main/resources/` para reprodução.

## F. Validação no Minecraft

Foram abertas duas sessões de cliente com diretório separado `build/baby-dragon-preview`, usando uma cópia do mundo descartável dos GameTests. Nenhum mundo de `run/saves/` foi aberto ou modificado.

Conferidos no jogo: modelo/textura, tamanho em relação ao jogador, voo e batidas de asas, reação ao alimento, domesticação com conquista vanilla, pose no ombro, manutenção após reabrir o mundo, primeira pessoa desobstruída, seis botões no índice e todas as nove páginas resultantes em cada idioma (en-US 55–63, pt-BR 57–65). As três gravuras carregaram e os textos continuaram pela paginação existente.

Duas correções vieram dessa inspeção: índice com o sexto botão fora do livro e dragão cobrindo parte da primeira pessoa. Ambas foram corrigidas e verificadas na segunda sessão. A ampliação discreta do peito durante a preparação foi acrescentada depois dessa sessão e é validada pelo bake/prévia offline, sem alegar inspeção dessa alteração no cliente.

Ainda precisa de avaliação humana: timbre/volume dos sons, leitura do ataque em uma luta prolongada, balanceamento, desempenho em florestas densas e sincronização sob latência com dois jogadores reais.

## G. Arquivos criados e alterados

Criados:

- `entity/babydragon/`: `BabyDragon.java`, `DragonBrain.java`, `DragonFlightControl.java`, `DragonState.java`, `DragonEmber.java`.
- `client/babydragon/`: `BabyDragonClient.java`, `BabyDragonModel.java`, `BabyDragonAnimations.java`, `BabyDragonRenderState.java`, `BabyDragonRenderer.java`.
- `verification/BabyDragonGameTests.java`.
- Itens/modelos JSON e sprites de `baby_dragon_spawn_egg` e `dragon_ember`; atlas `textures/entity/baby_dragon.png`; três pranchas `textures/gui/bestiary/baby_dragon_*.png`.
- `data/codenamecoraline/`: tabela de saque vazia, modificador `baby_dragon_spawns.json` e tag de biomas `baby_dragon_habitat.json`.
- `tools/`: gerador de assets, helper de conteúdo traduzido/sons, exportador Java, init script de exportação e rasterizador de pranchas/prévias.
- `docs/design/baby_dragon.md` e este relatório.

Alterados para integração:

- `build.gradle` (tarefa de teste), `CodenameCoraline.java` (mensagem de carregamento), `ModEntities.java`, `ModItems.java`, `ModSounds.java`, `ModCreativeTabs.java`.
- `Bestiary.java`, `client/bestiary/BestiaryScreen.java`, `lang/pt_br.json`, `lang/en_us.json`, `sounds.json`.
- `README.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md`, `docs/ROADMAP.md`, `docs/DEVELOPMENT.md`.

Os caminhos Java acima são relativos a `src/main/java/com/codenamecoraline/`; recursos, a `src/main/resources/`. As modificações do Basilisco já estavam no workspace ao começar e continuam presentes.

## H. Limites e pendências

- Não há transferência automática do companheiro entre dimensões. Dono ausente no mundo causa saída do ombro.
- Usa só um ombro por dono; não disputa o armazenamento vanilla de papagaios. Duas espécies podem coexistir visualmente, cenário ainda não avaliado no cliente.
- Arte/animações possuem prévias e inspeção no cliente, mas sons e balanceamento não foram declarados plenamente validados.
- As falhas intermitentes de regressão foram registradas com os resultados reais; nenhum teste antigo foi relaxado.


## Correcoes de ombro e ataque (2026-10-29)

- O dragao no ombro ainda empurrava o dono via `pushEntities()`: `isPushable()` bloqueava apenas o impulso recebido pelo proprio dragao. Agora `pushEntities()` e `push(Entity)` ficam desativados no ombro e voltam ao normal ao sair.
- O log real `run/logs/latest.log`, as 23:31:59, registrou `ClassCastException: BabyDragon cannot be cast to Sniffer` em `ClientPacketListener.handleEntityEvent`. O cliente intercepta o evento 63 antes do handler da entidade. Os sete eventos do dragao agora usam constantes 80-86, evitando os casos especiais vanilla 21/35/63 da versao instalada.
- Dois testes novos reproduziram os erros com as correcoes temporariamente retiradas: impulso lateral no tick 2 e ID reservado no tick 0. Com as correcoes restauradas, `gradlew.bat --no-parallel --max-workers=1 build runBabyDragonTest`: **33/33 aprovados; BUILD SUCCESSFUL**. Logs em `%TEMP%/coraline-dragon-repro.log` e `%TEMP%/coraline-dragon-fixes-final.log`.
- O teste de ombro observa 29 ticks sem impulso, testa a chamada direta de colisao e confirma que empurroes normais retornam ao sair. Sua primeira execucao revelou conflito entre o callback de observacao e a saida intencional no tick 30; a observacao foi limitada aos ticks anteriores. O teste de eventos verifica IDs e despacho de carregamento/disparo. Testes anteriores preservados.
- Sem nova sessao visual do cliente nesta correcao: diagnostico baseado no log real, codigo do cliente instalado e GameTests de fisica/eventos.


## Frequencia e mira (2026-10-29)

Reacao a cada tick, aquisicao ate 24 blocos do dragao, recarga reduzida de 60 para 30 ticks, centelha de 0,8 para 1,2 bloco/tick e antecipacao do movimento do alvo com arrasto. Mantidos preparo de 12 ticks, alcance de 12 blocos, dano e protecoes. Novos testes verificam reconhecimento do golpe com dragao distante, disparo apos aproximacao sem novo golpe e acerto real contra alvo atravessando a linha de tiro a 0,25 bloco/tick. `build runBabyDragonTest`: 35/35 e BUILD SUCCESSFUL (`%TEMP%/coraline-dragon-aim.log`). Sem nova avaliacao visual de balanceamento no cliente.


## Animais atacados pelo dono (2026-10-29)

A pedido do usuario, removida a exclusao de mobs pacificos quando o dono os ataca. Qualquer mob sem protecao pode ser alvo de assistencia apos golpe do dono; animais sem envolvimento continuam ignorados. A protecao de dono, aliados e companheiros tem prioridade. Tres testes novos usam ataques reais do jogador contra galinha, vaca e porco e verificam aquisicao e dano posterior das centelhas; teste adicional existente confirma que golpear um lobo aliado nao remove sua protecao. `build runBabyDragonTest`: **38/38 aprovados e BUILD SUCCESSFUL**, log `%TEMP%/coraline-dragon-passive.log`. Sem nova sessao visual do cliente. Esta regra substitui a restricao original a animais pacificos descrita nas validacoes anteriores.
