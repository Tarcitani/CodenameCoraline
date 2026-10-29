# Prompt mestre para criar um mob (ou boss) no CodenameCoraline

Como usar: copie tudo abaixo da linha `=== INÍCIO ===` até `=== FIM ===`, preencha a seção **1. FICHA** (o que está entre `<...>`) e cole numa sessão nova. Tudo fora da ficha são regras fixas que valem para qualquer mob; não precisa editar. Para um mob comum, apague as partes marcadas **[BOSS]**.

=== INÍCIO ===

# MISSÃO: criar o mob `<id_do_mob>` no CodenameCoraline, completo, testado e documentado

Você é o engenheiro responsável por entregar um mob inteiro: código, arte, animações, sons, drops, Bestiário, textos, testes e docs. Não entregue "só compila": entregue funcionando, verificável e sem regressões. Leia `CLAUDE.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md` e `docs/DECISIONS.md` antes de começar e siga o procedimento do `CLAUDE.md`.

## 0. Regras de segurança e isolamento (obrigatórias)
- Trabalhe só no workspace atual. Nunca leia para modificar, mova, renomeie ou reutilize arquivos de `C:\MinecraftDev\CodenameCoraline`.
- Não altere configurações globais (Git, Claude Code, Java, Gradle, OMC). Não instale nada global.
- Não sobrescreva arquivos existentes silenciosamente; consulte `git status` antes de editar. Nunca `git reset --hard`, `git clean`, `rm -rf` amplo.
- **Não faça commit nem push** sem minha confirmação explícita. Não imprima nem registre segredos.
- Não use GeckoLib nem bibliotecas novas. Não use arquivos `.ogg` externos sem autorização (use eventos de som do vanilla em `sounds.json`).
- Verifique as assinaturas reais da API do Minecraft 26.1.2 / NeoForge 26.1.2.124 (javap nos jars, ou leia o código vizinho). Não invente nomes de métodos.
- Nunca afirme que algo funciona sem tê-lo executado. Qualidade visual, de animação e de sensação de combate **só se declaram validadas depois de eu rodar `runClient`**: até lá, marque como "não verificado no jogo".

## 1. FICHA DO MOB (preencha)
- **ID / nome interno:** `<id>`; **Nome pt_br:** `<nome>`; **Nome en_us:** `<name>`; **Tipo:** mob comum | mini-boss | **[BOSS]**.
- **Conceito e lore (3–6 linhas):** `<quem é, de onde vem, por que é ameaçador, tema visual>`.
- **Referências visuais:** `<imagens/links; o que copiar e o que NÃO copiar>`. Silhueta, paleta (cores em hex), materiais, itens que carrega.
- **Tamanho:** altura `<blocos>`, largura da caixa de colisão `<blocos>`, altura dos olhos `<blocos>`.
- **Atributos:** vida `<n>`, armadura `<n>` (+ resistência), velocidade `<n>`, dano base `<n>`, resistência a repulsão `<n>`, alcance de visão `<n>`, XP `<n>`.
- **Imunidades/afinidades:** `<fogo, congelamento/neve em pó, afogamento, luz do sol, etc.>`.
- **Onde aparece:** `<biomas, estruturas, altura, condição de luz/horário, raridade, tamanho do grupo>` | invocação por item/ritual `<condições, o que gasta, quando falha não consome>`.
- **Comportamento geral:** `<solitário/bando, diurno/noturno, foge/persegue, relação com outros mobs, com o jogador, com animais>`.
- **Ataques** (uma linha por ataque): nome | gatilho e alcance | preparo (ticks) | execução | recuperação | dano | efeitos | telegrafia visual/sonora | contra-jogada do jogador.
- **Estágios [BOSS]:** limiares de vida, o que muda em cada um (ataques liberados, velocidade, cooldowns); estágio final de fúria que amplifica atributos.
- **Lacaios/invocações (se houver):** conceito próprio e coerente com o tema (nunca reaproveite mobs existentes que briguem entre si); regras de lado/dono, limite, tempo de vida.
- **Drops:** itens `<nome, quantidade, chance>`; item especial `<propriedade, custo, recarga>`; ingredientes/receitas.
- **Sons:** para cada evento (ambiente, dor, morte, passos, cada ataque) qual evento vanilla usar, com pitch/volume.
- **Textos:** entrada do Bestiário `<páginas>`, conquistas `<título/descrição/ícone>`.

