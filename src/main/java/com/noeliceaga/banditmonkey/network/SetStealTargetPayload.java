package com.noeliceaga.banditmonkey.network;

import com.noeliceaga.banditmonkey.BanditMonkey;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SetStealTargetPayload(UUID targetUUID) implements CustomPacketPayload {

    public static final Type<SetStealTargetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BanditMonkey.MODID, "set_steal_target"));

    public static final StreamCodec<ByteBuf, SetStealTargetPayload> CODEC = new StreamCodec<>() {
        @Override
        public SetStealTargetPayload decode(ByteBuf buf) {
            return new SetStealTargetPayload(new UUID(buf.readLong(), buf.readLong()));
        }
        @Override
        public void encode(ByteBuf buf, SetStealTargetPayload value) {
            buf.writeLong(value.targetUUID().getMostSignificantBits());
            buf.writeLong(value.targetUUID().getLeastSignificantBits());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
