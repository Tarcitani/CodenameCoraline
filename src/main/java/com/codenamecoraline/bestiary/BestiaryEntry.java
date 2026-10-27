package com.codenamecoraline.bestiary;

import java.util.List;

/** A mob chapter of the bestiary. Starts on a left-hand page. */
public record BestiaryEntry(String id, List<BestiaryPage> pages) {
    /** Translation key of the entry's name, shown in the index and on the first page. */
    public String nameKey() {
        return "bestiary.codenamecoraline." + this.id + ".name";
    }

    public String titleKey(BestiaryPage page) {
        return "bestiary.codenamecoraline." + this.id + "." + page.id() + ".title";
    }

    public String captionKey(BestiaryPage page) {
        return "bestiary.codenamecoraline." + this.id + "." + page.id() + ".caption";
    }

    public String bodyKey(BestiaryPage page) {
        return "bestiary.codenamecoraline." + this.id + "." + page.id() + ".body";
    }
}
