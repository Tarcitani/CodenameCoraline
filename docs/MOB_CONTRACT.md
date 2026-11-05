# MOB_CONTRACT.md — Contrato de Engenharia e Padrões para Criaturas

Este documento é a especificação técnica permanente para criação e refatoração de entidades (mobs comuns, companheiros e bosses) no `CodenameCoraline`.
Todo agente e desenvolvedor deve cumprir estes contratos de engenharia.

---

## 1. As 6 Regras Inegociáveis (Tolerância Zero)

1. **Isolamento Total:** Trabalhe exclusivamente no repositório `CodenameCoraline`. É proibido ler, modificar ou referenciar arquivos de `C:\MinecraftDev\MeuPrimeiroMod`.
2. **Controle de Versão pelo Usuário:** Nunca execute `git commit` ou `git push` sem autorização explícita. Nunca use `git reset --hard` ou `git clean`. Deixe as alterações testadas e *staged-ready*.
3. **Servidor Autoritativo:** Toda lógica de combate, IA, dano, estados e dados reside estritamente no servidor (`entity/`). O cliente (`client/`) é exclusivamente visual (modelos, animações, efeitos, HUD).
4. **Ficha Completa (Sem Alucinação):** Se a ficha fornecida contiver campos vazios, contraditórios ou marcas `<...>` residuais, **pare imediatamente e pergunte ao usuário**. É proibido inventar atributos, lore ou ataques não especificados.
5. **Verificação Obrigatória:** Nenhuma tarefa é declarada pronta sem compilação limpa (`compileJava` ou `build`) e execução bem-sucedida das suítes de GameTest pertinentes.
6. **Tolerância Zero a Falhas Intermitentes:** A suíte de GameTests da entidade e as suítes de regressão devem passar em **N rodadas consecutivas**, com N definido pelo Perfil na ficha (`boss`: 6; `hostil`, `neutro` e `aliado`: 4; `invocação`: 3; tabela em `docs/PROCESSO_MOB.md`). Uma única falha em qualquer rodada reinicia a contagem e bloqueia a entrega até a causa raiz ser corrigida. Falhas intermitentes são bugs reais de temporização ou concorrência, não ruído.

---

## 2. Contrato Visual e Modelagem (Minecraft / Blockbench)

### 2.1 Silhueta e Proporções
- **Proibição do Bipedismo/Simetria Genérica:** É vedado o uso de proporções clássicas retas (modelo Steve/Zumbi: cabeça 8x8x8, tronco 8x12x4, membros 4x12x4). A criatura deve possuir uma silhueta marcante e assimétrica/não-ortodoxa conforme a Ficha.
- **Segmentação Real de Membros:** Proibido o uso de caixas monolíticas únicas para membros articulados. Exige-se divisão funcional (membro superior, inferior e extremidade de apoio/ataque). Estruturas flexíveis (caudas, pescoços, tentáculos) exigem segmentação encadeada de 3 a 6 partes.
- **Volume em Camadas (*Layer Inflation*):** Roupas, carapaças e adornos devem ser caixas filhas com expansão (*inflate*) de `0.25` a `0.5` blocos para evitar *z-fighting*.
- **Pivô (`PartPose`) vs Origem da Caixa (`CubeListBuilder`):**
  - O pivô (`PartPose.offset`) define o **ponto de articulação** onde a junta gira (ex.: ombro, cotovelo, base do pescoço).
  - O offset da caixa em `CubeListBuilder.addBox(x, y, z, ...)` define a posição geométrica dos vértices em relação a esse pivô.
  - Não tente alinhar pivôs alterando apenas o offset da caixa: o pivô deve coincidir com o ponto físico de dobra anatômica.

### 2.2 Texturização e Atlas
- **Resolução e Escala de Texel:**
  - O padrão é consistência de texel uniforme (16 pixels por bloco cúbico no mundo).
  - Em chefes gigantes ou criaturas com alta densidade de detalhes, uma escala de texel específica (ex.: atlas 128×128 ou 256×256) pode ser declarada e justificada na Ficha. O proibido é a mistura incoerente de resoluções adjacentes (*mixels*) sem intenção visual.
- **Paleta Tonal Intencional:**
  - Toda textura gerada via script em `tools/generate_<mob>_assets.py` deve utilizar pixel art discreta em 3 a 5 tons: sombra (faces inferiores/fendas), tom base, destaque (faces superiores) e linhas de contraste (*edge shading*) nas arestas. Proibido o uso de filtros fotográficos ou blur.
- **Mapeamento UV e Emissivos:**
  - Mapeamento UV rigorosamente sem sobreposição involuntária no atlas.
  - Camada emissiva (`_eyes.png` ou `_glow.png`) com fundo 100% transparente (`RGBA = 0,0,0,0`) e coordenadas UV idênticas às da textura base.

---

## 3. Contrato de Rigging e Animações

