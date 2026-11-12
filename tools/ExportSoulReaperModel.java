import com.codenamecoraline.client.reaper.OmenHareModel;
import com.codenamecoraline.client.reaper.OmenHareRenderState;
import com.codenamecoraline.client.reaper.SoulReaperModel;
import com.codenamecoraline.client.reaper.SoulReaperRenderState;
import com.codenamecoraline.entity.reaper.OmenHare;
import com.codenamecoraline.entity.reaper.SoulReaper;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports the real baked SoulReaperModel and OmenHareModel across key poses for visual review and Bestiary plates. */
public final class ExportSoulReaperModel {
    private record Face(String part, int cube, List<float[]> vertices) {}

    public static void main(String[] args) throws Exception {
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        List<String> reaperPoses = List.of(
                "idle",
                "whirlwind_windup",
                "whirlwind_spin",
                "blink_strike",
                "soul_harvest",
                "frost_wave_slam",
                "gravedigger_slam",
                "funeral_bow",
                "anatomy",
                "combat");

        for (String name : reaperPoses) {
            var model = new SoulReaperModel(SoulReaperModel.createBodyLayer().bakeRoot());
            var state = new SoulReaperRenderState();
            state.state = SoulReaper.State.COMBAT_IDLE;
            state.stage = SoulReaper.Stage.STAGE_1;
            state.locomotionMode = SoulReaper.LocomotionMode.GLIDE;
            state.actionId = SoulReaper.ActionId.IDLE_1;
            state.primaryAnimState.start(0);
            state.ageInTicks = 20.0F;

            switch (name) {
                case "idle", "anatomy" -> {
                    state.actionId = SoulReaper.ActionId.IDLE_1;
                    state.ageInTicks = 20.0F;
                }
                case "whirlwind_windup" -> {
                    state.actionId = SoulReaper.ActionId.O1_WHIRLWIND;
                    state.ageInTicks = 18.0F; // 0.90 s peak counter-windup
                }
                case "whirlwind_spin", "combat" -> {
                    state.actionId = SoulReaper.ActionId.O1_WHIRLWIND;
                    state.ageInTicks = 28.0F; // active 360 spin with shroud flare
                }
                case "blink_strike" -> {
                    state.actionId = SoulReaper.ActionId.O2_BLINK_STRIKE;
                    state.ageInTicks = 22.0F; // 1.10 s diagonal cleave impact
                }
                case "soul_harvest" -> {
                    state.actionId = SoulReaper.ActionId.O3_SOUL_HARVEST;
                    state.ageInTicks = 40.0F; // 2.00 s open ribcage soul siphon
                }
                case "frost_wave_slam" -> {
                    state.actionId = SoulReaper.ActionId.O4_FROST_WAVE;
                    state.ageInTicks = 21.0F; // 1.05 s downward ice-wave slam
                }
                case "gravedigger_slam" -> {
                    state.actionId = SoulReaper.ActionId.F4_GRAVEDIGGER_SLAM;
                    state.ageInTicks = 15.0F; // 0.75 s overhead apex before slam
                }
                case "funeral_bow" -> {
                    state.actionId = SoulReaper.ActionId.P2_FUNERAL_BOW;
                    state.ageInTicks = 24.0F; // 1.20 s deep mocking bow
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
                        vertices.add(new float[] {v.x(), v.y(), v.z(), vertex.u(), vertex.v()});
                    }
                    faces.add(new Face(part, index, vertices));
                }
            });
            poses.put(name, faces);
        }

        // Also export Omen Hare poses
        for (String harePose : List.of("hare_stand_stare", "hare_tilt_taunt")) {
            var hareModel = new OmenHareModel(OmenHareModel.createBodyLayer().bakeRoot());
            var hareState = new OmenHareRenderState();
            if (harePose.equals("hare_stand_stare")) {
                hareState.state = OmenHare.State.STANDING_STARE;
                hareState.standStareAnimState.start(0);
                hareState.ageInTicks = 16.0F;
            } else {
                hareState.state = OmenHare.State.TILT_TAUNT;
                hareState.tiltTauntAnimState.start(0);
                hareState.ageInTicks = 16.0F;
            }
            hareModel.setupAnim(hareState);
            List<Face> faces = new ArrayList<>();
            hareModel.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                for (var polygon : cube.polygons) {
                    List<float[]> vertices = new ArrayList<>();
                    for (var vertex : polygon.vertices()) {
                        Vector3f v = pose.pose().transformPosition(
                                vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                        vertices.add(new float[] {v.x(), v.y(), v.z(), vertex.u(), vertex.v()});
                    }
                    faces.add(new Face(part, index, vertices));
                }
            });
            poses.put(harePose, faces);
        }

        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Exported Soul Reaper & Omen Hare model poses (" + poses.size() + ") to " + output);
    }
}

