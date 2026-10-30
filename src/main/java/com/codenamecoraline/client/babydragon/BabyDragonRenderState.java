package com.codenamecoraline.client.babydragon;

import com.codenamecoraline.entity.babydragon.BabyDragon.Pose;
import java.util.EnumMap;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public final class BabyDragonRenderState extends LivingEntityRenderState {
    public final EnumMap<Pose, AnimationState> animations = new EnumMap<>(Pose.class);
    public boolean shoulder, sleeping;
    public float flightBlend;
    public int blinkSeed;
    public BabyDragonRenderState() { for (Pose pose : Pose.values()) this.animations.put(pose, new AnimationState()); }
}
