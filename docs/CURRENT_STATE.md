# Estado atual do projeto

Resumo de uma página para começar sessões novas. **Não é fonte de verdade:** detalhes e evidências estão em `ROADMAP.md`, `DECISIONS.md` e `design/`. O código real prevalece. Atualize este arquivo ao concluir ou mudar algo relevante.

Última revisão: 2026-11-08 (a partir do `ROADMAP.md`).

## Stack
Minecraft 26.1.2 · NeoForge 26.1.2.124 · Java 25 · Gradle 9.2.1. Mod ID `codenamecoraline`. Tema: folclore feérico tradicional.

## Conteúdo implementado

| Sistema | Resumo | Design |
|---|---|---|
| Redcap | Mob com 3 variantes, bando, touca, drops | `design/redcap.md` |
| Basilisco da Praga | Mob com sopro (Decomposição III + Cegueira), poção vanilla | `design/basilisk.md` |
| Litófago | Mob com 7 estados de IA e 3 ataques | `design/lithophage.md` |
| Cobrador da Última Dádiva | Boss com 5 estágios, cinemática, trenó | `design/last_gift_collector.md` |
| Quebra-Copas | Boss arbóreo, Micos de Casca, Porrete de Raiz-Morta | `design/canopy_breaker.md` |
| Comandante Carmesim | Boss de 3 estágios, spawn raro em cavernas | `design/crimson_commander.md` |
| Bruxa dos Dez Fios | Boss composto (Matriarca + 10 marionetes), 33 ataques, sistema de animação próprio | `design/ten_threads_witch.md`, `design/matriarch_animation.md` |
| Dragão Bebê Companheiro | Domesticável; evolui Bebê → Jovem → Adulto; montaria, inventário, sopro | `design/baby_dragon.md`, `design/dragon_evolution.md`, `design/dragon_remodel.md`, `design/baby_dragon_roadmap.md` |
| Corvo Feérico Companheiro | Domesticável com bagas brilhantes; ombro, coleta de itens brilhantes (1 slot), Sentinela Mística contra invisíveis | `design/fey_raven.md` |
| Salamandra de Enxofre | Predador vulcânico do Nether, 8 estados, 3 ataques, 3 variantes de textura (Brimstone, Soulflame, Crimson) e estágio filhote com dinâmica familiar, combate juvenil e 6 texturas dedicadas | `design/brimstone_salamander.md` |
| Bestiário | Livro com capítulo ilustrado por mob/boss | `ARCHITECTURE.md` |

Os últimos commits entregam a Salamandra de Enxofre (`brimstone_salamander`, perfil `hostil` no Nether com estágio filhote completo), o Corvo Feérico Companheiro (`fey_raven`, perfil `aliado`) e as etapas do Baby Dragon.

## Validação
- Os GameTests rodam por tarefas `run<Mob>Test` (lista em `build.gradle`). Contagens recentes: Dragão Bebê 81, Comandante 34, Quebra-Copas 35, Bruxa 30, Salamandra de Enxofre 23, Matriarca 20, Cobrador 18, Basilisco 17, Litófago 11, Porrete 9, Corvo Feérico 8.
- Em 2026-11-05 o `build` e o `runTenThreadsWitchTest` ainda não tinham sido executados pelo agente (o Gradle não roda no shell de alguns agentes; ver `DEVELOPMENT.md`). Confirme no seu terminal.

## Em desenvolvimento

| Sistema | Resumo | Design |
|---|---|---|
| O Arquiteto (Kandiaru) | Boss de seis braços, disfarce de estátua; fatias 1 (fundação), 2 (visual, aprovada na 2b) 3 (IA e combate) e 4 (sons, receita da tábua, Cimitarra do Arquiteto como drop, conquistas, Bestiário; 34 GameTests em 6 rodadas, `build` verde) concluídas; nada visto no jogo ainda (próximo: `runClient`, depois a arena com H e I) | `design/architect.md` |

## Pendências recorrentes
- **Nada de Comandante, Bruxa e Matriarca foi visto no jogo** (`runClient`): aparência, animações, sons, barra, gravuras.
- Balanceamento real contra jogador, em terreno real e com multiplayer (2 clientes e latência).
- Dragões: aceite humano de voo, combate e áudio. Comparação com a imagem de referência ficou sem resposta.
- Gravuras do Bestiário da Matriarca não regeneradas com o rig novo.
- Licença real a definir antes de publicar (provisório: All Rights Reserved).
- Redcap: geração natural e voz original em `.ogg` não existem.
- Javali de Raiz adiado (D-029).

## Onde procurar
`AGENTS.md` (regras) · `ROADMAP.md` (histórico detalhado) · `DECISIONS.md` (D-001 a D-037) · `DEVELOPMENT.md` (comandos, falhas conhecidas) · `PROCESSO_MOB.md` (fluxo de criação de mob), `PROMPT_NOVO_MOB.md` (índice), `MOB_CONTRACT.md` (contratos) e `prompts/` (um prompt por fatia).

