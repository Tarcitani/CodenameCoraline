# Entrega da evolução do dragão — 2026-11-04

## A. Funcionalidades e conformidade

Atualizado o dragão existente, sem trocar seu registro nem recriar a entidade. Bebê/Jovem/Adulto usam XP próprio, crescimento físico e visual, atributos progressivos, bolas de fogo, esfera colossal, magma temporário, sopro contínuo, montaria aérea, tela/inventário vanilla e comandos administrativos. Detalhes e números em [dragon_evolution.md](dragon_evolution.md).

| Requisito | Estado | Evidência / limite |
|---|---|---|
| Estágios, XP, evolução única e crescimento | PASSOU | Testes de limiares, animação gradual, caixa/olhos/boca e atributos |
| Persistência, UUID, dono, evolução em andamento | PASSOU | Round-trip NBT e retomada; entidade adulta também reaberta no cliente |
| XP independente e proteção contra exploração | PASSOU | Dano real, invulnerabilidade, cura/limite por UUID, bônus de morte único |
| Bolas por estágio e cenário preservado | PASSOU | Impactos reais de 6/14/30, colisão de parede e proteção no splash |
| Magma com restauração e alterações posteriores | PASSOU | Diário serializado com prazo, restauração inclusive CAVE_AIR, preservação de bloco posterior |
| Descarregamento real de chunks com rastro | PARCIAL | Código não força chunks e conserva prazo; codec exercitado, sessão operacional de unload/reload pendente |
| Sopro periódico, paredes, aliados e recargas | PASSOU | Pulsos reais, preparo sem dano, parede, pet/dono, cancelamento e cooldown salvo |
| Montaria, voo e validação de intenções | PASSOU | Dono/idade, deslocamento de servidor, colisão, comandos forjados e ataques montados; montagem/G observados no cliente |
| Inventário, acesso, evolução e morte | PASSOU | 0/9/18, shift-clique sem duplicar, posições NBT, evolução, downgrade seguro e morte uma vez; transferência real na GUI |
| Tela vanilla, dados vivos e habilidades | PASSOU | Texturas vanilla, dados de servidor, registro/abertura e layout conferidos no cliente; tooltip e habilidades bloqueadas implementados |
| Funcionalidades antigas | PASSOU | Todos os 37 testes anteriores + framework continuam na suíte |
| Aparência, animações e sensação de combate finais | PARCIAL | Prévias offline e smoke de cliente feitos; aceite/avaliação humana ainda não verificados no jogo |
| Dois clientes reais e latência | PARCIAL | Protocolo/servidor exercitados, inclusive cliente integrado; sem sessão com dois clientes |

Decisões autônomas: ombro exclusivo do Bebê; livro agachado abre GUI sem remover a interação antiga; graveto normal mantém ficar/seguir no Adulto; X desce e B sopra para evitar conflitos com Shift/V. Magma fica sobre terreno natural sem substituir blocos. XP usa 2 por HP efetivo contra hostis, com limite por vítima, e 20 por derrota com participação recente. Não há ressurreição ou transferência automática entre dimensões. O usuário autorizou JUnit somente nos testes para corrigir o build já quebrado por dependência ausente.

## B. Arquivos criados ou modificados

Java relativo a `src/main/java/com/codenamecoraline/`:

- Criados `entity/babydragon/{DragonStage,DragonMagmaTrail,DragonEvents,DragonRiderPayload,DragonCommands}.java`, `inventory/DragonMenu.java`, `registry/ModDragonSystems.java`, `client/babydragon/{DragonScreen,DragonRiderControls,DragonEmberRenderer}.java` e `verification/DragonEvolutionGameTests.java`.
- Alterados `BabyDragon`, `DragonBrain`, `DragonEmber`, os cinco arquivos existentes de modelo/animação/render/client do dragão, `Bestiary`, `CodenameCoraline` e `BabyDragonGameTests`.
- Recursos: dois arquivos de idioma e `blockstates/dragon_temporary_magma.json`, que reutiliza o modelo vanilla de magma. Nenhuma textura original removida ou substituída.
- Build e ferramentas: `build.gradle`, `src/test/java/com/codenamecoraline/util/ThreadSafeLruCacheTest.java`, exportador/rasterizador existentes e `tools/dragon-evolution-preview.gradle`.
- Documentação: README, ARCHITECTURE, DECISIONS, ROADMAP, DEVELOPMENT, design original e os dois documentos de evolução.

