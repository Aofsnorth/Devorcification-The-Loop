package com.devorcification.multiplayer;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RealitySnapshot {
    public UUID playerUuid;
    public long timestamp;
    public Map<BlockPos, BlockState> perceivedBlocks;
    public List<UUID> perceivedEntities;

    public RealitySnapshot() {}

    public RealitySnapshot(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.timestamp = System.currentTimeMillis();
        this.perceivedBlocks = new HashMap<>();
        this.perceivedEntities = new java.util.ArrayList<>();
    }
}
