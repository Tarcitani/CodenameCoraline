# PROCESSO_MOB.md — Como criar um mob do conceito à entrega

Guia operacional do fluxo completo. As regras técnicas ficam em `MOB_CONTRACT.md`; a ficha fica em `docs/design/<mob>.md` (a partir de `docs/design/_TEMPLATE_MOB.md`); os prompts de cada etapa ficam em `docs/prompts/`. Este arquivo só diz **quem faz o quê, em que ordem e com qual modelo**.

Os comandos dos prompts estão em Bash; no PowerShell use `.\gradlew.bat` e os equivalentes indicados em `docs/prompts/fatia_4_polimento.md`.

## Princípios

1. **O arquivo é a memória, a conversa não.** Cada sessão começa lendo arquivos e termina gravando o estado em `docs/design/<mob>.md`. Nada importante fica só no chat.
2. **Uma sessão por fatia.** Contexto curto rende mais e custa menos que uma sessão longa (ver "Orçamento de contexto").
3. **Número onde o humano acerta, intenção onde ele erra.** A ficha traz dimensões, atributos, ticks e a pose-chave **em palavras**. Os ângulos em graus são calculados na fatia 2, contra o modelo real, porque um chat sem acesso ao código erra a orientação 3D das caixas.
4. **Um humano olha o visual.** A IA não julga animação. A fatia 2 só fecha depois que você vê as poses renderizadas.
5. **Um mob de referência.** Todo mob novo copia a estrutura de um mob existente (ver "Mobs de referência").

## Cadeia do processo

| Etapa | Quem | Saída | Modelo sugerido |
|---|---|---|---|
| 0a. Pesquisa e inspiração | Você | Links, imagens, notas | (humano) |
| 0b. Rascunho da ficha | ChatGPT (ou qualquer chat) com `docs/prompts/ficha_chatgpt.md` | `docs/design/<mob>.md` com Parte A da 1.6 (sem ângulos) e sem IDs de som | ChatGPT |
| 0c. Revisão da ficha | Claude Code, sessão curta | Ficha sem `<...>`, sem contradição, com números | Sonnet |
| 1. Fundação | Claude Code, `docs/prompts/fatia_1_fundacao.md` | Entidade registrada, spawn, compila | Haiku ou Sonnet |
| 2. Visual e rigging | Claude Code, `docs/prompts/fatia_2_visual.md` | Modelo, textura, animações, poses renderizadas | Opus ou Sonnet |
| 2b. Revisão visual | **Você** | Aprovação ou lista de ajustes | (humano) |
| 3. IA e Comportamento | Claude Code, `docs/prompts/fatia_3_ia_combate.md` (ou `fatia_3_aliado.md`) | Estados, IA/vínculos, ataques/comandos, GameTests | Opus ou Sonnet |
| 4. Polimento e docs | Claude Code, `docs/prompts/fatia_4_polimento.md` | Drops, Bestiário, sons, conquistas, docs, rodadas de teste | Sonnet ou Haiku |
| 5. Relatório e verificação | Sessão nova, `docs/prompts/relatorio.md` | Matriz de conformidade com evidências | Sonnet |

## Perfis de Entidade (Escopo e Testes)

Nem toda criatura é um boss. O template e o contrato adaptam-se conforme o **Perfil** declarado na ficha:

| Perfil | Escopo Obrigatório | Seções Dispensadas | Prompt Fatia 3 | Rodadas de Teste |
|---|---|---|---|---|
| **`boss`** | Cinemática, barra temática, estágios de fúria, arena/ritual, anti-cerco, anti-pillaring, 4 fases de ataque | Nenhuma | `fatia_3_ia_combate.md` | 6 consecutivas |
| **`hostil`** (mob comum) | 4 fases de ataque, anti-whiff, IA ecológica, 3 idles sorteados | Cinemática, barra de boss, estágios múltiplos | `fatia_3_ia_combate.md` | 4 consecutivas |
| **`neutro`** | Gatilhos de agressividade/raiva, defesa própria, perdão/memória, 3 idles sorteados | Anti-cerco complexo, cinemática, barra de boss | `fatia_3_ia_combate.md` | 4 consecutivas |
| **`aliado`** (companheiro) | Domesticação, vínculo por UUID, seguir/esperar, inventário/cura, prevenção de fogo amigo | Tabela de ataques de boss, anti-pillaring, agressão cega | `fatia_3_aliado.md` | 4 consecutivas |
| **`invocação`** (`PlayerSummon`) | Contrato `PlayerSummon`, limite populacional, tempo de vida, combate de apoio | Ecologia de bioma, spawn natural, domesticação | `fatia_3_ia_combate.md` | 3 consecutivas |
O número de rodadas é o **N** usado em `MOB_CONTRACT.md` (regra 6), em `fatia_4_polimento.md` e no relatório. Uma falha em qualquer rodada reinicia a contagem.

A escolha de modelo é uma sugestão de custo e risco: registro e arquivos de dados são mecânicos (barato); modelo 3D, animação e IA exigem julgamento (mais forte). Ajuste pela sua experiência.

## Onde o ChatGPT entra (e onde não)

Pode e deve ser usado para pesquisa e para o **rascunho da ficha**, porque isso é texto e criatividade, sem acesso ao código. Não use o ChatGPT para gerar o plano de implementação: ele não vê o repositório e o plano sai genérico. O plano de implementação de cada fatia sai do Claude Code, que lê o código.

Cole o conteúdo de `docs/prompts/ficha_chatgpt.md` junto com o template e suas referências. Exija a Parte A da seção 1.6 (pose-chave em palavras e ticks); os ângulos (Parte B) não vêm do chat.

