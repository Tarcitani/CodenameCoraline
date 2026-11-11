# Dragão de Fogo — bebê (`fire_dragon`)

> Rascunho de design consolidado em 2026-11-11. Fundação validada anteriormente. Usuário aprovou modelo e animações paradas e autorizou iniciar comportamentos. Fatia 3 escrita, sem compilação ou testes nesta etapa: TODOS os testes são executados pelo usuário. Outras animações permanecem sujeitas à revisão no jogo. Decisões explícitas do usuário estão registradas; números e detalhes criativos propostos pelo agente são sugestões de balanceamento. Escopo desta ficha: bebê. Jovem, adulto e ninho têm requisitos registrados, mas precisam de especificação antes de implementar a evolução completa.

## 1. Ficha

### 1.1 Identificação e Lore
- **ID / nome interno:** `fire_dragon`; **Nome pt_br:** Dragão de Fogo; **Nome en_us:** Fire Dragon.
- **Perfil:** aliado domesticável; selvagem não agride jogadores na fase bebê.
- **Nome do pacote (`pkg`):** `firedragon` (sugestão; novo, sem reutilizar a implementação removida).
- **Mob de referência (estrutura de código):** FeyRaven para vínculo, comandos e ombro; BrimstoneSalamander para anatomia e variações. BabyDragon citado no guia foi removido e não serve de base atual.
- **Lore e Origem Mítica (sugestão):**
  Nas montanhas pedregosas, dragões guardam o calor sob a rocha.
  Seus ovos despertam junto à lava, sob a vigília de um adulto protetor.
  Filhotes procuram alimento e aprendem a voar entre os picos.
  Um vínculo conquistado com salmão transforma sua cautela em companhia.
- **Assinatura de Silhueta e Proporções:** quadrúpede baixo, cabeça larga e grande, focinho curto, olhos expressivos, barriga arredondada, patas traseiras maiores que as dianteiras, asas pequenas articuladas e cauda longa de 4 segmentos; dois chifres curvos e crista dorsal. Sem proporção humanoide.
- **Referências visuais:** quatro imagens enviadas na conversa: bebê vermelho robusto; laranja de barriga clara e crista azul; cinza sentado de olhos grandes; preto com dourado. Incorporar expressão, chifres, barriga e asas; adaptar a caixas e pixel art Minecraft. Imagens ainda não arquivadas no repositório.
- **Cores:** predominante preto, branco, vermelho, laranja ou dourado. Detalhes usam outras cores dessas cinco, sem azul. Implementado: sortear secundária diferente e terceira livre para pequenos detalhes; salvar e sincronizar os três índices. Índices 0 preto, 1 branco, 2 vermelho, 3 laranja, 4 dourado; 125 texturas permitem combinações explícitas, 100 com predominante/secundária distintas no sorteio natural. Conservação na evolução depende da fatia futura. Predominante ocupa pelo menos 60% da superfície visível; barriga/membranas recebem secundária, crista/chifres recebem detalhe.
- **Dimensões (sugestão):** altura 0,65 bloco, largura de colisão 0,55, olhos a 0,48; comprimento visual aproximado 1,6 incluindo cauda; chifres ultrapassam a colisão de 0,65 de altura (visual próximo de 1 bloco).
- **Resolução do atlas / texel:** 16 px/bloco, atlas 128×128 sugerido para peças e asas; sem aumentar densidade local. Geometria e UV serão calculados na fatia 2.

### 1.2 Atributos e Ecologia Básica
- **Atributos (sugestão):** vida 20, armadura 2, resistência adicional 0, velocidade terrestre 0,25, voo desejado 4 blocos/s, dano de projétil 3, resistência a repulsão 0, visão 16 blocos, XP 0.
- **Imunidades e afinidades:** fogo, lava e queda; sem imunidade adicional a água, congelamento ou ataques comuns (sugestão).
- **Onde aparece:** ovos em ninhos de Stony Peaks, defendidos por adulto; bebês e jovens selvagens também podem ser encontrados. Frequências e geração pertencem à especificação de ninho, ainda pendente; não haverá spawn natural provisório inventado nesta fatia.
- **Limite de spawn:** sugestão para futuro ninho: 1 ovo e 1 guardião por ninho, separação mínima de 256 blocos; sujeito à revisão com a ficha do adulto. Bebê nasce 1 por ovo. Sem limite artificial de companheiros domesticados.
- **Dificuldade pacífica:** bebê permanece e pode ser domesticado (sugestão); guardião precisa de regra própria.
- **Tags de alvo:** caça usa lista explícita de animais de carne; jamais predicado genérico que inclua aldeões ou companheiros.
- **Persistência:** salvar dono, fase, idade restante, saciedade, origem de eclosão, cores e ordem de sentar; preservar tudo no ombro e em recarga do mundo. Sem crescimento offline.

