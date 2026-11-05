package com.codenamecoraline.bestiary;

import java.util.List;

/**
 * The bestiary book: one chapter per mob, in this order. To add a mob, append an entry here, write its texts in
 * the lang files (keys in BestiaryEntry) and draw its sketches under textures/gui/bestiary/ (see docs/DEVELOPMENT.md).
 */
public final class Bestiary {
    public static final BestiaryEntry REDCAP = new BestiaryEntry("redcap", List.of(
            BestiaryPage.sketch("cover", "redcap_spear", 112, 96),
            BestiaryPage.text("folklore"),
            BestiaryPage.text("blood"),
            BestiaryPage.sketch("dagger", "redcap_dagger", 112, 72),
            BestiaryPage.sketch("claws", "redcap_claws", 112, 72),
            BestiaryPage.text("attacks"),
            BestiaryPage.text("pack"),
            BestiaryPage.sketch("cap", "redcap_items", 112, 72),
            BestiaryPage.text("where"),
            BestiaryPage.text("drops")));

    public static final BestiaryEntry BASILISK = new BestiaryEntry("basilisk", List.of(
            BestiaryPage.sketch("anatomy", "basilisk_anatomy", 112, 80),
            BestiaryPage.text("habitat"),
            BestiaryPage.sketch("mist", "basilisk_breath", 112, 72),
            BestiaryPage.text("survive"),
            BestiaryPage.text("light"),
            BestiaryPage.sketch("spoils", "basilisk_parts", 112, 64),
            BestiaryPage.text("cyst")));

    public static final BestiaryEntry LITHOPHAGE = new BestiaryEntry("lithophage", List.of(
            BestiaryPage.sketch("anatomy", "lithophage_anatomy", 112, 80),
            BestiaryPage.text("habitat"),
            BestiaryPage.text("attributes"),
            BestiaryPage.sketch("combat", "lithophage_combat", 112, 72),
            BestiaryPage.text("tactics"),
            BestiaryPage.sketch("spoils", "lithophage_spoils", 112, 64)));

    public static final BestiaryEntry LAST_GIFT_COLLECTOR = new BestiaryEntry("last_gift_collector", List.of(
            BestiaryPage.sketch("anatomy", "last_gift_collector_anatomy", 112, 80),
            BestiaryPage.text("lore"),
            BestiaryPage.text("stats"),
            BestiaryPage.text("strategy"),
            BestiaryPage.sketch("combat", "last_gift_collector_combat", 112, 72),
            BestiaryPage.sketch("spoils", "last_gift_collector_spoils", 112, 64)));

    public static final BestiaryEntry CANOPY_BREAKER = new BestiaryEntry("canopy_breaker", List.of(
            BestiaryPage.sketch("anatomy", "canopy_breaker_anatomy", 112, 80),
            BestiaryPage.text("lore"),
            BestiaryPage.text("stats"),
            BestiaryPage.text("strategy"),
            BestiaryPage.sketch("combat", "canopy_breaker_combat", 112, 72),
            BestiaryPage.sketch("spoils", "canopy_breaker_spoils", 112, 64)));

    public static final BestiaryEntry BABY_DRAGON = new BestiaryEntry("baby_dragon", List.of(
            BestiaryPage.sketch("intro", "baby_dragon_anatomy", 112, 80),
            BestiaryPage.text("habitat"), BestiaryPage.text("behavior"), BestiaryPage.text("taming"),
            BestiaryPage.sketch("companion", "baby_dragon_flight", 112, 72),
            BestiaryPage.sketch("defense", "baby_dragon_defense", 112, 72),
            BestiaryPage.text("growth"), BestiaryPage.text("inferno"), BestiaryPage.text("riding"), BestiaryPage.text("inventory")));

    public static final BestiaryEntry CRIMSON_COMMANDER = new BestiaryEntry("crimson_commander", List.of(
            BestiaryPage.text("kingless"),
            BestiaryPage.sketch("anatomy", "crimson_commander_anatomy", 112, 80),
            BestiaryPage.sketch("greatsword", "crimson_commander_combat", 112, 72),
            BestiaryPage.text("duel"),
            BestiaryPage.text("oath"),
            BestiaryPage.sketch("spoils", "crimson_commander_spoils", 112, 64)));

    public static final BestiaryEntry TEN_THREADS_WITCH = new BestiaryEntry("ten_threads_witch", List.of(
            BestiaryPage.text("lore"),
            BestiaryPage.sketch("anatomy", "ten_threads_witch_anatomy", 112, 80),
            BestiaryPage.sketch("combat", "ten_threads_witch_combat", 112, 72),
            BestiaryPage.text("dossier1"),
            BestiaryPage.text("dossier2"),
            BestiaryPage.text("dossier3"),
            BestiaryPage.text("dossier4"),
            BestiaryPage.sketch("spoils", "ten_threads_witch_spoils", 112, 64)));

    public static final BestiaryEntry FEY_RAVEN = new BestiaryEntry("fey_raven", List.of(
            BestiaryPage.sketch("anatomy", "fey_raven_anatomy", 112, 80),
            BestiaryPage.text("habitat"),
            BestiaryPage.text("taming"),
            BestiaryPage.sketch("combat", "fey_raven_combat", 112, 72),
            BestiaryPage.text("sentinel"),
            BestiaryPage.sketch("spoils", "fey_raven_spoils", 112, 64)));

    public static final List<BestiaryEntry> ENTRIES = List.of(REDCAP, BASILISK, LITHOPHAGE, LAST_GIFT_COLLECTOR, CANOPY_BREAKER, BABY_DRAGON,
            CRIMSON_COMMANDER, TEN_THREADS_WITCH, FEY_RAVEN);

    private Bestiary() {}
}
