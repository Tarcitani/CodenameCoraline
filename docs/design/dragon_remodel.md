# Remodelamento visual do dragão

## A. Resultado

O dragão existente mantém a mesma entidade e os três estágios de evolução. Cada estágio recebe geometria, textura, máscara emissiva e animações próprias. O focinho, a origem dos ataques, a posição da montaria e as dimensões usam o mesmo rig das partes visuais.

## B. Diferenças entre estágios

| Estágio | Anatomia | Dimensões da colisão (largura × altura) | Geometria |
|---|---|---:|---:|
| Bebê | Silhueta compacta e paleta original refinada | 0,45 × 0,72 | 106 partes, 167 cubos |
| Jovem | Corpo e pescoço alongados, asas e cauda mais desenvolvidas | 1,15 × 1,85 | 133 partes, 200 cubos |
| Adulto | Pescoço arqueado, cabeça afilada, chifres, dentes, quatro pernas com garras, asas articuladas e cauda de seis segmentos com chama | 2,65 × 3,90 | 178 partes, 259 cubos |

Os atlas são empacotados por cubo, sem sobreposição de UV; a paleta é amostrada da textura original e guardada em `tools/dragon_original_palette.json`. As asas possuem braço, cotovelo, punho, dedos e membranas. Os estágios têm 17 animações nativas cada.

## C. Arquivos principais

- `client/babydragon/DragonGeometry.java`, `DragonModel.java`, `BabyDragonModel.java`, `YoungDragonModel.java`, `AdultDragonModel.java`, `DragonAnimations.java`, `DragonGlowLayer.java` e renderizador/registro do cliente.
- `entity/babydragon/DragonRig.java` e `DragonAnatomy.java`, integração em `BabyDragon.java` e dimensões em `ModEntities.java`.
- Texturas de cada estágio e respectivas máscaras em `textures/entity/`; gravuras existentes do Bestiário regeneradas a partir do modelo.
- `tools/generate_dragon_remodel.py`, `tools/ExportBabyDragonModel.java` e scripts de prévia; `verification/DragonModelGameTests.java` e ajustes de fixture em `DragonEvolutionGameTests.java`.

## D. Prévias

Após gerar as prévias, consulte `build/baby-dragon-sketches/baby_views.png`, `young_views.png`, `adult_views.png`, `anatomy_clay.png`, `evolution.png`, `adult_attacks.png` e `animations.gif`. São renderizações dos modelos reais, incluindo vistas múltiplas e poses. Uma captura do Adulto no cliente está em `build/dragon-evolution-preview/screenshots/2026-11-04_15.18.06.png`. O diretório `build/` é gerado e pode ser recriado.

## E. Referência

O anexo recebido contém a especificação escrita, mas nenhuma imagem de referência. A comparação visual com essa imagem está pendente; não se afirma fidelidade a uma referência que não foi fornecida.

## F. Testes executados

- Verificação complementar no cliente: Bebê e Jovem observados e capturados em `2026-11-04_15.34.13.png` e `2026-11-04_15.33.50.png`; as duas transições por XP ocorreram no mesmo UUID `cf2f7ad5-b211-4cdc-930a-615290697fa4`. Inventário do Adulto abriu e transferiu os 64 salmões para o jogador; a pilha e o estágio Adulto permaneceram após fechar e reabrir.
- A inspeção encontrou câmera frontal de terceira pessoa dentro da cabeça longa. O atributo vanilla sincronizado `CAMERA_DISTANCE` agora acompanha a largura do rig (10,6 no Adulto), preservando o recuo da câmera contra paredes do vanilla. O teste de assento também verifica a distância necessária.
- A inicialização dedicada revelou reflexão sobre classes gráficas nas suítes de modelo do dragão, Quebra-Copas e Carmesim. `ModelGameTestRegistration` carrega essas suítes somente quando a propriedade opt-in correspondente está habilitada. Após a alteração: build e suítes **81/81, 35/35 e 34/34** aprovados (`%TEMP%/coraline-dragon-server-compatibility-tests.log`).
- `./gradlew build`: aprovado, incluindo os testes JUnit existentes.
- `./gradlew runBabyDragonTest`: 81 testes obrigatórios aprovados em seis rodadas consecutivas após isolar a IA nas fixtures de mecânica; uma execução final com o build também passou.
- Suítes de regressão: Basilisk 17, Lithophage 11, Last Gift Collector 18, Canopy 35, Deadroot 9 e Crimson 34 testes aprovados. O Cobrador apresentou uma falha intermitente em uma primeira execução e passou na repetição, sem alteração em seu código.
- Uma rodada anterior do dragão falhou no sopro porque o cérebro customizado movia a entidade durante o teste. A fixture passou a usar `setNoAi(true)` nos testes isolados; o teste de montaria mantém a IA habilitada quando necessário.
- Os testes adicionais verificam geometria e UV, animações, posição e direção do focinho frente ao modelo posado, proteção contra disparo através de parede, assento e inventário.

