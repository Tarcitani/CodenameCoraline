package com.codenamecoraline.item;

import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Tablet of the Commandments: raises the Architect as a dormant statue on top of the clicked block. Needs a sturdy top
 * face, room for the statue, a non-peaceful world and no other Architect nearby. A failed use is never consumed and
 * says why. Until the temple arena exists this is the survival way to meet him (spec §21, Q-06).
 */
public final class CommandmentTabletItem extends Item {
    public static final double OTHER_BOSS_RADIUS = 96.0D;
    /** The Architect is temporarily disabled awaiting redesign. In GameTests it is enabled. */
    public static boolean ENABLED = Boolean.getBoolean("coraline.architectTests");

    public enum SummonResult {
        SUCCESS("entity.codenamecoraline.architect.summon.success"),
        WRONG_FACE("entity.codenamecoraline.architect.summon.wrong_face"),
        PEACEFUL("entity.codenamecoraline.architect.summon.peaceful"),
        NO_ROOM("entity.codenamecoraline.architect.summon.no_room"),
        ALREADY_ACTIVE("entity.codenamecoraline.architect.summon.already_active"),
        DISABLED("entity.codenamecoraline.architect.summon.disabled");

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

    public CommandmentTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return context.getClickedFace() == Direction.UP ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        float facing = player != null ? player.getYRot() + 180.0F : 0.0F;
        SummonResult result = trySummon(serverLevel, context.getClickedPos(), context.getClickedFace(), facing, player,
                context.getItemInHand());
        return result.isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static SummonResult evaluateConditions(ServerLevel level, BlockPos clicked, Direction face) {
        if (!ENABLED) {
            return SummonResult.DISABLED;
        }
        if (face != Direction.UP || !level.getBlockState(clicked).isFaceSturdy(level, clicked, Direction.UP)) {
            return SummonResult.WRONG_FACE;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return SummonResult.PEACEFUL;
        }
        BlockPos spot = clicked.above();
        if (!level.noCollision(ModEntities.ARCHITECT.get().getSpawnAABB(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D))) {
            return SummonResult.NO_ROOM;
        }
        boolean active = !level.getEntitiesOfClass(Architect.class, new AABB(clicked).inflate(OTHER_BOSS_RADIUS),
                Architect::isAlive).isEmpty();
        return active ? SummonResult.ALREADY_ACTIVE : SummonResult.SUCCESS;
    }

    public static SummonResult trySummon(ServerLevel level, BlockPos clicked, Direction face, float facing,
            @Nullable Player player, ItemStack stack) {
        SummonResult check = evaluateConditions(level, clicked, face);
        if (!check.isSuccess()) {
            level.playSound(null, clicked, SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.9F, 0.6F);
            if (player != null) {
                player.sendOverlayMessage(Component.translatable(check.translationKey()).withStyle(ChatFormatting.GRAY));
            }
            return check;
        }
        Architect statue = ModEntities.ARCHITECT.get().create(level, EntitySpawnReason.TRIGGERED);
        if (statue == null) {
            return SummonResult.NO_ROOM;
        }
        BlockPos spot = clicked.above();
        statue.setPos(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D);
        statue.setYRot(facing);
        statue.setYHeadRot(facing);
        statue.yBodyRot = facing;
        statue.setState(Architect.State.DORMANT);
        level.addFreshEntity(statue);
        level.playSound(null, spot, SoundEvents.STONE_PLACE, SoundSource.HOSTILE, 1.6F, 0.5F);
        if (player != null) {
            stack.consume(1, player);
            player.sendOverlayMessage(Component.translatable(SummonResult.SUCCESS.translationKey()).withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            stack.shrink(1);
        }
        return SummonResult.SUCCESS;
    }
}
