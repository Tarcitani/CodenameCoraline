package com.codenamecoraline.client.ten_threads_witch.matriarch;

import com.codenamecoraline.client.ten_threads_witch.TenThreadsParts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The joints of the Matriarch, by index: the model parts (from the generated {@link TenThreadsParts}) followed by a few
 * virtual joints that carry no geometry but are animated like the others, so that clips can drive effects in step with the
 * body: the reveal, tension and snap of the threads of each hand, the attack threads, the dissolve of her body and the glow
 * of her eyes. Every pose is a flat array of 3 channels (rotation, position, scale) of 3 values per joint.
 */
public final class MatRig {
    public static final String ID = "ten_threads_witch";
    public static final List<String> PARTS = TenThreadsParts.partNames(ID);
    /** x reveal 0..1, y tension 0..1, z snap 0..1 of the threads leaving the left (right) hand. */
    public static final String FX_THREAD_L = "fx_thread_l";
    public static final String FX_THREAD_R = "fx_thread_r";
    /** x reach 0..1 of the attack threads towards the focus, y how many (0..1), z the whip of their wave. */
    public static final String FX_ATTACK = "fx_attack";
    /** x the death's dissolve 0..1 (hands first, the smile last), y the entrance's concealment 1..0. */
    public static final String FX_DISSOLVE = "fx_dissolve";
    /** x extra glow of the eyes (0..1), y the threads' crimson tint (0..1), z how freely the staff swings with the arm (0 kept upright). */
    public static final String FX_GLOW = "fx_glow";
    public static final List<String> JOINTS;
    public static final int COUNT;
    public static final int CHANNELS;
    private static final Map<String, Integer> INDEX = new HashMap<>();

    static {
        List<String> all = new ArrayList<>(PARTS);
        all.add(FX_THREAD_L);
        all.add(FX_THREAD_R);
        all.add(FX_ATTACK);
        all.add(FX_DISSOLVE);
        all.add(FX_GLOW);
        JOINTS = List.copyOf(all);
        COUNT = JOINTS.size();
        CHANNELS = COUNT * 3;
        for (int i = 0; i < COUNT; i++) {
            INDEX.put(JOINTS.get(i), i);
        }
    }

    public static final int ROT = 0;
    public static final int POS = 1;
    public static final int SCALE = 2;

    public static final int BODY = index("body");
    public static final int CHEST = index("chest");
    public static final int HEAD = index("head");
    public static final int EYES = index("eyes");
    public static final int EYE_L = index("eye_l");
    public static final int EYE_R = index("eye_r");
    public static final int ARM_L = index("arm_l");
    public static final int ARM_R = index("arm_r");
    public static final int FOREARM_L = index("forearm_l");
    public static final int FOREARM_R = index("forearm_r");
    public static final int HAND_L = index("hand_l");
    public static final int HAND_R = index("hand_r");
    public static final int LEG_L = index("leg_l");
    public static final int LEG_R = index("leg_r");
    public static final int FOOT_L = index("foot_l");
    public static final int FOOT_R = index("foot_r");
    public static final int SKIRT = index("skirt");
    public static final int SKIRT_HEM = index("skirt_hem");
    public static final int CLOAK_L = index("cloak_l");
    public static final int CLOAK_R = index("cloak_r");
    public static final int MANTLE = index("mantle");
    public static final int HOOD_TIP = index("hood_tip");
    public static final int HAIR_BACK = index("hair_back");
    public static final int LOCK_L = index("hair_lock_l");
    public static final int LOCK_L_TIP = index("hair_lock_l_tip");
    public static final int LOCK_R = index("hair_lock_r");
    public static final int LOCK_R_TIP = index("hair_lock_r_tip");
    public static final int MOUTH = index("mouth");
    public static final int WHEEL = index("wheel");
    public static final int STAFF = index("staff");
    public static final int[] FINGERS_L = {index("finger_l0"), index("finger_l1"), index("finger_l2"), index("finger_l3"), index("thumb_l")};
    public static final int[] FINGERS_R = {index("finger_r0"), index("finger_r1"), index("finger_r2"), index("finger_r3"), index("thumb_r")};
    public static final int THREAD_L = index(FX_THREAD_L);
    public static final int THREAD_R = index(FX_THREAD_R);
    public static final int ATTACK = index(FX_ATTACK);
    public static final int DISSOLVE = index(FX_DISSOLVE);
    public static final int GLOW = index(FX_GLOW);