## G. Não verificado

A inspeção operacional no jogo foi executada: três modelos carregados, duas evoluções por XP, inventário, persistência, montagem/desmontagem, assento, câmera, batimentos, cauda, bola colossal com magma e sopro. No servidor dedicado, a câmera frontal ajustada e o sopro foram capturados em `2026-11-04_15.51.08.png` e `2026-11-04_15.51.22.png`. Avanço e subida foram medidos de `(1,316; -57,33641; 1,537)` para `(1,334; -57,01229; 2,040)`; captura `2026-11-04_15.54.52.png`. Para manter a entrada com a ferramenta, avanço/subida foram temporariamente vinculados ao botão esquerdo somente no cliente descartável; os atalhos originais foram restaurados após a inspeção.

O servidor dedicado e dois clientes reais de desenvolvimento conectaram simultaneamente como `Dev` e `DragonObserver`. A revisão visual simultânea **não foi concluída**: ao alternar para a segunda janela, o cliente do dono caiu duas vezes com `EXCEPTION_ACCESS_VIOLATION` em `glfw.dll+0x10fa1`, em `RenderSystem.pollEvents`. Evidências: `build/dragon-evolution-preview/hs_err_pid7184.log` e `hs_err_pid22604.log`. Uma única janela conectada ao servidor dedicado permaneceu estável e permitiu a inspeção dos controles e ataques pela rede. Isso confirma conexão real, mas não permite declarar aprovadas a observação simultânea nem condições de latência.

A comparação com a imagem ausente, o aceite estético do usuário, áudio, balanceamento prolongado, terreno complexo e multiplayer sob latência continuam sem validação. As capturas e os testes não substituem a avaliação subjetiva de sensação de combate.

A sessão final mostrou o Adulto no chão com asas recolhidas e patas apoiadas após reabrir. A descida sustentada pelo comando X até o contato com o chão não teve uma medição conclusiva na automação; esse trajeto ainda exige conferência manual. O avanço e a subida foram medidos, mas não se declara um percurso completo de voo validado em terreno real.

## H. Como revisar

Execute `./gradlew build`, `./gradlew runBabyDragonTest` e `./gradlew runClient`. No cliente, confira Bebê, Jovem e Adulto em repouso, caminhada, voo e ataques; observe asas, focinho, chama, colisão, assento e inventário. Verifique a evolução e a persistência após salvar e reabrir o mundo, além da sincronização em multiplayer.

As prévias podem ser reproduzidas com `python tools/generate_dragon_remodel.py` e os comandos de exportação descritos em `docs/DEVELOPMENT.md`.

Para a sessão de rede, `tools/dragon-network-preview.gradle` fornece `runDragonNetworkServer` e `runDragonNetworkOwner`; `tools/dragon-observer-preview.gradle` fornece `runDragonObserver`. O servidor de inspeção usa uma cópia do mundo em `build/dragon-network-preview/world`, propriedades restritas a `server-ip=127.0.0.1` e perfis locais (`online-mode=false`); nunca use essa configuração para um servidor público. A tentativa inicial por LAN recusou a sessão de desenvolvimento, por isso a verificação usou o servidor dedicado local.

## I. Riscos e pendências

A imagem de referência ausente impede a comparação solicitada. O Adulto tem silhueta ampla; a câmera de terceira pessoa foi corrigida e observada no jogo, mas terrenos apertados e sessões longas ainda merecem avaliação. A falha nativa com duas janelas impediu a revisão visual simultânea. A falha intermitente do Cobrador merece atenção se voltar a ocorrer.

## J. Preservação

O remodelamento reutiliza a entidade e o sistema de evolução existentes, sem GeckoLib ou nova biblioteca de runtime. XP, fogo, magma, montaria, inventário, domesticação e persistência continuam no sistema existente e estão cobertos pelas suítes automatizadas; a experiência completa em jogo ainda requer revisão humana. Nenhum commit ou push foi feito.