### 1.3 Matriz de Comportamento e Interações
- **Própria espécie:** dragões do mesmo dono brincam e descansam próximos. Sugestão: parceiro até 6 blocos, tentativa a cada 200 ticks, brincadeira de até 100 ticks, recarga 400 ticks; perseguição sem dano e sem fogo. Sem comportamento social obrigatório entre donos diferentes.
- **Mortos-vivos:** sem agressão espontânea; retalia ameaças válidas. **Aldeões e golems:** ignora, salvo defesa válida. **Animais pacíficos:** caça para comer. **Outros mobs do mod:** sem agressão espontânea; nunca caça aliados domesticados.
- **Luz solar/escuridão:** indiferente. **Água/chuva (sugestão):** não sofre dano especial, evita pousar na água profunda, pesca a partir da margem ou voo baixo; não precisa mergulhar. **Fora do bioma:** mantém comportamento.
- **Rotina:** seguir, voar em trajetos curtos, caçar com fome, brincar, descansar. Busca de presa a cada 40 ticks; sem caça ou brincadeira sentado/no ombro.
- **Idles:** farejar erguendo o focinho; ajustar asas e apoio das patas; observar a cauda e soltar fumaça cosmética sem dano. Sorteio a cada 100–200 ticks fora de ações prioritárias.
- **Presas:** animais pacíficos com carne comestível; lista inicial ovelha, porco, vaca, galinha, coelho, bacalhau e salmão (as duas adições são sugestões coerentes com “todo animal pacífico que dê carne”). Excluir filhotes, animais domesticados e animais com nome (sugestão). Conteúdo de outros mods exige inclusão explícita em tag.

### 1.4 Combate, Fases e Dinâmica Multiplayer
- Bebê tem exatamente 1 ataque; nunca agride jogadores enquanto selvagem. Domesticado defende dono e a si próprio contra alvos válidos, respeitando PvP e equipes. Prioridade: agressor do dono, agressor próprio, alvo atacado pelo dono, caça. Sem fogo amigo com dono ou companheiros do mesmo dono.

| Ataque | Alcance (blocos) | Preparo (ticks) | Execução (ticks) | Recuperação (ticks) | Dano | Efeitos/projéteis | Telegrafia | Contra-jogada |
|---|---|---|---|---|---|---|---|---|
| Bola de fogo (sugestão numérica) | 12 | 12 | 1 | 15 | 3 direto + fogo por 60 ticks | 1 projétil a 0,8 bloco/tick; sem explosão, quebra ou incêndio de terreno | peito infla, cabeça recua e boca mostra brasas | sair da linha de tiro ou usar cobertura |

- **Validação:** no tick 12 verificar alvo vivo, distância, linha de visão e aliados na trajetória; abortar sem disparo quando inválido. Intervalo mínimo entre disparos: 60 ticks; recuperação não elimina o restante da recarga. Para permitir a caça aos peixes exigida pelo usuário, o projétil não é descartado apenas por entrar na água; mantém vida máxima de 30 ticks. Caça aquática precisa de teste no jogo.
- **Drops cozidos:** aplicar morte por fogo às presas para obter versões cozidas quando disponíveis; sem duplicar drops. Carne alimenta saciedade, só salmão cru acelera crescimento.
- **Boss:** não se aplica; sem cinemática, barra, estágios por vida ou lacaios.
- **Escala multiplayer:** não. **Leash (sugestão):** caça e brincadeira até 16 blocos do dono; abandonar perseguição ao exceder. Seguir prioritariamente a partir de 8 blocos; teletransportar a partir de 24 para posição segura na mesma dimensão quando seguindo e sem ombro. Nunca teleportar para lava.
- **Foco (sugestão):** manter agressor até invalidar ou surgir ameaça de maior prioridade; reavaliar a cada 10 ticks. Retomar ordem anterior após combate. Sentado permite defesa próxima, sem iniciar caça; no ombro não ataca nem sai automaticamente para combater.
- **Anti-cerco/altura:** voo e projétil já permitem defesa; sem ataque adicional ou quebra de blocos. Alvo inacessível por 100 ticks: desistir e retornar (sugestão).