### 3.1 Ordem de Rotação e Cinemática de Membros
- **Convenção de Eixos do Minecraft:** O agente deve inspecionar as chamadas de transformação em `ModelPart` da versão 26.1.2 do projeto e documentar a ordem das rotações aplicada no código. As rotações em `PartPose` e keyframes utilizam **radianos**, não graus ($180^\circ = \pi \approx 3.1415\text{ rad}$).
- **Compensação de Armas Empunhadas:** Documente o ângulo base da empunhadura na mão. Ao rotacionar braços em animações de ataque, compense numericamente a rotação do cabo para que a ponta da arma siga a trajetória do golpe e não penetre o corpo da entidade.
- **Cinemática Corpórea:** Ataques e impactos devem envolver rotação do tronco, flexão de pernas e compensação da cabeça, transmitindo inércia e transferência de peso real.

### 3.2 Famílias de Animação Obrigatórias
1. **Repouso (Idle):**
   - Ciclo contínuo base (respiração/postura neutra).
   - **Mínimo de 3 variações pontuais sorteadas:** o servidor deve disparar a cada 100–200 ticks sem combate uma de três ações de repouso distintas especificadas na Ficha (com cooldown de transição).
2. **Locomoção:** Ciclo proporcional à velocidade real de deslocamento no mundo.
3. **Combate (As 4 Fases Mecânicas):**
   - *Fase 1: Antecipação (Windup):* telegrafia clara onde a criatura arma a ação e recua peso.
   - *Fase 2: Validação / Clímax (Trigger):* instante exato onde o servidor checa se o alvo ainda é atingível. Se o alvo escapou, o golpe é abortado (*anti-whiff*).
   - *Fase 3: Liberação / Impacto (Active/Hit):* disparo explosivo de dano, partículas e som (frequentemente 2 a 5 ticks).
   - *Fase 4: Amortecimento / Recuperação (Recovery):* retorno à base e janela justa de punição para contra-ataque.
4. **Reações e Estados:** Dano recebido (*hurt*), atordoamento (*stun*) e encerramento de ciclo (morte/desintegração).

---

## 4. Contrato de IA, Ecologia e Multiplayer

1. **Matriz de Alvos e Predicados:**
   - Metas configuradas com `NearestAttackableTargetGoal` devem utilizar predicados estritos definidos na Ficha para distinguir mortos-vivos, aldeões, golems e animais.
2. **Rotina Ociosa e Custo de CPU:**
   - Quando não há jogadores ou ameaças num raio ativo, a IA deve entrar em rotina ociosa (dormir, vigiar ou patrulhar), reduzindo o agendamento de caminhos pesados (*pathfinding*).
3. **Multiplayer e Histerese de Ameaça:**
   - A troca de foco entre múltiplos jogadores deve possuir histerese: requer dano substancial ou proximidade crítica para alterar o alvo, proibindo oscilação de rotação a cada tick.
   - Todo boss deve implementar respostas táticas a cerco (ataques em área 360°, repulsão ou saltos de desengajamento).
4. **Prevenção de Abuso de Altura (*Anti-Pillaring*):**
   - A IA nunca deve colidir continuamente contra a base de pilares se o alvo estiver inacessível no topo. Deve alternar para ataques à distância, quebra de apoio ou recuo tático.

---

## 5. Contrato de Bosses: Cinemática e Barra Temática

1. **Cinemática de Entrada Obrigatória:**
   - Duração e ação cênica definidas na Ficha.
   - Diretor de câmera no cliente com tarjas (*letterbox*), tremor de tela e título/epíteto. Cada boss tem seu diretor em `client/<mob>/` (ex.: `CollectorCinematicDirector`, que usa `ViewportEvent` e `RegisterKeyMappingsEvent` do NeoForge). Não há diretor genérico: ao criar um segundo boss com cinemática, extraia a parte comum antes de copiar código.
   - **Tecla de Pular:** Suporte à tecla unificada `key.codenamecoraline.skip_cinematic`.
   - **Proteção no Servidor:** Boss e jogadores recebem imunidade temporária durante a cinemática para impedir dano assimétrico antes do início formal da luta.
2. **Barra de Vida Temática Obrigatória:**
   - Moldura personalizada em `textures/gui/boss_bar/<id>.png` gerada por script em `tools/`.
   - Implementação de `ThemedBossBars.Style` registrada em `client/bossbar/ThemedBossBars.java`.
   - Cores de preenchimento distintas por estágio e marcadores de transição na trilha. Proibido o uso da boss bar vanilla.

---

## 6. Diretrizes de Performance, GC e Clean Code

### 6.1 Alocações e Garbage Collection
- **Caminhos Quentes (*Hot Paths*):** Proibido instanciar coleções (`new ArrayList`), vetores ou caixas delimitadoras a cada tick em loops contínuos de IA.
  - Reutilize coleções de classe limpando com `.clear()`.
  - Prefira variáveis primitivas (`double x, y, z`) ou `BlockPos.MutableBlockPos`.
  - Alocações de `Vec3` ou `AABB` são aceitáveis pontualmente onde a API vanilla exige (em ataques discretos, raycasts esparsos ou eventos de dano).
