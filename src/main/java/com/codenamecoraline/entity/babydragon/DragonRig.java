package com.codenamecoraline.entity.babydragon;

/** Generated alongside the real model; coordinates are model pixels except physical dimensions and saddle. */
public record DragonRig(float bodyY, float neckY, float neckZ, float midY, float midZ, float headY, float headZ, float mouthY, float mouthZ, float jawLever, float neckPitch, float midPitch, float width, float height, float eye, float saddleY, float saddleZ, float tailRootZ, float tailSegments, float tailLength) {
    public static DragonRig of(DragonStage stage) { return switch(stage) {
        case BABY -> new DragonRig(19.50000F, -1.20000F, -3.00000F, -0.10000F, -1.00000F, 0.00000F, -0.50000F, 1.00000F, -6.80000F, 3.50000F, -0.10000F, 0.06000F, 0.45000F, 0.72000F, 0.53000F, 0.30000F, 0.02000F, 3.45000F, 3.00000F, 3.40000F);
        case YOUNG -> new DragonRig(17.70000F, -1.70000F, -4.20000F, -0.30000F, -2.50000F, -0.30000F, -2.10000F, 1.10000F, -9.00000F, 4.50000F, -0.32000F, 0.14000F, 1.15000F, 1.85000F, 1.40000F, 0.83000F, 0.10000F, 4.83000F, 4.00000F, 4.30000F);
        case ADULT -> new DragonRig(16.50000F, -3.00000F, -6.30000F, -0.50000F, -4.30000F, -0.20000F, -4.10000F, 1.25000F, -11.70000F, 5.70000F, -0.62000F, 0.28000F, 2.65000F, 3.90000F, 2.85000F, 2.16000F, 0.20000F, 6.90000F, 6.00000F, 5.20000F);
    }; }
}