### 1.4b Regras de Aliado / Companheiro
- **Domesticação:** salmão cru, 33% por oferta ao bebê selvagem; 50% se nasceu de ovo incubado; 10% no jovem, apenas fora de agressividade. Chances do bebê selvagem e jovem são sugestões anteriores; 50% do ovo foi definido pelo usuário. Consumir 1 por tentativa, salvo Criativo; partículas de sucesso/falha e vínculo por UUID.
- **Cura:** cada salmão cru restaura 4 de vida e 25 de saciedade, limitado ao máximo, além de acelerar crescimento. Nenhum efeito se o item não for consumido.
- **Fome (sugestão):** 0–100, início 100, perde 1 a cada 600 ticks carregado, inclusive no ombro; caça com 30 ou menos, para ao chegar a 55. Uma unidade de carne crua/cozida consumida restaura 25; consumir no máximo 1 item a cada 40 ticks. Restante fica no chão. Sem inventário. Em 0 não perde vida, mas crescimento natural pausa.
- **Crescimento (sugestão):** bebê 72.000 ticks (60 min), jovem 144.000 (120 min). Cada salmão cru subtrai 10% do restante, arredondado para cima com mínimo de 1 tick; ao zerar, muda 1 fase sem transferir redução para a próxima. Apenas tempo carregado e saciedade positiva avança naturalmente. Adulto não cresce. Evolução no ombro deve descarregar primeiro em posição segura; nunca perder a entidade.
- **Comandos:** botão direito alterna sentar/seguir. Caça é automática ao seguir, sem comando adicional.
- **Prioridade de interação:** Shift + botão direito com bebê próprio coloca no ombro, inclusive segurando salmão; sem Shift, salmão alimenta/domestica antes do comando; botão direito restante alterna ordem apenas para dono. Ombro cheio impede embarque e não consome comida (sugestão).
- **Desembarque:** pressionar Shift retira o bebê; usar transição da tecla para não desembarcar imediatamente no mesmo Shift usado para embarcar. Não copiar saída automática do papagaio por salto/água sem decisão explícita. Colocar em posição livre segura junto ao jogador; se impossível, manter no ombro e tentar novamente, sem apagar dados (sugestão).
- **Ombro:** exclusivo do bebê, máximo de 1 por ombro livre, sem ataque. Pose compacta com asas recolhidas. Preservar dano/morte/logout e dados corretamente; testes obrigatórios de ida e volta.
- **Recursos:** inventário 0; bebê e jovem não montáveis, mas voam; adulto montável para voar. Proteção contra dano direto do dono (sugestão); se dono morrer, permanece no local aguardando, sem caça ou perseguição distante.

### 1.4c Regras de Neutralidade
- Não se aplica ao bebê de perfil aliado; selvagem foge em vez de atacar jogadores (sugestão). Jovem e guardião terão regras de provocação em especificação própria.

### 1.5 Drops, Sons e Textos
- **Drops (sugestão):** bebê não deixa itens nem XP; nenhuma arma ou receita própria.
- **Sons (intenção; eventos vanilla verificados somente na fatia 4):** ambiente: trinados roucos curtos; carinho/domesticação: ronronar baixo; fome: chamado duplo; hurt: guincho breve; morte: suspiro descendente; passos: toques leves; voo: batidas macias de membrana; preparo: inspiração crepitante; disparo: sopro seco e estalo; impacto: pequeno crepitar; brincadeira: chilreios alternados. Volume sugerido 0,4–0,8, sem loop contínuo de fogo.
- **Bestiário (sugestão):** 6 páginas: lenda e montanhas; anatomia/cores; ovo/incubação; vínculo/comandos/ombro; alimentação/caça/defesa; crescimento/socialização. Não descrever sistemas pendentes como existentes no jogo.
- **Conquistas (sugestão):** “Uma pequena brasa” (chocar, ícone ovo); “Laços de fogo” (domesticar, salmão); “Companhia nas alturas” (ombro, pena). Cadeia nessa ordem; domesticar selvagem também libera a entrada de vínculo sem exigir eclosão prévia.
- **Faixa de eventos de rede:** nenhuma reservada neste rascunho; preferir estado sincronizado. Se forem necessários eventos customizados, reservar faixa livre ≥100 após busca em todas as constantes e fichas antes de implementar; nunca usar faixa não auditada.

### 1.6 Animação

**Parte A: intenção e tempo (sugestões).** Tempos a 20 ticks/s. Locomoção acompanha velocidade real; ações sociais nunca causam dano.

