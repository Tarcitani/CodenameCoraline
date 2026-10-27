package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.entity.basilisk.DeathBlight;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CodenameCoraline.MOD_ID);

    // Saved on logout, reset on death, and visible only to the affected player's client.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> DEATH_BLIGHT =
            TYPES.register("death_blight_buildup", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.intRange(0, 100).fieldOf("buildup"))
                    .sync((holder, player) -> holder == player, ByteBufCodecs.VAR_INT).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DeathBlight.Exposure>> BLIGHT_EXPOSURE =
            TYPES.register("death_blight_exposure", () -> AttachmentType.builder(DeathBlight.Exposure::new).build());

    private ModAttachments() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
