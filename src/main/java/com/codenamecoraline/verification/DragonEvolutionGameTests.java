package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.babydragon.*;
import com.codenamecoraline.inventory.DragonMenu;
import com.codenamecoraline.registry.*;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Real-server integration: all entities belong to their test, never cleanup by area/class. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class DragonEvolutionGameTests {
    private static final int Y = 10;
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
        Map.entry("dragon_evolution_stages_once", DragonEvolutionGameTests::stages),
        Map.entry("dragon_evolution_growth_collision", DragonEvolutionGameTests::dimensions),
        Map.entry("dragon_evolution_blocked_growth", DragonEvolutionGameTests::blockedGrowth),
        Map.entry("dragon_evolution_attributes", DragonEvolutionGameTests::attributes),
        Map.entry("dragon_evolution_xp_actual_damage", DragonEvolutionGameTests::xp),
        Map.entry("dragon_evolution_xp_not_invulnerable", DragonEvolutionGameTests::invulnerable),
        Map.entry("dragon_evolution_xp_damage_cap", DragonEvolutionGameTests::xpCap),
        Map.entry("dragon_evolution_xp_kill_once", DragonEvolutionGameTests::killOnce),
        Map.entry("dragon_evolution_save_load", DragonEvolutionGameTests::persistence),
        Map.entry("dragon_evolution_resume", DragonEvolutionGameTests::resume),
        Map.entry("dragon_evolution_juvenile_projectile", h -> projectile(h, DragonStage.YOUNG)),
        Map.entry("dragon_evolution_colossal_projectile", h -> projectile(h, DragonStage.ADULT)),
        Map.entry("dragon_evolution_projectile_wall_splash", DragonEvolutionGameTests::projectileWall),
        Map.entry("dragon_evolution_magma_safe_placement", DragonEvolutionGameTests::magmaPlace),
        Map.entry("dragon_evolution_magma_restore_save", DragonEvolutionGameTests::magmaRestore),
        Map.entry("dragon_evolution_magma_exact_air_restore", DragonEvolutionGameTests::magmaExactAir),
        Map.entry("dragon_evolution_riding_attacks", DragonEvolutionGameTests::riderAttacks),
        Map.entry("dragon_evolution_grown_rest_command", DragonEvolutionGameTests::grownRest),
        Map.entry("dragon_evolution_magma_player_edit", DragonEvolutionGameTests::magmaEdit),
        Map.entry("dragon_evolution_magma_protects_owner", DragonEvolutionGameTests::magmaOwner),
        Map.entry("dragon_evolution_breath_periodic", DragonEvolutionGameTests::breath),
        Map.entry("dragon_evolution_breath_wall", DragonEvolutionGameTests::breathWall),
        Map.entry("dragon_evolution_breath_allies", DragonEvolutionGameTests::breathAllies),
        Map.entry("dragon_evolution_breath_cancel_cooldown", DragonEvolutionGameTests::breathCancel),
        Map.entry("dragon_evolution_riding_gates", DragonEvolutionGameTests::mountGates),
        Map.entry("dragon_evolution_riding_flight", DragonEvolutionGameTests::ridingFlight),
        Map.entry("dragon_evolution_riding_forged_input", DragonEvolutionGameTests::forgedInput),
        Map.entry("dragon_evolution_inventory_slots_access", DragonEvolutionGameTests::inventoryAccess),
        Map.entry("dragon_evolution_inventory_shift_click", DragonEvolutionGameTests::inventoryTransfer),
        Map.entry("dragon_evolution_inventory_growth_save", DragonEvolutionGameTests::inventoryGrowth),
        Map.entry("dragon_evolution_inventory_death_once", DragonEvolutionGameTests::inventoryDeath),
        Map.entry("dragon_evolution_menu_sync_data", DragonEvolutionGameTests::menuData),
        Map.entry("dragon_evolution_admin_commands", DragonEvolutionGameTests::commands));
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name); }
    @SubscribeEvent public static void functions(RegisterEvent event) {
        if (Boolean.getBoolean("coraline.babyDragonTests")) event.register(Registries.TEST_FUNCTION, r -> TESTS.forEach((n, f) -> r.register(id(n), f)));
    }
    @SubscribeEvent public static void tests(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("coraline.babyDragonTests")) return;
        var environment = event.registerEnvironment(id("dragon_evolution_environment"));
        TESTS.forEach((n, f) -> event.registerTest(id(n), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(n)),
                new TestData<>(environment, id("basilisk_test_arena"), 300, 0, true))));
    }
    private static BabyDragon dragon(GameTestHelper h) {
        for (int x = 0; x <= 12; x++) for (int z = 0; z <= 12; z++) {
            h.setBlock(new BlockPos(x, Y, z), Blocks.STONE);
            for (int y = Y + 1; y < Y + 8; y++) h.setBlock(new BlockPos(x, y, z), Blocks.AIR);
        }
        BlockPos center = h.absolutePos(new BlockPos(6, Y, 6));
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) h.getLevel().setChunkForced((center.getX() >> 4) + x, (center.getZ() >> 4) + z, true);
        BabyDragon d = h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(), new BlockPos(3, Y + 1, 6));
        // removeFreeWill only clears vanilla goals; BabyDragon has a custom server brain.
        d.setNoAi(true); d.setNoGravity(true); return d;
    }
    private static ServerPlayer owner(GameTestHelper h, BabyDragon d) {
        ServerPlayer p = h.makeMockServerPlayerInLevel(); p.setPos(h.absoluteVec(new Vec3(1.5, Y + 1, 3.5))); p.setNoGravity(true); d.tame(p); return p;
    }
    private static LivingEntity enemy(GameTestHelper h, int x, int z) {
        var e = h.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(x, Y + 1, z)); e.setNoGravity(true);
        e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); e.getAttribute(Attributes.ARMOR).setBaseValue(0); e.setHealth(200); return e;
    }
    private static BabyDragon copy(GameTestHelper h, BabyDragon d) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess()); d.saveWithoutId(out);
        var copy = ModEntities.BABY_DRAGON.get().create(h.getLevel(), EntitySpawnReason.LOAD);
        copy.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), out.buildResult())); return copy;
    }
    private static void stage(GameTestHelper h, BabyDragon d, DragonStage stage) { h.assertTrue(d.setDragonStage(stage, false), "stage change has space"); }
    private static void stages(GameTestHelper h) {
        var d = dragon(h); owner(h, d); var uuid = d.getUUID(); d.addDragonXp(500);
        h.runAfterDelay(25, () -> { h.assertValueEqual(d.getDragonStage(), DragonStage.YOUNG, "500 XP unlocks young once"); h.assertTrue(d.getEvolutionTicks() > 0 && d.getGrowthScale() > 1 && d.getGrowthScale() < 1.8, "gradual growth"); });
        h.runAfterDelay(90, () -> { h.assertValueEqual(d.getEvolutionTicks(), 0, "young animation completed"); d.addDragonXp(1500); });
        h.runAfterDelay(180, () -> { h.assertValueEqual(d.getDragonStage(), DragonStage.ADULT, "1500 additional XP unlocks adult"); h.assertTrue(d.getUUID().equals(uuid) && d.getEvolutionTicks() == 0, "same entity and completed evolution"); d.addDragonXp(Integer.MAX_VALUE); });
        h.runAfterDelay(220, () -> { h.assertValueEqual(d.getDragonXp(), 2000, "XP safely saturates"); h.assertValueEqual(d.getEvolutionTicks(), 0, "adult cannot restart evolution"); h.succeed(); });
    }
    private static void dimensions(GameTestHelper h) {
        var d = dragon(h); float width = d.getBbWidth(), eyes = d.getEyeHeight(); stage(h, d, DragonStage.YOUNG);
        h.assertTrue(Math.abs(d.getBbWidth() - DragonRig.of(DragonStage.YOUNG).width()) < .01 && d.getBbWidth() > width, "young hitbox follows its distinct anatomy"); stage(h, d, DragonStage.ADULT);
        h.assertTrue(Math.abs(d.getBbWidth() - DragonRig.of(DragonStage.ADULT).width()) < .01 && Math.abs(d.getEyeHeight()-DragonRig.of(DragonStage.ADULT).eye()) < .01 && d.getEyeHeight() > eyes, "adult eye height and hitbox match the adult rig");
        h.assertTrue(d.getMouthPosition().y > d.getY() + 1, "mouth grows with body"); h.succeed();
    }
    private static void blockedGrowth(GameTestHelper h) {
        var d = dragon(h); h.setBlock(new BlockPos(3, Y + 2, 6), Blocks.STONE);
        h.assertTrue(!d.setDragonStage(DragonStage.ADULT, true), "ceiling blocks unsafe growth"); h.assertValueEqual(d.getDragonStage(), DragonStage.BABY, "no partial stage mutation"); h.succeed();
    }
    private static void attributes(GameTestHelper h) {
        var d = dragon(h);
        for (var s : DragonStage.values()) { stage(h, d, s); h.assertTrue(Math.abs(d.getMaxHealth() - s.health * d.getVitality()) < 0.01F, "health per stage");
            h.assertTrue(Math.abs(d.getAttributeValue(Attributes.ATTACK_DAMAGE) - (double) s.melee * d.getStrength()) < 0.01, "melee per stage");
            h.assertTrue(Math.abs(d.getAttributeValue(Attributes.FLYING_SPEED) - s.flightSpeed * d.getAgility()) < 0.001, "flight per stage");
            h.assertTrue(Math.abs(d.getAttributeValue(Attributes.ARMOR) - (double) s.armor * d.getVitality()) < 0.01, "armor per stage"); }
        h.succeed();
    }
    private static void xp(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); var target = enemy(h, 8, 6); int playerXp = p.totalExperience;
        d.damageEnemy(target, 6); h.assertValueEqual(d.getDragonXp(), 12, "XP from six actual health damage");
        h.assertValueEqual(p.totalExperience, playerXp, "player XP untouched"); h.succeed();
    }
    private static void invulnerable(GameTestHelper h) {
        var d = dragon(h); owner(h, d); var target = enemy(h, 8, 6); target.setInvulnerable(true);
        for (int i = 0; i < 100; i++) d.damageEnemy(target, 6);
        h.assertValueEqual(d.getDragonXp(), 0, "invulnerable hits give no XP"); h.succeed();
    }
    private static void xpCap(GameTestHelper h) {
        var d = dragon(h); owner(h, d); var target = enemy(h, 8, 6); target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20); target.setHealth(20);
        for (int i = 0; i < 20; i++) { target.invulnerableTime = 0; target.setHealth(20); d.damageEnemy(target, 5); }
        h.assertValueEqual(d.getDragonXp(), 40, "healing cannot exceed one health pool of participation XP");
        var loaded = copy(h, d); target.invulnerableTime = 0; target.setHealth(20); loaded.damageEnemy(target, 5);
        h.assertValueEqual(loaded.getDragonXp(), 40, "credit cap survives reload"); h.succeed();
    }
    private static void killOnce(GameTestHelper h) {
        var d = dragon(h); owner(h, d); var target = enemy(h, 8, 6); target.setHealth(4); d.damageEnemy(target, 6);
        h.assertValueEqual(d.getDragonXp(), 28, "actual damage plus kill bonus"); d.awardKill(target); d.awardKill(target);
        h.assertValueEqual(d.getDragonXp(), 28, "kill bonus once"); h.succeed();
    }
    private static void persistence(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.YOUNG); d.addDragonXp(240); d.setHealth(48);
        d.getDragonInventory().setItem(0, new ItemStack(Items.DIAMOND, 7)); d.getDragonBrain().setAttackCooldown(40);
        var loaded = copy(h, d);
        h.assertTrue(loaded.getUUID().equals(d.getUUID()) && loaded.isOwnedBy(p), "UUID and tame owner survive");
        h.assertValueEqual(loaded.getDragonStage(), DragonStage.YOUNG, "saved stage"); h.assertValueEqual(loaded.getDragonXp(), 740, "saved XP");
        h.assertValueEqual(loaded.getHealth(), 48F, "saved health"); h.assertValueEqual(loaded.getDragonInventory().getItem(0).getCount(), 7, "saved inventory");
        h.assertValueEqual(loaded.getDragonBrain().getAttackCooldown(), 40, "saved cooldown"); h.succeed();
    }
    private static void resume(GameTestHelper h) {
        var d = dragon(h); owner(h, d); h.assertTrue(d.setDragonStage(DragonStage.YOUNG, true), "starts evolution");
        var loaded = copy(h, d); d.discard(); h.getLevel().addFreshEntity(loaded);
        h.assertValueEqual(loaded.getEvolutionTicks(), 60, "evolution state saved");
        h.runAfterDelay(70, () -> { h.assertValueEqual(loaded.getEvolutionTicks(), 0, "resumed evolution completes"); h.assertValueEqual(loaded.getDragonStage(), DragonStage.YOUNG, "no rollback"); h.succeed(); });
    }
    private static void projectile(GameTestHelper h, DragonStage s) {
        var d = dragon(h); owner(h, d); stage(h, d, s); var target = enemy(h, 9, 6);
        var shot = new DragonEmber(h.getLevel(), d); shot.setPos(target.position().add(-3, .7, 0)); shot.shoot(1, 0, 0, 1, 0); h.getLevel().addFreshEntity(shot);
        h.assertTrue(shot.getBbWidth() > ModEntities.DRAGON_EMBER.get().getDimensions().width(), "larger physical projectile");
        h.runAfterDelay(8, () -> { h.assertTrue(Math.abs(target.getHealth() - (200 - d.getFireballDamage())) < 0.01F, "stage projectile damage"); h.succeed(); });
    }
    private static void projectileWall(GameTestHelper h) {
        var d = dragon(h); stage(h, d, DragonStage.ADULT); var target = enemy(h, 8, 6);
        for (int z = 3; z <= 9; z++) for (int y = Y + 1; y <= Y + 4; y++) h.setBlock(new BlockPos(7, y, z), Blocks.OAK_PLANKS);
        var shot = new DragonEmber(h.getLevel(), d); shot.setPos(h.absoluteVec(new Vec3(5, Y + 1.8, 6.5))); shot.shoot(1, 0, 0, 1, 0); h.getLevel().addFreshEntity(shot);
        h.runAfterDelay(8, () -> { h.assertValueEqual(target.getHealth(), 200F, "splash cannot cross wall"); h.assertTrue(h.getBlockState(new BlockPos(7, Y + 2, 6)).is(Blocks.OAK_PLANKS), "no construction destruction"); h.succeed(); });
    }
    private static BlockPos patch(GameTestHelper h) { return h.absolutePos(new BlockPos(6, Y + 1, 3)); }
    private static void magmaPlace(GameTestHelper h) {
        var d = dragon(h); var journal = new DragonMagmaTrail(); BlockPos pos = patch(h);
        h.assertTrue(journal.place(h.getLevel(), pos, d), "air above natural stone accepts temporary magma");
        h.assertTrue(!journal.place(h.getLevel(), pos, d), "cannot replace existing block");
        h.getLevel().setBlockAndUpdate(pos.above(), Blocks.CHEST.defaultBlockState());
        h.assertTrue(!journal.place(h.getLevel(), pos.above(), d), "never replaces chest");
        var occupied = h.absolutePos(new BlockPos(6, Y + 1, 4)); enemy(h, 6, 4);
        h.assertTrue(!journal.place(h.getLevel(), occupied, d), "never places a solid block inside a living creature"); h.succeed();
    }
    private static void magmaRestore(GameTestHelper h) {
        var d = dragon(h); var journal = new DragonMagmaTrail(); var pos = patch(h); journal.place(h.getLevel(), pos, d);
        var encoded = DragonMagmaTrail.CODEC.encodeStart(JsonOps.INSTANCE, journal).getOrThrow();
        var loaded = DragonMagmaTrail.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        h.assertValueEqual(loaded.size(), 1, "restore journal serializes position and expiry");
        h.runAfterDelay(170, () -> { loaded.tick(h.getLevel()); h.assertTrue(h.getLevel().getBlockState(pos).isAir(), "saved journal restores original air after expiry"); h.assertValueEqual(loaded.size(), 0, "expired patch removed"); h.succeed(); });
    }
    private static void magmaEdit(GameTestHelper h) {
        var d = dragon(h); var journal = new DragonMagmaTrail(); var pos = patch(h); journal.place(h.getLevel(), pos, d);
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState()); journal.tick(h.getLevel());
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK) && journal.size() == 0, "later player edit wins over restoration"); h.succeed();
    }
    private static void magmaExactAir(GameTestHelper h) {
        var d = dragon(h); var journal = new DragonMagmaTrail(); var pos = patch(h);
        h.getLevel().setBlockAndUpdate(pos, Blocks.CAVE_AIR.defaultBlockState());
        h.assertTrue(journal.place(h.getLevel(), pos, d), "cave air is a valid empty surface");
        var loaded = DragonMagmaTrail.CODEC.parse(JsonOps.INSTANCE, DragonMagmaTrail.CODEC.encodeStart(JsonOps.INSTANCE, journal).getOrThrow()).getOrThrow();
        h.runAfterDelay(170, () -> { loaded.tick(h.getLevel()); h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.CAVE_AIR), "exact original air variant restored after serialization"); h.succeed(); });
    }
    private static void magmaOwner(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); var journal = new DragonMagmaTrail(); var pos = patch(h); journal.place(h.getLevel(), pos, d);
        p.setPos(Vec3.atBottomCenterOf(pos.above())); float health = p.getHealth();
        h.runAfterDelay(30, () -> { journal.tick(h.getLevel()); h.assertValueEqual(p.getHealth(), health, "temporary magma has no vanilla owner burn"); h.succeed(); });
    }
    private static BabyDragon breathing(GameTestHelper h) {
        var d = dragon(h); owner(h, d); stage(h, d, DragonStage.ADULT); var target = enemy(h, 9, 6);
        d.setTarget(target); d.setYRot(-90); d.setYHeadRot(-90); d.setXRot(0); return d;
    }
    private static void breath(GameTestHelper h) {
        var d = breathing(h); var target = d.getTarget(); h.assertTrue(d.beginBreath(), "adult begins breath");
        var observations = new StringBuilder();
        for(int tick:new int[]{25,35,45}) h.runAfterDelay(tick,()->observations.append(" mouth=").append(d.getMouthPosition().subtract(d.position()))
                .append(" dir=").append(d.getFireDirection()).append(" yaw=").append(d.getYHeadRot()).append(" body=").append(d.yBodyRot)
                .append(" target=").append(target.position().subtract(d.position())).append(" pitch=").append(d.getXRot()).append(" clear=").append(d.hasClearMouth()).append(" hp=").append(target.getHealth()));
        h.runAfterDelay(15, () -> h.assertValueEqual(target.getHealth(), 200F, "preparation cannot damage"));
        h.runAfterDelay(55, () -> { h.assertTrue(target.getHealth() <= 185, "continuous breath applies multiple five-point pulses"+observations); h.assertTrue(target.isOnFire(), "breath ignites enemies"); h.succeed(); });
    }
    private static void breathWall(GameTestHelper h) {
        var d = breathing(h); var target = d.getTarget();
        for (int z = 3; z <= 9; z++) for (int y = Y + 1; y <= Y + 4; y++) h.setBlock(new BlockPos(7, y, z), Blocks.STONE);
        d.applyBreathDamage(); h.assertValueEqual(target.getHealth(), 200F, "cone collision checks line of sight"); h.succeed();
    }
    private static void breathAllies(GameTestHelper h) {
        var d = breathing(h); var p = (Player)d.getOwner(); p.setPos(h.absoluteVec(new Vec3(7, Y + 1, 6.5)));
        var wolf = h.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(8, Y + 1, 6)); wolf.tame(p); wolf.setNoGravity(true);
        float health = wolf.getHealth(); d.applyBreathDamage();
        h.assertValueEqual(wolf.getHealth(), health, "pet inside cone protected"); h.assertTrue(!wolf.isOnFire() && !p.isOnFire(), "allies never ignited"); h.succeed();
    }
    private static void breathCancel(GameTestHelper h) {
        var d = breathing(h); h.assertTrue(d.beginBreath(), "starts once"); h.assertTrue(!d.beginBreath(), "cannot stack breath"); d.setTarget(null);
        h.runAfterDelay(2, () -> { h.assertValueEqual(d.getBreathTicks(), 0, "invalid target cancels"); h.assertTrue(d.getBreathCooldown() > 0 && !d.beginBreath(), "cancel retains cooldown"); h.assertTrue(copy(h, d).getBreathCooldown() > 0, "cooldown persists"); h.succeed(); });
    }
    private static void mountGates(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); h.assertTrue(!d.mountOwner(p), "baby cannot mount"); stage(h, d, DragonStage.YOUNG);
        h.assertTrue(!d.mountOwner(p) && !d.requestShoulder(p), "young cannot mount or perch"); stage(h, d, DragonStage.ADULT);
        Player stranger = h.makeMockPlayer(GameType.SURVIVAL); h.assertTrue(!d.mountOwner(stranger), "stranger cannot ride");
        p.setPos(d.position().add(0, 0, 2)); h.assertTrue(d.mountOwner(p) && d.getRider() == p, "owner rides adult"); h.succeed();
    }
    private static void ridingFlight(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.ADULT); p.setPos(d.position().add(0, 0, 2)); d.mountOwner(p);
        Vec3 start = d.position(); h.assertTrue(d.acceptRiderInput(p, 5, -90, 0), "owner flight intent accepted");
        for (int i = 0; i < 10; i++) d.travel(Vec3.ZERO);
        h.assertTrue(d.isFlying() && d.getY() > start.y + .4 && d.position().distanceToSqr(start) > .5, "server flight moves smoothly");
        h.assertTrue(d.clearBlocks(d.position()), "mounted flight retains collision"); h.succeed();
    }
    private static void forgedInput(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.ADULT); d.mountOwner(p);
        Player stranger = h.makeMockPlayer(GameType.SURVIVAL);
        h.assertTrue(!d.acceptRiderInput(stranger, 255, 0, 0), "non-rider rejected");
        h.assertTrue(!d.acceptRiderInput(p, 1, Float.NaN, 0) && !d.acceptRiderInput(p, 1024, 0, 0), "invalid rotations and action bits rejected");
        h.assertValueEqual(d.getDragonXp(), 2000, "packets cannot grant XP"); h.succeed();
    }
    private static void riderAttacks(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.ADULT); d.mountOwner(p);
        // Real mounted travel applies the rider's aim; NoAI deliberately skips LivingEntity.travel.
        // Aim skyward so the long-range sphere cannot enter another test's arena.
        d.setNoAi(false); d.setXRot(-65); d.setYRot(-90); d.setYHeadRot(-90);
        d.acceptRiderInput(p, 16, -90, -65);
        h.runAfterDelay(8, () -> h.assertTrue(d.getChargeTicks() > 0, "mounted fireball has server preparation"));
        h.runAfterDelay(38, () -> {
            h.assertTrue(d.getDragonBrain().getAttackCooldown() > 0 && d.getChargeTicks() == 0, "mounted shot finishes and starts cooldown");
        });
        h.runAfterDelay(46, () -> d.acceptRiderInput(p, 32, -90, -65));
        h.runAfterDelay(50, () -> { h.assertTrue(d.getBreathTicks() > 0 && d.getBreathCooldown() > 0, "mounted breath starts without autonomous target"); h.succeed(); });
    }
    private static void grownRest(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.ADULT);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK)); d.mobInteract(p, InteractionHand.MAIN_HAND);
        h.assertTrue(d.isOrderedToSit(), "adult retains rest command using stick"); d.mobInteract(p, InteractionHand.MAIN_HAND);
        h.assertTrue(!d.isOrderedToSit(), "adult retains follow command"); h.succeed();
    }
    private static void inventoryAccess(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); p.setPos(d.position().add(0, 0, 2)); var menu = new DragonMenu(1, p.getInventory(), d);
        h.assertTrue(menu.slots.stream().limit(18).noneMatch(Slot -> Slot.isActive()), "baby has zero storage slots");
        stage(h, d, DragonStage.YOUNG); h.assertValueEqual(menu.slots.stream().limit(18).filter(Slot -> Slot.isActive()).count(), 9L, "young has nine slots");
        h.assertTrue(!menu.stillValid(h.makeMockPlayer(GameType.SURVIVAL)), "stranger access denied");
        p.setPos(p.position().add(20, 0, 0)); h.assertTrue(!menu.stillValid(p), "distance closes inventory"); h.succeed();
    }
    private static void inventoryTransfer(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.YOUNG); p.setPos(d.position().add(0, 0, 2));
        p.getInventory().setItem(9, new ItemStack(Items.DIAMOND, 11)); var menu = new DragonMenu(1, p.getInventory(), d);
        menu.quickMoveStack(p, 18); h.assertValueEqual(d.getDragonInventory().getItem(0).getCount(), 11, "native shift transfer into dragon");
        h.assertTrue(p.getInventory().getItem(9).isEmpty(), "source consumed exactly once"); menu.quickMoveStack(p, 18);
        h.assertValueEqual(d.getDragonInventory().getItem(0).getCount(), 11, "repeated click cannot duplicate"); menu.quickMoveStack(p, 0);
        h.assertTrue(d.getDragonInventory().getItem(0).isEmpty(), "transfer out clears source"); h.succeed();
    }
    private static void inventoryGrowth(GameTestHelper h) {
        var d = dragon(h); owner(h, d); stage(h, d, DragonStage.YOUNG); d.getDragonInventory().setItem(8, new ItemStack(Items.EMERALD, 23));
        stage(h, d, DragonStage.ADULT); d.getDragonInventory().setItem(17, new ItemStack(Items.DIAMOND, 5));
        h.assertTrue(!d.setDragonStage(DragonStage.YOUNG, false), "admin downgrade refuses to hide stored items");
        var loaded = copy(h, d); h.assertValueEqual(loaded.getDragonInventory().getItem(8).getCount(), 23, "young inventory preserved");
        h.assertValueEqual(loaded.getDragonInventory().getItem(17).getCount(), 5, "adult inventory preserved"); h.succeed();
    }
    private static void inventoryDeath(GameTestHelper h) {
        var d = dragon(h); owner(h, d); stage(h, d, DragonStage.YOUNG); d.getDragonInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        var box = d.getBoundingBox().inflate(3); d.setHealth(0); d.die(d.damageSources().generic()); d.die(d.damageSources().generic());
        int count = h.getLevel().getEntitiesOfClass(ItemEntity.class, box).stream().filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertValueEqual(count, 7, "inventory drops once including repeated death callbacks"); h.assertTrue(d.getDragonInventory().isEmpty(), "inventory cleared after delivery"); h.succeed();
    }
    private static void menuData(GameTestHelper h) {
        var d = dragon(h); var p = owner(h, d); stage(h, d, DragonStage.YOUNG); var menu = new DragonMenu(1, p.getInventory(), d);
        d.addDragonXp(37); d.setHealth(42);
        h.assertValueEqual(menu.value(1), 537, "live XP data"); h.assertValueEqual(menu.value(2), 42, "live health data");
        h.assertValueEqual(menu.getStage(), DragonStage.YOUNG, "live stage data"); h.assertValueEqual(menu.ownerName(), p.getName().getString(), "owner data"); h.succeed();
    }
    private static void commands(GameTestHelper h) {
        var d = dragon(h); owner(h, d); var other = h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(), new BlockPos(11, Y + 1, 3));
        var source = h.getLevel().getServer().createCommandSourceStack().withLevel(h.getLevel());
        var commands = h.getLevel().getServer().getCommands(); commands.performPrefixedCommand(source, "dragon " + d.getUUID() + " stage adult");
        h.assertValueEqual(d.getDragonStage(), DragonStage.ADULT, "operator can set selected stage"); h.assertValueEqual(other.getDragonStage(), DragonStage.BABY, "other dragon unchanged");
        h.succeed();
    }
    private DragonEvolutionGameTests() {}
}
