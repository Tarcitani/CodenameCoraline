package com.codenamecoraline.entity.babydragon;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** All progression/balance values use the existing hatchling as their geometric base. */
public enum DragonStage {
    BABY(1, 30, 2, 4, 6, .25, 12, 0, 0, 30),
    YOUNG(1.8F, 80, 8, 9, 14, .35, 20, 9, 500, 55),
    ADULT(3, 200, 16, 16, 30, .45, 35, 18, 2000, 100);

    public final float scale, health, armor, melee, fireball;
    public final double flightSpeed, range;
    public final int slots, totalXp, fireCooldown;
    DragonStage(float scale, float health, float armor, float melee, float fireball,
                double flightSpeed, double range, int slots, int totalXp, int fireCooldown) {
        this.scale = scale; this.health = health; this.armor = armor; this.melee = melee;
        this.fireball = fireball; this.flightSpeed = flightSpeed; this.range = range;
        this.slots = slots; this.totalXp = totalXp; this.fireCooldown = fireCooldown;
    }
    public Component title() { return Component.translatable("dragon.codenamecoraline.stage." + this.name().toLowerCase(Locale.ROOT)); }
    public DragonStage next() { return values()[Math.min(ordinal() + 1, 2)]; }
    public static DragonStage byId(int id) { return values()[Math.max(0, Math.min(2, id))]; }
}
