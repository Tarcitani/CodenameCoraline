# Design: Redcap (Barrete Vermelho)

Status: **proposta**, ainda não implementada. Origem: dossiê fornecido pelo usuário em 2026-10-27. O prompt técnico do dossiê (MCreator, Blockbench AI, Fabric/Forge/Bedrock) não se aplica a este projeto, que usa NeoForge 26.1.2; por isso ele não foi copiado para cá.

## 1. Base folclórica

- Criatura da fronteira anglo-escocesa, pertencente à Corte Unseelie (hostil).
- Habita ruínas de castelos e torres defensivas onde houve batalhas ou massacres.
- Aparência: elfo velho, baixo e atarracado; dentes longos, garras de águia, olhos vermelho-fogo, cabelos grisalhos; pique na mão esquerda, gorro escarlate e botas pesadas de ferro.
- Precisa manter o gorro ensopado de sangue fresco. Se o sangue secar por completo, morre.
- Mata viajantes arremessando pedras das muralhas ou com o pique.
- Não pode ser vencido na força comum nem na corrida.
- Fraqueza: Escrituras ou crucifixo. Ele grita e evapora em chamas, deixando um dente.
- Referência: Robin Redcap, familiar de William de Soulis no Castelo Hermitage.

Fontes citadas no dossiê (Folklore Scotland, Spooky Scotland, Wikipedia): ainda não verificadas por este projeto.

## 2. Aparência

- Silhueta: 1,35 bloco de altura, 0,7 de largura, encurvada.
- Olhos com textura emissiva vermelho-fogo (`#FF1E00`).
- Gorro frígio com três estados, ligados a um valor sincronizado de saturação de sangue:
  1. Saturado: `#990000`.
  2. Secando: `#5C1A1A`.
  3. Ressecado: `#4A403D`.
- Pique de ferro enferrujado, na mão esquerda.
- Spawn egg: base `#3A3D40`, manchas `#A80000`.

## 3. Atributos

| Atributo | Valor |
|---|---|
| Vida máxima | 40 |
| Armadura | 8 |
| Resistência a knockback | 0,4 |
| Velocidade de movimento | 0,36 |
| Dano corpo a corpo (pique) | 7; 10 com gorro saturado (+ Wither I por 4 s) |
| Dano à distância (pedra) | 8; repulsão e Lentidão II por 2 s |
| Alcance de detecção | 32 blocos |

## 4. Comportamento (máquina de estados)

1. **Emboscada:** sem alvo, sobe em blocos altos e fica agachado. A 16 blocos ou menos, arremessa uma pedra e avança.
2. **Perseguição:** sons de passos de ferro, sem fugir de combate corpo a corpo.
3. **Ciclo do gorro:** temporizador de 1200 ticks. Sem dano a seres vivos em 60 s, passa a ressecar: 2 de dano a cada 3 s e 25% menos velocidade. Ao acertar um golpe ou abater, volta a saturado, cura 6 e ganha Força I por 8 s.
4. **Fraqueza sagrada:** armas com Smite causam dano extra e impedem regeneração do gorro por 5 s. Totem da Imortalidade ou livro encantado usado com botão direito a menos de 5 blocos, ou poção de cura/regeneração arremessada, faz o Redcap evaporar em chamas.

## 5. Drops

- Combate normal: 1–2 pepitas de ferro ou botas de ferro encantadas (15%); "Gorro Carmesim Ressecado" (5%).
- Banimento sagrado: 100% "Presa de Redcap".

## 6. Pontos a decidir antes da implementação

1. **Escopo da primeira fatia.** Este é o principal.
2. **Mão esquerda:** o pique na mão esquerda exige modelo e lógica de braço próprios. Mobs vanilla seguram itens na mão direita.
3. **Velocidade 0,36:** é cerca de 2,7 vezes a corrida do jogador. Pode ser alta demais; precisa de teste em jogo.
4. **Banimento por item:** o gatilho "botão direito a menos de 5 blocos" exige um evento próprio e pode ser simplificado.
5. **Estado do gorro:** deve ser um dado sincronizado da entidade, não uma tag NBT de item. Afeta a textura, o dano e o drop.
