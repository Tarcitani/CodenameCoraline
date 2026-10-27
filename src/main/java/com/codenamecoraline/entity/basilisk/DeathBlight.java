package com.codenamecoraline.entity.basilisk;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModAttachments;
import com.codenamecoraline.registry.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jspecify.annotations.Nullable;

/** All damage and status changes run on the logical server. The HUD reads a synced attachment. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class DeathBlight {
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "death_blight"));
    public static final DustParticleOptions GOLD = new DustParticleOptions(0xC59B27, 0.8F);
    private static final DustParticleOptions ROOT = new DustParticleOptions(0x171D18, 1.2F);

    /** Transient attachment: never stores entity references in a global collection. */
    public static final class Exposure {
        public final DeathBlightMeter meter = new DeathBlightMeter();
        public long lastExposure = Long.MIN_VALUE;
        public @Nullable LivingEntity owner;
        public int rootedTicks;
        public Vec3 rootPosition = Vec3.ZERO;
    }

    private DeathBlight() {}

    public static boolean susceptible(LivingEntity entity) {
        return entity.isAlive() && !(entity instanceof Basilisk)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    public static void expose(LivingEntity entity, @Nullable LivingEntity owner) {
        if (!susceptible(entity)) return;
        Exposure exposure = entity.getData(ModAttachments.BLIGHT_EXPOSURE);
        exposure.lastExposure = entity.level().getGameTime();
        exposure.owner = owner;
    }

    public static int buildup(LivingEntity entity) {
        return entity.hasData(ModAttachments.DEATH_BLIGHT) ? entity.getData(ModAttachments.DEATH_BLIGHT) : 0;
    }

    public static boolean warded(LivingEntity entity) {
        return entity.getMainHandItem().is(ModItems.BASILISK_WARD.get())
                || entity.getOffhandItem().is(ModItems.BASILISK_WARD.get());
    }

    public static void clear(LivingEntity entity) {
        if (entity.hasData(ModAttachments.DEATH_BLIGHT)) entity.setData(ModAttachments.DEATH_BLIGHT, 0);
        if (entity.hasData(ModAttachments.BLIGHT_EXPOSURE)) {
            Exposure exposure = entity.getData(ModAttachments.BLIGHT_EXPOSURE);
            exposure.meter.reset();
            exposure.lastExposure = Long.MIN_VALUE;
            exposure.owner = null;
        }
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || !(entity.level() instanceof ServerLevel level)) return;
        if (!entity.hasData(ModAttachments.BLIGHT_EXPOSURE) && !entity.hasData(ModAttachments.DEATH_BLIGHT)) return;
        if (!susceptible(entity)) {
            clear(entity);
            return;
        }
        Exposure exposure = entity.getData(ModAttachments.BLIGHT_EXPOSURE);
        if (exposure.rootedTicks > 0) {
            // A short root also prevents a totem survivor immediately triggering a second proc.
            entity.teleportTo(exposure.rootPosition.x, exposure.rootPosition.y, exposure.rootPosition.z);
            entity.setDeltaMovement(Vec3.ZERO);
            if (exposure.rootedTicks-- % 4 == 0) roots(level, exposure.rootPosition);
            return;
        }
        boolean exposed = exposure.lastExposure >= level.getGameTime() - 1;
        int previous = buildup(entity);
        int amount = exposure.meter.tick(previous, exposed, warded(entity));
        if (!exposed) exposure.owner = null;
        if (amount >= DeathBlightMeter.MAX) {
            LivingEntity owner = exposure.owner;
            clear(entity);
            exposure.rootedTicks = 20;
            exposure.rootPosition = entity.position();
            roots(level, entity.position());
            level.playSound(null, entity.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.65F, 0.7F);
            // Bypasses armor, but deliberately preserves vanilla totem and creative protection.
            entity.hurtServer(level, new DamageSource(level.registryAccess().holderOrThrow(DAMAGE), owner), 100.0F);
        } else if (amount != previous) {
            entity.setData(ModAttachments.DEATH_BLIGHT, amount);
        }
    }

    private static void roots(ServerLevel level, Vec3 position) {
        for (int branch = 0; branch < 5; branch++) {
            double angle = branch * Math.PI * 2.0 / 5.0;
            for (int step = 0; step < 9; step++) {
                double height = step * 0.2;
                double radius = 0.65 * (1.0 - step / 12.0);
                level.sendParticles(ROOT, position.x + Math.cos(angle + step * 0.13) * radius,
                        position.y + height, position.z + Math.sin(angle + step * 0.13) * radius, 1, 0, 0, 0, 0);
            }
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, position.x, position.y + 0.4, position.z, 8, 0.4, 0.4, 0.4, 0.02);
    }

    @SubscribeEvent
    public static void onConsume(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) return;
        var item = event.getItem();
        if (item.is(Items.MILK_BUCKET) || item.is(Items.HONEY_BOTTLE)
                || item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            clear(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onWitherDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypes.WITHER) && warded(event.getEntity())) {
            event.setAmount(event.getAmount() * 0.5F);
        }
    }
}