## Orçamento de contexto (limite do Claude Code)

O contexto da sessão é finito e a qualidade cai antes do fim. Regras:

- **Leitura mínima por sessão:** só `AGENTS.md` (seção Memória do projeto), a seção da fatia em `MOB_CONTRACT.md`, `docs/design/<mob>.md` e os arquivos do mob de referência que a fatia exige. Nunca leia `DECISIONS.md`, `ARCHITECTURE.md` ou `ROADMAP.md` inteiros: busque a seção com grep.
- **Logs em arquivo:** `mkdir -p build/logs` uma vez, depois `./gradlew ... > build/logs/<fatia>.log 2>&1`, e leia só o trecho de erro. Nunca despeje o log de build no chat.
- **O Gradle pode não rodar no shell do agente.** No Claude Code desta máquina falhou em 2026-11-05 (`Unable to establish loopback connection` em `compileJava`); em outro agente/terminal a mesma compilação passou. Depende do ambiente; histórico em `DEVELOPMENT.md`. Se falhar, o agente não deve improvisar lançador manual. Ele entrega o comando exato; **você** roda no seu terminal e cola só o trecho de erro (ou salva o log em `build/logs/` e diz o caminho). Uma fatia só vira "concluída" com o resultado real, anotado com quem rodou.
- **Uma fatia, uma sessão.** Termine, grave o estado e use `/clear` (ou abra sessão nova). Não encadeie fatias na mesma conversa.
- **Se a sessão encher no meio da fatia** (muitas idas e voltas, arquivos grandes lidos): grave em "Próximo passo" do arquivo de design o que falta e abra sessão nova. Não confie no resumo automático.
- **Fichas curtas.** Se a ficha passar de ~300 linhas, mova a animação para `docs/design/<mob>_animation.md` e referencie.
- **Prompts curtos.** Cada prompt de fatia aponta para arquivos em vez de repetir regras.

## Qualidade visual e animação

- A ficha traz a **Parte A da seção 1.6** (pose-chave em palavras e duração em ticks por animação). A fatia 2 calcula os ângulos e grava a **Parte B** (rot inicial, rot final e curva por junta), sempre igual ao que o código faz.
- **Texturas:** são geradas por script em `tools/generate_<id>_assets.py` (regra de `tools/README.md`: nada de asset desenhado "na mão" sem script). Se você já tiver textura ou modelo do Blockbench, entregue o arquivo e diga na ficha; o agente reproduz o atlas por script a partir dele.
- A fatia 2 parte de um **mob de referência** com a mesma categoria, e copia a estrutura de classes (`Model`, `Animations`, `RenderState`, `Renderer`, `EyesLayer`, `Client`).
- Para fechar a fatia 2, o agente gera as poses com o script de gravuras do mob e entrega a lista dos arquivos para você abrir. **Você** responde "aprovado" ou lista os ajustes.
- Checklist de revisão visual (você, 5 minutos): silhueta legível de frente, de lado e de costas; nenhuma junta quebrada; pose de preparo diferente da de impacto; olhos e brilho no lugar; escala certa ao lado de um jogador.
- Visual e animação só contam como validados depois do `runClient` (regra do `AGENTS.md`). O agente deve entregar os comandos `/summon` para você testar.

## Mobs de referência

| Quando o mob novo é... | Copie a estrutura de | Por quê |
|---|---|---|
| Mob comum com projétil e animações | `Lithophage` | Menor, completo, com GameTests |
| Boss com cinemática | `LastGiftCollector` (`client/collector/`) | Tem diretor de câmera, barra e Bestiário |
| Boss com fases e invocações | `CanopyBreaker` ou `CrimsonCommander` | Estágios, `PlayerSummon`, espíritos |
| Companheiro com evolução | `BabyDragon` | Atributos, hunger, interações |

Se a categoria nova não existir, a fatia 1 registra em `docs/DECISIONS.md` qual mob foi usado como base.

## Regras de entrada e saída de cada fatia

- **Entrada:** a fatia anterior está `concluída` em "Estado de implementação"; `git status` está limpo ou as mudanças pendentes são conhecidas.
- **Saída:** o comando de aceite da fatia passou, o estado foi atualizado com data e comando, e a sessão parou.

## Commits entre fatias

Cada fatia aprovada vira **um commit**, para que um problema na fatia seguinte possa ser desfeito sem perder o visual aprovado. Quem decide é você; o agente só commita se você pedir.

| Quando | Mensagem sugerida |
|---|---|
| Fatia 1 concluída | `feat(<id>): fatia 1 - fundacao` |
| Fatia 2 aprovada (2b) | `feat(<id>): fatia 2 - visual` |
| Fatia 3 concluída | `feat(<id>): fatia 3 - ia e combate` |
| Fatia 4 concluída | `feat(<id>): fatia 4 - polimento e docs` |

Antes de cada commit: `git status`, conferir que só entraram arquivos do mob e nenhum segredo. A ficha (`docs/design/<id>.md`) vai junto, com o Estado de implementação atualizado. Se a fatia seguinte der errado, volte ao commit anterior só dos arquivos dela (`git restore` nos arquivos da fatia), sem usar `reset --hard`.

## Quando algo dá errado

- **Plano da fatia diverge da ficha:** corrija a ficha primeiro, depois o código. A ficha é a fonte.
- **GameTest intermitente:** a regra do `MOB_CONTRACT` (N rodadas conforme o Perfil) vale. Não rode em loop até "dar verde": registre a causa.
- **Visual ruim depois de duas iterações:** pare, melhore a Parte A da 1.6 (pose-chave mais clara) ou a referência e recomece a fatia, em vez de continuar remendando.
