package com.codenamecoraline.client.bestiary;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.bestiary.Bestiary;
import com.codenamecoraline.bestiary.BestiaryEntry;
import com.codenamecoraline.bestiary.BestiaryPage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * The bestiary: a two-page spread. The first spread is the index (one button per mob), the others are the
 * chapters, each starting on a left page. Text is wrapped to the page and flows onto extra pages when it is long.
 */
public final class BestiaryScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/gui/bestiary_book.png");
    private static final int BOOK_WIDTH = 300;
    private static final int BOOK_HEIGHT = 190;
    private static final int PAGE_WIDTH = 124;
    private static final int PAGE_TOP = 16;
    private static final int PAGE_BOTTOM = 16;
    private static final int LINE_HEIGHT = 9;
    private static final int LEFT_PAGE_X = 14;
    private static final int RIGHT_PAGE_X = 162;

    private static final int INK = 0xFF3B2A1A;
    private static final int TITLE_INK = 0xFF7A1010;
    private static final int NOTE_INK = 0xFF6B5238;

    /** What one page shows once its texts are wrapped. */
    private record RenderedPage(@Nullable Component title, @Nullable Identifier drawing, int drawingWidth, int drawingHeight,
                                List<FormattedCharSequence> caption, List<FormattedCharSequence> body) {}

    private final List<RenderedPage> pages = new ArrayList<>();
    /** Index of the first page of each entry in `pages`. */
    private final List<Integer> entryStarts = new ArrayList<>();
    private int spread;
    private PageButton forwardButton;
    private PageButton backButton;
    private final List<Button> indexButtons = new ArrayList<>();

    public BestiaryScreen() {
        super(Component.translatable("item.codenamecoraline.bestiary"));
    }

    @Override
    protected void init() {
        this.buildPages();
        int left = this.left();
        int top = this.top();
        this.indexButtons.clear();
        int indexStep = Math.min(24, (BOOK_HEIGHT - 24 - 48 - 18) / Math.max(1, Bestiary.ENTRIES.size() - 1));
        for (int i = 0; i < Bestiary.ENTRIES.size(); i++) {
            int entry = i;
            Button button = Button.builder(Component.translatable(Bestiary.ENTRIES.get(i).nameKey()), b -> this.openEntry(entry))
                    .pos(left + RIGHT_PAGE_X, top + 48 + i * indexStep).size(PAGE_WIDTH, 18).build();
            this.indexButtons.add(this.addRenderableWidget(button));
        }
        this.forwardButton = this.addRenderableWidget(new PageButton(left + BOOK_WIDTH - 44, top + BOOK_HEIGHT - 20, true, b -> this.turn(1), true));
        this.backButton = this.addRenderableWidget(new PageButton(left + 20, top + BOOK_HEIGHT - 20, false, b -> this.turn(-1), true));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .pos((this.width - 200) / 2, top + BOOK_HEIGHT + 4).width(200).build());
        this.updateButtons();
    }

    /** Wraps every entry's texts into pages. A chapter always begins on a left page. */
    private void buildPages() {
        this.pages.clear();
        this.entryStarts.clear();
        for (BestiaryEntry entry : Bestiary.ENTRIES) {
            if (this.pages.size() % 2 == 1) {
                this.pages.add(new RenderedPage(null, null, 0, 0, List.of(), List.of()));
            }
            this.entryStarts.add(this.pages.size());
            boolean first = true;
            for (BestiaryPage page : entry.pages()) {
                Component title = Component.translatable(first ? entry.nameKey() : entry.titleKey(page))
                        .withStyle(Style.EMPTY.withBold(true));
                first = false;
                Identifier drawing = page.drawing() == null ? null
                        : Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "textures/gui/bestiary/" + page.drawing() + ".png");
                List<FormattedCharSequence> caption = drawing == null ? List.of()
                        : this.font.split(Component.translatable(entry.captionKey(page)).withStyle(Style.EMPTY.withItalic(true)), PAGE_WIDTH);
                List<FormattedCharSequence> body = this.font.split(Component.translatable(entry.bodyKey(page)), PAGE_WIDTH);
                int used = this.font.lineHeight + 6 + (drawing == null ? 0 : page.drawingHeight() + 4 + caption.size() * LINE_HEIGHT + 4);
                int budget = Math.max(1, (BOOK_HEIGHT - PAGE_TOP - PAGE_BOTTOM - used) / LINE_HEIGHT);
                int taken = Math.min(budget, body.size());
                this.pages.add(new RenderedPage(title, drawing, page.drawingWidth(), page.drawingHeight(), caption, body.subList(0, taken)));
                // The rest of a long text continues on pages of its own, under a "continued" heading.
                int continuedBudget = (BOOK_HEIGHT - PAGE_TOP - PAGE_BOTTOM - this.font.lineHeight - 6) / LINE_HEIGHT;
                int from = taken;
                while (from < body.size()) {
                    int to = Math.min(body.size(), from + continuedBudget);
                    this.pages.add(new RenderedPage(Component.translatable("bestiary.codenamecoraline.continued"), null, 0, 0,
                            List.of(), body.subList(from, to)));
                    from = to;
                }
            }
        }
    }

    private int spreadCount() {
        return 1 + (this.pages.size() + 1) / 2;
    }

    private void openEntry(int entry) {
        this.spread = 1 + this.entryStarts.get(entry) / 2;
        this.updateButtons();
    }

    private void turn(int delta) {
        this.spread = Mth.clamp(this.spread + delta, 0, this.spreadCount() - 1);
        this.updateButtons();
    }

    private void updateButtons() {
        this.forwardButton.visible = this.spread < this.spreadCount() - 1;
        this.backButton.visible = this.spread > 0;
        for (Button button : this.indexButtons) {
            button.visible = this.spread == 0;
            button.active = this.spread == 0;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        // Page Up / Page Down turn the spread, like the vanilla book.
        if (event.key() == 266) {
            this.turn(-1);
            return true;
        }
        if (event.key() == 267) {
            this.turn(1);
            return true;
        }
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.left(), this.top(), 0.0F, 0.0F, BOOK_WIDTH, BOOK_HEIGHT, BOOK_WIDTH, BOOK_HEIGHT);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int left = this.left();
        int top = this.top();
        if (this.spread == 0) {
            this.renderIndexPage(graphics, left + LEFT_PAGE_X, top + PAGE_TOP);
            graphics.centeredText(this.font, Component.translatable("bestiary.codenamecoraline.index").withStyle(Style.EMPTY.withBold(true)),
                    left + RIGHT_PAGE_X + PAGE_WIDTH / 2, top + PAGE_TOP + 4, TITLE_INK);
            return;
        }
        int first = (this.spread - 1) * 2;
        if (first < this.pages.size()) {
            this.renderPage(graphics, this.pages.get(first), left + LEFT_PAGE_X, top + PAGE_TOP, first + 1);
        }
        if (first + 1 < this.pages.size()) {
            this.renderPage(graphics, this.pages.get(first + 1), left + RIGHT_PAGE_X, top + PAGE_TOP, first + 2);
        }
    }

    private void renderIndexPage(GuiGraphicsExtractor graphics, int x, int y) {
        int center = x + PAGE_WIDTH / 2;
        graphics.centeredText(this.font, Component.translatable("bestiary.codenamecoraline.title").withStyle(Style.EMPTY.withBold(true)),
                center, y + 10, TITLE_INK);
        int line = y + 34;
        for (FormattedCharSequence text : this.font.split(Component.translatable("bestiary.codenamecoraline.intro"), PAGE_WIDTH)) {
            graphics.text(this.font, text, x, line, INK, false);
            line += LINE_HEIGHT;
        }
    }

    private void renderPage(GuiGraphicsExtractor graphics, RenderedPage page, int x, int y, int number) {
        int cursor = y;
        if (page.title() != null) {
            graphics.centeredText(this.font, page.title(), x + PAGE_WIDTH / 2, cursor + 2, TITLE_INK);
            cursor += this.font.lineHeight + 6;
        }
        if (page.drawing() != null) {
            int drawingX = x + (PAGE_WIDTH - page.drawingWidth()) / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, page.drawing(), drawingX, cursor, 0.0F, 0.0F,
                    page.drawingWidth(), page.drawingHeight(), page.drawingWidth(), page.drawingHeight());
            cursor += page.drawingHeight() + 4;
            for (FormattedCharSequence text : page.caption()) {
                graphics.text(this.font, text, x, cursor, NOTE_INK, false);
                cursor += LINE_HEIGHT;
            }
            cursor += 4;
        }
        for (FormattedCharSequence text : page.body()) {
            graphics.text(this.font, text, x, cursor, INK, false);
            cursor += LINE_HEIGHT;
        }
        if (page.title() != null || !page.body().isEmpty()) {
            graphics.centeredText(this.font, Component.literal("- " + number + " -"), x + PAGE_WIDTH / 2, y + BOOK_HEIGHT - PAGE_TOP - PAGE_BOTTOM - 4, NOTE_INK);
        }
    }

    private int left() {
        return (this.width - BOOK_WIDTH) / 2;
    }

    private int top() {
        return Math.max(4, (this.height - BOOK_HEIGHT) / 2 - 12);
    }
}
