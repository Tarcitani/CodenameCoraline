package com.codenamecoraline.entity.salamander;

import net.minecraft.util.StringRepresentable;

/**
 * Visual and ecological variant of the Brimstone Salamander.
 * Determined upon spawn based on Nether biome affinities or random distribution.
 */
public enum BrimstoneSalamanderVariant implements StringRepresentable {
    /** Classic basalt charcoal, sulfur crystals, and orange magma veins (Basalt Deltas & Nether Wastes). */
    BRIMSTONE(0, "brimstone"),
    /** Spectral turquoise and cold soul-ash hide (Soul Sand Valley). */
    SOULFLAME(1, "soulflame"),
    /** Deep vulcanized netherrack, fungal scutes, and ruby-crimson glow (Crimson Forest). */
    CRIMSON(2, "crimson");

    private final int id;
    private final String name;

    BrimstoneSalamanderVariant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static BrimstoneSalamanderVariant byId(int id) {
        BrimstoneSalamanderVariant[] all = values();
        return all[Math.floorMod(id, all.length)];
    }

    public static BrimstoneSalamanderVariant byName(String name) {
        for (BrimstoneSalamanderVariant variant : values()) {
            if (variant.name.equalsIgnoreCase(name)) {
                return variant;
            }
        }
        return BRIMSTONE;
    }
}

