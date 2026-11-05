package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.CodenameCoraline;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server-authoritative naming payloads triggered upon taming a BabyDragon. */
public final class DragonNamingPayloads {
    public record Prompt(int entityId) implements CustomPacketPayload {
        public static final Type<Prompt> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_name_prompt"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Prompt> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Prompt::entityId, Prompt::new);
        @Override public Type<Prompt> type() { return TYPE; }
    }

    public record Submit(int entityId, String name) implements CustomPacketPayload {
        public static final Type<Submit> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_name_submit"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Submit> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Submit::entityId, ByteBufCodecs.stringUtf8(64), Submit::name, Submit::new);
        @Override public Type<Submit> type() { return TYPE; }
    }

    @EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
            var registrar = event.registrar("1");
            registrar.playToClient(Prompt.TYPE, Prompt.CODEC);
            registrar.playToServer(Submit.TYPE, Submit.CODEC, (payload, context) -> {
                if (context.player().level().getEntity(payload.entityId()) instanceof BabyDragon dragon) {
                    dragon.acceptOwnerName(context.player(), payload.name());
                }
            });
        }
        private Registration() {}
    }

    private DragonNamingPayloads() {}
}

