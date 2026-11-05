package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.client.babydragon.*;
import com.codenamecoraline.entity.babydragon.*;
import com.codenamecoraline.registry.*;
import java.util.Map;
import java.util.List;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Opt-in integration tests with real ticks, collisions, serialization and projectile hits. */
@EventBusSubscriber(modid=CodenameCoraline.MOD_ID)
public final class BabyDragonGameTests {
    private static final int Y=10;
    private static final Map<String,Consumer<GameTestHelper>> TESTS=Map.ofEntries(
        Map.entry("dragon_shoulder_no_owner_impulse",BabyDragonGameTests::shoulderNoImpulse),
        Map.entry("dragon_animation_packet_ids",BabyDragonGameTests::animationPackets),
        Map.entry("dragon_owner_attack_response",BabyDragonGameTests::ownerAttackResponse),
        Map.entry("dragon_moving_target_hit",BabyDragonGameTests::movingTargetHit),
        Map.entry("dragon_owner_attacks_chicken",h->ownerAttacksAnimal(h,EntityType.CHICKEN)),
        Map.entry("dragon_owner_attacks_cow",h->ownerAttacksAnimal(h,EntityType.COW)),
        Map.entry("dragon_owner_attacks_pig",h->ownerAttacksAnimal(h,EntityType.PIG)),
        Map.entry("dragon_registration",BabyDragonGameTests::registration),
        Map.entry("dragon_attributes",BabyDragonGameTests::attributes),
        Map.entry("dragon_natural_spawn",BabyDragonGameTests::spawn),
        Map.entry("dragon_taming_and_feeding",BabyDragonGameTests::taming),
        Map.entry("dragon_owner_persistence",BabyDragonGameTests::ownerPersistence),
        Map.entry("dragon_ground_follow",BabyDragonGameTests::groundFollow),
        Map.entry("dragon_air_follow",BabyDragonGameTests::airFollow),
        Map.entry("dragon_orbit_moves",BabyDragonGameTests::orbit),
        Map.entry("dragon_shoulder_approach",BabyDragonGameTests::shoulderApproach),
        Map.entry("dragon_shoulder_exit",BabyDragonGameTests::shoulderExit),
        Map.entry("dragon_shoulder_preserves_data",BabyDragonGameTests::shoulderData),
        Map.entry("dragon_no_duplicate",BabyDragonGameTests::noDuplicate),
        Map.entry("dragon_reactive_defense",BabyDragonGameTests::defense),
        Map.entry("dragon_ember_damage",BabyDragonGameTests::emberDamage),
        Map.entry("dragon_owner_safe",BabyDragonGameTests::ownerSafe),
        Map.entry("dragon_allies_safe",BabyDragonGameTests::alliesSafe),
        Map.entry("dragon_fire_cooldown",BabyDragonGameTests::cooldown),
        Map.entry("dragon_fire_and_fall_immunity",BabyDragonGameTests::immunity),
        Map.entry("dragon_safe_teleport",BabyDragonGameTests::teleport),
        Map.entry("dragon_sit_command",BabyDragonGameTests::sit),
        Map.entry("dragon_save_load",BabyDragonGameTests::saveLoad),
        Map.entry("dragon_bestiary",BabyDragonGameTests::bestiary),
        Map.entry("dragon_animations_resources",BabyDragonGameTests::resources),
        Map.entry("dragon_ember_stops_at_wall",BabyDragonGameTests::wall),
        Map.entry("dragon_defense_toggle",BabyDragonGameTests::defenseToggle),
        Map.entry("dragon_shoulder_unsafe_exit",BabyDragonGameTests::unsafeExit),
        Map.entry("dragon_flight_wall_collision",BabyDragonGameTests::flightWall),
        Map.entry("dragon_ember_range",BabyDragonGameTests::emberRange),
        Map.entry("dragon_self_defense_and_peaceful_targets",BabyDragonGameTests::selfDefense),
        Map.entry("dragon_neutral_combat_only",BabyDragonGameTests::neutralCombat),
        Map.entry("dragon_fire_cooks_meat_all_stages",BabyDragonGameTests::fireCooksMeatAllStages),
        Map.entry("dragon_breath_and_magma_cook_and_preserve_drops",BabyDragonGameTests::breathAndMagmaCookAndPreserveDrops),
        Map.entry("dragon_fire_respects_immune_and_invulnerable",BabyDragonGameTests::fireRespectsImmuneAndInvulnerable),
        Map.entry("dragon_individual_traits_generation_and_persistence",BabyDragonGameTests::individualTraitsGenerationAndPersistence),
        Map.entry("dragon_individual_traits_legacy_save_migration",BabyDragonGameTests::individualTraitsLegacySaveMigration),
        Map.entry("dragon_individual_traits_behavioral_impact",BabyDragonGameTests::individualTraitsBehavioralImpact),
        Map.entry("dragon_post_tame_naming_and_skip",BabyDragonGameTests::postTameNamingAndSkip),
        Map.entry("dragon_naming_security_validation",BabyDragonGameTests::namingSecurityValidation),
        Map.entry("dragon_texture_variants_persistence",BabyDragonGameTests::textureVariantsPersistence),
        Map.entry("dragon_combat_retains_target_beyond_240_ticks",BabyDragonGameTests::combatRetainsTargetBeyond240Ticks),
        Map.entry("dragon_stay_self_defense_and_return_to_anchor",BabyDragonGameTests::staySelfDefenseAndReturnToAnchor),
        Map.entry("dragon_commands_return_protect_follow_and_rescue",BabyDragonGameTests::commandsReturnProtectFollowAndRescue),
        Map.entry("dragon_flight_physics_dive_climb_banking",BabyDragonGameTests::flightPhysicsDiveClimbBanking),
        Map.entry("dragon_advanced_combat_threat_and_positioning",BabyDragonGameTests::advancedCombatThreatAndPositioning),
        Map.entry("dragon_saddle_backpack_and_legacy_inventory",BabyDragonGameTests::saddleBackpackAndLegacyInventory),
        Map.entry("dragon_totem_of_undying_owner_protection",BabyDragonGameTests::totemOfUndyingOwnerProtection),
        Map.entry("dragon_land_hunting_delivery_and_self_heal",BabyDragonGameTests::landHuntingDeliveryAndSelfHeal),
        Map.entry("dragon_aquatic_hunting_dive_and_resurface",BabyDragonGameTests::aquaticHuntingDiveAndResurface),
        Map.entry("dragon_social_interaction_and_interruption",BabyDragonGameTests::socialInteractionAndInterruption));
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,name);}
    @SubscribeEvent public static void functions(RegisterEvent e){
        if(Boolean.getBoolean("coraline.babyDragonTests")) e.register(Registries.TEST_FUNCTION,r->TESTS.forEach((n,f)->r.register(id(n),f)));
    }
    @SubscribeEvent public static void tests(RegisterGameTestsEvent e){
        if(!Boolean.getBoolean("coraline.babyDragonTests"))return;
        var environment=e.registerEnvironment(id("baby_dragon_environment"));
        TESTS.forEach((n,f)->e.registerTest(id(n),new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,id(n)),new TestData<>(environment,id("basilisk_test_arena"),300,0,true))));
    }
    private static void arena(GameTestHelper h){
        for(int x=0;x<=12;x++)for(int z=0;z<=12;z++) {
            h.setBlock(new BlockPos(x,Y,z),Blocks.STONE);
            for(int y=Y+1;y<=Y+7;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
        BlockPos p=h.absolutePos(new BlockPos(6,Y,6));
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.getLevel().setChunkForced((p.getX()>>4)+x,(p.getZ()>>4)+z,true);
    }
    private static BabyDragon dragon(GameTestHelper h,boolean ai){
        arena(h);
        return ai?h.spawn(ModEntities.BABY_DRAGON.get(),new BlockPos(3,Y+1,6)):h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(3,Y+1,6));
    }
    private static ServerPlayer owner(GameTestHelper h,BabyDragon d,int x){
        ServerPlayer p=h.makeMockServerPlayerInLevel();p.setPos(h.absoluteVec(new Vec3(x+.5,Y+1,6.5)));
        p.connection.markClientLoaded();
        p.setNoGravity(true);d.tame(p);return p;
    }
    private static LivingEntity hostile(GameTestHelper h){
        var target=h.spawnWithNoFreeWill(EntityType.HUSK,new BlockPos(9,Y+1,6));
        target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);return target;
    }
    private static BabyDragon copy(GameTestHelper h,BabyDragon d){
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());d.saveWithoutId(out);
        BabyDragon result=ModEntities.BABY_DRAGON.get().create(h.getLevel(),EntitySpawnReason.LOAD);
        result.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));return result;
    }
    private static void registration(GameTestHelper h){
        var d=dragon(h,false);h.assertTrue(d.getType()==ModEntities.BABY_DRAGON.get(),"registered dragon");
        h.assertValueEqual(d.getType().getCategory(),MobCategory.CREATURE,"peaceful creature category");
        h.assertTrue(ModItems.BABY_DRAGON_SPAWN_EGG.get() instanceof SpawnEggItem,"registered spawn egg");h.succeed();
    }
    private static void attributes(GameTestHelper h){
        var d=dragon(h,false);
        h.assertTrue(d.getIntelligence()>=BabyDragon.MIN_TRAIT&&d.getIntelligence()<=BabyDragon.MAX_TRAIT,"intelligence in range");
        h.assertTrue(d.getVitality()>=BabyDragon.MIN_TRAIT&&d.getVitality()<=BabyDragon.MAX_TRAIT,"vitality in range");
        h.assertTrue(d.getStrength()>=BabyDragon.MIN_TRAIT&&d.getStrength()<=BabyDragon.MAX_TRAIT,"strength in range");
        h.assertTrue(d.getAgility()>=BabyDragon.MIN_TRAIT&&d.getAgility()<=BabyDragon.MAX_TRAIT,"agility in range");
        h.assertTrue(Math.abs(d.getMaxHealth()-30F*d.getVitality())<.01F,"updated hatchling health scaled by vitality");
        h.assertTrue(Math.abs(d.getAttributeValue(Attributes.ARMOR)-2D*d.getVitality())<.01D,"armor scaled by vitality");
        h.assertTrue(Math.abs(d.getAttributeValue(Attributes.MOVEMENT_SPEED)-.22D*d.getAgility())<.001D,"ground speed scaled by agility");
        h.assertTrue(d.getAttributeValue(Attributes.FLYING_SPEED)>0,"flight attribute configurable");
        h.assertTrue(!d.isTame(),"wild initially");h.succeed();
    }
    private static void spawn(GameTestHelper h){
        var d=dragon(h,false);var biomes=h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for(var key:List.of(Biomes.FOREST,Biomes.GROVE,Biomes.JAGGED_PEAKS)){
            var entries=biomes.getOrThrow(key).value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap();
            h.assertTrue(entries.stream().anyMatch(e->e.value().type()==ModEntities.BABY_DRAGON.get()&&e.value().minCount()==1&&e.value().maxCount()==2),"habitat includes rare one/two dragon groups");
        }
        BlockPos p=h.absolutePos(new BlockPos(6,Y+1,6));
        h.assertTrue(BabyDragon.checkSpawnRules(ModEntities.BABY_DRAGON.get(),h.getLevel(),EntitySpawnReason.NATURAL,p,h.getLevel().getRandom()),"bright open solid floor permits spawn");
        h.setBlock(new BlockPos(6,Y+1,6),Blocks.STONE);
        h.assertTrue(!BabyDragon.checkSpawnRules(ModEntities.BABY_DRAGON.get(),h.getLevel(),EntitySpawnReason.NATURAL,p,h.getLevel().getRandom()),"solid block rejects spawn");h.succeed();
    }
    private static void taming(GameTestHelper h){
        var d=dragon(h,false);Player p=h.makeMockPlayer(GameType.SURVIVAL);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SALMON,64));
        d.getRandom().setSeed(31415);
        int attempts=0;while(!d.isTame()&&attempts<64){d.mobInteract(p,InteractionHand.MAIN_HAND);attempts++;}
        h.assertTrue(d.isTame()&&d.isOwnedBy(p),"salmon tames to interacting player's UUID");h.assertValueEqual(p.getMainHandItem().getCount(),64-attempts,"one salmon consumed per attempt");
        d.setHealth(10);d.mobInteract(p,InteractionHand.MAIN_HAND);h.assertValueEqual(d.getHealth(),14F,"feeding heals four health");
        Player stranger=h.makeMockPlayer(GameType.SURVIVAL);stranger.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SALMON));d.mobInteract(stranger,InteractionHand.MAIN_HAND);
        h.assertTrue(d.isOwnedBy(p)&&stranger.getMainHandItem().getCount()==1,"stranger cannot steal or feed companion");h.succeed();
    }
    private static void ownerPersistence(GameTestHelper h){var d=dragon(h,false);var p=owner(h,d,5);var loaded=copy(h,d);h.assertTrue(loaded.isTame()&&loaded.getOwnerReference().getUUID().equals(p.getUUID()),"owner UUID survives serialization");h.succeed();}
    private static void groundFollow(GameTestHelper h){
        var d=dragon(h,true);var p=owner(h,d,9);double initial=d.distanceToSqr(p);
        h.runAfterDelay(60,()->{h.assertTrue(d.distanceToSqr(p)<initial-4,"ground navigation approaches owner");h.assertTrue(!d.isFlying(),"walks a moderate distance");h.succeed();});
    }
    private static void airFollow(GameTestHelper h){
        var d=dragon(h,true);var p=owner(h,d,11);d.setPos(d.position().add(-2,0,0));double initial=d.distanceToSqr(p);
        h.runAfterDelay(30,()->{h.assertTrue(d.isFlying()&&d.getY()>h.absolutePos(new BlockPos(0,Y+1,0)).getY()+.4,"takes off to follow");h.assertTrue(d.distanceToSqr(p)<initial,"flight closes distance");h.succeed();});
    }
    private static void orbit(GameTestHelper h){
        var d=dragon(h,true);var p=owner(h,d,6);d.getDragonBrain().delayAmbient(0);Vec3[] pos={null};
        h.runAfterDelay(110,()->{h.assertValueEqual(d.getDragonState(),DragonState.ORBITING,"idle owner triggers orbit");pos[0]=d.position();});
        h.runAfterDelay(145,()->{h.assertTrue(d.position().distanceToSqr(pos[0])>.15,"orbital flight actually moves");h.assertTrue(d.distanceToSqr(p)<64,"stays near owner");h.succeed();});
    }
    private static void shoulderApproach(GameTestHelper h){
        var d=dragon(h,true);var p=owner(h,d,5);h.assertTrue(d.requestShoulder(p),"owner requests approach");
        h.runAfterDelay(60,()->{h.assertTrue(d.isOnShoulder(),"approach lands on shoulder");h.assertTrue(d.position().distanceToSqr(BabyDragon.shoulderPosition(p))<.01,"shoulder anchored");h.succeed();});
    }
    private static void shoulderExit(GameTestHelper h){var d=dragon(h,true);var p=owner(h,d,5);h.assertTrue(d.landOnShoulder(p),"lands");p.setShiftKeyDown(true);h.runAfterDelay(65,()->{h.assertTrue(!d.isOnShoulder(),"held sneak releases same entity");h.assertTrue(d.isAlive()&&d.clearBlocks(d.position()),"exit stays alive outside solids");h.succeed();});}
    private static void shoulderData(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,5);d.setHealth(13);d.setCustomName(net.minecraft.network.chat.Component.literal("Brasa"));var uuid=d.getUUID();
        h.assertTrue(d.landOnShoulder(p),"lands");h.assertTrue(d.getUUID().equals(uuid)&&d.getHealth()==13&&d.getName().getString().equals("Brasa")&&d.isOwnedBy(p),"perching retains UUID health name owner");
        var loaded=copy(h,d);h.assertTrue(loaded.isOnShoulder()&&loaded.getUUID().equals(uuid),"shoulder flag and UUID persisted");h.succeed();
    }
    private static void noDuplicate(GameTestHelper h){var d=dragon(h,false);var p=owner(h,d,5);var uuid=d.getUUID();for(int i=0;i<8;i++){d.setPos(p.position().add(-1,0,0));h.assertTrue(d.landOnShoulder(p),"lands once");d.leaveShoulder();}h.assertValueEqual(h.getLevel().getEntitiesOfClass(BabyDragon.class,p.getBoundingBox().inflate(8)).stream().filter(e->e.getUUID().equals(uuid)).count(),1L,"exactly one original entity");h.succeed();}
    private static void defense(GameTestHelper h){
        var d=dragon(h,true);var p=owner(h,d,4);var target=hostile(h);
        h.runAfterDelay(8,()->{h.assertTrue(d.getTarget()==null,"does not seek nearby hostiles");p.setLastHurtByMob(target);});
        h.runAfterDelay(20,()->{h.assertTrue(d.getTarget()==target&&d.getDragonState()==DragonState.DEFENDING,"reacts to owner being attacked");h.succeed();});
    }
    private static void launch(GameTestHelper h,BabyDragon d,Vec3 pos,Vec3 direction){var e=new DragonEmber(h.getLevel(),d);e.setPos(pos);e.shoot(direction.x,direction.y,direction.z,.8F,0);h.getLevel().addFreshEntity(e);}
    private static void emberDamage(GameTestHelper h){var d=dragon(h,false);var pig=h.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(8,Y+1,6));pig.setNoGravity(true);float health=pig.getHealth();launch(h,d,pig.position().add(-2,.4,0),new Vec3(1,0,0));h.runAfterDelay(8,()->{h.assertTrue(Math.abs(pig.getHealth()-(health-d.getFireballDamage()))<.01F,"updated hatchling ember deals scaled fireball HP");h.assertTrue(pig.isOnFire(),"ember ignites compatible target");h.succeed();});}
    private static void ownerSafe(GameTestHelper h){var d=dragon(h,false);var p=owner(h,d,6);h.assertTrue(d.isProtected(p)&&!d.canAttack(p),"owner immune to targeting");h.assertTrue(!d.validDefensiveTarget(h.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(8,Y+1,6))),"uninvolved peaceful animals are not targeted");h.succeed();}
    private static void alliesSafe(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,5);var pet=h.spawnWithNoFreeWill(EntityType.WOLF,new BlockPos(7,Y+1,6));pet.tame(p);pet.setNoGravity(true);float health=pet.getHealth();
        launch(h,d,pet.position().add(-2,.4,0),new Vec3(1,0,0));h.assertTrue(d.isProtected(pet)&&!d.canAttack(pet),"same owner's pet protected");
        h.runAfterDelay(10,()->{h.assertValueEqual(pet.getHealth(),health,"ember passes ally without damage");h.succeed();});
    }
    private static void cooldown(GameTestHelper h){
        var d=dragon(h,false);owner(h,d,4);var target=hostile(h);d.setTarget(target);
        for(int t=0;t<13;t++)d.getDragonBrain().tick();
        h.assertValueEqual(d.getDragonBrain().getAttackCooldown(),30,"charge releases after twelve ticks then cooldown starts");
        for(int t=0;t<29;t++)d.getDragonBrain().tick();h.assertValueEqual(d.getChargeTicks(),0,"no early next shot");
        d.getDragonBrain().tick();h.assertValueEqual(d.getChargeTicks(),12,"next preparation after thirty ticks");h.succeed();
    }
    private static void immunity(GameTestHelper h){var d=dragon(h,false);h.assertTrue(!d.hurtServer(h.getLevel(),d.damageSources().lava(),8),"fire immune");h.assertTrue(!d.causeFallDamage(40,1,d.damageSources().fall()),"fall immune");h.assertValueEqual(d.getHealth(),d.getMaxHealth(),"updated hatchling health intact");h.succeed();}
    private static void teleport(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,6);Vec3 original=d.position();d.setPos(original.add(30,0,0));h.assertTrue(d.teleportNearOwner(),"distant companion teleports to safe ground");h.assertTrue(d.safeGround(d.position())&&d.distanceToSqr(p)<36,"safe solid destination near owner");
        for(int x=1;x<=11;x++)for(int z=1;z<=11;z++)h.setBlock(new BlockPos(x,Y,z),Blocks.LAVA);
        d.setPos(original.add(30,0,0));h.assertTrue(!d.teleportNearOwner(),"rejects lava destinations");h.succeed();
    }
    private static void sit(GameTestHelper h){var d=dragon(h,true);var p=owner(h,d,8);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);d.mobInteract(p,InteractionHand.MAIN_HAND);Vec3 pos=d.position();h.runAfterDelay(60,()->{h.assertTrue(d.isOrderedToSit()&&d.getDragonState()==DragonState.SITTING,"owner orders rest");h.assertTrue(d.position().distanceToSqr(pos)<.25,"sitting companion stays");h.succeed();});}
    private static void saveLoad(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,5);d.setOrderedToSit(true);d.getDragonBrain().setAttackCooldown(37);d.setHealth(11);var uuid=d.getUUID();
        var loaded=copy(h,d);d.discard();h.getLevel().addFreshEntity(loaded);
        h.assertTrue(loaded.getUUID().equals(uuid)&&loaded.isOwnedBy(p)&&loaded.isOrderedToSit()&&loaded.getHealth()==11&&loaded.getDragonBrain().getAttackCooldown()==37,"save/load retains identity commands cooldown and health");h.succeed();
    }
    private static void bestiary(GameTestHelper h){h.assertTrue(Bestiary.ENTRIES.contains(Bestiary.BABY_DRAGON)&&Bestiary.BABY_DRAGON.pages().size()>=6,"six-page chapter registered");h.succeed();}
    private static void resources(GameTestHelper h){
        var model=new BabyDragonModel(BabyDragonModel.createBodyLayer().bakeRoot());
        h.assertValueEqual(BabyDragonAnimations.ALL.size(),17,"all previous and new animations bake against real skeleton");
        for(var pose:BabyDragon.Pose.values()){var state=new BabyDragonRenderState();state.ageInTicks=8;state.animations.get(pose).start(0);model.setupAnim(state);}
        for(String path:List.of("textures/entity/baby_dragon.png","textures/item/dragon_ember.png","textures/gui/bestiary/baby_dragon_anatomy.png","textures/gui/bestiary/baby_dragon_flight.png","textures/gui/bestiary/baby_dragon_defense.png"))h.assertTrue(h.getLevel().getServer().getResourceManager().getResource(id(path)).isPresent()||BabyDragonGameTests.class.getResource("/assets/codenamecoraline/"+path)!=null,"packaged asset: "+path);
        h.succeed();
    }
    private static void wall(GameTestHelper h){
        var d=dragon(h,false);var pig=h.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(9,Y+1,6));pig.setNoGravity(true);float health=pig.getHealth();
        h.setBlock(new BlockPos(7,Y+1,6),Blocks.OAK_PLANKS);h.setBlock(new BlockPos(7,Y+2,6),Blocks.OAK_PLANKS);
        launch(h,d,h.absoluteVec(new Vec3(5,Y+1.4,6.5)),new Vec3(1,0,0));
        h.runAfterDelay(12,()->{h.assertValueEqual(pig.getHealth(),health,"wall stops projectile");h.assertTrue(h.getBlockState(new BlockPos(7,Y+1,6)).is(Blocks.OAK_PLANKS)&&h.getBlockState(new BlockPos(7,Y+2,6)).is(Blocks.OAK_PLANKS),"wood never burns or breaks");h.succeed();});
    }
    private static void defenseToggle(GameTestHelper h){var d=dragon(h,false);var p=owner(h,d,5);p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));d.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(!d.isDefensive()&&!copy(h,d).isDefensive(),"defense command persisted");d.setTarget(hostile(h));d.getDragonBrain().tick();h.assertTrue(d.getTarget()==null,"disabled defense clears target");h.succeed();}
    private static void unsafeExit(GameTestHelper h){var d=dragon(h,true);var p=owner(h,d,5);d.landOnShoulder(p);p.setPos(p.position().add(0,0,1));h.setBlock(BlockPos.containing(BabyDragon.shoulderPosition(p)).subtract(h.absolutePos(BlockPos.ZERO)),Blocks.STONE);h.runAfterDelay(3,()->{h.assertTrue(!d.isOnShoulder()&&d.clearBlocks(d.position()),"blocked shoulder releases safely");h.succeed();});}
    private static void flightWall(GameTestHelper h){var d=dragon(h,false);d.setFlying(true);for(int y=Y+1;y<=Y+5;y++)for(int z=0;z<=12;z++)h.setBlock(new BlockPos(5,y,z),Blocks.STONE);d.setDeltaMovement(3,0,0);d.travel(Vec3.ZERO);h.assertTrue(d.getX()<h.absolutePos(new BlockPos(5,Y+1,6)).getX(),"physical flight cannot cross solid wall");h.succeed();}
    private static void emberRange(GameTestHelper h){
        var d=dragon(h,false);var ember=new DragonEmber(h.getLevel(),d);Vec3 origin=d.position().add(0,3,0);ember.setPos(origin);ember.shoot(0,1,0,DragonEmber.SPEED,0);h.getLevel().addFreshEntity(ember);
        h.runAfterDelay(18,()->{h.assertTrue(ember.isRemoved(),"ember expires at twelve-block range");h.assertTrue(ember.position().distanceToSqr(origin)<=144.01,"never travels beyond attack range");h.succeed();});
    }
    private static void selfDefense(GameTestHelper h){
        var d=dragon(h,true);var attacker=hostile(h);
        h.assertTrue(!d.validDefensiveTarget(h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(6,Y+1,4))),"uninvolved villager excluded");
        h.runAfterDelay(2,()->d.hurtServer(h.getLevel(),d.damageSources().mobAttack(attacker),1));
        h.runAfterDelay(10,()->{h.assertTrue(d.getTarget()==attacker,"wild dragon reacts to its own attacker");h.succeed();});
    }
    private static void neutralCombat(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,5);var golem=h.spawnWithNoFreeWill(EntityType.IRON_GOLEM,new BlockPos(8,Y+1,6));
        h.assertTrue(!d.validDefensiveTarget(golem),"uninvolved neutral mob is never a target");
        p.setLastHurtMob(golem);h.assertTrue(d.validDefensiveTarget(golem),"owner may direct defense against involved neutral mob");
        var pig=h.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(8,Y+1,4));p.setLastHurtMob(pig);
        h.assertTrue(d.validDefensiveTarget(pig),"owner strike permits targeting a peaceful animal");
        var pet=h.spawnWithNoFreeWill(EntityType.WOLF,new BlockPos(6,Y+1,4));pet.tame(p);p.setLastHurtMob(pet);
        h.assertTrue(!d.validDefensiveTarget(pet),"owner strike never overrides companion protection");h.succeed();
    }
    private static void shoulderNoImpulse(GameTestHelper h){
        var d=dragon(h,true);var p=owner(h,d,5);
        h.assertTrue(d.landOnShoulder(p),"lands on owner");
        p.setDeltaMovement(Vec3.ZERO);
        for(int tick=1;tick<30;tick++)h.runAfterDelay(tick,()->{
            h.assertTrue(d.isOnShoulder(),"remains perched during idle observation");
            h.assertTrue(p.getDeltaMovement().horizontalDistanceSqr()<1.0E-10,"perched dragon never accelerates owner");
        });
        h.runAfterDelay(30,()->{
            d.push(p);
            h.assertTrue(p.getDeltaMovement().horizontalDistanceSqr()<1.0E-10,"direct collision cannot push owner while perched");
            d.leaveShoulder();d.setPos(p.position().add(.4,0,0));d.push(p);
            h.assertTrue(p.getDeltaMovement().horizontalDistanceSqr()>0,"normal entity pushing resumes after leaving shoulder");
            h.succeed();
        });
    }
    private static void animationPackets(GameTestHelper h){
        var d=dragon(h,false);
        for(byte event:new byte[]{BabyDragon.EVENT_TAKEOFF,BabyDragon.EVENT_LANDING,BabyDragon.EVENT_CHARGE,
                BabyDragon.EVENT_SHOOT,BabyDragon.EVENT_HURT,BabyDragon.EVENT_HAPPY,BabyDragon.EVENT_CANCEL_CHARGE}){
            var packet=new net.minecraft.network.protocol.game.ClientboundEntityEventPacket(d,event);
            // These IDs are intercepted before Entity.handleEntityEvent in ClientPacketListener.
            h.assertTrue(packet.getEventId()!=21&&packet.getEventId()!=35&&packet.getEventId()!=63,
                    "dragon animation must reach entity dispatch instead of a vanilla special handler");
            d.handleEntityEvent(packet.getEventId());
        }
        d.handleEntityEvent(BabyDragon.EVENT_CHARGE);h.assertValueEqual(d.getChargeTicks(),12,"charge starts on packet dispatch");
        d.handleEntityEvent(BabyDragon.EVENT_SHOOT);h.assertValueEqual(d.getChargeTicks(),0,"shot ends charge on packet dispatch");
        h.succeed();
    }
    private static void ownerAttackResponse(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,8);var target=hostile(h);
        d.setPos(p.position().add(-15,0,0));
        h.runAfterDelay(2,()->{
            p.setLastHurtMob(target);d.getDragonBrain().tick();
            h.assertTrue(d.getTarget()==target,"owner strike acquired immediately even when dragon is outside firing range");
            d.setPos(target.position().add(-5,2,0));
            for(int i=0;i<14;i++)d.getDragonBrain().tick();
            h.assertTrue(d.getDragonBrain().getAttackCooldown()>0,"approaching dragon fires without requiring another owner strike");h.succeed();
        });
    }
    private static void movingTargetHit(GameTestHelper h){
        var d=dragon(h,false);var target=hostile(h);Vec3 start=target.position();
        target.setPos(start.add(0,0,-2));target.setDeltaMovement(0,0,.25);
        d.setPos(target.position().add(-7,1,0));float health=target.getHealth();
        d.shoot(target);
        for(int tick=1;tick<=10;tick++){
            final int step=tick;
            h.runAfterDelay(tick,()->{target.setPos(start.add(0,0,-2+.25*step));target.setDeltaMovement(0,0,.25);});
        }
        h.runAfterDelay(11,()->{h.assertTrue(target.getHealth()<health,"lead shot hits a target moving across the firing line");h.succeed();});
    }
    private static void ownerAttacksAnimal(GameTestHelper h,EntityType<? extends net.minecraft.world.entity.Mob> type){
        var d=dragon(h,true);var p=owner(h,d,5);
        var animal=h.spawnWithNoFreeWill(type,new BlockPos(8,Y+1,6));animal.setNoGravity(true);
        animal.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);animal.setHealth(40);
        h.assertTrue(!d.validDefensiveTarget(animal),"peaceful animal ignored before owner strike");
        float[] afterStrike={0};
        h.runAfterDelay(2,()->{p.attack(animal);afterStrike[0]=animal.getHealth();
            h.assertTrue(afterStrike[0]<40,"real owner attack damages animal");});
        h.runAfterDelay(5,()->h.assertTrue(d.getTarget()==animal,"dragon assists against animal struck by owner"));
        h.runAfterDelay(60,()->{h.assertTrue(animal.getHealth()<afterStrike[0],"dragon projectile damages owner's animal target");h.succeed();});
    }
    private static void fireCooksMeatAllStages(GameTestHelper h){
        var d=dragon(h,false);d.setNoAi(true);d.setNoGravity(true);var p=owner(h,d,2);
        // 1. BABY dragon ember kills a Chicken (4 HP) -> drops COOKED_CHICKEN, never raw CHICKEN
        var chicken=h.spawnWithNoFreeWill(EntityType.CHICKEN,new BlockPos(6,Y+1,4));chicken.setNoGravity(true);
        launch(h,d,chicken.position().add(-1.5,.2,0),new Vec3(1,0,0));
        // 2. YOUNG dragon ember kills a Pig (10 HP < 14 fireball) -> drops COOKED_PORKCHOP, and splash ignites nearby Cow
        var dYoung=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(3,Y+1,6));dYoung.setNoAi(true);dYoung.setNoGravity(true);dYoung.tame(p);
        h.assertTrue(dYoung.setDragonStage(DragonStage.YOUNG,false),"young stage");
        var pig=h.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(6,Y+1,6));pig.setNoGravity(true);
        var splashCow=h.spawnWithNoFreeWill(EntityType.COW,new BlockPos(6,Y+1,7));splashCow.setNoGravity(true);p.setLastHurtMob(splashCow);
        launch(h,dYoung,pig.position().add(-1.8,.4,0),new Vec3(1,0,0));
        // 3. ADULT dragon ember kills a Cow (10 HP < 30 fireball) -> drops COOKED_BEEF
        var dAdult=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(3,Y+1,9));dAdult.setNoAi(true);dAdult.setNoGravity(true);dAdult.tame(p);
        h.assertTrue(dAdult.setDragonStage(DragonStage.ADULT,false),"adult stage");
        var cow=h.spawnWithNoFreeWill(EntityType.COW,new BlockPos(8,Y+1,9));cow.setNoGravity(true);
        launch(h,dAdult,cow.position().add(-2.2,.6,0),new Vec3(1,0,0));
        h.runAfterDelay(12,()->{
            h.assertTrue(!chicken.isAlive(),"baby ember killed chicken");
            h.assertTrue(!pig.isAlive(),"young ember killed pig");
            h.assertTrue(!cow.isAlive(),"adult ember killed cow");
            h.assertTrue(splashCow.isOnFire(),"young ember splash ignited nearby valid target");
            var items=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(h.absolutePos(new BlockPos(0,Y,0))).inflate(14));
            h.assertTrue(items.stream().anyMatch(e->e.isAlive()&&e.getItem().is(Items.COOKED_CHICKEN)),"cooked chicken dropped and intact");
            h.assertTrue(items.stream().noneMatch(e->e.getItem().is(Items.CHICKEN)),"no raw chicken dropped");
            h.assertTrue(items.stream().anyMatch(e->e.isAlive()&&e.getItem().is(Items.COOKED_PORKCHOP)),"cooked porkchop dropped and intact");
            h.assertTrue(items.stream().noneMatch(e->e.getItem().is(Items.PORKCHOP)),"no raw porkchop dropped");
            h.assertTrue(items.stream().anyMatch(e->e.isAlive()&&e.getItem().is(Items.COOKED_BEEF)),"cooked beef dropped and intact");
            h.assertTrue(items.stream().noneMatch(e->e.getItem().is(Items.BEEF)),"no raw beef dropped");
            h.succeed();
        });
    }
    private static void breathAndMagmaCookAndPreserveDrops(GameTestHelper h){
        var d=dragon(h,false);d.setNoAi(true);d.setNoGravity(true);var p=owner(h,d,2);
        h.assertTrue(d.setDragonStage(DragonStage.ADULT,false),"adult stage");
        d.setYRot(-90);d.setYHeadRot(-90);d.yBodyRot=-90;d.setXRot(0);
        // Place a Chicken with 4 HP in the breath cone (killed on the very first 5-HP breath pulse)
        var chicken=h.spawnWithNoFreeWill(EntityType.CHICKEN,new BlockPos(9,Y+1,6));chicken.setNoGravity(true);
        p.setLastHurtMob(chicken);
        d.aimAt(chicken.getBoundingBox().getCenter());
        d.applyBreathDamage();
        h.assertTrue(!chicken.isAlive(),"lethal first breath pulse killed chicken");
        var box=chicken.getBoundingBox().inflate(3);
        var dropsAfterKill=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box);
        h.assertTrue(dropsAfterKill.stream().anyMatch(e->e.isAlive()&&e.getItem().is(Items.COOKED_CHICKEN)),"lethal first breath pulse cooked chicken");
        h.assertTrue(dropsAfterKill.stream().noneMatch(e->e.getItem().is(Items.CHICKEN)),"no raw chicken from lethal breath");
        // Apply additional breath pulses and projectile over the drops to prove they are not destroyed
        d.applyBreathDamage();
        launch(h,d,chicken.position().add(-2,.3,0),new Vec3(1,0,0));
        // Also test DragonMagmaTrail cooking and not overwriting ItemEntity positions on valid stone ground
        BlockPos floorDropPos=h.absolutePos(new BlockPos(6,Y+1,4));
        dropsAfterKill.getFirst().setPos(Vec3.atBottomCenterOf(floorDropPos));
        var journal=new DragonMagmaTrail();
        h.assertTrue(!journal.place(h.getLevel(),floorDropPos,d),"magma trail never places solid block over dropped items on stone");
        BlockPos magmaPos=h.absolutePos(new BlockPos(6,Y+1,3));
        h.assertTrue(journal.place(h.getLevel(),magmaPos,d),"magma placed on clear stone");
        var sheep=h.spawnWithNoFreeWill(EntityType.SHEEP,new BlockPos(6,Y+1,3));sheep.setNoGravity(true);sheep.setHealth(1.5F);
        p.setLastHurtMob(sheep);
        d.damageEnemyWithFire(sheep,2,2);
        h.assertTrue(!sheep.isAlive(),"magma fire damage killed low-HP sheep");
        h.runAfterDelay(10,()->{
            h.assertTrue(dropsAfterKill.stream().allMatch(Entity::isAlive),"drops survive subsequent breath and ember attacks");
            var sheepDrops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,sheep.getBoundingBox().inflate(3));
            h.assertTrue(sheepDrops.stream().anyMatch(e->e.isAlive()&&e.getItem().is(Items.COOKED_MUTTON)),"cooked mutton dropped from magma kill");
            h.assertTrue(sheepDrops.stream().noneMatch(e->e.getItem().is(Items.MUTTON)),"no raw mutton dropped");
            h.succeed();
        });
    }
    private static void fireRespectsImmuneAndInvulnerable(GameTestHelper h){
        var d=dragon(h,false);d.setNoAi(true);d.setNoGravity(true);var p=owner(h,d,2);
        var blaze=h.spawnWithNoFreeWill(EntityType.BLAZE,new BlockPos(7,Y+1,5));blaze.setNoGravity(true);
        float blazeHp=blaze.getHealth();
        h.assertTrue(d.damageEnemyWithFire(blaze,6,4),"fire-immune enemy still takes physical impact damage");
        h.assertValueEqual(blaze.getHealth(),blazeHp-6,"blaze took six damage");
        h.assertTrue(!blaze.isOnFire(),"fire-immune blaze is not ignited");
        var invulnPig=h.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(7,Y+1,7));invulnPig.setNoGravity(true);invulnPig.setInvulnerable(true);
        h.assertTrue(!d.damageEnemyWithFire(invulnPig,6,4),"invulnerable target rejects damage");
        h.assertTrue(!invulnPig.isOnFire(),"invulnerable target is not ignited when damage fails");
        var allyWolf=h.spawnWithNoFreeWill(EntityType.WOLF,new BlockPos(7,Y+1,8));allyWolf.tame(p);allyWolf.setNoGravity(true);
        h.assertTrue(!d.damageEnemyWithFire(allyWolf,6,4),"ally wolf rejected");
        h.assertTrue(!allyWolf.isOnFire(),"ally wolf never ignited");
        h.succeed();
    }
    private static void individualTraitsGenerationAndPersistence(GameTestHelper h){
        var d=dragon(h,false);
        float intel=d.getIntelligence(),vit=d.getVitality(),str=d.getStrength(),agi=d.getAgility();
        h.assertTrue(d.areTraitsInitialized(),"traits initialized once at spawn");
        // Verify distinct dragons roll varied profiles
        boolean foundDifferent=false;
        for(int i=0;i<6;i++){
            var other=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(4+i,Y+1,3));
            if(other.getIntelligence()!=intel||other.getVitality()!=vit||other.getStrength()!=str||other.getAgility()!=agi)foundDifferent=true;
            other.discard();
        }
        h.assertTrue(foundDifferent,"separate dragons generate distinct individual traits");
        // Verify taming never rerolls traits
        Player p=h.makeMockPlayer(GameType.SURVIVAL);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SALMON,64));
        d.getRandom().setSeed(31415);
        while(!d.isTame())d.mobInteract(p,InteractionHand.MAIN_HAND);
        h.assertValueEqual(d.getIntelligence(),intel,"intelligence unchanged after taming");
        h.assertValueEqual(d.getVitality(),vit,"vitality unchanged after taming");
        h.assertValueEqual(d.getStrength(),str,"strength unchanged after taming");
        h.assertValueEqual(d.getAgility(),agi,"agility unchanged after taming");
        // Verify stage evolution never overwrites traits and scales attributes consistently
        for(DragonStage s:DragonStage.values()){
            h.assertTrue(d.setDragonStage(s,false),"evolve to "+s);
            h.assertValueEqual(d.getIntelligence(),intel,"intelligence preserved at "+s);
            h.assertValueEqual(d.getVitality(),vit,"vitality preserved at "+s);
            h.assertValueEqual(d.getStrength(),str,"strength preserved at "+s);
            h.assertValueEqual(d.getAgility(),agi,"agility preserved at "+s);
            h.assertTrue(Math.abs(d.getMaxHealth()-s.health*vit)<.01F,"max health scaled at "+s);
            h.assertTrue(Math.abs(d.getAttributeValue(Attributes.ATTACK_DAMAGE)-s.melee*str)<.01D,"melee scaled at "+s);
            h.assertTrue(Math.abs(d.getAttributeValue(Attributes.FLYING_SPEED)-s.flightSpeed*agi)<.001D,"flight speed scaled at "+s);
        }
        // Verify save/load persistence
        var loaded=copy(h,d);
        h.assertValueEqual(loaded.getIntelligence(),intel,"intelligence survives reload");
        h.assertValueEqual(loaded.getVitality(),vit,"vitality survives reload");
        h.assertValueEqual(loaded.getStrength(),str,"strength survives reload");
        h.assertValueEqual(loaded.getAgility(),agi,"agility survives reload");
        h.assertTrue(Math.abs(loaded.getMaxHealth()-DragonStage.ADULT.health*vit)<.01F,"adult max health survives reload");
        h.succeed();
    }
    private static void individualTraitsLegacySaveMigration(GameTestHelper h){
        var d=dragon(h,false);owner(h,d,5);
        h.assertTrue(d.setDragonStage(DragonStage.YOUNG,false),"young stage");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());
        d.saveWithoutId(out);
        var compound=out.buildResult();
        // Simulate an existing world save from before Etapa 2 by removing the trait tags
        compound.remove("DragonTraitsInitialized");
        compound.remove("DragonIntelligence");
        compound.remove("DragonVitality");
        compound.remove("DragonStrength");
        compound.remove("DragonAgility");
        compound.putFloat("Health",80F);
        BabyDragon legacy=ModEntities.BABY_DRAGON.get().create(h.getLevel(),EntitySpawnReason.LOAD);
        legacy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),compound));
        h.assertValueEqual(legacy.getIntelligence(),BabyDragon.DEFAULT_TRAIT,"legacy dragon defaults to 1.0 intelligence");
        h.assertValueEqual(legacy.getVitality(),BabyDragon.DEFAULT_TRAIT,"legacy dragon defaults to 1.0 vitality");
        h.assertValueEqual(legacy.getStrength(),BabyDragon.DEFAULT_TRAIT,"legacy dragon defaults to 1.0 strength");
        h.assertValueEqual(legacy.getAgility(),BabyDragon.DEFAULT_TRAIT,"legacy dragon defaults to 1.0 agility");
        h.assertValueEqual(legacy.getMaxHealth(),DragonStage.YOUNG.health,"legacy young dragon retains exact 80 max health");
        h.assertValueEqual(legacy.getHealth(),80F,"legacy young dragon retains exact 80 health");
        h.succeed();
    }
    private static void individualTraitsBehavioralImpact(GameTestHelper h){
        var low=dragon(h,false);var high=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(5,Y+1,6));
        low.setIndividualTraits(BabyDragon.MIN_TRAIT,BabyDragon.MIN_TRAIT,BabyDragon.MIN_TRAIT,BabyDragon.MIN_TRAIT);
        high.setIndividualTraits(BabyDragon.MAX_TRAIT,BabyDragon.MAX_TRAIT,BabyDragon.MAX_TRAIT,BabyDragon.MAX_TRAIT);
        h.assertTrue(high.getMaxHealth()>low.getMaxHealth(),"high vitality increases max health");
        h.assertTrue(high.getAttributeValue(Attributes.ARMOR)>low.getAttributeValue(Attributes.ARMOR),"high vitality increases armor");
        h.assertTrue(high.getFireballDamage()>low.getFireballDamage(),"high strength increases fireball damage");
        h.assertTrue(high.getBreathDamage()>low.getBreathDamage(),"high strength increases breath damage");
        h.assertTrue(high.getAttributeValue(Attributes.ATTACK_DAMAGE)>low.getAttributeValue(Attributes.ATTACK_DAMAGE),"high strength increases melee damage");
        h.assertTrue(high.getAttributeValue(Attributes.FLYING_SPEED)>low.getAttributeValue(Attributes.FLYING_SPEED),"high agility increases flying speed");
        h.assertTrue(high.getAttributeValue(Attributes.MOVEMENT_SPEED)>low.getAttributeValue(Attributes.MOVEMENT_SPEED),"high agility increases ground speed");
        h.assertTrue(high.getAttributeValue(Attributes.FOLLOW_RANGE)>low.getAttributeValue(Attributes.FOLLOW_RANGE),"high intelligence increases perception range");
        h.succeed();
    }
    private static void postTameNamingAndSkip(GameTestHelper h){
        var d=dragon(h,false);
        Player p=h.makeMockPlayer(GameType.SURVIVAL);
        p.setPos(d.position().add(1,0,0));
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SALMON,64));
        d.getRandom().setSeed(31415);
        int attempts=0;
        while(!d.isTame()&&attempts<64){d.mobInteract(p,InteractionHand.MAIN_HAND);attempts++;}
        h.assertTrue(d.isTame()&&d.wasNamePromptRequested(),"successful taming triggers post-tame naming prompt");
        // Test Skip ("Pular") leaves default name intact
        h.assertTrue(d.acceptOwnerName(p,"   "),"skipping with blank name is accepted");
        h.assertTrue(!d.wasNamePromptRequested()&&!d.hasCustomName(),"skip clears prompt and keeps default name");
        // Test Confirm ("Confirmar") trims and applies custom name
        h.assertTrue(d.acceptOwnerName(p,"  Ignis  "),"owner confirms valid name");
        h.assertTrue(d.hasCustomName()&&d.getName().getString().equals("Ignis"),"custom name trimmed and applied");
        // Verify name survives stage evolution and reload
        h.assertTrue(d.setDragonStage(DragonStage.YOUNG,false),"evolves to young");
        h.assertValueEqual(d.getName().getString(),"Ignis","custom name preserved on young stage");
        h.assertTrue(d.setDragonStage(DragonStage.ADULT,false),"evolves to adult");
        h.assertValueEqual(d.getName().getString(),"Ignis","custom name preserved on adult stage");
        var loaded=copy(h,d);
        h.assertTrue(loaded.hasCustomName()&&loaded.getName().getString().equals("Ignis"),"custom name survives NBT reload");
        h.succeed();
    }
    private static void namingSecurityValidation(GameTestHelper h){
        var wild=dragon(h,false);
        Player p=h.makeMockPlayer(GameType.SURVIVAL);
        p.setPos(wild.position().add(1,0,0));
        h.assertTrue(!wild.acceptOwnerName(p,"Selvagem"),"wild dragon rejects naming");
        var d=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(5,Y+1,6));
        d.tame(p);
        Player stranger=h.makeMockPlayer(GameType.SURVIVAL);
        stranger.setPos(d.position().add(1,0,0));
        h.assertTrue(!d.acceptOwnerName(stranger,"Intruso")&&!d.hasCustomName(),"non-owner rejected");
        h.assertTrue(!d.acceptOwnerName(p,"A".repeat(BabyDragon.MAX_CUSTOM_NAME_LENGTH+1)),"oversized name rejected");
        h.assertTrue(!d.acceptOwnerName(p,"Ignis\nMalicioso"),"control characters rejected");
        h.assertTrue(!d.acceptOwnerName(p,"§4ColorExploit"),"section sign formatting rejected");
        h.assertTrue(d.acceptOwnerName(p,"Draco"),"valid name accepted");
        p.setPos(d.position().add(20,0,0));
        h.assertTrue(!d.acceptOwnerName(p,"Longe")&&d.getName().getString().equals("Draco"),"distant owner rejected without overwriting name");
        h.succeed();
    }
    private static void textureVariantsPersistence(GameTestHelper h){
        var d=dragon(h,false);
        java.util.Set<Integer> seen=new java.util.HashSet<>();
        seen.add(d.getTextureVariant());
        for(int i=0;i<24;i++){
            BabyDragon extra=ModEntities.BABY_DRAGON.get().create(h.getLevel(),EntitySpawnReason.NATURAL);
            h.assertTrue(extra.getTextureVariant()>=0&&extra.getTextureVariant()<BabyDragon.VARIANT_COUNT,"variant in valid range 0..5");
            seen.add(extra.getTextureVariant());
        }
        h.assertTrue(seen.size()>=3,"multiple distinct texture variants rolled across population");
        d.setTextureVariant(4);
        var p=owner(h,d,5);
        h.assertValueEqual(d.getTextureVariant(),4,"taming preserves texture variant");
        h.assertTrue(d.setDragonStage(DragonStage.YOUNG,false),"evolves to young");
        h.assertValueEqual(d.getTextureVariant(),4,"young evolution preserves texture variant");
        h.assertTrue(d.setDragonStage(DragonStage.ADULT,false),"evolves to adult");
        h.assertValueEqual(d.getTextureVariant(),4,"adult evolution preserves texture variant");
        var loaded=copy(h,d);
        h.assertValueEqual(loaded.getTextureVariant(),4,"NBT reload preserves texture variant");
        h.succeed();
    }
    private static void combatRetainsTargetBeyond240Ticks(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,4);
        var target=hostile(h);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);target.setHealth(500);
        d.setTarget(target);
        for(int t=0;t<300;t++)d.getDragonBrain().tick();
        h.assertTrue(d.getTarget()==target&&d.getDragonBrain().getCombatTicks()>=300,"combat does not drop valid visible target after 240 ticks");
        // Place solid obstacles around one half of the target's orbit and verify combat positioning picks clear space
        for(int y=Y+1;y<=Y+5;y++)for(int z=2;z<=10;z++)h.setBlock(new BlockPos(11,y,z),Blocks.STONE);
        for(int i=0;i<16;i++){
            Vec3 pos=d.getDragonBrain().selectCombatPosition(target);
            h.assertTrue(d.clearBlocks(pos),"combat orbit avoids solid obstacles");
        }
        h.succeed();
    }
    private static void staySelfDefenseAndReturnToAnchor(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,4);
        Vec3 post=d.position();
        h.assertTrue(d.issueCommand(BabyDragon.Command.STAY,p),"stay command issued");
        h.assertTrue(d.isOrderedToSit()&&d.getActiveCommand()==BabyDragon.Command.STAY&&d.getStayAnchor()!=null,"stay anchor recorded");
        var threat=h.spawnWithNoFreeWill(EntityType.HUSK,new BlockPos(6,Y+1,6));threat.setNoGravity(true);
        d.setLastHurtByMob(threat);
        d.getDragonBrain().tick();
        h.assertTrue(d.getTarget()==threat&&d.getDragonState()==DragonState.DEFENDING&&d.isOrderedToSit(),"sitting dragon defends against immediate attacker");
        // Threat eliminated while dragon is displaced 3 blocks from its stay anchor
        threat.discard();
        d.setPos(post.add(3,1,0));
        d.getDragonBrain().tick();
        h.assertTrue(d.getTarget()==null&&d.getDragonState()==DragonState.RETURNING,"after threat ends, dragon returns toward stay anchor");
        d.setPos(post);d.setFlying(false);
        d.getDragonBrain().tick();
        h.assertTrue(d.getDragonState()==DragonState.SITTING&&d.isOrderedToSit(),"dragon resumes sitting at original anchor");
        h.succeed();
    }
    private static void commandsReturnProtectFollowAndRescue(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,3);
        var target=hostile(h);
        d.setTarget(target);
        d.setPos(h.absoluteVec(new Vec3(10.5,Y+2,6.5)));
        h.assertTrue(d.issueCommand(BabyDragon.Command.RETURN,p),"return command accepted");
        h.assertTrue(d.isReturnRequested()&&d.getActiveCommand()==BabyDragon.Command.RETURN,"active command is RETURN");
        var savedReturn=copy(h,d);
        h.assertTrue(savedReturn.isReturnRequested(),"return command state persists across NBT reload");
        d.getDragonBrain().tick();
        h.assertValueEqual(d.getDragonState(),DragonState.RETURNING,"return command prioritizes flying back to owner");
        d.setPos(p.position().add(1,0,0));
        d.getDragonBrain().tick();
        h.assertTrue(!d.isReturnRequested()&&d.getActiveCommand()==BabyDragon.Command.PROTECT,"arriving near owner completes return and restores protect mode");
        h.assertTrue(d.issueCommand(BabyDragon.Command.FOLLOW,p)&&!d.isDefensive()&&d.getActiveCommand()==BabyDragon.Command.FOLLOW,"follow command disables proactive defense");
        h.assertTrue(d.issueCommand(BabyDragon.Command.PROTECT,p)&&d.isDefensive()&&d.getActiveCommand()==BabyDragon.Command.PROTECT,"protect command enables active defense");
        // Test adult dragon rescuing falling owner mid-air
        h.assertTrue(d.setDragonStage(DragonStage.ADULT,false),"evolves to adult");
        p.setPos(d.position().add(1,2,0));
        p.setOnGround(false);
        p.fallDistance=8.0F;
        d.getDragonBrain().tick();
        h.assertTrue(d.getRider()==p&&p.fallDistance==0.0F,"adult dragon intercepts and mounts falling owner");
        p.stopRiding();
        h.succeed();
    }
    private static void flightPhysicsDiveClimbBanking(GameTestHelper h){
        var levelDragon=dragon(h,false);var p1=owner(h,levelDragon,4);
        h.assertTrue(levelDragon.setDragonStage(DragonStage.ADULT,false),"adult stage");
        levelDragon.setIndividualTraits(1.0F,1.0F,1.0F,1.0F);
        levelDragon.setPos(h.absoluteVec(new Vec3(3.5,Y+4,6.5)));
        p1.setPos(levelDragon.position());
        h.assertTrue(levelDragon.mountOwner(p1),"owner mounts level dragon");
        levelDragon.setFlying(true);
        levelDragon.setYRot(-90);
        levelDragon.acceptRiderInput(p1,1,-90,0);
        for(int i=0;i<8;i++)levelDragon.travel(Vec3.ZERO);
        double levelHorizSpeed=levelDragon.getDeltaMovement().horizontalDistance();
        p1.stopRiding();
        levelDragon.discard();

        var diveDragon=dragon(h,false);var p2=owner(h,diveDragon,4);
        h.assertTrue(diveDragon.setDragonStage(DragonStage.ADULT,false),"adult stage");
        diveDragon.setIndividualTraits(1.0F,1.0F,1.0F,1.25F);
        diveDragon.setPos(h.absoluteVec(new Vec3(3.5,Y+5,6.5)));
        p2.setPos(diveDragon.position());
        h.assertTrue(diveDragon.mountOwner(p2),"owner mounts dive dragon");
        diveDragon.setFlying(true);
        diveDragon.setYRot(-90);
        // Pitch down (+40 deg) and turn toward -30 deg while flying forward to build dive momentum and banking roll
        diveDragon.acceptRiderInput(p2,1,-30,40);
        for(int i=0;i<8;i++)diveDragon.travel(Vec3.ZERO);
        h.assertTrue(diveDragon.getFlightMomentum()>0.1F,"diving builds flight momentum");
        h.assertTrue(diveDragon.getDeltaMovement().horizontalDistance()>levelHorizSpeed,"dive + agility accelerates beyond flat cruise speed");
        h.assertTrue(Math.abs(diveDragon.getFlightRoll())>1.0F,"turning in flight produces banking roll angle");
        h.assertTrue(diveDragon.getFlightPitch()>2.0F,"diving pitches dragon body downward");
        // Verify DragonModel applies banking roll and pitch cleanly
        var model=new AdultDragonModel(AdultDragonModel.createBodyLayer().bakeRoot());
        var state=new BabyDragonRenderState();
        state.flightBlend=1.0F;state.flightRoll=diveDragon.getFlightRoll();state.flightPitch=diveDragon.getFlightPitch();state.glideFactor=diveDragon.getGlideFactor();
        model.setupAnim(state);
        p2.stopRiding();
        h.succeed();
    }
    private static void advancedCombatThreatAndPositioning(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,4);
        h.assertTrue(d.setDragonStage(DragonStage.YOUNG,false),"young stage");
        var farEnemy=h.spawnWithNoFreeWill(EntityType.HUSK,new BlockPos(10,Y+1,9));farEnemy.setNoGravity(true);
        var aggressiveNearEnemy=h.spawnWithNoFreeWill(EntityType.HUSK,new BlockPos(5,Y+1,6));aggressiveNearEnemy.setNoGravity(true);
        aggressiveNearEnemy.setHealth(5.0F);
        p.setLastHurtByMob(aggressiveNearEnemy);
        double nearScore=d.getDragonBrain().evaluateThreatPriority(p,aggressiveNearEnemy);
        double farScore=d.getDragonBrain().evaluateThreatPriority(p,farEnemy);
        h.assertTrue(nearScore>farScore,"tactical threat evaluation prioritizes aggressive close low-HP threat to owner");
        // Place lava floor on one side of the target and verify combat position avoids hazardous floor
        h.setBlock(new BlockPos(8,Y,6),Blocks.LAVA);
        Vec3 pos=d.getDragonBrain().selectCombatPosition(farEnemy);
        h.assertTrue(d.clearBlocks(pos),"selected combat position is clear of obstacles and liquids");
        h.succeed();
    }
    private static void saddleBackpackAndLegacyInventory(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,4);
        // Rule verification: adult dragon can be mounted WITHOUT a saddle
        h.assertTrue(d.setDragonStage(DragonStage.ADULT,false),"adult stage");
        h.assertTrue(!d.isSaddled()&&d.mountOwner(p),"adult dragon can be mounted without a saddle");
        p.stopRiding();
        h.assertTrue(d.setDragonStage(DragonStage.BABY,false),"back to baby stage");
        h.assertValueEqual(d.getActiveCargoSlots(),0,"unsaddled baby has 0 active cargo slots");
        // Equip saddle on baby dragon -> unlocks 9 backpack cargo slots and totem slot
        h.assertTrue(d.equipSaddle(new ItemStack(Items.SADDLE)),"saddle equipped on tamed dragon");
        h.assertTrue(d.isSaddled()&&d.getActiveCargoSlots()==9,"saddled baby unlocks 9 backpack slots");
        var menu=new com.codenamecoraline.inventory.DragonMenu(1,p.getInventory(),d);
        h.assertValueEqual(menu.slots.stream().limit(18).filter(net.minecraft.world.inventory.Slot::isActive).count(),9L,"menu exposes 9 active cargo slots on saddled baby");
        h.assertTrue(menu.getSlot(com.codenamecoraline.inventory.DragonMenu.MENU_SLOT_TOTEM).isActive(),"totem slot active when saddled");
        // Place item in bonus backpack slot 0 and verify saddle cannot be unequipped while backpack is non-empty
        d.getDragonInventory().setItem(0,new ItemStack(Items.DIAMOND,3));
        h.assertTrue(!d.canUnequipSaddle()&&!menu.getSlot(com.codenamecoraline.inventory.DragonMenu.MENU_SLOT_SADDLE).mayPickup(p),"cannot unequip saddle while backpack holds items");
        d.getDragonInventory().setItem(0,ItemStack.EMPTY);
        h.assertTrue(d.canUnequipSaddle(),"can unequip saddle once bonus slots and totem are empty");
        // Equip totem and verify persistence + legacy save compatibility
        h.assertTrue(d.equipTotem(new ItemStack(Items.TOTEM_OF_UNDYING)),"totem equipped in saddle slot");
        h.assertTrue(d.hasEquippedTotem()&&!d.canUnequipSaddle(),"cannot unequip saddle while totem is equipped");
        d.getDragonInventory().setItem(0,new ItemStack(Items.EMERALD,5));
        var reloaded=copy(h,d);
        h.assertTrue(reloaded.isSaddled()&&reloaded.hasEquippedTotem(),"saddle and totem persist across NBT reload");
        h.assertValueEqual(reloaded.getDragonInventory().getItem(0).getCount(),5,"cargo slot 0 preserved without index shift");
        h.succeed();
    }
    private static void totemOfUndyingOwnerProtection(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,4);
        p.setGameMode(GameType.SURVIVAL);
        p.setHealth(10.0F);
        h.assertTrue(d.equipSaddle(new ItemStack(Items.SADDLE)),"equip saddle");
        h.assertTrue(d.equipTotem(new ItemStack(Items.TOTEM_OF_UNDYING)),"equip totem");
        h.assertTrue(d.canProtectOwnerWithTotem(p),"dragon ready to protect owner with totem");
        // Deal fatal combat damage (50 HP) to owner within 32 blocks
        boolean hurt=p.hurtServer(h.getLevel(),d.damageSources().magic(),50.0F);
        h.assertTrue(hurt&&p.isAlive()&&p.getHealth()>=1.0F,"owner survives fatal damage via dragon's equipped totem");
        h.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)&&p.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION)&&p.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE),"authentic DeathProtection effects applied to owner");
        h.assertTrue(!d.hasEquippedTotem()&&d.getTotemStack().isEmpty(),"totem consumed atomically once");
        // Verify out-of-range dragon (> 32 blocks) does not activate totem
        h.assertTrue(d.equipTotem(new ItemStack(Items.TOTEM_OF_UNDYING)),"re-equip second totem");
        d.setPos(p.position().add(40,0,0));
        h.assertTrue(!d.canProtectOwnerWithTotem(p),"dragon beyond 32 blocks cannot trigger totem");
        h.succeed();
    }
    private static void landHuntingDeliveryAndSelfHeal(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,4);
        d.setHealth(10.0F); // Injured (10 / 30 HP) -> should hunt, cook prey, carry in mouth, and eat to heal
        d.getDragonBrain().setHuntCooldown(0);
        var chicken1=h.spawnWithNoFreeWill(EntityType.CHICKEN,new BlockPos(5,Y+1,6));chicken1.setNoGravity(true);
        d.setPos(chicken1.position().add(-1.0,0,0));
        for(int i=0;i<5;i++)d.getDragonBrain().tick();
        h.assertTrue(!chicken1.isAlive(),"injured dragon hunted and killed land prey");
        h.assertTrue(d.getCarriedPreyItem().is(Items.COOKED_CHICKEN),"dragon carries cooked chicken from fire hunt in its mouth");
        for(int i=0;i<10;i++)d.getDragonBrain().tick();
        h.assertTrue(d.getCarriedPreyItem().isEmpty()&&d.getHealth()>10.0F,"injured dragon ate carried prey and healed itself");

        // Now test full-health dragon hunting and delivering cooked drop to owner
        d.setHealth(d.getMaxHealth());
        d.getDragonBrain().setHuntCooldown(0);
        p.getInventory().clearContent();
        var chicken2=h.spawnWithNoFreeWill(EntityType.CHICKEN,new BlockPos(6,Y+1,6));chicken2.setNoGravity(true);
        d.setPos(chicken2.position().add(-1.0,0,0));
        for(int i=0;i<5;i++)d.getDragonBrain().tick();
        h.assertTrue(!chicken2.isAlive()&&d.getCarriedPreyItem().is(Items.COOKED_CHICKEN),"full-health dragon captured cooked chicken in mouth");
        d.setPos(p.position().add(1.0,0,0));
        for(int i=0;i<10;i++)d.getDragonBrain().tick();
        h.assertTrue(d.getCarriedPreyItem().isEmpty(),"dragon mouth emptied upon delivery");
        h.assertTrue(p.getInventory().countItem(Items.COOKED_CHICKEN)>=1,"cooked chicken delivered to owner's inventory");
        h.succeed();
    }
    private static void aquaticHuntingDiveAndResurface(GameTestHelper h){
        var d=dragon(h,false);var p=owner(h,d,3);
        d.setHealth(d.getMaxHealth());
        d.getDragonBrain().setHuntCooldown(0);
        p.getInventory().clearContent();
        // Carve a 3x3x2 water pool in the arena at (6..8, Y-1..Y, 5..7)
        for(int x=6;x<=8;x++)for(int z=5;z<=7;z++){
            h.setBlock(new BlockPos(x,Y-2,z),Blocks.STONE);
            h.setBlock(new BlockPos(x,Y-1,z),Blocks.WATER);
            h.setBlock(new BlockPos(x,Y,z),Blocks.WATER);
        }
        var salmon=h.spawnWithNoFreeWill(EntityType.SALMON,new BlockPos(7,Y-1,6));salmon.setNoGravity(true);
        d.setPos(salmon.position().add(0,0.4,0));
        for(int i=0;i<5;i++)d.getDragonBrain().tick();
        h.assertTrue(!salmon.isAlive(),"dragon hunted and killed aquatic salmon in water");
        h.assertTrue(d.getCarriedPreyItem().is(Items.SALMON),"dragon captured raw salmon in mouth during aquatic hunt");
        // Resurface and deliver to owner on dry land
        d.setPos(p.position().add(1.0,0,0));
        for(int i=0;i<10;i++)d.getDragonBrain().tick();
        h.assertTrue(d.getCarriedPreyItem().isEmpty()&&p.getInventory().countItem(Items.SALMON)>=1,"resurfaced dragon delivered captured salmon to owner");
        h.succeed();
    }
    private static void socialInteractionAndInterruption(GameTestHelper h){
        var d1=dragon(h,false);var p=owner(h,d1,4);
        var d2=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(5,Y+1,6));
        d2.tame(p);
        d1.setFlying(false);d2.setFlying(false);
        d1.getDragonBrain().setSocialCooldown(0);
        d2.getDragonBrain().setSocialCooldown(0);
        d1.getDragonBrain().tick();
        d2.getDragonBrain().tick();
        h.assertTrue(d1.getDragonBrain().isSocialActive()&&d2.getDragonBrain().isSocialActive(),"two idle ground dragons of the same owner start synchronized social interaction");
        h.assertTrue(d1.getDragonBrain().getSocialPartner()==d2&&d2.getDragonBrain().getSocialPartner()==d1,"both dragons reference each other as social partner");
        // Hostile attacks owner -> must immediately interrupt social interaction and enter combat
        var threat=hostile(h);
        p.setLastHurtByMob(threat);
        d1.getDragonBrain().tick();
        h.assertTrue(!d1.getDragonBrain().isSocialActive()&&!d2.getDragonBrain().isSocialActive(),"threat immediately interrupts social interaction on both dragons");
        h.assertTrue(d1.getTarget()==threat&&d1.getDragonState()==DragonState.DEFENDING,"dragon immediately switches from social interaction to defending owner");
        h.succeed();
    }
    private BabyDragonGameTests(){}
}
