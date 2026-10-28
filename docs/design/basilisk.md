# Basilisco

Implementação para Minecraft 26.1.2 / NeoForge 26.1.2.124, baseada no Markdown `basilisk_elden_ring_e_mob_minecraft.md` fornecido pelo usuário. A revisão de 2026-10-28 substitui a praga por Decomposição vanilla e troca as recompensas por perna, olho, rabo e poção de resistência preparada no suporte vanilla. A stack e a arquitetura do projeto são mantidas.

## Entidade e visual

- ID: `codenamecoraline:basilisk`; invocação: `/summon codenamecoraline:basilisk`.
- Ovo: `codenamecoraline:basilisk_spawn_egg`, disponível junto ao ovo do Redcap na aba do mod.
- Anfíbio quadrúpede com cauda segmentada, barbatanas, garras, olhos falsos grandes e olhos reais pequenos junto ao focinho.
- Modelo nativo em `client/basilisk/BasiliskModel.java`; textura e máscara emissiva de 128×128. Os olhos falsos não piscam. Respiração, saltos, oscilação dos olhos e cauda e expansão de até 60% do papo são procedurais.
- O Bestiário inclui três pranchas: anatomia, sopro e partes. Anatomia e sopro são renderizados a partir do modelo e das poses reais em tinta sépia; as partes usam os ícones correspondentes.
- Texturas próprias e ícones reproduzíveis com `python tools/generate_basilisk_assets.py` (Pillow). Não há dependência de biblioteca de animação.

## Combate e Decomposição

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

O sopro tem antecipação audível de 24 ticks (1,2 segundo). Ao terminar, emite um cone de partículas e deixa uma `WitherMistCloud` de raio 3,5 por 100 ticks (cinco segundos).

- A névoa aplica **Decomposição III (Wither) por 100 ticks**, o efeito vanilla que escurece os corações e causa dano ao longo do tempo, podendo matar.
- A detecção ocorre na entrada e a cada dez ticks; paredes sólidas bloqueiam a exposição. Basiliscos e jogadores em criativo/espectador são excluídos.
- Instâncias de Decomposição III ou superior terminam antes de nova aplicação; instâncias mais fracas são elevadas para III. Isso preserva o relógio de dano vanilla, que depende da duração restante: renovar a duração continuamente impediria o dano. Nuvens sobrepostas não aumentam a intensidade nem atrasam o dano; efeitos mais fortes de outras fontes são preservados.
- Sair da névoa não remove uma Decomposição já aplicada. Ela termina naturalmente ou pode ser removida com leite. Mel e maçãs douradas seguem suas regras vanilla, sem cura especial deste mod.
- **Praga removida:** sem barra, acúmulo, decadência, raízes, imobilização, attachments ou disparo de 100 de dano. Não há mais tipo de dano próprio.

## Luz das almas

Segurar uma tocha ou lanterna das almas em qualquer mão impede o mob de escolher o portador como alvo e interrompe a preparação do sopro. Ele recua para manter aproximadamente seis blocos. A névoa já existente continua perigosa.

Um ataque direto faz aquele Basilisco memorizar o UUID do agressor, que deixa de ser protegido pela própria luz das almas. Essa memória é salva com a entidade.

## Geração e recompensas

O Basilisco é um monstro hostil comum. O modificador adiciona grupos de **4–8**, com **peso 100**, a todos os biomas da tag `minecraft:is_overworld`. Esse peso é relativo aos outros monstros, não uma probabilidade percentual.

- **Pântano e manguezal:** geração na superfície ou embaixo dela, em qualquer luminosidade, inclusive de dia.
- **Subterrâneo dos demais biomas do Overworld:** sem céu visível, luz local ≤ 7 e posição pelo menos quatro blocos abaixo do topo da altura `MOTION_BLOCKING_NO_LEAVES`. Folhas não transformam o chão da floresta em uma caverna. Não há mais exigência de Y < 0.
- Em ambos os casos, exige solo válido e dificuldade diferente de Pacífico. A categoria continua `MONSTER`, com os limites, distâncias e descarte de monstros do jogo. O limite próprio de agrupamento foi elevado de quatro para oito.
- O spawn é natural e recorrente, sujeito à competição e ao limite de monstros; não usa um gerador artificial nem a população passiva de vacas/porcos. A abundância prática depende também de área disponível, luz, outros mobs e regras do mundo.


| Drop | Chance | Uso implementado |
|---|---|---|
| Perna de Basilisco (1) | 50% | Ingrediente da poção de Resistência à Decomposição |
| Olho de Basilisco (1) | 25% | Ingrediente da mesma poção |
| Rabo de Basilisco (1) | 35% | Ingrediente final da mesma poção |

Os sorteios são independentes e não dependem do bioma de origem. Não recebem bônus de Saque. Os valores de drop e duração são parâmetros iniciais de balanceamento, ainda sujeitos à avaliação do jogador.

Preparo no **suporte de poções vanilla**, abastecido com pó de blaze:

1. Frascos de água + fungo do Nether → poções estranhas (receita vanilla).
2. Poção estranha + perna → Infusão de Perna de Basilisco.
3. Infusão de perna + olho → Infusão Ocular de Basilisco.
4. Infusão ocular + rabo → Poção de Resistência à Decomposição (três minutos).
5. Opcional: poção pronta + redstone → versão de oito minutos.

- Cada etapa usa o tempo vanilla de **400 ticks (vinte segundos)**, combustível e um ingrediente para até três frascos. A ordem é obrigatória; as duas infusões intermediárias não concedem proteção.
- Não há receita de bancada. Tipos registrados em `Registries.POTION` e misturas via `RegisterBrewingRecipesEvent`/`PotionBrewing.Builder.addMix` usam o sistema nativo.
- Pólvora e bafo de dragão seguem as conversões vanilla para versões arremessável e persistente. A extensão por redstone preserva o recipiente.
- A poção concede `codenamecoraline:wither_resistance` por **3600 ticks (três minutos)** ou **9600 ticks (oito minutos)** na versão estendida; beber devolve frasco de vidro.
- Remove Decomposição existente ao beber e bloqueia novas aplicações enquanto durar, inclusive Decomposição III e efeitos de outras fontes como o Wither. Não concede Resistência genérica a outros danos.
- Leite remove a proteção; após expirar, Decomposição volta a ser aplicável.
- Perna, olho e rabo aparecem em Ingredientes do criativo. As poções são listadas automaticamente na aba vanilla de consumíveis a partir do registro nativo, com nomes nos dois idiomas.

Lírio, borboleta, cisto, amuleto e frasco de névoa não são mais drops nem possuem receitas ou entradas no criativo. Os registros antigos dos itens permanecem para preservar inventários existentes. O lírio já existente conserva seu uso e o frasco antigo passa a produzir a mesma névoa de Decomposição; o amuleto não oferece proteção. O ID antigo `death_blight_cloud` é mantido apenas como compatibilidade de entidade salva, executando `WitherMistCloud`.

## Verificação

Comandos, evidências e limites da validação estão em [DEVELOPMENT.md](../DEVELOPMENT.md). As verificações de servidor usam um mundo separado em `build/basilisk-gametest/`.
