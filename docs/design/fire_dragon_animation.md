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
| fly | 24 | left_wing_upper / rot | 0 → -8,58,-46; 6 → -12,68,-10; 12 → 8,76,48; 15 → 10,70,54; 19 → -4,50,4; 24 → -8,58,-46 | pendente de revisão visual |
| fly | 24 | left_wing_forearm / rot | 0 → 0,-12,16; 6 → 0,-22,6; 12 → 0,-18,-18; 15 → 0,-10,-26; 19 → 0,24,-44; 24 → 0,-12,16 | pendente de revisão visual |
| fly | 24 | left_wing_tip / rot | 0 → 0,-14,22; 6 → 0,-20,12; 12 → 0,-18,-10; 15 → 0,-12,-24; 19 → 0,16,-36; 24 → 0,-14,22 | pendente de revisão visual |
| fly | 24 | left_front_upper / rot | 0 → -48,0,0; 12 → -54,0,0; 18 → -44,0,0; 24 → -48,0,0 | pendente de revisão visual |
| fly | 24 | left_front_lower / rot | 0 → 72,0,0; 12 → 78,0,0; 18 → 66,0,0; 24 → 72,0,0 | pendente de revisão visual |
| fly | 24 | left_front_foot / rot | 0 → -24,0,0; 12 → -28,0,0; 24 → -24,0,0 | pendente de revisão visual |
| fly | 24 | left_hind_upper / rot | 0 → 42,0,0; 12 → 50,0,0; 18 → 36,0,0; 24 → 42,0,0 | pendente de revisão visual |
| fly | 24 | left_hind_lower / rot | 0 → 28,0,0; 12 → 20,0,0; 18 → 34,0,0; 24 → 28,0,0 | pendente de revisão visual |
| fly | 24 | left_hind_foot / rot | 0 → 24,0,0; 12 → 30,0,0; 24 → 24,0,0 | pendente de revisão visual |
| fly | 24 | right_wing_upper / rot | 0 → -8,-58,46; 6 → -12,-68,10; 12 → 8,-76,-48; 15 → 10,-70,-54; 19 → -4,-50,-4; 24 → -8,-58,46 | pendente de revisão visual |
| fly | 24 | right_wing_forearm / rot | 0 → 0,12,-16; 6 → 0,22,-6; 12 → 0,18,18; 15 → 0,10,26; 19 → 0,-24,44; 24 → 0,12,-16 | pendente de revisão visual |
| fly | 24 | right_wing_tip / rot | 0 → 0,14,-22; 6 → 0,20,-12; 12 → 0,18,10; 15 → 0,12,24; 19 → 0,-16,36; 24 → 0,14,-22 | pendente de revisão visual |
| fly | 24 | right_front_upper / rot | 0 → -48,0,0; 12 → -54,0,0; 18 → -44,0,0; 24 → -48,0,0 | pendente de revisão visual |
| fly | 24 | right_front_lower / rot | 0 → 72,0,0; 12 → 78,0,0; 18 → 66,0,0; 24 → 72,0,0 | pendente de revisão visual |
| fly | 24 | right_front_foot / rot | 0 → -24,0,0; 12 → -28,0,0; 24 → -24,0,0 | pendente de revisão visual |
| fly | 24 | right_hind_upper / rot | 0 → 42,0,0; 12 → 50,0,0; 18 → 36,0,0; 24 → 42,0,0 | pendente de revisão visual |
| fly | 24 | right_hind_lower / rot | 0 → 28,0,0; 12 → 20,0,0; 18 → 34,0,0; 24 → 28,0,0 | pendente de revisão visual |
| fly | 24 | right_hind_foot / rot | 0 → 24,0,0; 12 → 30,0,0; 24 → 24,0,0 | pendente de revisão visual |
| fly | 24 | torso / rot | 0 → 4,0,0; 6 → 2,0,0; 12 → -6,0,0; 16 → -4,0,0; 20 → 3,0,0; 24 → 4,0,0 | pendente de revisão visual |
| fly | 24 | neck_base / rot | 0 → -5,0,0; 6 → -3,0,0; 12 → 6,0,0; 16 → 4,0,0; 20 → -3,0,0; 24 → -5,0,0 | pendente de revisão visual |
| fly | 24 | neck_mid / rot | 0 → 2,0,0; 12 → -3,0,0; 24 → 2,0,0 | pendente de revisão visual |
| fly | 24 | head / rot | 0 → 2,0,0; 12 → -2,0,0; 24 → 2,0,0 | pendente de revisão visual |
| fly | 24 | tail_1 / rot | 0 → -6,0,0; 6 → 5,0,0; 12 → 8,0,0; 18 → -5,0,0; 24 → -6,0,0 | pendente de revisão visual |
| fly | 24 | tail_2 / rot | 0 → -10,0,0; 8 → 2,0,0; 15 → 12,0,0; 21 → -4,0,0; 24 → -10,0,0 | pendente de revisão visual |
| fly | 24 | tail_3 / rot | 0 → -8,0,0; 10 → -4,0,0; 18 → 14,0,0; 24 → -8,0,0 | pendente de revisão visual |
| fly | 24 | tail_4 / rot | 0 → -4,0,0; 12 → -8,0,0; 20 → 16,0,0; 24 → -4,0,0 | pendente de revisão visual |
| fly | 24 | torso / pos | 0 → 0,-1.0,0; 6 → 0,-0.4,0; 12 → 0,1.2,0; 16 → 0,1.8,0; 20 → 0,0.2,0; 24 → 0,-1.0,0 | pendente de revisão visual |
| takeoff | 16 | torso / rot | 0 → 0,0,0; 4 → 10,0,0; 8 → -4,0,0; 12 → -18,0,0; 16 → -10,0,0 | pendente de revisão visual |
| takeoff | 16 | neck_base / rot | 0 → 0,0,0; 4 → 14,0,0; 8 → -6,0,0; 12 → -12,0,0; 16 → -6,0,0 | pendente de revisão visual |
| takeoff | 16 | neck_mid / rot | 0 → 0,0,0; 4 → 6,0,0; 12 → -6,0,0; 16 → 0,0,0 | pendente de revisão visual |
| takeoff | 16 | head / rot | 0 → 0,0,0; 4 → -8,0,0; 12 → 10,0,0; 16 → 4,0,0 | pendente de revisão visual |
| takeoff | 16 | tail_1 / rot | 0 → 0,0,0; 4 → -8,0,0; 12 → 18,0,0; 16 → 10,0,0 | pendente de revisão visual |
| takeoff | 16 | tail_2 / rot | 0 → 0,0,0; 6 → -10,0,0; 14 → 16,0,0; 16 → 8,0,0 | pendente de revisão visual |
| takeoff | 16 | tail_3 / rot | 0 → 0,0,0; 8 → -8,0,0; 16 → 12,0,0 | pendente de revisão visual |
| takeoff | 16 | left_wing_upper / rot | 0 → 0,0,0; 4 → 0,35,-20; 8 → -10,68,-48; 12 → 8,74,52; 16 → -6,60,-32 | pendente de revisão visual |
| takeoff | 16 | left_wing_forearm / rot | 0 → 0,0,0; 4 → 0,18,10; 8 → 0,-18,12; 12 → 0,-18,-20; 16 → 0,-12,8 | pendente de revisão visual |
| takeoff | 16 | left_wing_tip / rot | 0 → 0,0,0; 6 → 0,10,12; 10 → 0,-18,10; 13 → 0,-16,-22; 16 → 0,-12,12 | pendente de revisão visual |
| takeoff | 16 | left_front_upper / rot | 0 → 0,0,0; 4 → -32,0,0; 9 → -10,0,0; 16 → -48,0,0 | pendente de revisão visual |
| takeoff | 16 | left_front_lower / rot | 0 → 0,0,0; 4 → 48,0,0; 9 → 18,0,0; 16 → 72,0,0 | pendente de revisão visual |
| takeoff | 16 | left_hind_upper / rot | 0 → 0,0,0; 4 → -38,0,0; 9 → 36,0,0; 16 → 42,0,0 | pendente de revisão visual |
| takeoff | 16 | left_hind_lower / rot | 0 → 0,0,0; 4 → 58,0,0; 9 → -12,0,0; 16 → 28,0,0 | pendente de revisão visual |
| takeoff | 16 | right_wing_upper / rot | 0 → 0,0,0; 4 → 0,-35,20; 8 → -10,-68,48; 12 → 8,-74,-52; 16 → -6,-60,32 | pendente de revisão visual |
| takeoff | 16 | right_wing_forearm / rot | 0 → 0,0,0; 4 → 0,-18,-10; 8 → 0,18,-12; 12 → 0,18,20; 16 → 0,12,-8 | pendente de revisão visual |
| takeoff | 16 | right_wing_tip / rot | 0 → 0,0,0; 6 → 0,-10,-12; 10 → 0,18,-10; 13 → 0,16,22; 16 → 0,12,-12 | pendente de revisão visual |
| takeoff | 16 | right_front_upper / rot | 0 → 0,0,0; 4 → -32,0,0; 9 → -10,0,0; 16 → -48,0,0 | pendente de revisão visual |
| takeoff | 16 | right_front_lower / rot | 0 → 0,0,0; 4 → 48,0,0; 9 → 18,0,0; 16 → 72,0,0 | pendente de revisão visual |
| takeoff | 16 | right_hind_upper / rot | 0 → 0,0,0; 4 → -38,0,0; 9 → 36,0,0; 16 → 42,0,0 | pendente de revisão visual |
| takeoff | 16 | right_hind_lower / rot | 0 → 0,0,0; 4 → 58,0,0; 9 → -12,0,0; 16 → 28,0,0 | pendente de revisão visual |
| takeoff | 16 | torso / pos | 0 → 0,0,0; 4 → 0,-1.8,0; 9 → 0,1.2,0; 16 → 0,2.2,0 | pendente de revisão visual |
| land | 16 | torso / rot | 0 → -14,0,0; 4 → -24,0,0; 8 → -10,0,0; 11 → 10,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | neck_base / rot | 0 → -8,0,0; 4 → 16,0,0; 8 → 8,0,0; 11 → 12,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | neck_mid / rot | 0 → 0,0,0; 4 → 8,0,0; 11 → -4,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | head / rot | 0 → 6,0,0; 4 → -12,0,0; 11 → -6,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | tail_1 / rot | 0 → 10,0,0; 4 → 22,0,0; 9 → 12,0,0; 12 → -6,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | tail_2 / rot | 0 → 8,0,0; 5 → 18,0,0; 12 → -8,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | tail_3 / rot | 0 → 6,0,0; 6 → 14,0,0; 13 → -6,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_wing_upper / rot | 0 → -6,65,-30; 4 → 18,74,-18; 8 → 12,62,26; 12 → 4,28,-8; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_wing_forearm / rot | 0 → 0,-16,0; 4 → 0,-22,-10; 8 → 0,12,-16; 12 → 0,16,-8; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_wing_tip / rot | 0 → 0,-14,0; 5 → 0,-18,-12; 10 → 0,10,-12; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_front_upper / rot | 0 → -46,0,0; 5 → -28,0,0; 9 → -18,0,0; 12 → -24,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_front_lower / rot | 0 → 68,0,0; 5 → 20,0,0; 9 → 14,0,0; 12 → 42,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_hind_upper / rot | 0 → 36,0,0; 5 → -36,0,0; 9 → -20,0,0; 12 → -30,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | left_hind_lower / rot | 0 → 24,0,0; 5 → 26,0,0; 9 → 18,0,0; 12 → 48,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_wing_upper / rot | 0 → -6,-65,30; 4 → 18,-74,18; 8 → 12,-62,-26; 12 → 4,-28,8; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_wing_forearm / rot | 0 → 0,16,0; 4 → 0,22,10; 8 → 0,-12,16; 12 → 0,-16,8; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_wing_tip / rot | 0 → 0,14,0; 5 → 0,18,12; 10 → 0,-10,12; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_front_upper / rot | 0 → -46,0,0; 5 → -28,0,0; 9 → -18,0,0; 12 → -24,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_front_lower / rot | 0 → 68,0,0; 5 → 20,0,0; 9 → 14,0,0; 12 → 42,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_hind_upper / rot | 0 → 36,0,0; 5 → -36,0,0; 9 → -20,0,0; 12 → -30,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | right_hind_lower / rot | 0 → 24,0,0; 5 → 26,0,0; 9 → 18,0,0; 12 → 48,0,0; 16 → 0,0,0 | pendente de revisão visual |
| land | 16 | torso / pos | 0 → 0,1.4,0; 5 → 0,0.8,0; 9 → 0,-0.4,0; 12 → 0,-1.8,0; 16 → 0,0,0 | pendente de revisão visual |
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
| sit | 12 | left_hind_upper / rot | 0 → 0,0,0; 12 → -52,0,0 | pendente de revisão visual |
| sit | 12 | right_hind_upper / rot | 0 → 0,0,0; 12 → -52,0,0 | pendente de revisão visual |
| sit | 12 | left_hind_lower / rot | 0 → 0,0,0; 12 → 74,0,0 | pendente de revisão visual |
| sit | 12 | right_hind_lower / rot | 0 → 0,0,0; 12 → 74,0,0 | pendente de revisão visual |
| sit | 12 | left_hind_foot / rot | 0 → 0,0,0; 12 → -22,0,0 | pendente de revisão visual |
| sit | 12 | right_hind_foot / rot | 0 → 0,0,0; 12 → -22,0,0 | pendente de revisão visual |
| sit | 12 | left_front_upper / rot | 0 → 0,0,0; 12 → 10,0,0 | pendente de revisão visual |
| sit | 12 | right_front_upper / rot | 0 → 0,0,0; 12 → 10,0,0 | pendente de revisão visual |
| sit | 12 | neck_base / rot | 0 → 0,0,0; 12 → 12,0,0 | pendente de revisão visual |
| sit | 12 | neck_mid / rot | 0 → 0,0,0; 12 → -4,0,0 | pendente de revisão visual |
| sit | 12 | tail_1 / rot | 0 → 0,0,0; 12 → 10,20,0 | pendente de revisão visual |
| sit | 12 | tail_2 / rot | 0 → 0,0,0; 12 → 4,24,0 | pendente de revisão visual |
| sit | 12 | tail_3 / rot | 0 → 0,0,0; 12 → 0,22,0 | pendente de revisão visual |
| sit | 12 | tail_4 / rot | 0 → 0,0,0; 12 → -2,16,0 | pendente de revisão visual |
| sit | 12 | torso / pos | 0 → 0,0,0; 12 → 0,-1.2,0 | pendente de revisão visual |
| stand | 12 | torso / rot | 0 → -12,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | left_hind_upper / rot | 0 → -52,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | right_hind_upper / rot | 0 → -52,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | left_hind_lower / rot | 0 → 74,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | right_hind_lower / rot | 0 → 74,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | left_hind_foot / rot | 0 → -22,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | right_hind_foot / rot | 0 → -22,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | left_front_upper / rot | 0 → 10,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | right_front_upper / rot | 0 → 10,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | neck_base / rot | 0 → 12,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | neck_mid / rot | 0 → -4,0,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | tail_1 / rot | 0 → 10,20,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | tail_2 / rot | 0 → 4,24,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | tail_3 / rot | 0 → 0,22,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | tail_4 / rot | 0 → -2,16,0; 12 → 0,0,0 | pendente de revisão visual |
| stand | 12 | torso / pos | 0 → 0,-1.2,0; 12 → 0,0,0 | pendente de revisão visual |
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
| rest_together | 100 | left_hind_upper / rot | 0 → 0,0,0; 30 → -52,0,0; 100 → -52,0,0 | pendente de revisão visual |
| rest_together | 100 | right_hind_upper / rot | 0 → 0,0,0; 30 → -52,0,0; 100 → -52,0,0 | pendente de revisão visual |
| rest_together | 100 | left_hind_lower / rot | 0 → 0,0,0; 30 → 74,0,0; 100 → 74,0,0 | pendente de revisão visual |
| rest_together | 100 | right_hind_lower / rot | 0 → 0,0,0; 30 → 74,0,0; 100 → 74,0,0 | pendente de revisão visual |
| rest_together | 100 | left_hind_foot / rot | 0 → 0,0,0; 30 → -22,0,0; 100 → -22,0,0 | pendente de revisão visual |
| rest_together | 100 | right_hind_foot / rot | 0 → 0,0,0; 30 → -22,0,0; 100 → -22,0,0 | pendente de revisão visual |
| rest_together | 100 | left_front_upper / rot | 0 → 0,0,0; 30 → 10,0,0; 100 → 10,0,0 | pendente de revisão visual |
| rest_together | 100 | right_front_upper / rot | 0 → 0,0,0; 30 → 10,0,0; 100 → 10,0,0 | pendente de revisão visual |
| rest_together | 100 | neck_base / rot | 0 → 0,0,0; 30 → 25,0,0; 100 → 25,0,0 | pendente de revisão visual |
| rest_together | 100 | neck_mid / rot | 0 → 0,0,0; 30 → -4,0,0; 100 → -4,0,0 | pendente de revisão visual |
| rest_together | 100 | tail_1 / rot | 0 → 0,0,0; 30 → 0,25,0; 100 → 0,25,0 | pendente de revisão visual |
| rest_together | 100 | tail_2 / rot | 0 → 0,0,0; 30 → 0,25,0; 100 → 0,25,0 | pendente de revisão visual |
| rest_together | 100 | tail_3 / rot | 0 → 0,0,0; 30 → 0,20,0; 100 → 0,20,0 | pendente de revisão visual |
| rest_together | 100 | tail_4 / rot | 0 → 0,0,0; 30 → -2,16,0; 100 → -2,16,0 | pendente de revisão visual |
| rest_together | 100 | head / rot | 0 → 0,0,0; 30 → 15,0,-10; 100 → 15,0,-10 | pendente de revisão visual |
| rest_together | 100 | torso / pos | 0 → 0,0,0; 30 → 0,-1.3,0; 100 → 0,-1.3,0 | pendente de revisão visual |
| bite_windup | 8 | torso / rot | 0 → 0,0,0; 8 → -6,0,0 | pendente de revisão visual |
| bite_windup | 8 | neck_base / rot | 0 → 0,0,0; 8 → -18,0,0 | pendente de revisão visual |
| bite_windup | 8 | neck_mid / rot | 0 → 0,0,0; 8 → -10,0,0 | pendente de revisão visual |
| bite_windup | 8 | head / rot | 0 → 0,0,0; 8 → -10,0,0 | pendente de revisão visual |
| bite_windup | 8 | jaw / rot | 0 → 0,0,0; 8 → 36,0,0 | pendente de revisão visual |
| bite_windup | 8 | tail_1 / rot | 0 → 0,0,0; 8 → 10,0,0 | pendente de revisão visual |
| bite_windup | 8 | torso / pos | 0 → 0,0,0; 8 → 0,0,0.9 | pendente de revisão visual |
| bite_strike | 4 | torso / rot | 0 → -6,0,0; 4 → 8,0,0 | pendente de revisão visual |
| bite_strike | 4 | neck_base / rot | 0 → -18,0,0; 4 → 20,0,0 | pendente de revisão visual |
| bite_strike | 4 | neck_mid / rot | 0 → -10,0,0; 4 → 14,0,0 | pendente de revisão visual |
| bite_strike | 4 | head / rot | 0 → -10,0,0; 4 → 6,0,0 | pendente de revisão visual |
| bite_strike | 4 | jaw / rot | 0 → 36,0,0; 4 → 2,0,0 | pendente de revisão visual |
| bite_strike | 4 | tail_1 / rot | 0 → 10,0,0; 4 → -8,0,0 | pendente de revisão visual |
| bite_strike | 4 | torso / pos | 0 → 0,0,0.9; 4 → 0,-0.4,-1.4 | pendente de revisão visual |
| bite_recovery | 8 | torso / rot | 0 → 8,0,0; 8 → 0,0,0 | pendente de revisão visual |
| bite_recovery | 8 | neck_base / rot | 0 → 20,0,0; 8 → 0,0,0 | pendente de revisão visual |
| bite_recovery | 8 | neck_mid / rot | 0 → 14,0,0; 8 → 0,0,0 | pendente de revisão visual |
| bite_recovery | 8 | head / rot | 0 → 6,0,0; 8 → 0,0,0 | pendente de revisão visual |
| bite_recovery | 8 | jaw / rot | 0 → 2,0,0; 8 → 0,0,0 | pendente de revisão visual |
| bite_recovery | 8 | tail_1 / rot | 0 → -8,0,0; 8 → 0,0,0 | pendente de revisão visual |
| bite_recovery | 8 | torso / pos | 0 → 0,-0.4,-1.4; 8 → 0,0,0 | pendente de revisão visual |
| breath_windup | 10 | torso / rot | 0 → 0,0,0; 10 → -8,0,0 | pendente de revisão visual |
| breath_windup | 10 | neck_base / rot | 0 → 0,0,0; 10 → -14,0,0 | pendente de revisão visual |
| breath_windup | 10 | neck_mid / rot | 0 → 0,0,0; 10 → 10,0,0 | pendente de revisão visual |
| breath_windup | 10 | head / rot | 0 → 0,0,0; 10 → -6,0,0 | pendente de revisão visual |
| breath_windup | 10 | jaw / rot | 0 → 0,0,0; 10 → 28,0,0 | pendente de revisão visual |
| breath_windup | 10 | left_wing_upper / rot | 0 → 0,0,0; 10 → 0,45,-20 | pendente de revisão visual |
| breath_windup | 10 | right_wing_upper / rot | 0 → 0,0,0; 10 → 0,-45,20 | pendente de revisão visual |
| breath_windup | 10 | tail_1 / rot | 0 → 0,0,0; 10 → 10,0,0 | pendente de revisão visual |
| breath_windup | 10 | torso / pos | 0 → 0,0,0; 10 → 0,-0.4,0.8 | pendente de revisão visual |
| breath_active | 24 | torso / rot | 0 → -4,0,0; 12 → -4,0,0; 24 → -4,0,0 | pendente de revisão visual |
| breath_active | 24 | neck_base / rot | 0 → 6,-14,0; 12 → 6,14,0; 24 → 6,-10,0 | pendente de revisão visual |
| breath_active | 24 | neck_mid / rot | 0 → -4,-10,0; 12 → -4,10,0; 24 → -4,-6,0 | pendente de revisão visual |
| breath_active | 24 | head / rot | 0 → -4,-12,0; 12 → -4,12,0; 24 → -4,-8,0 | pendente de revisão visual |
| breath_active | 24 | jaw / rot | 0 → 34,0,0; 12 → 32,0,0; 24 → 30,0,0 | pendente de revisão visual |
| breath_active | 24 | left_wing_upper / rot | 0 → 0,50,-22; 12 → 0,55,-18; 24 → 0,50,-22 | pendente de revisão visual |
| breath_active | 24 | right_wing_upper / rot | 0 → 0,-50,22; 12 → 0,-55,18; 24 → 0,-50,22 | pendente de revisão visual |
| breath_active | 24 | tail_1 / rot | 0 → 0,12,0; 12 → 0,-12,0; 24 → 0,10,0 | pendente de revisão visual |
| breath_active | 24 | torso / pos | 0 → 0,-0.2,-0.3; 24 → 0,-0.2,-0.3 | pendente de revisão visual |
| breath_recovery | 10 | torso / rot | 0 → -4,0,0; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | neck_base / rot | 0 → 6,-10,0; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | neck_mid / rot | 0 → -4,-6,0; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | head / rot | 0 → -4,-8,0; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | jaw / rot | 0 → 30,0,0; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | left_wing_upper / rot | 0 → 0,50,-22; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | right_wing_upper / rot | 0 → 0,-50,22; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | tail_1 / rot | 0 → 0,10,0; 10 → 0,0,0 | pendente de revisão visual |
| breath_recovery | 10 | torso / pos | 0 → 0,-0.2,-0.3; 10 → 0,0,0 | pendente de revisão visual |
| growl | 24 | torso / rot | 0 → 0,0,0; 12.0 → 8,0,0; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | neck_base / rot | 0 → 0,0,0; 12.0 → 14,0,0; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | head / rot | 0 → 0,0,0; 6 → -10,8,0; 14 → -10,-8,0; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | jaw / rot | 0 → 0,0,0; 6 → 26,0,0; 18 → 22,0,0; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | left_wing_upper / rot | 0 → 0,0,0; 12.0 → 0,38,-22; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | right_wing_upper / rot | 0 → 0,0,0; 12.0 → 0,-38,22; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | tail_1 / rot | 0 → 0,0,0; 12 → 0,14,0; 24 → 0,0,0 | pendente de revisão visual |
| growl | 24 | torso / pos | 0 → 0,0,0; 12.0 → 0,-0.8,0; 24 → 0,0,0 | pendente de revisão visual |
| claw_windup | 8 | torso / rot | 0 → 0,0,0; 8 → -12,-14,6 | pendente de revisão visual |
| claw_windup | 8 | neck_base / rot | 0 → 0,0,0; 8 → -14,10,0 | pendente de revisão visual |
| claw_windup | 8 | head / rot | 0 → 0,0,0; 8 → -10,8,0 | pendente de revisão visual |
| claw_windup | 8 | jaw / rot | 0 → 0,0,0; 8 → 32,0,0 | pendente de revisão visual |
| claw_windup | 8 | right_front_upper / rot | 0 → 0,0,0; 8 → -72,-18,24 | pendente de revisão visual |
| claw_windup | 8 | right_front_lower / rot | 0 → 0,0,0; 8 → 42,0,0 | pendente de revisão visual |
| claw_windup | 8 | left_wing_upper / rot | 0 → 0,0,0; 8 → 0,32,-18 | pendente de revisão visual |
| claw_windup | 8 | right_wing_upper / rot | 0 → 0,0,0; 8 → 0,-42,26 | pendente de revisão visual |
| claw_windup | 8 | tail_1 / rot | 0 → 0,0,0; 8 → 8,18,0 | pendente de revisão visual |
| claw_windup | 8 | torso / pos | 0 → 0,0,0; 8 → 0,0.6,0.8 | pendente de revisão visual |
| claw_strike | 6 | torso / rot | 0 → -12,-14,6; 6 → 8,16,-8 | pendente de revisão visual |
| claw_strike | 6 | neck_base / rot | 0 → -14,10,0; 6 → 18,-10,0 | pendente de revisão visual |
| claw_strike | 6 | head / rot | 0 → -10,8,0; 6 → 8,-6,0 | pendente de revisão visual |
| claw_strike | 6 | jaw / rot | 0 → 32,0,0; 6 → 4,0,0 | pendente de revisão visual |
| claw_strike | 6 | right_front_upper / rot | 0 → -72,-18,24; 6 → 46,16,-14 | pendente de revisão visual |
| claw_strike | 6 | right_front_lower / rot | 0 → 42,0,0; 6 → -18,0,0 | pendente de revisão visual |
| claw_strike | 6 | left_wing_upper / rot | 0 → 0,32,-18; 6 → 0,18,-10 | pendente de revisão visual |
| claw_strike | 6 | right_wing_upper / rot | 0 → 0,-42,26; 6 → 0,-20,12 | pendente de revisão visual |
| claw_strike | 6 | tail_1 / rot | 0 → 8,18,0; 6 → -6,-16,0 | pendente de revisão visual |
| claw_strike | 6 | torso / pos | 0 → 0,0.6,0.8; 6 → 0,-0.6,-1.6 | pendente de revisão visual |
| claw_recovery | 8 | torso / rot | 0 → 8,16,-8; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | neck_base / rot | 0 → 18,-10,0; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | head / rot | 0 → 8,-6,0; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | jaw / rot | 0 → 4,0,0; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | right_front_upper / rot | 0 → 46,16,-14; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | right_front_lower / rot | 0 → -18,0,0; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | left_wing_upper / rot | 0 → 0,18,-10; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | right_wing_upper / rot | 0 → 0,-20,12; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | tail_1 / rot | 0 → -6,-16,0; 8 → 0,0,0 | pendente de revisão visual |
| claw_recovery | 8 | torso / pos | 0 → 0,-0.6,-1.6; 8 → 0,0,0 | pendente de revisão visual |
| roar | 28 | torso / rot | 0 → 0,0,0; 6 → -16,0,0; 20 → -18,0,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | neck_base / rot | 0 → 0,0,0; 6 → -24,0,0; 20 → -26,0,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | neck_mid / rot | 0 → 0,0,0; 6 → -10,0,0; 14 → -12,4,0; 20 → -10,-4,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | head / rot | 0 → 0,0,0; 6 → -16,0,0; 14 → -18,6,0; 20 → -16,-6,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | jaw / rot | 0 → 0,0,0; 6 → 44,0,0; 20 → 42,0,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | left_wing_upper / rot | 0 → 0,0,0; 7 → -8,68,-38; 20 → -6,64,-34; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | right_wing_upper / rot | 0 → 0,0,0; 7 → -8,-68,38; 20 → -6,-64,34; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | left_wing_forearm / rot | 0 → 0,0,0; 8 → 0,-18,12; 20 → 0,-16,10; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | right_wing_forearm / rot | 0 → 0,0,0; 8 → 0,18,-12; 20 → 0,16,-10; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | tail_1 / rot | 0 → 0,0,0; 8 → 14,12,0; 18 → 14,-12,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | tail_2 / rot | 0 → 0,0,0; 10 → 10,16,0; 20 → 10,-16,0; 28 → 0,0,0 | pendente de revisão visual |
| roar | 28 | torso / pos | 0 → 0,0,0; 6 → 0,1.2,0; 20 → 0,1.2,0; 28 → 0,0,0 | pendente de revisão visual |

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
