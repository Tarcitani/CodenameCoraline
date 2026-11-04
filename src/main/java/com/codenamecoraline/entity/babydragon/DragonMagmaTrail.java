package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.registry.ModDragonSystems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Per-dimension journal. It never force-loads chunks or replaces non-air blocks. */
public final class DragonMagmaTrail extends SavedData {
    public record Patch(BlockPos pos, long expires, String dragon, BlockState original) {
        public static final Codec<Patch> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Patch::pos), Codec.LONG.fieldOf("expires").forGetter(Patch::expires),
                Codec.STRING.fieldOf("dragon").forGetter(Patch::dragon),
                BlockState.CODEC.optionalFieldOf("original", Blocks.AIR.defaultBlockState()).forGetter(Patch::original)).apply(i, Patch::new));
    }
    public static final Codec<DragonMagmaTrail> CODEC = Patch.CODEC.listOf().xmap(DragonMagmaTrail::new, data -> data.patches);
    public static final SavedDataType<DragonMagmaTrail> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_magma"), DragonMagmaTrail::new, CODEC);
    private final ArrayList<Patch> patches;
    public DragonMagmaTrail() { this.patches = new ArrayList<>(); }
    private DragonMagmaTrail(java.util.List<Patch> patches) { this.patches = new ArrayList<>(patches); }
    public static DragonMagmaTrail get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    public int size() { return this.patches.size(); }
    public boolean place(ServerLevel level, BlockPos pos, BabyDragon dragon) {
        if (this.patches.size() >= 4096 || !level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || !level.getBlockState(pos).isAir() || level.getBlockEntity(pos.below()) != null) return false;
        var ground = level.getBlockState(pos.below());
        if (!(ground.is(Blocks.STONE) || ground.is(Blocks.DEEPSLATE) || ground.is(Blocks.GRASS_BLOCK)
                || ground.is(Blocks.DIRT) || ground.is(Blocks.SAND) || ground.is(Blocks.NETHERRACK))) return false;
        if (!level.noCollision(new AABB(pos)) || !level.getEntitiesOfClass(LivingEntity.class, new AABB(pos)).isEmpty()) return false;
        // Journal first; the original state is always air. A replacement is removed only while it is still ours.
        this.patches.add(new Patch(pos.immutable(), level.getGameTime() + 160, dragon.getUUID().toString(), level.getBlockState(pos))); this.setDirty();
        level.setBlockAndUpdate(pos, ModDragonSystems.TEMPORARY_MAGMA.get().defaultBlockState()); return true;
    }
    public void sample(ServerLevel level, Vec3 point, BabyDragon dragon) {
        BlockPos start = BlockPos.containing(point);
        for (int dy = 0; dy <= 12; dy++) {
            BlockPos pos = start.below(dy);
            if (!level.hasChunkAt(pos)) return;
            if (!level.getBlockState(pos).isAir()) { this.place(level, pos.above(), dragon); return; }
        }
    }
    public void tick(ServerLevel level) {
        Iterator<Patch> iterator = this.patches.iterator();
        while (iterator.hasNext()) {
            Patch patch = iterator.next();
            if (!level.hasChunkAt(patch.pos)) continue;
            if (!level.getBlockState(patch.pos).is(ModDragonSystems.TEMPORARY_MAGMA.get())) { iterator.remove(); this.setDirty(); continue; }
            if (level.getGameTime() >= patch.expires) {
                level.setBlockAndUpdate(patch.pos, patch.original); iterator.remove(); this.setDirty(); continue;
            }
            if (level.getGameTime() % 10 != 0) continue;
            UUID uuid;
            try { uuid = UUID.fromString(patch.dragon); } catch (IllegalArgumentException invalid) { continue; }
            BabyDragon dragon = level.getEntity(uuid) instanceof BabyDragon d ? d : null;
            if (dragon == null || !dragon.isAlive()) continue;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(patch.pos).inflate(.1, .25, .1)))
                if (dragon.validDefensiveTarget(target)) dragon.damageEnemy(target, 2);
        }
    }
}