    private MatRig() {}

    public static int index(String joint) {
        Integer i = INDEX.get(joint);
        if (i == null) {
            throw new IllegalArgumentException("The Matriarch has no joint " + joint);
        }
        return i;
    }

    public static boolean has(String joint) {
        return INDEX.containsKey(joint);
    }

    public static boolean isVirtual(int joint) {
        return joint >= PARTS.size();
    }

    /** The joint on the other side ({@code arm_l} for {@code arm_r}), or the joint itself. */
    public static String mirror(String joint) {
        if (joint.equals(FX_THREAD_L)) return FX_THREAD_R;
        if (joint.equals(FX_THREAD_R)) return FX_THREAD_L;
        if (joint.startsWith("finger_l")) return "finger_r" + joint.substring(8);
        if (joint.startsWith("finger_r")) return "finger_l" + joint.substring(8);
        if (joint.endsWith("_l")) return joint.substring(0, joint.length() - 2) + "_r";
        if (joint.endsWith("_r")) return joint.substring(0, joint.length() - 2) + "_l";
        if (joint.endsWith("_l_tip")) return joint.replace("_l_tip", "_r_tip");
        if (joint.endsWith("_r_tip")) return joint.replace("_r_tip", "_l_tip");
        return joint;
    }

    public static boolean isFinger(String joint) {
        return joint.startsWith("finger_") || joint.startsWith("thumb_");
    }

    /**
     * Order in which the death dissolves the body (0 first .. 1 last): fingers, hands, forearms, arms, the staff, legs, the
     * cloak and the skirt, the chest, hair, the hood, the head and, last of all, the face (the smile stays to the end).
     */
    public static float dissolveOrder(String joint) {
        if (isFinger(joint)) return 0.08F;
        if (joint.startsWith("hand_")) return 0.18F;
        if (joint.startsWith("forearm_")) return 0.28F;
        if (joint.startsWith("arm_")) return 0.36F;
        if (joint.startsWith("spool_")) return 0.40F;
        if (joint.equals("wheel") || joint.equals("staff")) return 0.44F;
        if (joint.startsWith("foot_")) return 0.48F;
        if (joint.startsWith("leg_")) return 0.52F;
        if (joint.startsWith("cloak_")) return 0.58F;
        if (joint.equals("skirt_hem")) return 0.62F;
        if (joint.equals("skirt")) return 0.66F;
        if (joint.equals("mantle")) return 0.70F;
        if (joint.equals("body")) return 0.72F;
        if (joint.equals("chest")) return 0.76F;
        if (joint.startsWith("hair_")) return 0.80F;
        if (joint.startsWith("hood")) return 0.84F;
        if (joint.equals("blindfold")) return 0.86F;
        if (joint.equals("head")) return 0.90F;
        if (joint.startsWith("brow_") || joint.startsWith("eye")) return 0.94F;
        if (joint.startsWith("mouth")) return 0.985F;
        return 0.5F;
    }

    /**
     * Order in which the entrance reveals her (0 first): the silhouette (body, legs, chest, cloak, hood, head), then the
     * staff, the hair and the dress panels, the face, and last the arms, the hands and the fingers.
     */
    public static float revealOrder(String joint) {
        if (joint.equals("body") || joint.startsWith("leg_") || joint.startsWith("foot_")) return 0.10F;
        if (joint.equals("chest") || joint.equals("mantle")) return 0.20F;
        if (joint.startsWith("cloak_") || joint.equals("skirt")) return 0.26F;
        if (joint.startsWith("hood") || joint.equals("head") || joint.equals("blindfold")) return 0.34F;
        if (joint.equals("staff") || joint.equals("wheel") || joint.startsWith("spool_")) return 0.45F;
        if (joint.startsWith("hair_") || joint.equals("skirt_hem")) return 0.55F;
        if (joint.startsWith("brow_") || joint.startsWith("mouth") || joint.startsWith("eye")) return 0.62F;
        if (joint.startsWith("arm_")) return 0.70F;
        if (joint.startsWith("forearm_")) return 0.76F;
        if (joint.startsWith("hand_")) return 0.84F;
        if (isFinger(joint)) return 0.92F;
        return 0.5F;
    }
}
