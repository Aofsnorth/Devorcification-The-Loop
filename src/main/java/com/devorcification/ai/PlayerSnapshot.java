package com.devorcification.ai;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class PlayerSnapshot {
    public UUID playerId;
    public String playerName;
    public BlockPos position;
    public Vec3d velocity;
    public float yaw;
    public float pitch;
    public float health;
    public float hunger;
    public int lightLevel;
    public String lookingAt;
    public boolean isSprinting;
    public boolean isSneaking;
    public boolean isInventoryOpen;
    public int rapidLookCount;
    public int backpedalCount;
    public long freezeDurationMs;
    public int defensiveBlockPlacements;
    public int chatMessageCount;
    public int cycleNumber;
    public long timeInLoopMs;
    public long capturedAtMs;
    public String fearProfile;

    public PlayerSnapshot() {}

    public PlayerSnapshot(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.capturedAtMs = System.currentTimeMillis();
    }
}