Sem commit ou push. Nenhuma configuração global alterada; mundos de inspeção e arte intermediária ficam em `build/`.

## C. Build

`build runBabyDragonTest --rerun-tasks` com `--no-parallel --max-workers=1`: seis rodadas consecutivas aprovadas. Cada uma recompilou e executou **15 testes JUnit e 71 GameTests**. Jar: `build/libs/codenamecoraline-0.1.0-SNAPSHOT.jar`. JUnit só existe em configurações de teste.

O build inicial falhou porque o teste de cache já existente importava Jupiter sem dependência. Após autorização, declarada a dependência de teste. Uma execução revelou também uma corrida legítima de valores na própria fixture (`put` e `getOrCompute` compartilhavam parte das chaves); as faixas foram separadas, mantendo 16 threads, 1000 operações por thread, capacidade e todas as asserções. O cache de produção não foi alterado.

## D. Testes executados

- Base anterior: **38/38** após atualizar apenas vida/dano/catalogação para os valores exigidos pela nova ficha.
- Suíte ampliada: **71/71**, incluindo os 37 testes anteriores, 33 de evolução e um do framework; **seis rodadas finais forçadas consecutivas** nos logs `%TEMP%/coraline-dragon-evolution-verified-1.log` a `-6.log`.
- Após a última correção exclusivamente visual e a proteção explícita contra colocar magma dentro de criaturas: novo `build` aprovado e **mais seis rodadas 71/71** nos logs `%TEMP%/coraline-dragon-evolution-delivery-test-1.log` a `-6.log`. O teste de colocação também exige recusa quando uma criatura ocupa a posição. Build final: `%TEMP%/coraline-dragon-evolution-delivery-build.log`.
- Durante implementação, os testes detectaram inventário compactando posições, magma colocado diante do próprio projétil e direção de sopro baseada no corpo. Corrigidos com `ContainerHelper`, rastro atrás da esfera e orientação de cabeça.
- Uma rodada intermediária falhou na órbita. A fixture de ataque montado usava NoAI (que impede travel e aplicação da mira) e podia disparar horizontalmente para fora da arena; passou a exercitar física normal, mirando para cima dentro do próprio cenário. Nenhuma asserção da órbita foi alterada. As seis rodadas finais passaram.

| Regressão | Testes por rodada | Aprovações / tentativas |
|---|---:|---:|
| Basilisco | 17 | 6 / 7 |
| Litófago | 11 | 6 / 6 |
| Cobrador | 18 | 6 / 7 |
| Quebra-Copas | 35 | 6 / 6 |
| Porrete | 9 | 6 / 6 |
| Comandante Carmesim | 34 | 6 / 6 |

Falhas intermediárias preservadas: Basilisco `swamp_and_underground_spawn_rules` (iluminação, rodada 1); Cobrador `collector_gift_rain_and_crescent` (onda não atingiu o alvo, rodada 3). Passaram nas repetições sem mudanças em seus testes ou código. Não se declara ausência de instabilidade nessas suítes.

Logs de regressão: `%TEMP%/coraline-dragon-evolution-regression-run<Suite>Test-<1..7>.log`. Comandos reproduzíveis: `runBasiliskTest`, `runLithophageTest`, `runLastGiftCollectorTest`, `runCanopyBreakerTest`, `runDeadrootClubTest`, `runCrimsonCommanderTest`, separados e com um worker.

## E. Imagens e limites da inspeção

