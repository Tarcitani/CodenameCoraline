import com.codenamecoraline.client.collector.LastGiftCollectorModel;
import com.codenamecoraline.client.collector.LastGiftCollectorRenderState;
import com.codenamecoraline.entity.collector.LastGiftCollector;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked LastGiftCollector 3D model and poses for the Bestiary's ink engravings. */
public final class ExportLastGiftCollectorModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (String name : List.of("anatomy", "combat")) {
            var model = new LastGiftCollectorModel(LastGiftCollectorModel.createBodyLayer().bakeRoot());
            var state = new LastGiftCollectorRenderState();
            state.ageInTicks = 16;
            state.bossState = LastGiftCollector.BossState.PHASE_ONE;
            state.idleAnimationState.start(0);
            if (name.equals("combat")) {
                state.bossState = LastGiftCollector.BossState.ATTACK_PREPARATION;
                state.activeAttack = LastGiftCollector.AttackType.WINTER_SCYTHE;
                state.phaseTwo = true;
                state.cageOpen = true;
                state.idleAnimationState.stop();
                state.attackHollowBellPrepAnimationState.start(0);
                state.ageInTicks = 28;
            }
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
            poses.put(name, faces);
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Exported Last Gift Collector model poses to " + output);
    }
}

