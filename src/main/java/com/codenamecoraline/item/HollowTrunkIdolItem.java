package com.codenamecoraline.item;

import com.codenamecoraline.entity.canopy.CanopyBreaker;
import com.codenamecoraline.entity.canopy.TreeNav;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Hollow Trunk Idol: a carved stump that calls the Canopy Breaker. Used on a log in an old-growth taiga or a dark forest,
 * at night, with enough trees around, a clearing to arrive in, and no other Canopy Breaker nearby. A failed use never
 * consumes the idol and says why.
 */
public final class HollowTrunkIdolItem extends Item {
    public static final long NIGHT_START_TICK = 12500L;
    public static final long NIGHT_END_TICK = 23500L;
    public static final int TREE_RADIUS = 8;
    public static final int MIN_LOGS_NEARBY = 14;
    public static final double OTHER_BOSS_RADIUS = 96.0D;

    public enum SummonResult {
        SUCCESS("entity.codenamecoraline.canopy_breaker.summon.success"),
        WRONG_BLOCK("entity.codenamecoraline.canopy_breaker.summon.wrong_block"),
        WRONG_BIOME("entity.codenamecoraline.canopy_breaker.summon.wrong_biome"),
        NOT_NIGHT("entity.codenamecoraline.canopy_breaker.summon.not_night"),
        FEW_TREES("entity.codenamecoraline.canopy_breaker.summon.few_trees"),
        NO_CLEARING("entity.codenamecoraline.canopy_breaker.summon.no_clearing"),
        ALREADY_ACTIVE("entity.codenamecoraline.canopy_breaker.summon.already_active");

        private final String translationKey;

        SummonResult(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return this.translationKey;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }

    public HollowTrunkIdolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (level.isClientSide()) {
            return TreeNav.isLog(level.getBlockState(pos)) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        SummonResult result = trySummon(serverLevel, pos, player, context.getItemInHand());
        return result.isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static SummonResult evaluateConditions(ServerLevel level, BlockPos clicked) {
        if (!TreeNav.isLog(level.getBlockState(clicked))) {
            return SummonResult.WRONG_BLOCK;
        }
        Holder<Biome> biome = level.getBiome(clicked);
        if (!biome.is(Biomes.OLD_GROWTH_PINE_TAIGA) && !biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA) && !biome.is(Biomes.DARK_FOREST)) {
            return SummonResult.WRONG_BIOME;
        }
        long time = Math.floorMod(level.getDefaultClockTime(), 24000L);
        if (time < NIGHT_START_TICK || time > NIGHT_END_TICK) {
            return SummonResult.NOT_NIGHT;
        }
        int logs = 0;
        for (BlockPos p : BlockPos.betweenClosed(clicked.offset(-TREE_RADIUS, -2, -TREE_RADIUS), clicked.offset(TREE_RADIUS, 14, TREE_RADIUS))) {
            if (TreeNav.isLog(level.getBlockState(p)) && ++logs >= MIN_LOGS_NEARBY) {
                break;
            }
        }
        if (logs < MIN_LOGS_NEARBY) {
            return SummonResult.FEW_TREES;
        }
        boolean active = !level.getEntitiesOfClass(CanopyBreaker.class, new AABB(clicked).inflate(OTHER_BOSS_RADIUS),
                b -> b.isAlive() && b.getActivity() != CanopyBreaker.Activity.DEAD).isEmpty();
        if (active) {
            return SummonResult.ALREADY_ACTIVE;
        }
        return findClearing(level, clicked) == null ? SummonResult.NO_CLEARING : SummonResult.SUCCESS;
    }

    /** A free, supported spot near the clicked trunk where a 1.8 x 3.1 beast can arrive. */
    public static @Nullable Vec3 findClearing(ServerLevel level, BlockPos clicked) {
        return TreeNav.findLanding(level, Vec3.atBottomCenterOf(clicked), 7.0D, CanopyBreaker.WIDTH, CanopyBreaker.HEIGHT, false);
    }

    public static SummonResult trySummon(ServerLevel level, BlockPos clicked, @Nullable Player player, ItemStack stack) {
        SummonResult check = evaluateConditions(level, clicked);
        if (!check.isSuccess()) {
            level.playSound(null, clicked, SoundEvents.WOOD_HIT, SoundSource.PLAYERS, 0.9F, 0.5F);
            if (player != null) {
                player.sendOverlayMessage(Component.translatable(check.translationKey()).withStyle(ChatFormatting.GRAY));
            }
            return check;
        }
        Vec3 spot = findClearing(level, clicked);
        CanopyBreaker boss = ModEntities.CANOPY_BREAKER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (spot == null || boss == null) {
            return SummonResult.NO_CLEARING;
        }
        boss.setPos(spot.x, spot.y, spot.z);
        boss.startIntro(level);
        level.addFreshEntity(boss);
        level.playSound(null, clicked, ModSounds.CANOPY_ROAR_ALERT.get(), SoundSource.HOSTILE, 3.0F, 0.7F);
        if (player != null) {
            stack.consume(1, player);
            player.sendOverlayMessage(Component.translatable(SummonResult.SUCCESS.translationKey()).withStyle(ChatFormatting.GREEN));
        } else {
            stack.shrink(1);
        }
        return SummonResult.SUCCESS;
    }
}
