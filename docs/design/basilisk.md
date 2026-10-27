# Basilisco da Praga

Implementação para Minecraft 26.1.2 / NeoForge 26.1.2.124, baseada no Markdown `basilisk_elden_ring_e_mob_minecraft.md` fornecido pelo usuário. O documento serve como referência de design; a stack e a arquitetura do projeto são mantidas.

## Entidade e visual

- ID: `codenamecoraline:basilisk`; invocação: `/summon codenamecoraline:basilisk`.
- Ovo: `codenamecoraline:basilisk_spawn_egg`, disponível junto ao ovo do Redcap na aba do mod.
- Anfíbio quadrúpede com cauda segmentada, barbatanas, garras, olhos falsos grandes e olhos reais pequenos junto ao focinho.
- Modelo nativo em `client/basilisk/BasiliskModel.java`; textura e máscara emissiva de 128×128. Os olhos falsos não piscam. Respiração, saltos, oscilação dos olhos e cauda e expansão de até 60% do papo são procedurais.
- Texturas próprias e ícones reproduzíveis com `python tools/generate_basilisk_assets.py` (Pillow). Não há dependência de biblioteca de animação.

## Combate e praga

| Atributo | Valor |
|---|---|
| Vida | 28 (14 corações) |
| Armadura | 2 |
| Movimento em terra | 0,32 |
| Movimento na água/lama | 0,38 |
| Golpe durante o salto | 4 |
| Fraquezas | fogo ×1,5; golpes diretos de espada ×1,25 |
| Imunidades | Veneno, Decomposição e Escuridão |

O mob escolhe jogadores a menos de dez blocos, salta para atacar e circula a vítima durante o intervalo do sopro. Sem alvo, espreita na água, na lama ou no sculk. A navegação anfíbia fornece o caminho; um controle próprio desloca o corpo em saltos. Não há escalada nem aderência ao teto.

O sopro tem antecipação audível de 24 ticks (1,2 segundo). Ao terminar, emite um cone de partículas e deixa uma `DeathBlightCloud` de raio 3,5, duração 100 ticks e sem dano direto. Paredes sólidas bloqueiam a exposição; nuvens sobrepostas não multiplicam a velocidade de acúmulo.

- Dentro da névoa: +15 a cada dez ticks (meio segundo).
- Fora: −5 a cada vinte ticks (um segundo).
- Ao chegar a 100: 100 de dano que ignora armadura, escudo e o intervalo de invulnerabilidade entre golpes, com raízes de partículas e imobilização por um segundo.
- O dano conserva as proteções do criativo e do Totem da Imortalidade. O período de imobilização impede outro disparo imediato contra quem sobreviveu com um totem.
- Leite, mel e ambas as maçãs douradas limpam a barra ao terminar o consumo.
- O efeito alcança criaturas vivas, exceto Basiliscos e jogadores em criativo/espectador. Isso permite usar a mesma nuvem no frasco fabricável.

O valor da barra fica em um attachment persistente e sincronizado somente com o jogador afetado. Sair e entrar no mundo conserva o valor; morrer não o copia. Os temporizadores de exposição e de raízes são transitórios. O HUD preto/dourado aparece somente com acúmulo positivo.

## Luz das almas

Segurar uma tocha ou lanterna das almas em qualquer mão impede o mob de escolher o portador como alvo e interrompe a preparação do sopro. Ele recua para manter aproximadamente seis blocos. A névoa já existente continua perigosa.

Um ataque direto faz aquele Basilisco memorizar o UUID do agressor, que deixa de ser protegido pela própria luz das almas. Essa memória é salva com a entidade.

## Geração e recompensas

O modificador de bioma adiciona grupos de 2–4, com peso 25. A regra da entidade exige Y < 0, luz local ≤ 5, ausência de céu visível, solo válido e dificuldade diferente de Pacífico. Biomas permitidos: `deep_dark`, `lush_caves` e `mangrove_swamp`. O último representa somente posições subterrâneas que ainda pertençam ao bioma de manguezal; não há bioma novo.

| Drop | Chance | Uso implementado |
|---|---|---|
| Lírio das Catacumbas (1–2) | 25% | Usar em golem de ferro cura 12 e concede Resistência I por 30 s; também fabrica visão espiritual |
| Borboleta Putrefata | 8%, se nasceu em cavernas verdejantes/manguezal | Com quatro flechas, fabrica quatro flechas de Decomposição I por 5 s |
| Cisto Ocular | 5% | Fabrica frascos de névoa ou amuleto |

Receitas sem forma:

- Cisto + frasco de vidro + sculk → dois Frascos de Névoa da Morte. São arremessáveis e também podem atingir quem arremessou.
- Cisto + barra de ouro + corrente de ferro (`minecraft:iron_chain`) → Amuleto do Basilisco. Segurá-lo em qualquer mão reduz pela metade a taxa de acúmulo e o dano de Decomposição.
- Lírio + bolsa de tinta brilhante + frasco de vidro → Poção de Visão Espiritual (Visão Noturna por um minuto).

Adaptações da referência: o amuleto ocupa uma mão, sem acessórios externos; Decomposição usa redução de dano porque o efeito vanilla não possui barra de acúmulo; a podridão usa o efeito vanilla Decomposição. Raízes e névoa são efeitos visuais e não alteram blocos. Os drops são sorteados no código, conservam a origem úmida da entidade e não recebem bônus de Saque.

## Verificação

Comandos, evidências e limites da validação estão em [DEVELOPMENT.md](../DEVELOPMENT.md). As verificações de servidor usam um mundo separado em `build/basilisk-gametest/`.
