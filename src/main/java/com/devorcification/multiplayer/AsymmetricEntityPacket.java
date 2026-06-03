package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record AsymmetricEntityPacket(
    UUID targetUuid,
    boolean spawn,
    String entityType,
    UUID mimicTargetUuid,
    Vec3d position,
    float yaw,
    float pitch,
    String behavior,
    int durationTicks
) implements CustomPayload {

    public static final CustomPayload.Id<AsymmetricEntityPacket> ID =
        new CustomPayload.Id<>(new Identifier(Devorcification.MOD_ID, "asymmetric_entity"));

    public static final PacketCodec<PacketByteBuf, AsymmetricEntityPacket> CODEC =
        PacketCodec.of(AsymmetricEntityPacket::write, AsymmetricEntityPacket::read);

    public static AsymmetricEntityPacket read(PacketByteBuf buf) {
        UUID t = buf.readUuid();
        boolean spawn = buf.readBoolean();
        String et = buf.readString(32767);
        boolean hasMimic = buf.readBoolean();
        UUID mimic = hasMimic ? buf.readUuid() : null;
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        String beh = buf.readString(32767);
        int dur = buf.readVarInt();
        return new AsymmetricEntityPacket(t, spawn, et, mimic, new Vec3d(x, y, z), yaw, pitch, beh, dur);
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(targetUuid);
        buf.writeBoolean(spawn);
        buf.writeString(entityType == null ? "" : entityType);
        buf.writeBoolean(mimicTargetUuid != null);
        if (mimicTargetUuid != null) buf.writeUuid(mimicTargetUuid);
        buf.writeDouble(position == null ? 0 : position.x);
        buf.writeDouble(position == null ? 0 : position.y);
        buf.writeDouble(position == null ? 0 : position.z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeString(behavior == null ? "stand_stare" : behavior);
        buf.writeVarInt(durationTicks);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static AsymmetricEntityPacket spawn(UUID target, FakeEntityData d) {
        return new AsymmetricEntityPacket(target, true, d.entityType, d.mimicTargetUuid,
            d.position, d.yaw, d.pitch, d.behavior, d.durationTicks);
    }

    public static AsymmetricEntityPacket despawn(UUID target, String entityType) {
        return new AsymmetricEntityPacket(target, false, entityType, null,
            Vec3d.ZERO, 0f, 0f, "", 0);
    }
}
