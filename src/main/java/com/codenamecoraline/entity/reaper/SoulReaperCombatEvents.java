package com.codenamecoraline.entity.reaper;

import com.codenamecoraline.CodenameCoraline;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/**
 * Global server event hooks for the Reaper of Wandering Souls:
 * <ul>
 *   <li>Awakening reveal protection (players within 24 blocks take no damage while the Reaper is awakening).</li>
 *   <li>Post-armor anti-instakill rules ({@link LivingDamageEvent.Pre}).</li>
 *   <li>Shield block habit tracking ({@link LivingShieldBlockEvent}).</li>
 *   <li>Provenance-based potion and Golden Apple nullification ({@link LivingEntityUseItemEvent.Start} / {@code Finish}).</li>
 * </ul>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class SoulReaperCombatEvents {
    public static final double REVEAL_PROTECTION_RADIUS = 24.0D;

    private SoulReaperCombatEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (victim instanceof Player player && isProtectedByAwakening(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof SoulReaper reaper) {
            float clamped = reaper.clampOutgoingDamagePostArmor(victim, event.getNewDamage());
            event.setNewDamage(clamped);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        Entity attacker = event.getDamageSource().getEntity();
        if (attacker instanceof SoulReaper reaper) {
            reaper.recordShieldBlock(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!isTrackedConsumable(stack)) {
            return;
        }
        List<SoulReaper> reapers = serverLevel.getEntitiesOfClass(
                SoulReaper.class,
                entity.getBoundingBox().inflate(SoulReaper.FOLLOW_RANGE),
                SoulReaper::isAlive);
        for (SoulReaper reaper : reapers) {
            reaper.recordPreConsumeSnapshot(entity);
        }
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!isTrackedConsumable(stack)) {
            return;
        }
        List<SoulReaper> reapers = serverLevel.getEntitiesOfClass(
                SoulReaper.class,
                entity.getBoundingBox().inflate(SoulReaper.FOLLOW_RANGE),
                SoulReaper::isAlive);
        for (SoulReaper reaper : reapers) {
            reaper.onConsumableFinished(serverLevel, entity, stack);
        }
    }

    public static boolean isTrackedConsumable(ItemStack stack) {
        return stack.is(Items.POTION)
                || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    public static boolean isProtectedByAwakening(Player player) {
        return !player.level().getEntitiesOfClass(
                SoulReaper.class,
                player.getBoundingBox().inflate(REVEAL_PROTECTION_RADIUS),
                r -> r.isAlive() && r.getState() == SoulReaper.State.AWAKENING).isEmpty();
    }
}

