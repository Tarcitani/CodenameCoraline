import com.codenamecoraline.client.salamander.BrimstoneSalamanderModel;
import com.codenamecoraline.client.salamander.BrimstoneSalamanderRenderState;
import com.codenamecoraline.entity.salamander.BrimstoneSalamander;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked BrimstoneSalamanderModel across key poses for visual review and Bestiary plates. */
public final class ExportBrimstoneSalamanderModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        List<String> poseNames = List.of(
                "idle",
                "idle_throat_puff",
                "walk",
                "swim_lava",
                "snap_bite_prep",
                "snap_bite_exec",
                "sulfur_spit_prep",
                "sulfur_spit_exec",
                "tail_whip_prep",
                "tail_whip_exec",
                "anatomy",
                "combat");

        for (String name : poseNames) {
            var model = new BrimstoneSalamanderModel(BrimstoneSalamanderModel.createBodyLayer().bakeRoot());
            var state = new BrimstoneSalamanderRenderState();
            state.state = BrimstoneSalamander.State.IDLE;
            state.ageInTicks = 20.0F;

            switch (name) {
                case "idle", "anatomy" -> {
                    state.idleBreatheAnimationState.start(0);
                    state.ageInTicks = 20.0F;
                }
                case "idle_throat_puff" -> {
                    state.idleThroatPuffAnimationState.start(0);
                    state.ageInTicks = 15.0F;
                }
                case "walk" -> {
                    state.walkAnimationPos = 1.4F;
                    state.walkAnimationSpeed = 0.85F;
                    state.ageInTicks = 8.0F;
                }
                case "swim_lava" -> {
                    state.swimmingInLava = true;
                    state.swimLavaAnimationState.start(0);
                    state.ageInTicks = 4.0F;
                }
                case "snap_bite_prep" -> {
                    state.state = BrimstoneSalamander.State.SNAP_BITE;
                    state.snapBitePrepAnimationState.start(0);
                    state.ageInTicks = 8.0F;
                }
                case "snap_bite_exec" -> {
                    state.state = BrimstoneSalamander.State.SNAP_BITE;
                    state.snapBiteExecAnimationState.start(0);
                    state.ageInTicks = 2.0F;
                }
                case "sulfur_spit_prep", "combat" -> {
                    state.state = BrimstoneSalamander.State.SULFUR_SPIT;
                    state.sulfurSpitPrepAnimationState.start(0);
                    state.ageInTicks = 15.0F;
                }
                case "sulfur_spit_exec" -> {
                    state.state = BrimstoneSalamander.State.SULFUR_SPIT;
                    state.sulfurSpitExecAnimationState.start(0);
                    state.ageInTicks = 2.0F;
                }
                case "tail_whip_prep" -> {
                    state.state = BrimstoneSalamander.State.TAIL_WHIP;
                    state.tailWhipPrepAnimationState.start(0);
                    state.ageInTicks = 10.0F;
                }
                case "tail_whip_exec" -> {
                    state.state = BrimstoneSalamander.State.TAIL_WHIP;
                    state.tailWhipExecAnimationState.start(0);
                    state.ageInTicks = 3.0F;
                }
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
        System.out.println("Exported Brimstone Salamander model poses (" + poses.size() + ") to " + output);
    }
}

