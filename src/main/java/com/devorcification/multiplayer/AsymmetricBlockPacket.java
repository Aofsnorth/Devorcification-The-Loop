package com.devorcification.multiplayer;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record AsymmetricBlockPacket(
    UUID targetUuid,
    BlockPos pos,
    BlockState fakeState,
    int durationTicks
) implements FabricPacket {

    public static final PacketType<AsymmetricBlockPacket> TYPE =
        PacketType.create(new Identifier(Devorcification.MOD_ID, "asymmetric_block"), AsymmetricBlockPacket::new);

    public AsymmetricBlockPacket(PacketByteBuf buf) {
        this(
            buf.readUuid(),
            buf.readBlockPos(),
            Block.getStateFromRawId(buf.readVarInt()),
            buf.readVarInt()
        );
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(targetUuid);
        buf.writeBlockPos(pos);
        buf.writeVarInt(Block.getRawIdFromState(fakeState));
        buf.writeVarInt(durationTicks);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
