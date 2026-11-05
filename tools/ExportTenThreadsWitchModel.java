import com.codenamecoraline.client.ten_threads_witch.PuppetModel;
import com.codenamecoraline.client.ten_threads_witch.PuppetRenderState;
import com.codenamecoraline.client.ten_threads_witch.TenThreadsParts;
import com.codenamecoraline.entity.ten_threads_witch.PuppetAnim;
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
 * Exports the real baked models of the Witch of Ten Threads and her puppets, posed by their own animations, for the
 * Bestiary engravings and the design document pose sheets. Usage: ExportTenThreadsWitchModel out.json [ID:ANIM:t1,t2[:broken][:fury] ...];
 * without poses it writes the engravings (anatomy: the Matriarch idle; combat: the Cradle Giant winding up with the crimson thread).
 */
public final class ExportTenThreadsWitchModel {
    public static void main(String[] args) throws Exception {
        Map<String, List<Map<String, Object>>> out = new LinkedHashMap<>();
        if (args.length == 1) {
            args = new String[] {args[0], "ten_threads_witch:IDLE:1.0:0", "cradle_giant:WINDUP_CRIMSON:1.0"};
        }
        for (int i = 1; i < args.length; i++) {
            String[] spec = args[i].split(":");
            String id = spec[0];
            PuppetAnim anim = PuppetAnim.valueOf(spec[1]);
            int broken = spec.length > 3 ? Integer.parseInt(spec[3]) : 0;
            boolean fury = spec.length > 4 && spec[4].equals("fury");
            for (String ts : spec[2].split(",")) {
                float t = Float.parseFloat(ts);
                var model = new PuppetModel(TenThreadsParts.layer(id).bakeRoot(), id);
                var state = new PuppetRenderState();
                state.anim = anim;
                state.lookAtTarget = false;
                state.brokenSpools = broken;
                state.fury = fury;
                state.ageInTicks = 100.0F + t * 20.0F;
                state.animState.start(100);
                model.setupAnim(state);
                List<Map<String, Object>> faces = new ArrayList<>();
                var lookup = model.root().createPartLookup();
                model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                    for (String name : part.split("/")) {
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
                        Map<String, Object> face = new LinkedHashMap<>();
                        face.put("part", part);
                        face.put("vertices", vertices);
                        faces.add(face);
                    }
                });
                out.put(id + "@" + anim.name().toLowerCase() + "@" + ts, faces);
            }
        }
        Files.writeString(Path.of(args[0]), new GsonBuilder().create().toJson(out));
        System.out.println("wrote " + out.size());
    }
}
