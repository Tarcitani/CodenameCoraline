package com.codenamecoraline.item;

import com.codenamecoraline.entity.ten_threads_witch.AttackRunner;
import com.codenamecoraline.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Staff of the Ten Threads: 7 melee damage and three stances of thread, cycled with Shift + right-click:
 * <ol>
 *   <li><b>Hunting Threads</b> (mobility): a thread up to 22 blocks long that hauls the player to where it lands, with
 *       slow falling for 3 s. Cooldown 60 ticks, 1 durability.</li>
 *   <li><b>Control Threads</b> (area): a toll that pushes hostile creatures within 8 blocks away, roots them for 4 s and
 *       deals 8. Cooldown 140 ticks, 2 durability.</li>
 *   <li><b>Ruin Threads</b> (devastation): a projection of the Executioner's arm and the Giant's arms crushes a 4 x 4
 *       area in front for 22. Cooldown 200 ticks, 4 durability.</li>
 * </ol>
 * Repaired with Puppet Remnants. The stance lives in the stack's custom data.
 */
public final class TenThreadsStaffItem extends Item {
    public static final int POSTURES = 3;
    public static final double HOOK_RANGE = 22.0D;
    public static final int HOOK_COOLDOWN = 60;
    public static final int HOOK_SLOW_FALLING = 60;
    public static final int HOOK_COST = 1;
    public static final double CONTROL_RADIUS = 8.0D;
    public static final float CONTROL_DAMAGE = 8.0F;
    public static final int CONTROL_ROOT_TICKS = 80;
    public static final int CONTROL_COOLDOWN = 140;
    public static final int CONTROL_COST = 2;
    public static final double RUIN_SIZE = 4.0D;
    public static final float RUIN_DAMAGE = 22.0F;
    public static final int RUIN_COOLDOWN = 200;
    public static final int RUIN_COST = 4;

    public TenThreadsStaffItem(Properties properties) {
        super(properties);
    }

    // ---------------------------------------------------------------- the stance
    /** The current stance (0, 1 or 2). */
    public static int posture(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : Math.floorMod(data.copyTag().getIntOr("posture", 0), POSTURES);
    }

