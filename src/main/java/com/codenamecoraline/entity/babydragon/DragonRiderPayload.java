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

/** Intent only: no coordinates, damage, inventory or XP are accepted from clients. */
public record DragonRiderPayload(int buttons, float yaw, float pitch) implements CustomPacketPayload {
    public static final Type<DragonRiderPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_rider"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonRiderPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DragonRiderPayload::buttons, ByteBufCodecs.FLOAT, DragonRiderPayload::yaw,
            ByteBufCodecs.FLOAT, DragonRiderPayload::pitch, DragonRiderPayload::new);
    @Override public Type<DragonRiderPayload> type() { return TYPE; }
    @EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToServer(TYPE, CODEC, (payload, context) -> {
                if (context.player().getVehicle() instanceof BabyDragon dragon)
                    dragon.acceptRiderInput(context.player(), payload.buttons, payload.yaw, payload.pitch);
            });
        }
        private Registration() {}
    }
}
