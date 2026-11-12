package com.codenamecoraline.client.bossbar;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Project rule: every boss draws its own themed health bar instead of the vanilla strip. A boss registers a
 * {@link Style} here under the translation key of its entity name (the name its server boss event carries); the vanilla
 * bar is cancelled for it and the style draws frame, fill and name. Bosses without a style keep the vanilla bar.
 * To add one: write the texture generator under {@code tools/}, a {@link Style}, and register it in the static block.
 */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID, value = Dist.CLIENT)
public final class ThemedBossBars {
    /** One boss's bar. */
    public interface Style {
        /** Draws the whole bar with its frame centred on {@code centerX}, top edge at {@code top}. */
        void render(GuiGraphicsExtractor graphics, LerpingBossEvent boss, int centerX, int top);

        /** Vertical space the bar takes, including its name, so stacked bars do not overlap. */
        int height();
    }

    private static final Map<String, Style> STYLES = new HashMap<>();

    static {
        register("entity." + CodenameCoraline.MOD_ID + ".last_gift_collector", new LastGiftCollectorBossBar());
        register("entity." + CodenameCoraline.MOD_ID + ".canopy_breaker", new CanopyBreakerBossBar());
        register("entity." + CodenameCoraline.MOD_ID + ".crimson_commander", new CrimsonCommanderBossBar());
        register("entity." + CodenameCoraline.MOD_ID + ".architect", new ArchitectBossBar());
        TenThreadsWitchBossBar witchBar = new TenThreadsWitchBossBar();
        register("entity." + CodenameCoraline.MOD_ID + ".ten_threads_witch", witchBar);
        register("bossbar." + CodenameCoraline.MOD_ID + ".ten_threads_witch.stage", witchBar);
        register("bossbar." + CodenameCoraline.MOD_ID + ".ten_threads_witch.fury", witchBar);
        register("entity." + CodenameCoraline.MOD_ID + ".soul_reaper", new SoulReaperBossBar());
    }

    private ThemedBossBars() {}

    public static void register(String entityTranslationKey, Style style) {
        STYLES.put(entityTranslationKey, style);
    }

    public static @Nullable Style find(Component bossName) {
        if (bossName.getContents() instanceof TranslatableContents translatable) {
            return STYLES.get(translatable.getKey());
        }
        return null;
    }

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        Style style = find(event.getBossEvent().getName());
        if (style == null) {
            return;
        }
        event.setCanceled(true);
        int centerX = event.getWindow().getGuiScaledWidth() / 2;
        style.render(event.getGuiGraphics(), event.getBossEvent(), centerX, Math.max(4, event.getY() - 6));
        event.setIncrement(style.height());
    }
}
