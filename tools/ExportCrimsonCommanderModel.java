import com.codenamecoraline.client.crimson.CrimsonCommanderModel;
import com.codenamecoraline.client.crimson.CrimsonCommanderRenderState;
import com.codenamecoraline.entity.crimson.CrimsonAnim;
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
 * Exports the real baked Crimson Commander model, posed by its own animations, for the Bestiary's ink engravings and the
 * pose sheets of the design document. Usage: ExportCrimsonCommanderModel out.json [NAME=ANIMATION:seconds[:vanguard] ...];
 * without poses it writes the engravings' poses (anatomy: Combat Idle Stance, combat: Dash Thrust).
 */
public final class ExportCrimsonCommanderModel {
    private record Face(String part, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, String> specs = new LinkedHashMap<>();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                String[] kv = args[i].split("=", 2);
                specs.put(kv[0], kv[1]);
            }
        } else {
            specs.put("anatomy", "COMBAT_IDLE:0.0");
            specs.put("combat", "THRUST_EXECUTE:0.4");
        }
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (var entry : specs.entrySet()) {
            String[] parts = entry.getValue().split(":");
            var model = new CrimsonCommanderModel(CrimsonCommanderModel.createBodyLayer().bakeRoot());
            var state = new CrimsonCommanderRenderState();
            state.anim = CrimsonAnim.valueOf(parts[0]);
            state.vanguard = parts.length > 2 && parts[2].equals("vanguard");
            state.lookAtTarget = false;
            state.ageInTicks = 100.0F + Float.parseFloat(parts[1]) * 20.0F;
            state.animState.start(100);
            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            var lookup = model.root().createPartLookup();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                for (String name : part.split("/")) { // hidden parts (a vanguard's plume, cape and spikes) are not drawn
                    var p = name.isEmpty() ? null : lookup.apply(name);
                    if (p != null && !p.visible) {
                        return;
                    }
                }
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
        System.out.println("Exported Crimson Commander model poses to " + output);
    }
}
