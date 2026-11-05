package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.bestiary.BestiaryPage;
import com.codenamecoraline.client.ten_threads_witch.PuppetAnimations;
import com.codenamecoraline.client.ten_threads_witch.PuppetModel;
import com.codenamecoraline.client.ten_threads_witch.PuppetRenderState;
import com.codenamecoraline.client.ten_threads_witch.TenThreadsParts;
import com.codenamecoraline.entity.ten_threads_witch.AttackRunner;
import com.codenamecoraline.entity.ten_threads_witch.EncounterPhase;
import com.codenamecoraline.entity.ten_threads_witch.MatriarchWitchEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAnim;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAttack;
import com.codenamecoraline.entity.ten_threads_witch.PuppetEntity;
import com.codenamecoraline.entity.ten_threads_witch.PuppetKind;
import com.codenamecoraline.entity.ten_threads_witch.PuppetState;
import com.codenamecoraline.entity.ten_threads_witch.SpoolVitalOrbEntity;
import com.codenamecoraline.entity.ten_threads_witch.ThreadHitboxEntity;
import com.codenamecoraline.entity.ten_threads_witch.WitchDialogueManager;
import com.codenamecoraline.item.CursedSpindleItem;
import com.codenamecoraline.item.TenThreadsStaffItem;
import com.codenamecoraline.registry.ModEntities;
import com.codenamecoraline.registry.ModItems;
import com.codenamecoraline.registry.ModSounds;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Integration tests of the Witch of Ten Threads and her ten puppets: registration, attributes and content; models, UV
 * layout and animations; the Matriarch's invulnerability and the stage sequence (one puppet at a time, orbs, spools,
 * collapse, fury, death and its single casket); the crimson thread (cut, cancelled attack, TANGLED and its 1.75x), missed
 * special attacks, the porcelain crack and every weak point; every attack of every puppet (hurts a foe in range, never a
 * bystander puppet); "never swing at the air"; alliances; the staff (stances, cooldown, wear); the Cursed Spindle's rules;
 * the arena (anti-pillar, anti-hole, the push at the edge, the reset) and the dialogue hook.
 *
 * <p>Tests build their own plots high above the grid (stone floor) and keep every plot, mob and blow inside relative x and
 * z 0 to 18, and they use iron golems (never server players, which every puppet within 48 blocks would hunt) as targets.
 * The few tests that need a real {@link ServerPlayer}, the Spindle and the arena edge run on far plots of their own.</p>
 */
public final class TenThreadsWitchGameTests {
    private static final int Y = 100;
    private static final int PUMP_TICKS = 1200;

    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    static {
        TESTS.put("tt_registration_attributes_and_content", TenThreadsWitchGameTests::registrationAndContent);
        TESTS.put("tt_models_uv_and_animations", TenThreadsWitchGameTests::modelsUvAndAnimations);
        TESTS.put("tt_matriarch_invulnerable_until_stage_11", TenThreadsWitchGameTests::matriarchInvulnerable);
        TESTS.put("tt_sequence_stages_1_to_3", TenThreadsWitchGameTests::sequenceFirstStages);
        TESTS.put("tt_sequence_9_to_fury_death_and_casket", TenThreadsWitchGameTests::sequenceToDeath);
        TESTS.put("tt_thread_cut_cancels_and_tangles", TenThreadsWitchGameTests::threadCut);
        TESTS.put("tt_missed_special_tangles", TenThreadsWitchGameTests::missedSpecialTangles);
        TESTS.put("tt_hound_cracks_against_a_wall", TenThreadsWitchGameTests::houndCracks);
        TESTS.put("tt_weak_points_and_shells", TenThreadsWitchGameTests::weakPoints);
        TESTS.put("tt_never_swings_at_air_and_chases", TenThreadsWitchGameTests::neverSwingsAtAir);
        TESTS.put("tt_alliances", TenThreadsWitchGameTests::alliances);
        TESTS.put("tt_staff_postures_cooldown_and_wear", TenThreadsWitchGameTests::staffPostures);
        TESTS.put("tt_orb_rewards", TenThreadsWitchGameTests::orbRewards);
        TESTS.put("tt_dialogue_hook", TenThreadsWitchGameTests::dialogueHook);
        TESTS.put("tt_anti_pillar_pulls_the_target_down", TenThreadsWitchGameTests::antiPillar);
        TESTS.put("tt_anti_hole_lifts_the_puppet", TenThreadsWitchGameTests::antiHole);
        TESTS.put("tt_spindle_conditions", TenThreadsWitchGameTests::spindleConditions);
        TESTS.put("tt_arena_edge_and_advancement", TenThreadsWitchGameTests::arenaEdge);
        TESTS.put("tt_reset_when_everyone_leaves", TenThreadsWitchGameTests::resetWhenAlone);
        for (PuppetKind kind : PuppetKind.values()) {
            TESTS.put("tt_attacks_" + kind.id, h -> attacksHurtFoesSpareAllies(h, kind));
        }
    }

    private TenThreadsWitchGameTests() {}

