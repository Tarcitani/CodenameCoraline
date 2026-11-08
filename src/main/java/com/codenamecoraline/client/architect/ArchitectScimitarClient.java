package com.codenamecoraline.client.architect;

import com.codenamecoraline.item.ArchitectScimitarItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jspecify.annotations.Nullable;

/**
 * Client side of the Architect's Scimitar (spec §26.3): the third-person arm pose and the first-person hand transform
 * while the guard is up, and the short counter pose after a parry lands. A parry is seen on the client as the moment
 * the held scimitar gains the charged-Reprise flag; no packet of its own.
 */
public final class ArchitectScimitarClient implements IClientItemExtensions {
    public static final ArchitectScimitarClient INSTANCE = new ArchitectScimitarClient();
    /** Ticks the counter pose lasts (2 out, 4 back). */
    static final int COUNTER_TICKS = 6;

    private static final Map<Integer, Boolean> WAS_CHARGED = new HashMap<>();
    private static final Map<Integer, Long> COUNTER_START = new HashMap<>();

    private ArchitectScimitarClient() {}

    private static boolean guarding(LivingEntity entity, ItemStack stack) {
        return entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.MAIN_HAND && entity.getUseItem() == stack;
    }

    /** Ticks since this entity's parry landed (fractional), or -1 when no counter is playing. */
    private static float counterAge(LivingEntity entity, float partialTick) {
        Long start = COUNTER_START.get(entity.getId());
        if (start == null) {
            return -1.0F;
        }
        float age = entity.level().getGameTime() - start + partialTick;
        return age < COUNTER_TICKS ? age : -1.0F;
    }

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (hand != InteractionHand.MAIN_HAND) {
            return null;
        }
        if (guarding(entity, stack)) {
            return ScimitarArmPoses.PARRY.getValue();
        }
        return counterAge(entity, 0.0F) >= 0.0F ? ScimitarArmPoses.COUNTER.getValue() : null;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm, ItemStack stack,
            float partialTick, float equipProcess, float swingProcess) {
        boolean guard = guarding(player, stack);
        float counter = counterAge(player, partialTick);
        if (!guard && counter < 0.0F) {
            return false; // vanilla swing and bob
        }
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        pose.translate(side * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F); // vanilla applyItemArmTransform
        if (guard) {
            float t = player.getTicksUsingItem(partialTick);
            float k = ScimitarArmPoses.raise(t);
            pose.translate(side * -0.25F * k, 0.15F * k, 0.0F);
            pose.mulPose(Axis.YP.rotationDegrees(side * -60.0F * k));
            pose.mulPose(Axis.ZP.rotationDegrees(side * 70.0F * k + ScimitarArmPoses.tremor(t, 0.6F)));
        } else {
            float a = counter < 2.0F ? counter / 2.0F : 1.0F - (counter - 2.0F) / (COUNTER_TICKS - 2.0F);
            pose.translate(0.0F, 0.0F, 0.05F * a);
            pose.mulPose(Axis.ZP.rotationDegrees(side * 12.0F * a));
        }
        return true;
    }

    /** Watches every player's held scimitar for the moment it becomes charged: that is the parry landing. */
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            WAS_CHARGED.clear();
            COUNTER_START.clear();
            return;
        }
        Map<Integer, Boolean> now = new HashMap<>();
        for (Player p : mc.level.players()) {
            boolean charged = ArchitectScimitarItem.isChargedStack(p.getMainHandItem());
            if (charged && !WAS_CHARGED.getOrDefault(p.getId(), false)) {
                COUNTER_START.put(p.getId(), mc.level.getGameTime());
            }
            now.put(p.getId(), charged);
        }
        WAS_CHARGED.clear();
        WAS_CHARGED.putAll(now);
        COUNTER_START.keySet().retainAll(now.keySet());
    }
}
