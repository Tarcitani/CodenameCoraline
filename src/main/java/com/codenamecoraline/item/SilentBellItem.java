package com.codenamecoraline.item;

import com.codenamecoraline.entity.collector.LastGiftCollector;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ritual bell used to summon the Collector of the Last Gift (`codenamecoraline:last_gift_collector`).
 *
 * <p>Validates all five ritual conditions before consuming the item:
 * <ol>
 *   <li>Used directly on {@link Blocks#PACKED_ICE}.</li>
 *   <li>Located in {@link Biomes#SNOWY_TAIGA} or {@link Biomes#GROVE}.</li>
 *   <li>Nighttime ({@code 12500..23500} ticks in the day cycle).</li>
 *   <li>Unobstructed sky access and a clear landing volume above the altar block.</li>
 *   <li>No other active {@link LastGiftCollector} within 64 blocks.</li>
 * </ol>
 */
public final class SilentBellItem extends Item {
    public static final double ACTIVE_COLLECTOR_RADIUS = 64.0D;
    public static final int MIN_VERTICAL_CLEARANCE = 16;
    public static final long NIGHT_START_TICK = 12500L;
    public static final long NIGHT_END_TICK = 23500L;

    public enum SummonResult {
        SUCCESS("entity.codenamecoraline.last_gift_collector.summon.success"),
        WRONG_BLOCK("entity.codenamecoraline.last_gift_collector.summon.wrong_block"),
        WRONG_BIOME("entity.codenamecoraline.last_gift_collector.summon.wrong_biome"),
        NOT_NIGHT("entity.codenamecoraline.last_gift_collector.summon.not_night"),
        OBSTRUCTED_SKY("entity.codenamecoraline.last_gift_collector.summon.obstructed"),
        ALREADY_ACTIVE("entity.codenamecoraline.last_gift_collector.summon.already_active");

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

    public SilentBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide()) {
            return level.getBlockState(clickedPos).is(Blocks.PACKED_ICE)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        SummonResult result = trySummon(serverLevel, clickedPos, player, stack);
        return result.isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static SummonResult evaluateConditions(ServerLevel level, BlockPos clickedPos) {
        if (!level.getBlockState(clickedPos).is(Blocks.PACKED_ICE)) {
            return SummonResult.WRONG_BLOCK;
        }

        Holder<Biome> biome = level.getBiome(clickedPos);
        if (!biome.is(Biomes.SNOWY_TAIGA) && !biome.is(Biomes.GROVE)) {
            return SummonResult.WRONG_BIOME;
        }

        long timeOfDay = Math.floorMod(level.getDefaultClockTime(), 24000L);
        if (timeOfDay < NIGHT_START_TICK || timeOfDay > NIGHT_END_TICK) {
            return SummonResult.NOT_NIGHT;
        }

        if (!hasSkyAndLandingClearance(level, clickedPos)) {
            return SummonResult.OBSTRUCTED_SKY;
        }

        AABB searchBox = new AABB(clickedPos).inflate(ACTIVE_COLLECTOR_RADIUS);
        boolean activeNearby = !level.getEntitiesOfClass(
                LastGiftCollector.class,
                searchBox,
                collector -> collector.isAlive() && collector.getBossState() != LastGiftCollector.BossState.DEAD
        ).isEmpty();
        if (activeNearby) {
            return SummonResult.ALREADY_ACTIVE;
        }

        return SummonResult.SUCCESS;
    }

    public static boolean hasSkyAndLandingClearance(ServerLevel level, BlockPos clickedPos) {
        int motionBlockingY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, clickedPos.getX(), clickedPos.getZ());
        if (motionBlockingY > clickedPos.getY() + 1) {
            return false;
        }
        for (int dy = 1; dy <= MIN_VERTICAL_CLEARANCE; dy++) {
            BlockPos check = clickedPos.above(dy);
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                return false;
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    BlockPos landingPos = clickedPos.offset(dx, dy, dz);
                    if (!level.getBlockState(landingPos).getCollisionShape(level, landingPos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static SummonResult trySummon(
            ServerLevel level,
            BlockPos clickedPos,
            @Nullable Player player,
            ItemStack stack
    ) {
        SummonResult check = evaluateConditions(level, clickedPos);
        if (!check.isSuccess()) {
            level.playSound(null, clickedPos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(),
                    SoundSource.PLAYERS, 0.8F, 0.5F);
            if (player != null) {
                player.sendOverlayMessage(
                        Component.translatable(check.translationKey()).withStyle(ChatFormatting.GRAY)
                );
            }
            return check;
        }

        LastGiftCollector collector = ModEntities.LAST_GIFT_COLLECTOR.get().create(level, EntitySpawnReason.TRIGGERED);
        if (collector == null) {
            return SummonResult.OBSTRUCTED_SKY;
        }

        collector.startIntroCinematic(level, clickedPos.above(), player);
        level.addFreshEntity(collector);

        level.playSound(null, clickedPos, ModSounds.COLLECTOR_BELL_PREP.get(),
                SoundSource.HOSTILE, 2.0F, 0.65F);

        if (player != null) {
            stack.consume(1, player);
            player.sendOverlayMessage(
                    Component.translatable(SummonResult.SUCCESS.translationKey()).withStyle(ChatFormatting.AQUA)
            );
        } else {
            stack.shrink(1);
        }

        return SummonResult.SUCCESS;
    }
}
