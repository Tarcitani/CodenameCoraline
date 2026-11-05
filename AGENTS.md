# AGENTS.md — Manual operacional do CodenameCoraline

Fonte única de regras para **todos** os agentes e ferramentas (Antigravity, Claude Code, Cursor, Codex, Hermes...). Vale para qualquer modelo (GPT, Claude, Gemini etc.) e para qualquer membro da equipe. `CLAUDE.md` deve apenas apontar para este arquivo.

Mod de Minecraft Java (NeoForge). Nome interno: `CodenameCoraline`. Mod ID: `codenamecoraline`. Nome provisório.

## Stack (versões verificadas)
- Minecraft 26.1.2 · NeoForge 26.1.2.124 · Java 25 (toolchain Gradle) · Gradle Wrapper 9.2.1 · plugin `net.neoforged.moddev` 2.0.148.
- Versões em `gradle.properties` e `build.gradle`; motivos em `docs/DECISIONS.md`.

## Isolamento e segurança (obrigatório)
- Trabalhe somente no workspace atual do repositório (`C:\MinecraftDev\CodenameCoraline`).
- Nunca leia para modificar, mova, renomeie, exclua ou reutilize arquivos de `C:\MinecraftDev\MeuPrimeiroMod`.
- Não altere configurações globais (Git, Java, Gradle, ferramentas de IA). Use configuração local ao projeto.
- Não instale ferramentas globais sem autorização explícita.
- Não sobrescreva arquivos existentes silenciosamente. Não use `git reset --hard`, `git clean`, `rm -rf` amplo nem reinicialize o repositório.
- Não registre, imprima ou documente tokens, senhas ou chaves. Verifique segredos antes de cada commit.
- Commit e push só após confirmação explícita do usuário.

## Memória do projeto (onde está o contexto)
O projeto é a memória; a conversa não. Ao começar uma sessão nova:
1. `git status` e `git log --oneline -5`.
2. `docs/CURRENT_STATE.md` (resumo de uma página); `docs/ROADMAP.md` só se precisar do histórico detalhado.
3. `docs/design/<sistema>.md` do mob/boss envolvido.
4. `docs/ARCHITECTURE.md` e `docs/DECISIONS.md` apenas nas seções relevantes (são grandes: use busca, não leia inteiro).

Mapa de docs: `ARCHITECTURE` (estrutura), `DECISIONS` (decisões), `DEVELOPMENT` (comandos e validação), `ROADMAP` (estado), `design/` (especificação por mob/boss), `PROCESSO_MOB.md` (fluxo de criação de mob: etapas, modelos, orçamento de contexto), `PROMPT_NOVO_MOB.md` (índice), `MOB_CONTRACT.md` (contrato técnico de entidades) e `prompts/` (um prompt por fatia).

Precedência: **o código real prevalece sobre documentação desatualizada**. Se achar divergência, corrija a doc ou avise. Não invente funcionalidades que não existem no código nem refaça o que já está implementado (confira antes com busca).

## Convenções arquiteturais
- Monólito modular: um único módulo Gradle. Sem subprojetos sem justificativa concreta.
- Pacotes só quando há requisito real. Sem pacotes vazios nem interfaces genéricas.
- Ponto de entrada só carrega o mod e coordena registros. Lógica de gameplay não vai em renderizadores.
- Registros via `DeferredRegister` do NeoForge.
- Código e comentários em inglês; documentação em português.
- Nenhuma dependência sem finalidade registrada em `docs/DECISIONS.md`.

## Regras de bosses
- Todo boss tem **barra de vida própria e temática**, nunca a barra padrão do Minecraft.
- Estilo em `client/bossbar/` implementando `ThemedBossBars.Style`, registrado em `ThemedBossBars` pela chave de tradução do nome da entidade; textura `textures/gui/boss_bar/<id>.png` gerada por script em `tools/` (geometria do script e do estilo devem coincidir); preenchimento acompanha a cor definida pelo servidor por estágio; marcas na trilha indicam o início de cada estágio.
- Boss novo só está pronto com: barra temática, conquistas de história, Bestiário, GameTests e docs. Visual de barra, animações e partículas só são "validados" após `runClient`.

## Comandos
Na raiz do projeto (PowerShell ou Git Bash):
- Build + testes: `./gradlew build` (Windows: `gradlew.bat build`)
- Só compilar: `./gradlew compileJava`
- Cliente / servidor de dev: `./gradlew runClient` / `./gradlew runServer`
- GameTests: `runBasiliskTest`, `runLithophageTest`, `runLastGiftCollectorTest`, `runCanopyBreakerTest`, `runDeadrootClubTest` (e os demais em `build.gradle`).
- Nunca afirme que um comando funciona sem tê-lo executado com sucesso. Falhas conhecidas: `docs/DEVELOPMENT.md`.

## Procedimento de implementação
1. Leia a seção "Memória do projeto".
2. `git status` antes de editar.
3. Mudança pequena e rastreável; uma intenção por commit (Conventional Commits em pt-BR sem acento, ex.: `feat(dragon): ...`).
4. Após editar: rode o menor comando que cubra a mudança (`compileJava`, GameTest da área ou `build`) e registre o resultado.
5. Atualize a documentação afetada no mesmo trabalho.
6. Resuma ao final: arquivos alterados, testes executados e resultado, pendências.

**Definição de pronto:** compila, testes relevantes passam, docs atualizadas. "A IA disse que terminou" não conta.

## Atualização de documentação
- Versão, loader, Java ou plugin → `gradle.properties`/`build.gradle` + `docs/DECISIONS.md` + `README.md`.
- Novo pacote, registro ou fluxo de inicialização → `docs/ARCHITECTURE.md`.
- Novo comando ou validação → `docs/DEVELOPMENT.md` (status "validado" só após execução real).
- Decisão arquitetural relevante → `docs/DECISIONS.md` (contexto, decisão, consequências). Não é diário.
- Concluído ou pendente → `docs/ROADMAP.md` e o resumo em `docs/CURRENT_STATE.md`.
- Problema difícil resolvido → registrar causa e solução em `docs/DEVELOPMENT.md`.
- Nunca marque como concluído sem evidência de execução.

## Trabalho em equipe e multiagente
- Há mais de uma pessoa e vários modelos trabalhando no repo. Não assuma que você é o único: confira `git status`/`git log` e não reverta mudanças que não são suas.
- Agentes paralelos usam branches ou `git worktree` separados e não editam os mesmos arquivos ao mesmo tempo.
- Não crie memória paralela (arquivos de notas por ferramenta). Fontes de continuidade: código, docs e Git.

## Economia de contexto
- Criação de mob: uma sessão por fatia, estado gravado em `docs/design/<mob>.md` (ver `docs/PROCESSO_MOB.md`). Ao ficar sem contexto, grave "Próximo passo" na ficha e abra sessão nova.
- Leia só os arquivos envolvidos e suas dependências diretas. Use busca antes de abrir arquivos grandes.
- Não despeje logs: redirecione para arquivo e leia só o trecho de erro.
- Prefira diffs pequenos e evite relatórios intermediários redundantes.
- Não rode `runClient`/`runServer` sem necessidade: use `compileJava`/`build`.

