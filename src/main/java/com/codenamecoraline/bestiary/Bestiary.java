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
            BestiaryPage.text("anatomy"),
            BestiaryPage.text("habitat"),
            BestiaryPage.text("mist"),
            BestiaryPage.text("survive"),
            BestiaryPage.text("light"),
            BestiaryPage.text("spoils"),
            BestiaryPage.text("cyst")));

    public static final List<BestiaryEntry> ENTRIES = List.of(REDCAP, BASILISK);

    private Bestiary() {}
}
