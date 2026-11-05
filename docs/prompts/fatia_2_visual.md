# Prompt: Fatia 2 — Visual e rigging

Modelo sugerido: Opus ou Sonnet. É a fatia onde mais se perde qualidade: não economize aqui.

=== INÍCIO ===

MISSÃO: executar a **Fatia 2 (Visual e rigging)** do mob `<id>`.

Leia, nesta ordem e nada além disso:
1. `AGENTS.md` (Memória do projeto) e `docs/MOB_CONTRACT.md` §2 e §3.
2. `docs/design/<id>.md`: ficha completa, **seção 1.6 (Parte A: intenção e ticks; a Parte B é sua)** e Estado de implementação.
3. `tools/README.md`.
4. O mob de referência da ficha: `client/<ref_pkg>/*Model.java`, `*Animations.java`, `*RenderState.java`, `*Renderer.java`, `*EyesLayer.java`, `*Client.java`, e o script `tools/generate_<ref>_assets.py`.

Pré-condição: "1. Fundação" concluída. Se não, pare e avise.

Faça:
- Plano curto: hierarquia de juntas (com pivô de articulação físico de cada uma) e lista de arquivos. Aguarde "ok".
- `client/<pkg>/<Mob>Model.java` seguindo o contrato §2.1 (sem caixa monolítica, segmentação de 3 a 6 partes em cauda/pescoço, camadas com inflate 0,25 a 0,5, pivô correto).
- `tools/generate_<id>_assets.py` gerando textura e camada emissiva (3 a 5 tons, sem sobreposição de UV, fundo transparente no emissivo).
- `*Animations.java` com **cada animação da Parte A da seção 1.6** (duração em ticks e pose-chave). Você calcula os ângulos e as curvas contra o modelo real e o mob de referência. Em seguida, `*RenderState`, `*Renderer`, `*EyesLayer`, `*Client`.
- Preencha a **Parte B da seção 1.6** com os ângulos que o código usa de fato (inicial, final, curva) e marque cada linha como `pendente de revisão visual`.
- `tools/Export<Mob>Model.java` e `tools/generate_<id>_sketches.py` (+ `.gradle`) para gerar as poses offline; renderize idle, andar, preparo/impacto/recuperação de cada ataque.
- Quando um ângulo não funcionar visualmente, ajuste o código **e atualize a Parte B**; a ficha deve continuar igual ao que o código faz.
- Se o Gradle falhar com `Unable to establish loopback connection`, entregue ao usuário o comando exato para rodar no terminal dele e continue o que não depende dele (scripts Python).

Comando de aceite:
```bash
./gradlew compileJava > build/logs/fatia2_compile.log 2>&1
python tools/generate_<id>_assets.py
./gradlew <tarefa-de-sketches-do-mob> > build/logs/fatia2_sketches.log 2>&1
```
Confirme que não há sobreposição de UV (o script deve reportar ou ter checagem) e liste os caminhos dos PNG de pose para o usuário abrir.

Ao final: marque "2. Visual e rigging" como **em revisão** (não concluída). Entregue ao usuário a lista de poses e os comandos `/summon` para `runClient`. A fatia só vira "concluída" depois de "2b. Revisão visual" aprovada pelo usuário. PARE. Não commit sem autorização. Após a sua aprovação visual, sugira `feat(<id>): fatia 2 - visual`.

=== FIM ===