| Animação | Duração (ticks) | Pose-chave e movimento | Peso e inércia | Tick do impacto |
|---|---|---|---|---|
| Respiração | 40, ciclo | peito expande, cabeça e ponta da cauda acompanham | apoio baixo e estável | n/a |
| Idle farejar | 30 | pescoço ergue focinho e olha para os lados | cauda compensa cabeça | n/a |
| Idle ajustar asas | 36 | abre uma asa, depois outra, reajusta patas | peso alterna entre apoios | n/a |
| Idle cauda/fumaça | 40 | acompanha cauda com olhos, sopra fumaça pequena | corpo curva sem deslizar | n/a |
| Andar | 20, ciclo | quatro patas alternadas, cauda contrabalança | cabeça oscila suavemente | n/a |
| Decolar | 16 | agacha e impulsiona pernas com asas abertas | peso transfere para cima | n/a |
| Voar | 16, ciclo | asas batem, patas recolhem, cauda estabiliza | tronco sobe e desce pouco | n/a |
| Pousar | 16 | patas buscam chão e asas freiam | pernas absorvem descida | n/a |
| Fogo preparo | 12 | peito infla, pescoço recua, boca abre com brasas | patas firmam ou asas sustentam | n/a |
| Fogo liberação | 1 | cabeça avança e projétil sai da boca | pequeno recuo do tronco | 12, disparo; colisão variável |
| Fogo recuperação | 15 | fecha boca e devolve cabeça à base | cauda amortece recuo | n/a |
| Hurt | 8 | retrai pescoço e encolhe asas | recuo curto | n/a |
| Stun | 20 | cabeça baixa e patas firmes | recuperação lenta; apenas se efeito existir | n/a |
| Morte | 20 | perde apoio, tomba e recolhe asas | cauda assenta por último | n/a |
| Sentar/levantar | 12 cada | traseiro apoia ou ergue, asas fechadas | desloca centro de massa | n/a |
| Ombro | 40, ciclo | garras apoiadas, asas recolhidas, cabeça curiosa | sem balanço de voo | n/a |
| Comer | 24 | cabeça baixa pega alimento e mastiga | patas apoiadas | n/a |
| Brincar perseguição | 100 máximo | corrida/voo curtos alternando perseguidor | curvas com cauda e asas | n/a |
| Cumprimento focinho | 30 | inclina cabeça e toca focinho do parceiro | aproximação suave | n/a |
| Imitar asas | 40 | parceiros abrem asas em sequência | patas permanecem apoiadas | n/a |
| Descansar juntos | 100 | sentam próximos e acomodam a cauda | movimento desacelera | n/a |

- **Juntas:** root > torso; torso > neck_base > neck_mid > head > jaw; torso > tail_1 > tail_2 > tail_3 > tail_4; torso > wing_left/right_upper > wing_forearm > wing_tip; torso > cada leg_upper > leg_lower > foot. Pescoço dobra na base e meio; mandíbula na base do focinho; asas no ombro/cotovelo/ponta; pernas em quadril/ombro, joelho/cotovelo e tornozelo; cauda dobra em quatro segmentos. Chifres/crista acompanham a peça pai.

**Parte B: ângulos.** Catálogo completo em [fire_dragon_animation.md](fire_dragon_animation.md), gerado da mesma fonte que as animações Java: ticks, todas as juntas, keyframes, curvas LINEAR e bind pose com pivôs. Rotações ZYX confirmadas nas fontes reais de ModelPart 26.1.2.124. Todas as linhas estão pendentes de revisão visual humana.

## 2. Estado de implementação

| Fatia | Estado | Data | Comando de aceite e resultado |
|---|---|---|---|
| 0. Ficha revisada | concluída para fundação do bebê | 2026-11-11 | revisão documental e plano autorizado pelo usuário; sem execução de gameplay |
| 1. Fundação | concluída | 2026-11-11 | agente: `compileJava` e `build` aprovados; `runFireDragonTest runFeyRavenTest runBrimstoneSalamanderTest` em 4 rodadas consecutivas: 4/4, 9/9, 23/23 por rodada (contagens do servidor incluem teste do framework) |
| 2. Visual e rigging | modelo/idles aprovados; demais clipes em revisão | 2026-11-11 | geradores, compileJava/build, exportador de 75 poses e runClient executados; 4 rodadas Dragão/Corvo/Salamandra aprovadas (6/6, 9/9, 23/23 por rodada) |
| 2b. Revisão visual (humano) | modelo e animações paradas aprovados pelo usuário | 2026-11-11 | usuário autorizou continuar a IA; demais clipes precisam ser observados durante seus testes |
| 3. IA e combate | escrita; aguardando compilação e testes do usuário | 2026-11-11 | nenhum comando de teste/compilação executado pelo agente nesta etapa |
| 4. Polimento e docs | pendente | não iniciada | não executado |
| 5. Relatório | pendente | não iniciada | não executado |