## 2. Arquitetura (siga o que já existe; leia um mob vizinho como modelo: `Redcap`, `Basilisk`, `Lithophage`, `LastGiftCollector`)
- Monólito modular. Pacotes por mob: `entity/<mob>/`, `client/<mob>/`, itens em `item/`, registros em `registry/` (`DeferredRegister`). Entrada do mod só coordena registros. Lógica de gameplay nunca em renderizador. Código/comentários em inglês, docs em português. Nada de pacote vazio nem interface genérica sem uso.
- **Servidor:** classe da entidade com máquina de estados explícita (estados enum), `SynchedEntityData` para o que o cliente precisa, cooldowns por ataque, goal único de "cérebro de combate". Persistência em NBT (`ValueInput/ValueOutput`, sempre com valor padrão).
- **Eventos de entidade (`broadcastEntityEvent`):** use IDs ≥ 100 e únicos por mob. Os IDs do vanilla (por exemplo 60 = fumaça de morte, 2/3 = dano/morte, 4 = ataque, 29–35, 46–55) colidem e quebram animações.
- **Cliente:** `EntityModel` com partes nomeadas e hierárquicas, `AnimationState` + `KeyframeAnimation` nativos, estado de renderização próprio, camada emissiva (`EyesLayer`) para olhos/runas. Registrar camada, renderizador e atributos.
- **Registros obrigatórios:** tipo de entidade, atributos, ovo de spawn (aba de ovos), item(ns) de drop, receitas, loot table, tags de entidade quando precisar (`powder_snow_walkable_mobs`, `freeze_immune_entity_types`, etc.), biome modifiers/spawn placements, sons, aba criativa.
- Nenhuma dependência nova sem finalidade registrada em `docs/DECISIONS.md`.

## 3. Qualidade de IA (os erros recorrentes que NÃO devem se repetir)
1. **Perseguir de verdade:** planejar caminho até a posição prevista do alvo (`createPath` + `canReach`), atualizar a cada poucos ticks, aproximar-se quando um ataque corpo a corpo está pronto, circular só quando tudo está em recarga e só por pontos com chão seguro.
2. **Nunca bater no ar:** só iniciar ataque se alcance, linha de visão e diferença de altura permitirem; durante o preparo continuar mirando/avançando até poucos ticks antes do golpe; no momento de comprometer, se o alvo escapou, **cancelar o preparo** (com cooldown curto) ou fazer investida curta; nunca executar o golpe no vazio.
3. **Relevo:** pular degraus, saltar para quem está num bloco/pilar acima, não golpear o ar sobre um buraco (usar ataque à distância/magia), usar blink ou salto quando travado, desenterrar-se se ficar preso em blocos, imunidade a neve em pó/congelamento se o tema pedir.
4. **Previsibilidade:** escolha ponderada que não repete o ataque anterior, jitter de preparo, combos que pulam recuperação, reação a quem fica colado durante a recuperação.
5. **Contra-estratégias:** quem foge recebe ataque à distância; quem se esconde/escala recebe magia ou salto; mirar à frente de quem se move (velocidade rastreada, não a do cliente).
6. **Alvo:** quem o feriu primeiro, senão o mais próximo, com histerese para não oscilar.
7. **Aliados e lacaios:** lado explícito (dono/UUID), nunca atacam o mestre nem o dono, dano do mestre não os fere, limite de quantidade, somem junto com o mestre.
8. **Dificuldade:** o mob tem que exigir atenção: telegrafias legíveis, mas com ritmo variável, dano relevante e janelas de punição justas.

## 4. Animações (validar com imagem, não no chute)
- As rotações das partes seguem a ordem Z·Y·X do ModelPart; um objeto seguro na mão fica de cabeça para baixo quando o braço sobe. **Documente poses de referência** (por exemplo "carregar", "armar", "impacto") com os ângulos e derive as animações delas.
- Valide **renderizando poses offline**: exporte as faces posadas do modelo real e desenhe visões lateral e 3/4 (ferramentas em `tools/` ou no scratchpad: `PoseTool`, `pose_sheet.py`). Itere até a arma/garras/cauda se moverem de forma legível em cada ataque. Anexe as imagens ao relatório.
- Cada ataque tem animação própria que respeita a arma ou membro usado (nada de golpe de foice com machado). Preparo, execução e recuperação são animações separadas. Idle, caminhada, dano, morte, e (se boss) transição de estágio e fúria.
- Partículas: ataques mágicos/projéteis têm **rastro contínuo** (interpolar entre posições, vários tons), explosão de impacto e telegrafia no chão antes do dano.

## 5. Arte (sempre por script reproduzível em `tools/`)
- Texturas por Python/Pillow em `tools/generate_<mob>_assets.py`, sem sobreposição de UV (conferir que as áreas das caixas não colidem; documentar o mapa), camada emissiva, ícones 16×16 com contorno automático, prévia gerada e **olhada** antes de seguir.
- Modelos de itens segurados: modelo 3D de caixas, girado 45° como sprite de ferramenta para herdar as transformações padrão; seleção por `display_context` (ícone no inventário/chão, 3D na mão).
- Gravuras do Bestiário geradas a partir do **modelo real** (`ExportXModel` + script de gravura), com os tamanhos exatos do projeto (anatomia 112×80, combate 112×72, espólios 112×64).
- **[BOSS]** Barra de vida **temática própria**, no estilo das barras ornamentadas de referência: moldura em `textures/gui/boss_bar/<id>.png` (slot de vida transparente, emblema por cima, nome dividido sob a trilha), estilo registrado em `client/bossbar/ThemedBossBars`, cor do preenchimento por estágio, marcas de estágio. Nunca a barra padrão.

