package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record DirectedAudioPayload(
    UUID targetUuid,
    Identifier soundId,
    BlockPos origin,
    float volume,
    float pitch,
    boolean bypassDistance,
    boolean whisperSubtitle,
    String subtitleText
) implements FabricPacket {

    public static final PacketType<DirectedAudioPayload> TYPE =
        PacketType.create(new Identifier(Devorcification.MOD_ID, "directed_audio"), DirectedAudioPayload::new);

    public DirectedAudioPayload(PacketByteBuf buf) {
        this(
            buf.readUuid(),
            buf.readIdentifier(),
            buf.readBlockPos(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readString(32767)
        );
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(targetUuid);
        buf.writeIdentifier(soundId);
        buf.writeBlockPos(origin);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(bypassDistance);
        buf.writeBoolean(whisperSubtitle);
        buf.writeString(subtitleText == null ? "" : subtitleText);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