- **Próximo passo:** usuário rodar compileJava, runFireDragonTest e runClient; enviar resultado/erros. Corrigir e repetir conforme resultado. Não marcar fatia 3 concluída antes das evidências. Jovem, adulto e incubação ficam para depois.
- **Escopo autorizado da fundação:** bebê de tamanho fixo, atributos, imunidades, registro e ovo de spawn criativo (não é o ovo incubável). Sem spawn natural, incubação, evolução, reprodução, domesticação, IA, cores ou renderizador nesta fatia. Jovem/adulto/ninho não bloqueiam este esqueleto isolado. Estado inicial WILD_IDLE; estágio bebê é conceitual, sem usar idade vanilla ou sua redução adicional de escala. Velocidade aérea é apenas atributo 0,2; meta de 4 blocos/s depende da navegação futura e medição.
- **Decisões:** cores permanentes; bebê com ataque único; Shift+direito embarca, novo Shift desembarca; caça automática; apenas adulto montável; salmão cru domestica/cura/acelera; alimento de caça também sacia. O usuário delegou atributos, animações, sons e visual ao agente.
- **Ovo confirmado:** lava diretamente 1 bloco abaixo; 12.000 ticks aquecidos (10 min), progresso pausa sem lava e com chunk descarregado; nasce somente bebê não domesticado; chance de domesticação 50%. Cor e origem precisam persistir ao mover o ovo; regras de coleta e bloco do ovo ainda pendentes.
- **Jovem confirmado:** mantém cores, voa sozinho, não aceita montaria, domesticação mais difícil e pode atacar jogador; atributos, ataques e gatilhos de raiva pendentes.
- **Adulto confirmado:** mantém cores e permite montaria aérea. Guardião defende ovo contra jogador ou hostil que se aproxime. Sugestões anteriores: proteção de 16 blocos, perseguição até 32 do ninho, prioriza ameaça mais próxima e protege recém-nascido até domesticação; essas distâncias ainda precisam de revisão com combate adulto.
- **Pendências e riscos:** (1) definir jovem/adulto sem reaproveitar código removido; (2) decidir coleta/transporte do ovo e relação com guardião; (3) garantir conservação de UUID/dados no ombro, inclusive evolução e morte do dono; (4) validar voo/caça a peixes e ausência de fogo amigo; (5) balancear fome/crescimento, arquivar referências e validar visual no runClient. Sem reprodução ou herança de cores especificadas.

### Evidências da fundação (2026-11-11)

- `python tools/generate_fire_dragon_assets.py`: executado, ícone RGBA 16×16 criado e inspecionado. JSONs e traduções conferidos.
- `compileJava`: aprovado após corrigir uso de `level.getRandom()` nos testes. `build`: aprovado na rodada 1.
- Quatro rodadas consecutivas, sem falhas, das suítes Dragão/Corvo/Salamandra. Logs em `build/logs/fire_dragon_fatia1_round_1.log` até `fire_dragon_fatia1_round_4.log`.
- Sem `runClient`, sem validação visual da criatura, sem commit. Mensagem sugerida: `feat(fire_dragon): fatia 1 - fundacao`.

### Evidências e revisão da fatia 2 (2026-11-11)

- Modelo real com 63 volumes e 3 juntas no pescoço, 4 na cauda, asas e quatro patas segmentadas. UV 128×128 verificado sem sobreposição; 125 combinações com 4 tons por material e emissivo transparente alinhado. Geradores preservam os mesmos índices de cor.
- 22 clipes da Parte A, mais entrada NONE; `ExportFireDragonModel` valida todas as juntas em todos os ticks e exporta 75 poses. `FireDragonModel` usa os mesmos clipes no jogo e no exportador. Sem geometria alternativa nos previews.
- Persistência/sincronização de cores e relógio de animação pelo tempo do mundo; servidor sorteia 3 idles a cada 100–200 ticks. Testes novos cobrem cores em save/load, limites, prévia inválida e ida/volta dos idles.
- Executados pelo agente: `python tools/generate_fire_dragon_assets.py`, `python tools/generate_fire_dragon_animations.py`, `.\gradlew.bat compileJava -I tools/fire-dragon-sketches.gradle exportFireDragonSketchModel`, `python tools/generate_fire_dragon_sketches.py`, `build` e quatro rodadas de `runFireDragonTest runFeyRavenTest runBrimstoneSalamanderTest` (6/6, 9/9 e 23/23 por rodada, incluindo framework). Logs `build/logs/fire_dragon_fatia2_round_1.log` a `_4.log`; exportador em `fire_dragon_fatia2_export.log`.
- `runClient` executado e encerrado normalmente. Mundo novo isolado “Fire Dragon Visual Review”, sem alterar mundos anteriores. Vistos: bebê vermelho/dourado e preto/dourado, rotação, asas em movimento, preparo de fogo e escala junto ao jogador. Log final `build/logs/fire_dragon_fatia2_client_final.log`. Isto é inspeção técnica do agente; não substitui aprovação humana das animações.
- Pranchas: `build/fire-dragon-sketches/anatomy.png`, `colors.png`, `poses.png`; 23 PNGs individuais `pose_*.png`; GIFs `animation_idle.gif`, `animation_walk.gif`, `animation_fly.gif`, `animation_fire.gif`. GIFs offline são amostras a 12 quadros/ciclo, não captura do cliente.
- Respiração, andar por velocidade, voo quando no ar, hurt/morte e postura sentada conectados ao renderizador; decolar, pousar, ombro, comer e socialização têm clipes preparados, mas gatilhos/comportamento ficam na fatia 3. Sem bola de fogo funcional nesta fatia. Partículas da boca, sons e camada de ombro no jogador ainda pendentes.
- Prévia de diagnóstico `VisualPreview` afeta só pose, não aplica dano nem movimenta. `none` restaura renderização normal. Não é comando de gameplay. Nenhum evento de rede customizado ou dependência adicionados. Commits autorizados posteriormente pelo usuário; ver versionamento abaixo.

