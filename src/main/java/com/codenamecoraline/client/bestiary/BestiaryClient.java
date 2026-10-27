package com.codenamecoraline.client.bestiary;

import net.minecraft.client.Minecraft;

/** Client-side entry point of the bestiary book. */
public final class BestiaryClient {
    private BestiaryClient() {}

    public static void open() {
        Minecraft.getInstance().setScreen(new BestiaryScreen());
    }
}
