# Evolução do dragão companheiro — 2026-11-04

**Revisão visual posterior:** as três idades agora têm anatomias e texturas próprias. A escala continua complementando o crescimento; colisões, olhos, boca e assento foram recalibrados. Referência atual de arte, dimensões e prévias em [dragon_remodel.md](dragon_remodel.md). XP, danos, inventário e regras de progressão abaixo permanecem válidos.

O registro continua `codenamecoraline:baby_dragon`, com o mesmo UUID, dono e recursos. Mundos antigos carregam como Bebê quando não há campos novos. O nome padrão acompanha a idade; etiquetas personalizadas permanecem intactas. Plataforma mantida: Minecraft 26.1.2, NeoForge 26.1.2.124, Java 25.

## Estágios e progressão

| Propriedade | Bebê | Jovem | Adulto |
|---|---:|---:|---:|
| XP acumulado mínimo | 0 | 500 | 2000 |
| Escala sobre o modelo original | 1 | 1,8 | 3 |
| Vida | 30 | 80 | 200 |
| Armadura / resistência | 2 / 0 | 8 / 2 | 16 / 4 |
| Dano de mordida | 4 | 9 | 16 |
| Bola de fogo | 6 | 14 | 30 |
| Velocidade aérea | 0,25 | 0,35 | 0,45 |
| Alcance de fogo | 12 | 20 | 35 |
| Inventário | 0 | 9 | 18 |
| Ombro | Sim | Não | Não |
| Montaria e sopro | Não | Não | Sim |

XP independente do jogador e de qualquer RPG: 2 XP por ponto efetivo de vida removido de um hostil (`Enemy`), mais 20 pela morte se participou nos últimos 200 ticks. O limite de participação por UUID é uma vida máxima da vítima: curá-la ou bater em invulnerabilidade não permite XP infinito. Créditos são persistidos (cap de 1024 vítimas); animais atingidos a pedido do dono continuam sendo defendidos, mas não dão XP do dragão. XP total satura em 2000.

A cada segundo verifica o próximo estágio. Com XP suficiente, sem passageiro/ombro e com a caixa futura livre, começa evolução de 60 ticks: dimensões e render crescem progressivamente, partículas e pose própria, atributos novos e saúde proporcional. Salvar durante a evolução retoma o relógio. Cura não regride nem repete um estágio. Não havia sistema de ressurreição, e nenhum foi criado.

## Combate

| Ataque | Preparo / execução / recuperação | Dano / recarga | Restrições |
|---|---|---|---|
| Centelha Bebê | 12 / projétil / animação de 8 ticks dentro da recarga | 6 / 30 ticks | Alcance 12, sem incêndio de cenário |
| Bola Jovem | 16 / projétil / 8 | 14 direto, 7 na área de 1,5 / 55 ticks | Parede protege; sem destruição |
| Bola colossal | 32 / esfera com rastro / 8 (12 montado) | 30 direto, 15 na área de 3 / 100 ticks | Alcance 35; magma atrás da trajetória |
| Sopro Infernal | 20 / 80 de cone contínuo / 20 | 5 a cada 10 ticks e ignição de 3 s / 240 ticks desde o início | Só Adulto, solo/ar, cone até 35, raio ≈ 0,45 + distância × 0,19 |
| Mordida | 8 / acerto / 6 | atributo de ataque / 32 ticks | Alvo próximo, válido e visível; conferência no acerto |

O servidor aplica dano e valida oclusão. O cliente desenha o sopro em volume com chamas vanilla amarelas/laranjas e brilho vermelho, carga na boca, mandíbula aberta e cabeça acompanhando o alvo. Não há entidades de bola de fogo no sopro. O mesmo filtro protege dono, equipe, pets e `PlayerSummon`; fogo não atravessa paredes nem destrói blocos. A IA adulta prefere sopro a curta/média distância, esfera quando distante ou com sopro em recarga e mordida se colado. Preparos sem alvo/visão são cancelados; cada habilidade tem sua recarga. Bebê mantém defesa reativa; crescidos também procuram hostis próximos do dono. Ordens de descansar/defesa prevalecem.

Magma: bloco próprio com aparência vanilla, colocado em ar sobre pedra, ardósia, grama, terra, areia ou netherrack. Nunca substitui esses blocos nem baús. Expira em 160 ticks; `SavedData` guarda posição, prazo e mestre. Chunks não são forçados; posições vencidas são restauradas ao voltar a carregar, somente se ainda contiverem o magma temporário. Modificações posteriores vencem. Sem mestre carregado/vivo, o rastro não causa dano; com mestre, apenas inimigos recebem 2 a cada 10 ticks. Não há item, drop nem transporte por pistão. Teto de 4096 posições por dimensão.

