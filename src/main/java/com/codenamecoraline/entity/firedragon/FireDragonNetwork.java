package com.codenamecoraline.entity.firedragon;

import com.codenamecoraline.CodenameCoraline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-to-server payloads for Adult Fire Dragon mounted vertical flight controls and Z/X/C/V abilities. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class FireDragonNetwork {
    private FireDragonNetwork() {}

    public record MountInputPayload(int dragonId, boolean ascend, boolean descend) implements CustomPacketPayload {
        public static final Type<MountInputPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_mount_input"));
        public static final StreamCodec<ByteBuf, MountInputPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, MountInputPayload::dragonId,
                ByteBufCodecs.BOOL, MountInputPayload::ascend,
                ByteBufCodecs.BOOL, MountInputPayload::descend,
                MountInputPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AbilityPayload(int dragonId, int abilityIndex) implements CustomPacketPayload {
        public static final Type<AbilityPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "dragon_ability"));
        public static final StreamCodec<ByteBuf, AbilityPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, AbilityPayload::dragonId,
                ByteBufCodecs.VAR_INT, AbilityPayload::abilityIndex,
                AbilityPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    @SubscribeEvent
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(MountInputPayload.TYPE, MountInputPayload.STREAM_CODEC, FireDragonNetwork::handleMountInput);
        registrar.playToServer(AbilityPayload.TYPE, AbilityPayload.STREAM_CODEC, FireDragonNetwork::handleAbility);
    }

    private static void handleMountInput(MountInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getVehicle() instanceof FireDragon dragon
                    && dragon.getId() == payload.dragonId()
                    && dragon.isAdult() && dragon.isOwnedBy(player)) {
                dragon.setRiderVerticalInput(payload.ascend(), payload.descend());
            }
        });
    }

    private static void handleAbility(AbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getVehicle() instanceof FireDragon dragon
                    && dragon.getId() == payload.dragonId()
                    && dragon.isAdult() && dragon.isOwnedBy(player)) {
                dragon.triggerMountedAbility(player, payload.abilityIndex());
            }
        });
    }
}
