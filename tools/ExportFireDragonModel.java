import com.codenamecoraline.client.firedragon.FireDragonModel;
import com.codenamecoraline.entity.firedragon.FireDragonAnimation;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/** Exports actual baked geometry and animation poses; also checks every clip on every tick. */
public final class ExportFireDragonModel {
    private record Face(String part, int cube, List<float[]> vertices) {}
    public static void main(String[] args) throws Exception {
        var model = new FireDragonModel(FireDragonModel.createBodyLayer().bakeRoot());
        Map<String, List<Face>> poses = new LinkedHashMap<>();
        for (var clip : FireDragonAnimation.values()) {
            for (int t = 0; t <= clip.ticks; t++) {
                model.applyPose(clip, t);
                model.root().getAllParts().forEach(p -> {
                    if (!Float.isFinite(p.xRot + p.yRot + p.zRot + p.x + p.y + p.z))
                        throw new IllegalStateException("Non-finite joint in " + clip);
                });
            }
            export(model, poses, clip.name().toLowerCase(), clip, clip == FireDragonAnimation.FIRE_RELEASE ? 1 : clip.ticks / 2.0F);
        }
        export(model, poses, "idle", FireDragonAnimation.BREATHE, 0);
        export(model, poses, "fire_windup_peak", FireDragonAnimation.FIRE_WINDUP, 12);
        export(model, poses, "fire_recovery_end", FireDragonAnimation.FIRE_RECOVERY, 15);
        export(model, poses, "sit_end", FireDragonAnimation.SIT, 12);
        for (String family : List.of("idle", "walk", "fly", "fire")) {
            for (int i = 0; i < 12; i++) {
                FireDragonAnimation clip;
                float time;
                if (family.equals("fire")) {
                    float total = i * 28.0F / 11;
                    clip = total < 12 ? FireDragonAnimation.FIRE_WINDUP : total < 13 ? FireDragonAnimation.FIRE_RELEASE : FireDragonAnimation.FIRE_RECOVERY;
                    time = total < 12 ? total : total < 13 ? total - 12 : total - 13;
                } else {
                    clip = family.equals("idle") ? FireDragonAnimation.BREATHE : family.equals("walk") ? FireDragonAnimation.WALK : FireDragonAnimation.FLY;
                    time = i * clip.ticks / 12.0F;
                }
                export(model, poses, "frame_" + family + "_" + i, clip, time);
            }
        }
        Path output = Path.of(args[0]); Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().create().toJson(poses));
        System.out.println("Validated all 22 clips tick by tick; exported " + poses.size() + " real model poses to " + output);
    }
    private static void export(FireDragonModel model, Map<String, List<Face>> poses, String name, FireDragonAnimation clip, float tick) {
        model.applyPose(clip, tick);
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
}
