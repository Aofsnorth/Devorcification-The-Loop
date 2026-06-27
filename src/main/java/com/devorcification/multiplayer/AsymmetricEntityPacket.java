package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
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
) implements FabricPacket {

    public static final PacketType<AsymmetricEntityPacket> TYPE =
        PacketType.create(new Identifier(Devorcification.MOD_ID, "asymmetric_entity"), AsymmetricEntityPacket::new);

    public AsymmetricEntityPacket(PacketByteBuf buf) {
        this(
            buf.readUuid(),
            buf.readBoolean(),
            buf.readString(32767),
            buf.readBoolean() ? buf.readUuid() : null,
            new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
            buf.readFloat(),
            buf.readFloat(),
            buf.readString(32767),
            buf.readVarInt()
        );
    }

    @Override
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
    public PacketType<?> getType() {
        return TYPE;
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
