package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record AsymmetricChatPacket(
    UUID targetUuid,
    String fromPlayerName,
    String message,
    long fakeTimestampMs
) implements FabricPacket {

    public static final PacketType<AsymmetricChatPacket> TYPE =
        PacketType.create(new Identifier(Devorcification.MOD_ID, "asymmetric_chat"), AsymmetricChatPacket::new);

    public AsymmetricChatPacket(PacketByteBuf buf) {
        this(
            buf.readUuid(),
            buf.readString(32767),
            buf.readString(32767),
            buf.readLong()
        );
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(targetUuid);
        buf.writeString(fromPlayerName == null ? "" : fromPlayerName);
        buf.writeString(message == null ? "" : message);
        buf.writeLong(fakeTimestampMs);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
