package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.CodenameCoraline;
import java.util.Comparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class DragonEvents {
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % 5 == 0) DragonMagmaTrail.get(level).tick(level);
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level)
            for (BabyDragon dragon : level.getEntitiesOfClass(BabyDragon.class, event.getEntity().getBoundingBox().inflate(48)))
                dragon.awardKill(event.getEntity());
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !victim.isAlive()) return;
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        float finalDamage = event.getNewDamage();
        if (finalDamage < victim.getHealth() + victim.getAbsorptionAmount()) return;
        for (InteractionHand hand : InteractionHand.values()) {
            if (victim.getItemInHand(hand).has(DataComponents.DEATH_PROTECTION)) return;
        }
        BabyDragon protector = level.getEntitiesOfClass(BabyDragon.class, victim.getBoundingBox().inflate(32.0),
                dragon -> dragon.canProtectOwnerWithTotem(victim))
                .stream()
                .min(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(victim)))
                .orElse(null);
        if (protector != null && protector.tryActivateTotemForOwner(victim, source)) {
            event.setNewDamage(0.0F);
        }
    }
    private DragonEvents() {}
}