## Interação, controles e inventário

| Interação | Efeito |
|---|---|
| Salmão | Domesticação original (1/3) ou cura de 4 pelo dono |
| Mão vazia normal, Bebê/Jovem | Ficar/seguir |
| Mão vazia normal, Adulto | Montar (apenas dono) |
| Graveto normal | Ficar/seguir em qualquer estágio |
| Agachar + graveto | Alternar defesa |
| Agachar + mão vazia, Bebê | Ombro original |
| Agachar + mão vazia, Jovem/Adulto | Tela/inventário |
| Agachar + livro comum | Tela/inventário em qualquer estágio |

W/S avançam/recuam; A/D viram; o mouse orienta; Espaço decola/sobe; X desce/pousa; G desmonta; R prepara a esfera; B inicia o sopro. As quatro teclas novas ficam na categoria Montaria de Dragão e são remapeáveis. Shift conserva a desmontagem vanilla; V continua pulando cinemáticas. Pouse antes de desmontar em altura.

A física roda no servidor com aceleração interpolada, giro de no máximo 6°/tick e colisões reais. Pacotes aceitam apenas intenção/orientação do dono que está efetivamente montado; timeout de 10 ticks neutraliza comandos. Nenhum pacote aceita posição, itens, XP ou dano. A câmera usa a montaria vanilla e o assento acompanha o dorso.

Tela em estilo vanilla com texturas do baú, slots nativos, barra de XP, nome, dono, estágio, vida, armadura, dano, velocidade e habilidades (bloqueadas em cinza; detalhes no tooltip). Inventário fixo de 18 posições: abre 0/9/18 conforme idade, sem migrar pilhas ao evoluir. `ContainerHelper` salva os índices. Acesso exige dono, proximidade de até 8 blocos e entidade viva. Shift-clique e cliques usam transações/IDs de estado vanilla. A morte esvazia o inventário e solta o conteúdo uma única vez, mesmo se a rotina de morte for chamada novamente; a regra de loot de mobs não silencia essa entrega.

## Comandos de teste

Permissão de operador nível de mestre de jogo, uma entidade explícita e na mesma dimensão:

```mcfunction
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] info
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage baby
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage young
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] stage adult
/dragon @e[type=codenamecoraline:baby_dragon,sort=nearest,limit=1] xp add 500
```

Também aceita UUID explícito, recomendado com vários dragões juntos. `stage` força a idade sem animação e coloca XP no início daquele estágio para facilitar testes. Recusa colisão, passageiro e itens que seriam ocultados num downgrade. `xp add` exercita a evolução normal, com animação e espera por espaço. O progresso de campanha só sobe; a redução existe exclusivamente no comando administrativo.

## Arte, animações e balanceamento

Na remodelagem, a paleta do Bebê foi preservada em snapshot extraído do atlas original, e os três modelos receberam anatomias/atlas próprios. `DragonAnimations` mantém 17 definições por idade, com pesos e ritmos diferentes. `DragonRig` e `DragonAnatomy` ligam os modelos à boca, eixo de fogo, assento e dimensões físicas. `DragonStage` continua centralizando atributos, escalas, dano, alcance, slots e recarga da esfera; constantes em `BabyDragon` controlam evolução/sopro, `DragonMagmaTrail` controla duração/cap.

Poses atuais usam as matrizes articuladas de pescoço/cabeça em `DragonAnatomy`, compensando o olhar e mantendo o fogo no eixo real da mandíbula. Abertura chega a cerca de 40° no sopro e fecha em dez ticks visuais; a animação de voo usa batimentos e articulações próprios por idade. Detalhes e imagens em `dragon_remodel.md`.

```powershell
.\gradlew.bat -I tools/baby-dragon-sketches.gradle exportBabyDragonSketchModel
python tools/generate_baby_dragon_sketches.py
.\gradlew.bat --no-parallel --max-workers=1 build runBabyDragonTest
```

Exporta 612 amostras do modelo real (17 poses × 12 quadros × 3 idades). Prévias em `build/baby-dragon-sketches/{review,evolution,adult_attacks}.png` e `animations.gif`. Inspeção no cliente e limites detalhados no [relatório de validação](dragon_evolution_validation.md).
