import com.codenamecoraline.client.canopy.CanopyBreakerModel;
import com.codenamecoraline.client.canopy.CanopyBreakerRenderState;
import com.codenamecoraline.entity.canopy.CanopyAnim;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/**
 * Exports the real baked Canopy Breaker model, posed by its own animations, for the Bestiary's ink engravings.
 * Usage: ExportCanopyBreakerModel out.json [NAME=ANIMATION:seconds:club ...]; without poses it writes the engravings' poses.
 */
public final class ExportCanopyBreakerModel {
    private record Face(String part, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, String> specs = new LinkedHashMap<>();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                String[] kv = args[i].split("=", 2);
                specs.put(kv[0], kv[1]);
            }
        } else {
            specs.put("anatomy", "CLUB_GUARD_IDLE:0.0:club");
            specs.put("combat", "ATTACK_TRUNK_SLAM_EXECUTE:0.25:club");
            specs.put("drop", "CANOPY_DROP_AIRBORNE:0.3:club");
        }
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (var entry : specs.entrySet()) {
            String[] parts = entry.getValue().split(":");
            var model = new CanopyBreakerModel(CanopyBreakerModel.createBodyLayer().bakeRoot());
            var state = new CanopyBreakerRenderState();
            state.anim = CanopyAnim.valueOf(parts[0]);
            state.clubDrawn = parts.length > 2 && parts[2].equals("club");
            state.lookAtTarget = false;
            state.ageInTicks = 100.0F + Float.parseFloat(parts[1]) * 20.0F;
            state.animState.start(100);
            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                for (var polygon : cube.polygons) {
                    List<float[]> vertices = new ArrayList<>();
                    for (var vertex : polygon.vertices()) {
                        Vector3f v = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                        vertices.add(new float[]{v.x(), v.y(), v.z(), vertex.u(), vertex.v()});
                    }
                    faces.add(new Face(part, vertices));
                }
            });
            poses.put(entry.getKey(), faces);
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Exported Canopy Breaker model poses to " + output);
    }
}