- [Comparação 1× / 1,8× / 3×](../../build/baby-dragon-sketches/evolution.png).
- [Ataques adultos, lateral e 3/4](../../build/baby-dragon-sketches/adult_attacks.png).
- [17 poses do modelo real](../../build/baby-dragon-sketches/review.png) e [GIF](../../build/baby-dragon-sketches/animations.gif).
- [Posição no dorso no cliente](../../build/dragon-evolution-preview/screenshots/2026-11-04_14.15.14.png).
- [GUI corrigida com item armazenado](../../build/dragon-evolution-preview/screenshots/2026-11-04_14.22.13.png).

O exportador executado pelo Gradle gerou 612 amostras da geometria real, rasterizadas e inspecionadas. A inspeção real do cliente identificou e corrigiu espaçamento de habilidades, leitura de toques rápidos e fumaça excessiva (partículas de lava substituídas por brilho vermelho). O mundo usado é uma cópia descartável em `build/dragon-evolution-preview`, sem abrir campanhas em `run/saves`.

**Não declarar validada a qualidade final sem o usuário rodar `runClient`.** Ainda pendentes: áudio, fluidez do voo em terreno real, intensidade/forma final do sopro em combate, sensação de evolução, dois clientes, desempenho sob latência e campanhas prolongadas. O texto anexado citava uma imagem, mas a imagem não estava disponível; a implementação seguiu a descrição de cone contínuo volumoso.

## F. Comandos para os estágios

Após domesticar com salmão, em área aberta e com cheats/operador:

```mcfunction
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] info
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage baby
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage young
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage adult
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] xp add 500
```

Use UUID explícito para evitar escolher outro dragão em grupos. Comandos de estágio reposicionam o XP para teste; o ganho de XP normal é permanente e só avança. Para ver a evolução, comece Bebê e adicione 500; depois acrescente 1500. Deve haver espaço para a caixa futura e nenhum passageiro/ombro.

## G. Montaria

Clique vazio no Adulto sendo o dono. W/S movem, A/D viram, mouse orienta; Espaço decola/sobe; X desce/pousa; G desmonta; R lança bola; B sopra. Remapeie as teclas na categoria Montaria de Dragão. Shift continua sendo desmontagem vanilla; V continua pulando cinemáticas. Pouse antes de desmontar em altura.

No `runClient`, confira assento, câmera, resposta suave, colisão com paredes, pouso, recargas e sopro no solo/ar. Em multijogador, confirme que outro jogador não monta, não abre o inventário nem controla o dragão do dono.

## H. Inventário

Agache e clique com livro comum em qualquer estágio; no Jovem/Adulto também funciona mão vazia agachada. Bebê: zero posições; Jovem: nove; Adulto: dezoito. Guarde pilhas em posições distantes, evolua, salve/reabra e confira posição/quantidade. Só o dono acessa perto da entidade viva. A morte solta todo o conteúdo uma vez; downgrade administrativo com itens em posições excedentes é recusado.

## I. Problemas conhecidos e pendências

Não há transferência automática entre dimensões ou recuperação de pet morto, pois esses sistemas não existiam. A expiração do magma usa tempo do mundo: servidor parado pausa o relógio. O diário limita 4096 posições por dimensão e o ledger de XP 1024 vítimas. Sem mestre carregado/vivo, magma continua visível até expirar, mas não aplica dano. Desmontagem em altitude exige cuidado; use X para pousar antes de G/Shift. As instabilidades históricas de regressão e os cenários humanos pendentes estão explicitados acima.

## J. Preservação

Nenhum registro, modelo, atlas, domesticação, alimentação, defesa, acompanhamento, órbita ou funcionalidade anterior foi removido. O ombro continua no Bebê; ficar/seguir permanece com graveto em todas as idades para acomodar montagem/GUI nas interações novas. Os atributos e dano Bebê foram elevados conforme a nova ficha (20 → 30 HP, 3 → 6 de dano). Todos os testes anteriores continuam ativos, sem afrouxar asserções para obter aprovação. Não houve commit/push.
