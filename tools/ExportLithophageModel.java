import com.codenamecoraline.client.lithophage.LithophageModel;
import com.codenamecoraline.client.lithophage.LithophageRenderState;
import com.codenamecoraline.entity.lithophage.Lithophage;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked Lithophage model and poses for the bestiary's ink drawings. */
public final class ExportLithophageModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (String name : List.of("anatomy", "combat")) {
            var model = new LithophageModel(LithophageModel.createBodyLayer().bakeRoot());
            var state = new LithophageRenderState();
            state.ageInTicks = 10;
            state.state = Lithophage.State.VIGILANT;
            state.awakeningProgress = 1.0F;
            state.idleAnimationState.start(0);
            if (name.equals("combat")) {
                state.state = Lithophage.State.COMBAT;
                state.attackType = Lithophage.AttackType.SHARD;
                state.holdingShard = true;
                state.attackShardAnimationState.start(0);
                state.ageInTicks = 11; // right before shard release, arm raised with glowing shard
            }
            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                if (part.contains("held_shard") && !state.holdingShard) {
                    return;
                }
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
            poses.put(name, faces);
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Exported Lithophage model poses to " + output);
    }
}

