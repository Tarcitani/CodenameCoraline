# CLAUDE.md — Manual operacional do CodenameCoraline

Mod de Minecraft Java (NeoForge). Nome interno: `CodenameCoraline`. Mod ID: `codenamecoraline`. Nome provisório. Contém Redcap, Basilisco da Praga e Bestiário; estrutura atual em `docs/ARCHITECTURE.md`.

## Stack (versões verificadas)
- Minecraft 26.1.2 · NeoForge 26.1.2.124 · Java 25 (toolchain Gradle) · Gradle Wrapper 9.2.1 · plugin `net.neoforged.moddev` 2.0.148.
- Detalhes e motivos: `docs/DECISIONS.md`. Versões em `gradle.properties` e `build.gradle`.

## Isolamento e segurança (obrigatório)
- Trabalhe no workspace atual `C:\Users\Venti\CodenameCoraline`.
- Nunca leia para modificar, mova, renomeie, exclua ou reutilize arquivos de `C:\MinecraftDev\MeuPrimeiroMod`.
- Não altere configurações globais (Git, Claude Code, Java, Gradle, OMC). Use configuração local ao projeto.
- Não instale ferramentas globais sem autorização explícita.
- Não sobrescreva arquivos existentes silenciosamente. Não use `git reset --hard`, `git clean`, `rm -rf` amplo nem reinicialize o repositório.
- Não registre, imprima ou documente tokens, senhas ou chaves. Verifique segredos antes de cada commit.
- Commit e push só após confirmação explícita do usuário.

## Estrutura atual
```
build.gradle, settings.gradle, gradle.properties   # build e versões
gradlew, gradlew.bat, gradle/wrapper/              # Gradle Wrapper
src/main/java/com/codenamecoraline/
  CodenameCoraline.java                            # ponto de entrada (@Mod)
src/main/templates/META-INF/neoforge.mods.toml     # metadados, expandidos pelo Gradle
docs/                                              # ARCHITECTURE, DEVELOPMENT, DECISIONS, ROADMAP
```

## Convenções arquiteturais
- Monólito modular: um único módulo Gradle. Não crie subprojetos sem justificativa concreta.
- Pacotes são criados quando há requisito real. Não crie pacotes vazios nem interfaces genéricas.
- Ponto de entrada apenas carrega o mod e coordena registros. Lógica de gameplay não vai em renderizadores.
- Registros via `DeferredRegister` do NeoForge quando existirem (ver `docs/ARCHITECTURE.md`).
- Código e comentários em inglês; documentação em português.
- Nenhuma biblioteca/dependência sem finalidade concreta registrada em `docs/DECISIONS.md`.

## Comandos de desenvolvimento
Executados na raiz do projeto (Git Bash ou PowerShell):
- Compilar e verificar: `./gradlew build` (Windows: `gradlew.bat build`)
- Compilar sem testes: `./gradlew compileJava`
- Instância de desenvolvimento cliente: `./gradlew runClient`
- Servidor de desenvolvimento: `./gradlew runServer`

`./gradlew build` está validado (2026-10-27), inclusive pelo shell do agente no workspace atual. `.\gradlew.bat runBasiliskTest` executa a suíte de integração em `build/basilisk-gametest/`. Uma falha histórica de loopback está documentada em `docs/DEVELOPMENT.md`. Não afirme que um comando funciona sem tê-lo executado com sucesso.

## Procedimento de implementação e verificação
1. Leia este arquivo e `docs/ROADMAP.md` (estado atual e pendências).
2. Consulte `git status` antes de editar.
3. Mudança pequena e rastreável; uma intenção por commit.
4. Após editar: rode `./gradlew build` (ou o menor comando que cubra a mudança) e registre o resultado.
5. Atualize a documentação afetada (abaixo) no mesmo trabalho.
6. Resuma ao final: arquivos alterados, testes executados e resultado, pendências.

## Requisitos de atualização da documentação
- Mudança de versão, loader, Java ou plugin → `gradle.properties`/`build.gradle` + `docs/DECISIONS.md` (nova decisão) + `README.md` (tabela de versões).
- Novo pacote, ponto de registro ou fluxo de inicialização → `docs/ARCHITECTURE.md`.
- Novo comando ou procedimento de validação → `docs/DEVELOPMENT.md` (com status "validado" só após execução real).
- Decisão arquitetural relevante → `docs/DECISIONS.md` (contexto, decisão, consequências). Não é diário de mudanças.
- Concluído ou pendente de escopo → `docs/ROADMAP.md`.
- Nunca marque como concluído algo sem evidência de execução.

## Economia de contexto
- Leia somente os arquivos envolvidos e suas dependências diretas. Não releia o repositório inteiro.
- Use `Grep`/`Glob` antes de abrir arquivos grandes. Não despeje logs extensos: use `tail` ou filtros.
- Logs do Gradle: redirecione para arquivo fora do projeto (scratchpad) e leia apenas o trecho de erro.
- Prefira diffs pequenos. Não gere relatórios intermediários redundantes.
- Não crie memória própria paralela. Fontes de continuidade: código, docs e Git.
- Ao retomar em sessão nova: reconstrua o estado a partir de `git status`, `git log --oneline -5` e `docs/ROADMAP.md`.

## Economia de verificação
- Não execute `runClient`/`runServer` sem necessidade: baixam e processam o Minecraft. Use `compileJava`/`build` para validar código.
