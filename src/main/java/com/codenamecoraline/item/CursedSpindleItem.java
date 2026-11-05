package com.codenamecoraline.item;

import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jspecify.annotations.Nullable;

/**
 * Cursed Spindle: calls the Witch of Ten Threads. Used on a solid block in a dark forest, a pale garden or an old-growth
 * taiga (or, at night, in any dense forest), with a free space of 5 x 5 x 5 blocks above it and no other Matriarch within
 * 64 blocks. A failed use never consumes the spindle, plays a subtle failure sound and says why on the action bar.
 */
public final class CursedSpindleItem extends Item {
    public static final int CLEAR_SIZE = 5;
    public static final long NIGHT_START_TICK = 12500L;
    public static final long NIGHT_END_TICK = 23500L;
    public static final int FOREST_RADIUS = 8;
    public static final int MIN_LOGS_NEARBY = 14;

    public enum SummonResult {
        SUCCESS("entity.codenamecoraline.ten_threads_witch.summon.success"),
        NOT_SOLID("entity.codenamecoraline.ten_threads_witch.summon.not_solid"),
        WRONG_BIOME("entity.codenamecoraline.ten_threads_witch.summon.wrong_biome"),
        NO_SPACE("entity.codenamecoraline.ten_threads_witch.summon.no_space"),
        ALREADY_ACTIVE("entity.codenamecoraline.ten_threads_witch.summon.already_active");

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

    public CursedSpindleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide()) {
            return level.getBlockState(pos).isSolidRender() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        SummonResult result = trySummon(serverLevel, pos, context.getPlayer(), context.getItemInHand());
        return result.isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private static boolean allowedBiome(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.PALE_GARDEN) || biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
                || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA)) {
            return true;
        }
        long time = Math.floorMod(level.getDefaultClockTime(), 24000L); // any dense forest will do, at night
        if (time < NIGHT_START_TICK || time > NIGHT_END_TICK) {
            return false;
        }
        int logs = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-FOREST_RADIUS, 0, -FOREST_RADIUS), pos.offset(FOREST_RADIUS, 12, FOREST_RADIUS))) {
            if (level.getBlockState(p).is(BlockTags.LOGS) && ++logs >= MIN_LOGS_NEARBY) {
                return true;
            }
        }
        return false;
    }

    public static SummonResult evaluateConditions(ServerLevel level, BlockPos clicked) {
        if (!level.getBlockState(clicked).isFaceSturdy(level, clicked, net.minecraft.core.Direction.UP)) {
            return SummonResult.NOT_SOLID;
        }
        if (!allowedBiome(level, clicked)) {
            return SummonResult.WRONG_BIOME;
        }
        int half = CLEAR_SIZE / 2;
        for (BlockPos p : BlockPos.betweenClosed(clicked.offset(-half, 1, -half), clicked.offset(half, CLEAR_SIZE, half))) {
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
                return SummonResult.NO_SPACE; // trees all around are welcome; a block in the way of the 5x5x5 is not
            }
        }
        return MatriarchWitchEntity.otherWitchNear(level, clicked) ? SummonResult.ALREADY_ACTIVE : SummonResult.SUCCESS;
    }

    public static SummonResult trySummon(ServerLevel level, BlockPos clicked, @Nullable Player player, ItemStack stack) {
        SummonResult check = evaluateConditions(level, clicked);
        if (!check.isSuccess()) {
            level.playSound(null, clicked, SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.8F, 0.6F);
            if (player != null) {
                player.sendOverlayMessage(Component.translatable(check.translationKey()).withStyle(ChatFormatting.GRAY));
            }
            return check;
        }
        MatriarchWitchEntity witch = ModEntities.TEN_THREADS_WITCH.get().create(level, EntitySpawnReason.TRIGGERED);
        if (witch == null) {
            return SummonResult.NO_SPACE;
        }
        witch.setPos(clicked.getX() + 0.5D, clicked.getY() + 1.0D, clicked.getZ() + 0.5D);
        if (player != null) {
            float yaw = (float) Math.toDegrees(Math.atan2(-(player.getX() - witch.getX()), player.getZ() - witch.getZ()));
            witch.setYRot(yaw);
            witch.setYHeadRot(yaw);
            witch.yBodyRot = yaw;
        }
        if (!level.addFreshEntity(witch)) {
            return SummonResult.NO_SPACE; // the world refused her: the spindle is kept
        }
        witch.startEncounter(level);
        if (player != null) {
            stack.consume(1, player);
            player.sendOverlayMessage(Component.translatable(SummonResult.SUCCESS.translationKey()).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            stack.shrink(1);
        }
        return SummonResult.SUCCESS;
    }
}
