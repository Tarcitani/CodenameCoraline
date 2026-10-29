package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.codenamecoraline.entity.basilisk.WitherMistCloud;
import com.codenamecoraline.registry.ModEffects;
import com.codenamecoraline.registry.ModPotions;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import java.util.Map;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Opt-in server integration tests. Never registered during normal client or server runs. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class BasiliskGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("immunities_and_weaknesses", BasiliskGameTests::immunitiesAndWeaknesses),
            Map.entry("soul_light_and_provocation", BasiliskGameTests::soulLight),
            Map.entry("cloud_applies_vanilla_wither", BasiliskGameTests::cloud),
            Map.entry("overlapping_clouds_do_not_stack", BasiliskGameTests::overlap),
            Map.entry("walls_block_wither_mist", BasiliskGameTests::wall),
            Map.entry("potion_cures_and_blocks_wither", BasiliskGameTests::potion),
            Map.entry("resistance_expires", BasiliskGameTests::expiry),
            Map.entry("milk_removes_resistance", BasiliskGameTests::milk),
            Map.entry("vanilla_brewing_stand_chain", BasiliskGameTests::brewingStand),
            Map.entry("brewing_rejects_wrong_order", BasiliskGameTests::brewingOrder),
            Map.entry("vanilla_potion_variants", BasiliskGameTests::potionVariants),
            Map.entry("mist_upgrades_weaker_wither", BasiliskGameTests::witherStrength),
            Map.entry("only_basilisk_parts_drop", BasiliskGameTests::drops),
            Map.entry("ai_hops_inhales_and_exhales", BasiliskGameTests::combatAi),
            Map.entry("common_hostile_spawn_tables", BasiliskGameTests::spawnTables),
            Map.entry("swamp_and_underground_spawn_rules", BasiliskGameTests::spawnHabitats));

    private BasiliskGameTests() {}
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name); }

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        if (!Boolean.getBoolean("coraline.basiliskTests")) return;
        event.register(Registries.TEST_FUNCTION, registry -> TESTS.forEach((name, function) -> registry.register(id(name), function)));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("coraline.basiliskTests")) return;
        var environment = event.registerEnvironment(id("basilisk_test_environment"));
        TESTS.forEach((name, function) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), 180, 0, true))));
    }

    private static Basilisk basilisk(GameTestHelper helper, int x) {
        Basilisk mob = helper.spawnWithNoFreeWill(ModEntities.BASILISK.get(), new BlockPos(x, 1, 3));
        mob.setNoGravity(true);
        return mob;
    }

    private static void immunitiesAndWeaknesses(GameTestHelper helper) {
        Basilisk mob = basilisk(helper, 3);
        helper.assertValueEqual(mob.getMaxHealth(), 28.0F, "base health");
        helper.assertValueEqual(mob.getAttributeValue(Attributes.ARMOR), 2.0, "base armor");
        for (var effect : java.util.List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.DARKNESS)) {
            helper.assertTrue(!mob.addEffect(new MobEffectInstance(effect, 200)), "Basilisk must reject poison, wither and darkness");
        }
        mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
        mob.hurtServer(helper.getLevel(), mob.damageSources().lava(), 4);
        helper.assertValueEqual(mob.getHealth(), 22.0F, "fire multiplier");
        Basilisk swordTarget = basilisk(helper, 7);
        swordTarget.getAttribute(Attributes.ARMOR).setBaseValue(0);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        swordTarget.hurtServer(helper.getLevel(), swordTarget.damageSources().playerAttack(player), 4);
        helper.assertValueEqual(swordTarget.getHealth(), 23.0F, "sword multiplier");
        helper.succeed();
    }

    private static void soulLight(GameTestHelper helper) {
        Basilisk mob = basilisk(helper, 3);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(4, 1, 3)));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SOUL_TORCH));
        helper.assertTrue(mob.isRepelledBy(player) && !mob.canAttack(player), "off-hand torch repels");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SOUL_LANTERN));
        helper.assertTrue(mob.isRepelledBy(player), "main-hand lantern repels");
        mob.hurtServer(helper.getLevel(), mob.damageSources().playerAttack(player), 1);
        helper.assertTrue(!mob.isRepelledBy(player) && mob.canAttack(player), "direct attacker loses protection");
        helper.succeed();
    }

    private static LivingEntity victim(GameTestHelper helper) {
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 1, 5));
        pig.setNoGravity(true);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
        pig.setHealth(40);
        return pig;
    }

    private static WitherMistCloud mist(GameTestHelper helper, LivingEntity target) {
        WitherMistCloud cloud = WitherMistCloud.create(helper.getLevel(), target.position().add(0, 0.1, 0), null);
        helper.getLevel().addFreshEntity(cloud);
        return cloud;
    }

    private static void cloud(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        WitherMistCloud cloud = mist(helper, target);
        float health = target.getHealth();
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(target.hasEffect(MobEffects.WITHER), "mist applies vanilla Wither immediately");
            helper.assertValueEqual(target.getEffect(MobEffects.WITHER).getAmplifier(), 2, "Wither III");
            helper.assertValueEqual(target.getHealth(), health - 1, "only the first vanilla damage tick, without an instant lethal proc");
        });
        helper.runAfterDelay(75, () -> {
            helper.assertValueEqual(target.getHealth(), health - 8, "vanilla Wither III keeps ticking inside the mist");
            helper.assertTrue(target.isAlive(), "mist no longer triggers instant death");
        });
        helper.runAfterDelay(110, () -> {
            helper.assertTrue(cloud.isRemoved(), "mist expires after five seconds");
            helper.assertTrue(!target.hasEffect(MobEffects.WITHER), "Wither expires naturally");
            helper.succeed();
        });
    }

    private static void overlap(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        mist(helper, target);
        helper.runAfterDelay(12, () -> mist(helper, target));
        helper.runAfterDelay(75, () -> {
            helper.assertValueEqual(target.getHealth(), target.getMaxHealth() - 8, "overlap neither stacks nor delays Wither damage");
            helper.assertValueEqual(target.getEffect(MobEffects.WITHER).getAmplifier(), 2, "overlap preserves Wither III");
            helper.succeed();
        });
    }

    private static void wall(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        helper.setBlock(new BlockPos(4, 1, 5), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 2, 5), Blocks.STONE);
        var cloud = WitherMistCloud.create(helper.getLevel(), helper.absoluteVec(new Vec3(3, 1.1, 5.5)), null);
        helper.getLevel().addFreshEntity(cloud);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(!target.hasEffect(MobEffects.WITHER), "solid wall blocks exposure");
            helper.assertValueEqual(target.getHealth(), target.getMaxHealth(), "wall protects health");
            helper.succeed();
        });
    }

    private static void potion(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        ItemStack remainder = ModPotions.resistancePotion().finishUsingItem(helper.getLevel(), target);
        helper.assertTrue(remainder.is(Items.GLASS_BOTTLE), "drinking returns a glass bottle");
        helper.assertTrue(target.hasEffect(ModEffects.WITHER_RESISTANCE), "drinking grants resistance");
        helper.assertValueEqual(target.getEffect(ModEffects.WITHER_RESISTANCE).getDuration(), 3600, "three-minute duration");
        helper.assertTrue(!target.hasEffect(MobEffects.WITHER), "potion cures existing Wither");
        helper.assertTrue(!target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2)), "protection rejects Wither from any source");
        mist(helper, target);
        helper.runAfterDelay(75, () -> {
            helper.assertTrue(!target.hasEffect(MobEffects.WITHER), "potion blocks Basilisk mist");
            helper.assertValueEqual(target.getHealth(), target.getMaxHealth(), "protected victim takes no Wither damage");
            helper.succeed();
        });
    }

    private static void expiry(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        target.addEffect(new MobEffectInstance(ModEffects.WITHER_RESISTANCE, 20));
        helper.assertTrue(!target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100)), "resistance initially blocks Wither");
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(!target.hasEffect(ModEffects.WITHER_RESISTANCE), "protection expires");
            helper.assertTrue(target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100)), "Wither applies again after expiry");
            helper.succeed();
        });
    }

    private static void milk(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        ModPotions.resistancePotion().finishUsingItem(helper.getLevel(), target);
        new ItemStack(Items.MILK_BUCKET).finishUsingItem(helper.getLevel(), target);
        helper.assertTrue(!target.hasEffect(ModEffects.WITHER_RESISTANCE), "vanilla milk removes protection");
        helper.assertTrue(target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100)), "Wither applies after milk");
        helper.succeed();
    }

    private static ItemStack potionStack(Holder<Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    private static void assertPotion(GameTestHelper helper, ItemStack stack, Holder<Potion> expected) {
        helper.assertTrue(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(expected),
                "expected native potion " + expected.unwrapKey().orElseThrow().identifier());
    }

    private static void brewStage(GameTestHelper helper, BrewingStandBlockEntity stand,
                                  Item ingredient, Holder<Potion> expected) {
        stand.setItem(3, new ItemStack(ingredient, 2));
        ItemStack before = stand.getItem(0).copy();
        var level = helper.getLevel();
        for (int tick = 0; tick < 400; tick++) {
            BrewingStandBlockEntity.serverTick(level, stand.getBlockPos(), level.getBlockState(stand.getBlockPos()), stand);
        }
        helper.assertTrue(ItemStack.isSameItemSameComponents(stand.getItem(0), before), "brewing waits the full vanilla 400 ticks");
        helper.assertValueEqual(stand.getItem(3).getCount(), 2, "ingredient is not consumed before brewing finishes");
        BrewingStandBlockEntity.serverTick(level, stand.getBlockPos(), level.getBlockState(stand.getBlockPos()), stand);
        for (int slot = 0; slot < 3; slot++) assertPotion(helper, stand.getItem(slot), expected);
        helper.assertValueEqual(stand.getItem(3).getCount(), 1, "one ingredient brews all three bottles");
        stand.setItem(3, ItemStack.EMPTY);
    }

    private static void brewingStand(GameTestHelper helper) {
        var pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, Blocks.BREWING_STAND);
        var level = helper.getLevel();
        var stand = (BrewingStandBlockEntity) level.getBlockEntity(helper.absolutePos(pos));
        for (int slot = 0; slot < 3; slot++) stand.setItem(slot, potionStack(Potions.WATER));
        stand.setItem(3, new ItemStack(Items.NETHER_WART));
        for (int tick = 0; tick < 405; tick++) {
            BrewingStandBlockEntity.serverTick(level, stand.getBlockPos(), level.getBlockState(stand.getBlockPos()), stand);
        }
        assertPotion(helper, stand.getItem(0), Potions.WATER);
        helper.assertValueEqual(stand.getItem(3).getCount(), 1, "no brewing without blaze powder fuel");
        stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        brewStage(helper, stand, Items.NETHER_WART, Potions.AWKWARD);
        helper.assertTrue(stand.getItem(4).isEmpty(), "stand consumes blaze powder");
        brewStage(helper, stand, ModItems.BASILISK_LEG.get(), ModPotions.BASILISK_LEG_INFUSION);
        brewStage(helper, stand, ModItems.BASILISK_EYE.get(), ModPotions.BASILISK_EYE_INFUSION);
        brewStage(helper, stand, ModItems.BASILISK_TAIL.get(), ModPotions.WITHER_RESISTANCE);
        helper.assertTrue(ItemStack.isSameItemSameComponents(stand.getItem(0), ModPotions.resistancePotion()), "brewed potion uses the registered native type");
        ItemStack drink = stand.getItem(0).copy();
        LivingEntity target = victim(helper);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
        drink.finishUsingItem(level, target);
        helper.assertTrue(!target.hasEffect(MobEffects.WITHER), "brewed potion cures Wither III");
        helper.assertTrue(target.hasEffect(ModEffects.WITHER_RESISTANCE), "brewed potion grants resistance");
        helper.assertValueEqual(target.getEffect(ModEffects.WITHER_RESISTANCE).getDuration(), 3600, "brewed duration is three minutes");
        brewStage(helper, stand, Items.REDSTONE, ModPotions.LONG_WITHER_RESISTANCE);
        stand.getItem(0).copy().finishUsingItem(level, target);
        helper.assertValueEqual(target.getEffect(ModEffects.WITHER_RESISTANCE).getDuration(), 9600, "redstone extends resistance to eight minutes");
        helper.succeed();
    }

    private static void brewingOrder(GameTestHelper helper) {
        var brewing = helper.getLevel().potionBrewing();
        helper.assertTrue(!brewing.hasMix(potionStack(Potions.WATER), new ItemStack(ModItems.BASILISK_LEG.get())), "water requires nether wart first");
        helper.assertTrue(!brewing.hasMix(potionStack(Potions.AWKWARD), new ItemStack(ModItems.BASILISK_EYE.get())), "eye cannot skip the leg");
        helper.assertTrue(!brewing.hasMix(potionStack(Potions.AWKWARD), new ItemStack(ModItems.BASILISK_TAIL.get())), "tail cannot skip previous steps");
        helper.assertTrue(!brewing.hasMix(potionStack(ModPotions.BASILISK_LEG_INFUSION), new ItemStack(ModItems.BASILISK_TAIL.get())), "tail requires the eye infusion");
        helper.assertTrue(!brewing.hasMix(potionStack(ModPotions.BASILISK_EYE_INFUSION), new ItemStack(Items.REDSTONE)), "redstone cannot finish an intermediate infusion");
        helper.succeed();
    }

    private static void potionVariants(GameTestHelper helper) {
        var brewing = helper.getLevel().potionBrewing();
        ItemStack splash = brewing.mix(new ItemStack(Items.GUNPOWDER), ModPotions.resistancePotion());
        helper.assertTrue(splash.is(Items.SPLASH_POTION), "gunpowder makes a splash potion");
        assertPotion(helper, splash, ModPotions.WITHER_RESISTANCE);
        ItemStack lingering = brewing.mix(new ItemStack(Items.DRAGON_BREATH), splash);
        helper.assertTrue(lingering.is(Items.LINGERING_POTION), "dragon breath makes a lingering potion");
        assertPotion(helper, lingering, ModPotions.WITHER_RESISTANCE);
        ItemStack extended = brewing.mix(new ItemStack(Items.REDSTONE), lingering);
        helper.assertTrue(extended.is(Items.LINGERING_POTION), "extension preserves the potion container");
        assertPotion(helper, extended, ModPotions.LONG_WITHER_RESISTANCE);
        helper.succeed();
    }

    private static void witherStrength(GameTestHelper helper) {
        LivingEntity target = victim(helper);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
        WitherMistCloud cloud = mist(helper, target);
        helper.runAfterDelay(5, () -> {
            helper.assertValueEqual(target.getEffect(MobEffects.WITHER).getAmplifier(), 2, "mist upgrades existing Wither I to III");
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 3));
        });
        helper.runAfterDelay(15, () -> {
            helper.assertValueEqual(target.getEffect(MobEffects.WITHER).getAmplifier(), 3, "mist preserves stronger Wither IV");
            cloud.discard();
            helper.succeed();
        });
    }

    private static void drops(GameTestHelper helper) {
        for (int i = 0; i < 24; i++) {
            Basilisk mob = basilisk(helper, 5);
            mob.getRandom().setSeed(1729L + i * 7919L);
            mob.hurtServer(helper.getLevel(), mob.damageSources().genericKill(), 1000);
        }
        helper.runAfterDelay(25, () -> {
            var items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    helper.absoluteAABB(new net.minecraft.world.phys.AABB(0, 0, 0, 10, 5, 10)).inflate(1));
            helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(ModItems.BASILISK_LEG.get())), "legs drop");
            helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(ModItems.BASILISK_EYE.get())), "eyes drop");
            helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(ModItems.BASILISK_TAIL.get())), "tails drop");
            helper.assertTrue(items.stream().allMatch(item -> item.getItem().is(ModItems.BASILISK_LEG.get())
                    || item.getItem().is(ModItems.BASILISK_EYE.get())
                    || item.getItem().is(ModItems.BASILISK_TAIL.get())), "retired rewards never drop");
            helper.succeed();
        });
    }

    private static void spawnTables(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : java.util.List.of(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.PLAINS,
                Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES, Biomes.DEEP_DARK)) {
            var entries = biomes.getOrThrow(key).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
            var basilisk = entries.stream().filter(entry -> entry.value().type() == ModEntities.BASILISK.get()).toList();
            helper.assertValueEqual(basilisk.size(), 1, "one Basilisk spawn entry per Overworld biome");
            helper.assertValueEqual(basilisk.getFirst().weight(), 100, "common monster spawn weight");
            helper.assertValueEqual(basilisk.getFirst().value().minCount(), 4, "minimum group of four");
            helper.assertValueEqual(basilisk.getFirst().value().maxCount(), 8, "maximum group of eight");
        }
        helper.assertValueEqual(ModEntities.BASILISK.get().getCategory(), MobCategory.MONSTER, "Basilisk remains hostile");
        helper.assertValueEqual(basilisk(helper, 3).getMaxSpawnClusterSize(), 8, "entity permits full groups of eight");
        helper.succeed();
    }

    private static void testBiome(GameTestHelper helper, BlockPos pos, ResourceKey<Biome> key) {
        var level = helper.getLevel();
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key);
        for (int dx = -16; dx <= 16; dx += 16) {
            for (int dz = -16; dz <= 16; dz += 16) {
                level.getChunkAt(pos.offset(dx, 0, dz)).fillBiomesFromNoise((x, y, z, sampler) -> biome,
                        level.getChunkSource().randomState().sampler());
            }
        }
    }

    private static boolean naturalSpawn(GameTestHelper helper, BlockPos pos) {
        return Basilisk.checkSpawnRules(ModEntities.BASILISK.get(), helper.getLevel(),
                EntitySpawnReason.NATURAL, pos, helper.getLevel().getRandom());
    }

    private static void spawnHabitats(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(5, 1, 5);
        var pos = helper.absolutePos(relative);
        level.getServer().clockManager().setTotalTicks(level.dimensionTypeRegistration().value().defaultClock().orElseThrow(), 6000);
        helper.setBlock(relative.below(), Blocks.STONE);
        helper.assertTrue(level.canSeeSky(pos), "surface fixture has open sky");
        testBiome(helper, pos, Biomes.PLAINS);
        helper.assertTrue(!naturalSpawn(helper, pos), "ordinary open plains are not a habitat");
        testBiome(helper, pos, Biomes.SWAMP);
        helper.assertTrue(naturalSpawn(helper, pos), "swamps allow surface spawning in daylight");
        testBiome(helper, pos, Biomes.MANGROVE_SWAMP);
        helper.assertTrue(naturalSpawn(helper, pos), "mangroves allow surface spawning in daylight");
        helper.setBlock(relative.below(), Blocks.AIR);
        helper.assertTrue(!naturalSpawn(helper, pos), "swamp spawning still requires valid ground");
        helper.setBlock(relative.below(), Blocks.STONE);
        testBiome(helper, pos, Biomes.PLAINS);
        // A closed stone chamber gives the lighting engine an unambiguous dark cave.
        for (int x = 2; x <= 8; x++) {
            for (int z = 2; z <= 8; z++) {
                for (int y = 1; y <= 5; y++) {
                    if (x == 2 || x == 8 || z == 2 || z == 8 || y == 5) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                    }
                }
            }
        }
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(!level.canSeeSky(pos), "cave fixture has no open sky");
            helper.assertTrue(naturalSpawn(helper, pos), "dark caves also spawn Basilisks beneath plains");
            testBiome(helper, pos, Biomes.NETHER_WASTES);
            helper.assertTrue(!naturalSpawn(helper, pos), "Nether is excluded");
            testBiome(helper, pos, Biomes.PLAINS);
            helper.setBlock(relative.east(), Blocks.TORCH);
        });
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(!naturalSpawn(helper, pos), "well-lit caves are excluded");
            helper.succeed();
        });
    }

    private static void combatAi(GameTestHelper helper) {
        var target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(7, 1, 5));
        Basilisk mob = helper.spawn(ModEntities.BASILISK.get(), new BlockPos(3, 1, 5));
        mob.setTarget(target);
        boolean[] seen = new boolean[3];
        helper.onEachTick(() -> {
            if (mob.getState() == Basilisk.State.HOPPING) seen[0] = true;
            if (mob.getState() == Basilisk.State.INHALING) seen[1] = true;
            if (mob.getState() == Basilisk.State.EXHALING) seen[2] = true;
        });
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(seen[0], "combat AI must leap");
            helper.assertTrue(seen[1], "combat AI must telegraph its breath");
            helper.assertTrue(seen[2], "combat AI must release its breath");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(WitherMistCloud.class,
                    mob.getBoundingBox().inflate(10)).isEmpty(), "AI breath must leave a cloud");
            helper.succeed();
        });
    }
}
