package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record AsymmetricChatPacket(
    UUID targetUuid,
    String fromPlayerName,
    String message,
    long fakeTimestampMs
) implements CustomPayload {

    public static final CustomPayload.Id<AsymmetricChatPacket> ID =
        new CustomPayload.Id<>(new Identifier(Devorcification.MOD_ID, "asymmetric_chat"));

    public static final PacketCodec<PacketByteBuf, AsymmetricChatPacket> CODEC =
        PacketCodec.of(AsymmetricChatPacket::write, AsymmetricChatPacket::read);

    public static AsymmetricChatPacket read(PacketByteBuf buf) {
        UUID t = buf.readUuid();
        String name = buf.readString(32767);
        String msg = buf.readString(32767);
        long ts = buf.readLong();
        return new AsymmetricChatPacket(t, name, msg, ts);
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(targetUuid);
        buf.writeString(fromPlayerName == null ? "" : fromPlayerName);
        buf.writeString(message == null ? "" : message);
        buf.writeLong(fakeTimestampMs);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