- **Lambdas e Streams:**
  - Proibido usar streams e instanciar lambdas a cada tick no loop de atualização.
  - Lambdas de configuração (como predicados em `registerGoals()`) são permitidos pois são criados uma única vez na inicialização da entidade.

### 6.2 Consultas e Varreduras Espaciais
- **Throttling de Consultas de Entidades:** Execute uma única consulta espacial ampla (`getEntitiesOfClass`) por tick quando necessário e filtre os subgrupos em memória.
- **Rastreamento de Lacaios:** Armazene UUIDs de lacaios em `Set<UUID>`, proibindo varreduras globais no mundo para contagem de servos.
- **Varredura de Blocos Escalonada:** Buscas em volumes 3D (ex.: 15×15 a 31×31 blocos) devem ser executadas com intervalo de 20 a 40 ticks ou distribuídas por fatias parciais (*interleaving*), abortando no primeiro bloco válido.
- **Cache de Linha de Visão:** O resultado de `hasLineOfSight()` deve ser cacheado para o tick corrente.

### 6.3 Políticas de Clean Code
- **Cláusulas de Guarda (*Early Return*):** Verifique pré-condições no início do método e retorne imediatamente, eliminando pirâmides de blocos `if`.
- **Métodos Curtos (SRP):** Subdivida métodos longos de IA em funções de 20 a 40 linhas com responsabilidades coesas.
- **Máquinas de Estado Explícitas (`enum State`):** Proibido o uso de múltiplos booleanos concorrentes soltos para controlar modos de combate.
- **Constantes Nomeadas:** Proibido o uso de números mágicos; declare constantes estáticas imutáveis descritivas.

### 6.4 Reutilização de Subsistemas Existentes (DRY Pragmático)
- Lacaios e invocações aliadas devem estender/implementar `PlayerSummon` (`entity/PlayerSummon.java`).
- Espíritos de triunfo pós-morte devem reutilizar `BossSpirit` (`entity/spirit/BossSpirit.java`).
- Bestiário deve plugar-se na infraestrutura de `Bestiary.java`.
- Sons devem ser mapeados em `sounds.json` reutilizando eventos e áudios do vanilla com pitch/volume customizado (D-010), reconhecendo as limitações acústicas do jogo base.

---

## 7. IDs de Eventos de Rede (`broadcastEntityEvent`)

- Para evitar colisões com eventos do vanilla (`LivingEntity`, `Monster`, `PathfinderMob` usam a faixa 0–99), toda entidade customizada deve utilizar **IDs ≥ 100**.
- Cada mob deve reservar um intervalo numérico exclusivo documentado em `docs/design/<mob>.md`.

---

## 8. Processo de Execução em Fatias (Checkpoints Obrigatórios)

Para evitar degradação de contexto em sessões longas, a implementação de um novo mob deve ser dividida em **4 Fatias Incrementais**:

| Fatia | Escopo da Entrega | Critério de Aceite / Validação |
|---|---|---|
| **Fatia 1: Fundação** | Tipos de entidade, atributos, registros, ovo de spawn, tags e esqueleto da classe. | `compileJava` aprovado e GameTest de registro/spawn aprovado. |
| **Fatia 2: Visual & Rigging** | Modelo Java, scripts de textura/UV em `tools/`, poses offline renderizadas e animações. | Script `tools/generate_<mob>_assets.py` executa sem erro; `Export<Mob>Model.java` exporta a geometria; `./gradlew -I tools/<id>-sketches.gradle export<Mob>SketchModel` e `generate_<id>_sketches.py` geram as poses (ver `tools/README.md`); [BOSS] script, PNG e `Style` da barra temática prontos para revisão visual; UVs sem colisão; `runClient` não acusa erro de carregamento de camada. |
| **Fatia 3: IA & Combate** | Máquina de estados, metas de combate, ataques com 4 fases, anti-whiff, cinemática de boss. | Suíte de GameTests de IA e ataques aprovada no servidor de teste. |
| **Fatia 4: Polimento & Docs** | Drops, itens especiais, Bestiário (textos e gravuras), sons, avanços, documentação completa. | `./gradlew build` e suítes completas executadas N rodadas seguidas sem falhas (N pelo Perfil, regra 6); docs atualizadas. |

*Ao final de cada fatia, o agente deve parar, apresentar o resultado e **aguardar confirmação explícita do usuário** antes de iniciar a próxima. "Pausar" significa encerrar o turno, não seguir adiante sozinho.*

Cada fatia deve ter uma pré-condição: a fatia anterior está marcada como concluída em `## Estado de implementação` de `docs/design/<mob>.md`. Se não estiver, a sessão deve parar e avisar.

