import com.codenamecoraline.client.raven.FeyRavenModel;
import com.codenamecoraline.client.raven.FeyRavenRenderState;
import com.codenamecoraline.entity.raven.FeyRaven;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked FeyRavenModel across key poses for visual review and Bestiary ink plates. */
public final class ExportFeyRavenModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        List<String> poseNames = List.of(
                "idle",
                "walk",
                "fly_flap",
                "peck_windup",
                "peck_impact",
                "peck_recovery",
                "anatomy",
                "combat");

        for (String name : poseNames) {
            var model = new FeyRavenModel(FeyRavenModel.createBodyLayer().bakeRoot());
            var state = new FeyRavenRenderState();
            state.tamed = true;
            state.hasScroll = true;
            state.holdingBerries = name.equals("anatomy") || name.equals("idle");
            state.state = FeyRaven.State.FOLLOWING;
            state.ageInTicks = 20.0F;

            switch (name) {
                case "idle", "anatomy" -> {
                    state.idleBreatheAnimationState.start(0);
                    state.ageInTicks = 20.0F;
                }
                case "walk" -> {
                    state.walkAnimationPos = 1.2F;
                    state.walkAnimationSpeed = 0.85F;
                    state.ageInTicks = 6.0F;
                }
                case "fly_flap" -> {
                    state.airborne = true;
                    state.flyFlapAnimationState.start(0);
                    state.ageInTicks = 2.0F;
                }
                case "peck_windup" -> {
                    state.state = FeyRaven.State.PECKING;
                    state.peckAttackAnimationState.start(0);
                    state.ageInTicks = 5.0F; // 0.25 s windup peak
                }
                case "peck_impact", "combat" -> {
                    state.state = FeyRaven.State.PECKING;
                    state.peckAttackAnimationState.start(0);
                    state.ageInTicks = 8.0F; // 0.40 s impact trigger
                }
                case "peck_recovery" -> {
                    state.state = FeyRaven.State.PECKING;
                    state.peckAttackAnimationState.start(0);
                    state.ageInTicks = 12.0F; // 0.60 s recovery settle
                }
            }

            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                if (part.contains("beak_berries") && !state.holdingBerries) {
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
        System.out.println("Exported Fey Raven model poses (" + poses.size() + ") to " + output);
    }
}

