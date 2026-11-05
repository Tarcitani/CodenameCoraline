# Prompt: Relatório de entrega e verificação

Use uma **sessão nova**, de preferência com contexto limpo (`/clear`). O relatório deve ser feito por quem não implementou: leia as evidências, não confie no que o estado diz.

=== INÍCIO ===

MISSÃO: verificar e relatar a entrega do mob `<id>`.

Leia: `AGENTS.md` (Memória do projeto), `docs/design/<id>.md`, e o `git status` / `git diff --stat`.

Faça:
1. Para cada item da ficha, procure a evidência no código (arquivo, classe, método, linha) ou no teste. Marque **PASSOU** só com evidência; caso contrário **BLOQUEADO**.
2. Rode o comando de aceite pelo menos uma vez e anexe o resultado (não confie em logs antigos).
3. Verifique: nenhum `<...>` na ficha; nenhum `TODO`, `test.skip` ou ramo não implementado; textos em `en_us` e `pt_br`; sem segredos nos arquivos novos.
4. Entregue o relatório com as seções:
   A. Resumo executivo
   B. Matriz de conformidade com evidências
   C. Decisões técnicas (links para `docs/DECISIONS.md`)
   D. Evidências de teste (comando, rodadas, resultado)
   E. Artes e poses (scripts, UV sem sobreposição, poses renderizadas)
   F. Checklist manual de `runClient` (`/summon`, passos, pontos visuais e sonoros)
   G. Arquivos criados/alterados (prontos para o commit que o usuário autorizar)
   H. Pendências e próximos passos

Não commit. Não corrija nada nesta sessão: relate.

=== FIM ===
