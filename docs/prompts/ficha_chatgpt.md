# Prompt: rascunho da ficha (ChatGPT ou outro chat, sem acesso ao código)

Uso: cole este texto no chat, anexe `docs/design/_TEMPLATE_MOB.md` e suas referências/links. Devolva o resultado e salve como `docs/design/<id>.md`. Depois rode a revisão da ficha com o Claude Code.

=== INÍCIO ===

Você é designer de criaturas para um mod de Minecraft Java (NeoForge). Vou enviar um conceito, referências e o template de ficha. Preencha **todo o template**, sem alterar a estrutura nem os títulos das seções.

Regras:
1. Substitua todo `<...>`. Se faltar informação, proponha um valor concreto e marque com `(sugestão)`. Não deixe campo vazio.
2. Use números. Nada de "rápido", "forte" ou "fluido" sem valor: vida em pontos, ticks por fase de ataque, ângulos em graus por junta.
3. Escala Minecraft: jogador tem 1,8 blocos de altura e 20 de vida; zumbi tem 20 de vida e 3 de dano. Calibre atributos e dano por isso.
4. A silhueta deve fugir do corpo humano de blocos (assimetria, segmentos, proporções fora do padrão). Descreva cada junta e onde ela dobra.
5. Na seção 1.6 preencha só a **Parte A** (pose-chave em palavras e duração em ticks) para idle, andar, cada ataque (preparo, execução, recuperação), hurt e morte. **Não invente ângulos**: a Parte B é calculada depois, contra o código. Use nomes de partes do corpo consistentes com a hierarquia de juntas que você descrever.
5b. Em sons, descreva só o caráter de cada som. **Não escreva IDs de som** (`minecraft:entity....`): você não sabe quais existem na versão do mod.
6. Pelo menos 3 idles distintos e uma telegrafia clara para cada ataque.
7. Não escreva código nem plano de implementação. Só a ficha.
8. Ao final, liste em 5 linhas as maiores dúvidas ou riscos de design.

Conceito: `<cole aqui>`
Referências: `<links, imagens, trechos de lenda>`
Tipo desejado: `<comum | companheiro | mini-boss | boss>`

=== FIM ===