    private static boolean enabled() {
        return Boolean.getBoolean("coraline.tenThreadsTests");
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, name);
    }

    public static void functions(RegisterEvent event) {
        if (!enabled()) return;
        event.register(Registries.TEST_FUNCTION, registry -> TESTS.forEach((name, fn) -> registry.register(id(name),
                (Consumer<GameTestHelper>) helper -> {
                    pump(helper);
                    fn.accept(helper);
                })));
    }

    public static void tests(RegisterGameTestsEvent event) {
        if (!enabled()) return;
        var environment = event.registerEnvironment(id("ten_threads_test_environment"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                        new TestData<>(environment, id("basilisk_test_arena"), PUMP_TICKS, 0, true,
                                Rotation.NONE, false, 1, 1, true, 0))));
    }

    // ------------------------------------------------------------------------------------------------ plot helpers
    private static void forceChunks(GameTestHelper h, int x0, int z0, int x1, int z1) {
        BlockPos a = h.absolutePos(new BlockPos(x0, Y, z0));
        BlockPos b = h.absolutePos(new BlockPos(x1, Y, z1));
        for (int cx = (Math.min(a.getX(), b.getX()) >> 4) - 1; cx <= (Math.max(a.getX(), b.getX()) >> 4) + 1; cx++) {
            for (int cz = (Math.min(a.getZ(), b.getZ()) >> 4) - 1; cz <= (Math.max(a.getZ(), b.getZ()) >> 4) + 1; cz++) {
                h.getLevel().setChunkForced(cx, cz, true);
            }
        }
    }

    private static void floor(GameTestHelper h, int x0, int z0, int x1, int z1) {
        forceChunks(h, x0, z0, x1, z1);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                h.setBlock(new BlockPos(x, Y, z), Blocks.STONE);
            }
        }
    }

    /** Builds the plot's floor and runs {@code body} once every chunk of the plot (and its ring) ticks entities. */
    private static void plot(GameTestHelper h, int x0, int z0, int x1, int z1, Runnable body) {
        floor(h, x0, z0, x1, z1);
        ServerLevel level = h.getLevel();
        BlockPos a = h.absolutePos(new BlockPos(x0, Y, z0));
        BlockPos b = h.absolutePos(new BlockPos(x1, Y, z1));
        java.util.function.BooleanSupplier ticking = () -> {
            for (int cx = Math.min(a.getX(), b.getX()) >> 4; cx <= Math.max(a.getX(), b.getX()) >> 4; cx++) {
                for (int cz = Math.min(a.getZ(), b.getZ()) >> 4; cz <= Math.max(a.getZ(), b.getZ()) >> 4; cz++) {
                    if (!level.isPositionEntityTicking(new BlockPos(cx << 4, a.getY(), cz << 4))) {
                        return false;
                    }
                }
            }
            return true;
        };
        if (ticking.getAsBoolean()) {
            body.run();
            return;
        }
        waitUntil(h, 400, t -> { }, ticking, () -> {
            h.assertTrue(ticking.getAsBoolean(), "the plot ticks entities");
            body.run();
        });
    }

    private static final class Waiter {
        final int max;
        final Consumer<Integer> each;
        final java.util.function.BooleanSupplier stop;
        final Runnable done;
        int tick;

        Waiter(int max, Consumer<Integer> each, java.util.function.BooleanSupplier stop, Runnable done) {
            this.max = max;
            this.each = each;
            this.stop = stop;
            this.done = done;
        }
    }

    private static final Map<GameTestHelper, List<Waiter>> WAITERS = new java.util.IdentityHashMap<>();

    /** One scheduled runnable per tick, registered up front, drives every wait of the test (scheduling later crashes the server). */
    private static void pump(GameTestHelper h) {
        List<Waiter> list = new ArrayList<>();
        WAITERS.put(h, list);
        for (int t = 1; t <= PUMP_TICKS - 5; t++) {
            h.runAfterDelay(t, () -> {
                for (Waiter w : new ArrayList<>(list)) {
                    if (!list.contains(w)) {
                        continue;
                    }
                    w.tick++;
                    w.each.accept(w.tick);
                    if (w.stop.getAsBoolean() || w.tick >= w.max) {
                        list.remove(w);
                        w.done.run();
                    }
                }
            });
        }
    }

    private static void waitUntil(GameTestHelper h, int max, Consumer<Integer> each, java.util.function.BooleanSupplier stop, Runnable done) {
        WAITERS.get(h).add(new Waiter(max, each, stop, done));
    }

    private static void later(GameTestHelper h, int ticks, Runnable task) {
        waitUntil(h, ticks, t -> { }, () -> false, task);
    }

    private static PuppetEntity puppet(GameTestHelper h, PuppetKind kind, double x, double z) {
        PuppetEntity p = h.spawn(ModEntities.puppet(kind), new BlockPos((int) x, Y + 1, (int) z));
        p.setPos(h.absoluteVec(new Vec3(x + 0.5D, Y + 1, z + 0.5D)));
        p.setOnGround(true);
        return p;
    }

    private static MatriarchWitchEntity witch(GameTestHelper h, double x, double z) {
        MatriarchWitchEntity w = h.spawn(ModEntities.TEN_THREADS_WITCH.get(), new BlockPos((int) x, Y + 1, (int) z));
        w.setPos(h.absoluteVec(new Vec3(x + 0.5D, Y + 1, z + 0.5D)));
        w.setOnGround(true);
        return w;
    }

    private static IronGolem golem(GameTestHelper h, double x, double y, double z) {
        IronGolem g = h.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos((int) x, (int) y, (int) z));
        g.setPos(h.absoluteVec(new Vec3(x + 0.5D, y, z + 0.5D)));
        g.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0D);
        g.setHealth(400.0F);
        return g;
    }

    private static float lost(LivingEntity e) {
        return e.getMaxHealth() - e.getHealth();
    }

    private static void lockOffense(PuppetEntity p) {
        for (PuppetAttack a : p.getKind().attacks) {
            p.getRunner().setCooldown(a, 100000);
        }
    }

    private static List<PuppetEntity> awake(ServerLevel level, MatriarchWitchEntity w) {
        List<PuppetEntity> out = new ArrayList<>();
        for (PuppetEntity p : w.getPuppets(level)) {
            if (p.getPuppetState() != PuppetState.SLEEPING_SUSPENDED && p.getPuppetState() != PuppetState.DYING) {
                out.add(p);
            }
        }
        return out;
    }

    private static JsonObject lang(String name) {
        try (var in = TenThreadsWitchGameTests.class.getResourceAsStream("/assets/codenamecoraline/lang/" + name + ".json")) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("lang " + name, e);
        }
    }

    private static boolean resource(String path) {
        return TenThreadsWitchGameTests.class.getResourceAsStream("/" + path) != null;
    }

    private static List<String> modelIds() {
        List<String> ids = new ArrayList<>();
        ids.add("ten_threads_witch");
        for (PuppetKind k : PuppetKind.values()) {
            ids.add(k.id);
        }
        return ids;
    }

    // ------------------------------------------------------------------------------------------------ registration and content
    private static void registrationAndContent(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            int i = 0;
            for (PuppetKind k : PuppetKind.values()) {
                PuppetEntity p = puppet(helper, k, 1 + (i % 5) * 3, 1 + (i / 5) * 8);
                p.setNoAi(true);
                i++;
                helper.assertValueEqual(p.getKind(), k, k.id + " kind");
                helper.assertValueEqual(p.getMaxHealth(), (float) k.health, k.id + " health");
                helper.assertValueEqual(p.getAttributeValue(Attributes.ARMOR), k.armor, k.id + " armour");
                helper.assertValueEqual(p.getAttributeValue(Attributes.MOVEMENT_SPEED), k.speed, k.id + " speed");
                helper.assertValueEqual(p.getAttributeValue(Attributes.ATTACK_DAMAGE), k.damage, k.id + " damage");
                helper.assertValueEqual(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), Math.min(1.0D, k.knockbackResistance), k.id + " knockback resistance");
                helper.assertTrue(Math.abs(p.getBbWidth() - k.width) < 1.0E-4F, k.id + " width " + p.getBbWidth());
                helper.assertTrue(Math.abs(p.getBbHeight() - k.height) < 1.0E-4F, k.id + " height " + p.getBbHeight());
                helper.assertTrue(Math.abs(p.getEyeHeight() - k.eyeHeight) < 1.0E-4F, k.id + " eye height " + p.getEyeHeight());
                helper.assertValueEqual(p.isNoGravity(), k.flies, k.id + " flight");
                helper.assertValueEqual(k.stage(), k.ordinal() + 1, k.id + " stage");
                helper.assertTrue(!p.removeWhenFarAway(1000.0D), k.id + " never despawns by distance");
                helper.assertTrue(!p.causeFallDamage(30.0D, 1.0F, level.damageSources().fall()), k.id + " ignores falls");
                helper.assertValueEqual(k.attacks.size(), 3, k.id + " has three attacks");
                helper.assertTrue(k.special().special, k.id + " ends its pattern with a special attack");
                helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(id("entity." + k.id + ".action")), k.id + " action sound");
                p.discard();
            }
            MatriarchWitchEntity w = witch(helper, 9, 9);
            helper.assertValueEqual(w.getMaxHealth(), 150.0F, "the Matriarch has 150 HP");
            helper.assertValueEqual(w.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.28D, "speed 0.28");
            helper.assertValueEqual(w.getAttributeValue(Attributes.ATTACK_DAMAGE), 10.0D, "damage 10");
            helper.assertValueEqual(w.getBbWidth(), 0.6F, "0.6 wide");
            helper.assertValueEqual(w.getBbHeight(), 1.15F, "1.15 tall");
            helper.assertTrue(Math.abs(w.getEyeHeight() - 0.95F) < 1.0E-4F, "eyes at 0.95");
            helper.assertTrue(!w.canFreeze() && !w.removeWhenFarAway(1000.0D), "the Matriarch never freezes or despawns");
            helper.assertValueEqual(w.getPhase(), EncounterPhase.DORMANT, "dormant until woken");
            for (EntityType<?> type : List.of(ModEntities.TEN_THREADS_WITCH.get(), ModEntities.puppet(PuppetKind.PORCELAIN_HOUND),
                    ModEntities.puppet(PuppetKind.HOLLOW_BRIDE))) {
                helper.assertTrue(type.builtInRegistryHolder().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES), "freeze immune: " + type);
                helper.assertTrue(type.builtInRegistryHolder().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS), "powder snow walker: " + type);
            }
            w.discard();
            // items, eggs and the staff
            ItemStack egg = new ItemStack(ModItems.TEN_THREADS_WITCH_SPAWN_EGG.get());
            helper.assertValueEqual(((SpawnEggItem) egg.getItem()).getType(egg), ModEntities.TEN_THREADS_WITCH.get(), "the egg spawns the Matriarch");
            ItemStack staff = new ItemStack(ModItems.TEN_THREADS_STAFF.get());
            helper.assertValueEqual(staff.getMaxDamage(), 1561, "the staff lasts 1561 uses");
            helper.assertValueEqual(staff.getRarity(), Rarity.EPIC, "epic rarity");
            double damage = staff.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(Attributes.ATTACK_DAMAGE, 1.0D, EquipmentSlot.MAINHAND);
            helper.assertValueEqual(damage, 7.0D, "the staff strikes for 7");
            helper.assertTrue(new ItemStack(ModItems.PUPPET_REMNANT.get()).is(ModItems.PUPPET_REMNANTS), "the remnant is in its tag");
            helper.assertTrue(staff.isValidRepairItem(new ItemStack(ModItems.PUPPET_REMNANT.get())), "the staff is repaired with remnants");
            helper.assertTrue(ModItems.CURSED_SPINDLE.get() != null && ModItems.MATRIARCH_THIMBLE.get() != null
                    && ModItems.TEN_THREADS_WITCH_PORTRAIT.get() != null, "items registered");
            for (var sound : List.of(ModSounds.WITCH_AMBIENT, ModSounds.WITCH_HURT, ModSounds.WITCH_DEATH, ModSounds.WITCH_THREAD_TENSION,
                    ModSounds.WITCH_THREAD_SNAP, ModSounds.WITCH_VOICE, ModSounds.PUPPET_HURT, ModSounds.PUPPET_DEATH, ModSounds.PUPPET_AWAKEN,
                    ModSounds.PUPPET_TANGLED, ModSounds.PUPPET_CRASH, ModSounds.PUPPET_CRACK)) {
                helper.assertTrue(sound.get() != null, "sound " + sound.getId());
            }
            content(helper, level);
        });
    }

    private static void content(GameTestHelper helper, ServerLevel level) {
        // Bestiary: a chapter of eight pages with fixed plate sizes and the engravings on disk
        helper.assertTrue(Bestiary.ENTRIES.contains(Bestiary.TEN_THREADS_WITCH), "the Bestiary has her chapter");
        List<BestiaryPage> pages = Bestiary.TEN_THREADS_WITCH.pages();
        helper.assertValueEqual(pages.size(), 8, "eight pages");
        helper.assertValueEqual(pages.get(1).drawingWidth() * 1000 + pages.get(1).drawingHeight(), 112 * 1000 + 80, "anatomy 112x80");
        helper.assertValueEqual(pages.get(2).drawingWidth() * 1000 + pages.get(2).drawingHeight(), 112 * 1000 + 72, "combat 112x72");
        helper.assertValueEqual(pages.get(7).drawingWidth() * 1000 + pages.get(7).drawingHeight(), 112 * 1000 + 64, "spoils 112x64");
        for (String art : new String[] {"ten_threads_witch_anatomy", "ten_threads_witch_combat", "ten_threads_witch_spoils"}) {
            helper.assertTrue(resource("assets/codenamecoraline/textures/gui/bestiary/" + art + ".png"), "engraving " + art);
        }
        String[] advancements = {"craft_cursed_spindle", "summon_ten_threads_witch", "sever_crimson_thread", "defeat_ten_threads_witch", "obtain_ten_threads_staff"};
        for (String adv : advancements) {
            helper.assertTrue(level.getServer().getAdvancements().get(id("bosses/" + adv)) != null, "advancement bosses/" + adv);
        }
        helper.assertTrue(level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id("cursed_spindle"))).isPresent(), "the spindle recipe is loaded");
        var loot = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id("entities/ten_threads_witch")));
        helper.assertTrue(loot != net.minecraft.world.level.storage.loot.LootTable.EMPTY, "the loot table is loaded");
        for (String id : modelIds()) {
            for (String res : new String[] {"textures/entity/" + id + ".png", "textures/entity/" + id + "_eyes.png"}) {
                helper.assertTrue(resource("assets/codenamecoraline/" + res), "asset " + res);
            }
        }
        for (String res : new String[] {"textures/gui/boss_bar/ten_threads_witch.png", "textures/item/cursed_spindle.png", "textures/item/puppet_remnant.png",
                "textures/item/matriarch_thimble.png", "textures/item/ten_threads_staff.png", "textures/item/ten_threads_staff_3d.png",
                "textures/item/ten_threads_witch_portrait.png", "textures/item/ten_threads_witch_spawn_egg.png",
                "models/item/ten_threads_staff_3d.json", "items/ten_threads_staff.json"}) {
            helper.assertTrue(resource("assets/codenamecoraline/" + res), "asset " + res);
        }
        helper.assertTrue(resource("data/codenamecoraline/loot_table/entities/ten_threads_witch.json"), "loot table file");
        // text: every key in both languages
        List<String> keys = new ArrayList<>(List.of("entity.codenamecoraline.ten_threads_witch", "entity.codenamecoraline.thread_hitbox",
                "entity.codenamecoraline.spool_vital_orb", "bossbar.codenamecoraline.ten_threads_witch.stage", "bossbar.codenamecoraline.ten_threads_witch.fury",
                "item.codenamecoraline.cursed_spindle", "item.codenamecoraline.puppet_remnant", "item.codenamecoraline.matriarch_thimble",
                "item.codenamecoraline.ten_threads_staff", "item.codenamecoraline.ten_threads_witch_portrait", "item.codenamecoraline.ten_threads_witch_spawn_egg",
                "item.codenamecoraline.ten_threads_staff.lore1", "item.codenamecoraline.ten_threads_staff.lore2", "item.codenamecoraline.ten_threads_staff.no_anchor",
                "bestiary.codenamecoraline.ten_threads_witch.name"));
        for (CursedSpindleItem.SummonResult r : CursedSpindleItem.SummonResult.values()) {
            keys.add(r.translationKey());
        }
        for (PuppetKind k : PuppetKind.values()) {
            keys.add(k.nameKey());
        }
        for (int p = 0; p < TenThreadsStaffItem.POSTURES; p++) {
            keys.add(TenThreadsStaffItem.postureKey(p));
        }
        for (WitchDialogueManager.Line line : WitchDialogueManager.Line.values()) {
            keys.add(line.translationKey());
        }
        for (BestiaryPage page : pages) {
            keys.add(Bestiary.TEN_THREADS_WITCH.titleKey(page));
            keys.add(Bestiary.TEN_THREADS_WITCH.bodyKey(page));
            if (page.drawing() != null) {
                keys.add(Bestiary.TEN_THREADS_WITCH.captionKey(page));
            }
        }
        for (String adv : advancements) {
            keys.add("advancements.codenamecoraline.bosses." + adv + ".title");
            keys.add("advancements.codenamecoraline.bosses." + adv + ".description");
        }
        JsonObject sounds = JsonParser.parseReader(new InputStreamReader(
                TenThreadsWitchGameTests.class.getResourceAsStream("/assets/codenamecoraline/sounds.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        int soundDefs = 0;
        for (Map.Entry<String, JsonElement> e : sounds.entrySet()) {
            boolean ours = e.getKey().startsWith("entity.ten_threads_") || PuppetKind.byId(e.getKey().replace("entity.", "").replace(".action", "")) != null;
            if (!ours) {
                continue;
            }
            soundDefs++;
            keys.add(e.getValue().getAsJsonObject().get("subtitle").getAsString());
            for (JsonElement s : e.getValue().getAsJsonObject().getAsJsonArray("sounds")) {
                JsonObject o = s.getAsJsonObject();
                helper.assertValueEqual(o.get("type").getAsString(), "event", e.getKey() + " uses vanilla events only (no audio files)");
                helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(Identifier.parse(o.get("name").getAsString())), e.getKey() + " event " + o.get("name"));
            }
        }
        helper.assertValueEqual(soundDefs, 6 + 6 + 10, "22 sound definitions (6 witch, 6 puppet, 10 actions)");
        for (String langName : new String[] {"en_us", "pt_br"}) {
            JsonObject json = lang(langName);
            for (String key : keys) {
                helper.assertTrue(json.has(key), langName + " has " + key);
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------ models
    private record Pose(int faces, List<float[]> vertices) {}

    private static Pose pose(String id, PuppetAnim anim, float seconds, int broken, boolean fury) {
        var model = new PuppetModel(TenThreadsParts.layer(id).bakeRoot(), id);
        var state = new PuppetRenderState();
        state.anim = anim;
        state.lookAtTarget = false;
        state.brokenSpools = broken;
        state.fury = fury;
        state.ageInTicks = 100.0F + seconds * 20.0F;
        state.animState.start(100);
        model.setupAnim(state);
        List<float[]> vertices = new ArrayList<>();
        int[] faces = {0};
        var lookup = model.root().createPartLookup();
        model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
            for (String name : part.split("/")) {
                var p = name.isEmpty() ? null : lookup.apply(name);
                if (p != null && !p.visible) {
                    return;
                }
            }
            for (var polygon : cube.polygons) {
                faces[0]++;
                for (var vertex : polygon.vertices()) {
                    var v = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new org.joml.Vector3f());
                    vertices.add(new float[] {v.x(), v.y(), v.z()});
                }
            }
        });
        return new Pose(faces[0], vertices);
    }

    private static double distance(Pose a, Pose b) {
        double sum = 0.0D;
        int n = Math.min(a.vertices().size(), b.vertices().size());
        for (int i = 0; i < n; i++) {
            float[] u = a.vertices().get(i);
            float[] v = b.vertices().get(i);
            sum += Math.abs(u[0] - v[0]) + Math.abs(u[1] - v[1]) + Math.abs(u[2] - v[2]);
        }
        return sum;
    }

    private static void modelsUvAndAnimations(GameTestHelper helper) {
        for (String id : modelIds()) {
            var root = TenThreadsParts.layer(id).bakeRoot();
            helper.assertTrue(TenThreadsParts.partNames(id).contains("body"), id + " has a body");
            // UV layout: no two cubes share atlas pixels
            List<float[]> rects = new ArrayList<>();
            List<String> owner = new ArrayList<>();
            root.visit(new PoseStack(), (pose, part, index, cube) -> {
                for (var polygon : cube.polygons) {
                    float minU = 9, minV = 9, maxU = -1, maxV = -1;
                    for (var vertex : polygon.vertices()) {
                        minU = Math.min(minU, vertex.u());
                        maxU = Math.max(maxU, vertex.u());
                        minV = Math.min(minV, vertex.v());
                        maxV = Math.max(maxV, vertex.v());
                    }
                    rects.add(new float[] {minU * 256, minV * 256, maxU * 256, maxV * 256});
                    owner.add(part + "#" + index);
                }
            });
            for (int i = 0; i < rects.size(); i++) {
                for (int j = i + 1; j < rects.size(); j++) {
                    if (owner.get(i).equals(owner.get(j))) {
                        continue;
                    }
                    float[] a = rects.get(i);
                    float[] b = rects.get(j);
                    boolean overlap = a[0] < b[2] - 0.01F && b[0] < a[2] - 0.01F && a[1] < b[3] - 0.01F && b[1] < a[3] - 0.01F;
                    helper.assertTrue(!overlap, id + ": UV overlap between " + owner.get(i) + " and " + owner.get(j));
                }
            }
            // every animation exists, has its length and moves the model; no pose ever produces a broken vertex
            Pose idle = pose(id, PuppetAnim.IDLE, 0.6F, 0, false);
            for (PuppetAnim a : PuppetAnim.values()) {
                var def = PuppetAnimations.get(id, a);
                helper.assertTrue(def != null, id + "/" + a + " has keyframes");
                helper.assertTrue(Math.abs(def.lengthInSeconds() - PuppetAnimations.length(a)) < 1.0E-3F, id + "/" + a + " length");
                Pose p = pose(id, a, PuppetAnimations.length(a) * 0.5F, 0, false);
                helper.assertTrue(p.faces() > 0, id + "/" + a + " draws faces");
                for (float[] v : p.vertices()) {
                    helper.assertTrue(Float.isFinite(v[0]) && Float.isFinite(v[1]) && Float.isFinite(v[2]), id + "/" + a + " has finite vertices");
                }
                if (a == PuppetAnim.ATTACK_A || a == PuppetAnim.ATTACK_B || a == PuppetAnim.ATTACK_C || a == PuppetAnim.WINDUP_CRIMSON || a == PuppetAnim.DEATH) {
                    helper.assertTrue(distance(idle, p) > 0.5D, id + "/" + a + " differs from the idle pose (" + distance(idle, p) + ")");
                }
            }
        }
        // the staff wheel: ten spools, and each defeated puppet bursts its own
        long spools = TenThreadsParts.partNames("ten_threads_witch").stream().filter(n -> n.startsWith("spool_")).count();
        helper.assertValueEqual(spools, 10L, "ten spools on the wheel");
        int whole = pose("ten_threads_witch", PuppetAnim.IDLE, 0.0F, 0, false).faces();
        int one = pose("ten_threads_witch", PuppetAnim.IDLE, 0.0F, 1, false).faces();
        int all = pose("ten_threads_witch", PuppetAnim.IDLE, 0.0F, 0x3FF, false).faces();
        helper.assertTrue(one < whole && all < one, "spools disappear from the wheel as they burst (" + whole + " > " + one + " > " + all + ")");
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------ the Matriarch
    private static void matriarchInvulnerable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            MatriarchWitchEntity w = witch(helper, 9, 9);
            w.beginDirect(level, 1);
            helper.assertValueEqual(w.getPhase(), EncounterPhase.STAGE_01, "stage 1");
            helper.assertValueEqual(w.getCurrentStage(), 1, "stage number 1");
            helper.assertTrue(w.getBossEvent().isVisible(), "the bar is shown");
            helper.assertValueEqual(w.getPuppets(level).size(), 10, "ten puppets hang over the arena");
            later(helper, 3, () -> {
                float hp = w.getHealth();
                helper.assertTrue(!w.hurtServer(level, level.damageSources().generic(), 50.0F), "no blow reaches her while a puppet holds");
                helper.assertTrue(!w.hurtServer(level, level.damageSources().magic(), 50.0F), "no spell either");
                helper.assertValueEqual(w.getHealth(), hp, "she is unhurt");
                for (PuppetEntity p : w.getPuppets(level)) {
                    if (p.getKind().stage() != 1) {
                        helper.assertValueEqual(p.getPuppetState(), PuppetState.SLEEPING_SUSPENDED, p.getKind().id + " still hangs");
                        helper.assertTrue(!p.hurtServer(level, level.damageSources().generic(), 50.0F), p.getKind().id + " cannot be hurt while it hangs");
                    }
                }
                w.beginDirect(level, 11);
                helper.assertValueEqual(w.getPhase(), EncounterPhase.STAGE_11_FURY, "stage 11");
                helper.assertValueEqual(w.getBrokenSpoolsBitmask(), 0x3FF, "all ten spools are burst");
                helper.assertValueEqual(w.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.40D, "fury speed 0.40");
                helper.assertTrue(w.hurtServer(level, level.damageSources().generic(), 10.0F), "now she can be hurt");
                helper.assertValueEqual(w.getHealth(), 140.0F, "and takes the blow in full (150 HP, no armour)");
                for (PuppetEntity p : w.getPuppets(level)) {
                    p.discard();
                }
                w.discard();
                helper.succeed();
            });
        });
    }

    private static void killActive(ServerLevel level, MatriarchWitchEntity w) {
        PuppetEntity p = w.getActivePuppet(level);
        p.hurtServer(level, level.damageSources().genericKill(), 100000.0F);
    }

    /** Waits for the puppet of {@code stage} to be fighting, checks the invariants, kills it and goes on until {@code last}. */
    private static void fightThrough(GameTestHelper h, ServerLevel level, MatriarchWitchEntity w, int stage, int last, List<Integer> seen, Runnable done) {
        waitUntil(h, 160, t -> {
            if (seen.isEmpty() || seen.getLast() != w.getCurrentStage()) {
                seen.add(w.getCurrentStage());
            }
            h.assertTrue(awake(level, w).size() <= 1, "never two puppets awake at once (" + awake(level, w).size() + ")");
        }, () -> {
            PuppetEntity p = w.getActivePuppet(level);
            return w.getCurrentStage() == stage && p != null && p.getPuppetState() == PuppetState.IDLE_COMBAT;
        }, () -> {
            PuppetEntity p = w.getActivePuppet(level);
            h.assertTrue(w.getCurrentStage() == stage && p != null && p.getPuppetState() == PuppetState.IDLE_COMBAT, "stage " + stage + " puppet fights");
            h.assertValueEqual(p.getKind(), PuppetKind.forStage(stage), "stage " + stage + " fights its own puppet");
            h.assertValueEqual(w.getBrokenSpoolsBitmask(), (1 << (stage - 1)) - 1, "stage " + stage + " spools");
            h.assertTrue(!w.hurtServer(level, level.damageSources().generic(), 10.0F), "stage " + stage + ": the Matriarch is untouchable");
            h.assertTrue(w.getBossEvent().getName().getString().isEmpty() || w.getBossEvent().getProgress() > 0.99F, "the bar shows the puppet's full health");
            AABB around = p.getBoundingBox().inflate(6.0D);
            killActive(level, w);
            h.assertValueEqual(w.getBrokenSpoolsBitmask(), (1 << stage) - 1, "stage " + stage + " spool bursts at once");
            h.assertTrue(level.getEntitiesOfClass(SpoolVitalOrbEntity.class, around).size() >= 2, "two Orbs of Vital Spool");
            h.assertTrue(level.getEntitiesOfClass(ItemEntity.class, around, e -> e.getItem().is(ModItems.PUPPET_REMNANT.get())).size() >= 1, "a puppet remnant");
            if (stage < last) {
                fightThrough(h, level, w, stage + 1, last, seen, done);
            } else {
                done.run();
            }
        });
    }

    private static void sequenceFirstStages(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            MatriarchWitchEntity w = witch(helper, 9, 9);
            w.beginDirect(level, 1);
            List<Integer> seen = new ArrayList<>();
            fightThrough(helper, level, w, 1, 3, seen, () -> {
                helper.assertValueEqual(w.getBrokenSpoolsBitmask(), 0b111, "three spools burst");
                helper.assertTrue(seen.equals(List.of(1, 2, 3)), "the stages came in order, none skipped: " + seen);
                helper.assertTrue(w.getPhase() == EncounterPhase.STAGE_03, "still in stage 3");
                for (PuppetEntity p : w.getPuppets(level)) {
                    p.discard();
                }
                w.discard();
                helper.succeed();
            });
        });
    }

    private static void sequenceToDeath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            MatriarchWitchEntity w = witch(helper, 9, 9);
            w.beginDirect(level, 9);
            List<Integer> seen = new ArrayList<>();
            fightThrough(helper, level, w, 9, 10, seen, () -> {
                helper.assertTrue(seen.equals(List.of(9, 10)), "stages 9 and 10 in order: " + seen);
                waitUntil(helper, 90, t -> { }, () -> w.getPhase() == EncounterPhase.STAGE_11_TRANSITION, () -> {
                    helper.assertValueEqual(w.getPhase(), EncounterPhase.STAGE_11_TRANSITION, "the tenth puppet's fall collapses the Matriarch");
                    helper.assertTrue(!w.hurtServer(level, level.damageSources().generic(), 10.0F), "she cannot be hurt while she collapses");
                    waitUntil(helper, MatriarchWitchEntity.COLLAPSE_TICKS + 30, t -> { }, () -> w.getPhase() == EncounterPhase.STAGE_11_FURY, () -> {
                        helper.assertValueEqual(w.getPhase(), EncounterPhase.STAGE_11_FURY, "then she rises for the last phase");
                        helper.assertValueEqual(w.getHealth(), 150.0F, "with 150 HP");
                        helper.assertValueEqual(w.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.40D, "and speed 0.40");
                        helper.assertTrue(w.hurtServer(level, level.damageSources().generic(), 10.0F), "and can be hurt");
                        helper.assertTrue(w.getBossEvent().getColor() == net.minecraft.world.BossEvent.BossBarColor.RED, "the bar turns red");
                        // death: once, with one casket
                        w.hurtServer(level, level.damageSources().genericKill(), 100000.0F);
                        w.die(level.damageSources().generic());
                        helper.assertValueEqual(w.getDeathAwardCount(), 1, "the death is counted once");
                        BlockPos where = w.blockPosition();
                        waitUntil(helper, MatriarchWitchEntity.DEATH_TICKS + 30, t -> { }, w::isRemoved, () -> {
                            helper.assertTrue(w.isRemoved(), "she is gone after the death cinematic");
                            helper.assertValueEqual(w.getCasketCount(), 1, "exactly one casket");
                            ChestBlockEntity chest = null;
                            for (BlockPos p : BlockPos.betweenClosed(where.offset(-4, -3, -4), where.offset(4, 3, 4))) {
                                if (level.getBlockEntity(p) instanceof ChestBlockEntity c) {
                                    chest = c;
                                }
                            }
                            helper.assertTrue(chest != null, "the Seamstress Casket stands where she fell");
                            int staffs = 0;
                            int thimbles = 0;
                            for (int i = 0; i < chest.getContainerSize(); i++) {
                                ItemStack s = chest.getItem(i);
                                if (s.is(ModItems.TEN_THREADS_STAFF.get())) staffs += s.getCount();
                                if (s.is(ModItems.MATRIARCH_THIMBLE.get())) thimbles += s.getCount();
                            }
                            helper.assertValueEqual(staffs, 1, "the staff, always, once");
                            helper.assertTrue(thimbles >= 1 && thimbles <= 2, "one or two thimbles (" + thimbles + ")");
                            helper.assertValueEqual(level.getEntitiesOfClass(PuppetEntity.class, new AABB(where).inflate(40.0D), q -> q.isAlive() && w.getUUID().equals(q.getOwnerWitchUuid())).size(), 0, "no puppet of hers outlives her");
                            helper.assertValueEqual(level.getEntitiesOfClass(ItemEntity.class, new AABB(where).inflate(6.0D), e -> e.getItem().is(ModItems.TEN_THREADS_STAFF.get())).size(), 0, "no second staff on the floor");
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ the crimson thread
    private static void threadCut(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            PuppetEntity p = puppet(helper, PuppetKind.SEWING_ARACHNID, 9, 3);
            PuppetEntity twin = puppet(helper, PuppetKind.SEWING_ARACHNID, 3, 15);
            twin.setNoAi(true);
            IronGolem golem = golem(helper, 9, Y + 1, 11);
            Player cutter = helper.makeMockPlayer(GameType.SURVIVAL);
            lockOffense(p);
            p.setTarget(golem);
            p.getRunner().setCooldown(p.getKind().special(), 0);
            helper.assertTrue(!p.cutThread(level, cutter), "nothing to cut while the puppet is idle");
            helper.assertTrue(!p.isThreadCuttable(), "the thread is slack");
            p.getRunner().start(p.getKind().special(), golem);
            helper.assertValueEqual(p.getPuppetState(), PuppetState.WINDUP_CRIMSON, "the special attack winds up with the crimson thread");
            helper.assertTrue(p.isCrimsonThreadActive() && p.isThreadCuttable(), "the thread is taut and can be cut");
            StringBuilder log = new StringBuilder();
            waitUntil(helper, 3, t -> log.append(String.format("[t%d tick=%d hb=%s removed=%s state=%s] ", t, p.tickCount, p.getThreadHitbox() == null ? "null" : p.getThreadHitbox().position().subtract(p.position()), p.getThreadHitbox() != null && p.getThreadHitbox().isRemoved(), p.getPuppetState())), () -> false, () -> {
                List<ThreadHitboxEntity> boxes = level.getEntitiesOfClass(ThreadHitboxEntity.class, p.getBoundingBox().inflate(12.0D));
                if (boxes.isEmpty()) {
                    List<ThreadHitboxEntity> anywhere = level.getEntitiesOfClass(ThreadHitboxEntity.class, p.getBoundingBox().inflate(300.0D));
                    helper.fail("no hitbox near the puppet at " + log + p.position() + "; hitboxes anywhere: " + anywhere.size() + " "
                            + (anywhere.isEmpty() ? "" : anywhere.getFirst().position() + " removed " + anywhere.getFirst().isRemoved()) + "; puppet tick " + p.tickCount);
                    return;
                }
                helper.assertTrue(boxes.size() == 1 && boxes.getFirst().puppet() == p, "one thread hitbox, bound to the puppet (" + boxes.size() + " boxes, state " + p.getPuppetState() + ", busy " + p.getRunner().isBusy() + ", cuttable " + p.isThreadCuttable() + ")");
                helper.assertTrue(!boxes.getFirst().hurtServer(level, level.damageSources().mobAttack(golem), 3.0F), "only players and arrows cut it");
                float before = p.getHealth();
                helper.assertTrue(boxes.getFirst().hurtServer(level, level.damageSources().playerAttack(cutter), 3.0F), "a player's blow cuts it");
                helper.assertValueEqual(p.getThreadsCut(), 1, "one thread cut");
                helper.assertTrue(Math.abs((before - p.getHealth()) - 0.15F * p.getMaxHealth()) < 0.05F, "it costs 15% of its health (" + (before - p.getHealth()) + ")");
                helper.assertValueEqual(p.getPuppetState(), PuppetState.TANGLED_STUNNED, "the puppet is TANGLED");
                helper.assertTrue(p.isTangled() && !p.getRunner().isBusy(), "tangled and the attack is cancelled");
                helper.assertTrue(!p.isCrimsonThreadActive(), "the thread is spent");
                helper.assertTrue(!p.cutThread(level, cutter), "a thread cannot be cut twice");
                helper.assertValueEqual(p.getThreadsCut(), 1, "still one");
                // x1.75 while tangled, against the same blow on a puppet that is not
                p.invulnerableTime = 0;
                twin.invulnerableTime = 0;
                float tBefore = p.getHealth();
                float nBefore = twin.getHealth();
                DamageSource magic = level.damageSources().magic();
                helper.assertTrue(p.hurtServer(level, magic, 4.0F) && twin.hurtServer(level, magic, 4.0F), "both take the blow");
                float tangledLoss = tBefore - p.getHealth();
                float plainLoss = nBefore - twin.getHealth();
                helper.assertTrue(Math.abs(plainLoss - 4.0F) < 0.01F, "an ordinary puppet loses 4 (" + plainLoss + ")");
                helper.assertTrue(Math.abs(tangledLoss - 7.0F) < 0.01F, "a tangled one loses 4 x 1.75 = 7 (" + tangledLoss + ")");
                int[] ticks = {0};
                waitUntil(helper, 120, t -> ticks[0] = t, () -> p.getPuppetState() != PuppetState.TANGLED_STUNNED, () -> {
                    helper.assertTrue(ticks[0] >= 60 && ticks[0] <= 80, "tangled for about 3.5 s (" + ticks[0] + " ticks)");
                    helper.assertValueEqual(p.getPuppetState(), PuppetState.IDLE_COMBAT, "then it fights again");
                    helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "the cut attack never landed");
                    p.discard();
                    twin.discard();
                    golem.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void missedSpecialTangles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            PuppetEntity p = puppet(helper, PuppetKind.SEWING_ARACHNID, 9, 2);
            IronGolem golem = golem(helper, 9, Y + 1, 9);
            lockOffense(p);
            p.setTarget(golem);
            p.getRunner().start(p.getKind().special(), golem);
            later(helper, 21, () -> golem.setPos(helper.absoluteVec(new Vec3(16.5D, Y + 1, 16.5D)))); // sidestepped after the aim locked (tick 18)
            waitUntil(helper, 120, t -> { }, () -> p.getTangles() > 0, () -> {
                helper.assertTrue(p.getTangles() > 0, "the leap landed on nothing: the puppet is tangled");
                helper.assertTrue(p.getRunner().swings() >= 1, "the special attack does fire at its locked line");
                helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "nobody was hit");
                p.discard();
                golem.discard();
                helper.succeed();
            });
        });
    }

    private static void houndCracks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            PuppetEntity plain = puppet(helper, PuppetKind.PORCELAIN_HOUND, 2, 2);
            plain.setNoAi(true);
            helper.assertValueEqual(plain.getAttributeValue(Attributes.ARMOR), 10.0D, "armour 10 before");
            helper.assertTrue(!plain.isCracked(), "whole");
            plain.crack();
            helper.assertValueEqual(plain.getAttributeValue(Attributes.ARMOR), 0.0D, "armour 0 once cracked");
            helper.assertTrue(plain.isCracked(), "cracked");
            plain.crack();
            helper.assertValueEqual(plain.getAttributeValue(Attributes.ARMOR), 0.0D, "cracking twice changes nothing");
            plain.discard();
            // a real charge into a wall
            for (int x = 5; x <= 13; x++) {
                for (int y = Y + 1; y <= Y + 4; y++) {
                    helper.setBlock(new BlockPos(x, y, 8), Blocks.STONE);
                }
            }
            PuppetEntity hound = puppet(helper, PuppetKind.PORCELAIN_HOUND, 9, 3);
            IronGolem golem = golem(helper, 9, Y + 1, 12);
            lockOffense(hound);
            hound.setTarget(golem);
            hound.getRunner().start(hound.getKind().special(), golem);
            waitUntil(helper, 120, t -> { }, hound::isCracked, () -> {
                helper.assertTrue(hound.isCracked(), "the lunge against the wall cracks the porcelain");
                helper.assertValueEqual(hound.getAttributeValue(Attributes.ARMOR), 0.0D, "armour 10 to 0");
                helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "the wall kept the golem safe");
                waitUntil(helper, 40, t -> { }, () -> hound.getTangles() > 0, () -> {
                    helper.assertTrue(hound.getTangles() > 0, "the crash leaves it tangled");
                    hound.discard();
                    golem.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void weakPoints(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            IronGolem low = golem(helper, 3, Y + 1, 3);
            IronGolem high = golem(helper, 3, Y + 5, 15);
            Arrow arrow = new Arrow(EntityType.ARROW, level);
            Arrow arrowLow = new Arrow(EntityType.ARROW, level);
            DamageSource melee = level.damageSources().mobAttack(low);
            DamageSource melee2 = level.damageSources().mobAttack(high);
            DamageSource fromAbove = level.damageSources().arrow(arrow, high);
            DamageSource fromBelow = level.damageSources().arrow(arrowLow, low);
            // the Hollow Bell: arrows only hurt the clapper hanging below; melee at doll height is not a clapper hit
            PuppetEntity bell = puppet(helper, PuppetKind.HOLLOW_BELL, 9, 9);
            bell.setNoAi(true);
            bell.setPos(bell.getX(), bell.getY(), bell.getZ());
            high.setPos(bell.getX(), bell.getY() + 2.0D, bell.getZ() + 4.0D);
            low.setPos(bell.getX(), bell.getY(), bell.getZ() + 4.0D);
            arrow.setPos(high.position()); // an arrow's position is where it hits
            arrowLow.setPos(low.position());
            helper.assertValueEqual(bell.damageMultiplier(fromAbove), 0.0F, "an arrow from above bounces off the bell");
            helper.assertValueEqual(bell.damageMultiplier(fromBelow), 2.0F, "an arrow at the clapper does double");
            helper.assertValueEqual(bell.damageMultiplier(melee), 2.0F, "a blow at the clapper does double");
            helper.assertValueEqual(bell.damageMultiplier(melee2), 1.0F, "a blow at the doll is ordinary");
            bell.discard();
            // the Patchwork Executioner: the seam of the neck from above, 2.5x
            PuppetEntity exe = puppet(helper, PuppetKind.PATCHWORK_EXECUTIONER, 9, 9);
            exe.setNoAi(true);
            high.setPos(exe.getX(), exe.getY() + 3.0D, exe.getZ() + 4.0D);
            low.setPos(exe.getX(), exe.getY(), exe.getZ() + 4.0D);
            helper.assertValueEqual(exe.damageMultiplier(melee2), 2.5F, "the neck seam, from above, does 2.5x");
            helper.assertValueEqual(exe.damageMultiplier(melee), 1.0F, "elsewhere it is ordinary");
            exe.enterTangled(40);
            helper.assertValueEqual(exe.damageMultiplier(melee), 1.75F, "tangled: 1.75x");
            helper.assertTrue(Math.abs(exe.damageMultiplier(melee2) - 2.5F * 1.75F) < 1.0E-4F, "tangled and from above: both stack");
            exe.discard();
            // the Cradle Giant: the legs are armoured (0.5x), kneeling the cradle takes 2.5x
            PuppetEntity giant = puppet(helper, PuppetKind.CRADLE_GIANT, 9, 9);
            giant.setNoAi(true);
            high.setPos(giant.getX(), giant.getY() + 3.0D, giant.getZ() + 4.0D);
            low.setPos(giant.getX(), giant.getY() + 0.5D, giant.getZ() + 4.0D);
            helper.assertValueEqual(giant.damageMultiplier(melee), 0.5F, "blows at the legs are halved");
            helper.assertValueEqual(giant.damageMultiplier(melee2), 1.0F, "blows at the body are ordinary");
            giant.enterTangled(40);
            helper.assertValueEqual(giant.damageMultiplier(melee), 2.5F, "kneeling, the cradle takes 2.5x");
            giant.discard();
            low.discard();
            high.discard();
            arrow.discard();
            arrowLow.discard();
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ behaviour
    private static void neverSwingsAtAir(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            PuppetEntity stag = puppet(helper, PuppetKind.PATCHWORK_STAG, 9, 2);
            IronGolem golem = golem(helper, 9, Y + 1, 5);
            lockOffense(stag);
            PuppetAttack sweep = stag.getKind().attacks.getFirst();
            stag.setTarget(golem);
            stag.getRunner().start(sweep, golem);
            later(helper, 4, () -> golem.setPos(helper.absoluteVec(new Vec3(14.5D, Y + 1, 14.5D)))); // out of reach before the blow
            waitUntil(helper, 80, t -> { }, () -> !stag.getRunner().isBusy(), () -> {
                helper.assertValueEqual(stag.getRunner().swings(), 0, "it never swung");
                helper.assertValueEqual(stag.getRunner().cancelled(), 1, "the blow was called off at the commit");
                helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "nothing was hit");
                helper.assertValueEqual(stag.getPuppetState(), PuppetState.IDLE_COMBAT, "and it goes back to the hunt");
                // it chases: far from the golem, it closes in
                for (PuppetAttack a : stag.getKind().attacks) {
                    stag.getRunner().setCooldown(a, 100000);
                }
                stag.setPos(helper.absoluteVec(new Vec3(4.5D, Y + 1, 4.5D)));
                golem.setPos(helper.absoluteVec(new Vec3(13.5D, Y + 1, 13.5D)));
                double start = stag.distanceTo(golem);
                StringBuilder trace = new StringBuilder();
                waitUntil(helper, 90, t -> {
                    if (t % 10 == 0) {
                        trace.append(String.format("[t%d d=%.1f nav=%s on=%s hc=%s] ", t, stag.distanceTo(golem), stag.getNavigation().isDone(), stag.onGround(), stag.horizontalCollision));
                    }
                }, () -> false, () -> {
                    double end = stag.distanceTo(golem);
                    helper.assertTrue(end < start - 6.0D || end < 3.0D, "it chases a far target (" + start + " to " + end + ", state " + stag.getPuppetState() + ", nav done " + stag.getNavigation().isDone() + ", speed " + stag.getAttributeValue(Attributes.MOVEMENT_SPEED) + ", target " + stag.getTarget() + ", busy " + stag.getRunner().isBusy() + ") " + trace);
                    stag.discard();
                    golem.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void alliances(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            PuppetEntity a = puppet(helper, PuppetKind.SEWING_ARACHNID, 3, 3);
            PuppetEntity b = puppet(helper, PuppetKind.HOLLOW_BELL, 12, 12);
            a.setNoAi(true);
            b.setNoAi(true);
            MatriarchWitchEntity w = witch(helper, 9, 9);
            Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, Y + 1, 6));
            IronGolem golem = golem(helper, 15, Y + 1, 3);
            Player survival = helper.makeMockPlayer(GameType.SURVIVAL);
            Player spectator = helper.makeMockPlayer(GameType.SPECTATOR);
            helper.assertTrue(AttackRunner.isFoe(a, golem) && AttackRunner.isFoe(a, survival), "iron golems and players are foes");
            helper.assertTrue(!AttackRunner.isFoe(a, spectator), "spectators are not");
            helper.assertTrue(!AttackRunner.isFoe(a, zombie), "other monsters are not");
            helper.assertTrue(!AttackRunner.isFoe(a, b) && !AttackRunner.isFoe(a, w) && !AttackRunner.isFoe(w, a), "members of the encounter never fight each other");
            helper.assertTrue(!a.canAttack(b) && !a.canAttack(w), "a puppet will not target a member");
            float hp = b.getHealth();
            helper.assertTrue(!b.hurtServer(level, level.damageSources().mobAttack(a), 20.0F), "zero friendly fire between puppets");
            helper.assertValueEqual(b.getHealth(), hp, "unhurt");
            w.beginDirect(level, 11);
            helper.assertTrue(!w.hurtServer(level, level.damageSources().mobAttack(a), 20.0F), "nor onto the Matriarch");
            for (PuppetEntity p : w.getPuppets(level)) {
                p.discard();
            }
            helper.assertTrue(!b.hurtServer(level, level.damageSources().mobAttack(w), 20.0F), "nor from her onto a puppet");
            a.discard();
            b.discard();
            w.discard();
            zombie.discard();
            golem.discard();
            helper.succeed();
        });
    }

    /** Every attack of a puppet hurts a foe in range, and none of them ever scratches a bystander puppet. */
    private static void attacksHurtFoesSpareAllies(GameTestHelper helper, PuppetKind kind) {
        ServerLevel level = helper.getLevel();
        final int ox = 4000 + kind.ordinal() * 100;
        farPlot(helper, ox, 20, () -> {
            PuppetEntity p = puppet(helper, kind, ox + 9, 2);
            PuppetEntity bystander = puppet(helper, kind == PuppetKind.SEWING_ARACHNID ? PuppetKind.PORCELAIN_HOUND : PuppetKind.SEWING_ARACHNID, ox + 12, 12);
            bystander.setNoAi(true);
            IronGolem golem = golem(helper, ox + 9, Y + 1, 9);
            List<PuppetAttack> attacks = new ArrayList<>();
            for (PuppetAttack a : kind.attacks) {
                if (!a.reactive) {
                    attacks.add(a);
                }
            }
            lockOffense(p);
            p.setTarget(golem);
            runAttack(helper, level, ox, p, golem, bystander, attacks, 0, () -> {
                helper.assertValueEqual(bystander.getHealth(), bystander.getMaxHealth(), "the bystander puppet was never touched");
                p.discard();
                bystander.discard();
                golem.discard();
                helper.succeed();
            });
        });
    }

    private static void runAttack(GameTestHelper h, ServerLevel level, int ox, PuppetEntity p, IronGolem golem, PuppetEntity bystander,
                                  List<PuppetAttack> attacks, int index, Runnable done) {
        if (index >= attacks.size()) {
            done.run();
            return;
        }
        PuppetAttack a = attacks.get(index);
        waitUntil(h, 150, t -> { }, () -> p.getPuppetState() == PuppetState.IDLE_COMBAT && !p.getRunner().isBusy(), () -> {
            double d = Math.max(2.5D, Math.min(12.0D, (a.minRange + a.maxRange) / 2.0D));
            double zz = Math.min(15.0D, 2.0D + d);
            p.setPos(h.absoluteVec(new Vec3(ox + 9.5D, Y + 1, 2.5D)));
            p.setDeltaMovement(Vec3.ZERO);
            p.setHealth(p.getMaxHealth());
            golem.setPos(h.absoluteVec(new Vec3(ox + 9.5D, Y + 1, 2.5D + d)));
            golem.setHealth(golem.getMaxHealth());
            bystander.setPos(h.absoluteVec(new Vec3(ox + 11.5D, Y + 1, 2.5D + d)));
            p.setTarget(golem);
            h.assertTrue(zz <= 15.0D, "geometry stays inside the plot");
            h.assertTrue(p.getRunner().start(a, golem), kind(p) + "/" + a.id + " starts");
            waitUntil(h, 200, t -> { }, () -> !p.getRunner().isBusy(), () -> {
                h.assertTrue(!p.getRunner().isBusy(), kind(p) + "/" + a.id + " finishes");
                h.assertTrue(golem.getHealth() < golem.getMaxHealth(), kind(p) + "/" + a.id + " hurt the golem in range (" + golem.getHealth() + "/" + golem.getMaxHealth() + ", puppet at " + p.position().subtract(h.absoluteVec(new Vec3(0.0D, 0.0D, 0.0D))) + ", golem at " + golem.position().subtract(h.absoluteVec(new Vec3(0.0D, 0.0D, 0.0D))) + ", tangled " + p.isTangled() + ")");
                h.assertValueEqual(bystander.getHealth(), bystander.getMaxHealth(), kind(p) + "/" + a.id + " spared the bystander");
                runAttack(h, level, ox, p, golem, bystander, attacks, index + 1, done);
            });
        });
    }

    private static String kind(PuppetEntity p) {
        return p.getKind().id;
    }

    // ------------------------------------------------------------------------------------------------ the staff
    private static void staffPostures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            ItemStack staff = new ItemStack(ModItems.TEN_THREADS_STAFF.get());
            helper.assertValueEqual(TenThreadsStaffItem.posture(staff), 0, "a new staff starts in stance I");
            helper.assertValueEqual(TenThreadsStaffItem.cyclePosture(staff), 1, "II");
            helper.assertValueEqual(TenThreadsStaffItem.cyclePosture(staff), 2, "III");
            helper.assertValueEqual(TenThreadsStaffItem.cyclePosture(staff), 0, "and round to I");
            helper.assertTrue(staff.get(DataComponents.CUSTOM_DATA) != null, "the stance is kept in the stack's data");

            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setPos(helper.absoluteVec(new Vec3(9.5D, Y + 1, 9.5D)));
            player.setYRot(0.0F); // facing +z
            player.setXRot(0.0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, staff);
            // Shift + use only changes the stance
            player.setShiftKeyDown(true);
            staff.getItem().use(level, player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(TenThreadsStaffItem.posture(staff), 1, "Shift + use cycles the stance");
            helper.assertValueEqual(staff.getDamageValue(), 0, "and costs nothing");
            helper.assertTrue(!player.getCooldowns().isOnCooldown(staff), "and starts no cooldown");
            player.setShiftKeyDown(false);

            // stance II: a toll that hurts and roots every enemy within 8 blocks, and spares those who are not
            Zombie near = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(9, Y + 1, 13));
            Zombie far = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, Y + 1, 1));
            IronGolem ally = golem(helper, 5, Y + 1, 9);
            near.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
            near.setHealth(near.getMaxHealth());
            helper.assertTrue(staff.getItem().use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "stance II is used");
            helper.assertTrue(Math.abs(lost(near) - TenThreadsStaffItem.CONTROL_DAMAGE) < 0.01F, "it hurts an enemy for 8 (" + lost(near) + ")");
            helper.assertTrue(near.hasEffect(MobEffects.SLOWNESS), "and roots it");
            helper.assertValueEqual(far.getHealth(), far.getMaxHealth(), "an enemy beyond 8 blocks is spared");
            helper.assertValueEqual(ally.getHealth(), ally.getMaxHealth(), "and so is a non-enemy");
            helper.assertValueEqual(staff.getDamageValue(), TenThreadsStaffItem.CONTROL_COST, "it wears the staff by 2");
            helper.assertTrue(player.getCooldowns().isOnCooldown(staff), "and cools down");
            int wear = staff.getDamageValue();
            float hp = near.getHealth();
            helper.assertTrue(!staff.getItem().use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "no use during the cooldown");
            helper.assertValueEqual(staff.getDamageValue(), wear, "no wear during the cooldown");
            helper.assertValueEqual(near.getHealth(), hp, "no damage during the cooldown");

            // stance III: arms crush a 4 x 4 area three blocks ahead
            player.getCooldowns().removeCooldown(ModItems.TEN_THREADS_STAFF.get().builtInRegistryHolder().key().identifier());
            Zombie ahead = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(9, Y + 1, 12));
            ahead.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
            ahead.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
            ahead.setHealth(100.0F);
            TenThreadsStaffItem.setPosture(staff, 2);
            staff.getItem().use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(Math.abs(lost(ahead) - TenThreadsStaffItem.RUIN_DAMAGE) < 0.01F, "stance III crushes for 22 (" + lost(ahead) + ")");
            helper.assertValueEqual(staff.getDamageValue(), wear + TenThreadsStaffItem.RUIN_COST, "and wears the staff by 4");

            // stance I: a thread hauls the player to what it sees, and finds nothing in the sky
            player.getCooldowns().removeCooldown(ModItems.TEN_THREADS_STAFF.get().builtInRegistryHolder().key().identifier());
            TenThreadsStaffItem.setPosture(staff, 0);
            player.setXRot(-90.0F); // the sky
            int before = staff.getDamageValue();
            helper.assertTrue(!staff.getItem().use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "the thread finds no anchor in the sky");
            helper.assertValueEqual(staff.getDamageValue(), before, "and costs nothing");
            player.setXRot(60.0F); // the floor, a few blocks away
            player.setDeltaMovement(Vec3.ZERO);
            helper.assertTrue(staff.getItem().use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "it holds on the floor");
            helper.assertTrue(player.getDeltaMovement().lengthSqr() > 0.1D, "and hauls the player");
            helper.assertValueEqual(staff.getDamageValue(), before + TenThreadsStaffItem.HOOK_COST, "for 1 durability");
            helper.assertTrue(player.hasEffect(MobEffects.SLOW_FALLING), "with slow falling");
            near.discard();
            far.discard();
            ahead.discard();
            ally.discard();
            helper.succeed();
        });
    }

    private static void orbRewards(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack staff = new ItemStack(ModItems.TEN_THREADS_STAFF.get());
        staff.setDamageValue(100);
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        player.setHealth(10.0F);
        player.getFoodData().setFoodLevel(10);
        SpoolVitalOrbEntity.reward(player);
        helper.assertValueEqual(player.getHealth(), 14.0F, "an orb heals 4");
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 13, "feeds 3");
        helper.assertValueEqual(staff.getDamageValue(), 85, "and mends 15 of the staff");
        staff.setDamageValue(5);
        SpoolVitalOrbEntity.reward(player);
        helper.assertValueEqual(staff.getDamageValue(), 0, "never below zero");
        helper.succeed();
    }

    private static void dialogueHook(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            List<WitchDialogueManager.Line> heard = new ArrayList<>();
            WitchDialogueManager.setVoiceHook((lv, witch, line, text) -> heard.add(line));
            MatriarchWitchEntity w = witch(helper, 9, 9);
            w.beginDirect(level, 4);
            helper.assertTrue(heard.contains(WitchDialogueManager.Line.STAGE_4), "she speaks the stage 4 line when it begins: " + heard);
            helper.assertValueEqual(w.getLastLine(), WitchDialogueManager.Line.STAGE_4, "and remembers it");
            helper.assertValueEqual(WitchDialogueManager.Line.forStage(10), WitchDialogueManager.Line.STAGE_10, "every stage has its own line");
            helper.assertTrue(!WitchDialogueManager.say(level, w, WitchDialogueManager.Line.THREAD_CUT), "a complaint right after another line is held back");
            helper.assertTrue(!heard.contains(WitchDialogueManager.Line.THREAD_CUT), "and the hook never heard it");
            waitUntil(helper, WitchDialogueManager.THREAD_CUT_GAP_TICKS + 20, t -> { }, () -> false, () -> {
                helper.assertTrue(WitchDialogueManager.say(level, w, WitchDialogueManager.Line.THREAD_CUT), "after the gap the complaint is heard");
                helper.assertTrue(heard.contains(WitchDialogueManager.Line.THREAD_CUT), "and the hook heard it");
                helper.assertTrue(!WitchDialogueManager.say(level, w, WitchDialogueManager.Line.THREAD_CUT), "the next one, right away, is held back again");
                WitchDialogueManager.setVoiceHook(null);
                for (PuppetEntity p : w.getPuppets(level)) {
                    p.discard();
                }
                w.discard();
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ arena helpers
    private static void antiPillar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            MatriarchWitchEntity w = witch(helper, 9, 9);
            w.beginDirect(level, 1);
            for (int y = Y + 1; y <= Y + 8; y++) {
                helper.setBlock(new BlockPos(15, y, 15), Blocks.STONE);
            }
            IronGolem golem = golem(helper, 15, Y + 9, 15);
            waitUntil(helper, 240, t -> {
                PuppetEntity p = w.getActivePuppet(level);
                if (p != null && p.getPuppetState() == PuppetState.IDLE_COMBAT) {
                    p.setTarget(golem);
                }
            }, () -> w.getPulls() > 0, () -> {
                PuppetEntity act = w.getActivePuppet(level);
                helper.assertTrue(w.getPulls() > 0, "a target on a pillar out of reach is pulled down by a thread (puppet "
                        + (act == null ? "none" : act.getPuppetState() + ", reachable " + act.isTargetReachable() + ", y " + act.getY() + ", target " + act.getTarget() + ", golem y " + golem.getY()) + ")");
                helper.assertTrue(golem.hasEffect(MobEffects.SLOW_FALLING), "with slow falling, so the pull does not kill");
                for (PuppetEntity p : w.getPuppets(level)) {
                    p.discard();
                }
                w.discard();
                golem.discard();
                helper.succeed();
            });
        });
    }

    private static void antiHole(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plot(helper, 0, 0, 18, 18, () -> {
            MatriarchWitchEntity w = witch(helper, 9, 9);
            w.beginDirect(level, 2);
            // a sealed pit around the point the hound will wait in
            for (int x = 1; x <= 5; x++) {
                for (int z = 1; z <= 5; z++) {
                    boolean wall = x == 1 || x == 5 || z == 1 || z == 5;
                    for (int y = Y + 1; y <= Y + 5; y++) {
                        if (wall || y == Y + 5) {
                            helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                        }
                    }
                }
            }
            IronGolem golem = golem(helper, 15, Y + 1, 15);
            boolean[] placed = {false};
            waitUntil(helper, 400, t -> {
                PuppetEntity p = w.getActivePuppet(level);
                if (p != null && p.getPuppetState() == PuppetState.IDLE_COMBAT) {
                    if (!placed[0]) {
                        placed[0] = true;
                        p.setPos(helper.absoluteVec(new Vec3(3.5D, Y + 1, 3.5D)));
                    }
                    p.setTarget(golem);
                }
            }, () -> {
                PuppetEntity p = w.getActivePuppet(level);
                return p != null && p.getLiftsDone() > 0;
            }, () -> {
                PuppetEntity p = w.getActivePuppet(level);
                helper.assertTrue(p != null && p.getLiftsDone() > 0, "a puppet shut in a pit is hoisted out by the threads");
                later(helper, 14, () -> {
                    helper.assertTrue(p.distanceTo(golem) < 9.0D, "next to its target (" + p.distanceTo(golem) + ")");
                    for (PuppetEntity q : w.getPuppets(level)) {
                        q.discard();
                    }
                    w.discard();
                    golem.discard();
                    helper.succeed();
                });
            });
        });
    }

    // ------------------------------------------------------------------------------------------------ far plots
    /** A plot far from every other test (the Spindle, the arena edge and the reset look at the whole world). */
    private static void farPlot(GameTestHelper h, int ox, int width, Runnable body) {
        floor(h, ox, 0, ox + width, 20);
        ServerLevel level = h.getLevel();
        BlockPos mid = h.absolutePos(new BlockPos(ox + width / 2, Y, 10));
        java.util.function.BooleanSupplier ticking = () -> {
            for (int cx = -1; cx <= 1; cx++) {
                for (int cz = -1; cz <= 1; cz++) {
                    if (!level.isPositionEntityTicking(mid.offset(cx * 16, 0, cz * 16))) {
                        return false;
                    }
                }
            }
            return true;
        };
        forceChunks(h, ox - 16, 0, ox + width + 16, 40);
        waitUntil(h, 400, t -> { }, ticking, () -> {
            h.assertTrue(ticking.getAsBoolean(), "the far plot ticks entities");
            body.run();
        });
    }

    private static void spindleConditions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        final int ox = 3200;
        farPlot(helper, ox, 30, () -> {
            helper.setTime(6000L);
            BlockPos ground = helper.absolutePos(new BlockPos(ox + 10, Y, 10));
            BlockPos air = ground.above(8);
            ItemStack spindle = new ItemStack(ModItems.CURSED_SPINDLE.get(), 3);
            helper.assertValueEqual(CursedSpindleItem.trySummon(level, air, null, spindle), CursedSpindleItem.SummonResult.NOT_SOLID, "needs a solid block");
            helper.assertValueEqual(CursedSpindleItem.trySummon(level, ground, null, spindle), CursedSpindleItem.SummonResult.WRONG_BIOME, "wrong biome in daylight");
            helper.setTime(18000L);
            helper.assertValueEqual(CursedSpindleItem.trySummon(level, ground, null, spindle), CursedSpindleItem.SummonResult.WRONG_BIOME, "at night, a bare plain is not a forest either");
            // a dense wood: 16 logs around, clear of the 5x5x5
            for (int i = 0; i < 16; i++) {
                int x = 5 + (i % 4) * 2;
                int z = i < 8 ? 3 : 17;
                for (int y = 1; y <= 3; y++) {
                    helper.setBlock(new BlockPos(ox + (i % 8) * 2 + 4, Y + y, z), Blocks.OAK_LOG);
                }
            }
            helper.assertValueEqual(CursedSpindleItem.evaluateConditions(level, ground), CursedSpindleItem.SummonResult.SUCCESS, "a dense forest at night is enough");
            BlockPos beam = ground.offset(1, 3, 1);
            level.setBlock(beam, Blocks.STONE.defaultBlockState(), 3);
            helper.assertValueEqual(CursedSpindleItem.trySummon(level, ground, null, spindle), CursedSpindleItem.SummonResult.NO_SPACE, "a block inside the 5x5x5 above is too cramped");
            helper.assertValueEqual(spindle.getCount(), 3, "a failed use never consumes the spindle");
            level.setBlock(beam, Blocks.AIR.defaultBlockState(), 3);
            helper.assertValueEqual(CursedSpindleItem.trySummon(level, ground, null, spindle), CursedSpindleItem.SummonResult.SUCCESS, "summons");
            helper.assertValueEqual(spindle.getCount(), 2, "and consumes exactly one");
            List<MatriarchWitchEntity> found = level.getEntitiesOfClass(MatriarchWitchEntity.class, new AABB(ground).inflate(10.0D));
            helper.assertValueEqual(found.size(), 1, "exactly one Matriarch");
            MatriarchWitchEntity w = found.getFirst();
            helper.assertValueEqual(w.getPhase(), EncounterPhase.INTRO, "she begins with her intro");
            helper.assertTrue(w.getBossEvent().isVisible(), "the bar is shown");
            helper.assertValueEqual(w.getPuppets(level).size(), 10, "ten puppets hang");
            helper.assertValueEqual(CursedSpindleItem.trySummon(level, ground.east(4), null, spindle), CursedSpindleItem.SummonResult.ALREADY_ACTIVE, "never two at once");
            helper.assertValueEqual(spindle.getCount(), 2, "and nothing is consumed");
            helper.setTime(6000L);
            for (PuppetEntity p : w.getPuppets(level)) {
                p.discard();
            }
            w.discard();
            helper.succeed();
        });
    }

    private static void arenaEdge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        final int ox = 3400;
        farPlot(helper, ox, 50, () -> {
            MatriarchWitchEntity w = witch(helper, ox + 5, 10);
            w.beginDirect(level, 1);
            ServerPlayer inside = helper.makeMockServerPlayerInLevel();
            ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
            inside.setGameMode(GameType.SURVIVAL);
            stranger.setGameMode(GameType.SURVIVAL);
            inside.snapTo(helper.absoluteVec(new Vec3(ox + 12.5D, Y + 1, 10.5D)));
            stranger.snapTo(helper.absoluteVec(new Vec3(ox + 45.5D, Y + 1, 10.5D)));
            float strangerHp = stranger.getHealth();
            helper.assertTrue(level.players().contains(inside), "the mock player is in the level");
            later(helper, 4, () -> {
                helper.assertValueEqual(w.getArenaPushes(), 0, "nobody is pushed inside the arena, or from far away");
                inside.snapTo(helper.absoluteVec(new Vec3(ox + 5.0D + 31.0D, Y + 1, 10.5D)));
                float hp = inside.getHealth();
                waitUntil(helper, 30, t -> { }, () -> w.getArenaPushes() > 0, () -> {
                    helper.assertTrue(w.getArenaPushes() > 0, "whoever leaves the 28-block circle is pushed back");
                    helper.assertTrue(w.getArenaHits() > 0, "and hurt for 2 (the mock player itself is invulnerable until its client loads)");
                    helper.assertValueEqual(stranger.getHealth(), strangerHp, "a player who never entered is left alone");
                    // the advancement of the cut thread, on a real server player
                    PuppetEntity.awardAdvancement(level, inside, "sever_crimson_thread");
                    var holder = level.getServer().getAdvancements().get(id("bosses/sever_crimson_thread"));
                    helper.assertTrue(inside.getAdvancements().getOrStartProgress(holder).isDone(), "sever_crimson_thread is awarded");
                    for (PuppetEntity p : w.getPuppets(level)) {
                        p.discard();
                    }
                    w.discard();
                    helper.succeed();
                });
            });
        });
    }

    private static void resetWhenAlone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        final int ox = 3600;
        farPlot(helper, ox, 30, () -> {
            MatriarchWitchEntity w = witch(helper, ox + 15, 10);
            w.beginDirect(level, 2);
            waitUntil(helper, 80, t -> { }, () -> {
                PuppetEntity p = w.getActivePuppet(level);
                return p != null && p.getPuppetState() == PuppetState.IDLE_COMBAT;
            }, () -> {
                PuppetEntity p = w.getActivePuppet(level);
                helper.assertTrue(p != null, "the stage 2 puppet is fighting");
                p.setHealth(p.getMaxHealth() * 0.4F);
                waitUntil(helper, MatriarchWitchEntity.RESET_TICKS + 80, t -> { }, () -> w.getResets() > 0, () -> {
                    helper.assertTrue(w.getResets() > 0, "no player within 48 blocks for 10 s resets the encounter");
                    helper.assertValueEqual(p.getHealth(), p.getMaxHealth(), "the puppet heals back");
                    helper.assertValueEqual(w.getCurrentStage(), 2, "and the stage is kept");
                    for (PuppetEntity q : w.getPuppets(level)) {
                        q.discard();
                    }
                    w.discard();
                    helper.succeed();
                });
            });
        });
    }
}
