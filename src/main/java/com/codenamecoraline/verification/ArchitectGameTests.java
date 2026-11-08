package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.architect.Architect;
import com.codenamecoraline.item.CommandmentTabletItem;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Server integration tests for the Architect foundation: registration, attributes, phases, statue and summoning (Slice 1). */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class ArchitectGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("architect_attributes_and_registration", ArchitectGameTests::attributesAndRegistration),
            Map.entry("architect_phase_thresholds", ArchitectGameTests::phaseThresholds),
            Map.entry("architect_dormant_statue_constitution", ArchitectGameTests::dormantStatueConstitution),
            Map.entry("architect_tablet_summon_rules", ArchitectGameTests::tabletSummonRules),
            Map.entry("architect_player_hit_awakens_statue", ArchitectGameTests::playerHitAwakensStatue));

    private ArchitectGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.architectTests");
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!enabled()) return;
        event.register(Registries.TEST_FUNCTION,
                registry -> TESTS.forEach((name, fn) -> registry.register(id(name), fn)));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!enabled()) return;
        var environment = event.registerEnvironment(id("architect_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 200, 0, true))));
    }

    private static void attributesAndRegistration(GameTestHelper helper) {
        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        helper.assertValueEqual(architect.getMaxHealth(), 480.0F, "Architect max health");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ARMOR), 16.0D, "Architect armor");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 8.0D, "Architect armor toughness");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.ATTACK_DAMAGE), 12.0D, "Architect base damage");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.30D, "Architect movement speed");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.95D, "Architect knockback resistance");
        helper.assertValueEqual(architect.getAttributeValue(Attributes.FOLLOW_RANGE), 48.0D, "Architect follow range");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getCategory(), MobCategory.MONSTER, "Architect category");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getWidth(), Architect.WIDTH, "Architect collision width");
        helper.assertValueEqual(ModEntities.ARCHITECT.get().getHeight(), Architect.HEIGHT, "Architect collision height");
        helper.assertValueEqual(architect.getState(), Architect.State.DORMANT, "Architect starts as the statue");
        helper.assertValueEqual(architect.getPhase(), Architect.Phase.MONUMENT, "Dormant Architect is in the monument phase");
        helper.assertTrue(architect.isPersistenceRequired(), "Architect never despawns");
        helper.assertTrue(ModItems.ARCHITECT_SPAWN_EGG.get() instanceof SpawnEggItem, "Architect spawn egg registered");
        helper.assertTrue(ModItems.COMMANDMENT_TABLET.get() instanceof CommandmentTabletItem, "Tablet of the Commandments registered");
        helper.succeed();
    }

    private static void phaseThresholds(GameTestHelper helper) {
        helper.assertValueEqual(Architect.phaseFor(1.0F), Architect.Phase.DUELIST, "Full health is the Duelist");
        helper.assertValueEqual(Architect.phaseFor(0.70F), Architect.Phase.DUELIST, "70 % is still the Duelist");
        helper.assertValueEqual(Architect.phaseFor(0.69F), Architect.Phase.ADMINISTRATOR, "Below 70 % is the Administrator");
        helper.assertValueEqual(Architect.phaseFor(0.35F), Architect.Phase.ADMINISTRATOR, "35 % is still the Administrator");
        helper.assertValueEqual(Architect.phaseFor(0.34F), Architect.Phase.COLLAPSE, "Below 35 % is the Collapse");

        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        architect.setState(Architect.State.COMBAT_IDLE);
        architect.setHealth(architect.getMaxHealth() * 0.5F);
        helper.assertValueEqual(architect.getPhase(), Architect.Phase.ADMINISTRATOR, "Awake Architect at half health");
        helper.succeed();
    }

    private static void dormantStatueConstitution(GameTestHelper helper) {
        Architect architect = helper.spawnWithNoFreeWill(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        float health = architect.getHealth();
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 20.0F);
        helper.assertValueEqual(architect.getHealth(), health, "The dormant statue ignores damage that is not from a player");
        helper.assertFalse(architect.isPushable(), "The dormant statue cannot be pushed");
        helper.assertFalse(architect.causeFallDamage(5.0D, 1.0F, helper.getLevel().damageSources().fall()),
                "Falls under 6 blocks are absorbed by the wings");
        helper.assertFalse(architect.canBeAffected(new MobEffectInstance(MobEffects.POISON, 100)), "Stone ignores poison");
        helper.assertFalse(architect.canBeAffected(new MobEffectInstance(MobEffects.WITHER, 100)), "Stone ignores wither");
        helper.assertTrue(architect.canBreatheUnderwater(), "Stone does not drown");
        helper.assertFalse(architect.fireImmune(), "The Architect is not immune to fire");

        architect.setState(Architect.State.COMBAT_IDLE);
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 20.0F);
        helper.assertTrue(architect.getHealth() < health, "The awake Architect takes damage");
        helper.succeed();
    }

    /** Slice 2: a player's hit wakes the statue; the reveal plays its 110 ticks (invulnerable), then the combat idle. */
    private static void playerHitAwakensStatue(GameTestHelper helper) {
        Architect architect = helper.spawn(ModEntities.ARCHITECT.get(), new BlockPos(3, 1, 3));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        float health = architect.getHealth();
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 10.0F);
        helper.assertValueEqual(architect.getState(), Architect.State.AWAKENING, "A player hit wakes the statue");
        helper.assertValueEqual(architect.getAnim(), com.codenamecoraline.entity.architect.ArchitectAnim.AWAKEN, "The reveal clip plays");
        helper.assertValueEqual(architect.getHealth(), health, "The waking hit itself does no damage");
        architect.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 10.0F);
        helper.assertValueEqual(architect.getHealth(), health, "Invulnerable while the reveal plays");
        helper.runAfterDelay(112, () -> {
            helper.assertValueEqual(architect.getState(), Architect.State.COMBAT_IDLE, "After 110 ticks he stands in guard");
            helper.assertTrue(architect.getBossEvent().isVisible(), "The themed bar shows once he is awake");
            helper.succeed();
        });
    }

    private static void tabletSummonRules(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(2, 0, 2));
        level.setBlockAndUpdate(floor, Blocks.STONE_BRICKS.defaultBlockState());

        helper.assertValueEqual(CommandmentTabletItem.evaluateConditions(level, floor, Direction.NORTH),
                CommandmentTabletItem.SummonResult.WRONG_FACE, "The tablet only works on a top face");

        BlockPos ceiling = floor.above(3);
        level.setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
        helper.assertValueEqual(CommandmentTabletItem.evaluateConditions(level, floor, Direction.UP),
                CommandmentTabletItem.SummonResult.NO_ROOM, "The statue needs four free blocks of height");
        level.setBlockAndUpdate(ceiling, Blocks.AIR.defaultBlockState());

        ItemStack stack = new ItemStack(ModItems.COMMANDMENT_TABLET.get());
        CommandmentTabletItem.SummonResult result =
                CommandmentTabletItem.trySummon(level, floor, Direction.UP, 0.0F, null, stack);
        helper.assertValueEqual(result, CommandmentTabletItem.SummonResult.SUCCESS, "Summon on a free stone floor");
        helper.assertTrue(stack.isEmpty(), "A successful summon consumes the tablet");
        var summoned = level.getEntitiesOfClass(Architect.class, new net.minecraft.world.phys.AABB(floor.above()).inflate(1.0D));
        helper.assertValueEqual(summoned.size(), 1, "Exactly one statue rises on the tablet");
        helper.assertValueEqual(summoned.get(0).getState(), Architect.State.DORMANT, "The summoned Architect is a dormant statue");

        BlockPos other = helper.absolutePos(new BlockPos(6, 0, 6));
        level.setBlockAndUpdate(other, Blocks.STONE_BRICKS.defaultBlockState());
        ItemStack second = new ItemStack(ModItems.COMMANDMENT_TABLET.get());
        helper.assertValueEqual(CommandmentTabletItem.trySummon(level, other, Direction.UP, 0.0F, null, second),
                CommandmentTabletItem.SummonResult.ALREADY_ACTIVE, "Only one Architect within 96 blocks");
        helper.assertFalse(second.isEmpty(), "A failed summon keeps the tablet");
        helper.succeed();
    }
}
