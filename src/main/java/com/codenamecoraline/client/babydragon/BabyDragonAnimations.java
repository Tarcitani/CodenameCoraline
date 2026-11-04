package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.BabyDragon.Pose;
import com.codenamecoraline.entity.babydragon.DragonStage;
import java.util.EnumMap;
import net.minecraft.client.animation.AnimationDefinition;

/** Compatibility entry point for the original hatchling catalogue. */
public final class BabyDragonAnimations {
    public static final EnumMap<Pose, AnimationDefinition> ALL = DragonAnimations.forStage(DragonStage.BABY);
    private BabyDragonAnimations() {}
}