#### Comandos de revisão no cliente

```mcfunction
/summon codenamecoraline:fire_dragon ~ ~ ~3 {PrimaryColor:2,SecondaryColor:4,AccentColor:0,Rotation:[180f,0f]}
/summon codenamecoraline:fire_dragon ~2 ~ ~3 {PrimaryColor:0,SecondaryColor:4,AccentColor:2}
/summon codenamecoraline:fire_dragon ~-2 ~ ~3 {PrimaryColor:1,SecondaryColor:0,AccentColor:4}
/summon codenamecoraline:fire_dragon ~4 ~ ~3 {PrimaryColor:3,SecondaryColor:1,AccentColor:2}
/summon codenamecoraline:fire_dragon ~-4 ~ ~3 {PrimaryColor:4,SecondaryColor:2,AccentColor:0}
/data merge entity @e[type=codenamecoraline:fire_dragon,limit=1,sort=nearest] {VisualPreview:"fly"}
/data merge entity @e[type=codenamecoraline:fire_dragon,limit=1,sort=nearest] {VisualPreview:"fire_windup"}
/data merge entity @e[type=codenamecoraline:fire_dragon,limit=1,sort=nearest] {VisualPreview:"none"}
```

### Versionamento autorizado pelo usuário

- `e47337c` — `feat(fire_dragon): adicionar bebe com modelo e animacoes`.
- `4351b7b` — `test(fire_dragon): cobrir fundacao cores e animacoes ociosas`.
- Ficha, arquitetura, desenvolvimento, estado e ferramentas reunidos em commit `docs(fire_dragon)`. Aprovação visual humana continua pendente; autorização de commit não aprova o visual. Sem push.

## 3. Fatia 3 — código escrito, testes exclusivos do usuário

**Nível de raciocínio recomendado: High.** Autorização: usuário aprovou modelo/idles, pediu continuidade e determinou que TODOS os testes, incluindo compilação, são feitos por ele. Esta orientação prevalece sobre os prompts de fatia e sobre evidências históricas executadas pelo agente.

### Alterações desta etapa (não validadas)

- `FireDragon`: interação autoritativa no servidor; salmão cru com 33% ou 50% (`Hatched`), cura 4, saciedade +25 e redução de 10% do crescimento restante. Clique direito senta/segue; Shift+direito tem prioridade para ombro. Dono salvo pelo `TamableAnimal`; alimento/comando de terceiros recusados.
- `FireDragonCompanion`: coordenação de estados sem goals concorrentes. Defesa interrompe caça/socialização; limites de 16 blocos do dono, retorno/teletransporte a partir de 24 com chão e espaço seguros. Busca de presas e itens escalonada. Lista de presas da ficha; não caça filhotes, nomeados ou domesticados.
- Caça automática ativa com fome até saciar em 55; coleta uma unidade de carne por vez e deixa o restante. Carne de caça sacia; salmão oferecido diretamente também cura e acelera crescimento. Histerese de fome é transitória; após reload ela recomeça quando atingir 30.
- Ombro não remove, recria nem serializa o dragão dentro do jogador: a própria entidade mantém UUID, cores e dono, como no corvo. Dois lados, verificação de papagaios/corvo e outros dragões no embarque. Novo pressionamento de Shift desembarca após soltar o Shift do embarque; sem lugar seguro, tenta novamente. Não dispara do ombro. Sem montaria.
- `DragonFireball`: tipo próprio registrado, item visual vanilla FIRE_CHARGE, vida de 30 ticks, 3 de dano, ignição antes do dano para loot cozido; sem explosão, fogo em blocos ou duplicação de loot. Proteção ao dono/pets e equipes/PvP. Preparo 12, liberação 1, recuperação 15 ticks, intervalo 60; revalidação de alvo/linha/aliados antes do disparo.
- Saciedade e temporizadores persistentes; crescimento natural só carregado e alimentado. **Ao zerar, permanece bebê**, conservando o progresso até a implementação do jovem. Não existem ovo incubável ou ninho nesta entrega.
- Clipes de alimentação, ombro, fogo e interações sociais conectados; quatro interações sociais sorteadas entre dragões do mesmo dono. Decolagem/pouso ainda usam o voo genérico, sem transição dedicada. Sons e polimento continuam na fatia 4.
- GameTests escritos para domesticação/alimentação/comandos, persistência de dono/nutrição, proteção, Shift e UUID no ombro e morte de porco com carne cozida. Suíte agora contém 9 testes próprios; número efetivo e resultados só após execução do usuário. **Nenhum teste novo foi executado.**

