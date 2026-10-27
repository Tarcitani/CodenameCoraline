package com.codenamecoraline.entity.redcap;

import net.minecraft.util.StringRepresentable;

/**
 * What a Redcap carries. The variant decides its weapon model, its attack animation and its combat numbers.
 * Chosen at random when a Redcap spawns naturally, or fixed by the matching spawn egg.
 */
public enum RedcapVariant implements StringRepresentable {
    /** Pikestaff in the left hand: slow, long thrust. */
    SPEAR("spear", 7.0D, 5, 20, 1.8D, 2.2D),
    /** Dagger in the left hand: quick, short slashes. */
    DAGGER("dagger", 5.5D, 3, 12, 0.9D, 1.3D),
    /** Bare claws, plus hurled rocks from a distance. */
    CLAWS("claws", 6.0D, 3, 14, 0.8D, 1.2D);

    /** Attack damage attribute. */
    public final double damage;
    /** Ticks between the start of a swing and the hit, matching the strike frame of its animation. */
    public final int windupTicks;
    /** Ticks between two swings. */
    public final int cooldownTicks;
    /** Blocks beyond its hitbox at which the Redcap starts a swing. */
    public final double startReach;
    /** Blocks beyond its hitbox at which the swing still lands; larger than startReach because targets keep moving. */
    public final double strikeReach;

    private final String name;

    RedcapVariant(String name, double damage, int windupTicks, int cooldownTicks, double startReach, double strikeReach) {
        this.name = name;
        this.damage = damage;
        this.windupTicks = windupTicks;
        this.cooldownTicks = cooldownTicks;
        this.startReach = startReach;
        this.strikeReach = strikeReach;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static RedcapVariant byId(int id) {
        RedcapVariant[] all = values();
        return all[Math.floorMod(id, all.length)];
    }

    public static RedcapVariant byName(String name) {
        for (RedcapVariant variant : values()) {
            if (variant.name.equals(name)) {
                return variant;
            }
        }
        return SPEAR;
    }
}
