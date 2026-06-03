package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record AsymmetricBlockPacket(
    UUID targetUuid,
    BlockPos pos,
    BlockState fakeState,
    int durationTicks
) implements CustomPayload {

    public static final CustomPayload.Id<AsymmetricBlockPacket> ID =
        new CustomPayload.Id<>(new Identifier(Devorcification.MOD_ID, "asymmetric_block"));

    public static final PacketCodec<PacketByteBuf, AsymmetricBlockPacket> CODEC =
        PacketCodec.of(AsymmetricBlockPacket::write, AsymmetricBlockPacket::read);

    public static AsymmetricBlockPacket read(PacketByteBuf buf) {
        UUID u = buf.readUuid();
        BlockPos p = buf.readBlockPos();
        int stateId = buf.readVarInt();
        BlockState state = BlockState.fromRawId(stateId);
        int dur = buf.readVarInt();
        return new AsymmetricBlockPacket(u, p, state, dur);
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(targetUuid);
        buf.writeBlockPos(pos);
        buf.writeVarInt(net.minecraft.block.Block.getRawIdFromState(fakeState));
        buf.writeVarInt(durationTicks);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
