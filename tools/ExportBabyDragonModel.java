import com.codenamecoraline.client.babydragon.BabyDragonModel;
import com.codenamecoraline.client.babydragon.BabyDragonRenderState;
import com.codenamecoraline.client.babydragon.DragonModel;
import com.codenamecoraline.client.babydragon.YoungDragonModel;
import com.codenamecoraline.client.babydragon.AdultDragonModel;
import com.codenamecoraline.entity.babydragon.BabyDragon;
import com.codenamecoraline.entity.babydragon.DragonStage;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked Baby Dragon model and poses for the bestiary's ink drawings. */
public final class ExportBabyDragonModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (DragonStage stage : DragonStage.values()) {
        for (BabyDragon.Pose activity : BabyDragon.Pose.values()) {
          String name = activity.name().toLowerCase(java.util.Locale.ROOT);
          for (int frame = 0; frame < 12; frame++) {
            DragonModel model = switch(stage) {
                case BABY -> new BabyDragonModel(BabyDragonModel.createBodyLayer().bakeRoot());
                case YOUNG -> new YoungDragonModel(YoungDragonModel.createBodyLayer().bakeRoot());
                case ADULT -> new AdultDragonModel(AdultDragonModel.createBodyLayer().bakeRoot());
            };
            var state = new BabyDragonRenderState();
            state.ageInTicks = frame * (activity == BabyDragon.Pose.SLEEP ? 100 : activity == BabyDragon.Pose.IDLE ? 60 : activity == BabyDragon.Pose.HAPPY ? 30 : activity == BabyDragon.Pose.SHOULDER_IDLE ? 80 : activity == BabyDragon.Pose.WALK ? 16 : 12) / 12F;
            state.animations.get(activity).start(0);
            state.flightBlend = List.of("takeoff","flying","hover","fireball_charge","fireball_shoot").contains(name) ? 1 : 0;
            state.shoulder = name.equals("shoulder_idle"); state.sleeping = name.equals("sleep");
            state.stage = stage.ordinal(); state.growthScale = stage.scale;
            if (activity == BabyDragon.Pose.EVOLVE) state.ageInTicks = frame * 60 / 12F;
            if (activity == BabyDragon.Pose.BREATH_PREPARE || activity == BabyDragon.Pose.DEATH) state.ageInTicks = frame * 20 / 12F;
            float t = frame/12F;
            if (activity == BabyDragon.Pose.FIREBALL_CHARGE) state.chargeTicks = Math.max(1,(int)((1-t)*(stage==DragonStage.ADULT?32:stage==DragonStage.YOUNG?16:12)));
            if (activity == BabyDragon.Pose.FIREBALL_SHOOT) state.shotTicks = Math.max(1,(int)((1-t)*8));
            if (activity == BabyDragon.Pose.BREATH_PREPARE) state.breathTicks = 100-(int)(t*19);
            if (activity == BabyDragon.Pose.BREATH_LOOP) state.breathTicks = 80-(int)(t*50);
            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                for (var polygon : cube.polygons) {
                    List<float[]> vertices = new ArrayList<>();
                    for (var vertex : polygon.vertices()) {
                        Vector3f v = pose.pose().transformPosition(
                                vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                        vertices.add(new float[]{v.x(), v.y(), v.z(), vertex.u(), vertex.v()});
                    }
                    faces.add(new Face(part, index, vertices));
                }
            });
            poses.put((stage == DragonStage.BABY ? "" : stage.name().toLowerCase(java.util.Locale.ROOT) + "_") + name + "_" + frame, faces);
          }
        }
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        try (var writer = Files.newBufferedWriter(output)) { new GsonBuilder().create().toJson(poses,writer); }
        System.out.println("Exported Baby Dragon model poses to " + output);
    }
}
