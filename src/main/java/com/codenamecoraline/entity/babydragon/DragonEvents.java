package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    private DragonEvents() {}
}
