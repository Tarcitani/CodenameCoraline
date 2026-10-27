package com.codenamecoraline.entity.redcap;

import net.minecraft.util.StringRepresentable;

/**
 * How old a Redcap looks: only a slight shift of its colours (greener and smoother when young, greyer and more
 * wrinkled when old), so a pack does not look cloned. Has no effect on its stats.
 */
public enum RedcapAge implements StringRepresentable {
    YOUNG("young", "textures/entity/redcap_young.png"),
    ADULT("adult", "textures/entity/redcap.png"),
    OLD("old", "textures/entity/redcap_old.png");

    /** Texture path under assets/codenamecoraline. */
    public final String texture;

    private final String name;

    RedcapAge(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static RedcapAge byId(int id) {
        RedcapAge[] all = values();
        return all[Math.floorMod(id, all.length)];
    }

    public static RedcapAge byName(String name) {
        for (RedcapAge age : values()) {
            if (age.name.equals(name)) {
                return age;
            }
        }
        return ADULT;
    }

    /** Random age for a new Redcap: half adults, a quarter each of young and old. */
    public static RedcapAge random(net.minecraft.util.RandomSource random) {
        int roll = random.nextInt(4);
        return roll == 0 ? YOUNG : roll == 1 ? OLD : ADULT;
    }
}
