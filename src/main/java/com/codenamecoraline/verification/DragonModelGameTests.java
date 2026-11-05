package com.codenamecoraline.verification;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.client.babydragon.*;
import com.codenamecoraline.entity.babydragon.*;
import com.codenamecoraline.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.joml.Vector3f;

/** Verifies the actual baked geometries and attack transforms, not a second mock model. */
public final class DragonModelGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("dragon_model_distinct_anatomies",DragonModelGameTests::anatomies),
            Map.entry("dragon_model_all_native_animations",DragonModelGameTests::animations),
            Map.entry("dragon_model_uv_no_overlap",DragonModelGameTests::uvs),
            Map.entry("dragon_model_mouth_matches_real_head",DragonModelGameTests::mouth),
            Map.entry("dragon_model_world_aim_matches_head",DragonModelGameTests::worldAim),
            Map.entry("dragon_model_jaw_and_recovery",DragonModelGameTests::jaw),
            Map.entry("dragon_model_wings_and_tail_move",DragonModelGameTests::movement),
            Map.entry("dragon_model_stage_assets",DragonModelGameTests::assets),
            Map.entry("dragon_model_saddle_and_inventory",DragonModelGameTests::saddle),
            Map.entry("dragon_model_long_muzzle_wall",DragonModelGameTests::wall));
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID,name); }
    @SubscribeEvent public static void functions(RegisterEvent event) {
        if (Boolean.getBoolean("coraline.babyDragonTests")) event.register(Registries.TEST_FUNCTION,r -> TESTS.forEach((n,f) -> r.register(id(n),f)));
    }
    @SubscribeEvent public static void tests(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("coraline.babyDragonTests")) return;
        var env = event.registerEnvironment(id("dragon_model_environment"));
        TESTS.forEach((n,f) -> event.registerTest(id(n),new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,id(n)),new TestData<>(env,id("basilisk_test_arena"),100,0,true))));
    }
    private static DragonModel model(DragonStage stage) {
        return switch(stage) {
            case BABY -> new BabyDragonModel(BabyDragonModel.createBodyLayer().bakeRoot());
            case YOUNG -> new YoungDragonModel(YoungDragonModel.createBodyLayer().bakeRoot());
            case ADULT -> new AdultDragonModel(AdultDragonModel.createBodyLayer().bakeRoot());
        };
    }
    private static int cubes(DragonModel model) {
        int[] count={0}; model.root().visit(new PoseStack(),(p,n,i,c) -> count[0]++); return count[0];
    }
    private static void anatomies(GameTestHelper h) {
        int previous=0; float neck=0;
        for (DragonStage stage : DragonStage.values()) {
            var model=model(stage); int count=cubes(model);
            h.assertTrue(count>previous,"each age adds real anatomical geometry"); previous=count;
            float length=-model.root().createPartLookup().apply("neck_middle").z;
            h.assertTrue(length>neck,"longer neck exists in local geometry before rendering scale"); neck=length;
            h.assertTrue(model.root().createPartLookup().apply("left_wing_elbow")!=null,"articulated elbow");
            h.assertTrue(model.root().createPartLookup().apply("right_front_leg_claw_0")!=null,"geometric claws");
            if(stage==DragonStage.ADULT) h.assertTrue(model.root().createPartLookup().apply("tail_5")!=null,"six articulated adult tail segments");
        }
        h.succeed();
    }
    private static void animations(GameTestHelper h) {
        for(DragonStage stage:DragonStage.values()) {
            var model=model(stage); h.assertValueEqual(DragonAnimations.forStage(stage).size(),17,"complete stage catalogue");
            for(var pose:BabyDragon.Pose.values()) {
                var state=new BabyDragonRenderState(); state.stage=stage.ordinal(); state.ageInTicks=8;
                state.animations.get(pose).start(0); model.setupAnim(state);
            }
        }
        h.assertTrue(DragonAnimations.forStage(DragonStage.BABY).get(BabyDragon.Pose.WALK)!=DragonAnimations.forStage(DragonStage.ADULT).get(BabyDragon.Pose.WALK),"adult has its own animation definition"); h.succeed();
    }
    private static void uvs(GameTestHelper h) {
        for(DragonStage stage:DragonStage.values()) {
            boolean[] used=new boolean[512*512]; var model=model(stage);
            model.root().visit(new PoseStack(),(p,n,index,cube) -> {
                float minU=1,minV=1,maxU=0,maxV=0;
                for(var face:cube.polygons) for(var vertex:face.vertices()) {
                    minU=Math.min(minU,vertex.u()); minV=Math.min(minV,vertex.v()); maxU=Math.max(maxU,vertex.u()); maxV=Math.max(maxV,vertex.v());
                }
                h.assertTrue(minU>=0&&minV>=0&&maxU<=1&&maxV<=1,"UV stays inside stage atlas");
                int x0=(int)Math.floor(minU*512+.001),y0=(int)Math.floor(minV*512+.001);
                int x1=(int)Math.ceil(maxU*512-.001),y1=(int)Math.ceil(maxV*512-.001);
                for(int y=y0;y<y1;y++) for(int x=x0;x<x1;x++) {
                    int slot=y*512+x; h.assertTrue(!used[slot],"cuboids never overlap UV rectangles: "+stage+" "+n); used[slot]=true;
                }
            });
        }
        h.succeed();
    }
    private static void mouth(GameTestHelper h) {
        for(DragonStage stage:DragonStage.values()) for(int breath:new int[]{0,100,85,80,40,1}) for(float yaw:new float[]{-40,0,40}) {
            var model=model(stage); var state=new BabyDragonRenderState(); state.stage=stage.ordinal(); state.yRot=yaw; state.xRot=22;
            state.chargeTicks=breath==0?6:0; state.breathTicks=breath;
            model.setupAnim(state);
            var pose=DragonAnatomy.head(stage,yaw,22,state.chargeTicks,0,breath); var rig=DragonRig.of(stage);
            boolean[] found={false};
            model.root().visit(new PoseStack(),(matrix,path,index,cube) -> {
                if(!path.endsWith("/head") || index!=0) return;
                Vector3f actual=matrix.pose().transformPosition(0,(rig.mouthY()+net.minecraft.util.Mth.sin(pose.jawPitch())*rig.jawLever()*.45F)/16,rig.mouthZ()/16,new Vector3f());
                Vec3 expected=pose.mouthModel().scale(1D/16);
                h.assertTrue(new Vec3(actual.x,actual.y,actual.z).distanceTo(expected)<.00002,"server muzzle matches posed head exactly");
                Vector3f direction=matrix.pose().transformDirection(0,0,-1,new Vector3f()).normalize();
                h.assertTrue(new Vec3(direction.x,direction.y,direction.z).distanceTo(pose.directionModel())<.00002,"breath follows actual head axis"); found[0]=true;
            });
            h.assertTrue(found[0],"real head was inspected");
        }
        h.succeed();
    }
    private static void jaw(GameTestHelper h) {
        var model=model(DragonStage.ADULT); var state=new BabyDragonRenderState(); state.breathTicks=50; state.stage=2; model.setupAnim(state);
        float open=model.root().createPartLookup().apply("jaw").xRot;
        h.assertTrue(open>.6F,"adult jaw stays open while streaming");
        state.breathTicks=0; state.breathClosingTicks=5; model.setupAnim(state); float closing=model.root().createPartLookup().apply("jaw").xRot;
        h.assertTrue(closing>0&&closing<open,"jaw closes progressively during recovery");
        state.breathClosingTicks=0; model.setupAnim(state); h.assertValueEqual(model.root().createPartLookup().apply("jaw").xRot,0F,"jaw returns to rest"); h.succeed();
    }
    private static void worldAim(GameTestHelper h) {
        for(DragonStage stage:DragonStage.values()) for(float body:new float[]{-90,0,90}) for(float relative:new float[]{-40,0,40}) for(float pitch:new float[]{-30,0,30}) {
            var pose=DragonAnatomy.head(stage,relative,pitch,0,0,50);
            Vec3 actual=DragonAnatomy.directionToWorld(pose.directionModel(),body);
            float yaw=(body+relative)*(float)Math.PI/180, p=pitch*(float)Math.PI/180;
            Vec3 expected=new Vec3(-Math.sin(yaw)*Math.cos(p),-Math.sin(p),Math.cos(yaw)*Math.cos(p));
            h.assertTrue(actual.distanceTo(expected)<.001,"world head aim "+stage+" "+body+"/"+relative+" pitch="+pitch+" actual="+actual+" expected="+expected);
        }
        h.succeed();
    }
    private static void movement(GameTestHelper h) {
        for(DragonStage stage:DragonStage.values()) {
            var model=model(stage); var state=new BabyDragonRenderState(); state.stage=stage.ordinal(); state.flightBlend=1; state.ageInTicks=2; model.setupAnim(state);
            float wing=model.root().createPartLookup().apply("left_wing").zRot;
            float tail=model.root().createPartLookup().apply("tail_tip").yRot;
            state.ageInTicks=9; model.setupAnim(state);
            h.assertTrue(Math.abs(model.root().createPartLookup().apply("left_wing").zRot-wing)>.05,"wings beat in flight");
            h.assertTrue(Math.abs(model.root().createPartLookup().apply("tail_tip").yRot-tail)>.005,"tail joints move");
            h.assertTrue(model.root().createPartLookup().apply("left_front_leg").xRot<-.5,"feet tuck during flight");
        }
        h.succeed();
    }
    private static void assets(GameTestHelper h) {
        for(String stage:List.of("baby","young","adult")) for(String suffix:List.of("","_glow"))
            h.assertTrue(DragonModelGameTests.class.getResource("/assets/codenamecoraline/textures/entity/"+stage+"_dragon"+suffix+".png")!=null,"stage texture packaged");
        for(DragonStage stage:DragonStage.values()) {
            Set<String> hashes=new HashSet<>();
            for(int v=0;v<BabyDragon.VARIANT_COUNT;v++) {
                Identifier tex=BabyDragonRenderer.textureFor(stage,v);
                var url=DragonModelGameTests.class.getResource("/assets/"+tex.getNamespace()+"/"+tex.getPath());
                h.assertTrue(url!=null,"variant texture packaged: "+tex);
                try(var in=url.openStream()){hashes.add(java.util.Arrays.toString(java.security.MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));}
                catch(Exception ex){h.fail("failed reading variant texture "+tex);}
            }
            h.assertValueEqual(hashes.size(),BabyDragon.VARIANT_COUNT,"all 6 variants per stage are visually distinct: "+stage);
        }
        h.succeed();
    }
    private static BabyDragon adult(GameTestHelper h) {
        for(int x=0;x<=12;x++) for(int z=0;z<=12;z++) { h.setBlock(new BlockPos(x,10,z),Blocks.STONE); for(int y=11;y<=18;y++) h.setBlock(new BlockPos(x,y,z),Blocks.AIR); }
        var dragon=h.spawnWithNoFreeWill(ModEntities.BABY_DRAGON.get(),new BlockPos(3,11,6));
        h.assertTrue(dragon.setDragonStage(DragonStage.ADULT,false),"new adult fits the arena"); dragon.setNoGravity(true); return dragon;
    }
    private static void saddle(GameTestHelper h) {
        var dragon=adult(h); var owner=h.makeMockPlayer(GameType.SURVIVAL); owner.setPos(dragon.position().add(0,0,2)); dragon.tame(owner);
        dragon.getDragonInventory().setItem(17,new ItemStack(Items.DIAMOND,3));
        h.assertTrue(dragon.mountOwner(owner),"new anatomy still allows owner mounting");
        h.assertTrue(dragon.getPassengerRidingPosition(owner).y>dragon.getY()+2.0,"feet remain above adult torso");
        h.assertTrue(dragon.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE)>8,
                "detached rider camera clears the long adult head and neck");
        h.assertValueEqual(dragon.getDragonInventory().getItem(17).getCount(),3,"visual change preserves inventory"); h.succeed();
    }
    private static void wall(GameTestHelper h) {
        var dragon=adult(h); dragon.setYRot(0); dragon.setYHeadRot(0); dragon.yBodyRot=0;
        h.assertTrue(dragon.hasClearMouth(),"clear articulated neck can fire");
        for(int y=11;y<=16;y++) for(int x=1;x<=5;x++) h.setBlock(new BlockPos(x,y,8),Blocks.STONE);
        h.assertTrue(!dragon.hasClearMouth(),"wall crossing the long neck blocks fire origin"); dragon.shootDirection(new Vec3(0,0,1));
        h.assertTrue(h.getLevel().getEntitiesOfClass(DragonEmber.class,dragon.getBoundingBox().inflate(8)).stream().noneMatch(e->e.getOwner()==dragon),"projectiles cannot start beyond a wall inside the neck"); h.succeed();
    }
    private DragonModelGameTests() {}
}