### Roteiro de aceite para o usuário

```powershell
.\gradlew.bat compileJava
.\gradlew.bat runFireDragonTest
.\gradlew.bat runClient
```

1. Domesticar com salmão; alimentar ferido e conferir cura. Alternar sentar/seguir com mão vazia. Recarregar mundo e conferir dono, cores, ordem e crescimento.
2. Shift+direito no bebê próprio, inclusive com salmão: deve subir sem gastar comida. Manter Shift não pode derrubar imediatamente; soltar e pressionar de novo deve desembarcar. Testar dois bebês, ombros ocupados e teto baixo; repetir após salvar/carregar.
3. Seguir o dono em terra, desníveis e água. Afastar-se mais de 24 blocos e conferir retorno seguro; testar áreas sem pouso seguro, sem teletransporte para lava/parede. Voo pretende 4 blocos/s, velocidade real precisa ser medida.
4. Com fome, observar caça, consumo e retorno. Usar bebê de teste com `Satiety:0` e domesticar; porcos/ovelhas/vacas, depois peixes. Carne cozida deve vir do loot vanilla, sem item duplicado.
5. Durante caça/brincadeira, atacar o dono com um hostil: deve priorizar defesa. Conferir alvo atacado pelo dono, autodefesa, cobertura antes do disparo e alvo acima de pilar. Dono e pets não podem receber dano/fogo do dragão; bebê selvagem não ataca jogador.
6. Dois domesticados do mesmo dono, saciados e seguindo: observar brincadeiras sem dano e limite de distância. Conferir sentar interrompe caça e brincadeira. Testar morte/logout do dono e PvP desativado.
7. Verificar animações de fogo, voo, ombro e comida. Movimentos reais e interações com corvo/papagaio precisam de teste humano; não estão aprovados por terem código escrito.

Depois da primeira execução bem-sucedida, rodar também regressões `runFeyRavenTest`/`runBrimstoneSalamanderTest`; requisito de quatro rodadas consecutivas permanece, executado pelo usuário. Enviar só resultado final ou trecho do erro. Sem novo commit/push nesta etapa.

## 4. Ajustes após teste do usuário — ombro, mira e autodefesa

**Nível recomendado: High. Estado: alterações escritas, sem compilação ou testes do agente.** O usuário relatou empurrão no ombro, pontaria insuficiente e ausência de reação a ataques; enviou referência de dragão enrolado para a pose de ombro. Isso não equivale à validação completa da fatia 3.

- **Empurrão:** a entidade no ombro ainda participava da resolução de empurrões. `FireDragon` agora bloqueia `isPushable`, `push(Entity)`, `doPush` e `canCollideWith` enquanto estiver no ombro; fora dele, delega ao comportamento normal. Sem `noPhysics` global e sem remover a entidade.
- **Pose:** ponto de apoio mais próximo/baixo (deslocamento lateral 0,32 e altura do jogador menos 0,75 bloco). Patas recolhidas, asas fechadas, cabeça acomodada e cauda curvada junto ao pescoço; ciclo de respiração de 40 ticks. Lado direito espelha as rotações Y/Z centrais; pose não se desfaz com hurt visual. Fonte no gerador de animações e tabela regenerada em `fire_dragon_animation.md`. Apenas geração dos arquivos foi executada; nenhuma pose nova foi renderizada/validada pelo agente.
- **Mira:** disparo aponta para o centro do corpo e antecipa deslocamento observado, com limite de previsão, correção para arrasto aéreo vanilla e previsão vertical curta. Sem dispersão aleatória e sem projétil teleguiado. Parede e aliados são revalidados na trajetória prevista. Precisão contra corrida/saltos/água depende do teste humano.
- **Autodefesa:** dano aceito chama a aquisição do agressor imediatamente, inclusive no primeiro tick. Golpes repetidos do mesmo alvo não reiniciam o preparo. Durante a recarga, busca afastamento quando o inimigo está a menos de 4 blocos. Autodefesa próxima (8 blocos) não é descartada só porque o dono ficou longe; caça e defesa do dono continuam limitadas ao dono.
- **Bebê selvagem:** continua sem atacar jogadores; reage a agressor não jogador e preserva fuga diante de jogador. Domesticado não retalia dono/pets protegidos nem jogadores em Criativo/Espectador. No ombro permanece sem disparar. Para testar defesa, usar zumbi ou outro agressor válido, não golpear o próprio bebê domesticado com o dono.
- **Regressões escritas, não executadas:** ombro sem impulso no dono, aquisição imediata de agressor e disparo sob golpes repetidos, antecipação de alvo lateral. Agora são 12 testes próprios escritos; resultado/contagem do servidor ainda dependem de execução do usuário.

