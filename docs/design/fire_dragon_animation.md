# Dragão de Fogo — animações do bebê

Gerado por `tools/generate_fire_dragon_animations.py`. Parte B da ficha. Todas as linhas estão **pendentes de revisão visual humana**.
Rotações: deltas x/y/z em graus em relação ao bind pose; `KeyframeAnimations.degreeVec` converte para radianos. `ModelPart.translateAndRotate` aplica `Quaternionf.rotationZYX(zRot,yRot,xRot)`, confirmado nas fontes 26.1.2.124. Posição: unidades do modelo (1/16 bloco), convertidas por `posVec` (Y invertido). Curva LINEAR em todos os trechos; os pontos intermediários definem a aceleração e o amortecimento.

| Clipe | Ticks | Junta / canal | Keyframes: tick → x,y,z | Estado |
|---|---|---|---|---|
| breathe | 40 | torso / rot | 0 → 0,0,0; 20.0 → 1.5,0,0; 40 → 0,0,0 | pendente de revisão visual |
| breathe | 40 | neck_mid / rot | 0 → 0,0,0; 20.0 → -2,0,0; 40 → 0,0,0 | pendente de revisão visual |
| breathe | 40 | tail_2 / rot | 0 → 0,0,0; 20.0 → 0,3,0; 40 → 0,0,0 | pendente de revisão visual |
| idle_sniff | 30 | neck_base / rot | 0 → 0,0,0; 15.0 → -12,0,0; 30 → 0,0,0 | pendente de revisão visual |
| idle_sniff | 30 | head / rot | 0 → 0,0,0; 9 → -8,18,5; 20 → -5,-15,-5; 30 → 0,0,0 | pendente de revisão visual |
| idle_sniff | 30 | tail_1 / rot | 0 → 0,0,0; 15.0 → 0,-8,0; 30 → 0,0,0 | pendente de revisão visual |
| idle_wings | 36 | left_wing_upper / rot | 0 → 0,0,0; 11.88 → 0,30,-18; 36 → 0,0,0 | pendente de revisão visual |
| idle_wings | 36 | right_wing_upper / rot | 0 → 0,0,0; 24.12 → 0,-30,18; 36 → 0,0,0 | pendente de revisão visual |
| idle_wings | 36 | left_hind_upper / rot | 0 → 0,0,0; 18.0 → 5,0,0; 36 → 0,0,0 | pendente de revisão visual |
| idle_wings | 36 | right_hind_upper / rot | 0 → 0,0,0; 18.0 → -5,0,0; 36 → 0,0,0 | pendente de revisão visual |
| idle_tail | 40 | neck_base / rot | 0 → 0,0,0; 20.0 → 0,20,0; 40 → 0,0,0 | pendente de revisão visual |
| idle_tail | 40 | head / rot | 0 → 0,0,0; 20.0 → 5,30,12; 40 → 0,0,0 | pendente de revisão visual |
| idle_tail | 40 | tail_1 / rot | 0 → 0,0,0; 20.0 → 0,-20,0; 40 → 0,0,0 | pendente de revisão visual |
| idle_tail | 40 | tail_2 / rot | 0 → 0,0,0; 20.0 → 0,-18,0; 40 → 0,0,0 | pendente de revisão visual |
| idle_tail | 40 | jaw / rot | 0 → 0,0,0; 20.0 → 12,0,0; 40 → 0,0,0 | pendente de revisão visual |
| walk | 20 | left_front_upper / rot | 0 → 22,0,0; 10 → -22,0,0; 20 → 22,0,0 | pendente de revisão visual |
| walk | 20 | left_front_lower / rot | 0 → -8.8,0,0; 10 → 8.8,0,0; 20 → -8.8,0,0 | pendente de revisão visual |
| walk | 20 | left_hind_upper / rot | 0 → -22,0,0; 10 → 22,0,0; 20 → -22,0,0 | pendente de revisão visual |
| walk | 20 | left_hind_lower / rot | 0 → 8.8,0,0; 10 → -8.8,0,0; 20 → 8.8,0,0 | pendente de revisão visual |
| walk | 20 | right_front_upper / rot | 0 → -22,0,0; 10 → 22,0,0; 20 → -22,0,0 | pendente de revisão visual |
| walk | 20 | right_front_lower / rot | 0 → 8.8,0,0; 10 → -8.8,0,0; 20 → 8.8,0,0 | pendente de revisão visual |
| walk | 20 | right_hind_upper / rot | 0 → 22,0,0; 10 → -22,0,0; 20 → 22,0,0 | pendente de revisão visual |
| walk | 20 | right_hind_lower / rot | 0 → -8.8,0,0; 10 → 8.8,0,0; 20 → -8.8,0,0 | pendente de revisão visual |
| walk | 20 | tail_1 / rot | 0 → 0,-6,0; 10 → 0,6,0; 20 → 0,-6,0 | pendente de revisão visual |
| walk | 20 | torso / pos | 0 → 0,0,0; 5 → 0,0.3,0; 10 → 0,0,0; 15 → 0,0.3,0; 20 → 0,0,0 | pendente de revisão visual |
| fly | 16 | left_wing_upper / rot | 0 → 0,65,-35; 4 → 0,65,55; 8 → 0,65,-35; 12 → 0,65,55; 16 → 0,65,-35 | pendente de revisão visual |
| fly | 16 | left_wing_forearm / rot | 0 → 0,-18,0; 4 → 0,-18,-20; 8 → 0,-18,0; 12 → 0,-18,-20; 16 → 0,-18,0 | pendente de revisão visual |
| fly | 16 | left_wing_tip / rot | 0 → 0,-18,0; 8.0 → 0,-18,12; 16 → 0,-18,0 | pendente de revisão visual |
| fly | 16 | left_front_upper / rot | 0 → -35,0,0; 16 → -35,0,0 | pendente de revisão visual |
| fly | 16 | left_front_lower / rot | 0 → 55,0,0; 16 → 55,0,0 | pendente de revisão visual |
| fly | 16 | left_hind_upper / rot | 0 → -35,0,0; 16 → -35,0,0 | pendente de revisão visual |
| fly | 16 | left_hind_lower / rot | 0 → 55,0,0; 16 → 55,0,0 | pendente de revisão visual |
| fly | 16 | right_wing_upper / rot | 0 → 0,-65,35; 4 → 0,-65,-55; 8 → 0,-65,35; 12 → 0,-65,-55; 16 → 0,-65,35 | pendente de revisão visual |
| fly | 16 | right_wing_forearm / rot | 0 → 0,18,0; 4 → 0,18,20; 8 → 0,18,0; 12 → 0,18,20; 16 → 0,18,0 | pendente de revisão visual |
| fly | 16 | right_wing_tip / rot | 0 → 0,18,0; 8.0 → 0,18,-12; 16 → 0,18,0 | pendente de revisão visual |
| fly | 16 | right_front_upper / rot | 0 → -35,0,0; 16 → -35,0,0 | pendente de revisão visual |
| fly | 16 | right_front_lower / rot | 0 → 55,0,0; 16 → 55,0,0 | pendente de revisão visual |
| fly | 16 | right_hind_upper / rot | 0 → -35,0,0; 16 → -35,0,0 | pendente de revisão visual |
| fly | 16 | right_hind_lower / rot | 0 → 55,0,0; 16 → 55,0,0 | pendente de revisão visual |
| fly | 16 | torso / pos | 0 → 0,0,0; 8.0 → 0,1,0; 16 → 0,0,0 | pendente de revisão visual |
| takeoff | 16 | torso / rot | 0 → 0,0,0; 8.0 → -10,0,0; 16 → -10,0,0 | pendente de revisão visual |
| takeoff | 16 | left_wing_upper / rot | 0 → 0,0,0; 8.0 → 0,65,-35; 16 → 0,65,-35 | pendente de revisão visual |
| takeoff | 16 | right_wing_upper / rot | 0 → 0,0,0; 8.0 → 0,-65,35; 16 → 0,-65,35 | pendente de revisão visual |
| takeoff | 16 | torso / pos | 0 → 0,0,0; 6 → 0,-1,0; 16 → 0,2,0 | pendente de revisão visual |
| land | 16 | torso / rot | 0 → 0,0,0; 8.0 → 8,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_wing_upper / rot | 0 → 0,0,0; 8.0 → 0,65,-25; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_wing_upper / rot | 0 → 0,0,0; 8.0 → 0,-65,25; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | torso / pos | 0 → 0,0,0; 8.0 → 0,-1.2,0; 16 → 0,0,0 | pendente de revisão visual |
| fire_windup | 12 | torso / rot | 0 → 0,0,0; 12 → -7,0,0 | pendente de revisão visual |
| fire_windup | 12 | neck_base / rot | 0 → 0,0,0; 12 → -12,0,0 | pendente de revisão visual |
| fire_windup | 12 | neck_mid / rot | 0 → 0,0,0; 12 → 8,0,0 | pendente de revisão visual |
| fire_windup | 12 | head / rot | 0 → 0,0,0; 12 → -8,0,0 | pendente de revisão visual |
| fire_windup | 12 | jaw / rot | 0 → 0,0,0; 12 → 24,0,0 | pendente de revisão visual |
| fire_windup | 12 | tail_1 / rot | 0 → 0,0,0; 12 → 12,0,0 | pendente de revisão visual |
| fire_windup | 12 | left_hind_upper / rot | 0 → 0,0,0; 12 → 10,0,0 | pendente de revisão visual |
| fire_windup | 12 | right_hind_upper / rot | 0 → 0,0,0; 12 → 10,0,0 | pendente de revisão visual |
| fire_windup | 12 | torso / pos | 0 → 0,0,0; 12 → 0,-0.3,0.7 | pendente de revisão visual |
| fire_release | 1 | torso / rot | 0 → -7,0,0; 1 → 5,0,0 | pendente de revisão visual |
| fire_release | 1 | neck_base / rot | 0 → -12,0,0; 1 → 10,0,0 | pendente de revisão visual |
| fire_release | 1 | neck_mid / rot | 0 → 8,0,0; 1 → -10,0,0 | pendente de revisão visual |
| fire_release | 1 | head / rot | 0 → -8,0,0; 1 → -5,0,0 | pendente de revisão visual |
| fire_release | 1 | jaw / rot | 0 → 24,0,0; 1 → 32,0,0 | pendente de revisão visual |
| fire_release | 1 | tail_1 / rot | 0 → 12,0,0; 1 → -6,0,0 | pendente de revisão visual |
| fire_release | 1 | left_hind_upper / rot | 0 → 10,0,0; 1 → -4,0,0 | pendente de revisão visual |
| fire_release | 1 | right_hind_upper / rot | 0 → 10,0,0; 1 → -4,0,0 | pendente de revisão visual |
| fire_release | 1 | torso / pos | 0 → 0,-0.3,0.7; 1 → 0,0,-0.5 | pendente de revisão visual |
| fire_recovery | 15 | torso / rot | 0 → 5,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | neck_base / rot | 0 → 10,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | neck_mid / rot | 0 → -10,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | head / rot | 0 → -5,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | jaw / rot | 0 → 32,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | tail_1 / rot | 0 → -6,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | left_hind_upper / rot | 0 → -4,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | right_hind_upper / rot | 0 → -4,0,0; 15 → 0,0,0 | pendente de revisão visual |
| fire_recovery | 15 | torso / pos | 0 → 0,0,-0.5; 15 → 0,0,0 | pendente de revisão visual |
| hurt | 8 | torso / rot | 0 → 0,0,0; 2.0 → 0,0,-8; 8 → 0,0,0 | pendente de revisão visual |
| hurt | 8 | neck_base / rot | 0 → 0,0,0; 2.0 → -12,0,0; 8 → 0,0,0 | pendente de revisão visual |
| hurt | 8 | head / rot | 0 → 0,0,0; 2.0 → 10,0,0; 8 → 0,0,0 | pendente de revisão visual |
| stun | 20 | neck_base / rot | 0 → 0,0,0; 10.0 → 15,0,0; 20 → 0,0,0 | pendente de revisão visual |
| stun | 20 | head / rot | 0 → 0,0,0; 10.0 → 12,0,8; 20 → 0,0,0 | pendente de revisão visual |
| death | 20 | torso / rot | 0 → 0,0,0; 12 → 0,0,70; 20 → 0,0,85 | pendente de revisão visual |
| death | 20 | neck_base / rot | 0 → 0,0,0; 20 → 20,0,0 | pendente de revisão visual |
| death | 20 | tail_2 / rot | 0 → 0,0,0; 20 → 0,-15,0 | pendente de revisão visual |
| death | 20 | torso / pos | 0 → 0,0,0; 20 → 0,-2,0 | pendente de revisão visual |
| sit | 12 | torso / rot | 0 → 0,0,0; 12 → -12,0,0 | pendente de revisão visual |
| sit | 12 | left_hind_upper / rot | 0 → 0,0,0; 12 → -50,0,0 | pendente de revisão visual |
| sit | 12 | right_hind_upper / rot | 0 → 0,0,0; 12 → -50,0,0 | pendente de revisão visual |
| sit | 12 | left_hind_lower / rot | 0 → 0,0,0; 12 → 70,0,0 | pendente de revisão visual |
| sit | 12 | right_hind_lower / rot | 0 → 0,0,0; 12 → 70,0,0 | pendente de revisão visual |
| sit | 12 | neck_base / rot | 0 → 0,0,0; 12 → 12,0,0 | pendente de revisão visual |
| sit | 12 | tail_1 / rot | 0 → 0,0,0; 12 → 12,0,0 | pendente de revisão visual |
| sit | 12 | torso / pos | 0 → 0,0,0; 12 → 0,-1,0 | pendente de revisão visual |
| stand | 12 | torso / rot | 0 → -12,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | left_hind_upper / rot | 0 → -50,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | right_hind_upper / rot | 0 → -50,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | left_hind_lower / rot | 0 → 70,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | right_hind_lower / rot | 0 → 70,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | neck_base / rot | 0 → 12,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | tail_1 / rot | 0 → 12,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | torso / pos | 0 → 0,-1,0; 12 → 0,0,0 | pendente de revisão visual |
| shoulder | 40 | torso / rot | 0 → 6,-10,-8; 20 → 7,-10,-8; 40 → 6,-10,-8 | pendente de revisão visual |
| shoulder | 40 | neck_base / rot | 0 → 12,-18,6; 40 → 12,-18,6 | pendente de revisão visual |
| shoulder | 40 | neck_mid / rot | 0 → 6,-12,0; 20 → 7,-12,0; 40 → 6,-12,0 | pendente de revisão visual |
| shoulder | 40 | head / rot | 0 → 12,-12,8; 40 → 12,-12,8 | pendente de revisão visual |
| shoulder | 40 | tail_1 / rot | 0 → 4,42,-8; 40 → 4,42,-8 | pendente de revisão visual |
| shoulder | 40 | tail_2 / rot | 0 → -2,48,0; 40 → -2,48,0 | pendente de revisão visual |
| shoulder | 40 | tail_3 / rot | 0 → -4,45,0; 40 → -4,45,0 | pendente de revisão visual |
| shoulder | 40 | tail_4 / rot | 0 → -6,38,0; 20 → -6,40,0; 40 → -6,38,0 | pendente de revisão visual |
| shoulder | 40 | left_wing_upper / rot | 0 → 5,-20,12; 40 → 5,-20,12 | pendente de revisão visual |
| shoulder | 40 | right_wing_upper / rot | 0 → 5,20,-12; 40 → 5,20,-12 | pendente de revisão visual |
| shoulder | 40 | left_wing_forearm / rot | 0 → 0,55,-28; 40 → 0,55,-28 | pendente de revisão visual |
| shoulder | 40 | right_wing_forearm / rot | 0 → 0,-55,28; 40 → 0,-55,28 | pendente de revisão visual |
| shoulder | 40 | left_wing_tip / rot | 0 → 0,25,10; 40 → 0,25,10 | pendente de revisão visual |
| shoulder | 40 | right_wing_tip / rot | 0 → 0,-25,-10; 40 → 0,-25,-10 | pendente de revisão visual |
| shoulder | 40 | left_front_upper / rot | 0 → -65,0,0; 40 → -65,0,0 | pendente de revisão visual |
| shoulder | 40 | left_front_lower / rot | 0 → 95,0,0; 40 → 95,0,0 | pendente de revisão visual |
| shoulder | 40 | left_front_foot / rot | 0 → -30,0,0; 40 → -30,0,0 | pendente de revisão visual |
| shoulder | 40 | left_hind_upper / rot | 0 → -65,0,0; 40 → -65,0,0 | pendente de revisão visual |
| shoulder | 40 | left_hind_lower / rot | 0 → 95,0,0; 40 → 95,0,0 | pendente de revisão visual |
| shoulder | 40 | left_hind_foot / rot | 0 → -30,0,0; 40 → -30,0,0 | pendente de revisão visual |
| shoulder | 40 | right_front_upper / rot | 0 → -65,0,0; 40 → -65,0,0 | pendente de revisão visual |
| shoulder | 40 | right_front_lower / rot | 0 → 95,0,0; 40 → 95,0,0 | pendente de revisão visual |
| shoulder | 40 | right_front_foot / rot | 0 → -30,0,0; 40 → -30,0,0 | pendente de revisão visual |
| shoulder | 40 | right_hind_upper / rot | 0 → -65,0,0; 40 → -65,0,0 | pendente de revisão visual |
| shoulder | 40 | right_hind_lower / rot | 0 → 95,0,0; 40 → 95,0,0 | pendente de revisão visual |
| shoulder | 40 | right_hind_foot / rot | 0 → -30,0,0; 40 → -30,0,0 | pendente de revisão visual |
| shoulder | 40 | torso / pos | 0 → 0,1,0; 20 → 0,1.15,0; 40 → 0,1,0 | pendente de revisão visual |
| eat | 24 | neck_base / rot | 0 → 0,0,0; 12.0 → 20,0,0; 24 → 0,0,0 | pendente de revisão visual |
| eat | 24 | head / rot | 0 → 0,0,0; 12.0 → 12,0,0; 24 → 0,0,0 | pendente de revisão visual |
| eat | 24 | jaw / rot | 0 → 0,0,0; 6 → 18,0,0; 10 → 0,0,0; 14 → 18,0,0; 18 → 0,0,0; 24 → 0,0,0 | pendente de revisão visual |
| play_chase | 100 | torso / rot | 0 → -5,-8,0; 25 → -5,8,0; 50 → -5,-8,0; 75 → -5,8,0; 100 → -5,-8,0 | pendente de revisão visual |
| play_chase | 100 | tail_1 / rot | 0 → 0,15,0; 50 → 0,-15,0; 100 → 0,15,0 | pendente de revisão visual |
| play_chase | 100 | head / rot | 0 → 0,0,0; 50.0 → -10,12,0; 100 → 0,0,0 | pendente de revisão visual |
| greet | 30 | neck_base / rot | 0 → 0,0,0; 15.0 → -5,12,0; 30 → 0,0,0 | pendente de revisão visual |
| greet | 30 | head / rot | 0 → 0,0,0; 15.0 → 0,15,12; 30 → 0,0,0 | pendente de revisão visual |
| greet | 30 | tail_2 / rot | 0 → 0,0,0; 15.0 → 0,12,0; 30 → 0,0,0 | pendente de revisão visual |
| imitate_wings | 40 | left_wing_upper / rot | 0 → 0,0,0; 14.0 → 0,45,-25; 40 → 0,0,0 | pendente de revisão visual |
| imitate_wings | 40 | right_wing_upper / rot | 0 → 0,0,0; 26.0 → 0,-45,25; 40 → 0,0,0 | pendente de revisão visual |
| imitate_wings | 40 | head / rot | 0 → 0,0,0; 20.0 → -5,0,8; 40 → 0,0,0 | pendente de revisão visual |
| rest_together | 100 | torso / rot | 0 → 0,0,0; 30 → -12,0,0; 100 → -12,0,0 | pendente de revisão visual |
| rest_together | 100 | left_hind_upper / rot | 0 → 0,0,0; 30 → -50,0,0; 100 → -50,0,0 | pendente de revisão visual |
| rest_together | 100 | right_hind_upper / rot | 0 → 0,0,0; 30 → -50,0,0; 100 → -50,0,0 | pendente de revisão visual |
| rest_together | 100 | left_hind_lower / rot | 0 → 0,0,0; 30 → 70,0,0; 100 → 70,0,0 | pendente de revisão visual |
| rest_together | 100 | right_hind_lower / rot | 0 → 0,0,0; 30 → 70,0,0; 100 → 70,0,0 | pendente de revisão visual |
| rest_together | 100 | neck_base / rot | 0 → 0,0,0; 30 → 25,0,0; 100 → 25,0,0 | pendente de revisão visual |
| rest_together | 100 | tail_1 / rot | 0 → 0,0,0; 30 → 0,25,0; 100 → 0,25,0 | pendente de revisão visual |
| rest_together | 100 | head / rot | 0 → 0,0,0; 30 → 15,0,-10; 100 → 15,0,-10 | pendente de revisão visual |
| rest_together | 100 | tail_2 / rot | 0 → 0,0,0; 30 → 0,25,0; 100 → 0,25,0 | pendente de revisão visual |
| rest_together | 100 | tail_3 / rot | 0 → 0,0,0; 30 → 0,20,0; 100 → 0,20,0 | pendente de revisão visual |
| rest_together | 100 | torso / pos | 0 → 0,0,0; 30 → 0,-1.3,0; 100 → 0,-1.3,0 | pendente de revisão visual |

