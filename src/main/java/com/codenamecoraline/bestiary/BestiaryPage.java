package com.codenamecoraline.bestiary;

import org.jspecify.annotations.Nullable;

/**
 * One page of a bestiary entry. Every text is a translation key under "bestiary.codenamecoraline.<entry>.<page>".
 * A long body flows onto extra pages by itself, so an entry is written as a short list of these.
 *
 * @param id       page id, the middle part of its translation keys
 * @param drawing  sketch under textures/gui/bestiary/ (without ".png"), or null for a text-only page
 * @param drawingWidth  width of the sketch in GUI pixels
 * @param drawingHeight height of the sketch in GUI pixels
 */
public record BestiaryPage(String id, @Nullable String drawing, int drawingWidth, int drawingHeight) {
    public static BestiaryPage text(String id) {
        return new BestiaryPage(id, null, 0, 0);
    }

    public static BestiaryPage sketch(String id, String drawing, int width, int height) {
        return new BestiaryPage(id, drawing, width, height);
    }
}
