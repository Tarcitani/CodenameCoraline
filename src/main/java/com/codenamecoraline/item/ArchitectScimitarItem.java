package com.codenamecoraline.item;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.registry.ModSounds;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * The Architect's Scimitar (docs/design/architect.md §26): the dominant blade of his upper right arm. Right-click raises
 * it for the <b>Parry</b>: during ticks {@link #WINDOW_START}-{@link #WINDOW_END} a blow or projectile coming from the
 * front cone ({@link #PARRY_HALF_ARC}°) is cancelled, the attacker loses its footing and the blade charges the
 * <b>Reprise</b>: the next melee hit within {@link #REPRISE_TICKS} ticks deals {@link #REPRISE_MULTIPLIER}× damage.
 * A missed parry costs a longer cooldown. Explosions, magic, fire and falls are never parried.
 *
 * <p>State lives on the server, per player, and advances in the server tick (so it also runs for GameTest mock
 * players). While the Reprise is charged the stack carries custom-model-data flag 0, which the item model uses to light
 * the blade and the client uses for the counter pose.</p>
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class ArchitectScimitarItem extends Item {
    public static final int USE_TICKS = 16;
    public static final int WINDOW_START = 2;
    /** Exclusive: the window is ticks 2..9 (8 ticks). */
    public static final int WINDOW_END = 10;
    public static final float PARRY_HALF_ARC = 60.0F;
    public static final int SUCCESS_COOLDOWN = 60;
    public static final int FAIL_COOLDOWN = 100;
    public static final int REPRISE_TICKS = 40;
    public static final float REPRISE_MULTIPLIER = 1.6F;
    public static final int DURABILITY_COST = 2;
    public static final int STRONG_PROJECTILE_COST = 6;
    public static final int STAGGER_TICKS = 40;
    public static final double PARRY_KNOCKBACK = 0.4D;

    static final DustParticleOptions GROOVE_DUST = new DustParticleOptions(0x38BDBA, 0.4F);
    static final DustParticleOptions TIP_DUST = new DustParticleOptions(0xB6FFF0, 0.3F);
    static final DustParticleOptions RING_DUST = new DustParticleOptions(0xB6FFF0, 0.6F);
    static final DustParticleOptions AUTHORITY_DUST = new DustParticleOptions(0xD8C8FF, 0.5F);
    static final DustParticleOptions CUT_DUST = new DustParticleOptions(0x38BDBA, 0.5F);
    static final DustParticleOptions BASALT_DUST = new DustParticleOptions(0x55515B, 0.5F);
    static final DustParticleOptions CHARGE_DUST = new DustParticleOptions(0x38BDBA, 0.35F);
    private static final BlockParticleOption CHIPS = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState());

    private static final Map<UUID, Entry> ENTRIES = new HashMap<>();

    private static final class Entry {
        final Player player;
        /** Game time when the guard was raised; -1 when no guard is up. */
        long guardStart = -1L;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        /** Game time when the charged Reprise fades; -1 when not charged. */
        long repriseUntil = -1L;

        Entry(Player player) {
            this.player = player;
        }
    }

    public ArchitectScimitarItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE; // the pose comes from ArchitectScimitarClient (arm pose and first-person transform)
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || player.isPassenger() || player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide() && !raiseGuard(player)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return stack; // the guard simply drops at the end of the use; the server tick settles success or failure
    }

    // ================================================================= server logic
    /** Raises the guard (server). Puts the scimitar on the failure cooldown at once; a successful parry shortens it. */
    public static boolean raiseGuard(Player player) {
        if (!(player.level() instanceof ServerLevel level) || isGuarding(player)) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ArchitectScimitarItem)) {
            return false;
        }
        Entry entry = ENTRIES.computeIfAbsent(player.getUUID(), id -> new Entry(player));
        entry.guardStart = level.getGameTime();
        entry.hand = InteractionHand.MAIN_HAND;
        player.getCooldowns().addCooldown(stack, FAIL_COOLDOWN);
        play(player, ModSounds.SCIMITAR_PARRY_RAISE.get());
        Vec3 hand = handPos(player);
        Vec3 tip = tipPos(player);
        for (int i = 0; i < 2; i++) {
            Vec3 p = hand.lerp(tip, (i + 1) / 3.0D);
            level.sendParticles(GROOVE_DUST, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        stack.hurtAndBreak(DURABILITY_COST, player, InteractionHand.MAIN_HAND);
        return true;
    }

    public static boolean isGuarding(Player player) {
        Entry e = ENTRIES.get(player.getUUID());
        return e != null && e.guardStart >= 0L;
    }

    /** Ticks since the guard was raised, or -1. */
    public static int guardTicks(Player player) {
        Entry e = ENTRIES.get(player.getUUID());
        return e == null || e.guardStart < 0L ? -1 : (int) (player.level().getGameTime() - e.guardStart);
    }

    public static boolean isInWindow(Player player) {
        int t = guardTicks(player);
        return t >= WINDOW_START && t < WINDOW_END;
    }

    public static boolean isRepriseCharged(Player player) {
        Entry e = ENTRIES.get(player.getUUID());
        return e != null && e.repriseUntil >= 0L;
    }

    /** True when the stack carries the charged-Reprise flag (read by the item model and the client pose). */
    public static boolean isChargedStack(ItemStack stack) {
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return data != null && Boolean.TRUE.equals(data.getBoolean(0));
    }

    private static void setCharged(ItemStack stack, boolean charged) {
        if (!(stack.getItem() instanceof ArchitectScimitarItem)) {
            return;
        }
        if (charged) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(true), List.of(), List.of()));
        } else {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    /** Whether a hit from {@code from} (a position) lies in the front cone of the player's facing. */
    static boolean inFrontCone(Player player, Vec3 incoming) {
        double dx = incoming.x;
        double dz = incoming.z;
        if (dx * dx + dz * dz < 1.0E-6D) {
            return true; // straight above or below: the raised blade covers it
        }
        float towards = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        return Math.abs(Mth.wrapDegrees(towards - player.getYHeadRot())) <= PARRY_HALF_ARC;
    }

    /** One tick of one player's scimitar state. */
    private static boolean tickEntry(Entry e) {
        Player player = e.player;
        if (player.isRemoved() || !player.isAlive() || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        long now = level.getGameTime();
        if (e.guardStart >= 0L) {
            int t = (int) (now - e.guardStart);
            if (!(player.getMainHandItem().getItem() instanceof ArchitectScimitarItem)) {
                e.guardStart = -1L; // the blade left the hand: the guard is gone, the cooldown stays
            } else if (t >= WINDOW_START && t < WINDOW_END && t % 2 == 0) {
                Vec3 tip = tipPos(player);
                level.sendParticles(TIP_DUST, tip.x, tip.y, tip.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            } else if (t >= WINDOW_END) {
                missParry(level, player);
                e.guardStart = -1L;
            }
        }
        if (e.repriseUntil >= 0L) {
            if (now >= e.repriseUntil) {
                fadeReprise(level, player, e);
            } else if ((e.repriseUntil - now) % 4 == 0) {
                Vec3 hand = handPos(player);
                level.sendParticles(CHARGE_DUST, hand.x, hand.y, hand.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
            }
        }
        return e.guardStart >= 0L || e.repriseUntil >= 0L;
    }

    private static void missParry(ServerLevel level, Player player) {
        play(player, ModSounds.SCIMITAR_PARRY_FAIL.get());
        Vec3 tip = tipPos(player);
        level.sendParticles(CHIPS, tip.x, tip.y, tip.z, 2, 0.1D, 0.05D, 0.1D, 0.0D);
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ArchitectScimitarItem) {
            player.stopUsingItem();
        }
    }

    private static void fadeReprise(ServerLevel level, Player player, Entry e) {
        e.repriseUntil = -1L;
        clearChargedStacks(player);
        play(player, ModSounds.SCIMITAR_REPRISE_FADE.get());
        Vec3 hand = handPos(player);
        level.sendParticles(BASALT_DUST, hand.x, hand.y, hand.z, 3, 0.08D, 0.02D, 0.08D, 0.0D);
    }

    private static void clearChargedStacks(Player player) {
        for (ItemStack s : player.getInventory()) {
            if (isChargedStack(s)) {
                setCharged(s, false);
            }
        }
    }

    /** A parry landed: the hit is cancelled by the caller, the attacker is shaken and the Reprise is charged. */
    private static void landParry(ServerLevel level, Player player, Vec3 contact, @Nullable LivingEntity attacker) {
        Entry e = ENTRIES.get(player.getUUID());
        if (e == null) {
            return;
        }
        e.guardStart = -1L;
        e.repriseUntil = level.getGameTime() + REPRISE_TICKS;
        ItemStack stack = player.getMainHandItem();
        player.getCooldowns().addCooldown(stack, SUCCESS_COOLDOWN);
        setCharged(stack, true);
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ArchitectScimitarItem) {
            player.stopUsingItem();
        }
        play(player, ModSounds.SCIMITAR_PARRY_SUCCESS.get());
        play(player, ModSounds.SCIMITAR_PARRY_RESONANCE.get());
        play(player, ModSounds.SCIMITAR_REPRISE_CHARGED.get());
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, contact.x, contact.y, contact.z, 3, 0.05D, 0.05D, 0.05D, 0.2D);
        level.sendParticles(CHIPS, contact.x, contact.y, contact.z, 4, 0.1D, 0.1D, 0.1D, 0.1D);
        pressureRing(level, player, contact);
        if (attacker != null) {
            level.sendParticles(AUTHORITY_DUST, attacker.getX(), attacker.getY() + attacker.getBbHeight() + 0.2D, attacker.getZ(),
                    6, 0.25D, 0.1D, 0.25D, 0.0D);
            attacker.knockback(PARRY_KNOCKBACK, player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, STAGGER_TICKS, 1), player);
            attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STAGGER_TICKS, 0), player);
            if (attacker instanceof Architect architect) {
                architect.receiveParry();
            }
        }
    }

    /** Eight points around the contact, in the plane facing the player's look (spec §26.4, "anel de pressão"). */
    private static void pressureRing(ServerLevel level, Player player, Vec3 c) {
        Vec3 look = player.getLookAngle();
        Vec3 side = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        side = side.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
        Vec3 up = side.cross(look).normalize();
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2.0D * i / 8.0D;
            Vec3 p = c.add(side.scale(Math.cos(a) * 0.5D)).add(up.scale(Math.sin(a) * 0.5D));
            level.sendParticles(RING_DUST, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** The charged Reprise lands: an arc of mana and basalt across the swing, a heavy strike. */
    private static void landReprise(ServerLevel level, Player player, LivingEntity victim) {
        Entry e = ENTRIES.get(player.getUUID());
        if (e != null) {
            e.repriseUntil = -1L;
        }
        clearChargedStacks(player);
        play(player, ModSounds.SCIMITAR_REPRISE_HIT.get());
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        double y = player.getY() + player.getBbHeight() * 0.72D;
        for (int i = 0; i < 10; i++) {
            // 120° arc of radius 1.6 in front of the chest, swept from right to left
            double a = yaw + Math.toRadians(-60.0D + 120.0D * i / 9.0D);
            double px = player.getX() - Math.sin(a) * 1.6D;
            double pz = player.getZ() + Math.cos(a) * 1.6D;
            level.sendParticles(i % 3 == 2 ? BASALT_DUST : CUT_DUST, px, y, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        double vy = victim.getY() + victim.getBbHeight() * 0.6D;
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, victim.getX(), vy, victim.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.2D);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), vy, victim.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void play(Player player, SoundEvent sound) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** Roughly where the main hand is (spec §26.4). */
    static Vec3 handPos(Player player) {
        Vec3 look = player.getLookAngle();
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
        return player.getEyePosition().add(0.0D, -0.4D, 0.0D).add(look.scale(0.5D)).add(right.scale(0.35D));
    }

    /** The raised blade crosses in front of the face; its tip sits toward the off-hand side. */
    static Vec3 tipPos(Player player) {
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
        return player.getEyePosition().add(player.getLookAngle().scale(0.6D)).add(right.scale(-0.45D));
    }

    // ================================================================= events
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = event.getSource();
        // 1) the victim parries a melee blow
        if (event.getEntity() instanceof Player player && isInWindow(player) && parryableMelee(source)) {
            LivingEntity attacker = (LivingEntity) source.getDirectEntity();
            if (attacker != null && inFrontCone(player, attacker.position().subtract(player.position()))) {
                event.setCanceled(true);
                Vec3 contact = attacker.getBoundingBox().getCenter().lerp(player.getEyePosition(), 0.5D);
                landParry(level, player, contact, attacker);
                return;
            }
        }
        // 2) the attacker spends a charged Reprise
        if (source.getEntity() instanceof Player player && source.getDirectEntity() == player && isRepriseCharged(player)
                && player.getMainHandItem().getItem() instanceof ArchitectScimitarItem && event.getEntity() != player) {
            event.setAmount(event.getAmount() * REPRISE_MULTIPLIER);
            landReprise(level, player, event.getEntity());
        }
    }

    /** A blow struck in person by a living attacker; never explosions, fire, projectiles or what a shield cannot stop. */
    static boolean parryableMelee(DamageSource source) {
        return source.getDirectEntity() instanceof LivingEntity direct && direct == source.getEntity()
                && !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.BYPASSES_SHIELD) && !source.is(DamageTypeTags.IS_PROJECTILE);
    }

    @SubscribeEvent
    static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof Player player
                && parryProjectile(player, event.getProjectile())) {
            event.setCanceled(true);
        }
    }

    /**
     * A projectile reaching a guarding player (server): inside the window and from the front cone, a light one flies
     * back to where it came from and a strong one is stopped on the blade at a greater cost. True when parried.
     */
    public static boolean parryProjectile(Player player, Projectile projectile) {
        if (!(player.level() instanceof ServerLevel level) || !isInWindow(player)) {
            return false;
        }
        Vec3 v = projectile.getDeltaMovement();
        if (!inFrontCone(player, v.scale(-1.0D))) {
            return false;
        }
        Vec3 contact = projectile.position();
        if (Architect.isLightProjectile(projectile)) {
            projectile.deflect(ProjectileDeflection.REVERSE, player, EntityReference.of((Entity) player), false);
            play(player, ModSounds.SCIMITAR_PARRY_DEFLECT.get());
            for (int i = 1; i <= 4; i++) {
                Vec3 p = contact.add(v.scale(0.15D * i)); // trailing behind it, on its way back
                level.sendParticles(CUT_DUST, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        } else {
            projectile.discard(); // too heavy to send back: stopped on the blade at a greater cost
            player.getMainHandItem().hurtAndBreak(STRONG_PROJECTILE_COST - DURABILITY_COST, player, InteractionHand.MAIN_HAND);
        }
        landParry(level, player, contact, null);
        return true;
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Entry> it = ENTRIES.values().iterator();
        while (it.hasNext()) {
            if (!tickEntry(it.next())) {
                it.remove();
            }
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ENTRIES.remove(event.getEntity().getUUID());
        clearChargedStacks(event.getEntity());
    }
}