### Verificação solicitada ao usuário

1. Rodar `compileJava`, `runFireDragonTest`, depois `runClient` (comandos da seção 3).
2. Embarcar, soltar as teclas e verificar que o jogador fica parado sem deslizar; andar, girar, agachar para sair e repetir nos dois ombros. Conferir retorno da colisão normal depois de desembarcar.
3. Ver a nova pose em terceira pessoa, parado e girando: cauda próxima ao pescoço, asas/patas recolhidas e sem atravessar cabeça/armadura. Essa aproximação da referência ainda precisa de revisão humana.
4. Com bebê domesticado fora do ombro, deixar um zumbi atingi-lo repetidamente: deve adquirir o alvo, completar o disparo e buscar distância durante a recarga; repetir sentado e enquanto caçava.
5. Conferir acerto em alvo parado e andando lateralmente, sem atingir dono/pets que entrem na linha de tiro. No ombro, nenhum disparo.

Sem novo commit/push. A imagem de referência está na conversa; os ângulos e a intenção foram registrados aqui para continuidade.

### Acompanhamento do ombro — ajuste visual pendente de teste

Usuário aprovou defesa, mira e pose do ombro; relatou atraso do dragão ao andar. O renderer agora compensa a posição interpolada atrasada da entidade com a posição interpolada atual do dono, por `getRenderOffset`, e acompanha a rotação corporal do jogador. A posição/colisão e a autoridade do servidor permanecem na entidade. Offset zerado fora do ombro para não afetar voo ou desembarque. Nenhuma compilação ou teste executado pelo agente. Usuário deve conferir andar/correr, girar e desembarcar, nos dois lados e com outro jogador em multiplayer. Nível recomendado: Medium. Sem commit.

### Visibilidade em primeira pessoa — aguardando usuário

Renderer reduz para 60% o tamanho visual do dragão no ombro apenas quando a câmera em primeira pessoa pertence ao dono. Terceira pessoa, câmera de outro jogador, tamanho real/colisão e comportamento permanecem iguais. A escala é recalculada a cada extração de render state para restaurar o tamanho ao alternar câmera ou desembarcar. Sem compilação, testes ou runClient pelo agente. Usuário deve conferir os dois ombros em primeira pessoa e alternar F5, além de observar outro jogador. Nível recomendado: Medium. Sem commit.

### Altura da miniatura em primeira pessoa — aguardando teste humano

Usuário relatou que a redução deixou o bebê baixo demais. A escala visual continua em 60%, mas o renderer compensa a altura usando uma âncora de 10/16 bloco junto ao pescoço: deslocamento vertical de (1 − escala) × altura da âncora × escala base, ou +0,25 bloco na escala base 1. Assim a miniatura encolhe em torno da região do pescoço, em vez dos pés. Compensação exclusiva da primeira pessoa do dono; posição real e terceira pessoa não mudam. Sem compilação ou testes pelo agente. Conferir altura e campo de visão ao andar/girar e alternar F5.

### Estabilização do ombro ao caminhar — teste humano pendente

Usuário aprovou o tamanho atual, mas ainda observou glitches durante a caminhada. A primeira pessoa agora ancora posição e yaw na própria câmera (`Camera.position()`/`yRot()`), em vez da interpolação do dono e do yaw corporal que varia ao andar/strafe. A rotação relativa residual da cabeça é zerada nessa visão. Escala 60% e compensação vertical anteriores preservadas; terceira pessoa continua presa ao corpo do dono. Nenhuma compilação ou teste executado pelo agente. Conferir caminhada frontal, corrida, movimento lateral e giro nos dois ombros, alternando F5; relatar se a oscilação persiste. Nível recomendado: High. Sem commit.