    public static void setPosture(ItemStack stack, int posture) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("posture", Math.floorMod(posture, POSTURES)));
    }

    /** Moves to the next stance and returns it. */
    public static int cyclePosture(ItemStack stack) {
        int next = (posture(stack) + 1) % POSTURES;
        setPosture(stack, next);
        return next;
    }

    public static String postureKey(int posture) {
        return "item.codenamecoraline.ten_threads_staff.posture." + (posture + 1);
    }

    // ---------------------------------------------------------------- use
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            int next = cyclePosture(stack);
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable(postureKey(next)).withStyle(
                        next == 0 ? ChatFormatting.AQUA : next == 1 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED));
                level.playSound(null, player.blockPosition(), ModSounds.WITCH_THREAD_TENSION.get(), SoundSource.PLAYERS, 0.8F, 1.0F + next * 0.2F);
            }
            return InteractionResult.SUCCESS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        int cooldown;
        int cost;
        switch (posture(stack)) {
            case 0 -> {
                if (!huntingThread(serverLevel, player)) {
                    player.sendOverlayMessage(Component.translatable("item.codenamecoraline.ten_threads_staff.no_anchor").withStyle(ChatFormatting.GRAY));
                    return InteractionResult.FAIL;
                }
                cooldown = HOOK_COOLDOWN;
                cost = HOOK_COST;
            }
            case 1 -> {
                controlThreads(serverLevel, player);
                cooldown = CONTROL_COOLDOWN;
                cost = CONTROL_COST;
            }
            default -> {
                ruinThreads(serverLevel, player);
                cooldown = RUIN_COOLDOWN;
                cost = RUIN_COST;
            }
        }
        player.getCooldowns().addCooldown(stack, cooldown); // before the wear: a staff that breaks has no item left
        stack.hurtAndBreak(cost, player, hand);
        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }

    /** Stance I: the thread finds a block (or a creature) within 22 blocks and hauls the player to it. */
    public static boolean huntingThread(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(HOOK_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.MISS) {
            return false;
        }
        Vec3 point = hit.getLocation();
        Vec3 to = point.subtract(player.position().add(0.0D, 1.0D, 0.0D));
        double dist = to.length();
        double speed = Math.min(2.2D, 0.8D + dist * 0.06D);
        Vec3 v = to.normalize().scale(speed);
        player.setDeltaMovement(v.x, v.y + 0.25D, v.z);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, HOOK_SLOW_FALLING, 0));
        for (double k = 0.0D; k < 1.0D; k += 0.04D) {
            Vec3 p = eye.lerp(point, k);
            level.sendParticles(AttackRunner.THREAD_DUST, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, player.blockPosition(), ModSounds.WITCH_THREAD_TENSION.get(), SoundSource.PLAYERS, 1.2F, 1.5F);
        return true;
    }

    /** Stance II: a toll of threads and roots around the player. */
    public static void controlThreads(ServerLevel level, Player player) {
        AABB box = player.getBoundingBox().inflate(CONTROL_RADIUS, 3.0D, CONTROL_RADIUS);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive() && e instanceof Enemy)) {
            Vec3 d = e.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (d.length() > CONTROL_RADIUS) {
                continue;
            }
            e.hurtServer(level, level.damageSources().playerAttack(player), CONTROL_DAMAGE);
            Vec3 away = d.lengthSqr() > 1.0E-4D ? d.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
            e.setDeltaMovement(away.x * 0.9D, 0.3D, away.z * 0.9D);
            e.hurtMarked = true;
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, CONTROL_ROOT_TICKS, 6), player);
            level.sendParticles(AttackRunner.THREAD_DUST, e.getX(), e.getY() + 0.5D, e.getZ(), 16, 0.4D, 0.6D, 0.4D, 0.0D);
        }
        for (int i = 0; i < 32; i++) {
            double ang = Math.PI * 2.0D * i / 32.0D;
            level.sendParticles(AttackRunner.SPOOL_DUST, player.getX() + Math.cos(ang) * CONTROL_RADIUS, player.getY() + 0.2D,
                    player.getZ() + Math.sin(ang) * CONTROL_RADIUS, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, player.blockPosition(), ModSounds.puppetAction(com.codenamecoraline.entity.ten_threads_witch.PuppetKind.HOLLOW_BELL),
                SoundSource.PLAYERS, 1.6F, 0.8F);
    }

    /** Stance III: the projected arms crush a 4 x 4 area three blocks in front. */
    public static void ruinThreads(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 f = look.lengthSqr() > 1.0E-4D ? look.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 c = player.position().add(f.scale(3.0D));
        AABB area = new AABB(c.x - RUIN_SIZE / 2.0D, c.y - 1.0D, c.z - RUIN_SIZE / 2.0D, c.x + RUIN_SIZE / 2.0D, c.y + 3.0D, c.z + RUIN_SIZE / 2.0D);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && e instanceof Enemy);
        for (LivingEntity e : victims) {
            e.hurtServer(level, level.damageSources().playerAttack(player), RUIN_DAMAGE);
            e.setDeltaMovement(f.x * 0.4D, 0.5D, f.z * 0.4D);
            e.hurtMarked = true;
        }
        for (int i = 0; i < 30; i++) {
            level.sendParticles(AttackRunner.CRIMSON_DUST, c.x + (level.getRandom().nextDouble() - 0.5D) * RUIN_SIZE, c.y + 0.2D + level.getRandom().nextDouble() * 2.5D,
                    c.z + (level.getRandom().nextDouble() - 0.5D) * RUIN_SIZE, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, player.blockPosition(), ModSounds.puppetAction(com.codenamecoraline.entity.ten_threads_witch.PuppetKind.PATCHWORK_EXECUTIONER),
                SoundSource.PLAYERS, 1.8F, 0.7F);
    }
}
