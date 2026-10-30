import com.codenamecoraline.client.babydragon.BabyDragonModel;
import com.codenamecoraline.client.babydragon.BabyDragonRenderState;
import com.codenamecoraline.entity.babydragon.BabyDragon;
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
        for (String name : List.of("idle", "walk", "takeoff", "flying", "hover", "landing", "shoulder_idle", "fireball_charge", "fireball_shoot", "hurt", "sleep", "happy")) {
          for (int frame = 0; frame < 12; frame++) {
            var model = new BabyDragonModel(BabyDragonModel.createBodyLayer().bakeRoot());
            var state = new BabyDragonRenderState();
            var activity = BabyDragon.Pose.valueOf(name.toUpperCase(java.util.Locale.ROOT));
            state.ageInTicks = frame * (activity == BabyDragon.Pose.SLEEP ? 100 : activity == BabyDragon.Pose.IDLE ? 60 : activity == BabyDragon.Pose.HAPPY ? 30 : activity == BabyDragon.Pose.SHOULDER_IDLE ? 80 : activity == BabyDragon.Pose.WALK ? 16 : 12) / 12F;
            state.animations.get(activity).start(0);
            state.flightBlend = List.of("takeoff","flying","hover","fireball_charge","fireball_shoot").contains(name) ? 1 : 0;
            state.shoulder = name.equals("shoulder_idle"); state.sleeping = name.equals("sleep");
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
            poses.put(name + "_" + frame, faces);
          }
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Exported Baby Dragon model poses to " + output);
    }
}
