package com.devorcification.audio;

import com.devorcification.Devorcification;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
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
) implements CustomPayload {

    public static final CustomPayload.Id<DirectedAudioPayload> ID =
        new CustomPayload.Id<>(new Identifier(Devorcification.MOD_ID, "directed_audio"));

    public static final PacketCodec<PacketByteBuf, DirectedAudioPayload> CODEC =
        PacketCodec.of(DirectedAudioPayload::write, DirectedAudioPayload::read);

    public static DirectedAudioPayload read(PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        Identifier soundId = buf.readIdentifier();
        BlockPos pos = buf.readBlockPos();
        float vol = buf.readFloat();
        float pitch = buf.readFloat();
        boolean bypass = buf.readBoolean();
        boolean whisper = buf.readBoolean();
        String subtitle = buf.readString(32767);
        return new DirectedAudioPayload(uuid, soundId, pos, vol, pitch, bypass, whisper, subtitle);
    }

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
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