## 6. Conteúdo e textos
- `lang/pt_br.json` e `lang/en_us.json` completos: nome, ovo, itens, sons (legendas), mensagens, Bestiário (6+ páginas com título/corpo/legenda), conquistas.
- Entrada no `Bestiary.ENTRIES` (mesmo livro), com páginas de texto e gravuras.
- **Conquistas que contam a história** em cadeia (obter o item de invocação, invocar, derrotar, obter cada drop raro), todas com o rosto/retrato do mob como ícone (item `*_portrait` só para ícones).
- Loot table, XP, e (se boss) morte temática em cinemática (efeitos, e recompensa entregue uma única vez, por exemplo num baú dentro de um presente).
- **[BOSS]** Invocação com condições claras (bioma, horário, bloco, espaço livre, sem outro boss por perto); falha não consome o item e explica o motivo no overlay.

## 7. Testes (obrigatórios; sem desativar nem enfraquecer testes existentes)
- Um GameTest por comportamento: registro/atributos/animações assimiláveis, cada ataque (dano a quem deve, zero a quem não deve, recuperação), estágios/limiares (uma vez cada), invocações e limites, morte e recompensa uma única vez, conquistas carregadas, Bestiário (páginas e gravuras), imunidades, regras de aliança, "não bate no ar" (cancelar preparo se o alvo some), perseguição (aproxima-se de alvo distante).
- Armadilhas conhecidas dos testes: **testes rodam em arenas paralelas; nunca descarte entidades por área/classe** (use as do próprio teste); posições dentro de paredes causam dano `inWall` e falsos positivos; imunidade de dano (invulnerableTime) após um golpe invalida o próximo teste; para a IA não interferir, tire o alvo (`setTarget(null)`) ou use `spawnWithNoFreeWill`; avance `tick()` manualmente só quando necessário; leve em conta atrasos de 1–3 ticks.
- Rode cada suíte **várias vezes (6+)** para flagrar instabilidade; investigue a causa raiz, nunca aumente tolerância às cegas. Rode também as suítes dos outros mobs (regressão).
- Comandos: `./gradlew build` e as tarefas `run<Mob>Test` (adicione a do novo mob em `build.gradle`). Se o Gradle estiver bloqueado no shell do agente, diga isso e use o lançador direto do GameTestServer; peça para eu rodar os comandos Gradle.

## 8. Documentação (no mesmo trabalho)
- `docs/ARCHITECTURE.md` (pacotes, registros, fluxo), `docs/DECISIONS.md` (nova decisão com contexto/decisão/consequências), `docs/ROADMAP.md` (feito/pendente **com evidência**), `docs/DEVELOPMENT.md` (comandos novos, com status "validado" só após execução real), `README.md` (se mudar versões/recursos), `docs/design/<mob>.md` (design: tabela de ataques, estágios, drops, linhas de tempo).

## 9. Procedimento
1. Leia os arquivos de contexto e um mob vizinho; `git status`, `git log --oneline -5`.
2. Entregue um **plano curto** (arquivos a criar/alterar, riscos, decisões que tomou por conta própria) e **siga em frente sem esperar** (só pergunte se faltar algo que só eu tenho).
3. Implemente em passos pequenos; compile (`javac`/Gradle) a cada passo; corrija a causa raiz de cada falha.
4. Gere arte → renderize prévia → olhe → ajuste. Valide animações por poses renderizadas.
5. Escreva os testes junto com o código; rode várias vezes.
6. Atualize docs e textos.
7. Revise o diff final (sem prints de depuração, sem `TODO`/stubs, sem arquivos soltos fora do projeto) e **não faça commit**.

## 10. Relatório final (formato fixo)
A. Resumo executivo (o que ficou pronto). B. Matriz de conformidade com a ficha (PASSOU / FALHOU / PARCIAL / BLOQUEADO). C. Decisões que você tomou por conta própria. D. Testes executados (comando, resultado, quantas rodadas). E. Imagens de validação (poses, prévias de arte, barra). F. O que **não** foi verificado e o que preciso rodar (`build`, `run<Mob>Test`, `runClient` com o que olhar). G. Arquivos criados/alterados. H. Riscos e pendências.

=== FIM ===

## Notas de uso (não fazem parte do prompt)

- **Boss novo:** deixe as partes **[BOSS]** e preencha estágios, fúria, invocação e morte temática.
- **Mob pequeno:** apague as partes **[BOSS]**; reduza o Bestiário para 4 páginas se quiser.
- **Ajustar um mob existente:** troque a missão por "revisar o mob `<id>`" e use as seções 3, 4, 7 e 10 como critérios; descreva o que está ruim em frases curtas e concretas (por exemplo "bate no vazio", "animação do ataque X não usa o machado").
- Quanto mais concreta a ficha (números, referências, o que NÃO fazer), melhor o resultado. Vale mandar imagens de referência junto com a ficha.
