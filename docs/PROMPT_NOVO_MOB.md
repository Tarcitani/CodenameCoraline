# Criar um mob (ou boss) no CodenameCoraline — ponto de entrada

Este arquivo é o índice. O prompt único e longo foi dividido em etapas curtas, porque sessões longas perdem qualidade e estouram o contexto.

| O que | Onde |
|---|---|
| Fluxo completo, modelos sugeridos, orçamento de contexto, revisão visual | `docs/PROCESSO_MOB.md` |
| Regras técnicas obrigatórias (6 regras, visual, animação, IA, boss, performance, fatias) | `docs/MOB_CONTRACT.md` |
| Ficha do mob (modelo para copiar) | `docs/design/_TEMPLATE_MOB.md` → `docs/design/<id>.md` |
| Prompt para rascunhar a ficha num chat sem acesso ao código | `docs/prompts/ficha_chatgpt.md` |
| Prompts de cada fatia | `docs/prompts/fatia_1_fundacao.md`, `fatia_2_visual.md`, `fatia_3_ia_combate.md`, `fatia_4_polimento.md` |
| Relatório final de verificação | `docs/prompts/relatorio.md` |
| Mapa de arquivos de um mob | `docs/ARCHITECTURE.md`, seção "Anatomia de um mob" |

## Resumo do fluxo

1. Pesquise e junte referências (você).
2. Rascunhe a ficha com `docs/prompts/ficha_chatgpt.md` e salve em `docs/design/<id>.md`.
3. Revise a ficha no Claude Code (sem `<...>`, com números, sem contradição).
4. Uma sessão por fatia, nesta ordem: Fundação → Visual e rigging → (**sua revisão visual**) → IA e combate → Polimento e docs.
5. Sessão nova para o relatório (`docs/prompts/relatorio.md`).
6. Commit só com sua autorização.

Em cada sessão o agente lê só `AGENTS.md`, a seção da fatia no contrato, a ficha e o mob de referência. O estado vive em "Estado de implementação" na ficha.

## Pontos que mudaram em relação à versão anterior

- A ficha saiu daqui e foi para `docs/design/_TEMPLATE_MOB.md`, com campos novos (escala multiplayer, persistência, leash, limite de spawn, tags de alvo) o campo `pkg` e a **animação em duas partes** (A: intenção e ticks na ficha; B: ângulos calculados na fatia 2).
- O relatório de entrega saiu daqui e foi para `docs/prompts/relatorio.md`.
- A regra "N rodadas consecutivas" (N pelo Perfil: 6 para boss) e a parada obrigatória entre fatias estão em `MOB_CONTRACT.md` (regra 6 e §8).
