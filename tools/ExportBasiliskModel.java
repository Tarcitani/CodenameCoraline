import com.codenamecoraline.client.basilisk.BasiliskModel;
import com.codenamecoraline.client.basilisk.BasiliskRenderState;
import com.codenamecoraline.entity.basilisk.Basilisk;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked model and animation poses for the bestiary's ink drawings. */
public final class ExportBasiliskModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (String name : List.of("anatomy", "breath")) {
            var model = new BasiliskModel(BasiliskModel.createBodyLayer().bakeRoot());
            var state = new BasiliskRenderState();
            state.ageInTicks = 10;
            if (name.equals("breath")) {
                state.inflation = 1;
                state.state = Basilisk.State.EXHALING;
            }
            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                for (var polygon : cube.polygons) {
                    List<float[]> vertices = new ArrayList<>();
                    for (var vertex : polygon.vertices()) {
                        Vector3f v = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
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
        System.out.println("Exported Basilisk model poses to " + output);
    }
}
