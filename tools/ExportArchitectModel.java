import com.codenamecoraline.client.architect.ArchitectModel;
import com.codenamecoraline.client.architect.ArchitectRenderState;
import com.codenamecoraline.entity.architect.ArchitectAnim;
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
 * Exports the real baked Architect model, posed by its own generated animations, for the pose review sheets of the
 * design document and the Bestiary engravings. Usage: ExportArchitectModel out.json [NAME=ANIMATION:tick ...]; without
 * poses it writes the review set (statue, reveal, idle, walk, guard and the key frames of every attack).
 */
public final class ExportArchitectModel {
    private record Face(String part, List<float[]> vertices) {}

    private static final String[] REVIEW = {
            "statue=DORMANT_STATUE:0", "awaken_mask=AWAKEN:44", "awaken_arms=AWAKEN:76", "awaken_end=AWAKEN:104",
            "idle=BATTLE_IDLE:0", "walk=WALK:0", "run=RUN:4", "guard=GUARD_FULL:0", "laugh=MANIACAL_LAUGH:8",
            "stagger=STAGGER:6", "death=DEATH:70",
            "a_prepare=SIXFOLD_EDICT:5", "a_cut3=SIXFOLD_EDICT:18", "a_end=SIXFOLD_EDICT:31",
            "b_prepare=CROSSED_MANDATE:8", "b_high_x=CROSSED_MANDATE:13", "b_low_x=CROSSED_MANDATE:18",
            "c_open=CARTENON_WHEEL:15", "c_spin=CARTENON_WHEEL:22", "c_recover=CARTENON_WHEEL:40",
            "d_crouch=SPLITTER_CRESCENT:7", "d_rise=SPLITTER_CRESCENT:20", "d_chop=SPLITTER_CRESCENT:24",
            "e_aim=PIERCING_SENTENCE:8", "e_thrust=PIERCING_SENTENCE:16", "e_recover=PIERCING_SENTENCE:22",
            "f_load=GARGOYLE_FALL:8", "f_apex=GARGOYLE_FALL:20", "f_impact=GARGOYLE_FALL:35",
            "g_block=PARRY_LEFT:6", "g_riposte=PARRY_LEFT:14",
    };

    public static void main(String[] args) throws Exception {
        Map<String, String> specs = new LinkedHashMap<>();
        String[] list = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : REVIEW;
        for (String arg : list) {
            String[] kv = arg.split("=", 2);
            specs.put(kv[0], kv[1]);
        }
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (var entry : specs.entrySet()) {
            String[] parts = entry.getValue().split(":");
            var model = new ArchitectModel(ArchitectModel.createBodyLayer().bakeRoot());
            var state = new ArchitectRenderState();
            state.anim = ArchitectAnim.valueOf(parts[0]);
            state.lookAtTarget = false;
            state.ageInTicks = 100.0F + Float.parseFloat(parts[1]);
            state.animState.start(100);
            model.setupAnim(state);
            List<Face> faces = new ArrayList<>();
            var lookup = model.root().createPartLookup();
            model.root().visit(new PoseStack(), (pose, part, index, cube) -> {
                for (String name : part.split("/")) { // hidden parts (the mask and the tablet in combat) are not drawn
                    var p = name.isEmpty() ? null : lookup.apply(name);
                    if (p != null && (!p.visible || p.xScale < 0.01F)) {
                        return;
                    }
                }
                for (var polygon : cube.polygons) {
                    List<float[]> vertices = new ArrayList<>();
                    for (var vertex : polygon.vertices()) {
                        Vector3f v = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                        vertices.add(new float[] {v.x(), v.y(), v.z(), vertex.u(), vertex.v()});
                    }
                    faces.add(new Face(part, vertices));
                }
            });
            poses.put(entry.getKey(), faces);
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Exported Architect model poses to " + output);
    }
}
