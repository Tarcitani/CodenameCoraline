package com.codenamecoraline.entity;

/**
 * A creature a player called with a weapon (the Renegade's Greataxe's Gift Mimics, the Deadroot Club's Bark Macaques).
 * Player summons are one side: they never target or hurt one another, whoever summoned them, and the summoning weapons'
 * own area attacks spare them.
 */
public interface PlayerSummon {
    /** True while this creature fights for a player (a boss's minion of the same species answers false). */
    boolean isPlayerSummon();
}
