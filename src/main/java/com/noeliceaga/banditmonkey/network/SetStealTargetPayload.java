package com.noeliceaga.banditmonkey.network;

import com.noeliceaga.banditmonkey.BanditMonkey;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetStealTargetPayload(int targetEntityId) implements CustomPacketPayload {

    public static final Type<SetStealTargetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BanditMonkey.MODID, "set_steal_target"));

    public static final StreamCodec<ByteBuf, SetStealTargetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SetStealTargetPayload::targetEntityId,
            SetStealTargetPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
