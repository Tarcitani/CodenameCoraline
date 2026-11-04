package com.codenamecoraline.item;

import com.codenamecoraline.entity.crimson.CrimsonCommander;
import com.codenamecoraline.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Stained Oath Crest: the insignia of the Crimson Commander's oath. Used on deepslate bricks or polished deepslate with a
 * free space of 5 x 5 x 5 blocks above, and no other Commander within 48 blocks, it calls him to an audience. A failed use
 * never consumes the crest and says exactly why on the action bar.
 */
public final class StainedOathCrestItem extends Item {
    /** Side of the free cube needed above the clicked block. */
    public static final int CLEAR_SIZE = 5;

    public enum SummonResult {
        SUCCESS("entity.codenamecoraline.crimson_commander.summon.success"),
        WRONG_BLOCK("entity.codenamecoraline.crimson_commander.summon.wrong_block"),
        NO_SPACE("entity.codenamecoraline.crimson_commander.summon.no_space"),
        ALREADY_ACTIVE("entity.codenamecoraline.crimson_commander.summon.already_active");

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

    public StainedOathCrestItem(Properties properties) {
        super(properties);
    }

    public static boolean isAltar(BlockState state) {
        return state.is(Blocks.DEEPSLATE_BRICKS) || state.is(Blocks.POLISHED_DEEPSLATE);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide()) {
            return isAltar(level.getBlockState(pos)) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        SummonResult result = trySummon(serverLevel, pos, context.getPlayer(), context.getItemInHand());
        return result.isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static SummonResult evaluateConditions(ServerLevel level, BlockPos clicked) {
        if (!isAltar(level.getBlockState(clicked))) {
            return SummonResult.WRONG_BLOCK;
        }
        int half = CLEAR_SIZE / 2;
        for (BlockPos p : BlockPos.betweenClosed(clicked.offset(-half, 1, -half), clicked.offset(half, CLEAR_SIZE, half))) {
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
                return SummonResult.NO_SPACE;
            }
        }
        boolean active = !level.getEntitiesOfClass(CrimsonCommander.class, new AABB(clicked).inflate(CrimsonCommander.OTHER_COMMANDER_RADIUS),
                c -> c.isAlive() && c.getActivity() != CrimsonCommander.Activity.DEAD).isEmpty();
        return active ? SummonResult.ALREADY_ACTIVE : SummonResult.SUCCESS;
    }

    public static SummonResult trySummon(ServerLevel level, BlockPos clicked, @Nullable Player player, ItemStack stack) {
        SummonResult check = evaluateConditions(level, clicked);
        if (!check.isSuccess()) {
            level.playSound(null, clicked, SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.9F, 0.6F);
            if (player != null) {
                player.sendOverlayMessage(Component.translatable(check.translationKey()).withStyle(ChatFormatting.GRAY));
            }
            return check;
        }
        CrimsonCommander boss = ModEntities.CRIMSON_COMMANDER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (boss == null) {
            return SummonResult.NO_SPACE;
        }
        boss.setPos(clicked.getX() + 0.5D, clicked.getY() + 1.0D, clicked.getZ() + 0.5D);
        if (player != null) {
            float yaw = (float) Math.toDegrees(Math.atan2(-(player.getX() - boss.getX()), player.getZ() - boss.getZ()));
            boss.setYRot(yaw);
            boss.setYHeadRot(yaw);
            boss.yBodyRot = yaw;
        }
        boss.startIntro(level);
        if (!level.addFreshEntity(boss)) {
            return SummonResult.NO_SPACE; // the world refused him: the crest is kept
        }
        if (player != null) {
            stack.consume(1, player);
            player.sendOverlayMessage(Component.translatable(SummonResult.SUCCESS.translationKey()).withStyle(ChatFormatting.RED));
        } else {
            stack.shrink(1);
        }
        return SummonResult.SUCCESS;
    }
}
