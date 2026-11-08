# 01 — Sewing Arachnid (Aracnídea de Costura)

Ficha de planejamento da primeira marionete. Etapas 1 e 2 do processo; as etapas 3 a 6 (animações, catálogo, especificação técnica e prompt de implementação) ainda não foram feitas.

> Status: **planejamento**. Nada implementado nesta ficha. O modelo atual (`tools/generate_ten_threads_witch_assets.py`, `sewing_arachnid`) ainda é o antigo e precisa ser refeito conforme a seção 1.5.

## 1. Conceito e identidade

### 1.1 Natureza física (decidido)
Aranha-marionete: corpo de aranha construído como uma marionete grotesca, misturando elementos orgânicos, mecânicos e de costura. Não é humanoide. Foi montada, na fantasia do mod, pela própria Matriarca.

### 1.2 Aparência (decidido)
- **Corpo**: volumoso, arredondado e arqueado, lembrando um casulo de tecido remendado.
- **Revestimento**: tecidos envelhecidos, costuras aparentes, remendos, rasgos e partes desgastadas.
- **Patas**: estruturas mecânicas articuladas, com segmentos de materiais diferentes, faixas de tecido e juntas expostas.
- **Extremidades**: pontiagudas, como agulhas e instrumentos de costura.
- **Aparência geral**: assimétrica, grotesca e parcialmente orgânica.
- **Postura**: corpo próximo ao chão, patas distribuídas ao redor.

### 1.3 Anatomia (decidido)
| Parte | Função |
|---|---|
| 6 patas locomotoras | sustentação, caminhada, corrida, escalada e estabilidade |
| 2 manipuladores **simétricos** | agarrar, prender, perfurar e costurar |
| Pinças nos manipuladores | duas ou três pontas articuladas, abrem, fecham e se movem de forma independente |
| Corpo | volumoso, arqueado, com tecidos remendados |

Os manipuladores são iguais em formato e capacidade. Não há braço especializado.

### 1.4 Personalidade e comportamento (decidido)
**Agitada e instável.** Tem pressa, não fica parada e reage a tudo. Os movimentos são bruscos, com paradas súbitas e retomadas em outra direção. Tiques de costura: as pinças se abrem e fecham sem motivo aparente, as pontas batem no chão e as patas tremem quando ela está em alerta. Em combate, prefere ataques rápidos e repetidos a um golpe pesado, e perde a paciência quando o jogador se afasta.

### 1.5 Divergência com o modelo atual (a corrigir)
| Item | Modelo atual | Decidido |
|---|---|---|
| Patas locomotoras | 8 (4 pares) | 6 |
| Braços | 2, sem pinça (2 caixas cada) | 2 manipuladores com pinça articulada |
| Mecanismo | sem articulação de pinça | pontas articuladas, abrem e fecham |

A troca altera o modelo, as partes (nomes de `TenThreadsParts`), as animações (papéis `LEG` e `ARM`) e os GameTests de contagem. Deve ser feita antes da etapa 3.

## 2. Combate (rascunho para decisão)

Ainda não decidido. Itens que precisam de definição:

- **Ataques com manipuladores**: quais combinações simétricas (um braço, os dois, varredura, perfuração em linha reta).
- **Ataques com pernas**: o salto com as seis patas, o ataque de empurrão com o corpo.
- **Ataque característico**: definir um ataque que só ela faça (ex.: costura de uma área, prender o jogador com linha).
- **Defesa**: se fecha as pinças, se encolhe sobre as patas, se usa o corpo como escudo.
- **Mecânicas próprias**: a definir. Precisa ser coerente com o papel do jogo (o 1º estágio da sequência de dez).
- **Movimentação**: caminhada, escalada de paredes e teto, recuo e aproximação.

## 3. Pendências antes da etapa 3
1. Escolha dos ataques e mecânicas de combate (seção 2).
2. Aprovação da correção do rig (1.5).

## 2.1 Decisões de combate (uma pergunta por vez)
- **Ataque principal: ataque de costura (agarrar e prender).** Uma pinça prende o jogador por alguns instantes e a outra perfura. O jogador tem uma janela para se soltar, o que dá tempo de reação.
- **Defesa de perto: recua rápido e volta.** Salto curto para trás, pinças abertas, e volta para a pressão logo depois.
- **Mecânica própria: remenda a si mesma.** Ela recupera vida ao costurar um ponto em si mesma, mas só enquanto o jogador está preso pelo ataque de costura. Recompensa o agarrar.

## 2.2 Movimentação
- **Salta entre pontos.** Em vez de andar, dá saltos curtos e bruscos entre pontos do cenário. A leitura para o jogador fica mais difícil, então a animação precisa marcar bem a saída e a chegada de cada salto.