## Ombro: referência enrolada

Clipe de 40 ticks: patas recolhidas, asas fechadas sobre o dorso, cabeça acomodada e cauda curvada para dentro. Pequena respiração no tronco/pescoço e ajuste da ponta da cauda. No ombro direito, o renderizador espelha Y/Z das juntas centrais (tronco, pescoço, cabeça e cauda), incluindo a rotação de bind. Sem espelhar UVs ou escalar negativamente a malha. Pose e altura ainda aguardam teste visual do usuário.

## Bind pose e pivôs

Unidades: 1/16 bloco para offsets; graus para a rotação inicial. Juntas sem geometria continuam sendo pivôs reais. A camada ventral usa CubeDeformation 0,25 unidade do modelo, coerente com os mobs de referência; não são 0,25 bloco de expansão.

| Junta | Pai | Pivô x,y,z | Rotação base x,y,z |
|---|---|---|---|
| torso | root | (0, 19, 0) | (0, 0, 0) |
| belly | torso | (0, 0, 0) | (0, 0, 0) |
| neck_base | torso | (0, -1, -3) | (0, 0, 0) |
| neck_mid | neck_base | (0, -1, -1) | (0, 0, 0) |
| neck_top | neck_mid | (0, -1, -1) | (0, 0, 0) |
| head | neck_top | (0, 0, -1) | (0, 0, 0) |
| jaw | head | (0, 1.65, -1.5) | (0, 0, 0) |
| left_eye | head | (3.45, -0.7, -2.3) | (0, 0, 0) |
| left_horn | head | (2.4, -2, 0) | (-25, 0, 18) |
| left_horn_tip | left_horn | (0, -2.5, 0) | (-22, 0, 15) |
| left_wing_upper | torso | (2.8, -2, 1) | (0, -65, -40) |
| left_wing_forearm | left_wing_upper | (3, 0, 0) | (0, 18, 8) |
| left_wing_tip | left_wing_forearm | (3, 0, 0) | (0, 18, 0) |
| left_front_upper | torso | (2.5, 1, -2.1) | (0, 0, 0) |
| left_front_lower | left_front_upper | (0, 1.5, 0) | (0, 0, 0) |
| left_front_foot | left_front_lower | (0, 1.5, -0.5) | (0, 0, 0) |
| left_hind_upper | torso | (2.5, 1, 2.2) | (0, 0, 0) |
| left_hind_lower | left_hind_upper | (0, 1.5, 0) | (0, 0, 0) |
| left_hind_foot | left_hind_lower | (0, 1.5, -0.5) | (0, 0, 0) |
| right_eye | head | (-3.45, -0.7, -2.3) | (0, 0, 0) |
| right_horn | head | (-2.4, -2, 0) | (-25, 0, -18) |
| right_horn_tip | right_horn | (0, -2.5, 0) | (-22, 0, -15) |
| right_wing_upper | torso | (-2.8, -2, 1) | (0, 65, 40) |
| right_wing_forearm | right_wing_upper | (-3, 0, 0) | (0, -18, -8) |
| right_wing_tip | right_wing_forearm | (-3, 0, 0) | (0, -18, 0) |
| right_front_upper | torso | (-2.5, 1, -2.1) | (0, 0, 0) |
| right_front_lower | right_front_upper | (0, 1.5, 0) | (0, 0, 0) |
| right_front_foot | right_front_lower | (0, 1.5, -0.5) | (0, 0, 0) |
| right_hind_upper | torso | (-2.5, 1, 2.2) | (0, 0, 0) |
| right_hind_lower | right_hind_upper | (0, 1.5, 0) | (0, 0, 0) |
| right_hind_foot | right_hind_lower | (0, 1.5, -0.5) | (0, 0, 0) |
| tail_1 | torso | (0, 0, 3) | (-8, 0, 0) |
| tail_2 | tail_1 | (0, 0, 2.5) | (-4, 0, 0) |
| tail_3 | tail_2 | (0, 0, 2.5) | (-4, 7, 0) |
| tail_4 | tail_3 | (0, 0, 2.5) | (-4, 0, 0) |
| crest_0 | torso | (0, -3, -1) | (0, 0, 0) |
| crest_1 | torso | (0, -3, 1) | (0, 0, 0) |
| crest_2 | torso | (0, -3, 3) | (0, 0, 0) |
